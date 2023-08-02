package com.academy.project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class OrganizationFragment extends Fragment {

    ArrayList<Items> itemsArrayList;
    Items item;
    RecyclerView recyclerView;
    MyListAdapter adapter;
    SharedPreferences sharedPreferences;
    FloatingActionButton floatingActionButton;
    boolean isConnected, isHome;
    public OrganizationFragment() {

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organization, container, false);
        sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        recyclerView = view.findViewById(R.id.recyclerViewOrganization);

        if (sharedPreferences.getBoolean("isAdmin", false)){
            checkInternetConnectionAdmin();
        }else{
            checkInternetConnectionUser();
        }

        return view;
    }

    //delete from recycler view
    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            Log.i("BRADIC", "Position: " + position);
            if (position == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Admin can't be deleted from organization list");
                builder.setTitle("Something went wrong");
                builder.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog, which) -> {
                    itemsArrayList.clear();
                    organizationItem();
                });

                builder.setCancelable(false);
                AlertDialog alertDialog = builder.show();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Click confirm to delete user from your organization");
                builder.setTitle("Do you really want to delete user?");
                builder.setPositiveButton("Delete", (AlertDialog.OnClickListener) (dialog, which) -> {
                    if (position == 0){
                        Toast.makeText(requireActivity(), "Admin can't be deleted!", Toast.LENGTH_LONG).show();
                        itemsArrayList.clear();
                        organizationItem();
                    }else{
                        Items itemToDelete = itemsArrayList.get(position);
                        deleteUser(itemToDelete.getId());

                        itemsArrayList.remove(position);
                        adapter.notifyItemRemoved(position);

                        Toast.makeText(getActivity(), "User deleted", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNeutralButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                });
                builder.setCancelable(false);
                AlertDialog alertDialog = builder.show();

                Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                nbutton.setTextColor(Color.RED);
            }

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.RED)
                    .addSwipeLeftActionIcon(R.drawable.baseline_delete_24)
                    .setSwipeLeftLabelColor(ContextCompat.getColor(getActivity(), R.color.white))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
    // delete from recycler view end

    public void organizationItem(){
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;
            @SuppressLint("NotifyDataSetChanged")
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
                        JSONObject itemJson = jsonArray.getJSONObject(i);

                        String firstName = itemJson.getString("first_name");
                        String lastName = itemJson.getString("last_name");
                        int id = itemJson.getInt("id");

                        String fn = String.valueOf(firstName.charAt(0));
                        String ln = String.valueOf(lastName.charAt(0));
                        String initials = fn.toUpperCase() + ln.toUpperCase();

                        item = new Items(id, initials, firstName + " " + lastName);
                        itemsArrayList.add(item);
                    }

                    requireActivity().runOnUiThread(() ->{
                        recyclerView.setAdapter(adapter);
                        adapter.setItems(itemsArrayList);
                        adapter.notifyDataSetChanged();
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("ORGANIZATION", "Exception" + e.getMessage());
                }
                finally {
                    if(connection != null) connection.disconnect();
                }
            }
        });
        thread.start();
    }

    public void deleteUser(int id){
        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;
            @Override
            public void run() {
                try {
                    sharedPreferences = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/users/" + id);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("X-HTTP-Method-Override", "DELETE");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Basic "+ token);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    responseCode = connection.getResponseCode();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("DELETE", "Exception:" + e.getMessage());
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

    public void startAddNewUserActivity(){
        Intent intent = new Intent(getActivity(), AddNewUserActivity.class);
        startActivityForResult(intent, 123);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123){
            if (resultCode == 201){
                itemsArrayList.clear();
                organizationItem();
                Log.i("ACTIVITY", "Usao u if");
            }
        }
    }

    public void checkInternetConnectionAdmin(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            floatingActionButton.setVisibility(View.GONE);
        }else{
            floatingActionButton.setVisibility(View.VISIBLE);
            // floating button - change color of plus
            @SuppressLint("UseCompatLoadingForDrawables")
            Drawable icon = getResources().getDrawable(R.drawable.baseline_add_24);
            Drawable willBeWhite = icon.getConstantState().newDrawable();
            willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            floatingActionButton.setImageDrawable(willBeWhite);
            // floating button - change color of plus end

            floatingActionButton.setOnClickListener(v -> startAddNewUserActivity());

            // swipe and delete (recycler view)
            ItemTouchHelper helper = new ItemTouchHelper(callback);
            if (sharedPreferences.getBoolean("isAdmin", false)) {
                floatingActionButton.setVisibility(View.VISIBLE);
                helper.attachToRecyclerView(recyclerView);
            }
            // swipe and delete (recycler view) end

            itemsArrayList = new ArrayList<>();
            adapter = new MyListAdapter(itemsArrayList, null);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            setHasOptionsMenu(true);

            organizationItem();
        }

    }

    public void checkInternetConnectionUser(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            floatingActionButton.setVisibility(View.GONE);
        }else{
            floatingActionButton.setVisibility(View.GONE);
            itemsArrayList = new ArrayList<>();
            adapter = new MyListAdapter(itemsArrayList, null);

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            setHasOptionsMenu(true);
            organizationItem();
        }

    }
}