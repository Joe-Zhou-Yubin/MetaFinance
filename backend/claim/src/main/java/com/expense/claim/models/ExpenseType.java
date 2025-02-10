package com.expense.claim.models;

import jakarta.persistence.*;

@Entity
@Table(name = "expense_types")
public class ExpenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

    public ExpenseType() {
    }

    public ExpenseType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ExpenseType{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}
