package com.taskava.data.repository;

import com.taskava.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndOrganizationId(String email, UUID organizationId);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUsernameAndOrganizationId(String username, UUID organizationId);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsernameAndOrganizationId(String username, UUID organizationId);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") UUID userId, @Param("loginTime") Instant loginTime);
    
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerifiedAt = :verifiedAt WHERE u.id = :userId")
    void verifyEmail(@Param("userId") UUID userId, @Param("verifiedAt") Instant verifiedAt);
}