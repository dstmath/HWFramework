package com.android.server.devicepolicy;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.os.IPowerManager.Stub;
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
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
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
import com.android.server.UiThread;
import com.android.server.devicepolicy.AbsDevicePolicyManagerService.EffectedItem;
import com.android.server.devicepolicy.AbsDevicePolicyManagerService.HwActiveAdmin;
import com.android.server.devicepolicy.DevicePolicyManagerService.ActiveAdmin;
import com.android.server.devicepolicy.DevicePolicyManagerService.DevicePolicyData;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import com.android.server.devicepolicy.plugins.DeviceApplicationPlugin;
import com.android.server.devicepolicy.plugins.DeviceBluetoothPlugin;
import com.android.server.devicepolicy.plugins.DeviceCameraPlugin;
import com.android.server.devicepolicy.plugins.DeviceControlPlugin;
import com.android.server.devicepolicy.plugins.DeviceFirewallManagerImpl;
import com.android.server.devicepolicy.plugins.DeviceInfraredPlugin;
import com.android.server.devicepolicy.plugins.DeviceLocationPlugin;
import com.android.server.devicepolicy.plugins.DeviceNetworkPlugin;
import com.android.server.devicepolicy.plugins.DeviceP2PPlugin;
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
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.huawei.android.widget.LockPatternUtilsEx;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwDevicePolicyManagerService extends DevicePolicyManagerService implements IHwDevicePolicyManager {
    private static final /* synthetic */ int[] -com-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues = null;
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
    private static boolean isSimplePwdOpen = SystemProperties.getBoolean("ro.config.not_allow_simple_pwd", false);
    private static final boolean isSupportCrypt = SystemProperties.getBoolean("ro.config.support_sdcard_crypt", true);
    private static final boolean mHasHwMdmFeature = true;
    public static final int transaction_setActiveVisitorPasswordState = 1003;
    private boolean hasInit = false;
    private final Context mContext;
    private AlertDialog mErrorDialog;
    private HwAdminCache mHwAdminCache;
    private HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private TransactionProcessor mProcessor = null;
    private final UserManager mUserManager;
    final SparseArray<DeviceVisitorPolicyData> mVisitorUserData = new SparseArray();

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

    private static /* synthetic */ int[] -getcom-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues() {
        if (-com-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues != null) {
            return -com-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues;
        }
        int[] iArr = new int[PolicyType.values().length];
        try {
            iArr[PolicyType.CONFIGURATION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PolicyType.LIST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PolicyType.STATE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues = iArr;
        return iArr;
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
            for (DevicePolicyPlugin plugin : this.globalPlugins) {
                if (plugin != null) {
                    PolicyStruct struct = plugin.getPolicyStruct();
                    for (PolicyItem item : struct.getPolicyItems()) {
                        if (item == null) {
                            HwLog.w(TAG, "policyItem is null in plugin: " + plugin.getPluginName());
                        }
                    }
                    addPolicyStruct(struct);
                }
            }
        }
    }

    DeviceVisitorPolicyData getVisitorUserData(int userHandle) {
        DeviceVisitorPolicyData policy;
        synchronized (this) {
            policy = (DeviceVisitorPolicyData) this.mVisitorUserData.get(userHandle);
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
        return new JournaledFile(new File(base), new File(base + ".tmp"));
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00e2 A:{SYNTHETIC, Splitter: B:32:0x00e2} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00eb A:{SYNTHETIC, Splitter: B:37:0x00eb} */
    /* JADX WARNING: Missing block: B:26:0x00d6, code:
            if (r4.mActivePasswordNonLetter == 0) goto L_0x00ac;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveVisitorSettingsLock(int userHandle) {
        Throwable th;
        DeviceVisitorPolicyData policy = getVisitorUserData(userHandle);
        JournaledFile journal = makeJournaledFile2(userHandle);
        FileOutputStream stream = null;
        try {
            FileOutputStream stream2 = new FileOutputStream(journal.chooseForWrite(), false);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream2, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "policies");
                if (policy.mActivePasswordQuality == 0 && policy.mActivePasswordLength == 0) {
                    if (policy.mActivePasswordUpperCase == 0) {
                        if (policy.mActivePasswordLowerCase == 0) {
                            if (policy.mActivePasswordLetters == 0) {
                                if (policy.mActivePasswordNumeric == 0) {
                                    if (policy.mActivePasswordSymbols == 0) {
                                    }
                                }
                            }
                        }
                    }
                }
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
                out.endTag(null, "policies");
                out.endDocument();
                journal.commit();
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e) {
                    }
                }
                stream = stream2;
            } catch (IOException e2) {
                stream = stream2;
                try {
                    journal.rollback();
                    if (stream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            journal.rollback();
            if (stream == null) {
                try {
                    stream.close();
                } catch (IOException e5) {
                }
            }
        }
    }

    private void loadVisitorSettingsLocked(DeviceVisitorPolicyData policy, int userHandle) {
        NullPointerException e;
        NumberFormatException e2;
        XmlPullParserException e3;
        IOException e4;
        IndexOutOfBoundsException e5;
        FileInputStream stream = null;
        File file = makeJournaledFile2(userHandle).chooseForRead();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                do {
                    type = parser.next();
                    if (type == 1) {
                        break;
                    }
                } while (type != 2);
                String tag = parser.getName();
                if ("policies".equals(tag)) {
                    type = parser.next();
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                            stream = stream2;
                        } else if (!(type == 3 || type == 4)) {
                            tag = parser.getName();
                            if ("active-password2".equals(tag)) {
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
                                Slog.w(TAG, "Unknown tag: " + tag);
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                    stream = stream2;
                    if (stream != null) {
                        try {
                            stream.close();
                            return;
                        } catch (IOException e6) {
                            return;
                        }
                    }
                    return;
                }
                throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
            } catch (NullPointerException e7) {
                e = e7;
                stream = stream2;
            } catch (NumberFormatException e8) {
                e2 = e8;
                stream = stream2;
            } catch (XmlPullParserException e9) {
                e3 = e9;
                stream = stream2;
            } catch (FileNotFoundException e10) {
                stream = stream2;
            } catch (IOException e11) {
                e4 = e11;
                stream = stream2;
            } catch (IndexOutOfBoundsException e12) {
                e5 = e12;
                stream = stream2;
            }
        } catch (NullPointerException e13) {
            e = e13;
            Slog.w(TAG, "failed parsing " + file + " " + e);
        } catch (NumberFormatException e14) {
            e2 = e14;
            Slog.w(TAG, "failed parsing " + file + " " + e2);
        } catch (XmlPullParserException e15) {
            e3 = e15;
            Slog.w(TAG, "failed parsing " + file + " " + e3);
        } catch (FileNotFoundException e16) {
        } catch (IOException e17) {
            e4 = e17;
            Slog.w(TAG, "failed parsing " + file + " " + e4);
        } catch (IndexOutOfBoundsException e18) {
            e5 = e18;
            Slog.w(TAG, "failed parsing " + file + " " + e5);
        }
    }

    private boolean isPrivacyModeEnabled() {
        if (Secure.getInt(this.mContext.getContentResolver(), PRIVACY_MODE_ON, 0) == 1) {
            return isFeatrueSupported();
        }
        return false;
    }

    private static boolean isFeatrueSupported() {
        return SystemProperties.getBoolean("ro.config.hw_privacymode", false);
    }

    public void systemReady(int phase) {
        super.systemReady(phase);
        if (isFeatrueSupported()) {
            if (this.mHasFeature) {
                Slog.w(TAG, "systemReady");
                synchronized (this) {
                    loadVisitorSettingsLocked(getVisitorUserData(0), 0);
                }
            } else {
                return;
            }
        }
        if (phase == 1000) {
            listenForUserSwitches();
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0034, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:35:0x0074, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isActivePasswordSufficient(int userHandle, boolean parent) {
        boolean z = true;
        if (!isPrivacyModeEnabled()) {
            return super.isActivePasswordSufficient(userHandle, parent);
        }
        if (!super.isActivePasswordSufficient(userHandle, parent)) {
            return false;
        }
        Slog.w(TAG, "super is ActivePassword Sufficient");
        if (!this.mHasFeature) {
            return true;
        }
        synchronized (this) {
            DeviceVisitorPolicyData policy = getVisitorUserData(userHandle);
            if (policy.mActivePasswordQuality < getPasswordQuality(null, userHandle, parent) || policy.mActivePasswordLength < getPasswordMinimumLength(null, userHandle, parent)) {
            } else if (policy.mActivePasswordQuality != 393216) {
                return true;
            } else if (policy.mActivePasswordUpperCase < getPasswordMinimumUpperCase(null, userHandle, parent) || policy.mActivePasswordLowerCase < getPasswordMinimumLowerCase(null, userHandle, parent) || policy.mActivePasswordLetters < getPasswordMinimumLetters(null, userHandle, parent) || policy.mActivePasswordNumeric < getPasswordMinimumNumeric(null, userHandle, parent) || policy.mActivePasswordSymbols < getPasswordMinimumSymbols(null, userHandle, parent)) {
                z = false;
            } else if (policy.mActivePasswordNonLetter < getPasswordMinimumNonLetter(null, userHandle, parent)) {
                z = false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:31:0x005a, code:
            if (r2.mActivePasswordNonLetter == r14) goto L_0x003e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setActiveVisitorPasswordState(int quality, int length, int letters, int uppercase, int lowercase, int numbers, int symbols, int nonletter, int userHandle) {
        if (this.mHasFeature) {
            enforceFullCrossUsersPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("android.permission.BIND_DEVICE_ADMIN", null);
            DeviceVisitorPolicyData p = getVisitorUserData(userHandle);
            validateQualityConstant(quality);
            synchronized (this) {
                if (p.mActivePasswordQuality == quality && p.mActivePasswordLength == length) {
                    if (p.mFailedPasswordAttempts == 0) {
                        if (p.mActivePasswordLetters == letters) {
                            if (p.mActivePasswordUpperCase == uppercase) {
                                if (p.mActivePasswordLowerCase == lowercase) {
                                    if (p.mActivePasswordNumeric == numbers) {
                                        if (p.mActivePasswordSymbols == symbols) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    void setAllowSimplePassword(ComponentName who, boolean mode, int userHandle) {
        if (this.mHasFeature && (isSimplePwdOpen ^ 1) == 0) {
            HwLog.d(TAG, "setAllowSimplePassword mode =" + mode);
            synchronized (this) {
                if (who == null) {
                    throw new NullPointerException("ComponentName is null");
                }
                ActiveAdmin ap = getActiveAdminForCallerLocked(who, 0);
                if (ap.allowSimplePassword != mode) {
                    ap.allowSimplePassword = mode;
                    saveSettingsLocked(userHandle);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0019, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getAllowSimplePassword(ComponentName who, int userHandle) {
        boolean z = true;
        if (!this.mHasFeature || (isSimplePwdOpen ^ 1) != 0) {
            return true;
        }
        synchronized (this) {
            boolean mode = true;
            ActiveAdmin admin;
            if (who != null) {
                admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin != null) {
                    z = admin.allowSimplePassword;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    admin = (ActiveAdmin) policy.mAdminList.get(i);
                    if (mode && mode != admin.allowSimplePassword) {
                        mode = admin.allowSimplePassword;
                    }
                }
                HwLog.d(TAG, "getAllowSimplePassword mode =" + mode);
                return mode;
            }
        }
    }

    void saveCurrentPwdStatus(boolean isCurrentPwdSimple, int userHandle) {
        if (this.mHasFeature && (isSimplePwdOpen ^ 1) == 0) {
            synchronized (this) {
                getUserData(userHandle).mIsCurrentPwdSimple = isCurrentPwdSimple;
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (this.mProcessor.processTransaction(code, data, reply) || this.mProcessor.processTransactionWithPolicyName(code, data, reply)) {
            return true;
        }
        ComponentName _arg0;
        switch (code) {
            case 1003:
                Slog.w(TAG, "transaction_setActiveVisitorPasswordState");
                data.enforceInterface("com.android.internal.widget.ILockSettings");
                setActiveVisitorPasswordState(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case 7001:
                if (isSimplePwdOpen) {
                    data.enforceInterface(DPMDESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    setAllowSimplePassword(_arg0, data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                }
                break;
            case 7002:
                if (isSimplePwdOpen) {
                    data.enforceInterface(DPMDESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    boolean _result = getAllowSimplePassword(_arg0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                }
                break;
            case 7003:
                if (isSimplePwdOpen) {
                    data.enforceInterface(DPMDESCRIPTOR);
                    saveCurrentPwdStatus(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                }
                break;
        }
        return super.onTransact(code, data, reply, flags);
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

    /* JADX WARNING: Missing block: B:19:0x004b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x0057, code:
            if (r9.mHwAdminCache == null) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:25:0x0059, code:
            r9.mHwAdminCache.syncHwAdminCache(0, isWifiDisabled(null, r12));
     */
    /* JADX WARNING: Missing block: B:26:0x0062, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setWifiDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have wifi MDM permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableWifi != disabled) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    WifiManager mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
                    if (!mWifiManager.isWifiEnabled() || !disabled || (mWifiManager.setWifiEnabled(false) ^ 1) == 0) {
                        Binder.restoreCallingIdentity(callingId);
                        admin.disableWifi = disabled;
                        saveSettingsLocked(userHandle);
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
    }

    public boolean isWifiDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableWifi;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableWifi) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isBluetoothDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableBluetooth;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableBluetooth) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:19:0x004d, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x0059, code:
            if (r9.mHwAdminCache == null) goto L_0x0066;
     */
    /* JADX WARNING: Missing block: B:25:0x005b, code:
            r9.mHwAdminCache.syncHwAdminCache(8, isBluetoothDisabled(null, r12));
     */
    /* JADX WARNING: Missing block: B:26:0x0066, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setBluetoothDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_BLUETOOTH", "does not have bluethooth MDM permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableBluetooth != disabled) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    BluetoothAdapter mBTAdapter = ((BluetoothManager) this.mContext.getSystemService("bluetooth")).getAdapter();
                    if (!mBTAdapter.isEnabled() || !disabled || (mBTAdapter.disable() ^ 1) == 0) {
                        Binder.restoreCallingIdentity(callingId);
                        admin.disableBluetooth = disabled;
                        saveSettingsLocked(userHandle);
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
    }

    public void setWifiApDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have Wifi AP MDM permission!");
        synchronized (this) {
            enforceUserRestrictionPermission(who, "no_config_tethering", userHandle);
            HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
            if (ap.disableWifiAp != disabled) {
                ap.disableWifiAp = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_config_tethering", userHandle);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isWifiApDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableWifiAp;
                }
            } else if (this.mUserManager.hasUserRestriction("no_config_tethering", new UserHandle(userHandle))) {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin == null || !admin2.mHwActiveAdmin.disableWifiAp) {
                        i++;
                    } else {
                        return true;
                    }
                }
                return false;
            } else {
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
        synchronized (this) {
            enforceUserRestrictionPermission(who, "no_usb_file_transfer", userHandle);
            HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
            if (ap.disableUSBData != disabled) {
                ap.disableUSBData = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_usb_file_transfer", userHandle);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isUSBDataDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
                if (ap != null) {
                    z = ap.disableUSBData;
                }
            } else if (this.mUserManager.hasUserRestriction("no_usb_file_transfer", new UserHandle(userHandle))) {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableUSBData) {
                        i++;
                    } else {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    public void setExternalStorageDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_SDCARD", "does not have SDCARD MDM permission!");
        synchronized (this) {
            enforceUserRestrictionPermission(who, "no_physical_media", userHandle);
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableExternalStorage != disabled) {
                admin.disableExternalStorage = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_physical_media", userHandle);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isExternalStorageDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableExternalStorage;
                }
            } else if (this.mUserManager.hasUserRestriction("no_physical_media", new UserHandle(userHandle))) {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin == null || !admin2.mHwActiveAdmin.disableExternalStorage) {
                        i++;
                    } else {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    public void setNFCDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NFC", "does not have NFC MDM permission!");
        synchronized (this) {
            if (who == null) {
                throw new NullPointerException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableNFC != disabled) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
                    if (nfcAdapter != null) {
                        boolean nfcOriginalState = nfcAdapter.isEnabled();
                        if (disabled && nfcOriginalState) {
                            boolean setDisableResult = nfcAdapter.disable();
                        }
                    }
                    admin.disableNFC = disabled;
                    saveSettingsLocked(userHandle);
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isNFCDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableNFC;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin == null || !admin2.mHwActiveAdmin.disableNFC) {
                        i++;
                    } else {
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
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableDataConnectivity != disabled) {
                admin.disableDataConnectivity = disabled;
                saveSettingsLocked(userHandle);
            }
            if (disabled) {
                TelephonyManager.from(this.mContext).setDataEnabled(disabled ^ 1);
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isDataConnectivityDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableDataConnectivity;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin == null || !admin2.mHwActiveAdmin.disableDataConnectivity) {
                        i++;
                    } else {
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
        synchronized (this) {
            enforceUserRestrictionPermission(who, "no_outgoing_calls", userHandle);
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
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

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isVoiceDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableVoice;
                }
            } else if (this.mUserManager.hasUserRestriction("no_outgoing_calls", new UserHandle(userHandle))) {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin == null || !admin2.mHwActiveAdmin.disableVoice) {
                        i++;
                    } else {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    public void setSMSDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_MMS", "Does not hava SMS disable MDM permission.");
        synchronized (this) {
            enforceUserRestrictionPermission(who, "no_sms", userHandle);
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableSMS != disabled) {
                admin.disableSMS = disabled;
                saveSettingsLocked(userHandle);
            }
            hwSyncDeviceCapabilitiesLocked("no_sms", userHandle);
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isSMSDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableSMS;
                }
            } else if (this.mUserManager.hasUserRestriction("no_sms", new UserHandle(userHandle))) {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin == null || !admin2.mHwActiveAdmin.disableSMS) {
                        i++;
                    } else {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0038, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setStatusBarExpandPanelDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this) {
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableStatusBarExpandPanel != disabled) {
                if (!disabled || (setStatusBarPanelDisabledInternal(userHandle) ^ 1) == 0) {
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

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isStatusBarExpandPanelDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableStatusBarExpandPanel;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin == null || !admin2.mHwActiveAdmin.disableStatusBarExpandPanel) {
                        i++;
                    } else {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void hangupCalling(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "Does not hava hangup calling permission.");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (getHwActiveAdmin(who, userHandle) != null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    TelephonyManager.from(this.mContext).endCall();
                    UiThread.getHandler().post(new Runnable() {
                        public void run() {
                            if (HwDevicePolicyManagerService.this.mErrorDialog != null) {
                                HwDevicePolicyManagerService.this.mErrorDialog.dismiss();
                                HwDevicePolicyManagerService.this.mErrorDialog = null;
                            }
                            HwDevicePolicyManagerService.this.mErrorDialog = new Builder(HwDevicePolicyManagerService.this.mContext, 33947691).setMessage(33686011).setPositiveButton(33685688, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    HwDevicePolicyManagerService.this.mErrorDialog.dismiss();
                                }
                            }).setCancelable(true).create();
                            HwDevicePolicyManagerService.this.mErrorDialog.getWindow().setType(DeviceStatusConstant.MSDP_DEVICE_STATUS_MOVEMENT);
                            HwDevicePolicyManagerService.this.mErrorDialog.show();
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    public void installPackage(ComponentName who, String packagePath, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (getHwActiveAdmin(who, userHandle) != null) {
                installPackage(packagePath, who.getPackageName());
            }
        }
    }

    public void uninstallPackage(ComponentName who, String packageName, boolean keepData, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (getHwActiveAdmin(who, userHandle) != null) {
                    uninstallPackage(packageName, keepData);
                }
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
    }

    public void clearPackageData(ComponentName who, String packageName, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                } else if (TextUtils.isEmpty(packageName)) {
                    throw new IllegalArgumentException("packageNames is null or empty");
                } else {
                    enforceCheckNotSystemApp(packageName, userHandle);
                    if (getHwActiveAdmin(who, userHandle) != null) {
                        long id = Binder.clearCallingIdentity();
                        try {
                            boolean ret = ((ActivityManager) this.mContext.getSystemService("activity")).clearApplicationUserData(packageName, null);
                        } finally {
                            Binder.restoreCallingIdentity(id);
                        }
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
    }

    public void enableInstallPackage(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin != null) {
                admin.disableInstallSource = false;
                admin.installSourceWhitelist = null;
            }
            saveSettingsLocked(userHandle);
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(2, isInstallSourceDisabled(null, userHandle));
        }
    }

    public void disableInstallSource(ComponentName who, List<String> whitelist, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have wifi MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(whitelist)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (whitelist != null) {
                    if (!whitelist.isEmpty()) {
                        HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
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
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(2, isInstallSourceDisabled(null, userHandle));
                this.mHwAdminCache.syncHwAdminCache(3, getInstallPackageSourceWhiteList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + whitelist + " is invalid.");
    }

    /* JADX WARNING: Missing block: B:8:0x000d, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isInstallSourceDisabled(ComponentName who, int userHandle) {
        boolean z = false;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin != null) {
                    z = admin.disableInstallSource;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                int i = 0;
                while (i < N) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin == null || !admin2.mHwActiveAdmin.disableInstallSource) {
                        i++;
                    } else {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getInstallPackageSourceWhiteList(ComponentName who, int userHandle) {
        List<String> list = null;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (!(admin.installSourceWhitelist == null || admin.installSourceWhitelist.isEmpty())) {
                    list = admin.installSourceWhitelist;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> whiteList = new ArrayList();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(whiteList, admin2.mHwActiveAdmin.installSourceWhitelist);
                    }
                }
                return whiteList;
            }
        }
    }

    public void addPersistentApp(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
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
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(4, getPersistentApp(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    public void removePersistentApp(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).persistentAppList, packageNames);
                        saveSettingsLocked(userHandle);
                        sendPersistentAppToIAware(userHandle);
                    }
                }
                throw new IllegalArgumentException("packageNames is null or empty");
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

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:21:0x0048, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getPersistentApp(ComponentName who, int userHandle) {
        List<String> list = null;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (!(admin.persistentAppList == null || admin.persistentAppList.isEmpty())) {
                    list = admin.persistentAppList;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> totalList = new ArrayList();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(totalList, admin2.mHwActiveAdmin.persistentAppList);
                    }
                }
                if (!totalList.isEmpty()) {
                    Object list2 = totalList;
                }
            }
        }
    }

    public void addDisallowedRunningApp(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava manager app MDM permission.");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                        if (admin.disallowedRunningAppList == null) {
                            admin.disallowedRunningAppList = new ArrayList();
                        }
                        HwDevicePolicyManagerServiceUtil.isOverLimit(admin.disallowedRunningAppList, packageNames);
                        filterOutSystemAppList(packageNames, userHandle);
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.disallowedRunningAppList, packageNames);
                        saveSettingsLocked(userHandle);
                        for (String packageName : packageNames) {
                            killApplicationInner(packageName);
                        }
                    }
                }
                throw new IllegalArgumentException("packageNames is null or empty");
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
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).disallowedRunningAppList, packageNames);
                        saveSettingsLocked(userHandle);
                    }
                }
                throw new IllegalArgumentException("packageNames is null or empty");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(5, getDisallowedRunningApp(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:21:0x0048, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getDisallowedRunningApp(ComponentName who, int userHandle) {
        List<String> list = null;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (!(admin.disallowedRunningAppList == null || admin.disallowedRunningAppList.isEmpty())) {
                    list = admin.disallowedRunningAppList;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> totalList = new ArrayList();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(totalList, admin2.mHwActiveAdmin.disallowedRunningAppList);
                    }
                }
                if (!totalList.isEmpty()) {
                    Object list2 = totalList;
                }
            }
        }
    }

    public void addInstallPackageWhiteList(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
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
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).installPackageWhitelist, packageNames);
                        saveSettingsLocked(userHandle);
                    }
                }
                throw new IllegalArgumentException("packageNames is null or empty");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(6, getInstallPackageWhiteList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:21:0x0048, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getInstallPackageWhiteList(ComponentName who, int userHandle) {
        List<String> list = null;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (!(admin.installPackageWhitelist == null || admin.installPackageWhitelist.isEmpty())) {
                    list = admin.installPackageWhitelist;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> whitelist = new ArrayList();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(whitelist, admin2.mHwActiveAdmin.installPackageWhitelist);
                    }
                }
                if (!whitelist.isEmpty()) {
                    Object list2 = whitelist;
                }
            }
        }
    }

    public void addDisallowedUninstallPackages(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
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
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).disallowedUninstallPackageList, packageNames);
                        saveSettingsLocked(userHandle);
                    }
                }
                throw new IllegalArgumentException("packageNames is null or empty");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(7, getDisallowedUninstallPackageList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:21:0x0048, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getDisallowedUninstallPackageList(ComponentName who, int userHandle) {
        List<String> list = null;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (!(admin.disallowedUninstallPackageList == null || admin.disallowedUninstallPackageList.isEmpty())) {
                    list = admin.disallowedUninstallPackageList;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> blacklist = new ArrayList();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(blacklist, admin2.mHwActiveAdmin.disallowedUninstallPackageList);
                    }
                }
                if (!blacklist.isEmpty()) {
                    Object list2 = blacklist;
                }
            }
        }
    }

    public void addDisabledDeactivateMdmPackages(ComponentName who, List<String> packageNames, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                        if (admin.disabledDeactiveMdmPackagesList == null) {
                            admin.disabledDeactiveMdmPackagesList = new ArrayList();
                        }
                        HwDevicePolicyManagerServiceUtil.isOverLimit(admin.disabledDeactiveMdmPackagesList, packageNames);
                        filterOutSystemAppList(packageNames, userHandle);
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.disabledDeactiveMdmPackagesList, packageNames);
                        saveSettingsLocked(userHandle);
                    }
                }
                throw new IllegalArgumentException("packageNames is null or empty");
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
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                if (packageNames != null) {
                    if (!packageNames.isEmpty()) {
                        HwDevicePolicyManagerServiceUtil.removeItemsFromList(getHwActiveAdmin(who, userHandle).disabledDeactiveMdmPackagesList, packageNames);
                        saveSettingsLocked(userHandle);
                    }
                }
                throw new IllegalArgumentException("packageNames is null or empty");
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(18, getDisabledDeactivateMdmPackageList(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageNames + " is invalid.");
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:21:0x0048, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getDisabledDeactivateMdmPackageList(ComponentName who, int userHandle) {
        List<String> list = null;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (!(admin.disabledDeactiveMdmPackagesList == null || admin.disabledDeactiveMdmPackagesList.isEmpty())) {
                    list = admin.disabledDeactiveMdmPackagesList;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> blacklist = new ArrayList();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(blacklist, admin2.mHwActiveAdmin.disabledDeactiveMdmPackagesList);
                    }
                }
                if (!blacklist.isEmpty()) {
                    Object list2 = blacklist;
                }
            }
        }
    }

    public void killApplicationProcess(ComponentName who, String packageName, int userHandle) {
        if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "Does not hava application management permission.");
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                } else if (TextUtils.isEmpty(packageName)) {
                    throw new IllegalArgumentException("Package name is empty");
                } else if (packageName.equals(who.getPackageName())) {
                    throw new IllegalArgumentException("Can not kill the caller application");
                } else {
                    enforceCheckNotSystemApp(packageName, userHandle);
                    if (getHwActiveAdmin(who, userHandle) != null) {
                        killApplicationInner(packageName);
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
    }

    private void killApplicationInner(String packageName) {
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
            for (RunningTaskInfo ti : am.getRunningTasks(10000)) {
                if (packageName.equals(ti.baseActivity.getPackageName())) {
                    am.forceStopPackage(packageName);
                    break;
                }
            }
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0035, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void shutdownOrRebootDevice(int code, ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            try {
                IPowerManager power = Stub.asInterface(ServiceManager.getService("power"));
                if (power != null) {
                    if (code == 1501) {
                        power.shutdown(false, null, false);
                    } else if (code == 1502) {
                        power.reboot(false, null, false);
                    }
                    Binder.restoreCallingIdentity(callingId);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "exception is " + e.getMessage());
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public void configExchangeMailProvider(ComponentName who, Bundle para, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_EMAIL", "does not have EMAIL MDM permission!");
        synchronized (this) {
            if (who == null || para == null) {
                throw new IllegalArgumentException("ComponentName or para is null");
            } else if (HwDevicePolicyManagerServiceUtil.isValidExchangeParameter(para)) {
                HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
                if (ap.mailProviderlist == null) {
                    ap.mailProviderlist = new ArrayList();
                    ap.mailProviderlist.add(para);
                    saveSettingsLocked(userHandle);
                } else if (ap.mailProviderlist.size() + 1 > 20) {
                    throw new IllegalArgumentException("already exceeds max number.");
                } else {
                    boolean isAlready = false;
                    Object provider = null;
                    for (Bundle each : ap.mailProviderlist) {
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
                }
            } else {
                throw new IllegalArgumentException("some paremeter is null");
            }
        }
    }

    /* JADX WARNING: Missing block: B:24:0x0045, code:
            return r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Bundle getMailProviderForDomain(ComponentName who, String domain, int userHandle) {
        if (userHandle != 0) {
            return null;
        }
        if (TextUtils.isEmpty(domain)) {
            throw new IllegalArgumentException("domain is empty.");
        }
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.mailProviderlist == null) {
                    return null;
                }
                boolean matched = false;
                Bundle retProvider = null;
                for (Bundle provider : admin.mailProviderlist) {
                    matched = HwDevicePolicyManagerServiceUtil.matchProvider(domain, provider.getString("domain"));
                    if (matched) {
                        retProvider = provider;
                        break;
                    }
                }
                if (!matched) {
                    retProvider = null;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
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
    }

    public boolean isRooted(ComponentName who, int userHandle) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            String currentState = SystemProperties.get("persist.sys.root.status");
            if (TextUtils.isEmpty(currentState) || ("0".equals(currentState) ^ 1) != 0) {
                return true;
            }
            return false;
        }
    }

    public void setSafeModeDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableSafeMode != disabled) {
                admin.disableSafeMode = disabled;
                saveSettingsLocked(userHandle);
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(10, isSafeModeDisabled(null, userHandle));
        }
    }

    public boolean isSafeModeDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableSafeMode;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableSafeMode) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void setAdbDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have MDM_USB permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableAdb != disabled) {
                admin.disableAdb = disabled;
                saveSettingsLocked(userHandle);
            }
        }
        long identityToken = Binder.clearCallingIdentity();
        if (disabled) {
            if (Global.getInt(this.mContext.getContentResolver(), "adb_enabled", 0) > 0) {
                Global.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
            }
        }
        Binder.restoreCallingIdentity(identityToken);
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(11, isAdbDisabled(null, userHandle));
        }
    }

    public boolean isAdbDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableAdb;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableAdb) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void setUSBOtgDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have MDM_USB permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableUSBOtg != disabled) {
                admin.disableUSBOtg = disabled;
                saveSettingsLocked(userHandle);
            }
        }
        long identityToken = Binder.clearCallingIdentity();
        StorageManager sm = (StorageManager) this.mContext.getSystemService("storage");
        for (StorageVolume storageVolume : sm.getVolumeList()) {
            if (storageVolume.isRemovable()) {
                if ("mounted".equals(sm.getVolumeState(storageVolume.getPath()))) {
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
        }
        Binder.restoreCallingIdentity(identityToken);
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(12, isUSBOtgDisabled(null, userHandle));
        }
    }

    public boolean isUSBOtgDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableUSBOtg;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableUSBOtg) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void setGPSDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_LOCATION", "does not have MDM_LOCATION permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableGPS != disabled) {
                admin.disableGPS = disabled;
                saveSettingsLocked(userHandle);
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
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableGPS;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableGPS) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void turnOnGPS(ComponentName who, boolean on, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_LOCATION", "does not have MDM_LOCATION permission!");
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        synchronized (this) {
            getHwActiveAdmin(who, userHandle);
        }
        if (isGPSTurnOn(who, userHandle) != on) {
            long identityToken = Binder.clearCallingIdentity();
            if (!Secure.setLocationProviderEnabledForUser(this.mContext.getContentResolver(), "gps", on, ActivityManager.getCurrentUser())) {
                Log.e(TAG, "setLocationProviderEnabledForUser failed");
            }
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    public boolean isGPSTurnOn(ComponentName who, int userHandle) {
        synchronized (this) {
            getHwActiveAdmin(who, userHandle);
        }
        long identityToken = Binder.clearCallingIdentity();
        boolean isGPSEnabled = Secure.isLocationProviderEnabledForUser(this.mContext.getContentResolver(), "gps", ActivityManager.getCurrentUser());
        Binder.restoreCallingIdentity(identityToken);
        return isGPSEnabled;
    }

    public void setTaskButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableTaskKey != disabled) {
                admin.disableTaskKey = disabled;
                saveSettingsLocked(userHandle);
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(15, isTaskButtonDisabled(null, userHandle));
        }
    }

    public boolean isTaskButtonDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableTaskKey;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableTaskKey) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void setHomeButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableHomeKey != disabled) {
                admin.disableHomeKey = disabled;
                saveSettingsLocked(userHandle);
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(14, isHomeButtonDisabled(null, userHandle));
        }
    }

    public boolean isHomeButtonDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableHomeKey;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableHomeKey) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void setBackButtonDisabled(ComponentName who, boolean disabled, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have DEVICE MANAGER permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
            if (admin.disableBackKey != disabled) {
                admin.disableBackKey = disabled;
                saveSettingsLocked(userHandle);
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(16, isBackButtonDisabled(null, userHandle));
        }
    }

    public boolean isBackButtonDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableBackKey;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableBackKey) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void setSysTime(ComponentName who, long millis, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device manager MDM permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long id = Binder.clearCallingIdentity();
            SystemClock.setCurrentTimeMillis(millis);
            Binder.restoreCallingIdentity(id);
        }
    }

    /* JADX WARNING: Missing block: B:37:0x0097, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCustomSettingsMenu(ComponentName who, List<String> menusToDelete, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            if (menusToDelete != null) {
                try {
                    if (!menusToDelete.isEmpty()) {
                        String oldMenus = Global.getStringForUser(this.mContext.getContentResolver(), SETTINGS_MENUS_REMOVE, userHandle);
                        String splitter = ",";
                        StringBuffer newMenus = new StringBuffer();
                        if (!TextUtils.isEmpty(oldMenus)) {
                            newMenus.append(oldMenus);
                        }
                        for (String menu : menusToDelete) {
                            if (oldMenus == null || !oldMenus.contains(menu)) {
                                newMenus.append(menu).append(splitter);
                            }
                        }
                        Global.putStringForUser(this.mContext.getContentResolver(), SETTINGS_MENUS_REMOVE, newMenus.toString(), userHandle);
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
            Global.putStringForUser(this.mContext.getContentResolver(), SETTINGS_MENUS_REMOVE, "", userHandle);
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setDefaultLauncher(ComponentName who, String packageName, String className, int userHandle) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName is null or empty");
        } else if (TextUtils.isEmpty(className)) {
            throw new IllegalArgumentException("className is null or empty");
        } else {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.SDK_LAUNCHER", "Does not have sdk_launcher permission.");
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                LauncherUtils.setDefaultLauncher(this.mContext, packageName, className);
                Binder.restoreCallingIdentity(callingId);
                admin.disableChangeLauncher = true;
                saveSettingsLocked(userHandle);
                if (this.mHwAdminCache != null) {
                    this.mHwAdminCache.syncHwAdminCache(17, isChangeLauncherDisabled(null, userHandle));
                }
            }
        }
    }

    public void clearDefaultLauncher(ComponentName who, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.SDK_LAUNCHER", "Does not have sdk_launcher permission.");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle).disableChangeLauncher = false;
            saveSettingsLocked(userHandle);
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(17, isChangeLauncherDisabled(null, userHandle));
            }
            long callingId = Binder.clearCallingIdentity();
            LauncherUtils.clearDefaultLauncher(this.mContext);
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public boolean isChangeLauncherDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdmin(who, userHandle).disableChangeLauncher;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableChangeLauncher) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public Bitmap captureScreen(ComponentName who, int userHandle) {
        Bitmap bmp;
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CAPTURE_SCREEN", "Does not have MDM_CAPTURE_SCREEN permission.");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            bmp = CaptureScreenUtils.captureScreen(this.mContext);
            Binder.restoreCallingIdentity(callingId);
        }
        return bmp;
    }

    public void addApn(ComponentName who, Map<String, String> apnInfo, int userHandle) {
        if (apnInfo == null || apnInfo.isEmpty()) {
            throw new IllegalArgumentException("apnInfo is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            ApnUtils.addApn(this.mContext.getContentResolver(), apnInfo);
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void deleteApn(ComponentName who, String apnId, int userHandle) {
        if (TextUtils.isEmpty(apnId)) {
            throw new IllegalArgumentException("apnId is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            ApnUtils.deleteApn(this.mContext.getContentResolver(), apnId);
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void updateApn(ComponentName who, Map<String, String> apnInfo, String apnId, int userHandle) {
        if (apnInfo == null || apnInfo.isEmpty()) {
            throw new IllegalArgumentException("apnInfo is empty.");
        } else if (TextUtils.isEmpty(apnId)) {
            throw new IllegalArgumentException("apnId is empty.");
        } else {
            enforceHwCrossUserPermission(userHandle);
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                getHwActiveAdmin(who, userHandle);
                long callingId = Binder.clearCallingIdentity();
                ApnUtils.updateApn(this.mContext.getContentResolver(), apnInfo, apnId);
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public void setPreferApn(ComponentName who, String apnId, int userHandle) {
        if (TextUtils.isEmpty(apnId)) {
            throw new IllegalArgumentException("apnId is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APN", "Does not have apn permission.");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            ApnUtils.setPreferApn(this.mContext.getContentResolver(), apnId);
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public List<String> queryApn(ComponentName who, Map<String, String> apnInfo, int userHandle) {
        if (apnInfo == null || apnInfo.isEmpty()) {
            throw new IllegalArgumentException("apnInfo is empty.");
        }
        List<String> ids;
        enforceHwCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            ids = ApnUtils.queryApn(this.mContext.getContentResolver(), apnInfo);
            Binder.restoreCallingIdentity(callingId);
        }
        return ids;
    }

    public Map<String, String> getApnInfo(ComponentName who, String apnId, int userHandle) {
        if (TextUtils.isEmpty(apnId)) {
            throw new IllegalArgumentException("apnId is empty.");
        }
        Map<String, String> apnInfo;
        enforceHwCrossUserPermission(userHandle);
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            apnInfo = ApnUtils.getApnInfo(this.mContext.getContentResolver(), apnId);
            Binder.restoreCallingIdentity(callingId);
        }
        return apnInfo;
    }

    public void addNetworkAccessWhitelist(ComponentName who, List<String> addrList, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have network_manager MDM permission!");
        if (HwDevicePolicyManagerServiceUtil.isValidIPAddrs(addrList)) {
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (admin.networkAccessWhitelist == null) {
                    admin.networkAccessWhitelist = new ArrayList();
                }
                HwDevicePolicyManagerServiceUtil.isAddrOverLimit(admin.networkAccessWhitelist, addrList);
                HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(admin.networkAccessWhitelist, addrList);
                saveSettingsLocked(userHandle);
                setNetworkAccessWhitelist(admin.networkAccessWhitelist);
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
            synchronized (this) {
                if (who == null) {
                    throw new IllegalArgumentException("ComponentName is null");
                }
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                HwDevicePolicyManagerServiceUtil.removeItemsFromList(admin.networkAccessWhitelist, addrList);
                saveSettingsLocked(userHandle);
                setNetworkAccessWhitelist(admin.networkAccessWhitelist);
            }
            if (this.mHwAdminCache != null) {
                this.mHwAdminCache.syncHwAdminCache(9, getNetworkAccessWhitelist(null, userHandle));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("addrlist invalid");
    }

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:21:0x0048, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getNetworkAccessWhitelist(ComponentName who, int userHandle) {
        List<String> list = null;
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
                if (!(admin.networkAccessWhitelist == null || admin.networkAccessWhitelist.isEmpty())) {
                    list = admin.networkAccessWhitelist;
                }
            } else {
                DevicePolicyData policy = getUserData(userHandle);
                ArrayList<String> addrList = new ArrayList();
                int N = policy.mAdminList.size();
                for (int i = 0; i < N; i++) {
                    ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                    if (admin2.mHwActiveAdmin != null) {
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(addrList, admin2.mHwActiveAdmin.networkAccessWhitelist);
                    }
                }
                if (!addrList.isEmpty()) {
                    Object list2 = addrList;
                }
            }
        }
    }

    private void setNetworkAccessWhitelist(List<String> whitelist) {
        String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
        IBinder b = ServiceManager.getService("network_management");
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        if (b != null) {
            try {
                _data.writeInterfaceToken("android.os.INetworkManagementService");
                _data.writeStringList(whitelist);
                b.transact(1106, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                Log.e(TAG, "setNetworkAccessWhitelist error", localRemoteException);
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public boolean getHwAdminCachedValue(int code) {
        int type = -1;
        switch (code) {
            case 4001:
                type = 0;
                break;
            case 4002:
                type = 1;
                break;
            case 4003:
                type = 2;
                break;
            case 4009:
                type = 8;
                break;
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
            case 5021:
                type = 29;
                break;
            case 5022:
                type = 32;
                break;
        }
        if (this.mHwAdminCache == null || type == -1) {
            return false;
        }
        return this.mHwAdminCache.getCachedValue(type);
    }

    public List<String> getHwAdminCachedList(int code) {
        List<String> result = null;
        int type = -1;
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
            case 4010:
                type = 9;
                break;
            case 4019:
                type = 18;
                break;
            case 4020:
                type = 20;
                break;
            case 4027:
                type = 27;
                break;
            case 4028:
                type = 28;
                break;
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

    private HwActiveAdmin getHwActiveAdmin(ComponentName who, int userHandle) {
        ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
        if (admin == null) {
            throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
        } else if (admin.getUid() != Binder.getCallingUid()) {
            throw new SecurityException("Admin " + who + " is not owned by uid " + Binder.getCallingUid());
        } else {
            HwActiveAdmin hwadmin = admin.mHwActiveAdmin;
            if (hwadmin != null) {
                return hwadmin;
            }
            hwadmin = new HwActiveAdmin();
            admin.mHwActiveAdmin = hwadmin;
            return hwadmin;
        }
    }

    private void setHwUserRestriction(String key, boolean disable, int userHandle) {
        UserHandle user = new UserHandle(userHandle);
        boolean alreadyRestricted = this.mUserManager.hasUserRestriction(key, user);
        if (HWFLOW) {
            Log.i(TAG, "setUserRestriction for (" + key + ", " + userHandle + "), is alreadyRestricted: " + alreadyRestricted);
        }
        long id = Binder.clearCallingIdentity();
        if (disable && (alreadyRestricted ^ 1) != 0) {
            try {
                if ("no_config_tethering".equals(key)) {
                    if (((WifiManager) this.mContext.getSystemService("wifi")).isWifiApEnabled()) {
                        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).stopTethering(0);
                        ((NotificationManager) this.mContext.getSystemService("notification")).cancelAsUser(null, 17303395, UserHandle.ALL);
                    }
                } else if ("no_physical_media".equals(key)) {
                    boolean hasExternalSdcard = StorageUtils.hasExternalSdcard(this.mContext);
                    boolean dafaultIsSdcard = DefaultStorageLocation.isSdcard();
                    if (hasExternalSdcard && (dafaultIsSdcard ^ 1) != 0) {
                        Log.w(TAG, "call doUnMount");
                        StorageUtils.doUnMount(this.mContext);
                    } else if (hasExternalSdcard && dafaultIsSdcard) {
                        if (StorageUtils.isSwitchPrimaryVolumeSupported()) {
                            throw new IllegalStateException("could not disable sdcard when it is primary card.");
                        }
                    }
                } else if ("no_usb_file_transfer".equals(key)) {
                    if (disable) {
                        Global.putStringForUser(this.mContext.getContentResolver(), "adb_enabled", "0", userHandle);
                    }
                    this.mUserManager.setUserRestriction("no_debugging_features", true, user);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(id);
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
        int userHandle = user.getIdentifier();
        return this.mUserManager.hasUserRestriction(key, user);
    }

    protected void syncHwDeviceSettingsLocked(int userHandle) {
        if (userHandle != 0) {
            Log.w(TAG, "userHandle is not USER_OWNER, return ");
            return;
        }
        combineAllPolicies(userHandle, true);
        try {
            synchronized (this) {
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
    }

    private void hwSyncDeviceCapabilitiesLocked(String restriction, int userHandle) {
        boolean disabled = false;
        boolean alreadyRestricted = haveHwUserRestriction(restriction, userHandle);
        DevicePolicyData policy = getUserData(userHandle);
        int N = policy.mAdminList.size();
        for (int i = 0; i < N; i++) {
            if (isUserRestrictionDisabled(restriction, ((ActiveAdmin) policy.mAdminList.get(i)).mHwActiveAdmin)) {
                disabled = true;
                break;
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
        return "no_physical_media".equals(restriction) && admin.disableExternalStorage;
    }

    private void installPackage(String packagePath, String installerPackageName) {
        if (TextUtils.isEmpty(packagePath)) {
            throw new IllegalArgumentException("Install package path is empty");
        }
        long ident = Binder.clearCallingIdentity();
        try {
            final File tempFile = new File(packagePath.trim()).getCanonicalFile();
            if (tempFile.getName().endsWith(".apk")) {
                this.mContext.getPackageManager().installPackage(Uri.fromFile(tempFile), new PackageInstallObserver() {
                    public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
                        if (1 != returnCode) {
                            Log.e(HwDevicePolicyManagerService.TAG, "The package " + tempFile.getName() + "installed failed, error code: " + returnCode);
                        }
                    }
                }, 2, null);
                Binder.restoreCallingIdentity(ident);
            }
        } catch (IOException e) {
            Log.e(TAG, "Get canonical file failed for package path: " + packagePath + ", error: " + e.getMessage());
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void uninstallPackage(String packageName, boolean keepData) {
        int i = 0;
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("Uninstall package name is empty");
        } else if (HwDevicePolicyManagerServiceUtil.isValidatePackageName(packageName)) {
            long ident = Binder.clearCallingIdentity();
            try {
                PackageManager pm = this.mContext.getPackageManager();
                if (pm.getApplicationInfo(packageName, 0) != null) {
                    if (keepData) {
                        i = 1;
                    }
                    pm.deletePackage(packageName, null, i);
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Name not found for package: " + packageName);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new IllegalArgumentException("packageName:" + packageName + " is invalid.");
        }
    }

    private void filterOutSystemAppList(List<String> packageNames, int userHandle) {
        List<String> systemAppList = new ArrayList();
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
            return false;
        } catch (Exception e2) {
            HwLog.e(TAG, "failed to check system app ");
            return false;
        } finally {
            restoreCallingIdentity(id);
        }
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
            } else if (!((flags & 1) == 0 || (flags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0)) {
                Log.w(TAG, "SystemApp preInstalledFlag");
                flag = false;
            }
            int hwFlags = appInfo.hwFlags;
            if (!((flags & 1) == 0 || (hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0)) {
                flag = false;
                Log.d(TAG, "packageName is not systemFlag");
            }
            if ((67108864 & hwFlags) != 0) {
                flag = false;
            }
            return flag;
        } catch (Exception e) {
            Log.e(TAG, "could not get appInfo, exception is " + e);
            return false;
        }
    }

    public int getSDCardEncryptionStatus() {
        if (!isSupportCrypt) {
            return -1;
        }
        String sdStatus = SystemProperties.get("vold.cryptsd.state");
        if (sdStatus.equals("invalid")) {
            return 0;
        }
        if (sdStatus.equals("encrypting")) {
            return 3;
        }
        if (sdStatus.equals("decrypting")) {
            return 4;
        }
        if (sdStatus.equals("disable")) {
            return 1;
        }
        if (sdStatus.equals("enable")) {
            return 2;
        }
        if (sdStatus.equals("mismatch")) {
            return 5;
        }
        if (sdStatus.equals("wait_unlock")) {
            return 6;
        }
        return 0;
    }

    public void setSDCardDecryptionDisabled(ComponentName who, boolean disabled, int userHandle) {
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            HwActiveAdmin admin = getHwActiveAdminForCallerLocked(who);
            if (admin.disableDecryptSDCard != disabled) {
                admin.disableDecryptSDCard = disabled;
                saveSettingsLocked(userHandle);
            }
        }
        if (this.mHwAdminCache != null) {
            this.mHwAdminCache.syncHwAdminCache(19, isSDCardDecryptionDisabled(null, userHandle));
        }
    }

    public boolean isSDCardDecryptionDisabled(ComponentName who, int userHandle) {
        synchronized (this) {
            if (who != null) {
                boolean z = getHwActiveAdminUncheckedLocked(who, userHandle).disableDecryptSDCard;
                return z;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            int i = 0;
            while (i < N) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (admin.mHwActiveAdmin == null || !admin.mHwActiveAdmin.disableDecryptSDCard) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    private HwActiveAdmin getHwActiveAdminUncheckedLocked(ComponentName who, int userHandle) {
        ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
        if (admin != null) {
            HwActiveAdmin hwadmin = admin.mHwActiveAdmin;
            if (hwadmin != null) {
                return hwadmin;
            }
            hwadmin = new HwActiveAdmin();
            admin.mHwActiveAdmin = hwadmin;
            return hwadmin;
        }
        throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
    }

    private HwActiveAdmin getHwActiveAdminForCallerLocked(ComponentName who) {
        ActiveAdmin admin = getActiveAdminForCallerLocked(who, 7);
        if (admin != null) {
            HwActiveAdmin hwadmin = admin.mHwActiveAdmin;
            if (hwadmin != null) {
                return hwadmin;
            }
            hwadmin = new HwActiveAdmin();
            admin.mHwActiveAdmin = hwadmin;
            return hwadmin;
        }
        throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
    }

    protected void init() {
        if (!this.hasInit) {
            for (PolicyStruct struct : this.globalStructs) {
                if (struct != null) {
                    struct.getOwner().init(struct);
                }
            }
            this.hasInit = true;
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
    }

    private void addPlugin(DevicePolicyPlugin plugin) {
        if (plugin != null) {
            this.globalPlugins.add(plugin);
        }
    }

    private void addPolicyStruct(PolicyStruct struct) {
        if (struct != null) {
            this.globalStructs.add(struct);
            for (PolicyItem item : struct.getPolicyItems()) {
                globalPolicyItems.put(item.getPolicyName(), item);
            }
        }
    }

    public void bdReport(int eventID, String eventMsg) {
        if (this.mContext != null) {
            Flog.bdReport(this.mContext, eventID, eventMsg);
        }
    }

    /* JADX WARNING: Missing block: B:59:0x026d, code:
            if (r24 != 1) goto L_0x028c;
     */
    /* JADX WARNING: Missing block: B:60:0x026f, code:
            r30.mHwAdminCache.syncHwAdminCache(r32, getPolicy(null, r32, r34));
     */
    /* JADX WARNING: Missing block: B:61:0x028c, code:
            if (r16 == false) goto L_0x0313;
     */
    /* JADX WARNING: Missing block: B:63:0x0294, code:
            if (r24 != 1) goto L_0x0313;
     */
    /* JADX WARNING: Missing block: B:64:0x0296, code:
            r26 = true;
     */
    /* JADX WARNING: Missing block: B:65:0x0298, code:
            r23.onSetPolicyCompleted(r31, r32, r26);
     */
    /* JADX WARNING: Missing block: B:66:0x02a3, code:
            return r24;
     */
    /* JADX WARNING: Missing block: B:77:0x0313, code:
            r26 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int setPolicy(ComponentName who, String policyName, Bundle policyData, int userHandle) {
        HwLog.d(TAG, "setPolicy, policyName = " + policyName + ", caller :" + (who == null ? "null" : who.flattenToString()));
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        DevicePolicyPlugin plugin = findPluginByPolicyName(policyName);
        if (plugin == null) {
            HwLog.e(TAG, "no plugin found, pluginName = " + policyName + ", caller :" + who.flattenToString());
            return -1;
        } else if (plugin.checkCallingPermission(who, policyName)) {
            boolean golbalPolicyChanged = false;
            PolicyStruct struct = findPolicyStructByPolicyName(policyName);
            synchronized (this) {
                HwActiveAdmin admin = getHwActiveAdminUncheckedLocked(who, userHandle);
                PolicyItem newItem = null;
                if (struct != null) {
                    PolicyItem item = (PolicyItem) admin.adminPolicyItems.get(policyName);
                    PolicyItem oldItem = struct.getItemByPolicyName(policyName);
                    if (oldItem != null) {
                        PolicyItem policyItem = new PolicyItem(policyName, oldItem.getItemType());
                        if (item == null) {
                            policyItem.copyFrom(oldItem);
                            policyItem.addAttrValues(policyItem, policyData);
                        } else {
                            policyItem.deepCopyFrom(item);
                        }
                        policyItem.addAttrValues(policyItem, policyData);
                        PolicyItem combinedItem = combinePoliciesWithPolicyChanged(who, policyItem, policyName, userHandle);
                        PolicyItem globalItem = (PolicyItem) globalPolicyItems.get(policyName);
                        if (globalItem == null) {
                            HwLog.e(TAG, "no policy item found, pluginName = " + policyName + ", caller :" + who.flattenToString());
                            return -1;
                        } else if (globalItem.equals(combinedItem)) {
                            policyItem.setGlobalPolicyChanged(2);
                            golbalPolicyChanged = false;
                        } else {
                            policyItem.setGlobalPolicyChanged(1);
                            golbalPolicyChanged = true;
                        }
                    } else {
                        HwLog.e(TAG, "no policy item found, pluginName = " + policyName + ", caller :" + who.flattenToString());
                        return -1;
                    }
                }
                HwLog.i(TAG, "when setPolicy, is global PolicyChanged ? = " + golbalPolicyChanged);
                long beginTime = System.currentTimeMillis();
                HwLog.i(TAG, "onSetPolicy, begin time: " + beginTime);
                boolean onSetPolicyResult = plugin.onSetPolicy(who, policyName, policyData, golbalPolicyChanged);
                HwLog.i(TAG, "onSetPolicy, costs time: " + (System.currentTimeMillis() - beginTime));
                int result;
                if (onSetPolicyResult) {
                    admin.adminPolicyItems.put(policyName, newItem);
                    if (newItem.getItemType() == PolicyType.CONFIGURATION) {
                        globalPolicyItems.put(policyName, newItem);
                        for (PolicyStruct globalStruct : this.globalStructs) {
                            boolean found = false;
                            for (String name : globalStruct.getPolicyMap().keySet()) {
                                if (newItem.getPolicyName().equals(name)) {
                                    struct.addPolicyItem(newItem);
                                    found = true;
                                    continue;
                                    break;
                                }
                            }
                            if (found) {
                                break;
                            }
                        }
                    }
                    saveSettingsLocked(userHandle);
                    combineAllPolicies(userHandle, false);
                    result = 1;
                } else {
                    HwLog.e(TAG, "onSetPolicy failed, pluginName = " + policyName + ", caller :" + who.flattenToString());
                    result = -1;
                }
            }
        } else {
            HwLog.e(TAG, "permission denied: " + who.flattenToString());
            return -1;
        }
    }

    public Bundle getPolicy(ComponentName who, String policyName, int userHandle) {
        DevicePolicyPlugin plugin = findPluginByPolicyName(policyName);
        if (plugin == null) {
            HwLog.e(TAG, "no plugin found, policyName = " + policyName + ", caller :" + (who == null ? "null" : who.flattenToString()));
            return null;
        }
        HwLog.d(TAG, "get :" + policyName + (who == null ? "" : " ,cal :" + who.flattenToString()));
        Bundle resultBundle = null;
        synchronized (this) {
            if (who != null) {
                PolicyItem item = (PolicyItem) getHwActiveAdminUncheckedLocked(who, userHandle).adminPolicyItems.get(policyName);
                if (item != null) {
                    resultBundle = item.combineAllAttributes();
                }
            } else if (globalPolicyItems.get(policyName) != null) {
                resultBundle = ((PolicyItem) globalPolicyItems.get(policyName)).combineAllAttributes();
            }
            notifyOnGetPolicy(plugin, who, policyName, resultBundle);
        }
        return resultBundle;
    }

    private void notifyOnGetPolicy(DevicePolicyPlugin plugin, ComponentName who, String policyName, Bundle policyData) {
        plugin.onGetPolicy(who, policyName, policyData);
    }

    public int removePolicy(ComponentName who, String policyName, Bundle policyData, int userHandle) {
        HwLog.d(TAG, "removePolicy, policyName = " + policyName + ", caller :" + (who == null ? "null" : who.flattenToString()));
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        DevicePolicyPlugin plugin = findPluginByPolicyName(policyName);
        if (plugin == null) {
            HwLog.e(TAG, "no plugin found, pluginName = " + policyName + ", caller :" + who.flattenToString());
            return -1;
        } else if (plugin.checkCallingPermission(who, policyName)) {
            boolean golbalPolicyChanged;
            int result;
            PolicyStruct struct = findPolicyStructByPolicyName(policyName);
            synchronized (this) {
                HwActiveAdmin admin = getHwActiveAdminUncheckedLocked(who, userHandle);
                PolicyItem item = null;
                PolicyItem newItem = null;
                if (struct != null) {
                    item = (PolicyItem) admin.adminPolicyItems.get(policyName);
                    if (item != null) {
                        newItem = new PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType());
                        newItem.deepCopyFrom(item);
                        newItem.removeAttrValues(newItem, policyData);
                        PolicyItem combinedItem = combinePoliciesWithPolicyChanged(who, newItem, policyName, userHandle);
                        PolicyItem globalItem = (PolicyItem) globalPolicyItems.get(policyName);
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
                } else {
                    golbalPolicyChanged = false;
                }
                HwLog.i(TAG, "when removePolicy,  is global PolicyChanged ? = " + golbalPolicyChanged);
                long beginTime = System.currentTimeMillis();
                HwLog.i(TAG, "onRemovePolicy, begin time: " + beginTime);
                boolean onRemoveResult = plugin.onRemovePolicy(who, policyName, policyData, golbalPolicyChanged);
                HwLog.i(TAG, "onRemovePolicy, costs time: " + (System.currentTimeMillis() - beginTime));
                if (onRemoveResult) {
                    if (item == null || item.getItemType() != PolicyType.LIST || policyData == null) {
                        admin.adminPolicyItems.remove(policyName);
                    } else {
                        admin.adminPolicyItems.put(policyName, newItem);
                    }
                    saveSettingsLocked(userHandle);
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
            boolean z = golbalPolicyChanged && result == 1;
            plugin.onRemovePolicyCompleted(who, policyName, z);
            return result;
        } else {
            HwLog.e(TAG, "permission denied: " + who.flattenToString());
            return -1;
        }
    }

    /* JADX WARNING: Missing block: B:10:0x004c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void notifyPlugins(ComponentName who, int userHandle) {
        HwLog.d(TAG, "notifyPlugins: " + (who == null ? "null" : who.flattenToString()) + " userId: " + userHandle);
        ActiveAdmin activeAdminToRemove = getActiveAdminUncheckedLocked(who, userHandle);
        if (activeAdminToRemove != null && activeAdminToRemove.mHwActiveAdmin != null && activeAdminToRemove.mHwActiveAdmin.adminPolicyItems != null && !activeAdminToRemove.mHwActiveAdmin.adminPolicyItems.isEmpty()) {
            for (PolicyStruct struct : this.globalStructs) {
                if (struct != null) {
                    ArrayList<PolicyItem> removedPluginItems = new ArrayList();
                    for (PolicyItem removedItem : activeAdminToRemove.mHwActiveAdmin.adminPolicyItems.values()) {
                        if (removedItem != null) {
                            PolicyItem combinedItem = combinePoliciesWithoutRemovedPolicyItem(who, removedItem.getPolicyName(), userHandle);
                            if (((PolicyItem) globalPolicyItems.get(removedItem.getPolicyName())) == null) {
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
                    if (removedPluginItems.isEmpty()) {
                        continue;
                    } else {
                        DevicePolicyPlugin plugin = struct.getOwner();
                        if (plugin == null) {
                            HwLog.w(TAG, " policy struct has no owner");
                            return;
                        }
                        this.effectedItems.add(new EffectedItem(who, plugin, removedPluginItems));
                        long beginTime = System.currentTimeMillis();
                        HwLog.i(TAG, "onActiveAdminRemoved, begin time: " + beginTime);
                        plugin.onActiveAdminRemoved(who, removedPluginItems);
                        HwLog.i(TAG, "onActiveAdminRemoved, costs time: " + (System.currentTimeMillis() - beginTime));
                    }
                }
            }
        }
    }

    protected synchronized void removeActiveAdminCompleted(ComponentName who) {
        if (!this.effectedItems.isEmpty()) {
            Iterator<EffectedItem> it = this.effectedItems.iterator();
            while (it.hasNext()) {
                EffectedItem effectedItem = (EffectedItem) it.next();
                DevicePolicyPlugin plugin = effectedItem.effectedPlugin;
                if (plugin != null && who.equals(effectedItem.effectedAdmin)) {
                    plugin.onActiveAdminRemovedCompleted(effectedItem.effectedAdmin, effectedItem.effectedPolicies);
                    it.remove();
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
        for (PolicyStruct struct : this.globalStructs) {
            if (struct != null && struct.containsPolicyName(policyName)) {
                return struct;
            }
        }
        return null;
    }

    private synchronized void combineAllPolicies(int userHandle, boolean shouldChange) {
        DevicePolicyData policy = getUserData(userHandle);
        for (PolicyStruct struct : this.globalStructs) {
            for (String policyName : struct.getPolicyMap().keySet()) {
                PolicyItem globalItem = new PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType());
                globalItem.copyFrom(struct.getItemByPolicyName(policyName));
                int N = policy.mAdminList.size();
                int i;
                ActiveAdmin admin;
                if (globalItem.getItemType() != PolicyType.CONFIGURATION) {
                    for (i = 0; i < N; i++) {
                        admin = (ActiveAdmin) policy.mAdminList.get(i);
                        if (admin.mHwActiveAdmin != null) {
                            PolicyItem adminItem = (PolicyItem) admin.mHwActiveAdmin.adminPolicyItems.get(policyName);
                            if (adminItem != null && adminItem.hasAnyNonNullAttribute()) {
                                traverseCombinePolicyItem(globalItem, adminItem);
                            }
                        }
                    }
                } else if (shouldChange) {
                    for (i = N - 1; i >= 0; i--) {
                        admin = (ActiveAdmin) policy.mAdminList.get(i);
                        if (admin.mHwActiveAdmin != null) {
                            PolicyItem findItem = (PolicyItem) admin.mHwActiveAdmin.adminPolicyItems.get(policyName);
                            if (findItem != null) {
                                globalItem = findItem;
                                HwLog.w(TAG, "global policy will change: " + policyName);
                                break;
                            }
                        }
                    }
                } else {
                    HwLog.w(TAG, "global policy will not change: " + policyName);
                }
                globalPolicyItems.put(policyName, globalItem);
                struct.addPolicyItem(globalItem);
            }
        }
    }

    private synchronized PolicyItem combinePoliciesWithPolicyChanged(ComponentName who, PolicyItem newItem, String policyName, int userHandle) {
        PolicyItem globalAdminItem;
        DevicePolicyData policy = getUserData(userHandle);
        ActiveAdmin activeAdmin = getActiveAdminUncheckedLocked(who, userHandle);
        ArrayList<ActiveAdmin> adminList = new ArrayList();
        for (ActiveAdmin admin : policy.mAdminList) {
            adminList.add(admin);
        }
        if (activeAdmin != null) {
            if (adminList.size() > 0) {
                adminList.remove(activeAdmin);
            }
        }
        PolicyStruct struct = findPolicyStructByPolicyName(policyName);
        globalAdminItem = new PolicyItem(policyName, struct.getItemByPolicyName(policyName).getItemType());
        globalAdminItem.copyFrom(struct.getItemByPolicyName(policyName));
        int N = adminList.size();
        for (int i = 0; i < N; i++) {
            ActiveAdmin admin1 = (ActiveAdmin) adminList.get(i);
            if (admin1.mHwActiveAdmin != null) {
                PolicyItem adminItem = (PolicyItem) admin1.mHwActiveAdmin.adminPolicyItems.get(policyName);
                if (adminItem != null && adminItem.hasAnyNonNullAttribute()) {
                    traverseCombinePolicyItem(globalAdminItem, adminItem);
                }
            }
        }
        traverseCombinePolicyItem(globalAdminItem, newItem);
        return globalAdminItem;
    }

    private PolicyItem combinePoliciesWithoutRemovedPolicyItem(ComponentName who, String policyName, int userHandle) {
        DevicePolicyData policy = getUserData(userHandle);
        ArrayList<ActiveAdmin> adminList = new ArrayList();
        synchronized (this) {
            ActiveAdmin activeAdmin = getActiveAdminUncheckedLocked(who, userHandle);
            for (ActiveAdmin admin : policy.mAdminList) {
                adminList.add(admin);
            }
            if (activeAdmin != null) {
                if (adminList.size() > 0) {
                    adminList.remove(activeAdmin);
                }
            }
        }
        PolicyItem oldItem = findPolicyStructByPolicyName(policyName).getItemByPolicyName(policyName);
        PolicyItem globalAdminItem = null;
        if (oldItem != null) {
            globalAdminItem = new PolicyItem(policyName, oldItem.getItemType());
            globalAdminItem.copyFrom(oldItem);
            int N = adminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin1 = (ActiveAdmin) adminList.get(i);
                if (admin1.mHwActiveAdmin != null) {
                    PolicyItem adminItem = (PolicyItem) admin1.mHwActiveAdmin.adminPolicyItems.get(policyName);
                    if (adminItem != null && adminItem.hasAnyNonNullAttribute() && adminItem.getPolicyName().equals(policyName)) {
                        traverseCombinePolicyItem(globalAdminItem, adminItem);
                    }
                }
            }
        }
        return globalAdminItem;
    }

    private void traverseCombinePolicyItem(PolicyItem oldRoot, PolicyItem newRoot) {
        if (oldRoot != null && newRoot != null) {
            oldRoot.setAttributes(combineAttributes(oldRoot.getAttributes(), newRoot.getAttributes(), oldRoot));
            int n = oldRoot.getChildItem().size();
            for (int i = 0; i < n; i++) {
                traverseCombinePolicyItem((PolicyItem) oldRoot.getChildItem().get(i), (PolicyItem) newRoot.getChildItem().get(i));
            }
        }
    }

    private Bundle combineAttributes(Bundle oldAttr, Bundle newAttr, PolicyItem item) {
        switch (-getcom-android-server-devicepolicy-PolicyStruct$PolicyTypeSwitchesValues()[item.getItemType().ordinal()]) {
            case 1:
                for (String key : newAttr.keySet()) {
                    if (newAttr.get(key) != null) {
                        oldAttr.putString(key, newAttr.getString(key));
                    }
                }
                break;
            case 2:
                for (String key2 : newAttr.keySet()) {
                    if (newAttr.get(key2) != null) {
                        ArrayList<String> oldPolicyList = oldAttr.getStringArrayList(key2);
                        ArrayList<String> newPolicyList = newAttr.getStringArrayList(key2);
                        if (oldPolicyList == null) {
                            oldPolicyList = new ArrayList();
                        }
                        HwDevicePolicyManagerServiceUtil.addListWithoutDuplicate(oldPolicyList, newPolicyList);
                        oldAttr.putStringArrayList(key2, oldPolicyList);
                    }
                }
                break;
            case 3:
                for (String key22 : newAttr.keySet()) {
                    if (newAttr.get(key22) != null) {
                        oldAttr.putBoolean(key22, !oldAttr.getBoolean(key22) ? newAttr.getBoolean(key22) : true);
                    }
                }
                break;
        }
        return oldAttr;
    }

    public ArrayList<String> queryBrowsingHistory(ComponentName who, int userHandle) {
        String policyName = "network-black-list";
        ArrayList<String> historyList = new ArrayList();
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have network MDM permission!");
        enforceHwCrossUserPermission(userHandle);
        getHwActiveAdmin(who, userHandle);
        DevicePolicyPlugin plugin = findPluginByPolicyName("network-black-list");
        if (plugin == null || !(plugin instanceof DeviceNetworkPlugin)) {
            HwLog.e(TAG, "no DeviceNetworkPlugin found, pluginName = network-black-list");
            return historyList;
        }
        DeviceNetworkPlugin deviceNetworkPlugin = (DeviceNetworkPlugin) plugin;
        long callingId = Binder.clearCallingIdentity();
        historyList = deviceNetworkPlugin.queryBrowsingHistory();
        Binder.restoreCallingIdentity(callingId);
        return historyList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00d1 A:{Splitter: B:7:0x005c, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00ea A:{Splitter: B:9:0x0061, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Missing block: B:41:?, code:
            libcore.io.IoUtils.closeQuietly(r7);
     */
    /* JADX WARNING: Missing block: B:42:0x00c4, code:
            r6 = r7;
     */
    /* JADX WARNING: Missing block: B:44:0x00c6, code:
            com.android.server.devicepolicy.HwLog.d(TAG, "Can't find HwPolicy");
     */
    /* JADX WARNING: Missing block: B:45:0x00d0, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            com.android.server.devicepolicy.HwLog.e(TAG, "XmlPullParserException | IOException");
     */
    /* JADX WARNING: Missing block: B:50:?, code:
            libcore.io.IoUtils.closeQuietly(r6);
     */
    /* JADX WARNING: Missing block: B:54:0x00e2, code:
            r10 = th;
     */
    /* JADX WARNING: Missing block: B:61:0x00eb, code:
            r6 = r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasHwPolicy(int userHandle) {
        HwLog.d(TAG, "hasHwPolicy, userHandle :" + userHandle);
        synchronized (this) {
            String base;
            String DEVICE_POLICIES_XML = "device_policies.xml";
            if (userHandle == 0) {
                base = "/data/system/" + DEVICE_POLICIES_XML;
            } else {
                base = new File(Environment.getUserSystemDirectory(userHandle), DEVICE_POLICIES_XML).getAbsolutePath();
            }
            AutoCloseable stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(new JournaledFile(new File(base), new File(base + ".tmp")).chooseForRead());
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, StandardCharsets.UTF_8.name());
                    while (true) {
                        int type = parser.next();
                        if (type == 1) {
                            break;
                        } else if (type == 2) {
                            if ("hw_policy".equals(parser.getName())) {
                                while (true) {
                                    type = parser.next();
                                    if (type == 1 || type == 3) {
                                        break;
                                    } else if (type == 2) {
                                        HwLog.d(TAG, "find HwPolicy");
                                        IoUtils.closeQuietly(stream2);
                                        return true;
                                    }
                                }
                            }
                            if (type == 1) {
                                HwLog.d(TAG, "Can't find HwPolicy");
                                IoUtils.closeQuietly(stream2);
                                return false;
                            }
                        }
                    }
                } catch (XmlPullParserException e) {
                } catch (Throwable th) {
                    Throwable th2 = th;
                    Object stream3 = stream2;
                    IoUtils.closeQuietly(stream);
                    throw th2;
                }
            } catch (XmlPullParserException e2) {
            }
        }
    }

    protected boolean isSecureBlockEncrypted() {
        if (!StorageManager.isBlockEncrypted()) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        boolean isSecure;
        try {
            isSecure = IStorageManager.Stub.asInterface(ServiceManager.getService("mount")).isSecure();
            return isSecure;
        } catch (RemoteException e) {
            isSecure = TAG;
            Log.e(isSecure, "Error getting encryption type");
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX WARNING: Missing block: B:66:0x0189, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int configVpnProfile(ComponentName who, Bundle para, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (this) {
            KeyStore mKeyStore = KeyStore.getInstance();
            boolean paraIsNull = who == null || para == null;
            if (paraIsNull) {
                Log.e(TAG, "Bundle para is null or componentName is null!");
                return -1;
            } else if (isValidVpnConfig(para)) {
                VpnProfile profile = getProfile(para);
                if (mKeyStore.put("VPN_" + profile.key, profile.encode(), -1, 0)) {
                    String key = para.getString("key");
                    DevicePolicyData policy = getUserData(userHandle);
                    int N = policy.mAdminList.size();
                    for (int i = 0; i < N; i++) {
                        ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                        if (!(admin.mHwActiveAdmin == null || admin.mHwActiveAdmin.vpnProviderlist == null)) {
                            for (Bundle provider : admin.mHwActiveAdmin.vpnProviderlist) {
                                if (key.equals(provider.getString("key"))) {
                                    admin.mHwActiveAdmin.vpnProviderlist.remove(provider);
                                    saveSettingsLocked(userHandle);
                                }
                            }
                        }
                    }
                    HwActiveAdmin ap = getHwActiveAdmin(who, userHandle);
                    if (ap.vpnProviderlist != null) {
                        boolean isAlready = false;
                        Object delProvider = null;
                        for (Bundle provider2 : ap.vpnProviderlist) {
                            if (!(provider2 != null ? isEmpty(provider2.getString("key")) : true) && key.equals(provider2.getString("key"))) {
                                isAlready = true;
                                delProvider = provider2;
                                break;
                            }
                        }
                        boolean needDelete = isAlready && delProvider != null;
                        if (needDelete) {
                            ap.vpnProviderlist.remove(delProvider);
                        }
                        ap.vpnProviderlist.add(para);
                        saveSettingsLocked(userHandle);
                    } else {
                        ap.vpnProviderlist = new ArrayList();
                        ap.vpnProviderlist.add(para);
                        saveSettingsLocked(userHandle);
                    }
                } else {
                    Log.e(TAG, "Set vpn failed, check the config.");
                    return -1;
                }
            } else {
                Log.e(TAG, "This Config isn't valid vpnConfig");
                return -1;
            }
        }
    }

    public int removeVpnProfile(ComponentName who, Bundle para, int userHandle) {
        String key = para.getString("key");
        if (who == null || isEmpty(key)) {
            Log.e(TAG, "ComponentName or key is empty.");
            return -1;
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (this) {
            KeyStore mKeyStore = KeyStore.getInstance();
            boolean hasDeleted = false;
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
                if (!(admin.mHwActiveAdmin == null || admin.mHwActiveAdmin.vpnProviderlist == null)) {
                    for (Bundle provider : admin.mHwActiveAdmin.vpnProviderlist) {
                        if (key.equals(provider.getString("key"))) {
                            if (mKeyStore.delete("VPN_" + key)) {
                                admin.mHwActiveAdmin.vpnProviderlist.remove(provider);
                                saveSettingsLocked(userHandle);
                                hasDeleted = true;
                            } else {
                                Log.e(TAG, "Delete vpn failed, check the key.");
                                return -1;
                            }
                        }
                    }
                    continue;
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
        synchronized (this) {
            if (who != null) {
                HwActiveAdmin hwAdmin = getHwActiveAdmin(who, userHandle);
                if (hwAdmin.vpnProviderlist == null) {
                    return null;
                }
                for (Bundle provider : hwAdmin.vpnProviderlist) {
                    if (key.equals(provider.getString("key"))) {
                        return provider;
                    }
                }
                return null;
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin = (ActiveAdmin) policy.mAdminList.get(i);
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

    public Bundle getVpnList(ComponentName who, Bundle keyWords, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        synchronized (this) {
            ArrayList<String> vpnKeyList = new ArrayList();
            Bundle vpnListBundle = new Bundle();
            if (who != null) {
                HwActiveAdmin admin = getHwActiveAdmin(who, userHandle);
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
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                ActiveAdmin admin2 = (ActiveAdmin) policy.mAdminList.get(i);
                if (!(admin2.mHwActiveAdmin == null || admin2.mHwActiveAdmin.vpnProviderlist == null)) {
                    for (Bundle provider2 : admin2.mHwActiveAdmin.vpnProviderlist) {
                        if (!(isEmpty(provider2.getString("key")) || vpnKeyList.contains(provider2.getString("key")))) {
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

    /* JADX WARNING: Missing block: B:11:0x0038, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isValidVpnConfig(Bundle para) {
        if (para == null || isEmpty(para.getString("key")) || isEmpty(para.getString("name")) || isEmpty(para.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE)) || isEmpty(para.getString("server")) || Integer.parseInt(para.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE)) < 0 || Integer.parseInt(para.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE)) > 6) {
            return false;
        }
        switch (Integer.parseInt(para.getString(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE))) {
            case 2:
            case 4:
                return isEmpty(para.getString("ipsecSecret")) ^ 1;
            case 3:
            case 5:
                return isEmpty(para.getString("ipsecUserCert")) ^ 1;
            default:
                return true;
        }
    }

    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    public boolean getScreenCaptureDisabled(ComponentName who, int userHandle) {
        if (who != null) {
            return super.getScreenCaptureDisabled(who, userHandle);
        }
        boolean z;
        if (super.getScreenCaptureDisabled(who, userHandle)) {
            z = true;
        } else {
            z = HwDeviceManager.mdmDisallowOp(20, null);
        }
        return z;
    }

    public boolean formatSDCard(ComponentName who, String diskId, int userHandle) {
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_SDCARD", "does not have sd card MDM permission!");
        synchronized (this) {
            if (getActiveAdminUncheckedLocked(who, userHandle) == null) {
                throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
            }
            long token = Binder.clearCallingIdentity();
            try {
                ((StorageManager) this.mContext.getSystemService("storage")).partitionPublic(diskId);
            } catch (Exception e) {
                HwLog.e(TAG, "format sd card data error!");
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return true;
    }

    public void setAccountDisabled(ComponentName who, String accountType, boolean disabled, int userHandle) {
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app management MDM permission!");
        synchronized (this) {
            ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
            if (admin == null) {
                throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
            }
            if (disabled) {
                admin.accountTypesWithManagementDisabled.add(accountType);
            } else {
                admin.accountTypesWithManagementDisabled.remove(accountType);
            }
            saveSettingsLocked(userHandle);
        }
    }

    public boolean isAccountDisabled(ComponentName who, String accountType, int userHandle) {
        synchronized (this) {
            if (who != null) {
                ActiveAdmin admin = getActiveAdminUncheckedLocked(who, userHandle);
                if (admin != null) {
                    boolean contains = admin.accountTypesWithManagementDisabled.contains(accountType);
                    return contains;
                }
            }
            DevicePolicyData policy = getUserData(userHandle);
            int N = policy.mAdminList.size();
            for (int i = 0; i < N; i++) {
                if (((ActiveAdmin) policy.mAdminList.get(i)).accountTypesWithManagementDisabled.contains(accountType)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean installCertificateWithType(ComponentName who, int type, byte[] certBuffer, String alias, String password, int certInstallType, boolean requestAccess, int userHandle) {
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        boolean result;
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have install cert MDM permission!");
        synchronized (this) {
            if (getActiveAdminUncheckedLocked(who, userHandle) == null) {
                throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
            }
        }
        if (type == 0) {
            try {
                result = CertInstallHelper.installPkcs12Cert(password, certBuffer, alias, certInstallType);
            } catch (Exception e) {
                HwLog.e(TAG, "throw error when install cert");
                return false;
            }
        } else if (type == 1) {
            result = CertInstallHelper.installX509Cert(certBuffer, alias, certInstallType);
        } else {
            throw new IllegalArgumentException("the type of the installed cert is not illegal");
        }
        if (!requestAccess) {
            return result;
        }
        int callingUid = this.mInjector.binderGetCallingUid();
        long id = this.mInjector.binderClearCallingIdentity();
        try {
            KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, UserHandle.getUserHandleForUid(callingUid));
            try {
                keyChainConnection.getService().setGrant(callingUid, alias, true);
                this.mInjector.binderRestoreCallingIdentity(id);
                return true;
            } catch (RemoteException e2) {
                HwLog.e(TAG, "set grant certificate");
                this.mInjector.binderRestoreCallingIdentity(id);
                return false;
            } finally {
                keyChainConnection.close();
            }
        } catch (InterruptedException e3) {
            try {
                HwLog.w(TAG, "Interrupted while set granting certificate");
                Thread.currentThread().interrupt();
            } finally {
                this.mInjector.binderRestoreCallingIdentity(id);
            }
        }
    }

    protected long getUsrSetExtendTime() {
        String value = getPolicy(null, PASSWORD_CHANGE_EXTEND_TIME, UserHandle.myUserId()).getString("value");
        if (value == null || "".equals(value)) {
            return super.getUsrSetExtendTime();
        }
        return Long.parseLong(value);
    }

    public void setSilentActiveAdmin(ComponentName who, int userHandle) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have PERMISSION_MDM_DEVICE_MANAGER permission!");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            if (!isAdminActive(who, userHandle)) {
                HwLog.d(TAG, "setSilentActiveAdmin, mHasHwMdmFeature active supported.");
                long identityToken = Binder.clearCallingIdentity();
                try {
                    setActiveAdmin(who, true, userHandle);
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
            }
        }
    }

    protected void monitorFactoryReset(String component, String reason) {
        if (this.mMonitor == null || TextUtils.isEmpty(reason)) {
            HwLog.e(TAG, "monitorFactoryReset: Invalid parameter,mMonitor=" + this.mMonitor + ", reason=" + reason);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("component", component);
        bundle.putString("reason", reason);
        this.mMonitor.monitor(907400018, bundle);
    }

    protected void clearWipeDataFactoryLowlevel(String reason) {
        HwLog.d(TAG, "wipeData, reason=" + reason);
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(285212672);
        intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    public int setSystemLanguage(ComponentName who, Bundle bundle, int userHandle) {
        if (bundle == null || TextUtils.isEmpty(bundle.getString("locale"))) {
            throw new IllegalArgumentException("locale is empty.");
        }
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "Does not have mdm_device_manager permission.");
        synchronized (this) {
            if (who == null) {
                throw new IllegalArgumentException("ComponentName is null");
            }
            getHwActiveAdmin(who, userHandle);
            long callingId = Binder.clearCallingIdentity();
            try {
                Locale locale = Locale.forLanguageTag(bundle.getString("locale"));
                IActivityManager am = ActivityManagerNative.getDefault();
                Configuration config = am.getConfiguration();
                config.setLocale(locale);
                config.userSetLocale = true;
                am.updateConfiguration(config);
                System.putStringForUser(this.mContext.getContentResolver(), "system_locales", locale.toLanguageTag(), userHandle);
                BackupManager.dataChanged("com.android.providers.settings");
            } catch (Exception e) {
                Slog.w(TAG, "failed to set system language");
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
        return 1;
    }

    public void setDeviceOwnerApp(ComponentName admin, String ownerName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        enforceHwCrossUserPermission(userId);
        synchronized (this) {
            long ident = this.mInjector.binderClearCallingIdentity();
            try {
                this.mIsMDMDeviceOwnerAPI = true;
                super.setDeviceOwner(admin, ownerName, userId);
                this.mIsMDMDeviceOwnerAPI = false;
                this.mInjector.binderRestoreCallingIdentity(ident);
            } catch (Throwable th) {
                this.mIsMDMDeviceOwnerAPI = false;
                this.mInjector.binderRestoreCallingIdentity(ident);
            }
        }
    }

    public void clearDeviceOwnerApp(int userId) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        enforceHwCrossUserPermission(userId);
        synchronized (this) {
            try {
                ComponentName component = this.mOwners.getDeviceOwnerComponent();
                if (!this.mOwners.hasDeviceOwner() || component == null) {
                    throw new IllegalArgumentException("The device owner is not set up.");
                }
                this.mIsMDMDeviceOwnerAPI = true;
                super.clearDeviceOwner(component.getPackageName());
                this.mIsMDMDeviceOwnerAPI = false;
            } catch (Throwable th) {
                this.mIsMDMDeviceOwnerAPI = false;
            }
        }
    }

    public void turnOnMobiledata(ComponentName who, boolean on, int userHandle) {
        enforceHwCrossUserPermission(userHandle);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_NETWORK_MANAGER", "does not have MDM_NETWORK_MANAGER permission!");
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        synchronized (this) {
            getHwActiveAdmin(who, userHandle);
        }
        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        if (on) {
            try {
                phone.enableDataConnectivity();
                return;
            } catch (Exception e) {
                HwLog.e(TAG, "Can not calling the remote function to set data enabled!");
                return;
            }
        }
        phone.disableDataConnectivity();
    }

    public boolean setCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber, int userHandle) {
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        boolean z = this.mContext;
        z.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_KEYGUARD", "does not have keyguard MDM permission!");
        synchronized (this) {
            if (getActiveAdminUncheckedLocked(who, userHandle) == null) {
                throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
            }
            long token = Binder.clearCallingIdentity();
            try {
                z = new LockPatternUtilsEx(this.mContext).setExtendLockScreenPassword(password, phoneNumber, userHandle);
            } catch (Exception e) {
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return z;
    }

    public boolean clearCarrierLockScreenPassword(ComponentName who, String password, int userHandle) {
        boolean clearExtendLockScreenPassword;
        if (who == null) {
            throw new IllegalArgumentException("ComponentName is null");
        }
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_KEYGUARD", "does not have keyguard MDM permission!");
        synchronized (this) {
            if (getActiveAdminUncheckedLocked(who, userHandle) == null) {
                throw new SecurityException("No active admin owned by uid " + Binder.getCallingUid() + ", ComponentName:" + who);
            }
            long token = Binder.clearCallingIdentity();
            try {
                clearExtendLockScreenPassword = new LockPatternUtilsEx(this.mContext).clearExtendLockScreenPassword(password, userHandle);
            } catch (Exception e) {
                clearExtendLockScreenPassword = TAG;
                HwLog.e(clearExtendLockScreenPassword, "clear extended keyguard password error!");
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return clearExtendLockScreenPassword;
    }
}
