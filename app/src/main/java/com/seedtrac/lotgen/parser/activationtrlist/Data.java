package com.seedtrac.lotgen.parser.activationtrlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("mobile1")
    @Expose
    private String mobile1;
    @SerializedName("trid")
    @Expose
    private Integer trid;
    @SerializedName("trdate")
    @Expose
    private String trdate;
    @SerializedName("lotno")
    @Expose
    private String lotno;
    @SerializedName("Bags")
    @Expose
    private Integer bags;
    @SerializedName("Qty")
    @Expose
    private Double qty;

    public String getMobile1() {
        return mobile1;
    }

    public void setMobile1(String mobile1) {
        this.mobile1 = mobile1;
    }

    public Integer getTrid() {
        return trid;
    }

    public void setTrid(Integer trid) {
        this.trid = trid;
    }

    public String getTrdate() {
        return trdate;
    }

    public void setTrdate(String trdate) {
        this.trdate = trdate;
    }

    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
    }

    public Integer getBags() {
        return bags;
    }

    public void setBags(Integer bags) {
        this.bags = bags;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }
}
