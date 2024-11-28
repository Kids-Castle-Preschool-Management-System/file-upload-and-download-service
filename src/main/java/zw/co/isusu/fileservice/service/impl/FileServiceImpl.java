package zw.co.isusu.fileservice.service.impl;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    public FileUploadResponse uploadFile(FileUploadRequest request) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(request.fileName());
        fileEntity.setFileType(request.fileType());
        fileEntity.setData(request.data());
        FileEntity savedFile = fileRepository.save(fileEntity);
        return new FileUploadResponse(
                savedFile.getFileId(),
                savedFile.getFileName(),
                savedFile.getFileType()
        );
    }

    @Override
    public FileDownloadResponse downloadFile(UUID id) throws FileNotFoundException {
        FileEntity fileEntity = fileRepository.findByFileIdAndDeletedFalse(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + id));

        return new FileDownloadResponse(
                fileEntity.getFileId(),
                fileEntity.getFileName(),
                fileEntity.getFileType(),
                fileEntity.getData()
        );
    }

    @Override
    public void deleteFile(UUID fileId) throws FileNotFoundException {
        FileEntity fileEntity = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found or already deleted"));

        fileEntity.setDeleted(true);
        fileRepository.save(fileEntity);
    }

    @Override
    public FileDetailsResponse getFileById(UUID fileId) throws FileNotFoundException {
        FileEntity fileEntity = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + fileId));

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
        if (files == null || files.isEmpty()) {
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

        Pageable pageable = PageRequest.of(page, size);
        return fileRepository.findAllByDeletedFalse(pageable)
                .map(file -> new FileDetailsResponse(
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
        List<FileEntity> files = fileRepository.findAllByFileIdInAndDeletedFalse(fileIds);

        if (files.isEmpty()) {
            throw new FileNotFoundException("No valid files found for the provided IDs");
        }

        files.forEach(file -> file.setDeleted(true));
        fileRepository.saveAll(files);
    }

    @Override
    public FileDetailsResponse updateMetadata(UUID fileId, UpdateFileMetadataRequest request) {
        FileEntity file = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found or already deleted"));

        if (request.fileName() != null && !request.fileName().isBlank()) {
            file.setFileName(request.fileName());
        }

        if (request.tags() != null) {
            file.setTags(String.join(",", request.tags()));
        }

        fileRepository.save(file);

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

        FileEntity existingFile = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        existingFile.setFileName(file.getOriginalFilename());
        existingFile.setFileType(file.getContentType());
        existingFile.setData(file.getBytes());
        existingFile.setUpdatedAt(LocalDateTime.now());

        fileRepository.save(existingFile);

        return new FileUploadResponse(
                existingFile.getFileId(),
                existingFile.getFileName(),
                existingFile.getFileType()
        );
    }
    @Override
    public FileDownloadResponse previewFile(UUID fileId) {
        FileEntity file = fileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        return new FileDownloadResponse(file.getFileId(), file.getFileName(), file.getFileType(), file.getData());
    }

}
