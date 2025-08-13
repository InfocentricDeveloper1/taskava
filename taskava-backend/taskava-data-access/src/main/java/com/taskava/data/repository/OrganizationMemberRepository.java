package com.taskava.data.repository;

import com.taskava.data.entity.OrganizationMember;
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
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    @Query("SELECT om FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.user.id = :userId AND om.deleted = false")
    Optional<OrganizationMember> findByOrganizationIdAndUserId(@Param("organizationId") UUID organizationId, @Param("userId") UUID userId);

    @Query("SELECT om FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.deleted = false AND om.active = true")
    List<OrganizationMember> findActiveByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT om FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.deleted = false AND om.active = true")
    Page<OrganizationMember> findActiveByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT om FROM OrganizationMember om WHERE om.user.id = :userId AND om.deleted = false AND om.active = true")
    List<OrganizationMember> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT om FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.role = :role AND om.deleted = false AND om.active = true")
    List<OrganizationMember> findByOrganizationIdAndRole(@Param("organizationId") UUID organizationId, @Param("role") OrganizationMember.OrganizationRole role);

    @Query("SELECT COUNT(om) FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.deleted = false AND om.active = true")
    Long countActiveMembers(@Param("organizationId") UUID organizationId);

    @Query("SELECT COUNT(om) FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.role = :role AND om.deleted = false AND om.active = true")
    Long countByOrganizationIdAndRole(@Param("organizationId") UUID organizationId, @Param("role") OrganizationMember.OrganizationRole role);

    @Query("SELECT CASE WHEN COUNT(om) > 0 THEN true ELSE false END FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.user.id = :userId AND om.deleted = false AND om.active = true")
    boolean isActiveMember(@Param("organizationId") UUID organizationId, @Param("userId") UUID userId);

    @Query("SELECT om.role FROM OrganizationMember om WHERE om.organization.id = :organizationId AND om.user.id = :userId AND om.deleted = false AND om.active = true")
    Optional<OrganizationMember.OrganizationRole> getUserRole(@Param("organizationId") UUID organizationId, @Param("userId") UUID userId);
}