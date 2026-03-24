package com.seedtrac.lotgen.parser.spcodewisesummary;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpCodeWiseSummaryResponse {

    @SerializedName("status")
    private Boolean status;

    @SerializedName("msg")
    private String msg;

    @SerializedName("data")
    private List<SpCodeWiseSummaryData> data;

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

    public List<SpCodeWiseSummaryData> getData() {
        return data;
    }

    public void setData(List<SpCodeWiseSummaryData> data) {
        this.data = data;
    }

    public static class SpCodeWiseSummaryData {

        @SerializedName("crop")
        private String crop;

        @SerializedName("spcodef")
        private String spcodef;

        @SerializedName("spcodem")
        private String spcodem;

        @SerializedName("Bags")
        private String bags;

        @SerializedName("Qty")
        private Double qty;

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

        public String getBags() {
            return bags;
        }

        public void setBags(String bags) {
            this.bags = bags;
        }

        public Double getQty() {
            return qty;
        }

        public void setQty(Double qty) {
            this.qty = qty;
        }
    }
}
