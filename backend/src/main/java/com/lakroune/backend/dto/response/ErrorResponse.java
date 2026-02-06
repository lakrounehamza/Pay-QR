package com.lakroune.backend.dto.response;

import java.time.LocalDateTime;

public record ErrorResponse(
        int Status,String message ,
        LocalDateTime time
) {
}
