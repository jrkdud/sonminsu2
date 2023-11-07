package com.example.sonminsu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.example.sonminsu.Fragment.HomeFragment;
import com.example.sonminsu.Fragment.NotificationFragment;
import com.example.sonminsu.Fragment.PostFragment;
import com.example.sonminsu.Fragment.ProfileFragment;
import com.example.sonminsu.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    HomeFragment homeFragment;
    SearchFragment searchFragment;
    PostFragment postFragment;
    NotificationFragment notificationFragment;
    ProfileFragment profileFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        postFragment = new PostFragment();
        notificationFragment = new NotificationFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation);
        navigationBarView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
                return true;
            } else if (itemId == R.id.navigation_search) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, searchFragment).commit();
                return true;
            } else if (itemId == R.id.navigation_upload) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, postFragment).commit();
                return true;
            } else if (itemId == R.id.navigation_alarm) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, notificationFragment).commit();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
                return true;
            }

            return false;
        });
    }
}