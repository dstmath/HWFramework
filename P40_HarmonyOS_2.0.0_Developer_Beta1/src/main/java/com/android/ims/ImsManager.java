package com.android.ims;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsMmTelManager;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.aidl.IImsCapabilityCallback;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsConfigCallback;
import android.telephony.ims.aidl.IImsRegistrationCallback;
import android.telephony.ims.aidl.IImsSmsListener;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.MmTelFeature;
import android.util.Log;
import com.android.ims.ImsCall;
import com.android.ims.ImsManager;
import com.android.ims.MmTelFeatureConnection;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsUt;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.ITelephony;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class ImsManager {
    public static final String ACTION_IMS_INCOMING_CALL = "com.android.ims.IMS_INCOMING_CALL";
    public static final String ACTION_IMS_REGISTRATION_ERROR = "com.android.ims.REGISTRATION_ERROR";
    public static final String ACTION_IMS_SERVICE_DOWN = "com.android.ims.IMS_SERVICE_DOWN";
    public static final String ACTION_IMS_SERVICE_UP = "com.android.ims.IMS_SERVICE_UP";
    private static final boolean DBG = true;
    public static final String EXTRA_CALL_ID = "android:imsCallID";
    public static final String EXTRA_IS_UNKNOWN_CALL = "android:isUnknown";
    public static final String EXTRA_PHONE_ID = "android:phone_id";
    public static final String EXTRA_SERVICE_ID = "android:imsServiceId";
    public static final String EXTRA_USSD = "android:ussd";
    public static final String FALSE = "false";
    public static final int INCOMING_CALL_RESULT_CODE = 101;
    private static final int MAX_RECENT_DISCONNECT_REASONS = 16;
    public static final String PROPERTY_DBG_ALLOW_IMS_OFF_OVERRIDE = "persist.dbg.allow_ims_off";
    public static final int PROPERTY_DBG_ALLOW_IMS_OFF_OVERRIDE_DEFAULT = 0;
    public static final String PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE = "persist.dbg.volte_avail_ovr";
    public static final int PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE_DEFAULT = 0;
    public static final String PROPERTY_DBG_VT_AVAIL_OVERRIDE = "persist.dbg.vt_avail_ovr";
    public static final int PROPERTY_DBG_VT_AVAIL_OVERRIDE_DEFAULT = 0;
    public static final String PROPERTY_DBG_WFC_AVAIL_OVERRIDE = "persist.dbg.wfc_avail_ovr";
    public static final int PROPERTY_DBG_WFC_AVAIL_OVERRIDE_DEFAULT = 0;
    public static final String PROP_VILTE_ENABLE = "ro.config.hw_vtlte_on";
    public static final String PROP_VOLTE_ENABLE = "ro.config.hw_volte_on";
    public static final String PROP_VOWIFI_ENABLE = "ro.vendor.config.hw_vowifi";
    private static final int RESPONSE_WAIT_TIME_MS = 3000;
    private static final int SUB_PROPERTY_NOT_INITIALIZED = -1;
    private static final int SYSTEM_PROPERTY_NOT_SET = -1;
    private static final String TAG = "ImsManager";
    public static final String TRUE = "true";
    private static final int VOWIFI_PREFER_INVALID = 3;
    private static HashMap<Integer, ImsManager> sImsManagerInstances = new HashMap<>();
    private static int userSelectWfcMode = VOWIFI_PREFER_INVALID;
    private final boolean mConfigDynamicBind;
    private CarrierConfigManager mConfigManager;
    private boolean mConfigUpdated = false;
    private Context mContext;
    private ImsEcbm mEcbm = null;
    @VisibleForTesting
    public ExecutorFactory mExecutorFactory = $$Lambda$ImsManager$Flxe43OUFnnU0pgnksvwPE6o3Mk.INSTANCE;
    private boolean mGetImsService = false;
    private HwCustImsManager mHwCustImsManager;
    private ImsConfigListener mImsConfigListener;
    private MmTelFeatureConnection mMmTelFeatureConnection = null;
    private ImsMultiEndpoint mMultiEndpoint = null;
    private int mPhoneId;
    private ConcurrentLinkedDeque<ImsReasonInfo> mRecentDisconnectReasons = new ConcurrentLinkedDeque<>();
    private Set<MmTelFeatureConnection.IFeatureUpdate> mStatusCallbacks = new CopyOnWriteArraySet();
    private ImsUt mUt = null;

    @VisibleForTesting
    public interface ExecutorFactory {
        void executeRunnable(Runnable runnable);
    }

    public static class Connector extends Handler {
        private static final int CEILING_SERVICE_RETRY_COUNT = 6;
        private static final int IMS_RETRY_STARTING_TIMEOUT_MS = 500;
        private final Context mContext;
        private final Executor mExecutor;
        private final Runnable mGetServiceRunnable = new Runnable() {
            /* class com.android.ims.$$Lambda$ImsManager$Connector$N5r1SvOgM0jfHDwKkcQbyw_uTP0 */

            @Override // java.lang.Runnable
            public final void run() {
                ImsManager.Connector.this.lambda$new$0$ImsManager$Connector();
            }
        };
        private ImsManager mImsManager;
        private final Listener mListener;
        private final Object mLock = new Object();
        private MmTelFeatureConnection.IFeatureUpdate mNotifyStatusChangedCallback = new MmTelFeatureConnection.IFeatureUpdate() {
            /* class com.android.ims.ImsManager.Connector.AnonymousClass1 */

            @Override // com.android.ims.MmTelFeatureConnection.IFeatureUpdate
            public void notifyStateChanged() {
                Connector.this.mExecutor.execute(new Runnable() {
                    /* class com.android.ims.$$Lambda$ImsManager$Connector$1$QkUK3GnYms22eckyg3OLBmtP3M */

                    @Override // java.lang.Runnable
                    public final void run() {
                        ImsManager.Connector.AnonymousClass1.this.lambda$notifyStateChanged$0$ImsManager$Connector$1();
                    }
                });
            }

            public /* synthetic */ void lambda$notifyStateChanged$0$ImsManager$Connector$1() {
                int status = 0;
                try {
                    synchronized (Connector.this.mLock) {
                        if (Connector.this.mImsManager != null) {
                            status = Connector.this.mImsManager.getImsServiceState();
                        }
                    }
                    if (status == 0 || status == 1) {
                        Connector.this.notifyNotReady();
                    } else if (status != 2) {
                        Log.w(ImsManager.TAG, "Unexpected State!");
                    } else {
                        Connector.this.notifyReady();
                    }
                } catch (ImsException e) {
                    Connector.this.notifyNotReady();
                    Connector.this.retryGetImsService();
                }
            }

            @Override // com.android.ims.MmTelFeatureConnection.IFeatureUpdate
            public void notifyUnavailable() {
                Connector.this.mExecutor.execute(new Runnable() {
                    /* class com.android.ims.$$Lambda$ImsManager$Connector$1$noNC6hbyVe0dHnOoOYgo9PyTSxw */

                    @Override // java.lang.Runnable
                    public final void run() {
                        ImsManager.Connector.AnonymousClass1.this.lambda$notifyUnavailable$1$ImsManager$Connector$1();
                    }
                });
            }

            public /* synthetic */ void lambda$notifyUnavailable$1$ImsManager$Connector$1() {
                Connector.this.notifyNotReady();
                Connector.this.retryGetImsService();
            }
        };
        private final int mPhoneId;
        private int mRetryCount = 0;
        @VisibleForTesting
        public RetryTimeout mRetryTimeout = new RetryTimeout() {
            /* class com.android.ims.$$Lambda$ImsManager$Connector$yM9scWJWjDp_h0yrkCgrjFZH5oI */

            @Override // com.android.ims.ImsManager.Connector.RetryTimeout
            public final int get() {
                return ImsManager.Connector.this.lambda$new$1$ImsManager$Connector();
            }
        };

        public interface Listener {
            void connectionReady(ImsManager imsManager) throws ImsException;

            void connectionUnavailable();
        }

        @VisibleForTesting
        public interface RetryTimeout {
            int get();
        }

        public /* synthetic */ void lambda$new$0$ImsManager$Connector() {
            try {
                getImsService();
            } catch (ImsException e) {
                retryGetImsService();
            }
        }

        public /* synthetic */ int lambda$new$1$ImsManager$Connector() {
            int timeout;
            synchronized (this.mLock) {
                timeout = (1 << this.mRetryCount) * IMS_RETRY_STARTING_TIMEOUT_MS;
                if (this.mRetryCount <= CEILING_SERVICE_RETRY_COUNT) {
                    this.mRetryCount++;
                }
            }
            return timeout;
        }

        public Connector(Context context, int phoneId, Listener listener) {
            this.mContext = context;
            this.mPhoneId = phoneId;
            this.mListener = listener;
            this.mExecutor = new HandlerExecutor(this);
        }

        @VisibleForTesting
        public Connector(Context context, int phoneId, Listener listener, Executor executor) {
            this.mContext = context;
            this.mPhoneId = phoneId;
            this.mListener = listener;
            this.mExecutor = executor;
        }

        public void connect() {
            if (ImsManager.isImsSupportedOnDevice(this.mContext)) {
                this.mRetryCount = 0;
                post(this.mGetServiceRunnable);
            }
        }

        public void disconnect() {
            removeCallbacks(this.mGetServiceRunnable);
            synchronized (this.mLock) {
                if (this.mImsManager != null) {
                    this.mImsManager.removeNotifyStatusChangedCallback(this.mNotifyStatusChangedCallback);
                }
            }
            notifyNotReady();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void retryGetImsService() {
            synchronized (this.mLock) {
                if (this.mImsManager != null) {
                    this.mImsManager.removeNotifyStatusChangedCallback(this.mNotifyStatusChangedCallback);
                    this.mImsManager = null;
                }
                removeCallbacks(this.mGetServiceRunnable);
                postDelayed(this.mGetServiceRunnable, (long) this.mRetryTimeout.get());
            }
        }

        private void getImsService() throws ImsException {
            synchronized (this.mLock) {
                this.mImsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
                if (this.mImsManager != null) {
                    this.mImsManager.addNotifyStatusChangedCallbackIfAvailable(this.mNotifyStatusChangedCallback);
                }
            }
            this.mNotifyStatusChangedCallback.notifyStateChanged();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyReady() throws ImsException {
            ImsManager manager;
            synchronized (this.mLock) {
                manager = this.mImsManager;
            }
            try {
                this.mListener.connectionReady(manager);
                synchronized (this.mLock) {
                    this.mRetryCount = 0;
                }
            } catch (ImsException e) {
                Log.w(ImsManager.TAG, "Connector: notifyReady exception: " + e.getMessage());
                throw e;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyNotReady() {
            this.mListener.connectionUnavailable();
        }
    }

    public static ImsManager getInstance(Context context, int phoneId) {
        synchronized (sImsManagerInstances) {
            if (sImsManagerInstances.containsKey(Integer.valueOf(phoneId))) {
                ImsManager m = sImsManagerInstances.get(Integer.valueOf(phoneId));
                if (m != null) {
                    m.connectIfServiceIsAvailable();
                }
                return m;
            }
            ImsManager mgr = new ImsManager(context, phoneId);
            sImsManagerInstances.put(Integer.valueOf(phoneId), mgr);
            return mgr;
        }
    }

    public boolean isGetImsService() {
        return this.mGetImsService;
    }

    public static boolean isImsSupportedOnDevice(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.telephony.ims");
    }

    public static boolean isEnhanced4gLteModeSettingEnabledByUser(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isEnhanced4gLteModeSettingEnabledByUser();
        }
        loge("isEnhanced4gLteModeSettingEnabledByUser: ImsManager null, returning default value.");
        return false;
    }

    public boolean isEnhanced4gLteModeSettingEnabledByUser() {
        int setting = SubscriptionManager.getIntegerSubscriptionProperty(getSubId(), "volte_vt_enabled", -1, this.mContext);
        boolean onByDefault = getBooleanCarrierConfig("enhanced_4g_lte_on_by_default_bool");
        boolean isHideVolteSwitch = getBooleanCarrierConfig("hide_enhanced_4g_lte_bool") || !getBooleanCarrierConfig("carrier_volte_show_switch_bool");
        logi("isEnhanced4gLteModeSettingEnabledByUser: isHideVolteSwitch = " + isHideVolteSwitch + ", setting = " + setting);
        if (!getBooleanCarrierConfig("editable_enhanced_4g_lte_bool") || isHideVolteSwitch || setting == -1) {
            return onByDefault;
        }
        if (setting == 1) {
            return DBG;
        }
        return false;
    }

    public static void setEnhanced4gLteModeSetting(Context context, boolean enabled) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            mgr.setEnhanced4gLteModeSetting(enabled);
        }
        loge("setEnhanced4gLteModeSetting: ImsManager null, value not set.");
    }

    public void setEnhanced4gLteModeSetting(boolean enabled) {
        HwCustImsManager hwCustImsManager;
        if (!enabled || isVolteProvisionedOnDevice()) {
            int subId = getSubId();
            int i = 0;
            boolean isHideVolteSwitch = getBooleanCarrierConfig("hide_enhanced_4g_lte_bool") || !getBooleanCarrierConfig("carrier_volte_show_switch_bool");
            logi("setEnhanced4gLteModeSetting: isHideVolteSwitch = " + isHideVolteSwitch);
            if (!getBooleanCarrierConfig("editable_enhanced_4g_lte_bool") || isHideVolteSwitch) {
                enabled = getBooleanCarrierConfig("enhanced_4g_lte_on_by_default_bool");
            }
            int prevSetting = SubscriptionManager.getIntegerSubscriptionProperty(subId, "volte_vt_enabled", -1, this.mContext);
            if (enabled) {
                i = 1;
            }
            if (prevSetting != i || ((hwCustImsManager = this.mHwCustImsManager) != null && hwCustImsManager.isForceSetLteFeature())) {
                if (isSubIdValid(subId)) {
                    SubscriptionManager.setSubscriptionProperty(subId, "volte_vt_enabled", booleanToPropertyString(enabled));
                } else {
                    loge("setEnhanced4gLteModeSetting: invalid sub id, can not set property in  siminfo db; subId=" + subId);
                }
                if (isNonTtyOrTtyOnVolteEnabled()) {
                    try {
                        setAdvanced4GMode(enabled);
                    } catch (ImsException e) {
                    }
                }
            }
        } else {
            logi("setEnhanced4gLteModeSetting: Not possible to enable VoLTE due to provisioning.");
        }
    }

    public static boolean isNonTtyOrTtyOnVolteEnabled(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isNonTtyOrTtyOnVolteEnabled();
        }
        loge("isNonTtyOrTtyOnVolteEnabled: ImsManager null, returning default value.");
        return false;
    }

    public boolean isNonTtyOrTtyOnVolteEnabled() {
        if (isTtyOnVoLteCapable()) {
            return DBG;
        }
        TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
        if (tm == null) {
            Log.w(TAG, "isNonTtyOrTtyOnVolteEnabled: telecom not available");
            return DBG;
        } else if (tm.getCurrentTtyMode() == 0) {
            return DBG;
        } else {
            return false;
        }
    }

    public boolean isTtyOnVoLteCapable() {
        return getBooleanCarrierConfig("carrier_volte_tty_supported_bool");
    }

    public static boolean isVolteEnabledByPlatform(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isVolteEnabledByPlatform();
        }
        loge("isVolteEnabledByPlatform: ImsManager null, returning default value.");
        return false;
    }

    public boolean isVolteEnabledByPlatform() {
        if (SystemProperties.getInt(PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE + Integer.toString(this.mPhoneId), -1) == 1 || SystemProperties.getInt(PROPERTY_DBG_VOLTE_AVAIL_OVERRIDE, -1) == 1) {
            return DBG;
        }
        if (!SystemProperties.getBoolean(PROP_VOLTE_ENABLE, false)) {
            logi("isVolteEnabledByPlatform ro.config.hw_volte_on is false");
            return false;
        } else if (!this.mContext.getResources().getBoolean(17891404) || !getBooleanCarrierConfig("carrier_volte_available_bool") || !isGbaValid()) {
            return false;
        } else {
            return DBG;
        }
    }

    public static boolean isVolteProvisionedOnDevice(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isVolteProvisionedOnDevice();
        }
        loge("isVolteProvisionedOnDevice: ImsManager null, returning default value.");
        return DBG;
    }

    public boolean isVolteProvisionedOnDevice() {
        if (getBooleanCarrierConfig("carrier_volte_provisioning_required_bool")) {
            return isVolteProvisioned();
        }
        return DBG;
    }

    public static boolean isWfcProvisionedOnDevice(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isWfcProvisionedOnDevice();
        }
        loge("isWfcProvisionedOnDevice: ImsManager null, returning default value.");
        return DBG;
    }

    public boolean isWfcProvisionedOnDevice() {
        if (getBooleanCarrierConfig("carrier_volte_override_wfc_provisioning_bool") && !isVolteProvisionedOnDevice()) {
            return false;
        }
        if (getBooleanCarrierConfig("carrier_volte_provisioning_required_bool")) {
            return isWfcProvisioned();
        }
        return DBG;
    }

    public static boolean isVtProvisionedOnDevice(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isVtProvisionedOnDevice();
        }
        loge("isVtProvisionedOnDevice: ImsManager null, returning default value.");
        return DBG;
    }

    public boolean isVtProvisionedOnDevice() {
        if (getBooleanCarrierConfig("carrier_volte_provisioning_required_bool")) {
            return isVtProvisioned();
        }
        return DBG;
    }

    public static boolean isVtEnabledByPlatform(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isVtEnabledByPlatform();
        }
        loge("isVtEnabledByPlatform: ImsManager null, returning default value.");
        return false;
    }

    public boolean isVtEnabledByPlatform() {
        if (SystemProperties.getInt(PROPERTY_DBG_VT_AVAIL_OVERRIDE + Integer.toString(this.mPhoneId), -1) == 1 || SystemProperties.getInt(PROPERTY_DBG_VT_AVAIL_OVERRIDE, -1) == 1) {
            return DBG;
        }
        if (!SystemProperties.getBoolean(PROP_VILTE_ENABLE, false)) {
            logi("isVtEnabledByPlatform ro.config.hw_vtlte_on is false");
            return false;
        } else if (!this.mContext.getResources().getBoolean(17891405) || !getBooleanCarrierConfig("carrier_vt_available_bool") || !isGbaValid()) {
            return false;
        } else {
            return DBG;
        }
    }

    public static boolean isVtEnabledByUser(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isVtEnabledByUser();
        }
        loge("isVtEnabledByUser: ImsManager null, returning default value.");
        return false;
    }

    public boolean isVtEnabledByUser() {
        int setting = SubscriptionManager.getIntegerSubscriptionProperty(getSubId(), "vt_ims_enabled", -1, this.mContext);
        if (setting == -1 || setting == 1) {
            return DBG;
        }
        return false;
    }

    public static void setVtSetting(Context context, boolean enabled) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            mgr.setVtSetting(enabled);
        }
        loge("setVtSetting: ImsManager null, can not set value.");
    }

    public void setVtSetting(boolean enabled) {
        if (!enabled || isVtProvisionedOnDevice()) {
            int subId = getSubId();
            if (isSubIdValid(subId)) {
                SubscriptionManager.setSubscriptionProperty(subId, "vt_ims_enabled", booleanToPropertyString(enabled));
            } else {
                loge("setVtSetting: sub id invalid, skip modifying vt state in subinfo db; subId=" + subId);
            }
            try {
                changeMmTelCapability(2, 0, enabled);
                if (enabled) {
                    logi("setVtSetting(b) : turnOnIms");
                    turnOnIms();
                } else if (!isTurnOffImsAllowedByPlatform()) {
                } else {
                    if (!isVolteEnabledByPlatform() || !isEnhanced4gLteModeSettingEnabledByUser()) {
                        logi("setVtSetting(b) : imsServiceAllowTurnOff -> turnOffIms");
                        turnOffIms();
                    }
                }
            } catch (ImsException e) {
                loge("setVtSetting(b): ", e);
            }
        } else {
            logi("setVtSetting: Not possible to enable Vt due to provisioning.");
        }
    }

    private static boolean isTurnOffImsAllowedByPlatform(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isTurnOffImsAllowedByPlatform();
        }
        loge("isTurnOffImsAllowedByPlatform: ImsManager null, returning default value.");
        return DBG;
    }

    private boolean isTurnOffImsAllowedByPlatform() {
        if (SystemProperties.getInt(PROPERTY_DBG_ALLOW_IMS_OFF_OVERRIDE + Integer.toString(this.mPhoneId), -1) == 1 || SystemProperties.getInt(PROPERTY_DBG_ALLOW_IMS_OFF_OVERRIDE, -1) == 1) {
            return DBG;
        }
        return getBooleanCarrierConfig("carrier_allow_turnoff_ims_bool");
    }

    public static boolean isWfcEnabledByUser(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isWfcEnabledByUser();
        }
        loge("isWfcEnabledByUser: ImsManager null, returning default value.");
        return DBG;
    }

    public boolean isWfcEnabledByUser() {
        int setting = SubscriptionManager.getIntegerSubscriptionProperty(getSubId(), "wfc_ims_enabled", -1, this.mContext);
        if (setting == -1) {
            return getBooleanCarrierConfig("carrier_default_wfc_ims_enabled_bool");
        }
        if (setting == 1) {
            return DBG;
        }
        return false;
    }

    public static void setWfcSetting(Context context, boolean enabled) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            mgr.setWfcSetting(enabled);
        }
        loge("setWfcSetting: ImsManager null, can not set value.");
    }

    public void setWfcSetting(boolean enabled) {
        if (!enabled || isWfcProvisionedOnDevice()) {
            int subId = getSubId();
            if (isSubIdValid(subId)) {
                SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_enabled", booleanToPropertyString(enabled));
            } else {
                loge("setWfcSetting: invalid sub id, can not set WFC setting in siminfo db; subId=" + subId);
            }
            setWfcNonPersistent(enabled, getWfcMode(((TelephonyManager) this.mContext.getSystemService("phone")).isNetworkRoaming(subId)));
            return;
        }
        logi("setWfcSetting: Not possible to enable WFC due to provisioning.");
    }

    public void setWfcNonPersistent(boolean enabled, int wfcMode) {
        boolean z = DBG;
        int imsWfcModeFeatureValue = enabled ? wfcMode : 1;
        try {
            changeMmTelCapability(1, 1, enabled);
            setWfcModeInternal(imsWfcModeFeatureValue);
            if (!enabled || !isWfcRoamingEnabledByUser()) {
                z = false;
            }
            setWfcRoamingSettingInternal(z);
            if (enabled) {
                logi("setWfcSetting() : turnOnIms");
                turnOnIms();
            } else if (!isTurnOffImsAllowedByPlatform()) {
            } else {
                if (!isVolteEnabledByPlatform() || !isEnhanced4gLteModeSettingEnabledByUser()) {
                    logi("setWfcSetting() : imsServiceAllowTurnOff -> turnOffIms");
                    turnOffIms();
                }
            }
        } catch (ImsException e) {
            loge("setWfcSetting(): ", e);
        }
    }

    public static int getWfcMode(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.getWfcMode();
        }
        loge("getWfcMode: ImsManager null, returning default value.");
        return 0;
    }

    public int getWfcMode() {
        return getWfcMode(false);
    }

    public static void setWfcMode(Context context, int wfcMode) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            mgr.setWfcMode(wfcMode);
        }
        loge("setWfcMode: ImsManager null, can not set value.");
        logi("setWfcMode - setting=" + wfcMode);
        if (true == Boolean.valueOf(isWfcEnabledByPlatform(context)).booleanValue()) {
            userSelectWfcMode = wfcMode;
        }
    }

    public void setWfcMode(int wfcMode) {
        setWfcMode(wfcMode, false);
    }

    public static int getWfcMode(Context context, boolean roaming) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.getWfcMode(roaming);
        }
        loge("getWfcMode: ImsManager null, returning default value.");
        return 0;
    }

    public int getWfcMode(boolean roaming) {
        int setting;
        if (!checkCarrierConfigKeyExist(this.mContext, "carrier_default_wfc_ims_roaming_mode_int").booleanValue() || !roaming) {
            if (SubscriptionManager.getIntegerSubscriptionProperty(getSubId(), "wfc_ims_mode", -1, this.mContext) == -1) {
                setting = getIntCarrierConfig("carrier_default_wfc_ims_mode_int");
            } else {
                setting = getSettingFromSubscriptionManager("wfc_ims_mode", "carrier_default_wfc_ims_mode_int");
            }
            logi("getWfcMode - setting=" + setting);
        } else {
            if (getBooleanCarrierConfig("use_wfc_home_network_mode_in_roaming_network_bool")) {
                setting = getWfcMode(false);
            } else if (!getBooleanCarrierConfig("editable_wfc_roaming_mode_bool")) {
                setting = getIntCarrierConfig("carrier_default_wfc_ims_roaming_mode_int");
            } else {
                setting = getSettingFromSubscriptionManager("wfc_ims_roaming_mode", "carrier_default_wfc_ims_roaming_mode_int");
            }
            logi("getWfcMode (roaming) - setting=" + setting);
        }
        return setting;
    }

    private int getSettingFromSubscriptionManager(String subSetting, String defaultConfigKey) {
        int result = SubscriptionManager.getIntegerSubscriptionProperty(getSubId(), subSetting, -1, this.mContext);
        if (result == -1) {
            return getIntCarrierConfig(defaultConfigKey);
        }
        return result;
    }

    public static void setWfcMode(Context context, int wfcMode, boolean roaming) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            mgr.setWfcMode(wfcMode, roaming);
        }
        loge("setWfcMode: ImsManager null, can not set value.");
    }

    public void setWfcMode(int wfcMode, boolean roaming) {
        int subId = getSubId();
        if (isSubIdValid(subId)) {
            boolean hasCust = checkCarrierConfigKeyExist(this.mContext, "carrier_default_wfc_ims_roaming_mode_int").booleanValue();
            if (!hasCust || !roaming) {
                logi("setWfcMode(i,b) - setting=" + wfcMode);
                SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_mode", Integer.toString(wfcMode));
            } else {
                logi("setWfcMode(i,b) (roaming) - setting=" + wfcMode);
                SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_roaming_mode", Integer.toString(wfcMode));
            }
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
            if ((tm != null && roaming == tm.isNetworkRoaming(subId)) || !hasCust) {
                setWfcModeInternal(wfcMode);
            }
        } else {
            loge("setWfcMode(i,b): invalid sub id, skip setting setting in siminfo db; subId=" + subId);
        }
        if (roaming == ((TelephonyManager) this.mContext.getSystemService("phone")).isNetworkRoaming(getSubId())) {
            setWfcModeInternal(wfcMode);
        }
    }

    private int getSubId() {
        int[] subIds = SubscriptionManager.getSubId(this.mPhoneId);
        if (subIds == null || subIds.length < 1) {
            return -1;
        }
        return subIds[0];
    }

    private void setWfcModeInternal(int wfcMode) {
        this.mExecutorFactory.executeRunnable(new Runnable(wfcMode) {
            /* class com.android.ims.$$Lambda$ImsManager$LiW49wt0wLMYHjgtAwL8NLIATfs */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ImsManager.this.lambda$setWfcModeInternal$1$ImsManager(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setWfcModeInternal$1$ImsManager(int value) {
        try {
            getConfigInterface().setConfig(27, value);
        } catch (ImsException e) {
        }
    }

    public static boolean isWfcRoamingEnabledByUser(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isWfcRoamingEnabledByUser();
        }
        loge("isWfcRoamingEnabledByUser: ImsManager null, returning default value.");
        return false;
    }

    public boolean isWfcRoamingEnabledByUser() {
        int setting = SubscriptionManager.getIntegerSubscriptionProperty(getSubId(), "wfc_ims_roaming_enabled", -1, this.mContext);
        if (setting == -1) {
            return getBooleanCarrierConfig("carrier_default_wfc_ims_roaming_enabled_bool");
        }
        if (setting == 1) {
            return DBG;
        }
        return false;
    }

    public static void setWfcRoamingSetting(Context context, boolean enabled) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            mgr.setWfcRoamingSetting(enabled);
        }
        loge("setWfcRoamingSetting: ImsManager null, value not set.");
    }

    public void setWfcRoamingSetting(boolean enabled) {
        SubscriptionManager.setSubscriptionProperty(getSubId(), "wfc_ims_roaming_enabled", booleanToPropertyString(enabled));
        setWfcRoamingSettingInternal(enabled);
    }

    private void setWfcRoamingSettingInternal(boolean enabled) {
        int value;
        if (enabled) {
            value = 1;
        } else {
            value = 0;
        }
        this.mExecutorFactory.executeRunnable(new Runnable(value) {
            /* class com.android.ims.$$Lambda$ImsManager$D1JuJ3ba2jMHWDKlSpm03meBR1c */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ImsManager.this.lambda$setWfcRoamingSettingInternal$2$ImsManager(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setWfcRoamingSettingInternal$2$ImsManager(int value) {
        try {
            getConfigInterface().setConfig(26, value);
        } catch (ImsException e) {
        }
    }

    public static boolean isWfcEnabledByPlatform(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            return mgr.isWfcEnabledByPlatform();
        }
        loge("isWfcEnabledByPlatform: ImsManager null, returning default value.");
        return false;
    }

    public boolean isWfcEnabledByPlatform() {
        if (SystemProperties.getInt(PROPERTY_DBG_WFC_AVAIL_OVERRIDE + Integer.toString(this.mPhoneId), -1) == 1 || SystemProperties.getInt(PROPERTY_DBG_WFC_AVAIL_OVERRIDE, -1) == 1) {
            return DBG;
        }
        if (SystemProperties.getBoolean(PROP_VOWIFI_ENABLE, false) && this.mContext.getResources().getBoolean(17891406) && getBooleanCarrierConfig("carrier_wfc_ims_available_bool") && isGbaValid()) {
            return DBG;
        }
        return false;
    }

    public boolean isSuppServicesOverUtEnabledByPlatform() {
        if (((TelephonyManager) this.mContext.getSystemService("phone")).getSimState(this.mPhoneId) == 5 && getBooleanCarrierConfig("carrier_supports_ss_over_ut_bool") && isGbaValid()) {
            return DBG;
        }
        return false;
    }

    private boolean isGbaValid() {
        boolean booleanCarrierConfig = getBooleanCarrierConfig("carrier_ims_gba_required_bool");
        boolean result = DBG;
        if (!booleanCarrierConfig) {
            return DBG;
        }
        String efIst = new TelephonyManager(this.mContext, getSubId()).getIsimIst();
        if (efIst == null) {
            loge("isGbaValid - ISF is NULL");
            return DBG;
        }
        if (efIst.length() <= 1 || (((byte) efIst.charAt(1)) & 2) == 0) {
            result = false;
        }
        logi("isGbaValid - GBA capable=" + result + ", ISF=" + efIst);
        return result;
    }

    private boolean getProvisionedBool(ImsConfig config, int item) throws ImsException {
        int value = config.getProvisionedValue(item);
        if (value == -1) {
            throw new ImsException("getProvisionedBool failed with error for item: " + item, 103);
        } else if (value == 1) {
            return DBG;
        } else {
            return false;
        }
    }

    private void setProvisionedBool(ImsConfig config, int item, int value) throws ImsException {
        if (config.setConfig(item, value) != 0) {
            throw new ImsException("setProvisionedBool failed with error for item: " + item, 103);
        }
    }

    private boolean getProvisionedBoolNoException(int item) {
        try {
            return getProvisionedBool(getConfigInterface(), item);
        } catch (ImsException ex) {
            Log.w(TAG, "getProvisionedBoolNoException: operation failed for item=" + item + ". Exception:" + ex.getMessage() + ". Returning false.");
            return false;
        }
    }

    private boolean setProvisionedBoolNoException(int item, int value) {
        try {
            setProvisionedBool(getConfigInterface(), item, value);
            return DBG;
        } catch (ImsException ex) {
            Log.w(TAG, "setProvisionedBoolNoException: operation failed for item=" + item + ", value=" + value + ". Exception:" + ex.getMessage());
            return false;
        }
    }

    public static void updateImsServiceConfig(Context context, int phoneId, boolean force) {
        ImsManager mgr = getInstance(context, phoneId);
        if (mgr != null) {
            mgr.updateImsServiceConfig(force);
        }
        loge("updateImsServiceConfig: ImsManager null, returning without update.");
    }

    public void updateImsServiceConfig(boolean force) {
        if (!force && new TelephonyManager(this.mContext, getSubId()).getSimState() != 5) {
            logi("updateImsServiceConfig: SIM not ready");
        } else if (!this.mConfigUpdated || force) {
            try {
                CapabilityChangeRequest request = new CapabilityChangeRequest();
                updateVolteFeatureValue(request);
                updateWfcFeatureAndProvisionedValues(request);
                updateVideoCallFeatureValue(request);
                boolean isImsNeededForRtt = updateRttConfigValue();
                updateUtFeatureValue(request);
                changeMmTelCapability(request);
                if (!isImsNeededForRtt && isTurnOffImsAllowedByPlatform()) {
                    if (!isImsNeeded(request)) {
                        logi("updateImsServiceConfig: turnOffIms");
                        turnOffIms();
                        this.mConfigUpdated = DBG;
                    }
                }
                logi("updateImsServiceConfig: turnOnIms");
                turnOnIms();
                this.mConfigUpdated = DBG;
            } catch (ImsException e) {
                loge("updateImsServiceConfig: ", e);
                this.mConfigUpdated = false;
            }
        }
    }

    private boolean isImsNeeded(CapabilityChangeRequest r) {
        return r.getCapabilitiesToEnable().stream().anyMatch($$Lambda$ImsManager$YhRaDrc3t9_7beNiU5gQcqZilOw.INSTANCE);
    }

    static /* synthetic */ boolean lambda$isImsNeeded$3(CapabilityChangeRequest.CapabilityPair c) {
        if (c.getCapability() != 4) {
            return DBG;
        }
        return false;
    }

    private void updateVolteFeatureValue(CapabilityChangeRequest request) {
        boolean available = isVolteEnabledByPlatform();
        boolean enabled = isEnhanced4gLteModeSettingEnabledByUser();
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled();
        boolean isFeatureOn = available && enabled && isNonTty;
        logi("updateVolteFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty);
        if (isFeatureOn) {
            request.addCapabilitiesToEnableForTech(1, 0);
        } else {
            request.addCapabilitiesToDisableForTech(1, 0);
        }
    }

    private void updateVideoCallFeatureValue(CapabilityChangeRequest request) {
        boolean available = isVtEnabledByPlatform();
        boolean enabled = isVtEnabledByUser();
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled();
        boolean isDataEnabled = isDataEnabled();
        boolean isFeatureOn = (!available || !enabled || !isNonTty || (!getBooleanCarrierConfig("ignore_data_enabled_changed_for_video_calls") && !isDataEnabled)) ? false : DBG;
        logi("updateVideoCallFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty + ", data enabled = " + isDataEnabled);
        if (isFeatureOn) {
            request.addCapabilitiesToEnableForTech(2, 0);
        } else {
            request.addCapabilitiesToDisableForTech(2, 0);
        }
    }

    private void updateWfcFeatureAndProvisionedValues(CapabilityChangeRequest request) {
        boolean isNetworkRoaming = new TelephonyManager(this.mContext, getSubId()).isNetworkRoaming();
        boolean available = isWfcEnabledByPlatform();
        boolean enabled = isWfcEnabledByUser();
        int mode = getWfcMode(isNetworkRoaming);
        boolean roaming = isWfcRoamingEnabledByUser();
        boolean isFeatureOn = available && enabled;
        logi("updateWfcFeatureAndProvisionedValues: available = " + available + ", enabled = " + enabled + ", mode = " + mode + ", roaming = " + roaming);
        if (isFeatureOn) {
            request.addCapabilitiesToEnableForTech(1, 1);
        } else {
            request.addCapabilitiesToDisableForTech(1, 1);
        }
        if (!isFeatureOn) {
            mode = 1;
            roaming = false;
        }
        setWfcModeInternal(mode);
        setWfcRoamingSettingInternal(roaming);
    }

    private void updateUtFeatureValue(CapabilityChangeRequest request) {
        ITelephony telephony;
        boolean isCarrierSupported = isSuppServicesOverUtEnabledByPlatform();
        boolean requiresProvisioning = getBooleanCarrierConfig("carrier_ut_provisioning_required_bool");
        boolean isProvisioned = DBG;
        if (requiresProvisioning && (telephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"))) != null) {
            try {
                isProvisioned = telephony.isMmTelCapabilityProvisionedInCache(getSubId(), 4, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "updateUtFeatureValue: couldn't reach telephony! returning provisioned");
            }
        }
        boolean isFeatureOn = (!isCarrierSupported || !isProvisioned) ? false : DBG;
        logi("updateUtFeatureValue: available = " + isCarrierSupported + ", isProvisioned = " + isProvisioned + ", enabled = " + isFeatureOn);
        if (isFeatureOn) {
            request.addCapabilitiesToEnableForTech(4, 0);
        } else {
            request.addCapabilitiesToDisableForTech(4, 0);
        }
    }

    @VisibleForTesting
    public ImsManager(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mConfigDynamicBind = this.mContext.getResources().getBoolean(17891429);
        this.mConfigManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        createImsService();
        this.mHwCustImsManager = (HwCustImsManager) HwCustUtils.createObj(HwCustImsManager.class, new Object[]{this.mContext, Integer.valueOf(this.mPhoneId)});
    }

    public boolean isDynamicBinding() {
        return this.mConfigDynamicBind;
    }

    public boolean isServiceAvailable() {
        connectIfServiceIsAvailable();
        logi("isServiceAvailable: end mMmTelFeatureConnection=" + this.mMmTelFeatureConnection + ",mMmTelFeatureConnection.isBinderAlive()=" + this.mMmTelFeatureConnection.isBinderAlive());
        return this.mMmTelFeatureConnection.isBinderAlive();
    }

    public boolean isServiceReady() {
        connectIfServiceIsAvailable();
        return this.mMmTelFeatureConnection.isBinderReady();
    }

    public void connectIfServiceIsAvailable() {
        MmTelFeatureConnection mmTelFeatureConnection = this.mMmTelFeatureConnection;
        if (mmTelFeatureConnection == null || !mmTelFeatureConnection.isBinderAlive()) {
            createImsService();
        }
    }

    public void setConfigListener(ImsConfigListener listener) {
        this.mImsConfigListener = listener;
    }

    @VisibleForTesting
    public void addNotifyStatusChangedCallbackIfAvailable(MmTelFeatureConnection.IFeatureUpdate c) throws ImsException {
        if (!this.mMmTelFeatureConnection.isBinderAlive()) {
            throw new ImsException("Binder is not active!", 106);
        } else if (c != null) {
            this.mStatusCallbacks.add(c);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeNotifyStatusChangedCallback(MmTelFeatureConnection.IFeatureUpdate c) {
        if (c != null) {
            this.mStatusCallbacks.remove(c);
        } else {
            Log.w(TAG, "removeNotifyStatusChangedCallback: callback is null!");
        }
    }

    public void open(MmTelFeature.Listener listener) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        if (listener != null) {
            try {
                this.mMmTelFeatureConnection.openConnection(listener);
            } catch (RemoteException e) {
                throw new ImsException("open()", e, 106);
            }
        } else {
            throw new NullPointerException("listener can't be null");
        }
    }

    public void addRegistrationListener(int serviceClass, ImsConnectionStateListener listener) throws ImsException {
        addRegistrationListener(listener);
    }

    public void addRegistrationListener(final ImsConnectionStateListener listener) throws ImsException {
        if (listener != null) {
            addRegistrationCallback(listener);
            addCapabilitiesCallback(new ImsMmTelManager.CapabilityCallback() {
                /* class com.android.ims.ImsManager.AnonymousClass1 */

                @Override // android.telephony.ims.ImsMmTelManager.CapabilityCallback
                public void onCapabilitiesStatusChanged(MmTelFeature.MmTelCapabilities capabilities) {
                    listener.onFeatureCapabilityChangedAdapter(ImsManager.this.getRegistrationTech(), capabilities);
                }
            });
            logi("Registration Callback registered.");
            return;
        }
        throw new NullPointerException("listener can't be null");
    }

    public void addRegistrationCallback(ImsMmTelManager.RegistrationCallback callback) throws ImsException {
        if (callback != null) {
            try {
                callback.setExecutor(getThreadExecutor());
                this.mMmTelFeatureConnection.addRegistrationCallback(callback.getBinder());
                logi("Registration Callback registered.");
            } catch (IllegalStateException e) {
                throw new ImsException("addRegistrationCallback(IRIB)", e, 106);
            }
        } else {
            throw new NullPointerException("registration callback can't be null");
        }
    }

    public void removeRegistrationListener(ImsMmTelManager.RegistrationCallback callback) {
        if (callback != null) {
            this.mMmTelFeatureConnection.removeRegistrationCallback(callback.getBinder());
            logi("Registration callback removed.");
            return;
        }
        throw new NullPointerException("registration callback can't be null");
    }

    public void addRegistrationCallbackForSubscription(IImsRegistrationCallback callback, int subId) throws RemoteException {
        if (callback != null) {
            this.mMmTelFeatureConnection.addRegistrationCallbackForSubscription(callback, subId);
            logi("Registration Callback registered.");
            return;
        }
        throw new IllegalArgumentException("registration callback can't be null");
    }

    public void removeRegistrationCallbackForSubscription(IImsRegistrationCallback callback, int subId) {
        if (callback != null) {
            this.mMmTelFeatureConnection.removeRegistrationCallbackForSubscription(callback, subId);
            return;
        }
        throw new IllegalArgumentException("registration callback can't be null");
    }

    public void addCapabilitiesCallback(ImsMmTelManager.CapabilityCallback callback) throws ImsException {
        if (callback != null) {
            checkAndThrowExceptionIfServiceUnavailable();
            try {
                callback.setExecutor(getThreadExecutor());
                this.mMmTelFeatureConnection.addCapabilityCallback(callback.getBinder());
                logi("Capability Callback registered.");
            } catch (IllegalStateException e) {
                throw new ImsException("addCapabilitiesCallback(IF)", e, 106);
            }
        } else {
            throw new NullPointerException("capabilities callback can't be null");
        }
    }

    public void removeCapabilitiesCallback(ImsMmTelManager.CapabilityCallback callback) throws ImsException {
        if (callback != null) {
            checkAndThrowExceptionIfServiceUnavailable();
            this.mMmTelFeatureConnection.removeCapabilityCallback(callback.getBinder());
            return;
        }
        throw new NullPointerException("capabilities callback can't be null");
    }

    public void addCapabilitiesCallbackForSubscription(IImsCapabilityCallback callback, int subId) throws RemoteException {
        if (callback != null) {
            this.mMmTelFeatureConnection.addCapabilityCallbackForSubscription(callback, subId);
            logi("Capability Callback registered for subscription.");
            return;
        }
        throw new IllegalArgumentException("registration callback can't be null");
    }

    public void removeCapabilitiesCallbackForSubscription(IImsCapabilityCallback callback, int subId) {
        if (callback != null) {
            this.mMmTelFeatureConnection.removeCapabilityCallbackForSubscription(callback, subId);
            return;
        }
        throw new IllegalArgumentException("capabilities callback can't be null");
    }

    public void removeRegistrationListener(ImsConnectionStateListener listener) throws ImsException {
        if (listener != null) {
            checkAndThrowExceptionIfServiceUnavailable();
            this.mMmTelFeatureConnection.removeRegistrationCallback(listener.getBinder());
            logi("Registration Callback/Listener registered.");
            return;
        }
        throw new NullPointerException("listener can't be null");
    }

    public void addProvisioningCallbackForSubscription(IImsConfigCallback callback, int subId) {
        if (callback != null) {
            this.mMmTelFeatureConnection.addProvisioningCallbackForSubscription(callback, subId);
            logi("Capability Callback registered for subscription.");
            return;
        }
        throw new IllegalArgumentException("provisioning callback can't be null");
    }

    public void removeProvisioningCallbackForSubscription(IImsConfigCallback callback, int subId) {
        if (callback != null) {
            this.mMmTelFeatureConnection.removeProvisioningCallbackForSubscription(callback, subId);
            return;
        }
        throw new IllegalArgumentException("provisioning callback can't be null");
    }

    public int getRegistrationTech() {
        try {
            return this.mMmTelFeatureConnection.getRegistrationTech();
        } catch (RemoteException e) {
            Log.w(TAG, "getRegistrationTech: no connection to ImsService.");
            return -1;
        }
    }

    public void close() {
        MmTelFeatureConnection mmTelFeatureConnection = this.mMmTelFeatureConnection;
        if (mmTelFeatureConnection != null) {
            mmTelFeatureConnection.closeConnection();
        }
        this.mUt = null;
        this.mEcbm = null;
        this.mMultiEndpoint = null;
    }

    public ImsUtInterface getSupplementaryServiceConfiguration() throws ImsException {
        ImsUt imsUt = this.mUt;
        if (imsUt != null && imsUt.isBinderAlive()) {
            return this.mUt;
        }
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            IImsUt iUt = this.mMmTelFeatureConnection.getUtInterface();
            if (iUt != null) {
                logi("getSupplementaryServiceConfiguration: iUt = " + iUt + ", mPhoneId = " + this.mPhoneId);
                this.mUt = new ImsUt(iUt, this.mPhoneId);
                return this.mUt;
            }
            throw new ImsException("getSupplementaryServiceConfiguration()", 801);
        } catch (RemoteException e) {
            throw new ImsException("getSupplementaryServiceConfiguration()", e, 106);
        }
    }

    public ImsCallProfile createCallProfile(int serviceType, int callType) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mMmTelFeatureConnection.createCallProfile(serviceType, callType);
        } catch (RemoteException e) {
            throw new ImsException("createCallProfile()", e, 106);
        }
    }

    public ImsCall makeCall(ImsCallProfile profile, String[] callees, ImsCall.Listener listener) throws ImsException {
        logi("makeCall :: profile=" + profile);
        checkAndThrowExceptionIfServiceUnavailable();
        ImsCall call = new ImsCall(this.mContext, profile);
        call.setListener(listener);
        ImsCallSession session = createCallSession(profile);
        if (profile.getCallExtraBoolean("isConferenceUri", false) || callees == null || callees.length != 1) {
            call.start(session, callees);
        } else {
            call.start(session, callees[0]);
        }
        return call;
    }

    public ImsCall takeCall(IImsCallSession session, Bundle incomingCallExtras, ImsCall.Listener listener) throws ImsException {
        logi("takeCall :: incomingCall");
        checkAndThrowExceptionIfServiceUnavailable();
        if (incomingCallExtras == null) {
            throw new ImsException("Can't retrieve session with null intent", (int) INCOMING_CALL_RESULT_CODE);
        } else if (getCallId(incomingCallExtras) == null) {
            throw new ImsException("Call ID missing in the incoming call intent", (int) INCOMING_CALL_RESULT_CODE);
        } else if (session != null) {
            try {
                ImsCall call = new ImsCall(this.mContext, session.getCallProfile());
                call.attachSession(new ImsCallSession(session));
                call.setListener(listener);
                return call;
            } catch (Throwable t) {
                throw new ImsException("takeCall()", t, 0);
            }
        } else {
            throw new ImsException("No pending session for the call", 107);
        }
    }

    public ImsConfig getConfigInterface() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        IImsConfig config = this.mMmTelFeatureConnection.getConfigInterface();
        if (config != null) {
            return new ImsConfig(config);
        }
        throw new ImsException("getConfigInterface()", 131);
    }

    public void changeMmTelCapability(int capability, int radioTech, boolean isEnabled) throws ImsException {
        CapabilityChangeRequest request = new CapabilityChangeRequest();
        if (isEnabled) {
            request.addCapabilitiesToEnableForTech(capability, radioTech);
        } else {
            request.addCapabilitiesToDisableForTech(capability, radioTech);
        }
        changeMmTelCapability(request);
    }

    public void changeMmTelCapability(CapabilityChangeRequest r) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            Log.i(TAG, "changeMmTelCapability: changing capabilities for sub: " + getSubId() + ", request: " + r);
            this.mMmTelFeatureConnection.changeEnabledCapabilities(r, null);
            if (this.mImsConfigListener != null) {
                for (CapabilityChangeRequest.CapabilityPair enabledCaps : r.getCapabilitiesToEnable()) {
                    this.mImsConfigListener.onSetFeatureResponse(enabledCaps.getCapability(), enabledCaps.getRadioTech(), 1, -1);
                }
                for (CapabilityChangeRequest.CapabilityPair disabledCaps : r.getCapabilitiesToDisable()) {
                    this.mImsConfigListener.onSetFeatureResponse(disabledCaps.getCapability(), disabledCaps.getRadioTech(), 0, -1);
                }
            }
        } catch (RemoteException e) {
            throw new ImsException("changeMmTelCapability(CCR)", e, 106);
        }
    }

    public boolean updateRttConfigValue() {
        boolean isCarrierSupported = getBooleanCarrierConfig("rtt_supported_bool");
        boolean isRttEnabled = Settings.Secure.getInt(this.mContext.getContentResolver(), "rtt_calling_mode", 0) != 0;
        String simpleName = ImsManager.class.getSimpleName();
        Log.i(simpleName, "update RTT value " + isRttEnabled);
        if (isCarrierSupported) {
            setRttConfig(isRttEnabled);
        }
        if (!isCarrierSupported || !isRttEnabled) {
            return false;
        }
        return DBG;
    }

    private void setRttConfig(boolean enabled) {
        int value;
        if (enabled) {
            value = 1;
        } else {
            value = 0;
        }
        this.mExecutorFactory.executeRunnable(new Runnable(enabled, value) {
            /* class com.android.ims.$$Lambda$ImsManager$_6YCQyhjHBSdrm4ZBEMUQ2AAqOY */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ImsManager.this.lambda$setRttConfig$4$ImsManager(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setRttConfig$4$ImsManager(boolean enabled, int value) {
        try {
            String simpleName = ImsManager.class.getSimpleName();
            Log.i(simpleName, "Setting RTT enabled to " + enabled);
            getConfigInterface().setProvisionedValue(66, value);
        } catch (ImsException e) {
            String simpleName2 = ImsManager.class.getSimpleName();
            Log.e(simpleName2, "Unable to set RTT value enabled to " + enabled + ": " + e);
        }
    }

    public boolean queryMmTelCapability(final int capability, final int radioTech) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        final BlockingQueue<Boolean> result = new LinkedBlockingDeque<>(1);
        try {
            this.mMmTelFeatureConnection.queryEnabledCapabilities(capability, radioTech, new IImsCapabilityCallback.Stub() {
                /* class com.android.ims.ImsManager.AnonymousClass2 */

                public void onQueryCapabilityConfiguration(int resCap, int resTech, boolean enabled) {
                    if (resCap == capability && resTech == radioTech) {
                        result.offer(Boolean.valueOf(enabled));
                    }
                }

                public void onChangeCapabilityConfigurationError(int capability, int radioTech, int reason) {
                }

                public void onCapabilitiesStatusChanged(int config) {
                }
            });
            try {
                Boolean pollResult = result.poll(3000, TimeUnit.MILLISECONDS);
                if (pollResult == null) {
                    return false;
                }
                return pollResult.booleanValue();
            } catch (InterruptedException e) {
                Log.w(TAG, "queryMmTelCapability: interrupted while waiting for response");
                return false;
            }
        } catch (RemoteException e2) {
            throw new ImsException("queryMmTelCapability()", e2, 106);
        }
    }

    public void setRttEnabled(boolean enabled) {
        boolean z;
        if (enabled) {
            try {
                setEnhanced4gLteModeSetting(enabled);
            } catch (ImsException e) {
                String simpleName = ImsManager.class.getSimpleName();
                Log.e(simpleName, "Unable to set RTT enabled to " + enabled + ": " + e);
                return;
            }
        } else {
            if (!enabled) {
                if (!isEnhanced4gLteModeSettingEnabledByUser()) {
                    z = false;
                    setAdvanced4GMode(z);
                }
            }
            z = DBG;
            setAdvanced4GMode(z);
        }
        setRttConfig(enabled);
    }

    public void setTtyMode(int ttyMode) throws ImsException {
        if (!getBooleanCarrierConfig("carrier_volte_tty_supported_bool")) {
            setAdvanced4GMode((ttyMode != 0 || !isEnhanced4gLteModeSettingEnabledByUser()) ? false : DBG);
        }
    }

    public void setUiTTYMode(Context context, int uiTtyMode, Message onComplete) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mMmTelFeatureConnection.setUiTTYMode(uiTtyMode, onComplete);
        } catch (RemoteException e) {
            throw new ImsException("setTTYMode()", e, 106);
        }
    }

    private ImsReasonInfo makeACopy(ImsReasonInfo imsReasonInfo) {
        Parcel p = Parcel.obtain();
        imsReasonInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        ImsReasonInfo clonedReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(p);
        p.recycle();
        return clonedReasonInfo;
    }

    public ArrayList<ImsReasonInfo> getRecentImsDisconnectReasons() {
        ArrayList<ImsReasonInfo> disconnectReasons = new ArrayList<>();
        Iterator<ImsReasonInfo> it = this.mRecentDisconnectReasons.iterator();
        while (it.hasNext()) {
            disconnectReasons.add(makeACopy(it.next()));
        }
        return disconnectReasons;
    }

    public int getImsServiceState() throws ImsException {
        return this.mMmTelFeatureConnection.getFeatureState();
    }

    private Executor getThreadExecutor() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        return new HandlerExecutor(new Handler(Looper.myLooper()));
    }

    private boolean getBooleanCarrierConfig(String key) {
        PersistableBundle b = null;
        CarrierConfigManager carrierConfigManager = this.mConfigManager;
        if (carrierConfigManager != null) {
            b = carrierConfigManager.getConfigForSubId(getSubId());
        }
        if (b != null) {
            return b.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }

    private int getIntCarrierConfig(String key) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(getSubId());
        }
        if (b != null) {
            return b.getInt(key);
        }
        return CarrierConfigManager.getDefaultConfig().getInt(key);
    }

    private Boolean checkCarrierConfigKeyExist(Context context, String key) {
        Boolean ifExist = false;
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(getSubId());
        }
        if (!(b == null || b.get(key) == null)) {
            ifExist = Boolean.valueOf((boolean) DBG);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("carrierConfig key[");
        sb.append(key);
        sb.append("] ");
        sb.append(ifExist.booleanValue() ? "exists" : "does not exist");
        logi(sb.toString());
        return ifExist;
    }

    private static String getCallId(Bundle incomingCallExtras) {
        if (incomingCallExtras == null) {
            return null;
        }
        return incomingCallExtras.getString(EXTRA_CALL_ID);
    }

    private void checkAndThrowExceptionIfServiceUnavailable() throws ImsException {
        if (isImsSupportedOnDevice(this.mContext)) {
            MmTelFeatureConnection mmTelFeatureConnection = this.mMmTelFeatureConnection;
            if (mmTelFeatureConnection == null || !mmTelFeatureConnection.isBinderAlive()) {
                createImsService();
                if (this.mMmTelFeatureConnection == null) {
                    throw new ImsException("Service is unavailable", 106);
                }
                return;
            }
            return;
        }
        throw new ImsException("IMS not supported on device.", 150);
    }

    private void createImsService() {
        this.mMmTelFeatureConnection = MmTelFeatureConnection.create(this.mContext, this.mPhoneId);
        this.mMmTelFeatureConnection.setStatusCallback(new MmTelFeatureConnection.IFeatureUpdate() {
            /* class com.android.ims.ImsManager.AnonymousClass3 */

            @Override // com.android.ims.MmTelFeatureConnection.IFeatureUpdate
            public void notifyStateChanged() {
                ImsManager.this.mStatusCallbacks.forEach($$Lambda$a4IO_gY853vtN_bjQR9bZYk4Js0.INSTANCE);
            }

            @Override // com.android.ims.MmTelFeatureConnection.IFeatureUpdate
            public void notifyUnavailable() {
                ImsManager.this.mStatusCallbacks.forEach($$Lambda$VPAygt3Ycyud4AweDbrpru2LJ8.INSTANCE);
            }
        });
    }

    private ImsCallSession createCallSession(ImsCallProfile profile) throws ImsException {
        try {
            return new ImsCallSession(this.mMmTelFeatureConnection.createCallSession(profile));
        } catch (RemoteException e) {
            Rlog.w(TAG, "CreateCallSession: Error, remote exception: " + e.getMessage());
            throw new ImsException("createCallSession()", e, 106);
        }
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void logi(String s) {
        Rlog.i(TAG, s);
    }

    private static void loge(String s) {
        Rlog.e(TAG, s);
    }

    private static void loge(String s, Throwable t) {
        Rlog.e(TAG, s, t);
    }

    private void turnOnIms() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        HwCustImsManager hwCustImsManager = this.mHwCustImsManager;
        if (hwCustImsManager == null || !hwCustImsManager.shouldNotTurnOnImsForCust()) {
            ((TelephonyManager) this.mContext.getSystemService("phone")).enableIms(this.mPhoneId);
        } else {
            logi("turnOnIms: Not Turn On Ims for customization since Enhanced LTE Service is off, return.");
        }
    }

    private boolean isImsTurnOffAllowed() {
        HwCustImsManager hwCustImsManager = this.mHwCustImsManager;
        if (hwCustImsManager != null && hwCustImsManager.isImsTurnOffAllowedForCust()) {
            return isTurnOffImsAllowedByPlatform();
        }
        if (!isTurnOffImsAllowedByPlatform() || (isWfcEnabledByPlatform() && isWfcEnabledByUser())) {
            return false;
        }
        return DBG;
    }

    private void setLteFeatureValues(boolean turnOn) {
        logi("setLteFeatureValues: " + turnOn);
        CapabilityChangeRequest request = new CapabilityChangeRequest();
        boolean enableViLte = DBG;
        if (turnOn) {
            request.addCapabilitiesToEnableForTech(1, 0);
        } else {
            request.addCapabilitiesToDisableForTech(1, 0);
        }
        if (isVtEnabledByPlatform()) {
            boolean ignoreDataEnabledChanged = getBooleanCarrierConfig("ignore_data_enabled_changed_for_video_calls");
            if (!turnOn || !isVtEnabledByUser() || (!ignoreDataEnabledChanged && !isDataEnabled())) {
                enableViLte = false;
            }
            if (enableViLte) {
                request.addCapabilitiesToEnableForTech(2, 0);
            } else {
                request.addCapabilitiesToDisableForTech(2, 0);
            }
        }
        try {
            if (this.mHwCustImsManager != null) {
                this.mHwCustImsManager.changeMmTelCapWithWfcForCust(turnOn);
            }
            this.mMmTelFeatureConnection.changeEnabledCapabilities(request, null);
        } catch (RemoteException e) {
            Log.e(TAG, "setLteFeatureValues: Exception: " + e.getMessage());
        }
    }

    private void setAdvanced4GMode(boolean turnOn) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        if (turnOn) {
            setLteFeatureValues(turnOn);
            logi("setAdvanced4GMode: turnOnIms");
            turnOnIms();
            return;
        }
        if (isImsTurnOffAllowed()) {
            logi("setAdvanced4GMode: turnOffIms");
            turnOffIms();
        }
        setLteFeatureValues(turnOn);
    }

    private void turnOffIms() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        HwCustImsManager hwCustImsManager = this.mHwCustImsManager;
        if (hwCustImsManager == null || !hwCustImsManager.shouldNotTurnOffImsForCust()) {
            ((TelephonyManager) this.mContext.getSystemService("phone")).disableIms(this.mPhoneId);
        } else {
            logi("turnOffIms: Not Turn Off Ims for customization since Enhanced LTE Service is on, return.");
        }
    }

    private void addToRecentDisconnectReasons(ImsReasonInfo reason) {
        if (reason != null) {
            while (this.mRecentDisconnectReasons.size() >= MAX_RECENT_DISCONNECT_REASONS) {
                this.mRecentDisconnectReasons.removeFirst();
            }
            this.mRecentDisconnectReasons.addLast(reason);
        }
    }

    public ImsEcbm getEcbmInterface() throws ImsException {
        ImsEcbm imsEcbm = this.mEcbm;
        if (imsEcbm != null && imsEcbm.isBinderAlive()) {
            return this.mEcbm;
        }
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            IImsEcbm iEcbm = this.mMmTelFeatureConnection.getEcbmInterface();
            if (iEcbm != null) {
                this.mEcbm = new ImsEcbm(iEcbm);
                return this.mEcbm;
            }
            throw new ImsException("getEcbmInterface()", 901);
        } catch (RemoteException e) {
            throw new ImsException("getEcbmInterface()", e, 106);
        }
    }

    public void sendSms(int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) throws ImsException {
        try {
            this.mMmTelFeatureConnection.sendSms(token, messageRef, format, smsc, isRetry, pdu);
        } catch (RemoteException e) {
            throw new ImsException("sendSms()", e, 106);
        }
    }

    public void acknowledgeSms(int token, int messageRef, int result) throws ImsException {
        try {
            this.mMmTelFeatureConnection.acknowledgeSms(token, messageRef, result);
        } catch (RemoteException e) {
            throw new ImsException("acknowledgeSms()", e, 106);
        }
    }

    public void acknowledgeSmsReport(int token, int messageRef, int result) throws ImsException {
        try {
            this.mMmTelFeatureConnection.acknowledgeSmsReport(token, messageRef, result);
        } catch (RemoteException e) {
            throw new ImsException("acknowledgeSmsReport()", e, 106);
        }
    }

    public String getSmsFormat() throws ImsException {
        try {
            return this.mMmTelFeatureConnection.getSmsFormat();
        } catch (RemoteException e) {
            throw new ImsException("getSmsFormat()", e, 106);
        }
    }

    public void setSmsListener(IImsSmsListener listener) throws ImsException {
        try {
            this.mMmTelFeatureConnection.setSmsListener(listener);
        } catch (RemoteException e) {
            throw new ImsException("setSmsListener()", e, 106);
        }
    }

    public void onSmsReady() throws ImsException {
        try {
            this.mMmTelFeatureConnection.onSmsReady();
        } catch (RemoteException e) {
            throw new ImsException("onSmsReady()", e, 106);
        }
    }

    public int shouldProcessCall(boolean isEmergency, String[] numbers) throws ImsException {
        try {
            return this.mMmTelFeatureConnection.shouldProcessCall(isEmergency, numbers);
        } catch (RemoteException e) {
            throw new ImsException("shouldProcessCall()", e, 106);
        }
    }

    public MmTelFeatureConnection getImsServiceProxy() {
        return this.mMmTelFeatureConnection;
    }

    public void createImsServiceProxy(MmTelFeatureConnection imsServiceProxy) {
        this.mMmTelFeatureConnection = imsServiceProxy;
    }

    public ImsMultiEndpoint getMultiEndpointInterface() throws ImsException {
        ImsMultiEndpoint imsMultiEndpoint = this.mMultiEndpoint;
        if (imsMultiEndpoint != null && imsMultiEndpoint.isBinderAlive()) {
            return this.mMultiEndpoint;
        }
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            IImsMultiEndpoint iImsMultiEndpoint = this.mMmTelFeatureConnection.getMultiEndpointInterface();
            if (iImsMultiEndpoint != null) {
                this.mMultiEndpoint = new ImsMultiEndpoint(iImsMultiEndpoint);
                return this.mMultiEndpoint;
            }
            throw new ImsException("getMultiEndpointInterface()", 902);
        } catch (RemoteException e) {
            throw new ImsException("getMultiEndpointInterface()", e, 106);
        }
    }

    public static void factoryReset(Context context) {
        ImsManager mgr = getInstance(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (mgr != null) {
            mgr.factoryReset();
        }
        loge("factoryReset: ImsManager null.");
    }

    public void factoryReset() {
        int subId = getSubId();
        if (isSubIdValid(subId)) {
            SubscriptionManager.setSubscriptionProperty(subId, "volte_vt_enabled", booleanToPropertyString(getBooleanCarrierConfig("enhanced_4g_lte_on_by_default_bool")));
            SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_enabled", booleanToPropertyString(getBooleanCarrierConfig("carrier_default_wfc_ims_enabled_bool")));
            SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_mode", Integer.toString(getIntCarrierConfig("carrier_default_wfc_ims_mode_int")));
            SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_roaming_enabled", booleanToPropertyString(getBooleanCarrierConfig("carrier_default_wfc_ims_roaming_enabled_bool")));
            SubscriptionManager.setSubscriptionProperty(subId, "vt_ims_enabled", booleanToPropertyString(DBG));
        } else {
            loge("factoryReset: invalid sub id, can not reset siminfo db settings; subId=" + subId);
        }
        updateImsServiceConfig(DBG);
    }

    public void setVolteProvisioned(boolean isProvisioned) {
        int provisionStatus;
        if (isProvisioned) {
            provisionStatus = 1;
        } else {
            provisionStatus = 0;
        }
        setProvisionedBoolNoException(10, provisionStatus);
    }

    public void setWfcProvisioned(boolean isProvisioned) {
        int provisionStatus;
        if (isProvisioned) {
            provisionStatus = 1;
        } else {
            provisionStatus = 0;
        }
        setProvisionedBoolNoException(28, provisionStatus);
    }

    public void setVtProvisioned(boolean isProvisioned) {
        int provisionStatus;
        if (isProvisioned) {
            provisionStatus = 1;
        } else {
            provisionStatus = 0;
        }
        setProvisionedBoolNoException(11, provisionStatus);
    }

    private boolean isDataEnabled() {
        return new TelephonyManager(this.mContext, getSubId()).isDataCapable();
    }

    private boolean isVolteProvisioned() {
        return getProvisionedBoolNoException(10);
    }

    private boolean isWfcProvisioned() {
        return getProvisionedBoolNoException(28);
    }

    private boolean isVtProvisioned() {
        return getProvisionedBoolNoException(11);
    }

    private static String booleanToPropertyString(boolean bool) {
        return bool ? "1" : "0";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ImsManager:");
        pw.println("  device supports IMS = " + isImsSupportedOnDevice(this.mContext));
        pw.println("  mPhoneId = " + this.mPhoneId);
        pw.println("  mConfigUpdated = " + this.mConfigUpdated);
        pw.println("  mImsServiceProxy = " + this.mMmTelFeatureConnection);
        pw.println("  mDataEnabled = " + isDataEnabled());
        pw.println("  ignoreDataEnabledChanged = " + getBooleanCarrierConfig("ignore_data_enabled_changed_for_video_calls"));
        pw.println("  isGbaValid = " + isGbaValid());
        pw.println("  isImsTurnOffAllowed = " + isImsTurnOffAllowed());
        pw.println("  isNonTtyOrTtyOnVolteEnabled = " + isNonTtyOrTtyOnVolteEnabled());
        pw.println("  isVolteEnabledByPlatform = " + isVolteEnabledByPlatform());
        pw.println("  isVolteProvisionedOnDevice = " + isVolteProvisionedOnDevice());
        pw.println("  isEnhanced4gLteModeSettingEnabledByUser = " + isEnhanced4gLteModeSettingEnabledByUser());
        pw.println("  isVtEnabledByPlatform = " + isVtEnabledByPlatform());
        pw.println("  isVtEnabledByUser = " + isVtEnabledByUser());
        pw.println("  isWfcEnabledByPlatform = " + isWfcEnabledByPlatform());
        pw.println("  isWfcEnabledByUser = " + isWfcEnabledByUser());
        pw.println("  getWfcMode = " + getWfcMode());
        pw.println("  isWfcRoamingEnabledByUser = " + isWfcRoamingEnabledByUser());
        pw.println("  isVtProvisionedOnDevice = " + isVtProvisionedOnDevice());
        pw.println("  isWfcProvisionedOnDevice = " + isWfcProvisionedOnDevice());
        pw.flush();
    }

    private boolean isSubIdValid(int subId) {
        if (!SubscriptionManager.isValidSubscriptionId(subId) || subId == Integer.MAX_VALUE) {
            return false;
        }
        return DBG;
    }
}
