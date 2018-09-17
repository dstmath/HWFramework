package com.android.server.emcom.grabservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.android.server.emcom.EmcomThread;

public class AutoGrabService extends AccessibilityService {
    public static final String AUTOGRAB_SERVICE_COMPOENT_NAME = "android/com.android.server.emcom.grabservice.AutoGrabService";
    private static final long CONNECT_KEEP_TIME = 25000;
    private static final long DISCONNECT_DELAY = 5000;
    public static final int MSG_AUTOGRAB_CONFIG = 5;
    public static final int MSG_BT_CONNECTION_CHANAGE = 1;
    public static final int MSG_BT_RECEIVE = 2;
    public static final int MSG_BT_SEND = 3;
    public static final int MSG_DISCONNECT_TO_AMS = 7;
    public static final int MSG_GRAB_TIMEOUT = 4;
    public static final int MSG_NEW_WECHAT_PACKET = 6;
    public static final int MSG_PKG_CHANGED = 10;
    public static final int MSG_SAMPLE_WIN_CHANGE = 8;
    public static final int MSG_WECHAT_MONITOR_CONFIG_UPDATE = 9;
    static final String TAG = "GrabService";
    private static final int TIME_INTERVAL = 50;
    private static Handler mHandler;
    private volatile boolean mBluetoothConnect;
    private BluetoothConnMgr mConnectionManager;
    private volatile boolean mHasRecordInThisWindow;
    private volatile boolean mIsFeatureEnable;
    private volatile boolean mIsSampleWinEnable;
    private BroadcastReceiver mReceiver;
    private WechatGrabController mWechatGrabController;
    private WechatMonitor mWechatMonitor;

    public interface AccessibilityEventCallback {
        void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);
    }

    private class CommandHandler extends Handler {
        public CommandHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AutoGrabService.this.handleBtConnectionChange();
                    return;
                case 2:
                    AutoGrabService.this.executeCmd(msg.arg1, msg.arg2);
                    return;
                case 3:
                    AutoGrabService.this.mConnectionManager.sendMessage(msg.obj);
                    return;
                case 4:
                    AutoGrabService.this.mWechatGrabController.handleTimeout(msg.arg1, msg.arg2);
                    return;
                case 5:
                    String config = msg.obj;
                    Log.d(AutoGrabService.TAG, "receive add package config:" + config);
                    AutoGrabService.this.addAutograbConfig(config);
                    AutoGrabService.this.mIsFeatureEnable = true;
                    AutoGrabService.this.startListenIfEnable();
                    return;
                case 6:
                    Log.d(AutoGrabService.TAG, "new wechat message receive");
                    Notification wechatMessage = msg.obj;
                    if (AutoGrabService.this.mIsSampleWinEnable && (AutoGrabService.this.mHasRecordInThisWindow ^ 1) != 0 && AutoGrabService.this.isMoneyNotification(wechatMessage)) {
                        Log.d(AutoGrabService.TAG, "connect to AccessibilityManagerService");
                        AutoGrabService.this.connectToAccessibilityManagerService();
                        AutoGrabService.this.mHasRecordInThisWindow = true;
                    }
                    if (AutoGrabService.this.mBluetoothConnect) {
                        AutoGrabService.this.mWechatGrabController.handleNotification(wechatMessage);
                        return;
                    }
                    return;
                case 7:
                    AutoGrabService.this.disconnectFromAccessibilityManagerService();
                    return;
                case 8:
                    boolean enable = ((Boolean) msg.obj).booleanValue();
                    Log.d(AutoGrabService.TAG, "receive sample window change message:" + enable);
                    AutoGrabService.this.mIsSampleWinEnable = AutoGrabService.this.mWechatMonitor.updateSampleWinEnable(enable);
                    if (!enable) {
                        AutoGrabService.this.disconnectFromAccessibilityManagerService();
                        AutoGrabService.this.mHasRecordInThisWindow = false;
                        return;
                    }
                    return;
                case 9:
                    Log.d(AutoGrabService.TAG, "receive wechat monitor config update message.");
                    AutoGrabService.this.mWechatMonitor.initConfig();
                    return;
                case 10:
                    String name = msg.obj;
                    Log.d(AutoGrabService.TAG, "receive package change msg:" + name);
                    if ("com.tencent.mm".equals(name)) {
                        AutoGrabService.this.mWechatMonitor.updateCurrentVersionConfig();
                        return;
                    }
                    return;
                default:
                    Log.e(AutoGrabService.TAG, "unknown message type.");
                    return;
            }
        }
    }

    class GrabBroadcastReceiver extends BroadcastReceiver {
        GrabBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                switch (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 0)) {
                    case 10:
                        if (AutoGrabService.this.mIsFeatureEnable) {
                            Log.d("TAG", "bluetooth off, stop all bluetooch thread.");
                            AutoGrabService.this.mConnectionManager.stopAllThreads();
                            return;
                        }
                        return;
                    case 12:
                        if (AutoGrabService.this.mIsFeatureEnable) {
                            AutoGrabService.this.startListenIfEnable();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AutograbService onCreate");
        mHandler = new CommandHandler(EmcomThread.getInstanceLooper());
        this.mReceiver = new GrabBroadcastReceiver();
        this.mConnectionManager = new BluetoothConnMgr(mHandler);
        this.mWechatMonitor = new WechatMonitor(this);
        this.mWechatGrabController = new WechatGrabController(this, mHandler, "com.tencent.mm");
        registerBroadcastReceivers();
        AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, false);
    }

    private void registerBroadcastReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        registerReceiver(this.mReceiver, filter);
    }

    protected void executeCmd(int cmd, int notifyId) {
        if (cmd == 6) {
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
        this.mWechatMonitor.onAccessibilityEvent(event);
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

    private void addAutograbConfig(String config) {
        if (TextUtils.isEmpty(config)) {
            Log.d(TAG, "autograb config String is null!");
        } else {
            this.mWechatGrabController.parseGrabParams(config);
        }
    }

    private void startListenIfEnable() {
        if (this.mConnectionManager.isBluetoothEnable()) {
            Log.d(TAG, "bluetooth enable, listen connect request.");
            this.mConnectionManager.listenConnectRequest();
        }
    }

    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected.");
        String[] packageNames = new String[]{"com.tencent.mm"};
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = -1;
        serviceInfo.feedbackType = 16;
        serviceInfo.packageNames = packageNames;
        serviceInfo.notificationTimeout = 50;
        setServiceInfo(serviceInfo);
    }

    public void onDestroy() {
        AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, false);
        mHandler = null;
        unregisterReceiver(this.mReceiver);
        this.mIsFeatureEnable = false;
        super.onDestroy();
    }

    public void disconnectFromAccessibilityManagerService() {
        if (isGrabingNow()) {
            Log.d(TAG, "Grabing wechat packet now, delay disconnect.");
            mHandler.sendMessageDelayed(Message.obtain(mHandler, 7), DISCONNECT_DELAY);
            return;
        }
        Log.d(TAG, "Disconnect from AccessibilityManagerService");
        AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, false);
    }

    private boolean isMoneyNotification(Notification wechatMessage) {
        return this.mWechatMonitor.isMoneyNotification(wechatMessage);
    }

    private boolean isGrabingNow() {
        return this.mBluetoothConnect ? this.mWechatGrabController.isGrabingNow() : false;
    }

    private void connectToAccessibilityManagerService() {
        AutoGrabTools.setAccessibilityServiceEnable(this, AUTOGRAB_SERVICE_COMPOENT_NAME, true);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, 7), CONNECT_KEEP_TIME);
    }
}
