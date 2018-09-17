package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractSubscriptionInfoUpdater.SubscriptionInfoUpdaterReference;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.io.FileInputStream;
import java.io.IOException;

public class HwSubscriptionInfoUpdaterReference implements SubscriptionInfoUpdaterReference {
    private static final byte[] C2 = null;
    private static final int CARDTRAY_OUT_SLOT = 0;
    private static final String CARDTRAY_STATE_FILE = "/sys/kernel/sim/sim_hotplug_state";
    private static final boolean DBG = true;
    private static final int EVENT_ICC_CHANGED = 101;
    private static final int EVENT_QUERY_ICCID_DONE = 103;
    private static final int EVENT_STACK_READY = 102;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String ICCID_STRING_FOR_NV = "DUMMY_NV_ID";
    public static final boolean IS_MODEM_CAPABILITY_SUPPORT = false;
    public static final boolean IS_QUICK_BROADCAST_STATUS = false;
    private static final boolean IS_SINGLE_CARD_TRAY = false;
    private static final String LOG_TAG = "HwSubscriptionInfoUpdaterReference";
    private static final String MASTER_PASSWORD = null;
    private static final int PROJECT_SIM_NUM = 0;
    private static final boolean VDBG = false;
    private static Context mContext;
    private static IccFileHandler[] mFh;
    private static IccRecords[] mIccRecords;
    private static UiccController mUiccController;
    private static CardState[] sCardState;
    private static SubscriptionInfoUpdaterUtils subscriptionInfoUpdaterUtils;
    private String[] internalOldIccId;
    private boolean isNVSubAvailable;
    private boolean mChangeIccidDone;
    private CommandsInterface[] mCis;
    private Handler mHandler;
    private SubscriptionInfoUpdater mSubscriptionInfoUpdater;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwSubscriptionInfoUpdaterReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwSubscriptionInfoUpdaterReference.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwSubscriptionInfoUpdaterReference.<clinit>():void");
    }

    public HwSubscriptionInfoUpdaterReference(SubscriptionInfoUpdater subscriptionInfoUpdater) {
        this.isNVSubAvailable = IS_SINGLE_CARD_TRAY;
        this.mChangeIccidDone = IS_SINGLE_CARD_TRAY;
        this.mHandler = null;
        this.internalOldIccId = new String[PROJECT_SIM_NUM];
        this.mSubscriptionInfoUpdater = subscriptionInfoUpdater;
    }

    public void subscriptionInfoInit(Handler handler, Context context, CommandsInterface[] ci) {
        this.mCis = (CommandsInterface[]) ci.clone();
        this.mHandler = handler;
        mContext = context;
        SubscriptionHelper.init(context, ci);
        mUiccController = UiccController.getInstance();
        mUiccController.registerForIccChanged(this.mHandler, EVENT_ICC_CHANGED, null);
        HwModemStackController.getInstance().registerForStackReady(this.mHandler, EVENT_STACK_READY, null);
        for (int i = CARDTRAY_OUT_SLOT; i < PROJECT_SIM_NUM; i++) {
            sCardState[i] = CardState.CARDSTATE_ABSENT;
        }
    }

    public void handleMessageExtend(Message msg) {
        AsyncResult ar = msg.obj;
        switch (msg.what) {
            case EVENT_ICC_CHANGED /*101*/:
                Integer cardIndex = Integer.valueOf(CARDTRAY_OUT_SLOT);
                if (ar.result != null) {
                    updateIccAvailability(ar.result.intValue());
                    break;
                } else {
                    loge("Error: Invalid card index EVENT_ICC_CHANGED ");
                }
            case EVENT_STACK_READY /*102*/:
                logd("EVENT_STACK_READY");
                if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater) && PROJECT_SIM_NUM > 1) {
                    SubscriptionHelper.getInstance().updateSubActivation(subscriptionInfoUpdaterUtils.getInsertSimState(this.mSubscriptionInfoUpdater), DBG);
                    break;
                }
            case EVENT_QUERY_ICCID_DONE /*103*/:
                Integer slotId = ar.userObj;
                logd("handleMessage : <EVENT_QUERY_ICCID_DONE> SIM" + (slotId.intValue() + 1));
                if (ar.exception == null) {
                    if (ar.result != null) {
                        String iccId;
                        if (!(ar.result instanceof byte[])) {
                            try {
                                iccId = ar.result;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                break;
                            }
                        }
                        byte[] data = ar.result;
                        iccId = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, CARDTRAY_OUT_SLOT, data.length);
                        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = iccId;
                        if (subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] != null && subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()].trim().length() == 0) {
                            subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = "emptyiccid" + slotId;
                        }
                        if (HwVSimUtils.needBlockUnReservedForVsim(slotId.intValue())) {
                            subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                            logd("the slot is unreserved for vsim,just set to no_sim");
                        }
                    } else {
                        logd("Null ar");
                        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                    }
                } else if ((ar.exception instanceof CommandException) && (((CommandException) ar.exception).getCommandError() == Error.RADIO_NOT_AVAILABLE || ((CommandException) ar.exception).getCommandError() == Error.GENERIC_FAILURE)) {
                    logd("Do Nothing.");
                } else {
                    subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()] = ICCID_STRING_FOR_NO_SIM;
                    logd("Query IccId fail: " + ar.exception);
                }
                logd("mIccId[" + slotId + "] = " + printIccid(subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()]));
                if (IS_MODEM_CAPABILITY_SUPPORT) {
                    setNeedUpdateIfNeed(slotId.intValue(), subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId.intValue()]);
                }
                if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                    SubscriptionInfoUpdater subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
                    if (SubscriptionInfoUpdater.mNeedUpdate) {
                        subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                        break;
                    }
                }
                break;
            default:
                logd("Unknown msg:" + msg.what);
                break;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateIccAvailability(int slotId) {
        String str = null;
        if (mUiccController != null) {
            SubscriptionHelper subHelper = SubscriptionHelper.getInstance();
            logd("updateIccAvailability: Enter, slotId " + slotId);
            if (PROJECT_SIM_NUM <= 1 || subHelper.proceedToHandleIccEvent(slotId)) {
                CardState newState = CardState.CARDSTATE_ABSENT;
                UiccCard newCard = mUiccController.getUiccCard(slotId);
                if (newCard != null) {
                    newState = newCard.getCardState();
                    if (!IccCardStatusUtils.isCardPresent(newState) && this.isNVSubAvailable) {
                        Rlog.i(LOG_TAG, "updateIccAvailability: Returning NV mode ");
                        return;
                    }
                }
                Rlog.i(LOG_TAG, "updateIccAvailability: newCard is null, slotId " + slotId);
                if (!HwVSimUtils.isPlatformTwoModems() || HwVSimUtils.isRadioAvailable(slotId)) {
                    Rlog.i(LOG_TAG, "updateIccAvailability: not vsim pending sub");
                    return;
                }
                CardState oldState = sCardState[slotId];
                sCardState[slotId] = newState;
                logd("Slot[" + slotId + "]: New Card State = " + newState + " " + "Old Card State = " + oldState);
                SubscriptionInfoUpdater subscriptionInfoUpdater;
                SubscriptionInfoUpdater subscriptionInfoUpdater2;
                if (!IccCardStatusUtils.isCardPresent(newState)) {
                    if (!ICCID_STRING_FOR_NO_SIM.equals(subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId])) {
                        logd("SIM" + (slotId + 1) + " hot plug out");
                        subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
                        SubscriptionInfoUpdater.mNeedUpdate = DBG;
                        resetInternalOldIccId(slotId);
                    }
                    if (!IS_MODEM_CAPABILITY_SUPPORT) {
                        unRegisterForLoadIccID(slotId);
                        changeIccidForHotplug(slotId, sCardState);
                    }
                    mFh[slotId] = null;
                    subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NO_SIM;
                    if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                        subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
                        if (SubscriptionInfoUpdater.mNeedUpdate) {
                            subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                        }
                    }
                } else if (!IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState)) {
                    CharSequence iccId;
                    if (this.mChangeIccidDone && !HwVSimUtils.isPlatformRealTripple() && HwVSimUtils.isVSimOn()) {
                        subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
                        if (!SubscriptionInfoUpdater.mNeedUpdate) {
                            logd("SIM" + (slotId + 1) + " hot plug in");
                            subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = null;
                            subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
                            SubscriptionInfoUpdater.mNeedUpdate = DBG;
                            resetInternalOldIccId(slotId);
                            if (IS_MODEM_CAPABILITY_SUPPORT) {
                                queryIccId(slotId);
                            } else if (IS_QUICK_BROADCAST_STATUS || this.mCis == null || this.mCis[slotId] == null) {
                                changeIccidForHotplug(slotId, sCardState);
                                registerForLoadIccID(slotId);
                                if (newCard != null) {
                                    iccId = newCard.getIccId();
                                }
                                subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = iccId;
                                if (!TextUtils.isEmpty(iccId)) {
                                    logd("need to update subscription after fligt mode on and off..");
                                    if (HwVSimUtils.needBlockUnReservedForVsim(slotId)) {
                                        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NO_SIM;
                                        logd("the slot " + slotId + " is unreserved for vsim,just set to no_sim");
                                    }
                                    if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                                        subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
                                        if (SubscriptionInfoUpdater.mNeedUpdate) {
                                            subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                                        }
                                    }
                                }
                            } else {
                                changeIccidForHotplug(slotId, sCardState);
                                this.mCis[slotId].getICCID(this.mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                            }
                        }
                    }
                    if (subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] != null) {
                    }
                    if (IS_MODEM_CAPABILITY_SUPPORT) {
                        queryIccId(slotId);
                    } else {
                        if (IS_QUICK_BROADCAST_STATUS) {
                        }
                        changeIccidForHotplug(slotId, sCardState);
                        registerForLoadIccID(slotId);
                        if (newCard != null) {
                            iccId = newCard.getIccId();
                        }
                        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = iccId;
                        if (TextUtils.isEmpty(iccId)) {
                            logd("need to update subscription after fligt mode on and off..");
                            if (HwVSimUtils.needBlockUnReservedForVsim(slotId)) {
                                subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NO_SIM;
                                logd("the slot " + slotId + " is unreserved for vsim,just set to no_sim");
                            }
                            if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                                subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
                                if (SubscriptionInfoUpdater.mNeedUpdate) {
                                    subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                                }
                            }
                        }
                    }
                } else if (IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState) && TextUtils.isEmpty(subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId])) {
                    logd("SIM" + (slotId + 1) + " need to read iccid again in case of rild restart");
                    subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = null;
                    subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
                    SubscriptionInfoUpdater.mNeedUpdate = DBG;
                    resetInternalOldIccId(slotId);
                    if (IS_MODEM_CAPABILITY_SUPPORT) {
                        queryIccId(slotId);
                    } else if (!IS_QUICK_BROADCAST_STATUS || this.mCis == null || this.mCis[slotId] == null) {
                        registerForLoadIccID(slotId);
                        String[] iccId2 = subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater);
                        if (newCard != null) {
                            str = newCard.getIccId();
                        }
                        iccId2[slotId] = str;
                        if (subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] != null && subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId].trim().length() == 0) {
                            subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = "emptyiccid" + slotId;
                        }
                        if (HwVSimUtils.needBlockUnReservedForVsim(slotId)) {
                            subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NO_SIM;
                            logd("the slot " + slotId + " is unreserved for vsim,just set to no_sim");
                        }
                        if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                            subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
                            if (SubscriptionInfoUpdater.mNeedUpdate) {
                                subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                            }
                        }
                    } else {
                        this.mCis[slotId].getICCID(this.mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                    }
                } else if (IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState) && !subHelper.isApmSIMNotPwdn() && subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] == null) {
                    logd("SIM" + (slotId + 1) + " powered up from APM ");
                    mFh[slotId] = null;
                    subscriptionInfoUpdater2 = this.mSubscriptionInfoUpdater;
                    SubscriptionInfoUpdater.mNeedUpdate = DBG;
                    resetInternalOldIccId(slotId);
                    if (IS_MODEM_CAPABILITY_SUPPORT) {
                        queryIccId(slotId);
                    } else if (!IS_QUICK_BROADCAST_STATUS || this.mCis == null || this.mCis[slotId] == null) {
                        unRegisterForLoadIccID(slotId);
                        registerForLoadIccID(slotId);
                    } else {
                        this.mCis[slotId].getICCID(this.mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                    }
                } else if (IccCardStatusUtils.isCardPresent(oldState) && IccCardStatusUtils.isCardPresent(newState) && subHelper.needSubActivationAfterRefresh(slotId)) {
                    logd("SIM" + (slotId + 1) + " refresh happened, need sub activation");
                    if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
                        subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
                    }
                }
                return;
            }
            logd("updateIccAvailability: radio is OFF/unavailable, ignore ");
            if (!subHelper.isApmSIMNotPwdn()) {
                subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = null;
            }
        }
    }

    private void changeIccidForHotplug(int slotId, CardState[] cardState) {
        if (!IS_SINGLE_CARD_TRAY) {
            logd("changeIccidForHotplug don't for two card tray.");
        } else if (IccCardStatusUtils.isCardPresent(cardState[slotId]) || !isCardTrayOut(CARDTRAY_OUT_SLOT)) {
            logd("changeIccidForHotplug  mChangeIccidDone= " + this.mChangeIccidDone);
            if (!this.mChangeIccidDone) {
                for (i = CARDTRAY_OUT_SLOT; i < PROJECT_SIM_NUM; i++) {
                    if (i != slotId) {
                        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[i] = null;
                        r1 = this.mSubscriptionInfoUpdater;
                        SubscriptionInfoUpdater.mNeedUpdate = DBG;
                        logd("set iccid null i =  " + i);
                    }
                }
                this.mChangeIccidDone = DBG;
            }
        } else {
            logd("cardTray out set mChangeIccidDone = " + this.mChangeIccidDone);
            if (this.mChangeIccidDone) {
                this.mChangeIccidDone = IS_SINGLE_CARD_TRAY;
                i = CARDTRAY_OUT_SLOT;
                while (i < PROJECT_SIM_NUM) {
                    if (i != slotId && IccCardStatusUtils.isCardPresent(cardState[i])) {
                        r1 = this.mSubscriptionInfoUpdater;
                        SubscriptionInfoUpdater.mNeedUpdate = IS_SINGLE_CARD_TRAY;
                        logd("cardTray out set first card mNeedUpdate to false");
                    }
                    i++;
                }
            }
        }
    }

    private boolean isCardTrayOut(int SlotId) {
        Throwable th;
        boolean z = DBG;
        byte[] cardTrayState = new byte[4];
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(CARDTRAY_STATE_FILE);
            try {
                int length = fis2.read(cardTrayState, CARDTRAY_OUT_SLOT, 4);
                fis2.close();
                if (length < 4) {
                    loge("isCardTrayOut read byte fail.");
                    if (fis2 != null) {
                        try {
                            fis2.close();
                        } catch (IOException e) {
                            return IS_SINGLE_CARD_TRAY;
                        }
                    }
                    return IS_SINGLE_CARD_TRAY;
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e2) {
                        return IS_SINGLE_CARD_TRAY;
                    }
                }
                if (SlotId < 0 || SlotId > 1) {
                    return IS_SINGLE_CARD_TRAY;
                }
                if (cardTrayState[(SlotId * 2) + 1] != null) {
                    z = IS_SINGLE_CARD_TRAY;
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
                            return IS_SINGLE_CARD_TRAY;
                        }
                    }
                    return IS_SINGLE_CARD_TRAY;
                } catch (Throwable th2) {
                    th = th2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e5) {
                            return IS_SINGLE_CARD_TRAY;
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
            return IS_SINGLE_CARD_TRAY;
        }
    }

    public void queryIccId(int slotId) {
        logd("queryIccId: slotid=" + slotId);
        if (mFh[slotId] == null) {
            logd("Getting IccFileHandler");
            UiccCardApplication validApp = null;
            UiccCard uiccCard = mUiccController.getUiccCard(slotId);
            if (uiccCard != null) {
                int numApps = uiccCard.getNumApplications();
                for (int i = CARDTRAY_OUT_SLOT; i < numApps; i++) {
                    UiccCardApplication app = uiccCard.getApplicationIndex(i);
                    if (app != null && app.getType() != AppType.APPTYPE_UNKNOWN) {
                        validApp = app;
                        break;
                    }
                }
            }
            if (validApp != null) {
                mFh[slotId] = validApp.getIccFileHandler();
            }
        }
        if (mFh[slotId] != null) {
            String iccId = subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId];
            if (iccId == null) {
                logd("Querying IccId");
                mFh[slotId].loadEFTransparent(12258, this.mHandler.obtainMessage(EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId)));
                return;
            }
            logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + printIccid(iccId));
            return;
        }
        sCardState[slotId] = CardState.CARDSTATE_ABSENT;
        logd("mFh[" + slotId + "] is null, SIM not inserted");
    }

    public void resetIccid(int slotId) {
        if (slotId < 0 || slotId >= subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater).length) {
            logd("resetIccid: invaild slotid =" + slotId);
            return;
        }
        logd("resetIccid: set iccid is null");
        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = null;
    }

    public void updateSubIdForNV(int slotId) {
        subscriptionInfoUpdaterUtils.getIccId(this.mSubscriptionInfoUpdater)[slotId] = ICCID_STRING_FOR_NV;
        SubscriptionInfoUpdater subscriptionInfoUpdater = this.mSubscriptionInfoUpdater;
        SubscriptionInfoUpdater.mNeedUpdate = DBG;
        logd("[updateSubIdForNV]+ Start");
        if (subscriptionInfoUpdaterUtils.isAllIccIdQueryDone(this.mSubscriptionInfoUpdater)) {
            logd("[updateSubIdForNV]+ updating");
            subscriptionInfoUpdaterUtils.updateSubscriptionInfoByIccId(this.mSubscriptionInfoUpdater);
            this.isNVSubAvailable = DBG;
        }
    }

    public void updateSubActivation(int[] simStatus, boolean isStackReadyEvent) {
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            SubscriptionHelper.getInstance().updateNwMode();
            if (HwModemStackController.getInstance().isStackReady()) {
                SubscriptionHelper.getInstance().updateSubActivation(subscriptionInfoUpdaterUtils.getInsertSimState(this.mSubscriptionInfoUpdater), IS_SINGLE_CARD_TRAY);
                return;
            }
            return;
        }
        SubscriptionHelper.getInstance().updateSubActivation(subscriptionInfoUpdaterUtils.getInsertSimState(this.mSubscriptionInfoUpdater), IS_SINGLE_CARD_TRAY);
    }

    public void broadcastSubinfoRecordUpdated(String[] iccId, String[] oldIccId, int nNewCardCount, int nSubCount, int nNewSimStatus) {
        logd("broadcastSubinfoRecordUpdated");
        if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
            logd("setSubinfoAutoUpdateDone true");
            HwDsdsController.getInstance().setSubinfoAutoUpdateDone(DBG);
        }
        boolean hasSimRemoved = IS_SINGLE_CARD_TRAY;
        int i = CARDTRAY_OUT_SLOT;
        while (i < PROJECT_SIM_NUM) {
            if (iccId[i] != null && iccId[i].equals(ICCID_STRING_FOR_NO_SIM) && !oldIccId[i].equals(ICCID_STRING_FOR_NO_SIM)) {
                hasSimRemoved = DBG;
                break;
            }
            i++;
        }
        if (nNewCardCount == 0) {
            if (hasSimRemoved) {
                i = CARDTRAY_OUT_SLOT;
                while (i < PROJECT_SIM_NUM) {
                    if (subscriptionInfoUpdaterUtils.getInsertSimState(this.mSubscriptionInfoUpdater)[i] == -3) {
                        logd("No new SIM detected and SIM repositioned");
                        setUpdatedData(3, nSubCount, nNewSimStatus);
                        break;
                    }
                    i++;
                }
                if (i == PROJECT_SIM_NUM) {
                    logd("No new SIM detected and SIM removed");
                    setUpdatedData(2, nSubCount, nNewSimStatus);
                    return;
                }
                return;
            }
            i = CARDTRAY_OUT_SLOT;
            while (i < PROJECT_SIM_NUM) {
                if (subscriptionInfoUpdaterUtils.getInsertSimState(this.mSubscriptionInfoUpdater)[i] == -3) {
                    logd("No new SIM detected and SIM repositioned");
                    setUpdatedData(3, nSubCount, nNewSimStatus);
                    break;
                }
                i++;
            }
            if (i == PROJECT_SIM_NUM) {
                logd("[updateSimInfoByIccId] All SIM inserted into the same slot");
                setUpdatedData(4, nSubCount, nNewSimStatus);
            }
        } else if (HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isVSimOn()) {
            logd("VSim caused card status change, skip");
        } else {
            logd("New SIM detected");
            if (isNewSimCardInserted(iccId).booleanValue()) {
                setUpdatedData(1, nSubCount, nNewSimStatus);
                return;
            }
            logd("Insert Same Sim");
            setUpdatedData(5, nSubCount, nNewSimStatus);
        }
    }

    public Boolean isNewSimCardInserted(String[] sIccId) {
        Boolean result = Boolean.valueOf(IS_SINGLE_CARD_TRAY);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editor = sp.edit();
        int i = CARDTRAY_OUT_SLOT;
        while (i < PROJECT_SIM_NUM) {
            String sIccIdOld = sp.getString("SP_SUBINFO_SLOT" + i, null);
            String iccIdInSP = ICCID_STRING_FOR_NO_SIM;
            if (!(sIccIdOld == null || ICCID_STRING_FOR_NO_SIM.equals(sIccIdOld))) {
                try {
                    iccIdInSP = HwAESCryptoUtil.decrypt(MASTER_PASSWORD, sIccIdOld);
                } catch (Exception ex) {
                    logd("HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
                }
            }
            if (!(sIccId[i] == null || sIccId[i].equals(ICCID_STRING_FOR_NO_SIM) || (sIccIdOld != null && sIccId[i].equals(iccIdInSP)))) {
                result = Boolean.valueOf(DBG);
                String iccidEncrypted = ICCID_STRING_FOR_NO_SIM;
                try {
                    iccidEncrypted = HwAESCryptoUtil.encrypt(MASTER_PASSWORD, sIccId[i]);
                } catch (Exception ex2) {
                    logd("HwAESCryptoUtil encrypt excepiton:" + ex2.getMessage());
                }
                editor.putString("SP_SUBINFO_SLOT" + i, iccidEncrypted);
                editor.apply();
            }
            i++;
        }
        return result;
    }

    private static void setUpdatedData(int detectedType, int subCount, int newSimStatus) {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        logd("[setUpdatedData]+ ");
        if (detectedType == 1) {
            intent.putExtra("simDetectStatus", 1);
            intent.putExtra("simCount", subCount);
            intent.putExtra("newSIMSlot", newSimStatus);
        } else if (detectedType == 3) {
            intent.putExtra("simDetectStatus", 3);
            intent.putExtra("simCount", subCount);
        } else if (detectedType == 2) {
            intent.putExtra("simDetectStatus", 2);
            intent.putExtra("simCount", subCount);
        } else if (detectedType == 4) {
            intent.putExtra("simDetectStatus", 4);
        } else if (detectedType == 5) {
            intent.putExtra("simDetectStatus", 5);
        }
        logd("broadcast intent ACTION_SUBINFO_RECORD_UPDATED : [" + detectedType + ", " + subCount + ", " + newSimStatus + "]");
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
        logd("[setUpdatedData]- ");
    }

    private String printIccid(String iccid) {
        if (iccid == null) {
            return "null";
        }
        if (iccid.length() < 6) {
            return "less than 6 digits";
        }
        return iccid.substring(CARDTRAY_OUT_SLOT, 6) + new String(new char[(iccid.length() - 6)]).replace('\u0000', '*');
    }

    private void resetInternalOldIccId(int slotId) {
        logd("resetInternalOldIccId slotId:" + slotId);
        if (slotId >= 0 && slotId < PROJECT_SIM_NUM) {
            this.internalOldIccId[slotId] = null;
        }
    }

    public void setNeedUpdateIfNeed(int slotId, String currentIccId) {
        if (slotId >= 0 && slotId < PROJECT_SIM_NUM) {
            if (!(currentIccId == null || currentIccId.equals(this.internalOldIccId[slotId]))) {
                logd("internalOldIccId[" + slotId + "]:" + this.internalOldIccId[slotId] + " currentIccId[" + slotId + "]:" + currentIccId + " set mNeedUpdate = true");
                this.mSubscriptionInfoUpdater.setNeedUpdate(DBG);
            }
            this.internalOldIccId[slotId] = currentIccId;
        }
    }

    private void registerForLoadIccID(int slotId) {
        UiccCard uiccCard = mUiccController.getUiccCard(slotId);
        if (uiccCard != null) {
            UiccCardApplication validApp;
            UiccCardApplication app = uiccCard.getApplication(1);
            if (app != null) {
                validApp = app;
            } else {
                validApp = uiccCard.getApplication(2);
            }
            if (validApp != null) {
                IccRecords newIccRecords = validApp.getIccRecords();
                logd("SIM" + (slotId + 1) + " new : ");
                if (newIccRecords != null && (newIccRecords instanceof RuimRecords) && PhoneFactory.getPhone(slotId).getPhoneType() == 1) {
                    logd("registerForLoadIccID query iccid SIM" + (slotId + 1) + " for single mode ruim card");
                    queryIccId(slotId);
                } else if (newIccRecords != null && (mIccRecords[slotId] == null || newIccRecords != mIccRecords[slotId])) {
                    if (mIccRecords[slotId] != null) {
                        mIccRecords[slotId].unRegisterForLoadIccID(this.mHandler);
                    }
                    logd("registerForLoadIccID SIM" + (slotId + 1));
                    mIccRecords[slotId] = newIccRecords;
                    mIccRecords[slotId].registerForLoadIccID(this.mHandler, EVENT_QUERY_ICCID_DONE, Integer.valueOf(slotId));
                }
            } else {
                logd("validApp is null");
            }
        }
    }

    private void unRegisterForLoadIccID(int slotId) {
        if (mIccRecords[slotId] != null) {
            logd("unRegisterForLoadIccID SIM" + (slotId + 1));
            mIccRecords[slotId].unRegisterForLoadIccID(this.mHandler);
            mIccRecords[slotId] = null;
        }
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
