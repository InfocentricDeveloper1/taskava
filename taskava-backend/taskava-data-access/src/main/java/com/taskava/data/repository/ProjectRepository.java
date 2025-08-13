package com.taskava.data.repository;

import com.taskava.data.entity.Project;
import com.taskava.data.entity.Project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    // Find active project by ID
    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.deleted = false")
    Optional<Project> findActiveById(@Param("id") UUID id);

    // Find active project by ID and workspace
    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.workspace.id = :workspaceId AND p.deleted = false")
    Optional<Project> findActiveByIdAndWorkspace(@Param("id") UUID id, @Param("workspaceId") UUID workspaceId);

    // Find all projects in a workspace
    @Query("SELECT p FROM Project p WHERE p.workspace.id = :workspaceId AND p.deleted = false")
    Page<Project> findByWorkspaceId(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    // Find projects by workspace and status
    @Query("SELECT p FROM Project p WHERE p.workspace.id = :workspaceId AND p.status = :status AND p.deleted = false")
    Page<Project> findByWorkspaceIdAndStatus(@Param("workspaceId") UUID workspaceId, 
                                             @Param("status") ProjectStatus status, 
                                             Pageable pageable);

    // Find projects by team
    @Query("SELECT p FROM Project p WHERE p.team.id = :teamId AND p.deleted = false")
    Page<Project> findByTeamId(@Param("teamId") UUID teamId, Pageable pageable);

    // Find projects where user is a member
    @Query("SELECT DISTINCT p FROM Project p " +
           "JOIN p.members m " +
           "WHERE m.id = :userId AND p.deleted = false")
    Page<Project> findByMemberId(@Param("userId") UUID userId, Pageable pageable);

    // Find projects in workspace where user is a member
    @Query("SELECT DISTINCT p FROM Project p " +
           "JOIN p.members m " +
           "WHERE p.workspace.id = :workspaceId " +
           "AND m.id = :userId " +
           "AND p.deleted = false")
    Page<Project> findByWorkspaceIdAndMemberId(@Param("workspaceId") UUID workspaceId, 
                                                @Param("userId") UUID userId, 
                                                Pageable pageable);

    // Check if project name exists in workspace
    @Query("SELECT COUNT(p) > 0 FROM Project p " +
           "WHERE p.workspace.id = :workspaceId " +
           "AND LOWER(p.name) = LOWER(:name) " +
           "AND p.deleted = false")
    boolean existsByWorkspaceIdAndName(@Param("workspaceId") UUID workspaceId, 
                                       @Param("name") String name);

    // Check if project name exists in workspace excluding a specific project
    @Query("SELECT COUNT(p) > 0 FROM Project p " +
           "WHERE p.workspace.id = :workspaceId " +
           "AND LOWER(p.name) = LOWER(:name) " +
           "AND p.id != :excludeId " +
           "AND p.deleted = false")
    boolean existsByWorkspaceIdAndNameExcludingId(@Param("workspaceId") UUID workspaceId, 
                                                   @Param("name") String name, 
                                                   @Param("excludeId") UUID excludeId);

    // Count projects in workspace
    @Query("SELECT COUNT(p) FROM Project p WHERE p.workspace.id = :workspaceId AND p.deleted = false")
    Long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    // Count active projects in workspace
    @Query("SELECT COUNT(p) FROM Project p " +
           "WHERE p.workspace.id = :workspaceId " +
           "AND p.status = 'ACTIVE' " +
           "AND p.deleted = false")
    Long countActiveByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    // Find archived projects
    @Query("SELECT p FROM Project p " +
           "WHERE p.workspace.id = :workspaceId " +
           "AND p.status = 'ARCHIVED' " +
           "AND p.deleted = false")
    Page<Project> findArchivedByWorkspaceId(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    // Search projects by name or description
    @Query("SELECT p FROM Project p " +
           "WHERE p.workspace.id = :workspaceId " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND p.deleted = false")
    Page<Project> searchByNameOrDescription(@Param("workspaceId") UUID workspaceId, 
                                           @Param("query") String query, 
                                           Pageable pageable);

    // Find projects with custom field
    @Query("SELECT DISTINCT p FROM Project p " +
           "JOIN p.customFields cf " +
           "WHERE p.workspace.id = :workspaceId " +
           "AND cf.id = :customFieldId " +
           "AND p.deleted = false")
    List<Project> findByCustomFieldId(@Param("workspaceId") UUID workspaceId, 
                                      @Param("customFieldId") UUID customFieldId);

    // Check if user is a member of project
    @Query("SELECT COUNT(p) > 0 FROM Project p " +
           "JOIN p.members m " +
           "WHERE p.id = :projectId " +
           "AND m.id = :userId " +
           "AND p.deleted = false")
    boolean isMember(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}