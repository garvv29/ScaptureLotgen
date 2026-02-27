package com.seedtrac.lotgen.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.parser.loadinglist.Data;

import java.util.List;

public class LoadingListAdapter extends RecyclerView.Adapter<LoadingListAdapter.ViewHolder>{
    Context context;
    private List<Data> loadinList;

    public LoadingListAdapter(Context context, List<Data> loadinList) {
        this.context = context;
        this.loadinList = loadinList;
    }

    @NonNull
    @Override
    public LoadingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scannedbags_list, parent, false);
        return new LoadingListAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LoadingListAdapter.ViewHolder holder, int position) {
        Data transaction = loadinList.get(position);

        holder.tvSrno.setText(String.valueOf(position+1));
        holder.tvBarcode.setText(transaction.getLotno());
        holder.tvDate.setText(transaction.getTotbags());
    }

    @Override
    public int getItemCount() {
        return loadinList.size();
    }

    public interface ActivationPendingListListener {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSrno, tvBarcode, tvDate;;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSrno = itemView.findViewById(R.id.tvSrno);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    public interface LoadingListListener {
        void onItemClicked(Data data);
    }
}
