package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import com.android.ims.HwImsManager;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.wifipro.WifiProCommonUtils;

public class HwMultiNlpPolicy {
    private static final String HW_LOCATION_DEBUG = "hw_location_debug";
    private static final String HW_NLP_GLOBAL = "hw_nlp_global";
    private static final String HW_NLP_VOWIFI = "hw_nlp_vowifi";
    public static final int NLP_AB = 2;
    public static final int NLP_GOOGLE = 1;
    public static final int NLP_NONE = 0;
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final String TAG = "HwMultiNlpPolicy";
    private static final String WFC_IMS_ENABLED = "wfc_ims_enabled";
    private static final String WFC_IMS_ENABLED_0 = "wfc_ims_enabled_0";
    private static final String WFC_IMS_ENABLED_1 = "wfc_ims_enabled_1";
    private static volatile HwMultiNlpPolicy instance;
    private static final boolean mIsChineseVersion = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private boolean isChineseSimCard = false;
    private boolean isRegistedInChineseNetwork = false;
    private boolean isSimCardReady = false;
    /* access modifiers changed from: private */
    public boolean isVSimEnabled = false;
    /* access modifiers changed from: private */
    public boolean isWifiNetworkConnected = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(HwMultiNlpPolicy.TAG, "receive broadcast intent, action: " + action);
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (info != null && info.getType() == 1) {
                    NetworkInfo info2 = ((ConnectivityManager) HwMultiNlpPolicy.this.mContext.getSystemService("connectivity")).getNetworkInfo(info.getType());
                    if (info2 != null) {
                        boolean unused = HwMultiNlpPolicy.this.isWifiNetworkConnected = info2.isConnected();
                    }
                    Slog.d(HwMultiNlpPolicy.TAG, "isWifiNetworkConnected: " + HwMultiNlpPolicy.this.isWifiNetworkConnected);
                }
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                HwMultiNlpPolicy.this.setOrUpdateChineseSimCard(HwMultiNlpPolicy.this.mDataSub);
                if (HwMultiNlpPolicy.this.isHwNLPGlobal()) {
                    HwMultiNlpPolicy.this.handleServiceAb();
                }
            } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                int unused2 = HwMultiNlpPolicy.this.mDataSub = intent.getIntExtra("subscription", -1);
                HwMultiNlpPolicy.this.setOrUpdateChineseSimCard(HwMultiNlpPolicy.this.mDataSub);
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mDataSub = -1;
    private volatile boolean mGlobalGeocoderStart = false;
    private volatile boolean mGlobalNLPStart = false;
    private boolean mIsHwNLPGlobal;
    private int mLastChoiceNlp = 0;
    private PhoneStateListener[] mPhoneStateListener;
    private TelephonyManager mTelephonyManager;

    public static HwMultiNlpPolicy getDefault(Context context) {
        if (instance == null) {
            instance = new HwMultiNlpPolicy(context);
        }
        return instance;
    }

    public static HwMultiNlpPolicy getDefault() {
        return instance;
    }

    public HwMultiNlpPolicy(Context context) {
        this.mContext = context;
        this.mIsHwNLPGlobal = "true".equalsIgnoreCase(Settings.Global.getString(this.mContext.getContentResolver(), HW_NLP_GLOBAL));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addCategory("android.net.conn.CONNECTIVITY_CHANGE@hwBrExpand@ConnectStatus=WIFIDATACON|ConnectStatus=WIFIDATADSCON");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        registerPhoneStateListener();
        getDataSubscription();
    }

    private void registerPhoneStateListener() {
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mPhoneStateListener = new PhoneStateListener[3];
        for (int i = 0; i < 3; i++) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            this.mTelephonyManager.listen(this.mPhoneStateListener[i], 1);
        }
    }

    private PhoneStateListener getPhoneStateListener(int subId) {
        return new PhoneStateListener(Integer.valueOf(subId)) {
            public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                    Slog.d(HwMultiNlpPolicy.TAG, "PhoneStateListener " + this.mSubId + ", mDataSub " + HwMultiNlpPolicy.this.mDataSub);
                    if (this.mSubId.intValue() == HwMultiNlpPolicy.this.mDataSub && HwMultiNlpPolicy.this.mDataSub != -1) {
                        HwMultiNlpPolicy.this.setSimCardState(state.getDataRegState());
                        HwMultiNlpPolicy.this.setRegistedNetwork(state.getOperatorNumeric());
                    } else if (this.mSubId.intValue() == 2) {
                        boolean unused = HwMultiNlpPolicy.this.isVSimEnabled = HwTelephonyManager.getDefault().isVSimEnabled();
                    }
                }
            }
        };
    }

    public boolean shouldUseGoogleNLP(boolean update) {
        return 1 == choiceNLP(update);
    }

    public boolean shouldBeRecheck() {
        return this.mLastChoiceNlp != choiceNLP(false);
    }

    public int shouldUseNLP() {
        return choiceNLP(false);
    }

    private int choiceNLP(boolean update) {
        int result;
        Slog.d(TAG, "choiceNLP isSimCardReady: " + this.isSimCardReady + " isWifiNetworkConnected: " + this.isWifiNetworkConnected + " isRegistedInChineseNetwork: " + this.isRegistedInChineseNetwork + " isVSimEnabled: " + this.isVSimEnabled + " isChineseSimCard: " + this.isChineseSimCard);
        if (this.isSimCardReady) {
            if (this.isWifiNetworkConnected) {
                if (this.isRegistedInChineseNetwork) {
                    result = 2;
                } else {
                    result = 1;
                }
            } else if (this.isVSimEnabled) {
                if (isVsimRegistedInChineseNetwork()) {
                    result = 2;
                } else {
                    result = 1;
                }
            } else if (this.isChineseSimCard) {
                result = 2;
            } else {
                result = 1;
            }
        } else if (this.isVSimEnabled) {
            if (isVsimRegistedInChineseNetwork()) {
                result = 2;
            } else {
                result = 1;
            }
        } else if (isRegistedInChineseNetworkWithoutSimCard()) {
            result = 2;
        } else if (isRegistedInForeginNetworkWithoutSimCard()) {
            result = 1;
        } else if (isChineseVersion()) {
            result = 2;
        } else {
            result = 1;
        }
        Slog.d(TAG, "choiceNLP " + result);
        if (update) {
            this.mLastChoiceNlp = result;
        }
        return result;
    }

    public static boolean isChineseVersion() {
        return mIsChineseVersion;
    }

    private void getDataSubscription() {
        this.mDataSub = Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", 0);
        Slog.d(TAG, "getDataSubscription mDataSub: " + this.mDataSub);
    }

    public int getPreferredDataSubscription() {
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    /* access modifiers changed from: private */
    public void setSimCardState(int simState) {
        this.isSimCardReady = simState == 0;
        Slog.d(TAG, "setSimCardReady " + simState);
    }

    /* access modifiers changed from: private */
    public void setOrUpdateChineseSimCard(int dataSub) {
        if (dataSub != -1) {
            String simCountry = TelephonyManager.getDefault().getSimCountryIso(dataSub);
            boolean isCtSimCard = HwTelephonyManager.getDefault().isCTSimCard(dataSub);
            Slog.d(TAG, "isChineseSimCard datasub " + dataSub + ", " + simCountry);
            if ((simCountry == null || !simCountry.equalsIgnoreCase("CN")) && !isCtSimCard) {
                this.isChineseSimCard = false;
            } else {
                this.isChineseSimCard = true;
            }
        }
        Slog.d(TAG, "isChineseSimCard " + this.isChineseSimCard + " , dataSub " + dataSub);
    }

    /* access modifiers changed from: private */
    public void setRegistedNetwork(String numeric) {
        if (numeric == null || numeric.length() < 5 || !numeric.substring(0, 5).equals("99999")) {
            if (numeric != null && numeric.length() >= 3 && numeric.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                this.isRegistedInChineseNetwork = true;
            } else if (numeric != null && !numeric.equals("")) {
                this.isRegistedInChineseNetwork = false;
            }
        }
        Slog.d(TAG, "setRegistedNetwork " + numeric);
    }

    private boolean isVsimRegistedInChineseNetwork() {
        boolean ret = false;
        String networkCountry = SystemProperties.get("gsm.operator.iso-country.vsim");
        Slog.d(TAG, "isVsimRegistedInChineseNetwork " + networkCountry);
        if (networkCountry != null && networkCountry.equalsIgnoreCase("CN")) {
            ret = true;
        }
        Slog.d(TAG, "isVsimRegistedInChineseNetwork " + ret);
        return ret;
    }

    private boolean isRegistedInChineseNetworkWithoutSimCard() {
        boolean ret = false;
        String rplmns = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
        if (rplmns != null && rplmns.length() > 3 && rplmns.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
            ret = true;
        }
        Slog.d(TAG, "isRegistedInChineseNetworkWithoutSimCard rplmns " + rplmns + ", ret " + ret);
        return ret;
    }

    private boolean isRegistedInForeginNetworkWithoutSimCard() {
        boolean ret = false;
        String rplmns = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
        if (rplmns != null && rplmns.length() > 3 && !rplmns.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
            ret = true;
        }
        Slog.d(TAG, "isRegistedInForeginNetworkWithoutSimCard rplmns " + rplmns + ", ret " + ret);
        return ret;
    }

    public void initHwNLPVowifi(Handler handler) {
        if (isHwNLPGlobal()) {
            handleServiceAb();
            BroadcastReceiver receiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if ("android.intent.action.AIRPLANE_MODE".equals(action) || "android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                        HwMultiNlpPolicy.this.handleServiceAb();
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.AIRPLANE_MODE");
            filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
            this.mContext.registerReceiver(receiver, filter);
            ContentObserver observer = new ContentObserver(handler) {
                public void onChange(boolean selfChange) {
                    HwMultiNlpPolicy.this.handleServiceAb();
                }
            };
            if (isDualImsAvailable()) {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WFC_IMS_ENABLED_0), false, observer);
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WFC_IMS_ENABLED_1), false, observer);
            } else {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WFC_IMS_ENABLED), false, observer);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleServiceAb() {
        boolean isVoWifiOn;
        boolean isHwNLPVowifi = isHwNlpVowifi();
        boolean shouldStart = true;
        boolean isAirplaneModeOn = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        Log.i(TAG, "handleServiceAb isHwNLPVowifi " + isHwNLPVowifi + ", isAirplaneModeOn " + isAirplaneModeOn + ", isVoWifiOn " + isVoWifiOn);
        if (!isHwNLPVowifi || !isAirplaneModeOn || !isVoWifiOn) {
            shouldStart = false;
        }
        handleGlobalNLP(shouldStart);
        handleGlobalGeocoder(shouldStart);
    }

    private void handleGlobalNLP(boolean shouldStart) {
        if (shouldStart && this.mGlobalNLPStart) {
            return;
        }
        if (shouldStart || this.mGlobalNLPStart) {
            LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();
            HwLocationProviderProxy hwLocationProviderProxy = null;
            if (util != null) {
                synchronized (util.getmLock()) {
                    HwLocationProviderProxy hwLocationProviderProxy2 = (LocationProviderProxy) util.getProvidersByName().get("network");
                    if (hwLocationProviderProxy2 instanceof HwLocationProviderProxy) {
                        hwLocationProviderProxy = hwLocationProviderProxy2;
                    }
                }
                if (hwLocationProviderProxy != null) {
                    this.mGlobalNLPStart = shouldStart;
                    hwLocationProviderProxy.handleServiceAb(shouldStart);
                }
            }
        }
    }

    private void handleGlobalGeocoder(boolean shouldStart) {
        if (shouldStart && this.mGlobalGeocoderStart) {
            return;
        }
        if (shouldStart || this.mGlobalGeocoderStart) {
            LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();
            HwGeocoderProxy hwGeocoderProxy = null;
            if (util != null) {
                HwGeocoderProxy hwGeocoderProxy2 = util.getGeocoderProvider();
                if (hwGeocoderProxy2 instanceof HwGeocoderProxy) {
                    hwGeocoderProxy = hwGeocoderProxy2;
                }
                if (hwGeocoderProxy != null) {
                    this.mGlobalGeocoderStart = shouldStart;
                    hwGeocoderProxy.handleServiceAb(shouldStart);
                }
            }
        }
    }

    public boolean getGlobalNLPStart() {
        return this.mGlobalNLPStart;
    }

    public boolean getGlobalGeocoderStart() {
        return this.mGlobalGeocoderStart;
    }

    /* access modifiers changed from: private */
    public boolean isHwNLPGlobal() {
        return !mIsChineseVersion && this.mIsHwNLPGlobal;
    }

    private boolean isHwNlpVowifi() {
        return isHwNLPGlobal() && "true".equalsIgnoreCase(Settings.Global.getString(this.mContext.getContentResolver(), HW_NLP_VOWIFI));
    }

    public static boolean isHwLocationDebug(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), HW_LOCATION_DEBUG, 0) == 1;
    }

    private boolean isVowifiEnabled() {
        boolean isWfcEnabled;
        if (isDualImsAvailable()) {
            isWfcEnabled = isWfcEnabled(0) || isWfcEnabled(1);
        } else {
            isWfcEnabled = isWfcEnabled(0);
        }
        if (isWfcEnabled || isHwLocationDebug(this.mContext)) {
            return true;
        }
        return false;
    }

    private boolean isWfcEnabled(int subId) {
        if (HwImsManager.isWfcEnabledByPlatform(this.mContext, subId)) {
            return HwImsManager.isWfcEnabledByUser(this.mContext, subId);
        }
        return false;
    }

    private static boolean isDualImsAvailable() {
        return HwImsManager.isDualImsAvailable();
    }
}
