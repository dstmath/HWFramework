package com.android.internal.telephony.dataconnection;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import android.util.Pair;
import com.android.internal.telephony.GlobalSettingsHelper;
import com.android.internal.telephony.MultiSimSettingController;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DataEnabledSettings {
    private static final String LOG_TAG = "DataEnabledSettings";
    public static final int REASON_DATA_ENABLED_BY_CARRIER = 4;
    public static final int REASON_INTERNAL_DATA_ENABLED = 1;
    public static final int REASON_OVERRIDE_CONDITION_CHANGED = 8;
    public static final int REASON_OVERRIDE_RULE_CHANGED = 7;
    public static final int REASON_POLICY_DATA_ENABLED = 3;
    public static final int REASON_PROVISIONED_CHANGED = 5;
    public static final int REASON_PROVISIONING_DATA_ENABLED_CHANGED = 6;
    public static final int REASON_REGISTERED = 0;
    public static final int REASON_USER_DATA_ENABLED = 2;
    private boolean mCarrierDataEnabled = true;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        /* class com.android.internal.telephony.dataconnection.DataEnabledSettings.AnonymousClass3 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            DataEnabledSettings.this.updateDataEnabled();
        }
    };
    private DataEnabledOverride mDataEnabledOverride;
    private boolean mInternalDataEnabled = true;
    private boolean mIsDataEnabled = false;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.dataconnection.DataEnabledSettings.AnonymousClass1 */

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            synchronized (this) {
                if (DataEnabledSettings.this.mSubId != DataEnabledSettings.this.mPhone.getSubId()) {
                    DataEnabledSettings dataEnabledSettings = DataEnabledSettings.this;
                    dataEnabledSettings.log("onSubscriptionsChanged subId: " + DataEnabledSettings.this.mSubId + " to: " + DataEnabledSettings.this.mPhone.getSubId());
                    DataEnabledSettings.this.mSubId = DataEnabledSettings.this.mPhone.getSubId();
                    DataEnabledSettings.this.mDataEnabledOverride = DataEnabledSettings.this.getDataEnabledOverride();
                    DataEnabledSettings.this.updatePhoneStateListener();
                    DataEnabledSettings.this.updateDataEnabledAndNotify(2);
                    DataEnabledSettings.this.mPhone.notifyUserMobileDataStateChanged(DataEnabledSettings.this.isUserDataEnabled());
                }
            }
        }
    };
    private final RegistrantList mOverallDataEnabledChangedRegistrants = new RegistrantList();
    private final RegistrantList mOverallDataEnabledOverrideChangedRegistrants = new RegistrantList();
    private final Phone mPhone;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.internal.telephony.dataconnection.DataEnabledSettings.AnonymousClass2 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String phoneNumber) {
            DataEnabledSettings.this.updateDataEnabledAndNotify(8);
        }
    };
    private boolean mPolicyDataEnabled = true;
    private ContentResolver mResolver = null;
    private final LocalLog mSettingChangeLocalLog = new LocalLog(10);
    private int mSubId = -1;
    private TelephonyManager mTelephonyManager;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DataEnabledChangedReason {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePhoneStateListener() {
        this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        if (SubscriptionManager.isUsableSubscriptionId(this.mSubId)) {
            this.mTelephonyManager = this.mTelephonyManager.createForSubscriptionId(this.mSubId);
        }
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
    }

    public String toString() {
        return "[mInternalDataEnabled=" + this.mInternalDataEnabled + ", isUserDataEnabled=" + isUserDataEnabled() + ", isProvisioningDataEnabled=" + isProvisioningDataEnabled() + ", mPolicyDataEnabled=" + this.mPolicyDataEnabled + ", mCarrierDataEnabled=" + this.mCarrierDataEnabled + ", mIsDataEnabled=" + this.mIsDataEnabled + ", " + this.mDataEnabledOverride + "]";
    }

    public DataEnabledSettings(Phone phone) {
        this.mPhone = phone;
        this.mResolver = this.mPhone.getContext().getContentResolver();
        ((SubscriptionManager) this.mPhone.getContext().getSystemService("telephony_subscription_service")).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        this.mTelephonyManager = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        this.mDataEnabledOverride = getDataEnabledOverride();
        updateDataEnabled();
        this.mResolver.registerContentObserver(Settings.Global.getUriFor(getMobileDataSettingName()), false, this.mContentObserver);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DataEnabledOverride getDataEnabledOverride() {
        return new DataEnabledOverride(SubscriptionController.getInstance().getDataEnabledOverrideRules(this.mPhone.getSubId()));
    }

    public synchronized void setInternalDataEnabled(boolean enabled) {
        localLog("InternalDataEnabled", enabled);
        if (this.mInternalDataEnabled != enabled) {
            this.mInternalDataEnabled = enabled;
            updateDataEnabledAndNotify(1);
        }
    }

    public synchronized boolean isInternalDataEnabled() {
        return this.mInternalDataEnabled;
    }

    public synchronized void setUserDataEnabled(boolean enabled) {
        if (!isStandAloneOpportunistic(this.mPhone.getSubId(), this.mPhone.getContext()) || enabled) {
            localLog("UserDataEnabled", enabled);
            if (Settings.Global.getInt(this.mResolver, getMobileDataSettingName(), -1) != enabled) {
                int i = 0;
                if (TelephonyManager.getDefault().getSimCount() == 1) {
                    ContentResolver contentResolver = this.mResolver;
                    if (enabled) {
                        i = 1;
                    }
                    Settings.Global.putInt(contentResolver, "mobile_data", i);
                } else if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
                    log("vsim does not save mobile data");
                } else {
                    int phoneCount = TelephonyManager.getDefault().getPhoneCount();
                    for (int i2 = 0; i2 < phoneCount; i2++) {
                        Settings.Global.putInt(this.mResolver, "mobile_data" + i2, enabled ? 1 : 0);
                    }
                    ContentResolver contentResolver2 = this.mResolver;
                    if (enabled) {
                        i = 1;
                    }
                    Settings.Global.putInt(contentResolver2, "mobile_data", i);
                }
                this.mPhone.notifyUserMobileDataStateChanged(enabled);
                updateDataEnabledAndNotify(2);
            }
        }
    }

    public synchronized boolean isUserDataEnabled() {
        if (isStandAloneOpportunistic(this.mPhone.getSubId(), this.mPhone.getContext())) {
            return true;
        }
        if (isAnySimDetected() || !isProvisioning()) {
            return GlobalSettingsHelper.getBoolean(this.mPhone.getContext(), "mobile_data", this.mPhone.getPhoneId(), "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.mobiledata", "true")));
        }
        return isProvisioningDataEnabled();
    }

    private String getMobileDataSettingName() {
        int subId = this.mPhone.getSubId();
        if (TelephonyManager.getDefault().getSimCount() == 1 || !SubscriptionManager.isValidSubscriptionId(subId)) {
            return "mobile_data";
        }
        return "mobile_data" + this.mPhone.getPhoneId();
    }

    public synchronized boolean setAlwaysAllowMmsData(boolean alwaysAllow) {
        boolean changed;
        localLog("setAlwaysAllowMmsData", alwaysAllow);
        this.mDataEnabledOverride.setAlwaysAllowMms(alwaysAllow);
        changed = SubscriptionController.getInstance().setDataEnabledOverrideRules(this.mPhone.getSubId(), this.mDataEnabledOverride.getRules());
        if (changed) {
            updateDataEnabledAndNotify(7);
            notifyDataEnabledOverrideChanged();
        }
        return changed;
    }

    public synchronized boolean setAllowDataDuringVoiceCall(boolean allow) {
        boolean changed;
        localLog("setAllowDataDuringVoiceCall", allow);
        this.mDataEnabledOverride.setDataAllowedInVoiceCall(allow);
        changed = SubscriptionController.getInstance().setDataEnabledOverrideRules(this.mPhone.getSubId(), this.mDataEnabledOverride.getRules());
        if (changed) {
            updateDataEnabledAndNotify(7);
            notifyDataEnabledOverrideChanged();
        }
        return changed;
    }

    public synchronized boolean isDataAllowedInVoiceCall() {
        return this.mDataEnabledOverride.isDataAllowedInVoiceCall();
    }

    public synchronized void setPolicyDataEnabled(boolean enabled) {
        localLog("PolicyDataEnabled", enabled);
        if (this.mPolicyDataEnabled != enabled) {
            this.mPolicyDataEnabled = enabled;
            updateDataEnabledAndNotify(3);
        }
    }

    public synchronized boolean isPolicyDataEnabled() {
        return this.mPolicyDataEnabled;
    }

    public synchronized void setCarrierDataEnabled(boolean enabled) {
        localLog("CarrierDataEnabled", enabled);
        if (this.mCarrierDataEnabled != enabled) {
            this.mCarrierDataEnabled = enabled;
            updateDataEnabledAndNotify(4);
        }
    }

    public synchronized boolean isCarrierDataEnabled() {
        return this.mCarrierDataEnabled;
    }

    public synchronized void updateProvisionedChanged() {
        updateDataEnabledAndNotify(5);
    }

    public synchronized void updateProvisioningDataEnabled() {
        updateDataEnabledAndNotify(6);
    }

    public synchronized boolean isDataEnabled() {
        return this.mIsDataEnabled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void updateDataEnabledAndNotify(int reason) {
        boolean prevDataEnabled = this.mIsDataEnabled;
        updateDataEnabled();
        if (prevDataEnabled != this.mIsDataEnabled) {
            notifyDataEnabledChanged(!prevDataEnabled, reason);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void updateDataEnabled() {
        if (isAnySimDetected() || !isProvisioning()) {
            this.mIsDataEnabled = this.mInternalDataEnabled && (isUserDataEnabled() || this.mDataEnabledOverride.shouldOverrideDataEnabledSettings(this.mPhone, 8356095)) && this.mPolicyDataEnabled && this.mCarrierDataEnabled;
        } else {
            this.mIsDataEnabled = isProvisioningDataEnabled();
        }
    }

    public boolean isAnySimDetected() {
        return "true".equals(Settings.System.getString(this.mResolver, "any_sim_detect"));
    }

    public boolean isProvisioning() {
        return Settings.Global.getInt(this.mResolver, "device_provisioned", 0) == 0;
    }

    public boolean isProvisioningDataEnabled() {
        String prov_property = SystemProperties.get("ro.com.android.prov_mobiledata", "false");
        int prov_mobile_data = Settings.Global.getInt(this.mResolver, "device_provisioning_mobile_data", "true".equalsIgnoreCase(prov_property) ? 1 : 0);
        boolean retVal = prov_mobile_data != 0;
        log("getDataEnabled during provisioning retVal=" + retVal + " - (" + prov_property + ", " + prov_mobile_data + ")");
        return retVal;
    }

    public synchronized void setDataRoamingEnabled(boolean enabled) {
        localLog("setDataRoamingEnabled", enabled);
        if (GlobalSettingsHelper.setBoolean(this.mPhone.getContext(), "data_roaming", this.mPhone.getPhoneId(), enabled)) {
            MultiSimSettingController.getInstance().notifyRoamingDataEnabled(this.mPhone.getSubId(), enabled);
        }
    }

    public synchronized boolean getDataRoamingEnabled() {
        return GlobalSettingsHelper.getBoolean(this.mPhone.getContext(), "data_roaming", this.mPhone.getPhoneId(), getDefaultDataRoamingEnabled());
    }

    public synchronized boolean getDefaultDataRoamingEnabled() {
        boolean isDataRoamingEnabled;
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        isDataRoamingEnabled = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false"));
        if (configMgr.getConfigForSubId(this.mPhone.getSubId()) != null) {
            isDataRoamingEnabled |= configMgr.getConfigForSubId(this.mPhone.getSubId()).getBoolean("carrier_default_data_roaming_enabled_bool");
        }
        return isDataRoamingEnabled;
    }

    private void notifyDataEnabledChanged(boolean enabled, int reason) {
        this.mOverallDataEnabledChangedRegistrants.notifyResult(new Pair(Boolean.valueOf(enabled), Integer.valueOf(reason)));
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mOverallDataEnabledChangedRegistrants.addUnique(h, what, obj);
        notifyDataEnabledChanged(isDataEnabled(), 0);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mOverallDataEnabledChangedRegistrants.remove(h);
    }

    private void notifyDataEnabledOverrideChanged() {
        this.mOverallDataEnabledOverrideChangedRegistrants.notifyRegistrants();
    }

    public void registerForDataEnabledOverrideChanged(Handler h, int what) {
        this.mOverallDataEnabledOverrideChangedRegistrants.addUnique(h, what, (Object) null);
        notifyDataEnabledOverrideChanged();
    }

    public void unregisterForDataEnabledOverrideChanged(Handler h) {
        this.mOverallDataEnabledOverrideChangedRegistrants.remove(h);
    }

    private static boolean isStandAloneOpportunistic(int subId, Context context) {
        SubscriptionInfo info = SubscriptionController.getInstance().getActiveSubscriptionInfo(subId, context.getOpPackageName());
        return info != null && info.isOpportunistic() && info.getGroupUuid() == null;
    }

    public synchronized boolean isDataEnabled(int apnType) {
        if (isAnySimDetected() || !isProvisioning()) {
            return this.mInternalDataEnabled && this.mPolicyDataEnabled && this.mCarrierDataEnabled && (isUserDataEnabled() || this.mDataEnabledOverride.shouldOverrideDataEnabledSettings(this.mPhone, apnType));
        }
        return isProvisioningDataEnabled();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        Rlog.i(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void localLog(String name, boolean value) {
        LocalLog localLog = this.mSettingChangeLocalLog;
        localLog.log(name + " change to " + value);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(" DataEnabledSettings=");
        this.mSettingChangeLocalLog.dump(fd, pw, args);
    }
}
