package zw.co.isusu.fileservice.service.response;

/**
 * DTO for returning file details to clients.
 */

public record FileResponseDTO(
        String fileId,
        String fileName,
        String contentType,
        Long fileSize
) {}

