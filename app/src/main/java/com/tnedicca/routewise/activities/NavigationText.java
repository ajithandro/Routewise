package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Aachu on 27-02-2017.
 */
public class NavigationText extends Activity implements View.OnClickListener {

    String[] tempImages = {"fork_left", "fork_right", "keep_left", "keep_right",
            "merge", "ramp_left", "ramp_right", "roundabout_left", "roundabout_right", "straight", "turn_left", "turn_right",
            "turn_sharp_left", "turn_sharp_right", "turn_slight_left", "turn_slight_right", "uturn_left", "uturn_right"};
    List<String> maneuversList = Arrays.asList(tempImages);

    private SharedPreferences mPrefer;
    private RouteWise mInstance;
    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;
    private Bundle intent;
    private JSONObject mNavigateRoute;
    private int lineNumber;

    private String from;
    private String to;
    private String routeName;
    private String distance;
    private String duration;
    private CustomTextView routeNameText;
    private CustomTextView durationText;
    private CustomTextView distanceText;
    private ListView mListView;
    private ManeuverList mAdapter;

    private ArrayList<String> maneuverArray = new ArrayList<>();
    private ArrayList<String> navigationArray = new ArrayList<>();
    private ArrayList<String> distanceArray = new ArrayList<>();
    private RouteLog logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(NavigationText.class);

        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.title_plan_trip));
        infoIcon.setVisibility(View.INVISIBLE);
//        backIcon.setOnClickListener(this);

        intent = getIntent().getExtras();
        if (intent != null) {
            from = intent.getString(AppConstant.INTENT_FROM_ADDRESS);
            to = intent.getString(AppConstant.INTENT_TO_ADDRESS);
            routeName = intent.getString(AppConstant.INTENT_ROUTE_NAME);
            distance = intent.getString(AppConstant.INTENT_DISTANCE);
            duration = intent.getString(AppConstant.INTENT_DURATION);
//            path = intent.getString(AppConstant.INTENT_DIRECTIONS);
            String temp = intent.getString(AppConstant.INTENT_NAVIGATION);

            try {
                mNavigateRoute = new JSONObject(temp);
            } catch (Exception e) {
                lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                logger.error("Error at " + lineNumber, e);
            }
        }

        routeNameText = findViewById(R.id.route_name);
        durationText = findViewById(R.id.duration);
        distanceText = findViewById(R.id.distance);
        mListView = findViewById(R.id.list_view);

        routeNameText.setCustomText(routeName);
        durationText.setCustomText(duration);
        distanceText.setCustomText("(" + distance + ")");

        calcData();
        mAdapter = new ManeuverList(this);
        mListView.setAdapter(mAdapter);
    }

    private void calcData() {
        JSONArray tempLegs = mNavigateRoute.optJSONArray("legs");

        JSONObject temp;
        JSONObject legs = tempLegs.optJSONObject(0);
//        if (!from.equals("Your location"))
//            from = legs.optString("start_address");
//        to = legs.optString("end_address");

        JSONArray steps = legs.optJSONArray("steps");
        int count = steps.length();
        for (int i = 0; i < count; i++) {
            temp = steps.optJSONObject(i);
            JSONObject tempDistance = temp.optJSONObject("distance");
            String instruction = temp.optString("html_instructions");
            String maneuver = temp.optString("maneuver");

            instruction = Html.fromHtml(instruction).toString();
            if (maneuver == null) {
                maneuver = "";
            }
            maneuverArray.add(maneuver);
            navigationArray.add(instruction);
            long tempDis = tempDistance.optLong("value");
            double dist = tempDis / 1609.34;
            String dis = String.format("%.2f", dist);
            dis = dis + " mi";

            distanceArray.add(dis);
        }
    }

    public class ManeuverList extends BaseAdapter {
        List<maneuverListData> dataList;
        int count = 0;
        ViewHolder holder;
        private LayoutInflater layoutInflater;

        public ManeuverList(Context context) {
            layoutInflater = LayoutInflater.from(context);
            dataList = getDataForList();
        }

        @Override
        public int getCount() {
            count = dataList.size();
            return count;
        }

        @Override
        public maneuverListData getItem(int arg0) {
            return dataList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            if (view == null) {
                holder = new ViewHolder();
                view = layoutInflater.inflate(R.layout.maneuver_text, null);
                holder.maneuverImage = view.findViewById(R.id.maneuver_image);
                holder.navigationText = view.findViewById(R.id.navigation_text);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            final maneuverListData data = dataList.get(position);
            int icon = getResources().getIdentifier(data.maneuver, "drawable", getPackageName());

            String navi = data.navigation;
            String dis = data.distance;

            if (dis != null) {
                SpannableString ss1 = new SpannableString(dis);
                ss1.setSpan(new StyleSpan(Typeface.BOLD), 0, ss1.length(), 0);

                holder.navigationText.setCustomText("");
                holder.navigationText.append(navi);
                holder.navigationText.append("\n");
                holder.navigationText.append(ss1);
            } else
                holder.navigationText.setCustomText(navi);
            holder.maneuverImage.setImageResource(icon);
            return view;
        }

        class ViewHolder {
            ImageView maneuverImage;
            CustomTextView navigationText;
        }
    }

    public List<maneuverListData> getDataForList() {
        List<maneuverListData> dataForManeuverList = new ArrayList<maneuverListData>();
        int count = maneuverArray.size() + 2;
        for (int i = 0; i < count; i++) {
            maneuverListData chapter = new maneuverListData();
            if (i == 0) {
                chapter.maneuver = "ic_start_marker";
                chapter.navigation = from;
            } else if (i == count - 1) {
                chapter.maneuver = "ic_end_marker";
                chapter.navigation = to;
            } else {
                String maneuver = maneuverArray.get(i - 1);
                String distance = distanceArray.get(i - 1);
                String navigation = navigationArray.get(i - 1);
                maneuver = maneuver.replaceAll("-", "_");
                navigation = navigation.replaceAll("\\n\\n", "\n");
                navigation = navigation.replaceAll("\\n", ". ");

                boolean show = maneuversList.contains(maneuver);
                if (!show)
                    maneuver = "empty";

                chapter.maneuver = "ic_" + maneuver;
                chapter.navigation = navigation;
                chapter.distance = "(" + distance + ")";
            }
            dataForManeuverList.add(chapter);
        }
        return dataForManeuverList;
    }

    public class maneuverListData {
        String navigation;
        String distance;
        String maneuver;
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
                logger.info("default statement executed for onClick for view id : " + id);
                break;
        }
    }
}
