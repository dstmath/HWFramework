package android.content.res;

import android.app.ActivityThread;
import android.common.HwFrameworkFactory;
import android.content.pm.ActivityInfo;
import android.net.ProxyInfo;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.util.HwPCUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

public final class AssetManager implements AutoCloseable {
    public static final int ACCESS_BUFFER = 3;
    public static final int ACCESS_RANDOM = 1;
    public static final int ACCESS_STREAMING = 2;
    public static final int ACCESS_UNKNOWN = 0;
    private static final boolean DEBUG_REFS = false;
    static final int STYLE_ASSET_COOKIE = 2;
    static final int STYLE_CHANGING_CONFIGURATIONS = 4;
    static final int STYLE_DATA = 1;
    static final int STYLE_DENSITY = 5;
    static final int STYLE_NUM_ENTRIES = 6;
    static final int STYLE_RESOURCE_ID = 3;
    static final int STYLE_TYPE = 0;
    private static final String TAG = "AssetManager";
    private static final String backupResPath = "/data/hw_init/system/SharedRes/";
    private static String curLocale = ProxyInfo.LOCAL_EXCL_LIST;
    private static final String defautlResPath = "/data/share/SharedRes/";
    private static boolean hasSharedRes = false;
    private static final boolean localLOGV = false;
    private static final Object sSync = new Object();
    static AssetManager sSystem = null;
    private static HashMap<Integer, AssetManager> sharedAsset = new HashMap();
    private final TypedValue mDbidValue;
    private int mNumRefs;
    private long mObject;
    private final long[] mOffsets;
    private boolean mOpen;
    private HashMap<Long, RuntimeException> mRefStacks;
    private StringBlock[] mStringBlocks;
    private final TypedValue mValue;

    public final class AssetInputStream extends InputStream {
        private long mAsset;
        private long mLength;
        private long mMarkPos;

        /* synthetic */ AssetInputStream(AssetManager this$0, long asset, AssetInputStream -this2) {
            this(asset);
        }

        public final int getAssetInt() {
            throw new UnsupportedOperationException();
        }

        public final long getNativeAsset() {
            return this.mAsset;
        }

        private AssetInputStream(long asset) {
            this.mAsset = asset;
            this.mLength = AssetManager.this.getAssetLength(asset);
        }

        public final int read() throws IOException {
            return AssetManager.this.readAssetChar(this.mAsset);
        }

        public final boolean markSupported() {
            return true;
        }

        public final int available() throws IOException {
            long len = AssetManager.this.getAssetRemainingLength(this.mAsset);
            return len > 2147483647L ? Integer.MAX_VALUE : (int) len;
        }

        public final void close() throws IOException {
            synchronized (AssetManager.this) {
                if (this.mAsset != 0) {
                    AssetManager.this.destroyAsset(this.mAsset);
                    this.mAsset = 0;
                    AssetManager.this.decRefsLocked((long) hashCode());
                }
            }
        }

        public final void mark(int readlimit) {
            this.mMarkPos = AssetManager.this.seekAsset(this.mAsset, 0, 0);
        }

        public final void reset() throws IOException {
            AssetManager.this.seekAsset(this.mAsset, this.mMarkPos, -1);
        }

        public final int read(byte[] b) throws IOException {
            return AssetManager.this.readAsset(this.mAsset, b, 0, b.length);
        }

        public final int read(byte[] b, int off, int len) throws IOException {
            return AssetManager.this.readAsset(this.mAsset, b, off, len);
        }

        public final long skip(long n) throws IOException {
            long pos = AssetManager.this.seekAsset(this.mAsset, 0, 0);
            if (pos + n > this.mLength) {
                n = this.mLength - pos;
            }
            if (n > 0) {
                AssetManager.this.seekAsset(this.mAsset, n, 0);
            }
            return n;
        }

        protected void finalize() throws Throwable {
            close();
        }
    }

    private final native int addAssetPathNative(String str, boolean z);

    static final native void applyStyle(long j, int i, int i2, long j2, int[] iArr, int i3, long j3, long j4);

    static final native void applyThemeStyle(long j, int i, boolean z);

    static final native void clearTheme(long j);

    static final native void copyTheme(long j, long j2);

    private final native void deleteTheme(long j);

    private final native void destroy();

    private final native void destroyAsset(long j);

    static final native void dumpTheme(long j, int i, String str, String str2);

    private final native int[] getArrayStringInfo(int i);

    private final native String[] getArrayStringResource(int i);

    public static final native String getAssetAllocations();

    private final native long getAssetLength(long j);

    private final native long getAssetRemainingLength(long j);

    public static final native int getGlobalAssetCount();

    public static final native int getGlobalAssetManagerCount();

    private final native long getNativeStringBlock(int i);

    private final native int getStringBlockCount();

    static final native int getThemeChangingConfigurations(long j);

    private final native void init(boolean z);

    private final native int loadResourceBagValue(int i, int i2, TypedValue typedValue, boolean z);

    private final native int loadResourceValue(int i, short s, TypedValue typedValue, boolean z);

    static final native int loadThemeAttributeValue(long j, int i, TypedValue typedValue, boolean z);

    private final native long newTheme();

    private final native long openAsset(String str, int i);

    private final native ParcelFileDescriptor openAssetFd(String str, long[] jArr) throws IOException;

    private native ParcelFileDescriptor openNonAssetFdNative(int i, String str, long[] jArr) throws IOException;

    private final native long openNonAssetNative(int i, String str, int i2);

    private final native long openXmlAssetNative(int i, String str);

    private final native int readAsset(long j, byte[] bArr, int i, int i2);

    private final native int readAssetChar(long j);

    static final native boolean resolveAttrs(long j, int i, int i2, int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4);

    private final native long seekAsset(long j, long j2, int i);

    public final native int addOverlayPathNative(String str);

    final native int[] getArrayIntResource(int i);

    final native int getArraySize(int i);

    public final native SparseArray<String> getAssignedPackageIdentifiers();

    public final native String getCookieName(int i);

    public final native String[] getLocales();

    public final native String[] getNonSystemLocales();

    final native String getResourceEntryName(int i);

    final native int getResourceIdentifier(String str, String str2, String str3);

    final native String getResourceName(int i);

    final native String getResourcePackageName(int i);

    final native String getResourceTypeName(int i);

    public final native Configuration[] getSizeConfigurations();

    final native int[] getStyleAttributes(int i);

    public final native boolean isUpToDate();

    public final native String[] list(String str) throws IOException;

    final native int retrieveArray(int i, int[] iArr);

    final native boolean retrieveAttributes(long j, int[] iArr, int[] iArr2, int[] iArr3);

    public final native void setConfiguration(int i, int i2, String str, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16, int i17);

    public AssetManager() {
        this.mValue = new TypedValue();
        this.mDbidValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mStringBlocks = null;
        this.mNumRefs = 1;
        this.mOpen = true;
        synchronized (this) {
            init(false);
            ensureSystemAssets();
        }
    }

    public AssetManager(int notShared) {
        this.mValue = new TypedValue();
        this.mDbidValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mStringBlocks = null;
        this.mNumRefs = 1;
        this.mOpen = true;
        synchronized (this) {
            init(false);
        }
    }

    private static void ensureSystemAssets() {
        synchronized (sSync) {
            if (sSystem == null) {
                AssetManager system = new AssetManager(true);
                system.makeStringBlocks(null);
                sSystem = system;
            }
        }
    }

    private AssetManager(boolean isSystem) {
        this.mValue = new TypedValue();
        this.mDbidValue = new TypedValue();
        this.mOffsets = new long[2];
        this.mStringBlocks = null;
        this.mNumRefs = 1;
        this.mOpen = true;
        init(true);
    }

    public static AssetManager getSystem() {
        ensureSystemAssets();
        return sSystem;
    }

    private static void setSharePemmison() {
        File resRoot = new File(defautlResPath.trim());
        if (resRoot.getParentFile().exists()) {
            if (resRoot.getParentFile().canWrite()) {
                FileUtils.setPermissions(resRoot.getParentFile().getAbsolutePath(), 509, -1, -1);
            }
        } else if (resRoot.getParentFile().mkdir()) {
            FileUtils.setPermissions(resRoot.getParentFile().getAbsolutePath(), 509, -1, -1);
        }
        if (!resRoot.exists() && resRoot.mkdir()) {
            FileUtils.setPermissions(resRoot.getAbsolutePath(), 509, Process.myUid(), (int) Process.MEDIA_RW_GID);
        }
    }

    private static boolean makeSharedResource() {
        if (!sharedAsset.isEmpty()) {
            for (Integer key : sharedAsset.keySet()) {
                ((AssetManager) sharedAsset.get(key)).close();
            }
            sharedAsset.clear();
        }
        String lang = ProxyInfo.LOCAL_EXCL_LIST;
        String country = ProxyInfo.LOCAL_EXCL_LIST;
        String script = ProxyInfo.LOCAL_EXCL_LIST;
        if (curLocale.length() == 2) {
            lang = curLocale;
        } else if (curLocale.length() == 5) {
            lang = curLocale.substring(0, 2);
            country = curLocale.substring(3, 5);
        } else if (curLocale.length() == 7) {
            lang = curLocale.substring(0, 2);
            script = curLocale.substring(3, 7);
        } else if (curLocale.length() == 10) {
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
        if (!(resfolder.exists() || (backupresfolder.exists() ^ 1) == 0)) {
            resfolder = new File(defautlResPath + lang + "-" + script);
            backupresfolder = new File(backupResPath + lang + "-" + script);
            if (!(resfolder.exists() || (backupresfolder.exists() ^ 1) == 0)) {
                resfolder = new File(defautlResPath + lang);
                backupresfolder = new File(backupResPath + lang);
                if (!(resfolder.exists() || (backupresfolder.exists() ^ 1) == 0)) {
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
                    int resID = Integer.parseInt(fi.getName().replace(".lang", ProxyInfo.LOCAL_EXCL_LIST));
                    AssetManager am = new AssetManager(0);
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
        parentlist = new String[56][];
        parentlist[0] = new String[]{"es_AR", "es_US"};
        parentlist[1] = new String[]{"es_BO", "es_US"};
        parentlist[2] = new String[]{"es_CL", "es_US"};
        parentlist[3] = new String[]{"es_CO", "es_US"};
        parentlist[4] = new String[]{"es_CR", "es_US"};
        parentlist[5] = new String[]{"es_CU", "es_US"};
        parentlist[6] = new String[]{"es_DO", "es_US"};
        parentlist[7] = new String[]{"es_EC", "es_US"};
        parentlist[8] = new String[]{"es_GT", "es_US"};
        parentlist[9] = new String[]{"es_HN", "es_US"};
        parentlist[10] = new String[]{"es_MX", "es_US"};
        parentlist[11] = new String[]{"es_NI", "es_US"};
        parentlist[12] = new String[]{"es_PA", "es_US"};
        parentlist[13] = new String[]{"es_PE", "es_US"};
        parentlist[14] = new String[]{"es_PR", "es_US"};
        parentlist[15] = new String[]{"es_PY", "es_US"};
        parentlist[16] = new String[]{"es_SV", "es_US"};
        parentlist[17] = new String[]{"es_UY", "es_US"};
        parentlist[18] = new String[]{"es_VE", "es_US"};
        parentlist[19] = new String[]{"pt_AO", "pt_PT"};
        parentlist[20] = new String[]{"pt_CV", "pt_PT"};
        parentlist[21] = new String[]{"pt_GW", "pt_PT"};
        parentlist[22] = new String[]{"pt_MO", "pt_PT"};
        parentlist[23] = new String[]{"pt_MZ", "pt_PT"};
        parentlist[24] = new String[]{"pt_ST", "pt_PT"};
        parentlist[25] = new String[]{"pt_TL", "pt_PT"};
        parentlist[26] = new String[]{"en_AU", "en_GB"};
        parentlist[27] = new String[]{"en_BE", "en_GB"};
        parentlist[28] = new String[]{"en_DG", "en_GB"};
        parentlist[29] = new String[]{"en_FK", "en_GB"};
        parentlist[30] = new String[]{"en_GG", "en_GB"};
        parentlist[31] = new String[]{"en_GI", "en_GB"};
        parentlist[32] = new String[]{"en_HK", "en_GB"};
        parentlist[33] = new String[]{"en_IE", "en_GB"};
        parentlist[34] = new String[]{"en_IM", "en_GB"};
        parentlist[35] = new String[]{"en_IN", "en_GB"};
        parentlist[36] = new String[]{"en_IO", "en_GB"};
        parentlist[37] = new String[]{"en_JE", "en_GB"};
        parentlist[38] = new String[]{"en_MO", "en_GB"};
        parentlist[39] = new String[]{"en_MT", "en_GB"};
        parentlist[40] = new String[]{"en_NZ", "en_GB"};
        parentlist[41] = new String[]{"en_PK", "en_GB"};
        parentlist[42] = new String[]{"en_SG", "en_GB"};
        parentlist[43] = new String[]{"en_SH", "en_GB"};
        parentlist[44] = new String[]{"en_VG", "en_GB"};
        parentlist[45] = new String[]{"en_BN", "en_GB"};
        parentlist[46] = new String[]{"en_MY", "en_GB"};
        parentlist[47] = new String[]{"en_PG", "en_GB"};
        parentlist[48] = new String[]{"en_NR", "en_GB"};
        parentlist[49] = new String[]{"en_WS", "en_GB"};
        parentlist[50] = new String[]{"zh_MO", "zh_HK"};
        parentlist[51] = new String[]{"zh_SG", "zh_CN"};
        parentlist[52] = new String[]{"ms_BN", "ms_MY"};
        parentlist[53] = new String[]{"ms_SG", "ms_MY"};
        parentlist[54] = new String[]{"uz_AF", "uz_UZ"};
        parentlist[55] = new String[]{"bo_IN", "bo_CN"};
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

    public void close() {
        synchronized (this) {
            if (this.mOpen) {
                this.mOpen = false;
                decRefsLocked((long) hashCode());
            }
        }
    }

    final CharSequence getResourceText(int resId) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            if (getResourceValue(resId, 0, outValue, true)) {
                CharSequence coerceToString = outValue.coerceToString();
                return coerceToString;
            }
            return null;
        }
    }

    final CharSequence getResourceText(int ident, boolean flag) {
        synchronized (this) {
            TypedValue tmpValue = this.mDbidValue;
            int block = loadResourceValue(ident, (short) 0, tmpValue, true);
            CharSequence charSequence;
            if (block < 0) {
                return null;
            } else if (tmpValue.type == 3) {
                charSequence = this.mStringBlocks[block].get(tmpValue.data);
                return charSequence;
            } else {
                charSequence = tmpValue.coerceToString();
                return charSequence;
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0032, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final CharSequence getResourceBagText(int resId, int bagEntryId) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            int block = loadResourceBagValue(resId, bagEntryId, outValue, true);
            if (block < 0) {
                return null;
            }
            outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(outValue.changingConfigurations);
            if (outValue.type == 3) {
                CharSequence result = this.mStringBlocks[block].get(outValue.data);
                if (hasRes()) {
                    CharSequence rt = getTextForDBid(result);
                    if (rt != null) {
                        result = rt;
                    }
                }
            } else {
                CharSequence coerceToString = outValue.coerceToString();
                return coerceToString;
            }
        }
    }

    final String[] getResourceStringArray(int resId) {
        String[] retArray = getArrayStringResource(resId);
        if (hasRes()) {
            for (int i = 0; i < retArray.length; i++) {
                CharSequence rt = getTextForDBid(retArray[i]);
                if (rt != null) {
                    retArray[i] = rt.toString();
                }
            }
        }
        return retArray;
    }

    /* JADX WARNING: Missing block: B:28:0x0054, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean getResourceValue(int resId, int densityDpi, TypedValue outValue, boolean resolveRefs) {
        synchronized (this) {
            ActivityThread at = ActivityThread.currentActivityThread();
            if (at != null && HwPCUtils.isValidExtDisplayId(at.getDisplayId())) {
                IHwPCResourcesUtils hwPCResourcesUtils = HwFrameworkFactory.getHwPCResourcesUtils(this);
                if (hwPCResourcesUtils != null && hwPCResourcesUtils.getResourceValue(resId, outValue)) {
                    return true;
                }
            }
            int block = loadResourceValue(resId, (short) densityDpi, outValue, resolveRefs);
            if (block < 0) {
                return false;
            }
            outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(outValue.changingConfigurations);
            if (outValue.type == 3) {
                outValue.string = this.mStringBlocks[block].get(outValue.data);
                if (hasRes()) {
                    CharSequence rt = getTextForDBid(outValue.string);
                    if (rt != null) {
                        outValue.string = rt;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:25:0x0044, code:
            return r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final CharSequence[] getResourceTextArray(int resId) {
        synchronized (this) {
            int[] rawInfoArray = getArrayStringInfo(resId);
            if (rawInfoArray == null) {
                return null;
            }
            int rawInfoArrayLen = rawInfoArray.length;
            CharSequence[] retArray = new CharSequence[(rawInfoArrayLen / 2)];
            int i = 0;
            int j = 0;
            while (i < rawInfoArrayLen) {
                CharSequence charSequence;
                int block = rawInfoArray[i];
                int index = rawInfoArray[i + 1];
                if (index >= 0) {
                    charSequence = this.mStringBlocks[block].get(index);
                } else {
                    charSequence = null;
                }
                retArray[j] = charSequence;
                i += 2;
                j++;
            }
            if (hasRes()) {
                for (i = 0; i < retArray.length; i++) {
                    CharSequence rt = getTextForDBid(retArray[i]);
                    if (rt != null) {
                        retArray[i] = rt;
                    }
                }
            }
        }
    }

    private CharSequence getTextForDBid(CharSequence dbid_cs) {
        if (dbid_cs == null || dbid_cs.length() <= 5 || dbid_cs.charAt(0) != '[' || dbid_cs.charAt(3) != '_') {
            return null;
        }
        String dbid_str = dbid_cs.toString();
        int dbid = Integer.parseInt(dbid_str.substring(4, dbid_str.indexOf(93)));
        if (dbid <= 0) {
            return null;
        }
        synchronized (sharedAsset) {
            AssetManager am = (AssetManager) sharedAsset.get(Integer.valueOf(dbid / 65536));
            if (am != null) {
                CharSequence sharedResult = am.getResourceText((dbid % 65536) | 2130837504, true);
                if (sharedResult != null && sharedResult.length() > 0) {
                    return sharedResult;
                }
            }
            CharSequence subSequence = dbid_cs.subSequence(dbid_cs.toString().indexOf(93) + 2, dbid_cs.length());
            return subSequence;
        }
    }

    public static String[] getSharedResList() {
        File resfolder = new File(defautlResPath.trim());
        Object[] first = null;
        Object second = null;
        if (resfolder.exists()) {
            first = resfolder.list();
        }
        resfolder = new File(backupResPath.trim());
        if (resfolder.exists()) {
            second = resfolder.list();
        }
        if (first == null) {
            first = new String[0];
        }
        if (second == null) {
            second = new String[0];
        }
        String[] result = (String[]) Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    final boolean getThemeValue(long theme, int resId, TypedValue outValue, boolean resolveRefs) {
        int block = loadThemeAttributeValue(theme, resId, outValue, resolveRefs);
        if (block < 0) {
            return false;
        }
        outValue.changingConfigurations = ActivityInfo.activityInfoConfigNativeToJava(outValue.changingConfigurations);
        if (outValue.type == 3) {
            outValue.string = ensureStringBlocks()[block].get(outValue.data);
        }
        return true;
    }

    final StringBlock[] ensureStringBlocks() {
        StringBlock[] stringBlockArr;
        synchronized (this) {
            if (this.mStringBlocks == null) {
                makeStringBlocks(sSystem.mStringBlocks);
            }
            stringBlockArr = this.mStringBlocks;
        }
        return stringBlockArr;
    }

    final void makeStringBlocks(StringBlock[] seed) {
        int seedNum = seed != null ? seed.length : 0;
        int num = getStringBlockCount();
        this.mStringBlocks = new StringBlock[num];
        for (int i = 0; i < num; i++) {
            if (i < seedNum) {
                this.mStringBlocks[i] = seed[i];
            } else {
                this.mStringBlocks[i] = new StringBlock(getNativeStringBlock(i), true);
            }
        }
    }

    final CharSequence getPooledStringForCookie(int cookie, int id) {
        CharSequence charSequence;
        synchronized (this) {
            charSequence = this.mStringBlocks[cookie - 1].get(id);
        }
        return charSequence;
    }

    public final InputStream open(String fileName) throws IOException {
        return open(fileName, 2);
    }

    public final InputStream open(String fileName, int accessMode) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                long asset = openAsset(fileName, accessMode);
                if (asset != 0) {
                    AssetInputStream res = new AssetInputStream(this, asset, null);
                    incRefsLocked((long) res.hashCode());
                    return res;
                }
                throw new FileNotFoundException("Asset file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final AssetFileDescriptor openFd(String fileName) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                ParcelFileDescriptor pfd = openAssetFd(fileName, this.mOffsets);
                if (pfd != null) {
                    AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(pfd, this.mOffsets[0], this.mOffsets[1]);
                    return assetFileDescriptor;
                }
                throw new FileNotFoundException("Asset file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final InputStream openNonAsset(String fileName) throws IOException {
        return openNonAsset(0, fileName, 2);
    }

    public final InputStream openNonAsset(String fileName, int accessMode) throws IOException {
        return openNonAsset(0, fileName, accessMode);
    }

    public final InputStream openNonAsset(int cookie, String fileName) throws IOException {
        return openNonAsset(cookie, fileName, 2);
    }

    public final InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                long asset = openNonAssetNative(cookie, fileName, accessMode);
                if (asset != 0) {
                    AssetInputStream res = new AssetInputStream(this, asset, null);
                    incRefsLocked((long) res.hashCode());
                    return res;
                }
                throw new FileNotFoundException("Asset absolute file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final AssetFileDescriptor openNonAssetFd(String fileName) throws IOException {
        return openNonAssetFd(0, fileName);
    }

    public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                ParcelFileDescriptor pfd = openNonAssetFdNative(cookie, fileName, this.mOffsets);
                if (pfd != null) {
                    AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(pfd, this.mOffsets[0], this.mOffsets[1]);
                    return assetFileDescriptor;
                }
                throw new FileNotFoundException("Asset absolute file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final XmlResourceParser openXmlResourceParser(String fileName) throws IOException {
        return openXmlResourceParser(0, fileName);
    }

    public final XmlResourceParser openXmlResourceParser(int cookie, String fileName) throws IOException {
        XmlBlock block = openXmlBlockAsset(cookie, fileName);
        XmlResourceParser rp = block.newParser();
        block.close();
        return rp;
    }

    final XmlBlock openXmlBlockAsset(String fileName) throws IOException {
        return openXmlBlockAsset(0, fileName);
    }

    final XmlBlock openXmlBlockAsset(int cookie, String fileName) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                long xmlBlock = openXmlAssetNative(cookie, fileName);
                if (xmlBlock != 0) {
                    XmlBlock res = new XmlBlock(this, xmlBlock);
                    incRefsLocked((long) res.hashCode());
                    return res;
                }
                throw new FileNotFoundException("Asset XML file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    void xmlBlockGone(int id) {
        synchronized (this) {
            decRefsLocked((long) id);
        }
    }

    final long createTheme() {
        long res;
        synchronized (this) {
            if (this.mOpen) {
                res = newTheme();
                incRefsLocked(res);
            } else {
                throw new RuntimeException("Assetmanager has been closed");
            }
        }
        return res;
    }

    final void releaseTheme(long theme) {
        synchronized (this) {
            deleteTheme(theme);
            decRefsLocked(theme);
        }
    }

    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }

    public final int addAssetPath(String path) {
        return addAssetPathInternal(path, false);
    }

    public final int addAssetPathAsSharedLibrary(String path) {
        return addAssetPathInternal(path, true);
    }

    private final int addAssetPathInternal(String path, boolean appAsLib) {
        int res;
        synchronized (this) {
            res = addAssetPathNative(path, appAsLib);
            makeStringBlocks(this.mStringBlocks);
        }
        return res;
    }

    public final int addOverlayPath(String idmapPath) {
        int res;
        synchronized (this) {
            res = addOverlayPathNative(idmapPath);
            makeStringBlocks(this.mStringBlocks);
        }
        return res;
    }

    public final int[] addAssetPaths(String[] paths) {
        if (paths == null) {
            return null;
        }
        int[] cookies = new int[paths.length];
        for (int i = 0; i < paths.length; i++) {
            cookies[i] = addAssetPath(paths[i]);
        }
        return cookies;
    }

    public final void setConfiguration(int mcc, int mnc, String locale, int orientation, int touchscreen, int density, int keyboard, int keyboardHidden, int navigation, int screenWidth, int screenHeight, int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp, int screenLayout, int uiMode, int colorMode, int majorVersion, boolean ResFlag) {
        String tempLocale = locale;
        synchronized (sharedAsset) {
            if (!curLocale.equals(locale) && ResFlag) {
                setDbidConfig(locale);
                if (!Process.getCmdlineForPid(Process.myPid()).contains(Process.ZYGOTE_SOCKET)) {
                    makeSharedResource();
                }
            }
            if (Process.myUid() == 0) {
                setSharePemmison();
            }
            if (!sharedAsset.isEmpty()) {
                tempLocale = "zz-ZX";
            }
        }
        setConfiguration(mcc, mnc, tempLocale, orientation, touchscreen, density, keyboard, keyboardHidden, navigation, screenWidth, screenHeight, smallestScreenWidthDp, screenWidthDp, screenHeightDp, screenLayout, uiMode, colorMode, majorVersion);
    }

    private final void incRefsLocked(long id) {
        this.mNumRefs++;
    }

    private final void decRefsLocked(long id) {
        this.mNumRefs--;
        if (this.mNumRefs == 0) {
            destroy();
        }
    }
}
