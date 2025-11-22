package com.nhis.fraud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChartDataDto(
        LocalDate date,
        long count,
        double averageScore,
        BigDecimal totalAmount,
        long lowCount,
        long mediumCount,
        long highCount
) { }

