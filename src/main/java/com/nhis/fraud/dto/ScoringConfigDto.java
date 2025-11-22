package com.nhis.fraud.dto;

public record ScoringConfigDto(
        int zeroAmountWeight,
        double ratioHighThreshold,
        int ratioHighWeight,
        double ratioMedThreshold,
        int ratioMedWeight,
        int infertilityWeight,
        int cyesisWeight,
        int dentalWeight,
        int osteoWeight,
        int pediatricHtnWeight,
        int missingEncounterWeight,
        int dischargeBeforeEncounterWeight,
        int lowMax,
        int mediumMax
) {}


