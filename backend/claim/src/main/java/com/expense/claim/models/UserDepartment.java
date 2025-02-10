package com.expense.claim.models;

import jakarta.persistence.*;

@Entity
@Table(name = "user_department_mapping")
public class UserDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false, unique = true)
    private Department department;

    public UserDepartment() {
    }

    public UserDepartment(User user, Department department) {
        this.user = user;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "UserDepartment{" +
               "id=" + id +
               ", user=" + (user != null ? user.getUsername() : null) +
               ", department=" + (department != null ? department.getName() : null) +
               '}';
    }
}
