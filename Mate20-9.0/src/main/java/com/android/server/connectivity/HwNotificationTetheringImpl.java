package com.android.server.connectivity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import java.util.List;

public class HwNotificationTetheringImpl implements HwNotificationTethering {
    private static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    private static final String ACTION_WIFI_AP_STA_JOIN = "android.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "android.net.wifi.WIFI_AP_STA_LEAVE";
    /* access modifiers changed from: private */
    public static final boolean DBG = HWFLOW;
    private static final String EXTRA_BLUETOOTH_PAN_PROFILE_CONNECTEED_SIZE = "bluetooth_pan_profile_connected_size";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_WIFI_REPEATER_CLIENTS_SIZE = "wifi_repeater_clients_size";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String IDLE_WIFI_TETHER_ALARM_TAG = "HwCustTethering IDLE_WIFI_TETHER";
    private static final int IDLE_WIFI_TETHER_DELAY = 600000;
    private static final int NOTIFICATION_TYPE_BLUETOOTH = 2;
    private static final int NOTIFICATION_TYPE_MULTIPLE = 4;
    private static final int NOTIFICATION_TYPE_NONE = -1;
    private static final int NOTIFICATION_TYPE_P2P = 3;
    private static final int NOTIFICATION_TYPE_STOP_AP = 5;
    private static final int NOTIFICATION_TYPE_USB = 1;
    private static final int NOTIFICATION_TYPE_WIFI = 0;
    private static final int POWER_OFF = 0;
    private static final String START_TETHER_ACTION = "com.huawei.server.connectivity.action.START_TETHER";
    private static final String START_TETHER_PERMISSION = "com.android.server.connectivity.permission.START_TETHERING";
    private static final String TAG = "HwCustTethering";
    private static final String WIFI_REPEATER_CLIENTS_CHANGED_ACTION = "com.huawei.wifi.action.WIFI_REPEATER_CLIENTS_CHANGED";
    private static final Object tetheredLock = new Object();
    /* access modifiers changed from: private */
    public AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public int mBluetoothUsbNum;
    /* access modifiers changed from: private */
    public Context mContext;
    AlarmManager.OnAlarmListener mIdleApAlarmListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            boolean unused = HwNotificationTetheringImpl.this.mSetIdleAlarm = false;
            if (HwNotificationTetheringImpl.this.mPluggedType == 0) {
                HwNotificationTetheringImpl.this.stopTethering(0);
                Log.d(HwNotificationTetheringImpl.TAG, "WIFI_TETHER was in idle for 10 minutes, stop tethering, show notification");
                HwNotificationTetheringImpl.this.showStopWifiTetherNotification();
            }
        }
    };
    private NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public int mP2pConnectNum;
    /* access modifiers changed from: private */
    public boolean mP2pTethered;
    private PendingIntent mPi;
    /* access modifiers changed from: private */
    public int mPluggedType = -1;
    /* access modifiers changed from: private */
    public volatile boolean mSetIdleAlarm = false;
    /* access modifiers changed from: private */
    public OnStartTetheringCallback mStartTetheringCallback;
    private Notification mStopApNotification;
    private boolean mSupportWifiRepeater;
    /* access modifiers changed from: private */
    public Notification mTetheredNotification;
    private int[] mTetheredRecord = {0, 0, 0, 0};
    private int mTotalNum;
    /* access modifiers changed from: private */
    public int mWifiConnectNum;
    /* access modifiers changed from: private */
    public boolean mWifiTetherd;

    private class BluetoothPanProfileConnectNumReceiver extends BroadcastReceiver {
        private BluetoothPanProfileConnectNumReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int bluetoothConnectNum = intent.getIntExtra(HwNotificationTetheringImpl.EXTRA_BLUETOOTH_PAN_PROFILE_CONNECTEED_SIZE, 0);
            if (HwNotificationTetheringImpl.DBG) {
                Log.d(HwNotificationTetheringImpl.TAG, "BluetoothPanProfileConnectNumReceiver bluetoothConnectNum = " + bluetoothConnectNum);
            }
            if (HwNotificationTetheringImpl.this.mTetheredNotification != null && HwNotificationTetheringImpl.this.mBluetoothUsbNum != bluetoothConnectNum) {
                int unused = HwNotificationTetheringImpl.this.mBluetoothUsbNum = bluetoothConnectNum;
                HwNotificationTetheringImpl.this.showTetheredNotificationWithNumbers(2);
            }
        }
    }

    static final class OnStartTetheringCallback extends ConnectivityManager.OnStartTetheringCallback {
        OnStartTetheringCallback() {
        }

        public void onTetheringStarted() {
        }

        public void onTetheringFailed() {
            Log.e(HwNotificationTetheringImpl.TAG, "WIFI tethering FAILED!");
        }
    }

    private class P2pConnectNumReceiver extends BroadcastReceiver {
        private P2pConnectNumReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.d(HwNotificationTetheringImpl.TAG, "onReceive: " + intent.getAction());
            int p2pConnectNum = intent.getIntExtra(HwNotificationTetheringImpl.EXTRA_WIFI_REPEATER_CLIENTS_SIZE, 0);
            if (HwNotificationTetheringImpl.this.mTetheredNotification != null && HwNotificationTetheringImpl.this.mP2pTethered && HwNotificationTetheringImpl.this.mP2pConnectNum != p2pConnectNum) {
                int unused = HwNotificationTetheringImpl.this.mP2pConnectNum = p2pConnectNum;
                HwNotificationTetheringImpl.this.showTetheredNotificationWithNumbers(3);
            }
        }
    }

    private class WifiConnectNumReceiver extends BroadcastReceiver {
        private WifiConnectNumReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.d(HwNotificationTetheringImpl.TAG, "onReceive: " + intent.getAction());
            int wifiConnectNum = intent.getIntExtra(HwNotificationTetheringImpl.EXTRA_STA_COUNT, 0);
            if (HwNotificationTetheringImpl.this.mTetheredNotification != null && HwNotificationTetheringImpl.this.mWifiTetherd && HwNotificationTetheringImpl.this.mWifiConnectNum != wifiConnectNum) {
                int unused = HwNotificationTetheringImpl.this.mWifiConnectNum = wifiConnectNum;
                HwNotificationTetheringImpl.this.showTetheredNotificationWithNumbers(0);
            }
        }
    }

    public HwNotificationTetheringImpl(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        initConfigs();
        registerConnectNumReceiver();
        registerStartTetherReceiver();
        if (!isSoftApCust()) {
            registerPluggedTypeReceiver();
        }
    }

    private void initConfigs() {
        this.mSupportWifiRepeater = SystemProperties.getBoolean("ro.config.hw_wifibridge", false);
    }

    private void registerConnectNumReceiver() {
        if (this.mSupportWifiRepeater) {
            this.mContext.registerReceiver(new P2pConnectNumReceiver(), new IntentFilter(WIFI_REPEATER_CLIENTS_CHANGED_ACTION));
        }
        BroadcastReceiver receiver = new WifiConnectNumReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WIFI_AP_STA_JOIN);
        filter.addAction(ACTION_WIFI_AP_STA_LEAVE);
        this.mContext.registerReceiver(receiver, filter);
        this.mContext.registerReceiver(new BluetoothPanProfileConnectNumReceiver(), new IntentFilter(ACTION_CONNECTION_STATE_CHANGED));
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
                    Log.d(HwNotificationTetheringImpl.TAG, "new plugged:" + temp + ", current plugged:" + HwNotificationTetheringImpl.this.mPluggedType);
                    if (HwNotificationTetheringImpl.this.mPluggedType != temp) {
                        int unused = HwNotificationTetheringImpl.this.mPluggedType = temp;
                        if (HwNotificationTetheringImpl.this.mPluggedType != 0) {
                            boolean unused2 = HwNotificationTetheringImpl.this.mSetIdleAlarm = false;
                            HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleApAlarmListener);
                        } else if (HwNotificationTetheringImpl.this.mWifiTetherd && HwNotificationTetheringImpl.this.mWifiConnectNum <= 0) {
                            Log.d(HwNotificationTetheringImpl.TAG, "reset IDLE_WIFI_TETHER alarm");
                            HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleApAlarmListener);
                            boolean unused3 = HwNotificationTetheringImpl.this.mSetIdleAlarm = true;
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
                this.mNotificationManager.cancelAsUser(null, tempTetheredNotification.icon, UserHandle.ALL);
                CharSequence title = null;
                CharSequence message = null;
                if (this.mWifiConnectNum > 0 && this.mBluetoothUsbNum == 0) {
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

    @Deprecated
    public int getTetheredIcon(boolean usbTethered, boolean wifiTethered, boolean bluetoothTethered, boolean p2pTethered) {
        return 0;
    }

    @Deprecated
    public void setTetheringNumber(boolean wifiTethered, boolean usbTethered, boolean bluetoothTethered) {
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
        boolean wifiTethered = tetheringNumbers.contains(DevSchedFeatureRT.WIFI_FEATURE);
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

    @Deprecated
    public boolean sendTetherNotification(Notification tetheredNotification, CharSequence title, CharSequence message, PendingIntent pi) {
        return false;
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
            this.mSetIdleAlarm = false;
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

    /* access modifiers changed from: private */
    public void stopTethering(int type) {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (this.mTetheredRecord[type] == 1) {
            cm.stopTethering(type);
        }
    }

    public int getNotificationType(List<String> tetheringNumbers) {
        int type = -1;
        int tetheredTypes = 0;
        resetTetheredRecord();
        boolean wifiTethered = tetheringNumbers.contains(DevSchedFeatureRT.WIFI_FEATURE);
        boolean usbTethered = tetheringNumbers.contains("usb");
        boolean bluetoothTethered = tetheringNumbers.contains("bluetooth");
        boolean p2pTethered = tetheringNumbers.contains("p2p");
        if (wifiTethered) {
            type = 0;
            tetheredTypes = 0 + 1;
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
                return 17303548;
            case 1:
                return 33751226;
            case 2:
                return 33751224;
            case 3:
                return 17303548;
            case 4:
                return 33751225;
            case 5:
                return 33752006;
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
            return r.getText(33686233);
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
        if (notificationType == 0) {
            return r.getText(33685852);
        }
        if (notificationType == 3) {
            return r.getText(33685851);
        }
        if (notificationType != 5) {
            return r.getText(33685849);
        }
        return r.getText(33686231);
    }

    public Intent getNotificationIntent(int notificationType) {
        Intent intent = new Intent();
        if (notificationType == 0) {
            intent.setAction("android.settings.WIFI_AP_SETTINGS");
        } else if (notificationType != 3) {
            intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        } else {
            intent.setAction("android.settings.WIFI_BRIDGE_SETTINGS");
        }
        return intent;
    }

    public void showTetheredNotification(int notificationType, Notification notification, PendingIntent pi) {
        this.mTetheredNotification = notification;
        this.mPi = pi;
        showTetheredNotificationWithNumbers(notificationType);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c7, code lost:
        return;
     */
    public void showTetheredNotificationWithNumbers(int notificationType) {
        CharSequence message;
        synchronized (tetheredLock) {
            if (this.mTetheredNotification != null) {
                this.mNotificationManager.cancelAsUser(null, this.mTetheredNotification.icon, UserHandle.ALL);
                if (this.mWifiTetherd) {
                    clearStopApNotification();
                    if (this.mWifiConnectNum > 0) {
                        Log.d(TAG, "set mSetIdleAlarm false, mWifiConnectNum is " + this.mWifiConnectNum);
                        this.mSetIdleAlarm = false;
                        this.mAlarmManager.cancel(this.mIdleApAlarmListener);
                    } else if (!isSoftApCust() && !this.mSetIdleAlarm) {
                        Log.d(TAG, "set IDLE_WIFI_TETHER alarm, set mSetIdleAlarm:true");
                        this.mSetIdleAlarm = true;
                        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME, IDLE_WIFI_TETHER_ALARM_TAG, this.mIdleApAlarmListener, null);
                    }
                }
                Log.d(TAG, "showTetheredNotificationWithNumbers");
                if (isConfigR1()) {
                    this.mTetheredNotification.flags = 32;
                    if (this.mWifiConnectNum <= 0) {
                        message = Resources.getSystem().getText(33686235);
                    } else {
                        message = getNotificationMessageWithNumbers(notificationType);
                    }
                    notifyNotification(notificationType, message);
                } else if (this.mWifiConnectNum > 0) {
                    notifyNotification(notificationType, getNotificationMessageWithNumbers(notificationType));
                }
                if (this.mP2pConnectNum <= 0) {
                    if (this.mBluetoothUsbNum <= 0) {
                        if (notificationType == 3) {
                            notifyNotification(notificationType, Resources.getSystem().getText(33685848));
                        }
                    }
                }
                notifyNotification(notificationType, getNotificationMessageWithNumbers(notificationType));
            }
        }
    }

    private void notifyNotification(int notificationType, CharSequence message) {
        int selectType = notificationType;
        this.mTetheredNotification.setLatestEventInfo(this.mContext, getNotificationTitle(selectType), message, this.mPi);
        this.mTetheredNotification.actions = getNotificationAction(selectType);
        this.mNotificationManager.notifyAsUser(null, this.mTetheredNotification.icon, this.mTetheredNotification, UserHandle.ALL);
    }

    private boolean isConfigR1() {
        return SystemProperties.get("ro.config.hw_opta", "0").equals("389") && SystemProperties.get("ro.config.hw_optb", "0").equals("840");
    }

    /* access modifiers changed from: private */
    public synchronized void showStopWifiTetherNotification() {
        PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, getNotificationIntent(0), 0, null, UserHandle.CURRENT);
        PendingIntent pIntentCancel = PendingIntent.getBroadcast(this.mContext, 0, new Intent(START_TETHER_ACTION), 134217728);
        Resources resource = Resources.getSystem();
        CharSequence title = getNotificationTitle(5);
        CharSequence message = resource.getText(33686232);
        CharSequence action_text = getNotificationActionText(5);
        int icon = getNotificationIcon(5);
        Notification.Builder stopApNotificationBuilder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS);
        stopApNotificationBuilder.setWhen(0).setOngoing(false).setVisibility(1).setCategory("status").addAction(new Notification.Action(0, action_text, pIntentCancel)).setSmallIcon(icon).setContentTitle(title).setContentText(message).setStyle(new Notification.BigTextStyle().bigText(message)).setContentIntent(pi).setAutoCancel(true);
        this.mStopApNotification = stopApNotificationBuilder.build();
        this.mNotificationManager.notifyAsUser(null, this.mStopApNotification.icon, this.mStopApNotification, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    public synchronized void clearStopApNotification() {
        if (this.mStopApNotification != null) {
            this.mNotificationManager.cancelAsUser(null, this.mStopApNotification.icon, UserHandle.ALL);
            this.mStopApNotification = null;
        }
    }

    private Notification.Action[] getNotificationAction(int notificationType) {
        return new Notification.Action[]{new Notification.Action(0, getNotificationActionText(notificationType), PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.server.connectivity.action.STOP_TETHERING"), 134217728))};
    }

    private boolean isSoftApCust() {
        if (SystemProperties.getBoolean("ro.config.check_hotspot_status", false) || "true".equals(Settings.Global.getString(this.mContext.getContentResolver(), "hotspot_power_mode_on"))) {
            return true;
        }
        return false;
    }
}
