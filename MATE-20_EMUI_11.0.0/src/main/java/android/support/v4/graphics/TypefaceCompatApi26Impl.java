package android.support.v4.graphics;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.fonts.FontVariationAxis;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.content.res.FontResourcesParserCompat;
import android.support.v4.provider.FontsContractCompat;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Map;

@RequiresApi(26)
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class TypefaceCompatApi26Impl extends TypefaceCompatApi21Impl {
    private static final String ABORT_CREATION_METHOD = "abortCreation";
    private static final String ADD_FONT_FROM_ASSET_MANAGER_METHOD = "addFontFromAssetManager";
    private static final String ADD_FONT_FROM_BUFFER_METHOD = "addFontFromBuffer";
    private static final String CREATE_FROM_FAMILIES_WITH_DEFAULT_METHOD = "createFromFamiliesWithDefault";
    private static final String DEFAULT_FAMILY = "sans-serif";
    private static final String FONT_FAMILY_CLASS = "android.graphics.FontFamily";
    private static final String FREEZE_METHOD = "freeze";
    private static final int RESOLVE_BY_FONT_TABLE = -1;
    private static final String TAG = "TypefaceCompatApi26Impl";
    protected final Method mAbortCreation;
    protected final Method mAddFontFromAssetManager;
    protected final Method mAddFontFromBuffer;
    protected final Method mCreateFromFamiliesWithDefault;
    protected final Class mFontFamily;
    protected final Constructor mFontFamilyCtor;
    protected final Method mFreeze;

    public TypefaceCompatApi26Impl() {
        Method createFromFamiliesWithDefault;
        Method abortCreation;
        Method freeze;
        Method addFontFromBuffer;
        Constructor fontFamilyCtor;
        Class fontFamily;
        Method createFromFamiliesWithDefault2;
        try {
            Class fontFamily2 = obtainFontFamily();
            Constructor fontFamilyCtor2 = obtainFontFamilyCtor(fontFamily2);
            Method addFontFromAssetManager = obtainAddFontFromAssetManagerMethod(fontFamily2);
            Method addFontFromBuffer2 = obtainAddFontFromBufferMethod(fontFamily2);
            Method freeze2 = obtainFreezeMethod(fontFamily2);
            Method abortCreation2 = obtainAbortCreationMethod(fontFamily2);
            fontFamily = fontFamily2;
            createFromFamiliesWithDefault2 = obtainCreateFromFamiliesWithDefaultMethod(fontFamily2);
            createFromFamiliesWithDefault = abortCreation2;
            abortCreation = freeze2;
            freeze = addFontFromBuffer2;
            addFontFromBuffer = addFontFromAssetManager;
            fontFamilyCtor = fontFamilyCtor2;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            Log.e(TAG, "Unable to collect necessary methods for class " + e.getClass().getName(), e);
            fontFamily = null;
            fontFamilyCtor = null;
            addFontFromBuffer = null;
            freeze = null;
            abortCreation = null;
            createFromFamiliesWithDefault = null;
            createFromFamiliesWithDefault2 = null;
        }
        this.mFontFamily = fontFamily;
        this.mFontFamilyCtor = fontFamilyCtor;
        this.mAddFontFromAssetManager = addFontFromBuffer;
        this.mAddFontFromBuffer = freeze;
        this.mFreeze = abortCreation;
        this.mAbortCreation = createFromFamiliesWithDefault;
        this.mCreateFromFamiliesWithDefault = createFromFamiliesWithDefault2;
    }

    private boolean isFontFamilyPrivateAPIAvailable() {
        if (this.mAddFontFromAssetManager == null) {
            Log.w(TAG, "Unable to collect necessary private methods. Fallback to legacy implementation.");
        }
        return this.mAddFontFromAssetManager != null;
    }

    private Object newFamily() {
        try {
            return this.mFontFamilyCtor.newInstance(new Object[0]);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean addFontFromAssetManager(Context context, Object family, String fileName, int ttcIndex, int weight, int style, @Nullable FontVariationAxis[] axes) {
        try {
            return ((Boolean) this.mAddFontFromAssetManager.invoke(family, context.getAssets(), fileName, 0, false, Integer.valueOf(ttcIndex), Integer.valueOf(weight), Integer.valueOf(style), axes)).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean addFontFromBuffer(Object family, ByteBuffer buffer, int ttcIndex, int weight, int style) {
        try {
            return ((Boolean) this.mAddFontFromBuffer.invoke(family, buffer, Integer.valueOf(ttcIndex), null, Integer.valueOf(weight), Integer.valueOf(style))).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: protected */
    public Typeface createFromFamiliesWithDefault(Object family) {
        try {
            Object familyArray = Array.newInstance(this.mFontFamily, 1);
            Array.set(familyArray, 0, family);
            return (Typeface) this.mCreateFromFamiliesWithDefault.invoke(null, familyArray, -1, -1);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean freeze(Object family) {
        try {
            return ((Boolean) this.mFreeze.invoke(family, new Object[0])).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void abortCreation(Object family) {
        try {
            this.mAbortCreation.invoke(family, new Object[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // android.support.v4.graphics.TypefaceCompatBaseImpl
    public Typeface createFromFontFamilyFilesResourceEntry(Context context, FontResourcesParserCompat.FontFamilyFilesResourceEntry entry, Resources resources, int style) {
        if (!isFontFamilyPrivateAPIAvailable()) {
            return super.createFromFontFamilyFilesResourceEntry(context, entry, resources, style);
        }
        Object fontFamily = newFamily();
        FontResourcesParserCompat.FontFileResourceEntry[] entries = entry.getEntries();
        for (FontResourcesParserCompat.FontFileResourceEntry fontFile : entries) {
            if (!addFontFromAssetManager(context, fontFamily, fontFile.getFileName(), fontFile.getTtcIndex(), fontFile.getWeight(), fontFile.isItalic() ? 1 : 0, FontVariationAxis.fromFontVariationSettings(fontFile.getVariationSettings()))) {
                abortCreation(fontFamily);
                return null;
            }
        }
        if (!freeze(fontFamily)) {
            return null;
        }
        return createFromFamiliesWithDefault(fontFamily);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:56:0x00b9 */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0056, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0057, code lost:
        r4 = r0;
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005e, code lost:
        r5 = r0;
        r4 = r0;
     */
    @Override // android.support.v4.graphics.TypefaceCompatApi21Impl, android.support.v4.graphics.TypefaceCompatBaseImpl
    public Typeface createFromFontInfo(Context context, @Nullable CancellationSignal cancellationSignal, @NonNull FontsContractCompat.FontInfo[] fonts, int style) {
        ParcelFileDescriptor pfd;
        Throwable th;
        Throwable th2;
        if (fonts.length < 1) {
            return null;
        }
        if (!isFontFamilyPrivateAPIAvailable()) {
            FontsContractCompat.FontInfo bestFont = findBestInfo(fonts, style);
            try {
                pfd = context.getContentResolver().openFileDescriptor(bestFont.getUri(), "r", cancellationSignal);
                if (pfd == null) {
                    if (pfd != null) {
                        pfd.close();
                    }
                    return null;
                }
                Typeface build = new Typeface.Builder(pfd.getFileDescriptor()).setWeight(bestFont.getWeight()).setItalic(bestFont.isItalic()).build();
                if (pfd != null) {
                    pfd.close();
                }
                return build;
            } catch (IOException e) {
                return null;
            }
        } else {
            Map<Uri, ByteBuffer> uriBuffer = FontsContractCompat.prepareFontData(context, fonts, cancellationSignal);
            Object fontFamily = newFamily();
            int length = fonts.length;
            boolean atLeastOneFont = false;
            int i = 0;
            while (i < length) {
                FontsContractCompat.FontInfo font = fonts[i];
                ByteBuffer fontBuffer = uriBuffer.get(font.getUri());
                if (fontBuffer != null) {
                    if (!addFontFromBuffer(fontFamily, fontBuffer, font.getTtcIndex(), font.getWeight(), font.isItalic() ? 1 : 0)) {
                        abortCreation(fontFamily);
                        return null;
                    }
                    atLeastOneFont = true;
                }
                i++;
                atLeastOneFont = atLeastOneFont;
            }
            if (!atLeastOneFont) {
                abortCreation(fontFamily);
                return null;
            } else if (!freeze(fontFamily)) {
                return null;
            } else {
                return Typeface.create(createFromFamiliesWithDefault(fontFamily), style);
            }
        }
        if (pfd != null) {
            if (th != null) {
                try {
                    pfd.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
            } else {
                pfd.close();
            }
        }
        throw th2;
        throw th2;
    }

    @Override // android.support.v4.graphics.TypefaceCompatBaseImpl
    @Nullable
    public Typeface createFromResourcesFontFile(Context context, Resources resources, int id, String path, int style) {
        if (!isFontFamilyPrivateAPIAvailable()) {
            return super.createFromResourcesFontFile(context, resources, id, path, style);
        }
        Object fontFamily = newFamily();
        if (!addFontFromAssetManager(context, fontFamily, path, 0, -1, -1, null)) {
            abortCreation(fontFamily);
            return null;
        } else if (!freeze(fontFamily)) {
            return null;
        } else {
            return createFromFamiliesWithDefault(fontFamily);
        }
    }

    /* access modifiers changed from: protected */
    public Class obtainFontFamily() throws ClassNotFoundException {
        return Class.forName(FONT_FAMILY_CLASS);
    }

    /* access modifiers changed from: protected */
    public Constructor obtainFontFamilyCtor(Class fontFamily) throws NoSuchMethodException {
        return fontFamily.getConstructor(new Class[0]);
    }

    /* access modifiers changed from: protected */
    public Method obtainAddFontFromAssetManagerMethod(Class fontFamily) throws NoSuchMethodException {
        return fontFamily.getMethod(ADD_FONT_FROM_ASSET_MANAGER_METHOD, AssetManager.class, String.class, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, FontVariationAxis[].class);
    }

    /* access modifiers changed from: protected */
    public Method obtainAddFontFromBufferMethod(Class fontFamily) throws NoSuchMethodException {
        return fontFamily.getMethod(ADD_FONT_FROM_BUFFER_METHOD, ByteBuffer.class, Integer.TYPE, FontVariationAxis[].class, Integer.TYPE, Integer.TYPE);
    }

    /* access modifiers changed from: protected */
    public Method obtainFreezeMethod(Class fontFamily) throws NoSuchMethodException {
        return fontFamily.getMethod(FREEZE_METHOD, new Class[0]);
    }

    /* access modifiers changed from: protected */
    public Method obtainAbortCreationMethod(Class fontFamily) throws NoSuchMethodException {
        return fontFamily.getMethod(ABORT_CREATION_METHOD, new Class[0]);
    }

    /* access modifiers changed from: protected */
    public Method obtainCreateFromFamiliesWithDefaultMethod(Class fontFamily) throws NoSuchMethodException {
        Method m = Typeface.class.getDeclaredMethod(CREATE_FROM_FAMILIES_WITH_DEFAULT_METHOD, Array.newInstance(fontFamily, 1).getClass(), Integer.TYPE, Integer.TYPE);
        m.setAccessible(true);
        return m;
    }
}
