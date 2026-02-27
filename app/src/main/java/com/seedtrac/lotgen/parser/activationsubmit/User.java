package com.seedtrac.lotgen.parser.activationsubmit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("trid")
    @Expose
    private Integer trid;
    @SerializedName("scode")
    @Expose
    private String scode;

    public Integer getTrid() {
        return trid;
    }

    public void setTrid(Integer trid) {
        this.trid = trid;
    }

    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }
}
