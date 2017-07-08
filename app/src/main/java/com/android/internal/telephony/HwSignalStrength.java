package com.android.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import com.huawei.connectivitylog.ConnectivityLogManager;
import huawei.android.view.HwMotionEvent;
import huawei.com.android.internal.widget.HwFragmentContainer;

public class HwSignalStrength {
    private static final /* synthetic */ int[] -com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues = null;
    public static final int DEFAULT_NUM_SIGNAL_STRENGTH_BINS = 4;
    protected static final String DEFAULT_SIGNAL_CUST_CDMA = "5,false,-112,-106,-99,-92,-85";
    protected static final String DEFAULT_SIGNAL_CUST_CDMALTE = "5,false,-120,-115,-110,-105,-97";
    protected static final String DEFAULT_SIGNAL_CUST_EVDO = "5,false,-112,-106,-99,-92,-85";
    protected static final String DEFAULT_SIGNAL_CUST_GSM = "5,false,-109,-103,-97,-91,-85";
    protected static final String DEFAULT_SIGNAL_CUST_LTE = "5,false,-120,-115,-110,-105,-97";
    protected static final String DEFAULT_SIGNAL_CUST_UMTS = "5,false,-112,-105,-99,-93,-87";
    public static final int GSM_STRENGTH_NONE = 0;
    public static final int GSM_STRENGTH_UNKOUWN = 99;
    public static final int INVALID_ECIO = 255;
    public static final int INVALID_RSSI = -1;
    private static final String LOG_TAG = "HwSignalStrength";
    public static final int LTE_RSSNR_UNKOUWN_STD = 99;
    public static final int LTE_STRENGTH_UNKOUWN_STD = -30;
    public static final int MAX_ASU = 31;
    public static final int SIGNAL_STRENGTH_EXCELLENT = 5;
    public static final int WCDMA_ECIO_NONE = 255;
    public static final int WCDMA_STRENGTH_INVALID = Integer.MAX_VALUE;
    public static final int WCDMA_STRENGTH_NONE = 0;
    public static final int WCDMA_STRENGTH_UNKOUWN = 99;
    private static HwSignalStrength mHwSignalStrength;
    private String PROPERTY_SIGNAL_CUST_CDMA;
    private String PROPERTY_SIGNAL_CUST_CDMALTE;
    private String PROPERTY_SIGNAL_CUST_CONFIGURED;
    private String PROPERTY_SIGNAL_CUST_EVDO;
    private String PROPERTY_SIGNAL_CUST_GSM;
    private String PROPERTY_SIGNAL_CUST_LTE;
    private String PROPERTY_SIGNAL_CUST_UMTS;
    private SignalThreshold mCdmaLteSignalThreshold;
    private SignalThreshold mCdmaSignalThreshold;
    private SignalThreshold mEvdoSignalThreshold;
    private SignalThreshold mGsmSignalThreshold;
    private SignalThreshold mLteSignalThreshold;
    private boolean mSigCustConfigured;
    private SignalThreshold mUmtsSignalThreshold;

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
                iArr[SignalType.LTE.ordinal()] = HwSignalStrength.DEFAULT_NUM_SIGNAL_STRENGTH_BINS;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[SignalType.UMTS.ordinal()] = HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[SignalType.UNKNOWN.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            -com-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues = iArr;
            return iArr;
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
            this.mNumOfBins = Integer.parseInt(config[HwSignalStrength.WCDMA_STRENGTH_NONE]);
            this.mUseEcio = "true".equals(config[1]);
            this.mThresholdRssiPoor = Integer.parseInt(config[2]);
            this.mThresholdRssiModerate = Integer.parseInt(config[3]);
            this.mThresholdRssiGood = Integer.parseInt(config[HwSignalStrength.DEFAULT_NUM_SIGNAL_STRENGTH_BINS]);
            this.mThresholdRssiGreat = Integer.parseInt(config[HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT]);
            if (HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT == this.mNumOfBins) {
                this.mThresholdRssiExcellent = Integer.parseInt(config[6]);
            }
            if (this.mUseEcio) {
                switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[this.mType.ordinal()]) {
                    case HwFragmentContainer.TRANSITION_FADE /*1*/:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]);
                        this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]);
                        this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]);
                        this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]);
                        if (HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]);
                        }
                    case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                    case HwSignalStrength.DEFAULT_NUM_SIGNAL_STRENGTH_BINS /*4*/:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]);
                        this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]);
                        this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]);
                        this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]);
                        if (HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]);
                        }
                    case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]);
                        this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]);
                        this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]);
                        this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]);
                        if (HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]);
                        }
                    case HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT /*5*/:
                        this.mThresholdEcioPoor = Integer.parseInt(config[(this.mNumOfBins + 6) - 4]);
                        this.mThresholdEcioModerate = Integer.parseInt(config[(this.mNumOfBins + 7) - 4]);
                        this.mThresholdEcioGood = Integer.parseInt(config[(this.mNumOfBins + 8) - 4]);
                        this.mThresholdEcioGreat = Integer.parseInt(config[(this.mNumOfBins + 9) - 4]);
                        if (HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT == this.mNumOfBins) {
                            this.mThresholdEcioExcellent = Integer.parseInt(config[(this.mNumOfBins + 10) - 4]);
                        }
                    default:
                        this.mUseEcio = false;
                }
            }
        }

        public int getSignalLevel(int rssi, int ecio) {
            int rssiLevel;
            if (rssi < this.mThresholdRssiPoor || !HwSignalStrength.isRssiValid(this.mType, rssi)) {
                rssiLevel = HwSignalStrength.WCDMA_STRENGTH_NONE;
            } else if (rssi < this.mThresholdRssiModerate) {
                rssiLevel = 1;
            } else if (rssi < this.mThresholdRssiGood) {
                rssiLevel = 2;
            } else if (rssi < this.mThresholdRssiGreat) {
                rssiLevel = 3;
            } else if (HwSignalStrength.DEFAULT_NUM_SIGNAL_STRENGTH_BINS == this.mNumOfBins || (HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT == this.mNumOfBins && rssi < this.mThresholdRssiExcellent)) {
                rssiLevel = HwSignalStrength.DEFAULT_NUM_SIGNAL_STRENGTH_BINS;
            } else {
                rssiLevel = HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT;
            }
            if (!this.mUseEcio) {
                return rssiLevel;
            }
            int ecioLevel;
            if (!HwSignalStrength.isEcioValid(this.mType, ecio)) {
                ecioLevel = HwSignalStrength.WCDMA_STRENGTH_NONE;
            } else if (ecio < this.mThresholdEcioPoor) {
                ecioLevel = HwSignalStrength.WCDMA_STRENGTH_NONE;
            } else if (ecio < this.mThresholdEcioModerate) {
                ecioLevel = 1;
            } else if (ecio < this.mThresholdEcioGood) {
                ecioLevel = 2;
            } else if (ecio < this.mThresholdEcioGreat) {
                ecioLevel = 3;
            } else if (HwSignalStrength.DEFAULT_NUM_SIGNAL_STRENGTH_BINS == this.mNumOfBins || (HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT == this.mNumOfBins && ecio < this.mThresholdEcioExcellent)) {
                ecioLevel = HwSignalStrength.DEFAULT_NUM_SIGNAL_STRENGTH_BINS;
            } else {
                ecioLevel = HwSignalStrength.SIGNAL_STRENGTH_EXCELLENT;
            }
            return rssiLevel >= ecioLevel ? ecioLevel : rssiLevel;
        }
    }

    public enum SignalType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwSignalStrength.SignalType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwSignalStrength.SignalType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwSignalStrength.SignalType.<clinit>():void");
        }
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
            iArr[SignalType.GSM.ordinal()] = DEFAULT_NUM_SIGNAL_STRENGTH_BINS;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SignalType.LTE.ordinal()] = SIGNAL_STRENGTH_EXCELLENT;
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
        this.PROPERTY_SIGNAL_CUST_GSM = "gsm.sigcust.gsm";
        this.PROPERTY_SIGNAL_CUST_UMTS = "gsm.sigcust.umts";
        this.PROPERTY_SIGNAL_CUST_CDMA = "gsm.sigcust.cdma";
        this.PROPERTY_SIGNAL_CUST_EVDO = "gsm.sigcust.evdo";
        this.PROPERTY_SIGNAL_CUST_LTE = "gsm.sigcust.lte";
        this.PROPERTY_SIGNAL_CUST_CDMALTE = "gsm.sigcust.cdmalte";
        this.PROPERTY_SIGNAL_CUST_CONFIGURED = "gsm.sigcust.configured";
        this.mSigCustConfigured = false;
        this.mGsmSignalThreshold = new SignalThreshold(null);
        this.mUmtsSignalThreshold = new SignalThreshold(null);
        this.mCdmaSignalThreshold = new SignalThreshold(null);
        this.mEvdoSignalThreshold = new SignalThreshold(null);
        this.mLteSignalThreshold = new SignalThreshold(null);
        this.mCdmaLteSignalThreshold = new SignalThreshold(null);
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
            signalThreshold = new SignalThreshold(null);
        }
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[signalType.ordinal()]) {
            case HwFragmentContainer.TRANSITION_FADE /*1*/:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_CDMA, "");
                defaultValue = DEFAULT_SIGNAL_CUST_EVDO;
                break;
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_CDMALTE, "");
                defaultValue = DEFAULT_SIGNAL_CUST_LTE;
                break;
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_EVDO, "");
                defaultValue = DEFAULT_SIGNAL_CUST_EVDO;
                break;
            case DEFAULT_NUM_SIGNAL_STRENGTH_BINS /*4*/:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_GSM, "");
                defaultValue = DEFAULT_SIGNAL_CUST_GSM;
                break;
            case SIGNAL_STRENGTH_EXCELLENT /*5*/:
                configItem = SystemProperties.get(this.PROPERTY_SIGNAL_CUST_LTE, "");
                defaultValue = DEFAULT_SIGNAL_CUST_LTE;
                break;
            case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
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
        if (sig_cust_strings == null || "".equals(sig_cust_strings)) {
            logi("no cust_signal_thresholds found");
            return;
        }
        String[] network_type_sig_custs = sig_cust_strings.split(";");
        logi("cust_signal_thresholds : " + sig_cust_strings);
        if (network_type_sig_custs != null) {
            for (int i = WCDMA_STRENGTH_NONE; i < network_type_sig_custs.length; i++) {
                setCustConfigFromString(network_type_sig_custs[i]);
            }
        }
    }

    private static String getSigCustString(Context context) {
        String custStr = "";
        try {
            custStr = Systemex.getString(context.getContentResolver(), "cust_signal_thresholds");
        } catch (Exception e) {
        }
        return custStr;
    }

    private void setCustConfigFromString(String configstr) {
        if (configstr != null) {
            if (configstr.contains(",")) {
                int pos = configstr.indexOf(",");
                String configType = configstr.substring(WCDMA_STRENGTH_NONE, pos);
                String configInfo = configstr.substring(pos + 1, configstr.length());
                switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[typeString2Enum(configType).ordinal()]) {
                    case HwFragmentContainer.TRANSITION_FADE /*1*/:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_CDMA, configInfo);
                        logi("set cdma sig cust to " + configInfo);
                        break;
                    case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_EVDO, configInfo);
                        logi("set evdo sig cust to " + configInfo);
                        break;
                    case DEFAULT_NUM_SIGNAL_STRENGTH_BINS /*4*/:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_GSM, configInfo);
                        logi("set gsm sig cust to " + configInfo);
                        break;
                    case SIGNAL_STRENGTH_EXCELLENT /*5*/:
                        SystemProperties.set(this.PROPERTY_SIGNAL_CUST_LTE, configInfo);
                        logi("set lte sig cust to " + configInfo);
                        break;
                    case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
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
        logi("custInfo " + custInfo);
        if (custInfo == null || custInfo.length() == 0) {
            loge("Cust config is Empty." + custInfo);
            return false;
        }
        String[] config = custInfo.split(",");
        if (config.length < 6) {
            loge("Cust config length Error!!:" + config.length);
            return false;
        } else if ("4".equals(config[WCDMA_STRENGTH_NONE]) || "5".equals(config[WCDMA_STRENGTH_NONE])) {
            if (!"true".equals(config[1])) {
                if (!"4".equals(config[WCDMA_STRENGTH_NONE]) || config.length == 6) {
                    if ("5".equals(config[WCDMA_STRENGTH_NONE]) && config.length != 7) {
                    }
                }
                loge("Length of config Error!!:" + config.length);
                return false;
            } else if (("4".equals(config[WCDMA_STRENGTH_NONE]) && config.length != 10) || ("5".equals(config[WCDMA_STRENGTH_NONE]) && config.length != 12)) {
                loge("Length of config Error!!:" + config.length);
                return false;
            }
            logi("Cust config is valid!");
            return true;
        } else {
            loge("Num of bins Error!!:" + config[WCDMA_STRENGTH_NONE]);
            return false;
        }
    }

    protected SignalThreshold getSignalThreshold(SignalType type) {
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[type.ordinal()]) {
            case HwFragmentContainer.TRANSITION_FADE /*1*/:
                return this.mCdmaSignalThreshold;
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                return this.mCdmaLteSignalThreshold;
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                return this.mEvdoSignalThreshold;
            case DEFAULT_NUM_SIGNAL_STRENGTH_BINS /*4*/:
                return this.mGsmSignalThreshold;
            case SIGNAL_STRENGTH_EXCELLENT /*5*/:
                return this.mLteSignalThreshold;
            case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                return this.mUmtsSignalThreshold;
            default:
                return null;
        }
    }

    public void validateInput(SignalStrength newSignalStrength) {
        int gsmSignalStrength;
        int i = WCDMA_STRENGTH_INVALID;
        int i2 = WCDMA_ECIO_NONE;
        int i3 = INVALID_RSSI;
        Rlog.d(LOG_TAG, "Signal before HW validate=" + newSignalStrength);
        if (newSignalStrength.getGsmSignalStrength() > 0) {
            gsmSignalStrength = newSignalStrength.getGsmSignalStrength() * INVALID_RSSI;
        } else {
            gsmSignalStrength = INVALID_RSSI;
        }
        newSignalStrength.setGsmSignalStrength(gsmSignalStrength);
        if (newSignalStrength.getWcdmaRscp() > 0) {
            gsmSignalStrength = newSignalStrength.getWcdmaRscp() * INVALID_RSSI;
        } else {
            gsmSignalStrength = INVALID_RSSI;
        }
        newSignalStrength.setWcdmaRscp(gsmSignalStrength);
        if (newSignalStrength.getWcdmaEcio() >= 0) {
            gsmSignalStrength = newSignalStrength.getWcdmaEcio() * INVALID_RSSI;
        } else {
            gsmSignalStrength = WCDMA_ECIO_NONE;
        }
        newSignalStrength.setWcdmaEcio(gsmSignalStrength);
        if (newSignalStrength.getCdmaDbm() > 0) {
            gsmSignalStrength = newSignalStrength.getCdmaDbm() * INVALID_RSSI;
        } else {
            gsmSignalStrength = INVALID_RSSI;
        }
        newSignalStrength.setCdmaDbm(gsmSignalStrength);
        if (newSignalStrength.getCdmaEcio() > 0) {
            gsmSignalStrength = newSignalStrength.getCdmaEcio() * INVALID_RSSI;
        } else {
            gsmSignalStrength = WCDMA_ECIO_NONE;
        }
        newSignalStrength.setCdmaEcio(gsmSignalStrength);
        if (newSignalStrength.getEvdoDbm() > 0 && newSignalStrength.getEvdoDbm() < ConnectivityLogManager.WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT) {
            i3 = newSignalStrength.getEvdoDbm() * INVALID_RSSI;
        }
        newSignalStrength.setEvdoDbm(i3);
        if (newSignalStrength.getEvdoEcio() >= 0) {
            gsmSignalStrength = newSignalStrength.getEvdoEcio() * INVALID_RSSI;
        } else {
            gsmSignalStrength = WCDMA_ECIO_NONE;
        }
        newSignalStrength.setEvdoEcio(gsmSignalStrength);
        if (newSignalStrength.getEvdoSnr() > 0 && newSignalStrength.getEvdoSnr() <= 8) {
            i2 = newSignalStrength.getEvdoSnr();
        }
        newSignalStrength.setEvdoSnr(i2);
        newSignalStrength.setLteSignalStrength(newSignalStrength.getLteSignalStrength() >= 0 ? newSignalStrength.getLteSignalStrength() : WCDMA_STRENGTH_UNKOUWN);
        if (newSignalStrength.getLteRsrp() < 44 || newSignalStrength.getLteRsrp() > PduHeaders.MESSAGE_TYPE_MBOX_STORE_CONF) {
            gsmSignalStrength = WCDMA_STRENGTH_INVALID;
        } else {
            gsmSignalStrength = newSignalStrength.getLteRsrp() * INVALID_RSSI;
        }
        newSignalStrength.setLteRsrp(gsmSignalStrength);
        if (newSignalStrength.getLteRsrq() < 3 || newSignalStrength.getLteRsrq() > 20) {
            gsmSignalStrength = WCDMA_STRENGTH_INVALID;
        } else {
            gsmSignalStrength = newSignalStrength.getLteRsrq() * INVALID_RSSI;
        }
        newSignalStrength.setLteRsrq(gsmSignalStrength);
        if (newSignalStrength.getLteRssnr() >= -200 && newSignalStrength.getLteRssnr() <= 300) {
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
            return INVALID_RSSI;
        }
        return signalThreshold.getSignalLevel(rssi, ecio);
    }

    public static boolean isRssiValid(SignalType type, int rssi) {
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[type.ordinal()]) {
            case HwFragmentContainer.TRANSITION_FADE /*1*/:
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                if (rssi > 0 || INVALID_RSSI == rssi) {
                    return false;
                }
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
            case SIGNAL_STRENGTH_EXCELLENT /*5*/:
                if (WCDMA_STRENGTH_INVALID == rssi || rssi > LTE_STRENGTH_UNKOUWN_STD) {
                    return false;
                }
            case DEFAULT_NUM_SIGNAL_STRENGTH_BINS /*4*/:
                if (rssi == 0 || rssi == WCDMA_STRENGTH_UNKOUWN || rssi > 0 || INVALID_RSSI == rssi) {
                    return false;
                }
            case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                if (!(rssi == 0 || rssi == WCDMA_STRENGTH_INVALID || rssi == WCDMA_STRENGTH_UNKOUWN || rssi > 0)) {
                    if (INVALID_RSSI == rssi) {
                    }
                }
                return false;
            default:
                return false;
        }
        return true;
    }

    public static boolean isEcioValid(SignalType type, int ecio) {
        switch (-getcom-android-internal-telephony-HwSignalStrength$SignalTypeSwitchesValues()[type.ordinal()]) {
            case HwFragmentContainer.TRANSITION_FADE /*1*/:
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                if (WCDMA_ECIO_NONE == ecio) {
                    return false;
                }
                break;
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
            case SIGNAL_STRENGTH_EXCELLENT /*5*/:
                if (WCDMA_STRENGTH_INVALID == ecio || ecio > WCDMA_STRENGTH_UNKOUWN) {
                    return false;
                }
            case DEFAULT_NUM_SIGNAL_STRENGTH_BINS /*4*/:
                if (WCDMA_ECIO_NONE == ecio) {
                    return false;
                }
                break;
            case HwMotionEvent.TOOL_TYPE_FINGER_NAIL /*6*/:
                if (ecio == WCDMA_ECIO_NONE || ecio == WCDMA_STRENGTH_INVALID || ecio == WCDMA_ECIO_NONE) {
                    return false;
                }
            default:
                return false;
        }
        return true;
    }
}
