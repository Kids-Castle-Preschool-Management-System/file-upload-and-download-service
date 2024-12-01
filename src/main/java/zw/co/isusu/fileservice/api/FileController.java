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

    @Operation(summary = "Upload a file", description = "Uploads a single file to the server.")
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

    @Operation(summary = "Upload multiple files", description = "Uploads multiple files to the server.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Files uploaded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/uploads", consumes = "multipart/form-data")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(@RequestPart("files") MultipartFile[] files) throws IOException {
        List<FileUploadResponse> responses = fileService.uploadFiles(Arrays.asList(files));
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(summary = "Delete files", description = "Marks multiple files as deleted by their unique identifiers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
            @ApiResponse(responseCode = "404", description = "One or more files not found")
    })
    @PostMapping("/delete")
    public ResponseEntity<String> deleteFiles(@RequestBody List<UUID> fileIds) throws FileNotFoundException {
        fileService.deleteFiles(fileIds);
        return ResponseEntity.ok("Files deleted successfully");
    }

    @Operation(summary = "Update file metadata", description = "Updates the metadata of an existing file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File metadata updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @PutMapping("/{fileId}")
    public ResponseEntity<FileDetailsResponse> updateMetadata(
            @Parameter(description = "Unique identifier of the file", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID fileId,
            @RequestBody UpdateFileMetadataRequest request) {
        FileDetailsResponse response = fileService.updateMetadata(fileId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Replace a file", description = "Replaces an existing file with a new file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File replaced successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @PutMapping("/{fileId}/replace")
    public ResponseEntity<FileUploadResponse> replaceFile(
            @Parameter(description = "Unique identifier of the file to replace", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID fileId,
            @RequestPart("file") MultipartFile file) throws IOException {
        FileUploadResponse response = fileService.replaceFile(fileId, file);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download a file", description = "Downloads a file by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully", content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "Unique identifier of the file to download", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID fileId) throws FileNotFoundException {
        FileDownloadResponse response = fileService.downloadFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(response.data());
    }

    @Operation(summary = "Get file details", description = "Fetches the details of a file by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{fileId}")
    public ResponseEntity<FileDetailsResponse> getFileDetails(
            @Parameter(description = "Unique identifier of the file", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID fileId) throws FileNotFoundException {
        FileDetailsResponse response = fileService.getFileById(fileId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List all files", description = "Lists all files with pagination support.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files listed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<FileDetailsResponse>> listFiles(
            @Parameter(description = "Page number for pagination") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size for pagination") @RequestParam(defaultValue = "10") int size) {
        Page<FileDetailsResponse> files = fileService.listFiles(page, size);
        return ResponseEntity.ok(files);
    }

    @Operation(summary = "Preview a file", description = "Generates a preview of a file by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File preview generated successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> previewFile(
            @Parameter(description = "Unique identifier of the file to preview", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID fileId) {
        FileDownloadResponse response = fileService.previewFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.fileType()))
                .body(response.data());
    }

    @Operation(summary = "Delete a file", description = "Marks a file as deleted by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "Unique identifier of the file to delete", required = true, schema = @Schema(type = "string", format = "uuid"))
            @PathVariable UUID fileId) throws FileNotFoundException {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok("File deleted successfully");
    }
}
