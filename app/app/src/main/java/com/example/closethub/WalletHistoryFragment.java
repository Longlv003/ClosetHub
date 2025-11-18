package com.example.closethub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.closethub.adapter.TransactionAdapter;
import com.example.closethub.models.ApiResponse;
import com.example.closethub.models.Transaction;
import com.example.closethub.networks.ApiService;
import com.example.closethub.networks.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletHistoryFragment extends Fragment {
    private Button btnAll, btnDeposit, btnWithdraw;
    private RecyclerView rcvTransactions;
    private TextView txtNoTransaction;
    
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private TransactionAdapter transactionAdapter;
    private ArrayList<Transaction> transactionArrayList;
    private ArrayList<Transaction> allTransactions;
    private String currentFilter = "all";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_history, container, false);
        
        initUI(view);
        setupRecyclerView();
        
        apiService = RetrofitClient.getApiService();
        sharedPreferences = getContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        btnAll.setOnClickListener(v -> {
            filterTransactions("all");
            updateTabButtons("all");
        });

        btnDeposit.setOnClickListener(v -> {
            filterTransactions("deposit");
            updateTabButtons("deposit");
        });

        btnWithdraw.setOnClickListener(v -> {
            filterTransactions("withdraw");
            updateTabButtons("withdraw");
        });

        // Set tab "Tất cả" là tab mặc định
        updateTabButtons("all");
        
        loadAllTransactions();
        
        return view;
    }

    private void initUI(View view) {
        btnAll = view.findViewById(R.id.btnAll);
        btnDeposit = view.findViewById(R.id.btnDeposit);
        btnWithdraw = view.findViewById(R.id.btnWithdraw);
        rcvTransactions = view.findViewById(R.id.rcvTransactions);
        txtNoTransaction = view.findViewById(R.id.txtNoTransaction);
    }

    private void setupRecyclerView() {
        transactionArrayList = new ArrayList<>();
        allTransactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(getContext(), transactionArrayList);
        rcvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvTransactions.setAdapter(transactionAdapter);
    }

    private void loadAllTransactions() {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getWalletHistory("Bearer " + token).enqueue(new Callback<ApiResponse<List<Transaction>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Transaction>>> call, Response<ApiResponse<List<Transaction>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Transaction> transactions = response.body().getData();
                    if (transactions != null && !transactions.isEmpty()) {
                        Collections.reverse(transactions);
                        allTransactions.clear();
                        allTransactions.addAll(transactions);
                        filterTransactions(currentFilter);
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
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTransactions(String filter) {
        currentFilter = filter;
        transactionArrayList.clear();

        if (allTransactions.isEmpty()) {
            txtNoTransaction.setVisibility(View.VISIBLE);
            rcvTransactions.setVisibility(View.GONE);
            return;
        }

        if (filter.equals("all")) {
            transactionArrayList.addAll(allTransactions);
        } else {
            for (Transaction transaction : allTransactions) {
                if (transaction.getType() != null && transaction.getType().equals(filter)) {
                    transactionArrayList.add(transaction);
                }
            }
        }

        if (transactionArrayList.isEmpty()) {
            txtNoTransaction.setVisibility(View.VISIBLE);
            rcvTransactions.setVisibility(View.GONE);
        } else {
            txtNoTransaction.setVisibility(View.GONE);
            rcvTransactions.setVisibility(View.VISIBLE);
        }

        transactionAdapter.notifyDataSetChanged();
    }

    private void updateTabButtons(String selected) {
        // Reset tất cả buttons về trạng thái không được chọn
        int grayColor = ContextCompat.getColor(getContext(), R.color.gray_600);
        int whiteColor = ContextCompat.getColor(getContext(), android.R.color.white);
        int primaryColor = ContextCompat.getColor(getContext(), R.color.primary_color);
        
        btnAll.setTextColor(grayColor);
        btnAll.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), android.R.color.transparent));
        btnDeposit.setTextColor(grayColor);
        btnDeposit.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), android.R.color.transparent));
        btnWithdraw.setTextColor(grayColor);
        btnWithdraw.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), android.R.color.transparent));

        // Set button được chọn
        switch (selected) {
            case "all":
                btnAll.setTextColor(whiteColor);
                btnAll.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.primary_color));
                break;
            case "deposit":
                btnDeposit.setTextColor(whiteColor);
                btnDeposit.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.primary_color));
                break;
            case "withdraw":
                btnWithdraw.setTextColor(whiteColor);
                btnWithdraw.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.primary_color));
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllTransactions();
    }
}

