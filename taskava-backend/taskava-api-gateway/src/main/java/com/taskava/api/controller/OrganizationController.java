package com.taskava.api.controller;

import com.taskava.common.dto.OrganizationDTO;
import com.taskava.common.dto.MembershipDTO;
import com.taskava.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "Create a new organization")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrganizationDTO> createOrganization(
            @Valid @RequestBody OrganizationDTO organizationDTO,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        OrganizationDTO created = organizationService.createOrganization(organizationDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    @PreAuthorize("@securityExpressionService.hasOrganizationAccess(#id, authentication)")
    public ResponseEntity<OrganizationDTO> getOrganization(@PathVariable UUID id) {
        OrganizationDTO organization = organizationService.getOrganization(id);
        return ResponseEntity.ok(organization);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization")
    @PreAuthorize("@securityExpressionService.isOrganizationAdmin(#id, authentication)")
    public ResponseEntity<OrganizationDTO> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationDTO organizationDTO) {
        OrganizationDTO updated = organizationService.updateOrganization(id, organizationDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organization (soft delete)")
    @PreAuthorize("@securityExpressionService.isOrganizationOwner(#id, authentication)")
    public ResponseEntity<Void> deleteOrganization(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        organizationService.deleteOrganization(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get user's organizations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrganizationDTO>> getUserOrganizations(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<OrganizationDTO> organizations = organizationService.getUserOrganizations(userId);
        return ResponseEntity.ok(organizations);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to organization")
    @PreAuthorize("@securityExpressionService.canInviteToOrganization(#id, authentication)")
    public ResponseEntity<MembershipDTO> addMember(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        UUID userId = UUID.fromString(request.get("userId").toString());
        String role = request.get("role").toString();
        UUID invitedBy = UUID.fromString(authentication.getName());
        
        MembershipDTO membership = organizationService.addMember(id, userId, role, invitedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from organization")
    @PreAuthorize("@securityExpressionService.isOrganizationAdmin(#id, authentication)")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        organizationService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get organization members")
    @PreAuthorize("@securityExpressionService.hasOrganizationAccess(#id, authentication)")
    public ResponseEntity<Page<MembershipDTO>> getOrganizationMembers(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MembershipDTO> members = organizationService.getOrganizationMembers(id, pageable);
        return ResponseEntity.ok(members);
    }
}