package com.academy.project;

import androidx.annotation.NonNull;

public class HorizontalItem {
    String image;
    String title;
    int used;
    String description;

    public HorizontalItem(String image, String title, int used, String description){
        this.image = image;
        this.title = title;
        this.used = used;
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return image + title + used + description;
    }
}
