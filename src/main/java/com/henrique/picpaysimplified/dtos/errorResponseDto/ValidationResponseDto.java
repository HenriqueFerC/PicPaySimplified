package com.henrique.picpaysimplified.dtos.errorResponseDto;

import java.util.List;
import java.util.Map;

public record ValidationResponseDto(
        String field,
        List<String>  errors
) {
}
