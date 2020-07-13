package com.tnedicca.routewise.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.google.gson.Gson;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.DetailedView;
import com.tnedicca.routewise.activities.MainMenu;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.adapters.MyRecyclerViewAdapter;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.classes.Trip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by new on 07-02-2017.
 */

public class MyTripsList extends Fragment implements MyRecyclerViewAdapter.MyClickListener {

    ArrayList<String> volleyArray = new ArrayList<String>();
    ArrayList<Trip> tripArrayList;
    int lineNumber;
    MyTripsList myTripsList = this;
    private RouteWise mInstance;
    private ProgressDialog progressDialog;
    private SharedPreferences mPrefer;
    private Context context;
    private SharedPreferences.Editor edit;
    private ViewGroup rootView;
    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public DataFetcher dataFetcher;
    private DataSaver dataSaver;
    private MainMenu mainMenu;
    private RouteLog logger;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(MyTripsList.class);
        init();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.my_trip_fragment, container, false);
        mRecyclerView = rootView.findViewById(R.id.trip_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        dataFetcher = new DataFetcher();
        dataFetcher.execute();
        return rootView;
    }

    private void init() {
        context = getActivity().getApplication();
        mainMenu = (MainMenu) getActivity();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        tripArrayList = new ArrayList<>();
        mPrefer = context.getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
    }

    private void fetchTrips() {
        int method = Request.Method.GET;
        String url = BuildConfig.REST_URL + AppConstant.TRIP_LIST_URL;
        if (mInstance.dataStatus) {
            makeJsonArryReq(url, method, AppConstant.TRIPS_LIST_GET, AppConstant.TRIPS_LIST_GET);
        } else {
            logger.info(getString(R.string.log_connecting_rest_api));
            mInstance.myLibrary.DisplayToast(context, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
//            if (tripArrayList == null || tripArrayList.size() < 1) {
//                displayDashboard();
//            }
        }
    }

    private void addTripsToList(JSONArray result) {
        tripArrayList = new ArrayList<Trip>();
        for (int i = 0; i < result.length(); i++) {
            JSONObject object = result.optJSONObject(i);
            if (object != null) {
                Trip trip = new Trip();
                trip.setPathId(object.optString(AppConstant.PATH_ID));
                trip.setMapImage(context, object.optString(AppConstant.MAP_THUMBNAIL_URL));
                trip.setTripDate(mInstance.myLibrary.fetchDate(Long.parseLong(object.optString(AppConstant.START_TIME)) * 1000));
                trip.setStartPoint(object.optString(AppConstant.START_LOCATION));
                trip.setEndPoint(object.optString(AppConstant.END_LOCATION));
                trip.setTripLength(mInstance.myLibrary.fetchDistanceAndTime(Double.parseDouble(object.optString(AppConstant.TRIP_LENGTH)), Integer.parseInt(object.optString(AppConstant.TRIP_TIME))));
                trip.setRiskScore(object.optString(AppConstant.ROUTE_RISK_FACTOR));
                tripArrayList.add(trip);
            }
        }
        mAdapter = new MyRecyclerViewAdapter(tripArrayList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mAdapter.setMyClickListener(myTripsList);
        dataSaver = new DataSaver();
        dataSaver.execute();
    }

    public void saveList(List<Trip> tripList) {
        Gson gson = new Gson();
        String jsonTripsList = gson.toJson(tripList);
        edit = mPrefer.edit();
        edit.putString(AppConstant.TRIPLIST, jsonTripsList);
        edit.commit();
    }

    public ArrayList<Trip> getTripsList() {
        List<Trip> tripList;
        if (mPrefer.contains(AppConstant.TRIPLIST)) {
            String jsonTripsList = mPrefer.getString(AppConstant.TRIPLIST, null);
            Gson gson = new Gson();
            Trip[] trips = gson.fromJson(jsonTripsList, Trip[].class);
            tripList = Arrays.asList(trips);
            tripList = new ArrayList<Trip>(tripList);
        } else
            return null;

        return (ArrayList<Trip>) tripList;
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, response -> {
            volleyArray.remove(tag);
//                logger.info(getString(R.string.received_response));
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
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
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
//        progressDialog.dismiss();
        mInstance.myLibrary.checkResponse(response, code, mainMenu);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            mInstance.myLibrary.DisplayToast(context, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
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
                if (check.equals(AppConstant.TRIPS_LIST_GET)) {
                    displayServerMsg(result, status);
                }
            }
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error, String queryType) {
//        progressDialog.dismiss();
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
                mInstance.myLibrary.DisplayToast(context, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
//                if (tripArrayList == null || tripArrayList.size() < 1) {
//                    displayDashboard();
//                }
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

//    private void displayDashboard() {
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                final FragmentTransaction ft = getFragmentManager().beginTransaction();
//                ft.replace(R.id.content_frame, new Dashboard());
//                ft.commit();
//                mainMenu.previousMenuId = 2;
//                mainMenu.screenTitle.setCustomText(getString(R.string.title_dashboard));
//                mainMenu.infoIcon.setVisibility(View.VISIBLE);
//            }
//        }, 2000);
//    }

    // display server message
    private void displayServerMsg(JSONArray result, int code) {
        switch (code) {
            case AppConstant.RESPONSE_SUCCESS:
            case AppConstant.RESPONSE_GOOGLE_ANONYMUS:
                addTripsToList(result);
                break;
            case AppConstant.RESPONSE_USER_FOR_TRIP_NOT_AVAILABLE:
                mInstance.myLibrary.DisplayToast(context, AppConstant.USER_NOT_AVAILABLE, Toast.LENGTH_SHORT, Gravity.CENTER);
                break;
            case AppConstant.RESPONSE_UPDATION_FAILED:
                mInstance.myLibrary.DisplayToast(context, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
            default:
                logger.info("default executed for displayServerMsg for response code : " + code);
                break;
        }
    }

    @Override
    public void onItemClick(int position, View v) {
        String pathId = tripArrayList.get(position).getPathId();
        Intent intent = new Intent(mainMenu, DetailedView.class);
        intent.putExtra(AppConstant.PATH_ID, pathId);
        startActivity(intent);
        mainMenu.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
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
    public void onDetach() {
        super.onDetach();
        if (dataSaver != null) {
            dataSaver.cancel(true);
        }
        if (dataFetcher != null) {
//            progressDialog.dismiss();
            dataFetcher.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if(mAdapter != null) {
            ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                    .MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.i("app", "nnnnnnnnnnnnn");
                }
            });
        }*/
    }

    public class DataFetcher extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(AppConstant.LOADING);
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            tripArrayList = getTripsList();
            return mAdapter;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (tripArrayList != null) {
                mAdapter = new MyRecyclerViewAdapter(tripArrayList);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setMyClickListener(myTripsList);
                mAdapter.notifyDataSetChanged();
            }
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }, 500);
            fetchTrips();
        }
    }

    private class DataSaver extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            saveList(tripArrayList);
            return null;
        }

    }
}

