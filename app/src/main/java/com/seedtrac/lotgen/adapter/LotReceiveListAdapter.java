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
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.activity.BagsActivationScanningActivity;
import com.seedtrac.lotgen.activity.BagsActivationSetupActivityPrintRoll;
import com.seedtrac.lotgen.activity.LotReceiveActivity;
import com.seedtrac.lotgen.activity.LotReceiveListActivity;
import com.seedtrac.lotgen.activity.PrintBagsLabelActivity;
import com.seedtrac.lotgen.parser.activationtrlist.Data;
import com.seedtrac.lotgen.parser.recpendinglotlist.Datum;

import java.util.List;

public class LotReceiveListAdapter extends RecyclerView.Adapter<LotReceiveListAdapter.ViewHolder>{
    Context context;
    private List<Datum> transactionList;
    //private ActivationPendingListAdapter.OnItemActionListener listener;

    // Interface to communicate clicks back to Activity/Fragment


    public LotReceiveListAdapter(Context context, List<Datum> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        //this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lotreceive_pendinglist, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Datum transaction = transactionList.get(position);

        holder.tvLotNo.setText(transaction.getLotno());
        holder.tvBags.setText(transaction.getNob().toString());
        holder.tvHarvestDate.setText(transaction.getHarvestdate());
        holder.tvDate.setText(transaction.getTrdate());
        holder.tvSLOC.setText(transaction.getWhname()+"/"+transaction.getBinname());
        holder.btnEdit.setOnClickListener(v -> {
            if (transaction.getTrantype().equalsIgnoreCase("Roll")){
                // Check if this lot has already been activated
                String activationKey = "lot_activated_" + transaction.getLotno();
                Object activationStatus = SharedPreferences.getInstance(context).getObject(activationKey, String.class);
                boolean isActivated = activationStatus != null && activationStatus.toString().equals("true");
                
                if (transaction.getActtrid()>0) {
                    // Already activated, go directly to print
                    Intent intent = new Intent(context, PrintBagsLabelActivity.class);
                    intent.putExtra("lotNumber", transaction.getLotno());
                    intent.putExtra("harvestdate", transaction.getHarvestdate());
                    intent.putExtra("bagcount", transaction.getNob());
                    intent.putExtra("trid", transaction.getActtrid());
                    intent.putExtra("sourceActivity", "LotReceiveListActivity");
                    context.startActivity(intent);
                } else {
                    // Not yet activated, go to setup form
                    Intent intent = new Intent(context, BagsActivationSetupActivityPrintRoll.class);
                    intent.putExtra("lotNumber", transaction.getLotno());
                    intent.putExtra("harvestdate", transaction.getHarvestdate());
                    intent.putExtra("bagcount", transaction.getNob());
                    intent.putExtra("sourceActivity", "LotReceiveListActivity");
                    context.startActivity(intent);
                }
            }else {
                /*Intent intent = new Intent(context, LotReceiveActivity.class);
                intent.putExtra("lotNumber", transaction.getLotno());
                intent.putExtra("harvestdate", transaction.getHarvestdate());
                intent.putExtra("bagcount", transaction.getNob());
                intent.putExtra("whname", transaction.getWhname());
                intent.putExtra("binname", transaction.getBinname());
                intent.putExtra("trid", transaction.getTrid());
                intent.putExtra("whid", transaction.getWhid());
                intent.putExtra("binid", transaction.getBinid());
                intent.putExtra("rowid", transaction.getRowid());
                intent.putExtra("tagType", transaction.getTrantype());
                context.startActivity(intent);*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLotNo, tvBags, tvQty, tvHarvestDate, tvDate, tvSLOC;
        ImageButton btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLotNo = itemView.findViewById(R.id.tvLotNo);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvBags = itemView.findViewById(R.id.tvBags);
            tvQty = itemView.findViewById(R.id.tvQty);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            tvHarvestDate = itemView.findViewById(R.id.tvHarvestDate);
            tvSLOC = itemView.findViewById(R.id.tvSLOC);
        }
    }
}
