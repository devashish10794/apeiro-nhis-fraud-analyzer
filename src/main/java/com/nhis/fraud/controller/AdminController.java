package com.nhis.fraud.controller;

import com.nhis.fraud.service.IngestionService;
import com.nhis.fraud.config.ScoringConfig;
import com.nhis.fraud.dto.ScoringConfigDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin")
public class AdminController {

    private final IngestionService ingestionService;
    private final ScoringConfig scoringConfig;

    public AdminController(IngestionService ingestionService, ScoringConfig scoringConfig) {
        this.ingestionService = ingestionService;
        this.scoringConfig = scoringConfig;
    }

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ingest a CSV file upload")
    public ResponseEntity<?> ingest(@RequestPart("file") MultipartFile file) throws Exception {
        IngestionService.IngestResult res = ingestionService.ingest(file);
        return ResponseEntity.ok(Map.of(
            "total", res.total(),
            "inserted", res.inserted(),
            "skipped", res.skipped()
        ));
    }

    @GetMapping("/scoring-config")
    @Operation(summary = "Get current fraud scoring configuration")
    public ScoringConfigDto getConfig() {
        return new ScoringConfigDto(
            scoringConfig.getZeroAmountWeight(),
            scoringConfig.getRatioHighThreshold(),
            scoringConfig.getRatioHighWeight(),
            scoringConfig.getRatioMedThreshold(),
            scoringConfig.getRatioMedWeight(),
            scoringConfig.getInfertilityWeight(),
            scoringConfig.getCyesisWeight(),
            scoringConfig.getDentalWeight(),
            scoringConfig.getOsteoWeight(),
            scoringConfig.getPediatricHtnWeight(),
            scoringConfig.getMissingEncounterWeight(),
            scoringConfig.getDischargeBeforeEncounterWeight(),
            scoringConfig.getLowMax(),
            scoringConfig.getMediumMax()
        );
    }

    @PutMapping(value = "/scoring-config", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update fraud scoring configuration (in-memory)")
    public ResponseEntity<?> updateConfig(@RequestBody ScoringConfigDto dto) {
        scoringConfig.setZeroAmountWeight(dto.zeroAmountWeight());
        scoringConfig.setRatioHighThreshold(dto.ratioHighThreshold());
        scoringConfig.setRatioHighWeight(dto.ratioHighWeight());
        scoringConfig.setRatioMedThreshold(dto.ratioMedThreshold());
        scoringConfig.setRatioMedWeight(dto.ratioMedWeight());
        scoringConfig.setInfertilityWeight(dto.infertilityWeight());
        scoringConfig.setCyesisWeight(dto.cyesisWeight());
        scoringConfig.setDentalWeight(dto.dentalWeight());
        scoringConfig.setOsteoWeight(dto.osteoWeight());
        scoringConfig.setPediatricHtnWeight(dto.pediatricHtnWeight());
        scoringConfig.setMissingEncounterWeight(dto.missingEncounterWeight());
        scoringConfig.setDischargeBeforeEncounterWeight(dto.dischargeBeforeEncounterWeight());
        scoringConfig.setLowMax(dto.lowMax());
        scoringConfig.setMediumMax(dto.mediumMax());
        return ResponseEntity.noContent().build();
    }
}


