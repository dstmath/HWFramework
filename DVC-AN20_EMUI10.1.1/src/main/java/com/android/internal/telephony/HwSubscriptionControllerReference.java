package com.android.internal.telephony;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionInfoEx;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwSubscriptionControllerReference implements IHwSubscriptionControllerEx {
    private static final boolean DBG = true;
    private static final int EVENT_CHECK_SET_MAIN_SLOT = 2;
    private static final int EVENT_CHECK_SET_MAIN_SLOT_RETRY = 3;
    private static final int EVENT_SET_DEFAULT_DATA_DONE = 1;
    private static final String GMS_APP_NAME = "com.google.android.setupwizard";
    private static final int INT_INVALID_VALUE = -1;
    private static final String LOG_TAG = "HwSubscriptionControllerReference";
    private static final int SET_MAIN_SLOT_RETRY_INTERVAL = 5000;
    private static final int SET_MAIN_SLOT_RETRY_MAX_TIMES = 10;
    private static final boolean VDBG = false;
    private Context mContext = null;
    private MainHandler mMainHandler;
    private int mSetMainSlotRetryTimes = 0;
    private ISubscriptionControllerInner mSubscriptionController;
    private Object qcRilHook = null;

    public HwSubscriptionControllerReference(Context context, ISubscriptionControllerInner subscriptionController) {
        this.mSubscriptionController = subscriptionController;
        this.mContext = context;
        this.mMainHandler = new MainHandler();
    }

    public boolean isSMSPromptEnabled() {
        int value = 0;
        try {
            value = Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        return value != 0;
    }

    public void setSMSPromptEnabled(boolean isEnabled) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms_prompt", isEnabled ? 1 : 0);
        logd("setSMSPromptOption to " + isEnabled);
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
        int slotId = this.mSubscriptionController.getSlotIndex(subId);
        logd("setNwMode, nwMode: " + nwMode + " subId: " + subId + " slotId: " + slotId);
        ContentValues value = new ContentValues(1);
        value.put("network_mode", Integer.valueOf(nwMode));
        this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id= ?", new String[]{Integer.toString(slotId)});
        this.mSubscriptionController.refreshCachedActiveSubscriptionInfoList();
    }

    public int getNwMode(int subId) {
        SubscriptionInfo subInfo = ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).getActiveSubscriptionInfo(subId);
        if (subInfo != null) {
            return SubscriptionInfoEx.getNwMode(subInfo);
        }
        loge("getNwMode: invalid subId = " + subId);
        return -1;
    }

    public int setSubState(int slotId, int subStatus) {
        int subId = this.mSubscriptionController.getSubIdUsingPhoneId(slotId);
        logd("setSubState, subStatus: " + subStatus + " subId: " + subId + " slotId: " + slotId);
        ContentValues value = new ContentValues(1);
        value.put("sub_state", Integer.valueOf(subStatus));
        int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "sim_id= ?", new String[]{Integer.toString(slotId)});
        this.mSubscriptionController.refreshCachedActiveSubscriptionInfoList();
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.putExtra("_id", subId);
        intent.putExtra("subscription", subId);
        intent.putExtra("slot", slotId);
        intent.putExtra("columnName", "sub_state");
        intent.putExtra("intContent", subStatus);
        intent.putExtra("stringContent", "None");
        this.mContext.sendBroadcast(intent);
        this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
        this.mContext.sendBroadcast(new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
        return result;
    }

    public int getSubState(int slotId) {
        SubscriptionInfo subInfo = ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoForSimSlotIndex(slotId);
        if (subInfo == null || subInfo.getSimSlotIndex() < 0) {
            return 0;
        }
        return SubscriptionInfoEx.getSubStatus(subInfo);
    }

    public boolean isVoicePromptEnabled() {
        int value = 0;
        try {
            value = Settings.Global.getInt(this.mContext.getContentResolver(), "multi_sim_voice_prompt");
        } catch (Settings.SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Voice Prompt Values");
        }
        return value != 0;
    }

    public void setVoicePromptEnabled(boolean isEnabled) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_prompt", isEnabled ? 1 : 0);
        logd("setVoicePromptOption to " + isEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDataSubId(AsyncResult ar) {
        Integer subId = (Integer) ar.result;
        logd(" updateDataSubId,  subId=" + subId + " exception " + ar.exception);
        if (ar.exception == null) {
            setDataSubId(subId.intValue());
        } else {
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenSetDataSubFail(subId.intValue());
        }
        broadcastDefaultDataSubIdChanged(subId.intValue());
        this.mSubscriptionController.updateAllDataConnectionTrackersHw();
    }

    public void setDataSubId(int subId) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
    }

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

    private void broadcastDefaultDataSubIdChanged(int subId) {
        logd("[broadcastDefaultDataSubIdChanged] subId=" + subId);
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intent.addFlags(536870912);
        intent.putExtra("subscription", this.mSubscriptionController.getDefaultDataSubId());
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int updateClatForMobile(int subId) {
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return 4;
        }
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (this.mSubscriptionController == null) {
            return 4;
        }
        try {
            String mccMnc = TelephonyManager.from(this.mContext).getSimOperatorNumericForPhone(phoneId);
            String plmnsConfig = Settings.System.getString(this.mContext.getContentResolver(), "disable_mobile_clatd");
            if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccMnc)) {
                logd("plmnsConfig is null, return ");
                return 3;
            } else if (plmnsConfig.contains(mccMnc)) {
                logd("disable clatd!");
                SystemPropertiesEx.set("gsm.net.doxlat", "false");
                return 1;
            } else {
                SystemPropertiesEx.set("gsm.net.doxlat", "true");
                return 2;
            }
        } catch (Exception e) {
            loge("updateClatForMobile get an exception");
            return 4;
        }
    }

    public void setSubscriptionPropertyIntoSettingsGlobal(int subId, String propKey, String propValue) {
        if (isImsPropKey(propKey)) {
            int slotId = this.mSubscriptionController.getSlotIndex(subId);
            if (!SubscriptionManager.isValidSlotIndex(slotId)) {
                logd("[setSubscriptionPropertyFromSettingsGlobal] slotId=" + slotId + " subId=" + subId);
                return;
            }
            String propKey2 = buildExtPropKey(slotId, propKey);
            logd("[setSubscriptionPropertyIntoSettingsGlobal] propKey=" + propKey2 + ",propValue=" + propValue + " slotId=" + slotId);
            try {
                Settings.Global.putInt(this.mContext.getContentResolver(), propKey2, Integer.parseInt(propValue));
            } catch (NumberFormatException e) {
                logd("NumberFormat Exception");
            }
        }
    }

    public String getSubscriptionPropertyFromSettingsGlobal(int subId, String propKey) {
        if (!isImsPropKey(propKey)) {
            return null;
        }
        int slotId = this.mSubscriptionController.getSlotIndex(subId);
        if (!SubscriptionManager.isValidSlotIndex(slotId)) {
            logd("[setSubscriptionPropertyFromSettingsGlobal] slotId=" + slotId + " subId=" + subId);
            return null;
        }
        int result = Settings.Global.getInt(this.mContext.getContentResolver(), buildExtPropKey(slotId, propKey), -1);
        logd("[getSubscriptionPropertyFromSettingsGlobal] getResult=" + result + " slotId=" + slotId);
        return Integer.toString(result);
    }

    public String buildExtPropKey(int slotId, String propKey) {
        boolean isDualIms = HwImsManagerInner.isDualImsAvailable();
        StringBuilder sb = new StringBuilder(propKey);
        if (isDualIms) {
            sb.append("_");
            sb.append(slotId);
        }
        logd("[buildPropKey] propKey=" + sb.toString());
        return sb.toString();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean isImsPropKey(String propKey) {
        char c;
        switch (propKey.hashCode()) {
            case -1950380197:
                if (propKey.equals("volte_vt_enabled")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1218173306:
                if (propKey.equals("wfc_ims_enabled")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -420099376:
                if (propKey.equals("vt_ims_enabled")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 180938212:
                if (propKey.equals("wfc_ims_roaming_mode")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1334635646:
                if (propKey.equals("wfc_ims_mode")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1604840288:
                if (propKey.equals("wfc_ims_roaming_enabled")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        return c == 0 || c == 1 || c == 2 || c == 3 || c == 4 || c == 5;
    }

    public void getQcRilHook() {
        logd("Get QcRilHook Class");
        if (this.qcRilHook == null) {
            try {
                Object[] params = {this.mContext};
                this.qcRilHook = new PathClassLoader("system/framework/qcrilhook.jar", ClassLoader.getSystemClassLoader()).loadClass("com.qualcomm.qcrilhook.QcRilHook").getConstructor(Context.class).newInstance(params);
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
    }

    public void informDdsToQcril(int ddsPhoneId, int reason) {
        Object obj = this.qcRilHook;
        if (obj != null) {
            try {
                Method qcRilSendDDSInfo = obj.getClass().getMethod("qcRilSendDDSInfo", Integer.TYPE, Integer.TYPE, Integer.TYPE);
                int phoneCount = TelephonyManager.getDefault().getPhoneCount();
                if (ddsPhoneId < 0 || ddsPhoneId >= phoneCount) {
                    logd("informDdsToQcril dds phoneId is invalid = " + ddsPhoneId);
                    return;
                }
                for (int i = 0; i < phoneCount; i++) {
                    logd("informDdsToQcril rild= " + i + ", ddsPhoneId=" + ddsPhoneId + ", reason = " + reason);
                    qcRilSendDDSInfo.invoke(this.qcRilHook, Integer.valueOf(ddsPhoneId), Integer.valueOf(reason), Integer.valueOf(i));
                }
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

    /* access modifiers changed from: private */
    public class MainHandler extends Handler {
        private MainHandler() {
        }

        public void handleMessage(Message msg) {
            HwSubscriptionControllerReference hwSubscriptionControllerReference = HwSubscriptionControllerReference.this;
            hwSubscriptionControllerReference.logd("handleMessage, msg,what = " + msg.what);
            int i = msg.what;
            if (i == 2) {
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
                HwSubscriptionControllerReference.this.mSetMainSlotRetryTimes = 0;
            } else if (i == 3) {
                if (hasMessages(3)) {
                    removeMessages(3);
                }
                HwSubscriptionControllerReference.this.tryToSwitchMainSlot(msg.arg1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryToSwitchMainSlot(int subId) {
        if (subId == HwFullNetworkManager.getInstance().getUserSwitchDualCardSlots()) {
            logd("tryToSwitchMainSlot: subId(" + subId + ") is already main slot, return");
            this.mSetMainSlotRetryTimes = 0;
            return;
        }
        boolean couldSwitch = !HwFullNetworkManager.getInstance().get4GSlotInProgress() && !HwFullNetworkManager.getInstance().isRestartRildProgress();
        if (this.mSetMainSlotRetryTimes < SET_MAIN_SLOT_RETRY_MAX_TIMES) {
            if (couldSwitch) {
                HwFullNetworkManager.getInstance().setMainSlot(subId, this.mMainHandler.obtainMessage(2, subId, -1));
            } else {
                MainHandler mainHandler = this.mMainHandler;
                mainHandler.sendMessageDelayed(mainHandler.obtainMessage(3, subId, -1), 5000);
            }
            this.mSetMainSlotRetryTimes++;
            return;
        }
        this.mSetMainSlotRetryTimes = 0;
    }

    public void checkNeedSetMainSlotByPid(int slotId, int pid) {
        String pkgName = HwTelephonyActivityManagerUtils.getAppName(this.mContext, pid);
        logd("checkNeedSetMainSlotByPid, subId = " + slotId + ", pid = " + pid + ", pkg = " + pkgName);
        if (GMS_APP_NAME.equals(pkgName)) {
            tryToSwitchMainSlot(slotId);
        }
    }
}
