package com.huawei.displayengine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.ArrayMap;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;

public class DisplayEngineDbManager {
    private static final int BASE_ONE = 1;
    private static final int DB_RECORD_TRUE = 1;
    private static final int INVALID_NUM = -1;
    private static final int ONE_RECORD = 1;
    private static final String TAG = "DE J DisplayEngineDbManager";
    private static volatile DisplayEngineDbManager sInstance = null;
    private static final Object sLock = new Object();
    private SQLiteDatabase mDatabase = null;
    private final Object mDbLock = new Object();
    private DisplayEngineDbHelper mHelper;
    private final ArrayMap<String, TableProcessor> mTableProcessors = new ArrayMap<>();

    public static class AlgorithmEscwKey {
        public static final String ESCW = "ESCW";
        public static final String TAG = "AlgorithmESCW";
        public static final String USER_ID = "UserID";
    }

    public static class BrightnessCurveKey {
        public static final String AL = "AmbientLight";
        public static final String BL = "BackLight";
        public static final String USER_ID = "UserID";

        public static class Default {
            public static final String TAG = "BrightnessCurveDefault";
        }

        public static class High {
            public static final String TAG = "BrightnessCurveHigh";
        }

        public static class Low {
            public static final String TAG = "BrightnessCurveLow";
        }

        public static class Middle {
            public static final String TAG = "BrightnessCurveMiddle";
        }
    }

    public static class DataCleanerKey {
        public static final String RANGE_FLAG = "RangeFlag";
        public static final String TAG = "DataCleaner";
        public static final String TIMESTAMP = "TimeStamp";
        public static final String USER_ID = "UserID";
    }

    public static class DragInformationKey {
        public static final String AL = "AmbientLight";
        public static final String APP_TYPE = "AppType";
        public static final String GAME_STATE = "GameState";
        public static final String ID = "_ID";
        public static final String PACKAGE = "PackageName";
        public static final String PRIORITY = "Priority";
        public static final String PROXIMITY_POSITIVE = "ProximityPositive";
        public static final String START_POINT = "StartPoint";
        public static final String STOP_POINT = "StopPoint";
        public static final String TAG = "DragInfo";
        public static final String TIMESTAMP = "TimeStamp";
        public static final String USER_ID = "UserID";
    }

    public static class QueryInfoKey {
        public static final String NUMBER_LIMIT = "NumberLimit";
    }

    public static class UserPreferencesKey {
        public static final String AL = "AmbientLight";
        public static final String APP_TYPE = "AppType";
        public static final String DELTA = "BackLightDelta";
        public static final String TAG = "UserPref";
        public static final String USER_ID = "UserID";
    }

    private DisplayEngineDbManager(Context context) {
        this.mTableProcessors.put(DragInformationKey.TAG, new DragInformationTableProcessor());
        this.mTableProcessors.put(UserPreferencesKey.TAG, new UserPreferencesTableProcessor());
        this.mTableProcessors.put("BrightnessCurveLow", new BrightnessCurveTableProcessor("BrightnessCurveLow"));
        this.mTableProcessors.put("BrightnessCurveMiddle", new BrightnessCurveTableProcessor("BrightnessCurveMiddle"));
        this.mTableProcessors.put("BrightnessCurveHigh", new BrightnessCurveTableProcessor("BrightnessCurveHigh"));
        this.mTableProcessors.put("BrightnessCurveDefault", new BrightnessCurveTableProcessor("BrightnessCurveDefault"));
        this.mTableProcessors.put("AlgorithmESCW", new AlgorithmEscwTableProcessor());
        this.mTableProcessors.put("DataCleaner", new DataCleanerTableProcessor());
        this.mHelper = new DisplayEngineDbHelper(context);
        openDatabase();
    }

    public static DisplayEngineDbManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new DisplayEngineDbManager(context);
                }
            }
        }
        return sInstance;
    }

    public boolean setMaxSize(String name, int size) {
        TableProcessor processor = this.mTableProcessors.get(name);
        if (processor != null && size > 0) {
            return processor.setMaxSize(size);
        }
        DeLog.e(TAG, "Invalid input for setMaxSize(" + name + ") size=" + size);
        return false;
    }

    public boolean addOrUpdateRecord(String name, Bundle data) {
        TableProcessor processor = this.mTableProcessors.get(name);
        if (processor != null && data != null) {
            return processor.addOrUpdateRecord(data);
        }
        DeLog.e(TAG, "Invalid input for addOrUpdateRecord:" + name + " is not support or data is null!");
        return false;
    }

    public int getSize(String name, Bundle info) {
        TableProcessor processor = this.mTableProcessors.get(name);
        if (processor != null) {
            return processor.getSize(info);
        }
        DeLog.e(TAG, "Invalid input for getSize:" + name + " is not support!");
        return 0;
    }

    public int getSize(String name) {
        return getSize(name, null);
    }

    public ArrayList<Bundle> getAllRecords(String name, Bundle info) {
        TableProcessor processor = this.mTableProcessors.get(name);
        if (processor != null) {
            return processor.getAllRecords(info);
        }
        DeLog.e(TAG, "Invalid input for getAllRecords:" + name + " is not support!");
        return new ArrayList<>();
    }

    public ArrayList<Bundle> getAllRecords(String name) {
        return getAllRecords(name, null);
    }

    private void openDatabase() {
        try {
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            DeLog.e(TAG, "Failed to open DisplayEngine.db error:" + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkDatabaseStatusIsOk() {
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        if (sQLiteDatabase == null || !sQLiteDatabase.isOpen()) {
            openDatabase();
        }
        SQLiteDatabase sQLiteDatabase2 = this.mDatabase;
        return sQLiteDatabase2 != null && sQLiteDatabase2.isOpen();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeCursur(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    /* access modifiers changed from: private */
    public class TableProcessor {
        protected int mMaxSize = 0;

        public TableProcessor() {
        }

        public boolean setMaxSize(int size) {
            return false;
        }

        public boolean addOrUpdateRecord(Bundle data) {
            return false;
        }

        public ArrayList<Bundle> getAllRecords(Bundle info) {
            if (info == null) {
                return getAllRecords();
            }
            return new ArrayList<>();
        }

        public int getSize(Bundle info) {
            int size;
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (info == null) {
                    size = getSizeWithoutLock();
                } else {
                    size = getSizeWithoutLock(info);
                }
            }
            return size;
        }

        /* access modifiers changed from: protected */
        public ArrayList<Bundle> getAllRecords() {
            return new ArrayList<>();
        }

        /* access modifiers changed from: protected */
        public int getSizeWithoutLock() {
            return 0;
        }

        /* access modifiers changed from: protected */
        public int getSizeWithoutLock(Bundle info) {
            return 0;
        }
    }

    private class DragInformationTableProcessor extends TableProcessor {
        public DragInformationTableProcessor() {
            super();
        }

        private boolean verifyDbSizeInternal(Cursor cursor, int size) {
            Bundle info = new Bundle();
            info.putInt("UserID", cursor.getInt(cursor.getColumnIndex("USERID")));
            int realSize = getSizeWithoutLock(info);
            if (realSize <= size || deleteRecordsWithoutLock(info, realSize - size)) {
                return true;
            }
            return false;
        }

        private boolean verifyDbSize(int size) {
            boolean ret = true;
            Cursor cursor = null;
            try {
                cursor = DisplayEngineDbManager.this.mDatabase.rawQuery("SELECT DISTINCT USERID FROM UserDragInformation", null);
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        ret = verifyDbSizeInternal(cursor, size);
                    }
                }
            } catch (SQLException e) {
                ret = false;
                DeLog.w(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.setMaxSize() failed to get all the user IDs, error:" + e.getMessage());
            } catch (Throwable th) {
                DisplayEngineDbManager.this.closeCursur(null);
                throw th;
            }
            DisplayEngineDbManager.this.closeCursur(cursor);
            return ret;
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public boolean setMaxSize(int size) {
            if (size > 0) {
                synchronized (DisplayEngineDbManager.this.mDbLock) {
                    if (!DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                        DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.setMaxSize() mDatabase error!");
                        return false;
                    }
                    boolean ret = verifyDbSize(size);
                    this.mMaxSize = size;
                    DeLog.i(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.setMaxSize(" + this.mMaxSize + ") success.");
                    return ret;
                }
            }
            DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.setMaxSize() invalid input: size=" + size);
            return false;
        }

        private boolean validateForAddOrUpdateRecord(Bundle data, int userId, int appType, long time) {
            int realSize;
            if (userId < 0 || appType < 0 || time <= 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.addOrUpdateRecord invalid input: time=" + time + " userId=" + userId + " appType=" + appType);
                return false;
            } else if (this.mMaxSize <= 0 || (realSize = getSizeWithoutLock(data)) < this.mMaxSize || deleteRecordsWithoutLock(data, (realSize - this.mMaxSize) + 1)) {
                return true;
            } else {
                return false;
            }
        }

        /* access modifiers changed from: private */
        public class DragInfo {
            public int al = 0;
            public int appType = 0;
            public boolean cover = true;
            public int gameState = 0;
            public String pkgName = BuildConfig.FLAVOR;
            public int priority = 0;
            public float start = 0.0f;
            public float stop = 0.0f;
            public long time = 0;
            public int userId = 0;

            public DragInfo(Bundle data) {
                if (data != null) {
                    this.time = data.getLong("TimeStamp");
                    this.priority = data.getInt(DragInformationKey.PRIORITY);
                    this.start = data.getFloat(DragInformationKey.START_POINT);
                    this.stop = data.getFloat(DragInformationKey.STOP_POINT);
                    this.al = data.getInt("AmbientLight");
                    this.cover = data.getBoolean(DragInformationKey.PROXIMITY_POSITIVE);
                    this.userId = data.getInt("UserID");
                    this.appType = data.getInt("AppType");
                    this.gameState = data.getInt(DragInformationKey.GAME_STATE);
                    this.pkgName = data.getString(DragInformationKey.PACKAGE);
                }
            }
        }

        private boolean updateRecoredLogPrint(Bundle data, int rows) {
            DragInfo info = new DragInfo(data);
            if (rows == 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor update failed: time=" + info.time + ",priority=" + info.priority + ",start=" + info.start + ",stop=" + info.stop + ",al=" + info.al + ",proximitypositive=" + info.cover + ",userId=" + info.userId + ",appType=" + info.appType + ",gameState=" + info.gameState + ",pkgName=" + info.pkgName);
                return false;
            }
            DeLog.i(DisplayEngineDbManager.TAG, "DragInformationTableProcessor update succ: rows=" + rows + " time=" + info.time + ",priority=" + info.priority + ",start=" + info.start + ",stop=" + info.stop + ",al=" + info.al + ",proximitypositive=" + info.cover + ",userId=" + info.userId + ",appType=" + info.appType + ",gameState=" + info.gameState + ",pkgName=" + info.pkgName);
            return true;
        }

        private boolean addRecordLogPrint(Bundle data, long rowId) {
            DragInfo info = new DragInfo(data);
            if (rowId == -1) {
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor insert failed: time=" + info.time + ",priority=" + info.priority + ",start=" + info.start + ",stop=" + info.stop + ",al=" + info.al + ",proximitypositive=" + info.cover + ",userId=" + info.userId + ",appType=" + info.appType + ",gameState=" + info.gameState + ",pkgName=" + info.pkgName);
                return false;
            }
            DeLog.i(DisplayEngineDbManager.TAG, "DragInformationTableProcessor add a record(" + rowId + ") succ: time=" + info.time + ",priority=" + info.priority + ",start=" + info.start + ",stop=" + info.stop + ",al=" + info.al + ",proximitypositive=" + info.cover + ",userId=" + info.userId + ",appType=" + info.appType + ",gameState=" + info.gameState + ",pkgName=" + info.pkgName);
            return true;
        }

        private boolean addOrUpdateTableRecord(Bundle data) {
            boolean ret;
            DragInfo info = new DragInfo(data);
            Cursor cursor = null;
            try {
                cursor = DisplayEngineDbManager.this.mDatabase.rawQuery("SELECT * FROM UserDragInformation where TIMESTAMP = ?", new String[]{String.valueOf(info.time)});
                ContentValues values = new ContentValues();
                values.put("TIMESTAMP", Long.valueOf(info.time));
                values.put("PRIORITY", Integer.valueOf(info.priority));
                values.put("STARTPOINT", Float.valueOf(info.start));
                values.put("STOPPOINT", Float.valueOf(info.stop));
                values.put("AL", Integer.valueOf(info.al));
                values.put("PROXIMITYPOSITIVE", Integer.valueOf(info.cover ? 1 : 0));
                values.put("USERID", Integer.valueOf(info.userId));
                values.put("APPTYPE", Integer.valueOf(info.appType));
                values.put("GAMESTATE", Integer.valueOf(info.gameState));
                values.put("PACKAGE", info.pkgName);
                if (cursor == null || cursor.getCount() <= 0) {
                    ret = addRecordLogPrint(data, DisplayEngineDbManager.this.mDatabase.insert(DisplayEngineDbHelper.TABLE_NAME_DRAG_INFORMATION, null, values));
                } else {
                    ret = updateRecoredLogPrint(data, DisplayEngineDbManager.this.mDatabase.update(DisplayEngineDbHelper.TABLE_NAME_DRAG_INFORMATION, values, "TIMESTAMP = ?", new String[]{String.valueOf(info.time)}));
                }
            } catch (SQLException e) {
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor add a record time=" + info.time + ",start=" + info.start + ",stop=" + info.stop + ",al=" + info.al + ",proximitypositive=" + info.cover + ",userId=" + info.userId + ",appType=" + info.appType + ",gameState=" + info.gameState + ",pkgName=" + info.pkgName + ", error:" + e.getMessage());
                ret = false;
            } catch (Throwable th) {
                DisplayEngineDbManager.this.closeCursur(null);
                throw th;
            }
            DisplayEngineDbManager.this.closeCursur(cursor);
            return ret;
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public boolean addOrUpdateRecord(Bundle data) {
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    if (data != null) {
                        if (!validateForAddOrUpdateRecord(data, data.getInt("UserID"), data.getInt("AppType"), data.getLong("TimeStamp"))) {
                            return false;
                        }
                        return addOrUpdateTableRecord(data);
                    }
                }
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.addOrUpdateRecord() mDatabase error or Invalid input!");
                return false;
            }
        }

        private Bundle cursorToRecord(Cursor cursor) {
            Bundle record = new Bundle();
            record.putLong(DragInformationKey.ID, cursor.getLong(cursor.getColumnIndex("_id")));
            record.putLong("TimeStamp", cursor.getLong(cursor.getColumnIndex("TIMESTAMP")));
            record.putInt(DragInformationKey.PRIORITY, cursor.getInt(cursor.getColumnIndex("PRIORITY")));
            record.putFloat(DragInformationKey.START_POINT, cursor.getFloat(cursor.getColumnIndex("STARTPOINT")));
            record.putFloat(DragInformationKey.STOP_POINT, cursor.getFloat(cursor.getColumnIndex("STOPPOINT")));
            record.putInt("AmbientLight", cursor.getInt(cursor.getColumnIndex("AL")));
            boolean z = true;
            if (cursor.getInt(cursor.getColumnIndex("PROXIMITYPOSITIVE")) != 1) {
                z = false;
            }
            record.putBoolean(DragInformationKey.PROXIMITY_POSITIVE, z);
            record.putInt("UserID", cursor.getInt(cursor.getColumnIndex("USERID")));
            record.putInt("AppType", cursor.getInt(cursor.getColumnIndex("APPTYPE")));
            record.putInt(DragInformationKey.GAME_STATE, cursor.getInt(cursor.getColumnIndex("GAMESTATE")));
            record.putString(DragInformationKey.PACKAGE, cursor.getString(cursor.getColumnIndex("PACKAGE")));
            return record;
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public ArrayList<Bundle> getAllRecords(Bundle info) {
            DisplayEngineDbManager displayEngineDbManager;
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    if (info != null) {
                        int userId = info.getInt("UserID");
                        if (userId < 0) {
                            DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.getAllRecords() invalid input: userId=" + userId);
                            return records;
                        }
                        int numLimit = info.getInt(QueryInfoKey.NUMBER_LIMIT, -1);
                        Cursor cursor = null;
                        if (numLimit < 1) {
                            try {
                                cursor = DisplayEngineDbManager.this.mDatabase.rawQuery("SELECT * FROM UserDragInformation where USERID = ? ORDER BY _id ASC", new String[]{String.valueOf(userId)});
                            } catch (SQLException e) {
                                records = null;
                                DeLog.w(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.getAllRecords() error:" + e.getMessage());
                                displayEngineDbManager = DisplayEngineDbManager.this;
                            } catch (Throwable th) {
                                DisplayEngineDbManager.this.closeCursur(null);
                                throw th;
                            }
                        } else {
                            SQLiteDatabase sQLiteDatabase = DisplayEngineDbManager.this.mDatabase;
                            cursor = sQLiteDatabase.rawQuery("SELECT * FROM UserDragInformation where USERID = ? ORDER BY _id DESC LIMIT " + numLimit, new String[]{String.valueOf(userId)});
                        }
                        if (cursor == null) {
                            DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.getAllRecords() query database error.");
                            DisplayEngineDbManager.this.closeCursur(cursor);
                            return records;
                        }
                        while (cursor.moveToNext()) {
                            records.add(cursorToRecord(cursor));
                        }
                        displayEngineDbManager = DisplayEngineDbManager.this;
                        displayEngineDbManager.closeCursur(cursor);
                        return records;
                    }
                }
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.getAllRecords() mDatabase error or Invalid input!");
                return records;
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public int getSizeWithoutLock(Bundle info) {
            int size = 0;
            if (DisplayEngineDbManager.this.mDatabase == null || !DisplayEngineDbManager.this.mDatabase.isOpen() || info == null) {
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.getSizeWithoutLock() mDatabase error.");
                return 0;
            }
            int userId = info.getInt("UserID");
            if (userId < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.getSizeWithoutLock() invalid input: userId=" + userId);
                return 0;
            }
            try {
                size = (int) DisplayEngineDbManager.this.mDatabase.compileStatement("SELECT COUNT(*) FROM UserDragInformation where USERID = " + userId).simpleQueryForLong();
                DeLog.d(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.getSizeWithoutLock() return " + size);
                return size;
            } catch (SQLException e) {
                DeLog.w(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.getSizeWithoutLock() error:" + e.getMessage());
                return size;
            }
        }

        private boolean deleteRecordsWithoutLock(Bundle info, int count) {
            if (!DisplayEngineDbManager.this.checkDatabaseStatusIsOk() || info == null || count <= 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.deleteRecordsWithoutLock() mDatabase error, info is null or count=" + count);
                return false;
            }
            int userId = info.getInt("UserID");
            if (userId < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.deleteRecordsWithoutLock() invalid input: userId=" + userId);
                return false;
            }
            try {
                SQLiteDatabase sQLiteDatabase = DisplayEngineDbManager.this.mDatabase;
                int rows = sQLiteDatabase.delete(DisplayEngineDbHelper.TABLE_NAME_DRAG_INFORMATION, "_id IN(SELECT _id FROM UserDragInformation where USERID = " + userId + " ORDER BY PRIORITY DESC, _id ASC LIMIT " + count + ")", null);
                DeLog.i(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.deleteRecordsWithoutLock(userId=" + userId + ", count=" + count + ") sucess. Delete " + rows + " records.");
                return true;
            } catch (SQLException e) {
                DeLog.w(DisplayEngineDbManager.TAG, "DragInformationTableProcessor.deleteRecordsWithoutLock() error:" + e.getMessage());
                return false;
            }
        }
    }

    private class UserPreferencesTableProcessor extends TableProcessor {
        private static final int MAX_SEGMENT_LENGTH = 255;

        public UserPreferencesTableProcessor() {
            super();
        }

        private boolean validateForAddOrUpdateRecord(Bundle data, int userId, int appType, int[] alValues, int[] deltaValues) {
            if (userId < 0 || appType < 0 || alValues == null || deltaValues == null || alValues.length != deltaValues.length || alValues.length > 255 || (this.mMaxSize > 0 && alValues.length > this.mMaxSize)) {
                if (alValues == null || deltaValues == null) {
                    DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.addOrUpdateRecord error: al=null or delta=null");
                } else {
                    DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.addOrUpdateRecord error: userId=" + userId + " appType=" + appType + " al size=" + alValues.length + " delta size=" + deltaValues.length + " max size=" + this.mMaxSize);
                }
                return false;
            } else if (clearWithoutLock(data)) {
                return true;
            } else {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.addOrUpdateRecord(userId=" + userId + ", appType=" + appType + ") error: clear last records!");
                return false;
            }
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public boolean addOrUpdateRecord(Bundle data) {
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    if (data != null) {
                        int userId = data.getInt("UserID", -1);
                        int appType = data.getInt("AppType", -1);
                        int[] alValues = data.getIntArray("AmbientLight");
                        int[] deltaValues = data.getIntArray(UserPreferencesKey.DELTA);
                        if (!validateForAddOrUpdateRecord(data, userId, appType, alValues, deltaValues)) {
                            return false;
                        }
                        try {
                            StringBuffer text = new StringBuffer();
                            text.append("UserPreferencesTableProcessor add record succ: userId=" + userId + ", appType=" + appType + ", segment={");
                            int id = (userId << 16) + (appType << 8);
                            for (int i = 0; i < alValues.length; i++) {
                                DisplayEngineDbManager.this.mDatabase.execSQL("INSERT INTO UserPreferences VALUES(?, ?, ?, ?, ?)", new Object[]{Integer.valueOf(id + i + 1), Integer.valueOf(userId), Integer.valueOf(appType), Integer.valueOf(alValues[i]), Integer.valueOf(deltaValues[i])});
                                text.append(alValues[i] + "," + deltaValues[i] + ";");
                            }
                            DeLog.i(DisplayEngineDbManager.TAG, text.toString() + "}");
                            return true;
                        } catch (SQLException e) {
                            StringBuffer text2 = new StringBuffer();
                            text2.append("UserPreferencesTableProcessor add record userId=" + userId + ", appType=" + appType + ", segment={");
                            for (int i2 = 0; i2 < alValues.length; i2++) {
                                text2.append(alValues[i2] + "," + deltaValues[i2] + ";");
                            }
                            DeLog.e(DisplayEngineDbManager.TAG, text2.toString() + "}, error:" + e.getMessage());
                            return false;
                        }
                    }
                }
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.addOrUpdateRecord error: Invalid input!");
                return false;
            }
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public ArrayList<Bundle> getAllRecords(Bundle info) {
            DisplayEngineDbManager displayEngineDbManager;
            ArrayList<Bundle> records = new ArrayList<>();
            if (info == null) {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getAllRecords invalid input: info=null");
                return records;
            }
            int userId = info.getInt("UserID", -1);
            int appType = info.getInt("AppType", -1);
            if (userId < 0 || appType < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getAllRecords invalid input: userId=" + userId + " appType=" + appType);
                return records;
            }
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (!DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getAllRecords() mDatabase error.");
                    return records;
                }
                Cursor cursor = null;
                try {
                    cursor = DisplayEngineDbManager.this.mDatabase.rawQuery("SELECT * FROM UserPreferences WHERE USERID = ? AND APPTYPE = ?", new String[]{String.valueOf(userId), String.valueOf(appType)});
                    if (cursor == null) {
                        DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getAllRecords() query database error.");
                        DisplayEngineDbManager.this.closeCursur(cursor);
                        return records;
                    }
                    while (cursor.moveToNext()) {
                        Bundle record = new Bundle();
                        record.putInt("AmbientLight", cursor.getInt(cursor.getColumnIndex("AL")));
                        record.putInt(UserPreferencesKey.DELTA, cursor.getInt(cursor.getColumnIndex("DELTA")));
                        records.add(record);
                    }
                    displayEngineDbManager = DisplayEngineDbManager.this;
                    displayEngineDbManager.closeCursur(cursor);
                    return records;
                } catch (SQLException e) {
                    records = null;
                    DeLog.w(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getAllRecords() error:" + e.getMessage());
                    displayEngineDbManager = DisplayEngineDbManager.this;
                } catch (Throwable th) {
                    DisplayEngineDbManager.this.closeCursur(null);
                    throw th;
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public int getSizeWithoutLock(Bundle info) {
            if (info == null) {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() invalid input: info=null");
                return 0;
            }
            int userId = info.getInt("UserID", -1);
            int appType = info.getInt("AppType", -1);
            if (userId < 0 || appType < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() invalid input: userId=" + userId + " appType=" + appType);
                return 0;
            }
            int size = 0;
            if (DisplayEngineDbManager.this.mDatabase == null || !DisplayEngineDbManager.this.mDatabase.isOpen()) {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() mDatabase error.");
            } else {
                Cursor cursor = null;
                try {
                    cursor = DisplayEngineDbManager.this.mDatabase.rawQuery("SELECT * FROM UserPreferences where (USERID = ?) and (APPTYPE = ?)", new String[]{String.valueOf(userId), String.valueOf(appType)});
                    if (cursor != null && cursor.getCount() > 0) {
                        size = cursor.getCount();
                    }
                    DeLog.i(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() return " + size);
                } catch (SQLException e) {
                    DeLog.w(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() error:" + e.getMessage());
                } catch (Throwable th) {
                    DisplayEngineDbManager.this.closeCursur(null);
                    throw th;
                }
                DisplayEngineDbManager.this.closeCursur(cursor);
            }
            return size;
        }

        private boolean clearWithoutLock(Bundle info) {
            if (info == null) {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() invalid input: info=null");
                return false;
            }
            int userId = info.getInt("UserID", -1);
            int appType = info.getInt("AppType", -1);
            if (userId < 0 || appType < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() invalid input: userId=" + userId + " appType=" + appType);
                return false;
            } else if (!DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                DeLog.e(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() mDatabase error.");
                return false;
            } else {
                try {
                    if (getSizeWithoutLock(info) > 0) {
                        DisplayEngineDbManager.this.mDatabase.execSQL("DELETE FROM UserPreferences where (USERID = ?) and (APPTYPE = ?)", new Object[]{Integer.valueOf(userId), Integer.valueOf(appType)});
                    }
                    DeLog.i(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() sucess.");
                    return true;
                } catch (SQLException e) {
                    DeLog.w(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() error:" + e.getMessage());
                    return false;
                } catch (IllegalArgumentException e2) {
                    DeLog.w(DisplayEngineDbManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() error:" + e2.getMessage());
                    return false;
                }
            }
        }
    }

    private class BrightnessCurveTableProcessor extends TableProcessor {
        private final String mTableName;

        public BrightnessCurveTableProcessor(String name) {
            super();
            if (name == null) {
                this.mTableName = null;
                DeLog.e(DisplayEngineDbManager.TAG, "BrightnessCurveTableProcessor invalid input name=null!");
                return;
            }
            char c = 65535;
            switch (name.hashCode()) {
                case -1518388362:
                    if (name.equals("BrightnessCurveLow")) {
                        c = 0;
                        break;
                    }
                    break;
                case 174475712:
                    if (name.equals("BrightnessCurveHigh")) {
                        c = 2;
                        break;
                    }
                    break;
                case 310490675:
                    if (name.equals("BrightnessCurveMiddle")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1524927843:
                    if (name.equals("BrightnessCurveDefault")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                this.mTableName = "BrightnessCurveLow";
            } else if (c == 1) {
                this.mTableName = "BrightnessCurveMiddle";
            } else if (c == 2) {
                this.mTableName = "BrightnessCurveHigh";
            } else if (c != 3) {
                this.mTableName = null;
                DeLog.e(DisplayEngineDbManager.TAG, "BrightnessCurveTableProcessor unknown name=" + name);
            } else {
                this.mTableName = "BrightnessCurveDefault";
            }
        }

        private boolean validateForAddOrUpdateRecord(Bundle data, int userId, float[] alValues, float[] blValues) {
            if (userId < 0 || alValues == null || blValues == null || alValues.length != blValues.length || (this.mMaxSize > 0 && alValues.length > this.mMaxSize)) {
                if (alValues == null || blValues == null) {
                    DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].addOrUpdateRecord error: userId=" + userId + " al=null or bl=null");
                } else {
                    DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].addOrUpdateRecord error: userId=" + userId + " al size=" + alValues.length + " bl size=" + blValues.length + " max size=" + this.mMaxSize);
                }
                return false;
            } else if (clearWithoutLock(data)) {
                return true;
            } else {
                DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].addOrUpdateRecord error: clear last records!");
                return false;
            }
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public boolean addOrUpdateRecord(Bundle data) {
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    if (data != null) {
                        int userId = data.getInt("UserID");
                        float[] alValues = data.getFloatArray("AmbientLight");
                        float[] blValues = data.getFloatArray(BrightnessCurveKey.BL);
                        if (!validateForAddOrUpdateRecord(data, userId, alValues, blValues)) {
                            return false;
                        }
                        try {
                            StringBuffer text = new StringBuffer();
                            text.append("TableProcessor[" + this.mTableName + "] add record succ: userId=" + userId + " points={");
                            for (int i = 0; i < alValues.length; i++) {
                                SQLiteDatabase sQLiteDatabase = DisplayEngineDbManager.this.mDatabase;
                                sQLiteDatabase.execSQL("INSERT INTO " + this.mTableName + " VALUES(?, ?, ?, ?)", new Object[]{Integer.valueOf(i + 1), Integer.valueOf(userId), Float.valueOf(alValues[i]), Float.valueOf(blValues[i])});
                                text.append(alValues[i] + "," + blValues[i] + ";");
                            }
                            DeLog.i(DisplayEngineDbManager.TAG, text.toString() + "}");
                            return true;
                        } catch (SQLException e) {
                            StringBuffer text2 = new StringBuffer();
                            text2.append("TableProcessor[" + this.mTableName + "] add record userId=" + userId + " points={");
                            for (int i2 = 0; i2 < alValues.length; i2++) {
                                text2.append(alValues[i2] + "," + blValues[i2] + ";");
                            }
                            DeLog.e(DisplayEngineDbManager.TAG, text2.toString() + "}, error:" + e.getMessage());
                            return false;
                        }
                    }
                }
                DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].addOrUpdateRecord error: Invalid input!");
                return false;
            }
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public ArrayList<Bundle> getAllRecords(Bundle info) {
            DisplayEngineDbManager displayEngineDbManager;
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    if (info != null) {
                        int userId = info.getInt("UserID");
                        if (userId < 0) {
                            DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].getAllRecords() invalid input: userId=" + userId);
                            return records;
                        }
                        Cursor cursor = null;
                        try {
                            SQLiteDatabase sQLiteDatabase = DisplayEngineDbManager.this.mDatabase;
                            cursor = sQLiteDatabase.rawQuery("SELECT * FROM " + this.mTableName + " where USERID = ? ORDER BY AL ASC", new String[]{String.valueOf(userId)});
                            if (cursor == null) {
                                DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].getAllRecords() query database error.");
                                DisplayEngineDbManager.this.closeCursur(cursor);
                                return records;
                            }
                            while (cursor.moveToNext()) {
                                Bundle record = new Bundle();
                                record.putInt("UserID", cursor.getInt(cursor.getColumnIndex("USERID")));
                                record.putFloat("AmbientLight", cursor.getFloat(cursor.getColumnIndex("AL")));
                                record.putFloat(BrightnessCurveKey.BL, cursor.getFloat(cursor.getColumnIndex("BL")));
                                records.add(record);
                            }
                            displayEngineDbManager = DisplayEngineDbManager.this;
                            displayEngineDbManager.closeCursur(cursor);
                            return records;
                        } catch (SQLException e) {
                            records = null;
                            DeLog.w(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].getAllRecords() error:" + e.getMessage());
                            displayEngineDbManager = DisplayEngineDbManager.this;
                        } catch (Throwable th) {
                            DisplayEngineDbManager.this.closeCursur(null);
                            throw th;
                        }
                    }
                }
                DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].getAllRecords() mDatabase error or info is null.");
                return records;
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public int getSizeWithoutLock(Bundle info) {
            int size = 0;
            if (DisplayEngineDbManager.this.mDatabase == null || !DisplayEngineDbManager.this.mDatabase.isOpen() || info == null) {
                DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].getSizeWithoutLock() mDatabase error.");
                return 0;
            }
            int userId = info.getInt("UserID");
            if (userId < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].getSizeWithoutLock() invalid input: userId=" + userId);
                return 0;
            }
            try {
                size = (int) DisplayEngineDbManager.this.mDatabase.compileStatement("SELECT COUNT(*) FROM " + this.mTableName + " where USERID = " + userId).simpleQueryForLong();
                DeLog.i(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].getSizeWithoutLock() return " + size);
                return size;
            } catch (SQLException e) {
                DeLog.w(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].getSizeWithoutLock() error:" + e.getMessage());
                return size;
            }
        }

        private boolean clearWithoutLock(Bundle info) {
            if (!DisplayEngineDbManager.this.checkDatabaseStatusIsOk() || info == null) {
                DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() mDatabase error or info is null.");
                return false;
            }
            int userId = info.getInt("UserID");
            if (userId < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() invalid input: userId=" + userId);
                return false;
            }
            try {
                if (getSizeWithoutLock(info) > 0) {
                    SQLiteDatabase sQLiteDatabase = DisplayEngineDbManager.this.mDatabase;
                    sQLiteDatabase.execSQL("DELETE FROM " + this.mTableName + " where USERID = " + userId);
                }
                DeLog.i(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() sucess.");
                return true;
            } catch (SQLException e) {
                DeLog.w(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() error:" + e.getMessage());
                return false;
            } catch (IllegalArgumentException e2) {
                DeLog.w(DisplayEngineDbManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() error:" + e2.getMessage());
                return false;
            }
        }
    }

    private class AlgorithmEscwTableProcessor extends TableProcessor {
        public AlgorithmEscwTableProcessor() {
            super();
        }

        private boolean validateForAddOrUpdateRecord(Bundle data, int userId, float[] escwValues) {
            if (userId < 0 || escwValues == null || (this.mMaxSize > 0 && escwValues.length > this.mMaxSize)) {
                if (escwValues == null) {
                    DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.addOrUpdateRecord error: userId=" + userId + " escw=null");
                } else {
                    DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.addOrUpdateRecord error: userId=" + userId + " escw size=" + escwValues.length + " max size=" + this.mMaxSize);
                }
                return false;
            } else if (clearWithoutLock(data)) {
                return true;
            } else {
                DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.addOrUpdateRecord() error: clear last records!");
                return false;
            }
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public boolean addOrUpdateRecord(Bundle data) {
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    if (data != null) {
                        int userId = data.getInt("UserID");
                        float[] escwValues = data.getFloatArray(AlgorithmEscwKey.ESCW);
                        if (!validateForAddOrUpdateRecord(data, userId, escwValues)) {
                            return false;
                        }
                        try {
                            StringBuffer text = new StringBuffer();
                            text.append("AlgorithmEscwTableProcessor add record succ: userId=" + userId + " escw={");
                            for (int i = 0; i < escwValues.length; i++) {
                                DisplayEngineDbManager.this.mDatabase.execSQL("INSERT INTO AlgorithmESCW VALUES(?, ?, ?)", new Object[]{Integer.valueOf(i + 1), Integer.valueOf(userId), Float.valueOf(escwValues[i])});
                                text.append(escwValues[i] + ";");
                            }
                            DeLog.i(DisplayEngineDbManager.TAG, text.toString() + "}");
                            return true;
                        } catch (SQLException e) {
                            StringBuffer text2 = new StringBuffer();
                            text2.append("AlgorithmEscwTableProcessor add record escw={");
                            for (int i2 = 0; i2 < escwValues.length; i2++) {
                                text2.append(escwValues[i2] + ";");
                            }
                            DeLog.e(DisplayEngineDbManager.TAG, text2.toString() + "}, error:" + e.getMessage());
                            return false;
                        }
                    }
                }
                DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.addOrUpdateRecord error: Invalid input!");
                return false;
            }
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public ArrayList<Bundle> getAllRecords(Bundle info) {
            DisplayEngineDbManager displayEngineDbManager;
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    if (info != null) {
                        int userId = info.getInt("UserID");
                        if (userId < 0) {
                            DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.getAllRecords() invalid input: userId=" + userId);
                            return records;
                        }
                        Cursor cursor = null;
                        try {
                            cursor = DisplayEngineDbManager.this.mDatabase.rawQuery("SELECT * FROM AlgorithmESCW where USERID = ?", new String[]{String.valueOf(userId)});
                            if (cursor == null) {
                                DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.getAllRecords() query database error.");
                                DisplayEngineDbManager.this.closeCursur(cursor);
                                return records;
                            }
                            while (cursor.moveToNext()) {
                                Bundle record = new Bundle();
                                record.putInt("UserID", cursor.getInt(cursor.getColumnIndex("USERID")));
                                record.putFloat(AlgorithmEscwKey.ESCW, cursor.getFloat(cursor.getColumnIndex(AlgorithmEscwKey.ESCW)));
                                records.add(record);
                            }
                            displayEngineDbManager = DisplayEngineDbManager.this;
                            displayEngineDbManager.closeCursur(cursor);
                            return records;
                        } catch (SQLException e) {
                            records = null;
                            DeLog.w(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.getAllRecords() error:" + e.getMessage());
                            displayEngineDbManager = DisplayEngineDbManager.this;
                        } catch (Throwable th) {
                            DisplayEngineDbManager.this.closeCursur(null);
                            throw th;
                        }
                    }
                }
                DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.getAllRecords() mDatabase error or info is null.");
                return records;
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public int getSizeWithoutLock(Bundle info) {
            int size = 0;
            if (DisplayEngineDbManager.this.mDatabase == null || !DisplayEngineDbManager.this.mDatabase.isOpen() || info == null) {
                DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.getSizeWithoutLock() mDatabase error or info is null.");
                return 0;
            }
            int userId = info.getInt("UserID");
            if (userId < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.getSizeWithoutLock() invalid input: userId=" + userId);
                return 0;
            }
            try {
                size = (int) DisplayEngineDbManager.this.mDatabase.compileStatement("SELECT COUNT(*) FROM AlgorithmESCW where USERID = " + userId).simpleQueryForLong();
                DeLog.d(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.getSizeWithoutLock() return " + size);
                return size;
            } catch (SQLException e) {
                DeLog.w(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.getSizeWithoutLock() error:" + e.getMessage());
                return size;
            }
        }

        private boolean clearWithoutLock(Bundle info) {
            if (!DisplayEngineDbManager.this.checkDatabaseStatusIsOk() || info == null) {
                DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.clearWithoutLock() mDatabase error or info is null.");
                return false;
            }
            int userId = info.getInt("UserID");
            if (userId < 0) {
                DeLog.e(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.clearWithoutLock() invalid input: userId=" + userId);
                return false;
            }
            try {
                if (getSizeWithoutLock(info) > 0) {
                    SQLiteDatabase sQLiteDatabase = DisplayEngineDbManager.this.mDatabase;
                    sQLiteDatabase.execSQL("DELETE FROM AlgorithmESCW where USERID = " + userId);
                }
                DeLog.i(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.clearWithoutLock() sucess.");
                return true;
            } catch (SQLException e) {
                DeLog.w(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.clearWithoutLock() error:" + e.getMessage());
                return false;
            } catch (IllegalArgumentException e2) {
                DeLog.w(DisplayEngineDbManager.TAG, "AlgorithmEscwTableProcessor.clearWithoutLock() error:" + e2.getMessage());
                return false;
            }
        }
    }

    private class DataCleanerTableProcessor extends TableProcessor {
        public DataCleanerTableProcessor() {
            super();
        }

        private boolean validateForAddOrUpdateRecord(Bundle data, int userId, int flag, long time) {
            if (time > 0 && userId >= 0 && flag >= 0) {
                return true;
            }
            DeLog.e(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor.addOrUpdateRecord error: userId=" + userId + " time=" + time + " RangeFalg=" + flag);
            return false;
        }

        private boolean addOrUpdateTableRecord(int userId, int flag, long time) {
            boolean ret = true;
            Cursor cursor = null;
            try {
                cursor = DisplayEngineDbManager.this.mDatabase.rawQuery("SELECT * FROM DataCleaner where _id = ?", new String[]{String.valueOf(userId)});
                if (cursor == null || cursor.getCount() <= 0) {
                    DisplayEngineDbManager.this.mDatabase.execSQL("INSERT INTO DataCleaner VALUES(?, ?, ?)", new Object[]{Integer.valueOf(userId), Integer.valueOf(flag), Long.valueOf(time)});
                    DeLog.i(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor add a record succ: userId=" + userId + " time=" + time + " RangeFalg=" + flag);
                    DisplayEngineDbManager.this.closeCursur(cursor);
                    return ret;
                }
                ContentValues values = new ContentValues();
                values.put("_id", Integer.valueOf(userId));
                values.put("RANGEFLAG", Integer.valueOf(flag));
                values.put("TIMESTAMP", Long.valueOf(time));
                int rows = DisplayEngineDbManager.this.mDatabase.update("DataCleaner", values, "_id = ?", new String[]{String.valueOf(userId)});
                if (rows == 0) {
                    DeLog.e(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor update failed: userId=" + userId + " RangeFlag=" + flag + " timeStamp=" + time);
                    ret = false;
                } else {
                    DeLog.i(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor update succ: rows=" + rows + " userId=" + userId + " RangeFlag=" + flag + " timeStamp=" + time);
                }
                DisplayEngineDbManager.this.closeCursur(cursor);
                return ret;
            } catch (SQLException e) {
                DeLog.e(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor add a record userId=" + userId + " time=" + time + " RangeFalg=" + flag + ", error:" + e.getMessage());
                ret = false;
            } catch (Throwable th) {
                DisplayEngineDbManager.this.closeCursur(null);
                throw th;
            }
        }

        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public boolean addOrUpdateRecord(Bundle data) {
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    if (data != null) {
                        int userId = data.getInt("UserID");
                        int flag = data.getInt(DataCleanerKey.RANGE_FLAG);
                        long time = data.getLong("TimeStamp");
                        if (!validateForAddOrUpdateRecord(data, userId, flag, time)) {
                            return false;
                        }
                        return addOrUpdateTableRecord(userId, flag, time);
                    }
                }
                DeLog.e(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor.addOrUpdateRecord error: Invalid input!");
                return false;
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public ArrayList<Bundle> getAllRecords() {
            DisplayEngineDbManager displayEngineDbManager;
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDbManager.this.mDbLock) {
                if (!DisplayEngineDbManager.this.checkDatabaseStatusIsOk()) {
                    DeLog.e(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor.getAllRecords() mDatabase error.");
                    return records;
                }
                Cursor cursor = null;
                try {
                    cursor = DisplayEngineDbManager.this.mDatabase.rawQuery("SELECT * FROM DataCleaner", null);
                    if (cursor == null) {
                        DeLog.e(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor.getAllRecords() query database error.");
                        DisplayEngineDbManager.this.closeCursur(cursor);
                        return records;
                    }
                    while (cursor.moveToNext()) {
                        Bundle record = new Bundle();
                        record.putInt("UserID", cursor.getInt(cursor.getColumnIndex("_id")));
                        record.putInt(DataCleanerKey.RANGE_FLAG, cursor.getInt(cursor.getColumnIndex("RANGEFLAG")));
                        record.putLong("TimeStamp", cursor.getLong(cursor.getColumnIndex("TIMESTAMP")));
                        records.add(record);
                    }
                    displayEngineDbManager = DisplayEngineDbManager.this;
                    displayEngineDbManager.closeCursur(cursor);
                    return records;
                } catch (SQLException e) {
                    records = null;
                    DeLog.w(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor.getAllRecords() error:" + e.getMessage());
                    displayEngineDbManager = DisplayEngineDbManager.this;
                } catch (Throwable th) {
                    DisplayEngineDbManager.this.closeCursur(null);
                    throw th;
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.displayengine.DisplayEngineDbManager.TableProcessor
        public int getSizeWithoutLock() {
            int size = 0;
            if (DisplayEngineDbManager.this.mDatabase == null || !DisplayEngineDbManager.this.mDatabase.isOpen()) {
                DeLog.e(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor.getSizeWithoutLock() mDatabase error.");
                return 0;
            }
            try {
                size = (int) DisplayEngineDbManager.this.mDatabase.compileStatement("SELECT COUNT(*) FROM DataCleaner").simpleQueryForLong();
                DeLog.d(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor.getSizeWithoutLock() return " + size);
                return size;
            } catch (SQLException e) {
                DeLog.w(DisplayEngineDbManager.TAG, "DataCleanerTableProcessor.getSizeWithoutLock() error:" + e.getMessage());
                return size;
            }
        }
    }
}
