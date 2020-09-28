package android.content.res;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.ActivityInfo;
import android.content.res.XmlBlock;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import libcore.io.IoUtils;

public final class AssetManager implements AutoCloseable {
    public static final int ACCESS_BUFFER = 3;
    public static final int ACCESS_RANDOM = 1;
    public static final int ACCESS_STREAMING = 2;
    public static final int ACCESS_UNKNOWN = 0;
    private static final int DBID_FLAG = 9999;
    private static final boolean DEBUG_REFS = false;
    private static final boolean FEATURE_FLAG_IDMAP2 = true;
    private static final String FRAMEWORK_APK_PATH = "/system/framework/framework-res.apk";
    private static final String FRAMEWORK_HWEXT_APK_PATH = "/system/framework/framework-res-hwext.apk";
    private static final String TAG = "AssetManager";
    private static final ApkAssets[] sEmptyApkAssets = new ApkAssets[0];
    private static final Object sSync = new Object();
    @UnsupportedAppUsage
    @GuardedBy({"sSync"})
    static AssetManager sSystem = null;
    @GuardedBy({"sSync"})
    private static ApkAssets[] sSystemApkAssets = new ApkAssets[0];
    @GuardedBy({"sSync"})
    private static ArraySet<ApkAssets> sSystemApkAssetsSet;
    @GuardedBy({"this"})
    private ApkAssets[] mApkAssets;
    @GuardedBy({"this"})
    private boolean mDbidInit;
    private final TypedValue mDbidValue;
    private int mDeepType;
    public String mMainApkPackageName;
    @GuardedBy({"this"})
    private int mNumRefs;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    private long mObject;
    @GuardedBy({"this"})
    private final long[] mOffsets;
    @GuardedBy({"this"})
    private boolean mOpen;
    @GuardedBy({"this"})
    private HashMap<Long, RuntimeException> mRefStacks;
    @GuardedBy({"this"})
    private final TypedValue mValue;

    public static native String getAssetAllocations();

    @UnsupportedAppUsage
    public static native int getGlobalAssetCount();

    @UnsupportedAppUsage
    public static native int getGlobalAssetManagerCount();

    private static native void nativeApplyStyle(long j, long j2, int i, int i2, long j3, int[] iArr, long j4, long j5);

    /* access modifiers changed from: private */
    public static native void nativeAssetDestroy(long j);

    /* access modifiers changed from: private */
    public static native long nativeAssetGetLength(long j);

    /* access modifiers changed from: private */
    public static native long nativeAssetGetRemainingLength(long j);

    /* access modifiers changed from: private */
    public static native int nativeAssetRead(long j, byte[] bArr, int i, int i2);

    /* access modifiers changed from: private */
    public static native int nativeAssetReadChar(long j);

    /* access modifiers changed from: private */
    public static native long nativeAssetSeek(long j, long j2, int i);

    private static native int[] nativeAttributeResolutionStack(long j, long j2, int i, int i2, int i3);

    private static native long nativeCreate();

    private static native String[] nativeCreateIdmapsForStaticOverlaysTargetingAndroid();

    private static native void nativeDestroy(long j);

    private static native SparseArray<String> nativeGetAssignedPackageIdentifiers(long j);

    private static native String nativeGetLastResourceResolution(long j);

    private static native String[] nativeGetLocales(long j, boolean z);

    private static native Map nativeGetOverlayableMap(long j, String str);

    private static native int nativeGetResourceArray(long j, int i, int[] iArr);

    private static native int nativeGetResourceArraySize(long j, int i);

    private static native int nativeGetResourceBagValue(long j, int i, int i2, TypedValue typedValue);

    private static native String nativeGetResourceEntryName(long j, int i);

    private static native int nativeGetResourceIdentifier(long j, String str, String str2, String str3);

    private static native int[] nativeGetResourceIntArray(long j, int i);

    private static native String nativeGetResourceName(long j, int i);

    private static native String nativeGetResourcePackageName(long j, int i);

    private static native String[] nativeGetResourceStringArray(long j, int i);

    private static native int[] nativeGetResourceStringArrayInfo(long j, int i);

    private static native String nativeGetResourceTypeName(long j, int i);

    private static native int nativeGetResourceValue(long j, int i, short s, TypedValue typedValue, boolean z);

    private static native Configuration[] nativeGetSizeConfigurations(long j);

    private static native int[] nativeGetStyleAttributes(long j, int i);

    private static native String[] nativeList(long j, String str) throws IOException;

    private static native long nativeOpenAsset(long j, String str, int i);

    private static native ParcelFileDescriptor nativeOpenAssetFd(long j, String str, long[] jArr) throws IOException;

    private static native long nativeOpenNonAsset(long j, int i, String str, int i2);

    private static native ParcelFileDescriptor nativeOpenNonAssetFd(long j, int i, String str, long[] jArr) throws IOException;

    private static native long nativeOpenXmlAsset(long j, int i, String str);

    private static native boolean nativeResolveAttrs(long j, long j2, int i, int i2, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4);

    private static native boolean nativeRetrieveAttributes(long j, long j2, int[] iArr, int[] iArr2, int[] iArr3);

    /* access modifiers changed from: private */
    public static native void nativeSetApkAssets(long j, ApkAssets[] apkAssetsArr, boolean z);

    private static native void nativeSetConfiguration(long j, int i, int i2, String str, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16, int i17);

    private static native void nativeSetDbidFlag(long j, boolean z);

    private static native void nativeSetResourceResolutionLoggingEnabled(long j, boolean z);

    private static native void nativeThemeApplyStyle(long j, long j2, int i, boolean z);

    static native void nativeThemeClear(long j);

    private static native void nativeThemeCopy(long j, long j2, long j3, long j4);

    private static native long nativeThemeCreate(long j);

    private static native void nativeThemeDestroy(long j);

    private static native void nativeThemeDump(long j, long j2, int i, String str, String str2);

    private static native int nativeThemeGetAttributeValue(long j, long j2, int i, TypedValue typedValue, boolean z);

    static native int nativeThemeGetChangingConfigurations(long j);

    private static native void nativeVerifySystemIdmaps();

    public static class Builder {
        private String mApkPackageName;
        private AssetManager mAssetManager;
        private ArrayList<ApkAssets> mUserApkAssets;

        public Builder() {
            this.mUserApkAssets = new ArrayList<>();
            this.mApkPackageName = null;
            this.mAssetManager = null;
            this.mAssetManager = new AssetManager(false);
        }

        public AssetManager getAssets() {
            return this.mAssetManager;
        }

        public Builder addApkAssets(ApkAssets apkAssets) {
            this.mUserApkAssets.add(apkAssets);
            return this;
        }

        public void setMainApkPackageName(String packageName) {
            this.mApkPackageName = packageName;
        }

        /* JADX INFO: Multiple debug info for r5v2 android.content.res.AssetManager: [D('i' int), D('assetManager' android.content.res.AssetManager)] */
        public AssetManager build() {
            ApkAssets[] systemApkAssets = AssetManager.getSystem().getApkAssets();
            ApkAssets[] apkAssets = new ApkAssets[(systemApkAssets.length + this.mUserApkAssets.size())];
            System.arraycopy(systemApkAssets, 0, apkAssets, 0, systemApkAssets.length);
            int userApkAssetCount = this.mUserApkAssets.size();
            for (int i = 0; i < userApkAssetCount; i++) {
                apkAssets[systemApkAssets.length + i] = this.mUserApkAssets.get(i);
            }
            AssetManager assetManager = this.mAssetManager;
            assetManager.mApkAssets = apkAssets;
            AssetManager.nativeSetApkAssets(assetManager.mObject, apkAssets, false);
            assetManager.mMainApkPackageName = this.mApkPackageName;
            return assetManager;
        }
    }

    @UnsupportedAppUsage
    public AssetManager() {
        ApkAssets[] assets;
        this.mDbidValue = new TypedValue();
        this.mDbidInit = false;
        this.mValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mMainApkPackageName = null;
        this.mOpen = true;
        this.mNumRefs = 1;
        this.mDeepType = 0;
        synchronized (sSync) {
            createSystemAssetsInZygoteLocked();
            assets = sSystemApkAssets;
        }
        this.mObject = nativeCreate();
        setApkAssets(assets, false);
    }

    protected AssetManager(boolean sentinel) {
        this.mDbidValue = new TypedValue();
        this.mDbidInit = false;
        this.mValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mMainApkPackageName = null;
        this.mOpen = true;
        this.mNumRefs = 1;
        this.mDeepType = 0;
        this.mObject = nativeCreate();
        if (Process.myUid() == 0) {
            HwAssetManagerEx.setSharePemmison();
        }
    }

    @GuardedBy({"sSync"})
    private static void createSystemAssetsInZygoteLocked() {
        if (sSystem == null) {
            try {
                ArrayList<ApkAssets> apkAssets = new ArrayList<>();
                apkAssets.add(ApkAssets.loadFromPath(FRAMEWORK_APK_PATH, true));
                apkAssets.add(ApkAssets.loadFromPath(FRAMEWORK_HWEXT_APK_PATH, true));
                String[] systemIdmapPaths = nativeCreateIdmapsForStaticOverlaysTargetingAndroid();
                if (systemIdmapPaths != null) {
                    for (String idmapPath : systemIdmapPaths) {
                        apkAssets.add(ApkAssets.loadOverlayFromPath(idmapPath, true));
                    }
                } else {
                    Log.w(TAG, "'idmap2 --scan' failed: no static=\"true\" overlays targeting \"android\" will be loaded");
                }
                sSystemApkAssetsSet = new ArraySet<>(apkAssets);
                sSystemApkAssets = (ApkAssets[]) apkAssets.toArray(new ApkAssets[apkAssets.size()]);
                sSystem = new AssetManager(true);
                sSystem.setApkAssets(sSystemApkAssets, false);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create system AssetManager", e);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0053, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0054, code lost:
        if (r2 != null) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0059, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005c, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005d, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0060, code lost:
        throw r3;
     */
    private static void loadStaticRuntimeOverlays(ArrayList<ApkAssets> outApkAssets) throws IOException {
        try {
            FileInputStream fis = new FileInputStream("/data/resource-cache/overlays.list");
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                FileLock flock = fis.getChannel().lock(0, Long.MAX_VALUE, true);
                while (true) {
                    String line = br.readLine();
                    if (line != null) {
                        String[] lineArray = line.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                        if (lineArray != null) {
                            if (lineArray.length >= 2) {
                                outApkAssets.add(ApkAssets.loadOverlayFromPath(lineArray[1], true));
                            }
                        }
                    } else {
                        if (flock != null) {
                            $closeResource(null, flock);
                        }
                        $closeResource(null, br);
                        return;
                    }
                }
            } finally {
                IoUtils.closeQuietly(fis);
            }
        } catch (FileNotFoundException e) {
            Log.i(TAG, "no overlays.list file found");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    @UnsupportedAppUsage
    public static AssetManager getSystem() {
        AssetManager assetManager;
        synchronized (sSync) {
            createSystemAssetsInZygoteLocked();
            assetManager = sSystem;
        }
        return assetManager;
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        synchronized (this) {
            if (this.mOpen) {
                this.mOpen = false;
                decRefsLocked((long) hashCode());
            }
        }
    }

    public void setApkAssets(ApkAssets[] apkAssets, boolean invalidateCaches) {
        Preconditions.checkNotNull(apkAssets, "apkAssets");
        ApkAssets[] apkAssetsArr = sSystemApkAssets;
        ApkAssets[] newApkAssets = new ApkAssets[(apkAssetsArr.length + apkAssets.length)];
        System.arraycopy(apkAssetsArr, 0, newApkAssets, 0, apkAssetsArr.length);
        int newLength = sSystemApkAssets.length;
        for (ApkAssets apkAsset : apkAssets) {
            if (!sSystemApkAssetsSet.contains(apkAsset)) {
                newApkAssets[newLength] = apkAsset;
                newLength++;
            }
        }
        if (newLength != newApkAssets.length) {
            newApkAssets = (ApkAssets[]) Arrays.copyOf(newApkAssets, newLength);
        }
        synchronized (this) {
            ensureOpenLocked();
            this.mApkAssets = newApkAssets;
            nativeSetApkAssets(this.mObject, this.mApkAssets, invalidateCaches);
            if (invalidateCaches) {
                invalidateCachesLocked(-1);
            }
        }
    }

    private void invalidateCachesLocked(int diff) {
    }

    @UnsupportedAppUsage
    public ApkAssets[] getApkAssets() {
        synchronized (this) {
            if (!this.mOpen) {
                return sEmptyApkAssets;
            }
            return this.mApkAssets;
        }
    }

    public String[] getApkPaths() {
        synchronized (this) {
            if (!this.mOpen) {
                return new String[0];
            }
            String[] paths = new String[this.mApkAssets.length];
            int count = this.mApkAssets.length;
            for (int i = 0; i < count; i++) {
                paths[i] = this.mApkAssets[i].getAssetPath();
            }
            return paths;
        }
    }

    public int findCookieForPath(String path) {
        Preconditions.checkNotNull(path, "path");
        synchronized (this) {
            ensureValidLocked();
            int count = this.mApkAssets.length;
            for (int i = 0; i < count; i++) {
                if (path.equals(this.mApkAssets[i].getAssetPath())) {
                    return i + 1;
                }
            }
            return 0;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public int addAssetPath(String path) {
        return addAssetPathInternal(path, false, false);
    }

    @UnsupportedAppUsage
    @Deprecated
    public int addAssetPathAsSharedLibrary(String path) {
        return addAssetPathInternal(path, false, true);
    }

    @UnsupportedAppUsage
    @Deprecated
    public int addOverlayPath(String path) {
        return addAssetPathInternal(path, true, false);
    }

    private int addAssetPathInternal(String path, boolean overlay, boolean appAsLib) {
        ApkAssets assets;
        Preconditions.checkNotNull(path, "path");
        synchronized (this) {
            ensureOpenLocked();
            int count = this.mApkAssets.length;
            for (int i = 0; i < count; i++) {
                if (this.mApkAssets[i].getAssetPath().equals(path)) {
                    return i + 1;
                }
            }
            if (overlay) {
                try {
                    assets = ApkAssets.loadOverlayFromPath("/data/resource-cache/" + path.substring(1).replace('/', '@') + "@idmap", false);
                } catch (IOException e) {
                    return 0;
                }
            } else {
                assets = ApkAssets.loadFromPath(path, false, appAsLib);
            }
            this.mApkAssets = (ApkAssets[]) Arrays.copyOf(this.mApkAssets, count + 1);
            this.mApkAssets[count] = assets;
            nativeSetApkAssets(this.mObject, this.mApkAssets, true);
            invalidateCachesLocked(-1);
            return count + 1;
        }
    }

    @GuardedBy({"this"})
    private void ensureValidLocked() {
        if (this.mObject == 0) {
            throw new RuntimeException("AssetManager has been destroyed");
        }
    }

    @GuardedBy({"this"})
    private void ensureOpenLocked() {
        if (!this.mOpen) {
            throw new RuntimeException("AssetManager has been closed");
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean getResourceValue(int resId, int densityDpi, TypedValue outValue, boolean resolveRefs) {
        Preconditions.checkNotNull(outValue, "outValue");
        synchronized (this) {
            ensureValidLocked();
            int cookie = nativeGetResourceValue(this.mObject, resId, (short) densityDpi, outValue, resolveRefs);
            if (cookie <= 0) {
                return false;
            }
            outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(outValue.changingConfigurations);
            if (outValue.type == 3) {
                outValue.string = this.mApkAssets[cookie - 1].getStringFromPool(outValue.data);
                if (HwAssetManagerEx.hasRes() && !this.mDbidInit) {
                    CharSequence rt = null;
                    nativeSetDbidFlag(this.mObject, true);
                    this.mDbidInit = true;
                    if (getResourceValue(resId, 9999, this.mDbidValue, false)) {
                        rt = HwAssetManagerEx.getTextForDBid(this.mDbidValue.string);
                    }
                    nativeSetDbidFlag(this.mObject, false);
                    this.mDbidInit = false;
                    if (rt != null) {
                        outValue.string = rt;
                    }
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public CharSequence getResourceText(int resId) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            if (!getResourceValue(resId, 0, outValue, true)) {
                return null;
            }
            return outValue.coerceToString();
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public CharSequence getResourceBagText(int resId, int bagEntryId) {
        synchronized (this) {
            ensureValidLocked();
            TypedValue outValue = this.mValue;
            int cookie = nativeGetResourceBagValue(this.mObject, resId, bagEntryId, outValue);
            if (cookie <= 0) {
                return null;
            }
            outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(outValue.changingConfigurations);
            if (outValue.type == 3) {
                CharSequence result = this.mApkAssets[cookie - 1].getStringFromPool(outValue.data);
                if (HwAssetManagerEx.hasRes() && !this.mDbidInit) {
                    nativeSetDbidFlag(this.mObject, true);
                    this.mDbidInit = true;
                    CharSequence rt = HwAssetManagerEx.getTextForDBid(getResourceBagText(resId, bagEntryId));
                    nativeSetDbidFlag(this.mObject, false);
                    this.mDbidInit = false;
                    if (rt != null) {
                        result = rt;
                    }
                }
                return result;
            }
            return outValue.coerceToString();
        }
    }

    /* access modifiers changed from: package-private */
    public int getResourceArraySize(int resId) {
        int nativeGetResourceArraySize;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceArraySize = nativeGetResourceArraySize(this.mObject, resId);
        }
        return nativeGetResourceArraySize;
    }

    /* access modifiers changed from: package-private */
    public int getResourceArray(int resId, int[] outData) {
        int nativeGetResourceArray;
        Preconditions.checkNotNull(outData, "outData");
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceArray = nativeGetResourceArray(this.mObject, resId, outData);
        }
        return nativeGetResourceArray;
    }

    /* access modifiers changed from: package-private */
    public String[] getResourceStringArray(int resId) {
        String[] retArray;
        synchronized (this) {
            ensureValidLocked();
            retArray = nativeGetResourceStringArray(this.mObject, resId);
            if (HwAssetManagerEx.hasRes()) {
                nativeSetDbidFlag(this.mObject, true);
                this.mDbidInit = true;
                CharSequence[] dbidArray = getResourceTextArray(resId);
                this.mDbidInit = false;
                nativeSetDbidFlag(this.mObject, false);
                if (dbidArray != null) {
                    for (int i = 0; i < dbidArray.length; i++) {
                        CharSequence rt = HwAssetManagerEx.getTextForDBid(dbidArray[i]);
                        if (rt != null) {
                            retArray[i] = rt.toString();
                        }
                    }
                }
            }
        }
        return retArray;
    }

    /* access modifiers changed from: package-private */
    public CharSequence[] getResourceTextArray(int resId) {
        synchronized (this) {
            ensureValidLocked();
            int[] rawInfoArray = nativeGetResourceStringArrayInfo(this.mObject, resId);
            if (rawInfoArray == null) {
                return null;
            }
            int rawInfoArrayLen = rawInfoArray.length;
            CharSequence[] retArray = new CharSequence[(rawInfoArrayLen / 2)];
            int i = 0;
            int j = 0;
            while (i < rawInfoArrayLen) {
                int cookie = rawInfoArray[i];
                int index = rawInfoArray[i + 1];
                retArray[j] = (index < 0 || cookie <= 0) ? null : this.mApkAssets[cookie - 1].getStringFromPool(index);
                i += 2;
                j++;
            }
            if (HwAssetManagerEx.hasRes() && !this.mDbidInit) {
                nativeSetDbidFlag(this.mObject, true);
                this.mDbidInit = true;
                CharSequence[] dbidArray = getResourceTextArray(resId);
                this.mDbidInit = false;
                nativeSetDbidFlag(this.mObject, false);
                for (int i2 = 0; i2 < dbidArray.length; i2++) {
                    CharSequence rt = HwAssetManagerEx.getTextForDBid(dbidArray[i2]);
                    if (rt != null) {
                        retArray[i2] = rt;
                    }
                }
            }
            return retArray;
        }
    }

    /* access modifiers changed from: package-private */
    public int[] getResourceIntArray(int resId) {
        int[] nativeGetResourceIntArray;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceIntArray = nativeGetResourceIntArray(this.mObject, resId);
        }
        return nativeGetResourceIntArray;
    }

    /* access modifiers changed from: package-private */
    public int[] getStyleAttributes(int resId) {
        int[] nativeGetStyleAttributes;
        synchronized (this) {
            ensureValidLocked();
            nativeGetStyleAttributes = nativeGetStyleAttributes(this.mObject, resId);
        }
        return nativeGetStyleAttributes;
    }

    /* access modifiers changed from: package-private */
    public boolean getThemeValue(long theme, int resId, TypedValue outValue, boolean resolveRefs) {
        Preconditions.checkNotNull(outValue, "outValue");
        synchronized (this) {
            ensureValidLocked();
            int cookie = nativeThemeGetAttributeValue(this.mObject, theme, resId, outValue, resolveRefs);
            if (cookie <= 0) {
                return false;
            }
            outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(outValue.changingConfigurations);
            if (outValue.type == 3) {
                outValue.string = this.mApkAssets[cookie - 1].getStringFromPool(outValue.data);
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpTheme(long theme, int priority, String tag, String prefix) {
        synchronized (this) {
            ensureValidLocked();
            nativeThemeDump(this.mObject, theme, priority, tag, prefix);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public String getResourceName(int resId) {
        String nativeGetResourceName;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceName = nativeGetResourceName(this.mObject, resId);
        }
        return nativeGetResourceName;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public String getResourcePackageName(int resId) {
        String nativeGetResourcePackageName;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourcePackageName = nativeGetResourcePackageName(this.mObject, resId);
        }
        return nativeGetResourcePackageName;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public String getResourceTypeName(int resId) {
        String nativeGetResourceTypeName;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceTypeName = nativeGetResourceTypeName(this.mObject, resId);
        }
        return nativeGetResourceTypeName;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public String getResourceEntryName(int resId) {
        String nativeGetResourceEntryName;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceEntryName = nativeGetResourceEntryName(this.mObject, resId);
        }
        return nativeGetResourceEntryName;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public int getResourceIdentifier(String name, String defType, String defPackage) {
        int nativeGetResourceIdentifier;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceIdentifier = nativeGetResourceIdentifier(this.mObject, name, defType, defPackage);
        }
        return nativeGetResourceIdentifier;
    }

    public void setResourceResolutionLoggingEnabled(boolean enabled) {
        synchronized (this) {
            ensureValidLocked();
            nativeSetResourceResolutionLoggingEnabled(this.mObject, enabled);
        }
    }

    public String getLastResourceResolution() {
        String nativeGetLastResourceResolution;
        synchronized (this) {
            ensureValidLocked();
            nativeGetLastResourceResolution = nativeGetLastResourceResolution(this.mObject);
        }
        return nativeGetLastResourceResolution;
    }

    /* access modifiers changed from: package-private */
    public CharSequence getPooledStringForCookie(int cookie, int id) {
        return getApkAssets()[cookie - 1].getStringFromPool(id);
    }

    public InputStream open(String fileName) throws IOException {
        return open(fileName, 2);
    }

    public InputStream open(String fileName, int accessMode) throws IOException {
        AssetInputStream assetInputStream;
        Preconditions.checkNotNull(fileName, "fileName");
        synchronized (this) {
            ensureOpenLocked();
            long asset = nativeOpenAsset(this.mObject, fileName, accessMode);
            if (asset != 0) {
                assetInputStream = new AssetInputStream(asset);
                incRefsLocked((long) assetInputStream.hashCode());
            } else {
                throw new FileNotFoundException("Asset file: " + fileName);
            }
        }
        return assetInputStream;
    }

    public AssetFileDescriptor openFd(String fileName) throws IOException {
        AssetFileDescriptor assetFileDescriptor;
        Preconditions.checkNotNull(fileName, "fileName");
        synchronized (this) {
            ensureOpenLocked();
            ParcelFileDescriptor pfd = nativeOpenAssetFd(this.mObject, fileName, this.mOffsets);
            if (pfd != null) {
                assetFileDescriptor = new AssetFileDescriptor(pfd, this.mOffsets[0], this.mOffsets[1]);
            } else {
                throw new FileNotFoundException("Asset file: " + fileName);
            }
        }
        return assetFileDescriptor;
    }

    public String[] list(String path) throws IOException {
        String[] nativeList;
        Preconditions.checkNotNull(path, "path");
        synchronized (this) {
            ensureValidLocked();
            nativeList = nativeList(this.mObject, path);
        }
        return nativeList;
    }

    @UnsupportedAppUsage
    public InputStream openNonAsset(String fileName) throws IOException {
        return openNonAsset(0, fileName, 2);
    }

    @UnsupportedAppUsage
    public InputStream openNonAsset(String fileName, int accessMode) throws IOException {
        return openNonAsset(0, fileName, accessMode);
    }

    @UnsupportedAppUsage
    public InputStream openNonAsset(int cookie, String fileName) throws IOException {
        return openNonAsset(cookie, fileName, 2);
    }

    @UnsupportedAppUsage
    public InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
        AssetInputStream assetInputStream;
        Preconditions.checkNotNull(fileName, "fileName");
        synchronized (this) {
            ensureOpenLocked();
            long asset = nativeOpenNonAsset(this.mObject, cookie, fileName, accessMode);
            if (asset != 0) {
                assetInputStream = new AssetInputStream(asset);
                incRefsLocked((long) assetInputStream.hashCode());
            } else {
                throw new FileNotFoundException("Asset absolute file: " + fileName);
            }
        }
        return assetInputStream;
    }

    public AssetFileDescriptor openNonAssetFd(String fileName) throws IOException {
        return openNonAssetFd(0, fileName);
    }

    public AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
        AssetFileDescriptor assetFileDescriptor;
        Preconditions.checkNotNull(fileName, "fileName");
        synchronized (this) {
            ensureOpenLocked();
            ParcelFileDescriptor pfd = nativeOpenNonAssetFd(this.mObject, cookie, fileName, this.mOffsets);
            if (pfd != null) {
                assetFileDescriptor = new AssetFileDescriptor(pfd, this.mOffsets[0], this.mOffsets[1]);
            } else {
                throw new FileNotFoundException("Asset absolute file: " + fileName);
            }
        }
        return assetFileDescriptor;
    }

    public XmlResourceParser openXmlResourceParser(String fileName) throws IOException {
        return openXmlResourceParser(0, fileName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        if (r0 != null) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        throw r2;
     */
    public XmlResourceParser openXmlResourceParser(int cookie, String fileName) throws IOException {
        XmlBlock block = openXmlBlockAsset(cookie, fileName);
        XmlResourceParser parser = block.newParser();
        if (parser != null) {
            $closeResource(null, block);
            return parser;
        }
        throw new AssertionError("block.newParser() returned a null parser");
    }

    /* access modifiers changed from: package-private */
    public XmlBlock openXmlBlockAsset(String fileName) throws IOException {
        return openXmlBlockAsset(0, fileName);
    }

    /* access modifiers changed from: package-private */
    public XmlBlock openXmlBlockAsset(int cookie, String fileName) throws IOException {
        XmlBlock block;
        Preconditions.checkNotNull(fileName, "fileName");
        synchronized (this) {
            ensureOpenLocked();
            long xmlBlock = nativeOpenXmlAsset(this.mObject, cookie, fileName);
            if (xmlBlock != 0) {
                block = new XmlBlock(this, xmlBlock);
                incRefsLocked((long) block.hashCode());
            } else {
                throw new FileNotFoundException("Asset XML file: " + fileName);
            }
        }
        return block;
    }

    /* access modifiers changed from: package-private */
    public void xmlBlockGone(int id) {
        synchronized (this) {
            decRefsLocked((long) id);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void applyStyle(long themePtr, int defStyleAttr, int defStyleRes, XmlBlock.Parser parser, int[] inAttrs, long outValuesAddress, long outIndicesAddress) {
        Preconditions.checkNotNull(inAttrs, "inAttrs");
        synchronized (this) {
            ensureValidLocked();
            nativeApplyStyle(this.mObject, themePtr, defStyleAttr, defStyleRes, parser != null ? parser.mParseState : 0, inAttrs, outValuesAddress, outIndicesAddress);
        }
    }

    /* access modifiers changed from: package-private */
    public int[] getAttributeResolutionStack(long themePtr, int defStyleAttr, int defStyleRes, int xmlStyle) {
        int[] nativeAttributeResolutionStack;
        synchronized (this) {
            nativeAttributeResolutionStack = nativeAttributeResolutionStack(this.mObject, themePtr, xmlStyle, defStyleAttr, defStyleRes);
        }
        return nativeAttributeResolutionStack;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean resolveAttrs(long themePtr, int defStyleAttr, int defStyleRes, int[] inValues, int[] inAttrs, int[] outValues, int[] outIndices) {
        boolean nativeResolveAttrs;
        Preconditions.checkNotNull(inAttrs, "inAttrs");
        Preconditions.checkNotNull(outValues, "outValues");
        Preconditions.checkNotNull(outIndices, "outIndices");
        synchronized (this) {
            ensureValidLocked();
            nativeResolveAttrs = nativeResolveAttrs(this.mObject, themePtr, defStyleAttr, defStyleRes, inValues, inAttrs, outValues, outIndices);
        }
        return nativeResolveAttrs;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean retrieveAttributes(XmlBlock.Parser parser, int[] inAttrs, int[] outValues, int[] outIndices) {
        boolean nativeRetrieveAttributes;
        Preconditions.checkNotNull(parser, "parser");
        Preconditions.checkNotNull(inAttrs, "inAttrs");
        Preconditions.checkNotNull(outValues, "outValues");
        Preconditions.checkNotNull(outIndices, "outIndices");
        synchronized (this) {
            ensureValidLocked();
            nativeRetrieveAttributes = nativeRetrieveAttributes(this.mObject, parser.mParseState, inAttrs, outValues, outIndices);
        }
        return nativeRetrieveAttributes;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public long createTheme() {
        long themePtr;
        synchronized (this) {
            ensureValidLocked();
            themePtr = nativeThemeCreate(this.mObject);
            incRefsLocked(themePtr);
        }
        return themePtr;
    }

    /* access modifiers changed from: package-private */
    public void releaseTheme(long themePtr) {
        synchronized (this) {
            nativeThemeDestroy(themePtr);
            decRefsLocked(themePtr);
        }
    }

    /* access modifiers changed from: package-private */
    public void applyStyleToTheme(long themePtr, int resId, boolean force) {
        synchronized (this) {
            ensureValidLocked();
            nativeThemeApplyStyle(this.mObject, themePtr, resId, force);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void setThemeTo(long dstThemePtr, AssetManager srcAssetManager, long srcThemePtr) {
        synchronized (this) {
            ensureValidLocked();
            synchronized (srcAssetManager) {
                srcAssetManager.ensureValidLocked();
                nativeThemeCopy(this.mObject, dstThemePtr, srcAssetManager.mObject, srcThemePtr);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        synchronized (this) {
            if (this.mObject != 0) {
                nativeDestroy(this.mObject);
                this.mObject = 0;
            }
        }
    }

    public final class AssetInputStream extends InputStream {
        private long mAssetNativePtr;
        private long mLength;
        private long mMarkPos;

        @UnsupportedAppUsage
        public final int getAssetInt() {
            throw new UnsupportedOperationException();
        }

        @UnsupportedAppUsage
        public final long getNativeAsset() {
            return this.mAssetNativePtr;
        }

        private AssetInputStream(long assetNativePtr) {
            this.mAssetNativePtr = assetNativePtr;
            this.mLength = AssetManager.nativeAssetGetLength(assetNativePtr);
        }

        @Override // java.io.InputStream
        public final int read() throws IOException {
            ensureOpen();
            return AssetManager.nativeAssetReadChar(this.mAssetNativePtr);
        }

        @Override // java.io.InputStream
        public final int read(byte[] b) throws IOException {
            ensureOpen();
            Preconditions.checkNotNull(b, "b");
            return AssetManager.nativeAssetRead(this.mAssetNativePtr, b, 0, b.length);
        }

        @Override // java.io.InputStream
        public final int read(byte[] b, int off, int len) throws IOException {
            ensureOpen();
            Preconditions.checkNotNull(b, "b");
            return AssetManager.nativeAssetRead(this.mAssetNativePtr, b, off, len);
        }

        @Override // java.io.InputStream
        public final long skip(long n) throws IOException {
            ensureOpen();
            long pos = AssetManager.nativeAssetSeek(this.mAssetNativePtr, 0, 0);
            long j = this.mLength;
            if (pos + n > j) {
                n = j - pos;
            }
            if (n > 0) {
                AssetManager.nativeAssetSeek(this.mAssetNativePtr, n, 0);
            }
            return n;
        }

        @Override // java.io.InputStream
        public final int available() throws IOException {
            ensureOpen();
            long len = AssetManager.nativeAssetGetRemainingLength(this.mAssetNativePtr);
            if (len > 2147483647L) {
                return Integer.MAX_VALUE;
            }
            return (int) len;
        }

        public final boolean markSupported() {
            return true;
        }

        public final void mark(int readlimit) {
            ensureOpen();
            this.mMarkPos = AssetManager.nativeAssetSeek(this.mAssetNativePtr, 0, 0);
        }

        @Override // java.io.InputStream
        public final void reset() throws IOException {
            ensureOpen();
            AssetManager.nativeAssetSeek(this.mAssetNativePtr, this.mMarkPos, -1);
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
        public final void close() throws IOException {
            long j = this.mAssetNativePtr;
            if (j != 0) {
                AssetManager.nativeAssetDestroy(j);
                this.mAssetNativePtr = 0;
                synchronized (AssetManager.this) {
                    AssetManager.this.decRefsLocked((long) hashCode());
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.Object
        public void finalize() throws Throwable {
            close();
        }

        private void ensureOpen() {
            if (this.mAssetNativePtr == 0) {
                throw new IllegalStateException("AssetInputStream is closed");
            }
        }
    }

    @UnsupportedAppUsage
    public boolean isUpToDate() {
        synchronized (this) {
            if (!this.mOpen) {
                return false;
            }
            for (ApkAssets apkAssets : this.mApkAssets) {
                if (!apkAssets.isUpToDate()) {
                    return false;
                }
            }
            return true;
        }
    }

    public String[] getLocales() {
        String[] nativeGetLocales;
        synchronized (this) {
            ensureValidLocked();
            nativeGetLocales = nativeGetLocales(this.mObject, false);
        }
        return nativeGetLocales;
    }

    public String[] getNonSystemLocales() {
        String[] nativeGetLocales;
        synchronized (this) {
            ensureValidLocked();
            nativeGetLocales = nativeGetLocales(this.mObject, true);
        }
        return nativeGetLocales;
    }

    /* access modifiers changed from: package-private */
    public Configuration[] getSizeConfigurations() {
        Configuration[] nativeGetSizeConfigurations;
        synchronized (this) {
            ensureValidLocked();
            nativeGetSizeConfigurations = nativeGetSizeConfigurations(this.mObject);
        }
        return nativeGetSizeConfigurations;
    }

    @UnsupportedAppUsage
    public void setConfiguration(int mcc, int mnc, String locale, int orientation, int touchscreen, int density, int keyboard, int keyboardHidden, int navigation, int screenWidth, int screenHeight, int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp, int screenLayout, int uiMode, int colorMode, int majorVersion) {
        synchronized (this) {
            ensureValidLocked();
            nativeSetConfiguration(this.mObject, mcc, mnc, locale, orientation, touchscreen, density, keyboard, keyboardHidden, navigation, screenWidth, screenHeight, smallestScreenWidthDp, screenWidthDp, screenHeightDp, screenLayout, uiMode, colorMode, majorVersion);
        }
    }

    @UnsupportedAppUsage
    public SparseArray<String> getAssignedPackageIdentifiers() {
        SparseArray<String> nativeGetAssignedPackageIdentifiers;
        synchronized (this) {
            ensureValidLocked();
            nativeGetAssignedPackageIdentifiers = nativeGetAssignedPackageIdentifiers(this.mObject);
        }
        return nativeGetAssignedPackageIdentifiers;
    }

    @GuardedBy({"this"})
    public Map<String, String> getOverlayableMap(String packageName) {
        Map<String, String> nativeGetOverlayableMap;
        synchronized (this) {
            ensureValidLocked();
            nativeGetOverlayableMap = nativeGetOverlayableMap(this.mObject, packageName);
        }
        return nativeGetOverlayableMap;
    }

    @GuardedBy({"this"})
    private void incRefsLocked(long id) {
        this.mNumRefs++;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"this"})
    private void decRefsLocked(long id) {
        this.mNumRefs--;
        if (this.mNumRefs == 0) {
            long j = this.mObject;
            if (j != 0) {
                nativeDestroy(j);
                this.mObject = 0;
                this.mApkAssets = sEmptyApkAssets;
            }
        }
    }

    public void setDeepType(int deepType) {
        this.mDeepType = deepType;
    }

    public int getDeepType() {
        return this.mDeepType;
    }

    /* access modifiers changed from: protected */
    public CharSequence getResourceText(int ident, boolean flag) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            Preconditions.checkNotNull(outValue, "outValue");
            ensureValidLocked();
            int cookie = nativeGetResourceValue(this.mObject, ident, 0, outValue, true);
            if (cookie <= 0) {
                return null;
            }
            outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(outValue.changingConfigurations);
            if (outValue.type == 3) {
                outValue.string = this.mApkAssets[cookie - 1].getStringFromPool(outValue.data);
            }
            return outValue.string;
        }
    }
}
