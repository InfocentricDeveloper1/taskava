package com.taskava.data.repository;

import com.taskava.data.entity.CustomField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomFieldRepository extends JpaRepository<CustomField, UUID> {

    @Query("SELECT cf FROM CustomField cf WHERE cf.workspace.id = :workspaceId AND cf.deleted = false ORDER BY cf.displayOrder")
    List<CustomField> findByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT cf FROM CustomField cf WHERE cf.workspace.id = :workspaceId AND cf.fieldType = :fieldType AND cf.deleted = false ORDER BY cf.displayOrder")
    List<CustomField> findByWorkspaceIdAndFieldType(@Param("workspaceId") UUID workspaceId, 
                                                    @Param("fieldType") CustomField.FieldType fieldType);

    @Query("SELECT cf FROM CustomField cf WHERE cf.id = :id AND cf.workspace.id = :workspaceId AND cf.deleted = false")
    Optional<CustomField> findByIdAndWorkspaceId(@Param("id") UUID id, @Param("workspaceId") UUID workspaceId);

    @Query("SELECT cf FROM CustomField cf WHERE cf.workspace.id = :workspaceId AND cf.fieldKey = :fieldKey AND cf.deleted = false")
    Optional<CustomField> findByWorkspaceIdAndFieldKey(@Param("workspaceId") UUID workspaceId, 
                                                       @Param("fieldKey") String fieldKey);

    @Query("SELECT COUNT(cf) > 0 FROM CustomField cf WHERE cf.workspace.id = :workspaceId AND cf.fieldKey = :fieldKey AND cf.deleted = false")
    boolean existsByWorkspaceIdAndFieldKey(@Param("workspaceId") UUID workspaceId, 
                                          @Param("fieldKey") String fieldKey);

    @Query("SELECT COALESCE(MAX(cf.displayOrder), 0) FROM CustomField cf WHERE cf.workspace.id = :workspaceId")
    Integer getMaxDisplayOrder(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT COUNT(cf) FROM CustomField cf WHERE cf.workspace.id = :workspaceId AND cf.deleted = false")
    Long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("UPDATE CustomField cf SET cf.displayOrder = cf.displayOrder + 1 WHERE cf.workspace.id = :workspaceId AND cf.displayOrder >= :startOrder AND cf.deleted = false")
    void incrementDisplayOrdersFrom(@Param("workspaceId") UUID workspaceId, @Param("startOrder") Integer startOrder);

    @Query("UPDATE CustomField cf SET cf.displayOrder = cf.displayOrder - 1 WHERE cf.workspace.id = :workspaceId AND cf.displayOrder > :startOrder AND cf.deleted = false")
    void decrementDisplayOrdersAfter(@Param("workspaceId") UUID workspaceId, @Param("startOrder") Integer startOrder);
}