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
import android.util.wifi.HwHiLog;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.wavemapping.cons.Constant;
import java.util.List;

public class HwNotificationTetheringImpl implements HwNotificationTethering {
    private static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    private static final String ACTION_WIFI_AP_STA_JOIN = "com.huawei.net.wifi.WIFI_AP_STA_JOIN";
    private static final String ACTION_WIFI_AP_STA_LEAVE = "com.huawei.net.wifi.WIFI_AP_STA_LEAVE";
    private static final boolean DBG = HWFLOW;
    private static final String EXTRA_BLUETOOTH_PAN_PROFILE_CONNECTEED_SIZE = "bluetooth_pan_profile_connected_size";
    private static final String EXTRA_STA_COUNT = "staCount";
    private static final String EXTRA_WIFI_REPEATER_CLIENTS_SIZE = "wifi_repeater_clients_size";
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String IDLE_P2P_TETHER_ALARM_TAG = "HwCustTethering IDLE_P2P_TETHER";
    private static final String IDLE_WIFI_TETHER_ALARM_TAG = "HwCustTethering IDLE_WIFI_TETHER";
    private static final int IDLE_WIFI_TETHER_DELAY = 600000;
    private static final boolean MOBILE_NETWORK_SHARING_INTEGRATION = SystemProperties.getBoolean("ro.feature.mobile_network_sharing_integration", true);
    private static final int NOTIFICATION_TYPE_BLUETOOTH = 2;
    private static final int NOTIFICATION_TYPE_MULTIPLE = 4;
    private static final int NOTIFICATION_TYPE_NONE = -1;
    private static final int NOTIFICATION_TYPE_P2P = 3;
    private static final int NOTIFICATION_TYPE_STOP_AP = 5;
    private static final int NOTIFICATION_TYPE_USB = 1;
    private static final int NOTIFICATION_TYPE_WIFI = 0;
    private static final int POWER_OFF = 0;
    private static final String START_P2P_ACTION = "com.huawei.server.connectivity.action.START_P2P";
    private static final String START_TETHER_ACTION = "com.huawei.server.connectivity.action.START_TETHER";
    private static final String START_TETHER_PERMISSION = "com.android.server.connectivity.permission.START_TETHERING";
    private static final String TAG = "HwCustTethering";
    private static final int TETHERING_P2P = 3;
    private static final Object THEHERED_LOCK = new Object();
    private static final String WIFI_REPEATER_CLIENTS_CHANGED_ACTION = "com.huawei.wifi.action.WIFI_REPEATER_CLIENTS_CHANGED";
    private AlarmManager mAlarmManager;
    private int mBluetoothUsbNum;
    private Context mContext;
    private volatile boolean mHasSetIdleAlarm = false;
    private AlarmManager.OnAlarmListener mIdleApAlarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.connectivity.HwNotificationTetheringImpl.AnonymousClass1 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            HwNotificationTetheringImpl.this.mHasSetIdleAlarm = false;
            if (HwNotificationTetheringImpl.this.mPluggedType == 0) {
                HwNotificationTetheringImpl.this.stopTethering(0);
                HwHiLog.d(HwNotificationTetheringImpl.TAG, false, "WIFI_TETHER was in idle for 10 minutes, stop tethering, show notification", new Object[0]);
                HwNotificationTetheringImpl.this.showStopWifiTetherNotification();
            }
        }
    };
    private AlarmManager.OnAlarmListener mIdleP2pAlarmListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.connectivity.HwNotificationTetheringImpl.AnonymousClass2 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            HwNotificationTetheringImpl.this.mHasSetIdleAlarm = false;
            if (HwNotificationTetheringImpl.this.mPluggedType == 0) {
                HwNotificationTetheringImpl.this.stopTethering(3);
                Log.d(HwNotificationTetheringImpl.TAG, "P2P_TETHER was in idle for 10 minutes, stop tethering, show notification");
                HwNotificationTetheringImpl.this.showStopP2pTetherNotification();
            }
        }
    };
    private boolean mIsP2pTethered;
    private boolean mIsWifiRepeaterSupport;
    private boolean mIsWifiTetherd;
    private NotificationManager mNotificationManager;
    private int mP2pConnectNum;
    private PendingIntent mPi;
    private int mPluggedType = -1;
    private OnStartTetheringCallback mStartTetheringCallback;
    private Notification mStopApNotification;
    private Notification mStopP2pNotification;
    private Notification mTetheredNotification;
    private int[] mTetheredRecord = {0, 0, 0, 0};
    private int mTotalNum;
    private int mWifiConnectNum;

    public HwNotificationTetheringImpl(Context context) {
        this.mContext = context;
        Object notifyObject = this.mContext.getSystemService("notification");
        if (notifyObject instanceof NotificationManager) {
            this.mNotificationManager = (NotificationManager) notifyObject;
        }
        Object alarmObject = this.mContext.getSystemService("alarm");
        if (alarmObject instanceof AlarmManager) {
            this.mAlarmManager = (AlarmManager) alarmObject;
        }
        initConfigs();
        registerConnectNumReceiver();
        registerStartTetherReceiver();
        if (!isSoftApCust()) {
            registerPluggedTypeReceiver();
        }
        if (MOBILE_NETWORK_SHARING_INTEGRATION) {
            registerStartP2pReceiver();
            registerP2pPluggedTypeReceiver();
            return;
        }
        Log.d(TAG, "MOBILE_NETWORK_SHARING_INTEGRATION is false");
    }

    /* access modifiers changed from: package-private */
    public static final class OnStartTetheringCallback extends ConnectivityManager.OnStartTetheringCallback {
        OnStartTetheringCallback() {
        }

        public void onTetheringStarted() {
        }

        public void onTetheringFailed() {
            HwHiLog.e(HwNotificationTetheringImpl.TAG, false, "WIFI tethering FAILED!", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public class P2pConnectNumReceiver extends BroadcastReceiver {
        private P2pConnectNumReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwHiLog.d(HwNotificationTetheringImpl.TAG, false, "onReceive: %{public}s", new Object[]{intent.getAction()});
            int p2pConnectNum = intent.getIntExtra(HwNotificationTetheringImpl.EXTRA_WIFI_REPEATER_CLIENTS_SIZE, 0);
            if (HwNotificationTetheringImpl.this.mTetheredNotification != null && HwNotificationTetheringImpl.this.mIsP2pTethered && HwNotificationTetheringImpl.this.mP2pConnectNum != p2pConnectNum) {
                HwNotificationTetheringImpl.this.mP2pConnectNum = p2pConnectNum;
                HwNotificationTetheringImpl.this.showTetheredNotificationWithNumbers(3);
            }
        }
    }

    /* access modifiers changed from: private */
    public class WifiConnectNumReceiver extends BroadcastReceiver {
        private WifiConnectNumReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                HwHiLog.d(HwNotificationTetheringImpl.TAG, false, "onReceive: %{public}s", new Object[]{intent.getAction()});
                int wifiConnectNum = intent.getIntExtra(HwNotificationTetheringImpl.EXTRA_STA_COUNT, 0);
                if (HwNotificationTetheringImpl.this.mTetheredNotification != null && HwNotificationTetheringImpl.this.mIsWifiTetherd && HwNotificationTetheringImpl.this.mWifiConnectNum != wifiConnectNum) {
                    HwNotificationTetheringImpl.this.mWifiConnectNum = wifiConnectNum;
                    if (HwNotificationTetheringImpl.this.mWifiConnectNum != 0 || !HwNotificationTetheringImpl.ACTION_WIFI_AP_STA_JOIN.equals(intent.getAction())) {
                        HwNotificationTetheringImpl.this.showTetheredNotificationWithNumbers(0);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class BluetoothPanProfileConnectNumReceiver extends BroadcastReceiver {
        private BluetoothPanProfileConnectNumReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int bluetoothConnectNum = intent.getIntExtra(HwNotificationTetheringImpl.EXTRA_BLUETOOTH_PAN_PROFILE_CONNECTEED_SIZE, 0);
            if (HwNotificationTetheringImpl.DBG) {
                HwHiLog.d(HwNotificationTetheringImpl.TAG, false, "BluetoothPanProfileConnectNumReceiver bluetoothConnectNum = %{public}d", new Object[]{Integer.valueOf(bluetoothConnectNum)});
            }
            if (HwNotificationTetheringImpl.this.mTetheredNotification != null && HwNotificationTetheringImpl.this.mBluetoothUsbNum != bluetoothConnectNum) {
                HwNotificationTetheringImpl.this.mBluetoothUsbNum = bluetoothConnectNum;
                HwNotificationTetheringImpl.this.showTetheredNotificationWithNumbers(2);
            }
        }
    }

    private void initConfigs() {
        this.mIsWifiRepeaterSupport = SystemProperties.getBoolean("ro.config.hw_wifibridge", false);
    }

    private void registerConnectNumReceiver() {
        if (this.mIsWifiRepeaterSupport) {
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
            /* class com.android.server.connectivity.HwNotificationTetheringImpl.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (HwNotificationTetheringImpl.START_TETHER_ACTION.equals(intent.getAction())) {
                    HwHiLog.d(HwNotificationTetheringImpl.TAG, false, "receive start tether action", new Object[0]);
                    ConnectivityManager cm = null;
                    Object object = HwNotificationTetheringImpl.this.mContext.getSystemService("connectivity");
                    if (object instanceof ConnectivityManager) {
                        cm = (ConnectivityManager) object;
                    }
                    if (cm != null) {
                        cm.startTethering(0, false, HwNotificationTetheringImpl.this.mStartTetheringCallback);
                    }
                    HwNotificationTetheringImpl.this.clearStopApNotification();
                }
            }
        }, new IntentFilter(START_TETHER_ACTION), START_TETHER_PERMISSION, null);
    }

    private void registerPluggedTypeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.connectivity.HwNotificationTetheringImpl.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    int temp = intent.getIntExtra("plugged", 0);
                    HwHiLog.d(HwNotificationTetheringImpl.TAG, false, "new plugged:%{public}d, current plugged:%{public}d", new Object[]{Integer.valueOf(temp), Integer.valueOf(HwNotificationTetheringImpl.this.mPluggedType)});
                    if (HwNotificationTetheringImpl.this.mPluggedType != temp) {
                        HwNotificationTetheringImpl.this.mPluggedType = temp;
                        if (HwNotificationTetheringImpl.this.mPluggedType != 0) {
                            HwNotificationTetheringImpl.this.mHasSetIdleAlarm = false;
                            HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleApAlarmListener);
                        } else if (!HwNotificationTetheringImpl.this.mIsWifiTetherd || HwNotificationTetheringImpl.this.mWifiConnectNum > 0) {
                            HwHiLog.w(HwNotificationTetheringImpl.TAG, false, "unexpected condition when handle ACTION_BATTERY_CHANGED", new Object[0]);
                        } else {
                            HwHiLog.d(HwNotificationTetheringImpl.TAG, false, "reset IDLE_WIFI_TETHER alarm", new Object[0]);
                            HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleApAlarmListener);
                            HwNotificationTetheringImpl.this.mHasSetIdleAlarm = true;
                            HwNotificationTetheringImpl.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + Constant.MAX_TRAIN_MODEL_TIME, HwNotificationTetheringImpl.IDLE_WIFI_TETHER_ALARM_TAG, HwNotificationTetheringImpl.this.mIdleApAlarmListener, null);
                        }
                    }
                }
            }
        }, filter);
    }

    private void sendNotification(boolean shouldForceUpdate) {
        int totalNum = this.mBluetoothUsbNum + this.mWifiConnectNum;
        if (DBG) {
            HwHiLog.d(TAG, false, "sendNumberChangeNotification:%{public}d", new Object[]{Integer.valueOf(totalNum)});
        }
        if (shouldForceUpdate || this.mTotalNum != totalNum) {
            this.mTotalNum = totalNum;
            Notification tempTetheredNotification = this.mTetheredNotification;
            if (tempTetheredNotification != null) {
                this.mNotificationManager.cancelAsUser(null, tempTetheredNotification.icon, UserHandle.ALL);
                CharSequence title = null;
                CharSequence message = null;
                if (this.mWifiConnectNum > 0 && this.mBluetoothUsbNum == 0) {
                    Resources resource = Resources.getSystem();
                    title = this.mContext.getString(33685840);
                    int i = this.mWifiConnectNum;
                    message = resource.getQuantityString(34406400, i, Integer.valueOf(i));
                }
                tempTetheredNotification.tickerText = title;
                tempTetheredNotification.setLatestEventInfo(this.mContext, title, message, this.mPi);
                this.mNotificationManager.notifyAsUser(null, tempTetheredNotification.icon, tempTetheredNotification, UserHandle.ALL);
            }
        }
    }

    @Deprecated
    public int getTetheredIcon(boolean isUsbTethered, boolean isWifiTethered, boolean isBluetoothTethered, boolean isP2pTethered) {
        return 0;
    }

    @Deprecated
    public void setTetheringNumber(boolean isWifiTethered, boolean isUsbTethered, boolean isBluetoothTethered) {
    }

    public void setTetheringNumber(List<String> tetheringNumbers) {
        boolean isP2pTethered = tetheringNumbers.contains("p2p");
        if (this.mIsWifiRepeaterSupport) {
            HwHiLog.d(TAG, false, "setTetheringNumber: p2pTethered = %{public}s", new Object[]{String.valueOf(isP2pTethered)});
            this.mIsP2pTethered = isP2pTethered;
            if (!this.mIsP2pTethered) {
                this.mP2pConnectNum = 0;
            }
        }
        boolean isWifiTethered = tetheringNumbers.contains("wifi");
        boolean isUsbTethered = tetheringNumbers.contains("usb");
        boolean isBluetoothTethered = tetheringNumbers.contains("bluetooth");
        if (DBG) {
            HwHiLog.d(TAG, false, "wifiTethered:%{public}s usbTethered:%{public}s bluetoothTethered:%{public}s", new Object[]{String.valueOf(isWifiTethered), String.valueOf(isUsbTethered), String.valueOf(isBluetoothTethered)});
        }
        this.mBluetoothUsbNum = 0;
        if (isUsbTethered) {
            this.mBluetoothUsbNum = 1;
        }
        if (isBluetoothTethered) {
            this.mBluetoothUsbNum++;
        }
        this.mIsWifiTetherd = isWifiTethered;
        if (!this.mIsWifiTetherd) {
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
        synchronized (THEHERED_LOCK) {
            if (DBG) {
                HwHiLog.d(TAG, false, "clearTetheredNotification", new Object[0]);
            }
            if (!(this.mNotificationManager == null || this.mTetheredNotification == null)) {
                this.mNotificationManager.cancelAsUser(null, this.mTetheredNotification.icon, UserHandle.ALL);
            }
            this.mTetheredNotification = null;
            this.mHasSetIdleAlarm = false;
            this.mAlarmManager.cancel(this.mIdleApAlarmListener);
            if (MOBILE_NETWORK_SHARING_INTEGRATION) {
                this.mAlarmManager.cancel(this.mIdleP2pAlarmListener);
            }
        }
    }

    private void resetTetheredRecord() {
        for (int type = 0; type < 4; type++) {
            this.mTetheredRecord[type] = 0;
        }
    }

    public void stopTethering() {
        Object object = this.mContext.getSystemService("connectivity");
        ConnectivityManager cm = null;
        if (object instanceof ConnectivityManager) {
            cm = (ConnectivityManager) object;
        }
        for (int type = 0; type < 4; type++) {
            if (cm != null && this.mTetheredRecord[type] == 1) {
                cm.stopTethering(type);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopTethering(int type) {
        Object object = this.mContext.getSystemService("connectivity");
        ConnectivityManager cm = null;
        if (object instanceof ConnectivityManager) {
            cm = (ConnectivityManager) object;
        }
        if (cm != null && this.mTetheredRecord[type] == 1) {
            cm.stopTethering(type);
        }
    }

    public int getNotificationType(List<String> tetheringNumbers) {
        int type = -1;
        int tetheredTypes = 0;
        resetTetheredRecord();
        boolean isWifiTethered = tetheringNumbers.contains("wifi");
        boolean isUsbTethered = tetheringNumbers.contains("usb");
        boolean isBluetoothTethered = tetheringNumbers.contains("bluetooth");
        if (isWifiTethered) {
            type = 0;
            tetheredTypes = 0 + 1;
            this.mTetheredRecord[0] = 1;
        }
        if (isUsbTethered) {
            type = 1;
            tetheredTypes++;
            this.mTetheredRecord[1] = 1;
        }
        if (isBluetoothTethered) {
            type = 2;
            tetheredTypes++;
            this.mTetheredRecord[2] = 1;
        }
        if (tetheringNumbers.contains("p2p")) {
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
        if (notificationType == 0) {
            return 17303610;
        }
        if (notificationType == 1) {
            return 33751226;
        }
        if (notificationType == 2) {
            return 33751224;
        }
        if (notificationType == 3) {
            return 17303610;
        }
        if (notificationType == 4) {
            return 33751225;
        }
        if (notificationType != 5) {
            return 0;
        }
        return 33752210;
    }

    public CharSequence getNotificationTitle(int notificationType) {
        Resources resource = Resources.getSystem();
        if (notificationType == 3) {
            if (MOBILE_NETWORK_SHARING_INTEGRATION) {
                return resource.getText(33685601);
            }
            return resource.getText(33685850);
        } else if (notificationType == 5) {
            if (MOBILE_NETWORK_SHARING_INTEGRATION) {
                return resource.getText(33685602);
            }
            return resource.getText(33686268);
        } else if (notificationType != 0) {
            return resource.getText(33685847);
        } else {
            if (MOBILE_NETWORK_SHARING_INTEGRATION) {
                return resource.getText(33685603);
            }
            return resource.getText(33685847);
        }
    }

    private CharSequence getNotificationMessageWithNumbers(int notificationType) {
        Resources resource = Resources.getSystem();
        if (notificationType == 0) {
            int i = this.mWifiConnectNum;
            return resource.getQuantityString(34406401, i, Integer.valueOf(i));
        } else if (notificationType == 1 || notificationType == 2) {
            int i2 = this.mBluetoothUsbNum;
            return resource.getQuantityString(34406401, i2, Integer.valueOf(i2));
        } else if (notificationType != 3) {
            return resource.getText(33685848);
        } else {
            int i3 = this.mP2pConnectNum;
            return resource.getQuantityString(34406401, i3, Integer.valueOf(i3));
        }
    }

    public CharSequence getNotificationActionText(int notificationType) {
        Resources resource = Resources.getSystem();
        if (notificationType == 0) {
            return resource.getText(33685852);
        }
        if (notificationType != 3) {
            if (notificationType != 5) {
                return resource.getText(33685849);
            }
            return resource.getText(33686266);
        } else if (MOBILE_NETWORK_SHARING_INTEGRATION) {
            return resource.getText(33685852);
        } else {
            return resource.getText(33685851);
        }
    }

    public Intent getNotificationIntent(int notificationType) {
        Intent intent = new Intent();
        if (notificationType == 0) {
            intent.setAction("android.settings.WIFI_AP_SETTINGS");
        } else if (notificationType != 3) {
            intent.setAction("com.huawei.intent.action.WIFI_TETHER_SETTINGS");
        } else if (MOBILE_NETWORK_SHARING_INTEGRATION) {
            intent.setAction("android.settings.WIFI_AP_SETTINGS");
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
    /* access modifiers changed from: public */
    private void showTetheredNotificationWithNumbers(int notificationType) {
        synchronized (THEHERED_LOCK) {
            if (this.mTetheredNotification != null) {
                this.mNotificationManager.cancelAsUser(null, this.mTetheredNotification.icon, UserHandle.ALL);
                if (this.mIsWifiTetherd) {
                    clearStopApNotification();
                    if (this.mWifiConnectNum > 0) {
                        HwHiLog.d(TAG, false, "set mHasSetIdleAlarm false, mWifiConnectNum is %{public}d", new Object[]{Integer.valueOf(this.mWifiConnectNum)});
                        this.mHasSetIdleAlarm = false;
                        this.mAlarmManager.cancel(this.mIdleApAlarmListener);
                    } else if (isSoftApCust() || this.mHasSetIdleAlarm) {
                        HwHiLog.w(TAG, false, "unexpected condition when handle mIsWifiTetherd", new Object[0]);
                    } else {
                        HwHiLog.d(TAG, false, "set IDLE_WIFI_TETHER alarm, set mHasSetIdleAlarm:true", new Object[0]);
                        this.mHasSetIdleAlarm = true;
                        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + Constant.MAX_TRAIN_MODEL_TIME, IDLE_WIFI_TETHER_ALARM_TAG, this.mIdleApAlarmListener, null);
                    }
                }
                HwHiLog.d(TAG, false, "showTetheredNotificationWithNumbers", new Object[0]);
                if (MOBILE_NETWORK_SHARING_INTEGRATION && this.mIsP2pTethered) {
                    clearStopP2pNotification();
                    if (this.mP2pConnectNum > 0) {
                        Log.d(TAG, "set mHasSetIdleAlarm false, mP2pConnectNum is " + this.mP2pConnectNum);
                        this.mHasSetIdleAlarm = false;
                        this.mAlarmManager.cancel(this.mIdleP2pAlarmListener);
                    } else if (!this.mHasSetIdleAlarm) {
                        Log.d(TAG, "set IDLE_WIFI_TETHER alarm, set mHasSetIdleAlarm:true");
                        this.mHasSetIdleAlarm = true;
                        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + Constant.MAX_TRAIN_MODEL_TIME, IDLE_P2P_TETHER_ALARM_TAG, this.mIdleP2pAlarmListener, null);
                    } else {
                        Log.d(TAG, "unavaiable status");
                    }
                }
                showTetheredNotificationWithNumbersaddtion(notificationType);
            }
        }
    }

    private void showTetheredNotificationWithNumbersaddtion(int notificationType) {
        CharSequence message;
        Log.d(TAG, "showTetheredNotificationWithNumbers");
        if (isConfigR1()) {
            this.mTetheredNotification.flags = 32;
            if (this.mWifiConnectNum <= 0) {
                message = Resources.getSystem().getText(33686271);
            } else {
                message = getNotificationMessageWithNumbers(notificationType);
            }
            notifyNotification(notificationType, message);
        } else if (this.mWifiConnectNum > 0) {
            notifyNotification(notificationType, getNotificationMessageWithNumbers(notificationType));
        } else if (!MOBILE_NETWORK_SHARING_INTEGRATION || !this.mIsWifiTetherd) {
            Log.d(TAG, "no nofification");
        } else {
            notifyNotification(notificationType, Resources.getSystem().getText(33685848));
        }
        if (this.mP2pConnectNum > 0 || this.mBluetoothUsbNum > 0) {
            notifyNotification(notificationType, getNotificationMessageWithNumbers(notificationType));
        } else if (notificationType == 3) {
            notifyNotification(notificationType, Resources.getSystem().getText(33685848));
        } else {
            HwHiLog.w(TAG, false, "unexpected condition when notificationType", new Object[0]);
        }
    }

    private void notifyNotification(int notificationType, CharSequence message) {
        HwHiLog.d(TAG, false, "notifyNotification notificationType=%{public}d", new Object[]{Integer.valueOf(notificationType)});
        this.mTetheredNotification.setLatestEventInfo(this.mContext, getNotificationTitle(notificationType), message, this.mPi);
        this.mTetheredNotification.actions = getNotificationAction(notificationType);
        this.mNotificationManager.notifyAsUser(null, this.mTetheredNotification.icon, this.mTetheredNotification, UserHandle.ALL);
    }

    private boolean isConfigR1() {
        return "389".equals(SystemProperties.get("ro.config.hw_opta", "0")) && "840".equals(SystemProperties.get("ro.config.hw_optb", "0"));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void showStopWifiTetherNotification() {
        PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, getNotificationIntent(0), 0, null, UserHandle.CURRENT);
        PendingIntent intentCancel = PendingIntent.getBroadcast(this.mContext, 0, new Intent(START_TETHER_ACTION), 134217728);
        Resources resource = Resources.getSystem();
        CharSequence title = getNotificationTitle(5);
        CharSequence message = resource.getText(33686267);
        CharSequence actionText = getNotificationActionText(5);
        int icon = getNotificationIcon(5);
        Notification.Builder stopApNotificationBuilder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS);
        stopApNotificationBuilder.setWhen(0).setOngoing(false).setVisibility(1).setCategory("status").addAction(new Notification.Action(0, actionText, intentCancel)).setSmallIcon(icon).setContentTitle(title).setContentText(message).setStyle(new Notification.BigTextStyle().bigText(message)).setContentIntent(pi).setAutoCancel(true);
        this.mStopApNotification = stopApNotificationBuilder.build();
        this.mNotificationManager.notifyAsUser(null, this.mStopApNotification.icon, this.mStopApNotification, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void clearStopApNotification() {
        if (this.mStopApNotification != null) {
            this.mNotificationManager.cancelAsUser(null, this.mStopApNotification.icon, UserHandle.ALL);
            this.mStopApNotification = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void showStopP2pTetherNotification() {
        PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, getNotificationIntent(3), 0, null, UserHandle.CURRENT);
        PendingIntent intentCancel = PendingIntent.getBroadcast(this.mContext, 0, new Intent(START_P2P_ACTION), 134217728);
        Resources resource = Resources.getSystem();
        CharSequence title = getNotificationTitle(5);
        CharSequence message = resource.getText(33686267);
        CharSequence actionText = getNotificationActionText(5);
        int icon = getNotificationIcon(5);
        Notification.Builder stopApNotificationBuilder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS);
        stopApNotificationBuilder.setWhen(0).setOngoing(false).setVisibility(1).setCategory("status").addAction(new Notification.Action(0, actionText, intentCancel)).setSmallIcon(icon).setContentTitle(title).setContentText(message).setStyle(new Notification.BigTextStyle().bigText(message)).setContentIntent(pi).setAutoCancel(true);
        this.mStopP2pNotification = stopApNotificationBuilder.build();
        this.mNotificationManager.notifyAsUser(null, this.mStopP2pNotification.icon, this.mStopP2pNotification, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void clearStopP2pNotification() {
        if (this.mStopP2pNotification != null) {
            this.mNotificationManager.cancelAsUser(null, this.mStopP2pNotification.icon, UserHandle.ALL);
            this.mStopP2pNotification = null;
        }
    }

    private void registerP2pPluggedTypeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.connectivity.HwNotificationTetheringImpl.AnonymousClass5 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    int pluggedValue = intent.getIntExtra("plugged", 0);
                    Log.d(HwNotificationTetheringImpl.TAG, "P2P new plugged:" + pluggedValue + ", current plugged:" + HwNotificationTetheringImpl.this.mPluggedType);
                    if (HwNotificationTetheringImpl.this.mPluggedType != pluggedValue) {
                        HwNotificationTetheringImpl.this.mPluggedType = pluggedValue;
                        if (HwNotificationTetheringImpl.this.mPluggedType != 0) {
                            HwNotificationTetheringImpl.this.mHasSetIdleAlarm = false;
                            HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleApAlarmListener);
                        } else if (!HwNotificationTetheringImpl.this.mIsP2pTethered || HwNotificationTetheringImpl.this.mP2pConnectNum > 0) {
                            Log.d(HwNotificationTetheringImpl.TAG, "unavaiable status");
                        } else {
                            Log.d(HwNotificationTetheringImpl.TAG, "reset IDLE_P2P_TETHER alarm");
                            HwNotificationTetheringImpl.this.mAlarmManager.cancel(HwNotificationTetheringImpl.this.mIdleP2pAlarmListener);
                            HwNotificationTetheringImpl.this.mHasSetIdleAlarm = true;
                            HwNotificationTetheringImpl.this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + Constant.MAX_TRAIN_MODEL_TIME, HwNotificationTetheringImpl.IDLE_P2P_TETHER_ALARM_TAG, HwNotificationTetheringImpl.this.mIdleP2pAlarmListener, null);
                        }
                    }
                }
            }
        }, filter);
    }

    private void registerStartP2pReceiver() {
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.connectivity.HwNotificationTetheringImpl.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (HwNotificationTetheringImpl.START_P2P_ACTION.equals(intent.getAction())) {
                    Log.d(HwNotificationTetheringImpl.TAG, "receive start p2p action");
                    HwNotificationTetheringImpl.this.clearStopP2pNotification();
                }
            }
        }, new IntentFilter(START_P2P_ACTION), START_TETHER_PERMISSION, null);
    }

    private Notification.Action[] getNotificationAction(int notificationType) {
        return new Notification.Action[]{new Notification.Action(0, getNotificationActionText(notificationType), PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.server.connectivity.action.STOP_TETHERING"), 134217728))};
    }

    private boolean isSoftApCust() {
        if (SystemProperties.getBoolean("ro.config.check_hotspot_status", false) || AppActConstant.VALUE_TRUE.equals(Settings.Global.getString(this.mContext.getContentResolver(), "hotspot_power_mode_on"))) {
            return true;
        }
        return false;
    }

    public void setWifiTetherd(boolean isWifiTetherd) {
        this.mIsWifiTetherd = isWifiTetherd;
    }
}
