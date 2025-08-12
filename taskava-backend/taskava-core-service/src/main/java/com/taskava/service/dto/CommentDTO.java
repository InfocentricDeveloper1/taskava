package com.taskava.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDTO {
    private UUID id;
    private String content;
    private UserDTO author;
    private UUID taskId;
    private CommentDTO parentComment;
    private List<CommentDTO> replies;
    private List<AttachmentDTO> attachments;
    private boolean edited;
    private Instant createdAt;
    private Instant updatedAt;
}