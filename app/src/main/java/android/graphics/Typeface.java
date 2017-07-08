package android.graphics;

import android.content.res.AssetManager;
import android.graphics.FontListParser.Alias;
import android.graphics.FontListParser.Config;
import android.graphics.FontListParser.Family;
import android.graphics.FontListParser.Font;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.LruCache;
import android.util.SparseArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

public class Typeface {
    public static final int BOLD = 1;
    public static final int BOLD_ITALIC = 3;
    public static final Typeface CHNFZSLIM = null;
    public static final Typeface DEFAULT = null;
    public static final Typeface DEFAULT_BOLD = null;
    static final String FONTS_CONFIG = "fonts.xml";
    static final String HW_CHINESE_FONTS_CONFIG = "HwChinesefonts.xml";
    static final String HW_ENG_FONTS_CONFIG = "HwEnglishfonts.xml";
    public static final int ITALIC = 2;
    public static final Typeface MONOSPACE = null;
    public static final int NORMAL = 0;
    public static final Typeface SANS_SERIF = null;
    public static final Typeface SERIF = null;
    private static String TAG;
    private static boolean mCurrentIsChineseOverlayFontsExist;
    private static boolean mLastIsChineseOverlayFontsExist;
    private static int mUserId;
    private static Family sChineseFamily;
    static Typeface sDefaultTypeface;
    static Typeface[] sDefaults;
    private static final LruCache<String, Typeface> sDynamicTypefaceCache = null;
    private static Family sEnglishFamily;
    static FontFamily[] sFallbackFonts;
    private static Family sHwCurrentChineseFamily;
    private static Family sHwCurrentEnglishFamily;
    private static Family sHwLastChineseFamily;
    private static Family sHwLastEnglishFamily;
    private static Map<String, ByteBuffer> sHwbufferForPath;
    private static int sInitChinesePos;
    private static int sInitEnglishPos;
    static Map<String, Typeface> sSystemFontMap;
    private static final LongSparseArray<SparseArray<Typeface>> sTypefaceCache = null;
    private static Map<String, ByteBuffer> sbufferForPath;
    private int mStyle;
    public long native_instance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Typeface.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Typeface.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.Typeface.<clinit>():void");
    }

    private static native long nativeCreateFromArray(long[] jArr);

    private static native long nativeCreateFromTypeface(long j, int i);

    private static native long nativeCreateWeightAlias(long j, int i);

    private static native int nativeGetStyle(long j);

    private static native void nativeSetDefault(long j);

    private static native void nativeUnref(long j);

    private static void setDefault(Typeface t) {
        sDefaultTypeface = t;
        nativeSetDefault(t.native_instance);
    }

    public int getStyle() {
        return this.mStyle;
    }

    public final boolean isBold() {
        return (this.mStyle & BOLD) != 0;
    }

    public final boolean isItalic() {
        return (this.mStyle & ITALIC) != 0;
    }

    public static Typeface create(String familyName, int style) {
        if (sSystemFontMap != null) {
            return create((Typeface) sSystemFontMap.get(familyName), style);
        }
        return null;
    }

    public static Typeface create(Typeface family, int style) {
        Typeface typeface;
        if (style < 0 || style > BOLD_ITALIC) {
            style = 0;
        }
        long ni = 0;
        if (family != null) {
            if (family.mStyle == style) {
                return family;
            }
            ni = family.native_instance;
        }
        SparseArray<Typeface> styles = (SparseArray) sTypefaceCache.get(ni);
        if (styles != null) {
            typeface = (Typeface) styles.get(style);
            if (typeface != null) {
                return typeface;
            }
        }
        typeface = new Typeface(nativeCreateFromTypeface(ni, style));
        if (styles == null) {
            styles = new SparseArray(4);
            sTypefaceCache.put(ni, styles);
        }
        styles.put(style, typeface);
        return typeface;
    }

    public static Typeface defaultFromStyle(int style) {
        return sDefaults[style];
    }

    public static Typeface createFromAsset(AssetManager mgr, String path) {
        if (sFallbackFonts != null) {
            synchronized (sDynamicTypefaceCache) {
                String key = createAssetUid(mgr, path);
                Typeface typeface = (Typeface) sDynamicTypefaceCache.get(key);
                if (typeface != null) {
                    return typeface;
                }
                FontFamily fontFamily = new FontFamily();
                if (fontFamily.addFontFromAsset(mgr, path)) {
                    FontFamily[] families = new FontFamily[BOLD];
                    families[0] = fontFamily;
                    typeface = createFromFamiliesWithDefault(families);
                    sDynamicTypefaceCache.put(key, typeface);
                    return typeface;
                }
            }
        }
        throw new RuntimeException("Font asset not found " + path);
    }

    private static String createAssetUid(AssetManager mgr, String path) {
        SparseArray<String> pkgs = mgr.getAssignedPackageIdentifiers();
        StringBuilder builder = new StringBuilder();
        int size = pkgs.size();
        for (int i = 0; i < size; i += BOLD) {
            builder.append((String) pkgs.valueAt(i));
            builder.append("-");
        }
        builder.append(path);
        return builder.toString();
    }

    public static Typeface createFromFile(File path) {
        return createFromFile(path.getAbsolutePath());
    }

    public static Typeface createFromFile(String path) {
        if (sFallbackFonts != null) {
            FontFamily fontFamily = new FontFamily();
            if (fontFamily.addFont(path, 0)) {
                FontFamily[] families = new FontFamily[BOLD];
                families[0] = fontFamily;
                return createFromFamiliesWithDefault(families);
            }
        }
        throw new RuntimeException("Font not found " + path);
    }

    private static File getHwFontConfigLocation() {
        return new File("/data/skin/fonts/");
    }

    public static void initHwFontConfig() {
        File systemFontConfigLocation = getHwFontConfigLocation();
        sHwLastChineseFamily = sHwCurrentChineseFamily;
        sHwLastEnglishFamily = sHwCurrentEnglishFamily;
        mLastIsChineseOverlayFontsExist = mCurrentIsChineseOverlayFontsExist;
        sHwCurrentChineseFamily = null;
        sHwCurrentEnglishFamily = null;
        mCurrentIsChineseOverlayFontsExist = false;
        if (systemFontConfigLocation.exists()) {
            File chineseconfigFilename = new File(systemFontConfigLocation, HW_CHINESE_FONTS_CONFIG);
            File englishconfigFilename = new File(systemFontConfigLocation, HW_ENG_FONTS_CONFIG);
            if (chineseconfigFilename.exists()) {
                try {
                    sHwCurrentChineseFamily = (Family) FontListParser.parse(new FileInputStream(chineseconfigFilename)).families.get(0);
                    for (Font font : sHwCurrentChineseFamily.fonts) {
                        if (font.fontName != null) {
                            font.fontName = font.fontName.replaceAll("/system/fonts/hw/", "/data/skin/fonts/");
                        }
                    }
                } catch (RuntimeException e) {
                    Log.w(TAG, "Didn't create chinese family (most likely, non-Minikin build)", e);
                } catch (FileNotFoundException e2) {
                    Log.e(TAG, "Error opening " + chineseconfigFilename, e2);
                } catch (IOException e3) {
                    Log.e(TAG, "Error reading " + chineseconfigFilename, e3);
                } catch (XmlPullParserException e4) {
                    Log.e(TAG, "XML parse exception for " + chineseconfigFilename, e4);
                }
            }
            if (englishconfigFilename.exists()) {
                try {
                    sHwCurrentEnglishFamily = (Family) FontListParser.parse(new FileInputStream(englishconfigFilename)).families.get(0);
                    for (Font font2 : sHwCurrentEnglishFamily.fonts) {
                        if (font2.fontName != null) {
                            font2.fontName = font2.fontName.replaceAll("/system/fonts/hw/", "/data/skin/fonts/");
                        }
                    }
                } catch (RuntimeException e5) {
                    Log.w(TAG, "Didn't create english family (most likely, non-Minikin build)", e5);
                } catch (FileNotFoundException e22) {
                    Log.e(TAG, "Error opening " + englishconfigFilename, e22);
                } catch (IOException e32) {
                    Log.e(TAG, "Error reading " + englishconfigFilename, e32);
                } catch (XmlPullParserException e42) {
                    Log.e(TAG, "XML parse exception for " + englishconfigFilename, e42);
                }
            }
            if (new File("/data/skin/fonts/DroidSansChinese.ttf").exists()) {
                mCurrentIsChineseOverlayFontsExist = true;
            }
            Log.i(TAG, "initHwFontConfig end ");
        }
    }

    public static void updateFont() {
        FontFamily HwChineseFontFamily = null;
        FontFamily HwEnglishFontFamily = null;
        initHwFontConfig();
        if (sInitChinesePos > 0) {
            HwChineseFontFamily = sFallbackFonts[sInitChinesePos];
        }
        if (sInitEnglishPos > 0) {
            HwEnglishFontFamily = sFallbackFonts[sInitEnglishPos];
        }
        sHwbufferForPath.clear();
        if (sHwLastChineseFamily == null && sHwCurrentChineseFamily == null) {
            if (HwChineseFontFamily != null && (mLastIsChineseOverlayFontsExist || mCurrentIsChineseOverlayFontsExist)) {
                HwChineseFontFamily.resetFont();
                if (mCurrentIsChineseOverlayFontsExist) {
                    HwChineseFontFamily.addFont("/data/skin/fonts/DroidSansChinese.ttf", 0);
                    HwChineseFontFamily.setHwFontFamilyType(ITALIC);
                } else {
                    updateFamilyFromParsed(sChineseFamily, sHwbufferForPath, HwChineseFontFamily);
                    HwChineseFontFamily.setHwFontFamilyType(0);
                }
                HwChineseFontFamily.resetCoverage();
            }
        } else if (HwChineseFontFamily != null) {
            HwChineseFontFamily.resetFont();
            if (sHwCurrentChineseFamily != null) {
                updateFamilyFromParsed(sHwCurrentChineseFamily, sHwbufferForPath, HwChineseFontFamily);
                HwChineseFontFamily.setHwFontFamilyType(ITALIC);
            } else {
                updateFamilyFromParsed(sChineseFamily, sHwbufferForPath, HwChineseFontFamily);
                HwChineseFontFamily.setHwFontFamilyType(0);
            }
            HwChineseFontFamily.resetCoverage();
        }
        if (HwEnglishFontFamily == null) {
            return;
        }
        if (sHwLastEnglishFamily != null || sHwCurrentEnglishFamily != null) {
            HwEnglishFontFamily.resetFont();
            if (sHwCurrentEnglishFamily != null) {
                updateFamilyFromParsed(sHwCurrentEnglishFamily, sHwbufferForPath, HwEnglishFontFamily);
                HwEnglishFontFamily.setHwFontFamilyType(BOLD);
            } else {
                updateFamilyFromParsed(sEnglishFamily, sHwbufferForPath, HwEnglishFontFamily);
                HwEnglishFontFamily.setHwFontFamilyType(0);
            }
            HwEnglishFontFamily.resetCoverage();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void updateFamilyFromParsed(Family family, Map<String, ByteBuffer> bufferForPath, FontFamily fontfamily) {
        Throwable th;
        Throwable th2;
        for (Font font : family.fonts) {
            ByteBuffer fontBuffer = (ByteBuffer) bufferForPath.get(font.fontName);
            if (fontBuffer == null) {
                fontBuffer = (ByteBuffer) sbufferForPath.get(font.fontName);
            }
            if (fontBuffer == null) {
                Throwable th3 = null;
                FileInputStream fileInputStream = null;
                try {
                    FileInputStream fileInputStream2 = new FileInputStream(font.fontName);
                    try {
                        FileChannel fileChannel = fileInputStream2.getChannel();
                        fontBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
                        bufferForPath.put(font.fontName, fontBuffer);
                        if (fileInputStream2 != null) {
                            try {
                                fileInputStream2.close();
                            } catch (Throwable th4) {
                                th3 = th4;
                            }
                        }
                        if (th3 != null) {
                            try {
                                throw th3;
                            } catch (IOException e) {
                                fileInputStream = fileInputStream2;
                            }
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        th2 = null;
                        fileInputStream = fileInputStream2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable th6) {
                                if (th2 == null) {
                                    th2 = th6;
                                } else if (th2 != th6) {
                                    th2.addSuppressed(th6);
                                }
                            }
                        }
                        if (th2 == null) {
                            try {
                                throw th2;
                            } catch (IOException e2) {
                            }
                        } else {
                            throw th;
                        }
                    }
                } catch (Throwable th7) {
                    th = th7;
                    th2 = null;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (th2 == null) {
                        throw th;
                    }
                    throw th2;
                }
            }
            if (!fontfamily.addFontWeightStyle(fontBuffer, font.ttcIndex, font.axes, font.weight, font.isItalic)) {
                Log.e(TAG, "Error creating font " + font.fontName + "#" + font.ttcIndex);
            }
        }
    }

    public static synchronized void loadSystemFonts() {
        synchronized (Typeface.class) {
            updateFont();
        }
    }

    public static synchronized void setCurrentUserId(int userId) {
        synchronized (Typeface.class) {
            mUserId = userId;
        }
    }

    public static synchronized int getCurrentUserId() {
        int i;
        synchronized (Typeface.class) {
            i = mUserId;
        }
        return i;
    }

    public static Typeface createFromFamilies(FontFamily[] families) {
        long[] ptrArray = new long[families.length];
        for (int i = 0; i < families.length; i += BOLD) {
            ptrArray[i] = families[i].mNativePtr;
        }
        return new Typeface(nativeCreateFromArray(ptrArray));
    }

    public static Typeface createFromFamiliesWithDefault(FontFamily[] families) {
        int i;
        long[] ptrArray = new long[(families.length + sFallbackFonts.length)];
        for (i = 0; i < families.length; i += BOLD) {
            ptrArray[i] = families[i].mNativePtr;
        }
        for (i = 0; i < sFallbackFonts.length; i += BOLD) {
            ptrArray[families.length + i] = sFallbackFonts[i].mNativePtr;
        }
        return new Typeface(nativeCreateFromArray(ptrArray));
    }

    private Typeface(long ni) {
        this.mStyle = 0;
        if (ni == 0) {
            throw new RuntimeException("native typeface cannot be made");
        }
        this.native_instance = ni;
        this.mStyle = nativeGetStyle(ni);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static FontFamily makeFamilyFromParsed(Family family, Map<String, ByteBuffer> bufferForPath) {
        Throwable th;
        Throwable th2;
        FontFamily fontFamily = new FontFamily(family.lang, family.variant);
        for (Font font : family.fonts) {
            ByteBuffer fontBuffer = (ByteBuffer) bufferForPath.get(font.fontName);
            if (fontBuffer == null) {
                Throwable th3 = null;
                FileInputStream fileInputStream = null;
                try {
                    FileInputStream fileInputStream2 = new FileInputStream(font.fontName);
                    try {
                        FileChannel fileChannel = fileInputStream2.getChannel();
                        fontBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
                        bufferForPath.put(font.fontName, fontBuffer);
                        if (fileInputStream2 != null) {
                            try {
                                fileInputStream2.close();
                            } catch (Throwable th4) {
                                th3 = th4;
                            }
                        }
                        if (th3 != null) {
                            try {
                                throw th3;
                            } catch (IOException e) {
                                fileInputStream = fileInputStream2;
                            }
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        th2 = null;
                        fileInputStream = fileInputStream2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable th6) {
                                if (th2 == null) {
                                    th2 = th6;
                                } else if (th2 != th6) {
                                    th2.addSuppressed(th6);
                                }
                            }
                        }
                        if (th2 == null) {
                            try {
                                throw th2;
                            } catch (IOException e2) {
                            }
                        } else {
                            throw th;
                        }
                    }
                } catch (Throwable th7) {
                    th = th7;
                    th2 = null;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (th2 == null) {
                        throw th;
                    }
                    throw th2;
                }
            }
            if (!fontFamily.addFontWeightStyle(fontBuffer, font.ttcIndex, font.axes, font.weight, font.isItalic)) {
                Log.e(TAG, "Error creating font " + font.fontName + "#" + font.ttcIndex);
            }
        }
        return fontFamily;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void init() {
        File configFilename = new File(getSystemFontConfigLocation(), FONTS_CONFIG);
        try {
            Family f;
            Config fontConfig = FontListParser.parse(new FileInputStream(configFilename));
            Map<String, ByteBuffer> bufferForPath = new HashMap();
            List<FontFamily> familyList = new ArrayList();
            int i = 0;
            while (true) {
                if (i >= fontConfig.families.size()) {
                    break;
                }
                f = (Family) fontConfig.families.get(i);
                if (i == 0 || f.name == null) {
                    boolean isChineseFamily = false;
                    boolean isEnglishFamily = false;
                    for (Font font : f.fonts) {
                        if (font.fontName != null) {
                            if (font.fontName.contains("DroidSansChinese.ttf")) {
                                isChineseFamily = true;
                            }
                        }
                        if (font.fontName != null) {
                            if (font.fontName.contains("HwEnglish.ttf")) {
                                isEnglishFamily = true;
                            }
                        }
                    }
                    synchronized (Typeface.class) {
                        if (isChineseFamily) {
                            sInitChinesePos = familyList.size();
                            sChineseFamily = f;
                        }
                        if (isEnglishFamily) {
                            sInitEnglishPos = familyList.size();
                            sEnglishFamily = f;
                        }
                        familyList.add(makeFamilyFromParsed(f, bufferForPath));
                    }
                }
                i += BOLD;
            }
            sFallbackFonts = (FontFamily[]) familyList.toArray(new FontFamily[familyList.size()]);
            setDefault(createFromFamilies(sFallbackFonts));
            Map<String, Typeface> systemFonts = new HashMap();
            i = 0;
            while (true) {
                if (i >= fontConfig.families.size()) {
                    break;
                }
                f = (Family) fontConfig.families.get(i);
                if (f.name != null) {
                    Typeface typeface;
                    if (i == 0) {
                        typeface = sDefaultTypeface;
                    } else {
                        FontFamily[] families = new FontFamily[BOLD];
                        families[0] = makeFamilyFromParsed(f, bufferForPath);
                        typeface = createFromFamiliesWithDefault(families);
                    }
                    systemFonts.put(f.name, typeface);
                }
                i += BOLD;
            }
            sbufferForPath = bufferForPath;
            for (Alias alias : fontConfig.aliases) {
                Typeface base = (Typeface) systemFonts.get(alias.toName);
                Typeface newFace = base;
                int weight = alias.weight;
                if (weight != 400) {
                    Typeface typeface2 = new Typeface(nativeCreateWeightAlias(base.native_instance, weight));
                }
                systemFonts.put(alias.name, newFace);
            }
            sSystemFontMap = systemFonts;
        } catch (RuntimeException e) {
            Log.w(TAG, "Didn't create default family (most likely, non-Minikin build)", e);
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "Error opening " + configFilename, e2);
        } catch (IOException e3) {
            Log.e(TAG, "Error reading " + configFilename, e3);
        } catch (XmlPullParserException e4) {
            Log.e(TAG, "XML parse exception for " + configFilename, e4);
        }
    }

    private static File getSystemFontConfigLocation() {
        return new File("/system/etc/");
    }

    protected void finalize() throws Throwable {
        try {
            nativeUnref(this.native_instance);
            this.native_instance = 0;
        } finally {
            super.finalize();
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
        return ((((int) (this.native_instance ^ (this.native_instance >>> 32))) + 527) * 31) + this.mStyle;
    }
}
