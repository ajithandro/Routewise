package com.tnedicca.routewise.sensorsmodel;

public class Light_details  {
    int id;
    float light;
    long timestamp;
      long sensortime;

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

    public float getLight() {
        return light;
    }

    public void setLight(float light) {
        this.light = light;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
