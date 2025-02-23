import React, { useState, useEffect } from "react";
import axios from "axios";
import { Link } from "react-router-dom";

function Users() {
  const [users, setUsers] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);

  const [showConfirm, setShowConfirm] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState(null);

  const handleDeleteClick = (userId) => {
    setSelectedUserId(userId);
    setShowConfirm(true);
  };

  const confirmDelete = () => {
    handleDeleteUser(selectedUserId);
    setShowConfirm(false);
  };

  const cancelDelete = () => {
    setShowConfirm(false);
    setSelectedUserId(null);
  };

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem("userDetails"));
    setCurrentUser(user);
  }, []);

  const fetchUsers = async () => {
    try {
      const token = localStorage.getItem("jwtToken");
      const response = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/auth/users`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setUsers(response.data);
    } catch (error) {
      console.error("Error fetching users:", error);
    }
  };

  const handleDeleteUser = async (userId) => {
    try {
      const token = localStorage.getItem("jwtToken");
      await axios.delete(
        `${import.meta.env.VITE_API_BASE_URL}/auth/delete/${userId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      fetchUsers();
    } catch (error) {
      console.error("Error deleting user:", error);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const customRoleNames = {
    ROLE_MEMBER: "Member",
    ROLE_ADMIN: "Admin",
    ROLE_DIRECTOR: "Director",
  };

  return (
    <div className="container mx-auto mt-4 p-4">
      <div className="mb-4 flex gap-4">
        <Link to="/home" className="bg-blue-500 text-white px-4 py-2 rounded">
          Back to Home
        </Link>
        <Link
          to="/createuser"
          className="bg-green-500 text-white px-4 py-2 rounded"
        >
          Create User
        </Link>
      </div>
      {currentUser ? (
        <div className="bg-white shadow-lg rounded-lg p-6">
          <h2 className="text-2xl font-bold mb-4">User Dashboard</h2>
          <table className="min-w-full border border-gray-300">
            <thead>
              <tr className="bg-gray-200">
                <th className="border border-gray-300 px-4 py-2">User ID</th>
                <th className="border border-gray-300 px-4 py-2">Username</th>
                <th className="border border-gray-300 px-4 py-2">Email</th>
                <th className="border border-gray-300 px-4 py-2">Roles</th>
                <th className="border border-gray-300 px-4 py-2">Delete</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td className="border border-gray-300 px-4 py-2">
                    {user.id}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {user.username}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {user.email}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {user.roles
                      .map((role) => customRoleNames[role.name] || role.name)
                      .join(", ")}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {user.username !== currentUser.username && (
                      <button
                        className="bg-red-500 text-white px-3 py-1 rounded"
                        onClick={() => handleDeleteClick(user.id)}
                      >
                        Delete
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="text-red-500 text-center mt-4">
          You are not authenticated. Please log in to view this page.
        </div>
      )}

      {/* Confirmation Modal */}
      {showConfirm && (
        <div className="fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50">
          <div className="bg-white p-6 rounded-lg shadow-lg">
            <h3 className="text-xl font-bold mb-4">Confirm Delete</h3>
            <p>Are you sure you want to delete this user?</p>
            <div className="flex justify-end gap-4 mt-4">
              <button
                className="bg-gray-400 text-white px-4 py-2 rounded"
                onClick={cancelDelete}
              >
                Cancel
              </button>
              <button
                className="bg-red-500 text-white px-4 py-2 rounded"
                onClick={confirmDelete}
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Users;
