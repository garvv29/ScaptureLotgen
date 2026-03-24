package com.seedtrac.lotgen.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.activity.BagsActivationScanningActivity;
import com.seedtrac.lotgen.activity.PrintBagsLabelActivity;
import com.seedtrac.lotgen.parser.activationtrlist.Data;

import java.util.List;

public class ActivationPendingListAdapter extends RecyclerView.Adapter<ActivationPendingListAdapter.ViewHolder>{
    Context context;
    private List<Data> transactionList;
    //private ActivationPendingListAdapter.OnItemActionListener listener;

    // Interface to communicate clicks back to Activity/Fragment


    public ActivationPendingListAdapter(Context context, List<Data> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        //this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activation_pending_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Data transaction = transactionList.get(position);

        holder.tvLotNo.setText(transaction.getLotno());
        holder.tvQty.setText(transaction.getQty().toString());
        holder.tvBags.setText(transaction.getBags().toString());
        holder.tvTransaction.setText(transaction.getTrid().toString());
        holder.tvDate.setText(transaction.getTrdate());
        //holder.tvStatus.setText("GOT: " + transaction.getGotStatus() + " | Moisture: " + transaction.getMoisture());
        holder.btnEdit.setOnClickListener(v -> {
            // Check trantype and route accordingly
            if (transaction.getTrantype() != null && transaction.getTrantype().equalsIgnoreCase("Roll")) {
                // Roll type goes to PrintBagsLabelActivity
                Intent intent = new Intent(context, PrintBagsLabelActivity.class);
                intent.putExtra("lotNumber", transaction.getLotno());
                intent.putExtra("sourceActivity", "ActivationPendingListActivity");
                context.startActivity(intent);
            } else {
                // Other types go to BagsActivationScanningActivity
                Intent intent = new Intent(context, BagsActivationScanningActivity.class);
                intent.putExtra("lotNumber", transaction.getLotno());
                // Pass isPreprinted flag - true if trantype is NOT "Roll"
                boolean isPreprinted = transaction.getTrantype() != null && 
                                     !transaction.getTrantype().equalsIgnoreCase("Roll");
                intent.putExtra("isPreprinted", isPreprinted);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public interface ActivationPendingListListener {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLotNo, tvBags, tvQty, tvTransaction, tvDate;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLotNo = itemView.findViewById(R.id.tvLotNo);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvBags = itemView.findViewById(R.id.tvBags);
            tvQty = itemView.findViewById(R.id.tvQty);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            tvTransaction = itemView.findViewById(R.id.tvTransaction);
        }
    }
}
