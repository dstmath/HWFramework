package com.android.server.media.dtv;

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
import java.util.List;

class DTVTurnerManager {
    public static final String TAG = "DTVTurnerManager";

    public static native int nativeEcmProcess(byte[] bArr, int i);

    public static native int nativeEmmProcess(byte[] bArr, int i);

    public static native byte[] nativeGetInitialCbc(int i);

    public static native byte[] nativeGetScrambleKey(int i);

    public static native byte[] nativeGetSystemKey(int i);

    public static native void nativeRelease();

    public static native int nativeSetup();

    public static native int nativeTsidProcess(int i);

    public static native TuningTargetChannel[] nativeTunerxCsAddch(int i, int i2, TuningTargetChannel[] tuningTargetChannelArr);

    public static native TuningTargetChannel[] nativeTunerxCsDelch(int i, int i2, TuningTargetChannel[] tuningTargetChannelArr);

    public static native int nativeTunerxCsTuning(int i, int i2, int[] iArr);

    public static native void nativeTunerxEnd();

    public static native int nativeTunerxGetAc(TunerACInfo tunerACInfo, TunerSyncInfo tunerSyncInfo, long j);

    public static native int nativeTunerxGetBper(TunerBperInfo tunerBperInfo);

    public static native int nativeTunerxGetCnInfo(TunerCNInfo tunerCNInfo);

    public static native int nativeTunerxGetMonInfo(TunerMonitoringInfo tunerMonitoringInfo);

    public static native int nativeTunerxGetRssiInfo(TunerRSSIInfo tunerRSSIInfo);

    public static native int nativeTunerxGetSyncInfo(TunerSyncInfo tunerSyncInfo);

    public static native int nativeTunerxGetTmccInfo(TunerTMCCInfo tunerTMCCInfo);

    public static native int nativeTunerxSearchCh(List<TuningTargetChannel> list);

    public static native int nativeTunerxStart();

    public static native int nativeTunerxTsRead(TunerChData tunerChData);

    public static native int nativeTunerxTsStart(TunerDataTsif tunerDataTsif);

    public static native int nativeTunerxTsStop();

    public static native int nativeTunerxTuning(int i, int i2);

    DTVTurnerManager() {
    }

    static {
        System.loadLibrary("dtvturner_jni");
    }
}
