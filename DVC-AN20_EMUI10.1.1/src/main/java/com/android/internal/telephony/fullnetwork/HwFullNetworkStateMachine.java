package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.util.StateEx;
import com.huawei.internal.util.StateMachineEx;

public class HwFullNetworkStateMachine extends StateMachineEx {
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwFullNetworkSM";
    private static HwFullNetworkCheckStateBase mCheckStateBase;
    private static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkDefaultStateBase mDefaultStateBase;
    private static HwFullNetworkInitStateBase mInitStateBase;
    private static HwFullNetworkStateMachine mInstance = null;
    private static HwFullNetworkSetStateBase mSetStateBase;
    private final DefaultState mDefaultState = new DefaultState();
    private final InitialState mInitialState = new InitialState();
    private final MainSlotCheckState mMainSlotCheckState = new MainSlotCheckState();
    private final MainSlotSetState mMainSlotSetState = new MainSlotSetState();

    private HwFullNetworkStateMachine(Context c, CommandsInterfaceEx[] ci) {
        super(LOG_TAG, Looper.getMainLooper());
        mChipCommon = HwFullNetworkChipCommon.getInstance();
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mMainSlotCheckState, this.mDefaultState);
        addState(this.mMainSlotSetState, this.mDefaultState);
        setInitialState(this.mInitialState);
        RlogEx.i(LOG_TAG, "HwFullNetworkStateMachine construct finish!");
    }

    static HwFullNetworkStateMachine make(Context c, CommandsInterfaceEx[] ci) {
        HwFullNetworkStateMachine hwFullNetworkStateMachine;
        synchronized (LOCK) {
            if (mInstance != null) {
                throw new RuntimeException("HwFullNetworkStateMachine.make() should only be called once");
            }
            mInstance = new HwFullNetworkStateMachine(c, ci);
            mInstance.start();
            mDefaultStateBase = HwFullNetworkDefaultStateFactory.getHwFullNetworkDefaultState(c, ci, mInstance.getHandler());
            mInitStateBase = HwFullNetworkInitStateFactory.getHwFullNetworkInitState(c, ci, mInstance.getHandler());
            mCheckStateBase = HwFullNetworkCheckStateFactory.getHwFullNetworkCheckState(c, ci, mInstance.getHandler());
            mSetStateBase = HwFullNetworkSetStateFactory.getHwFullNetworkSetState(c, ci, mInstance.getHandler());
            hwFullNetworkStateMachine = mInstance;
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
        RlogEx.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        RlogEx.e(getName(), s);
    }

    /* access modifiers changed from: private */
    public class DefaultState extends StateEx {
        private DefaultState() {
        }

        public void enter() {
            HwFullNetworkStateMachine.this.logd("entering DefaultState");
        }

        public void exit() {
            HwFullNetworkStateMachine.this.logd("leaving DefaultState");
        }

        public boolean processMessage(Message msg) {
            HwFullNetworkStateMachine.this.logd("DefaultState Received Msg " + msg.what);
            switch (msg.what) {
                case HwFullNetworkConstantsInner.EVENT_CHECK_MAIN_SLOT:
                case HwFullNetworkConstantsInner.EVENT_CHECK_MAIN_SLOT_FOR_OPEATOR:
                case HwFullNetworkConstantsInner.EVENT_CHECK_MAIN_SLOT_FOR_MDM:
                case HwFullNetworkConstantsInner.EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC:
                    if (HwFullNetworkStateMachine.this.isInSpecificState(HwFullNetworkStateMachine.this.mMainSlotSetState)) {
                        HwFullNetworkStateMachine.this.logd("DefaultState deferred Msg " + msg.what);
                        HwFullNetworkStateMachine.this.deferMessage(msg);
                        break;
                    } else {
                        HwFullNetworkStateMachine.this.transitionTo(HwFullNetworkStateMachine.this.mMainSlotCheckState);
                        HwFullNetworkStateMachine.this.deferMessage(msg);
                        break;
                    }
                case HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT:
                    HwFullNetworkStateMachine.this.logd("DefaultState Received EVENT_SET_MAIN_SLOT.");
                    HwFullNetworkStateMachine.this.transitionTo(HwFullNetworkStateMachine.this.mMainSlotSetState);
                    HwFullNetworkStateMachine.this.deferMessage(msg);
                    break;
                case HwFullNetworkConstantsInner.EVENT_CHECK_NETWORK_TYPE:
                    HwFullNetworkStateMachine.this.logd("DefaultState Received EVENT_CHECK_NETWORK_TYPE.");
                    if (HwFullNetworkStateMachine.this.isInSpecificState(HwFullNetworkStateMachine.this.mMainSlotSetState)) {
                        HwFullNetworkStateMachine.this.logd("DefaultState deferred Msg " + msg.what);
                        HwFullNetworkStateMachine.this.deferMessage(msg);
                        break;
                    } else {
                        HwFullNetworkStateMachine.this.transitionTo(HwFullNetworkStateMachine.this.mMainSlotCheckState);
                        HwFullNetworkStateMachine.this.deferMessage(msg);
                        break;
                    }
                case HwFullNetworkConstantsInner.EVENT_SET_NETWORK_TYPE:
                default:
                    HwFullNetworkStateMachine.this.logd("DefaultState.processMessage default:" + msg.what);
                    break;
            }
            return true;
        }
    }

    private class InitialState extends StateEx {
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
            HwFullNetworkStateMachine.this.logd("InitialState.processMessage default:" + msg.what);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class MainSlotCheckState extends StateEx {
        private MainSlotCheckState() {
        }

        public void enter() {
            HwFullNetworkStateMachine.this.logd("entering MainSlotCheckState");
        }

        public void exit() {
            HwFullNetworkStateMachine.this.logd("leaving MainSlotCheckState");
        }

        public boolean processMessage(Message msg) {
            int mdmSlotId;
            switch (msg.what) {
                case HwFullNetworkConstantsInner.EVENT_CHECK_MAIN_SLOT:
                    HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_CHECK_MAIN_SLOT.");
                    if (HwFullNetworkStateMachine.mCheckStateBase.checkIfAllCardsReady(msg)) {
                        HwFullNetworkStateMachine.this.sendMessage(HwFullNetworkConstantsInner.EVENT_GET_MAIN_SLOT);
                    }
                    return true;
                case HwFullNetworkConstantsInner.EVENT_CHECK_MAIN_SLOT_FOR_OPEATOR:
                    HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_CHECK_MAIN_SLOT_FOR_OPEATOR.");
                    return true;
                case HwFullNetworkConstantsInner.EVENT_CHECK_MAIN_SLOT_FOR_MDM:
                    HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_CHECK_MAIN_SLOT_FOR_MDM.");
                    int preDefault4GSlot = HwFullNetworkStateMachine.mChipCommon.getUserSwitchDualCardSlots();
                    if (HwFullNetworkStateMachine.mCheckStateBase.judgeDefaultMainSlotForMDM() && preDefault4GSlot != (mdmSlotId = HwFullNetworkStateMachine.mCheckStateBase.getDefaultMainSlot()) && !HwFullNetworkStateMachine.mChipCommon.getWaitingSwitchBalongSlot()) {
                        HwFullNetworkStateMachine.this.logd("MainSlotCheckState slotId:" + mdmSlotId);
                        HwFullNetworkStateMachine.this.sendMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT, mdmSlotId, 0);
                    }
                    return true;
                case HwFullNetworkConstantsInner.EVENT_CHECK_NETWORK_TYPE:
                    HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_CHECK_NETWORK_TYPE.");
                    HwFullNetworkStateMachine.mCheckStateBase.checkNetworkType();
                    return true;
                case HwFullNetworkConstantsInner.EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC:
                    HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_FORCE_CHECK_MAIN_SLOT_FOR_CMCC.");
                    if (HwFullNetworkStateMachine.mCheckStateBase.judgeSetDefault4GSlotForCMCC(((Integer) msg.obj).intValue())) {
                        HwFullNetworkStateMachine.this.sendMessage(HwFullNetworkConstantsInner.EVENT_GET_MAIN_SLOT);
                    }
                    return true;
                case HwFullNetworkConstantsInner.EVENT_GET_MAIN_SLOT:
                    HwFullNetworkStateMachine.this.logd("MainSlotCheckState Received EVENT_GET_MAIN_SLOT.");
                    HwFullNetworkStateMachine.mChipCommon.default4GSlot = HwFullNetworkStateMachine.mCheckStateBase.getDefaultMainSlot();
                    HwFullNetworkStateMachine.this.logd("MainSlotCheckState slotId:" + HwFullNetworkStateMachine.mChipCommon.default4GSlot);
                    HwFullNetworkStateMachine.this.sendMessage(HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT, HwFullNetworkStateMachine.mChipCommon.default4GSlot, 0);
                    return true;
                default:
                    HwFullNetworkStateMachine.this.logd("MainSlotCheckState.processMessage default:" + msg.what);
                    return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class MainSlotSetState extends StateEx {
        private MainSlotSetState() {
        }

        public void enter() {
            HwFullNetworkStateMachine.this.logd("entering MainSlotSetState");
        }

        public void exit() {
            HwFullNetworkStateMachine.this.logd("leaving MainSlotSetState");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HwFullNetworkConstantsInner.EVENT_SET_MAIN_SLOT:
                    HwFullNetworkStateMachine.this.logd("MainSlotSetState Received EVENT_SET_MAIN_SLOT.");
                    int slotId = msg.arg1;
                    Message response = null;
                    if (msg.obj != null) {
                        response = (Message) msg.obj;
                    }
                    HwFullNetworkStateMachine.this.logd("MainSlotSetState Received EVENT_SET_MAIN_SLOT. slotId: " + slotId);
                    HwFullNetworkStateMachine.mSetStateBase.setMainSlot(slotId, response);
                    return true;
                case HwFullNetworkConstantsInner.EVENT_TRANS_TO_DEFAULT:
                    HwFullNetworkStateMachine.this.transitionTo(HwFullNetworkStateMachine.this.mDefaultState);
                    return true;
                default:
                    HwFullNetworkStateMachine.this.logd("MainSlotSetState.processMessage default:" + msg.what);
                    return false;
            }
        }
    }
}
