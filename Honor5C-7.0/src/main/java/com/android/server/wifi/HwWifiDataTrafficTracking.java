package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Slog;

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
    private static final boolean mFunctionEnable = false;
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
    private WifiController mWifiController;
    private Handler mWifiDataTrafficHandler;

    private class WifiDataTrafficHandler extends Handler {
        private static final int MSG_UPDATA_DATA_TAFFIC = 0;

        WifiDataTrafficHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwWifiDataTrafficTracking.UPDATE_REQUEST /*0*/:
                    HwWifiDataTrafficTracking.this.handleUpdataDateTraffic();
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwWifiDataTrafficTracking.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwWifiDataTrafficTracking.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwWifiDataTrafficTracking.<clinit>():void");
    }

    public HwWifiDataTrafficTracking(Context context, WifiController wifiController, Looper looper) {
        if (mFunctionEnable) {
            Slog.d(TAG, "init");
            this.mContext = context;
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
            this.mWifiController = wifiController;
            this.mWifiDataTrafficHandler = new WifiDataTrafficHandler(looper);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_TRACK_DATA);
            this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (HwWifiDataTrafficTracking.ACTION_TRACK_DATA.equals(intent.getAction())) {
                        HwWifiDataTrafficTracking.this.mWifiDataTrafficHandler.sendMessage(Message.obtain(HwWifiDataTrafficTracking.this.mWifiDataTrafficHandler, HwWifiDataTrafficTracking.UPDATE_REQUEST));
                    }
                }
            }, UserHandle.OWNER, new IntentFilter(filter), null, this.mWifiDataTrafficHandler);
            this.mUpdateIntent = PendingIntent.getBroadcast(this.mContext, UPDATE_REQUEST, new Intent(ACTION_TRACK_DATA, null), UPDATE_REQUEST);
            loadDataTrafficUpdateTime();
            loadLowDataTrafficLine();
            this.mInterface = SystemProperties.get("wifi.interface", "wlan0");
        }
    }

    public void startTrack() {
        if (mFunctionEnable && !this.mIsStart) {
            Slog.d(TAG, "startTrack");
            this.mAlarmManager.setExact(UPDATE_REQUEST, System.currentTimeMillis() + this.mUpdateMillis, this.mUpdateIntent);
            this.mIsStart = true;
            this.mLastTxBytes = 0;
            this.mLastRxBytes = 0;
            this.mCount = UPDATE_REQUEST;
        }
    }

    public void stopTrack() {
        if (this.mIsStart) {
            this.mAlarmManager.cancel(this.mUpdateIntent);
            this.mIsStart = DBG;
            Slog.d(TAG, "stopTrack");
        }
    }

    private boolean checkDataTrafficLine() {
        long txBytes = TrafficStats.getTxBytes(this.mInterface);
        long rxBytes = TrafficStats.getRxBytes(this.mInterface);
        long txSpeed = txBytes - this.mLastTxBytes;
        long rxSpeed = rxBytes - this.mLastRxBytes;
        Slog.d(TAG, " txBytes:" + txBytes + " rxBytes" + rxBytes + " mLowDataTrafficLine:" + this.mLowDataTrafficLine + " mUpdateMillis:" + this.mUpdateMillis);
        if (this.mLastTxBytes == 0 && this.mLastRxBytes == 0) {
            this.mLastTxBytes = txBytes;
            this.mLastRxBytes = rxBytes;
            return DBG;
        }
        this.mLastTxBytes = txBytes;
        this.mLastRxBytes = rxBytes;
        if (txSpeed >= this.mLowDataTrafficLine || rxSpeed >= this.mLowDataTrafficLine) {
            return DBG;
        }
        return true;
    }

    private void handleUpdataDateTraffic() {
        Slog.d(TAG, "handleUpdataDateTraffic");
        if (this.mIsStart) {
            if (checkDataTrafficLine()) {
                Slog.d(TAG, "LOW_DATA_TAFFIC mCount:" + this.mCount);
                this.mCount++;
                if (((long) this.mCount) >= DEFAULT_LOW_DATA_TRAFFIC_COUNTS) {
                    Slog.w(TAG, "send CMD_LOW_DATA_TAFFIC");
                    this.mWifiController.mLocks.clearLocks();
                }
            } else {
                this.mCount = UPDATE_REQUEST;
            }
            this.mAlarmManager.setExact(UPDATE_REQUEST, System.currentTimeMillis() + this.mUpdateMillis, this.mUpdateIntent);
        }
    }

    private void loadDataTrafficUpdateTime() {
        this.mUpdateMillis = Global.getLong(this.mContext.getContentResolver(), WIFI_UPDATE_DATA_TRAFFIC_MS, DEFAULT_UPDATE_DATA_TRAFFIC_MS);
    }

    private void loadLowDataTrafficLine() {
        this.mLowDataTrafficLine = (Global.getLong(this.mContext.getContentResolver(), WIFI_LOW_DATA_TRAFFIC_LINE, DEFAULT_LOW_DATA_TRAFFIC_LINE) * this.mUpdateMillis) / 1000;
    }
}
