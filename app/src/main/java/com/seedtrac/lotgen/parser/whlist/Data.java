package com.seedtrac.lotgen.parser.whlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("whid")
    @Expose
    private Integer whid;
    @SerializedName("whname")
    @Expose
    private String whname;

    public Integer getWhid() {
        return whid;
    }

    public void setWhid(Integer whid) {
        this.whid = whid;
    }

    public String getWhname() {
        return whname;
    }

    public void setWhname(String whname) {
        this.whname = whname;
    }

}
