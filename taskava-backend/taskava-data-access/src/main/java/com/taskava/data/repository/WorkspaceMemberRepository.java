package com.taskava.data.repository;

import com.taskava.data.entity.WorkspaceMember;
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
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.user.id = :userId AND wm.deleted = false")
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.deleted = false AND wm.active = true")
    List<WorkspaceMember> findActiveByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.deleted = false AND wm.active = true")
    Page<WorkspaceMember> findActiveByWorkspaceId(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.user.id = :userId AND wm.deleted = false AND wm.active = true")
    List<WorkspaceMember> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.role = :role AND wm.deleted = false AND wm.active = true")
    List<WorkspaceMember> findByWorkspaceIdAndRole(@Param("workspaceId") UUID workspaceId, @Param("role") WorkspaceMember.WorkspaceRole role);

    @Query("SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.deleted = false AND wm.active = true")
    Long countActiveMembers(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.role = :role AND wm.deleted = false AND wm.active = true")
    Long countByWorkspaceIdAndRole(@Param("workspaceId") UUID workspaceId, @Param("role") WorkspaceMember.WorkspaceRole role);

    @Query("SELECT CASE WHEN COUNT(wm) > 0 THEN true ELSE false END FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.user.id = :userId AND wm.deleted = false AND wm.active = true")
    boolean isActiveMember(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);

    @Query("SELECT wm.role FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.user.id = :userId AND wm.deleted = false AND wm.active = true")
    Optional<WorkspaceMember.WorkspaceRole> getUserRole(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.organization.id = :organizationId AND wm.user.id = :userId AND wm.deleted = false AND wm.active = true")
    List<WorkspaceMember> findActiveByOrganizationIdAndUserId(@Param("organizationId") UUID organizationId, @Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(wm) > 0 THEN true ELSE false END FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.user.id = :userId AND wm.canCreateProjects = true AND wm.deleted = false AND wm.active = true")
    boolean canCreateProjects(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(wm) > 0 THEN true ELSE false END FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.user.id = :userId AND wm.role = 'ADMIN' AND wm.deleted = false AND wm.active = true")
    boolean isAdmin(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);
}