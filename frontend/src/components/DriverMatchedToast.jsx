import { useEffect, useState } from 'react';

export default function DriverMatchedToast({ driverName, onDone }) {
  useEffect(() => {
    const id = setTimeout(onDone, 2800);
    return () => clearTimeout(id);
  }, [onDone]);

  return (
    <div className="matched-toast-overlay">
      <div className="matched-toast">
        <div className="matched-check">✓</div>
        <h3>Driver found!</h3>
        <p>{driverName} is on the way</p>
      </div>
    </div>
  );
}

export function useMatchedToast(liveEvent) {
  const [show, setShow] = useState(false);
  const [lastShownDuty, setLastShownDuty] = useState(null);

  useEffect(() => {
    if (
      liveEvent?.status === 'ACCEPTED'
      && liveEvent?.driverName
      && liveEvent.dutyId !== lastShownDuty
    ) {
      setShow(true);
      setLastShownDuty(liveEvent.dutyId);
    }
  }, [liveEvent, lastShownDuty]);

  return { show, dismiss: () => setShow(false) };
}
