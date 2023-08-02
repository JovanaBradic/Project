package com.academy.project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    ArrayList<Items> itemsArrayList;
    RecyclerViewClick recyclerViewClick;

    boolean isHome = false;

    //zadrzi set
    //organization set first name, setLast name
    //novi recycler view

    public MyListAdapter(ArrayList<Items> itemsArrayList, RecyclerViewClick recyclerViewClick){
       // this.itemsArrayList = itemsArrayList;
        this.recyclerViewClick = recyclerViewClick;
    }

    public void setItems(ArrayList<Items> itemsArrayList){
        this.itemsArrayList = itemsArrayList;
    }

    @NonNull
    @Override
    public MyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.recycler_view, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListAdapter.ViewHolder holder, int position) {
        Items item = itemsArrayList.get(position);
        Log.i("LOG", "" + item);

        holder.textViewInitials.setText(item.getInitials());
        holder.textViewDescription.setText(item.getTextRecyclerView());

        holder.cardView.setOnClickListener(v -> {
            if (recyclerViewClick != null) {
                recyclerViewClick.onItemClick(item.getFirstName(), item.getLastName(), item.getAmount(), item.getIdRequest(), item.getStatus(),
                        position, item.getCategoryId());
            }
        });

        if (recyclerViewClick != null){
            String status = item.getStatus();
            if (status != null){
                switch (status) {
                    case "denied":
                        holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#ed9987")));
                        break;
                    case "approved":
                        holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#bfe8be")));
                        break;
                    case "pending":
                        holder.cardView.setCardBackgroundColor(Color.WHITE);
                        break;
                }
            }

        }

    }

    @Override
    public int getItemCount() {
        return itemsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textViewInitials, textViewDescription;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textViewInitials = itemView.findViewById(R.id.textViewInitials);
            this.textViewDescription = itemView.findViewById(R.id.textViewDescription);
            this.cardView = itemView.findViewById(R.id.cardView);
        }
    }

}
