package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.content.res.FontResourcesParserCompat.FamilyResourceEntry;
import android.support.v4.content.res.FontResourcesParserCompat.FontFamilyFilesResourceEntry;
import android.support.v4.content.res.FontResourcesParserCompat.ProviderResourceEntry;
import android.support.v4.provider.FontsContractCompat;
import android.support.v4.provider.FontsContractCompat.FontInfo;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.TextView;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map;

@RestrictTo({Scope.LIBRARY_GROUP})
public class TypefaceCompat {
    private static final String TAG = "TypefaceCompat";
    private static final LruCache<String, Typeface> sTypefaceCache = new LruCache(16);
    private static final TypefaceCompatImpl sTypefaceCompatImpl;

    interface TypefaceCompatImpl {
        Typeface createFromFontFamilyFilesResourceEntry(Context context, FontFamilyFilesResourceEntry fontFamilyFilesResourceEntry, Resources resources, int i);

        Typeface createTypeface(Context context, @NonNull FontInfo[] fontInfoArr, Map<Uri, ByteBuffer> map);
    }

    static {
        if (VERSION.SDK_INT < 24) {
            sTypefaceCompatImpl = new TypefaceCompatBaseImpl();
        } else if (TypefaceCompatApi24Impl.isUsable()) {
            sTypefaceCompatImpl = new TypefaceCompatApi24Impl();
        } else {
            Log.w(TAG, "Unable to collect necessary private methods.Fallback to legacy implementation.");
            sTypefaceCompatImpl = new TypefaceCompatBaseImpl();
        }
    }

    private TypefaceCompat() {
    }

    public static Typeface findFromCache(Resources resources, int id, int style) {
        return (Typeface) sTypefaceCache.get(createResourceUid(resources, id, style));
    }

    private static String createResourceUid(Resources resources, int id, int style) {
        return resources.getResourcePackageName(id) + "-" + id + "-" + style;
    }

    public static Typeface createFromResourcesFamilyXml(Context context, FamilyResourceEntry entry, Resources resources, int id, int style, @Nullable TextView targetView) {
        Typeface typeface;
        if (entry instanceof ProviderResourceEntry) {
            ProviderResourceEntry providerEntry = (ProviderResourceEntry) entry;
            typeface = FontsContractCompat.getFontSync(context, providerEntry.getRequest(), targetView, providerEntry.getFetchStrategy(), providerEntry.getTimeout(), style);
        } else {
            typeface = sTypefaceCompatImpl.createFromFontFamilyFilesResourceEntry(context, (FontFamilyFilesResourceEntry) entry, resources, style);
        }
        if (typeface != null) {
            sTypefaceCache.put(createResourceUid(resources, id, style), typeface);
        }
        return typeface;
    }

    @Nullable
    public static Typeface createFromResourcesFontFile(Context context, Resources resources, int id, int style) {
        File tmpFile = TypefaceCompatUtil.getTempFile(context);
        if (tmpFile == null) {
            return null;
        }
        try {
            if (!TypefaceCompatUtil.copyToFile(tmpFile, resources, id)) {
                return null;
            }
            Typeface typeface = Typeface.createFromFile(tmpFile.getPath());
            if (typeface != null) {
                sTypefaceCache.put(createResourceUid(resources, id, style), typeface);
            }
            tmpFile.delete();
            return typeface;
        } catch (RuntimeException e) {
            return null;
        } finally {
            tmpFile.delete();
        }
    }

    public static Typeface createTypeface(Context context, @NonNull FontInfo[] fonts, Map<Uri, ByteBuffer> uriBuffer) {
        return sTypefaceCompatImpl.createTypeface(context, fonts, uriBuffer);
    }
}
