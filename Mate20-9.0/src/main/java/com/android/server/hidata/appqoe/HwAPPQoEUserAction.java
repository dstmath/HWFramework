package com.android.server.hidata.appqoe;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.lcagent.client.LogCollectManager;

public class HwAPPQoEUserAction {
    public static final String CHIPSET_TYPE_PROP = "ro.connectivity.sub_chiptype";
    private static final int INITIAL_USER_TYPE_CNT = 0;
    private static final String TAG = "HiData_HwAPPQoEUserAction";
    private static final int USER_TYPE_CNT_THRESHOLD = 3;
    private static final int WIFI_BAD_SIGNAL_THRESHOLD = 1;
    private static final int WIFI_DISABLE_TIME_ADVANCED = 1000;
    private static final int WIFI_DISABLE_TIME_DELTA = 10000;
    private static HwAPPQoEUserAction mUserAction = null;
    /* access modifiers changed from: private */
    public WifiStateChangeInfo curWifiStateInfo = new WifiStateChangeInfo();
    /* access modifiers changed from: private */
    public WifiSwitchChangeInfo currWifiDisableInfo = new WifiSwitchChangeInfo(-1);
    private IntentFilter intentFilter = new IntentFilter();
    /* access modifiers changed from: private */
    public boolean isWifiConnected = false;
    /* access modifiers changed from: private */
    public int lastWifiSwitchState = 4;
    private int mAppScenceId = -1;
    private BroadcastReceiver mBroadcastReceiver = new WifiBroadcastReceiver();
    private HwAPPChrExcpReport mChrExcpReport = null;
    private LogCollectManager mCollectManger = null;
    /* access modifiers changed from: private */
    public Context mContext;
    private HwAPPQoEDataBase mDataManger = null;
    private SQLiteDatabase mDatabase = null;
    private boolean mIsDefaultRadicalUser = false;
    private Object mSqlLock = new Object();
    /* access modifiers changed from: private */
    public int recentWifiRssi = -127;

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        private WifiBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                int wifistatus = intent.getIntExtra("wifi_state", 4);
                if (wifistatus == 1) {
                    if (1 != HwAPPQoEUserAction.this.lastWifiSwitchState) {
                        HwAPPQoEUtils.logD(HwAPPQoEUserAction.TAG, "WIFI_STATE_DISABLED:" + wifistatus);
                        WifiSwitchChangeInfo unused = HwAPPQoEUserAction.this.currWifiDisableInfo = new WifiSwitchChangeInfo(wifistatus);
                        HwAPPQoEUserAction.this.currWifiDisableInfo.wifiState.wifiDisconnTime = HwAPPQoEUserAction.this.curWifiStateInfo.wifiDisconnTime;
                        HwAPPQoEUserAction.this.currWifiDisableInfo.wifiState.wifiSSID = HwAPPQoEUserAction.this.curWifiStateInfo.wifiSSID;
                        HwAPPQoEUserAction.this.currWifiDisableInfo.wifiState.wifiRssi = HwAPPQoEUserAction.this.curWifiStateInfo.wifiRssi;
                        HwAPPQoEUserAction.this.currWifiDisableInfo.wifiState.wifiFreq = HwAPPQoEUserAction.this.curWifiStateInfo.wifiFreq;
                        HwAPPQoEUserAction.this.reportExceptionInfo();
                    }
                    int unused2 = HwAPPQoEUserAction.this.lastWifiSwitchState = wifistatus;
                } else if (wifistatus == 3) {
                    HwAPPQoEUtils.logD(HwAPPQoEUserAction.TAG, "WIFI_STATE_ENABLED:" + wifistatus);
                    int unused3 = HwAPPQoEUserAction.this.lastWifiSwitchState = wifistatus;
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo == null) {
                    HwAPPQoEUtils.logD(HwAPPQoEUserAction.TAG, "NETWORK_STATE_CHANGED_ACTION, netInfo is null.");
                    return;
                }
                HwAPPQoEUtils.logD(HwAPPQoEUserAction.TAG, "NETWORK_STATE_CHANGED_ACTION:" + netInfo.getState());
                if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    WifiManager mWifiManager = (WifiManager) HwAPPQoEUserAction.this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
                    if (mWifiManager != null) {
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        if (wifiInfo != null && !HwAPPQoEUserAction.this.isWifiConnected) {
                            WifiStateChangeInfo unused4 = HwAPPQoEUserAction.this.curWifiStateInfo = new WifiStateChangeInfo();
                            HwAPPQoEUserAction.this.curWifiStateInfo.wifiSSID = wifiInfo.getSSID();
                            HwAPPQoEUserAction.this.curWifiStateInfo.wifiConnTime = System.currentTimeMillis();
                            HwAPPQoEUserAction.this.curWifiStateInfo.wifiFreq = wifiInfo.getFrequency();
                            boolean unused5 = HwAPPQoEUserAction.this.isWifiConnected = true;
                            HwAPPQoEUtils.logD(HwAPPQoEUserAction.TAG, "CONNECTED:" + HwAPPQoEUserAction.this.curWifiStateInfo.wifiConnTime);
                        }
                    }
                } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED && HwAPPQoEUserAction.this.curWifiStateInfo.wifiSSID != null && HwAPPQoEUserAction.this.isWifiConnected) {
                    HwAPPQoEUserAction.this.curWifiStateInfo.wifiDisconnTime = System.currentTimeMillis();
                    HwAPPQoEUserAction.this.curWifiStateInfo.wifiRssi = HwAPPQoEUserAction.this.recentWifiRssi;
                    boolean unused6 = HwAPPQoEUserAction.this.isWifiConnected = false;
                    HwAPPQoEUtils.logD(HwAPPQoEUserAction.TAG, "DISCONNECTED:" + HwAPPQoEUserAction.this.curWifiStateInfo.wifiDisconnTime);
                }
            } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                int unused7 = HwAPPQoEUserAction.this.recentWifiRssi = intent.getIntExtra("newRssi", -127);
                HwAPPQoEUtils.logD(HwAPPQoEUserAction.TAG, "RSSI_CHANGE:" + HwAPPQoEUserAction.this.recentWifiRssi);
            }
        }
    }

    public static class WifiStateChangeInfo {
        public long wifiConnTime = -1;
        public long wifiDisconnTime = -1;
        public int wifiFreq = -1;
        public int wifiRssi = -1;
        public String wifiSSID = HwAPPQoEUtils.INVALID_STRING_VALUE;

        public String toString() {
            return "WifiStateChangeInfo:" + this.wifiConnTime + "," + this.wifiDisconnTime + "," + this.wifiSSID + "," + this.wifiRssi + "," + this.wifiFreq;
        }
    }

    public static class WifiSwitchChangeInfo {
        public WifiStateChangeInfo wifiState = new WifiStateChangeInfo();
        public int wifiSwitchState;
        public long wifiSwitchTime = System.currentTimeMillis();

        public WifiSwitchChangeInfo(int state) {
            this.wifiSwitchState = state;
        }
    }

    private HwAPPQoEUserAction(Context context) {
        this.mContext = context;
        this.mIsDefaultRadicalUser = isDefaultRadicalUser();
        registerBroadcastReceiver();
        this.mCollectManger = new LogCollectManager(context);
        this.mChrExcpReport = HwAPPChrExcpReport.getInstance();
        try {
            this.mDataManger = new HwAPPQoEDataBase(context);
            this.mDatabase = this.mDataManger.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            this.mDatabase = null;
            HwAPPQoEUtils.logD(TAG, "Init Error:" + e);
        }
    }

    public static HwAPPQoEUserAction createHwAPPQoEUserAction(Context context) {
        if (mUserAction == null) {
            mUserAction = new HwAPPQoEUserAction(context);
        }
        return mUserAction;
    }

    public static HwAPPQoEUserAction getInstance() {
        return mUserAction;
    }

    private void registerBroadcastReceiver() {
        this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
    }

    public void notifyAPPStateChange(long apkStartTime, long apkEndTime, int appId) {
        if (apkStartTime - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT <= this.currWifiDisableInfo.wifiSwitchTime && apkEndTime >= this.currWifiDisableInfo.wifiSwitchTime && this.currWifiDisableInfo.wifiSwitchState == 1 && this.currWifiDisableInfo.wifiSwitchTime - this.currWifiDisableInfo.wifiState.wifiDisconnTime <= 1000 && HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.currWifiDisableInfo.wifiState.wifiFreq, this.currWifiDisableInfo.wifiState.wifiRssi) > 1) {
            HwAPPQoEUtils.logD(TAG, "notifyAPPStateChange, wifi signal was better than threshold, RADICAL");
            updateUserActionData(2, appId, this.currWifiDisableInfo.wifiState.wifiSSID);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00db, code lost:
        if (r12 != null) goto L_0x00dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r12.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00fc, code lost:
        if (r12 == null) goto L_0x00ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ff, code lost:
        if (r8 == false) goto L_0x0177;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        com.android.server.hidata.appqoe.HwAPPQoEUtils.logD(TAG, "updateUserActionData userType :" + r2 + ", newCommonCnt:" + r4 + ", newRadicalCnt:" + r5);
        r0 = new android.content.ContentValues();
        r0.put("appId", java.lang.Integer.valueOf(r23));
        r0.put("wifiSSID", r3);
        r0.put("cardInfo", r9);
        r0.put("commonCnt", java.lang.Integer.valueOf(r4));
        r0.put("radicalCnt", java.lang.Integer.valueOf(r5));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x015a, code lost:
        r20 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r1.mDatabase.update(com.android.server.hidata.appqoe.HwAPPQoEDataBase.TABLE_USER_ACTION, r0, " (wifiSSID like ?) and (cardInfo = ?) and (appId = ?)", new java.lang.String[]{r3, r9, java.lang.String.valueOf(r23)});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0173, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0174, code lost:
        r20 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0177, code lost:
        r20 = r6;
        com.android.server.hidata.appqoe.HwAPPQoEUtils.logD(TAG, "insert userType :" + r2 + ", newCommonCnt:" + r4 + ", newRadicalCnt:" + r5);
        r1.mDatabase.execSQL("INSERT INTO APPQoEUserAction VALUES(null, ?, ?, ?, ?, ?)", new java.lang.Object[]{java.lang.Integer.valueOf(r23), r3, r9, java.lang.Integer.valueOf(r4), java.lang.Integer.valueOf(r5)});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x01c5, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x01c6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x01c7, code lost:
        r6 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x01db, code lost:
        throw r0;
     */
    public void updateUserActionData(int userType, int appId, String wifiSSID) {
        int i;
        Cursor c;
        int newRadicalCnt;
        String str = wifiSSID;
        int newCommonCnt = 0;
        int newRadicalCnt2 = 0;
        int cachedCommonCnt = 0;
        int cachedRadicalCnt = 0;
        boolean isEntryFound = false;
        HwAPPQoEUtils.logD(TAG, "updateUserActionData wifiSSID :" + str + " appId = " + appId + " userType = " + i);
        String cardInfo = getCurrentDefaultDataImsi();
        synchronized (this.mSqlLock) {
            try {
                if (this.mDatabase == null || !this.mDatabase.isOpen() || cardInfo == null || str == null) {
                    HwAPPQoEUtils.logD(TAG, "database invalid when update data.");
                    return;
                }
                c = null;
                try {
                    c = this.mDatabase.rawQuery("SELECT * FROM APPQoEUserAction where (wifiSSID like ?) and (cardInfo = ?) and (appId = ?)", new String[]{str, cardInfo, String.valueOf(appId)});
                    if (c.getCount() > 0 && c.moveToNext()) {
                        cachedCommonCnt = c.getInt(c.getColumnIndex("commonCnt"));
                        cachedRadicalCnt = c.getInt(c.getColumnIndex("radicalCnt"));
                        isEntryFound = true;
                        HwAPPQoEUtils.logD(TAG, "updateUserActionData cachedCommonCnt :" + cachedCommonCnt + ", cachedRadicalCnt:" + cachedRadicalCnt);
                    }
                    if (1 == i) {
                        newRadicalCnt = 0;
                        cachedCommonCnt++;
                        newCommonCnt = cachedCommonCnt;
                    } else {
                        newRadicalCnt = cachedRadicalCnt + 1;
                        newCommonCnt = 0;
                    }
                    newRadicalCnt2 = newRadicalCnt;
                    HwAPPQoEUtils.logD(TAG, "updateUserActionData newRadicalCnt :" + newRadicalCnt2 + ", newCommonCnt:" + newCommonCnt);
                } catch (SQLException e) {
                    HwAPPQoEUtils.logD(TAG, "updateUserActionData error:" + e);
                }
            } catch (Throwable th) {
                th = th;
            }
        }
    }

    public int getUserActionType(int appId) {
        int userType;
        synchronized (this.mSqlLock) {
            int number = queryUserActionData(this.curWifiStateInfo.wifiSSID, appId, getCurrentDefaultDataImsi());
            if (this.mIsDefaultRadicalUser) {
                if (number >= 3) {
                    userType = 1;
                } else {
                    userType = 2;
                }
            } else if (number >= 3) {
                userType = 2;
            } else {
                userType = 1;
            }
        }
        return userType;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0058, code lost:
        if (r2 != null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0078, code lost:
        if (r2 == null) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007c, code lost:
        return r0;
     */
    private int queryUserActionData(String wifiSSID, int appId, String cardInfo) {
        int number = 0;
        synchronized (this.mSqlLock) {
            if (this.mDatabase == null || !this.mDatabase.isOpen() || cardInfo == null || wifiSSID == null) {
                HwAPPQoEUtils.logD(TAG, "database invalid when get action data.");
                return 0;
            }
            Cursor c = null;
            try {
                c = this.mDatabase.rawQuery("SELECT * FROM APPQoEUserAction where (wifiSSID like ?) and (cardInfo = ?) and (appId = ?)", new String[]{wifiSSID, cardInfo, String.valueOf(appId)});
                if (c.getCount() > 0 && c.moveToNext()) {
                    number = this.mIsDefaultRadicalUser ? c.getInt(c.getColumnIndex("commonCnt")) : c.getInt(c.getColumnIndex("radicalCnt"));
                }
            } catch (SQLException e) {
                try {
                    HwAPPQoEUtils.logD(TAG, "queryUserActionData error:" + e);
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                    throw th;
                }
            }
        }
    }

    private String getCurrentDefaultDataImsi() {
        if (this.mCollectManger == null) {
            return null;
        }
        TelephonyManager mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (mTelephonyManager == null) {
            return null;
        }
        String imsi = mTelephonyManager.getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (imsi != null) {
            try {
                imsi = this.mCollectManger.doEncrypt(imsi);
            } catch (RemoteException ex) {
                HwAPPQoEUtils.logD(TAG, "get card info error:" + ex);
            }
        }
        return imsi;
    }

    public void setLatestAPPScenceId(int appScenceId) {
        this.mAppScenceId = appScenceId;
    }

    /* access modifiers changed from: private */
    public void reportExceptionInfo() {
        HwAPPQoEUtils.logD(TAG, "reportExceptionInfo, mAppScenceId:" + this.mAppScenceId);
        if (this.mAppScenceId > 0 && this.mChrExcpReport != null && System.currentTimeMillis() - this.currWifiDisableInfo.wifiState.wifiDisconnTime < 1000) {
            this.mChrExcpReport.reportAPPQoExcpInfo(1, this.mAppScenceId);
        }
    }

    public void resetUserActionType(int appId) {
        if (this.curWifiStateInfo.wifiSSID != null && !HwAPPQoEUtils.INVALID_STRING_VALUE.equals(this.curWifiStateInfo.wifiSSID)) {
            updateUserActionData(1, appId, this.curWifiStateInfo.wifiSSID);
        }
    }

    private boolean isDefaultRadicalUser() {
        String chipset = SystemProperties.get(CHIPSET_TYPE_PROP, "none");
        HwAPPQoEUtils.logD(TAG, "isDefaultRadicalUser, chipset = " + chipset);
        if (chipset == null || (!chipset.contains("4345") && !chipset.contains("4359") && !chipset.contains("1103"))) {
            return false;
        }
        return true;
    }
}
