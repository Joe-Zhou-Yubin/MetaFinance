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
import MyExpenses from './pages/expenses/MyExpenses';
import ExpenseDetail from './pages/expenses/ExpenseDetail';
import Commitments from './pages/commitments/Commitments';
import CreateCommitment from './pages/commitments/CreateCommitments';
import DepartmentCommitment from './pages/commitments/DepartmentCommitment';
import Configuration from './pages/configuration/Configuration';
import ExpenseTypes from './pages/configuration/ExpenseTypes';
import Inbox from './components/Inbox';

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
                <Route
                    path="/myexpenses"
                    element={
                        <>
                            <Navbar />
                            <MyExpenses />
                        </>
                    }
                />
                <Route
                    path="/myexpenses/:id"
                    element={
                        <>
                            <Navbar />
                            <ExpenseDetail />
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
                        <Route
                            path="/configuration"
                            element={
                                <>
                                    <Navbar />
                                    <Configuration />
                                </>
                            }
                        />
                        <Route
                            path="/expensetypes"
                            element={
                                <>
                                    <Navbar />
                                    <ExpenseTypes />
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
                        path="/budgets/department/:departmentId"
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
                    <Route
                        path="/commitments"
                        element={
                            <>
                                <Navbar />
                                <Commitments />
                            </>
                        }
                    />
                    <Route
                        path="/commitments/:departmentId"
                        element={
                            <>
                                <Navbar />
                                <DepartmentCommitment />
                            </>
                        }
                    />
                    <Route
                        path="/createcommitment"
                        element={
                            <>
                                <Navbar />
                                <CreateCommitment />
                            </>
                        }
                    />
                    <Route
                        path="/inbox"
                        element={
                            <>
                                <Navbar />
                                <Inbox />
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
