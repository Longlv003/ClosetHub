package com.example.closethub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.closethub.adapter.ColorProductAdapter;
import com.example.closethub.adapter.SizeProductAdapter;
import com.example.closethub.models.ApiResponse;
import com.example.closethub.models.Cart;
import com.example.closethub.models.CartLookUpProduct;
import com.example.closethub.models.CartRequest;
import com.example.closethub.models.Product;
import com.example.closethub.models.User;
import com.example.closethub.models.Variant;
import com.example.closethub.networks.ApiService;
import com.example.closethub.networks.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView imgBack, imgFavorite, imgProduct, imgAddQuantity, imgRemoveQuantity;
    private RecyclerView rcvColor, rcvSize;
    private EditText edtQuantity;
    private TextView txtNameProduct, txtPrice, txtDescription;
    private Button btnAddToCart;
    private void initUI() {
        imgBack = findViewById(R.id.imgBack);
        imgFavorite = findViewById(R.id.imgFavorite);
        imgProduct = findViewById(R.id.imgProduct);
        imgAddQuantity = findViewById(R.id.imgAddQuantity);
        imgRemoveQuantity = findViewById(R.id.imgRemoveQuantity);
        rcvColor = findViewById(R.id.rcvColor);
        rcvSize = findViewById(R.id.rcvSize);
        edtQuantity = findViewById(R.id.edtQuantity);
        txtNameProduct = findViewById(R.id.txtNameProduct);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
    }
    private ApiService apiService;
    private SizeProductAdapter sizeProductAdapter;
    private ColorProductAdapter colorProductAdapter;
    private Product product;
    private String selectedSize = null;
    private String selectedColor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUI();
        apiService = RetrofitClient.getApiService();

        String productId = getIntent().getStringExtra("product_id");
        Log.d("DETAIL", "productId = " + productId);

        imgBack.setOnClickListener(v -> {
            finish();
        });

        LoadDataDetail(productId);

        imgAddQuantity.setOnClickListener(v -> {
            Variant selected = getSelectedVariant();
            if (selected == null) return;

            int stock = selected.getQuantity();
            int current = Integer.parseInt(edtQuantity.getText().toString());

            if (current < stock) {
                edtQuantity.setText(String.valueOf(current + 1));
            }

            updateQuantityButtons();
        });


        imgRemoveQuantity.setOnClickListener(v -> {
            int current = Integer.parseInt(edtQuantity.getText().toString());

            if (current > 1) {
                edtQuantity.setText(String.valueOf(current - 1));
            }

            updateQuantityButtons();
        });

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userJson = prefs.getString("user_data", null);

        if (userJson == null) {
            Toast.makeText(this, "account found", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        User user = gson.fromJson(userJson, User.class);

        btnAddToCart.setOnClickListener(v -> {
            Variant variant = getSelectedVariant();
            if (variant == null) return;

            CartRequest request = new CartRequest();
            request.setId_user(user.get_id());
            request.setId_product(productId);
            request.setId_variant(variant.get_id());
            request.setQuantity(Integer.parseInt(edtQuantity.getText().toString().trim()));

            AddToCart(user.getToken(), request);
        });
    }

    private void AddToCart(String token, CartRequest request) {
        apiService.addToCart(token, request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductDetailActivity.this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Thêm sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable throwable) {
                Toast.makeText(ProductDetailActivity.this, "Error", Toast.LENGTH_SHORT).show();
                Log.e("Error", "AddToCart", throwable);
            }
        });
    }

    private void LoadDataDetail(String productId) {
        apiService.getProductDetail(productId).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    product = response.body().getData();

                    // Lấy ảnh đầu tiên trong danh sách ảnh sản phẩm
                    String imageUrl = "";
                    if (product.getImage() != null && !product.getImage().isEmpty()) {
                        imageUrl = "http://10.0.2.2:3000/images/products/" + product.getImage().get(0);
                    }

                    Glide.with(ProductDetailActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_placeholder) // Ảnh tạm khi đang load
                            .error(R.drawable.ic_error)             // Ảnh lỗi nếu load thất bại
                            .into(imgProduct);

                    if (product.isIs_favorite()) {
                        imgFavorite.setImageResource(R.drawable.ic_favorite_red);
                    } else {
                        imgFavorite.setImageResource(R.drawable.ic_favorite);
                    }

                    txtNameProduct.setText(product.getName());
                    txtDescription.setText(product.getDescription());

                    ArrayList<String> listSize = new ArrayList<>();
                    ArrayList<String> listColor = new ArrayList<>();

                    for (Variant v : product.getVariants()) {
                        if (!listSize.contains(v.getSize())) {
                            listSize.add(v.getSize());
                        }
                        if (!listColor.contains(v.getColor())) {
                            listColor.add(v.getColor());
                        }
                    }
                    Log.d("LIST_SIZE", "Count: "+listSize.size());
                    Log.d("LIST_COLOR", "Count: "+listColor.size());

                    setColorAdapter(listColor);
                    setSizeAdapter(listSize);

                    if (!product.getVariants().isEmpty()) {
                        Variant first = product.getVariants().get(0);

                        selectedSize = first.getSize();
                        selectedColor = first.getColor();

                        List<String> validColors = getColorsBySize(selectedSize);
                        colorProductAdapter.updateAvailableColors(validColors);
                        colorProductAdapter.setSelectedColor(selectedColor);

                        List<String> validSizes = getSizesByColor(selectedColor);
                        sizeProductAdapter.updateAvailableSizes(validSizes);
                        sizeProductAdapter.setSelectedSize(selectedSize);

                        updatePrice();
                    }

                    edtQuantity.setText("1");
                    updateQuantityButtons();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable throwable) {
                Log.e("API_ERROR", "Load detail failed", throwable);
            }
        });
    }

    private void setSizeAdapter(ArrayList<String> listSize) {
        sizeProductAdapter = new SizeProductAdapter(listSize, size -> {
            selectedSize = size;

            List<String> validColors = getColorsBySize(size);
            Log.d("GET_SIZE_COLOR", "Color = " + size + " → " + validColors);
            colorProductAdapter.updateAvailableColors(validColors);

            if (selectedColor == null || !validColors.contains(selectedColor)) {
                selectedColor = validColors.get(0);
            }

            colorProductAdapter.setSelectedColor(selectedColor);

            List<String> validSizes = getSizesByColor(selectedColor);
            sizeProductAdapter.updateAvailableSizes(validSizes);
            sizeProductAdapter.setSelectedSize(selectedSize);

            edtQuantity.setText("1");
            updateQuantityButtons();

            updatePrice();
        });
        rcvSize.setLayoutManager(new LinearLayoutManager(ProductDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
        rcvSize.setAdapter(sizeProductAdapter);
    }

    private void setColorAdapter(ArrayList<String> listColor) {
        colorProductAdapter = new ColorProductAdapter(listColor, color -> {
            selectedColor = color;

            List<String> validSizes = getSizesByColor(color);
            Log.d("GET_SIZE_COLOR", "Color = " + color + " → " + validSizes);
            sizeProductAdapter.updateAvailableSizes(validSizes);

            if (selectedSize == null || !validSizes.contains(selectedSize)) {
                selectedSize = validSizes.get(0);
            }

            sizeProductAdapter.setSelectedSize(selectedSize);

            List<String> validColors = getColorsBySize(selectedSize);
            colorProductAdapter.updateAvailableColors(validColors);
            colorProductAdapter.setSelectedColor(selectedColor);

            updatePrice();
        });
        rcvColor.setLayoutManager(new LinearLayoutManager(ProductDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
        rcvColor.setAdapter(colorProductAdapter);

        edtQuantity.setText("1");
        updateQuantityButtons();
    }

    private void updatePrice() {
        if (selectedSize == null || selectedColor == null) return;

        for (Variant v : product.getVariants()) {
            if (v.getSize().equals(selectedSize) &&
                    v.getColor().equals(selectedColor)) {

                txtPrice.setText(v.getPrice() + "đ");
                break;
            }
        }
    }

    private List<String> getColorsBySize(String size) {
        List<String> list = new ArrayList<>();
        for (Variant v : product.getVariants()) {
            if (v.getSize().equals(size)) {
                list.add(v.getColor());
            }
        }
        return list;
    }

    private List<String> getSizesByColor(String color) {
        List<String> list = new ArrayList<>();
        for (Variant v : product.getVariants()) {
            if (v.getColor().equals(color)) {
                list.add(v.getSize());
            }
        }
        return list;
    }

    private Variant getSelectedVariant() {
        if (product == null || product.getVariants() == null) return null;

        for (Variant v : product.getVariants()) {
            if (v.getSize().equals(selectedSize) &&
                    v.getColor().equals(selectedColor)) {
                return v;
            }
        }
        return null;
    }

    private void updateQuantityButtons() {
        Variant selected = getSelectedVariant();
        if (selected == null) return;

        int stock = selected.getQuantity();
        int current = Integer.parseInt(edtQuantity.getText().toString());

        // Disable nút trừ nếu số lượng = 1
        imgRemoveQuantity.setEnabled(current > 1);
        imgRemoveQuantity.setAlpha(current > 1 ? 1f : 0.3f);

        // Disable nút cộng nếu số lượng = stock
        imgAddQuantity.setEnabled(current < stock);
        imgAddQuantity.setAlpha(current < stock ? 1f : 0.3f);
    }

}