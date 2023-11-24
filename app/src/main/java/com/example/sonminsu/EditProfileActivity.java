package com.example.sonminsu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.sonminsu.Model.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;


public class EditProfileActivity extends AppCompatActivity {

    private EditText et_id, et_pass, et_pass2, et_name, et_email;
    private Button btn_modify;
    private ImageButton eye, eye2, back;
    private boolean isEyeOff = true;

    FirebaseUser firebaseUser;
    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageRef;

    String username;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        et_id = findViewById(R.id.et_id);
        et_pass = findViewById(R.id.et_pass);
        et_pass2 = findViewById(R.id.et_pass2);
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        back = findViewById(R.id.back_btn);
        eye = findViewById(R.id.eye);
        eye2 = findViewById(R.id.eye2);
        btn_modify = findViewById(R.id.btn_modify);



//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User user = snapshot.getValue(User.class);
//                et_name.setText(user.getUsername());
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        ImageButton settingsButton = findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티를 종료합니다. 이 코드가 없으면 이전 액티비티로 돌아갔다가 뒤로 가기 버튼을 누르면 현재 액티비티로 다시 돌아오게 됩니다.
            }
        });

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
        eye2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEyeOff) {
                    // 비밀번호 보이기
                    eye2.setImageResource(R.drawable.eye);
                    et_pass2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    // 비밀번호 숨기기
                    eye2.setImageResource(R.drawable.eyeoff);
                    et_pass2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                isEyeOff = !isEyeOff;
                et_pass2.setSelection(et_pass2.getText().length()); // 커서를 마지막으로 이동
            }
        });

//        btn_modify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                updateProfile(et_name.getText().toString(),
//                        et_id.getText().toString(),
//                        et_pass.getText().toString(),
//                        et_email.getText().toString());
//            }
//            private void updateProfile(String et_name, String et_id, String et_pass, String et_email){
//                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
//
//                HashMap<String, Object> hashMap = new HashMap<>();
//                hashMap.put("et_name", username);
//
//                reference.updateChildren(hashMap);
//
//            }
//
//        });

        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String userid = et_id.getText().toString();
                String passwd = et_pass.getText().toString();
                String name = et_name.getText().toString();
                int email = Integer.parseInt(et_email.getText().toString());

            }
        });

    }
}