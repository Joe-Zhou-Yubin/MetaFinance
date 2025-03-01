import React, { useState, useEffect } from "react";
import axios from "axios";

function Inbox() {
  const [requests, setRequests] = useState([]);
  const [filterType, setFilterType] = useState("All");
  const [notification, setNotification] = useState(null);

  // Fetch pending requests on component mount
  useEffect(() => {
    fetchPendingRequests();
  }, []);

  const fetchPendingRequests = async () => {
    try {
      const token = localStorage.getItem("jwtToken"); // Get JWT token from local storage
      const response = await axios.get(
        "http://localhost:8080/api/approver-requests/pending",
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setRequests(response.data);
    } catch (error) {
      console.error("Error fetching approval requests:", error);
    }
  };

  const showNotification = (message, type) => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3000);
  };

  const handleApprove = async (id) => {
    try {
      const token = localStorage.getItem("jwtToken");
      await axios.put(
        `http://localhost:8080/api/approver-requests/${id}/status`,
        null,
        {
          params: { status: "APPROVED" },
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      showNotification(`Request ${id} approved successfully!`, "success");
      fetchPendingRequests(); // Refresh the list after approval
    } catch (error) {
      console.error("Error approving request:", error);
      showNotification("Failed to approve request.", "error");
    }
  };

  const handleReject = async (id) => {
    try {
      const token = localStorage.getItem("jwtToken");
      await axios.put(
        `http://localhost:8080/api/approver-requests/${id}/status`,
        null,
        {
          params: { status: "REJECTED", rejectReason: "Rejected by approver" },
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      showNotification(`Request ${id} rejected!`, "error");
      fetchPendingRequests(); // Refresh the list after rejection
    } catch (error) {
      console.error("Error rejecting request:", error);
      showNotification("Failed to reject request.", "error");
    }
  };

  const filteredRequests =
    filterType === "All"
      ? requests
      : requests.filter((req) => req.type === filterType);

  return (
    <div className="container mx-auto mt-6 p-6">
      <h2 className="text-2xl font-bold mb-4">Inbox - Pending Approvals</h2>

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

      {/* Filter Buttons */}
      <div className="mb-4 flex space-x-2">
        <button
          className={`px-4 py-2 rounded-lg ${
            filterType === "All" ? "bg-blue-600 text-white" : "bg-gray-300"
          }`}
          onClick={() => setFilterType("All")}
        >
          All
        </button>
        {[...new Set(requests.map((req) => req.type))].map((type) => (
          <button
            key={type}
            className={`px-4 py-2 rounded-lg ${
              filterType === type ? "bg-blue-600 text-white" : "bg-gray-300"
            }`}
            onClick={() => setFilterType(type)}
          >
            {type}
          </button>
        ))}
      </div>

      {/* Approval Requests Table */}
      <div className="bg-white shadow-lg rounded-lg p-6">
        {filteredRequests.length === 0 ? (
          <p className="text-gray-600">No pending approvals.</p>
        ) : (
          <table className="w-full border-collapse border border-gray-200">
            <thead>
              <tr className="bg-gray-100">
                <th className="border border-gray-300 px-4 py-2">#</th>
                <th className="border border-gray-300 px-4 py-2">Requestor</th>
                <th className="border border-gray-300 px-4 py-2">Type</th>
                <th className="border border-gray-300 px-4 py-2">Reference ID</th>
                <th className="border border-gray-300 px-4 py-2">Created At</th>
                <th className="border border-gray-300 px-4 py-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredRequests.map((request, index) => (
                <tr
                  key={request.id}
                  className="text-center border-t border-gray-300 hover:bg-gray-50"
                >
                  <td className="border border-gray-300 px-4 py-2">{index + 1}</td>
                  <td className="border border-gray-300 px-4 py-2">{request.requestor.username}</td>
                  <td className="border border-gray-300 px-4 py-2">{request.type}</td>
                  <td className="border border-gray-300 px-4 py-2">{request.referenceId}</td>
                  <td className="border border-gray-300 px-4 py-2">
                    {new Date(request.createdAt).toLocaleDateString()}
                  </td>
                  <td className="border border-gray-300 px-4 py-2 flex justify-center space-x-2">
                    <button
                      className="bg-green-500 text-white px-2 py-1 rounded hover:bg-green-700"
                      onClick={() => handleApprove(request.id)}
                    >
                      Approve
                    </button>
                    <button
                      className="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-700"
                      onClick={() => handleReject(request.id)}
                    >
                      Reject
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default Inbox;
