package com.android.server.hidata.hinetwork;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.IMonitor;
import android.util.Log;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.hinetwork.HwHiNetworkParmStatistics;
import com.android.server.hidata.hinetwork.IAppNetService;
import com.android.server.pm.auth.DevicePublicKeyLoader;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.gameassist.IHiNetworkManager;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;

public class HwHiNetworkManager {
    private static final int ACCE_EFFECT_LEVEL_1 = 0;
    private static final int ACCE_EFFECT_LEVEL_2 = 1;
    private static final int ACCE_EFFECT_LEVEL_3 = 2;
    private static final int ACCE_EFFECT_LEVEL_4 = 3;
    private static final int ACCE_EFFECT_LEVEL_5 = 4;
    private static final int ACCE_FLAG_GAME = 0;
    private static final int ACCE_FLAG_VEIDO = 1;
    private static final int ACCE_STATUS_ACCELERATING = 1;
    private static final int ACCE_STATUS_NORMAL = 0;
    private static final int ACCE_SUPPORT_STATUS_INEXISTENCE = -1;
    private static final int ACCE_SUPPORT_STATUS_NONSUPPORT = 0;
    private static final int ACCE_SUPPORT_STATUS_XUNYOU = 1;
    private static final int CHR_UPLOAD_EVENT_ACCE_EFFECT = 909002069;
    private static final int CHR_UPLOAD_EVENT_TIME_DELAY = 909002068;
    private static final int CHR_UPLOAD_STATISTICS_EVENT = 909002067;
    private static final int DURATION_BIND_AIDL_OPEN_APP = 1000;
    private static final int DURATION_BIND_AIDL_POWER_ON = 20000;
    private static final int DURATION_CLOSE_APP = 20000;
    private static final int DURATION_UNBIND_AIDL = 30000;
    private static final int DURATION_WAIT_VENDOR_SDK_LAUNCH = 3000;
    private static final int E_909009067 = 909009067;
    private static final int E_909009068 = 909009068;
    private static final int E_909009069 = 909009069;
    private static final int FLAG_AIDL_BIND_FAID = 0;
    private static final int FLAG_AIDL_BIND_SUCC = 1;
    private static final int MESSAGE_APP_BACKGROUND = 5;
    private static final int MESSAGE_APP_CHANGE = 8;
    private static final int MESSAGE_APP_CLOSE = 4;
    private static final int MESSAGE_APP_FOREGROUND = 3;
    private static final int MESSAGE_APP_OPEN = 2;
    private static final int MESSAGE_DEAL_CLOSE_APP = 7;
    private static final int MESSAGE_DEAL_OPEN_APP = 6;
    private static final int MESSAGE_NULL = 0;
    private static final int MESSAGE_TIME_OUT = 1;
    private static final int MESSAGE_UNBIND_AIDL = 9;
    private static final int MODE_ACCELERATOR_ALL = 0;
    private static final int MODE_APP_BACKGROUND = 1;
    private static final int MODE_APP_END = 0;
    private static final int MODE_APP_FOREGROUND = 3;
    private static final int MODE_APP_OPEN = 2;
    private static final int NETWORKTYPE_CELL = 0;
    private static final int NETWORKTYPE_CELL_AND_WIFI = 2;
    private static final int NETWORKTYPE_WIFI = 1;
    private static final int NETWORK_TYPE_2G = 2;
    private static final int NETWORK_TYPE_3G = 3;
    private static final int NETWORK_TYPE_CELL = 0;
    private static final int NETWORK_TYPE_LTE = 4;
    private static final int NETWORK_TYPE_NO_SERVICE = -1;
    private static final int NETWORK_TYPE_WIFI = 1;
    private static final int NUM_BIND_AIDL_OPEN_APP = 3;
    private static final int NUM_BIND_AIDL_POWER_ON = 15;
    private static final int PURCHASE_PERIOD_INVALID = 0;
    private static final int PURCHASE_PERIOD_VALID = 1;
    private static final int SND_TIMED_DELAY_EVENT_DURATION = 3600000;
    private static final String TAG = "HiDATA_HiNetwork";
    private static final float THRESHOLD_ACCE_EFFECT = 0.1f;
    private static final int TIMER_STATUS_RUNNING = 0;
    private static final int TIMER_STATUS_STOP = 1;
    private static final int TIME_DELAY_LEVEL_1 = 0;
    private static final int TIME_DELAY_LEVEL_2 = 1;
    private static final int TIME_DELAY_LEVEL_3 = 2;
    private static final int TIME_DELAY_LEVEL_4 = 3;
    private static final int TIME_DELAY_LEVEL_ERR = -1;
    private static final int UPLOAD_STATISTICS_DURATION = 86400000;
    private static HwHiNetworkManager mHiNetworkManager;
    private List<String> curPackageNames;
    private String lastPkgName;
    private int lastUid;
    private ActivityManager mActivityManager;
    private IHwActivityNotifierEx mActivityNotifierEx;
    /* access modifiers changed from: private */
    public int mAidlBindFlag;
    /* access modifiers changed from: private */
    public List<AppObject> mAppList;
    /* access modifiers changed from: private */
    public BindAidlThread mBindAidlThread;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HiNetworkAidl mHiNetworkAidl;
    /* access modifiers changed from: private */
    public HwHiNetworkDataBase mHiNetworkDataBase;
    /* access modifiers changed from: private */
    public HwHiNetworkParmStatistics mHwHiNetworkParmStatistics;
    /* access modifiers changed from: private */
    public IAppNetService mIAppNetService;
    private List<String> mLastPackageNameList;
    private long mLastSndGainEventTime;
    private long mLastSndTimeDelayEventTime;
    /* access modifiers changed from: private */
    public long mLastUploadStatisticsTime;
    private LocalAppAcceList mLocalAppAcceList;
    private ServiceConnection mServiceConnection;
    /* access modifiers changed from: private */
    public Object object = new Object();

    public class AppObject {
        /* access modifiers changed from: private */
        public HiNetworkTimer mEndTimer;
        /* access modifiers changed from: private */
        public String mKey;
        /* access modifiers changed from: private */
        public String mPackageName;

        public AppObject(String packageName) {
            HwHiNetworkManager.log(" new appobject name is  " + packageName);
            this.mPackageName = packageName;
            this.mEndTimer = new HiNetworkTimer(20000, 1);
        }
    }

    public class BindAidlThread extends Thread {
        private int mDuration = 0;
        private int mNum = 0;

        public BindAidlThread(int duration, int num) {
            this.mDuration = duration;
            this.mNum = num;
        }

        public void run() {
            while (true) {
                try {
                    HwHiNetworkManager.log("bindAidl Thread run ");
                    if (1 != HwHiNetworkManager.this.mAidlBindFlag && true != HwHiNetworkManager.this.bindAidl()) {
                        if (this.mNum > 0) {
                            this.mNum--;
                            Thread.sleep((long) this.mDuration);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } catch (Exception e) {
                    HwHiNetworkManager.log("Exception str is " + e.toString());
                    return;
                }
            }
        }
    }

    private class HiNetworkAidl extends IHiNetworkManager.Stub {
        /* JADX WARNING: type inference failed for: r2v0, types: [com.android.server.hidata.hinetwork.HwHiNetworkManager$HiNetworkAidl, android.os.IBinder] */
        public HiNetworkAidl() {
            HwHiNetworkManager.log("start up HiNetworkAidl");
            try {
                ServiceManager.addService("hinetwork", this);
                HwHiNetworkManager.log("start up HiNetworkAidl success.");
            } catch (Throwable e) {
                HwHiNetworkManager.log("Failure starting HiNetworkAidl" + e.toString());
            }
        }

        public int onDetectTimeDelayResult(String timeDelayResult) throws RemoteException {
            String str = timeDelayResult;
            HwHiNetworkManager.this.sendUnbindAidlMsg();
            if (str == null || str.equals("")) {
                HwHiNetworkManager.log("onDetectTimeDelayResult failed, it's null.");
                return -1;
            } else if (!HwHiNetworkManager.this.isJson(str)) {
                HwHiNetworkManager.log("onDetectTimeDelayResult failed, it's not json");
                return -1;
            } else {
                HwHiNetworkManager.log("onDetectTimeDelayResult success " + str);
                float acceEffect = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                try {
                    long currentTime = System.currentTimeMillis();
                    JSONObject jsonObject = new JSONObject(str);
                    int beforeAcce = jsonObject.getInt("beforeAcce");
                    int afterAcce = jsonObject.getInt("afterAcce");
                    AppObject appObject = HwHiNetworkManager.this.isInLocalAppListByKey(jsonObject.getString(DevicePublicKeyLoader.KEY));
                    if (appObject != null && ((beforeAcce <= 0 && afterAcce > 0) || (beforeAcce > 0 && afterAcce > 0 && ((float) (beforeAcce - afterAcce)) / ((float) beforeAcce) > 0.1f))) {
                        synchronized (HwHiNetworkManager.this.object) {
                            HwHiNetworkManager.log("can useAccelerate");
                            HwHiNetworkManager.this.useAccelerate(appObject.mPackageName, 0);
                        }
                    }
                    if (afterAcce > 0 && beforeAcce > 0) {
                        acceEffect = (float) ((beforeAcce - afterAcce) / afterAcce);
                    }
                    if (1 == jsonObject.getInt("networkType")) {
                        int timeDelayLevel = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("A+X"));
                        if (-1 != timeDelayLevel) {
                            HwHiNetworkParmStatistics.Rtt rtt = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.wifiAccRtt[timeDelayLevel];
                            rtt.rtt = (short) (rtt.rtt + 1);
                        }
                        int timeDelayLevel2 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("C"));
                        if (-1 != timeDelayLevel2) {
                            HwHiNetworkParmStatistics.Rtt rtt2 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.wifiBonRtt[timeDelayLevel2];
                            rtt2.rtt = (short) (rtt2.rtt + 1);
                        }
                        int timeDelayLevel3 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("A+B'"));
                        if (-1 != timeDelayLevel3) {
                            HwHiNetworkParmStatistics.Rtt rtt3 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.wifiTotRtt[timeDelayLevel3];
                            rtt3.rtt = (short) (rtt3.rtt + 1);
                        }
                    } else if (jsonObject.getInt("networkType") == 0) {
                        int timeDelayLevel4 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("P+Z"));
                        if (-1 != timeDelayLevel4) {
                            HwHiNetworkParmStatistics.Rtt rtt4 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.cellAccRtt[timeDelayLevel4];
                            rtt4.rtt = (short) (rtt4.rtt + 1);
                        }
                        int timeDelayLevel5 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("R"));
                        if (-1 != timeDelayLevel5) {
                            HwHiNetworkParmStatistics.Rtt rtt5 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.cellBonRtt[timeDelayLevel5];
                            rtt5.rtt = (short) (rtt5.rtt + 1);
                        }
                        int timeDelayLevel6 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("P+Q"));
                        if (-1 != timeDelayLevel6) {
                            HwHiNetworkParmStatistics.Rtt rtt6 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.cellTotRtt[timeDelayLevel6];
                            rtt6.rtt = (short) (rtt6.rtt + 1);
                        }
                    } else if (2 == jsonObject.getInt("networkType")) {
                        int timeDelayLevel7 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("A+X"));
                        if (-1 != timeDelayLevel7) {
                            HwHiNetworkParmStatistics.Rtt rtt7 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.wifiAccRtt[timeDelayLevel7];
                            rtt7.rtt = (short) (rtt7.rtt + 1);
                        }
                        int timeDelayLevel8 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("C"));
                        if (-1 != timeDelayLevel8) {
                            HwHiNetworkParmStatistics.Rtt rtt8 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.wifiBonRtt[timeDelayLevel8];
                            rtt8.rtt = (short) (rtt8.rtt + 1);
                        }
                        int timeDelayLevel9 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("A+B'"));
                        if (-1 != timeDelayLevel9) {
                            HwHiNetworkParmStatistics.Rtt rtt9 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.wifiTotRtt[timeDelayLevel9];
                            rtt9.rtt = (short) (rtt9.rtt + 1);
                        }
                        int timeDelayLevel10 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("P+Z"));
                        if (-1 != timeDelayLevel10) {
                            HwHiNetworkParmStatistics.Rtt rtt10 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.cellAccRtt[timeDelayLevel10];
                            rtt10.rtt = (short) (rtt10.rtt + 1);
                        }
                        int timeDelayLevel11 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("R"));
                        if (-1 != timeDelayLevel11) {
                            HwHiNetworkParmStatistics.Rtt rtt11 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.cellBonRtt[timeDelayLevel11];
                            rtt11.rtt = (short) (rtt11.rtt + 1);
                        }
                        int timeDelayLevel12 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt("P+Q"));
                        if (-1 != timeDelayLevel12) {
                            HwHiNetworkParmStatistics.Rtt rtt12 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.cellTotRtt[timeDelayLevel12];
                            rtt12.rtt = (short) (rtt12.rtt + 1);
                        }
                    }
                    HwHiNetworkParmStatistics.TheraticGain theraticGain = HwHiNetworkManager.this.mHwHiNetworkParmStatistics.gain[HwHiNetworkManager.this.getAcceEffectLevel(acceEffect)];
                    theraticGain.gain = (short) (theraticGain.gain + 1);
                    if (currentTime - HwHiNetworkManager.this.mLastUploadStatisticsTime >= 86400000) {
                        HwHiNetworkManager.this.uploadHwHiNetworkParmStatistics();
                    }
                    HwHiNetworkTimeDelayInfo timeDelayInfo = new HwHiNetworkTimeDelayInfo();
                    timeDelayInfo.apk = jsonObject.getString("packageName");
                    timeDelayInfo.net = (short) jsonObject.getInt("networkType");
                    timeDelayInfo.wifiAccRtt = (short) jsonObject.getInt("A+X");
                    timeDelayInfo.wifiBonRtt = (short) jsonObject.getInt("C");
                    timeDelayInfo.wifiTotRtt = (short) jsonObject.getInt("A+B'");
                    timeDelayInfo.cellAccRtt = (short) jsonObject.getInt("P+Z");
                    timeDelayInfo.cellBonRtt = (short) jsonObject.getInt("R");
                    timeDelayInfo.cellTotRtt = (short) jsonObject.getInt("P+Q");
                    timeDelayInfo.aftAcc = (short) jsonObject.getInt("afterAcce");
                    timeDelayInfo.befAcc = (short) jsonObject.getInt("beforeAcce");
                    HwHiNetworkManager.this.sendCHRTimeDelayEvent(timeDelayInfo);
                    return 0;
                } catch (Exception e) {
                    HwHiNetworkManager.log("onDetectTimeDelayResult exception is:" + e.toString());
                    return -1;
                }
            }
        }

        public int onOpenAccelerateResult(String acceletrateResult) throws RemoteException {
            if (acceletrateResult == null || acceletrateResult.equals("")) {
                HwHiNetworkManager.log("onOpenAccelerateResult failed, it's null.");
                return -1;
            } else if (!HwHiNetworkManager.this.isJson(acceletrateResult)) {
                HwHiNetworkManager.log("onOpenAccelerateResult failed, it's not json");
                return -1;
            } else {
                HwHiNetworkManager.log("onOpenAccelerateResult success,acceletrateResult is " + acceletrateResult);
                try {
                    JSONObject jsonObject = new JSONObject(acceletrateResult);
                    int isOpenAccelerator = jsonObject.getInt("isOpenAccelerator");
                    int acceleratorType = jsonObject.getInt("acceleratorType");
                    if (acceleratorType == 0) {
                        if (isOpenAccelerator == 0) {
                            HwHiNetworkParmStatistics access$2700 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics;
                            access$2700.gameAccYes = (short) (access$2700.gameAccYes + 1);
                        } else if (-1 == isOpenAccelerator) {
                            HwHiNetworkParmStatistics access$27002 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics;
                            access$27002.gameAccNo = (short) (access$27002.gameAccNo + 1);
                        }
                    } else if (1 == acceleratorType) {
                        if (isOpenAccelerator == 0) {
                            HwHiNetworkParmStatistics access$27003 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics;
                            access$27003.videoAccYes = (short) (access$27003.videoAccYes + 1);
                        } else if (-1 == isOpenAccelerator) {
                            HwHiNetworkParmStatistics access$27004 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics;
                            access$27004.videoAccNo = (short) (access$27004.videoAccNo + 1);
                        }
                    }
                    return 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    return -1;
                }
            }
        }
    }

    public class HiNetworkTimer {
        private int mDuration;
        /* access modifiers changed from: private */
        public int mMessageType;
        private Timer mTimer;
        /* access modifiers changed from: private */
        public int mTimerStatus = 1;

        public HiNetworkTimer(int duration, int messageType) {
            this.mMessageType = messageType;
            if (duration < 0) {
                this.mDuration = 0;
            } else {
                this.mDuration = duration;
            }
        }

        /* access modifiers changed from: private */
        public int getTimerStatus() {
            return this.mTimerStatus;
        }

        /* access modifiers changed from: private */
        public void startTimer(final String packageName) {
            HwHiNetworkManager.log("startTimer, messageType is :" + String.valueOf(this.mMessageType));
            this.mTimerStatus = 0;
            this.mTimer = new Timer();
            this.mTimer.schedule(new TimerTask() {
                public void run() {
                    Message message = Message.obtain();
                    message.what = HiNetworkTimer.this.mMessageType;
                    if (1 == HiNetworkTimer.this.mMessageType) {
                        if (HwHiNetworkManager.this.mIAppNetService == null || HwHiNetworkManager.this.mAidlBindFlag == 0) {
                            BindAidlThread unused = HwHiNetworkManager.this.mBindAidlThread = new BindAidlThread(1000, 3);
                            HwHiNetworkManager.this.mBindAidlThread.start();
                        }
                        AppObject appObject = HwHiNetworkManager.this.isInLocalAppLisByName(packageName);
                        if (appObject != null) {
                            message.obj = appObject;
                            HwHiNetworkManager.this.mHandler.sendMessageDelayed(message, 3000);
                            HwHiNetworkManager.log("send message, messageType is :" + String.valueOf(HiNetworkTimer.this.mMessageType));
                        } else {
                            HwHiNetworkManager.log("appObject is inexistence,  don't send message.");
                        }
                    } else if (9 == HiNetworkTimer.this.mMessageType) {
                        HwHiNetworkManager.this.mHandler.sendMessage(message);
                        HwHiNetworkManager.log("send message, messageType is :" + String.valueOf(HiNetworkTimer.this.mMessageType));
                    }
                    int unused2 = HiNetworkTimer.this.mTimerStatus = 1;
                }
            }, (long) this.mDuration);
        }

        /* access modifiers changed from: private */
        public void stopTimer() {
            if (this.mTimer != null) {
                this.mTimer.cancel();
            }
            this.mTimerStatus = 1;
        }
    }

    public class LocalAppAcceList {
        SQLiteDatabase sqliteDatabase;

        public LocalAppAcceList() {
            this.sqliteDatabase = HwHiNetworkManager.this.mHiNetworkDataBase.getWritableDatabase();
        }

        public void put(String packageName, int isCanAcce) {
            try {
                ContentValues values = new ContentValues();
                Cursor cursor = this.sqliteDatabase.query(HwHiNetworkDataBase.TABLE_USER_ACTION, new String[]{"PACKAGENAME", "ISCANACCE"}, "PACKAGENAME=?", new String[]{packageName}, null, null, null);
                if (cursor.getCount() >= 1) {
                    cursor.close();
                    return;
                }
                values.put("PACKAGENAME", packageName);
                values.put("ISCANACCE", Integer.valueOf(isCanAcce));
                this.sqliteDatabase.insert(HwHiNetworkDataBase.TABLE_USER_ACTION, null, values);
                cursor.close();
            } catch (SQLException e) {
                HwHiNetworkManager.log("contain db file err: " + e.toString());
            }
        }

        public void remove(String packageName) {
            this.sqliteDatabase.delete(HwHiNetworkDataBase.TABLE_USER_ACTION, "PACKAGENAME=?", new String[]{packageName});
        }

        public int contain(String packageName) {
            int isCanAcce = -1;
            try {
                Cursor cursor = this.sqliteDatabase.query(HwHiNetworkDataBase.TABLE_USER_ACTION, new String[]{"PACKAGENAME", "ISCANACCE"}, "PACKAGENAME=?", new String[]{packageName}, null, null, null);
                if (cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    isCanAcce = cursor.getInt(cursor.getColumnIndex("ISCANACCE"));
                }
                cursor.close();
            } catch (SQLException e) {
                HwHiNetworkManager.log("contain db file err: " + e.toString());
            }
            HwHiNetworkManager.log("contain db file packageName: " + packageName + " iscanacce:" + String.valueOf(isCanAcce));
            return isCanAcce;
        }

        public void close() {
            this.sqliteDatabase.close();
        }
    }

    public static HwHiNetworkManager createInstance(Context context) {
        if (mHiNetworkManager == null) {
            mHiNetworkManager = new HwHiNetworkManager(context);
        }
        return mHiNetworkManager;
    }

    public static HwHiNetworkManager getInstance() {
        return mHiNetworkManager;
    }

    private HwHiNetworkManager(Context context) {
        log("HwHiNetworkManager initiate");
        this.mContext = context;
        this.mAidlBindFlag = 0;
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mAppList = new ArrayList();
        this.mLastPackageNameList = new ArrayList();
        this.mHiNetworkDataBase = new HwHiNetworkDataBase(this.mContext);
        this.mLocalAppAcceList = new LocalAppAcceList();
        this.mHwHiNetworkParmStatistics = new HwHiNetworkParmStatistics();
        this.mLastSndGainEventTime = System.currentTimeMillis();
        this.mLastSndTimeDelayEventTime = System.currentTimeMillis();
        this.mLastUploadStatisticsTime = System.currentTimeMillis();
        initHiNetworkManagerHandler();
        this.lastPkgName = "";
        this.lastUid = -1;
        this.curPackageNames = new ArrayList();
        this.mActivityNotifierEx = new IHwActivityNotifierEx() {
            public void call(Bundle extras) {
                if (extras == null) {
                    HwHiNetworkManager.log("AMS callback , extras=null");
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putInt("pid", extras.getInt("pid"));
                bundle.putInt("uid", extras.getInt("uid"));
                ComponentName componentName = (ComponentName) extras.getParcelable("comp");
                bundle.putString("packageName", componentName != null ? componentName.getPackageName() : "");
                if ("onResume".equals(extras.getString("state")) && MpLinkCommonUtils.isMpLinkEnabled(HwHiNetworkManager.this.mContext)) {
                    HwHiNetworkManager.this.mHandler.sendMessage(HwHiNetworkManager.this.mHandler.obtainMessage(8, bundle));
                }
            }
        };
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
        this.mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                HwHiNetworkManager.log("onServiceConnected aidl");
                IAppNetService unused = HwHiNetworkManager.this.mIAppNetService = IAppNetService.Stub.asInterface(service);
                int unused2 = HwHiNetworkManager.this.mAidlBindFlag = 1;
            }

            public void onServiceDisconnected(ComponentName name) {
                HwHiNetworkManager.log("onServiceDisconnected aidl");
                IAppNetService unused = HwHiNetworkManager.this.mIAppNetService = null;
                int unused2 = HwHiNetworkManager.this.mAidlBindFlag = 0;
            }
        };
        try {
            this.mHiNetworkAidl = new HiNetworkAidl();
        } catch (Throwable e) {
            log("can not set up hinetwor service" + e.toString());
        }
    }

    private void initHiNetworkManagerHandler() {
        HandlerThread handlerThread = new HandlerThread("HwHiNetworkManager_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (msg.obj != null) {
                            HwHiNetworkManager.this.onAppEndEx(((AppObject) msg.obj).mPackageName);
                            HwHiNetworkManager.this.mAppList.remove((AppObject) msg.obj);
                            return;
                        }
                        return;
                    case 2:
                        HwHiNetworkManager.this.onAppStart(String.valueOf(msg.obj), 2);
                        return;
                    case 3:
                        HwHiNetworkManager.this.onAppStart(String.valueOf(msg.obj), 3);
                        return;
                    case 4:
                        HwHiNetworkManager.this.onAppEnd(String.valueOf(msg.obj), 0);
                        return;
                    case 5:
                        HwHiNetworkManager.this.onAppEnd(String.valueOf(msg.obj), 1);
                        return;
                    case 6:
                        HwHiNetworkManager.this.onAppStartEx((AppObject) msg.obj);
                        return;
                    case 7:
                        HwHiNetworkManager.this.onAppEndEx(((AppObject) msg.obj).mPackageName);
                        HwHiNetworkManager.this.mAppList.remove((AppObject) msg.obj);
                        return;
                    case 8:
                        Bundle bundle = (Bundle) msg.obj;
                        HwHiNetworkManager.this.handleActivityChange(bundle.getString("packageName"), bundle.getInt("uid"));
                        return;
                    case 9:
                        boolean unused = HwHiNetworkManager.this.unBindAidl();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private String createKey() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static void logE(String info) {
        Log.e(TAG, info);
    }

    public static void log(String info) {
        Log.e(TAG, info);
    }

    /* access modifiers changed from: private */
    public void onAppStart(String packageName, int mode) {
        log("onAppStart packageName is " + packageName);
        AppObject appObject = isInLocalAppLisByName(packageName);
        int isCanAcce = this.mLocalAppAcceList.contain(packageName);
        if (appObject != null) {
            log("onAppStart stop timer.");
            appObject.mEndTimer.stopTimer();
        } else {
            appObject = new AppObject(packageName);
            this.mAppList.add(appObject);
        }
        if (2 == mode) {
            String unused = appObject.mKey = createKey();
            log("key is :" + appObject.mKey);
            if (isCanAcce != 0) {
                if (this.mIAppNetService == null || this.mAidlBindFlag == 0) {
                    this.mBindAidlThread = new BindAidlThread(1000, 3);
                    this.mBindAidlThread.start();
                }
                Message message = Message.obtain();
                message.what = 6;
                message.obj = appObject;
                this.mHandler.sendMessageDelayed(message, 3000);
            }
        } else if (3 == mode && appObject.mEndTimer.getTimerStatus() == 0) {
            appObject.mEndTimer.stopTimer();
        }
    }

    /* access modifiers changed from: private */
    public void onAppStartEx(AppObject appObject) {
        JSONObject appInfo;
        synchronized (this.object) {
            HiNetworkTimer unBindAidlTimer = new HiNetworkTimer(30000, 9);
            try {
                String establishedInfo = getEstablishedInfo(appObject.mPackageName, 0);
                if (establishedInfo == null || establishedInfo.equals("")) {
                    log("getEstablishedInfo error, result is null");
                } else {
                    log("establishedInfo is " + establishedInfo);
                    JSONObject jsonObject = new JSONObject(establishedInfo);
                    log("appInfo is " + appInfo);
                    int isSupport = appInfo.getInt("isSupport");
                    int establishedStatus = jsonObject.getInt("establishedStatus");
                    int isVaild = jsonObject.getInt("isValid");
                    this.mLocalAppAcceList.put(appObject.mPackageName, isSupport);
                    if (1 == isSupport && establishedStatus == 0 && isVaild == 0) {
                        String ipAddress = getInteractiveIpAddress(appObject.mPackageName);
                        int networkType = getNetworkType(this.mContext);
                        if (-1 == networkType) {
                            log("current network type is no service");
                            sendUnbindAidlMsg();
                            return;
                        }
                        if (1 == networkType) {
                            if (HwArbitrationCommonUtils.hasSimCard(this.mContext)) {
                                networkType = 2;
                            }
                        }
                        detectTimeDelay(appObject.mPackageName, networkType, ipAddress, appObject.mKey);
                    } else if (1 == isVaild) {
                        useAccelerate(appObject.mPackageName, 0);
                        sendUnbindAidlMsg();
                        return;
                    }
                }
                unBindAidlTimer.startTimer("");
            } catch (Exception e) {
                log("onAppStart thread exception. " + e.toString());
                sendUnbindAidlMsg();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onAppEnd(String packageName, int mode) {
        if (packageName != null && !packageName.equals("")) {
            log("onAppEnd enter packageName is " + packageName + " mode is:" + String.valueOf(mode));
            AppObject appObject = isInLocalAppLisByName(packageName);
            if (appObject == null) {
                synchronized (this.object) {
                    appObject = new AppObject(packageName);
                    this.mAppList.add(appObject);
                }
            } else if (appObject.mEndTimer.mTimerStatus == 0) {
                appObject.mEndTimer.stopTimer();
            }
            int isCanAcce = this.mLocalAppAcceList.contain(packageName);
            if (appObject != null && 1 == isCanAcce) {
                if (mode == 0) {
                    if (this.mIAppNetService == null || this.mAidlBindFlag == 0) {
                        this.mBindAidlThread = new BindAidlThread(1000, 3);
                        this.mBindAidlThread.start();
                    }
                    Message message = Message.obtain();
                    message.what = 7;
                    message.obj = appObject;
                    this.mHandler.sendMessageDelayed(message, 3000);
                } else if (1 == mode) {
                    synchronized (this.object) {
                        appObject.mEndTimer.startTimer(appObject.mPackageName);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onAppEndEx(String packageName) {
        log("enter onAppEndEx packageName is :" + packageName);
        try {
            String establishedInfo = getEstablishedInfo(packageName, 0);
            log("onAppEndEx establishedInfo is :" + establishedInfo);
            if (1 == new JSONObject(establishedInfo).getInt("establishedStatus")) {
                String acceEffect = getAccelerateEffect(packageName);
                log("acceEffect is :" + acceEffect);
            }
            sendUnbindAidlMsg();
        } catch (Exception e) {
            e.printStackTrace();
            sendUnbindAidlMsg();
        }
    }

    /* access modifiers changed from: private */
    public boolean bindAidl() {
        Intent intent = new Intent("com.huawei.gameassistant.netservice");
        intent.setPackage("com.huawei.gameassistant");
        Context context = this.mContext;
        ServiceConnection serviceConnection = this.mServiceConnection;
        Context context2 = this.mContext;
        boolean result = context.bindService(intent, serviceConnection, 1);
        log("bindAidl " + String.valueOf(result));
        return result;
    }

    /* access modifiers changed from: private */
    public boolean unBindAidl() {
        try {
            if (this.mIAppNetService != null || 1 == this.mAidlBindFlag) {
                log("unbindService with gameassistant.");
                this.mContext.unbindService(this.mServiceConnection);
                this.mAidlBindFlag = 0;
                this.mIAppNetService = null;
            }
            return true;
        } catch (Exception e) {
            log("unBindAidl exception.  " + e.toString());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void sendUnbindAidlMsg() {
        this.mHandler.removeMessages(9);
        this.mHandler.sendEmptyMessage(9);
    }

    /* access modifiers changed from: private */
    public AppObject isInLocalAppLisByName(String packageName) {
        AppObject appObject;
        synchronized (this.object) {
            appObject = null;
            Iterator<AppObject> it = this.mAppList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                AppObject item = it.next();
                if (packageName.equals(item.mPackageName) && !packageName.equals("")) {
                    appObject = item;
                    break;
                }
            }
        }
        return appObject;
    }

    /* access modifiers changed from: private */
    public AppObject isInLocalAppListByKey(String key) {
        for (AppObject item : this.mAppList) {
            if (key.equals(item.mKey)) {
                return item;
            }
        }
        return null;
    }

    public void detectTimeDelay(String packageName, int networkType, String ip, String key) {
        try {
            if (this.mIAppNetService != null) {
                log("detectTimeDelay packageName is: " + packageName + " networkType is : " + String.valueOf(networkType) + " ip is :" + ip + " key is :" + key);
                if (networkType == 0) {
                    HwHiNetworkParmStatistics hwHiNetworkParmStatistics = this.mHwHiNetworkParmStatistics;
                    hwHiNetworkParmStatistics.cellProbs = (short) (hwHiNetworkParmStatistics.cellProbs + 1);
                } else if (1 == networkType) {
                    HwHiNetworkParmStatistics hwHiNetworkParmStatistics2 = this.mHwHiNetworkParmStatistics;
                    hwHiNetworkParmStatistics2.wifiProbs = (short) (hwHiNetworkParmStatistics2.wifiProbs + 1);
                } else if (2 == networkType) {
                    HwHiNetworkParmStatistics hwHiNetworkParmStatistics3 = this.mHwHiNetworkParmStatistics;
                    hwHiNetworkParmStatistics3.bothProbs = (short) (hwHiNetworkParmStatistics3.bothProbs + 1);
                }
                this.mIAppNetService.detectTimeDelay(packageName, networkType, ip, key);
                return;
            }
            log("mIAppNetService is null, can not connect gameassistant ");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void useAccelerate(String packageName, int flag) {
        log("useAccelerate packageName is:" + packageName);
        try {
            if (this.mIAppNetService != null) {
                if (flag == 0) {
                    HwHiNetworkParmStatistics hwHiNetworkParmStatistics = this.mHwHiNetworkParmStatistics;
                    hwHiNetworkParmStatistics.gameAccNum = (short) (hwHiNetworkParmStatistics.gameAccNum + 1);
                } else if (1 == flag) {
                    HwHiNetworkParmStatistics hwHiNetworkParmStatistics2 = this.mHwHiNetworkParmStatistics;
                    hwHiNetworkParmStatistics2.videoAccNum = (short) (hwHiNetworkParmStatistics2.videoAccNum + 1);
                }
                this.mIAppNetService.useAccelerate(packageName, flag);
                return;
            }
            log("mIAppNetService is null, can not connect gameassistant ");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String getEstablishedInfo(String packageName, int flag) {
        try {
            if (this.mIAppNetService != null) {
                log("getEstablishedInfo packageName is :" + packageName);
                return this.mIAppNetService.getEstablishedInfo(packageName, flag);
            }
            log("mIAppNetService is null, can not connect gameassistant ");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            log("getEstablishedInfo Exception is :" + e.toString());
            return "";
        }
    }

    private String getAccelerateEffect(String packageName) {
        try {
            if (this.mIAppNetService != null) {
                log("getAccelerateEffect packageName is:" + packageName);
                String gainResult = this.mIAppNetService.getAccelerateEffect(packageName);
                try {
                    JSONObject jsonObject = new JSONObject(gainResult);
                    HwHiNetworkAcceEffectInfo acceEffectInfo = new HwHiNetworkAcceEffectInfo();
                    acceEffectInfo.apk = jsonObject.getString("packageName");
                    acceEffectInfo.gain = (short) jsonObject.getInt("pctOfAcce");
                    acceEffectInfo.avgRtt = (short) jsonObject.getInt("avgTimeDelay");
                    acceEffectInfo.pktLoss = (short) jsonObject.getInt("packetLossRate");
                    acceEffectInfo.net = (short) jsonObject.getInt("networkType");
                    acceEffectInfo.lagCnt = (short) jsonObject.getInt("lagCount");
                    acceEffectInfo.qosFlag = (short) jsonObject.getInt("4GQoSFlag");
                    acceEffectInfo.boneFlag = (short) jsonObject.getInt("backhaulFlag");
                    acceEffectInfo.dataUsg = (short) jsonObject.getInt("dataUsage");
                    acceEffectInfo.linkTime = (short) jsonObject.getInt("linkduration");
                    acceEffectInfo.code = (short) jsonObject.getInt("rtnCode");
                    sendCHRAcceEffectEvent(acceEffectInfo);
                    return gainResult;
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            } else {
                log("mIAppNetService is null, can not connect gameassistant ");
                return "";
            }
        } catch (RemoteException e2) {
            e2.printStackTrace();
            return "";
        }
    }

    /* access modifiers changed from: private */
    public int getTimeDelayLevel(int timeDeley) {
        if (timeDeley > 0 && timeDeley <= 50) {
            return 0;
        }
        if (timeDeley > 50 && timeDeley <= 100) {
            return 1;
        }
        if (timeDeley > 100 && timeDeley <= 200) {
            return 2;
        }
        if (timeDeley > 200) {
            return 3;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public int getAcceEffectLevel(float acceEffect) {
        if (((double) acceEffect) <= 0.0d) {
            return 0;
        }
        if (((double) acceEffect) <= 0.1d) {
            return 1;
        }
        if (((double) acceEffect) <= 0.3d) {
            return 2;
        }
        if (((double) acceEffect) <= 0.5d) {
            return 3;
        }
        return 4;
    }

    /* access modifiers changed from: private */
    public void uploadHwHiNetworkParmStatistics() {
        try {
            log("enter uploadHwHiNetworkParmStatistics.");
            IMonitor.EventStream event_statistics = IMonitor.openEventStream(CHR_UPLOAD_STATISTICS_EVENT);
            log("mHwHiNetworkParmStatistics.gameAccNum " + String.valueOf(this.mHwHiNetworkParmStatistics.gameAccNum));
            log("mHwHiNetworkParmStatistics.gameAccYes " + String.valueOf(this.mHwHiNetworkParmStatistics.gameAccYes));
            log("mHwHiNetworkParmStatistics.gameAccNo " + String.valueOf(this.mHwHiNetworkParmStatistics.gameAccNo));
            log("mHwHiNetworkParmStatistics.videoAccNum " + String.valueOf(this.mHwHiNetworkParmStatistics.videoAccNum));
            log("mHwHiNetworkParmStatistics.videoAccYes " + String.valueOf(this.mHwHiNetworkParmStatistics.videoAccYes));
            log("mHwHiNetworkParmStatistics.videoAccNo " + String.valueOf(this.mHwHiNetworkParmStatistics.videoAccNo));
            log("mHwHiNetworkParmStatistics.gameAccNum " + String.valueOf(this.mHwHiNetworkParmStatistics.gameAccNum));
            log("mHwHiNetworkParmStatistics.cellProbs " + String.valueOf(this.mHwHiNetworkParmStatistics.cellProbs));
            log("mHwHiNetworkParmStatistics.wifiProbs " + String.valueOf(this.mHwHiNetworkParmStatistics.wifiProbs));
            log("mHwHiNetworkParmStatistics.bothProbs " + String.valueOf(this.mHwHiNetworkParmStatistics.bothProbs));
            log("mHwHiNetworkParmStatistics. p+Z <50 " + String.valueOf(this.mHwHiNetworkParmStatistics.cellAccRtt[0].rtt));
            log("mHwHiNetworkParmStatistics.R<50 " + String.valueOf(this.mHwHiNetworkParmStatistics.cellBonRtt[0].rtt));
            log("mHwHiNetworkParmStatistics.P+Q<50 " + String.valueOf(this.mHwHiNetworkParmStatistics.cellTotRtt[0].rtt));
            log("mHwHiNetworkParmStatistics.gain " + String.valueOf(this.mHwHiNetworkParmStatistics.gameAccNum));
            event_statistics.setParam(HwHiNetworkParmStatistics.GAMEACCNUM, this.mHwHiNetworkParmStatistics.gameAccNum).setParam(HwHiNetworkParmStatistics.GAMEACCYES, this.mHwHiNetworkParmStatistics.gameAccYes).setParam(HwHiNetworkParmStatistics.GAMEACCNO, this.mHwHiNetworkParmStatistics.gameAccNo).setParam(HwHiNetworkParmStatistics.VIDEOACCNUM, this.mHwHiNetworkParmStatistics.videoAccNum).setParam(HwHiNetworkParmStatistics.VIDEOACCYES, this.mHwHiNetworkParmStatistics.videoAccYes).setParam(HwHiNetworkParmStatistics.VIDEOACCNO, this.mHwHiNetworkParmStatistics.videoAccNo);
            for (int i = 0; i < 10; i++) {
                IMonitor.EventStream event_rtt = IMonitor.openEventStream(E_909009067);
                event_rtt.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.cellAccRtt[i].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.cellAccRtt[i].rtt);
                event_statistics.fillArrayParam("cellAccRtt", event_rtt);
                IMonitor.closeEventStream(event_rtt);
            }
            for (int i2 = 0; i2 < 10; i2++) {
                IMonitor.EventStream event_rtt2 = IMonitor.openEventStream(E_909009067);
                event_rtt2.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.cellBonRtt[i2].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.cellBonRtt[i2].rtt);
                event_statistics.fillArrayParam("cellBonRtt", event_rtt2);
                IMonitor.closeEventStream(event_rtt2);
            }
            for (int i3 = 0; i3 < 10; i3++) {
                IMonitor.EventStream event_rtt3 = IMonitor.openEventStream(E_909009067);
                event_rtt3.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.cellTotRtt[i3].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.cellTotRtt[i3].rtt);
                event_statistics.fillArrayParam("cellTotRtt", event_rtt3);
                IMonitor.closeEventStream(event_rtt3);
            }
            for (int i4 = 0; i4 < 10; i4++) {
                IMonitor.EventStream event_rtt4 = IMonitor.openEventStream(E_909009067);
                event_rtt4.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.wifiAccRtt[i4].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.wifiAccRtt[i4].rtt);
                event_statistics.fillArrayParam(HwHiNetworkParmStatistics.WIFIACCRTT, event_rtt4);
                IMonitor.closeEventStream(event_rtt4);
            }
            for (int i5 = 0; i5 < 10; i5++) {
                IMonitor.EventStream event_rtt5 = IMonitor.openEventStream(E_909009067);
                event_rtt5.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.wifiBonRtt[i5].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.wifiBonRtt[i5].rtt);
                event_statistics.fillArrayParam(HwHiNetworkParmStatistics.WIFIBONRTT, event_rtt5);
                IMonitor.closeEventStream(event_rtt5);
            }
            for (int i6 = 0; i6 < 10; i6++) {
                IMonitor.EventStream event_rtt6 = IMonitor.openEventStream(E_909009067);
                event_rtt6.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.wifiTotRtt[i6].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.wifiTotRtt[i6].rtt);
                event_statistics.fillArrayParam(HwHiNetworkParmStatistics.WIFITOTRTT, event_rtt6);
                IMonitor.closeEventStream(event_rtt6);
            }
            event_statistics.setParam(HwHiNetworkParmStatistics.CELLPROBS, this.mHwHiNetworkParmStatistics.cellProbs).setParam(HwHiNetworkParmStatistics.WIFIPROBS, this.mHwHiNetworkParmStatistics.wifiProbs).setParam(HwHiNetworkParmStatistics.BOTHPROBS, this.mHwHiNetworkParmStatistics.bothProbs);
            for (int i7 = 0; i7 < 10; i7++) {
                IMonitor.EventStream event_gain = IMonitor.openEventStream(E_909009068);
                event_gain.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.gain[i7].level).setParam(HwHiNetworkParmStatistics.GAIN_SUB, this.mHwHiNetworkParmStatistics.gain[i7].gain);
                event_statistics.fillArrayParam("gain", event_gain);
                IMonitor.closeEventStream(event_gain);
            }
            for (int i8 = 0; i8 < 10; i8++) {
                IMonitor.EventStream event_usage = IMonitor.openEventStream(E_909009069);
                event_usage.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.wifiTotRtt[i8].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.wifiTotRtt[i8].rtt);
                event_statistics.fillArrayParam(HwHiNetworkParmStatistics.MOD, event_usage);
                IMonitor.closeEventStream(event_usage);
            }
            IMonitor.sendEvent(event_statistics);
            IMonitor.closeEventStream(event_statistics);
            this.mHwHiNetworkParmStatistics.reset();
            log("uploadHiNetworkParmStatistics success");
        } catch (RuntimeException e) {
            log("uploadHiNetworkParmStatistics RuntimeException" + e.toString());
        } catch (Exception e2) {
            log("uploadHiNetworkparamStatistics exception" + e2.toString());
        }
    }

    /* access modifiers changed from: private */
    public void sendCHRTimeDelayEvent(HwHiNetworkTimeDelayInfo timeDelayInfo) {
        log("enter sendCHRTimeDelayEvent.");
        if (System.currentTimeMillis() - this.mLastSndTimeDelayEventTime < WifiProCommonUtils.RECHECK_DELAYED_MS) {
            log("last snd acce effect event less than one hour");
            return;
        }
        IMonitor.EventStream timeDelayEventStream = IMonitor.openEventStream(CHR_UPLOAD_EVENT_TIME_DELAY);
        if (timeDelayInfo == null || timeDelayEventStream == null) {
            log("last snd time delay event less than one hour");
            return;
        }
        log("timeDelayInfo.apk is " + timeDelayInfo.apk);
        log("HwHiNetworkTimeDelayInfo.net is " + String.valueOf(timeDelayInfo.net));
        log("HwHiNetworkTimeDelayInfo.cellAccRtt is " + String.valueOf(timeDelayInfo.cellAccRtt));
        log("HwHiNetworkTimeDelayInfo.cellBonRtt is " + String.valueOf(timeDelayInfo.cellBonRtt));
        log("HwHiNetworkTimeDelayInfo.cellTotRtt is " + String.valueOf(timeDelayInfo.cellTotRtt));
        log("HwHiNetworkTimeDelayInfo.wifiAccRtt is " + String.valueOf(timeDelayInfo.wifiAccRtt));
        log("HwHiNetworkTimeDelayInfo.wifiBonRtt is " + String.valueOf(timeDelayInfo.wifiBonRtt));
        log("HwHiNetworkTimeDelayInfo.wifiTotRtt is " + String.valueOf(timeDelayInfo.wifiTotRtt));
        log("HwHiNetworkTimeDelayInfo.aftAcc is " + String.valueOf(timeDelayInfo.aftAcc));
        log("HwHiNetworkTimeDelayInfo.befAcc is " + String.valueOf(timeDelayInfo.befAcc));
        timeDelayEventStream.setParam("apk", timeDelayInfo.apk).setParam("apk", timeDelayInfo.apk).setParam("net", timeDelayInfo.net).setParam("cellAccRtt", timeDelayInfo.cellAccRtt).setParam("cellBonRtt", timeDelayInfo.cellBonRtt).setParam("cellTotRtt", timeDelayInfo.cellTotRtt).setParam(HwHiNetworkTimeDelayInfo.WIFIACCRTT, timeDelayInfo.wifiAccRtt).setParam(HwHiNetworkTimeDelayInfo.WIFIBONRTT, timeDelayInfo.wifiBonRtt).setParam(HwHiNetworkTimeDelayInfo.WIFITOTRTT, timeDelayInfo.wifiTotRtt).setParam(HwHiNetworkTimeDelayInfo.AFRACC, timeDelayInfo.aftAcc).setParam(HwHiNetworkTimeDelayInfo.BEFACC, timeDelayInfo.befAcc);
        IMonitor.sendEvent(timeDelayEventStream);
        IMonitor.closeEventStream(timeDelayEventStream);
        this.mLastSndTimeDelayEventTime = System.currentTimeMillis();
        log("sendCHRTimeDelayEvent success");
    }

    private void sendCHRAcceEffectEvent(HwHiNetworkAcceEffectInfo acceEffectInfo) {
        log("enter sendCHRAcceEffectEvent.");
        if (System.currentTimeMillis() - this.mLastSndGainEventTime < WifiProCommonUtils.RECHECK_DELAYED_MS) {
            log("last snd acce effect event less than one hour");
            return;
        }
        IMonitor.EventStream acceEffectEventStream = IMonitor.openEventStream(CHR_UPLOAD_EVENT_ACCE_EFFECT);
        if (acceEffectInfo != null && acceEffectEventStream != null) {
            log("acceEffectInfo.apk is " + acceEffectInfo.apk);
            log("acceEffectInfo.gain is " + String.valueOf(acceEffectInfo.gain));
            log("acceEffectInfo.avgRtt is " + String.valueOf(acceEffectInfo.avgRtt));
            log("acceEffectInfo.pktLoss is " + String.valueOf(acceEffectInfo.pktLoss));
            log("acceEffectInfo.net is " + String.valueOf(acceEffectInfo.net));
            log("acceEffectInfo.lagCnt is " + String.valueOf(acceEffectInfo.lagCnt));
            log("acceEffectInfo.dualCHFlag is " + String.valueOf(acceEffectInfo.dualCHFlag));
            log("acceEffectInfo.qosFlag is " + String.valueOf(acceEffectInfo.qosFlag));
            log("acceEffectInfo.boneFlag is " + String.valueOf(acceEffectInfo.boneFlag));
            log("acceEffectInfo.dataUsg is " + String.valueOf(acceEffectInfo.dataUsg));
            log("acceEffectInfo.linkTime is " + String.valueOf(acceEffectInfo.linkTime));
            log("acceEffectInfo.code is " + String.valueOf(acceEffectInfo.code));
            acceEffectEventStream.setParam("apk", acceEffectInfo.apk).setParam("gain", acceEffectInfo.gain).setParam(HwHiNetworkAcceEffectInfo.AVGRTT, acceEffectInfo.avgRtt).setParam(HwHiNetworkAcceEffectInfo.PKRLOSS, acceEffectInfo.pktLoss).setParam("net", acceEffectInfo.net).setParam(HwHiNetworkAcceEffectInfo.LAGCNT, acceEffectInfo.lagCnt).setParam(HwHiNetworkAcceEffectInfo.DUALCHFLAG, acceEffectInfo.dualCHFlag).setParam(HwHiNetworkAcceEffectInfo.QOSFLAG, acceEffectInfo.qosFlag).setParam(HwHiNetworkAcceEffectInfo.BONEFLAG, acceEffectInfo.boneFlag).setParam(HwHiNetworkAcceEffectInfo.DATAUSG, acceEffectInfo.dataUsg).setParam(HwHiNetworkAcceEffectInfo.LINKTIME, acceEffectInfo.linkTime).setParam(HwHiNetworkAcceEffectInfo.CODE, acceEffectInfo.code);
            IMonitor.sendEvent(acceEffectEventStream);
            IMonitor.closeEventStream(acceEffectEventStream);
            this.mLastSndGainEventTime = System.currentTimeMillis();
            log("sendCHRAcceEffectEvent success");
        }
    }

    private List<String> getPackageNames() {
        List<String> packageNames = new ArrayList<>();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = this.mActivityManager.getRunningAppProcesses();
        List<PackageInfo> packageInfos = this.mContext.getPackageManager().getInstalledPackages(0);
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                String[] pkgList = appProcess.pkgList;
                if (pkgList != null) {
                    for (String packageName : pkgList) {
                        if (packageInfos != null && packageInfos.size() > 0) {
                            Iterator<PackageInfo> it = packageInfos.iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    break;
                                }
                                PackageInfo packageInfo = it.next();
                                if (!packageInfo.packageName.equals("") && packageInfo.packageName.equals(packageName)) {
                                    packageNames.add(packageName);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return packageNames;
    }

    private String getInteractiveIpAddress(String packageName) {
        return "";
    }

    public static int getNetworkType(Context context) {
        int netType;
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null) {
            return -1;
        }
        int nType = networkInfo.getType();
        if (nType == 1) {
            netType = 1;
        } else if (nType == 0) {
            netType = 0;
        } else {
            netType = -1;
        }
        return netType;
    }

    public boolean isVpnConnected() {
        log("enter isVpnConnected");
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                Iterator<T> it = Collections.list(niList).iterator();
                while (it.hasNext()) {
                    NetworkInterface intf = (NetworkInterface) it.next();
                    if (intf.isUp()) {
                        if (intf.getInterfaceAddresses().size() != 0) {
                            if (!intf.getName().equals("tun0")) {
                                if (intf.getName().equals("ppp0")) {
                                }
                            }
                            log("xunyou vpn is connected");
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isOpenApp(String curPkgName, List<String> lastPkgNames) {
        for (String item : lastPkgNames) {
            if (curPkgName.equals(item)) {
                return false;
            }
        }
        return true;
    }

    private boolean isCloseApp(String lastPkgName2, List<String> curPkgNames) {
        for (String item : curPkgNames) {
            if (lastPkgName2.equals(item)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSystemServiceOrApp(String packageName) {
        if (packageName != null) {
            try {
                if (!packageName.equals("")) {
                    return (this.mContext.getPackageManager().getApplicationInfo(packageName, 0).flags & 1) == 1 || packageName.equals("");
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public boolean isJson(String content) {
        try {
            new JSONObject(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void handleActivityChange(String curPkgName, int curUid) {
        log("monitor handleActivityChange curPkgName is " + curPkgName + " curUid is " + String.valueOf(curUid) + " lastPkgName is :" + this.lastPkgName);
        if (this.lastUid != curUid) {
            this.curPackageNames = getPackageNames();
            if (!isSystemServiceOrApp(curPkgName)) {
                Message openMessage = Message.obtain();
                if (isOpenApp(curPkgName, this.mLastPackageNameList)) {
                    openMessage.what = 2;
                    openMessage.obj = curPkgName;
                } else {
                    openMessage.what = 3;
                    openMessage.obj = curPkgName;
                }
                this.mHandler.sendMessage(openMessage);
            }
            if (!isSystemServiceOrApp(this.lastPkgName)) {
                Message closeMessage = Message.obtain();
                if (isCloseApp(this.lastPkgName, this.curPackageNames)) {
                    closeMessage.what = 4;
                    closeMessage.obj = this.lastPkgName;
                } else {
                    closeMessage.what = 5;
                    closeMessage.obj = this.lastPkgName;
                }
                this.mHandler.sendMessage(closeMessage);
            }
            this.lastUid = curUid;
            this.lastPkgName = curPkgName;
            this.mLastPackageNameList.clear();
            for (String item : this.curPackageNames) {
                this.mLastPackageNameList.add(item);
            }
        }
    }
}
