package com.example.sonminsu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sonminsu.Adapter.UserAdapter;
import com.example.sonminsu.Adapter.PostAdapter; // Assuming you have a PostAdapter
import com.example.sonminsu.MainActivity;
import com.example.sonminsu.Model.Post;
import com.example.sonminsu.Model.User;
import com.example.sonminsu.R;
import com.example.sonminsu.SettingActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerViewUsers;
    private RecyclerView recyclerViewPosts;

    private UserAdapter userAdapter;
    private PostAdapter postAdapter;
    private List<User> mUsers;
    private List<Post> mPosts;

    EditText search_bar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerViewUsers = view.findViewById(R.id.recycler_view_users);
        recyclerViewUsers.setHasFixedSize(true);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewPosts = view.findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        search_bar = view.findViewById(R.id.search_bar);

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), mUsers);
        recyclerViewUsers.setAdapter(userAdapter);

        mPosts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), mPosts); // Assuming you have a PostAdapter
        recyclerViewPosts.setAdapter(postAdapter);

        Bundle args = getArguments();
        if (args != null) {
            String hashtag = args.getString("hashtag");
            if (hashtag != null) {
                search_bar.setText(hashtag);
                recyclerViewUsers.setVisibility(View.GONE);
                searchPosts(hashtag);
            } else {
                recyclerViewUsers.setVisibility(View.VISIBLE);
                readUsers();
            }
        } else {
            recyclerViewUsers.setVisibility(View.VISIBLE);
            readUsers();
        }



        readUsers();

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequenc, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    mUsers.clear();
                    mPosts.clear();
                    userAdapter.notifyDataSetChanged();
                    postAdapter.notifyDataSetChanged();

                    recyclerViewUsers.setVisibility(View.VISIBLE);

                    readUsers();
                } else {
                    searchUsers(charSequence.toString().toLowerCase());
                    searchPosts(charSequence.toString().toLowerCase());

                    if (charSequence.toString().startsWith("#")) {
                        recyclerViewUsers.setVisibility(View.GONE);
                    } else {
                        recyclerViewUsers.setVisibility(View.VISIBLE);
                    }
                }
            }



            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    private void searchUsers(String s) {
        if (s.isEmpty()) {
            mUsers.clear();
            userAdapter.notifyDataSetChanged();
            return; // No need to perform the database query
        }

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username").startAt(s).endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    mUsers.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void searchPosts(String s) {
        if (s.isEmpty()) {
            mPosts.clear();
            postAdapter.notifyDataSetChanged();
            return; // No need to perform the database query
        }

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("description");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPosts.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);


                    if (post.getDescription().toLowerCase().contains(s.toLowerCase())) {
                        mPosts.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }




    private void readUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    mUsers.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton settingsButton = view.findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).updateNavigationBarState(R.id.navigation_search);
    }
}
