import React, { useState } from "react";

const Configuration = () => {
  const [searchTerm, setSearchTerm] = useState("");

  // Dictionary of configuration endpoints with colors
  const configEndpoints = {
    users: { 
      path: "/users", 
      name: "Users", 
      description: "Manage all user accounts in the system.", 
      color: "bg-blue-500"
    },
    departments: { 
      path: "/departments", 
      name: "Departments", 
      description: "Manage departments and their details.", 
      color: "bg-green-500"
    },
    expensetypes: { 
        path: "/expensetypes", 
        name: "Expense Types", 
        description: "Manage expense types.", 
        color: "bg-yellow-500"
      },
      approvalmatrix: { 
        path: "/approvalmatrix", 
        name: "Approval Matrix", 
        description: "Maintain approvers for Budget, Expense and Commitments", 
        color: "bg-yellow-500"
      },
      adminbudget: { 
        path: "/adminbudget", 
        name: "Budget Admin", 
        description: "Manage all Budgets", 
        color: "bg-red-500"
      },
      admincommitment: { 
        path: "/admincommitment", 
        name: "Commitment Admin", 
        description: "Manage all Commitments", 
        color: "bg-red-500"
      },
      adminexpenses: { 
        path: "/adminexpenses", 
        name: "Expenses Admin", 
        description: "Manage all Expenses", 
        color: "bg-red-500"
      },
  };

  // Filter endpoints based on search term
  const filteredEndpoints = Object.values(configEndpoints).filter(endpoint =>
    endpoint.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="container mx-auto mt-6 p-6">
      <h1 className="text-2xl font-bold mb-4">Configuration</h1>

      {/* Search Bar */}
      <input
        type="text"
        placeholder="Search Configuration..."
        className="w-full px-4 py-2 border rounded-lg mb-6"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
      />

      {/* Endpoint Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredEndpoints.length > 0 ? (
          filteredEndpoints.map(({ path, name, description, color }) => (
            <a
              key={path}
              href={path}
              className={`block ${color} text-white shadow-lg p-6 rounded-lg hover:shadow-xl transition transform hover:scale-105`}
            >
              <h2 className="text-lg font-bold">{name}</h2>
              <p className="mt-2">{description}</p>
            </a>
          ))
        ) : (
          <p className="text-gray-500 col-span-full">No matching configurations found.</p>
        )}
      </div>
    </div>
  );
};

export default Configuration;
