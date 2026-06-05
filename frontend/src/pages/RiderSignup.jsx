import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import { auth } from '../api';

export default function RiderSignup() {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phone: '', password: '' });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    try {
      const { data } = await auth.riderSignup(form);
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      localStorage.setItem('userId', data.id);
      localStorage.setItem('name', `${form.firstName} ${form.lastName}`.trim());
      navigate('/rider');
    } catch (err) {
      setError(err.response?.data?.error || 'Signup failed');
    }
  };

  return (
    <AuthLayout
      title="Create rider account"
      subtitle="Start booking rides in under a minute"
      footer={<>Already registered? <Link to="/rider/login">Login</Link></>}
    >
      <form onSubmit={submit}>
        <div className="grid-2">
          <div><label>First name</label><input required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} /></div>
          <div><label>Last name</label><input required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} /></div>
        </div>
        <label>Email</label>
        <input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        <label>Phone</label>
        <input required value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        <label>Password (min 8 chars)</label>
        <input type="password" required minLength={8} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        {error && <p className="error">{error}</p>}
        <button className="btn btn-primary" type="submit">Create Account</button>
      </form>
    </AuthLayout>
  );
}
