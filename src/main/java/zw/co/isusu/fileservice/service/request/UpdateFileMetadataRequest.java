package zw.co.isusu.fileservice.service.request;

import java.util.List;

public record UpdateFileMetadataRequest(
        String fileName,
        List<String> tags
) { }

