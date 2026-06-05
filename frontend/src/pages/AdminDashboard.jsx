import { useEffect, useState } from 'react';
import { admin } from '../api';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    admin.stats().then(({ data }) => setStats(data)).catch(() => setStats(null));
  }, []);

  if (!stats) return <div className="container"><p>Loading stats...</p></div>;

  return (
    <div className="container">
      <h2>Admin Dashboard</h2>
      <div className="card stats-grid">
        <div className="stat-card"><h3>{stats.totalUsers}</h3><p>Users</p></div>
        <div className="stat-card"><h3>{stats.totalDrivers}</h3><p>Drivers</p></div>
        <div className="stat-card"><h3>{stats.totalDuties}</h3><p>Total Rides</p></div>
        <div className="stat-card"><h3>{stats.completedDuties}</h3><p>Completed</p></div>
        <div className="stat-card"><h3>₹{stats.totalRevenue}</h3><p>Revenue</p></div>
        <div className="stat-card"><h3>{stats.pendingPayments}</h3><p>Pending Payments</p></div>
      </div>
    </div>
  );
}
