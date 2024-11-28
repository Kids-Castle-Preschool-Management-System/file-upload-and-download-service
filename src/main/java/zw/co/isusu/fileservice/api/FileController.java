package zw.co.isusu.fileservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zw.co.isusu.fileservice.service.FileService;
import zw.co.isusu.fileservice.service.exception.FileNotFoundException;
import zw.co.isusu.fileservice.service.request.FileUploadRequest;
import zw.co.isusu.fileservice.service.request.UpdateFileMetadataRequest;
import zw.co.isusu.fileservice.service.response.FileDetailsResponse;
import zw.co.isusu.fileservice.service.response.FileDownloadResponse;
import zw.co.isusu.fileservice.service.response.FileUploadResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Controller for handling file-related operations.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileService fileService;

@Operation(summary = "Upload a file", description = "Uploads a file to the server.")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "File uploaded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
@PostMapping(value = "/upload", consumes = "multipart/form-data")
public ResponseEntity<FileUploadResponse> uploadFile(
        @RequestPart("file") MultipartFile file) throws IOException {
    FileUploadRequest uploadRequest = new FileUploadRequest(
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes()
    );

    FileUploadResponse response = fileService.uploadFile(uploadRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

@Operation(summary = "Upload files", description = "Upload files to the server.")
@ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Files uploaded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponse.class))),
                       @ApiResponse(responseCode = "400", description = "Invalid input data"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping(value = "/uploads",consumes = "multipart/form-data")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(@RequestPart("files") MultipartFile[] files) throws IOException {
        List<FileUploadResponse> responses = fileService.uploadFiles(Arrays.asList(files));
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) throws FileNotFoundException {
        FileDownloadResponse response = fileService.downloadFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(response.data());
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileDetailsResponse> getFileDetails(@PathVariable UUID fileId) throws FileNotFoundException {
        FileDetailsResponse response = fileService.getFileById(fileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<FileDetailsResponse>> listFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FileDetailsResponse> files = fileService.listFiles(page, size);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a file", description = "Marks the file as deleted by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "Unique identifier of the file", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID fileId) throws FileNotFoundException {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok("File marked as deleted successfully");
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteFiles(@RequestBody List<UUID> fileIds) throws FileNotFoundException {
        fileService.deleteFiles(fileIds);
        return ResponseEntity.ok("Files deleted successfully");
    }

    @PutMapping("/{fileId}")
    public ResponseEntity<FileDetailsResponse> updateMetadata(
            @PathVariable UUID fileId,
            @RequestBody UpdateFileMetadataRequest request) {
        FileDetailsResponse response = fileService.updateMetadata(fileId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{fileId}/replace")
    public ResponseEntity<FileUploadResponse> replaceFile(
            @PathVariable UUID fileId,
            @RequestPart("file") MultipartFile file) throws IOException {
        FileUploadResponse response = fileService.replaceFile(fileId, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> previewFile(@PathVariable UUID fileId) {
        FileDownloadResponse response = fileService.previewFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.fileType()))
                .body(response.data());
    }

}

