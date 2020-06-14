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

public class LoginActivity extends AppCompatActivity {

    private EditText login_email;
    private EditText login_password;
    private Button mLogin_btn;
    private ProgressDialog mLoginProgress;
    private Toolbar mtoolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    static String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mtoolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Login Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLoginProgress=new ProgressDialog(this);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password_);
        mLogin_btn =  findViewById(R.id.login_btn);

        mLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = login_email.getText().toString();
                String password = login_password.getText().toString();
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials.");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(email, password);

                }
                else
                    Toast.makeText(LoginActivity.this, "Fill all fields",Toast.LENGTH_SHORT).show() ;

            }
        });

    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {



                            final String current_user_id = mAuth.getCurrentUser().getUid();

                            //getting TOKEN

                           FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                           @Override
                           public void onComplete(@NonNull Task<InstanceIdResult> task) {
                               if (!task.isSuccessful()) {
                                   Log.w("getInstanceId failed", task.getException());
                                   return;
                               }
                                   deviceToken = task.getResult().getToken();

                               //setting TOKEN
                               mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {

                                       if (task.isSuccessful()){

                                           mLoginProgress.dismiss();
                                           Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                           mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                           startActivity(mainIntent);
                                           finish();
                                       }
                                   }
                               });


                           }
                           });

                        } else {
                            mLoginProgress.hide();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }
}
