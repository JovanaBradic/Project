package com.academy.project;

import androidx.annotation.NonNull;

public class Items {
    String initials;
    String textRecyclerView;
    static int id;
    String firstName;
    String lastName;
    int amount;
    int idRequest;
    String status;
    int categoryId;
    public Items(int id,String initials, String textRecyclerView){
        this.id = id;
        this.initials = initials;
        this.textRecyclerView = textRecyclerView;
    }

    public Items() {

    }

    public int getId(){
        return id;
    }

    public static void setId(int id) {
        Items.id = id;
    }

    public String getInitials() {
        return initials;
    }

    public String getTextRecyclerView() {
        return textRecyclerView;
    }

    public void setTextRecyclerView(String textRecyclerView) {
        this.textRecyclerView = textRecyclerView;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getIdRequest() {
        return idRequest;
    }

    public void setIdRequest(int idRequest) {
        this.idRequest = idRequest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }


    @NonNull
    @Override
    public String toString() {
        return getInitials() + "\t" + getTextRecyclerView() + "\n";
    }
}
