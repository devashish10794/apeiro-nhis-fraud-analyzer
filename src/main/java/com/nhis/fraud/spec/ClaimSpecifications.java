package com.nhis.fraud.spec;

import com.nhis.fraud.entity.Claim;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Objects;

public class ClaimSpecifications {
	public static Specification<Claim> patientIdEquals(String v) {
		return (root, q, cb) -> Objects.isNull(v) ? null : cb.equal(root.get("patientId"), v);
	}
	public static Specification<Claim> genderEquals(String v) {
		return (root, q, cb) -> Objects.isNull(v) ? null : cb.equal(root.get("gender"), v);
	}
	public static Specification<Claim> diagnosisContains(String v) {
		return (root, q, cb) -> Objects.isNull(v) ? null : cb.like(cb.lower(root.get("diagnosis")), "%" + v.toLowerCase() + "%");
	}
	public static Specification<Claim> scoreGte(Integer v) {
		return (root, q, cb) -> Objects.isNull(v) ? null : cb.greaterThanOrEqualTo(root.get("fraudScore"), v);
	}
	public static Specification<Claim> scoreLte(Integer v) {
		return (root, q, cb) -> Objects.isNull(v) ? null : cb.lessThanOrEqualTo(root.get("fraudScore"), v);
	}
	public static Specification<Claim> dateGte(LocalDate v) {
		return (root, q, cb) -> Objects.isNull(v) ? null : cb.greaterThanOrEqualTo(root.get("encounterDate"), v);
	}
	public static Specification<Claim> dateLte(LocalDate v) {
		return (root, q, cb) -> Objects.isNull(v) ? null : cb.lessThanOrEqualTo(root.get("encounterDate"), v);
	}
}


