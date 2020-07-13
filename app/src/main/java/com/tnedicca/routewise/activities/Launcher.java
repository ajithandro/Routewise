package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;

/**
 * Created by Vishal on 30-12-2016.
 */


public class Launcher extends Activity {

    public static RouteWise mInstance;
    private int lineNumber;
    private boolean keepMeLoggedIn;
    public SharedPreferences mPrefer;
    private Editor edit;

    private DisplayMetrics mMetrics;
    private int mScreenDensity;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mSdk;
    private RouteLog logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Launcher.class);
        
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        keepMeLoggedIn = mPrefer.getBoolean(AppConstant.KEEP_ME_LOGGED_IN, false);

        mSdk = Build.VERSION.SDK_INT;
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        mScreenDensity = getResources().getDisplayMetrics().densityDpi;
        mScreenWidth = mMetrics.widthPixels;
        mScreenHeight = mMetrics.heightPixels;

        edit = mPrefer.edit();
        edit.putInt(AppConstant.SDK, mSdk);
        edit.putInt(AppConstant.SCREEN_DENSITY, mScreenDensity);
        edit.putInt(AppConstant.SCREEN_WIDTH, mScreenWidth);
        edit.putInt(AppConstant.SCREEN_HEIGHT, mScreenHeight);
        edit.commit();

        try {
            Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable e) {
                    handleUncaughtException(thread, e);
                }
            });
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Sending thread to reporting activity due to unhandled exception
    public void handleUncaughtException(Thread thread, Throwable e) {
        logger.info("\n\n--------------Un Handled Exception Start-----------------\n");
        StackTraceElement[] error = e.getStackTrace();
        String message = e.getMessage();
        lineNumber = mInstance.myLibrary.getLineNumber((Exception) e, getClass().getName());
        logger.error("Un Handled Exception " + lineNumber, e);
        logger.info("Message " + message);
        if (message != null)
            Log.e("Message", message);
        for (int i = 0; i < error.length; i++) {
            logger.info("Exception " + error[i].toString());
            Log.e("Exception", error[i].toString());
        }
        Intent report = new Intent(this, Reporting.class);
        report.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(report);
        finish();
        logger.info("\n--------------Un Handled Exception Finish-----------------\n\n");
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}