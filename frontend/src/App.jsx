import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate, Link } from 'react-router-dom';
import AppFooter from './components/AppFooter';
import Home from './pages/Home';
import RiderLogin from './pages/RiderLogin';
import RiderSignup from './pages/RiderSignup';
import DriverLogin from './pages/DriverLogin';
import DriverSignup from './pages/DriverSignup';
import AdminLogin from './pages/AdminLogin';

const RiderDashboard = lazy(() => import('./pages/RiderDashboard'));
const DriverDashboard = lazy(() => import('./pages/DriverDashboard'));
const AdminDashboard = lazy(() => import('./pages/AdminDashboard'));

function Loading() {
  return (
    <div className="loading-screen">
      <div className="loading-spinner" />
      <p>Loading CabsNow…</p>
    </div>
  );
}

function Nav() {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  const logout = () => {
    localStorage.clear();
    window.location.href = '/';
  };
  return (
    <nav className="navbar">
      <Link to="/" className="logo">Cabs<span>Now</span></Link>
      <div className="nav-links">
        {token && role === 'RIDER' && <Link to="/rider">My Rides</Link>}
        {token && role === 'DRIVER' && <Link to="/driver">Driver Panel</Link>}
        {token && role === 'ADMIN' && <Link to="/admin">Admin</Link>}
        {token ? (
          <button className="btn btn-outline" onClick={logout}>Logout</button>
        ) : (
          <>
            <Link to="/rider/login">Rider Login</Link>
            <Link to="/driver/login">Drive</Link>
          </>
        )}
      </div>
    </nav>
  );
}

function PrivateRoute({ children, role }) {
  const token = localStorage.getItem('token');
  const userRole = localStorage.getItem('role');
  if (!token || userRole !== role) return <Navigate to="/" replace />;
  return <Suspense fallback={<Loading />}>{children}</Suspense>;
}

export default function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <Nav />
        <Suspense fallback={<Loading />}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/rider/login" element={<RiderLogin />} />
            <Route path="/rider/signup" element={<RiderSignup />} />
            <Route path="/driver/login" element={<DriverLogin />} />
            <Route path="/driver/signup" element={<DriverSignup />} />
            <Route path="/admin/login" element={<AdminLogin />} />
            <Route path="/rider" element={<PrivateRoute role="RIDER"><RiderDashboard /></PrivateRoute>} />
            <Route path="/driver" element={<PrivateRoute role="DRIVER"><DriverDashboard /></PrivateRoute>} />
            <Route path="/admin" element={<PrivateRoute role="ADMIN"><AdminDashboard /></PrivateRoute>} />
          </Routes>
        </Suspense>
        <AppFooter />
      </div>
    </BrowserRouter>
  );
}
