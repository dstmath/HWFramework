package com.android.server.wifi.HwWiTas;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiNative;

public class HwWiTasChr {
    private static final int INITIAL_VALUE_OF_WITAS = -1;
    private static final String KEY_APK = "apk";
    private static final String KEY_APK_ERRCODE = "errCode";
    private static final String KEY_APK_TYPE = "apkType";
    private static final String KEY_CHG_ANT_CNT = "chgAntCnt";
    private static final String KEY_CUR_ANT = "curAnt";
    private static final String KEY_DMEAT = "dMeaT";
    private static final String KEY_D_MEAD = "dMeaD";
    private static final String KEY_D_RSSI = "dRssi";
    private static final String KEY_D_RSSID = "dRssiD";
    private static final String KEY_D_RSSIT = "dRssiT";
    private static final String KEY_D_RTTD = "dRttD";
    private static final String KEY_D_RTTT = "dRttT";
    private static final String KEY_RSSI = "rssi";
    private static final String KEY_RTT = "rtt";
    private static final String KEY_SANT_DCNT = "sAntDCnt";
    private static final String KEY_SANT_TCNT = "sAntTCnt";
    private static final String KEY_STAT = "stat";
    private static final String KEY_STR_CNT = "strCnt";
    private static final String KEY_SWI_CNT = "swiCnt";
    private static final String KEY_S_ANT = "sAnt";
    private static final String KEY_S_RSSI = "sRssi";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String WITAS_OR_CHR_IS_NULL = "mWiTasArbitra or mHwWifiChrService is null";
    private static HwWiTasChr sWiTasChr = null;
    private int mDefaultAntCnt = 0;
    private int mDstAntRssi = 0;
    private int mErrCode = 0;
    private HwWifiCHRService mHwWifiChrService;
    private long mLastSampleTime = 0;
    private int mSrcAntChgCnt = 0;
    private int mSrcAntIndex = 0;
    private int mSrcAntRssi = 0;
    private int mStartCnt = 0;
    private int mState = 0;
    private int mSwitchCnt = 0;
    private int mTakeSampleCount = 0;
    private int mTasAntCnt = 0;
    private long mTimestamp = 0;
    private HwWiTasArbitra mWiTasArbitra;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;

    private HwWiTasChr(WifiManager wifiManager, WifiNative wifiNative) {
        this.mWifiManager = wifiManager;
        this.mWifiNative = wifiNative;
        this.mHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    protected static synchronized HwWiTasChr getInstance(WifiManager wifiManager, WifiNative wifiNative) {
        HwWiTasChr hwWiTasChr;
        synchronized (HwWiTasChr.class) {
            if (sWiTasChr == null) {
                sWiTasChr = new HwWiTasChr(wifiManager, wifiNative);
            }
            hwWiTasChr = sWiTasChr;
        }
        return hwWiTasChr;
    }

    /* access modifiers changed from: protected */
    public void initWitasChr() {
        this.mWiTasArbitra = HwWiTasArbitra.getInstance(this.mWifiManager, this.mWifiNative);
    }

    /* access modifiers changed from: protected */
    public void resetData() {
        this.mSrcAntIndex = -1;
        this.mTimestamp = -1;
        this.mSrcAntRssi = -1;
        this.mDstAntRssi = -1;
    }

    /* access modifiers changed from: protected */
    public void recordSrcIndex(int antIndex) {
        this.mSrcAntIndex = antIndex;
    }

    /* access modifiers changed from: protected */
    public void recordSrcRssi(int rssi) {
        this.mSrcAntRssi = rssi;
    }

    /* access modifiers changed from: protected */
    public void recordDstRssi(int rssi) {
        this.mDstAntRssi = rssi;
    }

    /* access modifiers changed from: protected */
    public void recordTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    /* access modifiers changed from: protected */
    public void recordErrCode(int errCode) {
        this.mErrCode = errCode;
        uploadWiTasChrErrInfo();
    }

    /* access modifiers changed from: protected */
    public void recordState(int state) {
        if (this.mState != state) {
            this.mState = state;
            uploadWiTasChrBasicInfo();
        }
    }

    public void increaseStartCnt() {
        this.mStartCnt++;
        uploadStatisticsEvent(KEY_STR_CNT, this.mStartCnt);
    }

    public void increaseSwitchCnt() {
        this.mSwitchCnt++;
        uploadStatisticsEvent(KEY_SWI_CNT, this.mSwitchCnt);
    }

    public void increaseDefaultAntCnt() {
        this.mDefaultAntCnt++;
        uploadStatisticsEvent(KEY_SANT_DCNT, this.mDefaultAntCnt);
    }

    public void increaseTasAntCnt() {
        this.mTasAntCnt++;
        uploadStatisticsEvent(KEY_SANT_TCNT, this.mTasAntCnt);
    }

    public void recordDefAntMeasureDiff(int defAntMeasureDiff) {
        uploadStatisticsEvent(KEY_D_MEAD, defAntMeasureDiff);
    }

    public void recordTasAntMeasureDiff(int tasAntMeasureDiff) {
        uploadStatisticsEvent(KEY_DMEAT, tasAntMeasureDiff);
    }

    public void increaseSrctAntChgCnt() {
        this.mSrcAntChgCnt++;
        uploadStatisticsEvent(KEY_CHG_ANT_CNT, this.mSrcAntChgCnt);
    }

    public void recordDefToTasRssi(int defToTasRssi) {
        uploadStatisticsEvent(KEY_D_RSSID, defToTasRssi);
    }

    public void recordTasToDefRssi(int tasToDefRssi) {
        uploadStatisticsEvent(KEY_D_RSSIT, tasToDefRssi);
    }

    public void recordDefToTasRtt(int defToTasRtt) {
        uploadStatisticsEvent(KEY_D_RTTD, defToTasRtt);
    }

    public void recordTasToDefRtt(int tasToDefRtt) {
        uploadStatisticsEvent(KEY_D_RTTT, tasToDefRtt);
    }

    private void uploadWiTasChrErrInfo() {
        if (this.mWiTasArbitra == null || this.mHwWifiChrService == null) {
            HwHiLog.e(HwWiTasUtils.TAG, false, WITAS_OR_CHR_IS_NULL, new Object[0]);
            return;
        }
        if (System.currentTimeMillis() - this.mLastSampleTime > 86400000) {
            this.mLastSampleTime = 0;
            this.mTakeSampleCount = 0;
        }
        if (this.mTakeSampleCount == 0) {
            this.mLastSampleTime = System.currentTimeMillis();
        }
        if (this.mErrCode == 7) {
            this.mTakeSampleCount++;
            if (this.mTakeSampleCount > 11) {
                return;
            }
        }
        Bundle data = new Bundle();
        data.putInt(KEY_APK_ERRCODE, this.mErrCode);
        data.putInt(KEY_APK, this.mWiTasArbitra.getGameId());
        data.putInt(KEY_RSSI, this.mWiTasArbitra.getCurRssiInfo());
        if (this.mWiTasArbitra.isGameInWhiteList()) {
            data.putInt(KEY_RTT, this.mWiTasArbitra.getGameDelay());
        } else {
            data.putInt(KEY_RTT, 0);
        }
        this.mHwWifiChrService.uploadDFTEvent(9, data);
    }

    private void uploadWiTasChrBasicInfo() {
        if (this.mWiTasArbitra == null || this.mHwWifiChrService == null) {
            HwHiLog.e(HwWiTasUtils.TAG, false, WITAS_OR_CHR_IS_NULL, new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putInt(KEY_STAT, this.mState);
        data.putInt(KEY_CUR_ANT, this.mWiTasArbitra.getCurAntIndex());
        data.putLong(KEY_TIMESTAMP, this.mTimestamp);
        data.putInt(KEY_S_ANT, this.mSrcAntIndex);
        data.putInt(KEY_S_RSSI, this.mSrcAntRssi);
        data.putInt(KEY_D_RSSI, this.mDstAntRssi);
        this.mHwWifiChrService.uploadDFTEvent(8, data);
    }

    private void uploadStatisticsEvent(String key, int value) {
        if (this.mWiTasArbitra == null || this.mHwWifiChrService == null) {
            HwHiLog.e(HwWiTasUtils.TAG, false, WITAS_OR_CHR_IS_NULL, new Object[0]);
            return;
        }
        Bundle data = new Bundle();
        data.putInt(KEY_APK_TYPE, this.mWiTasArbitra.isGameInWhiteList() ? 1 : 0);
        data.putInt(key, value);
        this.mHwWifiChrService.uploadDFTEvent(10, data);
    }
}
