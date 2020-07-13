package com.tnedicca.routewise.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomTextView;

import java.util.ArrayList;

/**
 * Created by Aachu on 27-02-2017.
 */
public class FullMapView extends Activity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraChangeListener {

    private SharedPreferences mPrefer;
    private RouteWise mInstance;
    public CustomTextView screenTitle;
    private ImageView menuIcon;
    public CustomButton direction;
    private Bundle intent;
    private String from;
    private String to;
    private String routeName;
    private String distance;
    private String duration;
    private CustomTextView durationText;
    private CustomTextView distanceText;
    private String path;
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private boolean maploaded = false;
    private String mNavigateRoute;
    private int lineNumber;
    private LatLngBounds.Builder builder;
    private boolean zoom = false;
    private RouteLog logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direction);
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(FullMapView.class);

        screenTitle = findViewById(R.id.action_bar_title);
        durationText = findViewById(R.id.duration);
        distanceText = findViewById(R.id.distance);
        menuIcon = findViewById(R.id.back_icon);
        direction = findViewById(R.id.direction);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        intent = getIntent().getExtras();
        if (intent != null) {
            from = intent.getString(AppConstant.INTENT_FROM_ADDRESS);
            to = intent.getString(AppConstant.INTENT_TO_ADDRESS);
            routeName = intent.getString(AppConstant.INTENT_ROUTE_NAME);
            distance = intent.getString(AppConstant.INTENT_DISTANCE);
            duration = intent.getString(AppConstant.INTENT_DURATION);
            path = intent.getString(AppConstant.INTENT_DIRECTIONS);
            mNavigateRoute = intent.getString(AppConstant.INTENT_NAVIGATION);
        }

        screenTitle.setCustomText(routeName);
        durationText.setCustomText(duration);
        distanceText.setCustomText("(" + distance + ")");
        direction.setOnClickListener(this);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mInstance.myLibrary.check6Compact(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, AppConstant.RESPONSE_6_LOCATION, R.string.permission_loc)) {
            if (mMap == null)
                setUpMapIfNeeded();
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null)
            mapFragment.getMapAsync(this);
        else
            setUpMap();
    }

    @SuppressLint("MissingPermission")
    private void setUpMap() {
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapLoadedCallback(this);
    }

    public void placeMarker() {
        ArrayList<LatLng> points = mInstance.myLibrary.decodePoly(path);
        if (points != null && points.size() < 5) {
            mInstance.myLibrary.DisplayToast(this, "Route not available", Toast.LENGTH_SHORT, Gravity.TOP);
        } else {
            int color = Color.parseColor("#4e8c4a");
            mInstance.myLibrary.drawRoute(mMap, points, getResources().getInteger(R.integer.polyline_width), color);
        }

        int size = points.size();
        builder = new LatLngBounds.Builder();
        for (int i = 0; i < size; i++) {
            LatLng latLng = points.get(i);
            if (i == 0)
                mInstance.myLibrary.drawMarker(mMap, latLng, R.drawable.ic_start_marker, "Start", from);
            else if (i == size - 1)
                mInstance.myLibrary.drawMarker(mMap, latLng, R.drawable.ic_end_marker, "End", to);
            builder.include(latLng);
        }
        zoom();
    }

    private void zoom() {
        if (maploaded) {
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
            mMap.animateCamera(cu);
            zoom = true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_top, R.anim.anim_slide_out_top);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_layout:
                onBackPressed();
                break;
            case R.id.direction:
                showNavigation();
                break;
            default:
                logger.info(getString(R.string.onclick_default) + id);
                break;
        }
    }

    private void showNavigation() {
        Intent intent = new Intent(this, NavigationText.class);
        intent.putExtra(AppConstant.INTENT_FROM_ADDRESS, from);
        intent.putExtra(AppConstant.INTENT_TO_ADDRESS, to);
        intent.putExtra(AppConstant.INTENT_DURATION, duration);
        intent.putExtra(AppConstant.INTENT_DISTANCE, distance);
        intent.putExtra(AppConstant.INTENT_ROUTE_NAME, routeName);
        intent.putExtra(AppConstant.INTENT_NAVIGATION, mNavigateRoute);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        CameraUpdate Update = CameraUpdateFactory.newLatLngZoom(new LatLng(AppConstant.CENTER_LAT, AppConstant.CENTER_LON), 6);
        mMap.moveCamera(Update);
        setUpMap();
        placeMarker();
    }

    @Override
    public void onMapLoaded() {
        maploaded = true;
        zoom();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (zoom) {
            final float zoom = cameraPosition.zoom;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom - 0.25f));
            this.zoom = false;
        }
    }
}
