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
import android.app.backup.BackupManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.net.VpnProfile;
import com.android.internal.telephony.ITelephony;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.JournaledFile;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.devicepolicy.AbsDevicePolicyManagerService;
import com.android.server.devicepolicy.DevicePolicyManagerService;
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
import com.android.server.devicepolicy.plugins.HwEmailMDMPlugin;
import com.android.server.devicepolicy.plugins.HwSystemManagerPlugin;
import com.android.server.devicepolicy.plugins.PhoneManagerPlugin;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.widget.LockPatternUtilsEx;
import com.huawei.systemmanager.appcontrol.iaware.HwAppStartupSettingEx;
import com.huawei.systemmanager.appcontrol.iaware.HwIAwareManager;
import com.huawei.systemmanager.appcontrol.iaware.IMultiTaskManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwDevicePolicyManagerService extends DevicePolicyManagerService implements IHwDevicePolicyManager {
    public static final int AS_MODIFIER_USER = 1;
    public static final int AS_TP_SHW = 0;
    public static final int AS_TP_SLF = 1;
    public static final int AS_TP_SMT = 0;
    public static final int CERTIFICATE_PEM_BASE64 = 1;
    public static final int CERTIFICATE_PKCS12 = 0;
    private static final boolean DBG = false;
    public static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
    private static final String DEVICE_POLICIES_1_XML = "device_policies_1.xml";
    protected static final String DPMDESCRIPTOR = "android.app.admin.IDevicePolicyManager";
    public static final String DYNAMIC_ROOT_PROP = "persist.sys.root.status";
    public static final String DYNAMIC_ROOT_STATE_SAFE = "0";
    private static final String EXCHANGE_DOMAIN = "domain";
    private static final int EXCHANGE_PROVIDER_MAX_NUM = 20;
    private static final int FAILED = -1;
    private static final Set<String> HWDEVICE_OWNER_USER_RESTRICTIONS = new HashSet();
    private static final String KEY = "key";
    private static final int MAX_QUERY_PROCESS = 10000;
    private static final double MAX_RETRY_TIMES = 3.0d;
    private static final String MDM_VPN_PERMISSION = "com.huawei.permission.sec.MDM_VPN";
    public static final int NOT_SUPPORT_SD_CRYPT = -1;
    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String PASSWORD_CHANGE_EXTEND_TIME = "pwd-password-change-extendtime";
    public static final String PRIVACY_MODE_ON = "privacy_mode_on";
    public static final int SD_CRYPT_STATE_DECRYPTED = 1;
    public static final int SD_CRYPT_STATE_DECRYPTING = 4;
    public static final int SD_CRYPT_STATE_ENCRYPTED = 2;
    public static final int SD_CRYPT_STATE_ENCRYPTING = 3;
    public static final int SD_CRYPT_STATE_INVALID = 0;
    public static final int SD_CRYPT_STATE_MISMATCH = 5;
    public static final int SD_CRYPT_STATE_WAIT_UNLOCK = 6;
    private static final String SETTINGS_MENUS_REMOVE = "settings_menus_remove";
    private static final int STATUS_BAR_DISABLE_MASK = 34013184;
    private static final int SUCCEED = 1;
    private static final String TAG = "HwDPMS";
    private static final String USB_STORAGE = "usb";
    private static ArrayList<String> USER_ISOLATION_POLICY_LIST = new ArrayList<String>() {
        {
            add("email-disable-delete-account");
            add("email-disable-add-account");
            add("allowing-addition-black-list");
        }
    };
    private static final long WAIT_FOR_IAWAREREADY_INTERVAL = 3000;
    private static boolean isSimplePwdOpen = SystemProperties.getBoolean("ro.config.not_allow_simple_pwd", false);
    private static final boolean isSupportCrypt = SystemProperties.getBoolean("ro.config.support_sdcard_crypt", true);
    private static final boolean mHasHwMdmFeature = true;
    private static final int[] mModify = {1, 1, 1, 1};
    private static final int[] mPolicy = {0, 1, 1, 1};
    private static final int[] mShow = {0, 0, 0, 0};
    public static final int transaction_setActiveVisitorPasswordState = 1003;
    private boolean hasInit = false;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public AlertDialog mErrorDialog;
    private HwAdminCache mHwAdminCache;
    private HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private TransactionProcessor mProcessor = null;
    private final UserManager mUserManager;
    final SparseArray<DeviceVisitorPolicyData> mVisitorUserData = new SparseArray<>();

    /* renamed from: com.android.server.devicepolicy.HwDevicePolicyManagerService$6  reason: invalid class name */
    static /* synthetic */ class AnonymousClass6 {
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
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyStruct.PolicyType.CONFIGURATION.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private static class DeviceVisitorPolicyData {
        int mActivePasswordLength = 0;
        int mActivePasswordLetters = 0;
        int mActivePasswordLowerCase = 0;
        int mActivePasswordNonLetter = 0;
        int mActivePasswordNumeric = 0;
        int mActivePasswordQuality = 0;
        int mActivePasswordSymbols = 0;
        int mActivePasswordUpperCase = 0;
        int mFailedPasswordAttempts = 0;
        int mUserHandle;

        public DeviceVisitorPolicyData(int userHandle) {
            this.mUserHandle = userHandle;
        }
    }

    static {
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_usb_file_transfer");
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_physical_media");
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_outgoing_calls");
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_sms");
        HWDEVICE_OWNER_USER_RESTRICTIONS.add("no_config_tethering");
    }

    public HwDevicePolicyManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mUserManager = UserManager.get(context);
        HwDevicePolicyManagerServiceUtil.initialize(context);
        this.mProcessor = new TransactionProcessor(this);
        this.mHwAdminCache = new HwAdminCache();
        addDevicePolicyPlugins(context);
        if (this.globalPlugins.size() > 0) {
            Iterator it = this.globalPlugins.iterator();
            while (it.hasNext()) {
                DevicePolicyPlugin plugin = (DevicePolicyPlugin) it.next();
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

    /* access modifiers changed from: package-private */
    public DeviceVisitorPolicyData getVisitorUserData(int userHandle) {
        DeviceVisitorPolicyData policy;
        synchronized (getLockObject()) {
            policy = this.mVisitorUserData.get(userHandle);
            if (policy == null) {
                policy = new DeviceVisitorPolicyData(userHandle);
                this.mVisitorUserData.append(userHandle, policy);
                loadVisitorSettingsLocked(policy, userHandle);
            }
        }
        return policy;
    }

    private static JournaledFile makeJournaledFile2(int userHandle) {
        String base;
        if (userHandle == 0) {
            base = "/data/system/device_policies_1.xml";
        } else {
            base = new File(Environment.getUserSystemDirectory(userHandle), DEVICE_POLICIES_1_XML).getAbsolutePath();
        }
        File file = new File(base);
        return new JournaledFile(file, new File(base + ".tmp"));
    }

    private void saveVisitorSettingsLock(int userHandle) {
        DeviceVisitorPolicyData policy = getVisitorUserData(userHandle);
        JournaledFile journal = makeJournaledFile2(userHandle);
        FileOutputStream stream = null;
        try {
            FileOutputStream stream2 = new FileOutputStream(journal.chooseForWrite(), false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(stream2, "utf-8");
            out.startDocument(null, true);
            out.startTag(null, "policies");
            if (!(policy.mActivePasswordQuality == 0 && policy.mActivePasswordLength == 0 && policy.mActivePasswordUpperCase == 0 && policy.mActivePasswordLowerCase == 0 && policy.mActivePasswordLetters == 0 && policy.mActivePasswordNumeric == 0 && policy.mActivePasswordSymbols == 0 && policy.mActivePasswordNonLetter == 0)) {
                out.startTag(null, "active-password2");
                out.attribute(null, "quality", Integer.toString(policy.mActivePasswordQuality));
                out.attribute(null, "length", Integer.toString(policy.mActivePasswordLength));
                out.attribute(null, "uppercase", Integer.toString(policy.mActivePasswordUpperCase));
                out.attribute(null, "lowercase", Integer.toString(policy.mActivePasswordLowerCase));
                out.attribute(null, "letters", Integer.toString(policy.mActivePasswordLetters));
                out.attribute(null, "numeric", Integer.toString(policy.mActivePasswordNumeric));
                out.attribute(null, "symbols", Integer.toString(policy.mActivePasswordSymbols));
                out.attribute(null, "nonletter", Integer.toString(policy.mActivePasswordNonLetter));
                out.endTag(null, "active-password2");
            }
            out.endTag(null, "policies");
            out.endDocument();
            journal.commit();
            try {
                stream2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            journal.rollback();
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:?, code lost:
        return;
     */
    private void loadVisitorSettingsLocked(DeviceVisitorPolicyData policy, int userHandle) {
        FileInputStream stream = null;
        File file = makeJournaledFile2(userHandle).chooseForRead();
        try {
            stream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == 1 || type == 2) {
                    String tag = parser.getName();
                }
            }
            String tag2 = parser.getName();
            if ("policies".equals(tag2)) {
                int type2 = parser.next();
                int outerDepth = parser.getDepth();
                while (true) {
                    int next2 = parser.next();
                    int type3 = next2;
                    if (next2 == 1 || (type3 == 3 && parser.getDepth() <= outerDepth)) {
                        try {
                            break;
                        } catch (IOException e) {
                            return;
                        }
                    } else if (type3 != 3) {
                        if (type3 != 4) {
                            String tag3 = parser.getName();
                            if ("active-password2".equals(tag3)) {
                                policy.mActivePasswordQuality = Integer.parseInt(parser.getAttributeValue(null, "quality"));
                                policy.mActivePasswordLength = Integer.parseInt(parser.getAttributeValue(null, "length"));
                                policy.mActivePasswordUpperCase = Integer.parseInt(parser.getAttributeValue(null, "uppercase"));
                                policy.mActivePasswordLowerCase = Integer.parseInt(parser.getAttributeValue(null, "lowercase"));
                                policy.mActivePasswordLetters = Integer.parseInt(parser.getAttributeValue(null, "letters"));
                                policy.mActivePasswordNumeric = Integer.parseInt(parser.getAttributeValue(null, "numeric"));
                                policy.mActivePasswordSymbols = Integer.parseInt(parser.getAttributeValue(null, "symbols"));
                                policy.mActivePasswordNonLetter = Integer.parseInt(parser.getAttributeValue(null, "nonletter"));
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                Slog.w(TAG, "Unknown tag: " + tag3);
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                }
            } else {
                throw new XmlPullParserException("Settings do not start with policies tag: found " + tag2);
            }
        } catch (NullPointerException e2) {
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            if (stream != null) {
                stream.close();
            }
        } catch (NumberFormatException e3) {
            Slog.w(TAG, "failed parsing " + file + " " + e3);
            if (stream != null) {
                stream.close();
            }
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "failed parsing " + file + " " + e4);
            if (stream != null) {
                stream.close();
            }
        } catch (FileNotFoundException e5) {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e6) {
            Slog.w(TAG, "failed parsing " + file + " " + e6);
            if (stream != null) {
                stream.close();
            }
        } catch (IndexOutOfBoundsException e7) {
            Slog.w(TAG, "failed parsing " + file + " " + e7);
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
    }

    private boolean isPrivacyModeEnabled() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), PRIVACY_MODE_ON, 0) == 1 && isFeatrueSupported();
    }

    private static boolean isFeatrueSupported() {
        return SystemProperties.getBoolean("ro.config.hw_privacymode", false);
    }

    public void systemReady(int phase) {
        HwDevicePolicyManagerService.super.systemReady(phase);
        if (isFeatrueSupported()) {
            if (this.mHasFeature) {
                Slog.w(TAG, "systemReady");
                synchronized (getLockObject()) {
                    loadVisitorSettingsLocked(getVisitorUserData(0), 0);
                }
            } else {
                return;
            }
        }
        if (phase == 1000) {
            listenForUserSwitches();
        }
        if (phase == 550) {
            Log.i(TAG, "systemReady to setDpcInAELaunchableAndBackgroundRunnable phase:" + phase);
            setDpcInAELaunchableAndBackgroundRunnable(false);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0071, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0073, code lost:
        return false;
     */
    public boolean isActivePasswordSufficient(int userHandle, boolean parent) {
        if (!isPrivacyModeEnabled()) {
            return HwDevicePolicyManagerService.super.isActivePasswordSufficient(userHandle, parent);
        }
        boolean z = false;
        if (!HwDevicePolicyManagerService.super.isActivePasswordSufficient(userHandle, parent)) {
            return false;
        }
        Slog.w(TAG, "super is ActivePassword Sufficient");
        if (!this.mHasFeature) {
            return true;
        }
        synchronized (getLockObject()) {
            DeviceVisitorPolicyData policy = getVisitorUserData(userHandle);
            if (policy.mActivePasswordQuality >= getPasswordQuality(null, userHandle, parent)) {
                if (policy.mActivePasswordLength >= getPasswordMinimumLength(null, userHandle, parent)) {
                    if (policy.mActivePasswordQuality != 393216) {
                        return true;
                    }
                    if (policy.mActivePasswordUpperCase >= getPasswordMinimumUpperCase(null, userHandle, parent) && policy.mActivePasswordLowerCase >= getPasswordMinimumLowerCase(null, userHandle, parent) && policy.mActivePasswordLetters >= getPasswordMinimumLetters(null, userHandle, parent) && policy.mActivePasswordNumeric >= getPasswordMinimumNumeric(null, userHandle, parent) && policy.mActivePasswordSymbols >= getPasswordMinimumSymbols(null, userHandle, parent) && policy.mActivePasswordNonLetter >= getPasswordMinimumNonLetter(null, userHandle, parent)) {
                        z = true;
                    }
                }
            }
        }
    }

    public void setActiveVisitorPasswordState(int quality, int length, int letters, int uppercase, int lowercase, int numbers, int symbols, int nonletter, int userHandle) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            DeviceVisitorPolicyData p = getVisitorUserData(userHandle);
            validateQualityConstant(quality);
            synchronized (getLockObject()) {
                if (!(p.mActivePasswordQuality == quality && p.mActivePasswordLength == length && p.mFailedPasswordAttempts == 0 && p.mActivePasswordLetters == letters && p.mActivePasswordUpperCase == uppercase && p.mActivePasswordLowerCase == lowercase && p.mActivePasswordNumeric == numbers && p.mActivePasswordSymbols == symbols && p.mActivePasswordNonLetter == nonletter)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        p.mActivePasswordQuality = quality;
                        p.mActivePasswordLength = length;
                        p.mActivePasswordLetters = letters;
                        p.mActivePasswordLowerCase = lowercase;
                        p.mActivePasswordUpperCase = uppercase;
                        p.mActivePasswordNumeric = numbers;
                        p.mActivePasswordSymbols = symbols;
                        p.mActivePasswordNonLetter = nonletter;
                        p.mFailedPasswordAttempts = 0;
                        saveVisitorSettingsLock(userHandle);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAllowSimplePassword(ComponentName who, boolean mode, int userHandle) {
        if (this.mHasFeature && isSimplePwdOpen) {
            HwLog.d(TAG, "setAllowSimplePassword mode =" + mode);
            synchronized (getLockObject()) {
                if (who != null) {
                    DevicePolicyManagerService.ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0);
                    if (ap.allowSimplePassword != mode) {
                        ap.allowSimplePassword = mode;
                        saveSettingsLocked(userHandle);
                    }
                } else {
                    throw new NullPointerException("ComponentName is null");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        return r3;
     */
    public boolean getAllowSimplePassword(ComponentName who, int userHandle) {
        if (!this.mHasFeature || !isSimplePwdOpen) {
            return true;
        }
        synchronized (getLockObject()) {
            boolean mode = true;
            if (who != null) {
                try {
                    DevicePolicyManagerService.ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                    boolean z = admin != null ? admin.allowSimplePassword : true;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (mode && mode != admin2.allowSimplePassword) {
                        mode = admin2.allowSimplePassword;
                    }
                }
                HwLog.d(TAG, "getAllowSimplePassword mode =" + mode);
                return mode;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void saveCurrentPwdStatus(boolean isCurrentPwdSimple, int userHandle) {
        if (this.mHasFeature && isSimplePwdOpen) {
            synchronized (getLockObject()) {
                getUserData(userHandle).mIsCurrentPwdSimple = isCurrentPwdSimple;
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int i = code;
        Parcel parcel = data;
        Parcel parcel2 = reply;
        if (this.mProcessor.processTransaction(i, parcel, parcel2) || this.mProcessor.processTransactionWithPolicyName(i, parcel, parcel2)) {
            return true;
        }
        if (i != 1003) {
            ComponentName _arg0 = null;
            boolean _arg1 = false;
            switch (i) {
                case 7001:
                    if (isSimplePwdOpen) {
                        parcel.enforceInterface(DPMDESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setAllowSimplePassword(_arg0, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    }
                    break;
                case 7002:
                    if (isSimplePwdOpen != 0) {
                        parcel.enforceInterface(DPMDESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result = getAllowSimplePassword(_arg0, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    }
                    break;
                case 7003:
                    if (isSimplePwdOpen) {
                        parcel.enforceInterface(DPMDESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        saveCurrentPwdStatus(_arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    }
                    break;
            }
            return HwDevicePolicyManagerService.super.onTransact(code, data, reply, flags);
        }
        Slog.w(TAG, "transaction_setActiveVisitorPasswordState");
        parcel.enforceInterface("com.android.internal.widget.ILockSettings");
        setActiveVisitorPasswordState(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
        reply.writeNoException();
        return true;
    }

    private void listenForUserSwitches() {
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
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
        enforceFullCrossUsersPermission(userHandle);
        if (userHandle != 0) {
            throw new IllegalArgumentException("Invalid userId " + userHandle + ",should be:" + 0);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        if (r7.mHwAdminCache == null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0052, code lost:
        r7.mHwAdminCache.syncHwAdminCache(0, isWifiDisabled(null, r10));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        return;
     */
    public void setWifiDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have wifi MDM permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableWifi != disabled) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        WifiManager mWifiManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
                        if (!mWifiManager.isWifiEnabled() || !disabled || mWifiManager.setWifiEnabled(false)) {
                            Binder.restoreCallingIdentity(callingId);
                            admin.disableWifi = disabled;
                            saveSettingsLocked(userHandle);
                        }
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    public boolean isWifiDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableWifi;
                    return z;
                } catch (Throwable admin) {
                    throw admin;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableWifi) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public boolean isBluetoothDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableBluetooth;
                    return z;
                } catch (Throwable admin) {
                    throw admin;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableBluetooth) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0052, code lost:
        if (r7.mHwAdminCache == null) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0054, code lost:
        r7.mHwAdminCache.syncHwAdminCache(8, isBluetoothDisabled(null, r10));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0060, code lost:
        return;
     */
    public void setBluetoothDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_BLUETOOTH", "does not have bluethooth MDM permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableBluetooth != disabled) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        BluetoothAdapter mBTAdapter = ((BluetoothManager) this.mContext.getSystemService("bluetooth")).getAdapter();
                        if (!mBTAdapter.isEnabled() || !disabled || mBTAdapter.disable()) {
                            Binder.restoreCallingIdentity(callingId);
                            admin.disableBluetooth = disabled;
                            saveSettingsLocked(userHandle);
                        }
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    public void setWifiApDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have Wifi AP MDM permission!");
        synchronized (getLockObject()) {
            enforceUserRestrictionPermission(who, "no_config_tethering", userHandle);
            AbsDevicePolicyManagerService.HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
            if (ap.disableWifiAp != disabled) {
                ap.disableWifiAp = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_config_tethering", userHandle);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isWifiApDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin != null) {
                        z = admin.disableWifiAp;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else if (!this.mUserManager.hasUserRestriction("no_config_tethering", new UserHandle(userHandle))) {
                return false;
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableWifiAp) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setBootLoaderDisabled(ComponentName who, boolean disabled, int userHandle) {
    }

    public boolean isBootLoaderDisabled(ComponentName who, int userHandle) {
        return false;
    }

    public void setUSBDataDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have USB MDM permission!");
        synchronized (getLockObject()) {
            enforceUserRestrictionPermission(who, "no_usb_file_transfer", userHandle);
            AbsDevicePolicyManagerService.HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
            if (ap.disableUSBData != disabled) {
                ap.disableUSBData = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_usb_file_transfer", userHandle);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isUSBDataDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
                    if (ap != null) {
                        z = ap.disableUSBData;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else if (!this.mUserManager.hasUserRestriction("no_usb_file_transfer", new UserHandle(userHandle))) {
                return false;
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin.mHwActiveAdmin != null && admin.mHwActiveAdmin.disableUSBData) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setExternalStorageDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_SDCARD", "does not have SDCARD MDM permission!");
        synchronized (getLockObject()) {
            enforceUserRestrictionPermission(who, "no_physical_media", userHandle);
            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableExternalStorage != disabled) {
                admin.disableExternalStorage = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_physical_media", userHandle);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isExternalStorageDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin != null) {
                        z = admin.disableExternalStorage;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else if (!this.mUserManager.hasUserRestriction("no_physical_media", new UserHandle(userHandle))) {
                return false;
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableExternalStorage) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setNFCDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NFC", "does not have NFC MDM permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableNFC != disabled) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
                        if (nfcAdapter != null) {
                            boolean nfcOriginalState = nfcAdapter.isEnabled();
                            if (disabled && nfcOriginalState) {
                                nfcAdapter.disable();
                            }
                        }
                        Binder.restoreCallingIdentity(callingId);
                        admin.disableNFC = disabled;
                        saveSettingsLocked(userHandle);
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

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isNFCDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin != null) {
                        z = admin.disableNFC;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableNFC) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setDataConnectivityDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CONNECTIVITY", "Does not hava data connectivity MDM permission.");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableDataConnectivity != disabled) {
                    admin.disableDataConnectivity = disabled;
                    saveSettingsLocked(userHandle);
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

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isDataConnectivityDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin != null) {
                        z = admin.disableDataConnectivity;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableDataConnectivity) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setVoiceDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "Does not hava phone disable MDM permission.");
        synchronized (getLockObject()) {
            enforceUserRestrictionPermission(who, "no_outgoing_calls", userHandle);
            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableVoice != disabled) {
                admin.disableVoice = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_outgoing_calls", userHandle);
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(1, isVoiceDisabled(null, userHandle));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isVoiceDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin != null) {
                        z = admin.disableVoice;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else if (!this.mUserManager.hasUserRestriction("no_outgoing_calls", new UserHandle(userHandle))) {
                return false;
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableVoice) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setSMSDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_MMS", "Does not hava SMS disable MDM permission.");
        synchronized (getLockObject()) {
            enforceUserRestrictionPermission(who, "no_sms", userHandle);
            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableSMS != disabled) {
                admin.disableSMS = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_sms", userHandle);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isSMSDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin != null) {
                        z = admin.disableSMS;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else if (!this.mUserManager.hasUserRestriction("no_sms", new UserHandle(userHandle))) {
                return false;
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableSMS) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0036, code lost:
        return;
     */
    public void setStatusBarExpandPanelDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (getLockObject()) {
            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableStatusBarExpandPanel != disabled) {
                if (!disabled || setStatusBarPanelDisabledInternal(userHandle)) {
                    admin.disableStatusBarExpandPanel = disabled;
                    saveSettingsLocked(userHandle);
                    if (!disabled) {
                        setStatusBarPanelEnableInternal(false, userHandle);
                    }
                } else {
                    Log.w(TAG, "cannot set statusBar disabled");
                }
            }
        }
    }

    private boolean setStatusBarPanelDisabledInternal(int userHandle) {
        long callingId = Binder.clearCallingIdentity();
        try {
            StatusBarManager statusBar = (StatusBarManager) this.mContext.getSystemService("statusbar");
            if (statusBar == null) {
                Log.w(TAG, "statusBar is null");
                return false;
            }
            statusBar.disable(STATUS_BAR_DISABLE_MASK);
            Binder.restoreCallingIdentity(callingId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "failed to set statusBar disabled.");
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private boolean setStatusBarPanelEnableInternal(boolean forceEnable, int userHandle) {
        long callingId = Binder.clearCallingIdentity();
        try {
            StatusBarManager statusBar = (StatusBarManager) this.mContext.getSystemService("statusbar");
            if (statusBar == null) {
                Log.w(TAG, "statusBar is null");
                return false;
            }
            if (forceEnable) {
                statusBar.disable(0);
            } else if (!isStatusBarExpandPanelDisabled(null, userHandle)) {
                statusBar.disable(0);
            }
            Binder.restoreCallingIdentity(callingId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "failed to set statusBar enabled.");
            return false;
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isStatusBarExpandPanelDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin != null) {
                        z = admin.disableStatusBarExpandPanel;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableStatusBarExpandPanel) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void hangupCalling(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "Does not hava hangup calling permission.");
        synchronized (getLockObject()) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            } else if (getHwActiveAdmin(who, userHandle) != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    TelephonyManager.from(this.mContext).endCall();
                    UiThread.getHandler().post(new Runnable() {
                        public void run() {
                            if (HwDevicePolicyManagerService.this.mErrorDialog != null) {
                                HwDevicePolicyManagerService.this.mErrorDialog.dismiss();
                                AlertDialog unused = HwDevicePolicyManagerService.this.mErrorDialog = null;
                            }
                            AlertDialog unused2 = HwDevicePolicyManagerService.this.mErrorDialog = new AlertDialog.Builder(HwDevicePolicyManagerService.this.mContext, 33947691).setMessage(33686011).setPositiveButton(33686164, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    HwDevicePolicyManagerService.this.mErrorDialog.dismiss();
                                }
                            }).setCancelable(true).create();
                            HwDevicePolicyManagerService.this.mErrorDialog.getWindow().setType(2003);
                            HwDevicePolicyManagerService.this.mErrorDialog.show();
                        }
                    });
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
        }
    }

    public void installPackage(ComponentName who, String packagePath, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
        synchronized (getLockObject()) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            } else if (getHwActiveAdmin(who, userHandle) != null) {
                installPackage(packagePath, who.getPackageName());
            }
        }
    }

    public void uninstallPackage(ComponentName who, String packageName, boolean keepData, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            synchronized (getLockObject()) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                } else if (getHwActiveAdmin(who, userHandle) != null) {
                    uninstallPackage(packageName, keepData);
                }
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
    }

    /* JADX INFO: finally extract failed */
    public void clearPackageData(ComponentName who, String packageName, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            synchronized (getLockObject()) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                } else if (!TextUtils.isEmpty(packageName)) {
                    enforceCheckNotSystemApp(packageName, userHandle);
                    if (getHwActiveAdmin(who, userHandle) != null) {
                        long id = Binder.clearCallingIdentity();
                        try {
                            ((ActivityManager) this.mContext.getSystemService("activity")).clearApplicationUserData(packageName, null);
                            Binder.restoreCallingIdentity(id);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(id);
                            throw th;
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

    public void enableInstallPackage(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    admin.disableInstallSource = false;
                    admin.installSourceWhitelist = null;
                }
                saveSettingsLocked(userHandle);
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(2, isInstallSourceDisabled(null, userHandle));
        }
    }

    public void disableInstallSource(ComponentName who, List<String> whitelist, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(whitelist)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (whitelist != null) {
                        if (!whitelist.isEmpty()) {
                            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                            admin.disableInstallSource = true;
                            if (admin.installSourceWhitelist == null) {
                                admin.installSourceWhitelist = new ArrayList();
                            }
                            HwDevicePolicyManagerServiceUtil.isOverLimit(admin.installSourceWhitelist, whitelist);
                            HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.installSourceWhitelist, whitelist);
                            saveSettingsLocked(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(2, isInstallSourceDisabled(null, userHandle));
                this.mHwAdminCache.syncHwAdminCache(3, getInstallPackageSourceWhiteList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + whitelist + " is invalid.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return r1;
     */
    public boolean isInstallSourceDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            boolean z = false;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin != null) {
                        z = admin.disableInstallSource;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableInstallSource) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        return r2;
     */
    public List<String> getInstallPackageSourceWhiteList(ComponentName who, int userHandle) {
        List<String> list;
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.installSourceWhitelist != null) {
                        if (!admin.installSourceWhitelist.isEmpty()) {
                            list = admin.installSourceWhitelist;
                        }
                    }
                    list = null;
                } catch (Throwable admin2) {
                    throw admin2;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> whiteList = new ArrayList<>();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin3 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin3.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(whiteList, admin3.mHwActiveAdmin.installSourceWhitelist);
                    }
                }
                return whiteList;
            }
        }
    }

    public void addPersistentApp(ComponentName who, List<String> packageNames, int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                            if (admin.persistentAppList == null) {
                                admin.persistentAppList = new ArrayList();
                            }
                            HwDevicePolicyManagerServiceUtil.isOverLimit(getPersistentApp(null, userHandle), packageNames, 3);
                            filterOutSystemAppList(packageNames, userHandle);
                            HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.persistentAppList, packageNames);
                            saveSettingsLocked(userHandle);
                            sendPersistentAppToIAware(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(4, getPersistentApp(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    public void removePersistentApp(ComponentName who, List<String> packageNames, int userHandle) {
        enforceFullCrossUsersPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).persistentAppList, packageNames);
                            saveSettingsLocked(userHandle);
                            sendPersistentAppToIAware(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(4, getPersistentApp(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    private void sendPersistentAppToIAware(int userHandle) {
        List<String> persistAppList = getPersistentApp(null, userHandle);
        if (persistAppList == null || persistAppList.size() <= 0) {
            ProcessCleaner.getInstance(this.mContext).removeProtectedListFromMDM();
            Slog.d(TAG, "removeProtectedListFromMDM for user " + userHandle);
            return;
        }
        ProcessCleaner.getInstance(this.mContext).setProtectedListFromMDM(persistAppList);
        Slog.d(TAG, "setProtectedListFromMDM for user " + userHandle + ":" + persistAppList);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        return r1;
     */
    public List<String> getPersistentApp(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            List<String> list = null;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.persistentAppList != null) {
                        if (!admin.persistentAppList.isEmpty()) {
                            list = admin.persistentAppList;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> totalList = new ArrayList<>();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(totalList, admin2.mHwActiveAdmin.persistentAppList);
                    }
                }
                if (totalList.isEmpty() == 0) {
                    list = totalList;
                }
            }
        }
    }

    public void addDisallowedRunningApp(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                            if (admin.disallowedRunningAppList == null) {
                                admin.disallowedRunningAppList = new ArrayList();
                            }
                            HwDevicePolicyManagerServiceUtil.isOverLimit(admin.disallowedRunningAppList, packageNames);
                            HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.disallowedRunningAppList, packageNames);
                            saveSettingsLocked(userHandle);
                            for (String packageName : packageNames) {
                                killApplicationInner(packageName);
                            }
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(5, getDisallowedRunningApp(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    public void removeDisallowedRunningApp(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).disallowedRunningAppList, packageNames);
                            saveSettingsLocked(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(5, getDisallowedRunningApp(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        return r1;
     */
    public List<String> getDisallowedRunningApp(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            List<String> list = null;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.disallowedRunningAppList != null) {
                        if (!admin.disallowedRunningAppList.isEmpty()) {
                            list = admin.disallowedRunningAppList;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> totalList = new ArrayList<>();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(totalList, admin2.mHwActiveAdmin.disallowedRunningAppList);
                    }
                }
                if (totalList.isEmpty() == 0) {
                    list = totalList;
                }
            }
        }
    }

    public void addInstallPackageWhiteList(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                            if (admin.installPackageWhitelist == null) {
                                admin.installPackageWhitelist = new ArrayList();
                            }
                            HwDevicePolicyManagerServiceUtil.isOverLimit(admin.installPackageWhitelist, packageNames);
                            HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.installPackageWhitelist, packageNames);
                            saveSettingsLocked(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(6, getInstallPackageWhiteList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    public void removeInstallPackageWhiteList(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).installPackageWhitelist, packageNames);
                            saveSettingsLocked(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(6, getInstallPackageWhiteList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        return r1;
     */
    public List<String> getInstallPackageWhiteList(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            List<String> list = null;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.installPackageWhitelist != null) {
                        if (!admin.installPackageWhitelist.isEmpty()) {
                            list = admin.installPackageWhitelist;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> whitelist = new ArrayList<>();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(whitelist, admin2.mHwActiveAdmin.installPackageWhitelist);
                    }
                }
                if (whitelist.isEmpty() == 0) {
                    list = whitelist;
                }
            }
        }
    }

    public void addDisallowedUninstallPackages(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                            if (admin.disallowedUninstallPackageList == null) {
                                admin.disallowedUninstallPackageList = new ArrayList();
                            }
                            HwDevicePolicyManagerServiceUtil.isOverLimit(admin.disallowedUninstallPackageList, packageNames);
                            filterOutSystemAppList(packageNames, userHandle);
                            HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.disallowedUninstallPackageList, packageNames);
                            saveSettingsLocked(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(7, getDisallowedUninstallPackageList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    public void removeDisallowedUninstallPackages(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).disallowedUninstallPackageList, packageNames);
                            saveSettingsLocked(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(7, getDisallowedUninstallPackageList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        return r1;
     */
    public List<String> getDisallowedUninstallPackageList(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            List<String> list = null;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.disallowedUninstallPackageList != null) {
                        if (!admin.disallowedUninstallPackageList.isEmpty()) {
                            list = admin.disallowedUninstallPackageList;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> blacklist = new ArrayList<>();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(blacklist, admin2.mHwActiveAdmin.disallowedUninstallPackageList);
                    }
                }
                if (blacklist.isEmpty() == 0) {
                    list = blacklist;
                }
            }
        }
    }

    public void addDisabledDeactivateMdmPackages(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                            if (admin.disabledDeactiveMdmPackagesList == null) {
                                admin.disabledDeactiveMdmPackagesList = new ArrayList();
                            }
                            HwDevicePolicyManagerServiceUtil.isOverLimit(admin.disabledDeactiveMdmPackagesList, packageNames);
                            HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.disabledDeactiveMdmPackagesList, packageNames);
                            saveSettingsLocked(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(18, getDisabledDeactivateMdmPackageList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    public void removeDisabledDeactivateMdmPackages(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    if (packageNames != null) {
                        if (!packageNames.isEmpty()) {
                            HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).disabledDeactiveMdmPackagesList, packageNames);
                            saveSettingsLocked(userHandle);
                        }
                    }
                    throw new IllegalArgumentException("packageNames is null or empty");
                }
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(18, getDisabledDeactivateMdmPackageList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        return r1;
     */
    public List<String> getDisabledDeactivateMdmPackageList(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            List<String> list = null;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.disabledDeactiveMdmPackagesList != null) {
                        if (!admin.disabledDeactiveMdmPackagesList.isEmpty()) {
                            list = admin.disabledDeactiveMdmPackagesList;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> blacklist = new ArrayList<>();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(blacklist, admin2.mHwActiveAdmin.disabledDeactiveMdmPackagesList);
                    }
                }
                if (blacklist.isEmpty() == 0) {
                    list = blacklist;
                }
            }
        }
    }

    public void killApplicationProcess(ComponentName who, String packageName, int userHandle) {
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
            synchronized (getLockObject()) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                } else if (TextUtils.isEmpty(packageName)) {
                    throw new IllegalArgumentException("Package name is empty");
                } else if (!packageName.equals(who.getPackageName())) {
                    enforceCheckNotSystemApp(packageName, userHandle);
                    if (getHwActiveAdmin(who, userHandle) != null) {
                        killApplicationInner(packageName);
                    }
                } else {
                    throw new IllegalArgumentException("Can not kill the caller application");
                }
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:?, code lost:
        android.app.ActivityManager.getService().removeTask(r5.id);
     */
    private void killApplicationInner(String packageName) {
        ActivityManager am;
        long ident = Binder.clearCallingIdentity();
        try {
            am = (ActivityManager) this.mContext.getSystemService("activity");
            Iterator<ActivityManager.RunningTaskInfo> it = am.getRunningTasks(10000).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ActivityManager.RunningTaskInfo ti = it.next();
                if (packageName.equals(ti.baseActivity.getPackageName())) {
                    break;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "killApplicationInner exception is " + e.getMessage());
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
        am.forceStopPackage(packageName);
        Binder.restoreCallingIdentity(ident);
    }

    /* JADX INFO: finally extract failed */
    public void shutdownOrRebootDevice(int code, ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                try {
                    IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                    if (power == null) {
                        Binder.restoreCallingIdentity(callingId);
                        return;
                    }
                    if (code == 1501) {
                        power.shutdown(false, null, false);
                    } else if (code == 1502) {
                        power.reboot(false, null, false);
                    }
                    Binder.restoreCallingIdentity(callingId);
                } catch (RemoteException e) {
                    try {
                        Log.e(TAG, "exception is " + e.getMessage());
                        Binder.restoreCallingIdentity(callingId);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingId);
                        throw th;
                    }
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    public void configExchangeMailProvider(ComponentName who, Bundle para, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_EMAIL", "does not have EMAIL MDM permission!");
        synchronized (getLockObject()) {
            if (who == null || para == null) {
                throw new IllegalArgumentException("ComponentName or para is null");
            } else if (HwDevicePolicyManagerServiceUtil.isValidExchangeParameter(para)) {
                AbsDevicePolicyManagerService.HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
                if (ap.mailProviderlist == null) {
                    ap.mailProviderlist = new ArrayList();
                    ap.mailProviderlist.add(para);
                    saveSettingsLocked(userHandle);
                } else if (ap.mailProviderlist.size() + 1 <= 20) {
                    boolean isAlready = false;
                    Bundle provider = null;
                    Iterator it = ap.mailProviderlist.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Bundle each = (Bundle) it.next();
                        if (HwDevicePolicyManagerServiceUtil.matchProvider(para.getString("domain"), each.getString("domain"))) {
                            isAlready = true;
                            provider = each;
                            break;
                        }
                    }
                    if (isAlready && provider != null) {
                        ap.mailProviderlist.remove(provider);
                    }
                    ap.mailProviderlist.add(para);
                    saveSettingsLocked(userHandle);
                } else {
                    throw new IllegalArgumentException("already exceeds max number.");
                }
            } else {
                throw new IllegalArgumentException("some paremeter is null");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0044, code lost:
        return r0;
     */
    public Bundle getMailProviderForDomain(ComponentName who, String domain, int userHandle) {
        Bundle bundle = null;
        if (userHandle != 0) {
            return null;
        }
        if (!TextUtils.isEmpty(domain)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    try {
                        AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                        if (admin.mailProviderlist == null) {
                            return null;
                        }
                        boolean matched = false;
                        Bundle retProvider = null;
                        Iterator it = admin.mailProviderlist.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            Bundle provider = (Bundle) it.next();
                            matched = HwDevicePolicyManagerServiceUtil.matchProvider(domain, provider.getString("domain"));
                            if (matched) {
                                retProvider = provider;
                                break;
                            }
                        }
                        if (matched) {
                            bundle = retProvider;
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                    int N = policy.mAdminList.size();
                    for (int i = 0; i < N; i++) {
                        DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                        if (!(admin2.mHwActiveAdmin == null || admin2.mHwActiveAdmin.mailProviderlist == null)) {
                            for (Bundle provider2 : admin2.mHwActiveAdmin.mailProviderlist) {
                                if (HwDevicePolicyManagerServiceUtil.matchProvider(domain, provider2.getString("domain"))) {
                                    return provider2;
                                }
                            }
                            continue;
                        }
                    }
                    return null;
                }
            }
        } else {
            throw new IllegalArgumentException("domain is empty.");
        }
    }

    public boolean isRooted(ComponentName who, int userHandle) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                String currentState = SystemProperties.get("persist.sys.root.status");
                if (!TextUtils.isEmpty(currentState)) {
                    if ("0".equals(currentState)) {
                        return false;
                    }
                }
                return true;
            }
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    public void setSafeModeDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableSafeMode != disabled) {
                    admin.disableSafeMode = disabled;
                    saveSettingsLocked(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(10, isSafeModeDisabled(null, userHandle));
        }
    }

    public boolean isSafeModeDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableSafeMode;
                    return z;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin.mHwActiveAdmin != null && admin.mHwActiveAdmin.disableSafeMode) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setAdbDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have MDM_USB permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableAdb != disabled) {
                    admin.disableAdb = disabled;
                    saveSettingsLocked(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        long identityToken = Binder.clearCallingIdentity();
        if (disabled) {
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "adb_enabled", 0) > 0) {
                Settings.Global.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
            }
        }
        Binder.restoreCallingIdentity(identityToken);
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(11, isAdbDisabled(null, userHandle));
        }
    }

    public boolean isAdbDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableAdb;
                    return z;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin.mHwActiveAdmin != null && admin.mHwActiveAdmin.disableAdb) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setUSBOtgDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have MDM_USB permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableUSBOtg != disabled) {
                    admin.disableUSBOtg = disabled;
                    saveSettingsLocked(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        long identityToken = Binder.clearCallingIdentity();
        StorageManager sm = (StorageManager) this.mContext.getSystemService("storage");
        for (StorageVolume storageVolume : sm.getVolumeList()) {
            if (storageVolume.isRemovable() && "mounted".equals(sm.getVolumeState(storageVolume.getPath()))) {
                VolumeInfo volumeInfo = sm.findVolumeByUuid(storageVolume.getUuid());
                if (volumeInfo != null) {
                    DiskInfo diskInfo = volumeInfo.getDisk();
                    if (diskInfo != null && diskInfo.isUsb()) {
                        Slog.e(TAG, "find usb otg device mounted , umounted it");
                        sm.unmount(storageVolume.getId());
                    }
                }
            }
        }
        Binder.restoreCallingIdentity(identityToken);
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(12, isUSBOtgDisabled(null, userHandle));
        }
    }

    public boolean isUSBOtgDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableUSBOtg;
                    return z;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin.mHwActiveAdmin != null && admin.mHwActiveAdmin.disableUSBOtg) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setGPSDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_LOCATION", "does not have MDM_LOCATION permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableGPS != disabled) {
                    admin.disableGPS = disabled;
                    saveSettingsLocked(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        if (isGPSTurnOn(who, userHandle) && disabled) {
            turnOnGPS(who, false, userHandle);
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(13, isGPSDisabled(null, userHandle));
        }
    }

    public boolean isGPSDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableGPS;
                    return z;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin.mHwActiveAdmin != null && admin.mHwActiveAdmin.disableGPS) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void turnOnGPS(ComponentName who, boolean on, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_LOCATION", "does not have MDM_LOCATION permission!");
        if (who != null) {
            synchronized (getLockObject()) {
                getHwActiveAdmin(who, userHandle);
            }
            if (isGPSTurnOn(who, userHandle) != on) {
                long identityToken = Binder.clearCallingIdentity();
                if (!Settings.Secure.setLocationProviderEnabledForUser(this.mContext.getContentResolver(), "gps", on, ActivityManager.getCurrentUser())) {
                    Log.e(TAG, "setLocationProviderEnabledForUser failed");
                }
                Binder.restoreCallingIdentity(identityToken);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    public boolean isGPSTurnOn(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            getHwActiveAdmin(who, userHandle);
        }
        long identityToken = Binder.clearCallingIdentity();
        boolean isGPSEnabled = Settings.Secure.isLocationProviderEnabledForUser(this.mContext.getContentResolver(), "gps", ActivityManager.getCurrentUser());
        Binder.restoreCallingIdentity(identityToken);
        return isGPSEnabled;
    }

    public void setTaskButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableTaskKey != disabled) {
                    admin.disableTaskKey = disabled;
                    saveSettingsLocked(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(15, isTaskButtonDisabled(null, userHandle));
        }
    }

    public boolean isTaskButtonDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableTaskKey;
                    return z;
                } catch (Throwable admin) {
                    throw admin;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableTaskKey) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setHomeButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableHomeKey != disabled) {
                    admin.disableHomeKey = disabled;
                    saveSettingsLocked(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(14, isHomeButtonDisabled(null, userHandle));
        }
    }

    public boolean isHomeButtonDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableHomeKey;
                    return z;
                } catch (Throwable admin) {
                    throw admin;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableHomeKey) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setBackButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.disableBackKey != disabled) {
                    admin.disableBackKey = disabled;
                    saveSettingsLocked(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(16, isBackButtonDisabled(null, userHandle));
        }
    }

    public boolean isBackButtonDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableBackKey;
                    return z;
                } catch (Throwable admin) {
                    throw admin;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableBackKey) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void setSysTime(ComponentName who, long millis, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device manager MDM permission!");
        synchronized (getLockObject()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                long id = Binder.clearCallingIdentity();
                SystemClock.setCurrentTimeMillis(millis);
                Binder.restoreCallingIdentity(id);
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    public void setCustomSettingsMenu(ComponentName who, List<String> menusToDelete, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (getLockObject()) {
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
                Settings.Global.putStringForUser(this.mContext.getContentResolver(), SETTINGS_MENUS_REMOVE, "", userHandle);
                Binder.restoreCallingIdentity(callingId);
                return;
            }
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    public void setDefaultLauncher(ComponentName who, String packageName, String className, int userHandle) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName is null or empty");
        } else if (!LauncherUtils.checkLauncherPermisson(this.mContext, packageName)) {
            throw new IllegalArgumentException("The Launcher's signature is different from the host app's!");
        } else if (!TextUtils.isEmpty(className)) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.SDK_LAUNCHER", "Does not have sdk_launcher permission.");
            synchronized (getLockObject()) {
                if (who != null) {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    long callingId = Binder.clearCallingIdentity();
                    LauncherUtils.setDefaultLauncher(this.mContext, packageName, className);
                    Binder.restoreCallingIdentity(callingId);
                    admin.disableChangeLauncher = true;
                    saveSettingsLocked(userHandle);
                    if (this.mHwAdminCache != null) {
                        this.mHwAdminCache.syncHwAdminCache(17, isChangeLauncherDisabled(null, userHandle));
                    }
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
        } else {
            throw new IllegalArgumentException("className is null or empty");
        }
    }

    public void clearDefaultLauncher(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.SDK_LAUNCHER", "Does not have sdk_launcher permission.");
        synchronized (getLockObject()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle).disableChangeLauncher = false;
                saveSettingsLocked(userHandle);
                if (this.mHwAdminCache != null) {
                    this.mHwAdminCache.syncHwAdminCache(17, isChangeLauncherDisabled(null, userHandle));
                }
                long callingId = Binder.clearCallingIdentity();
                LauncherUtils.clearDefaultLauncher(this.mContext);
                Binder.restoreCallingIdentity(callingId);
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    public boolean isChangeLauncherDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdmin(who, userHandle).disableChangeLauncher;
                    return z;
                } catch (Throwable admin) {
                    throw admin;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableChangeLauncher) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public Bitmap captureScreen(ComponentName who, int userHandle) {
        Bitmap bmp;
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CAPTURE_SCREEN", "Does not have MDM_CAPTURE_SCREEN permission.");
        synchronized (getLockObject()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                bmp = CaptureScreenUtils.captureScreen(this.mContext);
                Binder.restoreCallingIdentity(callingId);
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        return bmp;
    }

    public void addApn(ComponentName who, Map<String, String> apnInfo, int userHandle) {
        if (apnInfo == null || apnInfo.isEmpty()) {
            throw new IllegalArgumentException("apnInfo is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
        synchronized (getLockObject()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                ApnUtils.addApn(this.mContext.getContentResolver(), apnInfo);
                Binder.restoreCallingIdentity(callingId);
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
    }

    public void deleteApn(ComponentName who, String apnId, int userHandle) {
        if (!TextUtils.isEmpty(apnId)) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
            synchronized (getLockObject()) {
                if (who != null) {
                    getHwActiveAdmin(who, userHandle);
                    long callingId = Binder.clearCallingIdentity();
                    ApnUtils.deleteApn(this.mContext.getContentResolver(), apnId);
                    Binder.restoreCallingIdentity(callingId);
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
            return;
        }
        throw new IllegalArgumentException("apnId is empty.");
    }

    public void updateApn(ComponentName who, Map<String, String> apnInfo, String apnId, int userHandle) {
        if (apnInfo == null || apnInfo.isEmpty()) {
            throw new IllegalArgumentException("apnInfo is empty.");
        } else if (!TextUtils.isEmpty(apnId)) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
            synchronized (getLockObject()) {
                if (who != null) {
                    getHwActiveAdmin(who, userHandle);
                    long callingId = Binder.clearCallingIdentity();
                    ApnUtils.updateApn(this.mContext.getContentResolver(), apnInfo, apnId);
                    Binder.restoreCallingIdentity(callingId);
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
        } else {
            throw new IllegalArgumentException("apnId is empty.");
        }
    }

    public void setPreferApn(ComponentName who, String apnId, int userHandle) {
        if (!TextUtils.isEmpty(apnId)) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
            synchronized (getLockObject()) {
                if (who != null) {
                    getHwActiveAdmin(who, userHandle);
                    long callingId = Binder.clearCallingIdentity();
                    ApnUtils.setPreferApn(this.mContext.getContentResolver(), apnId);
                    Binder.restoreCallingIdentity(callingId);
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
            return;
        }
        throw new IllegalArgumentException("apnId is empty.");
    }

    public List<String> queryApn(ComponentName who, Map<String, String> apnInfo, int userHandle) {
        List<String> ids;
        if (apnInfo == null || apnInfo.isEmpty()) {
            throw new IllegalArgumentException("apnInfo is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        synchronized (getLockObject()) {
            if (who != null) {
                getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                ids = ApnUtils.queryApn(this.mContext.getContentResolver(), apnInfo);
                Binder.restoreCallingIdentity(callingId);
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        return ids;
    }

    public Map<String, String> getApnInfo(ComponentName who, String apnId, int userHandle) {
        Map<String, String> apnInfo;
        if (!TextUtils.isEmpty(apnId)) {
            enforceHwCrossUserPermission(userHandle);
            synchronized (getLockObject()) {
                if (who != null) {
                    getHwActiveAdmin(who, userHandle);
                    long callingId = Binder.clearCallingIdentity();
                    apnInfo = ApnUtils.getApnInfo(this.mContext.getContentResolver(), apnId);
                    Binder.restoreCallingIdentity(callingId);
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
            return apnInfo;
        }
        throw new IllegalArgumentException("apnId is empty.");
    }

    public void addNetworkAccessWhitelist(ComponentName who, List<String> addrList, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have network_manager MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidIPAddrs(addrList)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.networkAccessWhitelist == null) {
                        admin.networkAccessWhitelist = new ArrayList();
                    }
                    HwDevicePolicyManagerServiceUtil.isAddrOverLimit(admin.networkAccessWhitelist, addrList);
                    HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.networkAccessWhitelist, addrList);
                    saveSettingsLocked(userHandle);
                    setNetworkAccessWhitelist(admin.networkAccessWhitelist);
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(9, getNetworkAccessWhitelist(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("addrlist invalid");
    }

    public void removeNetworkAccessWhitelist(ComponentName who, List<String> addrList, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have network_manager MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidIPAddrs(addrList)) {
            synchronized (getLockObject()) {
                if (who != null) {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    HwDevicePolicyManagerServiceUtil.removeItemsFromList(admin.networkAccessWhitelist, addrList);
                    saveSettingsLocked(userHandle);
                    setNetworkAccessWhitelist(admin.networkAccessWhitelist);
                } else {
                    throw new IllegalArgumentException("ComponentName is null");
                }
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(9, getNetworkAccessWhitelist(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("addrlist invalid");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001d, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0051, code lost:
        return r1;
     */
    public List<String> getNetworkAccessWhitelist(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            List<String> list = null;
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                    if (admin.networkAccessWhitelist != null) {
                        if (!admin.networkAccessWhitelist.isEmpty()) {
                            list = admin.networkAccessWhitelist;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> addrList = new ArrayList<>();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(addrList, admin2.mHwActiveAdmin.networkAccessWhitelist);
                    }
                }
                if (addrList.isEmpty() == 0) {
                    list = addrList;
                }
            }
        }
    }

    private void setNetworkAccessWhitelist(List<String> whitelist) {
        IBinder b = ServiceManager.getService("network_management");
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        if (b != null) {
            try {
                _data.writeInterfaceToken("android.os.INetworkManagementService");
                _data.writeStringList(whitelist);
                b.transact(HwArbitrationDEFS.MSG_STREAMING_VIDEO_BAD, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                Log.e(TAG, "setNetworkAccessWhitelist error", localRemoteException);
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public boolean getHwAdminCachedValue(int code) {
        int type = -1;
        if (code != 4009) {
            switch (code) {
                case HwArbitrationDEFS.MSG_SMARTNW_TOTAL_SETTING_CHANGED /*4001*/:
                    type = 0;
                    break;
                case HwArbitrationDEFS.MSG_SMARTNW_APP_SETTING_CHANGED /*4002*/:
                    type = 1;
                    break;
                case 4003:
                    type = 2;
                    break;
                default:
                    switch (code) {
                        case 4011:
                            type = 10;
                            break;
                        case 4012:
                            type = 11;
                            break;
                        case 4013:
                            type = 12;
                            break;
                        case 4014:
                            type = 13;
                            break;
                        case 4015:
                            type = 14;
                            break;
                        case 4016:
                            type = 15;
                            break;
                        case 4017:
                            type = 16;
                            break;
                        case 4018:
                            type = 17;
                            break;
                        default:
                            switch (code) {
                                case 4021:
                                    type = 21;
                                    break;
                                case 4022:
                                    type = 22;
                                    break;
                                case 4023:
                                    type = 23;
                                    break;
                                case 4024:
                                    type = 24;
                                    break;
                                case 4025:
                                    type = 25;
                                    break;
                                case 4026:
                                    type = 26;
                                    break;
                                default:
                                    switch (code) {
                                        case 5021:
                                            type = 29;
                                            break;
                                        case 5022:
                                            type = 32;
                                            break;
                                    }
                            }
                    }
            }
        } else {
            type = 8;
        }
        if (this.mHwAdminCache == null || type == -1) {
            return false;
        }
        return this.mHwAdminCache.getCachedValue(type);
    }

    public List<String> getHwAdminCachedList(int code) {
        List<String> result = null;
        int type = -1;
        if (code != 4010) {
            switch (code) {
                case 4004:
                    type = 3;
                    break;
                case 4005:
                    type = 4;
                    break;
                case 4006:
                    type = 5;
                    break;
                case 4007:
                    type = 6;
                    break;
                case 4008:
                    type = 7;
                    break;
                default:
                    switch (code) {
                        case 4019:
                            type = 18;
                            break;
                        case 4020:
                            type = 20;
                            break;
                        default:
                            switch (code) {
                                case 4027:
                                    type = 27;
                                    break;
                                case 4028:
                                    type = 28;
                                    break;
                            }
                    }
            }
        } else {
            type = 9;
        }
        if (!(this.mHwAdminCache == null || type == -1)) {
            result = this.mHwAdminCache.getCachedList(type);
        }
        return result == null ? new ArrayList() : result;
    }

    public Bundle getHwAdminCachedBundle(String policyName) {
        if (this.mHwAdminCache != null) {
            return this.mHwAdminCache.getCachedBundle(policyName);
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

    private AbsDevicePolicyManagerService.HwActiveAdmin getHwActiveAdmin(ComponentName who, int userHandle) {
        DevicePolicyManagerService.ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
        if (admin == null) {
            throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
        } else if (admin.getUid() == Binder.getCallingUid()) {
            AbsDevicePolicyManagerService.HwActiveAdmin hwadmin = admin.mHwActiveAdmin;
            if (hwadmin != null) {
                return hwadmin;
            }
            AbsDevicePolicyManagerService.HwActiveAdmin hwadmin2 = new AbsDevicePolicyManagerService.HwActiveAdmin();
            admin.mHwActiveAdmin = hwadmin2;
            return hwadmin2;
        } else {
            throw new SecurityException("Admin " + who + " is not owned by uid " + Binder.getCallingUid());
        }
    }

    private void setHwUserRestriction(String key, boolean disable, int userHandle) {
        UserHandle user = new UserHandle(userHandle);
        boolean alreadyRestricted = this.mUserManager.hasUserRestriction(key, user);
        if (HWFLOW) {
            Log.i(TAG, "setUserRestriction for (" + key + ", " + userHandle + "), is alreadyRestricted: " + alreadyRestricted);
        }
        long id = Binder.clearCallingIdentity();
        if (disable && !alreadyRestricted) {
            try {
                if ("no_config_tethering".equals(key)) {
                    if (((WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE)).isWifiApEnabled()) {
                        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).stopTethering(0);
                        ((NotificationManager) this.mContext.getSystemService("notification")).cancelAsUser(null, 17303548, UserHandle.ALL);
                    }
                } else if ("no_physical_media".equals(key)) {
                    boolean hasExternalSdcard = StorageUtils.hasExternalSdcard(this.mContext);
                    boolean dafaultIsSdcard = DefaultStorageLocation.isSdcard();
                    if (hasExternalSdcard && !dafaultIsSdcard) {
                        Log.w(TAG, "call doUnMount");
                        StorageUtils.doUnMount(this.mContext);
                    } else if (hasExternalSdcard && dafaultIsSdcard) {
                        if (StorageUtils.isSwitchPrimaryVolumeSupported()) {
                            throw new IllegalStateException("could not disable sdcard when it is primary card.");
                        }
                    }
                } else if ("no_usb_file_transfer".equals(key)) {
                    if (disable) {
                        Settings.Global.putStringForUser(this.mContext.getContentResolver(), "adb_enabled", "0", userHandle);
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
        int identifier = user.getIdentifier();
        return this.mUserManager.hasUserRestriction(key, user);
    }

    /* access modifiers changed from: protected */
    public void syncHwDeviceSettingsLocked(int userHandle) {
        if (userHandle != 0) {
            Log.w(TAG, "userHandle is not USER_OWNER, return ");
            return;
        }
        combineAllPolicies(userHandle, true);
        try {
            synchronized (getLockObject()) {
                for (String s : HWDEVICE_OWNER_USER_RESTRICTIONS) {
                    hwSyncDeviceCapabilitiesLocked(s, userHandle);
                }
            }
            hwSyncDeviceStatusBarLocked(userHandle);
        } catch (Exception e) {
            Log.e(TAG, "syncHwDeviceSettingsLocked exception is " + e.getMessage());
        }
        try {
            syncHwAdminCache(userHandle);
        } catch (Exception e2) {
            Log.e(TAG, "syncHwAdminCache exception is " + e2.getMessage());
        }
        sendPersistentAppToIAware(userHandle);
    }

    private void syncHwAdminCache(int userHandle) {
        if (this.mHwAdminCache == null) {
            this.mHwAdminCache = new HwAdminCache();
        }
        this.mHwAdminCache.syncHwAdminCache(0, isWifiDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(8, isBluetoothDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(1, isVoiceDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(2, isInstallSourceDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(3, getInstallPackageSourceWhiteList(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(4, getPersistentApp(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(5, getDisallowedRunningApp(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(6, getInstallPackageWhiteList(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(7, getDisallowedUninstallPackageList(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(18, getDisabledDeactivateMdmPackageList(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(9, getNetworkAccessWhitelist(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(10, isSafeModeDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(11, isAdbDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(12, isUSBOtgDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(13, isGPSDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(14, isHomeButtonDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(15, isTaskButtonDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(16, isBackButtonDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(17, isChangeLauncherDisabled(null, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.INSTALL_APKS_BLACK_LIST_POLICY, getPolicy(null, HwAdminCache.INSTALL_APKS_BLACK_LIST_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_SCREEN_CAPTURE_POLICY, getPolicy(null, HwAdminCache.DISABLE_SCREEN_CAPTURE_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_APPLICATIONS_LIST_POLICY, getPolicy(null, HwAdminCache.DISABLE_APPLICATIONS_LIST_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache("disable-clipboard", getPolicy(null, "disable-clipboard", userHandle));
        this.mHwAdminCache.syncHwAdminCache("disable-google-account-autosync", getPolicy(null, "disable-google-account-autosync", userHandle));
        this.mHwAdminCache.syncHwAdminCache("ignore-frequent-relaunch-app", getPolicy(null, "ignore-frequent-relaunch-app", userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_SDWRITING_POLICY, getPolicy(null, HwAdminCache.DISABLE_SDWRITING_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_NOTIFICATION_POLICY, getPolicy(null, HwAdminCache.DISABLE_NOTIFICATION_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_MICROPHONE, getPolicy(null, HwAdminCache.DISABLE_MICROPHONE, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_NAVIGATIONBAR_POLICY, getPolicy(null, HwAdminCache.DISABLE_NAVIGATIONBAR_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.SUPER_WHITE_LIST_APP, getPolicy(null, HwAdminCache.SUPER_WHITE_LIST_APP, userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_FORBIDDEN_NETWORK_LOCATION, getPolicy(null, SettingsMDMPlugin.POLICY_FORBIDDEN_NETWORK_LOCATION, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_HEADPHONE, getPolicy(null, HwAdminCache.DISABLE_HEADPHONE, userHandle));
        this.mHwAdminCache.syncHwAdminCache("disable-send-notification", getPolicy(null, "disable-send-notification", userHandle));
        this.mHwAdminCache.syncHwAdminCache("policy-single-app", getPolicy(null, "policy-single-app", userHandle));
        this.mHwAdminCache.syncHwAdminCache("disable-change-wallpaper", getPolicy(null, "disable-change-wallpaper", userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_FORBIDDEN_SCREEN_OFF, getPolicy(null, SettingsMDMPlugin.POLICY_FORBIDDEN_SCREEN_OFF, userHandle));
        this.mHwAdminCache.syncHwAdminCache("disable-power-shutdown", getPolicy(null, "disable-power-shutdown", userHandle));
        this.mHwAdminCache.syncHwAdminCache("disable-shutdownmenu", getPolicy(null, "disable-shutdownmenu", userHandle));
        this.mHwAdminCache.syncHwAdminCache("disable-volume", getPolicy(null, "disable-volume", userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_SERVICE, getPolicy(null, SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_SERVICE, userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_MODE, getPolicy(null, SettingsMDMPlugin.POLICY_FORBIDDEN_LOCATION_MODE, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_SYNC, getPolicy(null, HwAdminCache.DISABLE_SYNC, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_PASSIVE_PROVIDER_POLICY, getPolicy(null, HwAdminCache.DISABLE_PASSIVE_PROVIDER_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_WIFIP2P_POLICY, getPolicy(null, HwAdminCache.DISABLE_WIFIP2P_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache(HwAdminCache.DISABLE_INFRARED_POLICY, getPolicy(null, HwAdminCache.DISABLE_INFRARED_POLICY, userHandle));
        this.mHwAdminCache.syncHwAdminCache("disable-fingerprint-authentication", getPolicy(null, "disable-fingerprint-authentication", userHandle));
        this.mHwAdminCache.syncHwAdminCache("force-enable-BT", getPolicy(null, "force-enable-BT", userHandle));
        this.mHwAdminCache.syncHwAdminCache("force-enable-wifi", getPolicy(null, "force-enable-wifi", userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST, getPolicy(null, SettingsMDMPlugin.POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST, userHandle));
        this.mHwAdminCache.syncHwAdminCache("policy-file-share-disabled", getPolicy(null, "policy-file-share-disabled", userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_PHONE_FIND, getPolicy(null, SettingsMDMPlugin.POLICY_PHONE_FIND, userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_PARENT_CONTROL, getPolicy(null, SettingsMDMPlugin.POLICY_PARENT_CONTROL, userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_SIM_LOCK, getPolicy(null, SettingsMDMPlugin.POLICY_SIM_LOCK, userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_APPLICATION_LOCK, getPolicy(null, SettingsMDMPlugin.POLICY_APPLICATION_LOCK, userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.DISABLED_ANDROID_ANIMATION, getPolicy(null, SettingsMDMPlugin.DISABLED_ANDROID_ANIMATION, userHandle));
        this.mHwAdminCache.syncHwAdminCache(SettingsMDMPlugin.POLICY_FORCE_ENCRYPT_SDCARD, getPolicy(null, SettingsMDMPlugin.POLICY_FORCE_ENCRYPT_SDCARD, userHandle));
    }

    private void hwSyncDeviceCapabilitiesLocked(String restriction, int userHandle) {
        boolean disabled = false;
        boolean alreadyRestricted = haveHwUserRestriction(restriction, userHandle);
        DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
        int N = policy.mAdminList.size();
        int i = 0;
        while (true) {
            if (i >= N) {
                break;
            } else if (isUserRestrictionDisabled(restriction, ((DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i)).mHwActiveAdmin)) {
                disabled = true;
                break;
            } else {
                i++;
            }
        }
        if (disabled != alreadyRestricted) {
            if (HWFLOW) {
                Log.i(TAG, "Set " + restriction + " to " + disabled);
            }
            setHwUserRestriction(restriction, disabled, userHandle);
        }
    }

    private void hwSyncDeviceStatusBarLocked(int userHandle) {
        if (isStatusBarExpandPanelDisabled(null, userHandle)) {
            setStatusBarPanelDisabledInternal(userHandle);
        } else {
            setStatusBarPanelEnableInternal(true, userHandle);
        }
    }

    private boolean isUserRestrictionDisabled(String restriction, AbsDevicePolicyManagerService.HwActiveAdmin admin) {
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

    private void installPackage(String packagePath, String installerPackageName) {
        if (!TextUtils.isEmpty(packagePath)) {
            long ident = Binder.clearCallingIdentity();
            try {
                final File tempFile = new File(packagePath.trim()).getCanonicalFile();
                if (!tempFile.getName().endsWith(".apk")) {
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).installPackageAsUser(Uri.fromFile(tempFile).getPath(), new PackageInstallObserver() {
                    public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
                        if (1 != returnCode) {
                            Log.e(HwDevicePolicyManagerService.TAG, "The package " + tempFile.getName() + "installed failed, error code: " + returnCode);
                        }
                    }
                }.getBinder(), 2, installerPackageName, 0);
                Binder.restoreCallingIdentity(ident);
            } catch (IOException e) {
                Log.e(TAG, "Get canonical file failed for package path: " + packagePath + ", error: " + e.getMessage());
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
                if (pm.getApplicationInfo(packageName, 0) == null) {
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                pm.deletePackage(packageName, null, keepData);
                Binder.restoreCallingIdentity(ident);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Name not found for package: " + packageName);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
        }
    }

    private void filterOutSystemAppList(List<String> packageNames, int userHandle) {
        List<String> systemAppList = new ArrayList<>();
        try {
            for (String name : packageNames) {
                if (isSystemAppExcludePreInstalled(name)) {
                    systemAppList.add(name);
                }
            }
            if (!systemAppList.isEmpty()) {
                packageNames.removeAll(systemAppList);
            }
        } catch (Exception e) {
            Log.e(TAG, "filterOutSystemAppList exception is " + e);
        }
    }

    private void enforceCheckNotSystemApp(String packageName, int userHandle) {
        if (isSystemAppExcludePreInstalled(packageName)) {
            throw new IllegalArgumentException("could not operate system app");
        }
    }

    private boolean isSystemAppExcludePreInstalled(String packageName) {
        long id = Binder.clearCallingIdentity();
        try {
            IPackageManager pm = AppGlobals.getPackageManager();
            if (pm == null) {
                restoreCallingIdentity(id);
                return false;
            }
            int userId = UserHandle.getCallingUserId();
            UserManager um = UserManager.get(this.mContext);
            if (um == null) {
                Log.e(TAG, "failed to get um");
                restoreCallingIdentity(id);
                return false;
            }
            UserInfo primaryUser = um.getProfileParent(userId);
            if (primaryUser == null) {
                primaryUser = um.getUserInfo(userId);
            }
            boolean isSystemAppExcludePreInstalled = isSystemAppExcludePreInstalled(pm, packageName, primaryUser.id);
            restoreCallingIdentity(id);
            return isSystemAppExcludePreInstalled;
        } catch (RemoteException e) {
            HwLog.e(TAG, "failed to check system app, RemoteException is  ");
        } catch (Exception e2) {
            HwLog.e(TAG, "failed to check system app ");
        } catch (Throwable th) {
            restoreCallingIdentity(id);
            throw th;
        }
        restoreCallingIdentity(id);
        return false;
    }

    private boolean isSystemAppExcludePreInstalled(IPackageManager pm, String packageName, int userId) throws RemoteException {
        if (packageName == null || packageName.equals("")) {
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 8192, userId);
            if (appInfo == null) {
                return false;
            }
            int flags = appInfo.flags;
            boolean flag = true;
            if ((flags & 1) == 0) {
                Log.d(TAG, "packageName is not systemFlag");
                flag = false;
            } else if (!((flags & 1) == 0 || (flags & 33554432) == 0)) {
                Log.w(TAG, "SystemApp preInstalledFlag");
                flag = false;
            }
            int hwFlags = appInfo.hwFlags;
            if (!((flags & 1) == 0 || (hwFlags & 33554432) == 0)) {
                flag = false;
                Log.d(TAG, "packageName is not systemFlag");
            }
            if ((hwFlags & 67108864) != 0) {
                flag = false;
            }
            return flag;
        } catch (Exception e) {
            Log.e(TAG, "could not get appInfo, exception is " + e);
            return false;
        }
    }

    public int getSDCardEncryptionStatus() {
        char c = 65535;
        if (!isSupportCrypt) {
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
            case 4:
                return 2;
            case 5:
                return 5;
            case 6:
                return 6;
            default:
                return 0;
        }
    }

    public void setSDCardDecryptionDisabled(ComponentName who, boolean disabled, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdminForCallerLocked(who);
                if (admin.disableDecryptSDCard != disabled) {
                    admin.disableDecryptSDCard = disabled;
                    saveSettingsLocked(userHandle);
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(19, isSDCardDecryptionDisabled(null, userHandle));
        }
    }

    public boolean isSDCardDecryptionDisabled(ComponentName who, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    boolean z = getHwActiveAdminUncheckedLocked(who, userHandle).disableDecryptSDCard;
                    return z;
                } catch (Throwable admin) {
                    throw admin;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null && admin2.mHwActiveAdmin.disableDecryptSDCard) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private AbsDevicePolicyManagerService.HwActiveAdmin getHwActiveAdminUncheckedLocked(ComponentName who, int userHandle) {
        DevicePolicyManagerService.ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
        if (admin != null) {
            AbsDevicePolicyManagerService.HwActiveAdmin hwadmin = admin.mHwActiveAdmin;
            if (hwadmin != null) {
                return hwadmin;
            }
            AbsDevicePolicyManagerService.HwActiveAdmin hwadmin2 = new AbsDevicePolicyManagerService.HwActiveAdmin();
            admin.mHwActiveAdmin = hwadmin2;
            return hwadmin2;
        }
        throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
    }

    private AbsDevicePolicyManagerService.HwActiveAdmin getHwActiveAdminForCallerLocked(ComponentName who) {
        DevicePolicyManagerService.ActiveAdmin admin = getActiveAdminForCallerLocked(who, 7);
        if (admin != null) {
            AbsDevicePolicyManagerService.HwActiveAdmin hwadmin = admin.mHwActiveAdmin;
            if (hwadmin != null) {
                return hwadmin;
            }
            AbsDevicePolicyManagerService.HwActiveAdmin hwadmin2 = new AbsDevicePolicyManagerService.HwActiveAdmin();
            admin.mHwActiveAdmin = hwadmin2;
            return hwadmin2;
        }
        throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
    }

    /* access modifiers changed from: protected */
    public void init() {
        if (!this.hasInit) {
            Iterator it = this.globalStructs.iterator();
            while (it.hasNext()) {
                PolicyStruct struct = (PolicyStruct) it.next();
                if (struct != null) {
                    struct.getOwner().init(struct);
                }
            }
            this.hasInit = true;
        }
    }

    private void setDpcInAELaunchableAndBackgroundRunnable(boolean executeImmediately) {
        Log.i(TAG, "setDpcInAELaunchableAndBackgroundRunnable from calling uuid" + Binder.getCallingUid());
        ComponentName deviceOwnerComponent = null;
        try {
            deviceOwnerComponent = getDeviceOwnerComponent(true);
        } catch (SecurityException e) {
            Log.e(TAG, "setDpcInAE->getDeviceOwnerComponent failed with SecurityException");
        }
        if (deviceOwnerComponent != null) {
            setPackageLaunchableAndBackgroundRunable(deviceOwnerComponent.getPackageName(), executeImmediately);
        } else {
            Log.i(TAG, "No device owner found");
        }
        for (UserInfo ui : this.mUserManager.getUsers(true)) {
            Log.i(TAG, "setDpcInAELaunchableAndBackgroundRunnable iterate user with id:" + ui.id);
            ComponentName profileName = getProfileOwner(ui.id);
            if (profileName != null) {
                setPackageLaunchableAndBackgroundRunable(profileName.getPackageName(), executeImmediately);
            } else {
                Log.i(TAG, "No profile owner found");
            }
        }
    }

    private void setPackageLaunchableAndBackgroundRunable(String packageName, boolean executeImmediately) {
        final List<HwAppStartupSettingEx> aePackageConfigs = new ArrayList<>();
        aePackageConfigs.add(new HwAppStartupSettingEx(packageName, mPolicy, mModify, mShow));
        Runnable runnable = new Runnable() {
            public void run() {
                boolean iAwareAvailable = false;
                int i = 0;
                while (true) {
                    if (((double) i) >= HwDevicePolicyManagerService.MAX_RETRY_TIMES) {
                        break;
                    }
                    IMultiTaskManager itf = HwIAwareManager.getMultiTaskManager();
                    if (itf != null) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            boolean isResult = itf.updateAppStartupSettings(aePackageConfigs, false);
                            iAwareAvailable = true;
                            HwLog.i(HwDevicePolicyManagerService.TAG, "setPackageLaunchableAndBackgroundRunable result:" + isResult);
                        } catch (RemoteException e) {
                            HwLog.e(HwDevicePolicyManagerService.TAG, "updateStartupSettings ex");
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                        Binder.restoreCallingIdentity(identity);
                        break;
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e2) {
                        HwLog.e(HwDevicePolicyManagerService.TAG, "reTry app whitelist failed");
                    }
                    i++;
                }
                if (!iAwareAvailable) {
                    HwLog.e(HwDevicePolicyManagerService.TAG, "IMultiTskMngerService unavailable after times retry ");
                }
            }
        };
        ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        if (!executeImmediately) {
            worker.schedule(runnable, 3000, TimeUnit.MILLISECONDS);
        } else {
            worker.schedule(runnable, 0, TimeUnit.MILLISECONDS);
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
        addPlugin(new DeviceRestrictionPlugin(context));
        addPlugin(new SettingsMDMPlugin(context));
        addPlugin(new DeviceWifiPlugin(context));
        addPlugin(new DeviceBluetoothPlugin(context));
        addPlugin(new DeviceLocationPlugin(context));
        addPlugin(new DeviceP2PPlugin(context));
        addPlugin(new DeviceInfraredPlugin(context));
        addPlugin(new DeviceControlPlugin(context));
        addPlugin(new DevicePackageManagerPlugin(context));
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
                globalPolicyItems.put(item.getPolicyName(), item);
            }
        }
    }

    public void bdReport(int eventID, String eventMsg) {
        if (this.mContext != null) {
            Flog.bdReport(this.mContext, eventID, eventMsg);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0253, code lost:
        if (r6 != 1) goto L_0x025f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0255, code lost:
        r1.mHwAdminCache.syncHwAdminCache(r3, getPolicy(null, r3, r5));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x025f, code lost:
        if (r22 == false) goto L_0x0265;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0261, code lost:
        if (r6 != 1) goto L_0x0265;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0263, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0265, code lost:
        r21.onSetPolicyCompleted(r2, r3, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x026a, code lost:
        return r6;
     */
    public int setPolicy(ComponentName who, String policyName, Bundle policyData, int userHandle) {
        boolean z;
        long beginTime;
        boolean onSetPolicyResult;
        long endTime;
        StringBuilder sb;
        DevicePolicyPlugin plugin;
        boolean golbalPolicyChanged;
        boolean onSetPolicyResult2;
        int i;
        boolean onSetPolicyResult3;
        ComponentName componentName = who;
        String str = policyName;
        Bundle bundle = policyData;
        int i2 = userHandle;
        StringBuilder sb2 = new StringBuilder();
        sb2.append("setPolicy, policyName = ");
        sb2.append(str);
        sb2.append(", caller :");
        sb2.append(componentName == null ? "null" : who.flattenToString());
        HwLog.d(TAG, sb2.toString());
        if (componentName != null) {
            DevicePolicyPlugin plugin2 = findPluginByPolicyName(str);
            if (plugin2 == null) {
                HwLog.e(TAG, "no plugin found, pluginName = " + str + ", caller :" + who.flattenToString());
                return -1;
            } else if (!plugin2.checkCallingPermission(componentName, str)) {
                HwLog.e(TAG, "permission denied: " + who.flattenToString());
                return -1;
            } else {
                boolean golbalPolicyChanged2 = false;
                PolicyStruct struct = findPolicyStructByPolicyName(str);
                synchronized (getLockObject()) {
                    try {
                        AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdminUncheckedLocked(componentName, i2);
                        PolicyStruct.PolicyItem item = null;
                        PolicyStruct.PolicyItem newItem = null;
                        if (struct != null) {
                            try {
                                PolicyStruct.PolicyItem item2 = (PolicyStruct.PolicyItem) admin.adminPolicyItems.get(str);
                                PolicyStruct.PolicyItem oldItem = struct.getItemByPolicyName(str);
                                if (oldItem != null) {
                                    newItem = new PolicyStruct.PolicyItem(str, oldItem.getItemType());
                                    if (item2 == null) {
                                        try {
                                            newItem.copyFrom(oldItem);
                                            newItem.addAttrValues(newItem, bundle);
                                        } catch (Throwable th) {
                                            th = th;
                                            DevicePolicyPlugin devicePolicyPlugin = plugin2;
                                        }
                                    } else {
                                        newItem.deepCopyFrom(item2);
                                    }
                                    newItem.addAttrValues(newItem, bundle);
                                    PolicyStruct.PolicyItem combinedItem = combinePoliciesWithPolicyChanged(componentName, newItem, str, i2);
                                    PolicyStruct.PolicyItem globalItem = (PolicyStruct.PolicyItem) globalPolicyItems.get(str);
                                    if (globalItem == null) {
                                        try {
                                            StringBuilder sb3 = new StringBuilder();
                                            PolicyStruct.PolicyItem policyItem = item2;
                                            sb3.append("no policy item found, pluginName = ");
                                            sb3.append(str);
                                            sb3.append(", caller :");
                                            sb3.append(who.flattenToString());
                                            HwLog.e(TAG, sb3.toString());
                                            return -1;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            DevicePolicyPlugin devicePolicyPlugin2 = plugin2;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                }
                                            }
                                            throw th;
                                        }
                                    } else {
                                        z = true;
                                        PolicyStruct.PolicyItem item3 = item2;
                                        if (globalItem.equals(combinedItem) != 0) {
                                            newItem.setGlobalPolicyChanged(2);
                                            golbalPolicyChanged2 = false;
                                        } else {
                                            newItem.setGlobalPolicyChanged(1);
                                            golbalPolicyChanged2 = true;
                                        }
                                        item = item3;
                                    }
                                } else {
                                    PolicyStruct.PolicyItem policyItem2 = item2;
                                    HwLog.e(TAG, "no policy item found, pluginName = " + str + ", caller :" + who.flattenToString());
                                    return -1;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                DevicePolicyPlugin devicePolicyPlugin3 = plugin2;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } else {
                            z = true;
                        }
                        try {
                            HwLog.i(TAG, "when setPolicy, is global PolicyChanged ? = " + golbalPolicyChanged2);
                            beginTime = System.currentTimeMillis();
                            StringBuilder sb4 = new StringBuilder();
                            PolicyStruct.PolicyItem policyItem3 = item;
                            sb4.append("onSetPolicy, begin time: ");
                            sb4.append(beginTime);
                            HwLog.i(TAG, sb4.toString());
                            onSetPolicyResult = plugin2.onSetPolicy(componentName, str, bundle, golbalPolicyChanged2);
                            endTime = System.currentTimeMillis();
                            sb = new StringBuilder();
                            sb.append("onSetPolicy, costs time: ");
                            plugin = plugin2;
                            golbalPolicyChanged = golbalPolicyChanged2;
                        } catch (Throwable th5) {
                            th = th5;
                            DevicePolicyPlugin devicePolicyPlugin4 = plugin2;
                            boolean z2 = golbalPolicyChanged2;
                            boolean z3 = z;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                        try {
                            sb.append(endTime - beginTime);
                            HwLog.i(TAG, sb.toString());
                            if (onSetPolicyResult) {
                                try {
                                    admin.adminPolicyItems.put(str, newItem);
                                    if (newItem.getItemType() == PolicyStruct.PolicyType.CONFIGURATION) {
                                        globalPolicyItems.put(str, newItem);
                                        Iterator it = this.globalStructs.iterator();
                                        while (true) {
                                            if (!it.hasNext()) {
                                                break;
                                            }
                                            boolean found = false;
                                            Iterator it2 = ((PolicyStruct) it.next()).getPolicyMap().keySet().iterator();
                                            while (true) {
                                                if (!it2.hasNext()) {
                                                    onSetPolicyResult3 = onSetPolicyResult;
                                                    break;
                                                }
                                                onSetPolicyResult3 = onSetPolicyResult;
                                                if (newItem.getPolicyName().equals((String) it2.next())) {
                                                    struct.addPolicyItem(newItem);
                                                    found = true;
                                                    break;
                                                }
                                                onSetPolicyResult = onSetPolicyResult3;
                                            }
                                            if (found) {
                                                break;
                                            }
                                            onSetPolicyResult = onSetPolicyResult3;
                                        }
                                    }
                                    saveSettingsLocked(i2);
                                    onSetPolicyResult2 = false;
                                    combineAllPolicies(i2, false);
                                    i = 1;
                                } catch (Throwable th6) {
                                    th = th6;
                                    boolean z4 = z;
                                    DevicePolicyPlugin devicePolicyPlugin5 = plugin;
                                    boolean z5 = golbalPolicyChanged;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } else {
                                boolean z6 = onSetPolicyResult;
                                onSetPolicyResult2 = false;
                                HwLog.e(TAG, "onSetPolicy failed, pluginName = " + str + ", caller :" + who.flattenToString());
                                i = -1;
                            }
                            int result = i;
                        } catch (Throwable th7) {
                            th = th7;
                            DevicePolicyPlugin devicePolicyPlugin6 = plugin;
                            boolean z7 = z;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                        try {
                        } catch (Throwable th8) {
                            th = th8;
                            DevicePolicyPlugin devicePolicyPlugin7 = plugin;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        DevicePolicyPlugin devicePolicyPlugin8 = plugin2;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    public Bundle getPolicy(ComponentName who, String policyName, int userHandle) {
        String str;
        enforceFullCrossUsersPermission(userHandle);
        DevicePolicyPlugin plugin = findPluginByPolicyName(policyName);
        if (plugin == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("no plugin found, policyName = ");
            sb.append(policyName);
            sb.append(", caller :");
            sb.append(who == null ? "null" : who.flattenToString());
            HwLog.e(TAG, sb.toString());
            return null;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("get :");
        sb2.append(policyName);
        if (who == null) {
            str = "";
        } else {
            str = " ,cal :" + who.flattenToString();
        }
        sb2.append(str);
        HwLog.d(TAG, sb2.toString());
        Bundle resultBundle = null;
        synchronized (getLockObject()) {
            if (who == null) {
                try {
                    if (USER_ISOLATION_POLICY_LIST.contains(policyName)) {
                        resultBundle = combinePoliciesAsUser(policyName, userHandle).combineAllAttributes();
                    } else if (globalPolicyItems.get(policyName) != null) {
                        resultBundle = ((PolicyStruct.PolicyItem) globalPolicyItems.get(policyName)).combineAllAttributes();
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                PolicyStruct.PolicyItem item = (PolicyStruct.PolicyItem) getHwActiveAdminUncheckedLocked(who, userHandle).adminPolicyItems.get(policyName);
                if (("update-sys-app-install-list".equals(policyName) || "update-sys-app-undetachable-install-list".equals(policyName)) && globalPolicyItems.get(policyName) != null) {
                    resultBundle = ((PolicyStruct.PolicyItem) globalPolicyItems.get(policyName)).combineAllAttributes();
                } else if (item != null) {
                    resultBundle = item.combineAllAttributes();
                }
            }
            notifyOnGetPolicy(plugin, who, policyName, resultBundle);
        }
        return resultBundle;
    }

    private void notifyOnGetPolicy(DevicePolicyPlugin plugin, ComponentName who, String policyName, Bundle policyData) {
        plugin.onGetPolicy(who, policyName, policyData);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:68:0x018d, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x018e, code lost:
        if (r6 != true) goto L_0x019a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0190, code lost:
        r1.mHwAdminCache.syncHwAdminCache(r3, getPolicy(null, r3, r5));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x019a, code lost:
        if (r22 == false) goto L_0x019f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x019c, code lost:
        if (r6 != true) goto L_0x019f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x019f, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01a0, code lost:
        r21.onRemovePolicyCompleted(r2, r3, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x01a5, code lost:
        return r6;
     */
    public int removePolicy(ComponentName who, String policyName, Bundle policyData, int userHandle) {
        boolean golbalPolicyChanged;
        long beginTime;
        StringBuilder sb;
        boolean onRemoveResult;
        long endTime;
        StringBuilder sb2;
        DevicePolicyPlugin plugin;
        boolean golbalPolicyChanged2;
        boolean onRemoveResult2;
        ComponentName componentName = who;
        String str = policyName;
        Bundle bundle = policyData;
        int i = userHandle;
        StringBuilder sb3 = new StringBuilder();
        sb3.append("removePolicy, policyName = ");
        sb3.append(str);
        sb3.append(", caller :");
        sb3.append(componentName == null ? "null" : who.flattenToString());
        HwLog.d(TAG, sb3.toString());
        if (componentName != null) {
            DevicePolicyPlugin plugin2 = findPluginByPolicyName(str);
            if (plugin2 == null) {
                HwLog.e(TAG, "no plugin found, pluginName = " + str + ", caller :" + who.flattenToString());
                return -1;
            } else if (!plugin2.checkCallingPermission(componentName, str)) {
                HwLog.e(TAG, "permission denied: " + who.flattenToString());
                return -1;
            } else {
                PolicyStruct struct = findPolicyStructByPolicyName(str);
                synchronized (getLockObject()) {
                    try {
                        AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdminUncheckedLocked(componentName, i);
                        PolicyStruct.PolicyItem item = null;
                        PolicyStruct.PolicyItem newItem = null;
                        if (struct != null) {
                            try {
                                item = (PolicyStruct.PolicyItem) admin.adminPolicyItems.get(str);
                                if (item != null) {
                                    newItem = new PolicyStruct.PolicyItem(str, struct.getItemByPolicyName(str).getItemType());
                                    newItem.deepCopyFrom(item);
                                    newItem.removeAttrValues(newItem, bundle);
                                    PolicyStruct.PolicyItem combinedItem = combinePoliciesWithPolicyChanged(componentName, newItem, str, i);
                                    PolicyStruct.PolicyItem globalItem = (PolicyStruct.PolicyItem) globalPolicyItems.get(str);
                                    if (globalItem == null) {
                                        newItem.setGlobalPolicyChanged(0);
                                        golbalPolicyChanged = false;
                                    } else if (globalItem.equals(combinedItem)) {
                                        newItem.setGlobalPolicyChanged(2);
                                        golbalPolicyChanged = false;
                                    } else {
                                        newItem.setGlobalPolicyChanged(1);
                                        golbalPolicyChanged = true;
                                    }
                                } else {
                                    golbalPolicyChanged = false;
                                }
                            } catch (Throwable th) {
                                admin = th;
                                PolicyStruct policyStruct = struct;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th2) {
                                        admin = th2;
                                    }
                                }
                                throw admin;
                            }
                        } else {
                            golbalPolicyChanged = false;
                        }
                        try {
                            HwLog.i(TAG, "when removePolicy,  is global PolicyChanged ? = " + golbalPolicyChanged);
                            beginTime = System.currentTimeMillis();
                            try {
                                sb = new StringBuilder();
                                PolicyStruct policyStruct2 = struct;
                            } catch (Throwable th3) {
                                admin = th3;
                                boolean z = golbalPolicyChanged;
                                PolicyStruct policyStruct3 = struct;
                                while (true) {
                                    break;
                                }
                                throw admin;
                            }
                            try {
                                sb.append("onRemovePolicy, begin time: ");
                                sb.append(beginTime);
                                HwLog.i(TAG, sb.toString());
                                onRemoveResult = plugin2.onRemovePolicy(componentName, str, bundle, golbalPolicyChanged);
                                endTime = System.currentTimeMillis();
                                sb2 = new StringBuilder();
                                plugin = plugin2;
                                try {
                                    sb2.append("onRemovePolicy, costs time: ");
                                    golbalPolicyChanged2 = golbalPolicyChanged;
                                } catch (Throwable th4) {
                                    admin = th4;
                                    boolean z2 = golbalPolicyChanged;
                                    DevicePolicyPlugin devicePolicyPlugin = plugin;
                                    while (true) {
                                        break;
                                    }
                                    throw admin;
                                }
                            } catch (Throwable th5) {
                                admin = th5;
                                boolean z3 = golbalPolicyChanged;
                                while (true) {
                                    break;
                                }
                                throw admin;
                            }
                        } catch (Throwable th6) {
                            admin = th6;
                            boolean z4 = golbalPolicyChanged;
                            PolicyStruct policyStruct4 = struct;
                            while (true) {
                                break;
                            }
                            throw admin;
                        }
                        try {
                            sb2.append(endTime - beginTime);
                            HwLog.i(TAG, sb2.toString());
                            if (onRemoveResult) {
                                if (item != null) {
                                    try {
                                        if (item.getItemType() == PolicyStruct.PolicyType.LIST && bundle != null) {
                                            admin.adminPolicyItems.put(str, newItem);
                                            saveSettingsLocked(i);
                                            combineAllPolicies(i, true);
                                            onRemoveResult2 = true;
                                        }
                                    } catch (Throwable th7) {
                                        admin = th7;
                                        DevicePolicyPlugin devicePolicyPlugin2 = plugin;
                                        boolean z5 = golbalPolicyChanged2;
                                        while (true) {
                                            break;
                                        }
                                        throw admin;
                                    }
                                }
                                admin.adminPolicyItems.remove(str);
                                saveSettingsLocked(i);
                                combineAllPolicies(i, true);
                                onRemoveResult2 = true;
                            } else {
                                HwLog.e(TAG, "onSetPolicy failed, pluginName = " + str + ", caller :" + who.flattenToString());
                                onRemoveResult2 = true;
                            }
                            try {
                            } catch (Throwable th8) {
                                admin = th8;
                                DevicePolicyPlugin devicePolicyPlugin3 = plugin;
                                while (true) {
                                    break;
                                }
                                throw admin;
                            }
                        } catch (Throwable th9) {
                            admin = th9;
                            DevicePolicyPlugin devicePolicyPlugin4 = plugin;
                            while (true) {
                                break;
                            }
                            throw admin;
                        }
                    } catch (Throwable th10) {
                        admin = th10;
                        PolicyStruct policyStruct5 = struct;
                        while (true) {
                            break;
                        }
                        throw admin;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    /* access modifiers changed from: protected */
    public void notifyPlugins(ComponentName who, int userHandle) {
        HwDevicePolicyManagerService hwDevicePolicyManagerService = this;
        ComponentName componentName = who;
        int i = userHandle;
        StringBuilder sb = new StringBuilder();
        sb.append("notifyPlugins: ");
        sb.append(componentName == null ? "null" : who.flattenToString());
        sb.append(" userId: ");
        sb.append(i);
        HwLog.d(TAG, sb.toString());
        DevicePolicyManagerService.ActiveAdmin activeAdminToRemove = getActiveAdminUncheckedLocked(who, userHandle);
        if (activeAdminToRemove != null && activeAdminToRemove.mHwActiveAdmin != null && activeAdminToRemove.mHwActiveAdmin.adminPolicyItems != null && !activeAdminToRemove.mHwActiveAdmin.adminPolicyItems.isEmpty()) {
            Iterator it = hwDevicePolicyManagerService.globalStructs.iterator();
            while (it.hasNext()) {
                PolicyStruct struct = (PolicyStruct) it.next();
                if (struct != null) {
                    ArrayList<PolicyStruct.PolicyItem> removedPluginItems = new ArrayList<>();
                    for (PolicyStruct.PolicyItem removedItem : activeAdminToRemove.mHwActiveAdmin.adminPolicyItems.values()) {
                        if (removedItem != null) {
                            PolicyStruct.PolicyItem combinedItem = hwDevicePolicyManagerService.combinePoliciesWithoutRemovedPolicyItem(componentName, removedItem.getPolicyName(), i);
                            if (((PolicyStruct.PolicyItem) globalPolicyItems.get(removedItem.getPolicyName())) == null) {
                                removedItem.setGlobalPolicyChanged(0);
                            } else if (removedItem.equals(combinedItem)) {
                                removedItem.setGlobalPolicyChanged(2);
                            } else {
                                removedItem.setGlobalPolicyChanged(1);
                                globalPolicyItems.put(removedItem.getPolicyName(), combinedItem);
                            }
                            if (struct.containsPolicyName(removedItem.getPolicyName())) {
                                removedPluginItems.add(removedItem);
                            }
                        }
                    }
                    if (!removedPluginItems.isEmpty()) {
                        DevicePolicyPlugin plugin = struct.getOwner();
                        if (plugin == null) {
                            HwLog.w(TAG, " policy struct has no owner");
                            return;
                        }
                        hwDevicePolicyManagerService.effectedItems.add(new AbsDevicePolicyManagerService.EffectedItem(componentName, plugin, removedPluginItems));
                        long beginTime = System.currentTimeMillis();
                        HwLog.i(TAG, "onActiveAdminRemoved, begin time: " + beginTime);
                        HwLog.i(TAG, "onActiveAdminRemoving, userHandle: " + i);
                        plugin.onActiveAdminRemoved(componentName, removedPluginItems, i);
                        long endTime = System.currentTimeMillis();
                        HwLog.i(TAG, "onActiveAdminRemoved, costs time: " + (endTime - beginTime));
                    }
                    hwDevicePolicyManagerService = this;
                    componentName = who;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeActiveAdminCompleted(ComponentName who) {
        synchronized (getLockObject()) {
            if (!this.effectedItems.isEmpty()) {
                Iterator<AbsDevicePolicyManagerService.EffectedItem> it = this.effectedItems.iterator();
                while (it.hasNext()) {
                    AbsDevicePolicyManagerService.EffectedItem effectedItem = it.next();
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

    private DevicePolicyPlugin findPluginByPolicyName(String policyName) {
        PolicyStruct struct = findPolicyStructByPolicyName(policyName);
        if (struct != null) {
            return struct.getOwner();
        }
        return null;
    }

    private PolicyStruct findPolicyStructByPolicyName(String policyName) {
        Iterator it = this.globalStructs.iterator();
        while (it.hasNext()) {
            PolicyStruct struct = (PolicyStruct) it.next();
            if (struct != null && struct.containsPolicyName(policyName)) {
                return struct;
            }
        }
        return null;
    }

    private void combineAllPolicies(int userHandle, boolean shouldChange) {
        synchronized (getLockObject()) {
            DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
            Iterator it = this.globalStructs.iterator();
            while (it.hasNext()) {
                PolicyStruct struct = (PolicyStruct) it.next();
                for (String policyName : struct.getPolicyMap().keySet()) {
                    PolicyStruct.PolicyItem globalItem = new PolicyStruct.PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType());
                    globalItem.copyFrom(struct.getItemByPolicyName(policyName));
                    int N = policy.mAdminList.size();
                    if (globalItem.getItemType() != PolicyStruct.PolicyType.CONFIGURATION) {
                        for (int i = 0; i < N; i++) {
                            DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                            if (admin.mHwActiveAdmin != null) {
                                PolicyStruct.PolicyItem adminItem = (PolicyStruct.PolicyItem) admin.mHwActiveAdmin.adminPolicyItems.get(policyName);
                                if (adminItem != null && adminItem.hasAnyNonNullAttribute()) {
                                    traverseCombinePolicyItem(globalItem, adminItem);
                                }
                            }
                        }
                    } else if (shouldChange) {
                        int i2 = N - 1;
                        while (true) {
                            if (i2 < 0) {
                                break;
                            }
                            DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i2);
                            if (admin2.mHwActiveAdmin != null) {
                                PolicyStruct.PolicyItem findItem = (PolicyStruct.PolicyItem) admin2.mHwActiveAdmin.adminPolicyItems.get(policyName);
                                if (findItem != null) {
                                    globalItem = findItem;
                                    HwLog.w(TAG, "global policy will change: " + policyName);
                                    break;
                                }
                            }
                            i2--;
                        }
                    } else {
                        HwLog.w(TAG, "global policy will not change: " + policyName);
                    }
                    globalPolicyItems.put(policyName, globalItem);
                    struct.addPolicyItem(globalItem);
                }
            }
        }
    }

    private PolicyStruct.PolicyItem combinePoliciesWithPolicyChanged(ComponentName who, PolicyStruct.PolicyItem newItem, String policyName, int userHandle) {
        PolicyStruct.PolicyItem globalAdminItem;
        synchronized (getLockObject()) {
            DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
            DevicePolicyManagerService.ActiveAdmin activeAdmin = getActiveAdminUncheckedLocked(who, userHandle);
            ArrayList<DevicePolicyManagerService.ActiveAdmin> adminList = new ArrayList<>();
            Iterator it = policy.mAdminList.iterator();
            while (it.hasNext()) {
                adminList.add((DevicePolicyManagerService.ActiveAdmin) it.next());
            }
            if (activeAdmin != null && adminList.size() > 0) {
                adminList.remove(activeAdmin);
            }
            PolicyStruct struct = findPolicyStructByPolicyName(policyName);
            globalAdminItem = new PolicyStruct.PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType());
            globalAdminItem.copyFrom(struct.getItemByPolicyName(policyName));
            int N = adminList.size();
            for (int i = 0; i < N; i++) {
                DevicePolicyManagerService.ActiveAdmin admin1 = adminList.get(i);
                if (admin1.mHwActiveAdmin != null) {
                    PolicyStruct.PolicyItem adminItem = (PolicyStruct.PolicyItem) admin1.mHwActiveAdmin.adminPolicyItems.get(policyName);
                    if (adminItem != null && adminItem.hasAnyNonNullAttribute()) {
                        traverseCombinePolicyItem(globalAdminItem, adminItem);
                    }
                }
            }
            traverseCombinePolicyItem(globalAdminItem, newItem);
        }
        return globalAdminItem;
    }

    private PolicyStruct.PolicyItem combinePoliciesAsUser(String policyName, int userHandle) {
        PolicyStruct.PolicyItem resultPolicyItem;
        synchronized (getLockObject()) {
            DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
            ArrayList<DevicePolicyManagerService.ActiveAdmin> adminList = new ArrayList<>();
            Iterator it = policy.mAdminList.iterator();
            while (it.hasNext()) {
                adminList.add((DevicePolicyManagerService.ActiveAdmin) it.next());
            }
            PolicyStruct struct = findPolicyStructByPolicyName(policyName);
            resultPolicyItem = new PolicyStruct.PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType());
            resultPolicyItem.copyFrom(struct.getItemByPolicyName(policyName));
            int size = adminList.size();
            for (int i = 0; i < size; i++) {
                DevicePolicyManagerService.ActiveAdmin admin = adminList.get(i);
                if (admin.mHwActiveAdmin != null) {
                    PolicyStruct.PolicyItem policyItemAsAdmin = (PolicyStruct.PolicyItem) admin.mHwActiveAdmin.adminPolicyItems.get(policyName);
                    if (policyItemAsAdmin != null && policyItemAsAdmin.hasAnyNonNullAttribute()) {
                        traverseCombinePolicyItem(resultPolicyItem, policyItemAsAdmin);
                    }
                }
            }
        }
        return resultPolicyItem;
    }

    private PolicyStruct.PolicyItem combinePoliciesWithoutRemovedPolicyItem(ComponentName who, String policyName, int userHandle) {
        DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
        ArrayList<DevicePolicyManagerService.ActiveAdmin> adminList = new ArrayList<>();
        synchronized (getLockObject()) {
            DevicePolicyManagerService.ActiveAdmin activeAdmin = getActiveAdminUncheckedLocked(who, userHandle);
            Iterator it = policy.mAdminList.iterator();
            while (it.hasNext()) {
                adminList.add((DevicePolicyManagerService.ActiveAdmin) it.next());
            }
            if (activeAdmin != null && adminList.size() > 0) {
                adminList.remove(activeAdmin);
            }
        }
        PolicyStruct struct = findPolicyStructByPolicyName(policyName);
        PolicyStruct.PolicyItem oldItem = struct != null ? struct.getItemByPolicyName(policyName) : null;
        PolicyStruct.PolicyItem globalAdminItem = null;
        if (oldItem != null) {
            globalAdminItem = new PolicyStruct.PolicyItem(policyName, oldItem.getItemType());
            globalAdminItem.copyFrom(oldItem);
            int N = adminList.size();
            for (int i = 0; i < N; i++) {
                DevicePolicyManagerService.ActiveAdmin admin1 = adminList.get(i);
                if (admin1.mHwActiveAdmin != null) {
                    PolicyStruct.PolicyItem adminItem = (PolicyStruct.PolicyItem) admin1.mHwActiveAdmin.adminPolicyItems.get(policyName);
                    if (adminItem != null && adminItem.hasAnyNonNullAttribute() && adminItem.getPolicyName().equals(policyName)) {
                        traverseCombinePolicyItem(globalAdminItem, adminItem);
                    }
                }
            }
        }
        return globalAdminItem;
    }

    private void traverseCombinePolicyItem(PolicyStruct.PolicyItem oldRoot, PolicyStruct.PolicyItem newRoot) {
        if (oldRoot != null && newRoot != null) {
            oldRoot.setAttributes(combineAttributes(oldRoot.getAttributes(), newRoot.getAttributes(), oldRoot));
            int n = oldRoot.getChildItem().size();
            for (int i = 0; i < n; i++) {
                traverseCombinePolicyItem((PolicyStruct.PolicyItem) oldRoot.getChildItem().get(i), (PolicyStruct.PolicyItem) newRoot.getChildItem().get(i));
            }
        }
    }

    private Bundle combineAttributes(Bundle oldAttr, Bundle newAttr, PolicyStruct.PolicyItem item) {
        switch (AnonymousClass6.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[item.getItemType().ordinal()]) {
            case 1:
                for (String key : newAttr.keySet()) {
                    if (newAttr.get(key) != null) {
                        oldAttr.putBoolean(key, oldAttr.getBoolean(key) || newAttr.getBoolean(key));
                    }
                }
                break;
            case 2:
                for (String key2 : newAttr.keySet()) {
                    if (newAttr.get(key2) != null) {
                        ArrayList<String> oldPolicyList = oldAttr.getStringArrayList(key2);
                        ArrayList<String> newPolicyList = newAttr.getStringArrayList(key2);
                        if (oldPolicyList == null) {
                            oldPolicyList = new ArrayList<>();
                        }
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(oldPolicyList, newPolicyList);
                        oldAttr.putStringArrayList(key2, oldPolicyList);
                    }
                }
                break;
            case 3:
                for (String key3 : newAttr.keySet()) {
                    if (newAttr.get(key3) != null) {
                        oldAttr.putString(key3, newAttr.getString(key3));
                    }
                }
                break;
        }
        return oldAttr;
    }

    public ArrayList<String> queryBrowsingHistory(ComponentName who, int userHandle) {
        ArrayList<String> historyList = new ArrayList<>();
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have network MDM permission!");
            enforceHwCrossUserPermission(userHandle);
            getHwActiveAdmin(who, userHandle);
            DevicePolicyPlugin plugin = findPluginByPolicyName("network-black-list");
            if (plugin == null || !(plugin instanceof DeviceNetworkPlugin)) {
                HwLog.e(TAG, "no DeviceNetworkPlugin found, pluginName = network-black-list");
                return historyList;
            }
            long callingId = Binder.clearCallingIdentity();
            ArrayList<String> historyList2 = ((DeviceNetworkPlugin) plugin).queryBrowsingHistory();
            Binder.restoreCallingIdentity(callingId);
            return historyList2;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        libcore.io.IoUtils.closeQuietly(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c6, code lost:
        com.android.server.devicepolicy.HwLog.d(TAG, "Can't find HwPolicy");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00cd, code lost:
        return false;
     */
    public boolean hasHwPolicy(int userHandle) {
        String base;
        HwLog.d(TAG, "hasHwPolicy, userHandle :" + userHandle);
        synchronized (getLockObject()) {
            if (userHandle == 0) {
                base = HwPackageManagerServiceEx.PREINSTALLED_APK_LIST_DIR + "device_policies.xml";
            } else {
                base = new File(Environment.getUserSystemDirectory(userHandle), "device_policies.xml").getAbsolutePath();
            }
            JournaledFile journal = new JournaledFile(new File(base), new File(base + ".tmp"));
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(journal.chooseForRead());
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                while (true) {
                    int next = parser.next();
                    int type = next;
                    if (next != 1) {
                        if (type == 2) {
                            if ("hw_policy".equals(parser.getName())) {
                                while (true) {
                                    int next2 = parser.next();
                                    type = next2;
                                    if (next2 == 1 || type == 3) {
                                        break;
                                    } else if (type == 2) {
                                        HwLog.d(TAG, "find HwPolicy");
                                        IoUtils.closeQuietly(stream);
                                        return true;
                                    }
                                }
                            }
                            if (type == 1) {
                                HwLog.d(TAG, "Can't find HwPolicy");
                                IoUtils.closeQuietly(stream);
                                return false;
                            }
                        }
                    }
                    break;
                }
            } catch (IOException | XmlPullParserException e) {
                try {
                    HwLog.e(TAG, "XmlPullParserException | IOException");
                } catch (Throwable th) {
                    IoUtils.closeQuietly(stream);
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSecureBlockEncrypted() {
        if (!StorageManager.isBlockEncrypted()) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            return IStorageManager.Stub.asInterface(ServiceManager.getService("mount")).isSecure();
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting encryption type");
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:71:0x014b, code lost:
        return 1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0029  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x010d  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x010a A[SYNTHETIC] */
    public int configVpnProfile(ComponentName who, Bundle para, int userHandle) {
        boolean paraIsNull;
        boolean paraIsNotNull;
        KeyStore mKeyStore;
        ComponentName componentName = who;
        Bundle bundle = para;
        int i = userHandle;
        enforceHwCrossUserPermission(i);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (getLockObject()) {
            KeyStore mKeyStore2 = KeyStore.getInstance();
            if (componentName != null) {
                if (bundle != null) {
                    paraIsNull = false;
                    if (!paraIsNull) {
                        Log.e(TAG, "Bundle para is null or componentName is null!");
                        return -1;
                    } else if (!isValidVpnConfig(bundle)) {
                        Log.e(TAG, "This Config isn't valid vpnConfig");
                        return -1;
                    } else {
                        if (!mKeyStore2.put("VPN_" + profile.key, getProfile(bundle).encode(), -1, 0)) {
                            Log.e(TAG, "Set vpn failed, check the config.");
                            return -1;
                        }
                        String key = bundle.getString("key");
                        DevicePolicyManagerService.DevicePolicyData policy = getUserData(i);
                        int N = policy.mAdminList.size();
                        int i2 = 0;
                        while (i2 < N) {
                            DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i2);
                            if (admin.mHwActiveAdmin == null || admin.mHwActiveAdmin.vpnProviderlist == null) {
                                mKeyStore = mKeyStore2;
                            } else {
                                Bundle speProvider = null;
                                Iterator it = admin.mHwActiveAdmin.vpnProviderlist.iterator();
                                while (true) {
                                    if (!it.hasNext()) {
                                        mKeyStore = mKeyStore2;
                                        break;
                                    }
                                    mKeyStore = mKeyStore2;
                                    Iterator it2 = it;
                                    Bundle provider = (Bundle) it.next();
                                    if (key.equals(provider.getString("key"))) {
                                        speProvider = provider;
                                        break;
                                    }
                                    mKeyStore2 = mKeyStore;
                                    it = it2;
                                }
                                if (speProvider != null) {
                                    admin.mHwActiveAdmin.vpnProviderlist.remove(speProvider);
                                    saveSettingsLocked(i);
                                }
                            }
                            i2++;
                            mKeyStore2 = mKeyStore;
                        }
                        AbsDevicePolicyManagerService.HwActiveAdmin ap = getHwActiveAdmin(componentName, i);
                        if (ap.vpnProviderlist != null) {
                            boolean isAlready = false;
                            Bundle delProvider = null;
                            Iterator it3 = ap.vpnProviderlist.iterator();
                            while (true) {
                                if (!it3.hasNext()) {
                                    break;
                                }
                                Bundle provider2 = (Bundle) it3.next();
                                if (provider2 != null) {
                                    if (!isEmpty(provider2.getString("key"))) {
                                        paraIsNotNull = false;
                                        if (paraIsNotNull) {
                                            boolean z = paraIsNotNull;
                                            if (key.equals(provider2.getString("key"))) {
                                                isAlready = true;
                                                delProvider = provider2;
                                                break;
                                            }
                                        }
                                        ComponentName componentName2 = who;
                                    }
                                }
                                paraIsNotNull = true;
                                if (paraIsNotNull) {
                                }
                                ComponentName componentName22 = who;
                            }
                            if (isAlready && delProvider != null) {
                                ap.vpnProviderlist.remove(delProvider);
                            }
                            ap.vpnProviderlist.add(bundle);
                            saveSettingsLocked(i);
                        } else {
                            ap.vpnProviderlist = new ArrayList();
                            ap.vpnProviderlist.add(bundle);
                            saveSettingsLocked(i);
                        }
                    }
                }
            }
            paraIsNull = true;
            if (!paraIsNull) {
            }
        }
    }

    public int removeVpnProfile(ComponentName who, Bundle para, int userHandle) {
        int i = userHandle;
        String key = para.getString("key");
        if (who == null || isEmpty(key)) {
            Log.e(TAG, "ComponentName or key is empty.");
            return -1;
        }
        enforceHwCrossUserPermission(i);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (getLockObject()) {
            KeyStore mKeyStore = KeyStore.getInstance();
            boolean hasDeleted = false;
            DevicePolicyManagerService.DevicePolicyData policy = getUserData(i);
            int N = policy.mAdminList.size();
            for (int i2 = 0; i2 < N; i2++) {
                DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i2);
                if (!(admin.mHwActiveAdmin == null || admin.mHwActiveAdmin.vpnProviderlist == null)) {
                    Bundle specProvider = null;
                    Iterator it = admin.mHwActiveAdmin.vpnProviderlist.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Bundle provider = (Bundle) it.next();
                        if (key.equals(provider.getString("key"))) {
                            specProvider = provider;
                            break;
                        }
                    }
                    if (specProvider != null) {
                        if (!mKeyStore.delete("VPN_" + key)) {
                            Log.e(TAG, "Delete vpn failed, check the key.");
                            return -1;
                        }
                        admin.mHwActiveAdmin.vpnProviderlist.remove(specProvider);
                        saveSettingsLocked(i);
                        hasDeleted = true;
                    } else {
                        continue;
                    }
                }
            }
            if (hasDeleted) {
                return 1;
            }
            return -1;
        }
    }

    public Bundle getVpnProfile(ComponentName who, Bundle keyWords, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        String key = keyWords.getString("key");
        if (isEmpty(key)) {
            Log.e(TAG, "key is null or empty.");
            return null;
        }
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    AbsDevicePolicyManagerService.HwActiveAdmin hwAdmin = getHwActiveAdmin(who, userHandle);
                    if (hwAdmin.vpnProviderlist == null) {
                        return null;
                    }
                    for (Bundle provider : hwAdmin.vpnProviderlist) {
                        if (key.equals(provider.getString("key"))) {
                            return provider;
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    DevicePolicyManagerService.ActiveAdmin admin = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                    if (!(admin.mHwActiveAdmin == null || admin.mHwActiveAdmin.vpnProviderlist == null)) {
                        for (Bundle provider2 : admin.mHwActiveAdmin.vpnProviderlist) {
                            if (key.equals(provider2.getString("key"))) {
                                return provider2;
                            }
                        }
                        continue;
                    }
                }
                return null;
            }
        }
    }

    public Bundle getVpnList(ComponentName who, Bundle keyWords, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (getLockObject()) {
            ArrayList<String> vpnKeyList = new ArrayList<>();
            Bundle vpnListBundle = new Bundle();
            if (who != null) {
                AbsDevicePolicyManagerService.HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.vpnProviderlist == null) {
                    return null;
                }
                for (Bundle provider : admin.vpnProviderlist) {
                    if (!isEmpty(provider.getString("key"))) {
                        vpnKeyList.add(provider.getString("key"));
                    }
                }
                vpnListBundle.putStringArrayList("keylist", vpnKeyList);
                return vpnListBundle;
            }
            DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                DevicePolicyManagerService.ActiveAdmin admin2 = (DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i);
                if (!(admin2.mHwActiveAdmin == null || admin2.mHwActiveAdmin.vpnProviderlist == null)) {
                    for (Bundle provider2 : admin2.mHwActiveAdmin.vpnProviderlist) {
                        if (!isEmpty(provider2.getString("key")) && !vpnKeyList.contains(provider2.getString("key"))) {
                            vpnKeyList.add(provider2.getString("key"));
                        }
                    }
                }
            }
            vpnListBundle.putStringArrayList("keylist", vpnKeyList);
            return vpnListBundle;
        }
    }

    private VpnProfile getProfile(Bundle vpnBundle) {
        VpnProfile profile = new VpnProfile(vpnBundle.getString("key"));
        profile.name = vpnBundle.getString("name");
        profile.type = Integer.parseInt(vpnBundle.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE));
        profile.server = vpnBundle.getString("server");
        profile.username = vpnBundle.getString("username");
        profile.password = vpnBundle.getString("password");
        switch (profile.type) {
            case 0:
                profile.mppe = Boolean.parseBoolean(vpnBundle.getString("mppe"));
                break;
            case 1:
                profile.l2tpSecret = vpnBundle.getString("l2tpSecret");
                break;
            case 2:
                profile.l2tpSecret = vpnBundle.getString("l2tpSecret");
                profile.ipsecIdentifier = vpnBundle.getString("ipsecIdentifier");
                profile.ipsecSecret = vpnBundle.getString("ipsecSecret");
                break;
            case 3:
                profile.l2tpSecret = vpnBundle.getString("l2tpSecret");
                profile.ipsecUserCert = vpnBundle.getString("ipsecUserCert");
                profile.ipsecCaCert = vpnBundle.getString("ipsecCaCert");
                profile.ipsecServerCert = vpnBundle.getString("ipsecServerCert");
                break;
            case 4:
                profile.ipsecIdentifier = vpnBundle.getString("ipsecIdentifier");
                profile.ipsecSecret = vpnBundle.getString("ipsecSecret");
                break;
            case 5:
                profile.ipsecUserCert = vpnBundle.getString("ipsecUserCert");
                profile.ipsecCaCert = vpnBundle.getString("ipsecCaCert");
                profile.ipsecServerCert = vpnBundle.getString("ipsecServerCert");
                break;
            case 6:
                profile.ipsecCaCert = vpnBundle.getString("ipsecCaCert");
                profile.ipsecServerCert = vpnBundle.getString("ipsecServerCert");
                break;
        }
        return profile;
    }

    private boolean isValidVpnConfig(Bundle para) {
        if (para == null || isEmpty(para.getString("key")) || isEmpty(para.getString("name")) || isEmpty(para.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE)) || isEmpty(para.getString("server")) || Integer.parseInt(para.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE)) < 0 || Integer.parseInt(para.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE)) > 6) {
            return false;
        }
        switch (Integer.parseInt(para.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE))) {
            case 2:
            case 4:
                return !isEmpty(para.getString("ipsecSecret"));
            case 3:
            case 5:
                return !isEmpty(para.getString("ipsecUserCert"));
            default:
                return true;
        }
    }

    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    public boolean getScreenCaptureDisabled(ComponentName who, int userHandle) {
        if (who != null) {
            return HwDevicePolicyManagerService.super.getScreenCaptureDisabled(who, userHandle);
        }
        return HwDevicePolicyManagerService.super.getScreenCaptureDisabled(who, userHandle) || HwDeviceManager.mdmDisallowOp(20, null);
    }

    public boolean formatSDCard(ComponentName who, String diskId, int userHandle) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_SDCARD", "does not have sd card MDM permission!");
            synchronized (getLockObject()) {
                if (getActiveAdminUncheckedLocked(who, userHandle) != null) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        ((StorageManager) this.mContext.getSystemService("storage")).partitionPublic(diskId);
                        Binder.restoreCallingIdentity(token);
                    } catch (Exception e) {
                        try {
                            HwLog.e(TAG, "format sd card data error!");
                            return false;
                        } finally {
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                } else {
                    throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
                }
            }
            return true;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    public void setAccountDisabled(ComponentName who, String accountType, boolean disabled, int userHandle) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app management MDM permission!");
            synchronized (getLockObject()) {
                DevicePolicyManagerService.ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin != null) {
                    if (disabled) {
                        admin.accountTypesWithManagementDisabled.add(accountType);
                    } else {
                        admin.accountTypesWithManagementDisabled.remove(accountType);
                    }
                    saveSettingsLocked(userHandle);
                } else {
                    throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
                }
            }
            return;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    public boolean isAccountDisabled(ComponentName who, String accountType, int userHandle) {
        synchronized (getLockObject()) {
            if (who != null) {
                try {
                    DevicePolicyManagerService.ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                    if (admin != null) {
                        boolean contains = admin.accountTypesWithManagementDisabled.contains(accountType);
                        return contains;
                    }
                } catch (Throwable admin2) {
                    throw admin2;
                }
            }
            DevicePolicyManagerService.DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                if (((DevicePolicyManagerService.ActiveAdmin) policy.mAdminList.get(i)).accountTypesWithManagementDisabled.contains(accountType)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0028, code lost:
        if (r3 != 0) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r9 = com.android.server.devicepolicy.CertInstallHelper.installPkcs12Cert(r22, r4, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0034, code lost:
        r10 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0036, code lost:
        if (r3 != 1) goto L_0x009d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003c, code lost:
        r9 = com.android.server.devicepolicy.CertInstallHelper.installX509Cert(r4, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003e, code lost:
        if (r24 == false) goto L_0x009c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0040, code lost:
        r12 = r1.mInjector.binderGetCallingUid();
        r13 = r1.mInjector.binderClearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r7 = android.security.KeyChain.bindAsUser(r1.mContext, android.os.UserHandle.getUserHandleForUid(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r7.getService().setGrant(r12, r5, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0061, code lost:
        r1.mInjector.binderRestoreCallingIdentity(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0067, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006b, code lost:
        r16 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        com.android.server.devicepolicy.HwLog.e(TAG, "set grant certificate");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0079, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007c, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x007d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        com.android.server.devicepolicy.HwLog.w(TAG, "Interrupted while set granting certificate");
        java.lang.Thread.currentThread().interrupt();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x008e, code lost:
        r1.mInjector.binderRestoreCallingIdentity(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0095, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0096, code lost:
        r1.mInjector.binderRestoreCallingIdentity(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x009b, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x009c, code lost:
        return r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00a5, code lost:
        throw new java.lang.IllegalArgumentException("the type of the installed cert is not illegal");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00a6, code lost:
        com.android.server.devicepolicy.HwLog.e(TAG, "throw error when install cert");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00b0, code lost:
        return false;
     */
    public boolean installCertificateWithType(ComponentName who, int type, byte[] certBuffer, String alias, String password, int certInstallType, boolean requestAccess, int userHandle) {
        ComponentName componentName = who;
        int i = type;
        byte[] bArr = certBuffer;
        String str = alias;
        int i2 = certInstallType;
        if (componentName != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have install cert MDM permission!");
            synchronized (getLockObject()) {
                try {
                    if (getActiveAdminUncheckedLocked(componentName, userHandle) == null) {
                        String str2 = password;
                        throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + componentName);
                    }
                } catch (Throwable th) {
                    admin = th;
                    throw admin;
                }
            }
        } else {
            String str3 = password;
            int i3 = userHandle;
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    /* access modifiers changed from: protected */
    public long getUsrSetExtendTime() {
        String value = getPolicy(null, PASSWORD_CHANGE_EXTEND_TIME, UserHandle.myUserId()).getString("value");
        if (value == null || "".equals(value)) {
            return HwDevicePolicyManagerService.super.getUsrSetExtendTime();
        }
        return Long.parseLong(value);
    }

    /* JADX INFO: finally extract failed */
    public void setSilentActiveAdmin(ComponentName who, int userHandle) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have PERMISSION_MDM_DEVICE_MANAGER permission!");
        synchronized (getLockObject()) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            } else if (!isAdminActive(who, userHandle)) {
                HwLog.d(TAG, "setSilentActiveAdmin, mHasHwMdmFeature active supported.");
                long identityToken = Binder.clearCallingIdentity();
                try {
                    setActiveAdmin(who, true, userHandle);
                    Binder.restoreCallingIdentity(identityToken);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identityToken);
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void monitorFactoryReset(String component, String reason) {
        if (this.mMonitor == null || TextUtils.isEmpty(reason)) {
            HwLog.e(TAG, "monitorFactoryReset: Invalid parameter,mMonitor=" + this.mMonitor + ", reason=" + reason);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("component", component);
        bundle.putString("reason", reason);
        this.mMonitor.monitor(907400018, bundle);
    }

    /* access modifiers changed from: protected */
    public void clearWipeDataFactoryLowlevel(String reason, boolean wipeEuicc) {
        HwLog.d(TAG, "wipeData, reason=" + reason + ", wipeEuicc=" + wipeEuicc);
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(285212672);
        intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
        intent.putExtra("wipeEuicc", wipeEuicc);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    public int setSystemLanguage(ComponentName who, Bundle bundle, int userHandle) {
        if (bundle == null || TextUtils.isEmpty(bundle.getString("locale"))) {
            throw new IllegalArgumentException("locale is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "Does not have mdm_device_manager permission.");
        synchronized (getLockObject()) {
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
                    Binder.restoreCallingIdentity(callingId);
                } catch (Exception e) {
                    try {
                        Slog.w(TAG, "failed to set system language");
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            } else {
                throw new IllegalArgumentException("ComponentName is null");
            }
        }
        return 1;
    }

    public void setDeviceOwnerApp(ComponentName admin, String ownerName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        enforceHwCrossUserPermission(userId);
        synchronized (getLockObject()) {
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIsMDMDeviceOwnerAPI = true;
                setDeviceOwner(admin, ownerName, userId);
            } finally {
                this.mIsMDMDeviceOwnerAPI = false;
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public boolean setDeviceOwner(ComponentName admin, String ownerName, int userId) {
        boolean result = HwDevicePolicyManagerService.super.setDeviceOwner(admin, ownerName, userId);
        HwDevicePolicyManagerServiceUtil.collectMdmDoSuccessDftData(admin.getPackageName());
        setDpcInAELaunchableAndBackgroundRunnable(true);
        return result;
    }

    public boolean setProfileOwner(ComponentName who, String ownerName, int userHandle) {
        boolean result = HwDevicePolicyManagerService.super.setProfileOwner(who, ownerName, userHandle);
        HwDevicePolicyManagerServiceUtil.collectMdmWpSuccessDftData(who.getPackageName());
        setDpcInAELaunchableAndBackgroundRunnable(true);
        return result;
    }

    public void clearDeviceOwnerApp(int userId) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        enforceHwCrossUserPermission(userId);
        synchronized (getLockObject()) {
            try {
                ComponentName component = this.mOwners.getDeviceOwnerComponent();
                if (!this.mOwners.hasDeviceOwner() || component == null) {
                    throw new IllegalArgumentException("The device owner is not set up.");
                }
                this.mIsMDMDeviceOwnerAPI = true;
                HwDevicePolicyManagerService.super.clearDeviceOwner(component.getPackageName());
                this.mIsMDMDeviceOwnerAPI = false;
            } catch (Throwable th) {
                this.mIsMDMDeviceOwnerAPI = false;
                throw th;
            }
        }
    }

    public void turnOnMobiledata(ComponentName who, boolean on, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have MDM_NETWORK_MANAGER permission!");
        if (who != null) {
            synchronized (getLockObject()) {
                getHwActiveAdmin(who, userHandle);
            }
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (on) {
                try {
                    phone.enableDataConnectivity();
                } catch (Exception e) {
                    HwLog.e(TAG, "Can not calling the remote function to set data enabled!");
                }
            } else {
                phone.disableDataConnectivity();
            }
        } else {
            throw new IllegalArgumentException("ComponentName is null");
        }
    }

    public boolean setCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber, int userHandle) {
        boolean extendLockScreenPassword;
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_KEYGUARD", "does not have keyguard MDM permission!");
            synchronized (getLockObject()) {
                if (getActiveAdminUncheckedLocked(who, userHandle) != null) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        extendLockScreenPassword = new LockPatternUtilsEx(this.mContext).setExtendLockScreenPassword(password, phoneNumber, userHandle);
                    } catch (Exception e) {
                        return false;
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                } else {
                    throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
                }
            }
            return extendLockScreenPassword;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    public boolean clearCarrierLockScreenPassword(ComponentName who, String password, int userHandle) {
        boolean clearExtendLockScreenPassword;
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_KEYGUARD", "does not have keyguard MDM permission!");
            synchronized (getLockObject()) {
                if (getActiveAdminUncheckedLocked(who, userHandle) != null) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        clearExtendLockScreenPassword = new LockPatternUtilsEx(this.mContext).clearExtendLockScreenPassword(password, userHandle);
                        Binder.restoreCallingIdentity(token);
                    } catch (Exception e) {
                        try {
                            HwLog.e(TAG, "clear extended keyguard password error!");
                            return false;
                        } finally {
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                } else {
                    throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
                }
            }
            return clearExtendLockScreenPassword;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    public void resetNetorkSetting(ComponentName who, int userHandle) {
        if (who != null) {
            synchronized (getLockObject()) {
                getHwActiveAdmin(who, userHandle);
            }
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have com.huawei.permission.sec.MDM_NETWORK_MANAGER !");
            Intent intent = new Intent("com.android.settings.mdm.receiver.action.MDMPolicyResetNetworkSetting");
            intent.setComponent(new ComponentName("com.android.settings", SettingsMDMPlugin.SETTINGS_MDM_RECEIVER));
            this.mContext.sendBroadcast(intent);
            return;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }

    public Bundle getTopAppPackageName(ComponentName who, int userHandle) {
        if (who != null) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
            synchronized (getLockObject()) {
                getHwActiveAdmin(who, userHandle);
            }
            long callingId = Binder.clearCallingIdentity();
            Bundle bundle = new Bundle();
            ActivityInfo lastResumeActivity = HwActivityManager.getLastResumedActivity();
            if (lastResumeActivity != null) {
                bundle.putString("value", lastResumeActivity.packageName);
            }
            Binder.restoreCallingIdentity(callingId);
            return bundle;
        }
        throw new IllegalArgumentException("ComponentName is null");
    }
}
