package com.nhis.fraud.controller;

import com.nhis.fraud.dto.ChartDataDto;
import com.nhis.fraud.dto.MetricsDto;
import com.nhis.fraud.service.ChartService;
import com.nhis.fraud.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metrics")
@Tag(name = "Metrics")
public class MetricsController {

	private final MetricsService metricsService;
	private final ChartService chartService;

	public MetricsController(MetricsService metricsService, ChartService chartService) {
		this.metricsService = metricsService;
		this.chartService = chartService;
	}

	@GetMapping
	@Operation(summary = "Get overall metrics and fraud score distribution")
	public MetricsDto get() {
		return metricsService.getMetrics();
	}

	@GetMapping("/chart-data")
	@Operation(summary = "Get time-series chart data")
	public List<ChartDataDto> getChartData(
		@RequestParam(required = false) LocalDate startDate,
		@RequestParam(required = false) LocalDate endDate
	) {
		return chartService.getTimeSeriesData(startDate, endDate);
	}
}


