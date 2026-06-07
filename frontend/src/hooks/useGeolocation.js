import { useEffect, useState } from 'react';

const DEFAULT = { lat: 28.6139, lng: 77.2090 };

export function useGeolocation(enabled = true) {
  const [position, setPosition] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!enabled || !navigator.geolocation) {
      if (!navigator.geolocation) setError('Geolocation not supported');
      return undefined;
    }

    const onSuccess = (pos) => {
      setPosition({
        lat: pos.coords.latitude,
        lng: pos.coords.longitude,
      });
      setError(null);
    };

    const onError = (err) => {
      setError(err.message || 'Location permission denied');
    };

    navigator.geolocation.getCurrentPosition(onSuccess, onError, {
      enableHighAccuracy: true,
      timeout: 15000,
      maximumAge: 5000,
    });

    const watchId = navigator.geolocation.watchPosition(onSuccess, onError, {
      enableHighAccuracy: true,
      maximumAge: 5000,
      timeout: 15000,
    });

    return () => navigator.geolocation.clearWatch(watchId);
  }, [enabled]);

  return { position, error, fallback: DEFAULT };
}

export function useLocationSync(position, syncFn, intervalMs = 5000, enabled = true) {
  useEffect(() => {
    if (!enabled || !position || !syncFn) return undefined;

    const send = () => {
      syncFn({ latitude: position.lat, longitude: position.lng }).catch(() => {});
    };

    send();
    const id = setInterval(send, intervalMs);
    return () => clearInterval(id);
  }, [position?.lat, position?.lng, intervalMs, enabled, syncFn]);
}
