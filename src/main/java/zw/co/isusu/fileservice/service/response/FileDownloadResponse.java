package zw.co.isusu.fileservice.service.response;

import java.util.UUID;

public record FileDownloadResponse(UUID fileId, String fileName, String fileType, byte [] data) {
}
