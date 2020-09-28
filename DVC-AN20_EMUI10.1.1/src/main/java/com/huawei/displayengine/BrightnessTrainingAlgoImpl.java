package com.huawei.displayengine;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import com.huawei.android.os.HwPowerManager;
import com.huawei.displayengine.DisplayEngineDbManager;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;

/* compiled from: BrigntnessTrainingAlgoImpl */
class BrightnessTrainingAlgoImpl {
    public static final int ABORT_TRAINING = 5;
    public static final int CREATE_TRAINING_ENGINE = 0;
    public static final int CURVE_COUNT = 35;
    public static final int DESTROY_TRAINING_ENGINE = 2;
    public static final int ESCW_COUNT = 9;
    public static final int GETPARAM_TRAINING_ENGINE = 4;
    public static final int MAX_ALGO_RESULT = 2000;
    public static final int PROCESS_TRAINING_ENGINE = 1;
    public static final int SETPARAM_TRAINING_ENGINE = 3;
    private static final String TAG = "DE J BrightnessTrainingAlgoImpl";
    private static String mAlgoXmlPath = BuildConfig.FLAVOR;
    private AlgoParam mAlgoInfo;
    private final Context mContext;
    private int mHandle = -1;
    private boolean mIsAlgoExist = false;
    private final Object mLockJNI = new Object();

    public BrightnessTrainingAlgoImpl(Context context) {
        DeLog.i(TAG, "BrightnessTrainingAlgoImpl enter");
        this.mContext = context;
    }

    private int readDragInfo() {
        DisplayEngineDbManager dbManager = DisplayEngineDbManager.getInstance(this.mContext);
        ArrayList<Bundle> items = DisplayEngineDataCleaner.getInstance(this.mContext).cleanData(dbManager.getAllRecords(DisplayEngineDbManager.DragInformationKey.TAG, new Bundle()), 0);
        AlgoParam algoParam = this.mAlgoInfo;
        algoParam.mDragCount = 0;
        algoParam.mDragSize = 0;
        algoParam.mDragInfo = BuildConfig.FLAVOR;
        int i = -1;
        if (items != null) {
            int i2 = 0;
            while (i2 < items.size()) {
                Bundle data = items.get(i2);
                if (data == null) {
                    DeLog.e(TAG, "Bundle data is null!");
                    return i;
                }
                long time = data.getLong("TimeStamp");
                float start = data.getFloat(DisplayEngineDbManager.DragInformationKey.START_POINT);
                float stop = data.getFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT);
                int al = data.getInt("AmbientLight");
                boolean z = data.getBoolean(DisplayEngineDbManager.DragInformationKey.PROXIMITY_POSITIVE);
                int appType = data.getInt("AppType");
                this.mAlgoInfo.mDragCount++;
                this.mAlgoInfo.mDragInfo = this.mAlgoInfo.mDragInfo + "[" + time + ", " + start + ", " + stop + ", " + al + ", " + (z ? 1 : 0) + ", " + appType + "]";
                i2++;
                dbManager = dbManager;
                items = items;
                i = -1;
            }
            AlgoParam algoParam2 = this.mAlgoInfo;
            algoParam2.mDragSize = algoParam2.mDragInfo.length();
        }
        if (this.mAlgoInfo.mDragCount >= 2) {
            return 0;
        }
        DeLog.i(TAG, "DragInfo number is not enough ! count: " + this.mAlgoInfo.mDragCount);
        return -1;
    }

    private int readCurveByTag(DisplayEngineDbManager dbManager, StringBuffer text, String name) {
        if (text == null || name == null) {
            DeLog.i(TAG, "text is null");
            return -1;
        }
        ArrayList<Bundle> items = dbManager.getAllRecords(name, new Bundle());
        if (items == null) {
            DeLog.i(TAG, "Read " + name + " failed !");
            return -1;
        } else if (items.size() == 0) {
            DeLog.i(TAG, "DisplayEngineDB low curve size is 0");
            return -1;
        } else {
            for (int i = 0; i < items.size(); i++) {
                Bundle data = items.get(i);
                if (data == null) {
                    DeLog.e(TAG, "Bundle data is null!");
                    return -1;
                }
                float al = data.getFloat("AmbientLight");
                float bl = data.getFloat(DisplayEngineDbManager.BrightnessCurveKey.BL);
                text.append("[" + al + "," + bl + "]");
            }
            return items.size();
        }
    }

    private int readBrightnessCurve() {
        DisplayEngineDbManager dbManager = DisplayEngineDbManager.getInstance(this.mContext);
        if (dbManager == null) {
            DeLog.e(TAG, "dbManager is null");
            return -1;
        }
        StringBuffer low_text = new StringBuffer();
        int count = readCurveByTag(dbManager, low_text, "BrightnessCurveLow");
        if (count <= 0) {
            DeLog.i(TAG, "Read low failed!");
            return -1;
        }
        this.mAlgoInfo.mBLCurveTypeLowLuma = low_text.toString();
        this.mAlgoInfo.mLowLumaCount = count;
        StringBuffer middle_text = new StringBuffer();
        int count2 = readCurveByTag(dbManager, middle_text, "BrightnessCurveMiddle");
        if (count2 <= 0) {
            DeLog.i(TAG, "Read middle failed!");
            return -1;
        }
        this.mAlgoInfo.mBLCurveTypeMedialLuma = middle_text.toString();
        this.mAlgoInfo.mMedialLumaCount = count2;
        StringBuffer hight_text = new StringBuffer();
        int count3 = readCurveByTag(dbManager, hight_text, "BrightnessCurveHigh");
        if (count3 <= 0) {
            DeLog.i(TAG, "Read hight failed!");
            return -1;
        }
        this.mAlgoInfo.mBLCurveTypeHighLuma = hight_text.toString();
        this.mAlgoInfo.mHighLumaCount = count3;
        StringBuffer default_text = new StringBuffer();
        int count4 = readCurveByTag(dbManager, default_text, "BrightnessCurveDefault");
        if (count4 <= 0) {
            DeLog.i(TAG, "Read default failed!");
            return -1;
        }
        this.mAlgoInfo.mBLCurveTypeDefaultLuma = default_text.toString();
        this.mAlgoInfo.mDefaultLumaCount = count4;
        ArrayList<Bundle> items = dbManager.getAllRecords("AlgorithmESCW", new Bundle());
        if (items == null) {
            DeLog.i(TAG, "DisplayEngineDB ESCW = null");
            return -1;
        } else if (items.size() == 0) {
            DeLog.i(TAG, "DisplayEngineDB ESCW size is 0");
            return -1;
        } else {
            StringBuffer text = new StringBuffer();
            text.append("[");
            for (int i = 0; i < items.size(); i++) {
                Bundle data = items.get(i);
                if (data == null) {
                    DeLog.e(TAG, "Bundle data is null!");
                    return -1;
                }
                float escw = data.getFloat(DisplayEngineDbManager.AlgorithmEscwKey.ESCW);
                text.append(escw + ",");
            }
            this.mAlgoInfo.mESCW = text.toString();
            this.mAlgoInfo.mESCWCount = 1;
            return 0;
        }
    }

    private static float getFloat(byte[] b, int offset) {
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 4; shiftBy++) {
            accum |= (b[shiftBy + offset] & 255) << (shiftBy * 8);
        }
        return Float.intBitsToFloat(accum);
    }

    private int writeCurveByTag(DisplayEngineDbManager dbManager, byte[] curve, String name, int buffer_offset) {
        if (dbManager == null || name == null || curve == null) {
            return -1;
        }
        Bundle data = new Bundle();
        float[] alValues = new float[35];
        float[] blValues = new float[35];
        for (int i = 0; i < 35; i++) {
            int offset = (i * 8) + buffer_offset;
            alValues[i] = getFloat(curve, offset);
            blValues[i] = getFloat(curve, offset + 4);
        }
        int buffer_offset2 = buffer_offset + 280;
        data.putFloatArray("AmbientLight", alValues);
        data.putFloatArray(DisplayEngineDbManager.BrightnessCurveKey.BL, blValues);
        dbManager.addOrUpdateRecord(name, data);
        return buffer_offset2;
    }

    private int writeBrightnessCurve(byte[] curve) {
        DisplayEngineDbManager dbManager = DisplayEngineDbManager.getInstance(this.mContext);
        if (dbManager == null) {
            DeLog.e(TAG, "dbManager is null");
            return -1;
        }
        Bundle data = new Bundle();
        float[] escwValues = new float[9];
        for (int i = 0; i < 9; i++) {
            escwValues[i] = getFloat(curve, 0 + (i * 4));
        }
        data.putFloatArray(DisplayEngineDbManager.AlgorithmEscwKey.ESCW, escwValues);
        dbManager.addOrUpdateRecord("AlgorithmESCW", data);
        int buffer_offset = writeCurveByTag(dbManager, curve, "BrightnessCurveLow", 36);
        if (buffer_offset <= 0) {
            DeLog.e(TAG, "Write  low curvefailed! ");
            return -1;
        }
        int buffer_offset2 = writeCurveByTag(dbManager, curve, "BrightnessCurveMiddle", buffer_offset);
        if (buffer_offset2 <= 0) {
            DeLog.e(TAG, "Write  middle curve failed! ");
            return -1;
        }
        int buffer_offset3 = writeCurveByTag(dbManager, curve, "BrightnessCurveHigh", buffer_offset2);
        if (buffer_offset3 <= 0) {
            DeLog.e(TAG, "Write  middle high failed! ");
            return -1;
        } else if (writeCurveByTag(dbManager, curve, "BrightnessCurveDefault", buffer_offset3) > 0) {
            return 0;
        } else {
            DeLog.e(TAG, "Write  middle default failed! ");
            return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        r3 = new byte[2000];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003c, code lost:
        if (com.huawei.displayengine.DisplayEngineLibraries.nativeProcessAlgorithm(1, r8.mHandle, 1, null, r3) == 0) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003e, code lost:
        com.huawei.displayengine.DeLog.i(com.huawei.displayengine.BrightnessTrainingAlgoImpl.TAG, " PROCESS_TRAINING_ENGINE failed!");
        r0 = r8.mLockJNI;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0047, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        destroyAlgo();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004b, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004c, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0050, code lost:
        com.huawei.displayengine.DeLog.i(com.huawei.displayengine.BrightnessTrainingAlgoImpl.TAG, "ESCW " + ((int) r3[0]) + ", " + ((int) r3[1]) + ", " + ((int) r3[2]) + ", " + ((int) r3[3]) + ", eswValue = " + ((int) r3[4]));
        writeBrightnessCurve(r3);
        r0 = new android.os.Bundle();
        r0.putInt("CurveUpdateFlag", 1);
        r4 = com.huawei.android.os.HwPowerManager.setHwBrightnessData("PersonalizedBrightness", r0);
        r6 = r8.mLockJNI;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a8, code lost:
        monitor-enter(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        destroyAlgo();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00ac, code lost:
        monitor-exit(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ad, code lost:
        return 0;
     */
    private int processAlgo() {
        synchronized (this.mLockJNI) {
            if (DisplayEngineLibraries.nativeProcessAlgorithm(1, this.mHandle, 3, this.mAlgoInfo, null) != 0) {
                DeLog.e(TAG, " SETPARAM_TRAINING_ENGINE failed");
                return -1;
            } else if (DisplayEngineLibraries.nativeProcessAlgorithm(1, this.mHandle, 0, null, null) != 0) {
                DeLog.e(TAG, " CREATE_TRAINING_ENGINE failed");
                return -1;
            } else {
                this.mIsAlgoExist = true;
            }
        }
    }

    private int destroyAlgo() {
        if (this.mIsAlgoExist) {
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(1, this.mHandle, 2, null, null);
            if (ret != 0) {
                DeLog.e(TAG, " DESTROY_TRAINING_ENGINE failed");
            }
            this.mIsAlgoExist = false;
            return ret;
        }
        DeLog.i(TAG, " destroyAlgo failed, algo is not exist!");
        return -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0261, code lost:
        if (processAlgo() == 0) goto L_0x026a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0263, code lost:
        com.huawei.displayengine.DeLog.i(com.huawei.displayengine.BrightnessTrainingAlgoImpl.TAG, " processAlgo failed! ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x026a, code lost:
        r4 = r12.mLockJNI;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x026c, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r2 = com.huawei.displayengine.DisplayEngineLibraries.nativeDeinitAlgorithm(1, r12.mHandle);
        r12.mHandle = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0276, code lost:
        if (r2 == 0) goto L_0x0281;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0278, code lost:
        com.huawei.displayengine.DeLog.e(com.huawei.displayengine.BrightnessTrainingAlgoImpl.TAG, " nativeDeinitAlgorithm failed! ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x027f, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0280, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0281, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0282, code lost:
        com.huawei.displayengine.DeLog.i(com.huawei.displayengine.BrightnessTrainingAlgoImpl.TAG, "processTraining  stop ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x028a, code lost:
        return 0;
     */
    public int processTraining() {
        DeLog.i(TAG, "processTraining  start !! ");
        this.mAlgoInfo = new AlgoParam();
        if (readDragInfo() != 0) {
            DeLog.i(TAG, "no DragInfo ! ");
            return -1;
        }
        if (readBrightnessCurve() != 0) {
            DeLog.i(TAG, "no BrightnessCurve  in DataBase! ");
            Bundle data = new Bundle();
            HwPowerManager.getHwBrightnessData("PersonalizedBrightness", data);
            ArrayList<PointF> list = data.getParcelableArrayList("DefaultCurve");
            if (list == null) {
                DeLog.e(TAG, "list is null!");
                return -1;
            }
            StringBuffer text = new StringBuffer();
            int listsize = list.size();
            for (int i = 0; i < listsize; i++) {
                float al = list.get(i).x;
                float bl = list.get(i).y;
                text.append("[" + al + "," + bl + "]");
            }
            this.mAlgoInfo.mBLCurveTypeLowLuma = text.toString();
            AlgoParam algoParam = this.mAlgoInfo;
            algoParam.mLowLumaCount = listsize;
            algoParam.mBLCurveTypeMedialLuma = text.toString();
            AlgoParam algoParam2 = this.mAlgoInfo;
            algoParam2.mMedialLumaCount = listsize;
            algoParam2.mBLCurveTypeHighLuma = text.toString();
            AlgoParam algoParam3 = this.mAlgoInfo;
            algoParam3.mHighLumaCount = listsize;
            algoParam3.mBLCurveTypeDefaultLuma = text.toString();
            AlgoParam algoParam4 = this.mAlgoInfo;
            algoParam4.mDefaultLumaCount = listsize;
            algoParam4.mESCW = "[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0]";
            algoParam4.mESCWCount = 1;
            algoParam4.mFirstInital = 1;
        }
        if (this.mAlgoInfo.mLowLumaCount == 35 && this.mAlgoInfo.mMedialLumaCount == 35 && this.mAlgoInfo.mHighLumaCount == 35 && this.mAlgoInfo.mDefaultLumaCount == 35 && this.mAlgoInfo.mESCWCount == 1) {
            AlgoParam algoParam5 = this.mAlgoInfo;
            algoParam5.mLowLumaSize = algoParam5.mBLCurveTypeLowLuma.length();
            AlgoParam algoParam6 = this.mAlgoInfo;
            algoParam6.mMedialLumaSize = algoParam6.mBLCurveTypeMedialLuma.length();
            AlgoParam algoParam7 = this.mAlgoInfo;
            algoParam7.mHighLumaSize = algoParam7.mBLCurveTypeHighLuma.length();
            AlgoParam algoParam8 = this.mAlgoInfo;
            algoParam8.mDefaultLumaSize = algoParam8.mBLCurveTypeDefaultLuma.length();
            AlgoParam algoParam9 = this.mAlgoInfo;
            algoParam9.mESCWSize = algoParam9.mESCW.length();
            DeLog.i(TAG, "input mDragInfo: " + this.mAlgoInfo.mDragInfo);
            DeLog.i(TAG, "input mDragSize: " + this.mAlgoInfo.mDragSize + ", mDragCount " + this.mAlgoInfo.mDragCount);
            StringBuilder sb = new StringBuilder();
            sb.append("input mBLCurveTypeLowLuma: ");
            sb.append(this.mAlgoInfo.mBLCurveTypeLowLuma);
            DeLog.i(TAG, sb.toString());
            DeLog.i(TAG, "input mLowLumaCount: " + this.mAlgoInfo.mLowLumaCount);
            DeLog.i(TAG, "input mBLCurveTypeMedialLuma: " + this.mAlgoInfo.mBLCurveTypeMedialLuma);
            DeLog.i(TAG, "input mMedialLumaCount: " + this.mAlgoInfo.mMedialLumaCount);
            DeLog.i(TAG, "input mBLCurveTypeHighLuma: " + this.mAlgoInfo.mBLCurveTypeHighLuma);
            DeLog.i(TAG, "input mHighLumaCount: " + this.mAlgoInfo.mHighLumaCount);
            DeLog.i(TAG, "input mBLCurveTypeDefaultLuma: " + this.mAlgoInfo.mBLCurveTypeDefaultLuma);
            DeLog.i(TAG, "input mDefaultLumaCount: " + this.mAlgoInfo.mDefaultLumaCount);
            DeLog.i(TAG, "input mESCW: " + this.mAlgoInfo.mESCW);
            DeLog.i(TAG, "input mESCWCount: " + this.mAlgoInfo.mESCWCount);
            synchronized (this.mLockJNI) {
                int ret = DisplayEngineLibraries.nativeInitAlgorithm(1);
                if (ret >= 0) {
                    this.mHandle = ret;
                } else {
                    this.mHandle = -1;
                    DeLog.e(TAG, "nativeInitAlgorithm failed! ");
                    return -1;
                }
            }
        } else {
            DeLog.i(TAG, "Count is not correct ! ");
            DeLog.i(TAG, "LowLumaCount = " + this.mAlgoInfo.mLowLumaCount);
            DeLog.i(TAG, "MedialLumaCount = " + this.mAlgoInfo.mMedialLumaCount);
            DeLog.i(TAG, "HighLumaCount = " + this.mAlgoInfo.mHighLumaCount);
            DeLog.i(TAG, "DefaultLumaCount = " + this.mAlgoInfo.mDefaultLumaCount);
            DeLog.i(TAG, "ESCWCount = " + this.mAlgoInfo.mESCWCount);
            return -1;
        }
    }

    public int abortTraining() {
        synchronized (this.mLockJNI) {
            if (this.mHandle != -1) {
                if (this.mIsAlgoExist) {
                    if (DisplayEngineLibraries.nativeProcessAlgorithm(1, this.mHandle, 5, null, null) == 0) {
                        return 0;
                    }
                    DeLog.e(TAG, " nativeDeinitAlgorithm failed! ");
                    return -1;
                }
            }
            DeLog.i(TAG, "abortTraining: PROCESS_TRAINING_ENGINE is not running.");
            return 0;
        }
    }
}
