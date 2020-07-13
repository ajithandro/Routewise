package com.tnedicca.routewise.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;


import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.sensorsmodel.ActivityModel;

import java.util.Calendar;
import java.util.List;

public class ActivityReciever extends BroadcastReceiver {

    public static final String INTENT_ACTION = "com.tnedicca.routewise.receivers" + ".ACTION_PROCESS_ACTIVITY_TRANSITIONS";
    private RouteWise mInstance;
    private RouteLog logger;
    private DetectedActivity mCurrentActivity;

    public SharedPreferences mPrefer;
    private List<DetectedActivity> mCurrentActivities;
    private int currentActivityType;
    private SharedPreferences.Editor edit;

    @Override
    public void onReceive(Context context, Intent intent) {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(ActivityReciever.class);
        mPrefer = mInstance.getBaseContext().getSharedPreferences(AppConstant.PREFERENCE, context.MODE_PRIVATE);
        mInstance.activityDetectedTime = System.currentTimeMillis() / 1000;
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleMotion(context, result);
        }

    }

    private void handleMotion(Context context, ActivityRecognitionResult result) {
        long currentTime = System.currentTimeMillis() / 1000;
        long lastLocTime = mPrefer.getLong(AppConstant.LAST_LOC_TIME, 0);
        boolean stopTimer = mPrefer.getBoolean(AppConstant.IS_RECORD_STOP_TIMER_SET, false);
        mCurrentActivities = result.getProbableActivities();
        for (DetectedActivity event : mCurrentActivities) {
            String name = mInstance.myLibrary.getActivityName(event);
            int rawConfidence = event.getConfidence();
            logger.info("ACTIVITIES  Available: " + name + "   CONFIDENCE : " + rawConfidence);
            ActivityModel activityModel=new ActivityModel();
            activityModel.setAct_type("ActivityRecognitionResult");
            activityModel.setConfidence(rawConfidence);
            activityModel.setIs_driving(true);
            activityModel.setTime(currentTime);
            mInstance.myLibrary.storeactivities(activityModel);
        }
        mCurrentActivity = result.getMostProbableActivity();
        int rawConfidence = mCurrentActivity.getConfidence();
        currentActivityType = mCurrentActivity.getType();
        String name = mInstance.myLibrary.getActivityName(mCurrentActivity);
        logger.info("ACTIVITY : " + currentActivityType + "   CONFIDENCE : " + rawConfidence + "   NAME : " + name);

        boolean isConfident = false;
        if (rawConfidence >= AppConstant.ACTIVITY_CONFIDENCE) {
            isConfident = true;
        }

        boolean isDriving = false;
        String activityText = "---";
        if (isConfident) {
            if (currentActivityType == DetectedActivity.IN_VEHICLE) {
                isDriving = true;
            } else if (currentActivityType != DetectedActivity.IN_VEHICLE) {
                isDriving = false;
            }
        }
        if (isDriving) {
            mInstance.drivingActivity = true;
            mInstance.recordingTrip = true;
            mInstance.removeGeofence();
            mInstance.lastDrivingTime = currentTime;
            logger.info("Activity : Automotive");
            sendActivityNotification(context, "MOTION", "Started Driving Is it right? Else please report...", AppConstant.NOTIFY_ACT_RECIEVER, false);
            activityText = "Driving";
        } else {
            mInstance.drivingActivity = false;
            activityText = getActivityString(context, currentActivityType);
            sendActivityNotification(context, "MOTION", "You are " + activityText + " Now :) Else please report...", AppConstant.NOTIFY_ACT_RECIEVER, false);
        }
        mInstance.lastActivityType = currentActivityType;
        logger.info("handleMotion  : " + mInstance.drivingActivity + "  " + activityText + "  " + rawConfidence + "  " + mCurrentActivity);

        long locDiff = currentTime - lastLocTime;
        int splitTime = AppConstant.LOC_SPLIT_INTERVAL + 10;
        logger.info("locDiff  : " + locDiff + "  currentTime : " + currentTime + "  lastLocTime : " + lastLocTime);

        if (!mInstance.drivingActivity && mInstance.recordingTrip) {
            if (!stopTimer) {
                int endTime = AppConstant.LOC_SPLIT_INTERVAL * 1000;
                mInstance.setAlarm(context, (Calendar.getInstance().getTimeInMillis()) + endTime, AppConstant.TRIP_STOP_TIMER, AppConstant.TRIP_STOP_TIMER_CODE);
                stopTimer = true;
            }
        }
        if (mInstance.drivingActivity && stopTimer) {
            mInstance.removeAlarm(context, AppConstant.TRIP_STOP_TIMER_CODE);
            stopTimer = false;
        }

        edit = mPrefer.edit();
        edit.putLong(AppConstant.LAST_DRIVING_TIME, mInstance.lastDrivingTime);
        edit.putBoolean(AppConstant.IS_RECORDING_TRIP, mInstance.recordingTrip);
        edit.putBoolean(AppConstant.IS_RECORD_STOP_TIMER_SET, stopTimer);
        edit.apply();
    }

    private void sendActivityNotification(Context context, String title, String name, int id, boolean ongoing) {
        if (mInstance.lastActivityType > 99 || currentActivityType != mInstance.lastActivityType) {
            mInstance.myLibrary.noti(context, title, name, id, ongoing);
        }
    }

    static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch (detectedActivityType) {
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.vehicle);
            default:
                return resources.getString(R.string.unknown_activity);
        }
    }
}
