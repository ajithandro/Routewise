package com.tnedicca.routewise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;

/**
 * Created by new on 21-02-2017.
 */

public class TimerExpiredReceiver extends BroadcastReceiver {

    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private RouteWise mInstance;
    private RouteLog logger;

    @Override
    public void onReceive(Context context, Intent intent) {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(TimerExpiredReceiver.class);

        logger.info("Received response in TimerExpiredReceiver to restart tracking service");
        mPrefer = context.getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);

        Bundle extra = intent.getExtras();
        boolean is_trip_stop = extra.getBoolean(AppConstant.TRIP_STOP_TIMER);
        boolean auto_track = extra.getBoolean(AppConstant.AUTO_TRACK);
        edit = mPrefer.edit();
        if (is_trip_stop) {
            mInstance.myLibrary.noti(context, AppConstant.DEFAULT_DB_NAME, "Alarm triggered for trip closing", AppConstant.NOTIFY_AUTO_ALARM_STARTED, true);
            mInstance.invokeTimer();
            edit.putBoolean(AppConstant.IS_RECORD_STOP_TIMER_SET, false);
        } else if (auto_track) {
            mInstance.myLibrary.noti(context, AppConstant.DEFAULT_DB_NAME, "Alarm triggered for auto Tracking", AppConstant.NOTIFY_AUTO_ALARM_STARTED, true);
            edit.remove(AppConstant.AUTO_ENABLE_TIME_START);
            edit.remove(AppConstant.AUTO_ENABLE_TIME);
        }
        edit.apply();
    }
}
