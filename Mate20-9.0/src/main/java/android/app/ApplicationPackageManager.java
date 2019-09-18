package android.app;

import android.common.HwFrameworkFactory;
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
import android.hsm.HwSystemManager;
import android.hwtheme.HwThemeManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.ArrayMap;
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
import libcore.util.EmptyArray;

public class ApplicationPackageManager extends PackageManager {
    @VisibleForTesting
    public static final int[] CORP_BADGE_LABEL_RES_ID = {R.string.managed_profile_label_badge, R.string.managed_profile_label_badge_2, R.string.managed_profile_label_badge_3};
    private static final boolean DEBUG_ICONS = false;
    private static final int DEFAULT_EPHEMERAL_COOKIE_MAX_SIZE_BYTES = 16384;
    private static final String TAG = "ApplicationPackageManager";
    private static boolean USE_CACHE = false;
    private static HwCustApplicationPackageManager mHwCustApplicationPackageManager = ((HwCustApplicationPackageManager) HwCustUtils.createObj(HwCustApplicationPackageManager.class, new Object[0]));
    private static final int sDefaultFlags = 1024;
    private static ArrayMap<ResourceName, WeakReference<Drawable.ConstantState>> sIconCache = new ArrayMap<>();
    private static ArrayMap<ResourceName, WeakReference<CharSequence>> sStringCache = new ArrayMap<>();
    private static final Object sSync = new Object();
    @GuardedBy("mLock")
    private ArtManager mArtManager;
    volatile int mCachedSafeMode = -1;
    private final ContextImpl mContext;
    @GuardedBy("mDelegates")
    private final ArrayList<MoveCallbackDelegate> mDelegates = new ArrayList<>();
    @GuardedBy("mFeaturesCache")
    private ArrayMap<String, Boolean> mFeaturesCache = new ArrayMap<>();
    @GuardedBy("mLock")
    private PackageInstaller mInstaller;
    private final Object mLock = new Object();
    private final IPackageManager mPM;
    private final Map<PackageManager.OnPermissionsChangedListener, IOnPermissionsChangeListener> mPermissionListeners = new ArrayMap();
    @GuardedBy("mLock")
    private String mPermissionsControllerPackageName;
    @GuardedBy("mLock")
    private UserManager mUserManager;

    private static class DexModuleRegisterCallbackDelegate extends IDexModuleRegisterCallback.Stub implements Handler.Callback {
        private static final int MSG_DEX_MODULE_REGISTERED = 1;
        private final PackageManager.DexModuleRegisterCallback callback;
        private final Handler mHandler = new Handler(Looper.getMainLooper(), this);

        DexModuleRegisterCallbackDelegate(PackageManager.DexModuleRegisterCallback callback2) {
            this.callback = callback2;
        }

        public void onDexModuleRegistered(String dexModulePath, boolean success, String message) throws RemoteException {
            this.mHandler.obtainMessage(1, new DexModuleRegisterResult(dexModulePath, success, message)).sendToTarget();
        }

        public boolean handleMessage(Message msg) {
            if (msg.what != 1) {
                return false;
            }
            DexModuleRegisterResult result = (DexModuleRegisterResult) msg.obj;
            this.callback.onDexModuleRegistered(result.dexModulePath, result.success, result.message);
            return true;
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

    private static class MoveCallbackDelegate extends IPackageMoveObserver.Stub implements Handler.Callback {
        private static final int MSG_CREATED = 1;
        private static final int MSG_STATUS_CHANGED = 2;
        final PackageManager.MoveCallback mCallback;
        final Handler mHandler;

        public MoveCallbackDelegate(PackageManager.MoveCallback callback, Looper looper) {
            this.mCallback = callback;
            this.mHandler = new Handler(looper, this);
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SomeArgs args = (SomeArgs) msg.obj;
                    this.mCallback.onCreated(args.argi1, (Bundle) args.arg2);
                    args.recycle();
                    return true;
                case 2:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    this.mCallback.onStatusChanged(args2.argi1, args2.argi2, ((Long) args2.arg3).longValue());
                    args2.recycle();
                    return true;
                default:
                    return false;
            }
        }

        public void onCreated(int moveId, Bundle extras) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = moveId;
            args.arg2 = extras;
            this.mHandler.obtainMessage(1, args).sendToTarget();
        }

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

        public void onPermissionsChanged(int uid) {
            this.mHandler.obtainMessage(1, uid, 0).sendToTarget();
        }

        public boolean handleMessage(Message msg) {
            if (msg.what != 1) {
                return false;
            }
            this.mListener.onPermissionsChanged(msg.arg1);
            return true;
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
            boolean z = true;
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
            if (this.packageName == null ? that.packageName != null : !this.packageName.equals(that.packageName)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (31 * this.packageName.hashCode()) + this.iconId;
        }

        public String toString() {
            return "{ResourceName " + this.packageName + " / " + this.iconId + "}";
        }
    }

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

    public int getUserId() {
        return this.mContext.getUserId();
    }

    public PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return getPackageInfoAsUser(packageName, flags, this.mContext.getUserId());
    }

    public PackageInfo getPackageInfo(VersionedPackage versionedPackage, int flags) throws PackageManager.NameNotFoundException {
        try {
            PackageInfo pi = this.mPM.getPackageInfoVersioned(versionedPackage, flags, this.mContext.getUserId());
            if (pi != null) {
                return pi;
            }
            throw new PackageManager.NameNotFoundException(versionedPackage.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        try {
            PackageInfo pi = HwFrameworkFactory.getHwApiCacheManagerEx().getPackageInfoAsUser(this.mPM, packageName, flags, userId);
            if (pi != null) {
                return pi;
            }
            throw new PackageManager.NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] currentToCanonicalPackageNames(String[] names) {
        try {
            return this.mPM.currentToCanonicalPackageNames(names);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] canonicalToCurrentPackageNames(String[] names) {
        try {
            return this.mPM.canonicalToCurrentPackageNames(names);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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
        if (ris.get(0) == null || ris.get(0).activityInfo == null) {
            Log.w(TAG, "getLaunchIntentForPackage " + packageName + " activityInfo is null, return!");
            return null;
        }
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }

    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        return getLaunchIntentForPackageAndCategory(packageName, Intent.CATEGORY_LEANBACK_LAUNCHER);
    }

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

    public int[] getPackageGids(String packageName) throws PackageManager.NameNotFoundException {
        return getPackageGids(packageName, 0);
    }

    public int[] getPackageGids(String packageName, int flags) throws PackageManager.NameNotFoundException {
        try {
            int[] gids = this.mPM.getPackageGids(packageName, flags, this.mContext.getUserId());
            if (gids != null) {
                return gids;
            }
            throw new PackageManager.NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPackageUid(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return getPackageUidAsUser(packageName, flags, this.mContext.getUserId());
    }

    public int getPackageUidAsUser(String packageName, int userId) throws PackageManager.NameNotFoundException {
        return getPackageUidAsUser(packageName, 0, userId);
    }

    public int getPackageUidAsUser(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        try {
            int uid = HwFrameworkFactory.getHwApiCacheManagerEx().getPackageUidAsUser(this.mPM, packageName, flags, userId);
            if (uid >= 0) {
                return uid;
            }
            throw new PackageManager.NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws PackageManager.NameNotFoundException {
        try {
            ParceledListSlice<PermissionInfo> parceledList = this.mPM.queryPermissionsByGroup(group, flags);
            if (parceledList != null) {
                List<PermissionInfo> pi = parceledList.getList();
                if (pi != null) {
                    return pi;
                }
            }
            throw new PackageManager.NameNotFoundException(group);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPermissionReviewModeEnabled() {
        return this.mContext.getResources().getBoolean(R.bool.config_permissionReviewRequired);
    }

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

    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return getApplicationInfoAsUser(packageName, flags, this.mContext.getUserId());
    }

    public ApplicationInfo getApplicationInfoAsUser(String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        try {
            ApplicationInfo ai = HwFrameworkFactory.getHwApiCacheManagerEx().getApplicationInfoAsUser(this.mPM, packageName, flags, userId);
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

    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        try {
            ActivityInfo ai = this.mPM.getActivityInfo(className, flags, this.mContext.getUserId());
            if (ai != null) {
                return ai;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        try {
            ActivityInfo ai = this.mPM.getReceiverInfo(className, flags, this.mContext.getUserId());
            if (ai != null) {
                return ai;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        try {
            ServiceInfo si = this.mPM.getServiceInfo(className, flags, this.mContext.getUserId());
            if (si != null) {
                return si;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
        try {
            ProviderInfo pi = this.mPM.getProviderInfo(className, flags, this.mContext.getUserId());
            if (pi != null) {
                return pi;
            }
            throw new PackageManager.NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getSystemSharedLibraryNames() {
        try {
            return this.mPM.getSystemSharedLibraryNames();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<SharedLibraryInfo> getSharedLibraries(int flags) {
        return getSharedLibrariesAsUser(flags, this.mContext.getUserId());
    }

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

    public String getServicesSystemSharedLibraryPackageName() {
        try {
            return this.mPM.getServicesSystemSharedLibraryPackageName();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getSharedSystemSharedLibraryPackageName() {
        try {
            return this.mPM.getSharedSystemSharedLibraryPackageName();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ChangedPackages getChangedPackages(int sequenceNumber) {
        try {
            return this.mPM.getChangedPackages(sequenceNumber, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public boolean hasSystemFeature(String name) {
        return hasSystemFeature(name, 0);
    }

    public boolean hasSystemFeature(String name, int version) {
        try {
            if (!USE_CACHE) {
                return this.mPM.hasSystemFeature(name, version);
            }
            String feature = name + version;
            synchronized (this.mFeaturesCache) {
                Boolean result = this.mFeaturesCache.get(feature);
                if (result != null) {
                    boolean booleanValue = result.booleanValue();
                    return booleanValue;
                }
                Boolean result2 = Boolean.valueOf(this.mPM.hasSystemFeature(name, version));
                this.mFeaturesCache.put(feature, result2);
                boolean booleanValue2 = result2.booleanValue();
                return booleanValue2;
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkPermission(String permName, String pkgName) {
        try {
            return this.mPM.checkPermission(permName, pkgName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        try {
            return this.mPM.isPermissionRevokedByPolicy(permName, pkgName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public boolean addPermission(PermissionInfo info) {
        try {
            return this.mPM.addPermission(info);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean addPermissionAsync(PermissionInfo info) {
        try {
            return this.mPM.addPermissionAsync(info);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removePermission(String name) {
        try {
            this.mPM.removePermission(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void grantRuntimePermission(String packageName, String permissionName, UserHandle user) {
        try {
            this.mPM.grantRuntimePermission(packageName, permissionName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void revokeRuntimePermission(String packageName, String permissionName, UserHandle user) {
        try {
            this.mPM.revokeRuntimePermission(packageName, permissionName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
        try {
            return this.mPM.getPermissionFlags(permissionName, packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {
        try {
            this.mPM.updatePermissionFlags(permissionName, packageName, flagMask, flagValues, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean shouldShowRequestPermissionRationale(String permission) {
        try {
            return this.mPM.shouldShowRequestPermissionRationale(permission, this.mContext.getPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkSignatures(String pkg1, String pkg2) {
        try {
            return this.mPM.checkSignatures(pkg1, pkg2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkSignatures(int uid1, int uid2) {
        try {
            return this.mPM.checkUidSignatures(uid1, uid2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasSigningCertificate(String packageName, byte[] certificate, int type) {
        try {
            return this.mPM.hasSigningCertificate(packageName, certificate, type);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasSigningCertificate(int uid, byte[] certificate, int type) {
        try {
            return this.mPM.hasUidSigningCertificate(uid, certificate, type);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getPackagesForUid(int uid) {
        try {
            return this.mPM.getPackagesForUid(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getNameForUid(int uid) {
        try {
            return this.mPM.getNameForUid(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getNamesForUids(int[] uids) {
        try {
            return this.mPM.getNamesForUids(uids);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public List<PackageInfo> getInstalledPackages(int flags) {
        return getInstalledPackagesAsUser(flags, this.mContext.getUserId());
    }

    public List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
        try {
            ParceledListSlice<PackageInfo> slice = this.mPM.getInstalledPackages(flags, userId);
            if (slice == null) {
                return Collections.emptyList();
            }
            if (HwSystemManager.allowOp(33554432)) {
                return slice.getList();
            }
            Log.d(TAG, "getInstalledPackages blocked, userId " + userId);
            return HwSystemManager.getFakePackages(slice.getList());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        try {
            ParceledListSlice<PackageInfo> parceledList = this.mPM.getPackagesHoldingPermissions(permissions, flags, this.mContext.getUserId());
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return getInstalledApplicationsAsUser(flags, this.mContext.getUserId());
    }

    public List<ApplicationInfo> getInstalledApplicationsAsUser(int flags, int userId) {
        try {
            ParceledListSlice<ApplicationInfo> slice = this.mPM.getInstalledApplications(flags, userId);
            if (slice == null) {
                return Collections.emptyList();
            }
            if (HwSystemManager.allowOp(33554432)) {
                return slice.getList();
            }
            Log.d(TAG, "getInstalledApplications blocked");
            return HwSystemManager.getFakeApplications(slice.getList());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InstantAppInfo> getInstantApps() {
        try {
            ParceledListSlice<InstantAppInfo> slice = this.mPM.getInstantApps(this.mContext.getUserId());
            if (slice != null) {
                return slice.getList();
            }
            return Collections.emptyList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Drawable getInstantAppIcon(String packageName) {
        try {
            Bitmap bitmap = this.mPM.getInstantAppIcon(packageName, this.mContext.getUserId());
            if (bitmap != null) {
                return new BitmapDrawable((Resources) null, bitmap);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isInstantApp() {
        return isInstantApp(this.mContext.getPackageName());
    }

    public boolean isInstantApp(String packageName) {
        try {
            return this.mPM.isInstantApp(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getInstantAppCookieMaxBytes() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "ephemeral_cookie_max_size_bytes", 16384);
    }

    public int getInstantAppCookieMaxSize() {
        return getInstantAppCookieMaxBytes();
    }

    public byte[] getInstantAppCookie() {
        try {
            byte[] cookie = this.mPM.getInstantAppCookie(this.mContext.getPackageName(), this.mContext.getUserId());
            if (cookie != null) {
                return cookie;
            }
            return EmptyArray.BYTE;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearInstantAppCookie() {
        updateInstantAppCookie(null);
    }

    public void updateInstantAppCookie(byte[] cookie) {
        if (cookie == null || cookie.length <= getInstantAppCookieMaxBytes()) {
            try {
                this.mPM.setInstantAppCookie(this.mContext.getPackageName(), cookie, this.mContext.getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("instant cookie longer than " + getInstantAppCookieMaxBytes());
        }
    }

    public boolean setInstantAppCookie(byte[] cookie) {
        try {
            return this.mPM.setInstantAppCookie(this.mContext.getPackageName(), cookie, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ResolveInfo resolveActivity(Intent intent, int flags) {
        return resolveActivityAsUser(intent, flags, this.mContext.getUserId());
    }

    public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
        try {
            if (!"com.google.android.gsf.GOOGLE_APPS_LOCATION_SETTINGS".equals(intent.getAction()) || !this.mContext.getPackageName().equals("com.google.android.xts.location")) {
                return this.mPM.resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        return queryIntentActivitiesAsUser(intent, flags, this.mContext.getUserId());
    }

    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            List<ResolveInfo> list = parceledList.getList();
            if (mHwCustApplicationPackageManager != null) {
                mHwCustApplicationPackageManager.removeMmsResolveInfo(list, intent, this.mContext);
            }
            if (!intent.hasCategory(Intent.CATEGORY_LAUNCHER) || HwSystemManager.allowOp(33554432)) {
                return list;
            }
            Log.d(TAG, "queryIntentActivitiesAsUser blocked, " + intent);
            return HwSystemManager.getFakeResolveInfoList(list);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        Intent[] intentArr = specifics;
        Intent intent2 = intent;
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] specificTypes = null;
        if (intentArr != null) {
            int N = intentArr.length;
            for (int i = 0; i < N; i++) {
                Intent sp = intentArr[i];
                if (sp != null) {
                    String t = sp.resolveTypeIfNeeded(resolver);
                    if (t != null) {
                        if (specificTypes == null) {
                            specificTypes = new String[N];
                        }
                        specificTypes[i] = t;
                    }
                }
            }
        }
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentActivityOptions(caller, intentArr, specificTypes, intent2, intent2.resolveTypeIfNeeded(resolver), flags, this.mContext.getUserId());
            if (parceledList == null) {
                return Collections.emptyList();
            }
            List<ResolveInfo> list = parceledList.getList();
            if (!intent2.hasCategory(Intent.CATEGORY_LAUNCHER) || HwSystemManager.allowOp(33554432)) {
                return list;
            }
            Log.d(TAG, "queryIntentActivityOptions blocked, " + intent2);
            return HwSystemManager.getFakeResolveInfoList(list);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentReceivers(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            List<ResolveInfo> list = parceledList.getList();
            if (HwSystemManager.allowOp(33554432)) {
                return list;
            }
            Log.d(TAG, "queryBroadcastReceivers blocked, " + intent);
            return HwSystemManager.getFakeResolveInfoList(list);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return queryBroadcastReceiversAsUser(intent, flags, this.mContext.getUserId());
    }

    public ResolveInfo resolveServiceAsUser(Intent intent, int flags, int userId) {
        try {
            return this.mPM.resolveService(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ResolveInfo resolveService(Intent intent, int flags) {
        return resolveServiceAsUser(intent, flags, this.mContext.getUserId());
    }

    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return queryIntentServicesAsUser(intent, flags, this.mContext.getUserId());
    }

    public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentContentProviders(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return queryIntentContentProvidersAsUser(intent, flags, this.mContext.getUserId());
    }

    public ProviderInfo resolveContentProvider(String name, int flags) {
        return resolveContentProviderAsUser(name, flags, this.mContext.getUserId());
    }

    public ProviderInfo resolveContentProviderAsUser(String name, int flags, int userId) {
        try {
            return this.mPM.resolveContentProvider(name, flags, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return queryContentProviders(processName, uid, flags, null);
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, String metaDataKey) {
        try {
            ParceledListSlice<ProviderInfo> slice = this.mPM.queryContentProviders(processName, uid, flags, metaDataKey);
            return slice != null ? slice.getList() : Collections.emptyList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public Drawable getActivityIcon(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadIcon(this);
    }

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

    public Drawable getDefaultActivityIcon() {
        return Resources.getSystem().getDrawable(17301651);
    }

    public Drawable getApplicationIcon(ApplicationInfo info) {
        return info.loadIcon(this);
    }

    public Drawable getApplicationIcon(String packageName) throws PackageManager.NameNotFoundException {
        return getApplicationIcon(getApplicationInfo(packageName, 1024));
    }

    public Drawable getActivityBanner(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadBanner(this);
    }

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

    public Drawable getApplicationBanner(ApplicationInfo info) {
        return info.loadBanner(this);
    }

    public Drawable getApplicationBanner(String packageName) throws PackageManager.NameNotFoundException {
        return getApplicationBanner(getApplicationInfo(packageName, 1024));
    }

    public Drawable getActivityLogo(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadLogo(this);
    }

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

    public Drawable getApplicationLogo(ApplicationInfo info) {
        return info.loadLogo(this);
    }

    public Drawable getApplicationLogo(String packageName) throws PackageManager.NameNotFoundException {
        return getApplicationLogo(getApplicationInfo(packageName, 1024));
    }

    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        if (isClonedProfile(user.getIdentifier())) {
            return HwThemeManager.getClonedDrawable(this.mContext, icon);
        }
        if (!isManagedProfile(user.getIdentifier())) {
            return icon;
        }
        return getBadgedDrawable(icon, new LauncherIcons(this.mContext).getBadgeDrawable(R.drawable.ic_corp_icon_badge_case, getUserBadgeColor(user)), null, false);
    }

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

    public Drawable getUserBadgeForDensity(UserHandle user, int density) {
        Drawable badgeColor = getManagedProfileIconForDensity(user, R.drawable.ic_corp_badge_color, density);
        if (badgeColor == null) {
            return null;
        }
        Drawable badgeForeground = getDrawableForDensity(R.drawable.ic_corp_badge_case, density);
        badgeForeground.setTint(getUserBadgeColor(user));
        return new LayerDrawable(new Drawable[]{badgeColor, badgeForeground});
    }

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
        return Resources.getSystem().getDrawableForDensity(drawableId, density);
    }

    private Drawable getManagedProfileIconForDensity(UserHandle user, int drawableId, int density) {
        if (isManagedProfile(user.getIdentifier())) {
            return getDrawableForDensity(drawableId, density);
        }
        return null;
    }

    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        if (!isManagedProfile(user.getIdentifier())) {
            return label;
        }
        int resourceId = CORP_BADGE_LABEL_RES_ID[getUserManager().getManagedProfileBadge(user.getIdentifier()) % CORP_BADGE_LABEL_RES_ID.length];
        return Resources.getSystem().getString(resourceId, label);
    }

    public Resources getResourcesForActivity(ComponentName activityName) throws PackageManager.NameNotFoundException {
        return getResourcesForApplication(getActivityInfo(activityName, 1024).applicationInfo);
    }

    public Resources getResourcesForApplication(ApplicationInfo app) throws PackageManager.NameNotFoundException {
        if (app.packageName.equals(HwThemeManager.HWT_USER_SYSTEM)) {
            return this.mContext.mMainThread.getSystemUiContext().getResources();
        }
        boolean sameUid = app.uid == Process.myUid();
        ResourcesManager.getInstance().setHwThemeType(sameUid ? app.sourceDir : app.publicSourceDir, app.hwThemeType);
        Resources r = this.mContext.mMainThread.getTopLevelResources(sameUid ? app.sourceDir : app.publicSourceDir, sameUid ? app.splitSourceDirs : app.splitPublicSourceDirs, app.resourceDirs, app.sharedLibraryFiles, 0, this.mContext.mPackageInfo);
        if (r != null) {
            r.setPackageName(app.packageName);
            r.getImpl().getHwResourcesImpl().setPackageName(app.packageName);
            return r;
        }
        throw new PackageManager.NameNotFoundException("Unable to open " + app.publicSourceDir);
    }

    public Resources getResourcesForApplication(String appPackageName) throws PackageManager.NameNotFoundException {
        return getResourcesForApplication(getApplicationInfo(appPackageName, 1024));
    }

    public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) throws PackageManager.NameNotFoundException {
        if (userId < 0) {
            throw new IllegalArgumentException("Call does not support special user #" + userId);
        } else if (HwThemeManager.HWT_USER_SYSTEM.equals(appPackageName)) {
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

    static void configurationChanged() {
        synchronized (sSync) {
            sIconCache.clear();
            sStringCache.clear();
        }
    }

    protected ApplicationPackageManager(ContextImpl context, IPackageManager pm) {
        this.mContext = context;
        this.mPM = pm;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
        return null;
     */
    private Drawable getCachedIcon(ResourceName name) {
        synchronized (sSync) {
            WeakReference<Drawable.ConstantState> wr = sIconCache.get(name);
            if (wr != null) {
                Drawable.ConstantState state = (Drawable.ConstantState) wr.get();
                if (state != null) {
                    Drawable newDrawable = state.newDrawable();
                    return newDrawable;
                }
                sIconCache.remove(name);
            }
        }
    }

    private void putCachedIcon(ResourceName name, Drawable dr) {
        synchronized (sSync) {
            sIconCache.put(name, new WeakReference(dr.getConstantState()));
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
            if (!needCleanup && !hasPkgInfo) {
                return;
            }
            if (immediateGc) {
                Runtime.getRuntime().gc();
            } else {
                ActivityThread.currentActivityThread().scheduleGcIdler();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        return null;
     */
    public CharSequence getCachedString(ResourceName name) {
        synchronized (sSync) {
            WeakReference<CharSequence> wr = sStringCache.get(name);
            if (wr != null) {
                CharSequence cs = (CharSequence) wr.get();
                if (cs != null) {
                    return cs;
                }
                sStringCache.remove(name);
            }
        }
    }

    public void putCachedString(ResourceName name, CharSequence cs) {
        synchronized (sSync) {
            sStringCache.put(name, new WeakReference(cs));
        }
    }

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

    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return info.loadLabel(this);
    }

    public int installExistingPackage(String packageName) throws PackageManager.NameNotFoundException {
        return installExistingPackage(packageName, 0);
    }

    public int installExistingPackage(String packageName, int installReason) throws PackageManager.NameNotFoundException {
        return installExistingPackageAsUser(packageName, installReason, this.mContext.getUserId());
    }

    public int installExistingPackageAsUser(String packageName, int userId) throws PackageManager.NameNotFoundException {
        return installExistingPackageAsUser(packageName, 0, userId);
    }

    private int installExistingPackageAsUser(String packageName, int installReason, int userId) throws PackageManager.NameNotFoundException {
        try {
            int res = this.mPM.installExistingPackageAsUser(packageName, userId, 0, installReason);
            if (res != -3) {
                return res;
            }
            throw new PackageManager.NameNotFoundException("Package " + packageName + " doesn't exist");
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void verifyPendingInstall(int id, int response) {
        try {
            this.mPM.verifyPendingInstall(id, response);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        try {
            this.mPM.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) {
        try {
            this.mPM.verifyIntentFilter(id, verificationCode, failedDomains);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getIntentVerificationStatusAsUser(String packageName, int userId) {
        try {
            return this.mPM.getIntentVerificationStatus(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean updateIntentVerificationStatusAsUser(String packageName, int status, int userId) {
        try {
            return this.mPM.updateIntentVerificationStatus(packageName, status, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public String getDefaultBrowserPackageNameAsUser(int userId) {
        try {
            return this.mPM.getDefaultBrowserPackageName(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
        try {
            return this.mPM.setDefaultBrowserPackageName(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        try {
            this.mPM.setInstallerPackageName(targetPackage, installerPackageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUpdateAvailable(String packageName, boolean updateAvailable) {
        try {
            this.mPM.setUpdateAvailable(packageName, updateAvailable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getInstallerPackageName(String packageName) {
        try {
            return this.mPM.getInstallerPackageName(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMoveStatus(int moveId) {
        try {
            return this.mPM.getMoveStatus(moveId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public int movePackage(String packageName, VolumeInfo vol) {
        String volumeUuid;
        try {
            if ("private".equals(vol.id)) {
                volumeUuid = StorageManager.UUID_PRIVATE_INTERNAL;
            } else if (vol.isPrimaryPhysical()) {
                volumeUuid = "primary_physical";
            } else {
                volumeUuid = (String) Preconditions.checkNotNull(vol.fsUuid);
                return this.mPM.movePackage(packageName, volumeUuid);
            }
            return this.mPM.movePackage(packageName, volumeUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public VolumeInfo getPackageCurrentVolume(ApplicationInfo app) {
        return getPackageCurrentVolume(app, (StorageManager) this.mContext.getSystemService(StorageManager.class));
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public VolumeInfo getPackageCurrentVolume(ApplicationInfo app, StorageManager storage) {
        if (app.isInternal()) {
            return storage.findVolumeById("private");
        }
        if (!app.isExternalAsec()) {
            return storage.findVolumeByUuid(app.volumeUuid);
        }
        if (mHwCustApplicationPackageManager == null || !mHwCustApplicationPackageManager.isSdInstallEnabled()) {
            return storage.getPrimaryPhysicalVolume();
        }
        return mHwCustApplicationPackageManager.getSDVolume(storage);
    }

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
        return Settings.Global.getInt(context.getContentResolver(), "force_allow_on_external", 0) != 0;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isAllow3rdPartyOnInternal(Context context) {
        return context.getResources().getBoolean(R.bool.config_allow3rdPartyAppOnInternal);
    }

    private boolean isPackageCandidateVolume(ContextImpl context, ApplicationInfo app, VolumeInfo vol, IPackageManager pm) {
        boolean forceAllowOnExternal = isForceAllowOnExternal(context);
        boolean z = true;
        if ("private".equals(vol.getId())) {
            if (!app.isSystemApp() && !isAllow3rdPartyOnInternal(context)) {
                z = false;
            }
            return z;
        } else if (app.isSystemApp() || (mHwCustApplicationPackageManager != null && !mHwCustApplicationPackageManager.isSdMoveabled(app))) {
            return false;
        } else {
            if ((!forceAllowOnExternal && (app.installLocation == 1 || app.installLocation == -1)) || !vol.isMountedWritable()) {
                return false;
            }
            if (vol.isPrimaryPhysical()) {
                return app.isInternal();
            }
            if (mHwCustApplicationPackageManager != null && mHwCustApplicationPackageManager.isSdVol(vol)) {
                return true;
            }
            try {
                if (pm.isPackageDeviceAdminOnAnyUser(app.packageName)) {
                    return false;
                }
                if (vol.getType() != 1) {
                    z = false;
                }
                return z;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int movePrimaryStorage(VolumeInfo vol) {
        String volumeUuid;
        try {
            if ("private".equals(vol.id)) {
                volumeUuid = StorageManager.UUID_PRIVATE_INTERNAL;
            } else if (vol.isPrimaryPhysical()) {
                volumeUuid = "primary_physical";
            } else {
                volumeUuid = (String) Preconditions.checkNotNull(vol.fsUuid);
                return this.mPM.movePrimaryStorage(volumeUuid);
            }
            return this.mPM.movePrimaryStorage(volumeUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public VolumeInfo getPrimaryStorageCurrentVolume() {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        return storage.findVolumeByQualifiedUuid(storage.getPrimaryStorageUuid());
    }

    public List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        VolumeInfo currentVol = getPrimaryStorageCurrentVolume();
        List<VolumeInfo> vols = storage.getVolumes();
        List<VolumeInfo> candidates = new ArrayList<>();
        if (!Objects.equals("primary_physical", storage.getPrimaryStorageUuid()) || currentVol == null) {
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
        boolean z = true;
        if ("private".equals(vol.getId())) {
            return true;
        }
        if (!vol.isMountedWritable()) {
            return false;
        }
        if (vol.getType() != 1) {
            z = false;
        }
        return z;
    }

    public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
        deletePackageAsUser(packageName, observer, flags, this.mContext.getUserId());
    }

    public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int flags, int userId) {
        try {
            this.mPM.deletePackageAsUser(packageName, -1, observer, userId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearApplicationUserData(String packageName, IPackageDataObserver observer) {
        try {
            this.mPM.clearApplicationUserData(packageName, observer, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
        try {
            this.mPM.deleteApplicationCacheFiles(packageName, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) {
        try {
            this.mPM.deleteApplicationCacheFilesAsUser(packageName, userId, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void freeStorageAndNotify(String volumeUuid, long idealStorageSize, IPackageDataObserver observer) {
        try {
            this.mPM.freeStorageAndNotify(volumeUuid, idealStorageSize, 0, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {
        try {
            this.mPM.freeStorage(volumeUuid, freeStorageSize, 0, pi);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, PersistableBundle appExtras, PersistableBundle launcherExtras, String dialogMessage) {
        try {
            return this.mPM.setPackagesSuspendedAsUser(packageNames, suspended, appExtras, launcherExtras, dialogMessage, this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Bundle getSuspendedPackageAppExtras() {
        try {
            PersistableBundle extras = this.mPM.getSuspendedPackageAppExtras(this.mContext.getOpPackageName(), this.mContext.getUserId());
            if (extras != null) {
                return new Bundle(extras.deepCopy());
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPackageSuspendedForUser(String packageName, int userId) {
        try {
            return this.mPM.isPackageSuspendedForUser(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPackageSuspended(String packageName) throws PackageManager.NameNotFoundException {
        try {
            return isPackageSuspendedForUser(packageName, this.mContext.getUserId());
        } catch (IllegalArgumentException e) {
            throw new PackageManager.NameNotFoundException(packageName);
        }
    }

    public boolean isPackageSuspended() {
        return isPackageSuspendedForUser(this.mContext.getOpPackageName(), this.mContext.getUserId());
    }

    public void setApplicationCategoryHint(String packageName, int categoryHint) {
        try {
            this.mPM.setApplicationCategoryHint(packageName, categoryHint, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void getPackageSizeInfoAsUser(String packageName, int userHandle, IPackageStatsObserver observer) {
        if (this.mContext.getApplicationInfo().targetSdkVersion >= 26) {
            throw new UnsupportedOperationException("Shame on you for calling the hidden API getPackageSizeInfoAsUser(). Shame!");
        } else if (observer != null) {
            Log.d(TAG, "Shame on you for calling the hidden API getPackageSizeInfoAsUser(). Shame!");
            try {
                observer.onGetStatsCompleted(null, false);
            } catch (RemoteException e) {
                Log.e(TAG, "getPackageSizeInfoAsUser()");
            }
        }
    }

    public void addPackageToPreferred(String packageName) {
        Log.w(TAG, "addPackageToPreferred() is a no-op");
    }

    public void removePackageFromPreferred(String packageName) {
        Log.w(TAG, "removePackageFromPreferred() is a no-op");
    }

    public List<PackageInfo> getPreferredPackages(int flags) {
        Log.w(TAG, "getPreferredPackages() is a no-op");
        return Collections.emptyList();
    }

    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        try {
            this.mPM.addPreferredActivity(filter, match, set, activity, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        try {
            this.mPM.addPreferredActivity(filter, match, set, activity, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        try {
            this.mPM.replacePreferredActivity(filter, match, set, activity, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void replacePreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        try {
            this.mPM.replacePreferredActivity(filter, match, set, activity, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearPackagePreferredActivities(String packageName) {
        try {
            this.mPM.clearPackagePreferredActivities(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        try {
            return this.mPM.getPreferredActivities(outFilters, outActivities, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
        try {
            return this.mPM.getHomeActivities(outActivities);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        try {
            this.mPM.setComponentEnabledSetting(componentName, newState, flags, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        try {
            return this.mPM.getComponentEnabledSetting(componentName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        try {
            this.mPM.setApplicationEnabledSetting(packageName, newState, flags, this.mContext.getUserId(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getApplicationEnabledSetting(String packageName) {
        try {
            return this.mPM.getApplicationEnabledSetting(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void flushPackageRestrictionsAsUser(int userId) {
        try {
            this.mPM.flushPackageRestrictionsAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle user) {
        try {
            return this.mPM.setApplicationHiddenSettingAsUser(packageName, hidden, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
        try {
            return this.mPM.getApplicationHiddenSettingAsUser(packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public KeySet getKeySetByAlias(String packageName, String alias) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(alias);
        try {
            return this.mPM.getKeySetByAlias(packageName, alias);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public KeySet getSigningKeySet(String packageName) {
        Preconditions.checkNotNull(packageName);
        try {
            return this.mPM.getSigningKeySet(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSignedBy(String packageName, KeySet ks) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(ks);
        try {
            return this.mPM.isPackageSignedByKeySet(packageName, ks);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSignedByExactly(String packageName, KeySet ks) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(ks);
        try {
            return this.mPM.isPackageSignedByKeySetExactly(packageName, ks);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentity() {
        try {
            return this.mPM.getVerifierDeviceIdentity();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUpgrade() {
        try {
            return this.mPM.isUpgrade();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PackageInstaller getPackageInstaller() {
        PackageInstaller packageInstaller;
        synchronized (this.mLock) {
            if (this.mInstaller == null) {
                try {
                    this.mInstaller = new PackageInstaller(this.mPM.getPackageInstaller(), this.mContext.getPackageName(), this.mContext.getUserId());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            packageInstaller = this.mInstaller;
        }
        return packageInstaller;
    }

    public boolean isPackageAvailable(String packageName) {
        try {
            return this.mPM.isPackageAvailable(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {
        try {
            this.mPM.addCrossProfileIntentFilter(filter, this.mContext.getOpPackageName(), sourceUserId, targetUserId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearCrossProfileIntentFilters(int sourceUserId) {
        try {
            this.mPM.clearCrossProfileIntentFilters(sourceUserId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        Drawable dr = loadUnbadgedItemIcon(itemInfo, appInfo);
        if (itemInfo.showUserIcon != -10000) {
            return dr;
        }
        return getUserBadgedIcon(dr, new UserHandle(this.mContext.getUserId()));
    }

    public Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        if (!(itemInfo instanceof PermissionGroupInfo)) {
            HwThemeManager.updateIconCache(itemInfo, itemInfo.name, itemInfo.packageName, itemInfo.icon, appInfo == null ? 0 : appInfo.icon);
        }
        if (itemInfo.showUserIcon != -10000) {
            Bitmap bitmap = getUserManager().getUserIcon(itemInfo.showUserIcon);
            if (bitmap == null) {
                return UserIcons.getDefaultUserIcon(this.mContext.getResources(), itemInfo.showUserIcon, false);
            }
            return new BitmapDrawable(bitmap);
        }
        Drawable dr = null;
        if (itemInfo.packageName != null) {
            dr = getDrawable(itemInfo.packageName, itemInfo.icon, appInfo);
        }
        if (dr == null) {
            dr = itemInfo.loadDefaultIcon(this);
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

    public int getInstallReason(String packageName, UserHandle user) {
        try {
            return this.mPM.getInstallReason(packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean canRequestPackageInstalls() {
        try {
            return this.mPM.canRequestPackageInstalls(this.mContext.getPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public ComponentName getInstantAppResolverSettingsComponent() {
        try {
            return this.mPM.getInstantAppResolverSettingsComponent();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public ComponentName getInstantAppInstallerComponent() {
        try {
            return this.mPM.getInstantAppInstallerComponent();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public String getInstantAppAndroidId(String packageName, UserHandle user) {
        try {
            return this.mPM.getInstantAppAndroidId(packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

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

    public CharSequence getHarmfulAppWarning(String packageName) {
        try {
            return this.mPM.getHarmfulAppWarning(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void setHarmfulAppWarning(String packageName, CharSequence warning) {
        try {
            this.mPM.setHarmfulAppWarning(packageName, warning, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

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

    public String getSystemTextClassifierPackageName() {
        try {
            return this.mPM.getSystemTextClassifierPackageName();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean isPackageStateProtected(String packageName, int userId) {
        try {
            return this.mPM.isPackageStateProtected(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void removeCacheIcon(String packageName, int resid) {
        sIconCache.remove(new ResourceName(packageName, resid));
    }

    private boolean isClonedProfile(int userId) {
        boolean isClonedProfile = false;
        if (userId == 0) {
            return false;
        }
        try {
            isClonedProfile = getUserManager().getUserInfo(userId).isClonedProfile();
        } catch (Exception e) {
        }
        return isClonedProfile;
    }

    public static void setUseCache(boolean iscache) {
        USE_CACHE = iscache;
    }
}
