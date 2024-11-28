package zw.co.isusu.fileservice.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zw.co.isusu.fileservice.domain.FileEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByFileIdAndDeletedFalse(UUID fileId);

    Page<FileEntity> findAllByDeletedFalse(Pageable pageable);

    List<FileEntity> findAllByFileIdInAndDeletedFalse(List<UUID> fileIds);
}

