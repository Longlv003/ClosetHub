package com.example.closethub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.closethub.adapter.BillAdapter;
import com.example.closethub.models.ApiResponse;
import com.example.closethub.models.Bill;
import com.example.closethub.networks.ApiService;
import com.example.closethub.networks.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BillFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BillFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BillFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BillFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BillFragment newInstance(String param1, String param2) {
        BillFragment fragment = new BillFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    private RecyclerView rcvBills;
    private BillAdapter billAdapter;
    private ArrayList<Bill> billArrayList;
    private ApiService apiService = RetrofitClient.getApiService();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bill, container, false);
        initUI(view);

        rcvBills.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sharedPref = getContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String idUser = sharedPref.getString("id_user", null);

        if(idUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        }
        billArrayList = new ArrayList<>();
        billAdapter = new BillAdapter(getContext(),billArrayList);
        rcvBills.setAdapter(billAdapter);

        loadBills(idUser);
        return view;
    }

    private void loadBills(String idUser) {
        apiService.getBills(idUser).enqueue(new Callback<ApiResponse<List<Bill>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Bill>>> call, Response<ApiResponse<List<Bill>>> response) {
                if(response.isSuccessful() && response.body() != null){
                    billArrayList.clear();
                    billArrayList.addAll(response.body().getData());
                    billAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Lỗi load bill!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Bill>>> call, Throwable throwable) {
                Log.e("Error", "Error", throwable);
            }
        });
    }

    private void initUI(View view) {
        rcvBills = view.findViewById(R.id.rcvBills);
    }
}