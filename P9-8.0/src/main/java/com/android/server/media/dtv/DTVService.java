package com.android.server.media.dtv;

import android.media.dtv.IDTVService.Stub;
import android.media.dtv.TunerACInfo;
import android.media.dtv.TunerBperInfo;
import android.media.dtv.TunerCNInfo;
import android.media.dtv.TunerChData;
import android.media.dtv.TunerDataTsif;
import android.media.dtv.TunerMonitoringInfo;
import android.media.dtv.TunerRSSIInfo;
import android.media.dtv.TunerSyncInfo;
import android.media.dtv.TunerTMCCInfo;
import android.media.dtv.TuningTargetChannel;
import android.util.Log;
import java.util.List;

public class DTVService extends Stub {
    public static final int RET_NG = -1;
    public static final int RET_OK = 0;
    public static final String TAG = "DTVService";

    public int tsidProcess(int ts_id) {
        return DTVTurnerManager.nativeTsidProcess(ts_id);
    }

    public int emmProcess(byte[] data, int len) {
        return DTVTurnerManager.nativeEmmProcess(data, len);
    }

    public int ecmProcess(byte[] data, int len) {
        return DTVTurnerManager.nativeEcmProcess(data, len);
    }

    public byte[] getScrambleKey(int uKeyLen) {
        return DTVTurnerManager.nativeGetScrambleKey(uKeyLen);
    }

    public byte[] getSystemKey(int uLen) {
        return DTVTurnerManager.nativeGetSystemKey(uLen);
    }

    public byte[] getInitialCbc(int uLen) {
        return DTVTurnerManager.nativeGetInitialCbc(uLen);
    }

    public int tunerxStart() {
        DTVTurnerManager.nativeSetup();
        return DTVTurnerManager.nativeTunerxStart();
    }

    public int tunerxTuning(int tunerBand, int ch) {
        return DTVTurnerManager.nativeTunerxTuning(tunerBand, ch);
    }

    public int tunerxCsTuning(int tunerBand, int ch, int[] lockFlg) {
        return DTVTurnerManager.nativeTunerxCsTuning(tunerBand, ch, lockFlg);
    }

    public int tunerxGetMonInfo(TunerMonitoringInfo tunerMonitoringInfo) {
        if (tunerMonitoringInfo == null) {
            tunerMonitoringInfo = new TunerMonitoringInfo();
        }
        return DTVTurnerManager.nativeTunerxGetMonInfo(tunerMonitoringInfo);
    }

    public int tunerxGetTmccInfo(TunerTMCCInfo tunerTMCCInfo) {
        if (tunerTMCCInfo == null) {
            tunerTMCCInfo = new TunerTMCCInfo();
        }
        return DTVTurnerManager.nativeTunerxGetTmccInfo(tunerTMCCInfo);
    }

    public int tunerxGetAc(TunerACInfo tunerACInfo, TunerSyncInfo tunerSyncInfo, long toms) {
        return DTVTurnerManager.nativeTunerxGetAc(tunerACInfo, tunerSyncInfo, toms);
    }

    public int tunerxGetRssiInfo(TunerRSSIInfo tunerRSSIInfo) {
        return DTVTurnerManager.nativeTunerxGetRssiInfo(tunerRSSIInfo);
    }

    public int tunerxGetSyncInfo(TunerSyncInfo tunerSyncInfo) {
        return DTVTurnerManager.nativeTunerxGetSyncInfo(tunerSyncInfo);
    }

    public int tunerxGetCnInfo(TunerCNInfo tunerACInfo) {
        return DTVTurnerManager.nativeTunerxGetCnInfo(tunerACInfo);
    }

    public int tunerxGetBper(TunerBperInfo tunerBperInfo) {
        return DTVTurnerManager.nativeTunerxGetBper(tunerBperInfo);
    }

    public int tunerxTsStart(TunerDataTsif tunerDataTsif) {
        if (tunerDataTsif == null) {
            Log.e(TAG, "parameter tunerDataTsif can not be null");
            return -1;
        }
        Log.i(TAG, "tunerxTsStart tsif:" + tunerDataTsif.toString());
        return DTVTurnerManager.nativeTunerxTsStart(tunerDataTsif);
    }

    public int tunerxTsRead(TunerChData tunerChData) {
        if (tunerChData != null) {
            return DTVTurnerManager.nativeTunerxTsRead(tunerChData);
        }
        Log.e(TAG, "parameter tunerChData can not be null");
        return -1;
    }

    public int tunerxTsStop() {
        return DTVTurnerManager.nativeTunerxTsStop();
    }

    public TuningTargetChannel[] tunerxCsAddch(int tunerBand, int ch, TuningTargetChannel[] tuningTargetChannels) {
        return DTVTurnerManager.nativeTunerxCsAddch(tunerBand, ch, tuningTargetChannels);
    }

    public TuningTargetChannel[] tunerxCsDelch(int tunerBand, int ch, TuningTargetChannel[] tuningTargetChannels) {
        return DTVTurnerManager.nativeTunerxCsDelch(tunerBand, ch, tuningTargetChannels);
    }

    public int tunerxSearchCh(List<TuningTargetChannel> tuningTargetChannels) {
        return DTVTurnerManager.nativeTunerxSearchCh(tuningTargetChannels);
    }

    public void tunerxEnd() {
        DTVTurnerManager.nativeTunerxEnd();
    }
}
