package com.android.internal.telephony.cdma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.CellInfo;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwAddonTelephonyFactory;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPlmnActConcat;
import com.android.internal.telephony.HwServiceStateManager;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.telephony.HwSignalStrength.SignalThreshold;
import com.android.internal.telephony.HwSignalStrength.SignalType;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.OnsDisplayParams;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PlmnConstants;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import huawei.cust.HwCustUtils;
import java.util.Locale;

public class HwCdmaServiceStateManager extends HwServiceStateManager {
    private static final int CDMA_LEVEL = 8;
    private static final int CDMA_STRENGTH_POOR_STD = SystemProperties.getInt("ro.cdma.poorstd", C_STRENGTH_POOR_STD);
    public static final String CLOUD_OTA_DPLMN_UPDATE = "cloud.ota.dplmn.UPDATE";
    public static final String CLOUD_OTA_MCC_UPDATE = "cloud.ota.mcc.UPDATE";
    public static final String CLOUD_OTA_PERMISSION = "huawei.permission.RECEIVE_CLOUD_OTA_UPDATA";
    private static final String CT_MACAO_MCC = "455";
    private static final int CT_MACAO_SID_END = 11311;
    private static final int CT_MACAO_SID_START = 11296;
    private static final String CT_MCC = "460";
    private static final int CT_SID_1st_END = 14335;
    private static final int CT_SID_1st_START = 13568;
    private static final int CT_SID_2nd_END = 26111;
    private static final int CT_SID_2nd_START = 25600;
    private static final int C_STRENGTH_POOR_STD = -112;
    private static final int DEFAULT_SID = 0;
    private static final int EVDO_LEVEL = 16;
    private static final int EVENT_CA_STATE_CHANGED = 105;
    private static final int EVENT_CRR_CONN = 153;
    private static final int EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH = 104;
    private static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 102;
    private static final int EVENT_PLMN_SELINFO = 154;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 106;
    private static final int GSM_LEVEL = 1;
    private static final String INVAILD_PLMN = "1023127-123456-1023456-123127-99999-";
    private static final boolean IS_CMCC_4G_DSDX_ENABLE = SystemProperties.getBoolean("ro.hwpp.cmcc_4G_dsdx_enable", false);
    private static final boolean IS_HISI_PLATFORM = (HwModemCapability.isCapabilitySupport(9) ^ 1);
    private static final String LOG_TAG = "HwCdmaServiceStateManager";
    private static final int LTE_LEVEL = 4;
    private static final int LTE_RSSNR_POOR_STD = SystemProperties.getInt("ro.lte.rssnrpoorstd", L_RSSNR_POOR_STD);
    private static final int LTE_RSSNR_UNKOUWN_STD = 99;
    private static final int LTE_STRENGTH_POOR_STD = SystemProperties.getInt("ro.lte.poorstd", L_STRENGTH_POOR_STD);
    private static final int LTE_STRENGTH_UNKOUWN_STD = -44;
    private static final int L_RSSNR_POOR_STD = -5;
    private static final int L_STRENGTH_POOR_STD = -125;
    private static final String PROPERTY_GLOBAL_FORCE_TO_SET_ECC = "ril.force_to_set_ecc";
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final boolean SET_UICC_BY_RADIO_POWER = SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false);
    private static final boolean SHOW_4G_PLUS_ICON = SystemProperties.getBoolean("ro.config.hw_show_4G_Plus_icon", false);
    private static final String SPRINT_OPERATOR = "310000";
    private static final String SPRINT_OPERATOR_ALPHA_LONG = "Sprint";
    private static final int TIME_NOT_SET = 0;
    private static final int UMTS_LEVEL = 2;
    private static final int VALUE_DELAY_DURING_TIME = 6000;
    private static final String mCdmaHomeOperatorNumeric = SystemProperties.get("ro.cdma.home.operator.numeric");
    private static final int mDelayDuringTimeLte = SystemProperties.getInt("ro.signalsmooth.delaytimer", VALUE_DELAY_DURING_TIME);
    private static HwSignalStrength mHwSigStr = HwSignalStrength.getInstance();
    private static final String[] usa_mcc_list = new String[]{"332", "310", "311", "312", "313", "314", "315", "316", "544"};
    private final boolean FEATURE_SIGNAL_DUALPARAM = SystemProperties.getBoolean("signal.dualparam", false);
    private CloudOtaBroadcastReceiver mCloudOtaBroadcastReceiver = new CloudOtaBroadcastReceiver(this, null);
    private Context mContext;
    private HwCustCdmaServiceStateManager mCust;
    private DoubleSignalStrength mDoubleSignalStrength;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                HwCdmaServiceStateManager.this.updateSpnDisplay();
            } else if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(action)) {
                Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "[SLOT" + HwCdmaServiceStateManager.this.mPhone.getPhoneId() + "]CardState: " + intent.getIntExtra("newSubState", -1) + "IsMphone: " + (HwCdmaServiceStateManager.this.mPhone.getPhoneId() == intent.getIntExtra("phone", 0)));
                if (intent.getIntExtra("operationResult", 1) == 0 && HwCdmaServiceStateManager.this.mPhone.getPhoneId() == intent.getIntExtra("phone", 0) && HwCdmaServiceStateManager.this.hasMessages(HwCdmaServiceStateManager.EVENT_DELAY_UPDATE_REGISTER_STATE_DONE) && intent.getIntExtra("newSubState", -1) == 0) {
                    HwCdmaServiceStateManager.this.cancelDeregisterStateDelayTimer();
                    HwCdmaServiceStateManager.this.mSST.pollState();
                }
            }
        }
    };
    private SignalStrength mModemSignalStrength;
    private int mOldCAstate = 0;
    private DoubleSignalStrength mOldDoubleSignalStrength;
    private GsmCdmaPhone mPhone;
    private int mPhoneId = 0;
    private ServiceStateTracker mSST;
    private String rplmn = "";

    private class CloudOtaBroadcastReceiver extends BroadcastReceiver {
        /* synthetic */ CloudOtaBroadcastReceiver(HwCdmaServiceStateManager this$0, CloudOtaBroadcastReceiver -this1) {
            this();
        }

        private CloudOtaBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (HwCdmaServiceStateManager.this.mPhone != null && HwCdmaServiceStateManager.this.mPhone.mCi != null) {
                String action = intent.getAction();
                if ("cloud.ota.mcc.UPDATE".equals(action)) {
                    Rlog.e(HwCdmaServiceStateManager.LOG_TAG, "HwCloudOTAService CLOUD_OTA_MCC_UPDATE");
                    HwCdmaServiceStateManager.this.mPhone.mCi.sendCloudMessageToModem(1);
                } else if ("cloud.ota.dplmn.UPDATE".equals(action)) {
                    Rlog.e(HwCdmaServiceStateManager.LOG_TAG, "HwCloudOTAService CLOUD_OTA_DPLMN_UPDATE");
                    HwCdmaServiceStateManager.this.mPhone.mCi.sendCloudMessageToModem(2);
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
        private double mDoubleCdmaDbm;
        private double mDoubleCdmaEcio;
        private double mDoubleEvdoDbm;
        private double mDoubleEvdoEcio;
        private double mDoubleEvdoSnr;
        private double mDoubleLteRsrp;
        private double mDoubleLteRssnr;
        private double mOldDoubleCdmaDbm;
        private double mOldDoubleCdmaEcio;
        private double mOldDoubleEvdoDbm;
        private double mOldDoubleEvdoEcio;
        private double mOldDoubleEvdoSnr;
        private double mOldDoubleLteRsrp;
        private double mOldDoubleLteRssnr;
        private int mTechState;

        public DoubleSignalStrength(SignalStrength ss) {
            this.mDoubleLteRsrp = (double) ss.getLteRsrp();
            this.mDoubleLteRssnr = (double) ss.getLteRssnr();
            this.mDoubleCdmaDbm = (double) ss.getCdmaDbm();
            this.mDoubleCdmaEcio = (double) ss.getCdmaEcio();
            this.mDoubleEvdoDbm = (double) ss.getEvdoDbm();
            this.mDoubleEvdoEcio = (double) ss.getEvdoEcio();
            this.mDoubleEvdoSnr = (double) ss.getEvdoSnr();
            this.mTechState = 0;
            if (ss.getCdmaDbm() < -1) {
                this.mTechState |= 8;
            }
            if (ss.isGsm()) {
                if (ss.getLteRsrp() < -1) {
                    this.mTechState |= 4;
                }
            } else if (ss.getEvdoDbm() < -1) {
                this.mTechState |= 16;
            }
            this.mOldDoubleLteRsrp = this.mDoubleLteRsrp;
            this.mOldDoubleLteRssnr = this.mDoubleLteRssnr;
            this.mOldDoubleCdmaDbm = this.mDoubleCdmaDbm;
            this.mOldDoubleCdmaEcio = this.mDoubleCdmaEcio;
            this.mOldDoubleEvdoDbm = this.mDoubleEvdoDbm;
            this.mOldDoubleEvdoEcio = this.mDoubleEvdoEcio;
            this.mOldDoubleEvdoSnr = this.mDoubleEvdoSnr;
            this.mDelayTime = 0;
        }

        public DoubleSignalStrength(DoubleSignalStrength doubleSS) {
            this.mDoubleLteRsrp = doubleSS.mDoubleLteRsrp;
            this.mDoubleLteRssnr = doubleSS.mDoubleLteRssnr;
            this.mDoubleCdmaDbm = doubleSS.mDoubleCdmaDbm;
            this.mDoubleCdmaEcio = doubleSS.mDoubleCdmaEcio;
            this.mDoubleEvdoDbm = doubleSS.mDoubleEvdoDbm;
            this.mDoubleEvdoEcio = doubleSS.mDoubleEvdoEcio;
            this.mDoubleEvdoSnr = doubleSS.mDoubleEvdoSnr;
            this.mTechState = doubleSS.mTechState;
            this.mOldDoubleLteRsrp = doubleSS.mOldDoubleLteRsrp;
            this.mOldDoubleLteRssnr = doubleSS.mOldDoubleLteRssnr;
            this.mOldDoubleCdmaDbm = doubleSS.mOldDoubleCdmaDbm;
            this.mOldDoubleCdmaEcio = doubleSS.mOldDoubleCdmaEcio;
            this.mOldDoubleEvdoDbm = doubleSS.mOldDoubleEvdoDbm;
            this.mOldDoubleEvdoEcio = doubleSS.mOldDoubleEvdoEcio;
            this.mOldDoubleEvdoSnr = doubleSS.mOldDoubleEvdoSnr;
            this.mDelayTime = doubleSS.mDelayTime;
        }

        public double getDoubleLteRsrp() {
            return this.mDoubleLteRsrp;
        }

        public double getDoubleLteRssnr() {
            return this.mDoubleLteRssnr;
        }

        public double getDoubleCdmaDbm() {
            return this.mDoubleCdmaDbm;
        }

        public double getDoubleCdmaEcio() {
            return this.mDoubleCdmaEcio;
        }

        public double getDoubleEvdoDbm() {
            return this.mDoubleEvdoDbm;
        }

        public double getDoubleEvdoEcio() {
            return this.mDoubleEvdoEcio;
        }

        public double getDoubleEvdoSnr() {
            return this.mDoubleEvdoSnr;
        }

        public double getOldDoubleLteRsrp() {
            return this.mOldDoubleLteRsrp;
        }

        public double getOldDoubleLteRssnr() {
            return this.mOldDoubleLteRssnr;
        }

        public double getOldDoubleCdmaDbm() {
            return this.mOldDoubleCdmaDbm;
        }

        public double getOldDoubleCdmaEcio() {
            return this.mOldDoubleCdmaEcio;
        }

        public double getOldDoubleEvdoDbm() {
            return this.mOldDoubleEvdoDbm;
        }

        public double getOldDoubleEvdoEcio() {
            return this.mOldDoubleEvdoEcio;
        }

        public double getOldDoubleEvdoSnr() {
            return this.mOldDoubleEvdoSnr;
        }

        public boolean processLteRsrpAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldRsrp = oldDoubleSS.getDoubleLteRsrp();
            double modemLteRsrp = (double) modemSS.getLteRsrp();
            this.mOldDoubleLteRsrp = oldRsrp;
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]Before processLteRsrpAlaphFilter -- old : " + oldRsrp + "; instant new : " + modemLteRsrp);
            if (modemLteRsrp >= -1.0d) {
                modemLteRsrp = (double) HwCdmaServiceStateManager.LTE_STRENGTH_POOR_STD;
            }
            if (oldRsrp <= modemLteRsrp) {
                this.mDoubleLteRsrp = modemLteRsrp;
            } else if (needProcessDescend) {
                this.mDoubleLteRsrp = ((7.0d * oldRsrp) + (5.0d * modemLteRsrp)) / 12.0d;
            } else {
                this.mDoubleLteRsrp = oldRsrp;
            }
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]LteRsrpAlaphFilter modem : " + modemLteRsrp + "; old : " + oldRsrp + "; new : " + this.mDoubleLteRsrp);
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
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]Before processLteRssnrAlaphFilter -- old : " + oldRssnr + "; instant new : " + modemLteRssnr);
            if (modemLteRssnr == 99.0d) {
                modemLteRssnr = (double) HwCdmaServiceStateManager.LTE_RSSNR_POOR_STD;
            }
            if (oldRssnr <= modemLteRssnr) {
                this.mDoubleLteRssnr = modemLteRssnr;
            } else if (needProcessDescend) {
                this.mDoubleLteRssnr = (VALUE_OLD_COEF_QUA_DES_SS * oldRssnr) + (VALUE_NEW_COEF_QUA_DES_SS * modemLteRssnr);
            } else {
                this.mDoubleLteRssnr = oldRssnr;
            }
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]LteRssnrAlaphFilter modem : " + modemLteRssnr + "; old : " + oldRssnr + "; new : " + this.mDoubleLteRssnr);
            if (this.mDoubleLteRssnr - modemLteRssnr <= -1.0d || this.mDoubleLteRssnr - modemLteRssnr >= 1.0d) {
                newSS.setLteRssnr((int) this.mDoubleLteRssnr);
                return true;
            }
            this.mDoubleLteRssnr = modemLteRssnr;
            newSS.setLteRssnr((int) this.mDoubleLteRssnr);
            return false;
        }

        public boolean processCdmaDbmAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldCdmaDbm = oldDoubleSS.getDoubleCdmaDbm();
            double modemCdmaDbm = (double) modemSS.getCdmaDbm();
            this.mOldDoubleCdmaDbm = oldCdmaDbm;
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]Before processCdmaDbmAlaphFilter -- old : " + oldCdmaDbm + "; instant new : " + modemCdmaDbm);
            if (modemCdmaDbm > -1.0d) {
                modemCdmaDbm = (double) HwCdmaServiceStateManager.CDMA_STRENGTH_POOR_STD;
            }
            if (oldCdmaDbm <= modemCdmaDbm) {
                this.mDoubleCdmaDbm = modemCdmaDbm;
            } else if (needProcessDescend) {
                this.mDoubleCdmaDbm = ((7.0d * oldCdmaDbm) + (5.0d * modemCdmaDbm)) / 12.0d;
            } else {
                this.mDoubleCdmaDbm = oldCdmaDbm;
            }
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]CdmaDbmAlaphFilter modem : " + modemCdmaDbm + "; old : " + oldCdmaDbm + "; new : " + this.mDoubleCdmaDbm);
            if (this.mDoubleCdmaDbm - modemCdmaDbm <= -1.0d || this.mDoubleCdmaDbm - modemCdmaDbm >= 1.0d) {
                newSS.setCdmaDbm((int) this.mDoubleCdmaDbm);
                return true;
            }
            this.mDoubleCdmaDbm = modemCdmaDbm;
            newSS.setCdmaDbm((int) this.mDoubleCdmaDbm);
            return false;
        }

        public boolean processCdmaEcioAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldCdmaEcio = oldDoubleSS.getDoubleCdmaEcio();
            double modemCdmaEcio = (double) modemSS.getCdmaEcio();
            this.mOldDoubleCdmaEcio = oldCdmaEcio;
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]Before processCdmaEcioAlaphFilter -- old : " + oldCdmaEcio + "; instant new : " + modemCdmaEcio);
            if (oldCdmaEcio <= modemCdmaEcio) {
                this.mDoubleCdmaEcio = modemCdmaEcio;
            } else if (needProcessDescend) {
                this.mDoubleCdmaEcio = (VALUE_OLD_COEF_QUA_DES_SS * oldCdmaEcio) + (VALUE_NEW_COEF_QUA_DES_SS * modemCdmaEcio);
            } else {
                this.mDoubleCdmaEcio = oldCdmaEcio;
            }
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]CdmaEcioAlaphFilter modem : " + modemCdmaEcio + "; old : " + oldCdmaEcio + "; new : " + this.mDoubleCdmaEcio);
            if (this.mDoubleCdmaEcio - modemCdmaEcio <= -1.0d || this.mDoubleCdmaEcio - modemCdmaEcio >= 1.0d) {
                newSS.setCdmaEcio((int) this.mDoubleCdmaEcio);
                return true;
            }
            this.mDoubleCdmaEcio = modemCdmaEcio;
            newSS.setCdmaEcio((int) this.mDoubleCdmaEcio);
            return false;
        }

        public boolean processEvdoDbmAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldEvdoDbm = oldDoubleSS.getDoubleEvdoDbm();
            double modemEvdoDbm = (double) modemSS.getEvdoDbm();
            this.mOldDoubleEvdoDbm = oldEvdoDbm;
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]Before processEvdoDbmAlaphFilter -- old : " + oldEvdoDbm + "; instant new : " + modemEvdoDbm);
            if (modemEvdoDbm > -1.0d) {
                modemEvdoDbm = (double) HwCdmaServiceStateManager.CDMA_STRENGTH_POOR_STD;
            }
            if (oldEvdoDbm <= modemEvdoDbm) {
                this.mDoubleEvdoDbm = modemEvdoDbm;
            } else if (needProcessDescend) {
                this.mDoubleEvdoDbm = ((7.0d * oldEvdoDbm) + (5.0d * modemEvdoDbm)) / 12.0d;
            } else {
                this.mDoubleEvdoDbm = oldEvdoDbm;
            }
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]EvdoDbmAlaphFilter modem : " + modemEvdoDbm + "; old : " + oldEvdoDbm + "; new : " + this.mDoubleEvdoDbm);
            if (this.mDoubleEvdoDbm - modemEvdoDbm <= -1.0d || this.mDoubleEvdoDbm - modemEvdoDbm >= 1.0d) {
                newSS.setEvdoDbm((int) this.mDoubleEvdoDbm);
                return true;
            }
            this.mDoubleEvdoDbm = modemEvdoDbm;
            newSS.setEvdoDbm((int) this.mDoubleEvdoDbm);
            return false;
        }

        public boolean processEvdoEcioAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldEvdoEcio = oldDoubleSS.getDoubleEvdoEcio();
            double modemEvdoEcio = (double) modemSS.getEvdoEcio();
            this.mOldDoubleEvdoEcio = oldEvdoEcio;
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]Before processEvdoEcioAlaphFilter -- old : " + oldEvdoEcio + "; instant new : " + modemEvdoEcio);
            if (oldEvdoEcio <= modemEvdoEcio) {
                this.mDoubleEvdoEcio = modemEvdoEcio;
            } else if (needProcessDescend) {
                this.mDoubleEvdoEcio = (VALUE_OLD_COEF_QUA_DES_SS * oldEvdoEcio) + (VALUE_NEW_COEF_QUA_DES_SS * modemEvdoEcio);
            } else {
                this.mDoubleEvdoEcio = oldEvdoEcio;
            }
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]EvdoEcioAlaphFilter modem : " + modemEvdoEcio + "; old : " + oldEvdoEcio + "; new : " + this.mDoubleEvdoEcio);
            if (this.mDoubleEvdoEcio - modemEvdoEcio <= -1.0d || this.mDoubleEvdoEcio - modemEvdoEcio >= 1.0d) {
                newSS.setEvdoEcio((int) this.mDoubleEvdoEcio);
                return true;
            }
            this.mDoubleEvdoEcio = modemEvdoEcio;
            newSS.setEvdoEcio((int) this.mDoubleEvdoEcio);
            return false;
        }

        public boolean processEvdoSnrAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldEvdoSnr = oldDoubleSS.getDoubleEvdoSnr();
            double modemEvdoSnr = (double) modemSS.getEvdoSnr();
            this.mOldDoubleEvdoSnr = oldEvdoSnr;
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]Before processEvdoSnrAlaphFilter -- old : " + oldEvdoSnr + "; instant new : " + modemEvdoSnr);
            if (oldEvdoSnr <= modemEvdoSnr) {
                this.mDoubleEvdoSnr = modemEvdoSnr;
            } else if (needProcessDescend) {
                this.mDoubleEvdoSnr = (VALUE_OLD_COEF_QUA_DES_SS * oldEvdoSnr) + (VALUE_NEW_COEF_QUA_DES_SS * modemEvdoSnr);
            } else {
                this.mDoubleEvdoSnr = oldEvdoSnr;
            }
            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]EvdoSnrAlaphFilter modem : " + modemEvdoSnr + "; old : " + oldEvdoSnr + "; new : " + this.mDoubleEvdoSnr);
            if (this.mDoubleEvdoSnr - modemEvdoSnr <= -1.0d || this.mDoubleEvdoSnr - modemEvdoSnr >= 1.0d) {
                newSS.setEvdoSnr((int) this.mDoubleEvdoSnr);
                return true;
            }
            this.mDoubleEvdoSnr = modemEvdoSnr;
            newSS.setEvdoSnr((int) this.mDoubleEvdoSnr);
            return false;
        }

        public void proccessAlaphFilter(SignalStrength newSS, SignalStrength modemSS) {
            proccessAlaphFilter(this, newSS, modemSS, true);
        }

        public void proccessAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            int needUpdate = 0;
            if (oldDoubleSS == null) {
                Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]proccess oldDoubleSS is null");
                return;
            }
            if (newSS.isGsm()) {
                if ((this.mTechState & 4) != 0) {
                    needUpdate = processLteRsrpAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                    if (HwCdmaServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                        needUpdate |= processLteRssnrAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                    }
                }
            } else if ((this.mTechState & 16) != 0) {
                needUpdate = processEvdoDbmAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                if (HwCdmaServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                    needUpdate = (needUpdate | processEvdoEcioAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend)) | processEvdoSnrAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                }
            }
            if ((this.mTechState & 8) != 0) {
                needUpdate |= processCdmaDbmAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                if (HwCdmaServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                    needUpdate |= processCdmaEcioAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                }
            }
            int phoneType = TelephonyManager.getDefault().getCurrentPhoneType(HwCdmaServiceStateManager.this.mPhoneId);
            boolean isCDMAPhone = phoneType != 2 ? phoneType == 6 : true;
            if (isCDMAPhone || !HwCdmaServiceStateManager.this.hasMessages(HwCdmaServiceStateManager.EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH)) {
                setFakeSignalStrengthForSlowDescend(this, newSS);
                HwCdmaServiceStateManager.this.mSST.setSignalStrength(newSS);
                if (needUpdate != 0) {
                    HwCdmaServiceStateManager.this.sendMessageDelayUpdateSingalStrength(this.mDelayTime);
                }
                return;
            }
            HwCdmaServiceStateManager.this.removeMessages(HwCdmaServiceStateManager.EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH);
        }

        private void setFakeSignalStrengthForSlowDescend(DoubleSignalStrength oldDoubleSS, SignalStrength newSS) {
            int oldLevel;
            int newLevel;
            int diffLevel;
            SignalThreshold signalThreshold;
            int lowerLevel;
            this.mDelayTime = 0;
            if (newSS.isGsm()) {
                if ((this.mTechState & 4) != 0) {
                    oldLevel = HwCdmaServiceStateManager.mHwSigStr.getLevel(SignalType.CDMALTE, (int) oldDoubleSS.getOldDoubleLteRsrp(), (int) oldDoubleSS.getOldDoubleLteRssnr());
                    newLevel = HwCdmaServiceStateManager.mHwSigStr.getLevel(SignalType.CDMALTE, newSS.getLteRsrp(), newSS.getLteRssnr());
                    diffLevel = oldLevel - newLevel;
                    Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]CDMALTE oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                    if (diffLevel > 1) {
                        signalThreshold = HwCdmaServiceStateManager.mHwSigStr.getSignalThreshold(SignalType.CDMALTE);
                        if (signalThreshold != null) {
                            lowerLevel = oldLevel - 1;
                            int lteRsrp = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                            if (-1 != lteRsrp) {
                                this.mDoubleLteRsrp = (double) lteRsrp;
                                newSS.setLteRsrp(lteRsrp);
                                this.mDelayTime = HwCdmaServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                            }
                            Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]CDMALTE lowerLevel: " + lowerLevel + ", lteRsrp: " + lteRsrp);
                            if (HwCdmaServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                                int lteRssnr = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true);
                                if (-1 != lteRssnr) {
                                    this.mDoubleLteRssnr = (double) lteRssnr;
                                    newSS.setLteRssnr(lteRssnr);
                                    this.mDelayTime = HwCdmaServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                                }
                            }
                        }
                    }
                }
            } else if ((this.mTechState & 16) != 0) {
                oldLevel = HwCdmaServiceStateManager.mHwSigStr.getLevel(SignalType.EVDO, (int) oldDoubleSS.getOldDoubleEvdoDbm(), (int) oldDoubleSS.getOldDoubleEvdoSnr());
                newLevel = HwCdmaServiceStateManager.mHwSigStr.getLevel(SignalType.EVDO, newSS.getEvdoDbm(), newSS.getEvdoSnr());
                diffLevel = oldLevel - newLevel;
                Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]EVDO oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1) {
                    signalThreshold = HwCdmaServiceStateManager.mHwSigStr.getSignalThreshold(SignalType.EVDO);
                    if (signalThreshold != null) {
                        lowerLevel = oldLevel - 1;
                        int evdoDbm = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                        if (-1 != evdoDbm) {
                            this.mDoubleEvdoDbm = (double) evdoDbm;
                            newSS.setEvdoDbm(evdoDbm);
                            this.mDelayTime = HwCdmaServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                        }
                        Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]EVDO lowerLevel: " + lowerLevel + ", evdoDbm: " + evdoDbm);
                        if (HwCdmaServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                            int evdoSnr = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true);
                            if (-1 != evdoSnr) {
                                this.mDoubleEvdoSnr = (double) evdoSnr;
                                newSS.setEvdoSnr(evdoSnr);
                                this.mDelayTime = HwCdmaServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                            }
                        }
                    }
                }
            }
            if ((this.mTechState & 8) != 0) {
                oldLevel = HwCdmaServiceStateManager.mHwSigStr.getLevel(SignalType.CDMA, (int) oldDoubleSS.getOldDoubleCdmaDbm(), (int) oldDoubleSS.getOldDoubleCdmaEcio());
                newLevel = HwCdmaServiceStateManager.mHwSigStr.getLevel(SignalType.CDMA, newSS.getCdmaDbm(), newSS.getCdmaEcio());
                diffLevel = oldLevel - newLevel;
                Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]CDMA oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1) {
                    signalThreshold = HwCdmaServiceStateManager.mHwSigStr.getSignalThreshold(SignalType.CDMA);
                    if (signalThreshold != null) {
                        lowerLevel = oldLevel - 1;
                        int cdmaDbm = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                        if (-1 != cdmaDbm) {
                            this.mDoubleCdmaDbm = (double) cdmaDbm;
                            newSS.setCdmaDbm(cdmaDbm);
                            this.mDelayTime = HwCdmaServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                        }
                        Rlog.d(HwCdmaServiceStateManager.LOG_TAG, "SUB[" + HwCdmaServiceStateManager.this.mPhoneId + "]CDMA lowerLevel: " + lowerLevel + ", cdmaDbm: " + cdmaDbm);
                        if (HwCdmaServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                            int cdmaEcio = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true);
                            if (-1 != cdmaEcio) {
                                this.mDoubleCdmaEcio = (double) cdmaEcio;
                                newSS.setCdmaEcio(cdmaEcio);
                                this.mDelayTime = HwCdmaServiceStateManager.VALUE_DELAY_DURING_TIME / diffLevel;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isHisiDsdsCardDeactived(ServiceState serviceState) {
        boolean airplaneModeOn = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        boolean bDSDS = MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration();
        if (airplaneModeOn || !IS_HISI_PLATFORM || ((!bDSDS && !SET_UICC_BY_RADIO_POWER) || serviceState == null || 3 != serviceState.getVoiceRegState())) {
            return false;
        }
        log("CT card is deactived on hisi dsds, should display out of service.");
        return true;
    }

    private ServiceState getSS() {
        return this.mSST.mSS;
    }

    private void updateSpnDisplay() {
        this.mSST.updateSpnDisplay();
    }

    public HwCdmaServiceStateManager(ServiceStateTracker cdmaServiceStateTracker, GsmCdmaPhone phone) {
        super(cdmaServiceStateTracker, phone);
        this.mSST = cdmaServiceStateTracker;
        this.mPhone = phone;
        this.mContext = this.mPhone.getContext();
        add_ACTION_LOCALE_CHANGED();
        this.mMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        this.mCust = (HwCustCdmaServiceStateManager) HwCustUtils.createObj(HwCustCdmaServiceStateManager.class, new Object[0]);
        this.mPhone.mCi.registerForCaStateChanged(this, EVENT_CA_STATE_CHANGED, null);
        this.mPhone.mCi.registerForCrrConn(this, EVENT_CRR_CONN, null);
        this.mPhone.mCi.setOnRegPLMNSelInfo(this, EVENT_PLMN_SELINFO, null);
        registerCloudOtaBroadcastReceiver();
        this.mPhone.mCi.registerForRplmnsStateChanged(this, EVENT_RPLMNS_STATE_CHANGED, null);
    }

    public HwCdmaServiceStateManager(ServiceStateTracker cdmaServiceStateTracker, GsmCdmaPhone phone, CellInfo cellInfo) {
        super(cdmaServiceStateTracker, phone);
        this.mSST = cdmaServiceStateTracker;
        this.mPhone = phone;
        this.mContext = this.mPhone.getContext();
        this.mPhoneId = phone.getPhoneId();
        add_ACTION_LOCALE_CHANGED();
        this.mMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        this.mCust = (HwCustCdmaServiceStateManager) HwCustUtils.createObj(HwCustCdmaServiceStateManager.class, new Object[0]);
        this.mPhone.mCi.registerForCaStateChanged(this, EVENT_CA_STATE_CHANGED, null);
        this.mPhone.mCi.registerForCrrConn(this, EVENT_CRR_CONN, null);
        this.mPhone.mCi.setOnRegPLMNSelInfo(this, EVENT_PLMN_SELINFO, null);
        registerCloudOtaBroadcastReceiver();
    }

    private void add_ACTION_LOCALE_CHANGED() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter);
    }

    private boolean isInvalidPlmn(String mccmnc) {
        return -1 != INVAILD_PLMN.indexOf(new StringBuilder().append(mccmnc).append("-").toString());
    }

    public String getRplmn() {
        return this.rplmn;
    }

    public String getPlmn() {
        String operatorNumeric = getSS().getOperatorNumeric();
        if (!TextUtils.isEmpty(operatorNumeric) && isInvalidPlmn(operatorNumeric)) {
            int systemId = getSS().getSystemId();
            boolean isSystemIdInCTRange = (systemId < CT_SID_1st_START || systemId > CT_SID_1st_END) ? systemId >= CT_SID_2nd_START && systemId <= CT_SID_2nd_END : true;
            if (isSystemIdInCTRange) {
                operatorNumeric = "46003";
            }
        }
        String data = null;
        try {
            data = System.getString(this.mContext.getContentResolver(), "plmn");
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
        if (plmnValue != null) {
            getSS().setOperatorAlphaLong(plmnValue);
        } else {
            plmnValue = getSS().getOperatorAlphaLong();
            int voiceRat = getSS().getRilVoiceRadioTechnology();
            boolean isCDMATec = (!SPRINT_OPERATOR.equals(mCdmaHomeOperatorNumeric) || (TextUtils.isEmpty(operatorNumeric) ^ 1) == 0) ? false : (4 == voiceRat || 5 == voiceRat) ? true : 6 == voiceRat;
            if (isCDMATec) {
                for (String startsWith : usa_mcc_list) {
                    if (operatorNumeric.startsWith(startsWith)) {
                        plmnValue = SPRINT_OPERATOR_ALPHA_LONG;
                        break;
                    }
                }
            }
        }
        if (getCombinedRegState(getSS()) != 1 ? isHisiDsdsCardDeactived(getSS()) : true) {
            plmnValue = Resources.getSystem().getText(17040278).toString();
            log("CDMA is out of service. plmnValue = " + plmnValue);
        } else if (HwPlmnActConcat.needPlmnActConcat()) {
            plmnValue = HwPlmnActConcat.getPlmnActConcat(plmnValue, getSS());
        }
        if (this.mCust == null) {
            return plmnValue;
        }
        return this.mCust.setEriBasedPlmn(this.mPhone, plmnValue);
    }

    public OnsDisplayParams getOnsDisplayParamsHw(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        plmn = getPlmn();
        OnsDisplayParams odp = new OnsDisplayParams(showSpn, plmn != null, rule, plmn, spn);
        if (this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().isWifiCallingEnabled()) {
            odp = getOnsDisplayParamsForVoWifi(odp);
        }
        this.mCurShowWifi = odp.mShowWifi;
        this.mCurWifi = odp.mWifi;
        return odp;
    }

    private void log(String string) {
        Rlog.d(LOG_TAG, string);
    }

    public void setAutoTimeAndZoneForCdma(int rt) {
        log("setAutoTimeAndZoneForCdma begin");
        if (ServiceState.isCdma(rt)) {
            Global.putInt(this.mPhone.getContext().getContentResolver(), "auto_time", 1);
            Global.putInt(this.mPhone.getContext().getContentResolver(), "auto_time_zone", 1);
        }
    }

    public void dispose() {
        if (this.mPhone != null) {
            this.mPhone.mCi.unregisterForCrrConn(this);
            this.mPhone.mCi.unSetOnRegPLMNSelInfo(this);
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
            this.mPhone.getContext().unregisterReceiver(this.mCloudOtaBroadcastReceiver);
            this.mPhone.mCi.unregisterForRplmnsStateChanged(this);
        }
    }

    private void sendBroadcastCrrConnInd(int modem0, int modem1, int modem2) {
        Rlog.d(LOG_TAG, "CDMA sendBroadcastCrrConnInd");
        String MODEM0 = "modem0";
        String MODEM1 = "modem1";
        String MODEM2 = "modem2";
        Intent intent = new Intent("com.huawei.action.ACTION_HW_CRR_CONN_IND");
        intent.putExtra("modem0", modem0);
        intent.putExtra("modem1", modem1);
        intent.putExtra("modem2", modem2);
        Rlog.d(LOG_TAG, "modem0: " + modem0 + " modem1: " + modem1 + " modem2: " + modem2);
        this.mPhone.getContext().sendBroadcast(intent, "com.huawei.permission.HUAWEI_BUSSINESS_PERMISSION");
    }

    private void sendBroadcastRegPLMNSelInfo(int flag, int result) {
        Rlog.d(LOG_TAG, "sendBroadcastRegPLMNSelInfo");
        String SUB_ID = HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID;
        String FLAG = "flag";
        String RES = "res";
        Intent intent = new Intent("com.huawei.action.SIM_PLMN_SELINFO");
        int subId = this.mPhone.getPhoneId();
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        intent.putExtra("flag", flag);
        intent.putExtra("res", result);
        Rlog.d(LOG_TAG, "subId: " + subId + " flag: " + flag + " result: " + result);
        this.mPhone.getContext().sendBroadcast(intent, "com.huawei.permission.HUAWEI_BUSSINESS_PERMISSION");
    }

    public void processCTNumMatch(boolean roaming, UiccCardApplication uiccCardApplication) {
        log("processCTNumMatch, roaming: " + roaming);
        if (IS_CHINATELECOM && uiccCardApplication != null && AppState.APPSTATE_READY == uiccCardApplication.getState()) {
            int slotId = HwAddonTelephonyFactory.getTelephony().getDefault4GSlotId();
            log("processCTNumMatch->getDefault4GSlotId, slotId: " + slotId);
            if (roaming || (HwTelephonyManagerInner.getDefault().isCTCdmaCardInGsmMode() && uiccCardApplicationUtils.getUiccCard(uiccCardApplication) == UiccController.getInstance().getUiccCard(slotId))) {
                log("processCTNumMatch, roaming or isCTCdmaCardInGsmMode..");
                setCTNumMatchRoamingForSlot(slotId);
                return;
            }
            setCTNumMatchHomeForSlot(slotId);
        }
    }

    public boolean updateCTRoaming(ServiceState newSS, boolean cdmaRoaming) {
        if (HwTelephonyManagerInner.getDefault().isChinaTelecom(SubscriptionController.getInstance().getSlotIndex(this.mPhone.getSubId()))) {
            setCTRoaming(newSS);
            cdmaRoaming = newSS.getRoaming();
            if (cdmaRoaming) {
                newSS.setCdmaEriIconIndex(0);
                newSS.setCdmaEriIconMode(0);
            } else {
                newSS.setCdmaEriIconIndex(1);
                newSS.setCdmaEriIconMode(0);
            }
        }
        return cdmaRoaming;
    }

    private void setCTRoaming(ServiceState newSS) {
        IccRecords iccRecords = (IccRecords) this.mPhone.mIccRecords.get();
        if (iccRecords == null) {
            log("setCTRoaming iccRecords is null");
            newSS.setRoaming(false);
            return;
        }
        String plmn = newSS.getOperatorNumeric();
        String hplmn = iccRecords.getOperatorNumeric();
        String nw_mcc = getMccFromPlmn(plmn);
        String sim_mcc = getMccFromPlmn(hplmn);
        int sid = newSS.getSystemId();
        log("setCTRoaming: plmn = " + plmn + ", hplmn = " + hplmn + ", sid = " + sid);
        if (plmn != null && plmn.length() >= 5 && -1 == INVAILD_PLMN.indexOf(plmn.trim() + "-")) {
            boolean isSim_mccInvaild = !TextUtils.isEmpty(sim_mcc) ? sim_mcc.equals(nw_mcc) ^ 1 : false;
            log("setCTRoaming: setRoaming" + isSim_mccInvaild);
            newSS.setRoaming(isSim_mccInvaild);
        } else if (sid == 0 || -1 == sid) {
            newSS.setRoaming(false);
        } else if ((CT_SID_1st_START <= sid && sid <= CT_SID_1st_END) || (CT_SID_2nd_START <= sid && sid <= CT_SID_2nd_END)) {
            newSS.setRoaming(!TextUtils.isEmpty(sim_mcc) ? CT_MCC.equals(sim_mcc) ^ 1 : false);
        } else if (CT_MACAO_SID_START > sid || sid > CT_MACAO_SID_END) {
            log("setCTRoaming sid is not in the specified range");
            newSS.setRoaming(true);
        } else {
            newSS.setRoaming(!TextUtils.isEmpty(sim_mcc) ? CT_MACAO_MCC.equals(sim_mcc) ^ 1 : false);
        }
    }

    private String getMccFromPlmn(String plmn) {
        if (TextUtils.isEmpty(plmn) || plmn.length() < 3) {
            return "";
        }
        return plmn.substring(0, 3);
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        int[] response;
        switch (msg.what) {
            case EVENT_DELAY_UPDATE_REGISTER_STATE_DONE /*102*/:
                Rlog.d(LOG_TAG, "[Phone" + this.mPhoneId + "]Delay Timer expired, begin get register state");
                this.mRefreshState = true;
                this.mSST.pollState();
                return;
            case EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH /*104*/:
                Rlog.d(LOG_TAG, "event update cdma&lte signal strength");
                this.mDoubleSignalStrength.proccessAlaphFilter(this.mSST.getSignalStrength(), this.mModemSignalStrength);
                this.mPhone.notifySignalStrength();
                return;
            case EVENT_CA_STATE_CHANGED /*105*/:
                ar = msg.obj;
                if (ar.exception == null) {
                    onCaStateChanged(((int[]) ar.result)[0] == 1);
                    return;
                } else {
                    log("EVENT_CA_STATE_CHANGED: exception;");
                    return;
                }
            case EVENT_RPLMNS_STATE_CHANGED /*106*/:
                Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]EVENT_RPLMNS_STATE_CHANGED");
                if (SystemProperties.getBoolean("ro.config.hw_globalEcc", false) && SystemProperties.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
                    Rlog.d(LOG_TAG, "the global emergency numbers custom-make does enable!!!!");
                    toGetRplmnsThenSendEccNum();
                    return;
                }
                return;
            case EVENT_CRR_CONN /*153*/:
                Rlog.d(LOG_TAG, "CDMA EVENT_CRR_CONN");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    response = ar.result;
                    if (response.length != 0) {
                        sendBroadcastCrrConnInd(response[0], response[1], response[2]);
                        return;
                    }
                    return;
                }
                Rlog.d(LOG_TAG, "CDMA EVENT_CRR_CONN: exception;");
                return;
            case EVENT_PLMN_SELINFO /*154*/:
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
            default:
                super.handleMessage(msg);
                return;
        }
    }

    public void toGetRplmnsThenSendEccNum() {
        String rplmns = "";
        String hplmn = TelephonyManager.getDefault().getSimOperator(this.mPhoneId);
        String forceEccState = SystemProperties.get(PROPERTY_GLOBAL_FORCE_TO_SET_ECC, "invalid");
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]GECC-toGetRplmnsThenSendEccNum: hplmn = " + hplmn + "; rplmn = " + this.rplmn + "; forceEccState = " + forceEccState);
        if (((IccCardProxy) PhoneFactory.getDefaultPhone().getIccCard()).getIccCardStateHW() || hplmn.equals("") || forceEccState.equals("usim_absent")) {
            rplmns = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
            if (!TextUtils.isEmpty(rplmns)) {
                this.mPhone.globalEccCustom(rplmns);
            }
        }
    }

    public static boolean isNetworkTypeChanged(SignalStrength oldSS, SignalStrength newSS) {
        int newState = 0;
        int oldState = 0;
        boolean result = false;
        if (oldSS.isGsm() != newSS.isGsm()) {
            result = true;
        } else {
            if (newSS.getCdmaDbm() < -1) {
                newState = 8;
            }
            if (oldSS.getCdmaDbm() < -1) {
                oldState = 8;
            }
            if (newSS.isGsm()) {
                if (newSS.getLteRsrp() < -1) {
                    newState |= 4;
                }
                if (oldSS.getLteRsrp() < -1) {
                    oldState |= 4;
                }
            } else {
                if (newSS.getEvdoDbm() < -1) {
                    newState |= 16;
                }
                if (oldSS.getEvdoDbm() < -1) {
                    oldState |= 16;
                }
            }
            if (!(newState == 0 || newState == oldState)) {
                result = true;
            }
        }
        Rlog.d(LOG_TAG, "isNetworkTypeChanged: " + result);
        return result;
    }

    public void sendMessageDelayUpdateSingalStrength(int time) {
        Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]sendMessageDelayUpdateSingalStrength, time: " + time);
        Message msg = obtainMessage();
        msg.what = EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH;
        if (time == 0) {
            sendMessageDelayed(msg, (long) mDelayDuringTimeLte);
        } else {
            sendMessageDelayed(msg, (long) time);
        }
    }

    public boolean notifySignalStrength(SignalStrength oldSS, SignalStrength newSS) {
        boolean notified;
        this.mModemSignalStrength = new SignalStrength(newSS);
        Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]Process notify signal strenght! ver.02");
        if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]In delay update register state process, no notify signal");
        }
        if (isNetworkTypeChanged(oldSS, newSS)) {
            Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "] Network is changed immediately!");
            if (hasMessages(EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH)) {
                removeMessages(EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH);
            }
            this.mDoubleSignalStrength = new DoubleSignalStrength(newSS);
            this.mSST.setSignalStrength(newSS);
            notified = true;
        } else if (hasMessages(EVENT_DELAY_UPDATE_CDMA_SIGNAL_STRENGTH)) {
            Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]has delay update message, don't proccess alpha filter immediately!");
            notified = false;
        } else {
            this.mOldDoubleSignalStrength = this.mDoubleSignalStrength;
            this.mDoubleSignalStrength = new DoubleSignalStrength(newSS);
            this.mDoubleSignalStrength.proccessAlaphFilter(this.mOldDoubleSignalStrength, newSS, this.mModemSignalStrength, false);
            notified = true;
        }
        if (notified) {
            try {
                Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]notifySignalStrength.");
                this.mPhone.notifySignalStrength();
            } catch (NullPointerException ex) {
                Rlog.e(LOG_TAG, "onSignalStrengthResult() Phone already destroyed: " + ex + "SignalStrength not notified");
            }
        }
        return notified;
    }

    private void cancelDeregisterStateDelayTimer() {
        if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            Rlog.d(LOG_TAG, "[SUB" + this.mPhone.getPhoneId() + "]cancelDeregisterStateDelayTimer");
            removeMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE);
        }
    }

    private void delaySendDeregisterStateChange(int delayTime) {
        if (!hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            Message msg = obtainMessage();
            msg.what = EVENT_DELAY_UPDATE_REGISTER_STATE_DONE;
            sendMessageDelayed(msg, (long) delayTime);
            Rlog.d(LOG_TAG, "[SUB" + this.mPhone.getPhoneId() + "]RegisterStateChange timer is running,do nothing");
        }
    }

    public boolean proccessCdmaLteDelayUpdateRegisterStateDone(ServiceState oldSS, ServiceState newSS) {
        if (HwModemCapability.isCapabilitySupport(6)) {
            return false;
        }
        boolean lostNetwork = oldSS.getDataRegState() == 0 ? newSS.getDataRegState() != 0 : false;
        boolean lostCSNetwork = (oldSS.getDataRegState() == 0 || newSS.getDataRegState() == 0) ? false : oldSS.getVoiceRegState() == 0 ? newSS.getVoiceRegState() != 0 : false;
        int isCmccLostCSNetwork = IS_CMCC_4G_DSDX_ENABLE ? lostCSNetwork : 0;
        State mExternalState = this.mPhone.getIccCard().getState();
        boolean isSubDeactivated = SubscriptionController.getInstance().getSubState(this.mPhone.getSubId()) == 0;
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        int userPref4GSlot = HwAllInOneController.getInstance().getUserPref4GSlot();
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhone.getPhoneId() + "]process delay update register state lostNetwork : " + lostNetwork + ", lostCSNetwork : " + lostCSNetwork + ", desiredPowerState : " + this.mSST.getDesiredPowerState() + ", radiostate : " + this.mPhone.mCi.getRadioState() + ", mRadioOffByDoRecovery : " + this.mSST.getDoRecoveryTriggerState() + ", isSubDeactivated : " + isSubDeactivated + ", newMainSlot : " + newMainSlot + ", userPref4GSlot : " + userPref4GSlot + ", mExternalState : " + mExternalState);
        if (newSS.getDataRegState() == 0 || (isCmccLostCSNetwork ^ 1) != 0 || (this.mSST.getDesiredPowerState() ^ 1) != 0 || this.mPhone.mCi.getRadioState() == RadioState.RADIO_OFF || this.mSST.getDoRecoveryTriggerState() || isCardInvalid(isSubDeactivated, this.mPhone.getSubId()) || this.mMainSlot != userPref4GSlot || newSS.getDataRegState() == 3 || mExternalState == State.PUK_REQUIRED) {
            this.mMainSlot = newMainSlot;
            cancelDeregisterStateDelayTimer();
        } else if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            return true;
        } else {
            if ((!lostNetwork ? lostCSNetwork : true) && !this.mRefreshState) {
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
        boolean isPsLostNetwork = oldSS.getDataRegState() == 0 ? newSS.getDataRegState() != 0 : false;
        int delayedTime = 0;
        TelephonyManager.getDefault();
        int networkClass = TelephonyManager.getNetworkClass(oldSS.getNetworkType());
        if (!isPsLostNetwork) {
            boolean isCsLostNetwork = oldSS.getVoiceRegState() == 0 ? newSS.getVoiceRegState() != 0 : false;
            if (isCsLostNetwork) {
                delayedTime = DELAYED_TIME_DEFAULT_VALUE * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
            }
        } else if (networkClass == 3) {
            delayedTime = DELAYED_TIME_NETWORKSTATUS_PS_4G;
        } else {
            delayedTime = DELAYED_TIME_DEFAULT_VALUE * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
        }
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhone.getPhoneId() + "] delay time = " + delayedTime);
        return delayedTime;
    }

    public int getCARilRadioType(int type) {
        int radioType = type;
        if (SHOW_4G_PLUS_ICON && 19 == type) {
            radioType = 14;
        }
        Rlog.d(LOG_TAG, "[CA] CA updateCAStatus  radioType=" + radioType + " type=" + type);
        return radioType;
    }

    public int updateCAStatus(int currentType) {
        int newType = currentType;
        if (SHOW_4G_PLUS_ICON) {
            Rlog.d(LOG_TAG, "[CA] currentType=" + currentType + "    oldCAstate=" + this.mOldCAstate);
            boolean HasCAactivated = 19 == currentType && 19 != this.mOldCAstate;
            boolean HasCAdeActivated = 19 != currentType && 19 == this.mOldCAstate;
            this.mOldCAstate = currentType;
            if (HasCAactivated || HasCAdeActivated) {
                Intent intentLteCAState = new Intent("com.huawei.intent.action.LTE_CA_STATE");
                intentLteCAState.putExtra("subscription", this.mPhone.getSubId());
                if (HasCAactivated) {
                    intentLteCAState.putExtra("LteCAstate", true);
                    Rlog.d(LOG_TAG, "[CA] CA activated !");
                } else if (HasCAdeActivated) {
                    intentLteCAState.putExtra("LteCAstate", false);
                    Rlog.d(LOG_TAG, "[CA] CA deactivated !");
                }
                this.mPhone.getContext().sendBroadcast(intentLteCAState);
            }
        }
        return currentType;
    }

    private void onCaStateChanged(boolean caActive) {
        Rlog.d(LOG_TAG, "onCaStateChanged caActive=" + caActive);
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
            intentLteCAState.putExtra("subscription", this.mPhone.getSubId());
            intentLteCAState.putExtra("LteCAstate", caActive);
            this.mPhone.getContext().sendBroadcast(intentLteCAState);
        }
    }

    private void registerCloudOtaBroadcastReceiver() {
        Rlog.e(LOG_TAG, "HwCloudOTAService registerCloudOtaBroadcastReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction("cloud.ota.mcc.UPDATE");
        filter.addAction("cloud.ota.dplmn.UPDATE");
        this.mPhone.getContext().registerReceiver(this.mCloudOtaBroadcastReceiver, filter, "huawei.permission.RECEIVE_CLOUD_OTA_UPDATA", null);
    }
}
