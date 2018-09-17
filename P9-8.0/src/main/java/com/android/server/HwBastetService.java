package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.biometrics.fingerprint.V2_1.RequestStatus;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.android.bastet.IBastetListener;
import com.huawei.android.bastet.IBastetListener.Stub;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class HwBastetService extends SystemService {
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
    private static final int MESSAGE_BROADCAST_ACTION = 1;
    private static final int MESSAGE_CHECK_SERVICE_TIMEOUT = 2;
    private static final int MESSAGE_PARSE_ACC_APP_LIST = 3;
    private static final int MESSAGE_UNKNOWN = 0;
    private static final String PROPEL_HW_BASTET_CONFIG_ACTION = "huawei.intent.action.PUSH_HW_BASTET_CONFIG_ACTION";
    private static final String PROPEL_TYPE_EXTRA = "pushType";
    private static final int PROPEL_TYPE_PARTNER = 1;
    private static final int PROPEL_TYPE_UNKNOWN = 0;
    private static final String PROPEL_TYPE_UPDATE_BASTET_PARTNER = "hw_bastet_partner";
    private static final String PROPEL_URI_EXTRA = "uri";
    private static final String TAG = "HwBastetService";
    private ArrayList<String> mAccAppList = new ArrayList();
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(HwBastetService.BASTET_ACTION_SEND_NRT)) {
                    HwBastetService.this.handleNotifyNrt();
                } else if (action.equals(HwBastetService.BASTET_ACTION_SEND_HEARTBEAT)) {
                    HwBastetService.this.handleAlarmTimeout(0);
                }
            }
        }
    };
    private IBinder mBastetService;
    private int mConnectTimes = 0;
    private Context mContext;
    private DeathRecipient mDeathRecipient = new DeathRecipient() {
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
    private ArrayList<Integer> mHrtAppList = new ArrayList();
    protected IBastetListener mIBastetListener = new Stub() {
        public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
            Log.e(HwBastetService.TAG, "onProxyIndicateMessage: proxyId=" + proxyId + ", err=" + err + ", ext=" + ext);
            switch (err) {
                case -23:
                    cancelAlarm(ext);
                    return;
                case RequestStatus.SYS_EINVAL /*-22*/:
                    HwBastetService.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) ext), HwBastetService.this.mHeartBeatPendingIntent);
                    return;
                case -21:
                    HwBastetService.this.removeHrtApp(ext);
                    return;
                case -20:
                    HwBastetService.this.addHrtApp(ext);
                    return;
                case -18:
                    HwBastetService.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) ext), HwBastetService.this.mNrtPendingIntent);
                    if (HwBastetService.this.mTelephonyManager != null) {
                        HwBastetService.this.mTelephonyManager.listen(HwBastetService.this.mPhoneStateListener, 128);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private void cancelAlarm(int operation) {
            PendingIntent pi;
            switch (operation) {
                case 0:
                    pi = HwBastetService.this.mHeartBeatPendingIntent;
                    break;
                case 1:
                    pi = HwBastetService.this.mNrtPendingIntent;
                    break;
                default:
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
    private PendingIntent mNrtPendingIntent = null;
    private PGPlug mPGPlug;
    private PgEventProcesser mPgEventProcesser = new PgEventProcesser(this, null);
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onDataActivity(int direction) {
            switch (direction) {
                case 1:
                case 3:
                    HwBastetService.this.handleNotifyNrt();
                    return;
                default:
                    return;
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
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
                            msg.obj = intent.getDataString().substring(8);
                            break;
                        case 9:
                            String value = intent.getStringExtra(HwBastetService.PROPEL_TYPE_EXTRA);
                            if (value != null && value.equals(HwBastetService.PROPEL_TYPE_UPDATE_BASTET_PARTNER)) {
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
            if (action.equals("android.intent.action.SCREEN_ON")) {
                return 1;
            }
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                HwBastetService.this.setAppBackground();
                return 2;
            } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                HwBastetService.this.initPgPlugThread();
                return 3;
            } else if (action.equals(HwBastetService.PROPEL_HW_BASTET_CONFIG_ACTION)) {
                return 9;
            } else {
                if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                    return 4;
                }
                if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                    return 5;
                }
                if (action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                    return 6;
                }
                if (action.equals("android.intent.action.PACKAGE_FIRST_LAUNCH")) {
                    return 7;
                }
                if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    HwBastetService.this.removeHrtApp(intent.getDataString().substring(8));
                    return 8;
                } else if (action.equals(HwBastetService.BASTET_DSCP_SET_ACTION)) {
                    return 10;
                } else {
                    if (action.equals(HwBastetService.BASTET_DSCP_UNSET_ACTION)) {
                        return 11;
                    }
                    return 0;
                }
            }
        }
    };
    private TelephonyManager mTelephonyManager;
    private HandlerThread mThread;

    private class HwBastetHandler extends Handler {
        public HwBastetHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.e(HwBastetService.TAG, "MESSAGE_UNKNOWN");
                    return;
                case 1:
                    HwBastetService.this.doBroadcastMessage(msg);
                    return;
                case 2:
                    HwBastetService.this.checkBastetService();
                    return;
                case 3:
                    HwBastetService.this.parseAccAppList(msg);
                    return;
                default:
                    return;
            }
        }
    }

    private class PgEventProcesser implements IPGPlugCallbacks {
        /* synthetic */ PgEventProcesser(HwBastetService this$0, PgEventProcesser -this1) {
            this();
        }

        private PgEventProcesser() {
        }

        public void onDaemonConnected() {
        }

        public void onConnectedTimeout() {
        }

        public boolean onEvent(int actionID, String msg) {
            if (PGAction.checkActionType(actionID) == 1 && PGAction.checkActionFlag(actionID) == 3) {
                HwBastetService.this.filterAccApp(msg, HwBastetService.this.filterHrtApp(msg));
            }
            return true;
        }
    }

    public void onStart() {
        Log.i(TAG, "start HwBastetService");
        initBroadcastReceiver();
        initHandlerThread();
        initTelephonyService();
        getBastetService();
    }

    public HwBastetService(Context context) {
        super(context);
        Log.i(TAG, TAG);
        this.mContext = context;
    }

    private void initGenericReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
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
        this.mNrtPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(BASTET_ACTION_SEND_NRT, null), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BASTET_ACTION_SEND_NRT);
        this.mHeartBeatPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(BASTET_ACTION_SEND_HEARTBEAT, null), 0);
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

    private void handleNotifyNrt() {
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.notifyNrtTimeout();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mAlarmManager.cancel(this.mNrtPendingIntent);
        if (this.mTelephonyManager != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        }
    }

    private void handleAlarmTimeout(int operation) {
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.notifyAlarmTimeout(operation);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
                    if (this.mIBastetManager.initHwBastetService(this.mIBastetListener) != 0) {
                        Log.e(TAG, "initHwBastetService Failed!");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private boolean checkBastetService() {
        boolean isConnected = false;
        if (this.mConnectTimes != 0) {
            isConnected = getBastetService();
        }
        this.mConnectTimes++;
        if (!isConnected && this.mConnectTimes < 5) {
            Message timeoutMsg = this.mHandler.obtainMessage();
            timeoutMsg.what = 2;
            this.mHandler.sendMessageDelayed(timeoutMsg, 5000);
        } else if (isConnected) {
            this.mConnectTimes = 0;
        } else if (this.mConnectTimes >= 5) {
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
        if (path == null || path.length() == 0) {
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
                Log.w(TAG, "Unable to prepare bastet file: " + file, e);
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x005a A:{SYNTHETIC, Splitter: B:27:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x005f A:{SYNTHETIC, Splitter: B:30:0x005f} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00a9 A:{SYNTHETIC, Splitter: B:53:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00ae A:{SYNTHETIC, Splitter: B:56:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00a9 A:{SYNTHETIC, Splitter: B:53:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00ae A:{SYNTHETIC, Splitter: B:56:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005a A:{SYNTHETIC, Splitter: B:27:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x005f A:{SYNTHETIC, Splitter: B:30:0x005f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean copyBastetXml(String destPath, Uri srcUri) {
        IOException e;
        Throwable th;
        boolean ret = false;
        InputStream in = getInputStreamByUri(this.mContext, srcUri);
        File file = getBastetFile(destPath);
        if (file == null || in == null) {
            Log.w(TAG, "Cannot update bastet xml, Bastet file or inputstream is null!");
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                    Log.e(TAG, "close inputstream error");
                }
            }
            return false;
        }
        FileWriter fw = null;
        Reader input = null;
        try {
            FileWriter fw2 = new FileWriter(file);
            try {
                Reader input2 = new InputStreamReader(in, Charset.defaultCharset());
                try {
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
                    if (input2 != null) {
                        try {
                            input2.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "close input error");
                        }
                    }
                    if (fw2 != null) {
                        try {
                            fw2.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "close fw error");
                        }
                    }
                    input = input2;
                } catch (IOException e5) {
                    e = e5;
                    input = input2;
                    fw = fw2;
                    try {
                        Log.e(TAG, "Failed to copy file!", e);
                        e.printStackTrace();
                        if (input != null) {
                        }
                        if (fw != null) {
                        }
                        return ret;
                    } catch (Throwable th2) {
                        th = th2;
                        if (input != null) {
                        }
                        if (fw != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    input = input2;
                    fw = fw2;
                    if (input != null) {
                    }
                    if (fw != null) {
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e = e6;
                fw = fw2;
                Log.e(TAG, "Failed to copy file!", e);
                e.printStackTrace();
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e7) {
                        Log.e(TAG, "close input error");
                    }
                }
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e8) {
                        Log.e(TAG, "close fw error");
                    }
                }
                return ret;
            } catch (Throwable th4) {
                th = th4;
                fw = fw2;
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e9) {
                        Log.e(TAG, "close input error");
                    }
                }
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e10) {
                        Log.e(TAG, "close fw error");
                    }
                }
                throw th;
            }
        } catch (IOException e11) {
            e = e11;
            Log.e(TAG, "Failed to copy file!", e);
            e.printStackTrace();
            if (input != null) {
            }
            if (fw != null) {
            }
            return ret;
        }
        return ret;
    }

    private boolean updateBastetConfig(int type, String strUri) {
        if (1 != type) {
            return false;
        }
        Uri uri = Uri.parse(strUri);
        Log.i(TAG, "Update Xml Uri :" + uri);
        return copyBastetXml(HW_BASTET_PARTNER_PATH, uri);
    }

    private void doBroadcastMessage(Message msg) {
        int actionId = msg.arg1;
        String name = msg.obj;
        switch (actionId) {
            case 1:
            case 2:
            case 3:
            case 10:
            case 11:
                break;
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
                    e.printStackTrace();
                    return;
                }
            case 9:
                if (!updateBastetConfig(msg.arg2, name)) {
                    return;
                }
                break;
            default:
                Log.e(TAG, "Unknown action id: " + actionId);
                return;
        }
        try {
            getBastetService();
            synchronized (this) {
                if (this.mIBastetManager != null) {
                    this.mIBastetManager.broadcastReceived(actionId);
                }
            }
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
    }

    private void parseAccAppList(Message msg) {
        String appString = msg.getData().getString("applist");
        if (appString == null) {
            Log.e(TAG, "parseAccAPPList the AppList is NULL.");
            return;
        }
        String[] appList = appString.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        int n = appList.length;
        for (int i = 0; i < n; i++) {
            Log.d(TAG, "parseAccAPPList AppList[" + i + "] is " + appList[i]);
            synchronized (this) {
                this.mAccAppList.add(appList[i]);
            }
        }
    }

    private void addHrtApp(int uid) {
        synchronized (this) {
            boolean found = false;
            int len = this.mHrtAppList.size();
            for (int i = 0; i < len; i++) {
                if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Log.e(TAG, "add hrt uid: " + uid);
                this.mHrtAppList.add(Integer.valueOf(uid));
                if (uid == this.mForegroundUid) {
                    Log.i(TAG, "foreground uid: " + uid);
                    setAppActivity(0, uid);
                }
            }
        }
    }

    private void removeHrtApp(int uid) {
        synchronized (this) {
            Log.e(TAG, "uid: " + uid);
            int len = this.mHrtAppList.size();
            for (int i = 0; i < len; i++) {
                if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                    Log.i(TAG, "remove hrt uid: " + uid);
                    this.mHrtAppList.remove(i);
                    break;
                }
            }
        }
    }

    private void removeHrtApp(String name) {
        synchronized (this) {
            Log.e(TAG, "name: " + name);
            int len = this.mHrtAppList.size();
            try {
                int uid = this.mContext.getPackageManager().getPackageUid(name, 0);
                Log.e(TAG, "len: " + len + ", name: " + name + ", uid: " + uid);
                for (int i = 0; i < len; i++) {
                    if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                        Log.i(TAG, "remove hrt uid: " + uid);
                        this.mHrtAppList.remove(i);
                        break;
                    }
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private boolean filterHrtApp(String msg) {
        String[] splits = msg.split("\t");
        String pkgName = null;
        boolean found = false;
        try {
            if (splits.length > 0) {
                pkgName = splits[0];
            }
        } catch (Exception e) {
        }
        try {
            int uid = this.mContext.getPackageManager().getPackageUid(pkgName, 0);
            synchronized (this) {
                this.mForegroundUid = uid;
                int len = this.mHrtAppList.size();
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                            Log.i(TAG, "foreground uid: " + uid);
                            setAppActivity(0, uid);
                            found = true;
                            break;
                        }
                    }
                    if (!found && this.mLastHrtUid > 0) {
                        Log.i(TAG, "background uid: " + this.mLastHrtUid);
                        setAppActivity(1, this.mLastHrtUid);
                    }
                }
            }
        } catch (NameNotFoundException e2) {
            e2.printStackTrace();
        }
        return found;
    }

    private void filterAccApp(String msg, boolean isFound) {
        if (msg != null) {
            String[] splits = msg.split("\t");
            String pkgName = null;
            if (splits.length > 0) {
                pkgName = splits[0];
            }
            try {
                int uid = this.mContext.getPackageManager().getPackageUid(pkgName, 0);
                synchronized (this) {
                    boolean found = false;
                    int len = this.mAccAppList.size();
                    if (len > 0) {
                        for (int i = 0; i < len; i++) {
                            if (((String) this.mAccAppList.get(i)).equals(pkgName)) {
                                Log.i(TAG, "filterAccApp foreground uid: " + uid);
                                setAppActivity(0, uid);
                                this.mLastAccUid = uid;
                                found = true;
                                break;
                            }
                        }
                        if (!(found || (isFound ^ 1) == 0 || this.mLastAccUid <= 0)) {
                            Log.i(TAG, "filterAccApp background uid: " + this.mLastAccUid);
                            setAppActivity(1, this.mLastAccUid);
                            this.mLastAccUid = -1;
                        }
                    }
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
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
            if (activity == 0) {
                this.mLastHrtUid = uid;
            } else if (activity == 1) {
                this.mLastHrtUid = -1;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setAppBackground() {
        synchronized (this) {
            if (this.mLastHrtUid > 0) {
                Log.i(TAG, "background uid: " + this.mLastHrtUid);
                setAppActivity(1, this.mLastHrtUid);
            }
        }
    }

    private void initPgPlugThread() {
        this.mPGPlug = new PGPlug(this.mPgEventProcesser, TAG);
        new Thread(this.mPGPlug, TAG).start();
    }
}
