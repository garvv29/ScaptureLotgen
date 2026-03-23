package com.seedtrac.lotgen.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.model.ScannedBarcode;

import java.util.List;

public class GsSlocBarcodeAdapter extends RecyclerView.Adapter<GsSlocBarcodeAdapter.ViewHolder> {

    private List<ScannedBarcode> scannedBarcodes;

    public GsSlocBarcodeAdapter(List<ScannedBarcode> scannedBarcodes) {
        this.scannedBarcodes = scannedBarcodes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gs_barcode, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedBarcode barcode = scannedBarcodes.get(position);
        holder.tvSrNo.setText(String.valueOf(barcode.getSrNo()));
        holder.tvBarcode.setText(barcode.getBarcode());
        holder.tvLotNo.setText(barcode.getLotNo());
    }

    @Override
    public int getItemCount() {
        return scannedBarcodes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrNo, tvBarcode, tvLotNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSrNo = itemView.findViewById(R.id.tv_sr_no);
            tvBarcode = itemView.findViewById(R.id.tv_barcode_value);
            tvLotNo = itemView.findViewById(R.id.tv_lot_no_value);
        }
    }
}
