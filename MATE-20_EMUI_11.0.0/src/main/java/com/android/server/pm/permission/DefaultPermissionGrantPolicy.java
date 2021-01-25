package com.android.server.pm.permission;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.permission.PermissionManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DefaultPermissionGrantPolicy extends AbsDefaultPermissionGrantPolicy {
    private static final String ACTION_TRACK = "com.android.fitness.TRACK";
    private static final Set<String> ACTIVITY_RECOGNITION_PERMISSIONS = new ArraySet();
    private static final Set<String> ALWAYS_LOCATION_PERMISSIONS = new ArraySet();
    private static final String ATTR_FIXED = "fixed";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PACKAGE = "package";
    private static final String ATTR_WHITELISTED = "whitelisted";
    private static final String AUDIO_MIME_TYPE = "audio/mpeg";
    private static final Set<String> CALENDAR_PERMISSIONS = new ArraySet();
    private static final Set<String> CAMERA_PERMISSIONS = new ArraySet();
    private static final Set<String> CONTACTS_PERMISSIONS = new ArraySet();
    private static final boolean DEBUG = false;
    private static final int DEFAULT_INTENT_QUERY_FLAGS = 794624;
    private static final int DEFAULT_PACKAGE_INFO_QUERY_FLAGS = 536915968;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final Set<String> MICROPHONE_PERMISSIONS = new ArraySet();
    private static final int MSG_READ_DEFAULT_PERMISSION_EXCEPTIONS = 1;
    private static final Set<String> PHONE_PERMISSIONS = new ArraySet();
    private static final Set<String> SENSORS_PERMISSIONS = new ArraySet();
    private static final Set<String> SMS_PERMISSIONS = new ArraySet();
    private static final Set<String> STORAGE_PERMISSIONS = new ArraySet();
    private static final String TAG = "DefaultPermGrantPolicy";
    private static final String TAG_EXCEPTION = "exception";
    private static final String TAG_EXCEPTIONS = "exceptions";
    private static final String TAG_PERMISSION = "permission";
    private final Context mContext;
    @GuardedBy({"mLock"})
    private SparseIntArray mDefaultPermissionsGrantedUsers = new SparseIntArray();
    private PackageManagerInternal.PackagesProvider mDialerAppPackagesProvider;
    private ArrayMap<String, List<DefaultPermissionGrant>> mGrantExceptions;
    private final Handler mHandler;
    private PackageManagerInternal.PackagesProvider mLocationExtraPackagesProvider;
    private PackageManagerInternal.PackagesProvider mLocationPackagesProvider;
    private final Object mLock = new Object();
    private final PermissionManagerService mPermissionManager;
    private final PackageManagerInternal mServiceInternal;
    private PackageManagerInternal.PackagesProvider mSimCallManagerPackagesProvider;
    private PackageManagerInternal.PackagesProvider mSmsAppPackagesProvider;
    private PackageManagerInternal.SyncAdapterPackagesProvider mSyncAdapterPackagesProvider;
    private PackageManagerInternal.PackagesProvider mUseOpenWifiAppPackagesProvider;
    private PackageManagerInternal.PackagesProvider mVoiceInteractionPackagesProvider;

    static {
        PHONE_PERMISSIONS.add("android.permission.READ_PHONE_STATE");
        PHONE_PERMISSIONS.add("android.permission.CALL_PHONE");
        PHONE_PERMISSIONS.add("android.permission.READ_CALL_LOG");
        PHONE_PERMISSIONS.add("android.permission.WRITE_CALL_LOG");
        PHONE_PERMISSIONS.add("com.android.voicemail.permission.ADD_VOICEMAIL");
        PHONE_PERMISSIONS.add("android.permission.USE_SIP");
        PHONE_PERMISSIONS.add("android.permission.PROCESS_OUTGOING_CALLS");
        CONTACTS_PERMISSIONS.add("android.permission.READ_CONTACTS");
        CONTACTS_PERMISSIONS.add("android.permission.WRITE_CONTACTS");
        CONTACTS_PERMISSIONS.add("android.permission.GET_ACCOUNTS");
        ALWAYS_LOCATION_PERMISSIONS.add("android.permission.ACCESS_FINE_LOCATION");
        ALWAYS_LOCATION_PERMISSIONS.add("android.permission.ACCESS_COARSE_LOCATION");
        ALWAYS_LOCATION_PERMISSIONS.add("android.permission.ACCESS_BACKGROUND_LOCATION");
        ACTIVITY_RECOGNITION_PERMISSIONS.add("android.permission.ACTIVITY_RECOGNITION");
        CALENDAR_PERMISSIONS.add("android.permission.READ_CALENDAR");
        CALENDAR_PERMISSIONS.add("android.permission.WRITE_CALENDAR");
        SMS_PERMISSIONS.add("android.permission.SEND_SMS");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_SMS");
        SMS_PERMISSIONS.add("android.permission.READ_SMS");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_WAP_PUSH");
        SMS_PERMISSIONS.add("android.permission.RECEIVE_MMS");
        SMS_PERMISSIONS.add("android.permission.READ_CELL_BROADCASTS");
        MICROPHONE_PERMISSIONS.add("android.permission.RECORD_AUDIO");
        CAMERA_PERMISSIONS.add("android.permission.CAMERA");
        SENSORS_PERMISSIONS.add("android.permission.BODY_SENSORS");
        STORAGE_PERMISSIONS.add("android.permission.READ_EXTERNAL_STORAGE");
        STORAGE_PERMISSIONS.add("android.permission.WRITE_EXTERNAL_STORAGE");
        STORAGE_PERMISSIONS.add("android.permission.ACCESS_MEDIA_LOCATION");
    }

    public DefaultPermissionGrantPolicy(Context context, Looper looper, PermissionManagerService permissionManager) {
        this.mContext = context;
        this.mHandler = new Handler(looper) {
            /* class com.android.server.pm.permission.DefaultPermissionGrantPolicy.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    synchronized (DefaultPermissionGrantPolicy.this.mLock) {
                        if (DefaultPermissionGrantPolicy.this.mGrantExceptions == null) {
                            DefaultPermissionGrantPolicy.this.mGrantExceptions = DefaultPermissionGrantPolicy.this.readDefaultPermissionExceptionsLocked();
                        }
                    }
                }
            }
        };
        this.mPermissionManager = permissionManager;
        this.mServiceInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
    }

    public void setLocationPackagesProvider(PackageManagerInternal.PackagesProvider provider) {
        synchronized (this.mLock) {
            this.mLocationPackagesProvider = provider;
        }
    }

    public void setLocationExtraPackagesProvider(PackageManagerInternal.PackagesProvider provider) {
        synchronized (this.mLock) {
            this.mLocationExtraPackagesProvider = provider;
        }
    }

    public void setVoiceInteractionPackagesProvider(PackageManagerInternal.PackagesProvider provider) {
        synchronized (this.mLock) {
            this.mVoiceInteractionPackagesProvider = provider;
        }
    }

    public void setSmsAppPackagesProvider(PackageManagerInternal.PackagesProvider provider) {
        synchronized (this.mLock) {
            this.mSmsAppPackagesProvider = provider;
        }
    }

    public void setDialerAppPackagesProvider(PackageManagerInternal.PackagesProvider provider) {
        synchronized (this.mLock) {
            this.mDialerAppPackagesProvider = provider;
        }
    }

    public void setSimCallManagerPackagesProvider(PackageManagerInternal.PackagesProvider provider) {
        synchronized (this.mLock) {
            this.mSimCallManagerPackagesProvider = provider;
        }
    }

    public void setUseOpenWifiAppPackagesProvider(PackageManagerInternal.PackagesProvider provider) {
        synchronized (this.mLock) {
            this.mUseOpenWifiAppPackagesProvider = provider;
        }
    }

    public void setSyncAdapterPackagesProvider(PackageManagerInternal.SyncAdapterPackagesProvider provider) {
        synchronized (this.mLock) {
            this.mSyncAdapterPackagesProvider = provider;
        }
    }

    public boolean wereDefaultPermissionsGrantedSinceBoot(int userId) {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDefaultPermissionsGrantedUsers.indexOfKey(userId) >= 0;
        }
        return z;
    }

    public void grantDefaultPermissions(int userId) {
        grantPermissionsToSysComponentsAndPrivApps(userId);
        grantDefaultSystemHandlerPermissions(userId);
        grantDefaultPermissionExceptions(userId);
        removeSystemFixedStorageFlagForPhoneUid(userId);
        synchronized (this.mLock) {
            this.mDefaultPermissionsGrantedUsers.put(userId, userId);
        }
    }

    private void removeSystemFixedStorageFlagForPhoneUid(int userId) {
        Log.v(TAG, "start removeSystemFixedStorageFlagForPhoneUid userId = " + userId);
        for (PackageInfo pkg : this.mContext.getPackageManager().getInstalledPackagesAsUser(DEFAULT_PACKAGE_INFO_QUERY_FLAGS, userId)) {
            if (!(pkg == null || pkg.requestedPermissions == null || pkg.applicationInfo == null)) {
                if (pkg.applicationInfo.uid == 1001 || pkg.applicationInfo.uid == 5513) {
                    String[] strArr = pkg.requestedPermissions;
                    for (String permission : strArr) {
                        if (("android.permission.READ_EXTERNAL_STORAGE".equals(permission) || "android.permission.WRITE_EXTERNAL_STORAGE".equals(permission) || "android.permission.ACCESS_MEDIA_LOCATION".equals(permission)) && (this.mContext.getPackageManager().getPermissionFlags(permission, pkg.packageName, UserHandle.of(userId)) & 16) != 0) {
                            Log.v(TAG, "Removing system fixed " + pkg.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + permission);
                            this.mContext.getPackageManager().updatePermissionFlags(permission, pkg.packageName, 16, 0, UserHandle.of(userId));
                        }
                    }
                }
            }
        }
    }

    private void grantRuntimePermissionsForSystemPackage(int userId, PackageInfo pkg) {
        Set<String> permissions = new ArraySet<>();
        String[] strArr = pkg.requestedPermissions;
        for (String permission : strArr) {
            BasePermission bp = this.mPermissionManager.getPermission(permission);
            if (bp != null && bp.isRuntime()) {
                permissions.add(permission);
            }
        }
        if (!permissions.isEmpty()) {
            grantRuntimePermissions(pkg, permissions, true, userId);
        }
    }

    public void scheduleReadDefaultPermissionExceptions() {
        this.mHandler.sendEmptyMessage(1);
    }

    private void grantPermissionsToSysComponentsAndPrivApps(int userId) {
        Log.i(TAG, "Granting permissions to platform components for user " + userId);
        for (PackageInfo pkg : this.mContext.getPackageManager().getInstalledPackagesAsUser(DEFAULT_PACKAGE_INFO_QUERY_FLAGS, 0)) {
            if (pkg != null && isSysComponentOrPersistentPlatformSignedPrivApp(pkg) && doesPackageSupportRuntimePermissions(pkg) && !ArrayUtils.isEmpty(pkg.requestedPermissions)) {
                grantRuntimePermissionsForSystemPackage(userId, pkg);
            }
        }
    }

    @SafeVarargs
    private final void grantIgnoringSystemPackage(String packageName, int userId, Set<String>... permissionGroups) {
        grantPermissionsToPackage(packageName, userId, true, true, permissionGroups);
    }

    @SafeVarargs
    private final void grantSystemFixedPermissionsToSystemPackage(String packageName, int userId, Set<String>... permissionGroups) {
        grantPermissionsToSystemPackage(packageName, userId, true, permissionGroups);
    }

    @SafeVarargs
    private final void grantPermissionsToSystemPackage(String packageName, int userId, Set<String>... permissionGroups) {
        grantPermissionsToSystemPackage(packageName, userId, false, permissionGroups);
    }

    @SafeVarargs
    private final void grantPermissionsToSystemPackage(String packageName, int userId, boolean systemFixed, Set<String>... permissionGroups) {
        if (isSystemPackage(packageName)) {
            grantPermissionsToPackage(getSystemPackageInfo(packageName), userId, systemFixed, false, true, permissionGroups);
        }
    }

    @SafeVarargs
    public final void grantPermissionsToPackage(String packageName, int userId, boolean ignoreSystemPackage, boolean whitelistRestrictedPermissions, Set<String>... permissionGroups) {
        grantPermissionsToPackage(getPackageInfo(packageName), userId, false, ignoreSystemPackage, whitelistRestrictedPermissions, permissionGroups);
    }

    /* access modifiers changed from: protected */
    @SafeVarargs
    public final void grantPermissionsToPackage(String packageName, int userId, boolean systemFixed, boolean ignoreSystemPackage, boolean whitelistRestrictedPermissions, Set<String>... permissionGroups) {
        grantPermissionsToPackage(getPackageInfo(packageName), userId, systemFixed, ignoreSystemPackage, whitelistRestrictedPermissions, permissionGroups);
    }

    @SafeVarargs
    private final void grantPermissionsToPackage(PackageInfo packageInfo, int userId, boolean systemFixed, boolean ignoreSystemPackage, boolean whitelistRestrictedPermissions, Set<String>... permissionGroups) {
        if (packageInfo != null && doesPackageSupportRuntimePermissions(packageInfo)) {
            for (Set<String> permissionGroup : permissionGroups) {
                grantRuntimePermissions(packageInfo, permissionGroup, systemFixed, ignoreSystemPackage, whitelistRestrictedPermissions, userId);
            }
        }
    }

    public void grantCustSmsApplication(PackageParser.Package pkg, int userId) {
        if (pkg != null) {
            Slog.w(TAG, "grantCustSmsApplication pkg is not null, return");
            return;
        }
        String defaultApplication = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "sms_default_application", userId);
        if (!TextUtils.isEmpty(defaultApplication)) {
            Slog.w(TAG, "grantCustSmsApplication SMS_DEFAULT_APPLICATION setting has value" + defaultApplication + " , return");
            return;
        }
        String custDefaultSmsApp = SystemProperties.get("ro.config.default_sms_app", this.mContext.getResources().getString(17039973));
        if (TextUtils.isEmpty(custDefaultSmsApp)) {
            Slog.w(TAG, "grantCustSmsApplication custDefaultSmsApp is null, return");
            return;
        }
        Slog.w(TAG, "grantCustSmsApplication custDefaultSmsApp:" + custDefaultSmsApp);
        grantDefaultPermissionsToDefaultSystemSmsApp(custDefaultSmsApp, userId);
    }

    /* JADX INFO: Multiple debug info for r2v57 java.lang.String: [D('useOpenWifiPackageName' java.lang.String), D('dialerAppPackageNames' java.lang.String[])] */
    private void grantDefaultSystemHandlerPermissions(int userId) {
        PackageManagerInternal.PackagesProvider locationPackagesProvider;
        PackageManagerInternal.PackagesProvider locationExtraPackagesProvider;
        PackageManagerInternal.PackagesProvider voiceInteractionPackagesProvider;
        PackageManagerInternal.PackagesProvider smsAppPackagesProvider;
        PackageManagerInternal.PackagesProvider dialerAppPackagesProvider;
        PackageManagerInternal.PackagesProvider simCallManagerPackagesProvider;
        PackageManagerInternal.PackagesProvider useOpenWifiAppPackagesProvider;
        PackageManagerInternal.SyncAdapterPackagesProvider syncAdapterPackagesProvider;
        String browserPackage;
        int i;
        char c;
        int i2;
        char c2;
        int i3;
        Log.i(TAG, "Granting permissions to default platform handlers for user " + userId);
        synchronized (this.mLock) {
            locationPackagesProvider = this.mLocationPackagesProvider;
            locationExtraPackagesProvider = this.mLocationExtraPackagesProvider;
            voiceInteractionPackagesProvider = this.mVoiceInteractionPackagesProvider;
            smsAppPackagesProvider = this.mSmsAppPackagesProvider;
            dialerAppPackagesProvider = this.mDialerAppPackagesProvider;
            simCallManagerPackagesProvider = this.mSimCallManagerPackagesProvider;
            useOpenWifiAppPackagesProvider = this.mUseOpenWifiAppPackagesProvider;
            syncAdapterPackagesProvider = this.mSyncAdapterPackagesProvider;
        }
        String[] voiceInteractPackageNames = voiceInteractionPackagesProvider != null ? voiceInteractionPackagesProvider.getPackages(userId) : null;
        String[] locationPackageNames = locationPackagesProvider != null ? locationPackagesProvider.getPackages(userId) : null;
        String[] locationExtraPackageNames = locationExtraPackagesProvider != null ? locationExtraPackagesProvider.getPackages(userId) : null;
        String[] smsAppPackageNames = smsAppPackagesProvider != null ? smsAppPackagesProvider.getPackages(userId) : null;
        String[] dialerAppPackageNames = dialerAppPackagesProvider != null ? dialerAppPackagesProvider.getPackages(userId) : null;
        String[] simCallManagerPackageNames = simCallManagerPackagesProvider != null ? simCallManagerPackagesProvider.getPackages(userId) : null;
        String[] useOpenWifiAppPackageNames = useOpenWifiAppPackagesProvider != null ? useOpenWifiAppPackagesProvider.getPackages(userId) : null;
        String[] contactsSyncAdapterPackages = syncAdapterPackagesProvider != null ? syncAdapterPackagesProvider.getPackages("com.android.contacts", userId) : null;
        String[] calendarSyncAdapterPackages = syncAdapterPackagesProvider != null ? syncAdapterPackagesProvider.getPackages("com.android.calendar", userId) : null;
        grantSystemFixedPermissionsToSystemPackage(getKnownPackage(2, userId), userId, STORAGE_PERMISSIONS);
        String verifier = getKnownPackage(3, userId);
        grantSystemFixedPermissionsToSystemPackage(verifier, userId, STORAGE_PERMISSIONS);
        grantPermissionsToSystemPackage(verifier, userId, PHONE_PERMISSIONS, SMS_PERMISSIONS);
        grantPermissionsToSystemPackage(getKnownPackage(1, userId), userId, PHONE_PERMISSIONS, CONTACTS_PERMISSIONS, ALWAYS_LOCATION_PERMISSIONS, CAMERA_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage("android.media.action.IMAGE_CAPTURE", userId), userId, CAMERA_PERMISSIONS, MICROPHONE_PERMISSIONS, STORAGE_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage("android.provider.MediaStore.RECORD_SOUND", userId), userId, MICROPHONE_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage(getDefaultProviderAuthorityPackage("media", userId), userId, STORAGE_PERMISSIONS, PHONE_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage(getDefaultProviderAuthorityPackage("downloads", userId), userId, STORAGE_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage("android.intent.action.VIEW_DOWNLOADS", userId), userId, STORAGE_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage(getDefaultProviderAuthorityPackage("com.android.externalstorage.documents", userId), userId, STORAGE_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage("android.credentials.INSTALL", userId), userId, STORAGE_PERMISSIONS);
        if (dialerAppPackageNames == null) {
            grantDefaultPermissionsToDefaultSystemDialerApp(getDefaultSystemHandlerActivityPackage("android.intent.action.DIAL", userId), userId);
        } else {
            for (String dialerAppPackageName : dialerAppPackageNames) {
                grantDefaultPermissionsToDefaultSystemDialerApp(dialerAppPackageName, userId);
            }
        }
        if (simCallManagerPackageNames != null) {
            for (String simCallManagerPackageName : simCallManagerPackageNames) {
                grantDefaultPermissionsToDefaultSystemSimCallManager(simCallManagerPackageName, userId);
            }
        }
        if (useOpenWifiAppPackageNames != null) {
            int length = useOpenWifiAppPackageNames.length;
            int i4 = 0;
            while (i4 < length) {
                grantDefaultPermissionsToDefaultSystemUseOpenWifiApp(useOpenWifiAppPackageNames[i4], userId);
                i4++;
                dialerAppPackageNames = dialerAppPackageNames;
            }
        }
        if (smsAppPackageNames == null) {
            grantDefaultPermissionsToDefaultSystemSmsApp(getDefaultSystemHandlerActivityPackageForCategory("android.intent.category.APP_MESSAGING", userId), userId);
        } else {
            for (String smsPackage : smsAppPackageNames) {
                grantDefaultPermissionsToDefaultSystemSmsApp(smsPackage, userId);
            }
        }
        grantSystemFixedPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage("android.provider.Telephony.SMS_CB_RECEIVED", userId), userId, SMS_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerServicePackage("android.provider.Telephony.SMS_CARRIER_PROVISION", userId), userId, SMS_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackageForCategory("android.intent.category.APP_CALENDAR", userId), userId, CALENDAR_PERMISSIONS);
        String calendarProvider = getDefaultProviderAuthorityPackage("com.android.calendar", userId);
        grantPermissionsToSystemPackage(calendarProvider, userId, CONTACTS_PERMISSIONS, STORAGE_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage(calendarProvider, userId, CALENDAR_PERMISSIONS);
        grantPermissionToEachSystemPackage(getHeadlessSyncAdapterPackages(calendarSyncAdapterPackages, userId), userId, CALENDAR_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackageForCategory("android.intent.category.APP_CONTACTS", userId), userId, CONTACTS_PERMISSIONS, PHONE_PERMISSIONS);
        grantPermissionToEachSystemPackage(getHeadlessSyncAdapterPackages(contactsSyncAdapterPackages, userId), userId, CONTACTS_PERMISSIONS);
        String contactsProviderPackage = getDefaultProviderAuthorityPackage("com.android.contacts", userId);
        grantSystemFixedPermissionsToSystemPackage(contactsProviderPackage, userId, CONTACTS_PERMISSIONS, PHONE_PERMISSIONS);
        grantPermissionsToSystemPackage(contactsProviderPackage, userId, STORAGE_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage("android.app.action.PROVISION_MANAGED_DEVICE", userId), userId, CONTACTS_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackageForCategory("android.intent.category.APP_MAPS", userId), userId, ALWAYS_LOCATION_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackageForCategory("android.intent.category.APP_GALLERY", userId), userId, STORAGE_PERMISSIONS);
        String browserPackage2 = getKnownPackage(4, userId);
        if (browserPackage2 == null) {
            String browserPackage3 = getDefaultSystemHandlerActivityPackageForCategory("android.intent.category.APP_BROWSER", userId);
            browserPackage = !isSystemPackage(browserPackage3) ? null : browserPackage3;
        } else {
            browserPackage = browserPackage2;
        }
        grantPermissionsToPackage(browserPackage, userId, false, true, ALWAYS_LOCATION_PERMISSIONS);
        int i5 = 6;
        if (voiceInteractPackageNames != null) {
            int length2 = voiceInteractPackageNames.length;
            int i6 = 0;
            while (i6 < length2) {
                String voiceInteractPackageName = voiceInteractPackageNames[i6];
                Set<String>[] setArr = new Set[i5];
                setArr[0] = CONTACTS_PERMISSIONS;
                setArr[1] = CALENDAR_PERMISSIONS;
                setArr[2] = MICROPHONE_PERMISSIONS;
                setArr[3] = PHONE_PERMISSIONS;
                setArr[4] = SMS_PERMISSIONS;
                setArr[5] = ALWAYS_LOCATION_PERMISSIONS;
                grantPermissionsToSystemPackage(voiceInteractPackageName, userId, setArr);
                i6++;
                i5 = 6;
            }
            i = 2;
        } else {
            i = 2;
        }
        if (ActivityManager.isLowRamDeviceStatic()) {
            String defaultSystemHandlerActivityPackage = getDefaultSystemHandlerActivityPackage("android.search.action.GLOBAL_SEARCH", userId);
            Set<String>[] setArr2 = new Set[i];
            setArr2[0] = MICROPHONE_PERMISSIONS;
            setArr2[1] = ALWAYS_LOCATION_PERMISSIONS;
            grantPermissionsToSystemPackage(defaultSystemHandlerActivityPackage, userId, setArr2);
        }
        grantPermissionsToSystemPackage(getDefaultSystemHandlerServicePackage(new Intent("android.speech.RecognitionService").addCategory("android.intent.category.DEFAULT"), userId), userId, MICROPHONE_PERMISSIONS);
        if (locationPackageNames != null) {
            for (String packageName : locationPackageNames) {
                Set<String>[] setArr3 = new Set[8];
                setArr3[0] = CONTACTS_PERMISSIONS;
                setArr3[1] = CALENDAR_PERMISSIONS;
                setArr3[i] = MICROPHONE_PERMISSIONS;
                setArr3[3] = PHONE_PERMISSIONS;
                setArr3[4] = SMS_PERMISSIONS;
                setArr3[5] = CAMERA_PERMISSIONS;
                setArr3[6] = SENSORS_PERMISSIONS;
                setArr3[7] = STORAGE_PERMISSIONS;
                grantPermissionsToSystemPackage(packageName, userId, setArr3);
                Set<String>[] setArr4 = new Set[i];
                setArr4[0] = ALWAYS_LOCATION_PERMISSIONS;
                setArr4[1] = ACTIVITY_RECOGNITION_PERMISSIONS;
                grantSystemFixedPermissionsToSystemPackage(packageName, userId, setArr4);
            }
        }
        if (locationExtraPackageNames != null) {
            for (String packageName2 : locationExtraPackageNames) {
                grantPermissionsToSystemPackage(packageName2, userId, ALWAYS_LOCATION_PERMISSIONS);
            }
        }
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage(new Intent("android.intent.action.VIEW").addCategory("android.intent.category.DEFAULT").setDataAndType(Uri.fromFile(new File("foo.mp3")), AUDIO_MIME_TYPE), userId), userId, STORAGE_PERMISSIONS);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.LAUNCHER_APP"), userId), userId, ALWAYS_LOCATION_PERMISSIONS);
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch", 0)) {
            String wearPackage = getDefaultSystemHandlerActivityPackageForCategory("android.intent.category.HOME_MAIN", userId);
            grantPermissionsToSystemPackage(wearPackage, userId, CONTACTS_PERMISSIONS, MICROPHONE_PERMISSIONS, ALWAYS_LOCATION_PERMISSIONS);
            c = 0;
            grantSystemFixedPermissionsToSystemPackage(wearPackage, userId, PHONE_PERMISSIONS);
            i2 = 1;
            grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage(ACTION_TRACK, userId), userId, SENSORS_PERMISSIONS, ALWAYS_LOCATION_PERMISSIONS);
        } else {
            c = 0;
            i2 = 1;
        }
        Set<String>[] setArr5 = new Set[i2];
        setArr5[c] = ALWAYS_LOCATION_PERMISSIONS;
        grantSystemFixedPermissionsToSystemPackage("com.android.printspooler", userId, setArr5);
        String defaultSystemHandlerActivityPackage2 = getDefaultSystemHandlerActivityPackage("android.telephony.action.EMERGENCY_ASSISTANCE", userId);
        Set<String>[] setArr6 = new Set[2];
        setArr6[c] = CONTACTS_PERMISSIONS;
        setArr6[1] = PHONE_PERMISSIONS;
        grantSystemFixedPermissionsToSystemPackage(defaultSystemHandlerActivityPackage2, userId, setArr6);
        grantPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage(new Intent("android.intent.action.VIEW").setType("vnd.android.cursor.item/ndef_msg"), userId), userId, CONTACTS_PERMISSIONS, PHONE_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage("android.os.storage.action.MANAGE_STORAGE", userId), userId, STORAGE_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage("com.android.companiondevicemanager", userId, ALWAYS_LOCATION_PERMISSIONS);
        grantSystemFixedPermissionsToSystemPackage(getDefaultSystemHandlerActivityPackage("android.intent.action.RINGTONE_PICKER", userId), userId, STORAGE_PERMISSIONS);
        String textClassifierPackageName = this.mContext.getPackageManager().getSystemTextClassifierPackageName();
        if (!TextUtils.isEmpty(textClassifierPackageName)) {
            grantPermissionsToSystemPackage(textClassifierPackageName, userId, PHONE_PERMISSIONS, SMS_PERMISSIONS, CALENDAR_PERMISSIONS, ALWAYS_LOCATION_PERMISSIONS, CONTACTS_PERMISSIONS);
        }
        String attentionServicePackageName = this.mContext.getPackageManager().getAttentionServicePackageName();
        if (!TextUtils.isEmpty(attentionServicePackageName)) {
            i3 = 1;
            c2 = 0;
            grantPermissionsToSystemPackage(attentionServicePackageName, userId, CAMERA_PERMISSIONS);
        } else {
            i3 = 1;
            c2 = 0;
        }
        Set<String>[] setArr7 = new Set[i3];
        setArr7[c2] = STORAGE_PERMISSIONS;
        grantSystemFixedPermissionsToSystemPackage(UserBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, userId, setArr7);
        String systemCaptionsServicePackageName = this.mContext.getPackageManager().getSystemCaptionsServicePackageName();
        if (!TextUtils.isEmpty(systemCaptionsServicePackageName)) {
            grantPermissionsToSystemPackage(systemCaptionsServicePackageName, userId, MICROPHONE_PERMISSIONS);
        }
    }

    private String getDefaultSystemHandlerActivityPackageForCategory(String category, int userId) {
        return getDefaultSystemHandlerActivityPackage(new Intent("android.intent.action.MAIN").addCategory(category), userId);
    }

    @SafeVarargs
    private final void grantPermissionToEachSystemPackage(ArrayList<String> packages, int userId, Set<String>... permissions) {
        if (packages != null) {
            int count = packages.size();
            for (int i = 0; i < count; i++) {
                grantPermissionsToSystemPackage(packages.get(i), userId, permissions);
            }
        }
    }

    private String getKnownPackage(int knownPkgId, int userId) {
        return this.mServiceInternal.getKnownPackageName(knownPkgId, userId);
    }

    private void grantDefaultPermissionsToDefaultSystemDialerApp(String dialerPackage, int userId) {
        if (dialerPackage != null) {
            if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch", 0)) {
                grantSystemFixedPermissionsToSystemPackage(dialerPackage, userId, PHONE_PERMISSIONS);
            } else {
                grantPermissionsToSystemPackage(dialerPackage, userId, PHONE_PERMISSIONS);
            }
            grantPermissionsToSystemPackage(dialerPackage, userId, CONTACTS_PERMISSIONS, SMS_PERMISSIONS, MICROPHONE_PERMISSIONS);
        }
    }

    private void grantDefaultPermissionsToDefaultSystemSmsApp(String smsPackage, int userId) {
        grantPermissionsToSystemPackage(smsPackage, userId, PHONE_PERMISSIONS, CONTACTS_PERMISSIONS, SMS_PERMISSIONS, STORAGE_PERMISSIONS, MICROPHONE_PERMISSIONS, CAMERA_PERMISSIONS);
    }

    private void grantDefaultPermissionsToDefaultSystemUseOpenWifiApp(String useOpenWifiPackage, int userId) {
        grantPermissionsToSystemPackage(useOpenWifiPackage, userId, ALWAYS_LOCATION_PERMISSIONS);
    }

    public void grantDefaultPermissionsToDefaultUseOpenWifiApp(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default Use Open WiFi app for user:" + userId);
        grantIgnoringSystemPackage(packageName, userId, ALWAYS_LOCATION_PERMISSIONS);
    }

    public void grantDefaultPermissionsToDefaultSimCallManager(String packageName, int userId) {
        if (packageName != null) {
            Log.i(TAG, "Granting permissions to sim call manager for user:" + userId);
            grantPermissionsToPackage(packageName, userId, false, true, PHONE_PERMISSIONS, MICROPHONE_PERMISSIONS);
        }
    }

    private void grantDefaultPermissionsToDefaultSystemSimCallManager(String packageName, int userId) {
        if (isSystemPackage(packageName)) {
            grantDefaultPermissionsToDefaultSimCallManager(packageName, userId);
        }
    }

    public void grantDefaultPermissionsToEnabledCarrierApps(String[] packageNames, int userId) {
        Log.i(TAG, "Granting permissions to enabled carrier apps for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                grantPermissionsToSystemPackage(packageName, userId, PHONE_PERMISSIONS, ALWAYS_LOCATION_PERMISSIONS, SMS_PERMISSIONS);
            }
        }
    }

    public void grantDefaultPermissionsToEnabledImsServices(String[] packageNames, int userId) {
        Log.i(TAG, "Granting permissions to enabled ImsServices for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                grantPermissionsToSystemPackage(packageName, userId, PHONE_PERMISSIONS, MICROPHONE_PERMISSIONS, ALWAYS_LOCATION_PERMISSIONS, CAMERA_PERMISSIONS, CONTACTS_PERMISSIONS);
            }
        }
    }

    public void grantDefaultPermissionsToEnabledTelephonyDataServices(String[] packageNames, int userId) {
        Log.i(TAG, "Granting permissions to enabled data services for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                grantSystemFixedPermissionsToSystemPackage(packageName, userId, PHONE_PERMISSIONS, ALWAYS_LOCATION_PERMISSIONS);
            }
        }
    }

    public void revokeDefaultPermissionsFromDisabledTelephonyDataServices(String[] packageNames, int userId) {
        Log.i(TAG, "Revoking permissions from disabled data services for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                PackageInfo pkg = getSystemPackageInfo(packageName);
                if (isSystemPackage(pkg) && doesPackageSupportRuntimePermissions(pkg)) {
                    revokeRuntimePermissions(packageName, PHONE_PERMISSIONS, true, userId);
                    revokeRuntimePermissions(packageName, ALWAYS_LOCATION_PERMISSIONS, true, userId);
                }
            }
        }
    }

    public void grantDefaultPermissionsToActiveLuiApp(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to active LUI app for user:" + userId);
        grantSystemFixedPermissionsToSystemPackage(packageName, userId, CAMERA_PERMISSIONS);
    }

    public void revokeDefaultPermissionsFromLuiApps(String[] packageNames, int userId) {
        Log.i(TAG, "Revoke permissions from LUI apps for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                PackageInfo pkg = getSystemPackageInfo(packageName);
                if (isSystemPackage(pkg) && doesPackageSupportRuntimePermissions(pkg)) {
                    revokeRuntimePermissions(packageName, CAMERA_PERMISSIONS, true, userId);
                }
            }
        }
    }

    public void grantDefaultPermissionsToDefaultBrowser(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default browser for user:" + userId);
        grantPermissionsToSystemPackage(packageName, userId, ALWAYS_LOCATION_PERMISSIONS);
    }

    private String getDefaultSystemHandlerActivityPackage(String intentAction, int userId) {
        return getDefaultSystemHandlerActivityPackage(new Intent(intentAction), userId);
    }

    private String getDefaultSystemHandlerActivityPackage(Intent intent, int userId) {
        ResolveInfo handler = this.mContext.getPackageManager().resolveActivityAsUser(intent, DEFAULT_INTENT_QUERY_FLAGS, userId);
        if (handler == null || handler.activityInfo == null || this.mServiceInternal.isResolveActivityComponent(handler.activityInfo)) {
            return null;
        }
        String packageName = handler.activityInfo.packageName;
        if (isSystemPackage(packageName)) {
            return packageName;
        }
        return null;
    }

    private String getDefaultSystemHandlerServicePackage(String intentAction, int userId) {
        return getDefaultSystemHandlerServicePackage(new Intent(intentAction), userId);
    }

    private String getDefaultSystemHandlerServicePackage(Intent intent, int userId) {
        List<ResolveInfo> handlers = this.mContext.getPackageManager().queryIntentServicesAsUser(intent, DEFAULT_INTENT_QUERY_FLAGS, userId);
        if (handlers == null) {
            return null;
        }
        int handlerCount = handlers.size();
        for (int i = 0; i < handlerCount; i++) {
            String handlerPackage = handlers.get(i).serviceInfo.packageName;
            if (isSystemPackage(handlerPackage)) {
                return handlerPackage;
            }
        }
        return null;
    }

    private ArrayList<String> getHeadlessSyncAdapterPackages(String[] syncAdapterPackageNames, int userId) {
        ArrayList<String> syncAdapterPackages = new ArrayList<>();
        Intent homeIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER");
        for (String syncAdapterPackageName : syncAdapterPackageNames) {
            homeIntent.setPackage(syncAdapterPackageName);
            if (this.mContext.getPackageManager().resolveActivityAsUser(homeIntent, DEFAULT_INTENT_QUERY_FLAGS, userId) == null && isSystemPackage(syncAdapterPackageName)) {
                syncAdapterPackages.add(syncAdapterPackageName);
            }
        }
        return syncAdapterPackages;
    }

    private String getDefaultProviderAuthorityPackage(String authority, int userId) {
        ProviderInfo provider = this.mContext.getPackageManager().resolveContentProviderAsUser(authority, DEFAULT_INTENT_QUERY_FLAGS, userId);
        if (provider != null) {
            return provider.packageName;
        }
        return null;
    }

    private PackageParser.Package getPackage(String packageName) {
        return this.mServiceInternal.getPackage(packageName);
    }

    private boolean isSystemPackage(String packageName) {
        return isSystemPackage(getPackageInfo(packageName));
    }

    private boolean isSystemPackage(PackageInfo pkg) {
        if (pkg != null && pkg.applicationInfo.isSystemApp() && !isSysComponentOrPersistentPlatformSignedPrivApp(pkg)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public PackageParser.Package getSystemPackage(String packageName) {
        PackageInfo pkg = getPackageInfo(packageName);
        if (pkg == null || !isSystemPackage(packageName) || isSysComponentOrPersistentPlatformSignedPrivApp(pkg)) {
            return null;
        }
        return getPackage(packageName);
    }

    /* access modifiers changed from: protected */
    public void grantRuntimePermissions(PackageInfo pkg, Set<String> permissions, boolean systemFixed, int userId) {
        grantRuntimePermissions(pkg, permissions, systemFixed, false, true, userId);
    }

    private void revokeRuntimePermissions(String packageName, Set<String> permissions, boolean systemFixed, int userId) {
        PackageInfo pkg = getSystemPackageInfo(packageName);
        if (!ArrayUtils.isEmpty(pkg.requestedPermissions)) {
            Set<String> revokablePermissions = new ArraySet<>(Arrays.asList(pkg.requestedPermissions));
            for (String permission : permissions) {
                if (revokablePermissions.contains(permission)) {
                    UserHandle user = UserHandle.of(userId);
                    int flags = this.mContext.getPackageManager().getPermissionFlags(permission, packageName, user);
                    if ((flags & 32) != 0 && (flags & 4) == 0) {
                        if ((flags & 16) == 0 || systemFixed) {
                            this.mContext.getPackageManager().revokeRuntimePermission(packageName, permission, user);
                            this.mContext.getPackageManager().updatePermissionFlags(permission, packageName, 32, 0, user);
                        }
                    }
                }
            }
        }
    }

    private boolean isFixedOrUserSet(int flags) {
        return (flags & 23) != 0;
    }

    private String getBackgroundPermission(String permission) {
        try {
            return this.mContext.getPackageManager().getPermissionInfo(permission, 0).backgroundPermission;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /* JADX INFO: Multiple debug info for r11v5 'permission'  java.lang.String: [D('requestedByNonSystemPackage' java.lang.String[]), D('permission' java.lang.String)] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00e6  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0103  */
    private void grantRuntimePermissions(PackageInfo pkg, Set<String> permissionsWithoutSplits, boolean systemFixed, boolean ignoreSystemPackage, boolean whitelistRestrictedPermissions, int userId) {
        String[] requestedPermissions;
        Set<String> grantablePermissions;
        int numRequestedPermissions;
        int requestedPermissionNum;
        PackageManager pm;
        Set<String> grantablePermissions2;
        String[] requestedByNonSystemPackage;
        List<PermissionManager.SplitPermissionInfo> splitPermissions;
        int numSplitPerms;
        int numRequestedPermissions2;
        String[] sortedRequestedPermissions;
        int flags;
        String permission;
        String permission2;
        int mode;
        PackageInfo disabledPkg;
        int newFlags;
        UserHandle user = UserHandle.of(userId);
        if (pkg != null) {
            String[] requestedPermissions2 = pkg.requestedPermissions;
            if (!ArrayUtils.isEmpty(requestedPermissions2)) {
                String[] requestedByNonSystemPackage2 = getPackageInfo(pkg.packageName).requestedPermissions;
                int size = requestedPermissions2.length;
                for (int i = 0; i < size; i++) {
                    if (!ArrayUtils.contains(requestedByNonSystemPackage2, requestedPermissions2[i])) {
                        requestedPermissions2[i] = null;
                    }
                }
                String[] requestedPermissions3 = (String[]) ArrayUtils.filterNotNull(requestedPermissions2, $$Lambda$DefaultPermissionGrantPolicy$SHfHTWKpfBf_vZtWArmFlNBI8k.INSTANCE);
                try {
                    PackageManager pm2 = this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user).getPackageManager();
                    ArraySet<String> permissions = new ArraySet<>(permissionsWithoutSplits);
                    ApplicationInfo applicationInfo = pkg.applicationInfo;
                    int newFlags2 = 32;
                    if (systemFixed) {
                        newFlags2 = 32 | 16;
                    }
                    List<PermissionManager.SplitPermissionInfo> splitPermissions2 = ((PermissionManager) this.mContext.getSystemService(PermissionManager.class)).getSplitPermissions();
                    int numSplitPerms2 = splitPermissions2.size();
                    int splitPermNum = 0;
                    while (splitPermNum < numSplitPerms2) {
                        PermissionManager.SplitPermissionInfo splitPerm = splitPermissions2.get(splitPermNum);
                        if (applicationInfo != null) {
                            newFlags = newFlags2;
                            if (applicationInfo.targetSdkVersion < splitPerm.getTargetSdk() && permissionsWithoutSplits.contains(splitPerm.getSplitPermission())) {
                                permissions.addAll(splitPerm.getNewPermissions());
                            }
                        } else {
                            newFlags = newFlags2;
                        }
                        splitPermNum++;
                        newFlags2 = newFlags;
                    }
                    int newFlags3 = newFlags2;
                    if (!ignoreSystemPackage && applicationInfo != null && applicationInfo.isUpdatedSystemApp() && (disabledPkg = getSystemPackageInfo(this.mServiceInternal.getDisabledSystemPackageName(pkg.packageName))) != null) {
                        if (ArrayUtils.isEmpty(disabledPkg.requestedPermissions)) {
                            return;
                        }
                        if (!Arrays.equals(requestedPermissions3, disabledPkg.requestedPermissions)) {
                            grantablePermissions = new ArraySet<>(Arrays.asList(requestedPermissions3));
                            requestedPermissions = disabledPkg.requestedPermissions;
                            numRequestedPermissions = requestedPermissions.length;
                            String[] sortedRequestedPermissions2 = new String[numRequestedPermissions];
                            int numOther = 0;
                            int numForeground = 0;
                            for (String permission3 : requestedPermissions) {
                                if (getBackgroundPermission(permission3) != null) {
                                    sortedRequestedPermissions2[numForeground] = permission3;
                                    numForeground++;
                                } else {
                                    sortedRequestedPermissions2[(numRequestedPermissions - 1) - numOther] = permission3;
                                    numOther++;
                                }
                            }
                            requestedPermissionNum = 0;
                            while (requestedPermissionNum < numRequestedPermissions) {
                                String permission4 = requestedPermissions[requestedPermissionNum];
                                if (grantablePermissions != null && !grantablePermissions.contains(permission4)) {
                                    pm = pm2;
                                    sortedRequestedPermissions = sortedRequestedPermissions2;
                                    numRequestedPermissions2 = numRequestedPermissions;
                                    grantablePermissions2 = grantablePermissions;
                                    numSplitPerms = numSplitPerms2;
                                    splitPermissions = splitPermissions2;
                                    requestedByNonSystemPackage = requestedByNonSystemPackage2;
                                } else if (permissions.contains(permission4)) {
                                    sortedRequestedPermissions = sortedRequestedPermissions2;
                                    numRequestedPermissions2 = numRequestedPermissions;
                                    int flags2 = this.mContext.getPackageManager().getPermissionFlags(permission4, pkg.packageName, user);
                                    boolean changingGrantForSystemFixed = systemFixed && (flags2 & 16) != 0;
                                    if (isFixedOrUserSet(flags2) && !ignoreSystemPackage && !changingGrantForSystemFixed) {
                                        pm = pm2;
                                        flags = flags2;
                                        grantablePermissions2 = grantablePermissions;
                                        numSplitPerms = numSplitPerms2;
                                        splitPermissions = splitPermissions2;
                                        requestedByNonSystemPackage = requestedByNonSystemPackage2;
                                        permission = permission4;
                                    } else if ((flags2 & 4) != 0) {
                                        pm = pm2;
                                        grantablePermissions2 = grantablePermissions;
                                        numSplitPerms = numSplitPerms2;
                                        splitPermissions = splitPermissions2;
                                        requestedByNonSystemPackage = requestedByNonSystemPackage2;
                                    } else {
                                        newFlags3 |= flags2 & 14336;
                                        if (!whitelistRestrictedPermissions || !isPermissionRestricted(permission4)) {
                                            permission2 = permission4;
                                            flags = flags2;
                                            grantablePermissions2 = grantablePermissions;
                                            numSplitPerms = numSplitPerms2;
                                            splitPermissions = splitPermissions2;
                                        } else {
                                            flags = flags2;
                                            permission2 = permission4;
                                            grantablePermissions2 = grantablePermissions;
                                            numSplitPerms = numSplitPerms2;
                                            splitPermissions = splitPermissions2;
                                            this.mContext.getPackageManager().updatePermissionFlags(permission2, pkg.packageName, 4096, 4096, user);
                                        }
                                        if (changingGrantForSystemFixed) {
                                            this.mContext.getPackageManager().updatePermissionFlags(permission2, pkg.packageName, flags, flags & -17, user);
                                        }
                                        if (pm2.checkPermission(permission2, pkg.packageName) != 0) {
                                            this.mContext.getPackageManager().grantRuntimePermission(pkg.packageName, permission2, user);
                                        }
                                        requestedByNonSystemPackage = requestedByNonSystemPackage2;
                                        permission = permission2;
                                        this.mContext.getPackageManager().updatePermissionFlags(permission2, pkg.packageName, newFlags3, newFlags3, user);
                                        int uid = UserHandle.getUid(userId, UserHandle.getAppId(pkg.applicationInfo.uid));
                                        List<String> fgPerms = this.mPermissionManager.getBackgroundPermissions().get(permission);
                                        if (fgPerms != null) {
                                            int numFgPerms = fgPerms.size();
                                            int fgPermNum = 0;
                                            while (true) {
                                                if (fgPermNum >= numFgPerms) {
                                                    break;
                                                }
                                                String fgPerm = fgPerms.get(fgPermNum);
                                                if (pm2.checkPermission(fgPerm, pkg.packageName) == 0) {
                                                    ((AppOpsManager) this.mContext.getSystemService(AppOpsManager.class)).setUidMode(AppOpsManager.permissionToOp(fgPerm), uid, 0);
                                                    break;
                                                } else {
                                                    fgPermNum++;
                                                    fgPerms = fgPerms;
                                                }
                                            }
                                        }
                                        String bgPerm = getBackgroundPermission(permission);
                                        String op = AppOpsManager.permissionToOp(permission);
                                        if (bgPerm != null) {
                                            if (pm2.checkPermission(bgPerm, pkg.packageName) == 0) {
                                                mode = 0;
                                            } else {
                                                mode = 4;
                                            }
                                            pm = pm2;
                                            ((AppOpsManager) this.mContext.getSystemService(AppOpsManager.class)).setUidMode(op, uid, mode);
                                        } else if (op != null) {
                                            ((AppOpsManager) this.mContext.getSystemService(AppOpsManager.class)).setUidMode(op, uid, 0);
                                            pm = pm2;
                                        } else {
                                            pm = pm2;
                                        }
                                    }
                                    if ((flags & 32) != 0 && (flags & 16) != 0 && !systemFixed) {
                                        this.mContext.getPackageManager().updatePermissionFlags(permission, pkg.packageName, 16, 0, user);
                                    }
                                } else {
                                    pm = pm2;
                                    sortedRequestedPermissions = sortedRequestedPermissions2;
                                    numRequestedPermissions2 = numRequestedPermissions;
                                    grantablePermissions2 = grantablePermissions;
                                    numSplitPerms = numSplitPerms2;
                                    splitPermissions = splitPermissions2;
                                    requestedByNonSystemPackage = requestedByNonSystemPackage2;
                                }
                                requestedPermissionNum++;
                                sortedRequestedPermissions2 = sortedRequestedPermissions;
                                numRequestedPermissions = numRequestedPermissions2;
                                numSplitPerms2 = numSplitPerms;
                                splitPermissions2 = splitPermissions;
                                requestedByNonSystemPackage2 = requestedByNonSystemPackage;
                                grantablePermissions = grantablePermissions2;
                                pm2 = pm;
                            }
                        }
                    }
                    requestedPermissions = requestedPermissions3;
                    grantablePermissions = null;
                    numRequestedPermissions = requestedPermissions.length;
                    String[] sortedRequestedPermissions22 = new String[numRequestedPermissions];
                    int numOther2 = 0;
                    int numForeground2 = 0;
                    while (i < numRequestedPermissions) {
                    }
                    requestedPermissionNum = 0;
                    while (requestedPermissionNum < numRequestedPermissions) {
                    }
                } catch (PackageManager.NameNotFoundException doesNotHappen) {
                    throw new IllegalStateException(doesNotHappen);
                }
            }
        }
    }

    static /* synthetic */ String[] lambda$grantRuntimePermissions$0(int x$0) {
        return new String[x$0];
    }

    private PackageInfo getSystemPackageInfo(String pkg) {
        return getPackageInfo(pkg, DumpState.DUMP_DEXOPT);
    }

    private PackageInfo getPackageInfo(String pkg) {
        return getPackageInfo(pkg, 0);
    }

    private PackageInfo getPackageInfo(String pkg, int extraFlags) {
        if (pkg == null) {
            return null;
        }
        try {
            return this.mContext.getPackageManager().getPackageInfo(pkg, DEFAULT_PACKAGE_INFO_QUERY_FLAGS | extraFlags);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "PackageNot found: " + pkg, e);
            return null;
        }
    }

    private boolean isSysComponentOrPersistentPlatformSignedPrivApp(PackageInfo pkg) {
        if (UserHandle.getAppId(pkg.applicationInfo.uid) < 10000) {
            return true;
        }
        if (!pkg.applicationInfo.isPrivilegedApp()) {
            return false;
        }
        PackageInfo disabledPkg = getSystemPackageInfo(this.mServiceInternal.getDisabledSystemPackageName(pkg.applicationInfo.packageName));
        if (disabledPkg != null) {
            ApplicationInfo disabledPackageAppInfo = disabledPkg.applicationInfo;
            if (disabledPackageAppInfo != null && (disabledPackageAppInfo.flags & 8) == 0) {
                return false;
            }
        } else if ((pkg.applicationInfo.flags & 8) == 0) {
            return false;
        }
        return this.mServiceInternal.isPlatformSigned(pkg.packageName);
    }

    private void grantDefaultPermissionExceptions(int userId) {
        Set<String> permissions;
        this.mHandler.removeMessages(1);
        synchronized (this.mLock) {
            if (this.mGrantExceptions == null) {
                this.mGrantExceptions = readDefaultPermissionExceptionsLocked();
            }
        }
        int exceptionCount = this.mGrantExceptions.size();
        Set<String> permissions2 = null;
        for (int i = 0; i < exceptionCount; i++) {
            PackageInfo pkg = getSystemPackageInfo(this.mGrantExceptions.keyAt(i));
            List<DefaultPermissionGrant> permissionGrants = this.mGrantExceptions.valueAt(i);
            int permissionGrantCount = permissionGrants.size();
            for (int j = 0; j < permissionGrantCount; j++) {
                DefaultPermissionGrant permissionGrant = permissionGrants.get(j);
                if (!isPermissionDangerous(permissionGrant.name)) {
                    Log.w(TAG, "Ignoring permission " + permissionGrant.name + " which isn't dangerous");
                } else {
                    if (permissions2 == null) {
                        permissions = new ArraySet<>();
                    } else {
                        permissions2.clear();
                        permissions = permissions2;
                    }
                    permissions.add(permissionGrant.name);
                    grantRuntimePermissions(pkg, permissions, permissionGrant.fixed, permissionGrant.whitelisted, true, userId);
                    permissions2 = permissions;
                }
            }
        }
    }

    private File[] getDefaultPermissionFiles() {
        ArrayList<File> ret = new ArrayList<>();
        File dir = new File(Environment.getRootDirectory(), "etc/default-permissions");
        if (dir.isDirectory() && dir.canRead()) {
            Collections.addAll(ret, dir.listFiles());
        }
        File dir2 = new File(Environment.getVendorDirectory(), "etc/default-permissions");
        if (dir2.isDirectory() && dir2.canRead()) {
            Collections.addAll(ret, dir2.listFiles());
        }
        File dir3 = new File(Environment.getOdmDirectory(), "etc/default-permissions");
        if (dir3.isDirectory() && dir3.canRead()) {
            Collections.addAll(ret, dir3.listFiles());
        }
        File dir4 = new File(Environment.getProductDirectory(), "etc/default-permissions");
        if (dir4.isDirectory() && dir4.canRead()) {
            Collections.addAll(ret, dir4.listFiles());
        }
        File dir5 = new File(Environment.getProductServicesDirectory(), "etc/default-permissions");
        if (dir5.isDirectory() && dir5.canRead()) {
            Collections.addAll(ret, dir5.listFiles());
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.embedded", 0)) {
            File dir6 = new File(Environment.getOemDirectory(), "etc/default-permissions");
            if (dir6.isDirectory() && dir6.canRead()) {
                Collections.addAll(ret, dir6.listFiles());
            }
        }
        Iterator it = HwCfgFilePolicy.getCfgFileList("default-permissions", 0).iterator();
        while (it.hasNext()) {
            File d = (File) it.next();
            if (d.isDirectory() && d.canRead()) {
                File[] listFiles = d.listFiles();
                for (File f : listFiles) {
                    if (!ret.contains(f)) {
                        ret.add(f);
                    }
                }
            }
        }
        if (ret.isEmpty()) {
            return null;
        }
        return (File[]) ret.toArray(new File[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0087, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008c, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x008d, code lost:
        r7.addSuppressed(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0090, code lost:
        throw r8;
     */
    private ArrayMap<String, List<DefaultPermissionGrant>> readDefaultPermissionExceptionsLocked() {
        File[] files = getDefaultPermissionFiles();
        if (files == null) {
            return new ArrayMap<>(0);
        }
        ArrayMap<String, List<DefaultPermissionGrant>> grantExceptions = new ArrayMap<>();
        for (File file : files) {
            if (!file.getPath().endsWith(".xml")) {
                Slog.i(TAG, "Non-xml file " + file + " in " + file.getParent() + " directory, ignoring");
            } else if (!file.canRead()) {
                Slog.w(TAG, "Default permissions file " + file + " cannot be read");
            } else {
                InputStream str = new BufferedInputStream(new FileInputStream(file));
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(str, null);
                parse(parser, grantExceptions);
                try {
                    str.close();
                } catch (IOException | XmlPullParserException e) {
                    Slog.w(TAG, "Error reading default permissions file " + file, e);
                }
            }
        }
        return grantExceptions;
    }

    private void parse(XmlPullParser parser, Map<String, List<DefaultPermissionGrant>> outGrantExceptions) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (TAG_EXCEPTIONS.equals(parser.getName())) {
                    parseExceptions(parser, outGrantExceptions);
                } else {
                    Log.e(TAG, "Unknown tag " + parser.getName());
                }
            }
        }
    }

    private void parseExceptions(XmlPullParser parser, Map<String, List<DefaultPermissionGrant>> outGrantExceptions) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (TAG_EXCEPTION.equals(parser.getName())) {
                    String packageName = parser.getAttributeValue(null, "package");
                    List<DefaultPermissionGrant> packageExceptions = outGrantExceptions.get(packageName);
                    if (packageExceptions == null) {
                        PackageInfo packageInfo = getSystemPackageInfo(packageName);
                        if (packageInfo == null) {
                            Log.w(TAG, "No such package:" + packageName);
                            XmlUtils.skipCurrentTag(parser);
                        } else if (!isSystemPackage(packageInfo)) {
                            Log.w(TAG, "Unknown system package:" + packageName);
                            XmlUtils.skipCurrentTag(parser);
                        } else if (!doesPackageSupportRuntimePermissions(packageInfo)) {
                            Log.w(TAG, "Skipping non supporting runtime permissions package:" + packageName);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            packageExceptions = new ArrayList();
                            outGrantExceptions.put(packageName, packageExceptions);
                        }
                    }
                    parsePermission(parser, packageExceptions);
                } else {
                    Log.e(TAG, "Unknown tag " + parser.getName() + "under <exceptions>");
                }
            }
        }
    }

    private void parsePermission(XmlPullParser parser, List<DefaultPermissionGrant> outPackageExceptions) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (TAG_PERMISSION.contains(parser.getName())) {
                    String name = parser.getAttributeValue(null, "name");
                    if (name == null) {
                        Log.w(TAG, "Mandatory name attribute missing for permission tag");
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        outPackageExceptions.add(new DefaultPermissionGrant(name, XmlUtils.readBooleanAttribute(parser, ATTR_FIXED), XmlUtils.readBooleanAttribute(parser, ATTR_WHITELISTED)));
                    }
                } else {
                    Log.e(TAG, "Unknown tag " + parser.getName() + "under <exception>");
                }
            }
        }
    }

    private static boolean doesPackageSupportRuntimePermissions(PackageInfo pkg) {
        return pkg.applicationInfo != null && pkg.applicationInfo.targetSdkVersion > 22;
    }

    private boolean isPermissionRestricted(String name) {
        try {
            return this.mContext.getPackageManager().getPermissionInfo(name, 0).isRestricted();
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isPermissionDangerous(String name) {
        try {
            return this.mContext.getPackageManager().getPermissionInfo(name, 0).getProtection() == 1;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static final class DefaultPermissionGrant {
        final boolean fixed;
        final String name;
        final boolean whitelisted;

        public DefaultPermissionGrant(String name2, boolean fixed2, boolean whitelisted2) {
            this.name = name2;
            this.fixed = fixed2;
            this.whitelisted = whitelisted2;
        }
    }
}
