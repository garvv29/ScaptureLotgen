package com.seedtrac.lotgen.model;

public class LoadingTrList {
    private String lotNumber;
    private String harvestDate;
    private int numberOfBags;
    private double totalQty;
    private double tareWeight;
    private String gotStatus;
    private String moisture;

    public LoadingTrList(String lotNumber, String harvestDate, int numberOfBags, double totalQty, double tareWeight, String gotStatus, String moisture) {
        this.lotNumber = lotNumber;
        this.harvestDate = harvestDate;
        this.numberOfBags = numberOfBags;
        this.totalQty = totalQty;
        this.tareWeight = tareWeight;
        this.gotStatus = gotStatus;
        this.moisture = moisture;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(String harvestDate) {
        this.harvestDate = harvestDate;
    }

    public int getNumberOfBags() {
        return numberOfBags;
    }

    public void setNumberOfBags(int numberOfBags) {
        this.numberOfBags = numberOfBags;
    }

    public double getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(double totalQty) {
        this.totalQty = totalQty;
    }

    public double getTareWeight() {
        return tareWeight;
    }

    public void setTareWeight(double tareWeight) {
        this.tareWeight = tareWeight;
    }

    public String getGotStatus() {
        return gotStatus;
    }

    public void setGotStatus(String gotStatus) {
        this.gotStatus = gotStatus;
    }

    public String getMoisture() {
        return moisture;
    }

    public void setMoisture(String moisture) {
        this.moisture = moisture;
    }
}
