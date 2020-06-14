package com.example.kitemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    //layout
    private TextView mDisplayName;
    private TextView mStatus;
    private CircleImageView mProfileImg;
    private Button statusBtn;
    private Button imgBtn;
    private Toolbar mToolbar;
    private static final int gallery_pic = 1;

    //Firebase_Storage
    private StorageReference mStorageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
//----------------------------------------
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser()!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        //--------------------------

        mDisplayName = findViewById(R.id.settings_display_name);
        mStatus = findViewById(R.id.settings_status_id);
        mProfileImg = findViewById(R.id.settings_profile_img);
        statusBtn = findViewById(R.id.settings_status_btn);
        imgBtn = findViewById(R.id.setting_img_btn);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mToolbar= findViewById(R.id.setting_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser!=null) {
            String Uid = currentUser.getUid();
           userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);
        }
        userDatabase.keepSynced(true);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);

            if(dataSnapshot.child("image").getValue().toString().equals("default") == false) {
                  Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                          .placeholder(R.drawable.default_profile_pic).into(mProfileImg, new Callback() {
                      @Override
                      public void onSuccess() {

                      }

                      @Override
                      public void onError() {

                          Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_profile_pic).into(mProfileImg);

                      }
                  });
              }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = mStatus.getText().toString();

                Intent status_intent = new Intent(SettingsActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value", status_value);
                startActivity(status_intent);

            }
        });

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent gal_intent = new Intent();
                gal_intent.setType("image/*");
                gal_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gal_intent, "SELECT IMAGE"),gallery_pic );
            */
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();




                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                File thumb_FilePath= new File(resultUri.getPath());
                String mCurrentUser = currentUser.getUid();


                try {
                    Bitmap thumb_Image = new Compressor(this)
                            .setMaxWidth(100)
                            .setMaxHeight(100)
                            .setQuality(75)
                            .compressToBitmap(thumb_FilePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_Image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumbs").child(mCurrentUser+ ".jpg");
                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> thumb_downloaduri = taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> thumb_task) {
                                    if (thumb_task.isSuccessful()) {
                                        Uri thumb_download = thumb_task.getResult();
                                        userDatabase.child("thumb_image").setValue(thumb_download.toString());
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "looks like an Error occured in Thumbnail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


                StorageReference file_path = mStorageRef.child("profile_images").child(mCurrentUser + ".jpg");
                file_path.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> downloaduri = taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    mProgressDialog.dismiss();
                                    Uri download = task.getResult();
                                    userDatabase.child("image").setValue(download.toString());
                                    Toast.makeText(SettingsActivity.this, "Profile Image Changed!", Toast.LENGTH_LONG).show();
                                } else {

                                    Toast.makeText(SettingsActivity.this, "looks like an Error occured", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }




}
