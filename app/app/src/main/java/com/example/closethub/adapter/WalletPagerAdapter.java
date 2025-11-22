package com.example.closethub.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.closethub.WalletAccountFragment;
import com.example.closethub.WalletHistoryFragment;
import com.example.closethub.WalletMainFragment;

public class WalletPagerAdapter extends FragmentStateAdapter {
    public WalletPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WalletMainFragment();
            case 1:
                return new WalletHistoryFragment();
            case 2:
                return new WalletAccountFragment();
            default:
                return new WalletMainFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

