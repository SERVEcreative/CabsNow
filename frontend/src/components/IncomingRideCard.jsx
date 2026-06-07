import { useCountdown } from '../hooks/useRideSocket';

export default function IncomingRideCard({ ride, onAccept, onSkip, accepting }) {
  const secondsLeft = useCountdown(ride.acceptDeadlineEpochMs);
  const expired = secondsLeft <= 0;
  const progress = Math.min(100, (secondsLeft / 30) * 100);

  if (expired) return null;

  return (
    <div className="incoming-ride-overlay">
      <div className="incoming-ride-card">
        <div className="incoming-ride-header">
          <span className="incoming-label">New ride request</span>
          <div className="timer-ring" style={{ '--progress': `${progress}%` }}>
            <span className="timer-value">{secondsLeft}</span>
          </div>
        </div>

        <div className="ride-route">
          <div className="route-point">
            <span className="dot pickup-dot" />
            <div>
              <small>Pickup</small>
              <p>{ride.pickupLocation}</p>
            </div>
          </div>
          <div className="route-line" />
          <div className="route-point">
            <span className="dot drop-dot" />
            <div>
              <small>Drop</small>
              <p>{ride.dropLocation}</p>
            </div>
          </div>
        </div>

        <div className="incoming-meta">
          <span className="badge badge-blue">{ride.vehicleType}</span>
          <span className="fare-tag">₹{ride.fare}</span>
        </div>

        <div className="incoming-actions">
          <button
            type="button"
            className="btn btn-outline btn-lg"
            onClick={() => onSkip(ride.dutyId)}
            disabled={accepting}
          >
            Skip
          </button>
          <button
            type="button"
            className="btn btn-primary btn-lg"
            onClick={() => onAccept(ride.dutyId)}
            disabled={accepting || expired}
          >
            {accepting ? 'Accepting…' : 'Accept Ride'}
          </button>
        </div>
      </div>
    </div>
  );
}
