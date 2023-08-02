package com.academy.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class SecondActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button buttonRefresh;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        buttonRefresh = findViewById(R.id.buttonRefresh);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        sharedPreferences.getBoolean("logged", true);

        checkInternetConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
        builder.setMessage("Are you sure you want to logout of application?");
        builder.setTitle("Logout");
        builder.setPositiveButton("Yes", (AlertDialog.OnClickListener) (dialog, which) -> {
            sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            editor.putBoolean("logged", false);
            editor.apply();

            finish();
        });
        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) ->{
        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.show();

        Button declineButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        declineButton.setTextColor(Color.RED);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.home){
            setFragment(new HomeFragment());
            Objects.requireNonNull(getSupportActionBar()).setTitle("Home");
        }else if(item.getItemId() == R.id.info){
            setFragment(new InfoFragment());
            Objects.requireNonNull(getSupportActionBar()).setTitle("My Info");
        }else{
            setFragment(new OrganizationFragment());
            Objects.requireNonNull(getSupportActionBar()).setTitle("Organization");
        }
        checkInternetConnection();
        return true;
    }

    public void setFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        if(bottomNavigationView.getSelectedItemId() == R.id.home){
            finishAffinity();
        }else if(bottomNavigationView.getSelectedItemId() != R.id.home) {
            setFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.home);
        }
    }

    public void checkInternetConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) SecondActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected && buttonRefresh != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
            builder.setMessage("Check your internet connection and try again!");
            builder.setTitle("No internet connection");
            builder.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog, which) -> {
                buttonRefresh.setVisibility(View.VISIBLE);
            });
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.show();

            buttonRefresh.setOnClickListener(v -> {
                NetworkInfo activeNetworkInfo1 = connectivityManager.getActiveNetworkInfo();
                isConnected = activeNetworkInfo1 != null && activeNetworkInfo1.isConnectedOrConnecting();
                if (isConnected){
                    bottomNavigationView.setSelectedItemId(bottomNavigationView.getSelectedItemId());
                    Toast.makeText(SecondActivity.this, "Connected to internet", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(SecondActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                }
            });
        }else if (buttonRefresh != null){
            buttonRefresh.setVisibility(View.GONE);
        }
    }


}