package com.seedtrac.lotgen.parser.lotinfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LotInfoData {
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
    @SerializedName("spcodef")
    @Expose
    private String spcodef;
    @SerializedName("spcodem")
    @Expose
    private String spcodem;
    @SerializedName("harvestdate")
    @Expose
    private String harvestdate;
    @SerializedName("gotstatus")
    @Expose
    private String gotstatus;
    @SerializedName("moisture")
    @Expose
    private String moisture;
    @SerializedName("orgname")
    @Expose
    private String orgname;
    @SerializedName("farmername")
    @Expose
    private String farmername;
    @SerializedName("productionlocation")
    @Expose
    private String productionlocation;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("productionpersonnel")
    @Expose
    private String productionpersonnel;
    @SerializedName("cropname")
    @Expose
    private String cropname;
    @SerializedName("agmt_no")
    @Expose
    private String agmtNo;
    @SerializedName("farmer_id")
    @Expose
    private String farmerId;
    @SerializedName("remarks")
    @Expose
    private String remarks;

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

    public String getSpcodef() {
        return spcodef;
    }

    public void setSpcodef(String spcodef) {
        this.spcodef = spcodef;
    }

    public String getSpcodem() {
        return spcodem;
    }

    public void setSpcodem(String spcodem) {
        this.spcodem = spcodem;
    }

    public String getHarvestdate() {
        return harvestdate;
    }

    public void setHarvestdate(String harvestdate) {
        this.harvestdate = harvestdate;
    }

    public String getGotstatus() {
        return gotstatus;
    }

    public void setGotstatus(String gotstatus) {
        this.gotstatus = gotstatus;
    }

    public String getMoisture() {
        return moisture;
    }

    public void setMoisture(String moisture) {
        this.moisture = moisture;
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }

    public String getFarmername() {
        return farmername;
    }

    public void setFarmername(String farmername) {
        this.farmername = farmername;
    }

    public String getProductionlocation() {
        return productionlocation;
    }

    public void setProductionlocation(String productionlocation) {
        this.productionlocation = productionlocation;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProductionpersonnel() {
        return productionpersonnel;
    }

    public void setProductionpersonnel(String productionpersonnel) {
        this.productionpersonnel = productionpersonnel;
    }

    public String getCropname() {
        return cropname;
    }

    public void setCropname(String cropname) {
        this.cropname = cropname;
    }

    public String getAgmtNo() {
        return agmtNo;
    }

    public void setAgmtNo(String agmtNo) {
        this.agmtNo = agmtNo;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
