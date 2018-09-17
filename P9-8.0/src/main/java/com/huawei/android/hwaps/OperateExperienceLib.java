package com.huawei.android.hwaps;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.hwaps.DataInfo.ApsDeleteDataInfo;
import com.huawei.android.hwaps.DataInfo.ApsInsertDataInfo;
import com.huawei.android.hwaps.DataInfo.ApsUpdateDataInfo;
import java.util.ArrayList;
import java.util.HashMap;

public class OperateExperienceLib {
    private static final String APS_APP_INFO = "content://com.huawei.android.hwaps.ApsProvider/APSAPPINFO";
    private static final String APS_DELETE_DATA_ACTION = "huawei.intent.action.APS_DELETE_DATA_ACTION";
    private static final String APS_DELETE_DATA_INFO = "ApsDeleteDataInfo";
    private static final String APS_INSERT_DATA_ACTION = "huawei.intent.action.APS_INSERT_DATA_ACTION";
    private static final String APS_INSERT_DATA_INFO = "ApsInsertDataInfo";
    private static final String APS_UPDATE_DATA_ACTION = "huawei.intent.action.APS_UPDATE_DATA_ACTION";
    private static final String APS_UPDATE_DATA_INFO = "ApsUpdateDataInfo";
    private static final int CONFIGTYPE_BLACKLIST = 7000;
    private static final int CONFIGTYPE_WHITELIST = 9998;
    private static final int CUR_CTRL_DURATION = 1;
    private static final int DEFAULT_CTRL_BATTERY = 20;
    private static final int DEFAULT_MAX_NONPLAY_FPS = 55;
    private static final int DEFAULT_MAX_PLAYING_FPS = 55;
    private static final int DEFAULT_MIN_NONPLAY_FPS = 30;
    private static final int DEFAULT_MIN_PLAYING_FPS = 30;
    private static final String GAMES_DURATION = "content://com.huawei.android.hwaps.ApsProvider/GamesDuration";
    private static final String GAMES_INTERVAL = "content://com.huawei.android.hwaps.ApsProvider/GamesInterval";
    private static final String GAMES_PLAY_INFO = "content://com.huawei.android.hwaps.ApsProvider/GamesPlayInfo";
    private static final String GAME_ROUND_DURATION = "content://com.huawei.android.hwaps.ApsProvider/GameRoundsDuration";
    private static final int LAST_CTRL_BEGIN_DATE = 3;
    private static final int LAST_CTRL_DURATION = 2;
    private static final int LAST_STUDY_BEGIN_DATE = 4;
    private static final String MAX_GAMES_DURATION = "content://com.huawei.android.hwaps.ApsProvider/GamesDuration/Max";
    private static final String MAX_GAMES_INTERVAL = "content://com.huawei.android.hwaps.ApsProvider/GamesInterval/Max";
    private static final String MAX_GAME_ROUND_DURATION = "content://com.huawei.android.hwaps.ApsProvider/GameRoundsDuration/Max";
    private static final String MIN_GAMES_DURATION = "content://com.huawei.android.hwaps.ApsProvider/GamesDuration/Min";
    private static final String MIN_GAMES_INTERVAL = "content://com.huawei.android.hwaps.ApsProvider/GamesInterval/Min";
    private static final String MIN_GAME_ROUND_DURATION = "content://com.huawei.android.hwaps.ApsProvider/GameRoundsDuration/Min";
    private static final String POWER_MODE_CHANGED_ACTION = "huawei.intent.action.POWER_MODE_CHANGED_ACTION";
    private static final String QUERY_RESULT_GAME_LIST = "content://com.huawei.android.hwaps.ApsProvider/QueryResultGameList";
    private static final int STAND_FEED_BACK_COUNT = 5;
    private static final String TAG = "Hwaps";
    private ArrayList<Integer> mArrayListFps = new ArrayList();
    private ArrayList<Float> mArrayListRatio = new ArrayList();
    private Context mContext;
    private int mCtrlBattery = 20;
    private long mCurCtrlDuration = 0;
    private long[] mDaysOfYear = new long[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private long mGameBeginDate = 0;
    private int mGameType;
    private Uri mGamesPlayInfoUri = Uri.parse(GAMES_PLAY_INFO);
    private int mGeneralFeedbackCount = 0;
    private boolean mIsPlayInfoExistInDataBase = false;
    private long mLastCtrlBeginDate = 0;
    private long mLastCtrlDuration = 0;
    private long mLastStudyBeginDate = 0;
    private int mMaxFPS = 55;
    private int mMaxNonplayFps = 55;
    private int mMinFPS = 30;
    private int mMinNonplayFps = 30;
    private float mMinSdrRatio = 1.0f;
    private String mPackageName = null;
    private ContentResolver mResolver;
    private Uri mUri = Uri.parse(APS_APP_INFO);

    public void initApsLibrary(Context context) {
        this.mPackageName = context.getPackageName();
        this.mResolver = context.getContentResolver();
        this.mContext = context;
    }

    public void insertData(String uri, HashMap values) {
        if (uri != null) {
            ApsInsertDataInfo dataInfo = new ApsInsertDataInfo(uri, values);
            Intent intent = new Intent(APS_INSERT_DATA_ACTION);
            intent.putExtra(APS_INSERT_DATA_INFO, dataInfo);
            this.mContext.sendBroadcast(intent);
            ApsCommon.logI(TAG, "send insert data broadcast, info:" + dataInfo);
        }
    }

    public void updateData(String uri, HashMap values) {
        if (uri != null) {
            String strWhere = "";
            if (uri.equals(APS_APP_INFO)) {
                strWhere = "name = '" + this.mPackageName + "'";
            } else {
                strWhere = "PkgName = '" + this.mPackageName + "'";
            }
            ApsUpdateDataInfo dataInfo = new ApsUpdateDataInfo(uri, values, strWhere, null);
            Intent intent = new Intent(APS_UPDATE_DATA_ACTION);
            intent.putExtra(APS_UPDATE_DATA_INFO, dataInfo);
            this.mContext.sendBroadcast(intent);
            ApsCommon.logI(TAG, "send insert data broadcast, info:" + dataInfo);
        }
    }

    public void deleteData(String uri) {
        if (uri != null) {
            String strWhere = "";
            if (uri.equals(APS_APP_INFO)) {
                strWhere = "name = '" + this.mPackageName + "'";
            } else {
                strWhere = "PkgName = '" + this.mPackageName + "'";
            }
            ApsDeleteDataInfo dataInfo = new ApsDeleteDataInfo(uri, strWhere, null);
            Intent intent = new Intent(APS_DELETE_DATA_ACTION);
            intent.putExtra(APS_DELETE_DATA_INFO, dataInfo);
            this.mContext.sendBroadcast(intent);
            ApsCommon.logI(TAG, "send insert data broadcast, info:" + dataInfo);
        }
    }

    private void insertAppInfo() {
        HashMap<String, Object> values = new HashMap(6);
        values.put("name", this.mPackageName);
        values.put("type", Integer.valueOf(this.mGameType));
        values.put("max", Integer.valueOf(this.mMaxFPS));
        values.put("min", Integer.valueOf(this.mMinFPS));
        values.put("glmax", Integer.valueOf(55));
        values.put("glmin", Integer.valueOf(this.mMinNonplayFps));
        insertData(APS_APP_INFO, values);
    }

    private void updateAppInfo() {
        HashMap<String, Object> values = new HashMap(6);
        values.put("name", this.mPackageName);
        values.put("type", Integer.valueOf(this.mGameType));
        values.put("max", Integer.valueOf(this.mMaxFPS));
        values.put("min", Integer.valueOf(this.mMinFPS));
        values.put("glmax", Integer.valueOf(55));
        values.put("glmin", Integer.valueOf(this.mMinNonplayFps));
        updateData(APS_APP_INFO, values);
    }

    public void updateGamePlayInfo() {
        HashMap<String, Object> values = new HashMap(6);
        values.put("PkgName", this.mPackageName);
        values.put("CurCtrlDuration", Long.valueOf(this.mCurCtrlDuration));
        values.put("LastCtrlDuration", Long.valueOf(this.mLastCtrlDuration));
        values.put("LastCtrlBeginTime", Long.valueOf(this.mLastCtrlBeginDate));
        values.put("LastStudyBeginTime", Long.valueOf(this.mLastStudyBeginDate));
        values.put("GeneralFeedbackCount", Integer.valueOf(this.mGeneralFeedbackCount));
        updateData(GAMES_PLAY_INFO, values);
    }

    public void insertGameRoundDuration(long roundDuration) {
        HashMap<String, Object> values = new HashMap(3);
        values.put("PkgName", this.mPackageName);
        values.put("SysTime", Long.valueOf(System.currentTimeMillis() / 1000));
        values.put("RoundsDuration", Long.valueOf(roundDuration));
        insertData(GAME_ROUND_DURATION, values);
    }

    private void insertGamePlayDuration(long playDuration) {
        HashMap<String, Object> values = new HashMap(3);
        values.put("PkgName", this.mPackageName);
        values.put("SysTime", Long.valueOf(System.currentTimeMillis() / 1000));
        values.put("GamesDuration", Long.valueOf(playDuration));
        insertData(GAMES_DURATION, values);
    }

    private void insertGameInteval(long inteval) {
        HashMap<String, Object> values = new HashMap(3);
        values.put("PkgName", this.mPackageName);
        values.put("SysTime", Long.valueOf(System.currentTimeMillis() / 1000));
        values.put("GamesInterval", Long.valueOf(inteval));
        insertData(GAMES_INTERVAL, values);
    }

    private void insertGamePlayInfo() {
        HashMap<String, Object> values = new HashMap(6);
        values.put("PkgName", this.mPackageName);
        values.put("CurCtrlDuration", Long.valueOf(this.mCurCtrlDuration));
        values.put("LastCtrlDuration", Long.valueOf(this.mLastCtrlDuration));
        values.put("LastCtrlBeginTime", Long.valueOf(this.mLastCtrlBeginDate));
        values.put("LastStudyBeginTime", Long.valueOf(this.mLastStudyBeginDate));
        values.put("GeneralFeedbackCount", Integer.valueOf(this.mGeneralFeedbackCount));
        insertData(GAMES_PLAY_INFO, values);
        this.mIsPlayInfoExistInDataBase = true;
    }

    public void saveGamePlayInfo(long time, int type) {
        switch (type) {
            case 1:
                this.mCurCtrlDuration = time;
                break;
            case 2:
                this.mLastCtrlDuration = time;
                break;
            case 3:
                this.mLastCtrlBeginDate = time;
                this.mLastStudyBeginDate = 0;
                break;
            case 4:
                this.mLastStudyBeginDate = time;
                this.mLastCtrlBeginDate = 0;
                break;
            case 5:
                this.mGeneralFeedbackCount = (int) time;
                break;
        }
        if (this.mIsPlayInfoExistInDataBase) {
            updateGamePlayInfo();
        } else {
            insertGamePlayInfo();
        }
    }

    public void saveAPSResult(int gameType, int maxFPS, int minFPS) {
        this.mGameType = gameType;
        this.mMaxFPS = maxFPS;
        this.mMinFPS = minFPS;
        if (isGameInfoExist()) {
            updateAppInfo();
        } else {
            insertAppInfo();
        }
    }

    public void saveMinFps(int nonplayFps, int playingFps) {
        ApsCommon.logD(TAG, "APS:save min of nonplay and playing");
        this.mMinFPS = playingFps;
        this.mMinNonplayFps = nonplayFps;
        if (isGameInfoExist()) {
            updateAppInfo();
        } else {
            insertAppInfo();
        }
    }

    private String getNameWithoutPlatName(String pkgName) {
        if (pkgName == null) {
            return null;
        }
        String strNameWithoutPlatName = pkgName;
        String[] tmpNames = pkgName.split("\\.");
        StringBuilder sb = new StringBuilder();
        if (tmpNames.length >= 4) {
            for (int i = 0; i < tmpNames.length - 1; i++) {
                sb.append(tmpNames[i]);
                if (i < tmpNames.length - 2) {
                    sb.append(".");
                }
            }
            strNameWithoutPlatName = sb.toString();
        }
        ApsCommon.logI(TAG, "APS: pkgName=" + pkgName + "  namewithoutPlatName=" + strNameWithoutPlatName);
        return strNameWithoutPlatName;
    }

    private boolean isGameInfoExist() {
        if (this.mResolver == null) {
            return false;
        }
        Cursor cursor = this.mResolver.query(this.mUri, null, "name = '" + this.mPackageName + "'", null, null);
        if (cursor == null) {
            return false;
        }
        boolean ret = cursor.moveToFirst();
        cursor.close();
        return ret;
    }

    public boolean query() {
        return queryByName(this.mPackageName);
    }

    public boolean queryByName(String name) {
        ApsCommon.logD(TAG, "query name : " + name);
        Cursor cursor = null;
        try {
            if (this.mResolver != null) {
                cursor = this.mResolver.query(this.mUri, new String[]{"type", "max", "min", "glmax", "glmin", "r_max", "r_min", "battery", "fps1", "ratio1", "fps2", "ratio2", "fps3", "ratio3", "fps4", "ratio4", "fps5", "ratio5"}, "name = '" + name + "'", null, null);
                if (cursor == null) {
                    Log.e(TAG, "cursor is null");
                    return false;
                } else if (cursor.moveToFirst()) {
                    this.mGameType = cursor.getInt(0);
                    this.mMaxFPS = cursor.getInt(1);
                    this.mMinFPS = cursor.getInt(2);
                    this.mMinNonplayFps = cursor.getInt(4);
                    this.mMinSdrRatio = cursor.getFloat(6);
                    this.mCtrlBattery = cursor.getInt(7);
                    int sceneNum = SystemProperties.getInt("debug.aps.scene_num", 0);
                    this.mArrayListFps.clear();
                    this.mArrayListRatio.clear();
                    for (int i = 0; i < sceneNum; i++) {
                        int fps = cursor.getInt(((i * 2) + 7) + 1);
                        float ratio = cursor.getFloat(((i * 2) + 7) + 2);
                        this.mArrayListFps.add(Integer.valueOf(fps));
                        this.mArrayListRatio.add(Float.valueOf(ratio));
                    }
                    ApsCommon.logI(TAG, "APS: query----beginquery data: name:" + name + "  type:" + this.mGameType + "  max:" + this.mMaxFPS + "  min:" + this.mMinFPS + " mMaxNonplayFps:" + this.mMaxNonplayFps + " mMinNonplayFps:" + this.mMinNonplayFps + " mMinSdrRatio:" + this.mMinSdrRatio + " mCtrlBattery = " + this.mCtrlBattery);
                    cursor.close();
                    return true;
                } else {
                    Log.e(TAG, "no data in database of APSAPPINFO");
                    cursor.close();
                    return false;
                }
            }
            Log.e(TAG, "resolver is null");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "DATA IS ERROR");
            if (cursor != null) {
                cursor.close();
            }
            return false;
        }
    }

    public static String[] getCustAppList(Context context, int type) {
        Throwable th;
        String[] projectid = new String[]{" name "};
        ArrayList<String> custAppList = new ArrayList();
        Cursor cursor = null;
        Cursor tempCursor = null;
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse(APS_APP_INFO);
        if (resolver != null) {
            String selection = null;
            if (type == CONFIGTYPE_BLACKLIST) {
                try {
                    selection = "type = '9998' and (r_min <= 0 or r_min >= 1)";
                } catch (Throwable th2) {
                    th = th2;
                }
            } else if (type == CONFIGTYPE_WHITELIST) {
                String selection2 = "type = '9998' and (r_min > 0 and r_min < 1)";
                try {
                    tempCursor = resolver.query(uri, null, "_id = 1", null, null);
                    if (tempCursor != null) {
                        if (tempCursor.moveToFirst()) {
                            int tempIndex = tempCursor.getColumnIndex("switch_level");
                            if (-1 != tempIndex) {
                                selection = selection2 + " and (switch_level = 0)";
                                ApsCommon.logD(TAG, "SDR: customized getCustAppList: tempCursor.getColumnIndex(\"switch_level\"): " + tempIndex);
                            }
                        }
                        selection = selection2;
                    } else {
                        selection = selection2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (tempCursor != null) {
                        tempCursor.close();
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            } else {
                Log.e(TAG, " SDR: customized getCustAppList: Error : the type is out of range!");
            }
            cursor = resolver.query(uri, projectid, selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(0);
                    custAppList.add(name);
                    ApsCommon.logD(TAG, " SDR: customized getCustAppList: query  name : " + name);
                } while (cursor.moveToNext());
            }
        }
        if (tempCursor != null) {
            tempCursor.close();
        }
        if (cursor != null) {
            cursor.close();
        }
        String[] custAppArray = new String[custAppList.size()];
        custAppList.toArray(custAppArray);
        return custAppArray;
    }

    public static String[] getQueryResultGameList(Context context, int type) {
        String[] projection = new String[]{"name"};
        ArrayList<String> gameList = new ArrayList();
        Cursor cursor = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null) {
                cursor = resolver.query(Uri.parse(QUERY_RESULT_GAME_LIST), projection, "type = " + type, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String name = cursor.getString(0);
                        gameList.add(name);
                        ApsCommon.logD(TAG, " SDR: customized getQueryResultGameList: query  name : " + name);
                    } while (cursor.moveToNext());
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            String[] outArray = new String[gameList.size()];
            gameList.toArray(outArray);
            return outArray;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean queryGamePlayInfo() {
        try {
            if (this.mResolver != null) {
                Cursor cursor = this.mResolver.query(this.mGamesPlayInfoUri, null, " PkgName = '" + this.mPackageName + "'", null, null);
                if (cursor == null) {
                    this.mIsPlayInfoExistInDataBase = false;
                    Log.e(TAG, "cursor is null");
                    return false;
                }
                this.mIsPlayInfoExistInDataBase = true;
                if (cursor.moveToFirst()) {
                    this.mIsPlayInfoExistInDataBase = true;
                    String name = cursor.getString(1);
                    this.mCurCtrlDuration = cursor.getLong(2);
                    this.mLastCtrlDuration = cursor.getLong(3);
                    this.mLastCtrlBeginDate = cursor.getLong(4);
                    this.mLastStudyBeginDate = cursor.getLong(5);
                    this.mGeneralFeedbackCount = cursor.getInt(6);
                    ApsCommon.logD(TAG, "query data: name:" + name + "  mLastCtrlDuration:" + this.mLastCtrlDuration + " mLastCtrlBeginDate=" + this.mLastCtrlBeginDate + " mLastStudyBeginDate=" + this.mLastStudyBeginDate + "  mGeneralFeedbackCount=" + this.mGeneralFeedbackCount);
                    return true;
                }
                this.mIsPlayInfoExistInDataBase = false;
                Log.e(TAG, "no data in database when query game play info");
                cursor.close();
                return false;
            }
            Log.e(TAG, "resolver is null");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "DATA IS ERROR");
            return false;
        }
    }

    public long queryGameTimeInfo(Uri uri) {
        long queryResult = 0;
        try {
            if (this.mResolver != null) {
                Cursor cursor = this.mResolver.query(uri, null, " PkgName = '" + this.mPackageName + "'", null, null);
                if (cursor == null) {
                    Log.e(TAG, "cursor is null");
                    return 0;
                } else if (cursor.moveToFirst()) {
                    String name = cursor.getString(1);
                    queryResult = cursor.getLong(2);
                    ApsCommon.logD(TAG, "query data: name:" + name + "  ime:" + queryResult);
                } else {
                    Log.e(TAG, "no data in database when query game time info");
                    cursor.close();
                    return 0;
                }
            }
            Log.e(TAG, "resolver is null");
        } catch (Exception e) {
            Log.e(TAG, "DATA IS ERROR");
        }
        return queryResult;
    }

    public int queryCount(Uri uri) {
        String[] projectid = new String[]{" count(_id) "};
        int count = 0;
        try {
            if (this.mResolver != null) {
                Cursor cursor = this.mResolver.query(uri, projectid, " PkgName = '" + this.mPackageName + "'", null, null);
                if (cursor == null) {
                    Log.e(TAG, "cursor is null");
                    return 0;
                } else if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                    ApsCommon.logD(TAG, "query data count: name:" + this.mPackageName + "  count:" + count);
                } else {
                    Log.e(TAG, "no data in database when query game time count");
                    return 0;
                }
            }
            Log.e(TAG, "resolver is null");
        } catch (Exception e) {
            Log.e(TAG, "DATA IS ERROR");
        }
        return count;
    }

    public int getGameType() {
        return this.mGameType;
    }

    public int getMaxFps() {
        return this.mMaxFPS;
    }

    public int getMinFps() {
        return this.mMinFPS;
    }

    public int getMinNonplayFps() {
        return this.mMinNonplayFps;
    }

    public int getMaxNonplayFps() {
        return this.mMaxNonplayFps;
    }

    public float getMinSdrRatio() {
        return this.mMinSdrRatio;
    }

    public ArrayList getSceneFps() {
        return this.mArrayListFps;
    }

    public ArrayList getSceneRatio() {
        return this.mArrayListRatio;
    }

    public int getCtrlBattery() {
        return this.mCtrlBattery;
    }

    public int getStandFeedbackCount() {
        return this.mGeneralFeedbackCount;
    }

    public long getLastStudyBeginDate() {
        return this.mLastStudyBeginDate;
    }

    public long getLastCtrlBeginDate() {
        return this.mLastCtrlBeginDate;
    }

    public long getCtrlPlayDuration() {
        long flag = this.mCurCtrlDuration % 10;
        long playDuration = this.mCurCtrlDuration / 10;
        if (0 == flag) {
            if (0 != playDuration && playDuration > 30) {
                insertGamePlayDuration(playDuration);
            }
            return 0;
        } else if (1 != flag) {
            return 0;
        } else {
            saveGamePlayInfo(0, 1);
            if (playDuration < 30) {
                playDuration = 0;
            }
            return playDuration;
        }
    }

    public long getCtrlGameInterval() {
        long gameInterval;
        if (0 != this.mLastCtrlBeginDate) {
            gameInterval = computeGameInterval(this.mGameBeginDate, this.mLastCtrlBeginDate);
            if (gameInterval <= 0) {
                gameInterval = 0;
            }
            return gameInterval;
        } else if (0 == this.mLastStudyBeginDate) {
            return 0;
        } else {
            gameInterval = computeGameInterval(this.mGameBeginDate, this.mLastStudyBeginDate);
            if (-1 != gameInterval) {
                insertGameInteval(gameInterval);
            }
            return 0;
        }
    }

    public long computeGameInterval(long curBeginDate, long LastBeginDate) {
        if ((curBeginDate / 10000) - 1 > LastBeginDate / 10000 || curBeginDate <= LastBeginDate) {
            return -1;
        }
        long curYear = curBeginDate / 10000;
        long lastYear = LastBeginDate / 10000;
        long curMonth = (curBeginDate % 10000) / 100;
        long lastMonth = (LastBeginDate % 10000) / 100;
        long curDays = curBeginDate % 100;
        long lastDays = LastBeginDate % 100;
        if (!isLeapYear(LastBeginDate / 1000)) {
            this.mDaysOfYear[1] = 28;
        }
        if (curYear == lastYear && curMonth == lastMonth) {
            return curDays - lastDays;
        }
        int i;
        for (i = 0; ((long) i) < lastMonth; i++) {
            lastDays += this.mDaysOfYear[i];
        }
        for (i = 0; ((long) i) < curMonth; i++) {
            curDays += this.mDaysOfYear[i];
        }
        if (curYear != lastYear) {
            curDays += 365;
        }
        if ((isLeapYear(curBeginDate / 1000) && curMonth > 2) || isLeapYear(LastBeginDate / 1000)) {
            curDays++;
        }
        Log.e(TAG, "curDays = " + curDays + "; lastDays = " + lastDays);
        return curDays - lastDays;
    }

    private boolean isLeapYear(long year) {
        return (0 == year % 4 && 0 != year % 100) || 0 == year % 400;
    }

    public void setGameBeginDate(long date) {
        this.mGameBeginDate = date;
    }
}
