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
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwCarrierConfigCardManager;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.HwTelephonyPropertiesInner;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.ApnReminder;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.gsm.HwEons;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.MccTableEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import libcore.util.EmptyArray;

public class HwSIMRecords extends HwIccRecordsEx {
    public static final String ANY_SIM_DETECTED = "any_sim_detect";
    private static final int EVENT_GET_ACTING_HPLMN_DONE = 201;
    private static final int EVENT_GET_ALL_OPL_RECORDS_DONE = 101;
    private static final int EVENT_GET_ALL_PNN_RECORDS_DONE = 102;
    private static final int EVENT_GET_CARRIER_FILE_DONE = 4;
    private static final int EVENT_GET_GID1_HW_DONE = 1;
    private static final int EVENT_GET_GID1_HW_DONE_EX = 3;
    private static final int EVENT_GET_PBR_DONE = 233;
    private static final int EVENT_GET_SIM_MATCHED_FILE_DONE = 106;
    private static final int EVENT_GET_SPN = 103;
    private static final int EVENT_GET_SPN_CPHS_DONE = 104;
    private static final int EVENT_GET_SPN_SHORT_CPHS_DONE = 105;
    private static final int EVENT_HW_CUST_BASE = 100;
    private static final String HW_ACTION_APN_REMINDER_NOTIFY = "com.huawei.action.NOTIFY_APN_REMINDER";
    private static final int INVALID_PHONEID = -1;
    private static final boolean IS_DELAY_UPDATENAME = SystemProperties.getBoolean("ro.config.delay_updatename", false);
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    private static final String MULTI_PDP_PLMN_MATCHED = "multi_pdp_plmn_matched";
    private static final String SIM_IMSI = "sim_imsi_key";
    private static final int SST_PNN_ENABLED = 48;
    private static final int SST_PNN_MASK = 48;
    private static final int SST_PNN_OFFSET = 12;
    private static final String TAG = "HwSIMRecords";
    private static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static PasswordUtil mPasswordUtil = HwFrameworkFactory.getPasswordUtil();
    private static final String pRefreshMultifileProp = "gsm.sim.refresh.multifile";
    private static final String pRefreshMultifilePropExtra = "gsm.sim.refresh.multifile.extra";
    private static String[] strEFIDs = new String[30];
    protected boolean bNeedSendRefreshBC = false;
    private GlobalChecker globalChecker = new GlobalChecker(this.mIccRecordsInner);
    private Handler handlerEx = new Handler() {
        /* class com.android.internal.telephony.uicc.HwSIMRecords.AnonymousClass2 */

        public void handleMessage(Message msg) {
            boolean isRecordLoadResponse = false;
            if (HwSIMRecords.this.mIccRecordsInner.judgeIfDestroyed()) {
                RlogEx.e(HwSIMRecords.TAG, "Received message " + msg + "[" + msg.what + "]  while being destroyed. Ignoring.");
                return;
            }
            try {
                int i = msg.what;
                if (i == 1) {
                    isRecordLoadResponse = true;
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        HwSIMRecords.log("Get GID1 failed, the exception: " + ar.exception);
                        HwSIMRecords.this.globalChecker.loadGID1Ex();
                    } else {
                        HwSIMRecords.this.mEfGid1 = (byte[]) ar.result;
                        HwSIMRecords.log("mEfGid1: " + IccUtils.bytesToHexString(HwSIMRecords.this.mEfGid1));
                    }
                } else if (i == 3) {
                    isRecordLoadResponse = true;
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception != null) {
                        HwSIMRecords.log("Get GID1_EX failed, the exception: " + ar2.exception);
                    } else {
                        HwSIMRecords.this.mEfGid1 = (byte[]) ar2.result;
                        HwSIMRecords.log("mEfGid1_ex: " + IccUtils.bytesToHexString(HwSIMRecords.this.mEfGid1));
                    }
                } else if (i != 4) {
                    HwSIMRecords.log("unknown Event: " + msg.what);
                } else {
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3.exception != null) {
                        String carrierFilePath = null;
                        String carrierFileId = null;
                        Bundle carrierBundle = msg.getData();
                        if (carrierBundle != null) {
                            carrierFilePath = carrierBundle.getString(HwTelephony.VirtualNets.MATCH_PATH);
                            carrierFileId = carrierBundle.getString(HwTelephony.VirtualNets.MATCH_FILE);
                            HwSIMRecords.log("load Specialfile: " + carrierFilePath + " " + carrierFileId + " fail!");
                        }
                        HwSIMRecords.this.mHwCarrierCardManager.addSpecialFileResult(false, carrierFilePath, carrierFileId, null, HwSIMRecords.this.getSlotId());
                        HwSIMRecords.log("exception=" + ar3.exception);
                    } else {
                        Bundle carrierBundle2 = msg.getData();
                        String carrierFilePath2 = carrierBundle2.getString(HwTelephony.VirtualNets.MATCH_PATH);
                        String carrierFileId2 = carrierBundle2.getString(HwTelephony.VirtualNets.MATCH_FILE);
                        String carrierFileValue = IccUtils.bytesToHexString((byte[]) ar3.result);
                        HwSIMRecords.this.mHwCarrierCardManager.addSpecialFileResult(true, carrierFilePath2, carrierFileId2, carrierFileValue, HwSIMRecords.this.getSlotId());
                        HwSIMRecords.log("Carrier load Specialfile: " + carrierFilePath2 + " " + carrierFileId2 + " = " + carrierFileValue);
                    }
                }
                if (!isRecordLoadResponse) {
                    return;
                }
            } catch (RuntimeException exc) {
                RlogEx.w(HwSIMRecords.TAG, "Exception parsing SIM record", exc);
                if (0 == 0) {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    HwSIMRecords.this.mIccRecordsInner.onRecordLoadedHw();
                }
                throw th;
            }
            HwSIMRecords.this.mIccRecordsInner.onRecordLoadedHw();
        }
    };
    private boolean isEnsEnabled = SystemProperties.getBoolean("ro.config.hw_is_ens_enabled", false);
    private String mActingHplmn = "";
    byte[] mEfGid1 = null;
    HwEons mEons = new HwEons();
    HwCarrierConfigCardManager mHwCarrierCardManager;
    private HwCustHwSIMRecords mHwCustHwSIMRecords;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.uicc.HwSIMRecords.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                boolean bNeedFetchRecords = true;
                if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(action)) {
                    HwSIMRecords.log("Receives ACTION_SET_RADIO_CAPABILITY_DONE on slot " + HwSIMRecords.this.getSlotId());
                    if (!HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT || !HwSIMRecords.this.mIsSimPowerDown || HwSIMRecords.this.mParentApp == null || IccCardApplicationStatusEx.AppStateEx.APPSTATE_READY != HwSIMRecords.this.mParentApp.getState()) {
                        bNeedFetchRecords = false;
                    }
                    if (bNeedFetchRecords) {
                        HwSIMRecords.log("fetchSimRecords again.");
                        HwSIMRecords.this.mIsSimPowerDown = false;
                        HwSIMRecords.this.mIccRecordsInner.fetchSimRecordsHw();
                    }
                } else if ("com.huawei.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                    if (intent.getExtras() != null) {
                        if (HwTelephonyPropertiesInner.ENABLE_NEW_PDP_SCHEME) {
                            HwSIMRecords.this.checkSinglePdpConfig();
                        }
                        int state = intent.getExtras().getInt("state");
                        HwSIMRecords.log(" onReceive action state = " + state);
                        if (state == 3 || state == 1) {
                            String operator = HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric();
                            StringBuilder sb = new StringBuilder();
                            sb.append("Receives HW_ACTION_CARRIER_CONFIG_CHANGED operator= ");
                            sb.append(Log.HWINFO ? operator : "***");
                            HwSIMRecords.log(sb.toString());
                            if (operator != null) {
                                HwSIMRecords.this.setVoiceMailByCountry(operator);
                            }
                        }
                    }
                } else if (HwSIMRecords.HW_ACTION_APN_REMINDER_NOTIFY.equals(action)) {
                    String imsi = HwSIMRecords.this.mIccRecordsInner.getImsiHw();
                    String operatorName = HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric();
                    if (HwSIMRecords.isMultiSimEnabled) {
                        int phoneId = intent.getIntExtra("APN_PHONE_ID", -1);
                        if (phoneId != -1 && HwSIMRecords.this.getSlotId() == phoneId) {
                            ApnReminder apnReminder = ApnReminder.getInstance(HwSIMRecords.this.mContext, phoneId);
                            apnReminder.setGID1(HwSIMRecords.this.mEfGid1);
                            apnReminder.setPlmnAndImsi(operatorName, imsi);
                            return;
                        }
                        return;
                    }
                    ApnReminder.getInstance(HwSIMRecords.this.mContext).setGID1(HwSIMRecords.this.mEfGid1);
                    ApnReminder.getInstance(HwSIMRecords.this.mContext).setPlmnAndImsi(operatorName, imsi);
                } else {
                    HwSIMRecords.log(" onReceive action  " + action + " is not processed.");
                }
            }
        }
    };
    private boolean mIsSimPowerDown = false;
    private ArrayList<byte[]> mPnnRecords = null;
    private boolean mSstPnnVaild = true;
    VoiceMailConstants mVmConfig;

    public HwSIMRecords(IIccRecordsInner iccRecordsInner, UiccCardApplicationEx app, Context c, CommandsInterfaceEx ci) {
        super(iccRecordsInner, app, c, ci);
        iccRecordsInner.initEventIdMap();
        this.mHwCustHwSIMRecords = (HwCustHwSIMRecords) HwCustUtils.createObj(HwCustHwSIMRecords.class, new Object[]{iccRecordsInner, this, c});
        this.mHwCarrierCardManager = HwCarrierConfigCardManager.getDefault(c);
        this.mHwCarrierCardManager.reportIccRecordInstance(getSlotId(), this);
        this.mVmConfig = iccRecordsInner.getVmConfig();
        if (getIccidSwitch()) {
            int eventId = iccRecordsInner.getEventIdFromMap("EVENT_GET_ICCID_DONE");
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(eventId));
            } else {
                this.mFh.loadEFTransparent(12258, obtainMessage(eventId));
            }
            this.mIccRecordsInner.addRecordsToLoadNum();
        }
        addIntentFilter(c);
    }

    private void addIntentFilter(Context c) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("com.huawei.action.CARRIER_CONFIG_CHANGED");
        filter.addAction(HW_ACTION_APN_REMINDER_NOTIFY);
        c.registerReceiver(this.mIntentReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkSinglePdpConfig() {
        int slotId = getSlotId();
        Boolean isSinglePdpEnabled = (Boolean) HwCfgFilePolicy.getValue("single_pdp_enabled", slotId, Boolean.class);
        log("checkSinglePdpConfig, slotId:" + slotId + " single_pdp_enabled:" + isSinglePdpEnabled);
        if (isSinglePdpEnabled == null || !isSinglePdpEnabled.booleanValue()) {
            SystemProperties.set(HwTelephonyPropertiesInner.PROP_SINGLE_PDP_HPLMN_MATCHED + slotId, "false");
            return;
        }
        SystemProperties.set(HwTelephonyPropertiesInner.PROP_SINGLE_PDP_HPLMN_MATCHED + slotId, "true");
    }

    public void handleMessage(Message msg) {
        int length;
        boolean isRecordLoadResponse = false;
        if (this.mIccRecordsInner.judgeIfDestroyed()) {
            RlogEx.e(TAG, "Received message while being destroyed. Ignoring.");
            return;
        }
        try {
            int i = msg.what;
            if (i == EVENT_GET_ALL_OPL_RECORDS_DONE) {
                isRecordLoadResponse = true;
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    RlogEx.e(TAG, "[EONS] Exception in fetching OPL Records: " + ar.exception);
                    this.mEons.resetOplData();
                } else {
                    this.mEons.setOplData((ArrayList) ar.result);
                    this.mIccRecordsInner.notifyRegisterForRecordsEvents(100);
                }
            } else if (i == EVENT_GET_ALL_PNN_RECORDS_DONE) {
                isRecordLoadResponse = true;
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception != null) {
                    RlogEx.e(TAG, "[EONS] Exception in fetching PNN Records: " + ar2.exception);
                    this.mEons.resetPnnData();
                } else if (IS_DELAY_UPDATENAME) {
                    this.mPnnRecords = new ArrayList<>();
                    this.mPnnRecords = (ArrayList) ar2.result;
                } else {
                    this.mEons.setPnnData((ArrayList) ar2.result);
                    this.mIccRecordsInner.notifyRegisterForRecordsEvents(100);
                }
            } else if (i == EVENT_GET_ACTING_HPLMN_DONE) {
                isRecordLoadResponse = true;
                AsyncResult ar3 = (AsyncResult) msg.obj;
                log("EVENT_GET_ACTING_HPLMN_DONE");
                if (ar3.exception != null) {
                    RlogEx.e(TAG, "Exception in get acting hplmn " + ar3.exception);
                } else {
                    int[] mHplmnData = getSimPlmnDigits((byte[]) ar3.result);
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
                        StringBuilder sb = new StringBuilder();
                        sb.append("length of mHplmnData =");
                        sb.append(length);
                        sb.append(", mActingHplmn = ");
                        sb.append(Log.HWINFO ? this.mActingHplmn : "***");
                        log(sb.toString());
                    }
                }
            } else if (i != EVENT_GET_PBR_DONE) {
                switch (i) {
                    case EVENT_GET_SPN_CPHS_DONE /*{ENCODED_INT: 104}*/:
                        isRecordLoadResponse = true;
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception != null) {
                            RlogEx.e(TAG, "[EONS] Exception in reading EF_SPN_CPHS: " + ar4.exception);
                            this.mEons.resetCphsData(HwEons.CphsType.LONG);
                            break;
                        } else {
                            this.mEons.setCphsData(HwEons.CphsType.LONG, (byte[]) ar4.result);
                            break;
                        }
                    case EVENT_GET_SPN_SHORT_CPHS_DONE /*{ENCODED_INT: 105}*/:
                        isRecordLoadResponse = true;
                        AsyncResult ar5 = (AsyncResult) msg.obj;
                        if (ar5.exception != null) {
                            RlogEx.e(TAG, "[EONS] Exception in reading EF_SPN_SHORT_CPHS: " + ar5.exception);
                            this.mEons.resetCphsData(HwEons.CphsType.SHORT);
                            break;
                        } else {
                            this.mEons.setCphsData(HwEons.CphsType.SHORT, (byte[]) ar5.result);
                            break;
                        }
                    case EVENT_GET_SIM_MATCHED_FILE_DONE /*{ENCODED_INT: 106}*/:
                        log("EVENT_GET_SIM_MATCHED_FILE_DONE");
                        isRecordLoadResponse = true;
                        onGetSimMatchedFileDone(msg);
                        break;
                    default:
                        this.mIccRecordsInner.handleMessageEx(msg);
                        break;
                }
            } else {
                isRecordLoadResponse = true;
                AsyncResult ar6 = (AsyncResult) msg.obj;
                if (ar6.exception == null) {
                    this.mIs3Gphonebook = true;
                } else if ((ar6.exception instanceof CommandException) && CommandException.Error.SIM_ABSENT == ar6.exception.getCommandError()) {
                    this.mIsSimPowerDown = true;
                    log("Get PBR Done,mIsSimPowerDown: " + this.mIsSimPowerDown);
                }
                this.mIsGetPBRDone = true;
                log("Get PBR Done,mIs3Gphonebook: " + this.mIs3Gphonebook);
            }
            if (!isRecordLoadResponse) {
                return;
            }
        } catch (RuntimeException exc) {
            RlogEx.w(TAG, "Exception parsing SIM record", exc);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                this.mIccRecordsInner.onRecordLoadedHw();
            }
            throw th;
        }
        this.mIccRecordsInner.onRecordLoadedHw();
    }

    public void onReady() {
        if (this.bNeedSendRefreshBC && IS_HW_SIM_REFRESH) {
            this.bNeedSendRefreshBC = false;
            notifyRegisterForIccRefresh();
        }
    }

    public boolean beforeHandleSimRefresh(int refreshResult, int efId) {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            ApnReminder.getInstance(this.mContext, getSlotId()).getCust().setSimRefreshingState(true);
        } else {
            ApnReminder.getInstance(this.mContext).getCust().setSimRefreshingState(true);
        }
        int slotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        UiccCardExt uiccCardExtBySlot = UiccControllerExt.getInstance().getUiccCard(slotId);
        UiccCardExt uiccCardExt = this.mParentApp != null ? this.mParentApp.getUiccCard() : null;
        log("beforeHandleSimRefresh->getDefault4GSlotId, slotId: " + slotId);
        if (refreshResult == 0) {
            log("beforeHandleSimRefresh with REFRESH_RESULT_FILE_UPDATE");
            if (IS_HW_CHINA_TELECOM && uiccCardExt != null && uiccCardExtBySlot != null && uiccCardExt.getUiccCard() == uiccCardExtBySlot.getUiccCard()) {
                log("Do not handleSimRefresh with SIM_FILE_UPDATED sent by RUIM.");
                return true;
            } else if (hwCustHandleSimRefresh(efId)) {
                return true;
            } else {
                return false;
            }
        } else if (refreshResult == 1) {
            log("beforeHandleSimRefresh with SIM_REFRESH_INIT");
            if (IS_HW_CHINA_TELECOM && uiccCardExt != null && uiccCardExtBySlot != null && uiccCardExt.getUiccCard() == uiccCardExtBySlot.getUiccCard()) {
                log("Do not handleSimRefresh with REFRESH_RESULT_INIT sent by RUIM.");
                return true;
            } else if (!IS_HW_SIM_REFRESH) {
                return false;
            } else {
                this.bNeedSendRefreshBC = true;
                return false;
            }
        } else if (refreshResult != 2) {
            log("beforeHandleSimRefresh with unknown operation");
            return false;
        } else {
            log("beforeHandleSimRefresh with SIM_REFRESH_RESET");
            return false;
        }
    }

    private boolean hwCustHandleSimRefresh(int efid) {
        if (65535 == efid) {
            String strEFID = SystemProperties.get(pRefreshMultifileProp, "");
            log("The strEFID is: " + strEFID);
            if (strEFID.isEmpty()) {
                log("handleSimRefresh with no multifile found");
                return false;
            }
            SystemProperties.set(pRefreshMultifileProp, "");
            String strEFIDExtra = SystemProperties.get(pRefreshMultifilePropExtra, "");
            if (!strEFIDExtra.isEmpty()) {
                log("The strEFIDExtra is: " + strEFIDExtra);
                strEFID = strEFID + ',' + strEFIDExtra;
                log("The strEFID is: " + strEFID);
                SystemProperties.set(pRefreshMultifilePropExtra, "");
            }
            strEFIDs = strEFID.split(",");
            log("strEFIDs.length()" + strEFIDs.length);
            for (int i = 0; i < strEFIDs.length; i++) {
                try {
                    log("handleSimRefresh with strEFIDs[i]: " + strEFIDs[i]);
                    int EFID = Integer.parseInt(strEFIDs[i], 16);
                    log("handleSimRefresh with EFID: " + EFID);
                    this.mIccRecordsInner.handleFileUpdateHw(EFID);
                } catch (NumberFormatException e) {
                    log("handleSimRefresh with convert EFID from String to Int error");
                }
            }
            log("notify mIccRefreshRegistrants");
            notifyRegisterForIccRefresh();
            return true;
        }
        log("refresh with only one EF ID");
        return false;
    }

    public boolean afterHandleSimRefresh(int refreshResult) {
        if (refreshResult == 0) {
            log("afterHandleSimRefresh with REFRESH_RESULT_FILE_UPDATE");
            notifyRegisterForIccRefresh();
            return false;
        } else if (refreshResult == 1) {
            log("afterHandleSimRefresh with SIM_REFRESH_INIT");
            return false;
        } else if (refreshResult != 2) {
            log("afterHandleSimRefresh with unknown operation");
            return false;
        } else {
            log("afterHandleSimRefresh with SIM_REFRESH_RESET");
            if (!IS_HW_SIM_REFRESH) {
                return false;
            }
            this.bNeedSendRefreshBC = true;
            return false;
        }
    }

    protected static void log(String string) {
        RlogEx.i(TAG, string);
    }

    protected static void loge(String string) {
        RlogEx.e(TAG, string);
    }

    /* access modifiers changed from: private */
    public class GlobalChecker {
        private IIccRecordsInner mSimRecords;

        public GlobalChecker(IIccRecordsInner simRecords) {
            this.mSimRecords = simRecords;
        }

        public void onOperatorNumericLoaded() {
            getHomeNumericAndSetRoaming();
            if (HwTelephonyPropertiesInner.ENABLE_NEW_PDP_SCHEME) {
                HwSIMRecords.this.checkSinglePdpConfig();
            } else {
                checkMultiPdpConfig();
            }
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
            int lDataVal = SubscriptionController.getInstance().getSlotIndex(HwTelephonyManagerInner.getDefault().getPreferredDataSubscription());
            boolean hasTwoCard = true;
            if (lDataVal == -1) {
                lDataVal = 0;
            }
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
            String[] plmns = plmnsConfig.split(",");
            for (String plmn : plmns) {
                if (plmn != null) {
                    if (plmn.equals(HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric())) {
                        if (!"true".equals(Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED)) && (lDataVal == lSimSlotVal || !hasTwoCard)) {
                            ((TelephonyManager) HwSIMRecords.this.mContext.getSystemService("phone")).setDataEnabled(false);
                            Settings.System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
                        }
                        if (lSimSlotVal == 0) {
                            SystemProperties.set("gsm.huawei.RemindDataService", "true");
                            return;
                        } else if (1 == lSimSlotVal) {
                            SystemProperties.set("gsm.huawei.RemindDataService_1", "true");
                            return;
                        } else {
                            return;
                        }
                    }
                }
            }
            if ("true".equals(Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED))) {
                return;
            }
            if (lDataVal == lSimSlotVal || !hasTwoCard) {
                Settings.System.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
            }
        }

        public void onAllRecordsLoaded() {
            updateCarrierFileIfNeed();
            HwSIMRecords.this.mVmConfig.resetVoiceMailLoadFlag();
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
                    HwSIMRecords.this.mIccRecordsInner.notifyRegisterForRecordsEvents(100);
                } catch (RuntimeException exc) {
                    RlogEx.w(HwSIMRecords.TAG, "Exception set PNN record", exc);
                }
            }
            updateClatForMobile();
            String operator = HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric();
            if (!TextUtils.isEmpty(operator)) {
                HwSIMRecords.this.setVoiceMailByCountry(operator);
            }
        }

        private void updateClatForMobile() {
            SubscriptionController subController = SubscriptionController.getInstance();
            if (subController != null && HwSIMRecords.this.getSlotId() == subController.getDefaultDataSubId()) {
                String mccMnc = HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric();
                try {
                    String plmnsConfig = Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), "disable_mobile_clatd");
                    if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccMnc)) {
                        HwSIMRecords.log("plmnsConfig is null, return");
                    } else if (plmnsConfig.contains(mccMnc)) {
                        HwSIMRecords.log("disable clatd!");
                        SystemProperties.set("gsm.net.doxlat", "false");
                    } else {
                        SystemProperties.set("gsm.net.doxlat", "true");
                    }
                } catch (Exception e) {
                    RlogEx.e(HwSIMRecords.TAG, "Exception for SystemProperties.set");
                }
            }
        }

        private void sendSimRecordsReadyBroadcast() {
            int[] subId;
            String operatorNumeric = HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric();
            String imsi = HwSIMRecords.this.mIccRecordsInner.getImsiHw();
            HwSIMRecords.log("broadcast TelephonyIntents.ACTION_SIM_RECORDS_READY");
            Intent intent = new Intent("com.huawei.intent.action.ACTION_SIM_RECORDS_READY");
            intent.addFlags(536870912);
            intent.putExtra("mccMnc", operatorNumeric);
            intent.putExtra("imsi", imsi);
            if (!(!TelephonyManager.getDefault().isMultiSimEnabled() || HwSIMRecords.this.mParentApp == null || HwSIMRecords.this.mParentApp.getUiccCard() == null || (subId = SubscriptionManager.getSubId(HwSIMRecords.this.mParentApp.getUiccCard().getPhoneId())) == null || subId.length <= 0)) {
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, SubscriptionManager.getPhoneId(subId[0]));
            }
            ActivityManagerNative.broadcastStickyIntent(intent, (String) null, 0);
        }

        private void checkMultiPdpConfig() {
            if (HwSIMRecords.this.mContext == null) {
                HwSIMRecords.log("checkMultiPdpConfig, mContext is null");
                return;
            }
            String plmnsConfig = Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.MULTI_PDP_PLMN_MATCHED);
            int slotId = HwSIMRecords.this.getSlotId();
            String operatorNumeric = HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric();
            HwSIMRecords.log("checkMultiPdpConfig, slotId:" + slotId + " plmn:" + operatorNumeric + " plmnsConfig:" + plmnsConfig);
            if (!TextUtils.isEmpty(plmnsConfig)) {
                String[] plmns = plmnsConfig.split(",");
                for (String plmn : plmns) {
                    if (!TextUtils.isEmpty(plmn) && plmn.equals(operatorNumeric)) {
                        SystemProperties.set(HwTelephonyPropertiesInner.MULTI_PDP_PLMN_MATCHED + slotId, "true");
                        return;
                    }
                }
            }
            SystemProperties.set(HwTelephonyPropertiesInner.MULTI_PDP_PLMN_MATCHED + slotId, "false");
        }

        private void checkDataServiceRemind() {
            SystemProperties.set("gsm.huawei.RemindDataService", "false");
            String plmnsConfig = SettingsEx.Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), "plmn_remind_data_service");
            StringBuilder sb = new StringBuilder();
            sb.append("checkDataServiceRemind plmnsConfig = ");
            sb.append(Log.HWINFO ? plmnsConfig : "***");
            HwSIMRecords.log(sb.toString());
            if (plmnsConfig == null) {
                plmnsConfig = "26006,26003";
            }
            String[] plmns = plmnsConfig.split(",");
            for (String plmn : plmns) {
                if (plmn != null && plmn.equals(HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric())) {
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

        /* JADX INFO: Multiple debug info for r4v1 int: [D('plmnCustomArray' java.lang.String[]), D('regplmnCustomArrayLen' int)] */
        private void checkGsmOnlyDataNotAllowed() {
            if (!HwSIMRecords.isMultiSimEnabled) {
                SystemProperties.set("gsm.data.gsm_only_not_allow_ps", "false");
            } else if (SubscriptionManager.getSubId(HwSIMRecords.this.getSlotId()) != null) {
                TelephonyManager.setTelephonyProperty(HwSIMRecords.this.getSlotId(), "gsm.data.gsm_only_not_allow_ps", "false");
            } else {
                return;
            }
            String plmnGsmonlyPsNotallowd = Settings.System.getString(HwSIMRecords.this.mContext.getContentResolver(), "hw_2gonly_psnotallowed");
            if (plmnGsmonlyPsNotallowd == null || "".equals(plmnGsmonlyPsNotallowd)) {
                plmnGsmonlyPsNotallowd = "23410";
            }
            String hplmn = HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric();
            if (hplmn == null || "".equals(hplmn)) {
                HwSIMRecords.log("is2GonlyPsAllowed home plmn not ready");
                return;
            }
            String[] plmnCustomArray = plmnGsmonlyPsNotallowd.split(",");
            int regplmnCustomArrayLen = plmnCustomArray.length;
            for (int i = 0; i < regplmnCustomArrayLen; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append("is2GonlyPsAllowed plmnCustomArray[");
                sb.append(i);
                sb.append("] = ");
                sb.append(Log.HWINFO ? plmnCustomArray[i] : "***");
                HwSIMRecords.log(sb.toString());
                if (hplmn.equals(plmnCustomArray[i])) {
                    if (HwSIMRecords.isMultiSimEnabled) {
                        TelephonyManager.setTelephonyProperty(HwSIMRecords.this.getSlotId(), "gsm.data.gsm_only_not_allow_ps", "true");
                        return;
                    } else {
                        SystemProperties.set("gsm.data.gsm_only_not_allow_ps", "true");
                        return;
                    }
                }
            }
        }

        public void loadGID1() {
            HwSIMRecords.this.mFh.loadEFTransparent(28478, HwSIMRecords.this.handlerEx.obtainMessage(1));
            HwSIMRecords.this.mIccRecordsInner.addRecordsToLoadNum();
        }

        public void loadGID1Ex() {
            if ((HwSIMRecords.this.mFh instanceof UsimFileHandler) && !"3F007FFF".equals(HwSIMRecords.this.mFh.getEFPath(28478))) {
                HwSIMRecords.this.mFh.loadEFTransparent("3F007FFF", 28478, HwSIMRecords.this.handlerEx.obtainMessage(3), true);
                HwSIMRecords.this.mIccRecordsInner.addRecordsToLoadNum();
            }
        }

        public String getHomeNumericAndSetRoaming() {
            String imsi = HwSIMRecords.this.mIccRecordsInner.getImsiHw();
            String operatorName = HwSIMRecords.this.mIccRecordsInner.getOperatorNumeric();
            if (HwSIMRecords.isMultiSimEnabled) {
                HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerOperator(operatorName, HwSIMRecords.this.getSlotId());
                HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerImsi(imsi, Integer.valueOf(HwSIMRecords.this.getSlotId()));
                if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(HwSIMRecords.this.getSlotId()))) {
                    return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(HwSIMRecords.this.getSlotId()));
                }
                return null;
            }
            HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerOperator(operatorName);
            HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerImsi(imsi);
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

        public void onIccIdLoadedHw(String iccId) {
            if (HwSIMRecords.isMultiSimEnabled) {
                HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerIccId(iccId, HwSIMRecords.this.getSlotId());
            } else {
                HwTelephonyFactory.getHwPhoneManager().setRoamingBrokerIccId(iccId);
            }
            HwTelephonyFactory.getHwPhoneManager().setMccTableIccId(iccId);
        }

        public void onImsiLoaded() {
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                HwSIMRecords.log("onImsiLoaded mPhoneId = " + HwSIMRecords.this.getSlotId());
                if (HwSIMRecords.this.getSlotId() == 1 && TelephonyManager.getDefault().getSimState(0) == 5) {
                    return;
                }
            }
            HwTelephonyFactory.getHwPhoneManager().setMccTableImsi(HwSIMRecords.this.mIccRecordsInner.getImsiHw());
        }

        private void updateCarrierFileIfNeed() {
            if (HwSIMRecords.this.mHwCarrierCardManager != null) {
                IccRecordsEx iccRecordsEx = new IccRecordsEx();
                iccRecordsEx.setIccRecords(this.mSimRecords);
                HwSIMRecords.this.mHwCarrierCardManager.updateCarrierFileIfNeed(HwSIMRecords.this.getSlotId(), iccRecordsEx);
            }
        }
    }

    public byte[] getGID1() {
        byte[] bArr = this.mEfGid1;
        if (bArr != null) {
            return Arrays.copyOf(bArr, bArr.length);
        }
        return new byte[]{0};
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

    public void onOperatorNumericLoadedHw() {
        this.globalChecker.onOperatorNumericLoaded();
        onImsiAndAdLoadedHw(this.mIccRecordsInner.getImsiHw());
    }

    public void onAllRecordsLoadedHw() {
        updateSarMnc(this.mIccRecordsInner.getImsiHw());
        this.globalChecker.onAllRecordsLoaded();
    }

    public void loadGID1() {
        this.globalChecker.loadGID1();
    }

    public void onIccIdLoadedHw() {
        String iccId = this.mIccRecordsInner.getIccIdHw();
        this.globalChecker.onIccIdLoadedHw(iccId);
        processGetIccIdDone(iccId);
        if (getIccidSwitch()) {
            sendIccidDoneBroadcast(iccId);
        }
    }

    public void processGetIccIdDone(String iccid) {
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            HwHotplugController.getInstance().onHotplugIccIdChanged(iccid, getSlotId());
        }
        updateCarrierFile(getSlotId(), 1, iccid);
    }

    public void onImsiLoadedHw() {
        this.globalChecker.onImsiLoaded();
    }

    private void onImsiAndAdLoadedHw(String imsi) {
        String mccmnc;
        String rbImsi = null;
        String rbMccmnc = null;
        if (imsi != null) {
            if (3 == this.mIccRecordsInner.getMncLength()) {
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
            StringBuilder sb = new StringBuilder();
            sb.append("Set RoamingBroker mccmnc=");
            sb.append(Log.HWINFO ? rbMccmnc : "***");
            log(sb.toString());
            updateCarrierFile(getSlotId(), 2, rbImsi);
            updateCarrierFile(getSlotId(), 3, rbMccmnc);
        }
    }

    public void updateCarrierFile(int slotId, int fileType, String fileValue) {
        this.mHwCarrierCardManager.updateCarrierFile(slotId, fileType, fileValue);
    }

    public void custMncLength(String mcc) {
        String mncHaving2Digits = SystemProperties.get("ro.config.mnc_having_2digits", "");
        log("mnc_having_2digits = " + mncHaving2Digits);
        if (mncHaving2Digits != null) {
            String custMccmncCode = this.mIccRecordsInner.getImsiHw().substring(0, 5);
            for (String plmn : mncHaving2Digits.split(",")) {
                if (custMccmncCode.equals(plmn)) {
                    this.mIccRecordsInner.setMncLength(2);
                    return;
                }
            }
        } else if (mcc.equals("416") && 3 == this.mIccRecordsInner.getMncLength()) {
            log("SIMRecords: customize for Jordan sim card, make the mcnLength to 2");
            this.mIccRecordsInner.setMncLength(2);
        }
    }

    @Override // com.android.internal.telephony.uicc.HwIccRecordsEx
    public String getOperatorNumericEx(ContentResolver cr, String name) {
        String imsi = this.mIccRecordsInner.getImsiHw();
        if (cr == null || imsi == null || "".equals(imsi) || name == null || "".equals(name)) {
            return this.mIccRecordsInner.getOperatorNumeric();
        }
        String hwImsiPlmnEx = Settings.System.getString(cr, name);
        if (hwImsiPlmnEx != null && !"".equals(hwImsiPlmnEx)) {
            String[] plmn_ex = hwImsiPlmnEx.split(",");
            for (String plmn_item : plmn_ex) {
                if (imsi.startsWith(plmn_item)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("getOperatorNumericEx: ");
                    sb.append(Log.HWINFO ? plmn_item : "***");
                    log(sb.toString());
                    return plmn_item;
                }
            }
        }
        return this.mIccRecordsInner.getOperatorNumeric();
    }

    public void beforeGetVoiceMailNumber() {
        ApnReminder apnReminder;
        if (isMultiSimEnabled) {
            apnReminder = ApnReminder.getInstance(this.mContext, getSlotId());
        } else {
            apnReminder = ApnReminder.getInstance(this.mContext);
        }
        if (!apnReminder.isPopupApnSettingsEmpty()) {
            log("beforeGetVoiceMailNumber: PopupApnSettings not empty");
            VoiceMailConstants voiceMailConstants = this.mVmConfig;
            if (voiceMailConstants != null) {
                voiceMailConstants.resetVoiceMailLoadFlag();
                long token = Binder.clearCallingIdentity();
                try {
                    setVoiceMailByCountry(this.mIccRecordsInner.getOperatorNumeric());
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
    }

    public void resetRecords() {
        this.mIs3Gphonebook = false;
        this.mIsGetPBRDone = false;
        this.mIsSimPowerDown = false;
        this.mSstPnnVaild = true;
        this.mPnnRecords = null;
    }

    public void getPbrRecordSize() {
        this.mFh.loadEFLinearFixedAll(20272, obtainMessage(EVENT_GET_PBR_DONE));
        this.mIccRecordsInner.addRecordsToLoadNum();
    }

    public void updateSarMnc(String imsi) {
        if (imsi != null && imsi.length() >= 3 && PhoneFactory.getDefaultSubscription() == SubscriptionController.getInstance().getSubIdUsingPhoneId(getSlotId())) {
            SystemProperties.set("reduce.sar.imsi.mnc", imsi.substring(0, 3));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVoiceMailByCountry(String spn) {
        StringBuilder sb = new StringBuilder();
        sb.append("setVoiceMailByCountry spn ");
        String str = "***";
        sb.append(Log.HWINFO ? spn : str);
        sb.append(" for slot");
        sb.append(getSlotId());
        log(sb.toString());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(getSlotId()))) {
                String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail(getSlotId());
                String spn2 = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(getSlotId()));
                if (this.mVmConfig.containsCarrierHw(spn2, getSlotId())) {
                    if (!TextUtils.isEmpty(number)) {
                        this.mIccRecordsInner.setVoiceFixedFlag(true);
                        this.mIccRecordsInner.setVoiceMailNumber(number);
                    } else {
                        this.mIccRecordsInner.setVoiceFixedFlag(this.mVmConfig.getVoiceMailFixed(spn2, getSlotId()));
                        this.mIccRecordsInner.setVoiceMailNumber(this.mVmConfig.getVoiceMailNumberHw(spn2, getSlotId()));
                    }
                    this.mIccRecordsInner.setVoiceMailTag(this.mVmConfig.getVoiceMailTagHw(spn2, getSlotId()));
                }
            } else if (this.mVmConfig.containsCarrierHw(spn, getSlotId())) {
                this.mIccRecordsInner.setVoiceFixedFlag(this.mVmConfig.getVoiceMailFixed(spn, getSlotId()));
                this.mIccRecordsInner.setVoiceMailNumber(this.mVmConfig.getVoiceMailNumberHw(spn, getSlotId()));
                this.mIccRecordsInner.setVoiceMailTag(this.mVmConfig.getVoiceMailTagHw(spn, getSlotId()));
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("VoiceMailConfig doesn't contains the carrier");
                if (Log.HWINFO) {
                    str = spn;
                }
                sb2.append(str);
                sb2.append(" for slot");
                sb2.append(getSlotId());
                log(sb2.toString());
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String spn3 = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
            String number2 = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            String previousOp = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
            String spn4 = previousOp != null ? previousOp : spn3;
            if (this.mVmConfig.containsCarrierHw(spn4)) {
                if (!TextUtils.isEmpty(number2)) {
                    this.mIccRecordsInner.setVoiceFixedFlag(true);
                    this.mIccRecordsInner.setVoiceMailNumber(number2);
                } else {
                    this.mIccRecordsInner.setVoiceFixedFlag(this.mVmConfig.getVoiceMailFixed(spn4));
                    this.mIccRecordsInner.setVoiceMailNumber(this.mVmConfig.getVoiceMailNumberHw(spn4));
                }
                this.mIccRecordsInner.setVoiceMailTag(this.mVmConfig.getVoiceMailTagHw(spn4));
            }
        } else {
            this.mIccRecordsInner.setVoiceMailByCountryHw(spn);
        }
    }

    @Override // com.android.internal.telephony.uicc.HwIccRecordsEx
    public boolean checkFileInServiceTable(int efid, UsimServiceTable usimServiceTable, byte[] data) {
        boolean serviceStatus = true;
        log("check file status in serivce table " + efid);
        if (efid == 28486) {
            log("check EF_SPN serivice in serivice table!!");
            if (this.mParentApp.getUiccProfileHw().isApplicationOnIcc(IccCardApplicationStatusEx.AppTypeEx.APPTYPE_USIM)) {
                if (usimServiceTable == null || usimServiceTable.isAvailable(UsimServiceTable.UsimService.SPN)) {
                    return true;
                }
                log("EF_SPN is disable in 3G card!!");
                return false;
            } else if (!this.mParentApp.getUiccProfileHw().isApplicationOnIcc(IccCardApplicationStatusEx.AppTypeEx.APPTYPE_SIM)) {
                return true;
            } else {
                int mSstSpnValue = data[4] & 15 & 3;
                if (3 == mSstSpnValue) {
                    log("SST: 2G Sim,SPNVALUE enabled SPNVALUE = " + mSstSpnValue);
                    return true;
                }
                log("SST: 2G Sim,SPNVALUE disabled  SPNVALUE = " + mSstSpnValue);
                return false;
            }
        } else if (efid != 28613) {
            return true;
        } else {
            log("check EF_PNN serivice in serivice table!!");
            if (this.mParentApp.getUiccProfileHw().isApplicationOnIcc(IccCardApplicationStatusEx.AppTypeEx.APPTYPE_USIM)) {
                if (usimServiceTable != null && !usimServiceTable.isAvailable(UsimServiceTable.UsimService.PLMN_NETWORK_NAME)) {
                    log("EF_PNN is disable in 3G or 4G card!!");
                    serviceStatus = false;
                }
            } else if (this.mParentApp.getUiccProfileHw().isApplicationOnIcc(IccCardApplicationStatusEx.AppTypeEx.APPTYPE_SIM) && data != null && data.length > 12) {
                int mSstPnnValue = data[12] & 48;
                if (48 == mSstPnnValue) {
                    serviceStatus = true;
                    log("SST: 2G Sim,PNNVALUE enabled PnnVALUE = " + mSstPnnValue);
                } else {
                    serviceStatus = false;
                    log("SST: 2G Sim,PNNVALUE disabled  PnnVALUE = " + mSstPnnValue);
                }
            }
            this.mSstPnnVaild = serviceStatus;
            return serviceStatus;
        }
    }

    public void loadEons() {
        this.mFh.loadEFLinearFixedAll(28614, obtainMessage(EVENT_GET_ALL_OPL_RECORDS_DONE));
        this.mIccRecordsInner.addRecordsToLoadNum();
        this.mFh.loadEFLinearFixedAll(28613, obtainMessage(EVENT_GET_ALL_PNN_RECORDS_DONE));
        this.mIccRecordsInner.addRecordsToLoadNum();
        this.mFh.loadEFTransparent(28436, obtainMessage(EVENT_GET_SPN_CPHS_DONE));
        this.mIccRecordsInner.addRecordsToLoadNum();
        this.mFh.loadEFTransparent(28440, obtainMessage(EVENT_GET_SPN_SHORT_CPHS_DONE));
        this.mIccRecordsInner.addRecordsToLoadNum();
    }

    public String getEons() {
        return this.mEons.getEons();
    }

    public boolean isEonsDisabled() {
        return this.mEons.isEonsDisabled();
    }

    public boolean updateEons(String regOperator, int lac) {
        return this.mEons.updateEons(regOperator, lac, this.mIccRecordsInner.getOperatorNumeric());
    }

    @Override // com.android.internal.telephony.uicc.HwIccRecordsEx
    public ArrayList<OperatorInfo> getEonsForAvailableNetworks(ArrayList<OperatorInfo> avlNetworks) {
        return this.mEons.getEonsForAvailableNetworks(avlNetworks);
    }

    public void initFdnPsStatus(int slotId) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            new QueryHandler(this.mContext.getContentResolver()).startQuery(0, null, ContentUris.withAppendedId(Uri.parse("content://icc/fdn/subId/"), (long) slotId), new String[]{"number"}, null, null, null);
        }
    }

    protected class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /* access modifiers changed from: protected */
        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
            HwSIMRecords.this.mIccRecordsInner.notifyRegisterForFdnRecordsLoaded();
        }
    }

    public void sendDualSimChangeBroadcast(boolean isSimImsiRefreshing, String mLastImsi, String mImsi) {
        if (isSimImsiRefreshing && mLastImsi != null && mImsi != null && !mLastImsi.equals(mImsi)) {
            ActivityManagerNative.broadcastStickyIntent(new Intent("android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE"), (String) null, 0);
            log("dual sim imsi change");
        }
    }

    public void loadCardSpecialFile(int fileid) {
        String path;
        if (fileid != 20276) {
            log("no fileid found for load");
        } else if (this.isEnsEnabled) {
            if (this.mFh instanceof UsimFileHandler) {
                path = "3F007FFF7F665F30";
                log("EF_HPLMN in USIMFileHandler");
            } else if (this.mFh instanceof SIMFileHandler) {
                path = "3F007F665F30";
                log("EF_HPLMN in SIMFileHandler");
            } else {
                path = this.mFh.getEFPath(20276);
                log("EF_HPLMN in other FileHandler");
            }
            this.mFh.loadEFTransparent(path, 20276, obtainMessage(EVENT_GET_ACTING_HPLMN_DONE), true);
            this.mIccRecordsInner.addRecordsToLoadNum();
        }
    }

    @Override // com.android.internal.telephony.uicc.HwIccRecordsEx
    public String[] getEhplmnOfSim() {
        String[] copyEhplmn;
        String[] ehplmns = this.mIccRecordsInner.getEhplmns();
        if (ehplmns != null) {
            copyEhplmn = (String[]) Arrays.copyOf(ehplmns, ehplmns.length);
            Arrays.sort(copyEhplmn);
        } else {
            log("getEhplmnOfSim: send EVENT_GET_EHPLMN_DONE_FOR_APNCURE");
            this.mFh.loadEFTransparent(28633, obtainMessage(this.mIccRecordsInner.getEventIdFromMap("EVENT_GET_EHPLMN_DONE_FOR_APNCURE")));
            copyEhplmn = EmptyArray.STRING;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getEhplmnOfSim:");
        sb.append(Log.HWINFO ? Arrays.toString(copyEhplmn) : "***");
        log(sb.toString());
        return copyEhplmn;
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

    public void refreshCardType() {
        HwCustHwSIMRecords hwCustHwSIMRecords = this.mHwCustHwSIMRecords;
        if (hwCustHwSIMRecords != null) {
            hwCustHwSIMRecords.refreshCardType();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedSetPnn() {
        String str;
        if (this.mSstPnnVaild) {
            return true;
        }
        String mccmnc = this.mIccRecordsInner.getOperatorNumeric();
        String plmnsConfig = Settings.System.getString(this.mContext.getContentResolver(), "hw_sst_pnn_by_mccmnc");
        StringBuilder sb = new StringBuilder();
        sb.append("isNeedSetPnn: mccmnc = ");
        String str2 = "***";
        if (Log.HWINFO) {
            str = mccmnc;
        } else {
            str = str2;
        }
        sb.append(str);
        sb.append(" plmnsConfig = ");
        sb.append(Log.HWINFO ? plmnsConfig : str2);
        log(sb.toString());
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccmnc)) {
            return true;
        }
        String[] plmns = plmnsConfig.split(",");
        for (String plmn : plmns) {
            if (plmn != null && plmn.equals(mccmnc)) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("isNeedSetPnn: mccmnc = ");
                if (Log.HWINFO) {
                    str2 = mccmnc;
                }
                sb2.append(str2);
                sb2.append(" no need set PNN from card.");
                log(sb2.toString());
                return false;
            }
        }
        return true;
    }

    public boolean isHwCustDataRoamingOpenArea() {
        HwCustHwSIMRecords hwCustHwSIMRecords = this.mHwCustHwSIMRecords;
        if (hwCustHwSIMRecords != null) {
            return hwCustHwSIMRecords.isHwCustDataRoamingOpenArea();
        }
        return false;
    }

    public void dispose() {
        log("Disposing HwSimRecords " + this);
        this.mHwCarrierCardManager.destory(getSlotId(), this);
        this.mContext.unregisterReceiver(this.mIntentReceiver);
    }

    public void loadSimMatchedFileFromRilCache() {
        if (this.mCi != null) {
            this.mCi.getSimMatchedFileFromRilCache(28589, obtainMessage(EVENT_GET_SIM_MATCHED_FILE_DONE));
            this.mIccRecordsInner.addRecordsToLoadNum();
            this.mCi.getSimMatchedFileFromRilCache(28472, obtainMessage(EVENT_GET_SIM_MATCHED_FILE_DONE));
            this.mIccRecordsInner.addRecordsToLoadNum();
            this.mCi.getSimMatchedFileFromRilCache(28478, obtainMessage(EVENT_GET_SIM_MATCHED_FILE_DONE));
            this.mIccRecordsInner.addRecordsToLoadNum();
            this.mCi.getSimMatchedFileFromRilCache(28479, obtainMessage(EVENT_GET_SIM_MATCHED_FILE_DONE));
            this.mIccRecordsInner.addRecordsToLoadNum();
        }
    }

    public void loadSimMatchedFileFromRilCacheByEfid(int efId) {
        if (this.mCi != null) {
            this.mCi.getSimMatchedFileFromRilCache(efId, obtainMessage(EVENT_GET_SIM_MATCHED_FILE_DONE));
        }
    }

    /* JADX INFO: Multiple debug info for r0v1 byte[]: [D('result' com.android.internal.telephony.uicc.IccIoResult), D('data' byte[])] */
    public void onGetSimMatchedFileDone(Message msg) {
        AsyncResult ar;
        if (msg == null || (ar = (AsyncResult) msg.obj) == null) {
            RlogEx.e(TAG, "onGetSimMatchedFileDone: msg or AsyncResult is null, return.");
            return;
        }
        IccIoResult resultEx = (IccIoResult) ar.result;
        int fileId = resultEx.getFileId();
        log("onGetSimMatchedFileDone: isValid=" + resultEx.isValidIccioResult() + ", fileId=0x" + Integer.toHexString(fileId));
        if (!resultEx.isValidIccioResult()) {
            executOriginalSimIoRequest(fileId);
            return;
        }
        Message response = obtainMessage(getOriginalSimIoEventId(fileId));
        if (!(fileId == 28436 || fileId == 28440 || fileId == 28472 || fileId == 28486)) {
            if (fileId == 28589) {
                IccIoResult result = new IccIoResult(resultEx.sw1, resultEx.sw2, IccUtils.bytesToHexString(resultEx.payload));
                this.mIccRecordsInner.addRecordsToLoadNum();
                AsyncResult.forMessage(response, result, ar.exception);
            } else if (!(fileId == 28478 || fileId == 28479)) {
                RlogEx.e(TAG, "onGetSimMatchedFileDone: do nothing for fileId = 0x" + Integer.toHexString(fileId));
            }
            response.sendToTarget();
        }
        byte[] data = resultEx.payload;
        this.mIccRecordsInner.addRecordsToLoadNum();
        AsyncResult.forMessage(response, data, ar.exception);
        response.sendToTarget();
    }

    private void executOriginalSimIoRequest(int fileId) {
        log("executOriginalSimIoRequest for fileId = 0x" + Integer.toHexString(fileId));
        if (!(fileId == 28436 || fileId == 28440 || fileId == 28472 || fileId == 28486)) {
            if (fileId == 28589) {
                CommandsInterfaceEx commandsInterfaceEx = this.mCi;
                IccFileHandler iccFileHandler = this.mFh;
                commandsInterfaceEx.iccIOForApp(176, 28589, this.mFh.getEFPath(28589), 0, 0, 4, (String) null, (String) null, this.mParentApp.getAid(), obtainMessage(getOriginalSimIoEventId(fileId)));
                this.mIccRecordsInner.addRecordsToLoadNum();
                return;
            } else if (!(fileId == 28478 || fileId == 28479)) {
                loge("executOriginalSimIoRequest: do nothing for fileId=0x" + Integer.toHexString(fileId));
                return;
            }
        }
        this.mFh.loadEFTransparent(fileId, obtainMessage(getOriginalSimIoEventId(fileId)));
        this.mIccRecordsInner.addRecordsToLoadNum();
    }

    private int getOriginalSimIoEventId(int fileId) {
        if (!(fileId == 28436 || fileId == 28440)) {
            if (fileId == 28472) {
                return this.mIccRecordsInner.getEventIdFromMap("EVENT_GET_SST_DONE");
            }
            if (fileId != 28486) {
                if (fileId == 28589) {
                    return this.mIccRecordsInner.getEventIdFromMap("EVENT_GET_AD_DONE");
                }
                if (fileId == 28478) {
                    return this.mIccRecordsInner.getEventIdFromMap("EVENT_GET_GID1_DONE");
                }
                if (fileId == 28479) {
                    return this.mIccRecordsInner.getEventIdFromMap("EVENT_GET_GID2_DONE");
                }
                loge("getOriginalSimIoEventId: Error, do nothing for fileId= 0x" + Integer.toHexString(fileId));
                return -1;
            }
        }
        return this.mIccRecordsInner.getEventIdFromMap("EVENT_GET_SPN_DONE");
    }

    public String getVmSimImsi() {
        PasswordUtil passwordUtil;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        if (!sp.contains(SIM_IMSI + getSlotId())) {
            if (sp.contains(VM_SIM_IMSI + getSlotId())) {
                String imsi = sp.getString(VM_SIM_IMSI + getSlotId(), null);
                if (!(imsi == null || (passwordUtil = mPasswordUtil) == null)) {
                    String oldDecodeVmSimImsi = passwordUtil.pswd2PlainText(imsi);
                    try {
                        imsi = new String(Base64.decode(imsi, 0), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        RlogEx.e(TAG, "getVmSimImsi UnsupportedEncodingException");
                    }
                    String simImsi = this.mIccRecordsInner.getImsiHw();
                    if (imsi.equals(simImsi) || oldDecodeVmSimImsi.equals(simImsi)) {
                        log("getVmSimImsi: Old IMSI encryption is not supported, now setVmSimImsi again.");
                        setVmSimImsi(simImsi);
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
            RlogEx.e(TAG, "getVmSimImsi IllegalArgumentException");
            return vmSimImsi;
        } catch (UnsupportedEncodingException e3) {
            RlogEx.e(TAG, "getVmSimImsi UnsupportedEncodingException");
            return vmSimImsi;
        }
    }

    public void setVmSimImsi(String imsi) {
        try {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            editor.putString(SIM_IMSI + getSlotId(), new String(Base64.encode(imsi.getBytes("utf-8"), 0), "utf-8"));
            editor.apply();
        } catch (UnsupportedEncodingException e) {
            log("setVmSimImsi UnsupportedEncodingException");
        }
    }

    public void sendCspChangedBroadcast(boolean isOldCspPlmnEnabled, boolean isCspPlmnEnabled) {
        if (SystemProperties.getBoolean("ro.config.csp_enable", false) && isOldCspPlmnEnabled != isCspPlmnEnabled) {
            Intent intent = new Intent("android.intent.action.ACTION_HW_CSP_PLMN_CHANGE");
            intent.addFlags(536870912);
            intent.putExtra("state", this.mIccRecordsInner.isCspPlmnEnabled());
            this.mContext.sendBroadcast(intent);
            log("Broadcast, CSP Plmn Enabled change to " + this.mIccRecordsInner.isCspPlmnEnabled());
        }
    }

    public void updateMccMncConfigWithGplmn(String operatorNumeric) {
        StringBuilder sb = new StringBuilder();
        sb.append("updateMccMncConfigWithGplmn: ");
        sb.append(Log.HWINFO ? operatorNumeric : "***");
        log(sb.toString());
        if (HwTelephonyFactory.getHwUiccManager().isCDMASimCard(this.mParentApp.getPhoneId())) {
            log("cdma card, ignore updateMccMncConfiguration");
        } else if (operatorNumeric != null && operatorNumeric.length() >= 5) {
            MccTableEx.updateMccMncConfiguration(this.mContext, operatorNumeric);
        }
    }

    public void adapterForDoubleRilChannelAfterImsiReady() {
        String imsi = this.mIccRecordsInner.getImsiHw();
        int mncLength = this.mIccRecordsInner.getMncLength();
        if (imsi != null && mncLength != 0 && mncLength != -1) {
            StringBuilder sb = new StringBuilder();
            sb.append("EVENT_GET_IMSI_DONE, update mccmnc=");
            sb.append(Log.HWINFO ? imsi.substring(0, mncLength + 3) : "***");
            log(sb.toString());
            updateMccMncConfigWithGplmn(imsi.substring(0, mncLength + 3));
            if (!this.mIsImsiLoad) {
                log("EVENT_GET_IMSI_DONE, trigger notifyGetAdDone and onOperatorNumericLoadedHw.");
                String operatorName = this.mIccRecordsInner.getOperatorNumeric();
                this.mIccRecordsInner.setSystemPropertyHw("gsm.sim.operator.numeric", operatorName);
                this.mIccRecordsInner.setSystemPropertyHw("persist.sys.mcc_match_fyrom", operatorName);
                this.mIsImsiLoad = true;
                this.mParentApp.notifyGetAdDone();
                onOperatorNumericLoadedHw();
            }
        }
    }
}
