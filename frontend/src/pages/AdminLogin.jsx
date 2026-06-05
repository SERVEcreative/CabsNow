import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import { auth } from '../api';

export default function AdminLogin() {
  const [email, setEmail] = useState('admin@cabsnow.com');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    try {
      const { data } = await auth.adminLogin({ email, password });
      localStorage.setItem('token', data.token);
      localStorage.setItem('role', data.role);
      navigate('/admin');
    } catch {
      setError('Invalid admin credentials');
    }
  };

  return (
    <AuthLayout
      title="Admin login"
      subtitle="Platform stats and management"
      footer={<Link to="/">← Back to home</Link>}
    >
      <form onSubmit={submit}>
        <label>Email</label>
        <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        <label>Password</label>
        <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <p className="error">{error}</p>}
        <button className="btn btn-primary" type="submit">Login</button>
      </form>
    </AuthLayout>
  );
}
