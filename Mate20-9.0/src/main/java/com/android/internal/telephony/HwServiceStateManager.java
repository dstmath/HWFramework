package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.icu.util.TimeZone;
import android.os.AsyncResult;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.NetLocationStrategy;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.HwLog;
import com.android.internal.telephony.cdma.HwCdmaServiceStateManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.gsm.HwGsmServiceStateManager;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.util.TimeZoneFinder;

public class HwServiceStateManager extends Handler {
    private static final String ACTION_TIMEZONE_SELECTION = "com.huawei.intent.action.ACTION_TIMEZONE_SELECTION";
    protected static final int CT_NUM_MATCH_HOME = 11;
    protected static final int CT_NUM_MATCH_ROAMING = 10;
    protected static final int CT_SID_1st_END = 14335;
    protected static final int CT_SID_1st_START = 13568;
    protected static final int CT_SID_2nd_END = 26111;
    protected static final int CT_SID_2nd_START = 25600;
    protected static final int DEFAULT_SID = 0;
    protected static final int DELAYED_ECC_TO_NOSERVICE_VALUE = SystemProperties.getInt("ro.ecc_to_noservice.timer", 0);
    private static final int DELAY_TIME_TO_NOTIF_NITZ = 60000;
    private static final String DUAL_CARDS_SETTINGS_ACTIVITY = "com.huawei.settings.intent.DUAL_CARD_SETTINGS";
    private static final int DUAL_SIM = 2;
    protected static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 0;
    private static final int EVENT_GET_SIGNAL_STRENGTH = 159;
    protected static final int EVENT_ICC_RECORDS_EONS_UPDATED = 1;
    private static final int EVENT_NETWORK_REJINFO_DONE = 156;
    private static final int EVENT_NETWORK_REJINFO_TIMEOUT = 157;
    private static final int EVENT_NITZ_CAPABILITY_NOTIFICATION = 160;
    protected static final int EVENT_RESUME_DATA = 203;
    protected static final int EVENT_SET_PRE_NETWORKTYPE = 202;
    private static final int EVENT_SIM_HOTPLUG = 158;
    private static final String EXTRA_SHOW_WIFI = "showWifi";
    private static final String EXTRA_SST_IN_SERVICE = "isSstInService";
    private static final String EXTRA_WIFI = "wifi";
    private static final boolean FEATURE_RECOVER_AUTO_NETWORK_MODE = SystemProperties.getBoolean("ro.hwpp.recover_auto_mode", false);
    private static final int INTERVAL_TIME = 120000;
    protected static final String INVAILD_PLMN = "1023127-123456-1023456-123127-";
    private static final int INVALID = -1;
    protected static final boolean IS_CHINATELECOM = SystemProperties.get("ro.config.hw_opta", "0").equals("92");
    protected static final boolean IS_MULTI_SIM_ENABLED = TelephonyManager.getDefault().isMultiSimEnabled();
    private static final boolean KEEP_NW_SEL_MANUAL = SystemProperties.getBoolean("ro.config.hw_keep_sel_manual", false);
    private static final String KEY_WFC_FORMAT_WIFI_STRING = "wfc_format_wifi_string";
    private static final String KEY_WFC_HIDE_WIFI_BOOL = "wfc_hide_wifi_bool";
    private static final String KEY_WFC_IS_SHOW_AIRPLANE = "wfc_is_show_air_plane";
    private static final String KEY_WFC_IS_SHOW_EMERGENCY_ONLY = "wfc_is_show_emergency_only";
    private static final String KEY_WFC_IS_SHOW_NO_SERVICE = "wfc_is_show_no_service";
    private static final String KEY_WFC_SPN_STRING = "wfc_spn_string";
    private static final int MAX_TOP_PACKAGE = 10;
    protected static final long MODE_REQUEST_CELL_LIST_STRATEGY_INVALID = -1;
    protected static final long MODE_REQUEST_CELL_LIST_STRATEGY_VALID = 0;
    private static final int[] NETWORK_REJINFO_CAUSES = {7, 11, 12, 13, 14, 15, 17};
    private static final int NETWORK_REJINFO_MAX_COUNT = 3;
    private static final int NETWORK_REJINFO_MAX_TIME = 1200000;
    private static final String NETWORK_REJINFO_NOTIFY_CHANNEL = "network_rejinfo_notify_channel";
    protected static final String NR_TECHNOLOGY_CONFIGA = "ConfigA";
    protected static final String NR_TECHNOLOGY_CONFIGB = "ConfigB";
    protected static final String NR_TECHNOLOGY_CONFIGC = "ConfigC";
    protected static final String NR_TECHNOLOGY_CONFIGD = "ConfigD";
    protected static final int NSA_STATE2 = 2;
    protected static final int NSA_STATE3 = 3;
    protected static final int NSA_STATE4 = 4;
    protected static final int NSA_STATE5 = 5;
    private static final int REJINFO_LEN = 4;
    protected static final int RESUME_DATA_TIME = 8000;
    protected static final int SET_PRE_NETWORK_TIME = 5000;
    protected static final int SET_PRE_NETWORK_TIME_DELAY = 2000;
    protected static final int SPN_RULE_SHOW_BOTH = 3;
    protected static final int SPN_RULE_SHOW_NITZNAME_PRIOR = 6;
    protected static final int SPN_RULE_SHOW_PLMN_ONLY = 2;
    protected static final int SPN_RULE_SHOW_PNN_PRIOR = 4;
    protected static final int SPN_RULE_SHOW_SIM_ONLY = 0;
    protected static final int SPN_RULE_SHOW_SPN_ONLY = 1;
    protected static final int SPN_RULE_SHOW_SPN_PRIOR = 5;
    private static final String TAG = "HwServiceStateManager";
    protected static final long VALUE_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    protected static final int VALUE_SCREEN_OFF_TIME_DEFAULT = 10;
    private static final int WIFI_IDX = 1;
    private static Map<Object, HwCdmaServiceStateManager> cdmaServiceStateManagers = new HashMap();
    private static String data = null;
    private static Map<Object, HwGsmServiceStateManager> gsmServiceStateManagers = new HashMap();
    private static final boolean isScreenOffNotUpdateLocation = SystemProperties.getBoolean("ro.config.updatelocation", false);
    private static Map<Object, HwServiceStateManager> serviceStateManagers = new HashMap();
    private static final boolean voice_reg_state_for_ons = "true".equals(SystemProperties.get("ro.hwpp.voice_reg_state_for_ons", "false"));
    protected int DELAYED_TIME_DEFAULT_VALUE = SystemProperties.getInt("ro.lostnetwork.default_timer", 20);
    protected int DELAYED_TIME_NETWORKSTATUS_CS_2G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs2G", this.DELAYED_TIME_DEFAULT_VALUE) * 1000);
    protected int DELAYED_TIME_NETWORKSTATUS_CS_3G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs3G", this.DELAYED_TIME_DEFAULT_VALUE) * 1000);
    protected int DELAYED_TIME_NETWORKSTATUS_CS_4G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_cs4G", this.DELAYED_TIME_DEFAULT_VALUE) * 1000);
    protected int DELAYED_TIME_NETWORKSTATUS_PS_2G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps2G", this.DELAYED_TIME_DEFAULT_VALUE) * 1000);
    protected int DELAYED_TIME_NETWORKSTATUS_PS_3G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps3G", this.DELAYED_TIME_DEFAULT_VALUE) * 1000);
    protected int DELAYED_TIME_NETWORKSTATUS_PS_4G = (SystemProperties.getInt("ro.lostnetwork.delaytimer_ps4G", this.DELAYED_TIME_DEFAULT_VALUE) * 1000);
    private Map<String, Integer> mCellInfoMap = new HashMap();
    /* access modifiers changed from: private */
    public Context mContext;
    protected boolean mCurShowWifi = false;
    protected String mCurWifi = "";
    protected int mMainSlot;
    protected int mMainSlotEcc;
    private boolean mNeedHandleRejInfoFlag = true;
    protected int mPendingPreNwType = 0;
    protected Message mPendingsavemessage;
    /* access modifiers changed from: private */
    public Phone mPhoneBase;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int phoneId = -1;
            if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("subscription", -1);
                ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
                if (slotId == HwServiceStateManager.this.mPhoneBase.getPhoneId() && serviceState.getState() == 0) {
                    HwServiceStateManager.this.clearNetworkRejInfo();
                    NotificationManager mNotificationManager = (NotificationManager) HwServiceStateManager.this.mContext.getSystemService("notification");
                    if (mNotificationManager != null) {
                        mNotificationManager.cancel(HwServiceStateManager.TAG, slotId);
                    }
                }
            } else if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                if (HwServiceStateManager.this.hasMessages(0)) {
                    HwServiceStateManager.this.removeMessages(0);
                }
                int phoneKey = intent.getIntExtra("phone", -1);
                if (HwServiceStateManager.this.mPhoneBase != null) {
                    phoneId = HwServiceStateManager.this.mPhoneBase.getPhoneId();
                }
                if (HwServiceStateManager.this.mPhoneBase != null && phoneKey == phoneId) {
                    HwServiceStateManager hwServiceStateManager = HwServiceStateManager.this;
                    hwServiceStateManager.logd("ACTION_CARRIER_CONFIG_CHANGED phoneId: " + phoneId);
                    HwServiceStateManager.this.mPhoneBase.mCi.getSignalStrength(HwServiceStateManager.this.obtainMessage(HwServiceStateManager.EVENT_GET_SIGNAL_STRENGTH));
                }
            }
        }
    };
    protected boolean mRefreshState = false;
    protected boolean mRefreshStateEcc = false;
    private ServiceStateTracker mServiceStateTracker;
    protected boolean mSetPreNwTypeRequested = false;
    private long mStartTime = MODE_REQUEST_CELL_LIST_STRATEGY_VALID;
    private ArrayList<String> networkRejInfoList = new ArrayList<>();

    private enum HotplugState {
        STATE_PLUG_OUT,
        STATE_PLUG_IN
    }

    protected HwServiceStateManager(Phone phoneBase) {
        super(Looper.getMainLooper());
        this.mPhoneBase = phoneBase;
        this.mContext = phoneBase.getContext();
        initNetworkRejInfo();
        registerCarrierConfig(this.mContext);
        loadAllowUpdateLocationPackage(this.mContext);
    }

    public String getPlmn() {
        return "";
    }

    public void sendDualSimUpdateSpnIntent(boolean showSpn, String spn, boolean showPlmn, String plmn) {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            Intent intent = null;
            int phoneId = this.mPhoneBase.getPhoneId();
            boolean z = true;
            if (phoneId == 0) {
                intent = new Intent("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
            } else if (1 == phoneId) {
                intent = new Intent("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
            } else {
                Rlog.e(TAG, "unsupport SUB ID :" + phoneId);
            }
            boolean isSstInService = false;
            if (!(this.mServiceStateTracker == null || this.mServiceStateTracker.mSS == null)) {
                int dataRegState = this.mServiceStateTracker.mSS.getDataRegState();
                int voiceRegState = this.mServiceStateTracker.mSS.getVoiceRegState();
                if (!(dataRegState == 0 || voiceRegState == 0)) {
                    z = false;
                }
                isSstInService = z;
                logd("dataRegState=" + dataRegState + ", voiceRegState=" + voiceRegState + ", isSstInService=" + isSstInService);
            }
            if (intent != null) {
                intent.addFlags(536870912);
                intent.putExtra("showSpn", showSpn);
                intent.putExtra("spn", spn);
                intent.putExtra("showPlmn", showPlmn);
                intent.putExtra("plmn", plmn);
                intent.putExtra("subscription", phoneId);
                intent.putExtra(EXTRA_SHOW_WIFI, this.mCurShowWifi);
                intent.putExtra(EXTRA_WIFI, this.mCurWifi);
                intent.putExtra(EXTRA_SST_IN_SERVICE, isSstInService);
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                Rlog.d(TAG, "Send updateSpnIntent for SUB :" + phoneId);
            }
        }
    }

    public OnsDisplayParams getOnsDisplayParamsHw(boolean showSpn, boolean showPlmn, int rule, String plmn, String spn) {
        OnsDisplayParams onsDisplayParams = new OnsDisplayParams(showSpn, showPlmn, rule, plmn, spn);
        return onsDisplayParams;
    }

    public HwServiceStateManager(ServiceStateTracker serviceStateTracker, Phone phoneBase) {
        super(Looper.getMainLooper());
        this.mPhoneBase = phoneBase;
        this.mServiceStateTracker = serviceStateTracker;
        this.mContext = phoneBase.getContext();
        initNetworkRejInfo();
        registerCarrierConfig(this.mContext);
        loadAllowUpdateLocationPackage(this.mContext);
    }

    public static synchronized HwServiceStateManager getHwServiceStateManager(ServiceStateTracker serviceStateTracker, Phone phoneBase) {
        HwServiceStateManager hwServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwServiceStateManager = serviceStateManagers.get(serviceStateTracker);
            if (hwServiceStateManager == null) {
                hwServiceStateManager = new HwServiceStateManager(serviceStateTracker, phoneBase);
                serviceStateManagers.put(serviceStateTracker, hwServiceStateManager);
            }
        }
        return hwServiceStateManager;
    }

    public static synchronized HwGsmServiceStateManager getHwGsmServiceStateManager(ServiceStateTracker serviceStateTracker, GsmCdmaPhone phone) {
        HwGsmServiceStateManager hwGsmServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwGsmServiceStateManager = gsmServiceStateManagers.get(serviceStateTracker);
            if (hwGsmServiceStateManager == null) {
                hwGsmServiceStateManager = new HwGsmServiceStateManager(serviceStateTracker, phone);
                gsmServiceStateManagers.put(serviceStateTracker, hwGsmServiceStateManager);
            }
        }
        return hwGsmServiceStateManager;
    }

    public static synchronized HwCdmaServiceStateManager getHwCdmaServiceStateManager(ServiceStateTracker serviceStateTracker, GsmCdmaPhone phone) {
        HwCdmaServiceStateManager hwCdmaServiceStateManager;
        synchronized (HwServiceStateManager.class) {
            hwCdmaServiceStateManager = cdmaServiceStateManagers.get(serviceStateTracker);
            if (hwCdmaServiceStateManager == null) {
                hwCdmaServiceStateManager = new HwCdmaServiceStateManager(serviceStateTracker, phone);
                cdmaServiceStateManagers.put(serviceStateTracker, hwCdmaServiceStateManager);
            }
        }
        return hwCdmaServiceStateManager;
    }

    public static synchronized void dispose(ServiceStateTracker serviceStateTracker) {
        synchronized (HwServiceStateManager.class) {
            if (serviceStateTracker != null) {
                HwGsmServiceStateManager hwGsmServiceStateManager = gsmServiceStateManagers.get(serviceStateTracker);
                if (hwGsmServiceStateManager != null) {
                    hwGsmServiceStateManager.dispose();
                }
                gsmServiceStateManagers.put(serviceStateTracker, null);
                HwCdmaServiceStateManager hwCdmaServiceStateManager = cdmaServiceStateManagers.get(serviceStateTracker);
                if (hwCdmaServiceStateManager != null) {
                    hwCdmaServiceStateManager.dispose();
                }
                cdmaServiceStateManagers.put(serviceStateTracker, null);
            }
        }
    }

    public int getCombinedRegState(ServiceState serviceState) {
        if (serviceState == null) {
            return 1;
        }
        int regState = serviceState.getVoiceRegState();
        int dataRegState = serviceState.getDataRegState();
        if (voice_reg_state_for_ons && serviceState.getRilDataRadioTechnology() != 14 && serviceState.getRilDataRadioTechnology() != 19) {
            return regState;
        }
        if (regState == 1 && dataRegState == 0) {
            Rlog.d(TAG, "getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
            regState = dataRegState;
        }
        return regState;
    }

    public void processCTNumMatch(boolean roaming, UiccCardApplication uiccCardApplication) {
    }

    /* access modifiers changed from: protected */
    public void checkMultiSimNumMatch() {
        int[] matchArray = {SystemProperties.getInt("gsm.hw.matchnum0", -1), SystemProperties.getInt("gsm.hw.matchnum.short0", -1), SystemProperties.getInt("gsm.hw.matchnum1", -1), SystemProperties.getInt("gsm.hw.matchnum.short1", -1)};
        Arrays.sort(matchArray);
        int numMatch = matchArray[3];
        int numMatchShort = numMatch;
        for (int i = 2; i >= 0; i--) {
            if (matchArray[i] < numMatch && matchArray[i] > 0) {
                numMatchShort = matchArray[i];
            }
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(numMatch));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(numMatchShort));
        Rlog.d(TAG, "checkMultiSimNumMatch: after setprop numMatch = " + SystemProperties.getInt("gsm.hw.matchnum", 0) + ", numMatchShort = " + SystemProperties.getInt("gsm.hw.matchnum.short", 0));
    }

    /* access modifiers changed from: protected */
    public void setCTNumMatchHomeForSlot(int slotId) {
        if (IS_MULTI_SIM_ENABLED) {
            SystemProperties.set("gsm.hw.matchnum" + slotId, Integer.toString(11));
            SystemProperties.set("gsm.hw.matchnum.short" + slotId, Integer.toString(11));
            checkMultiSimNumMatch();
            return;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(11));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(11));
    }

    /* access modifiers changed from: protected */
    public void setCTNumMatchRoamingForSlot(int slotId) {
        if (IS_MULTI_SIM_ENABLED) {
            SystemProperties.set("gsm.hw.matchnum" + slotId, Integer.toString(10));
            SystemProperties.set("gsm.hw.matchnum.short" + slotId, Integer.toString(10));
            checkMultiSimNumMatch();
            return;
        }
        SystemProperties.set("gsm.hw.matchnum", Integer.toString(10));
        SystemProperties.set("gsm.hw.matchnum.short", Integer.toString(10));
    }

    public static boolean isCustScreenOff(GsmCdmaPhone phoneBase) {
        if (!(!isScreenOffNotUpdateLocation || phoneBase == null || phoneBase.getContext() == null)) {
            PowerManager powerManager = (PowerManager) phoneBase.getContext().getSystemService("power");
            if (powerManager != null && !powerManager.isScreenOn()) {
                Rlog.d(TAG, " ScreenOff do nothing");
                return true;
            }
        }
        return false;
    }

    public void setOOSFlag(boolean flag) {
    }

    private void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        this.mPhoneBase.mCi.setPreferredNetworkType(networkType, response);
    }

    public void setPreferredNetworkTypeSafely(Phone phoneBase, int networkType, Message response) {
        this.mPhoneBase = phoneBase;
        DcTracker dcTracker = this.mPhoneBase.mDcTracker;
        if (this.mServiceStateTracker == null) {
            Rlog.d(TAG, "mServiceStateTracker is null, it is unexpected!");
        }
        if (networkType != 10) {
            if (this.mSetPreNwTypeRequested) {
                removeMessages(202);
                Rlog.d(TAG, "cancel setPreferredNetworkType");
            }
            this.mSetPreNwTypeRequested = false;
            Rlog.d(TAG, "PreNetworkType is not LTE, setPreferredNetworkType now!");
            setPreferredNetworkType(networkType, this.mPhoneBase.getPhoneId(), response);
        } else if (this.mSetPreNwTypeRequested) {
        } else {
            if (dcTracker.isDisconnected()) {
                setPreferredNetworkType(networkType, this.mPhoneBase.getPhoneId(), response);
                Rlog.d(TAG, "data is Disconnected, setPreferredNetworkType now!");
                return;
            }
            dcTracker.setInternalDataEnabled(false);
            Rlog.d(TAG, "Data is disabled and wait up to 8s to resume data.");
            sendMessageDelayed(obtainMessage(203), 8000);
            this.mPendingsavemessage = response;
            this.mPendingPreNwType = networkType;
            Message msg = Message.obtain(this);
            msg.what = 202;
            msg.arg1 = networkType;
            msg.obj = response;
            Rlog.d(TAG, "Wait up to 5s for data disconnect to setPreferredNetworkType.");
            sendMessageDelayed(msg, HwVSimConstants.WAIT_FOR_SIM_STATUS_CHANGED_UNSOL_TIMEOUT);
            this.mSetPreNwTypeRequested = true;
        }
    }

    public void checkAndSetNetworkType() {
        if (this.mSetPreNwTypeRequested) {
            Rlog.d(TAG, "mSetPreNwTypeRequested is true and wait a few seconds to setPreferredNetworkType");
            removeMessages(202);
            Message msg = Message.obtain(this);
            msg.what = 202;
            msg.arg1 = this.mPendingPreNwType;
            msg.obj = this.mPendingsavemessage;
            sendMessageDelayed(msg, 2000);
            return;
        }
        Rlog.d(TAG, "No need to setPreferredNetworkType");
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        switch (i) {
            case 156:
                logd("EVENT_NETWORK_REJINFO_DONE");
                onNetworkRejInfoDone(msg);
                return;
            case EVENT_NETWORK_REJINFO_TIMEOUT /*157*/:
                logd("EVENT_NETWORK_REJINFO_TIMEOUT");
                onNetworkRejInfoTimeout(msg);
                return;
            case EVENT_SIM_HOTPLUG /*158*/:
                logd("EVENT_SIM_HOTPLUG");
                onSimHotPlug(msg);
                return;
            case EVENT_GET_SIGNAL_STRENGTH /*159*/:
                this.mServiceStateTracker.onSignalStrengthResult((AsyncResult) msg.obj);
                return;
            case EVENT_NITZ_CAPABILITY_NOTIFICATION /*160*/:
                sendTimeZoneSelectionNotification();
                return;
            default:
                switch (i) {
                    case 202:
                        if (this.mSetPreNwTypeRequested) {
                            Rlog.d(TAG, "EVENT_SET_PRE_NETWORKTYPE, setPreferredNetworkType now.");
                            setPreferredNetworkType(msg.arg1, this.mPhoneBase.getPhoneId(), (Message) msg.obj);
                            this.mSetPreNwTypeRequested = false;
                            return;
                        }
                        Rlog.d(TAG, "No need to setPreferredNetworkType");
                        return;
                    case 203:
                        this.mPhoneBase.mDcTracker.setInternalDataEnabled(true);
                        Rlog.d(TAG, "EVENT_RESUME_DATA, resume data now.");
                        return;
                    default:
                        Rlog.d(TAG, "Unhandled message with number: " + msg.what);
                        return;
                }
        }
    }

    public boolean isCardInvalid(boolean isSubDeactivated, int subId) {
        IccCardStatus.CardState newState = IccCardStatus.CardState.CARDSTATE_ABSENT;
        UiccCard newCard = UiccController.getInstance().getUiccCard(subId);
        if (newCard != null) {
            newState = newCard.getCardState();
        }
        boolean isCardPresent = IccCardStatusUtils.isCardPresent(newState);
        Rlog.d(TAG, "isCardPresent : " + isCardPresent + "  subId : " + subId);
        return !isCardPresent || isSubDeactivated;
    }

    /* access modifiers changed from: protected */
    public OnsDisplayParams getOnsDisplayParamsForVoWifi(OnsDisplayParams ons) {
        String combineWifi;
        String combineWifi2;
        String combineWifi3;
        String combineWifi4;
        OnsDisplayParams onsDisplayParams = ons;
        int voiceIdx = 0;
        String spnConfiged = "";
        boolean hideWifi = false;
        String wifiConfiged = "";
        boolean isShowNoService = false;
        boolean isShowEmergency = false;
        boolean isShowAirplane = false;
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader != null) {
            try {
                PersistableBundle b = configLoader.getConfigForSubId(this.mPhoneBase.getSubId());
                if (b != null) {
                    voiceIdx = b.getInt("wfc_spn_format_idx_int");
                    spnConfiged = b.getString(KEY_WFC_SPN_STRING);
                    hideWifi = b.getBoolean(KEY_WFC_HIDE_WIFI_BOOL);
                    wifiConfiged = b.getString(KEY_WFC_FORMAT_WIFI_STRING);
                    isShowNoService = b.getBoolean(KEY_WFC_IS_SHOW_NO_SERVICE);
                    isShowEmergency = b.getBoolean(KEY_WFC_IS_SHOW_EMERGENCY_ONLY);
                    isShowAirplane = b.getBoolean(KEY_WFC_IS_SHOW_AIRPLANE);
                }
            } catch (Exception e) {
                Rlog.e(TAG, "getGsmOnsDisplayParams: carrier config error: " + e);
            }
        }
        Rlog.d(TAG, "updateSpnDisplay, voiceIdx = " + voiceIdx + " spnConfiged = " + spnConfiged + " hideWifi = " + hideWifi + " wifiConfiged = " + wifiConfiged + " isShowNoService = " + isShowNoService + " isShowEmergency = " + isShowEmergency + " isShowAirplane = " + isShowAirplane);
        String formatWifi = "%s";
        if (!hideWifi) {
            boolean useGoogleWifiFormat = voiceIdx == 1;
            String[] wfcSpnFormats = this.mContext.getResources().getStringArray(17236092);
            if (!TextUtils.isEmpty(wifiConfiged)) {
                formatWifi = wifiConfiged;
            } else if (!useGoogleWifiFormat || wfcSpnFormats == null) {
                formatWifi = this.mContext.getResources().getString(17041235);
            } else {
                formatWifi = wfcSpnFormats[1];
            }
        }
        String formatWifi2 = formatWifi;
        boolean inService = getCombinedRegState(this.mServiceStateTracker.mSS) == 0;
        boolean noService = false;
        boolean emergencyOnly = false;
        String combineWifi5 = "";
        int i = voiceIdx;
        boolean airplaneMode = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        int combinedRegState = getCombinedRegState(this.mServiceStateTracker.mSS);
        if (combinedRegState == 1 || combinedRegState == 2) {
            if (this.mServiceStateTracker.mSS == null || !this.mServiceStateTracker.mSS.isEmergencyOnly()) {
                noService = true;
            } else {
                emergencyOnly = true;
            }
        }
        if (!TextUtils.isEmpty(spnConfiged)) {
            combineWifi = spnConfiged;
        } else if (!TextUtils.isEmpty(onsDisplayParams.mSpn)) {
            combineWifi = onsDisplayParams.mSpn;
        } else if (!inService || TextUtils.isEmpty(onsDisplayParams.mPlmn)) {
            combineWifi = combineWifi5;
        } else {
            combineWifi = onsDisplayParams.mPlmn;
        }
        if (!airplaneMode || !isShowAirplane) {
            combineWifi2 = combineWifi;
            if (!noService || !isShowNoService) {
                if (emergencyOnly && isShowEmergency) {
                    combineWifi4 = Resources.getSystem().getText(17039987).toString();
                }
                combineWifi3 = String.format(formatWifi2, new Object[]{combineWifi2});
                try {
                    onsDisplayParams.mWifi = combineWifi3.trim();
                    onsDisplayParams.mShowWifi = true;
                    boolean z = airplaneMode;
                    String str = spnConfiged;
                } catch (RuntimeException e2) {
                    e = e2;
                    boolean z2 = airplaneMode;
                    StringBuilder sb = new StringBuilder();
                    String str2 = spnConfiged;
                    sb.append("combine wifi fail, ");
                    sb.append(e);
                    Rlog.e(TAG, sb.toString());
                    String str3 = combineWifi3;
                    return onsDisplayParams;
                }
                return onsDisplayParams;
            }
            combineWifi4 = Resources.getSystem().getText(17040350).toString();
        } else {
            String str4 = combineWifi;
            combineWifi4 = Resources.getSystem().getText(17040141).toString();
        }
        combineWifi2 = combineWifi4;
        try {
            combineWifi3 = String.format(formatWifi2, new Object[]{combineWifi2});
            onsDisplayParams.mWifi = combineWifi3.trim();
            onsDisplayParams.mShowWifi = true;
            boolean z3 = airplaneMode;
            String str5 = spnConfiged;
        } catch (RuntimeException e3) {
            e = e3;
            combineWifi3 = combineWifi2;
            boolean z22 = airplaneMode;
            StringBuilder sb2 = new StringBuilder();
            String str22 = spnConfiged;
            sb2.append("combine wifi fail, ");
            sb2.append(e);
            Rlog.e(TAG, sb2.toString());
            String str32 = combineWifi3;
            return onsDisplayParams;
        }
        return onsDisplayParams;
    }

    public void countPackageUseCellInfo(String packageName) {
        if (this.mPhoneBase != null) {
            boolean isMainSub = this.mPhoneBase.getSubId() == HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            if (isMainSub || this.mCellInfoMap.size() != 0) {
                Rlog.d(TAG, "countPackageUseCellInfo packageName is :" + packageName);
                if (TextUtils.isEmpty(packageName)) {
                    Rlog.d(TAG, "countPackageUseCellInfo packageName is null");
                    return;
                }
                if (this.mStartTime == MODE_REQUEST_CELL_LIST_STRATEGY_VALID) {
                    this.mStartTime = SystemClock.elapsedRealtime();
                }
                if (Math.abs(SystemClock.elapsedRealtime() - this.mStartTime) >= 120000) {
                    String topPackageString = "";
                    int topPackageNum = 0;
                    int count = this.mCellInfoMap.size();
                    Rlog.d(TAG, "countPackageUseCellInfo size:" + count);
                    for (Map.Entry<String, Integer> entry : this.mCellInfoMap.entrySet()) {
                        topPackageString = topPackageString + " name=" + entry.getKey() + " num=" + entry.getValue();
                        topPackageNum++;
                        if (10 == topPackageNum) {
                            HwLog.dubaie("DUBAI_TAG_LOCATION_COUNTER", "count=10" + topPackageString);
                            Rlog.d(TAG, "countPackageUseCellInfo topPackageString:count=10" + topPackageString);
                            topPackageNum = 0;
                            topPackageString = "";
                            count += -10;
                        } else if (count / 10 == 0) {
                            count--;
                            if (count == 0) {
                                HwLog.dubaie("DUBAI_TAG_LOCATION_COUNTER", "count=" + topPackageNum + topPackageString);
                                Rlog.d(TAG, "countPackageUseCellInfo topPackageString:count=" + topPackageNum + topPackageString);
                            }
                        }
                    }
                    this.mStartTime = SystemClock.elapsedRealtime();
                    this.mCellInfoMap.clear();
                }
                boolean isCharging = false;
                BatteryManager batteryManager = (BatteryManager) this.mPhoneBase.getContext().getSystemService("batterymanager");
                if (batteryManager != null) {
                    isCharging = batteryManager.isCharging();
                }
                if (isMainSub && isCustScreenOff(this.mPhoneBase) && !isCharging) {
                    if (this.mCellInfoMap.containsKey(packageName)) {
                        this.mCellInfoMap.put(packageName, Integer.valueOf(this.mCellInfoMap.get(packageName).intValue() + 1));
                    } else {
                        this.mCellInfoMap.put(packageName, 1);
                    }
                }
            }
        }
    }

    private boolean isCellAgeTimePassed(ServiceStateTracker stateTracker, GsmCdmaPhone phoneBase) {
        long curSysTime = SystemClock.elapsedRealtime();
        long lastRequestTime = stateTracker.getLastCellInfoListTime();
        boolean isScreenOff = isCustScreenOff(phoneBase);
        int screenOffTimes = SystemProperties.getInt("ro.config.screen_off_times", 10);
        long cellInfoListMaxAgeTime = 2000;
        if (isScreenOff) {
            cellInfoListMaxAgeTime = 2000 * ((long) screenOffTimes);
        }
        Rlog.d(TAG, "isCellAgeTimePassed(): isScreenOff=" + isScreenOff + " cellInfoListMaxAgeTime=" + cellInfoListMaxAgeTime + "ms.");
        if (curSysTime - lastRequestTime > cellInfoListMaxAgeTime) {
            Rlog.d(TAG, "isCellAgeTimePassed():return true.");
            return true;
        }
        Rlog.d(TAG, "isCellAgeTimePassed():return false,because already requested CellInfoList within " + cellInfoListMaxAgeTime + "ms.");
        return false;
    }

    public boolean isCellRequestStrategyPassed(ServiceStateTracker stateTracker, WorkSource workSource, GsmCdmaPhone phoneBase) {
        ServiceStateTracker serviceStateTracker = stateTracker;
        WorkSource workSource2 = workSource;
        GsmCdmaPhone gsmCdmaPhone = phoneBase;
        if (serviceStateTracker == null || workSource2 == null || gsmCdmaPhone == null || TextUtils.isEmpty(workSource2.getName(0))) {
            Rlog.e(TAG, "isCellRequestStrategyPassed():return false.Because null-pointer params");
            return false;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null && tm.getSimState(this.mPhoneBase.getSubId()) != 5) {
            Rlog.d(TAG, "isCellRequestStrategyPassed():return false.Because sim state not ready.");
            return false;
        } else if (!isCellAgeTimePassed(serviceStateTracker, gsmCdmaPhone)) {
            Rlog.d(TAG, "isCellRequestStrategyPassed():return false.Because isCellAgeTime is not passed.");
            return false;
        } else {
            long curSysTime = SystemClock.elapsedRealtime();
            long lastRequestTime = stateTracker.getLastCellInfoListTime();
            String pkgName = workSource2.getName(0);
            int uid = workSource2.get(0);
            long id = Binder.clearCallingIdentity();
            NetLocationStrategy strategy = HwSysResManager.getInstance().getNetLocationStrategy(pkgName, uid, 2);
            Binder.restoreCallingIdentity(id);
            if (strategy != null) {
                Rlog.d(TAG, "isCellRequestStrategyPassed():get iAware strategy result = " + strategy.toString());
                if (MODE_REQUEST_CELL_LIST_STRATEGY_INVALID == strategy.getCycle()) {
                    Rlog.d(TAG, "isCellRequestStrategyPassed():return false.Because iAware strategy return NOT_ALLOWED");
                    return false;
                } else if (MODE_REQUEST_CELL_LIST_STRATEGY_VALID >= strategy.getCycle() || curSysTime - lastRequestTime >= strategy.getCycle()) {
                    Rlog.d(TAG, "isCellRequestStrategyPassed():return true.");
                    return true;
                } else {
                    Rlog.d(TAG, "isCellRequestStrategyPassed():return false.Because already requested within iAware strategy cycle");
                    return false;
                }
            } else {
                Rlog.e(TAG, "isCellRequestStrategyPassed():get iAware strategy result = null.");
                return true;
            }
        }
    }

    public int getNetworkType(ServiceState ss) {
        if (ss.getDataNetworkType() != 0) {
            return ss.getDataNetworkType();
        }
        return ss.getVoiceNetworkType();
    }

    private void initNetworkRejInfo() {
        if (TelephonyManager.getDefault().getSimCount() == 2) {
            this.mPhoneBase.mCi.setOnNetReject(this, 156, null);
            this.mPhoneBase.mCi.registerForSimHotPlug(this, EVENT_SIM_HOTPLUG, Integer.valueOf(this.mPhoneBase.getPhoneId()));
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SERVICE_STATE"));
            NotificationManager mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(new NotificationChannel(NETWORK_REJINFO_NOTIFY_CHANNEL, this.mContext.getString(33686146), 3));
            }
        }
    }

    private void onNetworkRejInfoDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null) {
            String[] data2 = (String[]) ar.result;
            String rejectplmn = null;
            int rejectdomain = -1;
            int rejectcause = -1;
            int rejectrat = -1;
            if (data2.length >= 4) {
                try {
                    if (data2[0] != null && data2[0].length() > 0) {
                        rejectplmn = data2[0];
                    }
                    if (data2[1] != null && data2[1].length() > 0) {
                        rejectdomain = Integer.parseInt(data2[1]);
                    }
                    if (data2[2] != null && data2[2].length() > 0) {
                        rejectcause = Integer.parseInt(data2[2]);
                    }
                    if (data2[3] != null && data2[3].length() > 0) {
                        rejectrat = Integer.parseInt(data2[3]);
                    }
                } catch (Exception ex) {
                    Rlog.e(TAG, "error parsing NetworkReject!", ex);
                }
                handleNetworkRejinfoNotification(rejectplmn, rejectdomain, rejectcause, rejectrat);
            }
        }
    }

    private void onNetworkRejInfoTimeout(Message msg) {
        if (this.networkRejInfoList.size() > 3) {
            showNetworkRejInfoNotification();
            clearNetworkRejInfo();
            return;
        }
        this.networkRejInfoList.clear();
    }

    /* access modifiers changed from: private */
    public void clearNetworkRejInfo() {
        logd("clearNetworkRejInfo");
        this.networkRejInfoList.clear();
        removeMessages(EVENT_NETWORK_REJINFO_TIMEOUT);
        setNeedHandleRejInfoFlag(false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x007f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0081, code lost:
        return;
     */
    private synchronized void handleNetworkRejinfoNotification(String rejectplmn, int rejectdomain, int rejectcause, int rejectrat) {
        logd("handleNetworkRejinfoNotification:PLMN = " + rejectplmn + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat);
        boolean needHandleRejectCause = Arrays.binarySearch(NETWORK_REJINFO_CAUSES, rejectcause) >= 0;
        if (this.mNeedHandleRejInfoFlag && !TextUtils.isEmpty(rejectplmn)) {
            if (needHandleRejectCause) {
                if (!hasMessages(EVENT_NETWORK_REJINFO_TIMEOUT)) {
                    sendEmptyMessageDelayed(EVENT_NETWORK_REJINFO_TIMEOUT, 1200000);
                }
                if (!this.networkRejInfoList.contains(rejectplmn)) {
                    this.networkRejInfoList.add(rejectplmn);
                    logd("handleNetworkRejinfoNotification: add " + rejectplmn + " rejinfoList:" + this.networkRejInfoList);
                }
            }
        }
    }

    private void showNetworkRejInfoNotification() {
        if (this.mContext != null) {
            logd("showNetworkRejinfoNotification");
            Intent resultIntent = new Intent(DUAL_CARDS_SETTINGS_ACTIVITY);
            resultIntent.setFlags(335544320);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this.mContext, 0, resultIntent, 134217728);
            Notification.Action dualCardSettingsAction = new Notification.Action.Builder(null, this.mContext.getString(33686145), resultPendingIntent).build();
            int showSubId = this.mPhoneBase.getPhoneId() + 1;
            Notification.Builder builder = new Notification.Builder(this.mContext).setSmallIcon(33752038).setAppName(this.mContext.getString(33686146)).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(-1).setContentTitle(this.mContext.getString(33686150, new Object[]{Integer.valueOf(showSubId)})).setContentText(this.mContext.getString(33686149, new Object[]{Integer.valueOf(showSubId)})).setContentIntent(resultPendingIntent).setStyle(new Notification.BigTextStyle()).setChannelId(NETWORK_REJINFO_NOTIFY_CHANNEL).addAction(dualCardSettingsAction);
            NotificationManager mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            if (mNotificationManager != null) {
                mNotificationManager.notify(TAG, this.mPhoneBase.getPhoneId(), builder.build());
            }
        }
    }

    private void onSimHotPlug(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null && ar.result != null && ((int[]) ar.result).length > 0) {
            if (HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.result)[0]) {
                setNeedHandleRejInfoFlag(true);
            } else if (HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) ar.result)[0]) {
                clearNetworkRejInfo();
            }
        }
    }

    private void setNeedHandleRejInfoFlag(boolean needHandleRejInfoFlag) {
        if (this.mNeedHandleRejInfoFlag != needHandleRejInfoFlag) {
            this.mNeedHandleRejInfoFlag = needHandleRejInfoFlag;
            logd("set mNeedHandleRejInfoFlag =" + this.mNeedHandleRejInfoFlag);
        }
    }

    private void registerCarrierConfig(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        if (context != null) {
            context.registerReceiver(this.mReceiver, filter);
        }
    }

    /* access modifiers changed from: private */
    public void logd(String msg) {
        Rlog.d(TAG, "[SUB" + this.mPhoneBase.getPhoneId() + "]" + msg);
    }

    public void sendTimeZoneSelectionNotification(GsmCdmaPhone phone) {
        if (phone.isPhoneTypeGsm() && SystemProperties.getBoolean("ro.config_hw_doubletime", false) && HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == phone.getPhoneId()) {
            logd("[settimezone]roaming on, waiting for a few minutes to see if the NITZ is supported by the current network.");
            Message roamingOn = obtainMessage();
            roamingOn.what = EVENT_NITZ_CAPABILITY_NOTIFICATION;
            sendMessageDelayed(roamingOn, 60000);
        }
    }

    public void sendTimeZoneSelectionNotification() {
        String currentMcc = null;
        ContentResolver mCr = this.mPhoneBase.getContext().getContentResolver();
        if (this.mServiceStateTracker == null || this.mServiceStateTracker.mSS == null) {
            logd("sendTimeZoneSelectionNotification, invalid para");
            return;
        }
        String operator = this.mServiceStateTracker.mSS.getOperatorNumeric();
        if (operator != null && operator.length() >= 3) {
            currentMcc = operator.substring(0, 3);
        }
        if (!this.mServiceStateTracker.getNitzState().getNitzTimeZoneDetectionSuccessful()) {
            List<TimeZone> timeZones = null;
            int tzListSize = 0;
            String iso = this.mServiceStateTracker.getSystemProperty("gsm.operator.iso-country", "");
            String lastMcc = Settings.System.getString(mCr, "last_registed_mcc");
            boolean isTheSameNWAsLast = (lastMcc == null || currentMcc == null || !lastMcc.equals(currentMcc)) ? false : true;
            logd("[settimezone] the network " + operator + " don't support nitz! current network isTheSameNWAsLast" + isTheSameNWAsLast);
            if (!"".equals(iso)) {
                timeZones = TimeZoneFinder.getInstance().lookupTimeZonesByCountry(iso);
                tzListSize = timeZones == null ? 0 : timeZones.size();
            }
            if (1 != tzListSize || isTheSameNWAsLast) {
                logd("[settimezone] there are " + tzListSize + " timezones in " + iso);
                Intent intent = new Intent(ACTION_TIMEZONE_SELECTION);
                intent.putExtra("operator", operator);
                intent.putExtra("iso", iso);
                this.mPhoneBase.getContext().sendStickyBroadcast(intent);
            } else {
                TimeZone tz = timeZones.get(0);
                logd("[settimezone] time zone:" + tz.getID());
                TimeServiceHelper.setDeviceTimeZoneStatic(this.mPhoneBase.getContext(), tz.getID());
            }
        }
        if (currentMcc != null) {
            Settings.System.putString(mCr, "last_registed_mcc", currentMcc);
        }
    }

    public boolean isAllowLocationUpdate(int pid) {
        return isContainPackage(data, getAppName(pid));
    }

    private String getAppName(int pid) {
        String processName = "";
        List list = ((ActivityManager) this.mPhoneBase.getContext().getSystemService("activity")).getRunningAppProcesses();
        if (list == null) {
            return processName;
        }
        Iterator i = list.iterator();
        while (true) {
            if (!i.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo info = i.next();
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                    break;
                }
            } catch (RuntimeException e) {
                logd("RuntimeException");
            } catch (Exception e2) {
                logd("Get The appName is wrong");
            }
        }
        return processName;
    }

    private boolean isContainPackage(String data2, String packageName) {
        String[] enablePackage = null;
        if (!TextUtils.isEmpty(data2)) {
            enablePackage = data2.split(";");
        }
        if (enablePackage == null || enablePackage.length == 0) {
            return false;
        }
        for (int i = 0; i < enablePackage.length; i++) {
            if (!TextUtils.isEmpty(packageName) && packageName.equals(enablePackage[i])) {
                return true;
            }
        }
        return false;
    }

    private void loadAllowUpdateLocationPackage(Context context) {
        if (context != null) {
            ContentResolver cr = context.getContentResolver();
            data = cr != null ? Settings.System.getString(cr, "enable_get_location") : null;
        }
    }

    public boolean signalStrengthResultHW(GsmCdmaPhone phone, AsyncResult ar, SignalStrength signalStrength, boolean isGsm) {
        SignalStrength newSignalStrength;
        SignalStrength oldSignalStrength = signalStrength;
        if (ar.exception != null || ar.result == null || !(ar.result instanceof SignalStrength)) {
            if (ar.exception != null) {
                logd("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            } else if (ar.result != null) {
                logd("result : " + ar.result);
            } else {
                logd("ar.result is null!");
            }
            newSignalStrength = new SignalStrength(isGsm);
        } else {
            newSignalStrength = (SignalStrength) ar.result;
            newSignalStrength.validateInput();
            newSignalStrength.setGsm(isGsm);
        }
        HwTelephonyFactory.getHwNetworkManager().updateHwnff(this.mServiceStateTracker, newSignalStrength);
        if (this.mPhoneBase.getPhoneType() == 1) {
            return getHwGsmServiceStateManager(this.mServiceStateTracker, phone).notifySignalStrength(oldSignalStrength, newSignalStrength);
        }
        newSignalStrength.setCdma(true);
        return getHwCdmaServiceStateManager(this.mServiceStateTracker, phone).notifySignalStrength(oldSignalStrength, newSignalStrength);
    }

    public boolean checkForRoamingForIndianOperators(ServiceState s) {
        String simNumeric = SystemProperties.get("gsm.sim.operator.numeric", "");
        String operatorNumeric = s.getOperatorNumeric();
        try {
            String simMCC = simNumeric.substring(0, 3);
            String operatorMCC = operatorNumeric.substring(0, 3);
            if ((simMCC.equals("404") || simMCC.equals("405")) && (operatorMCC.equals("404") || operatorMCC.equals("405"))) {
                return true;
            }
        } catch (RuntimeException e) {
        }
        return false;
    }

    public boolean recoverAutoSelectMode(GsmCdmaPhone phone, boolean recoverAutoSelectMode) {
        boolean skipRestoringSelection = phone.getContext().getResources().getBoolean(17957110);
        if (getRecoverAutoModeFutureState(phone)) {
            logd("Feature recover network mode automatic is on..");
            return true;
        }
        if (!skipRestoringSelection) {
            if (!HwModemCapability.isCapabilitySupport(4) || KEEP_NW_SEL_MANUAL) {
                phone.restoreSavedNetworkSelection(null);
            } else {
                logd("Modem can select network auto with manual mode");
            }
        }
        return recoverAutoSelectMode;
    }

    public boolean getRecoverAutoModeFutureState(GsmCdmaPhone phone) {
        int subId = phone.getSubId();
        Boolean valueFromCard = (Boolean) HwCfgFilePolicy.getValue("recover_auto_mode", subId, Boolean.class);
        boolean valueFromProp = FEATURE_RECOVER_AUTO_NETWORK_MODE;
        logd("getRecoverAutoModeFutureState, subId:" + subId + ", card:" + valueFromCard + ", prop:" + valueFromProp);
        return valueFromCard != null ? valueFromCard.booleanValue() : valueFromProp;
    }

    public int getNrConfigTechnology(GsmCdmaPhone phone, int newRat, int nsaState) {
        if (newRat != 19 && newRat != 14) {
            return newRat;
        }
        String config = (String) HwCfgFilePolicy.getValue("nr_technology_config", phone.getPhoneId(), String.class);
        if (TextUtils.isEmpty(config)) {
            config = NR_TECHNOLOGY_CONFIGA;
        }
        logd("nsaState: " + nsaState + "   config: " + config);
        switch (nsaState) {
            case 2:
                if (NR_TECHNOLOGY_CONFIGD.equals(config)) {
                    return 20;
                }
                return newRat;
            case 3:
                if (NR_TECHNOLOGY_CONFIGC.equals(config) || NR_TECHNOLOGY_CONFIGD.equals(config)) {
                    return 20;
                }
                return newRat;
            case 4:
                if (NR_TECHNOLOGY_CONFIGB.equals(config) || NR_TECHNOLOGY_CONFIGC.equals(config) || NR_TECHNOLOGY_CONFIGD.equals(config)) {
                    return 20;
                }
                return newRat;
            case 5:
                return 20;
            default:
                return newRat;
        }
    }
}
