package com.tnedicca.routewise.helper;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.sensorsmodel.BasicModel;
import com.tnedicca.routewise.sensorsmodel.Light_details;

public class SensorHelper implements SensorEventListener {

    private Context context;
    private RouteWise mInstance;
    private SensorManager senSensorManager;
    private Sensor senceAccelerometer, senceProxmity, senceGravity, senceTemperature, senceGyroScope, senceLight, senceMagneticField;

    public SensorHelper(Context context) {
        this.context = context;
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            BasicModel model = new BasicModel();
            model.setX(sensorEvent.values[0]);
            model.setY(sensorEvent.values[1]);
            model.setZ(sensorEvent.values[2]);
            model.setAccuracy(sensorEvent.accuracy);
            model.setSensortime(sensorEvent.timestamp);
            model.setTime(System.currentTimeMillis()/1000);
            mInstance.myLibrary.storeaccelerometer(model);
        }
        else if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
            BasicModel model = new BasicModel();
            model.setX(sensorEvent.values[0]);
            model.setY(sensorEvent.values[1]);
            model.setZ(sensorEvent.values[2]);
            model.setAccuracy(sensorEvent.accuracy);
            model.setSensortime(sensorEvent.timestamp);
            model.setTime(System.currentTimeMillis()/1000);
            mInstance.myLibrary.storegravity(model);
        }
        else if (mySensor.getType() == Sensor.TYPE_LIGHT) {
            Light_details light_details = new Light_details();
            light_details.setLight(sensorEvent.values[0]);
            light_details.setSensortime(sensorEvent.timestamp);
            light_details.setTimestamp(System.currentTimeMillis()/1000);
            mInstance.myLibrary.storelight(light_details);
        }
        else if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            BasicModel model = new BasicModel();
            model.setX(sensorEvent.values[0]);
            model.setY(sensorEvent.values[1]);
            model.setZ(sensorEvent.values[2]);
            model.setAccuracy(sensorEvent.accuracy);
            model.setSensortime(sensorEvent.timestamp);
            model.setTime(System.currentTimeMillis()/1000);
            mInstance.myLibrary.storegyrocope(model);
        }
        else if (mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            BasicModel model = new BasicModel();
            model.setX(sensorEvent.values[0]);
            model.setY(sensorEvent.values[1]);
            model.setZ(sensorEvent.values[2]);
            model.setAccuracy(sensorEvent.accuracy);
            model.setSensortime(sensorEvent.timestamp);
            model.setTime(System.currentTimeMillis()/1000);
            mInstance.myLibrary.storemagnaticfields(model);
        }
        else  if (mySensor.getType() == Sensor.TYPE_PROXIMITY){
            BasicModel model = new BasicModel();
            model.setX(sensorEvent.values[0]);
            model.setY(sensorEvent.values[1]);
            model.setZ(sensorEvent.values[2]);
            model.setAccuracy(sensorEvent.accuracy);
            model.setSensortime(sensorEvent.timestamp);
            model.setTime(System.currentTimeMillis()/1000);
            mInstance.myLibrary.storeproximity(model);
        }

        if ((mySensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE)){
            Log.d("SENSORVALUES",String.valueOf(sensorEvent.values[0]));
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



}
