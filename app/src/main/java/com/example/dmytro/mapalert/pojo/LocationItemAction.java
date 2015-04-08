package com.example.dmytro.mapalert.pojo;

import java.io.Serializable;

public class LocationItemAction implements Serializable {

    private String actionText;
    private boolean done;

    public LocationItemAction() {
        this.actionText = "";
        this.done = false;
    }

    public LocationItemAction(String actionText, boolean state) {
        this.actionText = actionText;
        this.done = state;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
