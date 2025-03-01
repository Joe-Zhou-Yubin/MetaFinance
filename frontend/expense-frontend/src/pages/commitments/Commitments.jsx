import React, { useState, useEffect } from "react";
import axios from "axios";
import { Link } from "react-router-dom";

const Commitments = () => {
  const [departments, setDepartments] = useState([]);
  const [commitments, setCommitments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Fetch departments
  const fetchDepartments = async () => {
    try {
      const token = localStorage.getItem("jwtToken");
      const response = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/departments`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setDepartments(response.data);
    } catch (err) {
      console.error("Error fetching departments:", err);
      setError("Failed to fetch departments.");
    }
  };

  // Fetch approved commitments
  const fetchCommitments = async () => {
    try {
      const token = localStorage.getItem("jwtToken");
      const response = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/commitments/approved`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setCommitments(response.data);
    } catch (err) {
      console.error("Error fetching commitments:", err);
      setError("Failed to fetch commitments.");
    }
  };

  // Fetch data on mount
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      await Promise.all([fetchDepartments(), fetchCommitments()]);
      setLoading(false);
    };
    fetchData();
  }, []);

  // Sum total commitments per department (by departmentName)
  const getTotalCommitments = (departmentName) => {
    const total = commitments
      .filter((c) => c.departmentName === departmentName)
      .reduce((sum, c) => sum + (c.amount || 0), 0);
    return formatCurrency(total);
  };

  // Format numbers with thousands separator
  const formatCurrency = (amount) => {
    if (!amount || isNaN(amount)) return "$0.00"; // Handle NaN cases
    return `$${new Intl.NumberFormat("en-SG", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount)}`;
  };

  return (
    <div className="container mx-auto mt-4 p-4">
      <h1 className="text-2xl font-bold mb-6">All Commitments</h1>

      {loading && <p>Loading...</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!loading && !error && (
        <div className="bg-white shadow-lg rounded-lg p-6">
            <div className="mb-4 flex gap-4">
  <Link
    to="/createcommitment"
    className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-700"
  >
    Create Commitment
  </Link>
</div>

          <table className="min-w-full border border-gray-300">
            <thead>
              <tr className="bg-gray-200">
                <th className="border border-gray-300 px-4 py-2">Department ID</th>
                <th className="border border-gray-300 px-4 py-2">Department Name</th>
                <th className="border border-gray-300 px-4 py-2">Department Head</th>
                <th className="border border-gray-300 px-4 py-2">Total Approved Commitments</th>
                <th className="border border-gray-300 px-4 py-2">Details</th>
              </tr>
            </thead>
            <tbody>
              {departments.map((dept) => (
                <tr key={dept.id}>
                  <td className="border border-gray-300 px-4 py-2">{dept.id}</td>
                  <td className="border border-gray-300 px-4 py-2">{dept.name}</td>
                  <td className="border border-gray-300 px-4 py-2">{dept.head ? dept.head.username : "N/A"}</td>
                  <td className="border border-gray-300 px-4 py-2">{getTotalCommitments(dept.name)}</td>
                  <td className="border border-gray-300 px-4 py-2 text-center">
                    <Link to={`/commitments/${dept.id}`} className="text-blue-500 hover:text-blue-700 text-xl">
                      ➡
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default Commitments;
