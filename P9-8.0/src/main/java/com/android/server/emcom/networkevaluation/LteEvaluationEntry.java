package com.android.server.emcom.networkevaluation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.emcom.EmcomThread;
import java.util.Arrays;
import java.util.List;

class LteEvaluationEntry {
    static final int CELLULAR_TYPE_2G = 2000;
    static final int CELLULAR_TYPE_3G = 3000;
    static final int CELLULAR_TYPE_LTE = 4000;
    static final int CELLULAR_TYPE_UNKNOWN = 1000;
    private static final int INVALID_VALUE = -1;
    private static final int METRIC_COUNT = 8;
    private static final int METRIC_INTERVAL = 1000;
    private static final int MSG_TIMER_TRIGGERED = 10001;
    private static final String TAG = "LteEvaluation";
    private static volatile LteEvaluationEntry sLteEvaluationEntry;
    private int mCellularType;
    private Context mContext;
    private int mDataCardIndex;
    private Handler mOutputHandler;
    private boolean mRunning;
    private int[] mSignalStrengthArray = new int[8];
    private int mSignalStrengthWindowIndex;
    private TelephonyManager mTelephonyManager;
    private Handler mTimeHandler = new LteTimeHandler(EmcomThread.getInstanceLooper());
    private boolean mWindowFull;

    private class LteTimeHandler extends Handler {
        LteTimeHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (LteEvaluationEntry.this.mTimeHandler != null) {
                switch (msg.what) {
                    case 10001:
                        if (LteEvaluationEntry.this.mRunning) {
                            int signalStrength = LteEvaluationEntry.this.getSpecifiedCellSignalStrength(LteEvaluationEntry.this.mDataCardIndex);
                            if (-1 == signalStrength) {
                                Log.d(LteEvaluationEntry.TAG, "got an invalid signal strength");
                            } else {
                                LteEvaluationEntry.this.mSignalStrengthArray[LteEvaluationEntry.this.mSignalStrengthWindowIndex] = signalStrength;
                                LteEvaluationEntry lteEvaluationEntry = LteEvaluationEntry.this;
                                lteEvaluationEntry.mSignalStrengthWindowIndex = lteEvaluationEntry.mSignalStrengthWindowIndex + 1;
                                if (LteEvaluationEntry.this.mWindowFull) {
                                    LteEvaluationEntry.this.executeRankingProcess(8);
                                    if (LteEvaluationEntry.this.mSignalStrengthWindowIndex >= 8) {
                                        LteEvaluationEntry.this.mSignalStrengthWindowIndex = 0;
                                    }
                                } else if (LteEvaluationEntry.this.mSignalStrengthWindowIndex >= 8) {
                                    LteEvaluationEntry.this.executeRankingProcess(8);
                                    LteEvaluationEntry.this.mWindowFull = true;
                                    LteEvaluationEntry.this.mSignalStrengthWindowIndex = 0;
                                } else {
                                    LteEvaluationEntry.this.executeRankingProcess(LteEvaluationEntry.this.mSignalStrengthWindowIndex);
                                }
                            }
                            LteEvaluationEntry.this.triggerDelayed();
                            break;
                        }
                        break;
                    default:
                        Log.d(LteEvaluationEntry.TAG, "received a illegal message");
                        break;
                }
            }
        }
    }

    private LteEvaluationEntry(Context context, Handler handler) {
        this.mContext = context;
        this.mOutputHandler = handler;
    }

    public static LteEvaluationEntry getInstance(Context context, Handler handler) {
        if (sLteEvaluationEntry == null) {
            if (context == null || handler == null) {
                Log.e(TAG, "return null");
                return null;
            }
            synchronized (LteEvaluationEntry.class) {
                if (sLteEvaluationEntry == null) {
                    sLteEvaluationEntry = new LteEvaluationEntry(context, handler);
                }
            }
        }
        return sLteEvaluationEntry;
    }

    void startEvaluation(int dataCardIndex) {
        if (this.mRunning) {
            Log.d(TAG, "attempt to start a new LTE evaluation while a evaluation is already running");
            return;
        }
        Log.d(TAG, "start LTE evaluation");
        this.mDataCardIndex = dataCardIndex;
        this.mSignalStrengthWindowIndex = 0;
        Arrays.fill(this.mSignalStrengthArray, 0);
        this.mWindowFull = false;
        this.mRunning = true;
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        triggerDelayed();
    }

    void stopEvaluation() {
        if (this.mRunning) {
            this.mSignalStrengthWindowIndex = 0;
            this.mWindowFull = false;
            this.mRunning = false;
            return;
        }
        Log.d(TAG, "attempt to stop a stopped LTE evaluation");
    }

    private void triggerDelayed() {
        this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(10001), 1000);
    }

    private void cleanUpSignalStrengthArray() {
        if (this.mSignalStrengthArray == null) {
            this.mSignalStrengthArray = new int[8];
        }
        Arrays.fill(this.mSignalStrengthArray, 0);
        this.mSignalStrengthWindowIndex = 0;
    }

    private void executeRankingProcess(int length) {
        if (this.mSignalStrengthArray == null || length <= 0 || this.mOutputHandler == null) {
            Log.e(TAG, "try to executeRankingProcess() with null signalStrengthArray or handler");
        } else {
            LteRankingProcess.staticStartRanking(this.mSignalStrengthArray, length, this.mOutputHandler, this.mCellularType);
        }
    }

    private int getSpecifiedCellSignalStrength(int cellIndex) {
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        List<CellInfo> cellInfoList = this.mTelephonyManager.getAllCellInfo();
        if (cellInfoList == null || cellInfoList.isEmpty()) {
            Log.d(TAG, "null cellInfoList");
            return -1;
        } else if (cellIndex >= cellInfoList.size()) {
            Log.e(TAG, "incorrect cellIndex");
            return -1;
        } else {
            int signalStrength;
            int cellularType;
            CellInfo cellInfo = (CellInfo) cellInfoList.get(cellIndex);
            if (cellInfo instanceof CellInfoGsm) {
                signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
                cellularType = 2000;
            } else if (cellInfo instanceof CellInfoCdma) {
                signalStrength = ((CellInfoCdma) cellInfo).getCellSignalStrength().getDbm();
                cellularType = CELLULAR_TYPE_3G;
            } else if (cellInfo instanceof CellInfoWcdma) {
                signalStrength = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
                cellularType = CELLULAR_TYPE_3G;
            } else if (cellInfo instanceof CellInfoLte) {
                signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
                cellularType = 4000;
            } else {
                Log.d(TAG, "unidentified network type");
                signalStrength = -1;
                cellularType = 1000;
            }
            if (cellularType != this.mCellularType) {
                Log.d(TAG, "a cellular network switch is triggered, previously " + this.mCellularType + ", switch to " + cellularType);
                this.mCellularType = cellularType;
                cleanUpSignalStrengthArray();
            }
            return signalStrength;
        }
    }
}
