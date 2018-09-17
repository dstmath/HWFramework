package com.android.internal.telephony.uicc;

import android.app.ActivityManagerNative;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.HwTelephony.VirtualNets;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwAddonTelephonyFactory;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.HwVolteChrManagerImpl;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.ApnReminder;
import com.android.internal.telephony.gsm.HwEons;
import com.android.internal.telephony.gsm.HwEons.CphsType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.UsimServiceTable.UsimService;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;

public class HwSIMRecords extends SIMRecords {
    public static final String ANY_SIM_DETECTED = "any_sim_detect";
    private static final int EVENT_GET_ACTING_HPLMN_DONE = 201;
    private static final int EVENT_GET_ALL_OPL_RECORDS_DONE = 101;
    private static final int EVENT_GET_ALL_PNN_RECORDS_DONE = 102;
    private static final int EVENT_GET_GID1_HW_DONE = 1;
    private static final int EVENT_GET_GID1_HW_DONE_EX = 3;
    private static final int EVENT_GET_PBR_DONE = 233;
    private static final int EVENT_GET_SPECIAL_FILE_DONE = 2;
    private static final int EVENT_GET_SPN = 103;
    private static final int EVENT_GET_SPN_CPHS_DONE = 104;
    private static final int EVENT_GET_SPN_SHORT_CPHS_DONE = 105;
    private static final int EVENT_HW_CUST_BASE = 100;
    private static final boolean IS_DELAY_UPDATENAME = SystemProperties.getBoolean("ro.config.delay_updatename", false);
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    public static final String MULTI_PDP_PLMN_MATCHED = "multi_pdp_plmn_matched";
    private static final int SST_PNN_ENABLED = 48;
    private static final int SST_PNN_MASK = 48;
    private static final int SST_PNN_OFFSET = 12;
    private static final String TAG = "HwSIMRecords";
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static final String pRefreshMultifileProp = "gsm.sim.refresh.multifile";
    private static final String pRefreshMultifilePropExtra = "gsm.sim.refresh.multifile.extra";
    private static SIMRecordsUtils simRecordsUtils = ((SIMRecordsUtils) EasyInvokeFactory.getInvokeUtils(SIMRecordsUtils.class));
    private static String[] strEFIDs = new String[30];
    private static UiccCardApplicationUtils uiccCardApplicationUtils = new UiccCardApplicationUtils();
    protected boolean bNeedSendRefreshBC = false;
    private GlobalChecker globalChecker = new GlobalChecker(this);
    private Handler handlerEx = new Handler() {
        public void handleMessage(Message msg) {
            boolean isRecordLoadResponse = false;
            if (HwSIMRecords.this.mDestroyed.get()) {
                HwSIMRecords.this.loge("Received message " + msg + "[" + msg.what + "] " + " while being destroyed. Ignoring.");
                return;
            }
            try {
                AsyncResult ar;
                switch (msg.what) {
                    case 1:
                        isRecordLoadResponse = true;
                        ar = msg.obj;
                        if (ar.exception == null) {
                            HwSIMRecords.this.mEfGid1 = (byte[]) ar.result;
                            HwSIMRecords.this.log("mEfGid1: " + IccUtils.bytesToHexString(HwSIMRecords.this.mEfGid1));
                            break;
                        }
                        HwSIMRecords.this.log("Get GID1 failed, the exception: " + ar.exception);
                        HwSIMRecords.this.globalChecker.loadGID1Ex();
                        break;
                    case 2:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            Bundle bundle = msg.getData();
                            String filePath = bundle.getString(VirtualNets.MATCH_PATH);
                            String fileId = bundle.getString(VirtualNets.MATCH_FILE);
                            byte[] bytes = ar.result;
                            if (HwSIMRecords.isMultiSimEnabled) {
                                HwTelephonyFactory.getHwPhoneManager().addVirtualNetSpecialFile(filePath, fileId, bytes, HwSIMRecords.this.getSlotId());
                            } else {
                                HwTelephonyFactory.getHwPhoneManager().addVirtualNetSpecialFile(filePath, fileId, bytes);
                            }
                            if (HwSIMRecords.this.mHwCustHwSIMRecords != null) {
                                HwSIMRecords.this.mHwCustHwSIMRecords.addHwVirtualNetSpecialFiles(filePath, fileId, bytes, HwSIMRecords.this.getSlotId());
                            }
                            HwSIMRecords.this.log("load Specifile: " + filePath + " " + fileId + " = " + IccUtils.bytesToHexString(HwSIMRecords.this.mEfGid1));
                            break;
                        }
                        break;
                    case 3:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            HwSIMRecords.this.mEfGid1 = (byte[]) ar.result;
                            HwSIMRecords.this.log("mEfGid1_ex: " + IccUtils.bytesToHexString(HwSIMRecords.this.mEfGid1));
                            break;
                        }
                        HwSIMRecords.this.log("Get GID1_EX failed, the exception: " + ar.exception);
                        break;
                    default:
                        HwSIMRecords.this.log("unknown Event: " + msg.what);
                        break;
                }
                if (isRecordLoadResponse) {
                    HwSIMRecords.this.onRecordLoaded();
                }
            } catch (RuntimeException exc) {
                HwSIMRecords.this.logw("Exception parsing SIM record", exc);
                if (isRecordLoadResponse) {
                    HwSIMRecords.this.onRecordLoaded();
                }
            } catch (Throwable th) {
                if (isRecordLoadResponse) {
                    HwSIMRecords.this.onRecordLoaded();
                }
            }
        }
    };
    private boolean isEnsEnabled = SystemProperties.getBoolean("ro.config.hw_is_ens_enabled", false);
    private String mActingHplmn = "";
    byte[] mEfGid1 = null;
    HwEons mEons = new HwEons();
    private HwCustHwSIMRecords mHwCustHwSIMRecords;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction())) {
                    HwSIMRecords.this.log("Receives ACTION_SET_RADIO_CAPABILITY_DONE on slot " + HwSIMRecords.this.getSlotId());
                    boolean bNeedFetchRecords = (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT && HwSIMRecords.this.mIsSimPowerDown && HwSIMRecords.this.mParentApp != null) ? AppState.APPSTATE_READY == HwSIMRecords.this.mParentApp.getState() : false;
                    if (bNeedFetchRecords) {
                        HwSIMRecords.this.log("fetchSimRecords again.");
                        HwSIMRecords.this.mIsSimPowerDown = false;
                        HwSIMRecords.this.fetchSimRecords();
                    }
                }
            }
        }
    };
    private boolean mIsSimPowerDown = false;
    private ArrayList<byte[]> mPnnRecords = null;
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
            int hasTwoCard = 1;
            if (lSimSlotVal == 1 && lDataVal == 0) {
                hasTwoCard = TelephonyManager.getDefault().hasIccCard(lDataVal);
            }
            if (lSimSlotVal == 0) {
                SystemProperties.set("gsm.huawei.RemindDataService", "false");
            } else if (1 == lSimSlotVal) {
                SystemProperties.set("gsm.huawei.RemindDataService_1", "false");
            }
            String plmnsConfig = Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), "plmn_remind_data_service");
            if (plmnsConfig == null) {
                plmnsConfig = "26006,26003";
            }
            for (String plmn : plmnsConfig.split(",")) {
                if (plmn != null && plmn.equals(HwSIMRecords.this.getOperatorNumeric())) {
                    if (!"true".equals(System.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED)) && (lDataVal == lSimSlotVal || (hasTwoCard ^ 1) != 0)) {
                        ((TelephonyManager) HwSIMRecords.this.mContext.getSystemService("phone")).setDataEnabled(false);
                        System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
                    }
                    if (lSimSlotVal == 0) {
                        SystemProperties.set("gsm.huawei.RemindDataService", "true");
                    } else if (1 == lSimSlotVal) {
                        SystemProperties.set("gsm.huawei.RemindDataService_1", "true");
                    }
                }
            }
        }

        public void onAllRecordsLoaded() {
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
                    HwSIMRecords.this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(100));
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
                    String plmnsConfig = System.getString(HwSIMRecords.this.mContext.getContentResolver(), "disable_mobile_clatd");
                    if (TextUtils.isEmpty(plmnsConfig) || (TextUtils.isEmpty(mccMnc) ^ 1) == 0) {
                        Rlog.d("SIMRecords", "plmnsConfig is null, return");
                    } else if (plmnsConfig.contains(mccMnc)) {
                        Rlog.d("SIMRecords", "disable clatd!");
                        SystemProperties.set("gsm.net.doxlat", "false");
                    } else {
                        SystemProperties.set("gsm.net.doxlat", "true");
                    }
                } catch (Exception e) {
                    HwSIMRecords.this.loge("Exception e = " + e);
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
            intent.putExtra("imsi", imsi);
            if (!(!TelephonyManager.getDefault().isMultiSimEnabled() || HwSIMRecords.this.mParentApp == null || HwSIMRecords.this.mParentApp.getUiccCard() == null)) {
                int[] subId = SubscriptionManager.getSubId(HwSIMRecords.this.mParentApp.getUiccCard().getPhoneId());
                if (subId != null && subId.length > 0) {
                    SubscriptionManager.putPhoneIdAndSubIdExtra(intent, SubscriptionManager.getPhoneId(subId[0]));
                }
            }
            ActivityManagerNative.broadcastStickyIntent(intent, null, 0);
        }

        private void checkMultiPdpConfig() {
            String plmnsConfig = Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.MULTI_PDP_PLMN_MATCHED);
            Rlog.d("SIMRecords", "checkMultiPdpConfig plmnsConfig = " + plmnsConfig);
            if (plmnsConfig != null) {
                String[] plmns = plmnsConfig.split(",");
                int i = 0;
                int length = plmns.length;
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
            String plmnsConfig = Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), "plmn_remind_data_service");
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
                    if (!"true".equals(System.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED))) {
                        ((TelephonyManager) HwSIMRecords.this.mContext.getSystemService("phone")).setDataEnabled(false);
                    }
                    System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
                    SystemProperties.set("gsm.huawei.RemindDataService", "true");
                    return;
                }
            }
            System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
        }

        private void checkGsmOnlyDataNotAllowed() {
            if (HwSIMRecords.isMultiSimEnabled) {
                int[] subIds = SubscriptionManager.getSubId(HwSIMRecords.this.getSlotId());
                if (subIds != null) {
                    TelephonyManager.setTelephonyProperty(subIds[0], "gsm.data.gsm_only_not_allow_ps", "false");
                } else {
                    return;
                }
            }
            SystemProperties.set("gsm.data.gsm_only_not_allow_ps", "false");
            String plmnGsmonlyPsNotallowd = System.getString(HwSIMRecords.this.mContext.getContentResolver(), "hw_2gonly_psnotallowed");
            if (plmnGsmonlyPsNotallowd == null || "".equals(plmnGsmonlyPsNotallowd)) {
                plmnGsmonlyPsNotallowd = "23410";
            }
            String hplmn = HwSIMRecords.this.getOperatorNumeric();
            if (hplmn == null || "".equals(hplmn)) {
                HwSIMRecords.this.log("is2GonlyPsAllowed home plmn not ready");
                return;
            }
            String[] plmnCustomArray = plmnGsmonlyPsNotallowd.split(",");
            int regplmnCustomArrayLen = plmnCustomArray.length;
            for (int i = 0; i < regplmnCustomArrayLen; i++) {
                HwSIMRecords.this.log("is2GonlyPsAllowed plmnCustomArray[" + i + "] = " + plmnCustomArray[i]);
                if (hplmn.equals(plmnCustomArray[i])) {
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
            HwSIMRecords hwSIMRecords = HwSIMRecords.this;
            hwSIMRecords.mRecordsToLoad++;
        }

        public void loadGID1Ex() {
            if ((HwSIMRecords.this.mFh instanceof UsimFileHandler) && ("3F007FFF".equals(HwSIMRecords.this.mFh.getEFPath(28478)) ^ 1) != 0) {
                HwSIMRecords.this.mFh.loadEFTransparent("3F007FFF", 28478, HwSIMRecords.this.handlerEx.obtainMessage(3), true);
                HwSIMRecords hwSIMRecords = HwSIMRecords.this;
                hwSIMRecords.mRecordsToLoad++;
            }
        }

        public void loadVirtualNetSpecialFiles() {
            String homeNumeric = getHomeNumericAndSetRoaming();
            HwSIMRecords.this.log("GlobalChecker onOperatorNumericLoaded(): homeNumeric = " + homeNumeric);
            if (homeNumeric != null) {
                HwTelephonyFactory.getHwPhoneManager().loadVirtualNetSpecialFiles(homeNumeric, this.mSimRecords);
            } else {
                HwTelephonyFactory.getHwPhoneManager().loadVirtualNetSpecialFiles(HwSIMRecords.this.getOperatorNumeric(), this.mSimRecords);
            }
        }

        public void loadVirtualNet() {
            String homeNumeric = getHomeNumeric();
            HwSIMRecords.this.log("GlobalChecker onAllRecordsLoaded(): homeNumeric = " + homeNumeric);
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
    }

    protected class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            HwSIMRecords.this.mFdnRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
    }

    public HwSIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mHwCustHwSIMRecords = (HwCustHwSIMRecords) HwCustUtils.createObj(HwCustHwSIMRecords.class, new Object[]{this, c});
        if (getIccidSwitch()) {
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(simRecordsUtils.getEventIccidDone(null)));
            } else {
                this.mFh.loadEFTransparent(12258, obtainMessage(simRecordsUtils.getEventIccidDone(null)));
            }
            this.mRecordsToLoad++;
        }
        addIntentFilter(c);
    }

    private void addIntentFilter(Context c) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        c.registerReceiver(this.mIntentReceiver, filter);
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message while being destroyed. Ignoring.");
            return;
        }
        if (simRecordsUtils.getEventMbdnDone(null) == msg.what) {
            this.mVmConfig.setVoicemailOnSIM(null, null);
            super.handleMessage(msg);
            this.mVmConfig.setVoicemailOnSIM(this.mVoiceMailNum, this.mVoiceMailTag);
        } else {
            try {
                AsyncResult ar;
                switch (msg.what) {
                    case EVENT_GET_ALL_OPL_RECORDS_DONE /*101*/:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            this.mEons.setOplData((ArrayList) ar.result);
                            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(100));
                            break;
                        }
                        Rlog.e("SIMRecords", "[EONS] Exception in fetching OPL Records: " + ar.exception);
                        this.mEons.resetOplData();
                        break;
                    case EVENT_GET_ALL_PNN_RECORDS_DONE /*102*/:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            if (!IS_DELAY_UPDATENAME) {
                                this.mEons.setPnnData((ArrayList) ar.result);
                                this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(100));
                                break;
                            }
                            this.mPnnRecords = new ArrayList();
                            this.mPnnRecords = (ArrayList) ar.result;
                            break;
                        }
                        Rlog.e("SIMRecords", "[EONS] Exception in fetching PNN Records: " + ar.exception);
                        this.mEons.resetPnnData();
                        break;
                    case EVENT_GET_SPN /*103*/:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            byte[] data = (byte[]) ar.result;
                            this.mSpnDisplayCondition = data[0] & HwSubscriptionManager.SUB_INIT_STATE;
                            String spn = IccUtils.adnStringFieldToString(data, 1, data.length - 1);
                            setServiceProviderName(spn);
                            setSystemProperty("gsm.sim.operator.alpha", spn);
                            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(2));
                            break;
                        }
                        Rlog.e("SIMRecords", "[EONS] Exception in reading EF_SPN: " + ar.exception);
                        this.mSpnDisplayCondition = -1;
                        break;
                    case EVENT_GET_SPN_CPHS_DONE /*104*/:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            this.mEons.setCphsData(CphsType.LONG, (byte[]) ar.result);
                            break;
                        }
                        Rlog.e("SIMRecords", "[EONS] Exception in reading EF_SPN_CPHS: " + ar.exception);
                        this.mEons.resetCphsData(CphsType.LONG);
                        break;
                    case EVENT_GET_SPN_SHORT_CPHS_DONE /*105*/:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            this.mEons.setCphsData(CphsType.SHORT, (byte[]) ar.result);
                            break;
                        }
                        Rlog.e("SIMRecords", "[EONS] Exception in reading EF_SPN_SHORT_CPHS: " + ar.exception);
                        this.mEons.resetCphsData(CphsType.SHORT);
                        break;
                    case EVENT_GET_ACTING_HPLMN_DONE /*201*/:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        log("EVENT_GET_ACTING_HPLMN_DONE");
                        if (ar.exception == null) {
                            int[] mHplmnData = getSimPlmnDigits((byte[]) ar.result);
                            if (15 != mHplmnData[0]) {
                                int length;
                                StringBuffer buffer = new StringBuffer();
                                if (15 == mHplmnData[5]) {
                                    length = 5;
                                } else {
                                    length = 6;
                                }
                                for (int i = 0; i < length; i++) {
                                    buffer.append(mHplmnData[i]);
                                }
                                this.mActingHplmn = buffer.toString();
                                log("length of mHplmnData =" + length + ", " + "mActingHplmn = " + this.mActingHplmn);
                                break;
                            }
                            this.mActingHplmn = "";
                            break;
                        }
                        loge("Exception in get acting hplmn " + ar.exception);
                        break;
                    case EVENT_GET_PBR_DONE /*233*/:
                        isRecordLoadResponse = true;
                        ar = msg.obj;
                        if (ar.exception == null) {
                            this.mIs3Gphonebook = true;
                        } else if ((ar.exception instanceof CommandException) && Error.SIM_ABSENT == ((CommandException) ar.exception).getCommandError()) {
                            this.mIsSimPowerDown = true;
                            log("Get PBR Done,mIsSimPowerDown: " + this.mIsSimPowerDown);
                        }
                        this.mIsGetPBRDone = true;
                        log("Get PBR Done,mIs3Gphonebook: " + this.mIs3Gphonebook);
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
                if (isRecordLoadResponse) {
                    onRecordLoaded();
                }
            } catch (RuntimeException exc) {
                logw("Exception parsing SIM record", exc);
                if (isRecordLoadResponse) {
                    onRecordLoaded();
                }
            } catch (Throwable th) {
                if (isRecordLoadResponse) {
                    onRecordLoaded();
                }
            }
        }
    }

    public void onReady() {
        super.onReady();
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
                if (HW_IS_CHINA_TELECOM && this.mParentApp != null && uiccCardApplicationUtils.getUiccCard(this.mParentApp) == UiccController.getInstance().getUiccCard(slotId)) {
                    rlog("Do not handleSimRefresh with SIM_FILE_UPDATED sent by RUIM.");
                    return true;
                } else if (hwCustHandleSimRefresh(refreshResponse.efId)) {
                    return true;
                }
                break;
            case 1:
                rlog("beforeHandleSimRefresh with SIM_REFRESH_INIT");
                if (!HW_IS_CHINA_TELECOM || this.mParentApp == null || uiccCardApplicationUtils.getUiccCard(this.mParentApp) != UiccController.getInstance().getUiccCard(slotId)) {
                    if (HW_SIM_REFRESH) {
                        this.bNeedSendRefreshBC = true;
                        break;
                    }
                }
                rlog("Do not handleSimRefresh with REFRESH_RESULT_INIT sent by RUIM.");
                return true;
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
        if (HwVolteChrManagerImpl.MAX_MONITOR_TIME == efid) {
            String strEFID = SystemProperties.get(pRefreshMultifileProp, "");
            rlog("The strEFID is: " + strEFID);
            if (strEFID.isEmpty()) {
                rlog("handleSimRefresh with no multifile found");
                return false;
            }
            SystemProperties.set(pRefreshMultifileProp, "");
            String strEFIDExtra = SystemProperties.get(pRefreshMultifilePropExtra, "");
            if (!strEFIDExtra.isEmpty()) {
                rlog("The strEFIDExtra is: " + strEFIDExtra);
                strEFID = strEFID + ',' + strEFIDExtra;
                rlog("The strEFID is: " + strEFID);
                SystemProperties.set(pRefreshMultifilePropExtra, "");
            }
            strEFIDs = strEFID.split(",");
            rlog("strEFIDs.length()" + strEFIDs.length);
            for (int i = 0; i < strEFIDs.length; i++) {
                try {
                    rlog("handleSimRefresh with strEFIDs[i]: " + strEFIDs[i]);
                    int EFID = Integer.parseInt(strEFIDs[i], 16);
                    rlog("handleSimRefresh with EFID: " + EFID);
                    handleFileUpdate(EFID);
                } catch (NumberFormatException e) {
                    rlog("handleSimRefresh with convert EFID from String to Int error");
                }
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
        return new byte[]{(byte) 0};
    }

    public void setVoiceMailNumber(String voiceNumber) {
        this.mVoiceMailNum = voiceNumber;
    }

    public void loadFile(String matchPath, String matchFile) {
        if (matchPath != null && matchPath.length() >= 2 && (matchPath.substring(0, 2).equalsIgnoreCase("0x") ^ 1) == 0 && matchFile != null && matchFile.length() >= 2 && (matchFile.substring(0, 2).equalsIgnoreCase("0x") ^ 1) == 0) {
            String matchFileString = matchFile.substring(2);
            int matchField = 0;
            for (int i = 0; i < matchFileString.length(); i++) {
                matchField = (int) (((double) matchField) + (Math.pow(16.0d, (double) ((matchFileString.length() - i) - 1)) * ((double) HwIccUtils.hexCharToInt(matchFileString.charAt(i)))));
            }
            Message message = this.handlerEx.obtainMessage(2);
            Bundle data = new Bundle();
            data.putString(VirtualNets.MATCH_PATH, matchPath);
            data.putString(VirtualNets.MATCH_FILE, matchFile);
            message.setData(data);
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(matchPath.substring(2), matchField, message);
        }
    }

    protected void onOperatorNumericLoadedHw() {
        this.globalChecker.onOperatorNumericLoaded();
    }

    protected void onAllRecordsLoadedHw() {
        this.globalChecker.onAllRecordsLoaded();
    }

    protected void loadGID1() {
        this.globalChecker.loadGID1();
    }

    protected void onIccIdLoadedHw() {
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
    }

    protected void onImsiLoadedHw() {
        this.globalChecker.onImsiLoaded();
    }

    protected void custMncLength(String mcc) {
        int i = 0;
        String mncHaving2Digits = SystemProperties.get("ro.config.mnc_having_2digits", "");
        Rlog.d("SIMRecords", "mnc_having_2digits = " + mncHaving2Digits);
        if (mncHaving2Digits != null) {
            String custMccmncCode = this.mImsi.substring(0, 5);
            String[] plmns = mncHaving2Digits.split(",");
            int length = plmns.length;
            while (i < length) {
                if (custMccmncCode.equals(plmns[i])) {
                    this.mMncLength = 2;
                    return;
                }
                i++;
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
        String hwImsiPlmnEx = System.getString(cr, name);
        if (!(hwImsiPlmnEx == null || ("".equals(hwImsiPlmnEx) ^ 1) == 0)) {
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
                simRecordsUtils.setVoiceMailByCountry(this, getOperatorNumeric());
            }
        }
        return super.getVoiceMailNumber();
    }

    protected void resetRecords() {
        super.resetRecords();
        this.mIs3Gphonebook = false;
        this.mIsGetPBRDone = false;
        this.mIsSimPowerDown = false;
        this.mSstPnnVaild = true;
        this.mPnnRecords = null;
    }

    protected void getPbrRecordSize() {
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

    protected void setVoiceMailByCountry(String spn) {
        log("setVoiceMailByCountry spn " + spn + " for slot" + getSlotId());
        String number;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(getSlotId()))) {
                number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail(getSlotId());
                spn = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(getSlotId()));
                if (this.mVmConfig.containsCarrier(spn, getSlotId())) {
                    if (TextUtils.isEmpty(number)) {
                        this.mIsVoiceMailFixed = this.mVmConfig.getVoiceMailFixed(spn, getSlotId());
                        this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn, getSlotId());
                    } else {
                        this.mIsVoiceMailFixed = true;
                        this.mVoiceMailNum = number;
                    }
                    this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn, getSlotId());
                }
            } else if (this.mVmConfig.containsCarrier(spn, getSlotId())) {
                this.mIsVoiceMailFixed = this.mVmConfig.getVoiceMailFixed(spn, getSlotId());
                this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn, getSlotId());
                this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn, getSlotId());
            } else {
                log("VoiceMailConfig doesn't contains the carrier" + spn + " for slot" + getSlotId());
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            spn = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
            number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            String previousOp = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
            if (previousOp != null) {
                spn = previousOp;
            }
            if (this.mVmConfig.containsCarrier(spn)) {
                if (TextUtils.isEmpty(number)) {
                    this.mIsVoiceMailFixed = this.mVmConfig.getVoiceMailFixed(spn);
                    this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn);
                } else {
                    this.mIsVoiceMailFixed = true;
                    this.mVoiceMailNum = number;
                }
                this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn);
            }
        } else {
            super.setVoiceMailByCountry(spn);
        }
    }

    protected boolean checkFileInServiceTable(int efid, UsimServiceTable usimServiceTable, byte[] data) {
        boolean serviceStatus = true;
        rlog("check file status in serivce table " + efid);
        switch (efid) {
            case 28486:
                rlog("check EF_SPN serivice in serivice table!!");
                if (this.mParentApp.getUiccCard().isApplicationOnIcc(AppType.APPTYPE_USIM)) {
                    if (usimServiceTable == null || (usimServiceTable.isAvailable(UsimService.SPN) ^ 1) == 0) {
                        return true;
                    }
                    rlog("EF_SPN is disable in 3G card!!");
                    return false;
                } else if (!this.mParentApp.getUiccCard().isApplicationOnIcc(AppType.APPTYPE_SIM)) {
                    return true;
                } else {
                    int mSstSpnValue = (data[4] & 15) & 3;
                    if (3 == mSstSpnValue) {
                        rlog("SST: 2G Sim,SPNVALUE enabled SPNVALUE = " + mSstSpnValue);
                        return true;
                    }
                    rlog("SST: 2G Sim,SPNVALUE disabled  SPNVALUE = " + mSstSpnValue);
                    return false;
                }
            case 28613:
                rlog("check EF_PNN serivice in serivice table!!");
                if (this.mParentApp.getUiccCard().isApplicationOnIcc(AppType.APPTYPE_USIM)) {
                    if (!(usimServiceTable == null || (usimServiceTable.isAvailable(UsimService.PLMN_NETWORK_NAME) ^ 1) == 0)) {
                        rlog("EF_PNN is disable in 3G or 4G card!!");
                        serviceStatus = false;
                    }
                } else if (this.mParentApp.getUiccCard().isApplicationOnIcc(AppType.APPTYPE_SIM) && data != null && data.length > 12) {
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
            default:
                return true;
        }
    }

    protected void loadEons() {
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

    protected void initFdnPsStatus(int slotId) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            new QueryHandler(this.mContext.getContentResolver()).startQuery(0, null, ContentUris.withAppendedId(Uri.parse("content://icc/fdn/subId/"), (long) slotId), new String[]{"number"}, null, null, null);
        }
    }

    public void sendDualSimChangeBroadcast(boolean isSimImsiRefreshing, String mLastImsi, String mImsi) {
        if (isSimImsiRefreshing && mLastImsi != null && mImsi != null && (mLastImsi.equals(mImsi) ^ 1) != 0) {
            ActivityManagerNative.broadcastStickyIntent(new Intent("android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE"), null, 0);
            Rlog.d("SIMRecords", "dual sim imsi change");
        }
    }

    public void loadCardSpecialFile(int fileid) {
        switch (fileid) {
            case 20276:
                if (this.isEnsEnabled) {
                    this.mFh.loadEFTransparent(20276, obtainMessage(EVENT_GET_ACTING_HPLMN_DONE));
                    this.mRecordsToLoad++;
                    return;
                }
                return;
            default:
                Rlog.d("SIMRecords", "no fileid found for load");
                return;
        }
    }

    public String getActingHplmn() {
        return this.mActingHplmn;
    }

    private int[] getSimPlmnDigits(byte[] data) {
        if (data == null) {
            return new int[]{15};
        }
        int[] simPlmn = new int[]{0, 0, 0, 0, 0, 0};
        simPlmn[0] = data[0] & 15;
        simPlmn[1] = (data[0] >> 4) & 15;
        simPlmn[2] = data[1] & 15;
        simPlmn[3] = data[2] & 15;
        simPlmn[4] = (data[2] >> 4) & 15;
        simPlmn[5] = (data[1] >> 4) & 15;
        return simPlmn;
    }

    protected void refreshCardType() {
        if (this.mHwCustHwSIMRecords != null) {
            this.mHwCustHwSIMRecords.refreshCardType();
        }
    }

    private boolean isNeedSetPnn() {
        if (this.mSstPnnVaild) {
            return true;
        }
        String mccmnc = getOperatorNumeric();
        String plmnsConfig = System.getString(this.mContext.getContentResolver(), "hw_sst_pnn_by_mccmnc");
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
}
