package com.android.internal.telephony;

import android.content.ContentValues;
import android.content.Intent;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.AbstractSubscriptionController.OnDemandDdsLockNotifier;
import com.android.internal.telephony.AbstractSubscriptionController.SubscriptionControllerReference;
import com.android.internal.telephony.PhoneConstants.State;
import java.util.HashMap;

public class HwSubscriptionControllerReference implements SubscriptionControllerReference {
    public static final String ACTIONG_SET_USER_PREF_DATA_SLOTID_FAILED = "com.huawei.android.dualcard.ACTIONG_SET_USER_PREF_DATA_SLOTID_FAILED";
    private static final boolean DBG = true;
    private static final int EVENT_SET_DEFAULT_DATA_DONE = 1;
    private static final String LOG_TAG = "HwSubscriptionControllerReference";
    public static final String SET_PREF_DATA_SLOTID_FAILED_RECEIVER_PERMISSION = "com.huawei.permission.CARDS_SETTINGS";
    private static final boolean VDBG = false;
    private static SubscriptionControllerUtils subscriptionControllerUtils;
    private HashMap<Integer, OnDemandDdsLockNotifier> mOnDemandDdsLockNotificationRegistrants;
    private SubscriptionController mSubscriptionController;

    private class DataConnectionHandler extends Handler {
        private DataConnectionHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwSubscriptionControllerReference.EVENT_SET_DEFAULT_DATA_DONE /*1*/:
                    AsyncResult ar = msg.obj;
                    HwSubscriptionControllerReference.this.logd("EVENT_SET_DEFAULT_DATA_DONE subId:" + ((Integer) ar.result));
                    HwSubscriptionControllerReference.this.updateDataSubId(ar);
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwSubscriptionControllerReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwSubscriptionControllerReference.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwSubscriptionControllerReference.<clinit>():void");
    }

    public HwSubscriptionControllerReference(SubscriptionController subscriptionController) {
        this.mOnDemandDdsLockNotificationRegistrants = new HashMap();
        this.mSubscriptionController = subscriptionController;
    }

    public int getHwSlotId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            return this.mSubscriptionController.getDefaultSubId();
        }
        return subId;
    }

    public int[] getHwSubId(int slotIdx) {
        if (slotIdx == Integer.MAX_VALUE) {
            slotIdx = this.mSubscriptionController.getSlotId(this.mSubscriptionController.getDefaultSubId());
            logd("[getSubId] map default slotIdx=" + slotIdx);
        }
        if (SubscriptionManager.isValidSlotId(slotIdx)) {
            return new int[]{slotIdx, slotIdx};
        }
        logd("[getSubId]- invalid slotIdx=" + slotIdx);
        return null;
    }

    public int getHwPhoneId(int subId) {
        if (subId != Integer.MAX_VALUE) {
            return subId;
        }
        subId = this.mSubscriptionController.getDefaultSubId();
        logd("[getPhoneId] asked for default subId=" + subId);
        return subId;
    }

    public boolean isSMSPromptEnabled() {
        int value = 0;
        try {
            value = Global.getInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        return value == 0 ? false : DBG;
    }

    public void setSMSPromptEnabled(boolean enabled) {
        Global.putInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_sms_prompt", !enabled ? 0 : EVENT_SET_DEFAULT_DATA_DONE);
        logd("setSMSPromptOption to " + enabled);
    }

    public void activateSubId(int subId) {
        logd("activateSubId: subId = " + subId);
        SubscriptionHelper.getInstance().setUiccSubscription(this.mSubscriptionController.getSlotId(subId), EVENT_SET_DEFAULT_DATA_DONE);
    }

    public void deactivateSubId(int subId) {
        logd("deactivateSubId: subId = " + subId);
        SubscriptionHelper.getInstance().setUiccSubscription(this.mSubscriptionController.getSlotId(subId), 0);
    }

    public void setNwMode(int subId, int nwMode) {
        logd("setNwMode, nwMode: " + nwMode + " subId: " + subId);
        ContentValues value = new ContentValues(EVENT_SET_DEFAULT_DATA_DONE);
        value.put("network_mode", Integer.valueOf(nwMode));
        this.mSubscriptionController.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Integer.toString(subId), null);
    }

    public int getNwMode(int subId) {
        SubscriptionInfo subInfo = this.mSubscriptionController.getActiveSubscriptionInfo(subId, this.mSubscriptionController.mContext.getOpPackageName());
        if (subInfo != null) {
            return subInfo.mNwMode;
        }
        loge("getNwMode: invalid subId = " + subId);
        return -1;
    }

    public int setSubState(int subId, int subStatus) {
        int result = 0;
        logd("setSubState, subStatus: " + subStatus + " subId: " + subId);
        if (HwModemStackController.getInstance().isStackReady()) {
            ContentValues value = new ContentValues(EVENT_SET_DEFAULT_DATA_DONE);
            value.put("sub_state", Integer.valueOf(subStatus));
            result = this.mSubscriptionController.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id=" + Integer.toString(subId), null);
        }
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.putExtra("_id", subId);
        intent.putExtra("subscription", subId);
        intent.putExtra("columnName", "sub_state");
        intent.putExtra("intContent", subStatus);
        intent.putExtra("stringContent", "None");
        this.mSubscriptionController.mContext.sendBroadcast(intent);
        this.mSubscriptionController.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
        return result;
    }

    public int getSubState(int subId) {
        SubscriptionInfo subInfo = this.mSubscriptionController.getActiveSubscriptionInfo(subId, this.mSubscriptionController.mContext.getOpPackageName());
        if (subInfo == null || subInfo.getSimSlotIndex() < 0) {
            return 0;
        }
        return subInfo.mStatus;
    }

    public boolean isVoicePromptEnabled() {
        int value = 0;
        try {
            value = Global.getInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_voice_prompt");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Voice Prompt Values");
        }
        return value == 0 ? false : DBG;
    }

    public void setVoicePromptEnabled(boolean enabled) {
        Global.putInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_voice_prompt", !enabled ? 0 : EVENT_SET_DEFAULT_DATA_DONE);
        logd("setVoicePromptOption to " + enabled);
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public int getSubIdFromNetworkRequest(NetworkRequest n) {
        if (n == null) {
            return this.mSubscriptionController.getDefaultDataSubId();
        }
        int subId;
        try {
            subId = Integer.parseInt(n.networkCapabilities.getNetworkSpecifier());
        } catch (NumberFormatException e) {
            loge("Exception e = " + e);
            subId = this.mSubscriptionController.getDefaultDataSubId();
        }
        return subId;
    }

    public void startOnDemandDataSubscriptionRequest(NetworkRequest n) {
        logd("startOnDemandDataSubscriptionRequest = " + n);
    }

    public void stopOnDemandDataSubscriptionRequest(NetworkRequest n) {
        logd("stopOnDemandDataSubscriptionRequest = " + n);
    }

    public int getCurrentDds() {
        return PhoneFactory.getTopPrioritySubscriptionId();
    }

    private void updateDataSubId(AsyncResult ar) {
        Integer subId = ar.result;
        logd(" updateDataSubId,  subId=" + subId + " exception " + ar.exception);
        if (ar.exception == null) {
            setDataSubId(subId.intValue());
        } else {
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenSetDataSubFail(subId.intValue());
        }
        broadcastDefaultDataSubIdChanged(subId.intValue());
        subscriptionControllerUtils.updateAllDataConnectionTrackers(this.mSubscriptionController);
    }

    public boolean supportHwDualDataSwitch() {
        return DBG;
    }

    public void setDefaultDataSubIdHw(int subId) {
        int i = 0;
        if (NetworkFactory.isDualCellDataEnable()) {
            logd("Dual-Cell data is enabled so setDefaultDataSubId is return");
            this.mSubscriptionController.mContext.sendBroadcast(new Intent(ACTIONG_SET_USER_PREF_DATA_SLOTID_FAILED), SET_PREF_DATA_SLOTID_FAILED_RECEIVER_PERMISSION);
            return;
        }
        if (!HwModemCapability.isCapabilitySupport(0)) {
            Phone[] phones = PhoneFactory.getPhones();
            int length = phones.length;
            while (i < length) {
                Phone phone = phones[i];
                if (phone == null || phone.getState() == State.IDLE) {
                    i += EVENT_SET_DEFAULT_DATA_DONE;
                } else {
                    logd("[setDefaultDataSubId] phoneId:" + phone.getPhoneId() + " is calling, drop it and return");
                    this.mSubscriptionController.mContext.sendBroadcast(new Intent("com.android.huawei.DUAL_CARD_DATA_SUBSCRIPTION_CHANGE_FAILED"));
                    return;
                }
            }
        }
    }

    public void setDataSubId(int subId) {
        Global.putInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_data_call", subId);
    }

    public int getOnDemandDataSubId() {
        return getCurrentDds();
    }

    public void registerForOnDemandDdsLockNotification(int clientSubId, OnDemandDdsLockNotifier callback) {
        logd("registerForOnDemandDdsLockNotification for client=" + clientSubId);
        this.mOnDemandDdsLockNotificationRegistrants.put(Integer.valueOf(clientSubId), callback);
    }

    public void notifyOnDemandDataSubIdChanged(NetworkRequest n) {
        OnDemandDdsLockNotifier notifier = (OnDemandDdsLockNotifier) this.mOnDemandDdsLockNotificationRegistrants.get(Integer.valueOf(getSubIdFromNetworkRequest(n)));
        if (notifier != null) {
            notifier.notifyOnDemandDdsLockGranted(n);
        } else {
            logd("No registrants for OnDemandDdsLockGranted event");
        }
    }

    private void broadcastDefaultDataSubIdChanged(int subId) {
        logd("[broadcastDefaultDataSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intent.addFlags(536870912);
        intent.putExtra("subscription", this.mSubscriptionController.getDefaultDataSubId());
        this.mSubscriptionController.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }
}
