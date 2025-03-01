import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

const ExpenseDetail = () => {
  const { id } = useParams(); // Get expense ID from URL params
  const navigate = useNavigate();
  const [expense, setExpense] = useState(null);
  const [expenseTypes, setExpenseTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [notification, setNotification] = useState(null); // For popup notifications
  const [newLineItem, setNewLineItem] = useState({
    description: "",
    amount: "",
    expenseTypeId: "",
  });
  const [showEditModal, setShowEditModal] = useState(false);
  const [editLineItem, setEditLineItem] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    const fetchExpense = async () => {
      try {
        const token = localStorage.getItem("jwtToken");
        const response = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/expenses/header/${id}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setExpense(response.data);
      } catch (err) {
        console.error("Error fetching expense details:", err);
        setError("Failed to load expense details.");
      } finally {
        setLoading(false);
      }
    };

    const fetchExpenseTypes = async () => {
      try {
        const token = localStorage.getItem("jwtToken");
        const response = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/expense-types`, // Fetch all expense types
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setExpenseTypes(response.data);
      } catch (err) {
        console.error("Error fetching expense types:", err);
        setError("Failed to fetch expense types.");
      }
    };

    fetchExpense();
    fetchExpenseTypes();
  }, [id]);

  const handleDeleteExpense = async () => {
    try {
      const token = localStorage.getItem("jwtToken");
      const response = await axios.delete(
        `${import.meta.env.VITE_API_BASE_URL}/expenses/header/${id}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.status === 200) {
        alert("Expense deleted successfully.");
        navigate("/myexpenses"); // Redirect user after deletion
      }
    } catch (err) {
      console.error("Error deleting expense:", err);
      let errorMessage = "Failed to delete expense.";
      if (err.response && err.response.data && err.response.data.message) {
        errorMessage = err.response.data.message;
      }
      alert(errorMessage);
    }
  };

  const handleSubmit = async () => {
    try {
      const token = localStorage.getItem("jwtToken");
      const response = await axios.put(
        `${import.meta.env.VITE_API_BASE_URL}/expenses/header/submit/${id}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.status === 200) {
        alert("Expense submitted successfully.");
        navigate("/myexpenses"); // Redirect to My Expenses page
      }
    } catch (err) {
      console.error("Error submitting expense:", err);

      // Extract error message from response, if available
      let errorMessage = "Failed to submit expense.";
      if (err.response && err.response.data && err.response.data.message) {
        errorMessage = err.response.data.message; // Use backend error message
      }

      alert(errorMessage);
    }
  };

  // Function to show temporary notification
  const showNotification = (message, type) => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 5000); // Hide after 5 seconds
  };

  const handleEditClick = (item) => {
    console.log("Editing item:", item); // Debugging

    setEditLineItem({
      ...item,
      expenseTypeId: item.expenseType?.id || item.expenseTypeId || "",
    });

    setShowEditModal(true);
  };

  //delete line item modal
  const handleDeleteClick = async (headerId, lineItemId) => {
    if (!headerId || !lineItemId) {
      console.error("Invalid headerId or lineItemId:", headerId, lineItemId);
      showNotification(
        "Error: Unable to delete line item. Invalid ID.",
        "error"
      );
      return;
    }

    try {
      const token = localStorage.getItem("jwtToken");

      const response = await axios.delete(
        `${
          import.meta.env.VITE_API_BASE_URL
        }/expenses/line/${headerId}/${lineItemId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.status === 200) {
        setExpense((prev) => ({
          ...prev,
          lineItems: prev.lineItems.filter((item) => item.id !== lineItemId),
          totalAmount:
            prev.totalAmount -
              prev.lineItems.find((item) => item.id === lineItemId)?.amount ||
            0,
        }));

        showNotification("Line item deleted successfully.", "success");
      }
    } catch (err) {
      console.error("Error deleting line item:", err);

      let errorMessage = "Failed to delete line item.";
      if (err.response && err.response.data && err.response.data.message) {
        errorMessage = err.response.data.message;
      }

      showNotification(errorMessage, "error");
    }
  };

  //save edit method
  // **Save Edited Line Item**
  const handleSaveEdit = async () => {
    console.log("handleSaveEdit triggered");

    if (!editLineItem || !editLineItem.description || !editLineItem.amount) {
      console.log("Validation failed:", editLineItem);
      showNotification("All fields are required.", "error");
      return;
    }

    try {
      const token = localStorage.getItem("jwtToken");

      const payload = {
        description: editLineItem.description,
        amount: parseFloat(editLineItem.amount),
      };

      console.log("Sending payload:", payload);
      console.log(
        `PUT request to: ${
          import.meta.env.VITE_API_BASE_URL
        }/expenses/line/${id}/${editLineItem.id}`
      );

      await axios.put(
        `${import.meta.env.VITE_API_BASE_URL}/expenses/line/${id}/${
          editLineItem.id
        }`,
        payload,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setTimeout(() => {
        window.location.reload();
      }, 500);

      // Update UI state
      setExpense((prev) => ({
        ...prev,
        lineItems: prev.lineItems.map((item) =>
          item.id === editLineItem.id
            ? {
                ...item,
                ...payload,
              }
            : item
        ),
      }));

      console.log("UI state updated successfully");

      showNotification("Line item updated successfully!", "success");
      setShowEditModal(false);
    } catch (err) {
      console.error("Error updating line item:", err);
      showNotification("Failed to update line item.", "error");
    }
  };

  const handleAddLineItem = async () => {
    // Validation: Ensure all fields are filled
    if (
      !newLineItem.description ||
      !newLineItem.amount ||
      !newLineItem.expenseTypeId
    ) {
      showNotification("All fields are required.", "error");
      return;
    }

    try {
      const token = localStorage.getItem("jwtToken");
      const payload = [
        {
          description: newLineItem.description,
          amount: parseFloat(newLineItem.amount),
          expenseType: { id: newLineItem.expenseTypeId },
        },
      ];

      await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/expenses/line/${id}`,
        payload,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setExpense((prev) => ({
        ...prev,
        lineItems: [
          ...prev.lineItems,
          {
            id: prev.lineItems.length + 1, // Temporary ID
            description: newLineItem.description,
            amount: parseFloat(newLineItem.amount),
            expenseTypeName: expenseTypes.find(
              (type) => type.id === parseInt(newLineItem.expenseTypeId)
            )?.name,
            createdAt: new Date().toISOString(),
          },
        ],
        totalAmount: prev.totalAmount + parseFloat(newLineItem.amount),
      }));

      showNotification("Line item added successfully.", "success");
      setShowModal(false);
      setNewLineItem({ description: "", amount: "", expenseTypeId: "" });
    } catch (err) {
      showNotification("Failed to add line item.", "error");
    }
  };

  if (loading) return <p className="text-center">Loading...</p>;

  return (
    <div className="container mx-auto mt-6 p-6">
      {/* Notification Popup */}
      {notification && (
        <div
          className={`fixed top-4 left-1/2 transform -translate-x-1/2 px-4 py-2 rounded shadow-lg text-white ${
            notification.type === "success" ? "bg-green-500" : "bg-red-500"
          }`}
        >
          {notification.message}
        </div>
      )}
      {/* Back & Action Buttons */}
      <div className="flex justify-between items-center mb-4">
        {/* Back Button */}
        <button
          className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
          onClick={() => navigate("/myexpenses")}
        >
          Back
        </button>

        {/* Grouped Delete & Submit Buttons */}
        {!expense.submitted && (
          <div className="flex space-x-2">
            <button
              className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-700"
              onClick={() => setShowDeleteModal(true)}
            >
              Delete Expense
            </button>

            <button
              className="bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-700"
              onClick={handleSubmit}
            >
              Submit Expense
            </button>
          </div>
        )}
      </div>

      {/* Expense Header Card */}
      <div className="bg-white shadow-lg rounded-lg p-6 mb-6 max-w-3xl mx-auto border border-gray-300">
        <h2 className="text-xl font-bold mb-4">Expense Details</h2>
        <p>
          <strong>Report ID:</strong> {expense.id}
        </p>
        <p>
          <strong>Department:</strong> {expense.departmentName}
        </p>
        <p>
          <strong>Description:</strong> {expense.description}
        </p>
        <p>
          <strong>Total Amount:</strong> ${expense.totalAmount.toFixed(2)}
        </p>
        <p>
          <strong>Created On:</strong>{" "}
          {new Date(expense.createdAt).toLocaleDateString()}
        </p>
      </div>

      {/* Expense Line Items Table */}
      <div className="bg-white shadow-lg rounded-lg p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold">Expense Line Items</h2>
          <button
            className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
            onClick={() => setShowModal(true)}
          >
            + Add Line Item
          </button>
        </div>

        {expense.lineItems.length === 0 ? (
          <p className="text-gray-600">No line items available.</p>
        ) : (
          <table className="w-full border-collapse border border-gray-200">
            <thead>
              <tr className="bg-gray-100">
                <th className="border border-gray-300 px-4 py-2">#</th>
                <th className="border border-gray-300 px-4 py-2">
                  Expense Type
                </th>
                <th className="border border-gray-300 px-4 py-2">
                  Description
                </th>
                <th className="border border-gray-300 px-4 py-2">Amount</th>
                <th className="border border-gray-300 px-4 py-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {expense.lineItems.map((item, index) => (
                <tr
                  key={index}
                  className="text-center border-t border-gray-300 hover:bg-gray-50"
                >
                  <td className="border border-gray-300 px-4 py-2">
                    {index + 1}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {item.expenseTypeName}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {item.description}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    ${item.amount.toFixed(2)}
                  </td>
                  <td className="border border-gray-300 px-4 py-2 flex justify-center space-x-2">
                    <button
                      className="bg-yellow-500 text-white px-2 py-1 rounded hover:bg-yellow-700"
                      onClick={() => handleEditClick(item)}
                    >
                      Edit
                    </button>
                    <button
                      className="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-700"
                      onClick={() => handleDeleteClick(expense.id, item.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal for Adding Line Item */}
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96">
            <h2 className="text-lg font-bold mb-4">Add Line Item</h2>

            {/* Description Input */}
            <input
              type="text"
              placeholder="Description"
              className="w-full p-2 mb-2 border rounded"
              value={newLineItem.description}
              onChange={(e) =>
                setNewLineItem({ ...newLineItem, description: e.target.value })
              }
            />

            {/* Amount Input */}
            <input
              type="number"
              placeholder="Amount"
              className="w-full p-2 mb-2 border rounded"
              value={newLineItem.amount}
              onChange={(e) =>
                setNewLineItem({ ...newLineItem, amount: e.target.value })
              }
            />

            {/* Expense Type Selector */}
            <select
              className="w-full p-2 mb-2 border rounded"
              value={newLineItem.expenseTypeId}
              onChange={(e) =>
                setNewLineItem({
                  ...newLineItem,
                  expenseTypeId: e.target.value,
                })
              }
            >
              <option value="">Select Expense Type</option>
              {expenseTypes.length > 0 ? (
                expenseTypes.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.name}
                  </option>
                ))
              ) : (
                <option disabled>Loading expense types...</option>
              )}
            </select>

            {/* Action Buttons */}
            <div className="flex justify-end space-x-2 mt-4">
              <button
                className="bg-green-500 text-white px-4 py-2 rounded-lg"
                onClick={handleAddLineItem}
              >
                Add
              </button>
              <button
                className="bg-red-500 text-white px-4 py-2 rounded-lg"
                onClick={() => setShowModal(false)}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
      {showEditModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96">
            <h2 className="text-lg font-bold mb-4">Edit Line Item</h2>

            {/* Description Input */}
            <input
              type="text"
              placeholder="Description"
              className="w-full p-2 mb-2 border rounded"
              value={editLineItem.description}
              onChange={(e) =>
                setEditLineItem({
                  ...editLineItem,
                  description: e.target.value,
                })
              }
            />

            {/* Amount Input */}
            <input
              type="number"
              placeholder="Amount"
              className="w-full p-2 mb-2 border rounded"
              value={editLineItem.amount}
              onChange={(e) =>
                setEditLineItem({ ...editLineItem, amount: e.target.value })
              }
            />

            {/* Expense Type Selector */}
            {/* <select
              className="w-full p-2 mb-2 border rounded"
              value={editLineItem.expenseTypeId || ""} // Ensure it selects the correct id
              onChange={(e) =>
                setEditLineItem({
                  ...editLineItem,
                  expenseTypeId: e.target.value,
                })
              }
            >
              <option value="">Select Expense Type</option>
              {expenseTypes.length > 0 ? (
                expenseTypes.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.name}
                  </option>
                ))
              ) : (
                <option disabled>Loading expense types...</option>
              )}
            </select> */}

            {/* Action Buttons */}
            <div className="flex justify-end space-x-2 mt-4">
              <button
                className="bg-green-500 text-white px-4 py-2 rounded-lg"
                onClick={() => {
                  console.log("Save button clicked"); // Debugging log
                  handleSaveEdit();
                }}
              >
                Save
              </button>

              <button
                className="bg-red-500 text-white px-4 py-2 rounded-lg"
                onClick={() => setShowEditModal(false)}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {showDeleteModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96">
            <h2 className="text-lg font-bold mb-4 text-center text-red-600">
              Confirm Deletion
            </h2>
            <p className="text-center text-gray-700">
              Are you sure you want to delete this expense? This action cannot
              be undone.
            </p>

            {/* Action Buttons */}
            <div className="flex justify-center space-x-4 mt-6">
              <button
                className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-700"
                onClick={handleDeleteExpense}
              >
                Yes, Delete
              </button>
              <button
                className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-700"
                onClick={() => setShowDeleteModal(false)}
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

export default ExpenseDetail;
