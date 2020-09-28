package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.telephony.PhoneCapability;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class PhoneConfigurationManager {
    public static final String DSDA = "dsda";
    public static final String DSDS = "dsds";
    private static final int EVENT_GET_MODEM_STATUS = 101;
    private static final int EVENT_GET_MODEM_STATUS_DONE = 102;
    private static final int EVENT_GET_PHONE_CAPABILITY_DONE = 103;
    private static final int EVENT_SWITCH_DSDS_CONFIG_DONE = 100;
    private static final String LOG_TAG = "PhoneCfgMgr";
    public static final String SSSS = "";
    public static final String TSTS = "tsts";
    private static PhoneConfigurationManager sInstance = null;
    private final Context mContext;
    private final MainThreadHandler mHandler = new MainThreadHandler();
    private final Map<Integer, Boolean> mPhoneStatusMap = new HashMap();
    private final Phone[] mPhones;
    private final RadioConfig mRadioConfig = RadioConfig.getInstance(this.mContext);
    private PhoneCapability mStaticCapability = getDefaultCapability();

    public static PhoneConfigurationManager init(Context context) {
        PhoneConfigurationManager phoneConfigurationManager;
        synchronized (PhoneConfigurationManager.class) {
            if (sInstance == null) {
                sInstance = new PhoneConfigurationManager(context);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            phoneConfigurationManager = sInstance;
        }
        return phoneConfigurationManager;
    }

    private PhoneConfigurationManager(Context context) {
        this.mContext = context;
        new TelephonyManager(context);
        notifyCapabilityChanged();
        this.mPhones = PhoneFactory.getPhones();
        int i = 0;
        if (!StorageManager.inCryptKeeperBounce()) {
            Phone[] phoneArr = this.mPhones;
            int length = phoneArr.length;
            while (i < length) {
                Phone phone = phoneArr[i];
                phone.mCi.registerForAvailable(this.mHandler, 1, phone);
                i++;
            }
            return;
        }
        Phone[] phoneArr2 = this.mPhones;
        int length2 = phoneArr2.length;
        while (i < length2) {
            Phone phone2 = phoneArr2[i];
            phone2.mCi.registerForOn(this.mHandler, 5, phone2);
            i++;
        }
    }

    private PhoneCapability getDefaultCapability() {
        if (getPhoneCount() > 1) {
            return PhoneCapability.DEFAULT_DSDS_CAPABILITY;
        }
        return PhoneCapability.DEFAULT_SSSS_CAPABILITY;
    }

    public static PhoneConfigurationManager getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    public final class MainThreadHandler extends Handler {
        private MainThreadHandler() {
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1 || i == 5) {
                PhoneConfigurationManager.log("Received EVENT_RADIO_AVAILABLE/EVENT_RADIO_ON");
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.userObj == null || !(ar.userObj instanceof Phone)) {
                    PhoneConfigurationManager.log("Unable to add phoneStatus to cache. No phone object provided for event " + msg.what);
                } else {
                    PhoneConfigurationManager.this.updatePhoneStatus((Phone) ar.userObj);
                }
                PhoneConfigurationManager.this.getStaticPhoneCapability();
            } else if (i == 100) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2 == null || ar2.exception != null) {
                    PhoneConfigurationManager.log(msg.what + " failure. Not switching multi-sim config." + ar2.exception);
                    return;
                }
                PhoneConfigurationManager.this.setMultiSimProperties(msg.arg1);
            } else if (i == 102) {
                AsyncResult ar3 = (AsyncResult) msg.obj;
                if (ar3 == null || ar3.exception != null) {
                    PhoneConfigurationManager.log(msg.what + " failure. Not updating modem status." + ar3.exception);
                    return;
                }
                PhoneConfigurationManager.this.addToPhoneStatusCache(msg.arg1, ((Boolean) ar3.result).booleanValue());
            } else if (i == PhoneConfigurationManager.EVENT_GET_PHONE_CAPABILITY_DONE) {
                AsyncResult ar4 = (AsyncResult) msg.obj;
                if (ar4 == null || ar4.exception != null) {
                    PhoneConfigurationManager.log(msg.what + " failure. Not getting phone capability." + ar4.exception);
                    return;
                }
                PhoneConfigurationManager.this.mStaticCapability = (PhoneCapability) ar4.result;
                PhoneConfigurationManager.this.notifyCapabilityChanged();
            }
        }
    }

    public void enablePhone(Phone phone, boolean enable, Message result) {
        if (phone == null) {
            log("enablePhone failed phone is null");
        } else {
            phone.mCi.enableModem(enable, result);
        }
    }

    public boolean getPhoneStatus(Phone phone) {
        if (phone == null) {
            log("getPhoneStatus failed phone is null");
            return false;
        }
        try {
            return getPhoneStatusFromCache(phone.getPhoneId());
        } catch (NoSuchElementException e) {
            updatePhoneStatus(phone);
            return true;
        }
    }

    public void getPhoneStatusFromModem(Phone phone, Message result) {
        if (phone == null) {
            log("getPhoneStatus failed phone is null");
        } else {
            phone.mCi.getModemStatus(result);
        }
    }

    public boolean getPhoneStatusFromCache(int phoneId) throws NoSuchElementException {
        if (this.mPhoneStatusMap.containsKey(Integer.valueOf(phoneId))) {
            return this.mPhoneStatusMap.get(Integer.valueOf(phoneId)).booleanValue();
        }
        throw new NoSuchElementException("phoneId not found: " + phoneId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePhoneStatus(Phone phone) {
        phone.mCi.getModemStatus(Message.obtain(this.mHandler, 102, phone.getPhoneId(), 0));
    }

    public void addToPhoneStatusCache(int phoneId, boolean status) {
        this.mPhoneStatusMap.put(Integer.valueOf(phoneId), Boolean.valueOf(status));
    }

    public int getPhoneCount() {
        return new TelephonyManager(this.mContext).getPhoneCount();
    }

    public synchronized PhoneCapability getStaticPhoneCapability() {
        if (getDefaultCapability().equals(this.mStaticCapability)) {
            log("getStaticPhoneCapability: sending the request for getting PhoneCapability");
            this.mRadioConfig.getPhoneCapability(Message.obtain(this.mHandler, (int) EVENT_GET_PHONE_CAPABILITY_DONE));
        }
        return this.mStaticCapability;
    }

    public PhoneCapability getCurrentPhoneCapability() {
        return getStaticPhoneCapability();
    }

    public int getNumberOfModemsWithSimultaneousDataConnections() {
        return this.mStaticCapability.maxActiveData;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyCapabilityChanged() {
        new DefaultPhoneNotifier().notifyPhoneCapabilityChanged(this.mStaticCapability);
    }

    public void switchMultiSimConfig(int numOfSims) {
        log("switchMultiSimConfig: with numOfSims = " + numOfSims);
        if (getStaticPhoneCapability().logicalModemList.size() < numOfSims) {
            log("switchMultiSimConfig: Phone is not capable of enabling " + numOfSims + " sims, exiting!");
        } else if (getPhoneCount() != numOfSims) {
            log("switchMultiSimConfig: sending the request for switching");
            this.mRadioConfig.setModemsConfig(numOfSims, Message.obtain(this.mHandler, 100, numOfSims, 0));
        } else {
            log("switchMultiSimConfig: No need to switch. getNumOfActiveSims is already " + numOfSims);
        }
    }

    public boolean isRebootRequiredForModemConfigChange() {
        String rebootRequired = SystemProperties.get("persist.radio.reboot_on_modem_change");
        log("isRebootRequiredForModemConfigChange: isRebootRequired = " + rebootRequired);
        return !rebootRequired.equals("false");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMultiSimProperties(int numOfSims) {
        String finalMultiSimConfig;
        if (numOfSims == 2) {
            finalMultiSimConfig = DSDS;
        } else if (numOfSims != 3) {
            finalMultiSimConfig = SSSS;
        } else {
            finalMultiSimConfig = TSTS;
        }
        SystemProperties.set("persist.radio.multisim.config", finalMultiSimConfig);
        if (isRebootRequiredForModemConfigChange()) {
            log("setMultiSimProperties: Rebooting due to switching multi-sim config to " + finalMultiSimConfig);
            ((PowerManager) this.mContext.getSystemService("power")).reboot("Switching to " + finalMultiSimConfig);
            return;
        }
        log("setMultiSimProperties: Rebooting is not required to switch multi-sim config to " + finalMultiSimConfig);
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
