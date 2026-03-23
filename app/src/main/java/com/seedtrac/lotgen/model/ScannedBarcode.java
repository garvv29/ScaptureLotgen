package com.seedtrac.lotgen.model;

public class ScannedBarcode {
    private int srNo;
    private String barcode;
    private String lotNo;

    public ScannedBarcode(int srNo, String barcode, String lotNo) {
        this.srNo = srNo;
        this.barcode = barcode;
        this.lotNo = lotNo;
    }

    public int getSrNo() {
        return srNo;
    }

    public void setSrNo(int srNo) {
        this.srNo = srNo;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }
}
