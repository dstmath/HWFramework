package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.SpaceExpInfo;
import com.android.server.hidata.wavemapping.service.HwHistoryQoEResourceBuilder;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SpaceUserDAO {
    private static final int INTERVAL_24HR = 86400000;
    private static final String TAG = ("WMapping." + SpaceUserDAO.class.getSimpleName());
    private static SpaceUserDAO instance = null;
    private static HwHistoryQoEResourceBuilder mQoeAppBuilder = null;
    private String ScrbId = "NA";
    private String ScrbId_print = "NA";
    private String curFreqLoc = Constant.NAME_FREQLOCATION_OTHER;
    private SQLiteDatabase db = DatabaseSingleton.getInstance();
    private int modelVerAllAp = 0;
    private int modelVerMainAp = 0;
    private long time1stClean = 0;

    public SpaceUserDAO() {
        mQoeAppBuilder = HwHistoryQoEResourceBuilder.getInstance();
        this.time1stClean = System.currentTimeMillis();
    }

    public static synchronized SpaceUserDAO getInstance() {
        SpaceUserDAO spaceUserDAO;
        synchronized (SpaceUserDAO.class) {
            if (instance == null) {
                instance = new SpaceUserDAO();
            }
            spaceUserDAO = instance;
        }
        return spaceUserDAO;
    }

    public void setFreqLocation(String location) {
        if (location != null) {
            this.curFreqLoc = location;
            deleteOldRecords();
        }
    }

    public void setScrbId(String ScrbId2) {
        if (ScrbId2 != null) {
            this.ScrbId = ScrbId2;
            if (ScrbId2.length() > 10) {
                this.ScrbId_print = ScrbId2.substring(0, 10);
            } else {
                this.ScrbId_print = ScrbId2;
            }
            LogUtil.v("setScrbId :" + this.ScrbId_print);
        }
    }

    public void setModelVer(int model_AllAp, int model_MainAp) {
        this.modelVerAllAp = model_AllAp;
        this.modelVerMainAp = model_MainAp;
    }

    public boolean insertBase(SpaceExpInfo spaceinfo) {
        if (networkIdFoundBaseBySpaceNetwork(spaceinfo.getSpaceID(), spaceinfo.getSpaceIDMain(), spaceinfo.getNetworkId(), spaceinfo.getNetworkName(), spaceinfo.getNetworkFreq()) != null) {
            return update(spaceinfo);
        }
        ContentValues cValueBase = new ContentValues();
        cValueBase.put("SCRBID", this.ScrbId);
        cValueBase.put("FREQLOCNAME", this.curFreqLoc);
        cValueBase.put("SPACEID", Integer.valueOf(spaceinfo.getSpaceID()));
        cValueBase.put("SPACEIDMAIN", Integer.valueOf(spaceinfo.getSpaceIDMain()));
        cValueBase.put("NETWORKNAME", spaceinfo.getNetworkName());
        cValueBase.put("NETWORKFREQ", spaceinfo.getNetworkFreq());
        cValueBase.put("NETWORKID", spaceinfo.getNetworkId());
        cValueBase.put("SIGNAL_VALUE", Integer.valueOf(spaceinfo.getSignalValue()));
        cValueBase.put("POWER_CONSUMPTION", Long.valueOf(spaceinfo.getPowerConsumption()));
        cValueBase.put("DATA_RX", Long.valueOf(spaceinfo.getDataRx()));
        cValueBase.put("DATA_TX", Long.valueOf(spaceinfo.getDataTx()));
        cValueBase.put("USER_PREF_OPT_IN", Integer.valueOf(spaceinfo.getUserPrefOptIn()));
        cValueBase.put("USER_PREF_OPT_OUT", Integer.valueOf(spaceinfo.getUserPrefOptOut()));
        cValueBase.put("USER_PREF_STAY", Integer.valueOf(spaceinfo.getUserPrefStay()));
        cValueBase.put("USER_PREF_TOTAL_COUNT", Integer.valueOf(spaceinfo.getUserPrefTotalCount()));
        cValueBase.put("DURATION_CONNECTED", Long.valueOf(spaceinfo.getDuration()));
        cValueBase.put("NW_TYPE", Integer.valueOf(spaceinfo.getNetworkType()));
        cValueBase.put("MODEL_VER_ALLAP", Integer.valueOf(this.modelVerAllAp));
        cValueBase.put("MODEL_VER_MAINAP", Integer.valueOf(this.modelVerMainAp));
        cValueBase.put("DUBAI_SCREENOFF_TX", Long.valueOf(spaceinfo.getDubaiScreenOffTx()));
        cValueBase.put("DUBAI_SCREENOFF_RX", Long.valueOf(spaceinfo.getDubaiScreenOffRx()));
        cValueBase.put("DUBAI_SCREENOFF_POWER", Long.valueOf(spaceinfo.getDubaiScreenOffPower()));
        cValueBase.put("DUBAI_SCREENON_TX", Long.valueOf(spaceinfo.getDubaiScreenOnTx()));
        cValueBase.put("DUBAI_SCREENON_RX", Long.valueOf(spaceinfo.getDubaiScreenOnRx()));
        cValueBase.put("DUBAI_SCREENON_POWER", Long.valueOf(spaceinfo.getDubaiScreenOnPower()));
        cValueBase.put("DUBAI_IDLE_DURATION", Long.valueOf(spaceinfo.getDubaiIdleDuration()));
        cValueBase.put("DUBAI_IDLE_POWER", Long.valueOf(spaceinfo.getDubaiIdlePower()));
        LogUtil.i("insert BASE, FreqLoc:" + this.curFreqLoc + ", " + spaceinfo.toString() + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
        StringBuilder sb = new StringBuilder();
        sb.append("insert BASE, scrib ID:");
        sb.append(this.ScrbId_print);
        LogUtil.v(sb.toString());
        try {
            this.db.beginTransaction();
            this.db.insert(Constant.SPACEUSER_TABLE_NAME, null, cValueBase);
            this.db.setTransactionSuccessful();
            this.db.endTransaction();
            if (0 == this.time1stClean) {
                this.time1stClean = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - this.time1stClean > 86400000) {
                deleteOldRecords();
            }
            return true;
        } catch (Exception e) {
            LogUtil.e("insert exception: " + e.getMessage());
            this.db.endTransaction();
            return false;
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
    }

    public boolean insertApp(SpaceExpInfo spaceinfo) {
        int good;
        int poor;
        int poor2;
        long duration;
        SQLiteDatabase sQLiteDatabase;
        Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoEAppList().entrySet().iterator();
        boolean result = true;
        while (it.hasNext()) {
            String appName = Constant.USERDB_APP_NAME_PREFIX + it.next().getKey();
            HashMap<String, Long> mapDuration = spaceinfo.getMapAppDuration();
            try {
                if (mapDuration.containsKey(appName)) {
                    long duration2 = mapDuration.get(appName).longValue();
                    if (duration2 > 0) {
                        if (spaceinfo.getMapAppQoeGood().containsKey(appName)) {
                            good = spaceinfo.getMapAppQoeGood().get(appName).intValue();
                        } else {
                            good = 0;
                        }
                        try {
                            if (spaceinfo.getMapAppQoePoor().containsKey(appName)) {
                                poor = spaceinfo.getMapAppQoePoor().get(appName).intValue();
                            } else {
                                poor = 0;
                            }
                        } catch (Exception e) {
                            e = e;
                            LogUtil.e("insertApp exception: " + e.getMessage());
                            result = false;
                        }
                        try {
                            poor2 = poor;
                            duration = duration2;
                        } catch (Exception e2) {
                            e = e2;
                            int i = poor;
                            LogUtil.e("insertApp exception: " + e.getMessage());
                            result = false;
                        }
                        try {
                            if (networkIdFoundAppSpaceNetwork(appName, spaceinfo.getSpaceID(), spaceinfo.getSpaceIDMain(), spaceinfo.getNetworkId(), spaceinfo.getNetworkName(), spaceinfo.getNetworkFreq()) != null) {
                                try {
                                    if (!updateApp(appName, duration, good, poor2, spaceinfo)) {
                                        result = false;
                                        LogUtil.w("updateApp APP fail:" + appName + ",FreqLoc:" + this.curFreqLoc);
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("updateApp APP fail, nwId:");
                                        sb.append(spaceinfo.getNetworkId());
                                        LogUtil.v(sb.toString());
                                    }
                                    int i2 = poor2;
                                } catch (Exception e3) {
                                    e = e3;
                                    int i3 = poor2;
                                    LogUtil.e("insertApp exception: " + e.getMessage());
                                    result = false;
                                }
                            } else {
                                ContentValues cValueApp = new ContentValues();
                                cValueApp.put("SCRBID", this.ScrbId);
                                cValueApp.put("FREQLOCNAME", this.curFreqLoc);
                                cValueApp.put("SPACEID", Integer.valueOf(spaceinfo.getSpaceID()));
                                cValueApp.put("SPACEIDMAIN", Integer.valueOf(spaceinfo.getSpaceIDMain()));
                                cValueApp.put("NETWORKNAME", spaceinfo.getNetworkName());
                                cValueApp.put("NETWORKFREQ", spaceinfo.getNetworkFreq());
                                cValueApp.put("NETWORKID", spaceinfo.getNetworkId());
                                cValueApp.put("NW_TYPE", Integer.valueOf(spaceinfo.getNetworkType()));
                                cValueApp.put(Constant.USERDB_APP_NAME_DURATION, Long.valueOf(duration));
                                try {
                                    cValueApp.put(Constant.USERDB_APP_NAME_POOR, Integer.valueOf(poor2));
                                    cValueApp.put(Constant.USERDB_APP_NAME_GOOD, Integer.valueOf(good));
                                    cValueApp.put("MODEL_VER_ALLAP", Integer.valueOf(this.modelVerAllAp));
                                    cValueApp.put("MODEL_VER_MAINAP", Integer.valueOf(this.modelVerMainAp));
                                    LogUtil.i("insert APP:" + appName + ",FreqLoc:" + this.curFreqLoc + ", duration:" + duration + ", poor:" + poor + ", good:" + good + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append("insert APP:");
                                    sb2.append(appName);
                                    sb2.append(", scrib ID:");
                                    sb2.append(this.ScrbId_print);
                                    LogUtil.v(sb2.toString());
                                    try {
                                        this.db.beginTransaction();
                                        this.db.insert(appName, null, cValueApp);
                                        this.db.setTransactionSuccessful();
                                        sQLiteDatabase = this.db;
                                    } catch (Exception e4) {
                                        LogUtil.e("db insert exception: " + e4.getMessage());
                                        result = false;
                                        sQLiteDatabase = this.db;
                                    }
                                    sQLiteDatabase.endTransaction();
                                } catch (Exception e5) {
                                    e = e5;
                                } catch (Throwable th) {
                                    this.db.endTransaction();
                                    throw th;
                                }
                            }
                        } catch (Exception e6) {
                            e = e6;
                            int i4 = poor2;
                            LogUtil.e("insertApp exception: " + e.getMessage());
                            result = false;
                        }
                    }
                }
            } catch (Exception e7) {
                e = e7;
                LogUtil.e("insertApp exception: " + e.getMessage());
                result = false;
            }
        }
        return result;
    }

    public boolean update(SpaceExpInfo spaceinfo) {
        Object[] args = {Integer.valueOf(spaceinfo.getSignalValue()), Integer.valueOf(spaceinfo.getUserPrefOptIn()), Integer.valueOf(spaceinfo.getUserPrefOptOut()), Integer.valueOf(spaceinfo.getUserPrefStay()), Integer.valueOf(spaceinfo.getUserPrefTotalCount()), Long.valueOf(spaceinfo.getPowerConsumption()), Long.valueOf(spaceinfo.getDataRx()), Long.valueOf(spaceinfo.getDataTx()), Long.valueOf(spaceinfo.getDuration()), Long.valueOf(spaceinfo.getDubaiScreenOffTx()), Long.valueOf(spaceinfo.getDubaiScreenOffRx()), Long.valueOf(spaceinfo.getDubaiScreenOffPower()), Long.valueOf(spaceinfo.getDubaiScreenOnTx()), Long.valueOf(spaceinfo.getDubaiScreenOnRx()), Long.valueOf(spaceinfo.getDubaiScreenOnPower()), Long.valueOf(spaceinfo.getDubaiIdleDuration()), Long.valueOf(spaceinfo.getDubaiIdlePower()), this.ScrbId, this.curFreqLoc, spaceinfo.getNetworkId(), spaceinfo.getNetworkName(), spaceinfo.getNetworkFreq(), Integer.valueOf(spaceinfo.getSpaceID()), Integer.valueOf(spaceinfo.getSpaceIDMain()), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp)};
        LogUtil.i("update BASE begin: FreqLoc:" + this.curFreqLoc + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp + ", " + spaceinfo.toString());
        StringBuilder sb = new StringBuilder();
        sb.append("update BASE begin: scribId:");
        sb.append(this.ScrbId_print);
        LogUtil.v(sb.toString());
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE SPACEUSER_BASE SET SIGNAL_VALUE = ?,USER_PREF_OPT_IN = ?,USER_PREF_OPT_OUT = ?,USER_PREF_STAY = ?,USER_PREF_TOTAL_COUNT = ?, POWER_CONSUMPTION = ?, DATA_RX = ?, DATA_TX = ?, DURATION_CONNECTED = ?, DUBAI_SCREENOFF_TX = ?, DUBAI_SCREENOFF_RX = ?, DUBAI_SCREENOFF_POWER = ?, DUBAI_SCREENON_TX = ?, DUBAI_SCREENON_RX = ?, DUBAI_SCREENON_POWER = ?, DUBAI_IDLE_DURATION = ?, DUBAI_IDLE_POWER = ? WHERE SCRBID = ? AND FREQLOCNAME = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))", args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean updateApp(String appName, long duration, int good, int poor, SpaceExpInfo spaceinfo) {
        String sql = "UPDATE " + appName + " SET DURATION = ?,POORCOUNT = ?,GOODCOUNT = ? WHERE SCRBID = ? AND FREQLOCNAME = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))";
        Object[] args = {Long.valueOf(duration), Integer.valueOf(poor), Integer.valueOf(good), this.ScrbId, this.curFreqLoc, spaceinfo.getNetworkId(), spaceinfo.getNetworkName(), spaceinfo.getNetworkFreq(), Integer.valueOf(spaceinfo.getSpaceID()), Integer.valueOf(spaceinfo.getSpaceIDMain()), Integer.valueOf(this.modelVerAllAp), Integer.valueOf(this.modelVerMainAp)};
        LogUtil.i("update APP:" + appName + " begin: FreqLoc:" + this.curFreqLoc + ", duration:" + duration + ", poor:" + poor + ", good:" + good + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
        StringBuilder sb = new StringBuilder();
        sb.append("update APP:");
        sb.append(appName);
        sb.append(" begin: scribId:");
        sb.append(this.ScrbId_print);
        LogUtil.v(sb.toString());
        try {
            this.db.beginTransaction();
            this.db.execSQL(sql, args);
            this.db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
            return false;
        } finally {
            this.db.endTransaction();
        }
    }

    public boolean remove(int spaceid_allAp, int spaceid_mainAp, String networkid) {
        try {
            this.db.execSQL("DELETE FROM SPACEUSER_BASE  WHERE FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND NETWORKID = ?", new Object[]{this.curFreqLoc, Integer.valueOf(spaceid_allAp), Integer.valueOf(spaceid_mainAp), networkid});
            return true;
        } catch (SQLException e) {
            LogUtil.e("remove exception: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00a3, code lost:
        if (r2 == null) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0065, code lost:
        if (r2 != null) goto L_0x0067;
     */
    public String networkIdFoundBaseBySpaceNetwork(int spaceid_allAp, int spaceid_mainAp, String networkid, String networkname, String networkfreq) {
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(spaceid_mainAp), networkid, networkname, networkfreq, Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        Cursor cursor = null;
        String netId = null;
        String date = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery("SELECT NETWORKID, UPDATE_DATE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))", args);
            if (cursor.moveToNext()) {
                netId = cursor.getString(cursor.getColumnIndexOrThrow("NETWORKID"));
                date = cursor.getString(cursor.getColumnIndexOrThrow("UPDATE_DATE"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("networkIdFoundBaseBySpaceNetwork IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("networkIdFoundBaseBySpaceNetwork Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            if (netId != null) {
                LogUtil.i("networkIdFoundBaseBySpaceNetwork, Date:" + date + ", FreqLoc:" + this.curFreqLoc + ", SPACEID:" + spaceid_allAp + ", SPACEIDMAIN:" + spaceid_mainAp + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
                StringBuilder sb = new StringBuilder();
                sb.append("networkIdFoundBaseBySpaceNetwork, netId:");
                sb.append(netId);
                sb.append(", ScrbId:");
                sb.append(this.ScrbId_print);
                LogUtil.v(sb.toString());
            } else {
                LogUtil.d("networkIdFoundBaseBySpaceNetwork, NO DATA, sql:" + "SELECT NETWORKID, UPDATE_DATE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))" + ", FreqLoc:" + this.curFreqLoc + ", SPACEID:" + spaceid_allAp + ", SPACEIDMAIN:" + spaceid_mainAp + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
            }
            return netId;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00b7, code lost:
        if (r2 == null) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0079, code lost:
        if (r2 != null) goto L_0x007b;
     */
    public String networkIdFoundAppSpaceNetwork(String appName, int spaceid_allAp, int spaceid_mainAp, String networkid, String networkname, String networkfreq) {
        String sql = "SELECT NETWORKID, UPDATE_DATE FROM " + appName + " WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND NETWORKID = ? AND NETWORKNAME = ? AND NETWORKFREQ = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND UPDATE_DATE = (date('now', 'localtime'))";
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(spaceid_mainAp), networkid, networkname, networkfreq, Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        Cursor cursor = null;
        String netId = null;
        String date = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery(sql, args);
            if (cursor.moveToNext()) {
                netId = cursor.getString(cursor.getColumnIndexOrThrow("NETWORKID"));
                date = cursor.getString(cursor.getColumnIndexOrThrow("UPDATE_DATE"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("networkIdFoundAppSpaceNetwork IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("networkIdFoundAppSpaceNetwork Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            if (netId != null) {
                LogUtil.i("networkIdFoundAppSpaceNetwork, appName" + appName + ", Date:" + date + ", FreqLoc:" + this.curFreqLoc + ", SPACEID:" + spaceid_allAp + ", SPACEIDMAIN:" + spaceid_mainAp + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
                StringBuilder sb = new StringBuilder();
                sb.append("networkIdFoundAppSpaceNetwork, appName");
                sb.append(appName);
                sb.append(", netId:");
                sb.append(netId);
                sb.append(", ScrbId:");
                sb.append(this.ScrbId_print);
                LogUtil.v(sb.toString());
            } else {
                LogUtil.d("networkIdFoundAppSpaceNetwork, NO DATA, sql:" + sql + ", FreqLoc:" + this.curFreqLoc + ", SPACEID:" + spaceid_allAp + ", SPACEIDMAIN:" + spaceid_mainAp + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
            }
            return netId;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private String getAllAppTitleString() {
        StringBuffer sql_title = new StringBuffer();
        Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoEAppList().entrySet().iterator();
        while (it.hasNext()) {
            String appName = Constant.USERDB_APP_NAME_PREFIX + it.next().getKey();
            sql_title.append(", ");
            sql_title.append(appName + "." + Constant.USERDB_APP_NAME_DURATION + " AS " + appName + "_" + Constant.USERDB_APP_NAME_DURATION);
            sql_title.append(", ");
            sql_title.append(appName + "." + Constant.USERDB_APP_NAME_POOR + " AS " + appName + "_" + Constant.USERDB_APP_NAME_POOR);
            sql_title.append(", ");
            sql_title.append(appName + "." + Constant.USERDB_APP_NAME_GOOD + " AS " + appName + "_" + Constant.USERDB_APP_NAME_GOOD);
        }
        return sql_title.toString();
    }

    private String getAllAppJoinString() {
        StringBuffer sql_title = new StringBuffer();
        Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoEAppList().entrySet().iterator();
        while (it.hasNext()) {
            String appName = Constant.USERDB_APP_NAME_PREFIX + it.next().getKey();
            sql_title.append(" LEFT OUTER JOIN " + appName + " USING(SCRBID,FREQLOCNAME,SPACEID,SPACEIDMAIN,NETWORKID,NETWORKNAME,NETWORKFREQ,NW_TYPE,UPDATE_DATE)");
        }
        return sql_title.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:62:0x039a, code lost:
        if (r13 != null) goto L_0x039c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x03ef, code lost:
        if (r13 == null) goto L_0x03f2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x03cd A[Catch:{ IllegalArgumentException -> 0x03ce, Exception -> 0x03aa, all -> 0x03a0, all -> 0x03f3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x03f6  */
    public HashMap<String, SpaceExpInfo> findAllByTwoSpaces(int spaceid_allAp, int spaceid_mainAp) {
        HashMap<Integer, Float> mAppMap;
        String sql_app_title;
        String sql_condition;
        String sql_base = "SIGNAL_VALUE,USER_PREF_OPT_IN,USER_PREF_OPT_OUT,USER_PREF_STAY,USER_PREF_TOTAL_COUNT,POWER_CONSUMPTION,DURATION_CONNECTED,DATA_RX,DATA_TX,DUBAI_SCREENOFF_TX,DUBAI_SCREENOFF_RX,DUBAI_SCREENOFF_POWER,DUBAI_SCREENON_TX,DUBAI_SCREENON_RX,DUBAI_SCREENON_POWER,DUBAI_IDLE_DURATION,DUBAI_IDLE_POWER";
        String sql_app_title2 = getAllAppTitleString();
        String sql_app_join = getAllAppJoinString();
        String sql_condition2 = " WHERE SPACEUSER_BASE.SCRBID = ? AND SPACEUSER_BASE.FREQLOCNAME = ? AND SPACEUSER_BASE.SPACEID = ? AND SPACEUSER_BASE.SPACEIDMAIN = ? AND SPACEUSER_BASE.MODEL_VER_ALLAP = ? AND SPACEUSER_BASE.MODEL_VER_MAINAP = ? AND SPACEUSER_BASE.UPDATE_DATE = (date('now', 'localtime'))";
        String sql = "SELECT " + "SPACEUSER_BASE.SPACEID,SPACEUSER_BASE.SPACEIDMAIN,SPACEUSER_BASE.NETWORKID,SPACEUSER_BASE.NETWORKNAME,SPACEUSER_BASE.NETWORKFREQ,SPACEUSER_BASE.NW_TYPE," + sql_base + sql_app_title2 + " FROM " + Constant.SPACEUSER_TABLE_NAME + sql_app_join + sql_condition2;
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(spaceid_mainAp), Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        Cursor cursor = null;
        HashMap<String, SpaceExpInfo> spaceInfoHashMap = new HashMap<>();
        LogUtil.i(" findAllByTwoSpaces: sql_comm=SELECT " + "SPACEUSER_BASE.SPACEID,SPACEUSER_BASE.SPACEIDMAIN,SPACEUSER_BASE.NETWORKID,SPACEUSER_BASE.NETWORKNAME,SPACEUSER_BASE.NETWORKFREQ,SPACEUSER_BASE.NW_TYPE,");
        LogUtil.i(" findAllByTwoSpaces: sql_base=" + sql_base);
        LogUtil.i(" findAllByTwoSpaces: sql_appt=" + sql_app_title2);
        LogUtil.i(" findAllByTwoSpaces: sql_from= FROM SPACEUSER_BASE");
        LogUtil.i(" findAllByTwoSpaces: sql_appj=" + sql_app_join);
        LogUtil.i(" findAllByTwoSpaces: sql_cond=" + sql_condition2);
        StringBuilder sb = new StringBuilder();
        sb.append(" findAllByTwoSpaces: args=");
        Object obj = "SPACEUSER_BASE.SPACEID,SPACEUSER_BASE.SPACEIDMAIN,SPACEUSER_BASE.NETWORKID,SPACEUSER_BASE.NETWORKNAME,SPACEUSER_BASE.NETWORKFREQ,SPACEUSER_BASE.NW_TYPE,";
        sb.append(Arrays.toString(Arrays.copyOfRange(args, 1, args.length)));
        LogUtil.v(sb.toString());
        try {
            cursor = this.db.rawQuery(sql, args);
            while (cursor.moveToNext()) {
                StringBuilder spaceId = new StringBuilder(cursor.getString(cursor.getColumnIndexOrThrow("SPACEID")));
                StringBuilder spaceIdmain = new StringBuilder(cursor.getString(cursor.getColumnIndexOrThrow("SPACEIDMAIN")));
                HashMap<String, Long> duration_app = new HashMap<>();
                HashMap<String, Integer> qoe_app_poor = new HashMap<>();
                HashMap<String, Integer> qoe_app_good = new HashMap<>();
                String sql_base2 = sql_base;
                try {
                    mAppMap = mQoeAppBuilder.getQoEAppList();
                    sql_app_title = sql_app_title2;
                } catch (IllegalArgumentException e) {
                    e = e;
                    String str = sql_app_title2;
                    String str2 = sql_app_join;
                    String str3 = sql_condition2;
                    LogUtil.e("findAllByTwoSpaces IllegalArgumentException: " + e.getMessage());
                } catch (Exception e2) {
                    e = e2;
                    String str4 = sql_app_title2;
                    String str5 = sql_app_join;
                    String str6 = sql_condition2;
                    LogUtil.e("findAllByTwoSpaces Exception: " + e.getMessage());
                    if (cursor != null) {
                    }
                    return spaceInfoHashMap;
                } catch (Throwable th) {
                    th = th;
                    String str7 = sql_app_title2;
                    String str8 = sql_app_join;
                    String str9 = sql_condition2;
                    if (cursor != null) {
                    }
                    throw th;
                }
                try {
                    Iterator<Map.Entry<Integer, Float>> it = mAppMap.entrySet().iterator();
                    while (it.hasNext()) {
                        try {
                            Map.Entry next = it.next();
                            HashMap<Integer, Float> mAppMap2 = mAppMap;
                            StringBuilder sb2 = new StringBuilder();
                            Iterator<Map.Entry<Integer, Float>> it2 = it;
                            sb2.append(Constant.USERDB_APP_NAME_PREFIX);
                            String sql_app_join2 = sql_app_join;
                            try {
                                sb2.append(next.getKey());
                                String appName = sb2.toString();
                                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(appName + "_" + Constant.USERDB_APP_NAME_DURATION));
                                if (duration > 0) {
                                    duration_app.put(appName, Long.valueOf(duration));
                                    qoe_app_poor.put(appName, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(appName + "_" + Constant.USERDB_APP_NAME_POOR))));
                                    qoe_app_good.put(appName, Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(appName + "_" + Constant.USERDB_APP_NAME_GOOD))));
                                }
                                mAppMap = mAppMap2;
                                it = it2;
                                sql_app_join = sql_app_join2;
                            } catch (IllegalArgumentException e3) {
                                e = e3;
                                String str10 = sql_condition2;
                                LogUtil.e("findAllByTwoSpaces IllegalArgumentException: " + e.getMessage());
                            } catch (Exception e4) {
                                e = e4;
                                String str11 = sql_condition2;
                                LogUtil.e("findAllByTwoSpaces Exception: " + e.getMessage());
                                if (cursor != null) {
                                }
                                return spaceInfoHashMap;
                            } catch (Throwable th2) {
                                th = th2;
                                String str12 = sql_condition2;
                                if (cursor != null) {
                                }
                                throw th;
                            }
                        } catch (IllegalArgumentException e5) {
                            e = e5;
                            String str13 = sql_app_join;
                            String str14 = sql_condition2;
                            LogUtil.e("findAllByTwoSpaces IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e6) {
                            e = e6;
                            String str15 = sql_app_join;
                            String str16 = sql_condition2;
                            LogUtil.e("findAllByTwoSpaces Exception: " + e.getMessage());
                            if (cursor != null) {
                                cursor.close();
                            }
                            return spaceInfoHashMap;
                        } catch (Throwable th3) {
                            th = th3;
                            String str17 = sql_app_join;
                            String str18 = sql_condition2;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                    HashMap<Integer, Float> hashMap = mAppMap;
                    String sql_app_join3 = sql_app_join;
                    try {
                        sql_condition = sql_condition2;
                    } catch (IllegalArgumentException e7) {
                        e = e7;
                        String str19 = sql_condition2;
                        LogUtil.e("findAllByTwoSpaces IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e8) {
                        e = e8;
                        String str20 = sql_condition2;
                        LogUtil.e("findAllByTwoSpaces Exception: " + e.getMessage());
                        if (cursor != null) {
                        }
                        return spaceInfoHashMap;
                    } catch (Throwable th4) {
                        th = th4;
                        String str21 = sql_condition2;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                    try {
                        SpaceExpInfo spaceExpInfo = new SpaceExpInfo(spaceId, spaceIdmain, cursor.getString(cursor.getColumnIndexOrThrow("NETWORKID")), cursor.getString(cursor.getColumnIndexOrThrow("NETWORKNAME")), cursor.getString(cursor.getColumnIndexOrThrow("NETWORKFREQ")), qoe_app_poor, qoe_app_good, duration_app, 0, 0, 0, cursor.getInt(cursor.getColumnIndexOrThrow("SIGNAL_VALUE")), (long) cursor.getInt(cursor.getColumnIndexOrThrow("POWER_CONSUMPTION")), cursor.getInt(cursor.getColumnIndexOrThrow("USER_PREF_OPT_IN")), cursor.getInt(cursor.getColumnIndexOrThrow("USER_PREF_OPT_OUT")), cursor.getInt(cursor.getColumnIndexOrThrow("USER_PREF_STAY")), cursor.getInt(cursor.getColumnIndexOrThrow("USER_PREF_TOTAL_COUNT")), cursor.getLong(cursor.getColumnIndexOrThrow("DURATION_CONNECTED")), cursor.getInt(cursor.getColumnIndexOrThrow("NW_TYPE")), cursor.getLong(cursor.getColumnIndexOrThrow("DATA_RX")), cursor.getLong(cursor.getColumnIndexOrThrow("DATA_TX")), cursor.getLong(cursor.getColumnIndexOrThrow("DUBAI_SCREENOFF_TX")), cursor.getLong(cursor.getColumnIndexOrThrow("DUBAI_SCREENOFF_RX")), cursor.getLong(cursor.getColumnIndexOrThrow("DUBAI_SCREENOFF_POWER")), cursor.getLong(cursor.getColumnIndexOrThrow("DUBAI_SCREENON_TX")), cursor.getLong(cursor.getColumnIndexOrThrow("DUBAI_SCREENON_RX")), cursor.getLong(cursor.getColumnIndexOrThrow("DUBAI_SCREENON_POWER")), cursor.getLong(cursor.getColumnIndexOrThrow("DUBAI_IDLE_DURATION")), cursor.getLong(cursor.getColumnIndexOrThrow("DUBAI_IDLE_POWER")));
                        SpaceExpInfo spaceInfo = spaceExpInfo;
                        LogUtil.i(" findAllByTwoSpaces:SPACEID:" + spaceInfo.getSpaceID() + ",SPACEIDMAIN:" + spaceInfo.getSpaceIDMain() + ",FreqLoc:" + this.curFreqLoc + spaceInfo.toString());
                        spaceInfoHashMap.put(spaceInfo.getNetworkId(), spaceInfo);
                        sql_base = sql_base2;
                        sql_app_title2 = sql_app_title;
                        sql_app_join = sql_app_join3;
                        sql_condition2 = sql_condition;
                    } catch (IllegalArgumentException e9) {
                        e = e9;
                        LogUtil.e("findAllByTwoSpaces IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e10) {
                        e = e10;
                        LogUtil.e("findAllByTwoSpaces Exception: " + e.getMessage());
                        if (cursor != null) {
                        }
                        return spaceInfoHashMap;
                    }
                } catch (IllegalArgumentException e11) {
                    e = e11;
                    String str22 = sql_app_join;
                    String str23 = sql_condition2;
                    LogUtil.e("findAllByTwoSpaces IllegalArgumentException: " + e.getMessage());
                } catch (Exception e12) {
                    e = e12;
                    String str24 = sql_app_join;
                    String str25 = sql_condition2;
                    LogUtil.e("findAllByTwoSpaces Exception: " + e.getMessage());
                    if (cursor != null) {
                    }
                    return spaceInfoHashMap;
                } catch (Throwable th5) {
                    th = th5;
                    String str26 = sql_app_join;
                    String str27 = sql_condition2;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
            String str28 = sql_app_title2;
            String str29 = sql_app_join;
            String str30 = sql_condition2;
        } catch (IllegalArgumentException e13) {
            e = e13;
            String str31 = sql_base;
            String str32 = sql_app_title2;
            String str33 = sql_app_join;
            String str34 = sql_condition2;
            LogUtil.e("findAllByTwoSpaces IllegalArgumentException: " + e.getMessage());
        } catch (Exception e14) {
            e = e14;
            String str35 = sql_base;
            String str36 = sql_app_title2;
            String str37 = sql_app_join;
            String str38 = sql_condition2;
            LogUtil.e("findAllByTwoSpaces Exception: " + e.getMessage());
            if (cursor != null) {
            }
            return spaceInfoHashMap;
        } catch (Throwable th6) {
            th = th6;
            if (cursor != null) {
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0078, code lost:
        if (r3 == null) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x003a, code lost:
        if (r3 != null) goto L_0x003c;
     */
    public void deleteOldRecords() {
        String[] args = {this.ScrbId, this.curFreqLoc};
        Cursor cursor = null;
        int recordsCnt = 0;
        String expireDate = null;
        if (this.db != null) {
            try {
                cursor = this.db.rawQuery("SELECT COUNT(DISTINCT UPDATE_DATE) RECORDCNT, MIN(UPDATE_DATE) EXPDATE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ?", args);
                if (cursor.moveToNext()) {
                    recordsCnt = cursor.getInt(cursor.getColumnIndexOrThrow("RECORDCNT"));
                    expireDate = cursor.getString(cursor.getColumnIndexOrThrow("EXPDATE"));
                }
            } catch (IllegalArgumentException e) {
                LogUtil.e("networkIdFoundBaseBySpaceNetwork IllegalArgumentException: " + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e("networkIdFoundBaseBySpaceNetwork Exception: " + e2.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
                if (30 < recordsCnt) {
                    LogUtil.i("deleteOldRecords: current records exceed (30) days");
                    String[] args1 = {this.ScrbId, this.curFreqLoc, expireDate};
                    try {
                        this.db.beginTransaction();
                        this.db.execSQL("DELETE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND UPDATE_DATE <= ?", args1);
                        LogUtil.i("deleteOldRecords sql=" + "DELETE FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND UPDATE_DATE <= ?");
                        Iterator<Map.Entry<Integer, Float>> it = mQoeAppBuilder.getQoEAppList().entrySet().iterator();
                        while (it.hasNext()) {
                            String tableName = Constant.USERDB_APP_NAME_PREFIX + it.next().getKey();
                            LogUtil.i("deleteOldRecords sql=" + sql);
                            this.db.execSQL("DELETE FROM " + tableName + " WHERE SCRBID = ? AND FREQLOCNAME = ? AND UPDATE_DATE <= ?", args1);
                        }
                        this.db.setTransactionSuccessful();
                    } catch (IllegalArgumentException e3) {
                        LogUtil.e("networkIdFoundBaseBySpaceNetwork IllegalArgumentException: " + e3.getMessage());
                    } catch (Exception e4) {
                        LogUtil.e("networkIdFoundBaseBySpaceNetwork Exception: " + e4.getMessage());
                    } catch (Throwable th) {
                        this.db.endTransaction();
                        throw th;
                    }
                    this.db.endTransaction();
                } else {
                    LogUtil.i("deleteOldRecords: keep data");
                }
            } catch (Throwable th2) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th2;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0279, code lost:
        if (r14 != null) goto L_0x027b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x02da, code lost:
        if (r14 == null) goto L_0x02dd;
     */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x02b4 A[Catch:{ IllegalArgumentException -> 0x02b5, Exception -> 0x028d, all -> 0x027f, all -> 0x02de }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x02e1  */
    public HashMap<String, List> findAppQoEgroupByAllSpace(String appName, int spaceid_allAp) {
        String sql;
        long appDuration;
        int appGood;
        String title_duration;
        int appPoor;
        String title_poor;
        int days;
        String title_good;
        String title_idCount = "COUNT(\"NETWORKID\")";
        String title_freqCount = "COUNT(\"NETWORKFREQ\")";
        String title_duration2 = "SUM(\"DURATION\")";
        String title_poor2 = "SUM(\"POORCOUNT\")";
        String title_good2 = "SUM(\"GOODCOUNT\")";
        String title_dayCount = "COUNT(DISTINCT UPDATE_DATE)";
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        LogUtil.i("findAppQoEgroupByAllSpace:" + sql + ",FreqLoc:" + this.curFreqLoc + ", space_allAp:" + spaceid_allAp + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
        StringBuilder sb = new StringBuilder();
        sb.append("findAppQoEgroupByAllSpace, ScrbId:");
        sb.append(this.ScrbId_print);
        LogUtil.v(sb.toString());
        HashMap<String, List> results = new HashMap<>();
        Cursor cursor = null;
        if (this.db == null) {
            return results;
        }
        try {
            cursor = this.db.rawQuery(sql, args);
            while (cursor.moveToNext()) {
                ArrayList arrayList = new ArrayList();
                int networktype = cursor.getInt(cursor.getColumnIndexOrThrow("NW_TYPE"));
                int netIdCnt = cursor.getInt(cursor.getColumnIndexOrThrow(title_idCount));
                String title_idCount2 = title_idCount;
                try {
                    int netFreqCnt = cursor.getInt(cursor.getColumnIndexOrThrow(title_freqCount));
                    String title_freqCount2 = title_freqCount;
                    try {
                        appDuration = cursor.getLong(cursor.getColumnIndexOrThrow(title_duration2));
                        appGood = cursor.getInt(cursor.getColumnIndexOrThrow(title_good2));
                        title_duration = title_duration2;
                        try {
                            appPoor = cursor.getInt(cursor.getColumnIndexOrThrow(title_poor2));
                            title_poor = title_poor2;
                        } catch (IllegalArgumentException e) {
                            e = e;
                            String str = title_poor2;
                            String str2 = title_good2;
                            String str3 = title_dayCount;
                            LogUtil.e("findAppQoEgroupByAllSpace IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e2) {
                            e = e2;
                            String str4 = title_poor2;
                            String str5 = title_good2;
                            String str6 = title_dayCount;
                            LogUtil.e("findAppQoEgroupByAllSpace Exception: " + e.getMessage());
                            if (cursor != null) {
                            }
                            return results;
                        } catch (Throwable th) {
                            th = th;
                            String str7 = title_poor2;
                            String str8 = title_good2;
                            String str9 = title_dayCount;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    } catch (IllegalArgumentException e3) {
                        e = e3;
                        String str10 = title_duration2;
                        String str11 = title_poor2;
                        String str12 = title_good2;
                        String str13 = title_dayCount;
                        LogUtil.e("findAppQoEgroupByAllSpace IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e4) {
                        e = e4;
                        String str14 = title_duration2;
                        String str15 = title_poor2;
                        String str16 = title_good2;
                        String str17 = title_dayCount;
                        LogUtil.e("findAppQoEgroupByAllSpace Exception: " + e.getMessage());
                        if (cursor != null) {
                            cursor.close();
                        }
                        return results;
                    } catch (Throwable th2) {
                        th = th2;
                        String str18 = title_duration2;
                        String str19 = title_poor2;
                        String str20 = title_good2;
                        String str21 = title_dayCount;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                    try {
                        days = cursor.getInt(cursor.getColumnIndexOrThrow(title_dayCount));
                        title_good = title_good2;
                    } catch (IllegalArgumentException e5) {
                        e = e5;
                        String str22 = title_good2;
                        String str23 = title_dayCount;
                        LogUtil.e("findAppQoEgroupByAllSpace IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e6) {
                        e = e6;
                        String str24 = title_good2;
                        String str25 = title_dayCount;
                        LogUtil.e("findAppQoEgroupByAllSpace Exception: " + e.getMessage());
                        if (cursor != null) {
                        }
                        return results;
                    } catch (Throwable th3) {
                        th = th3;
                        String str26 = title_good2;
                        String str27 = title_dayCount;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                    try {
                        arrayList.add(Integer.valueOf(networktype));
                        arrayList.add(Integer.valueOf(netIdCnt));
                        arrayList.add(Integer.valueOf(netFreqCnt));
                        int i = netFreqCnt;
                        String title_dayCount2 = title_dayCount;
                        long appDuration2 = appDuration;
                        try {
                            arrayList.add(Long.valueOf(appDuration2));
                            arrayList.add(Integer.valueOf(appGood));
                            arrayList.add(Integer.valueOf(appPoor));
                            arrayList.add(0);
                            arrayList.add(0);
                            arrayList.add(Integer.valueOf(days));
                            String networkname = cursor.getString(cursor.getColumnIndexOrThrow("NETWORKNAME"));
                            results.put(networkname, arrayList);
                            StringBuilder sb2 = new StringBuilder();
                            ArrayList arrayList2 = arrayList;
                            sb2.append(" networkname:");
                            sb2.append(networkname);
                            sb2.append(",type:");
                            sb2.append(networktype);
                            sb2.append(", appDuration:");
                            sb2.append(appDuration2);
                            sb2.append(",appPoor:");
                            sb2.append(appPoor);
                            sb2.append(",appGood:");
                            sb2.append(appGood);
                            sb2.append(", modelVer:");
                            sb2.append(this.modelVerAllAp);
                            sb2.append("_");
                            sb2.append(this.modelVerMainAp);
                            LogUtil.i(sb2.toString());
                            title_idCount = title_idCount2;
                            title_freqCount = title_freqCount2;
                            title_duration2 = title_duration;
                            title_poor2 = title_poor;
                            title_good2 = title_good;
                            title_dayCount = title_dayCount2;
                            String str28 = appName;
                        } catch (IllegalArgumentException e7) {
                            e = e7;
                            LogUtil.e("findAppQoEgroupByAllSpace IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e8) {
                            e = e8;
                            LogUtil.e("findAppQoEgroupByAllSpace Exception: " + e.getMessage());
                            if (cursor != null) {
                            }
                            return results;
                        }
                    } catch (IllegalArgumentException e9) {
                        e = e9;
                        String str29 = title_dayCount;
                        LogUtil.e("findAppQoEgroupByAllSpace IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e10) {
                        e = e10;
                        String str30 = title_dayCount;
                        LogUtil.e("findAppQoEgroupByAllSpace Exception: " + e.getMessage());
                        if (cursor != null) {
                        }
                        return results;
                    } catch (Throwable th4) {
                        th = th4;
                        String str31 = title_dayCount;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } catch (IllegalArgumentException e11) {
                    e = e11;
                    String str32 = title_freqCount;
                    String str33 = title_duration2;
                    String str34 = title_poor2;
                    String str35 = title_good2;
                    String str36 = title_dayCount;
                    LogUtil.e("findAppQoEgroupByAllSpace IllegalArgumentException: " + e.getMessage());
                } catch (Exception e12) {
                    e = e12;
                    String str37 = title_freqCount;
                    String str38 = title_duration2;
                    String str39 = title_poor2;
                    String str40 = title_good2;
                    String str41 = title_dayCount;
                    LogUtil.e("findAppQoEgroupByAllSpace Exception: " + e.getMessage());
                    if (cursor != null) {
                    }
                    return results;
                } catch (Throwable th5) {
                    th = th5;
                    String str42 = title_freqCount;
                    String str43 = title_duration2;
                    String str44 = title_poor2;
                    String str45 = title_good2;
                    String str46 = title_dayCount;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
            String str47 = title_freqCount;
            String str48 = title_duration2;
            String str49 = title_poor2;
            String str50 = title_good2;
            String str51 = title_dayCount;
        } catch (IllegalArgumentException e13) {
            e = e13;
            String str52 = title_idCount;
            String str53 = title_freqCount;
            String str54 = title_duration2;
            String str55 = title_poor2;
            String str56 = title_good2;
            String str57 = title_dayCount;
            LogUtil.e("findAppQoEgroupByAllSpace IllegalArgumentException: " + e.getMessage());
        } catch (Exception e14) {
            e = e14;
            String str58 = title_idCount;
            String str59 = title_freqCount;
            String str60 = title_duration2;
            String str61 = title_poor2;
            String str62 = title_good2;
            String str63 = title_dayCount;
            LogUtil.e("findAppQoEgroupByAllSpace Exception: " + e.getMessage());
            if (cursor != null) {
            }
            return results;
        } catch (Throwable th6) {
            th = th6;
            if (cursor != null) {
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:60:0x028c, code lost:
        if (r15 != null) goto L_0x028e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x02ed, code lost:
        if (r15 == null) goto L_0x02f0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x02c7 A[Catch:{ IllegalArgumentException -> 0x02c8, Exception -> 0x02a0, all -> 0x0292, all -> 0x02f1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x02f4  */
    public HashMap<String, List> findAppQoEgroupBySpace(String appName, int spaceid_allAp, int spaceid_mainAp) {
        String sql;
        int netFreqCnt;
        String title_freqCount;
        long appDuration;
        int appGood;
        String title_duration;
        int days;
        String title_good;
        String title_idCount = "COUNT(\"NETWORKID\")";
        String title_freqCount2 = "COUNT(\"NETWORKFREQ\")";
        String title_duration2 = "SUM(\"DURATION\")";
        String title_poor = "SUM(\"POORCOUNT\")";
        String title_good2 = "SUM(\"GOODCOUNT\")";
        String title_dayCount = "COUNT(DISTINCT UPDATE_DATE)";
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(spaceid_mainAp), Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        LogUtil.i("findAppQoEgroupBySpace:" + sql + ",FreqLoc:" + this.curFreqLoc + ", space_allAp:" + spaceid_allAp + ", space_mainAp:" + spaceid_mainAp + ", modelVer:" + this.modelVerAllAp + "_" + this.modelVerMainAp);
        StringBuilder sb = new StringBuilder();
        sb.append("findAppQoEgroupBySpace, ScrbId:");
        sb.append(this.ScrbId_print);
        LogUtil.v(sb.toString());
        HashMap<String, List> results = new HashMap<>();
        Cursor cursor = null;
        if (this.db == null) {
            return results;
        }
        try {
            cursor = this.db.rawQuery(sql, args);
            while (cursor.moveToNext()) {
                ArrayList arrayList = new ArrayList();
                int networktype = cursor.getInt(cursor.getColumnIndexOrThrow("NW_TYPE"));
                int netIdCnt = cursor.getInt(cursor.getColumnIndexOrThrow(title_idCount));
                String title_idCount2 = title_idCount;
                try {
                    netFreqCnt = cursor.getInt(cursor.getColumnIndexOrThrow(title_freqCount2));
                    title_freqCount = title_freqCount2;
                    try {
                        appDuration = cursor.getLong(cursor.getColumnIndexOrThrow(title_duration2));
                        appGood = cursor.getInt(cursor.getColumnIndexOrThrow(title_good2));
                        title_duration = title_duration2;
                    } catch (IllegalArgumentException e) {
                        e = e;
                        String str = title_duration2;
                        String str2 = title_poor;
                        String str3 = title_good2;
                        String str4 = title_dayCount;
                        LogUtil.e("findAppQoEgroupBySpace IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e2) {
                        e = e2;
                        String str5 = title_duration2;
                        String str6 = title_poor;
                        String str7 = title_good2;
                        String str8 = title_dayCount;
                        LogUtil.e("findAppQoEgroupBySpace Exception: " + e.getMessage());
                        if (cursor != null) {
                            cursor.close();
                        }
                        return results;
                    } catch (Throwable th) {
                        th = th;
                        String str9 = title_duration2;
                        String str10 = title_poor;
                        String str11 = title_good2;
                        String str12 = title_dayCount;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (IllegalArgumentException e3) {
                    e = e3;
                    String str13 = title_freqCount2;
                    String str14 = title_duration2;
                    String str15 = title_poor;
                    String str16 = title_good2;
                    String str17 = title_dayCount;
                    LogUtil.e("findAppQoEgroupBySpace IllegalArgumentException: " + e.getMessage());
                } catch (Exception e4) {
                    e = e4;
                    String str18 = title_freqCount2;
                    String str19 = title_duration2;
                    String str20 = title_poor;
                    String str21 = title_good2;
                    String str22 = title_dayCount;
                    LogUtil.e("findAppQoEgroupBySpace Exception: " + e.getMessage());
                    if (cursor != null) {
                    }
                    return results;
                } catch (Throwable th2) {
                    th = th2;
                    String str23 = title_freqCount2;
                    String str24 = title_duration2;
                    String str25 = title_poor;
                    String str26 = title_good2;
                    String str27 = title_dayCount;
                    if (cursor != null) {
                    }
                    throw th;
                }
                try {
                    int appPoor = cursor.getInt(cursor.getColumnIndexOrThrow(title_poor));
                    String title_poor2 = title_poor;
                    try {
                        days = cursor.getInt(cursor.getColumnIndexOrThrow(title_dayCount));
                        title_good = title_good2;
                    } catch (IllegalArgumentException e5) {
                        e = e5;
                        String str28 = title_good2;
                        String str29 = title_dayCount;
                        LogUtil.e("findAppQoEgroupBySpace IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e6) {
                        e = e6;
                        String str30 = title_good2;
                        String str31 = title_dayCount;
                        LogUtil.e("findAppQoEgroupBySpace Exception: " + e.getMessage());
                        if (cursor != null) {
                        }
                        return results;
                    } catch (Throwable th3) {
                        th = th3;
                        String str32 = title_good2;
                        String str33 = title_dayCount;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                    try {
                        arrayList.add(Integer.valueOf(networktype));
                        arrayList.add(Integer.valueOf(netIdCnt));
                        arrayList.add(Integer.valueOf(netFreqCnt));
                        int i = netFreqCnt;
                        String title_dayCount2 = title_dayCount;
                        long appDuration2 = appDuration;
                        try {
                            arrayList.add(Long.valueOf(appDuration2));
                            arrayList.add(Integer.valueOf(appGood));
                            arrayList.add(Integer.valueOf(appPoor));
                            int i2 = netIdCnt;
                            arrayList.add(0);
                            arrayList.add(0);
                            arrayList.add(Integer.valueOf(days));
                            String networkname = cursor.getString(cursor.getColumnIndexOrThrow("NETWORKNAME"));
                            results.put(networkname, arrayList);
                            StringBuilder sb2 = new StringBuilder();
                            ArrayList arrayList2 = arrayList;
                            sb2.append(" networkname:");
                            sb2.append(networkname);
                            sb2.append(",type:");
                            sb2.append(networktype);
                            sb2.append(", appDuration:");
                            sb2.append(appDuration2);
                            sb2.append(",appPoor:");
                            sb2.append(appPoor);
                            sb2.append(",appGood:");
                            sb2.append(appGood);
                            sb2.append(", modelVer:");
                            sb2.append(this.modelVerAllAp);
                            sb2.append("_");
                            sb2.append(this.modelVerMainAp);
                            LogUtil.i(sb2.toString());
                            title_idCount = title_idCount2;
                            title_freqCount2 = title_freqCount;
                            title_duration2 = title_duration;
                            title_poor = title_poor2;
                            title_good2 = title_good;
                            title_dayCount = title_dayCount2;
                            String str34 = appName;
                        } catch (IllegalArgumentException e7) {
                            e = e7;
                            LogUtil.e("findAppQoEgroupBySpace IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e8) {
                            e = e8;
                            LogUtil.e("findAppQoEgroupBySpace Exception: " + e.getMessage());
                            if (cursor != null) {
                            }
                            return results;
                        }
                    } catch (IllegalArgumentException e9) {
                        e = e9;
                        String str35 = title_dayCount;
                        LogUtil.e("findAppQoEgroupBySpace IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e10) {
                        e = e10;
                        String str36 = title_dayCount;
                        LogUtil.e("findAppQoEgroupBySpace Exception: " + e.getMessage());
                        if (cursor != null) {
                        }
                        return results;
                    } catch (Throwable th4) {
                        th = th4;
                        String str37 = title_dayCount;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                } catch (IllegalArgumentException e11) {
                    e = e11;
                    String str38 = title_poor;
                    String str39 = title_good2;
                    String str40 = title_dayCount;
                    LogUtil.e("findAppQoEgroupBySpace IllegalArgumentException: " + e.getMessage());
                } catch (Exception e12) {
                    e = e12;
                    String str41 = title_poor;
                    String str42 = title_good2;
                    String str43 = title_dayCount;
                    LogUtil.e("findAppQoEgroupBySpace Exception: " + e.getMessage());
                    if (cursor != null) {
                    }
                    return results;
                } catch (Throwable th5) {
                    th = th5;
                    String str44 = title_poor;
                    String str45 = title_good2;
                    String str46 = title_dayCount;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
            String str47 = title_freqCount2;
            String str48 = title_duration2;
            String str49 = title_poor;
            String str50 = title_good2;
            String str51 = title_dayCount;
        } catch (IllegalArgumentException e13) {
            e = e13;
            String str52 = title_idCount;
            String str53 = title_freqCount2;
            String str54 = title_duration2;
            String str55 = title_poor;
            String str56 = title_good2;
            String str57 = title_dayCount;
            LogUtil.e("findAppQoEgroupBySpace IllegalArgumentException: " + e.getMessage());
        } catch (Exception e14) {
            e = e14;
            String str58 = title_idCount;
            String str59 = title_freqCount2;
            String str60 = title_duration2;
            String str61 = title_poor;
            String str62 = title_good2;
            String str63 = title_dayCount;
            LogUtil.e("findAppQoEgroupBySpace Exception: " + e.getMessage());
            if (cursor != null) {
            }
            return results;
        } catch (Throwable th6) {
            th = th6;
            if (cursor != null) {
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x015e, code lost:
        if (r6 == null) goto L_0x0161;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0121, code lost:
        if (r6 != null) goto L_0x0123;
     */
    public HashMap<String, Bundle> findUserPrefByAllApSpaces(int spaceid_allAp) {
        LogUtil.i("findUserPrefByAllApSpaces:" + "SELECT NETWORKNAME, NW_TYPE, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(USER_PREF_TOTAL_COUNT), SUM(DURATION_CONNECTED) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? GROUP BY NETWORKNAME" + ",FreqLoc:" + this.curFreqLoc + ", space_allAp:" + spaceid_allAp);
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(this.modelVerAllAp)};
        HashMap<String, Bundle> results = new HashMap<>();
        Cursor cursor = null;
        if (this.db == null) {
            return results;
        }
        try {
            cursor = this.db.rawQuery("SELECT NETWORKNAME, NW_TYPE, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(USER_PREF_TOTAL_COUNT), SUM(DURATION_CONNECTED) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? GROUP BY NETWORKNAME", args);
            while (cursor.moveToNext()) {
                int user_pref_opt_in = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_OPT_IN)"));
                int user_pref_opt_out = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_OPT_OUT)"));
                int user_pref_stay = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_STAY)"));
                int user_pref_entery = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_TOTAL_COUNT)"));
                String networkname = cursor.getString(cursor.getColumnIndexOrThrow("NETWORKNAME"));
                int networktype = cursor.getInt(cursor.getColumnIndexOrThrow("NW_TYPE"));
                long duration_connected = cursor.getLong(cursor.getColumnIndexOrThrow("SUM(DURATION_CONNECTED)"));
                Bundle statistics = new Bundle();
                statistics.putString("networkname", networkname);
                statistics.putInt("networktype", networktype);
                statistics.putInt("user_pref_opt_in", user_pref_opt_in);
                statistics.putInt("user_pref_opt_out", user_pref_opt_out);
                statistics.putInt("user_pref_stay", user_pref_stay);
                statistics.putInt("user_pref_entery", user_pref_entery);
                statistics.putLong("duration_connected", duration_connected);
                results.put(networkname, statistics);
                LogUtil.i(" networkname:" + networkname + ",type:" + networktype + ",user_pref_opt_in:" + user_pref_opt_in + ",user_pref_opt_out:" + user_pref_opt_out + ",user_pref_stay:" + user_pref_stay + ",duration_connected:" + duration_connected + ",user_pref_entery:" + user_pref_entery);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("findUserPrefByAllApSpaces IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findUserPrefByAllApSpaces Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return results;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x015e, code lost:
        if (r6 == null) goto L_0x0161;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0121, code lost:
        if (r6 != null) goto L_0x0123;
     */
    public HashMap<String, Bundle> findUserPrefByMainApSpaces(int spaceid_mainAp) {
        LogUtil.i("findUserPrefByTwoSpaces:" + "SELECT NETWORKNAME, NW_TYPE, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(USER_PREF_TOTAL_COUNT), SUM(DURATION_CONNECTED) FROM SPACEUSER_BASE WHERE IMSI = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? GROUP BY NETWORKNAME" + ",FreqLoc:" + this.curFreqLoc + ", space_allAp:" + spaceid_mainAp);
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_mainAp), Integer.toString(this.modelVerMainAp)};
        HashMap<String, Bundle> results = new HashMap<>();
        Cursor cursor = null;
        if (this.db == null) {
            return results;
        }
        try {
            cursor = this.db.rawQuery("SELECT NETWORKNAME, NW_TYPE, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(USER_PREF_TOTAL_COUNT), SUM(DURATION_CONNECTED) FROM SPACEUSER_BASE WHERE IMSI = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? GROUP BY NETWORKNAME", args);
            while (cursor.moveToNext()) {
                int user_pref_opt_in = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_OPT_IN)"));
                int user_pref_opt_out = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_OPT_OUT)"));
                int user_pref_stay = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_STAY)"));
                int user_pref_entery = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_TOTAL_COUNT)"));
                String networkname = cursor.getString(cursor.getColumnIndexOrThrow("NETWORKNAME"));
                int networktype = cursor.getInt(cursor.getColumnIndexOrThrow("NW_TYPE"));
                long duration_connected = cursor.getLong(cursor.getColumnIndexOrThrow("SUM(DURATION_CONNECTED)"));
                Bundle statistics = new Bundle();
                statistics.putString("networkname", networkname);
                statistics.putInt("networktype", networktype);
                statistics.putInt("user_pref_opt_in", user_pref_opt_in);
                statistics.putInt("user_pref_opt_out", user_pref_opt_out);
                statistics.putInt("user_pref_stay", user_pref_stay);
                statistics.putInt("user_pref_entery", user_pref_entery);
                statistics.putLong("duration_connected", duration_connected);
                results.put(networkname, statistics);
                LogUtil.i(" networkname:" + networkname + ",type:" + networktype + ",user_pref_opt_in:" + user_pref_opt_in + ",user_pref_opt_out:" + user_pref_opt_out + ",user_pref_stay:" + user_pref_stay + ",duration_connected:" + duration_connected + ",user_pref_entery:" + user_pref_entery);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("findUserPrefByTwoSpaces IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findUserPrefByTwoSpaces Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return results;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public void setUserPrefEnteryFlag(String netName, int netType, int spaceid_allAp, int flag) {
        LogUtil.i("setUserPrefEnteryFlag SET:" + "UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = ? WHERE NETWORKNAME = ? AND NW_TYPE = ? AND SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?" + ", NW Name:" + netName + ", NW Type:" + netType + ", Entery:" + flag + ", FreqLoc:" + this.curFreqLoc + ", space_allAp:" + spaceid_allAp);
        String[] args = {Integer.toString(flag), netName, Integer.toString(netType), this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(this.modelVerAllAp)};
        StringBuilder sb = new StringBuilder();
        sb.append("setUserPrefEnteryFlag CLN:");
        sb.append("UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = ? WHERE NETWORKNAME = ? AND NW_TYPE = ? AND SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?");
        sb.append(", FreqLoc:");
        sb.append(this.curFreqLoc);
        sb.append(", space_allAp:");
        sb.append(spaceid_allAp);
        LogUtil.i(sb.toString());
        Object[] args_c = {this.ScrbId, this.curFreqLoc, Integer.valueOf(spaceid_allAp), Integer.valueOf(this.modelVerAllAp)};
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = 0 WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", args_c);
            this.db.execSQL("UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = ? WHERE NETWORKNAME = ? AND NW_TYPE = ? AND SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", args);
            this.db.setTransactionSuccessful();
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
        this.db.endTransaction();
    }

    public void clearUserPrefEnteryFlag(int spaceid_allAp) {
        LogUtil.i("clearUserPrefEnteryFlag:" + "UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = 0 WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?" + ", FreqLoc:" + this.curFreqLoc + ", space_allAp:" + spaceid_allAp);
        Object[] args = {this.ScrbId, this.curFreqLoc, Integer.valueOf(spaceid_allAp), Integer.valueOf(this.modelVerAllAp)};
        try {
            this.db.beginTransaction();
            this.db.execSQL("UPDATE SPACEUSER_BASE SET USER_PREF_TOTAL_COUNT = 0 WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ?", args);
            this.db.setTransactionSuccessful();
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
        } catch (Throwable th) {
            this.db.endTransaction();
            throw th;
        }
        this.db.endTransaction();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00d9, code lost:
        if (r3 == null) goto L_0x00dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x009c, code lost:
        if (r3 != null) goto L_0x009e;
     */
    public ArrayList<Integer> findAllApSpaceIdByMainApSpace(int spaceid_mainAp, int netType) {
        LogUtil.i("findAllApSpaceIdByMainApSpace:" + "SELECT SPACEID FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? AND NW_TYPE = ? GROUP BY SPACEID, SPACEIDMAIN" + ",FreqLoc:" + this.curFreqLoc + ", spaceid_mainAp:" + spaceid_mainAp + ", ver:" + this.modelVerMainAp + ", netType:" + netType);
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_mainAp), Integer.toString(this.modelVerMainAp), Integer.toString(netType)};
        ArrayList<Integer> results = new ArrayList<>();
        Cursor cursor = null;
        if (this.db == null) {
            return results;
        }
        try {
            cursor = this.db.rawQuery("SELECT SPACEID FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? AND NW_TYPE = ? GROUP BY SPACEID, SPACEIDMAIN", args);
            while (cursor.moveToNext()) {
                int spaceAllAp = cursor.getInt(cursor.getColumnIndexOrThrow("SPACEID"));
                results.add(Integer.valueOf(spaceAllAp));
                LogUtil.i(" findAllApSpaceIdByMainApSpace, spaceId:" + spaceAllAp);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("findAllApSpaceIdByMainApSpace IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findAllApSpaceIdByMainApSpace Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return results;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public Bundle find4gCoverageByCurrLoc() {
        return queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE*DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND (SPACEID != ? OR SPACEIDMAIN != ?) AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", new String[]{this.ScrbId, this.curFreqLoc, "0", "0", Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)});
    }

    public Bundle find4gCoverageByBothSpace(int spaceid_allAp, int spaceid_mainAp) {
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(spaceid_mainAp), Integer.toString(this.modelVerAllAp), Integer.toString(this.modelVerMainAp)};
        Bundle coverage4G = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE*DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        coverage4G.putLong("duration_out4g", queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE*DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND SPACEIDMAIN = ? AND MODEL_VER_ALLAP = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME!='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args).getLong("total_duration"));
        return coverage4G;
    }

    public Bundle find4gCoverageByAllApSpace(int spaceid_allAp) {
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_allAp), Integer.toString(this.modelVerAllAp)};
        Bundle coverage4G = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE*DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? AND NETWORKNAME='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        coverage4G.putLong("duration_out4g", queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE*DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? AND NETWORKNAME!='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args).getLong("total_duration"));
        return coverage4G;
    }

    public Bundle find4gCoverageByMainApSpace(int spaceid_mainAp) {
        String[] args = {this.ScrbId, this.curFreqLoc, Integer.toString(spaceid_mainAp), Integer.toString(this.modelVerMainAp)};
        Bundle coverage4G = queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE*DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args);
        coverage4G.putLong("duration_out4g", queryCoverage("SELECT NW_TYPE, SUM(SIGNAL_VALUE*DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, COUNT(SIGNAL_VALUE) CELL_NUM, SUM(DURATION_CONNECTED) TOTAL_DURATION FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEIDMAIN = ? AND MODEL_VER_MAINAP = ? AND NETWORKNAME!='4G' AND NW_TYPE=0 GROUP BY NW_TYPE", args).getLong("total_duration"));
        return coverage4G;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00e8, code lost:
        if (r1 == null) goto L_0x00eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x00a9, code lost:
        if (r1 != null) goto L_0x00ab;
     */
    private Bundle queryCoverage(String sql, String[] args) {
        LogUtil.i("query4gCoverage: aql=" + sql + " ,args=" + Arrays.toString(Arrays.copyOfRange(args, 1, args.length)));
        Bundle results = new Bundle();
        Cursor cursor = null;
        if (this.db == null) {
            return results;
        }
        try {
            cursor = this.db.rawQuery(sql, args);
            while (cursor.moveToNext()) {
                int cellNum = cursor.getInt(cursor.getColumnIndexOrThrow("CELL_NUM"));
                int avgSignal = cursor.getInt(cursor.getColumnIndexOrThrow("AVG_SIGNAL"));
                long totalDuration = cursor.getLong(cursor.getColumnIndexOrThrow("TOTAL_DURATION"));
                int networktype = cursor.getInt(cursor.getColumnIndexOrThrow("NW_TYPE"));
                results.putInt("networktype", networktype);
                results.putInt("cell_num", cellNum);
                results.putInt("avg_signal", avgSignal);
                results.putLong("total_duration", totalDuration);
                LogUtil.i(" type:" + networktype + ",cell_num:" + cellNum + ",avg_signal:" + avgSignal + ",total_duration:" + totalDuration);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("query4gCoverage IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("query4gCoverage Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return results;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x035c, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0381, code lost:
        if (r7 == null) goto L_0x0384;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x035a, code lost:
        if (r7 == null) goto L_0x0384;
     */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x038a  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:97:0x0342=Splitter:B:97:0x0342, B:103:0x0369=Splitter:B:103:0x0369} */
    public HashMap<Integer, Bundle> findAppQoERecByFreqLoc(String freqlocation) {
        Cursor cursor;
        int app;
        ArrayList<String> argList;
        long duration;
        int poorcount;
        String sql;
        Bundle statistics;
        Cursor cursor2;
        String str = freqlocation;
        String sql2 = "";
        int count = 0;
        HashMap<Integer, Float> mAppMap = mQoeAppBuilder.getQoEAppList();
        HashMap<Integer, Bundle> results = new HashMap<>();
        ArrayList<String> argList2 = new ArrayList<>();
        Cursor cursor3 = null;
        if (this.db == null) {
            return results;
        }
        for (Map.Entry<Integer, Float> entry : mAppMap.entrySet()) {
            int appNum = entry.getKey().intValue();
            String appTableName = Constant.USERDB_APP_NAME_PREFIX + appNum;
            if (count == 0) {
                sql2 = "SELECT " + appNum + " AS NAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, COUNT(DISTINCT NETWORKID) NWIDCNT, NETWORKNAME, COUNT(DISTINCT NETWORKFREQ) NWFREQCNT, NW_TYPE, COUNT(DISTINCT UPDATE_DATE) REC, SUM(DURATION), SUM(POORCOUNT), SUM(GOODCOUNT) FROM " + appTableName + " WHERE SCRBID = ? AND FREQLOCNAME = ? GROUP BY NETWORKNAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, NW_TYPE, NAME";
            } else {
                sql2 = sql2 + " UNION SELECT " + appNum + " AS NAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, COUNT(DISTINCT NETWORKID) NWIDCNT, NETWORKNAME, COUNT(DISTINCT NETWORKFREQ) NWFREQCNT, NW_TYPE, COUNT(DISTINCT UPDATE_DATE) REC, SUM(DURATION), SUM(POORCOUNT), SUM(GOODCOUNT) FROM " + appTableName + " WHERE SCRBID = ? AND FREQLOCNAME = ? GROUP BY NETWORKNAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, NW_TYPE, NAME";
            }
            count++;
            argList2.add(this.ScrbId);
            argList2.add(str);
        }
        String sql3 = sql2 + " ORDER BY SUM(DURATION) DESC";
        String[] args = (String[]) argList2.toArray(new String[argList2.size()]);
        LogUtil.i("findAppQoERecByFreqLoc:" + sql3 + ",FreqLoc:" + str + ",args: " + Arrays.toString(args));
        int count2 = 0;
        try {
            cursor3 = this.db.rawQuery(sql3, args);
            while (cursor3.moveToNext()) {
                try {
                    count2++;
                } catch (IllegalArgumentException e) {
                    e = e;
                    HashMap<Integer, Float> hashMap = mAppMap;
                    ArrayList<String> arrayList = argList2;
                    Cursor cursor4 = cursor3;
                    String str2 = sql3;
                    String[] strArr = args;
                    LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
                } catch (Exception e2) {
                    e = e2;
                    HashMap<Integer, Float> hashMap2 = mAppMap;
                    ArrayList<String> arrayList2 = argList2;
                    Cursor cursor5 = cursor3;
                    String str3 = sql3;
                    String[] strArr2 = args;
                    try {
                        LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
                    } catch (Throwable th) {
                        th = th;
                        int i = count2;
                        if (cursor3 != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    HashMap<Integer, Float> hashMap3 = mAppMap;
                    ArrayList<String> arrayList3 = argList2;
                    Cursor cursor6 = cursor3;
                    String str4 = sql3;
                    String[] strArr3 = args;
                    int i2 = count2;
                    if (cursor3 != null) {
                    }
                    throw th;
                }
                try {
                    int spaceid = cursor3.getInt(cursor3.getColumnIndexOrThrow("SPACEID"));
                    int modelVerAllap = cursor3.getInt(cursor3.getColumnIndexOrThrow("MODEL_VER_ALLAP"));
                    int spaceidmain = cursor3.getInt(cursor3.getColumnIndexOrThrow("SPACEIDMAIN"));
                    int modelVerMainap = cursor3.getInt(cursor3.getColumnIndexOrThrow("MODEL_VER_MAINAP"));
                    int nwidcnt = cursor3.getInt(cursor3.getColumnIndexOrThrow("NWIDCNT"));
                    String networkname = cursor3.getString(cursor3.getColumnIndexOrThrow("NETWORKNAME"));
                    short nwfreqcnt = cursor3.getShort(cursor3.getColumnIndexOrThrow("NWFREQCNT"));
                    int nwType = cursor3.getInt(cursor3.getColumnIndexOrThrow("NW_TYPE"));
                    short rec = cursor3.getShort(cursor3.getColumnIndexOrThrow("REC"));
                    HashMap<Integer, Float> mAppMap2 = mAppMap;
                    try {
                        app = cursor3.getInt(cursor3.getColumnIndexOrThrow("NAME"));
                        argList = argList2;
                    } catch (IllegalArgumentException e3) {
                        e = e3;
                        int i3 = count2;
                        ArrayList<String> arrayList4 = argList2;
                        Cursor cursor7 = cursor3;
                        String str5 = sql3;
                        String[] strArr4 = args;
                        LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e4) {
                        e = e4;
                        int i4 = count2;
                        ArrayList<String> arrayList5 = argList2;
                        Cursor cursor8 = cursor3;
                        String str6 = sql3;
                        String[] strArr5 = args;
                        LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
                    } catch (Throwable th3) {
                        th = th3;
                        int i5 = count2;
                        ArrayList<String> arrayList6 = argList2;
                        Cursor cursor9 = cursor3;
                        String str7 = sql3;
                        String[] strArr6 = args;
                        if (cursor3 != null) {
                        }
                        throw th;
                    }
                    try {
                        duration = cursor3.getLong(cursor3.getColumnIndexOrThrow("SUM(DURATION)"));
                        poorcount = cursor3.getInt(cursor3.getColumnIndexOrThrow("SUM(POORCOUNT)"));
                        sql = sql3;
                    } catch (IllegalArgumentException e5) {
                        e = e5;
                        int i6 = count2;
                        Cursor cursor10 = cursor3;
                        String str8 = sql3;
                        String[] strArr7 = args;
                        LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e6) {
                        e = e6;
                        int i7 = count2;
                        Cursor cursor11 = cursor3;
                        String str9 = sql3;
                        String[] strArr8 = args;
                        LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
                    } catch (Throwable th4) {
                        th = th4;
                        int i8 = count2;
                        Cursor cursor12 = cursor3;
                        String str10 = sql3;
                        String[] strArr9 = args;
                        if (cursor3 != null) {
                        }
                        throw th;
                    }
                    try {
                        int goodcount = cursor3.getInt(cursor3.getColumnIndexOrThrow("SUM(GOODCOUNT)"));
                        String[] args2 = args;
                        try {
                            statistics = new Bundle();
                            cursor2 = cursor3;
                        } catch (IllegalArgumentException e7) {
                            e = e7;
                            int i9 = count2;
                            Cursor cursor13 = cursor3;
                            LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e8) {
                            e = e8;
                            int i10 = count2;
                            Cursor cursor14 = cursor3;
                            LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
                        } catch (Throwable th5) {
                            th = th5;
                            int i11 = count2;
                            Cursor cursor15 = cursor3;
                            if (cursor3 != null) {
                            }
                            throw th;
                        }
                        try {
                            statistics.putInt("spaceid", spaceid);
                            statistics.putInt("modelVerAllap", modelVerAllap);
                            statistics.putInt("spaceidmain", spaceidmain);
                            statistics.putInt("modelVerMainap", modelVerMainap);
                            statistics.putInt("nwidcnt", nwidcnt);
                            statistics.putString("networkname", networkname);
                            statistics.putShort("nwfreqcnt", nwfreqcnt);
                            statistics.putInt("nwType", nwType);
                            statistics.putShort("rec", rec);
                            int i12 = nwType;
                            short s = rec;
                            long duration2 = duration;
                            statistics.putLong("duration", duration2);
                            statistics.putInt("app", app);
                            statistics.putInt("poorcount", poorcount);
                            statistics.putInt("goodcount", goodcount);
                            results.put(Integer.valueOf(count2), statistics);
                            StringBuilder sb = new StringBuilder();
                            int count3 = count2;
                            try {
                                sb.append(" networkname:");
                                sb.append(networkname);
                                sb.append(",app:");
                                sb.append(app);
                                sb.append(",poorcount:");
                                sb.append(poorcount);
                                sb.append(",goodcount:");
                                sb.append(goodcount);
                                sb.append(",duration:");
                                sb.append(duration2);
                                sb.append(",spaceid:");
                                sb.append(spaceid);
                                LogUtil.i(sb.toString());
                                mAppMap = mAppMap2;
                                argList2 = argList;
                                sql3 = sql;
                                args = args2;
                                cursor3 = cursor2;
                                count2 = count3;
                                String str11 = freqlocation;
                            } catch (IllegalArgumentException e9) {
                                e = e9;
                                cursor3 = cursor2;
                                count2 = count3;
                                LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
                            } catch (Exception e10) {
                                e = e10;
                                cursor3 = cursor2;
                                count2 = count3;
                                LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
                            } catch (Throwable th6) {
                                th = th6;
                                cursor3 = cursor2;
                                if (cursor3 != null) {
                                }
                                throw th;
                            }
                        } catch (IllegalArgumentException e11) {
                            e = e11;
                            int i13 = count2;
                            cursor3 = cursor2;
                            LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e12) {
                            e = e12;
                            int i14 = count2;
                            cursor3 = cursor2;
                            LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
                        } catch (Throwable th7) {
                            th = th7;
                            int i15 = count2;
                            cursor3 = cursor2;
                            if (cursor3 != null) {
                                cursor3.close();
                            }
                            throw th;
                        }
                    } catch (IllegalArgumentException e13) {
                        e = e13;
                        int i16 = count2;
                        Cursor cursor16 = cursor3;
                        String[] strArr10 = args;
                        LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e14) {
                        e = e14;
                        int i17 = count2;
                        Cursor cursor17 = cursor3;
                        String[] strArr11 = args;
                        LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
                    } catch (Throwable th8) {
                        th = th8;
                        int i18 = count2;
                        Cursor cursor18 = cursor3;
                        String[] strArr12 = args;
                        if (cursor3 != null) {
                        }
                        throw th;
                    }
                } catch (IllegalArgumentException e15) {
                    e = e15;
                    int i19 = count2;
                    HashMap<Integer, Float> hashMap4 = mAppMap;
                    ArrayList<String> arrayList7 = argList2;
                    Cursor cursor19 = cursor3;
                    String str12 = sql3;
                    String[] strArr13 = args;
                    LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
                } catch (Exception e16) {
                    e = e16;
                    int i20 = count2;
                    HashMap<Integer, Float> hashMap5 = mAppMap;
                    ArrayList<String> arrayList8 = argList2;
                    Cursor cursor20 = cursor3;
                    String str13 = sql3;
                    String[] strArr14 = args;
                    LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
                } catch (Throwable th9) {
                    th = th9;
                    int i21 = count2;
                    HashMap<Integer, Float> hashMap6 = mAppMap;
                    ArrayList<String> arrayList9 = argList2;
                    Cursor cursor21 = cursor3;
                    String str14 = sql3;
                    String[] strArr15 = args;
                    if (cursor3 != null) {
                    }
                    throw th;
                }
            }
            ArrayList<String> arrayList10 = argList2;
            Cursor cursor22 = cursor3;
            String str15 = sql3;
            String[] strArr16 = args;
            if (cursor22 != null) {
                cursor = cursor22;
                cursor.close();
            } else {
                cursor = cursor22;
            }
            Cursor cursor23 = cursor;
        } catch (IllegalArgumentException e17) {
            e = e17;
            HashMap<Integer, Float> hashMap7 = mAppMap;
            ArrayList<String> arrayList11 = argList2;
            String str16 = sql3;
            String[] strArr17 = args;
            LogUtil.e("findAppQoERecByFreqLoc IllegalArgumentException: " + e.getMessage());
        } catch (Exception e18) {
            e = e18;
            HashMap<Integer, Float> hashMap8 = mAppMap;
            ArrayList<String> arrayList12 = argList2;
            String str17 = sql3;
            String[] strArr18 = args;
            LogUtil.e("findAppQoERecByFreqLoc Exception: " + e.getMessage());
        } catch (Throwable th10) {
            th = th10;
            HashMap<Integer, Float> hashMap9 = mAppMap;
            ArrayList<String> arrayList13 = argList2;
            String str18 = sql3;
            String[] strArr19 = args;
            if (cursor3 != null) {
            }
            throw th;
        }
        return results;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:81:0x03f5, code lost:
        if (r7 != null) goto L_0x03f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x03f7, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x041b, code lost:
        if (r7 != null) goto L_0x03f7;
     */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0422  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:85:0x0403=Splitter:B:85:0x0403, B:79:0x03dd=Splitter:B:79:0x03dd} */
    public HashMap<Integer, Bundle> findUserExpRecByFreqLoc(String freqlocation) {
        HashMap<Integer, Bundle> results;
        Cursor cursor;
        int count;
        int spaceid;
        int modelVerAllap;
        int spaceidmain;
        int modelVerMainap;
        int nwidcnt;
        String networkname;
        short nwfreqcnt;
        int nwType;
        short rec;
        long duration_connected;
        long datarx;
        long datatx;
        short avgSignal;
        int user_pref_opt_in;
        String sql;
        int user_pref_stay;
        HashMap<Integer, Bundle> results2;
        long duration_connected2;
        long powerconsumption;
        long dubaiScreenoffTx;
        long dubaiScreenoffRx;
        long dubaiScreenoffPower;
        long dubaiScreenonTx;
        long dubaiScreenonRx;
        long dubaiScreenonPower;
        long dubaiIdleDuration;
        long dubaiIdleDuration2;
        Integer valueOf;
        int count2;
        String str = freqlocation;
        String sql2 = "SELECT SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, COUNT(DISTINCT NETWORKID) NWIDCNT, NETWORKNAME, COUNT(DISTINCT NETWORKFREQ) NWFREQCNT, NW_TYPE, COUNT(DISTINCT UPDATE_DATE) REC,SUM(DURATION_CONNECTED), SUM(DATA_RX), SUM(DATA_TX), SUM(SIGNAL_VALUE*DURATION_CONNECTED)/SUM(DURATION_CONNECTED) AVG_SIGNAL, SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY), SUM(POWER_CONSUMPTION), SUM(DUBAI_SCREENOFF_TX), SUM(DUBAI_SCREENOFF_RX), SUM(DUBAI_SCREENOFF_POWER), SUM(DUBAI_SCREENON_TX), SUM(DUBAI_SCREENON_RX), SUM(DUBAI_SCREENON_POWER), SUM(DUBAI_IDLE_DURATION), SUM(DUBAI_IDLE_POWER) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND (SPACEID != 0 OR SPACEIDMAIN != 0) GROUP BY NETWORKNAME, SPACEID, MODEL_VER_ALLAP, SPACEIDMAIN, MODEL_VER_MAINAP, NW_TYPE ORDER BY SUM(DURATION_CONNECTED) DESC";
        LogUtil.i("findUserPrefByFreqLoc:" + sql2 + ",FreqLoc:" + str);
        String[] args = {this.ScrbId, str};
        int count3 = 0;
        HashMap<Integer, Bundle> results3 = new HashMap<>();
        Cursor cursor2 = null;
        if (this.db == null) {
            return results3;
        }
        try {
            cursor2 = this.db.rawQuery(sql2, args);
            while (cursor2.moveToNext()) {
                try {
                    count = count3 + 1;
                    try {
                        spaceid = cursor2.getInt(cursor2.getColumnIndexOrThrow("SPACEID"));
                        modelVerAllap = cursor2.getInt(cursor2.getColumnIndexOrThrow("MODEL_VER_ALLAP"));
                        spaceidmain = cursor2.getInt(cursor2.getColumnIndexOrThrow("SPACEIDMAIN"));
                        modelVerMainap = cursor2.getInt(cursor2.getColumnIndexOrThrow("MODEL_VER_MAINAP"));
                        nwidcnt = cursor2.getInt(cursor2.getColumnIndexOrThrow("NWIDCNT"));
                        networkname = cursor2.getString(cursor2.getColumnIndexOrThrow("NETWORKNAME"));
                        nwfreqcnt = cursor2.getShort(cursor2.getColumnIndexOrThrow("NWFREQCNT"));
                        nwType = cursor2.getInt(cursor2.getColumnIndexOrThrow("NW_TYPE"));
                        rec = cursor2.getShort(cursor2.getColumnIndexOrThrow("REC"));
                        duration_connected = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DURATION_CONNECTED)"));
                        datarx = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DATA_RX)"));
                        datatx = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DATA_TX)"));
                        avgSignal = cursor2.getShort(cursor2.getColumnIndexOrThrow("AVG_SIGNAL"));
                        user_pref_opt_in = cursor2.getInt(cursor2.getColumnIndexOrThrow("SUM(USER_PREF_OPT_IN)"));
                        sql = sql2;
                    } catch (IllegalArgumentException e) {
                        e = e;
                        String str2 = sql2;
                        String[] strArr = args;
                        int i = count;
                        results = results3;
                        Cursor cursor3 = cursor2;
                        LogUtil.e("findUserPrefByFreqLoc IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e2) {
                        e = e2;
                        String str3 = sql2;
                        String[] strArr2 = args;
                        int i2 = count;
                        results = results3;
                        Cursor cursor4 = cursor2;
                        try {
                            LogUtil.e("findUserPrefByFreqLoc Exception: " + e.getMessage());
                        } catch (Throwable th) {
                            th = th;
                            if (cursor2 != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        String str4 = sql2;
                        String[] strArr3 = args;
                        int i3 = count;
                        HashMap<Integer, Bundle> hashMap = results3;
                        Cursor cursor5 = cursor2;
                        if (cursor2 != null) {
                        }
                        throw th;
                    }
                } catch (IllegalArgumentException e3) {
                    e = e3;
                    String str5 = sql2;
                    String[] strArr4 = args;
                    int i4 = count3;
                    results = results3;
                    Cursor cursor6 = cursor2;
                    LogUtil.e("findUserPrefByFreqLoc IllegalArgumentException: " + e.getMessage());
                } catch (Exception e4) {
                    e = e4;
                    String str6 = sql2;
                    String[] strArr5 = args;
                    int i5 = count3;
                    results = results3;
                    Cursor cursor7 = cursor2;
                    LogUtil.e("findUserPrefByFreqLoc Exception: " + e.getMessage());
                } catch (Throwable th3) {
                    th = th3;
                    String str7 = sql2;
                    String[] strArr6 = args;
                    int i6 = count3;
                    HashMap<Integer, Bundle> hashMap2 = results3;
                    Cursor cursor8 = cursor2;
                    if (cursor2 != null) {
                    }
                    throw th;
                }
                try {
                    int user_pref_opt_out = cursor2.getInt(cursor2.getColumnIndexOrThrow("SUM(USER_PREF_OPT_OUT)"));
                    String[] args2 = args;
                    try {
                        user_pref_stay = cursor2.getInt(cursor2.getColumnIndexOrThrow("SUM(USER_PREF_STAY)"));
                        results2 = results3;
                    } catch (IllegalArgumentException e5) {
                        e = e5;
                        int i7 = count;
                        results = results3;
                        Cursor cursor9 = cursor2;
                        LogUtil.e("findUserPrefByFreqLoc IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e6) {
                        e = e6;
                        int i8 = count;
                        results = results3;
                        Cursor cursor10 = cursor2;
                        LogUtil.e("findUserPrefByFreqLoc Exception: " + e.getMessage());
                    } catch (Throwable th4) {
                        th = th4;
                        int i9 = count;
                        HashMap<Integer, Bundle> hashMap3 = results3;
                        Cursor cursor11 = cursor2;
                        if (cursor2 != null) {
                        }
                        throw th;
                    }
                    try {
                        long powerconsumption2 = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(POWER_CONSUMPTION)"));
                        long dubaiScreenoffTx2 = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DUBAI_SCREENOFF_TX)"));
                        long dubaiScreenoffRx2 = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DUBAI_SCREENOFF_RX)"));
                        long dubaiScreenoffPower2 = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DUBAI_SCREENOFF_POWER)"));
                        long dubaiScreenonTx2 = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DUBAI_SCREENON_TX)"));
                        long dubaiScreenonRx2 = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DUBAI_SCREENON_RX)"));
                        long dubaiScreenonPower2 = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DUBAI_SCREENON_POWER)"));
                        long dubaiIdleDuration3 = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DUBAI_IDLE_DURATION)"));
                        long dubaiIdlePower = cursor2.getLong(cursor2.getColumnIndexOrThrow("SUM(DUBAI_IDLE_POWER)"));
                        Bundle statistics = new Bundle();
                        Cursor cursor12 = cursor2;
                        try {
                            statistics.putInt("spaceid", spaceid);
                            statistics.putInt("modelVerAllap", modelVerAllap);
                            statistics.putInt("spaceidmain", spaceidmain);
                            statistics.putInt("modelVerMainap", modelVerMainap);
                            statistics.putInt("nwidcnt", nwidcnt);
                            statistics.putString("networkname", networkname);
                            statistics.putShort("nwfreqcnt", nwfreqcnt);
                            statistics.putInt("nwType", nwType);
                            statistics.putShort("rec", rec);
                            int i10 = modelVerAllap;
                            int i11 = spaceidmain;
                            duration_connected2 = duration_connected;
                            statistics.putLong("duration_connected", duration_connected2);
                            int i12 = modelVerMainap;
                            int i13 = nwidcnt;
                            long datarx2 = datarx;
                            statistics.putLong("datarx", datarx2);
                            long j = datarx2;
                            long datarx3 = datatx;
                            statistics.putLong("datatx", datarx3);
                            statistics.putShort("avgSignal", avgSignal);
                            statistics.putInt("user_pref_opt_in", user_pref_opt_in);
                            statistics.putInt("user_pref_opt_out", user_pref_opt_out);
                            statistics.putInt("user_pref_stay", user_pref_stay);
                            int i14 = spaceid;
                            short s = avgSignal;
                            powerconsumption = powerconsumption2;
                            statistics.putLong("powerconsumption", powerconsumption);
                            long j2 = datarx3;
                            long dubaiScreenoffTx3 = dubaiScreenoffTx2;
                            statistics.putLong("dubaiScreenoffTx", dubaiScreenoffTx3);
                            dubaiScreenoffTx = dubaiScreenoffTx3;
                            long dubaiScreenoffTx4 = dubaiScreenoffRx2;
                            statistics.putLong("dubaiScreenoffRx", dubaiScreenoffTx4);
                            dubaiScreenoffRx = dubaiScreenoffTx4;
                            long dubaiScreenoffPower3 = dubaiScreenoffPower2;
                            statistics.putLong("dubaiScreenoffPower", dubaiScreenoffPower3);
                            dubaiScreenoffPower = dubaiScreenoffPower3;
                            long dubaiScreenonTx3 = dubaiScreenonTx2;
                            statistics.putLong("dubaiScreenonTx", dubaiScreenonTx3);
                            dubaiScreenonTx = dubaiScreenonTx3;
                            long dubaiScreenonTx4 = dubaiScreenonRx2;
                            statistics.putLong("dubaiScreenonRx", dubaiScreenonTx4);
                            dubaiScreenonRx = dubaiScreenonTx4;
                            long dubaiScreenonRx3 = dubaiScreenonPower2;
                            statistics.putLong("dubaiScreenonPower", dubaiScreenonRx3);
                            dubaiScreenonPower = dubaiScreenonRx3;
                            long dubaiIdleDuration4 = dubaiIdleDuration3;
                            statistics.putLong("dubaiIdleDuration", dubaiIdleDuration4);
                            dubaiIdleDuration = dubaiIdleDuration4;
                            dubaiIdleDuration2 = dubaiIdlePower;
                            statistics.putLong("dubaiIdlePower", dubaiIdleDuration2);
                            valueOf = Integer.valueOf(count);
                            count2 = count;
                            results = results2;
                        } catch (IllegalArgumentException e7) {
                            e = e7;
                            int i15 = count;
                            results = results2;
                            cursor2 = cursor12;
                            LogUtil.e("findUserPrefByFreqLoc IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e8) {
                            e = e8;
                            int i16 = count;
                            results = results2;
                            cursor2 = cursor12;
                            LogUtil.e("findUserPrefByFreqLoc Exception: " + e.getMessage());
                        } catch (Throwable th5) {
                            th = th5;
                            int i17 = count;
                            HashMap<Integer, Bundle> hashMap4 = results2;
                            cursor2 = cursor12;
                            if (cursor2 != null) {
                                cursor2.close();
                            }
                            throw th;
                        }
                        try {
                            results.put(valueOf, statistics);
                            StringBuilder sb = new StringBuilder();
                            Bundle bundle = statistics;
                            sb.append(" networkname:");
                            sb.append(networkname);
                            sb.append(",type:");
                            sb.append(nwType);
                            sb.append(",user_pref_opt_in:");
                            sb.append(user_pref_opt_in);
                            sb.append(",user_pref_opt_out:");
                            sb.append(user_pref_opt_out);
                            sb.append(",user_pref_stay:");
                            sb.append(user_pref_stay);
                            sb.append(",duration_connected:");
                            sb.append(duration_connected2);
                            LogUtil.i(sb.toString());
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append(" powerconsumption:");
                            sb2.append(powerconsumption);
                            sb2.append(" dubaiScreenoffTx:");
                            long j3 = powerconsumption;
                            long dubaiScreenoffTx5 = dubaiScreenoffTx;
                            sb2.append(dubaiScreenoffTx5);
                            sb2.append(",dubaiScreenoffRx:");
                            long j4 = dubaiScreenoffTx5;
                            long dubaiScreenoffTx6 = dubaiScreenoffRx;
                            sb2.append(dubaiScreenoffTx6);
                            sb2.append(",dubaiScreenoffPower:");
                            long j5 = dubaiScreenoffTx6;
                            long dubaiScreenoffPower4 = dubaiScreenoffPower;
                            sb2.append(dubaiScreenoffPower4);
                            LogUtil.i(sb2.toString());
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append(" dubaiScreenonTx:");
                            long j6 = dubaiScreenoffPower4;
                            long dubaiScreenonTx5 = dubaiScreenonTx;
                            sb3.append(dubaiScreenonTx5);
                            sb3.append(" dubaiScreenonRx:");
                            long j7 = dubaiScreenonTx5;
                            long dubaiScreenonTx6 = dubaiScreenonRx;
                            sb3.append(dubaiScreenonTx6);
                            sb3.append(",dubaiScreenonPower:");
                            long j8 = dubaiScreenonTx6;
                            long dubaiScreenonRx4 = dubaiScreenonPower;
                            sb3.append(dubaiScreenonRx4);
                            sb3.append(",dubaiIdleDuration:");
                            long j9 = dubaiScreenonRx4;
                            sb3.append(dubaiIdleDuration);
                            sb3.append(",dubaiIdlePower:");
                            sb3.append(dubaiIdleDuration2);
                            LogUtil.i(sb3.toString());
                            results3 = results;
                            sql2 = sql;
                            args = args2;
                            cursor2 = cursor12;
                            count3 = count2;
                            String str8 = freqlocation;
                        } catch (IllegalArgumentException e9) {
                            e = e9;
                            cursor2 = cursor12;
                            LogUtil.e("findUserPrefByFreqLoc IllegalArgumentException: " + e.getMessage());
                        } catch (Exception e10) {
                            e = e10;
                            cursor2 = cursor12;
                            LogUtil.e("findUserPrefByFreqLoc Exception: " + e.getMessage());
                        } catch (Throwable th6) {
                            th = th6;
                            cursor2 = cursor12;
                            if (cursor2 != null) {
                            }
                            throw th;
                        }
                    } catch (IllegalArgumentException e11) {
                        e = e11;
                        int i18 = count;
                        Cursor cursor13 = cursor2;
                        results = results2;
                        LogUtil.e("findUserPrefByFreqLoc IllegalArgumentException: " + e.getMessage());
                    } catch (Exception e12) {
                        e = e12;
                        int i19 = count;
                        Cursor cursor14 = cursor2;
                        results = results2;
                        LogUtil.e("findUserPrefByFreqLoc Exception: " + e.getMessage());
                    } catch (Throwable th7) {
                        th = th7;
                        int i20 = count;
                        Cursor cursor15 = cursor2;
                        HashMap<Integer, Bundle> hashMap5 = results2;
                        if (cursor2 != null) {
                        }
                        throw th;
                    }
                } catch (IllegalArgumentException e13) {
                    e = e13;
                    String[] strArr7 = args;
                    int i21 = count;
                    results = results3;
                    Cursor cursor16 = cursor2;
                    LogUtil.e("findUserPrefByFreqLoc IllegalArgumentException: " + e.getMessage());
                } catch (Exception e14) {
                    e = e14;
                    String[] strArr8 = args;
                    int i22 = count;
                    results = results3;
                    Cursor cursor17 = cursor2;
                    LogUtil.e("findUserPrefByFreqLoc Exception: " + e.getMessage());
                } catch (Throwable th8) {
                    th = th8;
                    String[] strArr9 = args;
                    int i23 = count;
                    HashMap<Integer, Bundle> hashMap6 = results3;
                    Cursor cursor18 = cursor2;
                    if (cursor2 != null) {
                    }
                    throw th;
                }
            }
            String[] strArr10 = args;
            int i24 = count3;
            results = results3;
            Cursor cursor19 = cursor2;
            if (cursor19 != null) {
                cursor = cursor19;
                cursor.close();
            } else {
                cursor = cursor19;
            }
            Cursor cursor20 = cursor;
        } catch (IllegalArgumentException e15) {
            e = e15;
            String str9 = sql2;
            String[] strArr11 = args;
            results = results3;
            LogUtil.e("findUserPrefByFreqLoc IllegalArgumentException: " + e.getMessage());
        } catch (Exception e16) {
            e = e16;
            String str10 = sql2;
            String[] strArr12 = args;
            results = results3;
            LogUtil.e("findUserPrefByFreqLoc Exception: " + e.getMessage());
        } catch (Throwable th9) {
            th = th9;
            String str11 = sql2;
            String[] strArr13 = args;
            HashMap<Integer, Bundle> hashMap7 = results3;
            if (cursor2 != null) {
            }
            throw th;
        }
        return results;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0117, code lost:
        if (r7 == null) goto L_0x011a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x00da, code lost:
        if (r7 != null) goto L_0x00dc;
     */
    public Bundle getUerPrefTotalCountDurationByAllApSpaces(String freqlocation, int spaceid, int modelVerAllAp2) {
        String str = freqlocation;
        LogUtil.i("getUerPrefTotalCountDurationByAllApSpaces:" + "SELECT SUM(DURATION_CONNECTED), SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? GROUP BY SPACEID, MODEL_VER_ALLAP" + ",FreqLoc:" + str);
        String[] args = {this.ScrbId, str, Integer.toString(spaceid), Integer.toString(modelVerAllAp2)};
        int count = 0;
        Bundle results = new Bundle();
        Cursor cursor = null;
        if (this.db == null) {
            return results;
        }
        try {
            cursor = this.db.rawQuery("SELECT SUM(DURATION_CONNECTED), SUM(USER_PREF_OPT_IN), SUM(USER_PREF_OPT_OUT), SUM(USER_PREF_STAY) FROM SPACEUSER_BASE WHERE SCRBID = ? AND FREQLOCNAME = ? AND SPACEID = ? AND MODEL_VER_ALLAP = ? GROUP BY SPACEID, MODEL_VER_ALLAP", args);
            while (cursor.moveToNext()) {
                count++;
                long duration_connected = cursor.getLong(cursor.getColumnIndexOrThrow("SUM(DURATION_CONNECTED)"));
                int user_pref_opt_in = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_OPT_IN)"));
                int user_pref_opt_out = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_OPT_OUT)"));
                int user_pref_stay = cursor.getInt(cursor.getColumnIndexOrThrow("SUM(USER_PREF_STAY)"));
                int totalCount = Math.round(((float) (user_pref_opt_in + user_pref_opt_out)) / 2.0f) + user_pref_stay;
                results.putInt("totalDuration", ((int) duration_connected) / 1000);
                results.putInt("totalCount", totalCount);
                LogUtil.i(" totalDuration:" + (((int) duration_connected) / 1000) + ",totalCount:" + totalCount + ",user_pref_opt_in:" + user_pref_opt_in + ",user_pref_opt_out:" + user_pref_opt_out + ",user_pref_stay:" + user_pref_stay + ",duration_connected:" + duration_connected);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("getUerPrefTotalCountDurationByAllApSpaces IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getUerPrefTotalCountDurationByAllApSpaces Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return results;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }
}
