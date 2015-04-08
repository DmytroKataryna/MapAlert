package com.example.dmytro.mapalert.pojo;


import java.io.Serializable;
import java.util.TreeSet;

public class LocationTime implements Serializable {

    private Integer dataBaseID;
    private Integer hour;
    private Integer minute;
    private TreeSet<Integer> days;

    public LocationTime() {
    }

    public LocationTime(Integer dataBaseID, Integer hour, Integer minute, TreeSet<Integer> days) {
        this.dataBaseID = dataBaseID;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
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
}
