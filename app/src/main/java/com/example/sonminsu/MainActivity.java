package com.example.sonminsu;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.sonminsu.Fragment.HomeFragment;
import com.example.sonminsu.Fragment.NotificationFragment;
import com.example.sonminsu.Fragment.ProfileFragment;
import com.example.sonminsu.Fragment.SearchFragment;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    HomeFragment homeFragment;
    SearchFragment searchFragment;
    NotificationFragment notificationFragment;
    ProfileFragment profileFragment;

    // 선택 이력을 저장하는 스택
    private Stack<Integer> navigationStack = new Stack<>();

    // 클래스 멤버 변수로 선언
    NavigationBarView navigationBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        notificationFragment = new NotificationFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

        navigationBarView = findViewById(R.id.bottom_navigation);

        if (getIntent().getBooleanExtra("profileFragment", false)) {
            loadProfileFragment();
        }

        String hashtag = getIntent().getStringExtra("hashtag");
        if (hashtag != null) {
            Bundle args = new Bundle();
            args.putString("hashtag", hashtag);
            searchFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, searchFragment).commit();


            updateNavigationBarState(R.id.navigation_search);
        }



        navigationBarView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            navigationStack.push(itemId);

            if (itemId == R.id.navigation_home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
            } else if (itemId == R.id.navigation_search) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, searchFragment).commit();
            } else if (itemId == R.id.navigation_upload) {
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.navigation_alarm) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, notificationFragment).commit();
            } else if (itemId == R.id.navigation_profile) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
            }

            return true;
        });

    }

    public void checkPermission() {
        String[] permissions = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }

        List<String> denied_permissions = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                denied_permissions.add(perm);
        }

        if(denied_permissions.size() > 0) {
            String [] deniedPerms = denied_permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, deniedPerms, 1000);
        }
    }


    @Override
    public void onBackPressed() {
        if (!navigationStack.isEmpty()) {
            navigationStack.pop();  // 현재 선택 제거
            if (!navigationStack.isEmpty()) {
                int previousSelection = navigationStack.peek();  // 이전 선택 가져오기
                // 이전 선택을 SharedPreferences에 저장
                SharedPreferences sharedPref = getSharedPreferences("MY_PREFS", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("previous_selection", previousSelection);
                editor.apply();

                navigationBarView.setSelectedItemId(previousSelection);  // 이전 선택으로 돌아가기
            } else {
                super.onBackPressed();  // 스택이 비어있으면 기본 뒤로 가기 동작 수행
            }
        } else {
            super.onBackPressed();  // 스택이 비어있으면 기본 뒤로 가기 동작 수행
        }
    }

    private void loadProfileFragment() {
        // Begin a new transaction:
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace the current fragment with the existing ProfileFragment:
        transaction.replace(R.id.fragment_container, profileFragment);

        // Commit the transaction:
        transaction.commit();
    }

    public void updateNavigationBarState(int itemId) {
        navigationBarView.setSelectedItemId(itemId);
    }

}