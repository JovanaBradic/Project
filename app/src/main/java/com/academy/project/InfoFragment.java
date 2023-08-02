package com.academy.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class InfoFragment extends Fragment {

    Switch simpleSwitch;
    EditText email, address, collage, contact, dateOfBirth;
    TextView firstName, lastName, hireDate;
    String emailValue, addressValue, collageValue, contactValue;
    Button saveChanges;
    int userID;
    private final String REGEX_EMAIL = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private final String REGEX_ADDRESS = "[a-zčćšđžA-ZČĆŠĐŽ0-9., ]{3,}+";
    private final String REGEX_COLLAGE = "[a-zA-Z ]+";
    private final String REGEX_CONTACT = "[0-9]{10}";
    SharedPreferences sharedPreferences;
    boolean isConnected;
    ConstraintLayout constraintLayout;
    View view;

    public InfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info, container, false);
        firstName = view.findViewById(R.id.firstNameField);
        lastName = view.findViewById(R.id.lastNameField);
        email = view.findViewById(R.id.emailField);
        dateOfBirth = view.findViewById(R.id.dateOfBirthField);
        address = view.findViewById(R.id.addressField);
        collage = view.findViewById(R.id.collageField);
        hireDate = view.findViewById(R.id.hireDateField);
        contact = view.findViewById(R.id.contactField);
        saveChanges = view.findViewById(R.id.saveChanges);
        simpleSwitch = view.findViewById(R.id.switch1);
        constraintLayout = view.findViewById(R.id.layoutInfo);

        checkInternetConnection();
        return view;
    }

    private boolean isValid() {
        boolean isValid = true;

        emailValue = email.getText().toString();
        addressValue = address.getText().toString();
        collageValue = collage.getText().toString();
        contactValue = contact.getText().toString();

        if (!emailValue.matches(REGEX_EMAIL)) {
            isValid = false;
        } else if (!addressValue.matches(REGEX_ADDRESS)) {
            isValid = false;
        } else if (!collageValue.matches(REGEX_COLLAGE)) {
            isValid = false;
        } else if (!contactValue.matches(REGEX_CONTACT)) {
            isValid = false;
        }
        return isValid;
    }

    public void personalInfo() {
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;

            @Override
            public void run() {
                try {
                    sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/users/profile");
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

                    String inputStr;

                    while ((inputStr = reader.readLine()) != null) {
                        response.append(inputStr);
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());

                    String dob = jsonObject.getString("dob");

                    DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    DateFormat newFormat = new SimpleDateFormat("dd.MM.yy.");
                    Date date = originalFormat.parse(dob);
                    String newFormatedDate = newFormat.format(date);

                    String hireDateValue = jsonObject.getString("hire_date");

                    Date hireDateFormat = originalFormat.parse(hireDateValue);
                    String newFormatedHireDate = newFormat.format(hireDateFormat);

                    requireActivity().runOnUiThread(() -> {
                        try {
                            firstName.setText(jsonObject.getString("first_name"));
                            lastName.setText(jsonObject.getString("last_name"));
                            email.setText(jsonObject.getString("email"));
                            dateOfBirth.setText(newFormatedDate);
                            address.setText(jsonObject.getString("address"));
                            collage.setText(jsonObject.getString("degree"));
                            hireDate.setText(newFormatedHireDate);
                            contact.setText(jsonObject.getString("phone_number"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("INFO", "Exception" + e.getMessage());
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
        });
        thread.start();
    }

    public void simpleSwitchClick() {
        simpleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setEnabledField();
                textWatcher();
                setBackgroundField();
            } else {
                setEnabledField();
                setBackgroundField();
            }
        });
    }

    public void updateUser(int userID) {
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;

            @Override
            public void run() {
                try {
                    sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/users/" + userID);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PATCH");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Basic " + token);
                    connection.setRequestProperty("ID", "" + userID);

                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("email", emailValue);
                    jsonObject.put("address", addressValue);
                    jsonObject.put("degree", collageValue);
                    jsonObject.put("phone_number", contactValue);

                    DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                    stream.writeBytes(jsonObject.toString());
                    stream.flush();
                    stream.close();

                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                    StringBuilder response = new StringBuilder();
                    String inputStr;
                    while ((inputStr = reader.readLine()) != null)
                        response.append(inputStr);

                    responseCode = connection.getResponseCode();

                    requireActivity().runOnUiThread(() -> {
                        try {
                            email.setText(jsonObject.getString("email"));
                            address.setText(jsonObject.getString("address"));
                            collage.setText(jsonObject.getString("degree"));
                            contact.setText(jsonObject.getString("phone_number"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }
            }
        });
        thread.start();
    }

    public void checkInternetConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected){
            constraintLayout.setVisibility(View.GONE);
            saveChanges.setEnabled(false);
        }else{

            personalInfo();
            simpleSwitchClick();

            sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            userID = sharedPreferences.getInt("id", 0);

            saveChanges.setOnClickListener(v -> {
                updateUser(userID);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Info updated");
                builder.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog, which) -> {
                    simpleSwitch.setChecked(false);
                });
                builder.setCancelable(false);
                builder.setCancelable(false);

                AlertDialog alertDialog = builder.show();

                TextView messageView = (TextView) alertDialog.findViewById(android.R.id.message);
                messageView.setGravity(Gravity.CENTER);
            });
        }
    }

    public void textWatcher(){
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("WATCHER", "Before:" + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("WATCHER", "On text:" + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("WATCHER", "After:" + s);
                saveChanges.setEnabled(isValid());
            }
        });
        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveChanges.setEnabled(isValid());
            }
        });
        collage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveChanges.setEnabled(isValid());
            }
        });
        contact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveChanges.setEnabled(isValid());
            }
        });
    }

    public void setEnabledField(){
        if (simpleSwitch.isChecked()){
            email.setEnabled(true);
            address.setEnabled(true);
            collage.setEnabled(true);
            contact.setEnabled(true);
            saveChanges.setEnabled(true);
        }else{
            email.setEnabled(false);
            address.setEnabled(false);
            collage.setEnabled(false);
            contact.setEnabled(false);
            saveChanges.setEnabled(false);
        }
    }

    public void setBackgroundField(){
        if (simpleSwitch.isChecked()){
            email.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.enableColor));
            address.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.enableColor));
            collage.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.enableColor));
            contact.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.enableColor));
        }else{
            email.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray));
            address.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray));
            collage.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray));
            contact.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray));
        }
    }
}