package com.taskava.data.repository;

import com.taskava.data.entity.TeamMember;
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
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId AND tm.deleted = false")
    Optional<TeamMember> findByTeamIdAndUserId(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.deleted = false AND tm.active = true")
    List<TeamMember> findActiveByTeamId(@Param("teamId") UUID teamId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.deleted = false AND tm.active = true")
    Page<TeamMember> findActiveByTeamId(@Param("teamId") UUID teamId, Pageable pageable);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.user.id = :userId AND tm.deleted = false AND tm.active = true")
    List<TeamMember> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.role = :role AND tm.deleted = false AND tm.active = true")
    List<TeamMember> findByTeamIdAndRole(@Param("teamId") UUID teamId, @Param("role") TeamMember.TeamRole role);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.deleted = false AND tm.active = true")
    Long countActiveMembers(@Param("teamId") UUID teamId);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.role = :role AND tm.deleted = false AND tm.active = true")
    Long countByTeamIdAndRole(@Param("teamId") UUID teamId, @Param("role") TeamMember.TeamRole role);

    @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId AND tm.deleted = false AND tm.active = true")
    boolean isActiveMember(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    @Query("SELECT tm.role FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId AND tm.deleted = false AND tm.active = true")
    Optional<TeamMember.TeamRole> getUserRole(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.workspace.id = :workspaceId AND tm.user.id = :userId AND tm.deleted = false AND tm.active = true")
    List<TeamMember> findActiveByWorkspaceIdAndUserId(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);
}