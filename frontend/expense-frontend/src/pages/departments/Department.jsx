import React, { useState, useEffect } from "react";
import axios from "axios";
import { Link } from "react-router-dom";

function Departments() {
    const [departments, setDepartments] = useState([]);
    const [showConfirm, setShowConfirm] = useState(false);
    const [selectedDepartmentId, setSelectedDepartmentId] = useState(null);

    // Fetch departments from API
    const fetchDepartments = async () => {
        try {
            const token = localStorage.getItem("jwtToken");
            const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/departments`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            setDepartments(response.data);
        } catch (error) {
            console.error("Error fetching departments:", error);
        }
    };

    // Handle delete button click
    const handleDeleteClick = (departmentId) => {
        setSelectedDepartmentId(departmentId);
        setShowConfirm(true);
    };

    // Confirm delete
    const confirmDelete = async () => {
        try {
            const token = localStorage.getItem("jwtToken");
            await axios.delete(
                `${import.meta.env.VITE_API_BASE_URL}/departments/${selectedDepartmentId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            setShowConfirm(false);
            setSelectedDepartmentId(null);
            fetchDepartments();
        } catch (error) {
            console.error("Error deleting department:", error);
        }
    };

    // Cancel delete
    const cancelDelete = () => {
        setShowConfirm(false);
        setSelectedDepartmentId(null);
    };

    useEffect(() => {
        fetchDepartments();
    }, []);

    return (
        <div className="container mx-auto mt-4 p-4">
            <div className="mb-4 flex gap-4">
                <Link to="/home" className="bg-blue-500 text-white px-4 py-2 rounded">
                    Back to Home
                </Link>
                <Link to="/createdepartment" className="bg-green-500 text-white px-4 py-2 rounded">
                    Create Department
                </Link>
            </div>

            <div className="bg-white shadow-lg rounded-lg p-6">
                <h2 className="text-2xl font-bold mb-4">Department Dashboard</h2>
                <table className="min-w-full border border-gray-300">
                    <thead>
                        <tr className="bg-gray-200">
                            <th className="border border-gray-300 px-4 py-2">ID</th>
                            <th className="border border-gray-300 px-4 py-2">Name</th>
                            <th className="border border-gray-300 px-4 py-2">Head</th>
                            <th className="border border-gray-300 px-4 py-2">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {departments.map((dept) => (
                            <tr key={dept.id}>
                                <td className="border border-gray-300 px-4 py-2">{dept.id}</td>
                                <td className="border border-gray-300 px-4 py-2">{dept.name}</td>
                                <td className="border border-gray-300 px-4 py-2">{dept.head?.username || 'N/A'}</td>
                                <td className="border border-gray-300 px-4 py-2 flex gap-2">
                                    <Link to={`/editdepartment/${dept.id}`} className="bg-yellow-500 text-white px-3 py-1 rounded">
                                        Edit
                                    </Link>
                                    <button
                                        className="bg-red-500 text-white px-3 py-1 rounded"
                                        onClick={() => handleDeleteClick(dept.id)}
                                    >
                                        Delete
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {showConfirm && (
                <div className="fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg">
                        <h3 className="text-xl font-bold mb-4">Confirm Delete</h3>
                        <p>Are you sure you want to delete this department?</p>
                        <div className="mt-4 flex gap-4">
                            <button
                                onClick={confirmDelete}
                                className="bg-red-500 text-white px-4 py-2 rounded"
                            >
                                Confirm
                            </button>
                            <button
                                onClick={cancelDelete}
                                className="bg-gray-500 text-white px-4 py-2 rounded"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Departments;
