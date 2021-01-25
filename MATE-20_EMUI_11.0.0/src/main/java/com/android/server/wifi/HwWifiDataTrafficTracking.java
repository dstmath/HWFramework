package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.wifi.HwHiSLog;

public class HwWifiDataTrafficTracking {
    private static final String ACTION_TRACK_DATA = "com.android.server.WifiManager.action.DATA_TRAFFIC";
    private static final boolean DBG = false;
    private static final long DEFAULT_LOW_DATA_TRAFFIC_COUNTS = 3;
    private static final long DEFAULT_LOW_DATA_TRAFFIC_LINE = 1024;
    private static final long DEFAULT_UPDATE_DATA_TRAFFIC_MS = 600000;
    private static final String KEY_WIFI_DATA_TRAFFIC_TRACK = "ro.config.hw_wifitrafc";
    private static final String TAG = "HwWifiDataTrafficTracking";
    private static final int UPDATE_REQUEST = 0;
    public static final String WIFI_LOW_DATA_TRAFFIC_LINE = "wifi_low_data_traffic_line";
    public static final String WIFI_UPDATE_DATA_TRAFFIC_MS = "wifi_updata_data_traffic_ms";
    private static final boolean mFunctionEnable = SystemProperties.getBoolean(KEY_WIFI_DATA_TRAFFIC_TRACK, true);
    private AlarmManager mAlarmManager;
    private Context mContext;
    private int mCount;
    private String mInterface;
    private boolean mIsStart;
    private long mLastRxBytes;
    private long mLastTxBytes;
    private long mLowDataTrafficLine;
    private PendingIntent mUpdateIntent;
    private long mUpdateMillis;
    private Handler mWifiDataTrafficHandler;

    public HwWifiDataTrafficTracking(Context context, Looper looper) {
        if (mFunctionEnable) {
            HwHiSLog.d(TAG, false, "init", new Object[0]);
            this.mContext = context;
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
            this.mWifiDataTrafficHandler = new WifiDataTrafficHandler(looper);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_TRACK_DATA);
            this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
                /* class com.android.server.wifi.HwWifiDataTrafficTracking.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && HwWifiDataTrafficTracking.ACTION_TRACK_DATA.equals(intent.getAction())) {
                        HwWifiDataTrafficTracking.this.mWifiDataTrafficHandler.sendMessage(Message.obtain(HwWifiDataTrafficTracking.this.mWifiDataTrafficHandler, 0));
                    }
                }
            }, UserHandle.OWNER, new IntentFilter(filter), null, this.mWifiDataTrafficHandler);
            this.mUpdateIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_TRACK_DATA, (Uri) null), 0);
            loadDataTrafficUpdateTime();
            loadLowDataTrafficLine();
            this.mInterface = SystemProperties.get("wifi.interface", "wlan0");
        }
    }

    public void startTrack() {
        if (mFunctionEnable && !this.mIsStart) {
            HwHiSLog.d(TAG, false, "startTrack", new Object[0]);
            this.mAlarmManager.setExact(0, System.currentTimeMillis() + this.mUpdateMillis, this.mUpdateIntent);
            this.mIsStart = true;
            this.mLastTxBytes = 0;
            this.mLastRxBytes = 0;
            this.mCount = 0;
        }
    }

    public void stopTrack() {
        if (this.mIsStart) {
            this.mAlarmManager.cancel(this.mUpdateIntent);
            this.mIsStart = false;
            HwHiSLog.d(TAG, false, "stopTrack", new Object[0]);
        }
    }

    private class WifiDataTrafficHandler extends Handler {
        private static final int MSG_UPDATA_DATA_TAFFIC = 0;

        WifiDataTrafficHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwWifiDataTrafficTracking.this.handleUpdataDateTraffic();
            }
        }
    }

    private boolean checkDataTrafficLine() {
        long txBytes = TrafficStats.getTxBytes(this.mInterface);
        long rxBytes = TrafficStats.getRxBytes(this.mInterface);
        long txSpeed = txBytes - this.mLastTxBytes;
        long rxSpeed = rxBytes - this.mLastRxBytes;
        HwHiSLog.d(TAG, false, " txBytes:%{public}s rxBytes:%{public}s mLowDataTrafficLine:%{public}s mUpdateMillis:%{public}s", new Object[]{String.valueOf(txBytes), String.valueOf(rxBytes), String.valueOf(this.mLowDataTrafficLine), String.valueOf(this.mUpdateMillis)});
        if (this.mLastTxBytes == 0 && this.mLastRxBytes == 0) {
            this.mLastTxBytes = txBytes;
            this.mLastRxBytes = rxBytes;
            return false;
        }
        this.mLastTxBytes = txBytes;
        this.mLastRxBytes = rxBytes;
        long j = this.mLowDataTrafficLine;
        return txSpeed < j && rxSpeed < j;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdataDateTraffic() {
        HwHiSLog.d(TAG, false, "handleUpdataDateTraffic", new Object[0]);
        if (this.mIsStart) {
            if (checkDataTrafficLine()) {
                HwHiSLog.d(TAG, false, "LOW_DATA_TAFFIC mCount:%{public}d", new Object[]{Integer.valueOf(this.mCount)});
                this.mCount++;
                if (((long) this.mCount) >= DEFAULT_LOW_DATA_TRAFFIC_COUNTS) {
                    HwHiSLog.w(TAG, false, "send CMD_LOW_DATA_TAFFIC", new Object[0]);
                }
            } else {
                this.mCount = 0;
            }
            this.mAlarmManager.setExact(0, System.currentTimeMillis() + this.mUpdateMillis, this.mUpdateIntent);
        }
    }

    private void loadDataTrafficUpdateTime() {
        this.mUpdateMillis = Settings.Global.getLong(this.mContext.getContentResolver(), WIFI_UPDATE_DATA_TRAFFIC_MS, 600000);
    }

    private void loadLowDataTrafficLine() {
        this.mLowDataTrafficLine = (Settings.Global.getLong(this.mContext.getContentResolver(), WIFI_LOW_DATA_TRAFFIC_LINE, DEFAULT_LOW_DATA_TRAFFIC_LINE) * this.mUpdateMillis) / 1000;
    }
}
