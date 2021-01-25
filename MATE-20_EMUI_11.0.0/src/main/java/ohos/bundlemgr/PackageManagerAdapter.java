package ohos.bundlemgr;

import android.app.ActivityThread;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.IBackupSessionCallback;
import android.os.RemoteException;
import android.util.SparseArray;
import com.huawei.android.content.pm.HwPackageManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.SystemPropertyUtils;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.IBackupSessionCallback;
import ohos.bundle.ICleanCacheCallback;
import ohos.bundle.ShortcutIntent;
import ohos.global.resource.NotExistException;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.security.permission.PermissionConversion;
import ohos.security.permission.PermissionDef;
import ohos.security.permission.PermissionGroupDef;
import ohos.security.permissionkitinner.PermissionKitInner;
import ohos.utils.adapter.CapabilityConstantMapper;

public class PackageManagerAdapter {
    private static final int BACKUP_SESSION_FAILED = -1;
    private static final String CERT_X_509 = "X.509";
    private static final int CHECK_PERMISSION_FAILED = -1;
    private static final int GET_PACKAGE_INFO_FLAGS = 134218240;
    private static final String OHOS_PREFIX = "ohos";
    private static final int PUBLIC_KEY_BEGIN_OFFSET = 8;
    private static final String PUBLIC_KEY_MODULUS = "modulus";
    private static final String PUBLIC_KEY_PUBLIC_EXPONENT = "publicexponent";
    private static final String PUBLIC_KEY_SPEC_CHAR = "\\s*|\t|\r|\n";
    private static final String SHELL_SUFFIX = "ShellActivity";
    private static final int SHORTCUT_EXISTENCE_EXISTS = 0;
    private static final int SHORTCUT_EXISTENCE_NOT_EXISTS = 1;
    private static final int SHORTCUT_EXISTENCE_UNKNOW = 2;
    private static final String USES_FEATRUE_ZIDANE = "zidane.software.ability";
    private static volatile PackageManagerAdapter instance = new PackageManagerAdapter();
    private volatile int cachedSafeMode = -1;
    private Context context;
    private FeatureInfo[] featureInfos = null;
    private List<String> features = new ArrayList();
    private AndroidBackupCallback mCallback = new AndroidBackupCallback();
    private final SparseArray<IBackupSessionCallback> mSessions = new SparseArray<>();
    private PackageManager pkgManager = getPackageManager();
    private ShortcutManager shortcutManager = getShortcutManager();

    public static PackageManagerAdapter getInstance() {
        return instance;
    }

    private PackageManagerAdapter() {
    }

    private ShortcutManager getShortcutManager() {
        Context context2 = this.context;
        if (context2 != null) {
            return (ShortcutManager) context2.getSystemService(ShortcutManager.class);
        }
        AppLog.e("getShortcutManager failed due to context is null", new Object[0]);
        return null;
    }

    public boolean isPackageEnabled(String str) {
        if (this.pkgManager == null) {
            AppLog.e("packageManager is null", new Object[0]);
            return false;
        } else if (str == null || str.isEmpty()) {
            AppLog.e("packageName is null or empty", new Object[0]);
            return false;
        } else {
            int applicationEnabledSetting = this.pkgManager.getApplicationEnabledSetting(str);
            if (applicationEnabledSetting == 2 || applicationEnabledSetting == 3 || applicationEnabledSetting == 4) {
                return false;
            }
            return true;
        }
    }

    public boolean isComponentEnabled(String str, String str2) {
        if (this.pkgManager == null) {
            AppLog.e("packageManager is null", new Object[0]);
            return false;
        } else if (str == null || str.isEmpty()) {
            AppLog.e("packageName is null or empty", new Object[0]);
            return false;
        } else if (str2 == null || str2.isEmpty()) {
            AppLog.e("className is null or empty", new Object[0]);
            return false;
        } else {
            int componentEnabledSetting = this.pkgManager.getComponentEnabledSetting(new ComponentName(str, str2));
            if (componentEnabledSetting == 2 || componentEnabledSetting == 3 || componentEnabledSetting == 4) {
                return false;
            }
            return true;
        }
    }

    public void setComponentEnabled(String str, String str2, boolean z) {
        if (this.pkgManager == null) {
            AppLog.e("packageManager is null", new Object[0]);
        } else if (str == null || str.isEmpty()) {
            AppLog.e("packageName is null or empty", new Object[0]);
        } else if (str2 == null || str2.isEmpty()) {
            AppLog.e("className is null or empty", new Object[0]);
        } else {
            this.pkgManager.setComponentEnabledSetting(new ComponentName(str, str2), z ? 1 : 2, 0);
        }
    }

    public void setApplicationEnabled(String str, boolean z) {
        if (this.pkgManager == null) {
            AppLog.e("packageManager is null", new Object[0]);
        } else if (str == null || str.isEmpty()) {
            AppLog.e("packageName is null or empty", new Object[0]);
        } else {
            this.pkgManager.setApplicationEnabledSetting(str, z ? 1 : 2, 0);
        }
    }

    public List<String> getSystemAvailableFeatures() {
        if (!this.features.isEmpty()) {
            return this.features;
        }
        SystemPropertyUtils.DeviceType deviceType = SystemPropertyUtils.getDeviceType();
        if (SystemPropertyUtils.DeviceType.DEVICE_TYPE_PHONE.equals(deviceType) || SystemPropertyUtils.DeviceType.DEVICE_TYPE_TABLET.equals(deviceType) || SystemPropertyUtils.DeviceType.DEVICE_TYPE_DEFAULT.equals(deviceType)) {
            this.features.add("ohos.software.distributeddatamgr.datausage");
            this.features.add("ohos.software.distributeddatamgr.distributedfile");
            this.features.add("ohos.software.distributeddatamgr.search");
            this.features.add("ohos.software.miscservices.inputmethod");
            return this.features;
        }
        PackageManager packageManager = this.pkgManager;
        int i = 0;
        if (packageManager == null) {
            AppLog.e("packageManager is null", new Object[0]);
            return this.features;
        }
        if (this.featureInfos == null) {
            this.featureInfos = packageManager.getSystemAvailableFeatures();
        }
        if (this.featureInfos == null) {
            AppLog.e("get featureInfos from pms is null", new Object[0]);
            return this.features;
        }
        while (true) {
            FeatureInfo[] featureInfoArr = this.featureInfos;
            if (i >= featureInfoArr.length) {
                return this.features;
            }
            String str = featureInfoArr[i].name;
            if (str != null && !str.isEmpty()) {
                if (str.contains(OHOS_PREFIX)) {
                    this.features.add(str);
                } else {
                    Optional convertToCapability = CapabilityConstantMapper.convertToCapability(str);
                    if (convertToCapability.isPresent() && !((String) convertToCapability.get()).isEmpty()) {
                        this.features.add((String) convertToCapability.get());
                    }
                }
            }
            i++;
        }
    }

    public boolean hasSystemFeature(String str) {
        if (this.features.isEmpty()) {
            this.features = getSystemAvailableFeatures();
        }
        return this.features.contains(str);
    }

    public int[] getPackageGids(String str) {
        int[] iArr = new int[0];
        PackageManager packageManager = this.pkgManager;
        if (packageManager == null) {
            AppLog.e("packageManager is null", new Object[0]);
            return iArr;
        }
        try {
            return packageManager.getPackageGids(str);
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.w("getPackageGids failed, error : %{public}s", e.getMessage());
            return iArr;
        }
    }

    public boolean isSafeMode() {
        PackageManager packageManager;
        if (this.cachedSafeMode < 0 && (packageManager = this.pkgManager) != null) {
            this.cachedSafeMode = packageManager.isSafeMode() ? 1 : 0;
        }
        return this.cachedSafeMode != 0;
    }

    public PermissionDef getPermissionInfo(String str) {
        if (this.pkgManager == null) {
            AppLog.e("packageManager is null", new Object[0]);
            return null;
        }
        String aosPermissionNameIfPossible = PermissionConversion.getAosPermissionNameIfPossible(str);
        if (aosPermissionNameIfPossible == null || aosPermissionNameIfPossible.isEmpty()) {
            AppLog.e("get translated permission name is null", new Object[0]);
            return null;
        }
        try {
            PermissionInfo permissionInfo = this.pkgManager.getPermissionInfo(aosPermissionNameIfPossible, 0);
            if (permissionInfo != null) {
                return (PermissionDef) PermissionKitInner.getInstance().translatePermission(permissionInfo, str).orElse(null);
            }
            AppLog.e("get permission from pms is null", new Object[0]);
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.e("permissionName not found, %{public}s", e.getMessage());
            AppLog.e("getPermissionInfo from pms failed", new Object[0]);
            return null;
        }
    }

    public int checkPermission(String str, String str2) {
        if (this.pkgManager == null) {
            AppLog.e("packageManager is null", new Object[0]);
            return -1;
        }
        String aosPermissionNameIfPossible = PermissionConversion.getAosPermissionNameIfPossible(str);
        if (aosPermissionNameIfPossible != null && !aosPermissionNameIfPossible.isEmpty()) {
            return this.pkgManager.checkPermission(aosPermissionNameIfPossible, str2);
        }
        AppLog.e("get translated permission name is null", new Object[0]);
        return -1;
    }

    public Optional<PermissionGroupDef> getPermissionGroupInfo(String str) {
        if (this.pkgManager == null) {
            AppLog.e("permission group get packageManager is null", new Object[0]);
            return Optional.empty();
        }
        String androidPermGroupName = PermissionKitInner.getInstance().getAndroidPermGroupName(str);
        if (androidPermGroupName == null || androidPermGroupName.isEmpty()) {
            AppLog.e("get permission group, permission group name is invalid", new Object[0]);
            return Optional.empty();
        }
        try {
            PermissionGroupInfo permissionGroupInfo = this.pkgManager.getPermissionGroupInfo(androidPermGroupName, 0);
            if (permissionGroupInfo != null) {
                return PermissionKitInner.getInstance().translatePermissionGroup(permissionGroupInfo, str);
            }
            AppLog.e("get permission group from pms is null", new Object[0]);
            return Optional.empty();
        } catch (PackageManager.NameNotFoundException unused) {
            AppLog.e("getPermissionGroupInfo failed GroupName not found, %{public}s", str);
            return Optional.empty();
        }
    }

    public Optional<List<PermissionDef>> getPermissionInfoByGroup(String str) {
        if (this.pkgManager == null) {
            AppLog.e("sub permission info get by group packageManager is null", new Object[0]);
            return Optional.empty();
        }
        String androidPermGroupName = PermissionKitInner.getInstance().getAndroidPermGroupName(str);
        if (androidPermGroupName == null || androidPermGroupName.isEmpty()) {
            AppLog.e("get sub permission by group, group name is invalid", new Object[0]);
            return Optional.empty();
        }
        try {
            ArrayList arrayList = new ArrayList();
            List<PermissionInfo> queryPermissionsByGroup = this.pkgManager.queryPermissionsByGroup(androidPermGroupName, 0);
            if (queryPermissionsByGroup == null) {
                AppLog.e("get sub permission by permission group from pms is null", new Object[0]);
                return Optional.empty();
            }
            for (PermissionInfo permissionInfo : queryPermissionsByGroup) {
                if (permissionInfo != null) {
                    Optional translatePermission = PermissionKitInner.getInstance().translatePermission(permissionInfo, permissionInfo.name);
                    if (translatePermission.isPresent()) {
                        arrayList.add((PermissionDef) translatePermission.get());
                    }
                }
            }
            return Optional.of(arrayList);
        } catch (PackageManager.NameNotFoundException unused) {
            AppLog.e("get sub permission info failed GroupName not found, %{public}s", str);
            return Optional.empty();
        }
    }

    public void cleanBundleCacheFiles(String str, final ICleanCacheCallback iCleanCacheCallback) {
        if (this.pkgManager == null) {
            AppLog.e("clean bundle cache files get packageManager is null", new Object[0]);
            return;
        }
        this.pkgManager.deleteApplicationCacheFiles(str, new IPackageDataObserver.Stub() {
            /* class ohos.bundlemgr.PackageManagerAdapter.AnonymousClass1 */

            public void onRemoveCompleted(String str, boolean z) throws RemoteException {
                iCleanCacheCallback.onCleanCacheFinished(z);
            }
        });
    }

    private PackageManager getPackageManager() {
        Context applicationContext;
        Application currentApplication = ActivityThread.currentApplication();
        if (currentApplication == null || (applicationContext = currentApplication.getApplicationContext()) == null) {
            return null;
        }
        this.context = applicationContext;
        return applicationContext.getPackageManager();
    }

    public int startBackupSession(IBackupSessionCallback iBackupSessionCallback) {
        if (iBackupSessionCallback == null) {
            return -1;
        }
        int startBackupSession = HwPackageManager.startBackupSession(this.mCallback);
        if (startBackupSession > 0) {
            synchronized (this.mSessions) {
                this.mSessions.put(startBackupSession, iBackupSessionCallback);
            }
        }
        return startBackupSession;
    }

    public int executeBackupTask(int i, String str) {
        return HwPackageManager.executeBackupTask(i, str);
    }

    public int finishBackupSession(int i) {
        int finishBackupSession = HwPackageManager.finishBackupSession(i);
        synchronized (this.mSessions) {
            this.mSessions.remove(i);
        }
        return finishBackupSession;
    }

    /* access modifiers changed from: private */
    public final class AndroidBackupCallback extends IBackupSessionCallback.Stub {
        private AndroidBackupCallback() {
        }

        public void onTaskStatusChanged(int i, int i2, int i3, String str) {
            synchronized (PackageManagerAdapter.this.mSessions) {
                ohos.bundle.IBackupSessionCallback iBackupSessionCallback = (ohos.bundle.IBackupSessionCallback) PackageManagerAdapter.this.mSessions.get(i);
                if (iBackupSessionCallback == null) {
                    AppLog.i("no callback set for session:%{public}d", Integer.valueOf(i));
                } else {
                    iBackupSessionCallback.onTaskStatusChanged(i, i2, i3, str);
                }
            }
        }
    }

    public BundleInfo getBundleInfoFromPms(String str, boolean z) {
        AppLog.d("packageManager getBundleInfoFromPms is called", new Object[0]);
        if (this.pkgManager == null) {
            return null;
        }
        int i = GET_PACKAGE_INFO_FLAGS;
        if (z) {
            i = 134218253;
        }
        try {
            return convertPackageInfoToBundleInfo(this.pkgManager.getPackageInfo(str, i));
        } catch (PackageManager.NameNotFoundException unused) {
            AppLog.e("packageManager getBundleInfoFromPms occur exception.", new Object[0]);
            return null;
        }
    }

    public List<BundleInfo> getBundleInfosFromPms(boolean z) {
        BundleInfo convertPackageInfoToBundleInfo;
        AppLog.d("packageManager getBundleInfosFromPms is called", new Object[0]);
        if (this.pkgManager == null) {
            return null;
        }
        int i = GET_PACKAGE_INFO_FLAGS;
        if (z) {
            i = 134218253;
        }
        List<PackageInfo> installedPackages = this.pkgManager.getInstalledPackages(i);
        if (installedPackages == null || installedPackages.isEmpty()) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        Iterator<PackageInfo> it = installedPackages.iterator();
        while (true) {
            boolean z2 = true;
            if (it.hasNext()) {
                PackageInfo next = it.next();
                if (next != null) {
                    if (next.reqFeatures != null) {
                        FeatureInfo[] featureInfoArr = next.reqFeatures;
                        int length = featureInfoArr.length;
                        int i2 = 0;
                        while (true) {
                            if (i2 >= length) {
                                break;
                            } else if (USES_FEATRUE_ZIDANE.equals(featureInfoArr[i2].name)) {
                                break;
                            } else {
                                i2++;
                            }
                        }
                    }
                    z2 = false;
                    if (!z2 && (convertPackageInfoToBundleInfo = convertPackageInfoToBundleInfo(next)) != null) {
                        arrayList.add(convertPackageInfoToBundleInfo);
                    }
                }
            } else {
                AppLog.d("packageManager getBundleInfosFromPms bundleInfos size:%{public}d", Integer.valueOf(arrayList.size()));
                return arrayList;
            }
        }
    }

    public int isPinnedShortcutExist(String str, int i) {
        if (str == null || str.isEmpty()) {
            AppLog.e("invalid shortcutId", new Object[0]);
            return 1;
        }
        ShortcutManager shortcutManager2 = this.shortcutManager;
        if (shortcutManager2 == null) {
            AppLog.e("shortcutManager is null", new Object[0]);
            return 2;
        }
        try {
            List<ShortcutInfo> pinnedShortcuts = shortcutManager2.getPinnedShortcuts();
            if (pinnedShortcuts == null) {
                AppLog.w("infos is null", new Object[0]);
                return 1;
            }
            for (ShortcutInfo shortcutInfo : pinnedShortcuts) {
                if (str.equals(shortcutInfo.getId())) {
                    AppLog.w("id: %{private}s exists", str);
                    return 0;
                }
            }
            return 1;
        } catch (IllegalStateException unused) {
            AppLog.e("currently may be locked", new Object[0]);
        }
    }

    public boolean requestPinShortcut(ohos.bundle.ShortcutInfo shortcutInfo, ResourceManager resourceManager) {
        if (shortcutInfo == null || resourceManager == null) {
            AppLog.e("parameters invalid", new Object[0]);
            return false;
        } else if (this.shortcutManager == null) {
            AppLog.e("shortcutManager is null", new Object[0]);
            return false;
        } else {
            ShortcutInfo convertShortcut = convertShortcut(shortcutInfo, resourceManager);
            if (convertShortcut == null) {
                AppLog.e("shortcutInfo is invalid", new Object[0]);
                return false;
            }
            try {
                return this.shortcutManager.requestPinShortcut(convertShortcut, null);
            } catch (IllegalArgumentException unused) {
                AppLog.e("packageManager requestPinShortcut occurs IllegalArgumentException", new Object[0]);
                return false;
            } catch (IllegalStateException unused2) {
                AppLog.e("packageManager requestPinShortcut occurs IllegalStateException", new Object[0]);
                return false;
            }
        }
    }

    public boolean updateShortcuts(List<ohos.bundle.ShortcutInfo> list, ResourceManager resourceManager) {
        AppLog.d("updateShortcuts is called", new Object[0]);
        if (list == null || list.isEmpty() || resourceManager == null) {
            AppLog.e("parameters invalid", new Object[0]);
            return false;
        } else if (this.shortcutManager == null) {
            AppLog.e("shortcutManager is null", new Object[0]);
            return false;
        } else {
            List<ShortcutInfo> convertToAShortcuts = convertToAShortcuts(list, resourceManager);
            if (convertToAShortcuts.isEmpty()) {
                AppLog.e("updateShortcuts shorcutInfos is invalid", new Object[0]);
                return false;
            }
            try {
                return this.shortcutManager.updateShortcuts(convertToAShortcuts);
            } catch (IllegalArgumentException unused) {
                AppLog.e("packageManager updateShortcuts occur exception, shortcuts may be immutable", new Object[0]);
                return false;
            } catch (IllegalStateException unused2) {
                AppLog.e("packageManager updateShortcuts occur exception, device currently may be locked", new Object[0]);
                return false;
            }
        }
    }

    public boolean isRequestPinShortcutSupported() {
        ShortcutManager shortcutManager2 = this.shortcutManager;
        if (shortcutManager2 != null) {
            return shortcutManager2.isRequestPinShortcutSupported();
        }
        AppLog.e("shortcutManager is null", new Object[0]);
        return false;
    }

    public void disablePinShortcuts(List<String> list) {
        ShortcutManager shortcutManager2 = this.shortcutManager;
        if (shortcutManager2 == null) {
            AppLog.e("shortcutManager is null", new Object[0]);
            return;
        }
        try {
            shortcutManager2.disableShortcuts(list);
        } catch (IllegalArgumentException unused) {
            AppLog.e("packageManager disablePinShortcuts occur IllegalArgumentException", new Object[0]);
        } catch (IllegalStateException unused2) {
            AppLog.e("packageManager disablePinShortcuts occur IllegalStateException", new Object[0]);
        }
    }

    public void enablePinShortcuts(List<String> list) {
        ShortcutManager shortcutManager2 = this.shortcutManager;
        if (shortcutManager2 == null) {
            AppLog.e("shortcutManager is null", new Object[0]);
            return;
        }
        try {
            shortcutManager2.enableShortcuts(list);
        } catch (IllegalArgumentException unused) {
            AppLog.e("packageManager enablePinShortcuts occur IllegalArgumentException", new Object[0]);
        } catch (IllegalStateException unused2) {
            AppLog.e("packageManager enablePinShortcuts occur IllegalStateException", new Object[0]);
        }
    }

    private ShortcutInfo convertShortcut(ohos.bundle.ShortcutInfo shortcutInfo, ResourceManager resourceManager) throws IllegalArgumentException {
        if (this.context == null) {
            AppLog.e("context is null", new Object[0]);
            return null;
        } else if (shortcutInfo.getId() == null) {
            throw new IllegalArgumentException("shortcut's id can't be null");
        } else if (shortcutInfo.getLabel() != null) {
            ShortcutInfo.Builder shortLabel = new ShortcutInfo.Builder(this.context, shortcutInfo.getId().toString()).setShortLabel(shortcutInfo.getLabel());
            String bundleName = shortcutInfo.getBundleName();
            shortLabel.setActivity(new ComponentName(bundleName, shortcutInfo.getHostAbilityName() + "ShellActivity"));
            Intent[] createIntents = createIntents(shortcutInfo);
            if (createIntents.length != 0) {
                shortLabel.setIntents(createIntents);
                Icon createIcon = createIcon(shortcutInfo, resourceManager);
                if (createIcon != null) {
                    shortLabel.setIcon(createIcon);
                }
                String disableMessage = shortcutInfo.getDisableMessage();
                if (disableMessage != null && !disableMessage.isEmpty()) {
                    shortLabel.setDisabledMessage(disableMessage);
                }
                return shortLabel.build();
            }
            throw new IllegalArgumentException("shortcut's intents can't be null or empty");
        } else {
            throw new IllegalArgumentException("shortcut's label can't be null");
        }
    }

    private List<ShortcutInfo> convertToAShortcuts(List<ohos.bundle.ShortcutInfo> list, ResourceManager resourceManager) {
        ArrayList arrayList = new ArrayList();
        for (ohos.bundle.ShortcutInfo shortcutInfo : list) {
            ShortcutInfo convertShortcut = convertShortcut(shortcutInfo, resourceManager);
            if (convertShortcut != null) {
                arrayList.add(convertShortcut);
            }
        }
        return arrayList;
    }

    private Icon createIcon(ohos.bundle.ShortcutInfo shortcutInfo, ResourceManager resourceManager) {
        int shortcutIconId = shortcutInfo.getShortcutIconId();
        if (shortcutIconId <= 0) {
            InputStream iconStream = shortcutInfo.getIconStream();
            if (iconStream == null) {
                AppLog.w("createIcon failed due to resId is invalid and iconStream is null", new Object[0]);
                return null;
            }
            Bitmap decodeStream = BitmapFactory.decodeStream(iconStream);
            if (decodeStream != null) {
                return Icon.createWithBitmap(decodeStream);
            }
            AppLog.e("decodeStream failed", new Object[0]);
            return null;
        }
        try {
            PixelMap createPixelMap = createPixelMap(resourceManager.getResource(shortcutIconId));
            if (createPixelMap == null) {
                AppLog.e("createPixelMap failed", new Object[0]);
                return null;
            }
            Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(createPixelMap);
            if (createShadowBitmap != null) {
                return Icon.createWithBitmap(createShadowBitmap);
            }
            AppLog.e("createShadowBitmap failed", new Object[0]);
            return null;
        } catch (IOException | NotExistException e) {
            AppLog.e("getResource failed, exception: %{public}s", e.getMessage());
            return null;
        }
    }

    private PixelMap createPixelMap(Resource resource) {
        ImageSource.SourceOptions sourceOptions = new ImageSource.SourceOptions();
        sourceOptions.formatHint = "image/png";
        ImageSource create = ImageSource.create(resource, sourceOptions);
        if (create == null) {
            AppLog.e("create imageSource failed", new Object[0]);
            return null;
        }
        ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
        decodingOptions.desiredSize = new Size(0, 0);
        decodingOptions.desiredRegion = new Rect(0, 0, 0, 0);
        decodingOptions.desiredPixelFormat = PixelFormat.RGBA_8888;
        return create.createPixelmap(decodingOptions);
    }

    private Intent[] createIntents(ohos.bundle.ShortcutInfo shortcutInfo) {
        if (shortcutInfo == null || shortcutInfo.getIntents() == null) {
            AppLog.e("createIntents failed due to invalid parameter", new Object[0]);
            return new Intent[0];
        }
        int size = shortcutInfo.getIntents().size();
        Intent[] intentArr = new Intent[size];
        for (int i = 0; i < size; i++) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            ShortcutIntent shortcutIntent = shortcutInfo.getIntents().get(i);
            if (shortcutIntent == null) {
                AppLog.w("createIntents intent is null", new Object[0]);
            } else {
                String targetBundle = shortcutIntent.getTargetBundle();
                String targetClass = shortcutIntent.getTargetClass();
                if (targetBundle == null || targetClass == null) {
                    AppLog.w("createIntents bundleName or className is null", new Object[0]);
                } else {
                    if (targetClass.startsWith(".")) {
                        targetClass = targetBundle + shortcutIntent.getTargetClass();
                    }
                    intent.setComponent(new ComponentName(targetBundle, targetClass + "ShellActivity"));
                    for (Map.Entry<String, String> entry : shortcutIntent.getParams().entrySet()) {
                        intent.putExtra(entry.getKey(), entry.getValue());
                    }
                    intentArr[i] = intent;
                }
            }
        }
        return intentArr;
    }

    static String generateAppId(PackageInfo packageInfo) {
        Signature[] apkContentsSigners = packageInfo.signingInfo.getApkContentsSigners();
        if (apkContentsSigners == null || apkContentsSigners.length <= 0) {
            return "";
        }
        String publicKey = getPublicKey(apkContentsSigners[0]);
        if (!publicKey.isEmpty()) {
            return packageInfo.packageName + "_" + publicKey;
        }
        AppLog.w("generateAppId failed, public key is null", new Object[0]);
        return "";
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:16:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    private static String getPublicKey(Signature signature) {
        String str;
        try {
            Certificate generateCertificate = CertificateFactory.getInstance(CERT_X_509).generateCertificate(new ByteArrayInputStream(signature.toByteArray()));
            if (generateCertificate instanceof X509Certificate) {
                str = ((X509Certificate) generateCertificate).getPublicKey().toString();
                if (!str.isEmpty()) {
                    return str;
                }
                String lowerCase = Pattern.compile(PUBLIC_KEY_SPEC_CHAR).matcher(str).replaceAll("").replace(",", "").toLowerCase(Locale.ENGLISH);
                int indexOf = lowerCase.indexOf(PUBLIC_KEY_MODULUS);
                int indexOf2 = lowerCase.indexOf(PUBLIC_KEY_PUBLIC_EXPONENT);
                if (indexOf >= 0 && indexOf2 >= 0) {
                    return lowerCase.substring(indexOf + 8, indexOf2).toLowerCase(Locale.ENGLISH);
                }
                AppLog.w("getPublicKey failed, modulusPos is invalid", new Object[0]);
                return "";
            }
        } catch (CertificateException e) {
            AppLog.w("getPublicKey close failed, error : %{public}s", e.getMessage());
        }
        str = "";
        if (!str.isEmpty()) {
        }
    }

    private static BundleInfo convertPackageInfoToBundleInfo(PackageInfo packageInfo) {
        if (packageInfo == null || packageInfo.applicationInfo == null) {
            return null;
        }
        BundleInfo bundleInfo = new BundleInfo();
        bundleInfo.name = packageInfo.packageName;
        bundleInfo.appId = generateAppId(packageInfo);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        bundleInfo.uid = applicationInfo.uid;
        bundleInfo.sharedUserId = packageInfo.sharedUserId;
        bundleInfo.appInfo.name = packageInfo.packageName;
        bundleInfo.appInfo.systemApp = applicationInfo.isSystemApp();
        bundleInfo.appInfo.enabled = applicationInfo.enabled;
        ArrayList arrayList = new ArrayList();
        ActivityInfo[] activityInfoArr = packageInfo.activities;
        if (activityInfoArr != null && activityInfoArr.length > 0) {
            convertComponentInfoToAbilityInfo(Arrays.asList(activityInfoArr), arrayList);
        }
        ServiceInfo[] serviceInfoArr = packageInfo.services;
        if (serviceInfoArr != null && serviceInfoArr.length > 0) {
            convertComponentInfoToAbilityInfo(Arrays.asList(serviceInfoArr), arrayList);
        }
        ProviderInfo[] providerInfoArr = packageInfo.providers;
        if (providerInfoArr != null && providerInfoArr.length > 0) {
            convertComponentInfoToAbilityInfo(Arrays.asList(providerInfoArr), arrayList);
        }
        if (!arrayList.isEmpty()) {
            bundleInfo.abilityInfos.addAll(arrayList);
        }
        return bundleInfo;
    }

    private static void convertComponentInfoToAbilityInfo(List<? extends ComponentInfo> list, List<AbilityInfo> list2) {
        if (!(list == null || list2 == null)) {
            for (ComponentInfo componentInfo : list) {
                if (componentInfo != null) {
                    AbilityInfo abilityInfo = new AbilityInfo();
                    abilityInfo.bundleName = componentInfo.packageName;
                    abilityInfo.className = new ComponentName(componentInfo.packageName, componentInfo.name).getClassName();
                    abilityInfo.enabled = componentInfo.enabled;
                    list2.add(abilityInfo);
                }
            }
        }
    }
}
