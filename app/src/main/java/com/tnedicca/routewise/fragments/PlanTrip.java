package com.tnedicca.routewise.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.FullMapView;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.adapters.PlaceAutocompleteAdapter;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.listeners.OnSwipeTouchListener;
import com.tnedicca.routewise.views.CustomEditTextView;
import com.tnedicca.routewise.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aachu on 15-02-2017.
 */
@SuppressWarnings({"ResourceType"})
//public class PlanTrip extends Fragment {
public class PlanTrip extends Fragment implements AdapterView.OnItemClickListener, PlaceAutocompleteAdapter.AdapterCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraChangeListener, OnMapReadyCallback, TextWatcher, View.OnClickListener, View.OnFocusChangeListener {

    private ViewGroup rootView;
    private CustomEditTextView mFromText;
    private CustomEditTextView mToText;
    private FragmentActivity act;
    private CustomTextView mSearchText;
    private ListView mList;
    private SharedPreferences mPrefer;
    private RouteWise mInstance;
    private ArrayList recentSearchList;
    private ArrayList recentSearchListId;
    private ArrayAdapter recentSearchAdapter;
    private CustomEditTextView mSelectedView;
    private SharedPreferences.Editor edit;
    private boolean mSelected = false;
    private boolean mCached = false;
    private Location currentLocation;
    private PlaceAutocompleteAdapter streetAdapter;
    private ProgressBar mProgress;
    ArrayList<String> volleyArray = new ArrayList<String>();
    private int paddingLeft;
    private int paddingRight;
    private int paddingTop;
    private int paddingBottom;

    ArrayList<JSONObject> navigationRoute = new ArrayList<JSONObject>();
    HashMap<Integer, ArrayList<LatLng>> polylineTempArray = new HashMap<>();
    ArrayList<ArrayList<LatLng>> polylineArray = new ArrayList<ArrayList<LatLng>>();
    HashMap<String, String> locationDetails = new HashMap<>();

    private LatLng toLoc;
    private LatLng fromLoc;
    private boolean fromCurrentLoc = false;
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private RelativeLayout mBottomLayout;
    private CustomTextView mDistanceText;
    private CustomTextView mRiskValueText;
    private CustomTextView mDurationText;
    private CustomTextView mSafeValueText;
    private View mMapView;
    private int lineNumber;
    String from = null;
    String to = null;
    JSONArray riskResponse;
    JSONArray googleResponse;
    private int selectedRoute;
    private UiSettings mUiSettings;
    private boolean maploaded = false;
    JSONArray tempRoute;
    private boolean mSearch = false;
    private InputMethodManager mKeyboard;
    private boolean direction = false;
    private String selectedDuration;
    private String selectedDistance;
    private String selectedSafeRoute;
    private String selectedRisk;
    private LatLngBounds.Builder builder;
    private boolean zoom = false;
    private RouteLog logger;
    private FusedLocationProviderClient mFusedLocationApi;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private SettingsClient mSettingsClient;
    private LocationCallback mLocationCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.plan_trip, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        act = getActivity();
        mPrefer = act.getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);
        mKeyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(PlanTrip.class);

        recentSearchList = new ArrayList();
        recentSearchListId = new ArrayList();
        for (int i = 0; i < 5; i++) {
            String data = mPrefer.getString(AppConstant.PLACE_ADDRESS + i, "");
            if (data != null && !data.isEmpty())
                recentSearchList.add(data);
            String data_id = mPrefer.getString(AppConstant.PLACE_ADDRESS_ID + i, "");
            if (data_id != null && !data_id.isEmpty())
                recentSearchListId.add(data_id);

        }

        mFromText = rootView.findViewById(R.id.from);
        mToText = rootView.findViewById(R.id.to);
        mSearchText = rootView.findViewById(R.id.search_textview);
        mList = rootView.findViewById(R.id.places_list);
        mProgress = rootView.findViewById(R.id.progress);

        mapFragment = (MapFragment) act.getFragmentManager().findFragmentById(R.id.map);
        mMapView = rootView.findViewById(R.id.map);
        mBottomLayout = rootView.findViewById(R.id.bottom_layout);
        mDistanceText = rootView.findViewById(R.id.distance);
        mDurationText = rootView.findViewById(R.id.duration);
        mRiskValueText = rootView.findViewById(R.id.risk_factor_value);
        mSafeValueText = rootView.findViewById(R.id.safe_route_value);

        showMap(false);
        if (mInstance.myLibrary.check6Compact(act, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, AppConstant.RESPONSE_6_LOCATION, R.string.permission_loc)) {
            if (mMap == null)
                setUpMapIfNeeded();
        }

        streetAdapter = new PlaceAutocompleteAdapter(act, mProgress, mInstance.myLibrary.getBounds(), mList, this);
        if (recentSearchList.size() != 0) {
            setCachedAddress();
        } else
            mList.setVisibility(View.GONE);

        mList.setTextFilterEnabled(true);
        mList.setOnItemClickListener(this);
        mFromText.setOnFocusChangeListener(this);
        mToText.setOnFocusChangeListener(this);
        mFromText.setOnClickListener(this);
        mToText.setOnClickListener(this);
        mFromText.addTextChangedListener(this);
        mToText.addTextChangedListener(this);

        initLoc();
    }

    @SuppressLint("MissingPermission")
    private void initLoc() {
        mFusedLocationApi = LocationServices.getFusedLocationProviderClient(act);
        mLocationManager = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
        mSettingsClient = LocationServices.getSettingsClient(act);

        mLocationRequest = new LocationRequest().create();
        mLocationRequest.setInterval(AppConstant.GPS_UPDATE_TIME);
        mLocationRequest.setFastestInterval(AppConstant.GPS_FAST_UPDATE_TIME);
        mLocationRequest.setSmallestDisplacement(AppConstant.GPS_UPDATE_DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;
                }
            }
        };
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationApi.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentLocation = task.getResult();
            }
        }).addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationApi.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        mToText.requestFocus();
        getLastLocation();
        startLocationUpdates();
    }

    @Override
    public void onStop() {
        mFusedLocationApi.removeLocationUpdates(mLocationCallback);
        super.onStop();
    }

    @Override
    public void onPause() {
//        streetAdapter.resetUpdating(true);
        super.onPause();
        if (!direction) {
            android.app.Fragment fragment = act.getFragmentManager().findFragmentById(R.id.map);
            android.app.FragmentTransaction ft = act.getFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }
        RelativeLayout parent = (RelativeLayout) mToText.getParent().getParent().getParent();
        parent.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    @Override
    public void onResume() {
        super.onResume();
        RelativeLayout parent = (RelativeLayout) mToText.getParent().getParent().getParent();
        paddingLeft = parent.getPaddingLeft();
        paddingRight = parent.getPaddingRight();
        paddingTop = parent.getPaddingTop();
        paddingBottom = parent.getPaddingBottom();
        parent.setPadding(0, parent.getPaddingTop(), 0, parent.getPaddingBottom());
        if (direction)
            processGoogleResponse();
        direction = false;
    }

    private void setCachedAddress() {
        mCached = true;
        mSearchText.setText(R.string.searches);
        recentSearchAdapter = new ArrayAdapter(act, R.layout.list_item);
        recentSearchAdapter.addAll(recentSearchList);
        recentSearchAdapter.setNotifyOnChange(true);
        recentSearchAdapter.notifyDataSetChanged();
        mList.setAdapter(recentSearchAdapter);
        mList.setVisibility(View.VISIBLE);
//        streetAdapter.resetUpdating(true);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null)
            mapFragment.getMapAsync(this);
        else
            setUpMap();
    }

    private void setUpMap() {
        mUiSettings = mMap.getUiSettings();
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(this);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setScrollGesturesEnabled(false);
        mUiSettings.setZoomGesturesEnabled(false);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapLoadedCallback(this);
    }

    private void clearSearch() {
        navigationRoute.clear();
        polylineTempArray.clear();
        polylineArray.clear();
        showMap(false);
        if (mMap != null)
            mMap.clear();
        setCachedAddress();
    }

    private void search() {
        mKeyboard.hideSoftInputFromWindow(mSelectedView.getWindowToken(), 0);
        mSearch = true;
        clearSearch();
        mProgress.setVisibility(View.VISIBLE);
        mSearchText.setVisibility(View.GONE);
        mList.setVisibility(View.GONE);

        if (fromLoc != null)
            from = fromLoc.latitude + "," + fromLoc.longitude;
        if (toLoc != null)
            to = toLoc.latitude + "," + toLoc.longitude;

        String url = BuildConfig.REST_URL + AppConstant.RISK_SCORE_URL;
        int method = Request.Method.GET;
        makeJsonArryReq(url, method, AppConstant.RISK_API_GET, AppConstant.RISK_API_GET);
    }

    private void processRiskResponse() {
        int method = Request.Method.GET;
        String url = BuildConfig.REST_URL + AppConstant.GOOGLE_DIRECTION_URL;
        makeJsonArryReq(url, method, AppConstant.GOOGLE_DIRECTION_GET, AppConstant.GOOGLE_DIRECTION_GET);
    }

    private void processGoogleResponse() {
        showMap(true);
        calcSafeRoute();
        drawRoute(googleResponse);
        if (maploaded)
            placeMarker(googleResponse);
        else
            tempRoute = googleResponse;

        mSearch = false;
    }

    public void drawRoute(JSONArray route) {
        int count = route.length();
        float width = getResources().getInteger(R.integer.polyline_width);
        int color = Color.parseColor("#4e8c4a");
        drawPolyline(route.optJSONObject(selectedRoute), selectedRoute, width, color);
//        width = 7.0f;
        for (int i = 0; i < count; i++) {
            JSONObject temp = route.optJSONObject(i);
            if (i < count) {
                navigationRoute.add(temp);
                if (i == selectedRoute) {
                    continue;
                }

                color = Color.BLUE;
                if (i == 1) {
                    color = Color.RED;
                } else if (i == 2) {
                    color = Color.parseColor("#834ABD");
                }
                drawPolyline(temp, i, width, color);
                width--;
            }
        }
        int count1 = polylineTempArray.size();
        for (int i = 0; i < count1; i++) {
            polylineArray.add(polylineTempArray.get(i));
        }
    }

    public void drawPolyline(JSONObject route, int index, float width, int color) {
        if (route != null) {
            JSONObject line = route.optJSONObject(AppConstant.GOOGLE_OVERVIEW_POLYLINE);
            String point = line.optString(AppConstant.GOOGLE_POINTS);

            ArrayList<LatLng> points = mInstance.myLibrary.decodePoly(point);
            polylineTempArray.put(index, points);
            if (points != null && points.size() < 5) {
                mInstance.myLibrary.DisplayToast(act, "Route not available", Toast.LENGTH_SHORT, Gravity.TOP);
            } else {
                mInstance.myLibrary.drawRoute(mMap, points, width, color);
            }
        } else {
            mInstance.myLibrary.DisplayToast(act, "Route not available", Toast.LENGTH_SHORT, Gravity.TOP);
        }
    }

    public void calcSafeRoute() {
        int minScore = Integer.MAX_VALUE;
        for (int i = 0; i < riskResponse.length(); i++) {
            JSONObject routes = riskResponse.optJSONObject(i);
            String name = routes.optString(AppConstant.RISK_KEY_ROUTE_NAME);
            int score = routes.optInt(AppConstant.RISK_KEY_ROUTE_RAW_SCORE);
            String distance = routes.optString(AppConstant.RISK_KEY_ROUTE_DISTANCE);
            String duration = routes.optString(AppConstant.RISK_KEY_ROUTE_DURATION);
            String riskfactor = routes.optString(AppConstant.RISK_KEY_ROUTE_RISK);

            if (score < minScore) {
                minScore = score;
                selectedRoute = i;
            }

            locationDetails.put(AppConstant.ROUTE_NAME + i, name);
            locationDetails.put(AppConstant.ROUTE_RISK_FACTOR_RAW_SCORE + i, Integer.toString(score));
            locationDetails.put(AppConstant.ROUTE_DISTANCE + i, distance);
            locationDetails.put(AppConstant.ROUTE_DURATION + i, duration);
            locationDetails.put(AppConstant.ROUTE_RISK_FACTOR0 + i, riskfactor);
        }

        int count = riskResponse.length();
        for (int i = 0; i < count; i++) {
            String safe = "No";
            if (i == selectedRoute) {
                safe = "Yes";
            }
            locationDetails.put(AppConstant.ROUTE_SAFETY + i, safe);
        }
        showDetails();
    }

    public void showDetails() {
        String distance = locationDetails.get(AppConstant.ROUTE_DISTANCE + selectedRoute);
        if (distance.contains(" km")) {
            distance = distance.replace(" km", "");
            try {
                Double temp = Double.valueOf(distance);
                double dis = temp * 0.621371;
                dis = mInstance.myLibrary.round(dis, 2);
                selectedDistance = dis + " mi";
            } catch (Exception e) {
                lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                logger.error("Error at " + lineNumber, e);
                selectedDistance = "--";
            }
        } else
            selectedDistance = distance;

        selectedDuration = locationDetails.get(AppConstant.ROUTE_DURATION + selectedRoute);
        selectedSafeRoute = locationDetails.get(AppConstant.ROUTE_SAFETY + selectedRoute);
        selectedRisk = locationDetails.get(AppConstant.ROUTE_RISK_FACTOR0 + selectedRoute);

        mDurationText.setCustomText(selectedDuration);
        mDistanceText.setCustomText("(" + selectedDistance + ")");
        mSafeValueText.setCustomText(selectedSafeRoute);
        mRiskValueText.setCustomText(selectedRisk);
    }

    public void placeMarker(JSONArray route) {
        if (route != null && route.length() > 0) {
            JSONObject temp = route.optJSONObject(0);
            JSONArray legs = temp.optJSONArray("legs");
            temp = legs.optJSONObject(0);
            JSONObject start = temp.optJSONObject("start_location");
            JSONObject end = temp.optJSONObject("end_location");

            builder = new LatLngBounds.Builder();
            double lat = start.optDouble("lat");
            double lon = start.optDouble("lng");
            LatLng tempLatlng = new LatLng(lat, lon);
            builder.include(tempLatlng);
            mInstance.myLibrary.drawMarker(mMap, tempLatlng, R.drawable.ic_start_marker, "Start", from);

            lat = end.optDouble("lat");
            lon = end.optDouble("lng");
            tempLatlng = new LatLng(lat, lon);
            builder.include(tempLatlng);
            mInstance.myLibrary.drawMarker(mMap, tempLatlng, R.drawable.ic_end_marker, "End", to);
            zoom();
            tempRoute = null;
        } else {
            showMap(false);
        }
    }

    private void zoom() {
        if (maploaded && builder != null) {
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
            mMap.animateCamera(cu);
            zoom = true;
        }
    }

    private void showMap(boolean show) {
        if (show) {
            mSearchText.setVisibility(View.GONE);
            mList.setVisibility(View.GONE);
            mProgress.setVisibility(View.INVISIBLE);
            mMapView.setVisibility(View.VISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);
            mMap.setOnMapClickListener(this);
            mMap.setOnMarkerClickListener(this);

            mBottomLayout.setOnTouchListener(new OnSwipeTouchListener(act) {
                public void onSwipeTop() {
                    showDirection();
                }

                public void onClick() {
                    showDirection();
                }
            });
        } else {
            mSearchText.setVisibility(View.VISIBLE);
            mList.setVisibility(View.VISIBLE);
            mMapView.setVisibility(View.INVISIBLE);
            mProgress.setVisibility(View.GONE);
            mBottomLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void showDirection() {
        direction = true;
        String tempFrom = mFromText.getText().toString();
        String tempTo = mToText.getText().toString();
        if (tempFrom.contains("from "))
            tempFrom = tempFrom.replace("from ", "");
        if (tempTo.contains("to "))
            tempTo = tempTo.replace("to ", "");

        String routeName = locationDetails.get(AppConstant.ROUTE_NAME + selectedRoute);
        JSONObject route = googleResponse.optJSONObject(selectedRoute);
        JSONObject line = route.optJSONObject("overview_polyline");
        String point = line.optString("points");

        Intent intent = new Intent(act, FullMapView.class);
        if (fromCurrentLoc)
            intent.putExtra(AppConstant.INTENT_FROM_ADDRESS, "Your location");
        else
            intent.putExtra(AppConstant.INTENT_FROM_ADDRESS, tempFrom);
        intent.putExtra(AppConstant.INTENT_TO_ADDRESS, tempTo);
        intent.putExtra(AppConstant.INTENT_DURATION, selectedDuration);
        intent.putExtra(AppConstant.INTENT_DISTANCE, selectedDistance);
        intent.putExtra(AppConstant.INTENT_ROUTE_NAME, routeName);
        intent.putExtra(AppConstant.INTENT_DIRECTIONS, point);
        intent.putExtra(AppConstant.INTENT_NAVIGATION, route.toString());
        startActivity(intent);
        act.overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_slide_out_bottom);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.from:
                if (mSelectedView.getId() != R.id.from)
                    selectView(mFromText, true);
                break;
            case R.id.to:
                if (mSelectedView.getId() != R.id.to)
                    selectView(mToText, true);
                break;
            default:
                logger.info(getString(R.string.onclick_default) + id);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!mCached) {
            if (parent == mList) {
                final AutocompletePrediction item = streetAdapter.getItem(position);
                final String placeId = item.getPlaceId();
                streetAdapter.getPlaceByID(placeId);
            }
        } else {
            String address = (String) recentSearchAdapter.getItem(position);
            String ids = null;
            for (int i = 0; i < recentSearchList.size(); i++) {
                String temp = recentSearchList.get(i).toString();
                if (temp.equals(address)) {
                    ids = recentSearchListId.get(i).toString();
                    break;
                }
            }
            streetAdapter.getPlaceByID(ids);
        }
    }

    @Override
    public void getPlaceDetails(Place place) {
        mKeyboard.hideSoftInputFromWindow(mSelectedView.getWindowToken(), 0);
        int id = mSelectedView.getId();
        String address = place.getAddress();
        String addressId = place.getId();
        LatLng latLng = place.getLatLng();

        boolean isPresent = false;
        if (recentSearchList.size() == 0) {
            recentSearchList.add(address);
            recentSearchListId.add(addressId);
        } else {
            for (int i = 0; i < recentSearchListId.size(); i++) {
                if (addressId.equals(recentSearchListId.get(i).toString())) {
                    isPresent = true;
                    break;
                } else {
                    isPresent = false;
                }
            }
            if (!isPresent) {
                recentSearchList.add(address);
                recentSearchListId.add(addressId);
            }
            if (recentSearchList.size() > 5) {
                recentSearchList.remove(0);
                recentSearchListId.remove(0);
            }
        }

        edit = mPrefer.edit();
        for (int i = 0; i < recentSearchList.size(); i++) {
            edit.putString(AppConstant.PLACE_ADDRESS + i, recentSearchList.get(i).toString());
            edit.putString(AppConstant.PLACE_ADDRESS_ID + i, recentSearchListId.get(i).toString());
        }
        edit.commit();

        mSelected = true;
        mSelectedView.setCustomText(address);

        final int len = address.length();
        mSelectedView.post(new Runnable() {
            @Override
            public void run() {
                mSelectedView.setSelection(len);
            }
        });

        if (id == R.id.to) {
            toLoc = latLng;
        } else if (id == R.id.from) {
            fromLoc = latLng;
        }
        if (toLoc != null) {
            if (fromLoc == null) {
                if (currentLocation != null) {
                    latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    fromCurrentLoc = true;
                    fromLoc = latLng;
                    search();
                } else
                    mInstance.myLibrary.DisplayToast(act, "Current location Unavailable", Toast.LENGTH_SHORT, Gravity.CENTER);
            } else {
                fromCurrentLoc = false;
                search();
            }
        } else
            mInstance.myLibrary.DisplayToast(act, "Select destination", Toast.LENGTH_SHORT, Gravity.CENTER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!volleyArray.isEmpty()) {
            for (int i = 0; i < volleyArray.size(); i++) {
                mInstance.getRequestQueue().cancelAll(volleyArray.get(i));
            }
        }
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                volleyArray.remove(tag);
//                logger.info(getString(R.string.received_response));
                response(tag, response, 200, queryType);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
//                if (queryType.equals(AppConstant.GOOGLE_DIRECTION_GET)) {
                headers.put(AppConstant.REST_X_FROM, from);
                headers.put(AppConstant.REST_X_TO, to);
//                }
                return headers;
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
    private void response(String check, JSONArray response, int code, String queryType) {
        mInstance.myLibrary.checkResponse(response, code, act);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            mProgress.setVisibility(View.INVISIBLE);
            clearSearch();
            mInstance.myLibrary.DisplayToast(act, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            JSONObject result = response.optJSONObject(0);
            int state = response.optInt(1);
            if (state == AppConstant.RESPONSE_SUCCESS || state == AppConstant.RESPONSE_GOOGLE_ANONYMUS) {
                JSONArray routes = result.optJSONArray(AppConstant.RISK_KEY_ROUTES);
                if (check.equals(AppConstant.RISK_API_GET)) {
                    String status = result.optString(AppConstant.RISK_KEY_STATUS);
                    if (status.equals("success")) {
                        riskResponse = routes;
                        processRiskResponse();
                    } else {
                        mInstance.myLibrary.DisplayToast(act, "Data not available for the selected locations", Toast.LENGTH_SHORT, Gravity.CENTER);
                        clearSearch();
                    }
                } else if (check.equals(AppConstant.GOOGLE_DIRECTION_GET)) {
                    googleResponse = routes;
                    processGoogleResponse();
                }
            } else
                clearSearch();
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error, String queryType) {
        mProgress.setVisibility(View.INVISIBLE);
        clearSearch();
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode, queryType);
            } else {
                mInstance.myLibrary.DisplayToast(act, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(final CharSequence s, int start, int before, int count) {
        final String text = mSelectedView.getText().toString();
        int visible = mList.getVisibility();
        if (visible != View.VISIBLE)
            showMap(false);
        if (!mSelected) {
            if (text.length() == 0) {
                if (mSelectedView.getId() == mFromText.getId())
                    fromLoc = null;

                mSelected = false;
                mSearchText.setText(R.string.searches);
                if (recentSearchList != null)
                    setCachedAddress();
            } else if (text.length() > 2) {
//                streetAdapter.resetUpdating(false);
                mSearchText.setText(R.string.results);
                mCached = false;
                Thread back = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        streetAdapter.getResults(text);
                    }
                });
                back.start();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mSelected) {
            mSelected = false;
            if (!mCached) {
                mSelected = false;
                mSearchText.setText(R.string.searches);
                if (recentSearchList != null)
                    setCachedAddress();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mSelectedView = (CustomEditTextView) v;
            if (!mSearch) {
                clearSearch();
                selectView(mSelectedView, true);
            }
        } else {
            if (!mSearch) {
                CustomEditTextView temp = (CustomEditTextView) v;
                selectView(temp, false);
            }
        }
    }

    private void selectView(final CustomEditTextView view, boolean remove) {
//        streetAdapter.resetUpdating(true);
        String text = view.getText().toString();
        if (!text.isEmpty() && !text.equals("")) {
            if (view.getId() == mFromText.getId()) {
                if (text.contains("from ")) {
                    if (remove)
                        text = text.replace("from ", "");
                } else {
                    if (!remove)
                        text = "from " + text;
                }
            } else if (view.getId() == mToText.getId()) {
                if (text.contains("to ")) {
                    if (remove)
                        text = text.replace("to ", "");
                } else {
                    if (!remove)
                        text = "to " + text;
                }
            }
            if (!remove) {
                mSelected = true;
                view.setCustomText(text);
                mSelected = false;
            } else {
                view.setCustomText(text);
                final int len = text.length();
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setSelection(len);
                    }
                });
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        CameraUpdate Update = CameraUpdateFactory.newLatLngZoom(new LatLng(AppConstant.CENTER_LAT, AppConstant.CENTER_LON), 6);
        mMap.moveCamera(Update);
        setUpMap();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (cameraPosition.zoom > AppConstant.MAXZOOM) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(AppConstant.MAXZOOM));
        } else if (cameraPosition.zoom < AppConstant.MINZOOM) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(AppConstant.MINZOOM));
        }
        if (zoom) {
            final float zoom = cameraPosition.zoom;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom - 0.25f));
            this.zoom = false;
        }
    }

    @Override
    public void onMapLoaded() {
        maploaded = true;
        placeMarker(tempRoute);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        double minDistance = 10000;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 0; i < polylineArray.size(); i++) {
            double distance = mInstance.myLibrary.distanceOfPoint(latLng, polylineArray.get(i));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                if (nearestDistance < minDistance)
                    selectedRoute = i;
            }
        }
        showDetails();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }
}
