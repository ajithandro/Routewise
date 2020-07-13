package com.tnedicca.routewise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.sensorsmodel.ScreenStatus_model;

public class ScreenStatus extends BroadcastReceiver {
    RouteWise routeWise = RouteWise.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            passstatus(true);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            passstatus(false);

        }
    }

    private void passstatus(boolean type) {
        ScreenStatus_model screenStatus = new ScreenStatus_model();
        screenStatus.setScreen_status(type);
        screenStatus.setTimestamp(System.currentTimeMillis() / 1000);
        routeWise.myLibrary.storescreenstatus(screenStatus);
        Log.d("Status", String.valueOf(screenStatus.isScreen_status()));

    }

}
