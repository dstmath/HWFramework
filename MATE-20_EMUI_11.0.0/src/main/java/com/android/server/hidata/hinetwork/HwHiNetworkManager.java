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
import android.rms.iaware.AppTypeRecoManager;
import android.util.IMonitor;
import android.util.wifi.HwHiLog;
import com.android.server.appprotect.AppProtectActionConstant;
import com.android.server.hidata.arbitration.HwAppTimeDetail;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.hinetwork.HwHiNetworkParmStatistics;
import com.android.server.hidata.hinetwork.IAppNetService;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.gameassist.IHiNetworkManager;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
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
    private static final String APP_PACKAGE_NAME = "packageName";
    private static final String APP_UID = "uid";
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
    private static final String HW_SIGNATURE_OR_SYSTEM_PERMISSION = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final int MESSAGE_APP_BACKGROUND = 5;
    private static final int MESSAGE_APP_CHANGE = 8;
    private static final int MESSAGE_APP_CLOSE = 4;
    private static final int MESSAGE_APP_FOREGROUND = 3;
    private static final int MESSAGE_APP_OPEN = 2;
    private static final int MESSAGE_DEAL_CLOSE_APP = 7;
    private static final int MESSAGE_DEAL_OPEN_APP = 6;
    private static final int MESSAGE_DETECT_SPEED = 10;
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
    private int detectSpeed = 0;
    private String lastPkgName;
    private int lastUid;
    private ActivityManager mActivityManager;
    private IHwActivityNotifierEx mActivityNotifierEx;
    private int mAidlBindFlag;
    private List<AppObject> mAppList;
    private BindAidlThread mBindAidlThread;
    private Context mContext;
    private Handler mHandler;
    private HiNetworkAidl mHiNetworkAidl;
    private HwHiNetworkDataBase mHiNetworkDataBase;
    private HwHiNetworkParmStatistics mHwHiNetworkParmStatistics;
    private IAppNetService mIAppNetService;
    private List<String> mLastPackageNameList;
    private long mLastSndGainEventTime;
    private long mLastSndTimeDelayEventTime;
    private long mLastUploadStatisticsTime;
    private LocalAppAcceList mLocalAppAcceList;
    private ServiceConnection mServiceConnection;
    private final Object object = new Object();

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
        log(false, "HwHiNetworkManager initiate", new Object[0]);
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
            /* class com.android.server.hidata.hinetwork.HwHiNetworkManager.AnonymousClass1 */

            public void call(Bundle extras) {
                if (extras == null) {
                    HwHiNetworkManager.log(false, "AMS callback , extras=null", new Object[0]);
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
            /* class com.android.server.hidata.hinetwork.HwHiNetworkManager.AnonymousClass2 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                HwHiNetworkManager.log(false, "onServiceConnected aidl", new Object[0]);
                HwHiNetworkManager.this.mIAppNetService = IAppNetService.Stub.asInterface(service);
                HwHiNetworkManager.this.mAidlBindFlag = 1;
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                HwHiNetworkManager.log(false, "onServiceDisconnected aidl", new Object[0]);
                HwHiNetworkManager.this.mIAppNetService = null;
                HwHiNetworkManager.this.mAidlBindFlag = 0;
            }
        };
        try {
            this.mHiNetworkAidl = new HiNetworkAidl();
        } catch (Throwable e) {
            log(false, "can not set up hinetwor service" + e.getMessage(), new Object[0]);
        }
    }

    private void initHiNetworkManagerHandler() {
        HandlerThread handlerThread = new HandlerThread("HwHiNetworkManager_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.hidata.hinetwork.HwHiNetworkManager.AnonymousClass3 */

            @Override // android.os.Handler
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
                        if (msg.obj instanceof Bundle) {
                            Bundle bundle = (Bundle) msg.obj;
                            if (bundle.getString("packageName") != null) {
                                HwHiNetworkManager.this.handleActivityChange(bundle.getString("packageName"), bundle.getInt("uid"));
                                return;
                            }
                            return;
                        }
                        return;
                    case 9:
                        HwHiNetworkManager.this.unBindAidl();
                        return;
                    case 10:
                        HwAppTimeDetail.getInstance().handleTimeThread();
                        Message msgCycle = HwHiNetworkManager.this.mHandler.obtainMessage();
                        msgCycle.what = 10;
                        HwHiNetworkManager.this.mHandler.sendMessageDelayed(msgCycle, 3000);
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

    private static void logE(boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.e(TAG, isFmtStrPrivate, info, args);
    }

    public static void log(boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.e(TAG, isFmtStrPrivate, info, args);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAppStart(String packageName, int mode) {
        log(false, "onAppStart packageName is %{public}s", packageName);
        if (AppTypeRecoManager.getInstance().getAppType(packageName) != 9) {
            log(false, "Apptype is not game, not care app start", new Object[0]);
            return;
        }
        AppObject appObject = isInLocalAppLisByName(packageName);
        int isCanAcce = this.mLocalAppAcceList.contain(packageName);
        if (appObject != null) {
            log(false, "onAppStart stop timer.", new Object[0]);
            appObject.mEndTimer.stopTimer();
        } else {
            appObject = new AppObject(packageName);
            this.mAppList.add(appObject);
        }
        if (2 == mode) {
            appObject.mKey = createKey();
            log(false, "key is :%{public}s", appObject.mKey);
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
    /* access modifiers changed from: public */
    private void onAppStartEx(AppObject appObject) {
        synchronized (this.object) {
            HiNetworkTimer unBindAidlTimer = new HiNetworkTimer(30000, 9);
            try {
                String establishedInfo = getEstablishedInfo(appObject.mPackageName, 0);
                if (establishedInfo == null || establishedInfo.equals("")) {
                    log(false, "getEstablishedInfo error, result is null", new Object[0]);
                } else {
                    log(false, "establishedInfo is %{public}s", establishedInfo);
                    JSONObject jsonObject = new JSONObject(establishedInfo);
                    JSONObject appInfo = new JSONObject(jsonObject.getString("appInfo"));
                    log(false, "appInfo is %{public}s", appInfo.toString());
                    int isSupport = appInfo.getInt("isSupport");
                    int establishedStatus = jsonObject.getInt("establishedStatus");
                    int isVaild = jsonObject.getInt("isValid");
                    this.mLocalAppAcceList.put(appObject.mPackageName, isSupport);
                    if (1 == isSupport && establishedStatus == 0 && isVaild == 0) {
                        String ipAddress = getInteractiveIpAddress(appObject.mPackageName);
                        int networkType = getNetworkType(this.mContext);
                        if (-1 == networkType) {
                            log(false, "current network type is no service", new Object[0]);
                            sendUnbindAidlMsg();
                            return;
                        }
                        if (1 == networkType && HwArbitrationCommonUtils.hasSimCard(this.mContext)) {
                            networkType = 2;
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
                log(false, "onAppStart thread exception", new Object[0]);
                sendUnbindAidlMsg();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAppEnd(String packageName, int mode) {
        if (packageName != null && !packageName.equals("")) {
            log(false, "onAppEnd enter packageName is %{public}s mode is:%{public}d", packageName, Integer.valueOf(mode));
            if (AppTypeRecoManager.getInstance().getAppType(packageName) != 9) {
                log(false, "Apptype is not game, not care app end", new Object[0]);
                return;
            }
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
    /* access modifiers changed from: public */
    private void onAppEndEx(String packageName) {
        log(false, "enter onAppEndEx packageName is :%{public}s", packageName);
        try {
            String establishedInfo = getEstablishedInfo(packageName, 0);
            if (establishedInfo == null) {
                log(false, "establishedInfo is null, just return", new Object[0]);
                return;
            }
            log(false, "onAppEndEx establishedInfo is :%{public}s", establishedInfo);
            if (1 == new JSONObject(establishedInfo).getInt("establishedStatus")) {
                log(false, "acceEffect is :%{public}s", getAccelerateEffect(packageName));
            }
            sendUnbindAidlMsg();
        } catch (Exception e) {
            log(false, "Exception happeded in function onAppEndEx", new Object[0]);
            sendUnbindAidlMsg();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean bindAidl() {
        Intent intent = new Intent("com.huawei.gameassistant.netservice");
        intent.setPackage("com.huawei.gameassistant");
        intent.setComponent(intent.resolveSystemService(this.mContext.getPackageManager(), 0));
        boolean result = this.mContext.bindService(intent, this.mServiceConnection, 1);
        log(false, "bindAidl %{public}s", String.valueOf(result));
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean unBindAidl() {
        try {
            if (this.mIAppNetService != null || 1 == this.mAidlBindFlag) {
                log(false, "unbindService with gameassistant.", new Object[0]);
                this.mContext.unbindService(this.mServiceConnection);
                this.mAidlBindFlag = 0;
                this.mIAppNetService = null;
            }
            return true;
        } catch (Exception e) {
            log(false, "Exception happened in unBindAidl", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUnbindAidlMsg() {
        this.mHandler.removeMessages(9);
        this.mHandler.sendEmptyMessage(9);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AppObject isInLocalAppLisByName(String packageName) {
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
    /* access modifiers changed from: public */
    private AppObject isInLocalAppListByKey(String key) {
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
                log(false, "detectTimeDelay packageName is: %{public}s networkType is : %{public}d ip is :%{private}s key is :%{public}s", packageName, Integer.valueOf(networkType), ip, key);
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
            log(false, "mIAppNetService is null, can not connect gameassistant ", new Object[0]);
        } catch (RemoteException e) {
            log(false, "Exception happened in function detectTimeDelay", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void useAccelerate(String packageName, int flag) {
        log(false, "useAccelerate packageName is:%{public}s", packageName);
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
            log(false, "mIAppNetService is null, can not connect gameassistant ", new Object[0]);
        } catch (RemoteException e) {
            log(false, "Exception happened in function useAccelerate", new Object[0]);
        }
    }

    private String getEstablishedInfo(String packageName, int flag) {
        try {
            if (AppTypeRecoManager.getInstance().getAppType(packageName) != 9) {
                log(false, "Apptype is not game, no need getEstablishedInfo", new Object[0]);
                return "";
            } else if (this.mIAppNetService != null) {
                log(false, "getEstablishedInfo packageName is :%{public}s", packageName);
                return this.mIAppNetService.getEstablishedInfo(packageName, flag);
            } else {
                log(false, "mIAppNetService is null, can not connect gameassistant ", new Object[0]);
                return "";
            }
        } catch (Exception e) {
            log(false, "Exception happened in getEstablishedInfo", new Object[0]);
            return "";
        }
    }

    private String getAccelerateEffect(String packageName) {
        try {
            if (this.mIAppNetService != null) {
                log(false, "getAccelerateEffect packageName is:%{public}s", packageName);
                String gainResult = this.mIAppNetService.getAccelerateEffect(packageName);
                if (gainResult == null) {
                    return "";
                }
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
                    log(false, "Exception happened when get elements from json Object", new Object[0]);
                    return "";
                }
            } else {
                log(false, "mIAppNetService is null, can not connect gameassistant ", new Object[0]);
                return "";
            }
        } catch (RemoteException e2) {
            log(false, "Exception happened in function getAccelerateEffect", new Object[0]);
            return "";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getTimeDelayLevel(int timeDeley) {
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
    /* access modifiers changed from: public */
    private int getAcceEffectLevel(float acceEffect) {
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
    /* access modifiers changed from: public */
    private void uploadHwHiNetworkParmStatistics() {
        try {
            log(false, "enter uploadHwHiNetworkParmStatistics.", new Object[0]);
            IMonitor.EventStream event_statistics = IMonitor.openEventStream((int) CHR_UPLOAD_STATISTICS_EVENT);
            log(false, "mHwHiNetworkParmStatistics.gameAccNum %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.gameAccNum));
            log(false, "mHwHiNetworkParmStatistics.gameAccYes %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.gameAccYes));
            log(false, "mHwHiNetworkParmStatistics.gameAccNo %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.gameAccNo));
            log(false, "mHwHiNetworkParmStatistics.videoAccNum %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.videoAccNum));
            log(false, "mHwHiNetworkParmStatistics.videoAccYes %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.videoAccYes));
            log(false, "mHwHiNetworkParmStatistics.videoAccNo %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.videoAccNo));
            log(false, "mHwHiNetworkParmStatistics.gameAccNum %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.gameAccNum));
            log(false, "mHwHiNetworkParmStatistics.cellProbs %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.cellProbs));
            log(false, "mHwHiNetworkParmStatistics.wifiProbs %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.wifiProbs));
            log(false, "mHwHiNetworkParmStatistics.bothProbs %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.bothProbs));
            log(false, "mHwHiNetworkParmStatistics. p+Z <50 %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.cellAccRtt[0].rtt));
            log(false, "mHwHiNetworkParmStatistics.R<50 %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.cellBonRtt[0].rtt));
            log(false, "mHwHiNetworkParmStatistics.P+Q<50 %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.cellTotRtt[0].rtt));
            log(false, "mHwHiNetworkParmStatistics.gain %{public}d", Short.valueOf(this.mHwHiNetworkParmStatistics.gameAccNum));
            event_statistics.setParam(HwHiNetworkParmStatistics.GAMEACCNUM, this.mHwHiNetworkParmStatistics.gameAccNum).setParam(HwHiNetworkParmStatistics.GAMEACCYES, this.mHwHiNetworkParmStatistics.gameAccYes).setParam(HwHiNetworkParmStatistics.GAMEACCNO, this.mHwHiNetworkParmStatistics.gameAccNo).setParam(HwHiNetworkParmStatistics.VIDEOACCNUM, this.mHwHiNetworkParmStatistics.videoAccNum).setParam(HwHiNetworkParmStatistics.VIDEOACCYES, this.mHwHiNetworkParmStatistics.videoAccYes).setParam(HwHiNetworkParmStatistics.VIDEOACCNO, this.mHwHiNetworkParmStatistics.videoAccNo);
            for (int i = 0; i < 10; i++) {
                IMonitor.EventStream event_rtt = IMonitor.openEventStream((int) E_909009067);
                event_rtt.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.cellAccRtt[i].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.cellAccRtt[i].rtt);
                event_statistics.fillArrayParam("cellAccRtt", event_rtt);
                IMonitor.closeEventStream(event_rtt);
            }
            for (int i2 = 0; i2 < 10; i2++) {
                IMonitor.EventStream event_rtt2 = IMonitor.openEventStream((int) E_909009067);
                event_rtt2.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.cellBonRtt[i2].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.cellBonRtt[i2].rtt);
                event_statistics.fillArrayParam("cellBonRtt", event_rtt2);
                IMonitor.closeEventStream(event_rtt2);
            }
            for (int i3 = 0; i3 < 10; i3++) {
                IMonitor.EventStream event_rtt3 = IMonitor.openEventStream((int) E_909009067);
                event_rtt3.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.cellTotRtt[i3].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.cellTotRtt[i3].rtt);
                event_statistics.fillArrayParam("cellTotRtt", event_rtt3);
                IMonitor.closeEventStream(event_rtt3);
            }
            for (int i4 = 0; i4 < 10; i4++) {
                IMonitor.EventStream event_rtt4 = IMonitor.openEventStream((int) E_909009067);
                event_rtt4.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.wifiAccRtt[i4].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.wifiAccRtt[i4].rtt);
                event_statistics.fillArrayParam(HwHiNetworkParmStatistics.WIFIACCRTT, event_rtt4);
                IMonitor.closeEventStream(event_rtt4);
            }
            for (int i5 = 0; i5 < 10; i5++) {
                IMonitor.EventStream event_rtt5 = IMonitor.openEventStream((int) E_909009067);
                event_rtt5.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.wifiBonRtt[i5].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.wifiBonRtt[i5].rtt);
                event_statistics.fillArrayParam(HwHiNetworkParmStatistics.WIFIBONRTT, event_rtt5);
                IMonitor.closeEventStream(event_rtt5);
            }
            for (int i6 = 0; i6 < 10; i6++) {
                IMonitor.EventStream event_rtt6 = IMonitor.openEventStream((int) E_909009067);
                event_rtt6.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.wifiTotRtt[i6].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.wifiTotRtt[i6].rtt);
                event_statistics.fillArrayParam(HwHiNetworkParmStatistics.WIFITOTRTT, event_rtt6);
                IMonitor.closeEventStream(event_rtt6);
            }
            event_statistics.setParam(HwHiNetworkParmStatistics.CELLPROBS, this.mHwHiNetworkParmStatistics.cellProbs).setParam(HwHiNetworkParmStatistics.WIFIPROBS, this.mHwHiNetworkParmStatistics.wifiProbs).setParam(HwHiNetworkParmStatistics.BOTHPROBS, this.mHwHiNetworkParmStatistics.bothProbs);
            for (int i7 = 0; i7 < 10; i7++) {
                IMonitor.EventStream event_gain = IMonitor.openEventStream((int) E_909009068);
                event_gain.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.gain[i7].level).setParam(HwHiNetworkParmStatistics.GAIN_SUB, this.mHwHiNetworkParmStatistics.gain[i7].gain);
                event_statistics.fillArrayParam("gain", event_gain);
                IMonitor.closeEventStream(event_gain);
            }
            for (int i8 = 0; i8 < 10; i8++) {
                IMonitor.EventStream event_usage = IMonitor.openEventStream((int) E_909009069);
                event_usage.setParam(HwHiNetworkParmStatistics.LEVEL, this.mHwHiNetworkParmStatistics.wifiTotRtt[i8].level).setParam(HwHiNetworkParmStatistics.RTT, this.mHwHiNetworkParmStatistics.wifiTotRtt[i8].rtt);
                event_statistics.fillArrayParam(HwHiNetworkParmStatistics.MOD, event_usage);
                IMonitor.closeEventStream(event_usage);
            }
            IMonitor.sendEvent(event_statistics);
            IMonitor.closeEventStream(event_statistics);
            this.mHwHiNetworkParmStatistics.reset();
            log(false, "uploadHiNetworkParmStatistics success", new Object[0]);
        } catch (RuntimeException e) {
            log(false, "uploadHiNetworkParmStatistics RuntimeException %{public}s", e.getMessage());
        } catch (Exception e2) {
            log(false, "uploadHiNetworkparamStatistics exception", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCHRTimeDelayEvent(HwHiNetworkTimeDelayInfo timeDelayInfo) {
        log(false, "enter sendCHRTimeDelayEvent.", new Object[0]);
        if (System.currentTimeMillis() - this.mLastSndTimeDelayEventTime < WifiProCommonUtils.RECHECK_DELAYED_MS) {
            log(false, "last snd acce effect event less than one hour", new Object[0]);
            return;
        }
        IMonitor.EventStream timeDelayEventStream = IMonitor.openEventStream((int) CHR_UPLOAD_EVENT_TIME_DELAY);
        if (timeDelayInfo == null || timeDelayEventStream == null) {
            log(false, "last snd time delay event less than one hour", new Object[0]);
            return;
        }
        log(false, "timeDelayInfo.apk is %{public}s", timeDelayInfo.apk);
        log(false, "HwHiNetworkTimeDelayInfo.net is %{public}d", Short.valueOf(timeDelayInfo.net));
        log(false, "HwHiNetworkTimeDelayInfo.cellAccRtt is %{public}d", Short.valueOf(timeDelayInfo.cellAccRtt));
        log(false, "HwHiNetworkTimeDelayInfo.cellBonRtt is %{public}d", Short.valueOf(timeDelayInfo.cellBonRtt));
        log(false, "HwHiNetworkTimeDelayInfo.cellTotRtt is %{public}d", Short.valueOf(timeDelayInfo.cellTotRtt));
        log(false, "HwHiNetworkTimeDelayInfo.wifiAccRtt is %{public}d", Short.valueOf(timeDelayInfo.wifiAccRtt));
        log(false, "HwHiNetworkTimeDelayInfo.wifiBonRtt is %{public}d", Short.valueOf(timeDelayInfo.wifiBonRtt));
        log(false, "HwHiNetworkTimeDelayInfo.wifiTotRtt is %{public}d", Short.valueOf(timeDelayInfo.wifiTotRtt));
        log(false, "HwHiNetworkTimeDelayInfo.aftAcc is %{public}d", Short.valueOf(timeDelayInfo.aftAcc));
        log(false, "HwHiNetworkTimeDelayInfo.befAcc is %{public}d", Short.valueOf(timeDelayInfo.befAcc));
        timeDelayEventStream.setParam("apk", timeDelayInfo.apk).setParam("apk", timeDelayInfo.apk).setParam("net", timeDelayInfo.net).setParam("cellAccRtt", timeDelayInfo.cellAccRtt).setParam("cellBonRtt", timeDelayInfo.cellBonRtt).setParam("cellTotRtt", timeDelayInfo.cellTotRtt).setParam(HwHiNetworkTimeDelayInfo.WIFIACCRTT, timeDelayInfo.wifiAccRtt).setParam(HwHiNetworkTimeDelayInfo.WIFIBONRTT, timeDelayInfo.wifiBonRtt).setParam(HwHiNetworkTimeDelayInfo.WIFITOTRTT, timeDelayInfo.wifiTotRtt).setParam(HwHiNetworkTimeDelayInfo.AFRACC, timeDelayInfo.aftAcc).setParam(HwHiNetworkTimeDelayInfo.BEFACC, timeDelayInfo.befAcc);
        IMonitor.sendEvent(timeDelayEventStream);
        IMonitor.closeEventStream(timeDelayEventStream);
        this.mLastSndTimeDelayEventTime = System.currentTimeMillis();
        log(false, "sendCHRTimeDelayEvent success", new Object[0]);
    }

    private void sendCHRAcceEffectEvent(HwHiNetworkAcceEffectInfo acceEffectInfo) {
        log(false, "enter sendCHRAcceEffectEvent.", new Object[0]);
        if (System.currentTimeMillis() - this.mLastSndGainEventTime < WifiProCommonUtils.RECHECK_DELAYED_MS) {
            log(false, "last snd acce effect event less than one hour", new Object[0]);
            return;
        }
        IMonitor.EventStream acceEffectEventStream = IMonitor.openEventStream((int) CHR_UPLOAD_EVENT_ACCE_EFFECT);
        if (acceEffectInfo != null && acceEffectEventStream != null) {
            log(false, "acceEffectInfo.apk is %{public}s" + acceEffectInfo.apk, new Object[0]);
            log(false, "acceEffectInfo.gain is %{public}d", Short.valueOf(acceEffectInfo.gain));
            log(false, "acceEffectInfo.avgRtt is %{public}d", Short.valueOf(acceEffectInfo.avgRtt));
            log(false, "acceEffectInfo.pktLoss is %{public}d", Short.valueOf(acceEffectInfo.pktLoss));
            log(false, "acceEffectInfo.net is %{public}d", Short.valueOf(acceEffectInfo.net));
            log(false, "acceEffectInfo.lagCnt is %{public}d", Short.valueOf(acceEffectInfo.lagCnt));
            log(false, "acceEffectInfo.dualCHFlag is %{public}d", Short.valueOf(acceEffectInfo.dualCHFlag));
            log(false, "acceEffectInfo.qosFlag is %{public}d", Short.valueOf(acceEffectInfo.qosFlag));
            log(false, "acceEffectInfo.boneFlag is %{public}d", Short.valueOf(acceEffectInfo.boneFlag));
            log(false, "acceEffectInfo.dataUsg is %{public}d", Short.valueOf(acceEffectInfo.dataUsg));
            log(false, "acceEffectInfo.linkTime is %{public}d", Short.valueOf(acceEffectInfo.linkTime));
            log(false, "acceEffectInfo.code is %{public}d", Short.valueOf(acceEffectInfo.code));
            acceEffectEventStream.setParam("apk", acceEffectInfo.apk).setParam("gain", acceEffectInfo.gain).setParam(HwHiNetworkAcceEffectInfo.AVGRTT, acceEffectInfo.avgRtt).setParam(HwHiNetworkAcceEffectInfo.PKRLOSS, acceEffectInfo.pktLoss).setParam("net", acceEffectInfo.net).setParam(HwHiNetworkAcceEffectInfo.LAGCNT, acceEffectInfo.lagCnt).setParam(HwHiNetworkAcceEffectInfo.DUALCHFLAG, acceEffectInfo.dualCHFlag).setParam(HwHiNetworkAcceEffectInfo.QOSFLAG, acceEffectInfo.qosFlag).setParam(HwHiNetworkAcceEffectInfo.BONEFLAG, acceEffectInfo.boneFlag).setParam(HwHiNetworkAcceEffectInfo.DATAUSG, acceEffectInfo.dataUsg).setParam(HwHiNetworkAcceEffectInfo.LINKTIME, acceEffectInfo.linkTime).setParam(HwHiNetworkAcceEffectInfo.CODE, acceEffectInfo.code);
            IMonitor.sendEvent(acceEffectEventStream);
            IMonitor.closeEventStream(acceEffectEventStream);
            this.mLastSndGainEventTime = System.currentTimeMillis();
            log(false, "sendCHRAcceEffectEvent success", new Object[0]);
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
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null) {
            return -1;
        }
        int nType = networkInfo.getType();
        if (nType == 1) {
            return 1;
        }
        if (nType == 0) {
            return 0;
        }
        return -1;
    }

    public boolean isVpnConnected() {
        log(false, "enter isVpnConnected", new Object[0]);
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                Iterator it = Collections.list(niList).iterator();
                while (it.hasNext()) {
                    NetworkInterface intf = (NetworkInterface) it.next();
                    if (intf.isUp()) {
                        if (intf.getInterfaceAddresses().size() != 0) {
                            if (!intf.getName().equals("tun0")) {
                                if (intf.getName().equals("ppp0")) {
                                }
                            }
                            log(false, "xunyou vpn is connected", new Object[0]);
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable th) {
            log(false, "Exception happened in function isVPNConnected", new Object[0]);
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
        log(false, "monitor handleActivityChange curPkgName is %{public}s curUid is %{public}d lastPkgName is :%{public}s", curPkgName, Integer.valueOf(curUid), this.lastPkgName);
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
                HwAppTimeDetail.getInstance().startAppTime(curPkgName, curUid);
                if (this.detectSpeed == 0) {
                    this.detectSpeed = 1;
                    Message msgCycle = Message.obtain();
                    msgCycle.what = 10;
                    msgCycle.obj = curPkgName;
                    this.mHandler.sendMessageDelayed(msgCycle, 3000);
                    log(false, "start 3s thread speedtest", new Object[0]);
                }
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
                HwAppTimeDetail.getInstance().getAppUseTime(this.lastPkgName, this.lastUid);
            }
            this.lastUid = curUid;
            this.lastPkgName = curPkgName;
            this.mLastPackageNameList.clear();
            for (String item : this.curPackageNames) {
                this.mLastPackageNameList.add(item);
            }
        }
    }

    public class AppObject {
        private HiNetworkTimer mEndTimer;
        private String mKey;
        private String mPackageName;

        public AppObject(String packageName) {
            HwHiNetworkManager.log(false, " new appobject name is  %{public}s", packageName);
            this.mPackageName = packageName;
            this.mEndTimer = new HiNetworkTimer(20000, 1);
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
                HwHiNetworkManager.log(false, "contain db file err: %{public}s", e.getMessage());
            }
        }

        public void remove(String packageName) {
            this.sqliteDatabase.delete(HwHiNetworkDataBase.TABLE_USER_ACTION, "PACKAGENAME=?", new String[]{packageName});
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0045, code lost:
            if (0 == 0) goto L_0x0048;
         */
        public int contain(String packageName) {
            int isCanAcce = -1;
            Cursor cursor = null;
            try {
                cursor = this.sqliteDatabase.query(HwHiNetworkDataBase.TABLE_USER_ACTION, new String[]{"PACKAGENAME", "ISCANACCE"}, "PACKAGENAME=?", new String[]{packageName}, null, null, null);
                if (cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    isCanAcce = cursor.getInt(cursor.getColumnIndex("ISCANACCE"));
                }
            } catch (SQLException e) {
                HwHiNetworkManager.log(false, "contain db file err: %{public}s", e.getMessage());
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            cursor.close();
            HwHiNetworkManager.log(false, "contain db file packageName: %{public}s iscanacce:%{public}d", packageName, Integer.valueOf(isCanAcce));
            return isCanAcce;
        }

        public void close() {
            this.sqliteDatabase.close();
        }
    }

    public class HiNetworkTimer {
        private int mDuration;
        private int mMessageType;
        private Timer mTimer;
        private int mTimerStatus = 1;

        public HiNetworkTimer(int duration, int messageType) {
            this.mMessageType = messageType;
            if (duration < 0) {
                this.mDuration = 0;
            } else {
                this.mDuration = duration;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getTimerStatus() {
            return this.mTimerStatus;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startTimer(final String packageName) {
            HwHiNetworkManager.log(false, "startTimer, messageType is :%{public}d", Integer.valueOf(this.mMessageType));
            this.mTimerStatus = 0;
            this.mTimer = new Timer();
            this.mTimer.schedule(new TimerTask() {
                /* class com.android.server.hidata.hinetwork.HwHiNetworkManager.HiNetworkTimer.AnonymousClass1 */

                @Override // java.util.TimerTask, java.lang.Runnable
                public void run() {
                    Message message = Message.obtain();
                    message.what = HiNetworkTimer.this.mMessageType;
                    if (1 == HiNetworkTimer.this.mMessageType) {
                        if (HwHiNetworkManager.this.mIAppNetService == null || HwHiNetworkManager.this.mAidlBindFlag == 0) {
                            HwHiNetworkManager.this.mBindAidlThread = new BindAidlThread(1000, 3);
                            HwHiNetworkManager.this.mBindAidlThread.start();
                        }
                        AppObject appObject = HwHiNetworkManager.this.isInLocalAppLisByName(packageName);
                        if (appObject != null) {
                            message.obj = appObject;
                            HwHiNetworkManager.this.mHandler.sendMessageDelayed(message, 3000);
                            HwHiNetworkManager.log(false, "send message, messageType is :%{public}d", Integer.valueOf(HiNetworkTimer.this.mMessageType));
                        } else {
                            HwHiNetworkManager.log(false, "appObject is inexistence,  don't send message.", new Object[0]);
                        }
                    } else if (9 == HiNetworkTimer.this.mMessageType) {
                        HwHiNetworkManager.this.mHandler.sendMessage(message);
                        HwHiNetworkManager.log(false, "send message, messageType is :%{public}d", Integer.valueOf(HiNetworkTimer.this.mMessageType));
                    }
                    HiNetworkTimer.this.mTimerStatus = 1;
                }
            }, (long) this.mDuration);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopTimer() {
            Timer timer = this.mTimer;
            if (timer != null) {
                timer.cancel();
                this.mTimer = null;
            }
            this.mTimerStatus = 1;
        }
    }

    public class BindAidlThread extends Thread {
        private int mDuration = 0;
        private int mNum = 0;

        public BindAidlThread(int duration, int num) {
            this.mDuration = duration;
            this.mNum = num;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                try {
                    HwHiNetworkManager.log(false, "bindAidl Thread run ", new Object[0]);
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
                    HwHiNetworkManager.log(false, "Exception happened in bindAidlThread", new Object[0]);
                    return;
                }
            }
        }
    }

    private class HiNetworkAidl extends IHiNetworkManager.Stub {
        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.hidata.hinetwork.HwHiNetworkManager$HiNetworkAidl */
        /* JADX WARN: Multi-variable type inference failed */
        public HiNetworkAidl() {
            HwHiNetworkManager.log(false, "start up HiNetworkAidl", new Object[0]);
            try {
                ServiceManager.addService("hinetwork", this);
                HwHiNetworkManager.log(false, "start up HiNetworkAidl success.", new Object[0]);
            } catch (Throwable e) {
                HwHiNetworkManager.log(false, "Failure starting HiNetworkAidl %{public}s", e.getMessage());
            }
        }

        public int onDetectTimeDelayResult(String timeDelayResult) throws RemoteException {
            HwHiNetworkManager.this.mContext.enforceCallingPermission(HwHiNetworkManager.HW_SIGNATURE_OR_SYSTEM_PERMISSION, HwHiNetworkManager.TAG);
            HwHiNetworkManager.this.sendUnbindAidlMsg();
            if (timeDelayResult == null || timeDelayResult.equals("")) {
                HwHiNetworkManager.log(false, "onDetectTimeDelayResult failed, it's null.", new Object[0]);
                return -1;
            } else if (!HwHiNetworkManager.this.isJson(timeDelayResult)) {
                HwHiNetworkManager.log(false, "onDetectTimeDelayResult failed, it's not json", new Object[0]);
                return -1;
            } else {
                HwHiNetworkManager.log(false, "onDetectTimeDelayResult success %{public}s", timeDelayResult);
                float acceEffect = 0.0f;
                try {
                    long currentTime = System.currentTimeMillis();
                    JSONObject jsonObject = new JSONObject(timeDelayResult);
                    int beforeAcce = jsonObject.getInt("beforeAcce");
                    int afterAcce = jsonObject.getInt("afterAcce");
                    AppObject appObject = HwHiNetworkManager.this.isInLocalAppListByKey(jsonObject.getString("key"));
                    if (appObject != null && ((beforeAcce <= 0 && afterAcce > 0) || (beforeAcce > 0 && afterAcce > 0 && ((float) (beforeAcce - afterAcce)) / ((float) beforeAcce) > 0.1f))) {
                        synchronized (HwHiNetworkManager.this.object) {
                            HwHiNetworkManager.log(false, "can useAccelerate", new Object[0]);
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
                        int timeDelayLevel2 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt(AppProtectActionConstant.APP_PROTECT_C));
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
                        int timeDelayLevel5 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt(AwarenessInnerConstants.HAS_RELATION_FENCE_SEPARATE_KEY));
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
                        int timeDelayLevel8 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt(AppProtectActionConstant.APP_PROTECT_C));
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
                        int timeDelayLevel11 = HwHiNetworkManager.this.getTimeDelayLevel(jsonObject.getInt(AwarenessInnerConstants.HAS_RELATION_FENCE_SEPARATE_KEY));
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
                    timeDelayInfo.wifiBonRtt = (short) jsonObject.getInt(AppProtectActionConstant.APP_PROTECT_C);
                    timeDelayInfo.wifiTotRtt = (short) jsonObject.getInt("A+B'");
                    timeDelayInfo.cellAccRtt = (short) jsonObject.getInt("P+Z");
                    timeDelayInfo.cellBonRtt = (short) jsonObject.getInt(AwarenessInnerConstants.HAS_RELATION_FENCE_SEPARATE_KEY);
                    timeDelayInfo.cellTotRtt = (short) jsonObject.getInt("P+Q");
                    timeDelayInfo.aftAcc = (short) jsonObject.getInt("afterAcce");
                    timeDelayInfo.befAcc = (short) jsonObject.getInt("beforeAcce");
                    HwHiNetworkManager.this.sendCHRTimeDelayEvent(timeDelayInfo);
                    return 0;
                } catch (Exception e) {
                    HwHiNetworkManager.log(false, "Exception happened in onDetectTimeDelayResult", new Object[0]);
                    return -1;
                }
            }
        }

        public int onOpenAccelerateResult(String acceletrateResult) throws RemoteException {
            HwHiNetworkManager.this.mContext.enforceCallingPermission(HwHiNetworkManager.HW_SIGNATURE_OR_SYSTEM_PERMISSION, HwHiNetworkManager.TAG);
            if (acceletrateResult == null || acceletrateResult.equals("")) {
                HwHiNetworkManager.log(false, "onOpenAccelerateResult failed, it's null.", new Object[0]);
                return -1;
            } else if (!HwHiNetworkManager.this.isJson(acceletrateResult)) {
                HwHiNetworkManager.log(false, "onOpenAccelerateResult failed, it's not json", new Object[0]);
                return -1;
            } else {
                HwHiNetworkManager.log(false, "onOpenAccelerateResult success,acceletrateResult is %{public}s", acceletrateResult);
                try {
                    JSONObject jsonObject = new JSONObject(acceletrateResult);
                    int isOpenAccelerator = jsonObject.getInt("isOpenAccelerator");
                    int acceleratorType = jsonObject.getInt("acceleratorType");
                    if (acceleratorType == 0) {
                        if (isOpenAccelerator == 0) {
                            HwHiNetworkParmStatistics hwHiNetworkParmStatistics = HwHiNetworkManager.this.mHwHiNetworkParmStatistics;
                            hwHiNetworkParmStatistics.gameAccYes = (short) (hwHiNetworkParmStatistics.gameAccYes + 1);
                        } else if (-1 == isOpenAccelerator) {
                            HwHiNetworkParmStatistics hwHiNetworkParmStatistics2 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics;
                            hwHiNetworkParmStatistics2.gameAccNo = (short) (hwHiNetworkParmStatistics2.gameAccNo + 1);
                        }
                    } else if (1 == acceleratorType) {
                        if (isOpenAccelerator == 0) {
                            HwHiNetworkParmStatistics hwHiNetworkParmStatistics3 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics;
                            hwHiNetworkParmStatistics3.videoAccYes = (short) (hwHiNetworkParmStatistics3.videoAccYes + 1);
                        } else if (-1 == isOpenAccelerator) {
                            HwHiNetworkParmStatistics hwHiNetworkParmStatistics4 = HwHiNetworkManager.this.mHwHiNetworkParmStatistics;
                            hwHiNetworkParmStatistics4.videoAccNo = (short) (hwHiNetworkParmStatistics4.videoAccNo + 1);
                        }
                    }
                    return 0;
                } catch (Exception e) {
                    HwHiNetworkManager.log(false, "Exception happened in function onOpenAccelerateResult", new Object[0]);
                    return -1;
                }
            }
        }
    }
}
