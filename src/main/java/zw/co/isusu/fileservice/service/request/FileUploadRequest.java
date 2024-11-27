package zw.co.isusu.fileservice.service.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for handling file upload requests.
 */

public record FileUploadRequestDTO(
        @NotNull MultipartFile file,
        String metadata // Optional JSON metadata
) {}

