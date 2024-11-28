package zw.co.isusu.fileservice.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import zw.co.isusu.fileservice.service.exception.FileNotFoundException;
import zw.co.isusu.fileservice.service.request.FileUploadRequest;
import zw.co.isusu.fileservice.service.request.UpdateFileMetadataRequest;
import zw.co.isusu.fileservice.service.response.FileDetailsResponse;
import zw.co.isusu.fileservice.service.response.FileDownloadResponse;
import zw.co.isusu.fileservice.service.response.FileUploadResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for file operations.
 */
public interface FileService {
    FileUploadResponse uploadFile(FileUploadRequest request);

    FileDownloadResponse downloadFile(UUID id) throws FileNotFoundException;

    FileDetailsResponse getFileById(UUID id) throws FileNotFoundException;

    void deleteFile(UUID fileId) throws FileNotFoundException;

    List<FileUploadResponse> uploadFiles(List<MultipartFile> files) throws IOException;

    Page<FileDetailsResponse> listFiles(int page, int size);

    void deleteFiles(List<UUID> fileIds) throws FileNotFoundException;

    FileDetailsResponse updateMetadata(UUID fileId, UpdateFileMetadataRequest request);

    FileUploadResponse replaceFile(UUID fileId, MultipartFile file) throws IOException;

    FileDownloadResponse previewFile(UUID fileId);
}

