package com.nhis.fraud.service;

import com.nhis.fraud.entity.Claim;
import com.nhis.fraud.exception.BadRequestException;
import com.nhis.fraud.repository.ClaimRepository;
import com.nhis.fraud.util.CsvUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IngestionService {

    private final ClaimRepository claimRepository;
    private final FraudScoringService fraudScoringService;
    private final ValidationService validationService;

    public IngestionService(ClaimRepository claimRepository, FraudScoringService fraudScoringService, ValidationService validationService) {
        this.claimRepository = claimRepository;
        this.fraudScoringService = fraudScoringService;
        this.validationService = validationService;
    }

	public record IngestResult(long total, long inserted, long skipped) { }

	@Transactional
    public IngestResult ingest(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file uploaded");
        }
        // 25 MB limit (adjust as needed)
        long maxBytes = 25L * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new com.nhis.fraud.exception.BadRequestException("File too large. Max 25MB");
        }

        List<String[]> rows = CsvUtils.readAll(file.getInputStream());
		if (rows.isEmpty()) return new IngestResult(0, 0, 0);
        String[] header = rows.get(0);
        validationService.validateHeaders(header);
        Map<String, Integer> idx = indexMap(header);
		List<Claim> claims = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
			String[] r = rows.get(i);
            validationService.validateRow(idx, r, i + 1);
			Claim c = mapRow(r, idx);
			if (c != null) claims.add(c);
		}
		Map<String, BigDecimal> medians = computeDiagnosisMedians(claims);
		scoreAll(claims, medians);
		List<Claim> unique = dedupe(claims);
		long before = claimRepository.count();
		claimRepository.saveAll(unique);
		long after = claimRepository.count();
		long inserted = Math.max(0, after - before);
		long skipped = unique.size() - inserted;
		return new IngestResult(rows.size() - 1, inserted, skipped);
	}

	private Map<String, BigDecimal> computeDiagnosisMedians(List<Claim> claims) {
		Map<String, List<BigDecimal>> byDiag = claims.stream()
			.filter(c -> c.getAmountBilled() != null && c.getAmountBilled().compareTo(BigDecimal.ZERO) > 0)
			.collect(Collectors.groupingBy(
				c -> Optional.ofNullable(c.getDiagnosis()).orElse("").toUpperCase(),
				Collectors.mapping(Claim::getAmountBilled, Collectors.toList())
			));
		Map<String, BigDecimal> medians = new HashMap<>();
		for (Map.Entry<String, List<BigDecimal>> e : byDiag.entrySet()) {
			List<BigDecimal> list = e.getValue();
			list.sort(Comparator.naturalOrder());
			if (!list.isEmpty()) {
				int mid = list.size() / 2;
				BigDecimal median = list.size() % 2 == 1 ? list.get(mid)
					: list.get(mid - 1).add(list.get(mid)).divide(new BigDecimal("2"));
				medians.put(e.getKey(), median);
			}
		}
		return medians;
	}

	private void scoreAll(List<Claim> claims, Map<String, BigDecimal> medians) {
		for (Claim c : claims) {
			FraudScoringService.ScoreResult sr = fraudScoringService.score(c, medians);
			c.setFraudScore(sr.score());
			c.setScoreCategory(sr.category());
			c.setScoreReasons(sr.reasons());
		}
	}

	private List<Claim> dedupe(List<Claim> claims) {
		Set<String> seen = new HashSet<>();
		List<Claim> unique = new ArrayList<>();
		for (Claim c : claims) {
			String key = String.join("|",
				Optional.ofNullable(c.getPatientId()).orElse(""),
				Optional.ofNullable(c.getEncounterDate()).map(Object::toString).orElse(""),
				Optional.ofNullable(c.getDiagnosis()).orElse(""),
				Optional.ofNullable(c.getAmountBilled()).map(Object::toString).orElse("")
			);
			if (seen.add(key)) unique.add(c);
		}
		return unique;
	}

	private Map<String, Integer> indexMap(String[] header) {
		Map<String, Integer> m = new HashMap<>();
		for (int i = 0; i < header.length; i++) {
			m.put(header[i].trim().toUpperCase(), i);
		}
		return m;
	}

	private Claim mapRow(String[] r, Map<String, Integer> idx) {
		try {
			String patientId = get(r, idx, "PATIENT ID");
			if (patientId == null || patientId.isBlank()) return null;
			Double age = parseDouble(get(r, idx, "AGE"));
			String gender = opt(get(r, idx, "GENDER"));
			LocalDate encounter = parseDate(get(r, idx, "DATE OF ENCOUNTER"));
			LocalDate discharge = parseDate(get(r, idx, "DATE OF DISCHARGE"));
			BigDecimal amount = parseBigDecimal(get(r, idx, "AMOUNT BILLED"));
			String diagnosis = opt(get(r, idx, "DIAGNOSIS"));
			String fraudType = opt(get(r, idx, "FRAUD_TYPE"));

			return Claim.builder()
				.patientId(patientId)
				.age(age)
				.gender(gender)
				.encounterDate(encounter)
				.dischargeDate(discharge)
				.amountBilled(amount)
				.diagnosis(diagnosis)
				.fraudType(fraudType)
				.build();
		} catch (Exception e) {
			return null;
		}
	}

	private String get(String[] r, Map<String, Integer> idx, String key) { Integer i = idx.get(key); return i == null || i >= r.length ? null : r[i]; }
	private String opt(String s) { return s == null ? null : s.trim(); }
	private Double parseDouble(String s) { try { return s == null || s.isBlank() ? null : Double.parseDouble(s); } catch (Exception e) { return null; } }
	private BigDecimal parseBigDecimal(String s) { try { return s == null || s.isBlank() ? null : new BigDecimal(s); } catch (Exception e) { return null; } }
	private LocalDate parseDate(String s) {
		if (s == null || s.isBlank()) return null;
		s = s.trim();
		// Try multiple date formats
		DateTimeFormatter[] formatters = {
			DateTimeFormatter.ISO_DATE,                    // YYYY-MM-DD
			DateTimeFormatter.ofPattern("yyyy-MM-dd"),     // YYYY-MM-DD (explicit)
			DateTimeFormatter.ofPattern("MM/dd/yyyy"),    // MM/DD/YYYY
			DateTimeFormatter.ofPattern("dd/MM/yyyy"),      // DD/MM/YYYY
			DateTimeFormatter.ofPattern("yyyy/MM/dd"),      // YYYY/MM/DD
			DateTimeFormatter.ofPattern("M/d/yyyy"),       // M/D/YYYY
			DateTimeFormatter.ofPattern("d/M/yyyy"),        // D/M/YYYY
		};
		for (DateTimeFormatter fmt : formatters) {
			try {
				return LocalDate.parse(s, fmt);
			} catch (Exception ignored) {
				// Try next format
			}
		}
		return null;
	}
}


