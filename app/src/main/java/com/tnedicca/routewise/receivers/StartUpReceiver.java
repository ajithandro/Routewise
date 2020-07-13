package com.tnedicca.routewise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.fragments.Settings;

import java.util.Calendar;

/**
 * Created by Aachu on 08-02-2017.
 */
public class StartUpReceiver extends BroadcastReceiver {

    private RouteWise mInstance;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private long endTime;
    private RouteLog logger;

    @Override
    public void onReceive(Context context, Intent intent) {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(StartUpReceiver.class);
        mPrefer = context.getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            mInstance.myLibrary.noti(context, "BOOT COMPLETE", "StartUpReceiver", AppConstant.NOTIFY_BOOT_COMPLETE, false);
            mInstance.checkAutoTracking();
            mInstance.checkMotionActivity(true);
            mInstance.startGPSHighPower(true);
        }
    }

}
