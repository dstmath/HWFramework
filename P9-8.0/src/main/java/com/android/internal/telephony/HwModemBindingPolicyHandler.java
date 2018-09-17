package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.HwModemStackController.ModemCapabilityInfo;
import com.android.internal.telephony.uicc.UiccController;
import java.util.HashMap;

public class HwModemBindingPolicyHandler extends Handler {
    private static final int DEFAULT_NETWORK_MODE = SystemProperties.getInt("ro.telephony.default_network", -1);
    private static final int EVENT_MODEM_RAT_CAPS_AVAILABLE = 1;
    private static final int EVENT_SET_NW_MODE_DONE = 3;
    private static final int EVENT_UPDATE_BINDING_DONE = 2;
    private static final int FAILURE = 0;
    private static final int INVALID_NETWORK_MODE = -1;
    private static final boolean IS_CMCC_4G_DSDX_ENABLE = SystemProperties.getBoolean("ro.hwpp.cmcc_4G_dsdx_enable", false);
    private static final boolean IS_CMCC_CU_DSDX_ENABLE = SystemProperties.getBoolean("ro.hwpp.cmcc_cu_dsdx_enable", false);
    private static final boolean IS_FULL_NETWORK_SUPPORTED = SystemProperties.getBoolean("ro.config.full_network_support", false);
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
    private static final Object mLock = new Object();
    private static HwModemBindingPolicyHandler sHwModemBindingPolicyHandler;
    private CommandsInterface[] mCi;
    private Context mContext;
    private int[] mCurrentStackId = new int[this.mNumPhones];
    private boolean mIsSetPrefNwModeInProgress = false;
    private ModemCapabilityInfo[] mModemCapInfo = null;
    private boolean mModemRatCapabilitiesAvailable = false;
    private HwModemStackController mModemStackController;
    private int mNumOfSetPrefNwModeSuccess = 0;
    private int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private int[] mNwModeinSubIdTable = new int[this.mNumPhones];
    private int[] mPrefNwMode = new int[this.mNumPhones];
    private int[] mPreferredStackId = new int[this.mNumPhones];
    private HashMap<Integer, Message> mStoredResponse = new HashMap();

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
        logd("Constructor - Enter");
        this.mCi = ci;
        this.mContext = context;
        this.mModemStackController = HwModemStackController.getInstance();
        this.mModemCapInfo = new ModemCapabilityInfo[this.mNumPhones];
        this.mModemStackController.registerForModemRatCapsAvailable(this, 1, null);
        for (int i = 0; i < this.mNumPhones; i++) {
            this.mPreferredStackId[i] = i;
            this.mCurrentStackId[i] = i;
            this.mNwModeinSubIdTable[i] = 1;
            this.mStoredResponse.put(Integer.valueOf(i), null);
        }
        if (!(IS_CMCC_4G_DSDX_ENABLE || IS_CMCC_CU_DSDX_ENABLE)) {
            logd("init the PreNwMode if don't support CMCC and CMCC_CU DSDX");
            switch (DEFAULT_NETWORK_MODE) {
                case 17:
                    System.putInt(this.mContext.getContentResolver(), "network_mode_4G_pre", 17);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_3G_pre", 16);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_2G_only", 1);
                    break;
                case 20:
                    System.putInt(this.mContext.getContentResolver(), "network_mode_4G_pre", 20);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_3G_pre", 18);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_2G_only", 1);
                    break;
                default:
                    System.putInt(this.mContext.getContentResolver(), "network_mode_4G_pre", -1);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_4G_pre", -1);
                    System.putInt(this.mContext.getContentResolver(), "network_mode_2G_only", -1);
                    break;
            }
        }
        logd("Constructor - Exit");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                handleModemRatCapsAvailable();
                return;
            case 2:
                handleUpdateBindingDone(msg.obj);
                return;
            case 3:
                handleSetPreferredNetwork(msg);
                return;
            default:
                return;
        }
    }

    private void handleSetPreferredNetwork(Message msg) {
        AsyncResult ar = msg.obj;
        int index = ((Integer) ar.userObj).intValue();
        if (ar.exception == null) {
            this.mNumOfSetPrefNwModeSuccess++;
            if (this.mNumOfSetPrefNwModeSuccess == this.mNumPhones) {
                for (int i = 0; i < this.mNumPhones; i++) {
                    logd("Updating network mode in DB for slot[" + i + "] with " + this.mNwModeinSubIdTable[i]);
                    TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i, this.mNwModeinSubIdTable[i]);
                }
                this.mNumOfSetPrefNwModeSuccess = 0;
                return;
            }
            return;
        }
        logd("Failed to set preferred network mode for slot" + index);
        this.mNumOfSetPrefNwModeSuccess = 0;
    }

    private void handleUpdateBindingDone(AsyncResult ar) {
        this.mIsSetPrefNwModeInProgress = false;
        for (int i = 0; i < this.mNumPhones; i++) {
            int errorCode = 0;
            Message resp = (Message) this.mStoredResponse.get(Integer.valueOf(i));
            if (resp != null) {
                if (ar.exception != null) {
                    errorCode = 2;
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
        boolean updateRequired = false;
        syncPreferredNwModeFromDB();
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        for (i = 0; i < this.mNumPhones; i++) {
            int[] subIdList = subCtrlr.getSubId(i);
            if (subIdList != null && subIdList[0] >= 0) {
                int subId = subIdList[0];
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    this.mNwModeinSubIdTable[i] = subCtrlr.getNwMode(subId);
                } else {
                    this.mNwModeinSubIdTable[i] = 1;
                }
                if (this.mNwModeinSubIdTable[i] == -1) {
                    updateRequired = false;
                    break;
                } else if (this.mNwModeinSubIdTable[i] != this.mPrefNwMode[i]) {
                    updateRequired = true;
                }
            }
        }
        if (updateRequired && updateStackBindingIfRequired(false) == 0) {
            for (i = 0; i < this.mNumPhones; i++) {
                this.mCi[i].setPreferredNetworkType(this.mNwModeinSubIdTable[i], obtainMessage(3, Integer.valueOf(i)));
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
            if (1 != updateStackBindingIfRequired(true)) {
                this.mIsSetPrefNwModeInProgress = false;
            }
        }
    }

    private void syncCurrentStackInfo() {
        for (int i = 0; i < this.mNumPhones; i++) {
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
        boolean isUpdateStackBindingRequired = false;
        updatePreferredStackIds();
        for (int i = 0; i < this.mNumPhones; i++) {
            if (this.mPreferredStackId[i] != this.mCurrentStackId[i]) {
                isUpdateStackBindingRequired = true;
                logd("isUpdateStackBindingRequired = true;");
                break;
            }
        }
        if (!isBootUp && !isUpdateStackBindingRequired) {
            return 0;
        }
        int response = this.mModemStackController.updateStackBinding(this.mPreferredStackId, isBootUp, Message.obtain(this, 2, null));
        logd("mModemStackController.updateStackBinding");
        return response;
    }

    private void updatePreferredStackIds() {
        if (this.mModemRatCapabilitiesAvailable) {
            syncPreferredNwModeFromDB();
            syncCurrentStackInfo();
            int curPhoneId = 0;
            while (curPhoneId < this.mNumPhones) {
                if (isNwModeSupportedOnStack(this.mPrefNwMode[curPhoneId], this.mCurrentStackId[curPhoneId])) {
                    logd("updatePreferredStackIds: current stack[" + this.mCurrentStackId[curPhoneId] + "]supports NwMode[" + this.mPrefNwMode[curPhoneId] + "] on phoneId[" + curPhoneId + "]");
                } else {
                    int otherPhoneId = 0;
                    while (otherPhoneId < this.mNumPhones) {
                        if (otherPhoneId != curPhoneId && isNwModeSupportedOnStack(this.mPrefNwMode[curPhoneId], this.mCurrentStackId[otherPhoneId]) && isNwModeSupportedOnStack(this.mPrefNwMode[otherPhoneId], this.mCurrentStackId[curPhoneId])) {
                            logd("updatePreferredStackIds: Cross Binding is possible between phoneId[" + curPhoneId + "] and phoneId[" + otherPhoneId + "]");
                            this.mPreferredStackId[curPhoneId] = this.mCurrentStackId[otherPhoneId];
                            this.mPreferredStackId[otherPhoneId] = this.mCurrentStackId[curPhoneId];
                        }
                        otherPhoneId++;
                    }
                }
                curPhoneId++;
            }
            return;
        }
        loge("updatePreferredStackIds: Modem Capabilites are not Available. Return!!");
    }

    private boolean isNwModeSupportedOnStack(int nwMode, int stackId) {
        int[] numRatSupported = new int[this.mNumPhones];
        int maxNumRatSupported = 0;
        boolean isSupported = false;
        int i = 0;
        while (i < this.mNumPhones) {
            if (this.mModemCapInfo[i] != null) {
                numRatSupported[i] = getNumOfRatSupportedForNwMode(nwMode, this.mModemCapInfo[i]);
                if (maxNumRatSupported < numRatSupported[i]) {
                    maxNumRatSupported = numRatSupported[i];
                }
                i++;
            } else {
                logd("mModemInfo[" + i + "] is NULL");
                return false;
            }
        }
        if (numRatSupported[stackId] == maxNumRatSupported) {
            if (HwAllInOneController.IS_CARD2_CDMA_SUPPORTED || stackId == 0 || nwMode == 1 || numRatSupported[0] != maxNumRatSupported) {
                isSupported = true;
            } else {
                isSupported = false;
            }
        }
        logd("nwMode:" + nwMode + ", on stack:" + stackId + " is " + (isSupported ? "Supported" : "Not Supported"));
        return isSupported;
    }

    private void syncPreferredNwModeFromDB() {
        for (int i = 0; i < this.mNumPhones; i++) {
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
            case 17:
                if (9 != networkType) {
                    if (3 == networkType) {
                        networkTypeNew = 16;
                        break;
                    }
                }
                networkTypeNew = 17;
                break;
                break;
            case 20:
                if (9 != networkType) {
                    if (3 == networkType) {
                        networkTypeNew = 18;
                        break;
                    }
                }
                networkTypeNew = 20;
                break;
                break;
            case 22:
                if (!IS_FULL_NETWORK_SUPPORTED) {
                    if (9 != networkType) {
                        if (3 == networkType) {
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
            sendResponseToTarget(response, 2);
        } else if (HwAllInOneController.IS_QCRIL_CROSS_MAPPING) {
            logd("CROSS_MAPPING by QCRIL,setPreferredNetworkType directly");
            this.mCi[phoneId].setPreferredNetworkType(networkType, response);
        } else {
            logd("setPreferredNetworkType: nwMode:" + networkType + ", on phoneId:" + phoneId);
            networkType = updateNetworkType(networkType);
            this.mIsSetPrefNwModeInProgress = true;
            try {
                if (updateStackBindingIfRequired(false) == 1) {
                    this.mStoredResponse.put(Integer.valueOf(phoneId), response);
                } else {
                    this.mCi[phoneId].setPreferredNetworkType(networkType, response);
                    this.mIsSetPrefNwModeInProgress = false;
                }
            } catch (Exception e) {
                loge("setPreferredNetworkType: Exception:" + e.getMessage());
                sendResponseToTarget(response, 2);
                this.mIsSetPrefNwModeInProgress = false;
            }
        }
    }

    public void restoreToPrevState() {
        this.mModemStackController.backToPrevState();
        this.mIsSetPrefNwModeInProgress = false;
    }

    private void sendResponseToTarget(Message response, int responseCode) {
        if (response != null) {
            AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
            response.sendToTarget();
        }
    }

    private int getNumOfRatSupportedForNwMode(int nwMode, ModemCapabilityInfo modemCaps) {
        int supportedRatMaskForNwMode = 0;
        logd("getNumOfRATsSupportedForNwMode: nwMode[" + nwMode + "] modemCaps = " + modemCaps);
        if (modemCaps == null) {
            loge("getNumOfRATsSupportedForNwMode: modemCaps = null, return 0");
            return 0;
        }
        switch (nwMode) {
            case 0:
            case 3:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & 101902;
                break;
            case 1:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_GSM_ONLY;
                break;
            case 2:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_WCDMA_ONLY;
                break;
            case 4:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_CDMA;
                break;
            case 5:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_CDMA_NO_EVDO;
                break;
            case 6:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_EVDO_NO_CDMA;
                break;
            case 7:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_GLOBAL;
                break;
            case 8:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_CDMA_EVDO;
                break;
            case 9:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_GSM_WCDMA;
                break;
            case 10:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_CMDA_EVDO_GSM_WCDMA;
                break;
            case 11:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & 16384;
                break;
            case 12:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_LTE_WCDMA;
                break;
            case 13:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_ONLY;
                break;
            case 14:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_WCDMA;
                break;
            case 15:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_LTE;
                break;
            case 16:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_GSM;
                break;
            case 17:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_GSM_LTE;
                break;
            case 18:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_GSM_WCDMA;
                break;
            case 19:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_WCDMA_LTE;
                break;
            case 20:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_GSM_WCDMA_LTE;
                break;
            case 21:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_CDMA_EVDO_GSM_WCDMA;
                break;
            case 22:
                supportedRatMaskForNwMode = modemCaps.getSupportedRatBitMask() & NETWORK_MASK_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA;
                break;
        }
        logd("getNumOfRATsSupportedForNwMode: supportedRatMaskForNwMode:" + supportedRatMaskForNwMode);
        return getNumRatSupportedInMask(supportedRatMaskForNwMode);
    }

    private int getNumRatSupportedInMask(int mask) {
        int noOfOnes = 0;
        while (mask != 0) {
            mask &= mask - 1;
            noOfOnes++;
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
