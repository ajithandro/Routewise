package com.tnedicca.routewise.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.MainActivity;
import com.tnedicca.routewise.activities.MainMenu;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.pageviewer.AutoScrollViewPager;
import com.tnedicca.routewise.pageviewer.CirclePageIndicator;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.SeekArc;

import org.apache.log4j.chainsaw.Main;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by new on 04-01-2017.
 */

public class Dashboard extends Fragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private SeekArc mSeekArc;
    private View mDough;
    ArrayList<String> volleyArray = new ArrayList<String>();
    public SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;

    private float[] pagerValues = {0, 0};
    private AutoScrollViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private RouteWise mInstance;
    private ViewGroup rootView;
    private ScreenSlidePageFragment slide;
    private int lineNumber;
    private String mAccessToken;

    private CustomButton mMyTrip;
    private CustomButton mPlanTrip;
    private CustomButton mLogout;
    private MainMenu mainMenu;
    private RouteLog logger;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        rootView = (ViewGroup) inflater.inflate(R.layout.dashboard, container, false);
        mainMenu = (MainMenu) getActivity();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mInstance = RouteWise.getInstance();
        mainMenu = (MainMenu) getActivity();
        logger = new RouteLog();
        logger.setLoggerClass(Dashboard.class);

        mPrefer = mainMenu.getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);

        mAccessToken = mPrefer.getString(AppConstant.TOKEN, null);
        pagerValues[0] = mPrefer.getFloat(AppConstant.PAGER_VALUE0, 0);
        pagerValues[1] = mPrefer.getFloat(AppConstant.PAGER_VALUE1, 0);
//        mSeekArc = (SeekArc) findViewById(R.id.seekArcComplete);
//        mDough = findViewById(R.id.doughnut_container);

        getDetails();
//        sendViewToBack(mDough);
        mPager = rootView.findViewById(R.id.pager);
        mMyTrip = rootView.findViewById(R.id.my_trip);
        mPlanTrip = rootView.findViewById(R.id.plan_trip);
        mLogout = rootView.findViewById(R.id.logout);

        mMyTrip.setOnClickListener(this);
        mPlanTrip.setOnClickListener(this);
        mLogout.setOnClickListener(this);

        mPagerAdapter = new ScreenSlidePagerAdapter(this.getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setInterval(5000);
        mPager.addOnPageChangeListener(this);

        permissionView();
    }

    //    Your researching is not enough @vishal. we can't progress with this speed
//
//
//    you need to speed up your progress
//    Eventhough we don't use the Html.fromhtml its taking some time right to load
    private void permissionView() {
        //Set the pager with an adapter
        ViewPager pager = rootView.findViewById(R.id.pager);
        pager.setAdapter(mPagerAdapter);

        //Bind the title indicator to the adapter
        CirclePageIndicator titleIndicator = rootView.findViewById(R.id.pagerindicator);

//        final float density = getResources().getDisplayMetrics().density;
//        titleIndicator.setBackgroundColor(0xFFCCCCCC);
//        titleIndicator.setRadius(10 * density);
        titleIndicator.setPageColor(0x880000FF);
        titleIndicator.setFillColor(0xFF888888);
//        titleIndicator.setStrokeColor(0xFF000000);
//        titleIndicator.setStrokeWidth(2 * density);

        titleIndicator.setViewPager(pager);
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

    @Override
    public void onResume() {
        super.onResume();
        if (mPager != null)
            mPager.startAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPager != null)
            mPager.stopAutoScroll();
    }

    private void getDetails() {
        String url = BuildConfig.REST_URL + AppConstant.DASHBOARD_URL;
        int method = Request.Method.GET;
        String tag = AppConstant.DASHBOARD;
        makeJsonArryReq(url, method, tag);
    }

    public static void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.my_trip:
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, new MyTripsList());
                ft.commit();
                mainMenu.previousMenuId = R.id.trip_list;
                mainMenu.screenTitle.setCustomText(getString(R.string.my_trips_title));
                mainMenu.infoIcon.setVisibility(View.INVISIBLE);
                break;
            case R.id.plan_trip:
                final FragmentTransaction fragment = getFragmentManager().beginTransaction();
                fragment.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                fragment.replace(R.id.content_frame, new PlanTrip());
                fragment.commit();
                mainMenu.previousMenuId = R.id.menu_plan_trip;
                mainMenu.screenTitle.setCustomText(getString(R.string.title_plan_trip));
                mainMenu.infoIcon.setVisibility(View.INVISIBLE);
                break;
            case R.id.logout:
                logout();
                break;
            default:
                logger.info(getString(R.string.log_terms_and_condition_onclick_default) + id);
                break;
        }
    }

    private void logout() {
        logger.info("Logout Initiated");
        mInstance.myLibrary.alertBox(mainMenu, "LOGOUT", getString(R.string.logout_message), "LOGOUT", "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInstance.myLibrary.logout();

                Intent intent = new Intent(mainMenu, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                mainMenu.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
        });
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            slide = new ScreenSlidePageFragment();

            Bundle args = new Bundle();
            args.putInt(AppConstant.PAGE_NO, position);
            args.putFloat(AppConstant.PAGER_VALUE, pagerValues[position]);
            slide.setArguments(args);
            return slide;
        }

        @Override
        public int getCount() {
            return pagerValues.length;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag) {
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
                headers.put(AppConstant.REST_ACCESS_TOKEN, mAccessToken);
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
    private void response(String check, JSONArray response, int code) {
        mInstance.myLibrary.checkResponse(response, code, mainMenu);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
//            mInstance.myLibrary.DisplayToast(act, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            JSONObject result = response.optJSONObject(0);
            int status = response.optInt(1);
            String message = result.optString("message");
            if (status == 200) {
                if (check.equals(AppConstant.DASHBOARD)) {
                    logger.info("Recieved Dashboard Details");
                    JSONObject obj = response.optJSONObject(0);
                    double distance = obj.optDouble(AppConstant.TOTAL_DISTANCE);
                    double riskScore = obj.optDouble(AppConstant.RISK_SCORE);
                    double drivingScore = obj.optDouble(AppConstant.DRIVING_SCORE);

                    pagerValues[0] = (float) mInstance.myLibrary.round(riskScore, 2);
                    pagerValues[1] = (float) mInstance.myLibrary.round(distance, 2);

                    edit = mPrefer.edit();
                    edit.putFloat(AppConstant.PAGER_VALUE0, pagerValues[0]);
                    edit.putFloat(AppConstant.PAGER_VALUE1, pagerValues[1]);
                    edit.commit();
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
//                mInstance.myLibrary.DisplayToast(act, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        slide.onResumeFragment(position, pagerValues[position]);
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

}
