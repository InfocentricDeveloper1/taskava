package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class Portfolio extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "color")
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // TODO: Implement many-to-many relationship with Project through PortfolioProject join table
    // @ManyToMany
    // @JoinTable(
    //     name = "portfolio_projects",
    //     joinColumns = @JoinColumn(name = "portfolio_id"),
    //     inverseJoinColumns = @JoinColumn(name = "project_id")
    // )
    // private Set<Project> projects = new HashSet<>();
}