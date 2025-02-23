import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaBars, FaSearch } from "react-icons/fa";
import BurgerMenu from "./BurgerMenu";

const Navbar = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const navigate = useNavigate();

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate("/");
    };

    return (
        <nav className="bg-gray-800 text-white p-4 relative">
            <div className="container mx-auto flex items-center justify-between">
                {/* Left: Burger Menu and Logo */}
                <div className="flex items-center gap-4">
                    <button onClick={toggleMenu} className="text-white focus:outline-none">
                        <FaBars size={24} />
                    </button>
                    <Link to="/home" className="text-lg font-bold">
                        LogoPlaceholder
                    </Link>
                </div>

                {/* Center: Search Bar */}
                <div className="flex items-center border border-gray-600 rounded-lg overflow-hidden w-1/3">
                    <input
                        type="text"
                        placeholder="Search..."
                        className="w-full px-3 py-1 text-black focus:outline-none"
                    />
                    <button className="bg-gray-700 px-3 py-1">
                        <FaSearch />
                    </button>
                </div>

                {/* Right: Logout Button */}
                <button
                    onClick={handleLogout}
                    className="bg-red-500 px-4 py-2 rounded-lg hover:bg-red-600"
                >
                    Logout
                </button>
            </div>

            {/* Burger Menu Component */}
            <BurgerMenu isOpen={isMenuOpen} setIsOpen={setIsMenuOpen} />
        </nav>
    );
};

export default Navbar;
