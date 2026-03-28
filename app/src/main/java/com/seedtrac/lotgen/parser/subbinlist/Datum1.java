package com.seedtrac.lotgen.parser.subbinlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum1 {

    @SerializedName("subbinid")
    @Expose
    private Integer subbinid;
    @SerializedName("subbinname")
    @Expose
    private String subbinname;

    public Integer getSubbinid() {
        return subbinid;
    }

    public void setSubbinid(Integer subbinid) {
        this.subbinid = subbinid;
    }

    public String getSubbinname() {
        return subbinname;
    }

    public void setSubbinname(String Subbinname) {
        this.subbinname = subbinname;
    }
}
