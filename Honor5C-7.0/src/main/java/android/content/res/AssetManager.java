package android.content.res;

import android.net.ProxyInfo;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.preference.Preference;
import android.provider.DocumentsContract.Root;
import android.security.keymaster.KeymasterDefs;
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
    private static String curLocale = null;
    private static final String defautlResPath = "/data/share/SharedRes/";
    private static boolean hasSharedRes;
    private static boolean inited;
    private static final boolean localLOGV = false;
    private static boolean resValid;
    private static final Object sSync = null;
    static AssetManager sSystem;
    private static HashMap<Integer, AssetManager> sharedAsset;
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
            return len > 2147483647L ? Preference.DEFAULT_ORDER : (int) len;
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
            this.mMarkPos = AssetManager.this.seekAsset(this.mAsset, 0, AssetManager.STYLE_TYPE);
        }

        public final void reset() throws IOException {
            AssetManager.this.seekAsset(this.mAsset, this.mMarkPos, -1);
        }

        public final int read(byte[] b) throws IOException {
            return AssetManager.this.readAsset(this.mAsset, b, AssetManager.STYLE_TYPE, b.length);
        }

        public final int read(byte[] b, int off, int len) throws IOException {
            return AssetManager.this.readAsset(this.mAsset, b, off, len);
        }

        public final long skip(long n) throws IOException {
            long pos = AssetManager.this.seekAsset(this.mAsset, 0, AssetManager.STYLE_TYPE);
            if (pos + n > this.mLength) {
                n = this.mLength - pos;
            }
            if (n > 0) {
                AssetManager.this.seekAsset(this.mAsset, n, AssetManager.STYLE_TYPE);
            }
            return n;
        }

        protected void finalize() throws Throwable {
            close();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.AssetManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.res.AssetManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.AssetManager.<clinit>():void");
    }

    private final native int addAssetPathNative(String str, boolean z);

    static final native boolean applyStyle(long j, int i, int i2, long j2, int[] iArr, int[] iArr2, int[] iArr3);

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

    public final native void setConfiguration(int i, int i2, String str, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16);

    public AssetManager() {
        this.mValue = new TypedValue();
        this.mDbidValue = new TypedValue();
        this.mOffsets = new long[STYLE_ASSET_COOKIE];
        this.mStringBlocks = null;
        this.mNumRefs = STYLE_DATA;
        this.mOpen = true;
        synchronized (this) {
            init(DEBUG_REFS);
            ensureSystemAssets();
        }
    }

    public AssetManager(int notShared) {
        this.mValue = new TypedValue();
        this.mDbidValue = new TypedValue();
        this.mOffsets = new long[STYLE_ASSET_COOKIE];
        this.mStringBlocks = null;
        this.mNumRefs = STYLE_DATA;
        this.mOpen = true;
        synchronized (this) {
            init(DEBUG_REFS);
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
        this.mOffsets = new long[STYLE_ASSET_COOKIE];
        this.mStringBlocks = null;
        this.mNumRefs = STYLE_DATA;
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
        resValid = DEBUG_REFS;
        String lang = ProxyInfo.LOCAL_EXCL_LIST;
        String country = ProxyInfo.LOCAL_EXCL_LIST;
        String script = ProxyInfo.LOCAL_EXCL_LIST;
        if (curLocale.length() == STYLE_ASSET_COOKIE) {
            lang = curLocale;
        } else if (curLocale.length() == STYLE_DENSITY) {
            lang = curLocale.substring(STYLE_TYPE, STYLE_ASSET_COOKIE);
            country = curLocale.substring(STYLE_RESOURCE_ID, STYLE_DENSITY);
        } else if (curLocale.length() == 7) {
            lang = curLocale.substring(STYLE_TYPE, STYLE_ASSET_COOKIE);
            script = curLocale.substring(STYLE_RESOURCE_ID, 7);
        } else if (curLocale.length() == 10) {
            lang = curLocale.substring(STYLE_TYPE, STYLE_ASSET_COOKIE);
            script = curLocale.substring(STYLE_RESOURCE_ID, 7);
            country = curLocale.substring(8, 10);
        } else {
            lang = curLocale;
        }
        String locale = getParentLocale(lang + "_" + country);
        if (locale.length() == STYLE_DENSITY) {
            lang = locale.substring(STYLE_TYPE, STYLE_ASSET_COOKIE);
            country = locale.substring(STYLE_RESOURCE_ID, STYLE_DENSITY);
        }
        File resfolder = new File(defautlResPath + lang + "-" + country);
        File backupresfolder = new File(backupResPath + lang + "-" + country);
        if (!(resfolder.exists() || backupresfolder.exists())) {
            resfolder = new File(defautlResPath + lang + "-" + script);
            backupresfolder = new File(backupResPath + lang + "-" + script);
            if (!(resfolder.exists() || backupresfolder.exists())) {
                resfolder = new File(defautlResPath + lang);
                backupresfolder = new File(backupResPath + lang);
                if (!(resfolder.exists() || backupresfolder.exists())) {
                    if (resfolder.getParentFile().exists()) {
                        hasSharedRes = DEBUG_REFS;
                    }
                    return DEBUG_REFS;
                }
            }
        }
        File[] filearray = resfolder.listFiles();
        if (filearray == null) {
            filearray = backupresfolder.listFiles();
        }
        if (filearray != null) {
            int length = filearray.length;
            for (int i = STYLE_TYPE; i < length; i += STYLE_DATA) {
                File fi = filearray[i];
                if (fi.getName().contains(".lang")) {
                    int resID = Integer.parseInt(fi.getName().replace(".lang", ProxyInfo.LOCAL_EXCL_LIST));
                    AssetManager am = new AssetManager(STYLE_TYPE);
                    am.addAssetPath(fi.getPath());
                    sharedAsset.put(Integer.valueOf(resID), am);
                }
            }
        }
        if (sharedAsset.size() > 0) {
            hasSharedRes = true;
            return true;
        }
        hasSharedRes = DEBUG_REFS;
        return DEBUG_REFS;
    }

    public static boolean hasRes() {
        return resValid;
    }

    private static String getParentLocale(String locale) {
        parentlist = new String[58][];
        String[] strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_AR";
        strArr[STYLE_DATA] = "es_US";
        parentlist[STYLE_TYPE] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_BO";
        strArr[STYLE_DATA] = "es_US";
        parentlist[STYLE_DATA] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_CL";
        strArr[STYLE_DATA] = "es_US";
        parentlist[STYLE_ASSET_COOKIE] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_CO";
        strArr[STYLE_DATA] = "es_US";
        parentlist[STYLE_RESOURCE_ID] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_CR";
        strArr[STYLE_DATA] = "es_US";
        parentlist[STYLE_CHANGING_CONFIGURATIONS] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_CU";
        strArr[STYLE_DATA] = "es_US";
        parentlist[STYLE_DENSITY] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_DO";
        strArr[STYLE_DATA] = "es_US";
        parentlist[STYLE_NUM_ENTRIES] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_EC";
        strArr[STYLE_DATA] = "es_US";
        parentlist[7] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_GT";
        strArr[STYLE_DATA] = "es_US";
        parentlist[8] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_HN";
        strArr[STYLE_DATA] = "es_US";
        parentlist[9] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_MX";
        strArr[STYLE_DATA] = "es_US";
        parentlist[10] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_NI";
        strArr[STYLE_DATA] = "es_US";
        parentlist[11] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_PA";
        strArr[STYLE_DATA] = "es_US";
        parentlist[12] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_PE";
        strArr[STYLE_DATA] = "es_US";
        parentlist[13] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_PR";
        strArr[STYLE_DATA] = "es_US";
        parentlist[14] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_PY";
        strArr[STYLE_DATA] = "es_US";
        parentlist[15] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_SV";
        strArr[STYLE_DATA] = "es_US";
        parentlist[16] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_UY";
        strArr[STYLE_DATA] = "es_US";
        parentlist[17] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "es_VE";
        strArr[STYLE_DATA] = "es_US";
        parentlist[18] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "pt_AO";
        strArr[STYLE_DATA] = "pt_PT";
        parentlist[19] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "pt_CV";
        strArr[STYLE_DATA] = "pt_PT";
        parentlist[20] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "pt_GW";
        strArr[STYLE_DATA] = "pt_PT";
        parentlist[21] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "pt_MO";
        strArr[STYLE_DATA] = "pt_PT";
        parentlist[22] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "pt_MZ";
        strArr[STYLE_DATA] = "pt_PT";
        parentlist[23] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "pt_ST";
        strArr[STYLE_DATA] = "pt_PT";
        parentlist[24] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "pt_TL";
        strArr[STYLE_DATA] = "pt_PT";
        parentlist[25] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_AU";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[26] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_BE";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[27] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_DG";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[28] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_FK";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[29] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_GG";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[30] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_GI";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[31] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_HK";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[32] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_IE";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[33] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_IM";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[34] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_IN";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[35] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_IO";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[36] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_JE";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[37] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_MO";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[38] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_MT";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[39] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_NZ";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[40] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_PK";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[41] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_SG";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[42] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_SH";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[43] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_VG";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[44] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_BN";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[45] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_MY";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[46] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_PG";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[47] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_NR";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[48] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "en_WS";
        strArr[STYLE_DATA] = "en_GB";
        parentlist[49] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "zh_MO";
        strArr[STYLE_DATA] = "zh_HK";
        parentlist[50] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "zh_SG";
        strArr[STYLE_DATA] = "zh_CN";
        parentlist[51] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "ms_BN";
        strArr[STYLE_DATA] = "ms_MY";
        parentlist[52] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "ms_SG";
        strArr[STYLE_DATA] = "ms_MY";
        parentlist[53] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "uz_AF";
        strArr[STYLE_DATA] = "uz_UZ";
        parentlist[54] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "bo_IN";
        strArr[STYLE_DATA] = "bo_CN";
        parentlist[55] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "fil_PH";
        strArr[STYLE_DATA] = "tl_PH";
        parentlist[56] = strArr;
        strArr = new String[STYLE_ASSET_COOKIE];
        strArr[STYLE_TYPE] = "fil";
        strArr[STYLE_DATA] = "tl_PH";
        parentlist[57] = strArr;
        for (int i = STYLE_TYPE; i < parentlist.length; i += STYLE_DATA) {
            if (parentlist[i][STYLE_TYPE].equals(locale)) {
                return parentlist[i][STYLE_DATA];
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
                this.mOpen = DEBUG_REFS;
                decRefsLocked((long) hashCode());
            }
        }
    }

    final CharSequence getResourceText(int resId) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            if (getResourceValue(resId, STYLE_TYPE, outValue, true)) {
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
            if (block < 0) {
                return null;
            } else if (tmpValue.type == STYLE_RESOURCE_ID) {
                r2 = this.mStringBlocks[block].get(tmpValue.data);
                return r2;
            } else {
                r2 = tmpValue.coerceToString();
                return r2;
            }
        }
    }

    final CharSequence getResourceBagText(int resId, int bagEntryId) {
        synchronized (this) {
            TypedValue outValue = this.mValue;
            int block = loadResourceBagValue(hasRes() ? resId | KeymasterDefs.KM_BIGNUM : resId, bagEntryId, outValue, true);
            if (block < 0) {
                return null;
            } else if (outValue.type == STYLE_RESOURCE_ID) {
                CharSequence result = this.mStringBlocks[block].get(outValue.data);
                if (hasRes()) {
                    CharSequence rt = getTextForDBid(result);
                    if (rt != null) {
                        result = rt;
                    }
                }
                return result;
            } else {
                CharSequence coerceToString = outValue.coerceToString();
                return coerceToString;
            }
        }
    }

    final String[] getResourceStringArray(int resId) {
        int dbid_id;
        if (hasRes()) {
            dbid_id = resId | KeymasterDefs.KM_BIGNUM;
        } else {
            dbid_id = resId;
        }
        String[] retArray = getArrayStringResource(dbid_id);
        if (hasRes()) {
            for (int i = STYLE_TYPE; i < retArray.length; i += STYLE_DATA) {
                CharSequence rt = getTextForDBid(retArray[i]);
                if (rt != null) {
                    retArray[i] = rt.toString();
                }
            }
        }
        return retArray;
    }

    final boolean getResourceValue(int resId, int densityDpi, TypedValue outValue, boolean resolveRefs) {
        int block = loadResourceValue(resId, (short) densityDpi, outValue, resolveRefs);
        if (block < 0) {
            return DEBUG_REFS;
        }
        if (outValue.type == STYLE_RESOURCE_ID) {
            outValue.string = this.mStringBlocks[block].get(outValue.data);
            synchronized (this) {
                if (!inited || hasSharedRes) {
                    TypedValue tmpValue = this.mDbidValue;
                    int dbidblock = loadResourceValue(resId | KeymasterDefs.KM_BIGNUM, (short) densityDpi, tmpValue, resolveRefs);
                    if (dbidblock >= 0 && tmpValue.type == STYLE_RESOURCE_ID) {
                        CharSequence rt = getTextForDBid(this.mStringBlocks[dbidblock].get(tmpValue.data));
                        if (rt != null) {
                            outValue.string = rt;
                        }
                    }
                }
            }
        }
        return true;
    }

    final CharSequence[] getResourceTextArray(int resId) {
        int dbid_id;
        if (hasRes()) {
            dbid_id = resId | KeymasterDefs.KM_BIGNUM;
        } else {
            dbid_id = resId;
        }
        int[] rawInfoArray = getArrayStringInfo(dbid_id);
        int rawInfoArrayLen = rawInfoArray.length;
        CharSequence[] retArray = new CharSequence[(rawInfoArrayLen / STYLE_ASSET_COOKIE)];
        int i = STYLE_TYPE;
        int j = STYLE_TYPE;
        while (i < rawInfoArrayLen) {
            CharSequence charSequence;
            int block = rawInfoArray[i];
            int index = rawInfoArray[i + STYLE_DATA];
            if (index >= 0) {
                charSequence = this.mStringBlocks[block].get(index);
            } else {
                charSequence = null;
            }
            retArray[j] = charSequence;
            i += STYLE_ASSET_COOKIE;
            j += STYLE_DATA;
        }
        if (hasRes()) {
            for (i = STYLE_TYPE; i < retArray.length; i += STYLE_DATA) {
                CharSequence rt = getTextForDBid(retArray[i]);
                if (rt != null) {
                    retArray[i] = rt;
                }
            }
        }
        return retArray;
    }

    private CharSequence getTextForDBid(CharSequence dbid_cs) {
        if (dbid_cs == null || dbid_cs.length() <= STYLE_DENSITY || dbid_cs.charAt(STYLE_TYPE) != '[' || dbid_cs.charAt(STYLE_RESOURCE_ID) != '_') {
            return null;
        }
        String dbid_str = dbid_cs.toString();
        int dbid = Integer.parseInt(dbid_str.substring(STYLE_CHANGING_CONFIGURATIONS, dbid_str.indexOf(93)));
        if (dbid <= 0) {
            return null;
        }
        synchronized (sharedAsset) {
            if (!inited) {
                makeSharedResource();
                inited = true;
                if (sharedAsset.isEmpty()) {
                    return null;
                }
            }
            AssetManager am = (AssetManager) sharedAsset.get(Integer.valueOf(dbid / Root.FLAG_EMPTY));
            if (am != null) {
                CharSequence sharedResult = am.getResourceText((dbid % Root.FLAG_EMPTY) | 2130837504, true);
                if (sharedResult != null && sharedResult.length() > 0) {
                    setResValid();
                    return sharedResult;
                }
            }
            CharSequence subSequence = dbid_cs.subSequence(dbid_cs.toString().indexOf(93) + STYLE_ASSET_COOKIE, dbid_cs.length());
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
            first = new String[STYLE_TYPE];
        }
        if (second == null) {
            second = new String[STYLE_TYPE];
        }
        String[] result = (String[]) Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, STYLE_TYPE, result, first.length, second.length);
        return result;
    }

    private static void setResValid() {
        resValid = true;
    }

    final boolean getThemeValue(long theme, int resId, TypedValue outValue, boolean resolveRefs) {
        int block = loadThemeAttributeValue(theme, resId, outValue, resolveRefs);
        if (block < 0) {
            return DEBUG_REFS;
        }
        if (outValue.type == STYLE_RESOURCE_ID) {
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
        int seedNum = seed != null ? seed.length : STYLE_TYPE;
        int num = getStringBlockCount();
        this.mStringBlocks = new StringBlock[num];
        for (int i = STYLE_TYPE; i < num; i += STYLE_DATA) {
            if (i < seedNum) {
                this.mStringBlocks[i] = seed[i];
            } else {
                this.mStringBlocks[i] = new StringBlock(getNativeStringBlock(i), true);
            }
        }
    }

    final CharSequence getPooledStringForCookie(int cookie, int id) {
        return this.mStringBlocks[cookie - 1].get(id);
    }

    public final InputStream open(String fileName) throws IOException {
        return open(fileName, STYLE_ASSET_COOKIE);
    }

    public final InputStream open(String fileName, int accessMode) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                long asset = openAsset(fileName, accessMode);
                if (asset != 0) {
                    AssetInputStream res = new AssetInputStream(asset, null);
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
                    AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(pfd, this.mOffsets[STYLE_TYPE], this.mOffsets[STYLE_DATA]);
                    return assetFileDescriptor;
                }
                throw new FileNotFoundException("Asset file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final InputStream openNonAsset(String fileName) throws IOException {
        return openNonAsset(STYLE_TYPE, fileName, STYLE_ASSET_COOKIE);
    }

    public final InputStream openNonAsset(String fileName, int accessMode) throws IOException {
        return openNonAsset(STYLE_TYPE, fileName, accessMode);
    }

    public final InputStream openNonAsset(int cookie, String fileName) throws IOException {
        return openNonAsset(cookie, fileName, STYLE_ASSET_COOKIE);
    }

    public final InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                long asset = openNonAssetNative(cookie, fileName, accessMode);
                if (asset != 0) {
                    AssetInputStream res = new AssetInputStream(asset, null);
                    incRefsLocked((long) res.hashCode());
                    return res;
                }
                throw new FileNotFoundException("Asset absolute file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final AssetFileDescriptor openNonAssetFd(String fileName) throws IOException {
        return openNonAssetFd(STYLE_TYPE, fileName);
    }

    public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
        synchronized (this) {
            if (this.mOpen) {
                ParcelFileDescriptor pfd = openNonAssetFdNative(cookie, fileName, this.mOffsets);
                if (pfd != null) {
                    AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(pfd, this.mOffsets[STYLE_TYPE], this.mOffsets[STYLE_DATA]);
                    return assetFileDescriptor;
                }
                throw new FileNotFoundException("Asset absolute file: " + fileName);
            }
            throw new RuntimeException("Assetmanager has been closed");
        }
    }

    public final XmlResourceParser openXmlResourceParser(String fileName) throws IOException {
        return openXmlResourceParser(STYLE_TYPE, fileName);
    }

    public final XmlResourceParser openXmlResourceParser(int cookie, String fileName) throws IOException {
        XmlBlock block = openXmlBlockAsset(cookie, fileName);
        XmlResourceParser rp = block.newParser();
        block.close();
        return rp;
    }

    final XmlBlock openXmlBlockAsset(String fileName) throws IOException {
        return openXmlBlockAsset(STYLE_TYPE, fileName);
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
        return addAssetPathInternal(path, DEBUG_REFS);
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
        for (int i = STYLE_TYPE; i < paths.length; i += STYLE_DATA) {
            cookies[i] = addAssetPath(paths[i]);
        }
        return cookies;
    }

    public final void setConfiguration(int mcc, int mnc, String locale, int orientation, int touchscreen, int density, int keyboard, int keyboardHidden, int navigation, int screenWidth, int screenHeight, int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp, int screenLayout, int uiMode, int majorVersion, boolean ResFlag) {
        String shareLocale = locale.replaceAll("fil", "tl");
        synchronized (sharedAsset) {
            if (!curLocale.equals(shareLocale) && ResFlag) {
                setDbidConfig(shareLocale);
                if (!Process.getCmdlineForPid(Process.myPid()).contains(Process.ZYGOTE_SOCKET)) {
                    makeSharedResource();
                }
            }
            if (Process.myUid() == 0) {
                setSharePemmison();
            }
            if (!sharedAsset.isEmpty()) {
                for (Integer key : sharedAsset.keySet()) {
                    ((AssetManager) sharedAsset.get(key)).setConfiguration(mcc, mnc, locale, orientation, touchscreen, density, keyboard, keyboardHidden, navigation, screenWidth, screenHeight, smallestScreenWidthDp, screenWidthDp, screenHeightDp, screenLayout, uiMode, majorVersion);
                }
            }
        }
        setConfiguration(mcc, mnc, locale, orientation, touchscreen, density, keyboard, keyboardHidden, navigation, screenWidth, screenHeight, smallestScreenWidthDp, screenWidthDp, screenHeightDp, screenLayout, uiMode, majorVersion);
    }

    private final void incRefsLocked(long id) {
        this.mNumRefs += STYLE_DATA;
    }

    private final void decRefsLocked(long id) {
        this.mNumRefs--;
        if (this.mNumRefs == 0) {
            destroy();
        }
    }
}
