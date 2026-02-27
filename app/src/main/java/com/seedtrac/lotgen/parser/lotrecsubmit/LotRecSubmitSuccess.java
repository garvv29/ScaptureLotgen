package com.seedtrac.lotgen.parser.lotrecsubmit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LotRecSubmitSuccess {
    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("trid")
    @Expose
    private String trid;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTrid() {
        return trid;
    }

    public void setTrid(String trid) {
        this.trid = trid;
    }
}
