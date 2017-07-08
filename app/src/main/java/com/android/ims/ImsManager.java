package com.android.ims;

import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.ims.ImsCall.Listener;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener.Stub;
import com.android.ims.internal.IImsService;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.ImsCallSession;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class ImsManager {
    public static final String ACTION_IMS_INCOMING_CALL = "com.android.ims.IMS_INCOMING_CALL";
    public static final String ACTION_IMS_REGISTRATION_ERROR = "com.android.ims.REGISTRATION_ERROR";
    public static final String ACTION_IMS_SERVICE_DOWN = "com.android.ims.IMS_SERVICE_DOWN";
    public static final String ACTION_IMS_SERVICE_UP = "com.android.ims.IMS_SERVICE_UP";
    public static final int CALL_ACTIVE = 1;
    public static final int CALL_ALERTING = 4;
    public static final int CALL_DIALING = 3;
    public static final int CALL_END = 7;
    public static final int CALL_HOLD = 2;
    public static final int CALL_INCOMING = 5;
    public static final int CALL_WAITING = 6;
    private static final boolean DBG = true;
    public static final String EXTRA_CALL_ID = "android:imsCallID";
    public static final String EXTRA_IS_UNKNOWN_CALL = "android:isUnknown";
    public static final String EXTRA_PHONE_ID = "android:phone_id";
    public static final String EXTRA_SERVICE_ID = "android:imsServiceId";
    public static final String EXTRA_UNKNOWN_CALL_STATE = "codeaurora.unknownCallState";
    public static final String EXTRA_USSD = "android:ussd";
    private static final String IMS_SERVICE = "ims";
    public static final int INCOMING_CALL_RESULT_CODE = 101;
    public static final String PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE = "persist.dbg.volte_avail_ovr";
    public static final int PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE_DEFAULT = 0;
    public static final String PROPERTY_DBG_VT_AVAIL_OVERRIDE = "persist.dbg.vt_avail_ovr";
    public static final int PROPERTY_DBG_VT_AVAIL_OVERRIDE_DEFAULT = 0;
    public static final String PROPERTY_DBG_WFC_AVAIL_OVERRIDE = "persist.dbg.wfc_avail_ovr";
    public static final int PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT = 0;
    public static final String PROP_VOWIFI_ENABLE = "ro.config.hw_vowifi";
    private static final String TAG = "ImsManager";
    private static final int VOWIFI_PREFER_INVALID = 3;
    private static HashMap<Integer, ImsManager> sImsManagerInstances;
    private static int userSelectWfcMode;
    private ImsConfig mConfig;
    private boolean mConfigUpdated;
    private Context mContext;
    private ImsServiceDeathRecipient mDeathRecipient;
    private ImsEcbm mEcbm;
    private ImsConfigListener mImsConfigListener;
    private IImsService mImsService;
    private ImsMultiEndpoint mMultiEndpoint;
    private int mPhoneId;
    private ImsUt mUt;

    /* renamed from: com.android.ims.ImsManager.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ ImsManager val$imsManager;
        final /* synthetic */ int val$value;

        AnonymousClass1(ImsManager val$imsManager, int val$value) {
            this.val$imsManager = val$imsManager;
            this.val$value = val$value;
        }

        public void run() {
            try {
                this.val$imsManager.getConfigInterface().setProvisionedValue(27, this.val$value);
            } catch (ImsException e) {
            }
        }
    }

    /* renamed from: com.android.ims.ImsManager.2 */
    static class AnonymousClass2 implements Runnable {
        final /* synthetic */ ImsManager val$imsManager;
        final /* synthetic */ int val$value;

        AnonymousClass2(ImsManager val$imsManager, int val$value) {
            this.val$imsManager = val$imsManager;
            this.val$value = val$value;
        }

        public void run() {
            try {
                this.val$imsManager.getConfigInterface().setProvisionedValue(26, this.val$value);
            } catch (ImsException e) {
            }
        }
    }

    private class ImsRegistrationListenerProxy extends Stub {
        private ImsConnectionStateListener mListener;
        private int mServiceClass;

        public ImsRegistrationListenerProxy(int serviceClass, ImsConnectionStateListener listener) {
            this.mServiceClass = serviceClass;
            this.mListener = listener;
        }

        public boolean isSameProxy(int serviceClass) {
            return this.mServiceClass == serviceClass ? ImsManager.DBG : false;
        }

        @Deprecated
        public void registrationConnected() {
            ImsManager.log("registrationConnected ::");
            if (this.mListener != null) {
                this.mListener.onImsConnected();
            }
        }

        @Deprecated
        public void registrationProgressing() {
            ImsManager.log("registrationProgressing ::");
            if (this.mListener != null) {
                this.mListener.onImsProgressing();
            }
        }

        public void registrationConnectedWithRadioTech(int imsRadioTech) {
            ImsManager.log("registrationConnectedWithRadioTech :: imsRadioTech=" + imsRadioTech);
            if (this.mListener != null) {
                this.mListener.onImsConnected();
            }
        }

        public void registrationProgressingWithRadioTech(int imsRadioTech) {
            ImsManager.log("registrationProgressingWithRadioTech :: imsRadioTech=" + imsRadioTech);
            if (this.mListener != null) {
                this.mListener.onImsProgressing();
            }
        }

        public void registrationDisconnected(ImsReasonInfo imsReasonInfo) {
            ImsManager.log("registrationDisconnected :: imsReasonInfo" + imsReasonInfo);
            if (this.mListener != null) {
                this.mListener.onImsDisconnected(imsReasonInfo);
            }
        }

        public void registrationResumed() {
            ImsManager.log("registrationResumed ::");
            if (this.mListener != null) {
                this.mListener.onImsResumed();
            }
        }

        public void registrationSuspended() {
            ImsManager.log("registrationSuspended ::");
            if (this.mListener != null) {
                this.mListener.onImsSuspended();
            }
        }

        public void registrationServiceCapabilityChanged(int serviceClass, int event) {
            ImsManager.log("registrationServiceCapabilityChanged :: serviceClass=" + serviceClass + ", event=" + event);
            if (this.mListener != null) {
                this.mListener.onImsConnected();
            }
        }

        public void registrationFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
            ImsManager.log("registrationFeatureCapabilityChanged :: serviceClass=" + serviceClass);
            if (this.mListener != null) {
                this.mListener.onFeatureCapabilityChanged(serviceClass, enabledFeatures, disabledFeatures);
            }
        }

        public void voiceMessageCountUpdate(int count) {
            ImsManager.log("voiceMessageCountUpdate :: count=" + count);
            if (this.mListener != null) {
                this.mListener.onVoiceMessageCountChanged(count);
            }
        }

        public void registrationAssociatedUriChanged(Uri[] uris) {
            ImsManager.log("registrationAssociatedUriChanged ::");
            if (this.mListener != null) {
                this.mListener.registrationAssociatedUriChanged(uris);
            }
        }
    }

    private class ImsServiceDeathRecipient implements DeathRecipient {
        private ImsServiceDeathRecipient() {
        }

        public void binderDied() {
            ImsManager.this.mImsService = null;
            ImsManager.this.mUt = null;
            ImsManager.this.mConfig = null;
            ImsManager.this.mEcbm = null;
            ImsManager.this.mMultiEndpoint = null;
            if (ImsManager.this.mContext != null) {
                Intent intent = new Intent(ImsManager.ACTION_IMS_SERVICE_DOWN);
                intent.putExtra(ImsManager.EXTRA_PHONE_ID, ImsManager.this.mPhoneId);
                ImsManager.this.mContext.sendBroadcast(new Intent(intent));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.ImsManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.ImsManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsManager.<clinit>():void");
    }

    public static ImsManager getInstance(Context context, int phoneId) {
        synchronized (sImsManagerInstances) {
            if (sImsManagerInstances.containsKey(Integer.valueOf(phoneId))) {
                ImsManager imsManager = (ImsManager) sImsManagerInstances.get(Integer.valueOf(phoneId));
                return imsManager;
            }
            ImsManager mgr = new ImsManager(context, phoneId);
            sImsManagerInstances.put(Integer.valueOf(phoneId), mgr);
            return mgr;
        }
    }

    public static boolean isEnhanced4gLteModeSettingEnabledByUser(Context context) {
        boolean z = DBG;
        if (!getBooleanCarrierConfig(context, "editable_enhanced_4g_lte_bool")) {
            return DBG;
        }
        int enabled = Global.getInt(context.getContentResolver(), "volte_vt_enabled", CALL_ACTIVE);
        log("isEnhanced4gLteModeSettingEnabledByUser result -> " + enabled);
        if (enabled != CALL_ACTIVE) {
            z = false;
        }
        return z;
    }

    public static void setEnhanced4gLteModeSetting(Context context, boolean enabled) {
        int value = enabled ? CALL_ACTIVE : PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        log("setEnhanced4gLteModeSetting value : " + value);
        Global.putInt(context.getContentResolver(), "volte_vt_enabled", value);
        if (isNonTtyOrTtyOnVolteEnabled(context)) {
            ImsManager imsManager = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
            if (imsManager != null) {
                try {
                    imsManager.setAdvanced4GMode(enabled);
                } catch (ImsException e) {
                }
            }
        }
    }

    public static boolean isNonTtyOrTtyOnVolteEnabled(Context context) {
        if (getBooleanCarrierConfig(context, "carrier_volte_tty_supported_bool")) {
            return DBG;
        }
        boolean result = Secure.getInt(context.getContentResolver(), "preferred_tty_mode", PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT) == 0 ? DBG : false;
        log("isNonTtyOrTtyOnVolteEnabled result -> " + result);
        return result;
    }

    public static boolean isVolteEnabledByPlatform(Context context) {
        if (SystemProperties.getInt(PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE, PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT) == CALL_ACTIVE) {
            return DBG;
        }
        boolean isGbaValid;
        if (context.getResources().getBoolean(17957005) && getBooleanCarrierConfig(context, "carrier_volte_available_bool")) {
            isGbaValid = isGbaValid(context);
        } else {
            isGbaValid = false;
        }
        log("isVolteEnabledByPlatform result -> " + isGbaValid);
        return isGbaValid;
    }

    public static boolean isVolteProvisionedOnDevice(Context context) {
        boolean isProvisioned = DBG;
        if (getBooleanCarrierConfig(context, "carrier_volte_provisioning_required_bool")) {
            isProvisioned = false;
            ImsManager mgr = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
            if (mgr != null) {
                try {
                    ImsConfig config = mgr.getConfigInterface();
                    if (config != null) {
                        isProvisioned = config.getVolteProvisioned();
                    }
                } catch (ImsException e) {
                }
            }
        }
        return isProvisioned;
    }

    public static boolean isVtEnabledByPlatform(Context context) {
        boolean z = false;
        if (SystemProperties.getInt(PROPERTY_DBG_VT_AVAIL_OVERRIDE, PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT) == CALL_ACTIVE) {
            return DBG;
        }
        if (context.getResources().getBoolean(17957009) && getBooleanCarrierConfig(context, "carrier_vt_available_bool")) {
            z = isGbaValid(context);
        }
        return z;
    }

    public static boolean isVtEnabledByUser(Context context) {
        if (Global.getInt(context.getContentResolver(), "vt_ims_enabled", CALL_ACTIVE) == CALL_ACTIVE) {
            return DBG;
        }
        return false;
    }

    public static void setVtSetting(Context context, boolean enabled) {
        int i = CALL_ACTIVE;
        Global.putInt(context.getContentResolver(), "vt_ims_enabled", enabled ? CALL_ACTIVE : PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT);
        ImsManager imsManager = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
        if (imsManager != null) {
            try {
                ImsConfig config = imsManager.getConfigInterface();
                if (!enabled) {
                    i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
                }
                config.setFeatureValue(CALL_ACTIVE, 13, i, imsManager.mImsConfigListener);
                if (enabled) {
                    imsManager.turnOnIms();
                } else if (!getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool")) {
                } else {
                    if (!isVolteEnabledByPlatform(context) || !isEnhanced4gLteModeSettingEnabledByUser(context)) {
                        log("setVtSetting() : imsServiceAllowTurnOff -> turnOffIms");
                        imsManager.turnOffIms();
                    }
                }
            } catch (ImsException e) {
                loge("setVtSetting(): " + e);
            }
        }
    }

    public static boolean isWfcEnabledByUser(Context context) {
        int i;
        ContentResolver contentResolver = context.getContentResolver();
        String str = "wfc_ims_enabled";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool")) {
            i = CALL_ACTIVE;
        } else {
            i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        }
        int enabled = Global.getInt(contentResolver, str, i);
        log("isWfcEnabledByUser result -> " + enabled);
        if (enabled == CALL_ACTIVE) {
            return DBG;
        }
        return false;
    }

    public static void setWfcSetting(Context context, boolean enabled) {
        int i = CALL_ACTIVE;
        Global.putInt(context.getContentResolver(), "wfc_ims_enabled", enabled ? CALL_ACTIVE : PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT);
        ImsManager imsManager = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
        if (imsManager != null) {
            try {
                int i2;
                ImsConfig config = imsManager.getConfigInterface();
                Boolean isVowifiEnable = Boolean.valueOf(isWfcEnabledByPlatform(context));
                if (isVowifiEnable.booleanValue() && VOWIFI_PREFER_INVALID == userSelectWfcMode) {
                    userSelectWfcMode = getWfcMode(context);
                }
                if (enabled) {
                    i2 = CALL_ACTIVE;
                } else {
                    i2 = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
                }
                config.setFeatureValue(CALL_HOLD, 18, i2, imsManager.mImsConfigListener);
                if (enabled) {
                    if (isVowifiEnable.booleanValue()) {
                        log("isVowifiEnable = true, setWfcModeInternal - setting = " + userSelectWfcMode);
                        setWfcModeInternal(context, userSelectWfcMode);
                    }
                    imsManager.turnOnIms();
                } else if (getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool") && !(isVolteEnabledByPlatform(context) && isEnhanced4gLteModeSettingEnabledByUser(context))) {
                    log("setWfcSetting() : imsServiceAllowTurnOff -> turnOffIms");
                    imsManager.turnOffIms();
                }
                if (enabled) {
                    i = getWfcMode(context);
                }
                setWfcModeInternal(context, i);
            } catch (ImsException e) {
                loge("setWfcSetting(): " + e);
            }
        }
    }

    public static int getWfcMode(Context context) {
        int setting = Global.getInt(context.getContentResolver(), "wfc_ims_mode", getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int"));
        log("getWfcMode - setting=" + setting);
        return setting;
    }

    public static void setWfcMode(Context context, int wfcMode) {
        log("setWfcMode - setting=" + wfcMode);
        Global.putInt(context.getContentResolver(), "wfc_ims_mode", wfcMode);
        if (Boolean.valueOf(isWfcEnabledByPlatform(context)).booleanValue()) {
            userSelectWfcMode = wfcMode;
        }
        setWfcModeInternal(context, wfcMode);
    }

    private static void setWfcModeInternal(Context context, int wfcMode) {
        ImsManager imsManager = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
        if (imsManager != null) {
            int value = wfcMode;
            new Thread(new AnonymousClass1(imsManager, wfcMode)).start();
        }
    }

    public static boolean isWfcRoamingEnabledByUser(Context context) {
        int i;
        ContentResolver contentResolver = context.getContentResolver();
        String str = "wfc_ims_roaming_enabled";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool")) {
            i = CALL_ACTIVE;
        } else {
            i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        }
        if (Global.getInt(contentResolver, str, i) == CALL_ACTIVE) {
            return DBG;
        }
        return false;
    }

    public static void setWfcRoamingSetting(Context context, boolean enabled) {
        int i;
        ContentResolver contentResolver = context.getContentResolver();
        String str = "wfc_ims_roaming_enabled";
        if (enabled) {
            i = CALL_ACTIVE;
        } else {
            i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        }
        Global.putInt(contentResolver, str, i);
        setWfcRoamingSettingInternal(context, enabled);
    }

    private static void setWfcRoamingSettingInternal(Context context, boolean enabled) {
        ImsManager imsManager = getInstance(context, SubscriptionManager.getDefaultVoicePhoneId());
        if (imsManager != null) {
            int value;
            if (enabled) {
                value = CALL_ACTIVE;
            } else {
                value = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
            }
            new Thread(new AnonymousClass2(imsManager, value)).start();
        }
    }

    public static boolean isWfcEnabledByPlatform(Context context) {
        boolean result = false;
        if (SystemProperties.getInt(PROPERTY_DBG_WFC_AVAIL_OVERRIDE, PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT) == CALL_ACTIVE) {
            return DBG;
        }
        if (!SystemProperties.getBoolean(PROP_VOWIFI_ENABLE, false)) {
            return false;
        }
        boolean result1 = context.getResources().getBoolean(17957011);
        boolean result2 = getBooleanCarrierConfig(context, "carrier_wfc_ims_available_bool");
        log("Vowifi sim adp : Device =" + result1 + " XML_CarrierConfig =" + result2 + " GbaValid =" + isGbaValid(context));
        if (context.getResources().getBoolean(17957011) && getBooleanCarrierConfig(context, "carrier_wfc_ims_available_bool")) {
            result = isGbaValid(context);
        }
        return result;
    }

    private static boolean isGbaValid(Context context) {
        if (!getBooleanCarrierConfig(context, "carrier_ims_gba_required_bool")) {
            return DBG;
        }
        String efIst = TelephonyManager.getDefault().getIsimIst();
        if (efIst == null) {
            loge("ISF is NULL");
            return DBG;
        }
        boolean result = (efIst == null || efIst.length() <= CALL_ACTIVE) ? false : (((byte) efIst.charAt(CALL_ACTIVE)) & CALL_HOLD) != 0 ? DBG : false;
        log("GBA capable=" + result + ", ISF=" + efIst);
        return result;
    }

    public static void updateImsServiceConfig(Context context, int phoneId, boolean force) {
        if (force || TelephonyManager.getDefault().getSimState() == CALL_INCOMING) {
            ImsManager imsManager = getInstance(context, phoneId);
            if (imsManager != null && (!imsManager.mConfigUpdated || force)) {
                try {
                    if (((imsManager.updateVolteFeatureValue() | imsManager.updateVideoCallFeatureValue()) | imsManager.updateWfcFeatureAndProvisionedValues()) || !getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool")) {
                        imsManager.turnOnIms();
                    } else {
                        imsManager.turnOffIms();
                    }
                    imsManager.mConfigUpdated = DBG;
                } catch (ImsException e) {
                    loge("updateImsServiceConfig: " + e);
                    imsManager.mConfigUpdated = false;
                }
            }
            return;
        }
        log("updateImsServiceConfig: SIM not ready");
    }

    private boolean updateVolteFeatureValue() throws ImsException {
        int i;
        boolean available = isVolteEnabledByPlatform(this.mContext);
        boolean enabled = isEnhanced4gLteModeSettingEnabledByUser(this.mContext);
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled(this.mContext);
        boolean z = (available && enabled) ? isNonTty : false;
        log("updateVolteFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty);
        ImsConfig configInterface = getConfigInterface();
        if (z) {
            i = CALL_ACTIVE;
        } else {
            i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        }
        configInterface.setFeatureValue(PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT, 13, i, this.mImsConfigListener);
        return z;
    }

    private boolean updateVideoCallFeatureValue() throws ImsException {
        boolean isVtEnabledByUser;
        int i;
        boolean available = isVtEnabledByPlatform(this.mContext);
        if (isEnhanced4gLteModeSettingEnabledByUser(this.mContext)) {
            isVtEnabledByUser = isVtEnabledByUser(this.mContext);
        } else {
            isVtEnabledByUser = false;
        }
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled(this.mContext);
        boolean z = (available && isVtEnabledByUser) ? isNonTty : false;
        log("updateVideoCallFeatureValue: available = " + available + ", enabled = " + isVtEnabledByUser + ", nonTTY = " + isNonTty);
        ImsConfig configInterface = getConfigInterface();
        if (z) {
            i = CALL_ACTIVE;
        } else {
            i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        }
        configInterface.setFeatureValue(CALL_ACTIVE, 13, i, this.mImsConfigListener);
        return z;
    }

    private boolean updateWfcFeatureAndProvisionedValues() throws ImsException {
        int i;
        boolean available = isWfcEnabledByPlatform(this.mContext);
        boolean enabled = isWfcEnabledByUser(this.mContext);
        int mode = getWfcMode(this.mContext);
        boolean roaming = isWfcRoamingEnabledByUser(this.mContext);
        boolean z = available ? enabled : false;
        log("updateWfcFeatureAndProvisionedValues: available = " + available + ", enabled = " + enabled + ", mode = " + mode + ", roaming = " + roaming);
        ImsConfig configInterface = getConfigInterface();
        if (z) {
            i = CALL_ACTIVE;
        } else {
            i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        }
        configInterface.setFeatureValue(CALL_HOLD, 18, i, this.mImsConfigListener);
        if (!z) {
            mode = CALL_ACTIVE;
            roaming = false;
        }
        setWfcModeInternal(this.mContext, mode);
        setWfcRoamingSettingInternal(this.mContext, roaming);
        return z;
    }

    private ImsManager(Context context, int phoneId) {
        this.mImsService = null;
        this.mDeathRecipient = new ImsServiceDeathRecipient();
        this.mUt = null;
        this.mConfig = null;
        this.mConfigUpdated = false;
        this.mEcbm = null;
        this.mMultiEndpoint = null;
        this.mContext = context;
        this.mPhoneId = phoneId;
        createImsService(DBG);
    }

    public boolean isServiceAvailable() {
        if (this.mImsService == null && ServiceManager.checkService(getImsServiceName(this.mPhoneId)) == null) {
            return false;
        }
        return DBG;
    }

    public void setImsConfigListener(ImsConfigListener listener) {
        this.mImsConfigListener = listener;
    }

    public int open(int serviceClass, PendingIntent incomingCallPendingIntent, ImsConnectionStateListener listener) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        if (incomingCallPendingIntent == null) {
            throw new NullPointerException("incomingCallPendingIntent can't be null");
        } else if (listener == null) {
            throw new NullPointerException("listener can't be null");
        } else {
            try {
                int result = this.mImsService.open(this.mPhoneId, serviceClass, incomingCallPendingIntent, createRegistrationListenerProxy(serviceClass, listener));
                if (result > 0) {
                    return result;
                }
                throw new ImsException("open()", result * -1);
            } catch (RemoteException e) {
                throw new ImsException("open()", e, 106);
            }
        }
    }

    public void close(int serviceId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mImsService.close(serviceId);
            this.mUt = null;
            this.mConfig = null;
            this.mEcbm = null;
            this.mMultiEndpoint = null;
        } catch (RemoteException e) {
            throw new ImsException("close()", e, 106);
        } catch (Throwable th) {
            this.mUt = null;
            this.mConfig = null;
            this.mEcbm = null;
            this.mMultiEndpoint = null;
        }
    }

    public ImsUtInterface getSupplementaryServiceConfiguration(int serviceId) throws ImsException {
        if (this.mUt == null) {
            checkAndThrowExceptionIfServiceUnavailable();
            try {
                IImsUt iUt = this.mImsService.getUtInterface(serviceId);
                if (iUt == null) {
                    throw new ImsException("getSupplementaryServiceConfiguration()", 801);
                }
                this.mUt = new ImsUt(iUt);
            } catch (RemoteException e) {
                throw new ImsException("getSupplementaryServiceConfiguration()", e, 106);
            }
        }
        return this.mUt;
    }

    public boolean isConnected(int serviceId, int serviceType, int callType) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.isConnected(serviceId, serviceType, callType);
        } catch (RemoteException e) {
            throw new ImsException("isServiceConnected()", e, 106);
        }
    }

    public boolean isOpened(int serviceId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.isOpened(serviceId);
        } catch (RemoteException e) {
            throw new ImsException("isOpened()", e, 106);
        }
    }

    public ImsCallProfile createCallProfile(int serviceId, int serviceType, int callType) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mImsService.createCallProfile(serviceId, serviceType, callType);
        } catch (RemoteException e) {
            throw new ImsException("createCallProfile()", e, 106);
        }
    }

    public ImsCall makeCall(int serviceId, ImsCallProfile profile, String[] callees, Listener listener) throws ImsException {
        log("makeCall :: serviceId=" + serviceId + ", profile=" + profile + ", callees=" + callees);
        checkAndThrowExceptionIfServiceUnavailable();
        ImsCall call = new ImsCall(this.mContext, profile);
        call.setListener(listener);
        ImsCallSession session = createCallSession(serviceId, profile);
        if (profile.getCallExtraBoolean("isConferenceUri", false) || callees == null || callees.length != CALL_ACTIVE) {
            call.start(session, callees);
        } else {
            call.start(session, callees[PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT]);
        }
        return call;
    }

    public ImsCall takeCall(int serviceId, Intent incomingCallIntent, Listener listener) throws ImsException {
        log("takeCall :: serviceId=" + serviceId + ", incomingCall=" + incomingCallIntent);
        checkAndThrowExceptionIfServiceUnavailable();
        if (incomingCallIntent == null) {
            throw new ImsException("Can't retrieve session with null intent", INCOMING_CALL_RESULT_CODE);
        } else if (serviceId != getServiceId(incomingCallIntent)) {
            throw new ImsException("Service id is mismatched in the incoming call intent", INCOMING_CALL_RESULT_CODE);
        } else {
            String callId = getCallId(incomingCallIntent);
            if (callId == null) {
                throw new ImsException("Call ID missing in the incoming call intent", INCOMING_CALL_RESULT_CODE);
            }
            try {
                IImsCallSession session = this.mImsService.getPendingCallSession(serviceId, callId);
                if (session == null) {
                    throw new ImsException("No pending session for the call", 107);
                }
                ImsCall call = new ImsCall(this.mContext, session.getCallProfile());
                call.attachSession(new ImsCallSession(session));
                call.setListener(listener);
                return call;
            } catch (Throwable t) {
                ImsException imsException = new ImsException("takeCall()", t, PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT);
            }
        }
    }

    public ImsConfig getConfigInterface() throws ImsException {
        if (this.mConfig == null) {
            checkAndThrowExceptionIfServiceUnavailable();
            try {
                IImsConfig config = this.mImsService.getConfigInterface(this.mPhoneId);
                if (config == null) {
                    throw new ImsException("getConfigInterface()", 131);
                }
                this.mConfig = new ImsConfig(config, this.mContext);
            } catch (RemoteException e) {
                throw new ImsException("getConfigInterface()", e, 106);
            }
        }
        log("getConfigInterface(), mConfig= " + this.mConfig);
        return this.mConfig;
    }

    public void setUiTTYMode(Context context, int serviceId, int uiTtyMode, Message onComplete) throws ImsException {
        boolean z = false;
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mImsService.setUiTTYMode(serviceId, uiTtyMode, onComplete);
            if (!getBooleanCarrierConfig(context, "carrier_volte_tty_supported_bool")) {
                if (uiTtyMode == 0) {
                    z = isEnhanced4gLteModeSettingEnabledByUser(context);
                }
                setAdvanced4GMode(z);
            }
        } catch (RemoteException e) {
            throw new ImsException("setTTYMode()", e, 106);
        }
    }

    private static boolean getBooleanCarrierConfig(Context context, String key) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        }
        if (b != null) {
            return b.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }

    private static int getIntCarrierConfig(Context context, String key) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        }
        if (b != null) {
            return b.getInt(key);
        }
        return CarrierConfigManager.getDefaultConfig().getInt(key);
    }

    private static String getCallId(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return null;
        }
        return incomingCallIntent.getStringExtra(EXTRA_CALL_ID);
    }

    private static int getServiceId(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return -1;
        }
        return incomingCallIntent.getIntExtra(EXTRA_SERVICE_ID, -1);
    }

    private void checkAndThrowExceptionIfServiceUnavailable() throws ImsException {
        if (this.mImsService == null) {
            createImsService(DBG);
            if (this.mImsService == null) {
                throw new ImsException("Service is unavailable", 106);
            }
        }
    }

    private static String getImsServiceName(int phoneId) {
        return IMS_SERVICE;
    }

    private void createImsService(boolean checkService) {
        if (!checkService || ServiceManager.checkService(getImsServiceName(this.mPhoneId)) != null) {
            IBinder b = ServiceManager.getService(getImsServiceName(this.mPhoneId));
            if (b != null) {
                try {
                    b.linkToDeath(this.mDeathRecipient, PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT);
                } catch (RemoteException e) {
                }
            }
            this.mImsService = IImsService.Stub.asInterface(b);
        }
    }

    private ImsCallSession createCallSession(int serviceId, ImsCallProfile profile) throws ImsException {
        try {
            return new ImsCallSession(this.mImsService.createCallSession(serviceId, profile, null));
        } catch (RemoteException e) {
            return null;
        }
    }

    private ImsRegistrationListenerProxy createRegistrationListenerProxy(int serviceClass, ImsConnectionStateListener listener) {
        return new ImsRegistrationListenerProxy(serviceClass, listener);
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void loge(String s) {
        Rlog.e(TAG, s);
    }

    private static void loge(String s, Throwable t) {
        Rlog.e(TAG, s, t);
    }

    private void turnOnIms() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mImsService.turnOnIms(this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("turnOnIms() ", e, 106);
        }
    }

    private boolean isImsTurnOffAllowed() {
        if (!getBooleanCarrierConfig(this.mContext, "carrier_allow_turnoff_ims_bool")) {
            return false;
        }
        if (isWfcEnabledByPlatform(this.mContext) && isWfcEnabledByUser(this.mContext)) {
            return false;
        }
        return DBG;
    }

    private void setAdvanced4GMode(boolean turnOn) throws ImsException {
        int i = CALL_ACTIVE;
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            ImsConfig config = getConfigInterface();
            if (config != null && (turnOn || !isImsTurnOffAllowed())) {
                int i2;
                if (turnOn) {
                    i2 = CALL_ACTIVE;
                } else {
                    i2 = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
                }
                config.setFeatureValue(PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT, 13, i2, this.mImsConfigListener);
                if (isVtEnabledByPlatform(this.mContext)) {
                    if (!(turnOn ? isVtEnabledByUser(this.mContext) : false)) {
                        i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
                    }
                    config.setFeatureValue(CALL_ACTIVE, 13, i, this.mImsConfigListener);
                }
            }
        } catch (ImsException e) {
            log("setAdvanced4GMode() : " + e);
        }
        if (turnOn) {
            turnOnIms();
        } else if (isImsTurnOffAllowed()) {
            log("setAdvanced4GMode() : imsServiceAllowTurnOff -> turnOffIms");
            turnOffIms();
        }
    }

    private void turnOffIms() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mImsService.turnOffIms(this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("turnOffIms() ", e, 106);
        }
    }

    public ImsEcbm getEcbmInterface(int serviceId) throws ImsException {
        if (this.mEcbm == null) {
            checkAndThrowExceptionIfServiceUnavailable();
            try {
                IImsEcbm iEcbm = this.mImsService.getEcbmInterface(serviceId);
                if (iEcbm == null) {
                    throw new ImsException("getEcbmInterface()", 901);
                }
                this.mEcbm = new ImsEcbm(iEcbm);
            } catch (RemoteException e) {
                throw new ImsException("getEcbmInterface()", e, 106);
            }
        }
        return this.mEcbm;
    }

    public ImsMultiEndpoint getMultiEndpointInterface(int serviceId) throws ImsException {
        if (this.mMultiEndpoint == null) {
            checkAndThrowExceptionIfServiceUnavailable();
            try {
                IImsMultiEndpoint iImsMultiEndpoint = this.mImsService.getMultiEndpointInterface(serviceId);
                if (iImsMultiEndpoint == null) {
                    throw new ImsException("getMultiEndpointInterface()", 902);
                }
                this.mMultiEndpoint = new ImsMultiEndpoint(iImsMultiEndpoint);
            } catch (RemoteException e) {
                throw new ImsException("getMultiEndpointInterface()", e, 106);
            }
        }
        return this.mMultiEndpoint;
    }

    public static void factoryReset(Context context) {
        int i;
        int i2 = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        Global.putInt(context.getContentResolver(), "volte_vt_enabled", CALL_ACTIVE);
        ContentResolver contentResolver = context.getContentResolver();
        String str = "wfc_ims_enabled";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool")) {
            i = CALL_ACTIVE;
        } else {
            i = PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT;
        }
        Global.putInt(contentResolver, str, i);
        Global.putInt(context.getContentResolver(), "wfc_ims_mode", getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int"));
        ContentResolver contentResolver2 = context.getContentResolver();
        String str2 = "wfc_ims_roaming_enabled";
        if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool")) {
            i2 = CALL_ACTIVE;
        }
        Global.putInt(contentResolver2, str2, i2);
        Global.putInt(context.getContentResolver(), "vt_ims_enabled", CALL_ACTIVE);
        updateImsServiceConfig(context, SubscriptionManager.getDefaultVoicePhoneId(), DBG);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ImsManager:");
        pw.println("  mPhoneId = " + this.mPhoneId);
        pw.println("  mConfigUpdated = " + this.mConfigUpdated);
        pw.println("  mImsService = " + this.mImsService);
        pw.println("  isGbaValid = " + isGbaValid(this.mContext));
        pw.println("  isImsTurnOffAllowed = " + isImsTurnOffAllowed());
        pw.println("  isNonTtyOrTtyOnVolteEnabled = " + isNonTtyOrTtyOnVolteEnabled(this.mContext));
        pw.println("  isVolteEnabledByPlatform = " + isVolteEnabledByPlatform(this.mContext));
        pw.println("  isVolteProvisionedOnDevice = " + isVolteProvisionedOnDevice(this.mContext));
        pw.println("  isEnhanced4gLteModeSettingEnabledByUser = " + isEnhanced4gLteModeSettingEnabledByUser(this.mContext));
        pw.println("  isVtEnabledByPlatform = " + isVtEnabledByPlatform(this.mContext));
        pw.println("  isVtEnabledByUser = " + isVtEnabledByUser(this.mContext));
        pw.println("  isWfcEnabledByPlatform = " + isWfcEnabledByPlatform(this.mContext));
        pw.println("  isWfcEnabledByUser = " + isWfcEnabledByUser(this.mContext));
        pw.println("  getWfcMode = " + getWfcMode(this.mContext));
        pw.println("  isWfcRoamingEnabledByUser = " + isWfcRoamingEnabledByUser(this.mContext));
        pw.flush();
    }
}
