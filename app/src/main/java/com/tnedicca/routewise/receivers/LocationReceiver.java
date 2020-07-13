package com.tnedicca.routewise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;

import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class LocationReceiver extends BroadcastReceiver {

    private RouteWise mInstance;
    private int lineNumber;
    private long mCurrentTime;
    private RouteLog logger;

    private SharedPreferences mPrefer;
    private Location lastLocation;
    private boolean mInsertDummy;
    private boolean logFirst;
    private List<Location> regionArray = new ArrayList<>();
    private List<Location> tripWaitingArray = new ArrayList<>();
    private SharedPreferences.Editor edit;
    private boolean gotRecentLocation = true;
    private Location regionLocation;
    private boolean insertStart;
    private boolean tracking_enabled;
//    private boolean go = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(LocationReceiver.class);
        mPrefer = context.getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);

        try {
            Location location = intent.getParcelableExtra("com.google.android.location.LOCATION");
            if (location != null) {
                logger.info("Queued Location : " + location);
                getPreferenceValues();
                boolean allowLocationCalc = true;
                boolean isMock = location.isFromMockProvider();
                if (BuildConfig.MOCK_LOCATIONS) {
                    allowLocationCalc = isMock;
                } else if (!BuildConfig.MOCK_LOCATIONS) {
                    allowLocationCalc = !isMock;
                }
                if (allowLocationCalc) {
                    mCurrentTime = System.currentTimeMillis() / 1000;
                    long locTime1 = location.getElapsedRealtimeNanos() / 1000;
                    long locTime = location.getTime();
                    String time1 = mInstance.myLibrary.fetchDate(mCurrentTime * 1000);
                    String time2 = mInstance.myLibrary.fetchDate(locTime);
                    String time3 = mInstance.myLibrary.fetchDate(locTime1);
                    long diff = mCurrentTime - (locTime / 1000);
//                    logger.info("Location Recieved : " + location);
                    logger.info("\n\n --------------------Recieved LocationTime : " + time2 + " CurrentTime : " + time1 + " Diff : " + diff + "  -----------------------\n");
                    mInstance.myLibrary.noti(context, AppConstant.DEFAULT_DB_NAME, "" + location, AppConstant.NOTIFY_LOC_SERVICE, true);

                    if (tracking_enabled) {
                        calcLogic(context, location);
                    }
                } else {
                    mInstance.myLibrary.noti(context, AppConstant.DEFAULT_DB_NAME, "Detected Mock Location : " + location, AppConstant.NOTIFY_LOC_SERVICE, true);
                }
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    private void getPreferenceValues() {
        lastLocation = mInstance.myLibrary.retriveLocation(AppConstant.PREVIOUS_LOCATION);
        regionArray = mInstance.myLibrary.retriveLocationArray(AppConstant.REGION_ARRAY);
        tripWaitingArray = mInstance.myLibrary.retriveLocationArray(AppConstant.WAITING_ARRAY);
        mInsertDummy = mPrefer.getBoolean(AppConstant.INSERT_DUMMY, false);
        tracking_enabled = mPrefer.getBoolean(AppConstant.TRACKING_ENABLED, false);
        gotRecentLocation = mPrefer.getBoolean(AppConstant.GOT_RECENT_LOC, false);
        insertStart = mPrefer.getBoolean(AppConstant.INSERT_START_BUFFER, false);
        regionLocation = mInstance.myLibrary.retriveLocation(AppConstant.REGION_LOCATION);
        if (logFirst) {
            logger.info("getPreferenceValues lastLocTime : " + mInstance.lastLocTime + "  mInsertDummy : " + mInsertDummy);
            logFirst = !logFirst;
        }
    }

    private void calcLogic(Context context, Location location) {
        long currentTime = System.currentTimeMillis() / 1000;
        float horizontalAccuracy = location.getAccuracy();
        float speed = location.getSpeed();
        float hAccuracy = location.getAccuracy();
        long locTime = location.getTime() / 1000;
        long locDiff = currentTime - locTime;
        double distance = 1000;
        long drivingDiff = currentTime - mInstance.lastDrivingTime;

        boolean trustLocation = false;
        if (lastLocation != null) {
            distance = lastLocation.distanceTo(location);
        }
        if (hAccuracy > AppConstant.LOCATION_ACCURACY) {
            if (distance < AppConstant.LOCATION_DISTANCE_BUFFER) {
                trustLocation = true;
            }
        } else {
            trustLocation = true;
        }

        if (trustLocation) {
            //         Check for recent location from GPS within 15sec
            gotRecentLocation = locDiff < 15;
            edit = mPrefer.edit();
            edit.putBoolean(AppConstant.GOT_RECENT_LOC, gotRecentLocation);
            edit.apply();

            if (regionLocation != null) {
                double distance1 = regionLocation.distanceTo(location);
                logger.info("Location Distance from the Last Exited Region  " + distance1 + "m");
            }
            logger.info("Location recordingTrip : " + mInstance.recordingTrip);
            logger.info("Location Details : " + mInstance.drivingActivity + "  : " + locDiff + "sec   : " + distance + "m");
            logger.info("lastLocTime : " + mInstance.lastLocTime + "  currentTime : " + currentTime + "  drivingDiff : " + drivingDiff + "sec");

            mInstance.syncHelper.syncOldData();
            mInstance.myLibrary.deleteOldData();
            mInstance.invokeTimer();

            long motionDiff = currentTime - mInstance.activityDetectedTime;
            boolean drivingFlag = drivingDiff > (AppConstant.LOC_SPLIT_INTERVAL - 10);
            boolean motionFlag = motionDiff > (AppConstant.LOC_SPLIT_INTERVAL - 10);
            if (!mInstance.drivingActivity || drivingFlag) {
                logger.info("difff : " + motionFlag + "  motionDiff : " + motionDiff + "  motionFlag : " + motionFlag);
                mInstance.checkMotionActivity(false);
            }

            boolean allow = false;
            if (mInstance.lastLocTime == 0) {
                mInstance.lastLocTime = currentTime - AppConstant.UPLOAD_INTERVAL;
                logger.info("First Time lastLocTime : " + mInstance.lastLocTime);
                edit = mPrefer.edit();
                edit.putLong(AppConstant.LAST_LOC_TIME, mInstance.lastLocTime);
                edit.apply();
            } else {
                logger.info("Distance : " + distance + "   Speed : " + speed + "   horizontalAccuracy : " + horizontalAccuracy);
                logger.info("lastLocTime : " + mInstance.lastLocTime + "   currentTime : " + currentTime + "   diff : " + mInstance.lastLocTime + AppConstant.LOC_INTERVAL + "   Result : " + (currentTime > (mInstance.lastLocTime + AppConstant.LOC_INTERVAL)));

                if (currentTime > (mInstance.lastLocTime + AppConstant.LOC_INTERVAL)) {
                    if (mInstance.drivingActivity) {
                        allow = true;
                        int waitingCount = tripWaitingArray.size();
                        if (waitingCount > 0) {
                            validateWaitingBuffer(location);
                        }
                    } else {
                        double splitTime = AppConstant.LOC_SPLIT_INTERVAL;
                        drivingDiff = currentTime - mInstance.lastDrivingTime;
                        logger.info("lastDrivingTime : " + mInstance.lastDrivingTime + "   drivingDiff : " + drivingDiff);
                        if (drivingDiff < splitTime) {
                            tripWaitingArray.add(location);
                            if (tripWaitingArray.size() >= 60) {
                                tripWaitingArray = tripWaitingArray.subList(tripWaitingArray.size() - 60, tripWaitingArray.size());
                            }
                            mInstance.myLibrary.saveLocationArray(tripWaitingArray, AppConstant.WAITING_ARRAY);
                        }
                    }
                }
            }

            logger.info("ALLOW : " + allow + "  activeGeofence : " + mInstance.activeGeofence);
            if (allow && !mInstance.activeGeofence) {
                mInstance.recordingTrip = true;
                logger.info("Allow Location locTime : " + mInstance.lastLocTime + "  Insert Buffer : " + insertStart + "   Diff : " + currentTime + " lastLocTime)");
                if (!insertStart) {
                    insertStart = true;
                    logger.info("Location regionLocation  : " + regionLocation);
                    validateBufferRegions(location);
                }
                edit = mPrefer.edit();
                edit.putBoolean(AppConstant.INSERT_START_BUFFER, insertStart);
                edit.putBoolean(AppConstant.IS_RECORDING_TRIP, mInstance.recordingTrip);
                edit.commit();
                mInstance.myLibrary.insertLoc(location, 1, false, false);
            }

            lastLocation = location;
            mInstance.myLibrary.saveLocation(lastLocation, AppConstant.PREVIOUS_LOCATION);

            logger.info("drivingActivity : " + mInstance.drivingActivity + "  gotRecentLocation : " + gotRecentLocation);
            long geofenceDiff = currentTime - mInstance.geofenceDetectedTime;
            boolean geofenceFlag = geofenceDiff > (AppConstant.LOC_SPLIT_INTERVAL - 10);
            logger.info("difff : " + geofenceFlag + "  geofenceDiff : " + geofenceDiff);
            if (!mInstance.drivingActivity && !mInstance.recordingTrip) {
                mInstance.addGeofence(location, false);
            }
        } else {
            logger.info("High Accuracy Found");
        }
    }

    private void validateWaitingBuffer(Location location) {
        long currentTime = System.currentTimeMillis() / 1000;
        boolean calcBuffer = true;
        Cursor loc = mInstance.myLibrary.getLastLocationData();
        int count = loc.getCount();
        if (count > 0) {
            loc.moveToFirst();
            double lat = loc.getDouble(0);
            double lon = loc.getDouble(1);
            Location dbLoc = new Location("");
            dbLoc.setLatitude(lat);
            dbLoc.setLongitude(lon);
            double distance = location.distanceTo(dbLoc);
            if (distance < 50) {
                calcBuffer = false;
            }
        }

        int waitCount = tripWaitingArray.size();
        logger.info("validateWaitingBuffer calcBuffer   : " + calcBuffer + "  Count" + waitCount);
        if (calcBuffer) {
            for (int i = 0; i < waitCount; i++) {
                Location buffLoc = tripWaitingArray.get(i);
                logger.info("validateWaitingBuffer i " + i + " buffLoc  : " + buffLoc);
                long buffLocTime = buffLoc.getTime() / 1000;
                long buffDiff = currentTime - buffLocTime;
                logger.info("validateWaitingBuffer  buffDiff  : " + buffDiff);
                mInstance.myLibrary.insertLoc(buffLoc, 0, false, true);
            }
        }
        tripWaitingArray = new ArrayList<>();
        mInstance.myLibrary.saveLocationArray(tripWaitingArray, AppConstant.WAITING_ARRAY);
    }

    private void validateBufferRegions(Location location) {
        long currentTime = System.currentTimeMillis() / 1000;
        int startIndex = 0;
        Location previousLocation = null;
        long previousTime = 0;
        boolean crossedRegion = false;
        boolean timeTolerenceReached = false;
        boolean isBroken = false;
        int regionCount = regionArray.size();

        logger.info("validateBufferRegions regionCount :  " + regionCount + "  currentTime  : " + currentTime + " regionTime  : " + mInstance.regionTime);
        Collections.reverse(regionArray);
        for (int i = 0; i < regionArray.size(); i++) {
            Location buffLoc = regionArray.get(i);
            logger.info("validateBufferRegions PreviousLocation :  " + buffLoc + "  locTime : " + buffLoc.getTime());
        }
        for (int i = 0; i < regionArray.size(); i++) {
            Location buffLoc = regionArray.get(i);
            long buffLocTime = buffLoc.getTime() / 1000;
            long buffDiff = currentTime - buffLocTime;
            if (regionLocation == null) {
                regionLocation = buffLoc;
            }

            if (previousLocation != null) {
                double distance = buffLoc.distanceTo(previousLocation);
                long timeTravelled = previousTime - buffLocTime;
                double speed = distance / timeTravelled;
                logger.info("validateBufferRegions  speed  : " + speed + " timeTravelled  : " + timeTravelled + "  distance  : " + distance);

                // 3 Minutes Buffer to insert Locations
                if (buffDiff > (60 * 3)) {
                    timeTolerenceReached = true;
                }

                long regionDiff = buffLocTime - mInstance.regionTime;
                logger.info("validateBufferRegions  regionDiff  : " + regionDiff + " crossedRegion  : " + crossedRegion + "  timeTolerenceReached  : " + timeTolerenceReached);
                if (speed > 2 && timeTolerenceReached && crossedRegion) {
                    startIndex = i;
                    isBroken = true;
                    break;
                }
            }

            if (buffLoc.getLatitude() == regionLocation.getLatitude() && buffLoc.getLongitude() == regionLocation.getLongitude()) {
                crossedRegion = true;
            }
            previousLocation = buffLoc;
            previousTime = buffLocTime;
        }

        int insertIndex = 0;
        if (isBroken) {
            insertIndex = regionCount - (startIndex + 1);
        }

        List<Location> insertArray = new ArrayList<>();
        long adjustTime = currentTime - ((regionCount + 1) * 2);
        logger.info("validateBufferRegions  startIndex  : " + startIndex + "  insertIndex  : " + insertIndex + "  isBroken  : " + isBroken);

        Collections.reverse(regionArray);
        for (int i = 0; i < regionArray.size(); i++) {
            if (i >= insertIndex) {
                Location buffLoc = regionArray.get(i);
                if (buffLoc != null) {
                    long locTime = buffLoc.getTime() / 1000;
                    long buffDiff = currentTime - locTime;
                    if (buffDiff < (60 * 3) && adjustTime == 0) {
                        adjustTime = locTime - ((i + 1) * 2);
                    }
                    insertArray.add(buffLoc);
                }
            }
        }

        if (regionArray.size() == 1) {
            insertArray = regionArray;
        }

        for (int i = 0; i < insertArray.size(); i++) {
            Location buffLoc = insertArray.get(i);
            long insertTime = buffLoc.getTime() / 1000;
            if (insertTime < adjustTime) {
                insertTime = adjustTime;
            }
            logger.info("validateBufferRegions InsertedArray adjustTime : " + adjustTime + " insertTime  : " + insertTime + "   " + buffLoc);
            // Since time is divided again in the insertLoc function.
            buffLoc.setTime(insertTime * 1000);
            mInstance.myLibrary.insertLoc(buffLoc, 0, false, true);
            adjustTime = adjustTime + 2;
        }
    }

    private ArrayList<Location> resetBuffer(ArrayList<Location> location) {
        ArrayList<Location> newArray = new ArrayList<>();
        int size = location.size();
        if (size > 0) {
            Location oldLoc = location.get(size - 1);
            newArray.add(oldLoc);
        }
        logger.info("Buffer Reset Done");
        return newArray;
    }

}
