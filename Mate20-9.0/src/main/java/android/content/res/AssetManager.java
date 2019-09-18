package android.content.res;

import android.content.pm.ActivityInfo;
import android.content.res.XmlBlock;
import android.hardware.radio.V1_0.ApnTypes;
import android.hardware.radio.V1_0.RadioError;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.SELinux;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import libcore.io.IoUtils;

public final class AssetManager implements AutoCloseable {
    public static final int ACCESS_BUFFER = 3;
    public static final int ACCESS_RANDOM = 1;
    public static final int ACCESS_STREAMING = 2;
    public static final int ACCESS_UNKNOWN = 0;
    private static final boolean DEBUG_REFS = false;
    private static final String FRAMEWORK_APK_PATH = "/system/framework/framework-res.apk";
    private static final String FRAMEWORK_HWEXT_APK_PATH = "/system/framework/framework-res-hwext.apk";
    private static final String SHARED_RES_FOLDER = "SharedRes";
    private static final String TAG = "AssetManager";
    private static String backupResPath = "/data/hw_init/system/SharedRes/";
    private static String curLocale = "";
    private static final String defautlResPath = "/data/share/SharedRes/";
    private static boolean hasSharedRes = false;
    private static boolean isBackupResPathUpdated = false;
    private static long lastCheck = 0;
    private static String[] lastCheckResult = new String[0];
    private static final ApkAssets[] sEmptyApkAssets = new ApkAssets[0];
    private static final Object sSync = new Object();
    @GuardedBy("sSync")
    static AssetManager sSystem = null;
    @GuardedBy("sSync")
    private static ApkAssets[] sSystemApkAssets = new ApkAssets[0];
    @GuardedBy("sSync")
    private static ArraySet<ApkAssets> sSystemApkAssetsSet;
    private static HashMap<Integer, AssetManager> sharedAsset = new HashMap<>();
    private static boolean uninited = true;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public ApkAssets[] mApkAssets;
    private int mDeepType;
    @GuardedBy("this")
    private int mNumRefs;
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public long mObject;
    @GuardedBy("this")
    private final long[] mOffsets;
    @GuardedBy("this")
    private boolean mOpen;
    @GuardedBy("this")
    private HashMap<Long, RuntimeException> mRefStacks;
    @GuardedBy("this")
    private final TypedValue mValue;
    private String myPid;

    public final class AssetInputStream extends InputStream {
        private long mAssetNativePtr;
        private long mLength;
        private long mMarkPos;

        public final int getAssetInt() {
            throw new UnsupportedOperationException();
        }

        public final long getNativeAsset() {
            return this.mAssetNativePtr;
        }

        private AssetInputStream(long assetNativePtr) {
            this.mAssetNativePtr = assetNativePtr;
            this.mLength = AssetManager.nativeAssetGetLength(assetNativePtr);
        }

        public final int read() throws IOException {
            ensureOpen();
            return AssetManager.nativeAssetReadChar(this.mAssetNativePtr);
        }

        public final int read(byte[] b) throws IOException {
            ensureOpen();
            Preconditions.checkNotNull(b, "b");
            return AssetManager.nativeAssetRead(this.mAssetNativePtr, b, 0, b.length);
        }

        public final int read(byte[] b, int off, int len) throws IOException {
            ensureOpen();
            Preconditions.checkNotNull(b, "b");
            return AssetManager.nativeAssetRead(this.mAssetNativePtr, b, off, len);
        }

        public final long skip(long n) throws IOException {
            ensureOpen();
            long pos = AssetManager.nativeAssetSeek(this.mAssetNativePtr, 0, 0);
            if (pos + n > this.mLength) {
                n = this.mLength - pos;
            }
            if (n > 0) {
                long unused = AssetManager.nativeAssetSeek(this.mAssetNativePtr, n, 0);
            }
            return n;
        }

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

        public final void reset() throws IOException {
            ensureOpen();
            long unused = AssetManager.nativeAssetSeek(this.mAssetNativePtr, this.mMarkPos, -1);
        }

        public final void close() throws IOException {
            if (this.mAssetNativePtr != 0) {
                AssetManager.nativeAssetDestroy(this.mAssetNativePtr);
                this.mAssetNativePtr = 0;
                synchronized (AssetManager.this) {
                    AssetManager.this.decRefsLocked((long) hashCode());
                }
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            close();
        }

        private void ensureOpen() {
            if (this.mAssetNativePtr == 0) {
                throw new IllegalStateException("AssetInputStream is closed");
            }
        }
    }

    public static class Builder {
        private AssetManager mAssetManager;
        private ArrayList<ApkAssets> mUserApkAssets;

        public Builder() {
            this.mUserApkAssets = new ArrayList<>();
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

        public AssetManager build() {
            ApkAssets[] systemApkAssets = AssetManager.getSystem().getApkAssets();
            ApkAssets[] apkAssets = new ApkAssets[(systemApkAssets.length + this.mUserApkAssets.size())];
            System.arraycopy(systemApkAssets, 0, apkAssets, 0, systemApkAssets.length);
            int userApkAssetCount = this.mUserApkAssets.size();
            for (int i = 0; i < userApkAssetCount; i++) {
                apkAssets[systemApkAssets.length + i] = this.mUserApkAssets.get(i);
            }
            AssetManager assetManager = this.mAssetManager;
            ApkAssets[] unused = assetManager.mApkAssets = apkAssets;
            AssetManager.nativeSetApkAssets(assetManager.mObject, apkAssets, false);
            return assetManager;
        }
    }

    public static native String getAssetAllocations();

    public static native int getGlobalAssetCount();

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

    private static native long nativeCreate();

    private static native void nativeDestroy(long j);

    private static native SparseArray<String> nativeGetAssignedPackageIdentifiers(long j);

    private static native String[] nativeGetLocales(long j, boolean z);

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

    private static native void nativeThemeApplyStyle(long j, long j2, int i, boolean z);

    static native void nativeThemeClear(long j);

    static native void nativeThemeCopy(long j, long j2);

    private static native long nativeThemeCreate(long j);

    private static native void nativeThemeDestroy(long j);

    private static native void nativeThemeDump(long j, long j2, int i, String str, String str2);

    private static native int nativeThemeGetAttributeValue(long j, long j2, int i, TypedValue typedValue, boolean z);

    static native int nativeThemeGetChangingConfigurations(long j);

    private static native void nativeVerifySystemIdmaps();

    public AssetManager() {
        ApkAssets[] assets;
        this.mValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mApkAssets = new ApkAssets[0];
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

    private static void setBackupResPath() {
        if (!isBackupResPathUpdated) {
            isBackupResPathUpdated = true;
            String path = getSharedResCustPath(SHARED_RES_FOLDER);
            if (path != null) {
                backupResPath = path;
            }
        }
    }

    private static String getSharedResCustPath(String filename) {
        try {
            File file = HwCfgFilePolicy.getCfgFile(filename, 0);
            if (file == null) {
                return null;
            }
            return file.getCanonicalPath() + File.separator;
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy.getCfgFile not supported.");
            return null;
        } catch (IOException e2) {
            Log.e(TAG, "Exception in getting canonical path");
            return null;
        }
    }

    private AssetManager(boolean sentinel) {
        this.mValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mApkAssets = new ApkAssets[0];
        this.mOpen = true;
        this.mNumRefs = 1;
        this.mDeepType = 0;
        this.mObject = nativeCreate();
        if (Process.myUid() == 0) {
            setSharePemmison();
        }
    }

    @GuardedBy("sSync")
    private static void createSystemAssetsInZygoteLocked() {
        if (sSystem == null) {
            nativeVerifySystemIdmaps();
            try {
                ArrayList<ApkAssets> apkAssets = new ArrayList<>();
                apkAssets.add(ApkAssets.loadFromPath(FRAMEWORK_APK_PATH, true));
                apkAssets.add(ApkAssets.loadFromPath(FRAMEWORK_HWEXT_APK_PATH, true));
                loadStaticRuntimeOverlays(apkAssets);
                sSystemApkAssetsSet = new ArraySet<>(apkAssets);
                sSystemApkAssets = (ApkAssets[]) apkAssets.toArray(new ApkAssets[apkAssets.size()]);
                sSystem = new AssetManager(true);
                sSystem.setApkAssets(sSystemApkAssets, false);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create system AssetManager", e);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0044, code lost:
        if (r3 == null) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        $closeResource(null, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        $closeResource(null, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        libcore.io.IoUtils.closeQuietly(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0051, code lost:
        r4 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0052, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0056, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0057, code lost:
        r9 = r5;
        r5 = r4;
        r4 = r9;
     */
    private static void loadStaticRuntimeOverlays(ArrayList<ApkAssets> outApkAssets) throws IOException {
        BufferedReader br;
        Throwable th;
        Throwable th2;
        try {
            FileInputStream fis = new FileInputStream("/data/resource-cache/overlays.list");
            try {
                br = new BufferedReader(new InputStreamReader(fis));
                FileLock flock = fis.getChannel().lock(0, Long.MAX_VALUE, true);
                while (true) {
                    String readLine = br.readLine();
                    String line = readLine;
                    if (readLine == null) {
                        break;
                    }
                    String[] lineArray = line.split(" ");
                    if (lineArray != null) {
                        if (lineArray.length >= 2) {
                            outApkAssets.add(ApkAssets.loadOverlayFromPath(lineArray[1], true));
                        }
                    }
                }
                if (flock != null) {
                    $closeResource(th, flock);
                }
                throw th2;
            } catch (Throwable th3) {
                IoUtils.closeQuietly(fis);
                throw th3;
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

    public static AssetManager getSystem() {
        AssetManager assetManager;
        synchronized (sSync) {
            createSystemAssetsInZygoteLocked();
            assetManager = sSystem;
        }
        return assetManager;
    }

    public void close() {
        synchronized (this) {
            if (this.mOpen) {
                this.mOpen = false;
                decRefsLocked((long) hashCode());
            }
        }
    }

    /* JADX WARNING: type inference failed for: r2v7, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void setApkAssets(ApkAssets[] apkAssets, boolean invalidateCaches) {
        Preconditions.checkNotNull(apkAssets, "apkAssets");
        ApkAssets[] newApkAssets = new ApkAssets[(sSystemApkAssets.length + apkAssets.length)];
        System.arraycopy(sSystemApkAssets, 0, newApkAssets, 0, sSystemApkAssets.length);
        int newLength = sSystemApkAssets.length;
        for (ApkAssets apkAsset : apkAssets) {
            if (!sSystemApkAssetsSet.contains(apkAsset)) {
                newApkAssets[newLength] = apkAsset;
                newLength++;
            }
        }
        if (newLength != newApkAssets.length) {
            newApkAssets = Arrays.copyOf(newApkAssets, newLength);
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

    public ApkAssets[] getApkAssets() {
        synchronized (this) {
            if (!this.mOpen) {
                return sEmptyApkAssets;
            }
            ApkAssets[] apkAssetsArr = this.mApkAssets;
            return apkAssetsArr;
        }
    }

    public int findCookieForPath(String path) {
        Preconditions.checkNotNull(path, "path");
        synchronized (this) {
            ensureValidLocked();
            int count = this.mApkAssets.length;
            for (int i = 0; i < count; i++) {
                if (path.equals(this.mApkAssets[i].getAssetPath())) {
                    int i2 = i + 1;
                    return i2;
                }
            }
            return 0;
        }
    }

    @Deprecated
    public int addAssetPath(String path) {
        return addAssetPathInternal(path, false, false);
    }

    @Deprecated
    public int addAssetPathAsSharedLibrary(String path) {
        return addAssetPathInternal(path, false, true);
    }

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
                    int i2 = i + 1;
                    return i2;
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
            int i3 = count + 1;
            return i3;
        }
    }

    @GuardedBy("this")
    private void ensureValidLocked() {
        if (this.mObject == 0) {
            throw new RuntimeException("AssetManager has been destroyed");
        }
    }

    @GuardedBy("this")
    private void ensureOpenLocked() {
        if (!this.mOpen) {
            throw new RuntimeException("AssetManager has been closed");
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
        return true;
     */
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
                if (hasSharedRes) {
                    CharSequence rt = getTextForDBid(outValue.string);
                    if (rt != null) {
                        outValue.string = rt;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public CharSequence getResourceText(int resId) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            if (!getResourceValue(resId, 0, outValue, true)) {
                return null;
            }
            CharSequence coerceToString = outValue.coerceToString();
            return coerceToString;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0038, code lost:
        return r2;
     */
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
                if (hasRes()) {
                    CharSequence rt = getTextForDBid(result);
                    if (rt != null) {
                        result = rt;
                    }
                }
            } else {
                CharSequence result2 = outValue.coerceToString();
                return result2;
            }
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
            if (hasRes()) {
                for (int i = 0; i < retArray.length; i++) {
                    CharSequence rt = getTextForDBid(retArray[i]);
                    if (rt != null) {
                        retArray[i] = rt.toString();
                    }
                }
            }
        }
        return retArray;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        return r4;
     */
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
            int i2 = 0;
            int j = 0;
            while (i2 < rawInfoArrayLen) {
                int cookie = rawInfoArray[i2];
                int index = rawInfoArray[i2 + 1];
                retArray[j] = (index < 0 || cookie <= 0) ? null : this.mApkAssets[cookie - 1].getStringFromPool(index);
                i2 += 2;
                j++;
            }
            if (hasRes()) {
                while (true) {
                    int i3 = i;
                    if (i3 >= retArray.length) {
                        break;
                    }
                    CharSequence rt = getTextForDBid(retArray[i3]);
                    if (rt != null) {
                        retArray[i3] = rt;
                    }
                    i = i3 + 1;
                }
            }
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
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0036, code lost:
        return true;
     */
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
    public String getResourceName(int resId) {
        String nativeGetResourceName;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceName = nativeGetResourceName(this.mObject, resId);
        }
        return nativeGetResourceName;
    }

    /* access modifiers changed from: package-private */
    public String getResourcePackageName(int resId) {
        String nativeGetResourcePackageName;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourcePackageName = nativeGetResourcePackageName(this.mObject, resId);
        }
        return nativeGetResourcePackageName;
    }

    /* access modifiers changed from: package-private */
    public String getResourceTypeName(int resId) {
        String nativeGetResourceTypeName;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceTypeName = nativeGetResourceTypeName(this.mObject, resId);
        }
        return nativeGetResourceTypeName;
    }

    /* access modifiers changed from: package-private */
    public String getResourceEntryName(int resId) {
        String nativeGetResourceEntryName;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceEntryName = nativeGetResourceEntryName(this.mObject, resId);
        }
        return nativeGetResourceEntryName;
    }

    /* access modifiers changed from: package-private */
    public int getResourceIdentifier(String name, String defType, String defPackage) {
        int nativeGetResourceIdentifier;
        synchronized (this) {
            ensureValidLocked();
            nativeGetResourceIdentifier = nativeGetResourceIdentifier(this.mObject, name, defType, defPackage);
        }
        return nativeGetResourceIdentifier;
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

    public InputStream openNonAsset(String fileName) throws IOException {
        return openNonAsset(0, fileName, 2);
    }

    public InputStream openNonAsset(String fileName, int accessMode) throws IOException {
        return openNonAsset(0, fileName, accessMode);
    }

    public InputStream openNonAsset(int cookie, String fileName) throws IOException {
        return openNonAsset(cookie, fileName, 2);
    }

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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001e, code lost:
        if (r0 != null) goto L_0x0020;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0020, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0023, code lost:
        throw r2;
     */
    public XmlResourceParser openXmlResourceParser(int cookie, String fileName) throws IOException {
        XmlBlock block = openXmlBlockAsset(cookie, fileName);
        XmlResourceParser parser = block.newParser();
        if (parser != null) {
            if (block != null) {
                $closeResource(null, block);
            }
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
    public void applyStyle(long themePtr, int defStyleAttr, int defStyleRes, XmlBlock.Parser parser, int[] inAttrs, long outValuesAddress, long outIndicesAddress) {
        XmlBlock.Parser parser2 = parser;
        int[] iArr = inAttrs;
        Preconditions.checkNotNull(iArr, "inAttrs");
        synchronized (this) {
            ensureValidLocked();
            nativeApplyStyle(this.mObject, themePtr, defStyleAttr, defStyleRes, parser2 != null ? parser2.mParseState : 0, iArr, outValuesAddress, outIndicesAddress);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean resolveAttrs(long themePtr, int defStyleAttr, int defStyleRes, int[] inValues, int[] inAttrs, int[] outValues, int[] outIndices) {
        boolean nativeResolveAttrs;
        int[] iArr = inAttrs;
        Preconditions.checkNotNull(iArr, "inAttrs");
        int[] iArr2 = outValues;
        Preconditions.checkNotNull(iArr2, "outValues");
        int[] iArr3 = outIndices;
        Preconditions.checkNotNull(iArr3, "outIndices");
        synchronized (this) {
            ensureValidLocked();
            nativeResolveAttrs = nativeResolveAttrs(this.mObject, themePtr, defStyleAttr, defStyleRes, inValues, iArr, iArr2, iArr3);
        }
        return nativeResolveAttrs;
    }

    /* access modifiers changed from: package-private */
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

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this.mObject != 0) {
            nativeDestroy(this.mObject);
        }
    }

    public boolean isUpToDate() {
        for (ApkAssets apkAssets : getApkAssets()) {
            if (!apkAssets.isUpToDate()) {
                return false;
            }
        }
        return true;
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

    public void setConfiguration(int mcc, int mnc, String locale, int orientation, int touchscreen, int density, int keyboard, int keyboardHidden, int navigation, int screenWidth, int screenHeight, int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp, int screenLayout, int uiMode, int colorMode, int majorVersion, boolean ResFlag) {
        synchronized (this) {
            String tempLocale = locale;
            try {
                if (!curLocale.equals(locale) && ResFlag) {
                    this.myPid = Process.getCmdlineForPid(Process.myPid());
                    if (!this.myPid.contains("zygote")) {
                        setDbidConfig(locale);
                        makeSharedResource();
                    }
                }
                if (!sharedAsset.isEmpty()) {
                    String[] locales = getNonSystemLocales();
                    if (locales.length > 0) {
                        int length = locales.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            } else if (locales[i].equals("zz-ZX")) {
                                tempLocale = "zz-ZX";
                                break;
                            } else {
                                i++;
                            }
                        }
                    } else {
                        tempLocale = "zz-ZX";
                    }
                }
                ensureValidLocked();
                nativeSetConfiguration(this.mObject, mcc, mnc, tempLocale, orientation, touchscreen, density, keyboard, keyboardHidden, navigation, screenWidth, screenHeight, smallestScreenWidthDp, screenWidthDp, screenHeightDp, screenLayout, uiMode, colorMode, majorVersion);
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public void setConfiguration(int mcc, int mnc, String locale, int orientation, int touchscreen, int density, int keyboard, int keyboardHidden, int navigation, int screenWidth, int screenHeight, int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp, int screenLayout, int uiMode, int colorMode, int majorVersion) {
        setConfiguration(mcc, mnc, locale, orientation, touchscreen, density, keyboard, keyboardHidden, navigation, screenWidth, screenHeight, smallestScreenWidthDp, screenWidthDp, screenHeightDp, screenLayout, uiMode, colorMode, majorVersion, false);
    }

    public SparseArray<String> getAssignedPackageIdentifiers() {
        SparseArray<String> nativeGetAssignedPackageIdentifiers;
        synchronized (this) {
            ensureValidLocked();
            nativeGetAssignedPackageIdentifiers = nativeGetAssignedPackageIdentifiers(this.mObject);
        }
        return nativeGetAssignedPackageIdentifiers;
    }

    @GuardedBy("this")
    private void incRefsLocked(long id) {
        this.mNumRefs++;
    }

    /* access modifiers changed from: private */
    @GuardedBy("this")
    public void decRefsLocked(long id) {
        this.mNumRefs--;
        if (this.mNumRefs == 0 && this.mObject != 0) {
            nativeDestroy(this.mObject);
            this.mObject = 0;
            this.mApkAssets = sEmptyApkAssets;
        }
    }

    public void setDeepType(int deepType) {
        this.mDeepType = deepType;
    }

    public int getDeepType() {
        return this.mDeepType;
    }

    private static void setSharePemmison() {
        File resRoot = new File(defautlResPath.trim());
        if (!resRoot.getParentFile().exists()) {
            if (resRoot.getParentFile().mkdir()) {
                FileUtils.setPermissions(resRoot.getParentFile().getAbsolutePath(), RadioError.OEM_ERROR_9, -1, -1);
                SELinux.setFileContext(resRoot.getParentFile().getAbsolutePath(), "u:object_r:media_rw_data_file:s0");
            }
        } else if (resRoot.getParentFile().canWrite()) {
            FileUtils.setPermissions(resRoot.getParentFile().getAbsolutePath(), RadioError.OEM_ERROR_9, -1, -1);
        }
        if (!resRoot.exists() && resRoot.mkdir()) {
            FileUtils.setPermissions(resRoot.getAbsolutePath(), RadioError.OEM_ERROR_9, Process.myUid(), ApnTypes.ALL);
        }
    }

    private static boolean makeSharedResource() {
        String lang;
        if (!sharedAsset.isEmpty()) {
            for (Integer key : sharedAsset.keySet()) {
                sharedAsset.get(key).close();
            }
            sharedAsset.clear();
        }
        setBackupResPath();
        String country = "";
        String script = "";
        int len = curLocale.length();
        if (len == 2) {
            lang = curLocale;
        } else if (len == 5) {
            lang = curLocale.substring(0, 2);
            country = curLocale.substring(3, 5);
        } else if (len == 7) {
            lang = curLocale.substring(0, 2);
            script = curLocale.substring(3, 7);
        } else if (len == 10) {
            lang = curLocale.substring(0, 2);
            script = curLocale.substring(3, 7);
            country = curLocale.substring(8, 10);
        } else {
            lang = curLocale;
        }
        String locale = getParentLocale(lang + "_" + country);
        if (locale.length() == 5) {
            lang = locale.substring(0, 2);
            country = locale.substring(3, 5);
        }
        File resfolder = new File(defautlResPath + lang + "-" + country);
        File backupresfolder = new File(backupResPath + lang + "-" + country);
        if (!resfolder.exists() && !backupresfolder.exists()) {
            resfolder = new File(defautlResPath + lang + "-" + script);
            backupresfolder = new File(backupResPath + lang + "-" + script);
            if (!resfolder.exists() && !backupresfolder.exists()) {
                resfolder = new File(defautlResPath + lang);
                backupresfolder = new File(backupResPath + lang);
                if (!resfolder.exists() && !backupresfolder.exists()) {
                    hasSharedRes = false;
                    return false;
                }
            }
        }
        File[] filearray = resfolder.listFiles();
        if (filearray == null) {
            filearray = backupresfolder.listFiles();
        }
        if (filearray != null) {
            for (File fi : filearray) {
                if (fi.getName().contains(".lang")) {
                    int resID = Integer.parseInt(fi.getName().replace(".lang", ""));
                    AssetManager am = new AssetManager(false);
                    am.addAssetPath(fi.getPath());
                    sharedAsset.put(Integer.valueOf(resID), am);
                }
            }
        }
        if (sharedAsset.size() > 0) {
            hasSharedRes = true;
            return true;
        }
        hasSharedRes = false;
        return false;
    }

    public static boolean hasRes() {
        return hasSharedRes;
    }

    private static String getParentLocale(String locale) {
        String[][] parentlist = {new String[]{"es_AR", "es_US"}, new String[]{"es_BO", "es_US"}, new String[]{"es_CL", "es_US"}, new String[]{"es_CO", "es_US"}, new String[]{"es_CR", "es_US"}, new String[]{"es_CU", "es_US"}, new String[]{"es_DO", "es_US"}, new String[]{"es_EC", "es_US"}, new String[]{"es_GT", "es_US"}, new String[]{"es_HN", "es_US"}, new String[]{"es_MX", "es_US"}, new String[]{"es_NI", "es_US"}, new String[]{"es_PA", "es_US"}, new String[]{"es_PE", "es_US"}, new String[]{"es_PR", "es_US"}, new String[]{"es_PY", "es_US"}, new String[]{"es_SV", "es_US"}, new String[]{"es_UY", "es_US"}, new String[]{"es_VE", "es_US"}, new String[]{"pt_AO", "pt_PT"}, new String[]{"pt_CV", "pt_PT"}, new String[]{"pt_GW", "pt_PT"}, new String[]{"pt_MO", "pt_PT"}, new String[]{"pt_MZ", "pt_PT"}, new String[]{"pt_ST", "pt_PT"}, new String[]{"pt_TL", "pt_PT"}, new String[]{"en_AU", "en_GB"}, new String[]{"en_BE", "en_GB"}, new String[]{"en_DG", "en_GB"}, new String[]{"en_FK", "en_GB"}, new String[]{"en_GG", "en_GB"}, new String[]{"en_GI", "en_GB"}, new String[]{"en_HK", "en_GB"}, new String[]{"en_IE", "en_GB"}, new String[]{"en_IM", "en_GB"}, new String[]{"en_IN", "en_GB"}, new String[]{"en_IO", "en_GB"}, new String[]{"en_JE", "en_GB"}, new String[]{"en_MO", "en_GB"}, new String[]{"en_MT", "en_GB"}, new String[]{"en_NZ", "en_GB"}, new String[]{"en_PK", "en_GB"}, new String[]{"en_SG", "en_GB"}, new String[]{"en_SH", "en_GB"}, new String[]{"en_VG", "en_GB"}, new String[]{"en_BN", "en_GB"}, new String[]{"en_MY", "en_GB"}, new String[]{"en_PG", "en_GB"}, new String[]{"en_NR", "en_GB"}, new String[]{"en_WS", "en_GB"}, new String[]{"zh_MO", "zh_HK"}, new String[]{"zh_SG", "zh_CN"}, new String[]{"ms_BN", "ms_MY"}, new String[]{"ms_SG", "ms_MY"}, new String[]{"uz_AF", "uz_UZ"}, new String[]{"bo_IN", "bo_CN"}};
        for (int i = 0; i < parentlist.length; i++) {
            if (parentlist[i][0].equals(locale)) {
                return parentlist[i][1];
            }
        }
        return locale;
    }

    private static void setDbidConfig(String locale) {
        curLocale = locale;
    }

    public static CharSequence getTextForDBid(CharSequence dbid_cs) {
        if (dbid_cs == null || dbid_cs.length() <= 5 || dbid_cs.charAt(0) != '[' || dbid_cs.charAt(3) != '_') {
            return null;
        }
        String dbid_str = dbid_cs.toString();
        int dbid = Integer.parseInt(dbid_str.substring(4, dbid_str.indexOf(93)));
        if (dbid <= 0) {
            return null;
        }
        synchronized (sharedAsset) {
            AssetManager am = sharedAsset.get(Integer.valueOf(dbid / 65536));
            if (am == null && uninited) {
                makeSharedResource();
                uninited = false;
                am = sharedAsset.get(Integer.valueOf(dbid / 65536));
            }
            if (am != null) {
                CharSequence sharedResult = am.getResourceText(2130837504 | (dbid % 65536), true);
                if (sharedResult != null && sharedResult.length() > 0) {
                    return sharedResult;
                }
            }
            CharSequence subSequence = dbid_cs.subSequence(dbid_cs.toString().indexOf(93) + 2, dbid_cs.length());
            return subSequence;
        }
    }

    private CharSequence getResourceText(int ident, boolean flag) {
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
            CharSequence charSequence = outValue.string;
            return charSequence;
        }
    }

    public static String[] getSharedResList() {
        long now = System.currentTimeMillis();
        if (now - lastCheck <= 1000) {
            return lastCheckResult;
        }
        File resfolder = new File(defautlResPath.trim());
        String[] first = null;
        String[] second = null;
        if (resfolder.exists()) {
            first = resfolder.list();
        }
        setBackupResPath();
        File resfolder2 = new File(backupResPath.trim());
        if (resfolder2.exists()) {
            second = resfolder2.list();
        }
        if (first == null) {
            first = new String[0];
        }
        if (second == null) {
            second = new String[0];
        }
        String[] result = (String[]) Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        lastCheck = now;
        lastCheckResult = result;
        return result;
    }
}
