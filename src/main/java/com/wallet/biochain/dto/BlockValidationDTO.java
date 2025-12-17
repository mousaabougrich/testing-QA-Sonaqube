package com.wallet.biochain.dto;

import java.util.List;

public record BlockValidationDTO(
        Boolean isValid,
        String blockHash,
        Integer blockIndex,
        List<String> validationErrors,
        String message
) {}