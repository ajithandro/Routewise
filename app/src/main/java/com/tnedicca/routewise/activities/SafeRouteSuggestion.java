package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.LatLng;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.adapters.SafeRouteAdapter;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.classes.SafeTrip;
import com.tnedicca.routewise.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aachu on 21-02-2017.
 */
public class SafeRouteSuggestion extends Activity implements View.OnClickListener, SafeRouteAdapter.MyClickListener {

    ArrayList<String> volleyArray = new ArrayList<String>();
    int noOfTrips = -1;
    double safe = Double.MAX_VALUE;
    int safeDistance = Integer.MAX_VALUE;
    int safeId = 0;
    ArrayList<String> navigationArray = new ArrayList<>();
    ArrayList<JSONObject> distanceArray = new ArrayList<>();
    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;
    private RouteWise mInstance;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SafeRouteAdapter mAdapter;
    private ArrayList<SafeTrip> detailsArray = new ArrayList<SafeTrip>();
    private String start;
    private String end;
    private String startAddress;
    private String endAddress;
    private int startTime;
    private double riskScore;
    private double distance;
    private CustomTextView startText;
    private CustomTextView endText;
    private int lineNumber;
    private int routeCount;
    private int countingStart = 0;
    private JSONObject currentRoute;
    private JSONArray completRoute;
    private RelativeLayout addressLayout;
    private View view;
    private ProgressDialog progressDialog;
    private CustomTextView safeText;
    private ImageView thumbIcon;
    private CustomTextView text;
    private RouteLog logger;
    private SharedPreferences mPrefer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.safe_route);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(SafeRouteSuggestion.class);

        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.title_safe));
//        backIcon.setOnClickListener(this);
        infoIcon.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            start = extras.getString(AppConstant.INTENT_START);
            end = extras.getString(AppConstant.INTENT_END);
            startAddress = extras.getString(AppConstant.INTENT_START_ADDRESS);
            endAddress = extras.getString(AppConstant.INTENT_END_ADDRESS);
            startTime = extras.getInt(AppConstant.INTENT_START_TIME);
            riskScore = extras.getDouble(AppConstant.INTENT_RISK_SCORE);
            distance = extras.getDouble(AppConstant.INTENT_DISTANCE);
        }

        safeText = findViewById(R.id.safe_text);
        thumbIcon = findViewById(R.id.thumbs_image);

        addressLayout = findViewById(R.id.address_layout);
        startText = findViewById(R.id.start_point);
        endText = findViewById(R.id.end_point);
        view = findViewById(R.id.view1);
        text = findViewById(R.id.suggest_text);
        mRecyclerView = findViewById(R.id.trip_list);

        startText.setCustomText(startAddress);
        endText.setCustomText(endAddress);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(AppConstant.LOADING);
        progressDialog.show();

        int method = Request.Method.GET;
        String url = BuildConfig.REST_URL + AppConstant.GOOGLE_DIRECTION_URL;
        makeJsonArryReq(url, method, AppConstant.GOOGLE_DIRECTION_GET, null, AppConstant.GOOGLE_DIRECTION_GET);
    }

    private void cancel() {
        progressDialog.dismiss();
        onBackPressed();
    }

    public void calcRISKAPI(JSONArray response) {
        JSONObject result = response.optJSONObject(0);
        completRoute = result.optJSONArray(AppConstant.RISK_KEY_ROUTES);

        routeCount = completRoute.length();
        currentRoute = completRoute.optJSONObject(countingStart++);
        sendRequest();
    }

    public void sendRequest() {
        if (currentRoute != null) {
            JSONObject line = currentRoute.optJSONObject(AppConstant.GOOGLE_OVERVIEW_POLYLINE);
            String point = line.optString(AppConstant.GOOGLE_POINTS);
            ArrayList<LatLng> points = mInstance.myLibrary.decodePoly(point);
            JSONObject sendData = new JSONObject();
            try {
                JSONArray waypointArray = new JSONArray();
                double dis = 0;
                double distance = 0;
                double oldLat = 0;
                double oldLon = 0;
                for (int j = 0; j < points.size(); j++) {
                    LatLng tempPoint = points.get(j);

                    JSONObject waypoint = new JSONObject();
                    waypoint.put(AppConstant.LATITUDE, tempPoint.latitude);
                    waypoint.put(AppConstant.LONGITUDE, tempPoint.longitude);
                    waypoint.put(AppConstant.TIME, startTime);
                    if (j > 2 && j < points.size() - 1) {
//                        if(oldLat !=0 && oldLon !=0) {
                        dis = dis + mInstance.myLibrary.distance(tempPoint.latitude, tempPoint.longitude, oldLat, oldLon, 'k');
//                        LatLng old = new LatLng(oldLat, oldLon);
                        Location old = new Location("A");
                        old.setLatitude(oldLat);
                        old.setLongitude(oldLon);
                        Location now = new Location("B");
                        now.setLatitude(tempPoint.latitude);
                        now.setLongitude(tempPoint.longitude);

                        distance = distance + old.distanceTo(now);
                    }
                    oldLat = tempPoint.latitude;
                    oldLon = tempPoint.longitude;
                    waypointArray.put(waypoint);
                }
                sendData.put(AppConstant.WAYPOINTS, waypointArray);
            } catch (JSONException e) {
                lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
            }

            String url = BuildConfig.REST_URL + AppConstant.TEMPORAL_URL;
            int method = Request.Method.POST;
            makeJsonArryReq(url, method, AppConstant.RISK_API, sendData, AppConstant.RISK_API);
        } else {
            mInstance.myLibrary.DisplayToast(this, AppConstant.ROUTE_NOT_AVAILABLE, Toast.LENGTH_SHORT, Gravity.CENTER);
            cancel();
        }
    }

    public void calcStats(JSONObject route, JSONObject restRoute) {
        String routeName = route.optString(AppConstant.GOOGLE_SUMMARY);
        String riskFactor = restRoute.optString(AppConstant.RISK_KEY_ROUTE_RISK);
        JSONArray tempLegs = route.optJSONArray(AppConstant.GOOGLE_LEGS);
        JSONObject legs = tempLegs.optJSONObject(0);
        JSONObject completeDuration = legs.optJSONObject(AppConstant.GOOGLE_DURATION);
        JSONObject completeDistance = legs.optJSONObject(AppConstant.GOOGLE_DISTANCE);
        int duration = completeDuration.optInt(AppConstant.GOOGLE_VALUE);
        int distance = completeDistance.optInt(AppConstant.GOOGLE_VALUE);

        double tempRisk = 0;
        try {
            tempRisk = Double.parseDouble(riskFactor);
        } catch (Exception e) {
//            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
//            logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
        }

        boolean processRoute = false;
        double roundedTemp = mInstance.myLibrary.round(tempRisk, 2);
        double roundedRisk = mInstance.myLibrary.round(riskScore, 2);
        if (roundedTemp <= roundedRisk) {
            processRoute = roundedTemp != roundedRisk || !(distance > this.distance);
        }

        if (processRoute) {
            int size = detailsArray.size();
            if (noOfTrips == -1)
                noOfTrips++;
            boolean assign = false;
            if (tempRisk < safe) {
                assign = true;
            } else if (tempRisk == safe) {
                if (distance < safeDistance)
                    assign = true;
            }
            if (assign) {
                safe = tempRisk;
                safeDistance = distance;
                safeId = size;
            }

            double temp1 = distance * 0.000621371;
            double roundedValue = mInstance.myLibrary.round(temp1, 2);
            String finalDis = roundedValue + " miles";

            int[] response = mInstance.myLibrary.secondsToHoursMinutesSeconds(duration);
            String finalDur = "--";
            if (response[0] == 0) {
                if (response[1] == 0)
                    finalDur = response[2] + " seconds";
                else
                    finalDur = response[1] + " minute(s)";
            } else {
                finalDur = response[0] + " hour(s)";
            }
            String url = getStaticURL(route);

            JSONArray steps = legs.optJSONArray(AppConstant.GOOGLE_STEPS);
            for (int i = 0; i < steps.length(); i++) {
                JSONObject temp = steps.optJSONObject(i);
                JSONObject dis = temp.optJSONObject(AppConstant.GOOGLE_DISTANCE);

                String ins = temp.optString(AppConstant.GOOGLE_INSTRUCTIONS);
                String instruction = Html.fromHtml(ins).toString();

                navigationArray.add(instruction);
                distanceArray.add(dis);
            }

            SafeTrip trip = new SafeTrip();
            trip.setNavigationArray(navigationArray);
            trip.setDistanceArray(distanceArray);
            trip.setUrl(this, url);
            trip.setRouteName(routeName);
            trip.setRiskFactor(riskFactor);
            trip.setSafe(false);
            trip.setFinalDis(finalDis);
            trip.setFinalDur(finalDur);
            trip.setDuration(duration);
            trip.setCurrentRoute(currentRoute);
            detailsArray.add(trip);

            noOfTrips++;
        }
    }

    public void doFinalStep(JSONArray response) {
        progressDialog.dismiss();
        if (noOfTrips == -1) {
            mRecyclerView.setVisibility(View.GONE);
            safeText.setVisibility(View.VISIBLE);
            thumbIcon.setVisibility(View.VISIBLE);
        } else {
            SafeTrip temp = detailsArray.get(safeId);
            temp.setSafe(true);
            detailsArray.remove(safeId);
            detailsArray.add(temp);

            addressLayout.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new SafeRouteAdapter(detailsArray);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mAdapter.setMyClickListener(this);
        }
    }

    public String getStaticURL(JSONObject route) {
        JSONObject line = route.optJSONObject(AppConstant.GOOGLE_OVERVIEW_POLYLINE);
        String point = line.optString(AppConstant.GOOGLE_POINTS);
        ArrayList<LatLng> points = mInstance.myLibrary.decodePoly(point);
        int count = points.size();
        int divisor = 1;
        if (count > 50)
            divisor = 10;

        String start = "";
        String end = "";
        String result = "";
        for (int j = 0; j < count; j++) {
            LatLng tempPoint = points.get(j);
            double lat = tempPoint.latitude;
            double lon = tempPoint.longitude;

            if (j == 0) {
                start = lat + "," + lon;
                result = lat + "," + lon + "|";
            } else if (j == count - 1) {
                end = lat + "," + lon;
                result = result + lat + "," + lon;
            } else {
                if (j % divisor == 0)
                    result = result + lat + "," + lon + "|";
            }
        }

        String url = AppConstant.STATIC_MAP_URL;
        url = url.replace("$start", start);
        url = url.replace("$end", end);
        url = url.replace("$path", result);
        return url;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final JSONObject sendingData, final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                volleyArray.remove(tag);
//                logger.info(getString(R.string.received_response));
                response(tag, response, 200);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyArray.remove(tag);
                logger.info(getString(R.string.received_error));
                error(tag, error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                headers.put(AppConstant.REST_ACCESS_TOKEN, mPrefer.getString(AppConstant.TOKEN, ""));
                if (queryType.equals(AppConstant.GOOGLE_DIRECTION_GET)) {
                    headers.put(AppConstant.REST_X_FROM, start);
                    headers.put(AppConstant.REST_X_TO, end);
                }
                return headers;
            }

            @Override
            public byte[] getBody() {
                return sendingData.toString().getBytes();
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

    //handles diferent response codes from Volley
    private void response(String check, JSONArray response, int code) {
        mInstance.myLibrary.checkResponse(response, code, this);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            cancel();
        } else {
            JSONObject result = response.optJSONObject(0);
            int state = response.optInt(1);
            if (state == AppConstant.RESPONSE_SUCCESS || state ==  AppConstant.RESPONSE_GOOGLE_ANONYMUS) {
                if (check.equals(AppConstant.GOOGLE_DIRECTION_GET)) {
                    calcRISKAPI(response);
                } else if (check.equals(AppConstant.RISK_API)) {
                    String status = result.optString(AppConstant.RISK_KEY_STATUS);
                    if (status.equals("success")) {
                        JSONArray routes = result.optJSONArray(AppConstant.RISK_KEY_ROUTES);
                        calcStats(currentRoute, routes.optJSONObject(0));
                    }
                    if (countingStart < routeCount) {
                        currentRoute = completRoute.optJSONObject(countingStart++);
                        sendRequest();
                    } else if (countingStart == routeCount)
                        doFinalStep(response);
                }
            } else {
                cancel();
            }
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error) {
        cancel();
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode);
            } else {
                mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!volleyArray.isEmpty()) {
            for (int i = 0; i < volleyArray.size(); i++) {
                RouteWise.getInstance().getRequestQueue().cancelAll(volleyArray.get(i));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                onBackPressed();
                break;
            default:
                logger.info(getString(R.string.onclick_default) + v.getId());
                break;
        }
    }

    @Override
    public void onItemClick(int position, View v) {
        SafeTrip safeTrip = detailsArray.get(position);
        ArrayList<JSONObject> arr = safeTrip.getDistanceArray();
        ArrayList<String> arrString = new ArrayList<String>();
        for (int i = 0; i < arr.size(); i++) {
            arrString.add(arr.get(i).toString());
        }
        Intent intent = new Intent(SafeRouteSuggestion.this, Directions.class);
        intent.putExtra(AppConstant.SAFE_ROUTE_NAME, safeTrip.getRouteName());
        intent.putStringArrayListExtra(AppConstant.SAFE_ROUTE_DISTANCE_ARRAY, arrString);
        intent.putExtra(AppConstant.SAFE_ROUTE_NAVIGATION_ARRAY, safeTrip.getNavigationArray());
        intent.putExtra(AppConstant.SAFE_ROUTE_URL, safeTrip.getUrl());
        intent.putExtra(AppConstant.SAFE_ROUTE_RISK_FACTOR, safeTrip.getRiskFactor());
        intent.putExtra(AppConstant.SAFE_ROUTE_IS_SAFE, safeTrip.isSafe());
        intent.putExtra(AppConstant.SAFE_ROUTE_FINAL_DISTANCE, safeTrip.getFinalDis());
        intent.putExtra(AppConstant.SAFE_ROUTE_FINAL_DURATION, safeTrip.getFinalDur());
        intent.putExtra(AppConstant.INTENT_START_ADDRESS, startAddress);
        intent.putExtra(AppConstant.INTENT_END_ADDRESS, endAddress);
        intent.putExtra(AppConstant.SAFE_ROUTE_DURATION_INTEGER, safeTrip.getDuration());
        JSONObject selectedRoute = safeTrip.getCurrentRoute();
        JSONObject line = selectedRoute.optJSONObject(AppConstant.GOOGLE_OVERVIEW_POLYLINE);
        String coordinates = line.optString(AppConstant.GOOGLE_POINTS);
        intent.putExtra(AppConstant.SAFE_ROUTE_PATH_COORDINATES, coordinates);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }
}
