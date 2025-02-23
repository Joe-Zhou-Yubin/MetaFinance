import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { useNavigate, useParams } from 'react-router-dom';

const EditDepartment = () => {
    const [name, setName] = useState('');
    const [users, setUsers] = useState([]);
    const [filteredUsers, setFilteredUsers] = useState([]);
    const [selectedHead, setSelectedHead] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [dropdownVisible, setDropdownVisible] = useState(false);
    const dropdownRef = useRef(null);
    const navigate = useNavigate();
    const { id } = useParams(); // Get department ID from URL

    // ✅ Fetch department details to pre-fill form
    useEffect(() => {
        const fetchDepartment = async () => {
            try {
                const token = localStorage.getItem('jwtToken');
                const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/departments/${id}`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                const dept = response.data;
                setName(dept.name);
                setSelectedHead(dept.head?.id || '');
                setSearchTerm(dept.head?.username || '');
            } catch (err) {
                console.error('Error fetching department:', err);
            }
        };

        fetchDepartment();
    }, [id]);

    // ✅ Fetch users to populate the dropdown
    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const token = localStorage.getItem('jwtToken');
                const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/auth/users`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                setUsers(response.data);
                setFilteredUsers(response.data);
            } catch (err) {
                console.error('Error fetching users:', err);
            }
        };

        fetchUsers();
    }, []);

    // ✅ Filter users based on typing input
    const handleSearch = (e) => {
        const value = e.target.value;
        setSearchTerm(value);
        setDropdownVisible(true);

        const results = users.filter((user) =>
            user.username.toLowerCase().includes(value.toLowerCase())
        );
        setFilteredUsers(results);
    };

    // ✅ Handle clicking outside of dropdown
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setDropdownVisible(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    // ✅ Handle user selection from dropdown
    const handleUserSelect = (user) => {
        setSelectedHead(user.id); // Store user ID internally
        setSearchTerm(user.username); // Display username in input field
        setDropdownVisible(false); // Hide dropdown after selection
    };

    // ✅ Handle form submission
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        const payload = {
            name,
            head: {
                id: selectedHead
            }
        };

        try {
            const token = localStorage.getItem('jwtToken');
            const response = await axios.put(`${import.meta.env.VITE_API_BASE_URL}/departments/${id}`, payload, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (response.status === 200) {
                setSuccess('Department updated successfully!');
                setTimeout(() => navigate('/departments'), 500);
            }
        } catch (err) {
            console.error('Error updating department:', err);
            setError(err.response?.data?.message || 'Error: Unable to update department.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="bg-white shadow-lg rounded-lg p-8 w-96">
                <h1 className="text-2xl font-bold text-center mb-6">Edit Department</h1>

                {error && <div className="text-red-500 text-center mb-4">{error}</div>}
                {success && <div className="text-green-500 text-center mb-4">{success}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Department Name</label>
                        <input
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full px-4 py-2 border rounded-lg"
                            required
                        />
                    </div>

                    <div className="mb-4" ref={dropdownRef}>
                        <label className="block text-gray-700">Department Head</label>
                        <div className="relative">
                            <input
                                type="text"
                                placeholder="Search or select a user..."
                                value={searchTerm}
                                onChange={handleSearch}
                                onFocus={() => setDropdownVisible(true)}
                                className="w-full px-4 py-2 border rounded-lg"
                                required
                            />
                            {dropdownVisible && filteredUsers.length > 0 && (
                                <ul className="absolute z-10 bg-white border rounded-lg mt-1 w-full max-h-40 overflow-y-auto shadow-lg">
                                    {filteredUsers.map((user) => (
                                        <li
                                            key={user.id}
                                            onClick={() => handleUserSelect(user)}
                                            className="px-4 py-2 cursor-pointer hover:bg-gray-200"
                                        >
                                            {user.username}
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="w-full bg-purple-600 text-white py-2 rounded-lg hover:bg-purple-500"
                    >
                        Update Department
                    </button>
                </form>
            </div>
        </div>
    );
};

export default EditDepartment;
