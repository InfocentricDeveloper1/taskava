package com.taskava.common.dto;

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
    private UUID taskId;
    private UUID authorId;
    private String authorName;
    private String authorAvatar;
    private UUID parentId;
    private List<CommentDTO> replies;
    private List<UUID> mentionedUserIds;
    private List<AttachmentDTO> attachments;
    private boolean edited;
    private boolean pinned;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentDTO {
        private UUID id;
        private String fileName;
        private String fileUrl;
        private String fileType;
        private Long fileSize;
    }
}