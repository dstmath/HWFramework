package com.android.ims;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;

public class HwCustImsManagerImpl extends HwCustImsManager {
    private static final boolean IS_ATT_OPERATOR = ((!"07".equals(SystemProperties.get("ro.config.hw_opta")) || !"840".equals(SystemProperties.get("ro.config.hw_optb"))) ? IS_ATT_OPERATOR : true);
    private static final String LOG_TAG = "HwCustImsManagerImpl";
    private Context mContext;
    private int mPhoneId;

    public HwCustImsManagerImpl(Context context, int phoneId) {
        super(context, phoneId);
        this.mContext = context;
        this.mPhoneId = phoneId;
        log("Constructor:HwCustImsManagerImpl init for mPhoneId=" + this.mPhoneId);
    }

    private ImsManager getImsManager() {
        return ImsManager.getInstance(this.mContext, this.mPhoneId);
    }

    public boolean shouldNotTurnOnImsForCust() {
        if (IS_ATT_OPERATOR) {
            getImsManager();
            if (!ImsManager.isEnhanced4gLteModeSettingEnabledByUser(this.mContext)) {
                return true;
            }
        }
        return IS_ATT_OPERATOR;
    }

    public boolean shouldNotTurnOffImsForCust() {
        if (IS_ATT_OPERATOR) {
            getImsManager();
            if (ImsManager.isEnhanced4gLteModeSettingEnabledByUser(this.mContext)) {
                return true;
            }
        }
        return IS_ATT_OPERATOR;
    }

    public boolean isImsTurnOffAllowedForCust() {
        return IS_ATT_OPERATOR;
    }

    public void changeMmTelCapWithWfcForCust(boolean turnOn) {
        boolean enableWfc;
        if (IS_ATT_OPERATOR) {
            try {
                if (getImsManager().getConfigInterface() != null) {
                    getImsManager();
                    if (ImsManager.isWfcEnabledByPlatform(this.mContext)) {
                        log("set feature value of vowifi if wifi calling switcher is on.");
                        if (turnOn) {
                            getImsManager();
                            if (ImsManager.isWfcEnabledByUser(this.mContext)) {
                                enableWfc = true;
                                getImsManager().changeMmTelCapability(1, 1, enableWfc);
                            }
                        }
                        enableWfc = IS_ATT_OPERATOR;
                        getImsManager().changeMmTelCapability(1, 1, enableWfc);
                    }
                }
            } catch (ImsException e) {
                loge("changeMmTelCapWithWfcForCust: ImsException occurs.");
            }
        }
    }

    private void log(String s) {
        String tag = LOG_TAG;
        if (SubscriptionManager.isValidPhoneId(this.mPhoneId)) {
            tag = tag + "[SUB" + this.mPhoneId + "]";
        }
        Rlog.d(tag, s);
    }

    private void loge(String s) {
        String tag = LOG_TAG;
        if (SubscriptionManager.isValidPhoneId(this.mPhoneId)) {
            tag = tag + "[SUB" + this.mPhoneId + "]";
        }
        Rlog.e(tag, s);
    }
}
