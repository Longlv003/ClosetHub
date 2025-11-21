package com.example.closethub.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closethub.R;

import java.util.ArrayList;
import java.util.List;

public class ColorProductAdapter extends RecyclerView.Adapter<ColorProductAdapter.ViewHolder> {
    private List<String> colorList;
    private List<String> validColors;
    private int selectedIndex = -1;
    private OnColorClickListener listener;

    public interface OnColorClickListener {
        void onColorClick(String color);
    }

    public ColorProductAdapter(List<String> colorList, OnColorClickListener listener) {
        this.colorList = colorList;
        validColors = new ArrayList<>(colorList);
        this.listener = listener;
    }

    public void updateAvailableColors(List<String> list) {
        validColors = list;
        notifyDataSetChanged();
    }

    public void setSelectedColor(String color) {
        selectedIndex = colorList.indexOf(color);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_item_color_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String color = colorList.get(position);
        int select = position;

        // >>> Bạn map tên màu → mã màu RGB <<<
        String hexColor = mapColor(color);

        GradientDrawable bg = (GradientDrawable) holder.viewColor.getBackground();
        bg.setColor(Color.parseColor(hexColor));

        // Đổi nền khi chọn
        if (position == selectedIndex) {
            holder.itemLayout.setBackgroundResource(R.drawable.bg_selected_color);
        } else {
            holder.itemLayout.setBackgroundResource(R.drawable.bg_unselected);
        }

        boolean isValid = validColors.contains(color);
        holder.itemView.setAlpha(isValid ? 1f : 0.3f);

        holder.itemView.setOnClickListener(v -> {
            selectedIndex = select;
            listener.onColorClick(color);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        View itemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView;
            viewColor = itemView.findViewById(R.id.viewColor);
        }
    }

    // 👉 Map tên màu tiếng Việt sang mã màu HEX
    private String mapColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "đỏ": return "#FF0000";
            case "đen": return "#000000";
            case "xanh": return "#007BFF";
            case "vàng": return "#FFD600";
            case "hồng": return "#FF69B4";
            case "trắng": return "#FFFFFF";
        }
        return "#CCCCCC"; // fallback
    }
}
