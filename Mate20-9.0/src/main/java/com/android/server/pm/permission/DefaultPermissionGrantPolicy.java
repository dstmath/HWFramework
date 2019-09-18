package com.android.server.pm.permission;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.backup.BackupManagerService;
import com.android.server.pm.PackageManagerService;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DefaultPermissionGrantPolicy extends AbsDefaultPermissionGrantPolicy {
    private static final String ACTION_TRACK = "com.android.fitness.TRACK";
    private static final String ATTR_FIXED = "fixed";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PACKAGE = "package";
    private static final String AUDIO_MIME_TYPE = "audio/mpeg";
    private static final Set<String> CALENDAR_PERMISSIONS = new ArraySet();
    private static final Set<String> CAMERA_PERMISSIONS = new ArraySet();
    private static final Set<String> COARSE_LOCATION_PERMISSIONS = new ArraySet();
    private static final Set<String> CONTACTS_PERMISSIONS = new ArraySet();
    private static final boolean DEBUG = false;
    private static final int DEFAULT_FLAGS = 794624;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final Set<String> LOCATION_PERMISSIONS = new ArraySet();
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
    private PackageManagerInternal.PackagesProvider mDialerAppPackagesProvider;
    /* access modifiers changed from: private */
    public ArrayMap<String, List<DefaultPermissionGrant>> mGrantExceptions;
    private final Handler mHandler;
    private PackageManagerInternal.PackagesProvider mLocationPackagesProvider;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final DefaultPermissionGrantedCallback mPermissionGrantedCallback;
    private final PermissionManagerService mPermissionManager;
    private final PackageManagerInternal mServiceInternal;
    private PackageManagerInternal.PackagesProvider mSimCallManagerPackagesProvider;
    private PackageManagerInternal.PackagesProvider mSmsAppPackagesProvider;
    private PackageManagerInternal.SyncAdapterPackagesProvider mSyncAdapterPackagesProvider;
    private PackageManagerInternal.PackagesProvider mUseOpenWifiAppPackagesProvider;
    private PackageManagerInternal.PackagesProvider mVoiceInteractionPackagesProvider;

    private static final class DefaultPermissionGrant {
        final boolean fixed;
        final String name;

        public DefaultPermissionGrant(String name2, boolean fixed2) {
            this.name = name2;
            this.fixed = fixed2;
        }
    }

    public interface DefaultPermissionGrantedCallback {
        void onDefaultRuntimePermissionsGranted(int i);
    }

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
        LOCATION_PERMISSIONS.add("android.permission.ACCESS_FINE_LOCATION");
        LOCATION_PERMISSIONS.add("android.permission.ACCESS_COARSE_LOCATION");
        COARSE_LOCATION_PERMISSIONS.add("android.permission.ACCESS_COARSE_LOCATION");
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
    }

    public DefaultPermissionGrantPolicy(Context context, Looper looper, DefaultPermissionGrantedCallback callback, PermissionManagerService permissionManager) {
        this.mContext = context;
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    synchronized (DefaultPermissionGrantPolicy.this.mLock) {
                        if (DefaultPermissionGrantPolicy.this.mGrantExceptions == null) {
                            ArrayMap unused = DefaultPermissionGrantPolicy.this.mGrantExceptions = DefaultPermissionGrantPolicy.this.readDefaultPermissionExceptionsLocked();
                        }
                    }
                }
            }
        };
        this.mPermissionGrantedCallback = callback;
        this.mPermissionManager = permissionManager;
        this.mServiceInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
    }

    public void setLocationPackagesProvider(PackageManagerInternal.PackagesProvider provider) {
        synchronized (this.mLock) {
            this.mLocationPackagesProvider = provider;
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

    public void grantDefaultPermissions(int userId) {
        grantPermissionsToSysComponentsAndPrivApps(userId);
        grantDefaultSystemHandlerPermissions(userId);
        grantDefaultPermissionExceptions(userId);
    }

    private void grantRuntimePermissionsForPackage(int userId, PackageParser.Package pkg) {
        Set<String> permissions = new ArraySet<>();
        Iterator it = pkg.requestedPermissions.iterator();
        while (it.hasNext()) {
            String permission = (String) it.next();
            BasePermission bp = this.mPermissionManager.getPermission(permission);
            if (bp != null && bp.isRuntime()) {
                permissions.add(permission);
            }
        }
        if (!permissions.isEmpty()) {
            grantRuntimePermissions(pkg, permissions, true, userId);
        }
    }

    private void grantAllRuntimePermissions(int userId) {
        Log.i(TAG, "Granting all runtime permissions for user " + userId);
        for (String packageName : this.mServiceInternal.getPackageList().getPackageNames()) {
            PackageParser.Package pkg = this.mServiceInternal.getPackage(packageName);
            if (pkg != null) {
                grantRuntimePermissionsForPackage(userId, pkg);
            }
        }
    }

    public void scheduleReadDefaultPermissionExceptions() {
        this.mHandler.sendEmptyMessage(1);
    }

    private void grantPermissionsToSysComponentsAndPrivApps(int userId) {
        Log.i(TAG, "Granting permissions to platform components for user " + userId);
        for (String packageName : this.mServiceInternal.getPackageList().getPackageNames()) {
            PackageParser.Package pkg = this.mServiceInternal.getPackage(packageName);
            if (pkg != null && isSysComponentOrPersistentPlatformSignedPrivApp(pkg) && doesPackageSupportRuntimePermissions(pkg) && !pkg.requestedPermissions.isEmpty()) {
                grantRuntimePermissionsForPackage(userId, pkg);
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
        String custDefaultSmsApp = SystemProperties.get("ro.config.default_sms_app", this.mContext.getResources().getString(17039923));
        if (TextUtils.isEmpty(custDefaultSmsApp)) {
            Slog.w(TAG, "grantCustSmsApplication custDefaultSmsApp is null, return");
            return;
        }
        PackageParser.Package smsPackage = getSystemPackage(custDefaultSmsApp);
        if (smsPackage != null) {
            Slog.w(TAG, "grantCustSmsApplication smsPackage:" + smsPackage);
            grantDefaultPermissionsToDefaultSystemSmsApp(smsPackage, userId);
        } else {
            Slog.w(TAG, "grantCustSmsApplication smsPackage is null");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:301:0x0704  */
    /* JADX WARNING: Removed duplicated region for block: B:304:0x070e  */
    /* JADX WARNING: Removed duplicated region for block: B:338:? A[RETURN, SYNTHETIC] */
    private void grantDefaultSystemHandlerPermissions(int userId) {
        PackageManagerInternal.PackagesProvider locationPackagesProvider;
        PackageManagerInternal.PackagesProvider voiceInteractionPackagesProvider;
        PackageManagerInternal.PackagesProvider smsAppPackagesProvider;
        PackageManagerInternal.PackagesProvider dialerAppPackagesProvider;
        PackageManagerInternal.PackagesProvider simCallManagerPackagesProvider;
        PackageManagerInternal.PackagesProvider useOpenWifiAppPackagesProvider;
        PackageManagerInternal.SyncAdapterPackagesProvider syncAdapterPackagesProvider;
        String[] calendarSyncAdapterPackages;
        boolean z;
        boolean z2;
        PackageParser.Package browserPackage;
        int i;
        PackageParser.Package sharedStorageBackupPackage;
        List<PackageParser.Package> contactsSyncAdapters;
        PackageParser.Package contactsPackage;
        List<PackageParser.Package> calendarSyncAdapters;
        int i2 = userId;
        Log.i(TAG, "Granting permissions to default platform handlers for user " + i2);
        synchronized (this.mLock) {
            locationPackagesProvider = this.mLocationPackagesProvider;
            voiceInteractionPackagesProvider = this.mVoiceInteractionPackagesProvider;
            smsAppPackagesProvider = this.mSmsAppPackagesProvider;
            dialerAppPackagesProvider = this.mDialerAppPackagesProvider;
            simCallManagerPackagesProvider = this.mSimCallManagerPackagesProvider;
            useOpenWifiAppPackagesProvider = this.mUseOpenWifiAppPackagesProvider;
            syncAdapterPackagesProvider = this.mSyncAdapterPackagesProvider;
        }
        String[] voiceInteractPackageNames = voiceInteractionPackagesProvider != null ? voiceInteractionPackagesProvider.getPackages(i2) : null;
        String[] locationPackageNames = locationPackagesProvider != null ? locationPackagesProvider.getPackages(i2) : null;
        String[] smsAppPackageNames = smsAppPackagesProvider != null ? smsAppPackagesProvider.getPackages(i2) : null;
        String[] dialerAppPackageNames = dialerAppPackagesProvider != null ? dialerAppPackagesProvider.getPackages(i2) : null;
        String[] simCallManagerPackageNames = simCallManagerPackagesProvider != null ? simCallManagerPackagesProvider.getPackages(i2) : null;
        String[] useOpenWifiAppPackageNames = useOpenWifiAppPackagesProvider != null ? useOpenWifiAppPackagesProvider.getPackages(i2) : null;
        String[] contactsSyncAdapterPackages = syncAdapterPackagesProvider != null ? syncAdapterPackagesProvider.getPackages("com.android.contacts", i2) : null;
        if (syncAdapterPackagesProvider != null) {
            PackageManagerInternal.PackagesProvider packagesProvider = locationPackagesProvider;
            calendarSyncAdapterPackages = syncAdapterPackagesProvider.getPackages("com.android.calendar", i2);
        } else {
            calendarSyncAdapterPackages = null;
        }
        PackageManagerInternal.PackagesProvider packagesProvider2 = voiceInteractionPackagesProvider;
        PackageManagerInternal.PackagesProvider packagesProvider3 = smsAppPackagesProvider;
        String installerPackageName = this.mServiceInternal.getKnownPackageName(2, i2);
        PackageParser.Package installerPackage = getSystemPackage(installerPackageName);
        String str = installerPackageName;
        if (installerPackage == null || !doesPackageSupportRuntimePermissions(installerPackage)) {
        } else {
            PackageManagerInternal.PackagesProvider packagesProvider4 = dialerAppPackagesProvider;
            grantRuntimePermissions(installerPackage, STORAGE_PERMISSIONS, true, i2);
        }
        String verifierPackageName = this.mServiceInternal.getKnownPackageName(3, i2);
        PackageParser.Package verifierPackage = getSystemPackage(verifierPackageName);
        String str2 = verifierPackageName;
        if (verifierPackage == null || !doesPackageSupportRuntimePermissions(verifierPackage)) {
        } else {
            PackageParser.Package packageR = installerPackage;
            grantRuntimePermissions(verifierPackage, STORAGE_PERMISSIONS, true, i2);
            grantRuntimePermissions(verifierPackage, PHONE_PERMISSIONS, false, i2);
            grantRuntimePermissions(verifierPackage, SMS_PERMISSIONS, false, i2);
        }
        String setupWizardPackageName = this.mServiceInternal.getKnownPackageName(1, i2);
        PackageParser.Package setupPackage = getSystemPackage(setupWizardPackageName);
        if (setupPackage == null || !doesPackageSupportRuntimePermissions(setupPackage)) {
        } else {
            String str3 = setupWizardPackageName;
            grantRuntimePermissions(setupPackage, PHONE_PERMISSIONS, i2);
            grantRuntimePermissions(setupPackage, CONTACTS_PERMISSIONS, i2);
            grantRuntimePermissions(setupPackage, LOCATION_PERMISSIONS, i2);
            grantRuntimePermissions(setupPackage, CAMERA_PERMISSIONS, i2);
        }
        PackageParser.Package packageR2 = setupPackage;
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        PackageParser.Package cameraPackage = getDefaultSystemHandlerActivityPackage(cameraIntent, i2);
        if (cameraPackage == null || !doesPackageSupportRuntimePermissions(cameraPackage)) {
        } else {
            Intent intent = cameraIntent;
            grantRuntimePermissions(cameraPackage, CAMERA_PERMISSIONS, i2);
            grantRuntimePermissions(cameraPackage, MICROPHONE_PERMISSIONS, i2);
            grantRuntimePermissions(cameraPackage, STORAGE_PERMISSIONS, i2);
        }
        PackageParser.Package mediaStorePackage = getDefaultProviderAuthorityPackage("media", i2);
        if (mediaStorePackage != null) {
            PackageParser.Package packageR3 = cameraPackage;
            PackageParser.Package packageR4 = verifierPackage;
            z = true;
            grantRuntimePermissions(mediaStorePackage, STORAGE_PERMISSIONS, true, i2);
            grantRuntimePermissions(mediaStorePackage, PHONE_PERMISSIONS, true, i2);
        } else {
            PackageParser.Package packageR5 = verifierPackage;
            z = true;
        }
        PackageParser.Package downloadsPackage = getDefaultProviderAuthorityPackage("downloads", i2);
        if (downloadsPackage != null) {
            PackageParser.Package packageR6 = mediaStorePackage;
            grantRuntimePermissions(downloadsPackage, STORAGE_PERMISSIONS, z, i2);
        }
        Intent downloadsUiIntent = new Intent("android.intent.action.VIEW_DOWNLOADS");
        PackageParser.Package downloadsUiPackage = getDefaultSystemHandlerActivityPackage(downloadsUiIntent, i2);
        if (downloadsUiPackage == null || !doesPackageSupportRuntimePermissions(downloadsUiPackage)) {
            PackageParser.Package packageR7 = downloadsPackage;
            z2 = true;
        } else {
            Intent intent2 = downloadsUiIntent;
            PackageParser.Package packageR8 = downloadsPackage;
            z2 = true;
            grantRuntimePermissions(downloadsUiPackage, STORAGE_PERMISSIONS, true, i2);
        }
        PackageParser.Package storagePackage = getDefaultProviderAuthorityPackage("com.android.externalstorage.documents", i2);
        if (storagePackage != null) {
            PackageParser.Package packageR9 = downloadsUiPackage;
            grantRuntimePermissions(storagePackage, STORAGE_PERMISSIONS, z2, i2);
        }
        PackageParser.Package containerPackage = getSystemPackage(PackageManagerService.DEFAULT_CONTAINER_PACKAGE);
        if (containerPackage != null) {
            PackageParser.Package packageR10 = storagePackage;
            grantRuntimePermissions(containerPackage, STORAGE_PERMISSIONS, z2, i2);
        }
        Intent certInstallerIntent = new Intent("android.credentials.INSTALL");
        PackageParser.Package certInstallerPackage = getDefaultSystemHandlerActivityPackage(certInstallerIntent, i2);
        if (certInstallerPackage == null || !doesPackageSupportRuntimePermissions(certInstallerPackage)) {
            PackageParser.Package packageR11 = containerPackage;
        } else {
            Intent intent3 = certInstallerIntent;
            PackageParser.Package packageR12 = containerPackage;
            grantRuntimePermissions(certInstallerPackage, STORAGE_PERMISSIONS, true, i2);
        }
        if (dialerAppPackageNames == null) {
            PackageParser.Package dialerPackage = getDefaultSystemHandlerActivityPackage(new Intent("android.intent.action.DIAL"), i2);
            if (dialerPackage != null) {
                grantDefaultPermissionsToDefaultSystemDialerApp(dialerPackage, i2);
            }
            PackageParser.Package packageR13 = certInstallerPackage;
        } else {
            int length = dialerAppPackageNames.length;
            int i3 = 0;
            while (i3 < length) {
                int i4 = length;
                PackageParser.Package certInstallerPackage2 = certInstallerPackage;
                PackageParser.Package certInstallerPackage3 = getSystemPackage(dialerAppPackageNames[i3]);
                if (certInstallerPackage3 != null) {
                    grantDefaultPermissionsToDefaultSystemDialerApp(certInstallerPackage3, i2);
                }
                i3++;
                length = i4;
                certInstallerPackage = certInstallerPackage2;
            }
        }
        if (simCallManagerPackageNames != null) {
            int length2 = simCallManagerPackageNames.length;
            int i5 = 0;
            while (i5 < length2) {
                int i6 = length2;
                PackageParser.Package simCallManagerPackage = getSystemPackage(simCallManagerPackageNames[i5]);
                if (simCallManagerPackage != null) {
                    grantDefaultPermissionsToDefaultSimCallManager(simCallManagerPackage, i2);
                }
                i5++;
                length2 = i6;
            }
        }
        if (useOpenWifiAppPackageNames != null) {
            int length3 = useOpenWifiAppPackageNames.length;
            int i7 = 0;
            while (i7 < length3) {
                int i8 = length3;
                PackageParser.Package useOpenWifiPackage = getSystemPackage(useOpenWifiAppPackageNames[i7]);
                if (useOpenWifiPackage != null) {
                    grantDefaultPermissionsToDefaultSystemUseOpenWifiApp(useOpenWifiPackage, i2);
                }
                i7++;
                length3 = i8;
            }
        }
        if (smsAppPackageNames == null) {
            Intent smsIntent = new Intent("android.intent.action.MAIN");
            smsIntent.addCategory("android.intent.category.APP_MESSAGING");
            PackageParser.Package smsPackage = getDefaultSystemHandlerActivityPackage(smsIntent, i2);
            if (smsPackage != null) {
                grantDefaultPermissionsToDefaultSystemSmsApp(smsPackage, i2);
            }
            grantCustSmsApplication(smsPackage, i2);
        } else {
            int length4 = smsAppPackageNames.length;
            int i9 = 0;
            while (i9 < length4) {
                int i10 = length4;
                PackageParser.Package smsPackage2 = getSystemPackage(smsAppPackageNames[i9]);
                if (smsPackage2 != null) {
                    grantDefaultPermissionsToDefaultSystemSmsApp(smsPackage2, i2);
                }
                i9++;
                length4 = i10;
            }
        }
        Intent cbrIntent = new Intent("android.provider.Telephony.SMS_CB_RECEIVED");
        PackageParser.Package cbrPackage = getDefaultSystemHandlerActivityPackage(cbrIntent, i2);
        if (cbrPackage != null && doesPackageSupportRuntimePermissions(cbrPackage)) {
            grantRuntimePermissions(cbrPackage, SMS_PERMISSIONS, i2);
        }
        Intent intent4 = cbrIntent;
        Intent carrierProvIntent = new Intent("android.provider.Telephony.SMS_CARRIER_PROVISION");
        PackageParser.Package carrierProvPackage = getDefaultSystemHandlerServicePackage(carrierProvIntent, i2);
        if (carrierProvPackage == null || !doesPackageSupportRuntimePermissions(carrierProvPackage)) {
            PackageParser.Package packageR14 = cbrPackage;
        } else {
            Intent intent5 = carrierProvIntent;
            PackageParser.Package packageR15 = cbrPackage;
            grantRuntimePermissions(carrierProvPackage, SMS_PERMISSIONS, false, i2);
        }
        Intent calendarIntent = new Intent("android.intent.action.MAIN");
        calendarIntent.addCategory("android.intent.category.APP_CALENDAR");
        PackageParser.Package calendarPackage = getDefaultSystemHandlerActivityPackage(calendarIntent, i2);
        if (calendarPackage == null || !doesPackageSupportRuntimePermissions(calendarPackage)) {
        } else {
            Intent intent6 = calendarIntent;
            grantRuntimePermissions(calendarPackage, CALENDAR_PERMISSIONS, i2);
            grantRuntimePermissions(calendarPackage, CONTACTS_PERMISSIONS, i2);
        }
        PackageParser.Package calendarProviderPackage = getDefaultProviderAuthorityPackage("com.android.calendar", i2);
        if (calendarProviderPackage != null) {
            PackageParser.Package packageR16 = calendarPackage;
            grantRuntimePermissions(calendarProviderPackage, CONTACTS_PERMISSIONS, i2);
            PackageParser.Package packageR17 = carrierProvPackage;
            grantRuntimePermissions(calendarProviderPackage, CALENDAR_PERMISSIONS, true, i2);
            grantRuntimePermissions(calendarProviderPackage, STORAGE_PERMISSIONS, i2);
        } else {
            PackageParser.Package packageR18 = carrierProvPackage;
        }
        List<PackageParser.Package> calendarSyncAdapters2 = getHeadlessSyncAdapterPackages(calendarSyncAdapterPackages, i2);
        int calendarSyncAdapterCount = calendarSyncAdapters2.size();
        int i11 = 0;
        while (true) {
            String[] calendarSyncAdapterPackages2 = calendarSyncAdapterPackages;
            int i12 = i11;
            if (i12 >= calendarSyncAdapterCount) {
                break;
            }
            PackageParser.Package calendarProviderPackage2 = calendarProviderPackage;
            PackageParser.Package calendarSyncAdapter = calendarSyncAdapters2.get(i12);
            if (doesPackageSupportRuntimePermissions(calendarSyncAdapter)) {
                calendarSyncAdapters = calendarSyncAdapters2;
                grantRuntimePermissions(calendarSyncAdapter, CALENDAR_PERMISSIONS, i2);
            } else {
                calendarSyncAdapters = calendarSyncAdapters2;
            }
            i11 = i12 + 1;
            calendarSyncAdapterPackages = calendarSyncAdapterPackages2;
            calendarProviderPackage = calendarProviderPackage2;
            calendarSyncAdapters2 = calendarSyncAdapters;
        }
        List<PackageParser.Package> list = calendarSyncAdapters2;
        Intent contactsIntent = new Intent("android.intent.action.MAIN");
        contactsIntent.addCategory("android.intent.category.APP_CONTACTS");
        PackageParser.Package contactsPackage2 = getDefaultSystemHandlerActivityPackage(contactsIntent, i2);
        if (contactsPackage2 != null && doesPackageSupportRuntimePermissions(contactsPackage2)) {
            grantRuntimePermissions(contactsPackage2, CONTACTS_PERMISSIONS, i2);
            grantRuntimePermissions(contactsPackage2, PHONE_PERMISSIONS, i2);
        }
        List<PackageParser.Package> contactsSyncAdapters2 = getHeadlessSyncAdapterPackages(contactsSyncAdapterPackages, i2);
        Intent intent7 = contactsIntent;
        int contactsSyncAdapterCount = contactsSyncAdapters2.size();
        int i13 = 0;
        while (true) {
            String[] contactsSyncAdapterPackages2 = contactsSyncAdapterPackages;
            int i14 = i13;
            if (i14 >= contactsSyncAdapterCount) {
                break;
            }
            int contactsSyncAdapterCount2 = contactsSyncAdapterCount;
            PackageParser.Package contactsSyncAdapter = contactsSyncAdapters2.get(i14);
            if (doesPackageSupportRuntimePermissions(contactsSyncAdapter)) {
                contactsPackage = contactsPackage2;
                grantRuntimePermissions(contactsSyncAdapter, CONTACTS_PERMISSIONS, i2);
            } else {
                contactsPackage = contactsPackage2;
            }
            i13 = i14 + 1;
            contactsSyncAdapterPackages = contactsSyncAdapterPackages2;
            contactsSyncAdapterCount = contactsSyncAdapterCount2;
            contactsPackage2 = contactsPackage;
        }
        PackageParser.Package packageR19 = contactsPackage2;
        PackageParser.Package contactsProviderPackage = getDefaultProviderAuthorityPackage("com.android.contacts", i2);
        if (contactsProviderPackage != null) {
            grantRuntimePermissions(contactsProviderPackage, CONTACTS_PERMISSIONS, true, i2);
            grantRuntimePermissions(contactsProviderPackage, PHONE_PERMISSIONS, true, i2);
            grantRuntimePermissions(contactsProviderPackage, STORAGE_PERMISSIONS, i2);
        }
        Intent deviceProvisionIntent = new Intent("android.app.action.PROVISION_MANAGED_DEVICE");
        PackageParser.Package deviceProvisionPackage = getDefaultSystemHandlerActivityPackage(deviceProvisionIntent, i2);
        if (deviceProvisionPackage == null || !doesPackageSupportRuntimePermissions(deviceProvisionPackage)) {
        } else {
            PackageParser.Package packageR20 = contactsProviderPackage;
            grantRuntimePermissions(deviceProvisionPackage, CONTACTS_PERMISSIONS, i2);
        }
        Intent intent8 = deviceProvisionIntent;
        Intent mapsIntent = new Intent("android.intent.action.MAIN");
        mapsIntent.addCategory("android.intent.category.APP_MAPS");
        PackageParser.Package mapsPackage = getDefaultSystemHandlerActivityPackage(mapsIntent, i2);
        if (mapsPackage == null || !doesPackageSupportRuntimePermissions(mapsPackage)) {
        } else {
            Intent intent9 = mapsIntent;
            grantRuntimePermissions(mapsPackage, LOCATION_PERMISSIONS, i2);
        }
        PackageParser.Package packageR21 = mapsPackage;
        Intent galleryIntent = new Intent("android.intent.action.MAIN");
        galleryIntent.addCategory("android.intent.category.APP_GALLERY");
        PackageParser.Package galleryPackage = getDefaultSystemHandlerActivityPackage(galleryIntent, i2);
        if (galleryPackage == null || !doesPackageSupportRuntimePermissions(galleryPackage)) {
        } else {
            Intent intent10 = galleryIntent;
            grantRuntimePermissions(galleryPackage, STORAGE_PERMISSIONS, i2);
        }
        PackageParser.Package packageR22 = galleryPackage;
        Intent emailIntent = new Intent("android.intent.action.MAIN");
        emailIntent.addCategory("android.intent.category.APP_EMAIL");
        PackageParser.Package emailPackage = getDefaultSystemHandlerActivityPackage(emailIntent, i2);
        if (emailPackage == null || !doesPackageSupportRuntimePermissions(emailPackage)) {
        } else {
            Intent intent11 = emailIntent;
            grantRuntimePermissions(emailPackage, CONTACTS_PERMISSIONS, i2);
            grantRuntimePermissions(emailPackage, CALENDAR_PERMISSIONS, i2);
        }
        PackageParser.Package browserPackage2 = null;
        PackageParser.Package packageR23 = emailPackage;
        String defaultBrowserPackage = this.mServiceInternal.getKnownPackageName(4, i2);
        if (defaultBrowserPackage != null) {
            browserPackage2 = getPackage(defaultBrowserPackage);
        }
        if (browserPackage2 == null) {
            String str4 = defaultBrowserPackage;
            Intent browserIntent = new Intent("android.intent.action.MAIN");
            browserIntent.addCategory("android.intent.category.APP_BROWSER");
            browserPackage = getDefaultSystemHandlerActivityPackage(browserIntent, i2);
        } else {
            browserPackage = browserPackage2;
        }
        if (browserPackage != null && doesPackageSupportRuntimePermissions(browserPackage)) {
            grantRuntimePermissions(browserPackage, LOCATION_PERMISSIONS, i2);
        }
        if (voiceInteractPackageNames != null) {
            int length5 = voiceInteractPackageNames.length;
            PackageParser.Package packageR24 = browserPackage;
            int i15 = 0;
            while (i15 < length5) {
                int i16 = length5;
                String voiceInteractPackageName = voiceInteractPackageNames[i15];
                PackageParser.Package deviceProvisionPackage2 = deviceProvisionPackage;
                PackageParser.Package voiceInteractPackage = getSystemPackage(voiceInteractPackageName);
                if (voiceInteractPackage != null && doesPackageSupportRuntimePermissions(voiceInteractPackage)) {
                    String str5 = voiceInteractPackageName;
                    grantRuntimePermissions(voiceInteractPackage, CONTACTS_PERMISSIONS, i2);
                    grantRuntimePermissions(voiceInteractPackage, CALENDAR_PERMISSIONS, i2);
                    grantRuntimePermissions(voiceInteractPackage, MICROPHONE_PERMISSIONS, i2);
                    grantRuntimePermissions(voiceInteractPackage, PHONE_PERMISSIONS, i2);
                    grantRuntimePermissions(voiceInteractPackage, SMS_PERMISSIONS, i2);
                    grantRuntimePermissions(voiceInteractPackage, LOCATION_PERMISSIONS, i2);
                }
                i15++;
                length5 = i16;
                deviceProvisionPackage = deviceProvisionPackage2;
            }
        } else {
            PackageParser.Package packageR25 = deviceProvisionPackage;
        }
        if (ActivityManager.isLowRamDeviceStatic()) {
            Intent globalSearchIntent = new Intent("android.search.action.GLOBAL_SEARCH");
            PackageParser.Package globalSearchPickerPackage = getDefaultSystemHandlerActivityPackage(globalSearchIntent, i2);
            if (globalSearchPickerPackage != null && doesPackageSupportRuntimePermissions(globalSearchPickerPackage)) {
                Intent intent12 = globalSearchIntent;
                grantRuntimePermissions(globalSearchPickerPackage, MICROPHONE_PERMISSIONS, false, i2);
                grantRuntimePermissions(globalSearchPickerPackage, LOCATION_PERMISSIONS, false, i2);
            }
        }
        Intent voiceRecoIntent = new Intent("android.speech.RecognitionService");
        voiceRecoIntent.addCategory("android.intent.category.DEFAULT");
        PackageParser.Package voiceRecoPackage = getDefaultSystemHandlerServicePackage(voiceRecoIntent, i2);
        if (voiceRecoPackage != null && doesPackageSupportRuntimePermissions(voiceRecoPackage)) {
            grantRuntimePermissions(voiceRecoPackage, MICROPHONE_PERMISSIONS, i2);
        }
        if (locationPackageNames != null) {
            int length6 = locationPackageNames.length;
            Intent intent13 = voiceRecoIntent;
            int i17 = 0;
            while (i17 < length6) {
                PackageParser.Package voiceRecoPackage2 = voiceRecoPackage;
                String packageName = locationPackageNames[i17];
                int i18 = length6;
                PackageParser.Package locationPackage = getSystemPackage(packageName);
                if (locationPackage == null || !doesPackageSupportRuntimePermissions(locationPackage)) {
                    contactsSyncAdapters = contactsSyncAdapters2;
                } else {
                    String str6 = packageName;
                    grantRuntimePermissions(locationPackage, CONTACTS_PERMISSIONS, i2);
                    grantRuntimePermissions(locationPackage, CALENDAR_PERMISSIONS, i2);
                    grantRuntimePermissions(locationPackage, MICROPHONE_PERMISSIONS, i2);
                    grantRuntimePermissions(locationPackage, PHONE_PERMISSIONS, i2);
                    grantRuntimePermissions(locationPackage, SMS_PERMISSIONS, i2);
                    contactsSyncAdapters = contactsSyncAdapters2;
                    grantRuntimePermissions(locationPackage, LOCATION_PERMISSIONS, true, i2);
                    grantRuntimePermissions(locationPackage, CAMERA_PERMISSIONS, i2);
                    grantRuntimePermissions(locationPackage, SENSORS_PERMISSIONS, i2);
                    grantRuntimePermissions(locationPackage, STORAGE_PERMISSIONS, i2);
                }
                i17++;
                voiceRecoPackage = voiceRecoPackage2;
                length6 = i18;
                contactsSyncAdapters2 = contactsSyncAdapters;
            }
            List<PackageParser.Package> list2 = contactsSyncAdapters2;
        } else {
            PackageParser.Package packageR26 = voiceRecoPackage;
            List<PackageParser.Package> list3 = contactsSyncAdapters2;
        }
        Intent musicIntent = new Intent("android.intent.action.VIEW");
        musicIntent.addCategory("android.intent.category.DEFAULT");
        musicIntent.setDataAndType(Uri.fromFile(new File("foo.mp3")), AUDIO_MIME_TYPE);
        PackageParser.Package musicPackage = getDefaultSystemHandlerActivityPackage(musicIntent, i2);
        if (musicPackage != null && doesPackageSupportRuntimePermissions(musicPackage)) {
            grantRuntimePermissions(musicPackage, STORAGE_PERMISSIONS, i2);
        }
        Intent homeIntent = new Intent("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.HOME");
        homeIntent.addCategory("android.intent.category.LAUNCHER_APP");
        PackageParser.Package homePackage = getDefaultSystemHandlerActivityPackage(homeIntent, i2);
        if (homePackage == null || !doesPackageSupportRuntimePermissions(homePackage)) {
            PackageParser.Package packageR27 = musicPackage;
            i = 0;
        } else {
            Intent intent14 = musicIntent;
            PackageParser.Package packageR28 = musicPackage;
            i = 0;
            grantRuntimePermissions(homePackage, LOCATION_PERMISSIONS, false, i2);
        }
        Intent intent15 = homeIntent;
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch", i)) {
            Intent wearHomeIntent = new Intent("android.intent.action.MAIN");
            wearHomeIntent.addCategory("android.intent.category.HOME_MAIN");
            PackageParser.Package wearHomePackage = getDefaultSystemHandlerActivityPackage(wearHomeIntent, i2);
            if (wearHomePackage == null || !doesPackageSupportRuntimePermissions(wearHomePackage)) {
            } else {
                Intent intent16 = wearHomeIntent;
                grantRuntimePermissions(wearHomePackage, CONTACTS_PERMISSIONS, false, i2);
                grantRuntimePermissions(wearHomePackage, PHONE_PERMISSIONS, true, i2);
                grantRuntimePermissions(wearHomePackage, MICROPHONE_PERMISSIONS, false, i2);
                grantRuntimePermissions(wearHomePackage, LOCATION_PERMISSIONS, false, i2);
            }
            Intent trackIntent = new Intent(ACTION_TRACK);
            PackageParser.Package trackPackage = getDefaultSystemHandlerActivityPackage(trackIntent, i2);
            if (trackPackage != null && doesPackageSupportRuntimePermissions(trackPackage)) {
                Intent intent17 = trackIntent;
                PackageParser.Package packageR29 = wearHomePackage;
                grantRuntimePermissions(trackPackage, SENSORS_PERMISSIONS, false, i2);
                grantRuntimePermissions(trackPackage, LOCATION_PERMISSIONS, false, i2);
            }
        }
        PackageParser.Package printSpoolerPackage = getSystemPackage("com.android.printspooler");
        if (printSpoolerPackage != null && doesPackageSupportRuntimePermissions(printSpoolerPackage)) {
            grantRuntimePermissions(printSpoolerPackage, LOCATION_PERMISSIONS, true, i2);
        }
        Intent emergencyInfoIntent = new Intent("android.telephony.action.EMERGENCY_ASSISTANCE");
        PackageParser.Package emergencyInfoPckg = getDefaultSystemHandlerActivityPackage(emergencyInfoIntent, i2);
        if (emergencyInfoPckg == null || !doesPackageSupportRuntimePermissions(emergencyInfoPckg)) {
            Intent intent18 = emergencyInfoIntent;
        } else {
            PackageParser.Package packageR30 = printSpoolerPackage;
            Intent intent19 = emergencyInfoIntent;
            grantRuntimePermissions(emergencyInfoPckg, CONTACTS_PERMISSIONS, true, i2);
            grantRuntimePermissions(emergencyInfoPckg, PHONE_PERMISSIONS, true, i2);
        }
        Intent nfcTagIntent = new Intent("android.intent.action.VIEW");
        nfcTagIntent.setType("vnd.android.cursor.item/ndef_msg");
        PackageParser.Package nfcTagPkg = getDefaultSystemHandlerActivityPackage(nfcTagIntent, i2);
        if (nfcTagPkg == null || !doesPackageSupportRuntimePermissions(nfcTagPkg)) {
            PackageParser.Package packageR31 = emergencyInfoPckg;
        } else {
            Intent intent20 = nfcTagIntent;
            PackageParser.Package packageR32 = emergencyInfoPckg;
            grantRuntimePermissions(nfcTagPkg, CONTACTS_PERMISSIONS, false, i2);
            grantRuntimePermissions(nfcTagPkg, PHONE_PERMISSIONS, false, i2);
        }
        Intent storageManagerIntent = new Intent("android.os.storage.action.MANAGE_STORAGE");
        PackageParser.Package storageManagerPckg = getDefaultSystemHandlerActivityPackage(storageManagerIntent, i2);
        if (storageManagerPckg == null || !doesPackageSupportRuntimePermissions(storageManagerPckg)) {
            PackageParser.Package packageR33 = nfcTagPkg;
        } else {
            Intent intent21 = storageManagerIntent;
            PackageParser.Package packageR34 = nfcTagPkg;
            grantRuntimePermissions(storageManagerPckg, STORAGE_PERMISSIONS, true, i2);
        }
        PackageParser.Package companionDeviceDiscoveryPackage = getSystemPackage("com.android.companiondevicemanager");
        if (companionDeviceDiscoveryPackage == null || !doesPackageSupportRuntimePermissions(companionDeviceDiscoveryPackage)) {
            PackageParser.Package packageR35 = storageManagerPckg;
        } else {
            PackageParser.Package packageR36 = storageManagerPckg;
            grantRuntimePermissions(companionDeviceDiscoveryPackage, LOCATION_PERMISSIONS, true, i2);
        }
        Intent ringtonePickerIntent = new Intent("android.intent.action.RINGTONE_PICKER");
        PackageParser.Package ringtonePickerPackage = getDefaultSystemHandlerActivityPackage(ringtonePickerIntent, i2);
        if (ringtonePickerPackage == null || !doesPackageSupportRuntimePermissions(ringtonePickerPackage)) {
            Intent intent22 = ringtonePickerIntent;
        } else {
            PackageParser.Package packageR37 = companionDeviceDiscoveryPackage;
            Intent intent23 = ringtonePickerIntent;
            grantRuntimePermissions(ringtonePickerPackage, STORAGE_PERMISSIONS, true, i2);
        }
        String textClassifierPackageName = this.mContext.getPackageManager().getSystemTextClassifierPackageName();
        if (!TextUtils.isEmpty(textClassifierPackageName)) {
            PackageParser.Package textClassifierPackage = getSystemPackage(textClassifierPackageName);
            if (textClassifierPackage != null && doesPackageSupportRuntimePermissions(textClassifierPackage)) {
                String str7 = textClassifierPackageName;
                PackageParser.Package packageR38 = ringtonePickerPackage;
                grantRuntimePermissions(textClassifierPackage, PHONE_PERMISSIONS, false, i2);
                grantRuntimePermissions(textClassifierPackage, SMS_PERMISSIONS, false, i2);
                grantRuntimePermissions(textClassifierPackage, CALENDAR_PERMISSIONS, false, i2);
                grantRuntimePermissions(textClassifierPackage, LOCATION_PERMISSIONS, false, i2);
                grantRuntimePermissions(textClassifierPackage, CONTACTS_PERMISSIONS, false, i2);
                sharedStorageBackupPackage = getSystemPackage(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                if (sharedStorageBackupPackage != null) {
                    grantRuntimePermissions(sharedStorageBackupPackage, STORAGE_PERMISSIONS, true, i2);
                }
                if (this.mPermissionGrantedCallback == null) {
                    this.mPermissionGrantedCallback.onDefaultRuntimePermissionsGranted(i2);
                    return;
                }
                return;
            }
        }
        PackageParser.Package packageR39 = ringtonePickerPackage;
        sharedStorageBackupPackage = getSystemPackage(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
        if (sharedStorageBackupPackage != null) {
        }
        if (this.mPermissionGrantedCallback == null) {
        }
    }

    private void grantDefaultPermissionsToDefaultSystemDialerApp(PackageParser.Package dialerPackage, int userId) {
        if (doesPackageSupportRuntimePermissions(dialerPackage)) {
            grantRuntimePermissions(dialerPackage, PHONE_PERMISSIONS, this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch", 0), userId);
            grantRuntimePermissions(dialerPackage, CONTACTS_PERMISSIONS, userId);
            grantRuntimePermissions(dialerPackage, SMS_PERMISSIONS, userId);
            grantRuntimePermissions(dialerPackage, MICROPHONE_PERMISSIONS, userId);
            grantRuntimePermissions(dialerPackage, CAMERA_PERMISSIONS, userId);
        }
    }

    private void grantDefaultPermissionsToDefaultSystemSmsApp(PackageParser.Package smsPackage, int userId) {
        if (doesPackageSupportRuntimePermissions(smsPackage)) {
            grantRuntimePermissions(smsPackage, PHONE_PERMISSIONS, userId);
            grantRuntimePermissions(smsPackage, CONTACTS_PERMISSIONS, userId);
            grantRuntimePermissions(smsPackage, SMS_PERMISSIONS, userId);
            grantRuntimePermissions(smsPackage, STORAGE_PERMISSIONS, userId);
            grantRuntimePermissions(smsPackage, MICROPHONE_PERMISSIONS, userId);
            grantRuntimePermissions(smsPackage, CAMERA_PERMISSIONS, userId);
        }
    }

    private void grantDefaultPermissionsToDefaultSystemUseOpenWifiApp(PackageParser.Package useOpenWifiPackage, int userId) {
        if (doesPackageSupportRuntimePermissions(useOpenWifiPackage)) {
            grantRuntimePermissions(useOpenWifiPackage, COARSE_LOCATION_PERMISSIONS, userId);
        }
    }

    public void grantDefaultPermissionsToDefaultSmsApp(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default sms app for user:" + userId);
        if (packageName != null) {
            PackageParser.Package smsPackage = getPackage(packageName);
            if (smsPackage != null && doesPackageSupportRuntimePermissions(smsPackage)) {
                PackageParser.Package packageR = smsPackage;
                int i = userId;
                grantRuntimePermissions(packageR, PHONE_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, CONTACTS_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, SMS_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, STORAGE_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, MICROPHONE_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, CAMERA_PERMISSIONS, false, true, i);
            }
        }
    }

    public void grantDefaultPermissionsToDefaultDialerApp(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default dialer app for user:" + userId);
        if (packageName != null) {
            PackageParser.Package dialerPackage = getPackage(packageName);
            if (dialerPackage != null && doesPackageSupportRuntimePermissions(dialerPackage)) {
                PackageParser.Package packageR = dialerPackage;
                int i = userId;
                grantRuntimePermissions(packageR, PHONE_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, CONTACTS_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, SMS_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, MICROPHONE_PERMISSIONS, false, true, i);
                grantRuntimePermissions(packageR, CAMERA_PERMISSIONS, false, true, i);
            }
        }
    }

    public void grantDefaultPermissionsToDefaultUseOpenWifiApp(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default Use Open WiFi app for user:" + userId);
        if (packageName != null) {
            PackageParser.Package useOpenWifiPackage = getPackage(packageName);
            if (useOpenWifiPackage != null && doesPackageSupportRuntimePermissions(useOpenWifiPackage)) {
                grantRuntimePermissions(useOpenWifiPackage, COARSE_LOCATION_PERMISSIONS, false, true, userId);
            }
        }
    }

    private void grantDefaultPermissionsToDefaultSimCallManager(PackageParser.Package simCallManagerPackage, int userId) {
        Log.i(TAG, "Granting permissions to sim call manager for user:" + userId);
        if (doesPackageSupportRuntimePermissions(simCallManagerPackage)) {
            grantRuntimePermissions(simCallManagerPackage, PHONE_PERMISSIONS, userId);
            grantRuntimePermissions(simCallManagerPackage, MICROPHONE_PERMISSIONS, userId);
        }
    }

    public void grantDefaultPermissionsToDefaultSimCallManager(String packageName, int userId) {
        if (packageName != null) {
            PackageParser.Package simCallManagerPackage = getPackage(packageName);
            if (simCallManagerPackage != null) {
                grantDefaultPermissionsToDefaultSimCallManager(simCallManagerPackage, userId);
            }
        }
    }

    public void grantDefaultPermissionsToEnabledCarrierApps(String[] packageNames, int userId) {
        Log.i(TAG, "Granting permissions to enabled carrier apps for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                PackageParser.Package carrierPackage = getSystemPackage(packageName);
                if (carrierPackage != null && doesPackageSupportRuntimePermissions(carrierPackage)) {
                    grantRuntimePermissions(carrierPackage, PHONE_PERMISSIONS, userId);
                    grantRuntimePermissions(carrierPackage, LOCATION_PERMISSIONS, userId);
                    grantRuntimePermissions(carrierPackage, SMS_PERMISSIONS, userId);
                }
            }
        }
    }

    public void grantDefaultPermissionsToEnabledImsServices(String[] packageNames, int userId) {
        Log.i(TAG, "Granting permissions to enabled ImsServices for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                PackageParser.Package imsServicePackage = getSystemPackage(packageName);
                if (imsServicePackage != null && doesPackageSupportRuntimePermissions(imsServicePackage)) {
                    grantRuntimePermissions(imsServicePackage, PHONE_PERMISSIONS, userId);
                    grantRuntimePermissions(imsServicePackage, MICROPHONE_PERMISSIONS, userId);
                    grantRuntimePermissions(imsServicePackage, LOCATION_PERMISSIONS, userId);
                    grantRuntimePermissions(imsServicePackage, CAMERA_PERMISSIONS, userId);
                    grantRuntimePermissions(imsServicePackage, CONTACTS_PERMISSIONS, userId);
                }
            }
        }
    }

    public void grantDefaultPermissionsToEnabledTelephonyDataServices(String[] packageNames, int userId) {
        Log.i(TAG, "Granting permissions to enabled data services for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                PackageParser.Package dataServicePackage = getSystemPackage(packageName);
                if (dataServicePackage != null && doesPackageSupportRuntimePermissions(dataServicePackage)) {
                    grantRuntimePermissions(dataServicePackage, PHONE_PERMISSIONS, true, userId);
                    grantRuntimePermissions(dataServicePackage, LOCATION_PERMISSIONS, true, userId);
                }
            }
        }
    }

    public void revokeDefaultPermissionsFromDisabledTelephonyDataServices(String[] packageNames, int userId) {
        Log.i(TAG, "Revoking permissions from disabled data services for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                PackageParser.Package dataServicePackage = getSystemPackage(packageName);
                if (dataServicePackage != null && doesPackageSupportRuntimePermissions(dataServicePackage)) {
                    revokeRuntimePermissions(dataServicePackage, PHONE_PERMISSIONS, true, userId);
                    revokeRuntimePermissions(dataServicePackage, LOCATION_PERMISSIONS, true, userId);
                }
            }
        }
    }

    public void grantDefaultPermissionsToActiveLuiApp(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to active LUI app for user:" + userId);
        if (packageName != null) {
            PackageParser.Package luiAppPackage = getSystemPackage(packageName);
            if (luiAppPackage != null && doesPackageSupportRuntimePermissions(luiAppPackage)) {
                grantRuntimePermissions(luiAppPackage, CAMERA_PERMISSIONS, true, userId);
            }
        }
    }

    public void revokeDefaultPermissionsFromLuiApps(String[] packageNames, int userId) {
        Log.i(TAG, "Revoke permissions from LUI apps for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                PackageParser.Package luiAppPackage = getSystemPackage(packageName);
                if (luiAppPackage != null && doesPackageSupportRuntimePermissions(luiAppPackage)) {
                    revokeRuntimePermissions(luiAppPackage, CAMERA_PERMISSIONS, true, userId);
                }
            }
        }
    }

    public void grantDefaultPermissionsToDefaultBrowser(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default browser for user:" + userId);
        if (packageName != null) {
            PackageParser.Package browserPackage = getSystemPackage(packageName);
            if (browserPackage != null && doesPackageSupportRuntimePermissions(browserPackage)) {
                grantRuntimePermissions(browserPackage, LOCATION_PERMISSIONS, false, false, userId);
            }
        }
    }

    private PackageParser.Package getDefaultSystemHandlerActivityPackage(Intent intent, int userId) {
        ResolveInfo handler = this.mServiceInternal.resolveIntent(intent, intent.resolveType(this.mContext.getContentResolver()), DEFAULT_FLAGS, userId, false, Binder.getCallingUid());
        if (handler == null || handler.activityInfo == null || this.mServiceInternal.isResolveActivityComponent(handler.activityInfo)) {
            return null;
        }
        return getSystemPackage(handler.activityInfo.packageName);
    }

    private PackageParser.Package getDefaultSystemHandlerServicePackage(Intent intent, int userId) {
        List<ResolveInfo> handlers = this.mServiceInternal.queryIntentServices(intent, DEFAULT_FLAGS, Binder.getCallingUid(), userId);
        if (handlers == null) {
            return null;
        }
        int handlerCount = handlers.size();
        for (int i = 0; i < handlerCount; i++) {
            PackageParser.Package handlerPackage = getSystemPackage(handlers.get(i).serviceInfo.packageName);
            if (handlerPackage != null) {
                return handlerPackage;
            }
        }
        return null;
    }

    private List<PackageParser.Package> getHeadlessSyncAdapterPackages(String[] syncAdapterPackageNames, int userId) {
        List<PackageParser.Package> syncAdapterPackages = new ArrayList<>();
        Intent homeIntent = new Intent("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.LAUNCHER");
        for (String syncAdapterPackageName : syncAdapterPackageNames) {
            homeIntent.setPackage(syncAdapterPackageName);
            if (this.mServiceInternal.resolveIntent(homeIntent, homeIntent.resolveType(this.mContext.getContentResolver()), DEFAULT_FLAGS, userId, false, Binder.getCallingUid()) == null) {
                PackageParser.Package syncAdapterPackage = getSystemPackage(syncAdapterPackageName);
                if (syncAdapterPackage != null) {
                    syncAdapterPackages.add(syncAdapterPackage);
                }
            }
        }
        return syncAdapterPackages;
    }

    private PackageParser.Package getDefaultProviderAuthorityPackage(String authority, int userId) {
        ProviderInfo provider = this.mServiceInternal.resolveContentProvider(authority, DEFAULT_FLAGS, userId);
        if (provider != null) {
            return getSystemPackage(provider.packageName);
        }
        return null;
    }

    private PackageParser.Package getPackage(String packageName) {
        return this.mServiceInternal.getPackage(packageName);
    }

    /* access modifiers changed from: protected */
    public PackageParser.Package getSystemPackage(String packageName) {
        PackageParser.Package pkg = getPackage(packageName);
        PackageParser.Package packageR = null;
        if (pkg == null || !pkg.isSystem()) {
            return null;
        }
        if (!isSysComponentOrPersistentPlatformSignedPrivApp(pkg)) {
            packageR = pkg;
        }
        return packageR;
    }

    private void grantRuntimePermissions(PackageParser.Package pkg, Set<String> permissions, int userId) {
        grantRuntimePermissions(pkg, permissions, false, false, userId);
    }

    /* access modifiers changed from: protected */
    public void grantRuntimePermissions(PackageParser.Package pkg, Set<String> permissions, boolean systemFixed, int userId) {
        grantRuntimePermissions(pkg, permissions, systemFixed, false, userId);
    }

    private void revokeRuntimePermissions(PackageParser.Package pkg, Set<String> permissions, boolean systemFixed, int userId) {
        if (!pkg.requestedPermissions.isEmpty()) {
            Set<String> revokablePermissions = new ArraySet<>(pkg.requestedPermissions);
            for (String permission : permissions) {
                if (revokablePermissions.contains(permission)) {
                    int flags = this.mServiceInternal.getPermissionFlagsTEMP(permission, pkg.packageName, userId);
                    if ((flags & 32) != 0 && (flags & 4) == 0) {
                        if ((flags & 16) == 0 || systemFixed) {
                            this.mServiceInternal.revokeRuntimePermission(pkg.packageName, permission, userId, false);
                            this.mServiceInternal.updatePermissionFlagsTEMP(permission, pkg.packageName, 32, 0, userId);
                        }
                    }
                }
            }
        }
    }

    private void grantRuntimePermissions(PackageParser.Package pkg, Set<String> permissions, boolean systemFixed, boolean ignoreSystemPackage, int userId) {
        String permission;
        PackageParser.Package packageR = pkg;
        int i = userId;
        if (!packageR.requestedPermissions.isEmpty()) {
            List<String> requestedPermissions = packageR.requestedPermissions;
            Set<String> grantablePermissions = null;
            if (!ignoreSystemPackage && pkg.isUpdatedSystemApp()) {
                PackageParser.Package disabledPkg = this.mServiceInternal.getDisabledPackage(packageR.packageName);
                if (disabledPkg != null) {
                    if (!disabledPkg.requestedPermissions.isEmpty()) {
                        if (!requestedPermissions.equals(disabledPkg.requestedPermissions)) {
                            grantablePermissions = new ArraySet<>(requestedPermissions);
                            requestedPermissions = disabledPkg.requestedPermissions;
                        }
                    } else {
                        return;
                    }
                }
            }
            List<String> requestedPermissions2 = requestedPermissions;
            Set<String> grantablePermissions2 = grantablePermissions;
            int grantablePermissionCount = requestedPermissions2.size();
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 < grantablePermissionCount) {
                    String permission2 = requestedPermissions2.get(i3);
                    if ((grantablePermissions2 == null || grantablePermissions2.contains(permission2)) && permissions.contains(permission2)) {
                        int flags = this.mServiceInternal.getPermissionFlagsTEMP(permission2, packageR.packageName, i);
                        if (flags != 0 && !ignoreSystemPackage) {
                            permission = permission2;
                        } else if ((flags & 4) == 0) {
                            this.mServiceInternal.grantRuntimePermission(packageR.packageName, permission2, i, false);
                            int newFlags = 32;
                            if (systemFixed) {
                                newFlags = 32 | 16;
                            }
                            int newFlags2 = newFlags;
                            permission = permission2;
                            this.mServiceInternal.updatePermissionFlagsTEMP(permission2, packageR.packageName, newFlags2, newFlags2, i);
                        }
                        if (!((flags & 32) == 0 || (flags & 16) == 0 || systemFixed)) {
                            this.mServiceInternal.updatePermissionFlagsTEMP(permission, packageR.packageName, 16, 0, i);
                        }
                    }
                    i2 = i3 + 1;
                } else {
                    return;
                }
            }
        }
    }

    private boolean isSysComponentOrPersistentPlatformSignedPrivApp(PackageParser.Package pkg) {
        boolean z = true;
        if (UserHandle.getAppId(pkg.applicationInfo.uid) < 10000) {
            return true;
        }
        if (!pkg.isPrivileged()) {
            return false;
        }
        PackageParser.Package disabledPkg = this.mServiceInternal.getDisabledPackage(pkg.packageName);
        if (disabledPkg == null || disabledPkg.applicationInfo == null) {
            if ((pkg.applicationInfo.flags & 8) == 0) {
                return false;
            }
        } else if ((disabledPkg.applicationInfo.flags & 8) == 0) {
            return false;
        }
        PackageParser.Package systemPackage = getPackage(this.mServiceInternal.getKnownPackageName(0, 0));
        if (!pkg.mSigningDetails.hasAncestorOrSelf(systemPackage.mSigningDetails) && !systemPackage.mSigningDetails.checkCapability(pkg.mSigningDetails, 4)) {
            z = false;
        }
        return z;
    }

    private void grantDefaultPermissionExceptions(int userId) {
        this.mHandler.removeMessages(1);
        synchronized (this.mLock) {
            if (this.mGrantExceptions == null) {
                this.mGrantExceptions = readDefaultPermissionExceptionsLocked();
            }
        }
        int exceptionCount = this.mGrantExceptions.size();
        Set<String> permissions = null;
        int i = 0;
        while (i < exceptionCount) {
            PackageParser.Package pkg = getSystemPackage(this.mGrantExceptions.keyAt(i));
            List<DefaultPermissionGrant> permissionGrants = this.mGrantExceptions.valueAt(i);
            int permissionGrantCount = permissionGrants.size();
            Set<String> permissions2 = permissions;
            for (int j = 0; j < permissionGrantCount; j++) {
                DefaultPermissionGrant permissionGrant = permissionGrants.get(j);
                if (permissions2 == null) {
                    permissions2 = new ArraySet<>();
                } else {
                    permissions2.clear();
                }
                permissions2.add(permissionGrant.name);
                grantRuntimePermissions(pkg, permissions2, permissionGrant.fixed, userId);
            }
            i++;
            permissions = permissions2;
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
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.embedded", 0)) {
            File dir5 = new File(Environment.getOemDirectory(), "etc/default-permissions");
            if (dir5.isDirectory() && dir5.canRead()) {
                Collections.addAll(ret, dir5.listFiles());
            }
        }
        Iterator it = HwCfgFilePolicy.getCfgFileList("default-permissions", 0).iterator();
        while (it.hasNext()) {
            File d = (File) it.next();
            if (d.isDirectory() && d.canRead()) {
                for (File f : d.listFiles()) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0091, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009a, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009b, code lost:
        android.util.Slog.w(TAG, "Error reading default permissions file " + r4, r5);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x009a A[ExcHandler: IOException | XmlPullParserException (r5v3 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:12:0x006d] */
    public ArrayMap<String, List<DefaultPermissionGrant>> readDefaultPermissionExceptionsLocked() {
        InputStream str;
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
                try {
                    str = new BufferedInputStream(new FileInputStream(file));
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(str, null);
                    parse(parser, grantExceptions);
                    str.close();
                } catch (IOException | XmlPullParserException e) {
                } catch (Throwable th) {
                    if (r6 != null) {
                        str.close();
                    } else {
                        str.close();
                    }
                    throw th;
                }
            }
        }
        return grantExceptions;
    }

    private void parse(XmlPullParser parser, Map<String, List<DefaultPermissionGrant>> outGrantExceptions) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
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
            int next = parser.next();
            int type = next;
            if (next == 1) {
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
                        PackageParser.Package pkg = getSystemPackage(packageName);
                        if (pkg == null) {
                            Log.w(TAG, "Unknown package:" + packageName);
                            XmlUtils.skipCurrentTag(parser);
                        } else if (!doesPackageSupportRuntimePermissions(pkg)) {
                            Log.w(TAG, "Skipping non supporting runtime permissions package:" + packageName);
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            packageExceptions = new ArrayList<>();
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
            int next = parser.next();
            int type = next;
            if (next == 1) {
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
                        outPackageExceptions.add(new DefaultPermissionGrant(name, XmlUtils.readBooleanAttribute(parser, ATTR_FIXED)));
                    }
                } else {
                    Log.e(TAG, "Unknown tag " + parser.getName() + "under <exception>");
                }
            }
        }
    }

    private static boolean doesPackageSupportRuntimePermissions(PackageParser.Package pkg) {
        return pkg.applicationInfo.targetSdkVersion > 22;
    }
}
