package com.tnedicca.routewise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.Filler;
import com.tnedicca.routewise.classes.RouteLog;

import org.json.JSONObject;

/**
 * Created by new on 28-01-2017.
 */

public class ConnectivityReceiver extends BroadcastReceiver {

    private RouteWise mInstance;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private RouteLog logger;

    public ConnectivityReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(ConnectivityReceiver.class);
        Filler filler = new Filler(context);
        mPrefer = context.getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);
        JSONObject network = filler.getnetwork(context);
        if (network.optBoolean(AppConstant.NETWORK_RESPONSE_AVAILABLE)) {
            mInstance.dataStatus = true;
            if (network.optBoolean(AppConstant.WIFI)) {
                mInstance.dataType = AppConstant.WIFI;
            } else if (network.optBoolean(AppConstant.MOBILE)) {
                mInstance.dataType = AppConstant.MOBILE;
            } else if (network.optBoolean(AppConstant.ETHERNET)) {
                mInstance.dataType = AppConstant.ETHERNET;
            }
        } else {
            mInstance.dataStatus = false;
            mInstance.dataType = "";
        }
        logger.info("Internet Connection Status : " + mInstance.dataStatus + " Type : " + mInstance.dataType);
        edit = mPrefer.edit();
        edit.putString(AppConstant.DATA_CONNECTION_TYPE, mInstance.dataType);
        edit.putBoolean(AppConstant.DATA_CONNECTION_STATUS, mInstance.dataStatus);
        edit.apply();
    }
}
