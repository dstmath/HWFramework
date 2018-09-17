package com.android.internal.telephony.gsm;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.HwTelephony.NumMatchs;
import android.provider.HwTelephony.VirtualNets;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.CarrierConfigManager;
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
import com.android.internal.telephony.HwTelephonyFactory;
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
import com.android.internal.telephony.vsim.HwChrVSimConstants;
import com.android.internal.telephony.vsim.HwVSimConstants;
import huawei.cust.HwCustUtils;
import java.util.HashSet;
import java.util.Locale;

public class HwGsmServiceStateManager extends HwServiceStateManager {
    private static final int CDMA_LEVEL = 8;
    private static final String CHINAMOBILE_MCCMNC = "46000;46002;46007;46008;46004";
    private static final String CHINA_OPERATOR_MCC = "460";
    public static final String CLOUD_OTA_DPLMN_UPDATE = "cloud.ota.dplmn.UPDATE";
    public static final String CLOUD_OTA_MCC_UPDATE = "cloud.ota.mcc.UPDATE";
    public static final String CLOUD_OTA_PERMISSION = "huawei.permission.RECEIVE_CLOUD_OTA_UPDATA";
    private static final int EVDO_LEVEL = 16;
    private static final int EVENT_CA_STATE_CHANGED = 104;
    private static final int EVENT_CRR_CONN = 152;
    private static final int EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH = 103;
    private static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 102;
    private static final int EVENT_NETWORK_REJECTED_CASE = 105;
    private static final int EVENT_PLMN_SELINFO = 151;
    private static final int EVENT_POLL_LOCATION_INFO = 64;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 101;
    private static final String EXTRA_SHOW_WIFI = "showWifi";
    private static final String EXTRA_WIFI = "wifi";
    private static final int GSM_LEVEL = 1;
    private static final int GSM_STRENGTH_POOR_STD = -109;
    private static final String KEY_WFC_FORMAT_WIFI_STRING = "wfc_format_wifi_string";
    private static final String KEY_WFC_HIDE_WIFI_BOOL = "wfc_hide_wifi_bool";
    private static final String KEY_WFC_SPN_STRING = "wfc_spn_string";
    private static final String LOG_TAG = "HwGsmServiceStateManager";
    private static final int LTE_LEVEL = 4;
    private static final int LTE_RSSNR_POOR_STD = 0;
    private static final int LTE_RSSNR_UNKOUWN_STD = 99;
    private static final int LTE_STRENGTH_POOR_STD = 0;
    private static final int LTE_STRENGTH_UNKOUWN_STD = -44;
    private static final int L_RSSNR_POOR_STD = -5;
    private static final int L_STRENGTH_POOR_STD = -125;
    private static final int NETWORK_MODE_GSM_UMTS = 3;
    private static final Uri PREFERAPN_NO_UPDATE_URI = null;
    private static final String PROPERTY_GLOBAL_FORCE_TO_SET_ECC = "ril.force_to_set_ecc";
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final boolean PS_CLEARCODE = false;
    private static final boolean SHOW_4G_PLUS_ICON = false;
    private static final int UMTS_LEVEL = 2;
    private static final int VALUE_DELAY_DURING_TIME = 6000;
    private static final int WCDMA_ECIO_NONE = 255;
    private static final int WCDMA_ECIO_POOR_STD = 0;
    private static final int WCDMA_STRENGTH_POOR_STD = 0;
    private static final int WIFI_IDX = 1;
    private static final int W_ECIO_POOR_STD = -17;
    private static final int W_STRENGTH_POOR_STD = -112;
    private static final boolean isMultiSimEnabled = false;
    private static final int mDelayDuringTime = 0;
    private final boolean FEATURE_SIGNAL_DUALPARAM;
    CommandsInterface mCi;
    private CloudOtaBroadcastReceiver mCloudOtaBroadcastReceiver;
    private Context mContext;
    private ContentResolver mCr;
    private boolean mCurShowWifi;
    private String mCurWifi;
    private DoubleSignalStrength mDoubleSignalStrength;
    private GsmCdmaPhone mGsmPhone;
    private ServiceStateTracker mGsst;
    private HwCustGsmServiceStateManager mHwCustGsmServiceStateManager;
    private BroadcastReceiver mIntentReceiver;
    private SignalStrength mModemSignalStrength;
    private int mOldCAstate;
    private DoubleSignalStrength mOldDoubleSignalStrength;
    private int mPhoneId;
    private int mRac;
    private String oldRplmn;
    private int rejNum;
    private String rplmn;

    private class CloudOtaBroadcastReceiver extends BroadcastReceiver {
        private CloudOtaBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (HwGsmServiceStateManager.this.mGsmPhone != null && HwGsmServiceStateManager.this.mCi != null) {
                String action = intent.getAction();
                if (HwGsmServiceStateManager.CLOUD_OTA_MCC_UPDATE.equals(action)) {
                    Rlog.e(HwGsmServiceStateManager.LOG_TAG, "HwCloudOTAService CLOUD_OTA_MCC_UPDATE");
                    HwGsmServiceStateManager.this.mCi.sendCloudMessageToModem(HwGsmServiceStateManager.WIFI_IDX);
                } else if (HwGsmServiceStateManager.CLOUD_OTA_DPLMN_UPDATE.equals(action)) {
                    Rlog.e(HwGsmServiceStateManager.LOG_TAG, "HwCloudOTAService CLOUD_OTA_DPLMN_UPDATE");
                    HwGsmServiceStateManager.this.mCi.sendCloudMessageToModem(HwGsmServiceStateManager.UMTS_LEVEL);
                }
            }
        }
    }

    private class DoubleSignalStrength {
        private static final double VALUE_NEW_COEF_QUA_DES_SS = 0.15d;
        private static final int VALUE_NEW_COEF_STR_DES_SS = 5;
        private static final double VALUE_OLD_COEF_QUA_DES_SS = 0.85d;
        private static final int VALUE_OLD_COEF_STR_DES_SS = 7;
        private double mDoubleGsmSS;
        private double mDoubleLteRsrp;
        private double mDoubleLteRssnr;
        private double mDoubleWcdmaEcio;
        private double mDoubleWcdmaRscp;
        private int mTechState;

        public DoubleSignalStrength(SignalStrength ss) {
            this.mDoubleLteRsrp = (double) ss.getLteRsrp();
            this.mDoubleLteRssnr = (double) ss.getLteRssnr();
            this.mDoubleWcdmaRscp = (double) ss.getWcdmaRscp();
            this.mDoubleWcdmaEcio = (double) ss.getWcdmaEcio();
            this.mDoubleGsmSS = (double) ss.getGsmSignalStrength();
            this.mTechState = HwGsmServiceStateManager.WCDMA_STRENGTH_POOR_STD;
            if (ss.getGsmSignalStrength() < -1) {
                this.mTechState |= HwGsmServiceStateManager.WIFI_IDX;
            }
            if (ss.getWcdmaRscp() < -1) {
                this.mTechState |= HwGsmServiceStateManager.UMTS_LEVEL;
            }
            if (ss.getLteRsrp() < -1) {
                this.mTechState |= HwGsmServiceStateManager.LTE_LEVEL;
            }
        }

        public DoubleSignalStrength(DoubleSignalStrength doubleSS) {
            this.mDoubleLteRsrp = doubleSS.mDoubleLteRsrp;
            this.mDoubleLteRssnr = doubleSS.mDoubleLteRssnr;
            this.mDoubleWcdmaRscp = doubleSS.mDoubleWcdmaRscp;
            this.mDoubleWcdmaEcio = doubleSS.mDoubleWcdmaEcio;
            this.mDoubleGsmSS = doubleSS.mDoubleGsmSS;
            this.mTechState = doubleSS.mTechState;
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

        public boolean processLteRsrpAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldRsrp = oldDoubleSS.getDoubleLteRsrp();
            double modemLteRsrp = (double) modemSS.getLteRsrp();
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]Before LteRsrpAlaphFilter -- old : " + oldRsrp + "; instant new : " + modemLteRsrp);
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
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]LteRsrpAlaphFilter modem : " + modemLteRsrp + "; old : " + oldRsrp + "; new : " + this.mDoubleLteRsrp);
            if (this.mDoubleLteRsrp - modemLteRsrp <= -1.0d || this.mDoubleLteRsrp - modemLteRsrp >= 1.0d) {
                newSS.setLteRsrp((int) this.mDoubleLteRsrp);
                return true;
            }
            this.mDoubleLteRsrp = modemLteRsrp;
            newSS.setLteRsrp((int) this.mDoubleLteRsrp);
            return HwGsmServiceStateManager.SHOW_4G_PLUS_ICON;
        }

        public boolean processLteRssnrAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldRssnr = oldDoubleSS.getDoubleLteRssnr();
            double modemLteRssnr = (double) modemSS.getLteRssnr();
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
            return HwGsmServiceStateManager.SHOW_4G_PLUS_ICON;
        }

        public boolean processWcdmaRscpAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldWcdmaRscp = oldDoubleSS.getDoubleWcdmaRscp();
            double modemWcdmaRscp = (double) modemSS.getWcdmaRscp();
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
            return HwGsmServiceStateManager.SHOW_4G_PLUS_ICON;
        }

        public boolean processWcdmaEcioAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldWcdmaEcio = oldDoubleSS.getDoubleWcdmaEcio();
            double modemWcdmaEcio = (double) modemSS.getWcdmaEcio();
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
            return HwGsmServiceStateManager.SHOW_4G_PLUS_ICON;
        }

        public boolean processGsmSignalStrengthAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            double oldGsmSS = oldDoubleSS.getDoubleGsmSignalStrength();
            double modemGsmSS = (double) modemSS.getGsmSignalStrength();
            Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]Before processGsmSignalStrengthAlaphFilter -- old : " + oldGsmSS + "; instant new : " + modemGsmSS);
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
            return HwGsmServiceStateManager.SHOW_4G_PLUS_ICON;
        }

        public void proccessAlaphFilter(SignalStrength newSS, SignalStrength modemSS) {
            proccessAlaphFilter(this, newSS, modemSS, true);
        }

        public void proccessAlaphFilter(DoubleSignalStrength oldDoubleSS, SignalStrength newSS, SignalStrength modemSS, boolean needProcessDescend) {
            int i = HwGsmServiceStateManager.WCDMA_STRENGTH_POOR_STD;
            if (oldDoubleSS == null) {
                Rlog.d(HwGsmServiceStateManager.LOG_TAG, "SUB[" + HwGsmServiceStateManager.this.mPhoneId + "]proccess oldDoubleSS is null");
                return;
            }
            if ((this.mTechState & HwGsmServiceStateManager.LTE_LEVEL) != 0) {
                i = processLteRsrpAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                    i |= processLteRssnrAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                }
            }
            if ((this.mTechState & HwGsmServiceStateManager.UMTS_LEVEL) != 0) {
                i |= processWcdmaRscpAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                if (HwGsmServiceStateManager.this.FEATURE_SIGNAL_DUALPARAM) {
                    i |= processWcdmaEcioAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
                }
            }
            if ((this.mTechState & HwGsmServiceStateManager.WIFI_IDX) != 0) {
                i |= processGsmSignalStrengthAlaphFilter(oldDoubleSS, newSS, modemSS, needProcessDescend);
            }
            HwGsmServiceStateManager.this.mGsst.setSignalStrength(newSS);
            if (i != 0) {
                HwGsmServiceStateManager.this.sendMeesageDelayUpdateSingalStrength();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwGsmServiceStateManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwGsmServiceStateManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwGsmServiceStateManager.<clinit>():void");
    }

    public HwGsmServiceStateManager(ServiceStateTracker sst, GsmCdmaPhone gsmPhone) {
        super(gsmPhone);
        this.rejNum = WCDMA_STRENGTH_POOR_STD;
        this.FEATURE_SIGNAL_DUALPARAM = SystemProperties.getBoolean("signal.dualparam", SHOW_4G_PLUS_ICON);
        this.mPhoneId = WCDMA_STRENGTH_POOR_STD;
        this.rplmn = "";
        this.oldRplmn = "";
        this.mOldCAstate = WCDMA_STRENGTH_POOR_STD;
        this.mCloudOtaBroadcastReceiver = new CloudOtaBroadcastReceiver();
        this.mRac = -1;
        this.mCurShowWifi = SHOW_4G_PLUS_ICON;
        this.mCurWifi = "";
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = "";
                if (intent != null) {
                    action = intent.getAction();
                    if ("android.intent.action.refreshapn".equals(action)) {
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "refresh apn worked,updateSpnDisplay.");
                        HwGsmServiceStateManager.this.mGsst.updateSpnDisplay();
                    } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                        String simState = (String) intent.getExtra("ss");
                        slotId = intent.getIntExtra("slot", -1000);
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "ACTION_SIM_STATE_CHANGED simState = " + simState + " slotId = " + slotId);
                        if ("ABSENT".equals(simState)) {
                            Rlog.i(HwGsmServiceStateManager.LOG_TAG, "removeVirtualNet for slotId " + slotId);
                            VirtualNet.removeVirtualNet(slotId);
                            Rlog.i(HwGsmServiceStateManager.LOG_TAG, "reset PROPERTY_GSM_SIM_UPDATE_NITZ when the sim is absent.");
                            if (-1 != SystemProperties.getInt("gsm.sim.updatenitz", -1)) {
                                SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                            }
                        } else if ("LOADED".equals(simState) && slotId == HwGsmServiceStateManager.this.mPhoneId) {
                            Rlog.i(HwGsmServiceStateManager.LOG_TAG, "after simrecords loaded,updateSpnDisplay for virtualnet ons.");
                            HwGsmServiceStateManager.this.mGsst.updateSpnDisplay();
                        }
                    } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(action)) {
                        slotId = intent.getIntExtra("subscription", -1);
                        int intValue = intent.getIntExtra("intContent", HwGsmServiceStateManager.WCDMA_STRENGTH_POOR_STD);
                        String column = intent.getStringExtra("columnName");
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "Received ACTION_SUBINFO_CONTENT_CHANGE on slotId: " + slotId + " for " + column + ", intValue: " + intValue);
                        Rlog.i(HwGsmServiceStateManager.LOG_TAG, "PROPERTY_GSM_SIM_UPDATE_NITZ=" + SystemProperties.getInt("gsm.sim.updatenitz", -1));
                        if ("sub_state".equals(column) && -1 != slotId && intValue == 0 && slotId == SystemProperties.getInt("gsm.sim.updatenitz", -1)) {
                            Rlog.i(HwGsmServiceStateManager.LOG_TAG, "reset PROPERTY_GSM_SIM_UPDATE_NITZ when the sim is inactive and the time zone get from the sim' NITZ.");
                            SystemProperties.set("gsm.sim.updatenitz", String.valueOf(-1));
                        }
                    } else if (HwGsmServiceStateManager.this.mHwCustGsmServiceStateManager != null) {
                        HwGsmServiceStateManager.this.mRac = HwGsmServiceStateManager.this.mHwCustGsmServiceStateManager.handleBroadcastReceived(context, intent, HwGsmServiceStateManager.this.mRac);
                    }
                }
            }
        };
        this.mGsst = sst;
        this.mGsmPhone = gsmPhone;
        this.mContext = gsmPhone.getContext();
        this.mCr = this.mContext.getContentResolver();
        this.mPhoneId = gsmPhone.getPhoneId();
        this.mCi = gsmPhone.mCi;
        this.mCi.registerForRplmnsStateChanged(this, EVENT_RPLMNS_STATE_CHANGED, null);
        sendMessage(obtainMessage(EVENT_RPLMNS_STATE_CHANGED));
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]constructor init");
        Object[] objArr = new Object[UMTS_LEVEL];
        objArr[WCDMA_STRENGTH_POOR_STD] = sst;
        objArr[WIFI_IDX] = gsmPhone;
        this.mHwCustGsmServiceStateManager = (HwCustGsmServiceStateManager) HwCustUtils.createObj(HwCustGsmServiceStateManager.class, objArr);
        addBroadCastReceiver();
        this.mMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        this.mCi.registerForCaStateChanged(this, EVENT_CA_STATE_CHANGED, null);
        this.mCi.registerForCrrConn(this, EVENT_CRR_CONN, null);
        this.mCi.setOnRegPLMNSelInfo(this, EVENT_PLMN_SELINFO, null);
        registerCloudOtaBroadcastReceiver();
        if (PS_CLEARCODE) {
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
        if (this.mHwCustGsmServiceStateManager != null) {
            this.mHwCustGsmServiceStateManager.storeModemRoamingStatus(roaming);
        }
        if (roaming) {
            String hplmn = null;
            if (this.mGsmPhone.mIccRecords.get() != null) {
                hplmn = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            }
            String regplmn = getNewSS().getOperatorNumeric();
            String str = null;
            if (getNoRoamingByMcc(getNewSS())) {
                roaming = SHOW_4G_PLUS_ICON;
            }
            try {
                str = Systemex.getString(this.mContext.getContentResolver(), "reg_plmn_custom");
                Rlog.d("HwGsmServiceStateTracker", "handlePollStateResult plmnCustomString = " + str);
            } catch (Exception e) {
                Rlog.e("HwGsmServiceStateTracker", "Exception when got name value", e);
            }
            if (str != null) {
                String[] regplmnCustomArray = str.split(";");
                if (hplmn != null && regplmn != null && !"".equals(hplmn) && !"".equals(regplmn)) {
                    int regplmnCustomArrayLen = regplmnCustomArray.length;
                    for (int i = WCDMA_STRENGTH_POOR_STD; i < regplmnCustomArrayLen; i += WIFI_IDX) {
                        String[] regplmnCustomArrEleBuf = regplmnCustomArray[i].split(",");
                        if (containsPlmn(hplmn, regplmnCustomArrEleBuf) && containsPlmn(regplmn, regplmnCustomArrEleBuf)) {
                            roaming = SHOW_4G_PLUS_ICON;
                            break;
                        }
                    }
                } else {
                    roaming = SHOW_4G_PLUS_ICON;
                }
            }
        }
        Rlog.d("GsmServiceStateTracker", "roaming = " + roaming);
        if (HwTelephonyManagerInner.getDefault().isCTSimCard(this.mGsmPhone.getPhoneId()) && getNewSS().getState() == 0 && ServiceState.isGsm(getNewSS().getRilVoiceRadioTechnology())) {
            roaming = true;
            Rlog.d("GsmServiceStateTracker", "When CT card register in GSM/UMTS, it always should be roaming" + true);
        }
        if (this.mHwCustGsmServiceStateManager != null) {
            roaming = this.mHwCustGsmServiceStateManager.SetRoamingStateForOperatorCustomization(getNewSS(), roaming);
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
                if (WIFI_IDX == netClass || UMTS_LEVEL == netClass) {
                    roaming = SHOW_4G_PLUS_ICON;
                }
                if (NETWORK_MODE_GSM_UMTS == netClass) {
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
            data = Systemex.getString(this.mCr, "plmn");
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
        String str = null;
        String imsi = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            str = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            imsi = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getIMSI();
        }
        Rlog.d("HwGsmServiceStateTracker", "hplmn = " + str);
        if (this.mHwCustGsmServiceStateManager == null || !this.mHwCustGsmServiceStateManager.notUseVirtualName(imsi)) {
            plmnValue = HwTelephonyFactory.getHwPhoneManager().getVirtualNetOperatorName(plmnValue, getSS().getRoaming(), hasNitzOperatorName(slotId), slotId, str);
            Rlog.d("HwGsmServiceStateTracker", "VirtualNetName = " + plmnValue);
        } else {
            Rlog.d("HwGsmServiceStateTracker", "passed the Virtualnet cust");
        }
        if (isMultiSimEnabled) {
            apnReminder = ApnReminder.getInstance(this.mContext, slotId);
        } else {
            apnReminder = ApnReminder.getInstance(this.mContext);
        }
        if (!(apnReminder.isPopupApnSettingsEmpty() || getSS().getRoaming() || hasNitzOperatorName(slotId) || str == null)) {
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
            plmnValue = getVirtualNetPlmnValue(operatorNumeric, str, imsi, getEons(getSS().getOperatorAlphaLong()));
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
        String str = null;
        String custPlmn = System.getString(this.mCr, "hw_plmn_abbrev");
        if (TextUtils.isEmpty(custPlmn)) {
            return custPlmnValue;
        }
        if (this.mGsmPhone.mIccRecords.get() != null) {
            CustPlmnMember cpm = CustPlmnMember.getInstance();
            if (cpm.acquireFromCust(((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric(), getSS(), custPlmn)) {
                str = cpm.plmn;
                Rlog.d(LOG_TAG, " plmn2 =" + str);
            }
        }
        if (TextUtils.isEmpty(str)) {
            return custPlmnValue;
        }
        if (hasNitzOperatorName(slotId)) {
            result = getEons(getSS().getOperatorAlphaLong());
        } else {
            result = str;
            result = getEons(str);
        }
        Rlog.d("getGsmOnsDisplayPlmnByAbbrevPriority, ", "plmnValueByAbbrevPriority = " + result + " slotId = " + slotId + " PlmnValue = " + custPlmnValue);
        return result;
    }

    public String getGsmOnsDisplayPlmnByPriority(String custPlmnValue, int slotId) {
        boolean HW_NETWORK_SIM_UE_PRIORITY = SystemProperties.getBoolean("ro.config.net_sim_ue_pri", SHOW_4G_PLUS_ICON);
        boolean CUST_HPLMN_REGPLMN = enablePlmnByNetSimUePriority();
        if (!HW_NETWORK_SIM_UE_PRIORITY && !CUST_HPLMN_REGPLMN) {
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
            if (CUST_HPLMN_REGPLMN && !TextUtils.isEmpty(spnSim)) {
                result2 = spnSim;
            }
            result = getSS().getOperatorAlphaLong();
        } else {
            result = getSS().getOperatorAlphaLong();
            if (custPlmnValue != null) {
                result = custPlmnValue;
            }
            result = getEons(result);
            if (CUST_HPLMN_REGPLMN && !TextUtils.isEmpty(spnSim)) {
                result2 = spnSim;
            }
        }
        Rlog.d("getGsmOnsDisplayPlmnByPriority, ", "plmnValue = " + result + " slotId = " + slotId + " custPlmnValue = " + custPlmnValue);
        return result;
    }

    public boolean enablePlmnByNetSimUePriority() {
        Object obj = null;
        Object regplmn = null;
        boolean cust_hplmn_regplmn = SHOW_4G_PLUS_ICON;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            obj = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        String custNetSimUePriority = Systemex.getString(this.mCr, "hw_net_sim_ue_pri");
        if (!TextUtils.isEmpty(obj) && !TextUtils.isEmpty(r6) && !TextUtils.isEmpty(custNetSimUePriority)) {
            String[] custmccmncs = custNetSimUePriority.split(";");
            int length = custmccmncs.length;
            for (int i = WCDMA_STRENGTH_POOR_STD; i < length; i += WIFI_IDX) {
                String[] mccmncs = custmccmncs[i].split(",");
                if (obj.equals(mccmncs[WCDMA_STRENGTH_POOR_STD]) && r6.equals(mccmncs[WIFI_IDX])) {
                    cust_hplmn_regplmn = true;
                    break;
                }
            }
        } else {
            cust_hplmn_regplmn = SHOW_4G_PLUS_ICON;
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
        ContentResolver contentResolver;
        Uri withAppendedId;
        String[] strArr;
        if (isMultiSimEnabled) {
            int slotId = this.mGsmPhone.getPhoneId();
            contentResolver = this.mContext.getContentResolver();
            withAppendedId = ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId);
            strArr = new String[NETWORK_MODE_GSM_UMTS];
            strArr[WCDMA_STRENGTH_POOR_STD] = "_id";
            strArr[WIFI_IDX] = NumMatchs.NAME;
            strArr[UMTS_LEVEL] = "apn";
            cursor = contentResolver.query(withAppendedId, strArr, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        } else {
            contentResolver = this.mContext.getContentResolver();
            withAppendedId = PREFERAPN_NO_UPDATE_URI;
            strArr = new String[NETWORK_MODE_GSM_UMTS];
            strArr[WCDMA_STRENGTH_POOR_STD] = "_id";
            strArr[WIFI_IDX] = NumMatchs.NAME;
            strArr[UMTS_LEVEL] = "apn";
            cursor = contentResolver.query(withAppendedId, strArr, null, null, NumMatchs.DEFAULT_SORT_ORDER);
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
        CharSequence charSequence = null;
        CharSequence regplmn = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            charSequence = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        String[] mccMncList = CHINAMOBILE_MCCMNC.split(";");
        boolean isRegplmnCMCC = SHOW_4G_PLUS_ICON;
        boolean isHplmnCMCC = SHOW_4G_PLUS_ICON;
        if (TextUtils.isEmpty(regplmn) || TextUtils.isEmpty(charSequence)) {
            return SHOW_4G_PLUS_ICON;
        }
        for (int i = WCDMA_STRENGTH_POOR_STD; i < mccMncList.length; i += WIFI_IDX) {
            if (mccMncList[i].equals(regplmn)) {
                isRegplmnCMCC = true;
            }
            if (mccMncList[i].equals(charSequence)) {
                isHplmnCMCC = true;
            }
        }
        if (!isRegplmnCMCC) {
            isHplmnCMCC = SHOW_4G_PLUS_ICON;
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
            String custSpn = Systemex.getString(this.mCr, "hw_plmn_spn");
            if (custSpn != null) {
                Rlog.d(LOG_TAG, "hplmn =" + hplmn + " regplmn =" + regplmn + " custSpn length =" + custSpn.length());
            }
            if (cpm.acquireFromCust(hplmn, getSS(), custSpn)) {
                if (cpm.rule != 0) {
                    showSpn = cpm.showSpn;
                }
                if (cpm.rule != 0) {
                    showPlmn = cpm.showPlmn;
                }
                if (cpm.rule != 0) {
                    rule = cpm.rule;
                }
                if (!"####".equals(cpm.plmn)) {
                    plmn = cpm.plmn;
                }
                if (!"####".equals(cpm.spn)) {
                    spn = cpm.spn;
                }
                if (WIFI_IDX == cpm.rule && TextUtils.isEmpty(spn)) {
                    Rlog.d(LOG_TAG, " want to show spn while spn is null,use plmn instead " + tempPlmn);
                    spn = tempPlmn;
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
                        if (!"####".equals(cpm.plmn)) {
                            plmn = cpm.plmn;
                        }
                        if (!"####".equals(cpm.spn)) {
                            spn = cpm.spn;
                        }
                        if (WIFI_IDX == cpm.rule && TextUtils.isEmpty(spn)) {
                            Rlog.d(LOG_TAG, "want to show spn while spn is null,use plmn instead " + tempPlmn);
                            spn = tempPlmn;
                        }
                        Rlog.d(LOG_TAG, "showSpn2=" + showSpn + " showPlmn2=" + showPlmn + " spn2=" + spn + " plmn2=" + plmn);
                    }
                }
            }
        }
        OnsDisplayParams odpCust = getGsmOnsDisplayParamsBySpecialCust(showSpn, showPlmn, rule, plmn, spn);
        OnsDisplayParams odpForChinaOperator = getGsmOnsDisplayParamsForChinaOperator(showSpn, showPlmn, rule, plmn, spn);
        OnsDisplayParams onsDisplayParams = null;
        OnsDisplayParams onsDisplayParams2 = null;
        OnsDisplayParams odpCustForVideotron = null;
        if (this.mHwCustGsmServiceStateManager != null) {
            onsDisplayParams = this.mHwCustGsmServiceStateManager.getGsmOnsDisplayParamsForGlobalOperator(showSpn, showPlmn, rule, plmn, spn);
            onsDisplayParams2 = this.mHwCustGsmServiceStateManager.getVirtualNetOnsDisplayParams();
            odpCustForVideotron = this.mHwCustGsmServiceStateManager.getGsmOnsDisplayParamsForVideotron(showSpn, showPlmn, rule, plmn, spn);
        }
        if (odpCust != null) {
            odp = odpCust;
        } else if (odpForChinaOperator != null) {
            odp = odpForChinaOperator;
        } else if (onsDisplayParams2 != null) {
            odp = onsDisplayParams2;
        } else if (onsDisplayParams != null) {
            odp = onsDisplayParams;
        } else if (odpCustForVideotron != null) {
            odp = odpCustForVideotron;
        } else {
            odp = new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
        }
        if (this.mHwCustGsmServiceStateManager != null) {
            odp = this.mHwCustGsmServiceStateManager.SetOnsDisplayCustomization(odp, getSS());
        }
        if (this.mGsmPhone.getImsPhone() != null && this.mGsmPhone.getImsPhone().isWifiCallingEnabled()) {
            odp = getGsmOnsDisplayParamsForVoWifi(odp);
        }
        this.mCurShowWifi = odp.mShowWifi;
        this.mCurWifi = odp.mWifi;
        Object networkNameShow = null;
        if (odp.mShowPlmn) {
            networkNameShow = odp.mPlmn;
        } else if (odp.mShowSpn) {
            networkNameShow = odp.mSpn;
        }
        if (!(TextUtils.isEmpty(networkNameShow) || getSS() == null || ((getSS().getDataRegState() != 0 && getSS().getVoiceRegState() != 0) || this.mHwCustGsmServiceStateManager == null || this.mHwCustGsmServiceStateManager.skipPlmnUpdateFromCust()))) {
            Rlog.d(LOG_TAG, "before setprop, the long ons is = " + getSS().getOperatorAlphaLong());
            getSS().setOperatorName(networkNameShow, getSS().getOperatorAlphaShort(), getSS().getOperatorNumeric());
            Rlog.d(LOG_TAG, "after setprop, the long ons is = " + getSS().getOperatorAlphaLong());
        }
        return odp;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsForVoWifi(OnsDisplayParams ons) {
        int voiceIdx = WCDMA_STRENGTH_POOR_STD;
        String spnConfiged = "";
        boolean z = SHOW_4G_PLUS_ICON;
        String wifiConfiged = "";
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mGsmPhone.getContext().getSystemService("carrier_config");
        if (configLoader != null) {
            try {
                PersistableBundle b = configLoader.getConfigForSubId(this.mGsmPhone.getSubId());
                if (b != null) {
                    voiceIdx = b.getInt("wfc_spn_format_idx_int");
                    spnConfiged = b.getString(KEY_WFC_SPN_STRING);
                    z = b.getBoolean(KEY_WFC_HIDE_WIFI_BOOL);
                    wifiConfiged = b.getString(KEY_WFC_FORMAT_WIFI_STRING);
                }
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "getGsmOnsDisplayParams: carrier config error: " + e);
            }
        }
        Rlog.d(LOG_TAG, "updateSpnDisplay, voiceIdx = " + voiceIdx + " spnConfiged = " + spnConfiged + " hideWifi = " + z + " wifiConfiged = " + wifiConfiged);
        String formatWifi = "%s";
        if (!z) {
            boolean useGoogleWifiFormat = voiceIdx == WIFI_IDX ? true : SHOW_4G_PLUS_ICON;
            String[] wfcSpnFormats = this.mGsmPhone.getContext().getResources().getStringArray(17236067);
            if (!TextUtils.isEmpty(wifiConfiged)) {
                formatWifi = wifiConfiged;
            } else if (!useGoogleWifiFormat || wfcSpnFormats == null) {
                formatWifi = this.mGsmPhone.getContext().getResources().getString(17041122);
            } else {
                formatWifi = wfcSpnFormats[WIFI_IDX];
            }
        }
        String combineWifi = "";
        boolean inService = getCombinedRegState(getSS()) == 0 ? true : SHOW_4G_PLUS_ICON;
        if (!TextUtils.isEmpty(spnConfiged)) {
            combineWifi = spnConfiged;
        } else if (!TextUtils.isEmpty(ons.mSpn)) {
            combineWifi = ons.mSpn;
        } else if (inService && !TextUtils.isEmpty(ons.mPlmn)) {
            combineWifi = ons.mPlmn;
        }
        try {
            Object[] objArr = new Object[WIFI_IDX];
            objArr[WCDMA_STRENGTH_POOR_STD] = combineWifi;
            ons.mWifi = String.format(formatWifi, objArr).trim();
            ons.mShowWifi = true;
        } catch (RuntimeException e2) {
            Rlog.e(LOG_TAG, "combine wifi fail, " + e2);
        }
        return ons;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private OnsDisplayParams getGsmOnsDisplayParamsBySpecialCust(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        String plmnRslt = plmn;
        String spnRslt = spn;
        boolean showPlmnRslt = showPlmn;
        boolean z = showSpn;
        int ruleRslt = rule;
        Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsBySpecialCust: showPlmn = " + showPlmn + ", showSpn = " + showSpn + ", rule = " + rule + ", plmn = " + plmn + ", spn = " + spn);
        boolean matched = true;
        String regPlmn = getSS().getOperatorNumeric();
        int netType = getSS().getVoiceNetworkType();
        TelephonyManager.getDefault();
        int netClass = TelephonyManager.getNetworkClass(netType);
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        if (r != null) {
            String pnnName;
            String hplmn = r.getOperatorNumeric();
            String spnSim = r.getServiceProviderName();
            int ruleSim = r.getDisplayRule(regPlmn);
            Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsBySpecialCust: regPlmn = " + regPlmn + ", hplmn = " + hplmn + ", spnSim = " + spnSim + ", ruleSim = " + ruleSim + ", netType = " + netType + ", netClass = " + netClass);
            if ("21405".equals(hplmn)) {
                if ("21407".equals(regPlmn)) {
                    if ("tuenti".equalsIgnoreCase(spnSim)) {
                        pnnName = getEons(plmn);
                        if (!TextUtils.isEmpty(spnSim)) {
                            spnRslt = spnSim;
                            z = (ruleSim & WIFI_IDX) == WIFI_IDX ? true : SHOW_4G_PLUS_ICON;
                        }
                        if (!TextUtils.isEmpty(pnnName)) {
                            plmnRslt = pnnName;
                            showPlmnRslt = (ruleSim & UMTS_LEVEL) == UMTS_LEVEL ? true : SHOW_4G_PLUS_ICON;
                        }
                    }
                }
            }
            if ("21407".equals(hplmn)) {
                if ("21407".equals(regPlmn)) {
                    pnnName = getEons(plmn);
                    if (!TextUtils.isEmpty(spnSim)) {
                        spnRslt = spnSim;
                        z = (rule & WIFI_IDX) == WIFI_IDX ? true : SHOW_4G_PLUS_ICON;
                    }
                    if (!TextUtils.isEmpty(pnnName)) {
                        plmnRslt = pnnName;
                        showPlmnRslt = (rule & UMTS_LEVEL) == UMTS_LEVEL ? true : SHOW_4G_PLUS_ICON;
                    }
                }
            }
            if ("23420".equals(hplmn)) {
                if (getCombinedRegState(getSS()) == 0) {
                    pnnName = getEons(plmn);
                    if (!TextUtils.isEmpty(pnnName)) {
                        spnRslt = spnSim;
                        plmnRslt = pnnName;
                        z = SHOW_4G_PLUS_ICON;
                        showPlmnRslt = true;
                        ruleRslt = UMTS_LEVEL;
                    }
                }
            }
            if (!TextUtils.isEmpty(spnSim)) {
                if ("26006".equals(hplmn)) {
                    z = SHOW_4G_PLUS_ICON;
                    showPlmnRslt = true;
                    spnRslt = spnSim;
                    ruleRslt = ruleSim;
                    if ("26001".equals(regPlmn)) {
                        plmnRslt = spnSim + " (Plus)";
                    } else {
                        if ("26002".equals(regPlmn)) {
                            plmnRslt = spnSim + " (T-Mobile)";
                        } else {
                            if ("26003".equals(regPlmn)) {
                                plmnRslt = spnSim + " (Orange)";
                            } else {
                                if ("26006".equals(regPlmn)) {
                                    plmnRslt = spnSim;
                                } else {
                                    matched = SHOW_4G_PLUS_ICON;
                                }
                            }
                        }
                    }
                }
            }
            if ("74000".equals(hplmn)) {
                if ("74000".equals(regPlmn)) {
                }
            }
            if ("45006".equals(hplmn)) {
                if ("45006".equals(regPlmn)) {
                }
            }
            if ("732187".equals(hplmn)) {
                if (!"732103".equals(regPlmn)) {
                }
                if (WIFI_IDX == netClass || UMTS_LEVEL == netClass) {
                    plmnRslt = "ETB";
                } else if (NETWORK_MODE_GSM_UMTS == netClass) {
                    plmnRslt = "ETB 4G";
                }
            }
            if ("50218".equals(hplmn)) {
                if ("50212".equals(regPlmn)) {
                    if (WIFI_IDX == netClass || UMTS_LEVEL == netClass) {
                        z = true;
                        showPlmnRslt = SHOW_4G_PLUS_ICON;
                        ruleRslt = WIFI_IDX;
                        plmnRslt = "U Mobile";
                        spnRslt = "U Mobile";
                    } else if (NETWORK_MODE_GSM_UMTS == netClass) {
                        z = true;
                        showPlmnRslt = SHOW_4G_PLUS_ICON;
                        ruleRslt = WIFI_IDX;
                        plmnRslt = "MY MAXIS";
                        spnRslt = "MY MAXIS";
                    }
                }
            }
            if (!"334050".equals(hplmn)) {
                if (!"334090".equals(hplmn)) {
                    if (!"33405".equals(hplmn)) {
                        matched = SHOW_4G_PLUS_ICON;
                    }
                }
            }
            if (TextUtils.isEmpty(spnSim)) {
                if (!"334050".equals(regPlmn)) {
                }
                if (!TextUtils.isEmpty(plmn)) {
                    if (!plmn.startsWith("Iusacell")) {
                    }
                    Rlog.d(LOG_TAG, "AT&T a part of card has no opl/PNN and spn, then want it to be treated as AT&T");
                    plmnRslt = "AT&T";
                }
            }
            if (!TextUtils.isEmpty(plmnRslt)) {
                if (plmnRslt.startsWith("AT&T")) {
                    if (WIFI_IDX == netClass) {
                        plmnRslt = "AT&T EDGE";
                    } else if (UMTS_LEVEL == netClass) {
                        plmnRslt = "AT&T";
                    } else if (NETWORK_MODE_GSM_UMTS == netClass) {
                        plmnRslt = "AT&T 4G";
                    }
                }
            }
        } else {
            matched = SHOW_4G_PLUS_ICON;
        }
        Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsBySpecialCust: matched = " + matched + ", showPlmnRslt = " + showPlmnRslt + ", showSpnRslt = " + z + ", ruleRslt = " + ruleRslt + ", plmnRslt = " + plmnRslt + ", spnRslt = " + spnRslt);
        if (matched) {
            return new OnsDisplayParams(z, showPlmnRslt, ruleRslt, plmnRslt, spnRslt);
        }
        return null;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsForChinaOperator(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        CharSequence charSequence = null;
        CharSequence regplmn = null;
        if (this.mGsmPhone.mIccRecords.get() != null) {
            charSequence = ((IccRecords) this.mGsmPhone.mIccRecords.get()).getOperatorNumeric();
            regplmn = getSS().getOperatorNumeric();
        }
        Rlog.d("HwGsmServiceStateTracker", "showSpn:" + showSpn + ",showPlmn:" + showPlmn + ",rule:" + rule + ",plmn:" + plmn + ",spn:" + spn + ",hplmn:" + charSequence + ",regplmn:" + regplmn);
        if (!(TextUtils.isEmpty(regplmn) || TextUtils.isEmpty(charSequence))) {
            if (isChinaMobileMccMnc()) {
                if (spn == null || "".equals(spn) || "CMCC".equals(spn) || "China Mobile".equals(spn)) {
                    Rlog.d("HwGsmServiceStateTracker", "chinamobile just show plmn without spn.");
                    return new OnsDisplayParams(SHOW_4G_PLUS_ICON, true, rule, plmn, spn);
                }
                Rlog.d("HwGsmServiceStateTracker", "third party provider sim cust just show original rule.");
                return new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
            } else if (HuaweiTelephonyConfigs.isChinaTelecom() || (getSS().getRoaming() && "20404".equals(charSequence) && "20404".equals(regplmn) && NETWORK_MODE_GSM_UMTS == (HwAllInOneController.getInstance().getSpecCardType(this.mGsmPhone.getPhoneId()) & 15))) {
                Rlog.d("HwGsmServiceStateTracker", "In China Telecom, just show plmn without spn.");
                return new OnsDisplayParams(SHOW_4G_PLUS_ICON, true, rule, plmn, spn);
            }
        }
        return null;
    }

    public void sendDualSimUpdateSpnIntent(boolean showSpn, String spn, boolean showPlmn, String plmn) {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            int phoneId = this.mGsmPhone.getPhoneId();
            if (phoneId == 0) {
                log("Send Intent for SUB 1:android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
                Intent intent1 = new Intent("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
                intent1.addFlags(536870912);
                intent1.putExtra("showSpn", showSpn);
                intent1.putExtra(VirtualNets.SPN, spn);
                intent1.putExtra("showPlmn", showPlmn);
                intent1.putExtra("plmn", plmn);
                intent1.putExtra("subscription", phoneId);
                intent1.putExtra(EXTRA_SHOW_WIFI, this.mCurShowWifi);
                intent1.putExtra(EXTRA_WIFI, this.mCurWifi);
                this.mContext.sendStickyBroadcastAsUser(intent1, UserHandle.ALL);
            } else if (WIFI_IDX == phoneId) {
                log("Send Intent for SUB 2:android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
                Intent intent2 = new Intent("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
                intent2.addFlags(536870912);
                intent2.putExtra("showSpn", showSpn);
                intent2.putExtra(VirtualNets.SPN, spn);
                intent2.putExtra("showPlmn", showPlmn);
                intent2.putExtra("plmn", plmn);
                intent2.putExtra("subscription", phoneId);
                intent2.putExtra(EXTRA_SHOW_WIFI, this.mCurShowWifi);
                intent2.putExtra(EXTRA_WIFI, this.mCurWifi);
                this.mContext.sendStickyBroadcastAsUser(intent2, UserHandle.ALL);
            } else {
                log("unsupport SUB ID :" + phoneId);
            }
        }
    }

    private void log(String string) {
    }

    private boolean containsPlmn(String plmn, String[] plmnArray) {
        if (plmn == null || plmnArray == null) {
            return SHOW_4G_PLUS_ICON;
        }
        int length = plmnArray.length;
        for (int i = WCDMA_STRENGTH_POOR_STD; i < length; i += WIFI_IDX) {
            if (plmn.equals(plmnArray[i])) {
                return true;
            }
        }
        return SHOW_4G_PLUS_ICON;
    }

    private boolean hasNitzOperatorName(int slotId) {
        boolean z = true;
        boolean z2 = SHOW_4G_PLUS_ICON;
        String result = SystemProperties.get("persist.radio.nitz_hw_name");
        String result1 = SystemProperties.get("persist.radio.nitz_hw_name1");
        if (!isMultiSimEnabled) {
            if (result != null && result.length() > 0) {
                z2 = true;
            }
            return z2;
        } else if (slotId == 0) {
            if (result == null || result.length() <= 0) {
                z = SHOW_4G_PLUS_ICON;
            }
            return z;
        } else if (WIFI_IDX == slotId) {
            if (result1 != null && result1.length() > 0) {
                z2 = true;
            }
            return z2;
        } else {
            Rlog.e("HwGsmServiceStateTracker", "hasNitzOperatorName invalid sub id" + slotId);
            return SHOW_4G_PLUS_ICON;
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
                regplmnRoamCustomString = Systemex.getString(this.mCr, "reg_plmn_roam_custom");
            } catch (Exception e) {
                Rlog.e("HwGsmServiceStateTracker", "Exception when got reg_plmn_roam_custom value", e);
            }
            if (!(regplmnRoamCustomString == null || hplmn == null || regplmn == null)) {
                String[] rules = regplmnRoamCustomString.split(";");
                int length = rules.length;
                for (int i = WCDMA_STRENGTH_POOR_STD; i < length; i += WIFI_IDX) {
                    String[] rule_plmn_roam = rules[i].split(":");
                    int rule_roam = Integer.parseInt(rule_plmn_roam[WCDMA_STRENGTH_POOR_STD]);
                    String[] plmn_roam = rule_plmn_roam[WIFI_IDX].split(",");
                    if (UMTS_LEVEL == plmn_roam.length) {
                        if (plmn_roam[WCDMA_STRENGTH_POOR_STD].equals(hplmn)) {
                            if (plmn_roam[WIFI_IDX].equals(regplmn)) {
                                Rlog.d("HwGsmServiceStateTracker", "roaming customization by hplmn and regplmn success!");
                                if (WIFI_IDX == rule_roam) {
                                    return true;
                                }
                                if (UMTS_LEVEL == rule_roam) {
                                    return SHOW_4G_PLUS_ICON;
                                }
                            }
                        }
                        if (NETWORK_MODE_GSM_UMTS == rule_roam && hplmn.length() > UMTS_LEVEL && regplmn.length() > UMTS_LEVEL) {
                            String hplmnMcc = hplmn.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS);
                            String regplmnMcc = regplmn.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS);
                            if (plmn_roam[WCDMA_STRENGTH_POOR_STD].equals(hplmnMcc)) {
                                if (plmn_roam[WIFI_IDX].equals(regplmnMcc)) {
                                    roaming = SHOW_4G_PLUS_ICON;
                                }
                            }
                        }
                    } else {
                        if (NETWORK_MODE_GSM_UMTS == plmn_roam.length) {
                            Rlog.d("HwGsmServiceStateTracker", "roaming customization by RAT");
                            if (plmn_roam[WCDMA_STRENGTH_POOR_STD].equals(hplmn)) {
                                if (plmn_roam[WIFI_IDX].equals(regplmn) && plmn_roam[UMTS_LEVEL].contains(String.valueOf(netClass + WIFI_IDX))) {
                                    Rlog.d("HwGsmServiceStateTracker", "roaming customization by RAT success!");
                                    if (WIFI_IDX == rule_roam) {
                                        return true;
                                    }
                                    if (UMTS_LEVEL == rule_roam) {
                                        return SHOW_4G_PLUS_ICON;
                                    }
                                }
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
            return roaming;
        }
        Rlog.e("HwGsmServiceStateTracker", "mIccRecords null while getGsmRoamingCustByIMSIStart was called.");
        return roaming;
    }

    public void dispose() {
        if (this.mGsmPhone != null) {
            this.mCi.unregisterForCrrConn(this);
            this.mCi.unSetOnRegPLMNSelInfo(this);
            this.mGsmPhone.getContext().unregisterReceiver(this.mIntentReceiver);
            this.mGsmPhone.getContext().unregisterReceiver(this.mCloudOtaBroadcastReceiver);
            if (PS_CLEARCODE) {
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
        Rlog.i(LOG_TAG, "sendBroadcastRegPLMNSelInfo");
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
        if (this.mHwCustGsmServiceStateManager != null) {
            filter = this.mHwCustGsmServiceStateManager.getCustIntentFilter(filter);
        }
        this.mGsmPhone.getContext().registerReceiver(this.mIntentReceiver, filter);
    }

    public boolean needUpdateNITZTime() {
        int otherCardType;
        int otherCardState;
        int phoneId = this.mGsmPhone.getPhoneId();
        int ownCardType = HwTelephonyManagerInner.getDefault().getCardType(phoneId);
        if (phoneId == 0) {
            otherCardType = HwTelephonyManagerInner.getDefault().getCardType(WIFI_IDX);
            otherCardState = HwTelephonyManagerInner.getDefault().getSubState(1);
        } else if (phoneId == WIFI_IDX) {
            otherCardType = HwTelephonyManagerInner.getDefault().getCardType(WCDMA_STRENGTH_POOR_STD);
            otherCardState = HwTelephonyManagerInner.getDefault().getSubState(0);
        } else {
            otherCardType = -1;
            otherCardState = WCDMA_STRENGTH_POOR_STD;
        }
        Rlog.d("HwGsmServiceStateTracker", "ownCardType = " + ownCardType + ", otherCardType = " + otherCardType + ", otherCardState = " + otherCardState);
        if (ownCardType == 41 || ownCardType == 43) {
            Rlog.d("HwGsmServiceStateTracker", "Cdma card, uppdate NITZ time!");
            return true;
        } else if ((otherCardType == 30 || otherCardType == 43 || otherCardType == 41) && WIFI_IDX == otherCardState) {
            Rlog.d("HwGsmServiceStateTracker", "Other cdma card, ignore updating NITZ time!");
            return SHOW_4G_PLUS_ICON;
        } else if (phoneId == SystemProperties.getInt("gsm.sim.updatenitz", phoneId) || -1 == SystemProperties.getInt("gsm.sim.updatenitz", -1)) {
            SystemProperties.set("gsm.sim.updatenitz", String.valueOf(phoneId));
            Rlog.d("HwGsmServiceStateTracker", "Update NITZ time, set update card : " + phoneId);
            return true;
        } else {
            Rlog.d("HwGsmServiceStateTracker", "Ignore updating NITZ time, phoneid : " + phoneId);
            return SHOW_4G_PLUS_ICON;
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
            case WIFI_IDX /*1*/:
                ar = msg.obj;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "EVENT_ICC_RECORDS_EONS_UPDATED exception " + ar.exception);
                } else {
                    processIccEonsRecordsUpdated(((Integer) ar.result).intValue());
                }
            case EVENT_POLL_LOCATION_INFO /*64*/:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "EVENT_POLL_LOCATION_INFO exception " + ar.exception);
                    return;
                }
                String[] states = ar.result;
                Rlog.i(LOG_TAG, "CLEARCODE EVENT_POLL_LOCATION_INFO");
                if (states.length == LTE_LEVEL) {
                    try {
                        if (states[UMTS_LEVEL] != null && states[UMTS_LEVEL].length() > 0) {
                            this.mRac = Integer.parseInt(states[UMTS_LEVEL], EVDO_LEVEL);
                            Rlog.d(LOG_TAG, "CLEARCODE mRac = " + this.mRac);
                        }
                    } catch (NumberFormatException ex) {
                        Rlog.e(LOG_TAG, "error parsing LocationInfoState: " + ex);
                    }
                }
            case EVENT_RPLMNS_STATE_CHANGED /*101*/:
                Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]EVENT_RPLMNS_STATE_CHANGED");
                this.oldRplmn = this.rplmn;
                this.rplmn = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
                String mcc = "";
                String oldMcc = "";
                if (this.rplmn != null && this.rplmn.length() > NETWORK_MODE_GSM_UMTS) {
                    mcc = this.rplmn.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS);
                }
                if (this.oldRplmn != null && this.oldRplmn.length() > NETWORK_MODE_GSM_UMTS) {
                    oldMcc = this.oldRplmn.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS);
                }
                if (!("".equals(mcc) || mcc.equals(oldMcc))) {
                    this.mGsmPhone.notifyMccChanged(mcc);
                    Rlog.d(LOG_TAG, "rplmn mcc changed.");
                }
                Rlog.d(LOG_TAG, "rplmn" + this.rplmn);
                if (SystemProperties.getBoolean("ro.config.hw_globalEcc", SHOW_4G_PLUS_ICON)) {
                    Rlog.d(LOG_TAG, "the global emergency numbers custom-make does enable!!!!");
                    toGetRplmnsThenSendEccNum();
                }
            case EVENT_DELAY_UPDATE_REGISTER_STATE_DONE /*102*/:
                Rlog.d(LOG_TAG, "[Phone" + this.mPhoneId + "]Delay Timer expired, begin get register state");
                this.mRefreshState = true;
                this.mGsst.pollState();
            case EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH /*103*/:
                Rlog.d(LOG_TAG, "event update gsm signal strength");
                this.mDoubleSignalStrength.proccessAlaphFilter(this.mGsst.getSignalStrength(), this.mModemSignalStrength);
                this.mGsmPhone.notifySignalStrength();
            case EVENT_CA_STATE_CHANGED /*104*/:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    onCaStateChanged(((int[]) ar.result)[WCDMA_STRENGTH_POOR_STD] == WIFI_IDX ? true : SHOW_4G_PLUS_ICON);
                } else {
                    log("EVENT_CA_STATE_CHANGED: exception;");
                }
            case EVENT_NETWORK_REJECTED_CASE /*105*/:
                Rlog.d(LOG_TAG, "EVENT_NETWORK_REJECTED_CASE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "EVENT_NETWORK_REJECTED_CASE exception " + ar.exception);
                } else {
                    onNetworkReject(ar);
                }
            case EVENT_PLMN_SELINFO /*151*/:
                Rlog.d(LOG_TAG, "EVENT_PLMN_SELINFO");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    response = (int[]) ar.result;
                    if (response.length != 0) {
                        sendBroadcastRegPLMNSelInfo(response[WCDMA_STRENGTH_POOR_STD], response[WIFI_IDX]);
                    }
                }
            case EVENT_CRR_CONN /*152*/:
                Rlog.d(LOG_TAG, "GSM EVENT_CRR_CONN");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    response = ar.result;
                    if (response.length != 0) {
                        sendBroadcastCrrConnInd(response[WCDMA_STRENGTH_POOR_STD], response[WIFI_IDX], response[UMTS_LEVEL]);
                        return;
                    }
                    return;
                }
                Rlog.d(LOG_TAG, "GSM EVENT_CRR_CONN: exception;");
            default:
                super.handleMessage(msg);
        }
    }

    private void onNetworkReject(AsyncResult ar) {
        if (ar.exception == null) {
            String[] data = ar.result;
            String str = null;
            int rejectdomain = -1;
            int rejectcause = -1;
            int rejectrat = -1;
            if (data.length > 0) {
                try {
                    if (data[WCDMA_STRENGTH_POOR_STD] != null && data[WCDMA_STRENGTH_POOR_STD].length() > 0) {
                        str = data[WCDMA_STRENGTH_POOR_STD];
                    }
                    if (data[WIFI_IDX] != null && data[WIFI_IDX].length() > 0) {
                        rejectdomain = Integer.parseInt(data[WIFI_IDX]);
                    }
                    if (data[UMTS_LEVEL] != null && data[UMTS_LEVEL].length() > 0) {
                        rejectcause = Integer.parseInt(data[UMTS_LEVEL]);
                    }
                    if (data[NETWORK_MODE_GSM_UMTS] != null && data[NETWORK_MODE_GSM_UMTS].length() > 0) {
                        rejectrat = Integer.parseInt(data[NETWORK_MODE_GSM_UMTS]);
                    }
                } catch (Exception ex) {
                    Rlog.e(LOG_TAG, "error parsing NetworkReject!", ex);
                }
                if (UMTS_LEVEL == rejectrat) {
                    this.rejNum += WIFI_IDX;
                }
                Rlog.d(LOG_TAG, "NetworkReject:PLMN = " + str + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat + " rejNum = " + this.rejNum);
                if (this.rejNum >= NETWORK_MODE_GSM_UMTS) {
                    this.mGsmPhone.setPreferredNetworkType(NETWORK_MODE_GSM_UMTS, null);
                    Global.putInt(this.mGsmPhone.getContext().getContentResolver(), "preferred_network_mode", NETWORK_MODE_GSM_UMTS);
                    this.rejNum = WCDMA_STRENGTH_POOR_STD;
                    this.mRac = -1;
                }
            }
        }
    }

    public boolean isNetworkTypeChanged(SignalStrength oldSS, SignalStrength newSS) {
        int newState = WCDMA_STRENGTH_POOR_STD;
        int oldState = WCDMA_STRENGTH_POOR_STD;
        if (newSS.getGsmSignalStrength() < -1) {
            newState = WIFI_IDX;
        }
        if (oldSS.getGsmSignalStrength() < -1) {
            oldState = WIFI_IDX;
        }
        if (newSS.getWcdmaRscp() < -1) {
            newState |= UMTS_LEVEL;
        }
        if (oldSS.getWcdmaRscp() < -1) {
            oldState |= UMTS_LEVEL;
        }
        if (newSS.getLteRsrp() < -1) {
            newState |= LTE_LEVEL;
        }
        if (oldSS.getLteRsrp() < -1) {
            oldState |= LTE_LEVEL;
        }
        if (newState == 0 || newState == oldState) {
            return SHOW_4G_PLUS_ICON;
        }
        return true;
    }

    public void sendMeesageDelayUpdateSingalStrength() {
        Message msg = obtainMessage();
        msg.what = EVENT_DELAY_UPDATE_GSM_SIGNAL_STRENGTH;
        sendMessageDelayed(msg, (long) mDelayDuringTime);
    }

    public boolean notifySignalStrength(SignalStrength oldSS, SignalStrength newSS) {
        boolean notified;
        this.mModemSignalStrength = new SignalStrength(newSS);
        Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]Process notify signal strenght! ver.02");
        if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]In delay update register state process, no notify signal");
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
            Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]has delay update message, don't proccess alpha filter immediately!");
            notified = SHOW_4G_PLUS_ICON;
        } else {
            this.mOldDoubleSignalStrength = this.mDoubleSignalStrength;
            this.mDoubleSignalStrength = new DoubleSignalStrength(newSS);
            this.mDoubleSignalStrength.proccessAlaphFilter(this.mOldDoubleSignalStrength, newSS, this.mModemSignalStrength, SHOW_4G_PLUS_ICON);
            notified = true;
        }
        if (notified) {
            try {
                Rlog.d(LOG_TAG, "SUB[" + this.mPhoneId + "]notifySignalStrength.");
                this.mGsmPhone.notifySignalStrength();
            } catch (NullPointerException ex) {
                Rlog.e(LOG_TAG, "onSignalStrengthResult() Phone already destroyed: " + ex + "SignalStrength not notified");
            }
        }
        return notified;
    }

    public int getCARilRadioType(int type) {
        int radioType = type;
        if (SHOW_4G_PLUS_ICON && 31 == type) {
            radioType = 14;
        }
        Rlog.d(LOG_TAG, "[CA] CA updateCAStatus  radioType=" + radioType + " type=" + type);
        return radioType;
    }

    public int updateCAStatus(int currentType) {
        int newType = currentType;
        if (!SHOW_4G_PLUS_ICON) {
            return newType;
        }
        Rlog.d(LOG_TAG, "[CA] currentType=" + currentType + "    oldCAstate=" + this.mOldCAstate);
        boolean HasCAactivated = (31 != currentType || 31 == this.mOldCAstate) ? SHOW_4G_PLUS_ICON : true;
        boolean HasCAdeActivated = (31 == currentType || 31 != this.mOldCAstate) ? SHOW_4G_PLUS_ICON : true;
        this.mOldCAstate = currentType;
        if (HasCAactivated || HasCAdeActivated) {
            Intent intentLteCAState = new Intent("android.intent.action.LTE_CA_STATE");
            intentLteCAState.putExtra("subscription", this.mGsmPhone.getSubId());
            if (HasCAactivated) {
                intentLteCAState.putExtra("LteCAstate", true);
                Rlog.d(LOG_TAG, "[CA] CA activated !");
            } else if (HasCAdeActivated) {
                intentLteCAState.putExtra("LteCAstate", SHOW_4G_PLUS_ICON);
                Rlog.d(LOG_TAG, "[CA] CA deactivated !");
            }
            this.mGsmPhone.getContext().sendBroadcast(intentLteCAState);
        }
        if (31 == currentType) {
            return 14;
        }
        return newType;
    }

    private void onCaStateChanged(boolean caActive) {
        Rlog.d(LOG_TAG, "onCaStateChanged caActive=" + caActive);
        boolean oldCaActive = 31 == this.mOldCAstate ? true : SHOW_4G_PLUS_ICON;
        if (SHOW_4G_PLUS_ICON && oldCaActive != caActive) {
            if (caActive) {
                this.mOldCAstate = 31;
                Rlog.d(LOG_TAG, "[CA] CA activated !");
            } else {
                this.mOldCAstate = WCDMA_STRENGTH_POOR_STD;
                Rlog.d(LOG_TAG, "[CA] CA deactivated !");
            }
            Intent intentLteCAState = new Intent("android.intent.action.LTE_CA_STATE");
            intentLteCAState.putExtra("subscription", this.mGsmPhone.getSubId());
            intentLteCAState.putExtra("LteCAstate", caActive);
            this.mGsmPhone.getContext().sendBroadcast(intentLteCAState);
        }
    }

    private void processIccEonsRecordsUpdated(int eventCode) {
        switch (eventCode) {
            case UMTS_LEVEL /*2*/:
                this.mGsst.updateSpnDisplay();
            case HwChrVSimConstants.CHR_VSIM_SLOTS_TABLE_UNKNOWN /*100*/:
                this.mGsst.updateSpnDisplay();
            default:
        }
    }

    public void unregisterForRecordsEvents(IccRecords r) {
        if (r != null) {
            r.unregisterForRecordsEvents(this);
        }
    }

    public void registerForRecordsEvents(IccRecords r) {
        if (r != null) {
            r.registerForRecordsEvents(this, WIFI_IDX, null);
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
        return SHOW_4G_PLUS_ICON;
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
            return SHOW_4G_PLUS_ICON;
        }
        boolean lostNework = (oldSS.getVoiceRegState() == 0 || oldSS.getDataRegState() == 0) ? newSS.getVoiceRegState() != 0 ? newSS.getDataRegState() != 0 ? true : SHOW_4G_PLUS_ICON : SHOW_4G_PLUS_ICON : SHOW_4G_PLUS_ICON;
        boolean isSubDeactivated = SubscriptionController.getInstance().getSubState(this.mGsmPhone.getSubId()) == 0 ? true : SHOW_4G_PLUS_ICON;
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "]process delay update register state lostNework : " + lostNework + ", desiredPowerState : " + this.mGsst.getDesiredPowerState() + ", radiostate : " + this.mCi.getRadioState() + ", mRadioOffByDoRecovery : " + this.mGsst.getDoRecoveryTriggerState() + ", isSubDeactivated : " + isSubDeactivated + ", newMainSlot : " + newMainSlot + ", phoneOOS : " + this.mGsmPhone.getOOSFlag());
        if (newSS.getDataRegState() == 0 || newSS.getVoiceRegState() == 0 || !this.mGsst.getDesiredPowerState() || this.mCi.getRadioState() == RadioState.RADIO_OFF || this.mGsst.getDoRecoveryTriggerState() || isSubDeactivated || this.mMainSlot != newMainSlot || this.mGsmPhone.getOOSFlag() || newSS.getDataRegState() == NETWORK_MODE_GSM_UMTS) {
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
        this.mRefreshState = SHOW_4G_PLUS_ICON;
        return SHOW_4G_PLUS_ICON;
    }

    private int getSendDeregisterStateDelayedTime(ServiceState oldSS, ServiceState newSS) {
        boolean isCsLostNetwork = (oldSS.getVoiceRegState() != 0 || newSS.getVoiceRegState() == 0) ? SHOW_4G_PLUS_ICON : newSS.getDataRegState() != 0 ? true : SHOW_4G_PLUS_ICON;
        boolean isPsLostNetwork = (oldSS.getDataRegState() != 0 || newSS.getVoiceRegState() == 0) ? SHOW_4G_PLUS_ICON : newSS.getDataRegState() != 0 ? true : SHOW_4G_PLUS_ICON;
        int delayedTime = WCDMA_STRENGTH_POOR_STD;
        TelephonyManager.getDefault();
        int networkClass = TelephonyManager.getNetworkClass(oldSS.getNetworkType());
        if (isCsLostNetwork) {
            if (networkClass == WIFI_IDX) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_CS_2G;
            } else if (networkClass == UMTS_LEVEL) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_CS_3G;
            } else if (networkClass == NETWORK_MODE_GSM_UMTS) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_CS_4G;
            } else {
                delayedTime = DELAYED_TIME_DEFAULT_VALUE * HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
            }
        } else if (isPsLostNetwork) {
            if (networkClass == WIFI_IDX) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_PS_2G;
            } else if (networkClass == UMTS_LEVEL) {
                delayedTime = DELAYED_TIME_NETWORKSTATUS_PS_3G;
            } else if (networkClass == NETWORK_MODE_GSM_UMTS) {
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
        return SHOW_4G_PLUS_ICON;
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

    public void sendGsmRoamingIntentIfDenied(int regState, String[] states) {
        if (states == null) {
            Rlog.d(LOG_TAG, "[SLOT" + this.mPhoneId + "] states is null.");
            return;
        }
        if ((regState == NETWORK_MODE_GSM_UMTS || regState == 13) && states.length >= 14) {
            try {
                if (Integer.parseInt(states[13]) == 10) {
                    Rlog.d(LOG_TAG, "Posting Managed roaming intent sub = " + this.mGsmPhone.getSubId());
                    Intent intent = new Intent("codeaurora.intent.action.ACTION_MANAGED_ROAMING_IND");
                    intent.putExtra("subscription", this.mGsmPhone.getSubId());
                    this.mGsmPhone.getContext().sendBroadcast(intent);
                }
            } catch (NumberFormatException ex) {
                Rlog.e(LOG_TAG, "error parsing regCode: " + ex);
            }
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
        Rlog.d(LOG_TAG, "getOnsDisplayParamsBySpnOnly spn:" + spnSim + ",hplmn:" + hPlmn + ",regPlmn:" + regPlmn);
        if (!TextUtils.isEmpty(spnSim) && isMccForSpn(r.getOperatorNumeric()) && !TextUtils.isEmpty(hPlmn) && hPlmn.length() > NETWORK_MODE_GSM_UMTS) {
            String currentMcc = hPlmn.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS);
            if (!TextUtils.isEmpty(regPlmn) && regPlmn.length() > NETWORK_MODE_GSM_UMTS && currentMcc.equals(regPlmn.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS))) {
                mShowSpn = true;
                mShowPlmn = SHOW_4G_PLUS_ICON;
                spnRes = spnSim;
                mRule = ruleSim;
                plmnRes = "";
            }
        }
        return new OnsDisplayParams(mShowSpn, mShowPlmn, mRule, plmnRes, spnRes);
    }

    private boolean isMccForSpn(String currentMccmnc) {
        String strMcc = Systemex.getString(this.mContext.getContentResolver(), "hw_mcc_showspn_only");
        HashSet<String> mShowspnOnlyMcc = new HashSet();
        String currentMcc = "";
        if (currentMccmnc == null || currentMccmnc.length() < NETWORK_MODE_GSM_UMTS) {
            return SHOW_4G_PLUS_ICON;
        }
        currentMcc = currentMccmnc.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS);
        if (strMcc == null || mShowspnOnlyMcc.size() != 0) {
            return SHOW_4G_PLUS_ICON;
        }
        String[] mcc = strMcc.split(",");
        for (int i = WCDMA_STRENGTH_POOR_STD; i < mcc.length; i += WIFI_IDX) {
            mShowspnOnlyMcc.add(mcc[i].trim());
        }
        return mShowspnOnlyMcc.contains(currentMcc);
    }

    private boolean getNoRoamingByMcc(ServiceState mSS) {
        IccRecords r = (IccRecords) this.mGsmPhone.mIccRecords.get();
        if (!(r == null || mSS == null)) {
            String hplmn = r.getOperatorNumeric();
            String regplmn = mSS.getOperatorNumeric();
            if (isMccForNoRoaming(hplmn)) {
                String currentMcc = hplmn.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS);
                if (regplmn != null && regplmn.length() > NETWORK_MODE_GSM_UMTS && currentMcc.equals(regplmn.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS))) {
                    return true;
                }
            }
        }
        return SHOW_4G_PLUS_ICON;
    }

    private boolean isMccForNoRoaming(String currentMccmnc) {
        String strMcc = Systemex.getString(this.mContext.getContentResolver(), "hw_mcc_show_no_roaming");
        HashSet<String> mShowNoRoamingMcc = new HashSet();
        String currentMcc = "";
        if (currentMccmnc == null || currentMccmnc.length() < NETWORK_MODE_GSM_UMTS) {
            return SHOW_4G_PLUS_ICON;
        }
        currentMcc = currentMccmnc.substring(WCDMA_STRENGTH_POOR_STD, NETWORK_MODE_GSM_UMTS);
        if (strMcc == null || mShowNoRoamingMcc.size() != 0) {
            return SHOW_4G_PLUS_ICON;
        }
        String[] mcc = strMcc.split(",");
        for (int i = WCDMA_STRENGTH_POOR_STD; i < mcc.length; i += WIFI_IDX) {
            mShowNoRoamingMcc.add(mcc[i].trim());
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
            this.mCi.getLocationInfo(obtainMessage(EVENT_POLL_LOCATION_INFO));
        }
    }

    private void registerCloudOtaBroadcastReceiver() {
        Rlog.e(LOG_TAG, "HwCloudOTAService registerCloudOtaBroadcastReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(CLOUD_OTA_MCC_UPDATE);
        filter.addAction(CLOUD_OTA_DPLMN_UPDATE);
        this.mGsmPhone.getContext().registerReceiver(this.mCloudOtaBroadcastReceiver, filter, CLOUD_OTA_PERMISSION, null);
    }
}
