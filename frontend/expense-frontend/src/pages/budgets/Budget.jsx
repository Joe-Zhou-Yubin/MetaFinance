import React, { useState, useEffect } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";

const Budget = () => {
  const [departments, setDepartments] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [committedBudgets, setCommittedBudgets] = useState([]);

  const navigate = useNavigate();

  // Fetch departments
  const fetchDepartments = async () => {
    try {
      const token = localStorage.getItem("jwtToken");
      const response = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/departments`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setDepartments(response.data);
    } catch (err) {
      console.error("Error fetching departments:", err);
      setError("Failed to fetch departments.");
    }
  };

  const fetchCommittedBudgets = async () => {
    try {
      const token = localStorage.getItem("jwtToken");
      const response = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/budgets/committed`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setCommittedBudgets(response.data);
    } catch (err) {
      console.error("Error fetching committed budgets:", err);
      setError("Failed to fetch committed budgets.");
    }
  };

  // Fetch latest approved budgets
  const fetchBudgets = async () => {
    try {
      const token = localStorage.getItem("jwtToken");
      const response = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/budgets/department`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setBudgets(response.data);
    } catch (err) {
      console.error("Error fetching budgets:", err);
      setError("Failed to fetch budgets.");
    }
  };

  // Combine data after fetching
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      await Promise.all([
        fetchDepartments(),
        fetchBudgets(),
        fetchCommittedBudgets(),
      ]);
      setLoading(false);
    };

    fetchData();
  }, []);

  const formatCurrency = (amount) => {
    if (!amount || isNaN(amount)) return "$0.00"; // Handle NaN cases
    return `$${new Intl.NumberFormat("en-SG", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount)}`;
  };
  

  // Map department with their latest approved budget
  const getDepartmentBudget = (departmentId) => {
    const budget = budgets.find((b) => b.departmentId === departmentId);
    return budget ? formatCurrency(budget.amount) : "0.00";
  };

  const getCommittedBudget = (departmentId) => {
    const budget = committedBudgets.find((b) => b.departmentId === departmentId);
    return budget ? formatCurrency(budget.amount) : "0.00";
  };

  return (
    <div className="container mx-auto mt-4 p-4">
      <div className="mb-4 flex gap-4">
        <Link to="/home" className="bg-blue-500 text-white px-4 py-2 rounded">
          Back to Home
        </Link>
        <Link
          to="/createbudget"
          className="bg-green-500 text-white px-4 py-2 rounded"
        >
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
                <th className="border border-gray-300 px-4 py-2">
                  Department ID
                </th>
                <th className="border border-gray-300 px-4 py-2">
                  Department Name
                </th>
                <th className="border border-gray-300 px-4 py-2">
                  Department Head
                </th>
                <th className="border border-gray-300 px-4 py-2">
                  Budgeted Amount
                </th>
                <th className="border border-gray-300 px-4 py-2">
                  Committed Budget
                </th>
                <th className="border border-gray-300 px-4 py-2">Approved</th>
                <th className="border border-gray-300 px-4 py-2">
                  Last Updated
                </th>
                <th className="border border-gray-300 px-4 py-2">Details</th>
              </tr>
            </thead>
            <tbody>
              {departments.map((dept) => {
                return (
                  <tr key={dept.id}>
                    <td className="border border-gray-300 px-4 py-2">
                      {dept.id}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">
                      {dept.name}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">
                      {dept.head ? dept.head.username : "N/A"}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">
                      {getDepartmentBudget(dept.id)}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">
                      {getCommittedBudget(dept.id)}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">
                      {budgets.find((b) => b.departmentId === dept.id)
                        ? budgets.find((b) => b.departmentId === dept.id)
                            .approved
                          ? "Yes"
                          : "No"
                        : "-"}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">
                      {budgets.find((b) => b.departmentId === dept.id)
                        ? new Date(
                            budgets.find((b) => b.departmentId === dept.id)
                              .updatedAt
                          ).toLocaleDateString()
                        : "-"}
                    </td>
                    <td className="border border-gray-300 px-4 py-2 text-center">
                      <Link
                        to={`/budgets/department/${dept.id}`}
                        className="text-blue-500 hover:text-blue-700 text-xl"
                      >
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
