package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import java.util.List;

public class DefaultHwSubscriptionManager {
    protected static final int DUAL_SIM_SUBSCRIPTION_COUNT = 2;
    private static final String LOG_TAG = "DefaultHwSubscriptionManager";
    protected static final int SINGLE_SIM_SUBSCRIPTION_COUNT = 1;
    private static DefaultHwSubscriptionManager sInstance = new DefaultHwSubscriptionManager();
    protected Context mContext;
    protected SubscriptionControllerEx mSubscriptionController;

    public static DefaultHwSubscriptionManager getInstance() {
        return sInstance;
    }

    private static void logd(String message) {
        RlogEx.i(LOG_TAG, message);
    }

    public void init(Context context, CommandsInterfaceEx[] commandsInterfaceExes) {
        this.mContext = context;
        this.mSubscriptionController = SubscriptionControllerEx.getInstance();
        logd("Constructor - Complete");
    }

    public boolean setSubscription(int slotId, boolean isActivate, Message onCompleteMsg) {
        logd("setSubscription do nothing for single SIM mode : slot id = " + slotId);
        return false;
    }

    public void setUserPrefDataSlotId(int slotId) {
        logd("setUserPrefDataSlotId do nothing for single SIM mode : slot id = " + slotId);
    }

    public void setDefaultDataSubIdToDbBySlotId(int slotId) {
        logd("setDefaultDataSubIdToDbBySlotId do nothing for single SIM mode : slot id = " + slotId);
    }

    public void setDefaultDataSubIdBySlotId(int slotId) {
        logd("setDefaultDataSubIdBySlotId do nothing for single SIM mode : slot id = " + slotId);
    }

    public void setUserPrefDefaultSlotId(int slotId) {
        logd("setUserPrefDefaultSlotId do nothing for single SIM mode : slot id = " + slotId);
    }

    public void updateDataSlot() {
        logd("updateDataSlot do nothing for single SIM mode.");
    }

    private boolean isNeedSetDds(boolean isSetDds, int dataSubState, int dataSimState) {
        return isSetDds || dataSubState == 0 || dataSimState == 1;
    }

    /* access modifiers changed from: protected */
    public void setUserPreferences(int activeSubId, boolean isSetDds, int defaultDataSubId, int defaultVoiceSubId, int defaultSmsSubId) {
        int defaultSlotId = SubscriptionManagerEx.getSlotIndex(SubscriptionManager.getDefaultSubscriptionId());
        if (this.mSubscriptionController.getSubState(defaultSlotId) == 0 || TelephonyManagerEx.getDefault().getSimState(defaultSlotId) == 1) {
            this.mSubscriptionController.setDefaultFallbackSubId(activeSubId);
        }
        int defaultDataSlotId = SubscriptionManagerEx.getSlotIndex(defaultDataSubId);
        int dataSubState = this.mSubscriptionController.getSubState(defaultDataSlotId);
        int dataSimState = TelephonyManagerEx.getDefault().getSimState(defaultDataSlotId);
        if (!HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK && !HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING && isNeedSetDds(isSetDds, dataSubState, dataSimState)) {
            int dataSubId = defaultDataSubId;
            if (dataSubState == 0 || dataSimState == 1) {
                if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.isVSimEnabled()) {
                    dataSubId = activeSubId;
                } else {
                    logd("updateUserPreferences: vsim is enabled, block set dds, dataSubState = " + dataSubState);
                }
            }
            this.mSubscriptionController.setDefaultDataSubId(dataSubId);
        }
        int defaultVoiceSlotId = SubscriptionManagerEx.getSlotIndex(defaultVoiceSubId);
        if ((this.mSubscriptionController.getSubState(defaultVoiceSlotId) == 0 || TelephonyManagerEx.getDefault().getSimState(defaultVoiceSlotId) == 1) && !this.mSubscriptionController.isVoicePromptEnabled()) {
            this.mSubscriptionController.setDefaultVoiceSubId(activeSubId);
        }
        int defaultSmsSlotId = SubscriptionManagerEx.getSlotIndex(defaultSmsSubId);
        if ((this.mSubscriptionController.getSubState(defaultSmsSlotId) == 0 || TelephonyManagerEx.getDefault().getSimState(defaultSmsSlotId) == 1) && !this.mSubscriptionController.isSMSPromptEnabled()) {
            this.mSubscriptionController.setDefaultSmsSubId(activeSubId);
        }
    }

    public void updateUserPreferences(boolean isSetDds) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionController.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        int mActCount = 0;
        SubscriptionInfo nextActivatedSub = null;
        int defaultSubId = SubscriptionManager.getDefaultSubscriptionId();
        int defaultDataSubId = this.mSubscriptionController.getDefaultDataSubId();
        int defaultVoiceSubId = SubscriptionManager.getDefaultVoiceSubscriptionId();
        int defaultSmsSubId = SubscriptionManager.getDefaultSmsSubscriptionId();
        logd("updateUserPreferences: defaultSub = " + defaultSubId + ", defaultData = " + defaultDataSubId + ", defaultVoice = " + defaultVoiceSubId + ", defaultSms = " + defaultSmsSubId + ", setDDs = " + isSetDds);
        if (subInfoList == null) {
            this.mSubscriptionController.resetDefaultFallbackSubId();
            logd("updateUserPreferences: subscription are not avaiable!!! Exit !");
            return;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            if (this.mSubscriptionController.getSubState(subInfo.getSimSlotIndex()) == 1) {
                mActCount++;
                if (nextActivatedSub == null) {
                    nextActivatedSub = subInfo;
                }
            }
        }
        if (mActCount < 2) {
            this.mSubscriptionController.setSMSPromptEnabled(false);
            this.mSubscriptionController.setVoicePromptEnabled(false);
        }
        logd("updateUserPreferences: mActCount = " + mActCount);
        if (nextActivatedSub != null) {
            if (mActCount == 1) {
                setUserPreferences(nextActivatedSub.getSubscriptionId(), isSetDds, defaultDataSubId, defaultVoiceSubId, defaultSmsSubId);
            } else {
                logd("updateUserPreferences do nothing for single SIM mode.");
            }
            logd("updateUserPreferences: after current DataSub = " + this.mSubscriptionController.getDefaultDataSubId() + " VoiceSub = " + SubscriptionManager.getDefaultVoiceSubscriptionId() + " SmsSub = " + SubscriptionManager.getDefaultSmsSubscriptionId());
        }
    }
}
