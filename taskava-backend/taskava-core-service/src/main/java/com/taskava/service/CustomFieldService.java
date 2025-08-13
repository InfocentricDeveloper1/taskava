package com.taskava.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskava.common.dto.CustomFieldDTO;
import com.taskava.data.entity.CustomField;
import com.taskava.data.entity.Workspace;
import com.taskava.data.repository.CustomFieldRepository;
import com.taskava.data.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomFieldService {

    private final CustomFieldRepository customFieldRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PreAuthorize("@workspaceService.canViewWorkspace(#workspaceId, authentication.principal.id)")
    @Transactional(readOnly = true)
    public List<CustomFieldDTO> getWorkspaceCustomFields(UUID workspaceId, String fieldType) {
        log.info("Fetching custom fields for workspace: {}, type: {}", workspaceId, fieldType);
        
        List<CustomField> fields;
        if (fieldType != null) {
            fields = customFieldRepository.findByWorkspaceIdAndFieldType(workspaceId, 
                    CustomField.FieldType.valueOf(fieldType));
        } else {
            fields = customFieldRepository.findByWorkspaceId(workspaceId);
        }
        
        return fields.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("@workspaceService.canManageWorkspace(#fieldDto.workspaceId, authentication.principal.id)")
    public CustomFieldDTO createCustomField(CustomFieldDTO fieldDto, UUID createdBy) {
        log.info("Creating custom field: {} in workspace: {} by user: {}", 
                fieldDto.getName(), fieldDto.getWorkspaceId(), createdBy);
        
        // Validate workspace exists
        Workspace workspace = workspaceRepository.findActiveById(fieldDto.getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + fieldDto.getWorkspaceId()));
        
        // Generate field key from name if not provided
        String fieldKey = fieldDto.getFieldKey();
        if (fieldKey == null || fieldKey.isEmpty()) {
            fieldKey = generateFieldKey(fieldDto.getName());
        }
        
        // Check if field key already exists
        if (customFieldRepository.existsByWorkspaceIdAndFieldKey(fieldDto.getWorkspaceId(), fieldKey)) {
            throw new IllegalArgumentException("Custom field with key already exists: " + fieldKey);
        }
        
        // Create custom field
        CustomField customField = CustomField.builder()
                .name(fieldDto.getName())
                .fieldKey(fieldKey)
                .fieldType(CustomField.FieldType.valueOf(fieldDto.getFieldType()))
                .description(fieldDto.getDescription())
                .required(fieldDto.isRequired())
                .defaultValue(fieldDto.getDefaultValue())
                .options(fieldDto.getOptions() != null ? String.join(",", fieldDto.getOptions()) : null)
                .validationRules(fieldDto.getValidationRules() != null ? 
                        convertToJson(fieldDto.getValidationRules()) : null)
                .workspace(workspace)
                .displayOrder(fieldDto.getDisplayOrder() != null ? fieldDto.getDisplayOrder() : 
                        customFieldRepository.getMaxDisplayOrder(fieldDto.getWorkspaceId()) + 1)
                .build();
        
        customField = customFieldRepository.save(customField);
        
        return mapToDTO(customField);
    }

    @PreAuthorize("@workspaceService.canManageWorkspace(#workspaceId, authentication.principal.id)")
    public CustomFieldDTO updateCustomField(UUID workspaceId, UUID fieldId, CustomFieldDTO fieldDto) {
        log.info("Updating custom field: {} in workspace: {}", fieldId, workspaceId);
        
        CustomField customField = customFieldRepository.findByIdAndWorkspaceId(fieldId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Custom field not found: " + fieldId));
        
        // Update fields
        if (fieldDto.getName() != null) {
            customField.setName(fieldDto.getName());
        }
        if (fieldDto.getDescription() != null) {
            customField.setDescription(fieldDto.getDescription());
        }
        if (fieldDto.getDefaultValue() != null) {
            customField.setDefaultValue(fieldDto.getDefaultValue());
        }
        if (fieldDto.getOptions() != null) {
            customField.setOptions(String.join(",", fieldDto.getOptions()));
        }
        if (fieldDto.getValidationRules() != null) {
            customField.setValidationRules(convertToJson(fieldDto.getValidationRules()));
        }
        if (fieldDto.getDisplayOrder() != null) {
            customField.setDisplayOrder(fieldDto.getDisplayOrder());
        }
        customField.setRequired(fieldDto.isRequired());
        
        customField = customFieldRepository.save(customField);
        
        return mapToDTO(customField);
    }

    @PreAuthorize("@workspaceService.canManageWorkspace(#workspaceId, authentication.principal.id)")
    public void deleteCustomField(UUID workspaceId, UUID fieldId) {
        log.info("Deleting custom field: {} from workspace: {}", fieldId, workspaceId);
        
        CustomField customField = customFieldRepository.findByIdAndWorkspaceId(fieldId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Custom field not found: " + fieldId));
        
        // Soft delete
        customField.softDelete(UUID.randomUUID()); // Should use current user ID from security context
        customFieldRepository.save(customField);
    }

    private CustomFieldDTO mapToDTO(CustomField field) {
        List<String> options = null;
        if (field.getOptions() != null && !field.getOptions().isEmpty()) {
            options = List.of(field.getOptions().split(","));
        }
        
        java.util.Map<String, Object> validationRules = null;
        if (field.getValidationRules() != null && !field.getValidationRules().isEmpty()) {
            try {
                validationRules = objectMapper.readValue(field.getValidationRules(), java.util.Map.class);
            } catch (Exception e) {
                log.error("Failed to deserialize validation rules", e);
            }
        }
        
        return CustomFieldDTO.builder()
                .id(field.getId())
                .name(field.getName())
                .fieldKey(field.getFieldKey())
                .fieldType(field.getFieldType().toString())
                .description(field.getDescription())
                .required(field.isRequired())
                .defaultValue(field.getDefaultValue())
                .options(options)
                .validationRules(validationRules)
                .workspaceId(field.getWorkspace().getId())
                .displayOrder(field.getDisplayOrder())
                .active(true)
                .createdAt(field.getCreatedAt() != null ? 
                        field.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(field.getUpdatedAt() != null ? 
                        field.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .createdBy(field.getCreatedBy())
                .updatedBy(field.getUpdatedBy())
                .build();
    }

    private String generateFieldKey(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }
    
    private String convertToJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize validation rules", e);
        }
    }
}