package com.seedtrac.lotgen.parser.actbarcodelist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("lotno")
    @Expose
    private String lotno;
    @SerializedName("barcode")
    @Expose
    private String barcode;
    @SerializedName("actdate")
    @Expose
    private String actdate;
    @SerializedName("weight")
    @Expose
    private String weight;

    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getActdate() {
        return actdate;
    }

    public void setActdate(String actdate) {
        this.actdate = actdate;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
