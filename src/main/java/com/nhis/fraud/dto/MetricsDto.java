package com.nhis.fraud.dto;

public record MetricsDto(
	long totalClaims,
	double averageAmount,
	long highRiskCount,
	double highRiskPercent,
	long lowCount,
	long mediumCount,
	long highCount
) { }


