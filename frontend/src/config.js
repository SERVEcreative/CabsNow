export function getApiBaseUrl() {
  const configured = import.meta.env.VITE_API_URL?.trim();
  if (configured) {
    return configured.replace(/\/$/, '');
  }
  if (typeof window !== 'undefined') {
    return window.location.origin;
  }
  return 'http://localhost:8080';
}
