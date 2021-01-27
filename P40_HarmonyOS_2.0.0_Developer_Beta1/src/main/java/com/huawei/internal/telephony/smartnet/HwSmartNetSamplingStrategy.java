package com.huawei.internal.telephony.smartnet;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import java.util.ArrayList;
import java.util.List;

public class HwSmartNetSamplingStrategy {
    private static final int REQUEST_SAMPLE_INFO_EVENT_ID = 1;
    private static final int REQUEST_SAMPLE_INFO_TIMER_EVENT_ID = 2;
    private static final int SAMPLE_TIMER_INTERVAL = 5000;
    private static final String TAG = "HwSmartNetSamplingStrategy";
    private static HwSmartNetSamplingStrategy sInstance = null;
    private boolean isEstimateCanStart = false;
    private boolean isSampling = false;
    private ArrayList<ArrayList<CellSensor>> mCellSensorInfos;
    private Context mContext = null;
    private Handler mHandler = null;
    private Handler mStateHandler = null;
    private int numPhones;

    private HwSmartNetSamplingStrategy(Context context, Handler stateHandler) {
        this.mContext = context;
        this.mStateHandler = stateHandler;
        this.numPhones = TelephonyManagerEx.getPhoneCount();
        this.mCellSensorInfos = new ArrayList<>();
        for (int index = 0; index < this.numPhones; index++) {
            this.mCellSensorInfos.add(new ArrayList<>());
        }
        initHandler();
        logi("create HwSmartNetSamplingStrategy");
    }

    public static HwSmartNetSamplingStrategy init(Context context, Handler stateHandler) {
        HwSmartNetSamplingStrategy hwSmartNetSamplingStrategy;
        synchronized (HwSmartNetSamplingStrategy.class) {
            if (sInstance == null) {
                sInstance = new HwSmartNetSamplingStrategy(context, stateHandler);
            } else {
                logi("init() called multiple times!  sInstance = " + sInstance);
            }
            hwSmartNetSamplingStrategy = sInstance;
        }
        return hwSmartNetSamplingStrategy;
    }

    public static HwSmartNetSamplingStrategy getInstance() {
        HwSmartNetSamplingStrategy hwSmartNetSamplingStrategy;
        synchronized (HwSmartNetSamplingStrategy.class) {
            hwSmartNetSamplingStrategy = sInstance;
        }
        return hwSmartNetSamplingStrategy;
    }

    /* access modifiers changed from: private */
    public static void logi(String msg) {
        RlogEx.i(TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void logd(String msg) {
    }

    public boolean startSampling() {
        boolean z = false;
        if (this.isSampling || !HwSmartNetStateListener.getInstance().isAnySimLoaded()) {
            return false;
        }
        for (int index = 0; index < this.numPhones; index++) {
            this.mCellSensorInfos.get(index).clear();
        }
        this.isSampling = true;
        HwSmartNetEstimateStrategy.getInstance().initMatchInfoPoints();
        if (HwSmartNetStateListener.getInstance().isAllSimLoaded() && HwSmartNetStudyStrategy.getInstance().isAllSimSampleAndStudyDone(HwSmartNetRouteStrategy.getInstance().getCurrentRouteId(), HwSmartNetStateListener.getInstance().getIccIdHashs()) && HwSmartNetEstimateStrategy.getInstance().isMatchInfoStudied()) {
            z = true;
        }
        this.isEstimateCanStart = z;
        return true;
    }

    public boolean endSampling() {
        if (!this.isSampling) {
            return false;
        }
        this.mHandler.removeMessages(2);
        this.isSampling = false;
        this.isEstimateCanStart = false;
        int i = this.numPhones;
        return true;
    }

    public void abortSampling() {
        this.mHandler.removeMessages(2);
        this.isSampling = false;
        this.isEstimateCanStart = false;
        this.mStateHandler.obtainMessage(200).sendToTarget();
    }

    public void requestSampleInfo(int phoneId, int reason) {
        if (HwSmartNetStateListener.getInstance().isSimLoadedBySlot(phoneId)) {
            HwSmartNetStateListener.getInstance().requestCellSensor(phoneId, this.mHandler.obtainMessage(1, phoneId, reason));
            logd("requestSampleInfo :" + phoneId);
        }
    }

    private void requestSampleInfoTimer() {
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(2), 5000);
        logd("sendMessageDelayed REQUEST_SAMPLE_INFO_TIMER_EVENT_ID.");
    }

    public void requestSampleInfoAndStartTimer() {
        for (int index = 0; index < this.numPhones; index++) {
            requestSampleInfo(index, 204);
        }
        requestSampleInfoTimer();
    }

    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new SampleHandler(handlerThread.getLooper());
    }

    private void mergeSameCellIdInfo(CellSensor lastSampleInfo, CellSensor newSamplingInfo) {
        int blackPointType = newSamplingInfo.getBlackPointType();
        if (blackPointType != 0) {
            lastSampleInfo.setBlackType(blackPointType);
            if (lastSampleInfo.getExceptionStartTime() == 0) {
                lastSampleInfo.setExceptionStartTime(newSamplingInfo.getSamplingTime());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processSampleInfo(int phoneId, int reason, CellSensor result) {
        List<CellSensor> cellSensorInfos = this.mCellSensorInfos.get(phoneId);
        int length = cellSensorInfos.size();
        if (length == 0) {
            result.setRouteId(HwSmartNetRouteStrategy.getInstance().getCurrentRouteId());
            result.analyseAndUpdateBlackPointType();
            cellSensorInfos.add(result);
            return;
        }
        CellSensor lastSampleInfo = cellSensorInfos.get(length - 1);
        if (reason == 204 && lastSampleInfo.getMainCellId() == result.getMainCellId()) {
            logi("processSampleInfo phone id " + phoneId + " cell sensor timer request, main cell id not change ignore!");
            return;
        }
        result.setRouteId(HwSmartNetRouteStrategy.getInstance().getCurrentRouteId());
        result.analyseAndUpdateBlackPointType();
        logd("processSampleInfo phone id " + phoneId + " result = " + result.toString());
        if (lastSampleInfo.getMainCellId() == result.getMainCellId()) {
            mergeSameCellIdInfo(lastSampleInfo, result);
        } else {
            cellSensorInfos.add(result);
        }
    }

    public List<CellSensor> getCellSensorInfo(int phoneId) {
        return this.mCellSensorInfos.get(phoneId);
    }

    public void readTestDataAndProcess(int routeId, int slotId, String iccid) {
        this.isSampling = true;
        HwSmartNetRouteStrategy.getInstance().setCurrentRouteId(routeId);
        for (CellSensor cellSensor : HwSmartNetDb.getInstance().queryCellSensorTest(routeId, slotId, iccid)) {
            processSampleInfo(slotId, 204, cellSensor);
        }
        HwSmartNetRouteStrategy.getInstance().setCurrentRouteId(-1);
        this.mStateHandler.obtainMessage(HwSmartNetConstants.EVENT_STATE_ENTER_HOME_OR_COMPANY_FOR_TEST).sendToTarget();
    }

    /* access modifiers changed from: private */
    public class SampleHandler extends Handler {
        SampleHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (!HwSmartNetSamplingStrategy.this.isSampling) {
                HwSmartNetSamplingStrategy.logi("is not sampling, return message " + message.what);
                return;
            }
            HwSmartNetSamplingStrategy.logd("handle message " + message.what);
            int i = message.what;
            if (i != 1) {
                if (i != 2) {
                    super.handleMessage(message);
                } else {
                    HwSmartNetSamplingStrategy.this.requestSampleInfoAndStartTimer();
                }
            } else if (message.obj instanceof CellSensor) {
                HwSmartNetSamplingStrategy.this.processSampleInfo(message.arg1, message.arg2, (CellSensor) message.obj);
                if (HwSmartNetSamplingStrategy.this.isEstimateCanStart) {
                    boolean isSuccess = HwSmartNetEstimateStrategy.getInstance().executeEstimate(message.arg1, message.obj);
                    HwSmartNetSamplingStrategy.logd("EstimateState executeEstimate phoneid = " + message.arg1 + " execute isSuccess = " + isSuccess);
                }
            }
        }
    }
}
