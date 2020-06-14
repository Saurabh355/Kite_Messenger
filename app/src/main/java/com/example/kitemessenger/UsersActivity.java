package com.example.kitemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mtoolBar;
    private RecyclerView mUsersList;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        mtoolBar = findViewById(R.id.Users_app_bar);
        setSupportActionBar(mtoolBar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersList = findViewById(R.id.usersList);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUsersDatabase, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users, UsersViewHolder > firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull final Users model) {

                holder.userName.setText(model.getName());
                holder.status.setText(model.getStatus());

                Picasso.with(UsersActivity.this).load(model.getThumb_image())
                        .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile_pic).into(holder.profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        Picasso.with(UsersActivity.this).load(model.getThumb_image()).placeholder(R.drawable.default_profile_pic).into(holder.profileImage);
                    }
                });

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profile_Intent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profile_Intent.putExtra("user_id", user_id);
                        startActivity(profile_Intent);
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);
                return new UsersViewHolder(view);
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        TextView userName, status;
        CircleImageView profileImage;
        View mView;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;


            userName= itemView.findViewById(R.id.user_name);
            status = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_image);
        }
    }
}
