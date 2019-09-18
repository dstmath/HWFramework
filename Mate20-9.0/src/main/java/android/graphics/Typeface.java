package android.graphics;

import android.content.res.AssetManager;
import android.content.res.FontResourcesParser;
import android.graphics.fonts.FontVariationAxis;
import android.media.tv.TvContract;
import android.net.Uri;
import android.provider.FontRequest;
import android.provider.FontsContract;
import android.text.FontConfig;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.LruCache;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import libcore.util.NativeAllocationRegistry;
import org.xmlpull.v1.XmlPullParserException;

public class Typeface {
    public static final int BOLD = 1;
    public static final int BOLD_ITALIC = 3;
    public static final Typeface DEFAULT;
    public static final Typeface DEFAULT_BOLD;
    private static final String DEFAULT_FAMILY = "sans-serif";
    private static final int[] EMPTY_AXES = new int[0];
    public static final int HW_STYLE_ITALIC = 0;
    public static final int HW_STYLE_NORMAL = 0;
    public static final int ITALIC = 2;
    public static final int MAX_WEIGHT = 1000;
    public static final Typeface MONOSPACE = create("monospace", 0);
    public static final int NORMAL = 0;
    public static final int RESOLVE_BY_FONT_TABLE = -1;
    public static final Typeface SANS_SERIF = create(DEFAULT_FAMILY, 0);
    public static final Typeface SERIF = create("serif", 0);
    private static final int STYLE_ITALIC = 1;
    public static final int STYLE_MASK = 3;
    private static final int STYLE_NORMAL = 0;
    private static String TAG = "Typeface";
    static FontFamily[] familiestemp;
    static Typeface sDefaultTypeface;
    static Typeface[] sDefaults;
    /* access modifiers changed from: private */
    public static final Object sDynamicCacheLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy("sDynamicCacheLock")
    public static final LruCache<String, Typeface> sDynamicTypefaceCache = new LruCache<>(16);
    private static final NativeAllocationRegistry sRegistry;
    private static final Object sStyledCacheLock = new Object();
    @GuardedBy("sStyledCacheLock")
    private static final LongSparseArray<SparseArray<Typeface>> sStyledTypefaceCache = new LongSparseArray<>(3);
    static final Map<String, FontFamily[]> sSystemFallbackMap;
    static final Map<String, Typeface> sSystemFontMap;
    private static final Object sWeightCacheLock = new Object();
    @GuardedBy("sWeightCacheLock")
    private static final LongSparseArray<SparseArray<Typeface>> sWeightTypefaceCache = new LongSparseArray<>(3);
    /* access modifiers changed from: private */
    public int mStyle = 0;
    private int[] mSupportedAxes;
    /* access modifiers changed from: private */
    public int mWeight = 0;
    public long native_instance;

    public static final class Builder {
        public static final int BOLD_WEIGHT = 700;
        public static final int NORMAL_WEIGHT = 400;
        private AssetManager mAssetManager;
        private FontVariationAxis[] mAxes;
        private String mFallbackFamilyName;
        private FileDescriptor mFd;
        private Map<Uri, ByteBuffer> mFontBuffers;
        private FontsContract.FontInfo[] mFonts;
        private int mItalic = -1;
        private String mPath;
        private int mTtcIndex;
        private int mWeight = -1;

        public Builder(File path) {
            this.mPath = path.getAbsolutePath();
        }

        public Builder(FileDescriptor fd) {
            this.mFd = fd;
        }

        public Builder(String path) {
            this.mPath = path;
        }

        public Builder(AssetManager assetManager, String path) {
            this.mAssetManager = (AssetManager) Preconditions.checkNotNull(assetManager);
            this.mPath = (String) Preconditions.checkStringNotEmpty(path);
        }

        public Builder(FontsContract.FontInfo[] fonts, Map<Uri, ByteBuffer> buffers) {
            this.mFonts = fonts;
            this.mFontBuffers = buffers;
        }

        public Builder setWeight(int weight) {
            this.mWeight = weight;
            return this;
        }

        public Builder setItalic(boolean italic) {
            this.mItalic = italic;
            return this;
        }

        public Builder setTtcIndex(int ttcIndex) {
            if (this.mFonts == null) {
                this.mTtcIndex = ttcIndex;
                return this;
            }
            throw new IllegalArgumentException("TTC index can not be specified for FontResult source.");
        }

        public Builder setFontVariationSettings(String variationSettings) {
            if (this.mFonts != null) {
                throw new IllegalArgumentException("Font variation settings can not be specified for FontResult source.");
            } else if (this.mAxes == null) {
                this.mAxes = FontVariationAxis.fromFontVariationSettings(variationSettings);
                return this;
            } else {
                throw new IllegalStateException("Font variation settings are already set.");
            }
        }

        public Builder setFontVariationSettings(FontVariationAxis[] axes) {
            if (this.mFonts != null) {
                throw new IllegalArgumentException("Font variation settings can not be specified for FontResult source.");
            } else if (this.mAxes == null) {
                this.mAxes = axes;
                return this;
            } else {
                throw new IllegalStateException("Font variation settings are already set.");
            }
        }

        public Builder setFallback(String familyName) {
            this.mFallbackFamilyName = familyName;
            return this;
        }

        /* access modifiers changed from: private */
        public static String createAssetUid(AssetManager mgr, String path, int ttcIndex, FontVariationAxis[] axes, int weight, int italic, String fallback) {
            SparseArray<String> pkgs = mgr.getAssignedPackageIdentifiers();
            StringBuilder builder = new StringBuilder();
            int size = pkgs.size();
            for (int i = 0; i < size; i++) {
                builder.append(pkgs.valueAt(i));
                builder.append("-");
            }
            builder.append(path);
            builder.append("-");
            builder.append(Integer.toString(ttcIndex));
            builder.append("-");
            builder.append(Integer.toString(weight));
            builder.append("-");
            builder.append(Integer.toString(italic));
            builder.append("--");
            builder.append(fallback);
            builder.append("--");
            if (axes != null) {
                for (FontVariationAxis axis : axes) {
                    builder.append(axis.getTag());
                    builder.append("-");
                    builder.append(Float.toString(axis.getStyleValue()));
                }
            }
            return builder.toString();
        }

        private Typeface resolveFallbackTypeface() {
            if (this.mFallbackFamilyName == null) {
                return null;
            }
            Typeface base = Typeface.sSystemFontMap.get(this.mFallbackFamilyName);
            if (base == null) {
                base = Typeface.sDefaultTypeface;
            }
            if (this.mWeight == -1 && this.mItalic == -1) {
                return base;
            }
            int weight = this.mWeight == -1 ? base.mWeight : this.mWeight;
            boolean italic = false;
            if (this.mItalic != -1 ? this.mItalic == 1 : (base.mStyle & 2) != 0) {
                italic = true;
            }
            return Typeface.createWeightStyle(base, weight, italic);
        }

        public Typeface build() {
            FileInputStream fis;
            Throwable th;
            Throwable th2;
            Throwable th3;
            if (this.mFd != null) {
                try {
                    fis = new FileInputStream(this.mFd);
                    FileChannel channel = fis.getChannel();
                    ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                    FontFamily fontFamily = new FontFamily();
                    if (!fontFamily.addFontFromBuffer(buffer, this.mTtcIndex, this.mAxes, this.mWeight, this.mItalic)) {
                        fontFamily.abortCreation();
                        Typeface resolveFallbackTypeface = resolveFallbackTypeface();
                        fis.close();
                        return resolveFallbackTypeface;
                    } else if (!fontFamily.freeze()) {
                        Typeface resolveFallbackTypeface2 = resolveFallbackTypeface();
                        fis.close();
                        return resolveFallbackTypeface2;
                    } else {
                        Typeface access$400 = Typeface.createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, this.mFallbackFamilyName, this.mWeight, this.mItalic);
                        fis.close();
                        return access$400;
                    }
                } catch (IOException e) {
                    return resolveFallbackTypeface();
                } catch (Throwable th4) {
                    th2.addSuppressed(th4);
                }
            } else if (this.mAssetManager != null) {
                String key = createAssetUid(this.mAssetManager, this.mPath, this.mTtcIndex, this.mAxes, this.mWeight, this.mItalic, this.mFallbackFamilyName);
                synchronized (Typeface.sDynamicCacheLock) {
                    Typeface typeface = (Typeface) Typeface.sDynamicTypefaceCache.get(key);
                    if (typeface != null) {
                        return typeface;
                    }
                    FontFamily fontFamily2 = new FontFamily();
                    if (!fontFamily2.addFontFromAssetManager(this.mAssetManager, this.mPath, this.mTtcIndex, true, this.mTtcIndex, this.mWeight, this.mItalic, this.mAxes)) {
                        fontFamily2.abortCreation();
                        Typeface resolveFallbackTypeface3 = resolveFallbackTypeface();
                        return resolveFallbackTypeface3;
                    } else if (!fontFamily2.freeze()) {
                        Typeface resolveFallbackTypeface4 = resolveFallbackTypeface();
                        return resolveFallbackTypeface4;
                    } else {
                        Typeface typeface2 = Typeface.createFromFamiliesWithDefault(new FontFamily[]{fontFamily2}, this.mFallbackFamilyName, this.mWeight, this.mItalic);
                        Typeface.sDynamicTypefaceCache.put(key, typeface2);
                        return typeface2;
                    }
                }
            } else if (this.mPath != null) {
                FontFamily fontFamily3 = new FontFamily();
                if (!fontFamily3.addFont(this.mPath, this.mTtcIndex, this.mAxes, this.mWeight, this.mItalic)) {
                    fontFamily3.abortCreation();
                    return resolveFallbackTypeface();
                } else if (!fontFamily3.freeze()) {
                    return resolveFallbackTypeface();
                } else {
                    return Typeface.createFromFamiliesWithDefault(new FontFamily[]{fontFamily3}, this.mFallbackFamilyName, this.mWeight, this.mItalic);
                }
            } else if (this.mFonts != null) {
                FontFamily fontFamily4 = new FontFamily();
                boolean atLeastOneFont = false;
                for (FontsContract.FontInfo font : this.mFonts) {
                    ByteBuffer fontBuffer = this.mFontBuffers.get(font.getUri());
                    if (fontBuffer != null) {
                        if (!fontFamily4.addFontFromBuffer(fontBuffer, font.getTtcIndex(), font.getAxes(), font.getWeight(), font.isItalic() ? 1 : 0)) {
                            fontFamily4.abortCreation();
                            return null;
                        }
                        atLeastOneFont = true;
                    }
                }
                if (!atLeastOneFont) {
                    fontFamily4.abortCreation();
                    return null;
                }
                fontFamily4.freeze();
                return Typeface.createFromFamiliesWithDefault(new FontFamily[]{fontFamily4}, this.mFallbackFamilyName, this.mWeight, this.mItalic);
            } else {
                throw new IllegalArgumentException("No source was set.");
            }
            throw th3;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {
    }

    private static native long nativeCreateFromArray(long[] jArr, int i, int i2);

    private static native long nativeCreateFromTypeface(long j, int i);

    private static native long nativeCreateFromTypefaceWithExactStyle(long j, int i, boolean z);

    private static native long nativeCreateFromTypefaceWithVariation(long j, List<FontVariationAxis> list);

    private static native long nativeCreateWeightAlias(long j, int i);

    private static native long nativeGetReleaseFunc();

    private static native int nativeGetStyle(long j);

    private static native int[] nativeGetSupportedAxes(long j);

    private static native int nativeGetWeight(long j);

    private static native void nativeSetDefault(long j);

    static {
        NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(Typeface.class.getClassLoader(), nativeGetReleaseFunc(), 64);
        sRegistry = nativeAllocationRegistry;
        ArrayMap<String, Typeface> systemFontMap = new ArrayMap<>();
        ArrayMap<String, FontFamily[]> systemFallbackMap = new ArrayMap<>();
        buildSystemFallback("/system/etc/fonts.xml", "/system/fonts/", systemFontMap, systemFallbackMap);
        sSystemFontMap = Collections.unmodifiableMap(systemFontMap);
        sSystemFallbackMap = Collections.unmodifiableMap(systemFallbackMap);
        setDefault(sSystemFontMap.get(DEFAULT_FAMILY));
        String str = null;
        DEFAULT = create(str, 0);
        DEFAULT_BOLD = create(str, 1);
        sDefaults = new Typeface[]{DEFAULT, DEFAULT_BOLD, create(str, 2), create(str, 3)};
    }

    private static void setDefault(Typeface t) {
        sDefaultTypeface = t;
        nativeSetDefault(t.native_instance);
    }

    public int getWeight() {
        return this.mWeight;
    }

    public int getStyle() {
        return this.mStyle;
    }

    public final boolean isBold() {
        return (this.mStyle & 1) != 0;
    }

    public final boolean isItalic() {
        return (this.mStyle & 2) != 0;
    }

    public static Typeface createFromResources(AssetManager mgr, String path, int cookie) {
        synchronized (sDynamicCacheLock) {
            String key = Builder.createAssetUid(mgr, path, 0, null, -1, -1, DEFAULT_FAMILY);
            Typeface typeface = sDynamicTypefaceCache.get(key);
            if (typeface != null) {
                return typeface;
            }
            FontFamily fontFamily = new FontFamily();
            if (!fontFamily.addFontFromAssetManager(mgr, path, cookie, false, 0, -1, -1, null)) {
                return null;
            }
            if (!fontFamily.freeze()) {
                return null;
            }
            Typeface typeface2 = createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, DEFAULT_FAMILY, -1, -1);
            sDynamicTypefaceCache.put(key, typeface2);
            return typeface2;
        }
    }

    public static Typeface createFromResources(FontResourcesParser.FamilyResourceEntry entry, AssetManager mgr, String path) {
        FontResourcesParser.FamilyResourceEntry familyResourceEntry = entry;
        if (familyResourceEntry instanceof FontResourcesParser.ProviderResourceEntry) {
            FontResourcesParser.ProviderResourceEntry providerEntry = (FontResourcesParser.ProviderResourceEntry) familyResourceEntry;
            List<List<String>> givenCerts = providerEntry.getCerts();
            List<List<byte[]>> certs = new ArrayList<>();
            if (givenCerts != null) {
                for (int i = 0; i < givenCerts.size(); i++) {
                    List<String> certSet = givenCerts.get(i);
                    List<byte[]> byteArraySet = new ArrayList<>();
                    for (int j = 0; j < certSet.size(); j++) {
                        byteArraySet.add(Base64.decode(certSet.get(j), 0));
                    }
                    certs.add(byteArraySet);
                }
            }
            Typeface typeface = FontsContract.getFontSync(new FontRequest(providerEntry.getAuthority(), providerEntry.getPackage(), providerEntry.getQuery(), certs));
            return typeface == null ? DEFAULT : typeface;
        }
        Typeface typeface2 = findFromCache(mgr, path);
        if (typeface2 != null) {
            return typeface2;
        }
        FontFamily fontFamily = new FontFamily();
        FontResourcesParser.FontFileResourceEntry[] entries = ((FontResourcesParser.FontFamilyFilesResourceEntry) familyResourceEntry).getEntries();
        int length = entries.length;
        int i2 = 0;
        while (i2 < length) {
            FontResourcesParser.FontFileResourceEntry fontFile = entries[i2];
            String fileName = fontFile.getFileName();
            int ttcIndex = fontFile.getTtcIndex();
            int weight = fontFile.getWeight();
            int italic = fontFile.getItalic();
            FontVariationAxis[] fromFontVariationSettings = FontVariationAxis.fromFontVariationSettings(fontFile.getVariationSettings());
            FontResourcesParser.FontFileResourceEntry fontFileResourceEntry = fontFile;
            int i3 = italic;
            int i4 = i2;
            if (!fontFamily.addFontFromAssetManager(mgr, fileName, 0, false, ttcIndex, weight, i3, fromFontVariationSettings)) {
                return null;
            }
            i2 = i4 + 1;
        }
        if (!fontFamily.freeze()) {
            return null;
        }
        Typeface typeface3 = createFromFamiliesWithDefault(new FontFamily[]{fontFamily}, DEFAULT_FAMILY, -1, -1);
        synchronized (sDynamicCacheLock) {
            sDynamicTypefaceCache.put(Builder.createAssetUid(mgr, path, 0, null, -1, -1, DEFAULT_FAMILY), typeface3);
        }
        return typeface3;
    }

    public static Typeface findFromCache(AssetManager mgr, String path) {
        synchronized (sDynamicCacheLock) {
            Typeface typeface = sDynamicTypefaceCache.get(Builder.createAssetUid(mgr, path, 0, null, -1, -1, DEFAULT_FAMILY));
            if (typeface != null) {
                return typeface;
            }
            return null;
        }
    }

    public static Typeface create(String familyName, int style) {
        return create(sSystemFontMap.get(familyName), style);
    }

    public static Typeface create(Typeface family, int style) {
        if ((style & -4) != 0) {
            style = 0;
        }
        if (family == null) {
            family = sDefaultTypeface;
        }
        if (family.mStyle == style) {
            return family;
        }
        long ni = family.native_instance;
        synchronized (sStyledCacheLock) {
            SparseArray<Typeface> styles = sStyledTypefaceCache.get(ni);
            if (styles == null) {
                styles = new SparseArray<>(4);
                sStyledTypefaceCache.put(ni, styles);
            } else {
                Typeface typeface = styles.get(style);
                if (typeface != null) {
                    return typeface;
                }
            }
            Typeface typeface2 = new Typeface(nativeCreateFromTypeface(ni, style));
            styles.put(style, typeface2);
            return typeface2;
        }
    }

    public static Typeface create(Typeface family, int weight, boolean italic) {
        Preconditions.checkArgumentInRange(weight, 0, 1000, TvContract.PreviewPrograms.COLUMN_WEIGHT);
        if (family == null) {
            family = sDefaultTypeface;
        }
        return createWeightStyle(family, weight, italic);
    }

    /* access modifiers changed from: private */
    public static Typeface createWeightStyle(Typeface base, int weight, boolean italic) {
        int key = (weight << 1) | italic;
        synchronized (sWeightCacheLock) {
            SparseArray<Typeface> innerCache = sWeightTypefaceCache.get(base.native_instance);
            if (innerCache == null) {
                innerCache = new SparseArray<>(4);
                sWeightTypefaceCache.put(base.native_instance, innerCache);
            } else {
                Typeface typeface = innerCache.get(key);
                if (typeface != null) {
                    return typeface;
                }
            }
            Typeface typeface2 = new Typeface(nativeCreateFromTypefaceWithExactStyle(base.native_instance, weight, italic));
            innerCache.put((int) key, typeface2);
            return typeface2;
        }
    }

    public static Typeface createFromTypefaceWithVariation(Typeface family, List<FontVariationAxis> axes) {
        return new Typeface(nativeCreateFromTypefaceWithVariation(family == null ? 0 : family.native_instance, axes));
    }

    public static Typeface defaultFromStyle(int style) {
        return sDefaults[style];
    }

    public static Typeface createFromAsset(AssetManager mgr, String path) {
        Preconditions.checkNotNull(path);
        Preconditions.checkNotNull(mgr);
        Typeface typeface = new Builder(mgr, path).build();
        if (typeface != null) {
            return typeface;
        }
        try {
            InputStream inputStream = mgr.open(path);
            if (inputStream != null) {
                inputStream.close();
            }
            return DEFAULT;
        } catch (IOException e) {
            throw new RuntimeException("Font asset not found " + path);
        }
    }

    private static String createProviderUid(String authority, String query) {
        return "provider:" + authority + "-" + query;
    }

    public static Typeface createFromFile(File file) {
        Typeface typeface = new Builder(file).build();
        if (typeface != null) {
            return typeface;
        }
        if (file.exists()) {
            return DEFAULT;
        }
        throw new RuntimeException("Font asset not found " + file.getAbsolutePath());
    }

    public static Typeface createFromFile(String path) {
        Preconditions.checkNotNull(path);
        return createFromFile(new File(path));
    }

    private static Typeface createFromFamilies(FontFamily[] families) {
        long[] ptrArray = new long[families.length];
        for (int i = 0; i < families.length; i++) {
            ptrArray[i] = families[i].mNativePtr;
        }
        return new Typeface(nativeCreateFromArray(ptrArray, -1, -1));
    }

    private static Typeface createFromFamiliesWithDefault(FontFamily[] families, int weight, int italic) {
        return createFromFamiliesWithDefault(families, DEFAULT_FAMILY, weight, italic);
    }

    /* access modifiers changed from: private */
    public static Typeface createFromFamiliesWithDefault(FontFamily[] families, String fallbackName, int weight, int italic) {
        FontFamily[] tmpfallback;
        FontFamily[] fallback = sSystemFallbackMap.get(fallbackName);
        if (fallback == null) {
            fallback = sSystemFallbackMap.get(DEFAULT_FAMILY);
        }
        boolean hashook = false;
        int k = 0;
        while (true) {
            if (k >= fallback.length) {
                break;
            } else if (fallback[k].mIsHook) {
                hashook = true;
                break;
            } else {
                k++;
            }
        }
        if (hashook) {
            tmpfallback = new FontFamily[(fallback.length - 1)];
        } else {
            tmpfallback = new FontFamily[fallback.length];
        }
        int index = 0;
        for (int j = 0; j < fallback.length; j++) {
            if (!fallback[j].mIsHook) {
                tmpfallback[index] = fallback[j];
                index++;
            }
        }
        long[] ptrArray = new long[(families.length + tmpfallback.length)];
        for (int i = 0; i < families.length; i++) {
            ptrArray[i] = families[i].mNativePtr;
        }
        for (int i2 = 0; i2 < tmpfallback.length; i2++) {
            ptrArray[families.length + i2] = tmpfallback[i2].mNativePtr;
        }
        return new Typeface(nativeCreateFromArray(ptrArray, weight, italic));
    }

    private Typeface(long ni) {
        if (ni != 0) {
            this.native_instance = ni;
            sRegistry.registerNativeAllocation(this, this.native_instance);
            this.mStyle = nativeGetStyle(ni);
            this.mWeight = nativeGetWeight(ni);
            return;
        }
        throw new RuntimeException("native typeface cannot be made");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        r9 = r3;
        r3 = r2;
        r2 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001b, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        r3 = null;
     */
    private static ByteBuffer mmap(String fullPath) {
        Throwable th;
        Throwable th2;
        try {
            FileInputStream file = new FileInputStream(fullPath);
            FileChannel fileChannel = file.getChannel();
            MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            file.close();
            return map;
            throw th2;
            if (th != null) {
                try {
                    file.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
            } else {
                file.close();
            }
            throw th2;
        } catch (IOException e) {
            Log.e(TAG, "Error mapping font file " + fullPath);
            return null;
        }
    }

    private static FontFamily createFontFamily(String familyName, List<FontConfig.Font> fonts, String[] languageTags, int variant, Map<String, ByteBuffer> cache, String fontDir) {
        Map<String, ByteBuffer> map = cache;
        FontFamily family = new FontFamily(languageTags, variant);
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= fonts.size()) {
                break;
            }
            FontConfig.Font font = fonts.get(i2);
            String fullPath = fontDir + font.getFontName();
            if (fullPath.contains(HwTypefaceUtil.HW_DEFAULT_FONT_NAME) && HwTypefaceUtil.HW_CUSTOM_FONT_NAME != null) {
                String tmppath = HwTypefaceUtil.HW_CUSTOM_FONT_PATH + HwTypefaceUtil.HW_CUSTOM_FONT_NAME;
                if (new File(tmppath).exists()) {
                    fullPath = tmppath;
                    Log.i(TAG, "custom font, name = " + fullPath);
                }
            }
            String fullPath2 = fullPath;
            if (fullPath2.contains(HwTypefaceUtil.HWFONT_NAME)) {
                family.mIsHook = true;
            }
            ByteBuffer buffer = map.get(fullPath2);
            if (buffer == null) {
                if (!map.containsKey(fullPath2)) {
                    buffer = mmap(fullPath2);
                    map.put(fullPath2, buffer);
                    if (buffer == null) {
                    }
                }
                i = i2 + 1;
            }
            if (!family.addFontFromBuffer(buffer, font.getTtcIndex(), font.getAxes(), font.getWeight(), font.isItalic() ? 1 : 0)) {
                Log.e(TAG, "Error creating font " + fullPath2 + "#" + font.getTtcIndex());
            }
            i = i2 + 1;
        }
        List<FontConfig.Font> list = fonts;
        String str = fontDir;
        if (!family.freeze()) {
            Log.e(TAG, "Unable to load Family: " + familyName + " : " + Arrays.toString(languageTags));
            return null;
        }
        String str2 = familyName;
        return family;
    }

    private static void pushFamilyToFallback(FontConfig.Family xmlFamily, ArrayMap<String, ArrayList<FontFamily>> fallbackMap, Map<String, ByteBuffer> cache, String fontDir) {
        ArrayMap<String, ArrayList<FontFamily>> arrayMap = fallbackMap;
        String[] languageTags = xmlFamily.getLanguages();
        int variant = xmlFamily.getVariant();
        ArrayList<FontConfig.Font> defaultFonts = new ArrayList<>();
        ArrayMap arrayMap2 = new ArrayMap();
        for (FontConfig.Font font : xmlFamily.getFonts()) {
            String fallbackName = font.getFallbackFor();
            if (fallbackName == null) {
                defaultFonts.add(font);
            } else {
                ArrayList<FontConfig.Font> fallback = (ArrayList) arrayMap2.get(fallbackName);
                if (fallback == null) {
                    fallback = new ArrayList<>();
                    arrayMap2.put(fallbackName, fallback);
                }
                fallback.add(font);
            }
        }
        FontFamily defaultFamily = defaultFonts.isEmpty() ? null : createFontFamily(xmlFamily.getName(), defaultFonts, languageTags, variant, cache, fontDir);
        for (int i = 0; i < arrayMap.size(); i++) {
            ArrayList<FontConfig.Font> fallback2 = (ArrayList) arrayMap2.get(arrayMap.keyAt(i));
            if (fallback2 != null) {
                FontFamily family = createFontFamily(xmlFamily.getName(), fallback2, languageTags, variant, cache, fontDir);
                if (family != null) {
                    arrayMap.valueAt(i).add(family);
                } else if (defaultFamily != null) {
                    arrayMap.valueAt(i).add(defaultFamily);
                }
            } else if (defaultFamily != null) {
                arrayMap.valueAt(i).add(defaultFamily);
            }
        }
    }

    @VisibleForTesting
    public static void buildSystemFallback(String xmlPath, String fontDir, ArrayMap<String, Typeface> fontMap, ArrayMap<String, FontFamily[]> fallbackMap) {
        HashMap<String, ByteBuffer> bufferCache;
        FontConfig fontConfig;
        ArrayMap<String, Typeface> arrayMap = fontMap;
        try {
            FontConfig fontConfig2 = FontListParser.parse(new FileInputStream(xmlPath));
            HashMap<String, ByteBuffer> bufferCache2 = new HashMap<>();
            FontConfig.Family[] xmlFamilies = fontConfig2.getFamilies();
            ArrayMap arrayMap2 = new ArrayMap();
            for (FontConfig.Family xmlFamily : xmlFamilies) {
                String familyName = xmlFamily.getName();
                if (familyName != null) {
                    String familyName2 = familyName;
                    FontConfig.Family family = xmlFamily;
                    FontFamily family2 = createFontFamily(xmlFamily.getName(), Arrays.asList(xmlFamily.getFonts()), xmlFamily.getLanguages(), xmlFamily.getVariant(), bufferCache2, fontDir);
                    if (family2 != null) {
                        ArrayList<FontFamily> fallback = new ArrayList<>();
                        fallback.add(family2);
                        arrayMap2.put(familyName2, fallback);
                    }
                }
            }
            for (int i = 0; i < xmlFamilies.length; i++) {
                FontConfig.Family xmlFamily2 = xmlFamilies[i];
                if (i != 0) {
                    if (xmlFamily2.getName() != null) {
                        String str = fontDir;
                    }
                }
                pushFamilyToFallback(xmlFamily2, arrayMap2, bufferCache2, fontDir);
            }
            String str2 = fontDir;
            int i2 = 0;
            while (i2 < arrayMap2.size()) {
                String fallbackName = (String) arrayMap2.keyAt(i2);
                List<FontFamily> familyList = (List) arrayMap2.valueAt(i2);
                FontFamily[] families = (FontFamily[]) familyList.toArray(new FontFamily[familyList.size()]);
                familiestemp = (FontFamily[]) familyList.toArray(new FontFamily[familyList.size()]);
                try {
                    fallbackMap.put(fallbackName, families);
                    long[] ptrArray = new long[families.length];
                    int j = 0;
                    while (j < families.length) {
                        ptrArray[j] = families[j].mNativePtr;
                        j++;
                        familyList = familyList;
                        String str3 = fontDir;
                    }
                    arrayMap.put(fallbackName, new Typeface(nativeCreateFromArray(ptrArray, -1, -1)));
                    i2++;
                    String str4 = fontDir;
                } catch (RuntimeException e) {
                    e = e;
                    Log.w(TAG, "Didn't create default family (most likely, non-Minikin build)", e);
                } catch (FileNotFoundException e2) {
                    e = e2;
                    Log.e(TAG, "Error opening " + r1, e);
                } catch (IOException e3) {
                    e = e3;
                    Log.e(TAG, "Error reading " + r1, e);
                } catch (XmlPullParserException e4) {
                    e = e4;
                    Log.e(TAG, "XML parse exception for " + r1, e);
                }
            }
            ArrayMap<String, FontFamily[]> arrayMap3 = fallbackMap;
            FontConfig.Alias[] aliases = fontConfig2.getAliases();
            int length = aliases.length;
            int i3 = 0;
            while (i3 < length) {
                FontConfig.Alias alias = aliases[i3];
                Typeface base = arrayMap.get(alias.getToName());
                Typeface newFace = base;
                int weight = alias.getWeight();
                if (weight != 400) {
                    fontConfig = fontConfig2;
                    bufferCache = bufferCache2;
                    newFace = new Typeface(nativeCreateWeightAlias(base.native_instance, weight));
                } else {
                    fontConfig = fontConfig2;
                    bufferCache = bufferCache2;
                }
                arrayMap.put(alias.getName(), newFace);
                i3++;
                fontConfig2 = fontConfig;
                bufferCache2 = bufferCache;
            }
        } catch (RuntimeException e5) {
            e = e5;
            ArrayMap<String, FontFamily[]> arrayMap4 = fallbackMap;
            Log.w(TAG, "Didn't create default family (most likely, non-Minikin build)", e);
        } catch (FileNotFoundException e6) {
            e = e6;
            ArrayMap<String, FontFamily[]> arrayMap5 = fallbackMap;
            Log.e(TAG, "Error opening " + r1, e);
        } catch (IOException e7) {
            e = e7;
            ArrayMap<String, FontFamily[]> arrayMap6 = fallbackMap;
            Log.e(TAG, "Error reading " + r1, e);
        } catch (XmlPullParserException e8) {
            e = e8;
            ArrayMap<String, FontFamily[]> arrayMap7 = fallbackMap;
            Log.e(TAG, "XML parse exception for " + r1, e);
        }
    }

    public static synchronized void loadSystemFonts() {
        synchronized (Typeface.class) {
            for (int i = 0; i < familiestemp.length; i++) {
                if (familiestemp[i].mIsHook) {
                    HwTypefaceUtil.updateFont(familiestemp[i]);
                }
            }
        }
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Typeface typeface = (Typeface) o;
        if (!(this.mStyle == typeface.mStyle && this.native_instance == typeface.native_instance)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * 17) + ((int) (this.native_instance ^ (this.native_instance >>> 32))))) + this.mStyle;
    }

    public boolean isSupportedAxes(int axis) {
        if (this.mSupportedAxes == null) {
            synchronized (this) {
                if (this.mSupportedAxes == null) {
                    this.mSupportedAxes = nativeGetSupportedAxes(this.native_instance);
                    if (this.mSupportedAxes == null) {
                        this.mSupportedAxes = EMPTY_AXES;
                    }
                }
            }
        }
        return Arrays.binarySearch(this.mSupportedAxes, axis) >= 0;
    }
}
