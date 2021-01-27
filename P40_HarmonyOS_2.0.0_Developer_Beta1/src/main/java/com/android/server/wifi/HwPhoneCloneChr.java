package com.android.server.wifi;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.WifiConfiguration;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.wifi.HwHiLog;

public class HwPhoneCloneChr {
    private static final String CHANNEL = "channel";
    private static final String DATA_OF_CLONE = "data";
    private static final String IFACE_NAME = "wlan0";
    private static final int INIT_CAPACITY = 128;
    private static final String MODEL_OF_PHONE = "model";
    private static final String OLD_PHONE_BRAND = "clone_src_brand";
    private static final int PHONE_CLONE_STATISTICS_EVENT = 909002072;
    private static final String REQUEST_BANDWIDTH = "reqbw";
    private static final String RESPONSE_BANDWIDTH = "rspbw";
    private static final String SSID_TAG = "CloudClone";
    private static final String TAG = "HwPhoneCloneChr";
    private static final String TIME_OF_CLONE = "time";
    private static HwPhoneCloneChr mHwPhoneCloneChr = null;
    private boolean isPhoneClone;
    private short mChannel;
    private int mCloneTraffic;
    private String mPhoneModel;
    private byte mRequestBandWidth;
    private byte mResponseBandWidth;
    private long mStartApTimestamp;
    private long mStartRxBytes;
    private short mTimeOfClone;

    private HwPhoneCloneChr() {
        resetParameters();
    }

    public static synchronized HwPhoneCloneChr getInstance() {
        HwPhoneCloneChr hwPhoneCloneChr;
        synchronized (HwPhoneCloneChr.class) {
            if (mHwPhoneCloneChr == null) {
                mHwPhoneCloneChr = new HwPhoneCloneChr();
            }
            hwPhoneCloneChr = mHwPhoneCloneChr;
        }
        return hwPhoneCloneChr;
    }

    public void notifyApStarted(WifiConfiguration apConfig) {
        if (apConfig != null) {
            this.mChannel = (short) apConfig.apChannel;
            this.mRequestBandWidth = (byte) apConfig.apBandwidth;
        }
        this.mStartApTimestamp = SystemClock.elapsedRealtime();
        this.mStartRxBytes = TrafficStats.getRxBytes(IFACE_NAME);
    }

    public void notifyApStopped(Context context) {
        long now = SystemClock.elapsedRealtime();
        this.mCloneTraffic = (int) ((TrafficStats.getRxBytes(IFACE_NAME) - this.mStartRxBytes) / 1048576);
        this.mTimeOfClone = (short) ((int) ((now - this.mStartApTimestamp) / 1000));
        if (this.isPhoneClone && context != null) {
            this.mPhoneModel = Settings.Secure.getString(context.getContentResolver(), OLD_PHONE_BRAND);
            this.isPhoneClone = false;
        }
        uploadPhoneCloneParamStatistics();
    }

    public void updateApConfiguration(WifiConfiguration apConfig) {
        if (apConfig != null) {
            this.mChannel = (short) apConfig.apChannel;
            this.mRequestBandWidth = (byte) apConfig.apBandwidth;
            if (apConfig.SSID != null && apConfig.SSID.contains(SSID_TAG)) {
                this.isPhoneClone = true;
            }
        }
    }

    public void setResponseBandWidth(byte responseBandWidth) {
        this.mResponseBandWidth = responseBandWidth;
    }

    private void uploadPhoneCloneParamStatistics() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("upload phone clone parameter statistics");
        sb.append(",channel:");
        sb.append((int) this.mChannel);
        sb.append(",reqbw:");
        sb.append((int) this.mRequestBandWidth);
        sb.append(",rspbw:");
        sb.append((int) this.mResponseBandWidth);
        sb.append(",data:");
        sb.append(this.mCloneTraffic);
        sb.append(",time:");
        sb.append((int) this.mTimeOfClone);
        sb.append(",phone model:");
        sb.append(this.mPhoneModel);
        HwHiLog.d(TAG, false, sb.toString(), new Object[0]);
        resetParameters();
    }

    private void resetParameters() {
        this.mChannel = 0;
        this.mRequestBandWidth = 0;
        this.mResponseBandWidth = 0;
        this.mCloneTraffic = 0;
        this.mTimeOfClone = 0;
        this.mStartRxBytes = 0;
        this.mStartApTimestamp = 0;
        this.isPhoneClone = false;
        this.mPhoneModel = "";
    }
}
