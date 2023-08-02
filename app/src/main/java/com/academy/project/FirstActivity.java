package com.academy.project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class FirstActivity extends AppCompatActivity {
    Button logInButton, buttonRefresh;
    EditText email, password;
    private final String REGEX_EMAIL = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    public final String REGEX_PASSWORD = "^[a-z0-9]{3,}$";
    public final String REGEX_SPEC = "[!#$%&*()_+=|<>?{}\\[\\]~-]";
    String emailValue, passwordValue, def;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectivityManager connectivityManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        buttonRefresh = findViewById(R.id.buttonRefresh);
        email = findViewById(R.id.emailField);
        password = findViewById(R.id.passwordField);
        logInButton = findViewById(R.id.logInButton);


        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected){
            AlertDialog.Builder builder = new AlertDialog.Builder(FirstActivity.this);
            builder.setMessage("Check your internet connection and try again!");
            builder.setTitle("No internet connection");
            builder.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog, which) -> {
                email.setEnabled(false);
                password.setEnabled(false);
                buttonRefresh.setVisibility(View.VISIBLE);
                logInButton.setVisibility(View.GONE);
            });
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.show();

            buttonRefresh.setOnClickListener(v -> {
                ConnectivityManager connectivityManager = (ConnectivityManager) FirstActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo1 = connectivityManager.getActiveNetworkInfo();
                boolean isConnected1 = activeNetworkInfo1 != null && activeNetworkInfo1.isConnectedOrConnecting();

                if (isConnected1){
                    email.setEnabled(true);
                    password.setEnabled(true);
                    buttonRefresh.setVisibility(View.GONE);
                    logInButton.setVisibility(View.VISIBLE);
                    Toast.makeText(FirstActivity.this, "Connected to internet", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(FirstActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                }
            });
        }else {
            buttonRefresh.setVisibility(View.GONE);
            textWatcherLogIn();

            sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            boolean pref = sharedPreferences.getBoolean("logged", false);
            def = sharedPreferences.getString("token", "");
            int userID = sharedPreferences.getInt("id", 0);

            if (!def.isEmpty() && pref) {
                startActivity();
            }

            logInButton.setOnClickListener(v ->
//                createLoginRequest()
                createLoginRequest2()
            );
        }
    }

    private boolean isValid() {
        boolean isValid = true;

        emailValue = email.getText().toString();
        passwordValue = password.getText().toString();

        if (!emailValue.matches(REGEX_EMAIL)) {
            isValid = false;
        } else if (!passwordValue.matches(REGEX_PASSWORD)) {
            isValid = false;
        } else if (emailValue.matches(REGEX_SPEC)) {
            isValid = false;
        } else if (passwordValue.matches(REGEX_SPEC)) {
            isValid = false;
        }
        return isValid;
    }

    public void startActivity() {
        Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
        startActivity(intent);
    }

    private void createLoginRequest() {
        //kad je reponse 200 setuj share pref
        //async task
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                try {
                    URL url = new URL("http://api-charm.delsystems.academy/v1/login");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    Log.i("STATUS", "Connection" + connection.getRequestMethod());

                    JSONObject json = new JSONObject();
                    json.put("email", emailValue);
                    json.put("password", passwordValue);
                    Log.i("STATUS", "JSON params are:" + json);

                    DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                    stream.writeBytes(json.toString());
                    stream.flush();
                    stream.close();
                    Log.i("STATUS", String.valueOf(connection.getResponseCode()));

                    int responseCode = connection.getResponseCode();
                    FirstActivity.this.runOnUiThread(() -> {
                        if (responseCode == 200) {
                            startActivity();
                        } else {
                            alertDialog();
                        }
                    });

                } catch (
                        Exception e) {
                    e.printStackTrace();
                    Log.i("STATUS", "" + e.getMessage());
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
        });
        thread.start();
    }
    public void alertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FirstActivity.this);
        builder.setMessage("Check your email and password and try again");
        builder.setTitle("Invalid credentials");
        builder.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog, which) -> {

        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.show();
    }

    public void textWatcherLogIn(){
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("WATCHER", "" + s);
                logInButton.setEnabled(isValid());
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                logInButton.setEnabled(isValid());
            }
        });
    }

    private void createLoginRequest2(){
        new AsyncTaskRunner().execute((Void[]) null);
    }
    private class AsyncTaskRunner extends AsyncTask<Void, Void, String> {
        HttpURLConnection connection = null;
        int responseCode;
        String token = "";
        Integer userID = 0;
        String firstName = "";
        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url = new URL("http://api-charm.delsystems.academy/v1/login");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", emailValue);
                jsonObject.put("password",passwordValue);

                DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                stream.writeBytes(jsonObject.toString());
                stream.flush();
                stream.close();

                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                responseCode = connection.getResponseCode();

                if(responseCode == 200) {
                    StringBuilder response = new StringBuilder();
                    String inputStr;
                    while ((inputStr = reader.readLine()) != null)
                        response.append(inputStr);
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    token = jsonResponse.getString("token");
                    userID = jsonResponse.getInt("id");
                    firstName = jsonResponse.getString("first_name");
                }

            }catch (Exception e){
                e.printStackTrace();
                connection.disconnect();
            }
            return token;
        }

        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            if (!TextUtils.isEmpty(token)){
                editor = sharedPreferences.edit();
                editor.putBoolean("logged", true);
                editor.putString("token", token);
                editor.putInt("id", userID);
                editor.putBoolean("isAdmin", Objects.equals(firstName, "Admin"));
                editor.apply();

                startActivity();
            }else{
                alertDialog();
            }
        }
    }
}
