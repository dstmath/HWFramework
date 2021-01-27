package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
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
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.HwCustTelephonyManager;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UssdResponse;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.cdma.CdmaMmiCode;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.DataEnabledSettings;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.telephony.emergency.EmergencyNumberTracker;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.android.internal.telephony.nano.TelephonyProto;
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
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.ArrayUtils;
import com.google.android.mms.pdu.CharacterSets;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.ImsExceptionExt;
import com.huawei.internal.telephony.MmiCodeExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import huawei.cust.HwCustUtils;
import huawei.cust.HwGetCfgFileConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GsmCdmaPhone extends Phone implements IHwGsmCdmaPhoneInner {
    private static final boolean CALL_WAITING_CLASS_NONE = SystemProperties.getBoolean("ro.config.cw_no_class", false);
    public static final int CANCEL_ECM_TIMER = 1;
    private static final String CDMA_PHONE = "CDMA";
    public static final String CF_ENABLED = "cf_enabled_key";
    private static final boolean DBG = true;
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    private static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", false);
    private static final String GSM_PHONE = "GSM";
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
    private static final String PREFIX_WPS = "*272";
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
    private static final int[] VOICE_PS_CALL_RADIO_TECHNOLOGY = {14, 19, 18};
    private static Pattern pOtaSpNumSchema = Pattern.compile("[,\\s]+");
    public String LOG_TAG;
    private boolean mBroadcastEmergencyCallStateChanges;
    private BroadcastReceiver mBroadcastReceiver;
    private CarrierKeyDownloadManager mCDM;
    private CarrierInfoManager mCIM;
    @UnsupportedAppUsage
    public GsmCdmaCallTracker mCT;
    private String mCarrierOtaSpNumSchema;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public int mCdmaSubscriptionSource;
    private HwCustTelephonyManager mCustTelephonyManager;
    @UnsupportedAppUsage
    private Registrant mEcmExitRespRegistrant;
    private final RegistrantList mEcmTimerResetRegistrants;
    public EmergencyNumberTracker mEmergencyNumberTracker;
    private String mEsn;
    private Runnable mExitEcmRunnable;
    public IHwGsmCdmaPhoneEx mHwGsmCdmaPhoneEx;
    private IccPhoneBookInterfaceManager mIccPhoneBookIntManager;
    @UnsupportedAppUsage
    private IccSmsInterfaceManager mIccSmsInterfaceManager;
    private String mImei;
    private String mImeiSv;
    @UnsupportedAppUsage
    private IsimUiccRecords mIsimUiccRecords;
    private String mMeid;
    @UnsupportedAppUsage
    private ArrayList<MmiCode> mPendingMMIs;
    private int mPrecisePhoneType;
    private boolean mResetModemOnRadioTechnologyChange;
    private int mRilVersion;
    private AsyncResult mSSNResult;
    @UnsupportedAppUsage
    public ServiceStateTracker mSST;
    private final SettingsObserver mSettingsObserver;
    private SIMRecords mSimRecords;
    private RegistrantList mSsnRegistrants;
    protected String mUimid;
    private String mVmNumber;
    private PowerManager.WakeLock mWakeLock;
    private IccCardApplicationStatus.AppType newAppType;
    private IccCardApplicationStatus.AppType oldAppType;
    private final BroadcastReceiver sConfigChangeReceiver;

    public static class Cfu {
        final Message mOnComplete;
        final String mSetCfNumber;

        @UnsupportedAppUsage
        Cfu(String cfNumber, Message onComplete) {
            this.mSetCfNumber = cfNumber;
            this.mOnComplete = onComplete;
        }
    }

    public GsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        this(context, ci, notifier, false, phoneId, precisePhoneType, telephonyComponentFactory);
        this.LOG_TAG += "[SUB" + phoneId + "]";
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public GsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, boolean unitTestMode, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        super(precisePhoneType == 1 ? GSM_PHONE : CDMA_PHONE, notifier, context, ci, unitTestMode, phoneId, telephonyComponentFactory);
        this.LOG_TAG = LOG_TAG_STATIC;
        this.mSsnRegistrants = new RegistrantList();
        this.mCdmaSubscriptionSource = -1;
        this.oldAppType = IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN;
        this.newAppType = IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN;
        this.mExitEcmRunnable = new Runnable() {
            /* class com.android.internal.telephony.GsmCdmaPhone.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                GsmCdmaPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.mPendingMMIs = new ArrayList<>();
        this.mEcmTimerResetRegistrants = new RegistrantList();
        this.mResetModemOnRadioTechnologyChange = false;
        this.mBroadcastEmergencyCallStateChanges = false;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.GsmCdmaPhone.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String str = GsmCdmaPhone.this.LOG_TAG;
                    Rlog.d(str, "mBroadcastReceiver: action " + intent.getAction());
                    if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                        if (GsmCdmaPhone.this.mPhoneId == intent.getIntExtra("android.telephony.extra.SLOT_INDEX", -1)) {
                            GsmCdmaPhone gsmCdmaPhone = GsmCdmaPhone.this;
                            gsmCdmaPhone.sendMessage(gsmCdmaPhone.obtainMessage(43));
                        }
                    } else if ("com.huawei.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                        if (intent.getExtras() != null) {
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
                    } else if ("android.telecom.action.CURRENT_TTY_MODE_CHANGED".equals(intent.getAction())) {
                        GsmCdmaPhone.this.updateTtyMode(intent.getIntExtra("android.telecom.intent.extra.CURRENT_TTY_MODE", 0));
                    } else if ("android.telecom.action.TTY_PREFERRED_MODE_CHANGED".equals(intent.getAction())) {
                        GsmCdmaPhone.this.updateUiTtyMode(intent.getIntExtra("android.telecom.intent.extra.TTY_PREFERRED", 0));
                    }
                }
            }
        };
        this.sConfigChangeReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.GsmCdmaPhone.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    GsmCdmaPhone.this.logd("Carrier config changed. Reloading config");
                    if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                        GsmCdmaPhone.this.mCi.getVoiceRadioTechnology(GsmCdmaPhone.this.obtainMessage(40));
                    }
                }
            }
        };
        PhoneExt phoneEx = new PhoneExt();
        phoneEx.setPhone(this);
        this.mHwGsmCdmaPhoneEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwGsmCdmaPhoneEx(this, phoneEx);
        this.mPrecisePhoneType = precisePhoneType;
        initOnce(ci);
        initRatSpecific(precisePhoneType);
        this.mCarrierActionAgent = this.mTelephonyComponentFactory.inject(CarrierActionAgent.class.getName()).makeCarrierActionAgent(this);
        this.mCarrierSignalAgent = this.mTelephonyComponentFactory.inject(CarrierSignalAgent.class.getName()).makeCarrierSignalAgent(this);
        this.mTransportManager = this.mTelephonyComponentFactory.inject(TransportManager.class.getName()).makeTransportManager(this);
        this.mSST = this.mTelephonyComponentFactory.inject(ServiceStateTracker.class.getName()).makeServiceStateTracker(this, this.mCi);
        this.mEmergencyNumberTracker = this.mTelephonyComponentFactory.inject(EmergencyNumberTracker.class.getName()).makeEmergencyNumberTracker(this, this.mCi);
        this.mDataEnabledSettings = this.mTelephonyComponentFactory.inject(DataEnabledSettings.class.getName()).makeDataEnabledSettings(this);
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        for (int transport : availableTransports) {
            if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                this.mDcTrackers.put(transport, this.mTelephonyComponentFactory.inject(DcTracker.class.getName()).makeDcTracker(this, transport));
            } else if (transport == 1) {
                this.mDcTrackers.put(transport, this.mTelephonyComponentFactory.inject(DcTracker.class.getName()).makeDcTracker(this, transport));
            }
        }
        this.mCarrierResolver = this.mTelephonyComponentFactory.inject(CarrierResolver.class.getName()).makeCarrierResolver(this);
        getCarrierActionAgent().registerForCarrierAction(0, this, 48, null, false);
        this.mSST.registerForNetworkAttached(this, 19, null);
        HwTelephonyFactory.getHwPhoneManager().setGsmCdmaPhone(phoneEx, context);
        if (this.mHwGsmCdmaPhoneEx != null) {
            Rlog.d(this.LOG_TAG, "mHwGsmCdmaPhoneEx create success ");
            this.mHwGsmCdmaPhoneEx.restoreSavedRadioTech();
        } else {
            Rlog.d(this.LOG_TAG, "mHwGsmCdmaPhoneEx is null ");
        }
        this.mCi.getVoiceRadioTechnology(obtainMessage(40));
        this.mDeviceStateMonitor = this.mTelephonyComponentFactory.inject(DeviceStateMonitor.class.getName()).makeDeviceStateMonitor(this);
        this.mSST.registerForVoiceRegStateOrRatChanged(this, 46, null);
        this.mSettingsObserver = new SettingsObserver(context, this);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("device_provisioned"), 49);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("device_provisioning_mobile_data"), 50);
        loadTtyMode();
        this.mCustTelephonyManager = (HwCustTelephonyManager) HwCustUtils.createObj(HwCustTelephonyManager.class, new Object[]{context});
        logd("GsmCdmaPhone: constructor: sub = " + this.mPhoneId);
    }

    private void initOnce(CommandsInterface ci) {
        if (ci instanceof SimulatedRadioControl) {
            this.mSimulatedRadioControl = (SimulatedRadioControl) ci;
        }
        this.mCT = this.mTelephonyComponentFactory.inject(GsmCdmaCallTracker.class.getName()).makeGsmCdmaCallTracker(this);
        this.mIccPhoneBookIntManager = this.mTelephonyComponentFactory.inject(IccPhoneBookInterfaceManager.class.getName()).makeIccPhoneBookInterfaceManager(this);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, this.LOG_TAG);
        this.mIccSmsInterfaceManager = this.mTelephonyComponentFactory.inject(IccSmsInterfaceManager.class.getName()).makeIccSmsInterfaceManager(this);
        this.mContext.registerReceiver(this.sConfigChangeReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
        this.mCi.registerForAvailable(this, 1, null);
        this.mCi.registerForOffOrNotAvailable(this, 8, null);
        this.mCi.registerForOn(this, 5, null);
        this.mCi.registerForRadioStateChanged(this, 47, null);
        this.mCi.setOnSuppServiceNotification(this, 2, null);
        this.mCi.setOnUSSD(this, 7, null);
        this.mCi.setOnSs(this, 36, null);
        this.mCi.registerForUnsolOperatorInfo(this, 118, null);
        this.mCdmaSSM = this.mTelephonyComponentFactory.inject(CdmaSubscriptionSourceManager.class.getName()).getCdmaSubscriptionSourceManagerInstance(this.mContext, this.mCi, this, 27, null);
        this.mCi.setEmergencyCallbackMode(this, 25, null);
        this.mCi.registerForExitEmergencyCallbackMode(this, 26, null);
        this.mCi.registerForModemReset(this, 45, null);
        this.mCarrierOtaSpNumSchema = TelephonyManager.from(this.mContext).getOtaSpNumberSchemaForPhone(getPhoneId(), PhoneConfigurationManager.SSSS);
        this.mResetModemOnRadioTechnologyChange = SystemProperties.getBoolean("persist.radio.reset_on_switch", false);
        this.mCi.registerForRilConnected(this, 41, null);
        this.mCi.registerForVoiceRadioTechChanged(this, 39, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("com.huawei.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("android.telecom.action.CURRENT_TTY_MODE_CHANGED");
        filter.addAction("android.telecom.action.TTY_PREFERRED_MODE_CHANGED");
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
        String simState = TelephonyManager.getTelephonyProperty(this.mPhoneId, "gsm.sim.state", IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN);
        StringBuilder sb = new StringBuilder();
        sb.append("init: simState = ");
        sb.append(simState);
        logd(sb.toString());
        if (!IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN.equals(simState) && !IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT.equals(simState) && !IccCardConstantsEx.INTENT_VALUE_ICC_NOT_READY.equals(simState)) {
            isCardAbsentOrNotReady = false;
        }
        if (!isCardAbsentOrNotReady && getSubId() != -1) {
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
            tm.setSimCountryIsoForPhone(this.mPhoneId, PhoneConfigurationManager.SSSS);
            SubscriptionController.getInstance().setCountryIso(PhoneConfigurationManager.SSSS, getSubId());
            return;
        }
        String iso = PhoneConfigurationManager.SSSS;
        try {
            iso = MccTable.countryCodeForMcc(operatorNumeric.substring(0, 3));
        } catch (StringIndexOutOfBoundsException ex) {
            Rlog.e(this.LOG_TAG, "setIsoCountryProperty: countryCodeForMcc error", ex);
        }
        logd("setIsoCountryProperty: set 'gsm.sim.operator.iso-country' to iso=" + iso);
        tm.setSimCountryIsoForPhone(this.mPhoneId, iso);
        SubscriptionController.getInstance().setCountryIso(iso, getSubId());
    }

    @UnsupportedAppUsage
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
            getDataEnabledSettings().setInternalDataEnabled(true);
            notifyEmergencyCallRegistrants(false);
        }
        removeCallbacks(this.mExitEcmRunnable);
        initRatSpecific(precisePhoneType);
        this.mSST.updatePhoneType();
        setPhoneName(precisePhoneType == 1 ? GSM_PHONE : CDMA_PHONE);
        onUpdateIccAvailability();
        unregisterForIccRecordEvents();
        registerForIccRecordEvents();
        this.mCT.updatePhoneType();
        int radioState = this.mCi.getRadioState();
        if (radioState != 2) {
            handleRadioAvailable();
            sendMessage(obtainMessage(1));
            if (this.mCi.getRadioState() == 1) {
                handleRadioOn();
            }
        }
        if (radioState != 1) {
            handleRadioOffOrNotAvailable();
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        logd("GsmCdmaPhone finalized");
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null && wakeLock.isHeld()) {
            Rlog.e(this.LOG_TAG, "UNEXPECTED; mWakeLock is held when finalizing.");
            this.mWakeLock.release();
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    @UnsupportedAppUsage
    public ServiceState getServiceState() {
        ServiceStateTracker serviceStateTracker = this.mSST;
        if ((serviceStateTracker == null || serviceStateTracker.mSS.getState() != 0) && this.mImsPhone != null) {
            ServiceStateTracker serviceStateTracker2 = this.mSST;
            return mergeServiceStates(serviceStateTracker2 == null ? new ServiceState() : serviceStateTracker2.mSS, this.mImsPhone.getServiceState());
        }
        ServiceStateTracker serviceStateTracker3 = this.mSST;
        if (serviceStateTracker3 != null) {
            return serviceStateTracker3.mSS;
        }
        return new ServiceState();
    }

    @Override // com.android.internal.telephony.Phone
    public void getCellLocation(WorkSource workSource, Message rspMsg) {
        this.mSST.requestCellLocation(workSource, rspMsg);
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public PhoneConstants.State getState() {
        PhoneConstants.State imsState;
        if (VSimUtilsInner.isVSimSlot(this.mPhoneId)) {
            return PhoneConstants.State.IDLE;
        }
        if (this.mImsPhone != null && (imsState = this.mImsPhone.getState()) != PhoneConstants.State.IDLE) {
            return imsState;
        }
        PhoneConstants.State imsState2 = this.mCT;
        if (imsState2 == null) {
            return PhoneConstants.State.IDLE;
        }
        return imsState2.mState;
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public int getPhoneType() {
        if (this.mPrecisePhoneType == 1) {
            return 1;
        }
        return 2;
    }

    @Override // com.android.internal.telephony.Phone
    public ServiceStateTracker getServiceStateTracker() {
        return this.mSST;
    }

    @Override // com.android.internal.telephony.Phone
    public EmergencyNumberTracker getEmergencyNumberTracker() {
        return this.mEmergencyNumberTracker;
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public CallTracker getCallTracker() {
        return this.mCT;
    }

    @Override // com.android.internal.telephony.Phone
    public TransportManager getTransportManager() {
        return this.mTransportManager;
    }

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public List<? extends MmiCode> getPendingMmiCodes() {
        return this.mPendingMMIs;
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.PhoneInternalInterface
    public PhoneConstants.DataState getDataConnectionState(String apnType) {
        PhoneConstants.DataState ret = PhoneConstants.DataState.DISCONNECTED;
        ServiceStateTracker serviceStateTracker = this.mSST;
        if (serviceStateTracker == null) {
            ret = PhoneConstants.DataState.DISCONNECTED;
        } else if (serviceStateTracker.getCurrentDataConnectionState() == 0 || (!isPhoneTypeCdma() && !isPhoneTypeCdmaLte() && (!isPhoneTypeGsm() || apnType.equals("emergency")))) {
            int currentTransport = this.mTransportManager.getCurrentTransport(ApnSetting.getApnTypesBitmaskFromString(apnType));
            if (getDcTracker(currentTransport) != null) {
                int i = AnonymousClass4.$SwitchMap$com$android$internal$telephony$DctConstants$State[getDcTracker(currentTransport).getState(apnType).ordinal()];
                if (i == 1 || i == 2) {
                    if (this.mCT.mState == PhoneConstants.State.IDLE || this.mSST.isConcurrentVoiceAndDataAllowed()) {
                        ret = PhoneConstants.DataState.CONNECTED;
                    } else {
                        ret = PhoneConstants.DataState.SUSPENDED;
                    }
                } else if (i != 3) {
                    ret = PhoneConstants.DataState.DISCONNECTED;
                } else {
                    ret = PhoneConstants.DataState.CONNECTING;
                }
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public PhoneInternalInterface.DataActivityState getDataActivityState() {
        PhoneInternalInterface.DataActivityState ret = PhoneInternalInterface.DataActivityState.NONE;
        if (this.mSST.getCurrentDataConnectionState() != 0 || getDcTracker(1) == null) {
            return ret;
        }
        int i = AnonymousClass4.$SwitchMap$com$android$internal$telephony$DctConstants$Activity[getDcTracker(1).getActivity().ordinal()];
        if (i == 1) {
            return PhoneInternalInterface.DataActivityState.DATAIN;
        }
        if (i == 2) {
            return PhoneInternalInterface.DataActivityState.DATAOUT;
        }
        if (i == 3) {
            return PhoneInternalInterface.DataActivityState.DATAINANDOUT;
        }
        if (i != 4) {
            return PhoneInternalInterface.DataActivityState.NONE;
        }
        return PhoneInternalInterface.DataActivityState.DORMANT;
    }

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

    public void notifyPhoneStateChanged() {
        this.mNotifier.notifyPhoneState(this);
    }

    @UnsupportedAppUsage
    public void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChangedP();
    }

    public void notifyNewRingingConnection(Connection c) {
        super.notifyNewRingingConnectionP(c);
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getPhoneId(), 1, LOG_TAG_STATIC);
    }

    public void notifyDisconnect(Connection cn) {
        this.mDisconnectRegistrants.notifyResult(cn);
        this.mNotifier.notifyDisconnectCause(this, cn.getDisconnectCause(), cn.getPreciseDisconnectCause());
    }

    public void notifyUnknownConnection(Connection cn) {
        super.notifyUnknownConnectionP(cn);
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isInEmergencyCall() {
        if (isPhoneTypeGsm()) {
            return false;
        }
        return this.mCT.isInEmergencyCall();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.Phone
    public void setIsInEmergencyCall() {
        if (!isPhoneTypeGsm()) {
            this.mCT.setIsInEmergencyCall();
        }
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isInEmergencySmsMode() {
        return super.isInEmergencySmsMode() || (this.mImsPhone != null && this.mImsPhone.isInEmergencySmsMode());
    }

    private void sendEmergencyCallbackModeChange() {
        Intent intent = new Intent("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intent.putExtra("phoneinECMState", isInEcm());
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
        ActivityManager.broadcastStickyIntent(intent, -1);
        logi("sendEmergencyCallbackModeChange");
    }

    @Override // com.android.internal.telephony.Phone
    public void sendEmergencyCallStateChange(boolean callActive) {
        if (!isPhoneTypeCdma()) {
            logi("sendEmergencyCallbackModeChange - skip for non-cdma");
        } else if (this.mBroadcastEmergencyCallStateChanges) {
            Intent intent = new Intent("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
            intent.putExtra("phoneInEmergencyCall", callActive);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
            ActivityManager.broadcastStickyIntent(intent, -1);
            String str = this.LOG_TAG;
            Rlog.d(str, "sendEmergencyCallStateChange: callActive " + callActive);
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
        this.mBroadcastEmergencyCallStateChanges = broadcast;
    }

    public void notifySuppServiceFailed(PhoneInternalInterface.SuppService code) {
        this.mSuppServiceFailedRegistrants.notifyResult(code);
    }

    @UnsupportedAppUsage
    public void notifyServiceStateChanged(ServiceState ss) {
        super.notifyServiceStateChangedP(ss);
    }

    public void notifyLocationChanged(CellLocation cl) {
        this.mNotifier.notifyCellLocation(this, cl);
    }

    @Override // com.android.internal.telephony.Phone
    public void notifyCallForwardingIndicator() {
        this.mNotifier.notifyCallForwardingChanged(this);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
        AsyncResult asyncResult = this.mSSNResult;
        if (asyncResult != null) {
            this.mSsnRegistrants.notifyRegistrants(asyncResult);
        }
        if (this.mSsnRegistrants.size() == 1) {
            this.mCi.setSuppServiceNotifications(true, null);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
        this.mSSNResult = null;
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForSimRecordsLoaded(Handler h, int what, Object obj) {
        this.mSimRecordsLoadedRegistrants.addUnique(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForSimRecordsLoaded(Handler h) {
        this.mSimRecordsLoadedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void acceptCall(int videoState) throws CallStateException {
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getPhoneId(), 2, LOG_TAG_STATIC);
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || !imsPhone.getRingingCall().isRinging()) {
            this.mCT.acceptCall();
        } else {
            imsPhone.acceptCall(videoState);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void rejectCall() throws CallStateException {
        this.mCT.rejectCall();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void switchHoldingAndActive() throws CallStateException {
        this.mCT.switchWaitingOrHoldingAndActive();
    }

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void conference() {
        if (this.mImsPhone != null && this.mImsPhone.canConference()) {
            logd("conference() - delegated to IMS phone");
            try {
                this.mImsPhone.conference();
            } catch (CallStateException e) {
                loge(e.toString());
            }
        } else if (isPhoneTypeGsm()) {
            this.mCT.conference();
        } else {
            loge("conference: not possible in CDMA");
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("enableEnhancedVoicePrivacy: not expected on GSM");
        } else {
            this.mCi.setPreferredVoicePrivacy(enable, onComplete);
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void getEnhancedVoicePrivacy(Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("getEnhancedVoicePrivacy: not expected on GSM");
        } else {
            this.mCi.getPreferredVoicePrivacy(onComplete);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void clearDisconnected() {
        this.mCT.clearDisconnected();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean canTransfer() {
        if (isPhoneTypeGsm()) {
            return this.mCT.canTransfer();
        }
        loge("canTransfer: not possible in CDMA");
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void explicitCallTransfer() {
        if (isPhoneTypeGsm()) {
            this.mCT.explicitCallTransfer();
        } else {
            loge("explicitCallTransfer: not possible in CDMA");
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public GsmCdmaCall getForegroundCall() {
        return this.mCT.mForegroundCall;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public GsmCdmaCall getBackgroundCall() {
        return this.mCT.mBackgroundCall;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public Call getRingingCall() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null && imsPhone.getRingingCall().isRinging()) {
            return imsPhone.getRingingCall();
        }
        GsmCdmaCallTracker gsmCdmaCallTracker = this.mCT;
        if (gsmCdmaCallTracker == null) {
            return null;
        }
        return gsmCdmaCallTracker.mRingingCall;
    }

    private ServiceState mergeServiceStates(ServiceState baseSs, ServiceState imsSs) {
        if (imsSs.getVoiceRegState() != 0) {
            return baseSs;
        }
        if (imsSs.getRilDataRadioTechnology() == 18) {
            ServiceState newSs = new ServiceState(baseSs);
            newSs.setVoiceRegState(baseSs.getDataRegState());
            newSs.setEmergencyOnly(false);
            return newSs;
        } else if (isCTSimCard(this.mPhoneId)) {
            baseSs.setVoiceRegState(imsSs.getVoiceRegState());
            baseSs.setEmergencyOnly(false);
            return baseSs;
        } else {
            ServiceState newSs2 = new ServiceState(baseSs);
            newSs2.setVoiceRegState(imsSs.getVoiceRegState());
            newSs2.setEmergencyOnly(false);
            return newSs2;
        }
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    @UnsupportedAppUsage
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
        switch (dialString.charAt(0)) {
            case '0':
                return handleCallDeflectionIncallSupplementaryService(dialString);
            case '1':
                return handleCallWaitingIncallSupplementaryService(dialString);
            case '2':
                return handleCallHoldIncallSupplementaryService(dialString);
            case '3':
                return handleMultipartyIncallSupplementaryService(dialString);
            case '4':
                return handleEctIncallSupplementaryService(dialString);
            case '5':
                return handleCcbsIncallSupplementaryService(dialString);
            default:
                return false;
        }
    }

    @UnsupportedAppUsage
    public boolean isInCall() {
        return getForegroundCall().getState().isAlive() || getBackgroundCall().getState().isAlive() || getRingingCall().getState().isAlive();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public Connection dial(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        boolean isEmergency;
        boolean isCarrierSupportVolte;
        boolean isCarrierSupportVolte2;
        boolean useImsForEmergency;
        boolean useImsForEmergency2;
        Object obj;
        if (isPhoneTypeGsm() || dialArgs.uusInfo == null) {
            logd("eni -> dial getprop default_network: " + SystemProperties.get("ro.telephony.default_network"));
            HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getPhoneId(), 0, LOG_TAG_STATIC);
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                isEmergency = PhoneNumberUtils.isEmergencyNumber(getSubId(), dialString);
            } else {
                isEmergency = PhoneNumberUtils.isEmergencyNumber(dialString);
            }
            HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getPhoneId(), 0, "Ecc:" + isEmergency);
            Phone imsPhone = this.mImsPhone;
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            boolean allowWpsOverIms = false;
            if (configManager.getConfigForSubId(getSubId()) != null) {
                boolean alwaysTryImsForEmergencyCarrierConfig = configManager.getConfigForSubId(getSubId()).getBoolean("carrier_use_ims_first_for_emergency_bool");
                boolean isCarrierSupportVolte3 = configManager.getConfigForSubId(getSubId()).getBoolean("carrier_volte_available_bool");
                allowWpsOverIms = configManager.getConfigForSubId(getSubId()).getBoolean("support_wps_over_ims_bool");
                isCarrierSupportVolte = isCarrierSupportVolte3;
                isCarrierSupportVolte2 = alwaysTryImsForEmergencyCarrierConfig;
            } else {
                isCarrierSupportVolte = false;
                isCarrierSupportVolte2 = false;
            }
            boolean isHisiPlatform = HuaweiTelephonyConfigs.isHisiPlatform();
            boolean isWpsCall = dialString != null ? dialString.startsWith(PREFIX_WPS) : false;
            boolean useImsForCall = isImsUseEnabled() && this.mCT.getState() == PhoneConstants.State.IDLE && imsPhone != null && ((getImsSwitch() && isHisiPlatform) || ((imsPhone.isVolteEnabled() || imsPhone.isWifiCallingEnabled() || (imsPhone.isVideoEnabled() && VideoProfile.isVideo(dialArgs.videoState))) && imsPhone.getServiceState().getState() == 0)) && (!isWpsCall || allowWpsOverIms);
            if (imsPhone != null) {
                logd("mCT state = " + this.mCT.getState() + ", ims switch state = " + getImsSwitch() + ", isVideo = " + VideoProfile.isVideo(dialArgs.videoState) + ", video state = " + dialArgs.videoState);
            } else {
                logd("dial -> imsPhone is null");
            }
            boolean useImsForEmergency3 = imsPhone != null && isEmergency && isCarrierSupportVolte2 && ImsManager.getInstance(this.mContext, this.mPhoneId).isNonTtyOrTtyOnVolteEnabled() && imsPhone.isImsAvailable() && this.mCT.mForegroundCall.getState() != Call.State.ACTIVE;
            String dialPart = PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.stripSeparators(dialString));
            boolean isUt = ((dialPart.startsWith(CharacterSets.MIMENAME_ANY_CHARSET) || dialPart.startsWith("#")) && dialPart.endsWith("#")) && !ImsPhoneMmiCode.isVirtualNum(dialPart);
            boolean useImsForUt = imsPhone != null && imsPhone.isUtEnabled();
            if (!VSimUtilsInner.isVSimOn() || isDualImsAvailable()) {
                useImsForEmergency2 = useImsForEmergency3;
                useImsForEmergency = useImsForCall;
            } else {
                logd("vsim is on and the device do not support dual-IMS");
                useImsForEmergency2 = false;
                useImsForEmergency = false;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("useImsForCall=");
            sb.append(useImsForEmergency);
            sb.append(", useImsForEmergency=");
            sb.append(useImsForEmergency2);
            sb.append(", useImsForUt=");
            sb.append(useImsForUt);
            sb.append(", isUt=");
            sb.append(isUt);
            sb.append(", isWpsCall=");
            sb.append(isWpsCall);
            sb.append(", allowWpsOverIms=");
            sb.append(allowWpsOverIms);
            sb.append(", imsPhone=");
            sb.append(imsPhone);
            sb.append(", imsPhone.isVolteEnabled()=");
            Object obj2 = "N/A";
            if (imsPhone != null) {
                obj = obj2;
                obj2 = Boolean.valueOf(imsPhone.isVolteEnabled());
            } else {
                obj = obj2;
            }
            sb.append(obj2);
            sb.append(", imsPhone.isVowifiEnabled()=");
            sb.append(imsPhone != null ? Boolean.valueOf(imsPhone.isWifiCallingEnabled()) : obj);
            sb.append(", imsPhone.isVideoEnabled()=");
            sb.append(imsPhone != null ? Boolean.valueOf(imsPhone.isVideoEnabled()) : obj);
            sb.append(", imsPhone.getServiceState().getState()=");
            sb.append(imsPhone != null ? Integer.valueOf(imsPhone.getServiceState().getState()) : obj);
            sb.append(", mCT.mForegroundCall.getState=");
            sb.append(this.mCT.mForegroundCall.getState());
            logd(sb.toString());
            Phone.checkWfcWifiOnlyModeBeforeDial(this.mImsPhone, this.mPhoneId, this.mContext);
            if (isPhoneTypeGsm() || isCarrierSupportVolte) {
                if ((useImsForEmergency && !isUt) || ((isUt && useImsForUt) || useImsForEmergency2)) {
                    try {
                        logd("Trying IMS PS call");
                        return imsPhone.dial(dialString, dialArgs);
                    } catch (CallStateException e) {
                        logd("IMS PS call exception " + e + "useImsForCall =" + useImsForEmergency + ", imsPhone =" + imsPhone);
                        boolean isDialCsFallback = true;
                        IHwGsmCdmaPhoneEx iHwGsmCdmaPhoneEx = this.mHwGsmCdmaPhoneEx;
                        if (iHwGsmCdmaPhoneEx != null) {
                            isDialCsFallback = iHwGsmCdmaPhoneEx.isDialCsFallback(e);
                        }
                        if (!Phone.CS_FALLBACK.equals(e.getMessage())) {
                            if (!isEmergency || !isDialCsFallback) {
                                CallStateException ce = new CallStateException(e.getError(), e.getMessage());
                                ce.setStackTrace(e.getStackTrace());
                                throw ce;
                            }
                        }
                        logi("IMS call failed with Exception: " + e.getMessage() + ". Falling back to CS.");
                    }
                }
            }
            boolean shouldRunUtIgnoreCsService = HwTelephonyFactory.getHwPhoneManager().shouldRunUtIgnoreCSService(getGsmCdmaPhone(), isUt);
            logd("shouldRunUtIgnoreCsService = " + shouldRunUtIgnoreCsService);
            ServiceStateTracker serviceStateTracker = this.mSST;
            if (serviceStateTracker == null || serviceStateTracker.mSS.getState() != 1 || this.mSST.mSS.getDataRegState() == 0 || isEmergency || shouldRunUtIgnoreCsService) {
                ServiceStateTracker serviceStateTracker2 = this.mSST;
                if (serviceStateTracker2 == null || serviceStateTracker2.mSS.getState() != 3 || VideoProfile.isVideo(dialArgs.videoState) || isEmergency || isUt) {
                    ServiceStateTracker serviceStateTracker3 = this.mSST;
                    if (serviceStateTracker3 == null || serviceStateTracker3.mSS.getState() != 1 || ((this.mSST.mSS.getDataRegState() == 0 && ServiceState.isLte(this.mSST.mSS.getRilDataRadioTechnology())) || VideoProfile.isVideo(dialArgs.videoState) || isEmergency || shouldRunUtIgnoreCsService)) {
                        logd("Trying (non-IMS) CS call");
                        if (isPhoneTypeGsm()) {
                            return dialInternal(dialString, new PhoneInternalInterface.DialArgs.Builder().setIntentExtras(dialArgs.intentExtras).build());
                        }
                        return dialInternal(dialString, dialArgs);
                    }
                    throw new CallStateException(1, "cannot dial voice call in out of service");
                }
                throw new CallStateException(2, "cannot dial voice call in airplane mode");
            }
            throw new CallStateException("cannot dial in current state");
        }
        throw new CallStateException("Sending UUS information NOT supported in CDMA!");
    }

    public boolean isNotificationOfWfcCallRequired(String dialString) {
        PersistableBundle config = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId());
        if (!(config != null && config.getBoolean("notify_international_call_on_wfc_bool"))) {
            return false;
        }
        Phone imsPhone = this.mImsPhone;
        return isImsUseEnabled() && imsPhone != null && !imsPhone.isVolteEnabled() && imsPhone.isWifiCallingEnabled() && !PhoneNumberUtils.isEmergencyNumber(getSubId(), dialString) && PhoneNumberUtils.isInternationalNumber(dialString, getCountryIso());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.Phone
    public Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        return dialInternal(dialString, dialArgs, null);
    }

    /* access modifiers changed from: protected */
    public Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs, ResultReceiver wrappedCallback) throws CallStateException {
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        if (!isPhoneTypeGsm()) {
            IHwGsmCdmaPhoneEx iHwGsmCdmaPhoneEx = this.mHwGsmCdmaPhoneEx;
            if (iHwGsmCdmaPhoneEx == null || !iHwGsmCdmaPhoneEx.dialInternalForCdmaLte(newDialString)) {
                return this.mCT.dial(newDialString, dialArgs.intentExtras);
            }
            return null;
        } else if (handleInCallMmiCommands(newDialString)) {
            return null;
        } else {
            GsmMmiCode mmi = GsmMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, (UiccCardApplication) this.mUiccApplication.get(), wrappedCallback);
            logd("dialInternal: dialing w/ mmi '" + mmi + "'...");
            if (mmi != null) {
                HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", getPhoneId(), 0, "AP_FLOW_SUC");
            }
            if (mmi == null) {
                return this.mCT.dialGsm(newDialString, dialArgs.uusInfo, dialArgs.intentExtras);
            }
            if (mmi.isTemporaryModeCLIR()) {
                return this.mCT.dialGsm(mmi.mDialingNumber, mmi.getCLIRMode(), dialArgs.uusInfo, dialArgs.intentExtras);
            }
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                mmi.setImsPhone(this.mImsPhone);
            }
            try {
                mmi.processCode();
            } catch (CallStateException e) {
            }
            return null;
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
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
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
        try {
            mmi.processCode();
            return true;
        } catch (CallStateException e) {
            return true;
        }
    }

    private void sendUssdResponse(String ussdRequest, CharSequence message, int returnCode, ResultReceiver wrappedCallback) {
        UssdResponse response = new UssdResponse(ussdRequest, message);
        Bundle returnData = new Bundle();
        returnData.putParcelable("USSD_RESPONSE", response);
        wrappedCallback.send(returnCode, returnData);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
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
            logd("handleUssdRequest: exception");
            return false;
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void sendUssdResponse(String ussdMessge) {
        if (isPhoneTypeGsm()) {
            GsmMmiCode mmi = GsmMmiCode.newFromUssdUserInput(ussdMessge, this, (UiccCardApplication) this.mUiccApplication.get());
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
            mmi.sendUssd(ussdMessge);
            return;
        }
        loge("sendUssdResponse: not possible in CDMA");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.mState == PhoneConstants.State.OFFHOOK) {
            this.mCi.sendDtmf(c, null);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void startDtmf(char c) {
        if (PhoneNumberUtils.is12Key(c) || (c >= 'A' && c <= 'D')) {
            this.mCi.startDtmf(c, null);
            return;
        }
        loge("startDtmf called with invalid character '" + c + "'");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void stopDtmf() {
        this.mCi.stopDtmf(null);
    }

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setRadioPower(boolean power) {
        this.mSST.setRadioPower(power);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.PhoneInternalInterface
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getVoiceMailNumber() {
        String number;
        PersistableBundle b;
        PersistableBundle b2;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            number = r != null ? r.getVoiceMailNumber() : PhoneConfigurationManager.SSSS;
            if (TextUtils.isEmpty(number)) {
                number = this.mHwGsmCdmaPhoneEx.getVMNumberWhenIMSIChange();
            }
            if (TextUtils.isEmpty(number)) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                number = sp.getString(VM_NUMBER + getPhoneId(), null);
            }
        } else {
            SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(getContext());
            number = sp2.getString(VM_NUMBER_CDMA + getPhoneId(), null);
        }
        if (TextUtils.isEmpty(number) && (b2 = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId())) != null) {
            String defaultVmNumber = b2.getString("default_vm_number_string");
            String defaultVmNumberRoaming = b2.getString("default_vm_number_roaming_string");
            if (!TextUtils.isEmpty(defaultVmNumberRoaming) && this.mSST.mSS.getRoaming()) {
                number = defaultVmNumberRoaming;
            } else if (!TextUtils.isEmpty(defaultVmNumber)) {
                number = defaultVmNumber;
            }
        }
        if (TextUtils.isEmpty(number) && (b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId())) != null && b.getBoolean("config_telephony_use_own_number_for_voicemail_bool")) {
            number = getLine1Number();
        }
        if (isPhoneTypeGsm()) {
            return number;
        }
        String custNumber = this.mHwGsmCdmaPhoneEx.getCdmaVoiceMailNumberHwCust(this.mContext, getLine1Number(), getPhoneId());
        return !TextUtils.isEmpty(custNumber) ? custNumber : number;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getVoiceMailAlphaTag() {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            return this.mHwGsmCdmaPhoneEx.getDefaultVoiceMailAlphaTagText(this.mContext, r != null ? r.getVoiceMailAlphaTag() : PhoneConfigurationManager.SSSS);
        } else if (PhoneConfigurationManager.SSSS.length() == 0) {
            return this.mContext.getText(17039364).toString();
        } else {
            return PhoneConfigurationManager.SSSS;
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getDeviceId() {
        if (!VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            String str = this.LOG_TAG;
            Rlog.d(str, "getDeviceId, the phone is pending, mPhoneId is: " + this.mPhoneId);
            return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.DEVICE_ID_PREF);
        } else if (isPhoneTypeGsm()) {
            return this.mImei;
        } else {
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            if (configManager.getConfigForSubId(getSubId()) != null && configManager.getConfigForSubId(getSubId()).getBoolean("force_imei_bool")) {
                return this.mImei;
            }
            String id = getMeid();
            if (id != null && !id.matches("^0*$")) {
                return id;
            }
            loge("getDeviceId(): MEID is not initialized use ESN");
            return getEsn();
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
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

    @Override // com.android.internal.telephony.Phone
    public IsimRecords getIsimRecords() {
        return this.mIsimUiccRecords;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getImei() {
        if (VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            return this.mImei;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getImei, the phone is pending, mPhoneId is: " + this.mPhoneId);
        return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.IMEI_PREF);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    @UnsupportedAppUsage
    public String getEsn() {
        if (!VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            String str = this.LOG_TAG;
            Rlog.d(str, "getEsn, the phone is pending, mPhoneId is: " + this.mPhoneId);
            return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.ESN_PREF);
        } else if (isPhoneTypeGsm()) {
            return this.mEsn;
        } else {
            if (5 == TelephonyManager.getDefault().getSimState(getPhoneId())) {
                this.mEsn = this.mCi.getHwUimid();
            }
            return this.mEsn;
        }
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.AbstractPhoneInternalInterface
    public String getMeid() {
        if (VSimUtilsInner.isRadioAvailable(this.mPhoneId)) {
            return this.mMeid;
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "getMeid, the phone is pending, mPhoneId is: " + this.mPhoneId);
        return VSimUtilsInner.getPendingDeviceInfoFromSP(VSimUtilsInner.MEID_PREF);
    }

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getSubscriberId() {
        if (!isPhoneTypeCdma() && !HuaweiTelephonyConfigs.isChinaTelecom() && !IS_FULL_NETWORK_SUPPORTED) {
            IccRecords iccRecords = this.mUiccController.getIccRecords(this.mPhoneId, 1);
            if (iccRecords != null) {
                return iccRecords.getIMSI();
            }
            return null;
        } else if (this.mCdmaSubscriptionSource == 1) {
            Rlog.d(this.LOG_TAG, "getSubscriberId from mSST");
            return this.mSST.getImsi();
        } else {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getIMSI();
            }
            return null;
        }
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.PhoneInternalInterface
    public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int keyType) {
        return CarrierInfoManager.getCarrierInfoForImsiEncryption(keyType, this.mContext);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.PhoneInternalInterface
    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo) {
        CarrierInfoManager.setCarrierInfoForImsiEncryption(imsiEncryptionInfo, this.mContext, this.mPhoneId);
    }

    @Override // com.android.internal.telephony.Phone
    public int getCarrierId() {
        return this.mCarrierResolver.getCarrierId();
    }

    @Override // com.android.internal.telephony.Phone
    public String getCarrierName() {
        return this.mCarrierResolver.getCarrierName();
    }

    @Override // com.android.internal.telephony.Phone
    public int getMNOCarrierId() {
        return this.mCarrierResolver.getMnoCarrierId();
    }

    @Override // com.android.internal.telephony.Phone
    public int getSpecificCarrierId() {
        return this.mCarrierResolver.getSpecificCarrierId();
    }

    @Override // com.android.internal.telephony.Phone
    public String getSpecificCarrierName() {
        return this.mCarrierResolver.getSpecificCarrierName();
    }

    @Override // com.android.internal.telephony.Phone
    public void resolveSubscriptionCarrierId(String simState) {
        this.mCarrierResolver.resolveSubscriptionCarrierId(simState);
    }

    @Override // com.android.internal.telephony.Phone
    public int getCarrierIdListVersion() {
        return this.mCarrierResolver.getCarrierListVersion();
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.PhoneInternalInterface
    public void resetCarrierKeysForImsiEncryption() {
        this.mCIM.resetCarrierKeysForImsiEncryption(this.mContext, this.mPhoneId);
    }

    @Override // com.android.internal.telephony.Phone
    public void setCarrierTestOverride(String mccmnc, String imsi, String iccid, String gid1, String gid2, String pnn, String spn, String carrierPrivilegeRules, String apn) {
        this.mCarrierResolver.setTestOverrideApn(apn);
        this.mCarrierResolver.setTestOverrideCarrierPriviledgeRule(carrierPrivilegeRules);
        IccRecords r = null;
        if (isPhoneTypeGsm()) {
            r = (IccRecords) this.mIccRecords.get();
        } else if (isPhoneTypeCdmaLte()) {
            r = this.mSimRecords;
        } else {
            loge("setCarrierTestOverride fails in CDMA only");
        }
        if (r != null) {
            r.setCarrierTestOverride(mccmnc, imsi, iccid, gid1, gid2, pnn, spn);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getGroupIdLevel1() {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getGid1();
            }
            return null;
        } else if (isPhoneTypeCdma()) {
            loge("GID1 is not available in CDMA");
            return null;
        } else {
            SIMRecords sIMRecords = this.mSimRecords;
            return sIMRecords != null ? sIMRecords.getGid1() : PhoneConfigurationManager.SSSS;
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getGroupIdLevel2() {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getGid2();
            }
            return null;
        } else if (isPhoneTypeCdma()) {
            loge("GID2 is not available in CDMA");
            return null;
        } else {
            SIMRecords sIMRecords = this.mSimRecords;
            return sIMRecords != null ? sIMRecords.getGid2() : PhoneConfigurationManager.SSSS;
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    @UnsupportedAppUsage
    public String getLine1Number() {
        boolean use_usim;
        IHwGsmCdmaPhoneEx iHwGsmCdmaPhoneEx = this.mHwGsmCdmaPhoneEx;
        iHwGsmCdmaPhoneEx.logForTest("getLine1Number", "get phone number..****, phoneType:" + this.mPrecisePhoneType);
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getMsisdnNumber();
            }
            return null;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager.getConfigForSubId(getSubId()) == null) {
            use_usim = false;
        } else {
            use_usim = configManager.getConfigForSubId(getSubId()).getBoolean("use_usim_bool");
        }
        logd("getLine1Number use_usim = " + use_usim);
        if (use_usim) {
            SIMRecords sIMRecords = this.mSimRecords;
            if (sIMRecords != null) {
                return sIMRecords.getMsisdnNumber();
            }
            return null;
        }
        IccRecords records = (IccRecords) this.mIccRecords.get();
        if (records != null && (records instanceof RuimRecords)) {
            return ((RuimRecords) records).getMdn();
        }
        loge("getLine1Number error! newAppType = " + this.newAppType + " oldAppType = " + this.oldAppType);
        return null;
    }

    @Override // com.android.internal.telephony.Phone
    public String getPlmn() {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getPnnHomeName();
            }
            return null;
        } else if (isPhoneTypeCdma()) {
            loge("Plmn is not available in CDMA");
            return null;
        } else {
            SIMRecords sIMRecords = this.mSimRecords;
            if (sIMRecords != null) {
                return sIMRecords.getPnnHomeName();
            }
            return null;
        }
    }

    @Override // com.android.internal.telephony.Phone
    public String getCdmaPrlVersion() {
        return this.mSST.getPrlVersion();
    }

    @Override // com.android.internal.telephony.Phone
    public String getCdmaMin() {
        return this.mSST.getCdmaMin();
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isMinInfoReady() {
        return this.mSST.isMinInfoReady();
    }

    @Override // com.android.internal.telephony.Phone
    public String getMsisdn() {
        this.mHwGsmCdmaPhoneEx.logForTest("getMsisdn", "get phone number..****");
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getMsisdnNumber();
            }
            return null;
        } else if (isPhoneTypeCdmaLte()) {
            SIMRecords sIMRecords = this.mSimRecords;
            if (sIMRecords != null) {
                return sIMRecords.getMsisdnNumber();
            }
            return null;
        } else {
            loge("getMsisdn: not expected on CDMA");
            return null;
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public String getLine1AlphaTag() {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getMsisdnAlphaTag();
            }
            return null;
        }
        loge("getLine1AlphaTag: not possible in CDMA");
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setVoiceMailNumber(String alphaTag, String voiceMailNumber, Message onComplete) {
        if (PhoneConfigurationManager.SSSS.equals(voiceMailNumber)) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(VM_NUMBER, voiceMailNumber).commit();
        }
        this.mVmNumber = voiceMailNumber;
        Message resp = obtainMessage(20, 0, 0, onComplete);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (!isPhoneTypeGsm() && this.mSimRecords != null) {
            r = this.mSimRecords;
        }
        if (r != null) {
            r.setVoiceMailNumber(alphaTag, this.mVmNumber, resp);
        }
    }

    @UnsupportedAppUsage
    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        if (commandInterfaceCFReason == 0 || commandInterfaceCFReason == 1 || commandInterfaceCFReason == 2 || commandInterfaceCFReason == 3 || commandInterfaceCFReason == 4 || commandInterfaceCFReason == 5) {
            return true;
        }
        return false;
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public String getSystemProperty(String property, String defValue) {
        if (getUnitTestMode()) {
            return null;
        }
        return TelephonyManager.getTelephonyProperty(this.mPhoneId, property, defValue);
    }

    @UnsupportedAppUsage
    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        if (commandInterfaceCFAction == 0 || commandInterfaceCFAction == 1 || commandInterfaceCFAction == 3 || commandInterfaceCFAction == 4) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    private boolean isImsUtEnabledOverCdma() {
        return isPhoneTypeCdmaLte() && this.mImsPhone != null && this.mImsPhone.isUtEnabled();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        getCallForwardingOption(commandInterfaceCFReason, 0, onComplete);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getCallForwardingOption(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
        Message resp;
        Phone imsPhone;
        if ((isPhoneTypeGsm() || (this.mImsPhone != null && isPhoneTypeCdmaLte())) && (imsPhone = this.mImsPhone) != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
            imsPhone.getCallForwardingOption(commandInterfaceCFReason, onComplete);
        } else if (!isPhoneTypeGsm()) {
            loge("getCallForwardingOption: not possible in CDMA without IMS");
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        Message resp;
        Phone imsPhone;
        if ((isPhoneTypeGsm() || (this.mImsPhone != null && isPhoneTypeCdmaLte())) && (imsPhone = this.mImsPhone) != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
            imsPhone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, onComplete);
        } else if (!isPhoneTypeGsm()) {
            loge("setCallForwardingOption: not possible in CDMA");
        } else if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            if (commandInterfaceCFReason == 0) {
                resp = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cfu(dialingNumber, onComplete));
            } else {
                resp = onComplete;
            }
            this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, 1, this.mHwGsmCdmaPhoneEx.processPlusSymbol(dialingNumber, getSubscriberId()), timerSeconds, resp);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getCallBarring(String facility, String password, Message onComplete, int serviceClass) {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !imsPhone.isUtEnabled()) {
                this.mCi.queryFacilityLock(facility, password, serviceClass, onComplete);
            } else {
                imsPhone.getCallBarring(facility, password, onComplete, serviceClass);
            }
        } else {
            loge("getCallBarringOption: not possible in CDMA");
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !imsPhone.isUtEnabled()) {
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getCallWaiting(Message onComplete) {
        Phone imsPhone;
        if ((isPhoneTypeGsm() || (this.mImsPhone != null && isPhoneTypeCdmaLte())) && (imsPhone = this.mImsPhone) != null && ((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
            imsPhone.getCallWaiting(onComplete);
        } else if (isPhoneTypeGsm()) {
            this.mCi.queryCallWaiting(0, onComplete);
        } else {
            this.mCi.queryCallWaiting(1, onComplete);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCallWaiting(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm() || (this.mImsPhone != null && isPhoneTypeCdmaLte())) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone == null || !((ImsPhone) imsPhone).mHwImsPhoneEx.isUtEnable()) {
                int serviceClass = 1;
                PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
                if (b != null) {
                    serviceClass = b.getInt("call_waiting_service_class_int", 1);
                }
                if (isPhoneTypeGsm()) {
                    if (CALL_WAITING_CLASS_NONE) {
                        serviceClass = 0;
                    }
                    logd("setCallWaiting enable = " + enable + " serviceClass = " + serviceClass);
                    this.mCi.setCallWaiting(enable, serviceClass, onComplete);
                    return;
                }
                return;
            }
            imsPhone.setCallWaiting(enable, onComplete);
            return;
        }
        loge("method setCallWaiting is NOT supported in CDMA without IMS!");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getAvailableNetworks(Message response) {
        if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
            this.mCi.getAvailableNetworks(obtainMessage(51, this.mHwGsmCdmaPhoneEx.getCustAvailableNetworksMessage(response)));
            return;
        }
        loge("getAvailableNetworks: not possible in CDMA");
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void startNetworkScan(NetworkScanRequest nsr, Message response) {
        this.mCi.startNetworkScan(nsr, response);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void stopNetworkScan(Message response) {
        this.mCi.stopNetworkScan(response);
    }

    @Override // com.android.internal.telephony.Phone
    public void setTTYMode(int ttyMode, Message onComplete) {
        super.setTTYMode(ttyMode, onComplete);
        if (this.mImsPhone != null) {
            this.mImsPhone.setTTYMode(ttyMode, onComplete);
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        if (this.mImsPhone != null) {
            this.mImsPhone.setUiTTYMode(uiTtyMode, onComplete);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setMute(boolean muted) {
        GsmCdmaCallTracker gsmCdmaCallTracker = this.mCT;
        if (gsmCdmaCallTracker != null) {
            gsmCdmaCallTracker.setMute(muted);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean getMute() {
        GsmCdmaCallTracker gsmCdmaCallTracker = this.mCT;
        if (gsmCdmaCallTracker == null) {
            return false;
        }
        return gsmCdmaCallTracker.getMute();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void updateServiceLocation() {
        this.mSST.enableSingleLocationUpdate();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void enableLocationUpdates() {
        this.mSST.enableLocationUpdates();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void disableLocationUpdates() {
        this.mSST.disableLocationUpdates();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean getDataRoamingEnabled() {
        if (getDcTracker(1) != null) {
            return getDcTracker(1).getDataRoamingEnabled();
        }
        return false;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setDataRoamingEnabled(boolean enable) {
        if (getDcTracker(1) != null) {
            getDcTracker(1).setDataRoamingEnabledByUser(enable);
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
        this.mCi.registerForCdmaOtaProvision(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForCdmaOtaStatusChange(Handler h) {
        this.mCi.unregisterForCdmaOtaProvision(h);
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        this.mSST.registerForSubscriptionInfoReady(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mSST.unregisterForSubscriptionInfoReady(h);
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        this.mEcmExitRespRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unsetOnEcbModeExitResponse(Handler h) {
        this.mEcmExitRespRegistrant.clear();
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCT.registerForCallWaiting(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForCallWaiting(Handler h) {
        this.mCT.unregisterForCallWaiting(h);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public boolean isUserDataEnabled() {
        if (this.mDataEnabledSettings.isAnySimDetected() || !this.mDataEnabledSettings.isProvisioning()) {
            return this.mDataEnabledSettings.isUserDataEnabled();
        }
        return this.mDataEnabledSettings.isProvisioningDataEnabled();
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
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
            return;
        }
        String str3 = this.LOG_TAG;
        Rlog.i(str3, "onMMIDone: invalid response or already handled; ignoring: " + mmi);
    }

    public boolean supports3gppCallForwardingWhileRoaming() {
        PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
        if (b != null) {
            return b.getBoolean("support_3gpp_call_forwarding_while_roaming_bool", true);
        }
        return true;
    }

    private void onNetworkInitiatedUssd(MmiCode mmi) {
        String str = this.LOG_TAG;
        Rlog.v(str, "onNetworkInitiatedUssd: mmi=" + mmi);
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, (Throwable) null));
    }

    private void onIncomingUSSD(int ussdMode, String ussdMessage) {
        if (!isPhoneTypeGsm()) {
            loge("onIncomingUSSD: not expected on GSM");
        }
        boolean isUssdRelease = false;
        boolean isUssdRequest = ussdMode == 1;
        boolean isUssdError = (ussdMode == 0 || ussdMode == 1 || ussdMode == 12) ? false : true;
        if (this.mHwGsmCdmaPhoneEx.isUssdOkForRelease()) {
            isUssdError = isUssdError && ussdMode != 2;
        }
        if (ussdMode == 2) {
            isUssdRelease = true;
        }
        if (HwTelephonyFactory.getHwPhoneManager().needUnEscapeHtmlforUssdMsg(getGsmCdmaPhone())) {
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
                found = (GsmMmiCode) this.mPendingMMIs.get(i);
                break;
            } else {
                i++;
            }
        }
        if (found != null) {
            int custUssdState = -1;
            IHwGsmCdmaPhoneEx iHwGsmCdmaPhoneEx = this.mHwGsmCdmaPhoneEx;
            if (iHwGsmCdmaPhoneEx != null) {
                custUssdState = iHwGsmCdmaPhoneEx.removeUssdCust(getGsmCdmaPhone());
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

    @UnsupportedAppUsage
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

    private void handleRadioPowerStateChange() {
        int radioState = this.mCi.getRadioState();
        String str = this.LOG_TAG;
        Rlog.i(str, "handleRadioPowerStateChange, state= " + radioState);
        Settings.System.putInt(this.mContext.getContentResolver(), "hw_telephony_radio_state", radioState);
        this.mNotifier.notifyRadioPowerStateChanged(this, radioState);
    }

    private void handleEventModemReset(Message msg) {
        HwCustTelephonyManager hwCustTelephonyManager;
        if (!isInEcm()) {
            return;
        }
        if (!isPhoneTypeGsm() || ((hwCustTelephonyManager = this.mCustTelephonyManager) != null && hwCustTelephonyManager.isVZW())) {
            handleExitEmergencyCallbackMode(msg);
        } else if (this.mImsPhone != null) {
            this.mImsPhone.handleExitEmergencyCallbackMode();
        }
    }

    @Override // com.android.internal.telephony.Phone, android.os.Handler
    public void handleMessage(Message msg) {
        if (!this.mHwGsmCdmaPhoneEx.beforeHandleMessage(msg)) {
            boolean z = false;
            switch (msg.what) {
                case 1:
                    handleRadioAvailable();
                    this.mHwGsmCdmaPhoneEx.resetReduceSARPowerGrade();
                    this.mHwGsmCdmaPhoneEx.updateReduceSARState();
                    break;
                case 2:
                    logd("Event EVENT_SSN Received");
                    if (isPhoneTypeGsm()) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        this.mSSNResult = ar;
                        SuppServiceNotification suppServiceNotification = (SuppServiceNotification) ar.result;
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
                    updateVoiceMail();
                    this.mSimRecordsLoadedRegistrants.notifyRegistrants();
                    this.mHwGsmCdmaPhoneEx.processEccNumber(this.mSST);
                    break;
                case 4:
                case 11:
                case 14:
                case 15:
                case 16:
                case 17:
                case TelephonyProto.RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /* 24 */:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 37:
                case 38:
                default:
                    super.handleMessage(msg);
                    break;
                case 5:
                    logd("Event EVENT_RADIO_ON Received");
                    handleRadioOn();
                    break;
                case 6:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception == null) {
                        logd("Baseband version: " + ar2.result);
                        String baseBand = SystemProperties.get("gsm.config.version.baseband", PhoneConfigurationManager.SSSS);
                        String baseBandSingle = SystemProperties.get("gsm.config.single.version.baseband", PhoneConfigurationManager.SSSS);
                        if (!TextUtils.isEmpty(baseBand)) {
                            SystemProperties.set("gsm.version.baseband", baseBand);
                        } else if (!TextUtils.isEmpty(baseBandSingle)) {
                            TelephonyManager.from(this.mContext).setBasebandVersionForPhone(getPhoneId(), baseBandSingle);
                        } else {
                            TelephonyManager.from(this.mContext).setBasebandVersionForPhone(getPhoneId(), (String) ar2.result);
                        }
                        TelephonyManager.from(this.mContext);
                        TelephonyManager.setTelephonyProperty(getPhoneId(), "gsm.modem.version.baseband", (String) ar2.result);
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
                        this.mHwGsmCdmaPhoneEx.logForImei(GSM_PHONE, this.mImei);
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
                    if (ar9.exception != null) {
                        retryGetDeviceId(msg.arg1, 2);
                        break;
                    } else {
                        String[] respId = (String[]) ar9.result;
                        this.mImei = respId[0];
                        this.mHwGsmCdmaPhoneEx.logForImei(CDMA_PHONE, this.mImei);
                        this.mImeiSv = respId[1];
                        this.mEsn = respId[2];
                        this.mMeid = respId[3];
                        if (respId.length > 4) {
                            this.mUimid = respId[4];
                            SystemProperties.set("persist.radio.hwuimid", this.mUimid);
                            break;
                        }
                    }
                    break;
                case 22:
                    logd("Event EVENT_RUIM_RECORDS_LOADED Received");
                    updateCurrentCarrierInProvider();
                    this.mHwGsmCdmaPhoneEx.processEccNumber(this.mSST);
                    break;
                case 23:
                    logd("Event EVENT_NV_READY Received");
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
                    if (this.mSST.mSS.getIsManualSelection()) {
                        setNetworkSelectionModeAutomatic((Message) ar10.result);
                        logd("SET_NETWORK_SELECTION_AUTOMATIC: set to automatic");
                        break;
                    } else {
                        logd("SET_NETWORK_SELECTION_AUTOMATIC: already automatic, ignore");
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
                            int newVoiceTech = ((int[]) ar13.result)[0];
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
                    if (ar14.exception != null || ar14.result == null) {
                        logd("Unexpected exception on EVENT_RIL_CONNECTED");
                        this.mRilVersion = -1;
                        break;
                    } else {
                        this.mRilVersion = ((Integer) ar14.result).intValue();
                        break;
                    }
                    break;
                case 42:
                    phoneObjectUpdater(msg.arg1);
                    break;
                case 43:
                    if (!this.mContext.getResources().getBoolean(17891550)) {
                        this.mCi.getVoiceRadioTechnology(obtainMessage(40));
                    }
                    HwFrameworkFactory.updateImsServiceConfig(this.mContext, this.mPhoneId, true);
                    PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
                    if (b != null) {
                        boolean broadcastEmergencyCallStateChanges = b.getBoolean("broadcast_emergency_call_state_changes_bool");
                        logd("broadcastEmergencyCallStateChanges = " + broadcastEmergencyCallStateChanges);
                        setBroadcastEmergencyCallStateChanges(broadcastEmergencyCallStateChanges);
                    } else {
                        loge("didn't get broadcastEmergencyCallStateChanges from carrier config");
                    }
                    if (b != null) {
                        int config_cdma_roaming_mode = b.getInt("cdma_roaming_mode_int");
                        int current_cdma_roaming_mode = Settings.Global.getInt(getContext().getContentResolver(), "roaming_settings", -1);
                        if (config_cdma_roaming_mode != -1) {
                            if (config_cdma_roaming_mode != 0 && config_cdma_roaming_mode != 1 && config_cdma_roaming_mode != 2) {
                                loge("Invalid cdma_roaming_mode settings: " + config_cdma_roaming_mode);
                                break;
                            } else {
                                logd("cdma_roaming_mode is going to changed to " + config_cdma_roaming_mode);
                                setCdmaRoamingPreference(config_cdma_roaming_mode, obtainMessage(44));
                                break;
                            }
                        } else if (current_cdma_roaming_mode != config_cdma_roaming_mode) {
                            logd("cdma_roaming_mode is going to changed to " + current_cdma_roaming_mode);
                            setCdmaRoamingPreference(current_cdma_roaming_mode, obtainMessage(44));
                            break;
                        }
                    } else {
                        loge("didn't get the cdma_roaming_mode changes from the carrier config.");
                        break;
                    }
                    break;
                case 44:
                    logd("cdma_roaming_mode change is done");
                    break;
                case 45:
                    logd("Event EVENT_MODEM_RESET Received isInEcm = " + isInEcm() + " isPhoneTypeGsm = " + isPhoneTypeGsm() + " mImsPhone = " + this.mImsPhone);
                    handleEventModemReset(msg);
                    break;
                case 46:
                    Pair<Integer, Integer> vrsRatPair = (Pair) ((AsyncResult) msg.obj).result;
                    onVoiceRegStateOrRatChanged(((Integer) vrsRatPair.first).intValue(), ((Integer) vrsRatPair.second).intValue());
                    break;
                case 47:
                    logd("EVENT EVENT_RADIO_STATE_CHANGED");
                    handleRadioPowerStateChange();
                    break;
                case 48:
                    this.mDataEnabledSettings.setCarrierDataEnabled(((Boolean) ((AsyncResult) msg.obj).result).booleanValue());
                    break;
                case 49:
                    this.mDataEnabledSettings.updateProvisionedChanged();
                    break;
                case 50:
                    this.mDataEnabledSettings.updateProvisioningDataEnabled();
                    break;
                case 51:
                    AsyncResult ar15 = (AsyncResult) msg.obj;
                    if (!(ar15.exception != null || ar15.result == null || this.mSST == null)) {
                        List<OperatorInfo> filteredInfoList = new ArrayList<>();
                        for (OperatorInfo operatorInfo : (List) ar15.result) {
                            if (OperatorInfo.State.CURRENT == operatorInfo.getState()) {
                                filteredInfoList.add(new OperatorInfo(this.mSST.filterOperatorNameByPattern(operatorInfo.getOperatorAlphaLong()), this.mSST.filterOperatorNameByPattern(operatorInfo.getOperatorAlphaShort()), operatorInfo.getOperatorNumeric(), operatorInfo.getState()));
                            } else {
                                filteredInfoList.add(operatorInfo);
                            }
                        }
                        ar15.result = filteredInfoList;
                    }
                    Message onComplete4 = (Message) ar15.userObj;
                    if (onComplete4 != null) {
                        AsyncResult.forMessage(onComplete4, ar15.result, ar15.exception);
                        onComplete4.sendToTarget();
                        break;
                    }
                    break;
            }
            this.mHwGsmCdmaPhoneEx.afterHandleMessage(msg);
        }
    }

    public UiccCardApplication getUiccCardApplication() {
        if (isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.Phone
    public void onUpdateIccAvailability() {
        UiccCardApplication newUiccApplication;
        if (this.mUiccController != null) {
            if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
                UiccCardApplication newUiccApplication2 = this.mUiccController.getUiccCardApplication(this.mPhoneId, 3);
                IsimUiccRecords newIsimUiccRecords = null;
                if (newUiccApplication2 != null) {
                    newIsimUiccRecords = (IsimUiccRecords) newUiccApplication2.getIccRecords();
                    logd("New ISIM application found");
                }
                this.mIsimUiccRecords = newIsimUiccRecords;
            }
            SIMRecords sIMRecords = this.mSimRecords;
            if (sIMRecords != null) {
                sIMRecords.unregisterForRecordsLoaded(this);
            }
            if (isPhoneTypeCdmaLte() || isPhoneTypeCdma()) {
                UiccCardApplication newUiccApplication3 = this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
                SIMRecords newSimRecords = null;
                if (newUiccApplication3 != null) {
                    newSimRecords = (SIMRecords) newUiccApplication3.getIccRecords();
                }
                this.mSimRecords = newSimRecords;
                SIMRecords sIMRecords2 = this.mSimRecords;
                if (sIMRecords2 != null) {
                    sIMRecords2.registerForRecordsLoaded(this, 3, null);
                }
            } else {
                this.mSimRecords = null;
            }
            if (VSimUtilsInner.isHisiVSimSlot(this.mPhoneId)) {
                newUiccApplication = VSimUtilsInner.getVSimUiccCardApplication(1);
            } else {
                newUiccApplication = getUiccCardApplication();
            }
            if (!isPhoneTypeGsm() && newUiccApplication == null) {
                logd("can't find 3GPP2 application; trying APP_FAM_3GPP");
                newUiccApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
            }
            UiccCardApplication app = (UiccCardApplication) this.mUiccApplication.get();
            if (newUiccApplication != null) {
                this.newAppType = newUiccApplication.getType();
            }
            logd("newAppType = " + this.newAppType + ", oldAppType = " + this.oldAppType);
            if (app != newUiccApplication || (this.newAppType != IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN && this.oldAppType != IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN && this.newAppType != this.oldAppType)) {
                if (app != null) {
                    logd("Removing stale icc objects.");
                    if (this.mIccRecords.get() != null) {
                        unregisterForIccRecordEvents();
                        this.mIccPhoneBookIntManager.updateIccRecords(null);
                    }
                    this.mIccRecords.set(null);
                    this.mUiccApplication.set(null);
                }
                if (newUiccApplication != null) {
                    logd("New Uicc application found");
                    this.oldAppType = newUiccApplication.getType();
                    logd("New Uicc application found. type = " + newUiccApplication.getType());
                    IccRecords iccRecords = newUiccApplication.getIccRecords();
                    this.mUiccApplication.set(newUiccApplication);
                    this.mIccRecords.set(iccRecords);
                    registerForIccRecordEvents();
                    this.mIccPhoneBookIntManager.updateIccRecords(iccRecords);
                    if (iccRecords != null) {
                        String simOperatorNumeric = iccRecords.getOperatorNumeric();
                        logd("New simOperatorNumeric = " + simOperatorNumeric);
                        if (!TextUtils.isEmpty(simOperatorNumeric)) {
                            TelephonyManager.from(this.mContext).setSimOperatorNumericForPhone(this.mPhoneId, simOperatorNumeric);
                        }
                    }
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

    @Override // com.android.internal.telephony.Phone
    public boolean updateCurrentCarrierInProvider() {
        long currentDds = (long) PhoneFactory.getTopPrioritySubscriptionId();
        String operatorNumeric = getOperatorNumeric();
        logd("updateCurrentCarrierInProvider: mSubId = " + getSubId() + " currentDds = " + currentDds + " operatorNumeric = " + operatorNumeric);
        if (TextUtils.isEmpty(operatorNumeric) || ((long) getSubId()) != currentDds) {
            return false;
        }
        try {
            String currentOperatorNumeric = HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated() ? HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric() : operatorNumeric;
            Uri uri = Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "current");
            ContentValues map = new ContentValues();
            map.put("numeric", currentOperatorNumeric);
            this.mContext.getContentResolver().insert(uri, map);
            return true;
        } catch (SQLException e) {
            Rlog.e(this.LOG_TAG, "Can't store current operator", e);
            return false;
        }
    }

    private boolean updateCurrentCarrierInProvider(String operatorNumeric) {
        if (isPhoneTypeCdma() || (isPhoneTypeCdmaLte() && this.mUiccController.getUiccCardApplication(this.mPhoneId, 1) == null)) {
            logd("CDMAPhone: updateCurrentCarrierInProvider called");
            if (TextUtils.isEmpty(operatorNumeric)) {
                return false;
            }
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
                return false;
            }
        } else {
            logd("updateCurrentCarrierInProvider not updated X retVal=true");
            return true;
        }
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

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return this.mIccPhoneBookIntManager;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void activateCellBroadcastSms(int activate, Message response) {
        loge("[GsmCdmaPhone] activateCellBroadcastSms() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void getCellBroadcastSmsConfig(Message response) {
        loge("[GsmCdmaPhone] getCellBroadcastSmsConfig() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        loge("[GsmCdmaPhone] setCellBroadcastSmsConfig() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    @Override // com.android.internal.telephony.Phone
    public boolean needsOtaServiceProvisioning() {
        if (!isPhoneTypeGsm() && this.mSST.getOtasp() != 3) {
            return true;
        }
        return false;
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isCspPlmnEnabled() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.isCspPlmnEnabled();
        }
        return false;
    }

    public boolean shouldForceAutoNetworkSelect() {
        int nwMode = Phone.PREFERRED_NT_MODE;
        if (!SubscriptionManager.isValidSubscriptionId(getSubId())) {
            return false;
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int nwMode2 = Settings.Global.getInt(contentResolver, "preferred_network_mode" + this.mPhoneId, nwMode);
        logd("shouldForceAutoNetworkSelect in mode = " + nwMode2);
        if (!isManualSelProhibitedInGlobalMode() || !(nwMode2 == 10 || nwMode2 == 7)) {
            logd("Should not force auto network select mode = " + nwMode2);
            return false;
        }
        logd("Should force auto network select mode = " + nwMode2);
        return true;
    }

    @UnsupportedAppUsage
    private boolean isManualSelProhibitedInGlobalMode() {
        String[] configArray;
        boolean isProhibited = false;
        String configString = getContext().getResources().getString(17041086);
        if (!TextUtils.isEmpty(configString) && (configArray = configString.split(";")) != null && ((configArray.length == 1 && configArray[0].equalsIgnoreCase("true")) || (configArray.length == 2 && !TextUtils.isEmpty(configArray[1]) && configArray[0].equalsIgnoreCase("true") && isMatchGid(configArray[1])))) {
            isProhibited = true;
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
                r.registerForImsiReady(this, 105, null);
                this.mHwGsmCdmaPhoneEx.registerForCsgRecordsLoadedEvent();
                return;
            }
            r.registerForRecordsLoaded(this, 22, null);
            if (isPhoneTypeCdmaLte()) {
                r.registerForRecordsLoaded(this, 3, null);
            }
        }
    }

    private void unregisterForIccRecordEvents() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.unregisterForNetworkSelectionModeAutomatic(this);
            r.unregisterForRecordsEvents(this);
            r.unregisterForRecordsLoaded(this);
            this.mHwGsmCdmaPhoneEx.unregisterForCsgRecordsLoadedEvent();
        }
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
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
        Registrant registrant = this.mEcmExitRespRegistrant;
        if (registrant != null) {
            registrant.notifyRegistrant(ar);
        }
        if (ar.exception == null) {
            if (isInEcm()) {
                setIsInEcm(false);
            }
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            sendEmergencyCallbackModeChange();
            this.mDataEnabledSettings.setInternalDataEnabled(true);
            notifyEmergencyCallRegistrants(false);
        }
    }

    public void notifyEmergencyCallRegistrants(boolean started) {
        this.mEmergencyCallToggledRegistrants.notifyResult(Integer.valueOf(started ? 1 : 0));
    }

    public void handleTimerInEmergencyCallbackMode(int action) {
        if (action == 0) {
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
            this.mEcmTimerResetRegistrants.notifyResult(Boolean.FALSE);
        } else if (action != 1) {
            String str = this.LOG_TAG;
            Rlog.e(str, "handleTimerInEmergencyCallbackMode, unsupported action " + action);
        } else {
            removeCallbacks(this.mExitEcmRunnable);
            this.mEcmTimerResetRegistrants.notifyResult(Boolean.TRUE);
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
                        return false;
                    }
                    if (dialStr.regionMatches(0, sch[2], 0, Integer.parseInt(sch[1]))) {
                        return true;
                    }
                    Rlog.d(this.LOG_TAG, "isCarrierOtaSpNum,not otasp number");
                    return false;
                } else if (sysSelCodeInt != -1) {
                    return checkOtaSpNumBasedOnSysSelCode(sysSelCodeInt, sch);
                } else {
                    Rlog.d(this.LOG_TAG, "isCarrierOtaSpNum,sysSelCodeInt is invalid");
                    return false;
                }
            } else {
                String str3 = this.LOG_TAG;
                Rlog.d(str3, "isCarrierOtaSpNum,ota schema pattern not right" + this.mCarrierOtaSpNumSchema);
                return false;
            }
        } else {
            Rlog.d(this.LOG_TAG, "isCarrierOtaSpNum,ota schema pattern empty");
            return false;
        }
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isOtaSpNumber(String dialStr) {
        if (isPhoneTypeGsm()) {
            return super.isOtaSpNumber(dialStr);
        }
        boolean isOtaSpNum = false;
        String dialableStr = PhoneNumberUtils.extractNetworkPortionAlt(dialStr);
        if (dialableStr != null && !(isOtaSpNum = isIs683OtaSpDialStr(dialableStr))) {
            isOtaSpNum = isCarrierOtaSpNum(dialableStr);
        }
        String str = this.LOG_TAG;
        Rlog.d(str, "isOtaSpNumber " + isOtaSpNum);
        return isOtaSpNum;
    }

    @Override // com.android.internal.telephony.Phone
    public int getCdmaEriIconIndex() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriIconIndex();
        }
        return getServiceState().getCdmaEriIconIndex();
    }

    @Override // com.android.internal.telephony.Phone
    public int getCdmaEriIconMode() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriIconMode();
        }
        return getServiceState().getCdmaEriIconMode();
    }

    @Override // com.android.internal.telephony.Phone
    @UnsupportedAppUsage
    public String getCdmaEriText() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriText();
        }
        return this.mSST.getCdmaEriText(getServiceState().getCdmaRoamingIndicator(), getServiceState().getCdmaDefaultRoamingIndicator());
    }

    @Override // com.android.internal.telephony.Phone
    public boolean isCdmaSubscriptionAppPresent() {
        UiccCardApplication cdmaApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
        return cdmaApplication != null && (cdmaApplication.getType() == IccCardApplicationStatus.AppType.APPTYPE_CSIM || cdmaApplication.getType() == IccCardApplicationStatus.AppType.APPTYPE_RUIM);
    }

    private void phoneObjectUpdater(int newVoiceRadioTech) {
        logd("phoneObjectUpdater: newVoiceRadioTech=" + newVoiceRadioTech);
        if (ServiceState.isLte(newVoiceRadioTech) || newVoiceRadioTech == 0) {
            PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
            if (b != null) {
                int volteReplacementRat = b.getInt("volte_replacement_rat_int");
                logd("phoneObjectUpdater: volteReplacementRat=" + volteReplacementRat);
                if (volteReplacementRat != 0 && (ServiceState.isGsm(volteReplacementRat) || isCdmaSubscriptionAppPresent())) {
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
            return;
        }
        boolean isNoCardInCall = !SubscriptionManager.isValidSubscriptionId(getSubId()) && CallManager.getInstance().hasActiveFgCall();
        if (CallManager.getInstance().hasActiveFgCall(getSubId()) || isNoCardInCall) {
            logd("has ActiveFgCall, should not updatePhoneObject");
            return;
        }
        boolean oldPowerState = false;
        if (this.mResetModemOnRadioTechnologyChange && this.mCi.getRadioState() == 1) {
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

    private void switchVoiceRadioTech(int newVoiceRadioTech) {
        String outgoingPhoneName = getPhoneName();
        StringBuilder sb = new StringBuilder();
        sb.append("Switching Voice Phone : ");
        sb.append(outgoingPhoneName);
        sb.append(" >>> ");
        sb.append(ServiceState.isGsm(newVoiceRadioTech) ? GSM_PHONE : CDMA_PHONE);
        logd(sb.toString());
        if (ServiceState.isCdma(newVoiceRadioTech)) {
            UiccCardApplication cdmaApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
            if (cdmaApplication == null || cdmaApplication.getType() != IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
                switchPhoneType(6);
                TelephonyManager.setTelephonyProperty(this.mPhoneId, "persist.radio.last_phone_type", CDMA_PHONE);
                return;
            }
            switchPhoneType(2);
        } else if (ServiceState.isGsm(newVoiceRadioTech)) {
            switchPhoneType(1);
            TelephonyManager.setTelephonyProperty(this.mPhoneId, "persist.radio.last_phone_type", GSM_PHONE);
        } else {
            loge("deleteAndCreatePhone: newVoiceRadioTech=" + newVoiceRadioTech + " is not CDMA or GSM (error) - aborting!");
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void setSignalStrengthReportingCriteria(int[] thresholds, int ran) {
        this.mCi.setSignalStrengthReportingCriteria(REPORTING_HYSTERESIS_MILLIS, 2, thresholds, ran, null);
    }

    @Override // com.android.internal.telephony.Phone
    public void setLinkCapacityReportingCriteria(int[] dlThresholds, int[] ulThresholds, int ran) {
        this.mCi.setLinkCapacityReportingCriteria(REPORTING_HYSTERESIS_MILLIS, 50, 50, dlThresholds, ulThresholds, ran, null);
    }

    @Override // com.android.internal.telephony.Phone
    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return this.mIccSmsInterfaceManager;
    }

    @Override // com.android.internal.telephony.Phone
    public void updatePhoneObject(int voiceRadioTech) {
        logd("updatePhoneObject: radioTechnology=" + voiceRadioTech);
        sendMessage(obtainMessage(42, voiceRadioTech, 0, null));
    }

    @Override // com.android.internal.telephony.Phone
    public void setImsRegistrationState(boolean registered) {
        this.mSST.setImsRegistrationState(registered);
    }

    @Override // com.android.internal.telephony.Phone
    public boolean getIccRecordsLoaded() {
        UiccProfile uiccProfile = getUiccProfile();
        return uiccProfile != null && uiccProfile.getIccRecordsLoaded();
    }

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.Phone
    public boolean setOperatorBrandOverride(String brand) {
        UiccCard card;
        if (this.mUiccController == null || (card = this.mUiccController.getUiccCard(getPhoneId())) == null) {
            return false;
        }
        boolean status = card.setOperatorBrandOverride(brand);
        if (status) {
            TelephonyManager.from(this.mContext).setSimOperatorNameForPhone(getPhoneId(), this.mSST.getServiceProviderName());
            this.mSST.pollState();
        }
        return status;
    }

    private String getOperatorNumeric() {
        String operatorNumeric = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                operatorNumeric = r.getOperatorNumeric();
            }
            if (!isCTSimCard(getPhoneId())) {
                return operatorNumeric;
            }
            Rlog.d(this.LOG_TAG, "sub2 is dobule mode card.");
            return SystemProperties.get("gsm.national_roaming.apn", "46003");
        } else if (isCTSimCard(getPhoneId())) {
            String operatorNumeric2 = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "46003");
            String str = this.LOG_TAG;
            Rlog.d(str, "select china telecom hplmn " + operatorNumeric2);
            return operatorNumeric2;
        } else {
            IccRecords curIccRecords = null;
            int i = this.mCdmaSubscriptionSource;
            if (i == 1) {
                operatorNumeric = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
            } else if (i == 0) {
                UiccCardApplication uiccCardApplication = (UiccCardApplication) this.mUiccApplication.get();
                if (uiccCardApplication == null || uiccCardApplication.getType() != IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
                    curIccRecords = this.mSimRecords;
                } else {
                    logd("Legacy RUIM app present");
                    curIccRecords = (IccRecords) this.mIccRecords.get();
                }
                if (curIccRecords == null || curIccRecords != this.mSimRecords) {
                    curIccRecords = (IccRecords) this.mIccRecords.get();
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
            return operatorNumeric;
        }
    }

    public String getCountryIso() {
        String country;
        SubscriptionInfo subInfo = SubscriptionManager.from(getContext()).getActiveSubscriptionInfo(getSubId());
        if (subInfo == null || (country = subInfo.getCountryIso()) == null) {
            return null;
        }
        return country.toUpperCase();
    }

    public void notifyEcbmTimerReset(Boolean flag) {
        this.mEcmTimerResetRegistrants.notifyResult(flag);
    }

    public int getCsCallRadioTech() {
        ServiceStateTracker serviceStateTracker = this.mSST;
        if (serviceStateTracker != null) {
            return getCsCallRadioTech(serviceStateTracker.mSS.getVoiceRegState(), this.mSST.mSS.getRilVoiceRadioTechnology());
        }
        return 0;
    }

    private int getCsCallRadioTech(int vrs, int vrat) {
        logd("getCsCallRadioTech, current vrs=" + vrs + ", vrat=" + vrat);
        int calcVrat = vrat;
        if (vrs != 0 || ArrayUtils.contains(VOICE_PS_CALL_RADIO_TECHNOLOGY, vrat)) {
            calcVrat = 0;
        }
        logd("getCsCallRadioTech, result calcVrat=" + calcVrat);
        return calcVrat;
    }

    private void onVoiceRegStateOrRatChanged(int vrs, int vrat) {
        logd("onVoiceRegStateOrRatChanged");
        this.mCT.dispatchCsCallRadioTech(getCsCallRadioTech(vrs, vrat));
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
        this.mEcmTimerResetRegistrants.addUnique(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForEcmTimerReset(Handler h) {
        this.mEcmTimerResetRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.Phone
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
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void logd(String s) {
        Rlog.i(this.LOG_TAG, s);
    }

    private void logi(String s) {
        String str = this.LOG_TAG;
        Rlog.i(str, "[" + this.mPhoneId + "] " + s);
    }

    @UnsupportedAppUsage
    private void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }

    @Override // com.android.internal.telephony.Phone
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

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void cleanDeviceId() {
        logd("cleanDeviceId");
        if (isPhoneTypeGsm()) {
            this.mHwGsmCdmaPhoneEx.cleanDeviceId();
        } else {
            this.mMeid = null;
        }
        this.mImei = null;
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.PhoneInternalInterface
    public String getCdmaMlplVersion() {
        return this.mHwGsmCdmaPhoneEx.getCdmaMlplVersion(this.mSST.mMlplVersion);
    }

    @Override // com.android.internal.telephony.Phone, com.android.internal.telephony.PhoneInternalInterface
    public String getCdmaMsplVersion() {
        return this.mHwGsmCdmaPhoneEx.getCdmaMsplVersion(this.mSST.mMsplVersion);
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
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult((Object) null, mmi, e));
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForCdmaWaitingNumberChanged(Handler h, int what, Object obj) {
        Rlog.i(this.LOG_TAG, "registerForCdmaWaitingNumberChanged");
        this.mCT.registerForCdmaWaitingNumberChanged(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForCdmaWaitingNumberChanged(Handler h) {
        Rlog.i(this.LOG_TAG, "unregisterForCdmaWaitingNumberChanged");
        this.mCT.unregisterForCdmaWaitingNumberChanged(h);
    }

    @Override // com.android.internal.telephony.Phone
    public int getLteOnCdmaMode() {
        int currentConfig = super.getLteOnCdmaMode();
        UiccCardApplication cdmaApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
        if (cdmaApplication != null && cdmaApplication.getType() == IccCardApplicationStatus.AppType.APPTYPE_RUIM && currentConfig == 1) {
            return 0;
        }
        return currentConfig;
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public String getOperatorNumericHw() {
        return getOperatorNumeric();
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public PhoneExt getGsmCdmaPhone() {
        PhoneExt phoneExt = new PhoneExt();
        phoneExt.setPhone(this);
        return phoneExt;
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public boolean isValidCommandInterfaceCFActionHw(int commandInterfaceCFAction) {
        return isValidCommandInterfaceCFAction(commandInterfaceCFAction);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public boolean isValidCommandInterfaceCFReasonHw(int commandInterfaceCFReason) {
        return isValidCommandInterfaceCFReason(commandInterfaceCFReason);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public boolean isCfEnableHw(int action) {
        return isCfEnable(action);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public PhoneExt getImsPhoneHw() {
        PhoneExt phoneExt = new PhoneExt();
        phoneExt.setPhone(this.mImsPhone);
        return phoneExt;
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public UiccCardApplicationEx getUiccApplicationHw() {
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication((UiccCardApplication) this.mUiccApplication.get());
        return uiccCardApplicationEx;
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void addPendingMMIsHw(MmiCodeExt mmiCode) {
        this.mPendingMMIs.add(mmiCode.getMmiCode());
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void notifyRegistrantsHw(Object uo, Object object, Throwable ex) {
        MmiCode mmiCode = null;
        if (object instanceof MmiCodeExt) {
            mmiCode = ((MmiCodeExt) object).getMmiCode();
        }
        if (mmiCode != null && (mmiCode instanceof GsmMmiCode)) {
            GsmMmiCode gsmMmiCode = (GsmMmiCode) mmiCode;
        }
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult(uo, mmiCode, ex));
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public boolean isPhoneInEcmState() {
        return this.mIsPhoneInEcmState;
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void setPhoneInEcmState(boolean phoneInEcmState) {
        this.mIsPhoneInEcmState = phoneInEcmState;
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void removeCallbacksHw() {
        removeCallbacks(this.mExitEcmRunnable);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void handleEcmExitRespRegistrant() {
        Registrant registrant = this.mEcmExitRespRegistrant;
        if (registrant != null) {
            registrant.notifyRegistrant(new AsyncResult((Object) null, 0, (Throwable) null));
        }
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void handleWakeLock() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void sendEmergencyCallbackModeChangeHw() {
        sendEmergencyCallbackModeChange();
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void phoneObjectUpdaterHw(int newVoiceRadioTech) {
        phoneObjectUpdater(newVoiceRadioTech);
    }

    private void autoExitEmergencyCallbackMode() {
        IHwGsmCdmaPhoneEx iHwGsmCdmaPhoneEx = this.mHwGsmCdmaPhoneEx;
        if (iHwGsmCdmaPhoneEx != null) {
            iHwGsmCdmaPhoneEx.autoExitEmergencyCallbackMode();
        }
    }

    public IHwGsmCdmaPhoneEx getHwGsmCdmaPhoneEx() {
        return this.mHwGsmCdmaPhoneEx;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTtyMode(int ttyMode) {
        logi(String.format("updateTtyMode ttyMode=%d", Integer.valueOf(ttyMode)));
        setTTYMode(telecomModeToPhoneMode(ttyMode), null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateUiTtyMode(int ttyMode) {
        logi(String.format("updateUiTtyMode ttyMode=%d", Integer.valueOf(ttyMode)));
        setUiTTYMode(telecomModeToPhoneMode(ttyMode), null);
    }

    private static int telecomModeToPhoneMode(int telecomMode) {
        if (telecomMode == 1 || telecomMode == 2 || telecomMode == 3) {
            return 1;
        }
        return 0;
    }

    private void loadTtyMode() {
        int ttyMode = 0;
        TelecomManager telecomManager = TelecomManager.from(this.mContext);
        if (telecomManager != null) {
            ttyMode = telecomManager.getCurrentTtyMode();
        }
        updateTtyMode(ttyMode);
        updateUiTtyMode(Settings.Secure.getInt(this.mContext.getContentResolver(), "preferred_tty_mode", 0));
    }

    @Override // com.android.internal.telephony.Phone
    public void dispose() {
        synchronized (Phone.lockForRadioTechnologyChange) {
            super.dispose();
            this.mHwGsmCdmaPhoneEx.dispose();
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void setNetworkSelectionModeAutomatic(Message response) {
        if (isPhoneTypeGsm()) {
            super.setNetworkSelectionModeAutomatic(response);
        } else if (isChinaTelecom(getPhoneId())) {
            Rlog.d(this.LOG_TAG, "setNetworkSelectionModeAutomatic: It can run in ChinaTelecom");
            super.setNetworkSelectionModeAutomatic(response);
        } else {
            this.mHwGsmCdmaPhoneEx.setNetworkSelectionModeAutomatic(response);
        }
    }

    @Override // com.android.internal.telephony.Phone
    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
        if (isPhoneTypeGsm()) {
            super.selectNetworkManually(network, persistSelection, response);
        } else if (isChinaTelecom(getPhoneId())) {
            Rlog.d(this.LOG_TAG, "selectNetworkManually: It can run in ChinaTelecom");
            super.selectNetworkManually(network, persistSelection, response);
        } else {
            this.mHwGsmCdmaPhoneEx.selectNetworkManually(response);
        }
    }

    public boolean isChinaTelecom(int slotId) {
        return this.mHwGsmCdmaPhoneEx.isChinaTelecom(slotId);
    }

    public void updateReduceSARState() {
        this.mHwGsmCdmaPhoneEx.updateReduceSARState();
    }

    public boolean isCTSimCard(int slotId) {
        return this.mHwGsmCdmaPhoneEx.isCTSimCard(slotId);
    }

    public void updateWfcMode(Context context, boolean roaming, int subId) throws ImsException {
        try {
            this.mHwGsmCdmaPhoneEx.updateWfcMode(context, roaming, subId);
        } catch (ImsExceptionExt e) {
            throw new ImsException(e.getMessage(), e.getCause(), e.getCode());
        }
    }

    public boolean isDualImsAvailable() {
        return this.mHwGsmCdmaPhoneEx.isDualImsAvailable();
    }

    public void globalEccCustom(String operatorNumeric) {
        this.mHwGsmCdmaPhoneEx.globalEccCustom(operatorNumeric);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void gsmCdmaPhoneSwitchVoiceCallBackgroundState(int state) {
        this.mCT.switchVoiceCallBackgroundState(state);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void gsmCdmaPhoneRegisterForLineControlInfo(Handler h, int what, Object obj) {
        this.mCT.registerForLineControlInfo(h, what, obj);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void gsmCdmaPhoneUnRegisterForLineControlInfo(Handler h) {
        this.mCT.unregisterForLineControlInfo(h);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public void notifyEmergencyCallRegistrantsHw(boolean started) {
        notifyEmergencyCallRegistrants(started);
    }

    @Override // com.android.internal.telephony.IHwGsmCdmaPhoneInner
    public boolean isPhoneTypeCdmaLteHw() {
        return isPhoneTypeCdmaLte();
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public boolean isSupportCFT() {
        return this.mHwGsmCdmaPhoneEx.isSupportCFT();
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        this.mHwGsmCdmaPhoneEx.setCallForwardingUncondTimerOption(startHour, startMinute, endHour, endMinute, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, onComplete);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setImsSwitch(boolean on) {
        this.mHwGsmCdmaPhoneEx.setImsSwitch(on);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public boolean getImsSwitch() {
        return this.mHwGsmCdmaPhoneEx.getImsSwitch();
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public String getPesn() {
        return this.mHwGsmCdmaPhoneEx.getPesn();
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getCallbarringOption(String facility, String serviceClass, Message response) {
        this.mHwGsmCdmaPhoneEx.getCallbarringOption(facility, serviceClass, response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setCallbarringOption(String facility, String password, boolean isActivate, String serviceClass, Message response) {
        this.mHwGsmCdmaPhoneEx.setCallbarringOption(facility, password, isActivate, serviceClass, response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getCallbarringOption(String facility, int serviceClass, Message response) {
        this.mHwGsmCdmaPhoneEx.getCallbarringOption(facility, serviceClass, response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setCallbarringOption(String facility, String password, boolean isActivate, int serviceClass, Message response) {
        this.mHwGsmCdmaPhoneEx.setCallbarringOption(facility, password, isActivate, serviceClass, response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void changeBarringPassword(String oldPassword, String newPassword, Message response) {
        this.mHwGsmCdmaPhoneEx.changeBarringPassword(oldPassword, newPassword, response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void closeRrc() {
        this.mHwGsmCdmaPhoneEx.closeRrc();
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void switchVoiceCallBackgroundState(int state) {
        this.mHwGsmCdmaPhoneEx.switchVoiceCallBackgroundState(state);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getPOLCapabilty(Message response) {
        this.mHwGsmCdmaPhoneEx.getPOLCapabilty(response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getPreferedOperatorList(Message response) {
        this.mHwGsmCdmaPhoneEx.getPreferedOperatorList(response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        this.mHwGsmCdmaPhoneEx.setPOLEntry(index, numeric, nAct, response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void riseCdmaCutoffFreq(boolean on) {
        this.mHwGsmCdmaPhoneEx.riseCdmaCutoffFreq(on);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setLTEReleaseVersion(int state, Message response) {
        this.mHwGsmCdmaPhoneEx.setLTEReleaseVersion(state, response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public int getLteReleaseVersion() {
        return this.mHwGsmCdmaPhoneEx.getLteReleaseVersion();
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public boolean setISMCOEX(String setISMCoex) {
        return this.mHwGsmCdmaPhoneEx.setISMCOEX(setISMCoex);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void setImsDomainConfig(int domainType) {
        this.mHwGsmCdmaPhoneEx.setImsDomainConfig(domainType);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void getImsDomain(Message response) {
        this.mHwGsmCdmaPhoneEx.getImsDomain(response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void handleUiccAuth(int authType, byte[] rand, byte[] auth, Message response) {
        this.mHwGsmCdmaPhoneEx.handleUiccAuth(authType, rand, auth, response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void handleMapconImsaReq(byte[] Msg) {
        this.mHwGsmCdmaPhoneEx.handleMapconImsaReq(Msg);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase, com.android.internal.telephony.AbstractPhoneInternalInterface
    public void selectCsgNetworkManually(Message response) {
        this.mHwGsmCdmaPhoneEx.selectCsgNetworkManually(response);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void registerForHWBuffer(Handler h, int what, Object obj) {
        this.mHwGsmCdmaPhoneEx.registerForHWBuffer(h, what, obj);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void unregisterForHWBuffer(Handler h) {
        this.mHwGsmCdmaPhoneEx.unregisterForHWBuffer(h);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void sendHWSolicited(Message reqMsg, int event, byte[] reqData) {
        this.mHwGsmCdmaPhoneEx.sendHWSolicited(reqMsg, event, reqData);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return this.mHwGsmCdmaPhoneEx.cmdForECInfo(event, action, buf);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void registerForCallAltSrv(Handler h, int what, Object obj) {
        this.mHwGsmCdmaPhoneEx.registerForCallAltSrv(h, what, obj);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void unregisterForCallAltSrv(Handler h) {
        this.mHwGsmCdmaPhoneEx.unregisterForCallAltSrv(h);
    }

    @Override // com.android.internal.telephony.AbstractPhoneBase
    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        this.mHwGsmCdmaPhoneEx.notifyCellularCommParaReady(paratype, pathtype, response);
    }

    public boolean isMmiCode(String dialString, UiccCardApplication app) {
        UiccCardApplicationEx appEx = new UiccCardApplicationEx();
        appEx.setUiccCardApplication(app);
        return this.mHwGsmCdmaPhoneEx.isMmiCode(dialString, appEx);
    }

    public String getHwCdmaEsn() {
        return this.mHwGsmCdmaPhoneEx.getHwCdmaEsn();
    }

    @Override // com.android.internal.telephony.Phone
    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mHwGsmCdmaPhoneEx.registerForLineControlInfo(h, what, obj);
    }

    @Override // com.android.internal.telephony.Phone
    public void unregisterForLineControlInfo(Handler h) {
        this.mHwGsmCdmaPhoneEx.unregisterForLineControlInfo(h);
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        this.mHwGsmCdmaPhoneEx.judgeToLaunchCsgPeriodicSearchTimer();
    }

    public static Object getCfu(String dialingNumber, Message onComplete) {
        return new Cfu(dialingNumber, onComplete);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void startUploadAvailableNetworks(Object obj) {
        if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
            this.mHwGsmCdmaPhoneEx.startUploadAvailableNetworks(obj);
        } else {
            loge("startUploadAvailableNetworks: not possible in CDMA");
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void stopUploadAvailableNetworks() {
        if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
            this.mHwGsmCdmaPhoneEx.stopUploadAvailableNetworks();
        } else {
            loge("stopUploadAvailableNetworks: not possible in CDMA");
        }
    }
}
