package com.expense.claim.dto;

public class DepartmentDTO {
    private Long id;
    private String name;
    private UserDTO head;

    public DepartmentDTO(Long id, String name, UserDTO head) {
        this.id = id;
        this.name = name;
        this.head = head;
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

    public UserDTO getHead() {
        return head;
    }

    public void setHead(UserDTO head) {
        this.head = head;
    }
}
