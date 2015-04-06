package com.example.dmytro.mapalert.pojo;

import java.io.Serializable;

public class LocationItemAction implements Serializable {

    private String actionText;
    private boolean state;

    public LocationItemAction() {
    }

    public LocationItemAction(String actionText, boolean state) {
        this.actionText = actionText;
        this.state = state;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
