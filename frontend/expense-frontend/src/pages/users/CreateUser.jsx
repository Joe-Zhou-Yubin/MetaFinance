import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const CreateUser = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('member');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleCreateUser = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/auth/signup`, {
                username,
                email,
                password,
                role: [role]
            });

            if (response.status === 200) {
                navigate('/users');
            }
        } catch (err) {
            setError('Failed to create user. Please try again.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="bg-white shadow-lg rounded-lg p-8 w-96">
                <h1 className="text-2xl font-bold text-center mb-6">Create User</h1>
                {error && <div className="text-red-500 text-center mb-4">{error}</div>}
                <form onSubmit={handleCreateUser}>
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
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
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
                    <div className="mb-4">
                        <label className="block text-gray-700">Role</label>
                        <select
                            value={role}
                            onChange={(e) => setRole(e.target.value)}
                            className="w-full px-4 py-2 border rounded-lg"
                            required
                        >
                            <option value="member">Member</option>
                            <option value="director">Director</option>
                            <option value="admin">Admin</option>
                        </select>
                    </div>
                    <button type="submit" className="w-full bg-purple-600 text-white py-2 rounded-lg hover:bg-purple-500">
                        Create User
                    </button>
                </form>
            </div>
        </div>
    );
};

export default CreateUser;
