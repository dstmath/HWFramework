package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.SpaceExpInfo;
import com.android.server.hidata.wavemapping.service.HwHistoryQoeResourceBuilder;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SpaceUserDao {
    private static final String COMMA = ", ";
    private static final int DEFAULT_DIVISOR = 2;
    private static final int DEFAULT_LENGTH = 10;
    private static final int DEFAULT_TIME_UNIT = 1000;
    private static final String DOT = ".";
    private static final int INTERVAL_24HR = 86400000;
    private static final String KEY_AVG_SIGNAL = "AVG_SIGNAL";
    private static final String KEY_CELL_NUMBER = "CELL_NUM";
    private static final String KEY_DATA_RX = "DATA_RX";
    private static final String KEY_DATA_TX = "DATA_TX";
    private static final String KEY_DURATION_CONNECTED = "DURATION_CONNECTED";
    private static final String KEY_EXPIRE_DATE = "EXPDATE";
    private static final String KEY_FREQUENT_LOCATION_NAME = "FREQLOCNAME";
    private static final String KEY_GOOD_COUNT = "GOODCOUNT";
    private static final String KEY_MODEL_VERSION_ALL_AP = "MODEL_VER_ALLAP";
    private static final String KEY_MODEL_VERSION_MAIN_AP = "MODEL_VER_MAINAP";
    private static final String KEY_NAME = "NAME";
    private static final String KEY_NETWORK_FREQUENCY = "NETWORKFREQ";
    private static final String KEY_NETWORK_FREQ_COUNT = "NWFREQCNT";
    private static final String KEY_NETWORK_ID = "NETWORKID";
    private static final String KEY_NETWORK_ID_COUNT = "NWIDCNT";
    private static final String KEY_NETWORK_NAME = "NETWORKNAME";
    private static final String KEY_NETWORK_TYPE = "NW_TYPE";
    private static final String KEY_POOR_COUNT = "POORCOUNT";
    private static final String KEY_POWER_CONSUMPTION = "POWER_CONSUMPTION";
    private static final String KEY_REC = "REC";
    private static final String KEY_RECORD_COUNT = "RECORDCNT";
    private static final String KEY_RESULT_AVG_SIGNAL = "avg_signal";
    private static final String KEY_SIGNAL_VALUE = "SIGNAL_VALUE";
    private static final String KEY_SPACE_ID = "SPACEID";
    private static final String KEY_SPACE_ID_OF_MAIN = "SPACEIDMAIN";
    private static final String KEY_STATISTIC_APP = "app";
    private static final String KEY_STATISTIC_AVG_SIGNAL = "avgSignal";
    private static final String KEY_STATISTIC_CELL_NUMBER = "cell_num";
    private static final String KEY_STATISTIC_DATA_RX = "datarx";
    private static final String KEY_STATISTIC_DATA_TX = "datatx";
    private static final String KEY_STATISTIC_DURATION = "duration";
    private static final String KEY_STATISTIC_DURATION_CONNECTED = "duration_connected";
    private static final String KEY_STATISTIC_DURATION_OUT4G = "duration_out4g";
    private static final String KEY_STATISTIC_GOOD_COUNT = "goodcount";
    private static final String KEY_STATISTIC_MODEL_VERSION_ALL_AP = "modelVerAllap";
    private static final String KEY_STATISTIC_MODEL_VERSION_MAIN_AP = "modelVerMainap";
    private static final String KEY_STATISTIC_NETWORK_FREQ_COUNT = "nwfreqcnt";
    private static final String KEY_STATISTIC_NETWORK_ID_COUNT = "nwidcnt";
    private static final String KEY_STATISTIC_NETWORK_NAME = "networkname";
    private static final String KEY_STATISTIC_NETWORK_TYPE = "networktype";
    private static final String KEY_STATISTIC_NW_TYPE = "nwType";
    private static final String KEY_STATISTIC_POOR_COUNT = "poorcount";
    private static final String KEY_STATISTIC_POWER_CONSUMPTION = "powerconsumption";
    private static final String KEY_STATISTIC_REC = "rec";
    private static final String KEY_STATISTIC_SPACE_ID = "spaceid";
    private static final String KEY_STATISTIC_SPACE_ID_OF_MAIN = "spaceidmain";
    private static final String KEY_STATISTIC_TOTAL_DURATION = "total_duration";
    private static final String KEY_STATISTIC_USER_PREFERENCE_ENTRY = "user_pref_entery";
    private static final String KEY_STATISTIC_USER_PREFERENCE_OPTION_IN = "user_pref_opt_in";
    private static final String KEY_STATISTIC_USER_PREFERENCE_OPTION_OUT = "user_pref_opt_out";
    private static final String KEY_STATISTIC_USER_PREFERENCE_STAY = "user_pref_stay";
    private static final String KEY_SUBSCRIBE_ID = "SCRBID";
    private static final String KEY_SUM_DATA_RX = "SUM(DATA_RX)";
    private static final String KEY_SUM_DATA_TX = "SUM(DATA_TX)";
    private static final String KEY_SUM_DURATION = "SUM(DURATION)";
    private static final String KEY_SUM_DURATION_CONNECTED = "SUM(DURATION_CONNECTED)";
    private static final String KEY_SUM_GOOD_COUNT = "SUM(GOODCOUNT)";
    private static final String KEY_SUM_POOR_COUNT = "SUM(POORCOUNT)";
    private static final String KEY_SUM_POWER_CONSUMPTION = "SUM(POWER_CONSUMPTION)";
    private static final String KEY_SUM_USER_PREFERENCE_OPTION_IN = "SUM(USER_PREF_OPT_IN)";
    private static final String KEY_SUM_USER_PREFERENCE_OPTION_OUT = "SUM(USER_PREF_OPT_OUT)";
    private static final String KEY_SUM_USER_PREFERENCE_STAY = "SUM(USER_PREF_STAY)";
    private static final String KEY_SUM_USER_PREFERENCE_TOTAL_COUNT = "SUM(USER_PREF_TOTAL_COUNT)";
    private static final String KEY_TITLE_DAY_COUNT = "COUNT(DISTINCT UPDATE_DATE)";
    private static final String KEY_TITLE_DURATION = "SUM(\"DURATION\")";
    private static final String KEY_TITLE_FREQ_COUNT = "COUNT(\"NETWORKFREQ\")";
    private static final String KEY_TITLE_GOOD = "SUM(\"GOODCOUNT\")";
    private static final String KEY_TITLE_ID_COUNT = "COUNT(\"NETWORKID\")";
    private static final String KEY_TITLE_POOR = "SUM(\"POORCOUNT\")";
    private static final String KEY_TOTAL_DURATION = "TOTAL_DURATION";
    private static final String KEY_UPDATE = "UPDATE_DATE";
    private static final String KEY_USER_PREFERENCE_OPTION_IN = "USER_PREF_OPT_IN";
    private static final String KEY_USER_PREFERENCE_OPTION_OUT = "USER_PREF_OPT_OUT";
    private static final String KEY_USER_PREFERENCE_STAY = "USER_PREF_STAY";
    private static final String KEY_USER_PREFERENCE_TOTAL_COUNT = "USER_PREF_TOTAL_COUNT";
    private static final int LIST_DEFAULT_CAPACITY = 10;
    private static final int MAP_DEFAULT_CAPACITY = 16;
    private static final String SYMBOL_AS = " AS ";
    private static final String TAG = ("WMapping." + SpaceUserDao.class.getSimpleName());
    private static final String UNDER_LINE = "_";
    private static HwHistoryQoeResourceBuilder mQoeAppBuilder = null;
    private String curFreqLoc = Constant.NAME_FREQLOCATION_OTHER;
    private SQLiteDatabase db = DatabaseSingleton.getInstance();
    private int modelVerAllAp = 0;
    private int modelVerMainAp = 0;
    private String subscribeId = "NA";
    private String subscribeIdPrint = "NA";
    private long time1stClean = 0;

    public SpaceUserDao() {
        mQoeAppBuilder = HwHistoryQoeResourceBuilder.getInstance();
        this.time1stClean = System.currentTimeMillis();
    }

    public void setFreqLocation(String location) {
        if (location != null) {
            this.curFreqLoc = location;
            deleteOldRecords();
        }
    }

    public void setSubscribeId(String subscribeId2) {
        if (subscribeId2 != null) {
            this.subscribeId = subscribeId2;
            if (subscribeId2.length() > 10) {
                this.subscribeIdPrint = subscribeId2.substring(0, 10);
            } else {
                this.subscribeIdPrint = subscribeId2;
            }
            LogUtil.v(false, "setSubscribeId :%{public}s", this.subscribeIdPrint);
        }
    }

    public void setModelVer(int modelAllAp, int modelMainAp) {
        this.modelVerAllAp = modelAllAp;
        this.modelVerMainAp = modelMainAp;
    }

    public boolean insertBase(SpaceExpInfo spaceinfo) {
        if (networkIdFoundBaseBySpaceNetwork(spaceinfo.getSpaceId(), spaceinfo.getSpaceIdMain(), spaceinfo.getNetworkId(), spaceinfo.getNetworkName(), spaceinfo.getNetworkFreq()) != null) {
            return update(spaceinfo);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SUBSCRIBE_ID, this.subscribeId);
        contentValues.put(KEY_FREQUENT_LOCATION_NAME, this.curFreqLoc);
        contentValues.put(KEY_SPACE_ID, Integer.valueOf(spaceinfo.getSpaceId()));
        contentValues.put(KEY_SPACE_ID_OF_MAIN, Integer.valueOf(spaceinfo.getSpaceIdMain()));
        contentValues.put(KEY_NETWORK_NAME, spaceinfo.getNetworkName());
        contentValues.put(KEY_NETWORK_FREQUENCY, spaceinfo.getNetworkFreq());
        contentValues.put(KEY_NETWORK_ID, spaceinfo.getNetworkId());
        contentValues.put(KEY_SIGNAL_VALUE, Integer.valueOf(spaceinfo.getSignalValue()));
        contentValues.put(KEY_POWER_CONSUMPTION, Long.valueOf(spaceinfo.getPowerConsumption()));
        contentValues.put(KEY_DATA_RX, Long.valueOf(spaceinfo.getDataRx()));
        contentValues.put(KEY_DATA_TX, Long.valueOf(spaceinfo.getDataTx()));
        contentValues.put(KEY_USER_PREFERENCE_OPTION_IN, Integer.valueOf(spaceinfo.getUserPrefOptIn()));
        contentValues.put(KEY_USER_PREFERENCE_OPTION_OUT, Integer.valueOf(spaceinfo.getUserPrefOptOut()));
        contentValues.put(KEY_USER_PREFERENCE_STAY, Integer.valueOf(spaceinfo.getUserPrefStay()));
        contentValues.put(KEY_USER_PREFERENCE_TOTAL_COUNT, Integer.valueOf(spaceinfo.getUserPrefTotalCount()));
        contentValues.put(KEY_DURATION_CONNECTED, Long.valueOf(spaceinfo.getDuration()));
        contentValues.put(KEY_NETWORK_TYPE, Integer.valueOf(spaceinfo.getNetworkType()));
        contentValues.put(KEY_MODEL_VERSION_ALL_AP, Integer.valueOf(this.modelVerAllAp));
        contentValues.put(KEY_MODEL_VERSION_MAIN_AP, Integer.valueOf(this.modelVerMainAp));
        LogUtil.i(false, "insert BASE, FreqLoc:%{public}s, %{public}s, modelVer:%{public}d_%{public}d", this.curFreqLoc, spaceinfo.toString(), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
        LogUtil.v(false, "insert BASE, scrib ID:%{public}s", this.subscribeIdPrint);
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.SPACEUSER_TABLE_NAME, null, contentValues);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            if (this.time1stClean == 0) {
                this.time1stClean = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - this.time1stClean > Constant.MILLISEC_ONE_DAY) {
                deleteOldRecords();
            }
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "insert failed by Exception", new Object[0]);
            this.db.endTransaction();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r10v1, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r10v2 */
    /* JADX WARN: Type inference failed for: r10v3 */
    /* JADX WARN: Type inference failed for: r10v4 */
    /* JADX WARN: Type inference failed for: r10v5 */
    /* JADX WARN: Type inference failed for: r10v6 */
    /* JADX WARN: Type inference failed for: r10v8 */
    public boolean insertApp(SpaceExpInfo spaceInfo) {
        int i;
        int poor;
        SQLiteDatabase sQLiteDatabase;
        SpaceExpInfo spaceExpInfo = spaceInfo;
        boolean isInserted = true;
        for (Map.Entry<Integer, Float> entry : mQoeAppBuilder.getQoeAppList().entrySet()) {
            String appName = Constant.USERDB_APP_NAME_PREFIX + entry.getKey();
            HashMap<String, Long> mapDuration = spaceInfo.getMapAppDuration();
            try {
                if (mapDuration.containsKey(appName)) {
                    long duration = mapDuration.get(appName).longValue();
                    if (duration > 0) {
                        int good = judgeAppQoeGood(appName, spaceExpInfo);
                        try {
                            poor = judgeAppQoePoor(appName, spaceExpInfo);
                        } catch (SQLException e) {
                            i = 0;
                            LogUtil.e(i, "insertApp failed by Exception", new Object[i]);
                            isInserted = false;
                            spaceExpInfo = spaceInfo;
                        }
                        try {
                            if (networkIdFoundAppSpaceNetwork(appName, spaceInfo.getSpaceId(), spaceInfo.getSpaceIdMain(), spaceInfo.getNetworkId(), spaceInfo.getNetworkName(), spaceInfo.getNetworkFreq()) != null) {
                                try {
                                    if (!updateApp(appName, duration, good, poor, spaceInfo)) {
                                        isInserted = false;
                                        updateFailed(appName, spaceExpInfo);
                                    }
                                } catch (SQLException e2) {
                                    i = 0;
                                    LogUtil.e(i, "insertApp failed by Exception", new Object[i]);
                                    isInserted = false;
                                    spaceExpInfo = spaceInfo;
                                }
                            } else {
                                try {
                                    ContentValues contentValues = new ContentValues();
                                    i = 0;
                                    try {
                                        insertValues(contentValues, spaceInfo, duration, poor, good);
                                        LogUtil.i(false, "insert APP:%{public}s,FreqLoc:%{public}s, duration:%{public}s, poor:%{public}d, good:%{public}d, modelVer:%{public}d_%{public}d", appName, this.curFreqLoc, String.valueOf(duration), Integer.valueOf(poor), Integer.valueOf(good), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
                                        LogUtil.v(false, "insert APP:%{public}s, subscribe ID:%{public}s", appName, this.subscribeIdPrint);
                                        try {
                                            this.db.beginTransaction();
                                            this.db.insert(appName, null, contentValues);
                                            this.db.setTransactionSuccessful();
                                            sQLiteDatabase = this.db;
                                        } catch (SQLException e3) {
                                            LogUtil.e(false, "db insert failed by Exception", new Object[0]);
                                            isInserted = false;
                                            sQLiteDatabase = this.db;
                                        } catch (Throwable th) {
                                            this.db.endTransaction();
                                            throw th;
                                        }
                                        sQLiteDatabase.endTransaction();
                                    } catch (SQLException e4) {
                                        LogUtil.e(i, "insertApp failed by Exception", new Object[i]);
                                        isInserted = false;
                                        spaceExpInfo = spaceInfo;
                                    }
                                } catch (SQLException e5) {
                                    i = 0;
                                    LogUtil.e(i, "insertApp failed by Exception", new Object[i]);
                                    isInserted = false;
                                    spaceExpInfo = spaceInfo;
                                }
                            }
                        } catch (SQLException e6) {
                            i = 0;
                            LogUtil.e(i, "insertApp failed by Exception", new Object[i]);
                            isInserted = false;
                            spaceExpInfo = spaceInfo;
                        }
                    }
                }
            } catch (SQLException e7) {
                i = 0;
                LogUtil.e(i, "insertApp failed by Exception", new Object[i]);
                isInserted = false;
                spaceExpInfo = spaceInfo;
            }
            spaceExpInfo = spaceInfo;
        }
        return isInserted;
    }

    private int judgeAppQoeGood(String appName, SpaceExpInfo spaceInfo) {
        if (spaceInfo.getMapAppQoeGood().containsKey(appName)) {
            return spaceInfo.getMapAppQoeGood().get(appName).intValue();
        }
        return 0;
    }

    private int judgeAppQoePoor(String appName, SpaceExpInfo spaceInfo) {
        if (spaceInfo.getMapAppQoePoor().containsKey(appName)) {
            return spaceInfo.getMapAppQoePoor().get(appName).intValue();
        }
        return 0;
    }

    private void updateFailed(String appName, SpaceExpInfo spaceInfo) {
        LogUtil.w(false, "updateApp APP fail:%{public}s,FreqLoc:%{public}s", appName, this.curFreqLoc);
        LogUtil.v(false, "updateApp APP fail, nwId:%{public}s", spaceInfo.getNetworkId());
    }

    private void insertValues(ContentValues contentValues, SpaceExpInfo spaceInfo, long duration, int poor, int good) {
        contentValues.put(KEY_SUBSCRIBE_ID, this.subscribeId);
        contentValues.put(KEY_FREQUENT_LOCATION_NAME, this.curFreqLoc);
        contentValues.put(KEY_SPACE_ID, Integer.valueOf(spaceInfo.getSpaceId()));
        contentValues.put(KEY_SPACE_ID_OF_MAIN, Integer.valueOf(spaceInfo.getSpaceIdMain()));
        contentValues.put(KEY_NETWORK_NAME, spaceInfo.getNetworkName());
        contentValues.put(KEY_NETWORK_FREQUENCY, spaceInfo.getNetworkFreq());
        contentValues.put(KEY_NETWORK_ID, spaceInfo.getNetworkId());
        contentValues.put(KEY_NETWORK_TYPE, Integer.valueOf(spaceInfo.getNetworkType()));
        contentValues.put(KEY_DURATION_CONNECTED, Long.valueOf(duration));
        contentValues.put("POORCOUNT", Integer.valueOf(poor));
        contentValues.put("GOODCOUNT", Integer.valueOf(good));
        contentValues.put(KEY_MODEL_VERSION_ALL_AP, Integer.valueOf(this.modelVerAllAp));
        contentValues.put(KEY_MODEL_VERSION_MAIN_AP, Integer.valueOf(this.modelVerMainAp));
    }

    public boolean update(SpaceExpInfo spaceinfo) {
        Object[] args = {Integer.valueOf(spaceinfo.getSignalValue()), Integer.valueOf(spaceinfo.getUserPrefOptIn()), Integer.valueOf(spaceinfo.getUserPrefOptOut()), Integer.valueOf(spaceinfo.getUserPrefStay()), Integer.valueOf(spaceinfo.getUserPrefTotalCount()), Long.valueOf(spaceinfo.getPowerConsumption()), Long.valueOf(spaceinfo.getDataRx()), Long.valueOf(spaceinfo.getDataTx()), Long.valueOf(spaceinfo.getDuration()), this.subscribeId, this.curFreqLoc, spaceinfo.getNetworkId(), spaceinfo.getNetworkName(), spaceinfo.getNetworkFreq(), Integer.valueOf(spaceinfo.getSpaceId()), Integer.valueOf(spaceinfo.getSpaceIdMain()), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp)};
        LogUtil.i(false, "update BASE begin: FreqLoc:%{public}s, modelVer:%{public}d_%{public}d, %{public}s", this.curFreqLoc, Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp), spaceinfo.toString());
        LogUtil.v(false, "update BASE begin: scribId:%{public}s", this.subscribeIdPrint);
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE SPACEUSER_BASE SET SIGNAL_VALUE = ?,USER_PREF_OPT_IN = ?,USER_PREF_OPT_OUT = ?,USER_PREF_STAY = ?,USER_PREF_TOTAL_COUNT = ?, POWER_CONSUMPTION = ?, DATA_RX = ?, DATA_TX = ?, DURATION_CONNECTED = ? WHERE SCRBID = ? AND FREQLOCNAME = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean updateApp(String appName, long duration, int good, int poor, SpaceExpInfo spaceinfo) {
        String sql = "UPDATE " + appName + " SET DURATION = ?,POORCOUNT = ?,GOODCOUNT = ? WHERE SCRBID = ? AND FREQLOCNAME = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))";
        Object[] args = {Long.valueOf(duration), Integer.valueOf(poor), Integer.valueOf(good), this.subscribeId, this.curFreqLoc, spaceinfo.getNetworkId(), spaceinfo.getNetworkName(), spaceinfo.getNetworkFreq(), Integer.valueOf(spaceinfo.getSpaceId()), Integer.valueOf(spaceinfo.getSpaceIdMain()), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp)};
        LogUtil.i(false, "update APP:%{public}s begin: FreqLoc:%{public}s, duration:%{public}s, poor:%{public}d, good:%{public}d, modelVer:%{public}d_%{public}d", appName, this.curFreqLoc, String.valueOf(duration), Integer.valueOf(poor), Integer.valueOf(good), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
        LogUtil.v(false, "update APP:%{public}s begin: scribId:%{public}s", appName, this.subscribeIdPrint);
        try {
            this.db.beginTransaction();
            this.db.execSQL(sql, args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean remove(int spaceIdOfAllAp, int spaceIdOfMainAp, String networkId) {
        try {
            this.db.execSQL("DELETE FROM SPACEUSER_BASE  WHERE FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND NETWORKID = ?", new Object[]{this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp), networkId});
            return true;
        } catch (SQLException e) {
            LogUtil.e(false, "remove exception: %{public}s", e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0087, code lost:
        if (0 == 0) goto L_0x008a;
     */
    public String networkIdFoundBaseBySpaceNetwork(int spaceIdOfAllAp, int spaceIdOfMainAp, String networkId, String networkName, String networkfreq) {
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(spaceIdOfMainAp), networkId, networkName, networkfreq, Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        Cursor cursor = null;
        String netId = null;
        String date = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return null;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT NETWORKID, UPDATE_DATE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))", args);
            if (cursor.moveToNext()) {
                netId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_ID));
                date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATE));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "networkIdFoundBaseBySpaceNetwork IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "networkIdFoundBaseBySpaceNetwork failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        if (netId != null) {
            LogUtil.i(false, "networkIdFoundBaseBySpaceNetwork, Date:%{public}s, FreqLoc:%{public}s, SPACEID:%{public}d, SPACEIDMAIN:%{public}d, modelVer:%{public}d_%{public}d", date, this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
            LogUtil.v(false, "networkIdFoundBaseBySpaceNetwork, netId:%{public}s, subscribeId:%{public}s", netId, this.subscribeIdPrint);
        } else {
            LogUtil.d(false, "networkIdFoundBaseBySpaceNetwork, NO DATA, sql:%{public}s, FreqLoc:%{public}s, SPACEID:%{public}d, SPACEIDMAIN:%{public}d, modelVer:%{public}d_%{public}d", "SELECT NETWORKID, UPDATE_DATE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))", this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
        }
        return netId;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x009d, code lost:
        if (0 == 0) goto L_0x00a0;
     */
    public String networkIdFoundAppSpaceNetwork(String appName, int spaceIdOfAllAp, int spaceIdOfMainAp, String networkId, String networkName, String networkFreq) {
        String sql = "SELECT NETWORKID, UPDATE_DATE FROM " + appName + " WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))";
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(spaceIdOfMainAp), networkId, networkName, networkFreq, Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        Cursor cursor = null;
        String netId = null;
        String date = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return null;
        }
        try {
            cursor = sQLiteDatabase.rawQuery(sql, args);
            if (cursor.moveToNext()) {
                netId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_ID));
                date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_UPDATE));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "networkIdFoundAppSpaceNetwork IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "networkIdFoundAppSpaceNetwork failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        if (netId != null) {
            LogUtil.i(false, "networkIdFoundAppSpaceNetwork, appName %{public}s, Date:%{public}s, FreqLoc:%{public}s, SPACEID:%{public}d, SPACEIDMAIN:%{public}d, modelVer:%{public}d_%{public}d", appName, date, this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
            LogUtil.v(false, "networkIdFoundAppSpaceNetwork, appName %{public}s, netId:%{public}s, subscribeId:%{public}s", appName, netId, this.subscribeIdPrint);
        } else {
            LogUtil.d(false, "networkIdFoundAppSpaceNetwork, NO DATA, sql:%{public}s, FreqLoc:%{public}s, SPACEID:%{public}d, SPACEIDMAIN:%{public}d, modelVer:%{public}d_%{public}d", sql, this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
        }
        return netId;
    }

    private String getAllAppTitleString() {
        StringBuffer sqlTitle = new StringBuffer(16);
        Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoeAppList().entrySet().iterator();
        while (it.hasNext()) {
            String appName = Constant.USERDB_APP_NAME_PREFIX + it.next().getKey();
            sqlTitle.append(COMMA);
            sqlTitle.append(appName + DOT + Constant.USERDB_APP_NAME_DURATION + SYMBOL_AS + appName + "_" + Constant.USERDB_APP_NAME_DURATION);
            sqlTitle.append(COMMA);
            sqlTitle.append(appName + DOT + "POORCOUNT" + SYMBOL_AS + appName + "_POORCOUNT");
            sqlTitle.append(COMMA);
            sqlTitle.append(appName + DOT + "GOODCOUNT" + SYMBOL_AS + appName + "_GOODCOUNT");
        }
        return sqlTitle.toString();
    }

    private String getAllAppJoinString() {
        StringBuffer sqlTitle = new StringBuffer(16);
        Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoeAppList().entrySet().iterator();
        while (it.hasNext()) {
            sqlTitle.append(" LEFT OUTER JOIN " + (Constant.USERDB_APP_NAME_PREFIX + it.next().getKey()) + " USING(SCRBID,FREQLOCNAME,SPACEID,SPACEIDMAIN,NETWORKID,NETWORKNAME,NETWORKFREQ,NW_TYPE,UPDATE_DATE)");
        }
        return sqlTitle.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00e6, code lost:
        if (0 == 0) goto L_0x00e9;
     */
    public HashMap<String, SpaceExpInfo> findAllByTwoSpaces(int spaceIdOfAllAp, int spaceIdOfMainAp) {
        String sqlAppTitle = getAllAppTitleString();
        String sqlAppJoin = getAllAppJoinString();
        String sql = "SELECT SPACEUSER_BASE.SPACEID,SPACEUSER_BASE.SPACEIDMAIN,SPACEUSER_BASE.NETWORKID,SPACEUSER_BASE.NETWORKNAME,SPACEUSER_BASE.NETWORKFREQ,SPACEUSER_BASE.NW_TYPE,SIGNAL_VALUE,USER_PREF_OPT_IN,USER_PREF_OPT_OUT,USER_PREF_STAY,USER_PREF_TOTAL_COUNT,POWER_CONSUMPTION,DURATION_CONNECTED" + sqlAppTitle + " FROM " + Constant.SPACEUSER_TABLE_NAME + sqlAppJoin + " WHERE SPACEUSER_BASE.SCRBID = ? AND SPACEUSER_BASE.FREQLOCNAME = ? AND SPACEUSER_BASE.SPACEID = ? AND SPACEUSER_BASE.SPACEIDMAIN = ? AND SPACEUSER_BASE.MODEL_VER_ALLAP = ? AND SPACEUSER_BASE.MODEL_VER_MAINAP = ? AND SPACEUSER_BASE.UPDATE_DATE = (date('now', 'localtime'))";
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(spaceIdOfMainAp), Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        Cursor cursor = null;
        HashMap<String, SpaceExpInfo> spaceInfoHashMap = new HashMap<>(16);
        LogUtil.i(false, " findAllByTwoSpaces: sql_comm=SELECT %{public}s", "SPACEUSER_BASE.SPACEID,SPACEUSER_BASE.SPACEIDMAIN,SPACEUSER_BASE.NETWORKID,SPACEUSER_BASE.NETWORKNAME,SPACEUSER_BASE.NETWORKFREQ,SPACEUSER_BASE.NW_TYPE,");
        LogUtil.i(false, " findAllByTwoSpaces: sql_base=%{public}s", "SIGNAL_VALUE,USER_PREF_OPT_IN,USER_PREF_OPT_OUT,USER_PREF_STAY,USER_PREF_TOTAL_COUNT,POWER_CONSUMPTION,DURATION_CONNECTED");
        LogUtil.i(false, " findAllByTwoSpaces: sql_appt=%{public}s", sqlAppTitle);
        LogUtil.i(false, " findAllByTwoSpaces: sql_from=FROM %{public}s", Constant.SPACEUSER_TABLE_NAME);
        LogUtil.i(false, " findAllByTwoSpaces: sql_appj=%{public}s", sqlAppJoin);
        LogUtil.i(false, " findAllByTwoSpaces: sql_cond=%{public}s", " WHERE SPACEUSER_BASE.SCRBID = ? AND SPACEUSER_BASE.FREQLOCNAME = ? AND SPACEUSER_BASE.SPACEID = ? AND SPACEUSER_BASE.SPACEIDMAIN = ? AND SPACEUSER_BASE.MODEL_VER_ALLAP = ? AND SPACEUSER_BASE.MODEL_VER_MAINAP = ? AND SPACEUSER_BASE.UPDATE_DATE = (date('now', 'localtime'))");
        LogUtil.v(false, " findAllByTwoSpaces: args=%{public}s", Arrays.toString(Arrays.copyOfRange(args, 1, args.length)));
        try {
            cursor = this.db.rawQuery(sql, args);
            while (cursor.moveToNext()) {
                processAllSpace(cursor, spaceInfoHashMap);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findAllByTwoSpaces IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAllByTwoSpaces failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return spaceInfoHashMap;
    }

    private void processAllSpace(Cursor cursor, HashMap<String, SpaceExpInfo> spaceInfoHashMap) {
        StringBuilder spaceId = new StringBuilder(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SPACE_ID)));
        StringBuilder spaceIdMain = new StringBuilder(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SPACE_ID_OF_MAIN)));
        HashMap<String, Long> durationApp = new HashMap<>(16);
        HashMap<String, Integer> qoeAppPoor = new HashMap<>(16);
        HashMap<String, Integer> qoeAppGood = new HashMap<>(16);
        Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoeAppList().entrySet().iterator();
        while (it.hasNext()) {
            String appName = Constant.USERDB_APP_NAME_PREFIX + it.next().getKey();
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(appName + "_" + Constant.USERDB_APP_NAME_DURATION));
            if (duration > 0) {
                durationApp.put(appName, Long.valueOf(duration));
                qoeAppPoor.put(appName, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(appName + "_POORCOUNT"))));
                qoeAppGood.put(appName, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(appName + "_GOODCOUNT"))));
            }
        }
        SpaceExpInfo spaceInfo = new SpaceExpInfo(spaceId, spaceIdMain, cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_ID)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_NAME)), cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_FREQUENCY)), qoeAppPoor, qoeAppGood, durationApp, 0, 0, 0, cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SIGNAL_VALUE)), (long) cursor.getInt(cursor.getColumnIndexOrThrow(KEY_POWER_CONSUMPTION)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_PREFERENCE_OPTION_IN)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_PREFERENCE_OPTION_OUT)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_PREFERENCE_STAY)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_PREFERENCE_TOTAL_COUNT)), cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DURATION_CONNECTED)), cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_TYPE)));
        LogUtil.i(false, " findAllByTwoSpaces:SPACEID:%{public}d,SPACEIDMAIN:%{public}d,FreqLoc:%{public}s", Integer.valueOf(spaceInfo.getSpaceId()), Integer.valueOf(spaceInfo.getSpaceIdMain()), this.curFreqLoc + spaceInfo.toString());
        spaceInfoHashMap.put(spaceInfo.getNetworkId(), spaceInfo);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0058, code lost:
        if (0 == 0) goto L_0x005b;
     */
    public void deleteOldRecords() {
        String[] args = {this.subscribeId, this.curFreqLoc};
        Cursor cursor = null;
        int recordsCnt = 0;
        String expireDate = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase != null) {
            try {
                cursor = sQLiteDatabase.rawQuery("SELECT COUNT(DISTINCT UPDATE_DATE) RECORDCNT, MIN(UPDATE_DATE) EXPDATE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ?", args);
                if (cursor.moveToNext()) {
                    recordsCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_RECORD_COUNT));
                    expireDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPIRE_DATE));
                }
            } catch (IllegalArgumentException e) {
                LogUtil.e(false, "networkIdFoundBaseBySpaceNetwork IllegalArgumentException: %{public}s", e.getMessage());
            } catch (SQLException e2) {
                LogUtil.e(false, "deleteOldRecords rawQuery failed by Exception", new Object[0]);
                if (0 != 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
            if (recordsCnt > 30) {
                LogUtil.i(false, "deleteOldRecords: current records exceed (%{public}d) days", 30);
                try {
                    this.db.execSQL("DELETE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND UPDATE_DATE <= ?", new String[]{this.subscribeId, this.curFreqLoc, expireDate});
                } catch (IllegalArgumentException e3) {
                    LogUtil.e(false, "networkIdFoundBaseBySpaceNetwork IllegalArgumentException: %{public}s", e3.getMessage());
                } catch (SQLException e4) {
                    LogUtil.e(false, "deleteOldRecords execSQL failed by Exception", new Object[0]);
                }
            } else {
                LogUtil.i(false, "deleteOldRecords: keep data", new Object[0]);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00a6, code lost:
        if (0 == 0) goto L_0x00a9;
     */
    public HashMap<String, List> findAppQoeGroupByAllSpace(String appName, int spaceIdOfAllAp) {
        String sql = "SELECT NETWORKNAME, NW_TYPE, COUNT(\"NETWORKID\"), COUNT(\"NETWORKFREQ\"), SUM(\"DURATION\"), SUM(\"POORCOUNT\"), SUM(\"GOODCOUNT\"), COUNT(DISTINCT UPDATE_DATE) FROM " + appName + " WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? GROUP BY NETWORKNAME";
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        LogUtil.i(false, "findAppQoeGroupByAllSpace:%{public}s,FreqLoc:%{public}s, space_allAp:%{public}d, modelVer:%{public}d_%{public}d", sql, this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
        LogUtil.v(false, "findAppQoeGroupByAllSpace, subscribeId:%{public}s", this.subscribeIdPrint);
        HashMap<String, List> results = new HashMap<>(16);
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return results;
        }
        try {
            cursor = sQLiteDatabase.rawQuery(sql, args);
            while (cursor.moveToNext()) {
                processAppQoeByAllSpace(cursor, results);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findAppQoeGroupByAllSpace IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAppQoeGroupByAllSpace failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }

    private void processAppQoeByAllSpace(Cursor cursor, HashMap<String, List> results) {
        List appQoes = new ArrayList(10);
        int networkType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_TYPE));
        int netIdCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_ID_COUNT));
        int netFreqCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_FREQ_COUNT));
        long appDuration = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TITLE_DURATION));
        int appGood = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_GOOD));
        int appPoor = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_POOR));
        int days = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_DAY_COUNT));
        appQoes.add(Integer.valueOf(networkType));
        appQoes.add(Integer.valueOf(netIdCnt));
        appQoes.add(Integer.valueOf(netFreqCnt));
        appQoes.add(Long.valueOf(appDuration));
        appQoes.add(Integer.valueOf(appGood));
        appQoes.add(Integer.valueOf(appPoor));
        appQoes.add(0);
        appQoes.add(0);
        appQoes.add(Integer.valueOf(days));
        String networkName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_NAME));
        results.put(networkName, appQoes);
        LogUtil.i(false, " networkName:%{public}s, type:%{public}d, appDuration:%{public}s,appPoor:%{public}d, appGood:%{public}d, modelVer:%{public}d_%{public}d", networkName, Integer.valueOf(networkType), String.valueOf(appDuration), Integer.valueOf(appPoor), Integer.valueOf(appGood), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00b3, code lost:
        if (0 == 0) goto L_0x00b6;
     */
    public HashMap<String, List> findAppQoeGroupBySpace(String appName, int spaceIdOfAllAp, int spaceIdOfMainAp) {
        String sql = "SELECT NETWORKNAME, NW_TYPE, COUNT(\"NETWORKID\"), COUNT(\"NETWORKFREQ\"), SUM(\"DURATION\"), SUM(\"POORCOUNT\"), SUM(\"GOODCOUNT\"), COUNT(DISTINCT UPDATE_DATE) FROM " + appName + " WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? GROUP BY NETWORKNAME";
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(spaceIdOfMainAp), Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        LogUtil.i(false, "findAppQoeGroupBySpace:%{public}s, FreqLoc:%{public}s, space_allAp:%{public}d, space_mainAp:%{public}d, modelVer:%{public}d_%{public}d", sql, this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
        LogUtil.v(false, "findAppQoeGroupBySpace, subscribeId:%{public}s", this.subscribeIdPrint);
        HashMap<String, List> results = new HashMap<>(16);
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return results;
        }
        try {
            cursor = sQLiteDatabase.rawQuery(sql, args);
            while (cursor.moveToNext()) {
                processAppQoeGroup(cursor, results);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findAppQoeGroupBySpace IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAppQoeGroupBySpace failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }

    private void processAppQoeGroup(Cursor cursor, HashMap<String, List> results) {
        List appQoes = new ArrayList(10);
        int networkType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_TYPE));
        int netIdCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_ID_COUNT));
        int netFreqCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_FREQ_COUNT));
        int appDuration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_DURATION));
        int appGood = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_GOOD));
        int appPoor = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_POOR));
        int days = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TITLE_DAY_COUNT));
        appQoes.add(Integer.valueOf(networkType));
        appQoes.add(Integer.valueOf(netIdCnt));
        appQoes.add(Integer.valueOf(netFreqCnt));
        appQoes.add(Integer.valueOf(appDuration));
        appQoes.add(Integer.valueOf(appGood));
        appQoes.add(Integer.valueOf(appPoor));
        appQoes.add(0);
        appQoes.add(0);
        appQoes.add(Integer.valueOf(days));
        String networkName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_NAME));
        results.put(networkName, appQoes);
        LogUtil.i(false, " networkName:%{public}s, type:%{public}d, appDuration:%{public}d, appPoor:%{public}d, appGood:%{public}d, modelVer:%{public}d_%{public}d", networkName, Integer.valueOf(networkType), Integer.valueOf(appDuration), Integer.valueOf(appPoor), Integer.valueOf(appGood), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0131, code lost:
        if (0 == 0) goto L_0x0134;
     */
    public HashMap<String, Bundle> findUserPrefByAllApSpaces(int spaceIdOfAllAp) {
        LogUtil.i(false, "findUserPrefByAllApSpaces:%{public}s, FreqLoc:%{public}s, space_allAp:%{public}d", "SELECT NETWORKNAME, NW_TYPE, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(USER_PREF_TOTAL_COUNT), SUM(DURATION_CONNECTED) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? GROUP BY NETWORKNAME", this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp));
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(this.modelVerAllAp)};
        HashMap<String, Bundle> results = new HashMap<>(16);
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return results;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT NETWORKNAME, NW_TYPE, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(USER_PREF_TOTAL_COUNT), SUM(DURATION_CONNECTED) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? GROUP BY NETWORKNAME", args);
            while (cursor.moveToNext()) {
                int userPrefOptIn = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_OPTION_IN));
                int userPrefOptOut = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_OPTION_OUT));
                int userPrefStay = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_STAY));
                int userPrefEntery = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_TOTAL_COUNT));
                String networkName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_NAME));
                int networkType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_TYPE));
                long durationConnected = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUM_DURATION_CONNECTED));
                Bundle statistics = new Bundle();
                statistics.putString(KEY_STATISTIC_NETWORK_NAME, networkName);
                statistics.putInt(KEY_STATISTIC_NETWORK_TYPE, networkType);
                statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_OPTION_IN, userPrefOptIn);
                statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_OPTION_OUT, userPrefOptOut);
                statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_STAY, userPrefStay);
                statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_ENTRY, userPrefEntery);
                statistics.putLong(KEY_STATISTIC_DURATION_CONNECTED, durationConnected);
                results.put(networkName, statistics);
                LogUtil.i(false, " networkName:%{public}s, type:%{public}d, user_pref_opt_in:%{public}d, user_pref_opt_out:%{public}d, user_pref_stay:%{public}d, duration_connected:%{public}s, user_pref_entery:%{public}d", networkName, Integer.valueOf(networkType), Integer.valueOf(userPrefOptIn), Integer.valueOf(userPrefOptOut), Integer.valueOf(userPrefStay), String.valueOf(durationConnected), Integer.valueOf(userPrefEntery));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findUserPrefByAllApSpaces IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findUserPrefByAllApSpaces failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0131, code lost:
        if (0 == 0) goto L_0x0134;
     */
    public HashMap<String, Bundle> findUserPrefByMainApSpaces(int spaceIdOfMainAp) {
        LogUtil.i(false, "findUserPrefByTwoSpaces:%{public}s, FreqLoc:%{public}s, space_allAp:%{public}d", "SELECT NETWORKNAME, NW_TYPE, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(USER_PREF_TOTAL_COUNT), SUM(DURATION_CONNECTED) FROM SPACEUSER_BASE WHERE IMSI = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? GROUP BY NETWORKNAME", this.curFreqLoc, Integer.valueOf(spaceIdOfMainAp));
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfMainAp), Integer.toString(this.modelVerMainAp)};
        HashMap<String, Bundle> results = new HashMap<>(16);
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return results;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT NETWORKNAME, NW_TYPE, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(USER_PREF_TOTAL_COUNT), SUM(DURATION_CONNECTED) FROM SPACEUSER_BASE WHERE IMSI = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? GROUP BY NETWORKNAME", args);
            while (cursor.moveToNext()) {
                int userPrefOptIn = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_OPTION_IN));
                int userPrefOptOut = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_OPTION_OUT));
                int userPrefStay = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_STAY));
                int userPrefEntery = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_TOTAL_COUNT));
                String networkName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_NAME));
                int networkType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_TYPE));
                long durationConnected = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUM_DURATION_CONNECTED));
                Bundle statistics = new Bundle();
                statistics.putString(KEY_STATISTIC_NETWORK_NAME, networkName);
                statistics.putInt(KEY_STATISTIC_NETWORK_TYPE, networkType);
                statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_OPTION_IN, userPrefOptIn);
                statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_OPTION_OUT, userPrefOptOut);
                statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_STAY, userPrefStay);
                statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_ENTRY, userPrefEntery);
                statistics.putLong(KEY_STATISTIC_DURATION_CONNECTED, durationConnected);
                results.put(networkName, statistics);
                LogUtil.i(false, " networkName:%{public}s, type:%{public}d, user_pref_opt_in:%{public}d, user_pref_opt_out:%{public}d, user_pref_stay:%{public}d, duration_connected:%{public}s, user_pref_entery:%{public}d", networkName, Integer.valueOf(networkType), Integer.valueOf(userPrefOptIn), Integer.valueOf(userPrefOptOut), Integer.valueOf(userPrefStay), String.valueOf(durationConnected), Integer.valueOf(userPrefEntery));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findUserPrefByTwoSpaces IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findUserPrefByTwoSpaces failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }

    public void setUserPrefEnteryFlag(String netName, int netType, int spaceIdOfAllAp, int flag) {
        LogUtil.i(false, "setUserPrefEnteryFlag SET:%{public}s, NW Name:%{public}s, NW Type:%{public}d, Entery:%{public}d, FreqLoc:%{public}s, space_allAp:%{public}d", "UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = ? WHERE NETWORKNAME = ? AND NW_TYPE = ? AND SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", netName, Integer.valueOf(netType), Integer.valueOf(flag), this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp));
        String[] args = {Integer.toString(flag), netName, Integer.toString(netType), this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(this.modelVerAllAp)};
        LogUtil.i(false, "setUserPrefEnteryFlag CLN:%{public}s, FreqLoc:%{public}s, space_allAp:%{public}d", "UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = ? WHERE NETWORKNAME = ? AND NW_TYPE = ? AND SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp));
        Object[] clearArgs = {this.subscribeId, this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(this.modelVerAllAp)};
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = 0 WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", clearArgs);
            this.db.execSQL("UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = ? WHERE NETWORKNAME = ? AND NW_TYPE = ? AND SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", args);
            this.db.setTransactionSuccessful();
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
        this.db.endTransaction();
    }

    public void clearUserPrefEnteryFlag(int spaceIdOfAllAp) {
        LogUtil.i(false, "clearUserPrefEnteryFlag:%{public}s, FreqLoc:%{public}s, space_allAp:%{public}d", "UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = 0 WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp));
        Object[] args = {this.subscribeId, this.curFreqLoc, Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(this.modelVerAllAp)};
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = 0 WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", args);
            this.db.setTransactionSuccessful();
        } catch (SQLException e) {
            LogUtil.e(false, "UPDATE exception: %{public}s", e.getMessage());
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
        this.db.endTransaction();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x009e, code lost:
        if (0 == 0) goto L_0x00a1;
     */
    public ArrayList<Integer> findAllApSpaceIdByMainApSpace(int spaceIdOfMainAp, int netType) {
        LogUtil.i(false, "findAllApSpaceIdByMainApSpace:%{public}s, FreqLoc:%{public}s, spaceid_mainAp:%{public}d, ver:%{public}d, netType:%{public}d", "SELECT SPACEID FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? AND NW_TYPE = ? GROUP BY SPACEID, SPACEIDMAIN", this.curFreqLoc, Integer.valueOf(spaceIdOfMainAp), Integer.valueOf(this.modelVerMainAp), Integer.valueOf(netType));
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfMainAp), Integer.toString(this.modelVerMainAp), Integer.toString(netType)};
        ArrayList<Integer> results = new ArrayList<>(10);
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return results;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT SPACEID FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? AND NW_TYPE = ? GROUP BY SPACEID, SPACEIDMAIN", args);
            while (cursor.moveToNext()) {
                int spaceAllAp = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SPACE_ID));
                results.add(Integer.valueOf(spaceAllAp));
                LogUtil.i(false, " findAllApSpaceIdByMainApSpace, spaceId:%{public}d", Integer.valueOf(spaceAllAp));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findAllApSpaceIdByMainApSpace IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAllApSpaceIdByMainApSpace failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }

    public Bundle find4gCoverageByCurrLoc() {
        return queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE * DURATION_CONNECTED) / SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND (SPACEID != ? OR SPACEIDMAIN != ?) AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", new String[]{this.subscribeId, this.curFreqLoc, "0", "0", Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)});
    }

    public Bundle find4gCoverageByBothSpace(int spaceIdOfAllAp, int spaceIdOfMainAp) {
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(spaceIdOfMainAp), Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        Bundle coverage4G = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE * DURATION_CONNECTED) / SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        if (coverage4G == null) {
            return new Bundle();
        }
        Bundle coverageOther = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE * DURATION_CONNECTED) / SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME!='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        if (coverageOther == null) {
            return new Bundle();
        }
        coverage4G.putLong(KEY_STATISTIC_DURATION_OUT4G, coverageOther.getLong(KEY_STATISTIC_TOTAL_DURATION));
        return coverage4G;
    }

    public Bundle find4gCoverageByAllApSpace(int spaceIdOfAllAp) {
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfAllAp), Integer.toString(this.modelVerAllAp)};
        Bundle coverage4G = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE * DURATION_CONNECTED) / SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? AND NETWORKNAME='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        if (coverage4G == null) {
            return new Bundle();
        }
        Bundle coverageOther = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE * DURATION_CONNECTED) / SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? AND NETWORKNAME!='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        if (coverageOther == null) {
            return new Bundle();
        }
        coverage4G.putLong(KEY_STATISTIC_DURATION_OUT4G, coverageOther.getLong(KEY_STATISTIC_TOTAL_DURATION));
        return coverage4G;
    }

    public Bundle find4gCoverageByMainApSpace(int spaceIdOfMainAp) {
        String[] args = {this.subscribeId, this.curFreqLoc, Integer.toString(spaceIdOfMainAp), Integer.toString(this.modelVerMainAp)};
        Bundle coverage4G = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE * DURATION_CONNECTED) / SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        if (coverage4G == null) {
            return new Bundle();
        }
        Bundle coverageOther = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE * DURATION_CONNECTED) / SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME!='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        if (coverageOther == null) {
            return new Bundle();
        }
        coverage4G.putLong(KEY_STATISTIC_DURATION_OUT4G, coverageOther.getLong(KEY_STATISTIC_TOTAL_DURATION));
        return coverage4G;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00b3, code lost:
        if (0 == 0) goto L_0x00b6;
     */
    private Bundle queryCoverage(String sql, String[] args) {
        LogUtil.i(false, "query4gCoverage: aql=%{public}s ,args=%{public}s", sql, Arrays.toString(Arrays.copyOfRange(args, 1, args.length)));
        Bundle results = new Bundle();
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return results;
        }
        try {
            cursor = sQLiteDatabase.rawQuery(sql, args);
            while (cursor.moveToNext()) {
                int cellNum = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CELL_NUMBER));
                int avgSignal = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_AVG_SIGNAL));
                long totalDuration = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TOTAL_DURATION));
                int networkType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_TYPE));
                results.putInt(KEY_STATISTIC_NETWORK_TYPE, networkType);
                results.putInt(KEY_STATISTIC_CELL_NUMBER, cellNum);
                results.putInt(KEY_RESULT_AVG_SIGNAL, avgSignal);
                results.putLong(KEY_STATISTIC_TOTAL_DURATION, totalDuration);
                LogUtil.i(false, "type:%{public}d,cell_num:%{public}d,avg_signal:%{public}d,total_duration:%{public}s", Integer.valueOf(networkType), Integer.valueOf(cellNum), Integer.valueOf(avgSignal), String.valueOf(totalDuration));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "query4gCoverage IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "query4gCoverage failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00fc, code lost:
        if (0 == 0) goto L_0x00ff;
     */
    public HashMap<Integer, Bundle> findAppQoeRecByFreqLoc(String freqLocation) {
        String sql = "";
        int count = 0;
        HashMap<Integer, Float> mAppMap = mQoeAppBuilder.getQoeAppList();
        HashMap<Integer, Bundle> results = new HashMap<>(16);
        ArrayList<String> argList = new ArrayList<>(10);
        Cursor cursor = null;
        if (this.db == null) {
            return results;
        }
        for (Map.Entry<Integer, Float> entry : mAppMap.entrySet()) {
            int appNum = entry.getKey().intValue();
            String appTableName = Constant.USERDB_APP_NAME_PREFIX + appNum;
            sql = count == 0 ? "SELECT " + appNum + " AS NAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, COUNT(DISTINCT NETWORKID) NWIDCNT, NETWORKNAME, COUNT(DISTINCT NETWORKFREQ) NWFREQCNT, NW_TYPE, COUNT(DISTINCT UPDATE_DATE) REC, SUM(DURATION), SUM(POORCOUNT), SUM(GOODCOUNT) FROM " + appTableName + " WHERE SCRBID = ? AND FREQLOCNAME = ? GROUP BY NETWORKNAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, NW_TYPE, NAME" : sql + " UNION SELECT " + appNum + " AS NAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, COUNT(DISTINCT NETWORKID) NWIDCNT, NETWORKNAME, COUNT(DISTINCT NETWORKFREQ) NWFREQCNT, NW_TYPE, COUNT(DISTINCT UPDATE_DATE) REC, SUM(DURATION), SUM(POORCOUNT), SUM(GOODCOUNT) FROM " + appTableName + " WHERE SCRBID = ? AND FREQLOCNAME = ? GROUP BY NETWORKNAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, NW_TYPE, NAME";
            count++;
            argList.add(this.subscribeId);
            argList.add(freqLocation);
        }
        String sql2 = sql + " ORDER BY SUM(DURATION) DESC";
        String[] args = (String[]) argList.toArray(new String[argList.size()]);
        LogUtil.i(false, "findAppQoeRecByFreqLoc:%{public}s,FreqLoc:%{public}s,args: %{public}s", sql2, freqLocation, Arrays.toString(args));
        int count2 = 0;
        try {
            cursor = this.db.rawQuery(sql2, args);
            while (cursor.moveToNext()) {
                count2++;
                processAppQoeStatistics(cursor, results, count2);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findAppQoeRecByFreqLoc IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findAppQoeRecByFreqLoc failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }

    private void processAppQoeStatistics(Cursor cursor, HashMap<Integer, Bundle> results, int count) {
        int spaceId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SPACE_ID));
        int modelVerAllAp2 = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MODEL_VERSION_ALL_AP));
        int spaceIdOfMain = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SPACE_ID_OF_MAIN));
        int modelVerMainAp2 = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MODEL_VERSION_MAIN_AP));
        int nwIdCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_ID_COUNT));
        String networkName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_NAME));
        short nwFreqCnt = cursor.getShort(cursor.getColumnIndexOrThrow(KEY_NETWORK_FREQ_COUNT));
        int nwType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_TYPE));
        short rec = cursor.getShort(cursor.getColumnIndexOrThrow(KEY_REC));
        int app = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NAME));
        long duration = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUM_DURATION));
        int poorCount = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_POOR_COUNT));
        int goodCount = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_GOOD_COUNT));
        Bundle statistics = new Bundle();
        statistics.putInt(KEY_STATISTIC_SPACE_ID, spaceId);
        statistics.putInt(KEY_STATISTIC_MODEL_VERSION_ALL_AP, modelVerAllAp2);
        statistics.putInt(KEY_STATISTIC_SPACE_ID_OF_MAIN, spaceIdOfMain);
        statistics.putInt(KEY_STATISTIC_MODEL_VERSION_MAIN_AP, modelVerMainAp2);
        statistics.putInt(KEY_STATISTIC_NETWORK_ID_COUNT, nwIdCnt);
        statistics.putString(KEY_STATISTIC_NETWORK_NAME, networkName);
        statistics.putShort(KEY_STATISTIC_NETWORK_FREQ_COUNT, nwFreqCnt);
        statistics.putInt(KEY_STATISTIC_NW_TYPE, nwType);
        statistics.putShort(KEY_STATISTIC_REC, rec);
        statistics.putLong(KEY_STATISTIC_DURATION, duration);
        statistics.putInt(KEY_STATISTIC_APP, app);
        statistics.putInt(KEY_STATISTIC_POOR_COUNT, poorCount);
        statistics.putInt(KEY_STATISTIC_GOOD_COUNT, goodCount);
        results.put(Integer.valueOf(count), statistics);
        LogUtil.i(false, " networkName:%{public}s, app:%{public}d, poorCount:%{public}d, goodCount:%{public}d, duration:%{public}s, spaceId:%{public}d", networkName, Integer.valueOf(app), Integer.valueOf(poorCount), Integer.valueOf(goodCount), String.valueOf(duration), Integer.valueOf(spaceId));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0057, code lost:
        if (0 == 0) goto L_0x005a;
     */
    public HashMap<Integer, Bundle> findUserExpRecByFreqLoc(String freqLocation) {
        LogUtil.i(false, "findUserPrefByFreqLoc:%{public}s,FreqLoc:%{public}s", "SELECT SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, COUNT(DISTINCT NETWORKID) NWIDCNT,NETWORKNAME, COUNT(DISTINCT NETWORKFREQ) NWFREQCNT, NW_TYPE, COUNT(DISTINCT UPDATE_DATE) REC,SUM(DURATION_CONNECTED), SUM(DATA_RX), SUM(DATA_TX), SUM(SIGNAL_VALUE * DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(POWER_CONSUMPTION) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND (SPACEID != 0 OR SPACEIDMAIN != 0) GROUP BY NETWORKNAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, NW_TYPE ORDER BY SUM(DURATION_CONNECTED) DESC", freqLocation);
        String[] args = {this.subscribeId, freqLocation};
        int count = 0;
        HashMap<Integer, Bundle> results = new HashMap<>(16);
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return results;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, COUNT(DISTINCT NETWORKID) NWIDCNT,NETWORKNAME, COUNT(DISTINCT NETWORKFREQ) NWFREQCNT, NW_TYPE, COUNT(DISTINCT UPDATE_DATE) REC,SUM(DURATION_CONNECTED), SUM(DATA_RX), SUM(DATA_TX), SUM(SIGNAL_VALUE * DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(POWER_CONSUMPTION) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND (SPACEID != 0 OR SPACEIDMAIN != 0) GROUP BY NETWORKNAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, NW_TYPE ORDER BY SUM(DURATION_CONNECTED) DESC", args);
            while (cursor.moveToNext()) {
                count++;
                processUserExpStatistics(cursor, results, count);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "findUserPrefByFreqLoc IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "findUserPrefByFreqLoc failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }

    private void processUserExpStatistics(Cursor cursor, HashMap<Integer, Bundle> results, int count) {
        int spaceId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SPACE_ID));
        int modelVerAllAp2 = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MODEL_VERSION_ALL_AP));
        int spaceIdOfMain = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SPACE_ID_OF_MAIN));
        int modelVerMainAp2 = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MODEL_VERSION_MAIN_AP));
        int nwIdCnt = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_ID_COUNT));
        String networkName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NETWORK_NAME));
        short nwFreqCnt = cursor.getShort(cursor.getColumnIndexOrThrow(KEY_NETWORK_FREQ_COUNT));
        int nwType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NETWORK_TYPE));
        short rec = cursor.getShort(cursor.getColumnIndexOrThrow(KEY_REC));
        long durationConnected = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUM_DURATION_CONNECTED));
        long dataRx = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUM_DATA_RX));
        long dataTx = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUM_DATA_TX));
        short avgSignal = cursor.getShort(cursor.getColumnIndexOrThrow(KEY_AVG_SIGNAL));
        int userPrefOptIn = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_OPTION_IN));
        int userPrefOptOut = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_OPTION_OUT));
        int userPrefStay = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_STAY));
        long powerConsumption = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUM_POWER_CONSUMPTION));
        Bundle statistics = new Bundle();
        statistics.putInt(KEY_STATISTIC_SPACE_ID, spaceId);
        statistics.putInt(KEY_STATISTIC_MODEL_VERSION_ALL_AP, modelVerAllAp2);
        statistics.putInt(KEY_STATISTIC_SPACE_ID_OF_MAIN, spaceIdOfMain);
        statistics.putInt(KEY_STATISTIC_MODEL_VERSION_MAIN_AP, modelVerMainAp2);
        statistics.putInt(KEY_STATISTIC_NETWORK_ID_COUNT, nwIdCnt);
        statistics.putString(KEY_STATISTIC_NETWORK_NAME, networkName);
        statistics.putShort(KEY_STATISTIC_NETWORK_FREQ_COUNT, nwFreqCnt);
        statistics.putInt(KEY_STATISTIC_NW_TYPE, nwType);
        statistics.putShort(KEY_STATISTIC_REC, rec);
        statistics.putLong(KEY_STATISTIC_DURATION_CONNECTED, durationConnected);
        statistics.putLong(KEY_STATISTIC_DATA_RX, dataRx);
        statistics.putLong(KEY_STATISTIC_DATA_TX, dataTx);
        statistics.putShort(KEY_STATISTIC_AVG_SIGNAL, avgSignal);
        statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_OPTION_IN, userPrefOptIn);
        statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_OPTION_OUT, userPrefOptOut);
        statistics.putInt(KEY_STATISTIC_USER_PREFERENCE_STAY, userPrefStay);
        statistics.putLong(KEY_STATISTIC_POWER_CONSUMPTION, powerConsumption);
        results.put(Integer.valueOf(count), statistics);
        LogUtil.i(false, " networkName:%{public}s, type:%{public}d, user_pref_opt_in:%{public}d, user_pref_opt_out:%{public}d, user_pref_stay:%{public}d, duration_connected:%{public}s", networkName, Integer.valueOf(nwType), Integer.valueOf(userPrefOptIn), Integer.valueOf(userPrefOptOut), Integer.valueOf(userPrefStay), String.valueOf(durationConnected));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00e6, code lost:
        if (0 == 0) goto L_0x00e9;
     */
    public Bundle getUerPrefTotalCountDurationByAllApSpaces(String freqLocation, int spaceId, int modelVerAllAp2) {
        LogUtil.i(false, "getUerPrefTotalCountDurationByAllApSpaces:%{public}s,FreqLoc:%{public}s", "SELECT SUM(DURATION_CONNECTED), SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? GROUP BY SPACEID, MODEL_VER_ALLAP", freqLocation);
        String[] args = {this.subscribeId, freqLocation, Integer.toString(spaceId), Integer.toString(modelVerAllAp2)};
        int count = 0;
        Bundle results = new Bundle();
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = this.db;
        if (sQLiteDatabase == null) {
            return results;
        }
        try {
            cursor = sQLiteDatabase.rawQuery("SELECT SUM(DURATION_CONNECTED), SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? GROUP BY SPACEID, MODEL_VER_ALLAP", args);
            while (cursor.moveToNext()) {
                count++;
                long durationConnected = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUM_DURATION_CONNECTED));
                int userPrefOptIn = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_OPTION_IN));
                int userPrefOptOut = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_OPTION_OUT));
                int userPrefStay = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SUM_USER_PREFERENCE_STAY));
                int totalCount = Math.round(((float) (userPrefOptIn + userPrefOptOut)) / 2.0f) + userPrefStay;
                results.putInt("totalDuration", ((int) durationConnected) / 1000);
                results.putInt("totalCount", totalCount);
                LogUtil.i(false, " totalDuration:%{public}s, totalCount:%{public}d, user_pref_opt_in:%{public}d, user_pref_opt_out:%{public}d, user_pref_stay:%{public}d, duration_connected:%{public}s", String.valueOf(((int) durationConnected) / 1000), Integer.valueOf(totalCount), Integer.valueOf(userPrefOptIn), Integer.valueOf(userPrefOptOut), Integer.valueOf(userPrefStay), String.valueOf(durationConnected));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e(false, "getUerPrefTotalCountDurationByAllApSpaces IllegalArgumentException: %{public}s", e.getMessage());
        } catch (SQLException e2) {
            LogUtil.e(false, "getUerPrefTotalCountDurationByAllApSpaces failed by Exception", new Object[0]);
            if (0 != 0) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return results;
    }
}
