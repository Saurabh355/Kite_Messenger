package com.example.kitemessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button start_reg_btn;
    private Button start_log_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        start_reg_btn= findViewById(R.id.Start_reg_btn);
        start_reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


        start_log_btn= findViewById(R.id.start_login_btn);
        start_log_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });


    }
}
