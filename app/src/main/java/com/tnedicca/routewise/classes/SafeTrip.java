package com.tnedicca.routewise.classes;

import android.content.Context;

import com.tnedicca.routewise.R;


import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Aachu on 22-02-2017.
 */
public class SafeTrip {

    ArrayList<String> navigationArray;
    ArrayList<JSONObject> distanceArray;
    String url;
    String routeName;
    String riskFactor;
    boolean safe;
    String finalDur;
    String finalDis;
    int duration;
    JSONObject currentRoute;

    public ArrayList<String> getNavigationArray() {
        return navigationArray;
    }

    public void setNavigationArray(ArrayList<String> navigationArray) {
        this.navigationArray = navigationArray;
    }

    public ArrayList<JSONObject> getDistanceArray() {
        return distanceArray;
    }

    public void setDistanceArray(ArrayList<JSONObject> distanceArray) {
        this.distanceArray = distanceArray;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public JSONObject getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(JSONObject currentRoute) {
        this.currentRoute = currentRoute;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(Context context, String url) {
        url = url.replace("\"YOUR_KEY\"", context.getResources().getString(R.string.google_static_maps_key));
        this.url = url;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(String riskFactor) {
        this.riskFactor = riskFactor;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public String getFinalDur() {
        return finalDur;
    }

    public void setFinalDur(String finalDur) {
        this.finalDur = finalDur;
    }

    public String getFinalDis() {
        return finalDis;
    }

    public void setFinalDis(String finalDis) {
        this.finalDis = finalDis;
    }
}
