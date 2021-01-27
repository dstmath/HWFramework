package com.android.internal.telephony.gsm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.ToneGenerator;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CsgSearch;
import com.android.internal.telephony.CsgSearchFactory;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwQualcommCsgSearch;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HwCustGsmServiceStateTrackerImpl extends HwCustGsmServiceStateTracker {
    private static final String ACTION_ENFORCE_LTE_NETWORKTYPE = "android.intent.action.enforce_lte_networktype";
    private static final String ACTION_LTE_CA_STATE = "com.huawei.intent.action.LTE_CA_STATE";
    private static final int CAUSE_BY_DATA = 0;
    private static final int CAUSE_BY_ROAM = 1;
    private static final int CONGESTTION = 22;
    private static final int CS_ENABLED = 1003;
    private static final int CS_NORMAL_ENABLED = 1005;
    private static final int DATA_DISABLED = 0;
    private static final int DATA_ENABLED = 1;
    private static final boolean DBG = true;
    private static final String DEFAULT_PCO_DATA = "-2;-2";
    private static final int DIALOG_TIMEOUT = 120000;
    private static final int ENFORCE_LTE_NETWORKTYPE_PENDING_TIME = 10000;
    private static final int EPS_SERVICES_AND_NON_EPS_SERVICES_NOT_ALLOWED = 8;
    private static final int ESM_FAILURE = 19;
    private static final int EVENT_GET_CELL_INFO_LIST_OTDOA = 73;
    private static final int EVENT_GET_LTE_FREQ_WITH_WLAN_COEX = 63;
    private static final int EVENT_POLL_STATE_REGISTRATION = 4;
    private static final int EVENT_SET_NETWORK_MODE_DONE = 101;
    private static final int FOCUS_BEEP_VOLUME = 100;
    private static final int GPRS_SERVICES_NOT_ALLOWED = 7;
    private static final int GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN = 14;
    private static final boolean HW_ATT_SHOW_NET_REJ = SystemProperties.getBoolean("ro.config.hw_showNetworkReject", false);
    private static final boolean HW_SIM_ACTIVATION = SystemProperties.getBoolean("ro.config.hw_sim_activation", false);
    private static final int ILLEGAL_ME = 6;
    private static final int ILLEGAL_MS = 3;
    private static final int IMSI_UNKNOWN_IN_HLR = 2;
    private static final boolean IS_DATAONLY_LOCATION_ENABLED = SystemProperties.getBoolean("ro.config.hw_gmap_enabled", false);
    private static final boolean IS_DELAY_UPDATENAME = SystemProperties.getBoolean("ro.config.delay_updatename", false);
    private static final boolean IS_DELAY_UPDATENAME_LAC_NULL = SystemProperties.getBoolean("ro.config.lac_null_delay_update", false);
    private static final boolean IS_EMERGENCY_SHOWS_NOSERVICE = SystemProperties.getBoolean("ro.config.LTE_NO_SERVICE", false);
    private static final boolean IS_KT = (SystemProperties.get(OPTA_PROP_KEY, "0").equals("710") && SystemProperties.get(OPTB_PROP_KEY, "0").equals("410"));
    private static final boolean IS_LGU;
    private static final boolean IS_SIM_POWER_DOWN = SystemProperties.getBoolean("ro.config.SimPowerOperation", false);
    private static final boolean IS_UPDATE_LAC_CID = SystemProperties.getBoolean("ro.config.hw_update_lac_cid", false);
    private static final boolean IS_VERIZON;
    private static final long LAST_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    private static final int LA_NOT_ALLOWED = 12;
    private static final String LOG_TAG = "HwCustGsmServiceStateTrackerImpl";
    private static final String MANUAL_SELECT_FLAG = "manual_select_flag";
    private static final String MANUAL_SET_3G_FLAG = "manual_set_3g_flag";
    private static final int MSC_TEMPORARILY_NOT_REACHABLE = 16;
    private static final int MSG_ID_TIMEOUT = 1;
    private static final int NATIONAL_ROAMING_NOT_ALLOWED = 13;
    private static final int NETWORK_FAILURE = 17;
    private static final int NO_SUITABLE_CELLS_IN_LA = 15;
    private static final String OPTA_PROP_KEY = "ro.config.hw_opta";
    private static final String OPTB_PROP_KEY = "ro.config.hw_optb";
    private static final String PCO_DATA = "pco_data";
    private static final int PLMN_NOT_ALLOWED = 11;
    private static final int PREFERRED_NETWORK_TYPE = SystemProperties.getInt("ro.telephony.default_network", 0);
    private static final int RAT_LTE = 2;
    private static final int RAT_WCDMA = 1;
    private static final int REQUESTED_SERVICE_NOT_AUTHORIZED = 35;
    private static final int ROAM_DISABLED = 0;
    private static final int ROAM_ENABLED = 1;
    private static final boolean SHOW_REJ_NOTIFICATION_KO = SystemProperties.getBoolean("ro.config.show_rej_kt", false);
    private static final String SIM_ONE_LTE_ROAMING = "sim_one_lte_roaming";
    private static final String SIM_TWO_LTE_ROAMING = "sim_two_lte_roaming";
    private static final int STATE_ENABLED = 1;
    private static final int UNKNOWN_STATE = -1;
    private static final boolean VDBG = false;
    private static boolean mIsSupportCsgSearch = SystemProperties.getBoolean("ro.config.att.csg", false);
    private int dialogCanceled = 0;
    private PendingIntent enforcePendingLTENetworkTypeIntent = null;
    private CommandsInterface mCi;
    private CsgSearch mCsgSrch;
    private ContentObserver mDataEnabledObserver = new ContentObserver(new Handler()) {
        /* class com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.AnonymousClass2 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Rlog.d(HwCustGsmServiceStateTrackerImpl.LOG_TAG, "Data enabled state changed ");
            if (HwCustGsmServiceStateTrackerImpl.this.isDataOffForbidLTE()) {
                HwCustGsmServiceStateTrackerImpl.this.onDataEnableChanged();
            }
            if (HwCustGsmServiceStateTrackerImpl.this.isDataOffbyRoamAndData()) {
                HwCustGsmServiceStateTrackerImpl.this.mIsDataChanged = HwCustGsmServiceStateTrackerImpl.DBG;
                HwCustGsmServiceStateTrackerImpl.this.onDataMobileChanged();
            }
        }
    };
    private ContentObserver mDataRoamingObserver = new ContentObserver(new Handler()) {
        /* class com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.AnonymousClass3 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Rlog.d(HwCustGsmServiceStateTrackerImpl.LOG_TAG, "Data roaming state changed ");
            HwCustGsmServiceStateTrackerImpl.this.mIsRoamingChanged = HwCustGsmServiceStateTrackerImpl.DBG;
            HwCustGsmServiceStateTrackerImpl.this.onDataRoamingChanged();
        }
    };
    private boolean mEnforceLTEPending;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && HwCustGsmServiceStateTrackerImpl.ACTION_ENFORCE_LTE_NETWORKTYPE.equals(intent.getAction())) {
                Rlog.d(HwCustGsmServiceStateTrackerImpl.LOG_TAG, "Enforce LTE pending timer expired!");
                if (HwCustGsmServiceStateTrackerImpl.this.mEnforceLTEPending) {
                    HwCustGsmServiceStateTrackerImpl.this.processEnforceLTENetworkTypePending();
                }
            }
        }
    };
    private boolean mIsCaState;
    private boolean mIsDataChanged = false;
    private boolean mIsExtPlmnSent = false;
    private boolean mIsLTEBandWidthChanged = false;
    private boolean mIsRoamingChanged = false;
    private List<CellInfo> mLastEnhancedCellInfoList = null;
    private long mLastEnhancedCellInfoListTime;
    private Handler mMyHandler = new Handler() {
        /* class com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.AnonymousClass4 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == HwCustGsmServiceStateTrackerImpl.EVENT_SET_NETWORK_MODE_DONE) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar == null || ar.exception != null) {
                    Rlog.e(HwCustGsmServiceStateTrackerImpl.LOG_TAG, "set prefer network mode failed!");
                    return;
                }
                int setPrefMode = ((Integer) ar.userObj).intValue();
                Rlog.d(HwCustGsmServiceStateTrackerImpl.LOG_TAG, "set prefer network mode == " + setPrefMode);
                HwTelephonyManagerInner.getDefault().saveNetworkModeToDB(HwCustGsmServiceStateTrackerImpl.this.mGsmPhone.getPhoneId(), setPrefMode);
            }
        }
    };
    private DialogInterface.OnCancelListener mShowRejMsgOnCancelListener = new DialogInterface.OnCancelListener() {
        /* class com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.AnonymousClass6 */

        @Override // android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialog) {
            HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
            if (HwCustGsmServiceStateTrackerImpl.this.mToneGenerator != null) {
                HwCustGsmServiceStateTrackerImpl.this.mToneGenerator.release();
                HwCustGsmServiceStateTrackerImpl.this.mToneGenerator = null;
            }
            HwCustGsmServiceStateTrackerImpl.this.dialogCanceled = 0;
        }
    };
    private DialogInterface.OnKeyListener mShowRejMsgOnKeyListener = new DialogInterface.OnKeyListener() {
        /* class com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.AnonymousClass7 */

        @Override // android.content.DialogInterface.OnKeyListener
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (82 == keyCode || dialog == null) {
                return false;
            }
            dialog.dismiss();
            HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
            if (HwCustGsmServiceStateTrackerImpl.this.mToneGenerator != null) {
                HwCustGsmServiceStateTrackerImpl.this.mToneGenerator.release();
                HwCustGsmServiceStateTrackerImpl.this.mToneGenerator = null;
            }
            HwCustGsmServiceStateTrackerImpl.this.dialogCanceled = 0;
            return HwCustGsmServiceStateTrackerImpl.DBG;
        }
    };
    private String mSimRecordVoicemail = "";
    private Handler mTimeoutHandler = new Handler() {
        /* class com.android.internal.telephony.gsm.HwCustGsmServiceStateTrackerImpl.AnonymousClass5 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (HwCustGsmServiceStateTrackerImpl.this.dialogCanceled > 0) {
                    HwCustGsmServiceStateTrackerImpl.access$610(HwCustGsmServiceStateTrackerImpl.this);
                } else if (HwCustGsmServiceStateTrackerImpl.this.networkDialog != null && HwCustGsmServiceStateTrackerImpl.this.dialogCanceled == 0) {
                    HwCustGsmServiceStateTrackerImpl.this.networkDialog.dismiss();
                    HwCustGsmServiceStateTrackerImpl.this.networkDialog = null;
                    if (HwCustGsmServiceStateTrackerImpl.this.mToneGenerator != null) {
                        HwCustGsmServiceStateTrackerImpl.this.mToneGenerator.release();
                        HwCustGsmServiceStateTrackerImpl.this.mToneGenerator = null;
                    }
                }
            }
        }
    };
    private ToneGenerator mToneGenerator = null;
    private String mUlbwDlbwString;
    private boolean[] mlteEmmCauseRecords;
    private AlertDialog networkDialog = null;
    private int oldRejCode = 0;

    static /* synthetic */ int access$610(HwCustGsmServiceStateTrackerImpl x0) {
        int i = x0.dialogCanceled;
        x0.dialogCanceled = i + UNKNOWN_STATE;
        return i;
    }

    static {
        boolean equals = SystemProperties.get(OPTA_PROP_KEY, "0").equals("627");
        boolean z = DBG;
        IS_LGU = equals && SystemProperties.get(OPTB_PROP_KEY, "0").equals("410");
        if (!"389".equals(SystemProperties.get(OPTA_PROP_KEY)) || !"840".equals(SystemProperties.get(OPTB_PROP_KEY))) {
            z = false;
        }
        IS_VERIZON = z;
    }

    public HwCustGsmServiceStateTrackerImpl(GsmCdmaPhone gsmPhone) {
        super(gsmPhone);
        if (CsgSearch.isSupportCsgSearch()) {
            this.mCsgSrch = CsgSearchFactory.createCsgSearch(gsmPhone);
        } else {
            this.mCsgSrch = null;
        }
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        this.mlteEmmCauseRecords = new boolean[phoneCount];
        for (int i = 0; i < phoneCount; i++) {
            this.mlteEmmCauseRecords[i] = DBG;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDataRoamingChanged() {
        int dataRoaming = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "data_roaming", 0);
        Rlog.d(LOG_TAG, "data roaming change to:" + dataRoaming);
        if (dataRoaming == 1) {
            this.mIsRoamingChanged = false;
        } else if (!this.mGsmPhone.getServiceState().getDataRoaming()) {
            Rlog.d(LOG_TAG, "device is not roaming , set roam state immediately");
            setDataOrRoamState(1);
        } else if (this.mGsmPhone.getDcTracker(1).isDisconnected()) {
            Rlog.d(LOG_TAG, "Data disconnected, set roam state immediately");
            setDataOrRoamState(1);
        } else {
            Rlog.d(LOG_TAG, "Data not disconnected, pending");
            enforceLTEPending();
            this.mEnforceLTEPending = DBG;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDataMobileChanged() {
        int dataEnabled = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "mobile_data", 1);
        Rlog.d(LOG_TAG, "Mobile data change to:" + dataEnabled);
        if (dataEnabled == 1) {
            this.mIsDataChanged = false;
        } else if (this.mGsmPhone.getDcTracker(1).isDisconnected()) {
            Rlog.d(LOG_TAG, "Data disconnected, set mobile stateimmediately");
            setDataOrRoamState(0);
        } else {
            Rlog.d(LOG_TAG, "Data not disconnected, pending");
            enforceLTEPending();
            this.mEnforceLTEPending = DBG;
        }
    }

    private void setDataOrRoamState(int cause) {
        int mobileData = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "mobile_data", 1);
        int dataRoaming = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "data_roaming", 0);
        if (cause == 0) {
            if (SystemProperties.get("gsm.isuser.setdata", "true").equals("true")) {
                this.mIsDataChanged = false;
                setMobileDataEnable(mobileData);
            } else {
                SystemProperties.set("gsm.isuser.setdata", "true");
            }
        }
        if (cause == 1) {
            this.mIsRoamingChanged = false;
            setRoamingDataEnable(dataRoaming);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDataEnableChanged() {
        int dataEnabled = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "mobile_data", 1);
        Rlog.d(LOG_TAG, "Mobile data change to:" + dataEnabled);
        if (dataEnabled == 1) {
            Rlog.d(LOG_TAG, "Data on, enforce LTE immediately!");
            enforceLTENetworkType();
        } else if (this.mGsmPhone.getDcTracker(1).isDisconnected()) {
            Rlog.d(LOG_TAG, "Data disconnected, enforce LTE immediately");
            enforceLTENetworkType();
        } else {
            Rlog.d(LOG_TAG, "Data not disconnected, enforce LTE pending");
            enforceLTEPending();
            this.mEnforceLTEPending = DBG;
        }
    }

    private void enforceLTEPending() {
        if (this.enforcePendingLTENetworkTypeIntent != null) {
            Rlog.d(LOG_TAG, "enforcePendingLTENetworkTypeIntent already exists ,not recreate");
            return;
        }
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mGsmPhone.getContext(), 0, new Intent(ACTION_ENFORCE_LTE_NETWORKTYPE), 134217728);
        ((AlarmManager) this.mGsmPhone.getContext().getSystemService("alarm")).set(2, SystemClock.elapsedRealtime() + 10000, alarmIntent);
        this.enforcePendingLTENetworkTypeIntent = alarmIntent;
    }

    public void processEnforceLTENetworkTypePending() {
        if (this.enforcePendingLTENetworkTypeIntent == null && this.mEnforceLTEPending) {
            Rlog.d(LOG_TAG, "No enforce LTE network type pending!");
        } else if (this.mEnforceLTEPending) {
            this.enforcePendingLTENetworkTypeIntent.cancel();
            this.enforcePendingLTENetworkTypeIntent = null;
            int dataEnabled = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "mobile_data", 1);
            Rlog.d(LOG_TAG, "Before enforce LTE pending, mobile data is:" + dataEnabled);
            this.mEnforceLTEPending = false;
            if (isDataOffForbidLTE() && dataEnabled == 0) {
                enforceLTENetworkType();
            }
            if (isDataOffbyRoamAndData()) {
                int roamEnabled = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "data_roaming", 1);
                Rlog.d(LOG_TAG, "Before enforce setDataOrRoamState, mobile data is:" + dataEnabled + " data roam is " + roamEnabled);
                if (roamEnabled == 0 && this.mIsRoamingChanged) {
                    setDataOrRoamState(1);
                }
                if (dataEnabled == 0 && this.mIsDataChanged) {
                    setDataOrRoamState(0);
                }
            }
        }
    }

    private void enforceLTENetworkType() {
        boolean deleteLTE = false;
        int mobileData = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "mobile_data", 1);
        Rlog.d(LOG_TAG, "mobileData:" + mobileData);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mGsmPhone.getContext());
        boolean manualSelectNetorkFlag = prefs.getBoolean(MANUAL_SELECT_FLAG, false);
        boolean manualSet3GFlag = prefs.getBoolean(MANUAL_SET_3G_FLAG, false);
        int mNetworkMode = HwTelephonyManagerInner.getDefault().getNetworkModeFromDB(this.mGsmPhone.getPhoneId());
        if (mobileData == 0) {
            deleteLTE = DBG;
            if (manualSelectNetorkFlag && mNetworkMode == LA_NOT_ALLOWED) {
                deleteLTE = false;
            }
        } else {
            if (mNetworkMode == 2 && (manualSelectNetorkFlag || manualSet3GFlag)) {
                deleteLTE = DBG;
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(MANUAL_SELECT_FLAG, false);
            editor.commit();
        }
        if (this.mCi instanceof RIL) {
            int networkTypeToSet = getNetworkTypeToSet(deleteLTE);
            Rlog.d(LOG_TAG, "networkTypeSetted==" + SystemProperties.getInt("persist.radio.prefered_network", (int) GPRS_SERVICES_NOT_ALLOWED) + ",networkTypeToSet ==" + networkTypeToSet);
            if (networkTypeToSet != SystemProperties.getInt("persist.radio.prefered_network", (int) GPRS_SERVICES_NOT_ALLOWED)) {
                this.mCi.setPreferredNetworkType(networkTypeToSet, this.mMyHandler.obtainMessage(EVENT_SET_NETWORK_MODE_DONE, Integer.valueOf(networkTypeToSet)));
            }
        }
    }

    private int getNetworkTypeToSet(boolean deleteLTE) {
        int networkType;
        Rlog.d(LOG_TAG, "enforceLTENetworkType, deleteLTE: " + deleteLTE);
        if (deleteLTE) {
            networkType = deleteLTENetworkType();
        } else {
            networkType = PREFERRED_NETWORK_TYPE;
        }
        Rlog.d(LOG_TAG, "mSetPreferredNetworkType:" + PREFERRED_NETWORK_TYPE + ",networkType:" + networkType);
        return networkType;
    }

    private int deleteLTENetworkType() {
        int pseudoNetworkType = PREFERRED_NETWORK_TYPE;
        switch (PREFERRED_NETWORK_TYPE) {
            case 8:
                pseudoNetworkType = 4;
                break;
            case HwQualcommCsgSearch.CSGNetworkList.RADIO_IF_TDSCDMA /* 9 */:
                pseudoNetworkType = 2;
                break;
            case 10:
                pseudoNetworkType = GPRS_SERVICES_NOT_ALLOWED;
                break;
            case PLMN_NOT_ALLOWED /* 11 */:
                pseudoNetworkType = 2;
                break;
            case LA_NOT_ALLOWED /* 12 */:
                pseudoNetworkType = 2;
                break;
        }
        Rlog.d(LOG_TAG, "deleteLTENetworkType result=== " + pseudoNetworkType);
        return pseudoNetworkType;
    }

    public void initOnce(GsmCdmaPhone phone, CommandsInterface ci) {
        this.mCi = ci;
        phone.getContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor("mobile_data"), DBG, this.mDataEnabledObserver);
        Context context = phone.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ENFORCE_LTE_NETWORKTYPE);
        context.registerReceiver(this.mIntentReceiver, filter);
        if (isDataOffbyRoamAndData()) {
            phone.getContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_roaming"), DBG, this.mDataRoamingObserver);
            setMobileDataEnable(UNKNOWN_STATE);
            setRoamingDataEnable(UNKNOWN_STATE);
        }
    }

    public void dispose(GsmCdmaPhone phone) {
        phone.getContext().getContentResolver().unregisterContentObserver(this.mDataEnabledObserver);
        if (isDataOffbyRoamAndData()) {
            phone.getContext().getContentResolver().unregisterContentObserver(this.mDataRoamingObserver);
        }
    }

    public boolean isDataOffForbidLTE() {
        return SystemProperties.getBoolean("persist.sys.isDataOffForbidLTE", false);
    }

    public boolean isDataOffbyRoamAndData() {
        return SystemProperties.getBoolean("persist.sys.isDataOffByRAD", false);
    }

    private void setMobileDataEnable(int state) {
        if (state == UNKNOWN_STATE) {
            try {
                int newState = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "mobile_data", 1);
                Rlog.d(LOG_TAG, "first set mobile state to " + newState);
                this.mGsmPhone.mCi.setMobileDataEnable(newState, (Message) null);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception in setMobileDataEnable", e);
            }
        } else {
            Rlog.d(LOG_TAG, "user change mobile state to " + state);
            this.mGsmPhone.mCi.setMobileDataEnable(state, (Message) null);
        }
    }

    private void setRoamingDataEnable(int state) {
        if (state == UNKNOWN_STATE) {
            try {
                int newState = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "data_roaming", 1);
                Rlog.d(LOG_TAG, "first set roam state to " + newState);
                this.mGsmPhone.mCi.setRoamingDataEnable(newState, (Message) null);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception in setRoamingDataEnable", e);
            }
        } else {
            Rlog.d(LOG_TAG, "user change roam state to " + state);
            this.mGsmPhone.mCi.setRoamingDataEnable(state, (Message) null);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v1, resolved type: java.lang.StringBuffer */
    /* JADX DEBUG: Multi-variable search result rejected for r7v4, resolved type: java.lang.Object[] */
    /* JADX DEBUG: Multi-variable search result rejected for r7v9, resolved type: java.lang.Object[] */
    /* JADX WARN: Multi-variable type inference failed */
    public void updateRomingVoicemailNumber(ServiceState currentState) {
        IccRecords mIccRecord;
        String custRoamingVoicemail = null;
        try {
            custRoamingVoicemail = SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "hw_cust_roamingvoicemail");
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception get hw_cust_roamingvoicemail value", e);
        }
        if (currentState != null && !TextUtils.isEmpty(custRoamingVoicemail) && (mIccRecord = this.mGsmPhone.getIccRecords()) != null) {
            String hplmn = mIccRecord.getOperatorNumeric();
            String rplmn = currentState.getOperatorNumeric();
            String mVoicemailNum = mIccRecord.getVoiceMailNumber();
            String[] plmns = custRoamingVoicemail.split(",");
            if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(rplmn) && plmns.length == ILLEGAL_MS && !TextUtils.isEmpty(plmns[2])) {
                if (!hplmn.equals(plmns[0]) || !rplmn.equals(plmns[1])) {
                    if (!TextUtils.isEmpty(this.mSimRecordVoicemail) && plmns[2].equals(mVoicemailNum)) {
                        mIccRecord.setVoiceMailNumber(this.mSimRecordVoicemail);
                        this.mSimRecordVoicemail = "";
                    }
                } else if (!plmns[2].equals(mVoicemailNum)) {
                    this.mSimRecordVoicemail = mVoicemailNum;
                    mIccRecord.setVoiceMailNumber(plmns[2]);
                }
            }
        }
    }

    public void setRadioPower(CommandsInterface ci, boolean enabled) {
        if (IS_SIM_POWER_DOWN && ci != null && this.mGsmPhone != null) {
            boolean isAirplaneMode = Settings.Global.getInt(this.mGsmPhone.getContext().getContentResolver(), "airplane_mode_on", 0) == 1;
            try {
                Rlog.d(LOG_TAG, "Set radio power: " + enabled + ", is airplane mode: " + isAirplaneMode);
                if (enabled) {
                    ci.setSimState(1, 1, (Message) null);
                } else if (isAirplaneMode) {
                    ci.setSimState(1, 0, (Message) null);
                }
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception in setRadioPower", e);
            }
        }
    }

    public String setEmergencyToNoService(ServiceState ss, String plmn, boolean emergencyOnly) {
        if (IS_EMERGENCY_SHOWS_NOSERVICE && ss.getRadioTechnology() == GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN && emergencyOnly) {
            return Resources.getSystem().getText(17040422).toString();
        }
        if ("true".equals(Settings.System.getString(this.mGsmPhone.getContext().getContentResolver(), "emergency_shows_noservice"))) {
            int slotId = this.mGsmPhone.getPhoneId();
            boolean isSubInActive = false;
            boolean isSimNotReady = TelephonyManager.getDefault().getSimState(slotId) != 5;
            if (SubscriptionController.getInstance().getSubState(slotId) != 1) {
                isSubInActive = true;
            }
            boolean isVoiceCapable = this.mGsmPhone.getContext().getResources().getBoolean(17891573);
            if ((isSimNotReady || isSubInActive || !isVoiceCapable) && emergencyOnly) {
                return Resources.getSystem().getText(17040422).toString();
            }
        }
        return plmn;
    }

    public void setPsCell(ServiceState ss, GsmCellLocation mNewCellLoc, String[] states) {
        if (ss != null && mNewCellLoc != null && states != null && IS_DATAONLY_LOCATION_ENABLED) {
            int voiceRegState = ss.getVoiceRegState();
            boolean isCsOutOfservice = DBG;
            if (voiceRegState != 1) {
                isCsOutOfservice = false;
            }
            int tac = UNKNOWN_STATE;
            int ci = UNKNOWN_STATE;
            int pci = UNKNOWN_STATE;
            try {
                if (states.length >= ILLEGAL_ME) {
                    ci = Integer.parseInt(states[5]);
                }
                if (states.length >= GPRS_SERVICES_NOT_ALLOWED) {
                    pci = Integer.parseInt(states[ILLEGAL_ME]);
                }
                if (states.length >= 8) {
                    tac = Integer.parseInt(states[GPRS_SERVICES_NOT_ALLOWED]);
                }
            } catch (NumberFormatException e) {
                Rlog.d(LOG_TAG, "error parsing GprsRegistrationState: ");
            }
            if (isCsOutOfservice && tac >= 0 && ci >= 0 && pci >= 0) {
                Rlog.d(LOG_TAG, "data only card use ps cellid to location");
                mNewCellLoc.setLacAndCid(tac, ci);
                mNewCellLoc.setPsc(pci);
            }
        }
    }

    public boolean isInServiceState(int combinedregstate) {
        if (!HW_ATT_SHOW_NET_REJ || !this.mIsExtPlmnSent || combinedregstate != 0) {
            return false;
        }
        return DBG;
    }

    public boolean isInServiceState(ServiceState ss) {
        return isInServiceState(getCombinedRegState(ss));
    }

    public void setExtPlmnSent(boolean value) {
        if (HW_ATT_SHOW_NET_REJ) {
            this.mIsExtPlmnSent = value;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public void custHandlePollStateResult(int what, AsyncResult ar, int[] pollingContext) {
        if (HW_ATT_SHOW_NET_REJ && ar.userObj != pollingContext && ar.exception == null && what == 4) {
            try {
                String[] states = (String[]) ar.result;
                if (states != null && states.length > NATIONAL_ROAMING_NOT_ALLOWED) {
                    handleNetworkRejection(Integer.parseInt(states[NATIONAL_ROAMING_NOT_ALLOWED]));
                }
            } catch (RuntimeException e) {
            }
        }
    }

    public void handleNetworkRejection(int regState, String[] states) {
        if (!HW_ATT_SHOW_NET_REJ) {
            Rlog.d(LOG_TAG, "HW_ATT_SHOW_NET_REJ is disable.");
        } else if (states == null) {
            Rlog.d(LOG_TAG, "States is null.");
        } else if ((regState == ILLEGAL_MS || !(this.mGsmPhone == null || this.mGsmPhone.getServiceState() == null || !this.mGsmPhone.getServiceState().isEmergencyOnly())) && states.length >= GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN) {
            try {
                handleNetworkRejection(Integer.parseInt(states[NATIONAL_ROAMING_NOT_ALLOWED]));
            } catch (NumberFormatException ex) {
                Rlog.e(LOG_TAG, "error parsing regCode: " + ex);
            }
        }
    }

    private void showDialog(String msg, int regCode) {
        if (regCode != this.oldRejCode) {
            AlertDialog alertDialog = this.networkDialog;
            if (alertDialog != null) {
                this.dialogCanceled++;
                alertDialog.dismiss();
            }
            this.networkDialog = new AlertDialog.Builder(this.mGsmPhone.getContext()).setMessage(msg).setCancelable(DBG).create();
            this.networkDialog.getWindow().setType(2008);
            this.networkDialog.setOnKeyListener(this.mShowRejMsgOnKeyListener);
            this.networkDialog.setOnCancelListener(this.mShowRejMsgOnCancelListener);
            this.networkDialog.show();
            Handler handler = this.mTimeoutHandler;
            handler.sendMessageDelayed(handler.obtainMessage(1), 120000);
            try {
                this.mToneGenerator = new ToneGenerator(1, FOCUS_BEEP_VOLUME);
                this.mToneGenerator.startTone(28);
            } catch (RuntimeException e) {
                this.mToneGenerator = null;
            }
        }
    }

    private int getCombinedRegState(ServiceState ss) {
        if (ss == null) {
            return 1;
        }
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if (regState != 1 || dataRegState != 0) {
            return regState;
        }
        Rlog.e(LOG_TAG, "getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }

    private void handleNetworkRejection(int rejCode) {
        Resources r = Resources.getSystem();
        String plmn = r.getText(17040034).toString();
        if (rejCode == 2) {
            showDialog(r.getString(33685992), rejCode);
            handleShowLimitedService(plmn);
        } else if (rejCode == ILLEGAL_MS) {
            showDialog(r.getString(33685993), rejCode);
            handleShowLimitedService(plmn);
        } else if (rejCode != ILLEGAL_ME) {
            if (!(rejCode == NO_SUITABLE_CELLS_IN_LA || rejCode == NETWORK_FAILURE)) {
                switch (rejCode) {
                }
            }
            handleShowLimitedService(" ");
        } else {
            showDialog(r.getString(33685994), rejCode);
            handleShowLimitedService(plmn);
        }
        this.oldRejCode = rejCode;
    }

    private void handleShowLimitedService(String plmn) {
        Intent intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
        intent.putExtra("showSpn", false);
        intent.putExtra("spn", "");
        intent.putExtra("showPlmn", DBG);
        intent.putExtra("plmn", plmn);
        this.mGsmPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        this.mIsExtPlmnSent = DBG;
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        CsgSearch csgSearch = this.mCsgSrch;
        if (csgSearch != null && mIsSupportCsgSearch) {
            csgSearch.judgeToLaunchCsgPeriodicSearchTimer();
        }
    }

    public boolean isStopUpdateName(boolean isSimCardsLoaded) {
        Rlog.d(LOG_TAG, " isStopUpdateName: SimCardLoaded = " + isSimCardsLoaded + ", IS_DELAY_UPDATENAME = " + IS_DELAY_UPDATENAME);
        if (!IS_DELAY_UPDATENAME && !IS_DELAY_UPDATENAME_LAC_NULL) {
            return false;
        }
        if (!isSimCardsLoaded && IS_DELAY_UPDATENAME) {
            return DBG;
        }
        if (this.mGsmPhone == null || this.mGsmPhone.mSST == null || ((this.mGsmPhone.mSST.getCellLocationInfo() != null && ((GsmCellLocation) this.mGsmPhone.mSST.getCellLocationInfo()).getLac() != UNKNOWN_STATE) || !IS_DELAY_UPDATENAME_LAC_NULL)) {
            return false;
        }
        return DBG;
    }

    public boolean isUpdateLacAndCidCust(ServiceStateTracker sst) {
        Rlog.d(LOG_TAG, "isUpdateLacAndCidCust: Update Lac and Cid when cid is 0, ServiceStateTracker = " + sst + ",UPDATE_LAC_CID = " + IS_UPDATE_LAC_CID);
        return IS_UPDATE_LAC_CID;
    }

    public void setLTEUsageForRomaing(boolean isRoaming) {
        if (IS_LGU && !isRoaming) {
            HwTelephonyManagerInner inner = HwTelephonyManagerInner.getDefault();
            if (inner.getLteServiceAbility() != 1) {
                Rlog.i(LOG_TAG, " setLTEUsageForRomaing setLteServiceAbility... ");
                inner.setLteServiceAbility(1);
            }
        }
        if ("true".equals(Settings.System.getString(this.mGsmPhone.getContext().getContentResolver(), "hw_data_lte_roam_dlg"))) {
            setLTEStateForKT(isRoaming);
        }
        Rlog.d(LOG_TAG, "setLTEUsageForRomaing = " + isRoaming);
    }

    private void setLTEStateForKT(boolean isRoaming) {
        if (isRoaming && this.mGsmPhone != null) {
            int default4GSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            int isLTERoamingOn = UNKNOWN_STATE;
            if (default4GSlot >= 0) {
                if (default4GSlot == 0) {
                    isLTERoamingOn = Settings.System.getInt(this.mGsmPhone.getContext().getContentResolver(), SIM_ONE_LTE_ROAMING, 0);
                } else if (default4GSlot == 1) {
                    isLTERoamingOn = Settings.System.getInt(this.mGsmPhone.getContext().getContentResolver(), SIM_TWO_LTE_ROAMING, 0);
                }
                HwTelephonyManagerInner inner = HwTelephonyManagerInner.getDefault();
                if (inner != null && inner.getLteServiceAbility() != isLTERoamingOn) {
                    Rlog.i(LOG_TAG, " setLTEUsageForRomaing setLteServiceAbility... ");
                    inner.setLteServiceAbility(isLTERoamingOn);
                }
            }
        }
    }

    public boolean handleMessage(Message msg) {
        int i = msg.what;
        if (i == EVENT_GET_LTE_FREQ_WITH_WLAN_COEX) {
            Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX");
            handleGetLteFreqWithWlanCoex((AsyncResult) msg.obj);
            return DBG;
        } else if (i != EVENT_GET_CELL_INFO_LIST_OTDOA) {
            return false;
        } else {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.userObj instanceof AsyncResult) {
                Rlog.d(LOG_TAG, "EVENT_GET_CELL_INFO_LIST userObj is AsyncResult!");
                ar = (AsyncResult) ar.userObj;
            }
            if (!(ar.userObj instanceof CellInfoResult)) {
                Rlog.d(LOG_TAG, "EVENT_GET_CELL_INFO_LIST userObj:" + ar.userObj);
                return DBG;
            }
            CellInfoResult result = (CellInfoResult) ar.userObj;
            synchronized (result.lockObj) {
                if (ar.exception != null) {
                    Rlog.i(LOG_TAG, "EVENT_GET_CELL_INFO_LIST_OTDOA: error ret null, e=" + ar.exception);
                    result.list = null;
                } else {
                    result.list = (List) ar.result;
                }
                this.mLastEnhancedCellInfoListTime = SystemClock.elapsedRealtime();
                this.mLastEnhancedCellInfoList = result.list;
                result.lockObj.notify();
            }
            return DBG;
        }
    }

    private void handleGetLteFreqWithWlanCoex(AsyncResult ar) {
        if (ar != null && ar.exception == null && this.mGsmPhone != null) {
            int[] result = (int[]) ar.result;
            if (result == null) {
                Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX result is null");
                return;
            }
            int ulbw = result[2];
            int dlbw = result[4];
            if (!TextUtils.isEmpty(this.mUlbwDlbwString)) {
                String[] ulbwDlbw = this.mUlbwDlbwString.trim().split(";");
                Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX ulbw =" + ulbw + ", dlbw =" + dlbw + "mUlbwDlbwString =" + this.mUlbwDlbwString);
                boolean isHasCAState = false;
                if (ulbw >= Integer.parseInt(ulbwDlbw[0]) && dlbw >= Integer.parseInt(ulbwDlbw[1])) {
                    isHasCAState = true;
                }
                if (this.mIsCaState != isHasCAState) {
                    this.mIsLTEBandWidthChanged = DBG;
                    this.mIsCaState = isHasCAState;
                    Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX mIsCaState =" + this.mIsCaState);
                }
            }
        }
    }

    public void getLteFreqWithWlanCoex(CommandsInterface ci, ServiceStateTracker sst) {
        if (this.mContext == null || ci == null || sst == null) {
            Rlog.d(LOG_TAG, "getLteFreqWithWlanCoex error !");
            return;
        }
        try {
            this.mUlbwDlbwString = Settings.System.getString(this.mContext.getContentResolver(), "hw_query_lwclash");
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception get hw_query_lwclash value", e);
        }
        Rlog.d(LOG_TAG, "EVENT_GET_LTE_FREQ_WITH_WLAN_COEX mUlbwDlbwString =" + this.mUlbwDlbwString);
        if (!TextUtils.isEmpty(this.mUlbwDlbwString)) {
            ci.getLteFreqWithWlanCoex(sst.obtainMessage((int) EVENT_GET_LTE_FREQ_WITH_WLAN_COEX));
        }
    }

    private boolean isCustRejCodeKo(int rejcode) {
        if (this.mGsmPhone == null || this.mGsmPhone.getIccRecords() == null) {
            return false;
        }
        String mccmnc = this.mGsmPhone.getIccRecords().getOperatorNumeric();
        String rejPlmnsConfig = Settings.System.getString(this.mContext.getContentResolver(), "hw_rej_info_ko");
        Rlog.d(LOG_TAG, "mccmnc = " + mccmnc + " plmnsConfig = " + rejPlmnsConfig);
        if (TextUtils.isEmpty(rejPlmnsConfig) || TextUtils.isEmpty(mccmnc)) {
            return false;
        }
        for (String rejPlmn : rejPlmnsConfig.split(";")) {
            String[] rejectCodes = rejPlmn.split(":");
            if (rejectCodes.length != 2 || TextUtils.isEmpty(rejectCodes[0]) || TextUtils.isEmpty(rejectCodes[1])) {
                return false;
            }
            if (mccmnc.equals(rejectCodes[0]) && Arrays.asList(rejectCodes[1].split(",")).contains(String.valueOf(rejcode))) {
                return DBG;
            }
        }
        return false;
    }

    public void handleNetworkRejectionEx(int rejcode, int rejrat) {
        if (HW_ATT_SHOW_NET_REJ) {
            handleNetworkRejection(rejcode);
        }
        if (IS_KT) {
            handleKTNetworkRejectionEx(rejcode, rejrat);
        }
    }

    private void handleKTNetworkRejectionEx(int rejcode, int rejrat) {
        Rlog.d(LOG_TAG, "handleNetworkRejectionKo : rejcode :" + rejcode + "   rej rat==" + rejrat);
        if (!IS_KT || rejcode != NO_SUITABLE_CELLS_IN_LA) {
            Resources resources = Resources.getSystem();
            String msg = "";
            if (!isCustRejCodeKo(rejcode)) {
                msg = resources.getString(33685995);
            } else {
                if (rejcode != 2) {
                    if (rejcode == ILLEGAL_MS) {
                        msg = resources.getString(33685997);
                    } else if (rejcode != ILLEGAL_ME) {
                        if (rejcode != 8) {
                            if (rejcode != ESM_FAILURE && rejcode != CONGESTTION) {
                                switch (rejcode) {
                                    case NO_SUITABLE_CELLS_IN_LA /* 15 */:
                                        break;
                                    case MSC_TEMPORARILY_NOT_REACHABLE /* 16 */:
                                    case NETWORK_FAILURE /* 17 */:
                                        if (rejrat == 2 && IS_KT) {
                                            msg = resources.getString(33685995);
                                            break;
                                        } else {
                                            msg = resources.getString(33685999);
                                            break;
                                        }
                                    default:
                                        msg = resources.getString(33685995);
                                        break;
                                }
                            } else {
                                msg = resources.getString(33685999);
                            }
                        }
                    } else {
                        msg = resources.getString(33685998);
                    }
                }
                msg = resources.getString(33685996);
            }
            showDialog(msg, rejcode);
            this.oldRejCode = rejcode;
        }
    }

    public boolean isUpdateCAByCell(ServiceState newSS) {
        boolean updateCaByCell = DBG;
        if (newSS.getRilDataRadioTechnology() == GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN || newSS.getRilDataRadioTechnology() == ESM_FAILURE) {
            updateCaByCell = SystemProperties.getBoolean("ro.config.hw_updateCA_bycell", (boolean) DBG);
        }
        if (TextUtils.isEmpty(this.mUlbwDlbwString) || !this.mIsLTEBandWidthChanged) {
            return updateCaByCell;
        }
        this.mIsLTEBandWidthChanged = false;
        return false;
    }

    public void updateLTEBandWidth(ServiceState newSS) {
        if (!TextUtils.isEmpty(this.mUlbwDlbwString) && !newSS.isUsingCarrierAggregation()) {
            newSS.setIsUsingCarrierAggregation(this.mIsCaState);
        }
    }

    public void handleLteEmmCause(int phoneId, int rejrat, int originalrejectcause) {
        if (IS_VERIZON && rejrat == 2) {
            ContentResolver contentResolver = this.mGsmPhone.getContext().getContentResolver();
            Settings.Global.putInt(contentResolver, "LTE_Emm_Cause_" + phoneId, originalrejectcause);
            setLteEmmCauseRecorded(phoneId, DBG);
        }
    }

    public void clearLteEmmCause(int phoneId, ServiceState state) {
        if (IS_VERIZON && isLteEmmCauseRecorded(phoneId)) {
            if ((state.getRilVoiceRadioTechnology() == GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN && state.getVoiceRegState() == 0) || (state.getRilDataRadioTechnology() == GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN && state.getDataRegState() == 0)) {
                ContentResolver contentResolver = this.mGsmPhone.getContext().getContentResolver();
                Settings.Global.putInt(contentResolver, "LTE_Emm_Cause_" + phoneId, 0);
                setLteEmmCauseRecorded(phoneId, false);
            }
        }
    }

    private boolean isLteEmmCauseRecorded(int phoneId) {
        boolean[] zArr = this.mlteEmmCauseRecords;
        if (phoneId < zArr.length) {
            return zArr[phoneId];
        }
        return false;
    }

    private void setLteEmmCauseRecorded(int phoneId, boolean isRecorded) {
        boolean[] zArr = this.mlteEmmCauseRecords;
        if (phoneId < zArr.length) {
            zArr[phoneId] = isRecorded;
        }
    }

    public void clearPcoValue(GsmCdmaPhone phone) {
        if (HW_SIM_ACTIVATION) {
            Settings.Global.putString(phone.getContext().getContentResolver(), PCO_DATA, DEFAULT_PCO_DATA);
            Rlog.d(LOG_TAG, "Airplane mode on , clear pco data.");
        }
    }

    public boolean isCsPopShow(int notifyType) {
        if (!SystemProperties.getBoolean("ro.config.hw_CSResDialog", false) || (notifyType != CS_ENABLED && notifyType != CS_NORMAL_ENABLED)) {
            return false;
        }
        return DBG;
    }

    private List<CellInfo> getAllEnhancedCellInfoData(WorkSource workSource) {
        CellInfoResult result = new CellInfoResult();
        Rlog.i(LOG_TAG, "SST.getAllEnhancedCellInfo(): E");
        if (this.mGsmPhone.mCi.getRilVersion() < 8) {
            Rlog.i(LOG_TAG, "SST.getAllEnhancedCellInfo(): not implemented");
            result.list = null;
        } else if (Thread.currentThread() == this.mGsmPhone.getServiceStateTracker().getLooper().getThread()) {
            Rlog.i(LOG_TAG, "SST.getAllEnhancedCellInfo(): return last, same thread can't block");
            result.list = this.mLastEnhancedCellInfoList;
        } else if (SystemClock.elapsedRealtime() - this.mLastEnhancedCellInfoListTime > LAST_CELL_INFO_LIST_MAX_AGE_MS) {
            Message msg = this.mGsmPhone.getServiceStateTracker().obtainMessage((int) EVENT_GET_CELL_INFO_LIST_OTDOA, result);
            synchronized (result.lockObj) {
                result.list = null;
                this.mGsmPhone.mCi.getEnhancedCellInfoList(msg, workSource);
                try {
                    result.lockObj.wait(5000);
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "InterruptedException");
                }
            }
        } else {
            Rlog.i(LOG_TAG, "SST.getAllEnhancedCellInfo(): return last, back to back calls");
            result.list = this.mLastEnhancedCellInfoList;
        }
        synchronized (result.lockObj) {
            if (result.list != null) {
                Rlog.i(LOG_TAG, "SST.getAllEnhancedCellInfo(): X size=" + result.list.size() + " list=" + result.list);
                return result.list;
            }
            Rlog.i(LOG_TAG, "SST.getAllEnhancedCellInfo(): X size=0 list=null");
            return null;
        }
    }

    public List<CellInfo> getAllEnhancedCellInfo(WorkSource workSource) {
        List<CellInfo> cellInfoList = getAllEnhancedCellInfoData(workSource);
        if (cellInfoList == null) {
            return null;
        }
        if (Settings.Secure.getInt(this.mGsmPhone.getContext().getContentResolver(), "location_mode", 0) != 0) {
            return cellInfoList;
        }
        ArrayList<CellInfo> privateCellInfoList = new ArrayList<>(cellInfoList.size());
        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo instanceof CellInfoCdma) {
                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
                CellIdentityCdma maskedCellIdentity = new CellIdentityCdma(cellIdentity.getNetworkId(), cellIdentity.getSystemId(), cellIdentity.getBasestationId(), Integer.MAX_VALUE, Integer.MAX_VALUE, null, null);
                CellInfoCdma privateCellInfoCdma = new CellInfoCdma(cellInfoCdma);
                privateCellInfoCdma.setCellIdentity(maskedCellIdentity);
                privateCellInfoList.add(privateCellInfoCdma);
            } else {
                privateCellInfoList.add(cellInfo);
            }
        }
        return privateCellInfoList;
    }

    /* access modifiers changed from: private */
    public class CellInfoResult {
        List<CellInfo> list;
        Object lockObj;

        private CellInfoResult() {
            this.lockObj = new Object();
        }
    }

    public void tryClearRejCause(ServiceState newSs, boolean hasRilRadioTechnologyChanged, ServiceStateTracker sst) {
        if (((!ServiceState.isLte(newSs.getRilDataRadioTechnology()) || newSs.getDataRegState() != 0) ? false : DBG) && hasRilRadioTechnologyChanged) {
            sst.clearRejCause();
            Rlog.i(LOG_TAG, "RilRadioTechnologyChanged clearRejCause");
        }
    }
}
