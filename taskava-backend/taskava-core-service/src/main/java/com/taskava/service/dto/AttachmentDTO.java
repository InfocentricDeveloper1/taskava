package com.taskava.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentDTO {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private UserDTO uploadedBy;
    private Instant uploadedAt;
}