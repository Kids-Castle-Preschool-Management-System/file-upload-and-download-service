package zw.co.isusu.fileservice.api;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Upload a file", description = "Uploads a single file to the server.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestPart("file") MultipartFile file) throws IOException {
        log.info("Uploading file: {}", file.getOriginalFilename());
        FileUploadRequest uploadRequest = new FileUploadRequest(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        FileUploadResponse response = fileService.uploadFile(uploadRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Upload multiple files", description = "Uploads multiple files to the server.")
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(@RequestPart("files") MultipartFile[] files) throws IOException {
        log.info("Uploading {} files", files.length);
        List<FileUploadResponse> responses = fileService.uploadFiles(Arrays.asList(files));
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(summary = "Delete files", description = "Marks multiple files as deleted by their unique identifiers.")
    @PostMapping("/delete")
    public ResponseEntity<String> deleteFiles(@RequestBody List<UUID> fileIds) throws FileNotFoundException {
        log.info("Deleting files with IDs: {}", fileIds);
        fileService.deleteFiles(fileIds);
        return ResponseEntity.ok("Files deleted successfully");
    }

    @Operation(summary = "Update file metadata", description = "Updates the metadata of an existing file.")
    @PutMapping("/{fileId}")
    public ResponseEntity<FileDetailsResponse> updateMetadata(@PathVariable UUID fileId, @RequestBody UpdateFileMetadataRequest request) {
        log.info("Updating metadata for file ID: {}", fileId);
        FileDetailsResponse response = fileService.updateMetadata(fileId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Replace a file", description = "Replaces an existing file with a new file.")
    @PutMapping("/{fileId}/replace")
    public ResponseEntity<FileUploadResponse> replaceFile(@PathVariable UUID fileId, @RequestPart("file") MultipartFile file) throws IOException {
        log.info("Replacing file with ID: {}", fileId);
        FileUploadResponse response = fileService.replaceFile(fileId, file);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download a file", description = "Downloads a file by its unique identifier.")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) throws FileNotFoundException {
        log.info("Downloading file with ID: {}", fileId);
        FileDownloadResponse response = fileService.downloadFile(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.fileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(response.data());
    }

    @Operation(summary = "Get file details", description = "Fetches the details of a file by its unique identifier.")
    @GetMapping("/{fileId}")
    public ResponseEntity<FileDetailsResponse> getFileDetails(@PathVariable UUID fileId) throws FileNotFoundException {
        log.info("Fetching details for file ID: {}", fileId);
        FileDetailsResponse response = fileService.getFileById(fileId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List all files", description = "Lists all files with pagination support.")
    @GetMapping
    public ResponseEntity<Page<FileDetailsResponse>> listFiles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        log.info("Listing files with pagination: page={}, size={}", page, size);
        Page<FileDetailsResponse> files = fileService.listFiles(page, size);
        return ResponseEntity.ok(files);
    }

    @Operation(summary = "Preview a file", description = "Generates a preview of a file by its unique identifier.")
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> previewFile(@PathVariable UUID fileId) {
        log.info("Generating preview for file ID: {}", fileId);
        FileDownloadResponse response = fileService.previewFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.fileType()))
                .body(response.data());
    }

    @Operation(summary = "Delete a file", description = "Marks a file as deleted by its unique identifier.")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable UUID fileId) throws FileNotFoundException {
        log.info("Deleting file with ID: {}", fileId);
        fileService.deleteFile(fileId);
        return ResponseEntity.ok("File deleted successfully");
    }
}
