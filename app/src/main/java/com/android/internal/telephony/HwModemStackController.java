package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.RIL.UnsolOemHookBuffer;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccController;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class HwModemStackController extends Handler {
    private static final int BIND_TO_STACK = 1;
    private static final int CMD_DEACTIVATE_ALL_SUBS = 1;
    private static final int CMD_TRIGGER_BIND = 5;
    private static final int CMD_TRIGGER_UNBIND = 3;
    private static final int DEFAULT_MAX_DATA_ALLOWED = 1;
    private static final int EVENT_BIND_DONE = 6;
    private static final int EVENT_GET_MODEM_CAPS_DONE = 2;
    private static final int EVENT_MODEM_CAPABILITY_CHANGED = 10;
    private static final int EVENT_RADIO_AVAILABLE = 9;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 11;
    private static final int EVENT_SET_PREF_MODE_DONE = 7;
    private static final int EVENT_SUB_DEACTIVATED = 8;
    private static final int EVENT_UNBIND_DONE = 4;
    private static final int FAILURE = 0;
    private static final int GET_MODEM_CAPS_BUFFER_LEN = 7;
    static final String LOG_TAG = "HwModemStackController";
    private static final int MODEM_DUAL_DATA_ALLOWED = 2;
    private static final int PRIMARY_STACK_ID = 0;
    private static final int STATE_BIND = 5;
    private static final int STATE_GOT_MODEM_CAPS = 2;
    private static final int STATE_SET_PREF_MODE = 7;
    private static final int STATE_SUB_ACT = 6;
    private static final int STATE_SUB_DEACT = 3;
    private static final int STATE_UNBIND = 4;
    private static final int STATE_UNKNOWN = 1;
    private static final int SUCCESS = 1;
    private static final int UNBIND_TO_STACK = 0;
    private static final Object mLock = null;
    private static HwModemStackController sHwModemStackController;
    private int mActiveSubCount;
    private CommandsInterface[] mCi;
    private boolean[] mCmdFailed;
    private Context mContext;
    private int[] mCurrentStackId;
    private boolean mDeactivationInProgress;
    private int mDeactivedSubCount;
    private boolean mIsPhoneInEcbmMode;
    private boolean mIsRecoveryInProgress;
    private boolean mIsStackReady;
    private ModemCapabilityInfo[] mModemCapInfo;
    private RegistrantList mModemDataCapsAvailableRegistrants;
    private boolean mModemRatCapabilitiesAvailable;
    private RegistrantList mModemRatCapsAvailableRegistrants;
    private int mNumPhones;
    private int[] mPrefNwMode;
    private int[] mPreferredStackId;
    private BroadcastReceiver mReceiver;
    private RegistrantList mStackReadyRegistrants;
    private int[] mSubState;
    private HashMap<Integer, Integer> mSubcriptionStatus;
    private Message mUpdateStackMsg;

    public static class ModemCapabilityInfo {
        private int mMaxDataCap;
        private int mStackId;
        private int mSupportedRatBitMask;
        private int mVoiceDataCap;

        public ModemCapabilityInfo(int stackId, int supportedRatBitMask, int voiceCap, int dataCap) {
            this.mStackId = stackId;
            this.mSupportedRatBitMask = supportedRatBitMask;
            this.mVoiceDataCap = voiceCap;
            this.mMaxDataCap = dataCap;
        }

        public int getSupportedRatBitMask() {
            return this.mSupportedRatBitMask;
        }

        public int getStackId() {
            return this.mStackId;
        }

        public int getMaxDataCap() {
            return this.mMaxDataCap;
        }

        public String toString() {
            return "[stack = " + this.mStackId + ", SuppRatBitMask = " + this.mSupportedRatBitMask + ", voiceDataCap = " + this.mVoiceDataCap + ", maxDataCap = " + this.mMaxDataCap + "]";
        }
    }

    public enum SubscriptionStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwModemStackController.SubscriptionStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwModemStackController.SubscriptionStatus.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwModemStackController.SubscriptionStatus.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwModemStackController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwModemStackController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwModemStackController.<clinit>():void");
    }

    public static HwModemStackController make(Context context, UiccController uiccMgr, CommandsInterface[] ci) {
        HwModemStackController hwModemStackController;
        Rlog.d(LOG_TAG, "getInstance");
        synchronized (mLock) {
            if (sHwModemStackController == null) {
                sHwModemStackController = new HwModemStackController(context, uiccMgr, ci);
                hwModemStackController = sHwModemStackController;
            } else {
                throw new RuntimeException("HwModemStackController.make() should only be called once");
            }
        }
        return hwModemStackController;
    }

    public static HwModemStackController getInstance() {
        HwModemStackController hwModemStackController;
        synchronized (mLock) {
            if (sHwModemStackController == null) {
                throw new RuntimeException("HwModemStackController.getInstance called before make()");
            }
            hwModemStackController = sHwModemStackController;
        }
        return hwModemStackController;
    }

    private HwModemStackController(Context context, UiccController uiccManager, CommandsInterface[] ci) {
        int i;
        this.mNumPhones = TelephonyManager.getDefault().getPhoneCount();
        this.mActiveSubCount = PRIMARY_STACK_ID;
        this.mDeactivedSubCount = PRIMARY_STACK_ID;
        this.mPreferredStackId = new int[this.mNumPhones];
        this.mCurrentStackId = new int[this.mNumPhones];
        this.mPrefNwMode = new int[this.mNumPhones];
        this.mSubState = new int[this.mNumPhones];
        this.mIsStackReady = false;
        this.mIsRecoveryInProgress = false;
        this.mIsPhoneInEcbmMode = false;
        this.mModemRatCapabilitiesAvailable = false;
        this.mDeactivationInProgress = false;
        this.mCmdFailed = new boolean[this.mNumPhones];
        this.mStackReadyRegistrants = new RegistrantList();
        this.mModemRatCapsAvailableRegistrants = new RegistrantList();
        this.mModemDataCapsAvailableRegistrants = new RegistrantList();
        this.mSubcriptionStatus = new HashMap();
        this.mModemCapInfo = null;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(intent.getAction())) {
                    if (intent.getBooleanExtra("phoneinECMState", false)) {
                        HwModemStackController.this.logd("Device is in ECBM Mode");
                        HwModemStackController.this.mIsPhoneInEcbmMode = true;
                        return;
                    }
                    HwModemStackController.this.logd("Device is out of ECBM Mode");
                    HwModemStackController.this.mIsPhoneInEcbmMode = false;
                } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(intent.getAction())) {
                    subId = intent.getIntExtra("_id", -1);
                    String column = intent.getStringExtra("columnName");
                    int intValue = intent.getIntExtra("intContent", HwModemStackController.PRIMARY_STACK_ID);
                    HwModemStackController.this.logd("Received ACTION_SUBINFO_CONTENT_CHANGE on subId: " + subId + "for " + column + " intValue: " + intValue);
                    if (HwModemStackController.this.mDeactivationInProgress && column != null && -1 != subId && column.equals("sub_state")) {
                        phoneId = SubscriptionController.getInstance().getPhoneId(subId);
                        if (intValue == 0 && ((Integer) HwModemStackController.this.mSubcriptionStatus.get(Integer.valueOf(phoneId))).intValue() == HwModemStackController.SUCCESS) {
                            msg = HwModemStackController.this.obtainMessage(HwModemStackController.EVENT_SUB_DEACTIVATED, Integer.valueOf(phoneId));
                            AsyncResult.forMessage(msg, SubscriptionStatus.SUB_DEACTIVATED, null);
                            HwModemStackController.this.sendMessage(msg);
                            HwModemStackController.this.mSubcriptionStatus.put(Integer.valueOf(phoneId), Integer.valueOf(HwModemStackController.PRIMARY_STACK_ID));
                        }
                    }
                } else if ("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                    subId = intent.getIntExtra("subscription", -1000);
                    phoneId = intent.getIntExtra("phone", HwModemStackController.PRIMARY_STACK_ID);
                    int status = intent.getIntExtra("operationResult", HwModemStackController.SUCCESS);
                    int newSubState = intent.getIntExtra("newSubState", HwModemStackController.SUCCESS);
                    HwModemStackController.this.logd("Received ACTION_SUBSCRIPTION_SET_UICC_RESULT on subId: " + subId + "phoneId " + phoneId + " status: " + status + " newSubState:" + newSubState);
                    if (HwModemStackController.this.mDeactivationInProgress && status == HwModemStackController.SUCCESS && newSubState == 0) {
                        msg = HwModemStackController.this.obtainMessage(HwModemStackController.EVENT_SUB_DEACTIVATED, Integer.valueOf(phoneId));
                        AsyncResult.forMessage(msg, SubscriptionStatus.SUB_ACTIVATED, null);
                        HwModemStackController.this.sendMessage(msg);
                    }
                    int subStatus = SubscriptionController.getInstance().getSubState(subId);
                    if (HwModemStackController.this.mDeactivationInProgress && status == 0 && -1 != subId && subStatus == 0 && ((Integer) HwModemStackController.this.mSubcriptionStatus.get(Integer.valueOf(phoneId))).intValue() == HwModemStackController.SUCCESS) {
                        msg = HwModemStackController.this.obtainMessage(HwModemStackController.EVENT_SUB_DEACTIVATED, Integer.valueOf(phoneId));
                        AsyncResult.forMessage(msg, SubscriptionStatus.SUB_DEACTIVATED, null);
                        HwModemStackController.this.sendMessage(msg);
                        HwModemStackController.this.mSubcriptionStatus.put(Integer.valueOf(phoneId), Integer.valueOf(HwModemStackController.PRIMARY_STACK_ID));
                        HwModemStackController.this.logd("maybe no ACTION_SUBINFO_CONTENT_CHANGE for sub_state , when current sub state is INACTIVE, send success!");
                    }
                }
            }
        };
        logd("Constructor - Enter");
        this.mCi = ci;
        this.mContext = context;
        this.mModemCapInfo = new ModemCapabilityInfo[this.mNumPhones];
        for (i = PRIMARY_STACK_ID; i < this.mCi.length; i += SUCCESS) {
            this.mCi[i].registerForAvailable(this, EVENT_RADIO_AVAILABLE, Integer.valueOf(i));
            this.mCi[i].registerForModemCapEvent(this, EVENT_MODEM_CAPABILITY_CHANGED, null);
            this.mCi[i].registerForNotAvailable(this, EVENT_RADIO_NOT_AVAILABLE, Integer.valueOf(i));
        }
        for (i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            this.mPreferredStackId[i] = i;
            this.mCurrentStackId[i] = i;
            this.mSubState[i] = SUCCESS;
            this.mCmdFailed[i] = false;
            this.mSubcriptionStatus.put(Integer.valueOf(i), Integer.valueOf(-1));
        }
        if (this.mNumPhones == SUCCESS) {
            this.mIsStackReady = true;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        filter.addAction("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        this.mContext.registerReceiver(this.mReceiver, filter);
        logd("Constructor - Exit");
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        Integer phoneId;
        switch (msg.what) {
            case SUCCESS /*1*/:
                logd("CMD_DEACTIVATE_ALL_SUBS");
                deactivateAllSubscriptions();
            case STATE_GOT_MODEM_CAPS /*2*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logd("EVENT_GET_MODEM_CAPS_DONE");
                onGetModemCapabilityDone(ar, (byte[]) ar.result, phoneId.intValue());
            case STATE_SUB_DEACT /*3*/:
                phoneId = (Integer) msg.obj;
                logd("CMD_TRIGGER_UNBIND");
                unbindStackOnSub(phoneId.intValue());
            case STATE_UNBIND /*4*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logd("EVENT_UNBIND_DONE");
                onUnbindComplete(ar, phoneId.intValue());
            case STATE_BIND /*5*/:
                phoneId = (Integer) msg.obj;
                logd("CMD_TRIGGER_BIND");
                bindStackOnSub(phoneId.intValue());
            case STATE_SUB_ACT /*6*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logd("EVENT_BIND_DONE");
                onBindComplete(ar, phoneId.intValue());
            case STATE_SET_PREF_MODE /*7*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logd("EVENT_SET_PREF_MODE_DONE");
                onSetPrefNwModeDone(ar, phoneId.intValue());
            case EVENT_SUB_DEACTIVATED /*8*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logd("EVENT_SUB_DEACTIVATED");
                onSubDeactivated(ar, phoneId.intValue());
            case EVENT_RADIO_AVAILABLE /*9*/:
                ar = msg.obj;
                phoneId = ar.userObj;
                logd("EVENT_RADIO_AVAILABLE");
                processRadioAvailable(ar, phoneId.intValue());
            case EVENT_MODEM_CAPABILITY_CHANGED /*10*/:
                ar = (AsyncResult) msg.obj;
                logd("EVENT_MODEM_CAPABILITY_CHANGED ar =" + ar);
                onUnsolModemCapabilityChanged(ar);
            case EVENT_RADIO_NOT_AVAILABLE /*11*/:
                ar = (AsyncResult) msg.obj;
                phoneId = (Integer) ar.userObj;
                logd("EVENT_RADIO_NOT_AVAILABLE, phoneId = " + phoneId);
                processRadioNotAvailable(ar, phoneId.intValue());
            default:
        }
    }

    private void processRadioAvailable(AsyncResult ar, int phoneId) {
        logd("processRadioAvailable on phoneId = " + phoneId);
        if (phoneId < 0 || phoneId >= this.mNumPhones) {
            loge("Invalid Index!!!");
            return;
        }
        this.mCi[phoneId].getModemCapability(Message.obtain(this, STATE_GOT_MODEM_CAPS, Integer.valueOf(phoneId)));
    }

    private void processRadioNotAvailable(AsyncResult ar, int phoneId) {
        logd("processRadioNotAvailable on phoneId = " + phoneId);
        if (phoneId < 0 || phoneId >= this.mNumPhones) {
            loge("Invalid Index!!!");
        } else {
            this.mModemCapInfo[this.mCurrentStackId[phoneId]] = null;
        }
    }

    private void onGetModemCapabilityDone(AsyncResult ar, byte[] result, int phoneId) {
        if (result == null || (ar != null && (ar.exception instanceof CommandException))) {
            loge("onGetModemCapabilityDone: EXIT!, result null or Exception");
            notifyStackReady(false);
            return;
        }
        logd("onGetModemCapabilityDone on phoneId[" + phoneId + "] result = " + Arrays.toString(result));
        if (phoneId < 0 || phoneId >= this.mNumPhones) {
            loge("Invalid Index!!!");
        } else {
            this.mSubState[phoneId] = STATE_GOT_MODEM_CAPS;
            parseGetModemCapabilityResponse(result, phoneId);
            if (areAllModemCapInfoReceived()) {
                notifyModemRatCapabilitiesAvailable();
            }
        }
    }

    private void onUnsolModemCapabilityChanged(AsyncResult ar) {
        logd("onUnsolModemCapabilityChanged");
        UnsolOemHookBuffer unsolOemHookBuffer = ar.result;
        if (unsolOemHookBuffer == null && (ar.exception instanceof CommandException)) {
            loge("onUnsolModemCapabilityChanged: EXIT!, result null or Exception =" + ar.exception);
        } else if (unsolOemHookBuffer != null) {
            byte[] data = unsolOemHookBuffer.getUnsolOemHookBuffer();
            int phoneId = unsolOemHookBuffer.getRilInstance();
            logd("onUnsolModemCapabilityChanged on phoneId = " + phoneId);
            if (data == null) {
                loge("onUnsolModemCapabilityChanged: EXIT!, data is null");
                return;
            }
            parseGetModemCapabilityResponse(data, phoneId);
            notifyModemDataCapabilitiesAvailable();
        }
    }

    private void onSubDeactivated(AsyncResult ar, int phoneId) {
        SubscriptionStatus subStatus = ar.result;
        if (subStatus == null || SubscriptionStatus.SUB_DEACTIVATED != subStatus) {
            loge("onSubDeactivated on phoneId[" + phoneId + "] Failed!!!");
            this.mCmdFailed[phoneId] = true;
        }
        logd("onSubDeactivated on phoneId[" + phoneId + "] subStatus = " + subStatus);
        if (this.mSubState[phoneId] != STATE_SUB_DEACT) {
            this.mSubState[phoneId] = STATE_SUB_DEACT;
            this.mDeactivedSubCount += SUCCESS;
            if (this.mDeactivedSubCount == this.mActiveSubCount) {
                if (isAnyCmdFailed()) {
                    if (this.mUpdateStackMsg != null) {
                        sendResponseToTarget(this.mUpdateStackMsg, STATE_GOT_MODEM_CAPS);
                        this.mUpdateStackMsg = null;
                    }
                    notifyStackReady(false);
                    this.mDeactivationInProgress = false;
                } else {
                    this.mDeactivationInProgress = false;
                    triggerUnBindingOnAllSubs();
                }
            }
        }
    }

    private void bindStackOnSub(int phoneId) {
        logd("bindStack " + this.mPreferredStackId[phoneId] + " On phoneId[" + phoneId + "]");
        this.mCi[phoneId].updateStackBinding(this.mPreferredStackId[phoneId], SUCCESS, Message.obtain(this, STATE_SUB_ACT, Integer.valueOf(phoneId)));
    }

    private void unbindStackOnSub(int phoneId) {
        logd("unbindStack " + this.mCurrentStackId[phoneId] + " On phoneId[" + phoneId + "]");
        this.mCi[phoneId].updateStackBinding(this.mCurrentStackId[phoneId], PRIMARY_STACK_ID, Message.obtain(this, STATE_UNBIND, Integer.valueOf(phoneId)));
    }

    private void onUnbindComplete(AsyncResult ar, int phoneId) {
        if (ar.exception instanceof CommandException) {
            this.mCmdFailed[phoneId] = true;
            loge("onUnbindComplete(" + phoneId + "): got Exception =" + ar.exception);
        }
        this.mSubState[phoneId] = STATE_UNBIND;
        if (areAllSubsinSameState(STATE_UNBIND)) {
            if (isAnyCmdFailed()) {
                recoverToPrevState();
                return;
            }
            triggerBindingOnAllSubs();
        }
    }

    private void onBindComplete(AsyncResult ar, int phoneId) {
        if (ar.exception instanceof CommandException) {
            this.mCmdFailed[phoneId] = true;
            loge("onBindComplete(" + phoneId + "): got Exception =" + ar.exception);
        }
        this.mSubState[phoneId] = STATE_BIND;
        if (areAllSubsinSameState(STATE_BIND)) {
            if (isAnyCmdFailed()) {
                recoverToPrevState();
                return;
            }
            setPrefNwTypeOnAllSubs();
        }
    }

    private void onSetPrefNwModeDone(AsyncResult ar, int phoneId) {
        if (ar.exception instanceof CommandException) {
            this.mCmdFailed[phoneId] = true;
            loge("onSetPrefNwModeDone(SUB:" + phoneId + "): got Exception =" + ar.exception);
        }
        this.mSubState[phoneId] = STATE_SET_PREF_MODE;
        if (areAllSubsinSameState(STATE_SET_PREF_MODE)) {
            if (isAnyCmdFailed()) {
                recoverToPrevState();
                return;
            }
            if (this.mUpdateStackMsg != null) {
                sendResponseToTarget(this.mUpdateStackMsg, PRIMARY_STACK_ID);
                this.mUpdateStackMsg = null;
            }
            updateNetworkSelectionMode();
            notifyStackReady(true);
        }
    }

    private void updateNetworkSelectionMode() {
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            this.mCi[i].setNetworkSelectionModeAutomatic(null);
        }
    }

    private void triggerUnBindingOnAllSubs() {
        resetSubStates();
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            sendMessage(obtainMessage(STATE_SUB_DEACT, Integer.valueOf(i)));
        }
    }

    private void triggerBindingOnAllSubs() {
        resetSubStates();
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            sendMessage(obtainMessage(STATE_BIND, Integer.valueOf(i)));
        }
    }

    private void triggerDeactivationOnAllSubs() {
        resetSubStates();
        sendMessage(obtainMessage(SUCCESS));
    }

    private void setPrefNwTypeOnAllSubs() {
        resetSubStates();
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            this.mCi[i].setPreferredNetworkType(this.mPrefNwMode[i], obtainMessage(STATE_SET_PREF_MODE, Integer.valueOf(i)));
        }
    }

    private boolean areAllSubsinSameState(int state) {
        int[] iArr = this.mSubState;
        int length = iArr.length;
        for (int i = PRIMARY_STACK_ID; i < length; i += SUCCESS) {
            int subState = iArr[i];
            logd("areAllSubsinSameState state= " + state + " substate=" + subState);
            if (subState != state) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllModemCapInfoReceived() {
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            if (this.mModemCapInfo[this.mCurrentStackId[i]] == null) {
                return false;
            }
        }
        return true;
    }

    private void resetSubStates() {
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            this.mSubState[i] = SUCCESS;
            this.mCmdFailed[i] = false;
        }
    }

    private boolean isAnyCmdFailed() {
        boolean result = false;
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            if (this.mCmdFailed[i]) {
                result = true;
            }
        }
        return result;
    }

    private void updateModemCapInfo(int phoneId, int stackId, int supportedRatBitMask, int voiceDataCap, int maxDataCap) {
        this.mCurrentStackId[phoneId] = stackId;
        this.mModemCapInfo[this.mCurrentStackId[phoneId]] = new ModemCapabilityInfo(this.mCurrentStackId[phoneId], supportedRatBitMask, voiceDataCap, maxDataCap);
        logd("updateModemCapInfo: ModemCaps[" + phoneId + "]" + this.mModemCapInfo[this.mCurrentStackId[phoneId]]);
    }

    private void parseGetModemCapabilityResponse(byte[] result, int phoneId) {
        if (result.length != STATE_SET_PREF_MODE) {
            loge("parseGetModemCapabilityResponse: EXIT!, result length(" + result.length + ") and Expected length(" + STATE_SET_PREF_MODE + ") not matching.");
            return;
        }
        logd("parseGetModemCapabilityResponse: buffer = " + IccUtils.bytesToHexString(result));
        ByteBuffer respBuffer = ByteBuffer.wrap(result);
        respBuffer.order(ByteOrder.nativeOrder());
        int stackId = respBuffer.get();
        if (stackId < 0 || stackId >= this.mNumPhones) {
            loge("Invalid Index!!!");
            return;
        }
        updateModemCapInfo(phoneId, stackId, respBuffer.getInt(), respBuffer.get(), respBuffer.get());
    }

    private void syncPreferredNwModeFromDB() {
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            try {
                this.mPrefNwMode[i] = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i);
            } catch (SettingNotFoundException e) {
                loge("getPreferredNetworkMode: Could not find PREFERRED_NETWORK_MODE!!!");
                this.mPrefNwMode[i] = Phone.PREFERRED_NT_MODE;
            }
        }
    }

    private boolean isAnyCallsInProgress() {
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            if (TelephonyManager.getDefault().getCallState(SubscriptionController.getInstance().getSubIdUsingPhoneId(i)) != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isStackReady() {
        if (HwModemCapability.isCapabilitySupport(EVENT_RADIO_AVAILABLE) && !HwAllInOneController.IS_QCRIL_CROSS_MAPPING) {
            return this.mIsStackReady;
        }
        return true;
    }

    public int getMaxDataAllowed() {
        logd("getMaxDataAllowed");
        int ret = SUCCESS;
        List<Integer> unsortedList = new ArrayList();
        if (HwModemCapability.isCapabilitySupport(PRIMARY_STACK_ID)) {
            return STATE_GOT_MODEM_CAPS;
        }
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            if (this.mModemCapInfo[i] != null) {
                unsortedList.add(Integer.valueOf(this.mModemCapInfo[i].getMaxDataCap()));
            }
        }
        Collections.sort(unsortedList);
        int listSize = unsortedList.size();
        if (listSize > 0) {
            ret = ((Integer) unsortedList.get(listSize - 1)).intValue();
        }
        return ret;
    }

    public int getCurrentStackIdForPhoneId(int phoneId) {
        return this.mCurrentStackId[phoneId];
    }

    public int getPrimarySub() {
        for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            if (getCurrentStackIdForPhoneId(i) == 0) {
                return i;
            }
        }
        return PRIMARY_STACK_ID;
    }

    public ModemCapabilityInfo getModemRatCapsForPhoneId(int phoneId) {
        return this.mModemCapInfo[this.mCurrentStackId[phoneId]];
    }

    public int updateStackBinding(int[] prefStackIds, boolean isBootUp, Message msg) {
        boolean isUpdateRequired = false;
        boolean callInProgress = isAnyCallsInProgress();
        if (this.mNumPhones == SUCCESS) {
            loge("No need to update Stack Binding in case of Single Sim.");
            return PRIMARY_STACK_ID;
        }
        boolean isFlexmapDisabled = SystemProperties.getInt("persist.radio.disable_flexmap", PRIMARY_STACK_ID) == SUCCESS;
        if (callInProgress || this.mIsPhoneInEcbmMode || !(this.mIsStackReady || isBootUp)) {
            loge("updateStackBinding: Calls is progress = " + callInProgress + ", mIsPhoneInEcbmMode = " + this.mIsPhoneInEcbmMode + ", mIsStackReady = " + this.mIsStackReady + ". So EXITING!!!");
            return PRIMARY_STACK_ID;
        }
        int i;
        for (i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            this.mPreferredStackId[i] = prefStackIds[i];
        }
        for (i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
            if (this.mPreferredStackId[i] != this.mCurrentStackId[i]) {
                isUpdateRequired = true;
                break;
            }
        }
        if (isFlexmapDisabled || !isUpdateRequired) {
            loge("updateStackBinding: FlexMap Disabled : " + isFlexmapDisabled);
            if (isBootUp) {
                notifyStackReady(false);
            }
            return PRIMARY_STACK_ID;
        }
        this.mIsStackReady = false;
        this.mUpdateStackMsg = msg;
        syncPreferredNwModeFromDB();
        if (isBootUp) {
            triggerUnBindingOnAllSubs();
        } else {
            triggerDeactivationOnAllSubs();
        }
        return SUCCESS;
    }

    private void deactivateAllSubscriptions() {
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        List<SubscriptionInfo> subInfoList = subCtrlr.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        this.mActiveSubCount = PRIMARY_STACK_ID;
        if (subInfoList == null) {
            loge("deactivateAllSubscriptions: subInfoList == null!!!, return failure");
            if (this.mUpdateStackMsg != null) {
                sendResponseToTarget(this.mUpdateStackMsg, STATE_GOT_MODEM_CAPS);
                this.mUpdateStackMsg = null;
            }
            notifyStackReady(false);
            return;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            int subStatus = subCtrlr.getSubState(subInfo.getSubscriptionId());
            if (subStatus == SUCCESS) {
                this.mActiveSubCount += SUCCESS;
                subCtrlr.deactivateSubId(subInfo.getSubscriptionId());
            }
            this.mSubcriptionStatus.put(Integer.valueOf(subInfo.getSimSlotIndex()), Integer.valueOf(subStatus));
        }
        if (this.mActiveSubCount > 0) {
            this.mDeactivedSubCount = PRIMARY_STACK_ID;
            this.mDeactivationInProgress = true;
        } else {
            this.mDeactivationInProgress = false;
            triggerUnBindingOnAllSubs();
        }
    }

    private void notifyStackReady(boolean isCrossMapDone) {
        logd("notifyStackReady: Stack is READY!!!");
        this.mIsRecoveryInProgress = false;
        this.mIsStackReady = true;
        resetSubStates();
        if (isCrossMapDone) {
            for (int i = PRIMARY_STACK_ID; i < this.mNumPhones; i += SUCCESS) {
                this.mCurrentStackId[i] = this.mPreferredStackId[i];
            }
        }
        this.mStackReadyRegistrants.notifyRegistrants();
    }

    public void registerForStackReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        if (this.mIsStackReady) {
            r.notifyRegistrant();
        }
        synchronized (this.mStackReadyRegistrants) {
            this.mStackReadyRegistrants.add(r);
        }
    }

    private void notifyModemRatCapabilitiesAvailable() {
        logd("notifyGetRatCapabilitiesDone: Got RAT capabilities for all Stacks!!!");
        this.mModemRatCapabilitiesAvailable = true;
        this.mModemRatCapsAvailableRegistrants.notifyRegistrants();
    }

    private void notifyModemDataCapabilitiesAvailable() {
        logd("notifyGetDataCapabilitiesDone");
        this.mModemDataCapsAvailableRegistrants.notifyRegistrants();
    }

    public void registerForModemRatCapsAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        if (this.mModemRatCapabilitiesAvailable) {
            r.notifyRegistrant();
        }
        synchronized (this.mModemRatCapsAvailableRegistrants) {
            this.mModemRatCapsAvailableRegistrants.add(r);
        }
    }

    public void registerForModemDataCapsUpdate(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mModemDataCapsAvailableRegistrants) {
            this.mModemDataCapsAvailableRegistrants.add(r);
        }
    }

    private void recoverToPrevState() {
        if (this.mIsRecoveryInProgress) {
            if (this.mUpdateStackMsg != null) {
                sendResponseToTarget(this.mUpdateStackMsg, STATE_GOT_MODEM_CAPS);
                this.mUpdateStackMsg = null;
            }
            this.mIsRecoveryInProgress = false;
            if (STATE_SET_PREF_MODE == this.mSubState[PRIMARY_STACK_ID]) {
                notifyStackReady(true);
            }
            return;
        }
        this.mIsRecoveryInProgress = true;
        triggerUnBindingOnAllSubs();
    }

    public void backToPrevState() {
        logd("backToPrevState!!!");
        if (this.mUpdateStackMsg != null) {
            sendResponseToTarget(this.mUpdateStackMsg, STATE_GOT_MODEM_CAPS);
            this.mUpdateStackMsg = null;
        }
        if (STATE_SET_PREF_MODE == this.mSubState[PRIMARY_STACK_ID]) {
            notifyStackReady(true);
        } else {
            notifyStackReady(false);
        }
    }

    private void sendResponseToTarget(Message response, int responseCode) {
        AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
        response.sendToTarget();
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }
}
