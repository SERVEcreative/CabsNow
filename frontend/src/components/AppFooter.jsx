import { Link } from 'react-router-dom';

export default function AppFooter() {
  return (
    <footer className="app-footer">
      <div className="container footer-inner">
        <div>
          <strong className="logo">Cabs<span>Now</span></strong>
          <p className="footer-tag">Built for scale — targeted WebSocket dispatch</p>
        </div>
        <div className="footer-links">
          <Link to="/rider/signup">Ride</Link>
          <Link to="/driver/signup">Drive</Link>
          <Link to="/admin/login">Admin</Link>
        </div>
      </div>
    </footer>
  );
}
