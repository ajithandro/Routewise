package com.tnedicca.routewise.sensorsmodel;

public class BasicModel {

    int id;
    float x;
    float y;
    float z;
    long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    int accuracy;

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

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }








}
