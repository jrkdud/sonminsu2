package com.example.sonminsu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sonminsu.Fragment.ProfileFragment;
import com.example.sonminsu.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
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

    private EditText et_pass, et_pass2, et_name;
    private TextView et_id, et_email;
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

        ImageButton settingsButton = findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                intent.putExtra("profileFragment", true);
                startActivity(intent);
                finish();
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

        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                // 비밀번호 변경
                String oldPassword = et_pass.getText().toString();
                String newPassword = et_pass2.getText().toString();

                if (!oldPassword.isEmpty() && !newPassword.isEmpty()) {
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(newPassword)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(EditProfileActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();

                                                            // 로그아웃
                                                            FirebaseAuth.getInstance().signOut();

                                                            // 로그인 액티비티 시작
                                                            Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(EditProfileActivity.this, "비밀번호 변경에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(EditProfileActivity.this, "현재 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                // 닉네임 변경
                String newUsername = et_name.getText().toString();

                if (!newUsername.isEmpty()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("username", newUsername);
                    reference.updateChildren(hashMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditProfileActivity.this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(EditProfileActivity.this, "닉네임 변경에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User userProfile = dataSnapshot.getValue(User.class);

                    if (userProfile != null) {
                        String fullname = userProfile.getFullname();
                        String email = userProfile.getEmail();

                        et_id.setText(fullname);
                        et_email.setText(email);
                        et_id.setTextColor(ContextCompat.getColor(EditProfileActivity.this, R.color.colorMain));
                        et_email.setTextColor(ContextCompat.getColor(EditProfileActivity.this, R.color.colorMain));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 에러 처리
                }
            });
        }

    }
}