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
import com.android.ims.HwImsManager;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.wifipro.WifiProCommonUtils;

public class HwMultiNlpPolicy {
    private static final int DEFAULT_BUFFER_SIZE = 16;
    public static final String GLOBAL_NLP_CLIENT_PKG = "com.hisi.mapcon";
    public static final String GLOBAL_NLP_DEBUG_PKG = "com.huawei.android.gpsselfcheck";
    private static final String GMS_VERSION = SystemProperties.get("ro.com.google.gmsversion", "");
    private static final String HW_LOCATION_DEBUG = "hw_location_debug";
    private static final String HW_NLP_GLOBAL = "hw_nlp_global";
    private static final String HW_NLP_VOWIFI = "hw_nlp_vowifi";
    public static final int NLP_AB = 2;
    public static final int NLP_GOOGLE = 1;
    public static final int NLP_NONE = 0;
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private static final String TAG = "HwMultiNlpPolicy";
    private static final String WFC_IMS_ENABLED = "wfc_ims_enabled";
    private static final String WFC_IMS_ENABLED_0 = "wfc_ims_enabled_0";
    private static final String WFC_IMS_ENABLED_1 = "wfc_ims_enabled_1";
    private static volatile HwMultiNlpPolicy instance;
    private static boolean sIsChineseVersion = false;
    private boolean isChineseSimCard = false;
    private boolean isRegistedInChineseNetwork = false;
    private boolean isSimCardReady = false;
    private boolean isVSimEnabled = false;
    private boolean isWifiNetworkConnected = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.HwMultiNlpPolicy.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                LBSLog.i(HwMultiNlpPolicy.TAG, false, "receive broadcast intent, action: %{public}s", action);
                if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getType() == 1) {
                        NetworkInfo info2 = ((ConnectivityManager) HwMultiNlpPolicy.this.mContext.getSystemService("connectivity")).getNetworkInfo(info.getType());
                        if (info2 != null) {
                            HwMultiNlpPolicy.this.isWifiNetworkConnected = info2.isConnected();
                        }
                        LBSLog.i(HwMultiNlpPolicy.TAG, false, "isWifiNetworkConnected: %{public}b", Boolean.valueOf(HwMultiNlpPolicy.this.isWifiNetworkConnected));
                    }
                } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED.equals(action)) {
                    HwMultiNlpPolicy hwMultiNlpPolicy = HwMultiNlpPolicy.this;
                    hwMultiNlpPolicy.setOrUpdateChineseSimCard(hwMultiNlpPolicy.mDataSub);
                    if (HwMultiNlpPolicy.this.isHwNlpGlobal()) {
                        HwMultiNlpPolicy.this.handleServiceAb();
                    }
                } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED.equals(action)) {
                    HwMultiNlpPolicy.this.mDataSub = intent.getIntExtra("subscription", -1);
                    HwMultiNlpPolicy hwMultiNlpPolicy2 = HwMultiNlpPolicy.this;
                    hwMultiNlpPolicy2.setOrUpdateChineseSimCard(hwMultiNlpPolicy2.mDataSub);
                }
            }
        }
    };
    private Context mContext;
    private int mDataSub = -1;
    private HwLocationProxy mHwGeocoderProxy = new HwNullLocationProxy();
    private HwLocationProxy mHwLocationProviderProxy = new HwNullLocationProxy();
    private volatile boolean mIsGlobalGeocoderStart = false;
    private volatile boolean mIsGlobalNlpStart = false;
    private boolean mIsHwNlpGlobal = false;
    private int mLastChoiceNlp = 0;
    private PhoneStateListener[] mPhoneStateListeners;
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
        this.mIsHwNlpGlobal = AppActConstant.VALUE_TRUE.equalsIgnoreCase(Settings.Global.getString(this.mContext.getContentResolver(), HW_NLP_GLOBAL));
        sIsChineseVersion = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        filter.addCategory("android.net.conn.CONNECTIVITY_CHANGE@hwBrExpand@ConnectStatus=WIFIDATACON|ConnectStatus=WIFIDATADSCON");
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        registerPhoneStateListener();
        getDataSubscription();
    }

    private void registerPhoneStateListener() {
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mPhoneStateListeners = new PhoneStateListener[3];
        for (int i = 0; i < 3; i++) {
            this.mPhoneStateListeners[i] = getPhoneStateListener(i);
            this.mTelephonyManager.listen(this.mPhoneStateListeners[i], 1);
        }
    }

    private PhoneStateListener getPhoneStateListener(int subId) {
        return new PhoneStateListener(Integer.valueOf(subId)) {
            /* class com.android.server.location.HwMultiNlpPolicy.AnonymousClass2 */

            @Override // android.telephony.PhoneStateListener
            public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                    LBSLog.i(HwMultiNlpPolicy.TAG, false, "PhoneStateListener %{public}d , mDataSub %{public}d", this.mSubId, Integer.valueOf(HwMultiNlpPolicy.this.mDataSub));
                    if (this.mSubId.intValue() == HwMultiNlpPolicy.this.mDataSub && HwMultiNlpPolicy.this.mDataSub != -1) {
                        HwMultiNlpPolicy.this.setSimCardState(state.getDataRegState());
                        HwMultiNlpPolicy.this.setRegistedNetwork(state.getOperatorNumeric());
                    } else if (this.mSubId.intValue() == 2) {
                        HwMultiNlpPolicy.this.isVSimEnabled = HwTelephonyManager.getDefault().isVSimEnabled();
                    }
                }
            }
        };
    }

    public boolean shouldUseGoogleNLP(boolean update) {
        return choiceNlp(update) == 1;
    }

    public boolean shouldBeRecheck() {
        return this.mLastChoiceNlp != choiceNlp(false);
    }

    public int shouldUseNLP() {
        return choiceNlp(false);
    }

    private int choiceNlp(boolean update) {
        int result;
        LBSLog.i(TAG, false, "choiceNlp : %{public}b , %{public}b , %{public}b , %{public}b ,%{public}b", Boolean.valueOf(this.isSimCardReady), Boolean.valueOf(this.isWifiNetworkConnected), Boolean.valueOf(this.isRegistedInChineseNetwork), Boolean.valueOf(this.isVSimEnabled), Boolean.valueOf(this.isChineseSimCard));
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
        LBSLog.i(TAG, false, "choiceNlp %{public}d", Integer.valueOf(result));
        if (update) {
            this.mLastChoiceNlp = result;
        }
        return result;
    }

    public static boolean isChineseVersion() {
        return sIsChineseVersion;
    }

    public static boolean isGmsExist() {
        return !sIsChineseVersion && !"".equals(GMS_VERSION);
    }

    public static boolean isOverseasNoGms() {
        return !sIsChineseVersion && "".equals(GMS_VERSION);
    }

    public static String getHmsPackageName(Context context) {
        String hmsPackageName;
        if (context == null || (hmsPackageName = Settings.Global.getString(context.getContentResolver(), "hms_package_name")) == null || "".equals(hmsPackageName)) {
            return "";
        }
        return hmsPackageName;
    }

    private void getDataSubscription() {
        this.mDataSub = Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", 0);
        LBSLog.i(TAG, false, "getDataSubscription mDataSub: %{public}d", Integer.valueOf(this.mDataSub));
    }

    public int getPreferredDataSubscription() {
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSimCardState(int simState) {
        this.isSimCardReady = simState == 0;
        LBSLog.i(TAG, false, "setSimCardReady %{public}d", Integer.valueOf(simState));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setOrUpdateChineseSimCard(int dataSub) {
        if (dataSub != -1) {
            String simCountry = TelephonyManager.getDefault().getSimCountryIso(dataSub);
            boolean isCtSimCard = HwTelephonyManager.getDefault().isCTSimCard(dataSub);
            if ((simCountry == null || !"CN".equalsIgnoreCase(simCountry)) && !isCtSimCard) {
                this.isChineseSimCard = false;
            } else {
                this.isChineseSimCard = true;
            }
        }
        LBSLog.i(TAG, false, "isChineseSimCard %{public}b , dataSub %{public}d", Boolean.valueOf(this.isChineseSimCard), Integer.valueOf(dataSub));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRegistedNetwork(String numeric) {
        if (numeric != null && numeric.length() >= 5 && "99999".equals(numeric.substring(0, 5))) {
            LBSLog.i(TAG, false, "not a country code", new Object[0]);
        } else if (numeric != null && numeric.length() >= 3 && WifiProCommonUtils.COUNTRY_CODE_CN.equals(numeric.substring(0, 3))) {
            this.isRegistedInChineseNetwork = true;
        } else if (numeric == null || "".equals(numeric)) {
            LBSLog.d(TAG, false, "setRegistedNetwork", new Object[0]);
        } else {
            this.isRegistedInChineseNetwork = false;
        }
        LBSLog.i(TAG, false, "setRegistedNetwork %{private}s", numeric);
    }

    private boolean isVsimRegistedInChineseNetwork() {
        boolean ret = false;
        String networkCountry = SystemProperties.get("gsm.operator.iso-country.vsim");
        if (networkCountry != null && "CN".equalsIgnoreCase(networkCountry)) {
            ret = true;
        }
        LBSLog.i(TAG, false, "isVsimRegistedInChineseNetwork %{public}b", Boolean.valueOf(ret));
        return ret;
    }

    private boolean isRegistedInChineseNetworkWithoutSimCard() {
        boolean ret = false;
        String rplmns = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
        if (rplmns != null && rplmns.length() > 3 && WifiProCommonUtils.COUNTRY_CODE_CN.equals(rplmns.substring(0, 3))) {
            ret = true;
        }
        LBSLog.i(TAG, false, "isRegistedInChineseNetworkWithoutSimCard rplmns %{public}s, ret %{public}b", rplmns, Boolean.valueOf(ret));
        return ret;
    }

    private boolean isRegistedInForeginNetworkWithoutSimCard() {
        boolean ret = false;
        String rplmns = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
        if (rplmns != null && rplmns.length() > 3 && !WifiProCommonUtils.COUNTRY_CODE_CN.equals(rplmns.substring(0, 3))) {
            ret = true;
        }
        LBSLog.i(TAG, false, "isRegistedInForeginNetworkWithoutSimCard rplmns %{public}s, ret ", rplmns, Boolean.valueOf(ret));
        return ret;
    }

    public void setHwLocationProviderProxy(HwLocationProviderProxy hwLocationProviderProxy) {
        this.mHwLocationProviderProxy = hwLocationProviderProxy;
    }

    public void setHwGeocoderProxy(HwGeocoderProxy hwGeocoderProxy) {
        this.mHwGeocoderProxy = hwGeocoderProxy;
    }

    public void initHwNLPVowifi(Handler handler) {
        if (isHwNlpGlobal()) {
            handleServiceAb();
            BroadcastReceiver receiver = new BroadcastReceiver() {
                /* class com.android.server.location.HwMultiNlpPolicy.AnonymousClass3 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (intent != null) {
                        String action = intent.getAction();
                        if ("android.intent.action.AIRPLANE_MODE".equals(action) || "android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                            HwMultiNlpPolicy.this.handleServiceAb();
                        }
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.AIRPLANE_MODE");
            filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
            this.mContext.registerReceiver(receiver, filter);
            ContentObserver observer = new ContentObserver(handler) {
                /* class com.android.server.location.HwMultiNlpPolicy.AnonymousClass4 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    HwMultiNlpPolicy.this.handleServiceAb();
                }
            };
            if (isDualImsAvailable()) {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WFC_IMS_ENABLED_0), false, observer);
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WFC_IMS_ENABLED_1), false, observer);
                return;
            }
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(WFC_IMS_ENABLED), false, observer);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceAb() {
        boolean isHwNlpVowifi = isHwNlpVowifi();
        boolean shouldStart = false;
        boolean isAirplaneModeOn = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        boolean isVoWifiOn = isVowifiEnabled();
        StringBuilder sb = new StringBuilder(16);
        sb.append("handleServiceAb isHwNlpVowifi: ");
        sb.append(isHwNlpVowifi);
        sb.append(", isAirplaneModeOn: ");
        sb.append(isAirplaneModeOn);
        sb.append(", isVoWifiOn: ");
        sb.append(isVoWifiOn);
        LBSLog.i(TAG, false, "%{public}s", sb.toString());
        if (isHwNlpVowifi && isAirplaneModeOn && isVoWifiOn) {
            shouldStart = true;
        }
        handleGlobalNlp(shouldStart);
        handleGlobalGeocoder(shouldStart);
    }

    private void handleGlobalNlp(boolean shouldStart) {
        if (shouldStart && this.mIsGlobalNlpStart) {
            return;
        }
        if (shouldStart || this.mIsGlobalNlpStart) {
            this.mIsGlobalNlpStart = shouldStart;
            this.mHwLocationProviderProxy.handleServiceAB(shouldStart);
        }
    }

    private void handleGlobalGeocoder(boolean shouldStart) {
        if (shouldStart && this.mIsGlobalGeocoderStart) {
            return;
        }
        if (shouldStart || this.mIsGlobalGeocoderStart) {
            this.mIsGlobalGeocoderStart = shouldStart;
            this.mHwGeocoderProxy.handleServiceAB(shouldStart);
        }
    }

    public boolean getGlobalNLPStart() {
        return this.mIsGlobalNlpStart;
    }

    public boolean getGlobalGeocoderStart() {
        return this.mIsGlobalGeocoderStart;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isHwNlpGlobal() {
        return !sIsChineseVersion && this.mIsHwNlpGlobal;
    }

    private boolean isHwNlpVowifi() {
        return isHwNlpGlobal() && AppActConstant.VALUE_TRUE.equalsIgnoreCase(Settings.Global.getString(this.mContext.getContentResolver(), HW_NLP_VOWIFI));
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
        return isWfcEnabled || isHwLocationDebug(this.mContext);
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
