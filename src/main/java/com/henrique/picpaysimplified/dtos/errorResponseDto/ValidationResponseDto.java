package com.henrique.picpaysimplified.dtos.errorResponseDto;

import java.util.List;

public record ValidationResponseDto(
        String field,
        List<String> errors
) {
}
