package com.nhis.fraud.service;

import com.nhis.fraud.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ValidationService {

	private static final Set<String> REQUIRED_HEADERS = Set.of(
		"PATIENT ID", "AGE", "GENDER", "DATE OF ENCOUNTER", "DATE OF DISCHARGE", "AMOUNT BILLED", "DIAGNOSIS"
	);

	public void validateHeaders(String[] header) {
		if (header == null || header.length == 0) {
			throw new BadRequestException("CSV appears empty or missing header row");
		}
		Set<String> present = new HashSet<>();
		for (String h : header) present.add(h.trim().toUpperCase());
		List<String> missing = new ArrayList<>();
		for (String expected : REQUIRED_HEADERS) {
			if (!present.contains(expected)) missing.add(expected);
		}
		if (!missing.isEmpty()) {
			throw new BadRequestException("Missing required headers: " + String.join(", ", missing));
		}
	}

	public void validateRow(Map<String, Integer> headerIndex, String[] row, int rowNum) {
		// Row validation disabled as requested: accept all rows as-is.
		// Any parsing/coercion will be handled leniently downstream during ingestion.
	}

	private String get(String[] r, Map<String, Integer> idx, String key) {
		Integer i = idx.get(key);
		return i == null || i >= r.length ? null : r[i];
	}

	private LocalDate parseDate(String s) {
		try { return (s == null || s.isBlank()) ? null : LocalDate.parse(s, DateTimeFormatter.ISO_DATE); }
		catch (Exception e) { return null; }
	}
}


