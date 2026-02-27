package com.seedtrac.lotgen.parser.pendinglotreport;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {
    @SerializedName("lotno")
    @Expose
    private String lotno;
    @SerializedName("nob")
    @Expose
    private String nob;
    @SerializedName("crop")
    @Expose
    private String crop;
    @SerializedName("spcodef")
    @Expose
    private String spcodef;
    @SerializedName("spcodem")
    @Expose
    private String spcodem;

    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
    }

    public String getNob() {
        return nob;
    }

    public void setNob(String nob) {
        this.nob = nob;
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
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
}
