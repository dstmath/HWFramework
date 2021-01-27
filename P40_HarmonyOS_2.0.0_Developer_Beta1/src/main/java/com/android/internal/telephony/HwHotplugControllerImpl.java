package com.android.internal.telephony;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfigInner;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.app.ActivityManagerNativeEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.utils.HwPartResourceUtils;
import huawei.cust.HwCustUtils;

public class HwHotplugControllerImpl extends DefaultHwHotplugController {
    private static final int EVENT_HOTPLUG_GET_STATE = 0;
    private static final int EVENT_HOTPLUG_PROCESS_SIM1_TIMEOUT = 1;
    private static final int EVENT_HOTPLUG_PROCESS_SIM2_TIMEOUT = 2;
    private static final boolean IS_CHINA_TELECOM = HuaweiTelephonyConfigs.isChinaTelecom();
    private static final boolean IS_FULL_NETWORK_SUPPORTED = HwFullNetworkConfigInner.IS_FULL_NETWORK_SUPPORTED;
    private static final String KEY_HOTPLUG_MAINSLOT_ICCID = "hotplug_mainslot_iccid";
    private static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final int STATE_HOTPLUG_ADDED = 1;
    private static final int STATE_HOTPLUG_IDLE = 0;
    private static final int STATE_HOTPLUG_PLUGING = 3;
    private static final int STATE_HOTPLUG_QUERYING = 4;
    private static final int STATE_HOTPLUG_REMOVED = 2;
    private static final String TAG = "HwHotplugController";
    private static final int TIMEOUT_HOTPLUG_PROCESS = 15000;
    private static boolean isFactroyMode = "factory".equals(SystemPropertiesEx.get("ro.runmode", "normal"));
    private static HwHotplugControllerImpl sInstance;
    private static final Object sLock = new Object();
    private String dualCardClass;
    private String dualCardClassTag;
    private String dualCardPackage;
    private IccCardStatusExt.CardStateEx[] mCardStates = new IccCardStatusExt.CardStateEx[2];
    private CommandsInterfaceEx[] mCis;
    private Context mContext;
    HwCustHotplugController mCustHotplugController = null;
    private AlertDialog mDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.internal.telephony.HwHotplugControllerImpl.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int index = HwHotplugControllerImpl.this.getCiIndex(msg).intValue();
            if (index < 0 || index >= HwHotplugControllerImpl.this.mCis.length) {
                RlogEx.e(HwHotplugControllerImpl.TAG, "Invalid index : " + index + " received with event " + msg.what);
                return;
            }
            int i = msg.what;
            if (i == 0) {
                HwHotplugControllerImpl.this.processGetHotPlugState(AsyncResultEx.from(msg.obj), Integer.valueOf(index));
            } else if (i == 1 || i == 2) {
                HwHotplugControllerImpl.this.processNotifyPromptHotPlug(true);
            } else {
                RlogEx.e(HwHotplugControllerImpl.TAG, "invalid message.");
            }
        }
    };
    private int[] mHotPlugCardTypes = new int[2];
    private int[] mHotPlugStates = new int[2];
    private boolean mIsNotifyIccIdChange = false;
    private boolean[] mIsQueryingCardTypes = new boolean[2];
    private boolean[] mIsRestartRild = new boolean[2];
    private int[] mLastRadioStates = new int[2];
    private boolean mProccessHotPlugDone = true;

    public HwHotplugControllerImpl(Context c, CommandsInterfaceEx[] cis) {
        RlogEx.i(TAG, "constructor init");
        this.mContext = c;
        this.mCis = cis;
        for (int i = 0; i < this.mCis.length; i++) {
            this.mHotPlugStates[i] = 0;
            this.mLastRadioStates[i] = 2;
            this.mIsQueryingCardTypes[i] = false;
            this.mIsRestartRild[i] = false;
        }
        this.mCustHotplugController = (HwCustHotplugController) HwCustUtils.createObj(HwCustHotplugController.class, new Object[]{c});
        setDualcardPackage();
    }

    public static HwHotplugControllerImpl make(Context context, CommandsInterfaceEx[] ci) {
        HwHotplugControllerImpl hwHotplugControllerImpl;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new HwHotplugControllerImpl(context, ci);
                hwHotplugControllerImpl = sInstance;
            } else {
                throw new RuntimeException("HwHotplugController.make() should only be called once");
            }
        }
        return hwHotplugControllerImpl;
    }

    public static HwHotplugControllerImpl getInstance() {
        HwHotplugControllerImpl hwHotplugControllerImpl;
        synchronized (sLock) {
            if (sInstance != null) {
                hwHotplugControllerImpl = sInstance;
            } else {
                throw new RuntimeException("HwHotPlugController.getInstance can't be called before make()");
            }
        }
        return hwHotplugControllerImpl;
    }

    public void initHotPlugCardState(IccCardStatusExt status, Integer index) {
        if (status == null || !SubscriptionManagerEx.isValidSlotIndex(index.intValue())) {
            RlogEx.e(TAG, "initHotPlugCardState param is invalid, return!");
            return;
        }
        this.mCardStates[index.intValue()] = status.getCardState();
        int radioState = this.mCis[index.intValue()].getRadioState();
        this.mLastRadioStates[index.intValue()] = radioState;
        RlogEx.i(TAG, "mCardStates[" + index + "] : " + radioState + ", mLastRadioStates[" + index + "] : " + this.mLastRadioStates[index.intValue()]);
    }

    public void updateHotPlugCardState(IccCardStatusExt status, Integer index) {
        if (status == null || !SubscriptionManagerEx.isValidSlotIndex(index.intValue())) {
            RlogEx.e(TAG, "updateHotPlugCardState param is invalid, return!");
            return;
        }
        IccCardStatusExt.CardStateEx oldCardState = this.mCardStates[index.intValue()];
        this.mCardStates[index.intValue()] = status.getCardState();
        int radioState = this.mCis[index.intValue()].getRadioState();
        RlogEx.i(TAG, "updateHotPlugCardState SUB[" + index + "]: RadioState : " + radioState + ", mLastRadioStates : " + this.mLastRadioStates[index.intValue()]);
        RlogEx.i(TAG, "updateHotPlugCardState SUB[" + index + "]: Oldcard state : " + oldCardState + ", Newcard state : " + this.mCardStates[index.intValue()]);
        if (index.intValue() == 0) {
        }
        if (oldCardState != IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT && this.mCardStates[index.intValue()] == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
            processHotPlugState(index.intValue(), false);
        } else if (oldCardState != IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT || this.mCardStates[index.intValue()] == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
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
            for (int i = 0; i < this.mCis.length; i++) {
                this.mIsRestartRild[i] = true;
            }
        }
    }

    public void onHotplugIccIdChanged(String iccid, int slotId) {
        if (SIM_NUM != 1) {
            processMSimIccIdChange(iccid, slotId);
        }
    }

    public void onHotPlugQueryCardTypeDone(AsyncResultEx ar, Integer index) {
        if (!this.mProccessHotPlugDone) {
            this.mIsQueryingCardTypes[index.intValue()] = false;
        }
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimEnabled() && HwVSimUtils.isPlatformTwoModems()) {
            int notReservedCard = index.intValue() == 0 ? 1 : 0;
            this.mIsQueryingCardTypes[notReservedCard] = false;
            this.mHotPlugCardTypes[notReservedCard] = 0;
            RlogEx.i(TAG, "onHotPlugQueryCardTypeDone SUB[" + notReservedCard + "] : change to no-sim.");
        }
        if (ar != null && ar.getResult() != null) {
            int oldHotPlugCardType = this.mHotPlugCardTypes[index.intValue()];
            this.mHotPlugCardTypes[index.intValue()] = ((int[]) ar.getResult())[0] & 15;
            RlogEx.i(TAG, "onHotPlugQueryCardTypeDone SUB[" + index + "] :" + this.mHotPlugCardTypes[index.intValue()]);
            if (IS_FULL_NETWORK_SUPPORTED) {
                onHotPlugQueryCardTypeDoneFullNetwork(oldHotPlugCardType, index);
                return;
            }
            if (IS_CHINA_TELECOM && index.intValue() == HwFullNetworkManager.getInstance().getUserSwitchDualCardSlots()) {
                onHotPlugQueryCardTypeDoneCDMA(oldHotPlugCardType, index);
            }
            if (SIM_NUM != 1) {
                processNotifyPromptHotPlug(false);
            }
        }
    }

    private void onHotPlugQueryCardTypeDoneCDMA(int oldHotPlugCardType, Integer index) {
        RlogEx.i(TAG, "onHotPlugQueryCardTypeDoneCDMA SUB[" + index + "] : oldHotPlugCardType = " + oldHotPlugCardType + ", mHotPlugCardTypes = " + this.mHotPlugCardTypes[index.intValue()]);
        if (oldHotPlugCardType == 0 && this.mHotPlugCardTypes[index.intValue()] == 1) {
            processHotPlugState(index.intValue(), true);
        } else if (oldHotPlugCardType == 1 && this.mHotPlugCardTypes[index.intValue()] == 0) {
            processHotPlugState(index.intValue(), false);
        } else if (oldHotPlugCardType == 0 && this.mHotPlugCardTypes[index.intValue()] == 0) {
            processNotHotPlugState(index.intValue());
        } else {
            RlogEx.i(TAG, "onHotPlugQueryCardTypeDoneCDMA do nothing");
        }
    }

    private void onHotPlugQueryCardTypeDoneFullNetwork(int oldHotPlugCardType, Integer index) {
        RlogEx.i(TAG, "onHotPlugQueryCardTypeDoneFullNetwork SUB[" + index + "] : oldHotPlugCardType = " + oldHotPlugCardType + ", mHotPlugCardTypes = " + this.mHotPlugCardTypes[index.intValue()]);
        if (this.mIsRestartRild[index.intValue()]) {
            this.mIsRestartRild[index.intValue()] = false;
        }
        RlogEx.i(TAG, "mIsRestartRild[0] = " + this.mIsRestartRild[0] + "; mIsRestartRild[1] = " + this.mIsRestartRild[1]);
        if (!this.mProccessHotPlugDone) {
            boolean[] zArr = this.mIsRestartRild;
            if (!zArr[0] && !zArr[1]) {
                processNotifyPromptHotPlug(false);
            }
        }
    }

    public void updateHotPlugMainSlotIccId(String iccId) {
        try {
            String iccId2 = HwAESCryptoUtil.encrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, iccId);
            if (!TextUtils.isEmpty(Settings.System.getString(this.mContext.getContentResolver(), KEY_HOTPLUG_MAINSLOT_ICCID))) {
                Settings.System.putString(this.mContext.getContentResolver(), KEY_HOTPLUG_MAINSLOT_ICCID, null);
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            editor.putString(KEY_HOTPLUG_MAINSLOT_ICCID, iccId2);
            editor.apply();
        } catch (IllegalArgumentException e) {
            RlogEx.i(TAG, "updateHotPlugMainSlotIccId encrypt IllegalArgumentException.");
        } catch (Exception e2) {
            RlogEx.i(TAG, "updateHotPlugMainSlotIccId encrypt excepiton.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processGetHotPlugState(AsyncResultEx ar, Integer index) {
        int what;
        RlogEx.i(TAG, "processGetHotPlugState : begin mHotPlugStates[" + index + "] = " + this.mHotPlugStates[index.intValue()]);
        if (index.intValue() == 0) {
            what = 2;
        } else {
            what = 1;
        }
        if (this.mHandler.hasMessages(what)) {
            RlogEx.i(TAG, "processGetHotPlugState : has timeout message " + what + ", remove it");
            this.mHandler.removeMessages(what);
        }
        if (this.mHotPlugStates[index.intValue()] == 4) {
            if (ar == null || ar.getResult() == null) {
                RlogEx.i(TAG, "processGetHotPlugState : ar = " + ar);
                this.mHotPlugStates[index.intValue()] = 0;
            } else if (((int[]) ar.getResult())[0] == 1) {
                this.mHotPlugStates[index.intValue()] = 3;
            } else {
                this.mHotPlugStates[index.intValue()] = 0;
            }
            RlogEx.i(TAG, "processGetHotPlugState : end mHotPlugStates[" + index + "] = " + this.mHotPlugStates[index.intValue()]);
            processNotifyPromptHotPlug(false);
        }
    }

    private void processNotHotPlugState(int index) {
        if (SIM_NUM != 1) {
            processNotMSimHotPlugState(index);
        }
    }

    private void processNotMSimHotPlugState(int index) {
        if (!this.mProccessHotPlugDone) {
            int[] iArr = this.mHotPlugStates;
            if (iArr[index] == 3) {
                boolean[] zArr = this.mIsRestartRild;
                if (!zArr[0] && !zArr[1]) {
                    iArr[index] = 0;
                    processNotifyPromptHotPlug(false);
                }
            }
        }
    }

    private void processHotPlugState(int index, boolean isAdded) {
        if (SIM_NUM != 1) {
            processMSimHotPlugState(index, isAdded);
        }
    }

    private void processMSimHotPlugState(int index, boolean isAdded) {
        int what;
        int otherIndex = 1;
        if (isAdded) {
            this.mHotPlugStates[index] = 1;
            if (this.mHotPlugCardTypes[index] == 0) {
                this.mIsQueryingCardTypes[index] = true;
            }
        } else {
            this.mHotPlugStates[index] = 2;
            this.mHotPlugCardTypes[index] = 0;
        }
        RlogEx.i(TAG, "processMSimHotPlugState : mHotPlugStates[0] = " + this.mHotPlugStates[0] + ", mHotPlugStates[1] = " + this.mHotPlugStates[1]);
        if (this.mProccessHotPlugDone) {
            RlogEx.i(TAG, "processMSimHotPlug --------> begin");
            this.mProccessHotPlugDone = false;
        }
        if (index == 0) {
            what = 1;
        } else {
            what = 2;
        }
        if (this.mHandler.hasMessages(what)) {
            RlogEx.i(TAG, "processMSimHotPlugState : has timeout message " + what + ", remove it");
            this.mHandler.removeMessages(what);
        }
        if (index != 0) {
            otherIndex = 0;
        }
        int[] iArr = this.mHotPlugStates;
        if (iArr[otherIndex] == 0) {
            iArr[otherIndex] = 4;
            RlogEx.i(TAG, "processMSimHotPlugState : getSimHotPlugState mHotPlugStates[" + otherIndex + "] : " + this.mHotPlugStates[otherIndex]);
            this.mCis[otherIndex].getSimHotPlugState(this.mHandler.obtainMessage(0, Integer.valueOf(otherIndex)));
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(what), 15000);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0067  */
    public void processNotifyPromptHotPlug(boolean isTimeout) {
        boolean needWaitNotify;
        int[] iArr;
        if (this.mProccessHotPlugDone) {
            RlogEx.i(TAG, "processNotifyPromptHotPlug : Hotplug process is complete, don't process notify.");
            return;
        }
        if (HwFullNetworkConfigInner.IS_HISI_DSDX) {
            if (HwFullNetworkManager.getInstance().getWaitingSwitchBalongSlot()) {
                RlogEx.i(TAG, "processNotifyPromptHotPlug : Need waitingSwitchBalongSlot");
                return;
            } else if ("1".equals(SystemPropertiesEx.get("gsm.nvcfg.resetrild", "0"))) {
                RlogEx.i(TAG, "processNotifyPromptHotPlug : Need wait nv restart rild");
                return;
            }
        }
        int[] iArr2 = this.mHotPlugStates;
        if (!(iArr2[0] == 3 || iArr2[0] == 4 || iArr2[1] == 3 || iArr2[1] == 4)) {
            boolean[] zArr = this.mIsQueryingCardTypes;
            if (!zArr[0] && !zArr[1]) {
                needWaitNotify = false;
                if (!needWaitNotify) {
                    int[] iArr3 = this.mHotPlugStates;
                    if (iArr3[0] == 2) {
                        iArr3[0] = 0;
                    }
                }
                if (!needWaitNotify) {
                    int[] iArr4 = this.mHotPlugStates;
                    if (iArr4[1] == 2) {
                        iArr4[1] = 0;
                    }
                }
                boolean needNotify = needWaitNotify && ((this.mHotPlugStates[0] != 1 && this.mHotPlugCardTypes[0] != 0) || (this.mHotPlugStates[1] == 1 && this.mHotPlugCardTypes[1] != 0));
                RlogEx.i(TAG, "processNotifyPromptHotPlug : mHotPlugStates[0] = " + this.mHotPlugStates[0] + ", mHotPlugStates[1] = " + this.mHotPlugStates[1] + ", mIsQueryingCardTypes[0] = " + this.mIsQueryingCardTypes[0] + ", mIsQueryingCardTypes[1] = " + this.mIsQueryingCardTypes[1] + ", needNotify = " + needNotify + ", isTimeout = " + isTimeout);
                if (needNotify || isTimeout) {
                    int[] iArr5 = this.mHotPlugStates;
                    iArr5[0] = 0;
                    iArr5[1] = 0;
                    notifyMSimHotPlugPrompt();
                }
                iArr = this.mHotPlugStates;
                if (iArr[0] == 0 && iArr[1] == 0) {
                    this.mProccessHotPlugDone = true;
                    RlogEx.i(TAG, "processMSimHotPlug --------> end");
                    return;
                }
            }
        }
        needWaitNotify = true;
        if (!needWaitNotify) {
        }
        if (!needWaitNotify) {
        }
        if (needWaitNotify) {
        }
        RlogEx.i(TAG, "processNotifyPromptHotPlug : mHotPlugStates[0] = " + this.mHotPlugStates[0] + ", mHotPlugStates[1] = " + this.mHotPlugStates[1] + ", mIsQueryingCardTypes[0] = " + this.mIsQueryingCardTypes[0] + ", mIsQueryingCardTypes[1] = " + this.mIsQueryingCardTypes[1] + ", needNotify = " + needNotify + ", isTimeout = " + isTimeout);
        int[] iArr52 = this.mHotPlugStates;
        iArr52[0] = 0;
        iArr52[1] = 0;
        notifyMSimHotPlugPrompt();
        iArr = this.mHotPlugStates;
        if (iArr[0] == 0) {
        }
    }

    private void processMSimIccIdChange(String iccid, int slotId) {
        if (!TextUtils.isEmpty(iccid)) {
            int mainSlot = HwFullNetworkManager.getInstance().getUserSwitchDualCardSlots();
            int secSlot = mainSlot == 0 ? 1 : 0;
            String oldMainIccId = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(KEY_HOTPLUG_MAINSLOT_ICCID, null);
            try {
                oldMainIccId = HwAESCryptoUtil.decrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, oldMainIccId);
            } catch (IllegalArgumentException e) {
                RlogEx.i(TAG, "processMSimIccIdChange decrypt IllegalArgumentException iccid.");
            } catch (Exception e2) {
                RlogEx.i(TAG, "processMSimIccIdChange decrypt excepiton.");
            }
            if (mainSlot == slotId && !iccid.equals(oldMainIccId)) {
                RlogEx.i(TAG, "update main slot iccid change. mainSlot : " + mainSlot + ", slotId : " + slotId);
                updateHotPlugMainSlotIccId(iccid);
                if (!TextUtils.isEmpty(oldMainIccId)) {
                    this.mIsNotifyIccIdChange = true;
                    notfiyHotPlugIccIdChange(mainSlot, secSlot);
                }
            }
        }
    }

    public void notifyMSimHotPlugPrompt() {
        int mainSlot = HwFullNetworkManager.getInstance().getUserSwitchDualCardSlots();
        int secSlot = mainSlot == 0 ? 1 : 0;
        RlogEx.i(TAG, "notifyMSimHotPlugPrompt : mainSlot = " + mainSlot + ", secSlot = " + secSlot + ", mHotPlugCardTypes[0] = " + this.mHotPlugCardTypes[0] + ", mHotPlugCardTypes[1] = " + this.mHotPlugCardTypes[1]);
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload())) {
            RlogEx.i(TAG, "vsim processHotPlug");
            HwVSimUtils.processHotPlug(this.mHotPlugCardTypes);
        } else if (!IS_CHINA_TELECOM) {
            int[] iArr = this.mHotPlugCardTypes;
            if (iArr[mainSlot] != 0 || iArr[secSlot] == 0 || HwFullNetworkConfigInner.IS_HISI_DSDX) {
                notfiyHotPlugIccIdChange(mainSlot, secSlot);
                return;
            }
            RlogEx.i(TAG, "notifyMSimHotPlugPrompt : main card need switch.");
            showHotPlugDialog(33685804);
        } else if (SystemPropertiesEx.getBoolean("persist.sys.dualcards", false)) {
            notifyMSimHotPlugPromptCDMA(mainSlot, secSlot);
        } else if (this.mHotPlugCardTypes[mainSlot] == 1) {
            broadcastForHwCardManager();
        }
    }

    private void broadcastForHwCardManager() {
        RlogEx.i(TAG, "[broadcastForHwCardManager]");
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intent.putExtra("popupDialog", "true");
        ActivityManagerNativeEx.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    private void notifyMSimHotPlugPromptCDMA(int mainSlot, int secSlot) {
        int[] iArr = this.mHotPlugCardTypes;
        if (iArr[mainSlot] == 2 || iArr[mainSlot] == 3 || !(iArr[secSlot] == 2 || iArr[secSlot] == 3)) {
            int[] iArr2 = this.mHotPlugCardTypes;
            if (iArr2[mainSlot] == 1 && iArr2[secSlot] == 0) {
                RlogEx.i(TAG, "notifyMSimHotPlugPromptCDMA : gsm card need switch.");
                showHotPlugDialogCDMA(34013209);
                return;
            }
            RlogEx.i(TAG, "notifyMSimHotPlugPromptCDMA : do nothing.");
            notfiyHotPlugIccIdChange(mainSlot, secSlot);
            return;
        }
        RlogEx.i(TAG, "notifyMSimHotPlugPromptCDMA : cdma card need switch.");
        showHotPlugDialogCDMA(34013208);
    }

    private void notfiyHotPlugIccIdChange(int mainSlot, int secSlot) {
        if (this.mIsNotifyIccIdChange) {
            int[] iArr = this.mHotPlugStates;
            if (iArr[0] == 0 && iArr[1] == 0) {
                this.mIsNotifyIccIdChange = false;
                if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload())) {
                    RlogEx.i(TAG, "vsim is on, skip notify");
                } else if (IS_CHINA_TELECOM) {
                    notifyHotPlugIccIdChangeCDMA(mainSlot, secSlot);
                } else {
                    int[] iArr2 = this.mHotPlugCardTypes;
                    if (iArr2[mainSlot] != 0 && iArr2[secSlot] != 0 && !HwFullNetworkConfigInner.IS_HISI_DSDX) {
                        showHotPlugDialog(33685805);
                    }
                }
            } else {
                RlogEx.i(TAG, "The hotplug process is not complete, wait to noify iccid change");
            }
        }
    }

    private void notifyHotPlugIccIdChangeCDMA(int mainSlot, int secSlot) {
        int[] iArr = this.mHotPlugCardTypes;
        if (iArr[mainSlot] == 2 || iArr[mainSlot] == 3) {
            int[] iArr2 = this.mHotPlugCardTypes;
            if ((iArr2[secSlot] == 2 || iArr2[secSlot] == 3) && !HwFullNetworkConfigInner.IS_HISI_DSDX) {
                showHotPlugDialog(33685805);
            }
        }
    }

    private void showHotPlugDialog(int stringId) {
        try {
            if (!isAirplaneMode()) {
                if (isFactroyMode) {
                    RlogEx.i(TAG, "showHotPlugDialog:don't show dialog in factory mode");
                    return;
                }
                if (this.mDialog != null) {
                    this.mDialog.dismiss();
                    this.mDialog = null;
                }
                Resources r = Resources.getSystem();
                String title = r.getString(33685797);
                String message = r.getString(stringId);
                if (this.mCustHotplugController != null) {
                    message = this.mCustHotplugController.change4GString(message);
                }
                this.mDialog = new AlertDialog.Builder(this.mContext, 33947691).setTitle(title).setMessage(message).setPositiveButton(33685803, new DialogInterface.OnClickListener() {
                    /* class com.android.internal.telephony.HwHotplugControllerImpl.AnonymousClass2 */

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("android.intent.action.MAIN");
                        if (HwHotplugControllerImpl.IS_CHINA_TELECOM) {
                            intent.setClassName(HwHotplugControllerImpl.this.dualCardPackage, HwHotplugControllerImpl.this.dualCardClassTag);
                        } else {
                            intent.setClassName(HwHotplugControllerImpl.this.dualCardPackage, HwHotplugControllerImpl.this.dualCardClass);
                        }
                        intent.addFlags(805306368);
                        RlogEx.i(HwHotplugControllerImpl.TAG, "start HWCardManagerActivity.");
                        HwHotplugControllerImpl.this.mContext.startActivity(intent);
                    }
                }).setNegativeButton(HwPartResourceUtils.getResourceId("cancel"), (DialogInterface.OnClickListener) null).setCancelable(false).create();
                this.mDialog.getWindow().setType(HwFullNetworkConstantsInner.EVENT_GET_PREF_NETWORK_MODE_DONE);
                this.mDialog.show();
            }
        } catch (Exception e) {
            RlogEx.e(TAG, "showHotPlugDialog exception");
        }
    }

    private void showHotPlugDialogCDMA(int layoutId) {
        try {
            if (!isAirplaneMode()) {
                if (isFactroyMode) {
                    RlogEx.i(TAG, "showHotPlugDialogCDMA:don't show dialog in factory mode");
                    return;
                }
                if (this.mDialog != null) {
                    this.mDialog.dismiss();
                    this.mDialog = null;
                }
                Resources r = Resources.getSystem();
                LayoutInflater inflater = (LayoutInflater) new ContextThemeWrapper(this.mContext, r.getIdentifier("androidhwext:style/Theme.Emui", null, null)).getSystemService("layout_inflater");
                if (inflater != null) {
                    String title = r.getString(33685797);
                    this.mDialog = new AlertDialog.Builder(this.mContext, 33947691).setTitle(title).setView(inflater.inflate(layoutId, (ViewGroup) null)).setPositiveButton(33685803, new DialogInterface.OnClickListener() {
                        /* class com.android.internal.telephony.HwHotplugControllerImpl.AnonymousClass3 */

                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent("android.intent.action.MAIN");
                            intent.setClassName(HwHotplugControllerImpl.this.dualCardPackage, HwHotplugControllerImpl.this.dualCardClassTag);
                            intent.addFlags(805306368);
                            RlogEx.i(HwHotplugControllerImpl.TAG, "start HWCardManagerTabActivity.");
                            HwHotplugControllerImpl.this.mContext.startActivity(intent);
                        }
                    }).setNegativeButton(HwPartResourceUtils.getResourceId("cancel"), (DialogInterface.OnClickListener) null).setCancelable(false).create();
                    this.mDialog.getWindow().setType(HwFullNetworkConstantsInner.EVENT_RESET_OOS_FLAG);
                    this.mDialog.show();
                }
            }
        } catch (Exception e) {
            RlogEx.e(TAG, "showHotPlugDialogCDMA exception");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Integer getCiIndex(Message msg) {
        if (msg == null) {
            return 0;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (AsyncResultEx.from(msg.obj) != null) {
            AsyncResultEx arEx = AsyncResultEx.from(msg.obj);
            if (arEx.getUserObj() == null || !(arEx.getUserObj() instanceof Integer)) {
                return 0;
            }
            return (Integer) arEx.getUserObj();
        }
        RlogEx.i(TAG, "invalid index, use default");
        return 0;
    }

    private boolean isAirplaneMode() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    private void setDualcardPackage() {
        try {
            PackageManager pm = this.mContext.getPackageManager();
            if (pm != null) {
                pm.getPackageInfo("com.huawei.dsdscardmanager", 1);
                this.dualCardPackage = "com.huawei.dsdscardmanager";
                this.dualCardClass = "com.huawei.dsdscardmanager.HWCardManagerActivity";
                this.dualCardClassTag = "com.huawei.dsdscardmanager.HWCardManagerTabActivity";
            }
        } catch (PackageManager.NameNotFoundException e) {
            RlogEx.i(TAG, "setDualcardPackage use old dualcard package.");
            this.dualCardPackage = "com.huawei.android.dsdscardmanager";
            this.dualCardClass = "com.huawei.android.dsdscardmanager.HWCardManagerActivity";
            this.dualCardClassTag = "com.huawei.android.dsdscardmanager.HWCardManagerTabActivity";
        }
    }
}
