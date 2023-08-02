package com.academy.project;

public class CategoryItem {
    String title;
    Integer usage;
    int maxUsage;
    int image;
    int background;
    String titleDH;
    int categoryId;
    public CategoryItem(String title, Integer usage){
        this.title = title;
        this.usage = usage;
    }

    public String getTitle() {
        return title;
    }
    public Integer getUsage() { return usage; }
    public void setUsage(int usage) {
        this.usage = usage;
    }

    public void setMaxUsage(int maxUsage){
        this.maxUsage = maxUsage;
    }
    public int getMaxUsage() {
        return maxUsage;
    }

    public void setImage(int image){
        this.image = image;
    }
    public int getImage() {
        return image;
    }

    public int getBackground(){
        return background;
    }
    public void setBackground(int background){
        this.background = background;
    }

    public void setDaysHoursUsed(String titleDH){
        this.titleDH = titleDH;
    }
    public String getTitleDH() {
        return titleDH;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "CategoryItem{" +
                "title='" + title + '\'' +
                ", usage=" + usage +
                '}';
    }
}
