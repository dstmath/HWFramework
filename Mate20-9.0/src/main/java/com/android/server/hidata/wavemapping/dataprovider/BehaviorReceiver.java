package com.android.server.hidata.wavemapping.dataprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.telephony.TelephonyManager;
import com.android.server.hidata.wavemapping.chr.BuildBenefitStatisticsChrInfo;
import com.android.server.hidata.wavemapping.chr.BuildSpaceUserStatisticsChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.BehaviorDAO;
import com.android.server.hidata.wavemapping.dao.LocationDAO;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.ShowToast;
import java.util.concurrent.atomic.AtomicBoolean;

public class BehaviorReceiver {
    private static final String PG_AR_STATE_ACTION = "com.huawei.intent.action.PG_AR_STATE_ACTION";
    private static final String PG_RECEIVER_PERMISSION = "com.huawei.powergenie.receiverPermission";
    private static final int STEP_INCREASE_THRESHOLD = 5;
    private static final long STEP_RELEASE_THRESHOLD = 5000;
    public static final String TAG = ("WMapping." + BehaviorReceiver.class.getSimpleName());
    /* access modifiers changed from: private */
    public static int batch = 1;
    /* access modifiers changed from: private */
    public static boolean isCharging = true;
    /* access modifiers changed from: private */
    public static boolean mIsStationary = false;
    /* access modifiers changed from: private */
    public static int oldSim1Ready = -1;
    /* access modifiers changed from: private */
    public static int oldSimReady = -1;
    /* access modifiers changed from: private */
    public static boolean screenState = true;
    private BroadcastReceiver arReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                boolean mIsStationaryTmp = intent.getBooleanExtra("stationary", false);
                LogUtil.i("Stationary =" + mIsStationaryTmp);
                if (BehaviorReceiver.mIsStationary != mIsStationaryTmp) {
                    BehaviorReceiver.access$108();
                    boolean unused = BehaviorReceiver.mIsStationary = mIsStationaryTmp;
                    if (BehaviorReceiver.this.mMachineHandler != null) {
                        if (BehaviorReceiver.mIsStationary) {
                            LogUtil.i("arReceiver: send STATION");
                            BehaviorReceiver.this.mMachineHandler.sendEmptyMessage(111);
                        } else {
                            LogUtil.i("arReceiver send MOVE");
                            BehaviorReceiver.this.mMachineHandler.sendEmptyMessage(110);
                            ShowToast.showToast("Moving by AR");
                        }
                    }
                    LogUtil.i("AR Status Changed, batch =" + BehaviorReceiver.batch);
                }
            }
        }
    };
    private BehaviorDAO behaviorDAO;
    private AtomicBoolean mAccSensorRegistered = new AtomicBoolean(false);
    private BuildBenefitStatisticsChrInfo mBuildBenefitStatisticsChrInfo = null;
    private BuildSpaceUserStatisticsChrInfo mBuildSpaceUserStatisticsChrInfo = null;
    /* access modifiers changed from: private */
    public Context mCtx;
    private FrequentLocation mFrequentLocation = null;
    /* access modifiers changed from: private */
    public int mLastStepCnt;
    private int mLastWifiBatch;
    private int mLastWifiStepCnt;
    private LocationDAO mLocationDAO;
    /* access modifiers changed from: private */
    public Handler mMachineHandler;
    private final StepSensorEventListener mSensorEventListener = new StepSensorEventListener();
    private SensorManager mSensorManager;
    private Sensor mStepCntSensor;
    private ParameterInfo param;
    private int savedBatch = 0;
    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            LogUtil.i("receive screen state");
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                BehaviorReceiver.access$108();
                boolean unused = BehaviorReceiver.screenState = true;
                LogUtil.i("BehaviorReceiver screenOn = " + BehaviorReceiver.screenState + ", batch = " + BehaviorReceiver.batch);
                BehaviorReceiver.this.registerStepCntSensor();
            } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                BehaviorReceiver.access$108();
                boolean unused2 = BehaviorReceiver.screenState = false;
                LogUtil.i("BehaviorReceiver screenOff = " + BehaviorReceiver.screenState + ", batch = " + BehaviorReceiver.batch);
                BehaviorReceiver.this.unregisterStepCntSensor();
            }
        }
    };
    private BroadcastReceiver simReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean sendMsg = false;
            try {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    LogUtil.i("BehaviorReceiver SIM state changed " + intent.getAction());
                    if (BehaviorReceiver.this.mMachineHandler != null) {
                        TelephonyManager tm = (TelephonyManager) BehaviorReceiver.this.mCtx.getSystemService("phone");
                        if (tm == null || 5 != tm.getSimState(0)) {
                            LogUtil.i("SIM0 invalid : " + BehaviorReceiver.oldSimReady);
                            if (BehaviorReceiver.oldSimReady != 0) {
                                sendMsg = true;
                            }
                            int unused = BehaviorReceiver.oldSimReady = 0;
                        } else {
                            LogUtil.i("SIM0 ready : " + BehaviorReceiver.oldSimReady);
                            if (1 != BehaviorReceiver.oldSimReady) {
                                sendMsg = true;
                            }
                            int unused2 = BehaviorReceiver.oldSimReady = 1;
                        }
                        if (tm == null || 5 != tm.getSimState(1)) {
                            LogUtil.i("SIM1 invalid : " + BehaviorReceiver.oldSim1Ready);
                            if (BehaviorReceiver.oldSim1Ready != 0) {
                                sendMsg = true;
                            }
                            int unused3 = BehaviorReceiver.oldSim1Ready = 0;
                        } else {
                            LogUtil.i("SIM1 ready : " + BehaviorReceiver.oldSim1Ready);
                            if (1 != BehaviorReceiver.oldSim1Ready) {
                                sendMsg = true;
                            }
                            int unused4 = BehaviorReceiver.oldSim1Ready = 1;
                        }
                        if (sendMsg) {
                            BehaviorReceiver.this.mMachineHandler.sendEmptyMessage(94);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.e("simReceiver:" + e.getMessage());
            }
        }
    };
    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            LogUtil.i("receive power state");
            if ("android.intent.action.ACTION_POWER_CONNECTED".equals(intent.getAction())) {
                boolean unused = BehaviorReceiver.isCharging = true;
                LogUtil.i("BehaviorReceiver Power Connected ");
                BehaviorReceiver.this.mMachineHandler.sendEmptyMessage(112);
                BehaviorReceiver.this.uploadBenefitCHR();
                BehaviorReceiver.this.uploadSpaceUserCHR();
            } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(intent.getAction())) {
                LogUtil.i("BehaviorReceiver Power Disconnected ");
                BehaviorReceiver.this.mMachineHandler.sendEmptyMessage(113);
                boolean unused2 = BehaviorReceiver.isCharging = false;
                BehaviorReceiver.this.uploadBenefitCHR();
                BehaviorReceiver.this.uploadSpaceUserCHR();
            }
        }
    };

    class StepSensorEventListener implements SensorEventListener {
        private int mMotionDetectedCnt = 0;
        private long mSensorEventRcvdTs = -1;

        public StepSensorEventListener() {
            int unused = BehaviorReceiver.this.mLastStepCnt = 0;
        }

        public void reset() {
            int unused = BehaviorReceiver.this.mLastStepCnt = 0;
            this.mMotionDetectedCnt = 0;
            this.mSensorEventRcvdTs = -1;
        }

        public void onSensorChanged(SensorEvent event) {
            if (event != null && event.sensor != null && event.sensor.getType() == 19) {
                long currentTimestamp = System.currentTimeMillis();
                int currentStepCnt = (int) event.values[0];
                if (BehaviorReceiver.this.mMachineHandler != null) {
                    if (currentStepCnt - BehaviorReceiver.this.mLastStepCnt > 0) {
                        this.mMotionDetectedCnt++;
                        if (this.mMotionDetectedCnt == 5) {
                            LogUtil.i("step counter: send MOVE");
                            this.mMotionDetectedCnt = 0;
                            BehaviorReceiver.this.mMachineHandler.sendEmptyMessage(110);
                        }
                    } else if (this.mSensorEventRcvdTs > 0 && currentTimestamp - this.mSensorEventRcvdTs > BehaviorReceiver.STEP_RELEASE_THRESHOLD) {
                        this.mMotionDetectedCnt = 0;
                        LogUtil.i("Step Counter: send STATION");
                        BehaviorReceiver.this.mMachineHandler.sendEmptyMessage(111);
                    }
                    int unused = BehaviorReceiver.this.mLastStepCnt = currentStepCnt;
                    this.mSensorEventRcvdTs = currentTimestamp;
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            LogUtil.i("SensorEventListener::onAccuracyChanged, accuracy = " + accuracy);
        }
    }

    static /* synthetic */ int access$108() {
        int i = batch;
        batch = i + 1;
        return i;
    }

    public BehaviorReceiver(Handler handler) {
        LogUtil.i("BehaviorReceiverHandler");
        this.mCtx = ContextManager.getInstance().getContext();
        try {
            this.param = ParamManager.getInstance().getParameterInfo();
            this.behaviorDAO = new BehaviorDAO();
            this.mLocationDAO = new LocationDAO();
            this.savedBatch = this.behaviorDAO.getBatch();
            if (this.savedBatch == 0) {
                this.behaviorDAO.insert(batch);
                LogUtil.d(" no default record, init...");
            } else {
                batch = this.savedBatch + 1;
                LogUtil.i(" default record, history batch=" + this.savedBatch);
            }
            this.mCtx.registerReceiver(this.arReceiver, new IntentFilter(PG_AR_STATE_ACTION), PG_RECEIVER_PERMISSION, null);
            this.mSensorManager = (SensorManager) this.mCtx.getSystemService("sensor");
            if (this.mSensorManager != null) {
                this.mStepCntSensor = this.mSensorManager.getDefaultSensor(19);
                registerStepCntSensor();
            } else {
                LogUtil.e(" mSensorManager == null");
            }
            IntentFilter screenFilter = new IntentFilter();
            screenFilter.addAction("android.intent.action.SCREEN_ON");
            screenFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mCtx.registerReceiver(this.screenReceiver, screenFilter);
            IntentFilter usbFilter = new IntentFilter();
            usbFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
            usbFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
            this.mCtx.registerReceiver(this.usbReceiver, usbFilter);
            IntentFilter simFilter = new IntentFilter();
            simFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
            this.mCtx.registerReceiver(this.simReceiver, simFilter);
            this.mBuildBenefitStatisticsChrInfo = new BuildBenefitStatisticsChrInfo();
            this.mBuildSpaceUserStatisticsChrInfo = new BuildSpaceUserStatisticsChrInfo();
            this.mMachineHandler = handler;
        } catch (Exception e) {
            LogUtil.e("start:" + e.getMessage());
        }
    }

    public void uploadBenefitCHR() {
        long now = System.currentTimeMillis();
        long oobtime = this.mLocationDAO.getOOBTime();
        long lastupdatetime = this.mLocationDAO.getBenefitCHRTime();
        long maxtime = oobtime > lastupdatetime ? oobtime : lastupdatetime;
        boolean retH = false;
        boolean retO = false;
        LogUtil.i("now =" + now + " oob time =" + oobtime + " last Benefit CHR time=" + lastupdatetime);
        if (maxtime > 0 && now - maxtime > Constant.MILLISEC_SEVEN_DAYS) {
            retH = this.mBuildBenefitStatisticsChrInfo.commitCHR(Constant.NAME_FREQLOCATION_HOME);
            retO = this.mBuildBenefitStatisticsChrInfo.commitCHR(Constant.NAME_FREQLOCATION_OFFICE);
        }
        if (retH && retO) {
            this.mLocationDAO.updateBenefitCHRTime(now);
        }
    }

    public void uploadSpaceUserCHR() {
        long now = System.currentTimeMillis();
        long oobtime = this.mLocationDAO.getOOBTime();
        long lastupdatetime = this.mLocationDAO.getSpaceUserCHRTime();
        long maxtime = oobtime > lastupdatetime ? oobtime : lastupdatetime;
        boolean retH = false;
        boolean retO = false;
        long uploadTime = Constant.MILLISEC_ONE_MONTH;
        if (this.param != null && this.param.isBetaUser()) {
            uploadTime = Constant.MILLISEC_SEVEN_DAYS;
        }
        LogUtil.i("now =" + now + " oob time =" + oobtime + " last SpaceUser CHR time=" + lastupdatetime);
        if (maxtime > 0 && now - maxtime > uploadTime) {
            retH = this.mBuildSpaceUserStatisticsChrInfo.commitCHR(Constant.NAME_FREQLOCATION_HOME);
            retO = this.mBuildSpaceUserStatisticsChrInfo.commitCHR(Constant.NAME_FREQLOCATION_OFFICE);
            this.mBuildSpaceUserStatisticsChrInfo.commitCHR(Constant.NAME_FREQLOCATION_OTHER);
        }
        if (retH && retO) {
            this.mLocationDAO.updateSpaceUserCHRTime(now);
        }
    }

    /* access modifiers changed from: private */
    public void registerStepCntSensor() {
        if (this.param == null) {
            LogUtil.e("registerStepCntSensor, not register due to NULL param");
        } else if (this.param.isFactoryVer()) {
            LogUtil.w("registerStepCntSensor, not register due to Factory version");
        } else {
            if (!this.mAccSensorRegistered.get()) {
                LogUtil.i("registerStepCntSensor, mSensorEventListener");
                this.mSensorEventListener.reset();
                this.mSensorManager.registerListener(this.mSensorEventListener, this.mStepCntSensor, 3);
                this.mAccSensorRegistered.set(true);
            }
        }
    }

    /* access modifiers changed from: private */
    public void unregisterStepCntSensor() {
        if (this.mAccSensorRegistered.get() && this.mSensorEventListener != null) {
            LogUtil.i("unregisterStepCntSensor, mSensorEventListener");
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mAccSensorRegistered.set(false);
        }
    }

    public void checkMoveBtwWifiScans() {
        if (this.mLastWifiBatch == batch) {
            if (this.mLastStepCnt - this.mLastWifiStepCnt > 5) {
                batch++;
                LogUtil.i(" checkMoveBtwWifiScans, between two scans, steps=" + offsetStepCnt);
            }
        }
        this.mLastWifiStepCnt = this.mLastStepCnt;
        this.mLastWifiBatch = batch;
    }

    public void saveBatch() {
        if (this.behaviorDAO.update(batch)) {
            LogUtil.i("BehaviorReceiver, batch update success, batch:" + batch);
            return;
        }
        LogUtil.d("BehaviorReceiver, batch update failure, batch :" + batch);
    }

    public void stop() {
        LogUtil.d("wifi stop");
        this.mCtx.unregisterReceiver(this.screenReceiver);
        this.mCtx.unregisterReceiver(this.arReceiver);
    }

    public static int getBatch() {
        return batch;
    }

    public static boolean getScrnState() {
        return screenState;
    }

    public static boolean getArState() {
        return mIsStationary;
    }

    public boolean stopListen() {
        return true;
    }

    public static boolean isCharging() {
        return isCharging;
    }
}
