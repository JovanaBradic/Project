package com.academy.project;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyRvAdapter extends RecyclerView.Adapter<MyRvAdapter.MyHolder> {
    ArrayList<CategoryItem> data;
    CategoryItem categoryItem;

    public MyRvAdapter(ArrayList<CategoryItem> data){
        this.data = data;
        Log.i("TESTERKO", "data: " + data.size());
    }

//    public void setData(ArrayList<CategoryItem> data){
//        this.data = data;
//    }

    @NonNull
    @Override
    public MyRvAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_rv_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRvAdapter.MyHolder holder, int position) {
        categoryItem = data.get(position);
        Log.i("JOVANA", "items: " + categoryItem );
        holder.title.setText(categoryItem.getTitle());

        String usageText = categoryItem.getUsage() + "/" + categoryItem.getMaxUsage() + "\n" + categoryItem.getTitleDH() + " ";
        holder.usage.setText(usageText);
        holder.image.setImageResource(categoryItem.getImage());
        holder.horizontalItem.setBackgroundColor(categoryItem.getBackground());

    }

    public void setUsage(int usage, int categoryId){
        int position = categoryId - 1;
        data.get(position).setUsage(usage);
        notifyItemChanged(categoryId - 1);
    }
    public int getUsage(int categoryId){
        return data.get(categoryId).getUsage();
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        TextView title, usage;
        ImageView image;

        RelativeLayout horizontalItem;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            usage = itemView.findViewById(R.id.usage);
            image = itemView.findViewById(R.id.image);
            horizontalItem = itemView.findViewById(R.id.horizontalItem);
        }
    }

}
