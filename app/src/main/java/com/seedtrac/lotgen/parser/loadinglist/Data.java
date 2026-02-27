package com.seedtrac.lotgen.parser.loadinglist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("lotno")
    @Expose
    private String lotno;
    @SerializedName("totbags")
    @Expose
    private String totbags;

    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
    }

    public String getTotbags() {
        return totbags;
    }

    public void setTotbags(String totbags) {
        this.totbags = totbags;
    }
}
