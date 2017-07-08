package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.HwModemStackController.ModemCapabilityInfo;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.HashMap;

public class HwModemBindingPolicyHandler extends Handler {
    private static final int DEFAULT_NETWORK_MODE = 0;
    private static final int EVENT_MODEM_RAT_CAPS_AVAILABLE = 1;
    private static final int EVENT_SET_NW_MODE_DONE = 3;
    private static final int EVENT_UPDATE_BINDING_DONE = 2;
    private static final int FAILURE = 0;
    private static final int INVALID_NETWORK_MODE = -1;
    private static final boolean IS_CMCC_4G_DSDX_ENABLE = false;
    private static final boolean IS_CMCC_CU_DSDX_ENABLE = false;
    private static final boolean IS_FULL_NETWORK_SUPPORTED = false;
    static final String LOG_TAG = "HwModemBindingPolicyHandler";
    private static final int NETWORK_MASK_CDMA = 12784;
    private static final int NETWORK_MASK_CDMA_NO_EVDO = 112;
    private static final int NETWORK_MASK_EVDO_NO_CDMA = 12672;
    private static final int NETWORK_MASK_GLOBAL = 114686;
    private static final int NETWORK_MASK_GSM_ONLY = 65542;
    private static final int NETWORK_MASK_GSM_UMTS = 101902;
    private static final int NETWORK_MASK_LTE_CDMA_EVDO = 29168;
    private static final int NETWORK_MASK_LTE_CMDA_EVDO_GSM_WCDMA = 131070;
    private static final int NETWORK_MASK_LTE_GSM_WCDMA = 118286;
    private static final int NETWORK_MASK_LTE_ONLY = 16384;
    private static final int NETWORK_MASK_LTE_WCDMA = 52744;
    private static final int NETWORK_MASK_TD_SCDMA_CDMA_EVDO_GSM_WCDMA = 245758;
    private static final int NETWORK_MASK_TD_SCDMA_GSM = 196614;
    private static final int NETWORK_MASK_TD_SCDMA_GSM_LTE = 212998;
    private static final int NETWORK_MASK_TD_SCDMA_GSM_WCDMA = 232974;
    private static final int NETWORK_MASK_TD_SCDMA_GSM_WCDMA_LTE = 249358;
    private static final int NETWORK_MASK_TD_SCDMA_LTE = 147456;
    private static final int NETWORK_MASK_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA = 262142;
    private static final int NETWORK_MASK_TD_SCDMA_ONLY = 131072;
    private static final int NETWORK_MASK_TD_SCDMA_WCDMA = 167432;
    private static final int NETWORK_MASK_TD_SCDMA_WCDMA_LTE = 183816;
    private static final int NETWORK_MASK_WCDMA_ONLY = 36360;
    private static final int NETWORK_MASK_WCDMA_PREF = 101902;
    public static final int PREFERNETWORK_TYPE_3G = 3;
    public static final int PREFERNETWORK_TYPE_4G = 9;
    private static final int SUCCESS = 1;
    private static final Object mLock = null;
    private static HwModemBindingPolicyHandler sHwModemBindingPolicyHandler;
    private CommandsInterface[] mCi;
    private Context mContext;
    private int[] mCurrentStackId;
    private boolean mIsSetPrefNwModeInProgress;
    private ModemCapabilityInfo[] mModemCapInfo;
    private boolean mModemRatCapabilitiesAvailable;
    private HwModemStackController mModemStackController;
    private int mNumOfSetPrefNwModeSuccess;
    private int mNumPhones;
    private int[] mNwModeinSubIdTable;
    private int[] mPrefNwMode;
    private int[] mPreferredStackId;
    private HashMap<Integer, Message> mStoredResponse;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwModemBindingPolicyHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwModemBindingPolicyHandler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwModemBindingPolicyHandler.<clinit>():void");
    }

    public static HwModemBindingPolicyHandler make(Context context, UiccController uiccMgr, CommandsInterface[] ci) {
        HwModemBindingPolicyHandler hwModemBindingPolicyHandler;
        Rlog.d(LOG_TAG, "getInstance");
        synchronized (mLock) {
            if (sHwModemBindingPolicyHandler == null) {
                sHwModemBindingPolicyHandler = new HwModemBindingPolicyHandler(context, uiccMgr, ci);
                hwModemBindingPolicyHandler = sHwModemBindingPolicyHandler;
            } else {
                throw new RuntimeException("HwModemBindingPolicyHandler.make() should be called once");
            }
        }
        return hwModemBindingPolicyHandler;
    }

    public static HwModemBindingPolicyHandler getInstance() {
        if (sHwModemBindingPolicyHandler != null) {
            return sHwModemBindingPolicyHandler;
        }
        throw new RuntimeException("ModemBindingPolicyHdlr.getInstance called before make()");
    }

    private HwModemBindingPolicyHandler(Context context, UiccController uiccManager, CommandsInterface[] ci) {
        this.mNumOfSetPrefNwModeSuccess = FAILURE;
        this.mNumPhones = TelephonyManager.getDefault().getPhoneCount();
        this.mModemRatCapabilitiesAvailable = IS_FULL_NETWORK_SUPPORTED;
        this.mIsSetPrefNwModeInProgress = IS_FULL_NETWORK_SUPPORTED;
        this.mPreferredStackId = new int[this.mNumPhones];
        this.mCurrentStackId = new int[this.mNumPhones];
        this.mPrefNwMode = new int[this.mNumPhones];
        this.mNwModeinSubIdTable = new int[this.mNumPhones];
        this.mStoredResponse = new HashMap();
        this.mModemCapInfo = null;
        logd("Constructor - Enter");
        this.mCi = ci;
        this.mContext = context;
        this.mModemStackController = HwModemStackController.getInstance();
        this.mModemCapInfo = new ModemCapabilityInfo[this.mNumPhones];
        this.mModemStackController.registerForModemRatCapsAvailable(this, SUCCESS, null);
        for (int i = FAILURE; i < this.mNumPhones; i += SUCCESS) {
            this.mPreferredStackId[i] = i;
            this.mCurrentStackId[i] = i;
            this.mNwModeinSubIdTable[i] = SUCCESS;
            this.mStoredResponse.put(Integer.valueOf(i), null);
        }
        if (!(IS_CMCC_4G_DSDX_ENABLE || IS_CMCC_CU_DSDX_ENABLE)) {
            logd("init the PreNwMode if don't support CMCC and CMCC_CU DSDX");
            switch (DEFAULT_NETWORK_MODE) {
                case HwVSimConstants.EVENT_SET_APDSFLOWCFG_DONE /*17*/:
                    System.putInt(this.mContext.getContentResolver(), "network_mode_4G_pre", 17);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_3G_pre", 16);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_2G_only", SUCCESS);
                    break;
                case HwVSimConstants.CMD_SET_APN_READY /*20*/:
                    System.putInt(this.mContext.getContentResolver(), "network_mode_4G_pre", 20);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_3G_pre", 18);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_2G_only", SUCCESS);
                    break;
                default:
                    System.putInt(this.mContext.getContentResolver(), "network_mode_4G_pre", INVALID_NETWORK_MODE);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_4G_pre", INVALID_NETWORK_MODE);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_2G_only", INVALID_NETWORK_MODE);
                    break;
            }
        }
        logd("Constructor - Exit");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SUCCESS /*1*/:
                handleModemRatCapsAvailable();
            case EVENT_UPDATE_BINDING_DONE /*2*/:
                handleUpdateBindingDone(msg.obj);
            case PREFERNETWORK_TYPE_3G /*3*/:
                handleSetPreferredNetwork(msg);
            default:
        }
    }

    private void handleSetPreferredNetwork(Message msg) {
        AsyncResult ar = msg.obj;
        int index = ((Integer) ar.userObj).intValue();
        if (ar.exception == null) {
            this.mNumOfSetPrefNwModeSuccess += SUCCESS;
            if (this.mNumOfSetPrefNwModeSuccess == this.mNumPhones) {
                for (int i = FAILURE; i < this.mNumPhones; i += SUCCESS) {
                    logd("Updating network mode in DB for slot[" + i + "] with " + this.mNwModeinSubIdTable[i]);
                    TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i, this.mNwModeinSubIdTable[i]);
                }
                this.mNumOfSetPrefNwModeSuccess = FAILURE;
                return;
            }
            return;
        }
        logd("Failed to set preferred network mode for slot" + index);
        this.mNumOfSetPrefNwModeSuccess = FAILURE;
    }

    private void handleUpdateBindingDone(AsyncResult ar) {
        this.mIsSetPrefNwModeInProgress = IS_FULL_NETWORK_SUPPORTED;
        for (int i = FAILURE; i < this.mNumPhones; i += SUCCESS) {
            int errorCode = FAILURE;
            Message resp = (Message) this.mStoredResponse.get(Integer.valueOf(i));
            if (resp != null) {
                if (ar.exception != null) {
                    errorCode = EVENT_UPDATE_BINDING_DONE;
                }
                sendResponseToTarget(resp, errorCode);
                this.mStoredResponse.put(Integer.valueOf(i), null);
            }
        }
    }

    public void updatePrefNwTypeIfRequired() {
        if (HwAllInOneController.IS_QCRIL_CROSS_MAPPING) {
            logd("CROSS_MAPPING by QCRIL_,return updatePrefNwTypeIfRequired");
            return;
        }
        int i;
        boolean updateRequired = IS_FULL_NETWORK_SUPPORTED;
        syncPreferredNwModeFromDB();
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        for (i = FAILURE; i < this.mNumPhones; i += SUCCESS) {
            int[] subIdList = subCtrlr.getSubId(i);
            if (subIdList != null && subIdList[FAILURE] >= 0) {
                int subId = subIdList[FAILURE];
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    this.mNwModeinSubIdTable[i] = subCtrlr.getNwMode(subId);
                } else {
                    this.mNwModeinSubIdTable[i] = SUCCESS;
                }
                if (this.mNwModeinSubIdTable[i] == INVALID_NETWORK_MODE) {
                    updateRequired = IS_FULL_NETWORK_SUPPORTED;
                    break;
                } else if (this.mNwModeinSubIdTable[i] != this.mPrefNwMode[i]) {
                    updateRequired = true;
                }
            }
        }
        if (updateRequired && updateStackBindingIfRequired(IS_FULL_NETWORK_SUPPORTED) == 0) {
            for (i = FAILURE; i < this.mNumPhones; i += SUCCESS) {
                this.mCi[i].setPreferredNetworkType(this.mNwModeinSubIdTable[i], obtainMessage(PREFERNETWORK_TYPE_3G, Integer.valueOf(i)));
            }
        }
    }

    private void handleModemRatCapsAvailable() {
        if (HwAllInOneController.IS_QCRIL_CROSS_MAPPING) {
            logd("CROSS MAPPING by QCRIL,return handleModemRatCapsAvailable");
            return;
        }
        this.mModemRatCapabilitiesAvailable = true;
        if (!this.mIsSetPrefNwModeInProgress) {
            this.mIsSetPrefNwModeInProgress = true;
            if (SUCCESS != updateStackBindingIfRequired(true)) {
                this.mIsSetPrefNwModeInProgress = IS_FULL_NETWORK_SUPPORTED;
            }
        }
    }

    private void syncCurrentStackInfo() {
        for (int i = FAILURE; i < this.mNumPhones; i += SUCCESS) {
            int i2;
            this.mCurrentStackId[i] = this.mModemStackController.getCurrentStackIdForPhoneId(i);
            this.mModemCapInfo[this.mCurrentStackId[i]] = this.mModemStackController.getModemRatCapsForPhoneId(i);
            int[] iArr = this.mPreferredStackId;
            if (this.mCurrentStackId[i] >= 0) {
                i2 = this.mCurrentStackId[i];
            } else {
                i2 = i;
            }
            iArr[i] = i2;
        }
    }

    private int updateStackBindingIfRequired(boolean isBootUp) {
        boolean isUpdateStackBindingRequired = IS_FULL_NETWORK_SUPPORTED;
        updatePreferredStackIds();
        for (int i = FAILURE; i < this.mNumPhones; i += SUCCESS) {
            if (this.mPreferredStackId[i] != this.mCurrentStackId[i]) {
                isUpdateStackBindingRequired = true;
                logd("isUpdateStackBindingRequired = true;");
                break;
            }
        }
        if (!isBootUp && !isUpdateStackBindingRequired) {
            return FAILURE;
        }
        int response = this.mModemStackController.updateStackBinding(this.mPreferredStackId, isBootUp, Message.obtain(this, EVENT_UPDATE_BINDING_DONE, null));
        logd("mModemStackController.updateStackBinding");
        return response;
    }

    private void updatePreferredStackIds() {
        if (this.mModemRatCapabilitiesAvailable) {
            syncPreferredNwModeFromDB();
            syncCurrentStackInfo();
            int curPhoneId = FAILURE;
            while (curPhoneId < this.mNumPhones) {
                if (isNwModeSupportedOnStack(this.mPrefNwMode[curPhoneId], this.mCurrentStackId[curPhoneId])) {
                    logd("updatePreferredStackIds: current stack[" + this.mCurrentStackId[curPhoneId] + "]supports NwMode[" + this.mPrefNwMode[curPhoneId] + "] on phoneId[" + curPhoneId + "]");
                } else {
                    int otherPhoneId = FAILURE;
                    while (otherPhoneId < this.mNumPhones) {
                        if (otherPhoneId != curPhoneId && isNwModeSupportedOnStack(this.mPrefNwMode[curPhoneId], this.mCurrentStackId[otherPhoneId]) && isNwModeSupportedOnStack(this.mPrefNwMode[otherPhoneId], this.mCurrentStackId[curPhoneId])) {
                            logd("updatePreferredStackIds: Cross Binding is possible between phoneId[" + curPhoneId + "] and phoneId[" + otherPhoneId + "]");
                            this.mPreferredStackId[curPhoneId] = this.mCurrentStackId[otherPhoneId];
                            this.mPreferredStackId[otherPhoneId] = this.mCurrentStackId[curPhoneId];
                        }
                        otherPhoneId += SUCCESS;
                    }
                }
                curPhoneId += SUCCESS;
            }
            return;
        }
        loge("updatePreferredStackIds: Modem Capabilites are not Available. Return!!");
    }

    private boolean isNwModeSupportedOnStack(int nwMode, int stackId) {
        int[] numRatSupported = new int[this.mNumPhones];
        int maxNumRatSupported = FAILURE;
        boolean isSupported = IS_FULL_NETWORK_SUPPORTED;
        int i = FAILURE;
        while (i < this.mNumPhones) {
            if (this.mModemCapInfo[i] != null) {
                numRatSupported[i] = getNumOfRatSupportedForNwMode(nwMode, this.mModemCapInfo[i]);
                if (maxNumRatSupported < numRatSupported[i]) {
                    maxNumRatSupported = numRatSupported[i];
                }
                i += SUCCESS;
            } else {
                logd("mModemInfo[" + i + "] is NULL");
                return IS_FULL_NETWORK_SUPPORTED;
            }
        }
        if (numRatSupported[stackId] == maxNumRatSupported) {
            if (HwAllInOneController.IS_CARD2_CDMA_SUPPORTED || stackId == 0 || nwMode == SUCCESS || numRatSupported[FAILURE] != maxNumRatSupported) {
                isSupported = true;
            } else {
                isSupported = IS_FULL_NETWORK_SUPPORTED;
            }
        }
        logd("nwMode:" + nwMode + ", on stack:" + stackId + " is " + (isSupported ? "Supported" : "Not Supported"));
        return isSupported;
    }

    private void syncPreferredNwModeFromDB() {
        for (int i = FAILURE; i < this.mNumPhones; i += SUCCESS) {
            try {
                this.mPrefNwMode[i] = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i);
            } catch (SettingNotFoundException e) {
                loge("getPreferredNetworkMode: Could not find PREFERRED_NETWORK_MODE!!!");
                this.mPrefNwMode[i] = Phone.PREFERRED_NT_MODE;
            }
        }
    }

    public int updateNetworkType(int networkType) {
        int networkTypeNew = networkType;
        if (IS_CMCC_4G_DSDX_ENABLE || IS_CMCC_CU_DSDX_ENABLE) {
            return networkType;
        }
        switch (DEFAULT_NETWORK_MODE) {
            case HwVSimConstants.EVENT_SET_APDSFLOWCFG_DONE /*17*/:
                if (PREFERNETWORK_TYPE_4G != networkType) {
                    if (PREFERNETWORK_TYPE_3G == networkType) {
                        networkTypeNew = 16;
                        break;
                    }
                }
                networkTypeNew = 17;
                break;
                break;
            case HwVSimConstants.CMD_SET_APN_READY /*20*/:
                if (PREFERNETWORK_TYPE_4G != networkType) {
                    if (PREFERNETWORK_TYPE_3G == networkType) {
                        networkTypeNew = 18;
                        break;
                    }
                }
                networkTypeNew = 20;
                break;
                break;
            case HwVSimConstants.CMD_GET_SIM_STATE_VIA_SYSINFOEX /*22*/:
                if (!IS_FULL_NETWORK_SUPPORTED) {
                    if (PREFERNETWORK_TYPE_4G != networkType) {
                        if (PREFERNETWORK_TYPE_3G == networkType) {
                            networkTypeNew = 21;
                            break;
                        }
                    }
                    networkTypeNew = 22;
                    break;
                }
                break;
        }
        logd("updateNetworkType: networkTypeNew = " + networkTypeNew + ", networkType = " + networkType + ", IS_FULL_NETWORK_SUPPORTED = " + IS_FULL_NETWORK_SUPPORTED + ", DEFAULT_NETWORK_MODE = " + DEFAULT_NETWORK_MODE);
        return networkTypeNew;
    }

    public void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        if (this.mIsSetPrefNwModeInProgress) {
            loge("setPreferredNetworkType: In Progress:");
            sendResponseToTarget(response, EVENT_UPDATE_BINDING_DONE);
        } else if (HwAllInOneController.IS_QCRIL_CROSS_MAPPING) {
            logd("CROSS_MAPPING by QCRIL,setPreferredNetworkType directly");
            this.mCi[phoneId].setPreferredNetworkType(networkType, response);
        } else {
            logd("setPreferredNetworkType: nwMode:" + networkType + ", on phoneId:" + phoneId);
            networkType = updateNetworkType(networkType);
            this.mIsSetPrefNwModeInProgress = true;
            try {
                if (updateStackBindingIfRequired(IS_FULL_NETWORK_SUPPORTED) == SUCCESS) {
                    this.mStoredResponse.put(Integer.valueOf(phoneId), response);
                } else {
                    this.mCi[phoneId].setPreferredNetworkType(networkType, response);
                    this.mIsSetPrefNwModeInProgress = IS_FULL_NETWORK_SUPPORTED;
                }
            } catch (Exception e) {
                loge("setPreferredNetworkType: Exception:" + e.getMessage());
                sendResponseToTarget(response, EVENT_UPDATE_BINDING_DONE);
                this.mIsSetPrefNwModeInProgress = IS_FULL_NETWORK_SUPPORTED;
            }
        }
    }

    public void restoreToPrevState() {
        this.mModemStackController.backToPrevState();
        this.mIsSetPrefNwModeInProgress = IS_FULL_NETWORK_SUPPORTED;
    }

    private void sendResponseToTarget(Message response, int responseCode) {
        if (response != null) {
            AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
            response.sendToTarget();
        }
    }

    private int getNumOfRatSupportedForNwMode(int nwMode, ModemCapabilityInfo modemCaps) {
        int supportedRatMaskForNwMode = FAILURE;
        logd("getNumOfRATsSupportedForNwMode: nwMode[" + nwMode + "] modemCaps = " + modemCaps);
        if (modemCaps == null) {
            loge("getNumOfRATsSupportedForNwMode: modemCaps = null, return 0");
            return FAILURE;
        }
        switch (nwMode) {
            case FAILURE /*0*/:
            case PREFERNETWORK_TYPE_3G /*3*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_WCDMA_PREF;
                break;
            case SUCCESS /*1*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_GSM_ONLY;
                break;
            case EVENT_UPDATE_BINDING_DONE /*2*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_WCDMA_ONLY;
                break;
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_CDMA;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_CDMA_NO_EVDO;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_EVDO_NO_CDMA;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_GLOBAL;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_ACTIVE_MODEM_MODE /*8*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_CDMA_EVDO;
                break;
            case PREFERNETWORK_TYPE_4G /*9*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_GSM_WCDMA;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_NETWORK_TYPE /*10*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_CMDA_EVDO_GSM_WCDMA;
                break;
            case HwVSimUtilsInner.VSIM /*11*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_ONLY;
                break;
            case HwVSimEventReport.VSIM_PROCESS_TYPE_DB /*12*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_WCDMA;
                break;
            case HwVSimEventReport.VSIM_PROCESS_TYPE_DC /*13*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_ONLY;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_DISABLE_VSIM_DONE /*14*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_WCDMA;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_PLMN_SELINFO /*15*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_LTE;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_NETWORK_CONNECTED /*16*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_GSM;
                break;
            case HwVSimConstants.EVENT_SET_APDSFLOWCFG_DONE /*17*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_GSM_LTE;
                break;
            case HwVSimConstants.CMD_SET_DSFLOWNVCFG /*18*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_GSM_WCDMA;
                break;
            case HwVSimConstants.EVENT_SET_DSFLOWNVCFG_DONE /*19*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_WCDMA_LTE;
                break;
            case HwVSimConstants.CMD_SET_APN_READY /*20*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_GSM_WCDMA_LTE;
                break;
            case HwVSimConstants.EVENT_SET_APN_READY_DONE /*21*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_CDMA_EVDO_GSM_WCDMA;
                break;
            case HwVSimConstants.CMD_GET_SIM_STATE_VIA_SYSINFOEX /*22*/:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA;
                break;
        }
        logd("getNumOfRATsSupportedForNwMode: supportedRatMaskForNwMode:" + supportedRatMaskForNwMode);
        return getNumRatSupportedInMask(supportedRatMaskForNwMode);
    }

    private int getNumRatSupportedInMask(int mask) {
        int noOfOnes = FAILURE;
        while (mask != 0) {
            mask &= mask + INVALID_NETWORK_MODE;
            noOfOnes += SUCCESS;
        }
        return noOfOnes;
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }
}
