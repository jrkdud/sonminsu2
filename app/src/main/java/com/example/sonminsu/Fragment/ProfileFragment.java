package com.example.sonminsu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

//import com.example.sonminsu.FollowersActivity;
import com.example.sonminsu.EditProfileActivity;
import com.example.sonminsu.LoginActivity;
import com.example.sonminsu.ModifyActivity;
import com.example.sonminsu.R;
import com.example.sonminsu.RegisterActivity;
import com.example.sonminsu.SettingActivity;
import com.example.sonminsu.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.HashMap;

public class ProfileFragment extends Fragment {

    FirebaseUser firebaseUser;
    String profileid;

    private TextView profile_edit, logout;

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

        profile_edit = view.findViewById(R.id.profile_edit);
        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditProfileActivity로 전환하기 위해 Intent 생성
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);

                // startActivity를 사용하여 새로운 Activity로 전환
                startActivity(intent);

                // 애니메이션 없애기
                getActivity().overridePendingTransition(0, 0);

                addNotifications();
            }
        });

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
}
