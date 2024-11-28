package zw.co.isusu.fileservice.service.response;

import zw.co.isusu.fileservice.domain.utils.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileDetailsResponse(
        UUID fileId,
        String fileName,
        String fileType,
        long size, // File size in bytes
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }

