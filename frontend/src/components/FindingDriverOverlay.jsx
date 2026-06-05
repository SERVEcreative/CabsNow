import { useEffect, useState } from 'react';
import { useCountdown } from '../hooks/useRideSocket';

const SEARCH_MESSAGES = [
  'Scanning drivers near you…',
  'Notifying nearby captains…',
  'Waiting for a driver to accept…',
];

export default function FindingDriverOverlay({ ride, onCancel }) {
  const secondsLeft = useCountdown(ride?.acceptDeadlineEpochMs);
  const expired = ride?.acceptDeadlineEpochMs ? secondsLeft <= 0 : false;
  const [msgIndex, setMsgIndex] = useState(0);

  useEffect(() => {
    const id = setInterval(() => {
      setMsgIndex((i) => (i + 1) % SEARCH_MESSAGES.length);
    }, 2200);
    return () => clearInterval(id);
  }, []);

  if (!ride) return null;

  return (
    <div className="finding-overlay" role="dialog" aria-modal="true" aria-label="Finding driver">
      <div className="finding-card">
        <div className="finding-radar" aria-hidden>
          <span className="radar-ring ring-1" />
          <span className="radar-ring ring-2" />
          <span className="radar-ring ring-3" />
          <span className="radar-center">🚗</span>
        </div>

        <h2 className="finding-title">Finding your driver</h2>
        <p className="finding-subtitle">{SEARCH_MESSAGES[msgIndex]}</p>

        {!expired && ride.acceptDeadlineEpochMs && (
          <div className="finding-timer">
            <span className="finding-timer-label">Drivers can accept for</span>
            <span className="finding-timer-value">{secondsLeft}s</span>
          </div>
        )}

        {expired && (
          <p className="finding-still">Still looking… hang tight, we&apos;re on it</p>
        )}

        {ride.nearbyDriversNotified != null && ride.nearbyDriversNotified > 0 && (
          <p className="finding-drivers-count">
            {ride.nearbyDriversNotified} driver{ride.nearbyDriversNotified !== 1 ? 's' : ''} notified nearby
          </p>
        )}

        <div className="finding-route">
          <div className="finding-route-row">
            <span className="dot pickup-dot" />
            <span>{ride.pickupLocation}</span>
          </div>
          <div className="finding-route-row">
            <span className="dot drop-dot" />
            <span>{ride.dropLocation}</span>
          </div>
          {ride.fare != null && (
            <p className="finding-fare">Estimated fare · ₹{ride.fare}</p>
          )}
        </div>

        <div className="finding-dots" aria-hidden>
          <span /><span /><span />
        </div>

        <button type="button" className="btn btn-outline finding-cancel" onClick={onCancel}>
          Cancel request
        </button>
      </div>
    </div>
  );
}
