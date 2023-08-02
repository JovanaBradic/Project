package com.academy.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddNewUserActivity extends AppCompatActivity {

    EditText firstName, lastName, email, password;
    String firstNameValue, lastNameValue, emailValue, passwordValue;
    Button addNewUser;
    Map <String, Boolean> validationMap;
    public final String REGEX_FIRST_NAME = "[A-Za-z]{2,}";
    public final String REGEX_LAST_NAME = "[A-Za-z]{3,}";
    private final String REGEX_EMAIL = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    public final String REGEX_PASSWORD = "[A-Za-z]{3,}";
    public final String REGEX_SPEC = "[!#$%&*()_+=|<>?{}\\[\\]~-]";
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_user);

        firstName = findViewById(R.id.firstNameField);
        lastName = findViewById(R.id.lastNameField);
        email = findViewById(R.id.emailField);
        password = findViewById(R.id.passwordField);
        addNewUser = findViewById(R.id.addNewUser);

        // back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // back button end

        validationMap = new HashMap<>();
        validationMap.put("firstName", false);
        validationMap.put("lastName", false);
        validationMap.put("email", false);
        validationMap.put("password", false);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        if (!isConnected){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddNewUserActivity.this);
            builder.setMessage("Check your internet connection and try again!");
            builder.setTitle("No internet connection");
            builder.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog, which) -> {
                firstName.setEnabled(false);
                lastName.setEnabled(false);
                email.setEnabled(false);
                password.setEnabled(false);
            });
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.show();
        }else{
            textWatcherAddNewUser();

            addNewUser.setOnClickListener(v -> {
                checkEmailUser();
//                AlertDialog.Builder builder = new AlertDialog.Builder(AddNewUserActivity.this);
//                builder.setMessage("Add new user - " + firstNameValue);
//                builder.setPositiveButton("Confirm", (AlertDialog.OnClickListener) (dialog, which) -> {
//
//                    addNewUser();
//                    AlertDialog.Builder builder1 = new AlertDialog.Builder(AddNewUserActivity.this);
//                    builder1.setMessage("User successfully added");
//                    builder1.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog1, which1) -> {
//                        setResult(201);
//                        finish();
//                    });
//                    AlertDialog alertDialog = builder1.show();
//                });
//                builder.setNeutralButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
//                });
//                builder.setCancelable(false);
//                AlertDialog alertDialog = builder.show();
//
//                TextView messageView = (TextView)alertDialog.findViewById(android.R.id.message);
//                messageView.setGravity(Gravity.CENTER);
//                messageView.setTextSize(18);
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addNewUser(){
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;
            @Override
            public void run() {
                try {
                    sharedPreferences = AddNewUserActivity.this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/users");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Basic " + token);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("email", emailValue);
                    jsonObject.put("password", passwordValue);
                    jsonObject.put("first_name", firstNameValue);
                    jsonObject.put("last_name", lastNameValue);

                    DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                    stream.writeBytes(jsonObject.toString());
                    stream.flush();
                    stream.close();

                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                    responseCode = connection.getResponseCode();

                    StringBuilder response = new StringBuilder();
                    String inputStr;
                    while ((inputStr = reader.readLine()) != null)
                        response.append(inputStr);

                    AddNewUserActivity.this.runOnUiThread(() -> {
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("USER", "Exception" + e.getMessage());
                }
                finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent();
        setResult(201, backIntent);
        super.onBackPressed();
    }

    private void enabledAddNewUserButton(){
        Log.i("MAP", "" + validationMap.toString());
        addNewUser.setEnabled(!validationMap.containsValue(false));
    }

    public void textWatcherAddNewUser(){
        firstNameValue = firstName.getText().toString();
        lastNameValue = lastName.getText().toString();
        emailValue = email.getText().toString();
        passwordValue = password.getText().toString();

        firstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("JOVANA", "" + s.toString());
                firstNameValue = firstName.getText().toString();
                if (firstNameValue.matches(REGEX_FIRST_NAME) && !firstNameValue.equals("")){
                    validationMap.put("firstName", true);
                }else{
                    validationMap.put("firstName", false);
                }
                enabledAddNewUserButton();
            }
        });
        lastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                lastNameValue = lastName.getText().toString();
                if (lastNameValue.matches(REGEX_LAST_NAME) && !lastNameValue.equals("")){
                    validationMap.put("lastName", true);
                }else{
                    validationMap.put("lastName", false);
                }
                enabledAddNewUserButton();
            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                emailValue = email.getText().toString();
                if (emailValue.matches(REGEX_EMAIL) && !emailValue.equals("")){
                    validationMap.put("email", true);
                }else {
                    validationMap.put("email", false);
                }
                enabledAddNewUserButton();
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
                passwordValue = password.getText().toString();
                if (passwordValue.matches(REGEX_PASSWORD) && !password.equals("") && !passwordValue.matches(REGEX_SPEC)){
                    validationMap.put("password", true);
                }else{
                    validationMap.put("password", false);
                }
                enabledAddNewUserButton();
            }
        });
    }

    public void checkEmailUser(){
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;
            final ArrayList<User> userArrayList = new ArrayList<>();
            @Override
            public void run() {
                try {
                    sharedPreferences = AddNewUserActivity.this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/users");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Basic " + token);
                    connection.setDoInput(true);

                    responseCode = connection.getResponseCode();

                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();

                    String string;
                    while ((string = reader.readLine()) != null){
                        response.append(string);
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    for(int i=0; i < jsonArray.length(); i++){
                        JSONObject jsonUser = jsonArray.getJSONObject(i);

                        int id = jsonUser.getInt("id");
                        String firstName = jsonUser.getString("first_name");
                        String lastName = jsonUser.getString("last_name");
                        String email = jsonUser.getString("email");

                        User user = new User(id, firstName, lastName, email);
                        userArrayList.add(user);
                    }

                    AddNewUserActivity.this.runOnUiThread(() -> {
                        boolean isEmailValid = true;
                        String emailUser = "";
                        for (int i=0; i < userArrayList.size(); i++){
                            emailUser = userArrayList.get(i).getEmail();
                            Log.i("BRADIC", "uneta email adresa: " + emailValue);
                            if (emailValue.equals(emailUser)){
                                isEmailValid = false;
                            }
                        }
                        if (isEmailValid){
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddNewUserActivity.this);
                            builder.setMessage("Add new user - " + firstNameValue);
                            builder.setPositiveButton("Confirm", (AlertDialog.OnClickListener) (dialog, which) -> {

                                addNewUser();
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(AddNewUserActivity.this);
                                builder1.setMessage("User successfully added");
                                builder1.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog1, which1) -> {
                                    setResult(201);
                                    finish();
                                });
                                AlertDialog alertDialog = builder1.show();
                            });
                            builder.setNeutralButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
                            });
                            builder.setCancelable(false);
                            AlertDialog alertDialog = builder.show();

                            TextView messageView = (TextView)alertDialog.findViewById(android.R.id.message);
                            messageView.setGravity(Gravity.CENTER);
                            messageView.setTextSize(18);
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddNewUserActivity.this);
                            builder.setMessage("Your email address is already used, please try again");
                            builder.setTitle("Something went wrong");
                            builder.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog, which) -> {

                            });
                            builder.setCancelable(false);
                            AlertDialog alertDialog = builder.show();
                        }
                    });


                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("LOAD", "Exception:" + e.getMessage());
                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        });
        thread.start();
    }
}