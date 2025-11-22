package com.nhis.fraud.config;

import org.springframework.stereotype.Component;

@Component
public class ScoringConfig {
    // Amount-based
    private int zeroAmountWeight = 70;
    private double ratioHighThreshold = 2.0;
    private int ratioHighWeight = 40;
    private double ratioMedThreshold = 1.5;
    private int ratioMedWeight = 25;

    // Diagnosis-based
    private int infertilityWeight = 15;
    private int cyesisWeight = 15;
    private int dentalWeight = 10;
    private int osteoWeight = 10;
    private int pediatricHtnWeight = 15;

    // Date-based
    private int missingEncounterWeight = 20;
    private int dischargeBeforeEncounterWeight = 20;

    // Category cutoffs
    private int lowMax = 25;     // <= lowMax => LOW
    private int mediumMax = 75;  // <= mediumMax => MEDIUM; else HIGH

    public int getZeroAmountWeight() { return zeroAmountWeight; }
    public void setZeroAmountWeight(int v) { this.zeroAmountWeight = v; }

    public double getRatioHighThreshold() { return ratioHighThreshold; }
    public void setRatioHighThreshold(double v) { this.ratioHighThreshold = v; }

    public int getRatioHighWeight() { return ratioHighWeight; }
    public void setRatioHighWeight(int v) { this.ratioHighWeight = v; }

    public double getRatioMedThreshold() { return ratioMedThreshold; }
    public void setRatioMedThreshold(double v) { this.ratioMedThreshold = v; }

    public int getRatioMedWeight() { return ratioMedWeight; }
    public void setRatioMedWeight(int v) { this.ratioMedWeight = v; }

    public int getInfertilityWeight() { return infertilityWeight; }
    public void setInfertilityWeight(int v) { this.infertilityWeight = v; }

    public int getCyesisWeight() { return cyesisWeight; }
    public void setCyesisWeight(int v) { this.cyesisWeight = v; }

    public int getDentalWeight() { return dentalWeight; }
    public void setDentalWeight(int v) { this.dentalWeight = v; }

    public int getOsteoWeight() { return osteoWeight; }
    public void setOsteoWeight(int v) { this.osteoWeight = v; }

    public int getPediatricHtnWeight() { return pediatricHtnWeight; }
    public void setPediatricHtnWeight(int v) { this.pediatricHtnWeight = v; }

    public int getMissingEncounterWeight() { return missingEncounterWeight; }
    public void setMissingEncounterWeight(int v) { this.missingEncounterWeight = v; }

    public int getDischargeBeforeEncounterWeight() { return dischargeBeforeEncounterWeight; }
    public void setDischargeBeforeEncounterWeight(int v) { this.dischargeBeforeEncounterWeight = v; }

    public int getLowMax() { return lowMax; }
    public void setLowMax(int v) { this.lowMax = v; }

    public int getMediumMax() { return mediumMax; }
    public void setMediumMax(int v) { this.mediumMax = v; }
}


