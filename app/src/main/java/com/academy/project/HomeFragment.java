package com.academy.project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Collections;
import java.util.Objects;

public class HomeFragment extends Fragment implements RecyclerViewClick{

    RecyclerView horizontalRecyclerView, recyclerView;
    LinearLayoutManager linearLayoutManager;
    ArrayList<Items> itemsArrayList;
    ArrayList<CategoryItem> horizontalArrayList = new ArrayList<>();
    ArrayList<User> userArrayList = new ArrayList<>();
    MyListAdapter adapter;
    MyRvAdapter myRvAdapter;
    TextView usageView, textRecyclerView;
    SharedPreferences sharedPreferences;
    CategoryItem categoryItem;
    Button requestTimeOff, buttonRefresh;
    boolean isConnected;
    View view;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        buttonRefresh = view.findViewById(R.id.buttonRefresh);
        requestTimeOff = view.findViewById(R.id.requestTimeOff);

        sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean("isAdmin", false)){
            checkInternetConnection();
        }else {
            checkInternetConnection();
        }
        return view;
    }

    private void loadCategories(){
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {

                try {
                    URL url = new URL("http://api-charm.delsystems.academy/v1/categories");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoInput(true);

                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    StringBuilder response = new StringBuilder();

                    String inputStr;

                    while ((inputStr = reader.readLine()) != null)
                        response.append(inputStr);

                    JSONObject jsonObject = new JSONObject(response.toString());

                    JSONArray items = jsonObject.getJSONArray("items");
                    for(int i=0; i < items.length(); i++){
                        JSONObject item = items.getJSONObject(i);
                        String description = item.getString("description");

                        categoryItem = new CategoryItem(description, 0);
                        horizontalArrayList.add(categoryItem);
                    }

                    requireActivity().runOnUiThread(() -> {
                        myRvAdapter.notifyDataSetChanged();
                        loadUsage();
                    });


                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("HOME", "Exception" + e.getMessage());
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

    public void loadUsage(){
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;
            @Override
            public void run() {
                try {
                    sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/usage");
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

                    while ((inputStr = reader.readLine()) != null)
                        response.append(inputStr);

                    JSONObject jsonObject = new JSONObject(response.toString());

                    JSONArray items = jsonObject.getJSONArray("items");
                    jsonObject = items.getJSONObject(0);

                    for(int i=0; i < horizontalArrayList.size(); i++){
                        String title = horizontalArrayList.get(i).getTitle().replace(" ", "_");

                        if(title.equals("paid_time_off_-_pto")){
                            title = "paid_time_off";
                        }else if(title.equals("Patron_Saint_Day")){
                            title = "patron_saint_day";
                        }

                        categoryItem = horizontalArrayList.get(i);

                        categoryItem(i);
                        maxUsage(i);

                        categoryItem.setUsage(jsonObject.getInt(title));
                    }

                    requireActivity().runOnUiThread(() -> {
                        myRvAdapter.notifyDataSetChanged();
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("USAGE", "Exception:" + e.getMessage());
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

    public void maxUsage(int i){
        if ( i == 0 || i == 1 || i == 2 || i == 3) {
            categoryItem.setMaxUsage(20);
        } else if(i == 4){
            categoryItem.setMaxUsage(30);
        }else{
            categoryItem.setMaxUsage(1);
        }
    }

    public void categoryItem(int i){
        if(i == 0){
            categoryItem.setImage(R.drawable.icons8_clock);
            categoryItem.setBackground(getResources().getColor(R.color.firstItemHorizontal));
            categoryItem.setDaysHoursUsed("hours used");
        }else if(i == 1){
            categoryItem.setImage(R.drawable._99053_computer_icon__1_);
            categoryItem.setBackground(getResources().getColor(R.color.secondItemHorizontal));
            categoryItem.setDaysHoursUsed("days used");
        }else if(i == 2){
            categoryItem.setImage(R.drawable.palm);
            categoryItem.setBackground(getResources().getColor(R.color.thirdItemHorizontal));
            categoryItem.setDaysHoursUsed("days available");
        }else if(i == 3){
            categoryItem.setBackground(getResources().getColor(R.color.fourthItemHorizontal));
            categoryItem.setImage(R.drawable.sick);
            categoryItem.setDaysHoursUsed("days used");
        }else if(i == 4){
            categoryItem.setBackground(getResources().getColor(R.color.fifthItemHorizontal));
            categoryItem.setImage(R.drawable.icons8_home);
            categoryItem.setDaysHoursUsed("days used");
        }else if(i == 5){
            categoryItem.setBackground(getResources().getColor(R.color.sixthItemHorizontal));
            categoryItem.setImage(R.drawable.saint);
            categoryItem.setDaysHoursUsed("day used");
        }
    }

    public void fetchAllRequestTimeOff(){
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;
            @Override
            public void run() {
                try {
                    sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/time-off-requests?page=1,500");
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
                    for (int i=0; i < jsonArray.length(); i++){
                        JSONObject jsonUser = jsonArray.getJSONObject(i);
                        int amount = jsonUser.getInt("amount");
                        int userId = jsonUser.getInt("user_id");
                        int idRequest = jsonUser.getInt("id");
                        String status = jsonUser.getString("status");
                        int categoryId = jsonUser.getInt("category_id");

                        String firstName = "";
                        String lastName = "";
                        String initials = "";

                        Items request = new Items();
                        for(int j = 0; j < userArrayList.size(); j++){
                            int id = userArrayList.get(j).getId();

                            if (id == userId){
                                firstName = userArrayList.get(j).getFirstName();
                                lastName = userArrayList.get(j).getLastName();

                                String fn = String.valueOf(firstName.charAt(0));
                                String ln = String.valueOf(lastName.charAt(0));
                                initials = fn.toUpperCase() + ln.toUpperCase();

                                request.setInitials(initials);
                                request.setFirstName(firstName);
                                request.setLastName(lastName);
                                request.setAmount(amount);

                                if (categoryId == 1){
                                    request.setTextRecyclerView(firstName +" " + lastName + " asks for " + amount + " hours off");
                                }else{
                                    request.setTextRecyclerView(firstName +" " + lastName + " asks for " + amount + " days off");
                                }
                                request.setIdRequest(idRequest);
                                request.setStatus(status);
                                request.setCategoryId(categoryId);
                            }
                        }
                        itemsArrayList.add(request);
                        Collections.reverse(itemsArrayList);
                    }

                    getActivity().runOnUiThread(() -> {
                        if (responseCode == 200){
                            recyclerView.setAdapter(adapter);
                            adapter.setItems(itemsArrayList);
                            adapter.notifyDataSetChanged();
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("BRADIC", "Exception is:" + e.getMessage());
                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        });
        thread.start();
    }

    public void loadUsers(){
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;
            @Override
            public void run() {
                try {
                    sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
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

                    requireActivity().runOnUiThread(() -> {
                        fetchAllRequestTimeOff();
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
    @Override
    public void onItemClick(String firstName, String lastName, int amount, int requestId, String status, int position, int categoryId) {
                if (sharedPreferences.getBoolean("isAdmin", false)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setMessage(firstName + " " + lastName + " wants to take " + amount + " days off");
                    builder.setTitle("Time - off request");
                    builder.setPositiveButton("Approve", (AlertDialog.OnClickListener) (dialog, which) -> {
                        respondTimeOffRequest(requestId, "approved", position);
                        int categoryUsageValue = myRvAdapter.getUsage(categoryId - 1);
                        categoryUsageValue = categoryUsageValue + amount;
                        myRvAdapter.setUsage(categoryUsageValue,  categoryId);
                    });
                    builder.setNegativeButton("Decline", (DialogInterface.OnClickListener) (dialog, which) ->{
                        respondTimeOffRequest(requestId, "denied", position);
                    });
                    builder.setNeutralButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) ->{
                        respondTimeOffRequest(requestId, "pending", position);
                    });
                    builder.setCancelable(false);
                    AlertDialog alertDialog = builder.show();

                    Button declineButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    declineButton.setTextColor(Color.RED);
                }
    }
    private void respondTimeOffRequest(int requestId, String status, int position) {
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;
            @Override
            public void run() {
                try {
                    sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/time-off-requests/" + requestId);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("PATCH");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Basic " + token);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("status", status);

                    DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                    stream.writeBytes(jsonObject.toString());
                    stream.flush();
                    stream.close();

                    responseCode = connection.getResponseCode();

                    requireActivity().runOnUiThread(() -> {
                        itemsArrayList.get(position).setStatus(status);
                        adapter.notifyItemChanged(position);
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("CLICK", "Exception:" + e.getMessage());
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        });
        thread.start();
    }

    public void requestTimeOffActivity(){
        Intent intent = new Intent(getActivity(), RequestTimeOffActivity.class);
        startActivityForResult(intent, 321);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 321){
            if (resultCode == 200){
                itemsArrayList.clear();
                fetchAllRequestTimeOff();
            }else if (resultCode == 1){
                itemsArrayList.clear();
                fetchAllRequestTimeOff();
                Toast.makeText(requireActivity(), "Request sent successfully", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void checkInternetConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected){
            requestTimeOff.setVisibility(View.GONE);
        }else {
            requestTimeOff.setVisibility(View.VISIBLE);
            // horizontal recycler view
            horizontalRecyclerView = view.findViewById(R.id.horizontalRv);
            usageView = view.findViewById(R.id.usage);

            linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            myRvAdapter = new MyRvAdapter(horizontalArrayList);
            horizontalRecyclerView.setLayoutManager(linearLayoutManager);
            horizontalRecyclerView.setAdapter(myRvAdapter);
            // horizontal recycler view end

            itemsArrayList = new ArrayList<>();

            recyclerView = view.findViewById(R.id.recyclerViewHome);
            adapter = new MyListAdapter(itemsArrayList, this);
            textRecyclerView = view.findViewById(R.id.textViewDescription);

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            requestTimeOff = view.findViewById(R.id.requestTimeOff);
            requestTimeOff.setOnClickListener(v -> requestTimeOffActivity());

            loadCategories();
            loadUsers();
        }

    }

}