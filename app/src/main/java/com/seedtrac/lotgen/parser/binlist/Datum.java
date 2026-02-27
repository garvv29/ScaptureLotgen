package com.seedtrac.lotgen.parser.binlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {

    @SerializedName("binid")
    @Expose
    private Integer binid;
    @SerializedName("binname")
    @Expose
    private String binname;

    public Integer getBinid() {
        return binid;
    }

    public void setBinid(Integer binid) {
        this.binid = binid;
    }

    public String getBinname() {
        return binname;
    }

    public void setBinname(String binname) {
        this.binname = binname;
    }
}
