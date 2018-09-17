package android.telephony;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.service.carrier.CarrierIdentifier;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.util.LogException;
import com.android.ims.internal.IImsServiceController;
import com.android.ims.internal.IImsServiceFeatureListener;
import com.android.internal.R;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.CellNetworkScanResult;
import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.util.Preconditions;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelephonyManager {
    private static final /* synthetic */ int[] -android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues = null;
    public static final String ACTION_CONFIGURE_VOICEMAIL = "android.telephony.action.CONFIGURE_VOICEMAIL";
    public static final String ACTION_EMERGENCY_ASSISTANCE = "android.telephony.action.EMERGENCY_ASSISTANCE";
    public static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
    public static final String ACTION_PRECISE_CALL_STATE_CHANGED = "android.intent.action.PRECISE_CALL_STATE";
    @Deprecated
    public static final String ACTION_PRECISE_DATA_CONNECTION_STATE_CHANGED = "android.intent.action.PRECISE_DATA_CONNECTION_STATE_CHANGED";
    public static final String ACTION_RESPOND_VIA_MESSAGE = "android.intent.action.RESPOND_VIA_MESSAGE";
    public static final String ACTION_SHOW_VOICEMAIL_NOTIFICATION = "android.telephony.action.SHOW_VOICEMAIL_NOTIFICATION";
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
    public static final int CARRIER_PRIVILEGE_STATUS_ERROR_LOADING_RULES = -2;
    public static final int CARRIER_PRIVILEGE_STATUS_HAS_ACCESS = 1;
    public static final int CARRIER_PRIVILEGE_STATUS_NO_ACCESS = 0;
    public static final int CARRIER_PRIVILEGE_STATUS_RULES_NOT_LOADED = -1;
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
    public static final String EVENT_DOWNGRADE_DATA_DISABLED = "android.telephony.event.EVENT_DOWNGRADE_DATA_DISABLED";
    public static final String EVENT_DOWNGRADE_DATA_LIMIT_REACHED = "android.telephony.event.EVENT_DOWNGRADE_DATA_LIMIT_REACHED";
    public static final String EVENT_HANDOVER_TO_WIFI_FAILED = "android.telephony.event.EVENT_HANDOVER_TO_WIFI_FAILED";
    public static final String EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI = "android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI";
    public static final String EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE = "android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE";
    public static final String EVENT_NOTIFY_INTERNATIONAL_CALL_ON_WFC = "android.telephony.event.EVENT_NOTIFY_INTERNATIONAL_CALL_ON_WFC";
    public static final String EXTRA_BACKGROUND_CALL_STATE = "background_state";
    public static final String EXTRA_CALL_VOICEMAIL_INTENT = "android.telephony.extra.CALL_VOICEMAIL_INTENT";
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
    public static final String EXTRA_LAUNCH_VOICEMAIL_SETTINGS_INTENT = "android.telephony.extra.LAUNCH_VOICEMAIL_SETTINGS_INTENT";
    public static final String EXTRA_NOTIFICATION_COUNT = "android.telephony.extra.NOTIFICATION_COUNT";
    public static final String EXTRA_PHONE_ACCOUNT_HANDLE = "android.telephony.extra.PHONE_ACCOUNT_HANDLE";
    public static final String EXTRA_PRECISE_DISCONNECT_CAUSE = "precise_disconnect_cause";
    public static final String EXTRA_RINGING_CALL_STATE = "ringing_state";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_STATE_IDLE = State.IDLE.toString();
    public static final String EXTRA_STATE_OFFHOOK = State.OFFHOOK.toString();
    public static final String EXTRA_STATE_RINGING = State.RINGING.toString();
    public static final String EXTRA_VISUAL_VOICEMAIL_ENABLED_BY_USER_BOOL = "android.telephony.extra.VISUAL_VOICEMAIL_ENABLED_BY_USER_BOOL";
    public static final String EXTRA_VOICEMAIL_NUMBER = "android.telephony.extra.VOICEMAIL_NUMBER";
    public static final String EXTRA_VOICEMAIL_SCRAMBLED_PIN_STRING = "android.telephony.extra.VOICEMAIL_SCRAMBLED_PIN_STRING";
    public static final int ICCID_BEGIN = 0;
    public static final int ICCID_END = 19;
    public static final boolean IS_CHINA_TELECOM;
    public static final String METADATA_HIDE_VOICEMAIL_SETTINGS_MENU = "android.telephony.HIDE_VOICEMAIL_SETTINGS_MENU";
    public static final String MODEM_ACTIVITY_RESULT_KEY = "controller_activity";
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;
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
    public static final int NETWORK_TYPE_TD_SCDMA = 17;
    public static final int NETWORK_TYPE_UMTS = 3;
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    public static final boolean NETWROK_TYPE_COMMA_SWITCH = SystemProperties.getBoolean("ro.hwpps.network.type.comma", false);
    public static final int OTASP_NEEDED = 2;
    public static final int OTASP_NOT_NEEDED = 3;
    public static final int OTASP_SIM_UNPROVISIONED = 5;
    public static final int OTASP_UNINITIALIZED = 0;
    public static final int OTASP_UNKNOWN = 1;
    public static final int PHONE_TYPE_CDMA = 2;
    public static final int PHONE_TYPE_GSM = 1;
    public static final int PHONE_TYPE_NONE = 0;
    public static final int PHONE_TYPE_SIP = 3;
    public static final int PREFERRED_NETWORK_MODE_CFG_NUM = 1;
    public static final int SCOPE_ALL = 0;
    public static final int SCOPE_IMEI = 1;
    public static final int SIM_ACTIVATION_STATE_ACTIVATED = 2;
    public static final int SIM_ACTIVATION_STATE_ACTIVATING = 1;
    public static final int SIM_ACTIVATION_STATE_DEACTIVATED = 3;
    public static final int SIM_ACTIVATION_STATE_RESTRICTED = 4;
    public static final int SIM_ACTIVATION_STATE_UNKNOWN = 0;
    public static final int SIM_STATE_ABSENT = 1;
    public static final int SIM_STATE_CARD_IO_ERROR = 8;
    public static final int SIM_STATE_CARD_RESTRICTED = 9;
    public static final int SIM_STATE_NETWORK_LOCKED = 4;
    public static final int SIM_STATE_NOT_READY = 6;
    public static final int SIM_STATE_PERM_DISABLED = 7;
    public static final int SIM_STATE_PIN_REQUIRED = 2;
    public static final int SIM_STATE_PUK_REQUIRED = 3;
    public static final int SIM_STATE_READY = 5;
    public static final int SIM_STATE_UNKNOWN = 0;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final boolean SUPPORT_19BIT_ICCID = SystemProperties.getBoolean("ro.config.sup_19bit_iccid", false);
    private static final String TAG = "TelephonyManager";
    public static final int USSD_ERROR_SERVICE_UNAVAIL = -2;
    public static final String USSD_RESPONSE = "USSD_RESPONSE";
    public static final int USSD_RETURN_FAILURE = -1;
    public static final int USSD_RETURN_SUCCESS = 100;
    public static final String VVM_TYPE_CVVM = "vvm_type_cvvm";
    public static final String VVM_TYPE_OMTP = "vvm_type_omtp";
    private static String multiSimConfig = SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG);
    private static TelephonyManager sInstance = new TelephonyManager();
    private static final String sKernelCmdLine = getProcCmdLine();
    private static final String sLteOnCdmaProductType = SystemProperties.get(TelephonyProperties.PROPERTY_LTE_ON_CDMA_PRODUCT_TYPE, LogException.NO_VALUE);
    private static final Pattern sProductTypePattern = Pattern.compile("\\sproduct_type\\s*=\\s*(\\w+)");
    private static ITelephonyRegistry sRegistry;
    private final Context mContext;
    private final int mSubId;
    private SubscriptionManager mSubscriptionManager;

    public enum MultiSimVariants {
        DSDS,
        DSDA,
        TSTS,
        UNKNOWN
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

    private static /* synthetic */ int[] -getandroid-telephony-TelephonyManager$MultiSimVariantsSwitchesValues() {
        if (-android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues != null) {
            return -android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues;
        }
        int[] iArr = new int[MultiSimVariants.values().length];
        try {
            iArr[MultiSimVariants.DSDA.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[MultiSimVariants.DSDS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[MultiSimVariants.TSTS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[MultiSimVariants.UNKNOWN.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean z = false;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            z = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        }
        IS_CHINA_TELECOM = z;
    }

    public TelephonyManager(Context context) {
        this(context, Integer.MAX_VALUE);
    }

    public TelephonyManager(Context context, int subId) {
        this.mSubId = subId;
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            this.mContext = appContext;
        } else {
            this.mContext = context;
        }
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        if (sRegistry == null) {
            sRegistry = Stub.asInterface(ServiceManager.getService("telephony.registry"));
        }
    }

    private TelephonyManager() {
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

    public MultiSimVariants getMultiSimConfiguration() {
        String mSimConfig = SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG);
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
        switch (-getandroid-telephony-TelephonyManager$MultiSimVariantsSwitchesValues()[getMultiSimConfiguration().ordinal()]) {
            case 1:
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
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
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            return new TelephonyManager(this.mContext, subId);
        }
        return null;
    }

    public boolean isMultiSimEnabled() {
        if (multiSimConfig.equals("dsds") || multiSimConfig.equals("dsda")) {
            return true;
        }
        return multiSimConfig.equals("tsts");
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
        return getNai(getSlotIndex());
    }

    public String getNai(int slotIndex) {
        int[] subId = SubscriptionManager.getSubId(slotIndex);
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            String nai = info.getNaiForSubscriber(subId[0], this.mContext.getOpPackageName());
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
            if (bundle.isEmpty()) {
                Rlog.d(TAG, "getCellLocation returning null because bundle is empty");
                return null;
            }
            CellLocation cl = CellLocation.newFromBundle(bundle);
            if (!cl.isEmpty()) {
                return cl;
            }
            Rlog.d(TAG, "getCellLocation returning null because CellLocation is empty");
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    @Deprecated
    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        Log.d(TAG, "getNeighboringCellInfo calling app is " + HwFrameworkFactory.getHwInnerTelephonyManager().getCallingAppName(this.mContext));
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

    public int getCurrentPhoneType() {
        return getCurrentPhoneType(getSubId());
    }

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
        if (isVoiceCapable()) {
            return getCurrentPhoneType();
        }
        return 0;
    }

    private int getPhoneTypeFromProperty() {
        return getPhoneTypeFromProperty(getPhoneId());
    }

    private int getPhoneTypeFromProperty(int phoneId) {
        String type = getTelephonyProperty(phoneId, TelephonyProperties.CURRENT_ACTIVE_PHONE, null);
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
        if (mode == null || (mode.isEmpty() ^ 1) == 0) {
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
                return 1;
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
                return getLteOnCdmaModeStatic() == 1 ? 2 : 1;
            default:
                return 1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x005d A:{SYNTHETIC, Splitter: B:20:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0066 A:{SYNTHETIC, Splitter: B:25:0x0066} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getProcCmdLine() {
        IOException e;
        Throwable th;
        String cmdline = LogException.NO_VALUE;
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream("/proc/cmdline");
            try {
                byte[] buffer = new byte[2048];
                int count = is2.read(buffer);
                if (count > 0) {
                    cmdline = new String(buffer, 0, count);
                }
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException e2) {
                    }
                }
                is = is2;
            } catch (IOException e3) {
                e = e3;
                is = is2;
                try {
                    Rlog.d(TAG, "No /proc/cmdline exception=" + e);
                    if (is != null) {
                    }
                    Rlog.d(TAG, "/proc/cmdline=" + cmdline);
                    return cmdline;
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                if (is != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            Rlog.d(TAG, "No /proc/cmdline exception=" + e);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e6) {
                }
            }
            Rlog.d(TAG, "/proc/cmdline=" + cmdline);
            return cmdline;
        }
        Rlog.d(TAG, "/proc/cmdline=" + cmdline);
        return cmdline;
    }

    public static int getLteOnCdmaModeStatic() {
        String productType = LogException.NO_VALUE;
        int curVal = SystemProperties.getInt(TelephonyProperties.PROPERTY_LTE_ON_CDMA_DEVICE, -1);
        int retVal = curVal;
        if (curVal == -1) {
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
        return getTelephonyProperty(SubscriptionManager.getPhoneId(subId), TelephonyProperties.PROPERTY_OPERATOR_ALPHA, LogException.NO_VALUE);
    }

    public String getNetworkOperator() {
        return getNetworkOperatorForPhone(getPhoneId());
    }

    public String getNetworkOperator(int subId) {
        return getNetworkOperatorForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getNetworkOperatorForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, LogException.NO_VALUE);
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
        return Boolean.parseBoolean(getTelephonyProperty(SubscriptionManager.getPhoneId(subId), TelephonyProperties.PROPERTY_OPERATOR_ISROAMING, null));
    }

    public String getNetworkCountryIso() {
        return getNetworkCountryIsoForPhone(getPhoneId());
    }

    public String getNetworkCountryIso(int subId) {
        return getNetworkCountryIsoForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getNetworkCountryIsoForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, LogException.NO_VALUE);
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
            case 30:
                return 2;
            case 13:
            case 18:
            case 19:
                return 3;
            default:
                return 0;
        }
    }

    public String getNetworkTypeName() {
        return getNetworkTypeName(getNetworkType());
    }

    public static String getNetworkTypeName(int type) {
        if (IS_CHINA_TELECOM) {
            switch (type) {
                case 1:
                    return "GSM";
                case 2:
                    return "GSM";
                case 3:
                    return "WCDMA";
                case 4:
                    return "CDMA 1x";
                case 5:
                    return "CDMA EVDO";
                case 6:
                    return "CDMA EVDO";
                case 7:
                    return "CDMA 1x";
                case 8:
                    return "HSPA";
                case 9:
                    return "HSPA";
                case 10:
                    return "HSPA";
                case 11:
                    return "iDEN";
                case 12:
                    return "CDMA EVDO";
                case 13:
                    return "LTE";
                case 14:
                    return "eHRPD";
                case 15:
                    return "HSPA+";
                case 16:
                    return "GSM";
                default:
                    return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            }
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
            case 30:
                return "DC-HSPA+";
            default:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
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
        int slotIndex = getSlotIndex();
        if (slotIndex >= 0) {
            return getSimState(slotIndex);
        }
        for (int i = 0; i < getPhoneCount(); i++) {
            int simState = getSimState(i);
            if (simState != 1) {
                Rlog.d(TAG, "getSimState: default sim:" + slotIndex + ", sim state for " + "slotIndex=" + i + " is " + simState + ", return state as unknown");
                return 0;
            }
        }
        Rlog.d(TAG, "getSimState: default sim:" + slotIndex + ", all SIMs absent, return " + "state as absent");
        return 1;
    }

    public int getSimState(int slotIndex) {
        return SubscriptionManager.getSimStateForSlotIndex(slotIndex);
    }

    public String getSimOperator() {
        return getSimOperatorNumeric();
    }

    public String getSimOperator(int subId) {
        return getSimOperatorNumeric(subId);
    }

    public String getSimOperatorNumeric() {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (!SubscriptionManager.isUsableSubIdValue(subId)) {
            subId = SubscriptionManager.getDefaultSmsSubscriptionId();
            if (!SubscriptionManager.isUsableSubIdValue(subId)) {
                subId = SubscriptionManager.getDefaultVoiceSubscriptionId();
                if (!SubscriptionManager.isUsableSubIdValue(subId)) {
                    subId = SubscriptionManager.getDefaultSubscriptionId();
                }
            }
        }
        return getSimOperatorNumeric(subId);
    }

    public String getSimOperatorNumeric(int subId) {
        return getSimOperatorNumericForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getSimOperatorNumericForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, LogException.NO_VALUE);
    }

    public String getSimOperatorName() {
        return getSimOperatorNameForPhone(getPhoneId());
    }

    public String getSimOperatorName(int subId) {
        return getSimOperatorNameForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getSimOperatorNameForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, LogException.NO_VALUE);
    }

    public String getSimCountryIso() {
        return getSimCountryIsoForPhone(getPhoneId());
    }

    public String getSimCountryIso(int subId) {
        return getSimCountryIsoForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getSimCountryIsoForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY, LogException.NO_VALUE);
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
            if (SUPPORT_19BIT_ICCID && iccid != null && iccid.length() >= 19) {
                iccid = iccid.substring(0, 19);
            }
            return iccid;
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

    public String getGroupIdLevel1() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getGroupIdLevel1(this.mContext.getOpPackageName());
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        if (number != null) {
            return number;
        }
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getLine1NumberForSubscriber(subId, this.mContext.getOpPackageName());
        } catch (RemoteException e3) {
            return null;
        } catch (NullPointerException e4) {
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
        } catch (RemoteException e3) {
            return null;
        } catch (NullPointerException e4) {
            return null;
        }
    }

    public String[] getMergedSubscriberIds() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getMergedSubscriberIds(getOpPackageName());
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return false;
    }

    @SuppressLint({"Doclava125"})
    public void setVisualVoicemailEnabled(PhoneAccountHandle phoneAccountHandle, boolean enabled) {
    }

    @SuppressLint({"Doclava125"})
    public boolean isVisualVoicemailEnabled(PhoneAccountHandle phoneAccountHandle) {
        return false;
    }

    @SuppressLint({"Doclava125"})
    public Bundle getVisualVoicemailSettings() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVisualVoicemailSettings(this.mContext.getOpPackageName(), this.mSubId);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return null;
    }

    public String getVisualVoicemailPackageName() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVisualVoicemailPackageName(this.mContext.getOpPackageName(), getSubId());
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
        if (settings == null) {
            throw new IllegalArgumentException("Settings cannot be null");
        }
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.enableVisualVoicemailSmsFilter(this.mContext.getOpPackageName(), subId, settings);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    public void disableVisualVoicemailSmsFilter(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.disableVisualVoicemailSmsFilter(this.mContext.getOpPackageName(), subId);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    public VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVisualVoicemailSmsFilterSettings(this.mContext.getOpPackageName(), subId);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return null;
    }

    public VisualVoicemailSmsFilterSettings getActiveVisualVoicemailSmsFilterSettings(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getActiveVisualVoicemailSmsFilterSettings(subId);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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

    public void setVoiceActivationState(int subId, int activationState) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setVoiceActivationState(subId, activationState);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    public void setDataActivationState(int subId, int activationState) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setDataActivationState(subId, activationState);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    public int getVoiceActivationState(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getVoiceActivationState(subId, getOpPackageName());
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return 0;
    }

    public int getDataActivationState(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getDataActivationState(subId, getOpPackageName());
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
            getITelephony().sendDialerSpecialCode(this.mContext.getOpPackageName(), inputCode);
        } catch (RemoteException ex) {
            ex.rethrowFromSystemServer();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Telephony service unavailable");
        }
    }

    public String getIsimImpi() {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getIsimImpi();
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
            return info.getIsimDomain();
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
            return info.getIsimImpu();
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

    private static void setTelephonyRegistry() {
        sRegistry = Stub.asInterface(ServiceManager.getService("telephony.registry"));
    }

    public void listen(PhoneStateListener listener, int events) {
        if (this.mContext != null) {
            try {
                boolean notifyNow = getITelephony() != null;
                if (listener.mSubId == null) {
                    listener.mSubId = Integer.valueOf(this.mSubId);
                }
                if (sRegistry == null) {
                    Rlog.e(TAG, "sRegistry is null, get telephony.registry service again");
                    setTelephonyRegistry();
                }
                sRegistry.listenForSubscriber(listener.mSubId.intValue(), getOpPackageName(), listener.callback, events, notifyNow);
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
            return resources.getBoolean(R.bool.config_voice_capable);
        }
        Rlog.e(TAG, "isVoiceCapable:resources is null");
        return true;
    }

    public boolean isSmsCapable() {
        if (this.mContext == null) {
            return true;
        }
        return this.mContext.getResources().getBoolean(R.bool.config_sms_capable);
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
    }

    public String getMmsUserAgent() {
        if (this.mContext == null) {
            return null;
        }
        return this.mContext.getResources().getString(R.string.config_mms_user_agent);
    }

    public String getMmsUAProfUrl() {
        if (this.mContext == null) {
            return null;
        }
        return this.mContext.getResources().getString(R.string.config_mms_user_agent_profile_url);
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
                return telephony.iccOpenLogicalChannel(subId, AID, p2);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return null;
    }

    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(String AID, byte p2) {
        return iccOpenLogicalChannel(getSubId(), AID, p2);
    }

    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(int subId, String AID, byte p2) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.iccOpenLogicalChannelP2(subId, AID, p2);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return LogException.NO_VALUE;
    }

    public String iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data) {
        return iccTransmitApduBasicChannel(getSubId(), cla, instruction, p1, p2, p3, data);
    }

    public String iccTransmitApduBasicChannel(int subId, int cla, int instruction, int p1, int p2, int p3, String data) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.iccTransmitApduBasicChannel(subId, cla, instruction, p1, p2, p3, data);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return LogException.NO_VALUE;
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return LogException.NO_VALUE;
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
        return LogException.NO_VALUE;
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

    public int getSlotIndex() {
        int slotIndex = SubscriptionManager.getSlotIndex(getSubId());
        if (slotIndex == -1) {
            return Integer.MAX_VALUE;
        }
        return slotIndex;
    }

    public static void setTelephonyProperty(int phoneId, String property, String value) {
        String propVal = LogException.NO_VALUE;
        String[] p = null;
        String prop = SystemProperties.get(property);
        if (value == null) {
            value = LogException.NO_VALUE;
        }
        if (prop != null) {
            p = prop.split(",");
        }
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            int i = 0;
            while (i < phoneId) {
                String str = LogException.NO_VALUE;
                if (p != null && i < p.length) {
                    str = p[i];
                }
                propVal = propVal + str + ",";
                i++;
            }
            propVal = propVal + value;
            if (p != null) {
                for (i = phoneId + 1; i < p.length; i++) {
                    propVal = propVal + "," + p[i];
                }
            }
            if (propVal.length() > 91) {
                Rlog.d(TAG, "setTelephonyProperty: property too long phoneId=" + phoneId + " property=" + property + " value: " + value + " propVal=" + propVal);
                return;
            }
            Rlog.d(TAG, "setTelephonyProperty: success phoneId=" + phoneId + " property=" + property + " value: " + value + " propVal=" + propVal);
            SystemProperties.set(property, propVal);
            return;
        }
        Rlog.d(TAG, "setTelephonyProperty: invalid phoneId=" + phoneId + " property=" + property + " value: " + value + " prop=" + prop);
    }

    private static String updatePreferNetworkModeValArray(String v, String name) {
        String vNew = v;
        try {
            if (Global.PREFERRED_NETWORK_MODE.equals(name) && getDefault().isMultiSimEnabled()) {
                Rlog.d(TAG, "updatePreferNetworkModeValArray: v = " + v + ", name = " + name);
                if (v != null) {
                    String[] valArray = v.split(",");
                    if (1 == valArray.length) {
                        int subId = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
                        if (subId == 0) {
                            vNew = valArray[0] + ",1";
                        } else if (1 == subId) {
                            vNew = "1," + valArray[0];
                        }
                        Rlog.d(TAG, "updatePreferNetworkModeValArray: vNew = " + vNew + ", sub = " + subId);
                    }
                }
            }
        } catch (NullPointerException ex) {
            Rlog.d(TAG, "updatePreferNetworkModeValArray NPE: " + ex);
        } catch (Exception ex2) {
            Rlog.d(TAG, "updatePreferNetworkModeValArray Exception: " + ex2);
        }
        return vNew;
    }

    public static int getIntAtIndex(ContentResolver cr, String name, int index) throws SettingNotFoundException {
        String v = Global.getString(cr, name);
        if (NETWROK_TYPE_COMMA_SWITCH) {
            v = updatePreferNetworkModeValArray(v, name);
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
        throw new SettingNotFoundException(name);
    }

    public static boolean putIntAtIndex(ContentResolver cr, String name, int index, int value) {
        String data = LogException.NO_VALUE;
        String[] valArray = null;
        String v = Global.getString(cr, name);
        if (NETWROK_TYPE_COMMA_SWITCH) {
            v = updatePreferNetworkModeValArray(v, name);
        }
        if (index == Integer.MAX_VALUE) {
            throw new RuntimeException("putIntAtIndex index == MAX_VALUE index=" + index);
        } else if (index < 0) {
            throw new RuntimeException("putIntAtIndex index < 0 index=" + index);
        } else {
            if (v != null) {
                valArray = v.split(",");
            }
            int i = 0;
            while (i < index) {
                String str = LogException.NO_VALUE;
                if (valArray != null && i < valArray.length) {
                    str = valArray[i];
                }
                data = data + str + ",";
                i++;
            }
            data = data + value;
            if (valArray != null) {
                for (i = index + 1; i < valArray.length; i++) {
                    data = data + "," + valArray[i];
                }
            }
            return Global.putString(cr, name, data);
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
            return info.getIsimIst();
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
            return info.getIsimPcscf();
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getIsimChallengeResponse(String nonce) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getIsimChallengeResponse(nonce);
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
            return telephony.getForbiddenPlmns(subId, appType);
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

    public IImsServiceController getImsServiceControllerAndListen(int slotIndex, int feature, IImsServiceFeatureListener callback) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getImsServiceControllerAndListen(slotIndex, feature, callback);
            }
        } catch (RemoteException e) {
            Rlog.e(TAG, "getImsServiceControllerAndListen, RemoteException: " + e.getMessage());
        }
        return null;
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

    public boolean setNetworkSelectionModeManual(int subId, OperatorInfo operator, boolean persistSelection) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.setNetworkSelectionModeManual(subId, operator, persistSelection);
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
        boolean z = true;
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                if (telephony.getCarrierPrivilegeStatus(this.mSubId) != 1) {
                    z = false;
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

    public String getCdmaMdn() {
        return getCdmaMdn(getSubId());
    }

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

    public String getCdmaMin() {
        return getCdmaMin(getSubId());
    }

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

    public List<String> getCarrierPackageNamesForIntent(Intent intent) {
        return getCarrierPackageNamesForIntentAndPhone(intent, getPhoneId());
    }

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
    public void silenceRinger() {
        try {
            getTelecomService().silenceRinger(getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelecomService#silenceRinger", e);
        }
    }

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
            protected void onReceiveResult(int resultCode, Bundle ussdResponse) {
                Rlog.d(TelephonyManager.TAG, "USSD:" + resultCode);
                Preconditions.checkNotNull(ussdResponse, "ussdResponse cannot be null.");
                UssdResponse response = (UssdResponse) ussdResponse.getParcelable(TelephonyManager.USSD_RESPONSE);
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
            UssdResponse response = new UssdResponse(ussdRequest, LogException.NO_VALUE);
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

    public boolean isDataConnectivityPossible() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.isDataConnectivityPossible();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isDataConnectivityPossible", e);
        }
        return false;
    }

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
        setDataEnabled(SubscriptionManager.getDefaultDataSubscriptionId(), enable);
    }

    public void setDataEnabled(int subId, boolean enable) {
        try {
            Log.d(TAG, "setDataEnabled: enabled=" + enable);
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setDataEnabled(subId, enable);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#setDataEnabled", e);
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

    @Deprecated
    public boolean getDataEnabled() {
        return isDataEnabled();
    }

    public boolean isDataEnabled() {
        return getDataEnabled(getSubId(SubscriptionManager.getDefaultDataSubscriptionId()));
    }

    public boolean getDataEnabled(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getDataEnabled(subId);
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getDataEnabled", e);
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
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return -1;
    }

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

    public static int getIntWithSubId(ContentResolver cr, String name, int subId) throws SettingNotFoundException {
        int val;
        try {
            if (!name.equals("data_roaming")) {
                return Global.getInt(cr, name + subId);
            }
            val = Global.getInt(cr, name);
            if (subId == 1) {
                val = Global.getInt(cr, DATA_ROAMING_SIM2);
            }
            Global.putInt(cr, name + subId, val);
            return val;
        } catch (SettingNotFoundException e) {
            try {
                val = Global.getInt(cr, name);
                Global.putInt(cr, name + subId, val);
                int default_val = val;
                if (name.equals(Global.MOBILE_DATA)) {
                    default_val = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.mobiledata", "true")) ? 1 : 0;
                } else if (name.equals("data_roaming")) {
                    default_val = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false")) ? 1 : 0;
                }
                if (default_val != val) {
                    Global.putInt(cr, name, default_val);
                }
                return val;
            } catch (SettingNotFoundException e2) {
                throw new SettingNotFoundException(name);
            }
        }
    }

    public boolean isImsRegistered() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return false;
            }
            return telephony.isImsRegistered();
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isVolteAvailable() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return false;
            }
            return telephony.isVolteAvailable();
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isVideoTelephonyAvailable() {
        try {
            return getITelephony().isVideoTelephonyAvailable();
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean isWifiCallingAvailable() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return false;
            }
            return telephony.isWifiCallingAvailable();
        } catch (RemoteException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public void setSimOperatorNumeric(String numeric) {
        setSimOperatorNumericForPhone(getPhoneId(), numeric);
    }

    public void setSimOperatorNumericForPhone(int phoneId, String numeric) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, numeric);
    }

    public void setSimOperatorName(String name) {
        setSimOperatorNameForPhone(getPhoneId(), name);
    }

    public void setSimOperatorNameForPhone(int phoneId, String name) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, name);
    }

    public void setSimCountryIso(String iso) {
        setSimCountryIsoForPhone(getPhoneId(), iso);
    }

    public void setSimCountryIsoForPhone(int phoneId, String iso) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY, iso);
    }

    public void setSimState(String state) {
        setSimStateForPhone(getPhoneId(), state);
    }

    public void setSimStateForPhone(int phoneId, String state) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_SIM_STATE, state);
    }

    public void setSimPowerState(boolean powerUp) {
        setSimPowerStateForSlot(getSlotIndex(), powerUp);
    }

    public void setSimPowerStateForSlot(int slotIndex, boolean powerUp) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                telephony.setSimPowerStateForSlot(slotIndex, powerUp);
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
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_BASEBAND_VERSION, version);
    }

    public void setPhoneType(int type) {
        setPhoneType(getPhoneId(), type);
    }

    public void setPhoneType(int phoneId, int type) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.CURRENT_ACTIVE_PHONE, String.valueOf(type));
        }
    }

    public String getOtaSpNumberSchema(String defaultValue) {
        return getOtaSpNumberSchemaForPhone(getPhoneId(), defaultValue);
    }

    public String getOtaSpNumberSchemaForPhone(int phoneId, String defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OTASP_NUM_SCHEMA, defaultValue);
        }
        return defaultValue;
    }

    public boolean getSmsReceiveCapable(boolean defaultValue) {
        return getSmsReceiveCapableForPhone(getPhoneId(), defaultValue);
    }

    public boolean getSmsReceiveCapableForPhone(int phoneId, boolean defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return Boolean.parseBoolean(getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_SMS_RECEIVE, String.valueOf(defaultValue)));
        }
        return defaultValue;
    }

    public boolean getSmsSendCapable(boolean defaultValue) {
        return getSmsSendCapableForPhone(getPhoneId(), defaultValue);
    }

    public boolean getSmsSendCapableForPhone(int phoneId, boolean defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return Boolean.parseBoolean(getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_SMS_SEND, String.valueOf(defaultValue)));
        }
        return defaultValue;
    }

    public void setNetworkOperatorName(String name) {
        setNetworkOperatorNameForPhone(getPhoneId(), name);
    }

    public void setNetworkOperatorNameForPhone(int phoneId, String name) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ALPHA, name);
        }
    }

    public void setNetworkOperatorNumeric(String numeric) {
        setNetworkOperatorNumericForPhone(getPhoneId(), numeric);
    }

    public void setNetworkOperatorNumericForPhone(int phoneId, String numeric) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, numeric);
    }

    public void setNetworkRoaming(boolean isRoaming) {
        setNetworkRoamingForPhone(getPhoneId(), isRoaming);
    }

    public void setNetworkRoamingForPhone(int phoneId, boolean isRoaming) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ISROAMING, isRoaming ? "true" : "false");
        }
    }

    public void setNetworkCountryIso(String iso) {
        setNetworkCountryIsoForPhone(getPhoneId(), iso);
    }

    public void setNetworkCountryIsoForPhone(int phoneId, String iso) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, iso);
        }
    }

    public void setDataNetworkType(int type) {
        setDataNetworkTypeForPhone(getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId()), type);
    }

    public void setDataNetworkTypeForPhone(int phoneId, int type) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE, ServiceState.rilRadioTechnologyToString(type));
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

    public List<CarrierIdentifier> getAllowedCarriers(int slotIndex) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getAllowedCarriers(slotIndex);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getAllowedCarriers", e);
        } catch (NullPointerException e2) {
            Log.e(TAG, "Error calling ITelephony#setAllowedCarriers", e2);
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

    public long getVtDataUsage() {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.getVtDataUsage();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling getVtDataUsage", e);
        }
        return 0;
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

    public boolean setDualCardMode(int nMode) {
        return HwFrameworkFactory.getHwInnerTelephonyManager().setDualCardMode(nMode);
    }

    public int getDualCardMode() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().getDualCardMode();
    }

    public String getPesn() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().getPesn();
    }
}
