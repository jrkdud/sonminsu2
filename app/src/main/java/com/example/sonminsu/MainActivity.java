package com.example.sonminsu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.sonminsu.Fragment.HomeFragment;
import com.example.sonminsu.Fragment.NotificationFragment;
import com.example.sonminsu.Fragment.ProfileFragment;
import com.example.sonminsu.Fragment.SearchFragment;
import com.google.android.material.navigation.NavigationBarView;

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

        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        notificationFragment = new NotificationFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

        // Check if we're supposed to load the ProfileFragment:
        if (getIntent().getBooleanExtra("profileFragment", false)) {
            loadProfileFragment();
        }

        navigationBarView = findViewById(R.id.bottom_navigation);

        navigationBarView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            navigationStack.push(itemId);  // 선택 이력 저장

            // 선택한 아이템에 따라 프래그먼트 이동
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

            return true;  // 선택 처리 완료
        });
    }

    // 뒤로 가기 버튼 처리
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
}