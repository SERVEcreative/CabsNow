import { useCallback, useEffect, useMemo, useState } from 'react';
import { rides } from '../api';
import IncomingRideCard from '../components/IncomingRideCard';
import LiveRideStatus from '../components/LiveRideStatus';
import { buildDeadlineFromDuty, useRideSocket } from '../hooks/useRideSocket';

export default function DriverDashboard() {
  const driverId = localStorage.getItem('userId');
  const driverName = localStorage.getItem('name') || 'Driver';

  const [incomingRide, setIncomingRide] = useState(null);
  const [activeRide, setActiveRide] = useState(null);
  const [liveEvent, setLiveEvent] = useState(null);
  const [msg, setMsg] = useState('');
  const [accepting, setAccepting] = useState(false);
  const [isOnline, setIsOnline] = useState(false);
  const [location, setLocation] = useState({ latitude: 28.6139, longitude: 77.2090 });

  const topics = useMemo(() => [`/topic/driver/${driverId}`], [driverId]);

  const handleRideEvent = useCallback((event) => {
    setLiveEvent(event);

    if (event.status === 'PENDING') {
      const deadline = event.acceptDeadlineEpochMs || Date.now() + 5000;
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

  useEffect(() => {
    const goOnline = async () => {
      try {
        await rides.goOnline(location);
        setIsOnline(true);
        setMsg('You are online — receiving nearby rides only');
      } catch {
        setMsg('Could not go online. Is backend running?');
      }
    };
    goOnline();

    return () => {
      rides.goOffline().catch(() => {});
    };
  }, []);

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
  }, [isOnline, activeRide]);

  const shareLocation = async () => {
    try {
      if (isOnline) {
        await rides.goOnline(location);
      } else {
        await rides.updateLocation(location);
      }
      setMsg('Location updated — matching improved');
    } catch {
      setMsg('Could not update location');
    }
  };

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

  return (
    <div className="container">
      <div className="dashboard-header">
        <div>
          <h2 className="page-title">Hi, {driverName}</h2>
          <p className="page-sub">Geo-targeted requests · 5 sec to accept</p>
        </div>
        <span className={`driver-status-chip ${isOnline ? 'online' : ''}`}>
          {isOnline ? 'Online — nearby only' : 'Connecting…'}
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

      <div className="online-banner">
        <h3>Smart dispatch active</h3>
        <p>You only receive rides near your location — not every driver in the city.</p>
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

      <div className="card location-card">
        <h3>Your location</h3>
        <div className="map-placeholder">📍 Delhi NCR — drivers matched within 10 km</div>
        <div className="grid-2">
          <div>
            <label>Latitude</label>
            <input type="number" step="any" value={location.latitude} onChange={(e) => setLocation({ ...location, latitude: +e.target.value })} />
          </div>
          <div>
            <label>Longitude</label>
            <input type="number" step="any" value={location.longitude} onChange={(e) => setLocation({ ...location, longitude: +e.target.value })} />
          </div>
        </div>
        <button type="button" className="btn btn-primary" onClick={shareLocation}>
          Update location
        </button>
      </div>

      {msg && (
        <p className={msg.includes('accepted') || msg.includes('completed') || msg.includes('online') || msg.includes('Location') ? 'success' : 'error'}>
          {msg}
        </p>
      )}
    </div>
  );
}
