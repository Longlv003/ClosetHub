package com.example.closethub.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closethub.R;
import com.example.closethub.models.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private Context context;
    private ArrayList<Transaction> transactionArrayList;

    public TransactionAdapter(Context context, ArrayList<Transaction> transactionArrayList) {
        this.context = context;
        this.transactionArrayList = transactionArrayList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionArrayList.get(position);
        
        // Set type and icon
        if (transaction.getType() != null) {
            if (transaction.getType().equals("deposit")) {
                holder.txtType.setText(transaction.getDescription() != null && !transaction.getDescription().isEmpty() 
                    ? transaction.getDescription() : "Nạp tiền");
                holder.imgIcon.setImageResource(R.drawable.ic_arrow_up_green);
                holder.imgIcon.setRotation(45);
                holder.layoutIcon.setBackgroundResource(R.drawable.bg_icon_green);
                holder.txtAmount.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
            } else if (transaction.getType().equals("withdraw")) {
                holder.txtType.setText(transaction.getDescription() != null && !transaction.getDescription().isEmpty() 
                    ? transaction.getDescription() : "Rút tiền");
                holder.imgIcon.setImageResource(R.drawable.ic_arrow_up_red);
                holder.imgIcon.setRotation(45);
                holder.layoutIcon.setBackgroundResource(R.drawable.bg_icon_red);
                holder.txtAmount.setTextColor(Color.parseColor("#212121"));
            } else {
                holder.txtType.setText(transaction.getDescription() != null && !transaction.getDescription().isEmpty() 
                    ? transaction.getDescription() : transaction.getType());
            }
        }

        // Set amount
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(transaction.getAmount());
        
        if (transaction.getType() != null && transaction.getType().equals("deposit")) {
            holder.txtAmount.setText("+" + formattedAmount + "₫");
        } else {
            holder.txtAmount.setText("-" + formattedAmount + "₫");
        }

        // Set date
        if (transaction.getCreated_date() != null && !transaction.getCreated_date().isEmpty()) {
            try {
                String dateStr = transaction.getCreated_date();
                Date date = null;
                
                // Thử parse ISO format
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    date = sdf.parse(dateStr);
                } catch (Exception e1) {
                    // Thử parse format khác (không có milliseconds)
                    try {
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        date = sdf2.parse(dateStr);
                    } catch (Exception e2) {
                        // Thử parse format không có Z
                        try {
                            SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                            date = sdf3.parse(dateStr);
                        } catch (Exception e3) {
                            // Thử parse date format đơn giản
                            try {
                                SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                date = sdf4.parse(dateStr);
                            } catch (Exception e4) {
                                // Không parse được, hiển thị nguyên bản
                                holder.txtDate.setText(dateStr);
                                return;
                            }
                        }
                    }
                }
                
                if (date != null) {
                    Date now = new Date();
                    long diffInMillis = now.getTime() - date.getTime();
                    long diffInHours = diffInMillis / (1000 * 60 * 60);
                    long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
                    
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'Th'MM, yyyy", Locale.getDefault());
                    
                    if (diffInDays == 0) {
                        // Hôm nay
                        holder.txtDate.setText("Hôm nay, " + timeFormat.format(date));
                    } else if (diffInDays == 1) {
                        // Hôm qua
                        holder.txtDate.setText("Hôm qua, " + timeFormat.format(date));
                    } else {
                        // Ngày khác
                        holder.txtDate.setText(dateFormat.format(date));
                    }
                } else {
                    holder.txtDate.setText(dateStr);
                }
            } catch (Exception e) {
                // Nếu parse lỗi, hiển thị nguyên bản
                holder.txtDate.setText(transaction.getCreated_date());
            }
        } else {
            holder.txtDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return transactionArrayList != null ? transactionArrayList.size() : 0;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutIcon;
        ImageView imgIcon;
        TextView txtType;
        TextView txtDate;
        TextView txtAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutIcon = itemView.findViewById(R.id.layoutIcon);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtType = itemView.findViewById(R.id.txtType);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtAmount = itemView.findViewById(R.id.txtAmount);
        }
    }
}

