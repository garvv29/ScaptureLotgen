package com.seedtrac.lotgen.parser.dashboarddata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("recpending")
    @Expose
    private Integer recpending;
    @SerializedName("actpending")
    @Expose
    private Integer actpending;
    @SerializedName("loadpending")
    @Expose
    private Integer loadpending;
    @SerializedName("totactivated")
    @Expose
    private Integer totactivated;
    @SerializedName("totdisp")
    @Expose
    private Integer totdisp;

    public Integer getRecpending() {
        return recpending;
    }

    public void setRecpending(Integer recpending) {
        this.recpending = recpending;
    }

    public Integer getActpending() {
        return actpending;
    }

    public void setActpending(Integer actpending) {
        this.actpending = actpending;
    }

    public Integer getLoadpending() {
        return loadpending;
    }

    public void setLoadpending(Integer loadpending) {
        this.loadpending = loadpending;
    }

    public Integer getTotactivated() {
        return totactivated;
    }

    public void setTotactivated(Integer totactivated) {
        this.totactivated = totactivated;
    }

    public Integer getTotdisp() {
        return totdisp;
    }

    public void setTotdisp(Integer totdisp) {
        this.totdisp = totdisp;
    }
}
