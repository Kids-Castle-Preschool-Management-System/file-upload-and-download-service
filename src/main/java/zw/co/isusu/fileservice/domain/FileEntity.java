package zw.co.isusu.fileservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import zw.co.isusu.fileservice.domain.utils.BaseEntity;
import java.util.UUID;

/**
 * Represents file data stored in the database.
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "files")
public class FileEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private UUID fileId = UUID.randomUUID();

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Lob
    private byte[] data;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column
    private String tags;

}

