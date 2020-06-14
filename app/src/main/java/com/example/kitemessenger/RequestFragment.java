package com.example.kitemessenger;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private View RequestFragmentView;
    private RecyclerView mRequestList;

    private DatabaseReference chat_request_ref, user_Ref, friend_Ref;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        RequestFragmentView  = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
         currentUserID = mAuth.getCurrentUser().getUid();

         user_Ref = FirebaseDatabase.getInstance().getReference().child("Users");
        chat_request_ref = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        friend_Ref = FirebaseDatabase.getInstance().getReference();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        mRequestList = RequestFragmentView.findViewById(R.id.requests_list);
        mRequestList.setLayoutManager(linearLayoutManager);

        return RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chat_request_ref.child(currentUserID),Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull Contacts contacts) {


                        final String list_user_id = getRef(i).getKey();
                        DatabaseReference get_type_ref = getRef(i).child("request_type").getRef();

                        get_type_ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received"))
                                    {
                                        requestViewHolder.itemView.findViewById(R.id.accept_btn).setVisibility(View.VISIBLE);
                                        requestViewHolder.itemView.findViewById(R.id.cancel_btn).setVisibility(View.VISIBLE);

                                        user_Ref.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    final String request_userName = dataSnapshot.child("name").getValue().toString();
                                                    final String request_userStatus = dataSnapshot.child("status").getValue().toString();
                                                    final String request_userProfileImage = dataSnapshot.child("thumb_image").getValue().toString();

                                                    requestViewHolder.user_name.setText(request_userName);
                                                    requestViewHolder.status.setText(request_userStatus);
                                                    Picasso.with(getContext()).load(request_userProfileImage).placeholder(R.drawable.default_profile_pic).into(requestViewHolder.profileImage);


                                                    requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                            profileIntent.putExtra("user_id", list_user_id);
                                                            startActivity(profileIntent);
                                                        }
                                                    });




                                                    requestViewHolder.itemView.findViewById(R.id.accept_btn).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                                                        Map friendsMap = new HashMap();
                                                        friendsMap.put("Friends/" + currentUserID + "/" + list_user_id + "/date", currentDate);
                                                        friendsMap.put("Friends/" + list_user_id + "/"  + currentUserID + "/date", currentDate);

                                                        friendsMap.put("Friend_req/" + currentUserID + "/" + list_user_id, null);
                                                        friendsMap.put("Friend_req/" + list_user_id + "/" + currentUserID, null);

                                                        friend_Ref.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                                                if(databaseError == null){

                                                                } else {
                                                                    String error = databaseError.getMessage();
                                                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });

                                                requestViewHolder.itemView.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        final Map declineMap = new HashMap();
                                                        declineMap.put("Friend_req/" + currentUserID + "/" + list_user_id, null);
                                                        declineMap.put("Friend_req/" + list_user_id + "/" + currentUserID, null);

                                                        friend_Ref.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                if(databaseError == null)
                                                                {
                                                                }else{
                                                                    String error = databaseError.getMessage();
                                                                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                    else if (type.equals("sent")){

                                        Button cancel_sent_req_btn = requestViewHolder.itemView.findViewById(R.id.cancel_sent_request_btn);
                                        cancel_sent_req_btn.setVisibility(View.VISIBLE);


                                        user_Ref.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                final String request_userName = dataSnapshot.child("name").getValue().toString();
                                                final String request_userStatus = dataSnapshot.child("status").getValue().toString();
                                                final String request_userProfileImage = dataSnapshot.child("thumb_image").getValue().toString();

                                                requestViewHolder.user_name.setText(request_userName);
                                                requestViewHolder.status.setText(request_userStatus);
                                                Picasso.with(getContext()).load(request_userProfileImage)
                                                        .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile_pic).into(requestViewHolder.profileImage, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {

                                                        Picasso.with(getContext()).load(request_userProfileImage).placeholder(R.drawable.default_profile_pic).into(requestViewHolder.profileImage);
                                                    }
                                                });

                                                requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                        profileIntent.putExtra("user_id", list_user_id);
                                                        startActivity(profileIntent);
                                                    }
                                                });

                                                requestViewHolder.itemView.findViewById(R.id.cancel_sent_request_btn).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                      chat_request_ref.child(currentUserID).child(list_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                chat_request_ref.child(list_user_id).child(currentUserID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {


                                                                    }
                                                                });

                                                            }
                                                        });


                                                    }
                                                });


                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout,parent,false);
                      RequestViewHolder holder = new RequestViewHolder(view);
                      return holder;
                    }
                };
        mRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView user_name, status;
        CircleImageView profileImage;
        Button acceptBtn, cancelBtn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            user_name = itemView.findViewById(R.id.user_name);
            status = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_image);
            acceptBtn = itemView.findViewById(R.id.accept_btn);
            cancelBtn = itemView.findViewById(R.id.cancel_btn);
        }
    }
}
