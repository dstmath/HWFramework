package com.android.internal.telephony.gsm;

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
import android.os.SystemProperties;
import android.provider.HwTelephony.NumMatchs;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.CustPlmnMember;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwAddonTelephonyFactory;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPlmnActConcat;
import com.android.internal.telephony.HwServiceStateManager;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.telephony.HwSignalStrength.SignalThreshold;
import com.android.internal.telephony.HwSignalStrength.SignalType;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.OnsDisplayParams;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PlmnConstants;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.ServiceStateTrackerUtils;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.VirtualNet;
import com.android.internal.telephony.VirtualNetOnsExtend;
import com.android.internal.telephony.dataconnection.ApnReminder;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimUtils;
import huawei.cust.HwCustUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

public class HwGsmServiceStateManager extends HwServiceStateManager {
    private static final int AIRPLANE_MODE_ON = 1;
    private static final int CDMA_LEVEL = 8;
    private static final String CHINAMOBILE_MCCMNC = "46000;46002;46007;46008;46004";
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final String CHINA_TELECOM_SPN = "%E4%B8%AD%E5%9B%BD%E7%94%B5%E4%BF%A1";
    public static final String CLOUD_OTA_DPLMN_UPDATE = "cloud.ota.dplmn.UPDATE";
    public static final String CLOUD_OTA_MCC_UPDATE = "cloud.ota.mcc.UPDATE";
    public static final String CLOUD_OTA_PERMISSION = "huawei.permission.RECEIVE_CLOUD_OTA_UPDATA";
    private static final String EMERGENCY_PLMN = Resources.getSystem().getText(17039939).toString();
    private static final int EVDO_LEVEL = 16;
    private static final int EVENT_CA_STATE_CHANGED = 104;
    private static final int EVENT_CRR_CONN = 152;
    private static final int EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH = 103;
    private static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 102;
    private static final int EVENT_MCC_CHANGED = 154;
    private static final int EVENT_NETWORK_REJECTED_CASE = 105;
    private static final int EVENT_PLMN_SELINFO = 151;
    private static final int EVENT_POLL_LOCATION_INFO = 64;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 101;
    private static final int GSM_LEVEL = 1;
    private static final int GSM_STRENGTH_POOR_STD = -109;
    private static final boolean KEEP_3GPLUS_HPLUS = SystemProperties.getBoolean("ro.config.keep_3gplus_hplus", false);
    private static final String LOG_TAG = "HwGsmServiceStateManager";
    private static final int LTE_LEVEL = 4;
    private static final int LTE_RSSNR_POOR_STD = SystemProperties.getInt("ro.lte.rssnrpoorstd", L_RSSNR_POOR_STD);
    private static final int LTE_RSSNR_UNKOUWN_STD = 99;
    private static final int LTE_STRENGTH_POOR_STD = SystemProperties.getInt("ro.lte.poorstd", L_STRENGTH_POOR_STD);
    private static final int LTE_STRENGTH_UNKOUWN_STD = -44;
    private static final int L_RSSNR_POOR_STD = -5;
    private static final int L_STRENGTH_POOR_STD = -125;
    private static final int MCC_LENGTH = 3;
    private static final int NETWORK_MODE_GSM_UMTS = 3;
    private static final String NO_SERVICE_PLMN = Resources.getSystem().getText(17040278).toString();
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
    private static final int WCDMA_STRENGTH_POOR_STD = SystemProperties.getInt("ro.wcdma.poorstd", W_STRENGTH_POOR_STD);
    private static final int W_ECIO_POOR_STD = -17;
    private static final int W_STRENGTH_POOR_STD = -112;
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static final int mDelayDuringTime = SystemProperties.getInt("ro.signalsmooth.delaytimer", VALUE_DELAY_DURING_TIME);
    private static HwSignalStrength mHwSigStr = HwSignalStrength.getInstance();
    private final boolean FEATURE_SIGNAL_DUALPARAM = SystemProperties.getBoolean("signal.dualparam", false);
    private int lastCid = -1;
    private int lastLac = -1;
    private int lastType = -1;
    CommandsInterface mCi;
    private CloudOtaBroadcastReceiver mCloudOtaBroadcastReceiver = new CloudOtaBroadcastReceiver(this, null);
    private Context mContext;
    private ContentResolver mCr;
    private DoubleSignalStrength mDoubleSignalStrength;
    private GsmCdmaPhone mGsmPhone;
    private ServiceStateTracker mGsst;
    private HwCustGsmServiceStateManager mHwCustGsmServiceStateManager;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = "";
            if (intent != null) {
                action = intent.getAction();
                int slotId;
                if ("android.intent.action.refreshapn".equals(action)) {
                    Rlog.i(HwGsmServiceStateManager.LOG_TAG, "refresh apn worked,updateSpnDisplay.");
                    HwGsmServiceStateManager.this.mGsst.updateSpnDisplay();
                } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    String simState = (String) intent.getExtra("ss");
                    slotId = intent.getIntExtra("slot", -1000);
                    Rlog.i(HwGsmServiceStateManager.LOG_TAG, "simState = " + simState + " slotId = " + slotId);
                    if ("ABSENT".equals(simState)) {
                        VirtualNet.removeVirtualNet(slotId);
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "sim absent, reset");
                        if (-1 != SystemProperties.getInt("gsm.sim.updatenitz", -1)) {
                            SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                        }
                    } else if ("LOADED".equals(simState) && slotId == HwGsmServiceStateManager.this.mPhoneId) {
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "after simrecords loaded,updateSpnDisplay for virtualnet ons.");
                        HwGsmServiceStateManager.this.mGsst.updateSpnDisplay();
                    }
                } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(action)) {
                    slotId = intent.getIntExtra("subscription", -1);
                    int intValue = intent.getIntExtra("intContent", 0);
                    String column = intent.getStringExtra("columnName");
                    Rlog.i(HwGsmServiceStateManager.LOG_TAG, "Received ACTION_SUBINFO_CONTENT_CHANGE on slotId: " + slotId + " for " + column + ", intValue: " + intValue);
                    Rlog.i(HwGsmServiceStateManager.LOG_TAG, "PROPERTY_GSM_SIM_UPDATE_NITZ=" + SystemProperties.getInt("gsm.sim.updatenitz", -1));
                    if ("sub_state".equals(column) && -1 != slotId && intValue == 0 && slotId == SystemProperties.getInt("gsm.sim.updatenitz", -1)) {
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "reset PROPERTY_GSM_SIM_UPDATE_NITZ when the sim is inactive and the time zone get from the sim' NITZ.");
                        SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                    }
                } else if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(action)) {
                    Rlog.d(HwGsmServiceStateManager.LOG_TAG, "[SLOT" + HwGsmServiceStateManager.this.mPhoneId + "]CardState: " + intent.getIntExtra("newSubState", -1) + "IsMphone: " + (HwGsmServiceStateManager.this.mPhoneId == intent.getIntExtra("phone", 0)));
                    if (intent.getIntExtra("operationResult", 1) == 0 && HwGsmServiceStateManager.this.mPhoneId == intent.getIntExtra("phone", 0) && HwGsmServiceStateManager.this.hasMessages(HwGsmServiceStateManager.EVENT_DELAY_UPDATE_REGISTER_STATE_DONE) && intent.getIntExtra("newSubState", -1) == 0) {
                        HwGsmServiceStateManager.this.cancelDeregisterStateDelayTimer();
                        HwGsmServiceStateManager.this.mGsst.pollState();
                    }
                } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    boolean airplaneMode = Global.getInt(HwGsmServiceStateManager.this.mCr, "airplane_mode_on", 0) == 1;
                    Rlog.d(HwGsmServiceStateManager.LOG_TAG, "airplaneMode: " + airplaneMode);
                    if (airplaneMode) {
                        SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                    }
                } else if (HwGsmServiceStateManager.this.mHwCustGsmServiceStateManager != null) {
                    HwGsmServiceStateManager.this.mRac = HwGsmServiceStateManager.this.mHwCustGsmServiceStateManager.handleBroadcastReceived(context, intent, HwGsmServiceStateManager.this.mRac);
                }
            }
        }
    };
    private SignalStrength mModemSignalStrength;
    private int mOldCAstate = 0;
    private DoubleSignalStrength mOldDoubleSignalStrength;
    private int mPhoneId = 0;
    private int mRac = -1;
    private String oldRplmn = "";
    private int rejNum = 0;
    private String rplmn = "";

    private class CloudOtaBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ CloudOtaBroadcastReceiver(HwGsmServiceStateManager this$0, CloudOtaBroadcastReceiver -this1) {
            this();
        }

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
        private double mDoubleWcdmaEcio;
        private double mDoubleWcdmaRscp;
        private double mOldDoubleGsmSS;
        private double mOldDoubleLteRsrp;
        private double mOldDoubleLteRssnr;
        private double mOldDoubleWcdmaEcio;
        private double mOldDoubleWcdmaRscp;
        private int mTechState;

        public DoubleSignalStrength(SignalStrength ss) {
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
            this.mOldDoubleLteRsrp = this.mDoubleLteRsrp;
            this.mOldDoubleLteRssnr = this.mDoubleLteRssnr;
            this.mOldDoubleWcdmaRscp = this.mDoubleWcdmaRscp;
            this.mOldDoubleWcdmaEcio = this.mDoubleWcdmaEcio;
            this.mOldDoubleGsmSS = this.mDoubleGsmSS;
            this.mDelayTime = 0;
        }

        public DoubleSignalStrength(DoubleSignalStrength doubleSS) {
            this.mDoubleLteRsrp = doubleSS.mDoubleLteRsrp;
            this.mDoubleLteRssnr = doubleSS.mDoubleLteRssnr;
            this.mDoubleWcdmaRscp = doubleSS.mDoubleWcdmaRscp;
            this.mDoubleWcdmaEcio = doubleSS.mDoubleWcdmaEcio;
            this.mDoubleGsmSS = doubleSS.mDoubleGsmSS;
            this.mTechState = doubleSS.mTechState;
            this.mOldDoubleLteRsrp = doubleSS.mDoubleLteRsrp;
            this.mOldDoubleLteRssnr = doubleSS.mDoubleLteRssnr;
            this.mOldDoubleWcdmaRscp = doubleSS.mDoubleWcdmaRscp;
            this.mOldDoubleWcdmaEcio = doubleSS.mDoubleWcdmaEcio;
            this.mOldDoubleGsmSS = doubleSS.mDoubleGsmSS;
            this.mDelayTime = doubleSS.mDelayTime;
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

        public boolean processLteRsrpAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldRsrp = oldDoubleSS.getDoubleLteRsrp();
            double modemLteRsrp = (double) modemSS.getLteRsrp();
            this.mOldDoubleLteRsrp = oldRsrp;
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]--old : " + oldRsrp + "; instant new : " + modemLteRsrp);
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
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]modem : " + modemLteRsrp + "; old : " + oldRsrp + "; new : " + this.mDoubleLteRsrp);
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
            int needUpdate = 0;
            if (oldDoubleSS == null) {
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]proccess oldDoubleSS is null");
                return;
            }
            if ((this.mTechState & 4) != 0) {
                needUpdate = processLteRsrpAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
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
            if ((this.mTechState & 1) != 0) {
                needUpdate |= processGsmSignalStrengthAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
            }
            if ((TelephonyManager.getDefault().getCurrentPhoneType(HwGsmServiceStateManager.this.mPhoneId) == 1) || !HwGsmServiceStateManager.this.hasMessages(HwGsmServiceStateManager.EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH)) {
                setFakeSignalStrengthForSlowDescend(this, newSS);
                HwGsmServiceStateManager.this.mGsst.setSignalStrength(newSS);
                if (needUpdate != 0) {
                    HwGsmServiceStateManager.this.sendMessageDelayUpdateSingalStrength(this.mDelayTime);
                }
                return;
            }
            HwGsmServiceStateManager.this.removeMessages(HwGsmServiceStateManager.EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH);
        }

        private void setFakeSignalStrengthForSlowDescend(DoubleSignalStrength oldDoubleSS, SignalStrength newSS) {
            int oldLevel;
            int newLevel;
            int diffLevel;
            SignalThreshold signalThreshold;
            int lowerLevel;
            this.mDelayTime = 0;
            if ((this.mTechState & 4) != 0) {
                oldLevel = HwGsmServiceStateManager.mHwSigStr.getLevel(SignalType.LTE, (int) oldDoubleSS.getOldDoubleLteRsrp(), (int) oldDoubleSS.getOldDoubleLteRssnr());
                newLevel = HwGsmServiceStateManager.mHwSigStr.getLevel(SignalType.LTE, newSS.getLteRsrp(), newSS.getLteRssnr());
                diffLevel = oldLevel - newLevel;
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]LTE oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1) {
                    signalThreshold = HwGsmServiceStateManager.mHwSigStr.getSignalThreshold(SignalType.LTE);
                    if (signalThreshold != null) {
                        lowerLevel = oldLevel - 1;
                        int lteRsrp = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                        if (-1 != lteRsrp) {
                            this.mDoubleLteRsrp = (double) lteRsrp;
                            newSS.setLteRsrp(lteRsrp);
                            this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                        }
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]LTE lowerLevel: " + lowerLevel + ", lteRsrp: " + lteRsrp);
                        if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                            int lteRssnr = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true);
                            if (-1 != lteRssnr) {
                                this.mDoubleLteRssnr = (double) lteRssnr;
                                newSS.setLteRssnr(lteRssnr);
                                this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                            }
                        }
                    }
                }
            }
            if ((this.mTechState & 2) != 0) {
                oldLevel = HwGsmServiceStateManager.mHwSigStr.getLevel(SignalType.UMTS, (int) oldDoubleSS.getOldDoubleWcdmaRscp(), (int) oldDoubleSS.getOldDoubleWcdmaEcio());
                newLevel = HwGsmServiceStateManager.mHwSigStr.getLevel(SignalType.UMTS, newSS.getWcdmaRscp(), newSS.getWcdmaEcio());
                diffLevel = oldLevel - newLevel;
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]UMTS oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1) {
                    signalThreshold = HwGsmServiceStateManager.mHwSigStr.getSignalThreshold(SignalType.UMTS);
                    if (signalThreshold != null) {
                        lowerLevel = oldLevel - 1;
                        int wcdmaRscp = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                        if (-1 != wcdmaRscp) {
                            this.mDoubleWcdmaRscp = (double) wcdmaRscp;
                            newSS.setWcdmaRscp(wcdmaRscp);
                            this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                        }
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]UMTS lowerLevel: " + lowerLevel + ", wcdmaRscp: " + wcdmaRscp);
                        if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                            int wcdmaEcio = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true);
                            if (-1 != wcdmaEcio) {
                                this.mDoubleWcdmaEcio = (double) wcdmaEcio;
                                newSS.setWcdmaEcio(wcdmaEcio);
                                this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                            }
                        }
                    }
                }
            }
            if ((this.mTechState & 1) != 0) {
                oldLevel = HwGsmServiceStateManager.mHwSigStr.getLevel(SignalType.GSM, (int) oldDoubleSS.getOldDoubleGsmSignalStrength(), 255);
                newLevel = HwGsmServiceStateManager.mHwSigStr.getLevel(SignalType.GSM, newSS.getGsmSignalStrength(), 255);
                diffLevel = oldLevel - newLevel;
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]GSM oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1) {
                    signalThreshold = HwGsmServiceStateManager.mHwSigStr.getSignalThreshold(SignalType.GSM);
                    if (signalThreshold != null) {
                        lowerLevel = oldLevel - 1;
                        int gsmSS = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                        if (-1 != gsmSS) {
                            this.mDoubleGsmSS = (double) gsmSS;
                            newSS.setGsmSignalStrength(gsmSS);
                            this.mDelayTime = HwGsmServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                        }
                        Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]GSM lowerLevel: " + lowerLevel + ", gsmSS: " + gsmSS);
                    }
                }
            }
        }
    }

    public HwGsmServiceStateManager(ServiceStateTracker sst, GsmCdmaPhone gsmPhone) {
        super(sst, gsmPhone);
        this.mGsst = sst;
        this.mGsmPhone = gsmPhone;
        this.mContext = gsmPhone.getContext();
        this.mCr = this.mContext.getContentResolver();
        this.mPhoneId = gsmPhone.getPhoneId();
        this.mCi = gsmPhone.mCi;
        this.mCi.registerForRplmnsStateChanged(this, EVENT_RPLMNS_STATE_CHANGED, null);
        sendMessage(obtainMessage(EVENT_RPLMNS_STATE_CHANGED));
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]constructor init");
        this.mHwCustGsmServiceStateManager = (HwCustGsmServiceStateManager) HwCustUtils.createObj(HwCustGsmServiceStateManager.class, new Object[]{sst, gsmPhone});
        addBroadCastReceiver();
        this.mMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        this.mCi.registerForCaStateChanged(this, EVENT_CA_STATE_CHANGED, null);
        this.mCi.registerForCrrConn(this, EVENT_CRR_CONN, null);
        this.mCi.setOnRegPLMNSelInfo(this, EVENT_PLMN_SELINFO, null);
        this.mGsmPhone.registerForMccChanged(this, EVENT_MCC_CHANGED, null);
        registerCloudOtaBroadcastReceiver();
        if (PS_CLEARCODE || SHOW_REJ_INFO_KT) {
            this.mCi.setOnNetReject(this, EVENT_NETWORK_REJECTED_CASE, null);
        }
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
        int isCTCard_Reg_GSM;
        if (this.mHwCustGsmServiceStateManager != null) {
            this.mHwCustGsmServiceStateManager.storeModemRoamingStatus(roaming);
        }
        if (roaming) {
            Object hplmn = null;
            if (this.mGsmPhone.mIccRecords.get() != null) {
                hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            }
            String regplmn = getNewSS().getOperatorNumeric();
            String str = null;
            if (getNoRoamingByMcc(getNewSS())) {
                roaming = false;
            }
            try {
                str = System.getString(this.mContext.getContentResolver(), "reg_plmn_custom");
                Rlog.d("HwGsmServiceStateTracker", "handlePollStateResult plmnCustomString = " + str);
            } catch (Exception e) {
                Rlog.e("HwGsmServiceStateTracker", "Exception when got name value", e);
            }
            if (str != null) {
                String[] regplmnCustomArray = str.split(";");
                if (TextUtils.isEmpty(hplmn) || (TextUtils.isEmpty(regplmn) ^ 1) == 0) {
                    roaming = false;
                } else {
                    for (String split : regplmnCustomArray) {
                        String[] regplmnCustomArrEleBuf = split.split(",");
                        if (containsPlmn(hplmn, regplmnCustomArrEleBuf) ? containsPlmn(regplmn, regplmnCustomArrEleBuf) : false) {
                            roaming = false;
                            break;
                        }
                    }
                }
            }
        }
        Rlog.d("GsmServiceStateTracker", "roaming = " + roaming);
        if (!HwTelephonyManagerInner.getDefault().isCTSimCard(this.mGsmPhone.getPhoneId()) || getNewSS().getState() != 0) {
            isCTCard_Reg_GSM = 0;
        } else if (ServiceState.isGsm(getNewSS().getRilVoiceRadioTechnology())) {
            isCTCard_Reg_GSM = ServiceState.isLte(getNewSS().getRilVoiceRadioTechnology()) ^ 1;
        } else {
            isCTCard_Reg_GSM = 0;
        }
        if (isCTCard_Reg_GSM != 0) {
            roaming = true;
            Rlog.d("GsmServiceStateTracker", "When CT card register in GSM/UMTS, it always should be roaming" + true);
        }
        if (this.mHwCustGsmServiceStateManager != null) {
            roaming = this.mHwCustGsmServiceStateManager.setRoamingStateForOperatorCustomization(getNewSS(), roaming);
            Rlog.d("GsmServiceStateTracker", "roaming customization for MCC 302 roaming=" + roaming);
        }
        roaming = getGsmRoamingSpecialCustByNetType(getGsmRoamingCustByIMSIStart(roaming));
        if (this.mHwCustGsmServiceStateManager != null) {
            return this.mHwCustGsmServiceStateManager.checkIsInternationalRoaming(roaming, getNewSS());
        }
        return roaming;
    }

    private boolean getGsmRoamingSpecialCustByNetType(boolean roaming) {
        if (this.mGsmPhone.mIccRecords.get() != null) {
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            String regplmn = getNewSS().getOperatorNumeric();
            int netType = getNewSS().getVoiceNetworkType();
            TelephonyManager.getDefault();
            int netClass = TelephonyManager.getNetworkClass(netType);
            Rlog.d("HwGsmServiceStateTracker", "getGsmRoamingSpecialCustByNetType: hplmn=" + hplmn + " regplmn=" + regplmn + " netType=" + netType + " netClass=" + netClass);
            if ("50218".equals(hplmn) && "50212".equals(regplmn)) {
                if (1 == netClass || 2 == netClass) {
                    roaming = false;
                }
                if (3 == netClass) {
                    roaming = true;
                }
            }
        }
        Rlog.d("HwGsmServiceStateTracker", "getGsmRoamingSpecialCustByNetType: roaming = " + roaming);
        return roaming;
    }

    public String getPlmn() {
        if (getCombinedRegState(getSS()) != 0) {
            return null;
        }
        ApnReminder apnReminder;
        String operatorNumeric = getSS().getOperatorNumeric();
        String data = null;
        try {
            data = System.getString(this.mCr, "plmn");
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception when got data value", e);
        }
        PlmnConstants plmnConstants = new PlmnConstants(data);
        String plmnValue = plmnConstants.getPlmnValue(operatorNumeric, Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
        if (plmnValue == null) {
            String DEFAULT_PLMN_LANG_EN = "en_us";
            plmnValue = plmnConstants.getPlmnValue(operatorNumeric, "en_us");
            log("get default en_us plmn name:" + plmnValue);
        }
        int slotId = this.mGsmPhone.getPhoneId();
        Rlog.d("HwGsmServiceStateTracker", "slotId = " + slotId);
        String hplmn = null;
        String imsi = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            imsi = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getIMSI();
        }
        Rlog.d("HwGsmServiceStateTracker", "hplmn = " + hplmn);
        boolean isUseVirtualName = this.mHwCustGsmServiceStateManager != null ? this.mHwCustGsmServiceStateManager.notUseVirtualName(imsi) : false;
        boolean roaming = getSS().getRoaming();
        boolean isMatchRoamingRule = this.mHwCustGsmServiceStateManager != null ? this.mHwCustGsmServiceStateManager.iscustRoamingRuleAffect(roaming) : false;
        if (isUseVirtualName || isMatchRoamingRule) {
            Rlog.d("HwGsmServiceStateTracker", "passed the Virtualnet cust");
        } else {
            plmnValue = HwTelephonyFactory.getHwPhoneManager().getVirtualNetOperatorName(plmnValue, roaming, hasNitzOperatorName(slotId), slotId, hplmn);
            Rlog.d("HwGsmServiceStateTracker", "VirtualNetName = " + plmnValue);
        }
        if (isMultiSimEnabled) {
            apnReminder = ApnReminder.getInstance(this.mContext, slotId);
        } else {
            apnReminder = ApnReminder.getInstance(this.mContext);
        }
        boolean hasNitzOperatorName = (apnReminder.isPopupApnSettingsEmpty() || (getSS().getRoaming() ^ 1) == 0 || (hasNitzOperatorName(slotId) ^ 1) == 0 || hplmn == null) ? false : true;
        if (hasNitzOperatorName) {
            int apnId = getPreferedApnId();
            if (-1 != apnId) {
                plmnValue = apnReminder.getOnsNameByPreferedApn(apnId, plmnValue);
                Rlog.d("HwGsmServiceStateTracker", "apnReminder plmnValue = " + plmnValue);
            } else {
                plmnValue = null;
            }
        }
        plmnValue = getGsmOnsDisplayPlmnByAbbrevPriority(getGsmOnsDisplayPlmnByPriority(plmnValue, slotId), slotId);
        if (TextUtils.isEmpty(plmnValue)) {
            plmnValue = getVirtualNetPlmnValue(operatorNumeric, hplmn, imsi, getEons(getSS().getOperatorAlphaLong()));
        } else {
            getSS().setOperatorAlphaLong(plmnValue);
        }
        Rlog.d("GsmServiceStateTracker", "plmnValue = " + plmnValue);
        if (HwPlmnActConcat.needPlmnActConcat()) {
            plmnValue = HwPlmnActConcat.getPlmnActConcat(plmnValue, getSS());
        }
        return plmnValue;
    }

    public String getGsmOnsDisplayPlmnByAbbrevPriority(String custPlmnValue, int slotId) {
        String result = custPlmnValue;
        String plmnAbbrev = null;
        String custPlmn = System.getString(this.mCr, "hw_plmn_abbrev");
        if (TextUtils.isEmpty(custPlmn)) {
            return custPlmnValue;
        }
        if (this.mGsmPhone.mIccRecords.get() != null) {
            CustPlmnMember cpm = CustPlmnMember.getInstance();
            if (cpm.acquireFromCust(((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric(), getSS(), custPlmn)) {
                plmnAbbrev = cpm.plmn;
                Rlog.d(LOG_TAG, " plmn2 =" + plmnAbbrev);
            }
        }
        if (TextUtils.isEmpty(plmnAbbrev)) {
            return custPlmnValue;
        }
        if (hasNitzOperatorName(slotId)) {
            result = getEons(getSS().getOperatorAlphaLong());
        } else {
            result = plmnAbbrev;
            result = getEons(plmnAbbrev);
        }
        Rlog.d("getGsmOnsDisplayPlmnByAbbrevPriority, ", "plmnValueByAbbrevPriority = " + result + " slotId = " + slotId + " PlmnValue = " + custPlmnValue);
        return result;
    }

    public String getGsmOnsDisplayPlmnByPriority(String custPlmnValue, int slotId) {
        boolean HW_NETWORK_SIM_UE_PRIORITY = SystemProperties.getBoolean("ro.config.net_sim_ue_pri", false);
        boolean CUST_HPLMN_REGPLMN = enablePlmnByNetSimUePriority();
        if (!HW_NETWORK_SIM_UE_PRIORITY && (CUST_HPLMN_REGPLMN ^ 1) != 0) {
            return custPlmnValue;
        }
        String result = custPlmnValue;
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        CharSequence spnSim = null;
        if (r != null) {
            spnSim = r.getServiceProviderName();
        }
        CharSequence result2;
        if (hasNitzOperatorName(slotId)) {
            result = getEons(custPlmnValue);
            if (CUST_HPLMN_REGPLMN && (TextUtils.isEmpty(spnSim) ^ 1) != 0) {
                result2 = spnSim;
            }
            result = getSS().getOperatorAlphaLong();
        } else {
            result = getSS().getOperatorAlphaLong();
            if (custPlmnValue != null) {
                result = custPlmnValue;
            }
            result = getEons(result);
            if (CUST_HPLMN_REGPLMN && (TextUtils.isEmpty(spnSim) ^ 1) != 0) {
                result2 = spnSim;
            }
        }
        Rlog.d("getGsmOnsDisplayPlmnByPriority, ", "plmnValue = " + result + " slotId = " + slotId + " custPlmnValue = " + custPlmnValue);
        return result;
    }

    public boolean enablePlmnByNetSimUePriority() {
        Object hplmn = null;
        Object regplmn = null;
        boolean cust_hplmn_regplmn = false;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        String custNetSimUePriority = System.getString(this.mCr, "hw_net_sim_ue_pri");
        if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(regplmn) && !TextUtils.isEmpty(custNetSimUePriority)) {
            for (String mccmnc_item : custNetSimUePriority.split(";")) {
                String[] mccmncs = mccmnc_item.split(",");
                if (hplmn.equals(mccmncs[0]) && regplmn.equals(mccmncs[1])) {
                    cust_hplmn_regplmn = true;
                    break;
                }
            }
        } else {
            cust_hplmn_regplmn = false;
            Rlog.d(LOG_TAG, "enablePlmnByNetSimUePriority() failed, custNetSimUePriority or hplmm or regplmn is null or empty string");
        }
        Rlog.d(LOG_TAG, " cust_hplmn_equal_regplmn = " + cust_hplmn_regplmn);
        return cust_hplmn_regplmn;
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
            Object spnSim = null;
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
        if (isMultiSimEnabled) {
            int slotId = this.mGsmPhone.getPhoneId();
            cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId), new String[]{"_id", NumMatchs.NAME, "apn"}, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        } else {
            cursor = this.mContext.getContentResolver().query(PREFERAPN_NO_UPDATE_URI, new String[]{"_id", NumMatchs.NAME, "apn"}, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        }
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String apnName = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            Rlog.d("HwGsmServiceStateTracker", "getPreferedApnId: " + apnId + ", apn: " + apnName + ", name: " + cursor.getString(cursor.getColumnIndexOrThrow(NumMatchs.NAME)));
        }
        if (cursor != null) {
            cursor.close();
        }
        return apnId;
    }

    private boolean isChinaMobileMccMnc() {
        CharSequence hplmn = null;
        CharSequence regplmn = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        String[] mccMncList = CHINAMOBILE_MCCMNC.split(";");
        boolean isRegplmnCMCC = false;
        boolean isHplmnCMCC = false;
        if (TextUtils.isEmpty(regplmn) || (TextUtils.isEmpty(hplmn) ^ 1) == 0) {
            return false;
        }
        for (int i = 0; i < mccMncList.length; i++) {
            if (mccMncList[i].equals(regplmn)) {
                isRegplmnCMCC = true;
            }
            if (mccMncList[i].equals(hplmn)) {
                isHplmnCMCC = true;
            }
        }
        if (!isRegplmnCMCC) {
            isHplmnCMCC = false;
        }
        return isHplmnCMCC;
    }

    public OnsDisplayParams getOnsDisplayParamsHw(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        String tempPlmn = plmn;
        OnsDisplayParams odp = getOnsDisplayParamsBySpnOnly(showSpn, showPlmn, rule, plmn, spn);
        showSpn = odp.mShowSpn;
        showPlmn = odp.mShowPlmn;
        rule = odp.mRule;
        plmn = odp.mPlmn;
        spn = odp.mSpn;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            CustPlmnMember cpm = CustPlmnMember.getInstance();
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumericEx(this.mCr, "hw_ons_hplmn_ex");
            String regplmn = getSS().getOperatorNumeric();
            String custSpn = System.getString(this.mCr, "hw_plmn_spn");
            if (custSpn != null) {
                Rlog.d(LOG_TAG, "custSpn length =" + custSpn.length());
            }
            if (cpm.acquireFromCust(hplmn, getSS(), custSpn)) {
                showSpn = cpm.judgeShowSpn(showSpn);
                if (cpm.rule != 0) {
                    showPlmn = cpm.showPlmn;
                }
                if (cpm.rule != 0) {
                    rule = cpm.rule;
                }
                plmn = cpm.judgePlmn(plmn);
                spn = cpm.judgeSpn(spn);
                if (1 == cpm.rule && TextUtils.isEmpty(spn)) {
                    Rlog.d(LOG_TAG, " want to show spn while spn is null,use plmn instead " + tempPlmn);
                    spn = tempPlmn;
                    if (TextUtils.isEmpty(((IccRecords) this.mGsmPhone.mIccRecords.get()).getServiceProviderName())) {
                        spn = getDefaultSpn(tempPlmn, hplmn, regplmn);
                    }
                }
                if (4 == cpm.rule) {
                    boolean pnnEmpty = false;
                    CharSequence temPnn = null;
                    IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
                    if (!(r == null || (r.isEonsDisabled() ^ 1) == 0)) {
                        Rlog.d(LOG_TAG, "getEons():get plmn from SIM card! ");
                        if (updateEons(r)) {
                            temPnn = r.getEons();
                        }
                    }
                    Rlog.d(LOG_TAG, "temPnn = " + temPnn);
                    if (TextUtils.isEmpty(temPnn)) {
                        pnnEmpty = true;
                    }
                    if (!pnnEmpty || (TextUtils.isEmpty(spn) ^ 1) == 0) {
                        rule = 2;
                        if (!pnnEmpty) {
                            CharSequence plmn2 = temPnn;
                        }
                    } else {
                        Rlog.d(LOG_TAG, "want to show PNN while PNN is null, show SPN instead ");
                        rule = 1;
                    }
                    showSpn = (rule & 1) == 1;
                    showPlmn = (rule & 2) == 2;
                }
                Rlog.d(LOG_TAG, "showSpn2 =" + showSpn + " showPlmn2 =" + showPlmn + " spn2 =" + spn + " plmn2 =" + plmn);
            }
            if (VirtualNetOnsExtend.isVirtualNetOnsExtend() && this.mGsmPhone.mIccRecords.get() != null) {
                VirtualNetOnsExtend.createVirtualNetByHplmn(hplmn, (IccRecords) this.mGsmPhone.mIccRecords.get());
                if (VirtualNetOnsExtend.getCurrentVirtualNet() != null) {
                    String custOns = VirtualNetOnsExtend.getCurrentVirtualNet().getOperatorName();
                    Rlog.d(LOG_TAG, "hplmn=" + hplmn + " regplmn=" + regplmn + " VirtualNetOnsExtend.custOns=" + custOns);
                    if (!TextUtils.isEmpty(custOns) && cpm.acquireFromCust(hplmn, getSS(), custOns)) {
                        showSpn = cpm.showSpn;
                        showPlmn = cpm.showPlmn;
                        rule = cpm.rule;
                        plmn = cpm.judgePlmn(plmn);
                        spn = cpm.judgeSpn(spn);
                        if (1 == cpm.rule && TextUtils.isEmpty(spn)) {
                            Rlog.d(LOG_TAG, "want to show spn while spn is null,use plmn instead " + tempPlmn);
                            spn = tempPlmn;
                            if (TextUtils.isEmpty(((IccRecords) this.mGsmPhone.mIccRecords.get()).getServiceProviderName())) {
                                spn = getDefaultSpn(tempPlmn, hplmn, regplmn);
                            }
                        }
                        Rlog.d(LOG_TAG, "showSpn2=" + showSpn + " showPlmn2=" + showPlmn + " spn2=" + spn + " plmn2=" + plmn);
                    }
                }
            }
        }
        OnsDisplayParams odpCust = getGsmOnsDisplayParamsBySpecialCust(showSpn, showPlmn, rule, plmn, spn);
        OnsDisplayParams odpForChinaOperator = getGsmOnsDisplayParamsForChinaOperator(showSpn, showPlmn, rule, plmn, spn);
        OnsDisplayParams odpForGeneralOperator = null;
        OnsDisplayParams odpCustbyVirtualNetType = null;
        OnsDisplayParams odpSpnCust = getGsmOnsDisplayParamsBySpnCust(showSpn, showPlmn, rule, plmn, spn);
        OnsDisplayParams odpCustForVideotron = null;
        if (this.mHwCustGsmServiceStateManager != null) {
            odpForGeneralOperator = this.mHwCustGsmServiceStateManager.getGsmOnsDisplayParamsForGlobalOperator(showSpn, showPlmn, rule, plmn, spn);
            odpCustbyVirtualNetType = this.mHwCustGsmServiceStateManager.getVirtualNetOnsDisplayParams();
            odpCustForVideotron = this.mHwCustGsmServiceStateManager.getGsmOnsDisplayParamsForVideotron(showSpn, showPlmn, rule, plmn, spn);
        }
        if (odpCust != null) {
            odp = odpCust;
        } else if (odpSpnCust != null) {
            odp = odpSpnCust;
        } else if (odpForChinaOperator != null) {
            odp = odpForChinaOperator;
        } else if (odpCustbyVirtualNetType != null) {
            odp = odpCustbyVirtualNetType;
        } else if (odpForGeneralOperator != null) {
            odp = odpForGeneralOperator;
        } else if (odpCustForVideotron != null) {
            odp = odpCustForVideotron;
        } else {
            odp = new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
        }
        if (this.mHwCustGsmServiceStateManager != null) {
            odp = this.mHwCustGsmServiceStateManager.setOnsDisplayCustomization(odp, getSS());
        }
        if (this.mGsmPhone.getImsPhone() != null && this.mGsmPhone.getImsPhone().isWifiCallingEnabled()) {
            odp = getOnsDisplayParamsForVoWifi(odp);
        }
        this.mCurShowWifi = odp.mShowWifi;
        this.mCurWifi = odp.mWifi;
        Object networkNameShow = null;
        if (odp.mShowPlmn) {
            networkNameShow = odp.mPlmn;
        } else if (odp.mShowSpn) {
            networkNameShow = odp.mSpn;
        }
        if (!(TextUtils.isEmpty(networkNameShow) || getSS() == null || ((getSS().getDataRegState() != 0 && getSS().getVoiceRegState() != 0) || this.mHwCustGsmServiceStateManager == null || (this.mHwCustGsmServiceStateManager.skipPlmnUpdateFromCust() ^ 1) == 0))) {
            Rlog.d(LOG_TAG, "before setprop:" + getSS().getOperatorAlphaLong());
            getSS().setOperatorName(networkNameShow, getSS().getOperatorAlphaShort(), getSS().getOperatorNumeric());
            Rlog.d(LOG_TAG, "after setprop:" + getSS().getOperatorAlphaLong());
        }
        return odp;
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
            if ("732130".equals(hplmn) && (TextUtils.isEmpty(spnSim) ^ 1) != 0 && ("732103".equals(regPlmn) || "732111".equals(regPlmn) || "732123".equals(regPlmn) || "732101".equals(regPlmn))) {
                showSpnRslt = true;
                showPlmnRslt = false;
                ruleRslt = 1;
                spnRslt = spnSim;
            } else {
                matched = false;
            }
        } else {
            matched = false;
        }
        if (matched) {
            return new OnsDisplayParams(showSpnRslt, showPlmnRslt, ruleRslt, plmn, spnRslt);
        }
        return null;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsBySpecialCust(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        String plmnRslt = plmn;
        String spnRslt = spn;
        boolean showPlmnRslt = showPlmn;
        boolean showSpnRslt = showSpn;
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
            int ruleSim = r.getDisplayRule(regPlmn);
            Rlog.d(LOG_TAG, "regPlmn = " + regPlmn + ",hplmn = " + hplmn + ",spnSim = " + spnSim + ",ruleSim = " + ruleSim + ",netType = " + netType + ",netClass = " + netClass);
            String pnnName;
            if ("21405".equals(hplmn) && "21407".equals(regPlmn) && "tuenti".equalsIgnoreCase(spnSim)) {
                pnnName = getEons(plmn);
                if (!TextUtils.isEmpty(spnSim)) {
                    spnRslt = spnSim;
                    showSpnRslt = (ruleSim & 1) == 1;
                }
                if (!TextUtils.isEmpty(pnnName)) {
                    plmnRslt = pnnName;
                    showPlmnRslt = (ruleSim & 2) == 2;
                }
            } else if ("21407".equals(hplmn) && "21407".equals(regPlmn)) {
                pnnName = getEons(plmn);
                if (!TextUtils.isEmpty(spnSim)) {
                    spnRslt = spnSim;
                    showSpnRslt = (rule & 1) == 1;
                }
                if (!TextUtils.isEmpty(pnnName)) {
                    plmnRslt = pnnName;
                    showPlmnRslt = (rule & 2) == 2;
                }
            } else if ("23420".equals(hplmn) && getCombinedRegState(getSS()) == 0) {
                pnnName = getEons(plmn);
                if (!TextUtils.isEmpty(pnnName)) {
                    spnRslt = spnSim;
                    plmnRslt = pnnName;
                    showSpnRslt = false;
                    showPlmnRslt = true;
                    ruleRslt = 2;
                }
            } else if (("74000".equals(hplmn) && "74000".equals(regPlmn) && plmn.equals(spn)) || ("45006".equals(hplmn) && "45006".equals(regPlmn) && "LG U+".equals(plmn))) {
                showPlmnRslt = true;
                showSpnRslt = false;
                ruleRslt = 2;
            } else if ("732187".equals(hplmn) && ("732103".equals(regPlmn) || "732111".equals(regPlmn))) {
                if (1 == netClass || 2 == netClass) {
                    plmnRslt = "ETB";
                } else if (3 == netClass) {
                    plmnRslt = "ETB 4G";
                }
            } else if ("50218".equals(hplmn) && "50212".equals(regPlmn)) {
                if (1 == netClass || 2 == netClass) {
                    showSpnRslt = true;
                    showPlmnRslt = false;
                    ruleRslt = 1;
                    plmnRslt = "U Mobile";
                    spnRslt = "U Mobile";
                } else if (3 == netClass) {
                    showSpnRslt = true;
                    showPlmnRslt = false;
                    ruleRslt = 1;
                    plmnRslt = "MY MAXIS";
                    spnRslt = "MY MAXIS";
                }
            } else if ("334050".equals(hplmn) || "334090".equals(hplmn) || "33405".equals(hplmn)) {
                if (TextUtils.isEmpty(spnSim) && (("334050".equals(regPlmn) || "334090".equals(regPlmn)) && !TextUtils.isEmpty(plmn) && (plmn.startsWith("Iusacell") || plmn.startsWith("Nextel")))) {
                    Rlog.d(LOG_TAG, "AT&T a part of card has no opl/PNN and spn, then want it to be treated as AT&T");
                    plmnRslt = "AT&T";
                }
                if (!TextUtils.isEmpty(plmnRslt) && plmnRslt.startsWith("AT&T")) {
                    if (1 == netClass) {
                        plmnRslt = "AT&T EDGE";
                    } else if (2 == netClass) {
                        plmnRslt = "AT&T";
                    } else if (3 == netClass) {
                        plmnRslt = "AT&T 4G";
                    }
                }
            } else if (ServiceStateTrackerUtils.isDocomo()) {
                if (TextUtils.isEmpty(spn)) {
                    spnRslt = spnSim;
                }
                showSpnRslt = (getCombinedRegState(getSS()) != 0 || (TextUtils.isEmpty(spnRslt) ^ 1) == 0) ? false : (rule & 1) == 1;
                Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsBySpecialCust: spnRslt = " + spnRslt + ", showSpnRslt = " + showSpnRslt);
            } else {
                matched = false;
            }
        } else {
            matched = false;
        }
        Rlog.d(LOG_TAG, "matched = " + matched + ",showPlmnRslt = " + showPlmnRslt + ",showSpnRslt = " + showSpnRslt + ",ruleRslt = " + ruleRslt + ",plmnRslt = " + plmnRslt + ",spnRslt = " + spnRslt);
        if (matched) {
            return new OnsDisplayParams(showSpnRslt, showPlmnRslt, ruleRslt, plmnRslt, spnRslt);
        }
        return null;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsForChinaOperator(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        CharSequence hplmn = null;
        CharSequence regplmn = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        Rlog.d("HwGsmServiceStateTracker", "showSpn:" + showSpn + ",showPlmn:" + showPlmn + ",rule:" + rule + ",plmn:" + plmn + ",spn:" + spn + ",hplmn:" + hplmn + ",regplmn:" + regplmn);
        if (!TextUtils.isEmpty(regplmn)) {
            if (!isChinaMobileMccMnc()) {
                if (HwTelephonyFactory.getHwUiccManager().isCDMASimCard(this.mPhoneId)) {
                    if (!getSS().getRoaming()) {
                        Rlog.d("HwGsmServiceStateTracker", "In not roaming condition just show plmn without spn.");
                        return new OnsDisplayParams(false, true, rule, plmn, spn);
                    } else if (HwTelephonyManagerInner.getDefault().isCTSimCard(this.mPhoneId)) {
                        if (EMERGENCY_PLMN.equals(plmn) || NO_SERVICE_PLMN.equals(plmn)) {
                            Rlog.d("HwGsmServiceStateTracker", "out of service or emergency.");
                            return new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
                        }
                        if (TextUtils.isEmpty(spn)) {
                            Rlog.d("HwGsmServiceStateTracker", "spn is null.");
                            try {
                                spn = URLDecoder.decode(CHINA_TELECOM_SPN, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                Rlog.d("HwGsmServiceStateTracker", "UnsupportedEncodingException.");
                            }
                        }
                        return new OnsDisplayParams(true, true, rule, plmn, spn);
                    }
                }
                if (HuaweiTelephonyConfigs.isChinaTelecom() || (getSS().getRoaming() && (TextUtils.isEmpty(hplmn) ^ 1) != 0 && "20404".equals(hplmn) && "20404".equals(regplmn) && 3 == (HwAllInOneController.getInstance().getSpecCardType(this.mGsmPhone.getPhoneId()) & 15))) {
                    Rlog.d("HwGsmServiceStateTracker", "In China Telecom, just show plmn without spn.");
                    return new OnsDisplayParams(false, true, rule, plmn, spn);
                }
            } else if (spn == null || ("".equals(spn) ^ 1) == 0 || ("CMCC".equals(spn) ^ 1) == 0 || ("China Mobile".equals(spn) ^ 1) == 0) {
                Rlog.d("HwGsmServiceStateTracker", "chinamobile just show plmn without spn.");
                return new OnsDisplayParams(false, true, rule, plmn, spn);
            } else {
                Rlog.d("HwGsmServiceStateTracker", "third party provider sim cust just show original rule.");
                return new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
            }
        }
        return null;
    }

    private void log(String string) {
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
        boolean z = true;
        boolean z2 = false;
        String result = SystemProperties.get("persist.radio.nitz_hw_name");
        String result1 = SystemProperties.get("persist.radio.nitz_hw_name1");
        if (!isMultiSimEnabled) {
            if (result != null && result.length() > 0) {
                z2 = true;
            }
            return z2;
        } else if (slotId == 0) {
            if (result == null || result.length() <= 0) {
                z = false;
            }
            return z;
        } else if (1 == slotId) {
            if (result1 != null && result1.length() > 0) {
                z2 = true;
            }
            return z2;
        } else {
            Rlog.e("HwGsmServiceStateTracker", "hasNitzOperatorName invalid sub id" + slotId);
            return false;
        }
    }

    private boolean getGsmRoamingCustByIMSIStart(boolean roaming) {
        String regplmnRoamCustomString = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            String hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumericEx(this.mCr, "hw_roam_hplmn_ex");
            String regplmn = getNewSS().getOperatorNumeric();
            int netType = getNewSS().getVoiceNetworkType();
            TelephonyManager.getDefault();
            int netClass = TelephonyManager.getNetworkClass(netType);
            Rlog.d("HwGsmServiceStateTracker", "hplmn=" + hplmn + "  regplmn=" + regplmn + "  netType=" + netType + "  netClass=" + netClass);
            try {
                regplmnRoamCustomString = System.getString(this.mCr, "reg_plmn_roam_custom");
            } catch (Exception e) {
                Rlog.e("HwGsmServiceStateTracker", "Exception when got reg_plmn_roam_custom value", e);
            }
            if (!(regplmnRoamCustomString == null || hplmn == null || regplmn == null)) {
                for (String rule_item : regplmnRoamCustomString.split(";")) {
                    String[] rule_plmn_roam = rule_item.split(":");
                    if (rule_plmn_roam.length == 2) {
                        int rule_roam = Integer.parseInt(rule_plmn_roam[0]);
                        String[] plmn_roam = rule_plmn_roam[1].split(",");
                        if (2 == plmn_roam.length) {
                            if (plmn_roam[0].equals(hplmn) && plmn_roam[1].equals(regplmn)) {
                                Rlog.d("HwGsmServiceStateTracker", "roaming customization by hplmn and regplmn success!");
                                if (1 == rule_roam) {
                                    return true;
                                }
                                if (2 == rule_roam) {
                                    return false;
                                }
                            }
                            if (3 == rule_roam && hplmn.length() > 2 && regplmn.length() > 2) {
                                String hplmnMcc = hplmn.substring(0, 3);
                                String regplmnMcc = regplmn.substring(0, 3);
                                if (plmn_roam[0].equals(hplmnMcc) && plmn_roam[1].equals(regplmnMcc)) {
                                    roaming = false;
                                }
                            }
                        } else {
                            if (3 == plmn_roam.length) {
                                Rlog.d("HwGsmServiceStateTracker", "roaming customization by RAT");
                                if (plmn_roam[0].equals(hplmn) && plmn_roam[1].equals(regplmn) && plmn_roam[2].contains(String.valueOf(netClass + 1))) {
                                    Rlog.d("HwGsmServiceStateTracker", "roaming customization by RAT success!");
                                    if (1 == rule_roam) {
                                        return true;
                                    }
                                    if (2 == rule_roam) {
                                        return false;
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
            return roaming;
        }
        Rlog.e("HwGsmServiceStateTracker", "mIccRecords null while getGsmRoamingCustByIMSIStart was called.");
        return roaming;
    }

    private String getDefaultSpn(String spn, String hplmn, String regplmn) {
        if (TextUtils.isEmpty(hplmn) || TextUtils.isEmpty(regplmn)) {
            return spn;
        }
        String defaultSpnString = System.getString(this.mCr, "hw_spnnull_defaultspn");
        if (TextUtils.isEmpty(defaultSpnString) || (Pattern.matches(REGEX, defaultSpnString) ^ 1) != 0) {
            return spn;
        }
        for (String defaultSpnItem : defaultSpnString.split(";")) {
            String[] defaultSpn = defaultSpnItem.split(",");
            if (hplmn.equals(defaultSpn[0]) && regplmn.equals(defaultSpn[1])) {
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
            this.mGsmPhone.getContext().unregisterReceiver(this.mIntentReceiver);
            this.mGsmPhone.getContext().unregisterReceiver(this.mCloudOtaBroadcastReceiver);
            if (PS_CLEARCODE || SHOW_REJ_INFO_KT) {
                this.mCi.unSetOnNetReject(this);
            }
        }
    }

    private void sendBroadcastCrrConnInd(int modem0, int modem1, int modem2) {
        Rlog.i(LOG_TAG, "GSM sendBroadcastCrrConnInd");
        String MODEM0 = "modem0";
        String MODEM1 = "modem1";
        String MODEM2 = "modem2";
        Intent intent = new Intent("com.huawei.action.ACTION_HW_CRR_CONN_IND");
        intent.putExtra("modem0", modem0);
        intent.putExtra("modem1", modem1);
        intent.putExtra("modem2", modem2);
        Rlog.i(LOG_TAG, "modem0: " + modem0 + " modem1: " + modem1 + " modem2: " + modem2);
        this.mGsmPhone.getContext().sendBroadcast(intent, "com.huawei.permission.CRRCONN_PERMISSION");
    }

    private void sendBroadcastRegPLMNSelInfo(int flag, int result) {
        String SUB_ID = HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID;
        String FLAG = "flag";
        String RES = "res";
        Intent intent = new Intent("com.huawei.action.SIM_PLMN_SELINFO");
        int subId = this.mGsmPhone.getPhoneId();
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        intent.putExtra("flag", flag);
        intent.putExtra("res", result);
        Rlog.i(LOG_TAG, "subId: " + subId + " flag: " + flag + " result: " + result);
        this.mGsmPhone.getContext().sendBroadcast(intent, "com.huawei.permission.HUAWEI_BUSSINESS_PERMISSION");
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
        int otherCardType;
        int otherCardState;
        int otherPhoneType;
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
        Rlog.d("HwGsmServiceStateTracker", "ownCardType = " + ownCardType + ", otherCardType = " + otherCardType + ", otherCardState = " + otherCardState + " ownPhoneType = " + ownPhoneType + ", otherPhoneType = " + otherPhoneType);
        if ((ownCardType == 41 || ownCardType == 43) && ownPhoneType == 2) {
            Rlog.d("HwGsmServiceStateTracker", "Cdma card, uppdate NITZ time!");
            return true;
        } else if ((otherCardType == 30 || otherCardType == 43 || otherCardType == 41) && 1 == otherCardState && otherPhoneType == 2) {
            Rlog.d("HwGsmServiceStateTracker", "Other cdma card, ignore updating NITZ time!");
            return false;
        } else if (HwVSimUtils.isVSimOn() && HwVSimUtils.isVSimSub(phoneId)) {
            Rlog.d("HwGsmServiceStateTracker", "vsim phone, update NITZ time!");
            return true;
        } else if (phoneId == SystemProperties.getInt("gsm.sim.updatenitz", phoneId) || -1 == SystemProperties.getInt("gsm.sim.updatenitz", -1) || otherCardState == 0) {
            SystemProperties.set("gsm.sim.updatenitz", String.valueOf(phoneId));
            Rlog.d("HwGsmServiceStateTracker", "Update NITZ time, set update card : " + phoneId);
            return true;
        } else {
            Rlog.d("HwGsmServiceStateTracker", "Ignore updating NITZ time, phoneid : " + phoneId);
            return false;
        }
    }

    public void processCTNumMatch(boolean roaming, UiccCardApplication uiccCardApplication) {
        Rlog.d("HwGsmServiceStateTracker", "processCTNumMatch, roaming: " + roaming);
        if (IS_CHINATELECOM && uiccCardApplication != null && AppState.APPSTATE_READY == uiccCardApplication.getState()) {
            int slotId = HwAddonTelephonyFactory.getTelephony().getDefault4GSlotId();
            Rlog.d("HwGsmServiceStateTracker", "processCTNumMatch->getDefault4GSlotId, slotId: " + slotId);
            if (HwTelephonyManagerInner.getDefault().isCTCdmaCardInGsmMode() && uiccCardApplicationUtils.getUiccCard(uiccCardApplication) == UiccController.getInstance().getUiccCard(slotId)) {
                Rlog.d("HwGsmServiceStateTracker", "processCTNumMatch, isCTCdmaCardInGsmMode..");
                setCTNumMatchRoamingForSlot(slotId);
            }
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        int[] response;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "EVENT_ICC_RECORDS_EONS_UPDATED exception " + ar.exception);
                    return;
                } else {
                    processIccEonsRecordsUpdated(((Integer) ar.result).intValue());
                    return;
                }
            case 64:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "EVENT_POLL_LOCATION_INFO exception " + ar.exception);
                    return;
                }
                String[] states = ar.result;
                Rlog.i(LOG_TAG, "CLEARCODE EVENT_POLL_LOCATION_INFO");
                if (states.length == 4) {
                    try {
                        if (states[2] != null && states[2].length() > 0) {
                            this.mRac = Integer.parseInt(states[2], 16);
                            Rlog.d(LOG_TAG, "CLEARCODE mRac = " + this.mRac);
                            return;
                        }
                        return;
                    } catch (NumberFormatException ex) {
                        Rlog.e(LOG_TAG, "error parsing LocationInfoState: " + ex);
                        return;
                    }
                }
                return;
            case EVENT_RPLMNS_STATE_CHANGED /*101*/:
                Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]EVENT_RPLMNS_STATE_CHANGED");
                this.oldRplmn = this.rplmn;
                this.rplmn = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
                setNetworkSelectionModeAutomaticHw(this.oldRplmn, this.rplmn);
                String mcc = "";
                String oldMcc = "";
                if (this.rplmn != null && this.rplmn.length() > 3) {
                    mcc = this.rplmn.substring(0, 3);
                }
                if (this.oldRplmn != null && this.oldRplmn.length() > 3) {
                    oldMcc = this.oldRplmn.substring(0, 3);
                }
                if (!("".equals(mcc) || (mcc.equals(oldMcc) ^ 1) == 0)) {
                    this.mGsmPhone.notifyMccChanged(mcc);
                    Rlog.d(LOG_TAG, "rplmn mcc changed.");
                }
                Rlog.d(LOG_TAG, "rplmn" + this.rplmn);
                if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false) && this.mGsmPhone.mDcTracker != null) {
                    this.mGsmPhone.mDcTracker.checkPLMN(this.rplmn);
                }
                if (SystemProperties.getBoolean("ro.config.hw_globalEcc", true)) {
                    Rlog.d(LOG_TAG, "the global emergency numbers custom-make does enable!!!!");
                    toGetRplmnsThenSendEccNum();
                    return;
                }
                return;
            case EVENT_DELAY_UPDATE_REGISTER_STATE_DONE /*102*/:
                Rlog.d(LOG_TAG, "[Phone" + this.mPhoneId + "]Delay Timer expired, begin get register state");
                this.mRefreshState = true;
                this.mGsst.pollState();
                return;
            case EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH /*103*/:
                Rlog.d(LOG_TAG, "event update gsm signal strength");
                this.mDoubleSignalStrength.proccessAlaphFilter(this.mGsst.getSignalStrength(), this.mModemSignalStrength);
                this.mGsmPhone.notifySignalStrength();
                return;
            case EVENT_CA_STATE_CHANGED /*104*/:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    onCaStateChanged(((int[]) ar.result)[0] == 1);
                    return;
                } else {
                    log("EVENT_CA_STATE_CHANGED: exception;");
                    return;
                }
            case EVENT_NETWORK_REJECTED_CASE /*105*/:
                Rlog.d(LOG_TAG, "EVENT_NETWORK_REJECTED_CASE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "EVENT_NETWORK_REJECTED_CASE exception " + ar.exception);
                    return;
                } else {
                    onNetworkReject(ar);
                    return;
                }
            case EVENT_PLMN_SELINFO /*151*/:
                Rlog.d(LOG_TAG, "EVENT_PLMN_SELINFO");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    response = (int[]) ar.result;
                    if (response.length != 0) {
                        sendBroadcastRegPLMNSelInfo(response[0], response[1]);
                        return;
                    }
                    return;
                }
                return;
            case EVENT_CRR_CONN /*152*/:
                Rlog.d(LOG_TAG, "GSM EVENT_CRR_CONN");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    response = ar.result;
                    if (response.length != 0) {
                        sendBroadcastCrrConnInd(response[0], response[1], response[2]);
                        return;
                    }
                    return;
                }
                Rlog.d(LOG_TAG, "GSM EVENT_CRR_CONN: exception;");
                return;
            case EVENT_MCC_CHANGED /*154*/:
                Rlog.d(LOG_TAG, "EVENT_MCC_CHANGED");
                SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    private void setNetworkSelectionModeAutomaticHw(String oldRplmn, String rplmn) {
        String autoSelectMccs = System.getString(this.mContext.getContentResolver(), "hw_auto_select_network_mcc");
        if (!TextUtils.isEmpty(autoSelectMccs) && !TextUtils.isEmpty(oldRplmn) && !TextUtils.isEmpty(rplmn) && oldRplmn.length() >= 3 && rplmn.length() >= 3) {
            if (this.mGsmPhone == null || (this.mGsmPhone.getServiceState().getIsManualSelection() ^ 1) != 0) {
                Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomaticHw - already auto, ignoring.");
                return;
            }
            String[] mccs = autoSelectMccs.split(",");
            String oldMcc = oldRplmn.substring(0, 3);
            String newMcc = rplmn.substring(0, 3);
            boolean isNeedSelectAuto = false;
            int i = 0;
            while (i < mccs.length) {
                if (!oldMcc.equals(mccs[i]) && newMcc.equals(mccs[i])) {
                    isNeedSelectAuto = true;
                    break;
                }
                i++;
            }
            Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomaticHw isNeedSelectAuto:" + isNeedSelectAuto);
            if (isNeedSelectAuto) {
                this.mGsmPhone.setNetworkSelectionModeAutomatic(null);
            }
        }
    }

    private void onNetworkReject(AsyncResult ar) {
        if (ar.exception == null) {
            String[] data = ar.result;
            String rejectplmn = null;
            int rejectdomain = -1;
            int rejectcause = -1;
            int rejectrat = -1;
            if (data.length > 0) {
                try {
                    if (data[0] != null && data[0].length() > 0) {
                        rejectplmn = data[0];
                    }
                    if (data[1] != null && data[1].length() > 0) {
                        rejectdomain = Integer.parseInt(data[1]);
                    }
                    if (data[2] != null && data[2].length() > 0) {
                        rejectcause = Integer.parseInt(data[2]);
                    }
                    if (data[3] != null && data[3].length() > 0) {
                        rejectrat = Integer.parseInt(data[3]);
                    }
                } catch (Exception ex) {
                    Rlog.e(LOG_TAG, "error parsing NetworkReject!", ex);
                }
                if (SHOW_REJ_INFO_KT && this.mGsst.returnObject() != null) {
                    this.mGsst.returnObject().handleNetworkRejectionEx(rejectcause, rejectrat);
                }
                if (PS_CLEARCODE) {
                    if (2 == rejectrat) {
                        this.rejNum++;
                    }
                    Rlog.d(LOG_TAG, "NetworkReject:PLMN = " + rejectplmn + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat + " rejNum = " + this.rejNum);
                    if (this.rejNum >= 3) {
                        this.mGsmPhone.setPreferredNetworkType(3, null);
                        Global.putInt(this.mGsmPhone.getContext().getContentResolver(), "preferred_network_mode", 3);
                        this.rejNum = 0;
                        this.mRac = -1;
                    }
                }
            }
        }
    }

    public boolean isNetworkTypeChanged(SignalStrength oldSS, SignalStrength newSS) {
        int newState = 0;
        int oldState = 0;
        if (newSS.getGsmSignalStrength() < -1) {
            newState = 1;
        }
        if (oldSS.getGsmSignalStrength() < -1) {
            oldState = 1;
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
            sendMessageDelayed(msg, (long) mDelayDuringTime);
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
            } catch (NullPointerException ex) {
                Rlog.e(LOG_TAG, "onSignalStrengthResult() Phone already destroyed: " + ex + "SignalStrength not notified");
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
            boolean HasCAactivated = 19 == currentType && 19 != this.mOldCAstate;
            boolean HasCAdeActivated = 19 != currentType && 19 == this.mOldCAstate;
            this.mOldCAstate = currentType;
            if (HasCAactivated || HasCAdeActivated) {
                Intent intentLteCAState = new Intent("com.huawei.intent.action.LTE_CA_STATE");
                intentLteCAState.putExtra("subscription", this.mGsmPhone.getSubId());
                if (HasCAactivated) {
                    intentLteCAState.putExtra("LteCAstate", true);
                    Rlog.d(LOG_TAG, "[CA] CA activated !");
                } else if (HasCAdeActivated) {
                    intentLteCAState.putExtra("LteCAstate", false);
                    Rlog.d(LOG_TAG, "[CA] CA deactivated !");
                }
                this.mGsmPhone.getContext().sendBroadcast(intentLteCAState);
            }
        }
        return currentType;
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
        switch (eventCode) {
            case 2:
                this.mGsst.updateSpnDisplay();
                return;
            case 100:
                this.mGsst.updateSpnDisplay();
                return;
            default:
                return;
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
        if (r != null && (r.isEonsDisabled() ^ 1) != 0) {
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

    private void cancelDeregisterStateDelayTimer() {
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
        boolean lostNework = (oldSS.getVoiceRegState() == 0 || oldSS.getDataRegState() == 0) ? newSS.getVoiceRegState() != 0 ? newSS.getDataRegState() != 0 : false : false;
        boolean isSubDeactivated = SubscriptionController.getInstance().getSubState(this.mGsmPhone.getSubId()) == 0;
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        State mExternalState = this.mGsmPhone.getIccCard().getState();
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]lostNework : " + lostNework + ", desiredPowerState : " + this.mGsst.getDesiredPowerState() + ", radiostate : " + this.mCi.getRadioState() + ", mRadioOffByDoRecovery : " + this.mGsst.getDoRecoveryTriggerState() + ", isSubDeactivated : " + isSubDeactivated + ", newMainSlot : " + newMainSlot + ", phoneOOS : " + this.mGsmPhone.getOOSFlag() + ", isUserPref4GSlot : " + HwAllInOneController.getInstance().isUserPref4GSlot(this.mMainSlot) + ", mExternalState : " + mExternalState);
        if (newSS.getDataRegState() == 0 || newSS.getVoiceRegState() == 0 || (this.mGsst.getDesiredPowerState() ^ 1) != 0 || this.mCi.getRadioState() == RadioState.RADIO_OFF || this.mGsst.getDoRecoveryTriggerState() || isCardInvalid(isSubDeactivated, this.mGsmPhone.getSubId()) || (HwAllInOneController.getInstance().isUserPref4GSlot(this.mMainSlot) ^ 1) != 0 || this.mGsmPhone.getOOSFlag() || newSS.getDataRegState() == 3 || mExternalState == State.PUK_REQUIRED) {
            this.mMainSlot = newMainSlot;
            cancelDeregisterStateDelayTimer();
        } else if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            return true;
        } else {
            if (lostNework && (this.mRefreshState ^ 1) != 0) {
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
        boolean isCsLostNetwork = (oldSS.getVoiceRegState() != 0 || newSS.getVoiceRegState() == 0) ? false : newSS.getDataRegState() != 0;
        boolean isPsLostNetwork = (oldSS.getDataRegState() != 0 || newSS.getVoiceRegState() == 0) ? false : newSS.getDataRegState() != 0;
        int delayedTime = 0;
        TelephonyManager.getDefault();
        int networkClass = TelephonyManager.getNetworkClass(oldSS.getNetworkType());
        if (isCsLostNetwork) {
            if (networkClass == 1) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_CS_2G;
            } else if (networkClass == 2) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_CS_3G;
            } else if (networkClass == 3) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_CS_4G;
            } else {
                delayedTime = DELAYED_TIME_DEFAULT_VALUE * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
            }
        } else if (isPsLostNetwork) {
            if (networkClass == 1) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_PS_2G;
            } else if (networkClass == 2) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_PS_3G;
            } else if (networkClass == 3) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_PS_4G;
            } else {
                delayedTime = DELAYED_TIME_DEFAULT_VALUE * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
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
        String rplmns = "";
        String hplmn = TelephonyManager.getDefault().getSimOperator(this.mPhoneId);
        String forceEccState = SystemProperties.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]GECC-toGetRplmnsThenSendEccNum: hplmn = " + hplmn + "; forceEccState = " + forceEccState);
        if (((IccCardProxy) PhoneFactory.getDefaultPhone().getIccCard()).getIccCardStateHW() || hplmn.equals("") || forceEccState.equals("usim_absent")) {
            rplmns = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
            if (!rplmns.equals("")) {
                this.mGsmPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
                this.mGsmPhone.globalEccCustom(rplmns);
            }
        }
    }

    public void sendGsmRoamingIntentIfDenied(int regState, int rejectCode) {
        HwCustGsmServiceStateTracker returnObject = this.mGsst.returnObject();
        if ((regState == 3 || regState == 13) && rejectCode == 10) {
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
        if (r == null) {
            return new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
        }
        String hPlmn = r.getOperatorNumeric();
        String regPlmn = getSS().getOperatorNumeric();
        String spnSim = r.getServiceProviderName();
        int ruleSim = r.getDisplayRule(getSS().getOperatorNumeric());
        Rlog.d(LOG_TAG, "SpnOnly spn:" + spnSim + ",hplmn:" + hPlmn + ",regPlmn:" + regPlmn);
        if (!TextUtils.isEmpty(spnSim)) {
            if (isMccForSpn(r.getOperatorNumeric()) && (TextUtils.isEmpty(hPlmn) ^ 1) != 0 && hPlmn.length() > 3) {
                String currentMcc = hPlmn.substring(0, 3);
                if (!TextUtils.isEmpty(regPlmn) && regPlmn.length() > 3 && currentMcc.equals(regPlmn.substring(0, 3))) {
                    mShowSpn = true;
                    mShowPlmn = false;
                    spnRes = spnSim;
                    mRule = ruleSim;
                    plmnRes = "";
                }
            }
        }
        return new OnsDisplayParams(mShowSpn, mShowPlmn, mRule, plmnRes, spnRes);
    }

    private boolean isMccForSpn(String currentMccmnc) {
        String strMcc = System.getString(this.mContext.getContentResolver(), "hw_mcc_showspn_only");
        HashSet<String> mShowspnOnlyMcc = new HashSet();
        String currentMcc = "";
        if (currentMccmnc == null || currentMccmnc.length() < 3) {
            return false;
        }
        currentMcc = currentMccmnc.substring(0, 3);
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
        String strMcc = System.getString(this.mContext.getContentResolver(), "hw_mcc_show_no_roaming");
        HashSet<String> mShowNoRoamingMcc = new HashSet();
        String currentMcc = "";
        if (currentMccmnc == null || currentMccmnc.length() < 3) {
            return false;
        }
        currentMcc = currentMccmnc.substring(0, 3);
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
}
