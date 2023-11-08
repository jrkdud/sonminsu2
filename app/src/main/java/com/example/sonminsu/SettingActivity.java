package com.example.sonminsu;

import android.os.Bundle;
import android.app.Activity;

public class SettingActivity extends Activity {  // Activity를 상속받아야 합니다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {  // onCreate 메서드를 제대로 정의해야 합니다.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }
}

