import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const CreateBudget = () => {
    const [amount, setAmount] = useState('');
    const [departments, setDepartments] = useState([]);
    const [selectedDepartment, setSelectedDepartment] = useState('');
    const [existingBudgetAmount, setExistingBudgetAmount] = useState(0);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    // Fetch all departments
    useEffect(() => {
        const fetchDepartments = async () => {
            try {
                const token = localStorage.getItem('jwtToken');
                const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/departments`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setDepartments(response.data);
            } catch (err) {
                console.error('Error fetching departments:', err);
                setError('Failed to fetch departments.');
            }
        };

        fetchDepartments();
    }, []);

    // Fetch latest approved budget when department is selected
    const handleDepartmentChange = async (e) => {
        const departmentId = e.target.value;
        setSelectedDepartment(departmentId);

        if (!departmentId) {
            setExistingBudgetAmount(0);
            return;
        }

        try {
            const token = localStorage.getItem('jwtToken');
            const response = await axios.get(
                `${import.meta.env.VITE_API_BASE_URL}/budgets/department/${departmentId}`,
                {
                    headers: { Authorization: `Bearer ${token}` },
                }
            );
            const latestBudget = response.data?.amount || 0;
            setExistingBudgetAmount(latestBudget);
        } catch (err) {
            console.error('Error fetching budget:', err);
            setExistingBudgetAmount(0);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (!selectedDepartment) {
            setError('Please select a department.');
            return;
        }

        const newAmount = parseFloat(amount);
        if (isNaN(newAmount) || newAmount <= 0) {
            setError('Please enter a valid budget amount.');
            return;
        }

        if (newAmount <= existingBudgetAmount) {
            setError(`Budget must be higher than the existing budget of $${existingBudgetAmount.toFixed(2)}.`);
            return;
        }

        const payload = {
            department: {
                id: selectedDepartment,
            },
            amount: newAmount,
        };

        try {
            const token = localStorage.getItem('jwtToken');
            const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/budgets`, payload, {
                headers: { Authorization: `Bearer ${token}` },
            });

            if (response.status === 200) {
                setSuccess('Budget created successfully!');
                setTimeout(() => navigate('/budgets'), 1500);
            }
        } catch (err) {
            console.error('Error creating budget:', err);
            setError(err.response?.data?.message || 'Error: Unable to create budget.');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <div className="bg-white shadow-lg rounded-lg p-8 w-96">
                <h1 className="text-2xl font-bold text-center mb-6">Create Budget</h1>

                {error && <div className="text-red-500 text-center mb-4">{error}</div>}
                {success && <div className="text-green-500 text-center mb-4">{success}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Select Department</label>
                        <select
                            value={selectedDepartment}
                            onChange={handleDepartmentChange}
                            className="w-full px-4 py-2 border rounded-lg"
                            required
                        >
                            <option value="">Select a department</option>
                            {departments.map((dept) => (
                                <option key={dept.id} value={dept.id}>
                                    {dept.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    {selectedDepartment && (
                        <div className="mb-4">
                            <label className="block text-gray-700">
                                Current Budget: ${existingBudgetAmount.toFixed(2)}
                            </label>
                        </div>
                    )}

                    <div className="mb-4">
                        <label className="block text-gray-700">Budget Amount</label>
                        <input
                            type="number"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            className="w-full px-4 py-2 border rounded-lg"
                            min="0"
                            step="0.01"
                            required
                        />
                    </div>

                    <div className="flex justify-between items-center">
                        <button
                            type="button"
                            className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
                            onClick={() => navigate('/budgets')}
                        >
                            Back
                        </button>

                        <button
                            type="submit"
                            className="bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-500"
                        >
                            Create Budget
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateBudget;
