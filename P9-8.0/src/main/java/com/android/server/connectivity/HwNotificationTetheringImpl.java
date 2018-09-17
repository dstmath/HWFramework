package com.android.server.connectivity;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import java.util.List;

public class HwNotificationTetheringImpl implements HwNotificationTethering {
    private static final String ACTION_WIFI_AP_STA_JOIN = "android.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "android.net.wifi.WIFI_AP_STA_LEAVE";
    private static final boolean DBG = HWFLOW;
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_WIFI_REPEATER_CLIENTS_SIZE = "wifi_repeater_clients_size";
    protected static final boolean HWFLOW;
    private static final String IDLE_WIFI_TETHER_ALARM_TAG = "HwCustTethering IDLE_WIFI_TETHER";
    private static final int IDLE_WIFI_TETHER_DELAY = 600000;
    private static final int IDLE_WIFI_TETHER_MSG = 0;
    private static final int NOTIFICATION_TYPE_BLUETOOTH = 2;
    private static final int NOTIFICATION_TYPE_MULTIPLE = 4;
    private static final int NOTIFICATION_TYPE_NONE = -1;
    private static final int NOTIFICATION_TYPE_P2P = 3;
    private static final int NOTIFICATION_TYPE_STOP_AP = 5;
    private static final int NOTIFICATION_TYPE_USB = 1;
    private static final int NOTIFICATION_TYPE_WIFI = 0;
    private static final int NO_DEVICE_CONNECTED = 0;
    private static final int POWER_OFF = 0;
    private static final String START_TETHER_ACTION = "com.huawei.server.connectivity.action.START_TETHER";
    private static final String START_TETHER_PERMISSION = "com.android.server.connectivity.permission.START_TETHERING";
    private static final String TAG = "HwCustTethering";
    private static final int TYPE_WIFI = 0;
    private static final String WIFI_REPEATER_CLIENTS_CHANGED_ACTION = "com.huawei.wifi.action.WIFI_REPEATER_CLIENTS_CHANGED";
    private static final Object tetheredLock = new Object();
    private AlarmManager mAlarmManager;
    private int mBluetoothUsbNum;
    private Context mContext;
    OnAlarmListener mIdleApAlarmListener = new OnAlarmListener() {
        public void onAlarm() {
            if (HwNotificationTetheringImpl.this.mPluggedType != 0) {
                Log.d(HwNotificationTetheringImpl.TAG, "the current state is power on, cancle alarm");
                HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleApAlarmListener);
                return;
            }
            HwNotificationTetheringImpl.this.stopTethering(0);
            Log.d(HwNotificationTetheringImpl.TAG, "WIFI_TETHER was in idle 10 minutes, stop tethering,show notification");
            HwNotificationTetheringImpl.this.showStopWifiTetherNotification();
        }
    };
    private Handler mIdleWifiTetherHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.d(HwNotificationTetheringImpl.TAG, "handle message IDLE_WIFI_TETHER_MSG, do nothing");
                    return;
                default:
                    return;
            }
        }
    };
    private CharSequence mMessage;
    private NotificationManager mNotificationManager;
    private int mP2pConnectNum;
    private boolean mP2pTethered;
    private PendingIntent mPi;
    private int mPluggedType = -1;
    private OnStartTetheringCallback mStartTetheringCallback;
    private Notification mStopApNotification;
    private boolean mSupportWifiRepeater;
    private Notification mTetheredNotification;
    private int[] mTetheredRecord = new int[]{0, 0, 0, 0};
    private CharSequence mTitle;
    private int mTotalNum;
    private int mWifiConnectNum;
    private boolean mWifiTetherd;

    static final class OnStartTetheringCallback extends android.net.ConnectivityManager.OnStartTetheringCallback {
        OnStartTetheringCallback() {
        }

        public void onTetheringStarted() {
        }

        public void onTetheringFailed() {
            Log.e(HwNotificationTetheringImpl.TAG, "WIFI tethering FAILED!");
        }
    }

    private class P2pConnectNumReceiver extends BroadcastReceiver {
        /* synthetic */ P2pConnectNumReceiver(HwNotificationTetheringImpl this$0, P2pConnectNumReceiver -this1) {
            this();
        }

        private P2pConnectNumReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.d(HwNotificationTetheringImpl.TAG, "onReceive: " + intent.getAction());
            int p2pConnectNum = intent.getIntExtra(HwNotificationTetheringImpl.EXTRA_WIFI_REPEATER_CLIENTS_SIZE, 0);
            if (HwNotificationTetheringImpl.this.mTetheredNotification != null && HwNotificationTetheringImpl.this.mP2pTethered && HwNotificationTetheringImpl.this.mP2pConnectNum != p2pConnectNum) {
                HwNotificationTetheringImpl.this.mP2pConnectNum = p2pConnectNum;
                HwNotificationTetheringImpl.this.showTetheredNotificationWithNumbers(3);
            }
        }
    }

    private class WifiConnectNumReceiver extends BroadcastReceiver {
        /* synthetic */ WifiConnectNumReceiver(HwNotificationTetheringImpl this$0, WifiConnectNumReceiver -this1) {
            this();
        }

        private WifiConnectNumReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.d(HwNotificationTetheringImpl.TAG, "onReceive: " + intent.getAction());
            int wifiConnectNum = intent.getIntExtra(HwNotificationTetheringImpl.EXTRA_STA_COUNT, 0);
            if (HwNotificationTetheringImpl.this.mTetheredNotification != null && HwNotificationTetheringImpl.this.mWifiTetherd && HwNotificationTetheringImpl.this.mWifiConnectNum != wifiConnectNum) {
                HwNotificationTetheringImpl.this.mWifiConnectNum = wifiConnectNum;
                HwNotificationTetheringImpl.this.showTetheredNotificationWithNumbers(0);
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public HwNotificationTetheringImpl(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        initConfigs();
        registerConnectNumReceiver();
        registerStartTetherReceiver();
        if (!SystemProperties.getBoolean("ro.config.check_hotspot_status", false) && (SystemProperties.getBoolean("ro.config.hotspot_power_mode_on", false) ^ 1) != 0) {
            registerPluggedTypeReceiver();
        }
    }

    private void initConfigs() {
        this.mSupportWifiRepeater = SystemProperties.getBoolean("ro.config.hw_wifibridge", false);
    }

    private void registerConnectNumReceiver() {
        if (this.mSupportWifiRepeater) {
            this.mContext.registerReceiver(new P2pConnectNumReceiver(this, null), new IntentFilter(WIFI_REPEATER_CLIENTS_CHANGED_ACTION));
        }
        BroadcastReceiver receiver = new WifiConnectNumReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WIFI_AP_STA_JOIN);
        filter.addAction(ACTION_WIFI_AP_STA_LEAVE);
        this.mContext.registerReceiver(receiver, filter);
    }

    private void registerStartTetherReceiver() {
        this.mStartTetheringCallback = new OnStartTetheringCallback();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (HwNotificationTetheringImpl.START_TETHER_ACTION.equals(intent.getAction())) {
                    Log.d(HwNotificationTetheringImpl.TAG, "receive start tether action");
                    ((ConnectivityManager) HwNotificationTetheringImpl.this.mContext.getSystemService("connectivity")).startTethering(0, false, HwNotificationTetheringImpl.this.mStartTetheringCallback);
                    HwNotificationTetheringImpl.this.clearStopApNotification();
                }
            }
        }, new IntentFilter(START_TETHER_ACTION), START_TETHER_PERMISSION, null);
    }

    private void registerPluggedTypeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    int temp = intent.getIntExtra("plugged", 0);
                    if (HwNotificationTetheringImpl.this.mPluggedType != temp) {
                        Log.d(HwNotificationTetheringImpl.TAG, "mPluggedType changed to " + temp);
                        HwNotificationTetheringImpl.this.mPluggedType = temp;
                        if (HwNotificationTetheringImpl.this.mPluggedType != 0) {
                            Log.d(HwNotificationTetheringImpl.TAG, "receive power connected, cancle alarm");
                            HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleApAlarmListener);
                            return;
                        }
                        Log.d(HwNotificationTetheringImpl.TAG, "receive power disconnected, mWifiTetherd = " + HwNotificationTetheringImpl.this.mWifiTetherd + ", mWifiConnectNum = " + HwNotificationTetheringImpl.this.mWifiConnectNum);
                        if (HwNotificationTetheringImpl.this.mWifiTetherd && HwNotificationTetheringImpl.this.mWifiConnectNum == 0) {
                            HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleApAlarmListener);
                            HwNotificationTetheringImpl.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME, HwNotificationTetheringImpl.IDLE_WIFI_TETHER_ALARM_TAG, HwNotificationTetheringImpl.this.mIdleApAlarmListener, null);
                        }
                    }
                }
            }
        }, filter);
    }

    private void sendNotification(boolean shouldForceUpdate) {
        int totalNum = this.mBluetoothUsbNum + this.mWifiConnectNum;
        if (DBG) {
            Log.d(TAG, "sendNumberChangeNotification:" + totalNum);
        }
        if (shouldForceUpdate || this.mTotalNum != totalNum) {
            this.mTotalNum = totalNum;
            Notification tempTetheredNotification = this.mTetheredNotification;
            if (tempTetheredNotification != null) {
                CharSequence title;
                CharSequence message;
                this.mNotificationManager.cancelAsUser(null, tempTetheredNotification.icon, UserHandle.ALL);
                if (this.mWifiConnectNum <= 0 || this.mBluetoothUsbNum != 0) {
                    title = this.mTitle;
                    message = this.mMessage;
                } else {
                    Resources r = Resources.getSystem();
                    title = this.mContext.getString(33685840);
                    message = r.getQuantityString(34406400, this.mWifiConnectNum, new Object[]{Integer.valueOf(this.mWifiConnectNum)});
                }
                tempTetheredNotification.tickerText = title;
                tempTetheredNotification.setLatestEventInfo(this.mContext, title, message, this.mPi);
                this.mNotificationManager.notifyAsUser(null, tempTetheredNotification.icon, tempTetheredNotification, UserHandle.ALL);
            }
        }
    }

    public int getTetheredIcon(boolean usbTethered, boolean wifiTethered, boolean bluetoothTethered, boolean p2pTethered) {
        int icon = 0;
        int tetheredTypes = 0;
        resetTetheredRecord();
        if (wifiTethered) {
            icon = 17303395;
            tetheredTypes = 1;
            this.mTetheredRecord[0] = 1;
        }
        if (usbTethered) {
            icon = 33751226;
            tetheredTypes++;
            this.mTetheredRecord[1] = 1;
        }
        if (bluetoothTethered) {
            icon = 33751224;
            tetheredTypes++;
            this.mTetheredRecord[2] = 1;
        }
        if (p2pTethered) {
            icon = 33751225;
            tetheredTypes++;
            this.mTetheredRecord[3] = 1;
        }
        if (tetheredTypes > 1) {
            return 33751225;
        }
        return icon;
    }

    public void setTetheringNumber(boolean wifiTethered, boolean usbTethered, boolean bluetoothTethered) {
        if (DBG) {
            Log.d(TAG, "wifiTethered:" + wifiTethered + " usbTethered:" + usbTethered + " bluetoothTethered:" + bluetoothTethered);
        }
        this.mBluetoothUsbNum = 0;
        if (usbTethered) {
            this.mBluetoothUsbNum = 1;
        }
        if (bluetoothTethered) {
            this.mBluetoothUsbNum++;
        }
        this.mWifiTetherd = wifiTethered;
        if (!this.mWifiTetherd) {
            this.mWifiConnectNum = 0;
        }
    }

    public void setTetheringNumber(List<String> tetheringNumbers) {
        boolean p2pTethered = tetheringNumbers.contains("p2p");
        if (this.mSupportWifiRepeater) {
            Log.d(TAG, "setTetheringNumber: p2pTethered = " + p2pTethered);
            this.mP2pTethered = p2pTethered;
            if (!this.mP2pTethered) {
                this.mP2pConnectNum = 0;
            }
        }
        boolean wifiTethered = tetheringNumbers.contains("wifi");
        boolean usbTethered = tetheringNumbers.contains("usb");
        boolean bluetoothTethered = tetheringNumbers.contains("bluetooth");
        if (DBG) {
            Log.d(TAG, "wifiTethered:" + wifiTethered + " usbTethered:" + usbTethered + " bluetoothTethered:" + bluetoothTethered);
        }
        this.mBluetoothUsbNum = 0;
        if (usbTethered) {
            this.mBluetoothUsbNum = 1;
        }
        if (bluetoothTethered) {
            this.mBluetoothUsbNum++;
        }
        this.mWifiTetherd = wifiTethered;
        if (!this.mWifiTetherd) {
            this.mWifiConnectNum = 0;
        }
    }

    public boolean sendTetherNotification(Notification tetheredNotification, CharSequence title, CharSequence message, PendingIntent pi) {
        if (DBG) {
            Log.d(TAG, "sendTetherNotification " + tetheredNotification);
        }
        this.mTetheredNotification = tetheredNotification;
        this.mTitle = title;
        this.mMessage = message;
        this.mPi = pi;
        if (this.mWifiConnectNum <= 0 && this.mBluetoothUsbNum <= 0) {
            return false;
        }
        sendNotification(true);
        return true;
    }

    public void sendTetherNotification() {
        sendNotification(false);
    }

    public void clearTetheredNotification() {
        synchronized (tetheredLock) {
            if (DBG) {
                Log.d(TAG, "clearTetheredNotification");
            }
            this.mTetheredNotification = null;
            this.mIdleWifiTetherHandler.removeMessages(0);
            this.mAlarmManager.cancel(this.mIdleApAlarmListener);
        }
    }

    private void resetTetheredRecord() {
        for (int type = 0; type < 4; type++) {
            this.mTetheredRecord[type] = 0;
        }
    }

    public void stopTethering() {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        for (int type = 0; type < 4; type++) {
            if (this.mTetheredRecord[type] == 1) {
                cm.stopTethering(type);
            }
        }
    }

    private void stopTethering(int type) {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (this.mTetheredRecord[type] == 1) {
            cm.stopTethering(type);
        }
    }

    public int getNotificationType(List<String> tetheringNumbers) {
        int type = -1;
        int tetheredTypes = 0;
        resetTetheredRecord();
        boolean wifiTethered = tetheringNumbers.contains("wifi");
        boolean usbTethered = tetheringNumbers.contains("usb");
        boolean bluetoothTethered = tetheringNumbers.contains("bluetooth");
        boolean p2pTethered = tetheringNumbers.contains("p2p");
        if (wifiTethered) {
            type = 0;
            tetheredTypes = 1;
            this.mTetheredRecord[0] = 1;
        }
        if (usbTethered) {
            type = 1;
            tetheredTypes++;
            this.mTetheredRecord[1] = 1;
        }
        if (bluetoothTethered) {
            type = 2;
            tetheredTypes++;
            this.mTetheredRecord[2] = 1;
        }
        if (p2pTethered) {
            type = 3;
            tetheredTypes++;
            this.mTetheredRecord[3] = 1;
        }
        if (tetheredTypes > 1) {
            return 4;
        }
        return type;
    }

    public int getNotificationIcon(int notificationType) {
        switch (notificationType) {
            case 0:
                return 17303395;
            case 1:
                return 33751226;
            case 2:
                return 33751224;
            case 3:
                return 17303395;
            case 4:
                return 33751225;
            case 5:
                return 33751786;
            default:
                return 0;
        }
    }

    public CharSequence getNotificationTitle(int notificationType) {
        Resources r = Resources.getSystem();
        if (3 == notificationType) {
            return r.getText(33685850);
        }
        if (5 == notificationType) {
            return r.getText(33686063);
        }
        return r.getText(33685847);
    }

    private CharSequence getNotificationMessageWithNumbers(int notificationType) {
        Resources r = Resources.getSystem();
        switch (notificationType) {
            case 0:
                return r.getQuantityString(34406401, this.mWifiConnectNum, new Object[]{Integer.valueOf(this.mWifiConnectNum)});
            case 1:
            case 2:
                return r.getQuantityString(34406401, this.mBluetoothUsbNum, new Object[]{Integer.valueOf(this.mBluetoothUsbNum)});
            case 3:
                return r.getQuantityString(34406401, this.mP2pConnectNum, new Object[]{Integer.valueOf(this.mP2pConnectNum)});
            default:
                return r.getText(33685848);
        }
    }

    public CharSequence getNotificationActionText(int notificationType) {
        Resources r = Resources.getSystem();
        switch (notificationType) {
            case 0:
                return r.getText(33685852);
            case 3:
                return r.getText(33685851);
            case 5:
                return r.getText(33686061);
            default:
                return r.getText(33685849);
        }
    }

    public Intent getNotificationIntent(int notificationType) {
        Intent intent = new Intent();
        switch (notificationType) {
            case 0:
                intent.setAction("android.settings.WIFI_AP_SETTINGS");
                break;
            case 3:
                intent.setAction("android.settings.WIFI_BRIDGE_SETTINGS");
                break;
            default:
                intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
                break;
        }
        return intent;
    }

    public void showTetheredNotification(int notificationType, Notification notification, PendingIntent pi) {
        this.mTetheredNotification = notification;
        this.mPi = pi;
        showTetheredNotificationWithNumbers(notificationType);
    }

    /* JADX WARNING: Missing block: B:19:0x0085, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showTetheredNotificationWithNumbers(int notificationType) {
        synchronized (tetheredLock) {
            if (this.mTetheredNotification == null) {
                return;
            }
            this.mNotificationManager.cancelAsUser(null, this.mTetheredNotification.icon, UserHandle.ALL);
            if (this.mWifiTetherd) {
                clearStopApNotification();
                if (this.mWifiConnectNum > 0) {
                    Log.d(TAG, "removeMessage IDLE_WIFI_TETHER_MSG, mWifiConnectNum is " + this.mWifiConnectNum);
                    this.mIdleWifiTetherHandler.removeMessages(0);
                    this.mAlarmManager.cancel(this.mIdleApAlarmListener);
                } else if (!(SystemProperties.getBoolean("ro.config.check_hotspot_status", false) || (SystemProperties.getBoolean("ro.config.hotspot_power_mode_on", false) ^ 1) == 0 || (this.mIdleWifiTetherHandler.hasMessages(0) ^ 1) == 0)) {
                    Log.d(TAG, "send IDLE_WIFI_TETHER delay message");
                    Message msg = new Message();
                    msg.what = 0;
                    this.mIdleWifiTetherHandler.sendMessageDelayed(msg, AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME);
                    this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME, IDLE_WIFI_TETHER_ALARM_TAG, this.mIdleApAlarmListener, null);
                }
            }
            Log.d(TAG, "showTetheredNotificationWithNumbers");
            if (this.mWifiConnectNum <= 0 && this.mP2pConnectNum <= 0) {
                if (this.mBluetoothUsbNum <= 0) {
                    if (notificationType == 3) {
                        this.mTetheredNotification.setLatestEventInfo(this.mContext, getNotificationTitle(notificationType), Resources.getSystem().getText(33685848), this.mPi);
                        this.mTetheredNotification.actions = getNotificationAction(notificationType);
                        this.mNotificationManager.notifyAsUser(null, this.mTetheredNotification.icon, this.mTetheredNotification, UserHandle.ALL);
                    }
                }
            }
            this.mTetheredNotification.setLatestEventInfo(this.mContext, getNotificationTitle(notificationType), getNotificationMessageWithNumbers(notificationType), this.mPi);
            this.mTetheredNotification.actions = getNotificationAction(notificationType);
            this.mNotificationManager.notifyAsUser(null, this.mTetheredNotification.icon, this.mTetheredNotification, UserHandle.ALL);
        }
    }

    private synchronized void showStopWifiTetherNotification() {
        PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, getNotificationIntent(0), 0, null, UserHandle.CURRENT);
        PendingIntent pIntentCancel = PendingIntent.getBroadcast(this.mContext, 0, new Intent(START_TETHER_ACTION), 134217728);
        Resources resource = Resources.getSystem();
        CharSequence title = getNotificationTitle(5);
        CharSequence message = resource.getText(33686062);
        CharSequence action_text = getNotificationActionText(5);
        int icon = getNotificationIcon(5);
        Builder stopApNotificationBuilder = new Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS);
        stopApNotificationBuilder.setWhen(0).setOngoing(false).setVisibility(1).setCategory("status").addAction(new Action(0, action_text, pIntentCancel)).setSmallIcon(icon).setContentTitle(title).setContentText(message).setStyle(new BigTextStyle().bigText(message)).setContentIntent(pi).setAutoCancel(true);
        this.mStopApNotification = stopApNotificationBuilder.build();
        this.mNotificationManager.notifyAsUser(null, this.mStopApNotification.icon, this.mStopApNotification, UserHandle.ALL);
    }

    private synchronized void clearStopApNotification() {
        if (this.mStopApNotification != null) {
            this.mNotificationManager.cancelAsUser(null, this.mStopApNotification.icon, UserHandle.ALL);
            this.mStopApNotification = null;
        }
    }

    private Action[] getNotificationAction(int notificationType) {
        Action[] actions = new Action[1];
        actions[0] = new Action(0, getNotificationActionText(notificationType), PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.server.connectivity.action.STOP_TETHERING"), 134217728));
        return actions;
    }
}
