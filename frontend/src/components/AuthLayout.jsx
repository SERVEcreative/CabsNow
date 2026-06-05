export default function AuthLayout({ title, subtitle, children, footer }) {
  return (
    <div className="auth-page">
      <div className="auth-brand">
        <div className="auth-brand-inner">
          <div className="auth-logo">Cabs<span>Now</span></div>
          <h1>Rides that feel instant.</h1>
          <p>Geo-matched drivers · 5-second accept window · live sync on every trip.</p>
          <ul className="auth-features">
            <li>Nearby drivers only — no spam broadcasts</li>
            <li>Real-time updates via WebSocket</li>
            <li>Secure JWT login</li>
          </ul>
        </div>
      </div>
      <div className="auth-form-panel">
        <div className="auth-card">
          <h2>{title}</h2>
          {subtitle && <p className="auth-subtitle">{subtitle}</p>}
          {children}
          {footer && <div className="auth-footer">{footer}</div>}
        </div>
      </div>
    </div>
  );
}
