package com.android.server;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.permission.PermissionManager;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.huawei.server.HwCustSystemConfig;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SystemConfig {
    private static final int ALLOW_ALL = -1;
    private static final int ALLOW_APP_CONFIGS = 8;
    private static final int ALLOW_ASSOCIATIONS = 128;
    private static final int ALLOW_FEATURES = 1;
    private static final int ALLOW_HIDDENAPI_WHITELISTING = 64;
    private static final int ALLOW_LIBS = 2;
    private static final int ALLOW_OEM_PERMISSIONS = 32;
    private static final int ALLOW_PERMISSIONS = 4;
    private static final int ALLOW_PRIVAPP_PERMISSIONS = 16;
    private static final int CUST_TYPE_CONFIG = 0;
    private static final String SKU_PROPERTY = "ro.boot.product.hardware.sku";
    static final String TAG = "SystemConfig";
    static SystemConfig sInstance;
    private Object hwCustSystemConfigObj = HwCustUtils.createObj(HwCustSystemConfig.class, new Object[0]);
    final ArraySet<String> mAllowIgnoreLocationSettings = new ArraySet<>();
    final ArraySet<String> mAllowImplicitBroadcasts = new ArraySet<>();
    final ArraySet<String> mAllowInDataUsageSave = new ArraySet<>();
    final ArraySet<String> mAllowInPowerSave = new ArraySet<>();
    final ArraySet<String> mAllowInPowerSaveExceptIdle = new ArraySet<>();
    final ArraySet<String> mAllowUnthrottledLocation = new ArraySet<>();
    final ArrayMap<String, ArraySet<String>> mAllowedAssociations = new ArrayMap<>();
    final ArrayMap<String, FeatureInfo> mAvailableFeatures = new ArrayMap<>();
    final ArrayMap<String, FeatureInfo> mAvailableHwFeatures = new ArrayMap<>();
    final ArraySet<ComponentName> mBackupTransportWhitelist = new ArraySet<>();
    private final ArraySet<String> mBugreportWhitelistedPackages = new ArraySet<>();
    final ArraySet<ComponentName> mDefaultVrComponents = new ArraySet<>();
    final ArraySet<String> mDisabledUntilUsedPreinstalledCarrierApps = new ArraySet<>();
    final ArrayMap<String, List<String>> mDisabledUntilUsedPreinstalledCarrierAssociatedApps = new ArrayMap<>();
    int[] mGlobalGids;
    final ArraySet<String> mHiddenApiPackageWhitelist = new ArraySet<>();
    final ArraySet<String> mLinkedApps = new ArraySet<>();
    final ArrayMap<String, ArrayMap<String, Boolean>> mOemPermissions = new ArrayMap<>();
    final ArrayMap<String, PermissionEntry> mPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mPrivAppPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mProductPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mProductPrivAppPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mProductServicesPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mProductServicesPrivAppPermissions = new ArrayMap<>();
    final ArrayMap<String, SharedLibraryEntry> mSharedLibraries = new ArrayMap<>();
    final ArrayList<PermissionManager.SplitPermissionInfo> mSplitPermissions = new ArrayList<>();
    final SparseArray<ArraySet<String>> mSystemPermissions = new SparseArray<>();
    final ArraySet<String> mSystemUserBlacklistedApps = new ArraySet<>();
    final ArraySet<String> mSystemUserWhitelistedApps = new ArraySet<>();
    final ArraySet<String> mUnavailableFeatures = new ArraySet<>();
    final ArraySet<String> mUnavailableHwFeatures = new ArraySet<>();
    final ArrayMap<String, ArraySet<String>> mVendorPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mVendorPrivAppPermissions = new ArrayMap<>();

    public static final class SharedLibraryEntry {
        public final String[] dependencies;
        public final String filename;
        public final String name;

        SharedLibraryEntry(String name2, String filename2, String[] dependencies2) {
            this.name = name2;
            this.filename = filename2;
            this.dependencies = dependencies2;
        }
    }

    public static final class PermissionEntry {
        public int[] gids;
        public final String name;
        public boolean perUser;

        PermissionEntry(String name2, boolean perUser2) {
            this.name = name2;
            this.perUser = perUser2;
        }
    }

    public static SystemConfig getInstance() {
        SystemConfig systemConfig;
        synchronized (SystemConfig.class) {
            if (sInstance == null) {
                sInstance = new SystemConfig();
            }
            systemConfig = sInstance;
        }
        return systemConfig;
    }

    public int[] getGlobalGids() {
        return this.mGlobalGids;
    }

    public SparseArray<ArraySet<String>> getSystemPermissions() {
        return this.mSystemPermissions;
    }

    public ArrayList<PermissionManager.SplitPermissionInfo> getSplitPermissions() {
        return this.mSplitPermissions;
    }

    public ArrayMap<String, SharedLibraryEntry> getSharedLibraries() {
        return this.mSharedLibraries;
    }

    public ArrayMap<String, FeatureInfo> getAvailableFeatures() {
        return this.mAvailableFeatures;
    }

    public ArrayMap<String, FeatureInfo> getAvailableHwFeatures() {
        return this.mAvailableHwFeatures;
    }

    public ArrayMap<String, PermissionEntry> getPermissions() {
        return this.mPermissions;
    }

    public ArraySet<String> getAllowImplicitBroadcasts() {
        return this.mAllowImplicitBroadcasts;
    }

    public ArraySet<String> getAllowInPowerSaveExceptIdle() {
        return this.mAllowInPowerSaveExceptIdle;
    }

    public ArraySet<String> getAllowInPowerSave() {
        return this.mAllowInPowerSave;
    }

    public ArraySet<String> getAllowInDataUsageSave() {
        return this.mAllowInDataUsageSave;
    }

    public ArraySet<String> getAllowUnthrottledLocation() {
        return this.mAllowUnthrottledLocation;
    }

    public ArraySet<String> getAllowIgnoreLocationSettings() {
        return this.mAllowIgnoreLocationSettings;
    }

    public ArraySet<String> getLinkedApps() {
        return this.mLinkedApps;
    }

    public ArraySet<String> getSystemUserWhitelistedApps() {
        return this.mSystemUserWhitelistedApps;
    }

    public ArraySet<String> getSystemUserBlacklistedApps() {
        return this.mSystemUserBlacklistedApps;
    }

    public ArraySet<String> getHiddenApiWhitelistedApps() {
        return this.mHiddenApiPackageWhitelist;
    }

    public ArraySet<ComponentName> getDefaultVrComponents() {
        return this.mDefaultVrComponents;
    }

    public ArraySet<ComponentName> getBackupTransportWhitelist() {
        return this.mBackupTransportWhitelist;
    }

    public ArraySet<String> getDisabledUntilUsedPreinstalledCarrierApps() {
        return this.mDisabledUntilUsedPreinstalledCarrierApps;
    }

    public ArrayMap<String, List<String>> getDisabledUntilUsedPreinstalledCarrierAssociatedApps() {
        return this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps;
    }

    public ArraySet<String> getPrivAppPermissions(String packageName) {
        return this.mPrivAppPermissions.get(packageName);
    }

    public ArraySet<String> getPrivAppDenyPermissions(String packageName) {
        return this.mPrivAppDenyPermissions.get(packageName);
    }

    public ArraySet<String> getVendorPrivAppPermissions(String packageName) {
        return this.mVendorPrivAppPermissions.get(packageName);
    }

    public ArraySet<String> getVendorPrivAppDenyPermissions(String packageName) {
        return this.mVendorPrivAppDenyPermissions.get(packageName);
    }

    public ArraySet<String> getProductPrivAppPermissions(String packageName) {
        return this.mProductPrivAppPermissions.get(packageName);
    }

    public ArraySet<String> getProductPrivAppDenyPermissions(String packageName) {
        return this.mProductPrivAppDenyPermissions.get(packageName);
    }

    public ArraySet<String> getProductServicesPrivAppPermissions(String packageName) {
        return this.mProductServicesPrivAppPermissions.get(packageName);
    }

    public ArraySet<String> getProductServicesPrivAppDenyPermissions(String packageName) {
        return this.mProductServicesPrivAppDenyPermissions.get(packageName);
    }

    public Map<String, Boolean> getOemPermissions(String packageName) {
        Map<String, Boolean> oemPermissions = this.mOemPermissions.get(packageName);
        if (oemPermissions != null) {
            return oemPermissions;
        }
        return Collections.emptyMap();
    }

    public ArrayMap<String, ArraySet<String>> getAllowedAssociations() {
        return this.mAllowedAssociations;
    }

    public ArraySet<String> getBugreportWhitelistedPackages() {
        return this.mBugreportWhitelistedPackages;
    }

    SystemConfig() {
        readPermissions(Environment.buildPath(Environment.getRootDirectory(), "etc", "sysconfig"), -1);
        readPermissions(Environment.buildPath(Environment.getRootDirectory(), "etc", "permissions"), -1);
        int vendorPermissionFlag = Build.VERSION.FIRST_SDK_INT <= 27 ? 147 | 12 : 147;
        readPermissions(Environment.buildPath(Environment.getVendorDirectory(), "etc", "sysconfig"), vendorPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getVendorDirectory(), "etc", "permissions"), vendorPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getOdmDirectory(), "etc", "sysconfig"), vendorPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getOdmDirectory(), "etc", "permissions"), vendorPermissionFlag);
        String skuProperty = SystemProperties.get(SKU_PROPERTY, "");
        if (!skuProperty.isEmpty()) {
            String skuDir = "sku_" + skuProperty;
            readPermissions(Environment.buildPath(Environment.getOdmDirectory(), "etc", "sysconfig", skuDir), vendorPermissionFlag);
            readPermissions(Environment.buildPath(Environment.getOdmDirectory(), "etc", "permissions", skuDir), vendorPermissionFlag);
        }
        readPermissions(Environment.buildPath(Environment.getOemDirectory(), "etc", "sysconfig"), 161);
        readPermissions(Environment.buildPath(Environment.getOemDirectory(), "etc", "permissions"), 161);
        readPermissions(Environment.buildPath(Environment.getProductDirectory(), "etc", "sysconfig"), -1);
        readPermissions(Environment.buildPath(Environment.getProductDirectory(), "etc", "permissions"), -1);
        readPermissions(Environment.buildPath(Environment.getProductServicesDirectory(), "etc", "sysconfig"), -1);
        readPermissions(Environment.buildPath(Environment.getProductServicesDirectory(), "etc", "permissions"), -1);
        readCustPermissions();
        if (SystemProperties.getBoolean("ro.config.eea_enable", false)) {
            addFeature("com.google.android.feature.EEA_DEVICE", 0);
            addFeature("com.google.android.paid.search", 0);
            addFeature("com.google.android.paid.chrome", 0);
        }
        if (SystemProperties.getBoolean("hw_sc.digitalwellbing_enable", false)) {
            addFeature("com.google.android.feature.WELLBEING", 0);
        }
        if (SystemProperties.getBoolean("hw_sc.trfeature_enable", false)) {
            addFeature("com.google.android.feature.TR_DEVICE", 0);
            addFeature("com.google.android.paid.qsb_widget", 0);
        }
        if (!SystemProperties.getBoolean("ro.config.ztpsupport", true)) {
            removeFeature("com.google.android.feature.ZERO_TOUCH");
        }
        Object obj = this.hwCustSystemConfigObj;
        if (obj instanceof HwCustSystemConfig) {
            ((HwCustSystemConfig) obj).readOverlayFeaturesAndClearOld(this.mAvailableFeatures, this.mUnavailableFeatures);
        }
    }

    public void readCustPermissions() {
        String[] dirs = new String[0];
        String sysPath = getCanonicalPathOrNull(Environment.buildPath(Environment.getRootDirectory(), "etc"));
        try {
            dirs = HwCfgFilePolicy.getCfgPolicyDir(0);
        } catch (NoClassDefFoundError e) {
            Slog.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (sysPath != null && dirs.length > 0) {
            for (String dir : dirs) {
                File file = new File(dir);
                String dirPath = getCanonicalPathOrNull(file);
                if (dirPath != null && !dirPath.equals(sysPath)) {
                    readPermissions(Environment.buildPath(file, "sysconfig"), -1);
                    readPermissions(Environment.buildPath(file, "permissions"), -1);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String getCanonicalPathOrNull(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            Slog.d(TAG, "Unable to resolve canonical");
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void readPermissions(File libraryDir, int permissionFlag) {
        if (!libraryDir.exists() || !libraryDir.isDirectory()) {
            if (permissionFlag == -1) {
                Slog.w(TAG, "No directory " + libraryDir + ", skipping");
            }
        } else if (!libraryDir.canRead()) {
            Slog.w(TAG, "Directory " + libraryDir + " cannot be read");
        } else {
            File platformFile = null;
            File[] listFiles = libraryDir.listFiles();
            for (File f : listFiles) {
                if (f.isFile()) {
                    if (f.getPath().endsWith("etc/permissions/platform.xml")) {
                        platformFile = f;
                    } else if (!f.getPath().endsWith(".xml")) {
                        Slog.i(TAG, "Non-xml file " + f + " in " + libraryDir + " directory, ignoring");
                    } else if (!f.canRead()) {
                        Slog.w(TAG, "Permissions library file " + f + " cannot be read");
                    } else {
                        readPermissionsFromXml(f, permissionFlag);
                    }
                }
            }
            if (platformFile != null) {
                readPermissionsFromXml(platformFile, permissionFlag);
            }
        }
    }

    private void logNotAllowedInPartition(String name, File permFile, XmlPullParser parser) {
        Slog.w(TAG, "<" + name + "> not allowed in partition of " + permFile + " at " + parser.getPositionDescription());
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:449:0x0bbc, code lost:
        if (0 != 0) goto L_0x0bfa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:450:0x0bbe, code lost:
        reportReadPermissionsError(r32.getName(), r32.getPath(), r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:457:0x0be1, code lost:
        if (0 != 0) goto L_0x0bfa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:461:0x0bf7, code lost:
        if (0 != 0) goto L_0x0bfa;
     */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x04e9 A[Catch:{ XmlPullParserException -> 0x0b98, IOException -> 0x0b95, Exception -> 0x0b92, all -> 0x0b8c }] */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x04f1 A[Catch:{ XmlPullParserException -> 0x0b98, IOException -> 0x0b95, Exception -> 0x0b92, all -> 0x0b8c }] */
    /* JADX WARNING: Removed duplicated region for block: B:488:0x0c66  */
    private void readPermissionsFromXml(File permFile, int permissionFlag) {
        FileReader permReader;
        boolean isSuccess;
        Throwable th;
        int i;
        XmlPullParserException e;
        String str;
        IOException e2;
        String exceptionName;
        Exception e3;
        XmlPullParser parser;
        int type;
        char c;
        boolean z;
        String str2;
        boolean allowed;
        boolean vendor2;
        boolean allowed2;
        String str3 = "Got exception parsing permissions.";
        String str4 = "/";
        try {
            FileReader permReader2 = new FileReader(permFile);
            boolean lowRam = ActivityManager.isLowRamDeviceStatic();
            boolean isSuccess2 = true;
            String exceptionName2 = "";
            try {
                parser = Xml.newPullParser();
                parser.setInput(permReader2);
                if (type == 2) {
                    if (!parser.getName().equals("permissions")) {
                        try {
                            if (!parser.getName().equals("config")) {
                                throw new XmlPullParserException("Unexpected start tag in " + permFile + ": found " + parser.getName() + ", expected 'permissions' or 'config'");
                            }
                        } catch (XmlPullParserException e4) {
                            e = e4;
                            permReader = permReader2;
                            exceptionName2 = "XmlPullParserException";
                            Slog.w(TAG, str3, e);
                            IoUtils.closeQuietly(permReader);
                        } catch (IOException e5) {
                            e2 = e5;
                            str = str3;
                            permReader = permReader2;
                            exceptionName2 = "IOException";
                            try {
                                Slog.w(TAG, str, e2);
                                IoUtils.closeQuietly(permReader);
                            } catch (Throwable th2) {
                                th = th2;
                                isSuccess = false;
                                IoUtils.closeQuietly(permReader);
                                if (!isSuccess) {
                                }
                                throw th;
                            }
                        } catch (Exception e6) {
                            e3 = e6;
                            permReader = permReader2;
                            exceptionName = exceptionName2;
                            try {
                                exceptionName2 = e3.getClass().toString();
                                IoUtils.closeQuietly(permReader);
                            } catch (Throwable th3) {
                                th = th3;
                                isSuccess = false;
                                exceptionName2 = exceptionName;
                                IoUtils.closeQuietly(permReader);
                                if (!isSuccess) {
                                }
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            permReader = permReader2;
                            isSuccess = true;
                            IoUtils.closeQuietly(permReader);
                            if (!isSuccess) {
                            }
                            throw th;
                        }
                    }
                    boolean allowAll = permissionFlag == -1;
                    boolean allowLibs = (permissionFlag & 2) != 0;
                    boolean allowFeatures = (permissionFlag & 1) != 0;
                    boolean allowPermissions = (permissionFlag & 4) != 0;
                    boolean allowAppConfigs = (permissionFlag & 8) != 0;
                    boolean allowPrivappPermissions = (permissionFlag & 16) != 0;
                    boolean allowOemPermissions = (permissionFlag & 32) != 0;
                    boolean allowApiWhitelisting = (permissionFlag & 64) != 0;
                    boolean allowAssociations = (permissionFlag & 128) != 0;
                    while (true) {
                        XmlUtils.nextElement(parser);
                        if (parser.getEventType() == 1) {
                            IoUtils.closeQuietly(permReader2);
                            if (!isSuccess2) {
                                reportReadPermissionsError(permFile.getName(), permFile.getPath(), exceptionName2);
                            }
                            if (StorageManager.isFileEncryptedNativeOnly()) {
                                i = 0;
                                addFeature(PackageManager.FEATURE_FILE_BASED_ENCRYPTION, 0);
                                addFeature(PackageManager.FEATURE_SECURELY_REMOVES_USERS, 0);
                            } else {
                                i = 0;
                            }
                            if (StorageManager.hasAdoptable()) {
                                addFeature(PackageManager.FEATURE_ADOPTABLE_STORAGE, i);
                            }
                            if (ActivityManager.isLowRamDeviceStatic()) {
                                addFeature(PackageManager.FEATURE_RAM_LOW, i);
                            } else {
                                addFeature(PackageManager.FEATURE_RAM_NORMAL, i);
                            }
                            Iterator<String> it = this.mUnavailableFeatures.iterator();
                            while (it.hasNext()) {
                                removeFeature(it.next());
                            }
                            Iterator<String> it2 = this.mUnavailableHwFeatures.iterator();
                            while (it2.hasNext()) {
                                removeHwFeature(it2.next());
                            }
                            return;
                        }
                        String name = parser.getName();
                        if (name == null) {
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            switch (name.hashCode()) {
                                case -2040330235:
                                    if (name.equals("allow-unthrottled-location")) {
                                        c = '\n';
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -1882490007:
                                    if (name.equals("allow-in-power-save")) {
                                        c = '\b';
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -1005864890:
                                    if (name.equals("disabled-until-used-preinstalled-carrier-app")) {
                                        c = 19;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -980620291:
                                    if (name.equals("allow-association")) {
                                        c = 23;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -979207434:
                                    if (name.equals("feature")) {
                                        c = 5;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -851582420:
                                    if (name.equals("system-user-blacklisted-app")) {
                                        c = 15;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -828905863:
                                    if (name.equals("unavailable-feature")) {
                                        c = 6;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -642819164:
                                    if (name.equals("allow-in-power-save-except-idle")) {
                                        c = 7;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -560717308:
                                    if (name.equals("allow-ignore-location-settings")) {
                                        c = 11;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -517618225:
                                    if (name.equals("permission")) {
                                        c = 1;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -328989238:
                                    if (name.equals("unavailable-hwfeature")) {
                                        c = 26;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 98629247:
                                    if (name.equals(WifiConfiguration.GroupCipher.varName)) {
                                        c = 0;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 166208699:
                                    if (name.equals("library")) {
                                        c = 4;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 180165796:
                                    if (name.equals("hidden-api-whitelisted-app")) {
                                        c = 22;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 347247519:
                                    if (name.equals("backup-transport-whitelisted-service")) {
                                        c = 17;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 508457430:
                                    if (name.equals("system-user-whitelisted-app")) {
                                        c = 14;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 802332808:
                                    if (name.equals("allow-in-data-usage-save")) {
                                        c = '\t';
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 953292141:
                                    if (name.equals("assign-permission")) {
                                        c = 2;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1044015374:
                                    if (name.equals("oem-permissions")) {
                                        c = 21;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1121420326:
                                    if (name.equals("app-link")) {
                                        c = '\r';
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1260089095:
                                    if (name.equals("hwfeature")) {
                                        c = 25;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1269564002:
                                    if (name.equals("split-permission")) {
                                        c = 3;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1567330472:
                                    if (name.equals("default-enabled-vr-app")) {
                                        c = 16;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1633270165:
                                    if (name.equals("disabled-until-used-preinstalled-carrier-associated-app")) {
                                        c = 18;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1723146313:
                                    if (name.equals("privapp-permissions")) {
                                        c = 20;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1723586945:
                                    if (name.equals("bugreport-whitelisted")) {
                                        c = 24;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1954925533:
                                    if (name.equals("allow-implicit-broadcast")) {
                                        c = '\f';
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                default:
                                    c = 65535;
                                    break;
                            }
                            isSuccess = isSuccess2;
                            exceptionName = exceptionName2;
                            str = str3;
                            permReader = permReader2;
                            switch (c) {
                                case 0:
                                    str2 = str4;
                                    z = true;
                                    if (allowAll) {
                                        String gidStr = parser.getAttributeValue(null, "gid");
                                        if (gidStr != null) {
                                            this.mGlobalGids = ArrayUtils.appendInt(this.mGlobalGids, Process.getGidForName(gidStr));
                                        } else {
                                            Slog.w(TAG, "<" + name + "> without gid in " + permFile + " at " + parser.getPositionDescription());
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    break;
                                case 1:
                                    str2 = str4;
                                    z = true;
                                    if (!allowPermissions) {
                                        logNotAllowedInPartition(name, permFile, parser);
                                        XmlUtils.skipCurrentTag(parser);
                                        break;
                                    } else {
                                        String perm = parser.getAttributeValue(null, "name");
                                        if (perm != null) {
                                            readPermission(parser, perm.intern());
                                            break;
                                        } else {
                                            Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                        }
                                    }
                                case 2:
                                    str2 = str4;
                                    z = true;
                                    if (allowPermissions) {
                                        String perm2 = parser.getAttributeValue(null, "name");
                                        if (perm2 == null) {
                                            Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                        } else {
                                            String uidStr = parser.getAttributeValue(null, "uid");
                                            if (uidStr == null) {
                                                Slog.w(TAG, "<" + name + "> without uid in " + permFile + " at " + parser.getPositionDescription());
                                                XmlUtils.skipCurrentTag(parser);
                                                break;
                                            } else {
                                                int uid = Process.getUidForName(uidStr);
                                                if (uid < 0) {
                                                    Slog.w(TAG, "<" + name + "> with unknown uid \"" + uidStr + "  in " + permFile + " at " + parser.getPositionDescription());
                                                    XmlUtils.skipCurrentTag(parser);
                                                    break;
                                                } else {
                                                    String perm3 = perm2.intern();
                                                    ArraySet<String> perms = this.mSystemPermissions.get(uid);
                                                    if (perms == null) {
                                                        perms = new ArraySet<>();
                                                        this.mSystemPermissions.put(uid, perms);
                                                    }
                                                    perms.add(perm3);
                                                }
                                            }
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    break;
                                case 3:
                                    str2 = str4;
                                    z = true;
                                    if (!allowPermissions) {
                                        logNotAllowedInPartition(name, permFile, parser);
                                        XmlUtils.skipCurrentTag(parser);
                                        break;
                                    } else {
                                        readSplitPermission(parser, permFile);
                                        break;
                                    }
                                case 4:
                                    str2 = str4;
                                    z = true;
                                    if (allowLibs) {
                                        String lname = parser.getAttributeValue(null, "name");
                                        String lfile = parser.getAttributeValue(null, ContentResolver.SCHEME_FILE);
                                        String ldependency = parser.getAttributeValue(null, "dependency");
                                        if (lname == null) {
                                            Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                        } else if (lfile == null) {
                                            Slog.w(TAG, "<" + name + "> without file in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mSharedLibraries.put(lname, new SharedLibraryEntry(lname, lfile, ldependency == null ? new String[0] : ldependency.split(SettingsStringUtil.DELIMITER)));
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    break;
                                case 5:
                                    str2 = str4;
                                    if (allowFeatures) {
                                        String fname = parser.getAttributeValue(null, "name");
                                        int fversion = XmlUtils.readIntAttribute(parser, "version", 0);
                                        if (!lowRam) {
                                            allowed = true;
                                            z = true;
                                        } else {
                                            z = true;
                                            allowed = !"true".equals(parser.getAttributeValue(null, "notLowRam"));
                                        }
                                        if (fname == null) {
                                            Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                        } else if (allowed) {
                                            addFeature(fname, fversion);
                                        }
                                    } else {
                                        z = true;
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    break;
                                case 6:
                                    str2 = str4;
                                    if (allowFeatures) {
                                        String fname2 = parser.getAttributeValue(null, "name");
                                        if (fname2 == null) {
                                            Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mUnavailableFeatures.add(fname2);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 7:
                                    str2 = str4;
                                    if (allowAll) {
                                        String pkgname = parser.getAttributeValue(null, "package");
                                        if (pkgname == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mAllowInPowerSaveExceptIdle.add(pkgname);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case '\b':
                                    str2 = str4;
                                    if (allowAll) {
                                        String pkgname2 = parser.getAttributeValue(null, "package");
                                        if (pkgname2 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mAllowInPowerSave.add(pkgname2);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case '\t':
                                    str2 = str4;
                                    if (allowAll) {
                                        String pkgname3 = parser.getAttributeValue(null, "package");
                                        if (pkgname3 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mAllowInDataUsageSave.add(pkgname3);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case '\n':
                                    str2 = str4;
                                    if (allowAll) {
                                        String pkgname4 = parser.getAttributeValue(null, "package");
                                        if (pkgname4 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mAllowUnthrottledLocation.add(pkgname4);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 11:
                                    str2 = str4;
                                    if (allowAll) {
                                        String pkgname5 = parser.getAttributeValue(null, "package");
                                        if (pkgname5 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mAllowIgnoreLocationSettings.add(pkgname5);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case '\f':
                                    str2 = str4;
                                    if (allowAll) {
                                        String action = parser.getAttributeValue(null, "action");
                                        if (action == null) {
                                            Slog.w(TAG, "<" + name + "> without action in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mAllowImplicitBroadcasts.add(action);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case '\r':
                                    str2 = str4;
                                    if (allowAppConfigs) {
                                        String pkgname6 = parser.getAttributeValue(null, "package");
                                        if (pkgname6 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mLinkedApps.add(pkgname6);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 14:
                                    str2 = str4;
                                    if (allowAppConfigs) {
                                        String pkgname7 = parser.getAttributeValue(null, "package");
                                        if (pkgname7 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mSystemUserWhitelistedApps.add(pkgname7);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 15:
                                    str2 = str4;
                                    if (allowAppConfigs) {
                                        String pkgname8 = parser.getAttributeValue(null, "package");
                                        if (pkgname8 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mSystemUserBlacklistedApps.add(pkgname8);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 16:
                                    str2 = str4;
                                    if (allowAppConfigs) {
                                        String pkgname9 = parser.getAttributeValue(null, "package");
                                        String clsname = parser.getAttributeValue(null, "class");
                                        if (pkgname9 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else if (clsname == null) {
                                            Slog.w(TAG, "<" + name + "> without class in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mDefaultVrComponents.add(new ComponentName(pkgname9, clsname));
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 17:
                                    str2 = str4;
                                    if (allowFeatures) {
                                        String serviceName = parser.getAttributeValue(null, "service");
                                        if (serviceName == null) {
                                            Slog.w(TAG, "<" + name + "> without service in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            ComponentName cn = ComponentName.unflattenFromString(serviceName);
                                            if (cn == null) {
                                                Slog.w(TAG, "<" + name + "> with invalid service name " + serviceName + " in " + permFile + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mBackupTransportWhitelist.add(cn);
                                            }
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 18:
                                    str2 = str4;
                                    if (allowAppConfigs) {
                                        String pkgname10 = parser.getAttributeValue(null, "package");
                                        String carrierPkgname = parser.getAttributeValue(null, "carrierAppPackage");
                                        if (pkgname10 != null) {
                                            if (carrierPkgname != null) {
                                                List<String> associatedPkgs = this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps.get(carrierPkgname);
                                                if (associatedPkgs == null) {
                                                    associatedPkgs = new ArrayList();
                                                    this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps.put(carrierPkgname, associatedPkgs);
                                                }
                                                associatedPkgs.add(pkgname10);
                                            }
                                        }
                                        Slog.w(TAG, "<" + name + "> without package or carrierAppPackage in " + permFile + " at " + parser.getPositionDescription());
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 19:
                                    str2 = str4;
                                    if (allowAppConfigs) {
                                        String pkgname11 = parser.getAttributeValue(null, "package");
                                        if (pkgname11 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mDisabledUntilUsedPreinstalledCarrierApps.add(pkgname11);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    z = true;
                                    break;
                                case 20:
                                    if (!allowPrivappPermissions) {
                                        str2 = str4;
                                        logNotAllowedInPartition(name, permFile, parser);
                                        XmlUtils.skipCurrentTag(parser);
                                        z = true;
                                        break;
                                    } else {
                                        Path path = permFile.toPath();
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(Environment.getVendorDirectory().toPath());
                                        str2 = str4;
                                        sb.append(str2);
                                        if (!path.startsWith(sb.toString())) {
                                            if (!permFile.toPath().startsWith(Environment.getOdmDirectory().toPath() + str2)) {
                                                vendor2 = false;
                                                boolean product = permFile.toPath().startsWith(Environment.getProductDirectory().toPath() + str2);
                                                boolean productServices = permFile.toPath().startsWith(Environment.getProductServicesDirectory().toPath() + str2);
                                                if (!vendor2) {
                                                    readPrivAppPermissions(parser, this.mVendorPrivAppPermissions, this.mVendorPrivAppDenyPermissions);
                                                } else if (product) {
                                                    readPrivAppPermissions(parser, this.mProductPrivAppPermissions, this.mProductPrivAppDenyPermissions);
                                                } else if (productServices) {
                                                    readPrivAppPermissions(parser, this.mProductServicesPrivAppPermissions, this.mProductServicesPrivAppDenyPermissions);
                                                } else {
                                                    readPrivAppPermissions(parser, this.mPrivAppPermissions, this.mPrivAppDenyPermissions);
                                                }
                                                z = true;
                                                break;
                                            }
                                        }
                                        vendor2 = true;
                                        boolean product2 = permFile.toPath().startsWith(Environment.getProductDirectory().toPath() + str2);
                                        boolean productServices2 = permFile.toPath().startsWith(Environment.getProductServicesDirectory().toPath() + str2);
                                        if (!vendor2) {
                                        }
                                        z = true;
                                    }
                                case 21:
                                    if (!allowOemPermissions) {
                                        logNotAllowedInPartition(name, permFile, parser);
                                        XmlUtils.skipCurrentTag(parser);
                                        str2 = str4;
                                        z = true;
                                        break;
                                    } else {
                                        readOemPermissions(parser);
                                        str2 = str4;
                                        z = true;
                                        break;
                                    }
                                case 22:
                                    if (allowApiWhitelisting) {
                                        String pkgname12 = parser.getAttributeValue(null, "package");
                                        if (pkgname12 == null) {
                                            Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                        } else {
                                            this.mHiddenApiPackageWhitelist.add(pkgname12);
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    str2 = str4;
                                    z = true;
                                    break;
                                case 23:
                                    if (allowAssociations) {
                                        String target = parser.getAttributeValue(null, "target");
                                        if (target == null) {
                                            Slog.w(TAG, "<" + name + "> without target in " + permFile + " at " + parser.getPositionDescription());
                                            XmlUtils.skipCurrentTag(parser);
                                            str2 = str4;
                                            z = true;
                                            break;
                                        } else {
                                            String allowed3 = parser.getAttributeValue(null, "allowed");
                                            if (allowed3 == null) {
                                                Slog.w(TAG, "<" + name + "> without allowed in " + permFile + " at " + parser.getPositionDescription());
                                                XmlUtils.skipCurrentTag(parser);
                                                str2 = str4;
                                                z = true;
                                                break;
                                            } else {
                                                String target2 = target.intern();
                                                String allowed4 = allowed3.intern();
                                                ArraySet<String> associations = this.mAllowedAssociations.get(target2);
                                                if (associations == null) {
                                                    associations = new ArraySet<>();
                                                    this.mAllowedAssociations.put(target2, associations);
                                                }
                                                Slog.i(TAG, "Adding association: " + target2 + " <- " + allowed4);
                                                associations.add(allowed4);
                                            }
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    str2 = str4;
                                    z = true;
                                    break;
                                case 24:
                                    String pkgname13 = parser.getAttributeValue(null, "package");
                                    if (pkgname13 == null) {
                                        Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                    } else {
                                        this.mBugreportWhitelistedPackages.add(pkgname13);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    str2 = str4;
                                    z = true;
                                    break;
                                case 25:
                                    if (allowFeatures) {
                                        String featureName = parser.getAttributeValue(null, "name");
                                        int featureVersion = XmlUtils.readIntAttribute(parser, "version", 0);
                                        if (!lowRam) {
                                            allowed2 = true;
                                        } else {
                                            allowed2 = !"true".equals(parser.getAttributeValue(null, "notLowRam"));
                                        }
                                        if (featureName == null) {
                                            Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                        } else if (allowed2) {
                                            addHwFeature(featureName, featureVersion);
                                        } else {
                                            Slog.d(TAG, "lowRam cause not allow feature");
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    str2 = str4;
                                    z = true;
                                    break;
                                case 26:
                                    if (allowFeatures) {
                                        try {
                                            String featureName2 = parser.getAttributeValue(null, "name");
                                            if (featureName2 == null) {
                                                Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mUnavailableHwFeatures.add(featureName2);
                                            }
                                        } catch (XmlPullParserException e7) {
                                            e = e7;
                                            str3 = str;
                                            exceptionName2 = "XmlPullParserException";
                                            Slog.w(TAG, str3, e);
                                            IoUtils.closeQuietly(permReader);
                                            break;
                                        } catch (IOException e8) {
                                            e2 = e8;
                                            exceptionName2 = "IOException";
                                            Slog.w(TAG, str, e2);
                                            IoUtils.closeQuietly(permReader);
                                            break;
                                        } catch (Exception e9) {
                                            e3 = e9;
                                            exceptionName2 = e3.getClass().toString();
                                            IoUtils.closeQuietly(permReader);
                                            break;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            exceptionName2 = exceptionName;
                                            IoUtils.closeQuietly(permReader);
                                            if (!isSuccess) {
                                                reportReadPermissionsError(permFile.getName(), permFile.getPath(), exceptionName2);
                                            }
                                            throw th;
                                        }
                                    } else {
                                        logNotAllowedInPartition(name, permFile, parser);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                    str2 = str4;
                                    z = true;
                                    break;
                                default:
                                    str2 = str4;
                                    z = true;
                                    Slog.w(TAG, "Tag " + name + " is unknown in " + permFile + " at " + parser.getPositionDescription());
                                    XmlUtils.skipCurrentTag(parser);
                                    break;
                            }
                            str4 = str2;
                            isSuccess2 = isSuccess;
                            exceptionName2 = exceptionName;
                            type = type;
                            str3 = str;
                            permReader2 = permReader;
                            allowAll = allowAll;
                        }
                    }
                } else {
                    throw new XmlPullParserException("No start tag found");
                }
            } catch (XmlPullParserException e10) {
                permReader = permReader2;
                e = e10;
                exceptionName2 = "XmlPullParserException";
                Slog.w(TAG, str3, e);
                IoUtils.closeQuietly(permReader);
            } catch (IOException e11) {
                str = str3;
                permReader = permReader2;
                e2 = e11;
                exceptionName2 = "IOException";
                Slog.w(TAG, str, e2);
                IoUtils.closeQuietly(permReader);
            } catch (Exception e12) {
                permReader = permReader2;
                exceptionName = exceptionName2;
                e3 = e12;
                exceptionName2 = e3.getClass().toString();
                IoUtils.closeQuietly(permReader);
            } catch (Throwable th6) {
                permReader = permReader2;
                isSuccess = true;
                th = th6;
                IoUtils.closeQuietly(permReader);
                if (!isSuccess) {
                }
                throw th;
            }
        } catch (FileNotFoundException e13) {
            Slog.w(TAG, "Couldn't find or open permissions file " + permFile);
            return;
        }
        while (true) {
            int next = parser.next();
            type = next;
            if (next == 2 || type == 1) {
                break;
            }
        }
    }

    private void reportReadPermissionsError(String fileName, String filePath, String exceptionName) {
        if (fileName != null && filePath != null) {
            String[] subPaths = filePath.split("/");
            String rootPath = (subPaths.length <= 1 || !"".equals(subPaths[0])) ? subPaths[0] : subPaths[1];
            Slog.e(TAG, exceptionName + " Error reading permissions file:" + fileName + " in " + rootPath);
            Bundle data = new Bundle();
            data.putString(HwFrameworkMonitor.FILE_NAME, fileName);
            data.putString(HwFrameworkMonitor.EXCEPTION_NAME, exceptionName);
            data.putString(HwFrameworkMonitor.AREA_NAME, rootPath);
            HwFrameworkMonitor hwFwkMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
            if (hwFwkMonitor == null || !hwFwkMonitor.monitor(HwFrameworkMonitor.SCENE_PMS_PARSE_FILE_EXCEPTION, data)) {
                Slog.i(TAG, "upload bigdata fail for: " + fileName);
                return;
            }
            Slog.i(TAG, "upload bigdata success for: " + fileName);
        }
    }

    private void addFeature(String name, int version) {
        FeatureInfo fi = this.mAvailableFeatures.get(name);
        if (fi == null) {
            FeatureInfo fi2 = new FeatureInfo();
            fi2.name = name;
            fi2.version = version;
            this.mAvailableFeatures.put(name, fi2);
            return;
        }
        fi.version = Math.max(fi.version, version);
    }

    private void removeFeature(String name) {
        if (this.mAvailableFeatures.remove(name) != null) {
            Slog.d(TAG, "Removed unavailable feature " + name);
        }
    }

    private void addHwFeature(String name, int version) {
        FeatureInfo fi = this.mAvailableHwFeatures.get(name);
        if (fi == null) {
            FeatureInfo fi2 = new FeatureInfo();
            fi2.name = name;
            fi2.version = version;
            this.mAvailableHwFeatures.put(name, fi2);
            return;
        }
        fi.version = Math.max(fi.version, version);
    }

    private void removeHwFeature(String name) {
        if (this.mAvailableHwFeatures.remove(name) != null) {
            Slog.d(TAG, "Removed unavailable hwfeature " + name);
        }
    }

    /* access modifiers changed from: package-private */
    public void readPermission(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        if (!this.mPermissions.containsKey(name)) {
            PermissionEntry perm = new PermissionEntry(name, XmlUtils.readBooleanAttribute(parser, "perUser", false));
            this.mPermissions.put(name, perm);
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
                    if (WifiConfiguration.GroupCipher.varName.equals(parser.getName())) {
                        String gidStr = parser.getAttributeValue(null, "gid");
                        if (gidStr != null) {
                            perm.gids = ArrayUtils.appendInt(perm.gids, Process.getGidForName(gidStr));
                        } else {
                            Slog.w(TAG, "<group> without gid at " + parser.getPositionDescription());
                        }
                    }
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        } else {
            throw new IllegalStateException("Duplicate permission definition for " + name);
        }
    }

    private void readPrivAppPermissions(XmlPullParser parser, ArrayMap<String, ArraySet<String>> grantMap, ArrayMap<String, ArraySet<String>> denyMap) throws IOException, XmlPullParserException {
        String packageName = parser.getAttributeValue(null, "package");
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "package is required for <privapp-permissions> in " + parser.getPositionDescription());
            return;
        }
        ArraySet<String> permissions = grantMap.get(packageName);
        if (permissions == null) {
            permissions = new ArraySet<>();
        }
        ArraySet<String> denyPermissions = denyMap.get(packageName);
        int depth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, depth)) {
            String name = parser.getName();
            if ("permission".equals(name)) {
                String permName = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(permName)) {
                    Slog.w(TAG, "name is required for <permission> in " + parser.getPositionDescription());
                } else {
                    permissions.add(permName);
                }
            } else if ("deny-permission".equals(name)) {
                String permName2 = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(permName2)) {
                    Slog.w(TAG, "name is required for <deny-permission> in " + parser.getPositionDescription());
                } else {
                    if (denyPermissions == null) {
                        denyPermissions = new ArraySet<>();
                    }
                    denyPermissions.add(permName2);
                }
            }
        }
        grantMap.put(packageName, permissions);
        if (denyPermissions != null) {
            denyMap.put(packageName, denyPermissions);
        }
    }

    /* access modifiers changed from: package-private */
    public void readOemPermissions(XmlPullParser parser) throws IOException, XmlPullParserException {
        String packageName = parser.getAttributeValue(null, "package");
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "package is required for <oem-permissions> in " + parser.getPositionDescription());
            return;
        }
        ArrayMap<String, Boolean> permissions = this.mOemPermissions.get(packageName);
        if (permissions == null) {
            permissions = new ArrayMap<>();
        }
        int depth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, depth)) {
            String name = parser.getName();
            if ("permission".equals(name)) {
                String permName = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(permName)) {
                    Slog.w(TAG, "name is required for <permission> in " + parser.getPositionDescription());
                } else {
                    permissions.put(permName, Boolean.TRUE);
                }
            } else if ("deny-permission".equals(name)) {
                String permName2 = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(permName2)) {
                    Slog.w(TAG, "name is required for <deny-permission> in " + parser.getPositionDescription());
                } else {
                    permissions.put(permName2, Boolean.FALSE);
                }
            }
        }
        this.mOemPermissions.put(packageName, permissions);
    }

    private void readSplitPermission(XmlPullParser parser, File permFile) throws IOException, XmlPullParserException {
        String splitPerm = parser.getAttributeValue(null, "name");
        if (splitPerm == null) {
            Slog.w(TAG, "<split-permission> without name in " + permFile + " at " + parser.getPositionDescription());
            XmlUtils.skipCurrentTag(parser);
            return;
        }
        String targetSdkStr = parser.getAttributeValue(null, "targetSdk");
        int targetSdk = 10001;
        if (!TextUtils.isEmpty(targetSdkStr)) {
            try {
                targetSdk = Integer.parseInt(targetSdkStr);
            } catch (NumberFormatException e) {
                Slog.w(TAG, "<split-permission> targetSdk not an integer in " + permFile + " at " + parser.getPositionDescription());
                XmlUtils.skipCurrentTag(parser);
                return;
            }
        }
        int depth = parser.getDepth();
        List<String> newPermissions = new ArrayList<>();
        while (XmlUtils.nextElementWithin(parser, depth)) {
            if ("new-permission".equals(parser.getName())) {
                String newName = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(newName)) {
                    Slog.w(TAG, "name is required for <new-permission> in " + parser.getPositionDescription());
                } else {
                    newPermissions.add(newName);
                }
            } else {
                XmlUtils.skipCurrentTag(parser);
            }
        }
        if (!newPermissions.isEmpty()) {
            this.mSplitPermissions.add(new PermissionManager.SplitPermissionInfo(splitPerm, newPermissions, targetSdk));
        }
    }
}
