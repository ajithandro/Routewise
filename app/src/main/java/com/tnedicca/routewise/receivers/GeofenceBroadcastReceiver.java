package com.tnedicca.routewise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private Context mContext;
    private RouteWise mInstance;
    private RouteLog logger;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(GeofenceBroadcastReceiver.class);
        mPrefer = mInstance.getBaseContext().getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);

        long currentTime = System.currentTimeMillis() / 1000;
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            logger.info("Geofence Error : " + errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        String geofenceTransitionString = getTransitionString(geofenceTransition);
        // Test that the reported transition was of interest.
        Location location = geofencingEvent.getTriggeringLocation();
        logger.info("Location Geofence Status : " + geofenceTransitionString + "  at location :  " + location);
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            mInstance.activeGeofence = false;
            mInstance.geofenceDetectedTime = currentTime;
            mInstance. regionTime = currentTime;
            mInstance.myLibrary.saveLocation(location, AppConstant.REGION_LOCATION);
            // Send notification and log the transition details.
            mInstance.myLibrary.noti(mContext, geofenceTransitionString, "" + location, AppConstant.NOTIFY_GEOFENCE, false);

            mInstance.checkAutoTracking();
            mInstance.checkMotionActivity(true);
            mInstance.startGPSHighPower(true);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
        } else {
        }
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return mContext.getString(R.string.geo_fence_enter);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return mContext.getString(R.string.geo_fence_exit);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return mContext.getString(R.string.geo_fence_transition);
            default:
                return "unknown_geofence_transition";
        }
    }
}
