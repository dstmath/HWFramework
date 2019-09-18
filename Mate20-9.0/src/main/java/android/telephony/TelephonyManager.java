package android.telephony;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.ActivityThread;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.service.carrier.CarrierIdentifier;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.TelephonyScanManager;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.aidl.IImsRegistration;
import android.util.Log;
import com.android.ims.internal.IImsServiceFeatureCallback;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.CellNetworkScanResult;
import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.Preconditions;
import huawei.cust.HwCustUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelephonyManager {
    public static final String ACTION_CONFIGURE_VOICEMAIL = "android.telephony.action.CONFIGURE_VOICEMAIL";
    public static final String ACTION_DATA_STALL_DETECTED = "android.intent.action.DATA_STALL_DETECTED";
    public static final String ACTION_EMERGENCY_ASSISTANCE = "android.telephony.action.EMERGENCY_ASSISTANCE";
    public static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
    public static final String ACTION_PRECISE_CALL_STATE_CHANGED = "android.intent.action.PRECISE_CALL_STATE";
    @Deprecated
    public static final String ACTION_PRECISE_DATA_CONNECTION_STATE_CHANGED = "android.intent.action.PRECISE_DATA_CONNECTION_STATE_CHANGED";
    public static final String ACTION_RESPOND_VIA_MESSAGE = "android.intent.action.RESPOND_VIA_MESSAGE";
    public static final String ACTION_SHOW_VOICEMAIL_NOTIFICATION = "android.telephony.action.SHOW_VOICEMAIL_NOTIFICATION";
    @SystemApi
    public static final String ACTION_SIM_APPLICATION_STATE_CHANGED = "android.telephony.action.SIM_APPLICATION_STATE_CHANGED";
    @SystemApi
    public static final String ACTION_SIM_CARD_STATE_CHANGED = "android.telephony.action.SIM_CARD_STATE_CHANGED";
    @SystemApi
    public static final String ACTION_SIM_SLOT_STATUS_CHANGED = "android.telephony.action.SIM_SLOT_STATUS_CHANGED";
    public static final String ACTION_SUBSCRIPTION_CARRIER_IDENTITY_CHANGED = "android.telephony.action.SUBSCRIPTION_CARRIER_IDENTITY_CHANGED";
    public static final int APPTYPE_CSIM = 4;
    public static final int APPTYPE_ISIM = 5;
    public static final int APPTYPE_RUIM = 3;
    public static final int APPTYPE_SIM = 1;
    public static final int APPTYPE_USIM = 2;
    public static final int AUTHTYPE_EAP_AKA = 129;
    public static final int AUTHTYPE_EAP_SIM = 128;
    public static final int CALL_STATE_IDLE = 0;
    public static final int CALL_STATE_OFFHOOK = 2;
    public static final int CALL_STATE_RINGING = 1;
    public static final int CARD_POWER_DOWN = 0;
    public static final int CARD_POWER_UP = 1;
    public static final int CARD_POWER_UP_PASS_THROUGH = 2;
    @SystemApi
    public static final int CARRIER_PRIVILEGE_STATUS_ERROR_LOADING_RULES = -2;
    @SystemApi
    public static final int CARRIER_PRIVILEGE_STATUS_HAS_ACCESS = 1;
    @SystemApi
    public static final int CARRIER_PRIVILEGE_STATUS_NO_ACCESS = 0;
    @SystemApi
    public static final int CARRIER_PRIVILEGE_STATUS_RULES_NOT_LOADED = -1;
    public static final int CDMA_ROAMING_MODE_AFFILIATED = 1;
    public static final int CDMA_ROAMING_MODE_ANY = 2;
    public static final int CDMA_ROAMING_MODE_HOME = 0;
    public static final int CDMA_ROAMING_MODE_RADIO_DEFAULT = -1;
    public static final int DATA_ACTIVITY_DORMANT = 4;
    public static final int DATA_ACTIVITY_IN = 1;
    public static final int DATA_ACTIVITY_INOUT = 3;
    public static final int DATA_ACTIVITY_NONE = 0;
    public static final int DATA_ACTIVITY_OUT = 2;
    public static final int DATA_CONNECTED = 2;
    public static final int DATA_CONNECTING = 1;
    public static final int DATA_DISCONNECTED = 0;
    private static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    public static final int DATA_SUSPENDED = 3;
    public static final int DATA_UNKNOWN = -1;
    public static final boolean EMERGENCY_ASSISTANCE_ENABLED = true;
    public static final String EVENT_CALL_FORWARDED = "android.telephony.event.EVENT_CALL_FORWARDED";
    public static final String EVENT_DOWNGRADE_DATA_DISABLED = "android.telephony.event.EVENT_DOWNGRADE_DATA_DISABLED";
    public static final String EVENT_DOWNGRADE_DATA_LIMIT_REACHED = "android.telephony.event.EVENT_DOWNGRADE_DATA_LIMIT_REACHED";
    public static final String EVENT_HANDOVER_TO_WIFI_FAILED = "android.telephony.event.EVENT_HANDOVER_TO_WIFI_FAILED";
    public static final String EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI = "android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI";
    public static final String EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE = "android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE";
    public static final String EVENT_NOTIFY_INTERNATIONAL_CALL_ON_WFC = "android.telephony.event.EVENT_NOTIFY_INTERNATIONAL_CALL_ON_WFC";
    public static final String EVENT_SUPPLEMENTARY_SERVICE_NOTIFICATION = "android.telephony.event.EVENT_SUPPLEMENTARY_SERVICE_NOTIFICATION";
    public static final String EXTRA_BACKGROUND_CALL_STATE = "background_state";
    public static final String EXTRA_CALL_VOICEMAIL_INTENT = "android.telephony.extra.CALL_VOICEMAIL_INTENT";
    public static final String EXTRA_CARRIER_ID = "android.telephony.extra.CARRIER_ID";
    public static final String EXTRA_CARRIER_NAME = "android.telephony.extra.CARRIER_NAME";
    public static final String EXTRA_DATA_APN = "apn";
    public static final String EXTRA_DATA_APN_TYPE = "apnType";
    public static final String EXTRA_DATA_CHANGE_REASON = "reason";
    public static final String EXTRA_DATA_FAILURE_CAUSE = "failCause";
    public static final String EXTRA_DATA_LINK_PROPERTIES_KEY = "linkProperties";
    public static final String EXTRA_DATA_NETWORK_TYPE = "networkType";
    public static final String EXTRA_DATA_STATE = "state";
    public static final String EXTRA_DISCONNECT_CAUSE = "disconnect_cause";
    public static final String EXTRA_FOREGROUND_CALL_STATE = "foreground_state";
    public static final String EXTRA_HIDE_PUBLIC_SETTINGS = "android.telephony.extra.HIDE_PUBLIC_SETTINGS";
    public static final String EXTRA_INCOMING_NUMBER = "incoming_number";
    public static final String EXTRA_IS_REFRESH = "android.telephony.extra.IS_REFRESH";
    public static final String EXTRA_LAUNCH_VOICEMAIL_SETTINGS_INTENT = "android.telephony.extra.LAUNCH_VOICEMAIL_SETTINGS_INTENT";
    public static final String EXTRA_NOTIFICATION_CODE = "android.telephony.extra.NOTIFICATION_CODE";
    public static final String EXTRA_NOTIFICATION_COUNT = "android.telephony.extra.NOTIFICATION_COUNT";
    public static final String EXTRA_NOTIFICATION_MESSAGE = "android.telephony.extra.NOTIFICATION_MESSAGE";
    public static final String EXTRA_NOTIFICATION_TYPE = "android.telephony.extra.NOTIFICATION_TYPE";
    public static final String EXTRA_PHONE_ACCOUNT_HANDLE = "android.telephony.extra.PHONE_ACCOUNT_HANDLE";
    public static final String EXTRA_PRECISE_DISCONNECT_CAUSE = "precise_disconnect_cause";
    public static final String EXTRA_RECOVERY_ACTION = "recoveryAction";
    public static final String EXTRA_RINGING_CALL_STATE = "ringing_state";
    @SystemApi
    public static final String EXTRA_SIM_STATE = "android.telephony.extra.SIM_STATE";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_STATE_IDLE = PhoneConstants.State.IDLE.toString();
    public static final String EXTRA_STATE_OFFHOOK = PhoneConstants.State.OFFHOOK.toString();
    public static final String EXTRA_STATE_RINGING = PhoneConstants.State.RINGING.toString();
    public static final String EXTRA_SUBSCRIPTION_ID = "android.telephony.extra.SUBSCRIPTION_ID";
    @SystemApi
    public static final String EXTRA_VISUAL_VOICEMAIL_ENABLED_BY_USER_BOOL = "android.telephony.extra.VISUAL_VOICEMAIL_ENABLED_BY_USER_BOOL";
    public static final String EXTRA_VOICEMAIL_NUMBER = "android.telephony.extra.VOICEMAIL_NUMBER";
    @SystemApi
    public static final String EXTRA_VOICEMAIL_SCRAMBLED_PIN_STRING = "android.telephony.extra.VOICEMAIL_SCRAMBLED_PIN_STRING";
    private static final boolean HW_DEBUGGABLE = Build.IS_DEBUGGABLE;
    public static final int ICCID_BEGIN = 0;
    public static final int ICCID_END = 19;
    public static final int INDICATION_FILTER_DATA_CALL_DORMANCY_CHANGED = 4;
    public static final int INDICATION_FILTER_FULL_NETWORK_STATE = 2;
    public static final int INDICATION_FILTER_LINK_CAPACITY_ESTIMATE = 8;
    public static final int INDICATION_FILTER_PHYSICAL_CHANNEL_CONFIG = 16;
    public static final int INDICATION_FILTER_SIGNAL_STRENGTH = 1;
    public static final int INDICATION_UPDATE_MODE_IGNORE_SCREEN_OFF = 2;
    public static final int INDICATION_UPDATE_MODE_NORMAL = 1;
    public static final int KEY_TYPE_EPDG = 1;
    public static final int KEY_TYPE_WLAN = 2;
    public static final int MAX_NETWORK_TYPE = 19;
    public static final String METADATA_HIDE_VOICEMAIL_SETTINGS_MENU = "android.telephony.HIDE_VOICEMAIL_SETTINGS_MENU";
    public static final String MODEM_ACTIVITY_RESULT_KEY = "controller_activity";
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;
    public static final int NETWORK_CLASS_5_G = 4;
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_TYPE_1xRTT = 7;
    public static final int NETWORK_TYPE_CDMA = 4;
    public static final int NETWORK_TYPE_DCHSPAP = 30;
    public static final int NETWORK_TYPE_EDGE = 2;
    public static final int NETWORK_TYPE_EHRPD = 14;
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    public static final int NETWORK_TYPE_EVDO_A = 6;
    public static final int NETWORK_TYPE_EVDO_B = 12;
    public static final int NETWORK_TYPE_GPRS = 1;
    public static final int NETWORK_TYPE_GSM = 16;
    public static final int NETWORK_TYPE_HSDPA = 8;
    public static final int NETWORK_TYPE_HSPA = 10;
    public static final int NETWORK_TYPE_HSPAP = 15;
    public static final int NETWORK_TYPE_HSUPA = 9;
    public static final int NETWORK_TYPE_IDEN = 11;
    public static final int NETWORK_TYPE_IWLAN = 18;
    public static final int NETWORK_TYPE_LTE = 13;
    public static final int NETWORK_TYPE_LTE_CA = 19;
    public static final int NETWORK_TYPE_NR = 20;
    public static final int NETWORK_TYPE_TD_SCDMA = 17;
    public static final int NETWORK_TYPE_UMTS = 3;
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    public static final boolean NETWROK_TYPE_COMMA_SWITCH = SystemProperties.getBoolean("ro.hwpps.network.type.comma", false);
    public static final int OTASP_NEEDED = 2;
    public static final int OTASP_NOT_NEEDED = 3;
    public static final int OTASP_SIM_UNPROVISIONED = 5;
    public static final int OTASP_UNINITIALIZED = 0;
    public static final int OTASP_UNKNOWN = 1;
    public static final String PHONE_PROCESS_NAME = "com.android.phone";
    public static final int PHONE_TYPE_CDMA = 2;
    public static final int PHONE_TYPE_GSM = 1;
    public static final int PHONE_TYPE_NONE = 0;
    public static final int PHONE_TYPE_SIP = 3;
    public static final int SCOPE_ALL = 0;
    public static final int SCOPE_IMEI = 1;
    @SystemApi
    public static final int SIM_ACTIVATION_STATE_ACTIVATED = 2;
    @SystemApi
    public static final int SIM_ACTIVATION_STATE_ACTIVATING = 1;
    @SystemApi
    public static final int SIM_ACTIVATION_STATE_DEACTIVATED = 3;
    @SystemApi
    public static final int SIM_ACTIVATION_STATE_RESTRICTED = 4;
    @SystemApi
    public static final int SIM_ACTIVATION_STATE_UNKNOWN = 0;
    public static final int SIM_STATE_ABSENT = 1;
    public static final int SIM_STATE_CARD_IO_ERROR = 8;
    public static final int SIM_STATE_CARD_RESTRICTED = 9;
    @SystemApi
    public static final int SIM_STATE_LOADED = 10;
    public static final int SIM_STATE_NETWORK_LOCKED = 4;
    public static final int SIM_STATE_NOT_READY = 6;
    public static final int SIM_STATE_PERM_DISABLED = 7;
    public static final int SIM_STATE_PIN_REQUIRED = 2;
    @SystemApi
    public static final int SIM_STATE_PRESENT = 11;
    public static final int SIM_STATE_PUK_REQUIRED = 3;
    public static final int SIM_STATE_READY = 5;
    public static final int SIM_STATE_UNKNOWN = 0;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final boolean SUPPORT_19BIT_ICCID = SystemProperties.getBoolean("ro.config.sup_19bit_iccid", false);
    private static final String TAG = "TelephonyManager";
    public static final int UNKNOWN_CARRIER_ID = -1;
    public static final int UNKNOWN_CARRIER_ID_LIST_VERSION = -1;
    public static final int USSD_ERROR_SERVICE_UNAVAIL = -2;
    public static final String USSD_RESPONSE = "USSD_RESPONSE";
    public static final int USSD_RETURN_FAILURE = -1;
    public static final int USSD_RETURN_SUCCESS = 100;
    public static final String VVM_TYPE_CVVM = "vvm_type_cvvm";
    public static final String VVM_TYPE_OMTP = "vvm_type_omtp";
    private static String multiSimConfig = SystemProperties.get("persist.radio.multisim.config");
    private static TelephonyManager sInstance = new TelephonyManager();
    private static final String sKernelCmdLine = getProcCmdLine();
    private static final String sLteOnCdmaProductType = SystemProperties.get("telephony.lteOnCdmaProductType", "");
    private static final Pattern sProductTypePattern = Pattern.compile("\\sproduct_type\\s*=\\s*(\\w+)");
    private final Context mContext;
    private HwCustTelephonyManager mCust;
    private final int mSubId;
    private SubscriptionManager mSubscriptionManager;
    private TelephonyScanManager mTelephonyScanManager;

    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicationFilters {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicationUpdateMode {
    }

    public enum MultiSimVariants {
        DSDS,
        DSDA,
        TSTS,
        UNKNOWN
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SimActivationState {
    }

    public static abstract class UssdResponseCallback {
        public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
        }

        public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
        }
    }

    public interface WifiCallingChoices {
        public static final int ALWAYS_USE = 0;
        public static final int ASK_EVERY_TIME = 1;
        public static final int NEVER_USE = 2;
    }

    public TelephonyManager(Context context) {
        this(context, Integer.MAX_VALUE);
    }

    public TelephonyManager(Context context, int subId) {
        this.mCust = null;
        this.mSubId = subId;
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            this.mContext = appContext;
        } else {
            this.mContext = context;
        }
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        this.mCust = (HwCustTelephonyManager) HwCustUtils.createObj(HwCustTelephonyManager.class, new Object[]{this.mContext});
    }

    private TelephonyManager() {
        this.mCust = null;
        this.mContext = null;
        this.mSubId = Integer.MAX_VALUE;
    }

    public static TelephonyManager getDefault() {
        return sInstance;
    }

    private String getOpPackageName() {
        if (this.mContext != null) {
            return this.mContext.getOpPackageName();
        }
        return ActivityThread.currentOpPackageName();
    }

    private boolean isSystemProcess() {
        return Process.myUid() == 1000;
    }

    public MultiSimVariants getMultiSimConfiguration() {
        String mSimConfig = SystemProperties.get("persist.radio.multisim.config");
        if (mSimConfig.equals("dsds")) {
            return MultiSimVariants.DSDS;
        }
        if (mSimConfig.equals("dsda")) {
            return MultiSimVariants.DSDA;
        }
        if (mSimConfig.equals("tsts")) {
            return MultiSimVariants.TSTS;
        }
        return MultiSimVariants.UNKNOWN;
    }

    public int getPhoneCount() {
        switch (getMultiSimConfiguration()) {
            case UNKNOWN:
                if (isVoiceCapable() || isSmsCapable()) {
                    return 1;
                }
                if (this.mContext == null) {
                    return 1;
                }
                ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                if (cm == null) {
                    return 1;
                }
                if (cm.isNetworkSupported(0)) {
                    return 1;
                }
                return 0;
            case DSDS:
            case DSDA:
                return 2;
            case TSTS:
                return 3;
            default:
                return 1;
        }
    }

    public static TelephonyManager from(Context context) {
        return (TelephonyManager) context.getSystemService("phone");
    }

    public TelephonyManager createForSubscriptionId(int subId) {
        return new TelephonyManager(this.mContext, subId);
    }

    public TelephonyManager createForPhoneAccountHandle(PhoneAccountHandle phoneAccountHandle) {
        int subId = getSubIdForPhoneAccountHandle(phoneAccountHandle);
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return null;
        }
        return new TelephonyManager(this.mContext, subId);
    }

    public boolean isMultiSimEnabled() {
        return multiSimConfig.equals("dsds") || multiSimConfig.equals("dsda") || multiSimConfig.equals("tsts");
    }

    public String getDeviceSoftwareVersion() {
        return getDeviceSoftwareVersion(getSlotIndex());
    }

    public String getDeviceSoftwareVersion(int slotIndex) {
        ITelephony telephony = getITelephony();
        if (telephony == null) {
            return null;
        }
        try {
            return telephony.getDeviceSoftwareVersionForSlot(slotIndex, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    @Deprecated
    public String getDeviceId() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().getUniqueDeviceId(0);
    }

    @Deprecated
    public String getDeviceId(int slotIndex) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getDeviceIdForPhone(slotIndex, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getImei() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().getUniqueDeviceId(1);
    }

    public String getImei(int slotIndex) {
        ITelephony telephony = getITelephony();
        if (telephony == null) {
            return null;
        }
        try {
            return telephony.getImeiForSlot(slotIndex, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getMeid() {
        return getMeid(getSlotIndex());
    }

    public String getMeid(int slotIndex) {
        ITelephony telephony = getITelephony();
        if (telephony == null) {
            return null;
        }
        try {
            return telephony.getMeidForSlot(slotIndex, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getNai() {
        return getNaiBySubscriberId(getSubId());
    }

    public String getNai(int slotIndex) {
        int[] subId = SubscriptionManager.getSubId(slotIndex);
        if (subId == null) {
            return null;
        }
        return getNaiBySubscriberId(subId[0]);
    }

    private String getNaiBySubscriberId(int subId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            String nai = info.getNaiForSubscriber(subId, this.mContext.getOpPackageName());
            if (Log.isLoggable(TAG, 2)) {
                Rlog.v(TAG, "Nai = " + nai);
            }
            return nai;
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    @Deprecated
    public CellLocation getCellLocation() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                Rlog.d(TAG, "getCellLocation returning null because telephony is null");
                return null;
            }
            Log.d(TAG, "getCellLocation calling app is " + getOpPackageName());
            Bundle bundle = telephony.getCellLocation(this.mContext.getOpPackageName());
            if (bundle != null) {
                if (!bundle.isEmpty()) {
                    CellLocation cl = CellLocation.newFromBundle(bundle);
                    if (cl != null) {
                        if (!cl.isEmpty()) {
                            return cl;
                        }
                    }
                    Rlog.d(TAG, "getCellLocation returning null because CellLocation is empty");
                    return null;
                }
            }
            Rlog.d(TAG, "getCellLocation returning null because bundle is empty");
            return null;
        } catch (RemoteException ex) {
            Rlog.d(TAG, "getCellLocation returning null due to RemoteException " + ex);
            return null;
        } catch (NullPointerException ex2) {
            Rlog.d(TAG, "getCellLocation returning null due to NullPointerException " + ex2);
            return null;
        }
    }

    public void enableLocationUpdates() {
        enableLocationUpdates(getSubId());
    }

    public void enableLocationUpdates(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.enableLocationUpdatesForSubscriber(subId);
            }
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public void disableLocationUpdates() {
        disableLocationUpdates(getSubId());
    }

    public void disableLocationUpdates(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.disableLocationUpdatesForSubscriber(subId);
            }
        } catch (RemoteException | NullPointerException e) {
        }
    }

    @Deprecated
    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        String callingAppName = HwFrameworkFactory.getHwInnerTelephonyManager().getCallingAppName(this.mContext);
        Log.d(TAG, "getNeighboringCellInfo calling app is " + callingAppName);
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return null;
            }
            return telephony.getNeighboringCellInfo(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    @SystemApi
    public int getCurrentPhoneType() {
        return getCurrentPhoneType(getSubId());
    }

    @SystemApi
    public int getCurrentPhoneType(int subId) {
        int phoneId;
        if (subId == -1) {
            phoneId = 0;
        } else {
            phoneId = SubscriptionManager.getPhoneId(subId);
        }
        return getCurrentPhoneTypeForSlot(phoneId);
    }

    public int getCurrentPhoneTypeForSlot(int slotIndex) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getActivePhoneTypeForSlot(slotIndex);
            }
            return getPhoneTypeFromProperty(slotIndex);
        } catch (RemoteException e) {
            return getPhoneTypeFromProperty(slotIndex);
        } catch (NullPointerException e2) {
            return getPhoneTypeFromProperty(slotIndex);
        }
    }

    public int getPhoneType() {
        if (!isVoiceCapable()) {
            return 0;
        }
        return getCurrentPhoneType();
    }

    private int getPhoneTypeFromProperty() {
        return getPhoneTypeFromProperty(getPhoneId());
    }

    private int getPhoneTypeFromProperty(int phoneId) {
        String type = getTelephonyProperty(phoneId, "gsm.current.phone-type", null);
        if (type == null || type.isEmpty()) {
            return getPhoneTypeFromNetworkType(phoneId);
        }
        return Integer.parseInt(type);
    }

    private int getPhoneTypeFromNetworkType() {
        return getPhoneTypeFromNetworkType(getPhoneId());
    }

    private int getPhoneTypeFromNetworkType(int phoneId) {
        String mode = getTelephonyProperty(phoneId, "ro.telephony.default_network", null);
        if (mode == null || mode.isEmpty()) {
            return 0;
        }
        return getPhoneType(Integer.parseInt(mode));
    }

    public static int getPhoneType(int networkMode) {
        switch (networkMode) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 9:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
                break;
            case 4:
            case 5:
            case 6:
                return 2;
            case 7:
            case 8:
            case 10:
            case 21:
            case 22:
                return 2;
            case 11:
                if (getLteOnCdmaModeStatic() == 1) {
                    return 2;
                }
                return 1;
            default:
                switch (networkMode) {
                    case 64:
                    case 65:
                    case 66:
                    case 67:
                    case 68:
                    case 69:
                        break;
                    default:
                        return 1;
                }
        }
        return 1;
    }

    private static String getProcCmdLine() {
        String cmdline = "";
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream("/proc/cmdline");
            byte[] buffer = new byte[2048];
            int count = is2.read(buffer);
            if (count > 0) {
                cmdline = new String(buffer, 0, count);
            }
            try {
                is2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            Rlog.d(TAG, "No /proc/cmdline exception=" + e2);
            if (is != null) {
                is.close();
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        if (HW_DEBUGGABLE) {
            Rlog.d(TAG, "/proc/cmdline=" + cmdline);
        }
        return cmdline;
    }

    public static int getLteOnCdmaModeStatic() {
        String productType = "";
        int curVal = SystemProperties.getInt("telephony.lteOnCdmaDevice", -1);
        int retVal = curVal;
        if (retVal == -1) {
            Matcher matcher = sProductTypePattern.matcher(sKernelCmdLine);
            if (matcher.find()) {
                productType = matcher.group(1);
                if (sLteOnCdmaProductType.equals(productType)) {
                    retVal = 1;
                } else {
                    retVal = 0;
                }
            } else {
                retVal = 0;
            }
        }
        Rlog.d(TAG, "getLteOnCdmaMode=" + retVal + " curVal=" + curVal + " product_type='" + productType + "' lteOnCdmaProductType='" + sLteOnCdmaProductType + "'");
        return retVal;
    }

    public String getNetworkOperatorName() {
        return getNetworkOperatorName(getSubId());
    }

    public String getNetworkOperatorName(int subId) {
        return getTelephonyProperty(SubscriptionManager.getPhoneId(subId), "gsm.operator.alpha", "");
    }

    public String getNetworkOperator() {
        return getNetworkOperatorForPhone(getPhoneId());
    }

    public String getNetworkOperator(int subId) {
        return getNetworkOperatorForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getNetworkOperatorForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, "gsm.operator.numeric", "");
    }

    public String getNetworkSpecifier() {
        return String.valueOf(getSubId());
    }

    public PersistableBundle getCarrierConfig() {
        return ((CarrierConfigManager) this.mContext.getSystemService(CarrierConfigManager.class)).getConfigForSubId(getSubId());
    }

    public boolean isNetworkRoaming() {
        return isNetworkRoaming(getSubId());
    }

    public boolean isNetworkRoaming(int subId) {
        return Boolean.parseBoolean(getTelephonyProperty(SubscriptionManager.getPhoneId(subId), "gsm.operator.isroaming", null));
    }

    public String getNetworkCountryIso() {
        return getNetworkCountryIsoForPhone(getPhoneId());
    }

    public String getNetworkCountryIso(int subId) {
        return getNetworkCountryIsoForPhone(getPhoneId(subId));
    }

    public String getNetworkCountryIsoForPhone(int phoneId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return "";
            }
            return telephony.getNetworkCountryIsoForPhone(phoneId);
        } catch (RemoteException e) {
            return "";
        }
    }

    public int getNetworkType() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getNetworkType();
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    public int getNetworkType(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getNetworkTypeForSubscriber(subId, getOpPackageName());
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    public int getDataNetworkType() {
        return getDataNetworkType(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    public int getDataNetworkType(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getDataNetworkTypeForSubscriber(subId, getOpPackageName());
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    public int getVoiceNetworkType() {
        return getVoiceNetworkType(getSubId());
    }

    public int getVoiceNetworkType(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVoiceNetworkTypeForSubscriber(subId, getOpPackageName());
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    public static int getNetworkClass(int networkType) {
        if (networkType != 30) {
            switch (networkType) {
                case 1:
                case 2:
                case 4:
                case 7:
                case 11:
                case 16:
                    return 1;
                case 3:
                case 5:
                case 6:
                case 8:
                case 9:
                case 10:
                case 12:
                case 14:
                case 15:
                case 17:
                    break;
                case 13:
                case 18:
                case 19:
                    return 3;
                case 20:
                    return 4;
                default:
                    return 0;
            }
        }
        return 2;
    }

    public String getNetworkTypeName() {
        return getNetworkTypeName(getNetworkType());
    }

    public static String getNetworkTypeName(int type) {
        if (type == 30) {
            return "DC-HSPA+";
        }
        switch (type) {
            case 1:
                return "GPRS";
            case 2:
                return "EDGE";
            case 3:
                return "UMTS";
            case 4:
                return "CDMA";
            case 5:
                return "CDMA - EvDo rev. 0";
            case 6:
                return "CDMA - EvDo rev. A";
            case 7:
                return "CDMA - 1xRTT";
            case 8:
                return "HSDPA";
            case 9:
                return "HSUPA";
            case 10:
                return "HSPA";
            case 11:
                return "iDEN";
            case 12:
                return "CDMA - EvDo rev. B";
            case 13:
                return "LTE";
            case 14:
                return "CDMA - eHRPD";
            case 15:
                return "HSPA+";
            case 16:
                return "GSM";
            case 17:
                return "TD-SCDMA";
            case 18:
                return "IWLAN";
            case 19:
                return "LTE-CA";
            case 20:
                return "NR";
            default:
                return "UNKNOWN";
        }
    }

    public boolean hasIccCard() {
        return hasIccCard(getSlotIndex());
    }

    public boolean hasIccCard(int slotIndex) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return false;
            }
            return telephony.hasIccCardUsingSlotIndex(slotIndex);
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public int getSimState() {
        int simState = getSimStateIncludingLoaded();
        if (simState == 10) {
            return 5;
        }
        return simState;
    }

    private int getSimStateIncludingLoaded() {
        int slotIndex = getSlotIndex();
        if (slotIndex >= 0) {
            return SubscriptionManager.getSimStateForSlotIndex(slotIndex);
        }
        for (int i = 0; i < getPhoneCount(); i++) {
            if (getSimState(i) != 1) {
                Rlog.d(TAG, "getSimState: default sim:" + slotIndex + ", sim state for slotIndex=" + i + " is " + simState + ", return state as unknown");
                return 0;
            }
        }
        Rlog.d(TAG, "getSimState: default sim:" + slotIndex + ", all SIMs absent, return state as absent");
        return 1;
    }

    @SystemApi
    public int getSimCardState() {
        int simCardState = getSimState();
        switch (simCardState) {
            case 0:
            case 1:
            case 8:
            case 9:
                return simCardState;
            default:
                return 11;
        }
    }

    @SystemApi
    public int getSimApplicationState() {
        int simApplicationState = getSimStateIncludingLoaded();
        switch (simApplicationState) {
            case 0:
            case 1:
            case 8:
            case 9:
                return 0;
            case 5:
                return 6;
            default:
                return simApplicationState;
        }
    }

    public int getSimState(int slotIndex) {
        int simState = SubscriptionManager.getSimStateForSlotIndex(slotIndex);
        if (simState == 10) {
            return 5;
        }
        return simState;
    }

    public String getSimOperator() {
        return getSimOperatorNumeric();
    }

    public String getSimOperator(int subId) {
        return getSimOperatorNumeric(subId);
    }

    public String getSimOperatorNumeric() {
        int subId = this.mSubId;
        if (!SubscriptionManager.isUsableSubIdValue(subId)) {
            subId = SubscriptionManager.getDefaultDataSubscriptionId();
            if (!SubscriptionManager.isUsableSubIdValue(subId)) {
                subId = SubscriptionManager.getDefaultSmsSubscriptionId();
                if (!SubscriptionManager.isUsableSubIdValue(subId)) {
                    subId = SubscriptionManager.getDefaultVoiceSubscriptionId();
                    if (!SubscriptionManager.isUsableSubIdValue(subId)) {
                        subId = SubscriptionManager.getDefaultSubscriptionId();
                    }
                }
            }
        }
        return getSimOperatorNumeric(subId);
    }

    public String getSimOperatorNumeric(int subId) {
        return getSimOperatorNumericForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getSimOperatorNumericForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, "gsm.sim.operator.numeric", "");
    }

    public String getSimOperatorName() {
        return getSimOperatorNameForPhone(getPhoneId());
    }

    public String getSimOperatorName(int subId) {
        return getSimOperatorNameForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getSimOperatorNameForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, "gsm.sim.operator.alpha", "");
    }

    public String getSimCountryIso() {
        return getSimCountryIsoForPhone(getPhoneId());
    }

    public String getSimCountryIso(int subId) {
        return getSimCountryIsoForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getSimCountryIsoForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, "gsm.sim.operator.iso-country", "");
    }

    public String getSimSerialNumber() {
        return getSimSerialNumber(getSubId());
    }

    public String getSimSerialNumber(int subId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            String iccid = info.getIccSerialNumberForSubscriber(subId, this.mContext.getOpPackageName());
            return (!SUPPORT_19BIT_ICCID || iccid == null || iccid.length() < 19) ? iccid : iccid.substring(0, 19);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public int getLteOnCdmaMode() {
        return getLteOnCdmaMode(getSubId());
    }

    public int getLteOnCdmaMode(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return -1;
            }
            return telephony.getLteOnCdmaModeForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e2) {
            return -1;
        }
    }

    @SystemApi
    public UiccSlotInfo[] getUiccSlotsInfo() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return null;
            }
            return telephony.getUiccSlotsInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void refreshUiccProfile() {
        try {
            getITelephony().refreshUiccProfile(this.mSubId);
        } catch (RemoteException ex) {
            Rlog.w(TAG, "RemoteException", ex);
        }
    }

    @SystemApi
    public boolean switchSlots(int[] physicalSlots) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return false;
            }
            return telephony.switchSlots(physicalSlots);
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getSubscriberId() {
        return getSubscriberId(getSubId());
    }

    public String getSubscriberId(int subId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getSubscriberIdForSubscriber(subId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int keyType) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                Rlog.e(TAG, "IMSI error: Subscriber Info is null");
                return null;
            }
            int subId = getSubId(SubscriptionManager.getDefaultDataSubscriptionId());
            if (keyType != 1) {
                if (keyType != 2) {
                    throw new IllegalArgumentException("IMSI error: Invalid key type");
                }
            }
            ImsiEncryptionInfo imsiEncryptionInfo = info.getCarrierInfoForImsiEncryption(subId, keyType, this.mContext.getOpPackageName());
            if (imsiEncryptionInfo == null) {
                if (isImsiEncryptionRequired(subId, keyType)) {
                    Rlog.e(TAG, "IMSI error: key is required but not found");
                    throw new IllegalArgumentException("IMSI error: key is required but not found");
                }
            }
            return imsiEncryptionInfo;
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getCarrierInfoForImsiEncryption RemoteException" + ex);
            return null;
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getCarrierInfoForImsiEncryption NullPointerException" + ex2);
            return null;
        }
    }

    public void resetCarrierKeysForImsiEncryption() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                Rlog.e(TAG, "IMSI error: Subscriber Info is null");
                if (!isSystemProcess()) {
                    throw new RuntimeException("IMSI error: Subscriber Info is null");
                }
                return;
            }
            info.resetCarrierKeysForImsiEncryption(getSubId(SubscriptionManager.getDefaultDataSubscriptionId()), this.mContext.getOpPackageName());
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getCarrierInfoForImsiEncryption RemoteException" + ex);
            if (!isSystemProcess()) {
                ex.rethrowAsRuntimeException();
            }
        }
    }

    private static boolean isKeyEnabled(int keyAvailability, int keyType) {
        if (((keyAvailability >> (keyType - 1)) & 1) == 1) {
            return true;
        }
        return false;
    }

    private boolean isImsiEncryptionRequired(int subId, int keyType) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager == null) {
            return false;
        }
        PersistableBundle pb = configManager.getConfigForSubId(subId);
        if (pb == null) {
            return false;
        }
        return isKeyEnabled(pb.getInt(CarrierConfigManager.IMSI_KEY_AVAILABILITY_INT), keyType);
    }

    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info != null) {
                info.setCarrierInfoForImsiEncryption(this.mSubId, this.mContext.getOpPackageName(), imsiEncryptionInfo);
            }
        } catch (NullPointerException e) {
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setCarrierInfoForImsiEncryption RemoteException", ex);
        }
    }

    public String getGroupIdLevel1() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getGroupIdLevel1ForSubscriber(getSubId(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getGroupIdLevel1(int subId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getGroupIdLevel1ForSubscriber(subId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getLine1Number() {
        return getLine1Number(getSubId());
    }

    public String getLine1Number(int subId) {
        String number = null;
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                number = telephony.getLine1NumberForDisplay(subId, this.mContext.getOpPackageName());
            }
        } catch (RemoteException | NullPointerException e) {
        }
        if (this.mCust != null && this.mCust.isVZW() && getPhoneType() == 1) {
            number = null;
        }
        if (number != null) {
            return number;
        }
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            String number2 = info.getLine1NumberForSubscriber(subId, this.mContext.getOpPackageName());
            if (this.mCust == null || !this.mCust.isVZW() || getPhoneType() != 1) {
                return number2;
            }
            return this.mCust.getVZWLine1Number(subId, number2, getSimOperator());
        } catch (RemoteException e2) {
            return null;
        } catch (NullPointerException e3) {
            return null;
        }
    }

    public boolean setLine1NumberForDisplay(String alphaTag, String number) {
        return setLine1NumberForDisplay(getSubId(), alphaTag, number);
    }

    public boolean setLine1NumberForDisplay(int subId, String alphaTag, String number) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setLine1NumberForDisplayForSubscriber(subId, alphaTag, number);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return false;
    }

    public String getLine1AlphaTag() {
        return getLine1AlphaTag(getSubId());
    }

    public String getLine1AlphaTag(int subId) {
        String alphaTag = null;
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                alphaTag = telephony.getLine1AlphaTagForDisplay(subId, getOpPackageName());
            }
        } catch (RemoteException | NullPointerException e) {
        }
        if (alphaTag != null) {
            return alphaTag;
        }
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getLine1AlphaTagForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e2) {
            return null;
        } catch (NullPointerException e3) {
            return null;
        }
    }

    public String[] getMergedSubscriberIds() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getMergedSubscriberIds(getOpPackageName());
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return null;
    }

    public String getMsisdn() {
        return getMsisdn(getSubId());
    }

    public String getMsisdn(int subId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getMsisdnForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getVoiceMailNumber() {
        return getVoiceMailNumber(getSubId());
    }

    public String getVoiceMailNumber(int subId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getVoiceMailNumberForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getCompleteVoiceMailNumber() {
        return getCompleteVoiceMailNumber(getSubId());
    }

    public String getCompleteVoiceMailNumber(int subId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getCompleteVoiceMailNumberForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public boolean setVoiceMailNumber(String alphaTag, String number) {
        return setVoiceMailNumber(getSubId(), alphaTag, number);
    }

    public boolean setVoiceMailNumber(int subId, String alphaTag, String number) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setVoiceMailNumber(subId, alphaTag, number);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return false;
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public void setVisualVoicemailEnabled(PhoneAccountHandle phoneAccountHandle, boolean enabled) {
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public boolean isVisualVoicemailEnabled(PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public Bundle getVisualVoicemailSettings() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVisualVoicemailSettings(this.mContext.getOpPackageName(), this.mSubId);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return null;
    }

    public String getVisualVoicemailPackageName() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVisualVoicemailPackageName(this.mContext.getOpPackageName(), getSubId());
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return null;
    }

    public void setVisualVoicemailSmsFilterSettings(VisualVoicemailSmsFilterSettings settings) {
        if (settings == null) {
            disableVisualVoicemailSmsFilter(this.mSubId);
        } else {
            enableVisualVoicemailSmsFilter(this.mSubId, settings);
        }
    }

    public void sendVisualVoicemailSms(String number, int port, String text, PendingIntent sentIntent) {
        sendVisualVoicemailSmsForSubscriber(this.mSubId, number, port, text, sentIntent);
    }

    public void enableVisualVoicemailSmsFilter(int subId, VisualVoicemailSmsFilterSettings settings) {
        if (settings != null) {
            try {
                ITelephony telephony = getITelephony();
                if (telephony != null) {
                    telephony.enableVisualVoicemailSmsFilter(this.mContext.getOpPackageName(), subId, settings);
                }
            } catch (RemoteException | NullPointerException e) {
            }
        } else {
            throw new IllegalArgumentException("Settings cannot be null");
        }
    }

    public void disableVisualVoicemailSmsFilter(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.disableVisualVoicemailSmsFilter(this.mContext.getOpPackageName(), subId);
            }
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVisualVoicemailSmsFilterSettings(this.mContext.getOpPackageName(), subId);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return null;
    }

    public VisualVoicemailSmsFilterSettings getActiveVisualVoicemailSmsFilterSettings(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getActiveVisualVoicemailSmsFilterSettings(subId);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return null;
    }

    public void sendVisualVoicemailSmsForSubscriber(int subId, String number, int port, String text, PendingIntent sentIntent) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.sendVisualVoicemailSmsForSubscriber(this.mContext.getOpPackageName(), subId, number, port, text, sentIntent);
            }
        } catch (RemoteException e) {
        }
    }

    @SystemApi
    public void setVoiceActivationState(int activationState) {
        setVoiceActivationState(getSubId(), activationState);
    }

    public void setVoiceActivationState(int subId, int activationState) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setVoiceActivationState(subId, activationState);
            }
        } catch (RemoteException | NullPointerException e) {
        }
    }

    @SystemApi
    public void setDataActivationState(int activationState) {
        setDataActivationState(getSubId(), activationState);
    }

    public void setDataActivationState(int subId, int activationState) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setDataActivationState(subId, activationState);
            }
        } catch (RemoteException | NullPointerException e) {
        }
    }

    @SystemApi
    public int getVoiceActivationState() {
        return getVoiceActivationState(getSubId());
    }

    public int getVoiceActivationState(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVoiceActivationState(subId, getOpPackageName());
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return 0;
    }

    @SystemApi
    public int getDataActivationState() {
        return getDataActivationState(getSubId());
    }

    public int getDataActivationState(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getDataActivationState(subId, getOpPackageName());
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return 0;
    }

    public int getVoiceMessageCount() {
        return getVoiceMessageCount(getSubId());
    }

    public int getVoiceMessageCount(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return 0;
            }
            return telephony.getVoiceMessageCountForSubscriber(subId);
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    public String getVoiceMailAlphaTag() {
        return getVoiceMailAlphaTag(getSubId());
    }

    public String getVoiceMailAlphaTag(int subId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getVoiceMailAlphaTagForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public void sendDialerSpecialCode(String inputCode) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.sendDialerSpecialCode(this.mContext.getOpPackageName(), inputCode);
            } else if (!isSystemProcess()) {
                throw new RuntimeException("Telephony service unavailable");
            }
        } catch (RemoteException ex) {
            if (!isSystemProcess()) {
                ex.rethrowAsRuntimeException();
            }
        }
    }

    public String getIsimImpi() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getIsimImpi(getSubId());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getIsimDomain() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getIsimDomain(getSubId());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String[] getIsimImpu() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getIsimImpu(getSubId());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    private IPhoneSubInfo getSubscriberInfo() {
        return IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
    }

    public int getCallState() {
        try {
            ITelecomService telecom = getTelecomService();
            if (telecom != null) {
                return telecom.getCallState();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelecomService#getCallState", e);
        }
        return 0;
    }

    public int getCallState(int subId) {
        return getCallStateForSlot(SubscriptionManager.getPhoneId(subId));
    }

    public int getCallStateForSlot(int slotIndex) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return 0;
            }
            return telephony.getCallStateForSlot(slotIndex);
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    public int getDataActivity() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return 0;
            }
            return telephony.getDataActivity();
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    public int getDataState() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return 0;
            }
            return telephony.getDataState();
        } catch (RemoteException e) {
            return 0;
        } catch (NullPointerException e2) {
            return 0;
        }
    }

    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
    }

    private ITelecomService getTelecomService() {
        return ITelecomService.Stub.asInterface(ServiceManager.getService("telecom"));
    }

    private ITelephonyRegistry getTelephonyRegistry() {
        return ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
    }

    public void listen(PhoneStateListener listener, int events) {
        if (this.mContext != null) {
            try {
                boolean notifyNow = getITelephony() != null;
                if (listener.mSubId == null) {
                    listener.mSubId = Integer.valueOf(this.mSubId);
                }
                ITelephonyRegistry registry = getTelephonyRegistry();
                if (registry != null) {
                    registry.listenForSubscriber(listener.mSubId.intValue(), getOpPackageName(), listener.callback, events, notifyNow);
                } else {
                    Rlog.w(TAG, "telephony registry not ready.");
                }
            } catch (RemoteException e) {
                Rlog.w(TAG, "listen, RemoteException occurs");
            } catch (NullPointerException e2) {
                Rlog.w(TAG, "listen, NullPointerException occurs");
            }
        }
    }

    public int getCdmaEriIconIndex() {
        return getCdmaEriIconIndex(getSubId());
    }

    public int getCdmaEriIconIndex(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return -1;
            }
            return telephony.getCdmaEriIconIndexForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e2) {
            return -1;
        }
    }

    public int getCdmaEriIconMode() {
        return getCdmaEriIconMode(getSubId());
    }

    public int getCdmaEriIconMode(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return -1;
            }
            return telephony.getCdmaEriIconModeForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e2) {
            return -1;
        }
    }

    public String getCdmaEriText() {
        return getCdmaEriText(getSubId());
    }

    public String getCdmaEriText(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return null;
            }
            return telephony.getCdmaEriTextForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public boolean isVoiceCapable() {
        if (this.mContext == null) {
            return true;
        }
        Resources resources = this.mContext.getResources();
        if (resources != null) {
            return resources.getBoolean(17957068);
        }
        Rlog.e(TAG, "isVoiceCapable:resources is null");
        return true;
    }

    public boolean isSmsCapable() {
        if (this.mContext == null) {
            return true;
        }
        return this.mContext.getResources().getBoolean(17957026);
    }

    public List<CellInfo> getAllCellInfo() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return null;
            }
            Log.d(TAG, "getAllCellInfo calling app is " + getOpPackageName());
            return telephony.getAllCellInfo(getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public void setCellInfoListRate(int rateInMillis) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setCellInfoListRate(rateInMillis);
            }
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public String getMmsUserAgent() {
        if (this.mContext == null) {
            return null;
        }
        return this.mContext.getResources().getString(17039829);
    }

    public String getMmsUAProfUrl() {
        if (this.mContext == null) {
            return null;
        }
        return this.mContext.getResources().getString(17039830);
    }

    @Deprecated
    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(String AID) {
        return iccOpenLogicalChannel(getSubId(), AID, -1);
    }

    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(String AID, int p2) {
        return iccOpenLogicalChannel(getSubId(), AID, p2);
    }

    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(int subId, String AID, int p2) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.iccOpenLogicalChannel(subId, getOpPackageName(), AID, p2);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return null;
    }

    public boolean iccCloseLogicalChannel(int channel) {
        return iccCloseLogicalChannel(getSubId(), channel);
    }

    public boolean iccCloseLogicalChannel(int subId, int channel) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.iccCloseLogicalChannel(subId, channel);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return false;
    }

    public String iccTransmitApduLogicalChannel(int channel, int cla, int instruction, int p1, int p2, int p3, String data) {
        return iccTransmitApduLogicalChannel(getSubId(), channel, cla, instruction, p1, p2, p3, data);
    }

    public String iccTransmitApduLogicalChannel(int subId, int channel, int cla, int instruction, int p1, int p2, int p3, String data) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.iccTransmitApduLogicalChannel(subId, channel, cla, instruction, p1, p2, p3, data);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return "";
    }

    public String iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data) {
        return iccTransmitApduBasicChannel(getSubId(), cla, instruction, p1, p2, p3, data);
    }

    public String iccTransmitApduBasicChannel(int subId, int cla, int instruction, int p1, int p2, int p3, String data) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.iccTransmitApduBasicChannel(subId, getOpPackageName(), cla, instruction, p1, p2, p3, data);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return "";
    }

    public byte[] iccExchangeSimIO(int fileID, int command, int p1, int p2, int p3, String filePath) {
        return iccExchangeSimIO(getSubId(), fileID, command, p1, p2, p3, filePath);
    }

    public byte[] iccExchangeSimIO(int subId, int fileID, int command, int p1, int p2, int p3, String filePath) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.iccExchangeSimIO(subId, fileID, command, p1, p2, p3, filePath);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return null;
    }

    public String sendEnvelopeWithStatus(String content) {
        return sendEnvelopeWithStatus(getSubId(), content);
    }

    public String sendEnvelopeWithStatus(int subId, String content) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.sendEnvelopeWithStatus(subId, content);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return "";
    }

    public String nvReadItem(int itemID) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.nvReadItem(itemID);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "nvReadItem RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "nvReadItem NPE", ex2);
        }
        return "";
    }

    public boolean nvWriteItem(int itemID, String itemValue) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.nvWriteItem(itemID, itemValue);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "nvWriteItem RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "nvWriteItem NPE", ex2);
        }
        return false;
    }

    public boolean nvWriteCdmaPrl(byte[] preferredRoamingList) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.nvWriteCdmaPrl(preferredRoamingList);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "nvWriteCdmaPrl RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "nvWriteCdmaPrl NPE", ex2);
        }
        return false;
    }

    public boolean nvResetConfig(int resetType) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.nvResetConfig(resetType);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "nvResetConfig RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "nvResetConfig NPE", ex2);
        }
        return false;
    }

    private int getSubId() {
        if (SubscriptionManager.isUsableSubIdValue(this.mSubId)) {
            return this.mSubId;
        }
        return SubscriptionManager.getDefaultSubscriptionId();
    }

    private int getSubId(int preferredSubId) {
        if (SubscriptionManager.isUsableSubIdValue(this.mSubId)) {
            return this.mSubId;
        }
        return preferredSubId;
    }

    private int getPhoneId() {
        return SubscriptionManager.getPhoneId(getSubId());
    }

    private int getPhoneId(int preferredSubId) {
        return SubscriptionManager.getPhoneId(getSubId(preferredSubId));
    }

    @VisibleForTesting
    public int getSlotIndex() {
        int slotIndex = SubscriptionManager.getSlotIndex(getSubId());
        if (slotIndex == -1) {
            return Integer.MAX_VALUE;
        }
        return slotIndex;
    }

    public static void setTelephonyProperty(int phoneId, String property, String value) {
        String propVal = "";
        String[] p = null;
        String prop = SystemProperties.get(property);
        if (value == null) {
            value = "";
        }
        if (prop != null) {
            p = prop.split(",");
        }
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            Rlog.d(TAG, "setTelephonyProperty: invalid phoneId=" + phoneId + " property=" + property + " value: " + value + " prop=" + prop);
            return;
        }
        for (int i = 0; i < phoneId; i++) {
            String str = "";
            if (p != null && i < p.length) {
                str = p[i];
            }
            propVal = propVal + str + ",";
        }
        String propVal2 = propVal + value;
        if (p != null) {
            for (int i2 = phoneId + 1; i2 < p.length; i2++) {
                propVal2 = propVal2 + "," + p[i2];
            }
        }
        if (propVal2.length() > 91) {
            Rlog.d(TAG, "setTelephonyProperty: property too long phoneId=" + phoneId + " property=" + property + " value: " + value + " propVal=" + propVal2);
            return;
        }
        SystemProperties.set(property, propVal2);
    }

    public static void setTelephonyProperty(String property, String value) {
        if (value == null) {
            value = "";
        }
        Rlog.d(TAG, "setTelephonyProperty: success property=" + property + " value: " + value);
        SystemProperties.set(property, value);
    }

    public static int getIntAtIndex(ContentResolver cr, String name, int index) throws Settings.SettingNotFoundException {
        String v = Settings.Global.getString(cr, name);
        if (NETWROK_TYPE_COMMA_SWITCH) {
            v = HwFrameworkFactory.getHwInnerTelephonyManager().updatePreferNetworkModeValArray(v, name);
        }
        if (v != null) {
            String[] valArray = v.split(",");
            if (index >= 0 && index < valArray.length && valArray[index] != null) {
                try {
                    return Integer.parseInt(valArray[index]);
                } catch (NumberFormatException e) {
                }
            }
        }
        throw new Settings.SettingNotFoundException(name);
    }

    public static boolean putIntAtIndex(ContentResolver cr, String name, int index, int value) {
        String data = "";
        String[] valArray = null;
        String v = Settings.Global.getString(cr, name);
        if (NETWROK_TYPE_COMMA_SWITCH) {
            v = HwFrameworkFactory.getHwInnerTelephonyManager().updatePreferNetworkModeValArray(v, name);
        }
        if (index == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("putIntAtIndex index == MAX_VALUE index=" + index);
        } else if (index >= 0) {
            if (v != null) {
                valArray = v.split(",");
            }
            for (int i = 0; i < index; i++) {
                String str = "";
                if (valArray != null && i < valArray.length) {
                    str = valArray[i];
                }
                data = data + str + ",";
            }
            String data2 = data + value;
            if (valArray != null) {
                for (int i2 = index + 1; i2 < valArray.length; i2++) {
                    data2 = data2 + "," + valArray[i2];
                }
            }
            return Settings.Global.putString(cr, name, data2);
        } else {
            throw new IllegalArgumentException("putIntAtIndex index < 0 index=" + index);
        }
    }

    public static String getTelephonyProperty(int phoneId, String property, String defaultVal) {
        String propVal = null;
        String prop = SystemProperties.get(property);
        if (prop != null && prop.length() > 0) {
            String[] values = prop.split(",");
            if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                propVal = values[phoneId];
            }
        }
        return propVal == null ? defaultVal : propVal;
    }

    public static String getTelephonyProperty(String property, String defaultVal) {
        String propVal = SystemProperties.get(property);
        return propVal == null ? defaultVal : propVal;
    }

    public int getSimCount() {
        if (isMultiSimEnabled()) {
            return 2;
        }
        return 1;
    }

    public String getIsimIst() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getIsimIst(getSubId());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String[] getIsimPcscf() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getIsimPcscf(getSubId());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getIccAuthentication(int appType, int authType, String data) {
        return getIccAuthentication(getSubId(), appType, authType, data);
    }

    public String getIccAuthentication(int subId, int appType, int authType, String data) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getIccSimChallengeResponse(subId, appType, authType, data);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String[] getForbiddenPlmns() {
        return getForbiddenPlmns(getSubId(), 2);
    }

    public String[] getForbiddenPlmns(int subId, int appType) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return null;
            }
            return telephony.getForbiddenPlmns(subId, appType, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String[] getPcscfAddress(String apnType) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return new String[0];
            }
            return telephony.getPcscfAddress(apnType, getOpPackageName());
        } catch (RemoteException e) {
            return new String[0];
        }
    }

    public void enableIms(int slotId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.enableIms(slotId);
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "enableIms, RemoteException: " + e.getMessage());
        }
    }

    public void disableIms(int slotId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.disableIms(slotId);
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "disableIms, RemoteException: " + e.getMessage());
        }
    }

    public IImsMmTelFeature getImsMmTelFeatureAndListen(int slotIndex, IImsServiceFeatureCallback callback) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getMmTelFeatureAndListen(slotIndex, callback);
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsMmTelFeatureAndListen, RemoteException: " + e.getMessage());
        }
        return null;
    }

    public IImsRcsFeature getImsRcsFeatureAndListen(int slotIndex, IImsServiceFeatureCallback callback) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getRcsFeatureAndListen(slotIndex, callback);
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsRcsFeatureAndListen, RemoteException: " + e.getMessage());
        }
        return null;
    }

    public IImsRegistration getImsRegistration(int slotIndex, int feature) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getImsRegistration(slotIndex, feature);
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsRegistration, RemoteException: " + e.getMessage());
        }
        return null;
    }

    public IImsConfig getImsConfig(int slotIndex, int feature) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getImsConfig(slotIndex, feature);
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsRegistration, RemoteException: " + e.getMessage());
        }
        return null;
    }

    public boolean isResolvingImsBinding() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isResolvingImsBinding();
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "isResolvingImsBinding, RemoteException: " + e.getMessage());
        }
        return false;
    }

    public void setImsRegistrationState(boolean registered) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setImsRegistrationState(registered);
            }
        } catch (RemoteException e) {
        }
    }

    public int getPreferredNetworkType(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getPreferredNetworkType(subId);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getPreferredNetworkType RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getPreferredNetworkType NPE", ex2);
        }
        return -1;
    }

    public void setNetworkSelectionModeAutomatic() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setNetworkSelectionModeAutomatic(getSubId());
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setNetworkSelectionModeAutomatic RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setNetworkSelectionModeAutomatic NPE", ex2);
        }
    }

    public void setNetworkSelectionModeAutomatic(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setNetworkSelectionModeAutomatic(subId);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setNetworkSelectionModeAutomatic RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setNetworkSelectionModeAutomatic NPE", ex2);
        }
    }

    public CellNetworkScanResult getCellNetworkScanResults(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getCellNetworkScanResults(subId);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getCellNetworkScanResults RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getCellNetworkScanResults NPE", ex2);
        }
        return null;
    }

    public NetworkScan requestNetworkScan(NetworkScanRequest request, Executor executor, TelephonyScanManager.NetworkScanCallback callback) {
        synchronized (this) {
            if (this.mTelephonyScanManager == null) {
                this.mTelephonyScanManager = new TelephonyScanManager();
            }
        }
        return this.mTelephonyScanManager.requestNetworkScan(getSubId(), request, executor, callback);
    }

    @Deprecated
    public NetworkScan requestNetworkScan(NetworkScanRequest request, TelephonyScanManager.NetworkScanCallback callback) {
        return requestNetworkScan(request, AsyncTask.SERIAL_EXECUTOR, callback);
    }

    public boolean setNetworkSelectionModeManual(String operatorNumeric, boolean persistSelection) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setNetworkSelectionModeManual(getSubId(), operatorNumeric, persistSelection);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setNetworkSelectionModeManual RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setNetworkSelectionModeManual NPE", ex2);
        }
        return false;
    }

    public boolean setNetworkSelectionModeManual(int subId, String operatorNumeric, boolean persistSelection) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setNetworkSelectionModeManual(subId, operatorNumeric, persistSelection);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setNetworkSelectionModeManual RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setNetworkSelectionModeManual NPE", ex2);
        }
        return false;
    }

    public boolean setPreferredNetworkType(int subId, int networkType) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setPreferredNetworkType(subId, networkType);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setPreferredNetworkType RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setPreferredNetworkType NPE", ex2);
        }
        return false;
    }

    public boolean setPreferredNetworkTypeToGlobal() {
        return setPreferredNetworkTypeToGlobal(getSubId());
    }

    public boolean setPreferredNetworkTypeToGlobal(int subId) {
        return setPreferredNetworkType(subId, 10);
    }

    public int getTetherApnRequired() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getTetherApnRequired();
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "hasMatchedTetherApnSetting RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "hasMatchedTetherApnSetting NPE", ex2);
        }
        return 2;
    }

    public boolean hasCarrierPrivileges() {
        return hasCarrierPrivileges(getSubId());
    }

    public boolean hasCarrierPrivileges(int subId) {
        boolean z = false;
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                if (telephony.getCarrierPrivilegeStatus(this.mSubId) == 1) {
                    z = true;
                }
                return z;
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "hasCarrierPrivileges RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "hasCarrierPrivileges NPE", ex2);
        }
        return false;
    }

    public boolean setOperatorBrandOverride(String brand) {
        return setOperatorBrandOverride(getSubId(), brand);
    }

    public boolean setOperatorBrandOverride(int subId, String brand) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setOperatorBrandOverride(subId, brand);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setOperatorBrandOverride RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setOperatorBrandOverride NPE", ex2);
        }
        return false;
    }

    public boolean setRoamingOverride(List<String> gsmRoamingList, List<String> gsmNonRoamingList, List<String> cdmaRoamingList, List<String> cdmaNonRoamingList) {
        return setRoamingOverride(getSubId(), gsmRoamingList, gsmNonRoamingList, cdmaRoamingList, cdmaNonRoamingList);
    }

    public boolean setRoamingOverride(int subId, List<String> gsmRoamingList, List<String> gsmNonRoamingList, List<String> cdmaRoamingList, List<String> cdmaNonRoamingList) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setRoamingOverride(subId, gsmRoamingList, gsmNonRoamingList, cdmaRoamingList, cdmaNonRoamingList);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "setRoamingOverride RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "setRoamingOverride NPE", ex2);
        }
        return false;
    }

    @SystemApi
    public String getCdmaMdn() {
        return getCdmaMdn(getSubId());
    }

    @SystemApi
    public String getCdmaMdn(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return null;
            }
            return telephony.getCdmaMdn(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    @SystemApi
    public String getCdmaMin() {
        return getCdmaMin(getSubId());
    }

    @SystemApi
    public String getCdmaMin(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return null;
            }
            return telephony.getCdmaMin(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public int checkCarrierPrivilegesForPackage(String pkgName) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.checkCarrierPrivilegesForPackage(pkgName);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "checkCarrierPrivilegesForPackage RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "checkCarrierPrivilegesForPackage NPE", ex2);
        }
        return 0;
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public int checkCarrierPrivilegesForPackageAnyPhone(String pkgName) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.checkCarrierPrivilegesForPackageAnyPhone(pkgName);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "checkCarrierPrivilegesForPackageAnyPhone RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "checkCarrierPrivilegesForPackageAnyPhone NPE", ex2);
        }
        return 0;
    }

    @SystemApi
    public List<String> getCarrierPackageNamesForIntent(Intent intent) {
        return getCarrierPackageNamesForIntentAndPhone(intent, getPhoneId());
    }

    @SystemApi
    public List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int phoneId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getCarrierPackageNamesForIntentAndPhone(intent, phoneId);
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getCarrierPackageNamesForIntentAndPhone RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getCarrierPackageNamesForIntentAndPhone NPE", ex2);
        }
        return null;
    }

    public List<String> getPackagesWithCarrierPrivileges() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getPackagesWithCarrierPrivileges();
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "getPackagesWithCarrierPrivileges RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "getPackagesWithCarrierPrivileges NPE", ex2);
        }
        return Collections.EMPTY_LIST;
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public void dial(String number) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.dial(number);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#dial", e);
        }
    }

    @SystemApi
    @Deprecated
    public void call(String callingPackage, String number) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.call(callingPackage, number);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#call", e);
        }
    }

    @SystemApi
    @Deprecated
    public boolean endCall() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.endCall();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#endCall", e);
        }
        return false;
    }

    @SystemApi
    @Deprecated
    public void answerRingingCall() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.answerRingingCall();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#answerRingingCall", e);
        }
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    @Deprecated
    public void silenceRinger() {
        try {
            getTelecomService().silenceRinger(getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelecomService#silenceRinger", e);
        }
    }

    @SystemApi
    public boolean isOffhook() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isOffhook(getOpPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isOffhook", e);
        }
        return false;
    }

    @SystemApi
    public boolean isRinging() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isRinging(getOpPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isRinging", e);
        }
        return false;
    }

    @SystemApi
    public boolean isIdle() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isIdle(getOpPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isIdle", e);
        }
        return true;
    }

    @SystemApi
    public boolean isRadioOn() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isRadioOn(getOpPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isRadioOn", e);
        }
        return false;
    }

    @SystemApi
    public boolean supplyPin(String pin) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.supplyPin(pin);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#supplyPin", e);
        }
        return false;
    }

    @SystemApi
    public boolean supplyPuk(String puk, String pin) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.supplyPuk(puk, pin);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#supplyPuk", e);
        }
        return false;
    }

    @SystemApi
    public int[] supplyPinReportResult(String pin) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.supplyPinReportResult(pin);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#supplyPinReportResult", e);
        }
        return new int[0];
    }

    @SystemApi
    public int[] supplyPukReportResult(String puk, String pin) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.supplyPukReportResult(puk, pin);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#]", e);
        }
        return new int[0];
    }

    public void sendUssdRequest(String ussdRequest, final UssdResponseCallback callback, Handler handler) {
        Preconditions.checkNotNull(callback, "UssdResponseCallback cannot be null.");
        ResultReceiver wrappedCallback = new ResultReceiver(handler) {
            /* access modifiers changed from: protected */
            public void onReceiveResult(int resultCode, Bundle ussdResponse) {
                Rlog.d(TelephonyManager.TAG, "USSD:" + resultCode);
                Preconditions.checkNotNull(ussdResponse, "ussdResponse cannot be null.");
                UssdResponse response = (UssdResponse) ussdResponse.getParcelable(TelephonyManager.USSD_RESPONSE);
                if (response == null) {
                    return;
                }
                if (resultCode == 100) {
                    callback.onReceiveUssdResponse(this, response.getUssdRequest(), response.getReturnMessage());
                } else {
                    callback.onReceiveUssdResponseFailed(this, response.getUssdRequest(), resultCode);
                }
            }
        };
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.handleUssdRequest(getSubId(), ussdRequest, wrappedCallback);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#sendUSSDCode", e);
            UssdResponse response = new UssdResponse(ussdRequest, "");
            Bundle returnData = new Bundle();
            returnData.putParcelable(USSD_RESPONSE, response);
            wrappedCallback.send(-2, returnData);
        }
    }

    public boolean isConcurrentVoiceAndDataSupported() {
        boolean z = false;
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                z = telephony.isConcurrentVoiceAndDataAllowed(getSubId());
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isConcurrentVoiceAndDataAllowed", e);
            return false;
        }
    }

    @SystemApi
    public boolean handlePinMmi(String dialString) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.handlePinMmi(dialString);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#handlePinMmi", e);
        }
        return false;
    }

    @SystemApi
    public boolean handlePinMmiForSubscriber(int subId, String dialString) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.handlePinMmiForSubscriber(subId, dialString);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#handlePinMmi", e);
        }
        return false;
    }

    @SystemApi
    public void toggleRadioOnOff() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.toggleRadioOnOff();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#toggleRadioOnOff", e);
        }
    }

    @SystemApi
    public boolean setRadio(boolean turnOn) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setRadio(turnOn);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setRadio", e);
        }
        return false;
    }

    @SystemApi
    public boolean setRadioPower(boolean turnOn) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setRadioPower(turnOn);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setRadioPower", e);
        }
        return false;
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public void updateServiceLocation() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.updateServiceLocation();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#updateServiceLocation", e);
        }
    }

    @SystemApi
    public boolean enableDataConnectivity() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.enableDataConnectivity();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#enableDataConnectivity", e);
        }
        return false;
    }

    @SystemApi
    public boolean disableDataConnectivity() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.disableDataConnectivity();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#disableDataConnectivity", e);
        }
        return false;
    }

    @SystemApi
    public boolean isDataConnectivityPossible() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isDataConnectivityPossible(getSubId(SubscriptionManager.getDefaultDataSubscriptionId()));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isDataAllowed", e);
        }
        return false;
    }

    @SystemApi
    public boolean needsOtaServiceProvisioning() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.needsOtaServiceProvisioning();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#needsOtaServiceProvisioning", e);
        }
        return false;
    }

    public void setDataEnabled(boolean enable) {
        HwFrameworkFactory.getHwInnerTelephonyManager().printCallingAppNameInfo(enable, this.mContext);
        if (this.mCust == null || !this.mCust.isVZW()) {
            setDataEnabled(getSubId(SubscriptionManager.getDefaultDataSubscriptionId()), enable);
        } else {
            this.mCust.setDataEnabledVZW(this.mContext, SubscriptionManager.getDefaultDataSubscriptionId(), enable);
        }
    }

    @SystemApi
    @Deprecated
    public void setDataEnabled(int subId, boolean enable) {
        try {
            Log.d(TAG, "setDataEnabled: enabled=" + enable);
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setUserDataEnabled(subId, enable);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setUserDataEnabled", e);
        }
    }

    public void setDataEnabledProperties(String appName, boolean enable) {
        Log.d(TAG, "setDataEnabledProperties: appName=" + appName + " enable=" + enable);
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setDataEnabledProperties(appName, enable);
            }
        } catch (Exception ex) {
            Rlog.e(TAG, "setDataEnabledProperties Exception:", ex);
        }
    }

    @SystemApi
    @Deprecated
    public boolean getDataEnabled() {
        return isDataEnabled();
    }

    public boolean isDataEnabled() {
        return getDataEnabled(getSubId(SubscriptionManager.getDefaultDataSubscriptionId()));
    }

    @SystemApi
    @Deprecated
    public boolean getDataEnabled(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isUserDataEnabled(subId);
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isUserDataEnabled", e);
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    @Deprecated
    public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.invokeOemRilRequestRaw(oemReq, oemResp);
            }
        } catch (RemoteException | NullPointerException e) {
        }
        return -1;
    }

    @SystemApi
    public void enableVideoCalling(boolean enable) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.enableVideoCalling(enable);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#enableVideoCalling", e);
        }
    }

    @SystemApi
    public boolean isVideoCallingEnabled() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isVideoCallingEnabled(getOpPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isVideoCallingEnabled", e);
        }
        return false;
    }

    public boolean canChangeDtmfToneLength() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.canChangeDtmfToneLength();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#canChangeDtmfToneLength", e);
        } catch (SecurityException e2) {
            Log.e(TAG, "Permission error calling ITelephony#canChangeDtmfToneLength", e2);
        }
        return false;
    }

    public boolean isWorldPhone() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isWorldPhone();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isWorldPhone", e);
        } catch (SecurityException e2) {
            Log.e(TAG, "Permission error calling ITelephony#isWorldPhone", e2);
        }
        return false;
    }

    @Deprecated
    public boolean isTtyModeSupported() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isTtyModeSupported();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isTtyModeSupported", e);
        } catch (SecurityException e2) {
            Log.e(TAG, "Permission error calling ITelephony#isTtyModeSupported", e2);
        }
        return false;
    }

    public boolean isHearingAidCompatibilitySupported() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isHearingAidCompatibilitySupported();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isHearingAidCompatibilitySupported", e);
        } catch (SecurityException e2) {
            Log.e(TAG, "Permission error calling ITelephony#isHearingAidCompatibilitySupported", e2);
        }
        return false;
    }

    public boolean isImsRegistered(int subId) {
        try {
            return getITelephony().isImsRegistered(subId);
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public boolean isImsRegistered() {
        try {
            return getITelephony().isImsRegistered(getSubId());
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public boolean isVolteAvailable() {
        try {
            if (getITelephony() == null) {
                return false;
            }
            return getITelephony().isVolteAvailable(getSubId());
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public boolean isVideoTelephonyAvailable() {
        try {
            return getITelephony().isVideoTelephonyAvailable(getSubId());
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public boolean isWifiCallingAvailable() {
        try {
            if (getITelephony() == null) {
                return false;
            }
            return getITelephony().isWifiCallingAvailable(getSubId());
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    public int getImsRegTechnologyForMmTel() {
        try {
            return getITelephony().getImsRegTechnologyForMmTel(getSubId());
        } catch (RemoteException e) {
            return -1;
        }
    }

    public void setSimOperatorNumeric(String numeric) {
        setSimOperatorNumericForPhone(getPhoneId(), numeric);
    }

    public void setSimOperatorNumericForPhone(int phoneId, String numeric) {
        setTelephonyProperty(phoneId, "gsm.sim.operator.numeric", numeric);
    }

    public void setSimOperatorName(String name) {
        setSimOperatorNameForPhone(getPhoneId(), name);
    }

    public void setSimOperatorNameForPhone(int phoneId, String name) {
        setTelephonyProperty(phoneId, "gsm.sim.operator.alpha", name);
    }

    public void setSimCountryIso(String iso) {
        setSimCountryIsoForPhone(getPhoneId(), iso);
    }

    public void setSimCountryIsoForPhone(int phoneId, String iso) {
        setTelephonyProperty(phoneId, "gsm.sim.operator.iso-country", iso);
    }

    public void setSimState(String state) {
        setSimStateForPhone(getPhoneId(), state);
    }

    public void setSimStateForPhone(int phoneId, String state) {
        setTelephonyProperty(phoneId, "gsm.sim.state", state);
    }

    @SystemApi
    public void setSimPowerState(int state) {
        setSimPowerStateForSlot(getSlotIndex(), state);
    }

    @SystemApi
    public void setSimPowerStateForSlot(int slotIndex, int state) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setSimPowerStateForSlot(slotIndex, state);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setSimPowerStateForSlot", e);
        } catch (SecurityException e2) {
            Log.e(TAG, "Permission error calling ITelephony#setSimPowerStateForSlot", e2);
        }
    }

    public void setBasebandVersion(String version) {
        setBasebandVersionForPhone(getPhoneId(), version);
    }

    public void setBasebandVersionForPhone(int phoneId, String version) {
        setTelephonyProperty(phoneId, "gsm.version.baseband", version);
    }

    public String getBasebandVersion() {
        return getBasebandVersionForPhone(getPhoneId());
    }

    private String getBasebandVersionLegacy(int phoneId) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("gsm.version.baseband");
        sb.append(phoneId == 0 ? "" : Integer.toString(phoneId));
        return SystemProperties.get(sb.toString());
    }

    public String getBasebandVersionForPhone(int phoneId) {
        String version = getBasebandVersionLegacy(phoneId);
        if (version != null && !version.isEmpty()) {
            setBasebandVersionForPhone(phoneId, version);
        }
        return getTelephonyProperty(phoneId, "gsm.version.baseband", "");
    }

    public void setPhoneType(int type) {
        setPhoneType(getPhoneId(), type);
    }

    public void setPhoneType(int phoneId, int type) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, "gsm.current.phone-type", String.valueOf(type));
        }
    }

    public String getOtaSpNumberSchema(String defaultValue) {
        return getOtaSpNumberSchemaForPhone(getPhoneId(), defaultValue);
    }

    public String getOtaSpNumberSchemaForPhone(int phoneId, String defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return getTelephonyProperty(phoneId, "ro.cdma.otaspnumschema", defaultValue);
        }
        return defaultValue;
    }

    public boolean getSmsReceiveCapable(boolean defaultValue) {
        return getSmsReceiveCapableForPhone(getPhoneId(), defaultValue);
    }

    public boolean getSmsReceiveCapableForPhone(int phoneId, boolean defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return Boolean.parseBoolean(getTelephonyProperty(phoneId, "telephony.sms.receive", String.valueOf(defaultValue)));
        }
        return defaultValue;
    }

    public boolean getSmsSendCapable(boolean defaultValue) {
        return getSmsSendCapableForPhone(getPhoneId(), defaultValue);
    }

    public boolean getSmsSendCapableForPhone(int phoneId, boolean defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return Boolean.parseBoolean(getTelephonyProperty(phoneId, "telephony.sms.send", String.valueOf(defaultValue)));
        }
        return defaultValue;
    }

    public void setNetworkOperatorName(String name) {
        setNetworkOperatorNameForPhone(getPhoneId(), name);
    }

    public void setNetworkOperatorNameForPhone(int phoneId, String name) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, "gsm.operator.alpha", name);
        }
    }

    public void setNetworkOperatorNumeric(String numeric) {
        setNetworkOperatorNumericForPhone(getPhoneId(), numeric);
    }

    public void setNetworkOperatorNumericForPhone(int phoneId, String numeric) {
        setTelephonyProperty(phoneId, "gsm.operator.numeric", numeric);
    }

    public void setNetworkRoaming(boolean isRoaming) {
        setNetworkRoamingForPhone(getPhoneId(), isRoaming);
    }

    public void setNetworkRoamingForPhone(int phoneId, boolean isRoaming) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, "gsm.operator.isroaming", isRoaming ? "true" : "false");
        }
    }

    public void setNetworkCountryIso(String iso) {
        setNetworkCountryIsoForPhone(getPhoneId(), iso);
    }

    public void setNetworkCountryIsoForPhone(int phoneId, String iso) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, "gsm.operator.iso-country", iso);
        }
    }

    public void setDataNetworkType(int type) {
        setDataNetworkTypeForPhone(getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()), type);
    }

    public void setDataNetworkTypeForPhone(int phoneId, int type) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, "gsm.network.type", ServiceState.rilRadioTechnologyToString(type));
        }
    }

    public static boolean isSms7BitEnabled() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().isSms7BitEnabled();
    }

    public int getSubIdForPhoneAccount(PhoneAccount phoneAccount) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getSubIdForPhoneAccount(phoneAccount);
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        } catch (RuntimeException e2) {
            Log.e(TAG, "Error calling ITelephony#getSubIdForPhoneAccount RuntimeException");
            return -1;
        }
    }

    private int getSubIdForPhoneAccountHandle(PhoneAccountHandle phoneAccountHandle) {
        try {
            ITelecomService service = getTelecomService();
            if (service != null) {
                return getSubIdForPhoneAccount(service.getPhoneAccount(phoneAccountHandle));
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public void factoryReset(int subId) {
        try {
            Log.d(TAG, "factoryReset: subId=" + subId);
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.factoryReset(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public String getLocaleFromDefaultSim() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getLocaleFromDefaultSim();
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public void requestModemActivityInfo(ResultReceiver result) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                service.requestModemActivityInfo(result);
                return;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getModemActivityInfo", e);
        }
        result.send(0, null);
    }

    public ServiceState getServiceState() {
        return getServiceStateForSubscriber(getSubId());
    }

    public ServiceState getServiceStateForSubscriber(int subId) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getServiceStateForSubscriber(subId, getOpPackageName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getServiceStateForSubscriber", e);
        }
        return null;
    }

    public Uri getVoicemailRingtoneUri(PhoneAccountHandle accountHandle) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getVoicemailRingtoneUri(accountHandle);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getVoicemailRingtoneUri", e);
        }
        return null;
    }

    public void setVoicemailRingtoneUri(PhoneAccountHandle phoneAccountHandle, Uri uri) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                service.setVoicemailRingtoneUri(getOpPackageName(), phoneAccountHandle, uri);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setVoicemailRingtoneUri", e);
        }
    }

    public boolean isVoicemailVibrationEnabled(PhoneAccountHandle accountHandle) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.isVoicemailVibrationEnabled(accountHandle);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isVoicemailVibrationEnabled", e);
        }
        return false;
    }

    public void setVoicemailVibrationEnabled(PhoneAccountHandle phoneAccountHandle, boolean enabled) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                service.setVoicemailVibrationEnabled(getOpPackageName(), phoneAccountHandle, enabled);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isVoicemailVibrationEnabled", e);
        }
    }

    public int getSimCarrierId() {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getSubscriptionCarrierId(getSubId());
            }
        } catch (RemoteException e) {
        }
        return -1;
    }

    public CharSequence getSimCarrierIdName() {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getSubscriptionCarrierName(getSubId());
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public String getAidForAppType(int appType) {
        return getAidForAppType(getSubId(), appType);
    }

    public String getAidForAppType(int subId, int appType) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getAidForAppType(subId, appType);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getAidForAppType", e);
        }
        return null;
    }

    public String getEsn() {
        return getEsn(getSubId());
    }

    public String getEsn(int subId) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getEsn(subId);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getEsn", e);
        }
        return null;
    }

    @SystemApi
    public String getCdmaPrlVersion() {
        return getCdmaPrlVersion(getSubId());
    }

    public String getCdmaPrlVersion(int subId) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getCdmaPrlVersion(subId);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getCdmaPrlVersion", e);
        }
        return null;
    }

    @SystemApi
    public List<TelephonyHistogram> getTelephonyHistograms() {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getTelephonyHistograms();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getTelephonyHistograms", e);
        }
        return null;
    }

    @SystemApi
    public int setAllowedCarriers(int slotIndex, List<CarrierIdentifier> carriers) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.setAllowedCarriers(slotIndex, carriers);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setAllowedCarriers", e);
        } catch (NullPointerException e2) {
            Log.e(TAG, "Error calling ITelephony#setAllowedCarriers", e2);
        }
        return -1;
    }

    @SystemApi
    public List<CarrierIdentifier> getAllowedCarriers(int slotIndex) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getAllowedCarriers(slotIndex);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getAllowedCarriers", e);
        } catch (NullPointerException e2) {
            Log.e(TAG, "Error calling ITelephony#getAllowedCarriers", e2);
        }
        return new ArrayList(0);
    }

    public void carrierActionSetMeteredApnsEnabled(int subId, boolean enabled) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                service.carrierActionSetMeteredApnsEnabled(subId, enabled);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#carrierActionSetMeteredApnsEnabled", e);
        }
    }

    public void carrierActionSetRadioEnabled(int subId, boolean enabled) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                service.carrierActionSetRadioEnabled(subId, enabled);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#carrierActionSetRadioEnabled", e);
        }
    }

    public void carrierActionReportDefaultNetworkStatus(int subId, boolean report) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                service.carrierActionReportDefaultNetworkStatus(subId, report);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#carrierActionReportDefaultNetworkStatus", e);
        }
    }

    public NetworkStats getVtDataUsage(int how) {
        boolean perUidStats = true;
        if (how != 1) {
            perUidStats = false;
        }
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getVtDataUsage(getSubId(), perUidStats);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getVtDataUsage", e);
        }
        return null;
    }

    public void setPolicyDataEnabled(boolean enabled, int subId) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                service.setPolicyDataEnabled(enabled, subId);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setPolicyDataEnabled", e);
        }
    }

    public List<ClientRequestStats> getClientRequestStats(int subId) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getClientRequestStats(getOpPackageName(), subId);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getClientRequestStats", e);
        }
        return null;
    }

    @SystemApi
    public boolean getEmergencyCallbackMode() {
        return getEmergencyCallbackMode(getSubId());
    }

    public boolean getEmergencyCallbackMode(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return false;
            }
            return telephony.getEmergencyCallbackMode(subId);
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getEmergencyCallbackMode", e);
            return false;
        }
    }

    public SignalStrength getSignalStrength() {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getSignalStrength(getSubId());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getSignalStrength", e);
        }
        return null;
    }

    public int[] supplyPinReportResultForSubscriber(int subId, String pin) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.supplyPinReportResultForSubscriber(subId, pin);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#supplyPinReportResultForSubscriber", e);
        }
        return new int[0];
    }

    public int[] supplyPukReportResultForSubscriber(int subId, String puk, String pin) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.supplyPukReportResultForSubscriber(subId, puk, pin);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#]", e);
        }
        return new int[0];
    }

    public boolean isDataCapable() {
        try {
            int subId = getSubId(SubscriptionManager.getDefaultDataSubscriptionId());
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isDataEnabled(subId);
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isDataEnabled", e);
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public void setRadioIndicationUpdateMode(int filters, int updateMode) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setRadioIndicationUpdateMode(getSubId(), filters, updateMode);
            }
        } catch (RemoteException ex) {
            if (!isSystemProcess()) {
                ex.rethrowAsRuntimeException();
            }
        }
    }

    public void setCarrierTestOverride(String mccmnc, String imsi, String iccid, String gid1, String gid2, String plmn, String spn) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setCarrierTestOverride(getSubId(), mccmnc, imsi, iccid, gid1, gid2, plmn, spn);
            }
        } catch (RemoteException e) {
        }
    }

    public int getCarrierIdListVersion() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getCarrierIdListVersion(getSubId());
            }
        } catch (RemoteException e) {
        }
        return -1;
    }
}
