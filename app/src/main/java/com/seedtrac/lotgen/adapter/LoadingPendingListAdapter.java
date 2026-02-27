package com.seedtrac.lotgen.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.activity.LoadingScanningActivity;
import com.seedtrac.lotgen.model.LoadingTrList;
import com.seedtrac.lotgen.parser.loadingtrpendinglist.Data;

import java.util.List;

public class LoadingPendingListAdapter extends RecyclerView.Adapter<LoadingPendingListAdapter.ViewHolder> {
    private Context context;
    private List<Data> transactionList;
    private OnItemActionListener listener;

    // Interface to communicate clicks back to Activity/Fragment
    public interface OnItemActionListener {
        void onEditClicked(Data transaction, int position);
        void onDeleteClicked(int position);
    }

    public LoadingPendingListAdapter(Context context, List<Data> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.loading_pending_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Data transaction = transactionList.get(position);

        // Set data to views
        holder.tvTransportName.setText(transaction.getTrname());
        holder.tvVehicleNumber.setText(transaction.getVehno());
        holder.tvLRNumber.setText(transaction.getLrno());
        holder.tvDriverName.setText(transaction.getDrivername());
        holder.tvMobile.setText(transaction.getDriverno());
        //holder.tvDestination.setText(transaction.getDestination());
        holder.tvDispatchDate.setText(transaction.getDispdate());

        // Handle clicks
        /*holder.btnEdit.setOnClickListener(v ->
                listener.onEditClicked(transaction, position));*/

        holder.btnEdit.setOnClickListener(v -> {
            Toast.makeText(context, transaction.getTrid().toString(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, LoadingScanningActivity.class);
            intent.putExtra("trid", transaction.getTrid().toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransportName, tvVehicleNumber, tvLRNumber, tvDriverName, tvMobile, tvDestination, tvDispatchDate;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransportName = itemView.findViewById(R.id.tvTransportName);
            tvVehicleNumber = itemView.findViewById(R.id.tvVehicleNumber);
            tvLRNumber = itemView.findViewById(R.id.tvLrNumber);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDispatchDate = itemView.findViewById(R.id.tvDispatchDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Remove an item from list
    public void removeItem(int position) {
        transactionList.remove(position);
        notifyItemRemoved(position);
    }
}
