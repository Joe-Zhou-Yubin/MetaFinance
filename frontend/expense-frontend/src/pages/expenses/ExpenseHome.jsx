import React from 'react';
import { Link } from 'react-router-dom';

const ExpenseHome = () => {
    // Get user roles from localStorage
    const user = JSON.parse(localStorage.getItem('userDetails')) || {};
    const userRoles = user.roles || [];

    // Page dictionary with role-based access
    const pageAccess = {
        myexpenses: { path: "/myexpenses", name: "My Expenses", description: "All pending and approved Expenses", roles: ["ROLE_MEMBER", "ROLE_DIRECTOR", "ROLE_ADMIN"] },
        adminexpenses: { path: "/adminexpenses", name: "Expense Processing (Admin)", description: "Admin app for processing all expenses", roles: ["ROLE_ADMIN"] },
    }; //add more pages as needed

    // Utility to check if user can access a page
    const canAccess = (roles) => userRoles.some((role) => roles.includes(role));

    // Filter accessible pages
    const accessiblePages = Object.values(pageAccess).filter((page) => canAccess(page.roles));

    return (
        <div className="container mx-auto mt-4 p-4">
            <h1 className="text-2xl font-bold mb-6 text-center">Expense Management</h1>

            {/* Grid Layout: 3 Cards Per Row */}
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {accessiblePages.map((page, index) => (
                    <Link
                        key={index}
                        to={page.path}
                        className="block p-6 bg-white border border-gray-200 rounded-lg shadow hover:bg-gray-100 transition-transform transform hover:scale-105"
                    >
                        <h2 className="text-xl font-semibold mb-2 text-gray-800">{page.name}</h2>
                        <p className="text-gray-600">{page.description}</p>
                    </Link>
                ))}
            </div>
        </div>
    );
};

export default ExpenseHome;
