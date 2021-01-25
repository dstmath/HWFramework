package com.android.server.devicepolicy;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.NotificationManager;
import android.app.PackageInstallObserver;
import android.app.StatusBarManager;
import android.app.SynchronousUserSwitchObserver;
import android.app.admin.DevicePolicyManager;
import android.app.backup.BackupManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.security.KeyChain;
import android.security.KeyStore;
import android.telecom.TelecomManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.net.VpnProfile;
import com.android.internal.telephony.ITelephony;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.devicepolicy.HwDevicePolicyManagerInnerEx;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.plugins.DeviceApplicationPlugin;
import com.android.server.devicepolicy.plugins.DeviceBluetoothPlugin;
import com.android.server.devicepolicy.plugins.DeviceCameraPlugin;
import com.android.server.devicepolicy.plugins.DeviceControlPlugin;
import com.android.server.devicepolicy.plugins.DeviceFirewallManagerImpl;
import com.android.server.devicepolicy.plugins.DeviceInfraredPlugin;
import com.android.server.devicepolicy.plugins.DeviceLocationPlugin;
import com.android.server.devicepolicy.plugins.DeviceNetworkPlugin;
import com.android.server.devicepolicy.plugins.DeviceP2PPlugin;
import com.android.server.devicepolicy.plugins.DevicePackageManagerPlugin;
import com.android.server.devicepolicy.plugins.DevicePasswordPlugin;
import com.android.server.devicepolicy.plugins.DeviceRestrictionPlugin;
import com.android.server.devicepolicy.plugins.DeviceStorageManagerPlugin;
import com.android.server.devicepolicy.plugins.DeviceTelephonyPlugin;
import com.android.server.devicepolicy.plugins.DeviceVpnManagerImpl;
import com.android.server.devicepolicy.plugins.DeviceWifiPlugin;
import com.android.server.devicepolicy.plugins.FrameworkTestPlugin;
import com.android.server.devicepolicy.plugins.HwEmailMDMPlugin;
import com.android.server.devicepolicy.plugins.HwSystemManagerPlugin;
import com.android.server.devicepolicy.plugins.PhoneManagerPlugin;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import com.android.server.pm.auth.HwCertificationManager;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.widget.LockPatternUtilsEx;
import com.huawei.server.HwPartIawareUtil;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class HwDevicePolicyManagerService implements IHwDevicePolicyManager, IHwDevicePolicyManagerService {
    private static final String ADD = "add";
    private static final String ATTR_VALUE = "value";
    public static final int CERTIFICATE_PEM_BASE64 = 1;
    public static final int CERTIFICATE_PKCS12 = 0;
    private static final Set<Integer> DA_DISALLOWED_POLICIES = new ArraySet();
    private static final int DEFAULT_ACTIVE_MODE = 0;
    private static final String DELETE = "delete";
    private static final String DISABLE_ADB = "disable-adb";
    private static final String DISABLE_BACK_KEY = "disable-backKey";
    private static final String DISABLE_BLUETOOTH = "disable-bluetooth";
    private static final String DISABLE_CHANGE_LAUNCHER = "disable-change-launcher";
    private static final String DISABLE_DATA_CONNECTIVITY = "disable-dataconnectivity";
    private static final String DISABLE_GPS = "disable-gps";
    private static final String DISABLE_HOME_KEY = "disable-homekey";
    private static final String DISABLE_INSTALLSOURCE = "disable-installsource";
    private static final String DISABLE_NFC = "disable-nfc";
    private static final String DISABLE_SAFEMODE = "disable-safemode";
    private static final String DISABLE_STATUS_BAR = "disable-expandpanel";
    private static final String DISABLE_TASK_KEY = "disable-taskkey";
    private static final String DISABLE_USBOTG = "disable-usbotg";
    private static final String DISABLE_WIFI = "disable-wifi";
    private static final String DOES_NOT_HAVE_NETWORK_PERMISSION = "Does not have network manager MDM permission.";
    public static final String DYNAMIC_ROOT_PROP = "persist.sys.root.status";
    public static final String DYNAMIC_ROOT_STATE_SAFE = "0";
    private static final String EXCHANGE_DOMAIN = "domain";
    private static final int EXCHANGE_PROVIDER_MAX_NUM = 20;
    private static final int FAILED = -1;
    private static final int FLAG_SHUTDOWN = 65536;
    private static final int FORCED_ACTIVE_MODE = 1;
    public static final HashMap<String, PolicyStruct.PolicyItem> GLOBAL_POLICY_ITEMS = new HashMap<>();
    private static final boolean HWDBG = false;
    private static final Set<String> HWDEVICE_OWNER_USER_RESTRICTIONS = new HashSet();
    private static final boolean IS_HAS_HW_MDM_FEATURE = true;
    private static final boolean IS_SUPPORT_CRYPT = SystemProperties.getBoolean("ro.config.support_sdcard_crypt", true);
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", MDM_ACTIVE_ADMIN_DEFAULT));
    private static final String KEY = "key";
    private static final int MAX_DELAY_TIME = 72;
    private static final int MAX_QUERY_PROCESS = 10000;
    private static final String MDM_ACTIVE_ADMIN_DEFAULT = "default";
    private static final String MDM_DELAY_DEACTIVE_ADMIN = "mdm_delay_active_admin";
    private static final String MDM_DELAY_DEACTIVE_TIME = "mdm_delay_deactive_time";
    private static final String MDM_FORCED_ACTIVE_ADMIN = "mdm_forced_active_admin";
    private static final String MDM_VPN_PERMISSION = "com.huawei.permission.sec.MDM_VPN";
    private static final int MIN_DELAY_TIME = 1;
    private static final long MS_PER_HOUR = 3600000;
    public static final int NOT_SUPPORT_SD_CRYPT = -1;
    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String PASSWORD_CHANGE_EXTEND_TIME = "pwd-password-change-extendtime";
    private static final int PERSIST_APP_LIMITS = 10;
    private static final String PROP_ACTIVE_MODE = "hw_mc.mdm.activate_mode";
    public static final int SD_CRYPT_STATE_DECRYPTED = 1;
    public static final int SD_CRYPT_STATE_DECRYPTING = 4;
    public static final int SD_CRYPT_STATE_ENCRYPTED = 2;
    public static final int SD_CRYPT_STATE_ENCRYPTING = 3;
    public static final int SD_CRYPT_STATE_INVALID = 0;
    public static final int SD_CRYPT_STATE_MISMATCH = 5;
    public static final int SD_CRYPT_STATE_WAIT_UNLOCK = 6;
    private static final String SETTINGS_MENUS_REMOVE = "settings_menus_remove";
    private static final int SUCCEED = 1;
    private static final String TAG = "HwDPMS";
    private static final String TAG_POLICY_SYS_APP = "update-sys-app-install-list";
    private static final String TAG_POLICY_UNDETACHABLE_SYS_APP = "update-sys-app-undetachable-install-list";
    private static final String UPDATE = "update";
    private static final String USB_STORAGE = "usb";
    private static ArrayList<String> userIsolationPolicyList = new ArrayList<String>() {
        /* class com.android.server.devicepolicy.HwDevicePolicyManagerService.AnonymousClass1 */

        {
            add("email-disable-delete-account");
            add("email-disable-add-account");
            add("allowing-addition-black-list");
        }
    };
    private final String descriptorNetworkmanagementService = "android.os.INetworkManagementService";
    private final ArrayList<EffectedItem> effectedItems = new ArrayList<>();
    private final ArrayList<DevicePolicyPlugin> globalPlugins = new ArrayList<>();
    private final ArrayList<PolicyStruct> globalStructs = new ArrayList<>();
    private boolean isHasInit = false;
    private final Context mContext;
    private ConcurrentHashMap<String, Integer> mDelayDeactiveInfo = new ConcurrentHashMap<>();
    private AlertDialog mErrorDialog;
    private HwAdminCache mHwAdminCache;
    private final IHwDevicePolicyManagerInner mHwDevicePolicyManagerInner;
    private final IPackageManager mIPackageManager;
    private boolean mIsMDMDeviceOwnerAPI = false;
    private TransactionProcessor mProcessor = null;
    private final UserManager mUserManager;

    static {
        DA_DISALLOWED_POLICIES.add(8);
        DA_DISALLOWED_POLICIES.add(9);
        DA_DISALLOWED_POLICIES.add(6);
        DA_DISALLOWED_POLICIES.add(0);
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_usb_file_transfer");
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_physical_media");
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_outgoing_calls");
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_sms");
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_config_tethering");
    }

    public HwDevicePolicyManagerService(Context context, IHwDevicePolicyManagerInner innerService) {
        this.mContext = context;
        this.mHwDevicePolicyManagerInner = innerService;
        this.mUserManager = (UserManager) Preconditions.checkNotNull(UserManager.get(context));
        this.mIPackageManager = (IPackageManager) Preconditions.checkNotNull(AppGlobals.getPackageManager());
        HwDevicePolicyManagerServiceUtil.initialize(context);
        this.mProcessor = new TransactionProcessor(this);
        this.mHwAdminCache = new HwAdminCache();
        addDevicePolicyPlugins(context);
        initPolicyStruct();
    }

    private void initPolicyStruct() {
        if (this.globalPlugins.size() > 0) {
            Iterator<DevicePolicyPlugin> it = this.globalPlugins.iterator();
            while (it.hasNext()) {
                DevicePolicyPlugin plugin = it.next();
                if (plugin != null) {
                    PolicyStruct struct = plugin.getPolicyStruct();
                    for (PolicyStruct.PolicyItem item : struct.getPolicyItems()) {
                        if (item == null) {
                            HwLog.w(TAG, "policyItem is null in plugin: " + plugin.getPluginName());
                        }
                    }
                    addPolicyStruct(struct);
                }
            }
        }
    }

    public void systemReady(int phase) {
        HwLog.d(TAG, "systemReady, phase is " + phase);
        if (phase == 1000) {
            listenForUserSwitches();
        }
    }

    public boolean processTransaction(int code, Parcel data, Parcel reply, int flags) {
        if (this.mProcessor.processTransaction(code, data, reply) || this.mProcessor.processTransactionWithPolicyName(code, data, reply)) {
            return true;
        }
        return false;
    }

    private void listenForUserSwitches() {
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                /* class com.android.server.devicepolicy.HwDevicePolicyManagerService.AnonymousClass2 */

                public void onUserSwitching(int newUserId) throws RemoteException {
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    HwDevicePolicyManagerService.this.syncHwDeviceSettingsLocked(newUserId);
                }

                public void onForegroundProfileSwitch(int newProfileId) {
                }
            }, TAG);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to listen for user switching event", e);
        }
    }

    private void enforceHwCrossUserPermission(int userHandle) {
        this.mHwDevicePolicyManagerInner.enforceFullCrossUsersPermissionInner(userHandle);
        if (userHandle != 0) {
            throw new IllegalArgumentException("Invalid userId " + userHandle + ",should be:0");
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setWifiDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have wifi MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableWifi != disabled) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
                        boolean isEnalbed = false;
                        if (wifiManager.isWifiEnabled() && disabled && !wifiManager.setWifiEnabled(false)) {
                            isEnalbed = true;
                        }
                        if (!isEnalbed) {
                            Binder.restoreCallingIdentity(callingId);
                            admin.disableWifi = disabled;
                            this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                        } else {
                            return;
                        }
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4001, isWifiDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isWifiDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_WIFI);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isBluetoothDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_BLUETOOTH);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setBluetoothDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_BLUETOOTH", "does not have bluethooth MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableBluetooth != disabled) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) this.mContext.getSystemService("bluetooth")).getAdapter();
                        if (!(bluetoothAdapter.isEnabled() && disabled) || bluetoothAdapter.disable()) {
                            Binder.restoreCallingIdentity(callingId);
                            admin.disableBluetooth = disabled;
                            this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                        } else {
                            return;
                        }
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4009, isBluetoothDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setWifiApDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have Wifi AP MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            enforceUserRestrictionPermission(who, "no_config_tethering", userHandle);
            HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
            if (ap.disableWifiAp != disabled) {
                ap.disableWifiAp = disabled;
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_config_tethering", userHandle);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isWifiApDisabled(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            boolean z = false;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableWifiAp;
                }
                return z;
            } else if (!this.mUserManager.hasUserRestriction("no_config_tethering", new UserHandle(userHandle))) {
                return false;
            } else {
                Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
                while (it.hasNext()) {
                    HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                    if (admin2.getHwActiveAdmin() != null && admin2.getHwActiveAdmin().disableWifiAp) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setBootLoaderDisabled(ComponentName who, boolean disabled, int userHandle) {
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isBootLoaderDisabled(ComponentName who, int userHandle) {
        return false;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setUSBDataDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have USB MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            enforceUserRestrictionPermission(who, "no_usb_file_transfer", userHandle);
            HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
            if (ap.disableUSBData != disabled) {
                ap.disableUSBData = disabled;
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_usb_file_transfer", userHandle);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isUSBDataDisabled(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            boolean z = false;
            if (who != null) {
                HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
                if (ap != null) {
                    z = ap.disableUSBData;
                }
                return z;
            } else if (!this.mUserManager.hasUserRestriction("no_usb_file_transfer", new UserHandle(userHandle))) {
                return false;
            } else {
                Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
                while (it.hasNext()) {
                    HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                    if (admin.getHwActiveAdmin() != null && admin.getHwActiveAdmin().disableUSBData) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setExternalStorageDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_SDCARD", "does not have SDCARD MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            enforceUserRestrictionPermission(who, "no_physical_media", userHandle);
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableExternalStorage != disabled) {
                admin.disableExternalStorage = disabled;
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_physical_media", userHandle);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isExternalStorageDisabled(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            boolean z = false;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableExternalStorage;
                }
                return z;
            } else if (!this.mUserManager.hasUserRestriction("no_physical_media", new UserHandle(userHandle))) {
                return false;
            } else {
                Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
                while (it.hasNext()) {
                    HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                    if (admin2.getHwActiveAdmin() != null && admin2.getHwActiveAdmin().disableExternalStorage) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setNFCDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NFC", "does not have NFC MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (disabled != admin.disableNFC) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
                        if (nfcAdapter != null) {
                            boolean isNfcEnabled = nfcAdapter.isEnabled();
                            if (disabled && isNfcEnabled) {
                                nfcAdapter.disable();
                            }
                        }
                        Binder.restoreCallingIdentity(callingId);
                        admin.disableNFC = disabled;
                        this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingId);
                        throw th;
                    }
                }
            } else {
                throw new NullPointerException("ComponentName is null");
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isNFCDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_NFC);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setDataConnectivityDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CONNECTIVITY", "Does not hava data connectivity MDM permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableDataConnectivity != disabled) {
                    admin.disableDataConnectivity = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                }
                if (disabled) {
                    try {
                        ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).disableDataConnectivity();
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "Can not calling the remote function to set data enabled!");
                    }
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isDataConnectivityDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_DATA_CONNECTIVITY);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setVoiceDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "Does not hava phone disable MDM permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            enforceUserRestrictionPermission(who, "no_outgoing_calls", userHandle);
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableVoice != disabled) {
                admin.disableVoice = disabled;
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_outgoing_calls", userHandle);
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4002, isVoiceDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isVoiceDisabled(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            boolean z = false;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableVoice;
                }
                return z;
            } else if (!this.mUserManager.hasUserRestriction("no_outgoing_calls", new UserHandle(userHandle))) {
                return false;
            } else {
                Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
                while (it.hasNext()) {
                    HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                    if (admin2.getHwActiveAdmin() != null && admin2.getHwActiveAdmin().disableVoice) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setSMSDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_MMS", "Does not hava SMS disable MDM permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            enforceUserRestrictionPermission(who, "no_sms", userHandle);
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (disabled != admin.disableSMS) {
                admin.disableSMS = disabled;
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_sms", userHandle);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isSMSDisabled(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            boolean z = false;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableSMS;
                }
                return z;
            } else if (!this.mUserManager.hasUserRestriction("no_sms", new UserHandle(userHandle))) {
                return false;
            } else {
                Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
                while (it.hasNext()) {
                    HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                    if (admin2.getHwActiveAdmin() != null && admin2.getHwActiveAdmin().disableSMS) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setStatusBarExpandPanelDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableStatusBarExpandPanel != disabled) {
                if (!disabled || setStatusBarPanelDisabledInternal(userHandle)) {
                    admin.disableStatusBarExpandPanel = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                    if (!disabled) {
                        setStatusBarPanelEnableInternal(false, userHandle);
                    }
                } else {
                    HwLog.w(TAG, "cannot set statusBar disabled");
                }
            }
        }
    }

    private boolean setStatusBarPanelDisabledInternal(int userHandle) {
        long callingId = Binder.clearCallingIdentity();
        try {
            StatusBarManager statusBar = (StatusBarManager) this.mContext.getSystemService("statusbar");
            if (statusBar == null) {
                HwLog.w(TAG, "statusBar is null");
                Binder.restoreCallingIdentity(callingId);
                return false;
            }
            statusBar.disable2(1);
            Binder.restoreCallingIdentity(callingId);
            return true;
        } catch (ClassCastException e) {
            HwLog.e(TAG, "failed to set statusBar disabled. CalssCastException");
        } catch (Exception e2) {
            HwLog.e(TAG, "failed to set statusBar disabled.");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
        Binder.restoreCallingIdentity(callingId);
        return false;
    }

    private boolean setStatusBarPanelEnableInternal(boolean forceEnable, int userHandle) {
        long callingId = Binder.clearCallingIdentity();
        try {
            StatusBarManager statusBar = (StatusBarManager) this.mContext.getSystemService("statusbar");
            if (statusBar == null) {
                HwLog.w(TAG, "statusBar is null");
                Binder.restoreCallingIdentity(callingId);
                return false;
            }
            if (forceEnable) {
                statusBar.disable2(0);
            } else if (!isStatusBarExpandPanelDisabled(null, userHandle)) {
                statusBar.disable2(0);
            }
            Binder.restoreCallingIdentity(callingId);
            return true;
        } catch (ClassCastException e) {
            HwLog.e(TAG, "failed to set statusBar enabled. ClassCastException");
        } catch (Exception e2) {
            HwLog.e(TAG, "failed to set statusBar enabled.");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
            throw th;
        }
        Binder.restoreCallingIdentity(callingId);
        return false;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isStatusBarExpandPanelDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_STATUS_BAR);
    }

    private void showProhibitCallDialog() {
        UiThread.getHandler().post(new Runnable() {
            /* class com.android.server.devicepolicy.HwDevicePolicyManagerService.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                if (HwDevicePolicyManagerService.this.mErrorDialog != null) {
                    HwDevicePolicyManagerService.this.mErrorDialog.show();
                    return;
                }
                HwDevicePolicyManagerService hwDevicePolicyManagerService = HwDevicePolicyManagerService.this;
                hwDevicePolicyManagerService.mErrorDialog = new AlertDialog.Builder(hwDevicePolicyManagerService.mContext, 33947691).setMessage(33686011).setPositiveButton(33686094, new DialogInterface.OnClickListener() {
                    /* class com.android.server.devicepolicy.HwDevicePolicyManagerService.AnonymousClass3.AnonymousClass1 */

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        HwDevicePolicyManagerService.this.mErrorDialog.dismiss();
                    }
                }).setCancelable(true).create();
                HwDevicePolicyManagerService.this.mErrorDialog.getWindow().setType(2003);
                HwDevicePolicyManagerService.this.mErrorDialog.show();
            }
        });
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void hangupCalling(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "Does not hava hangup calling permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            } else if (getHwActiveAdmin(who, userHandle) != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    TelecomManager.from(this.mContext).endCall();
                    showProhibitCallDialog();
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x008d, code lost:
        if (0 != 0) goto L_0x00b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00a2, code lost:
        if (1 == 0) goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b5, code lost:
        if (1 == 0) goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00b7, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b8, code lost:
        if (r5 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00ba, code lost:
        r5.commit(new com.android.server.devicepolicy.HwDevicePolicyManagerService.LocalIntentReceiver(r19, r21.getPackageName(), r8).getIntentSender());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        return;
     */
    private void commitInstall(Context context, ComponentName who, Uri uri) {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(1);
        params.installReason = 9;
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.Session session = null;
        int sessionId = 0;
        try {
            sessionId = packageInstaller.createSession(params);
            session = packageInstaller.openSession(sessionId);
            InputStream in = this.mContext.getContentResolver().openInputStream(uri);
            if (in == null) {
                closeIO(session);
                closeIO(in);
                closeIO(null);
                closeIO(session);
                if (0 == 0) {
                    return;
                }
                return;
            }
            byte[] buffer = new byte[1048576];
            OutputStream out = session.openWrite(who.getPackageName(), 0, (long) in.available());
            if (out == null) {
                closeIO(in);
                closeIO(session);
                closeIO(in);
                closeIO(out);
                closeIO(session);
                if (0 == 0) {
                    return;
                }
                return;
            }
            while (true) {
                int length = in.read(buffer);
                if (length == -1) {
                    break;
                }
                out.write(buffer, 0, length);
            }
            session.fsync(out);
            closeIO(in);
            closeIO(out);
            closeIO(session);
        } catch (IOException e) {
            HwLog.w(TAG, "commitInstall IOException");
            closeIO(null);
            closeIO(null);
            closeIO(null);
        } catch (SecurityException e2) {
            HwLog.w(TAG, "commitInstall SecurityException");
            closeIO(null);
            closeIO(null);
            closeIO(null);
        } catch (Throwable th) {
            closeIO(null);
            closeIO(null);
            closeIO(null);
            if (0 != 0) {
            }
            throw th;
        }
    }

    private void closeIO(Closeable close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException e) {
                HwLog.w(TAG, "closeIO IOException when try to close");
            }
        }
    }

    /* access modifiers changed from: private */
    public class LocalIntentReceiver {
        private IIntentSender.Stub mLocalSender = new IIntentSender.Stub() {
            /* class com.android.server.devicepolicy.HwDevicePolicyManagerService.LocalIntentReceiver.AnonymousClass1 */

            public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
                String pkgName = intent.getStringExtra("android.content.pm.extra.PACKAGE_NAME");
                int installStatus = intent.getIntExtra("android.content.pm.extra.STATUS", 1);
                LocalIntentReceiver.this.sendMdmPackageInstallBroadcast(pkgName, installStatus);
                if (installStatus == 0 && LocalIntentReceiver.this.reportSessionId == intent.getIntExtra("android.content.pm.extra.SESSION_ID", 0)) {
                    BdReportUtils.reportInstallPkgData(LocalIntentReceiver.this.reportOwnerPkgName, pkgName, HwDevicePolicyManagerService.this.mContext);
                }
            }
        };
        private String reportOwnerPkgName;
        private int reportSessionId;

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendMdmPackageInstallBroadcast(String pkgName, int installStatus) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt("returnCode", installStatus);
            bundle.putString("packageName", pkgName);
            intent.putExtras(bundle);
            intent.setPackage(this.reportOwnerPkgName);
            intent.setAction("com.huawei.intent.action.InstallMdmPackage");
            HwDevicePolicyManagerService.this.mContext.sendBroadcast(intent);
        }

        LocalIntentReceiver(String ownerPkgName, int sessionId) {
            this.reportOwnerPkgName = ownerPkgName;
            this.reportSessionId = sessionId;
        }

        public IntentSender getIntentSender() {
            return new IntentSender(this.mLocalSender);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void installPackage(ComponentName who, String packagePath, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            } else if (getHwActiveAdmin(who, userHandle) != null) {
                if (!TextUtils.isEmpty(packagePath)) {
                    Uri uri = Uri.parse(packagePath);
                    if (uri == null || !"content".equalsIgnoreCase(uri.getScheme())) {
                        installPackage(packagePath, who.getPackageName());
                    } else {
                        long callingId = Binder.clearCallingIdentity();
                        try {
                            commitInstall(this.mContext, who, uri);
                        } finally {
                            Binder.restoreCallingIdentity(callingId);
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("Install package path is empty");
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void uninstallPackage(ComponentName who, String packageName, boolean keepData, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                } else if (getHwActiveAdmin(who, userHandle) != null) {
                    uninstallPackage(packageName, keepData);
                }
            }
            BdReportUtils.reportUninstallPkgData(who.getPackageName(), packageName, this.mContext);
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void clearPackageData(ComponentName who, String packageName, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                } else if (!TextUtils.isEmpty(packageName)) {
                    enforceCheckNotActiveAdminApp(packageName, userHandle);
                    if (getHwActiveAdmin(who, userHandle) != null) {
                        long id = Binder.clearCallingIdentity();
                        try {
                            ((ActivityManager) this.mContext.getSystemService("activity")).clearApplicationUserData(packageName, null);
                        } finally {
                            Binder.restoreCallingIdentity(id);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void enableInstallPackage(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    admin.disableInstallSource = false;
                    admin.installSourceWhitelist = null;
                }
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4003, isInstallSourceDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void disableInstallSource(ComponentName who, List<String> whitelist, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(whitelist)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (who != null) {
                    if (whitelist != null) {
                        if (!whitelist.isEmpty()) {
                            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                            admin.disableInstallSource = true;
                            if (admin.installSourceWhitelist == null) {
                                admin.installSourceWhitelist = new ArrayList();
                            }
                            HwDevicePolicyManagerServiceUtil.isOverLimit(admin.installSourceWhitelist, whitelist);
                            HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.installSourceWhitelist, whitelist);
                            this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwAdminCache hwAdminCache = this.mHwAdminCache;
            if (hwAdminCache != null) {
                hwAdminCache.syncHwAdminCache(4003, isInstallSourceDisabled(null, userHandle));
                this.mHwAdminCache.syncHwAdminCache(4004, getInstallPackageSourceWhiteList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + whitelist + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isInstallSourceDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_INSTALLSOURCE);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> getInstallPackageSourceWhiteList(ComponentName who, int userHandle) {
        List<String> list;
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.installSourceWhitelist != null) {
                    if (!admin.installSourceWhitelist.isEmpty()) {
                        list = admin.installSourceWhitelist;
                        return list;
                    }
                }
                list = null;
                return list;
            }
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            ArrayList<String> whiteList = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin2.getHwActiveAdmin() != null) {
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(whiteList, admin2.getHwActiveAdmin().installSourceWhitelist);
                }
            }
            return whiteList;
        }
    }

    private void checkParameter(ComponentName who, List<String> packageNames) {
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        } else if (packageNames == null || packageNames.isEmpty()) {
            throw new IllegalArgumentException("packageNames is null or empty");
        }
    }

    private void dealPersistentApp(ComponentName who, List<String> packageNames, int userHandle, boolean isAdd) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                checkParameter(who, packageNames);
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (isAdd) {
                    if (admin.persistentAppList == null) {
                        admin.persistentAppList = new ArrayList();
                    }
                    HwDevicePolicyManagerServiceUtil.isOverLimit(getPersistentApp(null, userHandle), packageNames, PERSIST_APP_LIMITS);
                    AppUtils.filterOutSystemAppList(this.mContext, packageNames);
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.persistentAppList, packageNames);
                } else {
                    HwDevicePolicyManagerServiceUtil.removeItemsFromList(admin.persistentAppList, packageNames);
                }
            }
            HwAdminCache hwAdminCache = this.mHwAdminCache;
            if (hwAdminCache != null) {
                hwAdminCache.syncHwAdminCache(4005, getPersistentApp(null, userHandle));
            }
            this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            sendPersistentAppToIAware(userHandle);
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void addPersistentApp(ComponentName who, List<String> packageNames, int userHandle) {
        dealPersistentApp(who, packageNames, userHandle, true);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void removePersistentApp(ComponentName who, List<String> packageNames, int userHandle) {
        dealPersistentApp(who, packageNames, userHandle, false);
    }

    private void sendPersistentAppToIAware(int userHandle) {
        List<String> persistAppList = getPersistentApp(null, userHandle);
        if (persistAppList == null || persistAppList.size() <= 0) {
            HwPartIawareUtil.removeProtectedListFromMdm(this.mContext);
            Slog.d(TAG, "removeProtectedListFromMdm for user " + userHandle);
            return;
        }
        HwPartIawareUtil.setProtectedListFromMdm(this.mContext, persistAppList);
        Slog.d(TAG, "setProtectedListFromMdm for user " + userHandle + ":" + persistAppList);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> getPersistentApp(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            List<String> list = null;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.persistentAppList != null) {
                    if (!admin.persistentAppList.isEmpty()) {
                        list = admin.persistentAppList;
                    }
                }
                return list;
            }
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            ArrayList<String> totalList = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin2.getHwActiveAdmin() != null) {
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(totalList, admin2.getHwActiveAdmin().persistentAppList);
                }
            }
            if (!totalList.isEmpty()) {
                list = totalList;
            }
            return list;
        }
    }

    private void dealDisallowedRunningApp(ComponentName who, List<String> packageNames, int userHandle, boolean isAdd) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                checkParameter(who, packageNames);
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (isAdd) {
                    if (admin.disallowedRunningAppList == null) {
                        admin.disallowedRunningAppList = new ArrayList();
                    }
                    HwDevicePolicyManagerServiceUtil.isOverLimit(admin.disallowedRunningAppList, packageNames);
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.disallowedRunningAppList, packageNames);
                    for (String packageName : packageNames) {
                        killApplicationInner(packageName);
                    }
                } else {
                    HwDevicePolicyManagerServiceUtil.removeItemsFromList(admin.disallowedRunningAppList, packageNames);
                }
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            HwAdminCache hwAdminCache = this.mHwAdminCache;
            if (hwAdminCache != null) {
                hwAdminCache.syncHwAdminCache(4006, getDisallowedRunningApp(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void addDisallowedRunningApp(ComponentName who, List<String> packageNames, int userHandle) {
        dealDisallowedRunningApp(who, packageNames, userHandle, true);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void removeDisallowedRunningApp(ComponentName who, List<String> packageNames, int userHandle) {
        dealDisallowedRunningApp(who, packageNames, userHandle, false);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> getDisallowedRunningApp(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            List<String> list = null;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disallowedRunningAppList != null) {
                    if (!admin.disallowedRunningAppList.isEmpty()) {
                        list = admin.disallowedRunningAppList;
                    }
                }
                return list;
            }
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            ArrayList<String> totalList = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin2.getHwActiveAdmin() != null) {
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(totalList, admin2.getHwActiveAdmin().disallowedRunningAppList);
                }
            }
            if (!totalList.isEmpty()) {
                list = totalList;
            }
            return list;
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void addInstallPackageWhiteList(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                checkParameter(who, packageNames);
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.installPackageWhitelist == null) {
                    admin.installPackageWhitelist = new ArrayList();
                }
                HwDevicePolicyManagerServiceUtil.isOverLimit(admin.installPackageWhitelist, packageNames);
                HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.installPackageWhitelist, packageNames);
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            HwAdminCache hwAdminCache = this.mHwAdminCache;
            if (hwAdminCache != null) {
                hwAdminCache.syncHwAdminCache(4007, getInstallPackageWhiteList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void removeInstallPackageWhiteList(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                checkParameter(who, packageNames);
                HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).installPackageWhitelist, packageNames);
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            HwAdminCache hwAdminCache = this.mHwAdminCache;
            if (hwAdminCache != null) {
                hwAdminCache.syncHwAdminCache(4007, getInstallPackageWhiteList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> getInstallPackageWhiteList(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            List<String> list = null;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.installPackageWhitelist != null) {
                    if (!admin.installPackageWhitelist.isEmpty()) {
                        list = admin.installPackageWhitelist;
                    }
                }
                return list;
            }
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            ArrayList<String> whitelist = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin2.getHwActiveAdmin() != null) {
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(whitelist, admin2.getHwActiveAdmin().installPackageWhitelist);
                }
            }
            if (!whitelist.isEmpty()) {
                list = whitelist;
            }
            return list;
        }
    }

    private void dealDisallowedUninstallPackages(ComponentName who, List<String> packageNames, int userHandle, boolean isAdd) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                checkParameter(who, packageNames);
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (isAdd) {
                    if (admin.disallowedUninstallPackageList == null) {
                        admin.disallowedUninstallPackageList = new ArrayList();
                    }
                    HwDevicePolicyManagerServiceUtil.isOverLimit(admin.disallowedUninstallPackageList, packageNames);
                    AppUtils.filterOutSystemAppList(this.mContext, packageNames);
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.disallowedUninstallPackageList, packageNames);
                } else {
                    HwDevicePolicyManagerServiceUtil.removeItemsFromList(admin.disallowedUninstallPackageList, packageNames);
                }
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            HwAdminCache hwAdminCache = this.mHwAdminCache;
            if (hwAdminCache != null) {
                hwAdminCache.syncHwAdminCache(4008, getDisallowedUninstallPackageList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void addDisallowedUninstallPackages(ComponentName who, List<String> packageNames, int userHandle) {
        dealDisallowedUninstallPackages(who, packageNames, userHandle, true);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void removeDisallowedUninstallPackages(ComponentName who, List<String> packageNames, int userHandle) {
        dealDisallowedUninstallPackages(who, packageNames, userHandle, false);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> getDisallowedUninstallPackageList(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            List<String> list = null;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disallowedUninstallPackageList != null) {
                    if (!admin.disallowedUninstallPackageList.isEmpty()) {
                        list = admin.disallowedUninstallPackageList;
                    }
                }
                return list;
            }
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            ArrayList<String> blacklist = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin2.getHwActiveAdmin() != null) {
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(blacklist, admin2.getHwActiveAdmin().disallowedUninstallPackageList);
                }
            }
            if (!blacklist.isEmpty()) {
                list = blacklist;
            }
            return list;
        }
    }

    private void dealDisabledDeactivateMdmPackages(ComponentName who, List<String> packageNames, int userHandle, boolean isAdd) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                checkParameter(who, packageNames);
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (isAdd) {
                    if (admin.disabledDeactiveMdmPackagesList == null) {
                        admin.disabledDeactiveMdmPackagesList = new ArrayList();
                    }
                    HwDevicePolicyManagerServiceUtil.isOverLimit(admin.disabledDeactiveMdmPackagesList, packageNames);
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.disabledDeactiveMdmPackagesList, packageNames);
                } else {
                    HwDevicePolicyManagerServiceUtil.removeItemsFromList(admin.disabledDeactiveMdmPackagesList, packageNames);
                }
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
            HwAdminCache hwAdminCache = this.mHwAdminCache;
            if (hwAdminCache != null) {
                hwAdminCache.syncHwAdminCache(4019, getDisabledDeactivateMdmPackageList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void addDisabledDeactivateMdmPackages(ComponentName who, List<String> packageNames, int userHandle) {
        dealDisabledDeactivateMdmPackages(who, packageNames, userHandle, true);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void removeDisabledDeactivateMdmPackages(ComponentName who, List<String> packageNames, int userHandle) {
        dealDisabledDeactivateMdmPackages(who, packageNames, userHandle, false);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> getDisabledDeactivateMdmPackageList(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            List<String> list = null;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disabledDeactiveMdmPackagesList != null) {
                    if (!admin.disabledDeactiveMdmPackagesList.isEmpty()) {
                        list = admin.disabledDeactiveMdmPackagesList;
                    }
                }
                return list;
            }
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            ArrayList<String> blacklist = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin2.getHwActiveAdmin() != null) {
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(blacklist, admin2.getHwActiveAdmin().disabledDeactiveMdmPackagesList);
                }
            }
            if (!blacklist.isEmpty()) {
                list = blacklist;
            }
            return list;
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void killApplicationProcess(ComponentName who, String packageName, int userHandle) {
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (who != null) {
                    if (!TextUtils.isEmpty(packageName) && !packageName.equals(who.getPackageName())) {
                        enforceCheckNotActiveAdminApp(packageName, userHandle);
                        if (getHwActiveAdmin(who, userHandle) != null) {
                            killApplicationInner(packageName);
                        }
                    }
                }
                throw new IllegalArgumentException("ComponentName null or packageName empty or cannot kill self");
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
    }

    private void killApplicationInner(String packageName) {
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
            for (ActivityManager.RecentTaskInfo ti : am.getRecentTasks(MAX_QUERY_PROCESS, 1)) {
                if (packageName.equals(ti.realActivity.getPackageName())) {
                    try {
                        ActivityManager.getService().removeTask(ti.persistentId);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "killApplicationInner exception is " + e.getMessage());
                    }
                }
            }
            am.forceStopPackage(packageName);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void shutdownOrRebootDevice(int code, ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                try {
                    IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                    if (power != null) {
                        if (code == 1501) {
                            if (IS_TV) {
                                power.goToSleep(SystemClock.uptimeMillis(), 4, (int) FLAG_SHUTDOWN);
                            } else {
                                power.shutdown(false, (String) null, false);
                            }
                        }
                        if (code == 1502) {
                            power.reboot(false, (String) null, false);
                        }
                        Binder.restoreCallingIdentity(callingId);
                    }
                } catch (RemoteException e) {
                    HwLog.e(TAG, "exception is " + e.getMessage());
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void configExchangeMailProvider(ComponentName who, Bundle para, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_EMAIL", "does not have EMAIL MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who == null || para == null) {
                throw new IllegalArgumentException("ComponentName or para is null");
            } else if (HwDevicePolicyManagerServiceUtil.isValidExchangeParameter(para)) {
                HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
                if (ap.mailProviderlist == null) {
                    ap.mailProviderlist = new ArrayList();
                    ap.mailProviderlist.add(para);
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                } else if (ap.mailProviderlist.size() + 1 <= EXCHANGE_PROVIDER_MAX_NUM) {
                    boolean isAlready = false;
                    Bundle provider = null;
                    Iterator it = ap.mailProviderlist.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Bundle each = (Bundle) it.next();
                        if (HwDevicePolicyManagerServiceUtil.matchProvider(para.getString(EXCHANGE_DOMAIN), each.getString(EXCHANGE_DOMAIN))) {
                            isAlready = true;
                            provider = each;
                            break;
                        }
                    }
                    if (isAlready && provider != null) {
                        ap.mailProviderlist.remove(provider);
                    }
                    ap.mailProviderlist.add(para);
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                } else {
                    throw new IllegalArgumentException("already exceeds max number.");
                }
            } else {
                throw new IllegalArgumentException("some paremeter is null");
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle getMailProviderForDomain(ComponentName who, String domain, int userHandle) {
        if (userHandle != 0) {
            return null;
        }
        if (!TextUtils.isEmpty(domain)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (who != null) {
                    HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.mailProviderlist == null) {
                        return null;
                    }
                    for (Bundle provider : admin.mailProviderlist) {
                        if (provider != null) {
                            if (HwDevicePolicyManagerServiceUtil.matchProvider(domain, provider.getString(EXCHANGE_DOMAIN))) {
                                return provider;
                            }
                        }
                    }
                    return null;
                }
                Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
                while (it.hasNext()) {
                    HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                    if (admin2.getHwActiveAdmin() != null) {
                        if (admin2.getHwActiveAdmin().mailProviderlist != null) {
                            for (Bundle provider2 : admin2.getHwActiveAdmin().mailProviderlist) {
                                if (provider2 != null) {
                                    if (HwDevicePolicyManagerServiceUtil.matchProvider(domain, provider2.getString(EXCHANGE_DOMAIN))) {
                                        return provider2;
                                    }
                                }
                            }
                        }
                    }
                    return null;
                }
                return null;
            }
        }
        throw new IllegalArgumentException("domain is empty.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isRooted(ComponentName who, int userHandle) {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                try {
                    getHwActiveAdmin(who, userHandle);
                    String currentState = SystemProperties.get(DYNAMIC_ROOT_PROP);
                    if (!TextUtils.isEmpty(currentState)) {
                        if (DYNAMIC_ROOT_STATE_SAFE.equals(currentState)) {
                            z = false;
                        }
                    }
                    z = true;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        return z;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setSafeModeDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableSafeMode != disabled) {
                    admin.disableSafeMode = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        syncHwAdminSafeModeCache(userHandle);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isSafeModeDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_SAFEMODE);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setAdbDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have MDM_USB permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableAdb != disabled) {
                    admin.disableAdb = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4012, isAdbDisabled(null, userHandle));
        }
        long identityToken = Binder.clearCallingIdentity();
        if (disabled) {
            applyAdbDisabled();
        }
        Binder.restoreCallingIdentity(identityToken);
    }

    private void applyAdbDisabled() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "adb_enabled", 0) > 0) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
            return;
        }
        UsbManager usbManager = (UsbManager) this.mContext.getSystemService(USB_STORAGE);
        if (usbManager == null) {
            Slog.e(TAG, "usbManager is null, return!!");
        } else {
            usbManager.setCurrentFunctions(usbManager.getCurrentFunctions());
        }
    }

    private boolean getFunctionDisabledStatus(ComponentName who, int userHandle, String key) {
        HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
        if (admin == null) {
            return false;
        }
        char c = 65535;
        switch (key.hashCode()) {
            case -2008858093:
                if (key.equals(DISABLE_BACK_KEY)) {
                    c = 5;
                    break;
                }
                break;
            case -1843316058:
                if (key.equals(DISABLE_DATA_CONNECTIVITY)) {
                    c = '\r';
                    break;
                }
                break;
            case -1419688439:
                if (key.equals(DISABLE_BLUETOOTH)) {
                    c = '\b';
                    break;
                }
                break;
            case -1253616571:
                if (key.equals(DISABLE_STATUS_BAR)) {
                    c = '\t';
                    break;
                }
                break;
            case -1031979302:
                if (key.equals(DISABLE_WIFI)) {
                    c = 7;
                    break;
                }
                break;
            case -792458415:
                if (key.equals(DISABLE_INSTALLSOURCE)) {
                    c = '\f';
                    break;
                }
                break;
            case -568907973:
                if (key.equals(DISABLE_HOME_KEY)) {
                    c = 4;
                    break;
                }
                break;
            case -322832328:
                if (key.equals(DISABLE_CHANGE_LAUNCHER)) {
                    c = 6;
                    break;
                }
                break;
            case 357203363:
                if (key.equals(DISABLE_USBOTG)) {
                    c = 1;
                    break;
                }
                break;
            case 1096113365:
                if (key.equals(DISABLE_TASK_KEY)) {
                    c = 3;
                    break;
                }
                break;
            case 1352162362:
                if (key.equals(DISABLE_ADB)) {
                    c = 0;
                    break;
                }
                break;
            case 1352168517:
                if (key.equals(DISABLE_GPS)) {
                    c = 2;
                    break;
                }
                break;
            case 1352174918:
                if (key.equals(DISABLE_NFC)) {
                    c = 11;
                    break;
                }
                break;
            case 1794281461:
                if (key.equals(DISABLE_SAFEMODE)) {
                    c = '\n';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return admin.disableAdb;
            case 1:
                return admin.disableUSBOtg;
            case 2:
                return admin.disableGPS;
            case 3:
                return admin.disableTaskKey;
            case SD_CRYPT_STATE_DECRYPTING /* 4 */:
                return admin.disableHomeKey;
            case SD_CRYPT_STATE_MISMATCH /* 5 */:
                return admin.disableBackKey;
            case SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                return admin.disableChangeLauncher;
            case 7:
                return admin.disableWifi;
            case '\b':
                return admin.disableBluetooth;
            case '\t':
                return admin.disableStatusBarExpandPanel;
            case PERSIST_APP_LIMITS /* 10 */:
                return admin.disableSafeMode;
            case 11:
                return admin.disableNFC;
            case '\f':
                return admin.disableInstallSource;
            case '\r':
                return admin.disableDataConnectivity;
            default:
                return false;
        }
    }

    private boolean getFunctionDisabledStatus(int userHandle, String key) {
        Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
        while (it.hasNext()) {
            HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
            if (admin.getHwActiveAdmin() != null) {
                char c = 65535;
                switch (key.hashCode()) {
                    case -2008858093:
                        if (key.equals(DISABLE_BACK_KEY)) {
                            c = 5;
                            break;
                        }
                        break;
                    case -1843316058:
                        if (key.equals(DISABLE_DATA_CONNECTIVITY)) {
                            c = '\r';
                            break;
                        }
                        break;
                    case -1419688439:
                        if (key.equals(DISABLE_BLUETOOTH)) {
                            c = '\b';
                            break;
                        }
                        break;
                    case -1253616571:
                        if (key.equals(DISABLE_STATUS_BAR)) {
                            c = '\t';
                            break;
                        }
                        break;
                    case -1031979302:
                        if (key.equals(DISABLE_WIFI)) {
                            c = 7;
                            break;
                        }
                        break;
                    case -792458415:
                        if (key.equals(DISABLE_INSTALLSOURCE)) {
                            c = '\f';
                            break;
                        }
                        break;
                    case -568907973:
                        if (key.equals(DISABLE_HOME_KEY)) {
                            c = 4;
                            break;
                        }
                        break;
                    case -322832328:
                        if (key.equals(DISABLE_CHANGE_LAUNCHER)) {
                            c = 6;
                            break;
                        }
                        break;
                    case 357203363:
                        if (key.equals(DISABLE_USBOTG)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1096113365:
                        if (key.equals(DISABLE_TASK_KEY)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1352162362:
                        if (key.equals(DISABLE_ADB)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 1352168517:
                        if (key.equals(DISABLE_GPS)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1352174918:
                        if (key.equals(DISABLE_NFC)) {
                            c = 11;
                            break;
                        }
                        break;
                    case 1794281461:
                        if (key.equals(DISABLE_SAFEMODE)) {
                            c = '\n';
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        if (admin.getHwActiveAdmin().disableAdb) {
                            return true;
                        }
                        continue;
                    case 1:
                        if (admin.getHwActiveAdmin().disableUSBOtg) {
                            return true;
                        }
                        continue;
                    case 2:
                        if (admin.getHwActiveAdmin().disableGPS) {
                            return true;
                        }
                        continue;
                    case 3:
                        if (admin.getHwActiveAdmin().disableTaskKey) {
                            return true;
                        }
                        continue;
                    case SD_CRYPT_STATE_DECRYPTING /* 4 */:
                        if (admin.getHwActiveAdmin().disableHomeKey) {
                            return true;
                        }
                        continue;
                    case SD_CRYPT_STATE_MISMATCH /* 5 */:
                        if (admin.getHwActiveAdmin().disableBackKey) {
                            return true;
                        }
                        continue;
                    case SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                        if (admin.getHwActiveAdmin().disableChangeLauncher) {
                            return true;
                        }
                        continue;
                    case 7:
                        if (admin.getHwActiveAdmin().disableWifi) {
                            return true;
                        }
                        continue;
                    case '\b':
                        if (admin.getHwActiveAdmin().disableBluetooth) {
                            return true;
                        }
                        continue;
                    case '\t':
                        if (admin.getHwActiveAdmin().disableStatusBarExpandPanel) {
                            return true;
                        }
                        continue;
                    case PERSIST_APP_LIMITS /* 10 */:
                        if (admin.getHwActiveAdmin().disableSafeMode) {
                            return true;
                        }
                        continue;
                    case 11:
                        if (admin.getHwActiveAdmin().disableNFC) {
                            return true;
                        }
                        continue;
                    case '\f':
                        if (admin.getHwActiveAdmin().disableInstallSource) {
                            return true;
                        }
                        continue;
                    case '\r':
                        if (admin.getHwActiveAdmin().disableDataConnectivity) {
                            return true;
                        }
                        continue;
                    default:
                        return false;
                }
            }
        }
        return false;
    }

    private boolean isFunctionDisabled(ComponentName who, int userHandle, String functionName) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                return getFunctionDisabledStatus(who, userHandle, functionName);
            }
            return getFunctionDisabledStatus(userHandle, functionName);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isAdbDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_ADB);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setUSBOtgDisabled(ComponentName who, boolean disabled, int userHandle) {
        VolumeInfo volumeInfo;
        DiskInfo diskInfo;
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have MDM_USB permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableUSBOtg != disabled) {
                    admin.disableUSBOtg = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        long identityToken = Binder.clearCallingIdentity();
        StorageManager sm = (StorageManager) this.mContext.getSystemService("storage");
        StorageVolume[] volumeList = sm.getVolumeList();
        for (StorageVolume storageVolume : volumeList) {
            if (storageVolume.isRemovable() && "mounted".equals(sm.getVolumeState(storageVolume.getPath())) && (volumeInfo = sm.findVolumeByUuid(storageVolume.getUuid())) != null && (diskInfo = volumeInfo.getDisk()) != null && diskInfo.isUsb()) {
                Slog.e(TAG, "find usb otg device mounted , umounted it");
                sm.unmount(storageVolume.getId());
            }
        }
        Binder.restoreCallingIdentity(identityToken);
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4013, isUSBOtgDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isUSBOtgDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_USBOTG);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setGPSDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_LOCATION", "does not have MDM_LOCATION permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (disabled != admin.disableGPS) {
                    admin.disableGPS = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        if (isGPSTurnOn(who, userHandle) && disabled) {
            turnOnGPS(who, false, userHandle);
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4014, isGPSDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isGPSDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_GPS);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void turnOnGPS(ComponentName who, boolean on, int userHandle) {
        int locationMode;
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_LOCATION", "does not have MDM_LOCATION permission!");
        if (who != null) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
            }
            if (isGPSTurnOn(who, userHandle) != on) {
                long identityToken = Binder.clearCallingIdentity();
                int locationMode2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, ActivityManager.getCurrentUser());
                if (on) {
                    locationMode = locationMode2 | 1;
                } else {
                    locationMode = locationMode2 & 2;
                }
                if (!Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "location_mode", locationMode, ActivityManager.getCurrentUser())) {
                    HwLog.e(TAG, "setLocationProviderEnabledForUser failed");
                }
                Binder.restoreCallingIdentity(identityToken);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isGPSTurnOn(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            getHwActiveAdmin(who, userHandle);
        }
        long identityToken = Binder.clearCallingIdentity();
        boolean isGPSEnabled = false;
        int locationMode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, ActivityManager.getCurrentUser());
        if (locationMode == 3 || locationMode == 2) {
            isGPSEnabled = true;
        }
        Binder.restoreCallingIdentity(identityToken);
        return isGPSEnabled;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setTaskButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableTaskKey != disabled) {
                    admin.disableTaskKey = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4016, isTaskButtonDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isTaskButtonDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_TASK_KEY);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setHomeButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableHomeKey != disabled) {
                    admin.disableHomeKey = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4015, isHomeButtonDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isHomeButtonDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_HOME_KEY);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setBackButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableBackKey != disabled) {
                    admin.disableBackKey = disabled;
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            hwAdminCache.syncHwAdminCache(4017, isBackButtonDisabled(null, userHandle));
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isBackButtonDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_BACK_KEY);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setSysTime(ComponentName who, long millis, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device manager MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                try {
                    getHwActiveAdmin(who, userHandle);
                    long id = Binder.clearCallingIdentity();
                    SystemClock.setCurrentTimeMillis(millis);
                    Binder.restoreCallingIdentity(id);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setCustomSettingsMenu(ComponentName who, List<String> menusToDelete, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                if (menusToDelete != null) {
                    try {
                        if (!menusToDelete.isEmpty()) {
                            String oldMenus = Settings.Global.getStringForUser(this.mContext.getContentResolver(), SETTINGS_MENUS_REMOVE, userHandle);
                            StringBuffer newMenus = new StringBuffer();
                            if (!TextUtils.isEmpty(oldMenus)) {
                                newMenus.append(oldMenus);
                            }
                            for (String menu : menusToDelete) {
                                if (oldMenus == null || !oldMenus.contains(menu)) {
                                    newMenus.append(menu);
                                    newMenus.append(",");
                                }
                            }
                            Settings.Global.putStringForUser(this.mContext.getContentResolver(), SETTINGS_MENUS_REMOVE, newMenus.toString(), userHandle);
                            return;
                        }
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
                Settings.Global.putStringForUser(this.mContext.getContentResolver(), SETTINGS_MENUS_REMOVE, SettingsMDMPlugin.EMPTY_STRING, userHandle);
                Binder.restoreCallingIdentity(callingId);
                return;
            }
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setDefaultLauncher(ComponentName who, String packageName, String className, int userHandle) {
        if (!LauncherUtils.checkPkgAndClassNameValid(packageName, className)) {
            throw new IllegalArgumentException("packageName or className is invalid");
        } else if (LauncherUtils.checkLauncherPermisson(packageName)) {
            Bundle result = getDefaultLauncher(null, userHandle);
            if (result == null || TextUtils.isEmpty(result.getString("value", SettingsMDMPlugin.EMPTY_STRING))) {
                enforceHwCrossUserPermission(userHandle);
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.SDK_LAUNCHER", "Does not have sdk_launcher permission.");
                synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                    if (who != null) {
                        try {
                            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                            if (LauncherUtils.setDefaultLauncher(this.mContext, packageName, className, this.mIPackageManager, userHandle)) {
                                admin.disableChangeLauncher = true;
                                admin.defaultLauncher = packageName + "/" + className;
                                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                                if (this.mHwAdminCache != null) {
                                    this.mHwAdminCache.syncHwAdminCache(4018, isChangeLauncherDisabled(null, userHandle));
                                    Bundle bundle = new Bundle();
                                    bundle.putString("value", admin.defaultLauncher);
                                    this.mHwAdminCache.syncHwAdminCache("set-default-launcher", bundle);
                                }
                            } else {
                                HwLog.w(TAG, "set default launcher failed.");
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    } else {
                        throw new IllegalArgumentException("ComponentName is null");
                    }
                }
                return;
            }
            throw new IllegalArgumentException("the device is already hava third default launcher, you must clear it first");
        } else {
            throw new IllegalArgumentException("The Launcher's signature is different from the host app's!");
        }
    }

    private Bundle getDefaultLauncher(ComponentName who, int userHandle) {
        Bundle bundle = new Bundle();
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                bundle.putString("value", getHwActiveAdmin(who, userHandle).defaultLauncher);
                return bundle;
            }
            Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin.getHwActiveAdmin() != null) {
                    String defalutLauncher = admin.getHwActiveAdmin().defaultLauncher;
                    if (!TextUtils.isEmpty(defalutLauncher)) {
                        bundle.putString("value", defalutLauncher);
                        return bundle;
                    }
                }
            }
            return null;
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void clearDefaultLauncher(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.SDK_LAUNCHER", "Does not have sdk_launcher permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                try {
                    HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.disableChangeLauncher && LauncherUtils.clearDefaultLauncher(this.mContext, this.mIPackageManager, userHandle)) {
                        admin.disableChangeLauncher = false;
                        admin.defaultLauncher = SettingsMDMPlugin.EMPTY_STRING;
                        this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                        if (this.mHwAdminCache != null) {
                            this.mHwAdminCache.syncHwAdminCache(4018, isChangeLauncherDisabled(null, userHandle));
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    public boolean isChangeLauncherDisabled(ComponentName who, int userHandle) {
        return isFunctionDisabled(who, userHandle, DISABLE_CHANGE_LAUNCHER);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bitmap captureScreen(ComponentName who, int userHandle) {
        Bitmap bmp;
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CAPTURE_SCREEN", "Does not have MDM_CAPTURE_SCREEN permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                try {
                    getHwActiveAdmin(who, userHandle);
                    long callingId = Binder.clearCallingIdentity();
                    bmp = CaptureScreenUtils.captureScreen(this.mContext);
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        return bmp;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0070 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00be A[Catch:{ all -> 0x00e6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00d7 A[Catch:{ all -> 0x00e6 }] */
    private void dealApn(ComponentName who, Map<String, String> apnInfo, String apnId, int userHandle, String type) {
        boolean z;
        int hashCode = type.hashCode();
        char c = 65535;
        if (hashCode != -1335458389) {
            if (hashCode != -838846263) {
                if (hashCode == 96417 && type.equals(ADD)) {
                    z = false;
                    if (!z) {
                        if (!z) {
                            if (z) {
                                if (apnInfo == null || apnInfo.isEmpty() || TextUtils.isEmpty(apnId)) {
                                    throw new IllegalArgumentException("apnInfo or apnId is empty.");
                                }
                            } else {
                                return;
                            }
                        } else if (TextUtils.isEmpty(apnId)) {
                            throw new IllegalArgumentException("apnId is empty.");
                        }
                    } else if (apnInfo == null || apnInfo.isEmpty()) {
                        throw new IllegalArgumentException("apnInfo is empty.");
                    }
                    enforceHwCrossUserPermission(userHandle);
                    this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
                    synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                        if (who != null) {
                            try {
                                getHwActiveAdmin(who, userHandle);
                                long callingId = Binder.clearCallingIdentity();
                                int hashCode2 = type.hashCode();
                                if (hashCode2 != -1335458389) {
                                    if (hashCode2 != -838846263) {
                                        if (hashCode2 == 96417 && type.equals(ADD)) {
                                            c = 0;
                                            if (c != 0) {
                                                ApnUtils.addApn(this.mContext.getContentResolver(), apnInfo);
                                            } else if (c == 1) {
                                                ApnUtils.deleteApn(this.mContext.getContentResolver(), apnId);
                                            } else if (c == 2) {
                                                ApnUtils.updateApn(this.mContext.getContentResolver(), apnInfo, apnId);
                                            }
                                            Binder.restoreCallingIdentity(callingId);
                                        }
                                    } else if (type.equals(UPDATE)) {
                                        c = 2;
                                        if (c != 0) {
                                        }
                                        Binder.restoreCallingIdentity(callingId);
                                    }
                                } else if (type.equals(DELETE)) {
                                    c = 1;
                                    if (c != 0) {
                                    }
                                    Binder.restoreCallingIdentity(callingId);
                                }
                                if (c != 0) {
                                }
                                Binder.restoreCallingIdentity(callingId);
                            } catch (Throwable th) {
                                throw th;
                            }
                        } else {
                            throw new IllegalArgumentException("ComponentName is null");
                        }
                    }
                    return;
                }
            } else if (type.equals(UPDATE)) {
                z = true;
                if (!z) {
                }
                enforceHwCrossUserPermission(userHandle);
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
                synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                }
            }
        } else if (type.equals(DELETE)) {
            z = true;
            if (!z) {
            }
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            }
        }
        z = true;
        if (!z) {
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void addApn(ComponentName who, Map<String, String> apnInfo, int userHandle) {
        dealApn(who, apnInfo, null, userHandle, ADD);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void deleteApn(ComponentName who, String apnId, int userHandle) {
        dealApn(who, null, apnId, userHandle, DELETE);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void updateApn(ComponentName who, Map<String, String> apnInfo, String apnId, int userHandle) {
        dealApn(who, apnInfo, apnId, userHandle, UPDATE);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setPreferApn(ComponentName who, String apnId, int userHandle) {
        if (!TextUtils.isEmpty(apnId)) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (who != null) {
                    try {
                        getHwActiveAdmin(who, userHandle);
                        long callingId = Binder.clearCallingIdentity();
                        ApnUtils.setPreferApn(this.mContext.getContentResolver(), apnId);
                        Binder.restoreCallingIdentity(callingId);
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
            return;
        }
        throw new IllegalArgumentException("apnId is empty.");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> queryApn(ComponentName who, Map<String, String> apnInfo, int userHandle) {
        List<String> ids;
        if (apnInfo == null || apnInfo.isEmpty()) {
            throw new IllegalArgumentException("apnInfo is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                try {
                    getHwActiveAdmin(who, userHandle);
                    long callingId = Binder.clearCallingIdentity();
                    ids = ApnUtils.queryApn(this.mContext.getContentResolver(), apnInfo);
                    Binder.restoreCallingIdentity(callingId);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        return ids;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Map<String, String> getApnInfo(ComponentName who, String apnId, int userHandle) {
        Map<String, String> apnInfo;
        if (!TextUtils.isEmpty(apnId)) {
            enforceHwCrossUserPermission(userHandle);
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (who != null) {
                    try {
                        getHwActiveAdmin(who, userHandle);
                        long callingId = Binder.clearCallingIdentity();
                        apnInfo = ApnUtils.getApnInfo(this.mContext.getContentResolver(), apnId);
                        Binder.restoreCallingIdentity(callingId);
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
            return apnInfo;
        }
        throw new IllegalArgumentException("apnId is empty.");
    }

    private void dealNetworkAccessWhitelist(ComponentName who, List<String> addrList, int userHandle, boolean isAdd) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have network_manager MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidIpAddrs(addrList)) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (who != null) {
                    HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (isAdd) {
                        if (admin.networkAccessWhitelist == null) {
                            admin.networkAccessWhitelist = new ArrayList();
                        }
                        HwDevicePolicyManagerServiceUtil.isAddrOverLimit(admin.networkAccessWhitelist, addrList);
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.networkAccessWhitelist, addrList);
                    } else {
                        HwDevicePolicyManagerServiceUtil.removeItemsFromList(admin.networkAccessWhitelist, addrList);
                    }
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                    setNetworkAccessWhitelist(admin.networkAccessWhitelist);
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
            HwAdminCache hwAdminCache = this.mHwAdminCache;
            if (hwAdminCache != null) {
                hwAdminCache.syncHwAdminCache(4010, getNetworkAccessWhitelist(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("addrlist invalid");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void addNetworkAccessWhitelist(ComponentName who, List<String> addrList, int userHandle) {
        dealNetworkAccessWhitelist(who, addrList, userHandle, true);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void removeNetworkAccessWhitelist(ComponentName who, List<String> addrList, int userHandle) {
        dealNetworkAccessWhitelist(who, addrList, userHandle, false);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> getNetworkAccessWhitelist(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            List<String> list = null;
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.networkAccessWhitelist != null) {
                    if (!admin.networkAccessWhitelist.isEmpty()) {
                        list = admin.networkAccessWhitelist;
                    }
                }
                return list;
            }
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            ArrayList<String> addrList = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin2.getHwActiveAdmin() != null) {
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(addrList, admin2.getHwActiveAdmin().networkAccessWhitelist);
                }
            }
            if (!addrList.isEmpty()) {
                list = addrList;
            }
            return list;
        }
    }

    private void setNetworkAccessWhitelist(List<String> whitelist) {
        IBinder binder = ServiceManager.getService("network_management");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (binder != null) {
            try {
                data.writeInterfaceToken("android.os.INetworkManagementService");
                data.writeStringList(whitelist);
                binder.transact(1106, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                HwLog.e(TAG, "setNetworkAccessWhitelist error");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean getHwAdminCachedValue(int code) {
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            return hwAdminCache.getCachedValue(code);
        }
        return false;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public List<String> getHwAdminCachedList(int code) {
        List<String> result = null;
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            result = hwAdminCache.getCachedList(code);
        }
        return result == null ? new ArrayList() : result;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle getHwAdminCachedBundle(String policyName) {
        HwAdminCache hwAdminCache = this.mHwAdminCache;
        if (hwAdminCache != null) {
            return hwAdminCache.getCachedBundle(policyName);
        }
        return null;
    }

    private void enforceUserRestrictionPermission(ComponentName who, String key, int userHandle) {
        long id = Binder.clearCallingIdentity();
        try {
            UserInfo info = this.mUserManager.getUserInfo(userHandle);
            if (info == null) {
                throw new IllegalArgumentException("Invalid user: " + userHandle);
            } else if (info.isGuest()) {
                throw new IllegalStateException("Cannot call this method on a guest");
            } else if (who == null) {
                throw new IllegalArgumentException("Component is null");
            } else if (userHandle != 0 && HWDEVICE_OWNER_USER_RESTRICTIONS.contains(key)) {
                throw new SecurityException("Cannot set user restriction " + key);
            }
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    private HwActiveAdmin getHwActiveAdmin(ComponentName who, int userHandle) {
        HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle);
        if (admin == null) {
            throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
        } else if (admin.getUid() == Binder.getCallingUid()) {
            HwActiveAdmin hwadmin = admin.getHwActiveAdmin();
            if (hwadmin != null) {
                return hwadmin;
            }
            HwActiveAdmin hwadmin2 = new HwActiveAdminImpl();
            admin.setHwActiveAdmin(hwadmin2);
            return hwadmin2;
        } else {
            throw new SecurityException("Admin " + who + " is not owned by uid " + Binder.getCallingUid());
        }
    }

    private HwActiveAdmin getHwActiveAdminEx(ComponentName who, int userHandle) {
        HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle);
        if (admin != null) {
            HwActiveAdmin hwAdmin = admin.getHwActiveAdmin();
            if (hwAdmin != null) {
                return hwAdmin;
            }
            HwActiveAdmin hwAdmin2 = new HwActiveAdminImpl();
            admin.setHwActiveAdmin(hwAdmin2);
            return hwAdmin2;
        }
        throw new SecurityException("No active admin, ComponentName:" + who);
    }

    private void setHwUserRestriction(String key, boolean disable, int userHandle) {
        UserHandle user = new UserHandle(userHandle);
        boolean isAlreadyRestricted = this.mUserManager.hasUserRestriction(key, user);
        long id = Binder.clearCallingIdentity();
        if (disable && !isAlreadyRestricted) {
            try {
                if ("no_config_tethering".equals(key)) {
                    cancelNotificationAsUser();
                }
                if ("no_physical_media".equals(key)) {
                    boolean isHasExternalSdcard = StorageUtils.hasExternalSdcard(this.mContext);
                    boolean isDafaultIsSdcard = DefaultStorageLocation.isSdcard();
                    if (isHasExternalSdcard && !isDafaultIsSdcard) {
                        HwLog.w(TAG, "call doUnMount");
                        StorageUtils.doUnMount(this.mContext);
                    }
                    if (isHasExternalSdcard && isDafaultIsSdcard) {
                        if (StorageUtils.isSwitchPrimaryVolumeSupported()) {
                            throw new IllegalStateException("could not disable sdcard when it is primary card.");
                        }
                    }
                }
                if ("no_usb_file_transfer".equals(key)) {
                    if (disable) {
                        Settings.Global.putStringForUser(this.mContext.getContentResolver(), "adb_enabled", DYNAMIC_ROOT_STATE_SAFE, userHandle);
                    }
                    this.mUserManager.setUserRestriction("no_debugging_features", true, user);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(id);
                throw th;
            }
        }
        this.mUserManager.setUserRestriction(key, disable, user);
        if ("no_usb_file_transfer".equals(key) && !disable) {
            this.mUserManager.setUserRestriction("no_debugging_features", false, user);
        }
        Binder.restoreCallingIdentity(id);
        sendHwChangedNotification(userHandle);
    }

    private void cancelNotificationAsUser() {
        if (((WifiManager) this.mContext.getSystemService("wifi")).isWifiApEnabled()) {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).stopTethering(0);
            ((NotificationManager) this.mContext.getSystemService("notification")).cancelAsUser(null, 17303610, UserHandle.ALL);
        }
    }

    private void sendHwChangedNotification(int userHandle) {
        Intent intent = new Intent("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intent.setFlags(1073741824);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, new UserHandle(userHandle));
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean haveHwUserRestriction(String key, int userid) {
        UserHandle user = new UserHandle(userid);
        user.getIdentifier();
        return this.mUserManager.hasUserRestriction(key, user);
    }

    public void syncHwDeviceSettingsLocked(int userHandle) {
        if (userHandle != 0) {
            HwLog.w(TAG, "userHandle is not USER_OWNER, return ");
            return;
        }
        HwLog.i(TAG, "syncHwDeviceSettingsLocked, userHandle is " + userHandle);
        combineAllPolicies(userHandle, true);
        try {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                for (String restriction : HWDEVICE_OWNER_USER_RESTRICTIONS) {
                    hwSyncDeviceCapabilitiesLocked(restriction, userHandle);
                }
            }
            hwSyncDeviceStatusBarLocked(userHandle);
        } catch (SecurityException e) {
            HwLog.e(TAG, "syncHwDeviceSettingsLocked SecurityException is happened");
        } catch (Exception e2) {
            HwLog.e(TAG, "syncHwDeviceSettingsLocked exception is happened");
        }
        try {
            syncHwAdminCache(userHandle);
        } catch (Exception e3) {
            HwLog.e(TAG, "syncHwAdminCache exception is happened");
        }
        sendPersistentAppToIAware(userHandle);
    }

    private void syncHwAdminSafeModeCache(int userHandle) {
        if (this.mHwAdminCache != null) {
            boolean isDisableSafeMode = isSafeModeDisabled(null, userHandle);
            this.mHwAdminCache.syncHwAdminCache(4011, isDisableSafeMode);
            long identityToken = Binder.clearCallingIdentity();
            int value = 1;
            if (!isDisableSafeMode) {
                value = 0;
            }
            Settings.Global.putInt(this.mContext.getContentResolver(), "isSafeModeDisabled", value);
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void syncHwAdminCacheForBoolean(int userHandle) {
        this.mHwAdminCache.syncHwAdminCache(4001, isWifiDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4009, isBluetoothDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4002, isVoiceDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4003, isInstallSourceDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4012, isAdbDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4013, isUSBOtgDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4014, isGPSDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4015, isHomeButtonDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4016, isTaskButtonDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4017, isBackButtonDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4018, isChangeLauncherDisabled(null, userHandle));
        for (Map.Entry<Integer, String> entry : PolicyConstant.getPolicyBundle2BooleanMap().entrySet()) {
            Bundle bundle = getPolicy(null, entry.getValue(), userHandle);
            if (bundle != null) {
                this.mHwAdminCache.syncHwAdminCache(entry.getKey().intValue(), bundle.getBoolean("value"));
            }
        }
        this.mHwAdminCache.syncHwAdminCache(5021, getPolicy(null, "wifi_p2p_item_policy_name", userHandle).getBoolean("wifi_p2p_policy_item_value"));
        this.mHwAdminCache.syncHwAdminCache(5022, getPolicy(null, "infrared_item_policy_name", userHandle).getBoolean("infrared_item_policy_value"));
    }

    private void syncHwAdminCacheForList(int userHandle) {
        this.mHwAdminCache.syncHwAdminCache(4004, getInstallPackageSourceWhiteList(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4005, getPersistentApp(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4006, getDisallowedRunningApp(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4007, getInstallPackageWhiteList(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4008, getDisallowedUninstallPackageList(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4019, getDisabledDeactivateMdmPackageList(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4010, getNetworkAccessWhitelist(null, userHandle));
        for (Map.Entry<Integer, String> entry : PolicyConstant.getPolicyBundle2ListMap().entrySet()) {
            Bundle bundle = getPolicy(null, entry.getValue(), userHandle);
            if (bundle != null) {
                try {
                    this.mHwAdminCache.syncHwAdminCache(entry.getKey().intValue(), bundle.getStringArrayList("value"));
                } catch (ArrayIndexOutOfBoundsException e) {
                    HwLog.e(TAG, "syncHwAdminCacheForList exception.");
                }
            }
        }
    }

    private void syncHwAdminCacheForBundle(int userHandle) {
        String[] bundleArray = PolicyConstant.getPolicyBundleArray();
        for (int i = 0; i < bundleArray.length; i++) {
            this.mHwAdminCache.syncHwAdminCache(bundleArray[i], getPolicy(null, bundleArray[i], userHandle));
        }
    }

    private void syncHwAdminCache(int userHandle) {
        HwLog.i(TAG, "syncHwAdminCache, userHandle is " + userHandle);
        if (this.mHwAdminCache == null) {
            this.mHwAdminCache = new HwAdminCache();
        }
        syncHwAdminCacheForBoolean(userHandle);
        syncHwAdminCacheForList(userHandle);
        syncHwAdminCacheForBundle(userHandle);
        syncHwAdminSafeModeCache(userHandle);
    }

    private void hwSyncDeviceCapabilitiesLocked(String restriction, int userHandle) {
        boolean isDisabled = false;
        boolean isAlreadyRestricted = haveHwUserRestriction(restriction, userHandle);
        Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
        while (true) {
            if (it.hasNext()) {
                if (isUserRestrictionDisabled(restriction, ((HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next()).getHwActiveAdmin())) {
                    isDisabled = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (isDisabled != isAlreadyRestricted) {
            setHwUserRestriction(restriction, isDisabled, userHandle);
        }
    }

    private void hwSyncDeviceStatusBarLocked(int userHandle) {
        if (isStatusBarExpandPanelDisabled(null, userHandle)) {
            setStatusBarPanelDisabledInternal(userHandle);
        } else {
            setStatusBarPanelEnableInternal(true, userHandle);
        }
    }

    private boolean isUserRestrictionDisabled(String restriction, HwActiveAdmin admin) {
        if (admin == null) {
            return false;
        }
        if ("no_usb_file_transfer".equals(restriction) && admin.disableUSBData) {
            return true;
        }
        if ("no_outgoing_calls".equals(restriction) && admin.disableVoice) {
            return true;
        }
        if ("no_sms".equals(restriction) && admin.disableSMS) {
            return true;
        }
        if ("no_config_tethering".equals(restriction) && admin.disableWifiAp) {
            return true;
        }
        if (!"no_physical_media".equals(restriction) || !admin.disableExternalStorage) {
            return false;
        }
        return true;
    }

    private void installPackage(String packagePath, final String installerPackageName) {
        if (!TextUtils.isEmpty(packagePath)) {
            long ident = Binder.clearCallingIdentity();
            try {
                final File tempFile = new File(packagePath.trim()).getCanonicalFile();
                if (!tempFile.getName().endsWith(".apk")) {
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                Uri packageUri = Uri.fromFile(tempFile);
                ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).installPackageAsUser(packageUri.getPath(), new PackageInstallObserver() {
                    /* class com.android.server.devicepolicy.HwDevicePolicyManagerService.AnonymousClass4 */

                    public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
                        BdReportUtils.reportInstallPkgData(installerPackageName, basePackageName, HwDevicePolicyManagerService.this.mContext);
                        if (returnCode != 1) {
                            HwLog.e(HwDevicePolicyManagerService.TAG, "The package " + tempFile.getName() + "installed failed, error code: " + returnCode);
                        }
                    }
                }.getBinder(), 2, installerPackageName, 0);
                Binder.restoreCallingIdentity(ident);
            } catch (IOException e) {
                HwLog.e(TAG, "Get canonical file failed for package path: " + packagePath + ", error: " + e.getMessage());
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Install package path is empty");
        }
    }

    private void uninstallPackage(String packageName, boolean keepData) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("Uninstall package name is empty");
        } else if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            long ident = Binder.clearCallingIdentity();
            try {
                PackageManager pm = this.mContext.getPackageManager();
                int i = 0;
                if (pm.getApplicationInfo(packageName, 0) == null) {
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                if (keepData) {
                    i = 1;
                }
                pm.deletePackage(packageName, null, i);
                Binder.restoreCallingIdentity(ident);
            } catch (PackageManager.NameNotFoundException e) {
                HwLog.e(TAG, "Name not found for package: " + packageName);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
        }
    }

    private void enforceCheckNotActiveAdminApp(String packageName, int userHandle) {
        if (((DevicePolicyManager) this.mContext.getSystemService("device_policy")).packageHasActiveAdmins(packageName, userHandle)) {
            throw new IllegalArgumentException("could not operate active admin app");
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int getSDCardEncryptionStatus() {
        char c = 65535;
        if (!IS_SUPPORT_CRYPT) {
            return -1;
        }
        String sdStatus = SystemProperties.get("vold.cryptsd.state");
        switch (sdStatus.hashCode()) {
            case -1512632483:
                if (sdStatus.equals("encrypting")) {
                    c = 1;
                    break;
                }
                break;
            case -1298848381:
                if (sdStatus.equals("enable")) {
                    c = 4;
                    break;
                }
                break;
            case -1212575282:
                if (sdStatus.equals("mismatch")) {
                    c = 5;
                    break;
                }
                break;
            case 395619662:
                if (sdStatus.equals("wait_unlock")) {
                    c = 6;
                    break;
                }
                break;
            case 1671308008:
                if (sdStatus.equals("disable")) {
                    c = 3;
                    break;
                }
                break;
            case 1959784951:
                if (sdStatus.equals("invalid")) {
                    c = 0;
                    break;
                }
                break;
            case 2066069301:
                if (sdStatus.equals("decrypting")) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return 0;
            case 1:
                return 3;
            case 2:
                return 4;
            case 3:
                return 1;
            case SD_CRYPT_STATE_DECRYPTING /* 4 */:
                return 2;
            case SD_CRYPT_STATE_MISMATCH /* 5 */:
                return 5;
            case SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                return 6;
            default:
                return 0;
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setSDCardDecryptionDisabled(ComponentName who, boolean disabled, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                try {
                    HwActiveAdmin admin = getHwActiveAdminForCallerLocked(who);
                    if (admin.disableDecryptSDCard != disabled) {
                        admin.disableDecryptSDCard = disabled;
                        this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isSDCardDecryptionDisabled(ComponentName who, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                return getHwActiveAdminUncheckedLocked(who, userHandle).disableDecryptSDCard;
            }
            Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (admin.getHwActiveAdmin() != null && admin.getHwActiveAdmin().disableDecryptSDCard) {
                    return true;
                }
            }
            return false;
        }
    }

    private HwActiveAdmin getHwActiveAdminUncheckedLocked(ComponentName who, int userHandle) {
        HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle);
        if (admin != null) {
            HwActiveAdmin hwadmin = admin.getHwActiveAdmin();
            if (hwadmin != null) {
                return hwadmin;
            }
            HwActiveAdmin hwadmin2 = new HwActiveAdminImpl();
            admin.setHwActiveAdmin(hwadmin2);
            return hwadmin2;
        }
        throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
    }

    private HwActiveAdmin getHwActiveAdminForCallerLocked(ComponentName who) {
        HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = this.mHwDevicePolicyManagerInner.getActiveAdminForCallerLockedInner(who, 7);
        if (admin != null) {
            HwActiveAdmin hwadmin = admin.getHwActiveAdmin();
            if (hwadmin != null) {
                return hwadmin;
            }
            HwActiveAdmin hwadmin2 = new HwActiveAdminImpl();
            admin.setHwActiveAdmin(hwadmin2);
            return hwadmin2;
        }
        throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
    }

    public void init() {
        if (!this.isHasInit) {
            HwLog.d(TAG, "init, isHasInit is " + this.isHasInit);
            Iterator<PolicyStruct> it = this.globalStructs.iterator();
            while (it.hasNext()) {
                PolicyStruct struct = it.next();
                if (struct != null) {
                    struct.getOwner().init(struct);
                }
            }
            this.isHasInit = true;
        }
    }

    private void addDevicePolicyPlugins(Context context) {
        addPlugin(new DeviceNetworkPlugin(context));
        addPlugin(new DeviceRestrictionPlugin(context));
        addPlugin(new HwSystemManagerPlugin(context));
        addPlugin(new PhoneManagerPlugin(context));
        addPlugin(new HwEmailMDMPlugin(context));
        addPlugin(new DeviceVpnManagerImpl(context));
        addPlugin(new DeviceFirewallManagerImpl(context));
        addPlugin(new DeviceApplicationPlugin(context));
        addPlugin(new DeviceTelephonyPlugin(context));
        addPlugin(new DeviceCameraPlugin(context));
        addPlugin(new DeviceStorageManagerPlugin(context));
        addPlugin(new DevicePasswordPlugin(context));
        addPlugin(new SettingsMDMPlugin(context));
        addPlugin(new DeviceWifiPlugin(context));
        addPlugin(new DeviceBluetoothPlugin(context));
        addPlugin(new DeviceLocationPlugin(context));
        addPlugin(new DeviceP2PPlugin(context));
        addPlugin(new DeviceInfraredPlugin(context));
        addPlugin(new DeviceControlPlugin(context));
        addPlugin(new DevicePackageManagerPlugin(context));
        addPlugin(new FrameworkTestPlugin(context));
    }

    private void addPlugin(DevicePolicyPlugin plugin) {
        if (plugin != null) {
            this.globalPlugins.add(plugin);
        }
    }

    private void addPolicyStruct(PolicyStruct struct) {
        if (struct != null) {
            this.globalStructs.add(struct);
            for (PolicyStruct.PolicyItem item : struct.getPolicyItems()) {
                GLOBAL_POLICY_ITEMS.put(item.getPolicyName(), item);
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void bdReport(int eventID, String eventMsg) {
        Context context = this.mContext;
        if (context != null) {
            Flog.bdReport(context, eventID, eventMsg);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int setPolicy(ComponentName who, String policyName, Bundle policyData, int userHandle) {
        String str;
        PolicyStruct.PolicyItem combinedItem;
        boolean z;
        boolean isSetPolicyResult;
        StringBuilder sb = new StringBuilder();
        sb.append("setPolicy, policyName = ");
        sb.append(policyName);
        sb.append(", caller :");
        if (who == null) {
            str = "null";
        } else {
            str = who.flattenToString();
        }
        sb.append(str);
        HwLog.i(TAG, sb.toString());
        if (who != null) {
            DevicePolicyPlugin plugin = findPluginByPolicyName(policyName);
            if (plugin == null) {
                HwLog.e(TAG, "no plugin found, pluginName = " + policyName + ", caller :" + who.flattenToString());
                return -1;
            } else if (!plugin.checkCallingPermission(who, policyName)) {
                HwLog.e(TAG, "permission denied: " + who.flattenToString());
                return -1;
            } else {
                boolean isGolbalPolicyChanged = false;
                PolicyStruct struct = findPolicyStructByPolicyName(policyName);
                synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                    try {
                        HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                        PolicyStruct.PolicyItem newItem = null;
                        boolean z2 = true;
                        if (struct != null) {
                            PolicyStruct.PolicyItem item = (PolicyStruct.PolicyItem) admin.adminPolicyItems.get(policyName);
                            PolicyStruct.PolicyItem oldItem = struct.getItemByPolicyName(policyName);
                            if (oldItem == null) {
                                try {
                                    StringBuilder sb2 = new StringBuilder();
                                    try {
                                        sb2.append("no policy item found, pluginName = ");
                                        sb2.append(policyName);
                                        sb2.append(", caller :");
                                        sb2.append(who.flattenToString());
                                        HwLog.e(TAG, sb2.toString());
                                        return -1;
                                    } catch (Throwable th) {
                                        combinedItem = th;
                                        throw combinedItem;
                                    }
                                } catch (Throwable th2) {
                                    combinedItem = th2;
                                    throw combinedItem;
                                }
                            } else {
                                try {
                                    newItem = new PolicyStruct.PolicyItem(policyName, oldItem.getItemType(), struct);
                                    if (item == null) {
                                        newItem.copyFrom(oldItem);
                                        newItem.addAttrValues(newItem, policyData);
                                    } else {
                                        newItem.deepCopyFrom(item);
                                    }
                                    newItem.addAttrValues(newItem, policyData);
                                    PolicyStruct.PolicyItem combinedItem2 = combinePoliciesWithPolicyChanged(who, newItem, policyName, userHandle);
                                    PolicyStruct.PolicyItem globalItem = GLOBAL_POLICY_ITEMS.get(policyName);
                                    if (globalItem == null) {
                                        try {
                                            HwLog.e(TAG, "no policy item found, pluginName = " + policyName + ", caller :" + who.flattenToString());
                                            return -1;
                                        } catch (Throwable th3) {
                                            combinedItem = th3;
                                            throw combinedItem;
                                        }
                                    } else if (globalItem.equals(combinedItem2)) {
                                        newItem.setGlobalPolicyChanged(2);
                                        isGolbalPolicyChanged = false;
                                    } else {
                                        newItem.setGlobalPolicyChanged(1);
                                        isGolbalPolicyChanged = true;
                                    }
                                } catch (Throwable th4) {
                                    combinedItem = th4;
                                    throw combinedItem;
                                }
                            }
                        }
                        HwLog.d(TAG, "when setPolicy, is global PolicyChanged ? = " + isGolbalPolicyChanged);
                        boolean isSetPolicyResult2 = plugin.onSetPolicy(who, policyName, policyData, isGolbalPolicyChanged);
                        if (!isSetPolicyResult2) {
                            HwLog.e(TAG, "onSetPolicy failed, pluginName = " + policyName + ", caller :" + who.flattenToString());
                            if (!isGolbalPolicyChanged || -1 != 1) {
                                z2 = false;
                            }
                            try {
                                plugin.onSetPolicyCompleted(who, policyName, z2);
                                return -1;
                            } catch (Throwable th5) {
                                combinedItem = th5;
                                throw combinedItem;
                            }
                        } else {
                            admin.adminPolicyItems.put(policyName, newItem);
                            if (newItem.getItemType() == PolicyStruct.PolicyType.CONFIGURATION) {
                                GLOBAL_POLICY_ITEMS.put(policyName, newItem);
                                Iterator<PolicyStruct> it = this.globalStructs.iterator();
                                while (true) {
                                    if (!it.hasNext()) {
                                        break;
                                    }
                                    boolean isFound = false;
                                    Iterator it2 = it.next().getPolicyMap().keySet().iterator();
                                    while (true) {
                                        if (!it2.hasNext()) {
                                            isSetPolicyResult = isSetPolicyResult2;
                                            break;
                                        }
                                        isSetPolicyResult = isSetPolicyResult2;
                                        if (newItem.getPolicyName().equals((String) it2.next())) {
                                            struct.addPolicyItem(newItem);
                                            isFound = true;
                                            break;
                                        }
                                        isSetPolicyResult2 = isSetPolicyResult;
                                    }
                                    if (isFound) {
                                        break;
                                    }
                                    isSetPolicyResult2 = isSetPolicyResult;
                                }
                            }
                            this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                            z = false;
                            combineAllPolicies(userHandle, false);
                        }
                    } catch (Throwable th6) {
                        combinedItem = th6;
                        throw combinedItem;
                    }
                }
                if (1 == 1) {
                    this.mHwAdminCache.syncHwAdminCache(policyName, getPolicy(null, policyName, userHandle));
                }
                if (isGolbalPolicyChanged && 1 == 1) {
                    z = true;
                }
                plugin.onSetPolicyCompleted(who, policyName, z);
                return 1;
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle getPolicy(ComponentName who, String policyName, int userHandle) {
        String str;
        Bundle resultBundle;
        String str2;
        DevicePolicyPlugin plugin = findPluginByPolicyName(policyName);
        if (plugin == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("no plugin found, policyName = ");
            sb.append(policyName);
            sb.append(", caller :");
            if (who == null) {
                str2 = "null";
            } else {
                str2 = who.flattenToString();
            }
            sb.append(str2);
            HwLog.e(TAG, sb.toString());
            return null;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("get :");
        sb2.append(policyName);
        if (who == null) {
            str = SettingsMDMPlugin.EMPTY_STRING;
        } else {
            str = " ,cal :" + who.flattenToString();
        }
        sb2.append(str);
        HwLog.d(TAG, sb2.toString());
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who == null) {
                PolicyStruct.PolicyItem item = GLOBAL_POLICY_ITEMS.get(policyName);
                if (!userIsolationPolicyList.contains(policyName)) {
                    if (item == null || !item.isSuppportMultipleUsers()) {
                        if (GLOBAL_POLICY_ITEMS.get(policyName) != null) {
                            resultBundle = GLOBAL_POLICY_ITEMS.get(policyName).combineAllAttributes();
                        } else {
                            resultBundle = null;
                        }
                    }
                }
                resultBundle = combinePoliciesAsUser(policyName, userHandle).combineAllAttributes();
            } else {
                PolicyStruct.PolicyItem item2 = (PolicyStruct.PolicyItem) getHwActiveAdminUncheckedLocked(who, userHandle).adminPolicyItems.get(policyName);
                if ((TAG_POLICY_SYS_APP.equals(policyName) || TAG_POLICY_UNDETACHABLE_SYS_APP.equals(policyName)) && GLOBAL_POLICY_ITEMS.get(policyName) != null) {
                    resultBundle = GLOBAL_POLICY_ITEMS.get(policyName).combineAllAttributes();
                } else if (item2 != null) {
                    resultBundle = item2.combineAllAttributes();
                } else {
                    resultBundle = null;
                }
            }
            notifyOnGetPolicy(plugin, who, policyName, resultBundle);
        }
        return resultBundle;
    }

    private void notifyOnGetPolicy(DevicePolicyPlugin plugin, ComponentName who, String policyName, Bundle policyData) {
        plugin.onGetPolicy(who, policyName, policyData);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x015c, code lost:
        if (r6 == 1) goto L_0x0160;
     */
    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int removePolicy(ComponentName who, String policyName, Bundle policyData, int userHandle) {
        String str;
        boolean isGolbalPolicyChanged;
        int result;
        boolean z;
        StringBuilder sb = new StringBuilder();
        sb.append("removePolicy, policyName = ");
        sb.append(policyName);
        sb.append(", caller :");
        if (who == null) {
            str = "null";
        } else {
            str = who.flattenToString();
        }
        sb.append(str);
        HwLog.i(TAG, sb.toString());
        if (who != null) {
            DevicePolicyPlugin plugin = findPluginByPolicyName(policyName);
            if (plugin == null) {
                HwLog.e(TAG, "no plugin found, pluginName = " + policyName + ", caller :" + who.flattenToString());
                return -1;
            } else if (!plugin.checkCallingPermission(who, policyName)) {
                HwLog.e(TAG, "permission denied: " + who.flattenToString());
                return -1;
            } else {
                PolicyStruct struct = findPolicyStructByPolicyName(policyName);
                synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                    HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    PolicyStruct.PolicyItem item = null;
                    PolicyStruct.PolicyItem newItem = null;
                    if (struct != null) {
                        item = (PolicyStruct.PolicyItem) admin.adminPolicyItems.get(policyName);
                        if (item != null) {
                            newItem = new PolicyStruct.PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType(), struct);
                            newItem.deepCopyFrom(item);
                            newItem.removeAttrValues(newItem, policyData);
                            PolicyStruct.PolicyItem combinedItem = combinePoliciesWithPolicyChanged(who, newItem, policyName, userHandle);
                            PolicyStruct.PolicyItem globalItem = GLOBAL_POLICY_ITEMS.get(policyName);
                            if (globalItem == null) {
                                newItem.setGlobalPolicyChanged(0);
                                isGolbalPolicyChanged = false;
                            } else if (globalItem.equals(combinedItem)) {
                                newItem.setGlobalPolicyChanged(2);
                                isGolbalPolicyChanged = false;
                            } else {
                                newItem.setGlobalPolicyChanged(1);
                                isGolbalPolicyChanged = true;
                            }
                        } else {
                            isGolbalPolicyChanged = false;
                        }
                    } else {
                        isGolbalPolicyChanged = false;
                    }
                    HwLog.d(TAG, "when removePolicy, is global PolicyChanged ? = " + isGolbalPolicyChanged);
                    if (plugin.onRemovePolicy(who, policyName, policyData, isGolbalPolicyChanged)) {
                        if (!((item == null || policyData == null) ? false : true) || !(item.getItemType() == PolicyStruct.PolicyType.LIST || item.getItemType() == PolicyStruct.PolicyType.CONFIGLIST)) {
                            admin.adminPolicyItems.remove(policyName);
                        } else {
                            admin.adminPolicyItems.put(policyName, newItem);
                        }
                        this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                        combineAllPolicies(userHandle, true);
                        result = 1;
                    } else {
                        HwLog.e(TAG, "onSetPolicy failed, pluginName = " + policyName + ", caller :" + who.flattenToString());
                        result = -1;
                    }
                }
                if (result == 1) {
                    this.mHwAdminCache.syncHwAdminCache(policyName, getPolicy(null, policyName, userHandle));
                }
                if (isGolbalPolicyChanged) {
                    z = true;
                }
                z = false;
                plugin.onRemovePolicyCompleted(who, policyName, z);
                return result;
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    public void notifyPlugins(ComponentName who, int userHandle) {
        StringBuilder sb = new StringBuilder();
        sb.append("notifyPlugins: ");
        sb.append(who == null ? "null" : who.flattenToString());
        sb.append(" userId: ");
        sb.append(userHandle);
        HwLog.i(TAG, sb.toString());
        HwDevicePolicyManagerInnerEx.ActiveAdminEx activeAdminToRemove = this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle);
        if (!(activeAdminToRemove == null || activeAdminToRemove.getHwActiveAdmin() == null)) {
            if (activeAdminToRemove.getHwActiveAdmin().disableChangeLauncher) {
                LauncherUtils.clearDefaultLauncher(this.mContext, this.mIPackageManager, userHandle);
            }
            if (!(activeAdminToRemove.getHwActiveAdmin().adminPolicyItems == null || activeAdminToRemove.getHwActiveAdmin().adminPolicyItems.isEmpty())) {
                Iterator<PolicyStruct> it = this.globalStructs.iterator();
                while (it.hasNext()) {
                    PolicyStruct struct = it.next();
                    if (struct != null) {
                        ArrayList<PolicyStruct.PolicyItem> removedPluginItems = new ArrayList<>();
                        for (PolicyStruct.PolicyItem removedItem : activeAdminToRemove.getHwActiveAdmin().adminPolicyItems.values()) {
                            if (removedItem != null) {
                                PolicyStruct.PolicyItem combinedItem = combinePoliciesWithoutRemovedPolicyItem(who, removedItem.getPolicyName(), userHandle);
                                if (GLOBAL_POLICY_ITEMS.get(removedItem.getPolicyName()) == null) {
                                    removedItem.setGlobalPolicyChanged(0);
                                } else if (removedItem.equals(combinedItem)) {
                                    removedItem.setGlobalPolicyChanged(2);
                                } else {
                                    removedItem.setGlobalPolicyChanged(1);
                                    GLOBAL_POLICY_ITEMS.put(removedItem.getPolicyName(), combinedItem);
                                }
                                if (struct.containsPolicyName(removedItem.getPolicyName())) {
                                    removedPluginItems.add(removedItem);
                                }
                            }
                        }
                        notifyPlugins(who, removedPluginItems, struct, userHandle);
                    }
                }
            }
        }
    }

    private void notifyPlugins(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPluginItems, PolicyStruct struct, int userHandle) {
        if (!removedPluginItems.isEmpty()) {
            DevicePolicyPlugin plugin = struct.getOwner();
            if (plugin == null) {
                HwLog.w(TAG, " policy struct has no owner");
                return;
            }
            this.effectedItems.add(new EffectedItem(who, plugin, removedPluginItems));
            HwLog.i(TAG, "onActiveAdminRemoving, userHandle: " + userHandle);
            plugin.onActiveAdminRemoved(who, removedPluginItems, userHandle);
        }
    }

    public void removeActiveAdminCompleted(ComponentName who) {
        StringBuilder sb = new StringBuilder();
        sb.append("removeActiveAdminCompleted: ");
        sb.append(who == null ? "null" : who.flattenToString());
        HwLog.i(TAG, sb.toString());
        if (who != null) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (!this.effectedItems.isEmpty()) {
                    Iterator<EffectedItem> it = this.effectedItems.iterator();
                    while (it.hasNext()) {
                        EffectedItem effectedItem = it.next();
                        DevicePolicyPlugin plugin = effectedItem.effectedPlugin;
                        if (plugin != null) {
                            if (who.equals(effectedItem.effectedAdmin)) {
                                plugin.onActiveAdminRemovedCompleted(effectedItem.effectedAdmin, effectedItem.effectedPolicies);
                                it.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    private DevicePolicyPlugin findPluginByPolicyName(String policyName) {
        PolicyStruct struct = findPolicyStructByPolicyName(policyName);
        if (struct != null) {
            return struct.getOwner();
        }
        return null;
    }

    private PolicyStruct findPolicyStructByPolicyName(String policyName) {
        Iterator<PolicyStruct> it = this.globalStructs.iterator();
        while (it.hasNext()) {
            PolicyStruct struct = it.next();
            if (struct != null && struct.containsPolicyName(policyName)) {
                return struct;
            }
        }
        return null;
    }

    private void combineEveryPolicy(HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy, PolicyStruct struct, String policyName, boolean shouldChange) {
        PolicyStruct.PolicyItem adminItem;
        Bundle bundle = new Bundle();
        if (TAG_POLICY_SYS_APP.equals(policyName) || TAG_POLICY_UNDETACHABLE_SYS_APP.equals(policyName)) {
            bundle = struct.getItemByPolicyName(policyName).getAttributes();
        }
        PolicyStruct.PolicyItem globalItem = new PolicyStruct.PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType(), struct);
        globalItem.copyFrom(struct.getItemByPolicyName(policyName));
        int adminSize = policy.getActiveAdminExList().size();
        if (globalItem.getItemType() != PolicyStruct.PolicyType.CONFIGURATION) {
            for (int i = 0; i < adminSize; i++) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) policy.getActiveAdminExList().get(i);
                if (!(admin.getHwActiveAdmin() == null || (adminItem = (PolicyStruct.PolicyItem) admin.getHwActiveAdmin().adminPolicyItems.get(policyName)) == null || !adminItem.hasAnyNonNullAttribute())) {
                    traverseCombinePolicyItem(globalItem, adminItem);
                }
            }
        } else if (shouldChange) {
            globalItem = findGlobleItem(adminSize, policy.getActiveAdminExList(), policyName, globalItem);
            if (TAG_POLICY_SYS_APP.equals(policyName) || TAG_POLICY_UNDETACHABLE_SYS_APP.equals(policyName)) {
                globalItem.setAttributes(bundle);
            }
        } else {
            HwLog.w(TAG, "global policy will not change: " + policyName);
            return;
        }
        GLOBAL_POLICY_ITEMS.put(policyName, globalItem);
        struct.addPolicyItem(globalItem);
    }

    private void combineAllPolicies(int userHandle, boolean shouldChange) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            Iterator<PolicyStruct> it = this.globalStructs.iterator();
            while (it.hasNext()) {
                PolicyStruct struct = it.next();
                for (String policyName : struct.getPolicyMap().keySet()) {
                    combineEveryPolicy(policy, struct, policyName, shouldChange);
                }
            }
        }
    }

    private PolicyStruct.PolicyItem findGlobleItem(int adminSize, ArrayList<HwDevicePolicyManagerInnerEx.ActiveAdminEx> adminList, String policyName, PolicyStruct.PolicyItem globalItem) {
        PolicyStruct.PolicyItem findItem;
        if (adminList == null || globalItem == null) {
            return globalItem;
        }
        for (int i = adminSize - 1; i >= 0; i--) {
            HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = adminList.get(i);
            if (admin.getHwActiveAdmin() != null && (findItem = (PolicyStruct.PolicyItem) admin.getHwActiveAdmin().adminPolicyItems.get(policyName)) != null) {
                HwLog.w(TAG, "global policy will change: " + policyName);
                return findItem;
            }
        }
        return globalItem;
    }

    private PolicyStruct.PolicyItem combinePoliciesWithPolicyChanged(ComponentName who, PolicyStruct.PolicyItem newItem, String policyName, int userHandle) {
        PolicyStruct.PolicyItem globalAdminItem;
        PolicyStruct.PolicyItem adminItem;
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            HwDevicePolicyManagerInnerEx.ActiveAdminEx activeAdmin = this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle);
            ArrayList<HwDevicePolicyManagerInnerEx.ActiveAdminEx> adminList = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                adminList.add((HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next());
            }
            if (activeAdmin != null && adminList.size() > 0) {
                adminList.remove(activeAdmin);
            }
            PolicyStruct struct = findPolicyStructByPolicyName(policyName);
            globalAdminItem = new PolicyStruct.PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType(), struct);
            globalAdminItem.copyFrom(struct.getItemByPolicyName(policyName));
            Iterator<HwDevicePolicyManagerInnerEx.ActiveAdminEx> it2 = adminList.iterator();
            while (it2.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = it2.next();
                if (!(admin.getHwActiveAdmin() == null || (adminItem = (PolicyStruct.PolicyItem) admin.getHwActiveAdmin().adminPolicyItems.get(policyName)) == null || !adminItem.hasAnyNonNullAttribute())) {
                    traverseCombinePolicyItem(globalAdminItem, adminItem);
                }
            }
            traverseCombinePolicyItem(globalAdminItem, newItem);
        }
        return globalAdminItem;
    }

    private PolicyStruct.PolicyItem combinePoliciesAsUser(String policyName, int userHandle) {
        PolicyStruct.PolicyItem resultPolicyItem;
        PolicyStruct.PolicyItem policyItemAsAdmin;
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
            ArrayList<HwDevicePolicyManagerInnerEx.ActiveAdminEx> adminList = new ArrayList<>();
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                adminList.add((HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next());
            }
            PolicyStruct struct = findPolicyStructByPolicyName(policyName);
            resultPolicyItem = new PolicyStruct.PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType(), struct);
            resultPolicyItem.copyFrom(struct.getItemByPolicyName(policyName));
            Iterator<HwDevicePolicyManagerInnerEx.ActiveAdminEx> it2 = adminList.iterator();
            while (it2.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = it2.next();
                if (!(admin.getHwActiveAdmin() == null || (policyItemAsAdmin = (PolicyStruct.PolicyItem) admin.getHwActiveAdmin().adminPolicyItems.get(policyName)) == null || !policyItemAsAdmin.hasAnyNonNullAttribute())) {
                    traverseCombinePolicyItem(resultPolicyItem, policyItemAsAdmin);
                }
            }
        }
        return resultPolicyItem;
    }

    private PolicyStruct.PolicyItem combinePoliciesWithoutRemovedPolicyItem(ComponentName who, String policyName, int userHandle) {
        PolicyStruct.PolicyItem adminItem;
        HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
        ArrayList<HwDevicePolicyManagerInnerEx.ActiveAdminEx> adminList = new ArrayList<>();
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            HwDevicePolicyManagerInnerEx.ActiveAdminEx activeAdmin = this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle);
            Iterator it = policy.getActiveAdminExList().iterator();
            while (it.hasNext()) {
                adminList.add((HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next());
            }
            if (activeAdmin != null && adminList.size() > 0) {
                adminList.remove(activeAdmin);
            }
        }
        PolicyStruct struct = findPolicyStructByPolicyName(policyName);
        PolicyStruct.PolicyItem oldItem = struct.getItemByPolicyName(policyName);
        PolicyStruct.PolicyItem globalAdminItem = null;
        if (oldItem != null) {
            globalAdminItem = new PolicyStruct.PolicyItem(policyName, oldItem.getItemType(), struct);
            globalAdminItem.copyFrom(oldItem);
            int adminSize = adminList.size();
            for (int i = 0; i < adminSize; i++) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = adminList.get(i);
                if (admin.getHwActiveAdmin() != null && (adminItem = (PolicyStruct.PolicyItem) admin.getHwActiveAdmin().adminPolicyItems.get(policyName)) != null && adminItem.hasAnyNonNullAttribute() && adminItem.getPolicyName().equals(policyName)) {
                    traverseCombinePolicyItem(globalAdminItem, adminItem);
                }
            }
        }
        return globalAdminItem;
    }

    private void traverseCombinePolicyItem(PolicyStruct.PolicyItem oldRoot, PolicyStruct.PolicyItem newRoot) {
        if (!(oldRoot == null || newRoot == null)) {
            oldRoot.setAttributes(combineAttributes(oldRoot.getAttributes(), newRoot.getAttributes(), oldRoot));
            int size = oldRoot.getChildItem().size();
            ArrayList<PolicyStruct.PolicyItem> leafItems = oldRoot.getChildItem();
            for (int i = 0; i < size; i++) {
                if (oldRoot.getItemType() == PolicyStruct.PolicyType.CONFIGLIST && leafItems.get(i).getPolicyStruct() == null) {
                    leafItems.get(i).setPolicyStruct(oldRoot.getPolicyStruct());
                }
                traverseCombinePolicyItem((PolicyStruct.PolicyItem) oldRoot.getChildItem().get(i), (PolicyStruct.PolicyItem) newRoot.getChildItem().get(i));
            }
        }
    }

    private void combineAttributesForState(Bundle oldAttr, Bundle newAttr) {
        if (!(oldAttr == null || newAttr == null)) {
            for (String key : newAttr.keySet()) {
                if (newAttr.get(key) != null) {
                    oldAttr.putBoolean(key, oldAttr.getBoolean(key) || newAttr.getBoolean(key));
                }
            }
        }
    }

    private void combineAttributesForList(Bundle oldAttr, Bundle newAttr) {
        if (!(oldAttr == null || newAttr == null)) {
            for (String key : newAttr.keySet()) {
                try {
                    if (newAttr.get(key) != null) {
                        ArrayList<String> oldPolicyList = oldAttr.getStringArrayList(key);
                        ArrayList<String> newPolicyList = newAttr.getStringArrayList(key);
                        if (oldPolicyList == null) {
                            oldPolicyList = new ArrayList<>();
                        }
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(oldPolicyList, newPolicyList);
                        oldAttr.putStringArrayList(key, oldPolicyList);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    HwLog.w(TAG, "combineAttributesForList exception.");
                }
            }
        }
    }

    private void combineAttributesForConfigList(PolicyStruct.PolicyItem item, Bundle oldAttr, Bundle newAttr) {
        if (!(item == null || oldAttr == null || newAttr == null)) {
            for (String key : newAttr.keySet()) {
                try {
                    if (newAttr.get(key) != null) {
                        ArrayList<String> oldConfigList = oldAttr.getStringArrayList(key);
                        if (oldConfigList == null) {
                            oldConfigList = new ArrayList<>();
                        }
                        item.addAndUpdateConfigurationList(oldConfigList, newAttr.getStringArrayList(key));
                        oldAttr.putStringArrayList(key, oldConfigList);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    HwLog.w(TAG, "combineAttributesForConfigList exception.");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.devicepolicy.HwDevicePolicyManagerService$5  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType = new int[PolicyStruct.PolicyType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.STATE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.LIST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.CONFIGLIST.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.CONFIGURATION.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private Bundle combineAttributes(Bundle oldAttr, Bundle newAttr, PolicyStruct.PolicyItem item) {
        int i = AnonymousClass5.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[item.getItemType().ordinal()];
        if (i == 1) {
            combineAttributesForState(oldAttr, newAttr);
        } else if (i == 2) {
            combineAttributesForList(oldAttr, newAttr);
        } else if (i == 3) {
            combineAttributesForConfigList(item, oldAttr, newAttr);
        } else if (i == 4) {
            for (String key : newAttr.keySet()) {
                if (newAttr.get(key) != null) {
                    oldAttr.putString(key, newAttr.getString(key));
                }
            }
        }
        return oldAttr;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public ArrayList<String> queryBrowsingHistory(ComponentName who, int userHandle) {
        ArrayList<String> historyList = new ArrayList<>();
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have network MDM permission!");
            enforceHwCrossUserPermission(userHandle);
            getHwActiveAdmin(who, userHandle);
            DevicePolicyPlugin plugin = findPluginByPolicyName("network-black-list");
            if (plugin == null || !(plugin instanceof DeviceNetworkPlugin)) {
                HwLog.e(TAG, "no DeviceNetworkPlugin found, pluginName");
                return historyList;
            }
            long callingId = Binder.clearCallingIdentity();
            ArrayList<String> historyList2 = ((DeviceNetworkPlugin) plugin).queryBrowsingHistory();
            Binder.restoreCallingIdentity(callingId);
            return historyList2;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean hasHwPolicy(int userHandle) {
        boolean hasHwPolicy;
        HwLog.d(TAG, "hasHwPolicy, userHandle :" + userHandle);
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            hasHwPolicy = PolicyFileUtils.hasHwPolicy(userHandle);
        }
        return hasHwPolicy;
    }

    /* access modifiers changed from: protected */
    public boolean isSecureBlockEncrypted() {
        return false;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int configVpnProfile(ComponentName who, Bundle para, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            KeyStore keyStore = KeyStore.getInstance();
            if (who != null) {
                if (para != null) {
                    if (!VpnUtils.isValidVpnConfig(para)) {
                        HwLog.e(TAG, "This Config isn't valid vpnConfig");
                        return -1;
                    }
                    VpnProfile profile = VpnUtils.getProfile(para);
                    if (profile == null) {
                        return -1;
                    }
                    if (!keyStore.put("VPN_" + profile.key, profile.encode(), -1, 0)) {
                        HwLog.e(TAG, "Set vpn failed, check the config.");
                        return -1;
                    }
                    String key = para.getString(KEY);
                    Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
                    while (it.hasNext()) {
                        HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                        if (!(admin.getHwActiveAdmin() == null || admin.getHwActiveAdmin().vpnProviderlist == null)) {
                            Bundle speProvider = null;
                            Iterator it2 = admin.getHwActiveAdmin().vpnProviderlist.iterator();
                            while (true) {
                                if (!it2.hasNext()) {
                                    break;
                                }
                                Bundle provider = (Bundle) it2.next();
                                if (key.equals(provider.getString(KEY))) {
                                    speProvider = provider;
                                    break;
                                }
                            }
                            if (speProvider != null) {
                                admin.getHwActiveAdmin().vpnProviderlist.remove(speProvider);
                                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                            }
                        }
                    }
                    configVpnProfile(getHwActiveAdmin(who, userHandle), key, para, userHandle);
                    return 1;
                }
            }
            HwLog.e(TAG, "Bundle para is null or componentName is null!");
            return -1;
        }
    }

    private void configVpnProfile(HwActiveAdmin ap, String key, Bundle para, int userHandle) {
        if (ap != null) {
            if (ap.vpnProviderlist != null) {
                Iterator it = ap.vpnProviderlist.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Bundle provider = (Bundle) it.next();
                    if (!TextUtils.isEmpty(key)) {
                        if (provider != null && !TextUtils.isEmpty(provider.getString(KEY)) && key.equals(provider.getString(KEY))) {
                            ap.vpnProviderlist.remove(provider);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else {
                ap.vpnProviderlist = new ArrayList();
            }
            ap.vpnProviderlist.add(para);
            this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int removeVpnProfile(ComponentName who, Bundle para, int userHandle) {
        String key = para.getString(KEY);
        int i = -1;
        if (who == null || TextUtils.isEmpty(key)) {
            HwLog.e(TAG, "ComponentName or key is empty.");
            return -1;
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            KeyStore keyStore = KeyStore.getInstance();
            boolean isDeleted = false;
            Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (!(admin.getHwActiveAdmin() == null || admin.getHwActiveAdmin().vpnProviderlist == null)) {
                    Bundle specProvider = null;
                    Iterator it2 = admin.getHwActiveAdmin().vpnProviderlist.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        Bundle provider = (Bundle) it2.next();
                        if (key.equals(provider.getString(KEY))) {
                            specProvider = provider;
                            break;
                        }
                    }
                    if (specProvider != null) {
                        if (!keyStore.delete("VPN_" + key)) {
                            HwLog.e(TAG, "Delete vpn failed, check the key.");
                            return -1;
                        }
                        admin.getHwActiveAdmin().vpnProviderlist.remove(specProvider);
                        this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                        isDeleted = true;
                    } else {
                        continue;
                    }
                }
            }
            if (isDeleted) {
                i = 1;
            }
            return i;
        }
    }

    private Bundle getVpnProfileForUser(ComponentName who, int userHandle, String profileKey) {
        HwActiveAdmin hwAdmin = getHwActiveAdmin(who, userHandle);
        if (hwAdmin == null || hwAdmin.vpnProviderlist == null || profileKey == null) {
            return null;
        }
        for (Bundle provider : hwAdmin.vpnProviderlist) {
            if (provider != null && profileKey.equals(provider.getString(KEY))) {
                return provider;
            }
        }
        return null;
    }

    private Bundle getVpnProfileForUser(int userHandle, String profileKey) {
        HwDevicePolicyManagerInnerEx.DevicePolicyDataEx policy = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle);
        if (profileKey == null || policy == null) {
            return null;
        }
        Iterator it = policy.getActiveAdminExList().iterator();
        while (it.hasNext()) {
            HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
            if (!(admin.getHwActiveAdmin() == null || admin.getHwActiveAdmin().vpnProviderlist == null)) {
                for (Bundle provider : admin.getHwActiveAdmin().vpnProviderlist) {
                    if (provider != null && profileKey.equals(provider.getString(KEY))) {
                        return provider;
                    }
                }
                continue;
            }
        }
        return null;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle getVpnProfile(ComponentName who, Bundle keyWords, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        String key = keyWords.getString(KEY);
        if (TextUtils.isEmpty(key)) {
            HwLog.e(TAG, "key is null or empty.");
            return null;
        }
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                return getVpnProfileForUser(who, userHandle, key);
            }
            return getVpnProfileForUser(userHandle, key);
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle getVpnList(ComponentName who, Bundle keyWords, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            ArrayList<String> vpnKeyList = new ArrayList<>();
            Bundle vpnListBundle = new Bundle();
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.vpnProviderlist == null) {
                    return null;
                }
                for (Bundle provider : admin.vpnProviderlist) {
                    if (!TextUtils.isEmpty(provider.getString(KEY))) {
                        vpnKeyList.add(provider.getString(KEY));
                    }
                }
                vpnListBundle.putStringArrayList("keylist", vpnKeyList);
                return vpnListBundle;
            }
            Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
            while (it.hasNext()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin2 = (HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next();
                if (!(admin2.getHwActiveAdmin() == null || admin2.getHwActiveAdmin().vpnProviderlist == null)) {
                    VpnUtils.filterVpnKeyList(admin2.getHwActiveAdmin().vpnProviderlist, vpnKeyList);
                }
            }
            vpnListBundle.putStringArrayList("keylist", vpnKeyList);
            return vpnListBundle;
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean formatSDCard(ComponentName who, String diskId, int userHandle) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_SDCARD", "does not have sd card MDM permission!");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
                long token = Binder.clearCallingIdentity();
                try {
                    ((StorageManager) this.mContext.getSystemService("storage")).partitionPublic(StorageUtils.getDiskId(this.mContext));
                } catch (ClassCastException e) {
                    HwLog.e(TAG, "format sd card data error! ClassCastException");
                    return false;
                } catch (Exception e2) {
                    HwLog.e(TAG, "format sd card data error!");
                    return false;
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
            return true;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setAccountDisabled(ComponentName who, String accountType, boolean disabled, int userHandle) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app management MDM permission!");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle);
                if (admin == null) {
                    throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
                } else if (admin.getUid() == Binder.getCallingUid()) {
                    if (disabled) {
                        admin.getAccountTypesWithManagementDisabled().add(accountType);
                    } else {
                        admin.getAccountTypesWithManagementDisabled().remove(accountType);
                    }
                    this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
                } else {
                    throw new SecurityException("Admin " + who + " is not owned by uid " + Binder.getCallingUid());
                }
            }
            return;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean isAccountDisabled(ComponentName who, String accountType, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                HwDevicePolicyManagerInnerEx.ActiveAdminEx admin = this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle);
                if (admin == null) {
                    throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
                } else if (admin.getUid() == Binder.getCallingUid()) {
                    return admin.getAccountTypesWithManagementDisabled().contains(accountType);
                } else {
                    throw new SecurityException("Admin " + who + " is not owned by uid " + Binder.getCallingUid());
                }
            } else {
                Iterator it = this.mHwDevicePolicyManagerInner.getUserDataInner(userHandle).getActiveAdminExList().iterator();
                while (it.hasNext()) {
                    if (((HwDevicePolicyManagerInnerEx.ActiveAdminEx) it.next()).getAccountTypesWithManagementDisabled().contains(accountType)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean installCertificateWithType(ComponentName who, int type, byte[] certBuffer, String alias, String password, int certInstallType, boolean requestAccess, int userHandle) {
        boolean isSuccess;
        long token;
        checkEnvBeforeCallInterface(who, userHandle);
        if (type == 0) {
            try {
                isSuccess = CertInstallHelper.installPkcs12Cert(password, certBuffer, alias, certInstallType);
            } catch (Exception e) {
                HwLog.e(TAG, "throw error when install cert");
                return false;
            }
        } else if (type == 1) {
            isSuccess = CertInstallHelper.installX509Cert(certBuffer, alias, certInstallType);
        } else {
            HwLog.e(TAG, "throw error when install cert");
            return false;
        }
        if (!requestAccess) {
            return isSuccess;
        }
        int callingUid = Binder.getCallingUid();
        token = Binder.clearCallingIdentity();
        try {
            KeyChain.KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, UserHandle.getUserHandleForUid(callingUid));
            try {
                keyChainConnection.getService().setGrant(callingUid, alias, true);
                keyChainConnection.close();
                Binder.restoreCallingIdentity(token);
                return true;
            } catch (RemoteException e2) {
                HwLog.e(TAG, "set grant certificate");
                keyChainConnection.close();
            } catch (Throwable th) {
                keyChainConnection.close();
                throw th;
            }
        } catch (InterruptedException e3) {
            HwLog.w(TAG, "Interrupted while set granting certificate");
            Thread.currentThread().interrupt();
        } catch (Throwable th2) {
            Binder.restoreCallingIdentity(token);
            throw th2;
        }
        Binder.restoreCallingIdentity(token);
        return false;
    }

    private void checkEnvBeforeCallInterface(ComponentName who, int userHandle) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have install cert MDM permission!");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
            }
            return;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    public long getUsrSetExtendTime() {
        String value = getPolicy(null, PASSWORD_CHANGE_EXTEND_TIME, UserHandle.myUserId()).getString("value");
        if (value == null || SettingsMDMPlugin.EMPTY_STRING.equals(value)) {
            return -1;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "getUsrSetExtendTime : NumberFormatException");
            return -1;
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setSilentActiveAdmin(ComponentName who, int userHandle) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have PERMISSION_MDM_DEVICE_MANAGER permission!");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
                if (!devicePolicyManager.isAdminActive(who)) {
                    HwLog.d(TAG, "setSilentActiveAdmin, IS_HAS_HW_MDM_FEATURE active supported.");
                    long identityToken = Binder.clearCallingIdentity();
                    try {
                        devicePolicyManager.setActiveAdmin(who, true, userHandle);
                    } finally {
                        Binder.restoreCallingIdentity(identityToken);
                    }
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    public void loadHwSpecialPolicyFromXml(XmlPullParser parser) {
        if (parser != null) {
            String tag = parser.getName();
            if (TAG_POLICY_SYS_APP.equals(tag) || TAG_POLICY_UNDETACHABLE_SYS_APP.equals(tag)) {
                String value = parser.getAttributeValue(null, "value");
                Bundle bundle = new Bundle();
                bundle.putString("value", value);
                HwLog.d(TAG, "loadHwSpecialPolicyFromXml value:" + value);
                int size = this.globalStructs.size();
                for (int i = 0; i < size; i++) {
                    PolicyStruct.PolicyItem globalItem = this.globalStructs.get(i).getPolicyItem(tag);
                    if (globalItem != null) {
                        globalItem.setAttributes(bundle);
                        HwLog.d(TAG, "loadHwSpecialPolicyFromXml find:" + tag + " in globalStructs and update its value");
                    }
                }
            }
        }
    }

    public void setHwSpecialPolicyToXml(XmlSerializer out) {
        Bundle bundle;
        if (out != null) {
            ArrayList<String> specialPolicies = new ArrayList<>();
            specialPolicies.add(TAG_POLICY_SYS_APP);
            specialPolicies.add(TAG_POLICY_UNDETACHABLE_SYS_APP);
            int size = specialPolicies.size();
            for (int i = 0; i < size; i++) {
                String policyName = specialPolicies.get(i);
                PolicyStruct.PolicyItem globalItem = GLOBAL_POLICY_ITEMS.get(policyName);
                if (!(globalItem == null || (bundle = globalItem.getAttributes()) == null || bundle.isEmpty())) {
                    String value = bundle.getString("value");
                    HwLog.d(TAG, "setHwSpecialPolicyToXml " + value);
                    if (!TextUtils.isEmpty(value)) {
                        try {
                            out.startTag(null, policyName);
                            out.attribute(null, "value", value);
                            out.endTag(null, policyName);
                        } catch (IOException e) {
                            HwLog.d(TAG, "failed parsing when try to setHwSpecialPolicyToXml");
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int setSystemLanguage(ComponentName who, Bundle bundle, int userHandle) {
        if (bundle == null || TextUtils.isEmpty(bundle.getString("locale"))) {
            throw new IllegalArgumentException("locale is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "Does not have mdm_device_manager permission.");
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                try {
                    Locale locale = Locale.forLanguageTag(bundle.getString("locale"));
                    IActivityManager am = ActivityManagerNative.getDefault();
                    Configuration config = am.getConfiguration();
                    config.setLocale(locale);
                    config.userSetLocale = true;
                    am.updateConfiguration(config);
                    Settings.System.putStringForUser(this.mContext.getContentResolver(), "system_locales", locale.toLanguageTag(), userHandle);
                    BackupManager.dataChanged("com.android.providers.settings");
                } catch (RemoteException e) {
                    Slog.w(TAG, "failed to set system language RemoteException");
                } catch (Exception e2) {
                    Slog.w(TAG, "failed to set system language");
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        return 1;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int setForcedActiveDeviceAdmin(ComponentName admin, int userId) {
        if (admin != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
            boolean isForcedActiveMode = false;
            if (SystemProperties.getInt(PROP_ACTIVE_MODE, 0) == 1) {
                isForcedActiveMode = true;
            }
            HwLog.d(TAG, "setForcedActivateDeviceAdmin isForcedActiveMode = " + isForcedActiveMode);
            if (!isForcedActiveMode && !HwCertificationManager.getInstance().isDevCertification(admin.getPackageName())) {
                return -1;
            }
            if (!((DevicePolicyManager) this.mContext.getSystemService("device_policy")).isAdminActive(admin)) {
                long callingId = Binder.clearCallingIdentity();
                Settings.Global.putStringForUser(this.mContext.getContentResolver(), MDM_FORCED_ACTIVE_ADMIN, admin.getPackageName(), userId);
                Binder.restoreCallingIdentity(callingId);
                return 1;
            } else if (isForcedActive(admin, userId)) {
                HwLog.d(TAG, "setForcedActivateDeviceAdmin, forced active already.");
                return -1;
            } else {
                setForcedActivePolicy(admin, userId);
                return 1;
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int setDelayDeactiveDeviceAdmin(ComponentName admin, Bundle bundle, int userId) {
        if (admin == null) {
            throw new IllegalArgumentException("ComponentName is null");
        } else if (bundle != null) {
            int delayTime = bundle.getInt("delay_time");
            if (delayTime < 1 || delayTime > MAX_DELAY_TIME) {
                throw new IllegalArgumentException("delayTime illegal");
            }
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
            if (!((DevicePolicyManager) this.mContext.getSystemService("device_policy")).isAdminActive(admin)) {
                this.mDelayDeactiveInfo.put(admin.getPackageName(), Integer.valueOf(delayTime));
            } else if (!TextUtils.isEmpty(getDeactiveTime(admin, userId))) {
                HwLog.d(TAG, "setDelayDeactivateDeviceAdmin, delay active already.");
                return -1;
            } else {
                this.mDelayDeactiveInfo.put(admin.getPackageName(), Integer.valueOf(delayTime));
                setDelayDeactivePolicy(admin, userId);
            }
            return 1;
        } else {
            throw new IllegalArgumentException("bundle is null");
        }
    }

    public void notifyActiveAdmin(ComponentName admin, int userHandle) {
        String type;
        if (admin != null) {
            if (admin.getPackageName().equals(Settings.Global.getStringForUser(this.mContext.getContentResolver(), MDM_FORCED_ACTIVE_ADMIN, userHandle))) {
                Settings.Global.putStringForUser(this.mContext.getContentResolver(), MDM_FORCED_ACTIVE_ADMIN, SettingsMDMPlugin.EMPTY_STRING, userHandle);
                type = MDM_FORCED_ACTIVE_ADMIN;
            } else if (admin.getPackageName().equals(Settings.Global.getStringForUser(this.mContext.getContentResolver(), MDM_DELAY_DEACTIVE_ADMIN, userHandle))) {
                Settings.Global.putStringForUser(this.mContext.getContentResolver(), MDM_DELAY_DEACTIVE_ADMIN, SettingsMDMPlugin.EMPTY_STRING, userHandle);
                type = MDM_DELAY_DEACTIVE_ADMIN;
            } else {
                type = MDM_ACTIVE_ADMIN_DEFAULT;
            }
            char c = 65535;
            int hashCode = type.hashCode();
            if (hashCode != -1555009733) {
                if (hashCode == 1659569267 && type.equals(MDM_FORCED_ACTIVE_ADMIN)) {
                    c = 0;
                }
            } else if (type.equals(MDM_DELAY_DEACTIVE_ADMIN)) {
                c = 1;
            }
            if (c == 0) {
                setForcedActivePolicy(admin, userHandle);
            } else if (c == 1) {
                setDelayDeactivePolicy(admin, userHandle);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int removeActiveDeviceAdmin(ComponentName admin, int userHandle) {
        if (admin != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(admin, userHandle);
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
                if (!devicePolicyManager.isAdminActive(admin)) {
                    return -1;
                }
                long identityToken = Binder.clearCallingIdentity();
                try {
                    devicePolicyManager.removeActiveAdmin(admin);
                    Binder.restoreCallingIdentity(identityToken);
                    return 1;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identityToken);
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle isForcedActiveDeviceAdmin(ComponentName admin, int userHandle) {
        if (admin != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                HwActiveAdmin hwAdmin = getHwActiveAdminEx(admin, userHandle);
                if (hwAdmin == null) {
                    return null;
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean("is_forced_active", hwAdmin.isForcedActive);
                return bundle;
            }
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle getDeviceAdminDeactiveTime(ComponentName admin, int userHandle) {
        if (admin != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                HwActiveAdmin hwAdmin = getHwActiveAdminEx(admin, userHandle);
                if (hwAdmin == null) {
                    return null;
                }
                Bundle bundle = new Bundle();
                bundle.putString("deactive_time", hwAdmin.deactiveTime);
                return bundle;
            }
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    private boolean isForcedActive(ComponentName admin, int userHandle) {
        Bundle bundle = isForcedActiveDeviceAdmin(admin, userHandle);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("is_forced_active");
    }

    private String getDeactiveTime(ComponentName admin, int userHandle) {
        Bundle bundle = getDeviceAdminDeactiveTime(admin, userHandle);
        if (bundle == null) {
            return SettingsMDMPlugin.EMPTY_STRING;
        }
        return bundle.getString("deactive_time");
    }

    private void setForcedActivePolicy(ComponentName admin, int userHandle) {
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            HwActiveAdmin hwAdmin = getHwActiveAdminEx(admin, userHandle);
            if (hwAdmin != null && !hwAdmin.isForcedActive) {
                hwAdmin.isForcedActive = true;
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
        }
    }

    private void setDelayDeactivePolicy(ComponentName admin, int userHandle) {
        String deactiveTime = String.valueOf(System.currentTimeMillis() + (((long) this.mDelayDeactiveInfo.get(admin.getPackageName()).intValue()) * MS_PER_HOUR));
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            HwActiveAdmin hwAdmin = getHwActiveAdminEx(admin, userHandle);
            if (hwAdmin != null) {
                hwAdmin.deactiveTime = deactiveTime;
                this.mHwDevicePolicyManagerInner.saveSettingsLockedInner(userHandle);
            }
        }
        this.mDelayDeactiveInfo.remove(admin.getPackageName());
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void setDeviceOwnerApp(ComponentName admin, String ownerName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_OWNER", "does not have device_manager MDM permission!");
        enforceHwCrossUserPermission(userId);
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            long callingId = Binder.clearCallingIdentity();
            if (isForcedActive(admin, userId)) {
                try {
                    this.mIsMDMDeviceOwnerAPI = true;
                    ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).setDeviceOwner(admin, ownerName, userId);
                } finally {
                    this.mIsMDMDeviceOwnerAPI = false;
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void clearDeviceOwnerApp(int userId) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_OWNER", "does not have device_manager MDM permission!");
        enforceHwCrossUserPermission(userId);
        synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
            try {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
                ComponentName component = devicePolicyManager.getDeviceOwnerComponentOnCallingUser();
                if (component != null) {
                    this.mIsMDMDeviceOwnerAPI = true;
                    devicePolicyManager.clearDeviceOwnerApp(component.getPackageName());
                    this.mIsMDMDeviceOwnerAPI = false;
                } else {
                    throw new IllegalArgumentException("The device owner is not set up.");
                }
            } catch (Throwable th) {
                this.mIsMDMDeviceOwnerAPI = false;
                throw th;
            }
        }
    }

    public boolean isMdmApiDeviceOwner() {
        HwLog.i(TAG, "isMdmApiDeviceOwner : " + this.mIsMDMDeviceOwnerAPI);
        return this.mIsMDMDeviceOwnerAPI;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void turnOnMobiledata(ComponentName who, boolean on, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have MDM_NETWORK_MANAGER permission!");
        if (who != null) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
            }
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (on) {
                try {
                    phone.enableDataConnectivity();
                } catch (RemoteException e) {
                    HwLog.e(TAG, "Can not calling the remote function to set data enabled!");
                }
            } else {
                phone.disableDataConnectivity();
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    private boolean dealCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber, int userHandle, boolean isSet) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_KEYGUARD", "does not have keyguard MDM permission!");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                if (this.mHwDevicePolicyManagerInner.getActiveAdminUncheckedLockedInner(who, userHandle) != null) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        LockPatternUtilsEx lockPatternUtils = new LockPatternUtilsEx(this.mContext);
                        if (isSet) {
                            return lockPatternUtils.setExtendLockScreenPassword(password, phoneNumber, userHandle);
                        }
                        boolean clearExtendLockScreenPassword = lockPatternUtils.clearExtendLockScreenPassword(password, userHandle);
                        Binder.restoreCallingIdentity(token);
                        return clearExtendLockScreenPassword;
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                } else {
                    throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
                }
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean setCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber, int userHandle) {
        return dealCarrierLockScreenPassword(who, password, phoneNumber, userHandle, true);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean clearCarrierLockScreenPassword(ComponentName who, String password, int userHandle) {
        return dealCarrierLockScreenPassword(who, password, null, userHandle, false);
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public void resetNetorkSetting(ComponentName who, int userHandle) {
        if (who != null) {
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
            }
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have com.huawei.permission.sec.MDM_NETWORK_MANAGER !");
            Intent intent = new Intent("com.android.settings.mdm.receiver.action.MDMPolicyResetNetworkSetting");
            intent.setComponent(new ComponentName(SettingsMDMPlugin.SETTINGS_APK_NAME, SettingsMDMPlugin.SETTINGS_MDM_RECEIVER));
            this.mContext.sendBroadcast(intent);
            return;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle getTopAppPackageName(ComponentName who, int userHandle) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
            }
            long callingId = Binder.clearCallingIdentity();
            Bundle bundle = new Bundle();
            ActivityInfo lastResumeActivity = HwActivityTaskManager.getLastResumedActivity();
            if (lastResumeActivity != null) {
                bundle.putString("value", lastResumeActivity.packageName);
            }
            Binder.restoreCallingIdentity(callingId);
            return bundle;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public boolean setDefaultDataCard(ComponentName who, int slotId, Message response, int userHandle) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have PERMISSION_MDM_NETWORK_MANAGER!");
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
            }
            long token = Binder.clearCallingIdentity();
            if (slotId >= 0) {
                try {
                    if (slotId < TelephonyManager.getDefault().getPhoneCount()) {
                        if (isAirplaneModeOn(this.mContext)) {
                            HwLog.i(TAG, "In air plane mode, can not change slot.");
                            return sendMessageForSetDefaultDataCard(response, "AIR_PLANE_MODE_ON");
                        } else if (HwTelephonyManagerInner.getDefault().getSubState((long) slotId) == 0) {
                            HwLog.i(TAG, "Target slot id [" + slotId + "] is inactive, can not change slot.");
                            boolean sendMessageForSetDefaultDataCard = sendMessageForSetDefaultDataCard(response, "SUBSCRIPTION_INACTIVE");
                            Binder.restoreCallingIdentity(token);
                            return sendMessageForSetDefaultDataCard;
                        } else if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId) {
                            HwLog.i(TAG, "Main slot id is " + slotId + ", no need to change.");
                            boolean sendMessageForSetDefaultDataCard2 = sendMessageForSetDefaultDataCard(response, null);
                            Binder.restoreCallingIdentity(token);
                            return sendMessageForSetDefaultDataCard2;
                        } else if (!HwTelephonyManagerInner.getDefault().isSetDefault4GSlotIdEnabled()) {
                            HwLog.i(TAG, "Can not set default main slot:" + slotId);
                            boolean sendMessageForSetDefaultDataCard3 = sendMessageForSetDefaultDataCard(response, "GENERIC_FAILURE");
                            Binder.restoreCallingIdentity(token);
                            return sendMessageForSetDefaultDataCard3;
                        } else {
                            HwTelephonyManagerInner.getDefault().setDefault4GSlotId(slotId, response);
                            Binder.restoreCallingIdentity(token);
                            return true;
                        }
                    }
                } catch (IllegalStateException e) {
                    HwLog.w(TAG, "IllegalStateException occured when set default data card!");
                    return sendMessageForSetDefaultDataCard(response, "GENERIC_FAILURE");
                } catch (Exception e2) {
                    HwLog.w(TAG, "Set default data card error!");
                    return sendMessageForSetDefaultDataCard(response, "GENERIC_FAILURE");
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
            HwLog.i(TAG, "Invalid slot ID = " + slotId);
            boolean sendMessageForSetDefaultDataCard4 = sendMessageForSetDefaultDataCard(response, "GENERIC_FAILURE");
            Binder.restoreCallingIdentity(token);
            return sendMessageForSetDefaultDataCard4;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    private boolean isAirplaneModeOn(Context context) {
        if (context == null || Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 0) {
            return false;
        }
        return true;
    }

    private boolean sendMessageForSetDefaultDataCard(Message response, String exception) {
        if (response == null || response.replyTo == null) {
            return false;
        }
        Bundle data = new Bundle();
        if (exception != null) {
            data.putBoolean("RESULT", false);
            data.putString("EXCEPTION", exception);
        } else {
            data.putBoolean("RESULT", true);
        }
        response.setData(data);
        try {
            response.replyTo.send(response);
            if (exception == null) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            HwLog.w(TAG, "RemoteException occured when send response to the third party apk!");
            return false;
        }
    }

    public boolean isDeprecatedPolicyEnabled(int reqPolicy, int userId) {
        Bundle bundle = getPolicy(null, "policy-deprecated-admin-interfaces-enabled", userId);
        boolean isSupportDeprecated = false;
        if (bundle == null) {
            HwLog.e(TAG, "The bundle is null");
        } else {
            isSupportDeprecated = bundle.getBoolean("value");
        }
        return DA_DISALLOWED_POLICIES.contains(Integer.valueOf(reqPolicy)) && isSupportDeprecated;
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public Bundle getEthernetConfiguration(ComponentName who, Bundle keyWords, int userHandle) {
        Bundle ethernetConfiguration;
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        } else if (keyWords != null) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", DOES_NOT_HAVE_NETWORK_PERMISSION);
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
                ethernetConfiguration = EthernetUtils.getEthernetConfiguration(this.mContext, keyWords);
            }
            return ethernetConfiguration;
        } else {
            throw new IllegalArgumentException("keywords is null");
        }
    }

    @Override // com.android.server.devicepolicy.IHwDevicePolicyManager
    public int setEthernetConfiguration(ComponentName who, Bundle policyData, int userHandle) {
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        } else if (policyData != null) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", DOES_NOT_HAVE_NETWORK_PERMISSION);
            synchronized (this.mHwDevicePolicyManagerInner.getLockObjectInner()) {
                getHwActiveAdmin(who, userHandle);
                if (EthernetUtils.setEthernetConfiguration(this.mContext, policyData)) {
                    return 1;
                }
                return -1;
            }
        } else {
            throw new IllegalArgumentException("policyData is null");
        }
    }

    public class EffectedItem {
        public ComponentName effectedAdmin = null;
        public DevicePolicyPlugin effectedPlugin = null;
        public ArrayList<PolicyStruct.PolicyItem> effectedPolicies = new ArrayList<>();

        public EffectedItem(ComponentName who, DevicePolicyPlugin plugin, ArrayList<PolicyStruct.PolicyItem> policies) {
            this.effectedAdmin = who;
            this.effectedPlugin = plugin;
            this.effectedPolicies = policies;
        }
    }
}
