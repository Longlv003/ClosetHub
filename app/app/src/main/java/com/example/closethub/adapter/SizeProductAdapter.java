package com.example.closethub.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closethub.R;

import java.util.ArrayList;
import java.util.List;

public class SizeProductAdapter extends RecyclerView.Adapter<SizeProductAdapter.ViewHolder> {
    private List<String> sizeList;
    private List<String> validSizes;
    private int selectedIndex = -1;
    private OnSizeClickListener listener;

    public interface OnSizeClickListener {
        void onSizeClick(String size);
    }

    public SizeProductAdapter(List<String> sizeList, OnSizeClickListener listener) {
        this.sizeList = sizeList;
        validSizes = new ArrayList<>(sizeList);
        this.listener = listener;
    }

    public void updateAvailableSizes(List<String> list) {
        validSizes = list;
        notifyDataSetChanged();
    }

    public void setSelectedSize(String size) {
        selectedIndex = sizeList.indexOf(size);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_item_size_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String size = sizeList.get(position);
        int select = position;

        holder.txtSize.setText(size);

        if (position == selectedIndex) {
            holder.txtSize.setBackgroundResource(R.drawable.bg_selected);
            holder.txtSize.setTextColor(0xFFFFFFFF);
        } else {
            holder.txtSize.setBackgroundResource(R.drawable.bg_unselected);
            holder.txtSize.setTextColor(0xFF111111);
        }

        boolean isValid = validSizes.contains(size);
        holder.txtSize.setAlpha(isValid ? 1f : 0.3f);

        holder.txtSize.setOnClickListener(v -> {
            selectedIndex = select;
            listener.onSizeClick(size);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return sizeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSize;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSize = itemView.findViewById(R.id.txtSize);
        }
    }
}
