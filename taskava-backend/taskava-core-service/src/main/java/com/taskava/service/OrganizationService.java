package com.taskava.service;

import com.taskava.common.dto.OrganizationDTO;
import com.taskava.common.dto.MembershipDTO;
import com.taskava.data.entity.Organization;
import com.taskava.data.entity.OrganizationMember;
import com.taskava.data.entity.User;
import com.taskava.data.repository.OrganizationMemberRepository;
import com.taskava.data.repository.OrganizationRepository;
import com.taskava.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public OrganizationDTO createOrganization(OrganizationDTO dto, UUID creatorUserId) {
        log.info("Creating organization: {} by user: {}", dto.getName(), creatorUserId);
        
        // Check if organization name or domain already exists
        if (organizationRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Organization with name already exists: " + dto.getName());
        }
        
        if (dto.getDomain() != null && organizationRepository.existsByDomain(dto.getDomain())) {
            throw new IllegalArgumentException("Organization with domain already exists: " + dto.getDomain());
        }
        
        // Create organization
        Organization organization = Organization.builder()
                .name(dto.getName())
                .displayName(dto.getDisplayName())
                .domain(dto.getDomain())
                .description(dto.getDescription())
                .logoUrl(dto.getLogoUrl())
                .planType(Organization.PlanType.valueOf(dto.getPlanType() != null ? dto.getPlanType() : "FREE"))
                .status(Organization.OrganizationStatus.ACTIVE)
                .maxUsers(dto.getMaxUsers())
                .maxProjects(dto.getMaxProjects())
                .maxStorageGb(dto.getMaxStorageGb())
                .build();
        
        organization = organizationRepository.save(organization);
        
        // Add creator as owner
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + creatorUserId));
        
        OrganizationMember ownerMembership = OrganizationMember.builder()
                .organization(organization)
                .user(creator)
                .role(OrganizationMember.OrganizationRole.OWNER)
                .active(true)
                .build();
        
        organizationMemberRepository.save(ownerMembership);
        
        return mapToDTO(organization);
    }

    @PreAuthorize("@organizationService.canViewOrganization(#id, authentication.principal.id)")
    @Transactional(readOnly = true)
    public OrganizationDTO getOrganization(UUID id) {
        Organization organization = organizationRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + id));
        
        OrganizationDTO dto = mapToDTO(organization);
        dto.setTotalUsers(organizationRepository.countActiveUsers(id));
        dto.setTotalWorkspaces(organizationRepository.countActiveWorkspaces(id));
        
        return dto;
    }

    @PreAuthorize("@organizationService.canManageOrganization(#id, authentication.principal.id)")
    public OrganizationDTO updateOrganization(UUID id, OrganizationDTO dto) {
        log.info("Updating organization: {}", id);
        
        Organization organization = organizationRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + id));
        
        // Update fields
        if (dto.getName() != null) {
            organization.setName(dto.getName());
        }
        if (dto.getDisplayName() != null) {
            organization.setDisplayName(dto.getDisplayName());
        }
        if (dto.getDescription() != null) {
            organization.setDescription(dto.getDescription());
        }
        if (dto.getLogoUrl() != null) {
            organization.setLogoUrl(dto.getLogoUrl());
        }
        if (dto.getDomain() != null && !dto.getDomain().equals(organization.getDomain())) {
            if (organizationRepository.existsByDomain(dto.getDomain())) {
                throw new IllegalArgumentException("Domain already in use: " + dto.getDomain());
            }
            organization.setDomain(dto.getDomain());
        }
        
        organization = organizationRepository.save(organization);
        return mapToDTO(organization);
    }

    @PreAuthorize("@organizationService.isOrganizationOwner(#id, authentication.principal.id)")
    public void deleteOrganization(UUID id, UUID deletedBy) {
        log.info("Soft deleting organization: {} by user: {}", id, deletedBy);
        
        Organization organization = organizationRepository.findActiveById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + id));
        
        organization.softDelete(deletedBy);
        organizationRepository.save(organization);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public List<OrganizationDTO> getUserOrganizations(UUID userId) {
        List<OrganizationMember> memberships = organizationMemberRepository.findActiveByUserId(userId);
        
        return memberships.stream()
                .map(membership -> {
                    OrganizationDTO dto = mapToDTO(membership.getOrganization());
                    dto.setPlanType(membership.getOrganization().getPlanType().toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @PreAuthorize("@organizationService.canManageOrganization(#organizationId, authentication.principal.id)")
    public MembershipDTO addMember(UUID organizationId, UUID userId, String role, UUID invitedBy) {
        log.info("Adding member {} to organization {} with role {}", userId, organizationId, role);
        
        // Check if user is already a member
        if (organizationMemberRepository.isActiveMember(organizationId, userId)) {
            throw new IllegalArgumentException("User is already a member of this organization");
        }
        
        Organization organization = organizationRepository.findActiveById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Check organization limits
        Long currentUsers = organizationRepository.countActiveUsers(organizationId);
        if (organization.getMaxUsers() != null && currentUsers >= organization.getMaxUsers()) {
            throw new IllegalArgumentException("Organization has reached maximum user limit");
        }
        
        OrganizationMember membership = OrganizationMember.builder()
                .organization(organization)
                .user(user)
                .role(OrganizationMember.OrganizationRole.valueOf(role))
                .invitedBy(invitedBy)
                .active(true)
                .build();
        
        membership = organizationMemberRepository.save(membership);
        
        return mapMembershipToDTO(membership);
    }

    @PreAuthorize("@organizationService.canManageOrganization(#organizationId, authentication.principal.id)")
    public void removeMember(UUID organizationId, UUID userId) {
        log.info("Removing member {} from organization {}", userId, organizationId);
        
        OrganizationMember membership = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found"));
        
        // Cannot remove the last owner
        if (membership.getRole() == OrganizationMember.OrganizationRole.OWNER) {
            Long ownerCount = organizationMemberRepository.countByOrganizationIdAndRole(
                    organizationId, OrganizationMember.OrganizationRole.OWNER);
            if (ownerCount <= 1) {
                throw new IllegalArgumentException("Cannot remove the last owner of the organization");
            }
        }
        
        membership.setActive(false);
        membership.softDelete(UUID.randomUUID()); // Should use current user ID from security context
        organizationMemberRepository.save(membership);
    }

    @PreAuthorize("@organizationService.canViewOrganization(#organizationId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public Page<MembershipDTO> getOrganizationMembers(UUID organizationId, Pageable pageable) {
        Page<OrganizationMember> members = organizationMemberRepository.findActiveByOrganizationId(organizationId, pageable);
        return members.map(this::mapMembershipToDTO);
    }

    // Helper methods for security checks
    public boolean canViewOrganization(UUID organizationId, UUID userId) {
        return organizationMemberRepository.isActiveMember(organizationId, userId);
    }

    public boolean canManageOrganization(UUID organizationId, UUID userId) {
        return organizationMemberRepository.getUserRole(organizationId, userId)
                .map(role -> role == OrganizationMember.OrganizationRole.OWNER || 
                            role == OrganizationMember.OrganizationRole.ADMIN)
                .orElse(false);
    }

    public boolean isOrganizationOwner(UUID organizationId, UUID userId) {
        return organizationMemberRepository.getUserRole(organizationId, userId)
                .map(role -> role == OrganizationMember.OrganizationRole.OWNER)
                .orElse(false);
    }

    private OrganizationDTO mapToDTO(Organization organization) {
        return OrganizationDTO.builder()
                .id(organization.getId())
                .name(organization.getName())
                .displayName(organization.getDisplayName())
                .domain(organization.getDomain())
                .description(organization.getDescription())
                .logoUrl(organization.getLogoUrl())
                .planType(organization.getPlanType().toString())
                .status(organization.getStatus().toString())
                .maxUsers(organization.getMaxUsers())
                .maxProjects(organization.getMaxProjects())
                .maxStorageGb(organization.getMaxStorageGb())
                .settings(organization.getSettings())
                .enabledFeatures(organization.getEnabledFeatures().stream()
                        .map(Enum::toString)
                        .collect(Collectors.toSet()))
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .createdBy(organization.getCreatedBy())
                .updatedBy(organization.getUpdatedBy())
                .build();
    }

    private MembershipDTO mapMembershipToDTO(OrganizationMember membership) {
        return MembershipDTO.builder()
                .id(membership.getId())
                .userId(membership.getUser().getId())
                .userEmail(membership.getUser().getEmail())
                .userName(membership.getUser().getFullName())
                .userAvatar(membership.getUser().getAvatarUrl())
                .role(membership.getRole().toString())
                .entityId(membership.getOrganization().getId())
                .entityType("ORGANIZATION")
                .entityName(membership.getOrganization().getName())
                .invitedBy(membership.getInvitedBy())
                .invitedAt(membership.getInvitedAt())
                .joinedAt(membership.getJoinedAt())
                .active(membership.isActive())
                .build();
    }
}