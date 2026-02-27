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

import java.util.ArrayList;
import java.util.List;

public class BagsAdapter extends RecyclerView.Adapter<BagsAdapter.BagViewHolder> {
    private List<Data> barcodeList;

    public BagsAdapter(List<Data> barcodeList) {
        this.barcodeList = barcodeList;
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
        Data barcodeData = barcodeList.get(position);
        holder.tvSrno.setText(String.valueOf(position+1));
        holder.tvBarcode.setText(barcodeData.getBarcode());
        //holder.tvDate.setText(barcodeData.getWeight());
        double value = Double.parseDouble(barcodeData.getWeight());
        holder.tvDate.setText(String.format("%.3f", value));
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
