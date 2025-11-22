package com.nhis.fraud.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "claims", indexes = {
	@Index(name = "idx_claims_patient", columnList = "patientId"),
	@Index(name = "idx_claims_diagnosis", columnList = "diagnosis"),
	@Index(name = "idx_claims_fraudScore", columnList = "fraudScore")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Claim {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false)
	private String patientId;

	private Double age;

	private String gender;

	private LocalDate encounterDate;

	private LocalDate dischargeDate;

	@Column(precision = 15, scale = 2)
	private BigDecimal amountBilled;

	@Column(length = 512)
	private String diagnosis;

	private String fraudType;

	private Integer fraudScore;

	@Enumerated(EnumType.STRING)
	private ScoreCategory scoreCategory;

	@Column(columnDefinition = "text")
	private String scoreReasons;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (id == null) id = UUID.randomUUID();
		if (createdAt == null) createdAt = OffsetDateTime.now();
	}
}


