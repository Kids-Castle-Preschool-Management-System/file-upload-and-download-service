package zw.co.isusu.fileservice.service.request;

/**
 * DTO for handling file upload requests.
 */

public record FileUploadRequest(String fileName, String fileType, byte[] data) {}

