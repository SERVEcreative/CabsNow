import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import { auth } from '../api';

export default function RiderLogin() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    try {
      const { data } = await auth.riderLogin({ email, password });
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      localStorage.setItem('userId', data.id);
      localStorage.setItem('name', data.name);
      navigate('/rider');
    } catch {
      setError('Invalid email or password');
    }
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to book your next ride"
      footer={<>New here? <Link to="/rider/signup">Create rider account</Link></>}
    >
      <form onSubmit={submit}>
        <label>Email</label>
        <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required placeholder="you@email.com" />
        <label>Password</label>
        <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required placeholder="••••••••" />
        {error && <p className="error">{error}</p>}
        <button className="btn btn-primary" type="submit">Login &amp; Book</button>
      </form>
    </AuthLayout>
  );
}
