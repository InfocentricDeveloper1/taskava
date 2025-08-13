package com.taskava.data.repository;

import com.taskava.data.entity.ProjectSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectSectionRepository extends JpaRepository<ProjectSection, UUID> {

    // Find section by ID and project
    @Query("SELECT s FROM ProjectSection s WHERE s.id = :id AND s.project.id = :projectId AND s.deleted = false")
    Optional<ProjectSection> findByIdAndProjectId(@Param("id") UUID id, @Param("projectId") UUID projectId);

    // Find all sections for a project ordered by position
    @Query("SELECT s FROM ProjectSection s WHERE s.project.id = :projectId AND s.deleted = false ORDER BY s.position ASC")
    List<ProjectSection> findByProjectIdOrderByPosition(@Param("projectId") UUID projectId);

    // Find section by name in project
    @Query("SELECT s FROM ProjectSection s WHERE s.project.id = :projectId AND LOWER(s.name) = LOWER(:name) AND s.deleted = false")
    Optional<ProjectSection> findByProjectIdAndName(@Param("projectId") UUID projectId, @Param("name") String name);

    // Get the maximum position in a project
    @Query("SELECT COALESCE(MAX(s.position), -1) FROM ProjectSection s WHERE s.project.id = :projectId AND s.deleted = false")
    Integer getMaxPositionByProjectId(@Param("projectId") UUID projectId);

    // Count sections in a project
    @Query("SELECT COUNT(s) FROM ProjectSection s WHERE s.project.id = :projectId AND s.deleted = false")
    Long countByProjectId(@Param("projectId") UUID projectId);

    // Update positions of sections after a specific position
    @Modifying
    @Query("UPDATE ProjectSection s SET s.position = s.position + :delta " +
           "WHERE s.project.id = :projectId " +
           "AND s.position >= :fromPosition " +
           "AND s.deleted = false")
    void updatePositionsAfter(@Param("projectId") UUID projectId, 
                             @Param("fromPosition") Integer fromPosition, 
                             @Param("delta") Integer delta);

    // Update positions of sections between two positions
    @Modifying
    @Query("UPDATE ProjectSection s SET s.position = s.position + :delta " +
           "WHERE s.project.id = :projectId " +
           "AND s.position >= :fromPosition " +
           "AND s.position <= :toPosition " +
           "AND s.deleted = false")
    void updatePositionsBetween(@Param("projectId") UUID projectId, 
                                @Param("fromPosition") Integer fromPosition,
                                @Param("toPosition") Integer toPosition,
                                @Param("delta") Integer delta);

    // Delete all sections for a project (soft delete handled by entity)
    @Modifying
    @Query("UPDATE ProjectSection s SET s.deleted = true, s.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE s.project.id = :projectId AND s.deleted = false")
    void softDeleteByProjectId(@Param("projectId") UUID projectId);

    // Check if section name exists in project
    @Query("SELECT COUNT(s) > 0 FROM ProjectSection s " +
           "WHERE s.project.id = :projectId " +
           "AND LOWER(s.name) = LOWER(:name) " +
           "AND s.deleted = false")
    boolean existsByProjectIdAndName(@Param("projectId") UUID projectId, @Param("name") String name);

    // Check if section name exists in project excluding a specific section
    @Query("SELECT COUNT(s) > 0 FROM ProjectSection s " +
           "WHERE s.project.id = :projectId " +
           "AND LOWER(s.name) = LOWER(:name) " +
           "AND s.id != :excludeId " +
           "AND s.deleted = false")
    boolean existsByProjectIdAndNameExcludingId(@Param("projectId") UUID projectId, 
                                                @Param("name") String name, 
                                                @Param("excludeId") UUID excludeId);
}