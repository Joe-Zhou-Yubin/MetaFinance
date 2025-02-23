// src/App.jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Navbar from './components/Navbar';
import Users from './pages/users/Users';
import CreateUser from './pages/users/CreateUser';
import Departments from './pages/departments/Department';
import CreateDepartment from './pages/departments/CreateDepartment';
import EditDepartment from './pages/departments/EditDepartment';
import Budget from './pages/budgets/Budget';
import DepartmentBudget from './pages/budgets/DepartmentBudget';
import CreateBudget from './pages/budgets/CreateBudget';
import ExpenseHome from './pages/expenses/ExpenseHome';

const App = () => {
    // Retrieve user from localStorage
    const user = JSON.parse(localStorage.getItem('userDetails'));

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route
                    path="/home"
                    element={
                        <>
                            <Navbar />
                            <Home />
                        </>
                    }
                />
                <Route
                    path="/expenses"
                    element={
                        <>
                            <Navbar />
                            <ExpenseHome />
                        </>
                    }
                />

                {/* Role-based route for Admin */}
                {user && user.roles && user.roles.includes('ROLE_ADMIN') && (
                    <>
                        <Route
                            path="/users"
                            element={
                                <>
                                    <Navbar />
                                    <Users />
                                </>
                            }
                        />
                        <Route
                            path="/createuser"
                            element={
                                <>
                                    <Navbar />
                                    <CreateUser />
                                </>
                            }
                        />
                    </>
                )}

                {/* Role-based route for Director and Admin */}
                {user && user.roles && (user.roles.includes('ROLE_DIRECTOR') || user.roles.includes('ROLE_ADMIN')) && (
                  <>
                    <Route
                        path="/departments"
                        element={
                            <>
                                <Navbar />
                                <Departments />
                            </>
                        }
                    />
                    <Route
                        path="/createdepartment"
                        element={
                            <>
                                <Navbar />
                                <CreateDepartment />
                            </>
                        }
                    />
                    <Route
                        path="/editdepartment/:id"
                        element={
                            <>
                                <Navbar />
                                <EditDepartment />
                            </>
                        }
                    />
                    <Route
                        path="/budgets"
                        element={
                            <>
                                <Navbar />
                                <Budget />
                            </>
                        }
                    />
                    <Route
                        path="/budgets/department/:id"
                        element={
                            <>
                                <Navbar />
                                <DepartmentBudget />
                            </>
                        }
                    />
                    <Route
                        path="/createbudget"
                        element={
                            <>
                                <Navbar />
                                <CreateBudget />
                            </>
                        }
                    />
                  </>
                )}
            </Routes>
        </BrowserRouter>
    );
};

export default App;
