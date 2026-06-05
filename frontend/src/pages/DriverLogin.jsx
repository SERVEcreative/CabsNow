import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import { auth } from '../api';

export default function DriverLogin() {
  const [phoneNumber, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    try {
      const { data } = await auth.driverLogin({ phoneNumber, password });
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      localStorage.setItem('userId', data.id);
      localStorage.setItem('name', data.name || 'Driver');
      navigate('/driver');
    } catch {
      setError('Invalid credentials');
    }
  };

  return (
    <AuthLayout
      title="Driver login"
      subtitle="Go online and accept nearby rides"
      footer={<>New driver? <Link to="/driver/signup">Register</Link></>}
    >
      <form onSubmit={submit}>
        <label>Phone number</label>
        <input required value={phoneNumber} onChange={(e) => setPhone(e.target.value)} placeholder="9876543210" />
        <label>Password</label>
        <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <p className="error">{error}</p>}
        <button className="btn btn-primary" type="submit">Go to Driver Panel</button>
      </form>
    </AuthLayout>
  );
}
