import React, { useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const BurgerMenu = ({ isOpen, setIsOpen }) => {
    const navigate = useNavigate();
    const menuRef = useRef(null);

    // Retrieve user roles from localStorage
    const user = JSON.parse(localStorage.getItem("userDetails")) || {};
    const userRoles = user.roles || [];

    // Page dictionary with access control
    const pageAccess = {
        home: { path: "/home", name: "Home", roles: ["ROLE_MEMBER", "ROLE_DIRECTOR", "ROLE_ADMIN"] },
        inbox: { path: "/inbox", name: "Inbox", roles: ["ROLE_DIRECTOR", "ROLE_ADMIN"] },
        budgets: { path: "/budgets", name: "Budgets", roles: ["ROLE_DIRECTOR", "ROLE_ADMIN"] },
        commitments: { path: "/commitments", name: "Commitments", roles: ["ROLE_DIRECTOR", "ROLE_ADMIN"] },
        expenses: { path: "/myexpenses", name: "Expenses", roles: ["ROLE_MEMBER", "ROLE_DIRECTOR", "ROLE_ADMIN"] },
        configuration: { path: "/configuration", name: "Configuration", roles: ["ROLE_ADMIN"] },
    };

    // Check if user has access to the page
    const canAccess = (allowedRoles) => {
        return userRoles.some(role => allowedRoles.includes(role));
    };

    // Handle Logout
    const handleLogout = () => {
        localStorage.clear();
        navigate("/");
    };

    // Close menu when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener("mousedown", handleClickOutside);
        }

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [isOpen, setIsOpen]);

    return (
        <div
            className={`fixed inset-0 z-50 transition-all ${
                isOpen ? "visible opacity-100" : "invisible opacity-0"
            }`}
        >
            {/* Transparent background that closes the menu on click */}
            <div
                className="absolute inset-0 bg-black bg-opacity-50"
                onClick={() => setIsOpen(false)}
            ></div>

            {/* Sidebar menu */}
            <div
                ref={menuRef}
                className={`w-64 bg-white h-full p-6 shadow-lg overflow-y-auto transform transition-transform ${
                    isOpen ? "translate-x-0" : "-translate-x-full"
                }`}
            >
                {/* Close Button */}
                <button onClick={() => setIsOpen(false)} className="text-black text-2xl mb-4">
                    ✖
                </button>

                {/* Navigation Links */}
                <nav className="space-y-4">
                    {Object.values(pageAccess).map(({ path, name, roles }) =>
                        canAccess(roles) ? (
                            <a
                                key={path}
                                href={path}
                                className="block text-gray-700 hover:text-purple-500"
                                onClick={() => setIsOpen(false)}
                            >
                                {name}
                            </a>
                        ) : null
                    )}

                    {/* Logout Button */}
                    <button
                        onClick={handleLogout}
                        className="block text-gray-700 hover:text-red-500 w-full text-left"
                    >
                        Logout
                    </button>
                </nav>
            </div>
        </div>
    );
};

export default BurgerMenu;
