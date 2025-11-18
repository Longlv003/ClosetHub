package com.example.closethub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closethub.R;
import com.example.closethub.models.Bill;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {
    private Context context;
    private ArrayList<Bill> billArrayList;

    public BillAdapter(Context context, ArrayList<Bill> billArrayList) {
        this.context = context;
        this.billArrayList = billArrayList;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BillViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_bill, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill b = billArrayList.get(position);
        holder.txtIdBill.setText("Mã đơn: " + b.getBill_id());
        
        // Parse date
        String dateStr = b.getCreated_date();
        if (dateStr != null && dateStr.contains("T")) {
            dateStr = dateStr.split("T")[0];
        }
        holder.txtDate.setText("Ngày: " + dateStr);
        
        // Parse address để hiển thị đẹp hơn
        String displayAddress = b.getAddress();
        if (displayAddress != null && displayAddress.contains("|")) {
            String[] parts = displayAddress.split("\\|");
            displayAddress = parts[0].trim(); // Chỉ lấy phần địa chỉ thực
        }
        holder.txtAddress.setText("Địa chỉ: " + displayAddress);

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        // Hiển thị tổng tiền với thông tin chi tiết
        String totalText = "Tổng: " + formatter.format(b.getTotal_amount()) + " ₫";
        
        // Nếu có subtotal và shipping_fee, hiển thị chi tiết
        if (b.getSubtotal() > 0 || b.getShipping_fee() > 0) {
            totalText += "\n(Tạm tính: " + formatter.format(b.getSubtotal()) + " ₫";
            if (b.getShipping_fee() > 0) {
                totalText += " + Phí vận chuyển: " + formatter.format(b.getShipping_fee()) + " ₫";
            }
            totalText += ")";
        }
        
        holder.txtTotal.setText(totalText);

        // set RecyclerView con
        ProductBillAdapter billAdapter = new ProductBillAdapter(context, new ArrayList<>(b.getProducts()));
        holder.rcvProducts.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rcvProducts.setAdapter(billAdapter);
    }

    @Override
    public int getItemCount() {
        return billArrayList.size();
    }

    public class BillViewHolder extends RecyclerView.ViewHolder {
        TextView txtIdBill, txtDate, txtAddress, txtTotal;
        RecyclerView rcvProducts;
        public BillViewHolder(@NonNull View itemView) {
            super(itemView);

            txtIdBill = itemView.findViewById(R.id.txtIdBill);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            rcvProducts = itemView.findViewById(R.id.rcvProducts);
        }
    }
}
