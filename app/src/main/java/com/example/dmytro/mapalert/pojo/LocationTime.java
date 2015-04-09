package com.example.dmytro.mapalert.pojo;


import java.io.Serializable;
import java.util.TreeSet;

public class LocationTime implements Serializable {

    private Integer dataBaseID;
    private Integer hour;
    private Integer minute;
    private TreeSet<Integer> days;
    private String title;
    private String imagePath;

    public LocationTime() {
    }

    public LocationTime(Integer dataBaseID, Integer hour, Integer minute, TreeSet<Integer> days, String title, String imagePath) {
        this.dataBaseID = dataBaseID;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
        this.title = title;
        this.imagePath = imagePath;
    }

    public Integer getDataBaseID() {
        return dataBaseID;
    }

    public void setDataBaseID(Integer dataBaseID) {
        this.dataBaseID = dataBaseID;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public TreeSet<Integer> getDays() {
        return days;
    }

    public void setDays(TreeSet<Integer> days) {
        this.days = days;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
