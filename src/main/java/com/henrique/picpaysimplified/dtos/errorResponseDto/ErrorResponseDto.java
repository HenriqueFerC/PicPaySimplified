package com.henrique.picpaysimplified.dtos.errorResponseDto;

import java.time.LocalDateTime;

public record ErrorResponseDto (
        int status,
        String message,
        String error,
        LocalDateTime localDateTime,
        String path) {
}
