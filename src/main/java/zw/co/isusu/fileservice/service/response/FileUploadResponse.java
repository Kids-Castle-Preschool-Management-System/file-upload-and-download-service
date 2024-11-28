package zw.co.isusu.fileservice.service.response;

/**
 * DTO for returning file details to clients.
 */

public record FileUploadResponse(
        Object fileId,
        String fileName,
        String fileType
) {}

