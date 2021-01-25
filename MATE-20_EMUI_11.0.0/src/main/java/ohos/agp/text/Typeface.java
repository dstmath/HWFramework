package ohos.agp.text;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Typeface {
    public static final int BOLD = 1;
    public static final int BOLD_ITALIC = 3;
    public static final Typeface DEFAULT = new Typeface(nativeGetDefault(), false);
    public static final Typeface DEFAULT_BOLD = new Typeface(nativeGetDefaultBold(), false);
    public static final int ITALIC = 2;
    private static final Object LOCK = new Object();
    public static final Typeface MONOSPACE = new Typeface(nativeGetMono(), false);
    public static final int NORMAL = 0;
    public static final Typeface SANS_SERIF = new Typeface(nativeGetSans(), false);
    public static final Typeface SERIF = new Typeface(nativeGetSerif(), false);
    public static final int STYLE_MASK = 3;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP");
    private static Map<String, String> sAdditionalSystemFonts = new HashMap();
    private static Map<String, Typeface> sTypefaceCache = new HashMap();
    private long mNativeTypefacePtr = 0;
    private Object mTypefaceImpl;

    private static native void nativeCreateFromFile(File file, TypefaceImplWrapper typefaceImplWrapper);

    private static native long nativeCreateWithDifferentStyle(long j, int i);

    private static native long nativeCreateWithDifferentWeight(long j, int i, boolean z);

    private static native long nativeCreateWithName(String str, int i);

    private static native long nativeGetDefault();

    private static native long nativeGetDefaultBold();

    private static native String nativeGetFamilyName(long j);

    private static native long nativeGetMono();

    private static native long nativeGetSans();

    private static native long nativeGetSerif();

    private static native int nativeGetStyle(long j);

    private static native int nativeGetWeight(long j);

    private static native boolean nativeIsItalic(long j);

    static {
        initAdditionalSystemFonts();
    }

    private static void initAdditionalSystemFonts() {
        sAdditionalSystemFonts.put("HOSP-digit-Bold", "/system/fonts/HW-digit-Bold.ttf");
        sTypefaceCache.put("HOSP-digit-Bold", null);
        sAdditionalSystemFonts.put("HOSP-Chinese-Medium", "/system/fonts/HwChinese-Medium.ttf");
        sTypefaceCache.put("HOSP-Chinese-Medium", null);
        sAdditionalSystemFonts.put("HOSP-DroidSansChinese", "/system/fonts/DroidSansChinese.ttf");
        sTypefaceCache.put("HOSP-DroidSansChinese", null);
    }

    private static class TypefaceCleaner extends NativeMemoryCleanerHelper {
        private native void nativeTypefaceRelease(long j);

        public TypefaceCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            nativeTypefaceRelease(j);
        }
    }

    private Typeface(long j, boolean z) {
        this.mNativeTypefacePtr = j;
        if (z) {
            MemoryCleanerRegistry.getInstance().register(this, new TypefaceCleaner(this.mNativeTypefacePtr));
        }
    }

    public static Typeface create(String str, int i) {
        if ((i & -4) != 0) {
            i = 0;
        }
        synchronized (LOCK) {
            if (!sTypefaceCache.containsKey(str)) {
                return new Typeface(nativeCreateWithName(str, i), true);
            }
            Typeface typeface = sTypefaceCache.get(str);
            if (typeface != null) {
                return create(typeface, i);
            }
            Typeface createFromFile = createFromFile(new File(sAdditionalSystemFonts.get(str)));
            sTypefaceCache.put(str, createFromFile);
            return create(createFromFile, i);
        }
    }

    public static Typeface create(Typeface typeface, int i) {
        if ((i & -4) != 0) {
            i = 0;
        }
        if (typeface == null || typeface.getNativeTypefacePtr() == 0) {
            typeface = DEFAULT;
        }
        if (typeface.getStyle() == i) {
            return typeface;
        }
        return new Typeface(nativeCreateWithDifferentStyle(typeface.getNativeTypefacePtr(), i), true);
    }

    public static Typeface create(Typeface typeface, int i, boolean z) {
        if (i < 1) {
            i = 1;
        } else if (i > 1000) {
            i = 1000;
        }
        if (typeface == null || typeface.getNativeTypefacePtr() == 0) {
            typeface = DEFAULT;
        }
        if (typeface.getWeight() == i && typeface.isItalic() == z) {
            return typeface;
        }
        return new Typeface(nativeCreateWithDifferentWeight(typeface.getNativeTypefacePtr(), i, z), true);
    }

    @Deprecated
    public static Typeface createFromFile(String str) {
        return createFromFile(new File(str));
    }

    public static Typeface createFromFile(File file) {
        if (file == null) {
            return DEFAULT;
        }
        TypefaceImplWrapper typefaceImplWrapper = new TypefaceImplWrapper();
        nativeCreateFromFile(file, typefaceImplWrapper);
        if (typefaceImplWrapper.nativeTypeface == 0) {
            HiLog.debug(TAG, "Font file not found: %{private}s.", new Object[]{file.getPath()});
            return DEFAULT;
        }
        Typeface typeface = new Typeface(typefaceImplWrapper.nativeTypeface, true);
        typeface.mTypefaceImpl = typefaceImplWrapper.typefaceImpl;
        return typeface;
    }

    /* access modifiers changed from: private */
    public static class TypefaceImplWrapper {
        public long nativeTypeface;
        public Object typefaceImpl;

        private TypefaceImplWrapper() {
        }
    }

    public int getStyle() {
        return nativeGetStyle(this.mNativeTypefacePtr);
    }

    public int getWeight() {
        return nativeGetWeight(this.mNativeTypefacePtr);
    }

    public boolean isItalic() {
        return nativeIsItalic(this.mNativeTypefacePtr);
    }

    @Deprecated
    public String getFamilyName() {
        return nativeGetFamilyName(this.mNativeTypefacePtr);
    }

    public long getNativeTypefacePtr() {
        return this.mNativeTypefacePtr;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof Typeface)) {
            return false;
        }
        return this.mNativeTypefacePtr == ((Typeface) obj).mNativeTypefacePtr;
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(this.mNativeTypefacePtr));
    }
}
