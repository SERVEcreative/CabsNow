import { useCountdown } from '../hooks/useRideSocket';

const STATUS_LABELS = {
  PENDING: 'Finding driver',
  ACCEPTED: 'Driver on the way',
  COMPLETED: 'Ride completed',
  REJECTED: 'Ride cancelled',
};

export default function LiveRideStatus({ event, role }) {
  if (!event) return null;

  const label = STATUS_LABELS[event.status] || event.status;
  const isSearching = event.status === 'PENDING';
  const isMatched = event.status === 'ACCEPTED';
  const isDone = event.status === 'COMPLETED';

  return (
    <div className={`live-ride-banner status-${event.status?.toLowerCase()}`}>
      <div className="live-ride-top">
        <div>
          {isSearching && (
            <div className="pulse-dot" aria-hidden />
          )}
          <div>
            <p className="live-label">{label}</p>
            <p className="live-message">{event.message}</p>
          </div>
        </div>
        <span className="badge badge-blue">{event.status}</span>
      </div>

      {(event.pickupLocation || event.dropLocation) && (
        <div className="live-route">
          <p><strong>From:</strong> {event.pickupLocation}</p>
          <p><strong>To:</strong> {event.dropLocation}</p>
          {event.fare != null && <p className="live-fare">Fare: ₹{event.fare}</p>}
        </div>
      )}

      {role === 'RIDER' && isSearching && event.acceptDeadlineEpochMs && (
        <RiderCountdown deadline={event.acceptDeadlineEpochMs} />
      )}

      {role === 'RIDER' && isSearching && event.nearbyDriversNotified != null && (
        <p className="search-hint">
          Notified <strong>{event.nearbyDriversNotified}</strong> nearby driver{event.nearbyDriversNotified !== 1 ? 's' : ''}
        </p>
      )}

      {role === 'RIDER' && isMatched && event.driverName && (
        <div className="driver-matched">
          <div className="driver-avatar">{event.driverName.charAt(0)}</div>
          <div>
            <p className="driver-name">{event.driverName}</p>
            <p className="driver-sub">Your driver is assigned — live synced</p>
          </div>
        </div>
      )}

      {role === 'DRIVER' && isMatched && (
        <p className="driver-sub">Active ride — complete when you drop the rider</p>
      )}

      {isDone && (
        <p className="success">Thank you for riding with CabsNow!</p>
      )}
    </div>
  );
}

function RiderCountdown({ deadline }) {
  const left = useCountdown(deadline);
  if (left <= 0) {
    return <p className="search-hint">Still searching for nearby drivers…</p>;
  }
  return (
    <p className="search-hint">
      Drivers have <strong>{left}s</strong> to accept your request
    </p>
  );
}
