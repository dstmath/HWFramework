package com.android.internal.telephony.uicc;

import android.app.ActivityManagerNative;
import android.common.HwFrameworkFactory;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.encrypt.PasswordUtil;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwAddonTelephonyFactory;
import com.android.internal.telephony.HwCarrierConfigCardManager;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.ApnReminder;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.gsm.HwEons;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.android.internal.telephony.vsim.HwVSimConstants;
import huawei.cust.HwCustUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class HwSIMRecords extends SIMRecords {
    public static final String ANY_SIM_DETECTED = "any_sim_detect";
    private static final int EVENT_GET_ACTING_HPLMN_DONE = 201;
    private static final int EVENT_GET_ALL_OPL_RECORDS_DONE = 101;
    private static final int EVENT_GET_ALL_PNN_RECORDS_DONE = 102;
    private static final int EVENT_GET_CARRIER_FILE_DONE = 4;
    private static final int EVENT_GET_GID1_HW_DONE = 1;
    private static final int EVENT_GET_GID1_HW_DONE_EX = 3;
    private static final int EVENT_GET_PBR_DONE = 233;
    private static final int EVENT_GET_SPECIAL_FILE_DONE = 2;
    private static final int EVENT_GET_SPN = 103;
    private static final int EVENT_GET_SPN_CPHS_DONE = 104;
    private static final int EVENT_GET_SPN_SHORT_CPHS_DONE = 105;
    private static final int EVENT_HW_CUST_BASE = 100;
    /* access modifiers changed from: private */
    public static final boolean IS_DELAY_UPDATENAME = SystemProperties.getBoolean("ro.config.delay_updatename", false);
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    public static final String MULTI_PDP_PLMN_MATCHED = "multi_pdp_plmn_matched";
    private static final String SIM_IMSI = "sim_imsi_key";
    private static final int SST_PNN_ENABLED = 48;
    private static final int SST_PNN_MASK = 48;
    private static final int SST_PNN_OFFSET = 12;
    private static final String TAG = "HwSIMRecords";
    private static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    /* access modifiers changed from: private */
    public static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static PasswordUtil mPasswordUtil = HwFrameworkFactory.getPasswordUtil();
    private static final String pRefreshMultifileProp = "gsm.sim.refresh.multifile";
    private static final String pRefreshMultifilePropExtra = "gsm.sim.refresh.multifile.extra";
    private static String[] strEFIDs = new String[30];
    protected boolean bNeedSendRefreshBC = false;
    /* access modifiers changed from: private */
    public GlobalChecker globalChecker = new GlobalChecker(this);
    /* access modifiers changed from: private */
    public Handler handlerEx = new Handler() {
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x022d, code lost:
            if (r0 != false) goto L_0x022f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x022f, code lost:
            r13.this$0.onRecordLoaded();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x023f, code lost:
            if (0 == 0) goto L_0x0242;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x0242, code lost:
            return;
         */
        public void handleMessage(Message msg) {
            boolean isRecordLoadResponse = false;
            if (HwSIMRecords.this.mDestroyed.get()) {
                HwSIMRecords hwSIMRecords = HwSIMRecords.this;
                hwSIMRecords.loge("Received message " + msg + "[" + msg.what + "]  while being destroyed. Ignoring.");
                return;
            }
            try {
                switch (msg.what) {
                    case 1:
                        isRecordLoadResponse = true;
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            HwSIMRecords.this.mEfGid1 = (byte[]) ar.result;
                            HwSIMRecords hwSIMRecords2 = HwSIMRecords.this;
                            hwSIMRecords2.log("mEfGid1: " + IccUtils.bytesToHexString(HwSIMRecords.this.mEfGid1));
                            break;
                        } else {
                            HwSIMRecords hwSIMRecords3 = HwSIMRecords.this;
                            hwSIMRecords3.log("Get GID1 failed, the exception: " + ar.exception);
                            HwSIMRecords.this.globalChecker.loadGID1Ex();
                            break;
                        }
                    case 2:
                        isRecordLoadResponse = true;
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null) {
                            Bundle bundle = msg.getData();
                            String filePath = bundle.getString(HwTelephony.VirtualNets.MATCH_PATH);
                            String fileId = bundle.getString(HwTelephony.VirtualNets.MATCH_FILE);
                            byte[] bytes = (byte[]) ar2.result;
                            if (HwSIMRecords.isMultiSimEnabled) {
                                HwTelephonyFactory.getHwPhoneManager().addVirtualNetSpecialFile(filePath, fileId, bytes, HwSIMRecords.this.getSlotId());
                            } else {
                                HwTelephonyFactory.getHwPhoneManager().addVirtualNetSpecialFile(filePath, fileId, bytes);
                            }
                            if (HwSIMRecords.this.mHwCustHwSIMRecords != null) {
                                HwSIMRecords.this.mHwCustHwSIMRecords.addHwVirtualNetSpecialFiles(filePath, fileId, bytes, HwSIMRecords.this.getSlotId());
                            }
                            HwSIMRecords hwSIMRecords4 = HwSIMRecords.this;
                            hwSIMRecords4.log("load Specifile: " + filePath + " " + fileId + " = " + IccUtils.bytesToHexString(HwSIMRecords.this.mEfGid1));
                            break;
                        } else {
                            break;
                        }
                    case 3:
                        isRecordLoadResponse = true;
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception == null) {
                            HwSIMRecords.this.mEfGid1 = (byte[]) ar3.result;
                            HwSIMRecords hwSIMRecords5 = HwSIMRecords.this;
                            hwSIMRecords5.log("mEfGid1_ex: " + IccUtils.bytesToHexString(HwSIMRecords.this.mEfGid1));
                            break;
                        } else {
                            HwSIMRecords hwSIMRecords6 = HwSIMRecords.this;
                            hwSIMRecords6.log("Get GID1_EX failed, the exception: " + ar3.exception);
                            break;
                        }
                    case 4:
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null) {
                            Bundle carrierBundle = msg.getData();
                            String carrierFilePath = carrierBundle.getString(HwTelephony.VirtualNets.MATCH_PATH);
                            String carrierFileId = carrierBundle.getString(HwTelephony.VirtualNets.MATCH_FILE);
                            String carrierFileValue = IccUtils.bytesToHexString((byte[]) ar4.result);
                            HwSIMRecords.this.mHwCarrierCardManager.addSpecialFileResult(true, carrierFilePath, carrierFileId, carrierFileValue, HwSIMRecords.this.getSlotId());
                            HwSIMRecords hwSIMRecords7 = HwSIMRecords.this;
                            hwSIMRecords7.log("Carrier load Specialfile: " + carrierFilePath + " " + carrierFileId + " = " + carrierFileValue);
                            break;
                        } else {
                            String carrierFilePath2 = null;
                            String carrierFileId2 = null;
                            Bundle carrierBundle2 = msg.getData();
                            if (carrierBundle2 != null) {
                                carrierFilePath2 = carrierBundle2.getString(HwTelephony.VirtualNets.MATCH_PATH);
                                carrierFileId2 = carrierBundle2.getString(HwTelephony.VirtualNets.MATCH_FILE);
                                HwSIMRecords hwSIMRecords8 = HwSIMRecords.this;
                                hwSIMRecords8.log("load Specialfile: " + carrierFilePath2 + " " + carrierFileId2 + " fail!");
                            }
                            HwSIMRecords.this.mHwCarrierCardManager.addSpecialFileResult(false, carrierFilePath2, carrierFileId2, null, HwSIMRecords.this.getSlotId());
                            HwSIMRecords hwSIMRecords9 = HwSIMRecords.this;
                            hwSIMRecords9.log("exception=" + ar4.exception);
                            break;
                        }
                    default:
                        HwSIMRecords hwSIMRecords10 = HwSIMRecords.this;
                        hwSIMRecords10.log("unknown Event: " + msg.what);
                        break;
                }
            } catch (RuntimeException exc) {
                HwSIMRecords.this.logw("Exception parsing SIM record", exc);
            } catch (Throwable th) {
                if (0 != 0) {
                    HwSIMRecords.this.onRecordLoaded();
                }
                throw th;
            }
        }
    };
    private boolean isEnsEnabled = SystemProperties.getBoolean("ro.config.hw_is_ens_enabled", false);
    private String mActingHplmn = "";
    byte[] mEfGid1 = null;
    HwEons mEons = new HwEons();
    HwCarrierConfigCardManager mHwCarrierCardManager;
    /* access modifiers changed from: private */
    public HwCustHwSIMRecords mHwCustHwSIMRecords;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(action)) {
                    HwSIMRecords hwSIMRecords = HwSIMRecords.this;
                    hwSIMRecords.log("Receives ACTION_SET_RADIO_CAPABILITY_DONE on slot " + HwSIMRecords.this.getSlotId());
                    if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT && HwSIMRecords.this.mIsSimPowerDown && HwSIMRecords.this.mParentApp != null && IccCardApplicationStatus.AppState.APPSTATE_READY == HwSIMRecords.this.mParentApp.getState()) {
                        HwSIMRecords.this.log("fetchSimRecords again.");
                        boolean unused = HwSIMRecords.this.mIsSimPowerDown = false;
                        HwSIMRecords.this.fetchSimRecords();
                    }
                } else if ("com.huawei.action.CARRIER_CONFIG_CHANGED".equals(action) && intent.getExtras() != null) {
                    int state = intent.getExtras().getInt("state");
                    Rlog.d("SIMRecords", " onReceive action state = " + state);
                    if (3 == state) {
                        String operator = HwSIMRecords.this.getOperatorNumeric();
                        HwSIMRecords hwSIMRecords2 = HwSIMRecords.this;
                        hwSIMRecords2.log("Receives HW_ACTION_CARRIER_CONFIG_CHANGED operator= " + operator);
                        if (operator != null) {
                            HwSIMRecords.this.setVoiceMailByCountry(operator);
                        }
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsSimPowerDown = false;
    /* access modifiers changed from: private */
    public ArrayList<byte[]> mPnnRecords = null;
    private boolean mSstPnnVaild = true;

    private class GlobalChecker {
        private SIMRecords mSimRecords;

        public GlobalChecker(SIMRecords simRecords) {
            this.mSimRecords = simRecords;
        }

        public void onOperatorNumericLoaded() {
            loadVirtualNetSpecialFiles();
            checkMultiPdpConfig();
            if (HwSIMRecords.isMultiSimEnabled) {
                checkDataServiceRemindMsim();
            } else {
                checkDataServiceRemind();
            }
            checkGsmOnlyDataNotAllowed();
            if (HwSIMRecords.this.mHwCustHwSIMRecords != null) {
                HwSIMRecords.this.mHwCustHwSIMRecords.setVmPriorityModeInClaro(HwSIMRecords.this.mVmConfig);
            }
        }

        private void checkDataServiceRemindMsim() {
            int lSimSlotVal = HwSIMRecords.this.getSlotId();
            int lDataVal = HwTelephonyManagerInner.getDefault().getPreferredDataSubscription();
            boolean hasTwoCard = true;
            if (lSimSlotVal == 1 && lDataVal == 0) {
                hasTwoCard = TelephonyManager.getDefault().hasIccCard(lDataVal);
            }
            if (lSimSlotVal == 0) {
                SystemProperties.set("gsm.huawei.RemindDataService", "false");
            } else if (1 == lSimSlotVal) {
                SystemProperties.set("gsm.huawei.RemindDataService_1", "false");
            }
            String plmnsConfig = SettingsEx.Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), "plmn_remind_data_service");
            if (plmnsConfig == null) {
                plmnsConfig = "26006,26003";
            }
            for (String plmn : plmnsConfig.split(",")) {
                if (plmn != null && plmn.equals(HwSIMRecords.this.getOperatorNumeric())) {
                    if (!"true".equals(Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED)) && (lDataVal == lSimSlotVal || !hasTwoCard)) {
                        ((TelephonyManager) HwSIMRecords.this.mContext.getSystemService("phone")).setDataEnabled(false);
                        Settings.System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
                    }
                    if (lSimSlotVal == 0) {
                        SystemProperties.set("gsm.huawei.RemindDataService", "true");
                    } else if (1 == lSimSlotVal) {
                        SystemProperties.set("gsm.huawei.RemindDataService_1", "true");
                    }
                }
            }
            Settings.System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
        }

        public void onAllRecordsLoaded() {
            updateCarrierFileIfNeed();
            HwSIMRecords.this.mVmConfig.clearVoicemailLoadedFlag();
            loadVirtualNet();
            if (HwSIMRecords.isMultiSimEnabled) {
                ApnReminder apnReminder = ApnReminder.getInstance(HwSIMRecords.this.mContext, HwSIMRecords.this.getSlotId());
                apnReminder.setGID1(HwSIMRecords.this.mEfGid1);
                apnReminder.setPlmnAndImsi(HwSIMRecords.this.getOperatorNumeric(), HwSIMRecords.this.mImsi);
            } else {
                ApnReminder.getInstance(HwSIMRecords.this.mContext).setGID1(HwSIMRecords.this.mEfGid1);
                ApnReminder.getInstance(HwSIMRecords.this.mContext).setPlmnAndImsi(HwSIMRecords.this.getOperatorNumeric(), HwSIMRecords.this.mImsi);
            }
            sendSimRecordsReadyBroadcast();
            if (HwSIMRecords.this.mHwCustHwSIMRecords != null) {
                HwSIMRecords.this.mHwCustHwSIMRecords.refreshDataRoamingSettings();
            }
            if (HwSIMRecords.this.mHwCustHwSIMRecords != null) {
                HwSIMRecords.this.mHwCustHwSIMRecords.refreshMobileDataAlwaysOnSettings();
            }
            if (HwSIMRecords.IS_DELAY_UPDATENAME && HwSIMRecords.this.mPnnRecords != null && HwSIMRecords.this.isNeedSetPnn()) {
                try {
                    HwSIMRecords.this.mEons.setPnnData(HwSIMRecords.this.mPnnRecords);
                    HwSIMRecords.this.mRecordsEventsRegistrants.notifyResult(100);
                } catch (RuntimeException exc) {
                    HwSIMRecords.this.logw("Exception set PNN record", exc);
                }
            }
            updateClatForMobile();
        }

        private void updateClatForMobile() {
            SubscriptionController subController = SubscriptionController.getInstance();
            if (subController != null && HwSIMRecords.this.getSlotId() == subController.getDefaultDataSubId()) {
                String mccMnc = HwSIMRecords.this.getOperatorNumeric();
                try {
                    String plmnsConfig = Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), "disable_mobile_clatd");
                    if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccMnc)) {
                        Rlog.d("SIMRecords", "plmnsConfig is null, return");
                    } else if (plmnsConfig.contains(mccMnc)) {
                        Rlog.d("SIMRecords", "disable clatd!");
                        SystemProperties.set("gsm.net.doxlat", "false");
                    } else {
                        SystemProperties.set("gsm.net.doxlat", "true");
                    }
                } catch (Exception e) {
                    HwSIMRecords hwSIMRecords = HwSIMRecords.this;
                    hwSIMRecords.loge("Exception e = " + e);
                }
            }
        }

        private void sendSimRecordsReadyBroadcast() {
            String operatorNumeric = HwSIMRecords.this.getOperatorNumeric();
            String imsi = HwSIMRecords.this.getIMSI();
            Rlog.d("SIMRecords", "broadcast TelephonyIntents.ACTION_SIM_RECORDS_READY");
            Intent intent = new Intent("com.huawei.intent.action.ACTION_SIM_RECORDS_READY");
            intent.addFlags(536870912);
            intent.putExtra("mccMnc", operatorNumeric);
            intent.putExtra(HwVSimConstants.ENABLE_PARA_IMSI, imsi);
            if (!(!TelephonyManager.getDefault().isMultiSimEnabled() || HwSIMRecords.this.mParentApp == null || HwSIMRecords.this.mParentApp.getUiccCard() == null)) {
                int[] subId = SubscriptionManager.getSubId(HwSIMRecords.this.mParentApp.getUiccCard().getPhoneId());
                if (subId != null && subId.length > 0) {
                    SubscriptionManager.putPhoneIdAndSubIdExtra(intent, SubscriptionManager.getPhoneId(subId[0]));
                }
            }
            ActivityManagerNative.broadcastStickyIntent(intent, null, 0);
        }

        private void checkMultiPdpConfig() {
            String plmnsConfig = SettingsEx.Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.MULTI_PDP_PLMN_MATCHED);
            Rlog.d("SIMRecords", "checkMultiPdpConfig plmnsConfig = " + plmnsConfig);
            if (plmnsConfig != null) {
                String[] plmns = plmnsConfig.split(",");
                int length = plmns.length;
                int i = 0;
                while (i < length) {
                    String plmn = plmns[i];
                    if (plmn == null || !plmn.equals(HwSIMRecords.this.getOperatorNumeric())) {
                        i++;
                    } else {
                        SystemProperties.set("gsm.multipdp.plmn.matched", "true");
                        return;
                    }
                }
            }
            SystemProperties.set("gsm.multipdp.plmn.matched", "false");
        }

        private void checkDataServiceRemind() {
            SystemProperties.set("gsm.huawei.RemindDataService", "false");
            String plmnsConfig = SettingsEx.Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), "plmn_remind_data_service");
            Rlog.d("SIMRecords", "checkDataServiceRemind plmnsConfig = " + plmnsConfig);
            if (plmnsConfig == null) {
                plmnsConfig = "26006,26003";
            }
            String[] plmns = plmnsConfig.split(",");
            int length = plmns.length;
            int i = 0;
            while (i < length) {
                String plmn = plmns[i];
                if (plmn == null || !plmn.equals(HwSIMRecords.this.getOperatorNumeric())) {
                    i++;
                } else {
                    if (!"true".equals(Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED))) {
                        ((TelephonyManager) HwSIMRecords.this.mContext.getSystemService("phone")).setDataEnabled(false);
                    }
                    Settings.System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
                    SystemProperties.set("gsm.huawei.RemindDataService", "true");
                    return;
                }
            }
            Settings.System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
        }

        private void checkGsmOnlyDataNotAllowed() {
            if (HwSIMRecords.isMultiSimEnabled) {
                int[] subIds = SubscriptionManager.getSubId(HwSIMRecords.this.getSlotId());
                if (subIds != null) {
                    TelephonyManager.setTelephonyProperty(subIds[0], "gsm.data.gsm_only_not_allow_ps", "false");
                } else {
                    return;
                }
            } else {
                SystemProperties.set("gsm.data.gsm_only_not_allow_ps", "false");
            }
            String plmnGsmonlyPsNotallowd = Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), "hw_2gonly_psnotallowed");
            if (plmnGsmonlyPsNotallowd == null || "".equals(plmnGsmonlyPsNotallowd)) {
                plmnGsmonlyPsNotallowd = "23410";
            }
            String hplmn = HwSIMRecords.this.getOperatorNumeric();
            if (hplmn == null || "".equals(hplmn)) {
                HwSIMRecords.this.log("is2GonlyPsAllowed home plmn not ready");
                return;
            }
            for (String equals : plmnGsmonlyPsNotallowd.split(",")) {
                HwSIMRecords.this.log("is2GonlyPsAllowed plmnCustomArray[" + i + "] = " + plmnCustomArray[i]);
                if (hplmn.equals(equals)) {
                    if (HwSIMRecords.isMultiSimEnabled) {
                        TelephonyManager.setTelephonyProperty(SubscriptionManager.getSubId(HwSIMRecords.this.getSlotId())[0], "gsm.data.gsm_only_not_allow_ps", "true");
                    } else {
                        SystemProperties.set("gsm.data.gsm_only_not_allow_ps", "true");
                    }
                    return;
                }
            }
        }

        public void loadGID1() {
            HwSIMRecords.this.mFh.loadEFTransparent(28478, HwSIMRecords.this.handlerEx.obtainMessage(1));
            HwSIMRecords.this.mRecordsToLoad++;
        }

        public void loadGID1Ex() {
            if ((HwSIMRecords.this.mFh instanceof UsimFileHandler) && !"3F007FFF".equals(HwSIMRecords.this.mFh.getEFPath(28478))) {
                HwSIMRecords.this.mFh.loadEFTransparent("3F007FFF", 28478, HwSIMRecords.this.handlerEx.obtainMessage(3), true);
                HwSIMRecords.this.mRecordsToLoad++;
            }
        }

        public void loadVirtualNetSpecialFiles() {
            String homeNumeric = getHomeNumericAndSetRoaming();
            HwSIMRecords hwSIMRecords = HwSIMRecords.this;
            hwSIMRecords.log("GlobalChecker onOperatorNumericLoaded(): homeNumeric = " + homeNumeric);
            if (homeNumeric != null) {
                HwTelephonyFactory.getHwPhoneManager().loadVirtualNetSpecialFiles(homeNumeric, this.mSimRecords);
            } else {
                HwTelephonyFactory.getHwPhoneManager().loadVirtualNetSpecialFiles(HwSIMRecords.this.getOperatorNumeric(), this.mSimRecords);
            }
        }

        public void loadVirtualNet() {
            String homeNumeric = getHomeNumeric();
            HwSIMRecords hwSIMRecords = HwSIMRecords.this;
            hwSIMRecords.log("GlobalChecker onAllRecordsLoaded(): homeNumeric = " + homeNumeric);
            if (homeNumeric != null) {
                HwTelephonyFactory.getHwPhoneManager().loadVirtualNet(homeNumeric, this.mSimRecords);
            } else {
                HwTelephonyFactory.getHwPhoneManager().loadVirtualNet(HwSIMRecords.this.getOperatorNumeric(), this.mSimRecords);
            }
        }

        public String getHomeNumericAndSetRoaming() {
            if (HwSIMRecords.isMultiSimEnabled) {
                HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerOperator(HwSIMRecords.this.getOperatorNumeric(), HwSIMRecords.this.getSlotId());
                HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerImsi(HwSIMRecords.this.mImsi, Integer.valueOf(HwSIMRecords.this.getSlotId()));
                if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(HwSIMRecords.this.getSlotId()))) {
                    return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(HwSIMRecords.this.getSlotId()));
                }
                return null;
            }
            HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerOperator(HwSIMRecords.this.getOperatorNumeric());
            HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerImsi(HwSIMRecords.this.mImsi);
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
                return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
            }
            return null;
        }

        public String getHomeNumeric() {
            if (HwSIMRecords.isMultiSimEnabled) {
                if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(HwSIMRecords.this.getSlotId()))) {
                    return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(HwSIMRecords.this.getSlotId()));
                }
                return null;
            } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
                return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
            } else {
                return null;
            }
        }

        public void onIccIdLoadedHw() {
            if (HwSIMRecords.isMultiSimEnabled) {
                HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerIccId(HwSIMRecords.this.mIccId, HwSIMRecords.this.getSlotId());
            } else {
                HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerIccId(HwSIMRecords.this.mIccId);
            }
            HwTelephonyFactory.getHwPhoneManager().setMccTableIccId(HwSIMRecords.this.mIccId);
        }

        public void onImsiLoaded() {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                Rlog.d("SIMRecords", "onImsiLoaded mPhoneId = " + HwSIMRecords.this.getSlotId());
                if (HwSIMRecords.this.getSlotId() == 1 && TelephonyManager.getDefault().getSimState(0) == 5) {
                    return;
                }
            }
            HwTelephonyFactory.getHwPhoneManager().setMccTableImsi(HwSIMRecords.this.mImsi);
        }

        private void updateCarrierFileIfNeed() {
            if (HwSIMRecords.this.mHwCarrierCardManager != null) {
                HwSIMRecords.this.mHwCarrierCardManager.updateCarrierFileIfNeed(HwSIMRecords.this.getSlotId(), this.mSimRecords);
            }
        }
    }

    protected class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* access modifiers changed from: protected */
        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
            HwSIMRecords.this.mFdnRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
    }

    public HwSIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        initEventIdMap();
        this.mHwCustHwSIMRecords = (HwCustHwSIMRecords) HwCustUtils.createObj(HwCustHwSIMRecords.class, new Object[]{this, c});
        this.mHwCarrierCardManager = HwCarrierConfigCardManager.getDefault(c);
        this.mHwCarrierCardManager.reportIccRecordInstance(getSlotId(), this);
        if (getIccidSwitch()) {
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(getEventIdFromMap("EVENT_GET_ICCID_DONE")));
            } else {
                this.mFh.loadEFTransparent(12258, obtainMessage(getEventIdFromMap("EVENT_GET_ICCID_DONE")));
            }
            this.mRecordsToLoad++;
        }
        addIntentFilter(c);
    }

    private void addIntentFilter(Context c) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("com.huawei.action.CARRIER_CONFIG_CHANGED");
        c.registerReceiver(this.mIntentReceiver, filter);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0260, code lost:
        if (r0 != false) goto L_0x0262;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0262, code lost:
        onRecordLoaded();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x026e, code lost:
        if (0 == 0) goto L_0x0271;
     */
    public void handleMessage(Message msg) {
        int length;
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message while being destroyed. Ignoring.");
            return;
        }
        if (getEventIdFromMap("EVENT_GET_MBDN_DONE") == msg.what) {
            this.mVmConfig.setVoicemailOnSIM(null, null);
            HwSIMRecords.super.handleMessage(msg);
            this.mVmConfig.setVoicemailOnSIM(this.mVoiceMailNum, this.mVoiceMailTag);
        } else {
            try {
                int i = msg.what;
                if (i != 42) {
                    if (i == 201) {
                        isRecordLoadResponse = true;
                        AsyncResult ar = (AsyncResult) msg.obj;
                        log("EVENT_GET_ACTING_HPLMN_DONE");
                        if (ar.exception != null) {
                            loge("Exception in get acting hplmn " + ar.exception);
                        } else {
                            int[] mHplmnData = getSimPlmnDigits((byte[]) ar.result);
                            if (15 == mHplmnData[0]) {
                                this.mActingHplmn = "";
                            } else {
                                StringBuffer buffer = new StringBuffer();
                                if (15 == mHplmnData[5]) {
                                    length = 5;
                                } else {
                                    length = 6;
                                }
                                for (int i2 = 0; i2 < length; i2++) {
                                    buffer.append(mHplmnData[i2]);
                                }
                                this.mActingHplmn = buffer.toString();
                                log("length of mHplmnData =" + length + ", mActingHplmn = " + this.mActingHplmn);
                            }
                        }
                    } else if (i != EVENT_GET_PBR_DONE) {
                        switch (i) {
                            case EVENT_GET_ALL_OPL_RECORDS_DONE /*101*/:
                                isRecordLoadResponse = true;
                                AsyncResult ar2 = (AsyncResult) msg.obj;
                                if (ar2.exception == null) {
                                    this.mEons.setOplData((ArrayList) ar2.result);
                                    this.mRecordsEventsRegistrants.notifyResult(100);
                                    break;
                                } else {
                                    Rlog.e("SIMRecords", "[EONS] Exception in fetching OPL Records: " + ar2.exception);
                                    this.mEons.resetOplData();
                                    break;
                                }
                            case EVENT_GET_ALL_PNN_RECORDS_DONE /*102*/:
                                isRecordLoadResponse = true;
                                AsyncResult ar3 = (AsyncResult) msg.obj;
                                if (ar3.exception == null) {
                                    if (!IS_DELAY_UPDATENAME) {
                                        this.mEons.setPnnData((ArrayList) ar3.result);
                                        this.mRecordsEventsRegistrants.notifyResult(100);
                                        break;
                                    } else {
                                        this.mPnnRecords = new ArrayList<>();
                                        this.mPnnRecords = (ArrayList) ar3.result;
                                        break;
                                    }
                                } else {
                                    Rlog.e("SIMRecords", "[EONS] Exception in fetching PNN Records: " + ar3.exception);
                                    this.mEons.resetPnnData();
                                    break;
                                }
                            case EVENT_GET_SPN /*103*/:
                                isRecordLoadResponse = true;
                                AsyncResult ar4 = (AsyncResult) msg.obj;
                                if (ar4.exception == null) {
                                    byte[] data = (byte[]) ar4.result;
                                    this.mSpnDisplayCondition = 255 & data[0];
                                    String spn = IccUtils.adnStringFieldToString(data, 1, data.length - 1);
                                    setServiceProviderName(spn);
                                    setSystemProperty("gsm.sim.operator.alpha", spn);
                                    this.mRecordsEventsRegistrants.notifyResult(2);
                                    break;
                                } else {
                                    Rlog.e("SIMRecords", "[EONS] Exception in reading EF_SPN: " + ar4.exception);
                                    this.mSpnDisplayCondition = -1;
                                    break;
                                }
                            case EVENT_GET_SPN_CPHS_DONE /*104*/:
                                isRecordLoadResponse = true;
                                AsyncResult ar5 = (AsyncResult) msg.obj;
                                if (ar5.exception == null) {
                                    this.mEons.setCphsData(HwEons.CphsType.LONG, (byte[]) ar5.result);
                                    break;
                                } else {
                                    Rlog.e("SIMRecords", "[EONS] Exception in reading EF_SPN_CPHS: " + ar5.exception);
                                    this.mEons.resetCphsData(HwEons.CphsType.LONG);
                                    break;
                                }
                            case EVENT_GET_SPN_SHORT_CPHS_DONE /*105*/:
                                isRecordLoadResponse = true;
                                AsyncResult ar6 = (AsyncResult) msg.obj;
                                if (ar6.exception == null) {
                                    this.mEons.setCphsData(HwEons.CphsType.SHORT, (byte[]) ar6.result);
                                    break;
                                } else {
                                    Rlog.e("SIMRecords", "[EONS] Exception in reading EF_SPN_SHORT_CPHS: " + ar6.exception);
                                    this.mEons.resetCphsData(HwEons.CphsType.SHORT);
                                    break;
                                }
                            default:
                                HwSIMRecords.super.handleMessage(msg);
                                break;
                        }
                    } else {
                        isRecordLoadResponse = true;
                        AsyncResult ar7 = (AsyncResult) msg.obj;
                        if (ar7.exception == null) {
                            this.mIs3Gphonebook = true;
                        } else if ((ar7.exception instanceof CommandException) && CommandException.Error.SIM_ABSENT == ar7.exception.getCommandError()) {
                            this.mIsSimPowerDown = true;
                            log("Get PBR Done,mIsSimPowerDown: " + this.mIsSimPowerDown);
                        }
                        this.mIsGetPBRDone = true;
                        log("Get PBR Done,mIs3Gphonebook: " + this.mIs3Gphonebook);
                    }
                } else {
                    log("EVENT_GET_SIM_MATCHED_FILE_DONE");
                    isRecordLoadResponse = true;
                    onGetSimMatchedFileDone(msg);
                }
            } catch (RuntimeException exc) {
                logw("Exception parsing SIM record", exc);
            } catch (Throwable th) {
                if (0 != 0) {
                    onRecordLoaded();
                }
                throw th;
            }
        }
    }

    public void onReady() {
        HwSIMRecords.super.onReady();
        if (this.bNeedSendRefreshBC && HW_SIM_REFRESH) {
            this.bNeedSendRefreshBC = false;
            synchronized (this) {
                this.mIccRefreshRegistrants.notifyRegistrants();
            }
        }
    }

    public boolean beforeHandleSimRefresh(IccRefreshResponse refreshResponse) {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            ApnReminder.getInstance(this.mContext, getSlotId()).getCust().setSimRefreshingState(true);
        } else {
            ApnReminder.getInstance(this.mContext).getCust().setSimRefreshingState(true);
        }
        int slotId = HwAddonTelephonyFactory.getTelephony().getDefault4GSlotId();
        rlog("beforeHandleSimRefresh->getDefault4GSlotId, slotId: " + slotId);
        switch (refreshResponse.refreshResult) {
            case 0:
                rlog("beforeHandleSimRefresh with REFRESH_RESULT_FILE_UPDATE");
                if (HW_IS_CHINA_TELECOM && this.mParentApp != null && this.mParentApp.getUiccCard() == UiccController.getInstance().getUiccCard(slotId)) {
                    rlog("Do not handleSimRefresh with SIM_FILE_UPDATED sent by RUIM.");
                    return true;
                } else if (hwCustHandleSimRefresh(refreshResponse.efId)) {
                    return true;
                }
                break;
            case 1:
                rlog("beforeHandleSimRefresh with SIM_REFRESH_INIT");
                if (!HW_IS_CHINA_TELECOM || this.mParentApp == null || this.mParentApp.getUiccCard() != UiccController.getInstance().getUiccCard(slotId)) {
                    if (HW_SIM_REFRESH) {
                        this.bNeedSendRefreshBC = true;
                        break;
                    }
                } else {
                    rlog("Do not handleSimRefresh with REFRESH_RESULT_INIT sent by RUIM.");
                    return true;
                }
                break;
            case 2:
                rlog("beforeHandleSimRefresh with SIM_REFRESH_RESET");
                break;
            default:
                rlog("beforeHandleSimRefresh with unknown operation");
                break;
        }
        return false;
    }

    private boolean hwCustHandleSimRefresh(int efid) {
        int EFID;
        int i = 0;
        if (65535 == efid) {
            String strEFID = SystemProperties.get(pRefreshMultifileProp, "");
            rlog("The strEFID is: " + strEFID);
            if (strEFID.isEmpty()) {
                rlog("handleSimRefresh with no multifile found");
                return false;
            }
            SystemProperties.set(pRefreshMultifileProp, "");
            if (!SystemProperties.get(pRefreshMultifilePropExtra, "").isEmpty()) {
                rlog("The strEFIDExtra is: " + strEFIDExtra);
                strEFID = strEFID + ',' + strEFIDExtra;
                rlog("The strEFID is: " + strEFID);
                SystemProperties.set(pRefreshMultifilePropExtra, "");
            }
            strEFIDs = strEFID.split(",");
            rlog("strEFIDs.length()" + strEFIDs.length);
            while (i < strEFIDs.length) {
                try {
                    rlog("handleSimRefresh with strEFIDs[i]: " + strEFIDs[i]);
                    rlog("handleSimRefresh with EFID: " + EFID);
                    handleFileUpdate(EFID);
                } catch (NumberFormatException e) {
                    rlog("handleSimRefresh with convert EFID from String to Int error");
                }
                i++;
            }
            rlog("notify mIccRefreshRegistrants");
            synchronized (this) {
                this.mIccRefreshRegistrants.notifyRegistrants();
            }
            return true;
        }
        rlog("refresh with only one EF ID");
        return false;
    }

    public boolean afterHandleSimRefresh(IccRefreshResponse refreshResponse) {
        switch (refreshResponse.refreshResult) {
            case 0:
                rlog("afterHandleSimRefresh with REFRESH_RESULT_FILE_UPDATE");
                synchronized (this) {
                    this.mIccRefreshRegistrants.notifyRegistrants();
                }
                break;
            case 1:
                rlog("afterHandleSimRefresh with SIM_REFRESH_INIT");
                break;
            case 2:
                rlog("afterHandleSimRefresh with SIM_REFRESH_RESET");
                if (HW_SIM_REFRESH) {
                    this.bNeedSendRefreshBC = true;
                    break;
                }
                break;
            default:
                rlog("afterHandleSimRefresh with unknown operation");
                break;
        }
        return false;
    }

    protected static void rlog(String string) {
        Rlog.d(TAG, string);
    }

    public byte[] getGID1() {
        if (this.mEfGid1 != null) {
            return Arrays.copyOf(this.mEfGid1, this.mEfGid1.length);
        }
        return new byte[]{0};
    }

    public void setVoiceMailNumber(String voiceNumber) {
        this.mVoiceMailNum = voiceNumber;
    }

    public void loadFile(String matchPath, String matchFile) {
        if (matchPath != null && matchPath.length() >= 2) {
            if (matchPath.substring(0, 2).equalsIgnoreCase("0x") && matchFile != null && matchFile.length() >= 2 && matchFile.substring(0, 2).equalsIgnoreCase("0x")) {
                String matchFileString = matchFile.substring(2);
                int matchField = 0;
                int matchFileStringLength = matchFileString.length();
                for (int i = 0; i < matchFileStringLength; i++) {
                    matchField = (int) (((double) matchField) + (Math.pow(16.0d, (double) ((matchFileString.length() - i) - 1)) * ((double) HwIccUtils.hexCharToInt(matchFileString.charAt(i)))));
                }
                Message message = this.handlerEx.obtainMessage(2);
                Bundle data = new Bundle();
                data.putString(HwTelephony.VirtualNets.MATCH_PATH, matchPath);
                data.putString(HwTelephony.VirtualNets.MATCH_FILE, matchFile);
                message.setData(data);
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(matchPath.substring(2), matchField, message);
            }
        }
    }

    public boolean loadSpecialPathFile(String matchPath, String matchFile, int msgType) {
        if (matchPath == null || matchPath.length() < 2 || !matchPath.substring(0, 2).equalsIgnoreCase("0x") || matchFile == null || matchFile.length() < 2 || !matchFile.substring(0, 2).equalsIgnoreCase("0x")) {
            return false;
        }
        String matchFileString = matchFile.substring(2);
        int matchField = 0;
        int matchFileStringLength = matchFileString.length();
        for (int i = 0; i < matchFileStringLength; i++) {
            matchField = (int) (((double) matchField) + (Math.pow(16.0d, (double) ((matchFileString.length() - i) - 1)) * ((double) HwIccUtils.hexCharToInt(matchFileString.charAt(i)))));
        }
        Message message = this.handlerEx.obtainMessage(msgType);
        Bundle data = new Bundle();
        data.putString(HwTelephony.VirtualNets.MATCH_PATH, matchPath);
        data.putString(HwTelephony.VirtualNets.MATCH_FILE, matchFile);
        message.setData(data);
        this.mFh.loadEFTransparent(matchPath.substring(2), matchField, message);
        log("loadSpecialPathFile: matchPath:" + matchPath + " matchFile:" + matchFile + " msgType:" + msgType);
        return true;
    }

    public boolean loadCarrierFile(String matchPath, String matchFile) {
        return loadSpecialPathFile(matchPath, matchFile, 4);
    }

    /* access modifiers changed from: protected */
    public void onOperatorNumericLoadedHw() {
        this.globalChecker.onOperatorNumericLoaded();
        onImsiAndAdLoadedHw(this.mImsi);
    }

    /* access modifiers changed from: protected */
    public void onAllRecordsLoadedHw() {
        updateSarMnc(this.mImsi);
        this.globalChecker.onAllRecordsLoaded();
    }

    /* access modifiers changed from: protected */
    public void loadGID1() {
        this.globalChecker.loadGID1();
    }

    /* access modifiers changed from: protected */
    public void onIccIdLoadedHw() {
        this.globalChecker.onIccIdLoadedHw();
        processGetIccIdDone(this.mIccId);
        if (getIccidSwitch()) {
            sendIccidDoneBroadcast(this.mIccId);
        }
    }

    public void processGetIccIdDone(String iccid) {
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            HwHotplugController.getInstance().onHotplugIccIdChanged(iccid, getSlotId());
        }
        updateCarrierFile(getSlotId(), 1, iccid);
    }

    /* access modifiers changed from: protected */
    public void onImsiLoadedHw() {
        this.globalChecker.onImsiLoaded();
    }

    private void onImsiAndAdLoadedHw(String imsi) {
        String mccmnc;
        String rbImsi = null;
        String rbMccmnc = null;
        if (imsi != null) {
            if (3 == this.mMncLength) {
                mccmnc = imsi.substring(0, 6);
            } else {
                mccmnc = imsi.substring(0, 5);
            }
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(getSlotId()))) {
                rbImsi = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerImsi(Integer.valueOf(getSlotId()));
                rbMccmnc = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(getSlotId()));
            }
            if (rbImsi == null || rbImsi.length() <= 0 || rbMccmnc == null || rbMccmnc.length() <= 0) {
                updateCarrierFile(getSlotId(), 2, imsi);
                updateCarrierFile(getSlotId(), 3, mccmnc);
                return;
            }
            Rlog.d("SIMRecords", "Set RoamingBroker mccmnc=" + rbMccmnc);
            updateCarrierFile(getSlotId(), 2, rbImsi);
            updateCarrierFile(getSlotId(), 3, rbMccmnc);
        }
    }

    /* access modifiers changed from: protected */
    public void updateCarrierFile(int slotId, int fileType, String fileValue) {
        this.mHwCarrierCardManager.updateCarrierFile(slotId, fileType, fileValue);
    }

    /* access modifiers changed from: protected */
    public void custMncLength(String mcc) {
        String mncHaving2Digits = SystemProperties.get("ro.config.mnc_having_2digits", "");
        Rlog.d("SIMRecords", "mnc_having_2digits = " + mncHaving2Digits);
        if (mncHaving2Digits != null) {
            String custMccmncCode = this.mImsi.substring(0, 5);
            for (String plmn : mncHaving2Digits.split(",")) {
                if (custMccmncCode.equals(plmn)) {
                    this.mMncLength = 2;
                    return;
                }
            }
        } else if (mcc.equals("416") && 3 == this.mMncLength) {
            Rlog.d("SIMRecords", "SIMRecords: customize for Jordan sim card, make the mcnLength to 2");
            this.mMncLength = 2;
        }
    }

    public String getOperatorNumericEx(ContentResolver cr, String name) {
        if (cr == null || this.mImsi == null || "".equals(this.mImsi) || name == null || "".equals(name)) {
            return getOperatorNumeric();
        }
        String hwImsiPlmnEx = Settings.System.getString(cr, name);
        if (hwImsiPlmnEx != null && !"".equals(hwImsiPlmnEx)) {
            for (String plmn_item : hwImsiPlmnEx.split(",")) {
                if (this.mImsi.startsWith(plmn_item)) {
                    rlog("getOperatorNumericEx: " + plmn_item);
                    return plmn_item;
                }
            }
        }
        return getOperatorNumeric();
    }

    public String getVoiceMailNumber() {
        ApnReminder apnReminder;
        if (isMultiSimEnabled) {
            apnReminder = ApnReminder.getInstance(this.mContext, getSlotId());
        } else {
            apnReminder = ApnReminder.getInstance(this.mContext);
        }
        if (!apnReminder.isPopupApnSettingsEmpty()) {
            rlog("getVoiceMailNumber: PopupApnSettings not empty");
            if (this.mVmConfig != null) {
                this.mVmConfig.resetVoiceMailLoadFlag();
                long token = Binder.clearCallingIdentity();
                try {
                    setVoiceMailByCountry(getOperatorNumeric());
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
        return HwSIMRecords.super.getVoiceMailNumber();
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
        HwSIMRecords.super.resetRecords();
        this.mIs3Gphonebook = false;
        this.mIsGetPBRDone = false;
        this.mIsSimPowerDown = false;
        this.mSstPnnVaild = true;
        this.mPnnRecords = null;
    }

    /* access modifiers changed from: protected */
    public void getPbrRecordSize() {
        this.mFh.loadEFLinearFixedAll(20272, obtainMessage(EVENT_GET_PBR_DONE));
        this.mRecordsToLoad++;
    }

    public int getSlotId() {
        if (this.mParentApp != null && this.mParentApp.getUiccCard() != null) {
            return this.mParentApp.getUiccCard().getPhoneId();
        }
        log("error , mParentApp.getUiccCard  is null");
        return 0;
    }

    /* access modifiers changed from: protected */
    public void setVoiceMailByCountry(String spn) {
        log("setVoiceMailByCountry spn " + spn + " for slot" + getSlotId());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(getSlotId()))) {
                String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail(getSlotId());
                String spn2 = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(getSlotId()));
                if (this.mVmConfig.containsCarrier(spn2, getSlotId())) {
                    if (!TextUtils.isEmpty(number)) {
                        this.mIsVoiceMailFixed = true;
                        this.mVoiceMailNum = number;
                    } else {
                        this.mIsVoiceMailFixed = this.mVmConfig.getVoiceMailFixed(spn2, getSlotId());
                        this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn2, getSlotId());
                    }
                    this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn2, getSlotId());
                }
            } else if (this.mVmConfig.containsCarrier(spn, getSlotId())) {
                this.mIsVoiceMailFixed = this.mVmConfig.getVoiceMailFixed(spn, getSlotId());
                this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn, getSlotId());
                this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn, getSlotId());
            } else {
                log("VoiceMailConfig doesn't contains the carrier" + spn + " for slot" + getSlotId());
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String spn3 = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
            String number2 = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            String previousOp = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
            String spn4 = previousOp != null ? previousOp : spn3;
            if (this.mVmConfig.containsCarrier(spn4)) {
                if (!TextUtils.isEmpty(number2)) {
                    this.mIsVoiceMailFixed = true;
                    this.mVoiceMailNum = number2;
                } else {
                    this.mIsVoiceMailFixed = this.mVmConfig.getVoiceMailFixed(spn4);
                    this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn4);
                }
                this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn4);
            }
        } else {
            HwSIMRecords.super.setVoiceMailByCountry(spn);
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkFileInServiceTable(int efid, UsimServiceTable usimServiceTable, byte[] data) {
        boolean serviceStatus = true;
        rlog("check file status in serivce table " + efid);
        if (efid == 28486) {
            rlog("check EF_SPN serivice in serivice table!!");
            if (this.mParentApp.getUiccCard().isApplicationOnIcc(IccCardApplicationStatus.AppType.APPTYPE_USIM)) {
                if (usimServiceTable == null || usimServiceTable.isAvailable(UsimServiceTable.UsimService.SPN)) {
                    return true;
                }
                rlog("EF_SPN is disable in 3G card!!");
                return false;
            } else if (!this.mParentApp.getUiccCard().isApplicationOnIcc(IccCardApplicationStatus.AppType.APPTYPE_SIM)) {
                return true;
            } else {
                int mSstSpnValue = data[4] & 15 & 3;
                if (3 == mSstSpnValue) {
                    rlog("SST: 2G Sim,SPNVALUE enabled SPNVALUE = " + mSstSpnValue);
                    return true;
                }
                rlog("SST: 2G Sim,SPNVALUE disabled  SPNVALUE = " + mSstSpnValue);
                return false;
            }
        } else if (efid != 28613) {
            return true;
        } else {
            rlog("check EF_PNN serivice in serivice table!!");
            if (this.mParentApp.getUiccCard().isApplicationOnIcc(IccCardApplicationStatus.AppType.APPTYPE_USIM)) {
                if (usimServiceTable != null && !usimServiceTable.isAvailable(UsimServiceTable.UsimService.PLMN_NETWORK_NAME)) {
                    rlog("EF_PNN is disable in 3G or 4G card!!");
                    serviceStatus = false;
                }
            } else if (this.mParentApp.getUiccCard().isApplicationOnIcc(IccCardApplicationStatus.AppType.APPTYPE_SIM) && data != null && data.length > 12) {
                int mSstPnnValue = data[12] & 48;
                if (48 == mSstPnnValue) {
                    serviceStatus = true;
                    rlog("SST: 2G Sim,PNNVALUE enabled PnnVALUE = " + mSstPnnValue);
                } else {
                    serviceStatus = false;
                    rlog("SST: 2G Sim,PNNVALUE disabled  PnnVALUE = " + mSstPnnValue);
                }
            }
            this.mSstPnnVaild = serviceStatus;
            return serviceStatus;
        }
    }

    /* access modifiers changed from: protected */
    public void loadEons() {
        this.mFh.loadEFLinearFixedAll(28614, obtainMessage(EVENT_GET_ALL_OPL_RECORDS_DONE));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixedAll(28613, obtainMessage(EVENT_GET_ALL_PNN_RECORDS_DONE));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28436, obtainMessage(EVENT_GET_SPN_CPHS_DONE));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(28440, obtainMessage(EVENT_GET_SPN_SHORT_CPHS_DONE));
        this.mRecordsToLoad++;
    }

    public String getEons() {
        return this.mEons.getEons();
    }

    public boolean isEonsDisabled() {
        return this.mEons.isEonsDisabled();
    }

    public boolean updateEons(String regOperator, int lac) {
        return this.mEons.updateEons(regOperator, lac, getOperatorNumeric());
    }

    public ArrayList<OperatorInfo> getEonsForAvailableNetworks(ArrayList<OperatorInfo> avlNetworks) {
        return this.mEons.getEonsForAvailableNetworks(avlNetworks);
    }

    /* access modifiers changed from: protected */
    public void initFdnPsStatus(int slotId) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            new QueryHandler(this.mContext.getContentResolver()).startQuery(0, null, ContentUris.withAppendedId(Uri.parse("content://icc/fdn/subId/"), (long) slotId), new String[]{"number"}, null, null, null);
        }
    }

    public void sendDualSimChangeBroadcast(boolean isSimImsiRefreshing, String mLastImsi, String mImsi) {
        if (isSimImsiRefreshing && mLastImsi != null && mImsi != null && !mLastImsi.equals(mImsi)) {
            ActivityManagerNative.broadcastStickyIntent(new Intent("android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE"), null, 0);
            Rlog.d("SIMRecords", "dual sim imsi change");
        }
    }

    public void loadCardSpecialFile(int fileid) {
        String path;
        if (fileid != 20276) {
            Rlog.d("SIMRecords", "no fileid found for load");
        } else if (this.isEnsEnabled) {
            if (this.mFh instanceof UsimFileHandler) {
                path = "3F007FFF7F665F30";
                Rlog.d("SIMRecords", "EF_HPLMN in USIMFileHandler");
            } else if (this.mFh instanceof SIMFileHandler) {
                path = "3F007F665F30";
                Rlog.d("SIMRecords", "EF_HPLMN in SIMFileHandler");
            } else {
                path = this.mFh.getEFPath(20276);
                Rlog.d("SIMRecords", "EF_HPLMN in other FileHandler");
            }
            this.mFh.loadEFTransparent(path, 20276, obtainMessage(201), true);
            this.mRecordsToLoad++;
        }
    }

    public String getActingHplmn() {
        return this.mActingHplmn;
    }

    private int[] getSimPlmnDigits(byte[] data) {
        if (data == null) {
            return new int[]{15};
        }
        int[] simPlmn = {0, 0, 0, 0, 0, 0};
        simPlmn[0] = data[0] & 15;
        simPlmn[1] = (data[0] >> 4) & 15;
        simPlmn[2] = data[1] & 15;
        simPlmn[3] = data[2] & 15;
        simPlmn[4] = (data[2] >> 4) & 15;
        simPlmn[5] = (data[1] >> 4) & 15;
        return simPlmn;
    }

    /* access modifiers changed from: protected */
    public void refreshCardType() {
        if (this.mHwCustHwSIMRecords != null) {
            this.mHwCustHwSIMRecords.refreshCardType();
        }
    }

    /* access modifiers changed from: private */
    public boolean isNeedSetPnn() {
        if (this.mSstPnnVaild) {
            return true;
        }
        String mccmnc = getOperatorNumeric();
        String plmnsConfig = Settings.System.getString(this.mContext.getContentResolver(), "hw_sst_pnn_by_mccmnc");
        Rlog.d("SIMRecords", "isNeedSetPnn: mccmnc = " + mccmnc + " plmnsConfig = " + plmnsConfig);
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccmnc)) {
            return true;
        }
        String[] plmns = plmnsConfig.split(",");
        int length = plmns.length;
        int i = 0;
        while (i < length) {
            String plmn = plmns[i];
            if (plmn == null || !plmn.equals(mccmnc)) {
                i++;
            } else {
                Rlog.d("SIMRecords", "isNeedSetPnn: mccmnc = " + mccmnc + " no need set PNN from card.");
                return false;
            }
        }
        return true;
    }

    public boolean isHwCustDataRoamingOpenArea() {
        if (this.mHwCustHwSIMRecords != null) {
            return this.mHwCustHwSIMRecords.isHwCustDataRoamingOpenArea();
        }
        return false;
    }

    public void dispose() {
        log("Disposing HwSimRecords " + this);
        this.mHwCarrierCardManager.destory(getSlotId(), this);
        this.mContext.unregisterReceiver(this.mIntentReceiver);
        HwSIMRecords.super.dispose();
    }

    /* access modifiers changed from: protected */
    public void loadSimMatchedFileFromRilCache() {
        if (this.mCi != null) {
            this.mCi.getSimMatchedFileFromRilCache(28589, obtainMessage(42));
            this.mRecordsToLoad++;
            this.mCi.getSimMatchedFileFromRilCache(28472, obtainMessage(42));
            this.mRecordsToLoad++;
            this.mCi.getSimMatchedFileFromRilCache(28478, obtainMessage(42));
            this.mRecordsToLoad++;
            this.mCi.getSimMatchedFileFromRilCache(28479, obtainMessage(42));
            this.mRecordsToLoad++;
        }
    }

    /* access modifiers changed from: protected */
    public void onGetSimMatchedFileDone(Message msg) {
        if (msg != null) {
            AsyncResult asyncResult = (AsyncResult) msg.obj;
            AsyncResult ar = asyncResult;
            if (asyncResult != null) {
                IccIoResult resultEx = (IccIoResult) ar.result;
                int fileId = resultEx.getFileId();
                log("onGetSimMatchedFileDone: isValid=" + resultEx.isValidIccioResult() + ", fileId=0x" + Integer.toHexString(fileId));
                if (!resultEx.isValidIccioResult()) {
                    executOriginalSimIoRequest(fileId);
                    return;
                }
                Message response = obtainMessage(getOriginalSimIoEventId(fileId));
                if (!(fileId == 28436 || fileId == 28440 || fileId == 28472 || fileId == 28486)) {
                    if (fileId != 28589) {
                        switch (fileId) {
                            case 28478:
                            case 28479:
                                break;
                            default:
                                loge("onGetSimMatchedFileDone: do nothing for fileId = 0x" + Integer.toHexString(fileId));
                                break;
                        }
                    } else {
                        IccIoResult result = new IccIoResult(resultEx.sw1, resultEx.sw2, IccUtils.bytesToHexString(resultEx.payload));
                        this.mRecordsToLoad++;
                        AsyncResult.forMessage(response, result, ar.exception);
                    }
                    response.sendToTarget();
                    return;
                }
                byte[] data = resultEx.payload;
                this.mRecordsToLoad++;
                AsyncResult.forMessage(response, data, ar.exception);
                response.sendToTarget();
                return;
            }
        }
        loge("onGetSimMatchedFileDone: msg or AsyncResult is null, return.");
    }

    private void executOriginalSimIoRequest(int fileId) {
        log("executOriginalSimIoRequest for fileId = 0x" + Integer.toHexString(fileId));
        if (!(fileId == 28436 || fileId == 28440 || fileId == 28472 || fileId == 28486)) {
            if (fileId != 28589) {
                switch (fileId) {
                    case 28478:
                    case 28479:
                        break;
                    default:
                        loge("executOriginalSimIoRequest: do nothing for fileId=0x" + Integer.toHexString(fileId));
                        return;
                }
            } else {
                CommandsInterface commandsInterface = this.mCi;
                IccFileHandler iccFileHandler = this.mFh;
                commandsInterface.iccIOForApp(176, 28589, this.mFh.getEFPath(28589), 0, 0, 4, null, null, this.mParentApp.getAid(), obtainMessage(getOriginalSimIoEventId(fileId)));
                this.mRecordsToLoad++;
                return;
            }
        }
        this.mFh.loadEFTransparent(fileId, obtainMessage(getOriginalSimIoEventId(fileId)));
        this.mRecordsToLoad++;
    }

    private int getOriginalSimIoEventId(int fileId) {
        if (!(fileId == 28436 || fileId == 28440)) {
            if (fileId == 28472) {
                return getEventIdFromMap("EVENT_GET_SST_DONE");
            }
            if (fileId != 28486) {
                if (fileId == 28589) {
                    return getEventIdFromMap("EVENT_GET_AD_DONE");
                }
                switch (fileId) {
                    case 28478:
                        return getEventIdFromMap("EVENT_GET_GID1_DONE");
                    case 28479:
                        return getEventIdFromMap("EVENT_GET_GID2_DONE");
                    default:
                        loge("getOriginalSimIoEventId: Error, do nothing for fileId= 0x" + Integer.toHexString(fileId));
                        return -1;
                }
            }
        }
        return getEventIdFromMap("EVENT_GET_SPN_DONE");
    }

    /* access modifiers changed from: protected */
    public String getVmSimImsi() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        if (!sp.contains(SIM_IMSI + getSlotId())) {
            if (sp.contains(VM_SIM_IMSI + getSlotId())) {
                String imsi = sp.getString(VM_SIM_IMSI + getSlotId(), null);
                if (!(imsi == null || mPasswordUtil == null)) {
                    String oldDecodeVmSimImsi = mPasswordUtil.pswd2PlainText(imsi);
                    try {
                        imsi = new String(Base64.decode(imsi, 0), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        Rlog.e("SIMRecords", "getVmSimImsi UnsupportedEncodingException");
                    }
                    if (imsi.equals(this.mImsi) || oldDecodeVmSimImsi.equals(this.mImsi)) {
                        String imsi2 = this.mImsi;
                        Rlog.d("SIMRecords", "getVmSimImsi: Old IMSI encryption is not supported, now setVmSimImsi again.");
                        setVmSimImsi(imsi2);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.remove(VM_SIM_IMSI + getSlotId());
                        editor.commit();
                    }
                }
            }
        }
        String vmSimImsi = sp.getString(SIM_IMSI + getSlotId(), null);
        if (vmSimImsi == null) {
            return vmSimImsi;
        }
        try {
            return new String(Base64.decode(vmSimImsi, 0), "utf-8");
        } catch (IllegalArgumentException e2) {
            Rlog.e("SIMRecords", "getVmSimImsi IllegalArgumentException");
            return vmSimImsi;
        } catch (UnsupportedEncodingException e3) {
            Rlog.e("SIMRecords", "getVmSimImsi UnsupportedEncodingException");
            return vmSimImsi;
        }
    }

    /* access modifiers changed from: protected */
    public void setVmSimImsi(String imsi) {
        try {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            editor.putString(SIM_IMSI + getSlotId(), new String(Base64.encode(imsi.getBytes("utf-8"), 0), "utf-8"));
            editor.apply();
        } catch (UnsupportedEncodingException e) {
            Rlog.d("SIMRecords", "setVmSimImsi UnsupportedEncodingException");
        }
    }

    /* access modifiers changed from: protected */
    public void sendCspChangedBroadcast(boolean oldCspPlmnEnabled, boolean CspPlmnEnabled) {
        if (SystemProperties.getBoolean("ro.config.csp_enable", false) && oldCspPlmnEnabled != CspPlmnEnabled) {
            Intent intent = new Intent("android.intent.action.ACTION_HW_CSP_PLMN_CHANGE");
            intent.addFlags(536870912);
            intent.putExtra("state", this.mCspPlmnEnabled);
            this.mContext.sendBroadcast(intent);
            log("Broadcast, CSP Plmn Enabled change to " + this.mCspPlmnEnabled);
        }
    }

    /* access modifiers changed from: protected */
    public void adapterForDoubleRilChannelAfterImsiReady() {
        if (this.mImsi != null && this.mMncLength != 0 && this.mMncLength != -1) {
            log("EVENT_GET_IMSI_DONE, update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
            updateMccMncConfigWithGplmn(this.mImsi.substring(0, 3 + this.mMncLength));
            if (!this.mImsiLoad) {
                log("EVENT_GET_IMSI_DONE, trigger notifyGetAdDone and onOperatorNumericLoadedHw.");
                setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                setSystemProperty("persist.sys.mcc_match_fyrom", getOperatorNumeric());
                this.mImsiLoad = true;
                this.mParentApp.notifyGetAdDone(null);
                onOperatorNumericLoadedHw();
            }
        }
    }
}
