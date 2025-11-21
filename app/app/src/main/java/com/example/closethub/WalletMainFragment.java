package com.example.closethub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closethub.adapter.TransactionAdapter;
import com.example.closethub.models.ApiResponse;
import com.example.closethub.models.Transaction;
import com.example.closethub.models.WalletResponse;
import com.example.closethub.models.WalletTransactionRequest;
import com.example.closethub.networks.ApiService;
import com.example.closethub.networks.RetrofitClient;
import android.widget.Button;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletMainFragment extends Fragment {
    private ImageView imgToggleBalance;
    private TextView txtBalance, txtViewAll;
    private LinearLayout btnDeposit, btnWithdraw;
    private RecyclerView rcvTransactions;
    private TextView txtNoTransaction;
    
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private TransactionAdapter transactionAdapter;
    private ArrayList<Transaction> transactionArrayList;
    private boolean isBalanceVisible = true;
    private double currentBalance = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_main, container, false);
        
        initUI(view);
        setupRecyclerView();
        
        apiService = RetrofitClient.getApiService();
        sharedPreferences = getContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        imgToggleBalance.setOnClickListener(v -> toggleBalanceVisibility());
        btnDeposit.setOnClickListener(v -> showTransactionDialog("deposit", "Nạp tiền"));
        btnWithdraw.setOnClickListener(v -> showTransactionDialog("withdraw", "Chuyển tiền"));
        txtViewAll.setOnClickListener(v -> {
            // Chuyển sang tab "Lịch sử" (index 1)
            if (getActivity() instanceof WalletActivity) {
                ((WalletActivity) getActivity()).switchToHistoryTab();
            }
        });

        loadWalletInfo();
        
        return view;
    }

    private void initUI(View view) {
        imgToggleBalance = view.findViewById(R.id.imgToggleBalance);
        txtBalance = view.findViewById(R.id.txtBalance);
        txtViewAll = view.findViewById(R.id.txtViewAll);
        btnDeposit = view.findViewById(R.id.btnDeposit);
        btnWithdraw = view.findViewById(R.id.btnWithdraw);
        rcvTransactions = view.findViewById(R.id.rcvTransactions);
        txtNoTransaction = view.findViewById(R.id.txtNoTransaction);
    }

    private void setupRecyclerView() {
        transactionArrayList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(getContext(), transactionArrayList);
        rcvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvTransactions.setAdapter(transactionAdapter);
    }

    private void toggleBalanceVisibility() {
        isBalanceVisible = !isBalanceVisible;
        if (isBalanceVisible) {
            imgToggleBalance.setImageResource(R.drawable.ic_eye_off);
            updateBalanceDisplay(currentBalance);
        } else {
            imgToggleBalance.setImageResource(R.drawable.ic_eye);
            txtBalance.setText("••••••");
        }
    }

    private void loadWalletInfo() {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getWalletInfo("Bearer " + token).enqueue(new Callback<ApiResponse<WalletResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<WalletResponse>> call, Response<ApiResponse<WalletResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WalletResponse walletInfo = response.body().getData();
                    if (walletInfo != null) {
                        updateBalanceDisplay(walletInfo.getBalance());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<WalletResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        loadRecentTransactions();
    }

    private void loadRecentTransactions() {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) return;

        apiService.getWalletHistory("Bearer " + token).enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Transaction> transactions = response.body().getData();
                    if (transactions != null && !transactions.isEmpty()) {
                        // Server đã trả về transactions mới nhất trước (sort by created_date: -1)
                        // Lấy 7 giao dịch mới nhất (7 cái đầu tiên)
                        int limit = Math.min(7, transactions.size());
                        transactionArrayList.clear();
                        transactionArrayList.addAll(transactions.subList(0, limit));
                        transactionAdapter.notifyDataSetChanged();
                        
                        txtNoTransaction.setVisibility(View.GONE);
                        rcvTransactions.setVisibility(View.VISIBLE);
                    } else {
                        txtNoTransaction.setVisibility(View.VISIBLE);
                        rcvTransactions.setVisibility(View.GONE);
                    }
                } else {
                    txtNoTransaction.setVisibility(View.VISIBLE);
                    rcvTransactions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Transaction>>> call, Throwable t) {
                txtNoTransaction.setVisibility(View.VISIBLE);
                rcvTransactions.setVisibility(View.GONE);
            }
        });
    }

    private void updateBalanceDisplay(double balance) {
        currentBalance = balance;
        if (isBalanceVisible) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedBalance = formatter.format(balance) + "₫";
            txtBalance.setText(formattedBalance);
        } else {
            txtBalance.setText("••••••");
        }
    }

    private void showTransactionDialog(String type, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_wallet_transaction, null);
        builder.setView(dialogView);

        TextView txtDialogTitle = dialogView.findViewById(R.id.txtDialogTitle);
        TextView txtDialogDesc = dialogView.findViewById(R.id.txtDialogDesc);
        TextInputEditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        TextInputEditText edtPin = dialogView.findViewById(R.id.edtPin);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        txtDialogTitle.setText(title);
        if (type.equals("withdraw")) {
            txtDialogDesc.setText("Nhập số tiền và mã PIN để chuyển tiền");
        }

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString().trim();
            String pin = edtPin.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(getContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pin.isEmpty() || pin.length() != 6 || !pin.matches("\\d+")) {
                Toast.makeText(getContext(), "Mã PIN phải là 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }

            if (type.equals("deposit")) {
                performDeposit(amount, pin, dialog);
            } else {
                performWithdraw(amount, pin, dialog);
            }
        });

        dialog.show();
    }

    private void performDeposit(double amount, String pin, AlertDialog dialog) {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        WalletTransactionRequest request = new WalletTransactionRequest(amount, pin);
        apiService.depositWallet("Bearer " + token, request).enqueue(new Callback<ApiResponse<WalletResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<WalletResponse>> call, Response<ApiResponse<WalletResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WalletResponse walletResponse = response.body().getData();
                    if (walletResponse != null) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
                        // Reload wallet info để đảm bảo có số dư mới nhất từ server
                        loadWalletInfo();
                    }
                } else {
                    String errorMsg = "Nạp tiền thất bại";
                    if (response.body() != null && response.body().getMsg() != null) {
                        errorMsg = response.body().getMsg();
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<WalletResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performWithdraw(double amount, String pin, AlertDialog dialog) {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        WalletTransactionRequest request = new WalletTransactionRequest(amount, pin);
        apiService.withdrawWallet("Bearer " + token, request).enqueue(new Callback<ApiResponse<WalletResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<WalletResponse>> call, Response<ApiResponse<WalletResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WalletResponse walletResponse = response.body().getData();
                    if (walletResponse != null) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Chuyển tiền thành công!", Toast.LENGTH_SHORT).show();
                        // Reload wallet info để đảm bảo có số dư mới nhất từ server
                        loadWalletInfo();
                    }
                } else {
                    String errorMsg = "Chuyển tiền thất bại";
                    if (response.body() != null && response.body().getMsg() != null) {
                        errorMsg = response.body().getMsg();
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<WalletResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWalletInfo();
    }
}

