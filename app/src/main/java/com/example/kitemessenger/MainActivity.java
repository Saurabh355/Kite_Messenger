package com.example.kitemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Kite Messenger");

        if (mAuth.getCurrentUser()!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }


        mViewPager= findViewById(R.id.main_viewPager);
        mSectionPagerAdapter= new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);
        mTabLayout= findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);




    }
        @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
           sendToStart();
         }else {

            if(currentUser!=null){
            mUserRef.child("online").setValue(true);
        }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
        Intent StartIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(StartIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       super.onOptionsItemSelected(item);

       if(item.getItemId()== R.id.main_logout_btn){

           if (mAuth.getCurrentUser() != null) {
               mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
           }
           FirebaseAuth.getInstance().signOut();
           sendToStart();
       }
       if(item.getItemId()== R.id.main_acc_stng_btn){
           Intent SettingIntent = new Intent(MainActivity.this, SettingsActivity.class);
           startActivity(SettingIntent);
        }

        if(item.getItemId()== R.id.All_users_btn){
            Intent SettingIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(SettingIntent);
        }

        return true;
    }
}
