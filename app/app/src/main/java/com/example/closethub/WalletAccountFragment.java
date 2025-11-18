package com.example.closethub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.widget.Button;
import com.example.closethub.models.ApiResponse;
import com.example.closethub.models.WalletResponse;
import com.example.closethub.networks.ApiService;
import com.example.closethub.networks.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletAccountFragment extends Fragment {
    private TextView txtWalletNumber, txtCreateDate, txtTotalDeposits, txtTotalWithdrawals, txtChangePin;
    
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_account, container, false);
        
        initUI(view);
        
        apiService = RetrofitClient.getApiService();
        sharedPreferences = getContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        txtChangePin.setOnClickListener(v -> showChangePinDialog());

        loadWalletInfo();
        
        return view;
    }

    private void initUI(View view) {
        txtWalletNumber = view.findViewById(R.id.txtWalletNumber);
        txtCreateDate = view.findViewById(R.id.txtCreateDate);
        txtTotalDeposits = view.findViewById(R.id.txtTotalDeposits);
        txtTotalWithdrawals = view.findViewById(R.id.txtTotalWithdrawals);
        txtChangePin = view.findViewById(R.id.txtChangePin);
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
                        txtWalletNumber.setText(walletInfo.getWallet_number() != null ? walletInfo.getWallet_number() : "---");
                        
                        // Format date
                        if (walletInfo.getCreate_date() != null) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                                Date date = sdf.parse(walletInfo.getCreate_date());
                                if (date != null) {
                                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    txtCreateDate.setText(displayFormat.format(date));
                                } else {
                                    txtCreateDate.setText(walletInfo.getCreate_date());
                                }
                            } catch (Exception e) {
                                txtCreateDate.setText(walletInfo.getCreate_date());
                            }
                        }

                        // Format total deposits và withdrawals
                        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                        txtTotalDeposits.setText(formatter.format(walletInfo.getTotal_deposits()) + " ₫");
                        txtTotalWithdrawals.setText(formatter.format(walletInfo.getTotal_withdrawals()) + " ₫");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<WalletResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangePinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_pin, null);
        builder.setView(dialogView);

        TextInputEditText edtOldPin = dialogView.findViewById(R.id.edtOldPin);
        TextInputEditText edtNewPin = dialogView.findViewById(R.id.edtNewPin);
        TextInputEditText edtConfirmPin = dialogView.findViewById(R.id.edtConfirmPin);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String oldPin = edtOldPin.getText().toString().trim();
            String newPin = edtNewPin.getText().toString().trim();
            String confirmPin = edtConfirmPin.getText().toString().trim();

            if (oldPin.isEmpty() || oldPin.length() != 6 || !oldPin.matches("\\d+")) {
                Toast.makeText(getContext(), "Mã PIN cũ phải là 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPin.isEmpty() || newPin.length() != 6 || !newPin.matches("\\d+")) {
                Toast.makeText(getContext(), "Mã PIN mới phải là 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPin.equals(confirmPin)) {
                Toast.makeText(getContext(), "Mã PIN mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            changePin(oldPin, newPin, dialog);
        });

        dialog.show();
    }

    private void changePin(String oldPin, String newPin, AlertDialog dialog) {
        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement change PIN API call
        Toast.makeText(getContext(), "Tính năng đổi PIN đang phát triển", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWalletInfo();
    }
}

