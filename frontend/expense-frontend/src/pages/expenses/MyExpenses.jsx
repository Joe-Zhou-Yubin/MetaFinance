import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const MyExpenses = () => {
    const [expenses, setExpenses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [newExpense, setNewExpense] = useState({ description: '' });
    const [creating, setCreating] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchExpenses = async () => {
            try {
                const token = localStorage.getItem('jwtToken');
                const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/expenses/header/own`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setExpenses(response.data);
            } catch (err) {
                console.error('Error fetching expenses:', err);
                setError('Failed to fetch expenses.');
            } finally {
                setLoading(false);
            }
        };

        fetchExpenses();
    }, []);

    const handleCreateExpense = async () => {
        if (!newExpense.description.trim()) {
            alert("Description cannot be empty.");
            return;
        }

        setCreating(true);
        try {
            const token = localStorage.getItem('jwtToken');
            const response = await axios.post(
                `${import.meta.env.VITE_API_BASE_URL}/expenses/header`,
                { description: newExpense.description },
                { headers: { Authorization: `Bearer ${token}` } }
            );

            const createdExpense = response.data;
            setShowModal(false);
            setNewExpense({ description: '' });

            // Redirect to newly created expense
            navigate(`/myexpenses/${createdExpense.id}`);
        } catch (err) {
            console.error('Error creating expense:', err);
            alert("Failed to create expense.");
        } finally {
            setCreating(false);
        }
    };

    // Categorize expenses
    const draftExpenses = expenses.filter(exp => !exp.submitted);
    const submittedExpenses = expenses.filter(exp => exp.submitted && !exp.approved);
    const approvedExpenses = expenses.filter(exp => exp.approved);

    return (
        <div className="container mx-auto mt-4 p-4">
            {/* Header with Buttons */}
            <div className="flex items-center justify-between mb-6">
                <button 
                    className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
                    onClick={() => navigate('/home')}
                >
                    Back
                </button>
                
                <h1 className="text-2xl font-bold text-center flex-1">My Expenses</h1>

                <button 
                    className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
                    onClick={() => setShowModal(true)}
                >
                    + Create Expense
                </button>
            </div>

            {loading && <p>Loading...</p>}
            {error && <p className="text-red-500">{error}</p>}

            {!loading && (
                <div className="grid grid-cols-3 gap-4">
                    {/* Draft Expenses */}
                    <div className="bg-white shadow-lg rounded-lg p-6">
                        <h2 className="text-xl font-bold mb-4">Draft</h2>
                        {draftExpenses.length === 0 ? (
                            <p>No draft expenses</p>
                        ) : (
                            <ul>
                                {draftExpenses.map((exp) => (
                                    <li 
                                        key={exp.id} 
                                        className="border-b py-2 cursor-pointer hover:bg-gray-100 p-2 rounded"
                                        onClick={() => navigate(`/myexpenses/${exp.id}`)}
                                    >
                                        <p className="font-bold">{exp.description}</p>
                                        <p className="text-gray-600">Department: {exp.departmentName}</p>
                                        <p className="text-gray-600">Total: ${exp.totalAmount.toFixed(2)}</p>
                                        <p className="text-gray-400">Created: {new Date(exp.createdAt).toLocaleDateString()}</p>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    {/* Submitted Expenses */}
                    <div className="bg-white shadow-lg rounded-lg p-6">
                        <h2 className="text-xl font-bold mb-4">Submitted</h2>
                        {submittedExpenses.length === 0 ? (
                            <p>No submitted expenses</p>
                        ) : (
                            <ul>
                                {submittedExpenses.map((exp) => (
                                    <li 
                                        key={exp.id} 
                                        className="border-b py-2 cursor-pointer hover:bg-gray-100 p-2 rounded"
                                        onClick={() => navigate(`/myexpenses/${exp.id}`)}
                                    >
                                        <p className="font-bold">{exp.description}</p>
                                        <p className="text-gray-600">Department: {exp.departmentName}</p>
                                        <p className="text-gray-600">Total: ${exp.totalAmount.toFixed(2)}</p>
                                        <p className="text-gray-400">Created: {new Date(exp.createdAt).toLocaleDateString()}</p>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    {/* Approved Expenses */}
                    <div className="bg-white shadow-lg rounded-lg p-6">
                        <h2 className="text-xl font-bold mb-4">Approved</h2>
                        {approvedExpenses.length === 0 ? (
                            <p>No approved expenses</p>
                        ) : (
                            <ul>
                                {approvedExpenses.map((exp) => (
                                    <li 
                                        key={exp.id} 
                                        className="border-b py-2 cursor-pointer hover:bg-gray-100 p-2 rounded"
                                        onClick={() => navigate(`/myexpenses/${exp.id}`)}
                                    >
                                        <p className="font-bold">{exp.description}</p>
                                        <p className="text-gray-600">Department: {exp.departmentName}</p>
                                        <p className="text-gray-600">Total: ${exp.totalAmount.toFixed(2)}</p>
                                        <p className="text-gray-400">Created: {new Date(exp.createdAt).toLocaleDateString()}</p>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                </div>
            )}

            {/* Create Expense Modal */}
            {showModal && (
                <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-96">
                        <h2 className="text-lg font-bold mb-4">Create New Expense</h2>
                        
                        {/* Expense Description Input */}
                        <input 
                            type="text" 
                            placeholder="Enter expense description" 
                            className="w-full p-2 mb-4 border rounded" 
                            value={newExpense.description}
                            onChange={(e) => setNewExpense({ ...newExpense, description: e.target.value })}
                        />

                        {/* Action Buttons */}
                        <div className="flex justify-end space-x-2">
                            <button 
                                className="bg-green-500 text-white px-4 py-2 rounded-lg"
                                onClick={handleCreateExpense}
                                disabled={creating}
                            >
                                {creating ? "Creating..." : "Create"}
                            </button>
                            <button 
                                className="bg-red-500 text-white px-4 py-2 rounded-lg"
                                onClick={() => setShowModal(false)}
                                disabled={creating}
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MyExpenses;
