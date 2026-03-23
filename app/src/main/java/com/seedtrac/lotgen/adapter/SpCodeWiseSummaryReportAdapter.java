package com.seedtrac.lotgen.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.R;

import java.util.List;

public class SpCodeWiseSummaryReportAdapter extends RecyclerView.Adapter<SpCodeWiseSummaryReportAdapter.ViewHolder> {

    private List<SpCodeWiseSummaryData> reportDataList;

    public SpCodeWiseSummaryReportAdapter(List<SpCodeWiseSummaryData> reportDataList) {
        this.reportDataList = reportDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpCodeWiseSummaryData data = reportDataList.get(position);
        holder.bind(data, position + 1);
    }

    @Override
    public int getItemCount() {
        return reportDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrNo, tvCrop, tvSpCode, tvQty, tvBags, tvLotNo;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.spcode_wise_summary_report_item, parent, false));
            tvSrNo = itemView.findViewById(R.id.tvSrNo);
            tvCrop = itemView.findViewById(R.id.tvCrop);
            tvSpCode = itemView.findViewById(R.id.tvSpCode);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvBags = itemView.findViewById(R.id.tvBags);
            tvLotNo = itemView.findViewById(R.id.tvLotNo);
        }

        @SuppressLint("SetTextI18n")
        public void bind(SpCodeWiseSummaryData data, int srNo) {
            tvSrNo.setText(String.valueOf(srNo));
            tvCrop.setText(data.getCropname() != null ? data.getCropname() : "");
            tvSpCode.setText((data.getSpcodef() != null ? data.getSpcodef() : "") + " X " + (data.getSpcodem() != null ? data.getSpcodem() : ""));
            tvQty.setText(String.format("%.3f", data.getQty()));
            tvBags.setText(String.valueOf(data.getBags()));
            tvLotNo.setText(data.getLotno() != null ? data.getLotno() : "");
        }
    }

    // Inner class for data model
    public static class SpCodeWiseSummaryData {
        private String cropname;
        private String spcodef;
        private String spcodem;
        private Double qty;
        private Integer bags;
        private String lotno;

        public SpCodeWiseSummaryData(String cropname, String spcodef, String spcodem, Double qty, Integer bags, String lotno) {
            this.cropname = cropname;
            this.spcodef = spcodef;
            this.spcodem = spcodem;
            this.qty = qty;
            this.bags = bags;
            this.lotno = lotno;
        }

        // Getters
        public String getCropname() { return cropname; }
        public String getSpcodef() { return spcodef; }
        public String getSpcodem() { return spcodem; }
        public Double getQty() { return qty; }
        public Integer getBags() { return bags; }
        public String getLotno() { return lotno; }

        // Setters
        public void setCropname(String cropname) { this.cropname = cropname; }
        public void setSpcodef(String spcodef) { this.spcodef = spcodef; }
        public void setSpcodem(String spcodem) { this.spcodem = spcodem; }
        public void setQty(Double qty) { this.qty = qty; }
        public void setBags(Integer bags) { this.bags = bags; }
        public void setLotno(String lotno) { this.lotno = lotno; }
    }
}
