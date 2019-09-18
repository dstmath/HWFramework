package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
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
    public static final int MAX_ASU = 31;
    public static final int NEW_NUM_SIGNAL_STRENGTH_BINS = 5;
    public static final int SIGNAL_STRENGTH_EXCELLENT = 5;
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final int UMTS_ECIO_QCOM_MULTIPLE = 2;
    public static final int WCDMA_ECIO_NONE = 255;
    public static final int WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
    public static final int WCDMA_STRENGTH_NONE = 0;
    public static final int WCDMA_STRENGTH_UNKOUWN = 99;
    /* access modifiers changed from: private */
    public static boolean isQualcomOrMtk = (!HuaweiTelephonyConfigs.isHisiPlatform());
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
        public void onReceive(Context context, Intent intent) {
            if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(intent.getAction())) {
                int phoneId = intent.getIntExtra("phone", -1);
                if (HwSignalStrength.this.mPhoneId == phoneId) {
                    Rlog.d(HwSignalStrength.LOG_TAG, "ACTION_CARRIER_CONFIG_CHANGED  phoneId: " + phoneId);
                    HwSignalStrength.this.loadAllCustInfo();
                }
            }
        }
    };
    private SignalThreshold mLteSignalThreshold;
    private SignalThreshold mNrSignalThreshold;
    private Phone mPhone;
    /* access modifiers changed from: private */
    public int mPhoneId;
    private boolean mSigCustConfigured = false;
    private SignalThreshold mUmtsSignalThreshold;

    public class SignalThreshold {
        /* access modifiers changed from: private */
        public boolean mConfigured;
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

        private SignalThreshold(Phone phone, SignalType mSignalType) {
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
            String[] config = custInfo.split(",");
            this.mNumOfBins = Integer.parseInt(config[0]);
            int mCeciomulti = 1;
            this.mUseEcio = "true".equals(config[1]);
            this.mThresholdRssiPoor = Integer.parseInt(config[2]);
            this.mThresholdRssiModerate = Integer.parseInt(config[3]);
            this.mThresholdRssiGood = Integer.parseInt(config[4]);
            this.mThresholdRssiGreat = Integer.parseInt(config[5]);
            if (5 == this.mNumOfBins) {
                this.mThresholdRssiExcellent = Integer.parseInt(config[6]);
            }
            if (this.mUseEcio) {
                if (HwSignalStrength.isQualcomOrMtk) {
                    mCeciomulti = 10;
                    mLeciomulti = 2;
                } else {
                    mLeciomulti = 1;
                }
                switch (this.mType) {
                    case UMTS:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(6 + this.mNumOfBins) - 4]) * mLeciomulti;
                        this.mThresholdEcioModerate = Integer.parseInt(config[(7 + this.mNumOfBins) - 4]) * mLeciomulti;
                        this.mThresholdEcioGood = Integer.parseInt(config[(8 + this.mNumOfBins) - 4]) * mLeciomulti;
                        this.mThresholdEcioGreat = Integer.parseInt(config[(9 + this.mNumOfBins) - 4]) * mLeciomulti;
                        if (5 == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(10 + this.mNumOfBins) - 4]) * mLeciomulti;
                            return;
                        }
                        return;
                    case CDMA:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(6 + this.mNumOfBins) - 4]) * mLeciomulti;
                        this.mThresholdEcioModerate = Integer.parseInt(config[(7 + this.mNumOfBins) - 4]) * mLeciomulti;
                        this.mThresholdEcioGood = Integer.parseInt(config[(8 + this.mNumOfBins) - 4]) * mLeciomulti;
                        this.mThresholdEcioGreat = Integer.parseInt(config[(9 + this.mNumOfBins) - 4]) * mLeciomulti;
                        if (5 == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(10 + this.mNumOfBins) - 4]) * mLeciomulti;
                            return;
                        }
                        return;
                    case EVDO:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(6 + this.mNumOfBins) - 4]);
                        this.mThresholdEcioModerate = Integer.parseInt(config[(7 + this.mNumOfBins) - 4]);
                        this.mThresholdEcioGood = Integer.parseInt(config[(8 + this.mNumOfBins) - 4]);
                        this.mThresholdEcioGreat = Integer.parseInt(config[(9 + this.mNumOfBins) - 4]);
                        if (5 == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(10 + this.mNumOfBins) - 4]);
                            return;
                        }
                        return;
                    case LTE:
                    case CDMALTE:
                    case NR:
                        this.mThresholdEcioPoor = (int) (((float) mCeciomulti) * Float.valueOf(config[(6 + this.mNumOfBins) - 4]).floatValue());
                        this.mThresholdEcioModerate = (int) (((float) mCeciomulti) * Float.valueOf(config[(7 + this.mNumOfBins) - 4]).floatValue());
                        this.mThresholdEcioGood = (int) (((float) mCeciomulti) * Float.valueOf(config[(8 + this.mNumOfBins) - 4]).floatValue());
                        this.mThresholdEcioGreat = (int) (((float) mCeciomulti) * Float.valueOf(config[(9 + this.mNumOfBins) - 4]).floatValue());
                        if (5 == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = (int) (((float) mCeciomulti) * Float.valueOf(config[(10 + this.mNumOfBins) - 4]).floatValue());
                        }
                        Rlog.d(HwSignalStrength.LOG_TAG, "mThresholdEcioPoor" + this.mThresholdEcioPoor + "mThresholdEcioModerate" + this.mThresholdEcioModerate);
                        return;
                    default:
                        this.mUseEcio = false;
                        return;
                }
            }
        }

        public int getSignalLevel(int rssi, int ecio) {
            int rssiLevel;
            int level;
            boolean ecioValid;
            int ecioLevel = 5;
            if (rssi < this.mThresholdRssiPoor || !HwSignalStrength.isRssiValid(this.mType, rssi)) {
                rssiLevel = 0;
            } else if (rssi < this.mThresholdRssiModerate) {
                rssiLevel = 1;
            } else if (rssi < this.mThresholdRssiGood) {
                rssiLevel = 2;
            } else if (rssi < this.mThresholdRssiGreat) {
                rssiLevel = 3;
            } else if (4 == this.mNumOfBins || (5 == this.mNumOfBins && rssi < this.mThresholdRssiExcellent)) {
                rssiLevel = 4;
            } else {
                rssiLevel = 5;
            }
            if (this.mUseEcio) {
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
                } else if (4 == this.mNumOfBins || (5 == this.mNumOfBins && ecio < this.mThresholdEcioExcellent)) {
                    ecioLevel = 4;
                }
                int ecioLevel2 = ecioLevel;
                level = rssiLevel >= ecioLevel2 ? ecioLevel2 : rssiLevel;
            } else {
                level = rssiLevel;
            }
            return level;
        }

        public int getHighThresholdBySignalLevel(int level, boolean isEcio) {
            if (!isEcio) {
                switch (level) {
                    case 0:
                        return this.mThresholdRssiPoor - 1;
                    case 1:
                        return this.mThresholdRssiModerate - 1;
                    case 2:
                        return this.mThresholdRssiGood - 1;
                    case 3:
                        return this.mThresholdRssiGreat - 1;
                    case 4:
                        if (5 == this.mNumOfBins) {
                            return this.mThresholdRssiExcellent - 1;
                        }
                        return -1;
                    default:
                        Rlog.d(HwSignalStrength.LOG_TAG, "use default rssi high threshold");
                        return -1;
                }
            } else if (this.mUseEcio) {
                switch (level) {
                    case 0:
                        return this.mThresholdEcioPoor - 1;
                    case 1:
                        return this.mThresholdEcioModerate - 1;
                    case 2:
                        return this.mThresholdEcioGood - 1;
                    case 3:
                        return this.mThresholdEcioGreat - 1;
                    case 4:
                        if (5 == this.mNumOfBins) {
                            return this.mThresholdEcioExcellent - 1;
                        }
                        return -1;
                    default:
                        Rlog.d(HwSignalStrength.LOG_TAG, "use default ecio high threshold");
                        return -1;
                }
            } else {
                Rlog.d(HwSignalStrength.LOG_TAG, "not use ecio");
                return -1;
            }
        }
    }

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

    private void logi(String string) {
        Rlog.i(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }

    public HwSignalStrength(Phone phone, Context context) {
        this.mPhone = phone;
        this.mPhoneId = phone.getPhoneId();
        this.mContext = context;
        IntentFilter filter = new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED");
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.mIntentReceiver, filter);
        }
        this.mGsmSignalThreshold = new SignalThreshold(this.mPhone, SignalType.GSM);
        this.mUmtsSignalThreshold = new SignalThreshold(this.mPhone, SignalType.UMTS);
        this.mCdmaSignalThreshold = new SignalThreshold(this.mPhone, SignalType.CDMA);
        this.mEvdoSignalThreshold = new SignalThreshold(this.mPhone, SignalType.EVDO);
        this.mLteSignalThreshold = new SignalThreshold(this.mPhone, SignalType.LTE);
        this.mCdmaLteSignalThreshold = new SignalThreshold(this.mPhone, SignalType.CDMALTE);
        this.mNrSignalThreshold = new SignalThreshold(this.mPhone, SignalType.NR);
        updateSigCustInfoFromXML(this.mContext);
    }

    /* access modifiers changed from: private */
    public void loadAllCustInfo() {
        loadCustInfo(SignalType.GSM);
        loadCustInfo(SignalType.UMTS);
        loadCustInfo(SignalType.CDMA);
        loadCustInfo(SignalType.EVDO);
        loadCustInfo(SignalType.LTE);
        loadCustInfo(SignalType.CDMALTE);
        loadCustInfo(SignalType.NR);
    }

    public static HwSignalStrength getInstance(Phone phone) {
        HwSignalStrength hwSigStr;
        if (phone == null) {
            return null;
        }
        int phoneId = phone.getPhoneId();
        if (!isValidPhoneId(phoneId)) {
            return null;
        }
        Context context = phone.getContext();
        synchronized (sLockObject) {
            hwSigStr = mHwSigStrMap.get(Integer.valueOf(phoneId));
            if (hwSigStr == null) {
                hwSigStr = new HwSignalStrength(phone, context);
                mHwSigStrMap.put(Integer.valueOf(phoneId), hwSigStr);
                Rlog.d(LOG_TAG, "mHwSigStrMap init subId: " + phoneId);
            }
        }
        return hwSigStr;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void loadCustInfo(SignalType signalType) {
        String configItem = "";
        String defaultValue = "";
        SignalThreshold signalThreshold = getSignalThreshold(signalType);
        if (signalThreshold == null) {
            signalThreshold = new SignalThreshold(this.mPhone, signalType);
        }
        switch (signalType) {
            case GSM:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_GSM, "");
                defaultValue = DEFAULT_SIGNAL_CUST_GSM;
                break;
            case UMTS:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_UMTS, "");
                defaultValue = DEFAULT_SIGNAL_CUST_UMTS;
                break;
            case CDMA:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_CDMA, "");
                defaultValue = "5,false,-112,-106,-99,-92,-85";
                break;
            case EVDO:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_EVDO, "");
                defaultValue = "5,false,-112,-106,-99,-92,-85";
                break;
            case LTE:
                configItem = HwCfgFilePolicy.getValue(KEY_SIGNAL_STRENGTH_STRING, this.mPhoneId, String.class);
                if (TextUtils.isEmpty(configItem)) {
                    configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_LTE, "");
                }
                Rlog.d(LOG_TAG, "configItem: " + configItem + "  subId: " + this.mPhoneId);
                defaultValue = "5,false,-120,-115,-110,-105,-97";
                break;
            case CDMALTE:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_CDMALTE, "");
                defaultValue = "5,false,-120,-115,-110,-105,-97";
                break;
            case NR:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_NR, "");
                defaultValue = "5,false,-120,-115,-110,-105,-97";
                break;
            default:
                loge("invalid signalType :" + signalType);
                break;
        }
        if (isCustConfigValid(configItem)) {
            signalThreshold.loadConfigItem(configItem);
            boolean unused = signalThreshold.mConfigured = true;
            logi("loadCustInfo from hw_defaults");
        } else if (!defaultValue.isEmpty()) {
            signalThreshold.loadConfigItem(defaultValue);
            boolean unused2 = signalThreshold.mConfigured = true;
        } else {
            boolean unused3 = signalThreshold.mConfigured = false;
            loge("Error! Didn't get any cust config!!");
        }
    }

    private void updateSigCustInfoFromXML(Context context) {
        updateCustInfo(context);
        loadAllCustInfo();
        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_CONFIGURED, "true");
        this.mSigCustConfigured = true;
        logi("updateSigCustInfoFromXML finish, set gsm.sigcust.configured true");
    }

    private void updateCustInfo(Context context) {
        logi("updateCustInfo start  subId: " + this.mPhoneId);
        String sig_cust_strings = getSigCustString(context);
        if (sig_cust_strings == null || "".equals(sig_cust_strings)) {
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

    private static String getSigCustString(Context context) {
        try {
            return Settings.System.getString(context.getContentResolver(), "cust_signal_thresholds");
        } catch (Exception e) {
            return "";
        }
    }

    private void setCustConfigFromString(String configstr) {
        if (configstr != null) {
            if (!configstr.contains(",")) {
                logi("configstr format is wrong,cannot analyze!");
                return;
            }
            int pos = configstr.indexOf(",");
            String configType = configstr.substring(0, pos);
            String configInfo = configstr.substring(pos + 1, configstr.length());
            int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$HwSignalStrength$SignalType[typeString2Enum(configType).ordinal()];
            if (i != 7) {
                switch (i) {
                    case 1:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_GSM, configInfo);
                        logi("set gsm sig cust to " + configInfo);
                        break;
                    case 2:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_UMTS, configInfo);
                        logi("set umts sig cust to " + configInfo);
                        break;
                    case 3:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_CDMA, configInfo);
                        logi("set cdma sig cust to " + configInfo);
                        break;
                    case 4:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_EVDO, configInfo);
                        logi("set evdo sig cust to " + configInfo);
                        break;
                    case 5:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_LTE, configInfo);
                        logi("set lte sig cust to " + configInfo);
                        break;
                    default:
                        logi("invalid sig cust " + configInfo);
                        break;
                }
            } else {
                SystemProperties.set(this.PROPERTY_SIGNAL_CUST_NR, configInfo);
                logi("set nr sig cust to " + configInfo);
            }
        }
    }

    private SignalType typeString2Enum(String typeStr) {
        if (typeStr == null || "".equals(typeStr)) {
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
        switch (type) {
            case GSM:
                return this.mGsmSignalThreshold;
            case UMTS:
                return this.mUmtsSignalThreshold;
            case CDMA:
                return this.mCdmaSignalThreshold;
            case EVDO:
                return this.mEvdoSignalThreshold;
            case LTE:
                return this.mLteSignalThreshold;
            case CDMALTE:
                return this.mCdmaLteSignalThreshold;
            case NR:
                return this.mNrSignalThreshold;
            default:
                return null;
        }
    }

    public int getLevel(SignalType type, int rssi, int ecio) {
        if (!this.mSigCustConfigured && SystemProperties.getBoolean(this.PROPERTY_SIGNAL_CUST_CONFIGURED, false)) {
            loadAllCustInfo();
            this.mSigCustConfigured = true;
        }
        SignalThreshold signalThreshold = getSignalThreshold(type);
        if (signalThreshold == null || !signalThreshold.isConfigured()) {
            return -1;
        }
        return signalThreshold.getSignalLevel(rssi, ecio);
    }

    public static boolean isRssiValid(SignalType type, int rssi) {
        switch (type) {
            case GSM:
                if (rssi == 0 || rssi == 99 || rssi > 0 || -1 == rssi) {
                    return false;
                }
            case UMTS:
                if (rssi == 0 || rssi == Integer.MAX_VALUE || rssi == 99 || rssi > 0 || -1 == rssi) {
                    return false;
                }
            case CDMA:
            case EVDO:
                if (rssi > 0 || -1 == rssi) {
                    return false;
                }
            case LTE:
            case CDMALTE:
            case NR:
                if (Integer.MAX_VALUE == rssi || rssi > -20) {
                    return false;
                }
            default:
                return false;
        }
        return true;
    }

    public static boolean isEcioValid(SignalType type, int ecio) {
        switch (type) {
            case GSM:
                if (255 == ecio) {
                    return false;
                }
                break;
            case UMTS:
                if (ecio == 255 || ecio == Integer.MAX_VALUE || ecio == 255) {
                    return false;
                }
            case CDMA:
            case EVDO:
                if (255 == ecio) {
                    return false;
                }
                break;
            case LTE:
            case CDMALTE:
            case NR:
                if (Integer.MAX_VALUE == ecio || ecio > 99) {
                    return false;
                }
            default:
                return false;
        }
        return true;
    }

    public static boolean isEcioValidForQCom(SignalType type, int ecio) {
        switch (type) {
            case GSM:
            case UMTS:
            case CDMA:
            case EVDO:
                if (255 == ecio) {
                    return false;
                }
                break;
            case LTE:
            case CDMALTE:
                if (Integer.MAX_VALUE == ecio) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private static boolean isValidPhoneId(int phoneId) {
        return phoneId >= 0 && phoneId <= SIM_NUM;
    }
}
