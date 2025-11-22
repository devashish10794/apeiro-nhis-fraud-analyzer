package com.nhis.fraud.service;

import com.nhis.fraud.entity.Claim;
import com.nhis.fraud.entity.ScoreCategory;
import org.springframework.stereotype.Service;
import com.nhis.fraud.config.ScoringConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class FraudScoringService {

    private final ScoringConfig cfg;

    public FraudScoringService(ScoringConfig cfg) {
        this.cfg = cfg;
    }

	private static final Pattern INFERTILITY = Pattern.compile("INFERTIL", Pattern.CASE_INSENSITIVE);
	private static final Pattern CYESIS = Pattern.compile("CYESIS|TRIMESTER", Pattern.CASE_INSENSITIVE);
	private static final Pattern DENTAL = Pattern.compile("PERI.?ODONT|APICAL", Pattern.CASE_INSENSITIVE);
	private static final Pattern OSTEO = Pattern.compile("OSTEO", Pattern.CASE_INSENSITIVE);
	private static final Pattern HTN = Pattern.compile("\\bHTN\\b|HYPERTEN", Pattern.CASE_INSENSITIVE);

	public record ScoreResult(int score, ScoreCategory category, String reasons) { }

	public ScoreResult score(Claim claim, Map<String, BigDecimal> diagnosisMedian) {
		int score = 0;
		List<String> reasons = new ArrayList<>();

		BigDecimal amount = Optional.ofNullable(claim.getAmountBilled()).orElse(BigDecimal.ZERO);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            score += cfg.getZeroAmountWeight();
			reasons.add("amount is zero");
		}

		String diagnosis = Optional.ofNullable(claim.getDiagnosis()).orElse("");
		BigDecimal median = diagnosisMedian.getOrDefault(diagnosis.toUpperCase(), BigDecimal.ZERO);
		if (median.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal ratio = amount.divide(median, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(BigDecimal.valueOf(cfg.getRatioHighThreshold())) > 0) {
                score += cfg.getRatioHighWeight(); reasons.add("amount > " + cfg.getRatioHighThreshold() + "x diagnosis median");
            } else if (ratio.compareTo(BigDecimal.valueOf(cfg.getRatioMedThreshold())) > 0) {
                score += cfg.getRatioMedWeight(); reasons.add("amount > " + cfg.getRatioMedThreshold() + "x diagnosis median");
			}
		}

		String diagUpper = diagnosis.toUpperCase();
        if (INFERTILITY.matcher(diagUpper).find()) { score += cfg.getInfertilityWeight(); reasons.add("high-risk diagnosis: infertility"); }
        if (CYESIS.matcher(diagUpper).find()) { score += cfg.getCyesisWeight(); reasons.add("high-risk diagnosis: cyesis/trimester"); }
        if (DENTAL.matcher(diagUpper).find()) { score += cfg.getDentalWeight(); reasons.add("high-risk diagnosis: dental apical/periodontitis"); }
        if (OSTEO.matcher(diagUpper).find()) { score += cfg.getOsteoWeight(); reasons.add("high-risk diagnosis: osteo*"); }
        if (HTN.matcher(diagUpper).find() && Optional.ofNullable(claim.getAge()).orElse(0.0) < 18.0) { score += cfg.getPediatricHtnWeight(); reasons.add("pediatric HTN anomaly"); }

		LocalDate enc = claim.getEncounterDate();
		LocalDate dis = claim.getDischargeDate();
        if (enc == null) { score += cfg.getMissingEncounterWeight(); reasons.add("missing encounter date"); }
        if (enc != null && dis != null && dis.isBefore(enc)) { score += cfg.getDischargeBeforeEncounterWeight(); reasons.add("discharge before encounter"); }

		// Clamp 0..100
        score = Math.max(0, Math.min(100, score));
        ScoreCategory category = score <= cfg.getLowMax() ? ScoreCategory.LOW : (score <= cfg.getMediumMax() ? ScoreCategory.MEDIUM : ScoreCategory.HIGH);
		return new ScoreResult(score, category, String.join("; ", reasons));
	}
}


