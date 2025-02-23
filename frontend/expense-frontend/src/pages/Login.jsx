import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/auth/signin`, {
                username,
                password
            });

            if (response.status === 200) {
                const { accessToken, id, username, email, roles } = response.data;

                // Save JWT token and user details to localStorage
                localStorage.setItem('jwtToken', accessToken); // Save token
                localStorage.setItem('userDetails', JSON.stringify({ id, username, email, roles })); // Save user details

                // Redirect to home page
                navigate('/home');
            }
        } catch (err) {
            setError('Invalid username or password. Please try again.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="bg-white shadow-lg rounded-lg p-8 w-96">
                <h1 className="text-2xl font-bold text-center mb-6">Login</h1>
                {error && <div className="text-red-500 text-center mb-4">{error}</div>}
                <form onSubmit={handleLogin}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Username</label>
                        <input 
                            type="text" 
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className="w-full px-4 py-2 border rounded-lg"
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Password</label>
                        <input 
                            type="password" 
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full px-4 py-2 border rounded-lg"
                            required
                        />
                    </div>
                    <button type="submit" className="w-full bg-purple-600 text-white py-2 rounded-lg hover:bg-purple-500">
                        Login
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Login;
