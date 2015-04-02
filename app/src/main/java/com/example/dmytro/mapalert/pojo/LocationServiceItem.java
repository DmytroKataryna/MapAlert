package com.example.dmytro.mapalert.pojo;

import android.location.Location;

public class LocationServiceItem {

    private Location location;
    private boolean isInside;

    public LocationServiceItem() {
    }

    public LocationServiceItem(Location location, boolean isInside) {
        this.location = location;
        this.isInside = isInside;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isInside() {
        return isInside;
    }

    public void setInside(boolean isInside) {
        this.isInside = isInside;
    }
}
