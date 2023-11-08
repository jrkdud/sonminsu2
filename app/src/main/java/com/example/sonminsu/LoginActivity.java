package com.example.sonminsu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    EditText et_id, et_pass;
    Button btn_login, btn_register;
    Button checkbox;
    private boolean isChecked = false;
    ImageButton eye;
    private boolean isEyeOff = true;
    FirebaseAuth auth;
    ProgressDialog pd;

    // 아이디 저장을 위한 SharedPreferences 객체 선언
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceStats) {
        super.onCreate(savedInstanceStats);
        setContentView(R.layout.activity_login);

        et_id = findViewById(R.id.et_id);
        btn_login = findViewById(R.id.btn_login);
        et_pass = findViewById(R.id.et_pass);
        btn_register = findViewById(R.id.btn_register);
        checkbox = findViewById(R.id.checkbox);
        eye = findViewById(R.id.eye);

        auth = FirebaseAuth.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_id = et_id.getText().toString();
                String str_password = et_pass.getText().toString();

                // 체크박스 상태에 따라 아이디를 저장
                if(isChecked) {
                    editor.putString("id", str_id);
                    editor.putBoolean("isChecked", true);
                } else {
                    editor.remove("id");
                    editor.putBoolean("isChecked", false);
                }
                editor.apply();

                if (TextUtils.isEmpty(str_id) || TextUtils.isEmpty(str_password)) {
                    Toast.makeText(LoginActivity.this, "모든 입력란을 채워주세요", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(str_id, str_password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());

                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (pd != null && pd.isShowing()) { // pd가 null이 아니고, pd가 표시 중일 때만 dismiss() 호출
                                            pd.dismiss();
                                        }
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        if (pd != null && pd.isShowing()) { // pd가 null이 아니고, pd가 표시 중일 때만 dismiss() 호출
                                            pd.dismiss();
                                        }
                                    }
                                });
                            } else {
                                if (pd != null && pd.isShowing()) { // pd가 null이 아니고, pd가 표시 중일 때만 dismiss() 호출
                                    pd.dismiss();
                                }
                                Toast.makeText(LoginActivity.this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // 체크박스 상태와 아이디를 불러옴
        isChecked = sharedPreferences.getBoolean("isChecked", false);
        String savedId = sharedPreferences.getString("id", "");

        // 체크박스 상태에 따라 아이디를 설정하고 체크박스 모양 변경
        if(isChecked) {
            et_id.setText(savedId);
            checkbox.setBackgroundResource(R.drawable.checked);
        } else {
            checkbox.setBackgroundResource(R.drawable.unchecked);
        }

        //체크박스 모양 버튼
        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChecked) {
                    checkbox.setBackgroundResource(R.drawable.unchecked);
                } else {
                    checkbox.setBackgroundResource(R.drawable.checked);
                }
                isChecked = !isChecked;
            }
        });

        // 보이기 숨기기 이미지버튼
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEyeOff) {
                    // 비밀번호 보이기
                    eye.setImageResource(R.drawable.eye);
                    et_pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    // 비밀번호 숨기기
                    eye.setImageResource(R.drawable.eyeoff);
                    et_pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                isEyeOff = !isEyeOff;
                et_pass.setSelection(et_pass.getText().length()); // 커서를 마지막으로 이동
            }
        });

    }


}