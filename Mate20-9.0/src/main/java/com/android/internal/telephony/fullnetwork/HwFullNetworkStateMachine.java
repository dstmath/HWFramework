package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class HwFullNetworkStateMachine extends StateMachine {
    private static final String LOG_TAG = "HwFullNetworkSM";
    /* access modifiers changed from: private */
    public static HwFullNetworkCheckStateBase mCheckStateBase;
    /* access modifiers changed from: private */
    public static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkDefaultStateBase mDefaultStateBase;
    private static HwFullNetworkInitStateBase mInitStateBase;
    private static HwFullNetworkStateMachine mInstance = null;
    private static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public static HwFullNetworkSetStateBase mSetStateBase;
    /* access modifiers changed from: private */
    public final DefaultState mDefaultState = new DefaultState();
    private final InitialState mInitialState = new InitialState();
    /* access modifiers changed from: private */
    public final MainSlotCheckState mMainSlotCheckState = new MainSlotCheckState();
    /* access modifiers changed from: private */
    public final MainSlotSetState mMainSlotSetState = new MainSlotSetState();

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            HwFullNetworkStateMachine.this.logd("entering DefaultState");
        }

        public void exit() {
            HwFullNetworkStateMachine.this.logd("leaving DefaultState");
        }

        public boolean processMessage(Message msg) {
            HwFullNetworkStateMachine hwFullNetworkStateMachine = HwFullNetworkStateMachine.this;
            hwFullNetworkStateMachine.logd("DefaultState Received Msg " + msg.what);
            switch (msg.what) {
                case HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT:
                case HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT_FOR_OPEATOR:
                case HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT_FOR_MDM:
                case HwFullNetworkConstants.EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC:
                    if (!HwFullNetworkStateMachine.this.getCurrentState().equals(HwFullNetworkStateMachine.this.mMainSlotSetState)) {
                        HwFullNetworkStateMachine.this.transitionTo(HwFullNetworkStateMachine.this.mMainSlotCheckState);
                        HwFullNetworkStateMachine.this.deferMessage(msg);
                    } else {
                        HwFullNetworkStateMachine hwFullNetworkStateMachine2 = HwFullNetworkStateMachine.this;
                        hwFullNetworkStateMachine2.logd("DefaultState deferred Msg " + msg.what);
                        HwFullNetworkStateMachine.this.deferMessage(msg);
                    }
                    return true;
                case HwFullNetworkConstants.EVENT_SET_MAIN_SLOT:
                    HwFullNetworkStateMachine.this.logd("DefaultState Received EVENT_SET_MAIN_SLOT.");
                    HwFullNetworkStateMachine.this.transitionTo(HwFullNetworkStateMachine.this.mMainSlotSetState);
                    HwFullNetworkStateMachine.this.deferMessage(msg);
                    return true;
                case HwFullNetworkConstants.EVENT_CHECK_NETWORK_TYPE:
                    HwFullNetworkStateMachine.this.logd("DefaultState Received EVENT_CHECK_NETWORK_TYPE.");
                    if (!HwFullNetworkStateMachine.this.getCurrentState().equals(HwFullNetworkStateMachine.this.mMainSlotSetState)) {
                        HwFullNetworkStateMachine.this.transitionTo(HwFullNetworkStateMachine.this.mMainSlotCheckState);
                        HwFullNetworkStateMachine.this.deferMessage(msg);
                    } else {
                        HwFullNetworkStateMachine hwFullNetworkStateMachine3 = HwFullNetworkStateMachine.this;
                        hwFullNetworkStateMachine3.logd("DefaultState deferred Msg " + msg.what);
                        HwFullNetworkStateMachine.this.deferMessage(msg);
                    }
                    return true;
                default:
                    HwFullNetworkStateMachine hwFullNetworkStateMachine4 = HwFullNetworkStateMachine.this;
                    hwFullNetworkStateMachine4.logd("DefaultState.processMessage default:" + msg.what);
                    return true;
            }
        }
    }

    private class InitialState extends State {
        private InitialState() {
        }

        public void enter() {
            HwFullNetworkStateMachine.this.logd("entering InitialState");
        }

        public void exit() {
            HwFullNetworkStateMachine.this.logd("leaving InitialState");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            HwFullNetworkStateMachine hwFullNetworkStateMachine = HwFullNetworkStateMachine.this;
            hwFullNetworkStateMachine.logd("InitialState.processMessage default:" + msg.what);
            return false;
        }
    }

    private class MainSlotCheckState extends State {
        private MainSlotCheckState() {
        }

        public void enter() {
            HwFullNetworkStateMachine.this.logd("entering MainSlotCheckState");
        }

        public void exit() {
            HwFullNetworkStateMachine.this.logd("leaving MainSlotCheckState");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 201) {
                HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_CHECK_MAIN_SLOT.");
                if (HwFullNetworkStateMachine.mCheckStateBase.checkIfAllCardsReady(msg)) {
                    HwFullNetworkStateMachine.this.sendMessage(HwFullNetworkConstants.EVENT_GET_MAIN_SLOT);
                }
                return true;
            } else if (i == 207) {
                HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC.");
                if (HwFullNetworkStateMachine.mCheckStateBase.judgeSetDefault4GSlotForCMCC(((Integer) msg.obj).intValue())) {
                    HwFullNetworkStateMachine.this.sendMessage(HwFullNetworkConstants.EVENT_GET_MAIN_SLOT);
                }
                return true;
            } else if (i != 301) {
                switch (i) {
                    case HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT_FOR_OPEATOR:
                        HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_CHECK_MAIN_SLOT_FOR_OPEATOR.");
                        return true;
                    case HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT_FOR_MDM:
                        HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_CHECK_MAIN_SLOT_FOR_MDM.");
                        int preDefault4GSlot = HwFullNetworkStateMachine.mChipCommon.getUserSwitchDualCardSlots();
                        if (HwFullNetworkStateMachine.mCheckStateBase.judgeDefaultMainSlotForMDM()) {
                            int mdmSlotId = HwFullNetworkStateMachine.mCheckStateBase.getDefaultMainSlot();
                            if (preDefault4GSlot != mdmSlotId && !HwFullNetworkStateMachine.mChipCommon.getWaitingSwitchBalongSlot()) {
                                HwFullNetworkStateMachine hwFullNetworkStateMachine = HwFullNetworkStateMachine.this;
                                hwFullNetworkStateMachine.logd("MainSlotCheckState slotId:" + mdmSlotId);
                                HwFullNetworkStateMachine.this.sendMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT, mdmSlotId, 0);
                            }
                        }
                        return true;
                    case HwFullNetworkConstants.EVENT_CHECK_NETWORK_TYPE:
                        HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_CHECK_NETWORK_TYPE.");
                        HwFullNetworkStateMachine.mCheckStateBase.checkNetworkType();
                        return true;
                    default:
                        HwFullNetworkStateMachine hwFullNetworkStateMachine2 = HwFullNetworkStateMachine.this;
                        hwFullNetworkStateMachine2.logd("MainSlotCheckState.processMessage default:" + msg.what);
                        return false;
                }
            } else {
                HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_GET_MAIN_SLOT.");
                HwFullNetworkStateMachine.mChipCommon.default4GSlot = HwFullNetworkStateMachine.mCheckStateBase.getDefaultMainSlot();
                HwFullNetworkStateMachine hwFullNetworkStateMachine3 = HwFullNetworkStateMachine.this;
                hwFullNetworkStateMachine3.logd("MainSlotCheckState slotId:" + HwFullNetworkStateMachine.mChipCommon.default4GSlot);
                HwFullNetworkStateMachine.this.sendMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT, HwFullNetworkStateMachine.mChipCommon.default4GSlot, 0);
                return true;
            }
        }
    }

    private class MainSlotSetState extends State {
        private MainSlotSetState() {
        }

        public void enter() {
            HwFullNetworkStateMachine.this.logd("entering MainSlotSetState");
        }

        public void exit() {
            HwFullNetworkStateMachine.this.logd("leaving MainSlotSetState");
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: android.os.Message} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 202) {
                HwFullNetworkStateMachine.this.logd("MainSlotSetState Received EVENT_SET_MAIN_SLOT.");
                int slotId = msg.arg1;
                Message response = null;
                if (msg.obj != null) {
                    response = msg.obj;
                }
                HwFullNetworkStateMachine.mSetStateBase.setMainSlot(slotId, response);
                return true;
            } else if (i != 403) {
                HwFullNetworkStateMachine hwFullNetworkStateMachine = HwFullNetworkStateMachine.this;
                hwFullNetworkStateMachine.logd("MainSlotSetState.processMessage default:" + msg.what);
                return false;
            } else {
                HwFullNetworkStateMachine.this.transitionTo(HwFullNetworkStateMachine.this.mDefaultState);
                return true;
            }
        }
    }

    private HwFullNetworkStateMachine(Context c, CommandsInterface[] ci) {
        super(LOG_TAG, Looper.getMainLooper());
        mChipCommon = HwFullNetworkChipCommon.getInstance();
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mMainSlotCheckState, this.mDefaultState);
        addState(this.mMainSlotSetState, this.mDefaultState);
        setInitialState(this.mInitialState);
        log("HwFullNetworkStateMachine construct finish!");
    }

    static HwFullNetworkStateMachine make(Context c, CommandsInterface[] ci) {
        HwFullNetworkStateMachine hwFullNetworkStateMachine;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new HwFullNetworkStateMachine(c, ci);
                mInstance.start();
                mDefaultStateBase = HwFullNetworkDefaultStateFactory.getHwFullNetworkDefaultState(c, ci, mInstance.getHandler());
                mInitStateBase = HwFullNetworkInitStateFactory.getHwFullNetworkInitState(c, ci, mInstance.getHandler());
                mCheckStateBase = HwFullNetworkCheckStateFactory.getHwFullNetworkCheckState(c, ci, mInstance.getHandler());
                mSetStateBase = HwFullNetworkSetStateFactory.getHwFullNetworkSetState(c, ci, mInstance.getHandler());
                hwFullNetworkStateMachine = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkStateMachine.make() should only be called once");
            }
        }
        return hwFullNetworkStateMachine;
    }

    /* access modifiers changed from: package-private */
    public HwFullNetworkDefaultStateBase getDefaultStateBase() {
        return mDefaultStateBase;
    }

    /* access modifiers changed from: package-private */
    public HwFullNetworkInitStateBase getInitStateBase() {
        return mInitStateBase;
    }

    public void dispose() {
        quit();
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(getName(), s);
    }
}
