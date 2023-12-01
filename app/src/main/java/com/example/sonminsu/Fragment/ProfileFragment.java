package com.example.sonminsu.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

//import com.example.sonminsu.FollowersActivity;
import com.bumptech.glide.Glide;
import com.example.sonminsu.EditProfileActivity;
import com.example.sonminsu.LoginActivity;
import com.example.sonminsu.MainActivity;
import com.example.sonminsu.ModifyActivity;
import com.example.sonminsu.R;
import com.example.sonminsu.RegisterActivity;
import com.example.sonminsu.SettingActivity;
import com.example.sonminsu.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

public class ProfileFragment extends Fragment {

    FirebaseUser firebaseUser;
    String profileid;

    private TextView profile_edit, logout, username;
    ImageView image_profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageButton settingsButton = view.findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });

        username = view.findViewById(R.id.username);
        image_profile = view.findViewById(R.id.image_profile);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 데이터베이스에서 사용자 이름을 가져와 TextView에 설정합니다.
                String usernameFromDB = dataSnapshot.child("username").getValue(String.class);
                username.setText(usernameFromDB);

                // 데이터베이스에서 이미지 URL을 가져와 CircleImageView에 설정합니다.
                String imageUrlFromDB = dataSnapshot.child("imageurl").getValue(String.class);
                Glide.with(getContext()).load(imageUrlFromDB).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스에서 데이터를 읽는 데 실패했을 때 호출됩니다.
            }
        });



        RelativeLayout layout = view.findViewById(R.id.pf_edit_wrap);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ModifyActivity.class);
            }
        });

        RelativeLayout layout2 = view.findViewById(R.id.list_wrap);
        layout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new PostManageFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

        });
        RelativeLayout layout3 = view.findViewById(R.id.bookmarks_wrap);
        layout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new BookmarkFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

        });

        //내 정보 수정
        profile_edit = view.findViewById(R.id.profile_edit);
        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditProfileActivity로 전환하기 위해 Intent 생성
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);

                // startActivity를 사용하여 새로운 Activity로 전환
                startActivity(intent);

                // 애니메이션 없애기
                if (getActivity() != null) {
                    getActivity().overridePendingTransition(0, 0);
                }
            }
        });

        //로그아웃
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditProfileActivity로 전환하기 위해 Intent 생성
                Intent intent = new Intent(getActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // startActivity를 사용하여 새로운 Activity로 전환
                startActivity(intent);
            }
        });

        //회원탈퇴
        TextView userRemove = view.findViewById(R.id.userremove);
        userRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AlertDialog 생성
                new AlertDialog.Builder(getContext())
                        .setMessage("탈퇴하시겠습니까?")
                        .setPositiveButton("탈퇴", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                                    ref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Firebase에서 사용자 데이터 삭제
                                                user.delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    // 데이터 삭제 성공
                                                                    Intent intent = new Intent(getActivity(), LoginActivity.class)
                                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    startActivity(intent);
                                                                } else {
                                                                    // 데이터 삭제 실패
                                                                    Toast.makeText(getContext(), "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                // 데이터베이스에서 데이터 삭제 실패
                                                Toast.makeText(getContext(), "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        return view;
    }

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).updateNavigationBarState(R.id.navigation_profile);
    }

}
