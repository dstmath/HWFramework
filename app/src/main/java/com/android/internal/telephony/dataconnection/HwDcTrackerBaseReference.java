package com.android.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.LinkProperties;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.provider.Telephony.Carriers;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.GlobalParamsAdaptor;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwServiceStateManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase.DcTrackerBaseReference;
import com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReason;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import huawei.cust.HwCustUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HwDcTrackerBaseReference implements DcTrackerBaseReference {
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = null;
    protected static final String CAUSE_NO_RETRY_AFTER_DISCONNECT = null;
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final String CLEARCODE_2HOUR_DELAY_OVER = "clearcode2HourDelayOver";
    private static final String CT_CDMA_OPERATOR = "46003";
    public static final int DATA_ROAMING_EXCEPTION = -1;
    public static final int DATA_ROAMING_INTERNATIONAL = 2;
    public static final int DATA_ROAMING_NATIONAL = 1;
    public static final int DATA_ROAMING_OFF = 0;
    public static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    private static final boolean DBG = true;
    private static final int DELAY_2_HOUR = 7200000;
    protected static final String ENABLE_ALLOW_MMS = "enable_always_allow_mms";
    private static final int EVENT_FDN_RECORDS_LOADED = 2;
    private static final int EVENT_FDN_SWITCH_CHANGED = 1;
    private static final int EVENT_VOICE_CALL_ENDED = 3;
    protected static final Uri FDN_URL = null;
    private static final String INTENT_SET_PREF_NETWORK_TYPE = "com.android.internal.telephony.set-pref-networktype";
    private static final String INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE = "network_type";
    private static final int MCC_LENGTH = 3;
    protected static final boolean MMSIgnoreDSSwitchNotRoaming = false;
    protected static final boolean MMSIgnoreDSSwitchOnRoaming = false;
    protected static final boolean MMS_ON_ROAMING = false;
    protected static final int MMS_PROP = 0;
    private static final int NETWORK_MODE_GSM_UMTS = 3;
    private static final int NETWORK_MODE_LTE_GSM_WCDMA = 9;
    private static final int NETWORK_MODE_UMTS_ONLY = 2;
    private static final int PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_4G = 10000;
    private static final int PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_NOT_4G = 45000;
    private static final boolean RESET_PROFILE = false;
    public static final int SUB2 = 1;
    private static final String TAG = "HwDcTrackerBaseReference";
    protected static final boolean USER_FORCE_DATA_SETUP = false;
    protected static final boolean isMultiSimEnabled = false;
    private static int newRac;
    private static int oldRac;
    protected boolean ALLOW_MMS;
    private boolean SETAPN_UNTIL_CARDLOADED;
    private boolean SUPPORT_MPDN;
    private ContentObserver allowMmsObserver;
    private boolean broadcastPrePostPay;
    GsmCellLocation cellLoc;
    private Handler handler;
    private boolean isRecievedPingReply;
    private PendingIntent mAlarmIntent;
    private AlarmManager mAlarmManager;
    public DcFailCause mCurFailCause;
    private DcTracker mDcTrackerBase;
    private int mDelayTime;
    private FdnAsyncQueryHandler mFdnAsyncQuery;
    private FdnChangeObserver mFdnChangeObserver;
    ServiceStateTracker mGsmServiceStateTracker;
    private BroadcastReceiver mIntentReceiver;
    private boolean mIsClearCodeEnabled;
    private int mNwOldMode;
    private AlertDialog mPSClearCodeDialog;
    private String mSimState;
    private Integer mSubscription;
    private int mTryIndex;
    protected UiccController mUiccController;
    private int nwMode;
    private ContentObserver nwModeChangeObserver;
    private int oldRadioTech;
    private Condition pingCondition;
    private ReentrantLock pingThreadlLock;
    PhoneStateListener pslForCellLocation;
    private boolean removePreferredApn;

    class AllowMmmsContentObserver extends ContentObserver {
        public AllowMmmsContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            boolean z = HwDcTrackerBaseReference.DBG;
            int allowMms = System.getInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), HwDcTrackerBaseReference.ENABLE_ALLOW_MMS, HwDcTrackerBaseReference.MMS_PROP);
            HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
            if (allowMms != HwDcTrackerBaseReference.SUB2) {
                z = HwDcTrackerBaseReference.RESET_PROFILE;
            }
            hwDcTrackerBaseReference.ALLOW_MMS = z;
        }
    }

    private class FdnAsyncQueryHandler extends AsyncQueryHandler {
        public FdnAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            long subId = (long) HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getSubId();
            boolean isFdnActivated1 = SystemProperties.getBoolean("gsm.hw.fdn.activated1", HwDcTrackerBaseReference.RESET_PROFILE);
            boolean isFdnActivated2 = SystemProperties.getBoolean("gsm.hw.fdn.activated2", HwDcTrackerBaseReference.RESET_PROFILE);
            HwDcTrackerBaseReference.this.log("fddn onQueryComplete subId:" + subId + " ,isFdnActivated1:" + isFdnActivated1 + " ,isFdnActivated2:" + isFdnActivated2);
            if ((subId == 0 && isFdnActivated1) || (subId == 1 && isFdnActivated2)) {
                HwDcTrackerBaseReference.this.retryDataConnectionByFdn();
            }
        }
    }

    private class FdnChangeObserver extends ContentObserver {
        public FdnChangeObserver() {
            super(HwDcTrackerBaseReference.this.mDcTrackerBase);
        }

        public void onChange(boolean selfChange) {
            HwDcTrackerBaseReference.this.log("fddn FdnChangeObserver onChange, selfChange:" + selfChange);
            HwDcTrackerBaseReference.this.asyncQueryContact();
        }
    }

    class NwModeContentObserver extends ContentObserver {
        public NwModeContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean Change) {
            if (HwDcTrackerBaseReference.isMultiSimEnabled) {
                if (TelephonyManager.getTelephonyProperty(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getPhoneId(), "gsm.data.gsm_only_not_allow_ps", "false").equals("false")) {
                    return;
                }
            } else if (!SystemProperties.getBoolean("gsm.data.gsm_only_not_allow_ps", HwDcTrackerBaseReference.RESET_PROFILE)) {
                return;
            }
            HwDcTrackerBaseReference.this.nwMode = Global.getInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", Phone.PREFERRED_NT_MODE);
            HwDcTrackerBaseReference.this.log("NwModeChangeObserver onChange nwMode = " + HwDcTrackerBaseReference.this.nwMode);
            if (HwDcTrackerBaseReference.this.mDcTrackerBase instanceof DcTracker) {
                DcTracker dcTracker = HwDcTrackerBaseReference.this.mDcTrackerBase;
                if (HwDcTrackerBaseReference.SUB2 == HwDcTrackerBaseReference.this.nwMode) {
                    DcTrackerUtils.cleanUpAllConnections(dcTracker, HwDcTrackerBaseReference.DBG, "nwTypeChanged");
                } else if (HwDcTrackerBaseReference.SUB2 == HwDcTrackerBaseReference.this.mNwOldMode) {
                    DcTrackerUtils.onTrySetupData(dcTracker, "nwTypeChanged");
                }
            }
            HwDcTrackerBaseReference.this.mNwOldMode = HwDcTrackerBaseReference.this.nwMode;
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues;
        }
        int[] iArr = new int[AppType.values().length];
        try {
            iArr[AppType.APPTYPE_CSIM.ordinal()] = SUB2;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppType.APPTYPE_ISIM.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppType.APPTYPE_RUIM.ordinal()] = NETWORK_MODE_UMTS_ONLY;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppType.APPTYPE_SIM.ordinal()] = NETWORK_MODE_GSM_UMTS;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppType.APPTYPE_UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppType.APPTYPE_USIM.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.HwDcTrackerBaseReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.HwDcTrackerBaseReference.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.HwDcTrackerBaseReference.<clinit>():void");
    }

    public HwDcTrackerBaseReference(DcTracker dcTrackerBase) {
        this.oldRadioTech = MMS_PROP;
        this.mPSClearCodeDialog = null;
        this.mGsmServiceStateTracker = null;
        this.mTryIndex = MMS_PROP;
        this.mDelayTime = 3000;
        this.mIsClearCodeEnabled = SystemProperties.getBoolean("ro.config.hw_clearcode_pdp", RESET_PROFILE);
        this.SETAPN_UNTIL_CARDLOADED = SystemProperties.getBoolean("ro.config.delay_setapn", RESET_PROFILE);
        this.mSimState = null;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                    int subid = HwDcTrackerBaseReference.MMS_PROP;
                    if (intent.getExtra("subscription") != null) {
                        subid = intent.getIntExtra("subscription", HwDcTrackerBaseReference.DATA_ROAMING_EXCEPTION);
                    }
                    if (subid != HwDcTrackerBaseReference.this.mSubscription.intValue()) {
                        Rlog.d(HwDcTrackerBaseReference.TAG, "receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , but the subid is different from mSubscription");
                        return;
                    }
                    String curSimState = intent.getStringExtra("ss");
                    if (TextUtils.equals(curSimState, HwDcTrackerBaseReference.this.mSimState)) {
                        Rlog.d(HwDcTrackerBaseReference.TAG, "the curSimState is same as mSimState, so return");
                        return;
                    }
                    if (("ABSENT".equals(curSimState) || "CARD_IO_ERROR".equals(curSimState)) && !"ABSENT".equals(HwDcTrackerBaseReference.this.mSimState) && !"CARD_IO_ERROR".equals(HwDcTrackerBaseReference.this.mSimState) && HwDcTrackerBaseReference.RESET_PROFILE) {
                        Rlog.d(HwDcTrackerBaseReference.TAG, "receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , resetprofile");
                        HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.mCi.resetProfile(null);
                    }
                    HwDcTrackerBaseReference.this.mSimState = curSimState;
                } else if (intent.getAction() != null && intent.getAction().equals(HwDcTrackerBaseReference.INTENT_SET_PREF_NETWORK_TYPE)) {
                    HwDcTrackerBaseReference.this.onActionIntentSetNetworkType(intent);
                }
            }
        };
        this.allowMmsObserver = null;
        this.mUiccController = UiccController.getInstance();
        this.removePreferredApn = DBG;
        this.broadcastPrePostPay = DBG;
        this.ALLOW_MMS = RESET_PROFILE;
        this.SUPPORT_MPDN = SystemProperties.getBoolean("persist.telephony.mpdn", DBG);
        this.mNwOldMode = Phone.PREFERRED_NT_MODE;
        this.nwMode = Phone.PREFERRED_NT_MODE;
        this.isRecievedPingReply = RESET_PROFILE;
        this.pingThreadlLock = new ReentrantLock();
        this.pingCondition = this.pingThreadlLock.newCondition();
        this.cellLoc = new GsmCellLocation();
        this.pslForCellLocation = new PhoneStateListener() {
            public void onCellLocationChanged(CellLocation location) {
                if (HwDcTrackerBaseReference.this.mDcTrackerBase.mApnContexts != null) {
                    try {
                        HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged");
                        if (location instanceof GsmCellLocation) {
                            GsmCellLocation newCellLoc = (GsmCellLocation) location;
                            HwDcTrackerBaseReference.this.mGsmServiceStateTracker = HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getServiceStateTracker();
                            HwDcTrackerBaseReference.newRac = HwServiceStateManager.getHwGsmServiceStateManager(HwDcTrackerBaseReference.this.mGsmServiceStateTracker, (GsmCdmaPhone) HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone).getRac();
                            int radioTech = HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
                            HwDcTrackerBaseReference.this.log("CLEARCODE newCellLoc = " + newCellLoc + ", oldCellLoc = " + HwDcTrackerBaseReference.this.cellLoc + " oldRac = " + HwDcTrackerBaseReference.oldRac + " newRac = " + HwDcTrackerBaseReference.newRac + " radioTech = " + radioTech + " oldRadioTech = " + HwDcTrackerBaseReference.this.oldRadioTech);
                            if (HwDcTrackerBaseReference.this.oldRadioTech != radioTech) {
                                HwDcTrackerBaseReference.this.oldRadioTech = radioTech;
                                HwDcTrackerBaseReference.this.log("clearcode oldRadioTech = " + HwDcTrackerBaseReference.this.oldRadioTech);
                                HwDcTrackerBaseReference.oldRac = HwDcTrackerBaseReference.DATA_ROAMING_EXCEPTION;
                                HwDcTrackerBaseReference.this.resetTryTimes();
                            }
                            if (HwDcTrackerBaseReference.DATA_ROAMING_EXCEPTION == HwDcTrackerBaseReference.newRac) {
                                HwDcTrackerBaseReference.this.log("CLEARCODE not really changed");
                                return;
                            } else if (HwDcTrackerBaseReference.oldRac == HwDcTrackerBaseReference.newRac || radioTech != HwDcTrackerBaseReference.NETWORK_MODE_GSM_UMTS) {
                                HwDcTrackerBaseReference.this.log("CLEARCODE RAC not really changed");
                                return;
                            } else if (HwDcTrackerBaseReference.DATA_ROAMING_EXCEPTION == HwDcTrackerBaseReference.oldRac) {
                                HwDcTrackerBaseReference.oldRac = HwDcTrackerBaseReference.newRac;
                                HwDcTrackerBaseReference.this.log("CLEARCODE oldRac = -1 return");
                                return;
                            } else {
                                HwDcTrackerBaseReference.oldRac = HwDcTrackerBaseReference.newRac;
                                HwDcTrackerBaseReference.this.cellLoc = newCellLoc;
                                DcTracker dcTracker = HwDcTrackerBaseReference.this.mDcTrackerBase;
                                ApnContext defaultApn = (ApnContext) HwDcTrackerBaseReference.this.mDcTrackerBase.mApnContexts.get("default");
                                if (!(!HwDcTrackerBaseReference.this.mDcTrackerBase.mUserDataEnabled || defaultApn == null || defaultApn.getState() == State.CONNECTED)) {
                                    int curPrefMode = Global.getInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", HwDcTrackerBaseReference.MMS_PROP);
                                    HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged radioTech = " + radioTech + " curPrefMode" + curPrefMode);
                                    if (!(curPrefMode == HwDcTrackerBaseReference.NETWORK_MODE_LTE_GSM_WCDMA || curPrefMode == HwDcTrackerBaseReference.NETWORK_MODE_UMTS_ONLY)) {
                                        HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.setPreferredNetworkType(HwDcTrackerBaseReference.NETWORK_MODE_LTE_GSM_WCDMA, null);
                                        Global.putInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", HwDcTrackerBaseReference.NETWORK_MODE_LTE_GSM_WCDMA);
                                        HwServiceStateManager.getHwGsmServiceStateManager(HwDcTrackerBaseReference.this.mGsmServiceStateTracker, (GsmCdmaPhone) HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone).setRac(HwDcTrackerBaseReference.DATA_ROAMING_EXCEPTION);
                                        HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged try switch 3G to 4G and set newrac to -1");
                                    }
                                    boolean isDisconnected = (defaultApn.getState() == State.IDLE || defaultApn.getState() == State.FAILED) ? HwDcTrackerBaseReference.DBG : HwDcTrackerBaseReference.RESET_PROFILE;
                                    HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged try setup data again");
                                    DcTrackerUtils.cleanUpConnection(dcTracker, isDisconnected ? HwDcTrackerBaseReference.RESET_PROFILE : HwDcTrackerBaseReference.DBG, defaultApn);
                                    HwDcTrackerBaseReference.this.setupDataOnConnectableApns("cellLocationChanged", null);
                                    HwDcTrackerBaseReference.this.resetTryTimes();
                                }
                                return;
                            }
                        }
                        HwDcTrackerBaseReference.this.log("CLEARCODE location not instanceof GsmCellLocation");
                    } catch (Exception e) {
                        Rlog.e(HwDcTrackerBaseReference.TAG, "Exception in CellStateHandler.handleMessage:", e);
                    }
                }
            }
        };
        this.nwModeChangeObserver = null;
        this.handler = new Handler() {
            public void handleMessage(Message msg) {
                HwDcTrackerBaseReference.this.log("handleMessage msg=" + msg.what);
                switch (msg.what) {
                    case HwDcTrackerBaseReference.NETWORK_MODE_GSM_UMTS /*3*/:
                        HwDcTrackerBaseReference.this.onVoiceCallEndedHw();
                    default:
                }
            }
        };
        this.mDcTrackerBase = dcTrackerBase;
    }

    public void init() {
        boolean z;
        if (System.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), ENABLE_ALLOW_MMS, MMS_PROP) == SUB2) {
            z = DBG;
        } else {
            z = RESET_PROFILE;
        }
        this.ALLOW_MMS = z;
        Uri allowMmsUri = Systemex.CONTENT_URI;
        this.allowMmsObserver = new AllowMmmsContentObserver(this.mDcTrackerBase);
        this.mDcTrackerBase.mPhone.getContext().getContentResolver().registerContentObserver(allowMmsUri, DBG, this.allowMmsObserver);
        this.nwModeChangeObserver = new NwModeContentObserver(this.mDcTrackerBase);
        this.mDcTrackerBase.mPhone.getContext().getContentResolver().registerContentObserver(Global.getUriFor("preferred_network_mode"), DBG, this.nwModeChangeObserver);
        Phone phone = this.mDcTrackerBase.mPhone;
        this.nwMode = Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", Phone.PREFERRED_NT_MODE);
        this.mNwOldMode = this.nwMode;
        this.mDcTrackerBase.INTELLIGENT_DATA_SWITCH_CONFIG = SystemProperties.getBoolean("ro.hwpp.autodds", RESET_PROFILE);
        if (this.mDcTrackerBase.mPhone.getCallTracker() != null) {
            this.mDcTrackerBase.mPhone.getCallTracker().registerForVoiceCallEnded(this.handler, NETWORK_MODE_GSM_UMTS, null);
        }
        this.mSubscription = Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mAlarmManager = (AlarmManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("alarm");
        filter.addAction(INTENT_SET_PREF_NETWORK_TYPE);
        this.mDcTrackerBase.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mDcTrackerBase.mPhone);
    }

    public void dispose() {
        if (this.allowMmsObserver != null) {
            this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.allowMmsObserver);
        }
        if (this.nwModeChangeObserver != null) {
            this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.nwModeChangeObserver);
        }
        if (this.mDcTrackerBase.mPhone.getCallTracker() != null) {
            this.mDcTrackerBase.mPhone.getCallTracker().unregisterForVoiceCallEnded(this.handler);
        }
        if (this.mIntentReceiver != null) {
            this.mDcTrackerBase.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
    }

    private void onResetApn() {
        ApnContext apnContext = (ApnContext) this.mDcTrackerBase.mApnContexts.get("default");
        if (apnContext != null) {
            apnContext.setEnabled(DBG);
            apnContext.setDependencyMet(DBG);
        }
    }

    public void beforeHandleMessage(Message msg) {
        if (200 == msg.what) {
            onResetApn();
        }
    }

    public boolean isDataAllowedByApnContext(ApnContext apnContext) {
        if (isGsmOnlyPsNotAllowed()) {
            log("in GsmMode not allowed PS!");
            return RESET_PROFILE;
        }
        boolean isMMS = "mms".equals(apnContext.getApnType());
        boolean isUserEnable = isNeedForceSetup(apnContext);
        log("isDataAllowedByApnType: isMms = " + isMMS + " isUserEnable = " + isUserEnable);
        return this.mDcTrackerBase.isDataAllowed(new DataAllowFailReason(), isMMS, isUserEnable);
    }

    private boolean isNeedForceSetup(ApnContext apnContext) {
        boolean isUserEnable = "dataEnabled".equals(apnContext.getReason());
        boolean isCSInService = this.mDcTrackerBase.mPhone.getServiceState().getVoiceRegState() == 0 ? DBG : RESET_PROFILE;
        if (isUserEnable && isCSInService) {
            return USER_FORCE_DATA_SETUP;
        }
        return RESET_PROFILE;
    }

    public boolean isDataAllowedByApnType(DataAllowFailReason failureReason, String apnType) {
        if (isGsmOnlyPsNotAllowed()) {
            log("in GsmMode not allowed PS!");
            return RESET_PROFILE;
        }
        return "mms".equals(apnType) ? this.mDcTrackerBase.isDataAllowed(new DataAllowFailReason(), DBG) : this.mDcTrackerBase.isDataAllowed(new DataAllowFailReason(), RESET_PROFILE);
    }

    private boolean isGsmOnlyPsNotAllowed() {
        boolean z = DBG;
        boolean z2 = RESET_PROFILE;
        if (isMultiSimEnabled) {
            if (!(TelephonyManager.getTelephonyProperty(this.mDcTrackerBase.mPhone.getPhoneId(), "gsm.data.gsm_only_not_allow_ps", "false").equals("true") && SUB2 == this.nwMode)) {
                z = RESET_PROFILE;
            }
            return z;
        }
        if (SystemProperties.getBoolean("gsm.data.gsm_only_not_allow_ps", RESET_PROFILE) && SUB2 == this.nwMode) {
            z2 = DBG;
        }
        return z2;
    }

    public boolean isDataAllowedForRoaming(boolean isMms) {
        if (!this.mDcTrackerBase.mPhone.getServiceState().getRoaming() || this.mDcTrackerBase.getDataOnRoamingEnabled()) {
            return DBG;
        }
        if (this.ALLOW_MMS || MMS_ON_ROAMING) {
            return isMms;
        }
        return RESET_PROFILE;
    }

    public void onAllApnFirstActiveFailed() {
        if (isMultiSimEnabled) {
            ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getPhoneId()).allApnActiveFailed();
            return;
        }
        ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext()).allApnActiveFailed();
    }

    public void onAllApnPermActiveFailed() {
        if (this.broadcastPrePostPay && GlobalParamsAdaptor.getPrePostPayPreCondition()) {
            log("tryToActionPrePostPay.");
            GlobalParamsAdaptor.tryToActionPrePostPay();
            this.broadcastPrePostPay = RESET_PROFILE;
        }
        ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext()).getCust().handleAllApnPermActiveFailed(this.mDcTrackerBase.mPhone.getContext());
    }

    public boolean isBipApnType(String type) {
        if (HuaweiTelephonyConfigs.isModemBipEnable() || (!type.equals("bip0") && !type.equals("bip1") && !type.equals("bip2") && !type.equals("bip3") && !type.equals("bip4") && !type.equals("bip5") && !type.equals("bip6"))) {
            return RESET_PROFILE;
        }
        return DBG;
    }

    public ApnSetting fetchBipApn(ApnSetting preferredApn, ArrayList<ApnSetting> allApnSettings) {
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            ApnSetting mDataProfile = ApnSetting.fromString(SystemProperties.get("gsm.bip.apn"));
            if ("default".equals(SystemProperties.get("gsm.bip.apn"))) {
                if (preferredApn != null) {
                    log("find prefer apn, use this");
                    return preferredApn;
                }
                if (allApnSettings != null) {
                    for (ApnSetting apn : allApnSettings) {
                        if (apn.canHandleType("default")) {
                            log("find the first default apn");
                            return apn;
                        }
                    }
                }
                log("find non apn for default bip");
                return null;
            } else if (mDataProfile != null) {
                log("fetchBipApn: global BIP mDataProfile=" + mDataProfile);
                return mDataProfile;
            }
        }
        return null;
    }

    private void log(String string) {
        Rlog.d(TAG, string);
    }

    public void setFirstTimeEnableData() {
        log("=PREPOSTPAY=, Data Setup Successful.");
        if (this.broadcastPrePostPay) {
            this.broadcastPrePostPay = RESET_PROFILE;
        }
    }

    public boolean needRemovedPreferredApn() {
        if (!this.removePreferredApn || !GlobalParamsAdaptor.getPrePostPayPreCondition()) {
            return RESET_PROFILE;
        }
        log("Remove preferred apn.");
        this.removePreferredApn = RESET_PROFILE;
        return DBG;
    }

    public String getDataRoamingSettingItem(String originItem) {
        if (isMultiSimEnabled && this.mDcTrackerBase.mPhone.getPhoneId() == SUB2) {
            return DATA_ROAMING_SIM2;
        }
        return originItem;
    }

    public void disableGoogleDunApn(Context c, String apnData, ApnSetting dunSetting) {
        if (SystemProperties.getBoolean("ro.config.enable.gdun", RESET_PROFILE)) {
            dunSetting = ApnSetting.fromString("this is false");
        }
    }

    public boolean getAnyDataEnabledByApnContext(ApnContext apnContext, boolean enable) {
        boolean z = DBG;
        if (this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
            if ((this.ALLOW_MMS || MMSIgnoreDSSwitchOnRoaming) && "mms".equals(apnContext.getApnType())) {
                enable = DBG;
            }
            return enable;
        }
        if (!((this.ALLOW_MMS || MMSIgnoreDSSwitchNotRoaming) && "mms".equals(apnContext.getApnType()))) {
            z = enable;
        }
        return z;
    }

    public boolean shouldDisableMultiPdps(boolean onlySingleDcAllowed) {
        if (!(this.SUPPORT_MPDN || SystemProperties.getBoolean("gsm.multipdp.plmn.matched", RESET_PROFILE))) {
            onlySingleDcAllowed = DBG;
            log("SUPPORT_MPDN: " + this.SUPPORT_MPDN);
        }
        if (isMultiSimEnabled) {
            int subId = this.mDcTrackerBase.mPhone.getPhoneId();
            if (subId == 0) {
                SystemProperties.set("gsm.check_is_single_pdp_sub1", Boolean.toString(onlySingleDcAllowed));
            } else if (subId == SUB2) {
                SystemProperties.set("gsm.check_is_single_pdp_sub2", Boolean.toString(onlySingleDcAllowed));
            }
        } else {
            SystemProperties.set("gsm.check_is_single_pdp", Boolean.toString(onlySingleDcAllowed));
        }
        return onlySingleDcAllowed;
    }

    public void setMPDN(boolean bMPDN) {
        if (bMPDN == this.SUPPORT_MPDN) {
            log("MPDN is same,Don't need change");
            return;
        }
        if (bMPDN) {
            int radioTech = this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
            if (ServiceState.isCdma(radioTech) && radioTech != 13) {
                log("technology is not EHRPD and ServiceState is CDMA,Can't set MPDN");
                return;
            }
        }
        this.SUPPORT_MPDN = bMPDN;
        log("SUPPORT_MPDN change to " + bMPDN);
    }

    public void setMPDNByNetWork(String plmnNetWork) {
        if (this.mDcTrackerBase.mPhone == null) {
            log("mPhone is null");
            return;
        }
        String plmnsConfig = Systemex.getString(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "mpdn_plmn_matched_by_network");
        if (TextUtils.isEmpty(plmnsConfig)) {
            log("plmnConfig is Empty");
            return;
        }
        boolean bMPDN = RESET_PROFILE;
        String[] plmns = plmnsConfig.split(",");
        int length = plmns.length;
        for (int i = MMS_PROP; i < length; i += SUB2) {
            String plmn = plmns[i];
            if (!TextUtils.isEmpty(plmn) && plmn.equals(plmnNetWork)) {
                bMPDN = DBG;
                break;
            }
        }
        setMPDN(bMPDN);
        log("setMpdnByNewNetwork done, bMPDN is " + bMPDN);
    }

    public String getCTOperatorNumeric(String operator) {
        String result = operator;
        if (!HuaweiTelephonyConfigs.isChinaTelecom() || this.mDcTrackerBase.mPhone.getPhoneId() != 0) {
            return result;
        }
        result = CT_CDMA_OPERATOR;
        log("getCTOperatorNumeric: use china telecom operator=" + result);
        return result;
    }

    public ApnSetting makeHwApnSetting(Cursor cursor, String[] types) {
        return new HwApnSetting(cursor, types);
    }

    public boolean noNeedDoRecovery(ConcurrentHashMap mApnContexts) {
        if (SystemProperties.getBoolean("persist.radio.hw.nodorecovery", RESET_PROFILE)) {
            return DBG;
        }
        if (!SystemProperties.getBoolean("hw.ds.np.nopollstat", DBG) || isActiveDefaultApnPreset(mApnContexts)) {
            return RESET_PROFILE;
        }
        return DBG;
    }

    public boolean isActiveDefaultApnPreset(ConcurrentHashMap<String, ApnContext> mApnContexts) {
        ApnContext apnContext = (ApnContext) mApnContexts.get("default");
        if (apnContext != null && State.CONNECTED == apnContext.getState()) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null && (apnSetting instanceof HwApnSetting)) {
                HwApnSetting hwapnSetting = (HwApnSetting) apnSetting;
                log("current default apn is " + (hwapnSetting.isPreset() ? "preset" : "non-preset"));
                return hwapnSetting.isPreset();
            }
        }
        return DBG;
    }

    public boolean isApnPreset(ApnSetting apnSetting) {
        if (apnSetting == null || !(apnSetting instanceof HwApnSetting)) {
            return DBG;
        }
        return ((HwApnSetting) apnSetting).isPreset();
    }

    public HwCustDcTracker getCust(DcTracker dcTracker) {
        Object[] objArr = new Object[SUB2];
        objArr[MMS_PROP] = dcTracker;
        return (HwCustDcTracker) HwCustUtils.createObj(HwCustDcTracker.class, objArr);
    }

    public void setupDataOnConnectableApns(String reason, String excludedApnType) {
        log("setupDataOnConnectableApns: " + reason + ", excludedApnType = " + excludedApnType);
        for (ApnContext apnContext : this.mDcTrackerBase.mPrioritySortedApnContexts) {
            if (TextUtils.isEmpty(excludedApnType) || !excludedApnType.equals(apnContext.getApnType())) {
                log("setupDataOnConnectableApns: apnContext " + apnContext);
                if (apnContext.getState() == State.FAILED) {
                    apnContext.setState(State.IDLE);
                }
                if (apnContext.isConnectable()) {
                    log("setupDataOnConnectableApns: isConnectable() call trySetupData");
                    apnContext.setReason(reason);
                    this.mDcTrackerBase.onTrySetupData(apnContext);
                }
            }
        }
    }

    public boolean needRetryAfterDisconnected(DcFailCause cause) {
        String failCauseStr = "";
        if (DcFailCause.ERROR_UNSPECIFIED != cause) {
            return DBG;
        }
        failCauseStr = SystemProperties.get("ril.ps_ce_reason", "");
        if (TextUtils.isEmpty(failCauseStr)) {
            return DBG;
        }
        String[] noRetryCauses = CAUSE_NO_RETRY_AFTER_DISCONNECT.split(",");
        int length = noRetryCauses.length;
        for (int i = MMS_PROP; i < length; i += SUB2) {
            if (failCauseStr.equals(noRetryCauses[i])) {
                return RESET_PROFILE;
            }
        }
        return DBG;
    }

    public void setRetryAfterDisconnectedReason(DataConnection dc, ArrayList<ApnContext> apnsToCleanup) {
        for (ApnContext apnContext : dc.mApnContexts.keySet()) {
            apnContext.setReason("noRetryAfterDisconnect");
        }
        apnsToCleanup.addAll(dc.mApnContexts.keySet());
    }

    public boolean isChinaTelecom(int slotId) {
        return HwTelephonyManagerInner.getDefault().isChinaTelecom(slotId);
    }

    public boolean isFullNetworkSupported() {
        return HwTelephonyManagerInner.getDefault().isFullNetworkSupported();
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public boolean isCTDualModeCard(int sub) {
        int SubType = HwTelephonyManagerInner.getDefault().getCardType(sub);
        if (41 != SubType && 43 != SubType) {
            return RESET_PROFILE;
        }
        log("sub = " + sub + ", SubType = " + SubType + " is CT dual modem card");
        return DBG;
    }

    public boolean isPingOk() {
        boolean ret = RESET_PROFILE;
        if (HwVSimUtils.isVSimOn()) {
            log("isPineOk always ok for vsim on");
            return DBG;
        }
        try {
            String pingBeforeDorecovery = SystemProperties.get("ro.sys.ping_bf_dorecovery", "false");
            log("isPingOk pingBeforeDorecovery = " + pingBeforeDorecovery);
            String operatorNumeric = ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).getNetworkOperatorForPhone(this.mDcTrackerBase.mPhone.getSubId());
            Object obj = null;
            if (operatorNumeric != null && operatorNumeric.length() > NETWORK_MODE_GSM_UMTS) {
                obj = operatorNumeric.substring(MMS_PROP, NETWORK_MODE_GSM_UMTS);
                log("isPingOk mcc = " + obj);
            }
            if (pingBeforeDorecovery.equals("true") || CHINA_OPERATOR_MCC.equals(r1)) {
                Thread pingThread = new Thread(new Runnable() {
                    public void run() {
                        String result = "";
                        String serverName = "www.baidu.com";
                        HwDcTrackerBaseReference.this.log("ping thread enter, server name = " + serverName);
                        try {
                            HwDcTrackerBaseReference.this.pingThreadlLock.lock();
                            HwDcTrackerBaseReference.this.isRecievedPingReply = HwDcTrackerBaseReference.RESET_PROFILE;
                            HwDcTrackerBaseReference.this.pingThreadlLock.unlock();
                            HwDcTrackerBaseReference.this.log("pingThread begin to ping");
                            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -W 1 " + serverName);
                            int status = process.waitFor();
                            HwDcTrackerBaseReference.this.log("pingThread, process.waitFor, status = " + status);
                            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            StringBuffer stringBuffer = new StringBuffer();
                            String str = "";
                            while (true) {
                                str = buf.readLine();
                                if (str == null) {
                                    break;
                                }
                                stringBuffer.append(str);
                                stringBuffer.append("\r\n");
                            }
                            String str2 = stringBuffer.toString();
                            buf.close();
                            HwDcTrackerBaseReference.this.log("ping result:" + str2);
                            HwDcTrackerBaseReference.this.log("pingThread pingThreadlLock.lock");
                            HwDcTrackerBaseReference.this.pingThreadlLock.lock();
                            if (status != 0 || str2.indexOf("1 packets transmitted, 1 received") < 0) {
                                HwDcTrackerBaseReference.this.isRecievedPingReply = HwDcTrackerBaseReference.RESET_PROFILE;
                            } else {
                                HwDcTrackerBaseReference.this.isRecievedPingReply = HwDcTrackerBaseReference.DBG;
                            }
                            HwDcTrackerBaseReference.this.pingCondition.signal();
                            HwDcTrackerBaseReference.this.log("pingThread pingThreadlLock.unlock, ping thread return " + HwDcTrackerBaseReference.this.isRecievedPingReply);
                            HwDcTrackerBaseReference.this.pingThreadlLock.unlock();
                        } catch (Exception e) {
                            Rlog.e(HwDcTrackerBaseReference.TAG, "ping thread Exception: ", e);
                        }
                    }
                }, "ping thread");
                log("isPingOk pingThreadlLock.lock");
                this.pingThreadlLock.lock();
                pingThread.start();
                this.pingCondition.await(1000, TimeUnit.MILLISECONDS);
                ret = this.isRecievedPingReply;
                this.pingThreadlLock.unlock();
                log("isPingOk pingThreadlLock.unlock");
            }
        } catch (Exception e) {
            Rlog.e(TAG, "isPingOk Exception: ", e);
        }
        return ret;
    }

    public boolean isClearCodeEnabled() {
        return this.mIsClearCodeEnabled;
    }

    public void startListenCellLocationChange() {
        ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).listen(this.pslForCellLocation, 16);
    }

    public void stopListenCellLocationChange() {
        ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).listen(this.pslForCellLocation, MMS_PROP);
    }

    public void operateClearCodeProcess(ApnContext apnContext, DcFailCause cause, int delay) {
        this.mDelayTime = delay;
        if (cause.isPermanentFail()) {
            log("CLEARCODE isPermanentFail,perhaps APN is wrong");
            boolean isClearcodeDcFailCause = (cause == DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED || cause == DcFailCause.USER_AUTHENTICATION) ? DBG : RESET_PROFILE;
            if ("default".equals(apnContext.getApnType()) && isClearcodeDcFailCause) {
                this.mTryIndex += SUB2;
                log("CLEARCODE mTryIndex increase,current mTryIndex = " + this.mTryIndex);
                if (this.mTryIndex >= NETWORK_MODE_GSM_UMTS) {
                    if (isLteRadioTech()) {
                        this.mDcTrackerBase.mPhone.setPreferredNetworkType(NETWORK_MODE_GSM_UMTS, null);
                        Global.putInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", NETWORK_MODE_GSM_UMTS);
                        this.mGsmServiceStateTracker = this.mDcTrackerBase.mPhone.getServiceStateTracker();
                        HwServiceStateManager.getHwGsmServiceStateManager(this.mGsmServiceStateTracker, (GsmCdmaPhone) this.mDcTrackerBase.mPhone).setRac(DATA_ROAMING_EXCEPTION);
                        log("CLEARCODE mTryIndex >= 3 and is LTE,switch 4G to 3G and set newrac to -1");
                    } else {
                        log("CLEARCODE mTryIndex >= 3 and is 3G,show clearcode dialog");
                        if (this.mPSClearCodeDialog == null) {
                            this.mPSClearCodeDialog = createPSClearCodeDiag(cause);
                            if (this.mPSClearCodeDialog != null) {
                                this.mPSClearCodeDialog.show();
                            }
                        }
                        set2HourDelay();
                    }
                    this.mTryIndex = MMS_PROP;
                    apnContext.markApnPermanentFailed(apnContext.getApnSetting());
                }
            } else {
                this.mTryIndex = MMS_PROP;
                apnContext.markApnPermanentFailed(apnContext.getApnSetting());
            }
            return;
        }
        this.mTryIndex = MMS_PROP;
        log("CLEARCODE not isPermanentFail ");
    }

    public void resetTryTimes() {
        if (isClearCodeEnabled()) {
            this.mTryIndex = MMS_PROP;
            if (this.mAlarmManager != null && this.mAlarmIntent != null) {
                this.mAlarmManager.cancel(this.mAlarmIntent);
                log("CLEARCODE cancel Alarm resetTryTimes");
            }
        }
    }

    private boolean isLteRadioTech() {
        if (this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology() == 14) {
            return DBG;
        }
        return RESET_PROFILE;
    }

    public void setCurFailCause(AsyncResult ar) {
        if (!isClearCodeEnabled()) {
            return;
        }
        if (ar.result instanceof DcFailCause) {
            this.mCurFailCause = (DcFailCause) ar.result;
        } else {
            this.mCurFailCause = null;
        }
    }

    private AlertDialog createPSClearCodeDiag(DcFailCause cause) {
        Builder buider = new Builder(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getContext().getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        if (cause == DcFailCause.USER_AUTHENTICATION) {
            buider.setMessage(33685820);
            log("CLEARCODE clear_code_29");
        } else if (cause != DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED) {
            return null;
        } else {
            buider.setMessage(33685821);
            log("CLEARCODE clear_code_33");
        }
        buider.setIcon(17301543);
        buider.setCancelable(RESET_PROFILE);
        buider.setPositiveButton("Aceptar", new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwDcTrackerBaseReference.this.mPSClearCodeDialog = null;
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    private void set2HourDelay() {
        int delayTime = SystemProperties.getInt("gsm.radio.debug.cause_delay", DELAY_2_HOUR);
        log("CLEARCODE dataRadioTech is 3G and mTryIndex >= 3,so set2HourDelay delayTime =" + delayTime);
        Intent intent = new Intent(INTENT_SET_PREF_NETWORK_TYPE);
        intent.putExtra(INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE, NETWORK_MODE_LTE_GSM_WCDMA);
        this.mAlarmIntent = PendingIntent.getBroadcast(this.mDcTrackerBase.mPhone.getContext(), MMS_PROP, intent, 134217728);
        if (this.mAlarmManager != null) {
            this.mAlarmManager.setExact(NETWORK_MODE_UMTS_ONLY, SystemClock.elapsedRealtime() + ((long) delayTime), this.mAlarmIntent);
        }
    }

    public int getDelayTime() {
        if (this.mCurFailCause == DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED || this.mCurFailCause == DcFailCause.USER_AUTHENTICATION) {
            if (isLteRadioTech()) {
                this.mDelayTime = PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_4G;
            } else {
                this.mDelayTime = PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_NOT_4G;
            }
        }
        return this.mDelayTime;
    }

    protected void onActionIntentSetNetworkType(Intent intent) {
        int networkType = intent.getIntExtra(INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE, NETWORK_MODE_LTE_GSM_WCDMA);
        int curPrefMode = Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", networkType);
        log("CLEARCODE switch network type : " + networkType + " curPrefMode = " + curPrefMode);
        if (!(networkType == curPrefMode || curPrefMode == NETWORK_MODE_UMTS_ONLY)) {
            this.mDcTrackerBase.mPhone.setPreferredNetworkType(networkType, null);
            log("CLEARCODE switch network type to 4G and set newRac to -1");
            Global.putInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", networkType);
            this.mGsmServiceStateTracker = this.mDcTrackerBase.mPhone.getServiceStateTracker();
            HwServiceStateManager.getHwGsmServiceStateManager(this.mGsmServiceStateTracker, (GsmCdmaPhone) this.mDcTrackerBase.mPhone).setRac(DATA_ROAMING_EXCEPTION);
        }
        ApnContext defaultApn = (ApnContext) this.mDcTrackerBase.mApnContexts.get("default");
        boolean isDisconnected = (defaultApn.getState() == State.IDLE || defaultApn.getState() == State.FAILED) ? DBG : RESET_PROFILE;
        log("CLEARCODE 2 hours of delay is over,try setup data");
        DcTrackerUtils.cleanUpConnection(this.mDcTrackerBase, isDisconnected ? RESET_PROFILE : DBG, defaultApn);
        setupDataOnConnectableApns(CLEARCODE_2HOUR_DELAY_OVER, null);
    }

    public void unregisterForImsiReady(IccRecords r) {
        r.unregisterForImsiReady(this.mDcTrackerBase);
    }

    public void registerForImsiReady(IccRecords r) {
        r.registerForImsiReady(this.mDcTrackerBase, 270338, null);
    }

    public void unregisterForRecordsLoaded(IccRecords r) {
        r.unregisterForRecordsLoaded(this.mDcTrackerBase);
    }

    public void registerForRecordsLoaded(IccRecords r) {
        r.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
    }

    public void registerForGetAdDone(UiccCardApplication newUiccApplication) {
        newUiccApplication.registerForGetAdDone(this.mDcTrackerBase, 270338, null);
    }

    public void unregisterForGetAdDone(UiccCardApplication newUiccApplication) {
        newUiccApplication.unregisterForGetAdDone(this.mDcTrackerBase);
    }

    public void registerForImsi(UiccCardApplication newUiccApplication, IccRecords newIccRecords) {
        if (this.SETAPN_UNTIL_CARDLOADED) {
            newIccRecords.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
            return;
        }
        switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues()[newUiccApplication.getType().ordinal()]) {
            case SUB2 /*1*/:
            case NETWORK_MODE_UMTS_ONLY /*2*/:
                log("New CSIM records found");
                newIccRecords.registerForImsiReady(this.mDcTrackerBase, 271144, null);
                break;
            case NETWORK_MODE_GSM_UMTS /*3*/:
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                log("New USIM records found");
                newUiccApplication.registerForGetAdDone(this.mDcTrackerBase, 271144, null);
                break;
            default:
                log("New other records found");
                break;
        }
        newIccRecords.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
    }

    public boolean checkMvnoParams() {
        boolean z = RESET_PROFILE;
        String operator = this.mDcTrackerBase.getCTOperator(this.mDcTrackerBase.getOperatorNumeric());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId()))) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId()));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        if (operator != null) {
            String selection = "numeric = '" + operator + "'";
            log("checkMvnoParams: selection=" + selection);
            Cursor cursor = this.mDcTrackerBase.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, "_id");
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    z = checkMvno(cursor);
                }
                cursor.close();
            }
        }
        log("checkMvnoParams: X result = " + z);
        return z;
    }

    private boolean checkMvno(Cursor cursor) {
        if (cursor.moveToFirst()) {
            do {
                String mvnoType = cursor.getString(cursor.getColumnIndexOrThrow("mvno_type"));
                String mvnoMatchData = cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data"));
                if (!TextUtils.isEmpty(mvnoType) && !TextUtils.isEmpty(mvnoMatchData)) {
                    log("checkMvno: X has mvno paras");
                    return DBG;
                }
            } while (cursor.moveToNext());
        }
        return RESET_PROFILE;
    }

    public void registerForFdnRecordsLoaded(IccRecords r) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            r.registerForFdnRecordsLoaded(this.mDcTrackerBase, NETWORK_MODE_UMTS_ONLY, null);
        }
    }

    public void unregisterForFdnRecordsLoaded(IccRecords r) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            r.unregisterForFdnRecordsLoaded(this.mDcTrackerBase);
        }
    }

    public void registerForFdn() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            log("registerForFdn");
            this.mUiccController.registerForFdnStatusChange(this.mDcTrackerBase, SUB2, null);
            this.mFdnChangeObserver = new FdnChangeObserver();
            ContentResolver cr = this.mDcTrackerBase.mPhone.getContext().getContentResolver();
            cr.registerContentObserver(FDN_URL, DBG, this.mFdnChangeObserver);
            this.mFdnAsyncQuery = new FdnAsyncQueryHandler(cr);
        }
    }

    public void unregisterForFdn() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            log("unregisterForFdn");
            this.mUiccController.unregisterForFdnStatusChange(this.mDcTrackerBase);
            if (this.mFdnChangeObserver != null) {
                this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mFdnChangeObserver);
            }
        }
    }

    public boolean isPsAllowedByFdn() {
        long curSubId = (long) this.mDcTrackerBase.mPhone.getSubId();
        String isFdnActivated1 = SystemProperties.get("gsm.hw.fdn.activated1", "false");
        String isFdnActivated2 = SystemProperties.get("gsm.hw.fdn.activated2", "false");
        String isPSAllowedByFdn1 = SystemProperties.get("gsm.hw.fdn.ps.flag.exists1", "false");
        String isPSAllowedByFdn2 = SystemProperties.get("gsm.hw.fdn.ps.flag.exists2", "false");
        log("fddn isPSAllowedByFdn ,isFdnActivated1:" + isFdnActivated1 + " ,isFdnActivated2:" + isFdnActivated2 + " ,isPSAllowedByFdn1:" + isPSAllowedByFdn1 + " ,isPSAllowedByFdn2:" + isPSAllowedByFdn2);
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            if (curSubId == 0 && "true".equals(isFdnActivated1) && "false".equals(isPSAllowedByFdn1)) {
                return RESET_PROFILE;
            }
            if (curSubId == 1 && "true".equals(isFdnActivated2) && "false".equals(isPSAllowedByFdn2)) {
                return RESET_PROFILE;
            }
        }
        return DBG;
    }

    public void handleCustMessage(Message msg) {
        switch (msg.what) {
            case SUB2 /*1*/:
            case NETWORK_MODE_UMTS_ONLY /*2*/:
                log("fddn msg.what = " + msg.what);
                retryDataConnectionByFdn();
            default:
        }
    }

    public void retryDataConnectionByFdn() {
        if (this.mDcTrackerBase.mPhone.getSubId() != SubscriptionController.getInstance().getCurrentDds()) {
            log("fddn retryDataConnectionByFdn, not dds sub, do nothing.");
            return;
        }
        if (isPsAllowedByFdn()) {
            log("fddn retryDataConnectionByFdn, FDN status change and PS is enable, try setup data.");
            setupDataOnConnectableApns("psRestrictDisabled", null);
        } else {
            log("fddn retryDataConnectionByFdn, PS restricted by FDN, cleaup all connections.");
            this.mDcTrackerBase.cleanUpAllConnections(DBG, "psRestrictEnabled");
        }
    }

    private void asyncQueryContact() {
        long subId = (long) this.mDcTrackerBase.mPhone.getSubId();
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            FdnAsyncQueryHandler fdnAsyncQueryHandler = this.mFdnAsyncQuery;
            Uri withAppendedId = ContentUris.withAppendedId(FDN_URL, subId);
            String[] strArr = new String[SUB2];
            strArr[MMS_PROP] = "number";
            fdnAsyncQueryHandler.startQuery(MMS_PROP, null, withAppendedId, strArr, null, null, null);
        }
    }

    public boolean isActiveDataSubscription() {
        log("isActiveDataSubscription getSubId= " + this.mDcTrackerBase.mPhone.getSubId() + "mCurrentDds" + SubscriptionController.getInstance().getCurrentDds());
        return this.mDcTrackerBase.mPhone.getSubId() == SubscriptionController.getInstance().getCurrentDds() ? DBG : RESET_PROFILE;
    }

    public int get4gSlot() {
        int slot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (slot == 0) {
        }
        return slot;
    }

    public int get2gSlot() {
        if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == 0) {
            return SUB2;
        }
        return MMS_PROP;
    }

    public void addIfacePhoneHashMap(DcAsyncChannel dcac, HashMap<String, Integer> mIfacePhoneHashMap) {
        LinkProperties tempLinkProperties = dcac.getLinkPropertiesSync();
        if (tempLinkProperties != null) {
            String iface = tempLinkProperties.getInterfaceName();
            if (iface != null) {
                mIfacePhoneHashMap.put(iface, Integer.valueOf(this.mDcTrackerBase.mPhone.getPhoneId()));
            }
        }
    }

    public int getVSimSubId() {
        return HwVSimManager.getDefault().getVSimSubId();
    }

    public void onActionDataSwitch(int currRecoveryAction, int nextAction) {
        Intent doRecoverBroadcast = new Intent("android.intent.action.DATA_CONNECTION_STALL");
        doRecoverBroadcast.putExtra("recoveryAction", currRecoveryAction);
        int subId = this.mDcTrackerBase.mPhone.getSubId();
        doRecoverBroadcast.putExtra("subscription", subId);
        this.mDcTrackerBase.mPhone.getContext().sendBroadcast(doRecoverBroadcast);
        log("send Data switch intent because radio restart can not fix the data call, subId = " + subId);
        this.mDcTrackerBase.putRecoveryAction(nextAction);
    }

    public void addIntentFilter(IntentFilter filter) {
        filter.addAction("android.intent.action.INTELLGENT_DATA_SWITCH_IS_ON");
        filter.addAction("android.intent.action.INTELLGENT_DATA_SWITCH_IS_OFF");
    }

    public void disposeAddedIntent(String action) {
        if ("android.intent.action.INTELLGENT_DATA_SWITCH_IS_ON".equals(action)) {
            this.mDcTrackerBase.mIntelligentDataSwitchIsOn = DBG;
            log("ACTION_INTELLIGENT_DATA_SWITCH_IS_ON: mIntelligentDataSwitchIsOn=" + this.mDcTrackerBase.mIntelligentDataSwitchIsOn);
        } else if ("android.intent.action.INTELLGENT_DATA_SWITCH_IS_OFF".equals(action)) {
            this.mDcTrackerBase.mIntelligentDataSwitchIsOn = RESET_PROFILE;
            log("ACTION_INTELLIGENT_DATA_SWITCH_IS_OFF: mIntelligentDataSwitchIsOn=" + this.mDcTrackerBase.mIntelligentDataSwitchIsOn);
        }
    }

    public void sendRoamingDataStatusChangBroadcast() {
        this.mDcTrackerBase.mPhone.getContext().sendBroadcast(new Intent("com.android.huawei.INTERNATIONAL_ROAMING_DATA_STATUS_CHANGED"));
    }

    public void sendDSMipErrorBroadcast() {
        if (SystemProperties.getBoolean("ro.config.hw_mip_error_dialog", RESET_PROFILE)) {
            this.mDcTrackerBase.mPhone.getContext().sendBroadcast(new Intent("com.android.huawei.DATA_CONNECTION_MOBILE_IP_ERROR"));
        }
    }

    public boolean enableTcpUdpSumForDataStall() {
        return SystemProperties.getBoolean("ro.hwpp_enable_tcp_udp_sum", RESET_PROFILE);
    }

    public String networkTypeToApnType(int networkType) {
        switch (networkType) {
            case MMS_PROP /*0*/:
                return "default";
            case NETWORK_MODE_UMTS_ONLY /*2*/:
                return "mms";
            case NETWORK_MODE_GSM_UMTS /*3*/:
                return "supl";
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                return "dun";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                return "hipri";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_NETWORK_TYPE /*10*/:
                return "fota";
            case HwVSimUtilsInner.VSIM /*11*/:
                return "ims";
            case HwVSimEventReport.VSIM_PROCESS_TYPE_DB /*12*/:
                return "cbs";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_DISABLE_VSIM_DONE /*14*/:
                return "ia";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_PLMN_SELINFO /*15*/:
                return "emergency";
            case 38:
                return "bip0";
            case 39:
                return "bip1";
            case HwVSimConstants.CMD_ENABLE_VSIM /*40*/:
                return "bip2";
            case HwVSimConstants.EVENT_RADIO_POWER_OFF_DONE /*41*/:
                return "bip3";
            case HwVSimConstants.EVENT_CARD_POWER_OFF_DONE /*42*/:
                return "bip4";
            case HwVSimConstants.EVENT_SWITCH_SLOT_DONE /*43*/:
                return "bip5";
            case HwVSimConstants.EVENT_SET_TEE_DATA_READY_DONE /*44*/:
                return "bip6";
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE /*45*/:
                return "xcap";
            default:
                log("Error mapping networkType " + networkType + " to apnType");
                return "";
        }
    }

    public boolean isApnTypeDisabled(String apnType) {
        if (TextUtils.isEmpty(apnType)) {
            return RESET_PROFILE;
        }
        String[] disabledApnTypes = "ro.hwpp.disabled_apn_type".split(",");
        int length = disabledApnTypes.length;
        for (int i = MMS_PROP; i < length; i += SUB2) {
            if (apnType.equals(disabledApnTypes[i])) {
                return DBG;
            }
        }
        return RESET_PROFILE;
    }

    public boolean isNeedDataRoamingExpend() {
        if (this.mDcTrackerBase.mPhone == null || this.mDcTrackerBase.mPhone.mIccRecords == null || this.mDcTrackerBase.mPhone.mIccRecords.get() == null) {
            log("mPhone or mIccRecords is null");
            return RESET_PROFILE;
        }
        String plmnsConfig = System.getString(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "hw_data_roam_option");
        if (TextUtils.isEmpty(plmnsConfig)) {
            log("plmnConfig is Empty");
            return RESET_PROFILE;
        } else if ("ALL".equals(plmnsConfig)) {
            return DBG;
        } else {
            String mccmnc = ((IccRecords) this.mDcTrackerBase.mPhone.mIccRecords.get()).getOperatorNumeric();
            String[] plmns = plmnsConfig.split(",");
            int length = plmns.length;
            for (int i = MMS_PROP; i < length; i += SUB2) {
                String plmn = plmns[i];
                if (!TextUtils.isEmpty(plmn) && plmn.equals(mccmnc)) {
                    return DBG;
                }
            }
            return RESET_PROFILE;
        }
    }

    public boolean setDataRoamingScope(int scope) {
        log("dram setDataRoamingScope scope " + scope);
        if (scope < 0 || scope > NETWORK_MODE_UMTS_ONLY) {
            return RESET_PROFILE;
        }
        if (getDataRoamingScope() != scope) {
            Global.putInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), getDataRoamingSettingItem("data_roaming"), scope);
            if (this.mDcTrackerBase.mPhone.getServiceState() != null && this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
                log("dram setDataRoamingScope send EVENT_ROAMING_ON");
                this.mDcTrackerBase.sendMessage(this.mDcTrackerBase.obtainMessage(270347));
            }
        }
        return DBG;
    }

    public int getDataRoamingScope() {
        try {
            return Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), getDataRoamingSettingItem("data_roaming"));
        } catch (SettingNotFoundException e) {
            return DATA_ROAMING_EXCEPTION;
        }
    }

    public boolean getDataRoamingEnabledWithNational() {
        boolean result = DBG;
        int dataRoamingScope = getDataRoamingScope();
        if (dataRoamingScope == 0 || (SUB2 == dataRoamingScope && isInternationalRoaming())) {
            result = RESET_PROFILE;
        }
        log("dram getDataRoamingEnabledWithNational result " + result + " dataRoamingScope " + dataRoamingScope);
        return result;
    }

    public boolean isInternationalRoaming() {
        if (this.mDcTrackerBase.mPhone == null || this.mDcTrackerBase.mPhone.mIccRecords == null || this.mDcTrackerBase.mPhone.mIccRecords.get() == null) {
            log("mPhone or mIccRecords is null");
            return RESET_PROFILE;
        } else if (this.mDcTrackerBase.mPhone.getServiceState() == null) {
            log("dram isInternationalRoaming ServiceState is not start up");
            return RESET_PROFILE;
        } else if (this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
            String simNumeric = ((IccRecords) this.mDcTrackerBase.mPhone.mIccRecords.get()).getOperatorNumeric();
            String operatorNumeric = this.mDcTrackerBase.mPhone.getServiceState().getOperatorNumeric();
            if (TextUtils.isEmpty(simNumeric) || TextUtils.isEmpty(operatorNumeric)) {
                log("dram isInternationalRoaming SIMNumeric or OperatorNumeric is not got!");
                return RESET_PROFILE;
            }
            log("dram isInternationalRoaming simNumeric " + simNumeric + " operatorNumeric " + operatorNumeric);
            if (simNumeric.length() <= NETWORK_MODE_GSM_UMTS || operatorNumeric.length() <= NETWORK_MODE_GSM_UMTS || simNumeric.substring(MMS_PROP, NETWORK_MODE_GSM_UMTS).equals(operatorNumeric.substring(MMS_PROP, NETWORK_MODE_GSM_UMTS))) {
                return RESET_PROFILE;
            }
            return DBG;
        } else {
            log("dram isInternationalRoaming Current service state is not roaming, bail ");
            return RESET_PROFILE;
        }
    }

    private void onVoiceCallEndedHw() {
        log("onVoiceCallEndedHw");
        if (!HwModemCapability.isCapabilitySupport(MMS_PROP)) {
            int currentSub = this.mDcTrackerBase.mPhone.getPhoneId();
            SubscriptionController subscriptionController = SubscriptionController.getInstance();
            int defaultDataSubId = subscriptionController.getDefaultDataSubId();
            if (subscriptionController.getSubState(defaultDataSubId) == 0 && currentSub != defaultDataSubId) {
                log("defaultDataSub " + defaultDataSubId + " is inactive, set dataSubId to " + currentSub);
                subscriptionController.setDefaultDataSubId(currentSub);
            }
            if (this.mDcTrackerBase.mPhone.getServiceStateTracker() != null) {
                this.mDcTrackerBase.mPhone.notifyServiceStateChangedP(this.mDcTrackerBase.mPhone.getServiceStateTracker().mSS);
            }
        }
    }
}
