package com.taskava.data.repository;

import com.taskava.data.entity.Organization;
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
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByDomain(String domain);

    Optional<Organization> findByName(String name);

    @Query("SELECT o FROM Organization o WHERE o.id = :id AND o.deleted = false")
    Optional<Organization> findActiveById(@Param("id") UUID id);

    @Query("SELECT o FROM Organization o JOIN o.users u WHERE u.id = :userId AND o.deleted = false")
    List<Organization> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT o FROM Organization o JOIN o.users u WHERE u.email = :email AND o.deleted = false")
    List<Organization> findByUserEmail(@Param("email") String email);

    @Query("SELECT o FROM Organization o WHERE o.planType = :planType AND o.deleted = false")
    Page<Organization> findByPlanType(@Param("planType") Organization.PlanType planType, Pageable pageable);

    @Query("SELECT o FROM Organization o WHERE o.status = :status AND o.deleted = false")
    Page<Organization> findByStatus(@Param("status") Organization.OrganizationStatus status, Pageable pageable);

    @Query("SELECT COUNT(u) FROM Organization o JOIN o.users u WHERE o.id = :organizationId AND o.deleted = false")
    Long countActiveUsers(@Param("organizationId") UUID organizationId);

    @Query("SELECT COUNT(w) FROM Organization o JOIN o.workspaces w WHERE o.id = :organizationId AND w.deleted = false")
    Long countActiveWorkspaces(@Param("organizationId") UUID organizationId);

    boolean existsByDomain(String domain);

    boolean existsByName(String name);
}