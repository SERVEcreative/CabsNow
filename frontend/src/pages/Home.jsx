import { Link } from 'react-router-dom';

const FEATURES = [
  { icon: '📍', title: 'Geo-matched rides', desc: 'Only nearby online drivers get your request — not the whole city.' },
  { icon: '⚡', title: '5-second accept', desc: 'Drivers see a countdown popup. First accept wins, everyone else syncs instantly.' },
  { icon: '🔴', title: 'Live WebSocket sync', desc: 'Rider and driver stay in sync from booking to drop-off.' },
  { icon: '🛡️', title: 'Production-ready path', desc: 'Redis pub/sub bridge for multi-server scale when you enable REDIS_ENABLED=true.' },
];

export default function Home() {
  return (
    <>
      <div className="hero container">
        <span className="hero-badge">Delhi NCR · Live booking demo</span>
        <h1>Your ride, <em>now.</em></h1>
        <p>
          Book in seconds. Nearby drivers get a 5-second window to accept.
          Everything stays live synced — built the way Rapido scales.
        </p>
        <div className="hero-actions">
          <Link className="btn btn-primary" to="/rider/signup">Book a Ride</Link>
          <Link className="btn btn-outline" to="/driver/signup">Drive &amp; Earn</Link>
        </div>
      </div>

      <section className="features-section">
        <div className="container" style={{ textAlign: 'center' }}>
          <h2 style={{ fontSize: '1.5rem' }}>Why CabsNow feels different</h2>
          <p className="page-sub" style={{ marginTop: 8 }}>Smart dispatch, not broadcast spam</p>
        </div>
        <div className="features-grid container">
          {FEATURES.map((f) => (
            <div key={f.title} className="feature-card">
              <div className="feature-icon">{f.icon}</div>
              <h3>{f.title}</h3>
              <p>{f.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}
