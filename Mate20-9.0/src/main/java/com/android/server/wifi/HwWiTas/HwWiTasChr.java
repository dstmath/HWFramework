package com.android.server.wifi.HwWiTas;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.wifipro.HwDualBandMessageUtil;

public class HwWiTasChr {
    private static HwWiTasChr mWiTasChr = null;
    final String KEY_APK = "apk";
    final String KEY_APK_ERRCODE = "errCode";
    final String KEY_APK_TYPE = "apkType";
    final String KEY_CHG_ANT_CNT = "chgAntCnt";
    final String KEY_CUR_ANT = "curAnt";
    final String KEY_DMEAT = "dMeaT";
    final String KEY_D_MEAD = "dMeaD";
    final String KEY_D_RSSI = "dRssi";
    final String KEY_D_RSSID = "dRssiD";
    final String KEY_D_RSSIT = "dRssiT";
    final String KEY_D_RTTD = "dRttD";
    final String KEY_D_RTTT = "dRttT";
    final String KEY_RSSI = HwDualBandMessageUtil.MSG_KEY_RSSI;
    final String KEY_RTT = "rtt";
    final String KEY_SANT_DCNT = "sAntDCnt";
    final String KEY_SANT_TCNT = "sAntTCnt";
    final String KEY_STAT = "stat";
    final String KEY_STR_CNT = "strCnt";
    final String KEY_SWI_CNT = "swiCnt";
    final String KEY_S_ANT = "sAnt";
    final String KEY_S_RSSI = "sRssi";
    final String KEY_TIMESTAMP = "timestamp";
    private int mDefaultAntCnt = 0;
    private int mDstAntRssi = 0;
    private int mErrCode = 0;
    private HwWifiCHRService mHwWifiCHRService;
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

    protected static synchronized HwWiTasChr getInstance(WifiManager wifiManager, WifiNative wifiNative) {
        HwWiTasChr hwWiTasChr;
        synchronized (HwWiTasChr.class) {
            if (mWiTasChr == null) {
                mWiTasChr = new HwWiTasChr(wifiManager, wifiNative);
            }
            hwWiTasChr = mWiTasChr;
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
        uploadStatisticsEvent("strCnt", this.mStartCnt);
    }

    public void increaseSwitchCnt() {
        this.mSwitchCnt++;
        uploadStatisticsEvent("swiCnt", this.mSwitchCnt);
    }

    public void increaseDefaultAntCnt() {
        this.mDefaultAntCnt++;
        uploadStatisticsEvent("sAntDCnt", this.mDefaultAntCnt);
    }

    public void increaseTasAntCnt() {
        this.mTasAntCnt++;
        uploadStatisticsEvent("sAntTCnt", this.mTasAntCnt);
    }

    public void recordDefAntMeasureDiff(int defAntMeasureDiff) {
        uploadStatisticsEvent("dMeaD", defAntMeasureDiff);
    }

    public void recordTasAntMeasureDiff(int tasAntMeasureDiff) {
        uploadStatisticsEvent("dMeaT", tasAntMeasureDiff);
    }

    public void increaseSrctAntChgCnt() {
        this.mSrcAntChgCnt++;
        uploadStatisticsEvent("chgAntCnt", this.mSrcAntChgCnt);
    }

    public void recordDefToTasRssi(int defToTasRssi) {
        uploadStatisticsEvent("dRssiD", defToTasRssi);
    }

    public void recordTasToDefRssi(int tasToDefRssi) {
        uploadStatisticsEvent("dRssiT", tasToDefRssi);
    }

    public void recordDefToTasRtt(int defToTasRtt) {
        uploadStatisticsEvent("dRttD", defToTasRtt);
    }

    public void recordTasToDefRtt(int tasToDefRtt) {
        uploadStatisticsEvent("dRttT", tasToDefRtt);
    }

    private HwWiTasChr(WifiManager wifiManager, WifiNative wifiNative) {
        this.mWifiManager = wifiManager;
        this.mWifiNative = wifiNative;
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    private void uploadWiTasChrErrInfo() {
        if (this.mWiTasArbitra == null || this.mHwWifiCHRService == null) {
            Log.e(HwWiTasUtils.TAG, "mWiTasArbitra or mHwWifiCHRService is null");
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
        data.putInt("errCode", this.mErrCode);
        data.putInt("apk", this.mWiTasArbitra.getGameId());
        data.putInt(HwDualBandMessageUtil.MSG_KEY_RSSI, this.mWiTasArbitra.getCurRssiInfo());
        if (this.mWiTasArbitra.isGameInWhiteList()) {
            data.putInt("rtt", this.mWiTasArbitra.getGameDelay());
        } else {
            data.putInt("rtt", 0);
        }
        this.mHwWifiCHRService.uploadDFTEvent(9, data);
    }

    private void uploadWiTasChrBasicInfo() {
        if (this.mWiTasArbitra == null || this.mHwWifiCHRService == null) {
            Log.e(HwWiTasUtils.TAG, "mWiTasArbitra or mHwWifiCHRService is null");
            return;
        }
        Bundle data = new Bundle();
        data.putInt("stat", this.mState);
        data.putInt("curAnt", this.mWiTasArbitra.getCurAntIndex());
        data.putLong("timestamp", this.mTimestamp);
        data.putInt("sAnt", this.mSrcAntIndex);
        data.putInt("sRssi", this.mSrcAntRssi);
        data.putInt("dRssi", this.mDstAntRssi);
        this.mHwWifiCHRService.uploadDFTEvent(8, data);
    }

    private void uploadStatisticsEvent(String key, int nValue) {
        if (this.mWiTasArbitra == null || this.mHwWifiCHRService == null) {
            Log.e(HwWiTasUtils.TAG, "mWiTasArbitra or mHwWifiCHRService is null");
            return;
        }
        Bundle data = new Bundle();
        data.putInt("apkType", this.mWiTasArbitra.isGameInWhiteList() ? 1 : 0);
        data.putInt(key, nValue);
        this.mHwWifiCHRService.uploadDFTEvent(10, data);
    }
}
