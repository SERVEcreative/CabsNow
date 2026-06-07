import { useCallback, useMemo, useState } from 'react';
import { rides, payments, ratings } from '../api';
import FindingDriverOverlay from '../components/FindingDriverOverlay';
import DriverMatchedToast, { useMatchedToast } from '../components/DriverMatchedToast';
import LiveMap from '../components/LiveMap';
import LiveRideStatus from '../components/LiveRideStatus';
import StepIndicator from '../components/StepIndicator';
import { useRideSocket } from '../hooks/useRideSocket';
import { useGeolocation } from '../hooks/useGeolocation';

const VEHICLES = ['BIKE', 'ECONOMY', 'SEDAN', 'SUV', 'LUXURY'];

function getStep(status) {
  if (!status) return 0;
  if (status === 'PENDING') return 1;
  if (status === 'ACCEPTED') return 2;
  if (status === 'COMPLETED') return 3;
  return 0;
}

function pointFromEvent(event, latKey, lngKey) {
  const lat = event?.[latKey];
  const lng = event?.[lngKey];
  if (lat == null || lng == null) return null;
  return { lat: +lat, lng: +lng };
}

export default function RiderDashboard() {
  const riderId = localStorage.getItem('userId');
  const riderName = localStorage.getItem('name') || 'Rider';

  const [pickup, setPickup] = useState('Connaught Place, Delhi');
  const [drop, setDrop] = useState('IGI Airport, Delhi');
  const [vehicleType, setVehicleType] = useState('SEDAN');
  const [pickupLat, setPickupLat] = useState('28.6315');
  const [pickupLon, setPickupLon] = useState('77.2167');
  const [dropLat, setDropLat] = useState('28.5562');
  const [dropLon, setDropLon] = useState('77.1000');
  const [fare, setFare] = useState(null);
  const [booking, setBooking] = useState(false);
  const [liveEvent, setLiveEvent] = useState(null);
  const [lastDutyId, setLastDutyId] = useState(null);
  const [driverPos, setDriverPos] = useState(null);
  const [rating, setRating] = useState({ stars: 5, comment: '' });
  const [msg, setMsg] = useState('');

  const { position: riderGps, error: geoError } = useGeolocation(true);

  const topics = useMemo(() => {
    const list = [`/topic/rider/${riderId}`];
    if (lastDutyId) list.push(`/topic/ride/${lastDutyId}`);
    return list;
  }, [riderId, lastDutyId]);

  const handleRideEvent = useCallback((event) => {
    setLiveEvent((prev) => ({ ...prev, ...event }));
    if (event.dutyId) setLastDutyId(event.dutyId);

    if (event.driverLat != null && event.driverLng != null) {
      setDriverPos({ lat: +event.driverLat, lng: +event.driverLng });
    } else if (event.status === 'ACCEPTED' && event.driverLat != null) {
      setDriverPos({ lat: +event.driverLat, lng: +event.driverLng });
    }
  }, []);

  useRideSocket(topics, handleRideEvent);

  const useMyLocation = () => {
    if (!riderGps) {
      setMsg('Allow location access in browser settings');
      return;
    }
    setPickupLat(String(riderGps.lat));
    setPickupLon(String(riderGps.lng));
    setPickup('My current location');
    setMsg('Pickup set from GPS');
  };

  const estimateFare = async () => {
    try {
      const { data } = await rides.getFare({ pickupLat, pickupLon, dropLat, dropLon, vehicleType });
      setFare(data);
      setMsg('');
    } catch {
      setMsg('Could not estimate fare. Is backend running on port 8080?');
    }
  };

  const bookRide = async () => {
    if (!fare) {
      await estimateFare();
      return;
    }
    setBooking(true);
    setDriverPos(null);
    setLiveEvent({
      status: 'PENDING',
      message: 'Connecting to nearby drivers…',
      pickupLocation: pickup,
      dropLocation: drop,
      pickupLat: +pickupLat,
      pickupLng: +pickupLon,
      dropLat: +dropLat,
      dropLng: +dropLon,
      fare,
      vehicleType,
      acceptDeadlineEpochMs: Date.now() + 30000,
    });
    try {
      const { data } = await rides.book({
        pickupLocation: pickup,
        dropLocation: drop,
        vehicleType,
        fare,
        pickupLat: +pickupLat,
        pickupLng: +pickupLon,
        dropLat: +dropLat,
        dropLng: +dropLon,
      });
      setLastDutyId(data.dutyId);
      setLiveEvent({
        dutyId: data.dutyId,
        status: 'PENDING',
        message: 'Notifying nearby drivers…',
        pickupLocation: pickup,
        dropLocation: drop,
        pickupLat: data.pickupLat ?? +pickupLat,
        pickupLng: data.pickupLng ?? +pickupLon,
        dropLat: data.dropLat ?? +dropLat,
        dropLng: data.dropLng ?? +dropLon,
        fare,
        vehicleType,
        acceptDeadlineEpochMs: Date.now() + 30000,
      });
      setMsg('');
    } catch (err) {
      setLiveEvent(null);
      setMsg(err.response?.data?.error || 'Booking failed');
    } finally {
      setBooking(false);
    }
  };

  const cancelRide = async () => {
    try {
      await rides.cancel();
      setLiveEvent({ status: 'REJECTED', message: 'Ride cancelled' });
      setDriverPos(null);
      setMsg('');
    } catch (err) {
      setMsg(err.response?.data?.error || 'Cancel failed');
    }
  };

  const pay = async () => {
    if (!lastDutyId) return;
    try {
      await payments.create(lastDutyId);
      await payments.confirm(lastDutyId);
      setMsg('Payment successful');
    } catch (err) {
      setMsg(err.response?.data?.error || 'Payment failed');
    }
  };

  const submitRating = async () => {
    if (!lastDutyId) return;
    try {
      await ratings.submit({ dutyId: lastDutyId, stars: rating.stars, comment: rating.comment });
      setMsg('Thanks for rating!');
    } catch (err) {
      setMsg(err.response?.data?.error || 'Rating failed');
    }
  };

  const isSearching = liveEvent?.status === 'PENDING';
  const isActive = liveEvent?.status === 'ACCEPTED';
  const isDone = liveEvent?.status === 'COMPLETED';
  const showMap = isSearching || isActive || isDone;
  const { show: showMatchedToast, dismiss: dismissMatchedToast } = useMatchedToast(liveEvent);

  const pickupPoint = pointFromEvent(liveEvent, 'pickupLat', 'pickupLng')
    || { lat: +pickupLat, lng: +pickupLon };
  const dropPoint = pointFromEvent(liveEvent, 'dropLat', 'dropLng')
    || { lat: +dropLat, lng: +dropLon };

  return (
    <div className="container">
      {(isSearching || booking) && liveEvent?.status !== 'REJECTED' && (
        <FindingDriverOverlay
          ride={liveEvent || {
            pickupLocation: pickup,
            dropLocation: drop,
            fare,
            vehicleType,
            acceptDeadlineEpochMs: Date.now() + 30000,
          }}
          onCancel={cancelRide}
        />
      )}

      {showMatchedToast && liveEvent?.driverName && (
        <DriverMatchedToast driverName={liveEvent.driverName} onDone={dismissMatchedToast} />
      )}

      <h2 className="page-title">Hello, {riderName}</h2>
      <p className="page-sub">Book a ride — track driver live on map</p>

      <StepIndicator
        steps={['Details', 'Finding driver', 'On trip', 'Done']}
        current={getStep(liveEvent?.status)}
      />

      {showMap && (
        <div className="card">
          <h3>{isActive ? 'Driver approaching' : 'Ride map'}</h3>
          <p className="page-sub">
            {isActive && driverPos
              ? 'Blue dot = your driver (live GPS via WebSocket)'
              : 'Green = pickup · Red = drop'}
          </p>
          <LiveMap
            height={340}
            follow={isActive && driverPos ? 'driver' : 'rider'}
            pickup={pickupPoint}
            drop={dropPoint}
            driver={driverPos}
            rider={riderGps}
          />
          <div className="map-legend">
            <span><i className="dot pickup-dot" /> Pickup</span>
            <span><i className="dot drop-dot" /> Drop</span>
            {driverPos && <span><i className="dot driver-dot" /> Driver</span>}
            {riderGps && <span><i className="dot rider-dot" /> You</span>}
          </div>
        </div>
      )}

      {liveEvent && liveEvent.status && !isSearching && (
        <LiveRideStatus event={liveEvent} role="RIDER" />
      )}

      {!isActive && !isDone && (
        <div className="card">
          <h3>Where to?</h3>
          <label>Pickup</label>
          <input value={pickup} onChange={(e) => setPickup(e.target.value)} disabled={isSearching} placeholder="Enter pickup" />
          <button type="button" className="btn btn-outline gps-btn" onClick={useMyLocation} disabled={isSearching}>
            Use my GPS for pickup
          </button>
          <label>Drop</label>
          <input value={drop} onChange={(e) => setDrop(e.target.value)} disabled={isSearching} placeholder="Enter destination" />

          <label>Vehicle</label>
          <div className="vehicle-pills">
            {VEHICLES.map((v) => (
              <button
                key={v}
                type="button"
                className={`vehicle-pill ${vehicleType === v ? 'active' : ''}`}
                onClick={() => setVehicleType(v)}
                disabled={isSearching}
              >
                {v}
              </button>
            ))}
          </div>

          <div className="action-row">
            <button type="button" className="btn btn-outline" onClick={estimateFare} disabled={isSearching}>
              Estimate fare
            </button>
            <button type="button" className="btn btn-primary" onClick={bookRide} disabled={booking || isSearching}>
              {booking ? 'Booking…' : 'Book now'}
            </button>
            {isSearching && (
              <button type="button" className="btn btn-danger" onClick={cancelRide}>
                Cancel
              </button>
            )}
          </div>

          {fare != null && <p className="success">Estimated fare: ₹{fare}</p>}
          {geoError && !showMap && <p className="error">{geoError}</p>}
          {msg && <p className={msg.includes('Thanks') || msg.includes('Payment') || msg.includes('Pickup') ? 'success' : 'error'}>{msg}</p>}
        </div>
      )}

      {isActive && lastDutyId && (
        <div className="card">
          <h3>Trip in progress</h3>
          <p className="page-sub">Driver location updates every 5 seconds</p>
        </div>
      )}

      {lastDutyId && (isActive || isDone) && (
        <div className="card">
          <h3>After ride</h3>
          <button type="button" className="btn btn-primary" onClick={pay}>Pay (Mock UPI)</button>
          <div style={{ marginTop: 16 }}>
            <label>Rate driver (1–5)</label>
            <input type="number" min={1} max={5} value={rating.stars} onChange={(e) => setRating({ ...rating, stars: +e.target.value })} />
            <label>Comment</label>
            <textarea value={rating.comment} onChange={(e) => setRating({ ...rating, comment: e.target.value })} rows={3} />
            <button type="button" className="btn btn-success" onClick={submitRating}>Submit rating</button>
          </div>
        </div>
      )}
    </div>
  );
}
