package com.tnedicca.routewise.helper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.DetectedActivity;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.receivers.ActivityReciever;

import java.util.ArrayList;

public class MotionActivityHelper {

    private final Context mContext;
    private final RouteLog logger;
    private PendingIntent mActivityPendingIntent;

    public MotionActivityHelper(Context context) {
        mContext = context;
        logger = new RouteLog();
        logger.setLoggerClass(MotionActivityHelper.class);
    }

    public PendingIntent getActivityPendingIntent() {
        if (mActivityPendingIntent != null) {
            return mActivityPendingIntent;
        }
        Intent intent = new Intent(mContext, ActivityReciever.class);
        intent.setAction(ActivityReciever.INTENT_ACTION);
        mActivityPendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mActivityPendingIntent;
    }

    private ArrayList<ActivityTransition> getTransitions() {
        ArrayList<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(transitionBuilder(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
        transitions.add(transitionBuilder(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
        transitions.add(transitionBuilder(DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
        transitions.add(transitionBuilder(DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
//        transitions.add(transitionBuilder(DetectedActivity.ON_FOOT, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
//        transitions.add(transitionBuilder(DetectedActivity.ON_FOOT, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
        transitions.add(transitionBuilder(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
        transitions.add(transitionBuilder(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
//        transitions.add(transitionBuilder(DetectedActivity.UNKNOWN, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
//        transitions.add(transitionBuilder(DetectedActivity.UNKNOWN, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
//        transitions.add(transitionBuilder(DetectedActivity.TILTING, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
//        transitions.add(transitionBuilder(DetectedActivity.TILTING, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
        transitions.add(transitionBuilder(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
        transitions.add(transitionBuilder(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
        transitions.add(transitionBuilder(DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_ENTER));
        transitions.add(transitionBuilder(DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_EXIT));
        return transitions;
    }

    private ActivityTransition transitionBuilder(int type, int activityTransition) {
        return new ActivityTransition.Builder()
                .setActivityType(type)
                .setActivityTransition(activityTransition)
                .build();
    }
}
