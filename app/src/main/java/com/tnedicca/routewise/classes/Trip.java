package com.tnedicca.routewise.classes;

import android.content.Context;


import com.tnedicca.routewise.R;

/**
 * Created by new on 08-02-2017.
 */

public class Trip {

    private String mapImage;
    private String tripDate;
    private String startPoint;
    private String endPoint;
    private String tripLength;
    private String riskScore;
    private String pathId;

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getMapImage() {
        return mapImage;
    }

    public void setMapImage(Context context, String mapImage) {
        mapImage = mapImage.replace("\"YOUR_KEY\"", context.getResources().getString(R.string.google_static_maps_key));
        this.mapImage = mapImage;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getTripLength() {
        return tripLength;
    }

    public void setTripLength(String tripLength) {
        this.tripLength = tripLength;
    }

    public String getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(String riskScore) {
        this.riskScore = riskScore;
    }
}
