package com.android.server.wifi.wifinearfind;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.util.List;

public class HwWifiNearFindMonitor {
    private static final String ACTION_HICAR_STARTED = "com.huawei.hicar.ACTION_HICAR_STARTED";
    private static final String ACTION_HICAR_STOPPED = "com.huawei.hicar.ACTION_HICAR_STOPPED";
    private static final int AUDIO_RECORD_STATE_RECORDING = 3;
    private static final int AUDIO_RECORD_STATE_STOPPED = 1;
    private static final String HW_SIGNATURE_OR_SYSTEM = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final int P2P_STATE_DEFAULT_VALUE = -1;
    private static final int RECORDING_SOURCE_VOIP = 7;
    private static final String TAG = HwWifiNearFindMonitor.class.getSimpleName();
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.android.server.wifi.wifinearfind.HwWifiNearFindMonitor.AnonymousClass2 */

        public void call(Bundle extras) {
            if (extras == null) {
                Log.e(HwWifiNearFindMonitor.TAG, "AMS call, extras is null");
                return;
            }
            Object tempComp = extras.getParcelable("toActivity");
            if (!(tempComp instanceof ComponentName)) {
                Log.e(HwWifiNearFindMonitor.TAG, "AMS call, tempComp is not instance of ComponentName");
                return;
            }
            ComponentName componentName = (ComponentName) tempComp;
            String activityName = "";
            String topAppName = componentName != null ? componentName.getPackageName() : activityName;
            if (componentName != null) {
                activityName = componentName.getClassName();
            }
            boolean isHomeApp = false;
            if (HwWifiNearFindUtils.HOME_APK.equals(topAppName)) {
                isHomeApp = true;
            } else if (HwWifiNearFindUtils.HILINK_APK.equals(topAppName) && HwWifiNearFindUtils.HILINK_TOAST_ACTIVITY.equals(activityName)) {
                HwWifiNearFindChr.getInstance().reportDeviceInfoChr();
            }
            Message msg = Message.obtain();
            msg.what = 2;
            msg.arg1 = HwWifiNearFindUtils.booleanToInt(isHomeApp);
            HwWifiNearFindMonitor.this.mHandler.sendMessage(msg);
        }
    };
    private int mAudioRecordState = 1;
    private AudioManager.AudioRecordingCallback mAudioRecordingCallback = new AudioManager.AudioRecordingCallback() {
        /* class com.android.server.wifi.wifinearfind.HwWifiNearFindMonitor.AnonymousClass1 */

        @Override // android.media.AudioManager.AudioRecordingCallback
        public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
            if (!HwWifiNearFindMonitor.this.isRecordingInVoip(configs)) {
                if (HwWifiNearFindMonitor.this.mAudioRecordState == 3) {
                    HwWifiNearFindMonitor.this.mAudioRecordState = 1;
                    HwWifiNearFindMonitor.this.mHandler.sendEmptyMessage(6);
                }
            } else if (HwWifiNearFindMonitor.this.mAudioRecordState == 1) {
                HwWifiNearFindMonitor.this.mAudioRecordState = 3;
                HwWifiNearFindMonitor.this.mHandler.sendEmptyMessage(7);
            }
        }
    };
    private Context mContext;
    private Handler mHandler;

    public HwWifiNearFindMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        registerActivityObserver();
        registerActionReceiver();
        registerAudioStateListener();
        registerHwShareObserver();
        Log.d(TAG, "init HwWifiNearFindMonitor success");
    }

    private void registerAudioStateListener() {
        AudioManager.AudioRecordingCallback audioRecordingCallback;
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "registerAudioStateListener, mContext is null");
            return;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (audioManager == null || (audioRecordingCallback = this.mAudioRecordingCallback) == null) {
            Log.e(TAG, "registerAudioStateListener, audioManager or mAudioRecordingCallback is null");
        } else {
            audioManager.registerAudioRecordingCallback(audioRecordingCallback, null);
        }
    }

    private void registerActionReceiver() {
        if (this.mContext == null) {
            Log.e(TAG, "registerActionReceiver, mContext is null");
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        ActionReceiver actionReceiver = new ActionReceiver();
        this.mContext.registerReceiver(actionReceiver, filter);
        IntentFilter hicarFilter = new IntentFilter();
        hicarFilter.addAction(ACTION_HICAR_STARTED);
        hicarFilter.addAction(ACTION_HICAR_STOPPED);
        this.mContext.registerReceiver(actionReceiver, hicarFilter, HW_SIGNATURE_OR_SYSTEM, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRecordingInVoip(List<AudioRecordingConfiguration> configs) {
        if (configs != null && configs.stream().filter($$Lambda$HwWifiNearFindMonitor$KxLbfA0mC46MDdgC8rmQRsu_L1s.INSTANCE).count() > 0) {
            return true;
        }
        return false;
    }

    static /* synthetic */ boolean lambda$isRecordingInVoip$0(AudioRecordingConfiguration config) {
        return config != null && config.getClientAudioSource() == 7;
    }

    private void registerActivityObserver() {
        IHwActivityNotifierEx iHwActivityNotifierEx = this.mActivityNotifierEx;
        if (iHwActivityNotifierEx == null) {
            Log.e(TAG, "registerActivityObserver, mActivityNotifierEx is null");
        } else {
            ActivityManagerEx.registerHwActivityNotifier(iHwActivityNotifierEx, "appSwitch");
        }
    }

    private void registerHwShareObserver() {
        Context context = this.mContext;
        if (context == null || this.mHandler == null) {
            Log.e(TAG, "registerHwShareObserver, mContext or mHandler is null");
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            Log.e(TAG, "registerHwShareObserver, contentResolver is null");
            return;
        }
        Uri hwShareUri = Settings.Global.getUriFor(HwWifiNearFindUtils.KEY_HWSHARE_INIT_SETTINGS);
        if (hwShareUri == null) {
            Log.e(TAG, "registerHwShareObserver, hwShareUri is null");
        } else {
            contentResolver.registerContentObserver(hwShareUri, false, new HwShareObserver(this.mHandler));
        }
    }

    /* access modifiers changed from: private */
    public class ActionReceiver extends BroadcastReceiver {
        private boolean mIsP2pConnected;

        private ActionReceiver() {
            this.mIsP2pConnected = false;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwWifiNearFindMonitor.TAG, "onReceive, intent is null");
                return;
            }
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwWifiNearFindMonitor.this.mHandler.sendEmptyMessage(4);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwWifiNearFindMonitor.this.mHandler.sendEmptyMessage(3);
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                HwWifiNearFindMonitor.this.mHandler.sendEmptyMessage(13);
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action) || "android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                handleP2pConnectionChanged(intent);
            } else if (HwWifiNearFindMonitor.ACTION_HICAR_STARTED.equals(action)) {
                HwWifiNearFindMonitor.this.mHandler.sendEmptyMessage(8);
            } else if (HwWifiNearFindMonitor.ACTION_HICAR_STOPPED.equals(action)) {
                HwWifiNearFindMonitor.this.mHandler.sendEmptyMessage(9);
            } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                handleWifiStateChanged(intent);
            } else {
                String str = HwWifiNearFindMonitor.TAG;
                Log.d(str, "onReceive, unknown action is " + action);
            }
        }

        private void handleP2pConnectionChanged(Intent intent) {
            if (intent == null) {
                Log.e(HwWifiNearFindMonitor.TAG, "handleP2pConnectionChanged, intent is null");
                return;
            }
            String action = intent.getAction();
            boolean isCurrentP2pConnected = false;
            if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (p2pNetworkInfo != null) {
                    isCurrentP2pConnected = p2pNetworkInfo.isConnected();
                } else {
                    isCurrentP2pConnected = false;
                }
            } else if (!"android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                String str = HwWifiNearFindMonitor.TAG;
                Log.d(str, "handleP2pConnectionChanged, unknown action is " + action);
            } else if (intent.getIntExtra("extraState", -1) == 2) {
                isCurrentP2pConnected = true;
            } else {
                isCurrentP2pConnected = false;
            }
            if (isCurrentP2pConnected != this.mIsP2pConnected) {
                this.mIsP2pConnected = isCurrentP2pConnected;
                Message msg = Message.obtain();
                msg.what = 5;
                msg.arg1 = HwWifiNearFindUtils.booleanToInt(isCurrentP2pConnected);
                HwWifiNearFindMonitor.this.mHandler.sendMessage(msg);
            }
        }

        private void handleWifiStateChanged(Intent intent) {
            if (intent == null) {
                Log.e(HwWifiNearFindMonitor.TAG, "handleWifiStateChanged, intent is null");
                return;
            }
            int wifiState = intent.getIntExtra("wifi_state", 4);
            if (wifiState == 3) {
                sendWifiStateMessage(true);
            } else if (wifiState == 1) {
                sendWifiStateMessage(false);
            }
        }

        private void sendWifiStateMessage(boolean isWifiEnabled) {
            if (HwWifiNearFindMonitor.this.mHandler == null) {
                Log.e(HwWifiNearFindMonitor.TAG, "sendWifiStateMessage, mHandler is null");
                return;
            }
            Message msg = Message.obtain();
            msg.what = 14;
            msg.arg1 = HwWifiNearFindUtils.booleanToInt(isWifiEnabled);
            HwWifiNearFindMonitor.this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class HwShareObserver extends ContentObserver {
        public HwShareObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            if (HwWifiNearFindMonitor.this.mHandler == null) {
                Log.e(HwWifiNearFindMonitor.TAG, "onChange, mHandler is null");
                return;
            }
            int hwShareState = Settings.Global.getInt(HwWifiNearFindMonitor.this.mContext.getContentResolver(), HwWifiNearFindUtils.KEY_HWSHARE_INIT_SETTINGS, -1);
            Message msg = Message.obtain();
            msg.what = 15;
            msg.arg1 = hwShareState;
            HwWifiNearFindMonitor.this.mHandler.sendMessage(msg);
        }
    }
}
