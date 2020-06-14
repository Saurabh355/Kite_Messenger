package com.example.kitemessenger;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUserDatabase;

    private FirebaseAuth mAuth;

    private String mCurrentUserID;

    private View mMainView;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats,container,false);

        mConvList = mMainView.findViewById(R.id.conversation_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserID);
        mConvDatabase.keepSynced(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserID);
        mUserDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> adapter =new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(
                new FirebaseRecyclerOptions.Builder<Conversation>()
                .setIndexedQuery(conversationQuery,mMessageDatabase,Conversation.class)
                .build()

        ) {
            @Override
            protected void onBindViewHolder(@NonNull final ConversationViewHolder conversationViewHolder, int i, @NonNull final Conversation conversation) {

                final String list_user_id = getRef(i).getKey();
                Query last_msg_Query = mMessageDatabase.child(list_user_id).limitToLast(1);
                last_msg_Query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        final String type = dataSnapshot.child("type").getValue().toString();

                            if (dataSnapshot.hasChild("message")) {
                                String data = dataSnapshot.child("message").getValue().toString();
                                conversationViewHolder.setMessage(data, conversation.isSeen(),type);

                            }

                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName =dataSnapshot.child("name").getValue().toString();
                        String userThumb  = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")){

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            conversationViewHolder.setUserOnline(userOnline);
                        }
                        conversationViewHolder.setName(userName);
                        conversationViewHolder.setUserImage(userThumb,getContext());

                        conversationViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("user_id",list_user_id);
                                chatIntent.putExtra("user_name",userName);
                                startActivity(chatIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout,parent,false);
                ConversationViewHolder holder = new ConversationViewHolder(view);
                return holder;
            }
        };
        mConvList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

       public void setMessage(String message, boolean isSeen,String type){

           mView.findViewById(R.id.user_status).setVisibility(View.INVISIBLE);
            if (type.equals("text")) {
                TextView user_status_view = mView.findViewById(R.id.last_message);
                user_status_view.setVisibility(View.VISIBLE);
                user_status_view.setText(message);

                if (!isSeen) {
                    user_status_view.setTypeface(user_status_view.getTypeface(), Typeface.BOLD);
                } else {
                    user_status_view.setTypeface(user_status_view.getTypeface(), Typeface.NORMAL);
                }
            }
            else if (type.equals("image")){
                mView.findViewById(R.id.image_icon_layout).setVisibility(View.VISIBLE);
            }



       }

       public void setName(String name){
            TextView user_name_view = mView.findViewById(R.id.user_name);
            user_name_view.setText(name);
       }

       public void setUserImage(final String thumbImage, final Context ctx){
           final CircleImageView user_image_view = mView.findViewById(R.id.user_image);
           Picasso.with(ctx).load(thumbImage)
                   .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile_pic).into(user_image_view, new Callback() {
               @Override
               public void onSuccess() {

               }

               @Override
               public void onError() {

                   Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_profile_pic).into(user_image_view);
               }
           });
       }

       public void setUserOnline(String online_status){

           ImageView userOnlineView = mView.findViewById(R.id.user_online_icon);

           if (online_status.equals("true")){
               userOnlineView.setVisibility(View.VISIBLE);
           }else {
               userOnlineView.setVisibility(View.INVISIBLE);
           }
       }
    }
}
