package com.taskava.data.repository;

import com.taskava.data.entity.Team;
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
public interface TeamRepository extends JpaRepository<Team, UUID> {

    @Query("SELECT t FROM Team t WHERE t.id = :id AND t.deleted = false")
    Optional<Team> findActiveById(@Param("id") UUID id);

    @Query("SELECT t FROM Team t WHERE t.id = :id AND t.workspace.id = :workspaceId AND t.deleted = false")
    Optional<Team> findActiveByIdAndWorkspace(@Param("id") UUID id, @Param("workspaceId") UUID workspaceId);

    @Query("SELECT t FROM Team t WHERE t.workspace.id = :workspaceId AND t.deleted = false")
    List<Team> findByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("SELECT t FROM Team t WHERE t.workspace.id = :workspaceId AND t.deleted = false")
    Page<Team> findByWorkspaceId(@Param("workspaceId") UUID workspaceId, Pageable pageable);

    @Query("SELECT t FROM Team t JOIN t.members u WHERE u.id = :userId AND t.deleted = false")
    List<Team> findByMemberId(@Param("userId") UUID userId);

    @Query("SELECT t FROM Team t JOIN t.members u WHERE u.id = :userId AND t.deleted = false")
    Page<Team> findByMemberId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.workspace.id = :workspaceId AND t.name = :name AND t.deleted = false")
    Optional<Team> findByWorkspaceIdAndName(@Param("workspaceId") UUID workspaceId, @Param("name") String name);

    @Query("SELECT t FROM Team t WHERE t.teamLead.id = :userId AND t.deleted = false")
    List<Team> findByTeamLeadId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(m) FROM Team t JOIN t.members m WHERE t.id = :teamId")
    Long countMembers(@Param("teamId") UUID teamId);

    @Query("SELECT COUNT(p) FROM Team t JOIN t.projects p WHERE t.id = :teamId AND p.deleted = false")
    Long countActiveProjects(@Param("teamId") UUID teamId);

    @Query("SELECT COUNT(t) FROM Team t WHERE t.workspace.id = :workspaceId AND t.deleted = false")
    Long countByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    boolean existsByWorkspaceIdAndName(UUID workspaceId, String name);
}