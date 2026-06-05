import { useCallback, useMemo, useState } from 'react';
import { rides, payments, ratings } from '../api';
import FindingDriverOverlay from '../components/FindingDriverOverlay';
import DriverMatchedToast, { useMatchedToast } from '../components/DriverMatchedToast';
import LiveRideStatus from '../components/LiveRideStatus';
import StepIndicator from '../components/StepIndicator';
import { useRideSocket } from '../hooks/useRideSocket';

const VEHICLES = ['BIKE', 'ECONOMY', 'SEDAN', 'SUV', 'LUXURY'];

function getStep(status) {
  if (!status) return 0;
  if (status === 'PENDING') return 1;
  if (status === 'ACCEPTED') return 2;
  if (status === 'COMPLETED') return 3;
  return 0;
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
  const [rating, setRating] = useState({ stars: 5, comment: '' });
  const [msg, setMsg] = useState('');

  const topics = useMemo(() => [`/topic/rider/${riderId}`], [riderId]);

  const handleRideEvent = useCallback((event) => {
    setLiveEvent(event);
    setLastDutyId(event.dutyId);
  }, []);

  useRideSocket(topics, handleRideEvent);

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
    setLiveEvent({
      status: 'PENDING',
      message: 'Connecting to nearby drivers…',
      pickupLocation: pickup,
      dropLocation: drop,
      fare,
      vehicleType,
      acceptDeadlineEpochMs: Date.now() + 5000,
    });
    try {
      const { data } = await rides.book({
        pickupLocation: pickup,
        dropLocation: drop,
        vehicleType,
        fare,
        pickupLat: +pickupLat,
        pickupLng: +pickupLon,
      });
      setLastDutyId(data.dutyId);
      setLiveEvent({
        dutyId: data.dutyId,
        status: 'PENDING',
        message: 'Notifying nearby drivers…',
        pickupLocation: pickup,
        dropLocation: drop,
        fare,
        vehicleType,
        acceptDeadlineEpochMs: Date.now() + 5000,
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
  const { show: showMatchedToast, dismiss: dismissMatchedToast } = useMatchedToast(liveEvent);

  return (
    <div className="container">
      {(isSearching || booking) && liveEvent?.status !== 'REJECTED' && (
        <FindingDriverOverlay
          ride={liveEvent || {
            pickupLocation: pickup,
            dropLocation: drop,
            fare,
            vehicleType,
            acceptDeadlineEpochMs: Date.now() + 5000,
          }}
          onCancel={cancelRide}
        />
      )}

      {showMatchedToast && liveEvent?.driverName && (
        <DriverMatchedToast driverName={liveEvent.driverName} onDone={dismissMatchedToast} />
      )}

      <h2 className="page-title">Hello, {riderName}</h2>
      <p className="page-sub">Book a ride — only nearby drivers get notified</p>

      <StepIndicator
        steps={['Details', 'Finding driver', 'On trip', 'Done']}
        current={getStep(liveEvent?.status)}
      />

      {liveEvent && liveEvent.status && !isSearching && (
        <LiveRideStatus event={liveEvent} role="RIDER" />
      )}

      {!isActive && !isDone && (
        <div className="card">
          <h3>Where to?</h3>
          <label>Pickup</label>
          <input value={pickup} onChange={(e) => setPickup(e.target.value)} disabled={isSearching} placeholder="Enter pickup" />
          <label>Drop</label>
          <input value={drop} onChange={(e) => setDrop(e.target.value)} disabled={isSearching} placeholder="Enter destination" />

          <div className="grid-2">
            <div><label>Pickup lat</label><input value={pickupLat} onChange={(e) => setPickupLat(e.target.value)} disabled={isSearching} /></div>
            <div><label>Pickup lng</label><input value={pickupLon} onChange={(e) => setPickupLon(e.target.value)} disabled={isSearching} /></div>
            <div><label>Drop lat</label><input value={dropLat} onChange={(e) => setDropLat(e.target.value)} disabled={isSearching} /></div>
            <div><label>Drop lng</label><input value={dropLon} onChange={(e) => setDropLon(e.target.value)} disabled={isSearching} /></div>
          </div>

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
          {msg && <p className={msg.includes('Thanks') || msg.includes('Payment') ? 'success' : 'error'}>{msg}</p>}
        </div>
      )}

      {isActive && lastDutyId && (
        <div className="card">
          <h3>Trip in progress</h3>
          <p className="page-sub">Live synced with your driver via WebSocket</p>
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
