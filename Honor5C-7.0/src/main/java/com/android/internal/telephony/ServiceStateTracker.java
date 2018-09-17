package com.android.internal.telephony;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.AsyncResult;
import android.os.BaseBundle;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.SettingsEx.Systemex;
import android.provider.Telephony.CellBroadcasts;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Pair;
import android.util.TimeUtils;
import android.view.Display;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.gsm.HwCustGsmServiceStateTracker;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.google.android.mms.pdu.CharacterSets;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceStateTracker extends Handler {
    private static final /* synthetic */ int[] -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues = null;
    private static final String ACTION_COMMFORCE = "android.intent.action.COMMFORCE";
    private static final String ACTION_RADIO_OFF = "android.intent.action.ACTION_RADIO_OFF";
    private static final String ACTION_TIMEZONE_SELECTION = "android.intent.action.ACTION_TIMEZONE_SELECTION";
    private static final boolean CLEAR_NITZ_WHEN_REG = false;
    public static final int CS_DISABLED = 1004;
    public static final int CS_EMERGENCY_ENABLED = 1006;
    public static final int CS_ENABLED = 1003;
    public static final int CS_NORMAL_ENABLED = 1005;
    public static final int CS_NOTIFICATION = 999;
    protected static final boolean DBG = true;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60000;
    public static final String DEFAULT_MNC = "00";
    private static final int DELAY_TIME_TO_NOTIF_NITZ = 60000;
    protected static final int EVENT_ALL_DATA_DISCONNECTED = 49;
    protected static final int EVENT_CDMA_PRL_VERSION_CHANGED = 40;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 39;
    protected static final int EVENT_CHANGE_IMS_STATE = 45;
    protected static final int EVENT_CHECK_REPORT_GPRS = 22;
    protected static final int EVENT_ERI_FILE_LOADED = 36;
    protected static final int EVENT_GET_AD_DONE = 1002;
    protected static final int EVENT_GET_CELL_INFO_LIST = 43;
    protected static final int EVENT_GET_LOC_DONE = 15;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE = 19;
    protected static final int EVENT_GET_SIGNAL_STRENGTH = 3;
    public static final int EVENT_ICC_CHANGED = 42;
    protected static final int EVENT_IMS_CAPABILITY_CHANGED = 48;
    protected static final int EVENT_IMS_STATE_CHANGED = 46;
    protected static final int EVENT_IMS_STATE_DONE = 47;
    protected static final int EVENT_LOCATION_UPDATES_ENABLED = 18;
    protected static final int EVENT_NETWORK_STATE_CHANGED = 2;
    private static final int EVENT_NITZ_CAPABILITY_NOTIFICATION = 100;
    protected static final int EVENT_NITZ_TIME = 11;
    protected static final int EVENT_NV_READY = 35;
    protected static final int EVENT_OTA_PROVISION_STATUS_CHANGE = 37;
    protected static final int EVENT_PHONE_TYPE_SWITCHED = 50;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH = 10;
    protected static final int EVENT_POLL_STATE_CDMA_SUBSCRIPTION = 34;
    protected static final int EVENT_POLL_STATE_GPRS = 5;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_POLL_STATE_OPERATOR = 6;
    protected static final int EVENT_POLL_STATE_REGISTRATION = 4;
    protected static final int EVENT_RADIO_AVAILABLE = 13;
    protected static final int EVENT_RADIO_ON = 41;
    protected static final int EVENT_RADIO_STATE_CHANGED = 1;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE = 21;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED = 23;
    protected static final int EVENT_RUIM_READY = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 27;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE = 20;
    protected static final int EVENT_SET_RADIO_POWER_OFF = 38;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE = 12;
    protected static final int EVENT_SIM_READY = 17;
    protected static final int EVENT_SIM_RECORDS_LOADED = 16;
    protected static final int EVENT_UNSOL_CELL_INFO_LIST = 44;
    private static final String EXTRA_SHOW_WIFI = "showWifi";
    private static final String EXTRA_WIFI = "wifi";
    private static final boolean FEATURE_DELAY_UPDATE_SIGANL_STENGTH = false;
    private static final boolean FEATURE_RECOVER_AUTO_NETWORK_MODE = false;
    protected static final String[] GMT_COUNTRY_CODES = null;
    protected static final boolean HW_FAST_SET_RADIO_OFF = false;
    private static final int HW_OPTA = 0;
    private static final int HW_OPTB = 0;
    private static final boolean IGNORE_GOOGLE_NON_ROAMING = true;
    public static final String INVALID_MCC = "000";
    public static final boolean ISDEMO = false;
    private static final long LAST_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    private static final int MAX_NITZ_YEAR = 2037;
    private static final boolean MDOEM_WORK_MODE_IS_SRLTE = false;
    public static final int MS_PER_HOUR = 3600000;
    public static final int NITZ_UPDATE_DIFF_DEFAULT = 2000;
    public static final int NITZ_UPDATE_SPACING_DEFAULT = 600000;
    public static final int OTASP_NEEDED = 2;
    public static final int OTASP_NOT_NEEDED = 3;
    public static final int OTASP_SIM_UNPROVISIONED = 5;
    public static final int OTASP_UNINITIALIZED = 0;
    public static final int OTASP_UNKNOWN = 1;
    private static final String PERMISSION_COMM_FORCE = "android.permission.COMM_FORCE";
    private static boolean PLUS_TRANFER_IN_MDOEM = false;
    private static final int POLL_PERIOD_MILLIS = 20000;
    protected static final String PROP_FORCE_ROAMING = "telephony.test.forceRoaming";
    protected static final int PS_CS = 1;
    public static final int PS_DISABLED = 1002;
    public static final int PS_ENABLED = 1001;
    public static final int PS_NOTIFICATION = 888;
    protected static final int PS_ONLY = 0;
    protected static final String REGISTRATION_DENIED_AUTH = "Authentication Failure";
    protected static final String REGISTRATION_DENIED_GEN = "General";
    protected static final boolean RESET_PROFILE = false;
    protected static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    public static final String UNACTIVATED_MIN2_VALUE = "000000";
    public static final String UNACTIVATED_MIN_VALUE = "1111110111";
    protected static final boolean VDBG = false;
    public static final String WAKELOCK_TAG = "ServiceStateTracker";
    private static String data;
    protected static final boolean display_blank_ons = false;
    private String LOG_TAG;
    protected boolean isCurrent3GPsCsAllowed;
    private boolean mAlarmSwitch;
    protected RegistrantList mAttachedRegistrants;
    private ContentObserver mAutoTimeObserver;
    private ContentObserver mAutoTimeZoneObserver;
    private RegistrantList mCdmaForSubscriptionInfoReadyRegistrants;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public CellLocation mCellLoc;
    protected CommandsInterface mCi;
    private ContentResolver mCr;
    private String mCurDataSpn;
    private String mCurPlmn;
    private boolean mCurShowPlmn;
    private boolean mCurShowSpn;
    private boolean mCurShowWifi;
    private String mCurSpn;
    private String mCurWifi;
    private String mCurrentCarrier;
    private int mCurrentOtaspMode;
    private RegistrantList mDataRegStateOrRatChangedRegistrants;
    private boolean mDataRoaming;
    private RegistrantList mDataRoamingOffRegistrants;
    private RegistrantList mDataRoamingOnRegistrants;
    private Display mDefaultDisplay;
    private int mDefaultDisplayState;
    private int mDefaultRoamingIndicator;
    protected boolean mDesiredPowerState;
    protected RegistrantList mDetachedRegistrants;
    private boolean mDeviceShuttingDown;
    private final DisplayListener mDisplayListener;
    private boolean mDoRecoveryMarker;
    protected boolean mDontPollSignalStrength;
    private boolean mEmergencyOnly;
    private TelephonyEventLog mEventLog;
    private boolean mGotCountryCode;
    private boolean mGsmRoaming;
    private HbpcdUtils mHbpcdUtils;
    private int[] mHomeNetworkId;
    private int[] mHomeSystemId;
    private HwCustGsmServiceStateTracker mHwCustGsmServiceStateTracker;
    protected IccRecords mIccRecords;
    private boolean mImsRegistered;
    private boolean mImsRegistrationOnOff;
    private BroadcastReceiver mIntentReceiver;
    private boolean mIsEriTextLoaded;
    private boolean mIsInPrl;
    private boolean mIsMinInfoReady;
    private boolean mIsSubscriptionFromRuim;
    private boolean mKeepNwSelManual;
    private List<CellInfo> mLastCellInfoList;
    private long mLastCellInfoListTime;
    long mLastReceivedNITZReferenceTime;
    private SignalStrength mLastSignalStrength;
    private CellIdentityLte mLasteCellIdentityLte;
    private int mMaxDataCalls;
    private String mMdn;
    private String mMin;
    protected String mMlplVersion;
    protected String mMsplVersion;
    private boolean mNeedFixZoneAfterNitz;
    private RegistrantList mNetworkAttachedRegistrants;
    private CellIdentityLte mNewCellIdentityLte;
    private CellLocation mNewCellLoc;
    private int mNewMaxDataCalls;
    private int mNewReasonDataDenied;
    protected ServiceState mNewSS;
    private int mNitzUpdateDiff;
    private int mNitzUpdateSpacing;
    private boolean mNitzUpdatedTime;
    private Notification mNotification;
    private final SstSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    private boolean mPendingRadioPowerOffAfterDataOff;
    private int mPendingRadioPowerOffAfterDataOffTag;
    private GsmCdmaPhone mPhone;
    protected int[] mPollingContext;
    private boolean mPowerOffDelayNeed;
    private int mPreferredNetworkType;
    private String mPrlVersion;
    private RegistrantList mPsRestrictDisabledRegistrants;
    private RegistrantList mPsRestrictEnabledRegistrants;
    private boolean mRadioOffByDoRecovery;
    private PendingIntent mRadioOffIntent;
    private int mReasonDataDenied;
    private boolean mRecoverAutoSelectMode;
    private String mRegistrationDeniedReason;
    private int mRegistrationState;
    private boolean mReportedGprsNoReg;
    public RestrictedState mRestrictedState;
    private int mRoamingIndicator;
    public ServiceState mSS;
    private long mSavedAtTime;
    private long mSavedTime;
    private String mSavedTimeZone;
    protected SignalStrength mSignalStrength;
    private boolean mSimCardsLoaded;
    private boolean mSpnUpdatePending;
    private boolean mStartedGprsRegCheck;
    private int mSubId;
    private SubscriptionController mSubscriptionController;
    private SubscriptionManager mSubscriptionManager;
    protected UiccCardApplication mUiccApplcation;
    private UiccController mUiccController;
    protected boolean mVoiceCapable;
    private RegistrantList mVoiceRoamingOffRegistrants;
    private RegistrantList mVoiceRoamingOnRegistrants;
    private WakeLock mWakeLock;
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;
    private boolean mZoneDst;
    private int mZoneOffset;
    private long mZoneTime;

    /* renamed from: com.android.internal.telephony.ServiceStateTracker.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            Rlog.i(ServiceStateTracker.this.LOG_TAG, "Auto time state changed");
            ServiceStateTracker.this.revertToNitzTime();
        }
    }

    /* renamed from: com.android.internal.telephony.ServiceStateTracker.2 */
    class AnonymousClass2 extends ContentObserver {
        AnonymousClass2(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            Rlog.i(ServiceStateTracker.this.LOG_TAG, "Auto time zone state changed");
            if (ServiceStateTracker.this.updateTimeByNitz()) {
                ServiceStateTracker.this.revertToNitzTimeZone();
            } else {
                ServiceStateTracker.this.updateTimeZoneNoneNitz();
            }
        }
    }

    private class CellInfoResult {
        List<CellInfo> list;
        Object lockObj;

        private CellInfoResult() {
            this.lockObj = new Object();
        }
    }

    public class SstDisplayListener implements DisplayListener {
        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                int oldState = ServiceStateTracker.this.mDefaultDisplayState;
                ServiceStateTracker.this.mDefaultDisplayState = ServiceStateTracker.this.mDefaultDisplay.getState();
                if (ServiceStateTracker.this.mDefaultDisplayState != oldState && ServiceStateTracker.this.mDefaultDisplayState == ServiceStateTracker.OTASP_NEEDED && !ServiceStateTracker.this.mDontPollSignalStrength) {
                    ServiceStateTracker.this.sendMessage(ServiceStateTracker.this.obtainMessage(ServiceStateTracker.EVENT_POLL_SIGNAL_STRENGTH));
                }
            }
        }
    }

    private class SstSubscriptionsChangedListener extends OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        private SstSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            ServiceStateTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            int subId = ServiceStateTracker.this.mPhone.getSubId();
            if (this.mPreviousSubId.getAndSet(subId) != subId && SubscriptionManager.isValidSubscriptionId(subId)) {
                Context context = ServiceStateTracker.this.mPhone.getContext();
                ServiceStateTracker.this.mPhone.notifyPhoneStateChanged();
                ServiceStateTracker.this.mPhone.notifyCallForwardingIndicator();
                ServiceStateTracker.this.mPhone.sendSubscriptionSettings(context.getResources().getBoolean(17956963) ? ServiceStateTracker.VDBG : ServiceStateTracker.IGNORE_GOOGLE_NON_ROAMING);
                ServiceStateTracker.this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(ServiceStateTracker.this.mSS.getRilDataRadioTechnology()));
                if (ServiceStateTracker.this.mSpnUpdatePending) {
                    ServiceStateTracker.this.mSubscriptionController.setPlmnSpn(ServiceStateTracker.this.mPhone.getPhoneId(), ServiceStateTracker.this.mCurShowPlmn, ServiceStateTracker.this.mCurPlmn, ServiceStateTracker.this.mCurShowSpn, ServiceStateTracker.this.mCurSpn);
                    ServiceStateTracker.this.mSpnUpdatePending = ServiceStateTracker.VDBG;
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                String oldNetworkSelection = sp.getString(Phone.NETWORK_SELECTION_KEY, "");
                String oldNetworkSelectionName = sp.getString(Phone.NETWORK_SELECTION_NAME_KEY, "");
                String oldNetworkSelectionShort = sp.getString(Phone.NETWORK_SELECTION_SHORT_KEY, "");
                if (!(TextUtils.isEmpty(oldNetworkSelection) && TextUtils.isEmpty(oldNetworkSelectionName) && TextUtils.isEmpty(oldNetworkSelectionShort))) {
                    Editor editor = sp.edit();
                    editor.putString(Phone.NETWORK_SELECTION_KEY + subId, oldNetworkSelection);
                    editor.putString(Phone.NETWORK_SELECTION_NAME_KEY + subId, oldNetworkSelectionName);
                    editor.putString(Phone.NETWORK_SELECTION_SHORT_KEY + subId, oldNetworkSelectionShort);
                    editor.remove(Phone.NETWORK_SELECTION_KEY);
                    editor.remove(Phone.NETWORK_SELECTION_NAME_KEY);
                    editor.remove(Phone.NETWORK_SELECTION_SHORT_KEY);
                    editor.commit();
                }
                ServiceStateTracker.this.updateSpnDisplay();
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues() {
        if (-com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues != null) {
            return -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues;
        }
        int[] iArr = new int[RadioState.values().length];
        try {
            iArr[RadioState.RADIO_OFF.ordinal()] = PS_CS;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RadioState.RADIO_ON.ordinal()] = OTASP_NOT_NEEDED;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RadioState.RADIO_UNAVAILABLE.ordinal()] = OTASP_NEEDED;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.ServiceStateTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.ServiceStateTracker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.<clinit>():void");
    }

    private void updateTimeZoneNoneNitz() {
        if (Global.getInt(this.mPhone.getContext().getContentResolver(), "auto_time_zone", PS_ONLY) != 0) {
            String iso = "";
            String mcc = "";
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (operatorNumeric == null || TextUtils.isEmpty(operatorNumeric)) {
                log("updateTimeZoneNoneNitz:operatorNumeric is null");
                return;
            }
            try {
                mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
            } catch (NumberFormatException ex) {
                loge("updateTimeZoneNoneNitz: countryCodeForMcc error" + ex);
            } catch (StringIndexOutOfBoundsException ex2) {
                loge("updateTimeZoneNoneNitz: countryCodeForMcc error" + ex2);
            }
            if (!(iso == null || TextUtils.isEmpty(iso))) {
                TimeZone zone = null;
                ArrayList<TimeZone> uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                TimeZone defaultZones = getTimeZoneFromMcc(mcc);
                if (uniqueZones.size() == PS_CS || defaultZones != null) {
                    zone = (TimeZone) uniqueZones.get(PS_ONLY);
                    if (defaultZones != null) {
                        zone = defaultZones;
                        log("some countrys has more than two timezone, choose a default");
                    }
                } else {
                    log("uniqueZones more than one");
                }
                if (zone != null) {
                    String zoneId = zone.getID();
                    log("zoneId:" + zoneId);
                    setAndBroadcastNetworkSetTimeZone(zoneId);
                }
            }
        }
    }

    private boolean updateTimeByNitz() {
        return !this.mNitzUpdatedTime ? Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", PS_ONLY) > 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG : IGNORE_GOOGLE_NON_ROAMING;
    }

    public ServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        this.LOG_TAG = WAKELOCK_TAG;
        this.mUiccController = null;
        this.mUiccApplcation = null;
        this.mIccRecords = null;
        this.mLastCellInfoList = null;
        this.mDontPollSignalStrength = VDBG;
        this.mVoiceRoamingOnRegistrants = new RegistrantList();
        this.mVoiceRoamingOffRegistrants = new RegistrantList();
        this.mDataRoamingOnRegistrants = new RegistrantList();
        this.mDataRoamingOffRegistrants = new RegistrantList();
        this.mAttachedRegistrants = new RegistrantList();
        this.mDetachedRegistrants = new RegistrantList();
        this.mDataRegStateOrRatChangedRegistrants = new RegistrantList();
        this.mNetworkAttachedRegistrants = new RegistrantList();
        this.mPsRestrictEnabledRegistrants = new RegistrantList();
        this.mPsRestrictDisabledRegistrants = new RegistrantList();
        this.mPendingRadioPowerOffAfterDataOff = VDBG;
        this.mPendingRadioPowerOffAfterDataOffTag = PS_ONLY;
        this.mSimCardsLoaded = VDBG;
        this.isCurrent3GPsCsAllowed = IGNORE_GOOGLE_NON_ROAMING;
        this.mRadioOffByDoRecovery = VDBG;
        this.mDoRecoveryMarker = VDBG;
        this.mWakeLock = null;
        this.mImsRegistrationOnOff = VDBG;
        this.mAlarmSwitch = VDBG;
        this.mRadioOffIntent = null;
        this.mPowerOffDelayNeed = IGNORE_GOOGLE_NON_ROAMING;
        this.mDeviceShuttingDown = VDBG;
        this.mSpnUpdatePending = VDBG;
        this.mCurSpn = null;
        this.mCurDataSpn = null;
        this.mCurPlmn = null;
        this.mCurShowPlmn = VDBG;
        this.mCurShowSpn = VDBG;
        this.mSubId = -1;
        this.mCurShowWifi = VDBG;
        this.mCurWifi = null;
        this.mImsRegistered = VDBG;
        this.mDefaultDisplayState = PS_ONLY;
        this.mDisplayListener = new SstDisplayListener();
        this.mOnSubscriptionsChangedListener = new SstSubscriptionsChangedListener();
        this.mNeedFixZoneAfterNitz = VDBG;
        this.mGotCountryCode = VDBG;
        this.mAutoTimeObserver = new AnonymousClass1(new Handler());
        this.mAutoTimeZoneObserver = new AnonymousClass2(new Handler());
        this.mMaxDataCalls = PS_CS;
        this.mNewMaxDataCalls = PS_CS;
        this.mReasonDataDenied = -1;
        this.mNewReasonDataDenied = -1;
        this.mGsmRoaming = VDBG;
        this.mDataRoaming = VDBG;
        this.mEmergencyOnly = VDBG;
        this.mNitzUpdatedTime = VDBG;
        this.mKeepNwSelManual = SystemProperties.getBoolean("ro.config.hw_keep_sel_manual", VDBG);
        this.mRecoverAutoSelectMode = VDBG;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ServiceStateTracker.this.mPhone.isPhoneTypeGsm()) {
                    if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                        ServiceStateTracker.this.updateSpnDisplay();
                    } else if (intent.getAction().equals(ServiceStateTracker.ACTION_RADIO_OFF)) {
                        ServiceStateTracker.this.mAlarmSwitch = ServiceStateTracker.VDBG;
                        ServiceStateTracker.this.powerOffRadioSafely(ServiceStateTracker.this.mPhone.mDcTracker);
                    } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction()) || "android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction()) || ServiceStateTracker.ACTION_COMMFORCE.equals(intent.getAction()) || "android.intent.action.HEADSET_PLUG".equals(intent.getAction()) || "android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                        ServiceStateTracker.this.mPhone.updateReduceSARState();
                    }
                    return;
                }
                ServiceStateTracker.this.loge("Ignoring intent " + intent + " received on CDMA phone");
            }
        };
        this.mCurrentOtaspMode = PS_ONLY;
        this.mNitzUpdateSpacing = SystemProperties.getInt("ro.nitz_update_spacing", NITZ_UPDATE_SPACING_DEFAULT);
        this.mNitzUpdateDiff = SystemProperties.getInt("ro.nitz_update_diff", NITZ_UPDATE_DIFF_DEFAULT);
        this.mRegistrationState = -1;
        this.mCdmaForSubscriptionInfoReadyRegistrants = new RegistrantList();
        this.mHomeSystemId = null;
        this.mHomeNetworkId = null;
        this.mIsMinInfoReady = VDBG;
        this.mIsEriTextLoaded = VDBG;
        this.mIsSubscriptionFromRuim = VDBG;
        this.mHbpcdUtils = null;
        this.mCurrentCarrier = null;
        this.mNewCellIdentityLte = new CellIdentityLte();
        this.mLasteCellIdentityLte = new CellIdentityLte();
        this.mLastSignalStrength = null;
        initOnce(phone, ci);
        updatePhoneType();
        initDisplay();
    }

    private void initDisplay() {
        DisplayManager dm = (DisplayManager) this.mPhone.getContext().getSystemService("display");
        dm.registerDisplayListener(this.mDisplayListener, null);
        this.mDefaultDisplay = dm.getDisplay(PS_ONLY);
    }

    private void initOnce(GsmCdmaPhone phone, CommandsInterface ci) {
        boolean z;
        this.mPhone = phone;
        this.LOG_TAG += "[SUB" + this.mPhone.getPhoneId() + "]";
        this.mCi = ci;
        this.mVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17956956);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, EVENT_ICC_CHANGED, null);
        this.mCi.setOnSignalStrengthUpdate(this, EVENT_SIGNAL_STRENGTH_UPDATE, null);
        this.mCi.registerForCellInfoList(this, EVENT_UNSOL_CELL_INFO_LIST, null);
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mSubscriptionManager = SubscriptionManager.from(phone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mCi.registerForImsNetworkStateChanged(this, EVENT_IMS_STATE_CHANGED, null);
        this.mWakeLock = ((PowerManager) phone.getContext().getSystemService("power")).newWakeLock(PS_CS, WAKELOCK_TAG);
        this.mCi.registerForRadioStateChanged(this, PS_CS, null);
        this.mCi.registerForVoiceNetworkStateChanged(this, OTASP_NEEDED, null);
        this.mCi.setOnNITZTime(this, EVENT_NITZ_TIME, null);
        this.mCr = phone.getContext().getContentResolver();
        int airplaneMode = Global.getInt(this.mCr, "airplane_mode_on", PS_ONLY);
        if (Global.getInt(this.mCr, "enable_cellular_on_boot", PS_CS) <= 0 || airplaneMode > 0) {
            z = VDBG;
        } else {
            z = IGNORE_GOOGLE_NON_ROAMING;
        }
        this.mDesiredPowerState = z;
        this.mCr.registerContentObserver(Global.getUriFor("auto_time"), IGNORE_GOOGLE_NON_ROAMING, this.mAutoTimeObserver);
        this.mCr.registerContentObserver(Global.getUriFor("auto_time_zone"), IGNORE_GOOGLE_NON_ROAMING, this.mAutoTimeZoneObserver);
        setSignalStrengthDefaultValues();
        data = Systemex.getString(this.mCr, "enable_get_location");
        this.mCi.getSignalStrength(obtainMessage(OTASP_NOT_NEEDED));
        Context context = this.mPhone.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(ACTION_RADIO_OFF);
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.intent.action.HEADSET_PLUG");
        filter.addAction("android.intent.action.PHONE_STATE");
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(ACTION_COMMFORCE);
        context.registerReceiver(this.mIntentReceiver, filter, PERMISSION_COMM_FORCE, null);
        Object[] objArr = new Object[PS_CS];
        objArr[PS_ONLY] = this.mPhone;
        this.mHwCustGsmServiceStateTracker = (HwCustGsmServiceStateTracker) HwCustUtils.createObj(HwCustGsmServiceStateTracker.class, objArr);
        this.mEventLog = new TelephonyEventLog(this.mPhone.getPhoneId());
        this.mPhone.notifyOtaspChanged(PS_ONLY);
    }

    public void updatePhoneType() {
        this.mSS = new ServiceState();
        this.mNewSS = new ServiceState();
        this.mLastCellInfoListTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new SignalStrength();
        this.mRestrictedState = new RestrictedState();
        this.mStartedGprsRegCheck = VDBG;
        this.mReportedGprsNoReg = VDBG;
        this.mMdn = null;
        this.mMin = null;
        this.mPrlVersion = null;
        this.mIsMinInfoReady = VDBG;
        this.mNitzUpdatedTime = VDBG;
        cancelPollState();
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mCdmaSSM != null) {
                this.mCdmaSSM.dispose(this);
            }
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mPhone.unregisterForEriFileLoaded(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
            this.mCellLoc = new GsmCellLocation();
            this.mNewCellLoc = new GsmCellLocation();
            this.mCi.registerForAvailable(this, EVENT_RADIO_AVAILABLE, null);
            this.mCi.setOnRestrictedStateChanged(this, EVENT_RESTRICTED_STATE_CHANGED, null);
        } else {
            this.mCi.unregisterForAvailable(this);
            this.mCi.unSetOnRestrictedStateChanged(this);
            if (this.mPhone.isPhoneTypeCdmaLte()) {
                this.mPhone.registerForSimRecordsLoaded(this, EVENT_SIM_RECORDS_LOADED, null);
            }
            this.mCellLoc = new CdmaCellLocation();
            this.mNewCellLoc = new CdmaCellLocation();
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED, null);
            this.mIsSubscriptionFromRuim = this.mCdmaSSM.getCdmaSubscriptionSource() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
            this.mCi.registerForCdmaPrlChanged(this, EVENT_CDMA_PRL_VERSION_CHANGED, null);
            this.mPhone.registerForEriFileLoaded(this, EVENT_ERI_FILE_LOADED, null);
            this.mCi.registerForCdmaOtaProvision(this, EVENT_OTA_PROVISION_STATUS_CHANGE, null);
            this.mHbpcdUtils = new HbpcdUtils(this.mPhone.getContext());
            updateOtaspState();
        }
        onUpdateIccAvailability();
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(PS_ONLY));
        this.mCi.getSignalStrength(obtainMessage(OTASP_NOT_NEEDED));
        sendMessage(obtainMessage(EVENT_PHONE_TYPE_SWITCHED));
    }

    public void requestShutdown() {
        if (!this.mDeviceShuttingDown) {
            this.mDeviceShuttingDown = IGNORE_GOOGLE_NON_ROAMING;
            this.mDesiredPowerState = VDBG;
            setPowerStateToDesired();
        }
    }

    public void dispose() {
        this.mCi.unSetOnSignalStrengthUpdate(this);
        this.mUiccController.unregisterForIccChanged(this);
        this.mCi.unregisterForCellInfoList(this);
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mCi.unregisterForImsNetworkStateChanged(this);
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.unregisterForGetAdDone(this);
        }
        unregisterForRuimEvents();
        HwTelephonyFactory.getHwNetworkManager().dispose(this);
    }

    public boolean getDesiredPowerState() {
        return this.mDesiredPowerState;
    }

    public void setDesiredPowerState(boolean dps) {
        log("setDesiredPowerState, dps = " + dps);
        this.mDesiredPowerState = dps;
    }

    protected boolean notifySignalStrength() {
        if (this.mSignalStrength.equals(this.mLastSignalStrength)) {
            return VDBG;
        }
        try {
            this.mPhone.notifySignalStrength();
            return IGNORE_GOOGLE_NON_ROAMING;
        } catch (NullPointerException ex) {
            loge("updateSignalStrength() Phone already destroyed: " + ex + "SignalStrength not notified");
            return VDBG;
        }
    }

    protected void notifyDataRegStateRilRadioTechnologyChanged() {
        int rat = this.mSS.getRilDataRadioTechnology();
        int drs = this.mSS.getDataRegState();
        log("notifyDataRegStateRilRadioTechnologyChanged: drs=" + drs + " rat=" + rat);
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(rat));
        this.mDataRegStateOrRatChangedRegistrants.notifyResult(new Pair(Integer.valueOf(drs), Integer.valueOf(rat)));
    }

    protected void useDataRegStateForDataOnlyDevices() {
        if (!this.mVoiceCapable) {
            log("useDataRegStateForDataOnlyDevice: VoiceRegState=" + this.mNewSS.getVoiceRegState() + " DataRegState=" + this.mNewSS.getDataRegState());
            this.mNewSS.setVoiceRegState(this.mNewSS.getDataRegState());
        }
    }

    protected void updatePhoneObject() {
        boolean isRegistered = IGNORE_GOOGLE_NON_ROAMING;
        if (this.mPhone.getContext().getResources().getBoolean(17957018)) {
            if (!(this.mSS.getVoiceRegState() == 0 || this.mSS.getVoiceRegState() == OTASP_NEEDED)) {
                isRegistered = VDBG;
            }
            if (isRegistered) {
                this.mPhone.updatePhoneObject(this.mSS.getRilVoiceRadioTechnology());
            } else {
                log("updatePhoneObject: Ignore update");
            }
        }
    }

    public void registerForVoiceRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOnRegistrants.add(r);
        if (this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOn(Handler h) {
        this.mVoiceRoamingOnRegistrants.remove(h);
    }

    public void registerForVoiceRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOffRegistrants.add(r);
        if (!this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOff(Handler h) {
        this.mVoiceRoamingOffRegistrants.remove(h);
    }

    public void registerForDataRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOnRegistrants.add(r);
        if (this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOn(Handler h) {
        this.mDataRoamingOnRegistrants.remove(h);
    }

    public void registerForDataRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOffRegistrants.add(r);
        if (!this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOff(Handler h) {
        this.mDataRoamingOffRegistrants.remove(h);
    }

    public void reRegisterNetwork(Message onComplete) {
        int i = PS_CS;
        if (HwModemCapability.isCapabilitySupport(7) && this.mPhone.isPhoneTypeGsm()) {
            int i2;
            log("modem support rettach, rettach");
            CommandsInterface commandsInterface = this.mCi;
            if (EVENT_POLL_STATE_NETWORK_SELECTION_MODE == this.mSS.getRilDataRadioTechnology()) {
                i2 = PS_CS;
            } else {
                i2 = PS_ONLY;
            }
            commandsInterface.dataConnectionDetach(i2, null);
            CommandsInterface commandsInterface2 = this.mCi;
            if (EVENT_POLL_STATE_NETWORK_SELECTION_MODE != this.mSS.getRilDataRadioTechnology()) {
                i = PS_ONLY;
            }
            commandsInterface2.dataConnectionAttach(i, null);
            return;
        }
        log("modem not support rettach, reRegisterNetwork");
        this.mCi.getPreferredNetworkType(obtainMessage(EVENT_GET_PREFERRED_NETWORK_TYPE, onComplete));
    }

    public void setRadioPower(boolean power) {
        this.mDesiredPowerState = power;
        setPowerStateToDesired();
    }

    protected void setPowerStateToDesired(boolean power, Message msg) {
        if (Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", PS_ONLY) <= 0) {
            this.mDesiredPowerState = power;
        }
        log("mDesiredPowerState = " + this.mDesiredPowerState);
        getCaller();
        this.mCi.setRadioPower(this.mDesiredPowerState, msg);
    }

    public void setRadioPower(boolean power, Message msg) {
        setPowerStateToDesired(power, msg);
    }

    public void enableSingleLocationUpdate() {
        String appName = null;
        if (!(this.mPhone == null || this.mPhone.getContext() == null || this.mPhone.getContext().getPackageManager() == null)) {
            appName = this.mPhone.getContext().getPackageManager().getNameForUid(Binder.getCallingUid());
        }
        boolean containPackage = isContainPackage(data, appName);
        if ((!HwTelephonyFactory.getHwNetworkManager().isCustScreenOff(this.mPhone) || containPackage) && !this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantSingleLocationUpdate = IGNORE_GOOGLE_NON_ROAMING;
            this.mCi.setLocationUpdates(IGNORE_GOOGLE_NON_ROAMING, obtainMessage(EVENT_LOCATION_UPDATES_ENABLED));
        }
    }

    public void enableLocationUpdates() {
        if (!HwTelephonyFactory.getHwNetworkManager().isCustScreenOff(this.mPhone) && !this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantContinuousLocationUpdates = IGNORE_GOOGLE_NON_ROAMING;
            this.mCi.setLocationUpdates(IGNORE_GOOGLE_NON_ROAMING, obtainMessage(EVENT_LOCATION_UPDATES_ENABLED));
        }
    }

    protected void disableSingleLocationUpdate() {
        this.mWantSingleLocationUpdate = VDBG;
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mCi.setLocationUpdates(VDBG, null);
        }
    }

    public void disableLocationUpdates() {
        this.mWantContinuousLocationUpdates = VDBG;
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mCi.setLocationUpdates(VDBG, null);
        }
    }

    public void handleMessage(Message msg) {
        if (this.mHwCustGsmServiceStateTracker == null || !this.mHwCustGsmServiceStateTracker.handleMessage(msg)) {
            AsyncResult ar;
            switch (msg.what) {
                case PS_CS /*1*/:
                case EVENT_PHONE_TYPE_SWITCHED /*50*/:
                    if (!this.mDesiredPowerState || !HwTelephonyFactory.getHwUiccManager().uiccHwdsdsNeedSetActiveMode() || this.mDoRecoveryMarker) {
                        setDoRecoveryMarker(VDBG);
                        if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == RadioState.RADIO_ON) {
                            handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                            queueNextSignalStrengthPoll();
                        }
                        setPowerStateToDesired();
                        pollState();
                        break;
                    }
                    log("CdmaSerive/Gsmserive tracker need wait SetActiveMode ");
                    break;
                    break;
                case OTASP_NEEDED /*2*/:
                    modemTriggeredPollState();
                    break;
                case OTASP_NOT_NEEDED /*3*/:
                    if (this.mCi.getRadioState().isOn()) {
                        onSignalStrengthResult((AsyncResult) msg.obj);
                        queueNextSignalStrengthPoll();
                        break;
                    }
                    return;
                case EVENT_POLL_STATE_REGISTRATION /*4*/:
                case OTASP_SIM_UNPROVISIONED /*5*/:
                case EVENT_POLL_STATE_OPERATOR /*6*/:
                    handlePollStateResult(msg.what, (AsyncResult) msg.obj);
                    break;
                case EVENT_POLL_SIGNAL_STRENGTH /*10*/:
                    this.mCi.getSignalStrength(obtainMessage(OTASP_NOT_NEEDED));
                    break;
                case EVENT_NITZ_TIME /*11*/:
                    if (HwTelephonyFactory.getHwNetworkManager().needGsmUpdateNITZTime(this, this.mPhone)) {
                        ar = (AsyncResult) msg.obj;
                        String nitzString = ((Object[]) ar.result)[PS_ONLY];
                        long nitzReceiveTime = ((Long) ((Object[]) ar.result)[PS_CS]).longValue();
                        this.mLastReceivedNITZReferenceTime = nitzReceiveTime;
                        setTimeFromNITZString(nitzString, nitzReceiveTime);
                        break;
                    }
                    return;
                case EVENT_SIGNAL_STRENGTH_UPDATE /*12*/:
                    ar = (AsyncResult) msg.obj;
                    this.mDontPollSignalStrength = IGNORE_GOOGLE_NON_ROAMING;
                    onSignalStrengthResult(ar);
                    break;
                case EVENT_RADIO_AVAILABLE /*13*/:
                    break;
                case EVENT_POLL_STATE_NETWORK_SELECTION_MODE /*14*/:
                    log("EVENT_POLL_STATE_NETWORK_SELECTION_MODE");
                    ar = (AsyncResult) msg.obj;
                    if (!this.mPhone.isPhoneTypeGsm()) {
                        if (ar.exception == null && ar.result != null) {
                            if (ar.result[PS_ONLY] == PS_CS) {
                                this.mPhone.setNetworkSelectionModeAutomatic(null);
                                break;
                            }
                        }
                        log("Unable to getNetworkSelectionMode");
                        break;
                    }
                    handlePollStateResult(msg.what, ar);
                    break;
                    break;
                case EVENT_GET_LOC_DONE /*15*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        String[] states = (String[]) ar.result;
                        if (this.mPhone.isPhoneTypeGsm()) {
                            int lac = -1;
                            int cid = -1;
                            if (states.length >= OTASP_NOT_NEEDED) {
                                try {
                                    if (states[PS_CS] != null && states[PS_CS].length() > 0) {
                                        lac = Integer.parseInt(states[PS_CS], EVENT_SIM_RECORDS_LOADED);
                                    }
                                    if (states[OTASP_NEEDED] != null && states[OTASP_NEEDED].length() > 0) {
                                        cid = Integer.parseInt(states[OTASP_NEEDED], EVENT_SIM_RECORDS_LOADED);
                                    }
                                } catch (NumberFormatException ex) {
                                    Rlog.w(this.LOG_TAG, "error parsing location: " + ex);
                                }
                            }
                            ((GsmCellLocation) this.mCellLoc).setLacAndCid(lac, cid);
                        } else {
                            int baseStationId = -1;
                            int baseStationLatitude = Integer.MAX_VALUE;
                            int baseStationLongitude = Integer.MAX_VALUE;
                            int systemId = -1;
                            int networkId = -1;
                            if (states.length > 9) {
                                try {
                                    if (states[EVENT_POLL_STATE_REGISTRATION] != null) {
                                        baseStationId = Integer.parseInt(states[EVENT_POLL_STATE_REGISTRATION]);
                                    }
                                    if (states[OTASP_SIM_UNPROVISIONED] != null) {
                                        baseStationLatitude = Integer.parseInt(states[OTASP_SIM_UNPROVISIONED]);
                                    }
                                    if (states[EVENT_POLL_STATE_OPERATOR] != null) {
                                        baseStationLongitude = Integer.parseInt(states[EVENT_POLL_STATE_OPERATOR]);
                                    }
                                    if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                                        baseStationLatitude = Integer.MAX_VALUE;
                                        baseStationLongitude = Integer.MAX_VALUE;
                                    }
                                    if (states[8] != null) {
                                        systemId = Integer.parseInt(states[8]);
                                    }
                                    if (states[9] != null) {
                                        networkId = Integer.parseInt(states[9]);
                                    }
                                } catch (NumberFormatException ex2) {
                                    loge("error parsing cell location data: " + ex2);
                                }
                            }
                            ((CdmaCellLocation) this.mCellLoc).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
                        }
                        this.mPhone.notifyLocationChanged();
                    }
                    disableSingleLocationUpdate();
                    break;
                case EVENT_SIM_RECORDS_LOADED /*16*/:
                    log("EVENT_SIM_RECORDS_LOADED: what=" + msg.what);
                    this.mSimCardsLoaded = IGNORE_GOOGLE_NON_ROAMING;
                    updatePhoneObject();
                    updateOtaspState();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        updateSpnDisplay();
                        if (this.mHwCustGsmServiceStateTracker != null) {
                            this.mHwCustGsmServiceStateTracker.updateRomingVoicemailNumber(this.mSS);
                            break;
                        }
                    }
                    break;
                case EVENT_SIM_READY /*17*/:
                    this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
                    this.mSimCardsLoaded = VDBG;
                    log("skip setPreferredNetworkType when EVENT_SIM_READY");
                    boolean skipRestoringSelection = this.mPhone.getContext().getResources().getBoolean(17956963);
                    if (FEATURE_RECOVER_AUTO_NETWORK_MODE) {
                        log("Feature recover network mode automatic is on..");
                        this.mRecoverAutoSelectMode = IGNORE_GOOGLE_NON_ROAMING;
                    } else if (!skipRestoringSelection) {
                        if (!HwModemCapability.isCapabilitySupport(EVENT_POLL_STATE_REGISTRATION) || this.mKeepNwSelManual) {
                            this.mPhone.restoreSavedNetworkSelection(null);
                        } else {
                            log("Modem can select network auto with manual mode");
                        }
                    }
                    pollState();
                    queueNextSignalStrengthPoll();
                    break;
                case EVENT_LOCATION_UPDATES_ENABLED /*18*/:
                    if (((AsyncResult) msg.obj).exception == null) {
                        this.mCi.getVoiceRegistrationState(obtainMessage(EVENT_GET_LOC_DONE, null));
                        break;
                    }
                    break;
                case EVENT_GET_PREFERRED_NETWORK_TYPE /*19*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mPreferredNetworkType = ((int[]) ar.result)[PS_ONLY];
                    } else {
                        this.mPreferredNetworkType = 7;
                    }
                    this.mCi.setPreferredNetworkType(7, obtainMessage(EVENT_SET_PREFERRED_NETWORK_TYPE, ar.userObj));
                    break;
                case EVENT_SET_PREFERRED_NETWORK_TYPE /*20*/:
                    this.mCi.setPreferredNetworkType(this.mPreferredNetworkType, obtainMessage(EVENT_RESET_PREFERRED_NETWORK_TYPE, ((AsyncResult) msg.obj).userObj));
                    break;
                case EVENT_RESET_PREFERRED_NETWORK_TYPE /*21*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.userObj != null) {
                        AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                        ((Message) ar.userObj).sendToTarget();
                        break;
                    }
                    break;
                case EVENT_CHECK_REPORT_GPRS /*22*/:
                    if (this.mPhone.isPhoneTypeGsm() && this.mSS != null) {
                        if (!isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                            GsmCellLocation loc = (GsmCellLocation) this.mPhone.getCellLocation();
                            String[] strArr = new Object[OTASP_NEEDED];
                            strArr[PS_ONLY] = this.mSS.getOperatorNumeric();
                            strArr[PS_CS] = Integer.valueOf(loc != null ? loc.getCid() : -1);
                            EventLog.writeEvent(EventLogTags.DATA_NETWORK_REGISTRATION_FAIL, strArr);
                            this.mReportedGprsNoReg = IGNORE_GOOGLE_NON_ROAMING;
                        }
                    }
                    this.mStartedGprsRegCheck = VDBG;
                    break;
                case EVENT_RESTRICTED_STATE_CHANGED /*23*/:
                    if (this.mPhone.isPhoneTypeGsm()) {
                        log("EVENT_RESTRICTED_STATE_CHANGED");
                        onRestrictedStateChanged((AsyncResult) msg.obj);
                        break;
                    }
                    break;
                case EVENT_RUIM_READY /*26*/:
                    if (this.mPhone.getLteOnCdmaMode() == PS_CS) {
                        log("Receive EVENT_RUIM_READY");
                        pollState();
                    } else {
                        log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
                        getSubscriptionInfoAndStartPollingThreads();
                    }
                    this.mCi.getNetworkSelectionMode(obtainMessage(EVENT_POLL_STATE_NETWORK_SELECTION_MODE));
                    break;
                case EVENT_RUIM_RECORDS_LOADED /*27*/:
                    if (!this.mPhone.isPhoneTypeGsm()) {
                        log("EVENT_RUIM_RECORDS_LOADED: what=" + msg.what);
                        updatePhoneObject();
                        if (!this.mPhone.isPhoneTypeCdma()) {
                            RuimRecords ruim = (RuimRecords) this.mIccRecords;
                            if (ruim != null) {
                                if (ruim.isProvisioned()) {
                                    this.mMdn = ruim.getMdn();
                                    this.mMin = ruim.getMin();
                                    parseSidNid(ruim.getSid(), ruim.getNid());
                                    String prlVersion = ruim.getPrlVersion();
                                    if (prlVersion != null) {
                                        prlVersion = prlVersion.trim();
                                        if (!("".equals(prlVersion) || "65535".equals(prlVersion))) {
                                            this.mPrlVersion = prlVersion;
                                            SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                                        }
                                    }
                                    this.mIsMinInfoReady = IGNORE_GOOGLE_NON_ROAMING;
                                }
                                updateOtaspState();
                                notifyCdmaSubscriptionInfoReady();
                            }
                            pollState();
                            break;
                        }
                        updateSpnDisplay();
                        break;
                    }
                    break;
                case EVENT_POLL_STATE_CDMA_SUBSCRIPTION /*34*/:
                    if (!this.mPhone.isPhoneTypeGsm()) {
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            String[] cdmaSubscription = ar.result;
                            if (cdmaSubscription != null && cdmaSubscription.length >= OTASP_SIM_UNPROVISIONED) {
                                if (cdmaSubscription[PS_ONLY] != null) {
                                    this.mMdn = cdmaSubscription[PS_ONLY];
                                }
                                parseSidNid(cdmaSubscription[PS_CS], cdmaSubscription[OTASP_NEEDED]);
                                if (cdmaSubscription[OTASP_NOT_NEEDED] != null) {
                                    this.mMin = cdmaSubscription[OTASP_NOT_NEEDED];
                                }
                                if (cdmaSubscription[EVENT_POLL_STATE_REGISTRATION] != null) {
                                    this.mPrlVersion = cdmaSubscription[EVENT_POLL_STATE_REGISTRATION];
                                    SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                                }
                                log("GET_CDMA_SUBSCRIPTION: MDN=" + this.mMdn);
                                if (cdmaSubscription.length >= 7) {
                                    String str = this.mMlplVersion;
                                    log("GET_CDMA_SUBSCRIPTION: mMlplVersion=" + str + " mMsplVersion=" + this.mMsplVersion);
                                    this.mMlplVersion = cdmaSubscription[OTASP_SIM_UNPROVISIONED];
                                    this.mMsplVersion = cdmaSubscription[EVENT_POLL_STATE_OPERATOR];
                                }
                                this.mIsMinInfoReady = IGNORE_GOOGLE_NON_ROAMING;
                                updateOtaspState();
                                notifyCdmaSubscriptionInfoReady();
                                if (!this.mIsSubscriptionFromRuim && this.mIccRecords != null) {
                                    log("GET_CDMA_SUBSCRIPTION set imsi in mIccRecords");
                                    this.mIccRecords.setImsi(getImsi());
                                    break;
                                }
                                log("GET_CDMA_SUBSCRIPTION either mIccRecords is null or NV type device - not setting Imsi in mIccRecords");
                                break;
                            }
                            log("GET_CDMA_SUBSCRIPTION: error parsing cdmaSubscription params num=" + cdmaSubscription.length);
                            break;
                        }
                    }
                    break;
                case EVENT_NV_READY /*35*/:
                    updatePhoneObject();
                    this.mCi.getNetworkSelectionMode(obtainMessage(EVENT_POLL_STATE_NETWORK_SELECTION_MODE));
                    getSubscriptionInfoAndStartPollingThreads();
                    break;
                case EVENT_ERI_FILE_LOADED /*36*/:
                    log("ERI file has been loaded, repolling.");
                    pollState();
                    break;
                case EVENT_OTA_PROVISION_STATUS_CHANGE /*37*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        int otaStatus = ((int[]) ar.result)[PS_ONLY];
                        if (otaStatus == 8 || otaStatus == EVENT_POLL_SIGNAL_STRENGTH) {
                            log("EVENT_OTA_PROVISION_STATUS_CHANGE: Complete, Reload MDN");
                            this.mCi.getCDMASubscription(obtainMessage(EVENT_POLL_STATE_CDMA_SUBSCRIPTION));
                            break;
                        }
                    }
                    break;
                case EVENT_SET_RADIO_POWER_OFF /*38*/:
                    synchronized (this) {
                        int i;
                        if (this.mPendingRadioPowerOffAfterDataOff) {
                            int i2 = msg.arg1;
                            i = this.mPendingRadioPowerOffAfterDataOffTag;
                            if (i2 == r0) {
                                log("EVENT_SET_RADIO_OFF, turn radio off now.");
                                hangupAndPowerOff();
                                this.mPendingRadioPowerOffAfterDataOffTag += PS_CS;
                                this.mPendingRadioPowerOffAfterDataOff = VDBG;
                                releaseWakeLock();
                                break;
                            }
                        }
                        i = msg.arg1;
                        log("EVENT_SET_RADIO_OFF is stale arg1=" + i + "!= tag=" + this.mPendingRadioPowerOffAfterDataOffTag);
                        releaseWakeLock();
                        break;
                    }
                    break;
                case EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED /*39*/:
                    handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                    break;
                case EVENT_CDMA_PRL_VERSION_CHANGED /*40*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mPrlVersion = Integer.toString(((int[]) ar.result)[PS_ONLY]);
                        SystemProperties.set("persist.radio.hwprlversion", this.mPrlVersion);
                        break;
                    }
                    break;
                case EVENT_ICC_CHANGED /*42*/:
                    onUpdateIccAvailability();
                    break;
                case EVENT_GET_CELL_INFO_LIST /*43*/:
                    ar = msg.obj;
                    CellInfoResult result = ar.userObj;
                    synchronized (result.lockObj) {
                        if (ar.exception == null) {
                            result.list = (List) ar.result;
                            break;
                        } else {
                            log("EVENT_GET_CELL_INFO_LIST: error ret null, e=" + ar.exception);
                            result.list = null;
                        }
                        this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                        this.mLastCellInfoList = result.list;
                        result.lockObj.notify();
                        break;
                    }
                    break;
                case EVENT_UNSOL_CELL_INFO_LIST /*44*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        List<CellInfo> list = ar.result;
                        this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                        this.mLastCellInfoList = list;
                        this.mPhone.notifyCellInfo(list);
                        break;
                    }
                    log("EVENT_UNSOL_CELL_INFO_LIST: error ignoring, e=" + ar.exception);
                    break;
                case EVENT_CHANGE_IMS_STATE /*45*/:
                    log("EVENT_CHANGE_IMS_STATE:");
                    setPowerStateToDesired();
                    break;
                case EVENT_IMS_STATE_CHANGED /*46*/:
                    this.mCi.getImsRegistrationState(obtainMessage(EVENT_IMS_STATE_DONE));
                    break;
                case EVENT_IMS_STATE_DONE /*47*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mImsRegistered = ((int[]) ar.result)[PS_ONLY] == PS_CS ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                        break;
                    }
                    break;
                case EVENT_IMS_CAPABILITY_CHANGED /*48*/:
                    log("EVENT_IMS_CAPABILITY_CHANGED");
                    updateSpnDisplay();
                    break;
                case EVENT_ALL_DATA_DISCONNECTED /*49*/:
                    ProxyController.getInstance().unregisterForAllDataDisconnected(SubscriptionManager.getDefaultDataSubscriptionId(), this);
                    synchronized (this) {
                        if (!this.mPendingRadioPowerOffAfterDataOff) {
                            log("EVENT_ALL_DATA_DISCONNECTED is stale");
                            break;
                        }
                        log("EVENT_ALL_DATA_DISCONNECTED, turn radio off now.");
                        hangupAndPowerOff();
                        this.mPendingRadioPowerOffAfterDataOff = VDBG;
                        break;
                    }
                    break;
                case EVENT_NITZ_CAPABILITY_NOTIFICATION /*100*/:
                    log("[settimezone]EVENT_NITZ_CAPABILITY_NOTIFICATION");
                    sendTimeZoneSelectionNotification();
                    break;
                case PS_DISABLED /*1002*/:
                    if (this.mPollingContext[PS_ONLY] != 0) {
                        log("EVENT_GET_AD_DONE pollState working ,no need do again");
                        break;
                    }
                    log("EVENT_GET_AD_DONE pollState ");
                    pollState();
                    break;
                default:
                    log("Unhandled message with number: " + msg.what);
                    break;
            }
        }
    }

    public String getRplmn() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return HwTelephonyFactory.getHwNetworkManager().getGsmRplmn(this, this.mPhone);
        }
        return "";
    }

    public void setCurrent3GPsCsAllowed(boolean allowed) {
        log("setCurrent3GPsCsAllowed:" + allowed);
        this.isCurrent3GPsCsAllowed = allowed;
    }

    protected boolean isSidsAllZeros() {
        if (this.mHomeSystemId != null) {
            for (int i = PS_ONLY; i < this.mHomeSystemId.length; i += PS_CS) {
                if (this.mHomeSystemId[i] != 0) {
                    return VDBG;
                }
            }
        }
        return IGNORE_GOOGLE_NON_ROAMING;
    }

    private boolean isHomeSid(int sid) {
        if (this.mHomeSystemId != null) {
            for (int i = PS_ONLY; i < this.mHomeSystemId.length; i += PS_CS) {
                if (sid == this.mHomeSystemId[i]) {
                    return IGNORE_GOOGLE_NON_ROAMING;
                }
            }
        }
        return VDBG;
    }

    public String getMdnNumber() {
        return this.mMdn;
    }

    public String getCdmaMin() {
        return this.mMin;
    }

    public String getPrlVersion() {
        int subId = this.mPhone.getSubId();
        int simCardState = TelephonyManager.getDefault().getSimState(subId);
        if (OTASP_SIM_UNPROVISIONED == simCardState) {
            this.mPrlVersion = this.mCi.getHwPrlVersion();
        } else {
            this.mPrlVersion = ProxyController.MODEM_0;
        }
        Rlog.d(this.LOG_TAG, "getPrlVersion: prlVersion=" + this.mPrlVersion + ", subid=" + subId + ", simState=" + simCardState);
        return this.mPrlVersion;
    }

    public String getMlplVersion() {
        return this.mMlplVersion;
    }

    public String getMsplVersion() {
        return this.mMsplVersion;
    }

    public String getImsi() {
        String operatorNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (TextUtils.isEmpty(operatorNumeric) || getCdmaMin() == null) {
            return null;
        }
        return operatorNumeric + getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return this.mIsMinInfoReady;
    }

    public int getOtasp() {
        if (this.mPhone.isPhoneTypeGsm()) {
            log("getOtasp: otasp not needed for GSM");
            return OTASP_NOT_NEEDED;
        } else if (this.mIsSubscriptionFromRuim) {
            return OTASP_NOT_NEEDED;
        } else {
            int provisioningState;
            if (this.mMin == null || this.mMin.length() < EVENT_POLL_STATE_OPERATOR) {
                log("getOtasp: bad mMin='" + this.mMin + "'");
                provisioningState = PS_CS;
            } else if (this.mMin.equals(UNACTIVATED_MIN_VALUE) || this.mMin.substring(PS_ONLY, EVENT_POLL_STATE_OPERATOR).equals(UNACTIVATED_MIN2_VALUE) || SystemProperties.getBoolean("test_cdma_setup", VDBG)) {
                provisioningState = OTASP_NEEDED;
            } else {
                provisioningState = OTASP_NOT_NEEDED;
            }
            log("getOtasp: state=" + provisioningState);
            return provisioningState;
        }
    }

    protected void parseSidNid(String sidStr, String nidStr) {
        int i;
        if (sidStr != null) {
            String[] sid = sidStr.split(",");
            this.mHomeSystemId = new int[sid.length];
            for (i = PS_ONLY; i < sid.length; i += PS_CS) {
                try {
                    this.mHomeSystemId[i] = Integer.parseInt(sid[i]);
                } catch (NumberFormatException ex) {
                    loge("error parsing system id: " + ex);
                }
            }
        }
        log("CDMA_SUBSCRIPTION: SID=" + sidStr);
        if (nidStr != null) {
            String[] nid = nidStr.split(",");
            this.mHomeNetworkId = new int[nid.length];
            for (i = PS_ONLY; i < nid.length; i += PS_CS) {
                try {
                    this.mHomeNetworkId[i] = Integer.parseInt(nid[i]);
                } catch (NumberFormatException ex2) {
                    loge("CDMA_SUBSCRIPTION: error parsing network id: " + ex2);
                }
            }
        }
        log("CDMA_SUBSCRIPTION: NID=" + nidStr);
    }

    protected void updateOtaspState() {
        int otaspMode = getOtasp();
        int oldOtaspMode = this.mCurrentOtaspMode;
        this.mCurrentOtaspMode = otaspMode;
        if (oldOtaspMode != this.mCurrentOtaspMode) {
            log("updateOtaspState: call notifyOtaspChanged old otaspMode=" + oldOtaspMode + " new otaspMode=" + this.mCurrentOtaspMode);
            this.mPhone.notifyOtaspChanged(this.mCurrentOtaspMode);
        }
    }

    protected Phone getPhone() {
        return this.mPhone;
    }

    protected void handlePollStateResult(int what, AsyncResult ar) {
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                Error err = null;
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
                    return;
                } else if (err != Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                    loge("RIL implementation has returned an error where it must succeed" + ar.exception);
                }
            } else {
                try {
                    handlePollStateResultMessage(what, ar);
                } catch (RuntimeException ex) {
                    loge("Exception while polling service state. Probably malformed RIL response." + ex);
                }
            }
            int[] iArr = this.mPollingContext;
            iArr[PS_ONLY] = iArr[PS_ONLY] - 1;
            if (this.mPollingContext[PS_ONLY] == 0) {
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateRoamingState();
                    this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                } else {
                    boolean namMatch = VDBG;
                    if (!isSidsAllZeros() && isHomeSid(this.mNewSS.getSystemId())) {
                        namMatch = IGNORE_GOOGLE_NON_ROAMING;
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS));
                    }
                    boolean isVoiceInService = this.mNewSS.getVoiceRegState() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                    int dataRegType = this.mNewSS.getRilDataRadioTechnology();
                    if (isVoiceInService && ServiceState.isCdma(dataRegType)) {
                        this.mNewSS.setDataRoaming(this.mNewSS.getVoiceRoaming());
                    }
                    this.mNewSS.setCdmaDefaultRoamingIndicator(this.mDefaultRoamingIndicator);
                    this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                    boolean isPrlLoaded = IGNORE_GOOGLE_NON_ROAMING;
                    if (TextUtils.isEmpty(this.mPrlVersion)) {
                        isPrlLoaded = VDBG;
                    }
                    if (!isPrlLoaded || this.mNewSS.getRilVoiceRadioTechnology() == 0) {
                        log("Turn off roaming indicator if !isPrlLoaded or voice RAT is unknown");
                        this.mNewSS.setCdmaRoamingIndicator(PS_CS);
                    } else if (!isSidsAllZeros()) {
                        if (!namMatch && !this.mIsInPrl) {
                            this.mNewSS.setCdmaRoamingIndicator(this.mDefaultRoamingIndicator);
                        } else if (!namMatch || this.mIsInPrl) {
                            if (!namMatch && this.mIsInPrl) {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            } else if (this.mRoamingIndicator <= OTASP_NEEDED) {
                                this.mNewSS.setCdmaRoamingIndicator(PS_CS);
                            } else {
                                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                            }
                        } else if (this.mNewSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                            log("Turn off roaming indicator as voice is LTE");
                            this.mNewSS.setCdmaRoamingIndicator(PS_CS);
                        } else {
                            this.mNewSS.setCdmaRoamingIndicator(OTASP_NEEDED);
                        }
                    }
                    int roamingIndicator = this.mNewSS.getCdmaRoamingIndicator();
                    this.mNewSS.setCdmaEriIconIndex(this.mPhone.mEriManager.getCdmaEriIconIndex(roamingIndicator, this.mDefaultRoamingIndicator));
                    this.mNewSS.setCdmaEriIconMode(this.mPhone.mEriManager.getCdmaEriIconMode(roamingIndicator, this.mDefaultRoamingIndicator));
                    HwTelephonyFactory.getHwNetworkManager().updateCTRoaming(this, this.mPhone, this.mNewSS, this.mNewSS.getRoaming());
                    log("Set CDMA Roaming Indicator to: " + this.mNewSS.getCdmaRoamingIndicator() + ". voiceRoaming = " + this.mNewSS.getVoiceRoaming() + ". dataRoaming = " + this.mNewSS.getDataRoaming() + ", isPrlLoaded = " + isPrlLoaded + ". namMatch = " + namMatch + " , mIsInPrl = " + this.mIsInPrl + ", mRoamingIndicator = " + this.mRoamingIndicator + ", mDefaultRoamingIndicator= " + this.mDefaultRoamingIndicator);
                }
                pollStateDone();
            }
        }
    }

    private boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        return (!cdmaRoaming || isSameOperatorNameFromSimAndSS(s)) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
    }

    private boolean isAutoTime(int registrationState) {
        boolean z = IGNORE_GOOGLE_NON_ROAMING;
        if (PS_CS != registrationState && OTASP_SIM_UNPROVISIONED != registrationState) {
            return VDBG;
        }
        if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() != getPhoneId()) {
            z = VDBG;
        }
        return z;
    }

    void handlePollStateResultMessage(int what, AsyncResult ar) {
        String[] states;
        int type;
        int regState;
        switch (what) {
            case EVENT_POLL_STATE_REGISTRATION /*4*/:
                int cssIndicator;
                if (this.mPhone.isPhoneTypeGsm()) {
                    states = (String[]) ar.result;
                    int lac = -1;
                    int cid = -1;
                    type = PS_ONLY;
                    regState = EVENT_POLL_STATE_REGISTRATION;
                    int psc = -1;
                    cssIndicator = PS_ONLY;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[PS_ONLY]);
                            if (states.length >= OTASP_NOT_NEEDED) {
                                if (states[PS_CS] != null && states[PS_CS].length() > 0) {
                                    lac = Integer.parseInt(states[PS_CS], EVENT_SIM_RECORDS_LOADED);
                                }
                                if (states[OTASP_NEEDED] != null && states[OTASP_NEEDED].length() > 0) {
                                    cid = Integer.parseInt(states[OTASP_NEEDED], EVENT_SIM_RECORDS_LOADED);
                                }
                                if (states.length >= EVENT_POLL_STATE_REGISTRATION && states[OTASP_NOT_NEEDED] != null) {
                                    type = Integer.parseInt(states[OTASP_NOT_NEEDED]);
                                }
                            }
                            if (states.length >= 7 && states[7] != null) {
                                cssIndicator = Integer.parseInt(states[7]);
                            }
                            if (states.length > EVENT_POLL_STATE_NETWORK_SELECTION_MODE && states[EVENT_POLL_STATE_NETWORK_SELECTION_MODE] != null && states[EVENT_POLL_STATE_NETWORK_SELECTION_MODE].length() > 0) {
                                psc = Integer.parseInt(states[EVENT_POLL_STATE_NETWORK_SELECTION_MODE], EVENT_SIM_RECORDS_LOADED);
                            }
                        } catch (NumberFormatException ex) {
                            loge("error parsing RegistrationState: " + ex);
                        }
                    }
                    type = HwTelephonyFactory.getHwNetworkManager().getCARilRadioType(this, this.mPhone, type);
                    this.mGsmRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setVoiceRegState(regCodeToServiceState(regState));
                    this.mNewSS.setRilVoiceRadioTechnology(type);
                    this.mNewSS.setCssIndicator(cssIndicator);
                    HwTelephonyFactory.getHwNetworkManager().sendGsmRoamingIntentIfDenied(this, this.mPhone, regState, states);
                    boolean isVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17956956);
                    if ((regState == EVENT_RADIO_AVAILABLE || regState == EVENT_POLL_SIGNAL_STRENGTH || regState == EVENT_SIGNAL_STRENGTH_UPDATE || regState == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) && isVoiceCapable) {
                        this.mEmergencyOnly = IGNORE_GOOGLE_NON_ROAMING;
                    } else {
                        this.mEmergencyOnly = VDBG;
                    }
                    if (HwTelephonyFactory.getHwNetworkManager().isUpdateLacAndCid(this, this.mPhone, cid) || this.mHwCustGsmServiceStateTracker.isUpdateLacAndCidCust(this)) {
                        ((GsmCellLocation) this.mNewCellLoc).setLacAndCid(lac, cid);
                        ((GsmCellLocation) this.mNewCellLoc).setPsc(psc);
                        return;
                    }
                    return;
                }
                states = (String[]) ar.result;
                int registrationState = EVENT_POLL_STATE_REGISTRATION;
                int radioTechnology = -1;
                int baseStationId = -1;
                int baseStationLatitude = Integer.MAX_VALUE;
                int baseStationLongitude = Integer.MAX_VALUE;
                cssIndicator = PS_ONLY;
                int systemId = PS_ONLY;
                int networkId = PS_ONLY;
                int roamingIndicator = -1;
                int systemIsInPrl = PS_ONLY;
                int defaultRoamingIndicator = PS_ONLY;
                int reasonForDenial = PS_ONLY;
                if (states.length >= EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                    try {
                        if (states[PS_ONLY] != null) {
                            registrationState = Integer.parseInt(states[PS_ONLY]);
                        }
                        if (states[OTASP_NOT_NEEDED] != null) {
                            radioTechnology = Integer.parseInt(states[OTASP_NOT_NEEDED]);
                        }
                        if (states[EVENT_POLL_STATE_REGISTRATION] != null) {
                            baseStationId = Integer.parseInt(states[EVENT_POLL_STATE_REGISTRATION]);
                        }
                        if (states[OTASP_SIM_UNPROVISIONED] != null) {
                            baseStationLatitude = Integer.parseInt(states[OTASP_SIM_UNPROVISIONED]);
                        }
                        if (states[EVENT_POLL_STATE_OPERATOR] != null) {
                            baseStationLongitude = Integer.parseInt(states[EVENT_POLL_STATE_OPERATOR]);
                        }
                        if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                            baseStationLatitude = Integer.MAX_VALUE;
                            baseStationLongitude = Integer.MAX_VALUE;
                        }
                        if (states[7] != null) {
                            cssIndicator = Integer.parseInt(states[7]);
                        }
                        if (states[8] != null) {
                            systemId = Integer.parseInt(states[8]);
                        }
                        if (states[9] != null) {
                            networkId = Integer.parseInt(states[9]);
                        }
                        if (states[EVENT_POLL_SIGNAL_STRENGTH] != null) {
                            roamingIndicator = Integer.parseInt(states[EVENT_POLL_SIGNAL_STRENGTH]);
                        }
                        if (states[EVENT_NITZ_TIME] != null) {
                            systemIsInPrl = Integer.parseInt(states[EVENT_NITZ_TIME]);
                        }
                        if (states[EVENT_SIGNAL_STRENGTH_UPDATE] != null) {
                            defaultRoamingIndicator = Integer.parseInt(states[EVENT_SIGNAL_STRENGTH_UPDATE]);
                        }
                        if (states[EVENT_RADIO_AVAILABLE] != null) {
                            reasonForDenial = Integer.parseInt(states[EVENT_RADIO_AVAILABLE]);
                        }
                    } catch (NumberFormatException ex2) {
                        loge("EVENT_POLL_STATE_REGISTRATION_CDMA: error parsing: " + ex2);
                    }
                    radioTechnology = HwTelephonyFactory.getHwNetworkManager().getCARilRadioType(this, this.mPhone, radioTechnology);
                    this.mRegistrationState = registrationState;
                    boolean cdmaRoaming = (!regCodeIsRoaming(registrationState) || isRoamIndForHomeSystem(states[EVENT_POLL_SIGNAL_STRENGTH])) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
                    this.mNewSS.setVoiceRoaming(cdmaRoaming);
                    this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
                    this.mNewSS.setRilVoiceRadioTechnology(radioTechnology);
                    if (isAutoTime(registrationState)) {
                        HwTelephonyFactory.getHwNetworkManager().setAutoTimeAndZoneForCdma(this, this.mPhone, radioTechnology);
                    }
                    this.mNewSS.setCssIndicator(cssIndicator);
                    this.mNewSS.setSystemAndNetworkId(systemId, networkId);
                    this.mRoamingIndicator = roamingIndicator;
                    this.mIsInPrl = systemIsInPrl == 0 ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
                    this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                    ((CdmaCellLocation) this.mNewCellLoc).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
                    if (reasonForDenial == 0) {
                        this.mRegistrationDeniedReason = REGISTRATION_DENIED_GEN;
                    } else if (reasonForDenial == PS_CS) {
                        this.mRegistrationDeniedReason = REGISTRATION_DENIED_AUTH;
                    } else {
                        this.mRegistrationDeniedReason = "";
                    }
                    if (this.mRegistrationState == OTASP_NOT_NEEDED) {
                        log("Registration denied, " + this.mRegistrationDeniedReason);
                        return;
                    }
                    return;
                }
                throw new RuntimeException("Warning! Wrong number of parameters returned from RIL_REQUEST_REGISTRATION_STATE: expected 14 or more strings and got " + states.length + " strings");
            case OTASP_SIM_UNPROVISIONED /*5*/:
                int dataRegState;
                if (this.mPhone.isPhoneTypeGsm()) {
                    states = (String[]) ar.result;
                    type = PS_ONLY;
                    regState = EVENT_POLL_STATE_REGISTRATION;
                    this.mNewReasonDataDenied = -1;
                    this.mNewMaxDataCalls = PS_CS;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[PS_ONLY]);
                            if (states.length >= EVENT_POLL_STATE_REGISTRATION && states[OTASP_NOT_NEEDED] != null) {
                                type = Integer.parseInt(states[OTASP_NOT_NEEDED]);
                            }
                            if (states.length >= OTASP_SIM_UNPROVISIONED && regState == OTASP_NOT_NEEDED) {
                                this.mNewReasonDataDenied = Integer.parseInt(states[EVENT_POLL_STATE_REGISTRATION]);
                            }
                            if (states.length >= EVENT_POLL_STATE_OPERATOR) {
                                this.mNewMaxDataCalls = Integer.parseInt(states[OTASP_SIM_UNPROVISIONED]);
                            }
                        } catch (NumberFormatException ex22) {
                            loge("error parsing GprsRegistrationState: " + ex22);
                        }
                    }
                    type = HwTelephonyFactory.getHwNetworkManager().updateCAStatus(this, this.mPhone, type);
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setRilDataRadioTechnology(type);
                    if (this.mHwCustGsmServiceStateTracker != null) {
                        this.mHwCustGsmServiceStateTracker.setPsCell(this.mSS, (GsmCellLocation) this.mNewCellLoc, states);
                    }
                    log("handlPollStateResultMessage: GsmSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + type);
                } else if (this.mPhone.isPhoneTypeCdma()) {
                    states = (String[]) ar.result;
                    log("handlePollStateResultMessage: EVENT_POLL_STATE_GPRS states.length=" + states.length + " states=" + states);
                    regState = EVENT_POLL_STATE_REGISTRATION;
                    int dataRadioTechnology = PS_ONLY;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[PS_ONLY]);
                            if (states.length >= EVENT_POLL_STATE_REGISTRATION && states[OTASP_NOT_NEEDED] != null) {
                                dataRadioTechnology = Integer.parseInt(states[OTASP_NOT_NEEDED]);
                            }
                        } catch (NumberFormatException ex222) {
                            loge("handlePollStateResultMessage: error parsing GprsRegistrationState: " + ex222);
                        }
                    }
                    dataRadioTechnology = HwTelephonyFactory.getHwNetworkManager().updateCAStatus(this, this.mPhone, dataRadioTechnology);
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mNewSS.setRilDataRadioTechnology(dataRadioTechnology);
                    this.mNewSS.setDataRoaming(regCodeIsRoaming(regState));
                    log("handlPollStateResultMessage: cdma setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + dataRadioTechnology);
                } else {
                    states = (String[]) ar.result;
                    log("handlePollStateResultMessage: EVENT_POLL_STATE_GPRS states.length=" + states.length + " states=" + states);
                    int newDataRAT = PS_ONLY;
                    regState = -1;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[PS_ONLY]);
                            if (states.length >= EVENT_POLL_STATE_REGISTRATION && states[OTASP_NOT_NEEDED] != null) {
                                newDataRAT = Integer.parseInt(states[OTASP_NOT_NEEDED]);
                            }
                        } catch (NumberFormatException ex2222) {
                            loge("handlePollStateResultMessage: error parsing GprsRegistrationState: " + ex2222);
                        }
                    }
                    newDataRAT = HwTelephonyFactory.getHwNetworkManager().updateCAStatus(this, this.mPhone, newDataRAT);
                    int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                    if ((oldDataRAT != 0 || newDataRAT == 0) && !(ServiceState.isCdma(oldDataRAT) && newDataRAT == EVENT_POLL_STATE_NETWORK_SELECTION_MODE)) {
                        if (oldDataRAT == EVENT_POLL_STATE_NETWORK_SELECTION_MODE && ServiceState.isCdma(newDataRAT)) {
                        }
                        this.mNewSS.setRilDataRadioTechnology(newDataRAT);
                        dataRegState = regCodeToServiceState(regState);
                        this.mNewSS.setDataRegState(dataRegState);
                        this.mNewSS.setDataRoaming(regCodeIsRoaming(regState));
                        log("handlPollStateResultMessage: CdmaLteSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRAT);
                    }
                    this.mCi.getSignalStrength(obtainMessage(OTASP_NOT_NEEDED));
                    this.mNewSS.setRilDataRadioTechnology(newDataRAT);
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mNewSS.setDataRoaming(regCodeIsRoaming(regState));
                    log("handlPollStateResultMessage: CdmaLteSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRAT);
                }
            case EVENT_POLL_STATE_OPERATOR /*6*/:
                String[] opNames;
                String brandOverride;
                if (this.mPhone.isPhoneTypeGsm()) {
                    opNames = (String[]) ar.result;
                    if (opNames != null && opNames.length >= OTASP_NOT_NEEDED) {
                        brandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                        if (brandOverride != null) {
                            log("EVENT_POLL_STATE_OPERATOR: use brandOverride=" + brandOverride);
                            this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[OTASP_NEEDED]);
                            return;
                        }
                        this.mNewSS.setOperatorName(opNames[PS_ONLY], opNames[PS_CS], opNames[OTASP_NEEDED]);
                        return;
                    }
                    return;
                }
                opNames = (String[]) ar.result;
                if (opNames == null || opNames.length < OTASP_NOT_NEEDED) {
                    log("EVENT_POLL_STATE_OPERATOR_CDMA: error parsing opNames");
                    return;
                }
                if (opNames[OTASP_NEEDED] != null && opNames[OTASP_NEEDED].length() >= OTASP_SIM_UNPROVISIONED) {
                    if ("00000".equals(opNames[OTASP_NEEDED])) {
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        this.mNewSS.setOperatorName(opNames[PS_ONLY], opNames[PS_CS], opNames[OTASP_NEEDED]);
                    }
                    brandOverride = this.mUiccController.getUiccCard(getPhoneId()) == null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                    if (brandOverride == null) {
                        this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[OTASP_NEEDED]);
                        return;
                    } else {
                        this.mNewSS.setOperatorName(opNames[PS_ONLY], opNames[PS_CS], opNames[OTASP_NEEDED]);
                        return;
                    }
                }
                opNames[PS_ONLY] = null;
                opNames[PS_CS] = null;
                opNames[OTASP_NEEDED] = null;
                if (this.mIsSubscriptionFromRuim) {
                    if (this.mUiccController.getUiccCard(getPhoneId()) == null) {
                    }
                    if (brandOverride == null) {
                        this.mNewSS.setOperatorName(opNames[PS_ONLY], opNames[PS_CS], opNames[OTASP_NEEDED]);
                        return;
                    } else {
                        this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[OTASP_NEEDED]);
                        return;
                    }
                }
                this.mNewSS.setOperatorName(opNames[PS_ONLY], opNames[PS_CS], opNames[OTASP_NEEDED]);
            case EVENT_POLL_STATE_NETWORK_SELECTION_MODE /*14*/:
                int[] ints = (int[]) ar.result;
                this.mNewSS.setIsManualSelection(ints[PS_ONLY] == PS_CS ? IGNORE_GOOGLE_NON_ROAMING : VDBG);
                if (ints[PS_ONLY] == PS_CS && (!this.mPhone.isManualNetSelAllowed() || this.mRecoverAutoSelectMode)) {
                    this.mPhone.setNetworkSelectionModeAutomatic(null);
                    log(" Forcing Automatic Network Selection, manual selection is not allowed");
                    this.mRecoverAutoSelectMode = VDBG;
                } else if (this.mRecoverAutoSelectMode) {
                    this.mRecoverAutoSelectMode = VDBG;
                }
            default:
                loge("handlePollStateResultMessage: Unexpected RIL response received: " + what);
        }
    }

    private boolean isRoamIndForHomeSystem(String roamInd) {
        String[] homeRoamIndicators = this.mPhone.getContext().getResources().getStringArray(17236032);
        if (homeRoamIndicators == null) {
            return VDBG;
        }
        int length = homeRoamIndicators.length;
        for (int i = PS_ONLY; i < length; i += PS_CS) {
            if (homeRoamIndicators[i].equals(roamInd)) {
                return IGNORE_GOOGLE_NON_ROAMING;
            }
        }
        return VDBG;
    }

    protected void updateRoamingState() {
        CarrierConfigManager configLoader;
        PersistableBundle b;
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean roaming = !this.mGsmRoaming ? this.mDataRoaming : IGNORE_GOOGLE_NON_ROAMING;
            log("updateRoamingState: original roaming = " + roaming + " mGsmRoaming:" + this.mGsmRoaming + " mDataRoaming:" + this.mDataRoaming);
            if (checkForRoamingForIndianOperators(this.mNewSS)) {
                log("indian operator,skip");
            } else if (this.mGsmRoaming && !isOperatorConsideredRoaming(this.mNewSS) && (isSameNamedOperators(this.mNewSS) || isOperatorConsideredNonRoaming(this.mNewSS))) {
                roaming = VDBG;
                log("updateRoamingState: set roaming = false");
            }
            this.mNewSS.setDataRoamingFromRegistration(roaming);
            configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    if (alwaysOnHomeNetwork(b)) {
                        log("updateRoamingState: carrier config override always on home network");
                        roaming = VDBG;
                    } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = VDBG;
                    } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = IGNORE_GOOGLE_NON_ROAMING;
                    }
                } catch (Exception e) {
                    loge("updateRoamingState: unable to access carrier config service");
                }
            } else {
                log("updateRoamingState: no carrier config service available");
            }
            roaming = HwTelephonyFactory.getHwNetworkManager().getGsmRoamingState(this, this.mPhone, roaming);
            this.mNewSS.setVoiceRoaming(roaming);
            this.mNewSS.setDataRoaming(roaming);
            return;
        }
        this.mNewSS.setDataRoamingFromRegistration(this.mNewSS.getDataRoaming());
        configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configLoader != null) {
            try {
                b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                String systemId = Integer.toString(this.mNewSS.getSystemId());
                if (alwaysOnHomeNetwork(b)) {
                    log("updateRoamingState: carrier config override always on home network");
                    setRoamingOff();
                } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric()) || isNonRoamingInCdmaNetwork(b, systemId)) {
                    log("updateRoamingState: carrier config override set non-roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                    setRoamingOff();
                } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric()) || isRoamingInCdmaNetwork(b, systemId)) {
                    log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                    setRoamingOn();
                }
            } catch (Exception e2) {
                loge("updateRoamingState: unable to access carrier config service");
            }
        } else {
            log("updateRoamingState: no carrier config service available");
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, VDBG)) {
            this.mNewSS.setVoiceRoaming(IGNORE_GOOGLE_NON_ROAMING);
            this.mNewSS.setDataRoaming(IGNORE_GOOGLE_NON_ROAMING);
        }
    }

    private void setRoamingOn() {
        this.mNewSS.setVoiceRoaming(IGNORE_GOOGLE_NON_ROAMING);
        this.mNewSS.setDataRoaming(IGNORE_GOOGLE_NON_ROAMING);
        this.mNewSS.setCdmaEriIconIndex(PS_ONLY);
        this.mNewSS.setCdmaEriIconMode(PS_ONLY);
    }

    private void setRoamingOff() {
        this.mNewSS.setVoiceRoaming(VDBG);
        this.mNewSS.setDataRoaming(VDBG);
        this.mNewSS.setCdmaEriIconIndex(PS_CS);
    }

    public void updateSpnDisplay() {
        boolean showPlmn;
        String plmn;
        int subId;
        int[] subIds;
        if (this.mPhone.isPhoneTypeGsm()) {
            IccRecords iccRecords = this.mIccRecords;
            int rule = iccRecords != null ? iccRecords.getDisplayRule(this.mSS.getOperatorNumeric()) : PS_ONLY;
            int combinedRegState = HwTelephonyFactory.getHwNetworkManager().getGsmCombinedRegState(this, this.mPhone, this.mSS);
            if (combinedRegState == PS_CS || combinedRegState == OTASP_NEEDED) {
                showPlmn = IGNORE_GOOGLE_NON_ROAMING;
                if (this.mEmergencyOnly) {
                    plmn = Resources.getSystem().getText(17040036).toString();
                } else {
                    plmn = Resources.getSystem().getText(17040012).toString();
                }
                if (this.mHwCustGsmServiceStateTracker != null) {
                    plmn = this.mHwCustGsmServiceStateTracker.setEmergencyToNoService(this.mSS, plmn, this.mEmergencyOnly);
                }
                log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn + "'");
            } else if (combinedRegState == 0) {
                plmn = HwTelephonyFactory.getHwNetworkManager().getGsmPlmn(this, this.mPhone);
                showPlmn = !TextUtils.isEmpty(plmn) ? (rule & OTASP_NEEDED) == OTASP_NEEDED ? IGNORE_GOOGLE_NON_ROAMING : VDBG : VDBG;
            } else {
                showPlmn = IGNORE_GOOGLE_NON_ROAMING;
                plmn = Resources.getSystem().getText(17040012).toString();
                log("updateSpnDisplay: radio is off w/ showPlmn=" + IGNORE_GOOGLE_NON_ROAMING + " plmn=" + plmn);
            }
            String spn = iccRecords != null ? iccRecords.getServiceProviderName() : "";
            String dataSpn = spn;
            boolean showSpn = (combinedRegState != 0 || TextUtils.isEmpty(spn)) ? VDBG : (rule & PS_CS) == PS_CS ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
            if (!TextUtils.isEmpty(spn) && this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().isWifiCallingEnabled()) {
                String[] wfcSpnFormats = this.mPhone.getContext().getResources().getStringArray(17236067);
                int voiceIdx = PS_ONLY;
                int dataIdx = PS_ONLY;
                CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
                if (configLoader != null) {
                    try {
                        PersistableBundle b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                        if (b != null) {
                            voiceIdx = b.getInt("wfc_spn_format_idx_int");
                            dataIdx = b.getInt("wfc_data_spn_format_idx_int");
                        }
                    } catch (Exception e) {
                        loge("updateSpnDisplay: carrier config error: " + e);
                    }
                }
                String formatVoice = wfcSpnFormats[voiceIdx];
                String formatData = wfcSpnFormats[dataIdx];
                Object[] objArr = new Object[PS_CS];
                objArr[PS_ONLY] = spn.trim();
                dataSpn = String.format(formatData, objArr);
                showSpn = IGNORE_GOOGLE_NON_ROAMING;
                showPlmn = VDBG;
            } else if (this.mSS.getVoiceRegState() == OTASP_NOT_NEEDED || (showPlmn && TextUtils.equals(spn, plmn))) {
                spn = null;
                showSpn = VDBG;
            }
            OnsDisplayParams onsDispalyParams = HwTelephonyFactory.getHwNetworkManager().getGsmOnsDisplayParams(this, this.mPhone, showSpn, showPlmn, rule, plmn, spn);
            showSpn = onsDispalyParams.mShowSpn;
            showPlmn = onsDispalyParams.mShowPlmn;
            rule = onsDispalyParams.mRule;
            plmn = onsDispalyParams.mPlmn;
            spn = onsDispalyParams.mSpn;
            boolean showWifi = onsDispalyParams.mShowWifi;
            String wifi = onsDispalyParams.mWifi;
            subId = -1;
            subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
            if (subIds != null && subIds.length > 0) {
                subId = subIds[PS_ONLY];
            }
            boolean show_blank_ons = VDBG;
            if (this.mHwCustGsmServiceStateTracker != null && this.mHwCustGsmServiceStateTracker.isStopUpdateName(this.mSimCardsLoaded)) {
                show_blank_ons = IGNORE_GOOGLE_NON_ROAMING;
            }
            if ((display_blank_ons || show_blank_ons) && combinedRegState == 0) {
                log("In service , display blank ons for tracfone");
                plmn = " ";
                spn = " ";
                showPlmn = IGNORE_GOOGLE_NON_ROAMING;
                showSpn = VDBG;
            }
            if (this.mSubId == subId && showPlmn == this.mCurShowPlmn && showSpn == this.mCurShowSpn && TextUtils.equals(spn, this.mCurSpn)) {
                if (TextUtils.equals(dataSpn, this.mCurDataSpn) && TextUtils.equals(plmn, this.mCurPlmn) && showWifi == this.mCurShowWifi) {
                    if (TextUtils.equals(wifi, this.mCurWifi)) {
                        this.mSubId = subId;
                        this.mCurShowSpn = showSpn;
                        this.mCurShowPlmn = showPlmn;
                        this.mCurSpn = spn;
                        this.mCurDataSpn = dataSpn;
                        this.mCurPlmn = plmn;
                        this.mCurShowWifi = showWifi;
                        this.mCurWifi = wifi;
                        return;
                    }
                }
            }
            String str = "updateSpnDisplay: changed sending intent rule=" + rule + " showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s' subId='%d'";
            Object[] objArr2 = new Object[EVENT_POLL_STATE_OPERATOR];
            objArr2[PS_ONLY] = Boolean.valueOf(showPlmn);
            objArr2[PS_CS] = plmn;
            objArr2[OTASP_NEEDED] = Boolean.valueOf(showSpn);
            objArr2[OTASP_NOT_NEEDED] = spn;
            objArr2[EVENT_POLL_STATE_REGISTRATION] = dataSpn;
            objArr2[OTASP_SIM_UNPROVISIONED] = Integer.valueOf(subId);
            log(String.format(str, objArr2));
            updateOperatorProp();
            Intent intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
            intent.addFlags(536870912);
            intent.putExtra("showSpn", showSpn);
            intent.putExtra("spn", spn);
            intent.putExtra("spnData", dataSpn);
            intent.putExtra("showPlmn", showPlmn);
            intent.putExtra(CellBroadcasts.PLMN, plmn);
            intent.putExtra(EXTRA_SHOW_WIFI, showWifi);
            intent.putExtra(EXTRA_WIFI, wifi);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (!(this.mSS == null || this.mSS.getState() != 0 || this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, showSpn, spn))) {
                this.mSpnUpdatePending = IGNORE_GOOGLE_NON_ROAMING;
            }
            HwTelephonyFactory.getHwNetworkManager().sendGsmDualSimUpdateSpnIntent(this, this.mPhone, showSpn, spn, showPlmn, plmn);
            this.mSubId = subId;
            this.mCurShowSpn = showSpn;
            this.mCurShowPlmn = showPlmn;
            this.mCurSpn = spn;
            this.mCurDataSpn = dataSpn;
            this.mCurPlmn = plmn;
            this.mCurShowWifi = showWifi;
            this.mCurWifi = wifi;
            return;
        }
        plmn = HwTelephonyFactory.getHwNetworkManager().getCdmaPlmn(this, this.mPhone);
        showPlmn = plmn != null ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        subId = -1;
        subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
        if (subIds != null && subIds.length > 0) {
            subId = subIds[PS_ONLY];
        }
        if (display_blank_ons && (plmn != null || this.mSS.getState() == 0)) {
            log("In service , display blank ons for tracfone");
            plmn = " ";
        }
        if (!(this.mSubId == subId && TextUtils.equals(plmn, this.mCurPlmn))) {
            objArr2 = new Object[OTASP_NOT_NEEDED];
            objArr2[PS_ONLY] = Boolean.valueOf(showPlmn);
            objArr2[PS_CS] = plmn;
            objArr2[OTASP_NEEDED] = Integer.valueOf(subId);
            log(String.format("updateSpnDisplay: changed sending intent showPlmn='%b' plmn='%s' subId='%d'", objArr2));
            intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
            intent.addFlags(536870912);
            intent.putExtra("showSpn", VDBG);
            intent.putExtra("spn", "");
            intent.putExtra("showPlmn", showPlmn);
            intent.putExtra(CellBroadcasts.PLMN, plmn);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (this.mSS != null && this.mSS.getState() == 0) {
                if (!this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, VDBG, "")) {
                    this.mSpnUpdatePending = IGNORE_GOOGLE_NON_ROAMING;
                }
            }
            HwTelephonyFactory.getHwNetworkManager().sendCdmaDualSimUpdateSpnIntent(this, this.mPhone, VDBG, "", showPlmn, plmn);
        }
        updateOperatorProp();
        this.mSubId = subId;
        this.mCurShowSpn = VDBG;
        this.mCurShowPlmn = showPlmn;
        this.mCurSpn = "";
        this.mCurPlmn = plmn;
    }

    protected void setPowerStateToDesired() {
        getCaller();
        log("mDeviceShuttingDown=" + this.mDeviceShuttingDown + ", mDesiredPowerState=" + this.mDesiredPowerState + ", getRadioState=" + this.mCi.getRadioState() + ", mPowerOffDelayNeed=" + this.mPowerOffDelayNeed + ", mAlarmSwitch=" + this.mAlarmSwitch);
        if (ISDEMO) {
            this.mCi.setRadioPower(VDBG, null);
        }
        if (this.mPhone.isPhoneTypeGsm() && this.mAlarmSwitch) {
            log("mAlarmSwitch == true");
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = VDBG;
        }
        if (this.mDesiredPowerState && this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            if (this.mHwCustGsmServiceStateTracker != null) {
                this.mHwCustGsmServiceStateTracker.setRadioPower(this.mCi, IGNORE_GOOGLE_NON_ROAMING);
            }
            this.mCi.setRadioPower(IGNORE_GOOGLE_NON_ROAMING, null);
        } else if (!this.mDesiredPowerState && this.mCi.getRadioState().isOn()) {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else if (!this.mImsRegistrationOnOff || this.mAlarmSwitch) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else {
                log("mImsRegistrationOnOff == true");
                Context context = this.mPhone.getContext();
                AlarmManager am = (AlarmManager) context.getSystemService("alarm");
                this.mRadioOffIntent = PendingIntent.getBroadcast(context, PS_ONLY, new Intent(ACTION_RADIO_OFF), PS_ONLY);
                this.mAlarmSwitch = IGNORE_GOOGLE_NON_ROAMING;
                log("Alarm setting");
                am.set(OTASP_NEEDED, SystemClock.elapsedRealtime() + 3000, this.mRadioOffIntent);
            }
            if (this.mHwCustGsmServiceStateTracker != null) {
                this.mHwCustGsmServiceStateTracker.setRadioPower(this.mCi, VDBG);
            }
        } else if (this.mDeviceShuttingDown && this.mCi.getRadioState().isAvailable()) {
            this.mCi.requestShutdown(null);
        }
    }

    protected void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            if (this.mUiccApplcation != newUiccApplication) {
                if (this.mUiccApplcation != null) {
                    log("Removing stale icc objects.");
                    this.mUiccApplcation.unregisterForReady(this);
                    this.mUiccApplcation.unregisterForGetAdDone(this);
                    if (this.mIccRecords != null) {
                        this.mIccRecords.unregisterForRecordsLoaded(this);
                        HwTelephonyFactory.getHwNetworkManager().unregisterForSimRecordsEvents(this, this.mPhone, this.mIccRecords);
                    }
                    this.mIccRecords = null;
                    this.mUiccApplcation = null;
                }
                if (newUiccApplication != null) {
                    log("New card found");
                    this.mUiccApplcation = newUiccApplication;
                    this.mIccRecords = this.mUiccApplcation.getIccRecords();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mUiccApplcation.registerForReady(this, EVENT_SIM_READY, null);
                        this.mUiccApplcation.registerForGetAdDone(this, PS_DISABLED, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, EVENT_SIM_RECORDS_LOADED, null);
                            HwTelephonyFactory.getHwNetworkManager().registerForSimRecordsEvents(this, this.mPhone, this.mIccRecords);
                        }
                    } else if (this.mIsSubscriptionFromRuim) {
                        registerForRuimEvents();
                    }
                }
            }
        }
    }

    protected void log(String s) {
        Rlog.d(this.LOG_TAG, s);
    }

    protected void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }

    public int getCurrentDataConnectionState() {
        return this.mSS.getDataRegState();
    }

    public boolean isConcurrentVoiceAndDataAllowed() {
        boolean z = IGNORE_GOOGLE_NON_ROAMING;
        if (!this.mPhone.isPhoneTypeGsm()) {
            return (this.mPhone.isPhoneTypeCdma() || this.mSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE || MDOEM_WORK_MODE_IS_SRLTE) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
        } else {
            if (SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true") && !this.isCurrent3GPsCsAllowed) {
                log("current not allow voice and data simultaneously by vp");
                return VDBG;
            } else if (this.mSS.getRilDataRadioTechnology() >= OTASP_NOT_NEEDED && this.mSS.getRilDataRadioTechnology() != EVENT_SIM_RECORDS_LOADED) {
                return IGNORE_GOOGLE_NON_ROAMING;
            } else {
                if (this.mSS.getCssIndicator() != PS_CS) {
                    z = VDBG;
                }
                return z;
            }
        }
    }

    public void setImsRegistrationState(boolean registered) {
        log("ImsRegistrationState - registered : " + registered);
        if (this.mImsRegistrationOnOff && !registered && this.mAlarmSwitch) {
            this.mImsRegistrationOnOff = registered;
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = VDBG;
            sendMessage(obtainMessage(EVENT_CHANGE_IMS_STATE));
            return;
        }
        this.mImsRegistrationOnOff = registered;
    }

    public void onImsCapabilityChanged() {
        if (this.mPhone.isPhoneTypeGsm()) {
            sendMessage(obtainMessage(EVENT_IMS_CAPABILITY_CHANGED));
        }
    }

    public void pollState() {
        pollState(VDBG);
    }

    private void modemTriggeredPollState() {
        pollState(IGNORE_GOOGLE_NON_ROAMING);
    }

    public void pollState(boolean modemTriggered) {
        this.mPollingContext = new int[PS_CS];
        this.mPollingContext[PS_ONLY] = PS_ONLY;
        switch (-getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues()[this.mCi.getRadioState().ordinal()]) {
            case PS_CS /*1*/:
                this.mNewSS.setStateOff();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                this.mGotCountryCode = VDBG;
                this.mNitzUpdatedTime = VDBG;
                if (!(modemTriggered || EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology())) {
                    pollStateDone();
                    return;
                }
            case OTASP_NEEDED /*2*/:
                this.mNewSS.setStateOutOfService();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                this.mGotCountryCode = VDBG;
                this.mNitzUpdatedTime = VDBG;
                pollStateDone();
                return;
        }
        int[] iArr = this.mPollingContext;
        iArr[PS_ONLY] = iArr[PS_ONLY] + PS_CS;
        this.mCi.getOperator(obtainMessage(EVENT_POLL_STATE_OPERATOR, this.mPollingContext));
        iArr = this.mPollingContext;
        iArr[PS_ONLY] = iArr[PS_ONLY] + PS_CS;
        this.mCi.getDataRegistrationState(obtainMessage(OTASP_SIM_UNPROVISIONED, this.mPollingContext));
        iArr = this.mPollingContext;
        iArr[PS_ONLY] = iArr[PS_ONLY] + PS_CS;
        this.mCi.getVoiceRegistrationState(obtainMessage(EVENT_POLL_STATE_REGISTRATION, this.mPollingContext));
        if (this.mPhone.isPhoneTypeGsm()) {
            iArr = this.mPollingContext;
            iArr[PS_ONLY] = iArr[PS_ONLY] + PS_CS;
            this.mCi.getNetworkSelectionMode(obtainMessage(EVENT_POLL_STATE_NETWORK_SELECTION_MODE, this.mPollingContext));
            HwTelephonyFactory.getHwNetworkManager().getLocationInfo(this, this.mPhone);
        }
        if (this.mHwCustGsmServiceStateTracker != null) {
            this.mHwCustGsmServiceStateTracker.getLteFreqWithWlanCoex(this.mCi, this);
        }
    }

    private void updateOperatorProp() {
        if (this.mPhone != null && this.mSS != null) {
            this.mPhone.setSystemProperty("gsm.operator.alpha", this.mSS.getOperatorAlphaLong());
        }
    }

    private void pollStateDone() {
        if (this.mPhone.isPhoneTypeGsm()) {
            pollStateDoneGsm();
        } else if (this.mPhone.isPhoneTypeCdma()) {
            pollStateDoneCdma();
        } else {
            pollStateDoneCdmaLte();
        }
    }

    private void pollStateDoneGsm() {
        boolean hasRegistered;
        boolean hasGprsAttached;
        boolean hasGprsDetached;
        boolean hasVoiceRoamingOff;
        boolean dataRoaming;
        boolean hasDataRoamingOff;
        boolean hasLocationChanged;
        boolean needNotifyData;
        boolean hasLacChanged;
        TelephonyManager tm;
        Integer[] numArr;
        int cid;
        GsmCellLocation loc;
        ServiceState tss;
        CellLocation tcl;
        String prevOperatorNumeric;
        String operatorNumeric;
        String iso;
        String mcc;
        String prevMcc;
        TimeZone zone;
        boolean testOneUniqueOffsetPath;
        ArrayList<TimeZone> uniqueZones;
        TimeZone defaultZones;
        Message roamingOn;
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, VDBG)) {
            this.mNewSS.setVoiceRoaming(IGNORE_GOOGLE_NON_ROAMING);
            this.mNewSS.setDataRoaming(IGNORE_GOOGLE_NON_ROAMING);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]" + " oldMaxDataCalls=" + this.mMaxDataCalls + " mNewMaxDataCalls=" + this.mNewMaxDataCalls + " oldReasonDataDenied=" + this.mReasonDataDenied + " mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        if (this.mSS.getVoiceRegState() != 0) {
            hasRegistered = this.mNewSS.getVoiceRegState() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasRegistered = VDBG;
        }
        if (this.mSS.getVoiceRegState() == 0) {
            if (this.mNewSS.getVoiceRegState() != 0) {
            }
        }
        if (this.mSS.getDataRegState() != 0) {
            hasGprsAttached = this.mNewSS.getDataRegState() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasGprsAttached = VDBG;
        }
        if (this.mSS.getDataRegState() == 0) {
            hasGprsDetached = this.mNewSS.getDataRegState() != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasGprsDetached = VDBG;
        }
        boolean hasDataRegStateChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasVoiceRegStateChanged = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasRilDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasChanged = this.mNewSS.equals(this.mSS) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
        boolean voiceRoaming = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : VDBG;
        if (this.mSS.getVoiceRoaming()) {
            if (!this.mNewSS.getVoiceRoaming()) {
                hasVoiceRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                dataRoaming = this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : VDBG;
                if (this.mSS.getDataRoaming()) {
                    if (!this.mNewSS.getDataRoaming()) {
                        hasDataRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                        hasLocationChanged = this.mNewCellLoc.equals(this.mCellLoc) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
                        needNotifyData = this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                        hasLacChanged = ((GsmCellLocation) this.mNewCellLoc).getLac() == ((GsmCellLocation) this.mCellLoc).getLac() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                        resetServiceStateInIwlanMode();
                        tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                        if (hasVoiceRegStateChanged || hasDataRegStateChanged) {
                            numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                            numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                            numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                            numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                            numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                            EventLog.writeEvent(EventLogTags.GSM_SERVICE_STATE_CHANGE, numArr);
                        }
                        if (hasChanged && hasRegistered) {
                            this.mPhone.mDcTracker.setMPDNByNetWork(this.mNewSS.getOperatorNumeric());
                        }
                        if (hasRegistered || hasGprsAttached) {
                            log("service state hasRegistered , poll signal strength at once");
                            sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                        }
                        if (hasRilVoiceRadioTechnologyChanged) {
                            cid = -1;
                            loc = (GsmCellLocation) this.mNewCellLoc;
                            if (loc != null) {
                                cid = loc.getCid();
                            }
                            numArr = new Object[OTASP_NOT_NEEDED];
                            numArr[PS_ONLY] = Integer.valueOf(cid);
                            numArr[PS_CS] = Integer.valueOf(this.mSS.getRilVoiceRadioTechnology());
                            numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology());
                            EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED_NEW, numArr);
                            log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
                        }
                        if (!HwTelephonyFactory.getHwNetworkManager().proccessGsmDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                            tss = this.mSS;
                            this.mSS = this.mNewSS;
                            this.mNewSS = tss;
                            this.mNewSS.setStateOutOfService();
                            tcl = (GsmCellLocation) this.mCellLoc;
                            this.mCellLoc = this.mNewCellLoc;
                            this.mNewCellLoc = tcl;
                            this.mReasonDataDenied = this.mNewReasonDataDenied;
                            this.mMaxDataCalls = this.mNewMaxDataCalls;
                            if (hasRilVoiceRadioTechnologyChanged) {
                                updatePhoneObject();
                            }
                            if (hasRilDataRadioTechnologyChanged) {
                                tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                                if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                                    log("pollStateDone: IWLAN enabled");
                                }
                            }
                            if (hasRegistered) {
                                this.mPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
                                this.mNetworkAttachedRegistrants.notifyRegistrants();
                                if (CLEAR_NITZ_WHEN_REG) {
                                    if (SystemClock.elapsedRealtime() - this.mLastReceivedNITZReferenceTime > 5000) {
                                        this.mNitzUpdatedTime = VDBG;
                                        log("pollStateDone: registering current mNitzUpdatedTime=" + this.mNitzUpdatedTime + " changing to false because hasRegistered");
                                    }
                                }
                            }
                            if (hasChanged) {
                                updateSpnDisplay();
                                tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                                updateOperatorProp();
                                prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                                operatorNumeric = this.mSS.getOperatorNumeric();
                                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                                updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                                if (TextUtils.isEmpty(operatorNumeric)) {
                                    iso = "";
                                    mcc = "";
                                    prevMcc = "";
                                    try {
                                        mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                        iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                                        if (prevOperatorNumeric != null) {
                                            if (!prevOperatorNumeric.equals("")) {
                                                prevMcc = prevOperatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                                                this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                                zone = null;
                                                if (!this.mNitzUpdatedTime) {
                                                    if (!(mcc.equals(INVALID_MCC) || TextUtils.isEmpty(iso) || !getAutoTimeZone() || mcc.equals(prevMcc))) {
                                                        testOneUniqueOffsetPath = SystemProperties.getBoolean("telephony.test.ignore.nitz", VDBG) ? (SystemClock.uptimeMillis() & 1) != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG : VDBG;
                                                        uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                                                        defaultZones = getTimeZoneFromMcc(mcc);
                                                        if (uniqueZones.size() == PS_CS && !testOneUniqueOffsetPath && defaultZones == null) {
                                                            log("pollStateDone: there are " + uniqueZones.size() + " unique offsets for iso-cc='" + iso + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath + "', do nothing");
                                                        } else {
                                                            zone = (TimeZone) uniqueZones.get(PS_ONLY);
                                                            if (defaultZones != null) {
                                                                zone = defaultZones;
                                                                log("some countrys has more than two timezone, choose a default");
                                                            }
                                                            log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                                                            setAndBroadcastNetworkSetTimeZone(zone.getID());
                                                        }
                                                    }
                                                }
                                                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                                    fixTimeZone(iso, zone);
                                                }
                                            }
                                        }
                                        prevMcc = "";
                                    } catch (NumberFormatException ex) {
                                        loge("pollStateDone: countryCodeForMcc error" + ex);
                                    } catch (StringIndexOutOfBoundsException ex2) {
                                        loge("pollStateDone: countryCodeForMcc error" + ex2);
                                    }
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                                    this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                    zone = null;
                                    if (this.mNitzUpdatedTime) {
                                        if (SystemProperties.getBoolean("telephony.test.ignore.nitz", VDBG)) {
                                            if ((SystemClock.uptimeMillis() & 1) != 0) {
                                            }
                                        }
                                        uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                                        defaultZones = getTimeZoneFromMcc(mcc);
                                        if (uniqueZones.size() == PS_CS) {
                                        }
                                        zone = (TimeZone) uniqueZones.get(PS_ONLY);
                                        if (defaultZones != null) {
                                            zone = defaultZones;
                                            log("some countrys has more than two timezone, choose a default");
                                        }
                                        log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                                        setAndBroadcastNetworkSetTimeZone(zone.getID());
                                    }
                                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                        fixTimeZone(iso, zone);
                                    }
                                } else {
                                    log("operatorNumeric is null");
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                                    this.mGotCountryCode = VDBG;
                                    this.mNitzUpdatedTime = VDBG;
                                }
                                tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming());
                                setRoamingType(this.mSS);
                                log("Broadcasting ServiceState : " + this.mSS);
                                this.mPhone.notifyServiceStateChanged(this.mSS);
                                if (this.mHwCustGsmServiceStateTracker != null) {
                                    this.mHwCustGsmServiceStateTracker.updateRomingVoicemailNumber(this.mSS);
                                }
                                this.mEventLog.writeServiceStateChanged(this.mSS);
                            }
                            HwTelephonyFactory.getHwNetworkManager().processGsmCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                            if (hasGprsAttached) {
                                this.mAttachedRegistrants.notifyRegistrants();
                            }
                            if (hasGprsDetached) {
                                this.mDetachedRegistrants.notifyRegistrants();
                            }
                            if (hasDataRegStateChanged || hasRilDataRadioTechnologyChanged) {
                                notifyDataRegStateRilRadioTechnologyChanged();
                                if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                                    this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                                } else {
                                    needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                                }
                            }
                            if (needNotifyData) {
                                this.mPhone.notifyDataConnection(null);
                            }
                            if (voiceRoaming) {
                                if (SystemProperties.getBoolean("ro.config_hw_doubletime", VDBG) && HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == getPhoneId()) {
                                    log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                                    roamingOn = obtainMessage();
                                    roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
                                    sendMessageDelayed(roamingOn, 60000);
                                }
                                this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                            }
                            if (hasVoiceRoamingOff) {
                                this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                            }
                            if (dataRoaming) {
                                if (SystemProperties.getBoolean("ro.config_hw_doubletime", VDBG) && HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == getPhoneId()) {
                                    log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                                    roamingOn = obtainMessage();
                                    roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
                                    sendMessageDelayed(roamingOn, 60000);
                                }
                                this.mDataRoamingOnRegistrants.notifyRegistrants();
                            }
                            if (hasDataRoamingOff) {
                                this.mDataRoamingOffRegistrants.notifyRegistrants();
                            }
                            if (hasLocationChanged) {
                                this.mPhone.notifyLocationChanged();
                            }
                            if (hasLacChanged) {
                                Rlog.i(this.LOG_TAG, "LAC changed, update operator name display");
                                updateSpnDisplay();
                            }
                            if (!isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                                this.mReportedGprsNoReg = VDBG;
                            } else if (!(this.mStartedGprsRegCheck || this.mReportedGprsNoReg)) {
                                this.mStartedGprsRegCheck = IGNORE_GOOGLE_NON_ROAMING;
                                sendMessageDelayed(obtainMessage(EVENT_CHECK_REPORT_GPRS), (long) Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", DELAY_TIME_TO_NOTIF_NITZ));
                            }
                        }
                    }
                }
                hasDataRoamingOff = VDBG;
                if (this.mNewCellLoc.equals(this.mCellLoc)) {
                }
                if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
                }
                if (((GsmCellLocation) this.mNewCellLoc).getLac() == ((GsmCellLocation) this.mCellLoc).getLac()) {
                }
                resetServiceStateInIwlanMode();
                tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                EventLog.writeEvent(EventLogTags.GSM_SERVICE_STATE_CHANGE, numArr);
                this.mPhone.mDcTracker.setMPDNByNetWork(this.mNewSS.getOperatorNumeric());
                log("service state hasRegistered , poll signal strength at once");
                sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                if (hasRilVoiceRadioTechnologyChanged) {
                    cid = -1;
                    loc = (GsmCellLocation) this.mNewCellLoc;
                    if (loc != null) {
                        cid = loc.getCid();
                    }
                    numArr = new Object[OTASP_NOT_NEEDED];
                    numArr[PS_ONLY] = Integer.valueOf(cid);
                    numArr[PS_CS] = Integer.valueOf(this.mSS.getRilVoiceRadioTechnology());
                    numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology());
                    EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED_NEW, numArr);
                    log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
                }
                if (!HwTelephonyFactory.getHwNetworkManager().proccessGsmDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                    tss = this.mSS;
                    this.mSS = this.mNewSS;
                    this.mNewSS = tss;
                    this.mNewSS.setStateOutOfService();
                    tcl = (GsmCellLocation) this.mCellLoc;
                    this.mCellLoc = this.mNewCellLoc;
                    this.mNewCellLoc = tcl;
                    this.mReasonDataDenied = this.mNewReasonDataDenied;
                    this.mMaxDataCalls = this.mNewMaxDataCalls;
                    if (hasRilVoiceRadioTechnologyChanged) {
                        updatePhoneObject();
                    }
                    if (hasRilDataRadioTechnologyChanged) {
                        tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                        if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                            log("pollStateDone: IWLAN enabled");
                        }
                    }
                    if (hasRegistered) {
                        this.mPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
                        this.mNetworkAttachedRegistrants.notifyRegistrants();
                        if (CLEAR_NITZ_WHEN_REG) {
                            if (SystemClock.elapsedRealtime() - this.mLastReceivedNITZReferenceTime > 5000) {
                                this.mNitzUpdatedTime = VDBG;
                                log("pollStateDone: registering current mNitzUpdatedTime=" + this.mNitzUpdatedTime + " changing to false because hasRegistered");
                            }
                        }
                    }
                    if (hasChanged) {
                        updateSpnDisplay();
                        tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                        updateOperatorProp();
                        prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                        operatorNumeric = this.mSS.getOperatorNumeric();
                        tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                        updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                        if (TextUtils.isEmpty(operatorNumeric)) {
                            iso = "";
                            mcc = "";
                            prevMcc = "";
                            mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                            iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                            if (prevOperatorNumeric != null) {
                                if (prevOperatorNumeric.equals("")) {
                                    prevMcc = prevOperatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                                    this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                    zone = null;
                                    if (this.mNitzUpdatedTime) {
                                        if (SystemProperties.getBoolean("telephony.test.ignore.nitz", VDBG)) {
                                            if ((SystemClock.uptimeMillis() & 1) != 0) {
                                            }
                                        }
                                        uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                                        defaultZones = getTimeZoneFromMcc(mcc);
                                        if (uniqueZones.size() == PS_CS) {
                                        }
                                        zone = (TimeZone) uniqueZones.get(PS_ONLY);
                                        if (defaultZones != null) {
                                            zone = defaultZones;
                                            log("some countrys has more than two timezone, choose a default");
                                        }
                                        log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                                        setAndBroadcastNetworkSetTimeZone(zone.getID());
                                    }
                                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                        fixTimeZone(iso, zone);
                                    }
                                }
                            }
                            prevMcc = "";
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                            this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                            zone = null;
                            if (this.mNitzUpdatedTime) {
                                if (SystemProperties.getBoolean("telephony.test.ignore.nitz", VDBG)) {
                                }
                                uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                                defaultZones = getTimeZoneFromMcc(mcc);
                                if (uniqueZones.size() == PS_CS) {
                                }
                                zone = (TimeZone) uniqueZones.get(PS_ONLY);
                                if (defaultZones != null) {
                                    zone = defaultZones;
                                    log("some countrys has more than two timezone, choose a default");
                                }
                                log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                                setAndBroadcastNetworkSetTimeZone(zone.getID());
                            }
                            if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                fixTimeZone(iso, zone);
                            }
                        } else {
                            log("operatorNumeric is null");
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                            this.mGotCountryCode = VDBG;
                            this.mNitzUpdatedTime = VDBG;
                        }
                        tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming());
                        setRoamingType(this.mSS);
                        log("Broadcasting ServiceState : " + this.mSS);
                        this.mPhone.notifyServiceStateChanged(this.mSS);
                        if (this.mHwCustGsmServiceStateTracker != null) {
                            this.mHwCustGsmServiceStateTracker.updateRomingVoicemailNumber(this.mSS);
                        }
                        this.mEventLog.writeServiceStateChanged(this.mSS);
                    }
                    HwTelephonyFactory.getHwNetworkManager().processGsmCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                    if (hasGprsAttached) {
                        this.mAttachedRegistrants.notifyRegistrants();
                    }
                    if (hasGprsDetached) {
                        this.mDetachedRegistrants.notifyRegistrants();
                    }
                    notifyDataRegStateRilRadioTechnologyChanged();
                    if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                        needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                    } else {
                        this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                    }
                    if (needNotifyData) {
                        this.mPhone.notifyDataConnection(null);
                    }
                    if (voiceRoaming) {
                        log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                        roamingOn = obtainMessage();
                        roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
                        sendMessageDelayed(roamingOn, 60000);
                        this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasVoiceRoamingOff) {
                        this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (dataRoaming) {
                        log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                        roamingOn = obtainMessage();
                        roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
                        sendMessageDelayed(roamingOn, 60000);
                        this.mDataRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasDataRoamingOff) {
                        this.mDataRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (hasLocationChanged) {
                        this.mPhone.notifyLocationChanged();
                    }
                    if (hasLacChanged) {
                        Rlog.i(this.LOG_TAG, "LAC changed, update operator name display");
                        updateSpnDisplay();
                    }
                    if (!isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                        this.mReportedGprsNoReg = VDBG;
                    } else {
                        this.mStartedGprsRegCheck = IGNORE_GOOGLE_NON_ROAMING;
                        sendMessageDelayed(obtainMessage(EVENT_CHECK_REPORT_GPRS), (long) Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", DELAY_TIME_TO_NOTIF_NITZ));
                    }
                }
            }
        }
        hasVoiceRoamingOff = VDBG;
        if (this.mSS.getDataRoaming()) {
        }
        if (this.mSS.getDataRoaming()) {
            if (this.mNewSS.getDataRoaming()) {
                hasDataRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                if (this.mNewCellLoc.equals(this.mCellLoc)) {
                }
                if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
                }
                if (((GsmCellLocation) this.mNewCellLoc).getLac() == ((GsmCellLocation) this.mCellLoc).getLac()) {
                }
                resetServiceStateInIwlanMode();
                tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                EventLog.writeEvent(EventLogTags.GSM_SERVICE_STATE_CHANGE, numArr);
                this.mPhone.mDcTracker.setMPDNByNetWork(this.mNewSS.getOperatorNumeric());
                log("service state hasRegistered , poll signal strength at once");
                sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                if (hasRilVoiceRadioTechnologyChanged) {
                    cid = -1;
                    loc = (GsmCellLocation) this.mNewCellLoc;
                    if (loc != null) {
                        cid = loc.getCid();
                    }
                    numArr = new Object[OTASP_NOT_NEEDED];
                    numArr[PS_ONLY] = Integer.valueOf(cid);
                    numArr[PS_CS] = Integer.valueOf(this.mSS.getRilVoiceRadioTechnology());
                    numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology());
                    EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED_NEW, numArr);
                    log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
                }
                if (!HwTelephonyFactory.getHwNetworkManager().proccessGsmDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                    tss = this.mSS;
                    this.mSS = this.mNewSS;
                    this.mNewSS = tss;
                    this.mNewSS.setStateOutOfService();
                    tcl = (GsmCellLocation) this.mCellLoc;
                    this.mCellLoc = this.mNewCellLoc;
                    this.mNewCellLoc = tcl;
                    this.mReasonDataDenied = this.mNewReasonDataDenied;
                    this.mMaxDataCalls = this.mNewMaxDataCalls;
                    if (hasRilVoiceRadioTechnologyChanged) {
                        updatePhoneObject();
                    }
                    if (hasRilDataRadioTechnologyChanged) {
                        tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                        if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                            log("pollStateDone: IWLAN enabled");
                        }
                    }
                    if (hasRegistered) {
                        this.mPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
                        this.mNetworkAttachedRegistrants.notifyRegistrants();
                        if (CLEAR_NITZ_WHEN_REG) {
                            if (SystemClock.elapsedRealtime() - this.mLastReceivedNITZReferenceTime > 5000) {
                                this.mNitzUpdatedTime = VDBG;
                                log("pollStateDone: registering current mNitzUpdatedTime=" + this.mNitzUpdatedTime + " changing to false because hasRegistered");
                            }
                        }
                    }
                    if (hasChanged) {
                        updateSpnDisplay();
                        tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                        updateOperatorProp();
                        prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                        operatorNumeric = this.mSS.getOperatorNumeric();
                        tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                        updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                        if (TextUtils.isEmpty(operatorNumeric)) {
                            log("operatorNumeric is null");
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                            this.mGotCountryCode = VDBG;
                            this.mNitzUpdatedTime = VDBG;
                        } else {
                            iso = "";
                            mcc = "";
                            prevMcc = "";
                            mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                            iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                            if (prevOperatorNumeric != null) {
                                if (prevOperatorNumeric.equals("")) {
                                    prevMcc = prevOperatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                                    this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                    zone = null;
                                    if (this.mNitzUpdatedTime) {
                                        if (SystemProperties.getBoolean("telephony.test.ignore.nitz", VDBG)) {
                                            if ((SystemClock.uptimeMillis() & 1) != 0) {
                                            }
                                        }
                                        uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                                        defaultZones = getTimeZoneFromMcc(mcc);
                                        if (uniqueZones.size() == PS_CS) {
                                        }
                                        zone = (TimeZone) uniqueZones.get(PS_ONLY);
                                        if (defaultZones != null) {
                                            zone = defaultZones;
                                            log("some countrys has more than two timezone, choose a default");
                                        }
                                        log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                                        setAndBroadcastNetworkSetTimeZone(zone.getID());
                                    }
                                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                        fixTimeZone(iso, zone);
                                    }
                                }
                            }
                            prevMcc = "";
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                            this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                            zone = null;
                            if (this.mNitzUpdatedTime) {
                                if (SystemProperties.getBoolean("telephony.test.ignore.nitz", VDBG)) {
                                }
                                uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                                defaultZones = getTimeZoneFromMcc(mcc);
                                if (uniqueZones.size() == PS_CS) {
                                }
                                zone = (TimeZone) uniqueZones.get(PS_ONLY);
                                if (defaultZones != null) {
                                    zone = defaultZones;
                                    log("some countrys has more than two timezone, choose a default");
                                }
                                log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                                setAndBroadcastNetworkSetTimeZone(zone.getID());
                            }
                            if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                fixTimeZone(iso, zone);
                            }
                        }
                        tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming());
                        setRoamingType(this.mSS);
                        log("Broadcasting ServiceState : " + this.mSS);
                        this.mPhone.notifyServiceStateChanged(this.mSS);
                        if (this.mHwCustGsmServiceStateTracker != null) {
                            this.mHwCustGsmServiceStateTracker.updateRomingVoicemailNumber(this.mSS);
                        }
                        this.mEventLog.writeServiceStateChanged(this.mSS);
                    }
                    HwTelephonyFactory.getHwNetworkManager().processGsmCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                    if (hasGprsAttached) {
                        this.mAttachedRegistrants.notifyRegistrants();
                    }
                    if (hasGprsDetached) {
                        this.mDetachedRegistrants.notifyRegistrants();
                    }
                    notifyDataRegStateRilRadioTechnologyChanged();
                    if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                        this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                    } else {
                        needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                    }
                    if (needNotifyData) {
                        this.mPhone.notifyDataConnection(null);
                    }
                    if (voiceRoaming) {
                        log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                        roamingOn = obtainMessage();
                        roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
                        sendMessageDelayed(roamingOn, 60000);
                        this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasVoiceRoamingOff) {
                        this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (dataRoaming) {
                        log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                        roamingOn = obtainMessage();
                        roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
                        sendMessageDelayed(roamingOn, 60000);
                        this.mDataRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasDataRoamingOff) {
                        this.mDataRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (hasLocationChanged) {
                        this.mPhone.notifyLocationChanged();
                    }
                    if (hasLacChanged) {
                        Rlog.i(this.LOG_TAG, "LAC changed, update operator name display");
                        updateSpnDisplay();
                    }
                    if (!isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                        this.mStartedGprsRegCheck = IGNORE_GOOGLE_NON_ROAMING;
                        sendMessageDelayed(obtainMessage(EVENT_CHECK_REPORT_GPRS), (long) Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", DELAY_TIME_TO_NOTIF_NITZ));
                    } else {
                        this.mReportedGprsNoReg = VDBG;
                    }
                }
            }
        }
        hasDataRoamingOff = VDBG;
        if (this.mNewCellLoc.equals(this.mCellLoc)) {
        }
        if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
        }
        if (((GsmCellLocation) this.mNewCellLoc).getLac() == ((GsmCellLocation) this.mCellLoc).getLac()) {
        }
        resetServiceStateInIwlanMode();
        tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
        numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
        numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
        numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
        numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
        EventLog.writeEvent(EventLogTags.GSM_SERVICE_STATE_CHANGE, numArr);
        this.mPhone.mDcTracker.setMPDNByNetWork(this.mNewSS.getOperatorNumeric());
        log("service state hasRegistered , poll signal strength at once");
        sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
        if (hasRilVoiceRadioTechnologyChanged) {
            cid = -1;
            loc = (GsmCellLocation) this.mNewCellLoc;
            if (loc != null) {
                cid = loc.getCid();
            }
            numArr = new Object[OTASP_NOT_NEEDED];
            numArr[PS_ONLY] = Integer.valueOf(cid);
            numArr[PS_CS] = Integer.valueOf(this.mSS.getRilVoiceRadioTechnology());
            numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology());
            EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED_NEW, numArr);
            log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
        }
        if (!HwTelephonyFactory.getHwNetworkManager().proccessGsmDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
            tss = this.mSS;
            this.mSS = this.mNewSS;
            this.mNewSS = tss;
            this.mNewSS.setStateOutOfService();
            tcl = (GsmCellLocation) this.mCellLoc;
            this.mCellLoc = this.mNewCellLoc;
            this.mNewCellLoc = tcl;
            this.mReasonDataDenied = this.mNewReasonDataDenied;
            this.mMaxDataCalls = this.mNewMaxDataCalls;
            if (hasRilVoiceRadioTechnologyChanged) {
                updatePhoneObject();
            }
            if (hasRilDataRadioTechnologyChanged) {
                tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                    log("pollStateDone: IWLAN enabled");
                }
            }
            if (hasRegistered) {
                this.mPhone.getContext().sendBroadcast(new Intent("com.android.net.wifi.countryCode"));
                this.mNetworkAttachedRegistrants.notifyRegistrants();
                if (CLEAR_NITZ_WHEN_REG) {
                    if (SystemClock.elapsedRealtime() - this.mLastReceivedNITZReferenceTime > 5000) {
                        this.mNitzUpdatedTime = VDBG;
                        log("pollStateDone: registering current mNitzUpdatedTime=" + this.mNitzUpdatedTime + " changing to false because hasRegistered");
                    }
                }
            }
            if (hasChanged) {
                updateSpnDisplay();
                tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                updateOperatorProp();
                prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                operatorNumeric = this.mSS.getOperatorNumeric();
                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                if (TextUtils.isEmpty(operatorNumeric)) {
                    iso = "";
                    mcc = "";
                    prevMcc = "";
                    mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                    iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                    if (prevOperatorNumeric != null) {
                        if (prevOperatorNumeric.equals("")) {
                            prevMcc = prevOperatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                            this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                            zone = null;
                            if (this.mNitzUpdatedTime) {
                                if (SystemProperties.getBoolean("telephony.test.ignore.nitz", VDBG)) {
                                    if ((SystemClock.uptimeMillis() & 1) != 0) {
                                    }
                                }
                                uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                                defaultZones = getTimeZoneFromMcc(mcc);
                                if (uniqueZones.size() == PS_CS) {
                                }
                                zone = (TimeZone) uniqueZones.get(PS_ONLY);
                                if (defaultZones != null) {
                                    zone = defaultZones;
                                    log("some countrys has more than two timezone, choose a default");
                                }
                                log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                                setAndBroadcastNetworkSetTimeZone(zone.getID());
                            }
                            if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                fixTimeZone(iso, zone);
                            }
                        }
                    }
                    prevMcc = "";
                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                    this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                    zone = null;
                    if (this.mNitzUpdatedTime) {
                        if (SystemProperties.getBoolean("telephony.test.ignore.nitz", VDBG)) {
                        }
                        uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
                        defaultZones = getTimeZoneFromMcc(mcc);
                        if (uniqueZones.size() == PS_CS) {
                        }
                        zone = (TimeZone) uniqueZones.get(PS_ONLY);
                        if (defaultZones != null) {
                            zone = defaultZones;
                            log("some countrys has more than two timezone, choose a default");
                        }
                        log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                        setAndBroadcastNetworkSetTimeZone(zone.getID());
                    }
                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                        fixTimeZone(iso, zone);
                    }
                } else {
                    log("operatorNumeric is null");
                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                    this.mGotCountryCode = VDBG;
                    this.mNitzUpdatedTime = VDBG;
                }
                tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming());
                setRoamingType(this.mSS);
                log("Broadcasting ServiceState : " + this.mSS);
                this.mPhone.notifyServiceStateChanged(this.mSS);
                if (this.mHwCustGsmServiceStateTracker != null) {
                    this.mHwCustGsmServiceStateTracker.updateRomingVoicemailNumber(this.mSS);
                }
                this.mEventLog.writeServiceStateChanged(this.mSS);
            }
            HwTelephonyFactory.getHwNetworkManager().processGsmCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
            if (hasGprsAttached) {
                this.mAttachedRegistrants.notifyRegistrants();
            }
            if (hasGprsDetached) {
                this.mDetachedRegistrants.notifyRegistrants();
            }
            notifyDataRegStateRilRadioTechnologyChanged();
            if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
            } else {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            }
            if (needNotifyData) {
                this.mPhone.notifyDataConnection(null);
            }
            if (voiceRoaming) {
                log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                roamingOn = obtainMessage();
                roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
                sendMessageDelayed(roamingOn, 60000);
                this.mVoiceRoamingOnRegistrants.notifyRegistrants();
            }
            if (hasVoiceRoamingOff) {
                this.mVoiceRoamingOffRegistrants.notifyRegistrants();
            }
            if (dataRoaming) {
                log("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
                roamingOn = obtainMessage();
                roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
                sendMessageDelayed(roamingOn, 60000);
                this.mDataRoamingOnRegistrants.notifyRegistrants();
            }
            if (hasDataRoamingOff) {
                this.mDataRoamingOffRegistrants.notifyRegistrants();
            }
            if (hasLocationChanged) {
                this.mPhone.notifyLocationChanged();
            }
            if (hasLacChanged) {
                Rlog.i(this.LOG_TAG, "LAC changed, update operator name display");
                updateSpnDisplay();
            }
            if (!isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                this.mReportedGprsNoReg = VDBG;
            } else {
                this.mStartedGprsRegCheck = IGNORE_GOOGLE_NON_ROAMING;
                sendMessageDelayed(obtainMessage(EVENT_CHECK_REPORT_GPRS), (long) Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", DELAY_TIME_TO_NOTIF_NITZ));
            }
        }
    }

    private TimeZone getTimeZoneFromMcc(String mcc) {
        if ("460".equals(mcc)) {
            return TimeZone.getTimeZone("Asia/Shanghai");
        }
        if ("255".equals(mcc)) {
            return TimeZone.getTimeZone("Europe/Kiev");
        }
        if ("214".equals(mcc)) {
            return TimeZone.getTimeZone("Europe/Madrid");
        }
        return null;
    }

    private void sendTimeZoneSelectionNotification() {
        String currentMcc = null;
        String operator = this.mSS.getOperatorNumeric();
        if (operator != null && operator.length() >= OTASP_NOT_NEEDED) {
            currentMcc = operator.substring(PS_ONLY, OTASP_NOT_NEEDED);
        }
        if (!this.mNitzUpdatedTime) {
            boolean isTheSameNWAsLast;
            ArrayList<TimeZone> timeZones = null;
            int tzListSize = PS_ONLY;
            String iso = getSystemProperty("gsm.operator.iso-country", "");
            String lastMcc = Systemex.getString(this.mCr, "last_registed_mcc");
            if (lastMcc == null || currentMcc == null) {
                isTheSameNWAsLast = VDBG;
            } else {
                isTheSameNWAsLast = lastMcc.equals(currentMcc);
            }
            log("[settimezone] the network " + operator + " don't support nitz! current network isTheSameNWAsLast" + isTheSameNWAsLast);
            if (!"".equals(iso)) {
                timeZones = TimeUtils.getTimeZones(iso);
                tzListSize = timeZones == null ? PS_ONLY : timeZones.size();
            }
            if (PS_CS != tzListSize || isTheSameNWAsLast) {
                log("[settimezone] there are " + tzListSize + " timezones in " + iso);
                Intent intent = new Intent(ACTION_TIMEZONE_SELECTION);
                intent.putExtra(TelephonyEventLog.SERVICE_STATE_VOICE_NUMERIC, operator);
                intent.putExtra("iso", iso);
                this.mPhone.getContext().sendStickyBroadcast(intent);
            } else {
                TimeZone tz = (TimeZone) timeZones.get(PS_ONLY);
                log("[settimezone] time zone:" + tz.getID());
                setAndBroadcastNetworkSetTimeZone(tz.getID());
            }
        }
        if (currentMcc != null) {
            Systemex.putString(this.mCr, "last_registed_mcc", currentMcc);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void pollStateDoneCdma() {
        boolean hasRegistered;
        boolean hasCdmaDataConnectionAttached;
        boolean hasCdmaDataConnectionDetached;
        boolean hasVoiceRoamingOff;
        boolean dataRoaming;
        boolean hasDataRoamingOff;
        boolean hasLocationChanged;
        TelephonyManager tm;
        Integer[] numArr;
        ServiceState tss;
        CellLocation tcl;
        String mccmnc;
        String mcc;
        String eriText;
        String prevOperatorNumeric;
        String operatorNumeric;
        String isoCountryCode;
        updateRoamingState();
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("pollStateDone: cdma oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]");
        if (this.mSS.getVoiceRegState() != 0) {
            hasRegistered = this.mNewSS.getVoiceRegState() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasRegistered = VDBG;
        }
        if (this.mSS.getDataRegState() != 0) {
            hasCdmaDataConnectionAttached = this.mNewSS.getDataRegState() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasCdmaDataConnectionAttached = VDBG;
        }
        if (this.mSS.getDataRegState() == 0) {
            hasCdmaDataConnectionDetached = this.mNewSS.getDataRegState() != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasCdmaDataConnectionDetached = VDBG;
        }
        boolean hasCdmaDataConnectionChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasRilDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasChanged = this.mNewSS.equals(this.mSS) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
        boolean voiceRoaming = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : VDBG;
        if (this.mSS.getVoiceRoaming()) {
            if (!this.mNewSS.getVoiceRoaming()) {
                hasVoiceRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                dataRoaming = this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : VDBG;
                if (this.mSS.getDataRoaming()) {
                    if (!this.mNewSS.getDataRoaming()) {
                        hasDataRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                        hasLocationChanged = this.mNewCellLoc.equals(this.mCellLoc) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
                        tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                        if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                        }
                        numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                        numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                        numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                        numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                        numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                        EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                        if (hasRegistered || hasCdmaDataConnectionAttached) {
                            log("service state hasRegistered , poll signal strength at once");
                            sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                        }
                        tss = this.mSS;
                        this.mSS = this.mNewSS;
                        this.mNewSS = tss;
                        this.mNewSS.setStateOutOfService();
                        tcl = (CdmaCellLocation) this.mCellLoc;
                        this.mCellLoc = this.mNewCellLoc;
                        this.mNewCellLoc = tcl;
                        if (hasRilVoiceRadioTechnologyChanged) {
                            updatePhoneObject();
                        }
                        if (hasRilDataRadioTechnologyChanged) {
                            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                            if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                                log("pollStateDone: IWLAN enabled");
                            }
                        }
                        if (hasRegistered) {
                            if (SystemProperties.getBoolean("ro.config_hw_doubletime", VDBG)) {
                                mccmnc = this.mSS.getOperatorNumeric();
                                mcc = "";
                                if (mccmnc != null) {
                                    Systemex.putString(this.mCr, "last_registed_mcc", mccmnc.substring(PS_ONLY, OTASP_NOT_NEEDED));
                                }
                            }
                            this.mNetworkAttachedRegistrants.notifyRegistrants();
                        }
                        if (hasChanged) {
                            if (this.mCi.getRadioState().isOn() && !this.mIsSubscriptionFromRuim) {
                                if (this.mSS.getVoiceRegState() != 0) {
                                    eriText = this.mPhone.getCdmaEriText();
                                } else {
                                    eriText = this.mPhone.getContext().getText(17039589).toString();
                                }
                                this.mSS.setOperatorAlphaLong(eriText);
                            }
                            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                            updateOperatorProp();
                            prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                            operatorNumeric = this.mSS.getOperatorNumeric();
                            if (isInvalidOperatorNumeric(operatorNumeric)) {
                                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                            }
                            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                            if (isInvalidOperatorNumeric(operatorNumeric)) {
                                isoCountryCode = "";
                                mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                try {
                                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                                } catch (NumberFormatException ex) {
                                    loge("pollStateDone: countryCodeForMcc error" + ex);
                                } catch (StringIndexOutOfBoundsException ex2) {
                                    loge("pollStateDone: countryCodeForMcc error" + ex2);
                                }
                                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                                this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                setOperatorIdd(operatorNumeric);
                                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                    fixTimeZone(isoCountryCode);
                                }
                            } else {
                                log("operatorNumeric " + operatorNumeric + "is invalid");
                                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                                this.mGotCountryCode = VDBG;
                            }
                            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                            updateSpnDisplay();
                            setRoamingType(this.mSS);
                            log("Broadcasting ServiceState : " + this.mSS);
                            this.mPhone.notifyServiceStateChanged(this.mSS);
                        }
                        HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                        if (hasCdmaDataConnectionAttached) {
                            this.mAttachedRegistrants.notifyRegistrants();
                        }
                        if (hasCdmaDataConnectionDetached) {
                            this.mDetachedRegistrants.notifyRegistrants();
                        }
                        if (hasCdmaDataConnectionChanged || hasRilDataRadioTechnologyChanged) {
                            notifyDataRegStateRilRadioTechnologyChanged();
                            if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                            } else {
                                this.mPhone.notifyDataConnection(null);
                            }
                        }
                        if (voiceRoaming) {
                            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                        }
                        if (hasVoiceRoamingOff) {
                            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                        }
                        if (dataRoaming) {
                            this.mDataRoamingOnRegistrants.notifyRegistrants();
                        }
                        if (hasDataRoamingOff) {
                            this.mDataRoamingOffRegistrants.notifyRegistrants();
                        }
                        if (!hasLocationChanged) {
                            this.mPhone.notifyLocationChanged();
                        }
                    }
                }
                hasDataRoamingOff = VDBG;
                if (this.mNewCellLoc.equals(this.mCellLoc)) {
                }
                tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                }
                numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                log("service state hasRegistered , poll signal strength at once");
                sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                tss = this.mSS;
                this.mSS = this.mNewSS;
                this.mNewSS = tss;
                this.mNewSS.setStateOutOfService();
                tcl = (CdmaCellLocation) this.mCellLoc;
                this.mCellLoc = this.mNewCellLoc;
                this.mNewCellLoc = tcl;
                if (hasRilVoiceRadioTechnologyChanged) {
                    updatePhoneObject();
                }
                if (hasRilDataRadioTechnologyChanged) {
                    tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                    if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                        log("pollStateDone: IWLAN enabled");
                    }
                }
                if (hasRegistered) {
                    if (SystemProperties.getBoolean("ro.config_hw_doubletime", VDBG)) {
                        mccmnc = this.mSS.getOperatorNumeric();
                        mcc = "";
                        if (mccmnc != null) {
                            Systemex.putString(this.mCr, "last_registed_mcc", mccmnc.substring(PS_ONLY, OTASP_NOT_NEEDED));
                        }
                    }
                    this.mNetworkAttachedRegistrants.notifyRegistrants();
                }
                if (hasChanged) {
                    if (this.mSS.getVoiceRegState() != 0) {
                        eriText = this.mPhone.getContext().getText(17039589).toString();
                    } else {
                        eriText = this.mPhone.getCdmaEriText();
                    }
                    this.mSS.setOperatorAlphaLong(eriText);
                    tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                    updateOperatorProp();
                    prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                    operatorNumeric = this.mSS.getOperatorNumeric();
                    if (isInvalidOperatorNumeric(operatorNumeric)) {
                        operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                    }
                    tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                    updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                    if (isInvalidOperatorNumeric(operatorNumeric)) {
                        isoCountryCode = "";
                        mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                        isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                        tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                        this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                        setOperatorIdd(operatorNumeric);
                        if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                            fixTimeZone(isoCountryCode);
                        }
                    } else {
                        log("operatorNumeric " + operatorNumeric + "is invalid");
                        tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                        this.mGotCountryCode = VDBG;
                    }
                    if (this.mSS.getVoiceRoaming()) {
                    }
                    tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                    updateSpnDisplay();
                    setRoamingType(this.mSS);
                    log("Broadcasting ServiceState : " + this.mSS);
                    this.mPhone.notifyServiceStateChanged(this.mSS);
                }
                HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                if (hasCdmaDataConnectionAttached) {
                    this.mAttachedRegistrants.notifyRegistrants();
                }
                if (hasCdmaDataConnectionDetached) {
                    this.mDetachedRegistrants.notifyRegistrants();
                }
                notifyDataRegStateRilRadioTechnologyChanged();
                if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                    this.mPhone.notifyDataConnection(null);
                } else {
                    this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                }
                if (voiceRoaming) {
                    this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                }
                if (hasVoiceRoamingOff) {
                    this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                }
                if (dataRoaming) {
                    this.mDataRoamingOnRegistrants.notifyRegistrants();
                }
                if (hasDataRoamingOff) {
                    this.mDataRoamingOffRegistrants.notifyRegistrants();
                }
                if (!hasLocationChanged) {
                    this.mPhone.notifyLocationChanged();
                }
            }
        }
        hasVoiceRoamingOff = VDBG;
        if (this.mSS.getDataRoaming()) {
        }
        if (this.mSS.getDataRoaming()) {
            if (this.mNewSS.getDataRoaming()) {
                hasDataRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                if (this.mNewCellLoc.equals(this.mCellLoc)) {
                }
                tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                }
                numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                log("service state hasRegistered , poll signal strength at once");
                sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                tss = this.mSS;
                this.mSS = this.mNewSS;
                this.mNewSS = tss;
                this.mNewSS.setStateOutOfService();
                tcl = (CdmaCellLocation) this.mCellLoc;
                this.mCellLoc = this.mNewCellLoc;
                this.mNewCellLoc = tcl;
                if (hasRilVoiceRadioTechnologyChanged) {
                    updatePhoneObject();
                }
                if (hasRilDataRadioTechnologyChanged) {
                    tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                    if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                        log("pollStateDone: IWLAN enabled");
                    }
                }
                if (hasRegistered) {
                    if (SystemProperties.getBoolean("ro.config_hw_doubletime", VDBG)) {
                        mccmnc = this.mSS.getOperatorNumeric();
                        mcc = "";
                        if (mccmnc != null) {
                            Systemex.putString(this.mCr, "last_registed_mcc", mccmnc.substring(PS_ONLY, OTASP_NOT_NEEDED));
                        }
                    }
                    this.mNetworkAttachedRegistrants.notifyRegistrants();
                }
                if (hasChanged) {
                    if (this.mSS.getVoiceRegState() != 0) {
                        eriText = this.mPhone.getCdmaEriText();
                    } else {
                        eriText = this.mPhone.getContext().getText(17039589).toString();
                    }
                    this.mSS.setOperatorAlphaLong(eriText);
                    tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                    updateOperatorProp();
                    prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                    operatorNumeric = this.mSS.getOperatorNumeric();
                    if (isInvalidOperatorNumeric(operatorNumeric)) {
                        operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                    }
                    tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                    updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                    if (isInvalidOperatorNumeric(operatorNumeric)) {
                        log("operatorNumeric " + operatorNumeric + "is invalid");
                        tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                        this.mGotCountryCode = VDBG;
                    } else {
                        isoCountryCode = "";
                        mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                        isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                        tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                        this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                        setOperatorIdd(operatorNumeric);
                        if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                            fixTimeZone(isoCountryCode);
                        }
                    }
                    if (this.mSS.getVoiceRoaming()) {
                    }
                    tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                    updateSpnDisplay();
                    setRoamingType(this.mSS);
                    log("Broadcasting ServiceState : " + this.mSS);
                    this.mPhone.notifyServiceStateChanged(this.mSS);
                }
                HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                if (hasCdmaDataConnectionAttached) {
                    this.mAttachedRegistrants.notifyRegistrants();
                }
                if (hasCdmaDataConnectionDetached) {
                    this.mDetachedRegistrants.notifyRegistrants();
                }
                notifyDataRegStateRilRadioTechnologyChanged();
                if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                    this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                } else {
                    this.mPhone.notifyDataConnection(null);
                }
                if (voiceRoaming) {
                    this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                }
                if (hasVoiceRoamingOff) {
                    this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                }
                if (dataRoaming) {
                    this.mDataRoamingOnRegistrants.notifyRegistrants();
                }
                if (hasDataRoamingOff) {
                    this.mDataRoamingOffRegistrants.notifyRegistrants();
                }
                if (!hasLocationChanged) {
                    this.mPhone.notifyLocationChanged();
                }
            }
        }
        hasDataRoamingOff = VDBG;
        if (this.mNewCellLoc.equals(this.mCellLoc)) {
        }
        tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
        }
        numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
        numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
        numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
        numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
        numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
        EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
        log("service state hasRegistered , poll signal strength at once");
        sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
        tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        tcl = (CdmaCellLocation) this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        if (hasRilDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        }
        if (hasRegistered) {
            if (SystemProperties.getBoolean("ro.config_hw_doubletime", VDBG)) {
                mccmnc = this.mSS.getOperatorNumeric();
                mcc = "";
                if (mccmnc != null) {
                    Systemex.putString(this.mCr, "last_registed_mcc", mccmnc.substring(PS_ONLY, OTASP_NOT_NEEDED));
                }
            }
            this.mNetworkAttachedRegistrants.notifyRegistrants();
        }
        if (hasChanged) {
            if (this.mSS.getVoiceRegState() != 0) {
                eriText = this.mPhone.getContext().getText(17039589).toString();
            } else {
                eriText = this.mPhone.getCdmaEriText();
            }
            this.mSS.setOperatorAlphaLong(eriText);
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
            updateOperatorProp();
            prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            operatorNumeric = this.mSS.getOperatorNumeric();
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
            }
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                isoCountryCode = "";
                mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                setOperatorIdd(operatorNumeric);
                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                    fixTimeZone(isoCountryCode);
                }
            } else {
                log("operatorNumeric " + operatorNumeric + "is invalid");
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                this.mGotCountryCode = VDBG;
            }
            if (this.mSS.getVoiceRoaming()) {
            }
            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
            updateSpnDisplay();
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
        }
        HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
        if (hasCdmaDataConnectionAttached) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        notifyDataRegStateRilRadioTechnologyChanged();
        if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
            this.mPhone.notifyDataConnection(null);
        } else {
            this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
        }
        if (voiceRoaming) {
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (dataRoaming) {
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (!hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void pollStateDoneCdmaLte() {
        boolean hasRegistered;
        boolean hasDeregistered;
        boolean hasCdmaDataConnectionAttached;
        boolean hasCdmaDataConnectionDetached;
        boolean hasVoiceRoamingOff;
        boolean dataRoaming;
        boolean hasDataRoamingOff;
        boolean hasLocationChanged;
        boolean has4gHandoff;
        boolean hasMultiApnSupport;
        boolean hasLostMultiApnSupport;
        boolean needNotifyData;
        TelephonyManager tm;
        Integer[] numArr;
        ServiceState tss;
        CellLocation tcl;
        boolean hasBrandOverride;
        String eriText;
        boolean showSpn;
        int iconIndex;
        String prevOperatorNumeric;
        String operatorNumeric;
        String isoCountryCode;
        String mcc;
        updateRoamingState();
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, VDBG)) {
            this.mNewSS.setVoiceRoaming(IGNORE_GOOGLE_NON_ROAMING);
            this.mNewSS.setDataRoaming(IGNORE_GOOGLE_NON_ROAMING);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("pollStateDone: lte 1 ss=[" + this.mSS + "] newSS=[" + this.mNewSS + "]");
        if (this.mSS.getVoiceRegState() != 0) {
            hasRegistered = this.mNewSS.getVoiceRegState() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasRegistered = VDBG;
        }
        if (this.mSS.getVoiceRegState() == 0) {
            hasDeregistered = this.mNewSS.getVoiceRegState() != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasDeregistered = VDBG;
        }
        if (this.mSS.getDataRegState() != 0) {
            hasCdmaDataConnectionAttached = this.mNewSS.getDataRegState() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasCdmaDataConnectionAttached = VDBG;
        }
        if (this.mSS.getDataRegState() == 0) {
            hasCdmaDataConnectionDetached = this.mNewSS.getDataRegState() != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        } else {
            hasCdmaDataConnectionDetached = VDBG;
        }
        boolean hasCdmaDataConnectionChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        boolean hasChanged = this.mNewSS.equals(this.mSS) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
        boolean voiceRoaming = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : VDBG;
        if (this.mSS.getVoiceRoaming()) {
            if (!this.mNewSS.getVoiceRoaming()) {
                hasVoiceRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                dataRoaming = this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : VDBG;
                if (this.mSS.getDataRoaming()) {
                    if (!this.mNewSS.getDataRoaming()) {
                        hasDataRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                        hasLocationChanged = this.mNewCellLoc.equals(this.mCellLoc) ? VDBG : IGNORE_GOOGLE_NON_ROAMING;
                        if (this.mNewSS.getDataRegState() != 0) {
                            if (this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                if (this.mNewSS.getRilDataRadioTechnology() == EVENT_RADIO_AVAILABLE) {
                                    has4gHandoff = IGNORE_GOOGLE_NON_ROAMING;
                                }
                            }
                            if (this.mSS.getRilDataRadioTechnology() != EVENT_RADIO_AVAILABLE) {
                                has4gHandoff = this.mNewSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                            } else {
                                has4gHandoff = VDBG;
                            }
                        } else {
                            has4gHandoff = VDBG;
                        }
                        if (this.mNewSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                            if (this.mNewSS.getRilDataRadioTechnology() != EVENT_RADIO_AVAILABLE) {
                                hasMultiApnSupport = VDBG;
                                if (this.mNewSS.getRilDataRadioTechnology() < EVENT_POLL_STATE_REGISTRATION) {
                                    hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() > 8 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                                } else {
                                    hasLostMultiApnSupport = VDBG;
                                }
                                needNotifyData = this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                                tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                                if (hasRegistered || hasCdmaDataConnectionAttached) {
                                    log("service state hasRegistered , poll signal strength at once");
                                    sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                                }
                                log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
                                if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                                }
                                numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                                numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                                numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                                numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                                numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                                EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                                if (!HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                                    tss = this.mSS;
                                    this.mSS = this.mNewSS;
                                    this.mNewSS = tss;
                                    this.mNewSS.setStateOutOfService();
                                    tcl = (CdmaCellLocation) this.mCellLoc;
                                    this.mCellLoc = this.mNewCellLoc;
                                    this.mNewCellLoc = tcl;
                                    this.mNewSS.setStateOutOfService();
                                    if (hasVoiceRadioTechnologyChanged) {
                                        updatePhoneObject();
                                    }
                                    if (hasDataRadioTechnologyChanged) {
                                        tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                                        if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                                            log("pollStateDone: IWLAN enabled");
                                        }
                                    }
                                    if (hasRegistered) {
                                        this.mNetworkAttachedRegistrants.notifyRegistrants();
                                    }
                                    if (hasChanged) {
                                        if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                                            hasBrandOverride = VDBG;
                                        } else {
                                            hasBrandOverride = this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                                        }
                                        if (!hasBrandOverride) {
                                            if (this.mCi.getRadioState().isOn()) {
                                                if (this.mPhone.isEriFileLoaded()) {
                                                    if (this.mSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                                    }
                                                    if (!this.mIsSubscriptionFromRuim) {
                                                        eriText = this.mSS.getOperatorAlphaLong();
                                                        if (this.mSS.getVoiceRegState() != 0) {
                                                            eriText = this.mPhone.getCdmaEriText();
                                                        } else {
                                                            if (this.mSS.getVoiceRegState() != OTASP_NOT_NEEDED) {
                                                                eriText = this.mIccRecords == null ? this.mIccRecords.getServiceProviderName() : null;
                                                                if (TextUtils.isEmpty(eriText)) {
                                                                    eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                                                                }
                                                            } else {
                                                                if (this.mSS.getDataRegState() != 0) {
                                                                    eriText = this.mPhone.getContext().getText(17039589).toString();
                                                                }
                                                            }
                                                        }
                                                        this.mSS.setOperatorAlphaLong(eriText);
                                                    }
                                                }
                                            }
                                        }
                                        if (!(this.mUiccApplcation == null || this.mUiccApplcation.getState() != AppState.APPSTATE_READY || this.mIccRecords == null)) {
                                            if (this.mSS.getVoiceRegState() != 0) {
                                            }
                                            if (this.mSS.getRilVoiceRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                                showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                                                iconIndex = this.mSS.getCdmaEriIconIndex();
                                                if (showSpn && iconIndex == PS_CS) {
                                                    if (isInHomeSidNid(this.mSS.getSystemId(), this.mSS.getNetworkId()) && this.mIccRecords != null) {
                                                        if (!TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                                                            this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                                        prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                                        operatorNumeric = this.mSS.getOperatorNumeric();
                                        if (isInvalidOperatorNumeric(operatorNumeric)) {
                                            operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                                        }
                                        tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                                        updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                                        if (isInvalidOperatorNumeric(operatorNumeric)) {
                                            isoCountryCode = "";
                                            mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                            try {
                                                isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                                            } catch (NumberFormatException ex) {
                                                loge("countryCodeForMcc error" + ex);
                                            } catch (StringIndexOutOfBoundsException ex2) {
                                                loge("countryCodeForMcc error" + ex2);
                                            }
                                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                                            this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                            setOperatorIdd(operatorNumeric);
                                            if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                                fixTimeZone(isoCountryCode);
                                            }
                                        } else {
                                            log("operatorNumeric is null");
                                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                                            this.mGotCountryCode = VDBG;
                                        }
                                        tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                                        updateSpnDisplay();
                                        setRoamingType(this.mSS);
                                        log("Broadcasting ServiceState : " + this.mSS);
                                        this.mPhone.notifyServiceStateChanged(this.mSS);
                                    }
                                    HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                                    if (hasCdmaDataConnectionAttached || has4gHandoff) {
                                        this.mAttachedRegistrants.notifyRegistrants();
                                    }
                                    if (hasCdmaDataConnectionDetached) {
                                        this.mDetachedRegistrants.notifyRegistrants();
                                    }
                                    if (hasCdmaDataConnectionChanged || hasDataRadioTechnologyChanged) {
                                        notifyDataRegStateRilRadioTechnologyChanged();
                                        if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                                            this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                                        } else {
                                            needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                                        }
                                    }
                                    if (needNotifyData) {
                                        this.mPhone.notifyDataConnection(null);
                                    }
                                    if (voiceRoaming) {
                                        this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                                    }
                                    if (hasVoiceRoamingOff) {
                                        this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                                    }
                                    if (dataRoaming) {
                                        this.mDataRoamingOnRegistrants.notifyRegistrants();
                                    }
                                    if (hasDataRoamingOff) {
                                        this.mDataRoamingOffRegistrants.notifyRegistrants();
                                    }
                                    if (hasLocationChanged) {
                                        this.mPhone.notifyLocationChanged();
                                    }
                                }
                            }
                        }
                        if (this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                            hasMultiApnSupport = this.mSS.getRilDataRadioTechnology() == EVENT_RADIO_AVAILABLE ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
                        } else {
                            hasMultiApnSupport = VDBG;
                        }
                        if (this.mNewSS.getRilDataRadioTechnology() < EVENT_POLL_STATE_REGISTRATION) {
                            hasLostMultiApnSupport = VDBG;
                        } else {
                            if (this.mNewSS.getRilDataRadioTechnology() > 8) {
                            }
                        }
                        if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
                        }
                        tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                        log("service state hasRegistered , poll signal strength at once");
                        sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                        log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
                        if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                        }
                        numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                        numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                        numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                        numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                        numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                        EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                        if (!HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                            tss = this.mSS;
                            this.mSS = this.mNewSS;
                            this.mNewSS = tss;
                            this.mNewSS.setStateOutOfService();
                            tcl = (CdmaCellLocation) this.mCellLoc;
                            this.mCellLoc = this.mNewCellLoc;
                            this.mNewCellLoc = tcl;
                            this.mNewSS.setStateOutOfService();
                            if (hasVoiceRadioTechnologyChanged) {
                                updatePhoneObject();
                            }
                            if (hasDataRadioTechnologyChanged) {
                                tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                                if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                                    log("pollStateDone: IWLAN enabled");
                                }
                            }
                            if (hasRegistered) {
                                this.mNetworkAttachedRegistrants.notifyRegistrants();
                            }
                            if (hasChanged) {
                                if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                                    if (this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) {
                                    }
                                } else {
                                    hasBrandOverride = VDBG;
                                }
                                if (hasBrandOverride) {
                                    if (this.mCi.getRadioState().isOn()) {
                                        if (this.mPhone.isEriFileLoaded()) {
                                            if (this.mSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                            }
                                            if (this.mIsSubscriptionFromRuim) {
                                                eriText = this.mSS.getOperatorAlphaLong();
                                                if (this.mSS.getVoiceRegState() != 0) {
                                                    if (this.mSS.getVoiceRegState() != OTASP_NOT_NEEDED) {
                                                        if (this.mSS.getDataRegState() != 0) {
                                                            eriText = this.mPhone.getContext().getText(17039589).toString();
                                                        }
                                                    } else {
                                                        if (this.mIccRecords == null) {
                                                        }
                                                        if (TextUtils.isEmpty(eriText)) {
                                                            eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                                                        }
                                                    }
                                                } else {
                                                    eriText = this.mPhone.getCdmaEriText();
                                                }
                                                this.mSS.setOperatorAlphaLong(eriText);
                                            }
                                        }
                                    }
                                }
                                if (this.mSS.getVoiceRegState() != 0) {
                                }
                                if (this.mSS.getRilVoiceRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                    showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                                    iconIndex = this.mSS.getCdmaEriIconIndex();
                                    if (TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                                        this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                                    }
                                }
                                tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                                prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                                operatorNumeric = this.mSS.getOperatorNumeric();
                                if (isInvalidOperatorNumeric(operatorNumeric)) {
                                    operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                                }
                                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                                updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                                if (isInvalidOperatorNumeric(operatorNumeric)) {
                                    isoCountryCode = "";
                                    mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                                    this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                    setOperatorIdd(operatorNumeric);
                                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                        fixTimeZone(isoCountryCode);
                                    }
                                } else {
                                    log("operatorNumeric is null");
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                                    this.mGotCountryCode = VDBG;
                                }
                                if (this.mSS.getVoiceRoaming()) {
                                }
                                tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                                updateSpnDisplay();
                                setRoamingType(this.mSS);
                                log("Broadcasting ServiceState : " + this.mSS);
                                this.mPhone.notifyServiceStateChanged(this.mSS);
                            }
                            HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                            this.mAttachedRegistrants.notifyRegistrants();
                            if (hasCdmaDataConnectionDetached) {
                                this.mDetachedRegistrants.notifyRegistrants();
                            }
                            notifyDataRegStateRilRadioTechnologyChanged();
                            if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                                needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                            } else {
                                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                            }
                            if (needNotifyData) {
                                this.mPhone.notifyDataConnection(null);
                            }
                            if (voiceRoaming) {
                                this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                            }
                            if (hasVoiceRoamingOff) {
                                this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                            }
                            if (dataRoaming) {
                                this.mDataRoamingOnRegistrants.notifyRegistrants();
                            }
                            if (hasDataRoamingOff) {
                                this.mDataRoamingOffRegistrants.notifyRegistrants();
                            }
                            if (hasLocationChanged) {
                                this.mPhone.notifyLocationChanged();
                            }
                        }
                    }
                }
                hasDataRoamingOff = VDBG;
                if (this.mNewCellLoc.equals(this.mCellLoc)) {
                }
                if (this.mNewSS.getDataRegState() != 0) {
                    has4gHandoff = VDBG;
                } else {
                    if (this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                        if (this.mNewSS.getRilDataRadioTechnology() == EVENT_RADIO_AVAILABLE) {
                            has4gHandoff = IGNORE_GOOGLE_NON_ROAMING;
                        }
                    }
                    if (this.mSS.getRilDataRadioTechnology() != EVENT_RADIO_AVAILABLE) {
                        has4gHandoff = VDBG;
                    } else {
                        if (this.mNewSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                        }
                    }
                }
                if (this.mNewSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                    if (this.mNewSS.getRilDataRadioTechnology() != EVENT_RADIO_AVAILABLE) {
                        hasMultiApnSupport = VDBG;
                        if (this.mNewSS.getRilDataRadioTechnology() < EVENT_POLL_STATE_REGISTRATION) {
                            if (this.mNewSS.getRilDataRadioTechnology() > 8) {
                            }
                        } else {
                            hasLostMultiApnSupport = VDBG;
                        }
                        if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
                        }
                        tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                        log("service state hasRegistered , poll signal strength at once");
                        sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                        log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
                        if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                        }
                        numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                        numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                        numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                        numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                        numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                        EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                        if (!HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                            tss = this.mSS;
                            this.mSS = this.mNewSS;
                            this.mNewSS = tss;
                            this.mNewSS.setStateOutOfService();
                            tcl = (CdmaCellLocation) this.mCellLoc;
                            this.mCellLoc = this.mNewCellLoc;
                            this.mNewCellLoc = tcl;
                            this.mNewSS.setStateOutOfService();
                            if (hasVoiceRadioTechnologyChanged) {
                                updatePhoneObject();
                            }
                            if (hasDataRadioTechnologyChanged) {
                                tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                                if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                                    log("pollStateDone: IWLAN enabled");
                                }
                            }
                            if (hasRegistered) {
                                this.mNetworkAttachedRegistrants.notifyRegistrants();
                            }
                            if (hasChanged) {
                                if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                                    hasBrandOverride = VDBG;
                                } else {
                                    if (this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) {
                                    }
                                }
                                if (hasBrandOverride) {
                                    if (this.mCi.getRadioState().isOn()) {
                                        if (this.mPhone.isEriFileLoaded()) {
                                            if (this.mSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                            }
                                            if (this.mIsSubscriptionFromRuim) {
                                                eriText = this.mSS.getOperatorAlphaLong();
                                                if (this.mSS.getVoiceRegState() != 0) {
                                                    eriText = this.mPhone.getCdmaEriText();
                                                } else {
                                                    if (this.mSS.getVoiceRegState() != OTASP_NOT_NEEDED) {
                                                        if (this.mIccRecords == null) {
                                                        }
                                                        if (TextUtils.isEmpty(eriText)) {
                                                            eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                                                        }
                                                    } else {
                                                        if (this.mSS.getDataRegState() != 0) {
                                                            eriText = this.mPhone.getContext().getText(17039589).toString();
                                                        }
                                                    }
                                                }
                                                this.mSS.setOperatorAlphaLong(eriText);
                                            }
                                        }
                                    }
                                }
                                if (this.mSS.getVoiceRegState() != 0) {
                                }
                                if (this.mSS.getRilVoiceRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                    showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                                    iconIndex = this.mSS.getCdmaEriIconIndex();
                                    if (TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                                        this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                                    }
                                }
                                tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                                prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                                operatorNumeric = this.mSS.getOperatorNumeric();
                                if (isInvalidOperatorNumeric(operatorNumeric)) {
                                    operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                                }
                                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                                updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                                if (isInvalidOperatorNumeric(operatorNumeric)) {
                                    log("operatorNumeric is null");
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                                    this.mGotCountryCode = VDBG;
                                } else {
                                    isoCountryCode = "";
                                    mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                                    this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                    setOperatorIdd(operatorNumeric);
                                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                        fixTimeZone(isoCountryCode);
                                    }
                                }
                                if (this.mSS.getVoiceRoaming()) {
                                }
                                tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                                updateSpnDisplay();
                                setRoamingType(this.mSS);
                                log("Broadcasting ServiceState : " + this.mSS);
                                this.mPhone.notifyServiceStateChanged(this.mSS);
                            }
                            HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                            this.mAttachedRegistrants.notifyRegistrants();
                            if (hasCdmaDataConnectionDetached) {
                                this.mDetachedRegistrants.notifyRegistrants();
                            }
                            notifyDataRegStateRilRadioTechnologyChanged();
                            if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                            } else {
                                needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                            }
                            if (needNotifyData) {
                                this.mPhone.notifyDataConnection(null);
                            }
                            if (voiceRoaming) {
                                this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                            }
                            if (hasVoiceRoamingOff) {
                                this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                            }
                            if (dataRoaming) {
                                this.mDataRoamingOnRegistrants.notifyRegistrants();
                            }
                            if (hasDataRoamingOff) {
                                this.mDataRoamingOffRegistrants.notifyRegistrants();
                            }
                            if (hasLocationChanged) {
                                this.mPhone.notifyLocationChanged();
                            }
                        }
                    }
                }
                if (this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                    hasMultiApnSupport = VDBG;
                } else {
                    if (this.mSS.getRilDataRadioTechnology() == EVENT_RADIO_AVAILABLE) {
                    }
                }
                if (this.mNewSS.getRilDataRadioTechnology() < EVENT_POLL_STATE_REGISTRATION) {
                    hasLostMultiApnSupport = VDBG;
                } else {
                    if (this.mNewSS.getRilDataRadioTechnology() > 8) {
                    }
                }
                if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
                }
                tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                log("service state hasRegistered , poll signal strength at once");
                sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
                if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                }
                numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                if (!HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                    tss = this.mSS;
                    this.mSS = this.mNewSS;
                    this.mNewSS = tss;
                    this.mNewSS.setStateOutOfService();
                    tcl = (CdmaCellLocation) this.mCellLoc;
                    this.mCellLoc = this.mNewCellLoc;
                    this.mNewCellLoc = tcl;
                    this.mNewSS.setStateOutOfService();
                    if (hasVoiceRadioTechnologyChanged) {
                        updatePhoneObject();
                    }
                    if (hasDataRadioTechnologyChanged) {
                        tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                        if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                            log("pollStateDone: IWLAN enabled");
                        }
                    }
                    if (hasRegistered) {
                        this.mNetworkAttachedRegistrants.notifyRegistrants();
                    }
                    if (hasChanged) {
                        if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                            if (this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) {
                            }
                        } else {
                            hasBrandOverride = VDBG;
                        }
                        if (hasBrandOverride) {
                            if (this.mCi.getRadioState().isOn()) {
                                if (this.mPhone.isEriFileLoaded()) {
                                    if (this.mSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                    }
                                    if (this.mIsSubscriptionFromRuim) {
                                        eriText = this.mSS.getOperatorAlphaLong();
                                        if (this.mSS.getVoiceRegState() != 0) {
                                            if (this.mSS.getVoiceRegState() != OTASP_NOT_NEEDED) {
                                                if (this.mSS.getDataRegState() != 0) {
                                                    eriText = this.mPhone.getContext().getText(17039589).toString();
                                                }
                                            } else {
                                                if (this.mIccRecords == null) {
                                                }
                                                if (TextUtils.isEmpty(eriText)) {
                                                    eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                                                }
                                            }
                                        } else {
                                            eriText = this.mPhone.getCdmaEriText();
                                        }
                                        this.mSS.setOperatorAlphaLong(eriText);
                                    }
                                }
                            }
                        }
                        if (this.mSS.getVoiceRegState() != 0) {
                        }
                        if (this.mSS.getRilVoiceRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                            showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                            iconIndex = this.mSS.getCdmaEriIconIndex();
                            if (TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                                this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                            }
                        }
                        tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                        prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                        operatorNumeric = this.mSS.getOperatorNumeric();
                        if (isInvalidOperatorNumeric(operatorNumeric)) {
                            operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                        }
                        tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                        updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                        if (isInvalidOperatorNumeric(operatorNumeric)) {
                            isoCountryCode = "";
                            mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                            isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                            this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                            setOperatorIdd(operatorNumeric);
                            if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                fixTimeZone(isoCountryCode);
                            }
                        } else {
                            log("operatorNumeric is null");
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                            this.mGotCountryCode = VDBG;
                        }
                        if (this.mSS.getVoiceRoaming()) {
                        }
                        tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                        updateSpnDisplay();
                        setRoamingType(this.mSS);
                        log("Broadcasting ServiceState : " + this.mSS);
                        this.mPhone.notifyServiceStateChanged(this.mSS);
                    }
                    HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                    this.mAttachedRegistrants.notifyRegistrants();
                    if (hasCdmaDataConnectionDetached) {
                        this.mDetachedRegistrants.notifyRegistrants();
                    }
                    notifyDataRegStateRilRadioTechnologyChanged();
                    if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                        needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                    } else {
                        this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                    }
                    if (needNotifyData) {
                        this.mPhone.notifyDataConnection(null);
                    }
                    if (voiceRoaming) {
                        this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasVoiceRoamingOff) {
                        this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (dataRoaming) {
                        this.mDataRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasDataRoamingOff) {
                        this.mDataRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (hasLocationChanged) {
                        this.mPhone.notifyLocationChanged();
                    }
                }
            }
        }
        hasVoiceRoamingOff = VDBG;
        if (this.mSS.getDataRoaming()) {
        }
        if (this.mSS.getDataRoaming()) {
            if (this.mNewSS.getDataRoaming()) {
                hasDataRoamingOff = IGNORE_GOOGLE_NON_ROAMING;
                if (this.mNewCellLoc.equals(this.mCellLoc)) {
                }
                if (this.mNewSS.getDataRegState() != 0) {
                    if (this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                        if (this.mNewSS.getRilDataRadioTechnology() == EVENT_RADIO_AVAILABLE) {
                            has4gHandoff = IGNORE_GOOGLE_NON_ROAMING;
                        }
                    }
                    if (this.mSS.getRilDataRadioTechnology() != EVENT_RADIO_AVAILABLE) {
                        if (this.mNewSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                        }
                    } else {
                        has4gHandoff = VDBG;
                    }
                } else {
                    has4gHandoff = VDBG;
                }
                if (this.mNewSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                    if (this.mNewSS.getRilDataRadioTechnology() != EVENT_RADIO_AVAILABLE) {
                        hasMultiApnSupport = VDBG;
                        if (this.mNewSS.getRilDataRadioTechnology() < EVENT_POLL_STATE_REGISTRATION) {
                            if (this.mNewSS.getRilDataRadioTechnology() > 8) {
                            }
                        } else {
                            hasLostMultiApnSupport = VDBG;
                        }
                        if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
                        }
                        tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                        log("service state hasRegistered , poll signal strength at once");
                        sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                        log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
                        if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                        }
                        numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                        numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                        numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                        numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                        numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                        EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                        if (!HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                            tss = this.mSS;
                            this.mSS = this.mNewSS;
                            this.mNewSS = tss;
                            this.mNewSS.setStateOutOfService();
                            tcl = (CdmaCellLocation) this.mCellLoc;
                            this.mCellLoc = this.mNewCellLoc;
                            this.mNewCellLoc = tcl;
                            this.mNewSS.setStateOutOfService();
                            if (hasVoiceRadioTechnologyChanged) {
                                updatePhoneObject();
                            }
                            if (hasDataRadioTechnologyChanged) {
                                tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                                if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                                    log("pollStateDone: IWLAN enabled");
                                }
                            }
                            if (hasRegistered) {
                                this.mNetworkAttachedRegistrants.notifyRegistrants();
                            }
                            if (hasChanged) {
                                if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                                    hasBrandOverride = VDBG;
                                } else {
                                    if (this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) {
                                    }
                                }
                                if (hasBrandOverride) {
                                    if (this.mCi.getRadioState().isOn()) {
                                        if (this.mPhone.isEriFileLoaded()) {
                                            if (this.mSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                            }
                                            if (this.mIsSubscriptionFromRuim) {
                                                eriText = this.mSS.getOperatorAlphaLong();
                                                if (this.mSS.getVoiceRegState() != 0) {
                                                    eriText = this.mPhone.getCdmaEriText();
                                                } else {
                                                    if (this.mSS.getVoiceRegState() != OTASP_NOT_NEEDED) {
                                                        if (this.mIccRecords == null) {
                                                        }
                                                        if (TextUtils.isEmpty(eriText)) {
                                                            eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                                                        }
                                                    } else {
                                                        if (this.mSS.getDataRegState() != 0) {
                                                            eriText = this.mPhone.getContext().getText(17039589).toString();
                                                        }
                                                    }
                                                }
                                                this.mSS.setOperatorAlphaLong(eriText);
                                            }
                                        }
                                    }
                                }
                                if (this.mSS.getVoiceRegState() != 0) {
                                }
                                if (this.mSS.getRilVoiceRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                    showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                                    iconIndex = this.mSS.getCdmaEriIconIndex();
                                    if (TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                                        this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                                    }
                                }
                                tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                                prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                                operatorNumeric = this.mSS.getOperatorNumeric();
                                if (isInvalidOperatorNumeric(operatorNumeric)) {
                                    operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                                }
                                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                                updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                                if (isInvalidOperatorNumeric(operatorNumeric)) {
                                    log("operatorNumeric is null");
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                                    this.mGotCountryCode = VDBG;
                                } else {
                                    isoCountryCode = "";
                                    mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                                    this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                                    setOperatorIdd(operatorNumeric);
                                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                        fixTimeZone(isoCountryCode);
                                    }
                                }
                                if (this.mSS.getVoiceRoaming()) {
                                }
                                tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                                updateSpnDisplay();
                                setRoamingType(this.mSS);
                                log("Broadcasting ServiceState : " + this.mSS);
                                this.mPhone.notifyServiceStateChanged(this.mSS);
                            }
                            HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                            this.mAttachedRegistrants.notifyRegistrants();
                            if (hasCdmaDataConnectionDetached) {
                                this.mDetachedRegistrants.notifyRegistrants();
                            }
                            notifyDataRegStateRilRadioTechnologyChanged();
                            if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                            } else {
                                needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                            }
                            if (needNotifyData) {
                                this.mPhone.notifyDataConnection(null);
                            }
                            if (voiceRoaming) {
                                this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                            }
                            if (hasVoiceRoamingOff) {
                                this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                            }
                            if (dataRoaming) {
                                this.mDataRoamingOnRegistrants.notifyRegistrants();
                            }
                            if (hasDataRoamingOff) {
                                this.mDataRoamingOffRegistrants.notifyRegistrants();
                            }
                            if (hasLocationChanged) {
                                this.mPhone.notifyLocationChanged();
                            }
                        }
                    }
                }
                if (this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                    if (this.mSS.getRilDataRadioTechnology() == EVENT_RADIO_AVAILABLE) {
                    }
                } else {
                    hasMultiApnSupport = VDBG;
                }
                if (this.mNewSS.getRilDataRadioTechnology() < EVENT_POLL_STATE_REGISTRATION) {
                    hasLostMultiApnSupport = VDBG;
                } else {
                    if (this.mNewSS.getRilDataRadioTechnology() > 8) {
                    }
                }
                if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
                }
                tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                log("service state hasRegistered , poll signal strength at once");
                sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
                if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                }
                numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                if (!HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                    tss = this.mSS;
                    this.mSS = this.mNewSS;
                    this.mNewSS = tss;
                    this.mNewSS.setStateOutOfService();
                    tcl = (CdmaCellLocation) this.mCellLoc;
                    this.mCellLoc = this.mNewCellLoc;
                    this.mNewCellLoc = tcl;
                    this.mNewSS.setStateOutOfService();
                    if (hasVoiceRadioTechnologyChanged) {
                        updatePhoneObject();
                    }
                    if (hasDataRadioTechnologyChanged) {
                        tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                        if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                            log("pollStateDone: IWLAN enabled");
                        }
                    }
                    if (hasRegistered) {
                        this.mNetworkAttachedRegistrants.notifyRegistrants();
                    }
                    if (hasChanged) {
                        if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                            if (this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) {
                            }
                        } else {
                            hasBrandOverride = VDBG;
                        }
                        if (hasBrandOverride) {
                            if (this.mCi.getRadioState().isOn()) {
                                if (this.mPhone.isEriFileLoaded()) {
                                    if (this.mSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                    }
                                    if (this.mIsSubscriptionFromRuim) {
                                        eriText = this.mSS.getOperatorAlphaLong();
                                        if (this.mSS.getVoiceRegState() != 0) {
                                            if (this.mSS.getVoiceRegState() != OTASP_NOT_NEEDED) {
                                                if (this.mSS.getDataRegState() != 0) {
                                                    eriText = this.mPhone.getContext().getText(17039589).toString();
                                                }
                                            } else {
                                                if (this.mIccRecords == null) {
                                                }
                                                if (TextUtils.isEmpty(eriText)) {
                                                    eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                                                }
                                            }
                                        } else {
                                            eriText = this.mPhone.getCdmaEriText();
                                        }
                                        this.mSS.setOperatorAlphaLong(eriText);
                                    }
                                }
                            }
                        }
                        if (this.mSS.getVoiceRegState() != 0) {
                        }
                        if (this.mSS.getRilVoiceRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                            showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                            iconIndex = this.mSS.getCdmaEriIconIndex();
                            if (TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                                this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                            }
                        }
                        tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                        prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                        operatorNumeric = this.mSS.getOperatorNumeric();
                        if (isInvalidOperatorNumeric(operatorNumeric)) {
                            operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                        }
                        tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                        updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                        if (isInvalidOperatorNumeric(operatorNumeric)) {
                            isoCountryCode = "";
                            mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                            isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                            this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                            setOperatorIdd(operatorNumeric);
                            if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                fixTimeZone(isoCountryCode);
                            }
                        } else {
                            log("operatorNumeric is null");
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                            this.mGotCountryCode = VDBG;
                        }
                        if (this.mSS.getVoiceRoaming()) {
                        }
                        tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                        updateSpnDisplay();
                        setRoamingType(this.mSS);
                        log("Broadcasting ServiceState : " + this.mSS);
                        this.mPhone.notifyServiceStateChanged(this.mSS);
                    }
                    HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                    this.mAttachedRegistrants.notifyRegistrants();
                    if (hasCdmaDataConnectionDetached) {
                        this.mDetachedRegistrants.notifyRegistrants();
                    }
                    notifyDataRegStateRilRadioTechnologyChanged();
                    if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                        needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                    } else {
                        this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                    }
                    if (needNotifyData) {
                        this.mPhone.notifyDataConnection(null);
                    }
                    if (voiceRoaming) {
                        this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasVoiceRoamingOff) {
                        this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (dataRoaming) {
                        this.mDataRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasDataRoamingOff) {
                        this.mDataRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (hasLocationChanged) {
                        this.mPhone.notifyLocationChanged();
                    }
                }
            }
        }
        hasDataRoamingOff = VDBG;
        if (this.mNewCellLoc.equals(this.mCellLoc)) {
        }
        if (this.mNewSS.getDataRegState() != 0) {
            has4gHandoff = VDBG;
        } else {
            if (this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                if (this.mNewSS.getRilDataRadioTechnology() == EVENT_RADIO_AVAILABLE) {
                    has4gHandoff = IGNORE_GOOGLE_NON_ROAMING;
                }
            }
            if (this.mSS.getRilDataRadioTechnology() != EVENT_RADIO_AVAILABLE) {
                has4gHandoff = VDBG;
            } else {
                if (this.mNewSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                }
            }
        }
        if (this.mNewSS.getRilDataRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
            if (this.mNewSS.getRilDataRadioTechnology() != EVENT_RADIO_AVAILABLE) {
                hasMultiApnSupport = VDBG;
                if (this.mNewSS.getRilDataRadioTechnology() < EVENT_POLL_STATE_REGISTRATION) {
                    if (this.mNewSS.getRilDataRadioTechnology() > 8) {
                    }
                } else {
                    hasLostMultiApnSupport = VDBG;
                }
                if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
                }
                tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
                log("service state hasRegistered , poll signal strength at once");
                sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
                log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
                if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
                }
                numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
                numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
                numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
                numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
                numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
                EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
                if (!HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
                    tss = this.mSS;
                    this.mSS = this.mNewSS;
                    this.mNewSS = tss;
                    this.mNewSS.setStateOutOfService();
                    tcl = (CdmaCellLocation) this.mCellLoc;
                    this.mCellLoc = this.mNewCellLoc;
                    this.mNewCellLoc = tcl;
                    this.mNewSS.setStateOutOfService();
                    if (hasVoiceRadioTechnologyChanged) {
                        updatePhoneObject();
                    }
                    if (hasDataRadioTechnologyChanged) {
                        tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                        if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                            log("pollStateDone: IWLAN enabled");
                        }
                    }
                    if (hasRegistered) {
                        this.mNetworkAttachedRegistrants.notifyRegistrants();
                    }
                    if (hasChanged) {
                        if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                            hasBrandOverride = VDBG;
                        } else {
                            if (this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) {
                            }
                        }
                        if (hasBrandOverride) {
                            if (this.mCi.getRadioState().isOn()) {
                                if (this.mPhone.isEriFileLoaded()) {
                                    if (this.mSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                                    }
                                    if (this.mIsSubscriptionFromRuim) {
                                        eriText = this.mSS.getOperatorAlphaLong();
                                        if (this.mSS.getVoiceRegState() != 0) {
                                            eriText = this.mPhone.getCdmaEriText();
                                        } else {
                                            if (this.mSS.getVoiceRegState() != OTASP_NOT_NEEDED) {
                                                if (this.mIccRecords == null) {
                                                }
                                                if (TextUtils.isEmpty(eriText)) {
                                                    eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                                                }
                                            } else {
                                                if (this.mSS.getDataRegState() != 0) {
                                                    eriText = this.mPhone.getContext().getText(17039589).toString();
                                                }
                                            }
                                        }
                                        this.mSS.setOperatorAlphaLong(eriText);
                                    }
                                }
                            }
                        }
                        if (this.mSS.getVoiceRegState() != 0) {
                        }
                        if (this.mSS.getRilVoiceRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                            showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                            iconIndex = this.mSS.getCdmaEriIconIndex();
                            if (TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                                this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                            }
                        }
                        tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                        prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                        operatorNumeric = this.mSS.getOperatorNumeric();
                        if (isInvalidOperatorNumeric(operatorNumeric)) {
                            operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                        }
                        tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                        updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                        if (isInvalidOperatorNumeric(operatorNumeric)) {
                            log("operatorNumeric is null");
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                            this.mGotCountryCode = VDBG;
                        } else {
                            isoCountryCode = "";
                            mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                            isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                            tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                            this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                            setOperatorIdd(operatorNumeric);
                            if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                                fixTimeZone(isoCountryCode);
                            }
                        }
                        if (this.mSS.getVoiceRoaming()) {
                        }
                        tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                        updateSpnDisplay();
                        setRoamingType(this.mSS);
                        log("Broadcasting ServiceState : " + this.mSS);
                        this.mPhone.notifyServiceStateChanged(this.mSS);
                    }
                    HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
                    this.mAttachedRegistrants.notifyRegistrants();
                    if (hasCdmaDataConnectionDetached) {
                        this.mDetachedRegistrants.notifyRegistrants();
                    }
                    notifyDataRegStateRilRadioTechnologyChanged();
                    if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                        this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
                    } else {
                        needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
                    }
                    if (needNotifyData) {
                        this.mPhone.notifyDataConnection(null);
                    }
                    if (voiceRoaming) {
                        this.mVoiceRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasVoiceRoamingOff) {
                        this.mVoiceRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (dataRoaming) {
                        this.mDataRoamingOnRegistrants.notifyRegistrants();
                    }
                    if (hasDataRoamingOff) {
                        this.mDataRoamingOffRegistrants.notifyRegistrants();
                    }
                    if (hasLocationChanged) {
                        this.mPhone.notifyLocationChanged();
                    }
                }
            }
        }
        if (this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
            hasMultiApnSupport = VDBG;
        } else {
            if (this.mSS.getRilDataRadioTechnology() == EVENT_RADIO_AVAILABLE) {
            }
        }
        if (this.mNewSS.getRilDataRadioTechnology() < EVENT_POLL_STATE_REGISTRATION) {
            hasLostMultiApnSupport = VDBG;
        } else {
            if (this.mNewSS.getRilDataRadioTechnology() > 8) {
            }
        }
        if (this.mSS.getCssIndicator() == this.mNewSS.getCssIndicator()) {
        }
        tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        log("service state hasRegistered , poll signal strength at once");
        sendMessage(obtainMessage(EVENT_POLL_SIGNAL_STRENGTH));
        log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + voiceRoaming + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + dataRoaming + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
        if (this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState()) {
        }
        numArr = new Object[EVENT_POLL_STATE_REGISTRATION];
        numArr[PS_ONLY] = Integer.valueOf(this.mSS.getVoiceRegState());
        numArr[PS_CS] = Integer.valueOf(this.mSS.getDataRegState());
        numArr[OTASP_NEEDED] = Integer.valueOf(this.mNewSS.getVoiceRegState());
        numArr[OTASP_NOT_NEEDED] = Integer.valueOf(this.mNewSS.getDataRegState());
        EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
        if (!HwTelephonyFactory.getHwNetworkManager().proccessCdmaLteDelayUpdateRegisterStateDone(this, this.mPhone, this.mSS, this.mNewSS)) {
            tss = this.mSS;
            this.mSS = this.mNewSS;
            this.mNewSS = tss;
            this.mNewSS.setStateOutOfService();
            tcl = (CdmaCellLocation) this.mCellLoc;
            this.mCellLoc = this.mNewCellLoc;
            this.mNewCellLoc = tcl;
            this.mNewSS.setStateOutOfService();
            if (hasVoiceRadioTechnologyChanged) {
                updatePhoneObject();
            }
            if (hasDataRadioTechnologyChanged) {
                tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
                if (EVENT_LOCATION_UPDATES_ENABLED == this.mSS.getRilDataRadioTechnology()) {
                    log("pollStateDone: IWLAN enabled");
                }
            }
            if (hasRegistered) {
                this.mNetworkAttachedRegistrants.notifyRegistrants();
            }
            if (hasChanged) {
                if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                    if (this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) {
                    }
                } else {
                    hasBrandOverride = VDBG;
                }
                if (hasBrandOverride) {
                    if (this.mCi.getRadioState().isOn()) {
                        if (this.mPhone.isEriFileLoaded()) {
                            if (this.mSS.getRilVoiceRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                            }
                            if (this.mIsSubscriptionFromRuim) {
                                eriText = this.mSS.getOperatorAlphaLong();
                                if (this.mSS.getVoiceRegState() != 0) {
                                    if (this.mSS.getVoiceRegState() != OTASP_NOT_NEEDED) {
                                        if (this.mSS.getDataRegState() != 0) {
                                            eriText = this.mPhone.getContext().getText(17039589).toString();
                                        }
                                    } else {
                                        if (this.mIccRecords == null) {
                                        }
                                        if (TextUtils.isEmpty(eriText)) {
                                            eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                                        }
                                    }
                                } else {
                                    eriText = this.mPhone.getCdmaEriText();
                                }
                                this.mSS.setOperatorAlphaLong(eriText);
                            }
                        }
                    }
                }
                if (this.mSS.getVoiceRegState() != 0) {
                }
                if (this.mSS.getRilVoiceRadioTechnology() != EVENT_POLL_STATE_NETWORK_SELECTION_MODE) {
                    showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                    iconIndex = this.mSS.getCdmaEriIconIndex();
                    if (TextUtils.isEmpty(this.mIccRecords.getServiceProviderName())) {
                        this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                    }
                }
                tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
                prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
                operatorNumeric = this.mSS.getOperatorNumeric();
                if (isInvalidOperatorNumeric(operatorNumeric)) {
                    operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
                }
                tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
                updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                if (isInvalidOperatorNumeric(operatorNumeric)) {
                    isoCountryCode = "";
                    mcc = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                    this.mGotCountryCode = IGNORE_GOOGLE_NON_ROAMING;
                    setOperatorIdd(operatorNumeric);
                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                        fixTimeZone(isoCountryCode);
                    }
                } else {
                    log("operatorNumeric is null");
                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), "");
                    this.mGotCountryCode = VDBG;
                }
                if (this.mSS.getVoiceRoaming()) {
                }
                tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : IGNORE_GOOGLE_NON_ROAMING);
                updateSpnDisplay();
                setRoamingType(this.mSS);
                log("Broadcasting ServiceState : " + this.mSS);
                this.mPhone.notifyServiceStateChanged(this.mSS);
            }
            HwTelephonyFactory.getHwNetworkManager().processCdmaCTNumMatch(this, this.mPhone, this.mSS.getRoaming(), this.mUiccApplcation);
            this.mAttachedRegistrants.notifyRegistrants();
            if (hasCdmaDataConnectionDetached) {
                this.mDetachedRegistrants.notifyRegistrants();
            }
            notifyDataRegStateRilRadioTechnologyChanged();
            if (EVENT_LOCATION_UPDATES_ENABLED != this.mSS.getRilDataRadioTechnology()) {
                needNotifyData = IGNORE_GOOGLE_NON_ROAMING;
            } else {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            }
            if (needNotifyData) {
                this.mPhone.notifyDataConnection(null);
            }
            if (voiceRoaming) {
                this.mVoiceRoamingOnRegistrants.notifyRegistrants();
            }
            if (hasVoiceRoamingOff) {
                this.mVoiceRoamingOffRegistrants.notifyRegistrants();
            }
            if (dataRoaming) {
                this.mDataRoamingOnRegistrants.notifyRegistrants();
            }
            if (hasDataRoamingOff) {
                this.mDataRoamingOffRegistrants.notifyRegistrants();
            }
            if (hasLocationChanged) {
                this.mPhone.notifyLocationChanged();
            }
        }
    }

    private boolean isInHomeSidNid(int sid, int nid) {
        if (isSidsAllZeros() || this.mHomeSystemId.length != this.mHomeNetworkId.length || sid == 0) {
            return IGNORE_GOOGLE_NON_ROAMING;
        }
        int i = PS_ONLY;
        while (i < this.mHomeSystemId.length) {
            if (this.mHomeSystemId[i] == sid && (this.mHomeNetworkId[i] == 0 || this.mHomeNetworkId[i] == 65535 || nid == 0 || nid == CallFailCause.ERROR_UNSPECIFIED || this.mHomeNetworkId[i] == nid)) {
                return IGNORE_GOOGLE_NON_ROAMING;
            }
            i += PS_CS;
        }
        return VDBG;
    }

    protected void setOperatorIdd(String operatorNumeric) {
        if (PLUS_TRANFER_IN_MDOEM) {
            log("setOperatorIdd() return. because of PLUS_TRANFER_IN_MDOEM=" + PLUS_TRANFER_IN_MDOEM);
            return;
        }
        String idd = this.mHbpcdUtils.getIddByMcc(Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED)));
        if (idd == null || idd.isEmpty()) {
            this.mPhone.setSystemProperty("gsm.operator.idpstring", HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX);
        } else {
            this.mPhone.setSystemProperty("gsm.operator.idpstring", idd);
        }
    }

    protected boolean isInvalidOperatorNumeric(String operatorNumeric) {
        if (operatorNumeric == null || operatorNumeric.length() < OTASP_SIM_UNPROVISIONED) {
            return IGNORE_GOOGLE_NON_ROAMING;
        }
        return operatorNumeric.startsWith(INVALID_MCC);
    }

    protected String fixUnknownMcc(String operatorNumeric, int sid) {
        int i = PS_ONLY;
        if (sid <= 0) {
            return operatorNumeric;
        }
        boolean isNitzTimeZone = VDBG;
        int timeZone = PS_ONLY;
        if (this.mSavedTimeZone != null) {
            timeZone = TimeZone.getTimeZone(this.mSavedTimeZone).getRawOffset() / MS_PER_HOUR;
            isNitzTimeZone = IGNORE_GOOGLE_NON_ROAMING;
        } else {
            TimeZone tzone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            if (tzone != null) {
                timeZone = tzone.getRawOffset() / MS_PER_HOUR;
            }
        }
        HbpcdUtils hbpcdUtils = this.mHbpcdUtils;
        if (this.mZoneDst) {
            i = PS_CS;
        }
        int mcc = hbpcdUtils.getMcc(sid, timeZone, i, isNitzTimeZone);
        if (mcc > 0) {
            operatorNumeric = Integer.toString(mcc) + DEFAULT_MNC;
        }
        return operatorNumeric;
    }

    protected void fixTimeZone(String isoCountryCode) {
        fixTimeZone(isoCountryCode, null);
    }

    protected void fixTimeZone(String isoCountryCode, TimeZone mccZone) {
        TimeZone zone = mccZone;
        String zoneName = SystemProperties.get(TIMEZONE_PROPERTY);
        log("fixTimeZone zoneName='" + zoneName + "' mZoneOffset=" + this.mZoneOffset + " mZoneDst=" + this.mZoneDst + " iso-cc='" + isoCountryCode + "' iso-cc-idx=" + Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode));
        if ("".equals(isoCountryCode) && this.mNeedFixZoneAfterNitz) {
            zone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            log("pollStateDone: using NITZ TimeZone");
        } else if (this.mZoneOffset != 0 || this.mZoneDst || zoneName == null || zoneName.length() <= 0 || Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode) >= 0) {
            zone = TimeUtils.getTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime, isoCountryCode);
            log("fixTimeZone: using getTimeZone(off, dst, time, iso)");
            if (zone == null) {
                zone = TimeUtils.getTimeZone(this.mZoneOffset, this.mZoneDst ? VDBG : IGNORE_GOOGLE_NON_ROAMING, this.mZoneTime, isoCountryCode);
            }
        } else {
            if (mccZone == null) {
                zone = TimeZone.getDefault();
            }
            log("pollStateDone: default TimeZone is " + zone);
            if (this.mNeedFixZoneAfterNitz) {
                long ctm = System.currentTimeMillis();
                long tzOffset = (long) zone.getOffset(ctm);
                log("fixTimeZone: tzOffset=" + tzOffset + " ltod=" + TimeUtils.logTimeOfDay(ctm));
                if (getAutoTime()) {
                    long adj = ctm - tzOffset;
                    log("fixTimeZone: adj ltod=" + TimeUtils.logTimeOfDay(adj));
                    setAndBroadcastNetworkSetTime(adj);
                } else {
                    this.mSavedTime -= tzOffset;
                    log("fixTimeZone: adj mSavedTime=" + this.mSavedTime);
                }
            }
            log("fixTimeZone: using default TimeZone");
        }
        this.mNeedFixZoneAfterNitz = VDBG;
        if (zone != null) {
            log("fixTimeZone: zone != null zone.getID=" + zone.getID());
            if (getAutoTimeZone()) {
                setAndBroadcastNetworkSetTimeZone(zone.getID());
            } else {
                log("fixTimeZone: skip changing zone as getAutoTimeZone was false");
            }
            saveNitzTimeZone(zone.getID());
            return;
        }
        log("fixTimeZone: zone == null, do nothing for zone");
    }

    private boolean isGprsConsistent(int dataRegState, int voiceRegState) {
        return (voiceRegState != 0 || dataRegState == 0) ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
    }

    private TimeZone getNitzTimeZone(int offset, boolean dst, long when) {
        TimeZone guess = findTimeZone(offset, dst, when);
        if (guess == null) {
            guess = findTimeZone(offset, dst ? VDBG : IGNORE_GOOGLE_NON_ROAMING, when);
        }
        log("getNitzTimeZone returning " + (guess == null ? guess : guess.getID()));
        return guess;
    }

    private TimeZone findTimeZone(int offset, boolean dst, long when) {
        int rawOffset = offset;
        if (dst) {
            rawOffset = offset - MS_PER_HOUR;
        }
        String[] zones = TimeZone.getAvailableIDs(rawOffset);
        Date d = new Date(when);
        int length = zones.length;
        for (int i = PS_ONLY; i < length; i += PS_CS) {
            TimeZone tz = TimeZone.getTimeZone(zones[i]);
            if (tz.getOffset(when) == offset && tz.inDaylightTime(d) == dst) {
                return tz;
            }
        }
        return null;
    }

    private int regCodeToServiceState(int code) {
        switch (code) {
            case PS_ONLY /*0*/:
            case OTASP_NEEDED /*2*/:
            case OTASP_NOT_NEEDED /*3*/:
            case EVENT_POLL_STATE_REGISTRATION /*4*/:
            case EVENT_POLL_SIGNAL_STRENGTH /*10*/:
            case EVENT_SIGNAL_STRENGTH_UPDATE /*12*/:
            case EVENT_RADIO_AVAILABLE /*13*/:
            case EVENT_POLL_STATE_NETWORK_SELECTION_MODE /*14*/:
                return PS_CS;
            case PS_CS /*1*/:
            case OTASP_SIM_UNPROVISIONED /*5*/:
                return PS_ONLY;
            default:
                loge("regCodeToServiceState: unexpected service state " + code);
                return PS_CS;
        }
    }

    private boolean regCodeIsRoaming(int code) {
        return OTASP_SIM_UNPROVISIONED == code ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
    }

    private boolean isSameOperatorNameFromSimAndSS(ServiceState s) {
        String spn = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNameForPhone(getPhoneId());
        return !(!TextUtils.isEmpty(spn) ? spn.equalsIgnoreCase(s.getOperatorAlphaLong()) : VDBG) ? !TextUtils.isEmpty(spn) ? spn.equalsIgnoreCase(s.getOperatorAlphaShort()) : VDBG : IGNORE_GOOGLE_NON_ROAMING;
    }

    private boolean isSameNamedOperators(ServiceState s) {
        return currentMccEqualsSimMcc(s) ? isSameOperatorNameFromSimAndSS(s) : VDBG;
    }

    private boolean currentMccEqualsSimMcc(ServiceState s) {
        String simNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId());
        String operatorNumeric = s.getOperatorNumeric();
        boolean equalsMcc = IGNORE_GOOGLE_NON_ROAMING;
        try {
            equalsMcc = simNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED).equals(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED));
        } catch (Exception e) {
        }
        return equalsMcc;
    }

    private boolean isOperatorConsideredNonRoaming(ServiceState s) {
        return VDBG;
    }

    private boolean isOperatorConsideredRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        String[] numericArray = this.mPhone.getContext().getResources().getStringArray(17236028);
        if (numericArray.length == 0 || operatorNumeric == null) {
            return VDBG;
        }
        int length = numericArray.length;
        for (int i = PS_ONLY; i < length; i += PS_CS) {
            if (operatorNumeric.startsWith(numericArray[i])) {
                return IGNORE_GOOGLE_NON_ROAMING;
            }
        }
        return VDBG;
    }

    private static boolean checkForRoamingForIndianOperators(ServiceState s) {
        String simNumeric = SystemProperties.get("gsm.sim.operator.numeric", "");
        String operatorNumeric = s.getOperatorNumeric();
        try {
            String simMCC = simNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
            String operatorMCC = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
            if ((simMCC.equals("404") || simMCC.equals("405")) && (operatorMCC.equals("404") || operatorMCC.equals("405"))) {
                return IGNORE_GOOGLE_NON_ROAMING;
            }
        } catch (RuntimeException e) {
        }
        return VDBG;
    }

    private void onRestrictedStateChanged(AsyncResult ar) {
        boolean z = IGNORE_GOOGLE_NON_ROAMING;
        RestrictedState newRs = new RestrictedState();
        log("onRestrictedStateChanged: E rs " + this.mRestrictedState);
        if (ar.exception == null) {
            boolean z2;
            int state = ar.result[PS_ONLY];
            if ((state & PS_CS) != 0) {
                z2 = IGNORE_GOOGLE_NON_ROAMING;
            } else if ((state & EVENT_POLL_STATE_REGISTRATION) != 0) {
                z2 = IGNORE_GOOGLE_NON_ROAMING;
            } else {
                z2 = VDBG;
            }
            newRs.setCsEmergencyRestricted(z2);
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY) {
                if ((state & OTASP_NEEDED) != 0) {
                    z2 = IGNORE_GOOGLE_NON_ROAMING;
                } else if ((state & EVENT_POLL_STATE_REGISTRATION) != 0) {
                    z2 = IGNORE_GOOGLE_NON_ROAMING;
                } else {
                    z2 = VDBG;
                }
                newRs.setCsNormalRestricted(z2);
                if ((state & EVENT_SIM_RECORDS_LOADED) == 0) {
                    z = VDBG;
                }
                newRs.setPsRestricted(z);
            }
            log("onRestrictedStateChanged: new rs " + newRs);
            if (!this.mRestrictedState.isPsRestricted() && newRs.isPsRestricted()) {
                this.mPsRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(PS_ENABLED);
            } else if (this.mRestrictedState.isPsRestricted() && !newRs.isPsRestricted()) {
                this.mPsRestrictDisabledRegistrants.notifyRegistrants();
                setNotification(PS_DISABLED);
            }
            if (this.mRestrictedState.isCsRestricted()) {
                if (!newRs.isCsRestricted()) {
                    setNotification(CS_DISABLED);
                } else if (!newRs.isCsNormalRestricted()) {
                    setNotification(CS_EMERGENCY_ENABLED);
                } else if (!newRs.isCsEmergencyRestricted()) {
                    setNotification(CS_NORMAL_ENABLED);
                }
            } else if (!this.mRestrictedState.isCsEmergencyRestricted() || this.mRestrictedState.isCsNormalRestricted()) {
                if (this.mRestrictedState.isCsEmergencyRestricted() || !this.mRestrictedState.isCsNormalRestricted()) {
                    if (newRs.isCsRestricted()) {
                        setNotification(CS_ENABLED);
                    } else if (newRs.isCsEmergencyRestricted()) {
                        setNotification(CS_EMERGENCY_ENABLED);
                    } else if (newRs.isCsNormalRestricted()) {
                        setNotification(CS_NORMAL_ENABLED);
                    }
                } else if (!newRs.isCsRestricted()) {
                    setNotification(CS_DISABLED);
                } else if (newRs.isCsRestricted()) {
                    setNotification(CS_ENABLED);
                } else if (newRs.isCsEmergencyRestricted()) {
                    setNotification(CS_EMERGENCY_ENABLED);
                }
            } else if (!newRs.isCsRestricted()) {
                setNotification(CS_DISABLED);
            } else if (newRs.isCsRestricted()) {
                setNotification(CS_ENABLED);
            } else if (newRs.isCsNormalRestricted()) {
                setNotification(CS_NORMAL_ENABLED);
            }
            this.mRestrictedState = newRs;
        }
        log("onRestrictedStateChanged: X rs " + this.mRestrictedState);
    }

    public CellLocation getCellLocation() {
        if (((GsmCellLocation) this.mCellLoc).getLac() < 0 || ((GsmCellLocation) this.mCellLoc).getCid() < 0) {
            List<CellInfo> result = getAllCellInfo();
            if (result != null) {
                GsmCellLocation cellLocOther = new GsmCellLocation();
                for (CellInfo ci : result) {
                    if (ci instanceof CellInfoGsm) {
                        CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) ci).getCellIdentity();
                        cellLocOther.setLacAndCid(cellIdentityGsm.getLac(), cellIdentityGsm.getCid());
                        cellLocOther.setPsc(cellIdentityGsm.getPsc());
                        log("getCellLocation(): X ret GSM info=" + cellLocOther);
                        return cellLocOther;
                    } else if (ci instanceof CellInfoWcdma) {
                        CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) ci).getCellIdentity();
                        cellLocOther.setLacAndCid(cellIdentityWcdma.getLac(), cellIdentityWcdma.getCid());
                        cellLocOther.setPsc(cellIdentityWcdma.getPsc());
                        log("getCellLocation(): X ret WCDMA info=" + cellLocOther);
                        return cellLocOther;
                    } else if ((ci instanceof CellInfoLte) && (cellLocOther.getLac() < 0 || cellLocOther.getCid() < 0)) {
                        CellIdentityLte cellIdentityLte = ((CellInfoLte) ci).getCellIdentity();
                        if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                            cellLocOther.setLacAndCid(cellIdentityLte.getTac(), cellIdentityLte.getCi());
                            cellLocOther.setPsc(PS_ONLY);
                            log("getCellLocation(): possible LTE cellLocOther=" + cellLocOther);
                        }
                    }
                }
                log("getCellLocation(): X ret best answer cellLocOther=" + cellLocOther);
                return cellLocOther;
            }
            log("getCellLocation(): X empty mCellLoc and CellInfo mCellLoc=" + this.mCellLoc);
            return this.mCellLoc;
        }
        log("getCellLocation(): X good mCellLoc=" + this.mCellLoc);
        return this.mCellLoc;
    }

    private void setTimeFromNITZString(String nitz, long nitzReceiveTime) {
        long start = SystemClock.elapsedRealtime();
        log("NITZ: " + nitz + "," + nitzReceiveTime + " start=" + start + " delay=" + (start - nitzReceiveTime));
        long end;
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.clear();
            c.set(EVENT_SIM_RECORDS_LOADED, PS_ONLY);
            String[] nitzSubs = nitz.split("[/:,+-]");
            int year = Integer.parseInt(nitzSubs[PS_ONLY]) + NITZ_UPDATE_DIFF_DEFAULT;
            if (year > MAX_NITZ_YEAR) {
                loge("NITZ year: " + year + " exceeds limit, skip NITZ time update");
                return;
            }
            int dst;
            String ignore;
            long millisSinceNitzReceived;
            long gained;
            long timeSinceLastUpdate;
            int nitzUpdateSpacing;
            int nitzUpdateDiff;
            c.set(PS_CS, year);
            c.set(OTASP_NEEDED, Integer.parseInt(nitzSubs[PS_CS]) - 1);
            c.set(OTASP_SIM_UNPROVISIONED, Integer.parseInt(nitzSubs[OTASP_NEEDED]));
            c.set(EVENT_POLL_SIGNAL_STRENGTH, Integer.parseInt(nitzSubs[OTASP_NOT_NEEDED]));
            c.set(EVENT_SIGNAL_STRENGTH_UPDATE, Integer.parseInt(nitzSubs[EVENT_POLL_STATE_REGISTRATION]));
            c.set(EVENT_RADIO_AVAILABLE, Integer.parseInt(nitzSubs[OTASP_SIM_UNPROVISIONED]));
            boolean sign = nitz.indexOf(EVENT_CHANGE_IMS_STATE) == -1 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
            int tzOffset = Integer.parseInt(nitzSubs[EVENT_POLL_STATE_OPERATOR]);
            int length = nitzSubs.length;
            if (r0 >= 8) {
                dst = Integer.parseInt(nitzSubs[7]);
            } else {
                dst = PS_ONLY;
            }
            tzOffset = ((((sign ? PS_CS : -1) * tzOffset) * EVENT_GET_LOC_DONE) * 60) * CharacterSets.UCS2;
            TimeZone zone = null;
            length = nitzSubs.length;
            if (r0 >= 9) {
                zone = TimeZone.getTimeZone(nitzSubs[8].replace('!', '/'));
            }
            String iso = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
            if (zone == null && this.mGotCountryCode) {
                if (iso == null || iso.length() <= 0) {
                    zone = getNitzTimeZone(tzOffset, dst != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG, c.getTimeInMillis());
                } else {
                    zone = TimeUtils.getTimeZone(tzOffset, dst != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG, c.getTimeInMillis(), iso);
                }
            }
            if (zone != null) {
                length = this.mZoneOffset;
                if (r0 == tzOffset) {
                    if (this.mZoneDst != (dst != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG)) {
                    }
                    log("NITZ: tzOffset=" + tzOffset + " dst=" + dst + " zone=" + (zone == null ? zone.getID() : "NULL") + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
                    if (zone != null) {
                        if (getAutoTimeZone()) {
                            setAndBroadcastNetworkSetTimeZone(zone.getID());
                        }
                        saveNitzTimeZone(zone.getID());
                    }
                    ignore = SystemProperties.get("gsm.ignore-nitz");
                    if (ignore != null) {
                        if (ignore.equals("yes")) {
                            log("NITZ: Not setting clock because gsm.ignore-nitz is set");
                            return;
                        }
                    }
                    this.mWakeLock.acquire();
                    if (!this.mPhone.isPhoneTypeGsm() || getAutoTime()) {
                        millisSinceNitzReceived = SystemClock.elapsedRealtime() - nitzReceiveTime;
                        if (millisSinceNitzReceived < 0) {
                            log("NITZ: not setting time, clock has rolled backwards since NITZ time was received, " + nitz);
                            end = SystemClock.elapsedRealtime();
                            log("NITZ: end=" + end + " dur=" + (end - start));
                            this.mWakeLock.release();
                        } else if (millisSinceNitzReceived <= 2147483647L) {
                            log("NITZ: not setting time, processing has taken " + (millisSinceNitzReceived / 86400000) + " days");
                            end = SystemClock.elapsedRealtime();
                            log("NITZ: end=" + end + " dur=" + (end - start));
                            this.mWakeLock.release();
                        } else {
                            c.add(EVENT_POLL_STATE_NETWORK_SELECTION_MODE, (int) millisSinceNitzReceived);
                            log("NITZ: Setting time of day to " + c.getTime() + " NITZ receive delay(ms): " + millisSinceNitzReceived + " gained(ms): " + (c.getTimeInMillis() - System.currentTimeMillis()) + " from " + nitz);
                            if (this.mPhone.isPhoneTypeGsm()) {
                                if (getAutoTime()) {
                                    gained = c.getTimeInMillis() - System.currentTimeMillis();
                                    timeSinceLastUpdate = SystemClock.elapsedRealtime() - this.mSavedAtTime;
                                    nitzUpdateSpacing = Global.getInt(this.mCr, "nitz_update_spacing", this.mNitzUpdateSpacing);
                                    nitzUpdateDiff = Global.getInt(this.mCr, "nitz_update_diff", this.mNitzUpdateDiff);
                                    if (this.mSavedAtTime != 0) {
                                        if (timeSinceLastUpdate <= ((long) nitzUpdateSpacing)) {
                                            if (Math.abs(gained) <= ((long) nitzUpdateDiff)) {
                                                log("NITZ: ignore, a previous update was " + timeSinceLastUpdate + "ms ago and gained=" + gained + "ms");
                                                end = SystemClock.elapsedRealtime();
                                                log("NITZ: end=" + end + " dur=" + (end - start));
                                                this.mWakeLock.release();
                                                return;
                                            }
                                        }
                                    }
                                    log("NITZ: Auto updating time of day to " + c.getTime() + " NITZ receive delay=" + millisSinceNitzReceived + "ms gained=" + gained + "ms from " + nitz);
                                    setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                                }
                            } else if (Math.abs(c.getTimeInMillis() - System.currentTimeMillis()) < 3000) {
                                setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                                Rlog.i(this.LOG_TAG, "NITZ: Setting time ");
                            } else {
                                Rlog.i(this.LOG_TAG, "NITZ: skip Setting time");
                            }
                        }
                    }
                    SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                    SystemProperties.set("gsm.nitz.timereference", String.valueOf(SystemClock.elapsedRealtime()));
                    saveNitzTime(c.getTimeInMillis());
                    this.mNitzUpdatedTime = IGNORE_GOOGLE_NON_ROAMING;
                    end = SystemClock.elapsedRealtime();
                    log("NITZ: end=" + end + " dur=" + (end - start));
                    this.mWakeLock.release();
                }
            }
            this.mNeedFixZoneAfterNitz = IGNORE_GOOGLE_NON_ROAMING;
            this.mZoneOffset = tzOffset;
            this.mZoneDst = dst != 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
            this.mZoneTime = c.getTimeInMillis();
            if (zone == null) {
            }
            log("NITZ: tzOffset=" + tzOffset + " dst=" + dst + " zone=" + (zone == null ? zone.getID() : "NULL") + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
            if (zone != null) {
                if (getAutoTimeZone()) {
                    setAndBroadcastNetworkSetTimeZone(zone.getID());
                }
                saveNitzTimeZone(zone.getID());
            }
            ignore = SystemProperties.get("gsm.ignore-nitz");
            if (ignore != null) {
                if (ignore.equals("yes")) {
                    log("NITZ: Not setting clock because gsm.ignore-nitz is set");
                    return;
                }
            }
            this.mWakeLock.acquire();
            millisSinceNitzReceived = SystemClock.elapsedRealtime() - nitzReceiveTime;
            if (millisSinceNitzReceived < 0) {
                log("NITZ: not setting time, clock has rolled backwards since NITZ time was received, " + nitz);
                end = SystemClock.elapsedRealtime();
                log("NITZ: end=" + end + " dur=" + (end - start));
                this.mWakeLock.release();
            } else if (millisSinceNitzReceived <= 2147483647L) {
                c.add(EVENT_POLL_STATE_NETWORK_SELECTION_MODE, (int) millisSinceNitzReceived);
                log("NITZ: Setting time of day to " + c.getTime() + " NITZ receive delay(ms): " + millisSinceNitzReceived + " gained(ms): " + (c.getTimeInMillis() - System.currentTimeMillis()) + " from " + nitz);
                if (this.mPhone.isPhoneTypeGsm()) {
                    if (getAutoTime()) {
                        gained = c.getTimeInMillis() - System.currentTimeMillis();
                        timeSinceLastUpdate = SystemClock.elapsedRealtime() - this.mSavedAtTime;
                        nitzUpdateSpacing = Global.getInt(this.mCr, "nitz_update_spacing", this.mNitzUpdateSpacing);
                        nitzUpdateDiff = Global.getInt(this.mCr, "nitz_update_diff", this.mNitzUpdateDiff);
                        if (this.mSavedAtTime != 0) {
                            if (timeSinceLastUpdate <= ((long) nitzUpdateSpacing)) {
                                if (Math.abs(gained) <= ((long) nitzUpdateDiff)) {
                                    log("NITZ: ignore, a previous update was " + timeSinceLastUpdate + "ms ago and gained=" + gained + "ms");
                                    end = SystemClock.elapsedRealtime();
                                    log("NITZ: end=" + end + " dur=" + (end - start));
                                    this.mWakeLock.release();
                                    return;
                                }
                            }
                        }
                        log("NITZ: Auto updating time of day to " + c.getTime() + " NITZ receive delay=" + millisSinceNitzReceived + "ms gained=" + gained + "ms from " + nitz);
                        setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                    }
                    SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                    SystemProperties.set("gsm.nitz.timereference", String.valueOf(SystemClock.elapsedRealtime()));
                    saveNitzTime(c.getTimeInMillis());
                    this.mNitzUpdatedTime = IGNORE_GOOGLE_NON_ROAMING;
                    end = SystemClock.elapsedRealtime();
                    log("NITZ: end=" + end + " dur=" + (end - start));
                    this.mWakeLock.release();
                }
                if (Math.abs(c.getTimeInMillis() - System.currentTimeMillis()) < 3000) {
                    Rlog.i(this.LOG_TAG, "NITZ: skip Setting time");
                } else {
                    setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                    Rlog.i(this.LOG_TAG, "NITZ: Setting time ");
                }
                SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                SystemProperties.set("gsm.nitz.timereference", String.valueOf(SystemClock.elapsedRealtime()));
                saveNitzTime(c.getTimeInMillis());
                this.mNitzUpdatedTime = IGNORE_GOOGLE_NON_ROAMING;
                end = SystemClock.elapsedRealtime();
                log("NITZ: end=" + end + " dur=" + (end - start));
                this.mWakeLock.release();
            } else {
                log("NITZ: not setting time, processing has taken " + (millisSinceNitzReceived / 86400000) + " days");
                end = SystemClock.elapsedRealtime();
                log("NITZ: end=" + end + " dur=" + (end - start));
                this.mWakeLock.release();
            }
        } catch (RuntimeException ex) {
            loge("NITZ: Parsing NITZ time " + nitz + " ex=" + ex);
        } catch (Throwable th) {
            end = SystemClock.elapsedRealtime();
            log("NITZ: end=" + end + " dur=" + (end - start));
            this.mWakeLock.release();
        }
    }

    private boolean getAutoTime() {
        boolean z = IGNORE_GOOGLE_NON_ROAMING;
        try {
            if (Global.getInt(this.mCr, "auto_time") <= 0) {
                z = VDBG;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return IGNORE_GOOGLE_NON_ROAMING;
        }
    }

    private boolean getAutoTimeZone() {
        boolean z = IGNORE_GOOGLE_NON_ROAMING;
        try {
            if (Global.getInt(this.mCr, "auto_time_zone") <= 0) {
                z = VDBG;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return IGNORE_GOOGLE_NON_ROAMING;
        }
    }

    private void saveNitzTimeZone(String zoneId) {
        this.mSavedTimeZone = zoneId;
        HwTelephonyFactory.getHwNetworkManager().saveNitzTimeZoneToDB(this.mCr, zoneId);
    }

    private void saveNitzTime(long time) {
        this.mSavedTime = time;
        this.mSavedAtTime = SystemClock.elapsedRealtime();
    }

    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        log("setAndBroadcastNetworkSetTimeZone: setTimeZone=" + zoneId);
        ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).setTimeZone(zoneId);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIMEZONE");
        intent.addFlags(536870912);
        intent.putExtra("time-zone", zoneId);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        log("setAndBroadcastNetworkSetTimeZone: call alarm.setTimeZone and broadcast zoneId=" + zoneId);
    }

    private void setAndBroadcastNetworkSetTime(long time) {
        log("setAndBroadcastNetworkSetTime: time=" + time + "ms");
        SystemClock.setCurrentTimeMillis(time);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIME");
        intent.addFlags(536870912);
        intent.putExtra("time", time);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void revertToNitzTime() {
        if (Global.getInt(this.mCr, "auto_time", PS_ONLY) != 0) {
            log("Reverting to NITZ Time: mSavedTime=" + this.mSavedTime + " mSavedAtTime=" + this.mSavedAtTime);
            if (!(this.mSavedTime == 0 || this.mSavedAtTime == 0)) {
                setAndBroadcastNetworkSetTime(this.mSavedTime + (SystemClock.elapsedRealtime() - this.mSavedAtTime));
            }
        }
    }

    private void revertToNitzTimeZone() {
        if (Global.getInt(this.mCr, "auto_time_zone", PS_ONLY) != 0) {
            log("Reverting to NITZ TimeZone: tz='" + this.mSavedTimeZone);
            if (this.mSavedTimeZone != null) {
                setAndBroadcastNetworkSetTimeZone(this.mSavedTimeZone);
            }
        }
    }

    private void setNotification(int notifyType) {
        if (SystemProperties.getBoolean("ro.hwpp.cell_access_report", VDBG)) {
            log("setNotification: create notification " + notifyType);
            if (this.mPhone.getContext().getResources().getBoolean(17956958)) {
                Context context = this.mPhone.getContext();
                CharSequence details = "";
                CharSequence title = context.getText(17039556);
                int notificationId = CS_NOTIFICATION;
                switch (notifyType) {
                    case PS_ENABLED /*1001*/:
                        if (((long) SubscriptionManager.getDefaultDataSubscriptionId()) == ((long) this.mPhone.getSubId())) {
                            notificationId = PS_NOTIFICATION;
                            details = context.getText(17039556);
                            break;
                        }
                        return;
                    case PS_DISABLED /*1002*/:
                        notificationId = PS_NOTIFICATION;
                        break;
                    case CS_ENABLED /*1003*/:
                        details = context.getText(17039559);
                        break;
                    case CS_NORMAL_ENABLED /*1005*/:
                        details = context.getText(17039558);
                        break;
                    case CS_EMERGENCY_ENABLED /*1006*/:
                        details = context.getText(17039557);
                        break;
                }
                log("setNotification: put notification " + title + " / " + details);
                this.mNotification = new Builder(context).setWhen(System.currentTimeMillis()).setAutoCancel(IGNORE_GOOGLE_NON_ROAMING).setSmallIcon(17301642).setTicker(title).setColor(context.getResources().getColor(17170519)).setContentTitle(title).setContentText(details).build();
                NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                if (notifyType == PS_DISABLED || notifyType == CS_DISABLED) {
                    notificationManager.cancel(notificationId);
                } else {
                    notificationManager.notify(notificationId, this.mNotification);
                }
                return;
            }
            log("Ignore all the notifications");
        }
    }

    private UiccCardApplication getUiccCardApplication() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), PS_CS);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), OTASP_NEEDED);
    }

    private void queueNextSignalStrengthPoll() {
        if (!this.mDontPollSignalStrength && this.mDefaultDisplayState == OTASP_NEEDED) {
            Message msg = obtainMessage();
            msg.what = EVENT_POLL_SIGNAL_STRENGTH;
            sendMessageDelayed(msg, 20000);
        }
    }

    private void notifyCdmaSubscriptionInfoReady() {
        if (this.mCdmaForSubscriptionInfoReadyRegistrants != null) {
            log("CDMA_SUBSCRIPTION: call notifyRegistrants()");
            this.mCdmaForSubscriptionInfoReadyRegistrants.notifyRegistrants();
        }
    }

    public void registerForDataConnectionAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mAttachedRegistrants.add(r);
        if (getCurrentDataConnectionState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionAttached(Handler h) {
        this.mAttachedRegistrants.remove(h);
    }

    public void registerForDataConnectionDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDetachedRegistrants.add(r);
        if (getCurrentDataConnectionState() != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionDetached(Handler h) {
        this.mDetachedRegistrants.remove(h);
    }

    public void registerForDataRegStateOrRatChanged(Handler h, int what, Object obj) {
        this.mDataRegStateOrRatChangedRegistrants.add(new Registrant(h, what, obj));
        notifyDataRegStateRilRadioTechnologyChanged();
    }

    public void unregisterForDataRegStateOrRatChanged(Handler h) {
        this.mDataRegStateOrRatChangedRegistrants.remove(h);
    }

    public void registerForNetworkAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mNetworkAttachedRegistrants.add(r);
        if (this.mSS.getVoiceRegState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkAttached(Handler h) {
        this.mNetworkAttachedRegistrants.remove(h);
    }

    public void registerForPsRestrictedEnabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictEnabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedEnabled(Handler h) {
        this.mPsRestrictEnabledRegistrants.remove(h);
    }

    public void registerForPsRestrictedDisabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictDisabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedDisabled(Handler h) {
        this.mPsRestrictDisabledRegistrants.remove(h);
    }

    public void powerOffRadioSafely(DcTracker dcTracker) {
        synchronized (this) {
            if (RESET_PROFILE && this.mPhone.getContext() != null && Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", -1) == 0) {
                Rlog.d(this.LOG_TAG, "powerOffRadioSafely, it is not airplaneMode, resetProfile.");
                this.mCi.resetProfile(null);
            }
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                boolean isDisconnected;
                Message msg;
                int i;
                if (this.mPhone.isPhoneTypeGsm() || this.mPhone.isPhoneTypeCdmaLte()) {
                    int dds = SubscriptionManager.getDefaultDataSubscriptionId();
                    isDisconnected = VDBG;
                    if (HW_FAST_SET_RADIO_OFF) {
                        isDisconnected = dcTracker.isDisconnectedOrConnecting();
                    }
                    if (isDisconnected || (dcTracker.isDisconnected() && (dds == this.mPhone.getSubId() || ((dds != this.mPhone.getSubId() && ProxyController.getInstance().isDataDisconnected(dds)) || !SubscriptionManager.isValidSubscriptionId(dds))))) {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        log("Data disconnected, turn off radio right away.");
                        hangupAndPowerOff();
                    } else {
                        if (this.mPhone.isInCall()) {
                            this.mPhone.mCT.mRingingCall.hangupIfAlive();
                            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
                            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
                        }
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        if (!(dds == this.mPhone.getSubId() || ProxyController.getInstance().isDataDisconnected(dds))) {
                            log("Data is active on DDS.  Wait for all data disconnect");
                            ProxyController.getInstance().registerForAllDataDisconnected(dds, this, EVENT_ALL_DATA_DISCONNECTED, null);
                            this.mPendingRadioPowerOffAfterDataOff = IGNORE_GOOGLE_NON_ROAMING;
                        }
                        msg = Message.obtain(this);
                        msg.what = EVENT_SET_RADIO_POWER_OFF;
                        i = this.mPendingRadioPowerOffAfterDataOffTag + PS_CS;
                        this.mPendingRadioPowerOffAfterDataOffTag = i;
                        msg.arg1 = i;
                        if (sendMessageDelayed(msg, 30000)) {
                            log("Wait upto 30s for data to disconnect, then turn off radio.");
                            acquireWakeLock();
                            this.mPendingRadioPowerOffAfterDataOff = IGNORE_GOOGLE_NON_ROAMING;
                        } else {
                            log("Cannot send delayed Msg, turn off radio right away.");
                            hangupAndPowerOff();
                            this.mPendingRadioPowerOffAfterDataOff = VDBG;
                        }
                    }
                } else {
                    String[] networkNotClearData = this.mPhone.getContext().getResources().getStringArray(17236037);
                    String currentNetwork = this.mSS.getOperatorNumeric();
                    if (!(networkNotClearData == null || currentNetwork == null)) {
                        for (int i2 = PS_ONLY; i2 < networkNotClearData.length; i2 += PS_CS) {
                            if (currentNetwork.equals(networkNotClearData[i2])) {
                                log("Not disconnecting data for " + currentNetwork);
                                hangupAndPowerOff();
                                return;
                            }
                        }
                    }
                    if (HW_FAST_SET_RADIO_OFF) {
                        isDisconnected = dcTracker.isDisconnectedOrConnecting();
                    } else {
                        isDisconnected = dcTracker.isDisconnected();
                    }
                    if (isDisconnected) {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        log("Data disconnected, turn off radio right away.");
                        hangupAndPowerOff();
                    } else {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        msg = Message.obtain(this);
                        msg.what = EVENT_SET_RADIO_POWER_OFF;
                        i = this.mPendingRadioPowerOffAfterDataOffTag + PS_CS;
                        this.mPendingRadioPowerOffAfterDataOffTag = i;
                        msg.arg1 = i;
                        if (sendMessageDelayed(msg, 30000)) {
                            log("Wait upto 30s for data to disconnect, then turn off radio.");
                            acquireWakeLock();
                            this.mPendingRadioPowerOffAfterDataOff = IGNORE_GOOGLE_NON_ROAMING;
                        } else {
                            log("Cannot send delayed Msg, turn off radio right away.");
                            hangupAndPowerOff();
                        }
                    }
                }
            }
        }
    }

    public boolean processPendingRadioPowerOffAfterDataOff() {
        synchronized (this) {
            if (this.mPendingRadioPowerOffAfterDataOff) {
                HwTelephonyFactory.getHwNetworkManager().delaySendDetachAfterDataOff(this.mPhone);
                this.mPendingRadioPowerOffAfterDataOffTag += PS_CS;
                hangupAndPowerOff();
                this.mPendingRadioPowerOffAfterDataOff = VDBG;
                return IGNORE_GOOGLE_NON_ROAMING;
            }
            return VDBG;
        }
    }

    protected boolean onSignalStrengthResult(AsyncResult ar) {
        boolean isGsm = VDBG;
        if (this.mPhone.isPhoneTypeGsm() || (this.mPhone.isPhoneTypeCdmaLte() && this.mSS.getRilDataRadioTechnology() == EVENT_POLL_STATE_NETWORK_SELECTION_MODE)) {
            isGsm = IGNORE_GOOGLE_NON_ROAMING;
        }
        if (FEATURE_DELAY_UPDATE_SIGANL_STENGTH) {
            return onSignalStrengthResultHW(ar, isGsm);
        }
        if (ar.exception != null || ar.result == null) {
            log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            this.mSignalStrength = new SignalStrength(isGsm);
        } else {
            this.mSignalStrength = (SignalStrength) ar.result;
            this.mSignalStrength.validateInput();
            this.mSignalStrength.setGsm(isGsm);
        }
        HwTelephonyFactory.getHwNetworkManager().updateHwnff(this, this.mSignalStrength);
        return notifySignalStrength();
    }

    protected boolean onSignalStrengthResultHW(AsyncResult ar, boolean isGsm) {
        SignalStrength newSignalStrength;
        SignalStrength oldSignalStrength = this.mSignalStrength;
        if (ar.exception != null || ar.result == null) {
            log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            newSignalStrength = new SignalStrength(isGsm);
        } else {
            newSignalStrength = ar.result;
            newSignalStrength.validateInput();
            newSignalStrength.setGsm(isGsm);
        }
        HwTelephonyFactory.getHwNetworkManager().updateHwnff(this, newSignalStrength);
        if (this.mPhone.isPhoneTypeGsm()) {
            return HwTelephonyFactory.getHwNetworkManager().notifyGsmSignalStrength(this, this.mPhone, oldSignalStrength, newSignalStrength);
        }
        newSignalStrength.setCdma(IGNORE_GOOGLE_NON_ROAMING);
        return HwTelephonyFactory.getHwNetworkManager().notifyCdmaSignalStrength(this, this.mPhone, oldSignalStrength, newSignalStrength);
    }

    protected void hangupAndPowerOff() {
        if (!this.mPhone.isPhoneTypeGsm() || this.mPhone.isInCall()) {
            this.mPhone.mCT.mRingingCall.hangupIfAlive();
            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
        }
        this.mCi.setRadioPower(VDBG, null);
    }

    protected void cancelPollState() {
        this.mPollingContext = new int[PS_CS];
    }

    protected boolean shouldFixTimeZoneNow(Phone phone, String operatorNumeric, String prevOperatorNumeric, boolean needToFixTimeZone) {
        try {
            int prevMcc;
            int mcc = Integer.parseInt(operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED));
            try {
                if (TextUtils.isEmpty(prevOperatorNumeric)) {
                    prevMcc = mcc + PS_CS;
                } else {
                    prevMcc = Integer.parseInt(prevOperatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED));
                }
            } catch (Exception e) {
                prevMcc = mcc + PS_CS;
            }
            boolean iccCardExist = VDBG;
            if (this.mUiccApplcation != null) {
                iccCardExist = this.mUiccApplcation.getState() != AppState.APPSTATE_UNKNOWN ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
            }
            boolean z = (!iccCardExist || mcc == prevMcc) ? needToFixTimeZone : IGNORE_GOOGLE_NON_ROAMING;
            log("shouldFixTimeZoneNow: retVal=" + z + " iccCardExist=" + iccCardExist + " operatorNumeric=" + operatorNumeric + " mcc=" + mcc + " prevOperatorNumeric=" + prevOperatorNumeric + " prevMcc=" + prevMcc + " needToFixTimeZone=" + needToFixTimeZone + " ltod=" + TimeUtils.logTimeOfDay(System.currentTimeMillis()));
            return z;
        } catch (Exception e2) {
            log("shouldFixTimeZoneNow: no mcc, operatorNumeric=" + operatorNumeric + " retVal=false");
            return VDBG;
        }
    }

    public String getSystemProperty(String property, String defValue) {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), property, defValue);
    }

    public List<CellInfo> getAllCellInfo() {
        CellInfoResult result = new CellInfoResult();
        if (this.mCi.getRilVersion() < 8) {
            log("SST.getAllCellInfo(): not implemented");
            result.list = null;
        } else if (!isCallerOnDifferentThread()) {
            log("SST.getAllCellInfo(): return last, same thread can't block");
            result.list = this.mLastCellInfoList;
        } else if (SystemClock.elapsedRealtime() - this.mLastCellInfoListTime > LAST_CELL_INFO_LIST_MAX_AGE_MS) {
            Message msg = obtainMessage(EVENT_GET_CELL_INFO_LIST, result);
            synchronized (result.lockObj) {
                result.list = null;
                this.mCi.getCellInfoList(msg);
                try {
                    result.lockObj.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            log("SST.getAllCellInfo(): return last, back to back calls");
            result.list = this.mLastCellInfoList;
        }
        synchronized (result.lockObj) {
            if (result.list != null) {
                List<CellInfo> list = result.list;
                return list;
            }
            log("SST.getAllCellInfo(): X size=0 list=null");
            return null;
        }
    }

    public SignalStrength getSignalStrength() {
        return this.mSignalStrength;
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mCdmaForSubscriptionInfoReadyRegistrants.add(r);
        if (isMinInfoReady()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mCdmaForSubscriptionInfoReadyRegistrants.remove(h);
    }

    private void saveCdmaSubscriptionSource(int source) {
        log("Storing cdma subscription source: " + source);
        Global.putInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", source);
        log("Read from settings: " + Global.getInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", -1));
    }

    private void getSubscriptionInfoAndStartPollingThreads() {
        this.mCi.getCDMASubscription(obtainMessage(EVENT_POLL_STATE_CDMA_SUBSCRIPTION));
        pollState();
    }

    private void handleCdmaSubscriptionSource(int newSubscriptionSource) {
        boolean z = VDBG;
        log("Subscription Source : " + newSubscriptionSource);
        if (newSubscriptionSource == 0) {
            z = IGNORE_GOOGLE_NON_ROAMING;
        }
        this.mIsSubscriptionFromRuim = z;
        log("isFromRuim: " + this.mIsSubscriptionFromRuim);
        saveCdmaSubscriptionSource(newSubscriptionSource);
        if (this.mIsSubscriptionFromRuim) {
            registerForRuimEvents();
            return;
        }
        unregisterForRuimEvents();
        sendMessage(obtainMessage(EVENT_NV_READY));
    }

    private void registerForRuimEvents() {
        log("registerForRuimEvents");
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.registerForReady(this, EVENT_RUIM_READY, null);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.registerForRecordsLoaded(this, EVENT_RUIM_RECORDS_LOADED, null);
        }
    }

    private void unregisterForRuimEvents() {
        log("unregisterForRuimEvents");
        if (this.mUiccApplcation != null) {
            this.mUiccApplcation.unregisterForReady(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForRecordsLoaded(this);
        }
    }

    public void setSignalStrength(SignalStrength signalStrength) {
        log("setSignalStrength : " + signalStrength);
        this.mSignalStrength = signalStrength;
    }

    public void setDoRecoveryTriggerState(boolean state) {
        this.mRadioOffByDoRecovery = state;
        log("setDoRecoveryTriggerState, " + this.mRadioOffByDoRecovery);
    }

    public boolean getDoRecoveryTriggerState() {
        return this.mRadioOffByDoRecovery;
    }

    public void setDoRecoveryMarker(boolean state) {
        this.mDoRecoveryMarker = state;
        log("setDoRecoveryMarker, " + this.mDoRecoveryMarker);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ServiceStateTracker:");
        pw.println(" mSubId=" + this.mSubId);
        pw.println(" mSS=" + this.mSS);
        pw.println(" mNewSS=" + this.mNewSS);
        pw.println(" mVoiceCapable=" + this.mVoiceCapable);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPollingContext=" + this.mPollingContext + " - " + (this.mPollingContext != null ? Integer.valueOf(this.mPollingContext[PS_ONLY]) : ""));
        pw.println(" mDesiredPowerState=" + this.mDesiredPowerState);
        pw.println(" mDontPollSignalStrength=" + this.mDontPollSignalStrength);
        pw.println(" mSignalStrength=" + this.mSignalStrength);
        pw.println(" mLastSignalStrength=" + this.mLastSignalStrength);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPendingRadioPowerOffAfterDataOff=" + this.mPendingRadioPowerOffAfterDataOff);
        pw.println(" mPendingRadioPowerOffAfterDataOffTag=" + this.mPendingRadioPowerOffAfterDataOffTag);
        pw.println(" mCellLoc=" + this.mCellLoc);
        pw.println(" mNewCellLoc=" + this.mNewCellLoc);
        pw.println(" mLastCellInfoListTime=" + this.mLastCellInfoListTime);
        pw.println(" mPreferredNetworkType=" + this.mPreferredNetworkType);
        pw.println(" mMaxDataCalls=" + this.mMaxDataCalls);
        pw.println(" mNewMaxDataCalls=" + this.mNewMaxDataCalls);
        pw.println(" mReasonDataDenied=" + this.mReasonDataDenied);
        pw.println(" mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        pw.println(" mGsmRoaming=" + this.mGsmRoaming);
        pw.println(" mDataRoaming=" + this.mDataRoaming);
        pw.println(" mEmergencyOnly=" + this.mEmergencyOnly);
        pw.println(" mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
        pw.flush();
        pw.println(" mZoneOffset=" + this.mZoneOffset);
        pw.println(" mZoneDst=" + this.mZoneDst);
        pw.println(" mZoneTime=" + this.mZoneTime);
        pw.println(" mGotCountryCode=" + this.mGotCountryCode);
        pw.println(" mNitzUpdatedTime=" + this.mNitzUpdatedTime);
        pw.println(" mSavedTimeZone=" + this.mSavedTimeZone);
        pw.println(" mSavedTime=" + this.mSavedTime);
        pw.println(" mSavedAtTime=" + this.mSavedAtTime);
        pw.println(" mStartedGprsRegCheck=" + this.mStartedGprsRegCheck);
        pw.println(" mReportedGprsNoReg=" + this.mReportedGprsNoReg);
        pw.println(" mNotification=" + this.mNotification);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mCurSpn=" + this.mCurSpn);
        pw.println(" mCurDataSpn=" + this.mCurDataSpn);
        pw.println(" mCurShowSpn=" + this.mCurShowSpn);
        pw.println(" mCurPlmn=" + this.mCurPlmn);
        pw.println(" mCurShowPlmn=" + this.mCurShowPlmn);
        pw.flush();
        pw.println(" mCurrentOtaspMode=" + this.mCurrentOtaspMode);
        pw.println(" mRoamingIndicator=" + this.mRoamingIndicator);
        pw.println(" mIsInPrl=" + this.mIsInPrl);
        pw.println(" mDefaultRoamingIndicator=" + this.mDefaultRoamingIndicator);
        pw.println(" mRegistrationState=" + this.mRegistrationState);
        pw.println(" mMdn=" + this.mMdn);
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.println(" mMin=" + this.mMin);
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mIsMinInfoReady=" + this.mIsMinInfoReady);
        pw.println(" mIsEriTextLoaded=" + this.mIsEriTextLoaded);
        pw.println(" mIsSubscriptionFromRuim=" + this.mIsSubscriptionFromRuim);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mRegistrationDeniedReason=" + this.mRegistrationDeniedReason);
        pw.println(" mCurrentCarrier=" + this.mCurrentCarrier);
        pw.flush();
        pw.println(" mImsRegistered=" + this.mImsRegistered);
        pw.println(" mImsRegistrationOnOff=" + this.mImsRegistrationOnOff);
        pw.println(" mAlarmSwitch=" + this.mAlarmSwitch);
        pw.println(" mPowerOffDelayNeed=" + this.mPowerOffDelayNeed);
        pw.println(" mDeviceShuttingDown=" + this.mDeviceShuttingDown);
        pw.println(" mSpnUpdatePending=" + this.mSpnUpdatePending);
    }

    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    protected void checkCorrectThread() {
        if (Thread.currentThread() != getLooper().getThread()) {
            throw new RuntimeException("ServiceStateTracker must be used from within one thread");
        }
    }

    protected boolean isCallerOnDifferentThread() {
        return Thread.currentThread() != getLooper().getThread() ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
    }

    protected void updateCarrierMccMncConfiguration(String newOp, String oldOp, Context context) {
        if (newOp != null || TextUtils.isEmpty(oldOp)) {
            if (newOp == null || newOp.equals(oldOp)) {
                return;
            }
        }
        log("update mccmnc=" + newOp + " fromServiceState=true");
        MccTable.updateMccMncConfiguration(context, newOp, IGNORE_GOOGLE_NON_ROAMING);
    }

    protected boolean inSameCountry(String operatorNumeric) {
        if (TextUtils.isEmpty(operatorNumeric) || operatorNumeric.length() < OTASP_SIM_UNPROVISIONED) {
            return VDBG;
        }
        String homeNumeric = getHomeOperatorNumeric();
        if (TextUtils.isEmpty(homeNumeric) || homeNumeric.length() < OTASP_SIM_UNPROVISIONED) {
            return VDBG;
        }
        String networkMCC = operatorNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
        String homeMCC = homeNumeric.substring(PS_ONLY, OTASP_NOT_NEEDED);
        String networkCountry = "";
        String homeCountry = "";
        try {
            networkCountry = MccTable.countryCodeForMcc(Integer.parseInt(networkMCC));
            homeCountry = MccTable.countryCodeForMcc(Integer.parseInt(homeMCC));
        } catch (NumberFormatException ex) {
            log("inSameCountry: get networkCountry or homeCountry error: " + ex);
        }
        if (networkCountry.isEmpty() || homeCountry.isEmpty()) {
            return VDBG;
        }
        boolean inSameCountry = homeCountry.equals(networkCountry);
        if (inSameCountry) {
            return inSameCountry;
        }
        if ("us".equals(homeCountry) && "vi".equals(networkCountry)) {
            inSameCountry = IGNORE_GOOGLE_NON_ROAMING;
        } else if ("vi".equals(homeCountry) && "us".equals(networkCountry)) {
            inSameCountry = IGNORE_GOOGLE_NON_ROAMING;
        }
        return inSameCountry;
    }

    protected void setRoamingType(ServiceState currentServiceState) {
        boolean isVoiceInService;
        if (currentServiceState.getVoiceRegState() == 0) {
            isVoiceInService = IGNORE_GOOGLE_NON_ROAMING;
        } else {
            isVoiceInService = VDBG;
        }
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(PS_ONLY);
            } else if (!this.mPhone.isPhoneTypeGsm()) {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17236040);
                if (intRoamingIndicators != null && intRoamingIndicators.length > 0) {
                    currentServiceState.setVoiceRoamingType(OTASP_NEEDED);
                    int curRoamingIndicator = currentServiceState.getCdmaRoamingIndicator();
                    for (int i = PS_ONLY; i < intRoamingIndicators.length; i += PS_CS) {
                        if (curRoamingIndicator == intRoamingIndicators[i]) {
                            currentServiceState.setVoiceRoamingType(OTASP_NOT_NEEDED);
                            break;
                        }
                    }
                } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setVoiceRoamingType(OTASP_NEEDED);
                } else {
                    currentServiceState.setVoiceRoamingType(OTASP_NOT_NEEDED);
                }
            } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                currentServiceState.setVoiceRoamingType(OTASP_NEEDED);
            } else {
                currentServiceState.setVoiceRoamingType(OTASP_NOT_NEEDED);
            }
        }
        boolean isDataInService = currentServiceState.getDataRegState() == 0 ? IGNORE_GOOGLE_NON_ROAMING : VDBG;
        int dataRegType = currentServiceState.getRilDataRadioTechnology();
        if (!isDataInService) {
            return;
        }
        if (!currentServiceState.getDataRoaming()) {
            currentServiceState.setDataRoamingType(PS_ONLY);
        } else if (this.mPhone.isPhoneTypeGsm()) {
            if (!ServiceState.isGsm(dataRegType)) {
                currentServiceState.setDataRoamingType(PS_CS);
            } else if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(PS_CS);
            }
        } else if (ServiceState.isCdma(dataRegType)) {
            if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(PS_CS);
            }
        } else if (inSameCountry(currentServiceState.getDataOperatorNumeric())) {
            currentServiceState.setDataRoamingType(OTASP_NEEDED);
        } else {
            currentServiceState.setDataRoamingType(OTASP_NOT_NEEDED);
        }
    }

    private void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength(IGNORE_GOOGLE_NON_ROAMING);
    }

    protected String getHomeOperatorNumeric() {
        String numeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (this.mPhone.isPhoneTypeGsm() || !TextUtils.isEmpty(numeric)) {
            return numeric;
        }
        return SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "");
    }

    protected int getPhoneId() {
        return this.mPhone.getPhoneId();
    }

    protected void resetServiceStateInIwlanMode() {
        if (this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            boolean resetIwlanRatVal = VDBG;
            log("set service state as POWER_OFF");
            if (EVENT_LOCATION_UPDATES_ENABLED == this.mNewSS.getRilDataRadioTechnology()) {
                log("pollStateDone: mNewSS = " + this.mNewSS);
                log("pollStateDone: reset iwlan RAT value");
                resetIwlanRatVal = IGNORE_GOOGLE_NON_ROAMING;
            }
            this.mNewSS.setStateOff();
            if (resetIwlanRatVal) {
                this.mNewSS.setRilDataRadioTechnology(EVENT_LOCATION_UPDATES_ENABLED);
                this.mNewSS.setDataRegState(PS_ONLY);
                log("pollStateDone: mNewSS = " + this.mNewSS);
            }
        }
    }

    protected final boolean alwaysOnHomeNetwork(BaseBundle b) {
        return b.getBoolean("force_home_network_bool");
    }

    private boolean isInNetwork(BaseBundle b, String network, String key) {
        String[] networks = b.getStringArray(key);
        if (networks == null || !Arrays.asList(networks).contains(network)) {
            return VDBG;
        }
        return IGNORE_GOOGLE_NON_ROAMING;
    }

    protected final boolean isRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_roaming_networks_string_array");
    }

    protected final boolean isNonRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_nonroaming_networks_string_array");
    }

    protected final boolean isRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_roaming_networks_string_array");
    }

    protected final boolean isNonRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_nonroaming_networks_string_array");
    }

    public boolean isDeviceShuttingDown() {
        return this.mDeviceShuttingDown;
    }

    protected void getCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int length = stack.length;
        for (int i = PS_ONLY; i < length; i += PS_CS) {
            StackTraceElement ste = stack[i];
            log("    at " + ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")");
        }
    }

    protected void acquireWakeLock() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) this.mPhone.getContext().getSystemService("power")).newWakeLock(PS_CS, "SERVICESTATE_WAIT_DISCONNECT_WAKELOCK");
        }
        this.mWakeLock.setReferenceCounted(VDBG);
        log("Servicestate wait disconnect, acquire wakelock");
        this.mWakeLock.acquire();
    }

    protected void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            log("release wakelock");
        }
    }

    public boolean isContainPackage(String data, String packageName) {
        String[] enablePackage = null;
        if (!TextUtils.isEmpty(data)) {
            enablePackage = data.split(";");
        }
        if (enablePackage == null || enablePackage.length == 0) {
            return VDBG;
        }
        int i = PS_ONLY;
        while (i < enablePackage.length) {
            if (!TextUtils.isEmpty(packageName) && packageName.equals(enablePackage[i])) {
                return IGNORE_GOOGLE_NON_ROAMING;
            }
            i += PS_CS;
        }
        return VDBG;
    }
}
