package com.nhis.fraud.service;

import com.nhis.fraud.dto.MetricsDto;
import com.nhis.fraud.entity.Claim;
import com.nhis.fraud.entity.ScoreCategory;
import com.nhis.fraud.repository.ClaimRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MetricsService {

	private final ClaimRepository claimRepository;

	public MetricsService(ClaimRepository claimRepository) {
		this.claimRepository = claimRepository;
	}

	public MetricsDto getMetrics() {
		long total = claimRepository.count();
		double avg = 0.0;
		long high = 0, low = 0, med = 0;
		if (total > 0) {
			List<Claim> all = claimRepository.findAll();
			BigDecimal sum = all.stream()
				.map(Claim::getAmountBilled)
				.filter(a -> a != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			avg = sum.doubleValue() / total;
			for (Claim c : all) {
				if (c.getScoreCategory() == ScoreCategory.HIGH) high++;
				else if (c.getScoreCategory() == ScoreCategory.MEDIUM) med++;
				else low++;
			}
		}
		double pctHigh = total == 0 ? 0.0 : (high * 100.0) / total;
		return new MetricsDto(total, avg, high, pctHigh, low, med, high);
	}
}


