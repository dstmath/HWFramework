package com.android.internal.telephony;

import android.os.Build;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.telephony.dataconnection.ApnSetting;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class RetryManager {
    public static final boolean DBG = true;
    private static final String DEFAULT_DATA_RETRY_CONFIG = "default_randomization=2000,5000,10000,20000,40000,80000:5000,160000:5000,320000:5000,640000:5000,1280000:5000,1800000:5000";
    private static String DEFAULT_DATA_RETRY_CONFIG_CUST = null;
    private static final long DEFAULT_INTER_APN_DELAY = 20000;
    private static final long DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING = 3000;
    public static final String LOG_TAG = "RetryManager";
    private static final int MAX_SAME_APN_RETRY = 3;
    public static final long NO_RETRY = -1;
    public static final long NO_SUGGESTED_RETRY_DELAY = -2;
    private static final String OTHERS_DATA_RETRY_CONFIG = "max_retries=3, 5000, 5000, 5000";
    private static String OTHERS_DATA_RETRY_CONFIG_CUST;
    public static final boolean VDBG = false;
    private boolean RETRYFOREVER;
    private String mApnType;
    private String mConfig;
    private int mCurrentApnIndex;
    private long mFailFastInterApnDelay;
    private long mInterApnDelay;
    private int mMaxRetryCount;
    private long mModemSuggestedDelay;
    private Phone mPhone;
    private ArrayList<RetryRec> mRetryArray;
    private int mRetryCount;
    private boolean mRetryForever;
    private Random mRng;
    private int mSameApnRetryCount;
    private ArrayList<ApnSetting> mWaitingApns;

    private static class RetryRec {
        int mDelayTime;
        int mRandomizationTime;

        RetryRec(int delayTime, int randomizationTime) {
            this.mDelayTime = delayTime;
            this.mRandomizationTime = randomizationTime;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.RetryManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.RetryManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RetryManager.<clinit>():void");
    }

    public RetryManager(Phone phone, String apnType) {
        this.RETRYFOREVER = SystemProperties.getBoolean("ro.config.hw_pdp_retry_forever", false);
        this.mModemSuggestedDelay = NO_SUGGESTED_RETRY_DELAY;
        this.mSameApnRetryCount = 0;
        this.mRetryArray = new ArrayList();
        this.mRetryForever = false;
        this.mRetryCount = 0;
        this.mRng = new Random();
        this.mWaitingApns = null;
        this.mCurrentApnIndex = -1;
        this.mPhone = phone;
        this.mApnType = apnType;
    }

    private boolean configure(String configStr) {
        if (configStr.startsWith("\"") && configStr.endsWith("\"")) {
            configStr = configStr.substring(1, configStr.length() - 1);
        }
        reset();
        log("configure: '" + configStr + "'");
        this.mConfig = configStr;
        if (TextUtils.isEmpty(configStr)) {
            log("configure: cleared");
        } else {
            int defaultRandomization = 0;
            String[] strArray = configStr.split(",");
            for (int i = 0; i < strArray.length; i++) {
                String[] splitStr = strArray[i].split("=", 2);
                splitStr[0] = splitStr[0].trim();
                Pair<Boolean, Integer> value;
                if (splitStr.length > 1) {
                    splitStr[1] = splitStr[1].trim();
                    if (TextUtils.equals(splitStr[0], "default_randomization")) {
                        value = parseNonNegativeInt(splitStr[0], splitStr[1]);
                        if (!((Boolean) value.first).booleanValue()) {
                            return false;
                        }
                        defaultRandomization = ((Integer) value.second).intValue();
                    } else if (!TextUtils.equals(splitStr[0], "max_retries")) {
                        Rlog.e(LOG_TAG, "Unrecognized configuration name value pair: " + strArray[i]);
                        return false;
                    } else if (TextUtils.equals("infinite", splitStr[1])) {
                        this.mRetryForever = DBG;
                    } else {
                        value = parseNonNegativeInt(splitStr[0], splitStr[1]);
                        if (!((Boolean) value.first).booleanValue()) {
                            return false;
                        }
                        this.mMaxRetryCount = ((Integer) value.second).intValue();
                    }
                } else {
                    splitStr = strArray[i].split(":", 2);
                    splitStr[0] = splitStr[0].trim();
                    RetryRec rr = new RetryRec(0, 0);
                    value = parseNonNegativeInt("delayTime", splitStr[0]);
                    if (!((Boolean) value.first).booleanValue()) {
                        return false;
                    }
                    rr.mDelayTime = ((Integer) value.second).intValue();
                    if (splitStr.length > 1) {
                        splitStr[1] = splitStr[1].trim();
                        value = parseNonNegativeInt("randomizationTime", splitStr[1]);
                        if (!((Boolean) value.first).booleanValue()) {
                            return false;
                        }
                        rr.mRandomizationTime = ((Integer) value.second).intValue();
                    } else {
                        rr.mRandomizationTime = defaultRandomization;
                    }
                    this.mRetryArray.add(rr);
                }
            }
            if (this.mRetryArray.size() > this.mMaxRetryCount) {
                this.mMaxRetryCount = this.mRetryArray.size();
            }
        }
        return DBG;
    }

    private void configureRetry(boolean forDefault) {
        String configString = "";
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
            configString = forDefault ? !TextUtils.isEmpty(DEFAULT_DATA_RETRY_CONFIG_CUST) ? DEFAULT_DATA_RETRY_CONFIG_CUST : b.getString("carrier_data_call_retry_config_default_string", DEFAULT_DATA_RETRY_CONFIG) : !TextUtils.isEmpty(OTHERS_DATA_RETRY_CONFIG_CUST) ? OTHERS_DATA_RETRY_CONFIG_CUST : b.getString("carrier_data_call_retry_config_others_string", OTHERS_DATA_RETRY_CONFIG);
        } catch (NullPointerException e) {
            log("Failed to read configuration! Use the hardcoded default value.");
            this.mInterApnDelay = DEFAULT_INTER_APN_DELAY;
            this.mFailFastInterApnDelay = DEFAULT_INTER_APN_DELAY_FOR_PROVISIONING;
            configString = forDefault ? DEFAULT_DATA_RETRY_CONFIG : OTHERS_DATA_RETRY_CONFIG;
        }
        log("configString = " + configString + " forDefault : " + forDefault);
        configure(configString);
    }

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
            retVal = ((RetryRec) this.mRetryArray.get(index)).mDelayTime + nextRandomizationTime(index);
        }
        log("getRetryTimer: " + retVal + " mRetryCount = " + this.mRetryCount);
        return retVal;
    }

    private Pair<Boolean, Integer> parseNonNegativeInt(String name, String stringValue) {
        try {
            int value = Integer.parseInt(stringValue);
            return new Pair(Boolean.valueOf(validateNonNegativeInt(name, value)), Integer.valueOf(value));
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, name + " bad value: " + stringValue, e);
            return new Pair(Boolean.valueOf(false), Integer.valueOf(0));
        }
    }

    private boolean validateNonNegativeInt(String name, int value) {
        if (value >= 0) {
            return DBG;
        }
        Rlog.e(LOG_TAG, name + " bad value: is < 0");
        return false;
    }

    private int nextRandomizationTime(int index) {
        int randomTime = ((RetryRec) this.mRetryArray.get(index)).mRandomizationTime;
        if (randomTime == 0) {
            return 0;
        }
        return this.mRng.nextInt(randomTime);
    }

    public ApnSetting getNextApnSetting() {
        if (this.mWaitingApns == null || this.mWaitingApns.size() == 0) {
            log("Waiting APN list is null or empty.");
            return null;
        } else if (this.mModemSuggestedDelay == NO_SUGGESTED_RETRY_DELAY || this.mSameApnRetryCount >= MAX_SAME_APN_RETRY) {
            this.mSameApnRetryCount = 0;
            int index = this.mCurrentApnIndex;
            do {
                index++;
                if (index == this.mWaitingApns.size()) {
                    index = 0;
                }
                if (!((ApnSetting) this.mWaitingApns.get(index)).permanentFailed) {
                    this.mCurrentApnIndex = index;
                    return (ApnSetting) this.mWaitingApns.get(this.mCurrentApnIndex);
                }
            } while (index != this.mCurrentApnIndex);
            return null;
        } else {
            this.mSameApnRetryCount++;
            return (ApnSetting) this.mWaitingApns.get(this.mCurrentApnIndex);
        }
    }

    public long getDelayForNextApn(boolean failFastEnabled) {
        if (this.mWaitingApns == null || this.mWaitingApns.size() == 0) {
            log("Waiting APN list is null or empty.");
            return NO_RETRY;
        } else if (this.mModemSuggestedDelay == NO_RETRY) {
            log("Modem suggested not retrying.");
            return NO_RETRY;
        } else {
            boolean isNeedModemSuggestedRetry = TextUtils.isEmpty(DEFAULT_DATA_RETRY_CONFIG_CUST);
            if (this.mModemSuggestedDelay == NO_SUGGESTED_RETRY_DELAY || this.mSameApnRetryCount >= MAX_SAME_APN_RETRY || !isNeedModemSuggestedRetry) {
                int index = this.mCurrentApnIndex;
                do {
                    index++;
                    if (index >= this.mWaitingApns.size()) {
                        index = 0;
                    }
                    if (!((ApnSetting) this.mWaitingApns.get(index)).permanentFailed) {
                        long delay;
                        if (index <= this.mCurrentApnIndex) {
                            if (!this.mRetryForever && this.mRetryCount + 1 > this.mMaxRetryCount) {
                                log("Reached maximum retry count " + this.mMaxRetryCount + ".");
                                if (!this.RETRYFOREVER) {
                                    return NO_RETRY;
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
                return NO_RETRY;
            }
            log("Modem suggested retry in " + this.mModemSuggestedDelay + " ms.");
            return this.mModemSuggestedDelay;
        }
    }

    public void markApnPermanentFailed(ApnSetting apn) {
        if (apn != null) {
            apn.permanentFailed = DBG;
        }
    }

    private void reset() {
        this.mMaxRetryCount = 0;
        this.mRetryCount = 0;
        this.mCurrentApnIndex = -1;
        this.mSameApnRetryCount = 0;
        this.mModemSuggestedDelay = NO_SUGGESTED_RETRY_DELAY;
        this.mRetryArray.clear();
    }

    public void setWaitingApns(ArrayList<ApnSetting> waitingApns) {
        if (waitingApns == null) {
            log("No waiting APNs provided");
            return;
        }
        this.mWaitingApns = waitingApns;
        configureRetry(this.mApnType.equals("default"));
        for (ApnSetting apn : this.mWaitingApns) {
            apn.permanentFailed = false;
        }
        log("Setting " + this.mWaitingApns.size() + " waiting APNs.");
    }

    public ArrayList<ApnSetting> getWaitingApns() {
        return this.mWaitingApns;
    }

    public void setModemSuggestedDelay(long delay) {
        this.mModemSuggestedDelay = delay;
    }

    public long getInterApnDelay(boolean failFastEnabled) {
        long ApnDelayProper = SystemProperties.getLong("persist.radio.telecom_apn_delay", 0);
        long ApnDelayCfg = failFastEnabled ? this.mFailFastInterApnDelay : this.mInterApnDelay;
        if (ApnDelayProper > ApnDelayCfg) {
            return ApnDelayProper;
        }
        return ApnDelayCfg;
    }

    public String toString() {
        return "mApnType=" + this.mApnType + " mRetryCount=" + this.mRetryCount + " mMaxRetryCount=" + this.mMaxRetryCount + " mCurrentApnIndex=" + this.mCurrentApnIndex + " mSameApnRtryCount=" + this.mSameApnRetryCount + " mModemSuggestedDelay=" + this.mModemSuggestedDelay + " mRetryForever=" + this.mRetryForever + " mConfig={" + this.mConfig + "}";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("  RetryManager");
        pw.println("***************************************");
        pw.println("    config = " + this.mConfig);
        pw.println("    mApnType = " + this.mApnType);
        pw.println("    mCurrentApnIndex = " + this.mCurrentApnIndex);
        pw.println("    mRetryCount = " + this.mRetryCount);
        pw.println("    mMaxRetryCount = " + this.mMaxRetryCount);
        pw.println("    mSameApnRetryCount = " + this.mSameApnRetryCount);
        pw.println("    mModemSuggestedDelay = " + this.mModemSuggestedDelay);
        if (this.mWaitingApns != null) {
            pw.println("    APN list: ");
            for (int i = 0; i < this.mWaitingApns.size(); i++) {
                pw.println("      [" + i + "]=" + this.mWaitingApns.get(i));
            }
        }
        pw.println("***************************************");
        pw.flush();
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mApnType + "] " + s);
    }

    public boolean isLastApnSetting() {
        if (this.mCurrentApnIndex <= 0 || this.mWaitingApns == null || this.mCurrentApnIndex != this.mWaitingApns.size() - 1) {
            return false;
        }
        return DBG;
    }
}
