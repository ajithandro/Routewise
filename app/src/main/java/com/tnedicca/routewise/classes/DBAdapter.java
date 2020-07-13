package com.tnedicca.routewise.classes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.tnedicca.routewise.sensorsmodel.ActivityModel;
import com.tnedicca.routewise.sensorsmodel.BasicModel;
import com.tnedicca.routewise.sensorsmodel.Light_details;
import com.tnedicca.routewise.sensorsmodel.ScreenStatus_model;

import java.io.File;
import java.util.Collections;

@SuppressLint({"SdCardPath", "NewApi", "SimpleDateFormat"})
@SuppressWarnings("static-access")
public class DBAdapter {

    private final Context context;
    private final RouteLog logger;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    DBAdapter(Context ctx, String user) {
        context = ctx;
        logger = new RouteLog();
        logger.setLoggerClass(DBAdapter.class);
        DBHelper = new DatabaseHelper(context, user);
    }

    // ---opens the database---
    DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    @SuppressLint("StaticFieldLeak")
    void insertAccelerometer(BasicModel basicModel) {
        new AsyncTask<BasicModel,Void,String>(){
            @Override
            protected String doInBackground(BasicModel... basicModels) {
                 String status;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppConstant.KEY_X, basicModel.getX());
                    contentValues.put(AppConstant.KEY_Y, basicModel.getY());
                    contentValues.put(AppConstant.KEY_Z, basicModel.getZ());
                    contentValues.put(AppConstant.KEY_ACCURACY,basicModel.getAccuracy());
                    contentValues.put(AppConstant.KEY_SENSORTIME,basicModel.getSensortime());
                    contentValues.put(AppConstant.KEY_TIME, basicModel.getTime());
                    long id = db.insertWithOnConflict(AppConstant.DB_TABLE_ACCELEROMETER, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id < 1) {
                        logger.info("Conflict in Insert : " + contentValues);

                    }
                    status = "Accelerometer data inserted...!";
                } catch (Exception e) {
                    status = "Accelerometer data not inserted...!";
                    logger.info(status  + e.getMessage());

                }
                return status;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                logger.info(s);
                db.close();
            }
        }.execute(basicModel);

    }


    @SuppressLint("StaticFieldLeak")
    void insertmagnaticfields(BasicModel basicModel) {
        new AsyncTask<BasicModel,Void,String>(){
            @Override
            protected String doInBackground(BasicModel... basicModels) {
                String status;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppConstant.KEY_X, basicModel.getX());
                    contentValues.put(AppConstant.KEY_Y, basicModel.getY());
                    contentValues.put(AppConstant.KEY_Z, basicModel.getZ());
                    contentValues.put(AppConstant.KEY_ACCURACY,basicModel.getAccuracy());
                    contentValues.put(AppConstant.KEY_SENSORTIME,basicModel.getSensortime());
                    contentValues.put(AppConstant.KEY_TIME, basicModel.getTime());
                    long id = db.insertWithOnConflict(AppConstant.DB_TABLE_MEGNATICFIELDS, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id < 1) {
                        logger.info("Conflict in Insert : " + contentValues);

                    }
                    status = "Magnetic_fields data inserted...!";
                } catch (Exception e) {
                    status = "Magnetic_fields data not inserted...!";
                    logger.info(status  + e.getMessage());

                }

                return status;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                logger.info(s);
                db.close();
            }
        }.execute(basicModel);

    }


    @SuppressLint("StaticFieldLeak")
    void insertgravityrecords(BasicModel basicModel) {
        new AsyncTask<BasicModel,Void,String>(){

            @Override
            protected String doInBackground(BasicModel... basicModels) {
                String status;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppConstant.KEY_X, basicModel.getX());
                    contentValues.put(AppConstant.KEY_Y, basicModel.getY());
                    contentValues.put(AppConstant.KEY_Z, basicModel.getZ());
                    contentValues.put(AppConstant.KEY_ACCURACY,basicModel.getAccuracy());
                    contentValues.put(AppConstant.KEY_SENSORTIME,basicModel.getSensortime());
                    contentValues.put(AppConstant.KEY_TIME, basicModel.getTime());
                    long id = db.insertWithOnConflict(AppConstant.DB_TABLE_GRAVITY, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id < 1) {
                        logger.info("Conflict in Insert : " + contentValues);
                    }
                    status = "Gravity data inserted...! ";
                } catch (Exception e) {
                    status = "Gravity data not inserted...! ";
                    logger.info(status  + e.getMessage());
                }

                return status;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                logger.info(s);
                db.close();
            }
        }.execute(basicModel);

    }

    @SuppressLint("StaticFieldLeak")
    void insertlightsensor(Light_details details) {
        new AsyncTask<Light_details,Void,String>(){

            @Override
            protected String doInBackground(Light_details... light_details) {
                String status;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppConstant.KEY_LIGHT, details.getLight());
                    contentValues.put(AppConstant.KEY_SENSORTIME,details.getSensortime());
                    contentValues.put(AppConstant.KEY_TIME, details.getTimestamp());
                    long id = db.insertWithOnConflict(AppConstant.DB_TABLE_LIGHT, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id < 1) {
                        logger.info("Conflict in Insert : " + contentValues);
                    }
                    status = "LightSensor data inserted...! ";
                } catch (Exception e) {
                    status = "LightSensor data not inserted...! ";
                    logger.info(status  + e.getMessage());
                }

                return status;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                logger.info(s);
                db.close();
            }
        }.execute(details);


    }
    @SuppressLint("StaticFieldLeak")
    void  insertgyroscope(BasicModel basicModel){
        new AsyncTask<BasicModel, Void, String>() {
            @Override
            protected String doInBackground(BasicModel... basicModels) {
                String status;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppConstant.KEY_X, basicModel.getX());
                    contentValues.put(AppConstant.KEY_Y, basicModel.getY());
                    contentValues.put(AppConstant.KEY_Z, basicModel.getZ());
                    contentValues.put(AppConstant.KEY_ACCURACY,basicModel.getAccuracy());
                    contentValues.put(AppConstant.KEY_SENSORTIME,basicModel.getSensortime());
                    contentValues.put(AppConstant.KEY_TIME, basicModel.getTime());
                    long id = db.insertWithOnConflict(AppConstant.DB_TABLE_GYROSCOPE, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id < 1) {
                        logger.info("Conflict in Insert : " + contentValues);
                    }
                    status = "Gyroscope data inserted...! ";
                } catch (Exception e) {
                    status = "Gyroscope data not inserted...! ";
                    logger.info(status  + e.getMessage());
                }

                return status;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                logger.info(s);
                db.close();
            }
        }.execute(basicModel);

    }

    @SuppressLint("StaticFieldLeak")
    void  insertproximity(BasicModel basicModel){
//        new AsyncTask<BasicModel, Void, String>() {
//            @Override
//            protected String doInBackground(BasicModel... basicModels) {
                String status;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppConstant.KEY_X, basicModel.getX());
                    contentValues.put(AppConstant.KEY_Y, basicModel.getY());
                    contentValues.put(AppConstant.KEY_Z, basicModel.getZ());
                    contentValues.put(AppConstant.KEY_ACCURACY,basicModel.getAccuracy());
                    contentValues.put(AppConstant.KEY_SENSORTIME,basicModel.getSensortime());
                    contentValues.put(AppConstant.KEY_TIME, basicModel.getTime());
                    long id = db.insertWithOnConflict(AppConstant.DB_TABLE_PROXIMITY, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id < 1) {
                        logger.info("Conflict in Insert : " + contentValues);
                    }
                    status = "Proximity data inserted...! ";
                    logger.info(status);
                } catch (Exception e) {
                    status = "Proximity data not inserted...! ";
                    logger.info(status  + e.getMessage());
                }

//                return status;
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//                logger.info(s);
//                db.close();
//            }
//        }.execute(basicModel);

    }
    @SuppressLint("StaticFieldLeak")
    void insertactivitydetails(ActivityModel activityModel){
        new AsyncTask<ActivityModel, Void, String>() {
            @Override
            protected String doInBackground(ActivityModel... activityModels) {
                String status;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppConstant.KEY_ACT_TYPE, activityModel.getAct_type());
                    contentValues.put(AppConstant.KEY_CONFIDENCE, activityModel.getConfidence());
                    contentValues.put(AppConstant.KEY_ISDRIVING, activityModel.isIs_driving());
                    contentValues.put(AppConstant.KEY_TIME, activityModel.getTime());
                    long id = db.insertWithOnConflict(AppConstant.DB_TABLE_ACTIVITY, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id < 1) {
                        logger.info("Conflict in Insert : " + contentValues);
                    }
                    status = "ActivityDetails datas inserted...!";
                } catch (Exception e) {
                    status = "Error in insert : ActivityDetails ! ";
                    logger.info(status  + activityModel.toString());

                }

                return status;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                logger.info(s);
                db.close();
            }
        }.execute(activityModel);
    }
    @SuppressLint("StaticFieldLeak")
    void insertscreenstatus(ScreenStatus_model screenStatus){
                String status;
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AppConstant.KEY_SCREEN_STATUS,screenStatus.isScreen_status());
                    contentValues.put(AppConstant.KEY_TIME, screenStatus.getTimestamp());
                    long id = db.insert(AppConstant.DB_TABLE_SCREEN, null, contentValues);
                    if (id < 1) {
                        logger.info("Conflict in Insert : " + contentValues);
                    }
                    status = "Screenstatus  datas inserted...!";
                    logger.info(status);
                } catch (Exception e) {
                    status = "Error in insert : Screenstatus ! ";
                    logger.info(status  + e);
                }

    }


    void insertLocation(LocationDetails location) {
        try {
            ContentValues Co_ordinates = new ContentValues();
            Co_ordinates.put(AppConstant.KEY_LATITUDE, location.getLatitude());
            Co_ordinates.put(AppConstant.KEY_LONGITUDE, location.getLongitude());
//        Co_ordinates.put(AppConstant.KEY_TIME, 1589353914);
            Co_ordinates.put(AppConstant.KEY_TIME, location.getTime());
            Co_ordinates.put(AppConstant.KEY_SPEED, location.getSpeed());
            Co_ordinates.put(AppConstant.KEY_ALTITUDE, location.getAltitude());
            Co_ordinates.put(AppConstant.KEY_BEARING, location.getBearing());
            Co_ordinates.put(AppConstant.KEY_ACCURACY, location.getAccuracy());
            Co_ordinates.put(AppConstant.KEY_UPLOADED, false);
            long id = db.insertWithOnConflict(AppConstant.DB_TABLE_LOCATION, null, Co_ordinates, SQLiteDatabase.CONFLICT_IGNORE);
            if (id < 1) {
                logger.info("Conflict in Insert : " + Co_ordinates);
            }
        } catch (Exception e) {
            logger.info("Error in insertLocation : " + location.toString());
            logger.info("Error in insertLocation", e);
        }
        db.close();
    }

    Cursor getLastLocationData() {
        try {
            return db.query(AppConstant.DB_TABLE_LOCATION, new String[]{AppConstant.KEY_LATITUDE, AppConstant.KEY_LONGITUDE, AppConstant.KEY_TIME, AppConstant.KEY_SPEED, AppConstant.KEY_ALTITUDE, AppConstant.KEY_BEARING, AppConstant.KEY_ACCURACY}, null, null, null, null, AppConstant.KEY_TIME + " DESC", "2");
        } catch (Exception e) {
            logger.info("Error in getLastLocationData", e);
        }
        return null;
    }

    Cursor getValues() {
        try {
            return db.query(AppConstant.DB_TABLE_LOCATION, new String[]{AppConstant.KEY_ROWID, AppConstant.KEY_LATITUDE, AppConstant.KEY_LONGITUDE, AppConstant.KEY_TIME, AppConstant.KEY_SPEED, AppConstant.KEY_ALTITUDE, AppConstant.KEY_BEARING, AppConstant.KEY_ACCURACY, AppConstant.KEY_UPLOADED}, AppConstant.KEY_UPLOADED + " = ?", new String[]{"0"}, null, null, null);
        } catch (Exception e) {
            logger.info("Error in getValues", e);
        }
        return null;
    }

//    Cursor getsenceddatas(){
//        try {
//            return db.query(AppConstant.DB_TABLE_ACCELEROMETER, new String[]{AppConstant.KEY_ROWID, AppConstant.KEY_ACCELERO_X, AppConstant.KEY_ACCELERO_Y, AppConstant.KEY_ACCELERO_Z, AppConstant.KEY_TIMESTAMP}, AppConstant.KEY_ROWID + " = ?", new String[]{"5"}, null, null, null);
//        } catch (Exception e) {
//            logger.info("Error in getValues", e);
//        }
//        return null;
//    }


    void setUploadedFlag(String[] ids) {
        try {
            ContentValues args = new ContentValues();
            args.put(AppConstant.KEY_UPLOADED, true);
            int id = db.update(AppConstant.DB_TABLE_LOCATION, args, AppConstant.KEY_ROWID + " IN (" + TextUtils.join(",", Collections.nCopies(ids.length, "?")) + ")", ids);
            if (id < 1) {
                logger.info("Conflict in setUploadedFlag : " + ids.toString());
            }
        } catch (Exception e) {
            logger.info("Error in setUploadedFlag : " + ids.toString());
            logger.info("Error in setUploadedFlag", e);
        }
        db.close();
    }

    void deleteData(String weekBack) {
        try {
            int id = db.delete(AppConstant.DB_TABLE_LOCATION, AppConstant.KEY_UPLOADED + " = 0 AND " + AppConstant.KEY_TIME + " < ?", new String[]{weekBack});
            if (id < 0) {
                logger.info("Conflict in setUploadedFlag : " + weekBack);
            }
        } catch (Exception e) {
            logger.info("Error in deleteData : " + weekBack);
            logger.info("Error in deleteData", e);
        }
        db.close();
    }

//    public void deleteRange(String ids, int limit) {
//        ContentValues args = new ContentValues();
//        args.put(AppConstant.KEY_UPLOADED, "1");
//        db.update(AppConstant.DB_TABLE_LOCATION, args, AppConstant.KEY_ROWID + " IN (?)", new String[]{ids});
////        String sql = "DELETE FROM " + AppConstant.DB_TABLE_LOCATION + " WHERE \"" + AppConstant.KEY_ROWID + "\" IN (SELECT \"" + AppConstant.KEY_ROWID + "\" FROM " + AppConstant.DB_TABLE_LOCATION + " limit " + limit + ")";
////        String sql = "UPDATE " + AppConstant.DB_TABLE_LOCATION + " SET " + AppConstant.KEY_UPLOADED + " = 1 WHERE " + AppConstant.KEY_ROWID + " IN (" + ids + ")";
////        execQuery(sql);
//    }

//    // ---delete the database---
//    public DBAdapter delete(String user, String path, String database) throws SQLException {
//        String destPath = AppConstant.KEY_DB_STORAGE_PATH + "/" + user + "/" + database;
//        File f = new File(destPath);
//        db.deleteDatabase(f);
//        // db.delete(destPath, null, null);
//        return this;
//    }

    // ---closes the database---
    void close() {
        DBHelper.close();
        db.close();
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context, String user) {
            super(new DatabaseContext(context), user, null, AppConstant.DEFAULT_DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                /* Create a Table in the Database. */
                db.execSQL(AppConstant.CREATE_TABLE);
                db.execSQL(AppConstant.CREATE_TABLE_ACTIVITY);
                db.execSQL(AppConstant.CREATE_TABLE_ACCELEROMETER);
                db.execSQL(AppConstant.CREATE_TABLE_GRAVITY);
                db.execSQL(AppConstant.CREATE_TABLE_LIGHT);
                db.execSQL(AppConstant.CREATE_TABLE_GYROSCOPE);
                db.execSQL(AppConstant.CREATE_TABLE_SCREEN_STATUS);
                db.execSQL(AppConstant.CREATE_TABLE_MAGNATIC_FIELDS);
                db.execSQL(AppConstant.CREATE_TABLE_PROXIMITY);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Log.w(AppConstant.DEFAULT_DB_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }

    class DatabaseContext extends ContextWrapper {

        public DatabaseContext(Context base) {
            super(base);
        }

        @Override
        public File getDatabasePath(String user) {
            try {
                String destPath = AppConstant.KEY_DB_STORAGE_PATH + "/" + user;
                File file = new File(destPath);
                if (!file.exists()) {
                    boolean is_created = file.mkdirs();
                }
                String destFile = destPath + "/" + AppConstant.DEFAULT_DB_NAME + AppConstant.DB_EXTENTION_NEW;
                file = new File(destFile);
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /* this version is called for android devices >= api-11. thank to @damccull for fixing this. */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
            return openOrCreateDatabase(name, mode, factory);
        }

        /* this version is called for android devices < api-11 */
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
            SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
            // SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);
            return result;
        }
    }
}
