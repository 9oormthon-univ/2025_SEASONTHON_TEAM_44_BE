package goorm._44.dto.request;

import java.util.List;

public record PresignBatchRequest(
        List<PresignRequest> files
) {}
