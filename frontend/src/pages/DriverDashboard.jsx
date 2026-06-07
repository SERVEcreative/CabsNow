import { useCallback, useEffect, useMemo, useState } from 'react';
import { rides } from '../api';
import IncomingRideCard from '../components/IncomingRideCard';
import LiveMap from '../components/LiveMap';
import LiveRideStatus from '../components/LiveRideStatus';
import { buildDeadlineFromDuty, useRideSocket } from '../hooks/useRideSocket';
import { useGeolocation, useLocationSync } from '../hooks/useGeolocation';

export default function DriverDashboard() {
  const driverId = localStorage.getItem('userId');
  const driverName = localStorage.getItem('name') || 'Driver';

  const [incomingRide, setIncomingRide] = useState(null);
  const [activeRide, setActiveRide] = useState(null);
  const [liveEvent, setLiveEvent] = useState(null);
  const [msg, setMsg] = useState('');
  const [accepting, setAccepting] = useState(false);
  const [isOnline, setIsOnline] = useState(false);

  const { position, error: geoError, fallback } = useGeolocation(true);
  const mapPosition = position || fallback;

  const topics = useMemo(() => [`/topic/driver/${driverId}`], [driverId]);

  const handleRideEvent = useCallback((event) => {
    setLiveEvent(event);

    if (event.status === 'PENDING') {
      const deadline = event.acceptDeadlineEpochMs || Date.now() + 30000;
      if (deadline > Date.now()) {
        setIncomingRide({ ...event, acceptDeadlineEpochMs: deadline });
      }
    }

    if (event.status === 'ACCEPTED') {
      setIncomingRide(null);
      if (event.driverId === Number(driverId)) {
        setActiveRide(event);
        setMsg('Ride accepted — synced with rider');
      } else {
        setMsg('Ride taken by another driver');
        setTimeout(() => setMsg(''), 3000);
      }
    }

    if (event.status === 'COMPLETED' || event.status === 'REJECTED') {
      if (!event.driverId || event.driverId === Number(driverId)) {
        setActiveRide(null);
        setIncomingRide(null);
      }
    }
  }, [driverId]);

  useRideSocket(topics, handleRideEvent);

  const syncLocation = useCallback((loc) => rides.updateLocation(loc), []);
  useLocationSync(mapPosition, syncLocation, 5000, isOnline);

  useEffect(() => {
    let active = true;

    (async () => {
      try {
        await rides.goOnline({
          latitude: fallback.lat,
          longitude: fallback.lng,
        });
        if (active) {
          setIsOnline(true);
          setMsg('You are online — GPS tracking active');
        }
      } catch {
        if (active) setMsg('Could not go online. Is backend running?');
      }
    })();

    return () => {
      active = false;
      rides.goOffline().catch(() => {});
    };
  }, [fallback.lat, fallback.lng]);

  useEffect(() => {
    if (!isOnline) return;
    const loadPending = async () => {
      try {
        const { data } = await rides.pendingDuties();
        if (!data?.length || activeRide) return;
        const duty = data[0];
        const deadline = buildDeadlineFromDuty(duty);
        if (deadline > Date.now()) {
          setIncomingRide({
            dutyId: duty.dutyId,
            pickupLocation: duty.pickupLocation,
            dropLocation: duty.dropLocation,
            pickupLat: duty.pickupLat,
            pickupLng: duty.pickupLng,
            dropLat: duty.dropLat,
            dropLng: duty.dropLng,
            fare: duty.fare,
            vehicleType: duty.vehicleType,
            status: 'PENDING',
            acceptDeadlineEpochMs: deadline,
          });
        }
      } catch {
        /* no pending */
      }
    };
    loadPending();
    const interval = setInterval(loadPending, 3000);
    return () => clearInterval(interval);
  }, [isOnline, activeRide]);

  const accept = async (dutyId) => {
    setAccepting(true);
    try {
      await rides.accept(dutyId);
      setIncomingRide(null);
      setMsg('Ride accepted!');
    } catch (err) {
      setMsg(err.response?.data?.error || 'Could not accept — ride may be taken');
      setIncomingRide(null);
    } finally {
      setAccepting(false);
    }
  };

  const skip = (dutyId) => {
    setIncomingRide((current) => (current?.dutyId === dutyId ? null : current));
  };

  const complete = async (dutyId) => {
    try {
      await rides.complete(dutyId);
      setActiveRide(null);
      setMsg('Ride completed — you are available again');
    } catch {
      setMsg('Could not complete ride');
    }
  };

  const mapRide = activeRide || incomingRide;

  return (
    <div className="container">
      <div className="dashboard-header">
        <div>
          <h2 className="page-title">Hi, {driverName}</h2>
          <p className="page-sub">Live GPS · geo-targeted requests</p>
        </div>
        <span className={`driver-status-chip ${isOnline ? 'online' : ''}`}>
          {isOnline ? 'Online · GPS on' : 'Connecting…'}
        </span>
      </div>

      {incomingRide && (
        <IncomingRideCard
          ride={incomingRide}
          onAccept={accept}
          onSkip={skip}
          accepting={accepting}
        />
      )}

      <div className="card location-card">
        <h3>Live map</h3>
        <p className="page-sub">
          {position ? 'Using your device GPS (updates every 5s)' : 'Allow location access for live GPS demo'}
        </p>
        <LiveMap
          height={320}
          follow="driver"
          driver={mapPosition}
          pickup={mapRide?.pickupLat != null ? { lat: mapRide.pickupLat, lng: mapRide.pickupLng } : null}
          drop={mapRide?.dropLat != null ? { lat: mapRide.dropLat, lng: mapRide.dropLng } : null}
        />
        <div className="map-legend">
          <span><i className="dot pickup-dot" /> Pickup</span>
          <span><i className="dot drop-dot" /> Drop</span>
          <span><i className="dot driver-dot" /> You (driver)</span>
        </div>
        {geoError && <p className="error">{geoError} — using Delhi default for demo.</p>}
      </div>

      {liveEvent && <LiveRideStatus event={liveEvent} role="DRIVER" />}

      {activeRide && (
        <div className="card">
          <h3>Active ride #{activeRide.dutyId}</h3>
          <div className="ride-route">
            <div className="route-point">
              <span className="dot pickup-dot" />
              <div><small>Pickup</small><p>{activeRide.pickupLocation}</p></div>
            </div>
            <div className="route-line" />
            <div className="route-point">
              <span className="dot drop-dot" />
              <div><small>Drop</small><p>{activeRide.dropLocation}</p></div>
            </div>
          </div>
          <p className="live-fare">₹{activeRide.fare}</p>
          <button type="button" className="btn btn-success" onClick={() => complete(activeRide.dutyId)}>
            Complete ride
          </button>
        </div>
      )}

      {msg && (
        <p className={msg.includes('accepted') || msg.includes('completed') || msg.includes('online') || msg.includes('GPS') ? 'success' : 'error'}>
          {msg}
        </p>
      )}
    </div>
  );
}
