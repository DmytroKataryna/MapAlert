package com.example.dmytro.mapalert.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;

public class LocationItem implements Serializable {

    static final long serialVersionUID = 5716885753290653044L;

    private String mTitle;
    private List<LocationItemAction> mActions;
    private String mImagePath;

    private boolean timeSelected;
    private TreeSet<Integer> mRepeat;
    private String mTime;

    private double latitude;
    private double longitude;

    public LocationItem() {
    }

    public LocationItem(String mTitle, List<LocationItemAction> mAction, boolean timeSelected, String imagePath, double latitude, double longitude) {
        this.mTitle = mTitle;
        this.mActions = mAction;
        this.mImagePath = imagePath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeSelected = timeSelected;
    }

    public LocationItem(String mTitle, List<LocationItemAction> mAction, boolean timeSelected, String imagePath, TreeSet<Integer> mRepeat, String mTime, double latitude, double longitude) {
        this.mTitle = mTitle;
        this.mActions = mAction;
        this.mImagePath = imagePath;
        this.mRepeat = mRepeat;
        this.mTime = mTime;
        this.timeSelected = timeSelected;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public boolean isTimeSelected() {
        return timeSelected;
    }

    public void setTimeSelected(boolean timeSelected) {
        this.timeSelected = timeSelected;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public List<LocationItemAction> getActions() {
        return mActions;
    }

    public void setActions(List<LocationItemAction> mActions) {
        this.mActions = mActions;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public TreeSet<Integer> getRepeat() {
        return mRepeat;
    }

    public void setRepeat(TreeSet<Integer> mRepeat) {
        this.mRepeat = mRepeat;
    }
}
