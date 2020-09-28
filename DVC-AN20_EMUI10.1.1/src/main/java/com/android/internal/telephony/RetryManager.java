package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Build;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.telephony.dataconnection.TransportManager;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class RetryManager {
    public static final boolean DBG = true;
    private static final long DEFAULT_APN_RETRY_AFTER_DISCONNECT_DELAY = 10000;
    private static final String DEFAULT_DATA_RETRY_CONFIG = "max_retries=3, 5000, 5000, 5000";
    private static String DEFAULT_DATA_RETRY_CONFIG_CUST = SystemProperties.get("ro.gsm.data_retry_config");
    private static final long DEFAULT_INTER_APN_DELAY = 20000;
    private static final long DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING = 3000;
    public static final String LOG_TAG = "RetryManager";
    private static final int MAX_SAME_APN_RETRY = 3;
    public static final long NO_RETRY = -1;
    public static final long NO_SUGGESTED_RETRY_DELAY = -2;
    private static final String OTHERS_APN_TYPE = "others";
    private static String OTHERS_DATA_RETRY_CONFIG_CUST = SystemProperties.get("ro.gsm.2nd_data_retry_config");
    public static final boolean VDBG = false;
    private boolean RETRYFOREVER = SystemProperties.getBoolean("ro.config.hw_pdp_retry_forever", false);
    private long mApnRetryAfterDisconnectDelay;
    @UnsupportedAppUsage
    private String mApnType;
    private String mConfig;
    private int mCurrentApnIndex = -1;
    @UnsupportedAppUsage
    private long mFailFastInterApnDelay;
    @UnsupportedAppUsage
    private long mInterApnDelay;
    private int mMaxRetryCount;
    private long mModemSuggestedDelay = -2;
    @UnsupportedAppUsage
    private Phone mPhone;
    private ArrayList<RetryRec> mRetryArray = new ArrayList<>();
    private int mRetryCount = 0;
    private boolean mRetryForever = false;
    private Random mRng = new Random();
    private int mSameApnRetryCount = 0;
    private ArrayList<ApnSetting> mWaitingApns = null;

    /* access modifiers changed from: private */
    public static class RetryRec {
        int mDelayTime;
        int mRandomizationTime;

        RetryRec(int delayTime, int randomizationTime) {
            this.mDelayTime = delayTime;
            this.mRandomizationTime = randomizationTime;
        }
    }

    public RetryManager(Phone phone, String apnType) {
        this.mPhone = phone;
        this.mApnType = apnType;
    }

    @UnsupportedAppUsage
    private boolean configure(String configStr) {
        if (configStr.startsWith("\"") && configStr.endsWith("\"")) {
            configStr = configStr.substring(1, configStr.length() - 1);
        }
        reset();
        log("configure: '" + configStr + "'");
        this.mConfig = configStr;
        if (!TextUtils.isEmpty(configStr)) {
            int defaultRandomization = 0;
            String[] strArray = configStr.split(",");
            for (int i = 0; i < strArray.length; i++) {
                String[] splitStr = strArray[i].split("=", 2);
                splitStr[0] = splitStr[0].trim();
                if (splitStr.length > 1) {
                    splitStr[1] = splitStr[1].trim();
                    if (TextUtils.equals(splitStr[0], "default_randomization")) {
                        Pair<Boolean, Integer> value = parseNonNegativeInt(splitStr[0], splitStr[1]);
                        if (!((Boolean) value.first).booleanValue()) {
                            return false;
                        }
                        defaultRandomization = ((Integer) value.second).intValue();
                    } else if (!TextUtils.equals(splitStr[0], "max_retries")) {
                        Rlog.e(LOG_TAG, "Unrecognized configuration name value pair: " + strArray[i]);
                        return false;
                    } else if (TextUtils.equals("infinite", splitStr[1])) {
                        this.mRetryForever = true;
                    } else {
                        Pair<Boolean, Integer> value2 = parseNonNegativeInt(splitStr[0], splitStr[1]);
                        if (!((Boolean) value2.first).booleanValue()) {
                            return false;
                        }
                        this.mMaxRetryCount = ((Integer) value2.second).intValue();
                    }
                } else {
                    String[] splitStr2 = strArray[i].split(":", 2);
                    splitStr2[0] = splitStr2[0].trim();
                    RetryRec rr = new RetryRec(0, 0);
                    Pair<Boolean, Integer> value3 = parseNonNegativeInt("delayTime", splitStr2[0]);
                    if (!((Boolean) value3.first).booleanValue()) {
                        return false;
                    }
                    rr.mDelayTime = ((Integer) value3.second).intValue();
                    if (splitStr2.length > 1) {
                        splitStr2[1] = splitStr2[1].trim();
                        Pair<Boolean, Integer> value4 = parseNonNegativeInt("randomizationTime", splitStr2[1]);
                        if (!((Boolean) value4.first).booleanValue()) {
                            return false;
                        }
                        rr.mRandomizationTime = ((Integer) value4.second).intValue();
                    } else {
                        rr.mRandomizationTime = defaultRandomization;
                    }
                    this.mRetryArray.add(rr);
                }
            }
            if (this.mRetryArray.size() > this.mMaxRetryCount) {
                this.mMaxRetryCount = this.mRetryArray.size();
            }
        } else {
            log("configure: cleared");
        }
        return true;
    }

    private void configureRetry() {
        String configString = null;
        String otherConfigString = null;
        try {
            if (Build.IS_DEBUGGABLE) {
                String config = SystemProperties.get("test.data_retry_config");
                if (!TextUtils.isEmpty(config)) {
                    configure(config);
                    return;
                }
            }
            PersistableBundle b = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
            this.mInterApnDelay = b.getLong("carrier_data_call_apn_delay_default_long", DEFAULT_INTER_APN_DELAY);
            this.mFailFastInterApnDelay = b.getLong("carrier_data_call_apn_delay_faster_long", DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING);
            this.mApnRetryAfterDisconnectDelay = b.getLong("carrier_data_call_apn_retry_after_disconnect_long", DEFAULT_APN_RETRY_AFTER_DISCONNECT_DELAY);
            if (TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(this.mApnType) && !TextUtils.isEmpty(getGsmDefaultDataRetryConfig())) {
                configString = getGsmDefaultDataRetryConfig();
                log("configString = " + configString);
                configure(configString);
            } else if (TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(this.mApnType) || TextUtils.isEmpty(getGsm2ndDataRetryConfig())) {
                String[] allConfigStrings = b.getStringArray("carrier_data_call_retry_config_strings");
                if (allConfigStrings != null) {
                    int length = allConfigStrings.length;
                    String otherConfigString2 = null;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            otherConfigString = otherConfigString2;
                            break;
                        }
                        try {
                            String s = allConfigStrings[i];
                            if (!TextUtils.isEmpty(s)) {
                                String[] splitStr = s.split(":", 2);
                                if (splitStr.length == 2) {
                                    String apnType = splitStr[0].trim();
                                    if (apnType.equals(this.mApnType)) {
                                        configString = splitStr[1];
                                        otherConfigString = otherConfigString2;
                                        break;
                                    } else if (apnType.equals(OTHERS_APN_TYPE)) {
                                        otherConfigString2 = splitStr[1];
                                    }
                                } else {
                                    continue;
                                }
                            }
                            i++;
                        } catch (NullPointerException e) {
                            log("Failed to read configuration! Use the hardcoded default value.");
                            this.mInterApnDelay = DEFAULT_INTER_APN_DELAY;
                            this.mFailFastInterApnDelay = DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING;
                            configString = DEFAULT_DATA_RETRY_CONFIG;
                            log("configString = " + configString);
                            configure(configString);
                        }
                    }
                }
                if (configString == null) {
                    if (otherConfigString != null) {
                        configString = otherConfigString;
                    } else {
                        log("Invalid APN retry configuration!. Use the default one now.");
                        configString = DEFAULT_DATA_RETRY_CONFIG;
                    }
                }
                log("configString = " + configString);
                configure(configString);
            } else {
                configString = getGsm2ndDataRetryConfig();
                log("configString = " + configString);
                configure(configString);
            }
        } catch (NullPointerException e2) {
            log("Failed to read configuration! Use the hardcoded default value.");
            this.mInterApnDelay = DEFAULT_INTER_APN_DELAY;
            this.mFailFastInterApnDelay = DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING;
            configString = DEFAULT_DATA_RETRY_CONFIG;
            log("configString = " + configString);
            configure(configString);
        }
    }

    @UnsupportedAppUsage
    private int getRetryTimer() {
        int index;
        int retVal;
        if (this.mRetryCount < this.mRetryArray.size()) {
            index = this.mRetryCount;
        } else {
            index = this.mRetryArray.size() - 1;
        }
        if (index < 0 || index >= this.mRetryArray.size()) {
            retVal = 0;
        } else {
            retVal = this.mRetryArray.get(index).mDelayTime + nextRandomizationTime(index);
        }
        log("getRetryTimer: " + retVal + " mRetryCount = " + this.mRetryCount);
        return retVal;
    }

    private Pair<Boolean, Integer> parseNonNegativeInt(String name, String stringValue) {
        try {
            int value = Integer.parseInt(stringValue);
            return new Pair<>(Boolean.valueOf(validateNonNegativeInt(name, value)), Integer.valueOf(value));
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, name + " bad value: " + stringValue, e);
            return new Pair<>(false, 0);
        }
    }

    private boolean validateNonNegativeInt(String name, int value) {
        if (value >= 0) {
            return true;
        }
        Rlog.e(LOG_TAG, name + " bad value: is < 0");
        return false;
    }

    private int nextRandomizationTime(int index) {
        int randomTime = this.mRetryArray.get(index).mRandomizationTime;
        if (randomTime == 0) {
            return 0;
        }
        return this.mRng.nextInt(randomTime);
    }

    public ApnSetting getNextApnSetting() {
        int i;
        int i2;
        ArrayList<ApnSetting> arrayList = this.mWaitingApns;
        if (arrayList == null || arrayList.size() == 0) {
            log("Waiting APN list is null or empty.");
            return null;
        } else if (this.mModemSuggestedDelay == -2 || (i = this.mSameApnRetryCount) >= 3 || (i2 = this.mCurrentApnIndex) == -1) {
            this.mSameApnRetryCount = 0;
            int index = this.mCurrentApnIndex;
            do {
                index++;
                if (index == this.mWaitingApns.size()) {
                    index = 0;
                }
                if (!this.mWaitingApns.get(index).getPermanentFailed()) {
                    this.mCurrentApnIndex = index;
                    return this.mWaitingApns.get(this.mCurrentApnIndex);
                }
            } while (index != this.mCurrentApnIndex);
            return null;
        } else {
            this.mSameApnRetryCount = i + 1;
            return this.mWaitingApns.get(i2);
        }
    }

    public long getDelayForNextApn(boolean failFastEnabled) {
        long delay;
        ArrayList<ApnSetting> arrayList = this.mWaitingApns;
        if (arrayList == null || arrayList.size() == 0) {
            log("Waiting APN list is null or empty.");
            return -1;
        } else if (this.mModemSuggestedDelay == -1) {
            log("Modem suggested not retrying.");
            return -1;
        } else {
            boolean isNeedModemSuggestedRetry = TextUtils.isEmpty(getGsmDefaultDataRetryConfig());
            if (this.mModemSuggestedDelay == -2 || this.mSameApnRetryCount >= 3 || !isNeedModemSuggestedRetry) {
                log("mCurrentApnIndex is: " + this.mCurrentApnIndex + ", size is: " + this.mWaitingApns.size());
                int index = this.mCurrentApnIndex;
                do {
                    index++;
                    if (index >= this.mWaitingApns.size()) {
                        index = 0;
                    }
                    if (!this.mWaitingApns.get(index).getPermanentFailed()) {
                        if (index <= this.mCurrentApnIndex) {
                            if (!this.mRetryForever && this.mRetryCount + 1 > this.mMaxRetryCount) {
                                log("Reached maximum retry count " + this.mMaxRetryCount + ".");
                                if (!this.RETRYFOREVER) {
                                    return -1;
                                }
                                log("reset mRetryCount");
                                this.mRetryCount = 0;
                            }
                            delay = (long) getRetryTimer();
                            this.mRetryCount++;
                        } else {
                            delay = this.mInterApnDelay;
                        }
                        if (failFastEnabled && delay > this.mFailFastInterApnDelay) {
                            delay = this.mFailFastInterApnDelay;
                        }
                        if (SystemProperties.getLong("persist.radio.telecom_apn_delay", 0) > delay) {
                            delay = SystemProperties.getLong("persist.radio.telecom_apn_delay", 0);
                        }
                        log("getDelayForNextApn delay = " + delay);
                        return delay;
                    }
                } while (index != this.mCurrentApnIndex);
                log("All APNs have permanently failed.");
                return -1;
            }
            log("Modem suggested retry in " + this.mModemSuggestedDelay + " ms.");
            return this.mModemSuggestedDelay;
        }
    }

    public void resetApnPermanentFailedFlag(ApnSetting apn) {
        if (apn != null) {
            apn.setPermanentFailed(false);
        }
    }

    public void markApnPermanentFailed(ApnSetting apn) {
        if (apn != null) {
            apn.setPermanentFailed(true);
        }
    }

    private void reset() {
        this.mMaxRetryCount = 0;
        this.mRetryCount = 0;
        this.mCurrentApnIndex = -1;
        this.mSameApnRetryCount = 0;
        this.mModemSuggestedDelay = -2;
        this.mRetryArray.clear();
    }

    public void setWaitingApns(ArrayList<ApnSetting> waitingApns) {
        if (waitingApns == null) {
            log("No waiting APNs provided");
            return;
        }
        this.mWaitingApns = waitingApns;
        configureRetry();
        Iterator<ApnSetting> it = this.mWaitingApns.iterator();
        while (it.hasNext()) {
            it.next().setPermanentFailed(false);
        }
        log("Setting " + this.mWaitingApns.size() + " waiting APNs.");
    }

    public ArrayList<ApnSetting> getWaitingApns() {
        return this.mWaitingApns;
    }

    public void setModemSuggestedDelay(long delay) {
        this.mModemSuggestedDelay = delay;
    }

    public long getModemSuggestedDelay() {
        return this.mModemSuggestedDelay;
    }

    public long getRetryAfterDisconnectDelay() {
        long ApnDelayProper = SystemProperties.getLong("persist.radio.telecom_apn_delay", 0);
        long j = this.mApnRetryAfterDisconnectDelay;
        if (ApnDelayProper > j) {
            return ApnDelayProper;
        }
        return j;
    }

    public String toString() {
        if (this.mConfig == null) {
            return PhoneConfigurationManager.SSSS;
        }
        return "RetryManager: mApnType=" + this.mApnType + " mRetryCount=" + this.mRetryCount + " mMaxRetryCount=" + this.mMaxRetryCount + " mCurrentApnIndex=" + this.mCurrentApnIndex + " mSameApnRtryCount=" + this.mSameApnRetryCount + " mModemSuggestedDelay=" + this.mModemSuggestedDelay + " mRetryForever=" + this.mRetryForever + " mInterApnDelay=" + this.mInterApnDelay + " mApnRetryAfterDisconnectDelay=" + this.mApnRetryAfterDisconnectDelay + " mConfig={" + this.mConfig + "}";
    }

    @UnsupportedAppUsage
    private void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mApnType + "] " + s);
    }

    public boolean isLastApnSetting() {
        ArrayList<ApnSetting> arrayList;
        int i = this.mCurrentApnIndex;
        if (i <= 0 || (arrayList = this.mWaitingApns) == null || i != arrayList.size() - 1) {
            return false;
        }
        return true;
    }

    public void resetRetryCount() {
        this.mRetryCount = 0;
    }

    private String getGsmDefaultDataRetryConfig() {
        int slotId = this.mPhone.getPhoneId();
        String valueFromCard = (String) HwCfgFilePolicy.getValue("data_retry_config", slotId, String.class);
        String valueFromProp = DEFAULT_DATA_RETRY_CONFIG_CUST;
        log("getGsmDefaultDataRetryConfig, slotId:" + slotId + ", card:" + valueFromCard + ", prop:" + valueFromProp);
        return valueFromCard != null ? valueFromCard : valueFromProp;
    }

    private String getGsm2ndDataRetryConfig() {
        int slotId = this.mPhone.getPhoneId();
        String valueFromCard = (String) HwCfgFilePolicy.getValue("2nd_data_retry_config", slotId, String.class);
        String valueFromProp = OTHERS_DATA_RETRY_CONFIG_CUST;
        log("getGsm2ndDataRetryConfig, slotId:" + slotId + ", card:" + valueFromCard + ", prop:" + valueFromProp);
        return valueFromCard != null ? valueFromCard : valueFromProp;
    }
}
