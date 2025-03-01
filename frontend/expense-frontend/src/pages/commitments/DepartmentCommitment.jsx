import React, { useState, useEffect } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";

const DepartmentCommitment = () => {
  const { departmentId } = useParams(); // Get department ID from URL params
  const navigate = useNavigate();
  const [commitments, setCommitments] = useState([]);
  const [filteredCommitments, setFilteredCommitments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [departmentName, setDepartmentName] = useState(""); // Store department name
  const [filters, setFilters] = useState({
    createdStart: "",
    createdEnd: "",
    approvedStart: "",
    approvedEnd: "",
    searchQuery: "",
  });

  // Fetch department details and commitments
  useEffect(() => {
    const fetchData = async () => {
      if (!departmentId) {
        setError("Invalid department ID.");
        setLoading(false);
        return;
      }

      try {
        const token = localStorage.getItem("jwtToken");

        if (!token) {
          setError("Unauthorized: Please log in.");
          setLoading(false);
          return;
        }

        // Fetch commitments
        const response = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/commitments/department/${departmentId}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        setCommitments(response.data);
        setFilteredCommitments(response.data);

        // Fetch department name
        const departmentResponse = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/departments/${departmentId}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        setDepartmentName(departmentResponse.data.name);
      } catch (err) {
        console.error("Error fetching data:", err);
        setError("Failed to fetch data. Please check your credentials.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [departmentId]);

  // Format numbers with thousands separator
  const formatCurrency = (amount) => {
    if (!amount || isNaN(amount)) return "$0.00";
    return `$${new Intl.NumberFormat("en-SG", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount)}`;
  };

  // **Calculate total approved & unapproved commitments**
  const totalApproved = commitments
    .filter((c) => c.approved)
    .reduce((sum, c) => sum + c.amount, 0);

  const totalUnapproved = commitments
    .filter((c) => !c.approved)
    .reduce((sum, c) => sum + c.amount, 0);

  // **Filter data based on date range and description**
  useEffect(() => {
    const filtered = commitments.filter((c) => {
      const createdDate = new Date(c.createdAt);
      const approvedDate = c.approvedAt ? new Date(c.approvedAt) : null;

      return (
        (!filters.createdStart || createdDate >= new Date(filters.createdStart)) &&
        (!filters.createdEnd || createdDate <= new Date(filters.createdEnd)) &&
        (!filters.approvedStart || (approvedDate && approvedDate >= new Date(filters.approvedStart))) &&
        (!filters.approvedEnd || (approvedDate && approvedDate <= new Date(filters.approvedEnd))) &&
        (!filters.searchQuery || c.description.toLowerCase().includes(filters.searchQuery.toLowerCase()))
      );
    });

    setFilteredCommitments(filtered);
  }, [filters, commitments]);

  return (
    <div className="container mx-auto mt-4 p-4">
      <div className="mb-4 flex justify-between items-center">
        <button
          className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
          onClick={() => navigate("/commitments")}
        >
          Back to Commitments
        </button>
      </div>

      <h1 className="text-2xl font-bold mb-6">{departmentName} Commitments</h1>

      {/* Summary Card */}
      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="bg-green-100 border-l-4 border-green-500 p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-bold text-green-700">Total Approved</h2>
          <p className="text-green-800 text-xl">{formatCurrency(totalApproved)}</p>
        </div>
        <div className="bg-red-100 border-l-4 border-red-500 p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-bold text-red-700">Total Unapproved</h2>
          <p className="text-red-800 text-xl">{formatCurrency(totalUnapproved)}</p>
        </div>
      </div>

      {/* Filter Bar */}
<div className="bg-white shadow-lg rounded-lg p-4 mb-4 flex flex-wrap items-center gap-4">
  {/* Search Bar */}
  <div className="flex flex-col">
    <label className="text-gray-700 text-sm mb-1">Search Description</label>
    <input
      type="text"
      placeholder="Enter description..."
      className="px-3 py-2 border rounded-lg flex-1"
      value={filters.searchQuery}
      onChange={(e) => setFilters({ ...filters, searchQuery: e.target.value })}
    />
  </div>

  {/* Created Start Date */}
  <div className="flex flex-col">
    <label className="text-gray-700 text-sm mb-1">Created Start Date</label>
    <input
      type="date"
      className="px-3 py-2 border rounded-lg"
      value={filters.createdStart}
      onChange={(e) => setFilters({ ...filters, createdStart: e.target.value })}
    />
  </div>

  {/* Created End Date */}
  <div className="flex flex-col">
    <label className="text-gray-700 text-sm mb-1">Created End Date</label>
    <input
      type="date"
      className="px-3 py-2 border rounded-lg"
      value={filters.createdEnd}
      onChange={(e) => setFilters({ ...filters, createdEnd: e.target.value })}
    />
  </div>

  {/* Approved Start Date */}
  <div className="flex flex-col">
    <label className="text-gray-700 text-sm mb-1">Approved Start Date</label>
    <input
      type="date"
      className="px-3 py-2 border rounded-lg"
      value={filters.approvedStart}
      onChange={(e) => setFilters({ ...filters, approvedStart: e.target.value })}
    />
  </div>

  {/* Approved End Date */}
  <div className="flex flex-col">
    <label className="text-gray-700 text-sm mb-1">Approved End Date</label>
    <input
      type="date"
      className="px-3 py-2 border rounded-lg"
      value={filters.approvedEnd}
      onChange={(e) => setFilters({ ...filters, approvedEnd: e.target.value })}
    />
  </div>
</div>


      {loading && <p>Loading...</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!loading && !error && filteredCommitments.length > 0 ? (
        <div className="bg-white shadow-lg rounded-lg p-6">
          <table className="min-w-full border border-gray-300">
            <thead>
              <tr className="bg-gray-200">
                <th className="border border-gray-300 px-4 py-2">ID</th>
                <th className="border border-gray-300 px-4 py-2">Requestor</th>
                <th className="border border-gray-300 px-4 py-2">Description</th>
                <th className="border border-gray-300 px-4 py-2">Amount</th>
                <th className="border border-gray-300 px-4 py-2">Approved</th>
                <th className="border border-gray-300 px-4 py-2">Paid</th>
                <th className="border border-gray-300 px-4 py-2">Created At</th>
                <th className="border border-gray-300 px-4 py-2">Approved At</th>
              </tr>
            </thead>
            <tbody>
              {filteredCommitments.map((commitment) => (
                <tr key={commitment.id} className="text-center border-t border-gray-300">
                  <td className="border border-gray-300 px-4 py-2">{commitment.id}</td>
                  <td className="border border-gray-300 px-4 py-2">{commitment.requestorName}</td>
                  <td className="border border-gray-300 px-4 py-2">{commitment.description}</td>
                  <td className="border border-gray-300 px-4 py-2">{formatCurrency(commitment.amount)}</td>
                  <td className="border border-gray-300 px-4 py-2">{commitment.approved ? "Yes" : "No"}</td>
                  <td className="border border-gray-300 px-4 py-2">{commitment.paid ? "Yes" : "No"}</td>
                  <td className="border border-gray-300 px-4 py-2">{new Date(commitment.createdAt).toLocaleDateString()}</td>
                  <td className="border border-gray-300 px-4 py-2">{commitment.approvedAt ? new Date(commitment.approvedAt).toLocaleDateString() : "-"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        !loading && <p className="text-gray-600">No commitments found for this department.</p>
      )}
    </div>
  );
};

export default DepartmentCommitment;
