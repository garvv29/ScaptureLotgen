package com.seedtrac.lotgen.parser.labelprintingbarcodelist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {
    @SerializedName("rowid")
    @Expose
    private Integer rowid;
    @SerializedName("lotno")
    @Expose
    private String lotno;
    @SerializedName("qrcode")
    @Expose
    private String qrcode;
    @SerializedName("weight")
    @Expose
    private String weight;

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

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
