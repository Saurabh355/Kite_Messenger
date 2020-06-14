package com.example.kitemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText rDisplayName;
    private EditText rEmail;
    private EditText rPassword;
    private Button rCreateBtn;
    private Toolbar mToolbar;
    private ProgressDialog mProgressbar;
    private DatabaseReference mdatabase;
    String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar= findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressbar = new ProgressDialog(this);

        rDisplayName = findViewById(R.id.reg_display_name);
        rEmail = findViewById(R.id.reg_email);
        rPassword=findViewById(R.id.reg_password);
        rCreateBtn=findViewById(R.id.reg_create_btn);

        rCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = rDisplayName.getText().toString();
                String email = rEmail.getText().toString();
                String password = rPassword.getText().toString();

                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email)&& !TextUtils.isEmpty(password)){

                    mProgressbar.setTitle("Registering User");
                    mProgressbar.setMessage("Please wait while we create your account.");
                    mProgressbar.setCanceledOnTouchOutside(false);
                    mProgressbar.show();
                    registerUser( display_name, email, password);
                }
                else
                    Toast.makeText(RegisterActivity.this, "Fill all fields",Toast.LENGTH_SHORT).show() ;

            }
        });

    }

    private void registerUser(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                          //  FirebaseUser user = mAuth.getCurrentUser();
                          FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
                          String Uid= currentUser.getUid();
                          mdatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);

                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w("getInstanceId failed", task.getException());
                                        return;
                                    }
                                    deviceToken = task.getResult().getToken();

                                    HashMap<String, String> userMap = new HashMap<>();
                                    userMap.put("name", display_name);
                                    userMap.put("status", "Hi there, I'm using Kite Messenger.");
                                    userMap.put("image", "default");
                                    userMap.put("thumb_image", "default");
                                    userMap.put("device_token", deviceToken);

                                    mdatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                mProgressbar.dismiss();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();

                                            }

                                        }
                                    });

                                }
                            });




                        } else {
                            mProgressbar.hide();
                            // If sign in fails, display a message to the user
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }
}
