package com.android.server.wifi.wifipro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class WifiProHistoryDBManager {
    private static long AGING_MS_OF_EACH_DAY = 0;
    private static final int DBG_LOG_LEVEL = 1;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int INFO_LOG_LEVEL = 2;
    private static final short MAX_AP_INFO_RECORD_NUM = (short) 1;
    private static long MS_OF_ONE_DAY = 0;
    private static final String TAG = "WifiProHistoryDBManager";
    private static long TOO_OLD_VALID_DAY;
    private static WifiProHistoryDBManager mBQEDataBaseManager;
    private static int printLogLevel;
    private int mApRecordCount;
    private Object mBqeLock;
    private SQLiteDatabase mDatabase;
    private WifiProHistoryDBHelper mHelper;
    private int mHomeApRecordCount;
    private boolean mNeedDelOldDualBandApInfo;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.WifiProHistoryDBManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.WifiProHistoryDBManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiProHistoryDBManager.<clinit>():void");
    }

    private boolean checkHistoryRecordExist(java.lang.String r10, java.lang.String r11) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0076 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r8 = 0;
        r3 = 0;
        r0 = 0;
        r4 = r9.mDatabase;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5.<init>();	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r6 = "SELECT * FROM ";	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5 = r5.append(r6);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5 = r5.append(r10);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r6 = " where apBSSID like ?";	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5 = r5.append(r6);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5 = r5.toString();	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r6 = 1;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r6 = new java.lang.String[r6];	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r7 = 0;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r6[r7] = r11;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r0 = r4.rawQuery(r5, r6);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r2 = r0.getCount();	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        if (r2 <= 0) goto L_0x0031;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
    L_0x0030:
        r3 = 1;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
    L_0x0031:
        r4 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4.<init>();	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5 = "checkHistoryRecordExist read from:";	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4 = r4.append(r10);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5 = ", get record: ";	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4 = r4.append(r2);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4 = r4.toString();	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r9.logd(r4);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        if (r0 == 0) goto L_0x0058;
    L_0x0055:
        r0.close();
    L_0x0058:
        return r3;
    L_0x0059:
        r1 = move-exception;
        r4 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4.<init>();	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r5 = "checkHistoryRecordExist error:";	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4 = r4.append(r1);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r4 = r4.toString();	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        r9.loge(r4);	 Catch:{ SQLException -> 0x0059, all -> 0x0077 }
        if (r0 == 0) goto L_0x0076;
    L_0x0073:
        r0.close();
    L_0x0076:
        return r8;
    L_0x0077:
        r4 = move-exception;
        if (r0 == 0) goto L_0x007d;
    L_0x007a:
        r0.close();
    L_0x007d:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiProHistoryDBManager.checkHistoryRecordExist(java.lang.String, java.lang.String):boolean");
    }

    private boolean checkRelateApRcdExist(java.lang.String r10, java.lang.String r11) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0056 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r8 = 0;
        r3 = 0;
        r0 = 0;
        r4 = r9.mDatabase;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r5 = "SELECT * FROM WifiProRelateApTable where (apBSSID like ?) and (RelatedBSSID like ?)";	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r6 = 2;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r6 = new java.lang.String[r6];	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r7 = 0;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r6[r7] = r10;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r7 = 1;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r6[r7] = r11;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r0 = r4.rawQuery(r5, r6);	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r2 = r0.getCount();	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        if (r2 <= 0) goto L_0x001c;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
    L_0x001b:
        r3 = 1;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
    L_0x001c:
        r4 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r4.<init>();	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r5 = "checkRelateApRcdExist get record: ";	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r4 = r4.append(r2);	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r4 = r4.toString();	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r9.logd(r4);	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        if (r0 == 0) goto L_0x0038;
    L_0x0035:
        r0.close();
    L_0x0038:
        return r3;
    L_0x0039:
        r1 = move-exception;
        r4 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r4.<init>();	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r5 = "checkRelateApRcdExist error:";	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r4 = r4.append(r1);	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r4 = r4.toString();	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        r9.loge(r4);	 Catch:{ SQLException -> 0x0039, all -> 0x0057 }
        if (r0 == 0) goto L_0x0056;
    L_0x0053:
        r0.close();
    L_0x0056:
        return r8;
    L_0x0057:
        r4 = move-exception;
        if (r0 == 0) goto L_0x005d;
    L_0x005a:
        r0.close();
    L_0x005d:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiProHistoryDBManager.checkRelateApRcdExist(java.lang.String, java.lang.String):boolean");
    }

    public WifiProHistoryDBManager(Context context) {
        this.mBqeLock = new Object();
        this.mApRecordCount = 0;
        this.mHomeApRecordCount = 0;
        Log.w(TAG, "WifiProHistoryDBManager()");
        this.mHelper = new WifiProHistoryDBHelper(context);
        this.mDatabase = this.mHelper.getWritableDatabase();
    }

    public static WifiProHistoryDBManager getInstance(Context context) {
        if (mBQEDataBaseManager == null) {
            mBQEDataBaseManager = new WifiProHistoryDBManager(context);
        }
        return mBQEDataBaseManager;
    }

    public void closeDB() {
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                return;
            }
            Log.w(TAG, "closeDB()");
            this.mDatabase.close();
        }
    }

    private boolean deleteHistoryRecord(String dbTableName, String apBssid) {
        logi("deleteHistoryRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("deleteHistoryRecord database error.");
                return false;
            } else if (apBssid == null) {
                loge("deleteHistoryRecord null error.");
                return false;
            } else {
                try {
                    String[] strArr = new String[DBG_LOG_LEVEL];
                    strArr[0] = apBssid;
                    this.mDatabase.delete(dbTableName, "apBSSID like ?", strArr);
                    return true;
                } catch (SQLException e) {
                    loge("deleteHistoryRecord error:" + e);
                    return false;
                }
            }
        }
    }

    public boolean deleteApInfoRecord(String apBssid) {
        logd("deleteApInfoRecord delete record of bssid:" + apBssid);
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, apBssid);
    }

    private boolean updateApInfoRecord(WifiProApInfoRecord dbr) {
        ContentValues values = new ContentValues();
        values.put("apSSID", dbr.apSSID);
        values.put("apSecurityType", Integer.valueOf(dbr.apSecurityType));
        values.put("firstConnectTime", Long.valueOf(dbr.firstConnectTime));
        values.put("lastConnectTime", Long.valueOf(dbr.lastConnectTime));
        values.put("lanDataSize", Integer.valueOf(dbr.lanDataSize));
        values.put("highSpdFreq", Integer.valueOf(dbr.highSpdFreq));
        values.put("totalUseTime", Integer.valueOf(dbr.totalUseTime));
        values.put("totalUseTimeAtNight", Integer.valueOf(dbr.totalUseTimeAtNight));
        values.put("totalUseTimeAtWeekend", Integer.valueOf(dbr.totalUseTimeAtWeekend));
        values.put("judgeHomeAPTime", Long.valueOf(dbr.judgeHomeAPTime));
        try {
            String[] strArr = new String[DBG_LOG_LEVEL];
            strArr[0] = dbr.apBSSID;
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, values, "apBSSID like ?", strArr);
            if (rowChg == 0) {
                loge("updateApInfoRecord update failed.");
                return false;
            }
            logd("updateApInfoRecord update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateApInfoRecord error:" + e);
            return false;
        }
    }

    private boolean insertApInfoRecord(WifiProApInfoRecord dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProApInfoRecodTable VALUES(null,  ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,?)", new Object[]{dbr.apBSSID, dbr.apSSID, Integer.valueOf(dbr.apSecurityType), Long.valueOf(dbr.firstConnectTime), Long.valueOf(dbr.lastConnectTime), Integer.valueOf(dbr.lanDataSize), Integer.valueOf(dbr.highSpdFreq), Integer.valueOf(dbr.totalUseTime), Integer.valueOf(dbr.totalUseTimeAtNight), Integer.valueOf(dbr.totalUseTimeAtWeekend), Long.valueOf(dbr.judgeHomeAPTime)});
            logi("insertApInfoRecord add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertApInfoRecord error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateApInfoRecord(WifiProApInfoRecord dbr) {
        logd("addOrUpdateApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || dbr == null) {
                loge("addOrUpdateApInfoRecord error.");
                return false;
            } else if (dbr.apBSSID == null) {
                loge("addOrUpdateApInfoRecord null error.");
                return false;
            } else if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, dbr.apBSSID)) {
                r1 = updateApInfoRecord(dbr);
                return r1;
            } else {
                r1 = insertApInfoRecord(dbr);
                return r1;
            }
        }
    }

    public boolean queryApInfoRecord(String apBssid, WifiProApInfoRecord dbr) {
        int recCnt = 0;
        logd("queryApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryApInfoRecord database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryApInfoRecord null error.");
                return false;
            } else {
                Cursor cursor = null;
                try {
                    String[] strArr = new String[DBG_LOG_LEVEL];
                    strArr[0] = apBssid;
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable where apBSSID like ?", strArr);
                    while (cursor.moveToNext()) {
                        recCnt += DBG_LOG_LEVEL;
                        if (recCnt > DBG_LOG_LEVEL) {
                            break;
                        }
                        logd("read record id = " + cursor.getInt(cursor.getColumnIndex("_id")));
                        if (recCnt == DBG_LOG_LEVEL) {
                            dbr.apBSSID = apBssid;
                            dbr.apSSID = cursor.getString(cursor.getColumnIndex("apSSID"));
                            dbr.apSecurityType = cursor.getInt(cursor.getColumnIndex("apSecurityType"));
                            dbr.firstConnectTime = cursor.getLong(cursor.getColumnIndex("firstConnectTime"));
                            dbr.lastConnectTime = cursor.getLong(cursor.getColumnIndex("lastConnectTime"));
                            dbr.lanDataSize = cursor.getInt(cursor.getColumnIndex("lanDataSize"));
                            dbr.highSpdFreq = cursor.getInt(cursor.getColumnIndex("highSpdFreq"));
                            dbr.totalUseTime = cursor.getInt(cursor.getColumnIndex("totalUseTime"));
                            dbr.totalUseTimeAtNight = cursor.getInt(cursor.getColumnIndex("totalUseTimeAtNight"));
                            dbr.totalUseTimeAtWeekend = cursor.getInt(cursor.getColumnIndex("totalUseTimeAtWeekend"));
                            dbr.judgeHomeAPTime = cursor.getLong(cursor.getColumnIndex("judgeHomeAPTime"));
                            logi("read record succ, LastConnectTime:" + dbr.lastConnectTime);
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (recCnt > DBG_LOG_LEVEL) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryApInfoRecord not record.");
                    }
                    return true;
                } catch (SQLException e) {
                    loge("queryApInfoRecord error:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public int querySameSSIDApCount(String apBSSID, String apSsid, int secType) {
        int recCnt = 0;
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("querySameSSIDApCount database error.");
                return 0;
            } else if (apBSSID == null || apSsid == null) {
                loge("querySameSSIDApCount null error.");
                return 0;
            } else {
                Cursor cursor = null;
                try {
                    String[] strArr = new String[ERROR_LOG_LEVEL];
                    strArr[0] = apSsid;
                    strArr[DBG_LOG_LEVEL] = String.valueOf(secType);
                    strArr[INFO_LOG_LEVEL] = apBSSID;
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable where (apSSID like ?) and (apSecurityType = ?) and (apBSSID != ?)", strArr);
                    recCnt = cursor.getCount();
                    logi("querySameSSIDApCount read same (SSID:" + apSsid + ", secType:" + secType + ") and different BSSID record count=" + recCnt);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return recCnt;
                } catch (SQLException e) {
                    loge("querySameSSIDApCount error:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return recCnt;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public boolean removeTooOldApInfoRecord() {
        Vector<String> delRecordsVentor = new Vector();
        long currDateMsTime = new Date().getTime();
        logd("removeTooOldApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase != null) {
                if (this.mDatabase.isOpen()) {
                    delRecordsVentor.clear();
                    Cursor cursor = null;
                    try {
                        cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable", null);
                        logd("all record count=" + cursor.getCount());
                        while (cursor.moveToNext()) {
                            long lastConnDateMsTime = cursor.getLong(cursor.getColumnIndex("lastConnectTime"));
                            long totalConnectTime = (long) cursor.getInt(cursor.getColumnIndex("totalUseTime"));
                            long pastDays = (currDateMsTime - lastConnDateMsTime) / MS_OF_ONE_DAY;
                            if (pastDays > TOO_OLD_VALID_DAY && totalConnectTime - (AGING_MS_OF_EACH_DAY * pastDays) < 0) {
                                logi("check result: need delete.");
                                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                                String ssid = cursor.getString(cursor.getColumnIndex("apSSID"));
                                String bssid = cursor.getString(cursor.getColumnIndex("apBSSID"));
                                logi("check record: ssid:" + ssid + ", bssid:" + bssid + ", id:" + id + ", pass time:" + (currDateMsTime - lastConnDateMsTime));
                                delRecordsVentor.add(bssid);
                            }
                        }
                        int delSize = delRecordsVentor.size();
                        logi("start delete " + delSize + " records.");
                        for (int i = 0; i < delSize; i += DBG_LOG_LEVEL) {
                            String delBSSID = (String) delRecordsVentor.get(i);
                            if (delBSSID == null) {
                                break;
                            }
                            SQLiteDatabase sQLiteDatabase = this.mDatabase;
                            String str = WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME;
                            String[] strArr = new String[DBG_LOG_LEVEL];
                            strArr[0] = delBSSID;
                            sQLiteDatabase.delete(str, "apBSSID like ?", strArr);
                            logi("delete bssid:" + delBSSID + " success.");
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        return true;
                    } catch (SQLException e) {
                        loge("removeTooOldApInfoRecord error:" + e);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
            loge("removeTooOldApInfoRecord database error.");
            return false;
        }
    }

    public boolean statisticApInfoRecord() {
        logd("statisticApInfoRecord enter.");
        synchronized (this.mBqeLock) {
            Cursor cursor;
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("statisticApInfoRecord database error.");
                return false;
            }
            this.mHomeApRecordCount = 0;
            this.mApRecordCount = 0;
            cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProApInfoRecodTable", null);
                this.mApRecordCount = cursor.getCount();
                logi("all record count=" + this.mApRecordCount);
                while (cursor.moveToNext()) {
                    if (cursor.getLong(cursor.getColumnIndex("judgeHomeAPTime")) > 0) {
                        String ssid = cursor.getString(cursor.getColumnIndex("apSSID"));
                        this.mHomeApRecordCount += DBG_LOG_LEVEL;
                        logi("check record: Home ap ssid:" + ssid + ", total:" + this.mHomeApRecordCount);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            } catch (SQLException e) {
                loge("removeTooOldApInfoRecord error:" + e);
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public int getTotRecordCount() {
        int i;
        synchronized (this.mBqeLock) {
            i = this.mApRecordCount;
        }
        return i;
    }

    public int getHomeApRecordCount() {
        int i;
        synchronized (this.mBqeLock) {
            i = this.mHomeApRecordCount;
        }
        return i;
    }

    private boolean insertEnterpriseApRecord(String apSSID, int secType) {
        try {
            Object[] objArr = new Object[INFO_LOG_LEVEL];
            objArr[0] = apSSID;
            objArr[DBG_LOG_LEVEL] = Integer.valueOf(secType);
            this.mDatabase.execSQL("INSERT INTO WifiProEnterpriseAPTable VALUES(null,  ?, ?)", objArr);
            logi("insertEnterpriseApRecord add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertEnterpriseApRecord error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateEnterpriseApRecord(String apSSID, int secType) {
        logd("addOrUpdateEnterpriseApRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || apSSID == null) {
                loge("addOrUpdateEnterpriseApRecord error.");
                return false;
            } else if (queryEnterpriseApRecord(apSSID, secType)) {
                logi("already exist the record ssid:" + apSSID);
                return true;
            } else {
                logi("add record here ssid:" + apSSID);
                boolean insertEnterpriseApRecord = insertEnterpriseApRecord(apSSID, secType);
                return insertEnterpriseApRecord;
            }
        }
    }

    public boolean queryEnterpriseApRecord(String apSSID, int secType) {
        logd("queryEnterpriseApRecord enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryEnterpriseApRecord database error.");
                return false;
            } else if (apSSID == null) {
                loge("queryEnterpriseApRecord null error.");
                return false;
            } else {
                Cursor cursor = null;
                try {
                    String[] strArr = new String[INFO_LOG_LEVEL];
                    strArr[0] = apSSID;
                    strArr[DBG_LOG_LEVEL] = String.valueOf(secType);
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProEnterpriseAPTable where (apSSID like ?) and (apSecurityType = ?)", strArr);
                    int recCnt = cursor.getCount();
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (recCnt > 0) {
                        logi("SSID:" + apSSID + ", security: " + secType + " is in Enterprise Ap table. count:" + recCnt);
                        return true;
                    }
                    logi("SSID:" + apSSID + ", security: " + secType + " is not in Enterprise Ap table. count:" + recCnt);
                    return false;
                } catch (SQLException e) {
                    loge("queryEnterpriseApRecord error:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public boolean deleteEnterpriseApRecord(String tableName, String ssid, int secType) {
        if (tableName == null || ssid == null) {
            loge("deleteHistoryRecord null error.");
            return false;
        }
        logi("delete record of same (SSID:" + ssid + ", secType:" + secType + ") from " + tableName);
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("deleteHistoryRecord database error.");
                return false;
            }
            try {
                String[] strArr = new String[INFO_LOG_LEVEL];
                strArr[0] = ssid;
                strArr[DBG_LOG_LEVEL] = String.valueOf(secType);
                logd("delete record count=" + this.mDatabase.delete(tableName, "(apSSID like ?) and (apSecurityType = ?)", strArr));
                return true;
            } catch (SQLException e) {
                loge("deleteHistoryRecord error:" + e);
                return false;
            }
        }
    }

    public boolean deleteRelateApRcd(String apBssid) {
        logi("deleteRelateApRcd remove all record of bssid:" + apBssid);
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, apBssid);
    }

    public boolean deleteApQualityRcd(String apBssid) {
        logi("deleteApQualityRcd remove all record of bssid:" + apBssid);
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, apBssid);
    }

    public boolean deleteDualBandApInfoRcd(String apBssid) {
        logi("deleteDualBandApInfoRcd remove all record of bssid:" + apBssid);
        return deleteHistoryRecord(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, apBssid);
    }

    public boolean deleteRelateApRcd(String apBssid, String relatedBSSID) {
        logi("deleteRelateApRcd enter: apBssid=" + apBssid + ", relatedBSSID=" + relatedBSSID);
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("deleteRelateApRcd database error.");
                return false;
            } else if (apBssid == null || relatedBSSID == null) {
                loge("deleteRelateApRcd null error.");
                return false;
            } else {
                try {
                    String[] strArr = new String[INFO_LOG_LEVEL];
                    strArr[0] = apBssid;
                    strArr[DBG_LOG_LEVEL] = relatedBSSID;
                    this.mDatabase.delete(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, "(apBSSID like ?) and (RelatedBSSID like ?)", strArr);
                    return true;
                } catch (SQLException e) {
                    loge("deleteRelateApRcd error:" + e);
                    return false;
                }
            }
        }
    }

    public boolean deleteRelate5GAPRcd(String relatedBSSID) {
        logi("deleteRelateApRcd enter: relatedBSSID=" + relatedBSSID);
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("deleteRelateApRcd database error.");
                return false;
            } else if (relatedBSSID == null) {
                loge("deleteRelateApRcd null error.");
                return false;
            } else {
                try {
                    String[] strArr = new String[DBG_LOG_LEVEL];
                    strArr[0] = relatedBSSID;
                    this.mDatabase.delete(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, "(RelatedBSSID like ?)", strArr);
                    return true;
                } catch (SQLException e) {
                    loge("deleteRelateApRcd error:" + e);
                    return false;
                }
            }
        }
    }

    private boolean deleteAllDualBandAPRcd(String apBssid) {
        if (deleteDualBandApInfoRcd(apBssid) && deleteApQualityRcd(apBssid) && deleteRelateApRcd(apBssid)) {
            return deleteRelate5GAPRcd(apBssid);
        }
        return false;
    }

    private boolean updateApQualityRcd(WifiProApQualityRcd dbr) {
        ContentValues values = new ContentValues();
        values.put("RTT_Product", dbr.mRTT_Product);
        values.put("RTT_PacketVolume", dbr.mRTT_PacketVolume);
        values.put("HistoryAvgRtt", dbr.mHistoryAvgRtt);
        values.put("OTA_LostRateValue", dbr.mOTA_LostRateValue);
        values.put("OTA_PktVolume", dbr.mOTA_PktVolume);
        values.put("OTA_BadPktProduct", dbr.mOTA_BadPktProduct);
        try {
            String[] strArr = new String[DBG_LOG_LEVEL];
            strArr[0] = dbr.apBSSID;
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, values, "apBSSID like ?", strArr);
            if (rowChg == 0) {
                loge("updateApQualityRcd update failed.");
                return false;
            }
            logd("updateApQualityRcd update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateApQualityRcd error:" + e);
            return false;
        }
    }

    private boolean insertApQualityRcd(WifiProApQualityRcd dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProApQualityTable VALUES(null,  ?, ?, ?, ?, ?,   ?, ?)", new Object[]{dbr.apBSSID, dbr.mRTT_Product, dbr.mRTT_PacketVolume, dbr.mHistoryAvgRtt, dbr.mOTA_LostRateValue, dbr.mOTA_PktVolume, dbr.mOTA_BadPktProduct});
            logi("insertApQualityRcd add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertApQualityRcd error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateApQualityRcd(WifiProApQualityRcd dbr) {
        logd("addOrUpdateApQualityRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || dbr == null) {
                loge("addOrUpdateApQualityRcd error.");
                return false;
            } else if (dbr.apBSSID == null) {
                loge("addOrUpdateApQualityRcd null error.");
                return false;
            } else if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_QUALITY_TB_NAME, dbr.apBSSID)) {
                r0 = updateApQualityRcd(dbr);
                return r0;
            } else {
                r0 = insertApQualityRcd(dbr);
                return r0;
            }
        }
    }

    public boolean queryApQualityRcd(String apBssid, WifiProApQualityRcd dbr) {
        int recCnt = 0;
        logd("queryApQualityRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryApQualityRcd database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryApQualityRcd null error.");
                return false;
            } else {
                Cursor cursor = null;
                try {
                    String[] strArr = new String[DBG_LOG_LEVEL];
                    strArr[0] = apBssid;
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProApQualityTable where apBSSID like ?", strArr);
                    while (cursor.moveToNext()) {
                        recCnt += DBG_LOG_LEVEL;
                        if (recCnt > DBG_LOG_LEVEL) {
                            break;
                        } else if (recCnt == DBG_LOG_LEVEL) {
                            dbr.apBSSID = apBssid;
                            dbr.mRTT_Product = cursor.getBlob(cursor.getColumnIndex("RTT_Product"));
                            dbr.mRTT_PacketVolume = cursor.getBlob(cursor.getColumnIndex("RTT_PacketVolume"));
                            dbr.mHistoryAvgRtt = cursor.getBlob(cursor.getColumnIndex("HistoryAvgRtt"));
                            dbr.mOTA_LostRateValue = cursor.getBlob(cursor.getColumnIndex("OTA_LostRateValue"));
                            dbr.mOTA_PktVolume = cursor.getBlob(cursor.getColumnIndex("OTA_PktVolume"));
                            dbr.mOTA_BadPktProduct = cursor.getBlob(cursor.getColumnIndex("OTA_BadPktProduct"));
                            logi("read record succ");
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (recCnt > DBG_LOG_LEVEL) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryApQualityRcd not record.");
                        return false;
                    }
                    return true;
                } catch (SQLException e) {
                    loge("queryApQualityRcd error:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    private boolean updateRelateApRcd(WifiProRelateApRcd dbr) {
        ContentValues values = new ContentValues();
        values.put("RelateType", Integer.valueOf(dbr.mRelateType));
        values.put("MaxCurrentRSSI", Integer.valueOf(dbr.mMaxCurrentRSSI));
        values.put("MaxRelatedRSSI", Integer.valueOf(dbr.mMaxRelatedRSSI));
        values.put("MinCurrentRSSI", Integer.valueOf(dbr.mMinCurrentRSSI));
        values.put("MinRelatedRSSI", Integer.valueOf(dbr.mMinRelatedRSSI));
        try {
            String[] strArr = new String[INFO_LOG_LEVEL];
            strArr[0] = dbr.apBSSID;
            strArr[DBG_LOG_LEVEL] = dbr.mRelatedBSSID;
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_RELATE_AP_TB_NAME, values, "(apBSSID like ?) and (RelatedBSSID like ?)", strArr);
            if (rowChg == 0) {
                loge("updateRelateApRcd update failed.");
                return false;
            }
            logd("updateRelateApRcd update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateRelateApRcd error:" + e);
            return false;
        }
    }

    private boolean insertRelateApRcd(WifiProRelateApRcd dbr) {
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProRelateApTable VALUES(null,  ?, ?, ?, ?, ?, ?, ?)", new Object[]{dbr.apBSSID, dbr.mRelatedBSSID, Integer.valueOf(dbr.mRelateType), Integer.valueOf(dbr.mMaxCurrentRSSI), Integer.valueOf(dbr.mMaxRelatedRSSI), Integer.valueOf(dbr.mMinCurrentRSSI), Integer.valueOf(dbr.mMinRelatedRSSI)});
            logi("insertRelateApRcd add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertRelateApRcd error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateRelateApRcd(WifiProRelateApRcd dbr) {
        logd("addOrUpdateRelateApRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || dbr == null) {
                loge("addOrUpdateRelateApRcd error.");
                return false;
            } else if (dbr.apBSSID == null || dbr.mRelatedBSSID == null) {
                loge("addOrUpdateRelateApRcd null error.");
                return false;
            } else if (checkRelateApRcdExist(dbr.apBSSID, dbr.mRelatedBSSID)) {
                r0 = updateRelateApRcd(dbr);
                return r0;
            } else {
                r0 = insertRelateApRcd(dbr);
                return r0;
            }
        }
    }

    public boolean queryRelateApRcd(String apBssid, List<WifiProRelateApRcd> relateApList) {
        int recCnt = 0;
        logd("queryRelateApRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryRelateApRcd database error.");
                return false;
            } else if (apBssid == null || relateApList == null) {
                loge("queryRelateApRcd null error.");
                return false;
            } else {
                relateApList.clear();
                Cursor cursor = null;
                try {
                    String[] strArr = new String[DBG_LOG_LEVEL];
                    strArr[0] = apBssid;
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProRelateApTable where apBSSID like ?", strArr);
                    while (cursor.moveToNext()) {
                        recCnt += DBG_LOG_LEVEL;
                        if (recCnt > 20) {
                            break;
                        }
                        WifiProRelateApRcd dbr = new WifiProRelateApRcd(apBssid);
                        dbr.mRelatedBSSID = cursor.getString(cursor.getColumnIndex("RelatedBSSID"));
                        dbr.mRelateType = cursor.getShort(cursor.getColumnIndex("RelateType"));
                        dbr.mMaxCurrentRSSI = cursor.getInt(cursor.getColumnIndex("MaxCurrentRSSI"));
                        dbr.mMaxRelatedRSSI = cursor.getInt(cursor.getColumnIndex("MaxRelatedRSSI"));
                        dbr.mMinCurrentRSSI = cursor.getInt(cursor.getColumnIndex("MinCurrentRSSI"));
                        dbr.mMinRelatedRSSI = cursor.getInt(cursor.getColumnIndex("MinRelatedRSSI"));
                        logi("read record succ get relate bssid: " + dbr.mRelatedBSSID);
                        relateApList.add(dbr);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (recCnt == 0) {
                        logi("queryRelateApRcd not record.");
                        return false;
                    }
                    return true;
                } catch (SQLException e) {
                    loge("queryRelateApRcd error:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    private boolean updateDualBandApInfoRcd(WifiProDualBandApInfoRcd dbr) {
        ContentValues values = new ContentValues();
        values.put("apSSID", dbr.mApSSID);
        values.put("InetCapability", dbr.mInetCapability);
        values.put("ServingBand", dbr.mServingBand);
        values.put("ApAuthType", dbr.mApAuthType);
        values.put("ChannelFrequency", Integer.valueOf(dbr.mChannelFrequency));
        values.put("DisappearCount", Integer.valueOf(dbr.mDisappearCount));
        values.put("isInBlackList", Integer.valueOf(dbr.isInBlackList));
        values.put("UpdateTime", Long.valueOf(dbr.mUpdateTime));
        try {
            String[] strArr = new String[DBG_LOG_LEVEL];
            strArr[0] = dbr.apBSSID;
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, values, "apBSSID like ?", strArr);
            if (rowChg == 0) {
                loge("updateDualBandApInfoRcd update failed.");
                return false;
            }
            logd("updateDualBandApInfoRcd update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateDualBandApInfoRcd error:" + e);
            return false;
        }
    }

    private boolean insertDualBandApInfoRcd(WifiProDualBandApInfoRcd dbr) {
        if (this.mNeedDelOldDualBandApInfo || getDualBandApInfoSize() >= MessageUtil.DB_BSSID_MAX_QUANTA) {
            if (!this.mNeedDelOldDualBandApInfo) {
                this.mNeedDelOldDualBandApInfo = true;
            }
            if (!deleteOldestDualBandApInfo()) {
                return false;
            }
        }
        try {
            this.mDatabase.execSQL("INSERT INTO WifiProDualBandApInfoRcdTable VALUES(null,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{dbr.apBSSID, dbr.mApSSID, dbr.mInetCapability, dbr.mServingBand, dbr.mApAuthType, Integer.valueOf(dbr.mChannelFrequency), Integer.valueOf(dbr.mDisappearCount), Integer.valueOf(dbr.isInBlackList), Long.valueOf(dbr.mUpdateTime)});
            logi("insertDualBandApInfoRcd add a record succ.");
            return true;
        } catch (SQLException e) {
            loge("insertDualBandApInfoRcd error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateDualBandApInfoRcd(WifiProDualBandApInfoRcd dbr) {
        logd("addOrUpdateDualBandApInfoRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || dbr == null) {
                loge("addOrUpdateDualBandApInfoRcd error.");
                return false;
            } else if (dbr.apBSSID == null) {
                loge("addOrUpdateDualBandApInfoRcd null error.");
                return false;
            } else {
                dbr.mUpdateTime = System.currentTimeMillis();
                boolean updateDualBandApInfoRcd;
                if (checkHistoryRecordExist(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, dbr.apBSSID)) {
                    updateDualBandApInfoRcd = updateDualBandApInfoRcd(dbr);
                    return updateDualBandApInfoRcd;
                }
                updateDualBandApInfoRcd = insertDualBandApInfoRcd(dbr);
                return updateDualBandApInfoRcd;
            }
        }
    }

    public boolean queryDualBandApInfoRcd(String apBssid, WifiProDualBandApInfoRcd dbr) {
        int recCnt = 0;
        logd("queryDualBandApInfoRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryDualBandApInfoRcd database error.");
                return false;
            } else if (apBssid == null || dbr == null) {
                loge("queryDualBandApInfoRcd null error.");
                return false;
            } else {
                Cursor cursor = null;
                try {
                    String[] strArr = new String[DBG_LOG_LEVEL];
                    strArr[0] = apBssid;
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apBSSID like ?", strArr);
                    while (cursor.moveToNext()) {
                        recCnt += DBG_LOG_LEVEL;
                        if (recCnt > DBG_LOG_LEVEL) {
                            break;
                        } else if (recCnt == DBG_LOG_LEVEL) {
                            dbr.apBSSID = apBssid;
                            dbr.mApSSID = cursor.getString(cursor.getColumnIndex("apSSID"));
                            dbr.mInetCapability = Short.valueOf(cursor.getShort(cursor.getColumnIndex("InetCapability")));
                            dbr.mServingBand = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ServingBand")));
                            dbr.mApAuthType = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ApAuthType")));
                            dbr.mChannelFrequency = cursor.getInt(cursor.getColumnIndex("ChannelFrequency"));
                            dbr.mDisappearCount = cursor.getShort(cursor.getColumnIndex("DisappearCount"));
                            dbr.isInBlackList = cursor.getShort(cursor.getColumnIndex("isInBlackList"));
                            dbr.mUpdateTime = cursor.getLong(cursor.getColumnIndex("UpdateTime"));
                            logi("read record succ");
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (recCnt > DBG_LOG_LEVEL) {
                        loge("more than one record error. use first record.");
                    } else if (recCnt == 0) {
                        logi("queryDualBandApInfoRcd not record.");
                        return false;
                    }
                    return true;
                } catch (SQLException e) {
                    loge("queryDualBandApInfoRcd error:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public List<WifiProDualBandApInfoRcd> queryDualBandApInfoRcdBySsid(String ssid) {
        List<WifiProDualBandApInfoRcd> mRecList = new ArrayList();
        logd("queryDualBandApInfoRcd enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryDualBandApInfoRcdBySsid database error.");
                return null;
            } else if (ssid == null) {
                loge("queryDualBandApInfoRcdBySsid null error.");
                return null;
            } else {
                Cursor cursor = null;
                try {
                    String[] strArr = new String[DBG_LOG_LEVEL];
                    strArr[0] = ssid;
                    cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apSSID like ?", strArr);
                    while (cursor.moveToNext()) {
                        WifiProDualBandApInfoRcd dbr = new WifiProDualBandApInfoRcd(null);
                        dbr.apBSSID = cursor.getString(cursor.getColumnIndex("apBSSID"));
                        dbr.mApSSID = ssid;
                        dbr.mInetCapability = Short.valueOf(cursor.getShort(cursor.getColumnIndex("InetCapability")));
                        dbr.mServingBand = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ServingBand")));
                        dbr.mApAuthType = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ApAuthType")));
                        dbr.mChannelFrequency = cursor.getInt(cursor.getColumnIndex("ChannelFrequency"));
                        dbr.mDisappearCount = cursor.getShort(cursor.getColumnIndex("DisappearCount"));
                        dbr.isInBlackList = cursor.getShort(cursor.getColumnIndex("isInBlackList"));
                        dbr.mUpdateTime = cursor.getLong(cursor.getColumnIndex("UpdateTime"));
                        logi("read record succ");
                        mRecList.add(dbr);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (mRecList.size() == 0) {
                        logi("queryDualBandApInfoRcdBySsid not record.");
                    }
                    return mRecList;
                } catch (SQLException e) {
                    loge("queryDualBandApInfoRcdBySsid error:" + e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public List<WifiProDualBandApInfoRcd> getAllDualBandApInfo() {
        logd("getAllDualBandApInfo enter.");
        synchronized (this.mBqeLock) {
            Cursor cursor;
            ArrayList<WifiProDualBandApInfoRcd> apInfoList = new ArrayList();
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("queryDualBandApInfoRcd database error.");
                return apInfoList;
            }
            cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable", null);
                while (cursor.moveToNext()) {
                    WifiProDualBandApInfoRcd dbr = new WifiProDualBandApInfoRcd(cursor.getString(cursor.getColumnIndex("apBSSID")));
                    dbr.mApSSID = cursor.getString(cursor.getColumnIndex("apSSID"));
                    dbr.mInetCapability = Short.valueOf(cursor.getShort(cursor.getColumnIndex("InetCapability")));
                    dbr.mServingBand = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ServingBand")));
                    dbr.mApAuthType = Short.valueOf(cursor.getShort(cursor.getColumnIndex("ApAuthType")));
                    dbr.mChannelFrequency = cursor.getInt(cursor.getColumnIndex("ChannelFrequency"));
                    dbr.mDisappearCount = cursor.getShort(cursor.getColumnIndex("DisappearCount"));
                    dbr.isInBlackList = cursor.getShort(cursor.getColumnIndex("isInBlackList"));
                    dbr.mUpdateTime = cursor.getLong(cursor.getColumnIndex("UpdateTime"));
                    apInfoList.add(dbr);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLException e) {
                loge("queryDualBandApInfoRcd error:" + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return apInfoList;
        }
    }

    private boolean updateApRSSIThreshold(String apBSSID, String rssiThreshold) {
        ContentValues values = new ContentValues();
        values.put("RSSIThreshold", rssiThreshold);
        try {
            String[] strArr = new String[DBG_LOG_LEVEL];
            strArr[0] = apBSSID;
            int rowChg = this.mDatabase.update(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, values, "apBSSID like ?", strArr);
            if (rowChg == 0) {
                loge("updateApRSSIThreshold update failed.");
                return false;
            }
            logd("updateApRSSIThreshold update succ, rowChg=" + rowChg);
            return true;
        } catch (SQLException e) {
            loge("updateApRSSIThreshold error:" + e);
            return false;
        }
    }

    public boolean addOrUpdateApRSSIThreshold(String apBSSID, String rssiThreshold) {
        logd("addOrUpdateApRSSIThreshold enter.");
        synchronized (this.mBqeLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || apBSSID == null || rssiThreshold == null) {
                loge("addOrUpdateApRSSIThreshold error.");
                return false;
            }
            if (!checkHistoryRecordExist(WifiProHistoryDBHelper.WP_DUAL_BAND_AP_INFO_TB_NAME, apBSSID)) {
                insertDualBandApInfoRcd(new WifiProDualBandApInfoRcd(apBSSID));
            }
            boolean updateApRSSIThreshold = updateApRSSIThreshold(apBSSID, rssiThreshold);
            return updateApRSSIThreshold;
        }
    }

    public String queryApRSSIThreshold(String apBssid) {
        int recCnt = 0;
        String str = null;
        logd("queryApRSSIThreshold enter.");
        synchronized (this.mBqeLock) {
            Cursor cursor;
            if (this.mDatabase == null || !this.mDatabase.isOpen() || apBssid == null) {
                loge("queryApRSSIThreshold database error.");
                return null;
            }
            cursor = null;
            try {
                String[] strArr = new String[DBG_LOG_LEVEL];
                strArr[0] = apBssid;
                cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable where apBSSID like ?", strArr);
                while (cursor.moveToNext()) {
                    recCnt += DBG_LOG_LEVEL;
                    if (recCnt > DBG_LOG_LEVEL) {
                        break;
                    } else if (recCnt == DBG_LOG_LEVEL) {
                        str = cursor.getString(cursor.getColumnIndex("RSSIThreshold"));
                        logi("read record succ, RSSIThreshold = " + str);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLException e) {
                loge("queryApRSSIThreshold error:" + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return str;
        }
    }

    public int getDualBandApInfoSize() {
        logd("getDualBandApInfoSize enter.");
        synchronized (this.mBqeLock) {
            int result = -1;
            Cursor cursor;
            if (this.mDatabase == null || !this.mDatabase.isOpen()) {
                loge("getDualBandApInfoSize database error.");
                return -1;
            }
            cursor = null;
            try {
                cursor = this.mDatabase.rawQuery("SELECT * FROM WifiProDualBandApInfoRcdTable", null);
                result = cursor.getCount();
                if (cursor != null) {
                    cursor.close();
                }
                logd("getDualBandApInfoSize: " + result);
                return result;
            } catch (SQLException e) {
                loge("getDualBandApInfoSize error:" + e);
                if (cursor != null) {
                    cursor.close();
                }
                logd("getDualBandApInfoSize: " + result);
                return -1;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                logd("getDualBandApInfoSize: " + result);
                return -1;
            }
        }
    }

    private boolean deleteOldestDualBandApInfo() {
        List<WifiProDualBandApInfoRcd> allApInfos = getAllDualBandApInfo();
        if (allApInfos.size() <= 0) {
            return false;
        }
        WifiProDualBandApInfoRcd oldestApInfo = (WifiProDualBandApInfoRcd) allApInfos.get(0);
        for (WifiProDualBandApInfoRcd apInfo : allApInfos) {
            if (apInfo.mUpdateTime < oldestApInfo.mUpdateTime) {
                oldestApInfo = apInfo;
            }
        }
        return deleteAllDualBandAPRcd(oldestApInfo.apBSSID);
    }

    private void logd(String msg) {
        if (printLogLevel <= DBG_LOG_LEVEL) {
            Log.d(TAG, msg);
        }
    }

    private void logi(String msg) {
        if (printLogLevel <= INFO_LOG_LEVEL) {
            Log.i(TAG, msg);
        }
    }

    private void loge(String msg) {
        if (printLogLevel <= ERROR_LOG_LEVEL) {
            Log.e(TAG, msg);
        }
    }
}
