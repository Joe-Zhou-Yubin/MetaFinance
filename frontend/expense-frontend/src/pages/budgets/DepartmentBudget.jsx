import React, { useState, useEffect } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";

const DepartmentBudget = () => {
  const { departmentId } = useParams();
  const navigate = useNavigate();
  const [budgets, setBudgets] = useState([]);
  const [filteredBudgets, setFilteredBudgets] = useState([]);
  const [committedBudget, setCommittedBudget] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [departmentName, setDepartmentName] = useState("");
  const [searchQuery, setSearchQuery] = useState("");

  // Fetch department budgets
  useEffect(() => {
    const fetchBudgets = async () => {
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

        const response = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/budgets`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        // Filter for the specific department's budgets
        const departmentBudgets = response.data.filter(
          (budget) => budget.departmentId.toString() === departmentId
        );

        if (departmentBudgets.length > 0) {
          setDepartmentName(departmentBudgets[0].departmentName);
        }

        setBudgets(departmentBudgets);
        setFilteredBudgets(departmentBudgets);
      } catch (err) {
        console.error("Error fetching budgets:", err);
        setError("Failed to fetch budgets.");
      } finally {
        setLoading(false);
      }
    };

    fetchBudgets();
  }, [departmentId]);

  // Fetch committed budget
  useEffect(() => {
    const fetchCommittedBudget = async () => {
      try {
        const token = localStorage.getItem("jwtToken");

        const response = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/budgets/committed`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        // Find the committed budget for the specific department
        const departmentCommitment = response.data.find(
          (item) => item.departmentId.toString() === departmentId
        );

        setCommittedBudget(
          departmentCommitment ? departmentCommitment.amount : 0
        );
      } catch (err) {
        console.error("Error fetching committed budget:", err);
      }
    };

    fetchCommittedBudget();
  }, [departmentId]);

  // Format numbers with thousands separator
  const formatCurrency = (amount) => {
    if (!amount || isNaN(amount)) return "$0.00";
    return `$${new Intl.NumberFormat("en-SG", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount)}`;
  };

  // **Find latest approved and pending approval budget**
  const latestApprovedBudget = budgets
    .filter((b) => b.approved)
    .reduce(
      (latest, current) =>
        new Date(current.updatedAt) > new Date(latest.updatedAt)
          ? current
          : latest,
      budgets[0]
    );

  const latestPendingBudget = budgets
    .filter((b) => !b.approved)
    .reduce(
      (latest, current) =>
        new Date(current.updatedAt) > new Date(latest.updatedAt)
          ? current
          : latest,
      budgets[0]
    );

  // **Handle Filtering by Buttons**
  const handleFilter = (type) => {
    if (type === "approved") {
      setFilteredBudgets(
        budgets.filter((b) => b.approved && b.id === latestApprovedBudget?.id) // Exclude voided budgets
      );
    } else if (type === "pending") {
      setFilteredBudgets(
        budgets.filter((b) => !b.approved && b.id === latestPendingBudget?.id)
      );
    } else {
      setFilteredBudgets(budgets);
    }
  };
  

  // **Search Filter**
  useEffect(() => {
    let filtered = budgets;

    if (searchQuery) {
      filtered = filtered.filter((b) =>
        formatCurrency(b.amount).includes(searchQuery)
      );
    }

    setFilteredBudgets(filtered);
  }, [searchQuery, budgets]);

  return (
    <div className="container mx-auto mt-4 p-4">
      <div className="mb-4 flex justify-between items-center">
        <button
          className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
          onClick={() => navigate("/budgets")}
        >
          Back to Budgets
        </button>
      </div>

      <h1 className="text-2xl font-bold mb-6">
        Budget Overview for {departmentName}
      </h1>

      {/* Summary Cards */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-green-100 border-l-4 border-green-500 p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-bold text-green-700">Approved Budget</h2>
          <p className="text-green-800 text-xl">
            {formatCurrency(latestApprovedBudget?.amount || 0)}
          </p>
        </div>
        <div className="bg-yellow-100 border-l-4 border-yellow-500 p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-bold text-yellow-700">
            Most Recent Pending Approval
          </h2>
          <p className="text-yellow-800 text-xl">
            {formatCurrency(latestPendingBudget?.amount || 0)}
          </p>
        </div>
        <div className="bg-blue-100 border-l-4 border-blue-500 p-4 rounded-lg shadow-md">
          <h2 className="text-lg font-bold text-blue-700">Committed Budget</h2>
          <p className="text-blue-800 text-xl">
            {formatCurrency(committedBudget)}
          </p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-white shadow-lg rounded-lg p-4 mb-4 flex items-center gap-4">
        <button
          className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600"
          onClick={() => handleFilter("approved")}
        >
          Approved Budgets
        </button>
        <button
          className="bg-yellow-500 text-white px-4 py-2 rounded-lg hover:bg-yellow-600"
          onClick={() => handleFilter("pending")}
        >
          Pending Approval
        </button>
        <button
          className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-600"
          onClick={() => handleFilter("all")}
        >
          Reset
        </button>
      </div>

      {loading && <p>Loading...</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!loading && !error && filteredBudgets.length > 0 ? (
        <div className="bg-white shadow-lg rounded-lg p-6">
          <table className="min-w-full border border-gray-300">
            <thead>
              <tr className="bg-gray-200">
                <th className="border border-gray-300 px-4 py-2">ID</th>
                <th className="border border-gray-300 px-4 py-2">Amount</th>
                <th className="border border-gray-300 px-4 py-2">Status</th>
                <th className="border border-gray-300 px-4 py-2">
                  Last Updated
                </th>
              </tr>
            </thead>
            <tbody>
              {filteredBudgets.map((budget) => (
                <tr
                  key={budget.id}
                  className="text-center border-t border-gray-300"
                >
                  <td className="border border-gray-300 px-4 py-2">
                    {budget.id}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {formatCurrency(budget.amount)}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {budget.approved
                      ? budget.id === latestApprovedBudget?.id
                        ? "Approved"
                        : "Voided" // Mark older approved budgets as "Voided"
                      : budget.id === latestPendingBudget?.id
                      ? "Pending Approval"
                      : "Pending Approval"}
                  </td>

                  <td className="border border-gray-300 px-4 py-2">
                    {new Date(budget.updatedAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        !loading && (
          <p className="text-gray-600">No budgets found for this department.</p>
        )
      )}
    </div>
  );
};

export default DepartmentBudget;
