package com.example.sonminsu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    EditText et_name, et_id, et_pass, et_email;
    Button btn_register;
    ImageButton eye, back;
    private boolean isEyeOff = true;
    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_id = findViewById(R.id.et_id);
        et_pass = findViewById(R.id.et_pass);
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        eye = findViewById(R.id.eye);
        back = findViewById(R.id.back);
        btn_register = findViewById(R.id.btn_register);

        auth = FirebaseAuth.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_username = et_name.getText().toString();
                String str_id = et_id.getText().toString();
                String str_email = et_email.getText().toString();
                String str_password = et_pass.getText().toString();

                if(TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_id) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                    Toast.makeText(RegisterActivity.this, "모든 입력란을 채워주세요", Toast.LENGTH_SHORT).show();
                } else if (str_password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "비밀번호는 6자이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    register(str_username, str_id, str_email, str_password);
                }
            }
        });

        //로그인 화면으로 돌아가기
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티를 종료합니다. 이 코드가 없으면 이전 액티비티(로그인 화면)로 돌아갔다가 뒤로 가기 버튼을 누르면 현재 액티비티로 다시 돌아오게 됩니다.
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
    private void register(String username, String fullname, String email, String password){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String userid = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                    String[] imageUrls = {
                            "https://firebasestorage.googleapis.com/v0/b/smsfirebase-cbc65.appspot.com/o/person3.png?alt=media&token=e84ceb38-a55d-4375-b2b6-2aa68601ccc8",
                            "https://firebasestorage.googleapis.com/v0/b/smsfirebase-cbc65.appspot.com/o/person4.png?alt=media&token=5f22ce14-631d-4751-84b5-012d13c773c1",
                            "https://firebasestorage.googleapis.com/v0/b/smsfirebase-cbc65.appspot.com/o/person2.png?alt=media&token=7db9ec2b-4c22-4e8a-a184-39ab3c90ff2d"
                    };

                    Random rand = new Random();
                    String randomImageUrl = imageUrls[rand.nextInt(imageUrls.length)];

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("username", username.toLowerCase());
                    hashMap.put("fullname", fullname);
                    hashMap.put("bio", "");
                    hashMap.put("email", email);
                    hashMap.put("imageurl", randomImageUrl);  // 랜덤으로 선택한 이미지 URL을 사용


                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                pd.dismiss();
                                Toast.makeText(RegisterActivity.this, "회원가입에 성공하였습니다. 로그인 창으로 돌아갑니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                } else {
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "입력하신 이메일과 비밀번호는 가입할 수 없습니다.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}