package com.tnedicca.routewise.sensorsmodel;

public class ScreenStatus_model {

    int id;
    boolean screen_status;
    long timestamp;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isScreen_status() {
        return screen_status;
    }

    public void setScreen_status(boolean screen_status) {
        this.screen_status = screen_status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
