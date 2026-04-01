package com.seedtrac.lotgen.parser.gssubbinlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GsSubBinData {

    @SerializedName("binid")
    @Expose
    private Integer binid;
    @SerializedName("outertype")
    @Expose
    private String outertype;
    @SerializedName("outercontainer")
    @Expose
    private String outercontainer;

    public Integer getBinid() {
        return binid;
    }

    public void setBinid(Integer binid) {
        this.binid = binid;
    }

    public String getOutertype() {
        return outertype;
    }

    public void setOutertype(String outertype) {
        this.outertype = outertype;
    }

    public String getOutercontainer() {
        return outercontainer;
    }

    public void setOutercontainer(String outercontainer) {
        this.outercontainer = outercontainer;
    }
}
