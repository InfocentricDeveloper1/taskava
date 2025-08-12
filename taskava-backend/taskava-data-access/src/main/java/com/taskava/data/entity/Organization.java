package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "organizations", 
       indexes = {
           @Index(name = "idx_org_domain", columnList = "domain"),
           @Index(name = "idx_org_deleted", columnList = "is_deleted")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class Organization extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "domain", unique = true)
    private String domain;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType = PlanType.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_projects")
    private Integer maxProjects;

    @Column(name = "max_storage_gb")
    private Integer maxStorageGb;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Workspace> workspaces = new HashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "organization_settings", 
                    joinColumns = @JoinColumn(name = "organization_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value")
    private Map<String, String> settings = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "organization_features", 
                    joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "feature")
    @Enumerated(EnumType.STRING)
    private Set<Feature> enabledFeatures = new HashSet<>();

    public enum PlanType {
        FREE, STARTER, PROFESSIONAL, ENTERPRISE
    }

    public enum OrganizationStatus {
        ACTIVE, SUSPENDED, TRIAL, CANCELLED
    }

    public enum Feature {
        CUSTOM_FIELDS,
        AUTOMATION,
        PORTFOLIOS,
        FORMS,
        TIMELINE_VIEW,
        GANTT_VIEW,
        RESOURCE_MANAGEMENT,
        ADVANCED_SEARCH,
        API_ACCESS,
        SSO,
        AUDIT_LOG,
        DATA_EXPORT,
        CUSTOM_BRANDING,
        UNLIMITED_GUESTS
    }
}