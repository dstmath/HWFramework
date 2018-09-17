package android.telephony;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.IOnSubscriptionsChangedListener.Stub;
import com.android.internal.telephony.ISub;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionManager {
    public static final String ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED = "android.telephony.action.DEFAULT_SMS_SUBSCRIPTION_CHANGED";
    public static final String ACTION_DEFAULT_SUBSCRIPTION_CHANGED = "android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED";
    public static final int ACTIVE = 1;
    public static final String CARRIER_NAME = "carrier_name";
    public static final String CB_ALERT_REMINDER_INTERVAL = "alert_reminder_interval";
    public static final String CB_ALERT_SOUND_DURATION = "alert_sound_duration";
    public static final String CB_ALERT_SPEECH = "enable_alert_speech";
    public static final String CB_ALERT_VIBRATE = "enable_alert_vibrate";
    public static final String CB_AMBER_ALERT = "enable_cmas_amber_alerts";
    public static final String CB_CHANNEL_50_ALERT = "enable_channel_50_alerts";
    public static final String CB_CMAS_TEST_ALERT = "enable_cmas_test_alerts";
    public static final String CB_EMERGENCY_ALERT = "enable_emergency_alerts";
    public static final String CB_ETWS_TEST_ALERT = "enable_etws_test_alerts";
    public static final String CB_EXTREME_THREAT_ALERT = "enable_cmas_extreme_threat_alerts";
    public static final String CB_OPT_OUT_DIALOG = "show_cmas_opt_out_dialog";
    public static final String CB_SEVERE_THREAT_ALERT = "enable_cmas_severe_threat_alerts";
    public static final String COLOR = "color";
    public static final int COLOR_1 = 0;
    public static final int COLOR_2 = 1;
    public static final int COLOR_3 = 2;
    public static final int COLOR_4 = 3;
    public static final int COLOR_DEFAULT = 0;
    public static final Uri CONTENT_URI = Uri.parse("content://telephony/siminfo");
    public static final String DATA_ROAMING = "data_roaming";
    public static final int DATA_ROAMING_DEFAULT = 0;
    public static final int DATA_ROAMING_DISABLE = 0;
    public static final int DATA_ROAMING_ENABLE = 1;
    private static final boolean DBG = false;
    public static final int DEFAULT_NAME_RES = 17039374;
    public static final int DEFAULT_NW_MODE = -1;
    public static final int DEFAULT_PHONE_INDEX = Integer.MAX_VALUE;
    public static final int DEFAULT_SIM_SLOT_INDEX = Integer.MAX_VALUE;
    public static final int DEFAULT_SUBSCRIPTION_ID = Integer.MAX_VALUE;
    public static final String DISPLAY_NAME = "display_name";
    public static final int DISPLAY_NUMBER_DEFAULT = 1;
    public static final int DISPLAY_NUMBER_FIRST = 1;
    public static final String DISPLAY_NUMBER_FORMAT = "display_number_format";
    public static final int DISPLAY_NUMBER_LAST = 2;
    public static final int DISPLAY_NUMBER_NONE = 0;
    public static final int DUMMY_SUBSCRIPTION_ID_BASE = -2;
    public static final String EXTRA_SUBSCRIPTION_INDEX = "android.telephony.extra.SUBSCRIPTION_INDEX";
    public static final int EXTRA_VALUE_NOCHANGE = 4;
    public static final String ICC_ID = "icc_id";
    public static final int INACTIVE = 0;
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    public static final int INVALID_PHONE_INDEX = -1;
    public static final int INVALID_SIM_SLOT_INDEX = -1;
    public static final int INVALID_SLOT_ID = -1000;
    public static final int INVALID_SUBSCRIPTION_ID = -1;
    private static final String LOG_TAG = "SubscriptionManager";
    public static final int MAX_SUBSCRIPTION_ID_VALUE = 2147483646;
    public static final String MCC = "mcc";
    public static final int MIN_SUBSCRIPTION_ID_VALUE = 0;
    public static final String MNC = "mnc";
    public static final String NAME_SOURCE = "name_source";
    public static final int NAME_SOURCE_DEFAULT_SOURCE = 0;
    public static final int NAME_SOURCE_SIM_SOURCE = 1;
    public static final int NAME_SOURCE_UNDEFINDED = -1;
    public static final int NAME_SOURCE_USER_INPUT = 2;
    public static final String NETWORK_MODE = "network_mode";
    public static final String NUMBER = "number";
    public static final int SIM_NOT_INSERTED = -1;
    public static final int SIM_PROVISIONED = 0;
    public static final String SIM_PROVISIONING_STATUS = "sim_provisioning_status";
    public static final String SIM_SLOT_INDEX = "sim_id";
    public static final int SUB_CONFIGURATION_IN_PROGRESS = 2;
    public static final String SUB_DEFAULT_CHANGED_ACTION = "android.intent.action.SUB_DEFAULT_CHANGED";
    public static final String SUB_STATE = "sub_state";
    public static final String UNIQUE_KEY_SUBSCRIPTION_ID = "_id";
    private static final boolean VDBG = true;
    private final Context mContext;

    public static class OnSubscriptionsChangedListener {
        IOnSubscriptionsChangedListener callback = new Stub() {
            public void onSubscriptionsChanged() {
                OnSubscriptionsChangedListener.this.mHandler.sendEmptyMessage(0);
            }
        };
        private final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                OnSubscriptionsChangedListener.this.onSubscriptionsChanged();
            }
        };

        public void onSubscriptionsChanged() {
        }

        private void log(String s) {
            Rlog.d(SubscriptionManager.LOG_TAG, s);
        }
    }

    public SubscriptionManager(Context context) {
        this.mContext = context;
    }

    public static SubscriptionManager from(Context context) {
        return (SubscriptionManager) context.getSystemService("telephony_subscription_service");
    }

    public void addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : MediaStore.UNKNOWN_STRING;
        try {
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
            if (tr != null) {
                tr.addOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException e) {
        }
    }

    public void removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : MediaStore.UNKNOWN_STRING;
        try {
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
            if (tr != null) {
                tr.removeOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfo(int subId) {
        logd("[getActiveSubscriptionInfo]+ subId=" + subId);
        if (!isValidSubscriptionId(subId)) {
            return null;
        }
        SubscriptionInfo subInfo = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subInfo = iSub.getActiveSubscriptionInfo(subId, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
        }
        return subInfo;
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccIndex(String iccId) {
        logd("[getActiveSubscriptionInfoForIccIndex]+ iccId=" + SubscriptionInfo.givePrintableIccid(iccId));
        if (iccId == null) {
            logd("[getActiveSubscriptionInfoForIccIndex]- null iccid");
            return null;
        }
        SubscriptionInfo result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoForIccId(iccId, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIndex) {
        logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIndex=" + slotIndex);
        if (isValidSlotIndex(slotIndex)) {
            SubscriptionInfo result = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.getActiveSubscriptionInfoForSimSlotIndex(slotIndex, this.mContext.getOpPackageName());
                }
            } catch (RemoteException e) {
            }
            return result;
        }
        logd("[getActiveSubscriptionInfoForSimSlotIndex]- invalid slotIndex");
        return null;
    }

    public List<SubscriptionInfo> getAllSubscriptionInfoList() {
        logd("[getAllSubscriptionInfoList]+");
        List<SubscriptionInfo> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoList(this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public int getAllSubscriptionInfoCount() {
        logd("[getAllSubscriptionInfoCount]+");
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getAllSubInfoCount(this.mContext.getOpPackageName());
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getActiveSubscriptionInfoCount() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getActiveSubInfoCount(this.mContext.getOpPackageName());
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getActiveSubscriptionInfoCountMax() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getActiveSubInfoCountMax();
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public Uri addSubscriptionInfoRecord(String iccId, int slotIndex) {
        logd("[addSubscriptionInfoRecord]+ iccId:" + SubscriptionInfo.givePrintableIccid(iccId) + " slotIndex:" + slotIndex);
        if (iccId == null) {
            logd("[addSubscriptionInfoRecord]- null iccId");
        }
        if (!isValidSlotIndex(slotIndex)) {
            logd("[addSubscriptionInfoRecord]- invalid slotIndex");
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.addSubInfoRecord(iccId, slotIndex);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public int setIconTint(int tint, int subId) {
        logd("[setIconTint]+ tint:" + tint + " subId:" + subId);
        if (isValidSubscriptionId(subId)) {
            int result = 0;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.setIconTint(tint, subId);
                }
            } catch (RemoteException e) {
            }
            return result;
        }
        logd("[setIconTint]- fail");
        return -1;
    }

    public int setDisplayName(String displayName, int subId) {
        return setDisplayName(displayName, subId, -1);
    }

    public int setDisplayName(String displayName, int subId, long nameSource) {
        logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId + " nameSource:" + nameSource);
        if (isValidSubscriptionId(subId)) {
            int result = 0;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.setDisplayNameUsingSrc(displayName, subId, nameSource);
                }
            } catch (RemoteException e) {
            }
            return result;
        }
        logd("[setDisplayName]- fail");
        return -1;
    }

    public int setDisplayNumber(String number, int subId) {
        if (number == null || (isValidSubscriptionId(subId) ^ 1) != 0) {
            logd("[setDisplayNumber]- fail");
            return -1;
        }
        int result = 0;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDisplayNumber(number, subId);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public int setDataRoaming(int roaming, int subId) {
        logd("[setDataRoaming]+ roaming:" + roaming + " subId:" + subId);
        if (roaming < 0 || (isValidSubscriptionId(subId) ^ 1) != 0) {
            logd("[setDataRoaming]- fail");
            return -1;
        }
        int result = 0;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDataRoaming(roaming, subId);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public static int getSlotIndex(int subId) {
        boolean isValidSubscriptionId = isValidSubscriptionId(subId);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSlotIndex(subId);
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int[] getSubId(int slotIndex) {
        if (isValidSlotIndex(slotIndex)) {
            int[] subId = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    subId = iSub.getSubId(slotIndex);
                }
            } catch (RemoteException e) {
            }
            return subId;
        }
        logd("[getSubId]- fail");
        return null;
    }

    public static int getPhoneId(int subId) {
        if (!isValidSubscriptionId(subId)) {
            return -1;
        }
        int result = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getPhoneId(subId);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public static int getDefaultSubId() {
        return getDefaultSubscriptionId();
    }

    public static int getDefaultSubscriptionId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultSubId();
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int getDefaultVoiceSubscriptionId() {
        int subId = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultVoiceSubId();
            }
        } catch (RemoteException e) {
        }
        logd("getDefaultVoiceSubscriptionId, sub id = " + subId);
        return subId;
    }

    public void setDefaultVoiceSubId(int subId) {
        logd("setDefaultVoiceSubId sub id = " + subId);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultVoiceSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getDefaultVoiceSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultVoiceSubscriptionId());
    }

    public static int getDefaultVoicePhoneId() {
        return getPhoneId(getDefaultVoiceSubscriptionId());
    }

    public static int getDefaultSmsSubscriptionId() {
        int subId = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultSmsSubId();
            }
        } catch (RemoteException e) {
        }
        logd("getDefaultSmsSubscriptionId, sub id = " + subId);
        return subId;
    }

    public void setDefaultSmsSubId(int subId) {
        logd("setDefaultSmsSubId sub id = " + subId);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultSmsSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getDefaultSmsSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultSmsSubscriptionId());
    }

    public int getDefaultSmsPhoneId() {
        return getPhoneId(getDefaultSmsSubscriptionId());
    }

    public static int getDefaultDataSubscriptionId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultDataSubId();
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void setDefaultDataSubId(int subId) {
        logd("setDataSubscription sub id = " + subId);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultDataSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getDefaultDataSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultDataSubscriptionId());
    }

    public int getDefaultDataPhoneId() {
        return getPhoneId(getDefaultDataSubscriptionId());
    }

    public void clearSubscriptionInfo() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearSubInfo();
            }
        } catch (RemoteException e) {
        }
    }

    public boolean allDefaultsSelected() {
        if (isValidSubscriptionId(getDefaultDataSubscriptionId()) && isValidSubscriptionId(getDefaultSmsSubscriptionId()) && isValidSubscriptionId(getDefaultVoiceSubscriptionId())) {
            return true;
        }
        return false;
    }

    public void clearDefaultsForInactiveSubIds() {
        logd("clearDefaultsForInactiveSubIds");
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearDefaultsForInactiveSubIds();
            }
        } catch (RemoteException e) {
        }
    }

    public static boolean isValidSubscriptionId(int subId) {
        return subId > -1;
    }

    public static boolean isUsableSubIdValue(int subId) {
        return subId >= 0 && subId <= 2147483646;
    }

    public static boolean isValidSlotIndex(int slotIndex) {
        return slotIndex >= 0 && slotIndex < TelephonyManager.getDefault().getSimCount();
    }

    public static boolean isValidPhoneId(int phoneId) {
        return phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount();
    }

    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length <= 0) {
            logd("putPhoneIdAndSubIdExtra: no valid subs");
        } else {
            putPhoneIdAndSubIdExtra(intent, phoneId, subIds[0]);
        }
    }

    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId, int subId) {
        logd("putPhoneIdAndSubIdExtra: phoneId=" + phoneId + " subId=" + subId);
        intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, subId);
        intent.putExtra(EXTRA_SUBSCRIPTION_INDEX, subId);
        intent.putExtra("phone", phoneId);
        intent.putExtra(PhoneConstants.SLOT_KEY, phoneId);
    }

    public int[] getActiveSubscriptionIdList() {
        int[] subId = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getActiveSubIdList();
            }
        } catch (RemoteException e) {
        }
        if (subId == null) {
            return new int[0];
        }
        return subId;
    }

    public boolean isNetworkRoaming(int subId) {
        if (getPhoneId(subId) < 0) {
            return false;
        }
        return TelephonyManager.getDefault().isNetworkRoaming(subId);
    }

    public static int getSimStateForSlotIndex(int slotIndex) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSimStateForSlotIndex(slotIndex);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static void setSubscriptionProperty(int subId, String propKey, String propValue) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setSubscriptionProperty(subId, propKey, propValue);
            }
        } catch (RemoteException e) {
        }
    }

    private static String getSubscriptionProperty(int subId, String propKey, Context context) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSubscriptionProperty(subId, propKey, context.getOpPackageName());
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean getBooleanSubscriptionProperty(int subId, String propKey, boolean defValue, Context context) {
        boolean z = true;
        String result = getSubscriptionProperty(subId, propKey, context);
        if (result != null) {
            try {
                if (Integer.parseInt(result) != 1) {
                    z = false;
                }
                return z;
            } catch (NumberFormatException e) {
                logd("getBooleanSubscriptionProperty NumberFormat exception");
            }
        }
        return defValue;
    }

    public static int getIntegerSubscriptionProperty(int subId, String propKey, int defValue, Context context) {
        String result = getSubscriptionProperty(subId, propKey, context);
        if (result != null) {
            try {
                return Integer.parseInt(result);
            } catch (NumberFormatException e) {
                logd("getBooleanSubscriptionProperty NumberFormat exception");
            }
        }
        return defValue;
    }

    public static Resources getResourcesForSubId(Context context, int subId) {
        SubscriptionInfo subInfo = from(context).getActiveSubscriptionInfo(subId);
        Configuration config = context.getResources().getConfiguration();
        Configuration newConfig = new Configuration();
        newConfig.setTo(config);
        if (subInfo != null) {
            newConfig.mcc = subInfo.getMcc();
            newConfig.mnc = subInfo.getMnc();
            if (newConfig.mnc == 0) {
                newConfig.mnc = 65535;
            }
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        DisplayMetrics newMetrics = new DisplayMetrics();
        newMetrics.setTo(metrics);
        return new Resources(context.getResources().getAssets(), newMetrics, newConfig);
    }

    public boolean isActiveSubId(int subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.isActiveSubId(subId);
            }
        } catch (RemoteException e) {
        }
        return false;
    }
}
