package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.huawei.android.bastet.IBastetListener;
import com.huawei.android.bastet.IBastetListener.Stub;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
    private static final String BASTET_ACC_APP_LIST_ACTION = "huawei.intent.action.ACC_APP_LIST_ACTION";
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
    private ArrayList<String> mAccAppList;
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mAlarmReceiver;
    private IBinder mBastetService;
    private int mConnectTimes;
    private Context mContext;
    private DeathRecipient mDeathRecipient;
    private int mForegroundUid;
    private Handler mHandler;
    private PendingIntent mHeartBeatPendingIntent;
    private ArrayList<Integer> mHrtAppList;
    protected IBastetListener mIBastetListener;
    private IBastetManager mIBastetManager;
    private int mLastAccUid;
    private int mLastHrtUid;
    private PendingIntent mNrtPendingIntent;
    private PGPlug mPGPlug;
    private PgEventProcesser mPgEventProcesser;
    private PhoneStateListener mPhoneStateListener;
    private final BroadcastReceiver mReceiver;
    private TelephonyManager mTelephonyManager;
    private HandlerThread mThread;

    private class HwBastetHandler extends Handler {
        public HwBastetHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwBastetService.PROPEL_TYPE_UNKNOWN /*0*/:
                    Log.e(HwBastetService.TAG, "MESSAGE_UNKNOWN");
                case HwBastetService.PROPEL_TYPE_PARTNER /*1*/:
                    HwBastetService.this.doBroadcastMessage(msg);
                case HwBastetService.MESSAGE_CHECK_SERVICE_TIMEOUT /*2*/:
                    HwBastetService.this.checkBastetService();
                case HwBastetService.MESSAGE_PARSE_ACC_APP_LIST /*3*/:
                    HwBastetService.this.parseAccAppList(msg);
                default:
            }
        }
    }

    private class PgEventProcesser implements IPGPlugCallbacks {
        private PgEventProcesser() {
        }

        public void onDaemonConnected() {
        }

        public void onConnectedTimeout() {
        }

        public boolean onEvent(int actionID, String msg) {
            if (PGAction.checkActionType(actionID) == HwBastetService.PROPEL_TYPE_PARTNER && PGAction.checkActionFlag(actionID) == HwBastetService.MESSAGE_PARSE_ACC_APP_LIST) {
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
        this.mConnectTimes = PROPEL_TYPE_UNKNOWN;
        this.mNrtPendingIntent = null;
        this.mHeartBeatPendingIntent = null;
        this.mPgEventProcesser = new PgEventProcesser();
        this.mHrtAppList = new ArrayList();
        this.mAccAppList = new ArrayList();
        this.mLastHrtUid = INVALID_UID;
        this.mForegroundUid = INVALID_UID;
        this.mLastAccUid = INVALID_UID;
        this.mIBastetListener = new Stub() {
            public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
                Log.e(HwBastetService.TAG, "onProxyIndicateMessage: proxyId=" + proxyId + ", err=" + err + ", ext=" + ext);
                switch (err) {
                    case -23:
                        cancelAlarm(ext);
                    case -22:
                        HwBastetService.this.mAlarmManager.setExact(HwBastetService.MESSAGE_CHECK_SERVICE_TIMEOUT, SystemClock.elapsedRealtime() + ((long) ext), HwBastetService.this.mHeartBeatPendingIntent);
                    case -21:
                        HwBastetService.this.removeHrtApp(ext);
                    case -20:
                        HwBastetService.this.addHrtApp(ext);
                    case -18:
                        HwBastetService.this.mAlarmManager.setExact(HwBastetService.MESSAGE_CHECK_SERVICE_TIMEOUT, SystemClock.elapsedRealtime() + ((long) ext), HwBastetService.this.mNrtPendingIntent);
                        if (HwBastetService.this.mTelephonyManager != null) {
                            HwBastetService.this.mTelephonyManager.listen(HwBastetService.this.mPhoneStateListener, HwSecDiagnoseConstant.BIT_VERIFYBOOT);
                        }
                    default:
                }
            }

            private void cancelAlarm(int operation) {
                PendingIntent pi;
                switch (operation) {
                    case HwBastetService.PROPEL_TYPE_UNKNOWN /*0*/:
                        pi = HwBastetService.this.mHeartBeatPendingIntent;
                        break;
                    case HwBastetService.PROPEL_TYPE_PARTNER /*1*/:
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
        this.mDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                Log.e(HwBastetService.TAG, "Bastet service has died!");
                synchronized (HwBastetService.this) {
                    if (HwBastetService.this.mBastetService != null) {
                        HwBastetService.this.mBastetService.unlinkToDeath(this, HwBastetService.PROPEL_TYPE_UNKNOWN);
                        HwBastetService.this.mBastetService = null;
                        HwBastetService.this.mIBastetManager = null;
                    }
                    HwBastetService.this.mHrtAppList.clear();
                    HwBastetService.this.mAccAppList.clear();
                }
                HwBastetService.this.mAlarmManager.cancel(HwBastetService.this.mNrtPendingIntent);
                HwBastetService.this.mAlarmManager.cancel(HwBastetService.this.mHeartBeatPendingIntent);
                HwBastetService.this.mConnectTimes = HwBastetService.PROPEL_TYPE_UNKNOWN;
                HwBastetService.this.checkBastetService();
            }
        };
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onDataActivity(int direction) {
                switch (direction) {
                    case HwBastetService.PROPEL_TYPE_PARTNER /*1*/:
                    case HwBastetService.MESSAGE_PARSE_ACC_APP_LIST /*3*/:
                        HwBastetService.this.handleNotifyNrt();
                    default:
                }
            }
        };
        this.mAlarmReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals(HwBastetService.BASTET_ACTION_SEND_NRT)) {
                        HwBastetService.this.handleNotifyNrt();
                    } else if (action.equals(HwBastetService.BASTET_ACTION_SEND_HEARTBEAT)) {
                        HwBastetService.this.handleAlarmTimeout(HwBastetService.PROPEL_TYPE_UNKNOWN);
                    }
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int actionId = HwBastetService.PROPEL_TYPE_UNKNOWN;
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        actionId = HwBastetService.PROPEL_TYPE_PARTNER;
                    } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                        HwBastetService.this.setAppBackground();
                        actionId = HwBastetService.MESSAGE_CHECK_SERVICE_TIMEOUT;
                    } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        HwBastetService.this.initPgPlugThread();
                        actionId = HwBastetService.MESSAGE_PARSE_ACC_APP_LIST;
                    } else if (action.equals(HwBastetService.PROPEL_HW_BASTET_CONFIG_ACTION)) {
                        actionId = HwBastetService.ACTION_BASTET_UPDATE_CONFIG;
                    } else if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                        actionId = HwBastetService.ACTION_PACKAGE_ADDED;
                    } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                        actionId = HwBastetService.CONNECT_TIMES;
                    } else if (action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                        actionId = HwBastetService.ACTION_PACKAGE_RESTARTED;
                    } else if (action.equals("android.intent.action.PACKAGE_FIRST_LAUNCH")) {
                        actionId = HwBastetService.ACTION_PACKAGE_FIRST_LAUNCH;
                    } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        HwBastetService.this.removeHrtApp(intent.getDataString().substring(HwBastetService.INTENT_PACKAGE_NAME_OFFSET));
                        actionId = HwBastetService.INTENT_PACKAGE_NAME_OFFSET;
                    } else if (action.equals(HwBastetService.BASTET_DSCP_SET_ACTION)) {
                        actionId = HwBastetService.ACTION_BASTET_SET_DSCP;
                    } else if (action.equals(HwBastetService.BASTET_DSCP_UNSET_ACTION)) {
                        actionId = HwBastetService.ACTION_BASTET_UNSET_DSCP;
                    }
                    Log.d(HwBastetService.TAG, "BroadcastReceiver " + action);
                    if (actionId != 0) {
                        Message msg = HwBastetService.this.mHandler.obtainMessage();
                        msg.what = HwBastetService.PROPEL_TYPE_PARTNER;
                        msg.arg1 = actionId;
                        switch (actionId) {
                            case HwBastetService.ACTION_PACKAGE_ADDED /*4*/:
                            case HwBastetService.CONNECT_TIMES /*5*/:
                            case HwBastetService.ACTION_PACKAGE_RESTARTED /*6*/:
                            case HwBastetService.ACTION_PACKAGE_FIRST_LAUNCH /*7*/:
                            case HwBastetService.INTENT_PACKAGE_NAME_OFFSET /*8*/:
                                msg.obj = intent.getDataString().substring(HwBastetService.INTENT_PACKAGE_NAME_OFFSET);
                                break;
                            case HwBastetService.ACTION_BASTET_UPDATE_CONFIG /*9*/:
                                String value = intent.getStringExtra(HwBastetService.PROPEL_TYPE_EXTRA);
                                if (value != null && value.equals(HwBastetService.PROPEL_TYPE_UPDATE_BASTET_PARTNER)) {
                                    msg.arg2 = HwBastetService.PROPEL_TYPE_PARTNER;
                                }
                                msg.obj = intent.getStringExtra(HwBastetService.PROPEL_URI_EXTRA);
                                break;
                        }
                        HwBastetService.this.mHandler.sendMessage(msg);
                    }
                }
            }
        };
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
        filter.addDataScheme(ControlScope.PACKAGE_ELEMENT_KEY);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPropelReceiver() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(PROPEL_HW_BASTET_CONFIG_ACTION));
    }

    private void initAccPackageListReceiver() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(BASTET_ACC_APP_LIST_ACTION));
    }

    private void initAlarmReceiver() {
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            Log.e(TAG, "Failed to get alarm service");
            return;
        }
        this.mNrtPendingIntent = PendingIntent.getBroadcast(this.mContext, PROPEL_TYPE_UNKNOWN, new Intent(BASTET_ACTION_SEND_NRT, null), PROPEL_TYPE_UNKNOWN);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BASTET_ACTION_SEND_NRT);
        this.mHeartBeatPendingIntent = PendingIntent.getBroadcast(this.mContext, PROPEL_TYPE_UNKNOWN, new Intent(BASTET_ACTION_SEND_HEARTBEAT, null), PROPEL_TYPE_UNKNOWN);
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
        initAccPackageListReceiver();
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
            this.mTelephonyManager.listen(this.mPhoneStateListener, PROPEL_TYPE_UNKNOWN);
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
                    this.mBastetService.linkToDeath(this.mDeathRecipient, PROPEL_TYPE_UNKNOWN);
                    this.mIBastetManager = IBastetManager.Stub.asInterface(this.mBastetService);
                    if (this.mIBastetManager.initHwBastetService(this.mIBastetListener) != 0) {
                        Log.e(TAG, "initHwBastetService Failed!");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

    private boolean checkBastetService() {
        boolean isConnected = false;
        if (this.mConnectTimes != 0) {
            isConnected = getBastetService();
        }
        this.mConnectTimes += PROPEL_TYPE_PARTNER;
        if (!isConnected && this.mConnectTimes < CONNECT_TIMES) {
            Message timeoutMsg = this.mHandler.obtainMessage();
            timeoutMsg.what = MESSAGE_CHECK_SERVICE_TIMEOUT;
            this.mHandler.sendMessageDelayed(timeoutMsg, 5000);
        } else if (isConnected) {
            this.mConnectTimes = PROPEL_TYPE_UNKNOWN;
        } else if (this.mConnectTimes >= CONNECT_TIMES) {
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
        FileWriter fileWriter = null;
        Reader input = null;
        try {
            FileWriter fw = new FileWriter(file);
            try {
                Reader input2 = new InputStreamReader(in, Charset.defaultCharset());
                try {
                    char[] buffer = new char[HwGlobalActionsData.FLAG_SILENTMODE_NORMAL];
                    while (true) {
                        int length = input2.read(buffer);
                        if (length == INVALID_UID) {
                            break;
                        }
                        fw.write(buffer, PROPEL_TYPE_UNKNOWN, length);
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
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "close fw error");
                        }
                    }
                    input = input2;
                } catch (IOException e5) {
                    e = e5;
                    input = input2;
                    fileWriter = fw;
                    try {
                        Log.e(TAG, "Failed to copy file!", e);
                        e.printStackTrace();
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException e6) {
                                Log.e(TAG, "close input error");
                            }
                        }
                        if (fileWriter != null) {
                            try {
                                fileWriter.close();
                            } catch (IOException e7) {
                                Log.e(TAG, "close fw error");
                            }
                        }
                        return ret;
                    } catch (Throwable th2) {
                        th = th2;
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException e8) {
                                Log.e(TAG, "close input error");
                            }
                        }
                        if (fileWriter != null) {
                            try {
                                fileWriter.close();
                            } catch (IOException e9) {
                                Log.e(TAG, "close fw error");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    input = input2;
                    fileWriter = fw;
                    if (input != null) {
                        input.close();
                    }
                    if (fileWriter != null) {
                        fileWriter.close();
                    }
                    throw th;
                }
            } catch (IOException e10) {
                e = e10;
                fileWriter = fw;
                Log.e(TAG, "Failed to copy file!", e);
                e.printStackTrace();
                if (input != null) {
                    input.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                return ret;
            } catch (Throwable th4) {
                th = th4;
                fileWriter = fw;
                if (input != null) {
                    input.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                throw th;
            }
        } catch (IOException e11) {
            e = e11;
            Log.e(TAG, "Failed to copy file!", e);
            e.printStackTrace();
            if (input != null) {
                input.close();
            }
            if (fileWriter != null) {
                fileWriter.close();
            }
            return ret;
        }
        return ret;
    }

    private boolean updateBastetConfig(int type, String strUri) {
        if (PROPEL_TYPE_PARTNER != type) {
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
            case PROPEL_TYPE_PARTNER /*1*/:
            case MESSAGE_CHECK_SERVICE_TIMEOUT /*2*/:
            case MESSAGE_PARSE_ACC_APP_LIST /*3*/:
            case ACTION_BASTET_SET_DSCP /*10*/:
            case ACTION_BASTET_UNSET_DSCP /*11*/:
                break;
            case ACTION_PACKAGE_ADDED /*4*/:
            case CONNECT_TIMES /*5*/:
            case ACTION_PACKAGE_RESTARTED /*6*/:
            case ACTION_PACKAGE_FIRST_LAUNCH /*7*/:
            case INTENT_PACKAGE_NAME_OFFSET /*8*/:
                try {
                    getBastetService();
                    synchronized (this) {
                        if (this.mIBastetManager != null) {
                            this.mIBastetManager.packageChangedReceived(actionId, name);
                        }
                        break;
                    }
                    return;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                }
            case ACTION_BASTET_UPDATE_CONFIG /*9*/:
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
        for (int i = PROPEL_TYPE_UNKNOWN; i < n; i += PROPEL_TYPE_PARTNER) {
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
            for (int i = PROPEL_TYPE_UNKNOWN; i < len; i += PROPEL_TYPE_PARTNER) {
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
                    setAppActivity(PROPEL_TYPE_UNKNOWN, uid);
                }
            }
        }
    }

    private void removeHrtApp(int uid) {
        synchronized (this) {
            Log.e(TAG, "uid: " + uid);
            int len = this.mHrtAppList.size();
            for (int i = PROPEL_TYPE_UNKNOWN; i < len; i += PROPEL_TYPE_PARTNER) {
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
                int uid = this.mContext.getPackageManager().getPackageUid(name, PROPEL_TYPE_UNKNOWN);
                Log.e(TAG, "len: " + len + ", name: " + name + ", uid: " + uid);
                for (int i = PROPEL_TYPE_UNKNOWN; i < len; i += PROPEL_TYPE_PARTNER) {
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
    }

    private boolean filterHrtApp(String msg) {
        String[] splits = msg.split("\t");
        String pkgName = null;
        boolean found = false;
        try {
            if (splits.length > 0) {
                pkgName = splits[PROPEL_TYPE_UNKNOWN];
            }
        } catch (Exception e) {
        }
        try {
            int uid = this.mContext.getPackageManager().getPackageUid(pkgName, PROPEL_TYPE_UNKNOWN);
            synchronized (this) {
                this.mForegroundUid = uid;
                int len = this.mHrtAppList.size();
                if (len > 0) {
                    for (int i = PROPEL_TYPE_UNKNOWN; i < len; i += PROPEL_TYPE_PARTNER) {
                        if (uid == ((Integer) this.mHrtAppList.get(i)).intValue()) {
                            Log.i(TAG, "foreground uid: " + uid);
                            setAppActivity(PROPEL_TYPE_UNKNOWN, uid);
                            found = true;
                            break;
                        }
                    }
                    if (!found && this.mLastHrtUid > 0) {
                        Log.i(TAG, "background uid: " + this.mLastHrtUid);
                        setAppActivity(PROPEL_TYPE_PARTNER, this.mLastHrtUid);
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
                pkgName = splits[PROPEL_TYPE_UNKNOWN];
            }
            try {
                int uid = this.mContext.getPackageManager().getPackageUid(pkgName, PROPEL_TYPE_UNKNOWN);
                synchronized (this) {
                    boolean found = false;
                    int len = this.mAccAppList.size();
                    if (len > 0) {
                        for (int i = PROPEL_TYPE_UNKNOWN; i < len; i += PROPEL_TYPE_PARTNER) {
                            if (((String) this.mAccAppList.get(i)).equals(pkgName)) {
                                Log.i(TAG, "filterAccApp foreground uid: " + uid);
                                setAppActivity(PROPEL_TYPE_UNKNOWN, uid);
                                this.mLastAccUid = uid;
                                found = true;
                                break;
                            }
                        }
                        if (!(found || isFound)) {
                            if (this.mLastAccUid > 0) {
                                Log.i(TAG, "filterAccApp background uid: " + this.mLastAccUid);
                                setAppActivity(PROPEL_TYPE_PARTNER, this.mLastAccUid);
                                this.mLastAccUid = INVALID_UID;
                            }
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
            } else if (activity == PROPEL_TYPE_PARTNER) {
                this.mLastHrtUid = INVALID_UID;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setAppBackground() {
        synchronized (this) {
            if (this.mLastHrtUid > 0) {
                Log.i(TAG, "background uid: " + this.mLastHrtUid);
                setAppActivity(PROPEL_TYPE_PARTNER, this.mLastHrtUid);
            }
        }
    }

    private void initPgPlugThread() {
        this.mPGPlug = new PGPlug(this.mPgEventProcesser, TAG);
        new Thread(this.mPGPlug, TAG).start();
    }
}
