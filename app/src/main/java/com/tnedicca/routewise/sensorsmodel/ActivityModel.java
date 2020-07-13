package com.tnedicca.routewise.sensorsmodel;

public class ActivityModel {

    private int id;
    private  String act_type;
    private int confidence;
    private boolean is_driving;
    private long time;
    private  long sensortime;

    public long getSensortime() {
        return sensortime;
    }

    public void setSensortime(long sensortime) {
        this.sensortime = sensortime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAct_type() {
        return act_type;
    }

    public void setAct_type(String act_type) {
        this.act_type = act_type;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public boolean isIs_driving() {
        return is_driving;
    }

    public void setIs_driving(boolean is_driving) {
        this.is_driving = is_driving;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
