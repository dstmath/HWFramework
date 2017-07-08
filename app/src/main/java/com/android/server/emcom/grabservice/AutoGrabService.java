package com.android.server.emcom.grabservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.android.server.HwConnectivityService;
import com.android.server.emcom.EmcomThread;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import java.util.ArrayList;
import java.util.List;

public class AutoGrabService extends AccessibilityService {
    public static final String AUTOGRAB_SERVICE_COMPOENT_NAME = "android/com.android.server.emcom.grabservice.AutoGrabService";
    private static final int CONNECT_TO_AMS_DELAY = 1000;
    public static final String DELETE_ALL = "delete_all";
    public static final int MSG_ADD_CONFIG = 6;
    public static final int MSG_BT_CONNECTION_CHANAGE = 0;
    public static final int MSG_BT_RECEIVE = 1;
    public static final int MSG_BT_SEND = 2;
    public static final int MSG_CONNECT_TO_AMS = 7;
    public static final int MSG_DELETE_CONFIG = 5;
    public static final int MSG_GRAB_TIMEOUT = 3;
    public static final int MSG_INIT_PACKAGES_LIST = 4;
    public static final int MSG_NEW_WECHAT_PACKET = 9;
    public static final int MSG_SEND_GRAB_ACCE_RSP = 8;
    public static final String PACKAGE_NAME_SPLITER = ";";
    static final String TAG = "GrabService";
    private static final int TIME_INTERVAL = 50;
    private static Handler mHandler;
    private List<String> mAppPackageNames;
    private volatile boolean mBluetoothConnect;
    private BluetoothConnMgr mConnectionManager;
    private List<String> mEnablePackageNames;
    private BroadcastReceiver mReceiver;
    private AppStateWatcher mWatcher;
    private WechatGrabController mWechatGrabController;

    public interface AccessibilityEventCallback {
        void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);
    }

    private class CommandHandler extends Handler {
        public CommandHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AutoGrabService.MSG_BT_CONNECTION_CHANAGE /*0*/:
                    AutoGrabService.this.handleBtConnectionChange();
                case AutoGrabService.MSG_BT_RECEIVE /*1*/:
                    AutoGrabService.this.executeCmd(msg.arg1, msg.arg2);
                case AutoGrabService.MSG_BT_SEND /*2*/:
                    AutoGrabService.this.mConnectionManager.sendMessage(msg.obj);
                case AutoGrabService.MSG_GRAB_TIMEOUT /*3*/:
                    AutoGrabService.this.mWechatGrabController.handleTimeout(msg.arg1, msg.arg2);
                case AutoGrabService.MSG_INIT_PACKAGES_LIST /*4*/:
                    AutoGrabService.this.initWatchAppList(msg.obj);
                case AutoGrabService.MSG_DELETE_CONFIG /*5*/:
                    String packageName = msg.obj;
                    Log.d(AutoGrabService.TAG, "receive delete package config:" + packageName);
                    if (packageName != null) {
                        AutoGrabService.this.deleteConfig(packageName);
                    }
                case AutoGrabService.MSG_ADD_CONFIG /*6*/:
                    AppInfo info = msg.obj;
                    Log.d(AutoGrabService.TAG, "receive add package config:" + info);
                    AutoGrabService.this.addConfig(info);
                case AutoGrabService.MSG_SEND_GRAB_ACCE_RSP /*8*/:
                    Log.d(AutoGrabService.TAG, "receive grab accelerate response");
                case AutoGrabService.MSG_NEW_WECHAT_PACKET /*9*/:
                    Log.d(AutoGrabService.TAG, "new wechat message receive");
                    if (AutoGrabService.this.mBluetoothConnect) {
                        AutoGrabService.this.mWechatGrabController.handleNotification(msg.obj);
                    }
                default:
            }
        }
    }

    class GrabBroadcastReceiver extends BroadcastReceiver {
        GrabBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                switch (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", AutoGrabService.MSG_BT_CONNECTION_CHANAGE)) {
                    case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
                        Log.d("TAG", "bluetooth off, stop all bluetooch thread.");
                        AutoGrabService.this.mConnectionManager.stopAllThreads();
                    case HwGnssLogHandlerMsgID.UPDATESETPOSMODE /*12*/:
                        if (AutoGrabService.this.mConnectionManager.isBluetoothEnable()) {
                            Log.d("TAG", "bluetooth on, start listening.");
                            AutoGrabService.this.mConnectionManager.listenConnectRequest();
                        }
                    default:
                }
            }
        }
    }

    public AutoGrabService() {
        this.mAppPackageNames = new ArrayList();
        this.mEnablePackageNames = new ArrayList();
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AutograbService onCreate");
        mHandler = new CommandHandler(EmcomThread.getInstanceLooper());
        this.mReceiver = new GrabBroadcastReceiver();
        this.mConnectionManager = new BluetoothConnMgr(mHandler);
        this.mWatcher = new AppStateWatcher(this);
        this.mWechatGrabController = new WechatGrabController(this, mHandler, HwConnectivityService.MM_PKG_NAME);
        if (this.mConnectionManager.isBluetoothEnable()) {
            Log.d(TAG, "connection manager isBluetoothEnable success!");
            this.mConnectionManager.listenConnectRequest();
        }
        registerBroadcastReceivers();
        AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, false);
    }

    private void registerBroadcastReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        registerReceiver(this.mReceiver, filter);
    }

    protected void executeCmd(int cmd, int notifyId) {
        if (cmd == MSG_ADD_CONFIG) {
            AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, true);
        }
        this.mWechatGrabController.executeCommand(cmd, notifyId);
    }

    protected void handleBtConnectionChange() {
        if (this.mConnectionManager.getState() == ConnectState.Connected) {
            Log.d(TAG, "bluetooth is connect");
            this.mBluetoothConnect = true;
        } else if (this.mConnectionManager.getState() == ConnectState.Disconnected) {
            Log.d(TAG, "bluetooth is disconnect");
            this.mBluetoothConnect = false;
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        this.mWatcher.onAccessibilityEvent(event);
        if (this.mBluetoothConnect) {
            this.mWechatGrabController.onAccessibilityEvent(event);
        }
    }

    public void onInterrupt() {
    }

    public static Handler getHandler() {
        if (mHandler != null) {
            return mHandler;
        }
        return null;
    }

    private void addConfig(AppInfo info) {
        if (info == null) {
            Log.d(TAG, "info is null!");
            return;
        }
        if (!this.mAppPackageNames.contains(info.packageName)) {
            this.mAppPackageNames.add(info.packageName);
            AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, false);
            Message connectMsg = Message.obtain();
            connectMsg.what = MSG_CONNECT_TO_AMS;
            mHandler.sendMessageDelayed(connectMsg, 1000);
        }
        if (!this.mEnablePackageNames.contains(info.packageName)) {
            this.mEnablePackageNames.add(info.packageName);
        }
        this.mWatcher.addAppConfig(info);
        if (HwConnectivityService.MM_PKG_NAME.equals(info.packageName)) {
            this.mWechatGrabController.parseGrabParams(info.effectiveAutograbParam);
        }
    }

    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected:" + this.mAppPackageNames);
        String[] packageNames = AutoGrabTools.list2Array(this.mAppPackageNames);
        if (packageNames != null && packageNames.length > 0) {
            AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
            serviceInfo.eventTypes = -1;
            serviceInfo.feedbackType = 16;
            serviceInfo.packageNames = packageNames;
            serviceInfo.notificationTimeout = 50;
            setServiceInfo(serviceInfo);
        }
    }

    public void onDestroy() {
        AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, false);
        mHandler = null;
        unregisterReceiver(this.mReceiver);
        super.onDestroy();
    }

    private void initWatchAppList(String names) {
        resetAccessibilitySwitch();
        String[] packages = names.split(PACKAGE_NAME_SPLITER);
        Log.d(TAG, "receive isBluetoothEnable package list.");
        int length = packages.length;
        for (int i = MSG_BT_CONNECTION_CHANAGE; i < length; i += MSG_BT_RECEIVE) {
            String packageName = packages[i];
            Log.d(TAG, "package list add:" + packageName);
            if (this.mAppPackageNames.contains(packageName)) {
                Log.d(TAG, packageName + " is already in list");
            } else {
                this.mAppPackageNames.add(packageName);
            }
        }
    }

    private void resetAccessibilitySwitch() {
        if (AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, false)) {
            Message connectMsg = Message.obtain();
            connectMsg.what = MSG_CONNECT_TO_AMS;
            mHandler.sendMessageDelayed(connectMsg, 1000);
        }
    }

    private void deleteConfig(String packageName) {
        if (packageName.equals(DELETE_ALL)) {
            this.mEnablePackageNames.clear();
            this.mWatcher.deleteAllConfig();
        } else {
            this.mWatcher.deleteAppConfig(packageName);
            this.mEnablePackageNames.remove(packageName);
        }
        if (this.mEnablePackageNames.isEmpty()) {
            AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, false);
        }
    }
}
