package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import huawei.cust.HwCustUtils;

public class HwHotplugController extends Handler {
    private static final String DUALCARD_CLASS = "com.huawei.android.dsdscardmanager.HWCardManagerActivity";
    private static final String DUALCARD_CLASS_TAB = "com.huawei.android.dsdscardmanager.HWCardManagerTabActivity";
    private static final String DUALCARD_PACKAGE = "com.huawei.android.dsdscardmanager";
    private static final int EVENT_HOTPLUG_GET_STATE = 0;
    private static final int EVENT_HOTPLUG_PROCESS_SIM1_TIMEOUT = 1;
    private static final int EVENT_HOTPLUG_PROCESS_SIM2_TIMEOUT = 2;
    private static final boolean IS_CHINA_TELECOM = false;
    private static final boolean IS_FULL_NETWORK_SUPPORTED = false;
    public static final boolean IS_HOTSWAP_SUPPORT = false;
    private static final int SIM_NUM = 0;
    private static final int STATE_HOTPLUG_ADDED = 1;
    private static final int STATE_HOTPLUG_IDLE = 0;
    private static final int STATE_HOTPLUG_PLUGING = 3;
    private static final int STATE_HOTPLUG_QUERYING = 4;
    private static final int STATE_HOTPLUG_REMOVED = 2;
    private static final String TAG = "HwHotplugController";
    private static final int TIMEOUT_HOTPLUG_PROCESS = 15000;
    private static HwHotplugController mInstance;
    private static final Object mLock = null;
    private CardState[] mCardStates;
    private CommandsInterface[] mCis;
    private Context mContext;
    HwCustHotplugController mCustHotplugController;
    private AlertDialog mDialog;
    private int[] mHotPlugCardTypes;
    private int[] mHotPlugStates;
    private HwAllInOneController mHwAllInOneController;
    private boolean mIsNotifyIccIdChange;
    private boolean[] mIsQueryingCardTypes;
    private boolean[] mIsRestartRild;
    private RadioState[] mLastRadioStates;
    private boolean mProccessHotPlugDone;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwHotplugController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwHotplugController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwHotplugController.<clinit>():void");
    }

    public static HwHotplugController make(Context context, CommandsInterface[] ci) {
        HwHotplugController hwHotplugController;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("HwHotplugController.make() should only be called once");
            }
            mInstance = new HwHotplugController(context, ci);
            hwHotplugController = mInstance;
        }
        return hwHotplugController;
    }

    public static HwHotplugController getInstance() {
        HwHotplugController hwHotplugController;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("HwHotPlugController.getInstance can't be called before make()");
            }
            hwHotplugController = mInstance;
        }
        return hwHotplugController;
    }

    public HwHotplugController(Context c, CommandsInterface[] cis) {
        this.mIsQueryingCardTypes = new boolean[STATE_HOTPLUG_REMOVED];
        this.mIsRestartRild = new boolean[STATE_HOTPLUG_REMOVED];
        this.mHotPlugStates = new int[STATE_HOTPLUG_REMOVED];
        this.mHotPlugCardTypes = new int[STATE_HOTPLUG_REMOVED];
        this.mCardStates = new CardState[STATE_HOTPLUG_REMOVED];
        this.mLastRadioStates = new RadioState[STATE_HOTPLUG_REMOVED];
        this.mIsNotifyIccIdChange = IS_HOTSWAP_SUPPORT;
        this.mProccessHotPlugDone = true;
        this.mCustHotplugController = null;
        Rlog.d(TAG, "constructor init");
        this.mContext = c;
        this.mCis = UiccController.getInstance().getmCis();
        this.mHwAllInOneController = HwAllInOneController.getInstance();
        for (int i = STATE_HOTPLUG_IDLE; i < this.mCis.length; i += STATE_HOTPLUG_ADDED) {
            this.mHotPlugStates[i] = STATE_HOTPLUG_IDLE;
            this.mLastRadioStates[i] = RadioState.RADIO_UNAVAILABLE;
            this.mIsQueryingCardTypes[i] = IS_HOTSWAP_SUPPORT;
            this.mIsRestartRild[i] = IS_HOTSWAP_SUPPORT;
        }
        Object[] objArr = new Object[STATE_HOTPLUG_ADDED];
        objArr[STATE_HOTPLUG_IDLE] = c;
        this.mCustHotplugController = (HwCustHotplugController) HwCustUtils.createObj(HwCustHotplugController.class, objArr);
    }

    public void initHotPlugCardState(UiccCard uc, IccCardStatus status, Integer index) {
        this.mCardStates[index.intValue()] = status.mCardState;
        RadioState radioState = this.mCis[index.intValue()].getRadioState();
        this.mLastRadioStates[index.intValue()] = radioState;
        Rlog.d(TAG, "mCardStates[" + index + "] : " + radioState + ", mLastRadioStates[" + index + "] : " + this.mLastRadioStates[index.intValue()]);
    }

    public void updateHotPlugCardState(UiccCard uc, IccCardStatus status, Integer index) {
        CardState oldCardState = this.mCardStates[index.intValue()];
        this.mCardStates[index.intValue()] = status.mCardState;
        RadioState radioState = this.mCis[index.intValue()].getRadioState();
        Rlog.d(TAG, "updateHotPlugCardState SUB[" + index + "]: RadioState : " + radioState + ", mLastRadioStates : " + this.mLastRadioStates[index.intValue()]);
        Rlog.d(TAG, "updateHotPlugCardState SUB[" + index + "]: Oldcard state : " + oldCardState + ", Newcard state : " + this.mCardStates[index.intValue()]);
        if (index.intValue() == 0) {
        }
        if (oldCardState != CardState.CARDSTATE_ABSENT && this.mCardStates[index.intValue()] == CardState.CARDSTATE_ABSENT) {
            processHotPlugState(index.intValue(), IS_HOTSWAP_SUPPORT);
        } else if (oldCardState != CardState.CARDSTATE_ABSENT || this.mCardStates[index.intValue()] == CardState.CARDSTATE_ABSENT) {
            processNotHotPlugState(index.intValue());
        } else {
            processHotPlugState(index.intValue(), true);
        }
        this.mLastRadioStates[index.intValue()] = radioState;
    }

    public void onHotPlugIccStatusChanged(Integer index) {
        if (!this.mProccessHotPlugDone) {
            this.mIsQueryingCardTypes[index.intValue()] = true;
        }
    }

    public void onRestartRild() {
        if (!this.mProccessHotPlugDone) {
            for (int i = STATE_HOTPLUG_IDLE; i < this.mCis.length; i += STATE_HOTPLUG_ADDED) {
                this.mIsRestartRild[i] = true;
            }
        }
    }

    public void onHotplugIccIdChanged(String iccid, int slotId) {
        if (SIM_NUM != STATE_HOTPLUG_ADDED) {
            processMSimIccIdChange(iccid, slotId);
        }
    }

    public void onHotPlugQueryCardTypeDone(AsyncResult ar, Integer index) {
        if (!this.mProccessHotPlugDone) {
            this.mIsQueryingCardTypes[index.intValue()] = IS_HOTSWAP_SUPPORT;
        }
        if (ar != null && ar.result != null) {
            int oldHotPlugCardType = this.mHotPlugCardTypes[index.intValue()];
            this.mHotPlugCardTypes[index.intValue()] = ((int[]) ar.result)[STATE_HOTPLUG_IDLE] & 15;
            Rlog.d(TAG, "onHotPlugQueryCardTypeDone SUB[" + index + "] :" + this.mHotPlugCardTypes[index.intValue()]);
            if (IS_FULL_NETWORK_SUPPORTED) {
                onHotPlugQueryCardTypeDoneFullNetwork(oldHotPlugCardType, index);
                return;
            }
            if (IS_CHINA_TELECOM && index.intValue() == this.mHwAllInOneController.getUserSwitchDualCardSlots()) {
                onHotPlugQueryCardTypeDoneCDMA(oldHotPlugCardType, index);
            }
            if (SIM_NUM != STATE_HOTPLUG_ADDED) {
                processNotifyPromptHotPlug(IS_HOTSWAP_SUPPORT);
            }
        }
    }

    private void onHotPlugQueryCardTypeDoneCDMA(int oldHotPlugCardType, Integer index) {
        Rlog.d(TAG, "onHotPlugQueryCardTypeDoneCDMA SUB[" + index + "] : oldHotPlugCardType = " + oldHotPlugCardType + ", mHotPlugCardTypes = " + this.mHotPlugCardTypes[index.intValue()]);
        if (oldHotPlugCardType == 0 && this.mHotPlugCardTypes[index.intValue()] == STATE_HOTPLUG_ADDED) {
            processHotPlugState(index.intValue(), true);
        } else if (oldHotPlugCardType == STATE_HOTPLUG_ADDED && this.mHotPlugCardTypes[index.intValue()] == 0) {
            processHotPlugState(index.intValue(), IS_HOTSWAP_SUPPORT);
        } else if (oldHotPlugCardType == 0 && this.mHotPlugCardTypes[index.intValue()] == 0) {
            processNotHotPlugState(index.intValue());
        }
    }

    private void onHotPlugQueryCardTypeDoneFullNetwork(int oldHotPlugCardType, Integer index) {
        Rlog.d(TAG, "onHotPlugQueryCardTypeDoneFullNetwork SUB[" + index + "] : oldHotPlugCardType = " + oldHotPlugCardType + ", mHotPlugCardTypes = " + this.mHotPlugCardTypes[index.intValue()]);
        if (this.mIsRestartRild[index.intValue()]) {
            this.mIsRestartRild[index.intValue()] = IS_HOTSWAP_SUPPORT;
        }
        Rlog.d(TAG, "mIsRestartRild[0] = " + this.mIsRestartRild[STATE_HOTPLUG_IDLE] + "; mIsRestartRild[" + STATE_HOTPLUG_ADDED + "] = " + this.mIsRestartRild[STATE_HOTPLUG_ADDED]);
        if (!this.mProccessHotPlugDone && !this.mIsRestartRild[STATE_HOTPLUG_IDLE] && !this.mIsRestartRild[STATE_HOTPLUG_ADDED]) {
            processNotifyPromptHotPlug(IS_HOTSWAP_SUPPORT);
        }
    }

    public void updateHotPlugMainSlotIccId(String iccid) {
        System.putString(this.mContext.getContentResolver(), "hotplug_mainslot_iccid", iccid);
    }

    private void processGetHotPlugState(AsyncResult ar, Integer index) {
        int what;
        Rlog.d(TAG, "processGetHotPlugState : begin mHotPlugStates[" + index + "] = " + this.mHotPlugStates[index.intValue()]);
        if (index.intValue() == 0) {
            what = STATE_HOTPLUG_REMOVED;
        } else {
            what = STATE_HOTPLUG_ADDED;
        }
        if (hasMessages(what)) {
            Rlog.d(TAG, "processGetHotPlugState : has timeout message " + what + ", remove it");
            removeMessages(what);
        }
        if (this.mHotPlugStates[index.intValue()] == STATE_HOTPLUG_QUERYING) {
            if (ar == null || ar.result == null) {
                Rlog.d(TAG, "processGetHotPlugState : ar = " + ar);
                this.mHotPlugStates[index.intValue()] = STATE_HOTPLUG_IDLE;
            } else if (((int[]) ar.result)[STATE_HOTPLUG_IDLE] == STATE_HOTPLUG_ADDED) {
                this.mHotPlugStates[index.intValue()] = STATE_HOTPLUG_PLUGING;
            } else {
                this.mHotPlugStates[index.intValue()] = STATE_HOTPLUG_IDLE;
            }
            Rlog.d(TAG, "processGetHotPlugState : end mHotPlugStates[" + index + "] = " + this.mHotPlugStates[index.intValue()]);
            processNotifyPromptHotPlug(IS_HOTSWAP_SUPPORT);
        }
    }

    private void processNotHotPlugState(int index) {
        if (SIM_NUM != STATE_HOTPLUG_ADDED) {
            processNotMSimHotPlugState(index);
        }
    }

    private void processNotMSimHotPlugState(int index) {
        if (!this.mProccessHotPlugDone && this.mHotPlugStates[index] == STATE_HOTPLUG_PLUGING && !this.mIsRestartRild[STATE_HOTPLUG_IDLE] && !this.mIsRestartRild[STATE_HOTPLUG_ADDED]) {
            this.mHotPlugStates[index] = STATE_HOTPLUG_IDLE;
            processNotifyPromptHotPlug(IS_HOTSWAP_SUPPORT);
        }
    }

    private void processHotPlugState(int index, boolean isAdded) {
        if (SIM_NUM != STATE_HOTPLUG_ADDED) {
            processMSimHotPlugState(index, isAdded);
        }
    }

    private void processMSimHotPlugState(int index, boolean isAdded) {
        int what;
        if (isAdded) {
            this.mHotPlugStates[index] = STATE_HOTPLUG_ADDED;
            if (this.mHotPlugCardTypes[index] == 0) {
                this.mIsQueryingCardTypes[index] = true;
            }
        } else {
            this.mHotPlugStates[index] = STATE_HOTPLUG_REMOVED;
            this.mHotPlugCardTypes[index] = STATE_HOTPLUG_IDLE;
        }
        Rlog.d(TAG, "processMSimHotPlugState : mHotPlugStates[0] = " + this.mHotPlugStates[STATE_HOTPLUG_IDLE] + ", mHotPlugStates[1] = " + this.mHotPlugStates[STATE_HOTPLUG_ADDED]);
        if (this.mProccessHotPlugDone) {
            Rlog.d(TAG, "processMSimHotPlug --------> begin");
            this.mProccessHotPlugDone = IS_HOTSWAP_SUPPORT;
        }
        if (index == 0) {
            what = STATE_HOTPLUG_ADDED;
        } else {
            what = STATE_HOTPLUG_REMOVED;
        }
        if (hasMessages(what)) {
            Rlog.d(TAG, "processMSimHotPlugState : has timeout message " + what + ", remove it");
            removeMessages(what);
        }
        int otherIndex = index == 0 ? STATE_HOTPLUG_ADDED : STATE_HOTPLUG_IDLE;
        if (this.mHotPlugStates[otherIndex] == 0) {
            this.mHotPlugStates[otherIndex] = STATE_HOTPLUG_QUERYING;
            Rlog.d(TAG, "processMSimHotPlugState : getSimHotPlugState mHotPlugStates[" + otherIndex + "] : " + this.mHotPlugStates[otherIndex]);
            this.mCis[otherIndex].getSimHotPlugState(obtainMessage(STATE_HOTPLUG_IDLE, Integer.valueOf(otherIndex)));
            sendMessageDelayed(obtainMessage(what), 15000);
        }
    }

    public void processNotifyPromptHotPlug(boolean isTimeout) {
        if (this.mProccessHotPlugDone) {
            Rlog.d(TAG, "processNotifyPromptHotPlug : Hotplug process is complete, don't process notify.");
        } else if (IS_FULL_NETWORK_SUPPORTED && HwFullNetwork.getInstance().getNeedSwitchCommrilMode()) {
            Rlog.d(TAG, "processNotifyPromptHotPlug : Need switch comm ril mode");
        } else {
            boolean z;
            boolean hasHotPluged;
            if (HwAllInOneController.IS_HISI_DSDX) {
                if (this.mHwAllInOneController.getWaitingSwitchBalongSlot()) {
                    Rlog.d(TAG, "processNotifyPromptHotPlug : Need waitingSwitchBalongSlot");
                    return;
                } else if ("1".equals(SystemProperties.get("gsm.nvcfg.resetrild", "0"))) {
                    Rlog.d(TAG, "processNotifyPromptHotPlug : Need wait nv restart rild");
                    return;
                }
            }
            if (this.mHotPlugStates[STATE_HOTPLUG_IDLE] == STATE_HOTPLUG_PLUGING || this.mHotPlugStates[STATE_HOTPLUG_IDLE] == STATE_HOTPLUG_QUERYING || this.mHotPlugStates[STATE_HOTPLUG_ADDED] == STATE_HOTPLUG_PLUGING || this.mHotPlugStates[STATE_HOTPLUG_ADDED] == STATE_HOTPLUG_QUERYING || this.mIsQueryingCardTypes[STATE_HOTPLUG_IDLE]) {
                z = true;
            } else {
                z = this.mIsQueryingCardTypes[STATE_HOTPLUG_ADDED];
            }
            if (HwDsdsController.IS_DSDSPOWER_SUPPORT) {
                if (IS_FULL_NETWORK_SUPPORTED && (this.mIsRestartRild[STATE_HOTPLUG_IDLE] || this.mIsRestartRild[STATE_HOTPLUG_ADDED])) {
                    Rlog.d(TAG, "processNotifyPromptHotPlug : Need restart rild");
                    return;
                } else if (!z && ((this.mHotPlugStates[STATE_HOTPLUG_IDLE] == STATE_HOTPLUG_ADDED && this.mHotPlugCardTypes[STATE_HOTPLUG_IDLE] != 0) || (this.mHotPlugStates[STATE_HOTPLUG_ADDED] == STATE_HOTPLUG_ADDED && this.mHotPlugCardTypes[STATE_HOTPLUG_ADDED] != 0))) {
                    if (HwAllInOneController.IS_HISI_DSDX) {
                        UiccController tempUiccController = UiccController.getInstance();
                        if (tempUiccController == null || tempUiccController.getUiccCards() == null) {
                            Rlog.d(TAG, "haven't get all UiccCards done, please wait!");
                            return;
                        }
                        UiccCard[] uc = tempUiccController.getUiccCards();
                        for (int i = STATE_HOTPLUG_IDLE; i < uc.length; i += STATE_HOTPLUG_ADDED) {
                            if (uc[i] == null) {
                                Rlog.d(TAG, "UiccCard[" + i + "]" + "is null");
                                return;
                            }
                        }
                    }
                    HwDsdsController.getInstance().setActiveModeForHotPlug();
                }
            }
            boolean isRemovedSUB1 = IS_HOTSWAP_SUPPORT;
            boolean isRemovedSUB2 = IS_HOTSWAP_SUPPORT;
            if (IS_HOTSWAP_SUPPORT) {
                isRemovedSUB1 = this.mHotPlugStates[STATE_HOTPLUG_IDLE] == STATE_HOTPLUG_REMOVED ? true : IS_HOTSWAP_SUPPORT;
                isRemovedSUB2 = this.mHotPlugStates[STATE_HOTPLUG_ADDED] == STATE_HOTPLUG_REMOVED ? true : IS_HOTSWAP_SUPPORT;
            } else {
                if (!z && this.mHotPlugStates[STATE_HOTPLUG_IDLE] == STATE_HOTPLUG_REMOVED) {
                    this.mHotPlugStates[STATE_HOTPLUG_IDLE] = STATE_HOTPLUG_IDLE;
                }
                if (!z && this.mHotPlugStates[STATE_HOTPLUG_ADDED] == STATE_HOTPLUG_REMOVED) {
                    this.mHotPlugStates[STATE_HOTPLUG_ADDED] = STATE_HOTPLUG_IDLE;
                }
            }
            if ((this.mHotPlugStates[STATE_HOTPLUG_IDLE] != STATE_HOTPLUG_ADDED || this.mHotPlugCardTypes[STATE_HOTPLUG_IDLE] == 0) && !isRemovedSUB1 && (this.mHotPlugStates[STATE_HOTPLUG_ADDED] != STATE_HOTPLUG_ADDED || this.mHotPlugCardTypes[STATE_HOTPLUG_ADDED] == 0)) {
                hasHotPluged = isRemovedSUB2;
            } else {
                hasHotPluged = true;
            }
            boolean z2 = !z ? hasHotPluged : IS_HOTSWAP_SUPPORT;
            Rlog.d(TAG, "processNotifyPromptHotPlug : mHotPlugStates[0] = " + this.mHotPlugStates[STATE_HOTPLUG_IDLE] + ", mHotPlugStates[1] = " + this.mHotPlugStates[STATE_HOTPLUG_ADDED] + ", mIsQueryingCardTypes[0] = " + this.mIsQueryingCardTypes[STATE_HOTPLUG_IDLE] + ", mIsQueryingCardTypes[1] = " + this.mIsQueryingCardTypes[STATE_HOTPLUG_ADDED] + ", needNotify = " + z2 + ", isTimeout = " + isTimeout);
            if (z2 || isTimeout) {
                this.mHotPlugStates[STATE_HOTPLUG_IDLE] = STATE_HOTPLUG_IDLE;
                this.mHotPlugStates[STATE_HOTPLUG_ADDED] = STATE_HOTPLUG_IDLE;
                if (HwDsdsController.IS_DSDSPOWER_SUPPORT && HwDsdsController.getInstance().isProcessSetActiveModeForHotPlug()) {
                    Rlog.d(TAG, "processMSimHotPlug need wait ActiveMode done!");
                    HwDsdsController.getInstance().setNeedNotify(true);
                } else {
                    notifyMSimHotPlugPrompt();
                }
            }
            if (this.mHotPlugStates[STATE_HOTPLUG_IDLE] == 0 && this.mHotPlugStates[STATE_HOTPLUG_ADDED] == 0) {
                this.mProccessHotPlugDone = true;
                Rlog.d(TAG, "processMSimHotPlug --------> end");
            }
        }
    }

    private void processMSimIccIdChange(String iccid, int slotId) {
        if (!TextUtils.isEmpty(iccid)) {
            int mainSlot = this.mHwAllInOneController.getUserSwitchDualCardSlots();
            int secSlot = mainSlot == 0 ? STATE_HOTPLUG_ADDED : STATE_HOTPLUG_IDLE;
            String oldMainIccId = System.getString(this.mContext.getContentResolver(), "hotplug_mainslot_iccid");
            if (mainSlot == slotId && !iccid.equals(oldMainIccId)) {
                Rlog.d(TAG, "update main slot iccid change. mainSlot : " + mainSlot + ", slotId : " + slotId);
                updateHotPlugMainSlotIccId(iccid);
                if (!TextUtils.isEmpty(oldMainIccId)) {
                    this.mIsNotifyIccIdChange = true;
                    notfiyHotPlugIccIdChange(mainSlot, secSlot);
                }
            }
        }
    }

    public void notifyMSimHotPlugPrompt() {
        int mainSlot = this.mHwAllInOneController.getUserSwitchDualCardSlots();
        int secSlot = mainSlot == 0 ? STATE_HOTPLUG_ADDED : STATE_HOTPLUG_IDLE;
        Rlog.d(TAG, "notifyMSimHotPlugPrompt : mainSlot = " + mainSlot + ", secSlot = " + secSlot + ", mHotPlugCardTypes[0] = " + this.mHotPlugCardTypes[STATE_HOTPLUG_IDLE] + ", mHotPlugCardTypes[1] = " + this.mHotPlugCardTypes[STATE_HOTPLUG_ADDED]);
        if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload()) {
            Rlog.d(TAG, "vsim processHotPlug");
            HwVSimUtils.processHotPlug(this.mHotPlugCardTypes);
        } else if (IS_CHINA_TELECOM) {
            if (SystemProperties.getBoolean("persist.sys.dualcards", IS_HOTSWAP_SUPPORT)) {
                notifyMSimHotPlugPromptCDMA(mainSlot, secSlot);
            } else if (this.mHotPlugCardTypes[mainSlot] == STATE_HOTPLUG_ADDED) {
                broadcastForHwCardManager();
            }
        } else if (this.mHotPlugCardTypes[mainSlot] != 0 || this.mHotPlugCardTypes[secSlot] == 0 || HwAllInOneController.IS_HISI_DSDX) {
            notfiyHotPlugIccIdChange(mainSlot, secSlot);
        } else {
            Rlog.d(TAG, "notifyMSimHotPlugPrompt : main card need switch.");
            showHotPlugDialog(33685797);
        }
    }

    private void broadcastForHwCardManager() {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        Rlog.d(TAG, "[broadcastForHwCardManager]");
        intent.putExtra("popupDialog", "true");
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    private void notifyMSimHotPlugPromptCDMA(int mainSlot, int secSlot) {
        if (this.mHotPlugCardTypes[mainSlot] != STATE_HOTPLUG_REMOVED && this.mHotPlugCardTypes[mainSlot] != STATE_HOTPLUG_PLUGING && (this.mHotPlugCardTypes[secSlot] == STATE_HOTPLUG_REMOVED || this.mHotPlugCardTypes[secSlot] == STATE_HOTPLUG_PLUGING)) {
            Rlog.d(TAG, "notifyMSimHotPlugPromptCDMA : cdma card need switch.");
            showHotPlugDialogCDMA(34013208);
        } else if (this.mHotPlugCardTypes[mainSlot] != STATE_HOTPLUG_ADDED || this.mHotPlugCardTypes[secSlot] != 0) {
            notfiyHotPlugIccIdChange(mainSlot, secSlot);
        } else if (!HwForeignUsimForTelecom.IS_OVERSEA_USIM_SUPPORT || HwForeignUsimForTelecom.getInstance().isDomesticCard(mainSlot)) {
            Rlog.d(TAG, "notifyMSimHotPlugPromptCDMA : gsm card need switch.");
            showHotPlugDialogCDMA(34013209);
        } else {
            Rlog.d(TAG, "notifyMSimHotPlugPromptCDMA : foreign gsm card not need switch.");
        }
    }

    private void notfiyHotPlugIccIdChange(int mainSlot, int secSlot) {
        if (!this.mIsNotifyIccIdChange) {
            return;
        }
        if (this.mHotPlugStates[STATE_HOTPLUG_IDLE] == 0 && this.mHotPlugStates[STATE_HOTPLUG_ADDED] == 0) {
            this.mIsNotifyIccIdChange = IS_HOTSWAP_SUPPORT;
            if (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload()) {
                Rlog.d(TAG, "vsim is on, skip notify");
                return;
            } else if (IS_CHINA_TELECOM) {
                notifyHotPlugIccIdChangeCDMA(mainSlot, secSlot);
                return;
            } else {
                if (!(this.mHotPlugCardTypes[mainSlot] == 0 || this.mHotPlugCardTypes[secSlot] == 0 || HwAllInOneController.IS_HISI_DSDX)) {
                    showHotPlugDialog(33685798);
                }
                return;
            }
        }
        Rlog.d(TAG, "The hotplug process is not complete, wait to noify iccid change");
    }

    private void notifyHotPlugIccIdChangeCDMA(int mainSlot, int secSlot) {
        if (this.mHotPlugCardTypes[mainSlot] != STATE_HOTPLUG_REMOVED && this.mHotPlugCardTypes[mainSlot] != STATE_HOTPLUG_PLUGING) {
            return;
        }
        if ((this.mHotPlugCardTypes[secSlot] == STATE_HOTPLUG_REMOVED || this.mHotPlugCardTypes[secSlot] == STATE_HOTPLUG_PLUGING) && !HwAllInOneController.IS_HISI_DSDX) {
            showHotPlugDialog(33685798);
        }
    }

    private void showHotPlugDialog(int stringId) {
        try {
            if (!isAirplaneMode()) {
                if (this.mDialog != null) {
                    this.mDialog.dismiss();
                    this.mDialog = null;
                }
                Resources r = Resources.getSystem();
                String title = r.getString(33685790);
                String message = r.getString(stringId);
                if (this.mCustHotplugController != null) {
                    message = this.mCustHotplugController.change4GString(message);
                }
                this.mDialog = new Builder(this.mContext, 33947691).setTitle(title).setMessage(message).setPositiveButton(33685796, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("android.intent.action.MAIN");
                        if (HwHotplugController.IS_CHINA_TELECOM) {
                            intent.setClassName(HwHotplugController.DUALCARD_PACKAGE, HwHotplugController.DUALCARD_CLASS_TAB);
                        } else {
                            intent.setClassName(HwHotplugController.DUALCARD_PACKAGE, HwHotplugController.DUALCARD_CLASS);
                        }
                        intent.addFlags(805306368);
                        Rlog.d(HwHotplugController.TAG, "start HWCardManagerActivity.");
                        HwHotplugController.this.mContext.startActivity(intent);
                    }
                }).setNegativeButton(17039360, null).setCancelable(IS_HOTSWAP_SUPPORT).create();
                this.mDialog.getWindow().setType(2003);
                this.mDialog.show();
            }
        } catch (Exception e) {
            Rlog.e(TAG, "showHotPlugDialog exception: " + e);
        }
    }

    private void showHotPlugDialogCDMA(int layoutId) {
        try {
            if (!isAirplaneMode()) {
                if (this.mDialog != null) {
                    this.mDialog.dismiss();
                    this.mDialog = null;
                }
                Resources r = Resources.getSystem();
                LayoutInflater inflater = (LayoutInflater) new ContextThemeWrapper(this.mContext, r.getIdentifier("androidhwext:style/Theme.Emui", null, null)).getSystemService("layout_inflater");
                String title = r.getString(33685790);
                this.mDialog = new Builder(this.mContext, 33947691).setTitle(title).setView(inflater.inflate(layoutId, null)).setPositiveButton(33685796, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("android.intent.action.MAIN");
                        intent.setClassName(HwHotplugController.DUALCARD_PACKAGE, HwHotplugController.DUALCARD_CLASS_TAB);
                        intent.addFlags(805306368);
                        Rlog.d(HwHotplugController.TAG, "start HWCardManagerTabActivity.");
                        HwHotplugController.this.mContext.startActivity(intent);
                    }
                }).setNegativeButton(17039360, null).setCancelable(IS_HOTSWAP_SUPPORT).create();
                this.mDialog.getWindow().setType(2008);
                this.mDialog.show();
            }
        } catch (Exception e) {
            Rlog.e(TAG, "showHotPlugDialogCDMA exception: " + e);
        }
    }

    public Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(STATE_HOTPLUG_IDLE);
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

    public void handleMessage(Message msg) {
        Integer index = getCiIndex(msg);
        if (index.intValue() < 0 || index.intValue() >= this.mCis.length) {
            Rlog.e(TAG, "Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        switch (msg.what) {
            case STATE_HOTPLUG_IDLE /*0*/:
                processGetHotPlugState((AsyncResult) msg.obj, index);
                break;
            case STATE_HOTPLUG_ADDED /*1*/:
            case STATE_HOTPLUG_REMOVED /*2*/:
                processNotifyPromptHotPlug(true);
                break;
            default:
                Rlog.e(TAG, "xxxxx");
                break;
        }
    }

    private boolean isAirplaneMode() {
        return System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", STATE_HOTPLUG_IDLE) != 0 ? true : IS_HOTSWAP_SUPPORT;
    }
}
