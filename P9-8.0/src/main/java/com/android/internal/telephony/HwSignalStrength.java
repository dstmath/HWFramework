package com.android.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.rms.iaware.AppTypeInfo;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import com.huawei.connectivitylog.ConnectivityLogManager;

public class HwSignalStrength {
    private static final /* synthetic */ int[] -com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues = null;
    private static final int CDMA_ECIO_QCOM_MULTIPLE = 10;
    public static final int DEFAULT_NUM_SIGNAL_STRENGTH_BINS = 4;
    protected static final String DEFAULT_SIGNAL_CUST_CDMA = "5,false,-112,-106,-99,-92,-85";
    protected static final String DEFAULT_SIGNAL_CUST_CDMALTE = "5,false,-120,-115,-110,-105,-97";
    protected static final String DEFAULT_SIGNAL_CUST_EVDO = "5,false,-112,-106,-99,-92,-85";
    protected static final String DEFAULT_SIGNAL_CUST_GSM = "5,false,-109,-103,-97,-91,-85";
    protected static final String DEFAULT_SIGNAL_CUST_LTE = "5,false,-120,-115,-110,-105,-97";
    protected static final String DEFAULT_SIGNAL_CUST_UMTS = "5,false,-112,-105,-99,-93,-87";
    private static final int ECIO_HISI_MULTIPLE = 1;
    public static final int GSM_STRENGTH_NONE = 0;
    public static final int GSM_STRENGTH_UNKOUWN = 99;
    public static final int INVALID_ECIO = 255;
    public static final int INVALID_RSSI = -1;
    private static final String LOG_TAG = "HwSignalStrength";
    private static final int LTE_RSRQ_QCOM_MULTIPLE = 10;
    public static final int LTE_RSSNR_UNKOUWN_STD = 99;
    public static final int LTE_STRENGTH_UNKOUWN_STD = -30;
    public static final int MAX_ASU = 31;
    public static final int NEW_NUM_SIGNAL_STRENGTH_BINS = 5;
    public static final int SIGNAL_STRENGTH_EXCELLENT = 5;
    private static final int UMTS_ECIO_QCOM_MULTIPLE = 2;
    public static final int WCDMA_ECIO_NONE = 255;
    public static final int WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
    public static final int WCDMA_STRENGTH_NONE = 0;
    public static final int WCDMA_STRENGTH_UNKOUWN = 99;
    private static boolean isQualcom = HwModemCapability.isCapabilitySupport(9);
    private static HwSignalStrength mHwSignalStrength;
    private String PROPERTY_SIGNAL_CUST_CDMA = "gsm.sigcust.cdma";
    private String PROPERTY_SIGNAL_CUST_CDMALTE = "gsm.sigcust.cdmalte";
    private String PROPERTY_SIGNAL_CUST_CONFIGURED = "gsm.sigcust.configured";
    private String PROPERTY_SIGNAL_CUST_EVDO = "gsm.sigcust.evdo";
    private String PROPERTY_SIGNAL_CUST_GSM = "gsm.sigcust.gsm";
    private String PROPERTY_SIGNAL_CUST_LTE = "gsm.sigcust.lte";
    private String PROPERTY_SIGNAL_CUST_UMTS = "gsm.sigcust.umts";
    private SignalThreshold mCdmaLteSignalThreshold = new SignalThreshold(SignalType.CDMALTE, null);
    private SignalThreshold mCdmaSignalThreshold = new SignalThreshold(SignalType.CDMA, null);
    private SignalThreshold mEvdoSignalThreshold = new SignalThreshold(SignalType.EVDO, null);
    private SignalThreshold mGsmSignalThreshold = new SignalThreshold(SignalType.GSM, null);
    private SignalThreshold mLteSignalThreshold = new SignalThreshold(SignalType.LTE, null);
    private boolean mSigCustConfigured = false;
    private SignalThreshold mUmtsSignalThreshold = new SignalThreshold(SignalType.UMTS, null);

    public static class SignalThreshold {
        private static final /* synthetic */ int[] -com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues = null;
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

        private static /* synthetic */ int[] -getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues() {
            if (-com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues != null) {
                return -com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues;
            }
            int[] iArr = new int[SignalType.values().length];
            try {
                iArr[SignalType.CDMA.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[SignalType.CDMALTE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[SignalType.EVDO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[SignalType.GSM.ordinal()] = 6;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[SignalType.LTE.ordinal()] = 4;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[SignalType.UMTS.ordinal()] = 5;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[SignalType.UNKNOWN.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            -com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues = iArr;
            return iArr;
        }

        /* synthetic */ SignalThreshold(SignalType mSignalType, SignalThreshold -this1) {
            this(mSignalType);
        }

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
            String[] config = custInfo.split(",");
            this.mNumOfBins = Integer.parseInt(config[0]);
            this.mUseEcio = "true".equals(config[1]);
            this.mThresholdRssiPoor = Integer.parseInt(config[2]);
            this.mThresholdRssiModerate = Integer.parseInt(config[3]);
            this.mThresholdRssiGood = Integer.parseInt(config[4]);
            this.mThresholdRssiGreat = Integer.parseInt(config[5]);
            if (5 == this.mNumOfBins) {
                this.mThresholdRssiExcellent = Integer.parseInt(config[6]);
            }
            if (this.mUseEcio) {
                int mUeciomulti;
                int mLeciomulti;
                if (HwSignalStrength.isQualcom) {
                    mUeciomulti = 2;
                    mLeciomulti = 10;
                } else {
                    mUeciomulti = 1;
                    mLeciomulti = 1;
                }
                switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[this.mType.ordinal()]) {
                    case 1:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]) * mUeciomulti;
                        this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]) * mUeciomulti;
                        this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]) * mUeciomulti;
                        this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]) * mUeciomulti;
                        if (5 == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]) * mUeciomulti;
                            return;
                        }
                        return;
                    case 2:
                    case 4:
                        this.mThresholdEcioPoor = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 6) - 4]).floatValue());
                        this.mThresholdEcioModerate = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 7) - 4]).floatValue());
                        this.mThresholdEcioGood = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 8) - 4]).floatValue());
                        this.mThresholdEcioGreat = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 9) - 4]).floatValue());
                        if (5 == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = (int) (((float) mLeciomulti) * Float.valueOf(config[(this.mNumOfBins + 10) - 4]).floatValue());
                        }
                        Rlog.d(HwSignalStrength.LOG_TAG, "mThresholdEcioPoor" + this.mThresholdEcioPoor + "mThresholdEcioModerate" + this.mThresholdEcioModerate);
                        return;
                    case 3:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]);
                        this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]);
                        this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]);
                        this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]);
                        if (5 == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]);
                            return;
                        }
                        return;
                    case 5:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]) * mUeciomulti;
                        this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]) * mUeciomulti;
                        this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]) * mUeciomulti;
                        this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]) * mUeciomulti;
                        if (5 == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]) * mUeciomulti;
                            return;
                        }
                        return;
                    default:
                        this.mUseEcio = false;
                        return;
                }
            }
        }

        public int getSignalLevel(int rssi, int ecio) {
            int rssiLevel;
            if (rssi < this.mThresholdRssiPoor || (HwSignalStrength.isRssiValid(this.mType, rssi) ^ 1) != 0) {
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
            if (!this.mUseEcio) {
                return rssiLevel;
            }
            boolean ecioValid;
            int ecioLevel;
            if (HwSignalStrength.isQualcom) {
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
            } else {
                ecioLevel = 5;
            }
            return rssiLevel >= ecioLevel ? ecioLevel : rssiLevel;
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
        CDMALTE
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues() {
        if (-com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues != null) {
            return -com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues;
        }
        int[] iArr = new int[SignalType.values().length];
        try {
            iArr[SignalType.CDMA.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SignalType.CDMALTE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SignalType.EVDO.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SignalType.GSM.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SignalType.LTE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SignalType.UMTS.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SignalType.UNKNOWN.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        -com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues = iArr;
        return iArr;
    }

    private void logi(String string) {
        Rlog.i(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }

    public HwSignalStrength() {
        Rlog.i(LOG_TAG, "create HwSignalStrength");
        loadAllCustInfo();
    }

    private void loadAllCustInfo() {
        loadCustInfo(SignalType.GSM);
        loadCustInfo(SignalType.UMTS);
        loadCustInfo(SignalType.CDMA);
        loadCustInfo(SignalType.EVDO);
        loadCustInfo(SignalType.LTE);
        loadCustInfo(SignalType.CDMALTE);
    }

    public static HwSignalStrength getInstance() {
        if (mHwSignalStrength == null) {
            mHwSignalStrength = new HwSignalStrength();
        }
        return mHwSignalStrength;
    }

    private void loadCustInfo(SignalType signalType) {
        String configItem = "";
        String defaultValue = "";
        SignalThreshold signalThreshold = getSignalThreshold(signalType);
        if (signalThreshold == null) {
            signalThreshold = new SignalThreshold(signalType, null);
        }
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[signalType.ordinal()]) {
            case 1:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_CDMA, "");
                defaultValue = "5,false,-112,-106,-99,-92,-85";
                break;
            case 2:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_CDMALTE, "");
                defaultValue = "5,false,-120,-115,-110,-105,-97";
                break;
            case 3:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_EVDO, "");
                defaultValue = "5,false,-112,-106,-99,-92,-85";
                break;
            case 4:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_GSM, "");
                defaultValue = DEFAULT_SIGNAL_CUST_GSM;
                break;
            case 5:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_LTE, "");
                defaultValue = "5,false,-120,-115,-110,-105,-97";
                break;
            case 6:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_UMTS, "");
                defaultValue = DEFAULT_SIGNAL_CUST_UMTS;
                break;
            default:
                loge("invalid signalType :" + signalType);
                break;
        }
        if (isCustConfigValid(configItem)) {
            signalThreshold.loadConfigItem(configItem);
            signalThreshold.mConfigured = true;
            logi("loadCustInfo from hw_defaults");
        } else if (defaultValue.isEmpty()) {
            signalThreshold.mConfigured = false;
            loge("Error! Didn't get any cust config!!");
        } else {
            signalThreshold.loadConfigItem(defaultValue);
            signalThreshold.mConfigured = true;
        }
    }

    public void updateSigCustInfoFromXML(Context context) {
        updateCustInfo(context);
        loadAllCustInfo();
        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_CONFIGURED, "true");
        this.mSigCustConfigured = true;
        logi("updateSigCustInfoFromXML finish, set gsm.sigcust.configured true");
    }

    private void updateCustInfo(Context context) {
        logi("updateCustInfo start");
        String sig_cust_strings = getSigCustString(context);
        if (sig_cust_strings == null || ("".equals(sig_cust_strings) ^ 1) == 0) {
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
        String custStr = "";
        try {
            return System.getString(context.getContentResolver(), "cust_signal_thresholds");
        } catch (Exception e) {
            return custStr;
        }
    }

    private void setCustConfigFromString(String configstr) {
        if (configstr != null) {
            if (configstr.contains(",")) {
                int pos = configstr.indexOf(",");
                String configType = configstr.substring(0, pos);
                String configInfo = configstr.substring(pos + 1, configstr.length());
                switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[typeString2Enum(configType).ordinal()]) {
                    case 1:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_CDMA, configInfo);
                        logi("set cdma sig cust to " + configInfo);
                        break;
                    case 3:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_EVDO, configInfo);
                        logi("set evdo sig cust to " + configInfo);
                        break;
                    case 4:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_GSM, configInfo);
                        logi("set gsm sig cust to " + configInfo);
                        break;
                    case 5:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_LTE, configInfo);
                        logi("set lte sig cust to " + configInfo);
                        break;
                    case 6:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_UMTS, configInfo);
                        logi("set umts sig cust to " + configInfo);
                        break;
                    default:
                        logi("invalid sig cust " + configInfo);
                        break;
                }
            }
            logi("configstr format is wrong,cannot analyze!");
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
        return SignalType.UNKNOWN;
    }

    private boolean isCustConfigValid(String custInfo) {
        logi("custInfo :" + custInfo);
        if (TextUtils.isEmpty(custInfo)) {
            return false;
        }
        String[] config = custInfo.split(",");
        if (config.length < 6) {
            loge("Cust config length Error!!:" + config.length);
            return false;
        } else if ("4".equals(config[0]) || ("5".equals(config[0]) ^ 1) == 0) {
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
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                return this.mCdmaSignalThreshold;
            case 2:
                return this.mCdmaLteSignalThreshold;
            case 3:
                return this.mEvdoSignalThreshold;
            case 4:
                return this.mGsmSignalThreshold;
            case 5:
                return this.mLteSignalThreshold;
            case 6:
                return this.mUmtsSignalThreshold;
            default:
                return null;
        }
    }

    public void validateInput(SignalStrength newSignalStrength) {
        int wcdmaRscp;
        int i = WCDMA_STRENGTH_INVALID;
        int i2 = 255;
        int i3 = -1;
        Rlog.d(LOG_TAG, "Signal before HW validate=" + newSignalStrength);
        newSignalStrength.setGsmSignalStrength(newSignalStrength.getGsmSignalStrength() > 0 ? newSignalStrength.getGsmSignalStrength() * -1 : -1);
        if (newSignalStrength.getWcdmaRscp() > 0) {
            wcdmaRscp = newSignalStrength.getWcdmaRscp() * -1;
        } else {
            wcdmaRscp = -1;
        }
        newSignalStrength.setWcdmaRscp(wcdmaRscp);
        if (newSignalStrength.getWcdmaEcio() >= 0) {
            wcdmaRscp = newSignalStrength.getWcdmaEcio() * -1;
        } else {
            wcdmaRscp = 255;
        }
        newSignalStrength.setWcdmaEcio(wcdmaRscp);
        if (newSignalStrength.getCdmaDbm() > 0) {
            wcdmaRscp = newSignalStrength.getCdmaDbm() * -1;
        } else {
            wcdmaRscp = -1;
        }
        newSignalStrength.setCdmaDbm(wcdmaRscp);
        if (newSignalStrength.getCdmaEcio() > 0) {
            wcdmaRscp = newSignalStrength.getCdmaEcio() * -1;
        } else {
            wcdmaRscp = 255;
        }
        newSignalStrength.setCdmaEcio(wcdmaRscp);
        if (newSignalStrength.getEvdoDbm() > 0 && newSignalStrength.getEvdoDbm() < ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT) {
            i3 = newSignalStrength.getEvdoDbm() * -1;
        }
        newSignalStrength.setEvdoDbm(i3);
        if (newSignalStrength.getEvdoEcio() >= 0) {
            wcdmaRscp = newSignalStrength.getEvdoEcio() * -1;
        } else {
            wcdmaRscp = 255;
        }
        newSignalStrength.setEvdoEcio(wcdmaRscp);
        if (newSignalStrength.getEvdoSnr() > 0 && newSignalStrength.getEvdoSnr() <= 8) {
            i2 = newSignalStrength.getEvdoSnr();
        }
        newSignalStrength.setEvdoSnr(i2);
        newSignalStrength.setLteSignalStrength(newSignalStrength.getLteSignalStrength() >= 0 ? newSignalStrength.getLteSignalStrength() : 99);
        wcdmaRscp = (newSignalStrength.getLteRsrp() < 44 || newSignalStrength.getLteRsrp() > 140) ? WCDMA_STRENGTH_INVALID : newSignalStrength.getLteRsrp() * -1;
        newSignalStrength.setLteRsrp(wcdmaRscp);
        wcdmaRscp = (newSignalStrength.getLteRsrq() < 3 || newSignalStrength.getLteRsrq() > 20) ? WCDMA_STRENGTH_INVALID : newSignalStrength.getLteRsrq() * -1;
        newSignalStrength.setLteRsrq(wcdmaRscp);
        if (newSignalStrength.getLteRssnr() >= -200 && newSignalStrength.getLteRssnr() <= AppTypeInfo.PG_TYPE_BASE) {
            i = newSignalStrength.getLteRssnr();
        }
        newSignalStrength.setLteRssnr(i);
        Rlog.d(LOG_TAG, "Signal after HW validate=" + newSignalStrength);
    }

    public int getLevel(SignalType type, int rssi, int ecio) {
        if (!this.mSigCustConfigured && SystemProperties.getBoolean(this.PROPERTY_SIGNAL_CUST_CONFIGURED, false)) {
            loadAllCustInfo();
            this.mSigCustConfigured = true;
            logi("after updatesigcustxml, loadAllCustInfo again to load cust props");
        }
        SignalThreshold signalThreshold = getSignalThreshold(type);
        if (signalThreshold == null || !signalThreshold.isConfigured()) {
            return -1;
        }
        return signalThreshold.getSignalLevel(rssi, ecio);
    }

    public static boolean isRssiValid(SignalType type, int rssi) {
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[type.ordinal()]) {
            case 1:
            case 3:
                if (rssi > 0 || -1 == rssi) {
                    return false;
                }
            case 2:
            case 5:
                if (WCDMA_STRENGTH_INVALID == rssi || rssi > -30) {
                    return false;
                }
            case 4:
                if (rssi == 0 || rssi == 99 || rssi > 0 || -1 == rssi) {
                    return false;
                }
            case 6:
                if (rssi == 0 || rssi == WCDMA_STRENGTH_INVALID || rssi == 99 || rssi > 0 || -1 == rssi) {
                    return false;
                }
            default:
                return false;
        }
        return true;
    }

    public static boolean isEcioValid(SignalType type, int ecio) {
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[type.ordinal()]) {
            case 1:
            case 3:
                if (255 == ecio) {
                    return false;
                }
                break;
            case 2:
            case 5:
                if (WCDMA_STRENGTH_INVALID == ecio || ecio > 99) {
                    return false;
                }
            case 4:
                if (255 == ecio) {
                    return false;
                }
                break;
            case 6:
                if (ecio == 255 || ecio == WCDMA_STRENGTH_INVALID || ecio == 255) {
                    return false;
                }
            default:
                return false;
        }
        return true;
    }

    public static boolean isEcioValidForQCom(SignalType type, int ecio) {
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[type.ordinal()]) {
            case 1:
            case 3:
            case 4:
            case 6:
                if (255 == ecio) {
                    return false;
                }
                break;
            case 2:
            case 5:
                if (WCDMA_STRENGTH_INVALID == ecio) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
