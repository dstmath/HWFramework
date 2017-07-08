package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.Arrays;

public class HwForeignUsimForTelecom extends Handler {
    private static final int CARD_MAX = 0;
    private static final int CARD_TYPE_DUAL_MODE = 3;
    private static final int CARD_TYPE_NO_SIM = 0;
    private static final int CARD_TYPE_SINGLE_CDMA = 2;
    private static final int CARD_TYPE_SINGLE_GSM = 1;
    private static final String CHINA_CC = "86";
    private static final int COMBINE = 0;
    private static final int EVENT_CHECK_ALL_CARDS_READY = 106;
    private static final int EVENT_GET_RAT_COMBINE_MODE_DONE = 104;
    private static final int EVENT_ICC_STATUS_CHANGED = 105;
    private static final int EVENT_RADIO_AVAILABLE = 103;
    private static final int EVENT_RESTART_RILD = 101;
    private static final int EVENT_SET_RAT_COMBINE_DONE = 107;
    private static final int EVENT_SET_RAT_COMBINE_MODE_DONE = 102;
    private static final int ICCCARD = 1;
    private static final int ICCID_LEN_MINIMUM = 6;
    private static final int INVALID = -1;
    private static boolean IS_CHINA_TELECOM = false;
    public static final boolean IS_OVERSEA_USIM_SUPPORT = false;
    private static final int NOT_COMBINE = 1;
    private static final String OVERSEAS_MODE = "persist.radio.overseas_mode";
    private static final String PREFIX_MII_ICCID = "89";
    private static final int SLOT0 = 0;
    private static final int SLOT1 = 1;
    private static final String TAG = "HwForeignUsimForTelecom";
    private static final int UICCCARD = 2;
    private static HwForeignUsimForTelecom mInstance;
    private static final Object mLock = null;
    private boolean mAllCardsReady;
    private int mBalongSimSlot;
    private int[] mCardTypes;
    CommandsInterface[] mCis;
    Context mContext;
    private boolean[] mGetUiccCardsStatusDone;
    private String[] mIccIds;
    private boolean mIsOngoingRestartRild;
    private int mMainSlot;
    private boolean mNeedSwitchRatCombine;
    private int[] mOldMainSwitchTypes;
    private int mPollingCount;
    private boolean[] mRadioOn;
    private int[] mRatCombineMode;
    private Message mSetCombineCompleteMsg;
    private int[] mSwitchTypes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwForeignUsimForTelecom.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwForeignUsimForTelecom.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwForeignUsimForTelecom.<clinit>():void");
    }

    public static HwForeignUsimForTelecom make(Context context, CommandsInterface[] ci) {
        HwForeignUsimForTelecom hwForeignUsimForTelecom;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("HwForeignUsimForTelecom.make() should only be called once");
            }
            mInstance = new HwForeignUsimForTelecom(context, ci);
            hwForeignUsimForTelecom = mInstance;
        }
        return hwForeignUsimForTelecom;
    }

    private HwForeignUsimForTelecom(Context context, CommandsInterface[] ci) {
        this.mMainSlot = INVALID;
        this.mBalongSimSlot = INVALID;
        this.mSwitchTypes = new int[CARD_MAX];
        this.mRatCombineMode = new int[CARD_MAX];
        this.mGetUiccCardsStatusDone = new boolean[CARD_MAX];
        this.mRadioOn = new boolean[CARD_MAX];
        this.mAllCardsReady = IS_OVERSEA_USIM_SUPPORT;
        this.mIccIds = new String[CARD_MAX];
        this.mCardTypes = new int[CARD_MAX];
        this.mPollingCount = SLOT0;
        this.mOldMainSwitchTypes = new int[CARD_MAX];
        this.mIsOngoingRestartRild = IS_OVERSEA_USIM_SUPPORT;
        this.mNeedSwitchRatCombine = true;
        this.mCis = ci;
        this.mContext = context;
        for (int i = SLOT0; i < this.mCis.length; i += SLOT1) {
            Integer index = Integer.valueOf(i);
            this.mCis[i].registerForIccStatusChanged(this, EVENT_ICC_STATUS_CHANGED, index);
            this.mCis[i].registerForAvailable(this, EVENT_ICC_STATUS_CHANGED, index);
            this.mCis[i].registerForAvailable(this, EVENT_RADIO_AVAILABLE, index);
            this.mRatCombineMode[i] = INVALID;
            this.mSwitchTypes[i] = INVALID;
            this.mOldMainSwitchTypes[i] = INVALID;
            this.mGetUiccCardsStatusDone[i] = IS_OVERSEA_USIM_SUPPORT;
            this.mIccIds[i] = null;
            this.mRadioOn[i] = IS_OVERSEA_USIM_SUPPORT;
        }
        logi("HwForeignUsimForTelecom constructor!");
    }

    public static HwForeignUsimForTelecom getInstance() {
        HwForeignUsimForTelecom hwForeignUsimForTelecom;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("HwForeignUsimForTelecom.getInstance can't be called before make()");
            }
            hwForeignUsimForTelecom = mInstance;
        }
        return hwForeignUsimForTelecom;
    }

    public void handleMessage(Message msg) {
        Integer index = getCiIndex(msg);
        AsyncResult ar = msg.obj;
        switch (msg.what) {
            case EVENT_RESTART_RILD /*101*/:
                logd("Received EVENT_RESTART_RILD on index " + index + ", mPollingCount = " + this.mPollingCount);
                try {
                    if (this.mPollingCount == 0) {
                        logd("Finally, restart rild...");
                        this.mIsOngoingRestartRild = true;
                        disposeCardStatus();
                        this.mCis[SLOT0].restartRild(null);
                    }
                } catch (RuntimeException e) {
                }
            case EVENT_SET_RAT_COMBINE_MODE_DONE /*102*/:
                logd("Received EVENT_SET_RAT_COMBINE_MODE_DONE on index " + index);
                if (ar == null || ar.exception != null) {
                    loge("Error! setRatCommbie is failed!!");
                }
                waitToRestartRild();
            case EVENT_RADIO_AVAILABLE /*103*/:
                logd("Received EVENT_RADIO_AVAILABLE on index " + index);
                boolean ready = true;
                this.mRadioOn[index.intValue()] = true;
                boolean[] zArr = this.mRadioOn;
                int length = zArr.length;
                int i = SLOT0;
                while (i < length) {
                    if (zArr[i]) {
                        i += SLOT1;
                    } else {
                        ready = IS_OVERSEA_USIM_SUPPORT;
                        if (!ready) {
                            if (this.mSetCombineCompleteMsg != null) {
                                logd("Switch Combine Mode Done!!");
                                AsyncResult.forMessage(this.mSetCombineCompleteMsg, Boolean.valueOf(true), null);
                                this.mSetCombineCompleteMsg.sendToTarget();
                                this.mSetCombineCompleteMsg = null;
                            }
                            this.mIsOngoingRestartRild = IS_OVERSEA_USIM_SUPPORT;
                        } else if (this.mSetCombineCompleteMsg != null) {
                            logd("clean iccids!!");
                            PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
                        }
                    }
                }
                if (!ready) {
                    if (this.mSetCombineCompleteMsg != null) {
                        logd("Switch Combine Mode Done!!");
                        AsyncResult.forMessage(this.mSetCombineCompleteMsg, Boolean.valueOf(true), null);
                        this.mSetCombineCompleteMsg.sendToTarget();
                        this.mSetCombineCompleteMsg = null;
                    }
                    this.mIsOngoingRestartRild = IS_OVERSEA_USIM_SUPPORT;
                } else if (this.mSetCombineCompleteMsg != null) {
                    logd("clean iccids!!");
                    PhoneFactory.getSubInfoRecordUpdater().cleanIccids();
                }
            case EVENT_GET_RAT_COMBINE_MODE_DONE /*104*/:
                logd("Received EVENT_GET_RAT_COMBINE_MODE_DONE on index " + index);
                onGetRatCombineModeDone(ar, index);
            case EVENT_ICC_STATUS_CHANGED /*105*/:
                logd("Received EVENT_ICC_STATUS_CHANGED on index " + index);
                this.mCis[index.intValue()].getHwRatCombineMode(obtainMessage(EVENT_GET_RAT_COMBINE_MODE_DONE, index));
            case EVENT_CHECK_ALL_CARDS_READY /*106*/:
                logd("Received EVENT_CHECK_ALL_CARDS_READY on index " + index);
                checkIfAllCardsReady();
            case EVENT_SET_RAT_COMBINE_DONE /*107*/:
                logd("Received EVENT_SET_RAT_COMBINE_DONE on index " + index);
                HwAllInOneController.getInstance().setWaitingSwitchBalongSlot(IS_OVERSEA_USIM_SUPPORT);
            default:
        }
    }

    private void onGetRatCombineModeDone(AsyncResult ar, Integer index) {
        int slaveSlot = getAnotherSlotId(index.intValue());
        if (!(ar == null || ar.result == null)) {
            this.mRatCombineMode[index.intValue()] = ((int[]) ar.result)[SLOT0];
        }
        logd("mRatCombineMode[" + index + "] is " + this.mRatCombineMode[index.intValue()] + ", mRatCombineMode[" + slaveSlot + "] is " + this.mRatCombineMode[slaveSlot]);
        checkIfAllCardsReady();
        HwAllInOneController.getInstance().checkIfAllCardsReady();
    }

    public void onGetIccidDone(AsyncResult ar, Integer index) {
        logd("onGetIccidDone on index " + index);
        if (ar == null || ar.exception != null) {
            logd("get iccid exception, maybe card is absent. set iccid as \"\"");
            this.mIccIds[index.intValue()] = "";
            checkIfAllCardsReady();
            return;
        }
        byte[] data = ar.result;
        String iccid = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, SLOT0, data.length);
        if (TextUtils.isEmpty(iccid) || ICCID_LEN_MINIMUM > iccid.length()) {
            logd("iccId is invalid, set it as \"\" ");
            this.mIccIds[index.intValue()] = "";
        } else {
            this.mIccIds[index.intValue()] = iccid.substring(SLOT0, ICCID_LEN_MINIMUM);
        }
        logd("get iccid is " + this.mIccIds[index.intValue()] + " on index " + index);
        checkIfAllCardsReady();
    }

    public synchronized void onQueryCardTypeDone(AsyncResult ar, Integer index) {
        int slaveSlot = getAnotherSlotId(index.intValue());
        this.mOldMainSwitchTypes[index.intValue()] = this.mSwitchTypes[index.intValue()];
        if (!(ar == null || ar.result == null)) {
            this.mSwitchTypes[index.intValue()] = ((int[]) ar.result)[SLOT0] & 15;
            this.mCardTypes[index.intValue()] = (((int[]) ar.result)[SLOT0] & 240) >> 4;
        }
        checkIfAllCardsReady();
        logd("mSwitchTypes[" + index + "] = " + this.mSwitchTypes[index.intValue()] + ", mSwitchTypes[" + slaveSlot + "] = " + this.mSwitchTypes[slaveSlot]);
    }

    public void onGetBalongSimDone(AsyncResult ar, Integer index) {
        logd("onGetBalongSimDone");
        if (ar != null && ar.result != null && ((int[]) ar.result).length == CARD_TYPE_DUAL_MODE) {
            int[] slots = ar.result;
            logd("slot result = " + Arrays.toString(slots));
            if (slots[SLOT0] == 0 && slots[SLOT1] == SLOT1 && slots[UICCCARD] == UICCCARD) {
                this.mBalongSimSlot = SLOT0;
            } else if (slots[SLOT0] == SLOT1 && slots[SLOT1] == 0 && slots[UICCCARD] == UICCCARD) {
                this.mBalongSimSlot = SLOT1;
            } else if (slots[SLOT0] == UICCCARD && slots[SLOT1] == SLOT1 && slots[UICCCARD] == 0) {
                this.mBalongSimSlot = SLOT0;
            } else if (slots[SLOT0] == UICCCARD && slots[SLOT1] == 0 && slots[UICCCARD] == SLOT1) {
                this.mBalongSimSlot = SLOT1;
            } else {
                loge("onGetBalongSimDone invalid slot result");
            }
        } else if (ar == null || ar.result == null || ((int[]) ar.result).length != UICCCARD) {
            loge("onGetBalongSimDone error");
        } else {
            if (((int[]) ar.result)[SLOT1] + ((int[]) ar.result)[SLOT0] > SLOT1) {
                this.mBalongSimSlot = ((int[]) ar.result)[SLOT0] + INVALID;
            } else {
                this.mBalongSimSlot = ((int[]) ar.result)[SLOT0];
            }
        }
        logd("mBalongSimSlot = " + this.mBalongSimSlot);
        checkIfAllCardsReady();
    }

    public synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        logd("mGetUiccCardsStatusDone on index " + index);
        if (ar.exception != null) {
            loge("Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error: " + ar.exception);
        } else if (isValidIndex(index.intValue())) {
            this.mGetUiccCardsStatusDone[index.intValue()] = true;
            Message msg = obtainMessage(EVENT_CHECK_ALL_CARDS_READY, index);
            AsyncResult.forMessage(msg, null, null);
            sendMessage(msg);
        } else {
            loge("onGetIccCardStatusDone: invalid index : " + index);
        }
    }

    public void checkIfAllCardsReady() {
        if (!HwModemCapability.isCapabilitySupport(9)) {
            if (HwVSimUtils.isVSimEnabled()) {
                logd("checkIfAllCardsReady, vsim is on and return");
                disposeLocalCardStatus();
                return;
            }
            boolean ready;
            int i;
            if (INVALID != this.mOldMainSwitchTypes[SLOT0] || INVALID == this.mSwitchTypes[SLOT0] || this.mSwitchTypes[SLOT0] == 0) {
                if (!(INVALID != this.mOldMainSwitchTypes[SLOT1] || INVALID == this.mSwitchTypes[SLOT1] || this.mSwitchTypes[SLOT1] == 0)) {
                }
                ready = true;
                i = SLOT0;
                while (i < CARD_MAX) {
                    if (INVALID != this.mSwitchTypes[i]) {
                        logd("mSwitchTypes[" + i + "] is INVALID");
                        ready = IS_OVERSEA_USIM_SUPPORT;
                        break;
                    } else if (this.mGetUiccCardsStatusDone[i]) {
                        logd("mGetUiccCardsStatusDone[" + i + "] is false");
                        ready = IS_OVERSEA_USIM_SUPPORT;
                        break;
                    } else if (this.mIccIds[i] != null) {
                        logd("mIccIds[" + i + "] is null");
                        ready = IS_OVERSEA_USIM_SUPPORT;
                        break;
                    } else if (INVALID == this.mRatCombineMode[i]) {
                        logd("mRatCombineMode[" + i + "] is INVALID");
                        ready = IS_OVERSEA_USIM_SUPPORT;
                        break;
                    } else {
                        i += SLOT1;
                    }
                }
                if (INVALID == this.mMainSlot) {
                    logd("mMainSlot is not set");
                    ready = IS_OVERSEA_USIM_SUPPORT;
                }
                if (INVALID == this.mBalongSimSlot) {
                    logd("mBalongSimSlot is not set");
                    ready = IS_OVERSEA_USIM_SUPPORT;
                }
                this.mAllCardsReady = ready;
                logd("mAllCardsReady is " + this.mAllCardsReady);
                if (this.mAllCardsReady && this.mSetCombineCompleteMsg == null) {
                    trySwitchCommrilMode();
                }
            }
            logd("checkIfAllCardsReady, set mNeedSwitchRatCombine as true.");
            this.mNeedSwitchRatCombine = true;
            ready = true;
            i = SLOT0;
            while (i < CARD_MAX) {
                if (INVALID != this.mSwitchTypes[i]) {
                    if (this.mGetUiccCardsStatusDone[i]) {
                        if (this.mIccIds[i] != null) {
                            if (INVALID == this.mRatCombineMode[i]) {
                                logd("mRatCombineMode[" + i + "] is INVALID");
                                ready = IS_OVERSEA_USIM_SUPPORT;
                                break;
                            }
                            i += SLOT1;
                        } else {
                            logd("mIccIds[" + i + "] is null");
                            ready = IS_OVERSEA_USIM_SUPPORT;
                            break;
                        }
                    }
                    logd("mGetUiccCardsStatusDone[" + i + "] is false");
                    ready = IS_OVERSEA_USIM_SUPPORT;
                    break;
                }
                logd("mSwitchTypes[" + i + "] is INVALID");
                ready = IS_OVERSEA_USIM_SUPPORT;
                break;
                if (INVALID == this.mMainSlot) {
                    logd("mMainSlot is not set");
                    ready = IS_OVERSEA_USIM_SUPPORT;
                }
                if (INVALID == this.mBalongSimSlot) {
                    logd("mBalongSimSlot is not set");
                    ready = IS_OVERSEA_USIM_SUPPORT;
                }
                this.mAllCardsReady = ready;
                logd("mAllCardsReady is " + this.mAllCardsReady);
                trySwitchCommrilMode();
            }
            if (INVALID == this.mMainSlot) {
                logd("mMainSlot is not set");
                ready = IS_OVERSEA_USIM_SUPPORT;
            }
            if (INVALID == this.mBalongSimSlot) {
                logd("mBalongSimSlot is not set");
                ready = IS_OVERSEA_USIM_SUPPORT;
            }
            this.mAllCardsReady = ready;
            logd("mAllCardsReady is " + this.mAllCardsReady);
            trySwitchCommrilMode();
        }
    }

    private void trySwitchCommrilMode() {
        int foreignUCardSlot = getForeignCardSlot();
        boolean IS_OVERSEAS_MODE = SystemProperties.getBoolean(OVERSEAS_MODE, IS_OVERSEA_USIM_SUPPORT);
        logd("trySwitchCommrilMode foreignUCardSlot: " + foreignUCardSlot + ", IS_OVERSEAS_MODE: " + IS_OVERSEAS_MODE);
        if (!(INVALID == foreignUCardSlot || IS_OVERSEAS_MODE || this.mRatCombineMode[this.mBalongSimSlot] != 0 || isDomesticCard(this.mMainSlot))) {
            logd("trySwitchCommrilMode:need to set ratcombine:NOT_COMBINE on slot " + this.mBalongSimSlot);
            SystemProperties.set(OVERSEAS_MODE, "true");
            this.mSetCombineCompleteMsg = obtainMessage(EVENT_SET_RAT_COMBINE_DONE);
            this.mCis[this.mBalongSimSlot].setHwRatCombineMode(SLOT1, obtainMessage(EVENT_SET_RAT_COMBINE_MODE_DONE, Integer.valueOf(this.mBalongSimSlot)));
            incPollingCount(SLOT1);
        }
        if (this.mPollingCount == 0) {
            this.mNeedSwitchRatCombine = IS_OVERSEA_USIM_SUPPORT;
            logd("mPollingCount is 0");
            if (HwAllInOneController.getInstance().isSetDualCardSlotComplete()) {
                HwAllInOneController.getInstance().setWaitingSwitchBalongSlot(IS_OVERSEA_USIM_SUPPORT);
            }
        }
        logd("trySwitchCommrilMode end");
    }

    private int getAnotherSlotId(int slotId) {
        return slotId == 0 ? SLOT1 : SLOT0;
    }

    private Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(SLOT0);
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
        return (index < 0 || index >= CARD_MAX) ? IS_OVERSEA_USIM_SUPPORT : true;
    }

    public void disposeLocalCardStatus() {
        logd("disposeLocalCardStatus");
        for (int i = SLOT0; i < this.mCis.length; i += SLOT1) {
            this.mSwitchTypes[i] = INVALID;
            this.mOldMainSwitchTypes[i] = INVALID;
            this.mGetUiccCardsStatusDone[i] = IS_OVERSEA_USIM_SUPPORT;
            this.mIccIds[i] = null;
            this.mRatCombineMode[i] = INVALID;
            this.mRadioOn[i] = IS_OVERSEA_USIM_SUPPORT;
        }
        this.mBalongSimSlot = INVALID;
        this.mAllCardsReady = IS_OVERSEA_USIM_SUPPORT;
        this.mNeedSwitchRatCombine = IS_OVERSEA_USIM_SUPPORT;
    }

    private void disposeCardStatus() {
        logd("disposeCardStatus");
        disposeLocalCardStatus();
        for (int i = SLOT0; i < this.mCis.length; i += SLOT1) {
            this.mRatCombineMode[i] = INVALID;
        }
    }

    private int getUserSwitchDualCardSlots() {
        int subscription = SLOT0;
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
        return currSlot == this.mBalongSimSlot ? true : IS_OVERSEA_USIM_SUPPORT;
    }

    public void decPollingCount() {
        if (this.mPollingCount > 0) {
            this.mPollingCount += INVALID;
            logd("decPollingCount, mPollingCount = " + this.mPollingCount);
            return;
        }
        logd("decPollingCount already 0, can't dec!");
    }

    public void incPollingCount(int step) {
        this.mPollingCount += step;
        logd("incPollingCount, mPollingCount = " + this.mPollingCount);
    }

    public void waitToRestartRild() {
        logd("waitToRestartRild");
        decPollingCount();
        removeMessages(EVENT_RESTART_RILD);
        sendEmptyMessage(EVENT_RESTART_RILD);
    }

    public void setMainSlot(int slotId) {
        if (this.mIsOngoingRestartRild) {
            logd("Ready to restart rild, don't change mainSlot, wait till rild restarted!");
        } else {
            this.mMainSlot = slotId;
        }
        logd("setMainSlot is " + this.mMainSlot);
        if (!(slotId == this.mBalongSimSlot ? isBalongSimSynced() : IS_OVERSEA_USIM_SUPPORT)) {
            incPollingCount(SLOT1);
        }
        checkIfAllCardsReady();
    }

    public boolean getRestartRildTag() {
        logd("get mIsOngoingRestartRild " + this.mIsOngoingRestartRild);
        return this.mIsOngoingRestartRild;
    }

    public boolean getSwitchRatCombineTag() {
        logd("get mNeedSwitchRatCombine " + this.mNeedSwitchRatCombine);
        return this.mNeedSwitchRatCombine;
    }

    public String getIccid(int slotId) {
        if (isValidIndex(slotId)) {
            return this.mIccIds[slotId];
        }
        return null;
    }

    public int getRatCombineMode(int slotId) {
        if (isValidIndex(slotId)) {
            return this.mRatCombineMode[slotId];
        }
        return INVALID;
    }

    public boolean isDomesticCard(int slotId) {
        if (!isValidIndex(slotId)) {
            return true;
        }
        if (HwVSimUtils.isVSimEnabled()) {
            logd("vsim is on and return true");
            return true;
        }
        boolean bDomesticCard;
        if (TextUtils.isEmpty(this.mIccIds[slotId]) || !PREFIX_MII_ICCID.equals(this.mIccIds[slotId].substring(SLOT0, UICCCARD)) || HwAllInOneController.getInstance().isCTCard(this.mIccIds[slotId]) || HwAllInOneController.getInstance().isCUCard(this.mIccIds[slotId])) {
            bDomesticCard = true;
        } else {
            bDomesticCard = HwAllInOneController.getInstance().isCMCCCard(this.mIccIds[slotId]);
        }
        logd("bDomesticCard[" + slotId + "] is " + bDomesticCard);
        return bDomesticCard;
    }

    public int getForeignCardSlot() {
        logd("getForeignCardSlot start");
        if (HwVSimUtils.isVSimEnabled()) {
            logd("getForeignCardSlot, vsim is on and return");
            return INVALID;
        } else if (!IS_CHINA_TELECOM) {
            logd("getForeignCardSlot, IS_CHINA_TELECOM is false and return -1.");
            return INVALID;
        } else if (this.mSwitchTypes[SLOT0] == 0 && this.mSwitchTypes[SLOT1] == 0) {
            logd("getForeignCardSlot, two cards are absent and return -1.");
            return INVALID;
        } else if (UICCCARD == this.mSwitchTypes[SLOT0] || UICCCARD == this.mSwitchTypes[SLOT1] || CARD_TYPE_DUAL_MODE == this.mSwitchTypes[SLOT0] || CARD_TYPE_DUAL_MODE == this.mSwitchTypes[SLOT1]) {
            logd("getForeignCardSlot, got CDMA card and return -1.");
            return INVALID;
        } else if (this.mIccIds[SLOT0] == null || this.mIccIds[SLOT1] == null) {
            logd("getForeignCardSlot, got null mIccid and return -1.");
            return INVALID;
        } else if ((this.mSwitchTypes[SLOT0] == 0 || this.mIccIds[SLOT0].length() != 0) && (this.mSwitchTypes[SLOT1] == 0 || this.mIccIds[SLOT1].length() != 0)) {
            boolean bDomesticCard0 = isDomesticCard(SLOT0);
            boolean bDomesticCard1 = isDomesticCard(SLOT1);
            if ((SLOT1 == this.mSwitchTypes[SLOT0] && this.mSwitchTypes[SLOT1] == 0 && bDomesticCard0) || ((SLOT1 == this.mSwitchTypes[SLOT1] && this.mSwitchTypes[SLOT0] == 0 && bDomesticCard1) || (SLOT1 == this.mSwitchTypes[SLOT0] && bDomesticCard0 && SLOT1 == this.mSwitchTypes[SLOT1] && bDomesticCard1))) {
                logd("getForeignCardSlot, got only home gsm card and return -1.");
                return INVALID;
            }
            int mainSlot = INVALID;
            if (bDomesticCard1 || bDomesticCard0) {
                if (bDomesticCard0) {
                    if (!bDomesticCard1) {
                        if (UICCCARD == this.mCardTypes[SLOT1]) {
                            mainSlot = isBalongSimSynced() ? SLOT1 : SLOT0;
                        } else if (SLOT1 == this.mCardTypes[SLOT1]) {
                            mainSlot = isBalongSimSynced() ? SLOT1 : SLOT0;
                        }
                    }
                } else if (UICCCARD == this.mCardTypes[SLOT0]) {
                    mainSlot = isBalongSimSynced() ? SLOT0 : SLOT1;
                } else if (SLOT1 == this.mCardTypes[SLOT0]) {
                    mainSlot = isBalongSimSynced() ? SLOT0 : SLOT1;
                }
            } else if (UICCCARD == this.mCardTypes[SLOT0] && UICCCARD == this.mCardTypes[SLOT1]) {
                mainSlot = getUserSwitchDualCardSlots() == 0 ? SLOT0 : SLOT1;
            } else if (UICCCARD == this.mCardTypes[SLOT0]) {
                mainSlot = isBalongSimSynced() ? SLOT0 : SLOT1;
            } else if (UICCCARD == this.mCardTypes[SLOT1]) {
                mainSlot = isBalongSimSynced() ? SLOT1 : SLOT0;
            } else if (SLOT1 == this.mCardTypes[SLOT0] && SLOT1 == this.mCardTypes[SLOT1]) {
                mainSlot = getUserSwitchDualCardSlots() == 0 ? SLOT0 : SLOT1;
            } else if (SLOT1 == this.mCardTypes[SLOT0]) {
                mainSlot = isBalongSimSynced() ? SLOT0 : SLOT1;
            } else if (SLOT1 == this.mCardTypes[SLOT1]) {
                mainSlot = isBalongSimSynced() ? SLOT1 : SLOT0;
            }
            logd("mainSlot is" + mainSlot);
            return mainSlot;
        } else {
            logd("getForeignCardSlot, got miccid failure and return -1.");
            return INVALID;
        }
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
}
