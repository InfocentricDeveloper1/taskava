package com.taskava.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    
    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 10000, message = "Comment must be between 1 and 10000 characters")
    private String content;
    
    private UUID parentCommentId;
    
    private List<UUID> attachmentIds;
}