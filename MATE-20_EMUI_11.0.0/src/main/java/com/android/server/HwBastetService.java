package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.android.bastet.IBastetListener;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HwBastetService extends SystemService {
    private static final int ACTION_BASTET_RECEIVE_IPV6_ADDRESS = 12;
    private static final int ACTION_BASTET_SET_DSCP = 10;
    private static final int ACTION_BASTET_UNSET_DSCP = 11;
    private static final int ACTION_BASTET_UPDATE_CONFIG = 9;
    private static final int ACTION_BOOT_COMPLETED = 3;
    private static final int ACTION_PACKAGE_ADDED = 4;
    private static final int ACTION_PACKAGE_CHANGED = 5;
    private static final int ACTION_PACKAGE_FIRST_LAUNCH = 7;
    private static final int ACTION_PACKAGE_REMOVED = 8;
    private static final int ACTION_PACKAGE_RESTARTED = 6;
    private static final int ACTION_SCREEN_OFF = 2;
    private static final int ACTION_SCREEN_ON = 1;
    private static final int ACTION_SEND_HEARTBEAT = 0;
    private static final int ACTION_SEND_NRT = 1;
    private static final int ACTION_UNKNOWN = 0;
    private static final String BASTET_ACTION_SEND_HEARTBEAT = "com.huawei.android.bastet.ACTION_SEND_HEARTBEAT";
    private static final String BASTET_ACTION_SEND_NRT = "com.huawei.android.bastet.ACTION_SEND_NRT";
    private static final String BASTET_DSCP_SET_ACTION = "huawei.intent.action.PUSH_HW_BASTET_SET_DSCP_ACTION";
    private static final String BASTET_DSCP_UNSET_ACTION = "huawei.intent.action.PUSH_HW_BASTET_UNSET_DSCP_ACTION";
    private static final String BASTET_SERVICE = "BastetService";
    private static final int CONNECT_INTERVAL = 5000;
    private static final int CONNECT_TIMES = 5;
    private static final String HW_BASTET_PARTNER_PATH = "/data/bastet/hw_bastet_partner.xml";
    private static final int INTENT_PACKAGE_NAME_OFFSET = 8;
    private static final int INVALID_UID = -1;
    private static final int IP_ADDRESS_MAX_CNT = 2;
    private static final int MESSAGE_BROADCAST_ACTION = 1;
    private static final int MESSAGE_CHECK_SERVICE_TIMEOUT = 2;
    private static final int MESSAGE_IP_ADDRESS_UPDATE = 4;
    private static final int MESSAGE_PARSE_ACC_APP_LIST = 3;
    private static final int MESSAGE_UNKNOWN = 0;
    private static final String PROPEL_HW_BASTET_CONFIG_ACTION = "huawei.intent.action.PUSH_HW_BASTET_CONFIG_ACTION";
    private static final String PROPEL_TYPE_EXTRA = "pushType";
    private static final int PROPEL_TYPE_PARTNER = 1;
    private static final int PROPEL_TYPE_UNKNOWN = 0;
    private static final String PROPEL_TYPE_UPDATE_BASTET_PARTNER = "hw_bastet_partner";
    private static final String PROPEL_URI_EXTRA = "uri";
    private static final String TAG = "HwBastetService";
    private ArrayList<String> mAccAppList = new ArrayList<>();
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwBastetService.AnonymousClass5 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || context == null) {
                Log.i(HwBastetService.TAG, "intent or context is null, return.");
                return;
            }
            String action = intent.getAction();
            if (HwBastetService.BASTET_ACTION_SEND_NRT.equals(action)) {
                HwBastetService.this.handleNotifyNrt();
            }
            if (HwBastetService.BASTET_ACTION_SEND_HEARTBEAT.equals(action)) {
                HwBastetService.this.handleAlarmTimeout(0);
            }
        }
    };
    private IBinder mBastetService;
    private int mConnectTimes = 0;
    private ConnectivityManager mConnectivityManager = null;
    private Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.server.HwBastetService.AnonymousClass2 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.e(HwBastetService.TAG, "Bastet service has died!");
            synchronized (HwBastetService.this) {
                if (HwBastetService.this.mBastetService != null) {
                    HwBastetService.this.mBastetService.unlinkToDeath(this, 0);
                    HwBastetService.this.mBastetService = null;
                    HwBastetService.this.mIBastetManager = null;
                }
                HwBastetService.this.mHrtAppList.clear();
                HwBastetService.this.mAccAppList.clear();
            }
            HwBastetService.this.mAlarmManager.cancel(HwBastetService.this.mNrtPendingIntent);
            HwBastetService.this.mAlarmManager.cancel(HwBastetService.this.mHeartBeatPendingIntent);
            HwBastetService.this.mConnectTimes = 0;
            HwBastetService.this.checkBastetService();
        }
    };
    private int mForegroundUid = -1;
    private Handler mHandler;
    private PendingIntent mHeartBeatPendingIntent = null;
    private ArrayList<Integer> mHrtAppList = new ArrayList<>();
    protected IBastetListener mIBastetListener = new IBastetListener.Stub() {
        /* class com.android.server.HwBastetService.AnonymousClass1 */

        public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
            Log.i(HwBastetService.TAG, "onProxyIndicateMessage: proxyId=" + proxyId + ", err=" + err + ", ext=" + ext);
            switch (err) {
                case ModelBaseService.DISCRIMINATE_RET_CODE_ERROR3 /* -23 */:
                    cancelAlarm(ext);
                    return;
                case -22:
                    HwBastetService.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) ext), HwBastetService.this.mHeartBeatPendingIntent);
                    return;
                case ModelBaseService.DISCRIMINATE_RET_CODE_ERROR1 /* -21 */:
                    HwBastetService.this.removeHrtApp(ext);
                    return;
                case ModelBaseService.TRAINMODEL_RET_ERROR_CODE_20 /* -20 */:
                    HwBastetService.this.addHrtApp(ext);
                    return;
                case ModelBaseService.TRAINMODEL_RET_ERROR_CODE_19 /* -19 */:
                default:
                    return;
                case ModelBaseService.TRAINMODEL_RET_ERROR_CODE_18 /* -18 */:
                    HwBastetService.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) ext), HwBastetService.this.mNrtPendingIntent);
                    if (HwBastetService.this.mTelephonyManager != null) {
                        HwBastetService.this.mTelephonyManager.listen(HwBastetService.this.mPhoneStateListener, 128);
                        return;
                    }
                    return;
            }
        }

        private void cancelAlarm(int operation) {
            PendingIntent pi;
            if (operation == 0) {
                pi = HwBastetService.this.mHeartBeatPendingIntent;
            } else if (operation == 1) {
                pi = HwBastetService.this.mNrtPendingIntent;
            } else {
                return;
            }
            if (pi == null) {
                Log.e(HwBastetService.TAG, "cancelAlarm opration is null");
            } else {
                HwBastetService.this.mAlarmManager.cancel(pi);
            }
        }
    };
    private IBastetManager mIBastetManager;
    private int mLastAccUid = -1;
    private int mLastHrtUid = -1;
    private LinkProperties mLinkProperties = new LinkProperties();
    private ConnectivityManager.NetworkCallback mListenNetworkCallback = null;
    private int mNetId;
    private NetworkRequest mNetworkRequestForCallback = new NetworkRequest.Builder().addTransportType(0).addCapability(12).build();
    private PendingIntent mNrtPendingIntent = null;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.server.HwBastetService.AnonymousClass4 */

        @Override // android.telephony.PhoneStateListener
        public void onDataActivity(int direction) {
            if (direction == 1 || direction == 3) {
                HwBastetService.this.handleNotifyNrt();
            }
        }
    };
    private PowerKit mPowerKit;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwBastetService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && context != null && (action = intent.getAction()) != null) {
                int actionId = parseAction(action, intent);
                Log.d(HwBastetService.TAG, "BroadcastReceiver " + action);
                if (actionId != 0) {
                    Message msg = HwBastetService.this.mHandler.obtainMessage();
                    msg.what = 1;
                    msg.arg1 = actionId;
                    switch (actionId) {
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            if (intent.getDataString() != null) {
                                msg.obj = intent.getDataString().substring(8);
                                break;
                            } else {
                                return;
                            }
                        case 9:
                            if (HwBastetService.PROPEL_TYPE_UPDATE_BASTET_PARTNER.equals(intent.getStringExtra(HwBastetService.PROPEL_TYPE_EXTRA))) {
                                msg.arg2 = 1;
                            }
                            msg.obj = intent.getStringExtra(HwBastetService.PROPEL_URI_EXTRA);
                            break;
                    }
                    HwBastetService.this.mHandler.sendMessage(msg);
                }
            }
        }

        private int parseAction(String action, Intent intent) {
            if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                return 1;
            }
            if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                HwBastetService.this.setAppBackground();
                return 2;
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwBastetService.this.initPowerKit();
                return 3;
            } else if (HwBastetService.PROPEL_HW_BASTET_CONFIG_ACTION.equals(action)) {
                return 9;
            } else {
                if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    return 4;
                }
                if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                    return 5;
                }
                if ("android.intent.action.PACKAGE_RESTARTED".equals(action)) {
                    return 6;
                }
                if ("android.intent.action.PACKAGE_FIRST_LAUNCH".equals(action)) {
                    return 7;
                }
                if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    if (intent.getDataString() == null) {
                        return 0;
                    }
                    HwBastetService.this.removeHrtApp(intent.getDataString().substring(8));
                    return 8;
                } else if (HwBastetService.BASTET_DSCP_SET_ACTION.equals(action)) {
                    return 10;
                } else {
                    if (HwBastetService.BASTET_DSCP_UNSET_ACTION.equals(action)) {
                        return 11;
                    }
                    return 0;
                }
            }
        }
    };
    private PowerKit.Sink mStateRecognitionListener;
    private TelephonyManager mTelephonyManager;
    private HandlerThread mThread;

    public HwBastetService(Context context) {
        super(context);
        Log.i(TAG, TAG);
        this.mContext = context;
    }

    public void onStart() {
        Log.i(TAG, "start HwBastetService");
        initBroadcastReceiver();
        initHandlerThread();
        initTelephonyService();
        getBastetService();
        registerHwBastetServiceNetworkCallback();
    }

    private void initGenericReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPackageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGE_RESTARTED");
        filter.addAction("android.intent.action.PACKAGE_FIRST_LAUNCH");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPropelReceiver() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(PROPEL_HW_BASTET_CONFIG_ACTION));
    }

    private void initAlarmReceiver() {
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            Log.e(TAG, "Failed to get alarm service");
            return;
        }
        this.mNrtPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(BASTET_ACTION_SEND_NRT, (Uri) null), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BASTET_ACTION_SEND_NRT);
        this.mHeartBeatPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(BASTET_ACTION_SEND_HEARTBEAT, (Uri) null), 0);
        filter.addAction(BASTET_ACTION_SEND_HEARTBEAT);
        this.mContext.registerReceiver(this.mAlarmReceiver, filter);
    }

    private void initDscpReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BASTET_DSCP_SET_ACTION);
        filter.addAction(BASTET_DSCP_UNSET_ACTION);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initTelephonyService() {
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (this.mTelephonyManager == null) {
            Log.e(TAG, "Failed to get telephony service");
        }
    }

    private void initBroadcastReceiver() {
        initGenericReceiver();
        initPackageReceiver();
        initPropelReceiver();
        initAlarmReceiver();
        initDscpReceiver();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyNrt() {
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.notifyNrtTimeout();
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to handleNotifyNrt!");
        }
        this.mAlarmManager.cancel(this.mNrtPendingIntent);
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAlarmTimeout(int operation) {
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.notifyAlarmTimeout(operation);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to handleAlarmTimeout!");
        }
    }

    private void initHandlerThread() {
        this.mThread = new HandlerThread("HwBastetHandlerThread");
        this.mThread.start();
        this.mHandler = new HwBastetHandler(this.mThread.getLooper());
    }

    private boolean getBastetService() {
        synchronized (this) {
            if (this.mBastetService == null) {
                this.mBastetService = ServiceManager.getService(BASTET_SERVICE);
                if (this.mBastetService == null) {
                    Log.e(TAG, "Failed to get bastet service!");
                    return false;
                }
                try {
                    this.mBastetService.linkToDeath(this.mDeathRecipient, 0);
                    this.mIBastetManager = IBastetManager.Stub.asInterface(this.mBastetService);
                    if (this.mIBastetManager == null || this.mIBastetListener == null || this.mIBastetManager.initHwBastetService(this.mIBastetListener) != 0) {
                        Log.e(TAG, "initHwBastetService Failed!");
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to getBastetService!");
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkBastetService() {
        boolean isConnected = false;
        if (this.mConnectTimes != 0) {
            isConnected = getBastetService();
        }
        this.mConnectTimes++;
        if (isConnected) {
            this.mConnectTimes = 0;
            return true;
        }
        if (this.mConnectTimes < 5) {
            Message timeoutMsg = this.mHandler.obtainMessage();
            timeoutMsg.what = 2;
            this.mHandler.sendMessageDelayed(timeoutMsg, 5000);
        } else {
            Log.e(TAG, "checkBastetService failed");
        }
        return isConnected;
    }

    private static InputStream getInputStreamByUri(Context context, Uri uri) {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Uri :(" + uri + ") not found");
            return null;
        }
    }

    private static File getBastetFile(String path) {
        if (path == null || "".equals(path)) {
            return null;
        }
        File file = new File(path);
        if (file.exists()) {
            Log.i(TAG, "File Path: " + path + " is already exists.");
        } else {
            try {
                File bastetDir = file.getParentFile();
                if (bastetDir.exists() || bastetDir.mkdirs()) {
                    bastetDir.setExecutable(true, false);
                    bastetDir.setReadable(true, false);
                    if (file.createNewFile()) {
                        Log.i(TAG, "File Path: " + path + " is created!");
                        file.setExecutable(false);
                        file.setReadable(true, false);
                        file.setWritable(true);
                    } else {
                        Log.i(TAG, "File Path: " + path + " is already exists.");
                    }
                } else {
                    Log.e(TAG, "Failed to create file:" + bastetDir);
                    return null;
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to prepare bastet file: " + file);
                return null;
            }
        }
        return file;
    }

    private boolean copyBastetXml(String destPath, Uri srcUri) {
        boolean ret = false;
        InputStream in = getInputStreamByUri(this.mContext, srcUri);
        File file = getBastetFile(destPath);
        if (file == null || in == null) {
            Log.w(TAG, "Cannot update bastet xml, Bastet file or inputstream is null!");
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "close inputstream error");
                }
            }
            return false;
        }
        FileWriter fw = null;
        Reader input = null;
        try {
            FileWriter fw2 = new FileWriter(file);
            Reader input2 = new InputStreamReader(in, Charset.defaultCharset());
            char[] buffer = new char[1024];
            while (true) {
                int length = input2.read(buffer);
                if (length == -1) {
                    break;
                }
                fw2.write(buffer, 0, length);
            }
            Log.i(TAG, "Copy file: /data/bastet/hw_bastet_partner.xml success.");
            ret = true;
            try {
                input2.close();
            } catch (IOException e2) {
                Log.e(TAG, "close input error");
            }
            try {
                fw2.close();
            } catch (IOException e3) {
                Log.e(TAG, "close fw error");
            }
        } catch (IOException e4) {
            Log.e(TAG, "Failed to copy file!");
            if (0 != 0) {
                try {
                    input.close();
                } catch (IOException e5) {
                    Log.e(TAG, "close input error");
                }
            }
            if (0 != 0) {
                fw.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    input.close();
                } catch (IOException e6) {
                    Log.e(TAG, "close input error");
                }
            }
            if (0 != 0) {
                try {
                    fw.close();
                } catch (IOException e7) {
                    Log.e(TAG, "close fw error");
                }
            }
            throw th;
        }
        return ret;
    }

    private boolean updateBastetConfig(int type, String strUri) {
        if (type != 1) {
            return false;
        }
        Uri uri = Uri.parse(strUri);
        Log.i(TAG, "Update Xml Uri :" + uri);
        return copyBastetXml(HW_BASTET_PARTNER_PATH, uri);
    }

    private void doBroadcastMessageImpl(int actionId) {
        Log.i(TAG, "doBroadcastMessage actionId: " + actionId);
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.broadcastReceived(actionId);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "doBroadcastMessage RemoteException.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doBroadcastMessage(Message msg) {
        int actionId = msg.arg1;
        String name = (String) msg.obj;
        switch (actionId) {
            case 1:
            case 2:
            case 3:
            case 10:
            case 11:
                doBroadcastMessageImpl(actionId);
                return;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                try {
                    getBastetService();
                    synchronized (this) {
                        if (this.mIBastetManager != null) {
                            this.mIBastetManager.packageChangedReceived(actionId, name);
                        }
                    }
                    return;
                } catch (RemoteException e) {
                    Log.e(TAG, "doBroadcastMessage RemoteException.");
                    return;
                }
            case 9:
                if (updateBastetConfig(msg.arg2, name)) {
                    doBroadcastMessageImpl(9);
                    return;
                }
                return;
            default:
                Log.e(TAG, "Unknown action id: " + actionId);
                return;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void parseAccAppList(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle == null) {
            Log.e(TAG, "bundle is NULL");
            return;
        }
        String appString = bundle.getString("applist");
        if (appString == null) {
            Log.e(TAG, "parseAccAPPList the AppList is NULL.");
            return;
        }
        String[] appList = appString.split(AwarenessInnerConstants.SEMI_COLON_KEY);
        int len = appList.length;
        for (int i = 0; i < len; i++) {
            Log.d(TAG, "parseAccAPPList AppList[" + i + "] is " + appList[i]);
            synchronized (this) {
                this.mAccAppList.add(appList[i]);
            }
        }
    }

    /* access modifiers changed from: private */
    public class HwBastetHandler extends Handler {
        public HwBastetHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                Log.e(HwBastetService.TAG, "MESSAGE_UNKNOWN");
            } else if (i == 1) {
                HwBastetService.this.doBroadcastMessage(msg);
            } else if (i == 2) {
                HwBastetService.this.checkBastetService();
            } else if (i == 3) {
                HwBastetService.this.parseAccAppList(msg);
            } else if (i == 4) {
                HwBastetService.this.doNotifyIpAddressUpdate(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addHrtApp(int uid) {
        synchronized (this) {
            boolean isFound = false;
            int len = this.mHrtAppList.size();
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                } else if (uid == this.mHrtAppList.get(i).intValue()) {
                    isFound = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!isFound) {
                Log.e(TAG, "add hrt uid: " + uid);
                this.mHrtAppList.add(Integer.valueOf(uid));
                if (uid == this.mForegroundUid) {
                    Log.i(TAG, "foreground uid: " + uid);
                    setAppActivity(0, uid);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeHrtApp(int uid) {
        synchronized (this) {
            Log.e(TAG, "uid: " + uid);
            int len = this.mHrtAppList.size();
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                } else if (uid == this.mHrtAppList.get(i).intValue()) {
                    Log.i(TAG, "remove hrt uid: " + uid);
                    this.mHrtAppList.remove(i);
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeHrtApp(String name) {
        synchronized (this) {
            Log.e(TAG, "name: " + name);
            int len = this.mHrtAppList.size();
            try {
                int uid = this.mContext.getPackageManager().getPackageUid(name, 0);
                Log.e(TAG, "len: " + len + ", name: " + name + ", uid: " + uid);
                int i = 0;
                while (true) {
                    if (i >= len) {
                        break;
                    } else if (uid == this.mHrtAppList.get(i).intValue()) {
                        Log.i(TAG, "remove hrt uid: " + uid);
                        this.mHrtAppList.remove(i);
                        break;
                    } else {
                        i++;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "removeHrtApp NameNotFoundException!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean filterHrtApp(String msg) {
        String[] splits = msg.split("\t");
        String pkgName = null;
        boolean isFound = false;
        if (splits.length > 0) {
            pkgName = splits[0];
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null || pkgName == null) {
            Log.i(TAG, "pm or package is null, return!");
            return false;
        }
        try {
            int uid = pm.getPackageUid(pkgName, 0);
            synchronized (this) {
                this.mForegroundUid = uid;
                int len = this.mHrtAppList.size();
                if (len <= 0) {
                    Log.i(TAG, "current len less then 0, need return!");
                    return false;
                }
                int i = 0;
                while (true) {
                    if (i >= len) {
                        break;
                    } else if (uid == this.mHrtAppList.get(i).intValue()) {
                        Log.i(TAG, "foreground uid: " + uid);
                        setAppActivity(0, uid);
                        isFound = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!isFound && this.mLastHrtUid > 0) {
                    Log.i(TAG, "background uid: " + this.mLastHrtUid);
                    setAppActivity(1, this.mLastHrtUid);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "filterHrtApp NameNotFoundException!");
        }
        return isFound;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void filterAccApp(String msg, boolean isFound) {
        if (msg != null) {
            String[] splits = msg.split("\t");
            String pkgName = null;
            if (splits.length > 0) {
                pkgName = splits[0];
            }
            PackageManager pm = this.mContext.getPackageManager();
            if (pm == null || pkgName == null) {
                Log.i(TAG, "pm or package is null, return!");
                return;
            }
            try {
                int uid = pm.getPackageUid(pkgName, 0);
                synchronized (this) {
                    boolean hasFind = false;
                    int len = this.mAccAppList.size();
                    if (len <= 0) {
                        Log.i(TAG, "current len less then 0, need return!");
                        return;
                    }
                    int i = 0;
                    while (true) {
                        if (i >= len) {
                            break;
                        } else if (this.mAccAppList.get(i).equals(pkgName)) {
                            Log.i(TAG, "filterAccApp foreground uid: " + uid);
                            setAppActivity(0, uid);
                            this.mLastAccUid = uid;
                            hasFind = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (!hasFind && !isFound && this.mLastAccUid > 0) {
                        Log.i(TAG, "filterAccApp background uid: " + this.mLastAccUid);
                        setAppActivity(1, this.mLastAccUid);
                        this.mLastAccUid = -1;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "filterAccApp NameNotFoundException!");
            }
        }
    }

    private void setAppActivity(int activity, int uid) {
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.hrtAppActivity(activity, uid);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "setAppActivity RemoteException!");
        }
        if (activity == 0) {
            this.mLastHrtUid = uid;
        }
        if (activity == 1) {
            this.mLastHrtUid = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAppBackground() {
        synchronized (this) {
            if (this.mLastHrtUid > 0) {
                Log.i(TAG, "background uid: " + this.mLastHrtUid);
                setAppActivity(1, this.mLastHrtUid);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initPowerKit() {
        new Thread(new Runnable() {
            /* class com.android.server.HwBastetService.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                for (int i = 0; i < 10; i++) {
                    HwBastetService.this.mPowerKit = PowerKit.getInstance();
                    if (HwBastetService.this.mPowerKit == null) {
                        Log.i(HwBastetService.TAG, "get PowerKit instance failed! tryTimes:" + i);
                        SystemClock.sleep(5000);
                    } else {
                        try {
                            Log.i(HwBastetService.TAG, "get PowerKit instance success!");
                            HwBastetService.this.mStateRecognitionListener = new StateRecognitionListener();
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT);
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_EBOOK_FRONT);
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_GALLERY_FRONT);
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_OFFICE_FRONT);
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT);
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT);
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
                            HwBastetService.this.mPowerKit.enableStateEvent(HwBastetService.this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_MMS_FRONT);
                            return;
                        } catch (RemoteException e) {
                            Log.i(HwBastetService.TAG, "VBR PG Exception e: initialize powerkit error!");
                            return;
                        }
                    }
                }
            }
        }).start();
    }

    private class StateRecognitionListener implements PowerKit.Sink {
        private StateRecognitionListener() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if (eventType == 1 && pkg != null) {
                HwBastetService.this.filterAccApp(pkg, HwBastetService.this.filterHrtApp(pkg));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLinkProperties(LinkProperties lp, Network network) {
        Log.i(TAG, "updateLinkProperties");
        if (!(lp == null || this.mConnectivityManager == null || network == null)) {
            this.mLinkProperties = lp;
            this.mNetId = network.netId;
            Log.i(TAG, "mNetId:" + this.mNetId);
            String interfaceName = this.mLinkProperties.getInterfaceName();
            if (interfaceName == null || "".equals(interfaceName)) {
                Log.i(TAG, "mInterfaceName is null or empty");
                return;
            }
            new ArrayList();
            List<InetAddress> ipAddressList = this.mLinkProperties.getAddresses();
            if (ipAddressList == null || ipAddressList.size() < 1) {
                Log.i(TAG, "ipAddressList not contain values.");
                return;
            }
            Log.i(TAG, "ipAddressList.size() = " + ipAddressList.size());
            int ipAddressCnt = ipAddressList.size() > 2 ? 2 : ipAddressList.size();
            String[] ipAddresses = new String[2];
            for (int i = 0; i < ipAddressCnt; i++) {
                String temp = ipAddressList.get(i).toString();
                ipAddresses[i] = temp.substring(1, temp.length());
            }
            Message msg = this.mHandler.obtainMessage();
            msg.what = 4;
            Bundle bundle = new Bundle();
            if (ipAddressList.size() != 1) {
                int index = 0;
                while (true) {
                    if (index >= ipAddressCnt) {
                        break;
                    } else if (isIpv6Address(ipAddressList.get(index))) {
                        bundle.putString("ipAddress", ipAddresses[index]);
                        break;
                    } else {
                        index++;
                    }
                }
                if (index == ipAddressCnt) {
                    Log.i(TAG, "have several ip addresses, but not found ipv6 address!");
                    return;
                }
            } else if (isIpv6Address(ipAddressList.get(0))) {
                bundle.putString("ipAddress", ipAddresses[0]);
            } else {
                Log.i(TAG, "ipv4 only!");
                return;
            }
            bundle.putString("iface", interfaceName);
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: package-private */
    public class HwBastetServiceNetworkCallback extends ConnectivityManager.NetworkCallback {
        HwBastetServiceNetworkCallback() {
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            Log.i(HwBastetService.TAG, "NetworkCallback onAvailable");
            if (HwBastetService.this.mConnectivityManager != null) {
                HwBastetService.this.updateLinkProperties(HwBastetService.this.mConnectivityManager.getLinkProperties(network), network);
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            Log.i(HwBastetService.TAG, "NetworkCallback onLinkPropertiesChanged");
            HwBastetService.this.updateLinkProperties(linkProperties, network);
        }
    }

    private void registerHwBastetServiceNetworkCallback() {
        Log.i(TAG, "registerHwBastetServiceNetworkCallback");
        if (this.mConnectivityManager == null) {
            Object connectivityManager = this.mContext.getSystemService("connectivity");
            if (connectivityManager == null) {
                Log.d(TAG, "Object connectivityManager is null");
            } else if (connectivityManager instanceof ConnectivityManager) {
                this.mConnectivityManager = (ConnectivityManager) connectivityManager;
                if (this.mConnectivityManager != null) {
                    this.mListenNetworkCallback = new HwBastetServiceNetworkCallback();
                    this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequestForCallback, this.mListenNetworkCallback, this.mHandler);
                }
            } else {
                Log.d(TAG, "mConnectivityManager is not instanceof ConnectivityManager!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doNotifyIpAddressUpdate(Message msg) {
        int actionId = msg.arg1;
        Bundle bundle = msg.getData();
        if (bundle == null) {
            Log.e(TAG, "bundle is NULL");
            return;
        }
        String ipv6Address = bundle.getString("ipAddress");
        if (ipv6Address == null) {
            Log.e(TAG, "doNotifyIpAddressUpdate the ipv6Address is empty");
            return;
        }
        String interfaceName = bundle.getString("iface");
        if (interfaceName == null) {
            Log.e(TAG, "doNotifyIpAddressUpdate the interfaceName is empty");
            return;
        }
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.ipv6AddressUpdateReceived(actionId, interfaceName, ipv6Address);
                }
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Exception: update ipv6 address error!");
        }
    }

    private boolean isIpv6Address(InetAddress address) {
        if (address instanceof Inet6Address) {
            return true;
        }
        return false;
    }
}
