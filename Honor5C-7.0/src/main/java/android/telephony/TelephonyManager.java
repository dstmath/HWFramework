package android.telephony;

import android.app.ActivityThread;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.util.PtmLog;
import com.android.internal.R;
import com.android.internal.os.HwBootFail;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.CellNetworkScanResult;
import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.ITelephonyRegistry.Stub;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.hisi.perfhub.PerfHub;
import com.huawei.hwperformance.HwPerformance;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.microedition.khronos.opengles.GL10;

public class TelephonyManager {
    private static final /* synthetic */ int[] -android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues = null;
    public static final String ACTION_CONFIGURE_VOICEMAIL = "android.telephony.action.CONFIGURE_VOICEMAIL";
    public static final String ACTION_EMERGENCY_ASSISTANCE = "android.telephony.action.EMERGENCY_ASSISTANCE";
    public static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
    public static final String ACTION_PRECISE_CALL_STATE_CHANGED = "android.intent.action.PRECISE_CALL_STATE";
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
    public static final String EXTRA_INCOMING_NUMBER = "incoming_number";
    public static final String EXTRA_LAUNCH_VOICEMAIL_SETTINGS_INTENT = "android.telephony.extra.LAUNCH_VOICEMAIL_SETTINGS_INTENT";
    public static final String EXTRA_NOTIFICATION_COUNT = "android.telephony.extra.NOTIFICATION_COUNT";
    public static final String EXTRA_PRECISE_DISCONNECT_CAUSE = "precise_disconnect_cause";
    public static final String EXTRA_RINGING_CALL_STATE = "ringing_state";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_STATE_IDLE = null;
    public static final String EXTRA_STATE_OFFHOOK = null;
    public static final String EXTRA_STATE_RINGING = null;
    public static final String EXTRA_VOICEMAIL_NUMBER = "android.telephony.extra.VOICEMAIL_NUMBER";
    public static final boolean IS_CHINA_TELECOM = false;
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
    public static final int NETWORK_TYPE_LTE_CA = 31;
    public static final int NETWORK_TYPE_TD_SCDMA = 17;
    public static final int NETWORK_TYPE_UMTS = 3;
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    public static final boolean NETWROK_TYPE_COMMA_SWITCH = false;
    public static final int PHONE_TYPE_CDMA = 2;
    public static final int PHONE_TYPE_GSM = 1;
    public static final int PHONE_TYPE_NONE = 0;
    public static final int PHONE_TYPE_SIP = 3;
    public static final int PREFERRED_NETWORK_MODE_CFG_NUM = 1;
    public static final int SCOPE_ALL = 0;
    public static final int SCOPE_IMEI = 1;
    public static final int SIM_ACTIVATION_RESULT_CANCELED = 4;
    public static final int SIM_ACTIVATION_RESULT_COMPLETE = 0;
    public static final int SIM_ACTIVATION_RESULT_FAILED = 3;
    public static final int SIM_ACTIVATION_RESULT_IN_PROGRESS = 2;
    public static final int SIM_ACTIVATION_RESULT_NOT_SUPPORTED = 1;
    public static final int SIM_STATE_ABSENT = 1;
    public static final int SIM_STATE_CARD_IO_ERROR = 8;
    public static final int SIM_STATE_NETWORK_LOCKED = 4;
    public static final int SIM_STATE_NOT_READY = 6;
    public static final int SIM_STATE_PERM_DISABLED = 7;
    public static final int SIM_STATE_PIN_REQUIRED = 2;
    public static final int SIM_STATE_PUK_REQUIRED = 3;
    public static final int SIM_STATE_READY = 5;
    public static final int SIM_STATE_UNKNOWN = 0;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    private static final String TAG = "TelephonyManager";
    public static final String VVM_TYPE_CVVM = "vvm_type_cvvm";
    public static final String VVM_TYPE_OMTP = "vvm_type_omtp";
    private static String multiSimConfig;
    private static TelephonyManager sInstance;
    private static final String sKernelCmdLine = null;
    private static final String sLteOnCdmaProductType = null;
    private static final Pattern sProductTypePattern = null;
    private static ITelephonyRegistry sRegistry;
    private final Context mContext;
    private final int mSubId;
    private SubscriptionManager mSubscriptionManager;

    public enum MultiSimVariants {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.TelephonyManager.MultiSimVariants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.TelephonyManager.MultiSimVariants.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.telephony.TelephonyManager.MultiSimVariants.<clinit>():void");
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
            iArr[MultiSimVariants.DSDA.ordinal()] = SUB2;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[MultiSimVariants.DSDS.ordinal()] = SIM_STATE_PIN_REQUIRED;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[MultiSimVariants.TSTS.ordinal()] = SIM_STATE_PUK_REQUIRED;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[MultiSimVariants.UNKNOWN.ordinal()] = SIM_STATE_NETWORK_LOCKED;
        } catch (NoSuchFieldError e4) {
        }
        -android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.TelephonyManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.TelephonyManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.TelephonyManager.<clinit>():void");
    }

    public TelephonyManager(Context context) {
        this(context, HwBootFail.STAGE_BOOT_SUCCESS);
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
        this.mSubId = HwBootFail.STAGE_BOOT_SUCCESS;
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
            case SUB2 /*1*/:
            case SIM_STATE_PIN_REQUIRED /*2*/:
                return SIM_STATE_PIN_REQUIRED;
            case SIM_STATE_PUK_REQUIRED /*3*/:
                return SIM_STATE_PUK_REQUIRED;
            case SIM_STATE_NETWORK_LOCKED /*4*/:
                if (isVoiceCapable() || isSmsCapable()) {
                    return SUB2;
                }
                if (this.mContext == null) {
                    return SUB2;
                }
                ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                if (cm == null) {
                    return SUB2;
                }
                if (cm.isNetworkSupported(SUB1)) {
                    return SUB2;
                }
                return SUB1;
            default:
                return SUB2;
        }
    }

    public static TelephonyManager from(Context context) {
        return (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY);
    }

    public TelephonyManager createForSubscriptionId(int subId) {
        return new TelephonyManager(this.mContext, subId);
    }

    public boolean isMultiSimEnabled() {
        if (multiSimConfig.equals("dsds") || multiSimConfig.equals("dsda")) {
            return EMERGENCY_ASSISTANCE_ENABLED;
        }
        return multiSimConfig.equals("tsts");
    }

    public String getDeviceSoftwareVersion() {
        return getDeviceSoftwareVersion(getDefaultSim());
    }

    public String getDeviceSoftwareVersion(int slotId) {
        ITelephony telephony = getITelephony();
        if (telephony == null) {
            return null;
        }
        try {
            return telephony.getDeviceSoftwareVersionForSlot(slotId, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getDeviceId() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().getUniqueDeviceId(SUB1);
    }

    public String getDeviceId(int slotId) {
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            return info.getDeviceIdForPhone(slotId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getImei() {
        return HwFrameworkFactory.getHwInnerTelephonyManager().getUniqueDeviceId(SUB2);
    }

    public String getImei(int slotId) {
        ITelephony telephony = getITelephony();
        if (telephony == null) {
            return null;
        }
        try {
            return telephony.getImeiForSlot(slotId, getOpPackageName());
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public String getNai() {
        return getNai(getDefaultSim());
    }

    public String getNai(int slotId) {
        int[] subId = SubscriptionManager.getSubId(slotId);
        try {
            IPhoneSubInfo info = getSubscriberInfo();
            if (info == null) {
                return null;
            }
            String nai = info.getNaiForSubscriber(subId[SUB1], this.mContext.getOpPackageName());
            if (Log.isLoggable(TAG, SIM_STATE_PIN_REQUIRED)) {
                Rlog.v(TAG, "Nai = " + nai);
            }
            return nai;
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    public CellLocation getCellLocation() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                Rlog.d(TAG, "getCellLocation returning null because telephony is null");
                return null;
            }
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
        if (subId == DATA_UNKNOWN) {
            phoneId = SUB1;
        } else {
            phoneId = SubscriptionManager.getPhoneId(subId);
        }
        return getCurrentPhoneTypeForSlot(phoneId);
    }

    public int getCurrentPhoneTypeForSlot(int slotId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getActivePhoneTypeForSlot(slotId);
            }
            return getPhoneTypeFromProperty(slotId);
        } catch (RemoteException e) {
            return getPhoneTypeFromProperty(slotId);
        } catch (NullPointerException e2) {
            return getPhoneTypeFromProperty(slotId);
        }
    }

    public int getPhoneType() {
        if (isVoiceCapable()) {
            return getCurrentPhoneType();
        }
        return SUB1;
    }

    private int getPhoneTypeFromProperty() {
        return getPhoneTypeFromProperty(getDefaultPhone());
    }

    private int getPhoneTypeFromProperty(int phoneId) {
        String type = getTelephonyProperty(phoneId, TelephonyProperties.CURRENT_ACTIVE_PHONE, null);
        if (type == null || type.equals("")) {
            return getPhoneTypeFromNetworkType(phoneId);
        }
        return Integer.parseInt(type);
    }

    private int getPhoneTypeFromNetworkType() {
        return getPhoneTypeFromNetworkType(getDefaultPhone());
    }

    private int getPhoneTypeFromNetworkType(int phoneId) {
        String mode = getTelephonyProperty(phoneId, "ro.telephony.default_network", null);
        if (mode != null) {
            return getPhoneType(Integer.parseInt(mode));
        }
        return SUB1;
    }

    public static int getPhoneType(int networkMode) {
        switch (networkMode) {
            case SUB1 /*0*/:
            case SUB2 /*1*/:
            case SIM_STATE_PIN_REQUIRED /*2*/:
            case SIM_STATE_PUK_REQUIRED /*3*/:
            case NETWORK_TYPE_HSUPA /*9*/:
            case NETWORK_TYPE_EVDO_B /*12*/:
            case NETWORK_TYPE_LTE /*13*/:
            case NETWORK_TYPE_EHRPD /*14*/:
            case NETWORK_TYPE_HSPAP /*15*/:
            case NETWORK_TYPE_GSM /*16*/:
            case NETWORK_TYPE_TD_SCDMA /*17*/:
            case NETWORK_TYPE_IWLAN /*18*/:
            case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
            case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                return SUB2;
            case SIM_STATE_NETWORK_LOCKED /*4*/:
            case SIM_STATE_READY /*5*/:
            case SIM_STATE_NOT_READY /*6*/:
                return SIM_STATE_PIN_REQUIRED;
            case SIM_STATE_PERM_DISABLED /*7*/:
            case SIM_STATE_CARD_IO_ERROR /*8*/:
            case NETWORK_TYPE_HSPA /*10*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                return SIM_STATE_PIN_REQUIRED;
            case NETWORK_TYPE_IDEN /*11*/:
                return getLteOnCdmaModeStatic() == SUB2 ? SIM_STATE_PIN_REQUIRED : SUB2;
            default:
                return SUB2;
        }
    }

    private static String getProcCmdLine() {
        IOException e;
        Throwable th;
        String cmdline = "";
        FileInputStream fileInputStream = null;
        try {
            FileInputStream is = new FileInputStream("/proc/cmdline");
            try {
                byte[] buffer = new byte[GL10.GL_EXP];
                int count = is.read(buffer);
                if (count > 0) {
                    cmdline = new String(buffer, SUB1, count);
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                    }
                }
                fileInputStream = is;
            } catch (IOException e3) {
                e = e3;
                fileInputStream = is;
                try {
                    Rlog.d(TAG, "No /proc/cmdline exception=" + e);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e4) {
                        }
                    }
                    Rlog.d(TAG, "/proc/cmdline=" + cmdline);
                    return cmdline;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = is;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            Rlog.d(TAG, "No /proc/cmdline exception=" + e);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            Rlog.d(TAG, "/proc/cmdline=" + cmdline);
            return cmdline;
        }
        Rlog.d(TAG, "/proc/cmdline=" + cmdline);
        return cmdline;
    }

    public static int getLteOnCdmaModeStatic() {
        String productType = "";
        int curVal = SystemProperties.getInt(TelephonyProperties.PROPERTY_LTE_ON_CDMA_DEVICE, DATA_UNKNOWN);
        int retVal = curVal;
        if (curVal == DATA_UNKNOWN) {
            Matcher matcher = sProductTypePattern.matcher(sKernelCmdLine);
            if (matcher.find()) {
                productType = matcher.group(SUB2);
                if (sLteOnCdmaProductType.equals(productType)) {
                    retVal = SUB2;
                } else {
                    retVal = SUB1;
                }
            } else {
                retVal = SUB1;
            }
        }
        Rlog.d(TAG, "getLteOnCdmaMode=" + retVal + " curVal=" + curVal + " product_type='" + productType + "' lteOnCdmaProductType='" + sLteOnCdmaProductType + "'");
        return retVal;
    }

    public String getNetworkOperatorName() {
        return getNetworkOperatorName(getSubId());
    }

    public String getNetworkOperatorName(int subId) {
        return getTelephonyProperty(SubscriptionManager.getPhoneId(subId), TelephonyProperties.PROPERTY_OPERATOR_ALPHA, "");
    }

    public String getNetworkOperator() {
        return getNetworkOperatorForPhone(getDefaultPhone());
    }

    public String getNetworkOperator(int subId) {
        return getNetworkOperatorForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getNetworkOperatorForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, "");
    }

    public boolean isNetworkRoaming() {
        return isNetworkRoaming(getSubId());
    }

    public boolean isNetworkRoaming(int subId) {
        return Boolean.parseBoolean(getTelephonyProperty(SubscriptionManager.getPhoneId(subId), TelephonyProperties.PROPERTY_OPERATOR_ISROAMING, null));
    }

    public String getNetworkCountryIso() {
        return getNetworkCountryIsoForPhone(getDefaultPhone());
    }

    public String getNetworkCountryIso(int subId) {
        return getNetworkCountryIsoForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getNetworkCountryIsoForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, "");
    }

    public int getNetworkType() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getNetworkType();
            }
            return SUB1;
        } catch (RemoteException e) {
            return SUB1;
        } catch (NullPointerException e2) {
            return SUB1;
        }
    }

    public int getNetworkType(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getNetworkTypeForSubscriber(subId, getOpPackageName());
            }
            return SUB1;
        } catch (RemoteException e) {
            return SUB1;
        } catch (NullPointerException e2) {
            return SUB1;
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
            return SUB1;
        } catch (RemoteException e) {
            return SUB1;
        } catch (NullPointerException e2) {
            return SUB1;
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
            return SUB1;
        } catch (RemoteException e) {
            return SUB1;
        } catch (NullPointerException e2) {
            return SUB1;
        }
    }

    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case SUB2 /*1*/:
            case SIM_STATE_PIN_REQUIRED /*2*/:
            case SIM_STATE_NETWORK_LOCKED /*4*/:
            case SIM_STATE_PERM_DISABLED /*7*/:
            case NETWORK_TYPE_IDEN /*11*/:
            case NETWORK_TYPE_GSM /*16*/:
                return SUB2;
            case SIM_STATE_PUK_REQUIRED /*3*/:
            case SIM_STATE_READY /*5*/:
            case SIM_STATE_NOT_READY /*6*/:
            case SIM_STATE_CARD_IO_ERROR /*8*/:
            case NETWORK_TYPE_HSUPA /*9*/:
            case NETWORK_TYPE_HSPA /*10*/:
            case NETWORK_TYPE_EVDO_B /*12*/:
            case NETWORK_TYPE_EHRPD /*14*/:
            case NETWORK_TYPE_HSPAP /*15*/:
            case NETWORK_TYPE_TD_SCDMA /*17*/:
            case NETWORK_TYPE_DCHSPAP /*30*/:
                return SIM_STATE_PIN_REQUIRED;
            case NETWORK_TYPE_LTE /*13*/:
            case NETWORK_TYPE_IWLAN /*18*/:
            case NETWORK_TYPE_LTE_CA /*31*/:
                return SIM_STATE_PUK_REQUIRED;
            default:
                return SUB1;
        }
    }

    public String getNetworkTypeName() {
        return getNetworkTypeName(getNetworkType());
    }

    public static String getNetworkTypeName(int type) {
        if (IS_CHINA_TELECOM) {
            switch (type) {
                case SUB2 /*1*/:
                    return "GSM";
                case SIM_STATE_PIN_REQUIRED /*2*/:
                    return "GSM";
                case SIM_STATE_PUK_REQUIRED /*3*/:
                    return "WCDMA";
                case SIM_STATE_NETWORK_LOCKED /*4*/:
                    return "CDMA 1x";
                case SIM_STATE_READY /*5*/:
                    return "CDMA EVDO";
                case SIM_STATE_NOT_READY /*6*/:
                    return "CDMA EVDO";
                case SIM_STATE_PERM_DISABLED /*7*/:
                    return "CDMA 1x";
                case SIM_STATE_CARD_IO_ERROR /*8*/:
                    return "HSPA";
                case NETWORK_TYPE_HSUPA /*9*/:
                    return "HSPA";
                case NETWORK_TYPE_HSPA /*10*/:
                    return "HSPA";
                case NETWORK_TYPE_IDEN /*11*/:
                    return "iDEN";
                case NETWORK_TYPE_EVDO_B /*12*/:
                    return "CDMA EVDO";
                case NETWORK_TYPE_LTE /*13*/:
                    return "LTE";
                case NETWORK_TYPE_EHRPD /*14*/:
                    return "eHRPD";
                case NETWORK_TYPE_HSPAP /*15*/:
                    return "HSPA+";
                case NETWORK_TYPE_GSM /*16*/:
                    return "GSM";
                default:
                    return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            }
        }
        switch (type) {
            case SUB2 /*1*/:
                return "GPRS";
            case SIM_STATE_PIN_REQUIRED /*2*/:
                return "EDGE";
            case SIM_STATE_PUK_REQUIRED /*3*/:
                return "UMTS";
            case SIM_STATE_NETWORK_LOCKED /*4*/:
                return "CDMA";
            case SIM_STATE_READY /*5*/:
                return "CDMA - EvDo rev. 0";
            case SIM_STATE_NOT_READY /*6*/:
                return "CDMA - EvDo rev. A";
            case SIM_STATE_PERM_DISABLED /*7*/:
                return "CDMA - 1xRTT";
            case SIM_STATE_CARD_IO_ERROR /*8*/:
                return "HSDPA";
            case NETWORK_TYPE_HSUPA /*9*/:
                return "HSUPA";
            case NETWORK_TYPE_HSPA /*10*/:
                return "HSPA";
            case NETWORK_TYPE_IDEN /*11*/:
                return "iDEN";
            case NETWORK_TYPE_EVDO_B /*12*/:
                return "CDMA - EvDo rev. B";
            case NETWORK_TYPE_LTE /*13*/:
                return "LTE";
            case NETWORK_TYPE_EHRPD /*14*/:
                return "CDMA - eHRPD";
            case NETWORK_TYPE_HSPAP /*15*/:
                return "HSPA+";
            case NETWORK_TYPE_GSM /*16*/:
                return "GSM";
            case NETWORK_TYPE_TD_SCDMA /*17*/:
                return "TD-SCDMA";
            case NETWORK_TYPE_IWLAN /*18*/:
                return "IWLAN";
            case NETWORK_TYPE_DCHSPAP /*30*/:
                return "DC-HSPA+";
            case NETWORK_TYPE_LTE_CA /*31*/:
                return "LTE-CA";
            default:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
    }

    public boolean hasIccCard() {
        return hasIccCard(getDefaultSim());
    }

    public boolean hasIccCard(int slotId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return NETWROK_TYPE_COMMA_SWITCH;
            }
            return telephony.hasIccCardUsingSlotId(slotId);
        } catch (RemoteException e) {
            return NETWROK_TYPE_COMMA_SWITCH;
        } catch (NullPointerException e2) {
            return NETWROK_TYPE_COMMA_SWITCH;
        }
    }

    public int getSimState() {
        int slotIdx = getDefaultSim();
        if (slotIdx >= 0) {
            return getSimState(slotIdx);
        }
        for (int i = SUB1; i < getPhoneCount(); i += SUB2) {
            int simState = getSimState(i);
            if (simState != SUB2) {
                Rlog.d(TAG, "getSimState: default sim:" + slotIdx + ", sim state for " + "slotIdx=" + i + " is " + simState + ", return state as unknown");
                return SUB1;
            }
        }
        Rlog.d(TAG, "getSimState: default sim:" + slotIdx + ", all SIMs absent, return " + "state as absent");
        return SUB2;
    }

    public int getSimState(int slotIdx) {
        return SubscriptionManager.getSimStateForSlotIdx(slotIdx);
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
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, "");
    }

    public String getSimOperatorName() {
        return getSimOperatorNameForPhone(getDefaultPhone());
    }

    public String getSimOperatorName(int subId) {
        return getSimOperatorNameForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getSimOperatorNameForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, "");
    }

    public String getSimCountryIso() {
        return getSimCountryIsoForPhone(getDefaultPhone());
    }

    public String getSimCountryIso(int subId) {
        return getSimCountryIsoForPhone(SubscriptionManager.getPhoneId(subId));
    }

    public String getSimCountryIsoForPhone(int phoneId) {
        return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY, "");
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
            return info.getIccSerialNumberForSubscriber(subId, this.mContext.getOpPackageName());
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
                return DATA_UNKNOWN;
            }
            return telephony.getLteOnCdmaModeForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return DATA_UNKNOWN;
        } catch (NullPointerException e2) {
            return DATA_UNKNOWN;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
    }

    public int getVoiceMessageCount() {
        return getVoiceMessageCount(getSubId());
    }

    public int getVoiceMessageCount(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return SUB1;
            }
            return telephony.getVoiceMessageCountForSubscriber(subId);
        } catch (RemoteException e) {
            return SUB1;
        } catch (NullPointerException e2) {
            return SUB1;
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
        return SUB1;
    }

    public int getCallState(int subId) {
        return getCallStateForSlot(SubscriptionManager.getPhoneId(subId));
    }

    public int getCallStateForSlot(int slotId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return SUB1;
            }
            return telephony.getCallStateForSlot(slotId);
        } catch (RemoteException e) {
            return SUB1;
        } catch (NullPointerException e2) {
            return SUB1;
        }
    }

    public int getDataActivity() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return SUB1;
            }
            return telephony.getDataActivity();
        } catch (RemoteException e) {
            return SUB1;
        } catch (NullPointerException e2) {
            return SUB1;
        }
    }

    public int getDataState() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return SUB1;
            }
            return telephony.getDataState();
        } catch (RemoteException e) {
            return SUB1;
        } catch (NullPointerException e2) {
            return SUB1;
        }
    }

    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService(PhoneConstants.PHONE_KEY));
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
                Boolean notifyNow = Boolean.valueOf(getITelephony() != null ? EMERGENCY_ASSISTANCE_ENABLED : NETWROK_TYPE_COMMA_SWITCH);
                if (sRegistry == null) {
                    Rlog.e(TAG, "sRegistry is null, get telephony.registry service again");
                    setTelephonyRegistry();
                }
                sRegistry.listenForSubscriber(listener.mSubId, getOpPackageName(), listener.callback, events, notifyNow.booleanValue());
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
                return DATA_UNKNOWN;
            }
            return telephony.getCdmaEriIconIndexForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return DATA_UNKNOWN;
        } catch (NullPointerException e2) {
            return DATA_UNKNOWN;
        }
    }

    public int getCdmaEriIconMode() {
        return getCdmaEriIconMode(getSubId());
    }

    public int getCdmaEriIconMode(int subId) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return DATA_UNKNOWN;
            }
            return telephony.getCdmaEriIconModeForSubscriber(subId, getOpPackageName());
        } catch (RemoteException e) {
            return DATA_UNKNOWN;
        } catch (NullPointerException e2) {
            return DATA_UNKNOWN;
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
            return EMERGENCY_ASSISTANCE_ENABLED;
        }
        Resources resources = this.mContext.getResources();
        if (resources != null) {
            return resources.getBoolean(R.bool.config_voice_capable);
        }
        Rlog.e(TAG, "isVoiceCapable:resources is null");
        return EMERGENCY_ASSISTANCE_ENABLED;
    }

    public boolean isSmsCapable() {
        if (this.mContext == null) {
            return EMERGENCY_ASSISTANCE_ENABLED;
        }
        return this.mContext.getResources().getBoolean(R.bool.config_sms_capable);
    }

    public List<CellInfo> getAllCellInfo() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return null;
            }
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

    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(String AID) {
        return iccOpenLogicalChannel(getSubId(), AID);
    }

    public IccOpenLogicalChannelResponse iccOpenLogicalChannel(int subId, String AID) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.iccOpenLogicalChannel(subId, AID);
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return "";
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
    }

    private int getSubId() {
        if (this.mSubId == HwBootFail.STAGE_BOOT_SUCCESS) {
            return getDefaultSubscription();
        }
        return this.mSubId;
    }

    private static int getDefaultSubscription() {
        return SubscriptionManager.getDefaultSubscriptionId();
    }

    private static int getDefaultPhone() {
        return SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId());
    }

    public int getDefaultSim() {
        return SubscriptionManager.getSlotId(SubscriptionManager.getDefaultSubscriptionId());
    }

    public static void setTelephonyProperty(int phoneId, String property, String value) {
        String propVal = "";
        String[] p = null;
        String prop = SystemProperties.get(property);
        if (value == null) {
            value = "";
        }
        if (prop != null) {
            p = prop.split(PtmLog.PAIRE_DELIMETER);
        }
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            int i = SUB1;
            while (i < phoneId) {
                String str = "";
                if (p != null && i < p.length) {
                    str = p[i];
                }
                propVal = propVal + str + PtmLog.PAIRE_DELIMETER;
                i += SUB2;
            }
            propVal = propVal + value;
            if (p != null) {
                for (i = phoneId + SUB2; i < p.length; i += SUB2) {
                    propVal = propVal + PtmLog.PAIRE_DELIMETER + p[i];
                }
            }
            if (property.length() > NETWORK_TYPE_LTE_CA || propVal.length() > 91) {
                Rlog.d(TAG, "setTelephonyProperty: property to long phoneId=" + phoneId + " property=" + property + " value: " + value + " propVal=" + propVal);
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
            if ("preferred_network_mode".equals(name) && getDefault().isMultiSimEnabled()) {
                Rlog.d(TAG, "updatePreferNetworkModeValArray: v = " + v + ", name = " + name);
                if (v != null) {
                    String[] valArray = v.split(PtmLog.PAIRE_DELIMETER);
                    if (SUB2 == valArray.length) {
                        int subId = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
                        if (subId == 0) {
                            vNew = valArray[SUB1] + ",1";
                        } else if (SUB2 == subId) {
                            vNew = "1," + valArray[SUB1];
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
            String[] valArray = v.split(PtmLog.PAIRE_DELIMETER);
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
        String data = "";
        String[] valArray = null;
        String v = Global.getString(cr, name);
        if (NETWROK_TYPE_COMMA_SWITCH) {
            v = updatePreferNetworkModeValArray(v, name);
        }
        if (index == HwBootFail.STAGE_BOOT_SUCCESS) {
            throw new RuntimeException("putIntAtIndex index == MAX_VALUE index=" + index);
        } else if (index < 0) {
            throw new RuntimeException("putIntAtIndex index < 0 index=" + index);
        } else {
            if (v != null) {
                valArray = v.split(PtmLog.PAIRE_DELIMETER);
            }
            int i = SUB1;
            while (i < index) {
                String str = "";
                if (valArray != null && i < valArray.length) {
                    str = valArray[i];
                }
                data = data + str + PtmLog.PAIRE_DELIMETER;
                i += SUB2;
            }
            data = data + value;
            if (valArray != null) {
                for (i = index + SUB2; i < valArray.length; i += SUB2) {
                    data = data + PtmLog.PAIRE_DELIMETER + valArray[i];
                }
            }
            return Global.putString(cr, name, data);
        }
    }

    public static String getTelephonyProperty(int phoneId, String property, String defaultVal) {
        String propVal = null;
        String prop = SystemProperties.get(property);
        if (prop != null && prop.length() > 0) {
            String[] values = prop.split(PtmLog.PAIRE_DELIMETER);
            if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                propVal = values[phoneId];
            }
        }
        return propVal == null ? defaultVal : propVal;
    }

    public int getSimCount() {
        if (isMultiSimEnabled()) {
            return SIM_STATE_PIN_REQUIRED;
        }
        return SUB2;
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

    public String[] getPcscfAddress(String apnType) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return new String[SUB1];
            }
            return telephony.getPcscfAddress(apnType, getOpPackageName());
        } catch (RemoteException e) {
            return new String[SUB1];
        }
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
        return DATA_UNKNOWN;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
    }

    public boolean setPreferredNetworkTypeToGlobal() {
        return setPreferredNetworkTypeToGlobal(getSubId());
    }

    public boolean setPreferredNetworkTypeToGlobal(int subId) {
        return setPreferredNetworkType(subId, NETWORK_TYPE_HSPA);
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
        return SIM_STATE_PIN_REQUIRED;
    }

    public boolean hasCarrierPrivileges() {
        return hasCarrierPrivileges(getSubId());
    }

    public boolean hasCarrierPrivileges(int subId) {
        boolean z = EMERGENCY_ASSISTANCE_ENABLED;
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                if (telephony.getCarrierPrivilegeStatus(this.mSubId) != SUB2) {
                    z = NETWROK_TYPE_COMMA_SWITCH;
                }
                return z;
            }
        } catch (RemoteException ex) {
            Rlog.e(TAG, "hasCarrierPrivileges RemoteException", ex);
        } catch (NullPointerException ex2) {
            Rlog.e(TAG, "hasCarrierPrivileges NPE", ex2);
        }
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return SUB1;
    }

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
        return SUB1;
    }

    public List<String> getCarrierPackageNamesForIntent(Intent intent) {
        return getCarrierPackageNamesForIntentAndPhone(intent, getDefaultPhone());
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return EMERGENCY_ASSISTANCE_ENABLED;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return new int[SUB1];
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
        return new int[SUB1];
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
    }

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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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

    public boolean getDataEnabled() {
        return getDataEnabled(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    public boolean getDataEnabled(int subId) {
        boolean retVal = NETWROK_TYPE_COMMA_SWITCH;
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                retVal = telephony.getDataEnabled(subId);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#getDataEnabled", e);
        } catch (NullPointerException e2) {
        }
        return retVal;
    }

    public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) {
        try {
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.invokeOemRilRequestRaw(oemReq, oemResp);
            }
        } catch (RemoteException e) {
        } catch (NullPointerException e2) {
        }
        return DATA_UNKNOWN;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
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
        return NETWROK_TYPE_COMMA_SWITCH;
    }

    public static int getIntWithSubId(ContentResolver cr, String name, int subId) throws SettingNotFoundException {
        int val;
        try {
            if (!name.equals(SubscriptionManager.DATA_ROAMING)) {
                return Global.getInt(cr, name + subId);
            }
            val = Global.getInt(cr, name);
            if (subId == SUB2) {
                val = Global.getInt(cr, DATA_ROAMING_SIM2);
            }
            Global.putInt(cr, name + subId, val);
            return val;
        } catch (SettingNotFoundException e) {
            try {
                val = Global.getInt(cr, name);
                Global.putInt(cr, name + subId, val);
                int default_val = val;
                if (name.equals("mobile_data")) {
                    default_val = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.mobiledata", "true")) ? SUB2 : SUB1;
                } else if (name.equals(SubscriptionManager.DATA_ROAMING)) {
                    default_val = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false")) ? SUB2 : SUB1;
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
                return NETWROK_TYPE_COMMA_SWITCH;
            }
            return telephony.isImsRegistered();
        } catch (RemoteException e) {
            return NETWROK_TYPE_COMMA_SWITCH;
        } catch (NullPointerException e2) {
            return NETWROK_TYPE_COMMA_SWITCH;
        }
    }

    public boolean isVolteAvailable() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return NETWROK_TYPE_COMMA_SWITCH;
            }
            return telephony.isVolteAvailable();
        } catch (RemoteException e) {
            return NETWROK_TYPE_COMMA_SWITCH;
        } catch (NullPointerException e2) {
            return NETWROK_TYPE_COMMA_SWITCH;
        }
    }

    public boolean isVideoTelephonyAvailable() {
        try {
            return getITelephony().isVideoTelephonyAvailable();
        } catch (RemoteException e) {
            return NETWROK_TYPE_COMMA_SWITCH;
        } catch (NullPointerException e2) {
            return NETWROK_TYPE_COMMA_SWITCH;
        }
    }

    public boolean isWifiCallingAvailable() {
        try {
            ITelephony telephony = getITelephony();
            if (telephony == null) {
                return NETWROK_TYPE_COMMA_SWITCH;
            }
            return telephony.isWifiCallingAvailable();
        } catch (RemoteException e) {
            return NETWROK_TYPE_COMMA_SWITCH;
        } catch (NullPointerException e2) {
            return NETWROK_TYPE_COMMA_SWITCH;
        }
    }

    public void setSimOperatorNumeric(String numeric) {
        setSimOperatorNumericForPhone(getDefaultPhone(), numeric);
    }

    public void setSimOperatorNumericForPhone(int phoneId, String numeric) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, numeric);
    }

    public void setSimOperatorName(String name) {
        setSimOperatorNameForPhone(getDefaultPhone(), name);
    }

    public void setSimOperatorNameForPhone(int phoneId, String name) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, name);
    }

    public void setSimCountryIso(String iso) {
        setSimCountryIsoForPhone(getDefaultPhone(), iso);
    }

    public void setSimCountryIsoForPhone(int phoneId, String iso) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY, iso);
    }

    public void setSimState(String state) {
        setSimStateForPhone(getDefaultPhone(), state);
    }

    public void setSimStateForPhone(int phoneId, String state) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_SIM_STATE, state);
    }

    public void setBasebandVersion(String version) {
        setBasebandVersionForPhone(getDefaultPhone(), version);
    }

    public void setBasebandVersionForPhone(int phoneId, String version) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_BASEBAND_VERSION, version);
    }

    public void setPhoneType(int type) {
        setPhoneType(getDefaultPhone(), type);
    }

    public void setPhoneType(int phoneId, int type) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.CURRENT_ACTIVE_PHONE, String.valueOf(type));
        }
    }

    public String getOtaSpNumberSchema(String defaultValue) {
        return getOtaSpNumberSchemaForPhone(getDefaultPhone(), defaultValue);
    }

    public String getOtaSpNumberSchemaForPhone(int phoneId, String defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OTASP_NUM_SCHEMA, defaultValue);
        }
        return defaultValue;
    }

    public boolean getSmsReceiveCapable(boolean defaultValue) {
        return getSmsReceiveCapableForPhone(getDefaultPhone(), defaultValue);
    }

    public boolean getSmsReceiveCapableForPhone(int phoneId, boolean defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return Boolean.valueOf(getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_SMS_RECEIVE, String.valueOf(defaultValue))).booleanValue();
        }
        return defaultValue;
    }

    public boolean getSmsSendCapable(boolean defaultValue) {
        return getSmsSendCapableForPhone(getDefaultPhone(), defaultValue);
    }

    public boolean getSmsSendCapableForPhone(int phoneId, boolean defaultValue) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return Boolean.valueOf(getTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_SMS_SEND, String.valueOf(defaultValue))).booleanValue();
        }
        return defaultValue;
    }

    public void setNetworkOperatorName(String name) {
        setNetworkOperatorNameForPhone(getDefaultPhone(), name);
    }

    public void setNetworkOperatorNameForPhone(int phoneId, String name) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ALPHA, name);
        }
    }

    public void setNetworkOperatorNumeric(String numeric) {
        setNetworkOperatorNumericForPhone(getDefaultPhone(), numeric);
    }

    public void setNetworkOperatorNumericForPhone(int phoneId, String numeric) {
        setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, numeric);
    }

    public void setNetworkRoaming(boolean isRoaming) {
        setNetworkRoamingForPhone(getDefaultPhone(), isRoaming);
    }

    public void setNetworkRoamingForPhone(int phoneId, boolean isRoaming) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ISROAMING, isRoaming ? "true" : "false");
        }
    }

    public void setNetworkCountryIso(String iso) {
        setNetworkCountryIsoForPhone(getDefaultPhone(), iso);
    }

    public void setNetworkCountryIsoForPhone(int phoneId, String iso) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            setTelephonyProperty(phoneId, TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, iso);
        }
    }

    public void setDataNetworkType(int type) {
        setDataNetworkTypeForPhone(getDefaultPhone(), type);
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
        int retval = DATA_UNKNOWN;
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                retval = service.getSubIdForPhoneAccount(phoneAccount);
            }
        } catch (RemoteException e) {
        }
        return retval;
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
        result.send(SUB1, null);
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

    public boolean isVoicemailVibrationEnabled(PhoneAccountHandle accountHandle) {
        try {
            ITelephony service = getITelephony();
            if (service != null) {
                return service.isVoicemailVibrationEnabled(accountHandle);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error calling ITelephony#isVoicemailVibrationEnabled", e);
        }
        return NETWROK_TYPE_COMMA_SWITCH;
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
