package com.tnedicca.routewise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;

public class SensorsRecivers extends BroadcastReceiver implements SensorEventListener {
    private RouteWise mInstance;
    private SensorManager senSensorManager;
    private Sensor senceAccelerometer, senceProxmity, senceGravity, senceTemperature, senceGyroScope, senceLight, senceMagneticField;


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            Log.d("ACCELEROMETER", "SENSOR VALUES : " + String.valueOf(sensorEvent.timestamp));
        }

        if (mySensor.getType() == Sensor.TYPE_LIGHT) {
            Log.d("LIGHT", "SENSOR VALUES : " + String.valueOf(sensorEvent.values[0]));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        mInstance = RouteWise.getInstance();
        mInstance.myLibrary.DisplayToast(context, AppConstant.SENSOR_ACTIVATE, Toast.LENGTH_LONG, Gravity.CENTER);
        senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        /**
         * Sensor's Deceleration's
         */
        senceAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senceProxmity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        senceGravity = senSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        senceGyroScope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senceLight = senSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        senceMagneticField = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senceTemperature = senSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        /**
         * Sensor's Registration's
         */
        senSensorManager.registerListener(this, senceAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senceProxmity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senceGravity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senceGyroScope, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senceLight, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senceMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senceTemperature, SensorManager.SENSOR_DELAY_NORMAL);
    }



}
