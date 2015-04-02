package com.example.dmytro.mapalert.pojo;

import java.io.Serializable;

public class LocationServiceItem implements Serializable {

    private LocationItem locationItem;
    private boolean inside;

    public LocationServiceItem() {
    }

    public LocationServiceItem(LocationItem locationItem, boolean inside) {
        this.locationItem = locationItem;
        this.inside = inside;
    }

    public LocationItem getLocationItem() {
        return locationItem;
    }

    public void setLocationItem(LocationItem locationItem) {
        this.locationItem = locationItem;
    }


    public boolean isInside() {
        return inside;
    }

    public void setInside(boolean isInside) {
        this.inside = isInside;
    }
}
