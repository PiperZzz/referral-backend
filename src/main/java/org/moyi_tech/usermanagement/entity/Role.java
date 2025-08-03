package org.moyi_tech.usermanagement.entity;

import org.moyi_tech.usermanagement.constant.RoleName;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 50)
    private RoleName name;

    @Column
    private String description;

    // 构造函数
    public Role() {}

    public Role(RoleName name) {
        this.name = name;
        this.description = name.getDescription();
    }

    // Getters和Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoleName getName() { return name; }
    public void setName(RoleName name) { 
        this.name = name; 
        this.description = name != null ? name.getDescription() : null;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name=" + name +
                ", description='" + description + '\'' +
                '}';
    }
}