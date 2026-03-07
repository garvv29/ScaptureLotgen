package com.seedtrac.lotgen.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.parser.actbarcodelist.Data;
import com.seedtrac.lotgen.parser.labelprintingbarcodelist.Datum;

import java.util.List;

public class LabelPrintingBarcodeListAdapter extends RecyclerView.Adapter<LabelPrintingBarcodeListAdapter.BagViewHolder> {
    private List<Datum> barcodeList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Datum datum, int position);
    }

    public LabelPrintingBarcodeListAdapter(List<Datum> barcodeList) {
        this.barcodeList = barcodeList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public BagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scannedbags_list, parent, false);
        return new BagViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull BagViewHolder holder, int position) {
        Datum barcodeData = barcodeList.get(position);
        holder.tvSrno.setText(String.valueOf(position+1));
        holder.tvBarcode.setText(barcodeData.getQrcode());
        //holder.tvDate.setText(barcodeData.getWeight());
        double value = Double.parseDouble(barcodeData.getWeight());
        holder.tvDate.setText(String.format("%.3f", value));
        
        // Add click listener to show reprint dialog
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(barcodeData, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return barcodeList.size();
    }

    static class BagViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrno, tvBarcode, tvDate;;
        BagViewHolder(View itemView) {
            super(itemView);
            tvSrno = itemView.findViewById(R.id.tvSrno);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
