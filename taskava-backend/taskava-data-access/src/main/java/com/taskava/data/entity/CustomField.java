package com.taskava.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "custom_fields")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
public class CustomField extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Column(name = "description")
    private String description;

    @Column(name = "is_required")
    private boolean required = false;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options; // JSON for select/multi-select options

    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules; // JSON

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "display_order")
    private Integer displayOrder;

    public enum FieldType {
        TEXT, NUMBER, DATE, DATETIME, BOOLEAN, 
        SELECT, MULTI_SELECT, USER, CURRENCY, 
        PERCENTAGE, URL, EMAIL, PHONE
    }
}