package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.Filler;
import com.tnedicca.routewise.classes.MyLibrary;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.classes.Trip;
import com.tnedicca.routewise.helper.GeoFenceHelper;
import com.tnedicca.routewise.helper.MotionActivityHelper;
import com.tnedicca.routewise.helper.SensorHelper;
import com.tnedicca.routewise.helper.SyncHelper;
import com.tnedicca.routewise.receivers.LocationReceiver;
import com.tnedicca.routewise.receivers.ScreenStatus;
import com.tnedicca.routewise.receivers.TimerExpiredReceiver;
import com.tnedicca.routewise.utils.LruBitmapCache;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.android.volley.VolleyLog.TAG;

public class RouteWise extends Application implements Application.ActivityLifecycleCallbacks, SharedPreferences.OnSharedPreferenceChangeListener {

    private static RouteWise mInstance;
    public MyLibrary myLibrary;
    public SimpleDateFormat dateFormat;
    public SimpleDateFormat zipFileDateFormat;
    public SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    public SimpleDateFormat unsubscribeDateFormat;
    ArrayList<String> volleyArray = new ArrayList<String>();

    public boolean activeGeofence = false;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    public boolean dataStatus;
    public String dataType;
    public String uploadDataTypeSelected;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private RouteLog logger;

    public SyncHelper syncHelper;
    public GeoFenceHelper geofenceHelper;
    private MotionActivityHelper motionActivityHelper;
    private Intent mIntentLocation;
    private PendingIntent mLocationPendingIntent;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    public long locationManagerTime;
    private boolean isLocManagerActive = false;
    public boolean isBatterySaverMode = false;
    private boolean permission = false;
    public boolean activityStatus = false;
    public long activityDetectedTime = 0;
    public long geofenceDetectedTime = 0;
    public boolean drivingActivity = false;
    public boolean recordingTrip = false;
    public boolean subscribed = false;
    public boolean tracking_enabled = false;
    public boolean auto_enable = false;
    private ArrayList<Location> tripWaitingArray;
    public long lastDrivingTime = 0;
    public long lastLocTime;
    public long regionTime;
    private GeofencingClient geofencingClient;
    private long lastGeofenceTime = 0;
    private long lastGPSHTime = 0;
    private long lastGPSLTime = 0;
    public long lastTrafficTime = 0;
    public int lastActivityType = 100;
    private ActivityRecognitionClient activityRecognitionClient;
    private Filler mFiller;
    private int lineNumber;
    private CountDownTimer stopCounter;

    public static synchronized RouteWise getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        mInstance = this;
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        logger = new RouteLog();
        logger.configureLog();
        logger.setLoggerClass(RouteWise.class);
        myLibrary = new MyLibrary();
        mFiller = new Filler(this);
        logger.info("\n\n\nApplication onCreate\n\n\n");
        // sendBroadcast(new Intent(this,SensorsRecivers.class));
        new SensorHelper(this);
        BroadcastReceiver receiver = new ScreenStatus();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

        geofencingClient = LocationServices.getGeofencingClient(this);
        activityRecognitionClient = ActivityRecognition.getClient(this);
        motionActivityHelper = new MotionActivityHelper(this);
        geofenceHelper = new GeoFenceHelper(this);
        syncHelper = new SyncHelper(this);

        mIntentLocation = new Intent(this, LocationReceiver.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> addGeofence(location, true));
        mLocationPendingIntent = PendingIntent.getBroadcast(this, 1, mIntentLocation, 0);


        getFirst();
        initData();

        mPrefer.registerOnSharedPreferenceChangeListener(this);
        handleSSLHandshake();
        if (BuildConfig.MOCK_DRIVING) {
            mockDriving(this);
        }
    }

    void initData() {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        zipFileDateFormat = new SimpleDateFormat("MMM_dd_yyyy_HH_mm_ss_a");
        zipFileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        unsubscribeDateFormat = new SimpleDateFormat("MM/dd/yy");
        lastTrafficTime = (System.currentTimeMillis() / 1000) + 13;
        getPreferenceValues();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
//        logger.info("Application onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            logger.info("Application Entered Foreground");
            getFirst();
            // App enters foreground
            initManager(true);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
//        logger.info("Application onActivityResumed" );
    }

    @Override
    public void onActivityPaused(Activity activity) {
//        logger.info("Application onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            logger.info("Application Entered Background : " + recordingTrip);
            // App enters background
            if (!recordingTrip) {
                initManager(false);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
//        logger.info("Application onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
//        logger.info("Application onActivityDestroyed");
    }

    @Override
    public void onTerminate() {
        logger.info("Application onTerminate");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        logger.info("Application onLowMemory : " + activityReferences);
//        initManager(false);
        super.onLowMemory();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        logger.info("Application onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTrimMemory(int level) {
        logger.info("Application onTrimMemory : " + activityReferences);
        super.onTrimMemory(level);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        logger.info("Application OnSharedPreferenceChangeListener");
        if (key.equals(AppConstant.SUBSCRIBED)) {
            subscribed = sharedPreferences.getBoolean(key, false);
            logger.info("Application subscribed : " + subscribed);
            firstCheck();
        } else if (key.equals(AppConstant.TRACKING_ENABLED)) {
            tracking_enabled = sharedPreferences.getBoolean(key, false);
            logger.info("Application tracking enabled : " + tracking_enabled);
            checkTracking();
        }
        getPreferenceValues();
    }

    private void getPreferenceValues() {
        tracking_enabled = mPrefer.getBoolean(AppConstant.TRACKING_ENABLED, false);
        subscribed = mPrefer.getBoolean(AppConstant.SUBSCRIBED, false);
        auto_enable = mPrefer.getBoolean(AppConstant.AUTO_TRACKING, false);
        locationManagerTime = mPrefer.getLong(AppConstant.LOC_MANAGER_TIME, 0);
        lastGeofenceTime = mPrefer.getLong(AppConstant.LAST_GEOFENCE_TIME, 0);
        lastLocTime = mPrefer.getLong(AppConstant.LAST_LOC_TIME, 0);
        lastDrivingTime = mPrefer.getLong(AppConstant.LAST_DRIVING_TIME, 0);
        recordingTrip = mPrefer.getBoolean(AppConstant.IS_RECORDING_TRIP, false);
    }

    public void initManager(boolean override) {
        boolean isLowPower;
        isLowPower = !drivingActivity;
        if (override) {
            isLowPower = false;
        }
        logger.info("initManager override  : " + override + "  isLowPower : " + isLowPower);
        locationManagerTime = System.currentTimeMillis() / 1000;
        edit = mPrefer.edit();
        edit.putLong(AppConstant.LOC_MANAGER_TIME, locationManagerTime);
        edit.apply();

        if (!isLowPower) {
            startGPSHighPower(false);
        } else {
            startGPSLowPower();
        }
    }

    public void startGPSHighPower(boolean override) {
        logger.info("startGPSHighPower override  : " + override + "   tracking_enabled : " + tracking_enabled + "   subscribed : " + subscribed);
        if (subscribed && tracking_enabled) {
            boolean startManager = false;
            if (!isLocManagerActive) {
                startManager = true;
            } else {
                if (isBatterySaverMode) {
                    startManager = true;
                }
            }

            long currentTime = System.currentTimeMillis() / 1000;
            long gpsDiff = currentTime - lastGPSHTime;
            boolean gpsFlag = gpsDiff > AppConstant.GEOFENCE_TIMER_INTERVAL;
            logger.info("startGPSHighPower gpsDiff : " + gpsDiff + "  gpsFlag : " + gpsFlag);
            if (override || startManager) {
                lastGPSHTime = currentTime;
                isLocManagerActive = true;
                isBatterySaverMode = false;

                if (locationRequest == null) {
                    locationRequest = LocationRequest.create();
                }
                myLibrary.noti(this, "GPS", "HIGH POWER", AppConstant.NOTIFY_GPS, true);
                logger.info("Location Listening startGPSHighPower");
                locationRequest.setInterval(AppConstant.GPS_UPDATE_TIME);
                locationRequest.setFastestInterval(AppConstant.GPS_FAST_UPDATE_TIME);
                locationRequest.setSmallestDisplacement(AppConstant.GPS_UPDATE_DISTANCE);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationPendingIntent);
            }
        }
    }

    private void startGPSLowPower() {
        boolean startManager = false;
        if (!isLocManagerActive) {
            startManager = true;
        } else {
            if (!isBatterySaverMode) {
                startManager = true;
            }
        }

        logger.info("startGPSLowPower startManager : " + startManager + "   activeGeofence : " + activeGeofence);
//        startGPSHighPower(false);
        long currentTime = System.currentTimeMillis() / 1000;
        long gpsDiff = currentTime - lastGPSLTime;
        boolean gpsFlag = gpsDiff > AppConstant.GEOFENCE_TIMER_INTERVAL;
        logger.info("startGPSLowPower gpsDiff : " + gpsDiff + "  gpsFlag : " + gpsFlag);
        if ((startManager || gpsFlag) && activeGeofence) {
            lastGPSLTime = currentTime;
            locationManagerStatus();
            isLocManagerActive = true;
            isBatterySaverMode = true;

//            if (locationRequest == null) {
//                locationRequest = LocationRequest.create();
//            }
//            myLibrary.noti(this, "GPS","BALANCED POWER", AppConstant.NOTIFY_GPS, true);
//            logger.info("Location Listening startGPSLowPower");
//            locationRequest.setInterval(AppConstant.BATTERY_GPS_UPDATE_TIME);
//            locationRequest.setFastestInterval(AppConstant.BATTERY_GPS_FAST_UPDATE_TIME);
//            locationRequest.setSmallestDisplacement(AppConstant.GPS_BATTERY_UPDATE_DISTANCE);
//            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationPendingIntent);
        }
    }

    private void locationManagerStatus() {
        if (isLocManagerActive) {
            isLocManagerActive = false;
            isBatterySaverMode = false;
            logger.info("Location Listening Stopped");
            myLibrary.noti(this, "GPS", "STOPPED", AppConstant.NOTIFY_GPS, true);
            mFusedLocationClient.removeLocationUpdates(mLocationPendingIntent);
        }
    }

    public void firstCheck() {
        logger.info("firstCheck  subscribed : " + subscribed + "  permission : " + permission);
        if (subscribed && !permission) {
            permission = true;
            edit = mPrefer.edit();
            edit.putBoolean(AppConstant.LOCATION_PERMISSION, permission);
            edit.apply();
        }
    }

    private void checkTracking() {
        logger.info("checkTracking tracking : " + tracking_enabled);
        checkMotionActivity(false);
        initManager(true);
    }

    private void update(int time, boolean addDummy) {
        logger.info("Update Dummy Record");
        myLibrary.insertLoc(null, time, addDummy, true);
    }

    private void update2() {
        logger.info("Sent to server");
        syncHelper.sendToServer(false);
        boolean insertStart = false;
        edit = mPrefer.edit();
        edit.putBoolean(AppConstant.INSERT_START_BUFFER, insertStart);
        edit.apply();
        initManager(false);
    }

    public void enableAutoTracking() {
        logger.info("enableAutoTracking auto_enable : " + auto_enable);
        tracking_enabled = true;
        auto_enable = false;
        edit = mPrefer.edit();
        edit.putBoolean(AppConstant.AUTO_TRACKING, auto_enable);
        edit.putBoolean(AppConstant.TRACKING_ENABLED, tracking_enabled);
        edit.apply();
    }

    public void checkAutoTracking() {
        if (auto_enable) {
            logger.info("checkAutoTracking : " + auto_enable);
            long[] diff = myLibrary.calcDiff();
            if (diff[3] <= 0) {
                enableAutoTracking();
            }
        }
    }

    public void invokeTimer() {
        long currentTime = System.currentTimeMillis() / 1000;
        long locDiff = currentTime - lastLocTime;
        int splitTime = AppConstant.LOC_SPLIT_INTERVAL;
        long regionDiff = currentTime - regionTime;
        long drivingDiff = currentTime - lastDrivingTime;

        logger.info("Location invokeTimer : locDiff : " + locDiff + "  recordingTrip : " + recordingTrip + "  regionDiff : " + regionDiff + "  drivingDiff : " + drivingDiff + "  activeGeofence : " + activeGeofence);
        if (locDiff > splitTime && (regionTime == 0 || regionDiff > 10)) {
            initManager(false);
            if (drivingDiff > splitTime && lastDrivingTime != 0) {
                stopMotionActivity();
                validateEndBuffer();
                int extraTime = AppConstant.LOC_INTERVAL;
                update(extraTime + 1, true);
                update(splitTime, false);
                update2();

                List<Location> regionArray = new ArrayList<>();
                myLibrary.saveLocationArray(regionArray, AppConstant.REGION_ARRAY);
                addGeofence(myLibrary.retriveLocation(AppConstant.PREVIOUS_LOCATION), true);

                if (dataStatus) {
                    getTripList();
                }
                myLibrary.scheduleNotification(this, "NEW TRIP", "You have completed a new trip, click to view details", AppConstant.NOTIFY_TRIP_COMPLETE, false);
                recordingTrip = false;
                lastDrivingTime = 0;
                edit = mPrefer.edit();
                edit.putLong(AppConstant.LAST_DRIVING_TIME, lastDrivingTime);
                edit.putBoolean(AppConstant.IS_RECORDING_TRIP, recordingTrip);
                edit.apply();
            }
        }
    }

    public void checkMotionActivity(boolean override) {
        if (subscribed && tracking_enabled) {
            logger.info("checkMotionActivity Motion Sensor Started activityStatus : " + activityStatus + "  override : " + override);
            startMotionActivity(override);
        } else {
            logger.info("checkMotionActivity subscribed : " + subscribed + "  tracking_enabled : " + tracking_enabled);
            drivingActivity = false;
        }
    }

    private void validateEndBuffer() {
        tripWaitingArray = myLibrary.retriveLocationArray(AppConstant.WAITING_ARRAY);
        long currentTime = System.currentTimeMillis() / 1000;
        for (int i = 0; i < tripWaitingArray.size(); i++) {
            Location buffLoc = tripWaitingArray.get(i);
            long buffLocTime = buffLoc.getTime() / 1000;
            long buffDiff = currentTime - buffLocTime;
            logger.info("validateEndBuffer  buffLoc  : " + buffLoc + "     buffDiff  : " + buffDiff);
        }
        tripWaitingArray = new ArrayList<>();
        myLibrary.saveLocationArray(tripWaitingArray, AppConstant.WAITING_ARRAY);
    }

    public void startMotionActivity(boolean override) {
        logger.info("startMotionActivity override : " + override + "   activityStatus : " + activityStatus);
        if (!BuildConfig.MOCK_DRIVING && (!activityStatus || override)) {
            PendingIntent pendingIntent = motionActivityHelper.getActivityPendingIntent();
            activityRecognitionClient.requestActivityUpdates(1000, pendingIntent)
                    .addOnSuccessListener(aVoid -> {
                        long time = System.currentTimeMillis() / 1000;
                        activityStatus = true;
                        activityDetectedTime = time;
                        logger.info("Successfully Motion sensor started");
                    })
                    .addOnFailureListener(e -> {
                        activityStatus = false;
                        logger.error("Error in starting Motion sensor", e);
                    });
        }
    }

    public void stopMotionActivity() {
        long currentTime = System.currentTimeMillis() / 1000;
        long activityDiff = currentTime - activityDetectedTime;
        boolean activityFlag = activityDiff > AppConstant.GEOFENCE_TIMER_INTERVAL;

        logger.info("stopMotionActivity activityDiff : " + activityDiff + "   activityStatus : " + activityStatus + "   activityFlag : " + activityFlag);
        if (activityFlag) {
            PendingIntent pendingIntent = motionActivityHelper.getActivityPendingIntent();
            activityRecognitionClient.removeActivityUpdates(pendingIntent)
                    .addOnSuccessListener(aVoid -> {
                        activityStatus = false;
//                    pendingIntent.cancel();
                        logger.info("Successfully Motion sensor stopped");
                    })
                    .addOnFailureListener(e -> logger.error("Error in stopping Motion sensor", e));
        }
    }

    public void addGeofence(Location location, boolean override) {
        if (location != null) {
            boolean allowFenceCreation = true;
            boolean isMock = location.isFromMockProvider();
            if (BuildConfig.MOCK_LOCATIONS) {
                allowFenceCreation = isMock;
            } else if (!BuildConfig.MOCK_LOCATIONS) {
                allowFenceCreation = !isMock;
            }
            if (allowFenceCreation) {
                long currentTime = System.currentTimeMillis() / 1000;
                long geofenceDiff = currentTime - lastGeofenceTime;
                boolean geofenceFlag = geofenceDiff > AppConstant.GEOFENCE_TIMER_INTERVAL;
                logger.info("add Geofence override : " + override + "  activeGeofence : " + activeGeofence + "  geofenceDiff : " + geofenceDiff + "  geofenceFlag : " + geofenceFlag);
                if (!activeGeofence || override) {
                    long locTime = location.getTime() / 1000;
                    long diff = currentTime - locTime;
                    logger.info("add Geofence at diff : " + diff + "  location : " + location);
//                if (diff < 40) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                Geofence geofence = geofenceHelper.getGeofence(AppConstant.GEOFENCE_REQUEST_ID, latLng, AppConstant.GEOFENCE_RADIUS, Geofence.GEOFENCE_TRANSITION_EXIT);
                    Geofence geofence = geofenceHelper.getGeofence(AppConstant.GEOFENCE_REQUEST_ID, latLng, AppConstant.GEOFENCE_RADIUS, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
                    GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
                    PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

                    geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                            .addOnSuccessListener(aVoid -> {
                                logger.info("Successfully created the Geofence at location : " + location);
                                long time = System.currentTimeMillis() / 1000;
                                activeGeofence = true;
                                geofenceDetectedTime = time;
                                myLibrary.noti(this, "Geofence", "Created " + latLng, AppConstant.NOTIFY_GEOFENCE, false);
//                            if (!override) {
                                startGPSLowPower();
                                stopMotionActivity();
//                            }

                                List<Location> regionArray = myLibrary.retriveLocationArray(AppConstant.REGION_ARRAY);
                                regionArray.add(location);
                                if (regionArray.size() >= 30) {
                                    regionArray = regionArray.subList(regionArray.size() - 30, regionArray.size());
                                }
                                myLibrary.saveLocationArray(regionArray, AppConstant.REGION_ARRAY);
                            })
                            .addOnFailureListener(e -> {
                                logger.error("Error in Adding Geofence : ", e);
                            });

                    lastGeofenceTime = currentTime;
                    edit = mPrefer.edit();
                    edit.putLong(AppConstant.LAST_GEOFENCE_TIME, lastGeofenceTime);
                    edit.apply();
//                }
                }
            }
        }
    }

    public void removeGeofence() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        if (activeGeofence) {
            geofencingClient.removeGeofences(pendingIntent).addOnSuccessListener(aVoid -> {
                logger.info("Successfully removed Geofence");
                activeGeofence = false;
                mNotificationManager.cancel(AppConstant.NOTIFY_GEOFENCE);
                startGPSHighPower(true);
                startMotionActivity(true);
            }).addOnFailureListener(e -> logger.error("Error in removing Geofence : ", e));
        }
    }

    private void getFirst() {
        JSONObject network = mFiller.getnetwork(this);
        if (network.optBoolean(AppConstant.NETWORK_RESPONSE_AVAILABLE)) {
            dataStatus = true;
            if (network.optBoolean(AppConstant.WIFI)) {
                dataType = AppConstant.WIFI;
            } else if (network.optBoolean(AppConstant.MOBILE)) {
                dataType = AppConstant.MOBILE;
            } else if (network.optBoolean(AppConstant.ETHERNET)) {
                dataType = AppConstant.ETHERNET;
            }
        } else {
            dataStatus = false;
            dataType = "";
        }
        logger.info("Internet Connection Status : " + dataStatus + " Type : " + dataType);
        edit = mPrefer.edit();
        edit.putString(AppConstant.DATA_CONNECTION_TYPE, dataType);
        edit.putBoolean(AppConstant.DATA_CONNECTION_STATUS, dataStatus);
        edit.apply();
        if (dataStatus) {
            syncHelper.sendToServer(false);
            getTripList();
        }
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void setAlarm(Context context, long endTime, String type, int code) {
        logger.info("Alarm started for " + type + " code " + code + "  With time of " + endTime);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TimerExpiredReceiver.class);
        intent.putExtra(type, true);
        PendingIntent sender = PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(endTime, sender), sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, endTime, sender);
        }

        if (!BuildConfig.MOCK_DRIVING) {
            stopCounter = new CountDownTimer(endTime - Calendar.getInstance().getTimeInMillis(), 1000) {

                public void onTick(long millisUntilFinished) {
                    myLibrary.noti(context, AppConstant.DEFAULT_DB_NAME, "Trip will be completed in " + millisUntilFinished / 1000, AppConstant.NOTIFY_AUTO_ALARM_STARTED, true);
                }

                public void onFinish() {
                    myLibrary.noti(context, AppConstant.DEFAULT_DB_NAME, "Alarm Closed ", AppConstant.NOTIFY_AUTO_ALARM_STARTED, true);
                }
            };
            stopCounter.start();
        }
    }

    public void removeAlarm(Context context, int code) {
        logger.info("Alarm removed for " + code);
        Intent intent = new Intent(context, TimerExpiredReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
        if (!BuildConfig.MOCK_DRIVING) {
            if (stopCounter != null) {
                stopCounter.cancel();
            }
        }
    }

//    public void setConnectivityListener(ConnectivityReceiverListener listener) {
//        ConnectivityReceiver.connectivityReceiverListener = listener;
//    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue, new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, arg1) -> {
                Log.d("Hostname : ", hostname);
                return hostname.equalsIgnoreCase(BuildConfig.DOMAIN) || hostname.equalsIgnoreCase("maps.googleapis.com") ||
                        hostname.equalsIgnoreCase("clients4.google.com") || hostname.equalsIgnoreCase("csi.gstatic.com");
            });
        } catch (Exception ignored) {
        }
    }

    void getTripList() {
        int method = Request.Method.GET;
        String url = BuildConfig.REST_URL + AppConstant.TRIP_LIST_URL;
        syncTripList(url, method, AppConstant.TRIPS_LIST_GET, AppConstant.TRIPS_LIST_GET);
    }

    private void syncTripList(String url, final int method, final String tag, final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, response -> {
            volleyArray.remove(tag);
            response(tag, response, 200, queryType);
        }, error -> {
            volleyArray.remove(tag);
            logger.info(getString(R.string.received_error));
            error(tag, error, queryType);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, myLibrary.apiKeyEncrypter());
                headers.put(AppConstant.REST_ACCESS_TOKEN, mPrefer.getString(AppConstant.TOKEN, ""));
                headers.put(AppConstant.REST_X_RANGE, "0");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return null;
            }

            @Override
            public int getMethod() {
                return method;
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String jsonString = null;
                try {
                    jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    JSONArray jsonArray = new JSONArray(jsonString);
                    jsonArray.put(response.statusCode);
                    return Response.success(jsonArray, HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    lineNumber = myLibrary.getLineNumber(e, getClass().getName());
                    logger.error("Error at " + lineNumber, e);
                    return Response.error(new ParseError(e));
                }
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(AppConstant.REST_RETRY_MILLISECONDS, AppConstant.REST_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        RouteWise.getInstance().addToRequestQueue(req, tag);
        volleyArray.add(tag);
    }

    //handles diferent response codes from Volley
    private void response(String check, JSONArray response, int code, String queryType) {
        myLibrary.checkResponse(response, code, null);
        // handling response for submitting insurance data
        if (queryType.equals(AppConstant.TRIPS_LIST_GET)) {
            JSONArray result = new JSONArray();
            for (int i = 1; i < response.length() - 1; i++) {
                result.put(response.optJSONObject(i));
            }
            int status = response.optInt(response.length() - 1);
            if (status == 200) {
            } else if (status == 501) {
                logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.updation_failed));
            } else {
                logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status);
            }
            if (check.equals(AppConstant.TRIPS_LIST_GET) && (status == AppConstant.RESPONSE_SUCCESS || status == AppConstant.RESPONSE_GOOGLE_ANONYMUS)) {
                addTripsToList(result);
            }
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error, String queryType) {
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                System.out.println(message);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode, queryType);
            } else {
                logger.info("Timeout : " + AppConstant.CONNECTION_TIMEOUT);
            }
        } catch (Exception e) {
            lineNumber = myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    private void addTripsToList(JSONArray result) {
        ArrayList<Trip> tripArrayList = new ArrayList<Trip>();
        for (int i = 0; i < result.length(); i++) {
            JSONObject object = result.optJSONObject(i);
            if (object != null) {
                Trip trip = new Trip();
                trip.setPathId(object.optString(AppConstant.PATH_ID));
                trip.setMapImage(this, object.optString(AppConstant.MAP_THUMBNAIL_URL));
                trip.setTripDate(myLibrary.fetchDate(Long.parseLong(object.optString(AppConstant.START_TIME)) * 1000));
                trip.setStartPoint(object.optString(AppConstant.START_LOCATION));
                trip.setEndPoint(object.optString(AppConstant.END_LOCATION));
                trip.setTripLength(myLibrary.fetchDistanceAndTime(Double.parseDouble(object.optString(AppConstant.TRIP_LENGTH)), Integer.parseInt(object.optString(AppConstant.TRIP_TIME))));
                trip.setRiskScore(object.optString(AppConstant.ROUTE_RISK_FACTOR));
                tripArrayList.add(trip);
            }
        }
        saveList(tripArrayList);
    }

    public void saveList(List<Trip> tripList) {
        Gson gson = new Gson();
        String jsonTripsList = gson.toJson(tripList);
        edit = mPrefer.edit();
        edit.putString(AppConstant.TRIPLIST, jsonTripsList);
        edit.commit();
    }

    private void mockDriving(Context context) {
        new CountDownTimer(10 * 60 * 1000, 2000) {

            public void onTick(long millisUntilFinished) {
                testDriving(context);
            }

            public void onFinish() {
            }
        }.start();
    }

    private void testDriving(Context context) {
        long currentTime = System.currentTimeMillis() / 1000;
        long check = currentTime - lastTrafficTime;
        logger.info("CHECK : " + check + "  testLastTrafficTime : " + lastTrafficTime);
        boolean stopTimer = mPrefer.getBoolean(AppConstant.IS_RECORD_STOP_TIMER_SET, false);
        int testMaxTime = 230;
        boolean go = false;

        if (check >= 0) {
            go = true;
            if (check > testMaxTime) {
                go = false;
                // Check should exceed 100
                if (check > testMaxTime + (3 * (AppConstant.LOC_INTERVAL + 1)) + AppConstant.LOC_SPLIT_INTERVAL * 2) {
                    lastTrafficTime = lastTrafficTime + testMaxTime + (2 * (AppConstant.LOC_INTERVAL + 1)) + AppConstant.LOC_SPLIT_INTERVAL;
                    go = true;
                }
//                // Test Intermediate stops
//                if (check > testMaxTime + (2 * (AppConstant.LOC_INTERVAL) + 1) + 40) {
//                    lastTrafficTime = lastTrafficTime + testMaxTime + (2 * (AppConstant.LOC_INTERVAL) + 1) + 40;
//                    go = true;
//                }
            }
        }
        logger.info("GO : " + go + " testLastTrafficTime : " + lastTrafficTime);
        if (go) {
            drivingActivity = true;
            recordingTrip = true;
            removeGeofence();
            lastDrivingTime = System.currentTimeMillis() / 1000;
        } else {
            drivingActivity = false;
        }

        if (!drivingActivity && recordingTrip) {
            if (!stopTimer) {
                int endTime = AppConstant.LOC_SPLIT_INTERVAL * 1000;
                setAlarm(context, (Calendar.getInstance().getTimeInMillis()) + endTime, AppConstant.TRIP_STOP_TIMER, AppConstant.TRIP_STOP_TIMER_CODE);
                stopTimer = true;
            }
        }
        if (drivingActivity && stopTimer) {
            removeAlarm(context, AppConstant.TRIP_STOP_TIMER_CODE);
            stopTimer = false;
        }

        edit = mPrefer.edit();
        edit.putLong(AppConstant.LAST_DRIVING_TIME, lastDrivingTime);
        edit.putBoolean(AppConstant.IS_RECORDING_TRIP, recordingTrip);
        edit.putBoolean(AppConstant.IS_RECORD_STOP_TIMER_SET, stopTimer);
        edit.apply();

        if (lastLocTime != 0 && currentTime > (lastLocTime + AppConstant.LOC_SPLIT_INTERVAL)) {
            long locationDiff = currentTime - locationManagerTime;
            if (locationDiff > 600) {
                logger.info("initManager testMethod");
                initManager(true);
            }
        }
    }
}
