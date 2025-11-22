package com.example.closethub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closethub.R;
import com.example.closethub.models.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ProductBillAdapter extends RecyclerView.Adapter<ProductBillAdapter.PBillViewHolder> {
    private Context context;
    private ArrayList<Product> productArrayList;

    public ProductBillAdapter(Context context, ArrayList<Product> productArrayList) {
        this.context = context;
        this.productArrayList = productArrayList;
    }

    @NonNull
    @Override
    public PBillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_item_product_bill, parent, false);
        return new PBillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PBillViewHolder holder, int position) {
        Product p = productArrayList.get(position);
        
        // Hiển thị name với size và color nếu có
        String nameText = p.getName();
        if (p.getSize() != null && !p.getSize().isEmpty() && 
            p.getColor() != null && !p.getColor().isEmpty()) {
            nameText += " (" + p.getSize() + ", " + p.getColor() + ")";
        } else if (p.getSize() != null && !p.getSize().isEmpty()) {
            nameText += " (" + p.getSize() + ")";
        } else if (p.getColor() != null && !p.getColor().isEmpty()) {
            nameText += " (" + p.getColor() + ")";
        }
        
        holder.txtName.setText(nameText);

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        // Ưu tiên dùng price từ bill_detail, nếu không có thì dùng min_price
        double price = (p.getPrice() > 0) ? p.getPrice() : p.getMin_price();
        int quantity = (p.getQuantity() > 0) ? p.getQuantity() : 1;
        
        // Ưu tiên dùng amount từ server, nếu không có thì tính lại
        double amount = (p.getAmount() > 0) ? p.getAmount() : (price * quantity);
        
        holder.txtPrice.setText(formatter.format(price) + " ₫");
        holder.txtQuantity.setText("x" + quantity);
        holder.txtAmount.setText(formatter.format(amount) + " ₫");
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    public class PBillViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPrice, txtQuantity, txtAmount;
        public PBillViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtAmount = itemView.findViewById(R.id.txtAmount);
        }
    }
}
