package com.tnedicca.routewise.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.Filler;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.classes.VolleyMultipartRequest;
import com.tnedicca.routewise.classes.ZipManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SyncHelper {

    private final Context mContext;
    private ArrayList<String> volleyArray = new ArrayList<>();
    private final RouteWise mInstance;
    private final RouteLog logger;
    private final SharedPreferences mPrefer;
    private String mAccessToken;
    private int lineNumber;
    private boolean mSentBackLog = false;
    private boolean syncedLastTrip = true;
    private long lastSyncResetTime = 0;
    private SharedPreferences.Editor edit;
    private boolean subscribed;
    private String uploadDataTypeSelected;
    private int dataUploadCounter;
    private Filler mFiller;
    private ProgressDialog progressDialog;
    private JSONArray idsArray = new JSONArray();
    private String[] zipName = {""};

    public SyncHelper(Context context) {
        mContext = context;
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(SyncHelper.class);

        mFiller = new Filler(mContext);
        progressDialog = new ProgressDialog(mContext);
        mPrefer = context.getSharedPreferences(AppConstant.PREFERENCE, context.MODE_PRIVATE);
        getPreferenceValues();
    }

    private void getPreferenceValues() {
        syncedLastTrip = mPrefer.getBoolean(AppConstant.SYNC_LAST_TRIP, true);
        subscribed = mPrefer.getBoolean(AppConstant.SUBSCRIBED, false);
        mAccessToken = mPrefer.getString(AppConstant.TOKEN, null);
        uploadDataTypeSelected = mPrefer.getString(AppConstant.DATA_UPLOAD_CONDITION, AppConstant.BOTH);
    }

    public void syncOldData() {
        getPreferenceValues();
        long currentTime = System.currentTimeMillis() / 1000;
        if (lastSyncResetTime == 0) {
            lastSyncResetTime = currentTime;
        } else {
            long syncDiff = currentTime - lastSyncResetTime;
            logger.info("syncOldData lastSyncResetTime : " + lastSyncResetTime + "  currentTime : " + currentTime);
            if (syncDiff > AppConstant.SYNC_INTERVAL) {
                Cursor data = mInstance.myLibrary.getAllData();
                int count = data.getCount();
                logger.info("syncOldData count : " + count);
                if (count > 2) {
                    syncedLastTrip = false;
                    logger.info("Reset sync Time");
                } else if (count == 2) {
                    syncedLastTrip = false;
                    logger.info("Backup Update");
                } else {
                    lastSyncResetTime = currentTime;
                    String url = BuildConfig.REST_URL + AppConstant.PING_URL;
                    int method = Request.Method.GET;
                    String tag = AppConstant.PING;
                    makeJsonArryReq(url, method, tag, null);
                }
            }
        }

//        syncedLastTrip = false;
        if (!syncedLastTrip) {
            lastSyncResetTime = currentTime;
            syncedLastTrip = true;
            logger.info("Syncronize started for old trip");
            sendToServer(true);
        }

        edit = mPrefer.edit();
        edit.putBoolean(AppConstant.SYNC_LAST_TRIP, syncedLastTrip);
        edit.apply();
    }

    public void sendToServer(boolean backLog) {
        logger.info("Uploading to Server");
        getPreferenceValues();
        try {
            if (mInstance.dataStatus) {
                if (uploadDataTypeSelected.equals("")) {
                    if (dataUploadCounter > 2)
                        uploadDataTypeSelected = mPrefer.getString(AppConstant.DATA_UPLOAD_CONDITION, AppConstant.BOTH);
                    else
                        dataUploadCounter++;
                }
                if (AppConstant.BOTH.equals(uploadDataTypeSelected) || (mInstance.dataType.equals(uploadDataTypeSelected))) {
                    if (subscribed) {
                        Cursor data = mInstance.myLibrary.getAllData();
                        int count = data.getCount();
                        logger.info("Data Count : " + count);
                        if (count > 0) {
                            ArrayList<Long> timeArray = new ArrayList<>();
                            JSONArray jsonArray = new JSONArray();

                            boolean tempBackLog = backLog;
                            int size = count;
                            if (count > AppConstant.MAX_SYNC_COUNT) {
                                size = AppConstant.MAX_SYNC_COUNT;
                                tempBackLog = false;
                            }
                            if (backLog) {
                                long[] timeStamps = mInstance.myLibrary.getLastData();
                                if (timeStamps[2] >= AppConstant.LOCATION_SPLIT_INTERVAL)
                                    tempBackLog = false;
                            }
                            data.moveToFirst();
                            for (int i = 0; i < size; i++) {
                                boolean insert = true;
                                if (i >= size - 2) {
                                    if (tempBackLog)
                                        insert = false;
                                }
                                if (insert) {
                                    String id = data.getString(0);
                                    double lat = data.getDouble(1);
                                    double lon = data.getDouble(2);
                                    long time = data.getLong(3);
                                    float speed = data.getFloat(4);
                                    boolean is_uploaded = Boolean.parseBoolean(data.getString(8));

                                    if (!is_uploaded) {
                                        JSONObject dataJson = new JSONObject();
                                        dataJson.put(AppConstant.KEY_LATITUDE, lat);
                                        dataJson.put(AppConstant.KEY_LONGITUDE, lon);
                                        dataJson.put(AppConstant.KEY_TIME, time);
                                        dataJson.put(AppConstant.KEY_SPEED, speed);
                                        if (!timeArray.contains(time)) {
                                            timeArray.add(time);
                                            jsonArray.put(dataJson);
                                        }
                                        idsArray.put(id);
                                    }
                                }
                                data.moveToNext();
                            }
                            data.close();
                            mInstance.myLibrary.close();

                            int length = jsonArray.length();
                            logger.info("JsonArray Count : " + length + "  IDS : " + idsArray);
                            if (length > 0) {
                                JSONObject sendingData = new JSONObject();
                                sendingData.put(AppConstant.SYNC_ROUTE, jsonArray);
                                if (tempBackLog)
                                    sendingData.put(AppConstant.SYNC_DATA_COUNT, size - 2);
                                else
                                    sendingData.put(AppConstant.SYNC_DATA_COUNT, size);

                                mSentBackLog = backLog;
                                String url = BuildConfig.REST_URL + AppConstant.SYNC_URL;
                                int method = Request.Method.POST;
                                String tag = AppConstant.SYNC;
                                makeJsonArryReq(url, method, tag, sendingData);
                            }
                        }
                    }
                }
            } else {
                logger.info("Sync Not done No data Connection");
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final JSONObject sendingData) {
        JsonArrayRequest req = new JsonArrayRequest(url, response -> {
            logger.info("Success Response recieved  " + tag);
            volleyArray.remove(tag);
//                logger.info( getString(R.string.received_response));
            response(tag, response, 200);
        }, error -> {
            logger.info("Error Response recieved  " + tag);
            volleyArray.remove(tag);
//                logger.info( getString(R.string.received_error));
            error(tag, error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                headers.put(AppConstant.REST_ACCESS_TOKEN, mAccessToken);
                return headers;
            }

            @Override
            public byte[] getBody() {
                JSONObject finalToken = new JSONObject();
                String query = mInstance.myLibrary.dataEncrypt(sendingData.toString());
                try {
                    finalToken.put("json", query);
                } catch (Exception e) {
                    lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                    logger.error("Error at " + lineNumber, e);
                }
                return finalToken.toString().getBytes();
            }

            @Override
            public int getMethod() {
                return method;
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String jsonString;
                try {
                    jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    JSONArray jsonArray = new JSONArray(jsonString);
                    jsonArray.put(response.statusCode);
                    return Response.success(jsonArray, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                    logger.error("Error at " + lineNumber, e);
                    return Response.error(new ParseError(e));
                } catch (JSONException e) {
                    lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
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
    private void response(String check, JSONArray response, int code) {
        mInstance.myLibrary.checkResponse(response, code, mContext);
        if (code == AppConstant.RESPONSE_BAD_REQUEST) {
            sendToServer(true);
        } else if (code == AppConstant.RESPONSE_SERVER_ERROR) {
        } else if (code == AppConstant.RESPONSE_GET_LOGS) {
            sendLogs();
        } else if (code == AppConstant.RESPONSE_DEVICE_CHANGED) {
            logger.info("Device Changed");
            mInstance.myLibrary.delete(mContext, true);
//            mInstance.myLibrary.noti(context, "LOC : " + location, AppConstant.NOTIFY_LOC_RECIEVER);
            logger.info("User Removed");
        } else {
            if (check.equals(AppConstant.SYNC)) {
                int status = response.optInt(response.length() - 1);
                if (status == AppConstant.RESPONSE_USER_NOT_AVAILABLE) {
                    logger.info("User Unavailable");
                } else if (status == AppConstant.RESPONSE_SUCCESS || status == AppConstant.RESPONSE_GOOGLE_ANONYMUS) {
                    processResponse(response, mSentBackLog);
                } else if (status == AppConstant.RESPONSE_DATA_EXIST) {
                    logger.info("Data Already Exist");
                    processResponse(response, mSentBackLog);
                } else {
                    logger.info("Sync Failed Result : ");
                }
            }
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error) {
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode);
            } else {
//                mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    private void processResponse(JSONArray response, boolean backLog) {
        JSONObject result = response.optJSONObject(0);
        int count = result.optInt(AppConstant.SYNC_DATA_COUNT);
        logger.info("Data's Deleted Count : " + count + "  IDS : " + idsArray);
        String[] ids = new String[idsArray.length()];
        for (int i = 0; i < idsArray.length(); i++) {
            String id = idsArray.optString(i);
            ids[i] = id;
        }
        idsArray = new JSONArray();
        mInstance.myLibrary.setUploadedFlag(ids);
        logger.info("Data Deleted");
        if (count == AppConstant.MAX_SYNC_COUNT) {
            logger.info("sendToServer called from processResponse");
            sendToServer(backLog);
        } else {
            syncedLastTrip = true;
            edit = mPrefer.edit();
            edit.putBoolean(AppConstant.SYNC_LAST_TRIP, syncedLastTrip);
            edit.apply();
        }
    }

    private void sendLogs() {
        String url = BuildConfig.REST_URL + AppConstant.REPORT_BATTERY_URL;
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    try {
                        stopAndDelete();
                        JSONObject obj = new JSONObject(new String(response.data));
                        boolean result = obj.getBoolean("result");
                        if (result) {
                            mFiller.showSnackBar("Log Sent");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    stopAndDelete();
                    mFiller.showSnackBar("Log Sending Failed : " + error.getMessage());
                }) {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    String dbFolder = AppConstant.KEY_APP_FOLDER + "/databases";
                    String date = mInstance.zipFileDateFormat.format(new Date());
                    String currentUser = mPrefer.getString(AppConstant.USER, null);
                    String fileName = currentUser + "_" + date;
                    zipName[0] = AppConstant.KEY_APP_FOLDER + "/" + fileName + ".zip";
                    ZipManager zipManager = new ZipManager();
//                    zipManager.zipFolder(inputPath, zipName);
                    zipManager.compressDirectory(new String[]{AppConstant.KEY_LOG_FILE_PATH, dbFolder}, zipName[0]);

                    progressDialog.setMessage(AppConstant.UPLOADING_MSG);
                    params.put("uploadedFile", new DataPart(zipName[0]));
                } catch (Exception e) {
                    stopAndDelete();
                    e.printStackTrace();
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put(AppConstant.REST_KEY_CONTENT, getBodyContentType());
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                headers.put(AppConstant.REST_ACCESS_TOKEN, mPrefer.getString(AppConstant.TOKEN, ""));
                return headers;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(mContext).add(volleyMultipartRequest);
    }

    private void stopAndDelete() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            File file = new File(zipName[0]);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
