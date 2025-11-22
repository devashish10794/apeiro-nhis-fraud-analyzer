package com.nhis.fraud.controller;

import com.nhis.fraud.dto.ClaimResponseDto;
import com.nhis.fraud.entity.Claim;
import com.nhis.fraud.repository.ClaimRepository;
import com.nhis.fraud.spec.ClaimSpecifications;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Claims")
public class ClaimsController {

	private final ClaimRepository claimRepository;

	public ClaimsController(ClaimRepository claimRepository) { this.claimRepository = claimRepository; }

	@GetMapping
	@Operation(summary = "List claims with pagination, sorting, and filters")
	public Page<ClaimResponseDto> list(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "25") int size,
		@RequestParam(defaultValue = "fraudScore,desc") @Parameter(description = "field,direction") String sort,
		@RequestParam(required = false) String patientId,
		@RequestParam(required = false) String gender,
		@RequestParam(required = false) String diagnosis,
		@RequestParam(required = false) Integer minScore,
		@RequestParam(required = false) Integer maxScore,
		@RequestParam(required = false) LocalDate startDate,
		@RequestParam(required = false) LocalDate endDate
	) {
		Sort sortObj = Sort.by(Sort.Order.desc("fraudScore"));
		try {
			String[] s = sort.split(",");
			if (s.length == 2) {
				Sort.Direction dir = "asc".equalsIgnoreCase(s[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
				sortObj = Sort.by(new Sort.Order(dir, s[0]));
			}
		} catch (Exception ignored) { }
		Pageable pageable = PageRequest.of(page, size, sortObj);

		Specification<Claim> spec = Specification.where(ClaimSpecifications.patientIdEquals(patientId))
			.and(ClaimSpecifications.genderEquals(gender))
			.and(ClaimSpecifications.diagnosisContains(diagnosis))
			.and(ClaimSpecifications.scoreGte(minScore))
			.and(ClaimSpecifications.scoreLte(maxScore))
			.and(ClaimSpecifications.dateGte(startDate))
			.and(ClaimSpecifications.dateLte(endDate));

		return claimRepository.findAll(spec, pageable).map(this::toDto);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a single claim by id")
	public ClaimResponseDto get(@PathVariable UUID id) {
		Claim c = claimRepository.findById(id).orElseThrow();
		return toDto(c);
	}

	private ClaimResponseDto toDto(Claim c) {
		return new ClaimResponseDto(
			c.getId(), c.getPatientId(), c.getAge(), c.getGender(), c.getEncounterDate(), c.getDischargeDate(),
			c.getAmountBilled(), c.getDiagnosis(), c.getFraudType(), c.getFraudScore(), c.getScoreCategory(), c.getScoreReasons()
		);
	}
}


