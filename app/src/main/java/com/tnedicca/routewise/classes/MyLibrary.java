package com.tnedicca.routewise.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.MainActivity;
import com.tnedicca.routewise.activities.MainMenu;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.sensorsmodel.ActivityModel;
import com.tnedicca.routewise.sensorsmodel.BasicModel;
import com.tnedicca.routewise.sensorsmodel.Light_details;
import com.tnedicca.routewise.sensorsmodel.ScreenStatus_model;
import com.tnedicca.routewise.views.CustomEditTextView;
import com.tnedicca.routewise.views.CustomTextView;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.jeesoft.widget.pickerview.CharacterPickerWindow;

/**
 * Created by new on 30-12-2016.
 */

public class MyLibrary {

    private final RouteWise mInstance;
    private final Context mContext;
    private final SharedPreferences mPrefer;
    private final RouteLog logger;
    DBAdapter Db;
    private int lineNumber;
    private Toast toast;
    private String mCurrentUser;
    private SharedPreferences.Editor edit;

    public MyLibrary() {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(MyLibrary.class);

        mContext = mInstance.getBaseContext();
        mPrefer = mContext.getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);
        getDBAdapter();
    }

    public void getDBAdapter() {
        getPreferenceValues();
        Db = new DBAdapter(mContext, mCurrentUser);
    }

    public void disableSoftInputFromAppearing(CustomEditTextView editText) {
        if (Build.VERSION.SDK_INT >= 11) {
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextIsSelectable(true);
        } else {
            editText.setRawInputType(InputType.TYPE_NULL);
            editText.setFocusable(true);
        }
    }

    private void getPreferenceValues() {
        mCurrentUser = mPrefer.getString(AppConstant.USER, null);
    }

    public long getTimeStamp(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, day, 0, 0, 0);
        Date dates = cal.getTime();
        long dd = dates.getTime();
        return dd;
    }

    public LatLngBounds getBounds() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(85, -180));
        builder.include(new LatLng(-85, 180));
        LatLngBounds mBounds = builder.build();
        return mBounds;
    }

    public boolean isMyServiceRunning(Class<?> serviceClass, Context mContext) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (!enabled) {
            view.setAlpha(0.5f);
        } else {
            view.setAlpha(1f);
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (!enabled) {
                    child.setAlpha(0.5f);
                } else {
                    child.setAlpha(1f);
                }
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public void hideKeyBoard(View view, Context context) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void CopyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        // ---copy 1K bytes at a time---
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    public boolean isgpsavailable(Context activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean result = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return result;
    }

    public void showGPSDisabledAlertToUser(final Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Turn ON Location Service").setMessage("Location service is disabled in your device. Would you like to enable it?").setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(callGPSSettingIntent);
                    }
                });
//        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.cancel();
//            }
//        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.setCancelable(true);
        alert.show();
    }

//    public void CreateDB(Context context, String user) {
//        logger.info("Creating a new SQLite DB for : " + user);
//        try {
//            String destPath = AppConstant.KEY_DB_STORAGE_PATH + "/" + user;
//            File f = new File(destPath);
//            if (!f.exists())
//                f.mkdirs();
//            f.createNewFile();
//            CopyFile(context.getAssets().open(AppConstant.DEFAULT_DB_NAME + AppConstant.DB_EXTENTION), new FileOutputStream(destPath + "/" + AppConstant.DEFAULT_DB_NAME + AppConstant.DB_EXTENTION));
//        } catch (FileNotFoundException e) {
//            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
//            logger.error("Error at " + lineNumber, e);
//        } catch (Exception e) {
//            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
//            logger.error("Error at " + lineNumber, e);
//        }
//    }

    public String getDateFormat(int day, int month, int year) {
        String temp_day;
        String monthString = new DateFormatSymbols().getShortMonths()[month - 1];

        if (day >= 11 && day <= 13)
            temp_day = "th ";
        else
            switch (day % 10) {
                case 1:
                    temp_day = "st ";
                    break;
                case 2:
                    temp_day = "nd ";
                    break;
                case 3:
                    temp_day = "rd ";
                    break;
                default:
                    temp_day = "th ";
                    break;
            }
        StringBuilder temp_date = new StringBuilder().append(day).append(temp_day).append(monthString).append(" ").append(year);
        return temp_date.toString();
    }

    public double ensureRange(double value, boolean lat) {
        double min;
        double max;
        if (lat) {
            min = AppConstant.LATITUDE_MIN;
            max = AppConstant.LATITUDE_MAX;
        } else {
            min = AppConstant.LONGITUDE_MIN;
            max = AppConstant.LONGITUDE_MAX;
        }
        return Math.min(Math.min(value, min), max);
    }

    public boolean inRange(double value, boolean lat) {
        double min;
        double max;
        if (lat) {
            min = AppConstant.LATITUDE_MIN;
            max = AppConstant.LATITUDE_MAX;
        } else {
            min = AppConstant.LONGITUDE_MIN;
            max = AppConstant.LONGITUDE_MAX;
        }
        return (value >= min) && (value <= max);
    }

    public boolean checkpermission(Activity context, String[] permission) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<Integer> temp = new ArrayList();
            int hasWrite;
            for (int i = 0; i < permission.length; i++) {
                hasWrite = context.checkSelfPermission(permission[i]);
                if (hasWrite != PackageManager.PERMISSION_GRANTED)
                    temp.add(i);
            }
            return temp.size() == 0;
        } else
            return true;
    }

    public boolean check6Compact(Activity context, String[] permission, int responseCode, int message) {
        return check6Compact(null, context, null, null, null, permission, responseCode, message);
    }

    public boolean check6Compact(Fragment context, String[] permission, int responseCode, int message) {
        return check6Compact(null, null, context, null, null, permission, responseCode, message);
    }

    public boolean check6Compact(androidx.fragment.app.Fragment context, String[] permission, int responseCode, int message) {
        return check6Compact(null, null, null, context, null, permission, responseCode, message);
    }

    public boolean check6Compact(Service context, String[] permission, int responseCode, int message) {
        return check6Compact(null, null, null, null, context, permission, responseCode, message);
    }

    @SuppressLint("WrongConstant")
    private boolean check6Compact(final Context ctx, final Activity act, final Fragment frag, final androidx.fragment.app.Fragment frag1, final Service serv, String[] permission, final int responseCode, int message) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasWriteContactsPermission = -1;
            int size = permission.length;
            ArrayList<Integer> temp = new ArrayList();
            int j = 0;
            for (int i = 0; i < size; i++) {
                if (act != null)
                    hasWriteContactsPermission = act.checkSelfPermission(permission[i]);
                if (frag != null)
                    hasWriteContactsPermission = frag.getActivity().checkSelfPermission(permission[i]);
                if (frag1 != null)
                    hasWriteContactsPermission = frag1.getActivity().checkSelfPermission(permission[i]);
                if (serv != null)
                    hasWriteContactsPermission = serv.checkSelfPermission(permission[i]);
                if (ctx != null)
                    hasWriteContactsPermission = ctx.checkSelfPermission(permission[i]);
                if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED)
                    temp.add(i);
            }
            size = temp.size();
            if (ctx != null) {
                return size > 0;
            }
            final String[] temp_permission = new String[size];
            for (int i = 0; i < size; i++)
                temp_permission[j++] = permission[temp.get(i)];
            if (temp_permission.length > 0) {
                boolean check = true;
                size = temp_permission.length;
                for (int i = 0; i < size; i++) {
                    if (act != null)
                        check = act.shouldShowRequestPermissionRationale(temp_permission[i]);
                    if (frag != null)
                        check = frag.shouldShowRequestPermissionRationale(temp_permission[i]);
                    if (frag1 != null)
                        check = frag1.shouldShowRequestPermissionRationale(temp_permission[i]);
                    if (!check)
                        break;
                }
                if (!check) {
                    Context temp_ctx = null;
                    if (act != null)
                        temp_ctx = act;
                    if (frag != null)
                        temp_ctx = frag.getActivity();
                    if (frag1 != null)
                        temp_ctx = frag1.getActivity();
                    showMessageOKCancel(temp_ctx, temp_ctx.getResources().getString(message),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (act != null)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            act.requestPermissions(temp_permission, responseCode);
                                        }
                                    if (frag != null)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            frag.requestPermissions(temp_permission, responseCode);
                                        }
                                    if (frag1 != null)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            frag1.requestPermissions(temp_permission, responseCode);
                                        }
                                }
                            });
                } else {
                    if (act != null)
                        act.requestPermissions(temp_permission, responseCode);
                    if (frag != null)
                        frag.requestPermissions(temp_permission, responseCode);
                    if (frag1 != null)
                        frag1.requestPermissions(temp_permission, responseCode);
                }
                return false;
            } else
                return true;
        } else
            return true;
    }

    private void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setCancelable(false)
                .create()
                .show();
    }

    public String getUniqueId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
        String deviceId = deviceUuid.toString();
//        00000000-6def-1d7d-0000-00006def1d7d      My Mobile
//        ffffffff-eb24-d419-ffff-ffffeb24d419      Moto x Emulator
        return deviceId;
    }

    public ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }

    public SpannableString setSuperScript(String text) {
        SpannableString sb = new SpannableString(text);
        int prevIndex = 0;
        int index = 0;
        text.indexOf("®", prevIndex);
        while (index > -1) {
            index = text.indexOf("®", prevIndex);
            if (index > -1) {
                sb.setSpan(new SuperscriptSpan(), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.6f), index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            prevIndex = index + 1;
        }
        return sb;
    }

    public void drawRoute(GoogleMap mMap, ArrayList<LatLng> points, float width, int color) {
        mMap.addPolyline(new PolylineOptions().addAll(points).width(width).color(color));
    }

    public Marker drawMarker(GoogleMap mMap, LatLng position, int icon, String title, String snippet) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(title).snippet(snippet).icon(BitmapDescriptorFactory.fromResource(icon)));
        return marker;
    }

    public double distanceOfPoint(LatLng latLng, ArrayList<LatLng> polyline) {
        int total = polyline.size();
        double distance = Double.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < total; i++) {
            LatLng temp = polyline.get(i);
//            logger.info("temp : " + temp.toString());
            distance = distance(temp.latitude, temp.longitude, latLng.latitude, latLng.longitude, 'K') * 1000;
//            logger.info("distance : " + i + " " + distance);
            if (distance < minDistance)
                minDistance = distance;
        }
        return minDistance;
    }

    public Toast DisplayToast(Context context, String msg, int length, int gravity) {
        if (toast != null)
            toast.cancel();

        toast = Toast.makeText(context, msg, length);
        if (gravity == Gravity.TOP)
            toast.setGravity(gravity, 0, 200);
        else if (gravity == Gravity.BOTTOM)
            toast.setGravity(gravity, 0, 100);
        else if (gravity == Gravity.CENTER)
            toast.setGravity(gravity, 0, 200);
        toast.show();
        return toast;
    }

    public float getOriginalFontSize(int density, float size) {
        float finalSize = 0;
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                finalSize = size;
                break;
            case DisplayMetrics.DENSITY_280:
            case DisplayMetrics.DENSITY_HIGH:
                finalSize = size / 2;
                break;
            case DisplayMetrics.DENSITY_360:
            case DisplayMetrics.DENSITY_400:
            case DisplayMetrics.DENSITY_420:
            case DisplayMetrics.DENSITY_XHIGH:
                finalSize = size / 2;
                break;
            case DisplayMetrics.DENSITY_560:
            case DisplayMetrics.DENSITY_XXHIGH:
                finalSize = size / 3;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                finalSize = size / 4;
                break;
        }
        return finalSize;
    }

    public int[] secondsToHoursMinutesSeconds(int seconds) {
        int[] result = {0, 0, 0};
        result[0] = seconds / 3600;
        result[1] = (seconds % 3600) / 60;
        result[2] = (seconds % 3600) % 60;
        return result;
    }

    private String CHANNEL_ID;

    private void createNotificationChannel(Context context) {
        CharSequence channelName = CHANNEL_ID;
        String channelDesc = "channelDesc";
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDesc);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            NotificationChannel currChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (currChannel == null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    public void scheduleNotification(Context context, String title, String name, int id, boolean ongoing) {
        Notification note = getNotification(context, title, name, id, ongoing);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, note);
    }

    public Notification getNotification(Context context, String title, String name, int id, boolean ongoing) {
        CHANNEL_ID = "Routewise";
        createNotificationChannel(context);
        Intent i = new Intent(context, MainMenu.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        Notification note = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(name)
                .setOngoing(ongoing)
                .setContentIntent(pi).build();
        return note;
    }

    public void noti(Context context, String title, String name, int id, boolean ongoing) {
        if (!BuildConfig.RELEASE) {
            scheduleNotification(context, title, name, id, ongoing);
        }
    }

    public long[] calcDiff() {
        long startTime = mPrefer.getLong(AppConstant.AUTO_ENABLE_TIME_START, 0);
        long afterTime = mPrefer.getLong(AppConstant.AUTO_ENABLE_TIME, 0);

        long currentTime = System.currentTimeMillis() / 1000;
        long temp = startTime + afterTime;
        long diff = temp - currentTime;
        long day = diff / (24 * 60 * 60);
        long temp0 = diff / (60 * 60);
        long hoursTime = temp0 - (day * 24);
        long temp1 = diff / 60;
        long minTime = temp1 - (day * 24 * 60) - (hoursTime * 60);

        return new long[]{day, hoursTime, minTime, diff};
    }

    public Cursor getLastLocationData() {
        Db.open();
        return Db.getLastLocationData();
    }

    public void setUploadedFlag(String[] arrayString) {
        ArrayList<String[]> splitted = new ArrayList<>();//This list will contain all the splitted
        int lengthToSplit = 800;
        int arrayLength = arrayString.length;
        for (int i = 0; i < arrayLength; i = i + lengthToSplit) {
            String[] val = new String[lengthToSplit];
            if (arrayLength < i + lengthToSplit) {
                lengthToSplit = arrayLength - i;
            }
            System.arraycopy(arrayString, i, val, 0, lengthToSplit);
            splitted.add(val);
        }

        for (int i = 0; i < splitted.size(); i++) {
            Db.open();
            Db.setUploadedFlag(splitted.get(i));
        }
    }


    public long currenttime(long time){

        return time/1000;
    }
    public long[] getLastData() {
        long[] result = {0, 0, 0};
        try {
            getPreferenceValues();
            Db.open();
            Cursor start = Db.getLastLocationData();
            int count = start.getCount();
            if (count > 0) {
                start.moveToFirst();
                result[0] = start.getLong(2);
                start.moveToNext();
                result[1] = start.getLong(2);
                result[2] = result[0] - result[1];
            }
            start.close();
            close();
        } catch (Exception e) {

        }
        return result;
    }

    public Cursor getAllData() {
//        getPreferenceValues();
        Db.open();
        Cursor data = Db.getValues();
        return data;
    }


//    public ArrayList<Location> getLastLocationData() {
//        getPreferenceValues();
//        ArrayList<Location> newArray = new ArrayList<>();
//        Db.open(mCurrentUser);
//        Cursor data = Db.getValues();
//        int count = data.getCount();
//        if (count > 0) {
//            data.moveToFirst();
//            for (int i = 0; i < count; i++) {
//                Location location = new Location("");
//                location.setLatitude(data.getDouble(0));
//                location.setLongitude(data.getDouble(1));
//                location.setTime(data.getLong(2));
//                location.setSpeed(data.getFloat(3));
//                location.setAltitude(data.getDouble(4));
//                location.setBearing(data.getFloat(5));
//                location.setAccuracy(data.getFloat(6));
//                newArray.add(location);
//            }
//            data.moveToNext();
//        }
//        data.close();
//        return newArray;
//    }

    public void deleteOldData() {
        long currentTime = System.currentTimeMillis() / 1000;
        long lastSQLiteFlush = mPrefer.getLong(AppConstant.LAST_SQLITE_FLUSH, 0);
        if (lastSQLiteFlush == 0) {
            lastSQLiteFlush = currentTime;
        } else {
            long diff = currentTime - lastSQLiteFlush;
            if (diff > AppConstant.ONE_DAY_INTERVAL_IN_SECONDS) {
                // Remove 1 week old Data
                long weekCount = AppConstant.ONE_DAY_INTERVAL_IN_SECONDS * 7;
                String weekBack = String.valueOf(currentTime - weekCount);
                Db.open();
                Db.deleteData(weekBack);
                lastSQLiteFlush = currentTime;
            }
        }
        edit = mPrefer.edit();
        edit.putLong(AppConstant.LAST_SQLITE_FLUSH, lastSQLiteFlush);
        edit.apply();
    }

    public void insertLocation(LocationDetails location) {
        long time = System.currentTimeMillis();
        // Can divide by 1000
        long locTime = location.getTime();
        String time1 = fetchDate(time);
        String time2 = fetchDate(locTime * 1000);
        Db.open();
        Db.insertLocation(location);
    }

    public void storeaccelerometer(BasicModel details) {
        Db.open();
        Db.insertAccelerometer(details);

    }

    public void storegravity(BasicModel details) {
        Db.open();
        Db.insertgravityrecords(details);

    }

    public void storemagnaticfields(BasicModel basicModel){
        Db.open();
        Db.insertmagnaticfields(basicModel);
    }
    public void storelight(Light_details light_details){
        Db.open();
        Db.insertlightsensor(light_details);

    }

    public void storegyrocope(BasicModel basicModel){
        Db.open();
        Db.insertgyroscope(basicModel);

    }

    public void storeactivities(ActivityModel activityModel){
        Db.open();
        Db.insertactivitydetails(activityModel);
    }

    public void storescreenstatus(ScreenStatus_model screenStatus){
        Db.open();
        Db.insertscreenstatus(screenStatus);
    }

    public void  storeproximity(BasicModel basicModel){
        Db.open();
        Db.insertproximity(basicModel);
    }

    public void insertLoc(Location location, long addTime, boolean addDummy, boolean isBuffer) {
        getPreferenceValues();
        float accuracy = 0;
        double lat = 0;
        double lon = 0;
        float speed = 0;
        long time = 0;
        double altitude = 0;
        float bearing = 0;

        if (location == null) {
            Cursor loc = getLastLocationData();
            int count = loc.getCount();
            if (count > 0) {
                loc.moveToFirst();
                lat = loc.getDouble(0);
                lon = loc.getDouble(1);
//            time = loc.getLong(2) / 1000;
                time = loc.getLong(2);
                if (addDummy) {
                    lat = lat + 0.0003;
                    lon = lon + 0.0003;
                }
                time = time + addTime;
            }
            loc.close();
            close();
        } else {
            accuracy = location.getAccuracy();
            lat = location.getLatitude();
            lon = location.getLongitude();
            speed = location.getSpeed();
            time = location.getTime() / 1000;
//            // If time difference of the location is too old use this
            long currentTime = System.currentTimeMillis() / 1000;
            logger.info("insertLoc currentTime : " + currentTime + "  locTime : " + time);
            if (addTime == 1) {
                time = currentTime;
            }
            altitude = location.getAltitude();
            bearing = location.getBearing();
        }

        if (lat != 0) {
            LocationDetails details = new LocationDetails();
            details.setLatitude(lat);
            details.setLongitude(lon);
            details.setSpeed(speed);
            details.setTime(time);
            details.setAccuracy(accuracy);
            details.setAltitude(altitude);
            details.setBearing(bearing);

            insertLocation(details);
            if (!isBuffer) {
                mInstance.lastLocTime = System.currentTimeMillis() / 1000;
                edit = mPrefer.edit();
                edit.putLong(AppConstant.LAST_LOC_TIME, mInstance.lastLocTime);
                edit.commit();
            }
        }
    }

    public void close() {
        Db.close();
    }

    public String getActivityName(DetectedActivity activity) {
        int type = activity.getType();
        String act = "";
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                act = "IN_VEHICLE";
                break;
            case DetectedActivity.ON_BICYCLE:
                act = "ON_BICYCLE";
                break;
            case DetectedActivity.ON_FOOT:
                act = "ON_FOOT";
                break;
            case DetectedActivity.RUNNING:
                act = "RUNNING";
                break;
            case DetectedActivity.STILL:
                act = "STILL";
                break;
            case DetectedActivity.TILTING:
                act = "TILTING";
                break;
            case DetectedActivity.WALKING:
                act = "WALKING";
                break;
            case DetectedActivity.UNKNOWN:
                act = "UNKNOWN";
                break;
            default:
                act = "DEFAULT";
                break;
        }
        return act;
    }

    public int getLineNumber(Exception e, String classname) {
        int linenumber = 0;
        try {
            StackTraceElement[] trace = e.getStackTrace();
            for (int i = 0; i < trace.length; i++) {
                if ((trace[i].getClassName()).equals(classname)) {
                    linenumber = e.getStackTrace()[i].getLineNumber();
                    break;
                }
            }
        } catch (Exception er) {
            lineNumber = getLineNumber(er, getClass().getName());
            logger.error("Error at " + lineNumber, er);
        }
        return linenumber;
    }

    public double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
//        if (unit == 'K') {
//            dist = dist * 1.609344;
//        } else if (unit == 'N') {
//            dist = dist * 0.8684;
//        }
        if (Double.isNaN(dist))
            return 0;
        else
            return dist;
    }

    public double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public String passwordEncrypter(String plaintext) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            lineNumber = getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
        md5.reset();
        md5.update(plaintext.getBytes());
        byte[] digest = md5.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashtext = bigInt.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    public boolean isValidMail(String email) {
        if (email == null || "".equals(email))
            return false;
        email = email.trim();
        EmailValidator ev = EmailValidator.getInstance();
        return ev.isValid(email);
    }

    public boolean isValidPasswordFormat(String password) {
        boolean valid = password.matches(AppConstant.PASSWORD_REGEX);
        return valid;
    }

    public boolean isValidPhone(String phone) {
        boolean valid = phone.matches(AppConstant.PHONE_REGEX);
        return valid;
    }

    public boolean isValidName(String fullName) {
        boolean valid = fullName.matches(AppConstant.FULL_NAME_REGEX);
        return valid;
    }

    public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }
            String[] children = sourceLocation.list();
            if (children != null)
                for (int i = 0; i < children.length; i++) {
                    copyDirectory(new File(sourceLocation, children[i]),
                            new File(targetLocation, children[i]));
                }
        } else {
            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public void getIndex(String item, ArrayList list, CharacterPickerWindow picker) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).toString().equals(item)) {
                picker.setSelectOptions(i);
                break;
            }
        }
    }

    //Method to set error message and cross mark on validation failed
    public void setMessageOnValidationError(CustomTextView textView, ImageView imageView, String message) {
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.ic_erroricon);
        imageView.setVisibility(View.VISIBLE);
    }

    //Method to set tick mark on validation pass
    public void setMessageOnValidationPass(CustomTextView textView, ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_noerroricon);
        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.INVISIBLE);
    }

    //Method to clear error message of particular field
    public void clearMessage(CustomTextView textView, ImageView imageView) {
        textView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
    }

//    //Method to set response error
//    public void setMessageForResponse(CustomTextView textView, String message, ImageView imageView) {
//        textView.setText(message);
//        textView.setVisibility(View.VISIBLE);
//        imageView.setVisibility(View.INVISIBLE);
//    }

    public void saveLocation(Location location, String key) {
        edit = mPrefer.edit();
        if (location == null) {
            edit.remove(key + "_LAT");
            edit.remove(key + "_LON");
            edit.remove(key + "_PROVIDER");
            edit.remove(key + "_TIME");
            edit.remove(key + "_SPEED");
            edit.remove(key + "_ALTITUDE");
            edit.remove(key + "_BEARING");
            edit.remove(key + "_ACCURAACY");
        } else {
            edit.putString(key + "_LAT", String.valueOf(location.getLatitude()));
            edit.putString(key + "_LON", String.valueOf(location.getLongitude()));
            edit.putString(key + "_PROVIDER", location.getProvider());
            edit.putString(key + "_TIME", String.valueOf(location.getTime()));
            edit.putString(key + "_SPEED", String.valueOf(location.getSpeed()));
            edit.putString(key + "_ALTITUDE", String.valueOf(location.getAltitude()));
            edit.putString(key + "_BEARING", String.valueOf(location.getBearing()));
            edit.putString(key + "_ACCURAACY", String.valueOf(location.getAccuracy()));
        }
        edit.apply();
    }

    public Location retriveLocation(String key) {
        String lat = mPrefer.getString(key + "_LAT", null);
        String lon = mPrefer.getString(key + "_LON", null);
        Location location = null;
        if (lat != null && lon != null) {
            String provider = mPrefer.getString(key + "_PROVIDER", null);
            String time = mPrefer.getString(key + "_TIME", null);
            String speed = mPrefer.getString(key + "_SPEED", null);
            String altitude = mPrefer.getString(key + "_ALTITUDE", null);
            String bearing = mPrefer.getString(key + "_BEARING", null);
            String accuracy = mPrefer.getString(key + "_ACCURAACY", null);
            location = new Location(provider);
            location.setLatitude(Double.parseDouble(lat));
            location.setLongitude(Double.parseDouble(lon));
            location.setTime(Long.parseLong(time));
            location.setSpeed(Float.parseFloat(speed));
            location.setAltitude(Double.parseDouble(altitude));
            location.setBearing(Float.parseFloat(bearing));
            location.setAccuracy(Float.parseFloat(accuracy));
        }
        return location;
    }

    public void saveLocationArray(List<Location> location, String key) {
        int size = location.size();
        edit = mPrefer.edit();
        edit.putInt(key + "_COUNT", size);
        edit.apply();
        for (int i = 0; i < size; i++) {
            Location loc = location.get(i);
            saveLocation(loc, key + i);
        }
    }

    public ArrayList<Location> retriveLocationArray(String key) {
        ArrayList<Location> locArray = new ArrayList<>();
        int size = mPrefer.getInt(key + "_COUNT", 0);
        for (int i = 0; i < size; i++) {
            Location loc = retriveLocation(key + i);
            locArray.add(loc);
        }
        return locArray;
    }

    public String calculateTime(int time) {
        String timeTaken;
        int sec;
        int minutes = time / 60;
        sec = time % 60;
        /*if (time % 60 > 0) {
            minutes++;
        }*/
        int hours = minutes / 60;
        minutes = minutes % 60;
        if (hours == 0) {
            timeTaken = minutes + " min " + sec + " sec";
        } else {
            timeTaken = hours + " hrs " + minutes + " min";
        }
        return timeTaken;
    }

    public String fetchTime(long timeStamp) {
        Date timeData = new Date(timeStamp);
        DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        String timeInRequiredFormat = timeFormat.format(timeData);
        return timeInRequiredFormat;
    }

    public String fetchDate(long timeStamp) {
        Date dateData = new Date(timeStamp);
        DateFormat date = new SimpleDateFormat("MMM dd yyyy");
        String timeInRequiredFormat = fetchTime(timeStamp);
        String dateString = date.format(dateData);
        return dateString + ", " + timeInRequiredFormat;
    }

    public String getTime() {
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("hh:mm a");
        String date = simpledateformat.format(calender.getTime());
        return date;
    }

    public String fetchDistance(double distance) {
        double tripLength = distance * 0.000621371;
        tripLength = round(tripLength, 2);
//        if (tripLength < 1) {
//            tripLength = (double) Math.round(tripLength * 100) / 100;
//        } else {
//            tripLength = (double) Math.round(tripLength * 10) / 10;
//        }
        return tripLength + " miles";
    }

    public String fetchDistanceAndTime(double distance, int time) {
        double tripLength = distance * 0.000621371;
        tripLength = round(tripLength, 2);
//        tripLength = Math.round(tripLength * 10) / 10;
        String timeString = calculateTime(time);
        return tripLength + "mi  -  " + timeString;
    }

    public String apiKeyEncrypter() {
        String result = null;
        String IVString = "IQ20LOV4IZES1F01";
        long date = System.currentTimeMillis();
        String temp = AppConstant.API_KEY;
        long lastUpdate = mPrefer.getLong(AppConstant.UPDATE_APP_MANDATE_TIME, 0) / 1000;
        String key = temp + AppConstant.API_KEY_DIVIDER1 + date + AppConstant.API_KEY_DIVIDER2 + BuildConfig.VERSION_NAME + AppConstant.API_KEY_DIVIDER1 + BuildConfig.VERSION_CODE + AppConstant.API_KEY_DIVIDER2 + lastUpdate;
        try {
            SecretKeySpec skc;
            skc = new SecretKeySpec(AppConstant.API_ENCRYPTION_KEY.getBytes(), "AES");
            final byte[] IV = IVString.getBytes();
            final IvParameterSpec ivSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, skc, ivSpec);
            byte[] stringBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(stringBytes);
            result = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
        result = result.replace("\n", "");
        return result;
    }

    public String dataEncrypt(String data) {
        String result = null;
        String IVString = "IQ20LOV4IZES1F01";
        try {
            SecretKeySpec skc;
            skc = new SecretKeySpec(AppConstant.DATA_ENCRYPTION_KEY.getBytes(), "AES");
            final byte[] IV = IVString.getBytes();
            final IvParameterSpec ivSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, skc, ivSpec);
            byte[] stringBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(stringBytes);
            result = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e1) {
            lineNumber = getLineNumber(e1, getClass().getName());
            logger.error("Error at " + lineNumber, e1);
            e1.printStackTrace();
        }
        result = result.replace("\n", "");
        return result;
    }

    public void delete(final Context context, boolean send) {
//        mInstance.mTracking = false;
        mInstance.subscribed = false;
        mInstance.tracking_enabled = false;
        edit = mPrefer.edit();
        edit.remove(AppConstant.KEEP_ME_LOGGED_IN);
        edit.remove(AppConstant.TOKEN);
        edit.remove(AppConstant.USER);
        edit.remove(AppConstant.PAGER_VALUE0);
        edit.remove(AppConstant.PAGER_VALUE1);
        edit.remove(AppConstant.INSURANCE_FIRST_NAME);
        edit.remove(AppConstant.INSURANCE_LAST_NAME);
        edit.remove(AppConstant.INSURANCE_AGE);
        edit.remove(AppConstant.INSURANCE_PHONE_NUMBER);
        edit.remove(AppConstant.INSURANCE_EMAIL);
        edit.remove(AppConstant.INSURANCE_ADDRESS);
//        edit.remove(AppConstant.TRACKING);
        edit.remove(AppConstant.TRACKING_ENABLED);
        edit.remove(AppConstant.SUBSCRIBED);
        for (int i = 0; i < 5; i++) {
            edit.remove(AppConstant.SEARCHED_ADDRESS + i);
        }
        edit.commit();

        if (send) {
            final boolean check = isAppOnForeground(mInstance);
            logger.info("App Foreground check : " + check);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent restart = new Intent(context, MainActivity.class);
                    logger.info("Inside Restart Handler");
                    restart.putExtra(AppConstant.KEEP_ME_LOGGED_IN, false);
                    if (!check)
                        restart.putExtra(AppConstant.QUIT_APP, true);
                    restart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(restart);

                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }, 4000);
        }
    }

    public void logout() {
        edit = mPrefer.edit();
        edit.remove(AppConstant.KEEP_ME_LOGGED_IN);
        edit.remove(AppConstant.PAGER_VALUE0);
        edit.remove(AppConstant.PAGER_VALUE1);
        //added by Vishal to clear stored Insurance form fields values
        edit.remove(AppConstant.INSURANCE_FIRST_NAME);
        edit.remove(AppConstant.INSURANCE_LAST_NAME);
        edit.remove(AppConstant.INSURANCE_AGE);
        edit.remove(AppConstant.INSURANCE_PHONE_NUMBER);
        edit.remove(AppConstant.INSURANCE_EMAIL);
        edit.remove(AppConstant.INSURANCE_ADDRESS);
        edit.remove(AppConstant.TRIPLIST);
        edit.commit();
        mInstance.getRequestQueue().getCache().clear();
        logger.info("User Logged Out");
    }

    public void alertBox(final Activity activity, String title, String message, String positiveText, String negativeText, DialogInterface.OnClickListener okListener) {
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(activity);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(positiveText, okListener);
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    public void checkResponse(JSONArray response, int code, Context context) {
        JSONObject result = null;
        int status = 0;
        if (response != null)
            result = response.optJSONObject(0);
        if (result != null)
            status = result.optInt("status");

        if (status == AppConstant.RESPONSE_UPDATE_APP_MANDATE || code == AppConstant.RESPONSE_UPDATE_APP_MANDATE) {
            logger.info("App Update Neccessary");
            scheduleNotification(context, "UPDATE", "You need to update your app", AppConstant.NOTIFY_TRIP_COMPLETE, false);
            showUpdatePopup(true, true, context);
        } else if (status == AppConstant.RESPONSE_UPDATE_APP || code == AppConstant.RESPONSE_UPDATE_APP) {
            logger.info("App Update Available");
            long currentTime = System.currentTimeMillis();
            long lastUpdate = mPrefer.getLong(AppConstant.UPDATE_APP_MANDATE_TIME, 0);

            boolean show = false;
            long diff = currentTime - lastUpdate;
            if (diff > 86400) {
                lastUpdate = currentTime;
                edit = mPrefer.edit();
                edit.putLong(AppConstant.UPDATE_APP_MANDATE_TIME, lastUpdate);
                edit.commit();
                show = true;
            }
            if (show) {
                scheduleNotification(context, "UPDATE", "There is a new update available", AppConstant.NOTIFY_TRIP_COMPLETE, false);
                showUpdatePopup(true, false, context);
            }
        }
    }

    //    public void showUpdatePopup(boolean update, boolean mandate, final Activity activity, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
    public void showUpdatePopup(boolean update, boolean mandate, final Context context) {
        if (context == null) {
            edit = mPrefer.edit();
            edit.putBoolean(AppConstant.UPDATE_APP, update);
            edit.putBoolean(AppConstant.UPDATE_APP_MANDATE, mandate);
            edit.commit();
        } else {
            if (update || mandate) {
                try {
                    String message = "App update availbale, Do you want to update?";
                    if (mandate)
                        message = "Update your app to continue, Do you want to update?";

                    androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context);
                    alertDialog.setTitle("UPDATE");
                    alertDialog.setMessage(message);
                    alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openPlayStore(context);
                        }
                    });
                    if (!mandate)
                        alertDialog.setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                long currentTime = System.currentTimeMillis();
                                edit = mPrefer.edit();
                                edit.putLong(AppConstant.UPDATE_APP_MANDATE_TIME, currentTime);
                                edit.commit();
                                dialog.cancel();
                            }
                        });
                    else
                        alertDialog.setCancelable(false);
                    alertDialog.show();

                    edit = mPrefer.edit();
                    edit.remove(AppConstant.UPDATE_APP);
                    edit.remove(AppConstant.UPDATE_APP_MANDATE);
                    edit.commit();
                } catch (Exception e) {
                    lineNumber = getLineNumber(e, getClass().getName());
                    logger.error("Error at " + lineNumber, e);
                }
            }
        }
    }

    public void openPlayStore(Context context) {
        String packageName = context.getPackageName();
        packageName = "com.tnedicca.routewise";
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Try Google play
        intent.setData(Uri.parse("market://details?id=" + packageName));
        if (!checkActivity(context, intent)) {
            // Market (Google play) app seems not installed, let's try to open a
            // webbrowser
//            intent.setData(Uri.parse("https://play.google.com/apps/testing/com.kcube.tripsnirvana.activity"));
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            if (!checkActivity(context, intent)) {
                // Well if this also fails, we have run out of options, inform
                // the user.
                DisplayToast(context, "Could not open android market, please install the market app.", Toast.LENGTH_LONG, Gravity.BOTTOM);
            }
        }
    }

    private boolean checkActivity(Context context, Intent aIntent) {
        try {
            context.startActivity(aIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public boolean isProcessRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            String process = procInfos.get(i).processName;
            if (process.equals(context.getPackageName() + AppConstant.PROCESS_SERVICE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInRecentList(Context mContext) {
        if (mContext == null)
            return false;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> rec = am.getRecentTasks(10, ActivityManager.RECENT_WITH_EXCLUDED);
        for (int i = 0; i < rec.size(); i++) {
            ActivityManager.RecentTaskInfo task = rec.get(i);
            ComponentName act = task.baseIntent.getComponent();
            if (act.getPackageName().equals(mContext.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public double round(double value, int places) {
        try {
            if (places < 0)
                throw new IllegalArgumentException();
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } catch (Exception e) {
            lineNumber = getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
            return value;
        }
    }
}