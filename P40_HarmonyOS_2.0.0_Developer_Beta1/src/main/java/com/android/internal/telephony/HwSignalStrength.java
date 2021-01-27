package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import huawei.cust.HwCfgFilePolicy;
import java.util.Map;

public class HwSignalStrength {
    private static final int CDMA_ECIO_QCOM_MULTIPLE = 10;
    public static final int DEFAULT_NUM_SIGNAL_STRENGTH_BINS = 4;
    protected static final String DEFAULT_SIGNAL_CUST_CDMA = "5,false,-112,-106,-99,-92,-85";
    protected static final String DEFAULT_SIGNAL_CUST_CDMALTE = "5,false,-120,-115,-110,-105,-97";
    protected static final String DEFAULT_SIGNAL_CUST_EVDO = "5,false,-112,-106,-99,-92,-85";
    protected static final String DEFAULT_SIGNAL_CUST_GSM = "5,false,-109,-103,-97,-91,-85";
    protected static final String DEFAULT_SIGNAL_CUST_LTE = "5,false,-120,-115,-110,-105,-97";
    protected static final String DEFAULT_SIGNAL_CUST_NR = "5,false,-120,-115,-110,-105,-97";
    protected static final String DEFAULT_SIGNAL_CUST_UMTS = "5,false,-112,-105,-99,-93,-87";
    private static final int ECIO_HISI_MULTIPLE = 1;
    public static final int GSM_STRENGTH_NONE = 0;
    public static final int GSM_STRENGTH_UNKOUWN = 99;
    public static final int INVALID_ECIO = 255;
    public static final int INVALID_PHONEID = -1;
    public static final int INVALID_RSSI = -1;
    public static final String KEY_SIGNAL_STRENGTH_STRING = "signal_strength";
    private static final String LOG_TAG = "HwSignalStrength";
    private static final int LTE_RSRQ_QCOM_MULTIPLE = 10;
    public static final int LTE_RSSNR_UNKOUWN_STD = 99;
    public static final int LTE_STRENGTH_UNKOUWN_STD = -20;
    public static final int NEW_NUM_SIGNAL_STRENGTH_BINS = 5;
    public static final int SIGNAL_STRENGTH_EXCELLENT = 5;
    private static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final int UMTS_ECIO_QCOM_MULTIPLE = 2;
    public static final int WCDMA_ECIO_NONE = 255;
    public static final int WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
    public static final int WCDMA_STRENGTH_NONE = 0;
    public static final int WCDMA_STRENGTH_UNKOUWN = 99;
    private static boolean isQualcomOrMtk = (!HuaweiTelephonyConfigs.isHisiPlatform());
    private static final Map<Integer, HwSignalStrength> mHwSigStrMap = new ArrayMap();
    private static HwSignalStrength mHwSignalStrength;
    private static final Object sLockObject = new Object();
    private String PROPERTY_SIGNAL_CUST_CDMA = "gsm.sigcust.cdma";
    private String PROPERTY_SIGNAL_CUST_CDMALTE = "gsm.sigcust.cdmalte";
    private String PROPERTY_SIGNAL_CUST_CONFIGURED = "gsm.sigcust.configured";
    private String PROPERTY_SIGNAL_CUST_EVDO = "gsm.sigcust.evdo";
    private String PROPERTY_SIGNAL_CUST_GSM = "gsm.sigcust.gsm";
    private String PROPERTY_SIGNAL_CUST_LTE = "gsm.sigcust.lte";
    private String PROPERTY_SIGNAL_CUST_NR = "gsm.sigcust.nr";
    private String PROPERTY_SIGNAL_CUST_UMTS = "gsm.sigcust.umts";
    private SignalThreshold mCdmaLteSignalThreshold;
    private SignalThreshold mCdmaSignalThreshold;
    private Context mContext;
    private SignalThreshold mEvdoSignalThreshold;
    private SignalThreshold mGsmSignalThreshold;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwSignalStrength.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int phoneId;
            if (intent != null && "android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction()) && HwSignalStrength.this.mPhoneId == (phoneId = intent.getIntExtra("phone", -1))) {
                RlogEx.i(HwSignalStrength.LOG_TAG, "ACTION_CARRIER_CONFIG_CHANGED  phoneId: " + phoneId);
                HwSignalStrength.this.loadAllCustInfo();
            }
        }
    };
    private SignalThreshold mLteSignalThreshold;
    private SignalThreshold mNrSignalThreshold;
    private int mPhoneId;
    private boolean mSigCustConfigured = false;
    private SignalThreshold mUmtsSignalThreshold;

    public enum SignalType {
        UNKNOWN,
        GSM,
        UMTS,
        CDMA,
        EVDO,
        LTE,
        CDMALTE,
        NR
    }

    private HwSignalStrength(int phoneId, Context context) {
        this.mPhoneId = phoneId;
        this.mContext = context;
        IntentFilter filter = new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED");
        Context context2 = this.mContext;
        if (context2 != null) {
            context2.registerReceiver(this.mIntentReceiver, filter);
        }
        this.mGsmSignalThreshold = new SignalThreshold(SignalType.GSM);
        this.mUmtsSignalThreshold = new SignalThreshold(SignalType.UMTS);
        this.mCdmaSignalThreshold = new SignalThreshold(SignalType.CDMA);
        this.mEvdoSignalThreshold = new SignalThreshold(SignalType.EVDO);
        this.mLteSignalThreshold = new SignalThreshold(SignalType.LTE);
        this.mCdmaLteSignalThreshold = new SignalThreshold(SignalType.CDMALTE);
        this.mNrSignalThreshold = new SignalThreshold(SignalType.NR);
        updateSigCustInfoFromXML(this.mContext);
    }

    public static HwSignalStrength getInstance(int phoneId, Context context) {
        HwSignalStrength hwSigStr;
        if (!isValidPhoneId(phoneId)) {
            return null;
        }
        synchronized (sLockObject) {
            hwSigStr = mHwSigStrMap.get(Integer.valueOf(phoneId));
            if (hwSigStr == null) {
                hwSigStr = new HwSignalStrength(phoneId, context);
                mHwSigStrMap.put(Integer.valueOf(phoneId), hwSigStr);
                RlogEx.i(LOG_TAG, "mHwSigStrMap init subId: " + phoneId);
            }
        }
        return hwSigStr;
    }

    private static String getSigCustString(Context context) {
        try {
            return Settings.System.getString(context.getContentResolver(), "cust_signal_thresholds");
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    public static boolean isRssiValid(SignalType type, int rssi) {
        switch (AnonymousClass2.$SwitchMap$com$android$internal$telephony$HwSignalStrength$SignalType[type.ordinal()]) {
            case 1:
                if (rssi == 0 || rssi == 99 || rssi > 0 || -1 == rssi) {
                    return false;
                }
                return true;
            case 2:
                if (rssi == 0 || rssi == Integer.MAX_VALUE || rssi == 99 || rssi > 0 || -1 == rssi) {
                    return false;
                }
                return true;
            case 3:
            case 4:
                if (rssi > 0 || -1 == rssi) {
                    return false;
                }
                return true;
            case 5:
            case 6:
            case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                if (Integer.MAX_VALUE == rssi || rssi > -20) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    public static boolean isEcioValid(SignalType type, int ecio) {
        switch (AnonymousClass2.$SwitchMap$com$android$internal$telephony$HwSignalStrength$SignalType[type.ordinal()]) {
            case 1:
                if (255 == ecio) {
                    return false;
                }
                return true;
            case 2:
                if (ecio == 255 || ecio == Integer.MAX_VALUE || ecio == 255) {
                    return false;
                }
                return true;
            case 3:
            case 4:
                if (255 == ecio) {
                    return false;
                }
                return true;
            case 5:
            case 6:
            case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                if (Integer.MAX_VALUE == ecio || ecio > 99) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    public static boolean isEcioValidForQCom(SignalType type, int ecio) {
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$HwSignalStrength$SignalType[type.ordinal()];
        if (i == 1 || i == 2 || i == 3 || i == 4) {
            if (255 == ecio) {
                return false;
            }
        } else if ((i != 6 && i != 7) || Integer.MAX_VALUE == ecio) {
            return false;
        }
        return true;
    }

    private static boolean isValidPhoneId(int phoneId) {
        return phoneId >= 0 && phoneId <= SIM_NUM;
    }

    private void logi(String string) {
        RlogEx.i(LOG_TAG, string);
    }

    private void loge(String string) {
        RlogEx.e(LOG_TAG, string);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadAllCustInfo() {
        loadCustInfo(SignalType.GSM);
        loadCustInfo(SignalType.UMTS);
        loadCustInfo(SignalType.CDMA);
        loadCustInfo(SignalType.EVDO);
        loadCustInfo(SignalType.LTE);
        loadCustInfo(SignalType.CDMALTE);
        loadCustInfo(SignalType.NR);
    }

    private void loadCustInfo(SignalType signalType) {
        String configItem = BuildConfig.FLAVOR;
        String defaultValue = BuildConfig.FLAVOR;
        SignalThreshold signalThreshold = getSignalThreshold(signalType);
        if (signalThreshold == null) {
            signalThreshold = new SignalThreshold(signalType);
        }
        switch (AnonymousClass2.$SwitchMap$com$android$internal$telephony$HwSignalStrength$SignalType[signalType.ordinal()]) {
            case 1:
                configItem = SystemPropertiesEx.get(this.PROPERTY_SIGNAL_CUST_GSM, BuildConfig.FLAVOR);
                defaultValue = DEFAULT_SIGNAL_CUST_GSM;
                break;
            case 2:
                configItem = SystemPropertiesEx.get(this.PROPERTY_SIGNAL_CUST_UMTS, BuildConfig.FLAVOR);
                defaultValue = DEFAULT_SIGNAL_CUST_UMTS;
                break;
            case 3:
                configItem = SystemPropertiesEx.get(this.PROPERTY_SIGNAL_CUST_CDMA, BuildConfig.FLAVOR);
                defaultValue = "5,false,-112,-106,-99,-92,-85";
                break;
            case 4:
                configItem = SystemPropertiesEx.get(this.PROPERTY_SIGNAL_CUST_EVDO, BuildConfig.FLAVOR);
                defaultValue = "5,false,-112,-106,-99,-92,-85";
                break;
            case 5:
                configItem = SystemPropertiesEx.get(this.PROPERTY_SIGNAL_CUST_NR, BuildConfig.FLAVOR);
                defaultValue = "5,false,-120,-115,-110,-105,-97";
                break;
            case 6:
                configItem = (String) HwCfgFilePolicy.getValue(KEY_SIGNAL_STRENGTH_STRING, this.mPhoneId, String.class);
                if (TextUtils.isEmpty(configItem)) {
                    configItem = SystemPropertiesEx.get(this.PROPERTY_SIGNAL_CUST_LTE, BuildConfig.FLAVOR);
                }
                RlogEx.i(LOG_TAG, "configItem: " + configItem + "  mPhoneId: " + this.mPhoneId);
                defaultValue = "5,false,-120,-115,-110,-105,-97";
                break;
            case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                configItem = SystemPropertiesEx.get(this.PROPERTY_SIGNAL_CUST_CDMALTE, BuildConfig.FLAVOR);
                defaultValue = "5,false,-120,-115,-110,-105,-97";
                break;
            default:
                loge("invalid signalType :" + signalType);
                break;
        }
        if (isCustConfigValid(configItem)) {
            signalThreshold.loadConfigItem(configItem);
            signalThreshold.mConfigured = true;
            logi("loadCustInfo from hw_defaults");
        } else if (!defaultValue.isEmpty()) {
            signalThreshold.loadConfigItem(defaultValue);
            signalThreshold.mConfigured = true;
        } else {
            signalThreshold.mConfigured = false;
            loge("Error! Didn't get any cust config!!");
        }
    }

    private void updateSigCustInfoFromXML(Context context) {
        updateCustInfo(context);
        loadAllCustInfo();
        SystemPropertiesEx.set(this.PROPERTY_SIGNAL_CUST_CONFIGURED, "true");
        this.mSigCustConfigured = true;
        logi("updateSigCustInfoFromXML finish, set gsm.sigcust.configured true");
    }

    private void updateCustInfo(Context context) {
        logi("updateCustInfo start  subId: " + this.mPhoneId);
        String sig_cust_strings = getSigCustString(context);
        if (sig_cust_strings == null || BuildConfig.FLAVOR.equals(sig_cust_strings)) {
            logi("no cust_signal_thresholds found");
            return;
        }
        String[] network_type_sig_custs = sig_cust_strings.split(";");
        logi("cust_signal_thresholds : " + sig_cust_strings);
        if (network_type_sig_custs != null) {
            for (String configstr : network_type_sig_custs) {
                setCustConfigFromString(configstr);
            }
        }
    }

    private void setCustConfigFromString(String configstr) {
        if (configstr == null) {
            return;
        }
        if (!configstr.contains(",")) {
            logi("configstr format is wrong,cannot analyze!");
            return;
        }
        int pos = configstr.indexOf(",");
        String configType = configstr.substring(0, pos);
        String configInfo = configstr.substring(pos + 1, configstr.length());
        switch (typeString2Enum(configType)) {
            case GSM:
                SystemPropertiesEx.set(this.PROPERTY_SIGNAL_CUST_GSM, configInfo);
                logi("set gsm sig cust to " + configInfo);
                return;
            case UMTS:
                SystemPropertiesEx.set(this.PROPERTY_SIGNAL_CUST_UMTS, configInfo);
                logi("set umts sig cust to " + configInfo);
                return;
            case CDMA:
                SystemPropertiesEx.set(this.PROPERTY_SIGNAL_CUST_CDMA, configInfo);
                logi("set cdma sig cust to " + configInfo);
                return;
            case EVDO:
                SystemPropertiesEx.set(this.PROPERTY_SIGNAL_CUST_EVDO, configInfo);
                logi("set evdo sig cust to " + configInfo);
                return;
            case NR:
                SystemPropertiesEx.set(this.PROPERTY_SIGNAL_CUST_NR, configInfo);
                logi("set nr sig cust to " + configInfo);
                return;
            case LTE:
                SystemPropertiesEx.set(this.PROPERTY_SIGNAL_CUST_LTE, configInfo);
                logi("set lte sig cust to " + configInfo);
                return;
            default:
                logi("invalid sig cust " + configInfo);
                return;
        }
    }

    private SignalType typeString2Enum(String typeStr) {
        if (typeStr == null || BuildConfig.FLAVOR.equals(typeStr)) {
            return SignalType.UNKNOWN;
        }
        if (typeStr.equals("gsm")) {
            return SignalType.GSM;
        }
        if (typeStr.equals("umts")) {
            return SignalType.UMTS;
        }
        if (typeStr.equals("lte")) {
            return SignalType.LTE;
        }
        if (typeStr.equals("cdma")) {
            return SignalType.CDMA;
        }
        if (typeStr.equals("evdo")) {
            return SignalType.EVDO;
        }
        if (typeStr.equals("nr")) {
            return SignalType.NR;
        }
        return SignalType.UNKNOWN;
    }

    private boolean isCustConfigValid(String custInfo) {
        logi("updateCustInfo start  subId: " + this.mPhoneId);
        if (TextUtils.isEmpty(custInfo)) {
            return false;
        }
        String[] config = custInfo.split(",");
        if (config.length < 6) {
            loge("Cust config length Error!!:" + config.length);
            return false;
        } else if ("4".equals(config[0]) || "5".equals(config[0])) {
            if ("true".equals(config[1])) {
                if (("4".equals(config[0]) && config.length != 10) || ("5".equals(config[0]) && config.length != 12)) {
                    loge("Length of config Error!!:" + config.length);
                    return false;
                }
            } else if (("4".equals(config[0]) && config.length != 6) || ("5".equals(config[0]) && config.length != 7)) {
                loge("Length of config Error!!:" + config.length);
                return false;
            }
            logi("Cust config is valid!");
            return true;
        } else {
            loge("Num of bins Error!!:" + config[0]);
            return false;
        }
    }

    public SignalThreshold getSignalThreshold(SignalType type) {
        if (type == null) {
            return null;
        }
        switch (AnonymousClass2.$SwitchMap$com$android$internal$telephony$HwSignalStrength$SignalType[type.ordinal()]) {
            case 1:
                return this.mGsmSignalThreshold;
            case 2:
                return this.mUmtsSignalThreshold;
            case 3:
                return this.mCdmaSignalThreshold;
            case 4:
                return this.mEvdoSignalThreshold;
            case 5:
                return this.mNrSignalThreshold;
            case 6:
                return this.mLteSignalThreshold;
            case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                return this.mCdmaLteSignalThreshold;
            default:
                return null;
        }
    }

    public int getLevel(SignalType type, int rssi, int ecio) {
        if (!this.mSigCustConfigured && SystemPropertiesEx.getBoolean(this.PROPERTY_SIGNAL_CUST_CONFIGURED, false)) {
            loadAllCustInfo();
            this.mSigCustConfigured = true;
        }
        SignalThreshold signalThreshold = getSignalThreshold(type);
        if (signalThreshold == null || !signalThreshold.isConfigured()) {
            return -1;
        }
        return signalThreshold.getSignalLevel(rssi, ecio);
    }

    public class SignalThreshold {
        private boolean mConfigured;
        private int mNumOfBins;
        private int mThresholdEcioExcellent;
        private int mThresholdEcioGood;
        private int mThresholdEcioGreat;
        private int mThresholdEcioModerate;
        private int mThresholdEcioPoor;
        private int mThresholdRssiExcellent;
        private int mThresholdRssiGood;
        private int mThresholdRssiGreat;
        private int mThresholdRssiModerate;
        private int mThresholdRssiPoor;
        private SignalType mType;
        private boolean mUseEcio;

        private SignalThreshold(SignalType mSignalType) {
            this.mType = mSignalType;
        }

        public SignalType getSignalType() {
            return this.mType;
        }

        public int getNumofBins() {
            return this.mNumOfBins;
        }

        public boolean isConfigured() {
            return this.mConfigured;
        }

        public void loadConfigItem(String custInfo) {
            int mLeciomulti;
            int mUeciomulti;
            if (custInfo != null) {
                String[] config = custInfo.split(",");
                try {
                    this.mNumOfBins = Integer.parseInt(config[0]);
                    this.mUseEcio = "true".equals(config[1]);
                    this.mThresholdRssiPoor = Integer.parseInt(config[2]);
                    this.mThresholdRssiModerate = Integer.parseInt(config[3]);
                    this.mThresholdRssiGood = Integer.parseInt(config[4]);
                    this.mThresholdRssiGreat = Integer.parseInt(config[5]);
                    if (5 == this.mNumOfBins) {
                        this.mThresholdRssiExcellent = Integer.parseInt(config[6]);
                    }
                } catch (NumberFormatException e) {
                    RlogEx.e(HwSignalStrength.LOG_TAG, "SignalThreshold NumberFormatException init");
                }
                if (this.mUseEcio) {
                    if (HwSignalStrength.isQualcomOrMtk) {
                        mUeciomulti = 2;
                        mLeciomulti = 10;
                    } else {
                        mUeciomulti = 1;
                        mLeciomulti = 1;
                    }
                    switch (AnonymousClass2.$SwitchMap$com$android$internal$telephony$HwSignalStrength$SignalType[this.mType.ordinal()]) {
                        case 2:
                            try {
                                this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]) * mUeciomulti;
                                this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]) * mUeciomulti;
                                this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]) * mUeciomulti;
                                this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]) * mUeciomulti;
                                if (5 == this.mNumOfBins) {
                                    this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]) * mUeciomulti;
                                    return;
                                }
                                return;
                            } catch (NumberFormatException e2) {
                                RlogEx.e(HwSignalStrength.LOG_TAG, "SignalThreshold NumberFormatException UMTS");
                                return;
                            }
                        case 3:
                            try {
                                this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]) * mUeciomulti;
                                this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]) * mUeciomulti;
                                this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]) * mUeciomulti;
                                this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]) * mUeciomulti;
                                if (5 == this.mNumOfBins) {
                                    this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]) * mUeciomulti;
                                    return;
                                }
                                return;
                            } catch (NumberFormatException e3) {
                                RlogEx.e(HwSignalStrength.LOG_TAG, "SignalThreshold NumberFormatException CDMA");
                                return;
                            }
                        case 4:
                            try {
                                this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]);
                                this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]);
                                this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]);
                                this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]);
                                if (5 == this.mNumOfBins) {
                                    this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]);
                                    return;
                                }
                                return;
                            } catch (NumberFormatException e4) {
                                RlogEx.e(HwSignalStrength.LOG_TAG, "SignalThreshold NumberFormatException EVDO");
                                return;
                            }
                        case 5:
                        case 6:
                        case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                            try {
                                this.mThresholdEcioPoor = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 6) - 4]).floatValue());
                                this.mThresholdEcioModerate = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 7) - 4]).floatValue());
                                this.mThresholdEcioGood = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 8) - 4]).floatValue());
                                this.mThresholdEcioGreat = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 9) - 4]).floatValue());
                                if (5 == this.mNumOfBins) {
                                    this.mThresholdEcioExcellent = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 10) - 4]).floatValue());
                                }
                                RlogEx.i(HwSignalStrength.LOG_TAG, "mThresholdEcioPoor" + this.mThresholdEcioPoor + "mThresholdEcioModerate" + this.mThresholdEcioModerate);
                                return;
                            } catch (NumberFormatException e5) {
                                RlogEx.e(HwSignalStrength.LOG_TAG, "SignalThreshold NumberFormatException NR");
                                return;
                            }
                        default:
                            this.mUseEcio = false;
                            return;
                    }
                }
            }
        }

        public int getSignalLevel(int rssi, int ecio) {
            int rssiLevel;
            boolean ecioValid;
            int ecioLevel;
            if (rssi < this.mThresholdRssiPoor || !HwSignalStrength.isRssiValid(this.mType, rssi)) {
                rssiLevel = 0;
            } else if (rssi < this.mThresholdRssiModerate) {
                rssiLevel = 1;
            } else if (rssi < this.mThresholdRssiGood) {
                rssiLevel = 2;
            } else if (rssi < this.mThresholdRssiGreat) {
                rssiLevel = 3;
            } else {
                int rssiLevel2 = this.mNumOfBins;
                if (4 == rssiLevel2 || (5 == rssiLevel2 && rssi < this.mThresholdRssiExcellent)) {
                    rssiLevel = 4;
                } else {
                    rssiLevel = 5;
                }
            }
            if (!this.mUseEcio) {
                return rssiLevel;
            }
            if (HwSignalStrength.isQualcomOrMtk) {
                ecioValid = HwSignalStrength.isEcioValidForQCom(this.mType, ecio);
            } else {
                ecioValid = HwSignalStrength.isEcioValid(this.mType, ecio);
            }
            if (!ecioValid) {
                ecioLevel = 0;
            } else if (ecio < this.mThresholdEcioPoor) {
                ecioLevel = 0;
            } else if (ecio < this.mThresholdEcioModerate) {
                ecioLevel = 1;
            } else if (ecio < this.mThresholdEcioGood) {
                ecioLevel = 2;
            } else if (ecio < this.mThresholdEcioGreat) {
                ecioLevel = 3;
            } else {
                int i = this.mNumOfBins;
                if (4 == i || (5 == i && ecio < this.mThresholdEcioExcellent)) {
                    ecioLevel = 4;
                } else {
                    ecioLevel = 5;
                }
            }
            return rssiLevel >= ecioLevel ? ecioLevel : rssiLevel;
        }

        public int getHighThresholdBySignalLevel(int level, boolean isEcio) {
            if (!isEcio) {
                if (level == 0) {
                    return this.mThresholdRssiPoor - 1;
                }
                if (level == 1) {
                    return this.mThresholdRssiModerate - 1;
                }
                if (level == 2) {
                    return this.mThresholdRssiGood - 1;
                }
                if (level == 3) {
                    return this.mThresholdRssiGreat - 1;
                }
                if (level != 4) {
                    RlogEx.i(HwSignalStrength.LOG_TAG, "use default rssi high threshold");
                    return -1;
                } else if (5 == this.mNumOfBins) {
                    return this.mThresholdRssiExcellent - 1;
                } else {
                    return -1;
                }
            } else if (!this.mUseEcio) {
                RlogEx.i(HwSignalStrength.LOG_TAG, "not use ecio");
                return -1;
            } else if (level == 0) {
                return this.mThresholdEcioPoor - 1;
            } else {
                if (level == 1) {
                    return this.mThresholdEcioModerate - 1;
                }
                if (level == 2) {
                    return this.mThresholdEcioGood - 1;
                }
                if (level == 3) {
                    return this.mThresholdEcioGreat - 1;
                }
                if (level != 4) {
                    RlogEx.i(HwSignalStrength.LOG_TAG, "use default ecio high threshold");
                    return -1;
                } else if (5 == this.mNumOfBins) {
                    return this.mThresholdEcioExcellent - 1;
                } else {
                    return -1;
                }
            }
        }
    }
}
