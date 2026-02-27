package com.seedtrac.lotgen.parser.loadingtrpendinglist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("trid")
    @Expose
    private Integer trid;
    @SerializedName("trname")
    @Expose
    private String trname;
    @SerializedName("lrno")
    @Expose
    private String lrno;
    @SerializedName("vehno")
    @Expose
    private String vehno;
    @SerializedName("drivername")
    @Expose
    private String drivername;
    @SerializedName("driverno")
    @Expose
    private String driverno;
    @SerializedName("dispdate")
    @Expose
    private String dispdate;

    public Integer getTrid() {
        return trid;
    }

    public void setTrid(Integer trid) {
        this.trid = trid;
    }

    public String getTrname() {
        return trname;
    }

    public void setTrname(String trname) {
        this.trname = trname;
    }

    public String getLrno() {
        return lrno;
    }

    public void setLrno(String lrno) {
        this.lrno = lrno;
    }

    public String getVehno() {
        return vehno;
    }

    public void setVehno(String vehno) {
        this.vehno = vehno;
    }

    public String getDrivername() {
        return drivername;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    public String getDriverno() {
        return driverno;
    }

    public void setDriverno(String driverno) {
        this.driverno = driverno;
    }

    public String getDispdate() {
        return dispdate;
    }

    public void setDispdate(String dispdate) {
        this.dispdate = dispdate;
    }
}
