package com.huawei.displayengine;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.displayengine.DisplayEngineDBManager;
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
    private static String mAlgoXmlPath = "";
    private AlgoParam mAlgoInfo;
    private final Context mContext;
    private int mHandle = -1;
    private boolean mIsAlgoExist = false;
    private Object mLockJNI = new Object();

    public BrightnessTrainingAlgoImpl(Context context) {
        DElog.i(TAG, "BrightnessTrainingAlgoImpl enter");
        this.mContext = context;
    }

    private int readDragInfo() {
        DisplayEngineDBManager dbManager = DisplayEngineDBManager.getInstance(this.mContext);
        ArrayList<Bundle> items = dbManager.getAllRecords(DisplayEngineDBManager.DragInformationKey.TAG, new Bundle());
        DisplayEngineDataCleaner dataCleaner = DisplayEngineDataCleaner.getInstance(this.mContext);
        ArrayList<Bundle> items2 = dataCleaner.cleanData(items, 0);
        this.mAlgoInfo.mDragCount = 0;
        this.mAlgoInfo.mDragSize = 0;
        this.mAlgoInfo.mDragInfo = "";
        if (items2 != null) {
            int i = 0;
            while (i < items2.size()) {
                Bundle data = items2.get(i);
                long time = data.getLong("TimeStamp");
                float start = data.getFloat(DisplayEngineDBManager.DragInformationKey.STARTPOINT);
                float stop = data.getFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT);
                int al = data.getInt("AmbientLight");
                int appType = data.getInt("AppType");
                int i2 = data.getInt(DisplayEngineDBManager.DragInformationKey.GAMESTATE);
                String string = data.getString(DisplayEngineDBManager.DragInformationKey.PACKAGE);
                DisplayEngineDBManager dbManager2 = dbManager;
                this.mAlgoInfo.mDragCount++;
                this.mAlgoInfo.mDragInfo = this.mAlgoInfo.mDragInfo + "[" + time + ", " + start + ", " + stop + ", " + al + ", " + ((int) data.getBoolean(DisplayEngineDBManager.DragInformationKey.PROXIMITYPOSITIVE)) + ", " + appType + "]";
                i++;
                dbManager = dbManager2;
                items2 = items2;
                dataCleaner = dataCleaner;
            }
            ArrayList<Bundle> arrayList = items2;
            DisplayEngineDataCleaner displayEngineDataCleaner = dataCleaner;
            this.mAlgoInfo.mDragSize = this.mAlgoInfo.mDragInfo.length();
        } else {
            ArrayList<Bundle> arrayList2 = items2;
            DisplayEngineDataCleaner displayEngineDataCleaner2 = dataCleaner;
        }
        if (this.mAlgoInfo.mDragCount >= 2) {
            return 0;
        }
        DElog.i(TAG, "DragInfo number is not enough ! count: " + this.mAlgoInfo.mDragCount);
        return -1;
    }

    private int readCurveByTag(DisplayEngineDBManager dbManager, StringBuffer text, String name) {
        if (text == null || name == null) {
            DElog.i(TAG, "text is null");
            return -1;
        }
        ArrayList<Bundle> items = dbManager.getAllRecords(name, new Bundle());
        if (items == null) {
            DElog.i(TAG, "Read " + name + " failed !");
            return -1;
        } else if (items.size() == 0) {
            DElog.i(TAG, "DisplayEngineDB low curve size is 0");
            return -1;
        } else {
            for (int i = 0; i < items.size(); i++) {
                Bundle data = items.get(i);
                float al = data.getFloat("AmbientLight");
                float bl = data.getFloat(DisplayEngineDBManager.BrightnessCurveKey.BL);
                text.append("[" + al + "," + bl + "]");
            }
            return items.size();
        }
    }

    private int readBrightnessCurve() {
        DisplayEngineDBManager dbManager = DisplayEngineDBManager.getInstance(this.mContext);
        if (dbManager == null) {
            DElog.e(TAG, "dbManager is null");
            return -1;
        }
        StringBuffer low_text = new StringBuffer();
        int count = readCurveByTag(dbManager, low_text, "BrightnessCurveLow");
        if (count <= 0) {
            DElog.i(TAG, "Read low failed!");
            return -1;
        }
        this.mAlgoInfo.mBLCurveTypeLowLuma = low_text.toString();
        this.mAlgoInfo.mLowLumaCount = count;
        StringBuffer middle_text = new StringBuffer();
        int count2 = readCurveByTag(dbManager, middle_text, "BrightnessCurveMiddle");
        if (count2 <= 0) {
            DElog.i(TAG, "Read middle failed!");
            return -1;
        }
        this.mAlgoInfo.mBLCurveTypeMedialLuma = middle_text.toString();
        this.mAlgoInfo.mMedialLumaCount = count2;
        StringBuffer hight_text = new StringBuffer();
        int count3 = readCurveByTag(dbManager, hight_text, "BrightnessCurveHigh");
        if (count3 <= 0) {
            DElog.i(TAG, "Read hight failed!");
            return -1;
        }
        this.mAlgoInfo.mBLCurveTypeHighLuma = hight_text.toString();
        this.mAlgoInfo.mHighLumaCount = count3;
        StringBuffer default_text = new StringBuffer();
        int count4 = readCurveByTag(dbManager, default_text, "BrightnessCurveDefault");
        if (count4 <= 0) {
            DElog.i(TAG, "Read default failed!");
            return -1;
        }
        this.mAlgoInfo.mBLCurveTypeDefaultLuma = default_text.toString();
        this.mAlgoInfo.mDefaultLumaCount = count4;
        ArrayList<Bundle> items = dbManager.getAllRecords("AlgorithmESCW", new Bundle());
        if (items == null) {
            DElog.i(TAG, "DisplayEngineDB ESCW = null");
            return -1;
        } else if (items.size() == 0) {
            DElog.i(TAG, "DisplayEngineDB ESCW size is 0");
            return -1;
        } else {
            StringBuffer text = new StringBuffer();
            text.append("[");
            for (int i = 0; i < items.size(); i++) {
                float escw = items.get(i).getFloat(DisplayEngineDBManager.AlgorithmESCWKey.ESCW);
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

    private int writeCurveByTag(DisplayEngineDBManager dbManager, byte[] curve, String name, int buffer_offset) {
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
        data.putFloatArray(DisplayEngineDBManager.BrightnessCurveKey.BL, blValues);
        dbManager.addorUpdateRecord(name, data);
        return buffer_offset2;
    }

    private int writeBrightnessCurve(byte[] curve) {
        DisplayEngineDBManager dbManager = DisplayEngineDBManager.getInstance(this.mContext);
        if (dbManager == null) {
            DElog.e(TAG, "dbManager is null");
            return -1;
        }
        Bundle data = new Bundle();
        float[] escwValues = new float[9];
        for (int i = 0; i < 9; i++) {
            escwValues[i] = getFloat(curve, 0 + (i * 4));
        }
        data.putFloatArray(DisplayEngineDBManager.AlgorithmESCWKey.ESCW, escwValues);
        dbManager.addorUpdateRecord("AlgorithmESCW", data);
        int buffer_offset = writeCurveByTag(dbManager, curve, "BrightnessCurveLow", 36);
        if (buffer_offset <= 0) {
            DElog.e(TAG, "Write  low curvefailed! ");
            return -1;
        }
        int buffer_offset2 = writeCurveByTag(dbManager, curve, "BrightnessCurveMiddle", buffer_offset);
        if (buffer_offset2 <= 0) {
            DElog.e(TAG, "Write  middle curve failed! ");
            return -1;
        }
        int buffer_offset3 = writeCurveByTag(dbManager, curve, "BrightnessCurveHigh", buffer_offset2);
        if (buffer_offset3 <= 0) {
            DElog.e(TAG, "Write  middle high failed! ");
            return -1;
        } else if (writeCurveByTag(dbManager, curve, "BrightnessCurveDefault", buffer_offset3) > 0) {
            return 0;
        } else {
            DElog.e(TAG, "Write  middle default failed! ");
            return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        r3 = new byte[2000];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003c, code lost:
        if (com.huawei.displayengine.DisplayEngineLibraries.nativeProcessAlgorithm(1, r9.mHandle, 1, null, r3) == 0) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003e, code lost:
        com.huawei.displayengine.DElog.i(TAG, " PROCESS_TRAINING_ENGINE failed!");
        r0 = r9.mLockJNI;
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
        com.huawei.displayengine.DElog.i(TAG, "ESCW " + r3[0] + ", " + r3[1] + ", " + r3[2] + ", " + r3[3] + ", eswValue = " + r3[4]);
        writeBrightnessCurve(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r0 = android.os.IPowerManager.Stub.asInterface(android.os.ServiceManager.getService("power"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009f, code lost:
        if (r0 != null) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a1, code lost:
        com.huawei.displayengine.DElog.i(TAG, "power is null !");
        r1 = r9.mLockJNI;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00aa, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        destroyAlgo();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00ae, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00af, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b3, code lost:
        r1 = new android.os.Bundle();
        r1.putInt("CurveUpdateFlag", 1);
        r0.hwBrightnessSetData("PersonalizedBrightness", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c3, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c4, code lost:
        com.huawei.displayengine.DElog.i(TAG, "Failed to call hwbrightness error:" + r0.getMessage());
     */
    private int processAlgo() {
        synchronized (this.mLockJNI) {
            if (DisplayEngineLibraries.nativeProcessAlgorithm(1, this.mHandle, 3, this.mAlgoInfo, null) != 0) {
                DElog.e(TAG, " SETPARAM_TRAINING_ENGINE failed");
                return -1;
            } else if (DisplayEngineLibraries.nativeProcessAlgorithm(1, this.mHandle, 0, null, null) != 0) {
                DElog.e(TAG, " CREATE_TRAINING_ENGINE failed");
                return -1;
            } else {
                this.mIsAlgoExist = true;
            }
        }
        synchronized (this.mLockJNI) {
            destroyAlgo();
        }
        return 0;
    }

    private int destroyAlgo() {
        if (this.mIsAlgoExist) {
            int ret = DisplayEngineLibraries.nativeProcessAlgorithm(1, this.mHandle, 2, null, null);
            if (ret != 0) {
                DElog.e(TAG, " DESTROY_TRAINING_ENGINE failed");
            }
            this.mIsAlgoExist = false;
            return ret;
        }
        DElog.i(TAG, " destroyAlgo failed, algo is not exist!");
        return -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x02bb, code lost:
        if (processAlgo() == 0) goto L_0x02c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x02bd, code lost:
        com.huawei.displayengine.DElog.i(TAG, " processAlgo failed! ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x02c4, code lost:
        r5 = r14.mLockJNI;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x02c6, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        r0 = com.huawei.displayengine.DisplayEngineLibraries.nativeDeinitAlgorithm(1, r14.mHandle);
        r14.mHandle = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x02d0, code lost:
        if (r0 == 0) goto L_0x02db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x02d2, code lost:
        com.huawei.displayengine.DElog.e(TAG, " nativeDeinitAlgorithm failed! ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x02d9, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x02da, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x02db, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x02dc, code lost:
        com.huawei.displayengine.DElog.i(TAG, "processTraining  stop ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x02e3, code lost:
        return 0;
     */
    public int processTraining() {
        DElog.i(TAG, "processTraining  start !! ");
        this.mAlgoInfo = new AlgoParam();
        if (readDragInfo() != 0) {
            DElog.i(TAG, "no DragInfo ! ");
            return -1;
        }
        if (readBrightnessCurve() != 0) {
            DElog.i(TAG, "no BrightnessCurve  in DataBase! ");
            try {
                IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                if (power == null) {
                    DElog.i(TAG, "power is null");
                    return -1;
                }
                Bundle data = new Bundle();
                int hwBrightnessGetData = power.hwBrightnessGetData("PersonalizedBrightness", data);
                ArrayList<PointF> list = data.getParcelableArrayList("DefaultCurve");
                if (list == null) {
                    DElog.e(TAG, "list is null!");
                    return -1;
                }
                StringBuffer text = new StringBuffer();
                for (int i = 0; i < list.size(); i++) {
                    float al = list.get(i).x;
                    float bl = list.get(i).y;
                    text.append("[" + al + "," + bl + "]");
                }
                this.mAlgoInfo.mBLCurveTypeLowLuma = text.toString();
                this.mAlgoInfo.mLowLumaCount = list.size();
                this.mAlgoInfo.mBLCurveTypeMedialLuma = text.toString();
                this.mAlgoInfo.mMedialLumaCount = list.size();
                this.mAlgoInfo.mBLCurveTypeHighLuma = text.toString();
                this.mAlgoInfo.mHighLumaCount = list.size();
                this.mAlgoInfo.mBLCurveTypeDefaultLuma = text.toString();
                this.mAlgoInfo.mDefaultLumaCount = list.size();
                this.mAlgoInfo.mESCW = "[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0]";
                this.mAlgoInfo.mESCWCount = 1;
                this.mAlgoInfo.mFirstInital = 1;
            } catch (RemoteException e) {
                DElog.i(TAG, "Failed to call hwbrightness error:" + e.getMessage());
                return -1;
            }
        }
        if (this.mAlgoInfo.mLowLumaCount == 35 && this.mAlgoInfo.mMedialLumaCount == 35 && this.mAlgoInfo.mHighLumaCount == 35 && this.mAlgoInfo.mDefaultLumaCount == 35 && this.mAlgoInfo.mESCWCount == 1) {
            this.mAlgoInfo.mLowLumaSize = this.mAlgoInfo.mBLCurveTypeLowLuma.length();
            this.mAlgoInfo.mMedialLumaSize = this.mAlgoInfo.mBLCurveTypeMedialLuma.length();
            this.mAlgoInfo.mHighLumaSize = this.mAlgoInfo.mBLCurveTypeHighLuma.length();
            this.mAlgoInfo.mDefaultLumaSize = this.mAlgoInfo.mBLCurveTypeDefaultLuma.length();
            this.mAlgoInfo.mESCWSize = this.mAlgoInfo.mESCW.length();
            DElog.i(TAG, "input mDragInfo: " + this.mAlgoInfo.mDragInfo);
            DElog.i(TAG, "input mDragSize: " + this.mAlgoInfo.mDragSize + ", mDragCount " + this.mAlgoInfo.mDragCount);
            StringBuilder sb = new StringBuilder();
            sb.append("input mBLCurveTypeLowLuma: ");
            sb.append(this.mAlgoInfo.mBLCurveTypeLowLuma);
            DElog.i(TAG, sb.toString());
            DElog.i(TAG, "input mLowLumaCount: " + this.mAlgoInfo.mLowLumaCount);
            DElog.i(TAG, "input mBLCurveTypeMedialLuma: " + this.mAlgoInfo.mBLCurveTypeMedialLuma);
            DElog.i(TAG, "input mMedialLumaCount: " + this.mAlgoInfo.mMedialLumaCount);
            DElog.i(TAG, "input mBLCurveTypeHighLuma: " + this.mAlgoInfo.mBLCurveTypeHighLuma);
            DElog.i(TAG, "input mHighLumaCount: " + this.mAlgoInfo.mHighLumaCount);
            DElog.i(TAG, "input mBLCurveTypeDefaultLuma: " + this.mAlgoInfo.mBLCurveTypeDefaultLuma);
            DElog.i(TAG, "input mDefaultLumaCount: " + this.mAlgoInfo.mDefaultLumaCount);
            DElog.i(TAG, "input mESCW: " + this.mAlgoInfo.mESCW);
            DElog.i(TAG, "input mESCWCount: " + this.mAlgoInfo.mESCWCount);
            synchronized (this.mLockJNI) {
                int ret = DisplayEngineLibraries.nativeInitAlgorithm(1);
                if (ret >= 0) {
                    this.mHandle = ret;
                } else {
                    this.mHandle = -1;
                    DElog.e(TAG, "nativeInitAlgorithm failed! ");
                    return -1;
                }
            }
        } else {
            DElog.i(TAG, "Count is not correct ! ");
            DElog.i(TAG, "LowLumaCount = " + this.mAlgoInfo.mLowLumaCount);
            DElog.i(TAG, "MedialLumaCount = " + this.mAlgoInfo.mMedialLumaCount);
            DElog.i(TAG, "HighLumaCount = " + this.mAlgoInfo.mHighLumaCount);
            DElog.i(TAG, "DefaultLumaCount = " + this.mAlgoInfo.mDefaultLumaCount);
            DElog.i(TAG, "ESCWCount = " + this.mAlgoInfo.mESCWCount);
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
                    DElog.e(TAG, " nativeDeinitAlgorithm failed! ");
                    return -1;
                }
            }
            DElog.i(TAG, "abortTraining: PROCESS_TRAINING_ENGINE is not running.");
            return 0;
        }
    }
}
