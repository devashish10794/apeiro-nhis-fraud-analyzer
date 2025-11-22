package com.nhis.fraud;

import com.nhis.fraud.config.ScoringConfig;
import com.nhis.fraud.entity.Claim;
import com.nhis.fraud.entity.ScoreCategory;
import com.nhis.fraud.service.FraudScoringService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FraudScoringServiceTests {

	private FraudScoringService createService() {
		return new FraudScoringService(new ScoringConfig());
	}

	@Test
	void scoresHighForZeroAmount() {
		FraudScoringService svc = createService();
		Claim claim = Claim.builder()
			.diagnosis("HTN")
			.amountBilled(BigDecimal.ZERO)
			.encounterDate(LocalDate.now())
			.dischargeDate(LocalDate.now())
			.age(30.0)
			.gender("M")
			.build();
		FraudScoringService.ScoreResult result = svc.score(claim, Map.of("HTN", new BigDecimal("8000")));
		assertTrue(result.score() >= 70);
		assertEquals(ScoreCategory.HIGH, result.category());
	}

	@Test
	void ratioAboveTwoAdds40() {
		FraudScoringService svc = createService();
		Claim claim = Claim.builder()
			.diagnosis("HTN")
			.amountBilled(new BigDecimal("20000"))
			.age(40.0)
			.build();
		FraudScoringService.ScoreResult result = svc.score(claim, Map.of("HTN", new BigDecimal("9000")));
		assertTrue(result.score() >= 40);
	}
}


