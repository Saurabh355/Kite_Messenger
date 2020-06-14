package com.example.kitemessenger;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {



        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_list,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView mMessageText_left;
        public TextView mMessageText_right;
        public ImageView left_image;
        public ImageView right_image;

        public  MessageViewHolder(View view) {
            super(view);
            mMessageText_left = view.findViewById(R.id.message_text_layout_left);
            mMessageText_right = view.findViewById(R.id.message_text_layout_right);
            left_image = view.findViewById(R.id.left_image);
            right_image = view.findViewById(R.id.right_image);

        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        final Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
        String message_Type  = c.getType();

        holder.mMessageText_left.setVisibility(View.GONE);
        holder.mMessageText_right.setVisibility(View.GONE);
        holder.left_image.setVisibility(View.GONE);
        holder.right_image.setVisibility(View.GONE);

        if (message_Type.equals("text")) {

            if (from_user.equals(current_user_id)) {

                holder.mMessageText_right.setVisibility(View.VISIBLE);

                holder.mMessageText_right.setBackgroundResource(R.drawable.message_text_background);
                holder.mMessageText_right.setTextColor(Color.BLACK);

                holder.mMessageText_right.setText(c.getMessage());


            } else {

                holder.mMessageText_left.setVisibility(View.VISIBLE);


                holder.mMessageText_left.setBackgroundResource(R.drawable.message_text_background);
                holder.mMessageText_left.setTextColor(Color.BLACK);

                holder.mMessageText_left.setText(c.getMessage());

            }
            //  holder.mMessageText_right.setText(c.getMessage());
        }
        else if (message_Type.equals("image")){

            if (from_user.equals(current_user_id)){

                holder.mMessageText_left.setVisibility(View.INVISIBLE);
                holder.right_image.setVisibility(View.VISIBLE);
                Picasso.with(holder.right_image.getContext()).load(c.getMessage())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.image_default).into(holder.right_image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(holder.right_image.getContext()).load(c.getMessage())
                                .placeholder(R.drawable.image_default).into(holder.right_image);

                    }
                });

            }else {

                holder.mMessageText_right.setVisibility(View.INVISIBLE);
                holder.left_image.setVisibility(View.VISIBLE);
                Picasso.with(holder.left_image.getContext()).load(c.getMessage())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.image_default).into(holder.left_image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        Picasso.with(holder.left_image.getContext()).load(c.getMessage())
                                .placeholder(R.drawable.image_default).into(holder.left_image);
                    }
                });


            }
        }
    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}



/*
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mMsgRef;
    private FirebaseAuth mAuth;

    private LinearLayout layout;


    public TextView messageText;
    public CircleImageView profileImage;
    public TextView displayName;
    public ImageView messageImage;



    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout_left,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        //        public  TextView messageText;
        public CircleImageView profileImage;
//        public TextView displayName;
//        public ImageView messageImage;

        public LinearLayout leftMsgLayout;

        public LinearLayout rightMsgLayout;

        public TextView leftMsgTextView;

        public TextView rightMsgTextView;

        public TextView fromTime;

        public TextView toTime;



        public MessageViewHolder(View view) {
            super(view);

//            messageText = (TextView) view.findViewById(R.id.message_text_layout);
//            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
////            displayName = (TextView) view.findViewById(R.id.name_text_layout);
//            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
//
//            layout = view.findViewById(R.id.message_single_layout);

            leftMsgLayout =  itemView.findViewById(R.id.chat_left_msg_layout);
            rightMsgLayout =  itemView.findViewById(R.id.chat_right_msg_layout);
            leftMsgTextView =  itemView.findViewById(R.id.chat_left_msg_text_view);
            rightMsgTextView =  itemView.findViewById(R.id.chat_right_msg_text_view);
            fromTime = itemView.findViewById(R.id.from_time);
            toTime = itemView.findViewById(R.id.to_time);



        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int position) {

        mAuth = FirebaseAuth.getInstance();

        final String current_user_id = mAuth.getCurrentUser().getUid();

        final Messages msgDto = mMessageList.get(position);

        String from_user = msgDto.getFrom();
        final String message_type = msgDto.getType();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mMsgRef = FirebaseDatabase.getInstance().getReference().child("messages");

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();


                if(dataSnapshot.getKey().equals(current_user_id))
                {

                    viewHolder.rightMsgLayout.setVisibility(LinearLayout.VISIBLE);
                    if(message_type.equals("text")) {

                        viewHolder.rightMsgTextView.setText(msgDto.getMessage());

                    }
                    viewHolder.leftMsgLayout.setVisibility(LinearLayout.GONE);
                }
                else
                {
                    viewHolder.leftMsgLayout.setVisibility(LinearLayout.VISIBLE);

                    if(message_type.equals("text")) {

                        viewHolder.leftMsgTextView.setText(msgDto.getMessage());

                    }
                    viewHolder.rightMsgLayout.setVisibility(LinearLayout.GONE);
                }



//                viewHolder.displayName.setText(name);

//                Picasso.with(viewHolder.profileImage.getContext()).load(image)
//                        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        if(message_type.equals("text")) {
//
//            viewHolder.messageText.setText(c.getMessage());
//            viewHolder.messageImage.setVisibility(View.INVISIBLE);
//
//
//        } else {
//
//            viewHolder.messageText.setVisibility(View.INVISIBLE);
//            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
//                    .placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);
//
//        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
*/