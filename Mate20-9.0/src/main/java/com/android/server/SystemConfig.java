package com.android.server;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.pm.FeatureInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
    private static final int ALLOW_FEATURES = 1;
    private static final int ALLOW_HIDDENAPI_WHITELISTING = 64;
    private static final int ALLOW_LIBS = 2;
    private static final int ALLOW_OEM_PERMISSIONS = 32;
    private static final int ALLOW_PERMISSIONS = 4;
    private static final int ALLOW_PRIVAPP_PERMISSIONS = 16;
    private static final String CHARACTERISTICS = SystemProperties.get("ro.build.characteristics", "");
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    static final String TAG = "SystemConfig";
    static SystemConfig sInstance;
    final int CUST_TYPE_CONFIG = 0;
    final ArraySet<String> mAllowImplicitBroadcasts = new ArraySet<>();
    final ArraySet<String> mAllowInDataUsageSave = new ArraySet<>();
    final ArraySet<String> mAllowInPowerSave = new ArraySet<>();
    final ArraySet<String> mAllowInPowerSaveExceptIdle = new ArraySet<>();
    final ArraySet<String> mAllowUnthrottledLocation = new ArraySet<>();
    final ArrayMap<String, FeatureInfo> mAvailableFeatures = new ArrayMap<>();
    final ArraySet<ComponentName> mBackupTransportWhitelist = new ArraySet<>();
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
    final ArrayMap<String, String> mSharedLibraries = new ArrayMap<>();
    final SparseArray<ArraySet<String>> mSystemPermissions = new SparseArray<>();
    final ArraySet<String> mSystemUserBlacklistedApps = new ArraySet<>();
    final ArraySet<String> mSystemUserWhitelistedApps = new ArraySet<>();
    final ArraySet<String> mUnavailableFeatures = new ArraySet<>();
    final ArrayMap<String, ArraySet<String>> mVendorPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mVendorPrivAppPermissions = new ArrayMap<>();

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

    public ArrayMap<String, String> getSharedLibraries() {
        return this.mSharedLibraries;
    }

    public ArrayMap<String, FeatureInfo> getAvailableFeatures() {
        return this.mAvailableFeatures;
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

    public Map<String, Boolean> getOemPermissions(String packageName) {
        Map<String, Boolean> oemPermissions = this.mOemPermissions.get(packageName);
        if (oemPermissions != null) {
            return oemPermissions;
        }
        return Collections.emptyMap();
    }

    SystemConfig() {
        readPermissions(Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "sysconfig"}), -1);
        readPermissions(Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "permissions"}), -1);
        int vendorPermissionFlag = 19;
        vendorPermissionFlag = Build.VERSION.FIRST_SDK_INT <= 27 ? 19 | 12 : vendorPermissionFlag;
        readPermissions(Environment.buildPath(Environment.getVendorDirectory(), new String[]{"etc", "sysconfig"}), vendorPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getVendorDirectory(), new String[]{"etc", "permissions"}), vendorPermissionFlag);
        int odmPermissionFlag = vendorPermissionFlag;
        readPermissions(Environment.buildPath(Environment.getOdmDirectory(), new String[]{"etc", "sysconfig"}), odmPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getOdmDirectory(), new String[]{"etc", "permissions"}), odmPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getOemDirectory(), new String[]{"etc", "sysconfig"}), 33);
        readPermissions(Environment.buildPath(Environment.getOemDirectory(), new String[]{"etc", "permissions"}), 33);
        readPermissions(Environment.buildPath(Environment.getProductDirectory(), new String[]{"etc", "sysconfig"}), 31);
        readPermissions(Environment.buildPath(Environment.getProductDirectory(), new String[]{"etc", "permissions"}), 31);
        readCustPermissions();
        if (SystemProperties.getBoolean("persist.graphics.vulkan.disable", false)) {
            removeFeature("android.hardware.vulkan.level");
            removeFeature("android.hardware.vulkan.version");
        }
        int value = SystemProperties.getInt("ro.opengles.version", 0);
        if (value > 0 && value == 196608 && this.mAvailableFeatures.remove("android.hardware.opengles.aep") != null) {
            Slog.d(TAG, "Removed android.hardware.opengles.aep feature for opengles 3.0");
        }
        if (SystemProperties.getBoolean("ro.config.eea_enable", false)) {
            addFeature("com.google.android.feature.EEA_DEVICE", 0);
            addFeature("com.google.android.paid.search", 0);
            addFeature("com.google.android.paid.chrome", 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void readCustPermissions() {
        String[] dirs = new String[0];
        String sysPath = getCanonicalPathOrNull(Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc"}));
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
                    readPermissions(Environment.buildPath(file, new String[]{"sysconfig"}), -1);
                    readPermissions(Environment.buildPath(file, new String[]{"permissions"}), -1);
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
            for (File f : libraryDir.listFiles()) {
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
            if (platformFile != null) {
                readPermissionsFromXml(platformFile, permissionFlag);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:305:0x07e0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:306:0x07e1, code lost:
        r3 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:307:0x07e3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:308:0x07e4, code lost:
        r22 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:315:0x07f7, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x07f8, code lost:
        r3 = r4;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0028 A[Catch:{ XmlPullParserException -> 0x07dd, IOException -> 0x07e3, all -> 0x07e0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:276:0x0729 A[Catch:{ XmlPullParserException -> 0x07d9, IOException -> 0x07d7, all -> 0x07d2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:277:0x0731 A[Catch:{ XmlPullParserException -> 0x07d9, IOException -> 0x07d7, all -> 0x07d2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:296:0x07c6 A[Catch:{ XmlPullParserException -> 0x07d9, IOException -> 0x07d7, all -> 0x07d2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:305:0x07e0 A[ExcHandler: all (th java.lang.Throwable), PHI: r4 
      PHI: (r4v11 'permReader' java.io.FileReader) = (r4v1 'permReader' java.io.FileReader), (r4v1 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader) binds: [B:4:0x0013, B:6:0x0017, B:59:0x00b1, B:63:0x00c2, B:77:0x011f, B:90:0x0168, B:100:0x01a3, B:108:0x01d7] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0013] */
    /* JADX WARNING: Removed duplicated region for block: B:307:0x07e3 A[ExcHandler: IOException (e java.io.IOException), PHI: r4 
      PHI: (r4v10 'permReader' java.io.FileReader) = (r4v1 'permReader' java.io.FileReader), (r4v1 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader), (r4v13 'permReader' java.io.FileReader) binds: [B:4:0x0013, B:6:0x0017, B:59:0x00b1, B:63:0x00c2, B:77:0x011f, B:90:0x0168, B:100:0x01a3, B:108:0x01d7] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0013] */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x080a  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0816  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x081d  */
    /* JADX WARNING: Removed duplicated region for block: B:330:0x0828  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x082e  */
    /* JADX WARNING: Removed duplicated region for block: B:335:0x083f A[LOOP:2: B:333:0x0839->B:335:0x083f, LOOP_END] */
    private void readPermissionsFromXml(File permFile, int permissionFlag) {
        FileReader permReader;
        int i;
        Iterator<String> it;
        FileReader permReader2;
        int type;
        boolean allowPermissions;
        int type2;
        String str;
        boolean vendor;
        boolean allowed;
        File file = permFile;
        int i2 = permissionFlag;
        String str2 = null;
        try {
            FileReader permReader3 = new FileReader(file);
            boolean lowRam = ActivityManager.isLowRamDeviceStatic();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader3);
                while (true) {
                    int next = parser.next();
                    type = next;
                    int i3 = 1;
                    if (next == 2 || type == 1) {
                        if (type != 2) {
                            if (!parser.getName().equals("permissions")) {
                                try {
                                    if (!parser.getName().equals("config")) {
                                        throw new XmlPullParserException("Unexpected start tag in " + file + ": found " + parser.getName() + ", expected 'permissions' or 'config'");
                                    }
                                } catch (XmlPullParserException e) {
                                    e = e;
                                    permReader = permReader3;
                                } catch (IOException e2) {
                                    e = e2;
                                    permReader2 = permReader3;
                                    try {
                                        Slog.w(TAG, "Got exception parsing permissions.", e);
                                        IoUtils.closeQuietly(permReader2);
                                        if (StorageManager.isFileEncryptedNativeOnly()) {
                                        }
                                        if (StorageManager.hasAdoptable()) {
                                        }
                                        if (ActivityManager.isLowRamDeviceStatic()) {
                                        }
                                        it = this.mUnavailableFeatures.iterator();
                                        while (it.hasNext()) {
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        permReader = permReader2;
                                        IoUtils.closeQuietly(permReader);
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    permReader = permReader3;
                                    IoUtils.closeQuietly(permReader);
                                    throw th;
                                }
                            }
                            boolean allowAll = i2 == -1;
                            boolean allowLibs = (i2 & 2) != 0;
                            boolean allowFeatures = (i2 & 1) != 0;
                            boolean allowPermissions2 = (i2 & 4) != 0;
                            boolean allowAppConfigs = (i2 & 8) != 0;
                            boolean allowPrivappPermissions = (i2 & 16) != 0;
                            boolean allowOemPermissions = (i2 & 32) != 0;
                            boolean allowApiWhitelisting = (i2 & 64) != 0;
                            while (true) {
                                XmlUtils.nextElement(parser);
                                if (parser.getEventType() == i3) {
                                    break;
                                }
                                String name = parser.getName();
                                if (!"group".equals(name) || !allowAll) {
                                    type2 = type;
                                    if ("permission".equals(name) && allowPermissions2) {
                                        String perm = parser.getAttributeValue(null, "name");
                                        if (perm == null) {
                                            Slog.w(TAG, "<permission> without name in " + file + " at " + parser.getPositionDescription());
                                            XmlUtils.skipCurrentTag(parser);
                                        } else {
                                            readPermission(parser, perm.intern());
                                            permReader2 = permReader3;
                                            allowPermissions = allowPermissions2;
                                        }
                                    } else if (!"assign-permission".equals(name) || !allowPermissions2) {
                                        permReader2 = permReader3;
                                        allowPermissions = allowPermissions2;
                                        if ("library".equals(name) && allowLibs) {
                                            String lname = parser.getAttributeValue(null, "name");
                                            String lfile = parser.getAttributeValue(null, "file");
                                            if (lname == null) {
                                                Slog.w(TAG, "<library> without name in " + file + " at " + parser.getPositionDescription());
                                            } else if (lfile == null) {
                                                Slog.w(TAG, "<library> without file in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mSharedLibraries.put(lname, lfile);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i4 = permissionFlag;
                                            i3 = 1;
                                        } else if ("feature".equals(name) && allowFeatures) {
                                            String fname = parser.getAttributeValue(null, "name");
                                            int fversion = XmlUtils.readIntAttribute(parser, "version", 0);
                                            if (!lowRam) {
                                                allowed = true;
                                            } else {
                                                allowed = !"true".equals(parser.getAttributeValue(null, "notLowRam"));
                                            }
                                            if (fname == null) {
                                                Slog.w(TAG, "<feature> without name in " + file + " at " + parser.getPositionDescription());
                                            } else if (allowed) {
                                                if (!IS_CHINA || !"android.software.home_screen".equals(fname) || (!PhoneConstants.APN_TYPE_DEFAULT.equals(CHARACTERISTICS) && !"tablet".equals(CHARACTERISTICS))) {
                                                    addFeature(fname, fversion);
                                                } else {
                                                    Slog.w(TAG, "<feature> android.software.home_screen is disabled in china area");
                                                }
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i42 = permissionFlag;
                                            i3 = 1;
                                        } else if ("unavailable-feature".equals(name) && allowFeatures) {
                                            String fname2 = parser.getAttributeValue(null, "name");
                                            if (fname2 == null) {
                                                Slog.w(TAG, "<unavailable-feature> without name in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mUnavailableFeatures.add(fname2);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i422 = permissionFlag;
                                            i3 = 1;
                                        } else if ("allow-in-power-save-except-idle".equals(name) && allowAll) {
                                            String pkgname = parser.getAttributeValue(null, "package");
                                            if (pkgname == null) {
                                                Slog.w(TAG, "<allow-in-power-save-except-idle> without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mAllowInPowerSaveExceptIdle.add(pkgname);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i4222 = permissionFlag;
                                            i3 = 1;
                                        } else if ("allow-in-power-save".equals(name) && allowAll) {
                                            String pkgname2 = parser.getAttributeValue(null, "package");
                                            if (pkgname2 == null) {
                                                Slog.w(TAG, "<allow-in-power-save> without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mAllowInPowerSave.add(pkgname2);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i42222 = permissionFlag;
                                            i3 = 1;
                                        } else if ("allow-in-data-usage-save".equals(name) && allowAll) {
                                            String pkgname3 = parser.getAttributeValue(null, "package");
                                            if (pkgname3 == null) {
                                                Slog.w(TAG, "<allow-in-data-usage-save> without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mAllowInDataUsageSave.add(pkgname3);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i422222 = permissionFlag;
                                            i3 = 1;
                                        } else if ("allow-unthrottled-location".equals(name) && allowAll) {
                                            String pkgname4 = parser.getAttributeValue(null, "package");
                                            if (pkgname4 == null) {
                                                Slog.w(TAG, "<allow-unthrottled-location> without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mAllowUnthrottledLocation.add(pkgname4);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i4222222 = permissionFlag;
                                            i3 = 1;
                                        } else if ("allow-implicit-broadcast".equals(name) && allowAll) {
                                            String action = parser.getAttributeValue(null, "action");
                                            if (action == null) {
                                                Slog.w(TAG, "<allow-implicit-broadcast> without action in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mAllowImplicitBroadcasts.add(action);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i42222222 = permissionFlag;
                                            i3 = 1;
                                        } else if ("app-link".equals(name) && allowAppConfigs) {
                                            String pkgname5 = parser.getAttributeValue(null, "package");
                                            if (pkgname5 == null) {
                                                Slog.w(TAG, "<app-link> without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mLinkedApps.add(pkgname5);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else if ("system-user-whitelisted-app".equals(name) && allowAppConfigs) {
                                            String pkgname6 = parser.getAttributeValue(null, "package");
                                            if (pkgname6 == null) {
                                                Slog.w(TAG, "<system-user-whitelisted-app> without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mSystemUserWhitelistedApps.add(pkgname6);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else if ("system-user-blacklisted-app".equals(name) && allowAppConfigs) {
                                            String pkgname7 = parser.getAttributeValue(null, "package");
                                            if (pkgname7 == null) {
                                                Slog.w(TAG, "<system-user-blacklisted-app without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mSystemUserBlacklistedApps.add(pkgname7);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else if ("default-enabled-vr-app".equals(name) && allowAppConfigs) {
                                            String pkgname8 = parser.getAttributeValue(null, "package");
                                            String clsname = parser.getAttributeValue(null, "class");
                                            if (pkgname8 == null) {
                                                Slog.w(TAG, "<default-enabled-vr-app without package in " + file + " at " + parser.getPositionDescription());
                                            } else if (clsname == null) {
                                                Slog.w(TAG, "<default-enabled-vr-app without class in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mDefaultVrComponents.add(new ComponentName(pkgname8, clsname));
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else if ("backup-transport-whitelisted-service".equals(name) && allowFeatures) {
                                            String serviceName = parser.getAttributeValue(null, "service");
                                            if (serviceName == null) {
                                                Slog.w(TAG, "<backup-transport-whitelisted-service> without service in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                ComponentName cn = ComponentName.unflattenFromString(serviceName);
                                                if (cn == null) {
                                                    Slog.w(TAG, "<backup-transport-whitelisted-service> with invalid service name " + serviceName + " in " + file + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mBackupTransportWhitelist.add(cn);
                                                }
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else if ("disabled-until-used-preinstalled-carrier-associated-app".equals(name) && allowAppConfigs) {
                                            String pkgname9 = parser.getAttributeValue(null, "package");
                                            String carrierPkgname = parser.getAttributeValue(null, "carrierAppPackage");
                                            if (pkgname9 != null) {
                                                if (carrierPkgname != null) {
                                                    List<String> associatedPkgs = this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps.get(carrierPkgname);
                                                    if (associatedPkgs == null) {
                                                        associatedPkgs = new ArrayList<>();
                                                        this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps.put(carrierPkgname, associatedPkgs);
                                                    }
                                                    associatedPkgs.add(pkgname9);
                                                    XmlUtils.skipCurrentTag(parser);
                                                }
                                            }
                                            Slog.w(TAG, "<disabled-until-used-preinstalled-carrier-associated-app without package or carrierAppPackage in " + file + " at " + parser.getPositionDescription());
                                            XmlUtils.skipCurrentTag(parser);
                                        } else if ("disabled-until-used-preinstalled-carrier-app".equals(name) && allowAppConfigs) {
                                            String pkgname10 = parser.getAttributeValue(null, "package");
                                            if (pkgname10 == null) {
                                                Slog.w(TAG, "<disabled-until-used-preinstalled-carrier-app> without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mDisabledUntilUsedPreinstalledCarrierApps.add(pkgname10);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else if ("privapp-permissions".equals(name) && allowPrivappPermissions) {
                                            if (!permFile.toPath().startsWith(Environment.getVendorDirectory().toPath())) {
                                                if (!permFile.toPath().startsWith(Environment.getOdmDirectory().toPath())) {
                                                    vendor = false;
                                                    boolean product = permFile.toPath().startsWith(Environment.getProductDirectory().toPath());
                                                    if (!vendor) {
                                                        readPrivAppPermissions(parser, this.mVendorPrivAppPermissions, this.mVendorPrivAppDenyPermissions);
                                                    } else if (product) {
                                                        readPrivAppPermissions(parser, this.mProductPrivAppPermissions, this.mProductPrivAppDenyPermissions);
                                                    } else {
                                                        readPrivAppPermissions(parser, this.mPrivAppPermissions, this.mPrivAppDenyPermissions);
                                                    }
                                                }
                                            }
                                            vendor = true;
                                            boolean product2 = permFile.toPath().startsWith(Environment.getProductDirectory().toPath());
                                            if (!vendor) {
                                            }
                                        } else if ("oem-permissions".equals(name) && allowOemPermissions) {
                                            readOemPermissions(parser);
                                        } else if (!"hidden-api-whitelisted-app".equals(name) || !allowApiWhitelisting) {
                                            str = null;
                                            Slog.w(TAG, "Tag " + name + " is unknown or not allowed in " + permFile.getParent());
                                            XmlUtils.skipCurrentTag(parser);
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i422222222 = permissionFlag;
                                            i3 = 1;
                                        } else {
                                            str = null;
                                            String pkgname11 = parser.getAttributeValue(null, "package");
                                            if (pkgname11 == null) {
                                                Slog.w(TAG, "<hidden-api-whitelisted-app> without package in " + file + " at " + parser.getPositionDescription());
                                            } else {
                                                this.mHiddenApiPackageWhitelist.add(pkgname11);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i4222222222 = permissionFlag;
                                            i3 = 1;
                                        }
                                    } else {
                                        String perm2 = parser.getAttributeValue(null, "name");
                                        if (perm2 == null) {
                                            Slog.w(TAG, "<assign-permission> without name in " + file + " at " + parser.getPositionDescription());
                                            XmlUtils.skipCurrentTag(parser);
                                        } else {
                                            String uidStr = parser.getAttributeValue(null, "uid");
                                            if (uidStr == null) {
                                                StringBuilder sb = new StringBuilder();
                                                allowPermissions = allowPermissions2;
                                                sb.append("<assign-permission> without uid in ");
                                                sb.append(file);
                                                sb.append(" at ");
                                                sb.append(parser.getPositionDescription());
                                                Slog.w(TAG, sb.toString());
                                                XmlUtils.skipCurrentTag(parser);
                                                permReader2 = permReader3;
                                            } else {
                                                allowPermissions = allowPermissions2;
                                                int uid = Process.getUidForName(uidStr);
                                                if (uid < 0) {
                                                    StringBuilder sb2 = new StringBuilder();
                                                    permReader2 = permReader3;
                                                    try {
                                                        sb2.append("<assign-permission> with unknown uid \"");
                                                        sb2.append(uidStr);
                                                        sb2.append("  in ");
                                                        sb2.append(file);
                                                        sb2.append(" at ");
                                                        sb2.append(parser.getPositionDescription());
                                                        Slog.w(TAG, sb2.toString());
                                                        XmlUtils.skipCurrentTag(parser);
                                                    } catch (XmlPullParserException e3) {
                                                        e = e3;
                                                        permReader = permReader2;
                                                        try {
                                                            Slog.w(TAG, "Got exception parsing permissions.", e);
                                                            IoUtils.closeQuietly(permReader);
                                                            if (StorageManager.isFileEncryptedNativeOnly()) {
                                                            }
                                                            if (StorageManager.hasAdoptable()) {
                                                            }
                                                            if (ActivityManager.isLowRamDeviceStatic()) {
                                                            }
                                                            it = this.mUnavailableFeatures.iterator();
                                                            while (it.hasNext()) {
                                                            }
                                                        } catch (Throwable th3) {
                                                            th = th3;
                                                            IoUtils.closeQuietly(permReader);
                                                            throw th;
                                                        }
                                                    } catch (IOException e4) {
                                                        e = e4;
                                                        Slog.w(TAG, "Got exception parsing permissions.", e);
                                                        IoUtils.closeQuietly(permReader2);
                                                        if (StorageManager.isFileEncryptedNativeOnly()) {
                                                        }
                                                        if (StorageManager.hasAdoptable()) {
                                                        }
                                                        if (ActivityManager.isLowRamDeviceStatic()) {
                                                        }
                                                        it = this.mUnavailableFeatures.iterator();
                                                        while (it.hasNext()) {
                                                        }
                                                    } catch (Throwable th4) {
                                                        th = th4;
                                                        permReader = permReader2;
                                                        IoUtils.closeQuietly(permReader);
                                                        throw th;
                                                    }
                                                } else {
                                                    permReader2 = permReader3;
                                                    String perm3 = perm2.intern();
                                                    ArraySet<String> perms = this.mSystemPermissions.get(uid);
                                                    if (perms == null) {
                                                        perms = new ArraySet<>();
                                                        this.mSystemPermissions.put(uid, perms);
                                                    }
                                                    perms.add(perm3);
                                                    XmlUtils.skipCurrentTag(parser);
                                                }
                                            }
                                            str = null;
                                            str2 = str;
                                            type = type2;
                                            allowPermissions2 = allowPermissions;
                                            permReader3 = permReader2;
                                            int i42222222222 = permissionFlag;
                                            i3 = 1;
                                        }
                                    }
                                    str = null;
                                    str2 = str;
                                    type = type2;
                                    allowPermissions2 = allowPermissions;
                                    permReader3 = permReader2;
                                    int i422222222222 = permissionFlag;
                                    i3 = 1;
                                } else {
                                    String gidStr = parser.getAttributeValue(str2, "gid");
                                    if (gidStr != null) {
                                        this.mGlobalGids = ArrayUtils.appendInt(this.mGlobalGids, Process.getGidForName(gidStr));
                                        type2 = type;
                                    } else {
                                        StringBuilder sb3 = new StringBuilder();
                                        type2 = type;
                                        sb3.append("<group> without gid in ");
                                        sb3.append(file);
                                        sb3.append(" at ");
                                        sb3.append(parser.getPositionDescription());
                                        Slog.w(TAG, sb3.toString());
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                }
                                permReader2 = permReader3;
                                allowPermissions = allowPermissions2;
                                str = null;
                                str2 = str;
                                type = type2;
                                allowPermissions2 = allowPermissions;
                                permReader3 = permReader2;
                                int i4222222222222 = permissionFlag;
                                i3 = 1;
                            }
                            IoUtils.closeQuietly(permReader3);
                            FileReader fileReader = permReader3;
                            if (StorageManager.isFileEncryptedNativeOnly()) {
                                i = 0;
                                addFeature("android.software.file_based_encryption", 0);
                                addFeature("android.software.securely_removes_users", 0);
                            } else {
                                i = 0;
                            }
                            if (StorageManager.hasAdoptable()) {
                                addFeature("android.software.adoptable_storage", i);
                            }
                            if (ActivityManager.isLowRamDeviceStatic()) {
                                addFeature("android.hardware.ram.low", i);
                            } else {
                                addFeature("android.hardware.ram.normal", i);
                            }
                            it = this.mUnavailableFeatures.iterator();
                            while (it.hasNext()) {
                                removeFeature(it.next());
                            }
                        }
                        int i5 = type;
                        throw new XmlPullParserException("No start tag found");
                    }
                }
                if (type != 2) {
                }
            } catch (XmlPullParserException e5) {
                e = e5;
                permReader = permReader3;
                Slog.w(TAG, "Got exception parsing permissions.", e);
                IoUtils.closeQuietly(permReader);
                if (StorageManager.isFileEncryptedNativeOnly()) {
                }
                if (StorageManager.hasAdoptable()) {
                }
                if (ActivityManager.isLowRamDeviceStatic()) {
                }
                it = this.mUnavailableFeatures.iterator();
                while (it.hasNext()) {
                }
            } catch (IOException e6) {
            } catch (Throwable th5) {
            }
        } catch (FileNotFoundException e7) {
            Slog.w(TAG, "Couldn't find or open permissions file " + file);
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

    /* access modifiers changed from: package-private */
    public void readPermission(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        if (!this.mPermissions.containsKey(name)) {
            PermissionEntry perm = new PermissionEntry(name, XmlUtils.readBooleanAttribute(parser, "perUser", false));
            this.mPermissions.put(name, perm);
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
                    if ("group".equals(parser.getName())) {
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
}
