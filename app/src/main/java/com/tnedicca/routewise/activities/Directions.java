package com.tnedicca.routewise.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

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
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.fragments.CustomMapFragment;
import com.tnedicca.routewise.views.CustomTextView;
import com.tnedicca.routewise.fragments.DirectionStatementLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by new on 23-02-2017.
 */

public class Directions extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMapLoadedCallback, CustomMapFragment.OnTouchListener {

    public boolean autoZoom = false;
    BroadcastReceiver minuteBroadcastReceiver;
    Calendar calender;
    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;
    private RouteWise mInstance;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private ArrayList<String> navigationArray;
    private ArrayList<JSONObject> distanceArray;
    private String riskFactor;
    private boolean isSafe;
    private String finalDur;
    private String finalDis;
    private String startAddress;
    private String endAddress;
    private CustomTextView mRouteRiskScore;
    private ImageView mSafeChoice;
    private CustomTextView mDistance;
    private CustomMapFragment mapFragment;
    private GoogleMap mMap;
    private CustomTextView mDuration;
    private CustomTextView mStartPoint;
    private CustomTextView mStartTime;
    private CustomTextView mEndPoint;
    private CustomTextView mStopTime;
    private String point;
    private PolylineOptions polylineOptions;
    private ArrayList<LatLng> autoZoomPoints;
    private LatLng startLatLon;
    private LatLng stopLatLon;
    private float mZoomLevel;
    private int lineNumber;
    private int durationInteger = 0;
    private ScrollView scrollView;
    private RouteLog logger;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggested_route_directions);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Directions.class);
        
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        calender = Calendar.getInstance();
        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.directions));
//        backIcon.setOnClickListener(this);
        infoIcon.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        navigationArray = intent.getStringArrayListExtra(AppConstant.SAFE_ROUTE_NAVIGATION_ARRAY);
        distanceArray = new ArrayList<>();
        ArrayList<String> distArray = intent.getExtras().getStringArrayList(AppConstant.SAFE_ROUTE_DISTANCE_ARRAY);
        for (int i = 0; i < distArray.size(); i++) {
            try {
                distanceArray.add(new JSONObject(distArray.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        riskFactor = intent.getStringExtra(AppConstant.SAFE_ROUTE_RISK_FACTOR);
        isSafe = intent.getBooleanExtra(AppConstant.SAFE_ROUTE_IS_SAFE, false);
        finalDis = intent.getStringExtra(AppConstant.SAFE_ROUTE_FINAL_DISTANCE);
        finalDur = intent.getStringExtra(AppConstant.SAFE_ROUTE_FINAL_DURATION);
        startAddress = intent.getStringExtra(AppConstant.INTENT_START_ADDRESS);
        endAddress = intent.getStringExtra(AppConstant.INTENT_END_ADDRESS);
        point = intent.getStringExtra(AppConstant.SAFE_ROUTE_PATH_COORDINATES);
        durationInteger = intent.getIntExtra(AppConstant.SAFE_ROUTE_DURATION_INTEGER, 0);
        init();
    }

    private void init() {
        mRouteRiskScore = findViewById(R.id.route_risk_score);
        mSafeChoice = findViewById(R.id.safe_choice);
        mDistance = findViewById(R.id.distance);
        mapFragment = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mDuration = findViewById(R.id.duration);
        mStartPoint = findViewById(R.id.start_point);
        mStartTime = findViewById(R.id.start_time);
        mEndPoint = findViewById(R.id.end_point);
        mStopTime = findViewById(R.id.stop_time);
        scrollView = findViewById(R.id.scrollView);
        mapFragment.getMapAsync(this);
        polylineOptions = new PolylineOptions().color(getResources().getColor(R.color.blue)).width(getResources().getInteger(R.integer.polyline_width));
        autoZoomPoints = new ArrayList<LatLng>();
        ArrayList<LatLng> points = mInstance.myLibrary.decodePoly(point);
        polylineOptions.addAll(points);
        autoZoomPoints.addAll(points);
        startLatLon = points.get(0);
        stopLatLon = points.get(points.size() - 1);
        mRouteRiskScore.setText(riskFactor);
        if (isSafe) {
            mSafeChoice.setImageResource(R.drawable.ic_tickclick);
        } else {
            mSafeChoice.setImageResource(R.drawable.ic_ticknoclick);
        }
        String dist = finalDis.split(" ")[0];
        dist = dist + " mi";
        mDistance.setText(dist);
        mDuration.setText("Duration - " + finalDur);
        mStartPoint.setText(startAddress);
        displayRouteDescription();
        mEndPoint.setText(endAddress);
    }

    @Override
    protected void onPause() {
        if(handler != null)
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStartTime.setText(mInstance.myLibrary.getTime());
        calender = Calendar.getInstance();
        long currentTime = calender.getTimeInMillis() + durationInteger * 1000;
        mStopTime.setText(mInstance.myLibrary.fetchTime(currentTime));
        handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStartTime.setText(mInstance.myLibrary.getTime());
                calender = Calendar.getInstance();
                long currentTime = calender.getTimeInMillis() + durationInteger * 1000;
                mStopTime.setText(mInstance.myLibrary.fetchTime(currentTime));
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void displayRouteDescription() {
        for (int i = 0; i < navigationArray.size(); i++) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DirectionStatementLayout statementLayout = new DirectionStatementLayout();
            fragmentTransaction.add(R.id.route_description, statementLayout);
            fragmentTransaction.commit();
            statementLayout.setStatement((navigationArray.get(i)).replace("\n\n", ". "));
            statementLayout.setDistance(mInstance.myLibrary.fetchDistance(distanceArray.get(i).optDouble("value")));
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
        if (mMap != null) {
            mapFragment.setListener(this);
            mMap.addMarker(new MarkerOptions().position(startLatLon).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_marker)));
            mMap.addMarker(new MarkerOptions().position(stopLatLon).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end_marker)));
            mMap.addPolyline(polylineOptions);
            if (autoZoomPoints != null && !autoZoomPoints.isEmpty())
                autoZoom();
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_layout:
                onBackPressed();
                break;
            default:
                logger.info(getString(R.string.onclick_default) + id);
                break;
        }
    }

    @Override
    public void onMapLoaded() {
        setMapData();
    }

    @Override
    public void onTouch() {
        scrollView.requestDisallowInterceptTouchEvent(true);
    }
}
