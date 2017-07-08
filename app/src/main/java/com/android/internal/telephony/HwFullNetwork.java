package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.Rlog;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class HwFullNetwork extends Handler {
    private static final /* synthetic */ int[] -com-android-internal-telephony-HwFullNetwork$CommrilModeSwitchesValues = null;
    private static final int ACTIVE = 1;
    private static final int CARDTRAY_OUT_SLOT = 0;
    private static final String CARDTRAY_STATE_FILE = "/sys/kernel/sim/sim_hotplug_state";
    private static final int CARD_MAX = 0;
    private static final int CARD_TYPE_DUAL_MODE = 3;
    private static final int CARD_TYPE_NO_SIM = 0;
    private static final int CARD_TYPE_SINGLE_CDMA = 2;
    private static final int CARD_TYPE_SINGLE_GSM = 1;
    private static final int CHECK_CDMASIDE_TIMES = 5;
    private static final int COMBINE = 0;
    private static final int EVENT_DELAY_SET_CDMA_MODE_SIDE = 11;
    private static final int EVENT_DELAY_SWITCH_COMMRIL = 6;
    private static final int EVENT_GET_CDMA_MODE_SIDE_DONE = 12;
    private static final int EVENT_GET_RAT_COMBINE_MODE_DONE = 5;
    private static final int EVENT_RADIO_AVIALABLE = 4;
    private static final int EVENT_RADIO_UNAVAILABLE = 14;
    private static final int EVENT_RESTART_RILD = 1;
    private static final int EVENT_SET_CDMA_MODE_SIDE_DONE = 10;
    private static final int EVENT_SET_RAT_COMBINE_MODE_DONE = 2;
    private static final int EVENT_SIM_HOTPLUG = 13;
    private static final int EVENT_SWITCH_RFIC_CHANNEL_DONE = 3;
    private static final int EVENT_SWITCH_SIM_SLOT_CFG_DONE = 8;
    private static final int EVENT_SWTICH_COMMRIL_MODE_DONE = 7;
    private static final int INVALID = -1;
    private static final int INVALID_MODEM = -1;
    protected static final boolean IS_HISI_CDMA_SUPPORTED = false;
    private static final boolean IS_SINGLE_CARD_TRAY = false;
    private static final boolean IS_SUPPORT_FULL_NETWORK = false;
    private static final boolean IS_TUNERIC_LOW_PERF = false;
    private static final int MODEM0 = 0;
    private static final int MODEM1 = 1;
    private static final int NOT_ACTIVE = 0;
    private static final int NOT_COMBINE = 1;
    static final String PROPERTY_CG_STANDBY_MODE = "persist.radio.cg_standby_mode";
    static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    static final String PROPERTY_FULL_NETWORK_SUPPORT = "ro.config.full_network_support";
    static final String PROPERTY_HISI_CDMA_SUPPORTED = "ro.config.hisi_cdma_supported";
    private static final int RETRY_MAX_MILLI_SECONDS = 5000;
    private static final int RF0 = 0;
    private static final int RF1 = 1;
    private static final int SLOT1 = 1;
    private static final int SLOT2 = 2;
    private static final String TAG = "HwFullNetwork";
    private static final int TIME_DELAY_TO_SET_CDMA_MODE_SIDE = 5000;
    private static final int TIME_DELAY_TO_SWITCH_COMMRIL = 100;
    private static HwFullNetwork mInstance;
    private static final Object mLock = null;
    private static final boolean sIsPlatformSupportVSim = false;
    private int CMODEM_STATUS;
    private boolean bCheckedRatCombine;
    private boolean mAllCardsReady;
    private int mBalongSimSlot;
    private int mCdmaSide;
    CommandsInterface[] mCis;
    Context mContext;
    private int mDelayRetryCount;
    private CommrilMode mExpectCommrilMode;
    private boolean[] mGetBalongSimSlotDone;
    private boolean mGetCdmaSideDone;
    private boolean[] mGetUiccCardsStatusDone;
    private HotplugState[] mHotplugState;
    private boolean mIsOngoingRestartRild;
    private int mMainSlot;
    private boolean mNeedSwitchCommrilMode;
    private int[] mOldMainSwitchTypes;
    private int mPollingCount;
    private boolean[] mRadioOn;
    private int[] mRatCombineMode;
    private Message mSetCommrilModeCompleteMsg;
    private int mSwitchCommrilTimes;
    private int[] mSwitchTypes;
    private boolean mWaitingSwitchCommrilMode;

    public enum CommrilMode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwFullNetwork.CommrilMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwFullNetwork.CommrilMode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwFullNetwork.CommrilMode.<clinit>():void");
        }
    }

    public enum HotplugState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwFullNetwork.HotplugState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwFullNetwork.HotplugState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwFullNetwork.HotplugState.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-HwFullNetwork$CommrilModeSwitchesValues() {
        if (-com-android-internal-telephony-HwFullNetwork$CommrilModeSwitchesValues != null) {
            return -com-android-internal-telephony-HwFullNetwork$CommrilModeSwitchesValues;
        }
        int[] iArr = new int[CommrilMode.values().length];
        try {
            iArr[CommrilMode.CG_MODE.ordinal()] = SLOT1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommrilMode.CLG_MODE.ordinal()] = SLOT2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommrilMode.HISI_CGUL_MODE.ordinal()] = EVENT_SWITCH_RFIC_CHANNEL_DONE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommrilMode.HISI_CG_MODE.ordinal()] = EVENT_RADIO_AVIALABLE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommrilMode.NON_MODE.ordinal()] = EVENT_DELAY_SWITCH_COMMRIL;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommrilMode.SVLTE_MODE.ordinal()] = EVENT_SWTICH_COMMRIL_MODE_DONE;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommrilMode.ULG_MODE.ordinal()] = EVENT_GET_RAT_COMBINE_MODE_DONE;
        } catch (NoSuchFieldError e7) {
        }
        -com-android-internal-telephony-HwFullNetwork$CommrilModeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwFullNetwork.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwFullNetwork.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwFullNetwork.<clinit>():void");
    }

    public static HwFullNetwork make(Context context, CommandsInterface[] ci) {
        HwFullNetwork hwFullNetwork;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("MSimUiccController.make() should only be called once");
            }
            mInstance = new HwFullNetwork(context, ci);
            hwFullNetwork = mInstance;
        }
        return hwFullNetwork;
    }

    private HwFullNetwork(Context context, CommandsInterface[] ci) {
        this.bCheckedRatCombine = IS_TUNERIC_LOW_PERF;
        this.mCdmaSide = INVALID_MODEM;
        this.mSwitchCommrilTimes = RF0;
        this.mExpectCommrilMode = CommrilMode.NON_MODE;
        this.mMainSlot = INVALID_MODEM;
        this.mBalongSimSlot = RF0;
        this.mNeedSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
        this.mSwitchTypes = new int[CARD_MAX];
        this.mRatCombineMode = new int[CARD_MAX];
        this.mGetUiccCardsStatusDone = new boolean[CARD_MAX];
        this.mGetBalongSimSlotDone = new boolean[CARD_MAX];
        this.mRadioOn = new boolean[CARD_MAX];
        this.mAllCardsReady = IS_TUNERIC_LOW_PERF;
        this.mWaitingSwitchCommrilMode = true;
        this.mIsOngoingRestartRild = IS_TUNERIC_LOW_PERF;
        this.mDelayRetryCount = RF0;
        this.mPollingCount = RF0;
        this.mGetCdmaSideDone = IS_TUNERIC_LOW_PERF;
        this.CMODEM_STATUS = INVALID_MODEM;
        this.mOldMainSwitchTypes = new int[CARD_MAX];
        this.mHotplugState = new HotplugState[CARD_MAX];
        this.mCis = ci;
        this.mContext = context;
        if (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT) {
            this.mWaitingSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
        } else if (IS_SUPPORT_FULL_NETWORK && !HwModemCapability.isCapabilitySupport(EVENT_SWITCH_RFIC_CHANNEL_DONE)) {
            for (int i = RF0; i < this.mCis.length; i += SLOT1) {
                Integer index = Integer.valueOf(i);
                this.mCis[i].registerForAvailable(this, EVENT_RADIO_AVIALABLE, index);
                this.mRatCombineMode[i] = INVALID_MODEM;
                this.mSwitchTypes[i] = INVALID_MODEM;
                this.mGetUiccCardsStatusDone[i] = IS_TUNERIC_LOW_PERF;
                this.mGetBalongSimSlotDone[i] = IS_TUNERIC_LOW_PERF;
                this.mOldMainSwitchTypes[i] = INVALID_MODEM;
                if (IS_HISI_CDMA_SUPPORTED) {
                    this.mCis[i].registerForSimHotPlug(this, EVENT_SIM_HOTPLUG, index);
                }
                this.mHotplugState[i] = HotplugState.STATE_PLUG_IN;
                this.mCis[i].registerForNotAvailable(this, EVENT_RADIO_UNAVAILABLE, index);
            }
            logi("HwFullNetwork constructor!");
        }
    }

    public static HwFullNetwork getInstance() {
        HwFullNetwork hwFullNetwork;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("HwFullNetwork.getInstance can't be called before make()");
            }
            hwFullNetwork = mInstance;
        }
        return hwFullNetwork;
    }

    public void handleMessage(Message msg) {
        Integer index = getCiIndex(msg);
        AsyncResult ar = msg.obj;
        boolean[] zArr;
        int length;
        int i;
        switch (msg.what) {
            case SLOT1 /*1*/:
                logd("Received EVENT_RESTART_RILD on index " + index + ", mPollingCount = " + this.mPollingCount);
                try {
                    if (this.mPollingCount != 0 || hasMessages(EVENT_DELAY_SWITCH_COMMRIL)) {
                        logd("Don't restart rild now, waiting for other command set done...");
                        this.mWaitingSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
                        return;
                    }
                    if (this.mNeedSwitchCommrilMode && this.mExpectCommrilMode != CommrilMode.NON_MODE) {
                        logd("setCommrilMode to " + this.mExpectCommrilMode);
                        setCommrilMode(this.mExpectCommrilMode);
                        this.mExpectCommrilMode = CommrilMode.NON_MODE;
                        this.mNeedSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
                    }
                    logd("Finally, restart rild...");
                    this.mIsOngoingRestartRild = true;
                    disposeCardStatus();
                    if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
                        HwHotplugController.getInstance().onRestartRild();
                    }
                    this.mCis[RF0].restartRild(null);
                    this.mWaitingSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
                    return;
                } catch (RuntimeException e) {
                }
                break;
            case SLOT2 /*2*/:
                logd("Received EVENT_SET_RAT_COMBINE_MODE_DONE on index " + index);
                if (ar == null || ar.exception != null) {
                    loge("Error! setRatCommbie is failed!!");
                }
                waitToRestartRild();
            case EVENT_SWITCH_RFIC_CHANNEL_DONE /*3*/:
                logd("Received EVENT_SWITCH_RFIC_CHANNEL_DONE on index " + index);
                if (ar == null || ar.exception != null) {
                    loge("Error! switch rf channel is failed!!");
                }
                waitToRestartRild();
            case EVENT_RADIO_AVIALABLE /*4*/:
                logd("Received EVENT_RADIO_AVAILABLE on index " + index);
                this.mCis[index.intValue()].getHwRatCombineMode(obtainMessage(EVENT_GET_RAT_COMBINE_MODE_DONE, index));
                if (IS_HISI_CDMA_SUPPORTED && index.intValue() == 0) {
                    this.mCis[index.intValue()].getCdmaModeSide(obtainMessage(EVENT_GET_CDMA_MODE_SIDE_DONE, index));
                    this.mGetCdmaSideDone = IS_TUNERIC_LOW_PERF;
                }
                boolean ready = true;
                this.mRadioOn[index.intValue()] = true;
                zArr = this.mRadioOn;
                length = zArr.length;
                i = RF0;
                while (i < length) {
                    if (zArr[i]) {
                        i += SLOT1;
                    } else {
                        ready = IS_TUNERIC_LOW_PERF;
                        if (!ready) {
                            if (this.mSetCommrilModeCompleteMsg != null) {
                                logd("Switch CommrilMode Done!!");
                                AsyncResult.forMessage(this.mSetCommrilModeCompleteMsg, Boolean.valueOf(true), null);
                                this.mSetCommrilModeCompleteMsg.sendToTarget();
                                this.mSetCommrilModeCompleteMsg = null;
                            }
                            this.mIsOngoingRestartRild = IS_TUNERIC_LOW_PERF;
                        } else if (this.mSetCommrilModeCompleteMsg != null) {
                            logd("clean iccids!!");
                            PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
                        }
                    }
                }
                if (!ready) {
                    if (this.mSetCommrilModeCompleteMsg != null) {
                        logd("Switch CommrilMode Done!!");
                        AsyncResult.forMessage(this.mSetCommrilModeCompleteMsg, Boolean.valueOf(true), null);
                        this.mSetCommrilModeCompleteMsg.sendToTarget();
                        this.mSetCommrilModeCompleteMsg = null;
                    }
                    this.mIsOngoingRestartRild = IS_TUNERIC_LOW_PERF;
                } else if (this.mSetCommrilModeCompleteMsg != null) {
                    logd("clean iccids!!");
                    PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
                }
            case EVENT_GET_RAT_COMBINE_MODE_DONE /*5*/:
                logd("Received EVENT_GET_RAT_COMBINE_MODE_DONE on index " + index);
                onGetRatCombineModeDone(ar, index);
            case EVENT_DELAY_SWITCH_COMMRIL /*6*/:
                logd("Received EVENT_DELAY_SWITCH_COMMRIL on index " + index);
                if (TIME_DELAY_TO_SET_CDMA_MODE_SIDE == this.mDelayRetryCount && this.mMainSlot == INVALID_MODEM) {
                    logd("Blind-4G not set MainSlot, delay timer expire, Here do check using default 4G slotID!");
                    setMainSlot(HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId(), IS_TUNERIC_LOW_PERF);
                }
                trySwitchCommrilMode();
            case EVENT_SWTICH_COMMRIL_MODE_DONE /*7*/:
                logd("Received EVENT_SWTICH_COMMRIL_MODE_DONE on index " + index);
                logd("current commril mode is " + getCommrilMode().toString());
                if (isntFirstPowerup()) {
                    HwAllInOneController.getInstance().setWaitingSwitchBalongSlot(IS_TUNERIC_LOW_PERF);
                }
            case EVENT_SWITCH_SIM_SLOT_CFG_DONE /*8*/:
                logd("Received EVENT_SWITCH_SIM_SLOT_CFG_DONE on index " + index);
                if (ar == null || ar.exception != null) {
                    loge("Error! switch balong sim slot failed!!");
                }
                waitToRestartRild();
            case EVENT_SET_CDMA_MODE_SIDE_DONE /*10*/:
                logd("Received EVENT_SET_CDMA_MODE_SIDE_DONE on index " + index);
                removeMessages(EVENT_DELAY_SET_CDMA_MODE_SIDE);
                if (ar == null || ar.exception != null) {
                    loge("Error! setCdmaModeSide is failed!!");
                }
                waitToRestartRild();
            case EVENT_DELAY_SET_CDMA_MODE_SIDE /*11*/:
                logd("Received EVENT_DELAY_SET_CDMA_MODE_SIDE");
                removeMessages(EVENT_SET_CDMA_MODE_SIDE_DONE);
                waitToRestartRild();
            case EVENT_GET_CDMA_MODE_SIDE_DONE /*12*/:
                logd("Received EVENT_GET_CDMA_MODE_SIDE_DONE on index " + index);
                onGetCdmaModeDone(ar, index);
                if (HwAllInOneController.IS_HISI_DSDX) {
                    HwAllInOneController.getInstance().checkIfAllCardsReady();
                }
            case EVENT_SIM_HOTPLUG /*13*/:
                onSimHotPlug(ar, index);
            case EVENT_RADIO_UNAVAILABLE /*14*/:
                logd("EVENT_RADIO_UNAVAILABLE, on index " + index);
                this.mSwitchTypes[index.intValue()] = INVALID_MODEM;
                this.mGetUiccCardsStatusDone[index.intValue()] = IS_TUNERIC_LOW_PERF;
                this.mGetBalongSimSlotDone[index.intValue()] = IS_TUNERIC_LOW_PERF;
                this.mOldMainSwitchTypes[index.intValue()] = INVALID_MODEM;
                boolean firstRadioUnavaliable = true;
                zArr = this.mRadioOn;
                length = zArr.length;
                i = RF0;
                while (i < length) {
                    if (zArr[i]) {
                        i += SLOT1;
                    } else {
                        firstRadioUnavaliable = IS_TUNERIC_LOW_PERF;
                        if (firstRadioUnavaliable) {
                            HwAllInOneController.getInstance().setCommrilRestartRild(true);
                        }
                        this.mRadioOn[index.intValue()] = IS_TUNERIC_LOW_PERF;
                    }
                }
                if (firstRadioUnavaliable) {
                    HwAllInOneController.getInstance().setCommrilRestartRild(true);
                }
                this.mRadioOn[index.intValue()] = IS_TUNERIC_LOW_PERF;
            default:
        }
    }

    private void onGetRatCombineModeDone(AsyncResult ar, Integer index) {
        int slaveSlot = getAnotherSlotId(index.intValue());
        if (!(ar == null || ar.result == null)) {
            this.mRatCombineMode[index.intValue()] = ((int[]) ar.result)[RF0];
        }
        logd("mRatCombineMode[" + index + "] is " + this.mRatCombineMode[index.intValue()] + ", mRatCombineMode[" + slaveSlot + "] is " + this.mRatCombineMode[slaveSlot]);
    }

    private void onGetCdmaModeDone(AsyncResult ar, Integer index) {
        this.mGetCdmaSideDone = true;
        if (!HwVSimUtils.isAllowALSwitch()) {
            this.mCdmaSide = INVALID_MODEM;
        } else if (!(ar == null || ar.result == null)) {
            this.mCdmaSide = ((int[]) ar.result)[RF0];
        }
        logd("mCdmaSide is " + this.mCdmaSide);
    }

    private void onSimHotPlug(AsyncResult ar, Integer index) {
        logd("onSimHotPlug");
        if (ar != null && ar.result != null && ((int[]) ar.result).length > 0 && HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.result)[RF0]) {
            singleCardPlugIn(index.intValue());
        }
    }

    public synchronized void onQueryCardTypeDone(AsyncResult ar, Integer index) {
        int slaveSlot = getAnotherSlotId(index.intValue());
        this.mOldMainSwitchTypes[index.intValue()] = this.mSwitchTypes[index.intValue()];
        if (!(ar == null || ar.result == null)) {
            this.mSwitchTypes[index.intValue()] = ((int[]) ar.result)[RF0] & 15;
        }
        logd("mSwitchTypes[" + index + "] = " + this.mSwitchTypes[index.intValue()] + ", mSwitchTypes[" + slaveSlot + "] = " + this.mSwitchTypes[slaveSlot]);
    }

    public void onGetBalongSimDone(AsyncResult ar, Integer index) {
        logd("onGetBalongSimDone");
        if (ar != null && ar.result != null && ((int[]) ar.result).length == EVENT_SWITCH_RFIC_CHANNEL_DONE) {
            int[] slots = ar.result;
            boolean isMainSlotOnVSim = IS_TUNERIC_LOW_PERF;
            logd("slot result = " + Arrays.toString(slots));
            if (slots[RF0] == 0 && slots[SLOT1] == SLOT1 && slots[SLOT2] == SLOT2) {
                this.mBalongSimSlot = RF0;
                isMainSlotOnVSim = IS_TUNERIC_LOW_PERF;
            } else if (slots[RF0] == SLOT1 && slots[SLOT1] == 0 && slots[SLOT2] == SLOT2) {
                this.mBalongSimSlot = SLOT1;
                isMainSlotOnVSim = IS_TUNERIC_LOW_PERF;
            } else if (slots[RF0] == SLOT2 && slots[SLOT1] == SLOT1 && slots[SLOT2] == 0) {
                this.mBalongSimSlot = RF0;
                isMainSlotOnVSim = true;
            } else if (slots[RF0] == SLOT2 && slots[SLOT1] == 0 && slots[SLOT2] == SLOT1) {
                this.mBalongSimSlot = SLOT1;
                isMainSlotOnVSim = true;
            } else {
                loge("onGetBalongSimDone invalid slot result");
            }
            logd("isMainSlotOnVSim = " + isMainSlotOnVSim);
            this.mGetBalongSimSlotDone[index.intValue()] = true;
        } else if (ar == null || ar.result == null || ((int[]) ar.result).length != SLOT2) {
            loge("onGetBalongSimDone error");
        } else {
            if (((int[]) ar.result)[SLOT1] + ((int[]) ar.result)[RF0] > SLOT1) {
                this.mBalongSimSlot = ((int[]) ar.result)[RF0] + INVALID_MODEM;
            } else {
                this.mBalongSimSlot = ((int[]) ar.result)[RF0];
            }
            this.mGetBalongSimSlotDone[index.intValue()] = true;
        }
        logd("mBalongSimSlot = " + this.mBalongSimSlot);
    }

    public synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        if (ar.exception != null) {
            loge("Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error: " + ar.exception);
        } else if (isValidIndex(index.intValue())) {
            this.mGetUiccCardsStatusDone[index.intValue()] = true;
        } else {
            loge("onGetIccCardStatusDone: invalid index : " + index);
        }
    }

    public void checkIfAllCardsReady() {
        if (!HwVSimUtils.isAllowALSwitch()) {
            logd("checkIfAllCardsReady, vsim on sub");
            this.mNeedSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
            this.mWaitingSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
        } else if ((IS_SINGLE_CARD_TRAY && isCardTrayOut(RF0)) || (!IS_SINGLE_CARD_TRAY && isCardTrayOut(RF0) && isCardTrayOut(SLOT1))) {
            logd("checkIfAllCardsReady, both card tray is out, disposeLocalCardStatus.");
            disposeLocalCardStatus();
        } else if (IS_SINGLE_CARD_TRAY && !isCardTrayOut(RF0) && this.mSwitchTypes[RF0] == 0 && this.mSwitchTypes[SLOT1] == 0 && !HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload() && isCardHotPlugIn()) {
            disposeLocalCardStatus();
            if (HwAllInOneController.IS_HISI_DSDX && isGetCdmaModeDone()) {
                HwAllInOneController allInOneController = HwAllInOneController.getInstance();
                if (allInOneController != null) {
                    logd("All tray out. disposeCardStatus");
                    allInOneController.disposeCardStatus(true);
                }
            }
            logd("checkIfAllCardsReady, both cards are absent, return.");
        } else {
            boolean ready;
            int i;
            int countGetBalongSimSlotDone;
            if (INVALID_MODEM != this.mOldMainSwitchTypes[RF0] || INVALID_MODEM == this.mSwitchTypes[RF0] || this.mSwitchTypes[RF0] == 0) {
                if (!(INVALID_MODEM != this.mOldMainSwitchTypes[SLOT1] || INVALID_MODEM == this.mSwitchTypes[SLOT1] || this.mSwitchTypes[SLOT1] == 0)) {
                }
                ready = true;
                i = RF0;
                while (i < CARD_MAX) {
                    if (this.mSwitchTypes[i] != INVALID_MODEM) {
                        ready = IS_TUNERIC_LOW_PERF;
                        break;
                    } else if (!this.mGetUiccCardsStatusDone[i]) {
                        ready = IS_TUNERIC_LOW_PERF;
                        break;
                    } else {
                        i += SLOT1;
                    }
                }
                countGetBalongSimSlotDone = RF0;
                for (i = RF0; i < CARD_MAX; i += SLOT1) {
                    if (!this.mGetBalongSimSlotDone[i]) {
                        countGetBalongSimSlotDone += SLOT1;
                    }
                }
                if (countGetBalongSimSlotDone == 0) {
                    logd("mGetBalongSimSlotDone all false");
                    ready = IS_TUNERIC_LOW_PERF;
                }
                this.mAllCardsReady = ready;
                logd("mAllCardsReady is " + ready);
                if (this.mAllCardsReady && this.mSetCommrilModeCompleteMsg == null) {
                    logi("All uicc card ready!");
                    if (!HwAllInOneController.IS_HISI_DSDX) {
                        trySwitchCommrilMode();
                    }
                }
            }
            if (HwAllInOneController.IS_HISI_DSDX) {
                if (!this.mNeedSwitchCommrilMode) {
                    logd("checkIfAllCardsReady, set mNeedSwitchCommrilMode as true.");
                    this.mNeedSwitchCommrilMode = true;
                }
                if (!(INVALID_MODEM == this.mSwitchTypes[RF0] || this.mSwitchTypes[RF0] == 0)) {
                    this.mOldMainSwitchTypes[RF0] = this.mSwitchTypes[RF0];
                    logd("checkIfAllCardsReady, mOldMainSwitchTypes[SUB1] =" + this.mOldMainSwitchTypes[RF0]);
                }
                if (!(INVALID_MODEM == this.mSwitchTypes[SLOT1] || this.mSwitchTypes[SLOT1] == 0)) {
                    this.mOldMainSwitchTypes[SLOT1] = this.mSwitchTypes[SLOT1];
                    logd("checkIfAllCardsReady, mOldMainSwitchTypes[SUB2] =" + this.mOldMainSwitchTypes[SLOT1]);
                }
                vsimAdjustNeedSwitchCommrilMode();
            } else {
                logd("checkIfAllCardsReady, set mNeedSwitchCommrilMode as true.");
                this.mNeedSwitchCommrilMode = true;
            }
            ready = true;
            i = RF0;
            while (i < CARD_MAX) {
                if (this.mSwitchTypes[i] != INVALID_MODEM) {
                    if (!this.mGetUiccCardsStatusDone[i]) {
                        ready = IS_TUNERIC_LOW_PERF;
                        break;
                    }
                    i += SLOT1;
                } else {
                    ready = IS_TUNERIC_LOW_PERF;
                    break;
                }
                countGetBalongSimSlotDone = RF0;
                for (i = RF0; i < CARD_MAX; i += SLOT1) {
                    if (!this.mGetBalongSimSlotDone[i]) {
                        countGetBalongSimSlotDone += SLOT1;
                    }
                }
                if (countGetBalongSimSlotDone == 0) {
                    logd("mGetBalongSimSlotDone all false");
                    ready = IS_TUNERIC_LOW_PERF;
                }
                this.mAllCardsReady = ready;
                logd("mAllCardsReady is " + ready);
                logi("All uicc card ready!");
                if (HwAllInOneController.IS_HISI_DSDX) {
                    trySwitchCommrilMode();
                }
            }
            countGetBalongSimSlotDone = RF0;
            for (i = RF0; i < CARD_MAX; i += SLOT1) {
                if (!this.mGetBalongSimSlotDone[i]) {
                    countGetBalongSimSlotDone += SLOT1;
                }
            }
            if (countGetBalongSimSlotDone == 0) {
                logd("mGetBalongSimSlotDone all false");
                ready = IS_TUNERIC_LOW_PERF;
            }
            this.mAllCardsReady = ready;
            logd("mAllCardsReady is " + ready);
            logi("All uicc card ready!");
            if (HwAllInOneController.IS_HISI_DSDX) {
                trySwitchCommrilMode();
            }
        }
    }

    public boolean switchCommrilMode(CommrilMode expectCommrilMode, int expectMainSlot, int currMainSlot, Message onCompleteMsg) {
        if (currMainSlot < 0 || currMainSlot >= CARD_MAX || expectMainSlot >= CARD_MAX) {
            loge("invalid slot, currMainSlot = " + currMainSlot + ", expectMainSlot = " + expectMainSlot);
            return IS_TUNERIC_LOW_PERF;
        } else if (this.mSetCommrilModeCompleteMsg != null) {
            loge("FullNetwork is doing switch commril mode, other module shouldn't call!");
            return IS_TUNERIC_LOW_PERF;
        } else {
            this.mNeedSwitchCommrilMode = true;
            this.mSetCommrilModeCompleteMsg = onCompleteMsg;
            switchCommrilMode(expectCommrilMode, currMainSlot);
            if (expectMainSlot >= 0) {
                Message callbackMsg = obtainMessage(EVENT_SWITCH_SIM_SLOT_CFG_DONE);
                if (expectMainSlot == 0) {
                    this.mCis[expectMainSlot].switchBalongSim(SLOT1, SLOT2, callbackMsg);
                } else {
                    this.mCis[expectMainSlot].switchBalongSim(SLOT2, SLOT1, callbackMsg);
                }
                incPollingCount(SLOT1);
            }
            return true;
        }
    }

    public void switchCommrilModeIfNeeded(int mainSlotId, int pollingStepNeedAdded) {
        if (!IS_SUPPORT_FULL_NETWORK) {
            logd("Not support full network!");
        } else if (!HwModemCapability.isCapabilitySupport(EVENT_SWITCH_RFIC_CHANNEL_DONE)) {
            if (HwVSimUtils.isAllowALSwitch()) {
                logd("called by outside ,switch 4G slot to " + mainSlotId + " manully or blind-4G, check if need swtich commrilmode first.");
                if (HwAllInOneController.IS_HISI_DSDX && this.mSetCommrilModeCompleteMsg != null) {
                    loge("switchCommrilModeIfNeeded in switching mSetCommrilModeCompleteMsg is not null");
                    return;
                } else if (mainSlotId < 0 || mainSlotId >= CARD_MAX) {
                    loge("mainSlotId invalid, " + mainSlotId);
                    return;
                } else {
                    incPollingCount(pollingStepNeedAdded);
                    if (setMainSlot(mainSlotId, IS_TUNERIC_LOW_PERF)) {
                        trySwitchCommrilMode();
                    }
                    return;
                }
            }
            logd("switchCommrilModeIfNeeded, vsim on sub");
            this.mNeedSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
            this.mWaitingSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
        }
    }

    private void trySwitchCommrilMode() {
        if (!HwVSimUtils.isAllowALSwitch()) {
            logd("trySwitchCommrilMode, vsim on sub");
            this.mNeedSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
            this.mWaitingSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
        } else if ((IS_SINGLE_CARD_TRAY && isCardTrayOut(RF0)) || (!IS_SINGLE_CARD_TRAY && isCardTrayOut(RF0) && isCardTrayOut(SLOT1))) {
            logd("trySwitchCommrilMode, both card tray is out, disposeLocalCardStatus.");
            disposeLocalCardStatus();
        } else if (IS_SINGLE_CARD_TRAY && !isCardTrayOut(RF0) && ((this.mSwitchTypes[RF0] == 0 && this.mSwitchTypes[SLOT1] == 0 && isCardHotPlugIn()) || (INVALID_MODEM == this.mSwitchTypes[RF0] && INVALID_MODEM == this.mSwitchTypes[SLOT1]))) {
            logd("trySwitchCommrilMode, both cards are absent, return.");
            disposeLocalCardStatus();
        } else {
            logd("trySwitchCommrilMode, mAllCardsReady = " + this.mAllCardsReady + ", mMainSlot = " + this.mMainSlot);
            if (this.mAllCardsReady && this.mSetCommrilModeCompleteMsg == null && this.mMainSlot != INVALID_MODEM) {
                this.mDelayRetryCount = RF0;
                removeMessages(EVENT_DELAY_SWITCH_COMMRIL);
                switchCommrilModeIfNeeded();
            } else if (this.mDelayRetryCount < TIME_DELAY_TO_SET_CDMA_MODE_SIDE) {
                removeMessages(EVENT_DELAY_SWITCH_COMMRIL);
                sendEmptyMessageDelayed(EVENT_DELAY_SWITCH_COMMRIL, 100);
                this.mDelayRetryCount += TIME_DELAY_TO_SWITCH_COMMRIL;
            }
        }
    }

    private void switchCommrilModeIfNeeded() {
        logd("[switchCommrilModeIfNeeded]: mainslot = " + this.mMainSlot + ", cardType[SUB0] = " + this.mSwitchTypes[RF0] + ", cardType[SUB1] = " + this.mSwitchTypes[SLOT1]);
        CommrilMode currentMode = getCommrilMode();
        if (!(checkRatCombineModeMatched(currentMode, this.mMainSlot, this.mRatCombineMode) || IS_HISI_CDMA_SUPPORTED)) {
            currentMode = CommrilMode.NON_MODE;
            logd("[switchCommrilModeIfNeeded]: combineMode isn't Matched, set currentMode to NON_MODE");
        }
        if (!checkCdmaModeMatched(currentMode)) {
            currentMode = CommrilMode.NON_MODE;
            logd("[switchCommrilModeIfNeeded]: CdmaMode isn't Matched, set currentMode to NON_MODE");
        }
        if (!isBalongSimSynced()) {
            logd("mBalongSimSlot != mMainSlot");
        }
        this.mExpectCommrilMode = getExpectCommrilMode(this.mMainSlot, this.mSwitchTypes);
        logd("[switchCommrilModeIfNeeded]: CurrentCommrilMode = " + currentMode);
        if (this.mExpectCommrilMode == currentMode || this.mExpectCommrilMode == CommrilMode.NON_MODE) {
            logd("[switchCommrilModeIfNeeded]: Don't need switch commrilMode...");
            this.mNeedSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
            this.mWaitingSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
            this.mSwitchCommrilTimes = RF0;
            if (HwAllInOneController.getInstance().isSetDualCardSlotComplete()) {
                HwAllInOneController.getInstance().setWaitingSwitchBalongSlot(IS_TUNERIC_LOW_PERF);
            }
            if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
                logd("[switchCommrilModeIfNeeded]:Hotswap is supported!");
                HwHotplugController.getInstance().processNotifyPromptHotPlug(IS_TUNERIC_LOW_PERF);
                return;
            }
            logd("[switchCommrilModeIfNeeded]:Hot swap is not supported!");
            return;
        }
        logd("[switchCommrilModeIfNeeded]: Need switch commrilMode...");
        this.mSetCommrilModeCompleteMsg = obtainMessage(EVENT_SWTICH_COMMRIL_MODE_DONE);
        switchCommrilMode(this.mExpectCommrilMode, this.mBalongSimSlot);
        this.mSwitchCommrilTimes += SLOT1;
        this.mNeedSwitchCommrilMode = true;
    }

    private void switchCommrilMode(CommrilMode newCommrilMode, int mainSlot) {
        logd("[switchCommrilMode]: newCommrilMode = " + newCommrilMode + ", mainSlot = " + mainSlot);
        int slaveSlot = getAnotherSlotId(mainSlot);
        switch (-getcom-android-internal-telephony-HwFullNetwork$CommrilModeSwitchesValues()[newCommrilMode.ordinal()]) {
            case SLOT1 /*1*/:
                this.mCis[mainSlot].setHwRatCombineMode(SLOT1, obtainMessage(SLOT2, Integer.valueOf(mainSlot)));
                String cg_standby_mode = SystemProperties.get(PROPERTY_CG_STANDBY_MODE, "home");
                logd("[switchCommrilMode]: cg_standby_mode = " + cg_standby_mode);
                if ("roam_gsm".equals(cg_standby_mode)) {
                    this.mCis[slaveSlot].setHwRatCombineMode(SLOT1, obtainMessage(SLOT2, Integer.valueOf(slaveSlot)));
                    this.mCis[mainSlot].setHwRFChannelSwitch(RF0, obtainMessage(EVENT_SWITCH_RFIC_CHANNEL_DONE, Integer.valueOf(mainSlot)));
                } else {
                    this.mCis[slaveSlot].setHwRatCombineMode(RF0, obtainMessage(SLOT2, Integer.valueOf(slaveSlot)));
                    this.mCis[mainSlot].setHwRFChannelSwitch(SLOT1, obtainMessage(EVENT_SWITCH_RFIC_CHANNEL_DONE, Integer.valueOf(mainSlot)));
                }
                incPollingCount(EVENT_SWITCH_RFIC_CHANNEL_DONE);
                logd("[switchCommrilMode]: Send set CG_MODE request done...");
            case SLOT2 /*2*/:
                boolean clg_overseas_mode = SystemProperties.getBoolean("persist.radio.overseas_mode", IS_TUNERIC_LOW_PERF);
                logd("[switchCommrilMode]: clg_overseas_mode = " + clg_overseas_mode);
                if (clg_overseas_mode) {
                    this.mCis[mainSlot].setHwRatCombineMode(SLOT1, obtainMessage(SLOT2, Integer.valueOf(mainSlot)));
                } else {
                    this.mCis[mainSlot].setHwRatCombineMode(RF0, obtainMessage(SLOT2, Integer.valueOf(mainSlot)));
                }
                this.mCis[slaveSlot].setHwRatCombineMode(SLOT1, obtainMessage(SLOT2, Integer.valueOf(slaveSlot)));
                this.mCis[mainSlot].setHwRFChannelSwitch(RF0, obtainMessage(EVENT_SWITCH_RFIC_CHANNEL_DONE, Integer.valueOf(mainSlot)));
                incPollingCount(EVENT_SWITCH_RFIC_CHANNEL_DONE);
                logd("[switchCommrilMode]: Send set CLG_MODE request done...");
            case EVENT_SWITCH_RFIC_CHANNEL_DONE /*3*/:
                this.mCis[this.mBalongSimSlot].setCdmaModeSide(RF0, obtainMessage(EVENT_SET_CDMA_MODE_SIDE_DONE));
                sendEmptyMessageDelayed(EVENT_DELAY_SET_CDMA_MODE_SIDE, 5000);
                incPollingCount(SLOT1);
                logd("[switchCommrilMode]: Send set EVENT_SET_CDMA_MODE_SIDE_DONE to modem0 request done...");
            case EVENT_RADIO_AVIALABLE /*4*/:
                this.mCis[this.mBalongSimSlot].setCdmaModeSide(SLOT1, obtainMessage(EVENT_SET_CDMA_MODE_SIDE_DONE));
                sendEmptyMessageDelayed(EVENT_DELAY_SET_CDMA_MODE_SIDE, 5000);
                incPollingCount(SLOT1);
                logd("[switchCommrilMode]: Send set EVENT_SET_CDMA_MODE_SIDE_DONE to modem1 request done...");
            case EVENT_GET_RAT_COMBINE_MODE_DONE /*5*/:
                this.mCis[mainSlot].setHwRatCombineMode(SLOT1, obtainMessage(SLOT2, Integer.valueOf(mainSlot)));
                this.mCis[slaveSlot].setHwRatCombineMode(SLOT1, obtainMessage(SLOT2, Integer.valueOf(slaveSlot)));
                this.mCis[mainSlot].setHwRFChannelSwitch(RF0, obtainMessage(EVENT_SWITCH_RFIC_CHANNEL_DONE, Integer.valueOf(mainSlot)));
                incPollingCount(EVENT_SWITCH_RFIC_CHANNEL_DONE);
                logd("[switchCommrilMode]: Send set ULG_MODE request done...");
            default:
                loge("[switchCommrilMode]: Error!! Shouldn't enter here!!");
                if (this.mSetCommrilModeCompleteMsg != null) {
                    AsyncResult.forMessage(this.mSetCommrilModeCompleteMsg, Boolean.valueOf(IS_TUNERIC_LOW_PERF), null);
                    this.mSetCommrilModeCompleteMsg.sendToTarget();
                    this.mSetCommrilModeCompleteMsg = null;
                }
        }
    }

    private boolean checkCdmaModeMatched(CommrilMode mode) {
        logd("checkCdmaModeMatched enter mCheckCSideTime:" + this.mSwitchCommrilTimes);
        if (this.mCdmaSide == INVALID_MODEM || !IS_HISI_CDMA_SUPPORTED) {
            logd("checkCdmaModeMatched: mCdmaSide is invalid, return true.");
            return true;
        } else if (this.mSwitchCommrilTimes > EVENT_GET_RAT_COMBINE_MODE_DONE) {
            return true;
        } else {
            switch (-getcom-android-internal-telephony-HwFullNetwork$CommrilModeSwitchesValues()[mode.ordinal()]) {
                case EVENT_SWITCH_RFIC_CHANNEL_DONE /*3*/:
                    if (this.mCdmaSide == 0) {
                        return true;
                    }
                    break;
                case EVENT_RADIO_AVIALABLE /*4*/:
                    if (this.mCdmaSide == SLOT1) {
                        return true;
                    }
                    break;
            }
            return IS_TUNERIC_LOW_PERF;
        }
    }

    private boolean checkRatCombineModeMatched(CommrilMode mode, int mainSlot, int[] ratCombineMode) {
        logd("checkRatCombineModeMatched: CommrilMode = " + mode + ", mainSlot = " + mainSlot + ", ratCombineMode = " + ratCombineMode[RF0] + ", " + ratCombineMode[SLOT1]);
        int slaveSlot = getAnotherSlotId(mainSlot);
        if (ratCombineMode[mainSlot] == INVALID_MODEM || ratCombineMode[slaveSlot] == INVALID_MODEM) {
            logd("checkRatCombineModeMatched: ratCombineMode is invalid, return true.");
            return true;
        }
        if (IS_TUNERIC_LOW_PERF && HwModemCapability.isCapabilitySupport(EVENT_RADIO_UNAVAILABLE)) {
            int currentCmodemStatus;
            if (ratCombineMode[mainSlot] == 0 || ratCombineMode[slaveSlot] == 0) {
                currentCmodemStatus = SLOT1;
            } else {
                currentCmodemStatus = RF0;
            }
            logd("currentCmodemStatus:" + currentCmodemStatus + " oldCmodemStatus:" + this.CMODEM_STATUS);
            if (currentCmodemStatus != this.CMODEM_STATUS) {
                this.mCis[mainSlot].notifyCModemStatus(currentCmodemStatus, null);
                this.CMODEM_STATUS = currentCmodemStatus;
            }
        }
        if (this.bCheckedRatCombine) {
            logd("checkRatCombineModeMatched is already implemented, return true.");
            return true;
        }
        logd("checkRatCombineModeMatched set bCheckedRatCombine as true.");
        this.bCheckedRatCombine = true;
        switch (-getcom-android-internal-telephony-HwFullNetwork$CommrilModeSwitchesValues()[mode.ordinal()]) {
            case SLOT1 /*1*/:
                String cg_standby_mode = SystemProperties.get(PROPERTY_CG_STANDBY_MODE, "home");
                logd("cg_standby_mode = " + cg_standby_mode);
                if ("roam_gsm".equals(cg_standby_mode)) {
                    return (ratCombineMode[mainSlot] == SLOT1 && ratCombineMode[slaveSlot] == SLOT1) ? true : IS_TUNERIC_LOW_PERF;
                } else {
                    if (ratCombineMode[slaveSlot] == 0 && ratCombineMode[mainSlot] == SLOT1) {
                        return true;
                    }
                }
            case SLOT2 /*2*/:
                String overseas_mode = SystemProperties.get("persist.radio.overseas_mode", "false");
                logd("overseas_mode = " + overseas_mode);
                if ("true".equals(overseas_mode)) {
                    if (ratCombineMode[mainSlot] == SLOT1 && ratCombineMode[slaveSlot] == SLOT1) {
                        return true;
                    }
                } else if (ratCombineMode[mainSlot] == 0 && ratCombineMode[slaveSlot] == SLOT1) {
                    return true;
                }
            case EVENT_GET_RAT_COMBINE_MODE_DONE /*5*/:
                if (ratCombineMode[mainSlot] == SLOT1 && ratCombineMode[slaveSlot] == SLOT1) {
                    return true;
                }
        }
    }

    private CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        if (mainSlot == INVALID_MODEM) {
            logd("main slot invalid");
            return expectCommrilMode;
        }
        int anotherSlot = getAnotherSlotId(mainSlot);
        if (cardType[mainSlot] == SLOT2 || cardType[mainSlot] == EVENT_SWITCH_RFIC_CHANNEL_DONE) {
            if (IS_HISI_CDMA_SUPPORTED) {
                expectCommrilMode = CommrilMode.HISI_CGUL_MODE;
            } else {
                expectCommrilMode = CommrilMode.CLG_MODE;
            }
        } else if (cardType[mainSlot] == SLOT1 && (cardType[anotherSlot] == SLOT2 || cardType[anotherSlot] == EVENT_SWITCH_RFIC_CHANNEL_DONE)) {
            if (IS_HISI_CDMA_SUPPORTED) {
                expectCommrilMode = CommrilMode.HISI_CG_MODE;
            } else {
                expectCommrilMode = CommrilMode.CG_MODE;
            }
        } else if (cardType[mainSlot] == SLOT1) {
            if (IS_HISI_CDMA_SUPPORTED) {
                expectCommrilMode = CommrilMode.HISI_CGUL_MODE;
            } else {
                expectCommrilMode = CommrilMode.ULG_MODE;
            }
        } else if (cardType[mainSlot] == 0 && (cardType[anotherSlot] == SLOT2 || cardType[anotherSlot] == EVENT_SWITCH_RFIC_CHANNEL_DONE)) {
            if (IS_HISI_CDMA_SUPPORTED) {
                expectCommrilMode = CommrilMode.HISI_CG_MODE;
            } else {
                expectCommrilMode = CommrilMode.CG_MODE;
            }
        } else if (cardType[mainSlot] == 0 && cardType[anotherSlot] == SLOT1 && (CommrilMode.CG_MODE == getCommrilMode() || CommrilMode.HISI_CG_MODE == getCommrilMode())) {
            if (IS_HISI_CDMA_SUPPORTED) {
                expectCommrilMode = CommrilMode.HISI_CGUL_MODE;
            } else {
                expectCommrilMode = CommrilMode.ULG_MODE;
            }
        } else if (cardType[mainSlot] == 0 && cardType[anotherSlot] == SLOT1 && CommrilMode.ULG_MODE == getCommrilMode() && this.mRatCombineMode[mainSlot] != this.mRatCombineMode[anotherSlot]) {
            expectCommrilMode = CommrilMode.ULG_MODE;
        } else {
            expectCommrilMode = CommrilMode.NON_MODE;
        }
        logd("[getExpectCommrilMode]: expectCommrilMode = " + expectCommrilMode);
        return expectCommrilMode;
    }

    private int getAnotherSlotId(int slotId) {
        return slotId == 0 ? SLOT1 : RF0;
    }

    private void setCommrilMode(CommrilMode mode) {
        SystemProperties.set(PROPERTY_COMMRIL_MODE, mode.toString());
    }

    public CommrilMode getCommrilMode() {
        return (CommrilMode) Enum.valueOf(CommrilMode.class, SystemProperties.get(PROPERTY_COMMRIL_MODE, "CLG_MODE"));
    }

    private Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(RF0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return ar.userObj;
    }

    private boolean isValidIndex(int index) {
        return (index < 0 || index >= CARD_MAX) ? IS_TUNERIC_LOW_PERF : true;
    }

    private boolean isntFirstPowerup() {
        logd(" isntFirstPowerup   ------>>>  " + true);
        return true;
    }

    public void singleCardPlugIn(int cardIndex) {
        logd("singleCardPlugIn cardIndex:" + cardIndex);
        if (cardIndex >= 0 && cardIndex < CARD_MAX) {
            this.mSwitchTypes[cardIndex] = INVALID_MODEM;
            this.mHotplugState[cardIndex] = HotplugState.STATE_PLUG_IN;
            if (HwAllInOneController.IS_HISI_DSDX) {
                HwAllInOneController allInOneController = HwAllInOneController.getInstance();
                if (allInOneController != null) {
                    allInOneController.disposeCardStatus(cardIndex);
                }
            }
        }
    }

    public boolean isCardHotPlugIn() {
        if (!IS_SINGLE_CARD_TRAY || !IS_HISI_CDMA_SUPPORTED) {
            return true;
        }
        for (int i = RF0; i < CARD_MAX; i += SLOT1) {
            if (HotplugState.STATE_PLUG_OUT == this.mHotplugState[i]) {
                return IS_TUNERIC_LOW_PERF;
            }
        }
        return true;
    }

    public void setCardHotPlugState(HotplugState state) {
        if (IS_HISI_CDMA_SUPPORTED) {
            for (int i = RF0; i < CARD_MAX; i += SLOT1) {
                this.mHotplugState[i] = state;
            }
        }
    }

    public void disposeLocalCardStatus() {
        logd("disposeLocalCardStatus");
        for (int i = RF0; i < this.mCis.length; i += SLOT1) {
            this.mSwitchTypes[i] = INVALID_MODEM;
            this.mGetUiccCardsStatusDone[i] = IS_TUNERIC_LOW_PERF;
            this.mGetBalongSimSlotDone[i] = IS_TUNERIC_LOW_PERF;
            this.mOldMainSwitchTypes[i] = INVALID_MODEM;
        }
        this.mDelayRetryCount = RF0;
        this.mAllCardsReady = IS_TUNERIC_LOW_PERF;
        this.mNeedSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
        this.mWaitingSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
        setCardHotPlugState(HotplugState.STATE_PLUG_OUT);
    }

    private void disposeCardStatus() {
        logd("disposeCardStatus");
        disposeLocalCardStatus();
        for (int i = RF0; i < this.mCis.length; i += SLOT1) {
            this.mRatCombineMode[i] = INVALID_MODEM;
            this.mRadioOn[i] = IS_TUNERIC_LOW_PERF;
        }
        this.mCdmaSide = INVALID_MODEM;
        HwAllInOneController.getInstance().disposeCardStatus((boolean) IS_TUNERIC_LOW_PERF);
        HwAllInOneController.getInstance().setCommrilRestartRild(true);
    }

    private int getUserSwitchDualCardSlots() {
        int subscription = RF0;
        try {
            subscription = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Switch Dual Card Slots Values");
        }
        return subscription;
    }

    private boolean isBalongSimSynced() {
        int currSlot = getUserSwitchDualCardSlots();
        logd("currSlot  = " + currSlot + ", mBalongSimSlot = " + this.mBalongSimSlot);
        return currSlot == this.mBalongSimSlot ? true : IS_TUNERIC_LOW_PERF;
    }

    public void decPollingCount() {
        if (this.mPollingCount > 0) {
            this.mPollingCount += INVALID_MODEM;
            logd("Dec, mPollingCount = " + this.mPollingCount);
            return;
        }
        loge("polling count already 0, can't dec!");
    }

    public void incPollingCount(int step) {
        this.mPollingCount += step;
        logd("Inc, mPollingCount = " + this.mPollingCount);
    }

    public boolean setMainSlot(int mainSlot, boolean triggerSwith) {
        if (this.mMainSlot == mainSlot && !HwAllInOneController.IS_HISI_DSDX) {
            logd("mainSlot is " + mainSlot + ", not change, do nothing!");
            return IS_TUNERIC_LOW_PERF;
        } else if (mainSlot == INVALID_MODEM) {
            logd("mainSlot is -1, invalid parameter, do nothing!");
            return IS_TUNERIC_LOW_PERF;
        } else if (this.mIsOngoingRestartRild) {
            logd("Ready to restart rild, don't change mainSlot, wait till rild restarted!");
            return IS_TUNERIC_LOW_PERF;
        } else {
            this.mMainSlot = mainSlot;
            logi("setMainSlot " + mainSlot);
            if (triggerSwith) {
                trySwitchCommrilMode();
            }
            return true;
        }
    }

    public void waitToRestartRild() {
        logd("waitToRestartRild");
        decPollingCount();
        removeMessages(SLOT1);
        sendEmptyMessage(SLOT1);
    }

    public boolean getWaitingSwitchCommrilMode() {
        logd("getWaitingSwitchCommrilMode " + this.mWaitingSwitchCommrilMode);
        return this.mWaitingSwitchCommrilMode;
    }

    public boolean getNeedSwitchCommrilMode() {
        logd("getNeedSwitchCommrilMode " + this.mNeedSwitchCommrilMode);
        return this.mNeedSwitchCommrilMode;
    }

    private boolean isCardTrayOut(int SlotId) {
        Throwable th;
        boolean z = true;
        byte[] cardTrayState = new byte[EVENT_RADIO_AVIALABLE];
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(CARDTRAY_STATE_FILE);
            try {
                int length = fis2.read(cardTrayState, RF0, EVENT_RADIO_AVIALABLE);
                fis2.close();
                if (length < EVENT_RADIO_AVIALABLE) {
                    loge("isCardTrayOut read byte fail.");
                    if (fis2 != null) {
                        try {
                            fis2.close();
                        } catch (IOException e) {
                            return IS_TUNERIC_LOW_PERF;
                        }
                    }
                    return IS_TUNERIC_LOW_PERF;
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e2) {
                        return IS_TUNERIC_LOW_PERF;
                    }
                }
                if (SlotId < 0 || SlotId > SLOT1) {
                    return IS_TUNERIC_LOW_PERF;
                }
                if (cardTrayState[(SlotId * SLOT2) + SLOT1] != null) {
                    z = IS_TUNERIC_LOW_PERF;
                }
                return z;
            } catch (IOException e3) {
                fis = fis2;
                try {
                    loge("isCardTrayOut Exception");
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e4) {
                            return IS_TUNERIC_LOW_PERF;
                        }
                    }
                    return IS_TUNERIC_LOW_PERF;
                } catch (Throwable th2) {
                    th = th2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e5) {
                            return IS_TUNERIC_LOW_PERF;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                if (fis != null) {
                    fis.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            loge("isCardTrayOut Exception");
            if (fis != null) {
                fis.close();
            }
            return IS_TUNERIC_LOW_PERF;
        }
    }

    public boolean isOngoingRestartRild() {
        logd("isOngoingRestartRild " + this.mIsOngoingRestartRild);
        return this.mIsOngoingRestartRild;
    }

    private static void logd(String message) {
        Rlog.d(TAG, message);
    }

    private static void logi(String message) {
        Rlog.i(TAG, message);
    }

    private static void loge(String message) {
        Rlog.e(TAG, message);
    }

    public boolean isGetCdmaModeDone() {
        if (IS_HISI_CDMA_SUPPORTED) {
            return this.mGetCdmaSideDone;
        }
        return true;
    }

    private void vsimAdjustNeedSwitchCommrilMode() {
        if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate() || !HwVSimUtils.isAllowALSwitch()) {
            logd("vsim on sub, set mNeedSwitchCommrilMode as false");
            this.mNeedSwitchCommrilMode = IS_TUNERIC_LOW_PERF;
        }
    }
}
