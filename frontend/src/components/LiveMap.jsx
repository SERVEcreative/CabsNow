import { useEffect, useMemo } from 'react';
import { CircleMarker, MapContainer, Polyline, Popup, TileLayer, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

function MapView({ center, zoom = 14 }) {
  const map = useMap();

  useEffect(() => {
    if (center?.[0] != null && center?.[1] != null) {
      map.flyTo(center, zoom, { duration: 0.6 });
    }
  }, [center, zoom, map]);

  return null;
}

function toLatLng(point) {
  if (!point || point.lat == null || point.lng == null) return null;
  return [point.lat, point.lng];
}

export default function LiveMap({
  height = 300,
  pickup,
  drop,
  driver,
  rider,
  follow = 'driver',
}) {
  const pickupLL = toLatLng(pickup);
  const dropLL = toLatLng(drop);
  const driverLL = toLatLng(driver);
  const riderLL = toLatLng(rider);

  const center = useMemo(() => {
    if (follow === 'driver' && driverLL) return driverLL;
    if (follow === 'rider' && riderLL) return riderLL;
    if (pickupLL) return pickupLL;
    if (driverLL) return driverLL;
    if (riderLL) return riderLL;
    return [28.6139, 77.2090];
  }, [follow, pickupLL, driverLL, riderLL, riderLL]);

  const route = useMemo(() => {
    const points = [];
    if (driverLL) points.push(driverLL);
    if (pickupLL) points.push(pickupLL);
    if (dropLL) points.push(dropLL);
    return points.length >= 2 ? points : null;
  }, [driverLL, pickupLL, dropLL]);

  return (
    <div className="live-map-wrap" style={{ height }}>
      <MapContainer center={center} zoom={14} scrollWheelZoom className="live-map">
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapView center={center} />

        {pickupLL && (
          <CircleMarker center={pickupLL} radius={10} pathOptions={{ color: '#059669', fillColor: '#10b981', fillOpacity: 0.9 }}>
            <Popup>Pickup</Popup>
          </CircleMarker>
        )}

        {dropLL && (
          <CircleMarker center={dropLL} radius={10} pathOptions={{ color: '#dc2626', fillColor: '#ef4444', fillOpacity: 0.9 }}>
            <Popup>Drop</Popup>
          </CircleMarker>
        )}

        {driverLL && (
          <CircleMarker center={driverLL} radius={12} pathOptions={{ color: '#0b5fff', fillColor: '#3b82f6', fillOpacity: 1 }}>
            <Popup>Driver (live)</Popup>
          </CircleMarker>
        )}

        {riderLL && (
          <CircleMarker center={riderLL} radius={9} pathOptions={{ color: '#7c3aed', fillColor: '#8b5cf6', fillOpacity: 0.9 }}>
            <Popup>You</Popup>
          </CircleMarker>
        )}

        {route && (
          <Polyline positions={route} pathOptions={{ color: '#0b5fff', weight: 4, opacity: 0.7, dashArray: '8 8' }} />
        )}
      </MapContainer>
    </div>
  );
}
