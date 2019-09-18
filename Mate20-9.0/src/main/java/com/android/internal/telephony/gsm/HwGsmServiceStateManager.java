package com.android.internal.telephony.gsm;

import android.common.HwCfgKey;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CustPlmnMember;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwAddonTelephonyFactory;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.HwPlmnActConcat;
import com.android.internal.telephony.HwReportManagerImpl;
import com.android.internal.telephony.HwServiceStateManager;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.NitzStateMachine;
import com.android.internal.telephony.OnsDisplayParams;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PlmnConstants;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.ServiceStateTrackerUtils;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.VirtualNet;
import com.android.internal.telephony.VirtualNetOnsExtend;
import com.android.internal.telephony.dataconnection.ApnReminder;
import com.android.internal.telephony.dataconnection.InCallDataStateMachine;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimUtils;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import huawei.cust.HwGetCfgFileConfig;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

public class HwGsmServiceStateManager extends HwServiceStateManager {
    private static final int AIRPLANE_MODE_ON = 1;
    private static final int CDMA_LEVEL = 8;
    private static final String CHINAMOBILE_MCCMNC = "46000;46002;46007;46008;46004";
    private static final String CHINA_TELECOM_SPN = "%E4%B8%AD%E5%9B%BD%E7%94%B5%E4%BF%A1";
    private static final String CLOUD_OTA_DPLMN_UPDATE = "cloud.ota.dplmn.UPDATE";
    private static final String CLOUD_OTA_MCC_UPDATE = "cloud.ota.mcc.UPDATE";
    private static final String CLOUD_OTA_PERMISSION = "huawei.permission.RECEIVE_CLOUD_OTA_UPDATA";
    private static final int DELAY_DURING_TIME = SystemProperties.getInt("ro.signalsmooth.delaytimer", VALUE_DELAY_DURING_TIME);
    private static final String EMERGENCY_PLMN = Resources.getSystem().getText(17039987).toString();
    private static final int ESM_FAILURE = 19;
    private static final int EVDO_LEVEL = 16;
    private static final int EVENT_4R_MIMO_ENABLE = 106;
    private static final int EVENT_CA_STATE_CHANGED = 104;
    private static final int EVENT_CRR_CONN = 152;
    private static final int EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE = 155;
    private static final int EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH = 103;
    private static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 102;
    private static final int EVENT_DSDS_MODE = 153;
    private static final int EVENT_MCC_CHANGED = 154;
    private static final int EVENT_NETWORK_REJECTED_CASE = 105;
    private static final int EVENT_PLMN_SELINFO = 151;
    private static final int EVENT_POLL_LOCATION_INFO = 64;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 101;
    private static final int GSM_LEVEL = 1;
    private static final int GSM_STRENGTH_POOR_STD = -109;
    private static final String INTERNATIONAL_MCC = "901";
    private static final boolean IS_VERIZON;
    private static final boolean KEEP_3GPLUS_HPLUS = SystemProperties.getBoolean("ro.config.keep_3gplus_hplus", false);
    private static final String LOG_TAG = "HwGsmServiceStateManager";
    private static final int LTE_LEVEL = 4;
    /* access modifiers changed from: private */
    public static final int LTE_RSSNR_POOR_STD = SystemProperties.getInt("ro.lte.rssnrpoorstd", -5);
    private static final int LTE_RSSNR_UNKOUWN_STD = 99;
    /* access modifiers changed from: private */
    public static final int LTE_STRENGTH_POOR_STD = SystemProperties.getInt("ro.lte.poorstd", -125);
    private static final int LTE_STRENGTH_UNKOUWN_STD = -44;
    private static final int L_RSSNR_POOR_STD = -5;
    private static final int L_STRENGTH_POOR_STD = -125;
    private static final int MCC_LENGTH = 3;
    private static final boolean MIMO_4R_REPORT = SystemProperties.getBoolean("ro.config.hw_4.5gplus", false);
    private static final int NETWORK_MODE_GSM_UMTS = 3;
    private static final int NITZ_UPDATE_SPACING_TIME = 1800000;
    private static final String NO_SERVICE_PLMN = Resources.getSystem().getText(17040350).toString();
    private static final int NR_LEVEL = 32;
    /* access modifiers changed from: private */
    public static final int NR_RSSNR_POOR_STD = SystemProperties.getInt("ro.nr.rssnrpoorstd", -5);
    private static final int NR_RSSNR_UNKOUWN_STD = 99;
    /* access modifiers changed from: private */
    public static final int NR_STRENGTH_POOR_STD = SystemProperties.getInt("ro.nr.poorstd", -125);
    private static final int NR_STRENGTH_UNKOUWN_STD = -44;
    private static final int N_RSSNR_POOR_STD = -5;
    private static final int N_STRENGTH_POOR_STD = -125;
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final String PROPERTY_GLOBAL_FORCE_TO_SET_ECC = "ril.force_to_set_ecc";
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final boolean PS_CLEARCODE = SystemProperties.getBoolean("ro.config.hw_clearcode_pdp", false);
    private static final int RAT_LTE = 2;
    private static final String REGEX = "((\\d{5,14},\\d{5,14},[^:,;]{1,20};)){1,}$";
    private static final int REJ_TIMES = 3;
    private static final boolean SHOW_4G_PLUS_ICON = SystemProperties.getBoolean("ro.config.hw_show_4G_Plus_icon", false);
    private static final boolean SHOW_REJ_INFO_KT = SystemProperties.getBoolean("ro.config.show_rej_info", false);
    private static final int TIME_NOT_SET = 0;
    private static final int UMTS_LEVEL = 2;
    private static final int VALUE_DELAY_DURING_TIME = 6000;
    private static final int WCDMA_ECIO_NONE = 255;
    private static final int WCDMA_ECIO_POOR_STD = SystemProperties.getInt("ro.wcdma.eciopoorstd", W_ECIO_POOR_STD);
    /* access modifiers changed from: private */
    public static final int WCDMA_STRENGTH_POOR_STD = SystemProperties.getInt("ro.wcdma.poorstd", W_STRENGTH_POOR_STD);
    private static final int W_ECIO_POOR_STD = -17;
    private static final int W_STRENGTH_POOR_STD = -112;
    /* access modifiers changed from: private */
    public final boolean FEATURE_SIGNAL_DUALPARAM = SystemProperties.getBoolean("signal.dualparam", false);
    private int lastCid = -1;
    private int lastLac = -1;
    private int lastType = -1;
    CommandsInterface mCi;
    private CloudOtaBroadcastReceiver mCloudOtaBroadcastReceiver = new CloudOtaBroadcastReceiver();
    private Context mContext;
    /* access modifiers changed from: private */
    public ContentResolver mCr;
    private DoubleSignalStrength mDoubleSignalStrength;
    /* access modifiers changed from: private */
    public GsmCdmaPhone mGsmPhone;
    /* access modifiers changed from: private */
    public ServiceStateTracker mGsst;
    /* access modifiers changed from: private */
    public HwCustGsmServiceStateManager mHwCustGsmServiceStateManager;
    /* access modifiers changed from: private */
    public HwSignalStrength mHwSigStr;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean airplaneMode;
            String column;
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.refreshapn".equals(action)) {
                    Rlog.i(HwGsmServiceStateManager.LOG_TAG, "refresh apn worked,updateSpnDisplay.");
                    HwGsmServiceStateManager.this.mGsst.updateSpnDisplay();
                } else {
                    boolean airplaneMode2 = false;
                    if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                        String simState = (String) intent.getExtra("ss");
                        int slotId = intent.getIntExtra("slot", -1000);
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "simState = " + simState + " slotId = " + slotId);
                        if ("ABSENT".equals(simState)) {
                            VirtualNet.removeVirtualNet(slotId);
                            Rlog.i(HwGsmServiceStateManager.LOG_TAG, "sim absent, reset");
                            if (-1 != SystemProperties.getInt("gsm.sim.updatenitz", -1)) {
                                SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                            }
                            if (slotId == HwGsmServiceStateManager.this.mPhoneId) {
                                boolean unused = HwGsmServiceStateManager.this.mRejectflag = false;
                                Rlog.i(HwGsmServiceStateManager.LOG_TAG, "sim absent, clear mRejectflag");
                            }
                        } else if ("LOADED".equals(simState) && slotId == HwGsmServiceStateManager.this.mPhoneId) {
                            Rlog.i(HwGsmServiceStateManager.LOG_TAG, "after simrecords loaded,updateSpnDisplay for virtualnet ons.");
                            HwGsmServiceStateManager.this.mGsst.updateSpnDisplay();
                        }
                    } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(action)) {
                        int slotId2 = intent.getIntExtra("subscription", -1);
                        int intValue = intent.getIntExtra("intContent", 0);
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "Received ACTION_SUBINFO_CONTENT_CHANGE on slotId: " + slotId2 + " for " + column + ", intValue: " + intValue);
                        StringBuilder sb = new StringBuilder();
                        sb.append("PROPERTY_GSM_SIM_UPDATE_NITZ=");
                        sb.append(SystemProperties.getInt("gsm.sim.updatenitz", -1));
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, sb.toString());
                        if ("sub_state".equals(column) && -1 != slotId2 && intValue == 0 && slotId2 == SystemProperties.getInt("gsm.sim.updatenitz", -1)) {
                            Rlog.i(HwGsmServiceStateManager.LOG_TAG, "reset PROPERTY_GSM_SIM_UPDATE_NITZ");
                            SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                        }
                    } else if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(action)) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("[SLOT");
                        sb2.append(HwGsmServiceStateManager.this.mPhoneId);
                        sb2.append("] CardState: ");
                        sb2.append(intent.getIntExtra("newSubState", -1));
                        sb2.append("IsMphone: ");
                        sb2.append(HwGsmServiceStateManager.this.mPhoneId == intent.getIntExtra("phone", 0));
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, sb2.toString());
                        if (intent.getIntExtra("operationResult", 1) == 0 && HwGsmServiceStateManager.this.mPhoneId == intent.getIntExtra("phone", 0) && HwGsmServiceStateManager.this.hasMessages(HwGsmServiceStateManager.EVENT_DELAY_UPDATE_REGISTER_STATE_DONE) && intent.getIntExtra("newSubState", -1) == 0) {
                            HwGsmServiceStateManager.this.cancelDeregisterStateDelayTimer();
                            HwGsmServiceStateManager.this.mGsst.pollState();
                        }
                    } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                        if (Settings.Global.getInt(HwGsmServiceStateManager.this.mCr, "airplane_mode_on", 0) == 1) {
                            airplaneMode2 = true;
                        }
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, "airplaneMode: " + airplaneMode);
                        if (airplaneMode) {
                            SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                        }
                    } else if (HwGsmServiceStateManager.this.mHwCustGsmServiceStateManager != null) {
                        int unused2 = HwGsmServiceStateManager.this.mRac = HwGsmServiceStateManager.this.mHwCustGsmServiceStateManager.handleBroadcastReceived(context, intent, HwGsmServiceStateManager.this.mRac);
                    }
                }
            }
        }
    };
    private SignalStrength mModemSignalStrength;
    private int mOldCAstate = 0;
    private DoubleSignalStrength mOldDoubleSignalStrength;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    /* access modifiers changed from: private */
    public int mRac = -1;
    /* access modifiers changed from: private */
    public boolean mRejectflag = false;
    private boolean mis4RMimoEnable = false;
    private String oldRplmn = "";
    private int rejNum = 0;
    private String rplmn = "";

    private class CloudOtaBroadcastReceiver extends BroadcastReceiver {
        private CloudOtaBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (HwGsmServiceStateManager.this.mGsmPhone != null && HwGsmServiceStateManager.this.mCi != null) {
                String action = intent.getAction();
                if ("cloud.ota.mcc.UPDATE".equals(action)) {
                    Rlog.e(HwGsmServiceStateManager.LOG_TAG, "HwCloudOTAService CLOUD_OTA_MCC_UPDATE");
                    HwGsmServiceStateManager.this.mCi.sendCloudMessageToModem(1);
                } else if ("cloud.ota.dplmn.UPDATE".equals(action)) {
                    Rlog.e(HwGsmServiceStateManager.LOG_TAG, "HwCloudOTAService CLOUD_OTA_DPLMN_UPDATE");
                    HwGsmServiceStateManager.this.mCi.sendCloudMessageToModem(2);
                }
            }
        }
    }

    private class DoubleSignalStrength {
        private static final double VALUE_NEW_COEF_QUA_DES_SS = 0.15d;
        private static final int VALUE_NEW_COEF_STR_DES_SS = 5;
        private static final double VALUE_OLD_COEF_QUA_DES_SS = 0.85d;
        private static final int VALUE_OLD_COEF_STR_DES_SS = 7;
        private int mDelayTime;
        private double mDoubleGsmSS;
        private double mDoubleLteRsrp;
        private double mDoubleLteRssnr;
        private double mDoubleNrRsrp;
        private double mDoubleNrRssnr;
        private double mDoubleWcdmaEcio;
        private double mDoubleWcdmaRscp;
        private double mOldDoubleGsmSS;
        private double mOldDoubleLteRsrp;
        private double mOldDoubleLteRssnr;
        private double mOldDoubleNrRsrp;
        private double mOldDoubleNrRssnr;
        private double mOldDoubleWcdmaEcio;
        private double mOldDoubleWcdmaRscp;
        private int mTechState;

        public DoubleSignalStrength(SignalStrength ss) {
            this.mDoubleNrRsrp = (double) ss.getNrRsrp();
            this.mDoubleNrRssnr = (double) ss.getNrRssnr();
            this.mDoubleLteRsrp = (double) ss.getLteRsrp();
            this.mDoubleLteRssnr = (double) ss.getLteRssnr();
            this.mDoubleWcdmaRscp = (double) ss.getWcdmaRscp();
            this.mDoubleWcdmaEcio = (double) ss.getWcdmaEcio();
            this.mDoubleGsmSS = (double) ss.getGsmSignalStrength();
            this.mTechState = 0;
            if (ss.getGsmSignalStrength() < -1) {
                this.mTechState |= 1;
            }
            if (ss.getWcdmaRscp() < -1) {
                this.mTechState |= 2;
            }
            if (ss.getLteRsrp() < -1) {
                this.mTechState |= 4;
            }
            if (ss.getNrRsrp() < -1) {
                this.mTechState |= 32;
            }
            this.mOldDoubleNrRsrp = this.mDoubleNrRsrp;
            this.mOldDoubleNrRssnr = this.mDoubleNrRssnr;
            this.mOldDoubleLteRsrp = this.mDoubleLteRsrp;
            this.mOldDoubleLteRssnr = this.mDoubleLteRssnr;
            this.mOldDoubleWcdmaRscp = this.mDoubleWcdmaRscp;
            this.mOldDoubleWcdmaEcio = this.mDoubleWcdmaEcio;
            this.mOldDoubleGsmSS = this.mDoubleGsmSS;
            this.mDelayTime = 0;
        }

        public DoubleSignalStrength(DoubleSignalStrength doubleSS) {
            this.mDoubleNrRsrp = doubleSS.mDoubleNrRsrp;
            this.mDoubleNrRssnr = doubleSS.mDoubleNrRssnr;
            this.mDoubleLteRsrp = doubleSS.mDoubleLteRsrp;
            this.mDoubleLteRssnr = doubleSS.mDoubleLteRssnr;
            this.mDoubleWcdmaRscp = doubleSS.mDoubleWcdmaRscp;
            this.mDoubleWcdmaEcio = doubleSS.mDoubleWcdmaEcio;
            this.mDoubleGsmSS = doubleSS.mDoubleGsmSS;
            this.mTechState = doubleSS.mTechState;
            this.mOldDoubleNrRsrp = doubleSS.mDoubleNrRsrp;
            this.mOldDoubleNrRssnr = doubleSS.mDoubleNrRssnr;
            this.mOldDoubleLteRsrp = doubleSS.mDoubleLteRsrp;
            this.mOldDoubleLteRssnr = doubleSS.mDoubleLteRssnr;
            this.mOldDoubleWcdmaRscp = doubleSS.mDoubleWcdmaRscp;
            this.mOldDoubleWcdmaEcio = doubleSS.mDoubleWcdmaEcio;
            this.mOldDoubleGsmSS = doubleSS.mDoubleGsmSS;
            this.mDelayTime = doubleSS.mDelayTime;
        }

        public double getDoubleNrRsrp() {
            return this.mDoubleNrRsrp;
        }

        public double getDoubleNrRssnr() {
            return this.mDoubleNrRssnr;
        }

        public double getDoubleLteRsrp() {
            return this.mDoubleLteRsrp;
        }

        public double getDoubleLteRssnr() {
            return this.mDoubleLteRssnr;
        }

        public double getDoubleWcdmaRscp() {
            return this.mDoubleWcdmaRscp;
        }

        public double getDoubleWcdmaEcio() {
            return this.mDoubleWcdmaEcio;
        }

        public double getDoubleGsmSignalStrength() {
            return this.mDoubleGsmSS;
        }

        public double getOldDoubleNrRsrp() {
            return this.mOldDoubleNrRsrp;
        }

        public double getOldDoubleNrRssnr() {
            return this.mOldDoubleNrRssnr;
        }

        public double getOldDoubleLteRsrp() {
            return this.mOldDoubleLteRsrp;
        }

        public double getOldDoubleLteRssnr() {
            return this.mOldDoubleLteRssnr;
        }

        public double getOldDoubleWcdmaRscp() {
            return this.mOldDoubleWcdmaRscp;
        }

        public double getOldDoubleWcdmaEcio() {
            return this.mOldDoubleWcdmaEcio;
        }

        public double getOldDoubleGsmSignalStrength() {
            return this.mOldDoubleGsmSS;
        }

        public boolean processNrRsrpAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldRsrp = oldDoubleSS.getDoubleNrRsrp();
            double modemNrRsrp = (double) modemSS.getNrRsrp();
            this.mOldDoubleNrRsrp = oldRsrp;
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]NR--old : " + oldRsrp + "; instant new : " + modemNrRsrp);
            if (modemNrRsrp >= -1.0d) {
                modemNrRsrp = (double) HwGsmServiceStateManager.NR_STRENGTH_POOR_STD;
            }
            if (oldRsrp <= modemNrRsrp) {
                this.mDoubleNrRsrp = modemNrRsrp;
            } else if (needProcessDescend) {
                this.mDoubleNrRsrp = ((7.0d * oldRsrp) + (5.0d * modemNrRsrp)) / 12.0d;
            } else {
                this.mDoubleNrRsrp = oldRsrp;
            }
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]NR modem : " + modemNrRsrp + "; old : " + oldRsrp + "; new : " + this.mDoubleNrRsrp);
            if (this.mDoubleNrRsrp - modemNrRsrp <= -1.0d || this.mDoubleNrRsrp - modemNrRsrp >= 1.0d) {
                newSS.setNrRsrp((int) this.mDoubleNrRsrp);
                return true;
            }
            this.mDoubleNrRsrp = modemNrRsrp;
            newSS.setNrRsrp((int) this.mDoubleNrRsrp);
            return false;
        }

        public boolean processNrRssnrAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldRssnr = oldDoubleSS.getDoubleNrRssnr();
            double modemNrRssnr = (double) modemSS.getNrRssnr();
            this.mOldDoubleNrRssnr = oldRssnr;
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]Before processNrRssnrAlaphFilter -- old : " + oldRssnr + "; instant new : " + modemNrRssnr);
            if (modemNrRssnr == 99.0d) {
                modemNrRssnr = (double) HwGsmServiceStateManager.NR_RSSNR_POOR_STD;
            }
            if (oldRssnr <= modemNrRssnr) {
                this.mDoubleNrRssnr = modemNrRssnr;
            } else if (needProcessDescend) {
                this.mDoubleNrRssnr = (VALUE_OLD_COEF_QUA_DES_SS * oldRssnr) + (VALUE_NEW_COEF_QUA_DES_SS * modemNrRssnr);
            } else {
                this.mDoubleNrRssnr = oldRssnr;
            }
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]NrRssnrAlaphFilter modem : " + modemNrRssnr + "; old : " + oldRssnr + "; new : " + this.mDoubleNrRssnr);
            if (this.mDoubleNrRssnr - modemNrRssnr <= -1.0d || this.mDoubleLteRssnr - modemNrRssnr >= 1.0d) {
                newSS.setNrRssnr((int) this.mDoubleNrRssnr);
                return true;
            }
            this.mDoubleNrRssnr = modemNrRssnr;
            newSS.setNrRssnr((int) this.mDoubleNrRssnr);
            return false;
        }

        public boolean processLteRsrpAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldRsrp = oldDoubleSS.getDoubleLteRsrp();
            double modemLteRsrp = (double) modemSS.getLteRsrp();
            this.mOldDoubleLteRsrp = oldRsrp;
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]LTE--old : " + oldRsrp + "; instant new : " + modemLteRsrp);
            if (modemLteRsrp >= -1.0d) {
                modemLteRsrp = (double) HwGsmServiceStateManager.LTE_STRENGTH_POOR_STD;
            }
            if (oldRsrp <= modemLteRsrp) {
                this.mDoubleLteRsrp = modemLteRsrp;
            } else if (needProcessDescend) {
                this.mDoubleLteRsrp = ((7.0d * oldRsrp) + (5.0d * modemLteRsrp)) / 12.0d;
            } else {
                this.mDoubleLteRsrp = oldRsrp;
            }
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]LTE modem : " + modemLteRsrp + "; old : " + oldRsrp + "; new : " + this.mDoubleLteRsrp);
            if (this.mDoubleLteRsrp - modemLteRsrp <= -1.0d || this.mDoubleLteRsrp - modemLteRsrp >= 1.0d) {
                newSS.setLteRsrp((int) this.mDoubleLteRsrp);
                return true;
            }
            this.mDoubleLteRsrp = modemLteRsrp;
            newSS.setLteRsrp((int) this.mDoubleLteRsrp);
            return false;
        }

        public boolean processLteRssnrAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldRssnr = oldDoubleSS.getDoubleLteRssnr();
            double modemLteRssnr = (double) modemSS.getLteRssnr();
            this.mOldDoubleLteRssnr = oldRssnr;
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]Before processLteRssnrAlaphFilter -- old : " + oldRssnr + "; instant new : " + modemLteRssnr);
            if (modemLteRssnr == 99.0d) {
                modemLteRssnr = (double) HwGsmServiceStateManager.LTE_RSSNR_POOR_STD;
            }
            if (oldRssnr <= modemLteRssnr) {
                this.mDoubleLteRssnr = modemLteRssnr;
            } else if (needProcessDescend) {
                this.mDoubleLteRssnr = (VALUE_OLD_COEF_QUA_DES_SS * oldRssnr) + (VALUE_NEW_COEF_QUA_DES_SS * modemLteRssnr);
            } else {
                this.mDoubleLteRssnr = oldRssnr;
            }
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]LteRssnrAlaphFilter modem : " + modemLteRssnr + "; old : " + oldRssnr + "; new : " + this.mDoubleLteRssnr);
            if (this.mDoubleLteRssnr - modemLteRssnr <= -1.0d || this.mDoubleLteRssnr - modemLteRssnr >= 1.0d) {
                newSS.setLteRssnr((int) this.mDoubleLteRssnr);
                return true;
            }
            this.mDoubleLteRssnr = modemLteRssnr;
            newSS.setLteRssnr((int) this.mDoubleLteRssnr);
            return false;
        }

        public boolean processWcdmaRscpAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldWcdmaRscp = oldDoubleSS.getDoubleWcdmaRscp();
            double modemWcdmaRscp = (double) modemSS.getWcdmaRscp();
            this.mOldDoubleWcdmaRscp = oldWcdmaRscp;
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]Before processWcdmaRscpAlaphFilter -- old : " + oldWcdmaRscp + "; instant new : " + modemWcdmaRscp);
            if (modemWcdmaRscp >= -1.0d) {
                modemWcdmaRscp = (double) HwGsmServiceStateManager.WCDMA_STRENGTH_POOR_STD;
            }
            if (oldWcdmaRscp <= modemWcdmaRscp) {
                this.mDoubleWcdmaRscp = modemWcdmaRscp;
            } else if (needProcessDescend) {
                this.mDoubleWcdmaRscp = ((7.0d * oldWcdmaRscp) + (5.0d * modemWcdmaRscp)) / 12.0d;
            } else {
                this.mDoubleWcdmaRscp = oldWcdmaRscp;
            }
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]WcdmaRscpAlaphFilter modem : " + modemWcdmaRscp + "; old : " + oldWcdmaRscp + "; new : " + this.mDoubleWcdmaRscp);
            if (this.mDoubleWcdmaRscp - modemWcdmaRscp <= -1.0d || this.mDoubleWcdmaRscp - modemWcdmaRscp >= 1.0d) {
                newSS.setWcdmaRscp((int) this.mDoubleWcdmaRscp);
                return true;
            }
            this.mDoubleWcdmaRscp = modemWcdmaRscp;
            newSS.setWcdmaRscp((int) this.mDoubleWcdmaRscp);
            return false;
        }

        public boolean processWcdmaEcioAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldWcdmaEcio = oldDoubleSS.getDoubleWcdmaEcio();
            double modemWcdmaEcio = (double) modemSS.getWcdmaEcio();
            this.mOldDoubleWcdmaEcio = oldWcdmaEcio;
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]Before processWcdmaEcioAlaphFilter -- old : " + oldWcdmaEcio + "; instant new : " + modemWcdmaEcio);
            if (oldWcdmaEcio <= modemWcdmaEcio) {
                this.mDoubleWcdmaEcio = modemWcdmaEcio;
            } else if (needProcessDescend) {
                this.mDoubleWcdmaEcio = (VALUE_OLD_COEF_QUA_DES_SS * oldWcdmaEcio) + (VALUE_NEW_COEF_QUA_DES_SS * modemWcdmaEcio);
            } else {
                this.mDoubleWcdmaEcio = oldWcdmaEcio;
            }
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]WcdmaEcioAlaphFilter modem : " + modemWcdmaEcio + "; old : " + oldWcdmaEcio + "; new : " + this.mDoubleWcdmaEcio);
            if (this.mDoubleWcdmaEcio - modemWcdmaEcio <= -1.0d || this.mDoubleWcdmaEcio - modemWcdmaEcio >= 1.0d) {
                newSS.setWcdmaEcio((int) this.mDoubleWcdmaEcio);
                return true;
            }
            this.mDoubleWcdmaEcio = modemWcdmaEcio;
            newSS.setWcdmaEcio((int) this.mDoubleWcdmaEcio);
            return false;
        }

        public boolean processGsmSignalStrengthAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldGsmSS = oldDoubleSS.getDoubleGsmSignalStrength();
            double modemGsmSS = (double) modemSS.getGsmSignalStrength();
            this.mOldDoubleGsmSS = oldGsmSS;
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]Before>>old : " + oldGsmSS + "; instant new : " + modemGsmSS);
            if (modemGsmSS >= -1.0d) {
                modemGsmSS = -109.0d;
            }
            if (oldGsmSS <= modemGsmSS) {
                this.mDoubleGsmSS = modemGsmSS;
            } else if (needProcessDescend) {
                this.mDoubleGsmSS = ((7.0d * oldGsmSS) + (5.0d * modemGsmSS)) / 12.0d;
            } else {
                this.mDoubleGsmSS = oldGsmSS;
            }
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]GsmSS AlaphFilter modem : " + modemGsmSS + "; old : " + oldGsmSS + "; new : " + this.mDoubleGsmSS);
            if (this.mDoubleGsmSS - modemGsmSS <= -1.0d || this.mDoubleGsmSS - modemGsmSS >= 1.0d) {
                newSS.setGsmSignalStrength((int) this.mDoubleGsmSS);
                return true;
            }
            this.mDoubleGsmSS = modemGsmSS;
            newSS.setGsmSignalStrength((int) this.mDoubleGsmSS);
            return false;
        }

        public void proccessAlaphFilter(SignalStrength newSS, SignalStrength modemSS) {
            proccessAlaphFilter(this, newSS, modemSS, true);
        }

        public void proccessAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            boolean needUpdate = false;
            if (oldDoubleSS == null) {
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]proccess oldDoubleSS is null");
                return;
            }
            if ((this.mTechState & 32) != 0) {
                needUpdate = false | processNrRsrpAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                    needUpdate |= processNrRssnrAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                }
            }
            if ((this.mTechState & 4) != 0) {
                needUpdate |= processLteRsrpAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                    needUpdate |= processLteRssnrAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                }
            }
            if ((this.mTechState & 2) != 0) {
                needUpdate |= processWcdmaRscpAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                    needUpdate |= processWcdmaEcioAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                }
            }
            boolean isGsmPhone = true;
            if ((this.mTechState & 1) != 0) {
                needUpdate |= processGsmSignalStrengthAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
            }
            if (TelephonyManager.getDefault().getCurrentPhoneType(HwGsmServiceStateManager.this.mPhoneId) != 1) {
                isGsmPhone = false;
            }
            if (!isGsmPhone) {
                if (HwGsmServiceStateManager.this.hasMessages(HwGsmServiceStateManager.EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH)) {
                    HwGsmServiceStateManager.this.removeMessages(HwGsmServiceStateManager.EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH);
                }
                return;
            }
            setFakeSignalStrengthForSlowDescend(this, newSS);
            HwGsmServiceStateManager.this.mGsst.setSignalStrength(newSS);
            if (needUpdate) {
                HwGsmServiceStateManager.this.sendMessageDelayUpdateSingalStrength(this.mDelayTime);
            }
        }

        private void setFakeSignalStrengthForSlowDescend(DoubleSignalStrength oldDoubleSS, SignalStrength newSS) {
            this.mDelayTime = 0;
            if (HwGsmServiceStateManager.this.mHwSigStr == null) {
                Rlog.e(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "] mHwSigStr is null");
                return;
            }
            if ((this.mTechState & 32) != 0) {
                int oldLevel = HwGsmServiceStateManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.NR, (int) oldDoubleSS.getOldDoubleNrRsrp(), (int) oldDoubleSS.getOldDoubleNrRssnr());
                int diffLevel = oldLevel - HwGsmServiceStateManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.NR, newSS.getNrRsrp(), newSS.getNrRssnr());
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]NR oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1) {
                    HwSignalStrength.SignalThreshold signalThreshold = HwGsmServiceStateManager.this.mHwSigStr.getSignalThreshold(HwSignalStrength.SignalType.NR);
                    if (signalThreshold != null) {
                        int lowerLevel = oldLevel - 1;
                        int nrRsrp = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                        if (-1 != nrRsrp) {
                            this.mDoubleNrRsrp = (double) nrRsrp;
                            newSS.setNrRsrp(nrRsrp);
                            this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                        }
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]NR lowerLevel: " + lowerLevel + ", nrRsrp: " + nrRsrp);
                        if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                            int nrRssnr = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true);
                            if (-1 != nrRssnr) {
                                this.mDoubleNrRssnr = (double) nrRssnr;
                                newSS.setNrRssnr(nrRssnr);
                                this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                            }
                        }
                    }
                }
            }
            if ((this.mTechState & 4) != 0) {
                int oldLevel2 = HwGsmServiceStateManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.LTE, (int) oldDoubleSS.getOldDoubleLteRsrp(), (int) oldDoubleSS.getOldDoubleLteRssnr());
                int diffLevel2 = oldLevel2 - HwGsmServiceStateManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.LTE, newSS.getLteRsrp(), newSS.getLteRssnr());
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]LTE oldLevel: " + oldLevel2 + ", newLevel: " + newLevel);
                if (diffLevel2 > 1) {
                    HwSignalStrength.SignalThreshold signalThreshold2 = HwGsmServiceStateManager.this.mHwSigStr.getSignalThreshold(HwSignalStrength.SignalType.LTE);
                    if (signalThreshold2 != null) {
                        int lowerLevel2 = oldLevel2 - 1;
                        int lteRsrp = signalThreshold2.getHighThresholdBySignalLevel(lowerLevel2, false);
                        if (-1 != lteRsrp) {
                            this.mDoubleLteRsrp = (double) lteRsrp;
                            newSS.setLteRsrp(lteRsrp);
                            this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel2;
                        }
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]LTE lowerLevel: " + lowerLevel2 + ", lteRsrp: " + lteRsrp);
                        if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                            int lteRssnr = signalThreshold2.getHighThresholdBySignalLevel(lowerLevel2, true);
                            if (-1 != lteRssnr) {
                                this.mDoubleLteRssnr = (double) lteRssnr;
                                newSS.setLteRssnr(lteRssnr);
                                this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel2;
                            }
                        }
                    }
                }
            }
            if ((this.mTechState & 2) != 0) {
                int oldLevel3 = HwGsmServiceStateManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.UMTS, (int) oldDoubleSS.getOldDoubleWcdmaRscp(), (int) oldDoubleSS.getOldDoubleWcdmaEcio());
                int diffLevel3 = oldLevel3 - HwGsmServiceStateManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.UMTS, newSS.getWcdmaRscp(), newSS.getWcdmaEcio());
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]UMTS oldLevel: " + oldLevel3 + ", newLevel: " + newLevel);
                if (diffLevel3 > 1) {
                    HwSignalStrength.SignalThreshold signalThreshold3 = HwGsmServiceStateManager.this.mHwSigStr.getSignalThreshold(HwSignalStrength.SignalType.UMTS);
                    if (signalThreshold3 != null) {
                        int lowerLevel3 = oldLevel3 - 1;
                        int wcdmaRscp = signalThreshold3.getHighThresholdBySignalLevel(lowerLevel3, false);
                        if (-1 != wcdmaRscp) {
                            this.mDoubleWcdmaRscp = (double) wcdmaRscp;
                            newSS.setWcdmaRscp(wcdmaRscp);
                            this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel3;
                        }
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]UMTS lowerLevel: " + lowerLevel3 + ", wcdmaRscp: " + wcdmaRscp);
                        if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                            int wcdmaEcio = signalThreshold3.getHighThresholdBySignalLevel(lowerLevel3, true);
                            if (-1 != wcdmaEcio) {
                                this.mDoubleWcdmaEcio = (double) wcdmaEcio;
                                newSS.setWcdmaEcio(wcdmaEcio);
                                this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel3;
                            }
                        }
                    }
                }
            }
            if ((this.mTechState & 1) != 0) {
                int oldLevel4 = HwGsmServiceStateManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.GSM, (int) oldDoubleSS.getOldDoubleGsmSignalStrength(), 255);
                int diffLevel4 = oldLevel4 - HwGsmServiceStateManager.this.mHwSigStr.getLevel(HwSignalStrength.SignalType.GSM, newSS.getGsmSignalStrength(), 255);
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]GSM oldLevel: " + oldLevel4 + ", newLevel: " + newLevel);
                if (diffLevel4 > 1) {
                    HwSignalStrength.SignalThreshold signalThreshold4 = HwGsmServiceStateManager.this.mHwSigStr.getSignalThreshold(HwSignalStrength.SignalType.GSM);
                    if (signalThreshold4 != null) {
                        int gsmSS = signalThreshold4.getHighThresholdBySignalLevel(oldLevel4 - 1, false);
                        if (-1 != gsmSS) {
                            this.mDoubleGsmSS = (double) gsmSS;
                            newSS.setGsmSignalStrength(gsmSS);
                            this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel4;
                        }
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]GSM lowerLevel: " + lowerLevel + ", gsmSS: " + gsmSS);
                    }
                }
            }
        }
    }

    static {
        boolean z = false;
        if ("389".equals(SystemProperties.get("ro.config.hw_opta")) && "840".equals(SystemProperties.get("ro.config.hw_optb"))) {
            z = true;
        }
        IS_VERIZON = z;
    }

    public HwGsmServiceStateManager(ServiceStateTracker sst, GsmCdmaPhone gsmPhone) {
        super(sst, gsmPhone);
        this.mGsst = sst;
        this.mGsmPhone = gsmPhone;
        this.mContext = gsmPhone.getContext();
        this.mCr = this.mContext.getContentResolver();
        this.mPhoneId = gsmPhone.getPhoneId();
        this.mHwSigStr = HwSignalStrength.getInstance(this.mGsmPhone);
        this.mCi = gsmPhone.mCi;
        this.mCi.registerForRplmnsStateChanged(this, EVENT_RPLMNS_STATE_CHANGED, null);
        sendMessage(obtainMessage(EVENT_RPLMNS_STATE_CHANGED));
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]constructor init");
        this.mHwCustGsmServiceStateManager = (HwCustGsmServiceStateManager) HwCustUtils.createObj(HwCustGsmServiceStateManager.class, new Object[]{sst, gsmPhone});
        addBroadCastReceiver();
        this.mMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        this.mMainSlotEcc = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        this.mCi.registerForCaStateChanged(this, EVENT_CA_STATE_CHANGED, null);
        this.mCi.registerForCrrConn(this, EVENT_CRR_CONN, null);
        this.mCi.setOnRegPLMNSelInfo(this, EVENT_PLMN_SELINFO, null);
        this.mGsmPhone.registerForMccChanged(this, EVENT_MCC_CHANGED, null);
        this.mCi.registerForDSDSMode(this, EVENT_DSDS_MODE, null);
        this.mCi.registerForUnsol4RMimoStatus(this, EVENT_4R_MIMO_ENABLE, null);
        registerCloudOtaBroadcastReceiver();
        this.mCi.setOnNetReject(this, EVENT_NETWORK_REJECTED_CASE, null);
    }

    private ServiceState getNewSS() {
        return ServiceStateTrackerUtils.getNewSS(this.mGsst);
    }

    private ServiceState getSS() {
        return this.mGsst.mSS;
    }

    public String getRplmn() {
        return this.rplmn;
    }

    public boolean getRoamingStateHw(boolean roaming) {
        if (this.mHwCustGsmServiceStateManager != null) {
            this.mHwCustGsmServiceStateManager.storeModemRoamingStatus(roaming);
        }
        boolean isCTCardRegGSM = true;
        if (roaming) {
            String hplmn = null;
            if (this.mGsmPhone.mIccRecords.get() != null) {
                hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            }
            String regplmn = getNewSS().getOperatorNumeric();
            String regplmnCustomString = null;
            if (getNoRoamingByMcc(getNewSS())) {
                roaming = false;
            }
            try {
                regplmnCustomString = Settings.System.getString(this.mContext.getContentResolver(), "reg_plmn_custom");
                Rlog.d(LOG_TAG, "handlePollStateResult plmnCustomString = " + regplmnCustomString);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception when got name value");
            }
            if (regplmnCustomString != null) {
                String[] regplmnCustomArray = regplmnCustomString.split(";");
                if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(regplmn)) {
                    int regplmnCustomArrayLen = regplmnCustomArray.length;
                    int i = 0;
                    while (true) {
                        if (i >= regplmnCustomArrayLen) {
                            break;
                        }
                        String[] regplmnCustomArrEleBuf = regplmnCustomArray[i].split(",");
                        if (containsPlmn(hplmn, regplmnCustomArrEleBuf) && containsPlmn(regplmn, regplmnCustomArrEleBuf)) {
                            roaming = false;
                            break;
                        }
                        i++;
                    }
                } else {
                    roaming = false;
                }
            }
        }
        Rlog.d(LOG_TAG, "roaming = " + roaming);
        if (!HwTelephonyManagerInner.getDefault().isCTSimCard(this.mGsmPhone.getPhoneId()) || getNewSS().getState() != 0 || !ServiceState.isGsm(getNewSS().getRilVoiceRadioTechnology()) || ServiceState.isLte(getNewSS().getRilVoiceRadioTechnology())) {
            isCTCardRegGSM = false;
        }
        if (isCTCardRegGSM) {
            roaming = true;
            Rlog.d(LOG_TAG, "When CT card register in GSM/UMTS, it always should be roaming" + true);
        }
        if (this.mHwCustGsmServiceStateManager != null) {
            roaming = this.mHwCustGsmServiceStateManager.setRoamingStateForOperatorCustomization(getNewSS(), roaming);
            Rlog.d(LOG_TAG, "roaming customization for MCC 302 roaming=" + roaming);
        }
        boolean roaming2 = getGsmRoamingSpecialCustByNetType(getGsmRoamingCustByIMSIStart(roaming));
        if (this.mHwCustGsmServiceStateManager != null) {
            return this.mHwCustGsmServiceStateManager.checkIsInternationalRoaming(roaming2, getNewSS());
        }
        return roaming2;
    }

    private boolean getGsmRoamingSpecialCustByNetType(boolean roaming) {
        if (this.mGsmPhone.mIccRecords.get() != null) {
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            String regplmn = getNewSS().getOperatorNumeric();
            int netType = getNewSS().getVoiceNetworkType();
            TelephonyManager.getDefault();
            int netClass = TelephonyManager.getNetworkClass(netType);
            Rlog.d(LOG_TAG, "getGsmRoamingSpecialCustByNetType: hplmn=" + hplmn + " regplmn=" + regplmn + " netType=" + netType + " netClass=" + netClass);
            if ("50218".equals(hplmn) && "50212".equals(regplmn)) {
                if (1 == netClass || 2 == netClass) {
                    roaming = false;
                }
                if (3 == netClass) {
                    roaming = true;
                }
            }
        }
        Rlog.d(LOG_TAG, "getGsmRoamingSpecialCustByNetType: roaming = " + roaming);
        return roaming;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0174  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x017b  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x01a0  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x01d1  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01e2  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0205  */
    public String getPlmn() {
        String data;
        int slotId;
        String imsi;
        String hplmn;
        ApnReminder apnReminder;
        String plmnValue;
        if (getCombinedRegState(getSS()) != 0) {
            return null;
        }
        String operatorNumeric = getSS().getOperatorNumeric();
        try {
            data = Settings.System.getString(this.mCr, "plmn");
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception when got data value", e);
            data = null;
        }
        PlmnConstants plmnConstants = new PlmnConstants(data);
        String plmnValue2 = plmnConstants.getPlmnValue(operatorNumeric, Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
        Rlog.d(LOG_TAG, "getPlmn languageCode:" + languageCode + "  plmnValue:" + plmnValue2);
        if (plmnValue2 == null) {
            plmnValue2 = plmnConstants.getPlmnValue(operatorNumeric, "en_us");
            Rlog.d(LOG_TAG, "get default en_us plmn name:" + plmnValue2);
        }
        int slotId2 = this.mGsmPhone.getPhoneId();
        Rlog.d(LOG_TAG, "slotId = " + slotId2);
        String hplmn2 = null;
        String imsi2 = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn2 = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            imsi2 = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getIMSI();
        }
        String hplmn3 = hplmn2;
        String imsi3 = imsi2;
        Rlog.d(LOG_TAG, "hplmn = " + hplmn3);
        boolean hasNitzOperatorName = false;
        boolean isUseVirtualName = this.mHwCustGsmServiceStateManager != null && this.mHwCustGsmServiceStateManager.notUseVirtualName(imsi3);
        boolean roaming = getSS().getRoaming();
        boolean isMatchRoamingRule = this.mHwCustGsmServiceStateManager != null && this.mHwCustGsmServiceStateManager.iscustRoamingRuleAffect(roaming);
        if (isUseVirtualName) {
            imsi = imsi3;
            hplmn = hplmn3;
            slotId = slotId2;
        } else if (isMatchRoamingRule) {
            boolean z = roaming;
            imsi = imsi3;
            hplmn = hplmn3;
            slotId = slotId2;
        } else {
            slotId = slotId2;
            imsi = imsi3;
            hplmn = hplmn3;
            plmnValue2 = getVirCarrierOperatorName(HwTelephonyFactory.getHwPhoneManager().getVirtualNetOperatorName(plmnValue2, roaming, hasNitzOperatorName(slotId2), slotId, hplmn3), roaming, hasNitzOperatorName(slotId2), slotId2, hplmn);
            Rlog.d(LOG_TAG, "VirtualNetName = " + plmnValue2);
            if (!IS_MULTI_SIM_ENABLED) {
                apnReminder = ApnReminder.getInstance(this.mContext, slotId);
            } else {
                apnReminder = ApnReminder.getInstance(this.mContext);
            }
            if (!apnReminder.isPopupApnSettingsEmpty() && !getSS().getRoaming() && !hasNitzOperatorName(slotId) && hplmn != null) {
                hasNitzOperatorName = true;
            }
            if (hasNitzOperatorName) {
                int apnId = getPreferedApnId();
                if (-1 != apnId) {
                    plmnValue2 = apnReminder.getOnsNameByPreferedApn(apnId, plmnValue2);
                    Rlog.d(LOG_TAG, "apnReminder plmnValue = " + plmnValue2);
                } else {
                    plmnValue2 = null;
                }
            }
            plmnValue = getGsmOnsDisplayPlmnByAbbrevPriority(getGsmOnsDisplayPlmnByPriority(plmnValue2, slotId), slotId);
            if (!TextUtils.isEmpty(plmnValue)) {
                plmnValue = getVirtualNetPlmnValue(operatorNumeric, hplmn, imsi, getEons(getSS().getOperatorAlphaLong()));
            } else {
                getSS().setOperatorAlphaLong(plmnValue);
            }
            Rlog.d(LOG_TAG, "plmnValue = " + plmnValue);
            if (HwPlmnActConcat.needPlmnActConcat()) {
                plmnValue = HwPlmnActConcat.getPlmnActConcat(plmnValue, getSS());
            }
            return plmnValue;
        }
        Rlog.d(LOG_TAG, "passed the Virtualnet cust");
        if (!IS_MULTI_SIM_ENABLED) {
        }
        hasNitzOperatorName = true;
        if (hasNitzOperatorName) {
        }
        plmnValue = getGsmOnsDisplayPlmnByAbbrevPriority(getGsmOnsDisplayPlmnByPriority(plmnValue2, slotId), slotId);
        if (!TextUtils.isEmpty(plmnValue)) {
        }
        Rlog.d(LOG_TAG, "plmnValue = " + plmnValue);
        if (HwPlmnActConcat.needPlmnActConcat()) {
        }
        return plmnValue;
    }

    public String getVirCarrierOperatorName(String plmnValue, boolean roaming, boolean hasNitzOperatorName, int slotId, String hplmn) {
        if (roaming || hplmn == null || hasNitzOperatorName) {
            return plmnValue;
        }
        String custplmn = (String) HwCfgFilePolicy.getValue("virtualnet_operatorname", slotId, String.class);
        if (TextUtils.isEmpty(custplmn)) {
            return plmnValue;
        }
        String plmnValue2 = custplmn;
        Rlog.d(LOG_TAG, "getVirCarrierOperatorName: plmnValue = " + plmnValue2 + " custplmn = " + custplmn);
        return plmnValue2;
    }

    public String getGsmOnsDisplayPlmnByAbbrevPriority(String custPlmnValue, int slotId) {
        String result;
        String str = custPlmnValue;
        String plmnAbbrev = null;
        String custPlmn = Settings.System.getString(this.mCr, "hw_plmn_abbrev");
        if (this.mGsmPhone.mIccRecords.get() != null) {
            CustPlmnMember cpm = CustPlmnMember.getInstance();
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            if (cpm.acquireFromCust(hplmn, getSS(), custPlmn)) {
                plmnAbbrev = cpm.plmn;
                Rlog.d(LOG_TAG, " plmn2 =" + plmnAbbrev);
            }
            if (cpm.getCfgCustDisplayParams(hplmn, getSS(), "plmn_abbrev", slotId)) {
                plmnAbbrev = cpm.plmn;
                Rlog.d(LOG_TAG, "HwCfgFile: plmn =" + plmnAbbrev);
            }
        }
        if (TextUtils.isEmpty(plmnAbbrev)) {
            return custPlmnValue;
        }
        if (hasNitzOperatorName(slotId)) {
            result = getEons(getSS().getOperatorAlphaLong());
        } else {
            result = getEons(plmnAbbrev);
        }
        Rlog.d(LOG_TAG, "result = " + result + " slotId = " + slotId + " PlmnValue = " + custPlmnValue);
        return result;
    }

    public boolean getCarrierConfigPri(int slotId) {
        try {
            HwCfgKey keyCollection = new HwCfgKey("net_sim_ue_pri", "network_mccmnc", null, null, "network_highest", getSS().getOperatorNumeric(), null, null, slotId);
            Boolean carrerPriority = (Boolean) HwGetCfgFileConfig.getCfgFileData(keyCollection, Boolean.class);
            if (carrerPriority != null) {
                return carrerPriority.booleanValue();
            }
            return false;
        } catch (Exception e) {
            log("Exception: read net_sim_ue_pri error " + e.toString());
            return false;
        }
    }

    public String getGsmOnsDisplayPlmnByPriority(String custPlmnValue, int slotId) {
        String result;
        boolean hwNetworkSimUePriority = SystemProperties.getBoolean("ro.config.net_sim_ue_pri", false);
        boolean configNetworkSimUePriority = getCarrierConfigPri(slotId);
        boolean custHplmnRegplmn = enablePlmnByNetSimUePriority();
        if (!hwNetworkSimUePriority && !custHplmnRegplmn && !configNetworkSimUePriority) {
            return custPlmnValue;
        }
        String result2 = custPlmnValue;
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        String spnSim = null;
        if (r != null) {
            spnSim = r.getServiceProviderName();
        }
        if (hasNitzOperatorName(slotId)) {
            String result3 = getEons(result2);
            result = getSS().getOperatorAlphaLong();
        } else {
            String result4 = getSS().getOperatorAlphaLong();
            if (custPlmnValue != null) {
                result4 = custPlmnValue;
            }
            result = getEons(result4);
            if ((custHplmnRegplmn || configNetworkSimUePriority) && !TextUtils.isEmpty(spnSim)) {
                result = spnSim;
            }
        }
        Rlog.d(LOG_TAG, "plmnValue = " + result + " slotId = " + slotId + " custPlmnValue = " + custPlmnValue);
        return result;
    }

    public boolean enablePlmnByNetSimUePriority() {
        String hplmn = null;
        String regplmn = null;
        boolean custHplmnRegplmn = false;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        String custNetSimUePriority = Settings.System.getString(this.mCr, "hw_net_sim_ue_pri");
        if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(regplmn) && !TextUtils.isEmpty(custNetSimUePriority)) {
            String[] custmccmncs = custNetSimUePriority.split(";");
            int length = custmccmncs.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String[] mccmncs = custmccmncs[i].split(",");
                if (hplmn.equals(mccmncs[0]) && regplmn.equals(mccmncs[1])) {
                    custHplmnRegplmn = true;
                    break;
                }
                i++;
            }
        } else {
            custHplmnRegplmn = false;
            Rlog.d(LOG_TAG, "enablePlmnByNetSimUePriority() failed, priority or hplmm or regplmn is empty");
        }
        Rlog.d(LOG_TAG, " cust_hplmn_equal_regplmn = " + custHplmnRegplmn);
        return custHplmnRegplmn;
    }

    private String getVirtualNetPlmnValue(String operatorNumeric, String hplmn, String imsi, String plmnValue) {
        if (hasNitzOperatorName(this.mGsmPhone.getPhoneId())) {
            return plmnValue;
        }
        if ("22299".equals(operatorNumeric)) {
            if (hplmn == null) {
                return " ";
            }
            if (imsi != null && imsi.startsWith("222998")) {
                return " ";
            }
        }
        if ("22201".equals(operatorNumeric)) {
            if (hplmn == null) {
                return " ";
            }
            IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
            String spnSim = null;
            if (r != null) {
                spnSim = r.getServiceProviderName();
            }
            if ("Coop Mobile".equals(spnSim) && imsi != null && imsi.startsWith("22201")) {
                return " ";
            }
        }
        return plmnValue;
    }

    private int getPreferedApnId() {
        Cursor cursor;
        int apnId = -1;
        if (IS_MULTI_SIM_ENABLED) {
            cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mGsmPhone.getPhoneId()), new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        } else {
            cursor = this.mContext.getContentResolver().query(PREFERAPN_NO_UPDATE_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        }
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String apnName = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            String carrierName = cursor.getString(cursor.getColumnIndexOrThrow(HwTelephony.NumMatchs.NAME));
            Rlog.d(LOG_TAG, "getPreferedApnId: " + apnId + ", apn: " + apnName + ", name: " + carrierName);
        }
        if (cursor != null) {
            cursor.close();
        }
        return apnId;
    }

    private boolean isChinaMobileMccMnc() {
        String hplmn = null;
        String regplmn = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        String[] mccMncList = CHINAMOBILE_MCCMNC.split(";");
        boolean z = false;
        if (TextUtils.isEmpty(regplmn) || TextUtils.isEmpty(hplmn)) {
            return false;
        }
        boolean isHplmnCMCC = false;
        boolean isRegplmnCMCC = false;
        for (int i = 0; i < mccMncList.length; i++) {
            if (mccMncList[i].equals(regplmn)) {
                isRegplmnCMCC = true;
            }
            if (mccMncList[i].equals(hplmn)) {
                isHplmnCMCC = true;
            }
        }
        if (isRegplmnCMCC && isHplmnCMCC) {
            z = true;
        }
        return z;
    }

    public OnsDisplayParams getOnsDisplayParamsHw(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        String tempPlmn = plmn;
        OnsDisplayParams odp = getOnsDisplayParamsBySpnOnly(showSpn, showPlmn, rule, plmn, spn);
        OnsDisplayParams onsDisplayParams = new OnsDisplayParams(odp.mShowSpn, odp.mShowPlmn, odp.mRule, odp.mPlmn, odp.mSpn);
        OnsDisplayParams odpTemp = onsDisplayParams;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            CustPlmnMember cpm = CustPlmnMember.getInstance();
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumericEx(this.mCr, "hw_ons_hplmn_ex");
            int subid = this.mGsmPhone.getSubId();
            int slotId = SubscriptionManager.getSlotIndex(subid);
            String regplmn = getSS().getOperatorNumeric();
            String custSpn = Settings.System.getString(this.mCr, "hw_plmn_spn");
            if (custSpn != null) {
                Rlog.d(LOG_TAG, "custSpn length =" + custSpn.length());
            }
            getOnsByCpm(cpm, hplmn, custSpn, tempPlmn, regplmn, odpTemp);
            CustPlmnMember custPlmnMember = cpm;
            String str = hplmn;
            String str2 = tempPlmn;
            String str3 = regplmn;
            String str4 = custSpn;
            getOnsInVirtualNet(custPlmnMember, str, str2, str3, odpTemp);
            int i = subid;
            getOnsByCfgCust(custPlmnMember, str, str2, str3, slotId, odpTemp);
        }
        OnsDisplayParams odp2 = getOdpByCust(odp, odpTemp);
        setShowWifiByOdp(odp2);
        setSpnAndRuleByOdp(odp2);
        setOperatorNameByPlmnOrSpn(odp2);
        return odp2;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsBySpnCust(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        String plmnRslt = plmn;
        String spnRslt = spn;
        boolean showPlmnRslt = showPlmn;
        boolean showSpnRslt = showSpn;
        int ruleRslt = rule;
        boolean matched = true;
        String regPlmn = getSS().getOperatorNumeric();
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        if (r != null) {
            String hplmn = r.getOperatorNumeric();
            String spnSim = r.getServiceProviderName();
            if (!"732130".equals(hplmn) || TextUtils.isEmpty(spnSim) || (!"732103".equals(regPlmn) && !"732111".equals(regPlmn) && !"732123".equals(regPlmn) && !"732101".equals(regPlmn))) {
                matched = false;
            } else {
                showSpnRslt = true;
                showPlmnRslt = false;
                ruleRslt = 1;
                spnRslt = spnSim;
            }
        } else {
            matched = false;
        }
        String spnRslt2 = spnRslt;
        boolean showPlmnRslt2 = showPlmnRslt;
        boolean showSpnRslt2 = showSpnRslt;
        int ruleRslt2 = ruleRslt;
        if (!matched) {
            return null;
        }
        OnsDisplayParams odpRslt = new OnsDisplayParams(showSpnRslt2, showPlmnRslt2, ruleRslt2, plmnRslt, spnRslt2);
        return odpRslt;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsBySpecialCust(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        OnsDisplayParams odpRslt;
        boolean showPlmnRslt;
        boolean showPlmnRslt2;
        String plmnRslt;
        boolean showPlmnRslt3;
        boolean showSpnRslt;
        String str = plmn;
        String plmnRslt2 = str;
        String spnRslt = spn;
        boolean showPlmnRslt4 = showPlmn;
        boolean showSpnRslt2 = showSpn;
        int ruleRslt = rule;
        boolean matched = true;
        String regPlmn = getSS().getOperatorNumeric();
        int netType = getSS().getVoiceNetworkType();
        TelephonyManager.getDefault();
        int netClass = TelephonyManager.getNetworkClass(netType);
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        if (r != null) {
            String hplmn = r.getOperatorNumeric();
            String spnSim = r.getServiceProviderName();
            int ruleSim = r.getDisplayRule(getSS());
            showPlmnRslt = showPlmnRslt4;
            boolean showSpnRslt3 = showSpnRslt2;
            StringBuilder sb = new StringBuilder();
            odpRslt = null;
            sb.append("regPlmn = ");
            sb.append(regPlmn);
            sb.append(",hplmn = ");
            sb.append(hplmn);
            sb.append(",spnSim = ");
            sb.append(spnSim);
            sb.append(",ruleSim = ");
            sb.append(ruleSim);
            sb.append(",netType = ");
            sb.append(netType);
            sb.append(",netClass = ");
            sb.append(netClass);
            Rlog.d(LOG_TAG, sb.toString());
            if ("21405".equals(hplmn) && "21407".equals(regPlmn) && "tuenti".equalsIgnoreCase(spnSim)) {
                String pnnName = getEons(str);
                if (!TextUtils.isEmpty(spnSim)) {
                    spnRslt = spnSim;
                    showSpnRslt3 = (ruleSim & 1) == 1;
                }
                if (!TextUtils.isEmpty(pnnName)) {
                    plmnRslt2 = pnnName;
                    showPlmnRslt = (ruleSim & 2) == 2;
                }
            } else if ("21407".equals(hplmn) && "21407".equals(regPlmn)) {
                String pnnName2 = getEons(str);
                if (!TextUtils.isEmpty(spnSim)) {
                    spnRslt = spnSim;
                    showSpnRslt3 = (ruleRslt & 1) == 1;
                }
                if (!TextUtils.isEmpty(pnnName2)) {
                    plmnRslt2 = pnnName2;
                    showPlmnRslt = (ruleRslt & 2) == 2;
                }
            } else if ("23420".equals(hplmn) && getCombinedRegState(getSS()) == 0) {
                String pnnName3 = getEons(str);
                if (!TextUtils.isEmpty(pnnName3)) {
                    spnRslt = spnSim;
                    plmnRslt2 = pnnName3;
                    ruleRslt = 2;
                    showSpnRslt3 = false;
                    showPlmnRslt = true;
                }
            } else if (("74000".equals(hplmn) && "74000".equals(regPlmn) && plmn.equals(spn)) || ("45006".equals(hplmn) && "45006".equals(regPlmn) && "LG U+".equals(str))) {
                showSpnRslt2 = false;
                ruleRslt = 2;
                showPlmnRslt = true;
            } else if (!"732187".equals(hplmn) || (!"732103".equals(regPlmn) && !"732111".equals(regPlmn))) {
                if ("50218".equals(hplmn) && "50212".equals(regPlmn)) {
                    if (1 == netClass || 2 == netClass) {
                        showSpnRslt = true;
                        showPlmnRslt3 = false;
                        ruleRslt = 1;
                        plmnRslt2 = "U Mobile";
                        spnRslt = "U Mobile";
                    } else if (3 == netClass) {
                        showSpnRslt = true;
                        showPlmnRslt3 = false;
                        ruleRslt = 1;
                        plmnRslt2 = "MY MAXIS";
                        spnRslt = "MY MAXIS";
                    }
                    showPlmnRslt = showPlmnRslt3;
                    showSpnRslt2 = showSpnRslt;
                } else if ("334050".equals(hplmn) || "334090".equals(hplmn) || "33405".equals(hplmn)) {
                    if (TextUtils.isEmpty(spnSim) && (("334050".equals(regPlmn) || "334090".equals(regPlmn)) && !TextUtils.isEmpty(plmnRslt2) && (plmnRslt2.startsWith("Iusacell") || plmnRslt2.startsWith("Nextel")))) {
                        Rlog.d(LOG_TAG, "AT&T some card has no pnn and spn, then want it to be treated as AT&T");
                        plmnRslt2 = "AT&T";
                    }
                    if (!TextUtils.isEmpty(plmnRslt2) && plmnRslt2.startsWith("AT&T")) {
                        if (1 == netClass) {
                            plmnRslt = "AT&T EDGE";
                        } else if (2 == netClass) {
                            plmnRslt = "AT&T";
                        } else if (3 == netClass) {
                            plmnRslt = "AT&T 4G";
                        }
                        plmnRslt2 = plmnRslt;
                    }
                } else if (ServiceStateTrackerUtils.isDocomoTablet()) {
                    if (TextUtils.isEmpty(spnRslt)) {
                        spnRslt = spnSim;
                    }
                    showSpnRslt2 = getCombinedRegState(getSS()) == 0 && !TextUtils.isEmpty(spnRslt) && (rule & 1) == 1;
                    Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsBySpecialCust: spn = " + spnRslt + ", showSpn = " + showSpnRslt2);
                } else {
                    matched = false;
                }
            } else if (1 == netClass || 2 == netClass) {
                plmnRslt2 = "ETB";
            } else if (3 == netClass) {
                plmnRslt2 = "ETB 4G";
            }
            showSpnRslt2 = showSpnRslt3;
        } else {
            showPlmnRslt = showPlmnRslt4;
            boolean z = showSpnRslt2;
            odpRslt = null;
            matched = false;
        }
        Rlog.d(LOG_TAG, "matched = " + matched + ",showPlmnRslt = " + showPlmnRslt2 + ",showSpnRslt = " + showSpnRslt2 + ",ruleRslt = " + ruleRslt + ",plmnRslt = " + plmnRslt2 + ",spnRslt = " + spnRslt);
        if (!matched) {
            return odpRslt;
        }
        OnsDisplayParams odpRslt2 = new OnsDisplayParams(showSpnRslt2, showPlmnRslt2, ruleRslt, plmnRslt2, spnRslt);
        return odpRslt2;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsForChinaOperator(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        String str = plmn;
        String spn2 = spn;
        String hplmn = null;
        String regplmn = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        String hplmn2 = hplmn;
        String regplmn2 = regplmn;
        StringBuilder sb = new StringBuilder();
        sb.append("showSpn:");
        boolean z = showSpn;
        sb.append(z);
        sb.append(",showPlmn:");
        boolean z2 = showPlmn;
        sb.append(z2);
        sb.append(",rule:");
        int i = rule;
        sb.append(i);
        sb.append(",plmn:");
        sb.append(str);
        sb.append(",spn:");
        sb.append(spn2);
        sb.append(",hplmn:");
        sb.append(hplmn2);
        sb.append(",regplmn:");
        sb.append(regplmn2);
        Rlog.d(LOG_TAG, sb.toString());
        if (!TextUtils.isEmpty(regplmn2)) {
            if (!isChinaMobileMccMnc()) {
                if (HwTelephonyFactory.getHwUiccManager().isCDMASimCard(this.mPhoneId)) {
                    if (!getSS().getRoaming()) {
                        Rlog.d(LOG_TAG, "In not roaming condition just show plmn without spn.");
                        OnsDisplayParams onsDisplayParams = new OnsDisplayParams(false, true, i, str, spn2);
                        return onsDisplayParams;
                    } else if (HwTelephonyManagerInner.getDefault().isCTSimCard(this.mPhoneId)) {
                        if (EMERGENCY_PLMN.equals(str) || NO_SERVICE_PLMN.equals(str)) {
                            Rlog.d(LOG_TAG, "out of service or emergency.");
                            OnsDisplayParams onsDisplayParams2 = new OnsDisplayParams(z, z2, i, str, spn2);
                            return onsDisplayParams2;
                        }
                        if (TextUtils.isEmpty(spn)) {
                            Rlog.d(LOG_TAG, "spn is null.");
                            try {
                                spn2 = URLDecoder.decode(CHINA_TELECOM_SPN, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                Rlog.d(LOG_TAG, "UnsupportedEncodingException.");
                            }
                        }
                        OnsDisplayParams onsDisplayParams3 = new OnsDisplayParams(true, true, i, str, spn2);
                        return onsDisplayParams3;
                    }
                }
                if (HuaweiTelephonyConfigs.isChinaTelecom() || (getSS().getRoaming() && !TextUtils.isEmpty(hplmn2) && "20404".equals(hplmn2) && "20404".equals(regplmn2) && 3 == (HwFullNetworkManager.getInstance().getSpecCardType(this.mGsmPhone.getPhoneId()) & 15))) {
                    Rlog.d(LOG_TAG, "In China Telecom, just show plmn without spn.");
                    OnsDisplayParams onsDisplayParams4 = new OnsDisplayParams(false, true, i, str, spn2);
                    return onsDisplayParams4;
                }
            } else if (spn2 == null || "".equals(spn2) || "CMCC".equals(spn2) || "China Mobile".equals(spn2)) {
                Rlog.d(LOG_TAG, "chinamobile just show plmn without spn.");
                OnsDisplayParams onsDisplayParams5 = new OnsDisplayParams(false, true, i, str, spn2);
                return onsDisplayParams5;
            } else {
                Rlog.d(LOG_TAG, "third party provider sim cust just show original rule.");
                OnsDisplayParams onsDisplayParams6 = new OnsDisplayParams(z, z2, i, str, spn2);
                return onsDisplayParams6;
            }
        }
        return null;
    }

    private void log(String string) {
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logi(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    private boolean containsPlmn(String plmn, String[] plmnArray) {
        if (plmn == null || plmnArray == null) {
            return false;
        }
        for (String h : plmnArray) {
            if (plmn.equals(h)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNitzOperatorName(int slotId) {
        String result = SystemProperties.get("persist.radio.nitz_hw_name");
        String result1 = SystemProperties.get("persist.radio.nitz_hw_name1");
        boolean z = false;
        if (!IS_MULTI_SIM_ENABLED) {
            if (result != null && result.length() > 0) {
                z = true;
            }
            return z;
        } else if (slotId == 0) {
            if (result != null && result.length() > 0) {
                z = true;
            }
            return z;
        } else if (1 == slotId) {
            if (result1 != null && result1.length() > 0) {
                z = true;
            }
            return z;
        } else {
            Rlog.e(LOG_TAG, "hasNitzOperatorName invalid sub id" + slotId);
            return false;
        }
    }

    private boolean getGsmRoamingCustByIMSIStart(boolean roaming) {
        int netClass;
        boolean roaming2;
        String regplmnRoamCustomString;
        String[] rules;
        int i;
        String regplmnRoamCustomString2 = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumericEx(this.mCr, "hw_roam_hplmn_ex");
            String regplmn = getNewSS().getOperatorNumeric();
            int netType = getNewSS().getVoiceNetworkType();
            TelephonyManager.getDefault();
            Rlog.d(LOG_TAG, "hplmn=" + hplmn + "  regplmn=" + regplmn + "  netType=" + netType + "  netClass=" + netClass);
            try {
                regplmnRoamCustomString2 = Settings.System.getString(this.mCr, "reg_plmn_roam_custom");
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception when got reg_plmn_roam_custom value", e);
            }
            if (regplmnRoamCustomString2 == null || hplmn == null || regplmn == null) {
                roaming2 = roaming;
            } else {
                String[] rules2 = regplmnRoamCustomString2.split(";");
                int length = rules2.length;
                String hplmnMcc = null;
                int i2 = 0;
                roaming2 = roaming;
                while (i2 < length) {
                    String[] rulePlmnRoam = rules2[i2].split(":");
                    if (rulePlmnRoam.length == 2) {
                        int ruleRoam = Integer.parseInt(rulePlmnRoam[0]);
                        rules = rules2;
                        String[] plmnRoam = rulePlmnRoam[1].split(",");
                        regplmnRoamCustomString = regplmnRoamCustomString2;
                        if (4 == ruleRoam && 3 == plmnRoam.length && containsPlmn(hplmn, plmnRoam[0].split("\\|"))) {
                            return getGsmRoamingCustBySpecialRule(plmnRoam[0], plmnRoam[1], plmnRoam[2], roaming2);
                        }
                        if (2 == plmnRoam.length) {
                            if (!plmnRoam[0].equals(hplmn) || !plmnRoam[1].equals(regplmn)) {
                                i = 2;
                            } else {
                                Rlog.d(LOG_TAG, "roaming customization by hplmn and regplmn success!");
                                if (1 == ruleRoam) {
                                    return true;
                                }
                                i = 2;
                                if (2 == ruleRoam) {
                                    return false;
                                }
                            }
                            if (3 == ruleRoam && hplmn.length() > i && regplmn.length() > i) {
                                hplmnMcc = hplmn.substring(0, 3);
                                String regplmnMcc = regplmn.substring(0, 3);
                                if (plmnRoam[0].equals(hplmnMcc) && plmnRoam[1].equals(regplmnMcc)) {
                                    roaming2 = false;
                                }
                            }
                        } else if (3 == plmnRoam.length) {
                            Rlog.d(LOG_TAG, "roaming customization by RAT");
                            if (plmnRoam[0].equals(hplmn) && plmnRoam[1].equals(regplmn) && plmnRoam[2].contains(String.valueOf(netClass + 1))) {
                                Rlog.d(LOG_TAG, "roaming customization by RAT success!");
                                if (1 == ruleRoam) {
                                    return true;
                                }
                                if (2 == ruleRoam) {
                                    return false;
                                }
                            }
                        } else {
                            continue;
                        }
                    } else {
                        rules = rules2;
                        regplmnRoamCustomString = regplmnRoamCustomString2;
                    }
                    i2++;
                    rules2 = rules;
                    regplmnRoamCustomString2 = regplmnRoamCustomString;
                }
                String str = hplmnMcc;
            }
            return roaming2;
        }
        Rlog.e(LOG_TAG, "mIccRecords null while getGsmRoamingCustByIMSIStart was called.");
        return roaming;
    }

    private boolean getGsmRoamingCustBySpecialRule(String hplmnlist, String regmcclist, String regplmnlist, boolean roaming) {
        if (!TextUtils.isEmpty(hplmnlist) && !TextUtils.isEmpty(regmcclist) && !TextUtils.isEmpty(regplmnlist) && this.mGsmPhone.mIccRecords.get() != null) {
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            String regplmn = getNewSS().getOperatorNumeric();
            if (TextUtils.isEmpty(hplmn) || TextUtils.isEmpty(regplmn) || 3 > regplmn.length()) {
                return roaming;
            }
            boolean matchHplmn = false;
            boolean matchRegmcc = false;
            boolean matchRegplmn = false;
            String[] hplmnString = hplmnlist.split("\\|");
            String[] regmccString = regmcclist.split("\\|");
            String[] regplmnString = regplmnlist.split("\\|");
            if (containsPlmn(hplmn, hplmnString)) {
                matchHplmn = true;
            }
            if (containsPlmn(regplmn.substring(0, 3), regmccString)) {
                matchRegmcc = true;
            }
            if (containsPlmn(regplmn, regplmnString)) {
                matchRegplmn = true;
            }
            if (matchHplmn && matchRegmcc && matchRegplmn) {
                Rlog.d(LOG_TAG, "match regmcc and regplmn, roaming");
                return true;
            } else if (matchHplmn && matchRegmcc) {
                Rlog.d(LOG_TAG, "only match regmcc, no roaming");
                return false;
            }
        }
        return roaming;
    }

    private String getDefaultSpn(String spn, String hplmn, String regplmn) {
        if (TextUtils.isEmpty(hplmn) || TextUtils.isEmpty(regplmn)) {
            return spn;
        }
        String defaultSpnString = Settings.System.getString(this.mCr, "hw_spnnull_defaultspn");
        if (TextUtils.isEmpty(defaultSpnString) || !Pattern.matches(REGEX, defaultSpnString)) {
            return spn;
        }
        String[] defaultSpnList = defaultSpnString.split(";");
        int length = defaultSpnList.length;
        int i = 0;
        while (i < length) {
            String[] defaultSpn = defaultSpnList[i].split(",");
            if (!hplmn.equals(defaultSpn[0]) || !regplmn.equals(defaultSpn[1])) {
                i++;
            } else {
                Rlog.d(LOG_TAG, "defaultspn is not null,use defaultspn instead " + defaultSpn[2]);
                return defaultSpn[2];
            }
        }
        return spn;
    }

    public void dispose() {
        if (this.mGsmPhone != null) {
            this.mCi.unregisterForCrrConn(this);
            this.mCi.unSetOnRegPLMNSelInfo(this);
            this.mGsmPhone.unregisterForMccChanged(this);
            this.mCi.unregisterForDSDSMode(this);
            this.mCi.unregisterForUnsol4RMimoStatus(this);
            this.mGsmPhone.getContext().unregisterReceiver(this.mIntentReceiver);
            this.mGsmPhone.getContext().unregisterReceiver(this.mCloudOtaBroadcastReceiver);
            this.mCi.unSetOnNetReject(this);
        }
    }

    private void sendBroadcastCrrConnInd(int modem0, int modem1, int modem2) {
        Rlog.i(LOG_TAG, "GSM sendBroadcastCrrConnInd");
        Intent intent = new Intent("com.huawei.action.ACTION_HW_CRR_CONN_IND");
        intent.putExtra("modem0", modem0);
        intent.putExtra("modem1", modem1);
        intent.putExtra("modem2", modem2);
        Rlog.i(LOG_TAG, "modem0: " + modem0 + " modem1: " + modem1 + " modem2: " + modem2);
        this.mGsmPhone.getContext().sendBroadcast(intent, "com.huawei.permission.CRRCONN_PERMISSION");
    }

    private void sendBroadcastRegPLMNSelInfo(int flag, int result) {
        Intent intent = new Intent("com.huawei.action.SIM_PLMN_SELINFO");
        int subId = this.mGsmPhone.getPhoneId();
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        intent.putExtra("flag", flag);
        intent.putExtra("res", result);
        Rlog.i(LOG_TAG, "subId: " + subId + " flag: " + flag + " result: " + result);
        this.mGsmPhone.getContext().sendBroadcast(intent, "com.huawei.permission.HUAWEI_BUSSINESS_PERMISSION");
    }

    private void sendBroadcastDsdsMode(int dsdsMode) {
        Intent intent = new Intent(InCallDataStateMachine.ACTION_HW_DSDS_MODE_STATE);
        intent.putExtra("dsdsmode", dsdsMode);
        Rlog.i(LOG_TAG, "GSM dsdsMode: " + dsdsMode);
        this.mGsmPhone.getContext().sendBroadcast(intent, "com.huawei.permission.DSDSMODE_PERMISSION");
    }

    private void addBroadCastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.refreshapn");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        filter.addAction("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        if (this.mHwCustGsmServiceStateManager != null) {
            filter = this.mHwCustGsmServiceStateManager.getCustIntentFilter(filter);
        }
        this.mGsmPhone.getContext().registerReceiver(this.mIntentReceiver, filter);
    }

    public boolean needUpdateNITZTime() {
        int otherCardState;
        int otherPhoneType;
        int otherCardType;
        String mcc = "";
        if (this.rplmn != null && this.rplmn.length() > 3) {
            mcc = this.rplmn.substring(0, 3);
        }
        if (INTERNATIONAL_MCC.equals(mcc)) {
            Rlog.d(LOG_TAG, "international mcc without conuntry code, not allow update time, rplmn is " + this.rplmn);
            return false;
        }
        NitzStateMachine nitzStateMachine = this.mGsst.getNitzState();
        if (nitzStateMachine != null) {
            Rlog.d(LOG_TAG, "nitzSpaceTime : " + nitzStateMachine.getNitzSpaceTime() + "  elapsedRealtime  :" + SystemClock.elapsedRealtime());
            if (nitzStateMachine.getNitzSpaceTime() > 1800000) {
                return true;
            }
        }
        int phoneId = this.mGsmPhone.getPhoneId();
        int ownCardType = HwTelephonyManagerInner.getDefault().getCardType(phoneId);
        int ownPhoneType = TelephonyManager.getDefault().getCurrentPhoneType(phoneId);
        if (phoneId == 0) {
            otherCardType = HwTelephonyManagerInner.getDefault().getCardType(1);
            otherCardState = HwTelephonyManagerInner.getDefault().getSubState(1);
            otherPhoneType = TelephonyManager.getDefault().getCurrentPhoneType(1);
        } else if (phoneId == 1) {
            otherCardType = HwTelephonyManagerInner.getDefault().getCardType(0);
            otherCardState = HwTelephonyManagerInner.getDefault().getSubState(0);
            otherPhoneType = TelephonyManager.getDefault().getCurrentPhoneType(0);
        } else {
            otherCardType = -1;
            otherCardState = 0;
            otherPhoneType = 0;
        }
        Rlog.d(LOG_TAG, "ownCardType = " + ownCardType + ", otherCardType = " + otherCardType + ", otherCardState = " + otherCardState + " ownPhoneType = " + ownPhoneType + ", otherPhoneType = " + otherPhoneType);
        if ((ownCardType == 41 || ownCardType == 43) && ownPhoneType == 2) {
            Rlog.d(LOG_TAG, "Cdma card, uppdate NITZ time!");
            return true;
        } else if ((otherCardType == 30 || otherCardType == 43 || otherCardType == 41) && 1 == otherCardState && otherPhoneType == 2) {
            HwReportManagerImpl.getDefault().reportNitzIgnore(phoneId, "CG_IGNORE");
            Rlog.d(LOG_TAG, "Other cdma card, ignore updating NITZ time!");
            return false;
        } else if (HwVSimUtils.isVSimOn() && HwVSimUtils.isVSimSub(phoneId)) {
            Rlog.d(LOG_TAG, "vsim phone, update NITZ time!");
            return true;
        } else if (phoneId == SystemProperties.getInt("gsm.sim.updatenitz", phoneId) || -1 == SystemProperties.getInt("gsm.sim.updatenitz", -1) || otherCardState == 0) {
            SystemProperties.set("gsm.sim.updatenitz", String.valueOf(phoneId));
            Rlog.d(LOG_TAG, "Update NITZ time, set update card : " + phoneId);
            return true;
        } else {
            HwReportManagerImpl.getDefault().reportNitzIgnore(phoneId, "GG_IGNORE");
            Rlog.d(LOG_TAG, "Ignore updating NITZ time, phoneid : " + phoneId);
            return false;
        }
    }

    public void processCTNumMatch(boolean roaming, UiccCardApplication uiccCardApplication) {
        Rlog.d(LOG_TAG, "processCTNumMatch, roaming: " + roaming);
        if (IS_CHINATELECOM && uiccCardApplication != null && IccCardApplicationStatus.AppState.APPSTATE_READY == uiccCardApplication.getState()) {
            int slotId = HwAddonTelephonyFactory.getTelephony().getDefault4GSlotId();
            Rlog.d(LOG_TAG, "processCTNumMatch->getDefault4GSlotId, slotId: " + slotId);
            if (HwTelephonyManagerInner.getDefault().isCTCdmaCardInGsmMode() && uiccCardApplication.getUiccCard() == UiccController.getInstance().getUiccCard(slotId)) {
                Rlog.d(LOG_TAG, "processCTNumMatch, isCTCdmaCardInGsmMode..");
                setCTNumMatchRoamingForSlot(slotId);
            }
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            handleIccRecordsEonsUpdate(msg);
        } else if (i != 64) {
            switch (i) {
                case EVENT_RPLMNS_STATE_CHANGED /*101*/:
                    handleRplmnsStateChanged(msg);
                    return;
                case EVENT_DELAY_UPDATE_REGISTER_STATE_DONE /*102*/:
                    handleDelayUpdateRegisterStateDone();
                    return;
                case EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH /*103*/:
                    handleDelayUpdateGsmSingalStrength();
                    return;
                case EVENT_CA_STATE_CHANGED /*104*/:
                    handleCaStateChanged(msg);
                    return;
                case EVENT_NETWORK_REJECTED_CASE /*105*/:
                    handleNetworkRejectedCase(msg);
                    return;
                case EVENT_4R_MIMO_ENABLE /*106*/:
                    handle4GMimoEnable(msg);
                    return;
                default:
                    switch (i) {
                        case EVENT_PLMN_SELINFO /*151*/:
                            handlePlmnSelInfo(msg);
                            return;
                        case EVENT_CRR_CONN /*152*/:
                            handleCrrConn(msg);
                            return;
                        case EVENT_DSDS_MODE /*153*/:
                            handleDsdsMode(msg);
                            return;
                        case EVENT_MCC_CHANGED /*154*/:
                            handleMccChanged();
                            return;
                        case EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE /*155*/:
                            Rlog.d(LOG_TAG, "[Phone" + this.mPhoneId + "]  Delay Timer expired, begin get register state");
                            this.mRefreshStateEcc = true;
                            this.mGsst.pollState();
                            return;
                        default:
                            super.handleMessage(msg);
                            return;
                    }
            }
        } else {
            handlePollLocationInfo(msg);
        }
    }

    private void handleDelayUpdateGsmSingalStrength() {
        logd("event update gsm signal strength");
        this.mDoubleSignalStrength.proccessAlaphFilter(this.mGsst.getSignalStrength(), this.mModemSignalStrength);
        this.mGsmPhone.notifySignalStrength();
    }

    private void handleIccRecordsEonsUpdate(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception != null) {
            loge("EVENT_ICC_RECORDS_EONS_UPDATED exception " + ar.exception);
            return;
        }
        processIccEonsRecordsUpdated(((Integer) ar.result).intValue());
    }

    private void handleDelayUpdateRegisterStateDone() {
        logd("[Phone" + this.mPhoneId + "]Delay Timer expired, begin get register state");
        this.mRefreshState = true;
        this.mGsst.pollState();
    }

    private void handleCaStateChanged(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null) {
            boolean z = false;
            if (((int[]) ar.result)[0] == 1) {
                z = true;
            }
            onCaStateChanged(z);
            return;
        }
        log("EVENT_CA_STATE_CHANGED: exception;");
    }

    private void handleRplmnsStateChanged(Message msg) {
        logd("[SLOT" + this.mPhoneId + "]EVENT_RPLMNS_STATE_CHANGED");
        this.oldRplmn = this.rplmn;
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.result == null || !(ar.result instanceof String)) {
            this.rplmn = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
        } else {
            this.rplmn = (String) ar.result;
        }
        setNetworkSelectionModeAutomaticHw(this.oldRplmn, this.rplmn);
        String mcc = "";
        String oldMcc = "";
        if (this.rplmn != null && this.rplmn.length() > 3) {
            mcc = this.rplmn.substring(0, 3);
        }
        if (this.oldRplmn != null && this.oldRplmn.length() > 3) {
            oldMcc = this.oldRplmn.substring(0, 3);
        }
        if (!"".equals(mcc) && !mcc.equals(oldMcc)) {
            this.mGsmPhone.notifyMccChanged(mcc);
            Rlog.d(LOG_TAG, "rplmn mcc changed.");
        }
        logd("rplmn" + this.rplmn);
        if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false) && this.mGsmPhone.mDcTracker != null) {
            this.mGsmPhone.mDcTracker.checkPLMN(this.rplmn);
        }
        if (SystemProperties.getBoolean("ro.config.hw_globalEcc", true)) {
            logd("the global emergency numbers custom-make does enable!!!!");
            toGetRplmnsThenSendEccNum();
        }
    }

    private void handlePollLocationInfo(Message msg) {
        logd("GSM EVENT_POLL_LOCATION_INFO");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception != null) {
            loge("EVENT_POLL_LOCATION_INFO exception " + ar.exception);
            return;
        }
        String[] states = (String[]) ar.result;
        logi("CLEARCODE EVENT_POLL_LOCATION_INFO");
        if (states.length == 4) {
            try {
                if (states[2] != null && states[2].length() > 0) {
                    this.mRac = Integer.parseInt(states[2], 16);
                    logd("CLEARCODE mRac = " + this.mRac);
                }
            } catch (NumberFormatException ex) {
                loge("error parsing LocationInfoState: " + ex);
            }
        }
    }

    private void handleCrrConn(Message msg) {
        logd("GSM EVENT_CRR_CONN");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null) {
            int[] response = (int[]) ar.result;
            if (response.length > 2) {
                sendBroadcastCrrConnInd(response[0], response[1], response[2]);
                return;
            }
            return;
        }
        loge("GSM EVENT_CRR_CONN: exception;");
    }

    private void handlePlmnSelInfo(Message msg) {
        logd("EVENT_PLMN_SELINFO");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null) {
            int[] response = (int[]) ar.result;
            if (response.length != 0) {
                sendBroadcastRegPLMNSelInfo(response[0], response[1]);
            }
        }
    }

    private void handleDsdsMode(Message msg) {
        logd("GSM EVENT_DSDS_MODE");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null) {
            int[] response = (int[]) ar.result;
            if (response.length != 0) {
                sendBroadcastDsdsMode(response[0]);
                return;
            }
            return;
        }
        loge("GSM EVENT_DSDS_MODE: exception;");
    }

    private void handleNetworkRejectedCase(Message msg) {
        logd("EVENT_NETWORK_REJECTED_CASE");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception != null) {
            loge("EVENT_NETWORK_REJECTED_CASE exception " + ar.exception);
            return;
        }
        onNetworkReject(ar);
    }

    private void handleMccChanged() {
        logd("EVENT_MCC_CHANGED");
        SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
    }

    private void handle4GMimoEnable(Message msg) {
        if (MIMO_4R_REPORT) {
            logd("EVENT_4R_MIMO_ENABLE");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                loge("EVENT_MIMO_ENABLE exception " + ar.exception);
                return;
            }
            on4RMimoChange(ar);
        }
    }

    private void setNetworkSelectionModeAutomaticHw(String oldRplmn2, String rplmn2) {
        String autoSelectMccs = Settings.System.getString(this.mContext.getContentResolver(), "hw_auto_select_network_mcc");
        if (!TextUtils.isEmpty(autoSelectMccs) && !TextUtils.isEmpty(oldRplmn2) && !TextUtils.isEmpty(rplmn2) && oldRplmn2.length() >= 3 && rplmn2.length() >= 3) {
            if (this.mGsmPhone == null || !this.mGsmPhone.getServiceState().getIsManualSelection()) {
                Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomaticHw - already auto, ignoring.");
                return;
            }
            String[] mccs = autoSelectMccs.split(",");
            int i = 0;
            String oldMcc = oldRplmn2.substring(0, 3);
            String newMcc = rplmn2.substring(0, 3);
            boolean isNeedSelectAuto = false;
            while (true) {
                if (i < mccs.length) {
                    if (!oldMcc.equals(mccs[i]) && newMcc.equals(mccs[i])) {
                        isNeedSelectAuto = true;
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomaticHw isNeedSelectAuto:" + isNeedSelectAuto);
            if (isNeedSelectAuto) {
                this.mGsmPhone.setNetworkSelectionModeAutomatic(null);
            }
        }
    }

    private void onNetworkReject(AsyncResult ar) {
        if (ar.exception == null) {
            String[] data = (String[]) ar.result;
            String rejectplmn = null;
            int rejectdomain = -1;
            int rejectcause = -1;
            int rejectrat = -1;
            int orignalrejectcause = -1;
            if (data.length > 0) {
                try {
                    if (data[0] != null && data[0].length() > 0) {
                        rejectplmn = data[0];
                    }
                    if (data.length > 1 && data[1] != null && data[1].length() > 0) {
                        rejectdomain = Integer.parseInt(data[1]);
                    }
                    if (data.length > 2 && data[2] != null && data[2].length() > 0) {
                        rejectcause = Integer.parseInt(data[2]);
                    }
                    if (data.length > 3 && data[3] != null && data[3].length() > 0) {
                        rejectrat = Integer.parseInt(data[3]);
                    }
                    if (IS_VERIZON && data.length > 4 && data[4] != null && data[4].length() > 0) {
                        orignalrejectcause = Integer.parseInt(data[4]);
                    }
                } catch (Exception ex) {
                    Rlog.e(LOG_TAG, "error parsing NetworkReject!", ex);
                }
                if (rejectrat == 2 && rejectcause == 19) {
                    this.mRejectflag = true;
                }
                Rlog.d(LOG_TAG, "NetworkReject:PLMN = " + rejectplmn + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat + " rejNum = " + this.rejNum + "mRejectflag = " + this.mRejectflag);
                if (SHOW_REJ_INFO_KT && this.mGsst.returnObject() != null) {
                    this.mGsst.returnObject().handleNetworkRejectionEx(rejectcause, rejectrat);
                }
                if (PS_CLEARCODE) {
                    if (2 == rejectrat) {
                        this.rejNum++;
                    }
                    if (this.rejNum >= 3) {
                        this.mGsmPhone.setPreferredNetworkType(3, null);
                        HwNetworkTypeUtils.saveNetworkModeToDB(this.mGsmPhone.getContext(), this.mGsmPhone.getSubId(), 3);
                        this.rejNum = 0;
                        this.mRac = -1;
                    }
                }
                if (IS_VERIZON && this.mGsst.returnObject() != null) {
                    this.mGsst.returnObject().handleLteEmmCause(this.mGsmPhone.getPhoneId(), rejectrat, orignalrejectcause);
                }
            }
        }
    }

    public boolean isNetworkTypeChanged(SignalStrength oldSS, SignalStrength newSS) {
        int newState = 0;
        int oldState = 0;
        if (newSS.getGsmSignalStrength() < -1) {
            newState = 0 | 1;
        }
        if (oldSS.getGsmSignalStrength() < -1) {
            oldState = 0 | 1;
        }
        if (newSS.getWcdmaRscp() < -1) {
            newState |= 2;
        }
        if (oldSS.getWcdmaRscp() < -1) {
            oldState |= 2;
        }
        if (newSS.getLteRsrp() < -1) {
            newState |= 4;
        }
        if (oldSS.getLteRsrp() < -1) {
            oldState |= 4;
        }
        if (newSS.getNrRsrp() < -1) {
            newState |= 32;
        }
        if (oldSS.getNrRsrp() < -1) {
            oldState |= 32;
        }
        if (newState == 0 || newState == oldState) {
            return false;
        }
        return true;
    }

    public void sendMessageDelayUpdateSingalStrength(int time) {
        Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]sendMessageDelayUpdateSingalStrength, time: " + time);
        Message msg = obtainMessage();
        msg.what = EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH;
        if (time == 0) {
            sendMessageDelayed(msg, (long) DELAY_DURING_TIME);
        } else {
            sendMessageDelayed(msg, (long) time);
        }
    }

    public boolean notifySignalStrength(SignalStrength oldSS, SignalStrength newSS) {
        boolean notified;
        this.mModemSignalStrength = new SignalStrength(newSS);
        if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "] no notify signal");
        }
        if (isNetworkTypeChanged(oldSS, newSS)) {
            Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]Network is changed immediately!");
            if (hasMessages(EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH)) {
                removeMessages(EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH);
            }
            this.mDoubleSignalStrength = new DoubleSignalStrength(newSS);
            this.mGsst.setSignalStrength(newSS);
            notified = true;
        } else if (hasMessages(EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH)) {
            Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]has delay update msg");
            notified = false;
        } else {
            this.mOldDoubleSignalStrength = this.mDoubleSignalStrength;
            this.mDoubleSignalStrength = new DoubleSignalStrength(newSS);
            this.mDoubleSignalStrength.proccessAlaphFilter(this.mOldDoubleSignalStrength, newSS, this.mModemSignalStrength, false);
            notified = true;
        }
        if (notified) {
            try {
                this.mGsmPhone.notifySignalStrength();
            } catch (NullPointerException e) {
                Rlog.e(LOG_TAG, "onSignalStrengthResult() Phone already destroyed, SignalStrength not notified");
            }
        }
        return notified;
    }

    public int getCARilRadioType(int type) {
        int radioType = type;
        if (SHOW_4G_PLUS_ICON && 19 == type) {
            radioType = 14;
        }
        Rlog.d(LOG_TAG, "[CA]radioType=" + radioType + " type=" + type);
        return radioType;
    }

    public int updateCAStatus(int currentType) {
        int newType = currentType;
        if (SHOW_4G_PLUS_ICON) {
            Rlog.d(LOG_TAG, "[CA] currentType=" + currentType + " oldCAstate=" + this.mOldCAstate);
            boolean hasCAactivated = 19 == currentType && 19 != this.mOldCAstate;
            boolean hasCAdeActivated = 19 != currentType && 19 == this.mOldCAstate;
            this.mOldCAstate = currentType;
            if (hasCAactivated || hasCAdeActivated) {
                Intent intentLteCAState = new Intent("com.huawei.intent.action.LTE_CA_STATE");
                intentLteCAState.putExtra("subscription", this.mGsmPhone.getSubId());
                if (hasCAactivated) {
                    intentLteCAState.putExtra("LteCAstate", true);
                    Rlog.d(LOG_TAG, "[CA] CA activated !");
                } else if (hasCAdeActivated) {
                    intentLteCAState.putExtra("LteCAstate", false);
                    Rlog.d(LOG_TAG, "[CA] CA deactivated !");
                }
                this.mGsmPhone.getContext().sendBroadcast(intentLteCAState);
            }
        }
        return newType;
    }

    private void onCaStateChanged(boolean caActive) {
        boolean oldCaActive = 19 == this.mOldCAstate;
        if (SHOW_4G_PLUS_ICON && oldCaActive != caActive) {
            if (caActive) {
                this.mOldCAstate = 19;
                Rlog.d(LOG_TAG, "[CA] CA activated !");
            } else {
                this.mOldCAstate = 0;
                Rlog.d(LOG_TAG, "[CA] CA deactivated !");
            }
            Intent intentLteCAState = new Intent("com.huawei.intent.action.LTE_CA_STATE");
            intentLteCAState.putExtra("subscription", this.mGsmPhone.getSubId());
            intentLteCAState.putExtra("LteCAstate", caActive);
            this.mGsmPhone.getContext().sendBroadcast(intentLteCAState);
        }
    }

    private void processIccEonsRecordsUpdated(int eventCode) {
        if (eventCode == 2) {
            this.mGsst.updateSpnDisplay();
        } else if (eventCode == 100) {
            this.mGsst.updateSpnDisplay();
        }
    }

    public void unregisterForRecordsEvents(IccRecords r) {
        if (r != null) {
            r.unregisterForRecordsEvents(this);
        }
    }

    public void registerForRecordsEvents(IccRecords r) {
        if (r != null) {
            r.registerForRecordsEvents(this, 1, null);
        }
    }

    public String getEons(String defaultValue) {
        if (HwModemCapability.isCapabilitySupport(5)) {
            return defaultValue;
        }
        String result = null;
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        if (r != null && !r.isEonsDisabled()) {
            Rlog.d(LOG_TAG, "getEons():get plmn from SIM card! ");
            if (updateEons(r)) {
                result = r.getEons();
            }
        } else if (r != null && r.isEonsDisabled()) {
            String hplmn = r.getOperatorNumeric();
            String regplmn = getSS().getOperatorNumeric();
            if (!(hplmn == null || !hplmn.equals(regplmn) || r.getEons() == null)) {
                Rlog.d(LOG_TAG, "getEons():get plmn from Cphs when register to hplmn ");
                result = r.getEons();
            }
        }
        Rlog.d(LOG_TAG, "result = " + result);
        if (TextUtils.isEmpty(result)) {
            result = defaultValue;
        }
        return result;
    }

    public boolean updateEons(IccRecords r) {
        int lac = -1;
        if (this.mGsst.mCellLoc != null) {
            lac = ((GsmCellLocation) this.mGsst.mCellLoc).getLac();
        }
        if (r != null) {
            return r.updateEons(getSS().getOperatorNumeric(), lac);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void cancelDeregisterStateDelayTimer() {
        if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            Rlog.d(LOG_TAG, "[SUB" + this.mPhoneId + "]cancelDeregisterStateDelayTimer");
            removeMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE);
        }
    }

    private void delaySendDeregisterStateChange(int delayedTime) {
        if (!hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            Message msg = obtainMessage();
            msg.what = EVENT_DELAY_UPDATE_REGISTER_STATE_DONE;
            sendMessageDelayed(msg, (long) delayedTime);
            Rlog.d(LOG_TAG, "[SUB" + this.mPhoneId + "]RegisterStateChange timer is running,do nothing");
        }
    }

    public boolean proccessGsmDelayUpdateRegisterStateDone(ServiceState oldSS, ServiceState newSS) {
        if (HwModemCapability.isCapabilitySupport(6)) {
            return false;
        }
        if (delayUpdateGsmEcctoNoserviceState(oldSS, newSS)) {
            return true;
        }
        boolean lostNework = ((oldSS.getVoiceRegState() != 0 && oldSS.getDataRegState() != 0) || newSS.getVoiceRegState() == 0 || newSS.getDataRegState() == 0) ? false : true;
        boolean isSubDeactivated = SubscriptionController.getInstance().getSubState(this.mGsmPhone.getSubId()) == 0;
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        IccCardConstants.State mExternalState = IccCardConstants.State.UNKNOWN;
        IccCard iccCard = this.mGsmPhone.getIccCard();
        if (iccCard != null) {
            mExternalState = iccCard.getState();
        }
        PhoneConstants.State callState = PhoneFactory.getPhone(this.mGsmPhone.getPhoneId()).getState();
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]lostNework : " + lostNework + ", desiredPowerState : " + this.mGsst.getDesiredPowerState() + ", radiostate : " + this.mCi.getRadioState() + ", mRadioOffByDoRecovery : " + this.mGsst.getDoRecoveryTriggerState() + ", isSubDeactivated : " + isSubDeactivated + ", newMainSlot : " + newMainSlot + ", phoneOOS : " + this.mGsmPhone.getOOSFlag() + ", isUserPref4GSlot : " + HwFullNetworkManager.getInstance().isUserPref4GSlot(this.mMainSlot) + ", mExternalState : " + mExternalState + ", callState : " + callState);
        if (newSS.getDataRegState() == 0 || newSS.getVoiceRegState() == 0 || !this.mGsst.getDesiredPowerState() || this.mCi.getRadioState() == CommandsInterface.RadioState.RADIO_OFF || this.mGsst.getDoRecoveryTriggerState() || isCardInvalid(isSubDeactivated, this.mGsmPhone.getSubId()) || !HwFullNetworkManager.getInstance().isUserPref4GSlot(this.mMainSlot) || this.mGsmPhone.getOOSFlag() || newSS.getDataRegState() == 3 || mExternalState == IccCardConstants.State.PUK_REQUIRED || callState != PhoneConstants.State.IDLE) {
            this.mMainSlot = newMainSlot;
            cancelDeregisterStateDelayTimer();
        } else if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            return true;
        } else {
            if (lostNework && !this.mRefreshState) {
                int delayedTime = getSendDeregisterStateDelayedTime(oldSS, newSS);
                if (delayedTime > 0) {
                    delaySendDeregisterStateChange(delayedTime);
                    newSS.setStateOutOfService();
                    return true;
                }
            }
        }
        this.mRefreshState = false;
        return false;
    }

    private int getSendDeregisterStateDelayedTime(ServiceState oldSS, ServiceState newSS) {
        boolean isPsLostNetwork = false;
        boolean isCsLostNetwork = (oldSS.getVoiceRegState() != 0 || newSS.getVoiceRegState() == 0 || newSS.getDataRegState() == 0) ? false : true;
        if (!(oldSS.getDataRegState() != 0 || newSS.getVoiceRegState() == 0 || newSS.getDataRegState() == 0)) {
            isPsLostNetwork = true;
        }
        try {
            int slotId = SubscriptionManager.getSlotIndex(this.mGsmPhone.getPhoneId());
            Integer defaultTime = (Integer) HwCfgFilePolicy.getValue("lostnetwork.default_timer", slotId, Integer.class);
            Integer delaytimerCs2G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_cs2G", slotId, Integer.class);
            Integer delaytimerCs3G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_cs3G", slotId, Integer.class);
            Integer delaytimerCs4G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_cs4G", slotId, Integer.class);
            Integer delaytimerPs2G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_ps2G", slotId, Integer.class);
            Integer delaytimerPs3G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_ps3G", slotId, Integer.class);
            Integer delaytimerPs4G = (Integer) HwCfgFilePolicy.getValue("lostnetwork.delaytimer_ps4G", slotId, Integer.class);
            if (defaultTime != null) {
                this.DELAYED_TIME_DEFAULT_VALUE = defaultTime.intValue();
            }
            if (delaytimerCs2G != null) {
                this.DELAYED_TIME_NETWORKSTATUS_CS_2G = delaytimerCs2G.intValue() * 1000;
            }
            if (delaytimerCs3G != null) {
                this.DELAYED_TIME_NETWORKSTATUS_CS_3G = delaytimerCs3G.intValue() * 1000;
            }
            if (delaytimerCs4G != null) {
                this.DELAYED_TIME_NETWORKSTATUS_CS_4G = delaytimerCs4G.intValue() * 1000;
            }
            if (delaytimerPs2G != null) {
                this.DELAYED_TIME_NETWORKSTATUS_PS_2G = delaytimerPs2G.intValue() * 1000;
            }
            if (delaytimerPs3G != null) {
                this.DELAYED_TIME_NETWORKSTATUS_PS_3G = delaytimerPs3G.intValue() * 1000;
            }
            if (delaytimerPs4G != null) {
                this.DELAYED_TIME_NETWORKSTATUS_PS_4G = delaytimerPs4G.intValue() * 1000;
            }
        } catch (Exception ex) {
            Rlog.e(LOG_TAG, "lostnetwork error!", ex);
        }
        int delayedTime = 0;
        TelephonyManager.getDefault();
        int networkClass = TelephonyManager.getNetworkClass(getNetworkType(oldSS));
        if (isCsLostNetwork) {
            if (networkClass == 1) {
                delayedTime = this.DELAYED_TIME_NETWORKSTATUS_CS_2G;
            } else if (networkClass == 2) {
                delayedTime = this.DELAYED_TIME_NETWORKSTATUS_CS_3G;
            } else if (networkClass == 3) {
                delayedTime = this.DELAYED_TIME_NETWORKSTATUS_CS_4G;
            } else {
                delayedTime = this.DELAYED_TIME_DEFAULT_VALUE * 1000;
            }
        } else if (isPsLostNetwork) {
            if (networkClass == 1) {
                delayedTime = this.DELAYED_TIME_NETWORKSTATUS_PS_2G;
            } else if (networkClass == 2) {
                delayedTime = this.DELAYED_TIME_NETWORKSTATUS_PS_3G;
            } else if (networkClass == 3) {
                delayedTime = this.DELAYED_TIME_NETWORKSTATUS_PS_4G;
            } else {
                delayedTime = this.DELAYED_TIME_DEFAULT_VALUE * 1000;
            }
        }
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "] delay time = " + delayedTime);
        return delayedTime;
    }

    public boolean isUpdateLacAndCid(int cid) {
        if (!HwModemCapability.isCapabilitySupport(12) || cid != 0) {
            return true;
        }
        Rlog.d(LOG_TAG, "do not set the Lac and Cid when cid is 0");
        return false;
    }

    public void toGetRplmnsThenSendEccNum() {
        String hplmn = TelephonyManager.getDefault().getSimOperator(this.mPhoneId);
        String forceEccState = SystemProperties.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]GECC-toGetRplmnsThenSendEccNum: hplmn = " + hplmn + "; forceEccState = " + forceEccState);
        UiccProfile profile = UiccController.getInstance().getUiccProfileForPhone(PhoneFactory.getDefaultPhone().getPhoneId());
        if ((profile != null && profile.getIccCardStateHW()) || hplmn.equals("") || forceEccState.equals("usim_absent")) {
            String rplmns = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
            if (!rplmns.equals("")) {
                this.mGsmPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
                this.mGsmPhone.globalEccCustom(rplmns);
            }
        }
    }

    public void sendGsmRoamingIntentIfDenied(int regState, int rejectCode) {
        if ((regState == 3 || getNewSS().isEmergencyOnly()) && rejectCode == 10) {
            Rlog.d(LOG_TAG, "Posting Managed roaming intent sub = " + this.mGsmPhone.getSubId());
            Intent intent = new Intent("codeaurora.intent.action.ACTION_MANAGED_ROAMING_IND");
            intent.putExtra("subscription", this.mGsmPhone.getSubId());
            this.mGsmPhone.getContext().sendBroadcast(intent);
        }
    }

    private OnsDisplayParams getOnsDisplayParamsBySpnOnly(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        String plmnRes = plmn;
        String spnRes = spn;
        int mRule = rule;
        boolean mShowSpn = showSpn;
        boolean mShowPlmn = showPlmn;
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        if (r != null) {
            String hPlmn = r.getOperatorNumeric();
            String regPlmn = getSS().getOperatorNumeric();
            String spnSim = r.getServiceProviderName();
            int ruleSim = r.getDisplayRule(getSS());
            Rlog.d(LOG_TAG, "SpnOnly spn:" + spnSim + ",hplmn:" + hPlmn + ",regPlmn:" + regPlmn);
            if (!TextUtils.isEmpty(spnSim) && isMccForSpn(r.getOperatorNumeric()) && !TextUtils.isEmpty(hPlmn) && hPlmn.length() > 3) {
                String currentMcc = hPlmn.substring(0, 3);
                if (!TextUtils.isEmpty(regPlmn) && regPlmn.length() > 3 && currentMcc.equals(regPlmn.substring(0, 3))) {
                    mShowSpn = true;
                    mShowPlmn = false;
                    spnRes = spnSim;
                    mRule = ruleSim;
                    plmnRes = "";
                }
            }
            OnsDisplayParams onsSpn = new OnsDisplayParams(mShowSpn, mShowPlmn, mRule, plmnRes, spnRes);
            return onsSpn;
        }
        OnsDisplayParams onsSpn2 = new OnsDisplayParams(mShowSpn, mShowPlmn, mRule, plmnRes, spnRes);
        return onsSpn2;
    }

    private boolean isMccForSpn(String currentMccmnc) {
        String strMcc = Settings.System.getString(this.mContext.getContentResolver(), "hw_mcc_showspn_only");
        HashSet<String> mShowspnOnlyMcc = new HashSet<>();
        if (currentMccmnc == null || currentMccmnc.length() < 3) {
            return false;
        }
        String currentMcc = currentMccmnc.substring(0, 3);
        if (strMcc == null || mShowspnOnlyMcc.size() != 0) {
            return false;
        }
        String[] mcc = strMcc.split(",");
        for (String trim : mcc) {
            mShowspnOnlyMcc.add(trim.trim());
        }
        return mShowspnOnlyMcc.contains(currentMcc);
    }

    private boolean getNoRoamingByMcc(ServiceState mSS) {
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        if (!(r == null || mSS == null)) {
            String hplmn = r.getOperatorNumeric();
            String regplmn = mSS.getOperatorNumeric();
            if (isMccForNoRoaming(hplmn)) {
                String currentMcc = hplmn.substring(0, 3);
                if (regplmn != null && regplmn.length() > 3 && currentMcc.equals(regplmn.substring(0, 3))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMccForNoRoaming(String currentMccmnc) {
        String strMcc = Settings.System.getString(this.mContext.getContentResolver(), "hw_mcc_show_no_roaming");
        HashSet<String> mShowNoRoamingMcc = new HashSet<>();
        if (currentMccmnc == null || currentMccmnc.length() < 3) {
            return false;
        }
        String currentMcc = currentMccmnc.substring(0, 3);
        if (strMcc == null || mShowNoRoamingMcc.size() != 0) {
            return false;
        }
        String[] mcc = strMcc.split(",");
        for (String trim : mcc) {
            mShowNoRoamingMcc.add(trim.trim());
        }
        return mShowNoRoamingMcc.contains(currentMcc);
    }

    public void setRac(int rac) {
        this.mRac = rac;
    }

    public int getRac() {
        return this.mRac;
    }

    public void getLocationInfo() {
        if (PS_CLEARCODE) {
            this.mCi.getLocationInfo(obtainMessage(64));
        }
    }

    private void registerCloudOtaBroadcastReceiver() {
        Rlog.e(LOG_TAG, "HwCloudOTAService registerCloudOtaBroadcastReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction("cloud.ota.mcc.UPDATE");
        filter.addAction("cloud.ota.dplmn.UPDATE");
        this.mGsmPhone.getContext().registerReceiver(this.mCloudOtaBroadcastReceiver, filter, "huawei.permission.RECEIVE_CLOUD_OTA_UPDATA", null);
    }

    public int updateHSPAStatus(int type, GsmCdmaPhone phone) {
        if (KEEP_3GPLUS_HPLUS) {
            Rlog.d(LOG_TAG, "updateHSPAStatus dataRadioTechnology: " + type);
            int lac = -1;
            int cid = -1;
            if (phone != null) {
                CellLocation cl = phone.getCellLocation();
                if (cl instanceof GsmCellLocation) {
                    GsmCellLocation cellLocation = (GsmCellLocation) cl;
                    lac = cellLocation.getLac();
                    cid = cellLocation.getCid();
                }
            }
            if (this.lastLac == lac && this.lastCid == cid && this.lastType == 15 && (3 == type || 9 == type || 10 == type || 11 == type)) {
                type = this.lastType;
            }
            if (15 == type) {
                this.lastLac = lac;
                this.lastCid = cid;
                this.lastType = type;
            }
        }
        return type;
    }

    public boolean is4RMimoEnabled() {
        if (!MIMO_4R_REPORT) {
            return false;
        }
        Rlog.d(LOG_TAG, "is4RMimoEnabled = " + this.mis4RMimoEnable);
        return this.mis4RMimoEnable;
    }

    private void on4RMimoChange(AsyncResult ar) {
        int[] responseArray = (int[]) ar.result;
        int mimoResult = 0;
        if (responseArray.length != 0) {
            mimoResult = responseArray[0];
        }
        if (mimoResult == 1 && !this.mis4RMimoEnable) {
            this.mis4RMimoEnable = true;
        } else if (mimoResult == 0 && this.mis4RMimoEnable) {
            this.mis4RMimoEnable = false;
        } else {
            return;
        }
        Rlog.d(LOG_TAG, "4R_MIMO_ENABLE = " + mimoResult);
        Intent intent = new Intent("com.huawei.intent.action.4R_MIMO_CHANGE");
        intent.addFlags(536870912);
        intent.putExtra("4RMimoStatus", this.mis4RMimoEnable);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mGsmPhone.getPhoneId());
        this.mGsmPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public String getEonsWithoutCphs() {
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        if (r == null || r.isEonsDisabled()) {
            return null;
        }
        Rlog.d(LOG_TAG, "getEonsWithoutCphs():get plmn from SIM card! ");
        if (updateEons(r)) {
            return r.getEons();
        }
        return null;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsSpnPrior(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        boolean z = showSpn;
        boolean z2 = showPlmn;
        String plmnRes = plmn;
        String spnRes = spn;
        String temPnn = null;
        int newRule = 1;
        String cardspn = this.mGsmPhone.mIccRecords.get() != null ? ((IccRecords) this.mGsmPhone.mIccRecords.get()).getServiceProviderName() : null;
        if (!TextUtils.isEmpty(cardspn)) {
            newRule = 1;
            spnRes = cardspn;
        } else {
            temPnn = getEonsWithoutCphs();
            if (!TextUtils.isEmpty(temPnn)) {
                newRule = 2;
                plmnRes = temPnn;
            }
        }
        boolean showPlmnRes = false;
        boolean showSpnRes = (newRule & 1) == 1;
        if (newRule & true) {
            showPlmnRes = true;
        }
        Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsSpnPrior: cardspn= " + cardspn + " temPnn= " + temPnn + " newRule= " + newRule);
        OnsDisplayParams onsDisplayParams = new OnsDisplayParams(showSpnRes, showPlmnRes, newRule, plmnRes, spnRes);
        return onsDisplayParams;
    }

    private void getGsmOnsDisplayParamsNitzNamePrior(OnsDisplayParams odpOri) {
        int slotId = this.mGsmPhone.getPhoneId();
        Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsNitzNamePrior hasNitzOperatorName = " + hasNitzOperatorName(slotId));
        if (hasNitzOperatorName(slotId)) {
            odpOri.mPlmn = getSS().getOperatorAlphaLong();
            if (odpOri.mRule == 1) {
                Rlog.d(LOG_TAG, "sim rule is 1 and network has value, show network plmn pri");
                odpOri.mRule = 2;
                odpOri.mShowSpn = false;
                odpOri.mShowPlmn = true;
            }
        }
    }

    public boolean getRejFlag() {
        return this.mRejectflag;
    }

    public void clearRejFlag() {
        this.mRejectflag = false;
    }

    private String getSpnFromTempPlmn(CustPlmnMember cpm, String spn, String tempPlmn, String hplmn, String regplmn) {
        if (1 != cpm.rule || !TextUtils.isEmpty(spn)) {
            return spn;
        }
        Rlog.d(LOG_TAG, " want to show spn while spn is null,use plmn instead " + tempPlmn);
        String spn2 = tempPlmn;
        if (TextUtils.isEmpty(((IccRecords) this.mGsmPhone.mIccRecords.get()).getServiceProviderName())) {
            return getDefaultSpn(spn2, hplmn, regplmn);
        }
        return spn2;
    }

    private void getOnsByCpm(CustPlmnMember cpm, String hplmn, String custSpn, String tempPlmn, String regplmn, OnsDisplayParams odpTemp) {
        if (cpm.acquireFromCust(hplmn, getSS(), custSpn)) {
            odpTemp.mShowSpn = cpm.judgeShowSpn(odpTemp.mShowSpn);
            odpTemp.mShowPlmn = cpm.rule == 0 ? odpTemp.mShowPlmn : cpm.showPlmn;
            odpTemp.mRule = cpm.rule == 0 ? odpTemp.mRule : cpm.rule;
            odpTemp.mPlmn = cpm.judgePlmn(odpTemp.mPlmn);
            odpTemp.mSpn = cpm.judgeSpn(odpTemp.mSpn);
            odpTemp.mSpn = getSpnFromTempPlmn(cpm, odpTemp.mSpn, tempPlmn, hplmn, regplmn);
            if (4 == cpm.rule) {
                syncRuleByPlmnAndRule(getEonsWithoutCphs(), odpTemp);
                boolean z = true;
                odpTemp.mShowSpn = (odpTemp.mRule & 1) == 1;
                if ((odpTemp.mRule & 2) != 2) {
                    z = false;
                }
                odpTemp.mShowPlmn = z;
            }
            if (5 == cpm.rule) {
                OnsDisplayParams odprule5 = getGsmOnsDisplayParamsSpnPrior(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
                odpTemp.mShowSpn = odprule5.mShowSpn;
                odpTemp.mShowPlmn = odprule5.mShowPlmn;
                odpTemp.mRule = odprule5.mRule;
                odpTemp.mPlmn = odprule5.mPlmn;
                odpTemp.mSpn = odprule5.mSpn;
            }
            Rlog.d(LOG_TAG, "showSpn2 =" + odpTemp.mShowSpn + " showPlmn2 =" + odpTemp.mShowPlmn + " spn2 =" + odpTemp.mSpn + " plmn2 =" + odpTemp.mPlmn);
        }
    }

    private void getOnsInVirtualNet(CustPlmnMember cpm, String hplmn, String tempPlmn, String regplmn, OnsDisplayParams odpTemp) {
        if (VirtualNetOnsExtend.isVirtualNetOnsExtend() && this.mGsmPhone.mIccRecords.get() != null) {
            VirtualNetOnsExtend.createVirtualNetByHplmn(hplmn, (IccRecords) this.mGsmPhone.mIccRecords.get());
            if (VirtualNetOnsExtend.getCurrentVirtualNet() != null) {
                String custOns = VirtualNetOnsExtend.getCurrentVirtualNet().getOperatorName();
                Rlog.d(LOG_TAG, "virtual net hplmn=" + hplmn + " regplmn=" + regplmn + " VirtualNetOnsExtend.custOns=" + custOns);
                if (!TextUtils.isEmpty(custOns) && cpm.acquireFromCust(hplmn, getSS(), custOns)) {
                    odpTemp.mShowSpn = cpm.showSpn;
                    odpTemp.mShowPlmn = cpm.showPlmn;
                    odpTemp.mRule = cpm.rule;
                    odpTemp.mPlmn = cpm.judgePlmn(odpTemp.mPlmn);
                    odpTemp.mSpn = cpm.judgeSpn(odpTemp.mSpn);
                    if (1 == cpm.rule && TextUtils.isEmpty(odpTemp.mSpn)) {
                        Rlog.d(LOG_TAG, "want to show spn while spn is null,use plmn instead " + tempPlmn);
                        odpTemp.mSpn = tempPlmn;
                        if (TextUtils.isEmpty(((IccRecords) this.mGsmPhone.mIccRecords.get()).getServiceProviderName())) {
                            odpTemp.mSpn = getDefaultSpn(odpTemp.mSpn, hplmn, regplmn);
                        }
                    }
                    Rlog.d(LOG_TAG, "virtual net showSpn2=" + odpTemp.mShowSpn + " showPlmn2=" + odpTemp.mShowPlmn + " spn2=" + odpTemp.mSpn + " plmn2=" + odpTemp.mPlmn);
                }
            }
        }
    }

    private void getOnsByCfgCust(CustPlmnMember cpm, String hplmn, String tempPlmn, String regplmn, int slotId, OnsDisplayParams odpTemp) {
        int i;
        if (cpm.getCfgCustDisplayParams(hplmn, getSS(), "plmn_spn", slotId)) {
            if (cpm.rule == 0 || cpm.rule == 6) {
                i = odpTemp.mRule;
            } else {
                i = cpm.rule;
            }
            odpTemp.mRule = i;
            odpTemp.mPlmn = "####".equals(cpm.plmn) ? odpTemp.mPlmn : cpm.plmn;
            odpTemp.mSpn = "####".equals(cpm.spn) ? odpTemp.mSpn : cpm.spn;
            odpTemp.mSpn = getSpnFromTempPlmn(cpm, odpTemp.mSpn, tempPlmn, hplmn, regplmn);
            if (4 == cpm.rule) {
                String temPnn = null;
                IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
                if (r != null && !r.isEonsDisabled()) {
                    Rlog.d(LOG_TAG, "getEons():get plmn from SIM card! ");
                    if (updateEons(r)) {
                        temPnn = r.getEons();
                    }
                }
                Rlog.d(LOG_TAG, "temPnn = " + temPnn);
                syncRuleByPlmnAndRule(temPnn, odpTemp);
            }
            if (cpm.rule == 6) {
                getGsmOnsDisplayParamsNitzNamePrior(odpTemp);
            }
            boolean z = true;
            odpTemp.mShowSpn = (odpTemp.mRule & 1) == 1;
            if ((odpTemp.mRule & 2) != 2) {
                z = false;
            }
            odpTemp.mShowPlmn = z;
            Rlog.d(LOG_TAG, "getCfgCustDisplayParams showSpn=" + odpTemp.mShowSpn + " showPlmn=" + odpTemp.mShowPlmn + " spn=" + odpTemp.mSpn + " plmn=" + odpTemp.mPlmn + " rule=" + odpTemp.mRule);
        }
    }

    private OnsDisplayParams getOdpByCust(OnsDisplayParams odp, OnsDisplayParams odpTemp) {
        OnsDisplayParams odp2;
        OnsDisplayParams odpCust = getGsmOnsDisplayParamsBySpecialCust(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        OnsDisplayParams odpForChinaOperator = getGsmOnsDisplayParamsForChinaOperator(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        OnsDisplayParams odpForGeneralOperator = null;
        OnsDisplayParams odpCustbyVirtualNetType = null;
        OnsDisplayParams odpSpnCust = getGsmOnsDisplayParamsBySpnCust(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        OnsDisplayParams odpCustForVideotron = null;
        if (this.mHwCustGsmServiceStateManager != null) {
            odpForGeneralOperator = this.mHwCustGsmServiceStateManager.getGsmOnsDisplayParamsForGlobalOperator(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
            odpCustbyVirtualNetType = this.mHwCustGsmServiceStateManager.getVirtualNetOnsDisplayParams();
            odpCustForVideotron = this.mHwCustGsmServiceStateManager.getGsmOnsDisplayParamsForVideotron(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        }
        if (odpCust != null) {
            odp2 = odpCust;
        } else if (odpSpnCust != null) {
            odp2 = odpSpnCust;
        } else if (odpForChinaOperator != null) {
            odp2 = odpForChinaOperator;
        } else if (odpCustbyVirtualNetType != null) {
            odp2 = odpCustbyVirtualNetType;
        } else if (odpForGeneralOperator != null) {
            odp2 = odpForGeneralOperator;
        } else if (odpCustForVideotron != null) {
            odp2 = odpCustForVideotron;
        } else {
            OnsDisplayParams onsDisplayParams = new OnsDisplayParams(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
            odp2 = onsDisplayParams;
        }
        if (this.mHwCustGsmServiceStateManager != null) {
            odp2 = this.mHwCustGsmServiceStateManager.setOnsDisplayCustomization(odp2, getSS());
        }
        if (this.mGsmPhone.getImsPhone() == null || !this.mGsmPhone.getImsPhone().isWifiCallingEnabled()) {
            return odp2;
        }
        return getOnsDisplayParamsForVoWifi(odp2);
    }

    private void setShowWifiByOdp(OnsDisplayParams odp) {
        this.mCurShowWifi = odp.mShowWifi;
        this.mCurWifi = odp.mWifi;
    }

    private void setSpnAndRuleByOdp(OnsDisplayParams odp) {
        if (TextUtils.isEmpty(odp.mSpn) && 3 == odp.mRule) {
            Rlog.d(LOG_TAG, "Show plmn and spn while spn is null, show plmn only !");
            odp.mShowSpn = false;
            odp.mRule = 2;
        }
    }

    private void setOperatorNameByPlmnOrSpn(OnsDisplayParams odp) {
        String networkNameShow = null;
        if (odp.mShowPlmn) {
            networkNameShow = odp.mPlmn;
        } else if (odp.mShowSpn) {
            networkNameShow = odp.mSpn;
        }
        if (!TextUtils.isEmpty(networkNameShow)) {
            if ((getSS() != null && (getSS().getDataRegState() == 0 || getSS().getVoiceRegState() == 0)) && this.mHwCustGsmServiceStateManager != null && !this.mHwCustGsmServiceStateManager.skipPlmnUpdateFromCust()) {
                Rlog.d(LOG_TAG, "before setprop:" + getSS().getOperatorAlphaLong());
                getSS().setOperatorName(networkNameShow, getSS().getOperatorAlphaShort(), getSS().getOperatorNumeric());
                Rlog.d(LOG_TAG, "after setprop:" + getSS().getOperatorAlphaLong());
            }
        }
    }

    private void syncRuleByPlmnAndRule(String temPnn, OnsDisplayParams odpTemp) {
        Rlog.d(LOG_TAG, "temPnn = " + temPnn);
        boolean pnnEmpty = false;
        if (TextUtils.isEmpty(temPnn)) {
            pnnEmpty = true;
        }
        if (!pnnEmpty || TextUtils.isEmpty(odpTemp.mSpn)) {
            odpTemp.mRule = 2;
            if (!pnnEmpty) {
                odpTemp.mPlmn = temPnn;
                return;
            }
            return;
        }
        Rlog.d(LOG_TAG, "want to show PNN while PNN is null, show SPN instead ");
        odpTemp.mRule = 1;
    }

    private boolean delayUpdateGsmEcctoNoserviceState(ServiceState oldSS, ServiceState newSS) {
        int delayedTime = getEcctoNoserviceStateDelayedTime();
        if (delayedTime <= 0) {
            return false;
        }
        boolean isEcctoNoservice = (oldSS.getVoiceRegState() == 1 && oldSS.getDataRegState() == 1 && oldSS.isEmergencyOnly()) && (newSS.getVoiceRegState() == 1 && newSS.getDataRegState() == 1 && !newSS.isEmergencyOnly());
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "] isEcctoNoservice : " + isEcctoNoservice + ", newMainSlot : " + newMainSlot);
        if (isNeedCancelEccDelayTimer(newSS)) {
            this.mMainSlotEcc = newMainSlot;
            cancelEcctoNoserviceStateDelayTimer();
        } else {
            Rlog.d(LOG_TAG, "[SUB" + this.mPhoneId + "]  hasMessages EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE");
            if (hasMessages(EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE)) {
                return true;
            }
            if (isEcctoNoservice && !this.mRefreshStateEcc) {
                if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
                    Rlog.d(LOG_TAG, "[SUB" + this.mPhoneId + "]cancelDeregisterStateDelayTimer");
                    cancelDeregisterStateDelayTimer();
                }
                Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "] Ecc delay time = " + delayedTime);
                delayEcctoNoserviceStateChange(delayedTime);
                newSS.setStateOutOfService();
                return true;
            }
        }
        this.mRefreshStateEcc = false;
        return false;
    }

    private void cancelEcctoNoserviceStateDelayTimer() {
        if (hasMessages(EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE)) {
            Rlog.d(LOG_TAG, "[SUB" + this.mPhoneId + "] cancelEcctoNoserviceStateDelayTimer");
            removeMessages(EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE);
        }
    }

    private void delayEcctoNoserviceStateChange(int delayedTime) {
        if (!hasMessages(EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE)) {
            Message msg = obtainMessage();
            msg.what = EVENT_DELAY_UPDATE_EMERGENCY_TO_NOSERVICE_DONE;
            sendMessageDelayed(msg, (long) delayedTime);
            Rlog.d(LOG_TAG, "[SUB" + this.mPhoneId + "] EccStateChange timer is running,do nothing");
        }
    }

    private int getEcctoNoserviceStateDelayedTime() {
        return DELAYED_ECC_TO_NOSERVICE_VALUE * 1000;
    }

    private boolean isNeedCancelEccDelayTimer(ServiceState newSS) {
        IccCardConstants.State mExternalState = IccCardConstants.State.UNKNOWN;
        boolean isSubDeactivated = SubscriptionController.getInstance().getSubState(this.mGsmPhone.getPhoneId()) == 0;
        IccCard iccCard = this.mGsmPhone.getIccCard();
        if (iccCard != null) {
            mExternalState = iccCard.getState();
        }
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "] desiredPowerState : " + this.mGsst.getDesiredPowerState() + ", radiostate : " + this.mCi.getRadioState() + ", mRadioOffByDoRecovery : " + this.mGsst.getDoRecoveryTriggerState() + ", isSubDeactivated : " + isSubDeactivated + ", phoneOOS : " + this.mGsmPhone.getOOSFlag() + ", isUserPref4GSlot : " + HwFullNetworkManager.getInstance().isUserPref4GSlot(this.mMainSlotEcc) + ", mExternalState : " + mExternalState);
        return (newSS.getDataRegState() == 1 && newSS.getVoiceRegState() == 1 && (newSS.getVoiceRegState() != 1 || newSS.getDataRegState() != 1 || !newSS.isEmergencyOnly()) && this.mGsst.getDesiredPowerState() && this.mCi.getRadioState() != CommandsInterface.RadioState.RADIO_OFF && !this.mGsst.getDoRecoveryTriggerState() && !isCardInvalid(isSubDeactivated, this.mGsmPhone.getSubId()) && HwFullNetworkManager.getInstance().isUserPref4GSlot(this.mMainSlotEcc) && !this.mGsmPhone.getOOSFlag() && newSS.getDataRegState() != 3 && mExternalState != IccCardConstants.State.PUK_REQUIRED) ? false : true;
    }
}
