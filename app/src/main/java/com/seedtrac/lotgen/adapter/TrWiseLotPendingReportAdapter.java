package com.seedtrac.lotgen.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.parser.pendinglotreport.Datum;

import java.util.List;

public class TrWiseLotPendingReportAdapter extends RecyclerView.Adapter<TrWiseLotPendingReportAdapter.BagViewHolder>{
    private List<Datum> barcodeList;

    public TrWiseLotPendingReportAdapter(List<Datum> barcodeList) {
        this.barcodeList = barcodeList;
    }

    @NonNull
    @Override
    public TrWiseLotPendingReportAdapter.BagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pendinglotlist_report, parent, false);
        return new TrWiseLotPendingReportAdapter.BagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrWiseLotPendingReportAdapter.BagViewHolder holder, int position) {
        Datum barcodeData = barcodeList.get(position);
        holder.tvSrno.setText(String.valueOf(position+1));
        holder.tvLotNo.setText(barcodeData.getLotno());
        holder.tvCrop.setText(barcodeData.getCrop());
        holder.tvBags.setText(barcodeData.getNob());
    }

    @Override
    public int getItemCount() {
        return barcodeList.size();
    }

    static class BagViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrno, tvLotNo, tvCrop, tvBags, tvSLOC;
        BagViewHolder(View itemView) {
            super(itemView);
            tvSrno = itemView.findViewById(R.id.tvSrno);
            tvLotNo = itemView.findViewById(R.id.tvLotNo);
            tvCrop = itemView.findViewById(R.id.tvCrop);
            tvBags = itemView.findViewById(R.id.tvBags);
            tvSLOC = itemView.findViewById(R.id.tvSLOC);
        }
    }
}
