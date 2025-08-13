package com.taskava.data.repository;

import com.taskava.data.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    @Query("SELECT w FROM Workspace w WHERE w.id = :id AND w.deleted = false")
    Optional<Workspace> findActiveById(@Param("id") UUID id);

    @Query("SELECT w FROM Workspace w WHERE w.organization.id = :organizationId AND w.deleted = false")
    List<Workspace> findByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT w FROM Workspace w WHERE w.organization.id = :organizationId AND w.deleted = false")
    Page<Workspace> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT w FROM Workspace w JOIN w.members u WHERE u.id = :userId AND w.deleted = false")
    List<Workspace> findByMemberId(@Param("userId") UUID userId);

    @Query("SELECT w FROM Workspace w JOIN w.members u WHERE u.id = :userId AND w.deleted = false")
    Page<Workspace> findByMemberId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT w FROM Workspace w WHERE w.organization.id = :organizationId AND w.name = :name AND w.deleted = false")
    Optional<Workspace> findByOrganizationIdAndName(@Param("organizationId") UUID organizationId, @Param("name") String name);

    @Query("SELECT COUNT(p) FROM Workspace w JOIN w.projects p WHERE w.id = :workspaceId AND p.deleted = false")
    Long countActiveProjects(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT COUNT(t) FROM Workspace w JOIN w.teams t WHERE w.id = :workspaceId AND t.deleted = false")
    Long countActiveTeams(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT COUNT(m) FROM Workspace w JOIN w.members m WHERE w.id = :workspaceId")
    Long countMembers(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT w FROM Workspace w WHERE w.visibility = :visibility AND w.deleted = false")
    Page<Workspace> findByVisibility(@Param("visibility") Workspace.Visibility visibility, Pageable pageable);

    boolean existsByOrganizationIdAndName(UUID organizationId, String name);
}