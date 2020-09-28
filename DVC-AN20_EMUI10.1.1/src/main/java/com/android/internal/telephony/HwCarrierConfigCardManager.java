package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.uicc.HwIccRecordsEx;
import com.android.internal.telephony.uicc.HwRuimRecords;
import com.android.internal.telephony.uicc.HwSIMRecords;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import huawei.cust.HwCarrierConfigPolicy;
import huawei.cust.aidl.SimFileInfo;
import huawei.cust.aidl.SimMatchRule;
import huawei.cust.aidl.SpecialFile;
import java.util.List;

public class HwCarrierConfigCardManager {
    public static final int HW_CARRIER_FILE_AFTER_LOADED = 8;
    public static final int HW_CARRIER_FILE_C_IMSI = 4;
    public static final int HW_CARRIER_FILE_GID1 = 5;
    public static final int HW_CARRIER_FILE_GID2 = 6;
    public static final int HW_CARRIER_FILE_G_IMSI = 2;
    public static final int HW_CARRIER_FILE_G_MCCMNC = 3;
    public static final int HW_CARRIER_FILE_ICCID = 1;
    public static final int HW_CARRIER_FILE_SPN = 7;
    public static final int HW_CDMA_MCCMNC_LENGTH = 5;
    private static final int INVALID = -1;
    private static final String LOG_TAG = "HwCarrierConfigCardManager";
    private static final int MAX_PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    public static final int RULE_GID1 = 16;
    public static final int RULE_GID2 = 32;
    public static final int RULE_ICCID = 2;
    public static final int RULE_IMSI = 4;
    public static final int RULE_MCCMNC = 1;
    public static final int RULE_NONE = 0;
    public static final int RULE_SPECIAL = 64;
    public static final int RULE_SPN = 8;
    public static final int RULE_UNKNOW = -1;
    public static final int SENSITIVE_MSG_PRINT_MAX_LEN = 8;
    public static final int SPECIAL_FILE_STATE_DONE = 2;
    public static final int SPECIAL_FILE_STATE_NONE = 0;
    public static final int SPECIAL_FILE_STATE_REQUESTING = 1;
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static HwCarrierConfigCardRecord[] mCardRecords;
    private static HwCarrierConfigPolicy mCarrierPolicy;
    private static SimMatchRule[] mCarrierPolicyRule;
    private static boolean[] mCarrierPolicyUpdated;
    private static final Object mLock = new Object();
    private static HwRuimRecords[] mRuimRecords;
    private static HwSIMRecords[] mSimRecords;
    private static int[] mSpecialFileCount;
    private static int[] mSpecialFileRequestState;
    private static HwCarrierConfigCardManager sInstance;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwCarrierConfigCardManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwCarrierConfigCardManager.loge("intent is null, return");
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                String simState = "UNKNOWN";
                int slotId = -1;
                try {
                    simState = (String) intent.getExtra("ss");
                    slotId = intent.getIntExtra("phone", -1000);
                } catch (ClassCastException ex) {
                    HwCarrierConfigCardManager.logd("Get Intent Extra ClassCastException ex:" + ex.getMessage());
                }
                HwCarrierConfigCardManager.logd("receive broadcast intent SIM_STATE_CHANGED, slotId:" + slotId + " simState:" + simState);
                if (!HwCarrierConfigCardManager.this.checkParaAndData(slotId)) {
                    HwCarrierConfigCardManager.loge("onReceive ACTION_SIM_STATE_CHANGED para error!", slotId);
                    return;
                }
                boolean isNeedClearForEuicc = HwTelephonyManager.getDefault().isEuicc(slotId) && "NOT_READY".equals(simState);
                if ("ABSENT".equals(simState) || "LOCKED".equals(simState) || "CARD_IO_ERROR".equals(simState) || isNeedClearForEuicc) {
                    HwCarrierConfigCardManager.this.destoryAndUpdateSimFileInfo(slotId);
                }
            } else if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                int slotId2 = -1;
                int subState = -1;
                int result = -1;
                try {
                    slotId2 = ((Integer) intent.getExtra("phone", -1)).intValue();
                    subState = ((Integer) intent.getExtra("newSubState", -1)).intValue();
                    result = ((Integer) intent.getExtra("operationResult", -1)).intValue();
                } catch (ClassCastException ex2) {
                    HwCarrierConfigCardManager.logd("Get Intent Extra ClassCastException ex:" + ex2.getMessage());
                }
                HwCarrierConfigCardManager.logd("received broadcast intent ACTION_SUBSCRIPTION_SET_UICC_RESULT,slotId:" + slotId2 + " subState:" + subState + " result:" + result);
                if (!HwCarrierConfigCardManager.this.checkParaAndData(slotId2)) {
                    HwCarrierConfigCardManager.loge("onReceive ACTION_SUBSCRIPTION_SET_UICC_RESULT para error!", slotId2);
                } else if (result == 0 && subState == 0) {
                    HwCarrierConfigCardManager.this.destoryAndUpdateSimFileInfo(slotId2);
                }
            }
        }
    };

    static {
        int i = MAX_PHONE_COUNT;
        mCarrierPolicyRule = new SimMatchRule[i];
        mCarrierPolicyUpdated = new boolean[i];
        mSpecialFileCount = new int[i];
        mSpecialFileRequestState = new int[i];
        mCardRecords = new HwCarrierConfigCardRecord[i];
        mSimRecords = new HwSIMRecords[i];
        mRuimRecords = new HwRuimRecords[i];
    }

    private HwCarrierConfigCardManager(Context context) {
        for (int i = 0; i < MAX_PHONE_COUNT; i++) {
            mCarrierPolicyRule[i] = null;
            mCarrierPolicyUpdated[i] = false;
            mSpecialFileCount[i] = 0;
            mSpecialFileRequestState[i] = 0;
            mCardRecords[i] = new HwCarrierConfigCardRecord(i);
            mSimRecords[i] = null;
            mRuimRecords[i] = null;
        }
        mCarrierPolicy = HwCarrierConfigPolicy.getDefault();
        IntentFilter filter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            filter.addAction("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        } else {
            logd("ACTION_SUBSCRIPTION_SET_UICC_RESULT action Not process for HISI or MTK");
        }
        context.registerReceiver(this.mReceiver, filter);
        logd("HwCarrierConfigCardManager constructor!");
    }

    public static HwCarrierConfigCardManager getDefault(Context context) {
        HwCarrierConfigCardManager hwCarrierConfigCardManager;
        logd("HwCarrierConfigCardManager getDefault()");
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new HwCarrierConfigCardManager(context);
            }
            hwCarrierConfigCardManager = sInstance;
        }
        return hwCarrierConfigCardManager;
    }

    public void reportIccRecordInstance(int slotId, HwIccRecordsEx iccRecords) {
        if (!checkParaAndData(slotId)) {
            loge("reportIccRecordInstance para error!", slotId);
            return;
        }
        if (iccRecords instanceof HwSIMRecords) {
            mSimRecords[slotId] = (HwSIMRecords) iccRecords;
            mCardRecords[slotId].setGsmCardMode(true);
        } else if (iccRecords instanceof HwRuimRecords) {
            mRuimRecords[slotId] = (HwRuimRecords) iccRecords;
            mCardRecords[slotId].seCDMACardMode(true);
        }
        if (mRuimRecords[slotId] != null) {
            mCardRecords[slotId].setCDMAValid(true);
        }
    }

    public void updateCarrierFile(int slotId, int fileType, String fileValue) {
        try {
            if (!checkParaAndData(slotId)) {
                loge("updateCarrierFile para error!", slotId);
                return;
            }
            switch (fileType) {
                case 1:
                    logd("updateIccid iccid=" + givePrintableMsg(fileValue), slotId);
                    mCardRecords[slotId].setIccid(fileValue);
                    break;
                case 2:
                    logd("updateGImsi mccmnc=" + givePrintableMsg(fileValue), slotId);
                    mCardRecords[slotId].setGImsi(fileValue);
                    break;
                case 3:
                    logd("updateGMCCMNC mccmnc=" + fileValue, slotId);
                    mCardRecords[slotId].setGMccmnc(fileValue);
                    break;
                case 4:
                    logd("updateCImsi mccmnc=" + givePrintableMsg(fileValue), slotId);
                    mCardRecords[slotId].setCImsi(fileValue);
                    if (fileValue != null && fileValue.length() > 5) {
                        mCardRecords[slotId].setCMccmnc(fileValue.substring(0, 5));
                        break;
                    }
                case 5:
                    logd("update gid1=" + fileValue, slotId);
                    mCardRecords[slotId].setGid1(fileValue);
                    break;
                case 6:
                    logd("update gid2=" + fileValue, slotId);
                    mCardRecords[slotId].setGid2(fileValue);
                    break;
                case 7:
                    logd("update spn=" + fileValue, slotId);
                    mCardRecords[slotId].setSpn(fileValue);
                    break;
                case 8:
                    loge("HW_CARRIER_FILE_AFTER_LOADED", slotId);
                    break;
                default:
                    loge("updateCarrierFile unknow fileType=" + fileType, slotId);
                    return;
            }
            checkSimMatchInfo(slotId);
        } catch (Exception e) {
            loge("updateCarrierFile Exception");
        }
    }

    public void addSpecialFileResult(boolean result, String filePath, String fileId, String value, int slotId) {
        if (!checkParaAndData(slotId)) {
            loge("addSpecialFileResult para error!", slotId);
        } else if (2 == mSpecialFileRequestState[slotId]) {
            loge("addSpecialFile has finished!", slotId);
        } else {
            if (!result) {
                loge("addSpecialFileResult fail, filePath=" + filePath + " fileId=" + fileId, slotId);
            } else {
                logd("add filePath=" + filePath + " fileId=" + fileId + " value=" + value, slotId);
            }
            mCardRecords[slotId].setSpecialFileValue(filePath, fileId, value);
            int[] iArr = mSpecialFileCount;
            iArr[slotId] = iArr[slotId] - 1;
            if (iArr[slotId] == 0) {
                reportSpecialFileComplete(slotId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkParaAndData(int slotId) {
        if (slotId < 0 || slotId >= MAX_PHONE_COUNT) {
            loge("Error slotId:" + slotId);
            return false;
        } else if (mCardRecords[slotId] != null) {
            return true;
        } else {
            loge("mCardRecords is null!", slotId);
            return false;
        }
    }

    private boolean mandatoryInfoCheckPass(int slotId) {
        if (!checkParaAndData(slotId)) {
            loge("mandatoryInfoCheckPass para error!", slotId);
            return false;
        }
        int recordFlag = mCardRecords[slotId].getRecordFlag();
        if (7 == (recordFlag & 7)) {
            return true;
        }
        logd(" recordFlag check fail, recordFlag=0x" + Integer.toHexString(recordFlag), slotId);
        return false;
    }

    private void checkSimMatchInfo(int slotId) {
        SimMatchRule simRule;
        if (!checkParaAndData(slotId)) {
            loge("checkSimMatchInfo para error!", slotId);
        } else if (mCarrierPolicy == null) {
            loge("mCarrierPolicy is null!", slotId);
        } else if (mandatoryInfoCheckPass(slotId)) {
            if (mCarrierPolicyUpdated[slotId]) {
                logd("already called updateSimFileInfo before", slotId);
                return;
            }
            SimMatchRule[] simMatchRuleArr = mCarrierPolicyRule;
            if (simMatchRuleArr[slotId] == null) {
                logd("querySimMatchRule", slotId);
                simRule = mCarrierPolicy.querySimMatchRule(mCardRecords[slotId].getMccmnc(), mCardRecords[slotId].getIccid(), mCardRecords[slotId].getImsi(), slotId);
                mCarrierPolicyRule[slotId] = simRule;
            } else {
                simRule = simMatchRuleArr[slotId];
            }
            if (simRule == null) {
                logd("querySimMatchRule return null, query later!", slotId);
                return;
            }
            int ruleRet = simRule.getRule();
            if (ruleRet == 0) {
                logd("no need to call updateSimFileInfo", slotId);
                return;
            }
            if ((ruleRet & 64) != 0) {
                int[] iArr = mSpecialFileRequestState;
                if (1 == iArr[slotId]) {
                    logd("is requesting special file", slotId);
                    return;
                } else if (iArr[slotId] == 0) {
                    logd("need to requet special files:" + simRule.getSpecialFiles(), slotId);
                    if (requestSpecialFile(slotId, simRule.getSpecialFiles())) {
                        mSpecialFileRequestState[slotId] = 1;
                        logd("sent request special files!", slotId);
                        return;
                    }
                    loge("requestSpecialFile return fail!", slotId);
                    mSpecialFileRequestState[slotId] = 2;
                    mCardRecords[slotId].addSpecialFlag();
                }
            }
            if (mSimRecords == null) {
                ruleRet &= -57;
                logd("maybe single Ruim card, clear rule_spn/rule_gid1/rule_gid2!", slotId);
            }
            int recordFlag = mCardRecords[slotId].getRecordFlag();
            if (((~recordFlag) & ruleRet) != 0) {
                logd("sim info is not complete, ruleRet=0x" + Integer.toHexString(simRule.getRule()) + " recordFlag=0x" + Integer.toHexString(recordFlag), slotId);
                return;
            }
            SimFileInfo simInfo = new SimFileInfo();
            logd("ruleRet=0x" + Integer.toHexString(ruleRet) + " recordFlag=0x" + Integer.toHexString(recordFlag), slotId);
            if ((ruleRet & 4) != 0) {
                simInfo.setImsi(mCardRecords[slotId].getImsi());
            }
            if ((ruleRet & 1) != 0) {
                simInfo.setMccMnc(mCardRecords[slotId].getMccmnc());
            }
            if ((ruleRet & 2) != 0) {
                simInfo.setIccid(mCardRecords[slotId].getIccid());
            }
            if ((ruleRet & 8) != 0) {
                simInfo.setSpn(mCardRecords[slotId].getSpn());
            }
            if ((ruleRet & 16) != 0) {
                simInfo.setGid1(mCardRecords[slotId].getGid1());
            }
            if ((ruleRet & 32) != 0) {
                simInfo.setGid2(mCardRecords[slotId].getGid2());
            }
            if ((ruleRet & 64) != 0) {
                simInfo.setSpecialFiles(mCardRecords[slotId].getSpecialFiles());
            }
            logd("updateSimFileInfo to policy simInfo=" + simInfo, slotId);
            mCarrierPolicy.updateSimFileInfo(simInfo, slotId);
            mCarrierPolicyUpdated[slotId] = true;
        }
    }

    private boolean requestSpecialFile(int slotId, List<SpecialFile> specialFiles) {
        if (!checkParaAndData(slotId)) {
            loge("requestSpecialFile para error!", slotId);
            return false;
        } else if (specialFiles == null || mSimRecords[slotId] == null) {
            mCardRecords[slotId].setSpecialFiles(specialFiles);
            return false;
        } else {
            mSpecialFileCount[slotId] = 0;
            mCardRecords[slotId].setSpecialFiles(specialFiles);
            for (SpecialFile sp : specialFiles) {
                if (sp.getFileId() != null) {
                    if (mSimRecords[slotId].loadCarrierFile(sp.getFilePath(), sp.getFileId())) {
                        int[] iArr = mSpecialFileCount;
                        iArr[slotId] = iArr[slotId] + 1;
                    } else {
                        loge("loadCarrierFile fail, check filePath or fileId!", slotId);
                        mSpecialFileCount[slotId] = 0;
                        return false;
                    }
                }
            }
            logd("request " + mSpecialFileCount[slotId] + " files.", slotId);
            if (mSpecialFileCount[slotId] != 0) {
                return true;
            }
            logd("no file in the specialFile list!", slotId);
            return false;
        }
    }

    private void reportSpecialFileComplete(int slotId) {
        if (!checkParaAndData(slotId)) {
            loge("reportSpecialFileComplete para error!", slotId);
            return;
        }
        logd("reportSpecialFileComplete", slotId);
        mSpecialFileRequestState[slotId] = 2;
        mCardRecords[slotId].addSpecialFlag();
        checkSimMatchInfo(slotId);
    }

    public void destory(int slotId, HwIccRecordsEx iccRecords) {
        if (!checkParaAndData(slotId)) {
            loge("destory para error!", slotId);
            return;
        }
        mCarrierPolicyRule[slotId] = null;
        mCarrierPolicyUpdated[slotId] = false;
        mSpecialFileCount[slotId] = 0;
        mSpecialFileRequestState[slotId] = 0;
        HwCarrierConfigCardRecord[] hwCarrierConfigCardRecordArr = mCardRecords;
        if (hwCarrierConfigCardRecordArr[slotId] != null) {
            hwCarrierConfigCardRecordArr[slotId].dispose();
        }
        HwSIMRecords[] hwSIMRecordsArr = mSimRecords;
        if (hwSIMRecordsArr[slotId] == iccRecords) {
            hwSIMRecordsArr[slotId] = null;
        }
        HwRuimRecords[] hwRuimRecordsArr = mRuimRecords;
        if (hwRuimRecordsArr[slotId] == iccRecords) {
            hwRuimRecordsArr[slotId] = null;
        }
        logd("clear Records for slotId=" + slotId);
    }

    private static String givePrintableMsg(String msg) {
        if (msg == null) {
            return null;
        }
        if (msg.length() <= 8) {
            return msg;
        }
        return msg.substring(0, 8) + "XXXXXXXXXXX";
    }

    /* access modifiers changed from: private */
    public static void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private static void logd(String msg, int slotId) {
        RlogEx.i(LOG_TAG, "[" + slotId + "]" + msg);
    }

    /* access modifiers changed from: private */
    public static void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void loge(String msg, int slotId) {
        RlogEx.e(LOG_TAG, "[" + slotId + "]" + msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destoryAndUpdateSimFileInfo(int slotId) {
        if (!checkParaAndData(slotId)) {
            loge("destoryAndUpdateSimFileInfo para error!", slotId);
            return;
        }
        HwCarrierConfigPolicy hwCarrierConfigPolicy = mCarrierPolicy;
        if (hwCarrierConfigPolicy == null) {
            loge("mCarrierPolicy is null!", slotId);
            return;
        }
        hwCarrierConfigPolicy.updateSimFileInfo((SimFileInfo) null, slotId);
        logd("destoryAndUpdateSimFileInfo slotId=" + slotId);
    }

    public void updateCarrierFileIfNeed(int slotId, IccRecordsEx simRecords) {
        if (!checkParaAndData(slotId) || mCarrierPolicyUpdated[slotId] || simRecords == null) {
            loge("updateCarrierFileIfNeed: do nothing.");
            return;
        }
        loge("updateCarrierFileIfNeed: Maybe some exceptions occur, update carrier policy here.", slotId);
        mCardRecords[slotId].setIccid(simRecords.getIccIdHw());
        String imsi = simRecords.getIMSI();
        String mccmnc = simRecords.getOperatorNumeric();
        if (isMultiSimEnabled) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
                logd("updateCarrierFileIfNeed: MultiSimEnabled, RoamingBrokerActivated, use home imsi and mccmnc", slotId);
                imsi = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi(Integer.valueOf(slotId));
                mccmnc = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(slotId));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            logd("updateCarrierFileIfNeed: SingleSimEnabled, RoamingBrokerActivated, use home imsi and mccmnc", slotId);
            imsi = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi();
            mccmnc = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        mCardRecords[slotId].setGImsi(imsi);
        mCardRecords[slotId].setGMccmnc(mccmnc);
        mCardRecords[slotId].setSpn(simRecords.getServiceProviderName());
        mCardRecords[slotId].setGid1(simRecords.getGid1());
        mCardRecords[slotId].setGid2(simRecords.getGid2());
        mCardRecords[slotId].addSpecialFlag();
        updateCarrierFile(slotId, 8, null);
    }

    public void setSingleModeCdmaCard(int slotId, boolean value) {
        if (!checkParaAndData(slotId)) {
            loge("setSingleModeCdmaCard para error!", slotId);
        } else {
            mCardRecords[slotId].setSingleModeCdmaCard(value);
        }
    }
}
