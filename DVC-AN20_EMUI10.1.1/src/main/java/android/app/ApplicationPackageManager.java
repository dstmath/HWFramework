package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.ComponentInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IDexModuleRegisterCallback;
import android.content.pm.IOnPermissionsChangeListener;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstantAppInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.KeySet;
import android.content.pm.ModuleInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.SuspendDialogInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.content.pm.VersionedPackage;
import android.content.pm.dex.ArtManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hwtheme.HwThemeManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import android.util.LauncherIcons;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import com.android.internal.util.UserIcons;
import dalvik.system.VMRuntime;
import huawei.cust.HwCustUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import libcore.util.EmptyArray;

public class ApplicationPackageManager extends PackageManager {
    @VisibleForTesting
    public static final int[] CORP_BADGE_LABEL_RES_ID = {R.string.managed_profile_label_badge, R.string.managed_profile_label_badge_2, R.string.managed_profile_label_badge_3};
    private static final boolean DEBUG_ICONS = false;
    private static final int DEFAULT_EPHEMERAL_COOKIE_MAX_SIZE_BYTES = 16384;
    private static final String TAG = "ApplicationPackageManager";
    private static final int sDefaultFlags = 1024;
    private static ArrayMap<ResourceName, WeakReference<Drawable.ConstantState>> sIconCache = new ArrayMap<>();
    private static ArrayMap<ResourceName, WeakReference<CharSequence>> sStringCache = new ArrayMap<>();
    private static final Object sSync = new Object();
    @GuardedBy({"mLock"})
    private ArtManager mArtManager;
    volatile int mCachedSafeMode = -1;
    private final ContextImpl mContext;
    @GuardedBy({"mDelegates"})
    private final ArrayList<MoveCallbackDelegate> mDelegates = new ArrayList<>();
    private HwCustApplicationPackageManager mHwCustApplicationPackageManager = ((HwCustApplicationPackageManager) HwCustUtils.createObj(HwCustApplicationPackageManager.class, new Object[0]));
    @GuardedBy({"mLock"})
    private PackageInstaller mInstaller;
    private final Object mLock = new Object();
    @UnsupportedAppUsage
    private final IPackageManager mPM;
    private final Map<PackageManager.OnPermissionsChangedListener, IOnPermissionsChangeListener> mPermissionListeners = new ArrayMap();
    @GuardedBy({"mLock"})
    private String mPermissionsControllerPackageName;
    @GuardedBy({"mLock"})
    private UserManager mUserManager;
    private volatile boolean mUserUnlocked = false;

    /* access modifiers changed from: package-private */
    public UserManager getUserManager() {
        UserManager userManager;
        synchronized (this.mLock) {
            if (this.mUserManager == null) {
                this.mUserManager = UserManager.get(this.mContext);
            }
            userManager = this.mUserManager;
        }
        return userManager;
    }

    @Override // android.content.pm.PackageManager
    public int getUserId() {
        return this.mContext.getUserId();
    }

    @Override // android.content.pm.PackageManager
    public PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return getPackageInfoAsUser(packageName, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public PackageInfo getPackageInfo(VersionedPackage versionedPackage, int flags) throws PackageManager.NameNotFoundException {
        int userId = getUserId();
        try {
            PackageInfo pi = this.mPM.getPackageInfoVersioned(versionedPackage, updateFlagsForPackage(flags, userId), userId);
            if (pi != null) {
                return pi;
            }
            throw new PackageManager.NameNotFoundException(versionedPackage.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        try {
            PackageInfo pi = this.mPM.getPackageInfo(packageName, updateFlagsForPackage(flags, userId), userId);
            if (pi != null) {
                return pi;
            }
            throw new PackageManager.NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String[] currentToCanonicalPackageNames(String[] names) {
        try {
            return this.mPM.currentToCanonicalPackageNames(names);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String[] canonicalToCurrentPackageNames(String[] names) {
        try {
            return this.mPM.canonicalToCurrentPackageNames(names);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public Intent getLaunchIntentForPackage(String packageName) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0);
        if (ris == null || ris.size() <= 0) {
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = queryIntentActivities(intentToResolve, 0);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(268435456);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }

    @Override // android.content.pm.PackageManager
    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        return getLaunchIntentForPackageAndCategory(packageName, Intent.CATEGORY_LEANBACK_LAUNCHER);
    }

    @Override // android.content.pm.PackageManager
    public Intent getCarLaunchIntentForPackage(String packageName) {
        return getLaunchIntentForPackageAndCategory(packageName, Intent.CATEGORY_CAR_LAUNCHER);
    }

    private Intent getLaunchIntentForPackageAndCategory(String packageName, String category) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(category);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0);
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(268435456);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }

    @Override // android.content.pm.PackageManager
    public int[] getPackageGids(String packageName) throws PackageManager.NameNotFoundException {
        return getPackageGids(packageName, 0);
    }

    @Override // android.content.pm.PackageManager
    public int[] getPackageGids(String packageName, int flags) throws PackageManager.NameNotFoundException {
        int userId = getUserId();
        try {
            int[] gids = this.mPM.getPackageGids(packageName, updateFlagsForPackage(flags, userId), userId);
            if (gids != null) {
                return gids;
            }
            throw new PackageManager.NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getPackageUid(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return getPackageUidAsUser(packageName, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public int getPackageUidAsUser(String packageName, int userId) throws PackageManager.NameNotFoundException {
        return getPackageUidAsUser(packageName, 0, userId);
    }

    @Override // android.content.pm.PackageManager
    public int getPackageUidAsUser(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        try {
            int uid = this.mPM.getPackageUid(packageName, updateFlagsForPackage(flags, userId), userId);
            if (uid >= 0) {
                return uid;
            }
            throw new PackageManager.NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public PermissionInfo getPermissionInfo(String name, int flags) throws PackageManager.NameNotFoundException {
        try {
            PermissionInfo pi = this.mPM.getPermissionInfo(name, this.mContext.getOpPackageName(), flags);
            if (pi != null) {
                return pi;
            }
            throw new PackageManager.NameNotFoundException(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws PackageManager.NameNotFoundException {
        List<PermissionInfo> pi;
        try {
            ParceledListSlice<PermissionInfo> parceledList = this.mPM.queryPermissionsByGroup(group, flags);
            if (parceledList != null && (pi = parceledList.getList()) != null) {
                return pi;
            }
            throw new PackageManager.NameNotFoundException(group);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean arePermissionsIndividuallyControlled() {
        return this.mContext.getResources().getBoolean(R.bool.config_permissionsIndividuallyControlled);
    }

    @Override // android.content.pm.PackageManager
    public boolean isWirelessConsentModeEnabled() {
        return this.mContext.getResources().getBoolean(R.bool.config_wirelessConsentRequired);
    }

    @Override // android.content.pm.PackageManager
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws PackageManager.NameNotFoundException {
        try {
            PermissionGroupInfo pgi = this.mPM.getPermissionGroupInfo(name, flags);
            if (pgi != null) {
                return pgi;
            }
            throw new PackageManager.NameNotFoundException(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        try {
            ParceledListSlice<PermissionGroupInfo> parceledList = this.mPM.getAllPermissionGroups(flags);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return getApplicationInfoAsUser(packageName, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public ApplicationInfo getApplicationInfoAsUser(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        try {
            ApplicationInfo ai = this.mPM.getApplicationInfo(packageName, updateFlagsForApplication(flags, userId), userId);
            if (ai != null) {
                return maybeAdjustApplicationInfo(ai);
            }
            throw new PackageManager.NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static ApplicationInfo maybeAdjustApplicationInfo(ApplicationInfo info) {
        if (!(info.primaryCpuAbi == null || info.secondaryCpuAbi == null)) {
            String runtimeIsa = VMRuntime.getRuntime().vmInstructionSet();
            String secondaryIsa = VMRuntime.getInstructionSet(info.secondaryCpuAbi);
            String secondaryDexCodeIsa = SystemProperties.get("ro.dalvik.vm.isa." + secondaryIsa);
            if (runtimeIsa.equals(secondaryDexCodeIsa.isEmpty() ? secondaryIsa : secondaryDexCodeIsa)) {
                ApplicationInfo modified = new ApplicationInfo(info);
                modified.nativeLibraryDir = info.secondaryNativeLibraryDir;
                return modified;
            }
        }
        return info;
    }

    @Override // android.content.pm.PackageManager
    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        int userId = getUserId();
        try {
            ActivityInfo ai = this.mPM.getActivityInfo(className, updateFlagsForComponent(flags, userId, null), userId);
            if (ai != null) {
                return ai;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        int userId = getUserId();
        try {
            ActivityInfo ai = this.mPM.getReceiverInfo(className, updateFlagsForComponent(flags, userId, null), userId);
            if (ai != null) {
                return ai;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        int userId = getUserId();
        try {
            ServiceInfo si = this.mPM.getServiceInfo(className, updateFlagsForComponent(flags, userId, null), userId);
            if (si != null) {
                return si;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        int userId = getUserId();
        try {
            ProviderInfo pi = this.mPM.getProviderInfo(className, updateFlagsForComponent(flags, userId, null), userId);
            if (pi != null) {
                return pi;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String[] getSystemSharedLibraryNames() {
        try {
            return this.mPM.getSystemSharedLibraryNames();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<SharedLibraryInfo> getSharedLibraries(int flags) {
        return getSharedLibrariesAsUser(flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public List<SharedLibraryInfo> getSharedLibrariesAsUser(int flags, int userId) {
        try {
            ParceledListSlice<SharedLibraryInfo> sharedLibs = this.mPM.getSharedLibraries(this.mContext.getOpPackageName(), flags, userId);
            if (sharedLibs == null) {
                return Collections.emptyList();
            }
            return sharedLibs.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<SharedLibraryInfo> getDeclaredSharedLibraries(String packageName, int flags) {
        try {
            ParceledListSlice<SharedLibraryInfo> sharedLibraries = this.mPM.getDeclaredSharedLibraries(packageName, flags, this.mContext.getUserId());
            return sharedLibraries != null ? sharedLibraries.getList() : Collections.emptyList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getServicesSystemSharedLibraryPackageName() {
        try {
            return this.mPM.getServicesSystemSharedLibraryPackageName();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getSharedSystemSharedLibraryPackageName() {
        try {
            return this.mPM.getSharedSystemSharedLibraryPackageName();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ChangedPackages getChangedPackages(int sequenceNumber) {
        try {
            return this.mPM.getChangedPackages(sequenceNumber, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public FeatureInfo[] getSystemAvailableFeatures() {
        try {
            ParceledListSlice<FeatureInfo> parceledList = this.mPM.getSystemAvailableFeatures();
            if (parceledList == null) {
                return new FeatureInfo[0];
            }
            List<FeatureInfo> list = parceledList.getList();
            FeatureInfo[] res = new FeatureInfo[list.size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = list.get(i);
            }
            return res;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean hasSystemFeature(String name) {
        return hasSystemFeature(name, 0);
    }

    @Override // android.content.pm.PackageManager
    public boolean hasSystemFeature(String name, int version) {
        try {
            return this.mPM.hasSystemFeature(name, version);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int checkPermission(String permName, String pkgName) {
        try {
            return this.mPM.checkPermission(permName, pkgName, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        try {
            return this.mPM.isPermissionRevokedByPolicy(permName, pkgName, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getPermissionControllerPackageName() {
        String str;
        synchronized (this.mLock) {
            if (this.mPermissionsControllerPackageName == null) {
                try {
                    this.mPermissionsControllerPackageName = this.mPM.getPermissionControllerPackageName();
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            str = this.mPermissionsControllerPackageName;
        }
        return str;
    }

    @Override // android.content.pm.PackageManager
    public boolean addPermission(PermissionInfo info) {
        try {
            return this.mPM.addPermission(info);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean addPermissionAsync(PermissionInfo info) {
        try {
            return this.mPM.addPermissionAsync(info);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void removePermission(String name) {
        try {
            this.mPM.removePermission(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void grantRuntimePermission(String packageName, String permissionName, UserHandle user) {
        try {
            this.mPM.grantRuntimePermission(packageName, permissionName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void revokeRuntimePermission(String packageName, String permissionName, UserHandle user) {
        try {
            this.mPM.revokeRuntimePermission(packageName, permissionName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
        try {
            return this.mPM.getPermissionFlags(permissionName, packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {
        try {
            this.mPM.updatePermissionFlags(permissionName, packageName, flagMask, flagValues, this.mContext.getApplicationInfo().targetSdkVersion >= 29, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public Set<String> getWhitelistedRestrictedPermissions(String packageName, int whitelistFlags) {
        try {
            List<String> whitelist = this.mPM.getWhitelistedRestrictedPermissions(packageName, whitelistFlags, getUserId());
            if (whitelist != null) {
                return new ArraySet(whitelist);
            }
            return Collections.emptySet();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean addWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags) {
        try {
            return this.mPM.addWhitelistedRestrictedPermission(packageName, permission, whitelistFlags, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean removeWhitelistedRestrictedPermission(String packageName, String permission, int whitelistFlags) {
        try {
            return this.mPM.removeWhitelistedRestrictedPermission(packageName, permission, whitelistFlags, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    @UnsupportedAppUsage
    public boolean shouldShowRequestPermissionRationale(String permission) {
        try {
            return this.mPM.shouldShowRequestPermissionRationale(permission, this.mContext.getPackageName(), getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int checkSignatures(String pkg1, String pkg2) {
        try {
            return this.mPM.checkSignatures(pkg1, pkg2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int checkSignatures(int uid1, int uid2) {
        try {
            return this.mPM.checkUidSignatures(uid1, uid2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean hasSigningCertificate(String packageName, byte[] certificate, int type) {
        try {
            return this.mPM.hasSigningCertificate(packageName, certificate, type);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean hasSigningCertificate(int uid, byte[] certificate, int type) {
        try {
            return this.mPM.hasUidSigningCertificate(uid, certificate, type);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String[] getPackagesForUid(int uid) {
        try {
            return this.mPM.getPackagesForUid(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getNameForUid(int uid) {
        try {
            return this.mPM.getNameForUid(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String[] getNamesForUids(int[] uids) {
        try {
            return this.mPM.getNamesForUids(uids);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getUidForSharedUser(String sharedUserName) throws PackageManager.NameNotFoundException {
        try {
            int uid = this.mPM.getUidForSharedUser(sharedUserName);
            if (uid != -1) {
                return uid;
            }
            throw new PackageManager.NameNotFoundException("No shared userid for user:" + sharedUserName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ModuleInfo> getInstalledModules(int flags) {
        try {
            return this.mPM.getInstalledModules(flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ModuleInfo getModuleInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        try {
            ModuleInfo mi = this.mPM.getModuleInfo(packageName, flags);
            if (mi != null) {
                return mi;
            }
            throw new PackageManager.NameNotFoundException("No module info for package: " + packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<PackageInfo> getInstalledPackages(int flags) {
        return getInstalledPackagesAsUser(flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
        try {
            ParceledListSlice<PackageInfo> parceledList = this.mPM.getInstalledPackages(updateFlagsForPackage(flags, userId), userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        int userId = getUserId();
        try {
            ParceledListSlice<PackageInfo> parceledList = this.mPM.getPackagesHoldingPermissions(permissions, updateFlagsForPackage(flags, userId), userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return getInstalledApplicationsAsUser(flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public List<ApplicationInfo> getInstalledApplicationsAsUser(int flags, int userId) {
        try {
            ParceledListSlice<ApplicationInfo> parceledList = this.mPM.getInstalledApplications(updateFlagsForApplication(flags, userId), userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<InstantAppInfo> getInstantApps() {
        try {
            ParceledListSlice<InstantAppInfo> slice = this.mPM.getInstantApps(getUserId());
            if (slice != null) {
                return slice.getList();
            }
            return Collections.emptyList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public Drawable getInstantAppIcon(String packageName) {
        try {
            Bitmap bitmap = this.mPM.getInstantAppIcon(packageName, getUserId());
            if (bitmap != null) {
                return new BitmapDrawable((Resources) null, bitmap);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isInstantApp() {
        return isInstantApp(this.mContext.getPackageName());
    }

    @Override // android.content.pm.PackageManager
    public boolean isInstantApp(String packageName) {
        try {
            return this.mPM.isInstantApp(packageName, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getInstantAppCookieMaxBytes() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), Settings.Global.EPHEMERAL_COOKIE_MAX_SIZE_BYTES, 16384);
    }

    @Override // android.content.pm.PackageManager
    public int getInstantAppCookieMaxSize() {
        return getInstantAppCookieMaxBytes();
    }

    @Override // android.content.pm.PackageManager
    public byte[] getInstantAppCookie() {
        try {
            byte[] cookie = this.mPM.getInstantAppCookie(this.mContext.getPackageName(), getUserId());
            if (cookie != null) {
                return cookie;
            }
            return EmptyArray.BYTE;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void clearInstantAppCookie() {
        updateInstantAppCookie(null);
    }

    @Override // android.content.pm.PackageManager
    public void updateInstantAppCookie(byte[] cookie) {
        if (cookie == null || cookie.length <= getInstantAppCookieMaxBytes()) {
            try {
                this.mPM.setInstantAppCookie(this.mContext.getPackageName(), cookie, getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("instant cookie longer than " + getInstantAppCookieMaxBytes());
        }
    }

    @Override // android.content.pm.PackageManager
    @UnsupportedAppUsage
    public boolean setInstantAppCookie(byte[] cookie) {
        try {
            return this.mPM.setInstantAppCookie(this.mContext.getPackageName(), cookie, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        return resolveActivityAsUser(intent, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
        try {
            return this.mPM.resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), updateFlagsForComponent(flags, userId, intent), userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        return queryIntentActivitiesAsUser(intent, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), updateFlagsForComponent(flags, userId, intent), userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            List<ResolveInfo> list = parceledList.getList();
            if (this.mHwCustApplicationPackageManager != null) {
                this.mHwCustApplicationPackageManager.removeMmsResolveInfo(list, intent, this.mContext);
            }
            return list;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        String[] specificTypes;
        String t;
        int userId = getUserId();
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] specificTypes2 = null;
        if (specifics != null) {
            int N = specifics.length;
            for (int i = 0; i < N; i++) {
                Intent sp = specifics[i];
                if (!(sp == null || (t = sp.resolveTypeIfNeeded(resolver)) == null)) {
                    if (specificTypes2 == null) {
                        specificTypes2 = new String[N];
                    }
                    specificTypes2[i] = t;
                }
            }
            specificTypes = specificTypes2;
        } else {
            specificTypes = null;
        }
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentActivityOptions(caller, specifics, specificTypes, intent, intent.resolveTypeIfNeeded(resolver), updateFlagsForComponent(flags, userId, intent), userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentReceivers(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), updateFlagsForComponent(flags, userId, intent), userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return queryBroadcastReceiversAsUser(intent, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public ResolveInfo resolveServiceAsUser(Intent intent, int flags, int userId) {
        try {
            return this.mPM.resolveService(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), updateFlagsForComponent(flags, userId, intent), userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ResolveInfo resolveService(Intent intent, int flags) {
        return resolveServiceAsUser(intent, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), updateFlagsForComponent(flags, userId, intent), userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return queryIntentServicesAsUser(intent, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentContentProviders(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), updateFlagsForComponent(flags, userId, intent), userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return queryIntentContentProvidersAsUser(intent, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public ProviderInfo resolveContentProvider(String name, int flags) {
        return resolveContentProviderAsUser(name, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public ProviderInfo resolveContentProviderAsUser(String name, int flags, int userId) {
        try {
            return this.mPM.resolveContentProvider(name, updateFlagsForComponent(flags, userId, null), userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return queryContentProviders(processName, uid, flags, null);
    }

    @Override // android.content.pm.PackageManager
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, String metaDataKey) {
        try {
            ParceledListSlice<ProviderInfo> slice = this.mPM.queryContentProviders(processName, uid, updateFlagsForComponent(flags, UserHandle.getUserId(uid), null), metaDataKey);
            return slice != null ? slice.getList() : Collections.emptyList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        try {
            InstrumentationInfo ii = this.mPM.getInstrumentationInfo(className, flags);
            if (ii != null) {
                return ii;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        try {
            ParceledListSlice<InstrumentationInfo> parceledList = this.mPM.queryInstrumentation(targetPackage, flags);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public Drawable getDrawable(String packageName, int resId, ApplicationInfo appInfo) {
        ResourceName name = new ResourceName(packageName, resId);
        Drawable cachedIcon = getCachedIcon(name);
        if (cachedIcon != null) {
            return cachedIcon;
        }
        if (appInfo == null) {
            try {
                appInfo = getApplicationInfo(packageName, 1024);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
        if (resId != 0) {
            try {
                Drawable dr = getResourcesForApplication(appInfo).getDrawable(resId, null);
                if (dr != null) {
                    putCachedIcon(name, dr);
                }
                return dr;
            } catch (PackageManager.NameNotFoundException e2) {
                Log.w("PackageManager", "Failure retrieving resources for " + appInfo.packageName);
            } catch (Resources.NotFoundException e3) {
                Log.w("PackageManager", "Failure retrieving resources for " + appInfo.packageName + ": " + e3.getMessage());
            } catch (Exception e4) {
                Log.w("PackageManager", "Failure retrieving icon 0x" + Integer.toHexString(resId) + " in package " + packageName, e4);
            }
        }
        return null;
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityIcon(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadIcon(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityIcon(Intent intent) throws PackageManager.NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityIcon(intent.getComponent());
        }
        ResolveInfo info = resolveActivity(intent, 65536);
        if (info != null) {
            return info.activityInfo.loadIcon(this);
        }
        throw new PackageManager.NameNotFoundException(intent.toUri(0));
    }

    @Override // android.content.pm.PackageManager
    public Drawable getDefaultActivityIcon() {
        return this.mContext.getDrawable(17301651);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIcon(ApplicationInfo info) {
        return info.loadIcon(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIcon(String packageName) throws PackageManager.NameNotFoundException {
        return getApplicationIcon(getApplicationInfo(packageName, 1024));
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityBanner(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadBanner(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityBanner(Intent intent) throws PackageManager.NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityBanner(intent.getComponent());
        }
        ResolveInfo info = resolveActivity(intent, 65536);
        if (info != null) {
            return info.activityInfo.loadBanner(this);
        }
        throw new PackageManager.NameNotFoundException(intent.toUri(0));
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationBanner(ApplicationInfo info) {
        return info.loadBanner(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationBanner(String packageName) throws PackageManager.NameNotFoundException {
        return getApplicationBanner(getApplicationInfo(packageName, 1024));
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityLogo(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadLogo(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityLogo(Intent intent) throws PackageManager.NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityLogo(intent.getComponent());
        }
        ResolveInfo info = resolveActivity(intent, 65536);
        if (info != null) {
            return info.activityInfo.loadLogo(this);
        }
        throw new PackageManager.NameNotFoundException(intent.toUri(0));
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationLogo(ApplicationInfo info) {
        return info.loadLogo(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationLogo(String packageName) throws PackageManager.NameNotFoundException {
        return getApplicationLogo(getApplicationInfo(packageName, 1024));
    }

    @Override // android.content.pm.PackageManager
    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        if (isClonedProfile(user.getIdentifier())) {
            return HwThemeManager.getClonedDrawable(this.mContext, icon);
        }
        if (!isManagedProfile(user.getIdentifier())) {
            return icon;
        }
        return getBadgedDrawable(icon, new LauncherIcons(this.mContext).getBadgeDrawable(R.drawable.ic_corp_icon_badge_case, getUserBadgeColor(user)), null, false);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        Drawable badgeDrawable = getUserBadgeForDensity(user, badgeDensity);
        if (badgeDrawable == null) {
            return drawable;
        }
        return getBadgedDrawable(drawable, badgeDrawable, badgeLocation, true);
    }

    private int getUserBadgeColor(UserHandle user) {
        return IconDrawableFactory.getUserBadgeColor(getUserManager(), user.getIdentifier());
    }

    @Override // android.content.pm.PackageManager
    public Drawable getUserBadgeForDensity(UserHandle user, int density) {
        Drawable badgeColor = getManagedProfileIconForDensity(user, R.drawable.ic_corp_badge_color, density);
        if (badgeColor == null) {
            return null;
        }
        Drawable badgeForeground = getDrawableForDensity(R.drawable.ic_corp_badge_case, density);
        badgeForeground.setTint(getUserBadgeColor(user));
        return new LayerDrawable(new Drawable[]{badgeColor, badgeForeground});
    }

    @Override // android.content.pm.PackageManager
    public Drawable getUserBadgeForDensityNoBackground(UserHandle user, int density) {
        if (isClonedProfile(user.getIdentifier())) {
            return HwThemeManager.getClonedDrawable(this.mContext, null);
        }
        Drawable badge = getManagedProfileIconForDensity(user, R.drawable.ic_corp_badge_no_background, density);
        if (badge != null) {
            badge.setTint(getUserBadgeColor(user));
        }
        return badge;
    }

    private Drawable getDrawableForDensity(int drawableId, int density) {
        if (density <= 0) {
            density = this.mContext.getResources().getDisplayMetrics().densityDpi;
        }
        return this.mContext.getResources().getDrawableForDensity(drawableId, density);
    }

    private Drawable getManagedProfileIconForDensity(UserHandle user, int drawableId, int density) {
        if (isManagedProfile(user.getIdentifier())) {
            return getDrawableForDensity(drawableId, density);
        }
        return null;
    }

    @Override // android.content.pm.PackageManager
    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        if (!isManagedProfile(user.getIdentifier())) {
            return label;
        }
        int badge = getUserManager().getManagedProfileBadge(user.getIdentifier());
        int[] iArr = CORP_BADGE_LABEL_RES_ID;
        int resourceId = iArr[badge % iArr.length];
        return Resources.getSystem().getString(resourceId, label);
    }

    @Override // android.content.pm.PackageManager
    public Resources getResourcesForActivity(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return getResourcesForApplication(getActivityInfo(activityName, 1024).applicationInfo);
    }

    @Override // android.content.pm.PackageManager
    public Resources getResourcesForApplication(ApplicationInfo app) throws PackageManager.NameNotFoundException {
        if (app.packageName.equals("system")) {
            return this.mContext.mMainThread.getSystemUiContext().getResources();
        }
        boolean sameUid = app.uid == Process.myUid();
        ResourcesManager.getInstance().setHwThemeType(sameUid ? app.sourceDir : app.publicSourceDir, app.hwThemeType);
        Resources r = this.mContext.mMainThread.getTopLevelResources(sameUid ? app.sourceDir : app.publicSourceDir, sameUid ? app.splitSourceDirs : app.splitPublicSourceDirs, app.resourceDirs, app.sharedLibraryFiles, 0, this.mContext.mPackageInfo);
        if (r != null) {
            return r;
        }
        throw new PackageManager.NameNotFoundException("Unable to open " + app.publicSourceDir);
    }

    @Override // android.content.pm.PackageManager
    public Resources getResourcesForApplication(String appPackageName) throws PackageManager.NameNotFoundException {
        return getResourcesForApplication(getApplicationInfo(appPackageName, 1024));
    }

    @Override // android.content.pm.PackageManager
    public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) throws PackageManager.NameNotFoundException {
        if (userId < 0) {
            throw new IllegalArgumentException("Call does not support special user #" + userId);
        } else if ("system".equals(appPackageName)) {
            return this.mContext.mMainThread.getSystemUiContext().getResources();
        } else {
            try {
                ApplicationInfo ai = this.mPM.getApplicationInfo(appPackageName, 1024, userId);
                if (ai != null) {
                    return getResourcesForApplication(ai);
                }
                throw new PackageManager.NameNotFoundException("Package " + appPackageName + " doesn't exist");
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isSafeMode() {
        try {
            if (this.mCachedSafeMode < 0) {
                this.mCachedSafeMode = this.mPM.isSafeMode() ? 1 : 0;
            }
            return this.mCachedSafeMode != 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void addOnPermissionsChangeListener(PackageManager.OnPermissionsChangedListener listener) {
        synchronized (this.mPermissionListeners) {
            if (this.mPermissionListeners.get(listener) == null) {
                OnPermissionsChangeListenerDelegate delegate = new OnPermissionsChangeListenerDelegate(listener, Looper.getMainLooper());
                try {
                    this.mPM.addOnPermissionsChangeListener(delegate);
                    this.mPermissionListeners.put(listener, delegate);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    @Override // android.content.pm.PackageManager
    public void removeOnPermissionsChangeListener(PackageManager.OnPermissionsChangedListener listener) {
        synchronized (this.mPermissionListeners) {
            IOnPermissionsChangeListener delegate = this.mPermissionListeners.get(listener);
            if (delegate != null) {
                try {
                    this.mPM.removeOnPermissionsChangeListener(delegate);
                    this.mPermissionListeners.remove(listener);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    @UnsupportedAppUsage
    static void configurationChanged() {
        synchronized (sSync) {
            sIconCache.clear();
            sStringCache.clear();
        }
    }

    @UnsupportedAppUsage
    protected ApplicationPackageManager(ContextImpl context, IPackageManager pm) {
        this.mContext = context;
        this.mPM = pm;
    }

    private int updateFlagsForPackage(int flags, int userId) {
        if ((flags & 15) != 0 && (269221888 & flags) == 0) {
            onImplicitDirectBoot(userId);
        }
        return flags;
    }

    private int updateFlagsForApplication(int flags, int userId) {
        return updateFlagsForPackage(flags, userId);
    }

    private int updateFlagsForComponent(int flags, int userId, Intent intent) {
        if (!(intent == null || (intent.getFlags() & 256) == 0)) {
            flags |= 268435456;
        }
        if ((269221888 & flags) == 0) {
            onImplicitDirectBoot(userId);
        }
        return flags;
    }

    private void onImplicitDirectBoot(int userId) {
        if (!StrictMode.vmImplicitDirectBootEnabled()) {
            return;
        }
        if (userId == UserHandle.myUserId()) {
            if (!this.mUserUnlocked) {
                if (((UserManager) this.mContext.getSystemService(UserManager.class)).isUserUnlockingOrUnlocked(userId)) {
                    this.mUserUnlocked = true;
                } else {
                    StrictMode.onImplicitDirectBoot();
                }
            }
        } else if (!((UserManager) this.mContext.getSystemService(UserManager.class)).isUserUnlockingOrUnlocked(userId)) {
            StrictMode.onImplicitDirectBoot();
        }
    }

    private Drawable getCachedIcon(ResourceName name) {
        synchronized (sSync) {
            WeakReference<Drawable.ConstantState> wr = sIconCache.get(name);
            if (wr != null) {
                Drawable.ConstantState state = wr.get();
                if (state != null) {
                    return state.newDrawable();
                }
                sIconCache.remove(name);
            }
            return null;
        }
    }

    private void putCachedIcon(ResourceName name, Drawable dr) {
        synchronized (sSync) {
            sIconCache.put(name, new WeakReference<>(dr.getConstantState()));
        }
    }

    static void handlePackageBroadcast(int cmd, String[] pkgList, boolean hasPkgInfo) {
        boolean immediateGc = false;
        if (cmd == 1) {
            immediateGc = true;
        }
        if (pkgList != null && pkgList.length > 0) {
            boolean needCleanup = false;
            for (String ssp : pkgList) {
                synchronized (sSync) {
                    for (int i = sIconCache.size() - 1; i >= 0; i--) {
                        if (sIconCache.keyAt(i).packageName.equals(ssp)) {
                            sIconCache.removeAt(i);
                            needCleanup = true;
                        }
                    }
                    for (int i2 = sStringCache.size() - 1; i2 >= 0; i2--) {
                        if (sStringCache.keyAt(i2).packageName.equals(ssp)) {
                            sStringCache.removeAt(i2);
                            needCleanup = true;
                        }
                    }
                }
            }
            if (!(needCleanup || hasPkgInfo)) {
                return;
            }
            if (immediateGc) {
                Runtime.getRuntime().gc();
            } else {
                ActivityThread.currentActivityThread().scheduleGcIdler();
            }
        }
    }

    public static final class ResourceName {
        final int iconId;
        final String packageName;

        public ResourceName(String _packageName, int _iconId) {
            this.packageName = _packageName;
            this.iconId = _iconId;
        }

        ResourceName(ApplicationInfo aInfo, int _iconId) {
            this(aInfo.packageName, _iconId);
        }

        ResourceName(ComponentInfo cInfo, int _iconId) {
            this(cInfo.applicationInfo.packageName, _iconId);
        }

        ResourceName(ResolveInfo rInfo, int _iconId) {
            this(rInfo.activityInfo.applicationInfo.packageName, _iconId);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ResourceName that = (ResourceName) o;
            if (this.iconId != that.iconId) {
                return false;
            }
            String str = this.packageName;
            if (str != null) {
                if (str.equals(that.packageName)) {
                    return true;
                }
            } else if (that.packageName == null) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (this.packageName.hashCode() * 31) + this.iconId;
        }

        public String toString() {
            return "{ResourceName " + this.packageName + " / " + this.iconId + "}";
        }
    }

    public CharSequence getCachedString(ResourceName name) {
        synchronized (sSync) {
            WeakReference<CharSequence> wr = sStringCache.get(name);
            if (wr != null) {
                CharSequence cs = wr.get();
                if (cs != null) {
                    return cs;
                }
                sStringCache.remove(name);
            }
            return null;
        }
    }

    public void putCachedString(ResourceName name, CharSequence cs) {
        synchronized (sSync) {
            sStringCache.put(name, new WeakReference<>(cs));
        }
    }

    @Override // android.content.pm.PackageManager
    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        ResourceName name = new ResourceName(packageName, resid);
        CharSequence text = getCachedString(name);
        if (text != null) {
            return text;
        }
        if (appInfo == null) {
            try {
                appInfo = getApplicationInfo(packageName, 1024);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
        try {
            CharSequence text2 = getResourcesForApplication(appInfo).getText(resid);
            putCachedString(name, text2);
            return text2;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.w("PackageManager", "Failure retrieving resources for " + appInfo.packageName);
            return null;
        } catch (RuntimeException e3) {
            Log.w("PackageManager", "Failure retrieving text 0x" + Integer.toHexString(resid) + " in package " + packageName, e3);
            return null;
        }
    }

    @Override // android.content.pm.PackageManager
    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        if (appInfo == null) {
            try {
                appInfo = getApplicationInfo(packageName, 1024);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
        try {
            return getResourcesForApplication(appInfo).getXml(resid);
        } catch (RuntimeException e2) {
            Log.w("PackageManager", "Failure retrieving xml 0x" + Integer.toHexString(resid) + " in package " + packageName, e2);
            return null;
        } catch (PackageManager.NameNotFoundException e3) {
            Log.w("PackageManager", "Failure retrieving resources for " + appInfo.packageName);
            return null;
        }
    }

    @Override // android.content.pm.PackageManager
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return info.loadLabel(this);
    }

    @Override // android.content.pm.PackageManager
    public int installExistingPackage(String packageName) throws PackageManager.NameNotFoundException {
        return installExistingPackage(packageName, 0);
    }

    @Override // android.content.pm.PackageManager
    public int installExistingPackage(String packageName, int installReason) throws PackageManager.NameNotFoundException {
        return installExistingPackageAsUser(packageName, installReason, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public int installExistingPackageAsUser(String packageName, int userId) throws PackageManager.NameNotFoundException {
        return installExistingPackageAsUser(packageName, 0, userId);
    }

    private int installExistingPackageAsUser(String packageName, int installReason, int userId) throws PackageManager.NameNotFoundException {
        try {
            int res = this.mPM.installExistingPackageAsUser(packageName, userId, 4194304, installReason, null);
            if (res != -3) {
                return res;
            }
            throw new PackageManager.NameNotFoundException("Package " + packageName + " doesn't exist");
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void verifyPendingInstall(int id, int response) {
        try {
            this.mPM.verifyPendingInstall(id, response);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        try {
            this.mPM.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) {
        try {
            this.mPM.verifyIntentFilter(id, verificationCode, failedDomains);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getIntentVerificationStatusAsUser(String packageName, int userId) {
        try {
            return this.mPM.getIntentVerificationStatus(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean updateIntentVerificationStatusAsUser(String packageName, int status, int userId) {
        try {
            return this.mPM.updateIntentVerificationStatus(packageName, status, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
        try {
            ParceledListSlice<IntentFilterVerificationInfo> parceledList = this.mPM.getIntentFilterVerifications(packageName);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public List<IntentFilter> getAllIntentFilters(String packageName) {
        try {
            ParceledListSlice<IntentFilter> parceledList = this.mPM.getAllIntentFilters(packageName);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getDefaultBrowserPackageNameAsUser(int userId) {
        try {
            return this.mPM.getDefaultBrowserPackageName(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
        try {
            return this.mPM.setDefaultBrowserPackageName(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        try {
            this.mPM.setInstallerPackageName(targetPackage, installerPackageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void setUpdateAvailable(String packageName, boolean updateAvailable) {
        try {
            this.mPM.setUpdateAvailable(packageName, updateAvailable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getInstallerPackageName(String packageName) {
        try {
            return this.mPM.getInstallerPackageName(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getMoveStatus(int moveId) {
        try {
            return this.mPM.getMoveStatus(moveId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void registerMoveCallback(PackageManager.MoveCallback callback, Handler handler) {
        synchronized (this.mDelegates) {
            MoveCallbackDelegate delegate = new MoveCallbackDelegate(callback, handler.getLooper());
            try {
                this.mPM.registerMoveCallback(delegate);
                this.mDelegates.add(delegate);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Override // android.content.pm.PackageManager
    public void unregisterMoveCallback(PackageManager.MoveCallback callback) {
        synchronized (this.mDelegates) {
            Iterator<MoveCallbackDelegate> i = this.mDelegates.iterator();
            while (i.hasNext()) {
                MoveCallbackDelegate delegate = i.next();
                if (delegate.mCallback == callback) {
                    try {
                        this.mPM.unregisterMoveCallback(delegate);
                        i.remove();
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    @Override // android.content.pm.PackageManager
    public int movePackage(String packageName, VolumeInfo vol) {
        String volumeUuid;
        try {
            if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.id)) {
                volumeUuid = StorageManager.UUID_PRIVATE_INTERNAL;
            } else if (vol.isPrimaryPhysical()) {
                volumeUuid = StorageManager.UUID_PRIMARY_PHYSICAL;
            } else {
                volumeUuid = (String) Preconditions.checkNotNull(vol.fsUuid);
            }
            return this.mPM.movePackage(packageName, volumeUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    @UnsupportedAppUsage
    public VolumeInfo getPackageCurrentVolume(ApplicationInfo app) {
        return getPackageCurrentVolume(app, (StorageManager) this.mContext.getSystemService(StorageManager.class));
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public VolumeInfo getPackageCurrentVolume(ApplicationInfo app, StorageManager storage) {
        if (app.isInternal()) {
            return storage.findVolumeById(VolumeInfo.ID_PRIVATE_INTERNAL);
        }
        return storage.findVolumeByUuid(app.volumeUuid);
    }

    @Override // android.content.pm.PackageManager
    public List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
        return getPackageCandidateVolumes(app, (StorageManager) this.mContext.getSystemService(StorageManager.class), this.mPM);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app, StorageManager storageManager, IPackageManager pm) {
        VolumeInfo currentVol = getPackageCurrentVolume(app, storageManager);
        List<VolumeInfo> vols = storageManager.getVolumes();
        List<VolumeInfo> candidates = new ArrayList<>();
        for (VolumeInfo vol : vols) {
            if (Objects.equals(vol, currentVol) || isPackageCandidateVolume(this.mContext, app, vol, pm)) {
                candidates.add(vol);
            }
        }
        return candidates;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isForceAllowOnExternal(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.FORCE_ALLOW_ON_EXTERNAL, 0) != 0;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isAllow3rdPartyOnInternal(Context context) {
        return context.getResources().getBoolean(R.bool.config_allow3rdPartyAppOnInternal);
    }

    private boolean isPackageCandidateVolume(ContextImpl context, ApplicationInfo app, VolumeInfo vol, IPackageManager pm) {
        boolean forceAllowOnExternal = isForceAllowOnExternal(context);
        if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.getId())) {
            return app.isSystemApp() || isAllow3rdPartyOnInternal(context);
        }
        if (app.isSystemApp()) {
            return false;
        }
        if ((!forceAllowOnExternal && (app.installLocation == 1 || app.installLocation == -1)) || !vol.isMountedWritable()) {
            return false;
        }
        if (vol.isPrimaryPhysical()) {
            return app.isInternal();
        }
        try {
            if (pm.isPackageDeviceAdminOnAnyUser(app.packageName)) {
                return false;
            }
            return vol.getType() == 1;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int movePrimaryStorage(VolumeInfo vol) {
        String volumeUuid;
        try {
            if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.id)) {
                volumeUuid = StorageManager.UUID_PRIVATE_INTERNAL;
            } else if (vol.isPrimaryPhysical()) {
                volumeUuid = StorageManager.UUID_PRIMARY_PHYSICAL;
            } else {
                volumeUuid = (String) Preconditions.checkNotNull(vol.fsUuid);
            }
            return this.mPM.movePrimaryStorage(volumeUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public VolumeInfo getPrimaryStorageCurrentVolume() {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        return storage.findVolumeByQualifiedUuid(storage.getPrimaryStorageUuid());
    }

    @Override // android.content.pm.PackageManager
    public List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        VolumeInfo currentVol = getPrimaryStorageCurrentVolume();
        List<VolumeInfo> vols = storage.getVolumes();
        List<VolumeInfo> candidates = new ArrayList<>();
        if (!Objects.equals(StorageManager.UUID_PRIMARY_PHYSICAL, storage.getPrimaryStorageUuid()) || currentVol == null) {
            for (VolumeInfo vol : vols) {
                if (Objects.equals(vol, currentVol) || isPrimaryStorageCandidateVolume(vol)) {
                    candidates.add(vol);
                }
            }
        } else {
            candidates.add(currentVol);
        }
        return candidates;
    }

    private static boolean isPrimaryStorageCandidateVolume(VolumeInfo vol) {
        if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.getId())) {
            return true;
        }
        if (!vol.isMountedWritable()) {
            return false;
        }
        if (vol.getType() == 1) {
            return true;
        }
        return false;
    }

    @Override // android.content.pm.PackageManager
    @UnsupportedAppUsage
    public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
        deletePackageAsUser(packageName, observer, flags, getUserId());
    }

    @Override // android.content.pm.PackageManager
    public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int flags, int userId) {
        try {
            this.mPM.deletePackageAsUser(packageName, -1, observer, userId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void clearApplicationUserData(String packageName, IPackageDataObserver observer) {
        try {
            this.mPM.clearApplicationUserData(packageName, observer, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
        try {
            this.mPM.deleteApplicationCacheFiles(packageName, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) {
        try {
            this.mPM.deleteApplicationCacheFilesAsUser(packageName, userId, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void freeStorageAndNotify(String volumeUuid, long idealStorageSize, IPackageDataObserver observer) {
        try {
            this.mPM.freeStorageAndNotify(volumeUuid, idealStorageSize, 0, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {
        try {
            this.mPM.freeStorage(volumeUuid, freeStorageSize, 0, pi);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String[] setDistractingPackageRestrictions(String[] packages, int distractionFlags) {
        try {
            return this.mPM.setDistractingPackageRestrictionsAsUser(packages, distractionFlags, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, PersistableBundle appExtras, PersistableBundle launcherExtras, String dialogMessage) {
        SuspendDialogInfo dialogInfo;
        if (!TextUtils.isEmpty(dialogMessage)) {
            dialogInfo = new SuspendDialogInfo.Builder().setMessage(dialogMessage).build();
        } else {
            dialogInfo = null;
        }
        return setPackagesSuspended(packageNames, suspended, appExtras, launcherExtras, dialogInfo);
    }

    @Override // android.content.pm.PackageManager
    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, PersistableBundle appExtras, PersistableBundle launcherExtras, SuspendDialogInfo dialogInfo) {
        try {
            return this.mPM.setPackagesSuspendedAsUser(packageNames, suspended, appExtras, launcherExtras, dialogInfo, this.mContext.getOpPackageName(), getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public String[] getUnsuspendablePackages(String[] packageNames) {
        try {
            return this.mPM.getUnsuspendablePackagesForUser(packageNames, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public Bundle getSuspendedPackageAppExtras() {
        try {
            PersistableBundle extras = this.mPM.getSuspendedPackageAppExtras(this.mContext.getOpPackageName(), getUserId());
            if (extras != null) {
                return new Bundle(extras.deepCopy());
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isPackageSuspendedForUser(String packageName, int userId) {
        try {
            return this.mPM.isPackageSuspendedForUser(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isPackageSuspended(String packageName) throws PackageManager.NameNotFoundException {
        try {
            return isPackageSuspendedForUser(packageName, getUserId());
        } catch (IllegalArgumentException e) {
            throw new PackageManager.NameNotFoundException(packageName);
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isPackageSuspended() {
        return isPackageSuspendedForUser(this.mContext.getOpPackageName(), getUserId());
    }

    @Override // android.content.pm.PackageManager
    public void setApplicationCategoryHint(String packageName, int categoryHint) {
        try {
            this.mPM.setApplicationCategoryHint(packageName, categoryHint, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    @UnsupportedAppUsage
    public void getPackageSizeInfoAsUser(String packageName, int userHandle, IPackageStatsObserver observer) {
        if (this.mContext.getApplicationInfo().targetSdkVersion >= 26) {
            throw new UnsupportedOperationException("Shame on you for calling the hidden API getPackageSizeInfoAsUser(). Shame!");
        } else if (observer != null) {
            Log.d(TAG, "Shame on you for calling the hidden API getPackageSizeInfoAsUser(). Shame!");
            try {
                observer.onGetStatsCompleted(null, false);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.content.pm.PackageManager
    public void addPackageToPreferred(String packageName) {
        Log.w(TAG, "addPackageToPreferred() is a no-op");
    }

    @Override // android.content.pm.PackageManager
    public void removePackageFromPreferred(String packageName) {
        Log.w(TAG, "removePackageFromPreferred() is a no-op");
    }

    @Override // android.content.pm.PackageManager
    public List<PackageInfo> getPreferredPackages(int flags) {
        Log.w(TAG, "getPreferredPackages() is a no-op");
        return Collections.emptyList();
    }

    @Override // android.content.pm.PackageManager
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        try {
            this.mPM.addPreferredActivity(filter, match, set, activity, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void addPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        try {
            this.mPM.addPreferredActivity(filter, match, set, activity, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        try {
            this.mPM.replacePreferredActivity(filter, match, set, activity, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void replacePreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        try {
            this.mPM.replacePreferredActivity(filter, match, set, activity, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void clearPackagePreferredActivities(String packageName) {
        try {
            this.mPM.clearPackagePreferredActivities(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        try {
            return this.mPM.getPreferredActivities(outFilters, outActivities, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
        try {
            return this.mPM.getHomeActivities(outActivities);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void setSyntheticAppDetailsActivityEnabled(String packageName, boolean enabled) {
        int i;
        try {
            ComponentName componentName = new ComponentName(packageName, PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME);
            IPackageManager iPackageManager = this.mPM;
            if (enabled) {
                i = 0;
            } else {
                i = 2;
            }
            iPackageManager.setComponentEnabledSetting(componentName, i, 1, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean getSyntheticAppDetailsActivityEnabled(String packageName) {
        try {
            int state = this.mPM.getComponentEnabledSetting(new ComponentName(packageName, PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME), getUserId());
            return state == 1 || state == 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        try {
            this.mPM.setComponentEnabledSetting(componentName, newState, flags, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getComponentEnabledSetting(ComponentName componentName) {
        try {
            return this.mPM.getComponentEnabledSetting(componentName, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        try {
            this.mPM.setApplicationEnabledSetting(packageName, newState, flags, getUserId(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public int getApplicationEnabledSetting(String packageName) {
        try {
            return this.mPM.getApplicationEnabledSetting(packageName, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void flushPackageRestrictionsAsUser(int userId) {
        try {
            this.mPM.flushPackageRestrictionsAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle user) {
        try {
            return this.mPM.setApplicationHiddenSettingAsUser(packageName, hidden, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
        try {
            return this.mPM.getApplicationHiddenSettingAsUser(packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public KeySet getKeySetByAlias(String packageName, String alias) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(alias);
        try {
            return this.mPM.getKeySetByAlias(packageName, alias);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public KeySet getSigningKeySet(String packageName) {
        Preconditions.checkNotNull(packageName);
        try {
            return this.mPM.getSigningKeySet(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isSignedBy(String packageName, KeySet ks) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(ks);
        try {
            return this.mPM.isPackageSignedByKeySet(packageName, ks);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isSignedByExactly(String packageName, KeySet ks) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(ks);
        try {
            return this.mPM.isPackageSignedByKeySetExactly(packageName, ks);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public VerifierDeviceIdentity getVerifierDeviceIdentity() {
        try {
            return this.mPM.getVerifierDeviceIdentity();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isUpgrade() {
        return isDeviceUpgrading();
    }

    @Override // android.content.pm.PackageManager
    public boolean isDeviceUpgrading() {
        try {
            return this.mPM.isDeviceUpgrading();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public PackageInstaller getPackageInstaller() {
        PackageInstaller packageInstaller;
        synchronized (this.mLock) {
            if (this.mInstaller == null) {
                try {
                    this.mInstaller = new PackageInstaller(this.mPM.getPackageInstaller(), this.mContext.getPackageName(), getUserId());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            packageInstaller = this.mInstaller;
        }
        return packageInstaller;
    }

    @Override // android.content.pm.PackageManager
    public boolean isPackageAvailable(String packageName) {
        try {
            return this.mPM.isPackageAvailable(packageName, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {
        try {
            this.mPM.addCrossProfileIntentFilter(filter, this.mContext.getOpPackageName(), sourceUserId, targetUserId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public void clearCrossProfileIntentFilters(int sourceUserId) {
        try {
            this.mPM.clearCrossProfileIntentFilters(sourceUserId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        Drawable dr = loadUnbadgedItemIcon(itemInfo, appInfo);
        if (itemInfo.showUserIcon != -10000) {
            return dr;
        }
        return getUserBadgedIcon(dr, new UserHandle(getUserId()));
    }

    @Override // android.content.pm.PackageManager
    public Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        if (!(itemInfo instanceof PermissionGroupInfo)) {
            HwThemeManager.updateIconCache(itemInfo, itemInfo.name, itemInfo.packageName, itemInfo.icon, appInfo == null ? 0 : appInfo.icon);
        }
        if (itemInfo.showUserIcon != -10000) {
            return UserIcons.getDefaultUserIcon(this.mContext.getResources(), itemInfo.showUserIcon, false);
        }
        Drawable dr = null;
        if (itemInfo.packageName != null) {
            dr = getDrawable(itemInfo.packageName, itemInfo.icon, appInfo);
        }
        if (!(dr != null || itemInfo == appInfo || appInfo == null)) {
            dr = loadUnbadgedItemIcon(appInfo, appInfo);
        }
        if (dr == null) {
            return itemInfo.loadDefaultIcon(this);
        }
        return dr;
    }

    private Drawable getBadgedDrawable(Drawable drawable, Drawable badgeDrawable, Rect badgeLocation, boolean tryBadgeInPlace) {
        Bitmap bitmap;
        int badgedWidth = drawable.getIntrinsicWidth();
        int badgedHeight = drawable.getIntrinsicHeight();
        boolean canBadgeInPlace = tryBadgeInPlace && (drawable instanceof BitmapDrawable) && ((BitmapDrawable) drawable).getBitmap().isMutable();
        if (canBadgeInPlace) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = Bitmap.createBitmap(badgedWidth, badgedHeight, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        if (!canBadgeInPlace) {
            drawable.setBounds(0, 0, badgedWidth, badgedHeight);
            drawable.draw(canvas);
        }
        if (badgeLocation == null) {
            badgeDrawable.setBounds(0, 0, badgedWidth, badgedHeight);
            badgeDrawable.draw(canvas);
        } else if (badgeLocation.left < 0 || badgeLocation.top < 0 || badgeLocation.width() > badgedWidth || badgeLocation.height() > badgedHeight) {
            throw new IllegalArgumentException("Badge location " + badgeLocation + " not in badged drawable bounds " + new Rect(0, 0, badgedWidth, badgedHeight));
        } else {
            badgeDrawable.setBounds(0, 0, badgeLocation.width(), badgeLocation.height());
            canvas.save();
            canvas.translate((float) badgeLocation.left, (float) badgeLocation.top);
            badgeDrawable.draw(canvas);
            canvas.restore();
        }
        if (canBadgeInPlace) {
            return drawable;
        }
        BitmapDrawable mergedDrawable = new BitmapDrawable(this.mContext.getResources(), bitmap);
        if (drawable instanceof BitmapDrawable) {
            mergedDrawable.setTargetDensity(((BitmapDrawable) drawable).getBitmap().getDensity());
        }
        return mergedDrawable;
    }

    private boolean isManagedProfile(int userId) {
        return getUserManager().isManagedProfile(userId);
    }

    @Override // android.content.pm.PackageManager
    public int getInstallReason(String packageName, UserHandle user) {
        try {
            return this.mPM.getInstallReason(packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static class MoveCallbackDelegate extends IPackageMoveObserver.Stub implements Handler.Callback {
        private static final int MSG_CREATED = 1;
        private static final int MSG_STATUS_CHANGED = 2;
        final PackageManager.MoveCallback mCallback;
        final Handler mHandler;

        public MoveCallbackDelegate(PackageManager.MoveCallback callback, Looper looper) {
            this.mCallback = callback;
            this.mHandler = new Handler(looper, this);
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                SomeArgs args = (SomeArgs) msg.obj;
                this.mCallback.onCreated(args.argi1, (Bundle) args.arg2);
                args.recycle();
                return true;
            } else if (i != 2) {
                return false;
            } else {
                SomeArgs args2 = (SomeArgs) msg.obj;
                this.mCallback.onStatusChanged(args2.argi1, args2.argi2, ((Long) args2.arg3).longValue());
                args2.recycle();
                return true;
            }
        }

        @Override // android.content.pm.IPackageMoveObserver
        public void onCreated(int moveId, Bundle extras) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = moveId;
            args.arg2 = extras;
            this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        @Override // android.content.pm.IPackageMoveObserver
        public void onStatusChanged(int moveId, int status, long estMillis) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = moveId;
            args.argi2 = status;
            args.arg3 = Long.valueOf(estMillis);
            this.mHandler.obtainMessage(2, args).sendToTarget();
        }
    }

    public class OnPermissionsChangeListenerDelegate extends IOnPermissionsChangeListener.Stub implements Handler.Callback {
        private static final int MSG_PERMISSIONS_CHANGED = 1;
        private final Handler mHandler;
        private final PackageManager.OnPermissionsChangedListener mListener;

        public OnPermissionsChangeListenerDelegate(PackageManager.OnPermissionsChangedListener listener, Looper looper) {
            this.mListener = listener;
            this.mHandler = new Handler(looper, this);
        }

        @Override // android.content.pm.IOnPermissionsChangeListener
        public void onPermissionsChanged(int uid) {
            this.mHandler.obtainMessage(1, uid, 0).sendToTarget();
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            if (msg.what != 1) {
                return false;
            }
            this.mListener.onPermissionsChanged(msg.arg1);
            return true;
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean canRequestPackageInstalls() {
        try {
            return this.mPM.canRequestPackageInstalls(this.mContext.getPackageName(), getUserId());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public ComponentName getInstantAppResolverSettingsComponent() {
        try {
            return this.mPM.getInstantAppResolverSettingsComponent();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public ComponentName getInstantAppInstallerComponent() {
        try {
            return this.mPM.getInstantAppInstallerComponent();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getInstantAppAndroidId(String packageName, UserHandle user) {
        try {
            return this.mPM.getInstantAppAndroidId(packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    private static class DexModuleRegisterResult {
        final String dexModulePath;
        final String message;
        final boolean success;

        private DexModuleRegisterResult(String dexModulePath2, boolean success2, String message2) {
            this.dexModulePath = dexModulePath2;
            this.success = success2;
            this.message = message2;
        }
    }

    private static class DexModuleRegisterCallbackDelegate extends IDexModuleRegisterCallback.Stub implements Handler.Callback {
        private static final int MSG_DEX_MODULE_REGISTERED = 1;
        private final PackageManager.DexModuleRegisterCallback callback;
        private final Handler mHandler = new Handler(Looper.getMainLooper(), this);

        DexModuleRegisterCallbackDelegate(PackageManager.DexModuleRegisterCallback callback2) {
            this.callback = callback2;
        }

        @Override // android.content.pm.IDexModuleRegisterCallback
        public void onDexModuleRegistered(String dexModulePath, boolean success, String message) throws RemoteException {
            this.mHandler.obtainMessage(1, new DexModuleRegisterResult(dexModulePath, success, message)).sendToTarget();
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            if (msg.what != 1) {
                return false;
            }
            DexModuleRegisterResult result = (DexModuleRegisterResult) msg.obj;
            this.callback.onDexModuleRegistered(result.dexModulePath, result.success, result.message);
            return true;
        }
    }

    @Override // android.content.pm.PackageManager
    public void registerDexModule(String dexModule, PackageManager.DexModuleRegisterCallback callback) {
        boolean isSharedModule = false;
        try {
            if ((OsConstants.S_IROTH & Os.stat(dexModule).st_mode) != 0) {
                isSharedModule = true;
            }
            DexModuleRegisterCallbackDelegate callbackDelegate = null;
            if (callback != null) {
                callbackDelegate = new DexModuleRegisterCallbackDelegate(callback);
            }
            try {
                this.mPM.registerDexModule(this.mContext.getPackageName(), dexModule, isSharedModule, callbackDelegate);
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        } catch (ErrnoException e2) {
            callback.onDexModuleRegistered(dexModule, false, "Could not get stat the module file: " + e2.getMessage());
        }
    }

    @Override // android.content.pm.PackageManager
    public CharSequence getHarmfulAppWarning(String packageName) {
        try {
            return this.mPM.getHarmfulAppWarning(packageName, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public void setHarmfulAppWarning(String packageName, CharSequence warning) {
        try {
            this.mPM.setHarmfulAppWarning(packageName, warning, getUserId());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public ArtManager getArtManager() {
        ArtManager artManager;
        synchronized (this.mLock) {
            if (this.mArtManager == null) {
                try {
                    this.mArtManager = new ArtManager(this.mContext, this.mPM.getArtManager());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            artManager = this.mArtManager;
        }
        return artManager;
    }

    @Override // android.content.pm.PackageManager
    public String getSystemTextClassifierPackageName() {
        try {
            return this.mPM.getSystemTextClassifierPackageName();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getAttentionServicePackageName() {
        try {
            return this.mPM.getAttentionServicePackageName();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getWellbeingPackageName() {
        try {
            return this.mPM.getWellbeingPackageName();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getAppPredictionServicePackageName() {
        try {
            return this.mPM.getAppPredictionServicePackageName();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getSystemCaptionsServicePackageName() {
        try {
            return this.mPM.getSystemCaptionsServicePackageName();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public String getIncidentReportApproverPackageName() {
        try {
            return this.mPM.getIncidentReportApproverPackageName();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isPackageStateProtected(String packageName, int userId) {
        try {
            return this.mPM.isPackageStateProtected(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @Override // android.content.pm.PackageManager
    public void sendDeviceCustomizationReadyBroadcast() {
        try {
            this.mPM.sendDeviceCustomizationReadyBroadcast();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void removeCacheIcon(String packageName, int resid) {
        synchronized (sSync) {
            sIconCache.remove(new ResourceName(packageName, resid));
        }
    }

    private boolean isClonedProfile(int userId) {
        if (userId == 0) {
            return false;
        }
        try {
            return getUserManager().getUserInfo(userId).isClonedProfile();
        } catch (Exception e) {
            return false;
        }
    }
}
