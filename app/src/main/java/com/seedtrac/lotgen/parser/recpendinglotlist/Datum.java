package com.seedtrac.lotgen.parser.recpendinglotlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {
    @SerializedName("trid")
    @Expose
    private Integer trid;
    @SerializedName("trdate")
    @Expose
    private String trdate;
    @SerializedName("rowid")
    @Expose
    private Integer rowid;
    @SerializedName("lotno")
    @Expose
    private String lotno;
    @SerializedName("nob")
    @Expose
    private Integer nob;
    @SerializedName("harvestdate")
    @Expose
    private String harvestdate;
    @SerializedName("whname")
    @Expose
    private String whname;
    @SerializedName("binname")
    @Expose
    private String binname;
    @SerializedName("whid")
    @Expose
    private Integer whid;
    @SerializedName("binid")
    @Expose
    private Integer binid;
    @SerializedName("trantype")
    @Expose
    private String trantype;
    @SerializedName("acttrid")
    @Expose
    private Integer acttrid;

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

    public Integer getRowid() {
        return rowid;
    }

    public void setRowid(Integer rowid) {
        this.rowid = rowid;
    }

    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
    }

    public Integer getNob() {
        return nob;
    }

    public void setNob(Integer nob) {
        this.nob = nob;
    }

    public String getHarvestdate() {
        return harvestdate;
    }

    public void setHarvestdate(String harvestdate) {
        this.harvestdate = harvestdate;
    }

    public String getWhname() {
        return whname;
    }

    public void setWhname(String whname) {
        this.whname = whname;
    }

    public String getBinname() {
        return binname;
    }

    public void setBinname(String binname) {
        this.binname = binname;
    }

    public Integer getWhid() {
        return whid;
    }

    public void setWhid(Integer whid) {
        this.whid = whid;
    }

    public Integer getBinid() {
        return binid;
    }

    public void setBinid(Integer binid) {
        this.binid = binid;
    }

    public String getTrantype() {
        return trantype;
    }

    public void setTrantype(String trantype) {
        this.trantype = trantype;
    }

    public Integer getActtrid() {
        return acttrid;
    }

    public void setActtrid(Integer acttrid) {
        this.acttrid = acttrid;
    }
}
