import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const pageAccess = {
  home: { path: "/home", name: "Home", roles: ["ROLE_MEMBER", "ROLE_DIRECTOR", "ROLE_ADMIN"] },
  inbox: { path: "/inbox", name: "Inbox", roles: ["ROLE_DIRECTOR", "ROLE_ADMIN"] },
  budgets: { path: "/budgets", name: "Budgets", roles: ["ROLE_DIRECTOR", "ROLE_ADMIN"] },
  commitments: { path: "/commitments", name: "Commitments", roles: ["ROLE_DIRECTOR", "ROLE_ADMIN"] },
  expenses: { path: "/myexpenses", name: "Expenses", roles: ["ROLE_MEMBER", "ROLE_DIRECTOR", "ROLE_ADMIN"] },
  configuration: { path: "/configuration", name: "Configuration", roles: ["ROLE_ADMIN"] },
};

const Home = () => {
  const navigate = useNavigate();
  const [userRoles, setUserRoles] = useState([]);

  useEffect(() => {
    // Retrieve user details from localStorage
    const storedUser = localStorage.getItem("userDetails");
    if (storedUser) {
      const user = JSON.parse(storedUser);
      setUserRoles(user.roles || []);
    }
  }, []);

  useEffect(() => {
    // Redirect if the user is ROLE_MEMBER
    if (userRoles.includes("ROLE_MEMBER")) {
      navigate("/myexpenses");
    }
  }, [userRoles, navigate]);

  return (
    <div className="container mx-auto mt-6 p-6">
      <h2 className="text-2xl font-bold mb-4">Apps</h2>

      {/* Display available sections as cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {Object.values(pageAccess).map((item) =>
          item.roles.some((role) => userRoles.includes(role)) ? (
            <div
              key={item.path}
              className="bg-white shadow-lg p-4 rounded-lg border border-gray-300 hover:bg-gray-100 cursor-pointer"
              onClick={() => navigate(item.path)}
            >
              <h3 className="text-lg font-semibold">{item.name}</h3>
            </div>
          ) : null
        )}
      </div>
    </div>
  );
};

export default Home;
