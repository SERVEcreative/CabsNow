import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import { auth } from '../api';

export default function DriverSignup() {
  const [form, setForm] = useState({ name: '', phoneNumber: '', vehicleNumber: '', aadharNumber: '', password: '' });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    try {
      const { data } = await auth.driverSignup(form);
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      localStorage.setItem('userId', data.id);
      localStorage.setItem('name', form.name);
      navigate('/driver');
    } catch (err) {
      setError(err.response?.data?.error || 'Signup failed');
    }
  };

  return (
    <AuthLayout
      title="Join as driver"
      subtitle="Earn on your schedule — accept rides near you"
      footer={<Link to="/driver/login">Back to login</Link>}
    >
      <form onSubmit={submit}>
        <label>Full name</label>
        <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <label>Phone</label>
        <input required value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
        <label>Vehicle number</label>
        <input required value={form.vehicleNumber} onChange={(e) => setForm({ ...form, vehicleNumber: e.target.value })} />
        <label>Aadhar (12 digits)</label>
        <input required minLength={12} maxLength={12} value={form.aadharNumber} onChange={(e) => setForm({ ...form, aadharNumber: e.target.value })} />
        <label>Password</label>
        <input type="password" required minLength={8} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        {error && <p className="error">{error}</p>}
        <button className="btn btn-primary" type="submit">Register &amp; Go Online</button>
      </form>
    </AuthLayout>
  );
}
