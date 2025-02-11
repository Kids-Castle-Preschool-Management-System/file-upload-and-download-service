package zw.co.isusu.fileservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zw.co.isusu.fileservice.domain.FileEntity;
import zw.co.isusu.fileservice.persistence.FileRepository;
import zw.co.isusu.fileservice.service.FileService;
import zw.co.isusu.fileservice.service.exception.FileNotFoundException;
import zw.co.isusu.fileservice.service.request.FileUploadRequest;
import zw.co.isusu.fileservice.service.request.UpdateFileMetadataRequest;
import zw.co.isusu.fileservice.service.response.FileDetailsResponse;
import zw.co.isusu.fileservice.service.response.FileDownloadResponse;
import zw.co.isusu.fileservice.service.response.FileUploadResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing files.
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    public FileUploadResponse uploadFile(FileUploadRequest request) throws FileUploadException {
        log.info("Uploading file: {}", request.fileName());
        try {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(request.fileName());
            fileEntity.setFileType(request.fileType());
            fileEntity.setData(request.data());

            FileEntity savedFile = fileRepository.save(fileEntity);

            log.info("File uploaded successfully: {}", savedFile.getFileName());
            return new FileUploadResponse(
                    savedFile.getFileId(),
                    savedFile.getFileName(),
                    savedFile.getFileType()
            );
        } catch (Exception e) {
            log.error("Error uploading file: {}", request.fileName(), e);
            throw new FileUploadException("Failed to upload file", e);
        }
    }

    @Override
    public FileDownloadResponse downloadFile(UUID id) throws FileNotFoundException {
        log.info("Downloading file with ID: {}", id);
        FileEntity fileEntity = fileRepository.findByFileIdAndDeletedFalse(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));

        log.info("File downloaded successfully: {}", fileEntity.getFileName());
        return new FileDownloadResponse(
                fileEntity.getFileId(),
                fileEntity.getFileName(),
                fileEntity.getFileType(),
                fileEntity.getData()
        );
    }

    @Override
    public void deleteFile(UUID fileId) throws FileNotFoundException {
        log.info("Deleting file with ID: {}", fileId);
        FileEntity fileEntity = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found or already deleted"));

        fileEntity.setDeleted(true);
        fileRepository.save(fileEntity);

        log.info("File deleted successfully with ID: {}", fileId);
    }

    @Override
    public FileDetailsResponse getFileById(UUID fileId) throws FileNotFoundException {
        log.info("Fetching file details for ID: {}", fileId);
        FileEntity fileEntity = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + fileId));

        log.info("File details retrieved for ID: {}", fileId);
        return new FileDetailsResponse(
                fileEntity.getFileId(),
                fileEntity.getFileName(),
                fileEntity.getFileType(),
                fileEntity.getData().length,
                fileEntity.getCreatedAt(),
                fileEntity.getUpdatedAt()
        );
    }

    @Override
    public List<FileUploadResponse> uploadFiles(List<MultipartFile> files) throws IOException {
        log.info("Uploading multiple files, total count: {}", files.size());
        if (files.isEmpty()) {
            log.error("No files provided for upload");
            throw new IllegalArgumentException("No files provided for upload");
        }

        List<FileEntity> fileEntities = new ArrayList<>();
        for (MultipartFile file : files) {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(file.getOriginalFilename());
            fileEntity.setFileType(file.getContentType());
            fileEntity.setData(file.getBytes());
            fileEntities.add(fileEntity);
        }

        List<FileEntity> savedFiles = fileRepository.saveAll(fileEntities);

        log.info("Successfully uploaded {} files", savedFiles.size());
        return savedFiles.stream()
                .map(savedFile -> new FileUploadResponse(
                        savedFile.getFileId(),
                        savedFile.getFileName(),
                        savedFile.getFileType()
                ))
                .toList();
    }

    @Override
    public Page<FileDetailsResponse> listFiles(int page, int size) {
        log.info("Listing files, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<FileEntity> filePage = fileRepository.findAllByDeletedFalse(pageable);

        log.info("Retrieved {} files", filePage.getTotalElements());
        return filePage.map(file -> new FileDetailsResponse(
                file.getFileId(),
                file.getFileName(),
                file.getFileType(),
                file.getData().length,
                file.getCreatedAt(),
                file.getUpdatedAt()
        ));
    }

    @Override
    public void deleteFiles(List<UUID> fileIds) throws FileNotFoundException {
        log.info("Deleting multiple files, total count: {}", fileIds.size());
        List<FileEntity> files = fileRepository.findAllByFileIdInAndDeletedFalse(fileIds);

        if (files.isEmpty()) {
            log.error("No valid files found for the provided IDs");
            throw new FileNotFoundException("No valid files found for the provided IDs");
        }

        files.forEach(file -> file.setDeleted(true));
        fileRepository.saveAll(files);

        log.info("Successfully deleted {} files", files.size());
    }

    @Override
    public FileDetailsResponse updateMetadata(UUID fileId, UpdateFileMetadataRequest request) {
        log.info("Updating metadata for file ID: {}", fileId);
        FileEntity file = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found or already deleted"));

        if (request.fileName() != null && !request.fileName().isBlank()) {
            file.setFileName(request.fileName());
        }

        if (request.tags() != null) {
            file.setTags(String.join(",", request.tags()));
        }

        fileRepository.save(file);

        log.info("Metadata updated successfully for file ID: {}", fileId);
        return new FileDetailsResponse(
                file.getFileId(),
                file.getFileName(),
                file.getFileType(),
                file.getData().length,
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }

    @Override
    public FileUploadResponse replaceFile(UUID fileId, MultipartFile file) throws IOException {
        log.info("Replacing file with ID: {}", fileId);
        FileEntity existingFile = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        existingFile.setFileName(file.getOriginalFilename());
        existingFile.setFileType(file.getContentType());
        existingFile.setData(file.getBytes());
        existingFile.setUpdatedAt(LocalDateTime.now());

        fileRepository.save(existingFile);

        log.info("File replaced successfully with ID: {}", fileId);
        return new FileUploadResponse(
                existingFile.getFileId(),
                existingFile.getFileName(),
                existingFile.getFileType()
        );
    }

    @Override
    public FileDownloadResponse previewFile(UUID fileId) {
        log.info("Previewing file with ID: {}", fileId);
        FileEntity file = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        log.info("File previewed successfully with ID: {}", fileId);
        return new FileDownloadResponse(file.getFileId(), file.getFileName(), file.getFileType(), file.getData());
    }
}
