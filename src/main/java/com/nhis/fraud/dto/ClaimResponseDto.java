package com.nhis.fraud.dto;

import com.nhis.fraud.entity.ScoreCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimResponseDto(
	UUID id,
	String patientId,
	Double age,
	String gender,
	LocalDate encounterDate,
	LocalDate dischargeDate,
	BigDecimal amountBilled,
	String diagnosis,
	String fraudType,
	Integer fraudScore,
	ScoreCategory scoreCategory,
	String scoreReasons
) { }


