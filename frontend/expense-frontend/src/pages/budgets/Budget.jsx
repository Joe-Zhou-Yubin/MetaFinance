import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';

const Budget = () => {
    const [departments, setDepartments] = useState([]);
    const [budgets, setBudgets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const navigate = useNavigate();

    // Fetch departments
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

    // Fetch latest approved budgets
    const fetchBudgets = async () => {
        try {
            const token = localStorage.getItem('jwtToken');
            const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/budgets/department`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            setBudgets(response.data);
        } catch (err) {
            console.error('Error fetching budgets:', err);
            setError('Failed to fetch budgets.');
        }
    };

    // Combine data after fetching
    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            await Promise.all([fetchDepartments(), fetchBudgets()]);
            setLoading(false);
        };

        fetchData();
    }, []);

    // Map department with their latest approved budget
    const getDepartmentBudget = (departmentId) => {
        const budget = budgets.find((b) => b.departmentId === departmentId);
        return budget || null;
    };

    // Redirect to Create Budget with validation for budget amount
    const handleCreateBudget = (departmentId) => {
        const budget = getDepartmentBudget(departmentId);
        const existingAmount = budget ? budget.amount : 0;

        // Store the department ID and existing budget in localStorage to use in CreateBudget
        localStorage.setItem('createBudgetDepartmentId', departmentId);
        localStorage.setItem('existingBudgetAmount', existingAmount);

        navigate('/createbudget');
    };

    return (
        <div className="container mx-auto mt-4 p-4">
            <div className="mb-4 flex gap-4">
                <Link to="/home" className="bg-blue-500 text-white px-4 py-2 rounded">
                    Back to Home
                </Link>
                <Link to="/createbudget" className="bg-green-500 text-white px-4 py-2 rounded">
                    Create Budget
                </Link>
            </div>

            <h1 className="text-2xl font-bold mb-6">Department Budgets</h1>

            {loading && <p>Loading...</p>}
            {error && <p className="text-red-500">{error}</p>}

            {!loading && !error && (
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <table className="min-w-full border border-gray-300">
                        <thead>
                            <tr className="bg-gray-200">
                                <th className="border border-gray-300 px-4 py-2">Department ID</th>
                                <th className="border border-gray-300 px-4 py-2">Department Name</th>
                                <th className="border border-gray-300 px-4 py-2">Department Head</th>
                                <th className="border border-gray-300 px-4 py-2">Budget Amount</th>
                                <th className="border border-gray-300 px-4 py-2">Approved</th>
                                <th className="border border-gray-300 px-4 py-2">Last Updated</th>
                                <th className="border border-gray-300 px-4 py-2">Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            {departments.map((dept) => {
                                const budget = getDepartmentBudget(dept.id);
                                return (
                                    <tr key={dept.id}>
                                        <td className="border border-gray-300 px-4 py-2">{dept.id}</td>
                                        <td className="border border-gray-300 px-4 py-2">{dept.name}</td>
                                        <td className="border border-gray-300 px-4 py-2">
                                            {dept.head ? dept.head.username : 'N/A'}
                                        </td>
                                        <td className="border border-gray-300 px-4 py-2">
                                            {budget ? `$${budget.amount.toFixed(2)}` : 'No Budget Approved yet'}
                                        </td>
                                        <td className="border border-gray-300 px-4 py-2">
                                            {budget ? (budget.approved ? 'Yes' : 'No') : '-'}
                                        </td>
                                        <td className="border border-gray-300 px-4 py-2">
                                            {budget ? new Date(budget.updatedAt).toLocaleDateString() : '-'}
                                        </td>
                                        <td className="border border-gray-300 px-4 py-2 text-center">
                                            <Link to={`/budgets/department/${dept.id}`} className="text-blue-500 hover:text-blue-700 text-xl">
                                                ➡
                                            </Link>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default Budget;
