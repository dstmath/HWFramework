package com.android.internal.telephony.uicc;

import android.app.ActivityManagerNative;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.HwTelephony.VirtualNets;
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwAddonTelephonyFactory;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.HwVolteChrManagerImpl;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.dataconnection.ApnReminder;
import com.android.internal.telephony.gsm.HwEons;
import com.android.internal.telephony.gsm.HwEons.CphsType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.UsimServiceTable.UsimService;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;

public class HwSIMRecords extends SIMRecords {
    private static final String ACTION_NOTIFY_SIM_UNSUPPORT_LTE = "android.intent.action.sim_unsupport_lte";
    public static final String ANY_SIM_DETECTED = "any_sim_detect";
    private static final int CHINA_MOBILE_SIM_CARD = 1;
    private static final int CHINA_TELECOM_SIM_CARD = 3;
    private static final int CHINA_UNICOM_SIM_CARD = 2;
    private static final String DEFAULT_ICCID = "0";
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
    private static final String EXTRA_NOTIFY_ICCID = "NOTIFY_ICCID";
    private static final boolean FEATURE_POP_SIM_UNSUPPORT_LTE = false;
    private static final int INVALID_SIM_CARD = 0;
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = false;
    public static final String MULTI_PDP_PLMN_MATCHED = "multi_pdp_plmn_matched";
    private static final int NOTIFY_SIM_UNSUPPORT_LTE_ID = 123;
    private static final int OTHER_SIM_CARD = 4;
    private static final String TAG = "HwSIMRecords";
    private static final boolean isMultiSimEnabled = false;
    private static final String pRefreshMultifileProp = "gsm.sim.refresh.multifile";
    private static final String pRefreshMultifilePropExtra = "gsm.sim.refresh.multifile.extra";
    private static SIMRecordsUtils simRecordsUtils;
    private static String[] strEFIDs;
    private static UiccCardApplicationUtils uiccCardApplicationUtils;
    protected boolean bNeedSendRefreshBC;
    private GlobalChecker globalChecker;
    private Handler handlerEx;
    private boolean isEnsEnabled;
    private String mActingHplmn;
    byte[] mEfGid1;
    HwEons mEons;
    private HwCustHwSIMRecords mHwCustHwSIMRecords;

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
            if (lSimSlotVal == HwSIMRecords.EVENT_GET_GID1_HW_DONE && lDataVal == 0) {
                hasTwoCard = TelephonyManager.getDefault().hasIccCard(lDataVal);
            }
            if (lSimSlotVal == 0) {
                SystemProperties.set("gsm.huawei.RemindDataService", "false");
            } else if (HwSIMRecords.EVENT_GET_GID1_HW_DONE == lSimSlotVal) {
                SystemProperties.set("gsm.huawei.RemindDataService_1", "false");
            }
            String plmnsConfig = Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), "plmn_remind_data_service");
            if (plmnsConfig == null) {
                plmnsConfig = "26006,26003";
            }
            String[] plmns = plmnsConfig.split(",");
            int length = plmns.length;
            for (int i = HwSIMRecords.INVALID_SIM_CARD; i < length; i += HwSIMRecords.EVENT_GET_GID1_HW_DONE) {
                String plmn = plmns[i];
                if (plmn != null && plmn.equals(HwSIMRecords.this.getOperatorNumeric())) {
                    if (!"true".equals(Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED)) && (lDataVal == lSimSlotVal || !r0)) {
                        ((TelephonyManager) HwSIMRecords.this.mContext.getSystemService("phone")).setDataEnabled(HwSIMRecords.isMultiSimEnabled);
                        Systemex.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
                    }
                    if (lSimSlotVal == 0) {
                        SystemProperties.set("gsm.huawei.RemindDataService", "true");
                    } else if (HwSIMRecords.EVENT_GET_GID1_HW_DONE == lSimSlotVal) {
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
        }

        private void sendSimRecordsReadyBroadcast() {
            String operatorNumeric = HwSIMRecords.this.getOperatorNumeric();
            String imsi = HwSIMRecords.this.getIMSI();
            Rlog.d("SIMRecords", "broadcast TelephonyIntents.ACTION_SIM_RECORDS_READY");
            Intent intent = new Intent("android.intent.action.ACTION_SIM_RECORDS_READY");
            intent.addFlags(536870912);
            intent.putExtra("mccMnc", operatorNumeric);
            intent.putExtra("imsi", imsi);
            if (!(!TelephonyManager.getDefault().isMultiSimEnabled() || HwSIMRecords.this.mParentApp == null || HwSIMRecords.this.mParentApp.getUiccCard() == null)) {
                int[] subId = SubscriptionManager.getSubId(HwSIMRecords.this.mParentApp.getUiccCard().getPhoneId());
                if (subId != null && subId.length > 0) {
                    SubscriptionManager.putPhoneIdAndSubIdExtra(intent, SubscriptionManager.getPhoneId(subId[HwSIMRecords.INVALID_SIM_CARD]));
                }
            }
            ActivityManagerNative.broadcastStickyIntent(intent, null, HwSIMRecords.INVALID_SIM_CARD);
        }

        private void checkMultiPdpConfig() {
            String plmnsConfig = Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.MULTI_PDP_PLMN_MATCHED);
            Rlog.d("SIMRecords", "checkMultiPdpConfig plmnsConfig = " + plmnsConfig);
            if (plmnsConfig != null) {
                String[] plmns = plmnsConfig.split(",");
                int i = HwSIMRecords.INVALID_SIM_CARD;
                int length = plmns.length;
                while (i < length) {
                    String plmn = plmns[i];
                    if (plmn == null || !plmn.equals(HwSIMRecords.this.getOperatorNumeric())) {
                        i += HwSIMRecords.EVENT_GET_GID1_HW_DONE;
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
            int i = HwSIMRecords.INVALID_SIM_CARD;
            while (i < length) {
                String plmn = plmns[i];
                if (plmn == null || !plmn.equals(HwSIMRecords.this.getOperatorNumeric())) {
                    i += HwSIMRecords.EVENT_GET_GID1_HW_DONE;
                } else {
                    if (!"true".equals(Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED))) {
                        ((TelephonyManager) HwSIMRecords.this.mContext.getSystemService("phone")).setDataEnabled(HwSIMRecords.isMultiSimEnabled);
                    }
                    Systemex.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
                    SystemProperties.set("gsm.huawei.RemindDataService", "true");
                    return;
                }
            }
            Systemex.putString(HwSIMRecords.this.mContext.getContentResolver(), HwSIMRecords.ANY_SIM_DETECTED, "true");
        }

        private void checkGsmOnlyDataNotAllowed() {
            if (HwSIMRecords.isMultiSimEnabled) {
                int[] subIds = SubscriptionManager.getSubId(HwSIMRecords.this.getSlotId());
                if (subIds != null) {
                    TelephonyManager.setTelephonyProperty(subIds[HwSIMRecords.INVALID_SIM_CARD], "gsm.data.gsm_only_not_allow_ps", "false");
                } else {
                    return;
                }
            }
            SystemProperties.set("gsm.data.gsm_only_not_allow_ps", "false");
            String plmnGsmonlyPsNotallowd = Systemex.getString(HwSIMRecords.this.mContext.getContentResolver(), "hw_2gonly_psnotallowed");
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
            for (int i = HwSIMRecords.INVALID_SIM_CARD; i < regplmnCustomArrayLen; i += HwSIMRecords.EVENT_GET_GID1_HW_DONE) {
                HwSIMRecords.this.log("is2GonlyPsAllowed plmnCustomArray[" + i + "] = " + plmnCustomArray[i]);
                if (hplmn.equals(plmnCustomArray[i])) {
                    if (HwSIMRecords.isMultiSimEnabled) {
                        TelephonyManager.setTelephonyProperty(SubscriptionManager.getSubId(HwSIMRecords.this.getSlotId())[HwSIMRecords.INVALID_SIM_CARD], "gsm.data.gsm_only_not_allow_ps", "true");
                    } else {
                        SystemProperties.set("gsm.data.gsm_only_not_allow_ps", "true");
                    }
                    return;
                }
            }
        }

        public void loadGID1() {
            HwSIMRecords.this.mFh.loadEFTransparent(28478, HwSIMRecords.this.handlerEx.obtainMessage(HwSIMRecords.EVENT_GET_GID1_HW_DONE));
            HwSIMRecords hwSIMRecords = HwSIMRecords.this;
            hwSIMRecords.mRecordsToLoad += HwSIMRecords.EVENT_GET_GID1_HW_DONE;
        }

        public void loadGID1Ex() {
            if ((HwSIMRecords.this.mFh instanceof UsimFileHandler) && !"3F007FFF".equals(HwSIMRecords.this.mFh.getEFPath(28478))) {
                HwSIMRecords.this.mFh.loadEFTransparent("3F007FFF", 28478, HwSIMRecords.this.handlerEx.obtainMessage(HwSIMRecords.EVENT_GET_GID1_HW_DONE_EX), true);
                HwSIMRecords hwSIMRecords = HwSIMRecords.this;
                hwSIMRecords.mRecordsToLoad += HwSIMRecords.EVENT_GET_GID1_HW_DONE;
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
                if (HwSIMRecords.this.getSlotId() == HwSIMRecords.EVENT_GET_GID1_HW_DONE && TelephonyManager.getDefault().getSimState(HwSIMRecords.INVALID_SIM_CARD) == 5) {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.HwSIMRecords.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.HwSIMRecords.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.HwSIMRecords.<clinit>():void");
    }

    public HwSIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.bNeedSendRefreshBC = isMultiSimEnabled;
        this.mActingHplmn = "";
        this.isEnsEnabled = SystemProperties.getBoolean("ro.config.hw_is_ens_enabled", isMultiSimEnabled);
        this.mEfGid1 = null;
        this.mEons = new HwEons();
        this.globalChecker = new GlobalChecker(this);
        this.handlerEx = new Handler() {
            public void handleMessage(Message msg) {
                boolean isRecordLoadResponse = HwSIMRecords.isMultiSimEnabled;
                if (HwSIMRecords.this.mDestroyed.get()) {
                    HwSIMRecords.this.loge("Received message " + msg + "[" + msg.what + "] " + " while being destroyed. Ignoring.");
                    return;
                }
                try {
                    AsyncResult ar;
                    switch (msg.what) {
                        case HwSIMRecords.EVENT_GET_GID1_HW_DONE /*1*/:
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
                        case HwSIMRecords.EVENT_GET_SPECIAL_FILE_DONE /*2*/:
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
                        case HwSIMRecords.EVENT_GET_GID1_HW_DONE_EX /*3*/:
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
        Object[] objArr = new Object[EVENT_GET_SPECIAL_FILE_DONE];
        objArr[INVALID_SIM_CARD] = this;
        objArr[EVENT_GET_GID1_HW_DONE] = c;
        this.mHwCustHwSIMRecords = (HwCustHwSIMRecords) HwCustUtils.createObj(HwCustHwSIMRecords.class, objArr);
        if (getIccidSwitch()) {
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(simRecordsUtils.getEventIccidDone(null)));
            } else {
                this.mFh.loadEFTransparent(12258, obtainMessage(simRecordsUtils.getEventIccidDone(null)));
            }
            this.mRecordsToLoad += EVENT_GET_GID1_HW_DONE;
        }
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = isMultiSimEnabled;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
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
                            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(EVENT_HW_CUST_BASE));
                            break;
                        }
                        Rlog.e("SIMRecords", "[EONS] Exception in fetching OPL Records: " + ar.exception);
                        this.mEons.resetOplData();
                        break;
                    case EVENT_GET_ALL_PNN_RECORDS_DONE /*102*/:
                        isRecordLoadResponse = true;
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            this.mEons.setPnnData((ArrayList) ar.result);
                            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(EVENT_HW_CUST_BASE));
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
                            this.mSpnDisplayCondition = data[INVALID_SIM_CARD] & HwSubscriptionManager.SUB_INIT_STATE;
                            String spn = IccUtils.adnStringFieldToString(data, EVENT_GET_GID1_HW_DONE, data.length - 1);
                            setServiceProviderName(spn);
                            setSystemProperty("gsm.sim.operator.alpha", spn);
                            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(EVENT_GET_SPECIAL_FILE_DONE));
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
                            if (15 != mHplmnData[INVALID_SIM_CARD]) {
                                int length;
                                StringBuffer buffer = new StringBuffer();
                                if (15 == mHplmnData[5]) {
                                    length = 5;
                                } else {
                                    length = 6;
                                }
                                for (int i = INVALID_SIM_CARD; i < length; i += EVENT_GET_GID1_HW_DONE) {
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
                        if (msg.obj.exception == null) {
                            this.mIs3Gphonebook = true;
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
            this.bNeedSendRefreshBC = isMultiSimEnabled;
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
            case INVALID_SIM_CARD /*0*/:
                rlog("beforeHandleSimRefresh with REFRESH_RESULT_FILE_UPDATE");
                if (HW_IS_CHINA_TELECOM && this.mParentApp != null && uiccCardApplicationUtils.getUiccCard(this.mParentApp) == UiccController.getInstance().getUiccCard(slotId)) {
                    rlog("Do not handleSimRefresh with SIM_FILE_UPDATED sent by RUIM.");
                    return true;
                } else if (hwCustHandleSimRefresh(refreshResponse.efId)) {
                    return true;
                }
                break;
            case EVENT_GET_GID1_HW_DONE /*1*/:
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
            case EVENT_GET_SPECIAL_FILE_DONE /*2*/:
                rlog("beforeHandleSimRefresh with SIM_REFRESH_RESET");
                break;
            default:
                rlog("beforeHandleSimRefresh with unknown operation");
                break;
        }
        return isMultiSimEnabled;
    }

    private boolean hwCustHandleSimRefresh(int efid) {
        if (HwVolteChrManagerImpl.MAX_MONITOR_TIME == efid) {
            String strEFID = SystemProperties.get(pRefreshMultifileProp, "");
            rlog("The strEFID is: " + strEFID);
            if (strEFID.isEmpty()) {
                rlog("handleSimRefresh with no multifile found");
                return isMultiSimEnabled;
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
            for (int i = INVALID_SIM_CARD; i < strEFIDs.length; i += EVENT_GET_GID1_HW_DONE) {
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
        return isMultiSimEnabled;
    }

    public boolean afterHandleSimRefresh(IccRefreshResponse refreshResponse) {
        switch (refreshResponse.refreshResult) {
            case INVALID_SIM_CARD /*0*/:
                rlog("afterHandleSimRefresh with REFRESH_RESULT_FILE_UPDATE");
                synchronized (this) {
                    this.mIccRefreshRegistrants.notifyRegistrants();
                    break;
                }
                break;
            case EVENT_GET_GID1_HW_DONE /*1*/:
                rlog("afterHandleSimRefresh with SIM_REFRESH_INIT");
                break;
            case EVENT_GET_SPECIAL_FILE_DONE /*2*/:
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
        return isMultiSimEnabled;
    }

    protected static void rlog(String string) {
        Rlog.d(TAG, string);
    }

    public byte[] getGID1() {
        if (this.mEfGid1 != null) {
            return Arrays.copyOf(this.mEfGid1, this.mEfGid1.length);
        }
        byte[] bArr = new byte[EVENT_GET_GID1_HW_DONE];
        bArr[INVALID_SIM_CARD] = (byte) 0;
        return bArr;
    }

    public void setVoiceMailNumber(String voiceNumber) {
        this.mVoiceMailNum = voiceNumber;
    }

    public void loadFile(String matchPath, String matchFile) {
        if (matchPath != null && matchPath.length() >= EVENT_GET_SPECIAL_FILE_DONE && matchPath.substring(INVALID_SIM_CARD, EVENT_GET_SPECIAL_FILE_DONE).equalsIgnoreCase("0x") && matchFile != null && matchFile.length() >= EVENT_GET_SPECIAL_FILE_DONE && matchFile.substring(INVALID_SIM_CARD, EVENT_GET_SPECIAL_FILE_DONE).equalsIgnoreCase("0x")) {
            String matchFileString = matchFile.substring(EVENT_GET_SPECIAL_FILE_DONE);
            int matchField = INVALID_SIM_CARD;
            for (int i = INVALID_SIM_CARD; i < matchFileString.length(); i += EVENT_GET_GID1_HW_DONE) {
                matchField = (int) (((double) matchField) + (Math.pow(16.0d, (double) ((matchFileString.length() - i) - 1)) * ((double) HwIccUtils.hexCharToInt(matchFileString.charAt(i)))));
            }
            Message message = this.handlerEx.obtainMessage(EVENT_GET_SPECIAL_FILE_DONE);
            Bundle data = new Bundle();
            data.putString(VirtualNets.MATCH_PATH, matchPath);
            data.putString(VirtualNets.MATCH_FILE, matchFile);
            message.setData(data);
            this.mRecordsToLoad += EVENT_GET_GID1_HW_DONE;
            this.mFh.loadEFTransparent(matchPath.substring(EVENT_GET_SPECIAL_FILE_DONE), matchField, message);
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
        if (FEATURE_POP_SIM_UNSUPPORT_LTE && DEFAULT_ICCID.equals(getNotifyUnsupportCardIccId())) {
            UiccCard uiccCard = this.mParentApp.getUiccCard();
            if (uiccCard != null) {
                int masterCardSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                if (masterCardSlot == getSlotId()) {
                    popUnsupportLte(uiccCard, masterCardSlot, iccid);
                    return;
                } else {
                    Rlog.d(TAG, "The card is not masterCard, don't process notify unsupport lte.");
                    return;
                }
            }
            Rlog.d(TAG, "uiccCard is error.");
        }
    }

    private void popUnsupportLte(UiccCard uiccCard, int slotId, String iccid) {
        AppType apptype = this.mParentApp.getType();
        boolean is3gCard = (apptype == AppType.APPTYPE_CSIM || apptype == AppType.APPTYPE_ISIM) ? true : apptype == AppType.APPTYPE_USIM ? true : isMultiSimEnabled;
        int networkType = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode", 9);
        int simOperator = getSimOperatorFromImsi(getIMSI());
        Rlog.d(TAG, "networkType = " + networkType + ",simOperator = " + simOperator + ",apptype = " + apptype);
        if (is3gCard && ((networkType == 9 || networkType == 0) && (simOperator == EVENT_GET_GID1_HW_DONE || simOperator == EVENT_GET_SPECIAL_FILE_DONE))) {
            setNotifyUnsupportCardIccId(iccid);
        } else {
            setNotifyUnsupportCardIccId(DEFAULT_ICCID);
        }
    }

    private int getSimOperatorFromImsi(String imsi) {
        if (imsi == null) {
            return INVALID_SIM_CARD;
        }
        if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007") || imsi.startsWith("46008")) {
            return EVENT_GET_GID1_HW_DONE;
        }
        if (imsi.startsWith("46001") || imsi.startsWith("46009")) {
            return EVENT_GET_SPECIAL_FILE_DONE;
        }
        if (imsi.startsWith("46003") || imsi.startsWith("46011") || imsi.startsWith("46012") || imsi.startsWith("45502")) {
            return EVENT_GET_GID1_HW_DONE_EX;
        }
        return OTHER_SIM_CARD;
    }

    private void setNotifyUnsupportCardIccId(String iccId) {
        if (iccId != null) {
            if (iccId.length() > 91) {
                iccId = iccId.substring(INVALID_SIM_CARD, 91);
            }
            SystemProperties.set("persist.radio.sim.iccid", iccId);
        }
    }

    private String getNotifyUnsupportCardIccId() {
        return SystemProperties.get("persist.radio.sim.iccid", DEFAULT_ICCID);
    }

    protected void onImsiLoadedHw() {
        this.globalChecker.onImsiLoaded();
    }

    protected void custMncLength(String mcc) {
        int i = INVALID_SIM_CARD;
        String mncHaving2Digits = SystemProperties.get("ro.config.mnc_having_2digits", "");
        Rlog.d("SIMRecords", "mnc_having_2digits = " + mncHaving2Digits);
        if (mncHaving2Digits != null) {
            String custMccmncCode = this.mImsi.substring(INVALID_SIM_CARD, 5);
            String[] plmns = mncHaving2Digits.split(",");
            int length = plmns.length;
            while (i < length) {
                if (custMccmncCode.equals(plmns[i])) {
                    this.mMncLength = EVENT_GET_SPECIAL_FILE_DONE;
                    return;
                }
                i += EVENT_GET_GID1_HW_DONE;
            }
        } else if (mcc.equals("416") && EVENT_GET_GID1_HW_DONE_EX == this.mMncLength) {
            Rlog.d("SIMRecords", "SIMRecords: customize for Jordan sim card, make the mcnLength to 2");
            this.mMncLength = EVENT_GET_SPECIAL_FILE_DONE;
        }
    }

    public String getOperatorNumericEx(ContentResolver cr, String name) {
        if (cr == null || this.mImsi == null || "".equals(this.mImsi) || name == null || "".equals(name)) {
            return getOperatorNumeric();
        }
        String hwImsiPlmnEx = Systemex.getString(cr, name);
        if (!(hwImsiPlmnEx == null || "".equals(hwImsiPlmnEx))) {
            String[] plmn_ex = hwImsiPlmnEx.split(",");
            int length = plmn_ex.length;
            for (int i = INVALID_SIM_CARD; i < length; i += EVENT_GET_GID1_HW_DONE) {
                String plmn_item = plmn_ex[i];
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
        this.mIs3Gphonebook = isMultiSimEnabled;
        this.mIsGetPBRDone = isMultiSimEnabled;
    }

    protected void getPbrRecordSize() {
        this.mFh.loadEFLinearFixedAll(20272, obtainMessage(EVENT_GET_PBR_DONE));
        this.mRecordsToLoad += EVENT_GET_GID1_HW_DONE;
    }

    public int getSlotId() {
        if (this.mParentApp != null && this.mParentApp.getUiccCard() != null) {
            return this.mParentApp.getUiccCard().getPhoneId();
        }
        log("error , mParentApp.getUiccCard  is null");
        return INVALID_SIM_CARD;
    }

    protected void setVoiceMailByCountry(String spn) {
        log("setVoiceMailByCountry spn " + spn + " for slot" + getSlotId());
        String number;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(getSlotId()))) {
                number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail(getSlotId());
                spn = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(getSlotId()));
                if (this.mVmConfig.containsCarrier(spn, getSlotId())) {
                    if (number == null || number.isEmpty()) {
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
                if (number == null || number.isEmpty()) {
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
        rlog("check file status in serivce table " + efid);
        switch (efid) {
            case 28486:
                rlog("check EF_SPN serivice in serivice table!!");
                if (this.mParentApp.getUiccCard().isApplicationOnIcc(AppType.APPTYPE_USIM)) {
                    if (usimServiceTable == null || usimServiceTable.isAvailable(UsimService.SPN)) {
                        return true;
                    }
                    rlog("EF_SPN is disable in 3G card!!");
                    return isMultiSimEnabled;
                } else if (!this.mParentApp.getUiccCard().isApplicationOnIcc(AppType.APPTYPE_SIM)) {
                    return true;
                } else {
                    int mSstSpnValue = (data[OTHER_SIM_CARD] & 15) & EVENT_GET_GID1_HW_DONE_EX;
                    if (EVENT_GET_GID1_HW_DONE_EX == mSstSpnValue) {
                        rlog("SST: 2G Sim,SPNVALUE enabled SPNVALUE = " + mSstSpnValue);
                        return true;
                    }
                    rlog("SST: 2G Sim,SPNVALUE disabled  SPNVALUE = " + mSstSpnValue);
                    return isMultiSimEnabled;
                }
            default:
                return true;
        }
    }

    protected void loadEons() {
        this.mFh.loadEFLinearFixedAll(28614, obtainMessage(EVENT_GET_ALL_OPL_RECORDS_DONE));
        this.mRecordsToLoad += EVENT_GET_GID1_HW_DONE;
        this.mFh.loadEFLinearFixedAll(28613, obtainMessage(EVENT_GET_ALL_PNN_RECORDS_DONE));
        this.mRecordsToLoad += EVENT_GET_GID1_HW_DONE;
        this.mFh.loadEFTransparent(28436, obtainMessage(EVENT_GET_SPN_CPHS_DONE));
        this.mRecordsToLoad += EVENT_GET_GID1_HW_DONE;
        this.mFh.loadEFTransparent(28440, obtainMessage(EVENT_GET_SPN_SHORT_CPHS_DONE));
        this.mRecordsToLoad += EVENT_GET_GID1_HW_DONE;
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
            QueryHandler mQueryHandler = new QueryHandler(this.mContext.getContentResolver());
            Uri withAppendedId = ContentUris.withAppendedId(Uri.parse("content://icc/fdn/subId/"), (long) slotId);
            String[] strArr = new String[EVENT_GET_GID1_HW_DONE];
            strArr[INVALID_SIM_CARD] = "number";
            mQueryHandler.startQuery(INVALID_SIM_CARD, null, withAppendedId, strArr, null, null, null);
        }
    }

    public void sendDualSimChangeBroadcast(boolean isSimImsiRefreshing, String mLastImsi, String mImsi) {
        if (isSimImsiRefreshing && mLastImsi != null && mImsi != null && !mLastImsi.equals(mImsi)) {
            ActivityManagerNative.broadcastStickyIntent(new Intent("android.intent.action.ACTION_DUAL_SIM_IMSI_CHANGE"), null, INVALID_SIM_CARD);
            Rlog.d("SIMRecords", "dual sim imsi change");
        }
    }

    public void loadCardSpecialFile(int fileid) {
        switch (fileid) {
            case 20276:
                if (this.isEnsEnabled) {
                    this.mFh.loadEFTransparent(20276, obtainMessage(EVENT_GET_ACTING_HPLMN_DONE));
                    this.mRecordsToLoad += EVENT_GET_GID1_HW_DONE;
                }
            default:
                Rlog.d("SIMRecords", "no fileid found for load");
        }
    }

    public String getActingHplmn() {
        return this.mActingHplmn;
    }

    private int[] getSimPlmnDigits(byte[] data) {
        if (data == null) {
            int[] iArr = new int[EVENT_GET_GID1_HW_DONE];
            iArr[INVALID_SIM_CARD] = 15;
            return iArr;
        }
        int[] simPlmn = new int[]{INVALID_SIM_CARD, INVALID_SIM_CARD, INVALID_SIM_CARD, INVALID_SIM_CARD, INVALID_SIM_CARD, INVALID_SIM_CARD};
        simPlmn[INVALID_SIM_CARD] = data[INVALID_SIM_CARD] & 15;
        simPlmn[EVENT_GET_GID1_HW_DONE] = (data[INVALID_SIM_CARD] >> OTHER_SIM_CARD) & 15;
        simPlmn[EVENT_GET_SPECIAL_FILE_DONE] = data[EVENT_GET_GID1_HW_DONE] & 15;
        simPlmn[EVENT_GET_GID1_HW_DONE_EX] = data[EVENT_GET_SPECIAL_FILE_DONE] & 15;
        simPlmn[OTHER_SIM_CARD] = (data[EVENT_GET_SPECIAL_FILE_DONE] >> OTHER_SIM_CARD) & 15;
        simPlmn[5] = (data[EVENT_GET_GID1_HW_DONE] >> OTHER_SIM_CARD) & 15;
        return simPlmn;
    }
}
