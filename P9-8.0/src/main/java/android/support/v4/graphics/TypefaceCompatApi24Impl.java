package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.content.res.FontResourcesParserCompat.FontFamilyFilesResourceEntry;
import android.support.v4.content.res.FontResourcesParserCompat.FontFileResourceEntry;
import android.support.v4.provider.FontsContractCompat.FontInfo;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

@RequiresApi(24)
@RestrictTo({Scope.LIBRARY_GROUP})
class TypefaceCompatApi24Impl implements TypefaceCompatImpl {
    private static final String ADD_FONT_WEIGHT_STYLE_METHOD = "addFontWeightStyle";
    private static final String CREATE_FROM_FAMILIES_WITH_DEFAULT_METHOD = "createFromFamiliesWithDefault";
    private static final String FONT_FAMILY_CLASS = "android.graphics.FontFamily";
    private static final String TAG = "TypefaceCompatApi24Impl";
    private static final Method sAddFontWeightStyle;
    private static final Method sCreateFromFamiliesWithDefault;
    private static final Class sFontFamily;
    private static final Constructor sFontFamilyCtor;

    TypefaceCompatApi24Impl() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0052 A:{Splitter: B:0:0x0000, ExcHandler: java.lang.ClassNotFoundException (r2_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:4:0x0052, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0053, code:
            android.util.Log.e(TAG, r2.getClass().getName(), r2);
            r4 = null;
            r5 = null;
            r0 = null;
            r1 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static {
        Class fontFamilyClass;
        Constructor fontFamilyCtor;
        Method addFontMethod;
        Method createFromFamiliesWithDefaultMethod;
        try {
            fontFamilyClass = Class.forName(FONT_FAMILY_CLASS);
            fontFamilyCtor = fontFamilyClass.getConstructor(new Class[0]);
            addFontMethod = fontFamilyClass.getMethod(ADD_FONT_WEIGHT_STYLE_METHOD, new Class[]{ByteBuffer.class, Integer.TYPE, List.class, Integer.TYPE, Boolean.TYPE});
            Object familyArray = Array.newInstance(fontFamilyClass, 1);
            createFromFamiliesWithDefaultMethod = Typeface.class.getMethod(CREATE_FROM_FAMILIES_WITH_DEFAULT_METHOD, new Class[]{familyArray.getClass()});
        } catch (ReflectiveOperationException e) {
        }
        sFontFamilyCtor = fontFamilyCtor;
        sFontFamily = fontFamilyClass;
        sAddFontWeightStyle = addFontMethod;
        sCreateFromFamiliesWithDefault = createFromFamiliesWithDefaultMethod;
    }

    public static boolean isUsable() {
        return sAddFontWeightStyle != null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{Splitter: B:0:0x0000, ExcHandler: java.lang.IllegalAccessException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a A:{Splitter: B:0:0x0000, ExcHandler: java.lang.IllegalAccessException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:3:0x000a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0010, code:
            throw new java.lang.RuntimeException(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Object newFamily() {
        try {
            return sFontFamilyCtor.newInstance(new Object[0]);
        } catch (ReflectiveOperationException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x002c A:{Splitter: B:0:0x0000, ExcHandler: java.lang.IllegalAccessException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:3:0x002c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0032, code:
            throw new java.lang.RuntimeException(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean addFontWeightStyle(Object family, ByteBuffer buffer, int ttcIndex, int weight, boolean style) {
        try {
            return ((Boolean) sAddFontWeightStyle.invoke(family, new Object[]{buffer, Integer.valueOf(ttcIndex), null, Integer.valueOf(weight), Boolean.valueOf(style)})).booleanValue();
        } catch (ReflectiveOperationException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x001b A:{Splitter: B:0:0x0000, ExcHandler: java.lang.IllegalAccessException (r0_0 'e' java.lang.ReflectiveOperationException)} */
    /* JADX WARNING: Missing block: B:3:0x001b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x0021, code:
            throw new java.lang.RuntimeException(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Typeface createFromFamiliesWithDefault(Object family) {
        try {
            Array.set(Array.newInstance(sFontFamily, 1), 0, family);
            return (Typeface) sCreateFromFamiliesWithDefault.invoke(null, new Object[]{familyArray});
        } catch (ReflectiveOperationException e) {
        }
    }

    public Typeface createTypeface(Context context, @NonNull FontInfo[] fonts, Map<Uri, ByteBuffer> uriBuffer) {
        Object family = newFamily();
        for (FontInfo font : fonts) {
            if (!addFontWeightStyle(family, (ByteBuffer) uriBuffer.get(font.getUri()), font.getTtcIndex(), font.getWeight(), font.isItalic())) {
                return null;
            }
        }
        return createFromFamiliesWithDefault(family);
    }

    public Typeface createFromFontFamilyFilesResourceEntry(Context context, FontFamilyFilesResourceEntry entry, Resources resources, int style) {
        Object family = newFamily();
        for (FontFileResourceEntry e : entry.getEntries()) {
            if (!addFontWeightStyle(family, TypefaceCompatUtil.copyToDirectBuffer(context, resources, e.getResourceId()), 0, e.getWeight(), e.isItalic())) {
                return null;
            }
        }
        return createFromFamiliesWithDefault(family);
    }
}
