package com.android.internal.telephony;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.AbstractSubscriptionController;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimConstants;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HwSubscriptionControllerReference implements AbstractSubscriptionController.SubscriptionControllerReference {
    public static final String ACTIONG_SET_USER_PREF_DATA_SLOTID_FAILED = "com.huawei.android.dualcard.ACTIONG_SET_USER_PREF_DATA_SLOTID_FAILED";
    private static final boolean DBG = true;
    private static final int EVENT_CHECK_SET_MAIN_SLOT = 2;
    private static final int EVENT_CHECK_SET_MAIN_SLOT_RETRY = 3;
    private static final int EVENT_SET_DEFAULT_DATA_DONE = 1;
    private static final String GMS_APP_NAME = "com.google.android.setupwizard";
    private static final int INT_INVALID_VALUE = -1;
    private static final String LOG_TAG = "HwSubscriptionControllerReference";
    private static final int SET_MAIN_SLOT_RETRY_INTERVAL = 5000;
    private static final int SET_MAIN_SLOT_RETRY_MAX_TIMES = 10;
    public static final String SET_PREF_DATA_SLOTID_FAILED_RECEIVER_PERMISSION = "com.huawei.permission.CARDS_SETTINGS";
    private static final boolean VDBG = false;
    private static SubscriptionControllerUtils subscriptionControllerUtils = new SubscriptionControllerUtils();
    private MainHandler mMainHandler;
    private HashMap<Integer, AbstractSubscriptionController.OnDemandDdsLockNotifier> mOnDemandDdsLockNotificationRegistrants = new HashMap<>();
    /* access modifiers changed from: private */
    public int mSetMainSlotRetryTimes = 0;
    private SubscriptionController mSubscriptionController;
    private Object qcRilHook = null;

    private class DataConnectionHandler extends Handler {
        private DataConnectionHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                AsyncResult ar = (AsyncResult) msg.obj;
                HwSubscriptionControllerReference hwSubscriptionControllerReference = HwSubscriptionControllerReference.this;
                hwSubscriptionControllerReference.logd("EVENT_SET_DEFAULT_DATA_DONE subId:" + ((Integer) ar.result));
                HwSubscriptionControllerReference.this.updateDataSubId(ar);
            }
        }
    }

    private class MainHandler extends Handler {
        private MainHandler() {
        }

        public void handleMessage(Message msg) {
            HwSubscriptionControllerReference hwSubscriptionControllerReference = HwSubscriptionControllerReference.this;
            hwSubscriptionControllerReference.logd("handleMessage, msg,what = " + msg.what);
            switch (msg.what) {
                case 2:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar == null || ar.exception != null) {
                        HwSubscriptionControllerReference.this.loge("EVENT_CHECK_SET_MAIN_SLOT fail, try again.");
                        HwSubscriptionControllerReference.this.tryToSwitchMainSlot(msg.arg1);
                        return;
                    }
                    if (HwSubscriptionManager.getInstance() != null) {
                        HwSubscriptionManager.getInstance().setUserPrefDataSlotId(msg.arg1);
                    } else {
                        HwSubscriptionControllerReference.this.loge("HwSubscriptionManager is null!!");
                    }
                    int unused = HwSubscriptionControllerReference.this.mSetMainSlotRetryTimes = 0;
                    return;
                case 3:
                    if (hasMessages(3)) {
                        removeMessages(3);
                    }
                    HwSubscriptionControllerReference.this.tryToSwitchMainSlot(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    public HwSubscriptionControllerReference(SubscriptionController subscriptionController) {
        this.mSubscriptionController = subscriptionController;
        this.mMainHandler = new MainHandler();
    }

    public int getHwSlotId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            return this.mSubscriptionController.getDefaultSubId();
        }
        return subId;
    }

    public int[] getHwSubId(int slotIdx) {
        if (slotIdx == Integer.MAX_VALUE) {
            slotIdx = this.mSubscriptionController.getSlotIndex(this.mSubscriptionController.getDefaultSubId());
            logd("[getSubId] map default slotIdx=" + slotIdx);
        }
        if (!SubscriptionManager.isValidSlotIndex(slotIdx)) {
            logd("[getSubId]- invalid slotIdx=" + slotIdx);
            return null;
        }
        return new int[]{slotIdx, slotIdx};
    }

    public int getHwPhoneId(int subId) {
        if (subId != Integer.MAX_VALUE) {
            return subId;
        }
        int subId2 = this.mSubscriptionController.getDefaultSubId();
        logd("[getPhoneId] asked for default subId=" + subId2);
        return subId2;
    }

    public boolean isSMSPromptEnabled() {
        boolean prompt = false;
        int value = 0;
        try {
            value = Settings.Global.getInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        if (value != 0) {
            prompt = true;
        }
        return prompt;
    }

    public void setSMSPromptEnabled(boolean enabled) {
        Settings.Global.putInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_sms_prompt", (int) enabled);
        logd("setSMSPromptOption to " + enabled);
    }

    public void activateSubId(int subId) {
        logd("activateSubId: subId = " + subId);
        SubscriptionHelper.getInstance().setUiccSubscription(this.mSubscriptionController.getSlotIndex(subId), 1);
    }

    public void deactivateSubId(int subId) {
        logd("deactivateSubId: subId = " + subId);
        SubscriptionHelper.getInstance().setUiccSubscription(this.mSubscriptionController.getSlotIndex(subId), 0);
    }

    public void setNwMode(int subId, int nwMode) {
        logd("setNwMode, nwMode: " + nwMode + " subId: " + subId);
        ContentValues value = new ContentValues(1);
        value.put("network_mode", Integer.valueOf(nwMode));
        this.mSubscriptionController.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id= ?", new String[]{Integer.toString(subId)});
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
        logd("setSubState, subStatus: " + subStatus + " subId: " + subId);
        ContentValues value = new ContentValues(1);
        value.put("sub_state", Integer.valueOf(subStatus));
        int result = this.mSubscriptionController.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id= ?", new String[]{Integer.toString(subId)});
        this.mSubscriptionController.refreshCachedActiveSubscriptionInfoList();
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.putExtra("_id", subId);
        intent.putExtra("subscription", subId);
        intent.putExtra("columnName", "sub_state");
        intent.putExtra("intContent", subStatus);
        intent.putExtra("stringContent", "None");
        this.mSubscriptionController.mContext.sendBroadcast(intent);
        this.mSubscriptionController.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
        this.mSubscriptionController.mContext.sendBroadcast(new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
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
        boolean prompt = false;
        int value = 0;
        try {
            value = Settings.Global.getInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_voice_prompt");
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Voice Prompt Values");
        }
        if (value != 0) {
            prompt = true;
        }
        return prompt;
    }

    public void setVoicePromptEnabled(boolean enabled) {
        Settings.Global.putInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_voice_prompt", (int) enabled);
        logd("setVoicePromptOption to " + enabled);
    }

    /* access modifiers changed from: private */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public int getSubIdFromNetworkRequest(NetworkRequest n) {
        int subId;
        if (n == null) {
            return this.mSubscriptionController.getDefaultDataSubId();
        }
        String str = null;
        NetworkSpecifier ns = n.networkCapabilities.getNetworkSpecifier();
        if (ns instanceof StringNetworkSpecifier) {
            str = ns.toString();
        } else {
            loge("NetworkSpecifier not instance of StringNetworkSpecifier, got subid failed!");
        }
        try {
            subId = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            loge("getSubIdFromNetworkRequest get a NumberFormatException");
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

    /* access modifiers changed from: private */
    public void updateDataSubId(AsyncResult ar) {
        Integer subId = (Integer) ar.result;
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
        return true;
    }

    public void setDefaultDataSubIdHw(int subId) {
        if (NetworkFactory.isDualCellDataEnable()) {
            logd("Dual-Cell data is enabled so setDefaultDataSubId is return");
            this.mSubscriptionController.mContext.sendBroadcast(new Intent(ACTIONG_SET_USER_PREF_DATA_SLOTID_FAILED), SET_PREF_DATA_SLOTID_FAILED_RECEIVER_PERMISSION);
            return;
        }
        int i = 0;
        if (!HwModemCapability.isCapabilitySupport(0)) {
            Phone[] phones = PhoneFactory.getPhones();
            int length = phones.length;
            while (i < length) {
                Phone phone = phones[i];
                if (phone == null || phone.getState() == PhoneConstants.State.IDLE) {
                    i++;
                } else {
                    logd("[setDefaultDataSubId] phoneId:" + phone.getPhoneId() + " is calling, drop it and return");
                    this.mSubscriptionController.mContext.sendBroadcast(new Intent("com.android.huawei.DUAL_CARD_DATA_SUBSCRIPTION_CHANGE_FAILED"));
                    return;
                }
            }
        }
    }

    public void setDataSubId(int subId) {
        Settings.Global.putInt(this.mSubscriptionController.mContext.getContentResolver(), "multi_sim_data_call", subId);
    }

    public int getOnDemandDataSubId() {
        return getCurrentDds();
    }

    public void registerForOnDemandDdsLockNotification(int clientSubId, AbstractSubscriptionController.OnDemandDdsLockNotifier callback) {
        logd("registerForOnDemandDdsLockNotification for client=" + clientSubId);
        this.mOnDemandDdsLockNotificationRegistrants.put(Integer.valueOf(clientSubId), callback);
    }

    public void notifyOnDemandDataSubIdChanged(NetworkRequest n) {
        AbstractSubscriptionController.OnDemandDdsLockNotifier notifier = this.mOnDemandDdsLockNotificationRegistrants.get(Integer.valueOf(getSubIdFromNetworkRequest(n)));
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

    public int updateClatForMobile(int subId) {
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            int phoneId = SubscriptionManager.getPhoneId(subId);
            if (this.mSubscriptionController != null) {
                try {
                    String mccMnc = TelephonyManager.from(this.mSubscriptionController.mContext).getSimOperatorNumericForPhone(phoneId);
                    String plmnsConfig = Settings.System.getString(this.mSubscriptionController.mContext.getContentResolver(), "disable_mobile_clatd");
                    if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccMnc)) {
                        logd("plmnsConfig is null, return ");
                        return 3;
                    } else if (plmnsConfig.contains(mccMnc)) {
                        logd("disable clatd!");
                        SystemProperties.set("gsm.net.doxlat", "false");
                        return 1;
                    } else {
                        SystemProperties.set("gsm.net.doxlat", "true");
                        return 2;
                    }
                } catch (Exception e) {
                    loge("updateClatForMobile get an exception");
                }
            }
        }
        return 4;
    }

    public void setSubscriptionPropertyIntoSettingsGlobal(int subId, String propKey, String propValue) {
        if (isImsPropKey(propKey)) {
            String propKey2 = buildExtPropKey(subId, propKey);
            logd("[setSubscriptionPropertyIntoSettingsGlobal] propKey=" + propKey2 + ",propValue=" + propValue);
            Settings.Global.putInt(this.mSubscriptionController.mContext.getContentResolver(), propKey2, Integer.parseInt(propValue));
        }
    }

    public String getSubscriptionPropertyFromSettingsGlobal(int subId, String propKey) {
        if (!isImsPropKey(propKey)) {
            return null;
        }
        int result = Settings.Global.getInt(this.mSubscriptionController.mContext.getContentResolver(), buildExtPropKey(subId, propKey), -1);
        logd("[getSubscriptionPropertyFromSettingsGlobal] getResult=" + result);
        return Integer.toString(result);
    }

    public String buildExtPropKey(int subId, String propKey) {
        boolean isDualIms = HwImsManagerInner.isDualImsAvailable();
        StringBuilder sb = new StringBuilder(propKey);
        if (isDualIms) {
            sb.append("_");
            sb.append(subId);
        }
        logd("[buildPropKey] propKey=" + sb.toString());
        return sb.toString();
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public boolean isImsPropKey(String propKey) {
        char c;
        switch (propKey.hashCode()) {
            case -1950380197:
                if (propKey.equals("volte_vt_enabled")) {
                    c = 4;
                    break;
                }
            case -1218173306:
                if (propKey.equals("wfc_ims_enabled")) {
                    c = 0;
                    break;
                }
            case -420099376:
                if (propKey.equals("vt_ims_enabled")) {
                    c = 5;
                    break;
                }
            case 180938212:
                if (propKey.equals("wfc_ims_roaming_mode")) {
                    c = 2;
                    break;
                }
            case 1334635646:
                if (propKey.equals("wfc_ims_mode")) {
                    c = 1;
                    break;
                }
            case 1604840288:
                if (propKey.equals("wfc_ims_roaming_enabled")) {
                    c = 3;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    public Object getQcRilHook() {
        logd("Get QcRilHook Class");
        if (this.qcRilHook == null) {
            try {
                Object[] params = {this.mSubscriptionController.mContext};
                this.qcRilHook = new PathClassLoader("system/framework/qcrilhook.jar", ClassLoader.getSystemClassLoader()).loadClass("com.qualcomm.qcrilhook.QcRilHook").getConstructor(new Class[]{Context.class}).newInstance(params);
            } catch (ClassNotFoundException e) {
                loge("getQcRilHook ClassNotFoundException.");
            } catch (RuntimeException e2) {
                loge("getQcRilHook RuntimeException.");
            } catch (NoSuchMethodException e3) {
                loge("getQcRilHook NoSuchMethodException.");
            } catch (InstantiationException e4) {
                loge("getQcRilHook InstantiationException.");
            } catch (IllegalAccessException e5) {
                loge("getQcRilHook IllegalAccessException.");
            } catch (InvocationTargetException e6) {
                loge("getQcRilHook InvocationTargetException.");
            }
        }
        return this.qcRilHook;
    }

    public void informDdsToQcril(int ddsPhoneId, int reason) {
        if (this.qcRilHook != null) {
            try {
                Method qcRilSendDDSInfo = this.qcRilHook.getClass().getMethod("qcRilSendDDSInfo", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE});
                if (ddsPhoneId >= 0) {
                    SubscriptionController subscriptionController = this.mSubscriptionController;
                    if (ddsPhoneId < SubscriptionController.sPhones.length) {
                        int i = 0;
                        while (true) {
                            SubscriptionController subscriptionController2 = this.mSubscriptionController;
                            if (i < SubscriptionController.sPhones.length) {
                                logd("informDdsToQcril rild= " + i + ", ddsPhoneId=" + ddsPhoneId + ", reason = " + reason);
                                qcRilSendDDSInfo.invoke(this.qcRilHook, new Object[]{Integer.valueOf(ddsPhoneId), Integer.valueOf(reason), Integer.valueOf(i)});
                                i++;
                            } else {
                                return;
                            }
                        }
                    }
                }
                logd("informDdsToQcril dds phoneId is invalid = " + ddsPhoneId);
            } catch (NoSuchMethodException e) {
                loge("qcRilSendDDSInfo NoSuchMethodException.");
            } catch (RuntimeException e2) {
                loge("qcRilSendDDSInfo RuntimeException.");
            } catch (IllegalAccessException e3) {
                loge("qcRilSendDDSInfo IllegalAccessException.");
            } catch (InvocationTargetException e4) {
                loge("qcRilSendDDSInfo InvocationTargetException.");
            }
        } else {
            logd("informDdsToQcril qcRilHook is null.");
        }
    }

    private String getAppName(int pid) {
        String processName = "";
        List<ActivityManager.RunningAppProcessInfo> l = ((ActivityManager) this.mSubscriptionController.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (l == null) {
            return processName;
        }
        Iterator<ActivityManager.RunningAppProcessInfo> it = l.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo info = it.next();
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        return processName;
    }

    /* access modifiers changed from: private */
    public void tryToSwitchMainSlot(int subId) {
        if (subId == HwFullNetworkManager.getInstance().getUserSwitchDualCardSlots()) {
            logd("tryToSwitchMainSlot: subId(" + subId + ") is already main slot, return");
            this.mSetMainSlotRetryTimes = 0;
            return;
        }
        boolean couldSwitch = !HwFullNetworkManager.getInstance().get4GSlotInProgress() && !HwFullNetworkManager.getInstance().isRestartRildProgress();
        if (this.mSetMainSlotRetryTimes < 10) {
            if (couldSwitch) {
                HwFullNetworkManager.getInstance().setMainSlot(subId, this.mMainHandler.obtainMessage(2, subId, -1));
            } else {
                this.mMainHandler.sendMessageDelayed(this.mMainHandler.obtainMessage(3, subId, -1), HwVSimConstants.WAIT_FOR_SIM_STATUS_CHANGED_UNSOL_TIMEOUT);
            }
            this.mSetMainSlotRetryTimes++;
        } else {
            this.mSetMainSlotRetryTimes = 0;
        }
    }

    public void checkNeedSetMainSlotByPid(int subId, int pid) {
        String pkgName = getAppName(pid);
        logd("checkNeedSetMainSlotByPid, subId = " + subId + ", pid = " + pid + ", pkg = " + pkgName);
        if (GMS_APP_NAME.equals(pkgName)) {
            tryToSwitchMainSlot(subId);
        }
    }
}
