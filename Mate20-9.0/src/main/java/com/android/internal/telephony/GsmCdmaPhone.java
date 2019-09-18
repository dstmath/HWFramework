package com.android.internal.telephony;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.encrypt.PasswordUtil;
import android.hsm.HwSystemManager;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UssdResponse;
import android.telephony.cdma.CdmaCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.cdma.CdmaMmiCode;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccException;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccVmNotSupportedException;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.IsimUiccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.uicc.UiccSlot;
import com.android.internal.telephony.util.HwCustUtil;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.google.android.mms.pdu.CharacterSets;
import huawei.cust.HwGetCfgFileConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GsmCdmaPhone extends AbstractGsmCdmaPhone implements IHwGsmCdmaPhoneInner {
    private static final boolean CALL_WAITING_CLASS_NONE = SystemProperties.getBoolean("ro.config.cw_no_class", false);
    public static final int CANCEL_ECM_TIMER = 1;
    public static final String CF_ENABLED = "cf_enabled_key";
    private static final boolean DBG = true;
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    private static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", false);
    private static final int INVALID_SYSTEM_SELECTION_CODE = -1;
    private static final String IS683A_FEATURE_CODE = "*228";
    private static final int IS683A_FEATURE_CODE_NUM_DIGITS = 4;
    private static final int IS683A_SYS_SEL_CODE_NUM_DIGITS = 2;
    private static final int IS683A_SYS_SEL_CODE_OFFSET = 4;
    private static final int IS683_CONST_1900MHZ_A_BLOCK = 2;
    private static final int IS683_CONST_1900MHZ_B_BLOCK = 3;
    private static final int IS683_CONST_1900MHZ_C_BLOCK = 4;
    private static final int IS683_CONST_1900MHZ_D_BLOCK = 5;
    private static final int IS683_CONST_1900MHZ_E_BLOCK = 6;
    private static final int IS683_CONST_1900MHZ_F_BLOCK = 7;
    private static final int IS683_CONST_800MHZ_A_BAND = 0;
    private static final int IS683_CONST_800MHZ_B_BAND = 1;
    private static final boolean IS_FULL_NETWORK_SUPPORTED = SystemProperties.getBoolean("ro.config.full_network_support", false);
    public static final String LOG_TAG_STATIC = "GsmCdmaPhone";
    private static final int NO_VALUES = -1;
    public static final String PROPERTY_CDMA_HOME_OPERATOR_NUMERIC = "ro.cdma.home.operator.numeric";
    private static final int REPORTING_HYSTERESIS_DB = 2;
    private static final int REPORTING_HYSTERESIS_KBPS = 50;
    private static final int REPORTING_HYSTERESIS_MILLIS = 3000;
    public static final int RESTART_ECM_TIMER = 0;
    private static final String SC_WAIT = "43";
    private static final int SUBID_0 = 0;
    private static final int SUBID_1 = 1;
    private static final int VALUES_FALSE = 0;
    private static final int VALUES_TRUE = 1;
    private static final boolean VDBG = false;
    private static final String VM_NUMBER = "vm_number_key";
    private static final String VM_NUMBER_CDMA = "vm_number_key_cdma";
    private static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    private static PasswordUtil mPasswordUtil = HwFrameworkFactory.getPasswordUtil();
    private static Pattern pOtaSpNumSchema = Pattern.compile("[,\\s]+");
    public String LOG_TAG;
    private boolean mBroadcastEmergencyCallStateChanges;
    private BroadcastReceiver mBroadcastReceiver;
    private CarrierKeyDownloadManager mCDM;
    private CarrierInfoManager mCIM;
    public GsmCdmaCallTracker mCT;
    private CarrierIdentifier mCarrerIdentifier;
    private String mCarrierOtaSpNumSchema;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public int mCdmaSubscriptionSource;
    private Registrant mEcmExitRespRegistrant;
    private final RegistrantList mEcmTimerResetRegistrants;
    private final RegistrantList mEriFileLoadedRegistrants;
    public EriManager mEriManager;
    private String mEsn;
    private Runnable mExitEcmRunnable;
    public IHwGsmCdmaPhoneEx mHwGsmCdmaPhoneEx;
    private IccPhoneBookInterfaceManager mIccPhoneBookIntManager;
    private IccSmsInterfaceManager mIccSmsInterfaceManager;
    private String mImei;
    private String mImeiSv;
    private IsimUiccRecords mIsimUiccRecords;
    private String mMeid;
    private ArrayList<MmiCode> mPendingMMIs;
    private int mPrecisePhoneType;
    private boolean mResetModemOnRadioTechnologyChange;
    private int mRilVersion;
    private AsyncResult mSSNResult;
    public ServiceStateTracker mSST;
    private SIMRecords mSimRecords;
    private RegistrantList mSsnRegistrants;
    protected String mUimid;
    private String mVmNumber;
    private PowerManager.WakeLock mWakeLock;
    private IccCardApplicationStatus.AppType newAppType;
    private IccCardApplicationStatus.AppType oldAppType;
    private final BroadcastReceiver sConfigChangeReceiver;

    /* renamed from: com.android.internal.telephony.GsmCdmaPhone$4  reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$DctConstants$Activity = new int[DctConstants.Activity.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$DctConstants$State = new int[DctConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$Activity[DctConstants.Activity.DATAIN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$Activity[DctConstants.Activity.DATAOUT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$Activity[DctConstants.Activity.DATAINANDOUT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$Activity[DctConstants.Activity.DORMANT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.DISCONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public static class Cfu {
        final Message mOnComplete;
        final String mSetCfNumber;

        Cfu(String cfNumber, Message onComplete) {
            this.mSetCfNumber = cfNumber;
            this.mOnComplete = onComplete;
        }
    }

    public GsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        this(context, ci, notifier, false, phoneId, precisePhoneType, telephonyComponentFactory);
        this.LOG_TAG += "[SUB" + phoneId + "]";
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public GsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, boolean unitTestMode, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        super(r9 == 1 ? AbstractGsmCdmaPhone.GSM_PHONE : AbstractGsmCdmaPhone.CDMA_PHONE, notifier, context, ci, unitTestMode, phoneId, telephonyComponentFactory);
        int i = precisePhoneType;
        this.LOG_TAG = LOG_TAG_STATIC;
        this.mSsnRegistrants = new RegistrantList();
        this.mCdmaSubscriptionSource = -1;
        this.mEriFileLoadedRegistrants = new RegistrantList();
        this.oldAppType = IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN;
        this.newAppType = IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN;
        this.mExitEcmRunnable = new Runnable() {
            public void run() {
                GsmCdmaPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.mPendingMMIs = new ArrayList<>();
        this.mEcmTimerResetRegistrants = new RegistrantList();
        this.mResetModemOnRadioTechnologyChange = false;
        this.mBroadcastEmergencyCallStateChanges = false;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String str = GsmCdmaPhone.this.LOG_TAG;
                    Rlog.d(str, "mBroadcastReceiver: action " + intent.getAction());
                    if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                        GsmCdmaPhone.this.sendMessage(GsmCdmaPhone.this.obtainMessage(43));
                    } else if ("com.huawei.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction()) && intent.getExtras() != null) {
                        int slot = intent.getExtras().getInt("slot");
                        int state = intent.getExtras().getInt("state");
                        int phoneid = GsmCdmaPhone.this.getPhoneId();
                        String str2 = GsmCdmaPhone.this.LOG_TAG;
                        Rlog.d(str2, " onReceive action slot = " + slot + ",state = " + state);
                        if (1 == state && phoneid == slot) {
                            HwGetCfgFileConfig.readCfgFileConfig("xml/telephony-various.xml", slot);
                        } else if (2 == state && phoneid == slot) {
                            HwGetCfgFileConfig.clearCfgFileConfig(slot);
                        }
                    }
                }
            }
        };
        this.sConfigChangeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    GsmCdmaPhone.this.logd("Carrier config changed. Reloading config");
                    if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                        GsmCdmaPhone.this.mCi.getVoiceRadioTechnology(GsmCdmaPhone.this.obtainMessage(40));
                    }
                }
            }
        };
        this.mPrecisePhoneType = i;
        initOnce(ci);
        initRatSpecific(i);
        this.mCarrierActionAgent = this.mTelephonyComponentFactory.makeCarrierActionAgent(this);
        this.mCarrierSignalAgent = this.mTelephonyComponentFactory.makeCarrierSignalAgent(this);
        this.mSST = this.mTelephonyComponentFactory.makeServiceStateTracker(this, this.mCi);
        this.mDcTracker = this.mTelephonyComponentFactory.makeDcTracker(this);
        this.mCarrerIdentifier = this.mTelephonyComponentFactory.makeCarrierIdentifier(this);
        this.mSST.registerForNetworkAttached(this, 19, null);
        HwTelephonyFactory.getHwPhoneManager().setGsmCdmaPhone(this, context);
        this.mHwGsmCdmaPhoneEx = HwTelephonyFactory.getHwPhoneManager().createHwGsmCdmaPhoneEx(this);
        if (this.mHwGsmCdmaPhoneEx != null) {
            this.mHwGsmCdmaPhoneEx.restoreSavedRadioTech();
        }
        this.mCi.getVoiceRadioTechnology(obtainMessage(40));
        this.mDeviceStateMonitor = this.mTelephonyComponentFactory.makeDeviceStateMonitor(this);
        logd("GsmCdmaPhone: constructor: sub = " + this.mPhoneId);
    }

    private void initOnce(CommandsInterface ci) {
        if (ci instanceof SimulatedRadioControl) {
            this.mSimulatedRadioControl = (SimulatedRadioControl) ci;
        }
        this.mCT = this.mTelephonyComponentFactory.makeGsmCdmaCallTracker(this);
        this.mIccPhoneBookIntManager = this.mTelephonyComponentFactory.makeIccPhoneBookInterfaceManager(this);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, this.LOG_TAG);
        this.mIccSmsInterfaceManager = this.mTelephonyComponentFactory.makeIccSmsInterfaceManager(this);
        this.mContext.registerReceiver(this.sConfigChangeReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
        this.mCi.registerForAvailable(this, 1, null);
        this.mCi.registerForOffOrNotAvailable(this, 8, null);
        this.mCi.registerForOn(this, 5, null);
        this.mCi.setOnSuppServiceNotification(this, 2, null);
        this.mCi.setOnUSSD(this, 7, null);
        this.mCi.setOnSs(this, 36, null);
        this.mCdmaSSM = this.mTelephonyComponentFactory.getCdmaSubscriptionSourceManagerInstance(this.mContext, this.mCi, this, 27, null);
        this.mEriManager = this.mTelephonyComponentFactory.makeEriManager(this, this.mContext, 0);
        this.mCi.setEmergencyCallbackMode(this, 25, null);
        this.mCi.registerForExitEmergencyCallbackMode(this, 26, null);
        this.mCi.registerForModemReset(this, 45, null);
        this.mCarrierOtaSpNumSchema = TelephonyManager.from(this.mContext).getOtaSpNumberSchemaForPhone(getPhoneId(), "");
        this.mResetModemOnRadioTechnologyChange = SystemProperties.getBoolean("persist.radio.reset_on_switch", false);
        this.mCi.registerForRilConnected(this, 41, null);
        this.mCi.registerForVoiceRadioTechChanged(this, 39, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("com.huawei.action.CARRIER_CONFIG_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mCDM = new CarrierKeyDownloadManager(this);
        this.mCIM = new CarrierInfoManager();
    }

    private void initRatSpecific(int precisePhoneType) {
        this.mPendingMMIs.clear();
        this.mEsn = null;
        this.mMeid = null;
        this.mPrecisePhoneType = precisePhoneType;
        logd("Precise phone type " + this.mPrecisePhoneType);
        TelephonyManager tm = TelephonyManager.from(this.mContext);
        UiccProfile uiccProfile = getUiccProfile();
        boolean isCardAbsentOrNotReady = true;
        if (isPhoneTypeGsm()) {
            this.mCi.setPhoneType(1);
            tm.setPhoneType(getPhoneId(), 1);
            if (uiccProfile != null) {
                uiccProfile.setVoiceRadioTech(3);
                return;
            }
            return;
        }
        this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
        this.mIsPhoneInEcmState = Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(getPhoneId(), "ril.cdma.inecmmode", "false"));
        if (this.mIsPhoneInEcmState) {
            this.mCi.exitEmergencyCallbackMode(obtainMessage(26));
        }
        this.mCi.setPhoneType(2);
        tm.setPhoneType(getPhoneId(), 2);
        if (uiccProfile != null) {
            uiccProfile.setVoiceRadioTech(6);
        }
        String operatorAlpha = SystemProperties.get("ro.cdma.home.operator.alpha");
        String operatorNumeric = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
        logd("init: operatorAlpha='" + operatorAlpha + "' operatorNumeric='" + operatorNumeric + "'");
        String simState = TelephonyManager.getTelephonyProperty(this.mPhoneId, "gsm.sim.state", "UNKNOWN");
        StringBuilder sb = new StringBuilder();
        sb.append("init: simState = ");
        sb.append(simState);
        logd(sb.toString());
        if (!"UNKNOWN".equals(simState) && !"ABSENT".equals(simState) && !"NOT_READY".equals(simState)) {
            isCardAbsentOrNotReady = false;
        }
        if (!isCardAbsentOrNotReady) {
            if (!TextUtils.isEmpty(operatorAlpha)) {
                logd("init: set 'gsm.sim.operator.alpha' to operator='" + operatorAlpha + "'");
                tm.setSimOperatorNameForPhone(this.mPhoneId, operatorAlpha);
            }
            if (!TextUtils.isEmpty(operatorNumeric)) {
                logd("init: set 'gsm.sim.operator.numeric' to operator='" + operatorNumeric + "'");
                StringBuilder sb2 = new StringBuilder();
                sb2.append("update icc_operator_numeric=");
                sb2.append(operatorNumeric);
                logd(sb2.toString());
                tm.setSimOperatorNumericForPhone(this.mPhoneId, operatorNumeric);
                SubscriptionController.getInstance().setMccMnc(operatorNumeric, getSubId());
                setIsoCountryProperty(operatorNumeric);
                logd("update mccmnc=" + operatorNumeric);
            }
        }
        updateCurrentCarrierInProvider(operatorNumeric);
    }

    private void setIsoCountryProperty(String operatorNumeric) {
        TelephonyManager tm = TelephonyManager.from(this.mContext);
        if (TextUtils.isEmpty(operatorNumeric)) {
            logd("setIsoCountryProperty: clear 'gsm.sim.operator.iso-country'");
            tm.setSimCountryIsoForPhone(this.mPhoneId, "");
            return;
        }
        String iso = "";
        try {
            iso = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
        } catch (NumberFormatException ex) {
            Rlog.e(this.LOG_TAG, "setIsoCountryProperty: countryCodeForMcc error", ex);
        } catch (StringIndexOutOfBoundsException ex2) {
            Rlog.e(this.LOG_TAG, "setIsoCountryProperty: countryCodeForMcc error", ex2);
        }
        logd("setIsoCountryProperty: set 'gsm.sim.operator.iso-country' to iso=" + iso);
        tm.setSimCountryIsoForPhone(this.mPhoneId, iso);
    }

    public boolean isPhoneTypeGsm() {
        return this.mPrecisePhoneType == 1;
    }

    public boolean isPhoneTypeCdma() {
        return this.mPrecisePhoneType == 2;
    }

    public boolean isPhoneTypeCdmaLte() {
        return this.mPrecisePhoneType == 6;
    }

    private void switchPhoneType(int precisePhoneType) {
        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(getContext());
        boolean isInEcm = Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(getPhoneId(), "ril.cdma.inecmmode", "false"));
        logd("switchPhoneType,isInEcm=" + isInEcm);
        logd("switchPhoneType:getPhoneId=" + getPhoneId() + "getDefaultDataPhoneId=" + mSubscriptionManager.getDefaultDataPhoneId());
        if (isInEcm && getPhoneId() == mSubscriptionManager.getDefaultDataPhoneId()) {
            this.mDcTracker.setInternalDataEnabled(true);
            notifyEmergencyCallRegistrants(false);
        }
        removeCallbacks(this.mExitEcmRunnable);
        initRatSpecific(precisePhoneType);
        this.mSST.updatePhoneType();
        setPhoneName(precisePhoneType == 1 ? AbstractGsmCdmaPhone.GSM_PHONE : AbstractGsmCdmaPhone.CDMA_PHONE);
        onUpdateIccAvailability();
        this.mCT.updatePhoneType();
        CommandsInterface.RadioState radioState = this.mCi.getRadioState();
        if (radioState.isAvailable()) {
            handleRadioAvailable();
            sendMessage(obtainMessage(1));
            if (radioState.isOn()) {
                handleRadioOn();
            }
        }
        if (!radioState.isAvailable() || !radioState.isOn()) {
            handleRadioOffOrNotAvailable();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        logd("GsmCdmaPhone finalized");
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            Rlog.e(this.LOG_TAG, "UNEXPECTED; mWakeLock is held when finalizing.");
            this.mWakeLock.release();
        }
    }

    public ServiceState getServiceState() {
        if ((this.mSST == null || this.mSST.mSS.getState() != 0) && this.mImsPhone != null) {
            return ServiceState.mergeServiceStates(this.mSST == null ? new ServiceState() : this.mSST.mSS, this.mImsPhone.getServiceState());
        } else if (this.mSST != null) {
            return this.mSST.mSS;
        } else {
            return new ServiceState();
        }
    }

    public CellLocation getCellLocation(WorkSource workSource) {
        if (isPhoneTypeGsm()) {
            return this.mSST.getCellLocation(workSource);
        }
        CdmaCellLocation loc = (CdmaCellLocation) this.mSST.mCellLoc;
        if (Settings.Secure.getInt(getContext().getContentResolver(), "location_mode", 0) == 0) {
            CdmaCellLocation privateLoc = new CdmaCellLocation();
            privateLoc.setCellLocationData(loc.getBaseStationId(), KeepaliveStatus.INVALID_HANDLE, KeepaliveStatus.INVALID_HANDLE, loc.getSystemId(), loc.getNetworkId());
            privateLoc.setLacAndCid(loc.getLac(), loc.getCid());
            privateLoc.setPsc(loc.getPsc());
            loc = privateLoc;
        }
        return loc;
    }

    public PhoneConstants.State getState() {
        if (this.mImsPhone != null) {
            PhoneConstants.State imsState = this.mImsPhone.getState();
            if (imsState != PhoneConstants.State.IDLE) {
                return imsState;
            }
        }
        if (this.mCT == null) {
            return PhoneConstants.State.IDLE;
        }
        return this.mCT.mState;
    }

    public int getPhoneType() {
        if (this.mPrecisePhoneType == 1) {
            return 1;
        }
        return 2;
    }

    public ServiceStateTracker getServiceStateTracker() {
        return this.mSST;
    }

    public CallTracker getCallTracker() {
        return this.mCT;
    }

    public void updateVoiceMail() {
        if (isPhoneTypeGsm()) {
            int countVoiceMessages = 0;
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                countVoiceMessages = r.getVoiceMessageCount();
            }
            if (countVoiceMessages == -2) {
                countVoiceMessages = getStoredVoiceMessageCount();
            }
            logd("updateVoiceMail countVoiceMessages = " + countVoiceMessages + " subId " + getSubId());
            setVoiceMessageCount(countVoiceMessages);
            return;
        }
        setVoiceMessageCount(getStoredVoiceMessageCount());
    }

    public boolean getCallForwardingIndicator() {
        boolean cf = false;
        boolean z = false;
        if (((IccRecords) this.mIccRecords.get()) != null) {
            cf = ((IccRecords) this.mIccRecords.get()).getVoiceCallForwardingFlag() == 1;
        }
        if (!cf) {
            if (getCallForwardingPreference() && getSubscriberId() != null && getSubscriberId().equals(getVmSimImsi())) {
                z = true;
            }
            cf = z;
        }
        Rlog.d(this.LOG_TAG, "getCallForwardingIndicator getPhoneId=" + getPhoneId() + ", cf=" + cf);
        return cf;
    }

    public List<? extends MmiCode> getPendingMmiCodes() {
        return this.mPendingMMIs;
    }

    public PhoneConstants.DataState getDataConnectionState(String apnType) {
        PhoneConstants.DataState ret;
        PhoneConstants.DataState dataState = PhoneConstants.DataState.DISCONNECTED;
        if (this.mSST == null) {
            ret = PhoneConstants.DataState.DISCONNECTED;
        } else if (this.mSST.getCurrentDataConnectionState() == 0 || (!isPhoneTypeCdma() && !isPhoneTypeCdmaLte() && (!isPhoneTypeGsm() || apnType.equals("emergency")))) {
            switch (AnonymousClass4.$SwitchMap$com$android$internal$telephony$DctConstants$State[this.mDcTracker.getState(apnType).ordinal()]) {
                case 1:
                case 2:
                    if (this.mCT.mState != PhoneConstants.State.IDLE && !this.mSST.isConcurrentVoiceAndDataAllowed()) {
                        ret = PhoneConstants.DataState.SUSPENDED;
                        break;
                    } else {
                        ret = PhoneConstants.DataState.CONNECTED;
                        break;
                    }
                case 3:
                    ret = PhoneConstants.DataState.CONNECTING;
                    break;
                default:
                    ret = PhoneConstants.DataState.DISCONNECTED;
                    break;
            }
        } else {
            ret = PhoneConstants.DataState.DISCONNECTED;
        }
        if (HwTelephonyFactory.getHwDataConnectionManager().isSlaveActive() && getSubId() != SubscriptionController.getInstance().getDefaultDataSubId()) {
            logd("Slave is active, set state to DISCONNECTED.");
            ret = PhoneConstants.DataState.DISCONNECTED;
        }
        logd("getDataConnectionState apnType=" + apnType + " ret=" + ret);
        return ret;
    }

    public PhoneInternalInterface.DataActivityState getDataActivityState() {
        PhoneInternalInterface.DataActivityState ret = PhoneInternalInterface.DataActivityState.NONE;
        if (this.mSST.getCurrentDataConnectionState() != 0) {
            return ret;
        }
        switch (AnonymousClass4.$SwitchMap$com$android$internal$telephony$DctConstants$Activity[this.mDcTracker.getActivity().ordinal()]) {
            case 1:
                return PhoneInternalInterface.DataActivityState.DATAIN;
            case 2:
                return PhoneInternalInterface.DataActivityState.DATAOUT;
            case 3:
                return PhoneInternalInterface.DataActivityState.DATAINANDOUT;
            case 4:
                return PhoneInternalInterface.DataActivityState.DORMANT;
            default:
                return PhoneInternalInterface.DataActivityState.NONE;
        }
    }

    public void notifyPhoneStateChanged() {
        this.mNotifier.notifyPhoneState(this);
    }

    public void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChangedP();
    }

    public void notifyNewRingingConnection(Connection c) {
        super.notifyNewRingingConnectionP(c);
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getSubId(), 1, LOG_TAG_STATIC);
    }

    public void notifyDisconnect(Connection cn) {
        this.mDisconnectRegistrants.notifyResult(cn);
        this.mNotifier.notifyDisconnectCause(cn.getDisconnectCause(), cn.getPreciseDisconnectCause());
    }

    public void notifyUnknownConnection(Connection cn) {
        super.notifyUnknownConnectionP(cn);
    }

    public boolean isInEmergencyCall() {
        if (isPhoneTypeGsm()) {
            return false;
        }
        return this.mCT.isInEmergencyCall();
    }

    /* access modifiers changed from: protected */
    public void setIsInEmergencyCall() {
        if (!isPhoneTypeGsm()) {
            this.mCT.setIsInEmergencyCall();
        }
    }

    private void sendEmergencyCallbackModeChange() {
        Intent intent = new Intent("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intent.putExtra("phoneinECMState", isInEcm());
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
        ActivityManager.broadcastStickyIntent(intent, -1);
        logd("sendEmergencyCallbackModeChange");
    }

    public void sendEmergencyCallStateChange(boolean callActive) {
        if (this.mBroadcastEmergencyCallStateChanges) {
            Intent intent = new Intent("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
            intent.putExtra("phoneInEmergencyCall", callActive);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
            ActivityManager.broadcastStickyIntent(intent, -1);
            String str = this.LOG_TAG;
            Rlog.d(str, "sendEmergencyCallStateChange: callActive " + callActive);
        }
    }

    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
        this.mBroadcastEmergencyCallStateChanges = broadcast;
    }

    public void notifySuppServiceFailed(PhoneInternalInterface.SuppService code) {
        this.mSuppServiceFailedRegistrants.notifyResult(code);
    }

    public void notifyServiceStateChanged(ServiceState ss) {
        super.notifyServiceStateChangedP(ss);
    }

    public void notifyLocationChanged() {
        this.mNotifier.notifyCellLocation(this);
    }

    public void notifyCallForwardingIndicator() {
        this.mNotifier.notifyCallForwardingChanged(this);
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
        if (this.mSSNResult != null) {
            this.mSsnRegistrants.notifyRegistrants(this.mSSNResult);
        }
        if (this.mSsnRegistrants.size() == 1) {
            this.mCi.setSuppServiceNotifications(true, null);
        }
    }

    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
        this.mSSNResult = null;
    }

    public void registerForSimRecordsLoaded(Handler h, int what, Object obj) {
        this.mSimRecordsLoadedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSimRecordsLoaded(Handler h) {
        this.mSimRecordsLoadedRegistrants.remove(h);
    }

    public void acceptCall(int videoState) throws CallStateException {
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getSubId(), 2, LOG_TAG_STATIC);
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || !imsPhone.getRingingCall().isRinging()) {
            this.mCT.acceptCall();
        } else {
            imsPhone.acceptCall(videoState);
        }
    }

    public void rejectCall() throws CallStateException {
        this.mCT.rejectCall();
    }

    public void switchHoldingAndActive() throws CallStateException {
        this.mCT.switchWaitingOrHoldingAndActive();
    }

    public String getIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (!isPhoneTypeGsm() && r == null) {
            r = this.mUiccController.getIccRecords(this.mPhoneId, 1);
        }
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    public String getFullIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (!isPhoneTypeGsm() && r == null) {
            r = this.mUiccController.getIccRecords(this.mPhoneId, 1);
        }
        if (r != null) {
            return r.getFullIccId();
        }
        return null;
    }

    public boolean canConference() {
        if (this.mImsPhone != null && this.mImsPhone.canConference()) {
            return true;
        }
        if (isPhoneTypeGsm()) {
            return this.mCT.canConference();
        }
        loge("canConference: not possible in CDMA");
        return false;
    }

    public void conference() {
        if (this.mImsPhone == null || !this.mImsPhone.canConference()) {
            if (isPhoneTypeGsm()) {
                this.mCT.conference();
            } else {
                loge("conference: not possible in CDMA");
            }
            return;
        }
        logd("conference() - delegated to IMS phone");
        try {
            this.mImsPhone.conference();
        } catch (CallStateException e) {
            loge(e.toString());
        }
    }

    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("enableEnhancedVoicePrivacy: not expected on GSM");
        } else {
            this.mCi.setPreferredVoicePrivacy(enable, onComplete);
        }
    }

    public void getEnhancedVoicePrivacy(Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("getEnhancedVoicePrivacy: not expected on GSM");
        } else {
            this.mCi.getPreferredVoicePrivacy(onComplete);
        }
    }

    public void clearDisconnected() {
        this.mCT.clearDisconnected();
    }

    public boolean canTransfer() {
        if (isPhoneTypeGsm()) {
            return this.mCT.canTransfer();
        }
        loge("canTransfer: not possible in CDMA");
        return false;
    }

    public void explicitCallTransfer() {
        if (isPhoneTypeGsm()) {
            this.mCT.explicitCallTransfer();
        } else {
            loge("explicitCallTransfer: not possible in CDMA");
        }
    }

    public GsmCdmaCall getForegroundCall() {
        return this.mCT.mForegroundCall;
    }

    public GsmCdmaCall getBackgroundCall() {
        return this.mCT.mBackgroundCall;
    }

    public Call getRingingCall() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null && imsPhone.getRingingCall().isRinging()) {
            return imsPhone.getRingingCall();
        }
        if (this.mCT == null) {
            return null;
        }
        return this.mCT.mRingingCall;
    }

    private boolean handleCallDeflectionIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != Call.State.IDLE) {
            logd("MmiCode 0: rejectCall");
            try {
                this.mCT.rejectCall();
            } catch (CallStateException e) {
                Rlog.d(this.LOG_TAG, "reject failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != Call.State.IDLE) {
            logd("MmiCode 0: hangupWaitingOrBackground");
            this.mCT.hangupWaitingOrBackground();
        }
        return true;
    }

    private boolean handleCallWaitingIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        GsmCdmaCall call = getForegroundCall();
        if (len > 1) {
            try {
                int callIndex = dialString.charAt(1) - '0';
                if (callIndex >= 1 && callIndex <= 19) {
                    logd("MmiCode 1: hangupConnectionByIndex " + callIndex);
                    this.mCT.hangupConnectionByIndex(call, callIndex);
                }
            } catch (CallStateException e) {
                Rlog.d(this.LOG_TAG, "hangup failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.HANGUP);
            }
        } else if (call.getState() != Call.State.IDLE) {
            logd("MmiCode 1: hangup foreground");
            this.mCT.hangup(call);
        } else {
            logd("MmiCode 1: switchWaitingOrHoldingAndActive");
            this.mCT.switchWaitingOrHoldingAndActive();
        }
        return true;
    }

    private boolean handleCallHoldIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        GsmCdmaCall call = getForegroundCall();
        if (len > 1) {
            try {
                int callIndex = dialString.charAt(1) - '0';
                GsmCdmaConnection conn = this.mCT.getConnectionByIndex(call, callIndex);
                if (conn == null || callIndex < 1 || callIndex > 19) {
                    logd("separate: invalid call index " + callIndex);
                    notifySuppServiceFailed(PhoneInternalInterface.SuppService.SEPARATE);
                } else {
                    logd("MmiCode 2: separate call " + callIndex);
                    this.mCT.separate(conn);
                }
            } catch (CallStateException e) {
                Rlog.d(this.LOG_TAG, "separate failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.SEPARATE);
            }
        } else {
            try {
                if (getRingingCall().getState() != Call.State.IDLE) {
                    logd("MmiCode 2: accept ringing call");
                    this.mCT.acceptCall();
                } else {
                    logd("MmiCode 2: switchWaitingOrHoldingAndActive");
                    this.mCT.switchWaitingOrHoldingAndActive();
                }
            } catch (CallStateException e2) {
                Rlog.d(this.LOG_TAG, "switch failed", e2);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.SWITCH);
            }
        }
        return true;
    }

    private boolean handleMultipartyIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        logd("MmiCode 3: merge calls");
        conference();
        return true;
    }

    private boolean handleEctIncallSupplementaryService(String dialString) {
        if (dialString.length() != 1) {
            return false;
        }
        logd("MmiCode 4: explicit call transfer");
        explicitCallTransfer();
        return true;
    }

    private boolean handleCcbsIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        Rlog.i(this.LOG_TAG, "MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(PhoneInternalInterface.SuppService.UNKNOWN);
        return true;
    }

    public boolean handleInCallMmiCommands(String dialString) throws CallStateException {
        if (!isPhoneTypeGsm()) {
            loge("method handleInCallMmiCommands is NOT supported in CDMA!");
            return false;
        }
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            return imsPhone.handleInCallMmiCommands(dialString);
        }
        if (!isInCall() || TextUtils.isEmpty(dialString)) {
            return false;
        }
        boolean result = false;
        switch (dialString.charAt(0)) {
            case '0':
                result = handleCallDeflectionIncallSupplementaryService(dialString);
                break;
            case '1':
                result = handleCallWaitingIncallSupplementaryService(dialString);
                break;
            case '2':
                result = handleCallHoldIncallSupplementaryService(dialString);
                break;
            case '3':
                result = handleMultipartyIncallSupplementaryService(dialString);
                break;
            case '4':
                result = handleEctIncallSupplementaryService(dialString);
                break;
            case '5':
                result = handleCcbsIncallSupplementaryService(dialString);
                break;
        }
        return result;
    }

    public boolean isInCall() {
        return getForegroundCall().getState().isAlive() || getBackgroundCall().getState().isAlive() || getRingingCall().getState().isAlive();
    }

    public Connection dial(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        boolean isEmergency;
        String str = dialString;
        PhoneInternalInterface.DialArgs dialArgs2 = dialArgs;
        if (isPhoneTypeGsm() || dialArgs2.uusInfo == null) {
            logd("eni -> dial getprop default_network: " + SystemProperties.get("ro.telephony.default_network"));
            boolean useImsForUt = false;
            HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getSubId(), 0, LOG_TAG_STATIC);
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                isEmergency = PhoneNumberUtils.isEmergencyNumber(getSubId(), str);
            } else {
                isEmergency = PhoneNumberUtils.isEmergencyNumber(dialString);
            }
            boolean isEmergency2 = isEmergency;
            Phone imsPhone = this.mImsPhone;
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            boolean alwaysTryImsForEmergencyCarrierConfig = configManager.getConfigForSubId(getSubId()).getBoolean("carrier_use_ims_first_for_emergency_bool");
            boolean isCarrierSupportVolte = configManager.getConfigForSubId(getSubId()).getBoolean("carrier_volte_available_bool");
            boolean useImsForCall = isImsUseEnabled() && this.mCT.getState() == PhoneConstants.State.IDLE && imsPhone != null && ((getImsSwitch() && HuaweiTelephonyConfigs.isHisiPlatform()) || ((imsPhone.isVolteEnabled() || imsPhone.isWifiCallingEnabled() || (imsPhone.isVideoEnabled() && VideoProfile.isVideo(dialArgs2.videoState))) && imsPhone.getServiceState().getState() == 0));
            if (imsPhone != null) {
                logd("mCT state = " + this.mCT.getState() + ", ims switch state = " + getImsSwitch() + ", isVideo = " + VideoProfile.isVideo(dialArgs2.videoState) + ", video state = " + dialArgs2.videoState);
            } else {
                logd("dial -> imsPhone is null");
            }
            boolean useImsForEmergency = imsPhone != null && isEmergency2 && alwaysTryImsForEmergencyCarrierConfig && ImsManager.getInstance(this.mContext, this.mPhoneId).isNonTtyOrTtyOnVolteEnabled() && imsPhone.isImsAvailable() && this.mCT.mForegroundCall.getState() != Call.State.ACTIVE;
            String dialPart = PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.stripSeparators(dialString));
            boolean isUt = ((dialPart.startsWith(CharacterSets.MIMENAME_ANY_CHARSET) || dialPart.startsWith("#")) && dialPart.endsWith("#")) && !ImsPhoneMmiCode.isVirtualNum(dialPart);
            if (imsPhone != null && imsPhone.isUtEnabled()) {
                useImsForUt = true;
            }
            if (VSimUtilsInner.isVSimOn() && !isDualImsAvailable()) {
                logd("vsim is on and the device do not support dual-IMS");
                useImsForCall = false;
                useImsForEmergency = false;
            }
            boolean useImsForEmergency2 = useImsForEmergency;
            boolean useImsForCall2 = useImsForCall;
            StringBuilder sb = new StringBuilder();
            sb.append("useImsForCall=");
            sb.append(useImsForCall2);
            sb.append(", useImsForEmergency=");
            sb.append(useImsForEmergency2);
            sb.append(", useImsForUt=");
            sb.append(useImsForUt);
            sb.append(", isUt=");
            sb.append(isUt);
            sb.append(", imsPhone=");
            sb.append(imsPhone);
            sb.append(", imsPhone.isVolteEnabled()=");
            sb.append(imsPhone != null ? Boolean.valueOf(imsPhone.isVolteEnabled()) : "N/A");
            sb.append(", imsPhone.isVowifiEnabled()=");
            sb.append(imsPhone != null ? Boolean.valueOf(imsPhone.isWifiCallingEnabled()) : "N/A");
            sb.append(", imsPhone.isVideoEnabled()=");
            sb.append(imsPhone != null ? Boolean.valueOf(imsPhone.isVideoEnabled()) : "N/A");
            sb.append(", imsPhone.getServiceState().getState()=");
            sb.append(imsPhone != null ? Integer.valueOf(imsPhone.getServiceState().getState()) : "N/A");
            sb.append(", mCT.mForegroundCall.getState=");
            sb.append(this.mCT.mForegroundCall.getState());
            logd(sb.toString());
            CarrierConfigManager carrierConfigManager = configManager;
            Phone.checkWfcWifiOnlyModeBeforeDial(this.mImsPhone, this.mPhoneId, this.mContext);
            if ((isPhoneTypeGsm() || isCarrierSupportVolte) && ((useImsForCall2 && !isUt) || ((isUt && useImsForUt) || useImsForEmergency2))) {
                try {
                    logd("Trying IMS PS call");
                    return imsPhone.dial(str, dialArgs2);
                } catch (CallStateException e) {
                    logd("IMS PS call exception " + e + "useImsForCall =" + useImsForCall2 + ", imsPhone =" + imsPhone);
                    if (Phone.CS_FALLBACK.equals(e.getMessage()) || isEmergency2) {
                        logi("IMS call failed with Exception: " + e.getMessage() + ". Falling back to CS.");
                    } else {
                        CallStateException ce = new CallStateException(e.getMessage());
                        ce.setStackTrace(e.getStackTrace());
                        throw ce;
                    }
                }
            }
            boolean shouldRunUtIgnoreCsService = HwTelephonyFactory.getHwPhoneManager().shouldRunUtIgnoreCSService(this, isUt);
            logd("shouldRunUtIgnoreCsService = " + shouldRunUtIgnoreCsService);
            if (this.mSST == null || this.mSST.mSS.getState() != 1 || this.mSST.mSS.getDataRegState() == 0 || isEmergency2 || shouldRunUtIgnoreCsService) {
                if (this.mSST == null || this.mSST.mSS.getState() != 3 || VideoProfile.isVideo(dialArgs2.videoState) || isEmergency2) {
                } else if (isUt) {
                    Phone phone = imsPhone;
                } else {
                    Phone phone2 = imsPhone;
                    throw new CallStateException(2, "cannot dial voice call in airplane mode");
                }
                if (this.mSST == null || this.mSST.mSS.getState() != 1 || ((this.mSST.mSS.getDataRegState() == 0 && ServiceState.isLte(this.mSST.mSS.getRilDataRadioTechnology())) || VideoProfile.isVideo(dialArgs2.videoState) || isEmergency2 || shouldRunUtIgnoreCsService)) {
                    logd("Trying (non-IMS) CS call");
                    if (isPhoneTypeGsm()) {
                        return dialInternal(str, new PhoneInternalInterface.DialArgs.Builder().setIntentExtras(dialArgs2.intentExtras).build());
                    }
                    return dialInternal(dialString, dialArgs);
                }
                throw new CallStateException(1, "cannot dial voice call in out of service");
            }
            throw new CallStateException("cannot dial in current state");
        }
        throw new CallStateException("Sending UUS information NOT supported in CDMA!");
    }

    public boolean isNotificationOfWfcCallRequired(String dialString) {
        PersistableBundle config = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId());
        boolean shouldConfirmCall = true;
        if (!(config != null && config.getBoolean("notify_international_call_on_wfc_bool"))) {
            return false;
        }
        Phone imsPhone = this.mImsPhone;
        boolean isEmergency = PhoneNumberUtils.isEmergencyNumber(getSubId(), dialString);
        if (!isImsUseEnabled() || imsPhone == null || imsPhone.isVolteEnabled() || !imsPhone.isWifiCallingEnabled() || isEmergency || !PhoneNumberUtils.isInternationalNumber(dialString, getCountryIso())) {
            shouldConfirmCall = false;
        }
        return shouldConfirmCall;
    }

    /* access modifiers changed from: protected */
    public Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        return dialInternal(dialString, dialArgs, null);
    }

    /* access modifiers changed from: protected */
    public Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs, ResultReceiver wrappedCallback) throws CallStateException {
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        if (isPhoneTypeGsm()) {
            if (handleInCallMmiCommands(newDialString)) {
                return null;
            }
            GsmMmiCode mmi = GsmMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, (UiccCardApplication) this.mUiccApplication.get(), wrappedCallback);
            logd("dialInternal: dialing w/ mmi '" + mmi + "'...");
            if (mmi != null) {
                HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getSubId(), 0, "AP_FLOW_SUC");
            }
            if (mmi == null) {
                return this.mCT.dial(newDialString, dialArgs.uusInfo, dialArgs.intentExtras);
            }
            if (mmi.isTemporaryModeCLIR()) {
                return this.mCT.dial(mmi.mDialingNumber, mmi.getCLIRMode(), dialArgs.uusInfo, dialArgs.intentExtras);
            }
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                mmi.setImsPhone(this.mImsPhone);
            }
            try {
                mmi.processCode();
            } catch (CallStateException e) {
            }
            return null;
        } else if (this.mHwGsmCdmaPhoneEx == null || !this.mHwGsmCdmaPhoneEx.dialInternalForCdmaLte(newDialString)) {
            return this.mCT.dial(newDialString);
        } else {
            return null;
        }
    }

    public boolean handlePinMmi(String dialString) {
        MmiCode mmi;
        if (isPhoneTypeGsm()) {
            mmi = GsmMmiCode.newFromDialString(dialString, this, (UiccCardApplication) this.mUiccApplication.get());
        } else {
            mmi = CdmaMmiCode.newFromDialString(dialString, this, (UiccCardApplication) this.mUiccApplication.get());
        }
        if (mmi == null || !mmi.isPinPukCommand()) {
            loge("Mmi is null or unrecognized!");
            return false;
        }
        this.mPendingMMIs.add(mmi);
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        try {
            mmi.processCode();
        } catch (CallStateException e) {
        }
        return true;
    }

    private void sendUssdResponse(String ussdRequest, CharSequence message, int returnCode, ResultReceiver wrappedCallback) {
        UssdResponse response = new UssdResponse(ussdRequest, message);
        Bundle returnData = new Bundle();
        returnData.putParcelable("USSD_RESPONSE", response);
        wrappedCallback.send(returnCode, returnData);
    }

    public boolean handleUssdRequest(String ussdRequest, ResultReceiver wrappedCallback) {
        if (!isPhoneTypeGsm() || this.mPendingMMIs.size() > 0) {
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            return true;
        }
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null && (imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
            try {
                logd("handleUssdRequest: attempting over IMS");
                return imsPhone.handleUssdRequest(ussdRequest, wrappedCallback);
            } catch (CallStateException cse) {
                if (!Phone.CS_FALLBACK.equals(cse.getMessage())) {
                    return false;
                }
                logd("handleUssdRequest: fallback to CS required");
            }
        }
        try {
            dialInternal(ussdRequest, new PhoneInternalInterface.DialArgs.Builder().build(), wrappedCallback);
            return true;
        } catch (Exception e) {
            logd("handleUssdRequest: exception" + e);
            return false;
        }
    }

    public void sendUssdResponse(String ussdMessge) {
        if (isPhoneTypeGsm()) {
            GsmMmiCode mmi = GsmMmiCode.newFromUssdUserInput(ussdMessge, this, (UiccCardApplication) this.mUiccApplication.get());
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            mmi.sendUssd(ussdMessge);
            return;
        }
        loge("sendUssdResponse: not possible in CDMA");
    }

    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.mState == PhoneConstants.State.OFFHOOK) {
            this.mCi.sendDtmf(c, null);
        }
    }

    public void startDtmf(char c) {
        if (PhoneNumberUtils.is12Key(c) || (c >= 'A' && c <= 'D')) {
            this.mCi.startDtmf(c, null);
            return;
        }
        loge("startDtmf called with invalid character '" + c + "'");
    }

    public void stopDtmf() {
        this.mCi.stopDtmf(null);
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("[GsmCdmaPhone] sendBurstDtmf() is a CDMA method");
            return;
        }
        boolean check = true;
        int itr = 0;
        while (true) {
            if (itr >= dtmfString.length()) {
                break;
            } else if (!PhoneNumberUtils.is12Key(dtmfString.charAt(itr))) {
                String str = this.LOG_TAG;
                Rlog.e(str, "sendDtmf called with invalid character '" + dtmfString.charAt(itr) + "'");
                check = false;
                break;
            } else {
                itr++;
            }
        }
        if (this.mCT.mState == PhoneConstants.State.OFFHOOK && check) {
            this.mCi.sendBurstDtmf(dtmfString, on, off, onComplete);
        }
    }

    public void setRadioPower(boolean power) {
        this.mSST.setRadioPower(power);
    }

    public void setRadioPower(boolean power, Message msg) {
        this.mSST.setRadioPower(power, msg);
    }

    private void storeVoiceMailNumber(String number, boolean isSaveIccRecord) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        if (isPhoneTypeGsm()) {
            String mIccId = getIccSerialNumber();
            editor.putString(mIccId + getPhoneId(), number);
            editor.putString(VM_NUMBER + getPhoneId(), number);
            editor.apply();
            setVmSimImsi(getSubscriberId());
            if (isSaveIccRecord) {
                ((IccRecords) this.mIccRecords.get()).setVoiceMailNumber(number);
                return;
            }
            return;
        }
        editor.putString(VM_NUMBER_CDMA + getPhoneId(), number);
        editor.apply();
    }

    public String getVoiceMailNumber() {
        String number;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            number = r != null ? r.getVoiceMailNumber() : "";
            if (TextUtils.isEmpty(number)) {
                number = getVMNumberWhenIMSIChange();
            }
            if (TextUtils.isEmpty(number)) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                number = sp.getString(VM_NUMBER + getPhoneId(), null);
            }
        } else {
            SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(getContext());
            number = sp2.getString(VM_NUMBER_CDMA + getPhoneId(), null);
        }
        if (TextUtils.isEmpty(number)) {
            PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfig();
            if (b != null) {
                String defaultVmNumber = b.getString("default_vm_number_string");
                String defaultVmNumberRoaming = b.getString("default_vm_number_roaming_string");
                if (TextUtils.isEmpty(defaultVmNumberRoaming) || !this.mSST.mSS.getRoaming()) {
                    number = defaultVmNumber;
                } else {
                    number = defaultVmNumberRoaming;
                }
            }
        }
        if (!isPhoneTypeGsm() && TextUtils.isEmpty(number)) {
            PersistableBundle b2 = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfig();
            if (b2 == null || !b2.getBoolean("config_telephony_use_own_number_for_voicemail_bool")) {
                number = "*86";
            } else {
                number = getLine1Number();
            }
        }
        if (!isPhoneTypeGsm()) {
            return HwTelephonyFactory.getHwPhoneManager().getCDMAVoiceMailNumberHwCust(this.mContext, getLine1Number(), getPhoneId());
        }
        return number;
    }

    public String getVoiceMailAlphaTag() {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            return getDefaultVoiceMailAlphaTagText(this.mContext, r != null ? r.getVoiceMailAlphaTag() : "");
        } else if ("".length() == 0) {
            return this.mContext.getText(17039364).toString();
        } else {
            return "";
        }
    }

    public String getDeviceId() {
        if (!VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            String str = this.LOG_TAG;
            Rlog.d(str, "getDeviceId, the phone is pending, mPhoneId is: " + this.mPhoneId);
            return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.DEVICE_ID_PREF);
        } else if (isPhoneTypeGsm()) {
            return this.mImei;
        } else {
            if (((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId()).getBoolean("force_imei_bool")) {
                return this.mImei;
            }
            String id = getMeid();
            if (id == null || id.matches("^0*$")) {
                loge("getDeviceId(): MEID is not initialized use ESN");
                id = getEsn();
            }
            return id;
        }
    }

    public String getDeviceSvn() {
        if (!VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            String str = this.LOG_TAG;
            Rlog.d(str, "getDeviceSvn, the phone is pending, mPhoneId is: " + this.mPhoneId);
            return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.DEVICE_SVN_PREF);
        } else if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
            return this.mImeiSv;
        } else {
            loge("getDeviceSvn(): return 0");
            return ProxyController.MODEM_0;
        }
    }

    public IsimRecords getIsimRecords() {
        return this.mIsimUiccRecords;
    }

    public String getImei() {
        if (VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            return this.mImei;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getImei, the phone is pending, mPhoneId is: " + this.mPhoneId);
        return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.IMEI_PREF);
    }

    public String getEsn() {
        if (!VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            String str = this.LOG_TAG;
            Rlog.d(str, "getEsn, the phone is pending, mPhoneId is: " + this.mPhoneId);
            return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.ESN_PREF);
        } else if (isPhoneTypeGsm()) {
            return this.mEsn;
        } else {
            if (5 == TelephonyManager.getDefault().getSimState(getSubId())) {
                this.mEsn = this.mCi.getHwUimid();
            }
            return this.mEsn;
        }
    }

    public String getMeid() {
        if (VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            return this.mMeid;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getMeid, the phone is pending, mPhoneId is: " + this.mPhoneId);
        return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.MEID_PREF);
    }

    public String getNai() {
        IccRecords r = this.mUiccController.getIccRecords(this.mPhoneId, 2);
        if (Log.isLoggable(this.LOG_TAG, 2)) {
            String str = this.LOG_TAG;
            Rlog.v(str, "IccRecords is " + r);
        }
        if (r != null) {
            return r.getNAI();
        }
        return null;
    }

    public String getSubscriberId() {
        String subscriberId = null;
        if (!isPhoneTypeCdma() && !HuaweiTelephonyConfigs.isChinaTelecom() && !IS_FULL_NETWORK_SUPPORTED) {
            IccRecords iccRecords = this.mUiccController.getIccRecords(this.mPhoneId, 1);
            if (iccRecords != null) {
                subscriberId = iccRecords.getIMSI();
            }
            return subscriberId;
        } else if (this.mCdmaSubscriptionSource == 1) {
            Rlog.d(this.LOG_TAG, "getSubscriberId from mSST");
            return this.mSST.getImsi();
        } else {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            return r != null ? r.getIMSI() : null;
        }
    }

    public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int keyType) {
        return CarrierInfoManager.getCarrierInfoForImsiEncryption(keyType, this.mContext);
    }

    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo) {
        CarrierInfoManager.setCarrierInfoForImsiEncryption(imsiEncryptionInfo, this.mContext, this.mPhoneId);
    }

    public int getCarrierId() {
        return this.mCarrerIdentifier.getCarrierId();
    }

    public String getCarrierName() {
        return this.mCarrerIdentifier.getCarrierName();
    }

    public int getCarrierIdListVersion() {
        return this.mCarrerIdentifier.getCarrierListVersion();
    }

    public void resetCarrierKeysForImsiEncryption() {
        this.mCIM.resetCarrierKeysForImsiEncryption(this.mContext, this.mPhoneId);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: com.android.internal.telephony.uicc.IccRecords} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void setCarrierTestOverride(String mccmnc, String imsi, String iccid, String gid1, String gid2, String pnn, String spn) {
        IccRecords r = null;
        if (isPhoneTypeGsm()) {
            r = this.mIccRecords.get();
        } else if (isPhoneTypeCdmaLte()) {
            r = this.mSimRecords;
        } else {
            loge("setCarrierTestOverride fails in CDMA only");
        }
        if (r != null) {
            r.setCarrierTestOverride(mccmnc, imsi, iccid, gid1, gid2, pnn, spn);
        }
    }

    public String getGroupIdLevel1() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getGid1();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            loge("GID1 is not available in CDMA");
            return null;
        } else {
            return this.mSimRecords != null ? this.mSimRecords.getGid1() : "";
        }
    }

    public String getGroupIdLevel2() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getGid2();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            loge("GID2 is not available in CDMA");
            return null;
        } else {
            return this.mSimRecords != null ? this.mSimRecords.getGid2() : "";
        }
    }

    public String getLine1Number() {
        logForTest("getLine1Number", "get phone number..****, phoneType:" + this.mPrecisePhoneType);
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getMsisdnNumber();
            }
            return str;
        }
        IccRecords r2 = (IccRecords) this.mIccRecords.get();
        if (r2 != null) {
            str = r2.getMdn();
        }
        return str;
    }

    public String getPlmn() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getPnnHomeName();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            loge("Plmn is not available in CDMA");
            return null;
        } else {
            if (this.mSimRecords != null) {
                str = this.mSimRecords.getPnnHomeName();
            }
            return str;
        }
    }

    public String getCdmaPrlVersion() {
        return this.mSST.getPrlVersion();
    }

    public String getCdmaMin() {
        return this.mSST.getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return this.mSST.isMinInfoReady();
    }

    public String getMsisdn() {
        logForTest("getMsisdn", "get phone number..****");
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getMsisdnNumber();
            }
            return str;
        } else if (isPhoneTypeCdmaLte()) {
            if (this.mSimRecords != null) {
                str = this.mSimRecords.getMsisdnNumber();
            }
            return str;
        } else {
            loge("getMsisdn: not expected on CDMA");
            return null;
        }
    }

    public String getLine1AlphaTag() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getMsisdnAlphaTag();
            }
            return str;
        }
        loge("getLine1AlphaTag: not possible in CDMA");
        return null;
    }

    public boolean setLine1Number(String alphaTag, String number, Message onComplete) {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (isPhoneTypeGsm()) {
            if (r == null) {
                return false;
            }
            r.setMsisdnNumber(alphaTag, number, onComplete);
            return true;
        } else if (r == null) {
            return false;
        } else {
            r.setMdnNumber(alphaTag, number, onComplete);
            return true;
        }
    }

    public void setVoiceMailNumber(String alphaTag, String voiceMailNumber, Message onComplete) {
        if ("".equals(voiceMailNumber)) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(VM_NUMBER, voiceMailNumber).commit();
        }
        this.mVmNumber = voiceMailNumber;
        Message resp = obtainMessage(20, 0, 0, onComplete);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.setVoiceMailNumber(alphaTag, this.mVmNumber, resp);
        }
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    public String getSystemProperty(String property, String defValue) {
        if (getUnitTestMode()) {
            return null;
        }
        return TelephonyManager.getTelephonyProperty(this.mPhoneId, property, defValue);
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
            case 0:
            case 1:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        getCallForwardingOption(commandInterfaceCFReason, 0, onComplete);
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
        Message resp;
        if (isPhoneTypeGsm() || (this.mImsPhone != null && isPhoneTypeCdmaLte())) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                imsPhone.getCallForwardingOption(commandInterfaceCFReason, onComplete);
                return;
            }
        }
        if (!isPhoneTypeGsm()) {
            loge("getCallForwardingOption: not possible in CDMA");
        } else if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            logd("requesting call forwarding query.");
            if (commandInterfaceCFReason == 0) {
                resp = obtainMessage(13, onComplete);
            } else {
                resp = onComplete;
            }
            this.mCi.queryCallForwardStatus(commandInterfaceCFReason, serviceClass, null, resp);
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        Message resp;
        if (isPhoneTypeGsm() || (this.mImsPhone != null && isPhoneTypeCdmaLte())) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                imsPhone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, onComplete);
                return;
            }
        }
        if (!isPhoneTypeGsm()) {
            loge("setCallForwardingOption: not possible in CDMA");
        } else if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            if (commandInterfaceCFReason == 0) {
                resp = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cfu(dialingNumber, onComplete));
            } else {
                resp = onComplete;
            }
            this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, 1, processPlusSymbol(dialingNumber, getSubscriberId()), timerSeconds, resp);
        }
    }

    public void getCallBarring(String facility, String password, Message onComplete, int serviceClass) {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || (imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled())) {
                this.mCi.queryFacilityLock(facility, password, serviceClass, onComplete);
            } else {
                imsPhone.getCallBarring(facility, password, onComplete, serviceClass);
            }
        } else {
            loge("getCallBarringOption: not possible in CDMA");
        }
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || (imsPhone.getServiceState().getState() != 0 && !imsPhone.isUtEnabled())) {
                this.mCi.setFacilityLock(facility, lockState, password, serviceClass, onComplete);
            } else {
                imsPhone.setCallBarring(facility, lockState, password, onComplete, serviceClass);
            }
        } else {
            loge("setCallBarringOption: not possible in CDMA");
        }
    }

    public void changeCallBarringPassword(String facility, String oldPwd, String newPwd, Message onComplete) {
        if (isPhoneTypeGsm()) {
            this.mCi.changeBarringPassword(facility, oldPwd, newPwd, onComplete);
        } else {
            loge("changeCallBarringPassword: not possible in CDMA");
        }
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                this.mCi.getCLIR(onComplete);
            } else {
                imsPhone.getOutgoingCallerIdDisplay(onComplete);
            }
        } else {
            loge("getOutgoingCallerIdDisplay: not possible in CDMA");
        }
    }

    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                this.mCi.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
            } else {
                imsPhone.setOutgoingCallerIdDisplay(commandInterfaceCLIRMode, onComplete);
            }
        } else {
            loge("setOutgoingCallerIdDisplay: not possible in CDMA");
        }
    }

    public void getCallWaiting(Message onComplete) {
        if (isPhoneTypeGsm() || (this.mImsPhone != null && isPhoneTypeCdmaLte())) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                imsPhone.getCallWaiting(onComplete);
                return;
            }
        }
        if (isPhoneTypeGsm()) {
            this.mCi.queryCallWaiting(0, onComplete);
        } else {
            this.mCi.queryCallWaiting(1, onComplete);
        }
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm() || (this.mImsPhone != null && isPhoneTypeCdmaLte())) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                imsPhone.setCallWaiting(enable, onComplete);
                return;
            }
        }
        if (isPhoneTypeGsm()) {
            int serviceClass = 1;
            if (CALL_WAITING_CLASS_NONE) {
                serviceClass = 0;
            }
            logd("setCallWaiting enable = " + enable + " serviceClass = " + serviceClass);
            this.mCi.setCallWaiting(enable, serviceClass, onComplete);
        } else {
            loge("method setCallWaiting is NOT supported in CDMA!");
        }
    }

    public void getAvailableNetworks(Message response) {
        if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
            this.mCi.getAvailableNetworks(getCustAvailableNetworksMessage(response));
            return;
        }
        loge("getAvailableNetworks: not possible in CDMA");
    }

    public void startNetworkScan(NetworkScanRequest nsr, Message response) {
        this.mCi.startNetworkScan(nsr, response);
    }

    public void stopNetworkScan(Message response) {
        this.mCi.stopNetworkScan(response);
    }

    public void getNeighboringCids(Message response, WorkSource workSource) {
        if (isPhoneTypeGsm()) {
            this.mCi.getNeighboringCids(response, workSource);
        } else if (response != null) {
            AsyncResult.forMessage(response).exception = new CommandException(CommandException.Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void setTTYMode(int ttyMode, Message onComplete) {
        super.setTTYMode(ttyMode, onComplete);
        if (this.mImsPhone != null) {
            this.mImsPhone.setTTYMode(ttyMode, onComplete);
        }
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        if (this.mImsPhone != null) {
            this.mImsPhone.setUiTTYMode(uiTtyMode, onComplete);
        }
    }

    public void setMute(boolean muted) {
        if (this.mCT != null) {
            this.mCT.setMute(muted);
        }
    }

    public boolean getMute() {
        if (this.mCT == null) {
            return false;
        }
        return this.mCT.getMute();
    }

    public void updateServiceLocation() {
        this.mSST.enableSingleLocationUpdate();
    }

    public void enableLocationUpdates() {
        this.mSST.enableLocationUpdates();
    }

    public void disableLocationUpdates() {
        this.mSST.disableLocationUpdates();
    }

    public boolean getDataRoamingEnabled() {
        return this.mDcTracker.getDataRoamingEnabled();
    }

    public void setDataRoamingEnabled(boolean enable) {
        this.mDcTracker.setDataRoamingEnabledByUser(enable);
    }

    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
        this.mCi.registerForCdmaOtaProvision(h, what, obj);
    }

    public void unregisterForCdmaOtaStatusChange(Handler h) {
        this.mCi.unregisterForCdmaOtaProvision(h);
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        this.mSST.registerForSubscriptionInfoReady(h, what, obj);
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mSST.unregisterForSubscriptionInfoReady(h);
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        this.mEcmExitRespRegistrant = new Registrant(h, what, obj);
    }

    public void unsetOnEcbModeExitResponse(Handler h) {
        this.mEcmExitRespRegistrant.clear();
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCT.registerForCallWaiting(h, what, obj);
    }

    public void unregisterForCallWaiting(Handler h) {
        this.mCT.unregisterForCallWaiting(h);
    }

    public boolean isUserDataEnabled() {
        return this.mDcTracker.isUserDataEnabled();
    }

    public boolean isDataEnabled() {
        return this.mDcTracker.isDataEnabled();
    }

    public void setUserDataEnabled(boolean enable) {
        logForTest("setDataEnabled", "setDataEnabled to.." + enable);
        if (HwSystemManager.allowOp(null, 4194304, isDataEnabled())) {
            if (HwTelephonyFactory.getHwDataConnectionManager().needSetUserDataEnabled(enable)) {
                this.mDcTracker.setUserDataEnabled(enable);
            } else {
                logd("setDataEnabled ignored by HwDataConnectionManager");
            }
        }
    }

    public void onMMIDone(MmiCode mmi) {
        if (this.mPendingMMIs.remove(mmi) || (isPhoneTypeGsm() && (mmi.isUssdRequest() || ((GsmMmiCode) mmi).isSsInfo()))) {
            ResultReceiver receiverCallback = mmi.getUssdCallbackReceiver();
            if (receiverCallback != null) {
                String str = this.LOG_TAG;
                Rlog.i(str, "onMMIDone: invoking callback: " + mmi);
                sendUssdResponse(mmi.getDialString(), mmi.getMessage(), mmi.getState() == MmiCode.State.COMPLETE ? 100 : -1, receiverCallback);
                return;
            }
            String str2 = this.LOG_TAG;
            Rlog.i(str2, "onMMIDone: notifying registrants: " + mmi);
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            return;
        }
        String str3 = this.LOG_TAG;
        Rlog.i(str3, "onMMIDone: invalid response or already handled; ignoring: " + mmi);
    }

    public boolean supports3gppCallForwardingWhileRoaming() {
        PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfig();
        if (b != null) {
            return b.getBoolean("support_3gpp_call_forwarding_while_roaming_bool", true);
        }
        return true;
    }

    private void onNetworkInitiatedUssd(MmiCode mmi) {
        String str = this.LOG_TAG;
        Rlog.v(str, "onNetworkInitiatedUssd: mmi=" + mmi);
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v5, resolved type: com.android.internal.telephony.gsm.GsmMmiCode} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void onIncomingUSSD(int ussdMode, String ussdMessage) {
        if (!isPhoneTypeGsm()) {
            loge("onIncomingUSSD: not expected on GSM");
        }
        boolean isUssdRelease = false;
        boolean isUssdRequest = ussdMode == 1;
        boolean isUssdError = (ussdMode == 0 || ussdMode == 1 || ussdMode == 12) ? false : true;
        if (isUssdOkForRelease()) {
            isUssdError = isUssdError && ussdMode != 2;
        }
        if (ussdMode == 2) {
            isUssdRelease = true;
        }
        if (HwTelephonyFactory.getHwPhoneManager().needUnEscapeHtmlforUssdMsg(this)) {
            logd("onIncomingUSSD: Need UnEscape Html characters for this UssdMessage.");
            ussdMessage = HwTelephonyFactory.getHwPhoneManager().unEscapeHtml4(ussdMessage);
        }
        GsmMmiCode found = null;
        int i = 0;
        int s = this.mPendingMMIs.size();
        while (true) {
            if (i >= s) {
                break;
            } else if (((GsmMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                found = this.mPendingMMIs.get(i);
                break;
            } else {
                i++;
            }
        }
        if (found != null) {
            HwPhoneManager hwPhoneManager = HwTelephonyFactory.getHwPhoneManager();
            int custUssdState = -1;
            if (hwPhoneManager != null) {
                custUssdState = hwPhoneManager.removeUssdCust(this);
            }
            if ((GsmMmiCode.USSD_REMOVE_ERROR_MSG && custUssdState != 0) || custUssdState == 1) {
                found.setIncomingUSSD(true);
            }
            if (isUssdRelease) {
                found.onUssdFinished(ussdMessage, isUssdRequest);
            } else if (isUssdError) {
                found.onUssdFinishedError();
            } else {
                found.onUssdFinished(ussdMessage, isUssdRequest);
            }
        } else if (!isUssdError && ussdMessage != null) {
            onNetworkInitiatedUssd(GsmMmiCode.newNetworkInitiatedUssd(ussdMessage, isUssdRequest, this, (UiccCardApplication) this.mUiccApplication.get()));
        }
    }

    private void syncClirSetting() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        int clirSetting = sp.getInt(Phone.CLIR_KEY + getPhoneId(), -1);
        String str = this.LOG_TAG;
        Rlog.i(str, "syncClirSetting: clir_key" + getPhoneId() + "=" + clirSetting);
        if (clirSetting >= 0) {
            this.mCi.setCLIR(clirSetting, null);
        }
    }

    private void handleRadioAvailable() {
        this.mCi.getBasebandVersion(obtainMessage(6));
        this.mCi.getDeviceIdentity(obtainMessage(21));
        this.mCi.getRadioCapability(obtainMessage(35));
        startLceAfterRadioIsAvailable();
    }

    private void handleRadioOn() {
        this.mCi.getVoiceRadioTechnology(obtainMessage(40));
        if (!isPhoneTypeGsm()) {
            this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
        }
        this.mCi.getRadioCapability(obtainMessage(35));
        setPreferredNetworkTypeIfSimLoaded();
        if (this.mImsPhone != null && 3 == this.mImsPhone.getServiceState().getState()) {
            Log.i(this.LOG_TAG, "setting radio state out of service from power off ");
            this.mImsPhone.getServiceState().setState(1);
        }
    }

    private void handleRadioOffOrNotAvailable() {
        if (isPhoneTypeGsm()) {
            for (int i = this.mPendingMMIs.size() - 1; i >= 0; i--) {
                if (((GsmMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                    ((GsmMmiCode) this.mPendingMMIs.get(i)).onUssdFinishedError();
                }
            }
        }
        this.mRadioOffOrNotAvailableRegistrants.notifyRegistrants();
    }

    private void handleEventModemReset(Message msg) {
        if (!isInEcm()) {
            return;
        }
        if (!isPhoneTypeGsm() || HwCustUtil.isVZW) {
            handleExitEmergencyCallbackMode(msg);
        } else if (this.mImsPhone != null) {
            this.mImsPhone.handleExitEmergencyCallbackMode();
        }
    }

    public void handleMessage(Message msg) {
        int newVoiceTech;
        boolean broadcastEmergencyCallStateChanges;
        if (!beforeHandleMessage(msg)) {
            boolean z = false;
            switch (msg.what) {
                case 1:
                    handleRadioAvailable();
                    resetReduceSARPowerGrade();
                    updateReduceSARState();
                    break;
                case 2:
                    logd("Event EVENT_SSN Received");
                    if (isPhoneTypeGsm()) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        this.mSSNResult = ar;
                        Object obj = ar.result;
                        this.mSsnRegistrants.notifyRegistrants(ar);
                        break;
                    }
                    break;
                case 3:
                    updateCurrentCarrierInProvider();
                    String imsi = getVmSimImsi();
                    String imsiFromSIM = getSubscriberId();
                    if ((!isPhoneTypeGsm() || imsi != null) && imsiFromSIM != null && !imsiFromSIM.equals(imsi)) {
                        storeVoiceMailNumber(null, false);
                        setVmSimImsi(null);
                    }
                    this.mSimRecordsLoadedRegistrants.notifyRegistrants();
                    updateVoiceMail();
                    processEccNumber(this.mSST);
                    break;
                case 5:
                    logd("Event EVENT_RADIO_ON Received");
                    handleRadioOn();
                    break;
                case 6:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception == null) {
                        logd("Baseband version: " + ar2.result);
                        TelephonyManager.from(this.mContext).setBasebandVersionForPhone(getPhoneId(), (String) ar2.result);
                        break;
                    }
                    break;
                case 7:
                    String[] ussdResult = (String[]) ((AsyncResult) msg.obj).result;
                    if (ussdResult.length > 1) {
                        try {
                            onIncomingUSSD(Integer.parseInt(ussdResult[0]), ussdResult[1]);
                            break;
                        } catch (NumberFormatException e) {
                            Rlog.w(this.LOG_TAG, "error parsing USSD");
                            break;
                        }
                    }
                    break;
                case 8:
                    logd("Event EVENT_RADIO_OFF_OR_NOT_AVAILABLE Received");
                    handleRadioOffOrNotAvailable();
                    autoExitEmergencyCallbackMode();
                    break;
                case 9:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3.exception == null) {
                        this.mImei = (String) ar3.result;
                        logForImei(AbstractGsmCdmaPhone.GSM_PHONE, this.mImei);
                        getContext().sendBroadcast(new Intent("com.android.huawei.DM.IMEI_READY"));
                        break;
                    }
                    break;
                case 10:
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    if (ar4.exception == null) {
                        this.mImeiSv = (String) ar4.result;
                        break;
                    }
                    break;
                case 12:
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    IccRecords r = (IccRecords) this.mIccRecords.get();
                    Cfu cfu = (Cfu) ar5.userObj;
                    if (ar5.exception == null && r != null) {
                        if (msg.arg1 == 1) {
                            z = true;
                        }
                        setVoiceCallForwardingFlag(1, z, cfu.mSetCfNumber);
                    }
                    if (cfu.mOnComplete != null) {
                        AsyncResult.forMessage(cfu.mOnComplete, ar5.result, ar5.exception);
                        cfu.mOnComplete.sendToTarget();
                        break;
                    }
                    break;
                case 13:
                    AsyncResult ar6 = (AsyncResult) msg.obj;
                    if (ar6.exception == null) {
                        handleCfuQueryResult((CallForwardInfo[]) ar6.result);
                    }
                    Message onComplete = (Message) ar6.userObj;
                    if (onComplete != null) {
                        AsyncResult.forMessage(onComplete, ar6.result, ar6.exception);
                        onComplete.sendToTarget();
                        break;
                    }
                    break;
                case 18:
                    AsyncResult ar7 = (AsyncResult) msg.obj;
                    if (ar7.exception == null) {
                        saveClirSetting(msg.arg1);
                    }
                    Message onComplete2 = (Message) ar7.userObj;
                    if (onComplete2 != null) {
                        AsyncResult.forMessage(onComplete2, ar7.result, ar7.exception);
                        onComplete2.sendToTarget();
                        break;
                    }
                    break;
                case 19:
                    logd("Event EVENT_REGISTERED_TO_NETWORK Received");
                    if (isPhoneTypeGsm()) {
                        syncClirSetting();
                        break;
                    }
                    break;
                case 20:
                    AsyncResult ar8 = (AsyncResult) msg.obj;
                    if ((isPhoneTypeGsm() && IccVmNotSupportedException.class.isInstance(ar8.exception)) || (!isPhoneTypeGsm() && IccException.class.isInstance(ar8.exception))) {
                        storeVoiceMailNumber(this.mVmNumber, true);
                        ar8.exception = null;
                    }
                    Message onComplete3 = (Message) ar8.userObj;
                    if (onComplete3 != null) {
                        AsyncResult.forMessage(onComplete3, ar8.result, ar8.exception);
                        onComplete3.sendToTarget();
                        break;
                    }
                    break;
                case 21:
                    AsyncResult ar9 = (AsyncResult) msg.obj;
                    if (ar9.exception == null) {
                        String[] respId = (String[]) ar9.result;
                        this.mImei = respId[0];
                        logForImei(AbstractGsmCdmaPhone.CDMA_PHONE, this.mImei);
                        this.mImeiSv = respId[1];
                        this.mEsn = respId[2];
                        this.mMeid = respId[3];
                        if (respId.length > 4) {
                            this.mUimid = respId[4];
                            SystemProperties.set("persist.radio.hwuimid", this.mUimid);
                            break;
                        }
                    } else {
                        retryGetDeviceId(msg.arg1, 2);
                        break;
                    }
                    break;
                case 22:
                    logd("Event EVENT_RUIM_RECORDS_LOADED Received");
                    updateCurrentCarrierInProvider();
                    processEccNumber(this.mSST);
                    break;
                case 23:
                    logd("Event EVENT_NV_READY Received");
                    prepareEri();
                    SubscriptionInfoUpdater subInfoRecordUpdater = PhoneFactory.getSubInfoRecordUpdater();
                    if (subInfoRecordUpdater != null) {
                        subInfoRecordUpdater.updateSubIdForNV(this.mPhoneId);
                        break;
                    }
                    break;
                case 25:
                    handleEnterEmergencyCallbackMode(msg);
                    break;
                case 26:
                    handleExitEmergencyCallbackMode(msg);
                    break;
                case 27:
                    logd("EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED");
                    this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
                    break;
                case 28:
                    AsyncResult ar10 = (AsyncResult) msg.obj;
                    if (!this.mSST.mSS.getIsManualSelection()) {
                        logd("SET_NETWORK_SELECTION_AUTOMATIC: already automatic, ignore");
                        break;
                    } else {
                        setNetworkSelectionModeAutomatic((Message) ar10.result);
                        logd("SET_NETWORK_SELECTION_AUTOMATIC: set to automatic");
                        break;
                    }
                case 29:
                    processIccRecordEvents(((Integer) ((AsyncResult) msg.obj).result).intValue());
                    break;
                case 35:
                    AsyncResult ar11 = (AsyncResult) msg.obj;
                    RadioCapability rc = (RadioCapability) ar11.result;
                    if (ar11.exception != null) {
                        Rlog.d(this.LOG_TAG, "get phone radio capability fail, no need to change mRadioCapability");
                    } else {
                        radioCapabilityUpdated(rc);
                    }
                    Rlog.d(this.LOG_TAG, "EVENT_GET_RADIO_CAPABILITY: phone rc: " + rc);
                    break;
                case 36:
                    AsyncResult ar12 = (AsyncResult) msg.obj;
                    logd("Event EVENT_SS received");
                    if (isPhoneTypeGsm()) {
                        new GsmMmiCode(this, (UiccCardApplication) this.mUiccApplication.get()).processSsData(ar12);
                        break;
                    }
                    break;
                case 39:
                case 40:
                    String what = msg.what == 39 ? "EVENT_VOICE_RADIO_TECH_CHANGED" : "EVENT_REQUEST_VOICE_RADIO_TECH_DONE";
                    AsyncResult ar13 = (AsyncResult) msg.obj;
                    if (ar13.exception == null) {
                        if (ar13.result != null && ((int[]) ar13.result).length != 0) {
                            logd(what + ": newVoiceTech=" + newVoiceTech);
                            phoneObjectUpdater(newVoiceTech);
                            break;
                        } else {
                            loge(what + ": has no tech!");
                            break;
                        }
                    } else {
                        loge(what + ": exception=" + ar13.exception);
                        break;
                    }
                    break;
                case 41:
                    AsyncResult ar14 = (AsyncResult) msg.obj;
                    if (ar14.exception == null && ar14.result != null) {
                        this.mRilVersion = ((Integer) ar14.result).intValue();
                        break;
                    } else {
                        logd("Unexpected exception on EVENT_RIL_CONNECTED");
                        this.mRilVersion = -1;
                        break;
                    }
                    break;
                case 42:
                    phoneObjectUpdater(msg.arg1);
                    break;
                case 43:
                    if (!this.mContext.getResources().getBoolean(17957050)) {
                        this.mCi.getVoiceRadioTechnology(obtainMessage(40));
                    }
                    HwFrameworkFactory.updateImsServiceConfig(this.mContext, this.mPhoneId, true);
                    PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
                    if (b != null) {
                        logd("broadcastEmergencyCallStateChanges = " + broadcastEmergencyCallStateChanges);
                        setBroadcastEmergencyCallStateChanges(broadcastEmergencyCallStateChanges);
                    } else {
                        loge("didn't get broadcastEmergencyCallStateChanges from carrier config");
                    }
                    if (b != null) {
                        int config_cdma_roaming_mode = b.getInt("cdma_roaming_mode_int");
                        int current_cdma_roaming_mode = Settings.Global.getInt(getContext().getContentResolver(), "roaming_settings", -1);
                        switch (config_cdma_roaming_mode) {
                            case -1:
                                if (current_cdma_roaming_mode != config_cdma_roaming_mode) {
                                    logd("cdma_roaming_mode is going to changed to " + current_cdma_roaming_mode);
                                    setCdmaRoamingPreference(current_cdma_roaming_mode, obtainMessage(44));
                                    break;
                                }
                                break;
                            case 0:
                            case 1:
                            case 2:
                                logd("cdma_roaming_mode is going to changed to " + config_cdma_roaming_mode);
                                setCdmaRoamingPreference(config_cdma_roaming_mode, obtainMessage(44));
                                break;
                        }
                        loge("Invalid cdma_roaming_mode settings: " + config_cdma_roaming_mode);
                    } else {
                        loge("didn't get the cdma_roaming_mode changes from the carrier config.");
                    }
                    prepareEri();
                    this.mSST.pollState();
                    break;
                case 44:
                    logd("cdma_roaming_mode change is done");
                    break;
                case 45:
                    logd("Event EVENT_MODEM_RESET Received isInEcm = " + isInEcm() + " isPhoneTypeGsm = " + isPhoneTypeGsm() + " mImsPhone = " + this.mImsPhone);
                    handleEventModemReset(msg);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
            afterHandleMessage(msg);
        }
    }

    public UiccCardApplication getUiccCardApplication() {
        if (isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
    }

    /* JADX WARNING: type inference failed for: r5v3, types: [com.android.internal.telephony.uicc.IccRecords] */
    /* JADX WARNING: type inference failed for: r3v15, types: [com.android.internal.telephony.uicc.IccRecords] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
                UiccCardApplication newUiccApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 3);
                IsimUiccRecords newIsimUiccRecords = null;
                if (newUiccApplication != null) {
                    newIsimUiccRecords = newUiccApplication.getIccRecords();
                    logd("New ISIM application found");
                }
                this.mIsimUiccRecords = newIsimUiccRecords;
            }
            if (this.mSimRecords != null) {
                this.mSimRecords.unregisterForRecordsLoaded(this);
            }
            if (isPhoneTypeCdmaLte() || isPhoneTypeCdma()) {
                UiccCardApplication newUiccApplication2 = this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
                SIMRecords newSimRecords = null;
                if (newUiccApplication2 != null) {
                    newSimRecords = newUiccApplication2.getIccRecords();
                }
                this.mSimRecords = newSimRecords;
                if (this.mSimRecords != null) {
                    this.mSimRecords.registerForRecordsLoaded(this, 3, null);
                }
            } else {
                this.mSimRecords = null;
            }
            UiccCardApplication newUiccApplication3 = getUiccCardApplication();
            if (!isPhoneTypeGsm() && newUiccApplication3 == null) {
                logd("can't find 3GPP2 application; trying APP_FAM_3GPP");
                newUiccApplication3 = this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
            }
            UiccCardApplication app = (UiccCardApplication) this.mUiccApplication.get();
            if (newUiccApplication3 != null) {
                this.newAppType = newUiccApplication3.getType();
            }
            logd("newAppType = " + this.newAppType + ", oldAppType = " + this.oldAppType);
            if (!(app == newUiccApplication3 && (this.newAppType == IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN || this.oldAppType == IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN || this.newAppType == this.oldAppType))) {
                if (app != null) {
                    logd("Removing stale icc objects.");
                    if (this.mIccRecords.get() != null) {
                        unregisterForIccRecordEvents();
                        this.mIccPhoneBookIntManager.updateIccRecords(null);
                    }
                    this.mIccRecords.set(null);
                    this.mUiccApplication.set(null);
                }
                if (newUiccApplication3 != null) {
                    logd("New Uicc application found");
                    this.oldAppType = newUiccApplication3.getType();
                    logd("New Uicc application found. type = " + newUiccApplication3.getType());
                    this.mUiccApplication.set(newUiccApplication3);
                    this.mIccRecords.set(newUiccApplication3.getIccRecords());
                    registerForIccRecordEvents();
                    this.mIccPhoneBookIntManager.updateIccRecords((IccRecords) this.mIccRecords.get());
                    updateDataConnectionTracker();
                }
            }
        }
    }

    private void processIccRecordEvents(int eventCode) {
        if (eventCode == 1) {
            logi("processIccRecordEvents: EVENT_CFI");
            notifyCallForwardingIndicator();
        }
    }

    public boolean updateCurrentCarrierInProvider() {
        long currentDds = (long) PhoneFactory.getTopPrioritySubscriptionId();
        String operatorNumeric = getOperatorNumeric();
        logd("updateCurrentCarrierInProvider: mSubId = " + getSubId() + " currentDds = " + currentDds + " operatorNumeric = " + operatorNumeric);
        if (!TextUtils.isEmpty(operatorNumeric) && ((long) getSubId()) == currentDds) {
            try {
                String currentOperatorNumeric = HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated() ? HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric() : operatorNumeric;
                Uri uri = Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "current");
                ContentValues map = new ContentValues();
                map.put("numeric", currentOperatorNumeric);
                this.mContext.getContentResolver().insert(uri, map);
                return true;
            } catch (SQLException e) {
                Rlog.e(this.LOG_TAG, "Can't store current operator", e);
            }
        }
        return false;
    }

    private boolean updateCurrentCarrierInProvider(String operatorNumeric) {
        if (isPhoneTypeCdma() || (isPhoneTypeCdmaLte() && this.mUiccController.getUiccCardApplication(this.mPhoneId, 1) == null)) {
            logd("CDMAPhone: updateCurrentCarrierInProvider called");
            if (!TextUtils.isEmpty(operatorNumeric)) {
                try {
                    Uri uri = Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "current");
                    ContentValues map = new ContentValues();
                    map.put("numeric", operatorNumeric);
                    logd("updateCurrentCarrierInProvider from system: numeric=" + operatorNumeric);
                    getContext().getContentResolver().insert(uri, map);
                    logd("update mccmnc=" + operatorNumeric);
                    return true;
                } catch (SQLException e) {
                    Rlog.e(this.LOG_TAG, "Can't store current operator", e);
                }
            }
            return false;
        }
        logd("updateCurrentCarrierInProvider not updated X retVal=true");
        return true;
    }

    private void handleCfuQueryResult(CallForwardInfo[] infos) {
        if (((IccRecords) this.mIccRecords.get()) != null) {
            boolean z = false;
            if (infos == null || infos.length == 0) {
                setVoiceCallForwardingFlag(1, false, null);
                return;
            }
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                if ((infos[i].serviceClass & 1) != 0) {
                    if (infos[i].status == 1) {
                        z = true;
                    }
                    setVoiceCallForwardingFlag(1, z, infos[i].number);
                    return;
                }
            }
        }
    }

    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return this.mIccPhoneBookIntManager;
    }

    public void registerForEriFileLoaded(Handler h, int what, Object obj) {
        this.mEriFileLoadedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEriFileLoaded(Handler h) {
        this.mEriFileLoadedRegistrants.remove(h);
    }

    public void prepareEri() {
        if (this.mEriManager == null) {
            Rlog.e(this.LOG_TAG, "PrepareEri: Trying to access stale objects");
            return;
        }
        this.mEriManager.loadEriFile();
        if (this.mEriManager.isEriFileLoaded()) {
            logd("ERI read, notify registrants");
            this.mEriFileLoadedRegistrants.notifyRegistrants();
        }
    }

    public boolean isEriFileLoaded() {
        return this.mEriManager.isEriFileLoaded();
    }

    public void activateCellBroadcastSms(int activate, Message response) {
        loge("[GsmCdmaPhone] activateCellBroadcastSms() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public void getCellBroadcastSmsConfig(Message response) {
        loge("[GsmCdmaPhone] getCellBroadcastSmsConfig() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        loge("[GsmCdmaPhone] setCellBroadcastSmsConfig() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public boolean needsOtaServiceProvisioning() {
        boolean z = false;
        if (isPhoneTypeGsm()) {
            return false;
        }
        if (this.mSST.getOtasp() != 3) {
            z = true;
        }
        return z;
    }

    public boolean isCspPlmnEnabled() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.isCspPlmnEnabled();
        }
        return false;
    }

    public boolean shouldForceAutoNetworkSelect() {
        int nwMode = Phone.PREFERRED_NT_MODE;
        int subId = getSubId();
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return false;
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int nwMode2 = Settings.Global.getInt(contentResolver, "preferred_network_mode" + subId, nwMode);
        logd("shouldForceAutoNetworkSelect in mode = " + nwMode2);
        if (!isManualSelProhibitedInGlobalMode() || !(nwMode2 == 10 || nwMode2 == 7)) {
            logd("Should not force auto network select mode = " + nwMode2);
            return false;
        }
        logd("Should force auto network select mode = " + nwMode2);
        return true;
    }

    private boolean isManualSelProhibitedInGlobalMode() {
        boolean isProhibited = false;
        String configString = getContext().getResources().getString(17040960);
        if (!TextUtils.isEmpty(configString)) {
            String[] configArray = configString.split(";");
            if (configArray != null && ((configArray.length == 1 && configArray[0].equalsIgnoreCase("true")) || (configArray.length == 2 && !TextUtils.isEmpty(configArray[1]) && configArray[0].equalsIgnoreCase("true") && isMatchGid(configArray[1])))) {
                isProhibited = true;
            }
        }
        logd("isManualNetSelAllowedInGlobal in current carrier is " + isProhibited);
        return isProhibited;
    }

    private void registerForIccRecordEvents() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            if (isPhoneTypeGsm() || (r instanceof SIMRecords)) {
                r.registerForNetworkSelectionModeAutomatic(this, 28, null);
                r.registerForRecordsEvents(this, 29, null);
                r.registerForRecordsLoaded(this, 3, null);
                r.registerForImsiReady(this, AbstractPhoneBase.EVENT_GET_IMSI_DONE, null);
                registerForCsgRecordsLoadedEvent();
            } else {
                r.registerForRecordsLoaded(this, 22, null);
                if (isPhoneTypeCdmaLte()) {
                    r.registerForRecordsLoaded(this, 3, null);
                }
            }
        }
    }

    private void unregisterForIccRecordEvents() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.unregisterForNetworkSelectionModeAutomatic(this);
            r.unregisterForRecordsEvents(this);
            r.unregisterForRecordsLoaded(this);
            unregisterForCsgRecordsLoadedEvent();
        }
    }

    public void exitEmergencyCallbackMode() {
        String str = this.LOG_TAG;
        Rlog.d(str, "exitEmergencyCallbackMode: mImsPhone=" + this.mImsPhone + " isPhoneTypeGsm=" + isPhoneTypeGsm());
        if (!isPhoneTypeGsm()) {
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            this.mCi.exitEmergencyCallbackMode(obtainMessage(26));
        } else if (this.mImsPhone != null) {
            this.mImsPhone.exitEmergencyCallbackMode();
        }
    }

    private void handleEnterEmergencyCallbackMode(Message msg) {
        String str = this.LOG_TAG;
        Rlog.d(str, "handleEnterEmergencyCallbackMode, isInEcm()=" + isInEcm());
        if (!isInEcm()) {
            setIsInEcm(true);
            sendEmergencyCallbackModeChange();
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
            this.mWakeLock.acquire();
        }
    }

    private void handleExitEmergencyCallbackMode(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        String str = this.LOG_TAG;
        Rlog.d(str, "handleExitEmergencyCallbackMode,ar.exception , isInEcm=" + ar.exception + isInEcm());
        removeCallbacks(this.mExitEcmRunnable);
        if (this.mEcmExitRespRegistrant != null) {
            this.mEcmExitRespRegistrant.notifyRegistrant(ar);
        }
        if (ar.exception == null) {
            if (isInEcm()) {
                setIsInEcm(false);
            }
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            sendEmergencyCallbackModeChange();
            this.mDcTracker.setInternalDataEnabled(true);
            notifyEmergencyCallRegistrants(false);
        }
    }

    public void notifyEmergencyCallRegistrants(boolean started) {
        this.mEmergencyCallToggledRegistrants.notifyResult(Integer.valueOf(started));
    }

    public void handleTimerInEmergencyCallbackMode(int action) {
        switch (action) {
            case 0:
                postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
                this.mEcmTimerResetRegistrants.notifyResult(Boolean.FALSE);
                return;
            case 1:
                removeCallbacks(this.mExitEcmRunnable);
                this.mEcmTimerResetRegistrants.notifyResult(Boolean.TRUE);
                return;
            default:
                String str = this.LOG_TAG;
                Rlog.e(str, "handleTimerInEmergencyCallbackMode, unsupported action " + action);
                return;
        }
    }

    private static boolean isIs683OtaSpDialStr(String dialStr) {
        if (dialStr.length() != 4) {
            switch (extractSelCodeFromOtaSpNum(dialStr)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    return true;
                default:
                    return false;
            }
        } else if (dialStr.equals(IS683A_FEATURE_CODE)) {
            return true;
        } else {
            return false;
        }
    }

    private static int extractSelCodeFromOtaSpNum(String dialStr) {
        int dialStrLen = dialStr.length();
        int sysSelCodeInt = -1;
        if (dialStr.regionMatches(0, IS683A_FEATURE_CODE, 0, 4) && dialStrLen >= 6) {
            sysSelCodeInt = Integer.parseInt(dialStr.substring(4, 6));
        }
        Rlog.d(LOG_TAG_STATIC, "extractSelCodeFromOtaSpNum " + sysSelCodeInt);
        return sysSelCodeInt;
    }

    private static boolean checkOtaSpNumBasedOnSysSelCode(int sysSelCodeInt, String[] sch) {
        try {
            int selRc = Integer.parseInt(sch[1]);
            for (int i = 0; i < selRc; i++) {
                if (!TextUtils.isEmpty(sch[i + 2]) && !TextUtils.isEmpty(sch[i + 3])) {
                    int selMin = Integer.parseInt(sch[i + 2]);
                    int selMax = Integer.parseInt(sch[i + 3]);
                    if (sysSelCodeInt >= selMin && sysSelCodeInt <= selMax) {
                        return true;
                    }
                }
            }
            return false;
        } catch (NumberFormatException ex) {
            Rlog.e(LOG_TAG_STATIC, "checkOtaSpNumBasedOnSysSelCode, error", ex);
            return false;
        }
    }

    private boolean isCarrierOtaSpNum(String dialStr) {
        boolean isOtaSpNum = false;
        int sysSelCodeInt = extractSelCodeFromOtaSpNum(dialStr);
        if (sysSelCodeInt == -1) {
            return false;
        }
        if (!TextUtils.isEmpty(this.mCarrierOtaSpNumSchema)) {
            Matcher m = pOtaSpNumSchema.matcher(this.mCarrierOtaSpNumSchema);
            String str = this.LOG_TAG;
            Rlog.d(str, "isCarrierOtaSpNum,schema" + this.mCarrierOtaSpNumSchema);
            if (m.find()) {
                String[] sch = pOtaSpNumSchema.split(this.mCarrierOtaSpNumSchema);
                if (TextUtils.isEmpty(sch[0]) || !sch[0].equals("SELC")) {
                    if (TextUtils.isEmpty(sch[0]) || !sch[0].equals("FC")) {
                        String str2 = this.LOG_TAG;
                        Rlog.d(str2, "isCarrierOtaSpNum,ota schema not supported" + sch[0]);
                    } else {
                        if (dialStr.regionMatches(0, sch[2], 0, Integer.parseInt(sch[1]))) {
                            isOtaSpNum = true;
                        } else {
                            Rlog.d(this.LOG_TAG, "isCarrierOtaSpNum,not otasp number");
                        }
                    }
                } else if (sysSelCodeInt != -1) {
                    isOtaSpNum = checkOtaSpNumBasedOnSysSelCode(sysSelCodeInt, sch);
                } else {
                    Rlog.d(this.LOG_TAG, "isCarrierOtaSpNum,sysSelCodeInt is invalid");
                }
            } else {
                String str3 = this.LOG_TAG;
                Rlog.d(str3, "isCarrierOtaSpNum,ota schema pattern not right" + this.mCarrierOtaSpNumSchema);
            }
        } else {
            Rlog.d(this.LOG_TAG, "isCarrierOtaSpNum,ota schema pattern empty");
        }
        return isOtaSpNum;
    }

    public boolean isOtaSpNumber(String dialStr) {
        if (isPhoneTypeGsm()) {
            return super.isOtaSpNumber(dialStr);
        }
        boolean isOtaSpNum = false;
        String dialableStr = PhoneNumberUtils.extractNetworkPortionAlt(dialStr);
        if (dialableStr != null) {
            isOtaSpNum = isIs683OtaSpDialStr(dialableStr);
            if (!isOtaSpNum) {
                isOtaSpNum = isCarrierOtaSpNum(dialableStr);
            }
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "isOtaSpNumber " + isOtaSpNum);
        return isOtaSpNum;
    }

    public int getCdmaEriIconIndex() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriIconIndex();
        }
        return getServiceState().getCdmaEriIconIndex();
    }

    public int getCdmaEriIconMode() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriIconMode();
        }
        return getServiceState().getCdmaEriIconMode();
    }

    public String getCdmaEriText() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriText();
        }
        return this.mEriManager.getCdmaEriText(getServiceState().getCdmaRoamingIndicator(), getServiceState().getCdmaDefaultRoamingIndicator());
    }

    private void phoneObjectUpdater(int newVoiceRadioTech) {
        logd("phoneObjectUpdater: newVoiceRadioTech=" + newVoiceRadioTech);
        if (ServiceState.isLte(newVoiceRadioTech) || newVoiceRadioTech == 0) {
            PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
            if (b != null) {
                int volteReplacementRat = b.getInt("volte_replacement_rat_int");
                logd("phoneObjectUpdater: volteReplacementRat=" + volteReplacementRat);
                if (volteReplacementRat != 0) {
                    newVoiceRadioTech = volteReplacementRat;
                }
            } else {
                loge("phoneObjectUpdater: didn't get volteReplacementRat from carrier config");
            }
        }
        if (this.mRilVersion == 6 && getLteOnCdmaMode() == 1) {
            if (getPhoneType() == 2) {
                logd("phoneObjectUpdater: LTE ON CDMA property is set. Use CDMA Phone newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
                return;
            }
            logd("phoneObjectUpdater: LTE ON CDMA property is set. Switch to CDMALTEPhone newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
            newVoiceRadioTech = 6;
        } else if (isShuttingDown()) {
            logd("Device is shutting down. No need to switch phone now.");
            return;
        } else {
            boolean matchCdma = ServiceState.isCdma(newVoiceRadioTech);
            boolean matchGsm = ServiceState.isGsm(newVoiceRadioTech);
            if ((matchCdma && getPhoneType() == 2) || (matchGsm && getPhoneType() == 1)) {
                logd("phoneObjectUpdater: No change ignore, newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
                return;
            } else if (!matchCdma && !matchGsm) {
                loge("phoneObjectUpdater: newVoiceRadioTech=" + newVoiceRadioTech + " doesn't match either CDMA or GSM - error! No phone change");
                return;
            }
        }
        if (newVoiceRadioTech == 0) {
            logd("phoneObjectUpdater: Unknown rat ignore,  newVoiceRadioTech=Unknown. mActivePhone=" + getPhoneName());
        } else if (CallManager.getInstance().hasActiveFgCall(getPhoneId())) {
            logd("has ActiveFgCall, should not updatePhoneObject");
        } else {
            boolean oldPowerState = false;
            if (this.mResetModemOnRadioTechnologyChange && this.mCi.getRadioState().isOn()) {
                oldPowerState = true;
                logd("phoneObjectUpdater: Setting Radio Power to Off");
                this.mCi.setRadioPower(false, null);
            }
            switchVoiceRadioTech(newVoiceRadioTech);
            if (this.mResetModemOnRadioTechnologyChange && oldPowerState) {
                logd("phoneObjectUpdater: Resetting Radio");
                this.mCi.setRadioPower(oldPowerState, null);
            }
            UiccProfile uiccProfile = getUiccProfile();
            if (uiccProfile != null) {
                uiccProfile.setVoiceRadioTech(newVoiceRadioTech);
            }
            Intent intent = new Intent("android.intent.action.RADIO_TECHNOLOGY");
            intent.putExtra("phoneName", getPhoneName());
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhoneId);
            ActivityManager.broadcastStickyIntent(intent, -1);
        }
    }

    private void switchVoiceRadioTech(int newVoiceRadioTech) {
        String outgoingPhoneName = getPhoneName();
        StringBuilder sb = new StringBuilder();
        sb.append("Switching Voice Phone : ");
        sb.append(outgoingPhoneName);
        sb.append(" >>> ");
        sb.append(ServiceState.isGsm(newVoiceRadioTech) ? AbstractGsmCdmaPhone.GSM_PHONE : AbstractGsmCdmaPhone.CDMA_PHONE);
        logd(sb.toString());
        if (ServiceState.isCdma(newVoiceRadioTech)) {
            UiccCardApplication cdmaApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
            if (cdmaApplication == null || cdmaApplication.getType() != IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
                switchPhoneType(6);
                TelephonyManager.setTelephonyProperty(this.mPhoneId, "persist.radio.last_phone_type", AbstractGsmCdmaPhone.CDMA_PHONE);
            } else {
                switchPhoneType(2);
            }
        } else if (ServiceState.isGsm(newVoiceRadioTech)) {
            switchPhoneType(1);
            TelephonyManager.setTelephonyProperty(this.mPhoneId, "persist.radio.last_phone_type", AbstractGsmCdmaPhone.GSM_PHONE);
        } else {
            loge("deleteAndCreatePhone: newVoiceRadioTech=" + newVoiceRadioTech + " is not CDMA or GSM (error) - aborting!");
        }
    }

    public void setSignalStrengthReportingCriteria(int[] thresholds, int ran) {
        this.mCi.setSignalStrengthReportingCriteria(REPORTING_HYSTERESIS_MILLIS, 2, thresholds, ran, null);
    }

    public void setLinkCapacityReportingCriteria(int[] dlThresholds, int[] ulThresholds, int ran) {
        this.mCi.setLinkCapacityReportingCriteria(REPORTING_HYSTERESIS_MILLIS, 50, 50, dlThresholds, ulThresholds, ran, null);
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return this.mIccSmsInterfaceManager;
    }

    public void updatePhoneObject(int voiceRadioTech) {
        logd("updatePhoneObject: radioTechnology=" + voiceRadioTech);
        sendMessage(obtainMessage(42, voiceRadioTech, 0, null));
    }

    public void setImsRegistrationState(boolean registered) {
        this.mSST.setImsRegistrationState(registered);
    }

    public boolean getIccRecordsLoaded() {
        UiccProfile uiccProfile = getUiccProfile();
        return uiccProfile != null && uiccProfile.getIccRecordsLoaded();
    }

    public IccCard getIccCard() {
        IccCard card = getUiccProfile();
        if (card != null) {
            return card;
        }
        UiccSlot slot = this.mUiccController.getUiccSlotForPhone(this.mPhoneId);
        if (slot == null || slot.isStateUnknown()) {
            return new IccCard(IccCardConstants.State.UNKNOWN);
        }
        return new IccCard(IccCardConstants.State.ABSENT);
    }

    private UiccProfile getUiccProfile() {
        return UiccController.getInstance().getUiccProfileForPhone(this.mPhoneId);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("GsmCdmaPhone extends:");
        super.dump(fd, pw, args);
        pw.println(" mPrecisePhoneType=" + this.mPrecisePhoneType);
        pw.println(" mCT=" + this.mCT);
        pw.println(" mSST=" + this.mSST);
        pw.println(" mPendingMMIs=" + this.mPendingMMIs);
        pw.println(" mIccPhoneBookIntManager=" + this.mIccPhoneBookIntManager);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mCdmaSubscriptionSource=" + this.mCdmaSubscriptionSource);
        pw.println(" mEriManager=" + this.mEriManager);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" isInEcm()=" + isInEcm());
        pw.println(" mCarrierOtaSpNumSchema=" + this.mCarrierOtaSpNumSchema);
        if (!isPhoneTypeGsm()) {
            pw.println(" getCdmaEriIconIndex()=" + getCdmaEriIconIndex());
            pw.println(" getCdmaEriIconMode()=" + getCdmaEriIconMode());
            pw.println(" getCdmaEriText()=" + getCdmaEriText());
            pw.println(" isMinInfoReady()=" + isMinInfoReady());
        }
        pw.println(" isCspPlmnEnabled()=" + isCspPlmnEnabled());
        pw.flush();
    }

    public boolean setOperatorBrandOverride(String brand) {
        if (this.mUiccController == null) {
            return false;
        }
        UiccCard card = this.mUiccController.getUiccCard(getPhoneId());
        if (card == null) {
            return false;
        }
        boolean status = card.setOperatorBrandOverride(brand);
        if (status) {
            IccRecords iccRecords = (IccRecords) this.mIccRecords.get();
            if (iccRecords != null) {
                TelephonyManager.from(this.mContext).setSimOperatorNameForPhone(getPhoneId(), iccRecords.getServiceProviderName());
            }
            if (this.mSST != null) {
                this.mSST.pollState();
            }
        }
        return status;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: com.android.internal.telephony.uicc.IccRecords} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v20, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: com.android.internal.telephony.uicc.IccRecords} */
    /* JADX WARNING: Multi-variable type inference failed */
    public String getOperatorNumeric() {
        String operatorNumeric = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                operatorNumeric = r.getOperatorNumeric();
            }
            if (isCTSimCard(getPhoneId())) {
                Rlog.d(this.LOG_TAG, "sub2 is dobule mode card.");
                operatorNumeric = SystemProperties.get("gsm.national_roaming.apn", "46003");
            }
        } else if (isCTSimCard(getPhoneId())) {
            String operatorNumeric2 = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "46003");
            String str = this.LOG_TAG;
            Rlog.d(str, "select china telecom hplmn " + operatorNumeric2);
            return operatorNumeric2;
        } else {
            IccRecords curIccRecords = null;
            if (this.mCdmaSubscriptionSource == 1) {
                operatorNumeric = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
            } else if (this.mCdmaSubscriptionSource == 0) {
                UiccCardApplication uiccCardApplication = (UiccCardApplication) this.mUiccApplication.get();
                if (uiccCardApplication == null || uiccCardApplication.getType() != IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
                    curIccRecords = this.mSimRecords;
                } else {
                    logd("Legacy RUIM app present");
                    curIccRecords = this.mIccRecords.get();
                }
                if (curIccRecords == null || curIccRecords != this.mSimRecords) {
                    curIccRecords = this.mIccRecords.get();
                    if (curIccRecords != null && (curIccRecords instanceof RuimRecords)) {
                        operatorNumeric = ((RuimRecords) curIccRecords).getRUIMOperatorNumeric();
                    }
                } else {
                    operatorNumeric = curIccRecords.getOperatorNumeric();
                }
            }
            if (operatorNumeric == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("getOperatorNumeric: Cannot retrieve operatorNumeric: mCdmaSubscriptionSource = ");
                sb.append(this.mCdmaSubscriptionSource);
                sb.append(" mIccRecords = ");
                sb.append(curIccRecords != null ? Boolean.valueOf(curIccRecords.getRecordsLoaded()) : null);
                loge(sb.toString());
            }
            logd("getOperatorNumeric: mCdmaSubscriptionSource = " + this.mCdmaSubscriptionSource + " operatorNumeric = " + operatorNumeric);
        }
        return operatorNumeric;
    }

    public String getCountryIso() {
        SubscriptionInfo subInfo = SubscriptionManager.from(getContext()).getActiveSubscriptionInfo(getSubId());
        if (subInfo == null) {
            return null;
        }
        return subInfo.getCountryIso().toUpperCase();
    }

    public void notifyEcbmTimerReset(Boolean flag) {
        this.mEcmTimerResetRegistrants.notifyResult(flag);
    }

    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
        this.mEcmTimerResetRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForEcmTimerReset(Handler h) {
        this.mEcmTimerResetRegistrants.remove(h);
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                r.setVoiceMessageWaiting(line, countWaiting);
            } else {
                logd("SIM Records not found, MWI not updated");
            }
        } else {
            setVoiceMessageCount(countWaiting);
        }
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    private void logi(String s) {
        String str = this.LOG_TAG;
        Rlog.i(str, "[" + this.mPhoneId + "] " + s);
    }

    private void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }

    public boolean isUtEnabled() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null) {
            return imsPhone.isUtEnabled();
        }
        logd("isUtEnabled: called for GsmCdma");
        return false;
    }

    public String getDtmfToneDelayKey() {
        if (isPhoneTypeGsm()) {
            return "gsm_dtmf_tone_delay_int";
        }
        return "cdma_dtmf_tone_delay_int";
    }

    @VisibleForTesting
    public PowerManager.WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public void cleanDeviceId() {
        logd("cleanDeviceId");
        if (isPhoneTypeGsm()) {
            super.cleanDeviceId();
        } else {
            this.mMeid = null;
        }
        this.mImei = null;
    }

    public String getCdmaMlplVersion() {
        return getCdmaMlplVersion(this.mSST.mMlplVersion);
    }

    public String getCdmaMsplVersion() {
        return getCdmaMsplVersion(this.mSST.mMsplVersion);
    }

    public String getMeidHw() {
        if (VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            return this.mMeid;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getMeid, the phone is pending, mPhoneId is: " + this.mPhoneId);
        return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.MEID_PREF);
    }

    public void setMeidHw(String value) {
        this.mMeid = value;
    }

    public void onMMIDone(GsmMmiCode mmi, Exception e) {
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest() || mmi.isSsInfo()) {
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, e));
        }
    }

    public void registerForCdmaWaitingNumberChanged(Handler h, int what, Object obj) {
        Rlog.i(this.LOG_TAG, "registerForCdmaWaitingNumberChanged");
        this.mCT.registerForCdmaWaitingNumberChanged(h, what, obj);
    }

    public void unregisterForCdmaWaitingNumberChanged(Handler h) {
        Rlog.i(this.LOG_TAG, "unregisterForCdmaWaitingNumberChanged");
        this.mCT.unregisterForCdmaWaitingNumberChanged(h);
    }

    public int getLteOnCdmaMode() {
        int currentConfig = super.getLteOnCdmaMode();
        int i = currentConfig;
        UiccCardApplication cdmaApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
        if (cdmaApplication != null && cdmaApplication.getType() == IccCardApplicationStatus.AppType.APPTYPE_RUIM && currentConfig == 1) {
            return 0;
        }
        return currentConfig;
    }

    public GsmCdmaPhone getGsmCdmaPhone() {
        return this;
    }

    public boolean isValidCommandInterfaceCFActionHw(int commandInterfaceCFAction) {
        return isValidCommandInterfaceCFAction(commandInterfaceCFAction);
    }

    public boolean isValidCommandInterfaceCFReasonHw(int commandInterfaceCFReason) {
        return isValidCommandInterfaceCFReason(commandInterfaceCFReason);
    }

    public boolean isCfEnableHw(int action) {
        return isCfEnable(action);
    }

    public Phone getImsPhoneHw() {
        return this.mImsPhone;
    }

    public UiccCardApplication getUiccApplicationHw() {
        return (UiccCardApplication) this.mUiccApplication.get();
    }

    public void addPendingMMIsHw(MmiCode mmiCode) {
        this.mPendingMMIs.add(mmiCode);
    }

    public void notifyRegistrantsHw(AsyncResult result) {
        this.mMmiRegistrants.notifyRegistrants(result);
    }

    public boolean isPhoneInEcmState() {
        return this.mIsPhoneInEcmState;
    }

    public void setPhoneInEcmState(boolean phoneInEcmState) {
        this.mIsPhoneInEcmState = phoneInEcmState;
    }

    public void removeCallbacksHw() {
        removeCallbacks(this.mExitEcmRunnable);
    }

    public void handleEcmExitRespRegistrant() {
        if (this.mEcmExitRespRegistrant != null) {
            this.mEcmExitRespRegistrant.notifyRegistrant(new AsyncResult(null, 0, null));
        }
    }

    public void handleWakeLock() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public void sendEmergencyCallbackModeChangeHw() {
        sendEmergencyCallbackModeChange();
    }

    public void phoneObjectUpdaterHw(int newVoiceRadioTech) {
        phoneObjectUpdater(newVoiceRadioTech);
    }

    private void autoExitEmergencyCallbackMode() {
        if (this.mHwGsmCdmaPhoneEx != null) {
            this.mHwGsmCdmaPhoneEx.autoExitEmergencyCallbackMode();
        }
    }
}
