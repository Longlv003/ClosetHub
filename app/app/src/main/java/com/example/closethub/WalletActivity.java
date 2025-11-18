package com.example.closethub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.closethub.adapter.TransactionAdapter;
import com.example.closethub.adapter.WalletPagerAdapter;
import com.example.closethub.models.ApiResponse;
import com.example.closethub.models.Transaction;
import com.example.closethub.models.WalletResponse;
import com.example.closethub.models.WalletTransactionRequest;
import com.example.closethub.networks.ApiService;
import com.example.closethub.networks.RetrofitClient;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends AppCompatActivity {
    private ImageView imgMenu;
    
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initUI();
        setupViewPager();
        setupNavigationMenu();
        
        apiService = RetrofitClient.getApiService();
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        imgMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        // Ngăn DrawerLayout đóng drawer khi click vào header - dùng OnTouchListener để consume event
        View header = findViewById(R.id.header);
        View headerWallet = findViewById(R.id.headerWallet);
        if (headerWallet != null) {
            headerWallet.setOnTouchListener((v, event) -> {
                // Kiểm tra xem touch có nằm trong imgMenu không
                View menuIcon = headerWallet.findViewById(R.id.imgMenu);
                if (menuIcon != null) {
                    float x = event.getX();
                    float y = event.getY();
                    
                    // Lấy bounds của imgMenu trong headerWallet
                    float menuLeft = menuIcon.getLeft();
                    float menuTop = menuIcon.getTop();
                    float menuRight = menuIcon.getRight();
                    float menuBottom = menuIcon.getBottom();
                    
                    // Nếu touch vào imgMenu, để event lan truyền (return false)
                    if (x >= menuLeft && x <= menuRight && y >= menuTop && y <= menuBottom) {
                        return false; // Không consume, để imgMenu handle click
                    }
                }
                // Consume event nếu không phải click vào imgMenu (ngăn lan truyền lên DrawerLayout)
                return true;
            });
        } else if (header != null) {
            header.setOnTouchListener((v, event) -> {
                // Consume tất cả touch event trên header
                return true;
            });
        }
    }

    private void initUI() {
        imgMenu = findViewById(R.id.imgMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setupViewPager() {
        // Tạo adapter với các Fragment
        WalletPagerAdapter adapter = new WalletPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Màn hình chính");
                    break;
                case 1:
                    tab.setText("Lịch sử");
                    break;
                case 2:
                    tab.setText("Tài khoản");
                    break;
            }
        }).attach();
    }

    private void setupNavigationMenu() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(WalletActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(WalletActivity.this, AccountProfileActivity.class));
                } else if (id == R.id.nav_wallet) {
                    // Already in wallet
                } else if (id == R.id.nav_settings) {
                    // Settings
                } else if (id == R.id.nav_logout) {
                    startActivity(new Intent(WalletActivity.this, LoginActivity.class));
                    finish();
                }
                
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }
    
    public void switchToHistoryTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1, true); // Chuyển sang tab "Lịch sử" (index 1)
        }
    }

}

