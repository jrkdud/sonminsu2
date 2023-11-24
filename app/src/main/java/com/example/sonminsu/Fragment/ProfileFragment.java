package com.example.sonminsu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

//import com.example.sonminsu.FollowersActivity;
import com.example.sonminsu.ModifyActivity;
import com.example.sonminsu.R;
import com.example.sonminsu.SettingActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class ProfileFragment extends Fragment {

    FirebaseUser firebaseUser;
    String profileid;

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



//        private void addNotifications() {
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);
//
//            HashMap<String, Object> hashMap = new HashMap<>();
//            hashMap.put("userid", firebaseUser.getUid());
//            hashMap.put("text", "started following you");
//            hashMap.put("postid", "");
//            hashMap.put("ispost", false);
//
//            reference.push().setValue(hashMap);
//        }
        return view;
    }
}
