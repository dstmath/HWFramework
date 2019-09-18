package com.huawei.displayengine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.ArrayMap;
import java.util.ArrayList;

public class DisplayEngineDBManager {
    private static final String TAG = "DE J DisplayEngineDBManager";
    private static volatile DisplayEngineDBManager mInstance = null;
    private static Object mLock = new Object();
    private static final ArrayMap<String, TableProcessor> mTableProcessors = new ArrayMap<>();
    /* access modifiers changed from: private */
    public SQLiteDatabase mDatabase = null;
    private DisplayEngineDBHelper mHelper;
    /* access modifiers changed from: private */
    public Object mdbLock = new Object();

    public static class AlgorithmESCWKey {
        public static final String ESCW = "ESCW";
        public static final String TAG = "AlgorithmESCW";
        public static final String USERID = "UserID";
    }

    private class AlgorithmESCWTableProcessor extends TableProcessor {
        public AlgorithmESCWTableProcessor() {
            super();
        }

        private boolean pretreatmentForAddorUpdateRecord(Bundle data, int userID, float[] escwValues) {
            if (userID < 0 || escwValues == null || (this.mMaxSize > 0 && escwValues.length > this.mMaxSize)) {
                if (escwValues == null) {
                    DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.addorUpdateRecord error: userID=" + userID + " escw=null");
                } else {
                    DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.addorUpdateRecord error: userID=" + userID + " escw size=" + escwValues.length + " max size=" + this.mMaxSize);
                }
                return false;
            } else if (clearWithoutLock(data)) {
                return true;
            } else {
                DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.addorUpdateRecord() error: clear last records!");
                return false;
            }
        }

        public boolean addorUpdateRecord(Bundle data) {
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    if (data != null) {
                        int userID = data.getInt("UserID");
                        float[] escwValues = data.getFloatArray(AlgorithmESCWKey.ESCW);
                        if (!pretreatmentForAddorUpdateRecord(data, userID, escwValues)) {
                            return false;
                        }
                        try {
                            StringBuffer text = new StringBuffer();
                            text.append("AlgorithmESCWTableProcessor add record succ: userID=" + userID + " escw={");
                            for (int i = 0; i < escwValues.length; i++) {
                                DisplayEngineDBManager.this.mDatabase.execSQL("INSERT INTO AlgorithmESCW VALUES(?, ?, ?)", new Object[]{Integer.valueOf(i + 1), Integer.valueOf(userID), Float.valueOf(escwValues[i])});
                                text.append(escwValues[i] + ";");
                            }
                            DElog.i(DisplayEngineDBManager.TAG, text.toString() + "}");
                            return true;
                        } catch (SQLException e) {
                            StringBuffer text2 = new StringBuffer();
                            text2.append("AlgorithmESCWTableProcessor add record escw={");
                            for (int i2 = 0; i2 < escwValues.length; i2++) {
                                text2.append(escwValues[i2] + ";");
                            }
                            DElog.e(DisplayEngineDBManager.TAG, text2.toString() + "}, error:" + e.getMessage());
                            return false;
                        }
                    }
                }
                DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.addorUpdateRecord error: Invalid input!");
                return false;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0061, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x008f, code lost:
            if (r4 != null) goto L_0x0091;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b3, code lost:
            if (r4 == null) goto L_0x00b6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00b7, code lost:
            return r0;
         */
        public ArrayList<Bundle> getAllRecords(Bundle info) {
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    if (info != null) {
                        int userID = info.getInt("UserID");
                        if (userID < 0) {
                            DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.getAllRecords() invalid input: userID=" + userID);
                            return null;
                        }
                        Cursor c = null;
                        try {
                            c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM AlgorithmESCW where USERID = ?", new String[]{String.valueOf(userID)});
                            if (c == null) {
                                DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.getAllRecords() query database error.");
                                if (c != null) {
                                    c.close();
                                }
                            } else {
                                while (c.moveToNext()) {
                                    Bundle record = new Bundle();
                                    record.putInt("UserID", c.getInt(c.getColumnIndex("USERID")));
                                    record.putFloat(AlgorithmESCWKey.ESCW, c.getFloat(c.getColumnIndex(AlgorithmESCWKey.ESCW)));
                                    records.add(record);
                                }
                            }
                        } catch (SQLException e) {
                            records = null;
                            try {
                                DElog.w(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.getAllRecords() error:" + e.getMessage());
                            } catch (Throwable th) {
                                if (c != null) {
                                    c.close();
                                }
                                throw th;
                            }
                        }
                    }
                }
                DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.getAllRecords() mDatabase error or info is null.");
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public int getSizeWithoutLock(Bundle info) {
            int size = 0;
            if (DisplayEngineDBManager.this.mDatabase == null || !DisplayEngineDBManager.this.mDatabase.isOpen() || info == null) {
                DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.getSizeWithoutLock() mDatabase error or info is null.");
                return 0;
            }
            int userID = info.getInt("UserID");
            if (userID < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.getSizeWithoutLock() invalid input: userID=" + userID);
                return 0;
            }
            try {
                size = (int) DisplayEngineDBManager.this.mDatabase.compileStatement("SELECT COUNT(*) FROM AlgorithmESCW where USERID = " + userID).simpleQueryForLong();
                DElog.d(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.getSizeWithoutLock() return " + size);
                return size;
            } catch (SQLException e) {
                DElog.w(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.getSizeWithoutLock() error:" + e.getMessage());
                return size;
            }
        }

        /* access modifiers changed from: protected */
        public boolean clearWithoutLock(Bundle info) {
            boolean ret = false;
            if (!DisplayEngineDBManager.this.checkDatabaseStatusIsOk() || info == null) {
                DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.clearWithoutLock() mDatabase error or info is null.");
                return false;
            }
            int userID = info.getInt("UserID");
            if (userID < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.clearWithoutLock() invalid input: userID=" + userID);
                return false;
            }
            try {
                if (getSizeWithoutLock(info) > 0) {
                    SQLiteDatabase access$200 = DisplayEngineDBManager.this.mDatabase;
                    access$200.execSQL("DELETE FROM AlgorithmESCW where USERID = " + userID);
                }
                DElog.i(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.clearWithoutLock() sucess.");
                ret = true;
            } catch (SQLException e) {
                DElog.w(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.clearWithoutLock() error:" + e.getMessage());
            } catch (IllegalArgumentException e2) {
                DElog.w(DisplayEngineDBManager.TAG, "AlgorithmESCWTableProcessor.clearWithoutLock() error:" + e2.getMessage());
            }
            return ret;
        }
    }

    public static class BrightnessCurveKey {
        public static final String AL = "AmbientLight";
        public static final String BL = "BackLight";
        public static final String USERID = "UserID";

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

    private class BrightnessCurveTableProcessor extends TableProcessor {
        private final String mTableName;

        public BrightnessCurveTableProcessor(String name) {
            super();
            if (name == null) {
                this.mTableName = null;
                DElog.e(DisplayEngineDBManager.TAG, "BrightnessCurveTableProcessor invalid input name=null!");
                return;
            }
            char c = 65535;
            int hashCode = name.hashCode();
            if (hashCode != -1518388362) {
                if (hashCode != 174475712) {
                    if (hashCode != 310490675) {
                        if (hashCode == 1524927843 && name.equals("BrightnessCurveDefault")) {
                            c = 3;
                        }
                    } else if (name.equals("BrightnessCurveMiddle")) {
                        c = 1;
                    }
                } else if (name.equals("BrightnessCurveHigh")) {
                    c = 2;
                }
            } else if (name.equals("BrightnessCurveLow")) {
                c = 0;
            }
            switch (c) {
                case 0:
                    this.mTableName = "BrightnessCurveLow";
                    break;
                case 1:
                    this.mTableName = "BrightnessCurveMiddle";
                    break;
                case 2:
                    this.mTableName = "BrightnessCurveHigh";
                    break;
                case 3:
                    this.mTableName = "BrightnessCurveDefault";
                    break;
                default:
                    this.mTableName = null;
                    DElog.e(DisplayEngineDBManager.TAG, "BrightnessCurveTableProcessor unknown name=" + name);
                    break;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0071, code lost:
            if (r3 != null) goto L_0x0073;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x009f, code lost:
            if (r3 == null) goto L_0x00a2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            r7.mMaxSize = r8;
            com.huawei.displayengine.DElog.i(com.huawei.displayengine.DisplayEngineDBManager.TAG, "TableProcessor[" + r7.mTableName + "].setMaxSize(" + r7.mMaxSize + ") success.");
         */
        public boolean setMaxSize(int size) {
            boolean ret = true;
            if (size > 0) {
                synchronized (DisplayEngineDBManager.this.mdbLock) {
                    if (!DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                        DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].setMaxSize() mDatabase error!");
                        return false;
                    }
                    Cursor c = null;
                    try {
                        c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT DISTINCT USERID FROM UserDragInformation", null);
                        if (c != null && c.getCount() > 0) {
                            while (c.moveToNext()) {
                                Bundle info = new Bundle();
                                info.putInt("UserID", c.getInt(c.getColumnIndex("USERID")));
                                if (getSizeWithoutLock(info) > size && !clearWithoutLock(info)) {
                                    ret = false;
                                }
                            }
                        }
                    } catch (SQLException e) {
                        ret = false;
                        try {
                            DElog.w(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].setMaxSize() failed to get all the user IDs, error:" + e.getMessage());
                        } catch (Throwable th) {
                            if (c != null) {
                                c.close();
                            }
                            throw th;
                        }
                    }
                }
            } else {
                ret = false;
                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].setMaxSize invalid input: size=" + size);
            }
            return ret;
        }

        private boolean pretreatmentForAddorUpdateRecord(Bundle data, int userID, float[] alValues, float[] blValues) {
            if (userID < 0 || alValues == null || blValues == null || alValues.length != blValues.length || (this.mMaxSize > 0 && alValues.length > this.mMaxSize)) {
                if (alValues == null || blValues == null) {
                    DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].addorUpdateRecord error: userID=" + userID + " al=null or bl=null");
                } else {
                    DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].addorUpdateRecord error: userID=" + userID + " al size=" + alValues.length + " bl size=" + blValues.length + " max size=" + this.mMaxSize);
                }
                return false;
            } else if (clearWithoutLock(data)) {
                return true;
            } else {
                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].addorUpdateRecord error: clear last records!");
                return false;
            }
        }

        public boolean addorUpdateRecord(Bundle data) {
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    if (data != null) {
                        int userID = data.getInt("UserID");
                        float[] alValues = data.getFloatArray("AmbientLight");
                        float[] blValues = data.getFloatArray(BrightnessCurveKey.BL);
                        if (!pretreatmentForAddorUpdateRecord(data, userID, alValues, blValues)) {
                            return false;
                        }
                        try {
                            StringBuffer text = new StringBuffer();
                            text.append("TableProcessor[" + this.mTableName + "] add record succ: userID=" + userID + " points={");
                            for (int i = 0; i < alValues.length; i++) {
                                SQLiteDatabase access$200 = DisplayEngineDBManager.this.mDatabase;
                                access$200.execSQL("INSERT INTO " + this.mTableName + " VALUES(?, ?, ?, ?)", new Object[]{Integer.valueOf(i + 1), Integer.valueOf(userID), Float.valueOf(alValues[i]), Float.valueOf(blValues[i])});
                                text.append(alValues[i] + "," + blValues[i] + ";");
                            }
                            DElog.i(DisplayEngineDBManager.TAG, text.toString() + "}");
                            return true;
                        } catch (SQLException e) {
                            StringBuffer text2 = new StringBuffer();
                            text2.append("TableProcessor[" + this.mTableName + "] add record userID=" + userID + " points={");
                            for (int i2 = 0; i2 < alValues.length; i2++) {
                                text2.append(alValues[i2] + "," + blValues[i2] + ";");
                            }
                            DElog.e(DisplayEngineDBManager.TAG, text2.toString() + "}, error:" + e.getMessage());
                            return false;
                        }
                    }
                }
                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].addorUpdateRecord error: Invalid input!");
                return false;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0097, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x00d4, code lost:
            if (r4 != null) goto L_0x00d6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x0102, code lost:
            if (r4 == null) goto L_0x0105;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0106, code lost:
            return r0;
         */
        public ArrayList<Bundle> getAllRecords(Bundle info) {
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    if (info != null) {
                        int userID = info.getInt("UserID");
                        if (userID < 0) {
                            DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].getAllRecords() invalid input: userID=" + userID);
                            return null;
                        }
                        Cursor c = null;
                        try {
                            c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM " + this.mTableName + " where USERID = ? ORDER BY AL ASC", new String[]{String.valueOf(userID)});
                            if (c == null) {
                                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].getAllRecords() query database error.");
                                if (c != null) {
                                    c.close();
                                }
                            } else {
                                while (c.moveToNext()) {
                                    Bundle record = new Bundle();
                                    record.putInt("UserID", c.getInt(c.getColumnIndex("USERID")));
                                    record.putFloat("AmbientLight", c.getFloat(c.getColumnIndex("AL")));
                                    record.putFloat(BrightnessCurveKey.BL, c.getFloat(c.getColumnIndex("BL")));
                                    records.add(record);
                                }
                            }
                        } catch (SQLException e) {
                            records = null;
                            try {
                                DElog.w(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].getAllRecords() error:" + e.getMessage());
                            } catch (Throwable th) {
                                if (c != null) {
                                    c.close();
                                }
                                throw th;
                            }
                        }
                    }
                }
                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].getAllRecords() mDatabase error or info is null.");
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public int getSizeWithoutLock(Bundle info) {
            int size = 0;
            if (DisplayEngineDBManager.this.mDatabase == null || !DisplayEngineDBManager.this.mDatabase.isOpen() || info == null) {
                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].getSizeWithoutLock() mDatabase error.");
                return 0;
            }
            int userID = info.getInt("UserID");
            if (userID < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].getSizeWithoutLock() invalid input: userID=" + userID);
                return 0;
            }
            try {
                size = (int) DisplayEngineDBManager.this.mDatabase.compileStatement("SELECT COUNT(*) FROM " + this.mTableName + " where USERID = " + userID).simpleQueryForLong();
                DElog.i(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].getSizeWithoutLock() return " + size);
                return size;
            } catch (SQLException e) {
                DElog.w(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].getSizeWithoutLock() error:" + e.getMessage());
                return size;
            }
        }

        /* access modifiers changed from: protected */
        public boolean clearWithoutLock(Bundle info) {
            boolean ret = false;
            if (!DisplayEngineDBManager.this.checkDatabaseStatusIsOk() || info == null) {
                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() mDatabase error or info is null.");
                return false;
            }
            int userID = info.getInt("UserID");
            if (userID < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() invalid input: userID=" + userID);
                return false;
            }
            try {
                if (getSizeWithoutLock(info) > 0) {
                    SQLiteDatabase access$200 = DisplayEngineDBManager.this.mDatabase;
                    access$200.execSQL("DELETE FROM " + this.mTableName + " where USERID = " + userID);
                }
                DElog.i(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() sucess.");
                ret = true;
            } catch (SQLException e) {
                DElog.w(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() error:" + e.getMessage());
            } catch (IllegalArgumentException e2) {
                DElog.w(DisplayEngineDBManager.TAG, "TableProcessor[" + this.mTableName + "].clearWithoutLock() error:" + e2.getMessage());
            }
            return ret;
        }
    }

    public static class DataCleanerKey {
        public static final String RANGEFLAG = "RangeFlag";
        public static final String TAG = "DataCleaner";
        public static final String TIMESTAMP = "TimeStamp";
        public static final String USERID = "UserID";
    }

    private class DataCleanerTableProcessor extends TableProcessor {
        public DataCleanerTableProcessor() {
            super();
        }

        private boolean pretreatmentForAddorUpdateRecord(Bundle data, int userID, int flag, long time) {
            if (time > 0 && userID >= 0 && flag >= 0) {
                return true;
            }
            DElog.e(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor.addorUpdateRecord error: userID=" + userID + " time=" + time + " RangeFalg=" + flag);
            return false;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0134, code lost:
            if (r1 != null) goto L_0x0136;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0170, code lost:
            if (r1 == null) goto L_0x0173;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x0174, code lost:
            return r10;
         */
        public boolean addorUpdateRecord(Bundle data) {
            Bundle bundle = data;
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                boolean ret = true;
                if (DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    if (bundle != null) {
                        int userID = bundle.getInt("UserID");
                        int flag = bundle.getInt(DataCleanerKey.RANGEFLAG);
                        long time = bundle.getLong("TimeStamp");
                        if (!pretreatmentForAddorUpdateRecord(bundle, userID, flag, time)) {
                            return false;
                        }
                        Cursor c = null;
                        try {
                            c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM DataCleaner where _id = ?", new String[]{String.valueOf(userID)});
                            if (c == null || c.getCount() <= 0) {
                                DisplayEngineDBManager.this.mDatabase.execSQL("INSERT INTO DataCleaner VALUES(?, ?, ?)", new Object[]{Integer.valueOf(userID), Integer.valueOf(flag), Long.valueOf(time)});
                                DElog.i(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor add a record succ: userID=" + userID + " time=" + time + " RangeFalg=" + flag);
                            } else {
                                ContentValues values = new ContentValues();
                                values.put("_id", Integer.valueOf(userID));
                                values.put("RANGEFLAG", Integer.valueOf(flag));
                                values.put("TIMESTAMP", Long.valueOf(time));
                                if (DisplayEngineDBManager.this.mDatabase.update("DataCleaner", values, "_id = ?", new String[]{String.valueOf(userID)}) == 0) {
                                    DElog.e(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor update failed: userID=" + userID + " RangeFlag=" + flag + " timeStamp=" + time);
                                    ret = false;
                                } else {
                                    DElog.i(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor update succ: rows=" + rows + " userID=" + userID + " RangeFlag=" + flag + " timeStamp=" + time);
                                }
                            }
                        } catch (SQLException e) {
                            try {
                                DElog.e(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor add a record userID=" + userID + " time=" + time + " RangeFalg=" + flag + ", error:" + e.getMessage());
                                ret = false;
                            } catch (Throwable th) {
                                if (c != null) {
                                    c.close();
                                }
                                throw th;
                            }
                        }
                    }
                }
                DElog.e(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor.addorUpdateRecord error: Invalid input!");
                return false;
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x003c, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0079, code lost:
            if (r2 != null) goto L_0x007b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x009d, code lost:
            if (r2 == null) goto L_0x00a0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a1, code lost:
            return r0;
         */
        public ArrayList<Bundle> getAllRecords() {
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (!DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    DElog.e(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor.getAllRecords() mDatabase error.");
                    return null;
                }
                Cursor c = null;
                try {
                    c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM DataCleaner", null);
                    if (c == null) {
                        DElog.e(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor.getAllRecords() query database error.");
                        if (c != null) {
                            c.close();
                        }
                    } else {
                        while (c.moveToNext()) {
                            Bundle record = new Bundle();
                            record.putInt("UserID", c.getInt(c.getColumnIndex("_id")));
                            record.putInt(DataCleanerKey.RANGEFLAG, c.getInt(c.getColumnIndex("RANGEFLAG")));
                            record.putLong("TimeStamp", c.getLong(c.getColumnIndex("TIMESTAMP")));
                            records.add(record);
                        }
                    }
                } catch (SQLException e) {
                    records = null;
                    try {
                        DElog.w(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor.getAllRecords() error:" + e.getMessage());
                    } catch (Throwable th) {
                        if (c != null) {
                            c.close();
                        }
                        throw th;
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public int getSizeWithoutLock() {
            int size = 0;
            if (DisplayEngineDBManager.this.mDatabase == null || !DisplayEngineDBManager.this.mDatabase.isOpen()) {
                DElog.e(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor.getSizeWithoutLock() mDatabase error.");
                return 0;
            }
            try {
                size = (int) DisplayEngineDBManager.this.mDatabase.compileStatement("SELECT COUNT(*) FROM DataCleaner").simpleQueryForLong();
                DElog.d(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor.getSizeWithoutLock() return " + size);
                return size;
            } catch (SQLException e) {
                DElog.w(DisplayEngineDBManager.TAG, "DataCleanerTableProcessor.getSizeWithoutLock() error:" + e.getMessage());
                return size;
            }
        }
    }

    public static class DragInformationKey {
        public static final String AL = "AmbientLight";
        public static final String APPTYPE = "AppType";
        public static final String GAMESTATE = "GameState";
        public static final String PACKAGE = "PackageName";
        public static final String PRIORITY = "Priority";
        public static final String PROXIMITYPOSITIVE = "ProximityPositive";
        public static final String STARTPOINT = "StartPoint";
        public static final String STOPPOINT = "StopPoint";
        public static final String TAG = "DragInfo";
        public static final String TIMESTAMP = "TimeStamp";
        public static final String USERID = "UserID";
        public static final String _ID = "_ID";
    }

    private class DragInformationTableProcessor extends TableProcessor {
        public DragInformationTableProcessor() {
            super();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:23:0x005d, code lost:
            if (r3 != null) goto L_0x005f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
            r3.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0081, code lost:
            if (r3 == null) goto L_0x0084;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            r7.mMaxSize = r8;
            com.huawei.displayengine.DElog.i(com.huawei.displayengine.DisplayEngineDBManager.TAG, "DragInformationTableProcessor.setMaxSize(" + r7.mMaxSize + ") success.");
         */
        public boolean setMaxSize(int size) {
            boolean ret = true;
            if (size > 0) {
                synchronized (DisplayEngineDBManager.this.mdbLock) {
                    if (!DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                        DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.setMaxSize() mDatabase error!");
                        return false;
                    }
                    Cursor c = null;
                    try {
                        c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT DISTINCT USERID FROM UserDragInformation", null);
                        if (c != null && c.getCount() > 0) {
                            while (c.moveToNext()) {
                                Bundle info = new Bundle();
                                info.putInt("UserID", c.getInt(c.getColumnIndex("USERID")));
                                int realSize = getSizeWithoutLock(info);
                                if (realSize > size && !deleteRecordsWithoutLock(info, realSize - size)) {
                                    ret = false;
                                }
                            }
                        }
                    } catch (SQLException e) {
                        ret = false;
                        try {
                            DElog.w(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.setMaxSize() failed to get all the user IDs, error:" + e.getMessage());
                        } catch (Throwable th) {
                            if (c != null) {
                                c.close();
                            }
                            throw th;
                        }
                    }
                }
            } else {
                ret = false;
                DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.setMaxSize() invalid input: size=" + size);
            }
            return ret;
        }

        private boolean pretreatmentForAddorUpdateRecord(Bundle data, int userID, int appType, long time) {
            if (userID < 0 || appType < 0 || time <= 0) {
                DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.addorUpdateRecord invalid input: time=" + time + " userID=" + userID + " appType=" + appType);
                return false;
            }
            if (this.mMaxSize > 0) {
                int realSize = getSizeWithoutLock(data);
                if (realSize < this.mMaxSize || deleteRecordsWithoutLock(data, (realSize - this.mMaxSize) + 1)) {
                    return true;
                }
                return false;
            }
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:136:0x044f A[SYNTHETIC, Splitter:B:136:0x044f] */
        /* JADX WARNING: Removed duplicated region for block: B:145:0x045b A[Catch:{ all -> 0x0454 }] */
        /* JADX WARNING: Removed duplicated region for block: B:83:0x0318  */
        /* JADX WARNING: Removed duplicated region for block: B:86:0x031e  */
        public boolean addorUpdateRecord(Bundle data) {
            boolean ret;
            int userID;
            int al;
            int appType;
            boolean cover;
            String pkgName;
            int gameState;
            String pkgName2;
            Cursor c;
            boolean ret2;
            Bundle bundle = data;
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                int i = 0;
                if (DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    if (bundle != null) {
                        long time = bundle.getLong("TimeStamp");
                        int priority = bundle.getInt(DragInformationKey.PRIORITY);
                        float start = bundle.getFloat(DragInformationKey.STARTPOINT);
                        float stop = bundle.getFloat(DragInformationKey.STOPPOINT);
                        int al2 = bundle.getInt("AmbientLight");
                        boolean cover2 = bundle.getBoolean(DragInformationKey.PROXIMITYPOSITIVE);
                        int userID2 = bundle.getInt("UserID");
                        int appType2 = bundle.getInt("AppType");
                        int gameState2 = bundle.getInt(DragInformationKey.GAMESTATE);
                        String pkgName3 = bundle.getString(DragInformationKey.PACKAGE);
                        int gameState3 = gameState2;
                        int appType3 = appType2;
                        int userID3 = userID2;
                        int al3 = al2;
                        boolean cover3 = cover2;
                        if (!pretreatmentForAddorUpdateRecord(bundle, userID2, appType3, time)) {
                            return false;
                        }
                        Cursor c2 = null;
                        try {
                            c2 = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM UserDragInformation where TIMESTAMP = ?", new String[]{String.valueOf(time)});
                            ContentValues values = new ContentValues();
                            values.put("TIMESTAMP", Long.valueOf(time));
                            values.put("PRIORITY", Integer.valueOf(priority));
                            values.put("STARTPOINT", Float.valueOf(start));
                            values.put("STOPPOINT", Float.valueOf(stop));
                            al = al3;
                            try {
                                values.put("AL", Integer.valueOf(al));
                                cover = cover3;
                                if (cover) {
                                    i = 1;
                                }
                            } catch (SQLException e) {
                                e = e;
                                pkgName = pkgName3;
                                gameState = gameState3;
                                appType = appType3;
                                userID = userID3;
                                cover = cover3;
                                try {
                                    StringBuilder sb = new StringBuilder();
                                    int i2 = priority;
                                    try {
                                        sb.append("DragInformationTableProcessor add a record time=");
                                        sb.append(time);
                                        sb.append(",start=");
                                        sb.append(start);
                                        sb.append(",stop=");
                                        sb.append(stop);
                                        sb.append(",al=");
                                        sb.append(al);
                                        sb.append(",proximitypositive=");
                                        sb.append(cover);
                                        sb.append(",userID=");
                                        sb.append(userID);
                                        sb.append(",appType=");
                                        sb.append(appType);
                                        sb.append(",gameState=");
                                        sb.append(gameState);
                                        sb.append(",pkgName=");
                                        sb.append(pkgName);
                                        sb.append(", error:");
                                        sb.append(e.getMessage());
                                        DElog.e(DisplayEngineDBManager.TAG, sb.toString());
                                        ret = false;
                                        if (c2 != null) {
                                        }
                                        return ret;
                                    } catch (Throwable th) {
                                        th = th;
                                        if (c2 != null) {
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    int i3 = priority;
                                    if (c2 != null) {
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                String str = pkgName3;
                                int i4 = gameState3;
                                int i5 = appType3;
                                int i6 = userID3;
                                boolean z = cover3;
                                int i7 = priority;
                                if (c2 != null) {
                                }
                                throw th;
                            }
                            try {
                                values.put("PROXIMITYPOSITIVE", Integer.valueOf(i));
                                userID = userID3;
                                try {
                                    values.put("USERID", Integer.valueOf(userID));
                                    appType = appType3;
                                } catch (SQLException e2) {
                                    e = e2;
                                    pkgName = pkgName3;
                                    gameState = gameState3;
                                    appType = appType3;
                                    StringBuilder sb2 = new StringBuilder();
                                    int i22 = priority;
                                    sb2.append("DragInformationTableProcessor add a record time=");
                                    sb2.append(time);
                                    sb2.append(",start=");
                                    sb2.append(start);
                                    sb2.append(",stop=");
                                    sb2.append(stop);
                                    sb2.append(",al=");
                                    sb2.append(al);
                                    sb2.append(",proximitypositive=");
                                    sb2.append(cover);
                                    sb2.append(",userID=");
                                    sb2.append(userID);
                                    sb2.append(",appType=");
                                    sb2.append(appType);
                                    sb2.append(",gameState=");
                                    sb2.append(gameState);
                                    sb2.append(",pkgName=");
                                    sb2.append(pkgName);
                                    sb2.append(", error:");
                                    sb2.append(e.getMessage());
                                    DElog.e(DisplayEngineDBManager.TAG, sb2.toString());
                                    ret = false;
                                    if (c2 != null) {
                                    }
                                    return ret;
                                } catch (Throwable th4) {
                                    th = th4;
                                    String str2 = pkgName3;
                                    int i8 = gameState3;
                                    int i9 = appType3;
                                    int i10 = priority;
                                    if (c2 != null) {
                                    }
                                    throw th;
                                }
                            } catch (SQLException e3) {
                                e = e3;
                                pkgName = pkgName3;
                                gameState = gameState3;
                                appType = appType3;
                                userID = userID3;
                                StringBuilder sb22 = new StringBuilder();
                                int i222 = priority;
                                sb22.append("DragInformationTableProcessor add a record time=");
                                sb22.append(time);
                                sb22.append(",start=");
                                sb22.append(start);
                                sb22.append(",stop=");
                                sb22.append(stop);
                                sb22.append(",al=");
                                sb22.append(al);
                                sb22.append(",proximitypositive=");
                                sb22.append(cover);
                                sb22.append(",userID=");
                                sb22.append(userID);
                                sb22.append(",appType=");
                                sb22.append(appType);
                                sb22.append(",gameState=");
                                sb22.append(gameState);
                                sb22.append(",pkgName=");
                                sb22.append(pkgName);
                                sb22.append(", error:");
                                sb22.append(e.getMessage());
                                DElog.e(DisplayEngineDBManager.TAG, sb22.toString());
                                ret = false;
                                if (c2 != null) {
                                }
                                return ret;
                            } catch (Throwable th5) {
                                th = th5;
                                String str3 = pkgName3;
                                int i11 = gameState3;
                                int i12 = appType3;
                                int i13 = userID3;
                                int i14 = priority;
                                if (c2 != null) {
                                }
                                throw th;
                            }
                            try {
                                values.put("APPTYPE", Integer.valueOf(appType));
                                gameState = gameState3;
                                try {
                                    values.put("GAMESTATE", Integer.valueOf(gameState));
                                    pkgName2 = pkgName3;
                                    try {
                                        values.put("PACKAGE", pkgName2);
                                        if (c2 != null) {
                                            try {
                                                if (c2.getCount() > 0) {
                                                    c = c2;
                                                    String pkgName4 = pkgName2;
                                                    try {
                                                        int rows = DisplayEngineDBManager.this.mDatabase.update(DisplayEngineDBHelper.TABLE_NAME_DRAG_INFORMATION, values, "TIMESTAMP = ?", new String[]{String.valueOf(time)});
                                                        if (rows == 0) {
                                                            StringBuilder sb3 = new StringBuilder();
                                                            sb3.append("DragInformationTableProcessor update failed: time=");
                                                            sb3.append(time);
                                                            sb3.append(",priority=");
                                                            sb3.append(priority);
                                                            sb3.append(",start=");
                                                            sb3.append(start);
                                                            sb3.append(",stop=");
                                                            sb3.append(stop);
                                                            sb3.append(",al=");
                                                            sb3.append(al);
                                                            sb3.append(",proximitypositive=");
                                                            sb3.append(cover);
                                                            sb3.append(",userID=");
                                                            sb3.append(userID);
                                                            sb3.append(",appType=");
                                                            sb3.append(appType);
                                                            sb3.append(",gameState=");
                                                            sb3.append(gameState);
                                                            sb3.append(",pkgName=");
                                                            pkgName2 = pkgName4;
                                                            try {
                                                                sb3.append(pkgName2);
                                                                DElog.e(DisplayEngineDBManager.TAG, sb3.toString());
                                                                ret2 = false;
                                                                ContentValues contentValues = values;
                                                            } catch (SQLException e4) {
                                                                e = e4;
                                                                pkgName = pkgName2;
                                                                c2 = c;
                                                                StringBuilder sb222 = new StringBuilder();
                                                                int i2222 = priority;
                                                                sb222.append("DragInformationTableProcessor add a record time=");
                                                                sb222.append(time);
                                                                sb222.append(",start=");
                                                                sb222.append(start);
                                                                sb222.append(",stop=");
                                                                sb222.append(stop);
                                                                sb222.append(",al=");
                                                                sb222.append(al);
                                                                sb222.append(",proximitypositive=");
                                                                sb222.append(cover);
                                                                sb222.append(",userID=");
                                                                sb222.append(userID);
                                                                sb222.append(",appType=");
                                                                sb222.append(appType);
                                                                sb222.append(",gameState=");
                                                                sb222.append(gameState);
                                                                sb222.append(",pkgName=");
                                                                sb222.append(pkgName);
                                                                sb222.append(", error:");
                                                                sb222.append(e.getMessage());
                                                                DElog.e(DisplayEngineDBManager.TAG, sb222.toString());
                                                                ret = false;
                                                                if (c2 != null) {
                                                                }
                                                                return ret;
                                                            } catch (Throwable th6) {
                                                                th = th6;
                                                                String str4 = pkgName2;
                                                                int i15 = priority;
                                                                c2 = c;
                                                                if (c2 != null) {
                                                                }
                                                                throw th;
                                                            }
                                                        } else {
                                                            pkgName2 = pkgName4;
                                                            StringBuilder sb4 = new StringBuilder();
                                                            ContentValues contentValues2 = values;
                                                            sb4.append("DragInformationTableProcessor update succ: rows=");
                                                            sb4.append(rows);
                                                            sb4.append(" time=");
                                                            sb4.append(time);
                                                            sb4.append(",priority=");
                                                            sb4.append(priority);
                                                            sb4.append(",start=");
                                                            sb4.append(start);
                                                            sb4.append(",stop=");
                                                            sb4.append(stop);
                                                            sb4.append(",al=");
                                                            sb4.append(al);
                                                            sb4.append(",proximitypositive=");
                                                            sb4.append(cover);
                                                            sb4.append(",userID=");
                                                            sb4.append(userID);
                                                            sb4.append(",appType=");
                                                            sb4.append(appType);
                                                            sb4.append(",gameState=");
                                                            sb4.append(gameState);
                                                            sb4.append(",pkgName=");
                                                            sb4.append(pkgName2);
                                                            DElog.i(DisplayEngineDBManager.TAG, sb4.toString());
                                                            ret2 = true;
                                                        }
                                                        ret = ret2;
                                                        if (c != null) {
                                                            c.close();
                                                        }
                                                        int i16 = priority;
                                                    } catch (SQLException e5) {
                                                        e = e5;
                                                        c2 = c;
                                                        pkgName = pkgName4;
                                                        StringBuilder sb2222 = new StringBuilder();
                                                        int i22222 = priority;
                                                        sb2222.append("DragInformationTableProcessor add a record time=");
                                                        sb2222.append(time);
                                                        sb2222.append(",start=");
                                                        sb2222.append(start);
                                                        sb2222.append(",stop=");
                                                        sb2222.append(stop);
                                                        sb2222.append(",al=");
                                                        sb2222.append(al);
                                                        sb2222.append(",proximitypositive=");
                                                        sb2222.append(cover);
                                                        sb2222.append(",userID=");
                                                        sb2222.append(userID);
                                                        sb2222.append(",appType=");
                                                        sb2222.append(appType);
                                                        sb2222.append(",gameState=");
                                                        sb2222.append(gameState);
                                                        sb2222.append(",pkgName=");
                                                        sb2222.append(pkgName);
                                                        sb2222.append(", error:");
                                                        sb2222.append(e.getMessage());
                                                        DElog.e(DisplayEngineDBManager.TAG, sb2222.toString());
                                                        ret = false;
                                                        if (c2 != null) {
                                                        }
                                                        return ret;
                                                    } catch (Throwable th7) {
                                                        th = th7;
                                                        int i17 = priority;
                                                        c2 = c;
                                                        String str5 = pkgName4;
                                                        if (c2 != null) {
                                                        }
                                                        throw th;
                                                    }
                                                }
                                            } catch (SQLException e6) {
                                                e = e6;
                                                Cursor cursor = c2;
                                                pkgName = pkgName2;
                                                StringBuilder sb22222 = new StringBuilder();
                                                int i222222 = priority;
                                                sb22222.append("DragInformationTableProcessor add a record time=");
                                                sb22222.append(time);
                                                sb22222.append(",start=");
                                                sb22222.append(start);
                                                sb22222.append(",stop=");
                                                sb22222.append(stop);
                                                sb22222.append(",al=");
                                                sb22222.append(al);
                                                sb22222.append(",proximitypositive=");
                                                sb22222.append(cover);
                                                sb22222.append(",userID=");
                                                sb22222.append(userID);
                                                sb22222.append(",appType=");
                                                sb22222.append(appType);
                                                sb22222.append(",gameState=");
                                                sb22222.append(gameState);
                                                sb22222.append(",pkgName=");
                                                sb22222.append(pkgName);
                                                sb22222.append(", error:");
                                                sb22222.append(e.getMessage());
                                                DElog.e(DisplayEngineDBManager.TAG, sb22222.toString());
                                                ret = false;
                                                if (c2 != null) {
                                                }
                                                return ret;
                                            } catch (Throwable th8) {
                                                th = th8;
                                                Cursor cursor2 = c2;
                                                String str6 = pkgName2;
                                                int i18 = priority;
                                                if (c2 != null) {
                                                }
                                                throw th;
                                            }
                                        }
                                        c = c2;
                                    } catch (SQLException e7) {
                                        e = e7;
                                        pkgName = pkgName2;
                                        StringBuilder sb222222 = new StringBuilder();
                                        int i2222222 = priority;
                                        sb222222.append("DragInformationTableProcessor add a record time=");
                                        sb222222.append(time);
                                        sb222222.append(",start=");
                                        sb222222.append(start);
                                        sb222222.append(",stop=");
                                        sb222222.append(stop);
                                        sb222222.append(",al=");
                                        sb222222.append(al);
                                        sb222222.append(",proximitypositive=");
                                        sb222222.append(cover);
                                        sb222222.append(",userID=");
                                        sb222222.append(userID);
                                        sb222222.append(",appType=");
                                        sb222222.append(appType);
                                        sb222222.append(",gameState=");
                                        sb222222.append(gameState);
                                        sb222222.append(",pkgName=");
                                        sb222222.append(pkgName);
                                        sb222222.append(", error:");
                                        sb222222.append(e.getMessage());
                                        DElog.e(DisplayEngineDBManager.TAG, sb222222.toString());
                                        ret = false;
                                        if (c2 != null) {
                                            c2.close();
                                        }
                                        return ret;
                                    } catch (Throwable th9) {
                                        th = th9;
                                        String str7 = pkgName2;
                                        int i19 = priority;
                                        if (c2 != null) {
                                            c2.close();
                                        }
                                        throw th;
                                    }
                                } catch (SQLException e8) {
                                    e = e8;
                                    pkgName = pkgName3;
                                    StringBuilder sb2222222 = new StringBuilder();
                                    int i22222222 = priority;
                                    sb2222222.append("DragInformationTableProcessor add a record time=");
                                    sb2222222.append(time);
                                    sb2222222.append(",start=");
                                    sb2222222.append(start);
                                    sb2222222.append(",stop=");
                                    sb2222222.append(stop);
                                    sb2222222.append(",al=");
                                    sb2222222.append(al);
                                    sb2222222.append(",proximitypositive=");
                                    sb2222222.append(cover);
                                    sb2222222.append(",userID=");
                                    sb2222222.append(userID);
                                    sb2222222.append(",appType=");
                                    sb2222222.append(appType);
                                    sb2222222.append(",gameState=");
                                    sb2222222.append(gameState);
                                    sb2222222.append(",pkgName=");
                                    sb2222222.append(pkgName);
                                    sb2222222.append(", error:");
                                    sb2222222.append(e.getMessage());
                                    DElog.e(DisplayEngineDBManager.TAG, sb2222222.toString());
                                    ret = false;
                                    if (c2 != null) {
                                    }
                                    return ret;
                                } catch (Throwable th10) {
                                    th = th10;
                                    String str8 = pkgName3;
                                    int i20 = priority;
                                    if (c2 != null) {
                                    }
                                    throw th;
                                }
                                try {
                                    ContentValues values2 = values;
                                    long rowID = DisplayEngineDBManager.this.mDatabase.insert(DisplayEngineDBHelper.TABLE_NAME_DRAG_INFORMATION, null, values2);
                                    if (rowID == -1) {
                                        ContentValues contentValues3 = values2;
                                        StringBuilder sb5 = new StringBuilder();
                                        long j = rowID;
                                        sb5.append("DragInformationTableProcessor insert failed: time=");
                                        sb5.append(time);
                                        sb5.append(",priority=");
                                        sb5.append(priority);
                                        sb5.append(",start=");
                                        sb5.append(start);
                                        sb5.append(",stop=");
                                        sb5.append(stop);
                                        sb5.append(",al=");
                                        sb5.append(al);
                                        sb5.append(",proximitypositive=");
                                        sb5.append(cover);
                                        sb5.append(",userID=");
                                        sb5.append(userID);
                                        sb5.append(",appType=");
                                        sb5.append(appType);
                                        sb5.append(",gameState=");
                                        sb5.append(gameState);
                                        sb5.append(",pkgName=");
                                        sb5.append(pkgName2);
                                        DElog.e(DisplayEngineDBManager.TAG, sb5.toString());
                                        ret = false;
                                        if (c != null) {
                                        }
                                        int i162 = priority;
                                    } else {
                                        long rowID2 = rowID;
                                        ContentValues contentValues4 = values2;
                                        StringBuilder sb6 = new StringBuilder();
                                        sb6.append("DragInformationTableProcessor add a record(");
                                        String pkgName5 = pkgName2;
                                        try {
                                            sb6.append(rowID2);
                                            sb6.append(") succ: time=");
                                            sb6.append(time);
                                            sb6.append(",priority=");
                                            sb6.append(priority);
                                            sb6.append(",start=");
                                            sb6.append(start);
                                            sb6.append(",stop=");
                                            sb6.append(stop);
                                            sb6.append(",al=");
                                            sb6.append(al);
                                            sb6.append(",proximitypositive=");
                                            sb6.append(cover);
                                            sb6.append(",userID=");
                                            sb6.append(userID);
                                            sb6.append(",appType=");
                                            sb6.append(appType);
                                            sb6.append(",gameState=");
                                            sb6.append(gameState);
                                            sb6.append(",pkgName=");
                                            pkgName = pkgName5;
                                        } catch (SQLException e9) {
                                            e = e9;
                                            c2 = c;
                                            pkgName = pkgName5;
                                            StringBuilder sb22222222 = new StringBuilder();
                                            int i222222222 = priority;
                                            sb22222222.append("DragInformationTableProcessor add a record time=");
                                            sb22222222.append(time);
                                            sb22222222.append(",start=");
                                            sb22222222.append(start);
                                            sb22222222.append(",stop=");
                                            sb22222222.append(stop);
                                            sb22222222.append(",al=");
                                            sb22222222.append(al);
                                            sb22222222.append(",proximitypositive=");
                                            sb22222222.append(cover);
                                            sb22222222.append(",userID=");
                                            sb22222222.append(userID);
                                            sb22222222.append(",appType=");
                                            sb22222222.append(appType);
                                            sb22222222.append(",gameState=");
                                            sb22222222.append(gameState);
                                            sb22222222.append(",pkgName=");
                                            sb22222222.append(pkgName);
                                            sb22222222.append(", error:");
                                            sb22222222.append(e.getMessage());
                                            DElog.e(DisplayEngineDBManager.TAG, sb22222222.toString());
                                            ret = false;
                                            if (c2 != null) {
                                            }
                                            return ret;
                                        } catch (Throwable th11) {
                                            th = th11;
                                            c2 = c;
                                            String str9 = pkgName5;
                                            int i21 = priority;
                                            if (c2 != null) {
                                            }
                                            throw th;
                                        }
                                        try {
                                            sb6.append(pkgName);
                                            DElog.i(DisplayEngineDBManager.TAG, sb6.toString());
                                            ret = true;
                                            if (c != null) {
                                            }
                                            int i1622 = priority;
                                        } catch (SQLException e10) {
                                            e = e10;
                                            c2 = c;
                                            StringBuilder sb222222222 = new StringBuilder();
                                            int i2222222222 = priority;
                                            sb222222222.append("DragInformationTableProcessor add a record time=");
                                            sb222222222.append(time);
                                            sb222222222.append(",start=");
                                            sb222222222.append(start);
                                            sb222222222.append(",stop=");
                                            sb222222222.append(stop);
                                            sb222222222.append(",al=");
                                            sb222222222.append(al);
                                            sb222222222.append(",proximitypositive=");
                                            sb222222222.append(cover);
                                            sb222222222.append(",userID=");
                                            sb222222222.append(userID);
                                            sb222222222.append(",appType=");
                                            sb222222222.append(appType);
                                            sb222222222.append(",gameState=");
                                            sb222222222.append(gameState);
                                            sb222222222.append(",pkgName=");
                                            sb222222222.append(pkgName);
                                            sb222222222.append(", error:");
                                            sb222222222.append(e.getMessage());
                                            DElog.e(DisplayEngineDBManager.TAG, sb222222222.toString());
                                            ret = false;
                                            if (c2 != null) {
                                            }
                                            return ret;
                                        } catch (Throwable th12) {
                                            th = th12;
                                            c2 = c;
                                            int i23 = priority;
                                            if (c2 != null) {
                                            }
                                            throw th;
                                        }
                                    }
                                } catch (SQLException e11) {
                                    e = e11;
                                    pkgName = pkgName2;
                                    c2 = c;
                                    StringBuilder sb2222222222 = new StringBuilder();
                                    int i22222222222 = priority;
                                    sb2222222222.append("DragInformationTableProcessor add a record time=");
                                    sb2222222222.append(time);
                                    sb2222222222.append(",start=");
                                    sb2222222222.append(start);
                                    sb2222222222.append(",stop=");
                                    sb2222222222.append(stop);
                                    sb2222222222.append(",al=");
                                    sb2222222222.append(al);
                                    sb2222222222.append(",proximitypositive=");
                                    sb2222222222.append(cover);
                                    sb2222222222.append(",userID=");
                                    sb2222222222.append(userID);
                                    sb2222222222.append(",appType=");
                                    sb2222222222.append(appType);
                                    sb2222222222.append(",gameState=");
                                    sb2222222222.append(gameState);
                                    sb2222222222.append(",pkgName=");
                                    sb2222222222.append(pkgName);
                                    sb2222222222.append(", error:");
                                    sb2222222222.append(e.getMessage());
                                    DElog.e(DisplayEngineDBManager.TAG, sb2222222222.toString());
                                    ret = false;
                                    if (c2 != null) {
                                    }
                                    return ret;
                                } catch (Throwable th13) {
                                    th = th13;
                                    String str10 = pkgName2;
                                    c2 = c;
                                    int i24 = priority;
                                    if (c2 != null) {
                                    }
                                    throw th;
                                }
                            } catch (SQLException e12) {
                                e = e12;
                                pkgName = pkgName3;
                                gameState = gameState3;
                                StringBuilder sb22222222222 = new StringBuilder();
                                int i222222222222 = priority;
                                sb22222222222.append("DragInformationTableProcessor add a record time=");
                                sb22222222222.append(time);
                                sb22222222222.append(",start=");
                                sb22222222222.append(start);
                                sb22222222222.append(",stop=");
                                sb22222222222.append(stop);
                                sb22222222222.append(",al=");
                                sb22222222222.append(al);
                                sb22222222222.append(",proximitypositive=");
                                sb22222222222.append(cover);
                                sb22222222222.append(",userID=");
                                sb22222222222.append(userID);
                                sb22222222222.append(",appType=");
                                sb22222222222.append(appType);
                                sb22222222222.append(",gameState=");
                                sb22222222222.append(gameState);
                                sb22222222222.append(",pkgName=");
                                sb22222222222.append(pkgName);
                                sb22222222222.append(", error:");
                                sb22222222222.append(e.getMessage());
                                DElog.e(DisplayEngineDBManager.TAG, sb22222222222.toString());
                                ret = false;
                                if (c2 != null) {
                                }
                                return ret;
                            } catch (Throwable th14) {
                                th = th14;
                                String str11 = pkgName3;
                                int i25 = gameState3;
                                int i26 = priority;
                                if (c2 != null) {
                                }
                                throw th;
                            }
                        } catch (SQLException e13) {
                            e = e13;
                            pkgName = pkgName3;
                            gameState = gameState3;
                            appType = appType3;
                            userID = userID3;
                            al = al3;
                            cover = cover3;
                            StringBuilder sb222222222222 = new StringBuilder();
                            int i2222222222222 = priority;
                            sb222222222222.append("DragInformationTableProcessor add a record time=");
                            sb222222222222.append(time);
                            sb222222222222.append(",start=");
                            sb222222222222.append(start);
                            sb222222222222.append(",stop=");
                            sb222222222222.append(stop);
                            sb222222222222.append(",al=");
                            sb222222222222.append(al);
                            sb222222222222.append(",proximitypositive=");
                            sb222222222222.append(cover);
                            sb222222222222.append(",userID=");
                            sb222222222222.append(userID);
                            sb222222222222.append(",appType=");
                            sb222222222222.append(appType);
                            sb222222222222.append(",gameState=");
                            sb222222222222.append(gameState);
                            sb222222222222.append(",pkgName=");
                            sb222222222222.append(pkgName);
                            sb222222222222.append(", error:");
                            sb222222222222.append(e.getMessage());
                            DElog.e(DisplayEngineDBManager.TAG, sb222222222222.toString());
                            ret = false;
                            if (c2 != null) {
                            }
                            return ret;
                        } catch (Throwable th15) {
                            th = th15;
                            String str12 = pkgName3;
                            int i27 = gameState3;
                            int i28 = appType3;
                            int i29 = userID3;
                            int i30 = al3;
                            boolean z2 = cover3;
                            int i31 = priority;
                            if (c2 != null) {
                            }
                            throw th;
                        }
                    }
                }
                DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.addorUpdateRecord() mDatabase error or Invalid input!");
                return false;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0095, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0150, code lost:
            if (r5 != null) goto L_0x0152;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
            r5.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x0172, code lost:
            if (r5 == null) goto L_0x0175;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x0176, code lost:
            return r0;
         */
        public ArrayList<Bundle> getAllRecords(Bundle info) {
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    if (info != null) {
                        int userID = info.getInt("UserID");
                        if (userID < 0) {
                            DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.getAllRecords() invalid input: userID=" + userID);
                            return null;
                        }
                        Cursor c = null;
                        if (info.getInt(QueryInfoKey.NUMBERLIMIT, -1) < 1) {
                            try {
                                c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM UserDragInformation where USERID = ? ORDER BY _id ASC", new String[]{String.valueOf(userID)});
                            } catch (SQLException e) {
                                records = null;
                                try {
                                    DElog.w(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.getAllRecords() error:" + e.getMessage());
                                } catch (Throwable th) {
                                    if (c != null) {
                                        c.close();
                                    }
                                    throw th;
                                }
                            }
                        } else {
                            c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM UserDragInformation where USERID = ? ORDER BY _id DESC LIMIT " + numLimit, new String[]{String.valueOf(userID)});
                        }
                        if (c == null) {
                            DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.getAllRecords() query database error.");
                            if (c != null) {
                                c.close();
                            }
                        } else {
                            while (c.moveToNext()) {
                                Bundle record = new Bundle();
                                record.putLong(DragInformationKey._ID, c.getLong(c.getColumnIndex("_id")));
                                record.putLong("TimeStamp", c.getLong(c.getColumnIndex("TIMESTAMP")));
                                record.putInt(DragInformationKey.PRIORITY, c.getInt(c.getColumnIndex("PRIORITY")));
                                record.putFloat(DragInformationKey.STARTPOINT, c.getFloat(c.getColumnIndex("STARTPOINT")));
                                record.putFloat(DragInformationKey.STOPPOINT, c.getFloat(c.getColumnIndex("STOPPOINT")));
                                record.putInt("AmbientLight", c.getInt(c.getColumnIndex("AL")));
                                record.putBoolean(DragInformationKey.PROXIMITYPOSITIVE, c.getInt(c.getColumnIndex("PROXIMITYPOSITIVE")) == 1);
                                record.putInt("UserID", c.getInt(c.getColumnIndex("USERID")));
                                record.putInt("AppType", c.getInt(c.getColumnIndex("APPTYPE")));
                                record.putInt(DragInformationKey.GAMESTATE, c.getInt(c.getColumnIndex("GAMESTATE")));
                                record.putString(DragInformationKey.PACKAGE, c.getString(c.getColumnIndex("PACKAGE")));
                                records.add(record);
                            }
                        }
                    }
                }
                DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.getAllRecords() mDatabase error or Invalid input!");
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public int getSizeWithoutLock(Bundle info) {
            int size = 0;
            if (DisplayEngineDBManager.this.mDatabase == null || !DisplayEngineDBManager.this.mDatabase.isOpen() || info == null) {
                DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.getSizeWithoutLock() mDatabase error.");
                return 0;
            }
            int userID = info.getInt("UserID");
            if (userID < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.getSizeWithoutLock() invalid input: userID=" + userID);
                return 0;
            }
            try {
                size = (int) DisplayEngineDBManager.this.mDatabase.compileStatement("SELECT COUNT(*) FROM UserDragInformation where USERID = " + userID).simpleQueryForLong();
                DElog.d(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.getSizeWithoutLock() return " + size);
                return size;
            } catch (SQLException e) {
                DElog.w(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.getSizeWithoutLock() error:" + e.getMessage());
                return size;
            }
        }

        /* access modifiers changed from: protected */
        public boolean deleteRecordsWithoutLock(Bundle info, int count) {
            boolean ret = false;
            if (!DisplayEngineDBManager.this.checkDatabaseStatusIsOk() || info == null || count <= 0) {
                DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.deleteRecordsWithoutLock() mDatabase error, info is null or count=" + count);
                return false;
            }
            int userID = info.getInt("UserID");
            if (userID < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.deleteRecordsWithoutLock() invalid input: userID=" + userID);
                return false;
            }
            try {
                SQLiteDatabase access$200 = DisplayEngineDBManager.this.mDatabase;
                int rows = access$200.delete(DisplayEngineDBHelper.TABLE_NAME_DRAG_INFORMATION, "_id IN(SELECT _id FROM UserDragInformation where USERID = " + userID + " ORDER BY PRIORITY DESC, _id ASC LIMIT " + count + ")", null);
                DElog.i(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.deleteRecordsWithoutLock(userID=" + userID + ", count=" + count + ") sucess. Delete " + rows + " records.");
                ret = true;
            } catch (SQLException e) {
                DElog.w(DisplayEngineDBManager.TAG, "DragInformationTableProcessor.deleteRecordsWithoutLock() error:" + e.getMessage());
            }
            return ret;
        }
    }

    public static class QueryInfoKey {
        public static final String NUMBERLIMIT = "NumberLimit";
    }

    private class TableProcessor {
        protected int mMaxSize = 0;

        public TableProcessor() {
        }

        public boolean setMaxSize(int size) {
            return false;
        }

        public boolean addorUpdateRecord(Bundle data) {
            return false;
        }

        public ArrayList<Bundle> getAllRecords(Bundle info) {
            if (info == null) {
                return getAllRecords();
            }
            return null;
        }

        public int getSize(Bundle info) {
            int size;
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (info == null) {
                    try {
                        size = getSizeWithoutLock();
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    size = getSizeWithoutLock(info);
                }
            }
            return size;
        }

        /* access modifiers changed from: protected */
        public ArrayList<Bundle> getAllRecords() {
            return null;
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

    public static class UserPreferencesKey {
        public static final String AL = "AmbientLight";
        public static final String APPTYPE = "AppType";
        public static final String DELTA = "BackLightDelta";
        public static final String TAG = "UserPref";
        public static final String USERID = "UserID";
    }

    private class UserPreferencesTableProcessor extends TableProcessor {
        private static final int mMaxSegmentLength = 255;

        public UserPreferencesTableProcessor() {
            super();
        }

        private boolean pretreatmentForAddorUpdateRecord(Bundle data, int userID, int appType, int[] alValues, int[] deltaValues) {
            if (userID < 0 || appType < 0 || alValues == null || deltaValues == null || alValues.length != deltaValues.length || alValues.length > 255 || (this.mMaxSize > 0 && alValues.length > this.mMaxSize)) {
                if (alValues == null || deltaValues == null) {
                    DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.addorUpdateRecord error: al=null or delta=null");
                } else {
                    DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.addorUpdateRecord error: userID=" + userID + " appType=" + appType + " al size=" + alValues.length + " delta size=" + deltaValues.length + " max size=" + this.mMaxSize);
                }
                return false;
            } else if (clearWithoutLock(data)) {
                return true;
            } else {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.addorUpdateRecord(userID=" + userID + ", appType=" + appType + ") error: clear last records!");
                return false;
            }
        }

        public boolean addorUpdateRecord(Bundle data) {
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    if (data != null) {
                        int userID = data.getInt("UserID", -1);
                        int appType = data.getInt("AppType", -1);
                        int[] alValues = data.getIntArray("AmbientLight");
                        int[] deltaValues = data.getIntArray(UserPreferencesKey.DELTA);
                        if (!pretreatmentForAddorUpdateRecord(data, userID, appType, alValues, deltaValues)) {
                            return false;
                        }
                        try {
                            StringBuffer text = new StringBuffer();
                            text.append("UserPreferencesTableProcessor add record succ: userID=" + userID + ", appType=" + appType + ", segment={");
                            int id = (userID << 16) + (appType << 8);
                            for (int i = 0; i < alValues.length; i++) {
                                DisplayEngineDBManager.this.mDatabase.execSQL("INSERT INTO UserPreferences VALUES(?, ?, ?, ?, ?)", new Object[]{Integer.valueOf(id + i + 1), Integer.valueOf(userID), Integer.valueOf(appType), Integer.valueOf(alValues[i]), Integer.valueOf(deltaValues[i])});
                                text.append(alValues[i] + "," + deltaValues[i] + ";");
                            }
                            DElog.i(DisplayEngineDBManager.TAG, text.toString() + "}");
                            return true;
                        } catch (SQLException e) {
                            StringBuffer text2 = new StringBuffer();
                            text2.append("UserPreferencesTableProcessor add record userID=" + userID + ", appType=" + appType + ", segment={");
                            for (int i2 = 0; i2 < alValues.length; i2++) {
                                text2.append(alValues[i2] + "," + deltaValues[i2] + ";");
                            }
                            DElog.e(DisplayEngineDBManager.TAG, text2.toString() + "}, error:" + e.getMessage());
                            return false;
                        }
                    }
                }
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.addorUpdateRecord error: Invalid input!");
                return false;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x006a, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0098, code lost:
            if (r5 != null) goto L_0x009a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
            r5.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00bc, code lost:
            if (r5 == null) goto L_0x00bf;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c0, code lost:
            return r3;
         */
        public ArrayList<Bundle> getAllRecords(Bundle info) {
            if (info == null) {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getAllRecords invalid input: info=null");
                return null;
            }
            int userID = info.getInt("UserID", -1);
            int appType = info.getInt("AppType", -1);
            if (userID < 0 || appType < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getAllRecords invalid input: userID=" + userID + " appType=" + appType);
                return null;
            }
            ArrayList<Bundle> records = new ArrayList<>();
            synchronized (DisplayEngineDBManager.this.mdbLock) {
                if (!DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                    DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getAllRecords() mDatabase error.");
                    return null;
                }
                Cursor c = null;
                try {
                    c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM UserPreferences WHERE USERID = ? AND APPTYPE = ?", new String[]{String.valueOf(userID), String.valueOf(appType)});
                    if (c == null) {
                        DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getAllRecords() query database error.");
                        if (c != null) {
                            c.close();
                        }
                    } else {
                        while (c.moveToNext()) {
                            Bundle record = new Bundle();
                            record.putInt("AmbientLight", c.getInt(c.getColumnIndex("AL")));
                            record.putInt(UserPreferencesKey.DELTA, c.getInt(c.getColumnIndex("DELTA")));
                            records.add(record);
                        }
                    }
                } catch (SQLException e) {
                    records = null;
                    try {
                        DElog.w(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getAllRecords() error:" + e.getMessage());
                    } catch (Throwable th) {
                        if (c != null) {
                            c.close();
                        }
                        throw th;
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0074, code lost:
            if (r4 != null) goto L_0x0076;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0076, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0097, code lost:
            if (r4 == null) goto L_0x00a8;
         */
        public int getSizeWithoutLock(Bundle info) {
            if (info == null) {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() invalid input: info=null");
                return 0;
            }
            int userID = info.getInt("UserID", -1);
            int appType = info.getInt("AppType", -1);
            if (userID < 0 || appType < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() invalid input: userID=" + userID + " appType=" + appType);
                return 0;
            }
            int size = 0;
            if (DisplayEngineDBManager.this.mDatabase == null || !DisplayEngineDBManager.this.mDatabase.isOpen()) {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() mDatabase error.");
            } else {
                Cursor c = null;
                try {
                    c = DisplayEngineDBManager.this.mDatabase.rawQuery("SELECT * FROM UserPreferences where (USERID = ?) and (APPTYPE = ?)", new String[]{String.valueOf(userID), String.valueOf(appType)});
                    if (c != null && c.getCount() > 0) {
                        size = c.getCount();
                    }
                    DElog.i(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() return " + size);
                } catch (SQLException e) {
                    DElog.w(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.getSizeWithoutLock() error:" + e.getMessage());
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                    throw th;
                }
            }
            return size;
        }

        /* access modifiers changed from: protected */
        public boolean clearWithoutLock(Bundle info) {
            if (info == null) {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() invalid input: info=null");
                return false;
            }
            int userID = info.getInt("UserID", -1);
            int appType = info.getInt("AppType", -1);
            if (userID < 0 || appType < 0) {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() invalid input: userID=" + userID + " appType=" + appType);
                return false;
            } else if (!DisplayEngineDBManager.this.checkDatabaseStatusIsOk()) {
                DElog.e(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() mDatabase error.");
                return false;
            } else {
                boolean ret = false;
                try {
                    if (getSizeWithoutLock(info) > 0) {
                        DisplayEngineDBManager.this.mDatabase.execSQL("DELETE FROM UserPreferences where (USERID = ?) and (APPTYPE = ?)", new Object[]{Integer.valueOf(userID), Integer.valueOf(appType)});
                    }
                    DElog.i(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() sucess.");
                    ret = true;
                } catch (SQLException e) {
                    DElog.w(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() error:" + e.getMessage());
                } catch (IllegalArgumentException e2) {
                    DElog.w(DisplayEngineDBManager.TAG, "UserPreferencesTableProcessor.clearWithoutLock() error:" + e2.getMessage());
                }
                return ret;
            }
        }
    }

    private DisplayEngineDBManager(Context context) {
        mTableProcessors.put(DragInformationKey.TAG, new DragInformationTableProcessor());
        mTableProcessors.put(UserPreferencesKey.TAG, new UserPreferencesTableProcessor());
        mTableProcessors.put("BrightnessCurveLow", new BrightnessCurveTableProcessor("BrightnessCurveLow"));
        mTableProcessors.put("BrightnessCurveMiddle", new BrightnessCurveTableProcessor("BrightnessCurveMiddle"));
        mTableProcessors.put("BrightnessCurveHigh", new BrightnessCurveTableProcessor("BrightnessCurveHigh"));
        mTableProcessors.put("BrightnessCurveDefault", new BrightnessCurveTableProcessor("BrightnessCurveDefault"));
        mTableProcessors.put("AlgorithmESCW", new AlgorithmESCWTableProcessor());
        mTableProcessors.put("DataCleaner", new DataCleanerTableProcessor());
        this.mHelper = new DisplayEngineDBHelper(context);
        openDatabase();
    }

    public static DisplayEngineDBManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new DisplayEngineDBManager(context);
                }
            }
        }
        return mInstance;
    }

    public boolean setMaxSize(String name, int size) {
        TableProcessor processor = mTableProcessors.get(name);
        if (processor != null && size > 0) {
            return processor.setMaxSize(size);
        }
        DElog.e(TAG, "Invalid input for setMaxSize(" + name + ") size=" + size);
        return false;
    }

    public boolean addorUpdateRecord(String name, Bundle data) {
        TableProcessor processor = mTableProcessors.get(name);
        if (processor != null && data != null) {
            return processor.addorUpdateRecord(data);
        }
        DElog.e(TAG, "Invalid input for addorUpdateRecord:" + name + " is not support or data is null!");
        return false;
    }

    public int getSize(String name, Bundle info) {
        TableProcessor processor = mTableProcessors.get(name);
        if (processor != null) {
            return processor.getSize(info);
        }
        DElog.e(TAG, "Invalid input for getSize:" + name + " is not support!");
        return 0;
    }

    public int getSize(String name) {
        return getSize(name, null);
    }

    public ArrayList<Bundle> getAllRecords(String name, Bundle info) {
        TableProcessor processor = mTableProcessors.get(name);
        if (processor != null) {
            return processor.getAllRecords(info);
        }
        DElog.e(TAG, "Invalid input for getAllRecords:" + name + " is not support!");
        return null;
    }

    public ArrayList<Bundle> getAllRecords(String name) {
        return getAllRecords(name, null);
    }

    private void openDatabase() {
        try {
            this.mDatabase = this.mHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            DElog.e(TAG, "Failed to open DisplayEngine.db error:" + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public boolean checkDatabaseStatusIsOk() {
        if (this.mDatabase == null || !this.mDatabase.isOpen()) {
            openDatabase();
        }
        return this.mDatabase != null && this.mDatabase.isOpen();
    }
}
