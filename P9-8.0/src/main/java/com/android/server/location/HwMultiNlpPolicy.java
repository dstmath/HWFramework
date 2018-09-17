package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Slog;
import com.android.server.wifipro.WifiProCommonUtils;

public class HwMultiNlpPolicy {
    public static final int NLP_AB = 2;
    public static final int NLP_GOOGLE = 1;
    public static final int NLP_NONE = 0;
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final String TAG = "HwMultiNlpPolicy";
    private static volatile HwMultiNlpPolicy instance;
    private boolean isChineseSimCard = false;
    private boolean isRegistedInChineseNetwork = false;
    private boolean isSimCardReady = false;
    private boolean isVSimEnabled = false;
    private boolean isWifiNetworkConnected = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(HwMultiNlpPolicy.TAG, "receive broadcast intent, action: " + action);
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (info != null && info.getType() == 1) {
                    info = ((ConnectivityManager) HwMultiNlpPolicy.this.mContext.getSystemService("connectivity")).getNetworkInfo(info.getType());
                    if (info != null) {
                        HwMultiNlpPolicy.this.isWifiNetworkConnected = info.isConnected();
                    }
                    Slog.d(HwMultiNlpPolicy.TAG, "isWifiNetworkConnected: " + HwMultiNlpPolicy.this.isWifiNetworkConnected);
                }
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                HwMultiNlpPolicy.this.setOrUpdateChineseSimCard(HwMultiNlpPolicy.this.mDataSub);
            } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                HwMultiNlpPolicy.this.mDataSub = intent.getIntExtra("subscription", -1);
                HwMultiNlpPolicy.this.setOrUpdateChineseSimCard(HwMultiNlpPolicy.this.mDataSub);
            }
        }
    };
    private Context mContext;
    private int mDataSub = -1;
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
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
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
                        HwMultiNlpPolicy.this.isVSimEnabled = HwTelephonyManager.getDefault().isVSimEnabled();
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
        return "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    }

    private void getDataSubscription() {
        this.mDataSub = Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", 0);
        Slog.d(TAG, "getDataSubscription mDataSub: " + this.mDataSub);
    }

    public int getPreferredDataSubscription() {
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }

    private void setSimCardState(int simState) {
        boolean z = false;
        if (simState == 0) {
            z = true;
        }
        this.isSimCardReady = z;
        Slog.d(TAG, "setSimCardReady " + simState);
    }

    private void setOrUpdateChineseSimCard(int dataSub) {
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

    private void setRegistedNetwork(String numeric) {
        if (numeric == null || numeric.length() < 5 || !numeric.substring(0, 5).equals("99999")) {
            if (numeric != null && numeric.length() >= 3 && numeric.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                this.isRegistedInChineseNetwork = true;
            } else if (!(numeric == null || (numeric.equals("") ^ 1) == 0)) {
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
        if (!(rplmns == null || rplmns.length() <= 3 || (rplmns.substring(0, 3).equals(WifiProCommonUtils.COUNTRY_CODE_CN) ^ 1) == 0)) {
            ret = true;
        }
        Slog.d(TAG, "isRegistedInForeginNetworkWithoutSimCard rplmns " + rplmns + ", ret " + ret);
        return ret;
    }
}
