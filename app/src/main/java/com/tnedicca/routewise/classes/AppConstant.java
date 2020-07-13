package com.tnedicca.routewise.classes;

import com.tnedicca.routewise.BuildConfig;

public final class AppConstant {

//    private static AppConstant constant;

//    public static synchronized AppConstant getInstance() {
//        if (constant == null) {
//            constant = new AppConstant();
//        }
//        return constant;
//    }

    public static final String KEY_APP_FOLDER = "/data/data/" + BuildConfig.APPLICATION_ID;
    public static final String KEY_LOG_FILE_PATH = KEY_APP_FOLDER + "/LogFile";
    static final String KEY_DB_STORAGE_PATH = KEY_APP_FOLDER + "/databases/user_data";
    public static final String KEY_LOG_RUN_TIME = "/RTE";
    public static final String KEY_FULL_DB_STORAGE_PATH = KEY_APP_FOLDER + "/databases";
    public static final String KEY_LOG_PATH = "/RouteWise/LogFile";
    public static final String KEY_DB_PATH = "/RouteWise/databases";
    public static final String KEY_TARGET_DIR = "/RouteWise";
    public static final String DEFAULT_DB_NAME = "RouteWise";
    static final int DEFAULT_DB_VERSION = 1;
    static final String DB_EXTENTION_NEW = ".route";
    static final String LOG_FILE_NAME = "RouteWise.txt";
    static final String PROCESS_SERVICE = ":routingService";

    public static final String PREFERENCE = "RouteWisePreference";
    static final String SEARCHED_ADDRESS = "search_address";
    public static final String PLACE_ADDRESS = "place_address";
    public static final String PLACE_ADDRESS_ID = "place_address_id";
    public static final String SEARCHED_LATITUDE = "search_latitude";
    public static final String SEARCHED_LONGITUDE = "search_longitude";

    // SQLite DB Values
    static final String DB_TABLE_LOCATION = "location_details";
    static final String DB_TABLE_ACCELEROMETER = "accelerometer_details";
    static final String DB_TABLE_GRAVITY = "gravity_details";
    static  final String DB_TABLE_ACTIVITY = "activity_details";
    static final String DB_TABLE_SCREEN = "screenstatus";
    static final String DB_TABLE_PROXIMITY = "proximity_details";
    static final String DB_TABLE_GYROSCOPE = "gyroscope_details";
    static final String DB_TABLE_LIGHT = "light_details";
    static final String DB_TABLE_TEMPERATURE = "temperature_details";
    static final String DB_TABLE_MEGNATICFIELDS = "megnetic_fields";

    static final String KEY_ROWID = "id";
    public static final String KEY_TIME = "time";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_SPEED = "speed";
    static final String KEY_ALTITUDE = "altitude";
    static final String KEY_BEARING = "bearing";
    static final String KEY_ACCURACY = "accuracy";
    static final String KEY_UPLOADED = "is_uploaded";
    static final String KEY_X = "x";
    static final String KEY_Y = "y";
    static final String KEY_Z = "z";
    static final String KEY_ACT_TYPE = "activity_type";
    static final  String KEY_CONFIDENCE = "confidence";
    static final String KEY_ISDRIVING = "isdriving";
    static final String KEY_LIGHT = "light";
    static final String KEY_SCREEN_STATUS = "screen_status";
    static final String KEY_SENSORTIME = "sensortime";

    // SQLite Queries
    static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_LOCATION
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_LATITUDE + " DOUBLE," + KEY_LONGITUDE + " DOUBLE,"
            + KEY_TIME + " INTEGER UNIQUE," + KEY_SPEED + " DOUBLE," + KEY_ACCURACY + " DOUBLE,"
            + KEY_ALTITUDE + " DOUBLE," + KEY_BEARING + " DOUBLE," + KEY_UPLOADED + " BOOLEAN DEFAULT 0)";

    static final String CREATE_TABLE_ACCELEROMETER = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_ACCELEROMETER
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_X + " FLOAT," + KEY_Y + " FLOAT,"
            + KEY_Z + " FLOAT," + KEY_ACCURACY + " INTEGER," + KEY_SENSORTIME + " LONG," + KEY_TIME + " LONG)";

    static final String CREATE_TABLE_PROXIMITY = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_PROXIMITY
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_X + " FLOAT," + KEY_Y + " FLOAT,"
            + KEY_Z + " FLOAT," + KEY_ACCURACY + " INTEGER," + KEY_SENSORTIME + " LONG," + KEY_TIME + " LONG)";

    static final String CREATE_TABLE_GRAVITY = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_GRAVITY
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_X + " FLOAT," + KEY_Y + " FLOAT,"
            + KEY_Z + " FLOAT," + KEY_ACCURACY + " INTEGER,"  +  KEY_SENSORTIME + " LONG," + KEY_TIME + " LONG)";

    static final String CREATE_TABLE_LIGHT = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_LIGHT
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_LIGHT + " FLOAT," + KEY_SENSORTIME + " LONG," +  KEY_TIME + " LONG)";

    static final String CREATE_TABLE_GYROSCOPE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_GYROSCOPE
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_X + " FLOAT," + KEY_Y + " FLOAT,"
            + KEY_Z + " FLOAT,"  + KEY_ACCURACY + " INTEGER," + KEY_SENSORTIME + " LONG," +  KEY_TIME + " LONG)";

    static final String CREATE_TABLE_MAGNATIC_FIELDS = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_MEGNATICFIELDS
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_X + " FLOAT," + KEY_Y + " FLOAT,"
            + KEY_Z + " FLOAT,"  + KEY_ACCURACY + " INTEGER," + KEY_SENSORTIME + " LONG," +  KEY_TIME + " LONG)";

    static final String CREATE_TABLE_ACTIVITY = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_ACTIVITY
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_ACT_TYPE + " TEXT," + KEY_CONFIDENCE + " INTEGER,"
            + KEY_ISDRIVING + " BOOLEAN ," + KEY_TIME + " LONG)";

    static final  String CREATE_TABLE_SCREEN_STATUS = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_SCREEN
            + " (" + KEY_ROWID + " INTEGER PRIMARY KEY," + KEY_SCREEN_STATUS + " BOOLEAN DEFAULT 0 ," + KEY_TIME + " LONG)";




    // Geofence related Configs
    public static final String GEOFENCE_REQUEST_ID = "R_FENCE";
    public static final int GEOFENCE_RADIUS = 50;
    public static final int GEOFENCE_VALIDITY = 24 * 60 * 60 * 1000;

    // Google Map uses
    public static final float MAXZOOM = 19.0f;
    public static final float MINZOOM = 3.0f;
    public static final double CENTER_LAT = 39.098228;
    public static final double CENTER_LON = -84.521177;

    // BackGround Location Collection Logics
    public static final int MAX_SYNC_COUNT = 1000;
    public static final long LOCATION_INTERVAL = 9;
    //    public static final long SYNC_INTERVAL = 30;
    static final long ONE_DAY_INTERVAL_IN_SECONDS = 60 * 60 * 24;
    public static final long SYNC_INTERVAL = 60;
    public static final long LOCATION_SPLIT_INTERVAL = 301;
    public static final long UPLOAD_INTERVAL = 330;
    public static final float LOCATION_ACCURACY = 100;
    public static final long LOCATION_DISTANCE_BUFFER = 50;
    public static final long LOCATION_RESTART = 60;

    // Status Bar Notifications
    public static final int NOTIFY_LOC_SERVICE = 930;
    public static final int NOTIFY_ACT_RECIEVER = 931;
    public static final int NOTIFY_LOC_RECIEVER = 932;
    public static final int NOTIFY_AUTO_TRACKING_ENABLED = 933;
    public static final int NOTIFY_AUTO_ALARM_STARTED = 934;
    public static final int NOTIFY_GEOFENCE = 935;
    public static final int NOTIFY_BOOT_COMPLETE = 936;
    public static final int NOTIFY_TRIP_COMPLETE = 937;
    public static final int NOTIFY_MOTION_ACTIVITY = 938;
    public static final int NOTIFY_SERVICE = 939;
    public static final int NOTIFY_GPS = 940;

    // GPS Settings
    private static final long ACTIVITY_INTERVAL_IN_SECONDS = 1;
    public static final int INITIAL_CONFIDENCE = 45;
    public static final int ACTIVITY_CONFIDENCE = 65;
    public static final int BICYCLE_ACTIVITY_CONFIDENCE = 85;
    public static final int BICYCLE_INITIAL_CONFIDENCE = 65;
    public static final long GPS_UPDATE_DISTANCE = 1;
    public static final long GPS_BATTERY_UPDATE_DISTANCE = 250;
    public static final float GPS_ACCURACY = 15.0f;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 1;
    private static final float FASTEST_INTERVAL_IN_SECONDS = 0.5f;
    private static final int BATTERY_UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final float BATTERY_FASTEST_INTERVAL_IN_SECONDS = 2.5f;
    public static final double MAP_ZOOM_LATITUDE = 20;
    public static final double MAP_ZOOM_LONGITUDE = 83;

    // Mock Locations
    public static double MOCK_LATITUDE = 12.958546;
    public static double MOCK_LONGITUDE = 80.209898;

    static final double LATITUDE_MAX = 90;
    static final double LATITUDE_MIN = -90;
    static final double LONGITUDE_MAX = 180;
    static final double LONGITUDE_MIN = -180;

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // 24 Hours Calc
    public static final long HOURS_24 = 24 * 60 * 60 * MILLISECONDS_PER_SECOND;
    public static final long GPS_UPDATE_TIME = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    public static final long GPS_FAST_UPDATE_TIME = (long) (MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS);
    public static final long BATTERY_GPS_UPDATE_TIME = MILLISECONDS_PER_SECOND * BATTERY_UPDATE_INTERVAL_IN_SECONDS;
    public static final long BATTERY_GPS_FAST_UPDATE_TIME = (long) (MILLISECONDS_PER_SECOND * BATTERY_FASTEST_INTERVAL_IN_SECONDS);
    public static final long ACTIVITY_INTERVAL_TIME = (MILLISECONDS_PER_SECOND * ACTIVITY_INTERVAL_IN_SECONDS);

//    public static final long LOCATION_INTERVAL = (long) (MILLISECONDS_PER_SECOND * LOCATION_INTERVAL_IN_SECONDS);
//    public static final long LOCATION_SPLIT_INTERVAL = (long) (MILLISECONDS_PER_SECOND * LOCATION_SPLIT_INTERVAL_IN_SECONDS);
//    public static final long UPLOAD_INTERVAL = (long) (MILLISECONDS_PER_SECOND * UPLOAD_INTERVAL_IN_SECONDS);
//    public static final long LOCATION_RESTART = (long) (MILLISECONDS_PER_SECOND * LOCATION_RESTART_IN_SECONDS);

//    public static final LocationRequest REQUEST = LocationRequest.create().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    // General Details
    public static final String SDK = "Sdk";
    public static final String SCREEN_DENSITY = "Screen_Density";
    public static final String SCREEN_HEIGHT = "Screen_Height";
    public static final String SCREEN_WIDTH = "Screen_Width";

    // Intent variables
    public static final String INTENT_PERMISSION = "permission";
    public static final String QUIT_APP = "quit_app";

    // Android 6.0 Response Codes
//    public static final int RESPONSE_6_COMMON = 180;
//    public static final int RESPONSE_6_GPLUS = 181;
    public static final int RESPONSE_6_LOCATION = 182;
    //    public static final int RESPONSE_6_LOCATION0 = 183;
//    public static final int RESPONSE_6_CAM_STORAGE = 184;
    public static final int RESPONSE_6_STORAGE = 185;
    public static final int RESPONSE_6_SPLASH = 186;

    // REST Service Responses
    public static final String SERVER_RESPONSE = "Volley Response :";
    public static final String RESPONSE_CODE = "Status : ";
    public static final String SUCCESS_MESSAGE = "Success";
    public static final int RESPONSE_GOOGLE_ANONYMUS = 304;
    public static final int RESPONSE_SUCCESS = 200;
    public static final int RESPONSE_USER_UNAVAILABLE = 202;
    public static final int RESPONSE_LOGIN_FAILED = 205;
    public static final int RESPONSE_ACTIVATION_PENDING = 206;
    public static final int RESPONSE_USER_UNSUBSCRIBED = 207;
    public static final int RESPONSE_BAD_REQUEST = 400;
    public static final int RESPONSE_GET_LOGS = 416;
    public static final int RESPONSE_DEVICE_CHANGED = 417;
    static final int RESPONSE_UPDATE_APP_MANDATE = 418;
    static final int RESPONSE_UPDATE_APP = 419;
    public static final int RESPONSE_DATA_EXIST = 405;
    public static final int RESPONSE_SERVER_ERROR = 500;
    public static final int RESPONSE_USER_ALREADY_REGISTERED = 202;
    public static final int RESPONSE_PHONE_NUMBER_ALREADY_REGISTERED = 203;
    public static final int RESPONSE_USER_NOT_AVAILABLE = 202;
    public static final int RESPOMSE_PHONE_NUMBER_NOT_AVAILABLE = 203;
    public static final int RESPONSE_NO_CHANGE = 208;
    public static final int RESPONSE_NO_USER = 406;
    public static final int RESPONSE_UPDATION_FAILED = 501;
    public static final int RESPONSE_USER_FOR_TRIP_NOT_AVAILABLE = 203;
    public static final int RESPONSE_PASSWORD_VALIDATION_FAILED = 204;

    // Replace Patterns
    public static final String REPLACE_COMMA = "\\$\\^";
    public static final String ORIGINAL_COMMA = ",";
    public static final String REPLACE_LINEBREAK = "<br>";
    public static final String UNDO_LINEBREAK = "\\r?\\n";
    public static final String ORIGINAL_LINEBREAK = "\n";
    public static final String ORIGINAL_QUOTES = "\"";
    public static final String ORIGINAL_QUOTES_STRING = "&quote";

    static final String ENCODE_ORIGINAL_PLUS = "\\+";
    static final String ENCODE_PLUS = "%20";

    // REST Service Details
    public static final String REST_KEY_API = "x-api-key";
    public static final String REST_KEY_CONTENT = "Content-type";
    public static final String REST_KEY_CONTENT_TYPE = "application/json";
    public static final String REST_KEY_TYPE = "x-type";
    public static final String REST_ACCESS_TOKEN = "x-access-token";
    public static final String REST_ID = "x-id";
    public static final String REST_X_RANGE = "x-range";
    public static final String REST_X_FROM = "x-from";
    public static final String REST_X_TO = "x-to";

    static final String API_ENCRYPTION_KEY = "o3OrigHYeMpFRlhbCWN8mR61OBb8KM6k";
    static final String DATA_ENCRYPTION_KEY = "srjBRQ07WNjls71fUkClW0tGd82EJr9p";
    static final String API_KEY_DIVIDER1 = "&qit";
    static final String API_KEY_DIVIDER2 = "/tin";

    static final String API_KEY = "dyTnbq6gasWMmSj64jUepsFVzLKgBT8TzA7E+ypaA7NXB5yPj2k0Eq0fGzUPNObd4y2jWmmm0E1/Fk7x4CY=";

    public static final String STATIC_MAP_URL = "https://maps.googleapis.com/maps/api/staticmap?size=500x500&scale=2&maptype=roadmap&markers=color:green|label:A|$start&markers=color:red|label:B|$end&path=color:0x0000ff|weight:5|$path&key=\"YOUR_KEY\"";

    public static final String LOGIN_URL = "login";
    public static final String REPORT_BATTERY_URL = "User/reportBattery";
    public static final String SUBSCRIBE_URL = "User/subscribe";
    public static final String VEHICLE_URL = "getVehicles";
    public static final String REGISTER_URL = "register";
    public static final String ACTIVATE_URL = "User/activate";
    public static final String SYNC_URL = "User/sync";
    public static final String PING_URL = "User/ping";
    public static final String DASHBOARD_URL = "User/getDetails";
    public static final String FORGOT_URL = "getForgotDetails";
    public static final String UNSUBSCRIBE_URL = "User/unsubscribe";
    public static final String FORGOT_PASSWORD_URL = "forgotPassword";
    public static final String INSURANCE_URL = "User/insurance";
    public static final String TRIP_LIST_URL = "User/getTrips";
    public static final String GOOGLE_DIRECTION_URL = "User/googleDirection";
    public static final String RISK_SCORE_URL = "User/riskScoreAPI";
    public static final String TEMPORAL_URL = "User/temporalAPI";
    public static final String TRIP_DETAIL_URL = "User/getRoute";
    public static final String PASSENGER_STATUS_URL = "User/passenger";

    public static final String SYNC_ROUTE = "route";
    public static final String SYNC_DATA_COUNT = "data_count";

    // Google Response Keys
    public static final String GOOGLE_OVERVIEW_POLYLINE = "overview_polyline";
    public static final String GOOGLE_POINTS = "points";
    public static final String GOOGLE_SUMMARY = "summary";
    public static final String GOOGLE_LEGS = "legs";
    public static final String GOOGLE_DURATION = "duration";
    public static final String GOOGLE_DISTANCE = "distance";
    public static final String GOOGLE_VALUE = "value";
    public static final String GOOGLE_STEPS = "steps";
    public static final String GOOGLE_INSTRUCTIONS = "html_instructions";

    // RISK API Keys
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lon";
    public static final String TIME = "time";
    public static final String WAYPOINTS = "waypoints";
    public static final String KEY = "key";

    // REST Service Tags
    public static final String RISK_KEY_STATUS = "response_status_message";
    public static final String RISK_KEY_ROUTES = "routes";
    public static final String RISK_KEY_ROUTE_NAME = "route_name";
    public static final String RISK_KEY_ROUTE_RAW_SCORE = "route_risk_factor_raw_score";
    public static final String RISK_KEY_ROUTE_DISTANCE = "route_distance";
    public static final String RISK_KEY_ROUTE_DURATION = "route_duration";
    public static final String RISK_KEY_ROUTE_RISK = "route_risk_factor";

    // Location HashMap
    public static final String ROUTE = "route";
    public static final String ROUTE_NAME = "route_name";
    public static final String ROUTE_RISK_FACTOR_RAW_SCORE = "route_risk_factor_raw_score";
    public static final String ROUTE_DISTANCE = "route_distance";
    public static final String ROUTE_DURATION = "route_duration";
    public static final String ROUTE_RISK_FACTOR0 = "route_risk_factor0";
    public static final String ROUTE_SAFETY = "route_safety";

    public static final String UPDATE_APP = "update_app";
    public static final String UPDATE_APP_MANDATE = "update_app_mandate";
    static final String UPDATE_APP_MANDATE_TIME = "update_app_mandate_time";

    public static final String USER_EMAIL = "email_id";
    public static final String PASSWORD = "password";
    public static final String REGISTER_OLD_PASSWORD = "old_password";
    public static final String LOGIN_TYPE = "login_type_email";
    public static final String TOKEN = "token";
    public static final String DATE = "date";
    public static final String USER = "user";
    public static final String KEEP_ME_LOGGED_IN = "keep_me_logged_in";
    public static final String ACTIVATION_CODE = "activation_code";
    public static final String PAGER_VALUE0 = "pager_value0";
    public static final String PAGER_VALUE1 = "pager_value1";
    public static final String RESET_ID = "reset_id";
    public static final String RESET_SCREEN = "reset_password";
    public static final String RESET_SETTINGS = "reset_settings";
    public static final String EMAIL_TYPE = "email_type";

    // BackGround Tracking variables
//    public static final String INTENT_LOCATION_TIME = "intent_location_time";
//    public static final String INTENT_ACTIVITY_TIME = "intent_activity_time";
//    public static final String INTENT_LOCATION = "intent_location";
//    public static final String INTENT_ACTIVITY = "intent_activity";
//    public static final String INTENT_ACTIVITY_LIST = "intent_activity_list";

    // Routewise Service Broadcast Intents
    public static final String INTENT_GEOFENCE_EXITED = "intent_geofence_exited";
    public static final String INTENT_SYNC_DATA = "intent_sync_data";
    public static final String INTENT_LOCATION_RECIEVED = "intent_location_recieved";
    public static final String INTENT_CHECK_MOTION_SENSOR = "intent_check_motion_sensor";
    public static final String INTENT_ADD_GEOFENCE = "intent_add_geofence";
    public static final String INTENT_START_GPS_FULL = "intent_start_gps_full";
    public static final String INTENT_LOCATION = "intent_location";
    public static final String INTENT_INIT_MANAGER = "intent_init_manager";
    public static final String INTENT_INIT_MANAGER_OVERRIDE = "intent_init_manager_override";
    public static final String INTENT_INVOKE_TIMER = "intent_invoke_timer";
    public static final String INTENT_CHANGE_SUBSCRIBE = "intent_change_subscribe";
    public static final String INTENT_CHANGE_TRACKING = "intent_change_tracking";
    public static final String INTENT_CHANGE_AUTO_ENABLE = "intent_change_auto_enable";
//    public static final String INTENT_ = "intent_";
//    public static final String INTENT_ = "intent_";


    public static final String INTENT_UPLOAD_TYPE = "intent_upload_type";
    public static final String INTENT_HANDLER = "intent_handler";
    //    public static final String INTENT_TRACKING = "intent_tracking";
    public static final String INTENT_DATA_STATUS = "intent_data_status";
    public static final String INTENT_DATA_TYPE = "intent_data_type";

    //    public static final String DRIVING = "driving";
    // Intent Variables
    public static final String INTENT_START_ADDRESS = "intent_start_address";
    public static final String INTENT_END_ADDRESS = "intent_end_address";
    public static final String INTENT_START = "intent_start";
    public static final String INTENT_END = "intent_end";
    public static final String INTENT_START_TIME = "intent_start_time";
    public static final String INTENT_RISK_SCORE = "intent_risk_score";
//    public static final String INTENT_DISTANCE = "intent_distance";

    // Plan Trip variables
    public static final String INTENT_FROM_ADDRESS = "intent_from_address";
    public static final String INTENT_TO_ADDRESS = "intent_to_address";
    public static final String INTENT_DIRECTIONS = "intent_directions";
    public static final String INTENT_ROUTE_NAME = "intent_route_name";
    public static final String INTENT_DURATION = "intent_duration";
    public static final String INTENT_DISTANCE = "intent_distance";
    public static final String INTENT_NAVIGATION = "intent_navigation";

    public static final String RESET_BUFFER = "reset_buffer";
    //    public static final String TRACKING = "tracking";
    public static final String INSERT_END = "insert_end";
    public static final String INSERT_START = "insert_start";
    public static final String INSERT_DUMMY = "insert_dummy";
    public static final String SYNC_LAST_TRIP = "sync_last_trip";

    //    public static final String RECIEVER_DRIVING = "reciever_driving";
    public static final String RECIEVER_SERVICE = "reciever_service";
    public static final String RECIEVER_GEOFENCE = "reciever_geofence";

    public static final String RECEIVER_SERVICE_HANDLER = "receiver_service_handler";
    //    public static final String RECEIVER_APPLICATION = "receiver_application";
    //    public static final String RECEIVER_LOCATION = "receiver_location";
//    public static final String RECEIVER_SYNC = "receiver_sync";
    public static final String RECEIVER_UPLOAD_DATA_TYPE = "receiver_upload_data_type";
    //    public static final String RECEIVER_TRACKING = "receiver_tracking";
    public static final String RECEIVER_DATA_STATUS = "receiver_data_status";
//    public static final String RECEIVER_TRACKING_FOR_LOCATION = "receiver_tracking_for_location";

    public static final String FORGOT_TYPE = "forgot_type";
    public static final String EMAIL = "email_id";
    public static final String PSWRD = "password";
    public static final String USERNAME = "username";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String ADDRESS = "address";
    public static final String DEVICE_ID = "device_id";
    public static final String CHANGE_DEVICE_ID = "change_device_id";
    public static final String VEHICLE_MAKE = "vehicle_make";
    public static final String VEHICLE_MODEL = "vehicle_model";
    public static final String VEHICLE_YEAR = "vehicle_year";
    public static final String SUBMIT_REGISTRATION_DATA = "submit_registration_data";

    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String AGE = "age";

    public static final String INSURANCE_FIRST_NAME = "insurance_first_name";
    public static final String INSURANCE_LAST_NAME = "insurance_last_name";
    public static final String INSURANCE_AGE = "insurance_age";
    public static final String INSURANCE_PHONE_NUMBER = "insurance_phone_number";
    public static final String INSURANCE_EMAIL = "insurance_email_id";
    public static final String INSURANCE_ADDRESS = "insurance_address";
    public static final String HAS_APPLIED_BEFORE = "has_applied_before";

    public static final String MAIL_ACTIVATION = "activation";
    public static final String MAIL_FORGOT = "forgot";
    public static final String MAIL_REGISTER = "register";

    public static final String ACTIVATION = "activation";
    public static final String FORGOT_PASSWORD = "forgot_password";

    public static final String EMAIL_ID = "email_id";
    public static final String LOGIN_TYPE_EMAIL = "login_type_email";
    public static final String MAIL_TYPE = "mail_type";

    public static final int REST_RETRY = 0;
    public static final int REST_RETRY_MILLISECONDS = 15000;

    public static final String QUERY_VEHICLE_MAKE = "query_vehicle_make";
    public static final String QUERY_VEHICLE_MODEL = "query_vehicle_model";
    public static final String QUERY_UNSUBSCRIBE = "query_unsubscribe";
    public static final String INSURANCE_POST = "insurance_post";
    public static final String INSURANCE_GET = "insurance_get";
    public static final String TRIPS_LIST_GET = "trips_list_get";
    public static final String RISK_API_GET = "risk_api_get";
    public static final String GOOGLE_DIRECTION_GET = "google_direction_get";
    public static final String DETAILED_TRIP_GET = "detailed_trip_get";
    public static final String PASENGER_STATUS_POST = "passenger_status_post";

    // REST Service response keys
    public static final String ACCESS_TOKEN = "token";
    // Dashboard Service
    public static final String FROM_DATE = "from";
    public static final String TO_DATE = "to";
    public static final String RISK_SCORE = "risk_score";
    public static final String DRIVING_SCORE = "driving_score";
    public static final String TOTAL_DISTANCE = "total_distance";

    // REST Service Tags
    public static final String LOGIN = "login";
    public static final String PLAN = "plan";
    public static final String PING = "ping";
    public static final String SYNC = "sync";
    public static final String DASHBOARD = "dashboard";
    public static final String RESET = "reset";
    public static final String RESET_PASS = "reset_pass";
    public static final String RISK_API = "risk_api";
    public static final String FORGOT = "forgot";
    public static final String SUBSCRIBE_AGAIN = "subscribe";

    static final String PASSWORD_REGEX = "((?=.*\\d)(?=.*[A-Z])(?=.*[ !\"#$%&'()+,-.:;<=>?@[\\\\]^_`{|}~]).{8,})";
    static final String FULL_NAME_REGEX = "^[ A-z]+$";
    static final String PHONE_REGEX = "\\d{10}$";

    //tags
    public static final String USERNAME_TAG_KEY = "user_name_tag";
    public static final int USERNAME_TAG = 1;
    public static final int PASSWORD_TAG = 2;
    public static final int DRIVING_DATA_CONDITION_SWITCH_TAG = 3;
    public static final int SHARING_DATA_CONDITION_SWITCH_TAG = 4;
    public static final int INCURRED_CHARGES_CONDITION_SWITCH_TAG = 5;
    public static final int LIABILITY_CONDITION_SWITCH_TAG = 6;
    public static final int EMAIL_TAG = 7;
    public static final int CREATE_PASSWORD_TAG = 8;
    public static final int CONFIRM_PASSWORD_TAG = 9;
    public static final int FULL_NAME_TAG = 10;
    public static final int PHONE_NUMBER_TAG = 11;
    public static final int STREET_ADDRESS_TAG = 12;
    public static final int VEHICLE_MAKE_TAG = 13;
    public static final int VEHICLE_MODEL_TAG = 14;
    public static final int VEHICLE_YEAR_TAG = 15;
    public static final int NEXT_BUTTON_TAG = 16;
    public static final int PLACE_LIST_TAG = 17;
    public static final int VEHICLE_MAKE_PICKER_TAG = 18;
    public static final int VEHICLE_MODEL_PICKER_TAG = 19;
    public static final int VEHICLE_YEAR_PICKER_TAG = 20;
    public static final int BACK_ICON_TAG_REGISTRATION = 21;

    public static final int FIRST_NAME_FIELD_TAG = 22;
    public static final int LAST_NAME_FIELD_TAG = 23;
    public static final int AGE_FIELD_TAG = 24;
    public static final int PHONE_NUMBER_FIELD_TAG = 25;
    public static final int EMAIL_ADDRESS_FIELD_TAG = 26;
    public static final int GARAGING_ADDRESS_FIELD = 27;
    public static final int CANCEL_BUTTON_TAG = 28;
    public static final int SUBMIT_BUTTON_TAG = 29;
    public static final int AGE_PICKER_TAG = 30;
    public static final int BACK_ICON_TAG_INSURANCE = 31;

    public static final int BACK_ICON_TAG_DETAILED_TRIP_VIEW = 32;
    public static final int INFO_ICON_TAG_DETAILED_TRIP_VIEW = 33;

    public static final int DAYS_TAG = 34;
    public static final int HOURS_TAG = 35;
    public static final int MINUTES_TAG = 36;
    public static final int DAYS_PICKER_TAG = 37;
    public static final int HOURS_PICKER_TAG = 38;
    public static final int MINUTES_PICKER_TAG = 39;
    public static final int CELLULAR_RADIO_TAG = 40;
    public static final int WIFI_RADIO_TAG = 41;
    public static final int BOTH_RADIO_TAG = 42;
    public static final int RESET_PASSWORD_TAG = 43;
    public static final int SAVE_LOGS_TAG = 44;

    // Activity for result request code
    public static final int ADDRESS_REQUEST_CODE = 1;

    // Intent Arguments
    public static final String PAGER_VALUE = "pager_value";
    public static final String PAGE_NO = "page_no";

    //for displaying messages
    static final String RETRY = "Retry";
    public static final String BLANK_EMAIL = "blank_email";
    public static final String BLANK_PASSWORD = "blank_password";
    public static final String INVALID_EMAIL = "invalid_email";
    public static final String INVALID_PASSWORD = "invalid_password";
    public static final String VALID_EMAIL = "valid_email";
    public static final String VALID_PASSWORD = "valid_password";
    public static final String CLEAR_EMAIL_MESSAGE = "clear_email_message";
    public static final String CLEAR_PASSSWORD_MESSAGE = "clear_password_message";
    public static final String CONFIRM_PASSWORD_MATCHED = "confirm_password_matched";
    public static final String INVALID_CONFIRM_PASSWORD = "invalid_confirm_password";
    public static final String CONFIRM_PASSWORD_NOT_MATCHED = "confirm_password_not_matched";
    public static final String BLANK_CONFIRM_PASSWORD = "blank_confirm_password";
    public static final String CLEAR_CONFIRM_PASSWORD = "clear_confirm_password";
    public static final String INVALID_FULL_NAME = "invalid_full_name";
    public static final String VALID_FULL_NAME = "valid_full_name";
    public static final String BLANK_FULL_NAME = "blank_full_name";
    public static final String CLEAR_FULL_NAME = "clear_full_name";
    public static final String INVALID_PHONE_NUMBER = "invalid_phone_number";
    public static final String VALID_PHONE_NUMBER = "valid_phone_number";
    public static final String BLANK_PHONE_NUMBER = "blank_phone_number";
    public static final String CLEAR_PHONE_NUMBER = "clear_phone_number";
    public static final String INVALID_STREET_NAME = "invalid_street_name";
    public static final String VALID_STREET_NAME = "valid_street_name";
    public static final String BLANK_STREET_NAME = "blank_street_name";
    public static final String CLEAR_STREET_NAME = "clear_street_name";
    public static final String INVALID_VEHICLE_MAKE = "invalid_vehicle_make";
    public static final String VALID_VEHICLE_MAKE = "valid_vehicle_make";
    public static final String BLANK_VEHICLE_MAKE = "blank_vehicle_make";
    public static final String CLEAR_VEHICLE_MAKE = "clear_vehicle_make";
    public static final String INVALID_VEHICLE_MODEL = "invalid_vehicle_model";
    public static final String VALID_VEHICLE_MODEL = "valid_vehicle_model";
    public static final String BLANK_VEHICLE_MODEL = "blank_vehicle_model";
    public static final String CLEAR_VEHICLE_MODEL = "clear_vehicle_model";
    public static final String INVALID_VEHICLE_YEAR = "invalid_vehicle_year";
    public static final String VALID_VEHICLE_YEAR = "valid_vehicle_year";
    public static final String BLANK_VEHICLE_YEAR = "blank_vehicle_year";
    public static final String CLEAR_VEHICLE_YEAR = "clear_vehicle_year";

    public static final String BLANK_FIRST_NAME = "blank_first_name";
    public static final String INVALID_FIRST_NAME = "invalid_first_name";
    public static final String VALID_FIRST_NAME = "valid_first_name";
    public static final String CLEAR_FIRST_NAME = "clear_first_name";
    public static final String BLANK_LAST_NAME = "blank_last_name";
    public static final String INVALID_LAST_NAME = "invalid_last_name";
    public static final String VALID_LAST_NAME = "valid_last_name";
    public static final String CLEAR_LAST_NAME = "clear_last_name";
    public static final String BLANK_AGE = "blank_age";
    public static final String INVALID_AGE = "invalid_age";
    public static final String VALID_AGE = "valid_age";
    public static final String CLEAR_AGE = "clear_age";
    public static final String BLANK_PHONE_NO = "blank_phone_no";
    public static final String INVALID_PHONE_NO = "invalid_phone_no";
    public static final String VALID_PHONE_NO = "valid_phone_no";
    public static final String CLEAR_PHONE_NO = "clear_phone_no";
    public static final String BLANK_EMAIL_ADDRESS = "blank_email_address";
    public static final String INVALID_EMAIL_ADDRESS = "invalid_email_address";
    public static final String VALID_EMAIL_ADDRESS = "valid_email_address";
    public static final String CLEAR_EMAIL_ADDRESS = "clear_email_address";
    public static final String BLANK_GARAGING_ADDRESS = "blank_garaging_address";
    public static final String INVALID_GARAGING_ADDRESS = "invalid_garaging_address";
    public static final String VALID_GARAGING_ADDRESS = "valid_garaging_address";
    public static final String CLEAR_GARAGING_ADDRESS = "clear_garaging_address";
    public static final String QUERY_TYPE_ISURANCE_SEND_DATA = "query_type_insurance_send_data";
    public static final String QUERY_TYPE_ISURANCE_GET_DATA = "query_type_insurance_get_data";

    //miscellaneous
    public static final String PROGRESS_DIALOG_MSG = "Authenticating...";
    public static final String FAILED_TOAST_MSG = "Failed";
    public static final String PROCESSING_MSG = "Processing...";
    public static final String ZIPPING_MSG = "Compressing...";
    public static final String UPLOADING_MSG = "Uploading...";
    public static final String DELETING_MSG = "Deleting...";
    public static final String UNSUBSCRIBE_TOAST_MSG = "Unsubscribed";
    public static final String ACTIVATING_MSG = "Activating...";
    public static final String SENDING_CODE = "Sending Code...";
    public static final String NO_INTERNET_TOAST_MSG = "No Data Connection";
    public static final String UNABLE_TO_FETCH = "Unable to fetch";
    public static final String CONNECTION_TIMEOUT = "Connection timeout";
    public static final String LOADING = "Loading...";
    public static final String THANK_YOU_TOAST_MSG = "Thank You";
    public static final String USER_NOT_AVAILABLE = "User not available";
    public static final String ROUTE_NOT_AVAILABLE = "SafeRoute not available at this time";

    //Auto complete
    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String OUT_JSON = "/json";

    // Route Map Data
    public static final String MAP_PATH = "route_path";
    public static final String MAP_DISTANCE = "route_distance";
    public static final String MAP_TIME = "route_time";
    public static final String MAP_RISK = "route_risk";
    public static final String MAP_SAFE = "route_safe";
    public static final String MAP_URL = "route_url";
    public static final String MAP_INSTRUCTION = "route_instruction";
    public static final String MAP_DISTANCE_ARRAY = "route_dis_array";

    //Safe route
    public static final String SAFE_ROUTE_NAVIGATION_ARRAY = "safe_route_navigation_array";
    public static final String SAFE_ROUTE_DISTANCE_ARRAY = "safe_route_distance_array";
    public static final String SAFE_ROUTE_URL = "safe_route_url";
    public static final String SAFE_ROUTE_NAME = "safe_route_name";
    public static final String SAFE_ROUTE_RISK_FACTOR = "safe_route_risk_factor";
    public static final String SAFE_ROUTE_IS_SAFE = "safe_route_is_safe";
    public static final String SAFE_ROUTE_FINAL_DURATION = "safe_route_final_duration";
    public static final String SAFE_ROUTE_FINAL_DISTANCE = "safe_route_final_distance";
    public static final String SAFE_ROUTE_PATH_COORDINATES = "safe_route_path_coordinates";
    public static final String SAFE_ROUTE_DURATION_INTEGER = "safe_route_duartion_integer";

    //trip components
    public static final String PATH_ID = "path_id";
    public static final String START_TIME = "start_time";
    public static final String START_LOCATION = "start_location";
    public static final String END_LOCATION = "end_location";
    public static final String TRIP_LENGTH = "trip_length";
    public static final String TRIP_TIME = "trip_time";
    public static final String PASSENGER_RIDE = "passenger_ride";
    public static final String ROUTE_RISK_FACTOR = "route_risk_factor";
    public static final String MAP_THUMBNAIL_URL = "map_thumbnail_url";
    public static final String TRIPLIST = "trip_list";
    public static final String STOP_TIME = "stop_time";
    public static final String PASSENGER_STATUS = "status";

    //settings
    public static final String AUTO_ENABLE_TIME_START = "auto_enable_time_start";
    public static final String AUTO_ENABLE_TIME = "auto_enable_time";
    public static final String DATA_UPLOAD_CONDITION = "data_upload_condition";
    public static final String MOBILE = "Mobile";
    public static final String WIFI = "WIFI";
    public static final String ETHERNET = "Ethernet";
    public static final String BOTH = "Both";
    public static final String DATA_CONNECTION_TYPE = "data_connection_type";
    public static final String DATA_CONNECTION_STATUS = "data_connection_staus";
    public static final String NETWORK_RESPONSE_AVAILABLE = "network_response_available";
//    public static final String NETWORK_RESPONSE_AVAILABLE = "network_response_available";
//    public static final String NETWORK_RESPONSE_AVAILABLE = "network_response_available";
//    public static final String NETWORK_RESPONSE_AVAILABLE = "network_response_available";

    public static final String KEY_ZOOM_LEVEL = "Zoom_Level";

    //v3 Modifications
    public static final String LOC_MANAGER_TIME = "loc_manager_time";
    public static final String LAST_GEOFENCE_TIME = "last_geofence_time";
    public static final String LAST_DRIVING_TIME = "last_driving_time";
    public static final String TRACKING_ENABLED = "tracking_enabled";
    public static final String SUBSCRIBED = "subscribed";
    public static final String AUTO_TRACKING = "auto_tracking";
    public static final String LOCATION_PERMISSION = "location_permission";
    public static final String LAST_LOC_TIME = "last_loc_time";
    static final String LAST_SQLITE_FLUSH = "last_sqlite_flush";
    //    public static final String LAST_TRAFFIC_TIME = "last_traffic_time";
//    public static final String LAST_REGION_TIME = "last_region_time";
    public static final String GOT_RECENT_LOC = "got_recent_loc";
    public static final String PREVIOUS_LOCATION = "prev_loc";
    public static final String REGION_LOCATION = "region_loc";
    public static final String LAST_LOCATION = "prev_loc";
    public static final String INSERT_START_BUFFER = "insert_start_buffer";
    public static final String WAITING_ARRAY = "waiting_array";
    public static final String REGION_ARRAY = "region_array";
    public static final String IS_RECORDING_TRIP = "is_recording_trip";
    public static final String IS_RECORD_STOP_TIMER_SET = "is_record_stop_timer_set";

    //    Alaram Timer Details
    public static final String TRIP_STOP_TIMER = "trip_stop_timer";
    public static final int TRIP_STOP_TIMER_CODE = 1001;
    public static final String AUTO_TRACK = "auto_track";
    public static final int AUTO_TRACK_CODE = 1002;

    public static final int LOC_INTERVAL = 1;
    public static final int LOC_SPLIT_INTERVAL = 301;
    public static final double ACTIVITY_TIMER_INTERVAL = 100;
    public static final double LOC_UPLOAD_INTERVAL = 330;
    public static final double GEOFENCE_TIMER_INTERVAL = 30;


    /**
     * Here after created the constants by Ajith
     */
    public static final String SENSOR_ACTIVATE = "SENSOR LISTENER ACTIVATED...";


}
