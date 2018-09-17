package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.util.HwLog;
import android.util.Log;
import com.huawei.android.net.wifi.WifiManagerCommonEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.dubai.client.BufferedLogClient;
import com.huawei.dubai.client.BufferedLogClient.BufferLogCallback;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import java.io.File;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class HwDubaiService extends SystemService {
    private static final int BUFFERED_LOG_MAGIC_DAILY_STATISTICS = 0;
    private static final int BUFFERED_LOG_MAGIC_MOBILEPHONE = 1;
    private static final String BUFFERED_LOG_TYPE = "DubaiService";
    private static final boolean DEBUG = false;
    private static final String TAG = "HwDubaiService";
    private BufferedLogClient mClient;
    private Context mContext;
    private BufferedLogHandler mLogHandler;
    private PGPlug mPGPlug;
    private PgEventProcesser mPgEventProcesser = new PgEventProcesser(this, null);
    private MobilePhoneStateListener[] mPhoneStateListeners = null;
    private Object mPhoneStateLock = new Object();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwLog.dubaie("DUBAI_TAG_BOOT_COMPLETED", "");
                    HwDubaiService.this.initPgPlugThread();
                    HwDubaiService.this.initBufferedLogClient();
                    HwDubaiService.this.initAudioParameters();
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    HwDubaiService.this.getRemovedPackage(intent);
                } else if ("android.intent.action.UID_REMOVED".equals(action)) {
                    HwDubaiService.this.getRemovedUid(intent);
                } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    HwDubaiService.this.getAddedPackage(intent);
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    HwDubaiService.this.notifyShutDown();
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    HwDubaiService.this.getWifiInfo(intent);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    HwDubaiService.this.getWifiState(intent);
                } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                    HwDubaiService.this.getWifiApState(intent);
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    HwDubaiService.this.getWifiSignalLevel(intent);
                } else if ("android.media.STREAM_DEVICES_CHANGED_ACTION".equals(action)) {
                    HwDubaiService.this.getAudioDevice(intent);
                } else if ("android.media.VOLUME_CHANGED_ACTION".equals(action)) {
                    HwDubaiService.this.getAudioVolume(intent);
                } else if ("android.intent.action.HEADSET_PLUG".equals(action)) {
                    HwDubaiService.this.getAudioHeadset(intent);
                }
                Log.i(HwDubaiService.TAG, "BroadcastReceiver " + action);
            }
        }
    };
    private StorageManager mStorageManager = null;
    OnSubscriptionsChangedListener mSubChgListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            if (HwDubaiService.this.mSubscriptionManager != null) {
                if (HwDubaiService.this.mSubscriptionManager.getActiveSubscriptionInfoList() != null) {
                    HwDubaiService.this.unregisterPhoneListener();
                    HwDubaiService.this.registerPhoneListener();
                } else {
                    HwDubaiService.this.unregisterPhoneListener();
                }
            }
        }
    };
    private SubscriptionManager mSubscriptionManager = null;
    private TelephonyManager mTelephonyManager = null;
    private int mWifiSignalLevel = 0;

    private class BufferedLogHandler implements BufferLogCallback {
        /* synthetic */ BufferedLogHandler(HwDubaiService this$0, BufferedLogHandler -this1) {
            this();
        }

        private BufferedLogHandler() {
        }

        public void onConnectionSuccess() {
        }

        public void onConnectionFailed() {
            Log.e(HwDubaiService.TAG, "connect to server failed, reconnecting...");
            HwDubaiService.this.mClient.startToListen();
        }

        public void onLogReceived(int magic, Parcel parcel) {
            Log.i(HwDubaiService.TAG, "receive buffered log, magic=" + magic);
            switch (magic) {
                case 0:
                    HwDubaiService.this.getDailyStatistics(parcel);
                    return;
                case 1:
                    HwDubaiService.this.getNetworkData(parcel);
                    return;
                default:
                    Log.e(HwDubaiService.TAG, "Invalid buffered log magic");
                    return;
            }
        }
    }

    static class MobilePhoneStateListener extends PhoneStateListener {
        int mDataNetworkSwitchs = 0;
        int mLastDataNetworkType = 0;
        int mLastPhoneState = 0;
        int mLastVoiceNetworkType = 0;
        Object mLock = new Object();
        final int mMcc;
        final int mMnc;
        int mVoiceNetworkSwitchs = 0;

        public MobilePhoneStateListener(int subId, int mcc, int mnc) {
            super(Integer.valueOf(subId));
            this.mMcc = mcc;
            this.mMnc = mnc;
        }

        public void writeToParcel(Parcel dest) {
            if (dest != null) {
                synchronized (this.mLock) {
                    dest.writeInt(this.mSubId.intValue());
                    dest.writeInt(this.mMcc);
                    dest.writeInt(this.mMnc);
                    dest.writeInt(this.mDataNetworkSwitchs);
                    dest.writeInt(this.mVoiceNetworkSwitchs);
                }
            }
        }

        public void clear() {
            synchronized (this.mLock) {
                this.mDataNetworkSwitchs = 0;
                this.mVoiceNetworkSwitchs = 0;
            }
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state != null) {
                int voiceNetWorkType = state.getVoiceNetworkType();
                int dataNetWorkType = state.getDataNetworkType();
                synchronized (this.mLock) {
                    if (this.mLastVoiceNetworkType != voiceNetWorkType) {
                        this.mVoiceNetworkSwitchs++;
                        this.mLastVoiceNetworkType = voiceNetWorkType;
                    }
                    if (this.mLastDataNetworkType != dataNetWorkType) {
                        this.mDataNetworkSwitchs++;
                        this.mLastDataNetworkType = dataNetWorkType;
                    }
                }
            }
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            synchronized (this.mLock) {
                if (state != this.mLastPhoneState) {
                    HwLog.dubaie("DUBAI_TAG_PHONE_STATE", "subId=" + this.mSubId + " state=" + state);
                    this.mLastPhoneState = state;
                }
            }
        }
    }

    private class PgEventProcesser implements IPGPlugCallbacks {
        /* synthetic */ PgEventProcesser(HwDubaiService this$0, PgEventProcesser -this1) {
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
                HwDubaiService.this.getForegroundPackage(msg);
            }
            return true;
        }
    }

    public void onStart() {
        Log.i(TAG, "start HwDubaiService");
        initBroadcastReceiver();
    }

    public HwDubaiService(Context context) {
        super(context);
        Log.i(TAG, TAG);
        this.mContext = context;
        this.mStorageManager = (StorageManager) context.getSystemService("storage");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mSubscriptionManager = SubscriptionManager.from(context);
        if (this.mSubscriptionManager != null) {
            this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubChgListener);
        }
        registerPhoneListener();
    }

    private void registerPhoneListener() {
        if (this.mSubscriptionManager != null && this.mTelephonyManager != null) {
            List<SubscriptionInfo> subscriptions = this.mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptions != null && subscriptions.size() > 0) {
                synchronized (this.mPhoneStateLock) {
                    int phoneNum = subscriptions.size();
                    this.mPhoneStateListeners = new MobilePhoneStateListener[phoneNum];
                    for (int i = 0; i < phoneNum; i++) {
                        SubscriptionInfo subscriptionInfo = (SubscriptionInfo) subscriptions.get(i);
                        this.mPhoneStateListeners[i] = new MobilePhoneStateListener(subscriptionInfo.getSubscriptionId(), subscriptionInfo.getMcc(), subscriptionInfo.getMnc());
                        this.mTelephonyManager.listen(this.mPhoneStateListeners[i], 33);
                    }
                }
            }
        }
    }

    private void unregisterPhoneListener() {
        if (this.mTelephonyManager != null) {
            synchronized (this.mPhoneStateLock) {
                if (this.mPhoneStateListeners != null && this.mPhoneStateListeners.length > 0) {
                    int number = this.mPhoneStateListeners.length;
                    for (int index = 0; index < number; index++) {
                        if (this.mPhoneStateListeners[index] != null) {
                            this.mTelephonyManager.listen(this.mPhoneStateListeners[index], 0);
                            this.mPhoneStateListeners[index] = null;
                        }
                    }
                    this.mPhoneStateListeners = null;
                }
            }
        }
    }

    private void getNetworkData(Parcel parcel) {
        long timestamp = parcel.readLong();
        Parcel data = Parcel.obtain();
        int size = 0;
        data.writeLong(timestamp);
        synchronized (this.mPhoneStateLock) {
            if (this.mPhoneStateListeners != null && this.mPhoneStateListeners.length > 0) {
                size = this.mPhoneStateListeners.length;
            }
            data.writeInt(size);
            for (int i = 0; i < size; i++) {
                if (this.mPhoneStateListeners[i] != null) {
                    this.mPhoneStateListeners[i].writeToParcel(data);
                    this.mPhoneStateListeners[i].clear();
                }
            }
        }
        Log.i(TAG, "getNetworkData size=" + size);
        this.mClient.sendBufferedLog(1, data);
        data.recycle();
    }

    private void initBroadcastReceiver() {
        initGenericReceiver();
        initPackageReceiver();
    }

    private void initGenericReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.UID_REMOVED");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.RSSI_CHANGED");
        filter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        filter.addAction("android.intent.action.HEADSET_PLUG");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPackageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void initPgPlugThread() {
        this.mPGPlug = new PGPlug(this.mPgEventProcesser, TAG);
        new Thread(this.mPGPlug, TAG).start();
    }

    private void initBufferedLogClient() {
        this.mLogHandler = new BufferedLogHandler(this, null);
        this.mClient = new BufferedLogClient(this.mLogHandler, BUFFERED_LOG_TYPE);
        this.mClient.startToListen();
    }

    private void getDailyStatistics(Parcel parcel) {
        int power;
        long internalFreeBytes = 0;
        long internalAvailableBytes = 0;
        boolean sdcard = false;
        boolean usb = false;
        long timestamp = parcel.readLong();
        if (this.mStorageManager != null) {
            List<StorageVolume> list = this.mStorageManager.getStorageVolumes();
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                StorageVolume vol = (StorageVolume) list.get(i);
                if (vol.isPrimary()) {
                    File file = new File(vol.getPath());
                    if (file.exists()) {
                        try {
                            StatFs fsData = new StatFs(vol.getPath());
                            internalFreeBytes = fsData.getFreeBytes();
                            internalAvailableBytes = fsData.getAvailableBytes();
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Exception: " + e + " exist: " + file.exists());
                        }
                    } else {
                        Log.i(TAG, "Path not exist: " + vol.getPath());
                    }
                } else if (vol.isRemovable()) {
                    String uuid = vol.getUuid();
                    if (uuid != null) {
                        VolumeInfo volume = this.mStorageManager.findVolumeByUuid(uuid);
                        if (volume != null) {
                            DiskInfo disk = volume.getDisk();
                            if (disk != null) {
                                sdcard = disk.isSd();
                                usb = disk.isUsb();
                            }
                        }
                    }
                }
            }
        } else {
            this.mStorageManager = (StorageManager) this.mContext.getSystemService("storage");
            Log.e(TAG, "storage is null!");
        }
        int wlan = Global.getInt(this.mContext.getContentResolver(), "wifi_sleep_policy", 2);
        int mobile = System.getInt(this.mContext.getContentResolver(), "power_saving_on", 0);
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            power = 2;
        } else {
            power = System.getInt(this.mContext.getContentResolver(), "SmartModeStatus", 1);
        }
        Parcel data = Parcel.obtain();
        data.writeLong(timestamp);
        data.writeLong(internalFreeBytes);
        data.writeLong(internalAvailableBytes);
        data.writeInt(sdcard ? 1 : 0);
        data.writeInt(usb ? 1 : 0);
        data.writeInt(wlan);
        data.writeInt(mobile == 0 ? 1 : 0);
        data.writeInt(power);
        this.mClient.sendBufferedLog(0, data);
        data.recycle();
    }

    private void getForegroundPackage(String msg) {
        try {
            String[] splits = msg.split("\t");
            if (splits.length > 0) {
                HwLog.dubaie("DUBAI_TAG_FOREGROUND_CHANGED", "name=" + splits[0]);
            }
        } catch (PatternSyntaxException e) {
            Log.e(TAG, "Exception: " + e.toString());
        }
    }

    private void getRemovedPackage(Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        Bundle intentExtras = intent.getExtras();
        HwLog.dubaie("DUBAI_TAG_PACKAGE_REMOVED", "uid=" + (intentExtras != null ? intentExtras.getInt("android.intent.extra.UID") : -1) + " name=" + packageName);
    }

    private void getRemovedUid(Intent intent) {
        Bundle intentExtras = intent.getExtras();
        HwLog.dubaie("DUBAI_TAG_UID_REMOVED", "uid=" + (intentExtras != null ? intentExtras.getInt("android.intent.extra.UID") : -1));
    }

    private void getAddedPackage(Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        Bundle intentExtras = intent.getExtras();
        HwLog.dubaie("DUBAI_TAG_PACKAGE_ADDED", "uid=" + (intentExtras != null ? intentExtras.getInt("android.intent.extra.UID") : -1) + " name=" + packageName);
    }

    private void notifyShutDown() {
        HwLog.dubaie("DUBAI_TAG_SHUTDOWN", "status=1");
    }

    private void getWifiInfo(Intent intent) {
        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (netInfo == null) {
            return;
        }
        if (netInfo.isConnected()) {
            WifiInfo wifiInfo = (WifiInfo) intent.getParcelableExtra("wifiInfo");
            if (wifiInfo != null) {
                int band = 0;
                if (wifiInfo.is5GHz()) {
                    band = 1;
                } else if (wifiInfo.is24GHz()) {
                    band = 2;
                }
                HwLog.dubaie("DUBAI_TAG_WIFI_BAND", "band=" + band);
                return;
            }
            return;
        }
        HwLog.dubaie("DUBAI_TAG_WIFI_ACTIVITY", "activity=0");
    }

    private void getWifiState(Intent intent) {
        int state = intent.getIntExtra("wifi_state", 1);
        if (state == 3) {
            HwLog.dubaie("DUBAI_TAG_WIFI_ON", "");
        } else if (state == 1) {
            HwLog.dubaie("DUBAI_TAG_WIFI_OFF", "");
        }
    }

    private void getWifiApState(Intent intent) {
        int state = intent.getIntExtra("wifi_state", 11);
        if (state == 13) {
            HwLog.dubaie("DUBAI_TAG_WIFI_AP_ON", "");
        } else if (state == 11) {
            HwLog.dubaie("DUBAI_TAG_WIFI_AP_OFF", "");
        }
    }

    private void getWifiSignalLevel(Intent intent) {
        int level;
        int rssi = intent.getIntExtra("newRssi", -127);
        try {
            level = WifiManagerCommonEx.calculateSignalLevelHW(rssi);
        } catch (NoExtAPIException e) {
            level = WifiManager.calculateSignalLevel(rssi, 5);
        }
        if (level != this.mWifiSignalLevel) {
            HwLog.dubaie("DUBAI_TAG_WIFI_SIGNAL_LEVEL", "level=" + level);
            this.mWifiSignalLevel = level;
        }
    }

    private void getAudioDevice(Intent intent) {
        int stream = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
        HwLog.dubaie("DUBAI_TAG_AUDIO_DEVICE", "stream=" + stream + " device=" + intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", 0));
    }

    private void getAudioVolume(Intent intent) {
        int stream = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
        HwLog.dubaie("DUBAI_TAG_AUDIO_VOLUME", "stream=" + stream + " volume=" + intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1));
    }

    private void getAudioHeadset(Intent intent) {
        int state = intent.getIntExtra("state", 0);
        HwLog.dubaie("DUBAI_TAG_AUDIO_HEADSET", "state=" + state + " microphone=" + intent.getIntExtra("microphone", 0));
    }

    private void initAudioParameters() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (audioManager != null) {
            int streamTypes = AudioSystem.getNumStreamTypes();
            for (int i = 0; i < streamTypes; i++) {
                int device = audioManager.getDevicesForStream(i);
                int volume = audioManager.getStreamVolume(i);
                if (device != 0) {
                    HwLog.dubaie("DUBAI_TAG_AUDIO_DEVICE", "stream=" + i + " device=" + device);
                }
                HwLog.dubaie("DUBAI_TAG_AUDIO_VOLUME", "stream=" + i + " volume=" + volume);
            }
        }
    }
}
