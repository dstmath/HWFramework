package com.huawei.internal.telephony.smartnet;

import android.content.Context;
import android.os.Message;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.util.StateEx;
import com.huawei.internal.util.StateMachineEx;

public class HwSmartNetStateMachine extends StateMachineEx {
    private static final boolean HANDLED = true;
    private static final int INVALID = -1;
    private static final Object LOCK = new Object();
    private static final boolean NOT_HANDLED = false;
    private static final int SLOT0 = 0;
    private static final int SLOT1 = 1;
    private static final String TAG = "HwSmartNetStateMachine";
    private static HwSmartNetStateMachine sInstance;
    private Context mContext = null;
    private final DefaultState mDefaultState = new DefaultState();
    private final EstimateState mEstimateState = new EstimateState();
    private HwSmartNetEstimateStrategy mEstimateStrategy = null;
    private HwSmartNetRouteStrategy mRouteStrategy = null;
    private final SamplingState mSamplingState = new SamplingState();
    private HwSmartNetSamplingStrategy mSamplingStrategy = null;
    private HwSmartNetStateListener mSmartNetStateListener = null;
    private final StudyState mStudyState = new StudyState();
    private HwSmartNetStudyStrategy mStudyStrategy = null;

    private HwSmartNetStateMachine(Context context) {
        super(TAG);
        this.mContext = context;
        addState(this.mDefaultState);
        addState(this.mSamplingState, this.mDefaultState);
        addState(this.mStudyState, this.mDefaultState);
        addState(this.mEstimateState, this.mDefaultState);
        setInitialState(this.mDefaultState);
        HwSmartNetDb.make(this.mContext, getHandler());
        this.mSamplingStrategy = HwSmartNetSamplingStrategy.init(this.mContext, getHandler());
        this.mStudyStrategy = HwSmartNetStudyStrategy.init(this.mContext, getHandler());
        this.mEstimateStrategy = HwSmartNetEstimateStrategy.init(this.mContext, getHandler());
        this.mRouteStrategy = HwSmartNetRouteStrategy.init(this.mContext, getHandler());
        this.mSmartNetStateListener = HwSmartNetStateListener.make(this.mContext, getHandler());
        rlogi("create HwSmartNetStateMachine");
    }

    public static HwSmartNetStateMachine make(Context context) {
        HwSmartNetStateMachine hwSmartNetStateMachine;
        synchronized (LOCK) {
            if (sInstance != null) {
                throw new RuntimeException("HwSmartNetStateMachine.make() should only be called once");
            }
            sInstance = new HwSmartNetStateMachine(context);
            sInstance.start();
            hwSmartNetStateMachine = sInstance;
        }
        return hwSmartNetStateMachine;
    }

    /* access modifiers changed from: protected */
    public void rlogi(String string) {
        RlogEx.i(getName(), string);
    }

    /* access modifiers changed from: private */
    public class DefaultState extends StateEx {
        private DefaultState() {
        }

        public void enter() {
            HwSmartNetStateMachine.this.rlogi("DefaultState enter");
        }

        public void exit() {
            HwSmartNetStateMachine.this.rlogi("DefaultState exit");
        }

        public boolean processMessage(Message msg) {
            HwSmartNetStateMachine hwSmartNetStateMachine = HwSmartNetStateMachine.this;
            hwSmartNetStateMachine.rlogi("DefaultState Received Msg " + msg.what);
            if (msg.what != 451) {
                HwSmartNetStateMachine hwSmartNetStateMachine2 = HwSmartNetStateMachine.this;
                hwSmartNetStateMachine2.rlogi("DefaultState.processMessage default: NOT_HANDLED " + msg.what);
                return false;
            }
            HwSmartNetStateMachine.this.mSmartNetStateListener.registerSampleListener();
            HwSmartNetStateMachine.this.deferMessage(msg);
            HwSmartNetStateMachine.this.transitionTo(HwSmartNetStateMachine.this.mSamplingState);
            return HwSmartNetStateMachine.HANDLED;
        }
    }

    /* access modifiers changed from: private */
    public class SamplingState extends StateEx {
        private SamplingState() {
        }

        public void enter() {
            HwSmartNetStateMachine.this.rlogi("SamplingState enter");
        }

        public void exit() {
            HwSmartNetStateMachine.this.rlogi("SamplingState exit");
        }

        public boolean processMessage(Message msg) {
            HwSmartNetStateMachine hwSmartNetStateMachine = HwSmartNetStateMachine.this;
            hwSmartNetStateMachine.rlogi("SamplingState Received Msg " + msg.what);
            int i = msg.what;
            switch (i) {
                case 201:
                case 202:
                case 203:
                    HwSmartNetStateMachine.this.mSamplingStrategy.requestSampleInfo(msg.arg1, msg.what);
                    return HwSmartNetStateMachine.HANDLED;
                default:
                    switch (i) {
                        case HwSmartNetConstants.EVENT_STATE_EXIT_HOME_OR_COMPANY /* 451 */:
                            if (HwSmartNetStateMachine.this.mSamplingStrategy.startSampling()) {
                                HwSmartNetStateMachine.this.mSamplingStrategy.requestSampleInfoAndStartTimer();
                                return HwSmartNetStateMachine.HANDLED;
                            }
                            HwSmartNetStateMachine.this.rlogi("sim not ready, abort this sampling!");
                            HwSmartNetStateMachine.this.transitionTo(HwSmartNetStateMachine.this.mDefaultState);
                            return HwSmartNetStateMachine.HANDLED;
                        case HwSmartNetConstants.EVENT_STATE_ENTER_HOME_OR_COMPANY /* 452 */:
                        case HwSmartNetConstants.EVENT_STATE_ENTER_HOME_OR_COMPANY_FOR_TEST /* 453 */:
                            HwSmartNetStateMachine.this.mSmartNetStateListener.unRegisterSampleListener();
                            if (!HwSmartNetStateMachine.this.mSamplingStrategy.endSampling()) {
                                HwSmartNetStateMachine.this.rlogi("not sampling state, change to default state!");
                                HwSmartNetStateMachine.this.transitionTo(HwSmartNetStateMachine.this.mDefaultState);
                                return HwSmartNetStateMachine.HANDLED;
                            }
                            HwSmartNetStateMachine.this.transitionTo(HwSmartNetStateMachine.this.mStudyState);
                            return HwSmartNetStateMachine.HANDLED;
                        default:
                            HwSmartNetStateMachine hwSmartNetStateMachine2 = HwSmartNetStateMachine.this;
                            hwSmartNetStateMachine2.rlogi("SamplingState.processMessage default:" + msg.what);
                            return false;
                    }
            }
        }
    }

    /* access modifiers changed from: private */
    public class StudyState extends StateEx {
        private StudyState() {
        }

        public void enter() {
            HwSmartNetStateMachine.this.rlogi("StudyState enter");
            HwSmartNetStateMachine.this.mStudyStrategy.startAnalyseSampleInfo();
        }

        public void exit() {
            HwSmartNetStateMachine.this.rlogi("StudyState exit");
        }

        public boolean processMessage(Message msg) {
            HwSmartNetStateMachine hwSmartNetStateMachine = HwSmartNetStateMachine.this;
            hwSmartNetStateMachine.rlogi("StudyState Received Msg " + msg.what);
            if (msg.what != 402) {
                HwSmartNetStateMachine hwSmartNetStateMachine2 = HwSmartNetStateMachine.this;
                hwSmartNetStateMachine2.rlogi("StudyState.processMessage default:" + msg.what);
                return false;
            }
            HwSmartNetStateMachine.this.transitionTo(HwSmartNetStateMachine.this.mEstimateState);
            return HwSmartNetStateMachine.HANDLED;
        }
    }

    /* access modifiers changed from: private */
    public class EstimateState extends StateEx {
        private EstimateState() {
        }

        public void enter() {
            HwSmartNetStateMachine.this.rlogi("EstimateState enter");
            HwSmartNetStateMachine.this.mEstimateStrategy.endAndUpdateMatchInfo(HwSmartNetRouteStrategy.getInstance().getLastRouteId());
        }

        public void exit() {
            HwSmartNetStateMachine.this.rlogi("EstimateState exit");
        }

        public boolean processMessage(Message msg) {
            HwSmartNetStateMachine hwSmartNetStateMachine = HwSmartNetStateMachine.this;
            hwSmartNetStateMachine.rlogi("EstimateState Received Msg " + msg.what);
            int i = msg.what;
            HwSmartNetStateMachine hwSmartNetStateMachine2 = HwSmartNetStateMachine.this;
            hwSmartNetStateMachine2.rlogi("EstimateState.processMessage default:" + msg.what);
            return false;
        }
    }
}
