package com.tnedicca.routewise.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;

import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kyleduo.switchbutton.SwitchButton;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.fragments.CustomMapFragment;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by new on 13-02-2017.
 */

public class DetailedView extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, CompoundButton.OnCheckedChangeListener, GoogleMap.OnMapLoadedCallback, CustomMapFragment.OnTouchListener {

    public boolean autoZoom = false;
    ArrayList<String> volleyArray = new ArrayList<String>();
    PolylineOptions extraPO;
    PolylineOptions originalPO;
    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;
    private RouteWise mInstance;
    private String pathId;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private ProgressDialog progressDialog;
    private CustomTextView routeRiskScore;
    private CustomTextView distance;
    private SwitchButton driverOfTripSwitch;
    private CustomTextView startPoint;
    private CustomTextView endPoint;
    private CustomTextView startTime;
    private CustomTextView stopTime;
    private CustomTextView duration;
    private CustomMapFragment mapFragment;
    private GoogleMap mMap;
    private LatLng startLatLon;
    private LatLng stopLatLon;
    private boolean passengerStatus;
    private int lineNumber;
    private float[] pagerValues = {0, 0};
    private LinearLayout mainLayout;
    private ArrayList<LatLng> autoZoomPoints;
    private float mZoomLevel;
    private double mStartLat;
    private double mStartLon;
    private double mStopLat;
    private double mStopLon;
    private boolean isDataReady = false;
    private boolean isConnectionTimeout = false;
    private CustomButton mSuggestSafeRoute;
    private String startAddress;
    private String endAddress;
    private long startTimeValue;
    private double riskValue;
    private double distanceValue;
    private String tripLengthInMiles;
    private ScrollView scrollView;
    private RouteLog logger;
    private Handler handler1;
    private Handler handler2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailed_view);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(DetailedView.class);

        mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setVisibility(View.INVISIBLE);
        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.title_detail));
//        backIcon.setOnClickListener(this);
        infoIcon.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(AppConstant.LOADING);
        progressDialog.show();
        checkForConnection();
    }

    private void checkForConnection() {
        if (mInstance.dataStatus) {
            init();
        } else {
            mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    onBackPressed();
                }
            }, 2000);
        }
    }

    private void init() {
        pathId = getIntent().getStringExtra(AppConstant.PATH_ID);
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        routeRiskScore = findViewById(R.id.route_risk_score);
        distance = findViewById(R.id.distance);
        driverOfTripSwitch = findViewById(R.id.driver_of_trip_switch);
        mSuggestSafeRoute = findViewById(R.id.suggest_safe_route_button);

        mSuggestSafeRoute.setOnClickListener(this);
        driverOfTripSwitch.setOnCheckedChangeListener(this);
        startPoint = findViewById(R.id.start_point);
        endPoint = findViewById(R.id.end_point);
        startTime = findViewById(R.id.start_time);
        stopTime = findViewById(R.id.stop_time);
        duration = findViewById(R.id.duration);
        scrollView = findViewById(R.id.scrollView);
        mapFragment = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        extraPO = new PolylineOptions().color(getResources().getColor(R.color.light_gray_header_color)).width(getResources().getInteger(R.integer.polyline_width));
        originalPO = new PolylineOptions().color(getResources().getColor(R.color.blue)).width(getResources().getInteger(R.integer.polyline_width));
        autoZoomPoints = new ArrayList<LatLng>();
        fetchDetailedTrip();
    }

    private void fetchDetailedTrip() {
        int method = Request.Method.GET;
        String url = BuildConfig.REST_URL + AppConstant.TRIP_DETAIL_URL;
        if (mInstance.dataStatus) {
            makeJsonArryReq(url, method, AppConstant.DETAILED_TRIP_GET, AppConstant.DETAILED_TRIP_GET);
        } else {
            logger.info(getString(R.string.log_connecting_rest_api));
            progressDialog.dismiss();
            mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                isConnectionTimeout = false;
                volleyArray.remove(tag);
//                logger.info( getString(R.string.received_response));
                response(tag, response, 200, queryType);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isConnectionTimeout = true;
                volleyArray.remove(tag);
                logger.info(getString(R.string.received_error));
                error(tag, error, queryType);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                headers.put(AppConstant.REST_ACCESS_TOKEN, mPrefer.getString(AppConstant.TOKEN, ""));
                if (queryType.equals(AppConstant.DETAILED_TRIP_GET)) {
                    headers.put(AppConstant.REST_ID, mInstance.myLibrary.dataEncrypt(pathId));
                }
                return headers;
            }

            @Override
            public byte[] getBody() {
                if (queryType.equals(AppConstant.DETAILED_TRIP_GET)) {
                    return null;
                } else if (queryType.equals(AppConstant.PASENGER_STATUS_POST)) {
                    return getBodyForPost(queryType);
                } else {
                    return null;
                }
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

    // configuring body for volley request
    private byte[] getBodyForPost(String queryType) {
        JSONObject finalToken = new JSONObject();
        JSONObject tempjson = new JSONObject();
        // body for submitting registration data
        if (queryType.equals(AppConstant.PASENGER_STATUS_POST)) {
            try {
                tempjson.put(AppConstant.PATH_ID, pathId);
                tempjson.put(AppConstant.PASSENGER_STATUS, passengerStatus);
            } catch (Exception e) {
                lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
            }
        }
        String query = mInstance.myLibrary.dataEncrypt(tempjson.toString());
        try {
            finalToken.put("json", query);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
        return finalToken.toString().getBytes();
    }

    //handles diferent response codes from Volley
    private void response(String check, JSONArray response, int code, String queryType) {
        mInstance.myLibrary.checkResponse(response, code, this);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            progressDialog.dismiss();
            mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            // handling response for submitting insurance data
            if (queryType.equals(AppConstant.DETAILED_TRIP_GET)) {
                JSONObject result = response.optJSONObject(0);
                /*for (int i = 1; i < response.length() - 1; i++) {
                    result.put(response.optJSONObject(i));
                }*/
                int status = response.optInt(response.length() - 1);
                if (status == 200) {
                } else if (status == 501) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.updation_failed));
                } else {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status);
                }
                if (check.equals(AppConstant.DETAILED_TRIP_GET)) {
                    displayServerMsg(result, status);
                }
                progressDialog.dismiss();
            } else if (queryType.equals(AppConstant.PASENGER_STATUS_POST)) {
                logger.info("Recieved Passenger status");
                JSONObject obj = response.optJSONObject(0);
                double distance = obj.optDouble(AppConstant.TOTAL_DISTANCE);
                double riskScore = obj.optDouble(AppConstant.RISK_SCORE);
                pagerValues[0] = (float) mInstance.myLibrary.round(riskScore, 2);
                pagerValues[1] = (float) mInstance.myLibrary.round(distance, 2);
                edit = mPrefer.edit();
                edit.putFloat(AppConstant.PAGER_VALUE0, pagerValues[0]);
                edit.putFloat(AppConstant.PAGER_VALUE1, pagerValues[1]);
                edit.commit();
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
                if (queryType.equals(AppConstant.DETAILED_TRIP_GET)) {
                    progressDialog.dismiss();
                    mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
                    handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            onBackPressed();
                        }
                    }, 2000);
                } else {
                    progressDialog.dismiss();
                    passengerStatus = !passengerStatus;
                    driverOfTripSwitch.setChecked(!passengerStatus);
                    mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    // display server message
    private void displayServerMsg(JSONObject result, int code) {
        progressDialog.dismiss();
        switch (code) {
            case AppConstant.RESPONSE_SUCCESS:
            case AppConstant.RESPONSE_GOOGLE_ANONYMUS:
                showDetails(result);
                break;
            case AppConstant.RESPONSE_USER_FOR_TRIP_NOT_AVAILABLE:
                progressDialog.dismiss();
                mInstance.myLibrary.DisplayToast(this, AppConstant.USER_NOT_AVAILABLE, Toast.LENGTH_SHORT, Gravity.CENTER);
                break;
            case AppConstant.RESPONSE_UPDATION_FAILED:
                progressDialog.dismiss();
                mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
            default:
                logger.info("default executed for displayServerMsg for response code : " + code);
                break;
        }
    }

    private void showDetails(JSONObject result) {
//        JSONObject sendData = new JSONObject();
        try {
            riskValue = result.optDouble(AppConstant.ROUTE_RISK_FACTOR);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
        }
//        riskValue = result.optString(AppConstant.ROUTE_RISK_FACTOR);
        startAddress = result.optString(AppConstant.START_LOCATION);
        endAddress = result.optString(AppConstant.END_LOCATION);
        startTimeValue = Long.parseLong(result.optString(AppConstant.START_TIME));
        startTimeValue = result.optLong(AppConstant.START_TIME);
        distanceValue = result.optDouble(AppConstant.TRIP_LENGTH);

        String time = mInstance.myLibrary.fetchTime(startTimeValue * 1000);
        double tripLength = distanceValue * 0.000621371;
        tripLength = mInstance.myLibrary.round(tripLength, 2);
        tripLengthInMiles = tripLength + "mi";

        routeRiskScore.setText(riskValue + "");
        distance.setText(tripLengthInMiles);
        passengerStatus = result.optBoolean(AppConstant.PASSENGER_RIDE);
        driverOfTripSwitch.setChecked(!passengerStatus);
        startPoint.setText(startAddress);
        endPoint.setText(endAddress);
        startTime.setText(time);
        stopTime.setText(mInstance.myLibrary.fetchTime(result.optLong(AppConstant.STOP_TIME) * 1000));
        duration.setText(mInstance.myLibrary.calculateTime(Integer.parseInt(result.optString(AppConstant.TRIP_TIME))));
//        JSONArray coordinates = result.optJSONArray("coordinates");
        JSONArray sCoordinates = result.optJSONArray("coordinates");
        for (int j = 0; j < sCoordinates.length(); j++) {
            JSONArray coordinates = sCoordinates.optJSONArray(j);
            if (coordinates != null && coordinates.length() > 0) {
                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray points = coordinates.optJSONArray(i);
                    if (points != null && points.length() > 0) {
                        double lon = points.optDouble(0);
                        double lat = points.optDouble(1);
                        LatLng point = new LatLng(lat, lon);
                        if (j == 0) {
                            extraPO.add(point);
                        } else {
                            originalPO.add(point);
                        }
                        autoZoomPoints.add(point);

                        if (j == 0 && i == 0) {
                            mStartLat = lat;
                            mStartLon = lon;
                            startLatLon = new LatLng(lat, lon);
                        } else if (j == sCoordinates.length() - 1 && i == coordinates.length() - 1) {
                            mStopLat = lat;
                            mStopLon = lon;
                            stopLatLon = new LatLng(lat, lon);
                        }
                    }
                }
            }
        }
        isDataReady = true;
        setMapData();
        mainLayout.setVisibility(View.VISIBLE);
    }

    public void safeRoute() {
        String from = startLatLon.latitude + "," + startLatLon.longitude;
        String to = stopLatLon.latitude + "," + stopLatLon.longitude;
        Intent intent = new Intent(this, SafeRouteSuggestion.class);
        intent.putExtra(AppConstant.INTENT_START, from);
        intent.putExtra(AppConstant.INTENT_END, to);
        intent.putExtra(AppConstant.INTENT_START_ADDRESS, startAddress);
        intent.putExtra(AppConstant.INTENT_END_ADDRESS, endAddress);
        intent.putExtra(AppConstant.INTENT_START_TIME, startTimeValue);
        intent.putExtra(AppConstant.INTENT_RISK_SCORE, riskValue);
        intent.putExtra(AppConstant.INTENT_DISTANCE, distanceValue);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        logger.info("Returned to the My Trip List.");
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.suggest_safe_route_button:
                safeRoute();
                break;
            case R.id.back_layout:
                onBackPressed();
                break;
            default:
                logger.info(getString(R.string.onclick_default) + v.getId());
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);
        CameraUpdate cu = CameraUpdateFactory.newLatLng(new LatLng(39.828138, -98.579387));
        mMap.moveCamera(cu);
        mMap.setOnMapLoadedCallback(this);
    }

    private void setMapData() {
        if (isDataReady && mMap != null) {
            mapFragment.setListener(this);
            isDataReady = false;
            mMap.addMarker(new MarkerOptions().position(startLatLon).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_marker)));
            mMap.addMarker(new MarkerOptions().position(stopLatLon).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end_marker)));
            mMap.addPolyline(extraPO);
            mMap.addPolyline(originalPO);
            if (autoZoomPoints != null && !autoZoomPoints.isEmpty())
                autoZoom();
        }
    }

    private void upadteDriverStatus(boolean isChecked) {
        int method = Request.Method.POST;
        String url = BuildConfig.REST_URL + AppConstant.PASSENGER_STATUS_URL;
        if (mInstance.dataStatus) {
            makeJsonArryReq(url, method, AppConstant.PASENGER_STATUS_POST, AppConstant.PASENGER_STATUS_POST);
        } else {
            logger.info(getString(R.string.log_connecting_rest_api));
            progressDialog.dismiss();
            passengerStatus = !passengerStatus;
            driverOfTripSwitch.setChecked(!passengerStatus);
            mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    public void autoZoom() {
        try {
            boolean ok = false;
            if (autoZoomPoints != null && !autoZoomPoints.isEmpty() && !autoZoom) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng pos : autoZoomPoints) {
                    if (pos != null)
                        builder.include(pos);
                }
                LatLngBounds bounds = builder.build();
                mZoomLevel = mMap.getCameraPosition().zoom;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                mMap.moveCamera(cu);
                autoZoom = true;
                ok = true;
            } else {
                mZoomLevel = (float) 9;
                ok = false;
            }
            if (ok) {
                mZoomLevel = mMap.getCameraPosition().zoom - 0.25f;
                mMap.animateCamera(CameraUpdateFactory.zoomTo(mZoomLevel));
                edit = mPrefer.edit();
                edit.putFloat(AppConstant.KEY_ZOOM_LEVEL, mZoomLevel);
                edit.commit();
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Reseting Auto Zoom " + lineNumber, e);
            autoZoom();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.driver_of_trip_switch:
                passengerStatus = !isChecked;
                if (!isConnectionTimeout) {
                    upadteDriverStatus(isChecked);
                }
                isConnectionTimeout = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mInstance.setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler1 != null)
            handler1.removeCallbacksAndMessages(null);
        if (handler2 != null)
            handler2.removeCallbacksAndMessages(null);
//        mInstance.setConnectivityListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!volleyArray.isEmpty()) {
            for (int i = 0; i < volleyArray.size(); i++) {
                mInstance.getRequestQueue().cancelAll(volleyArray.get(i));
            }
        }
    }

//    @Override
//    public void onNetworkConnectionChanged(boolean isConnected) {
//        if (driverOfTripSwitch != null) {
//            if (mInstance.status) {
//                driverOfTripSwitch.setEnabled(true);
//            } else {
//                mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
//                driverOfTripSwitch.setEnabled(false);
//            }
//        }
//    }

    @Override
    public void onMapLoaded() {
        setMapData();
    }

    @Override
    public void onTouch() {
        scrollView.requestDisallowInterceptTouchEvent(true);
    }
}
