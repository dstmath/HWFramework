package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.content.res.FontResourcesParserCompat.FontFamilyFilesResourceEntry;
import android.support.v4.content.res.FontResourcesParserCompat.FontFileResourceEntry;
import android.support.v4.provider.FontsContractCompat.FontInfo;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map;

@RequiresApi(14)
@RestrictTo({Scope.LIBRARY_GROUP})
class TypefaceCompatBaseImpl implements TypefaceCompatImpl {
    private static final String CACHE_FILE_PREFIX = "cached_font_";
    private static final String TAG = "TypefaceCompatBaseImpl";

    TypefaceCompatBaseImpl() {
    }

    public Typeface createTypeface(Context context, @NonNull FontInfo[] fonts, Map<Uri, ByteBuffer> uriBuffer) {
        if (fonts.length < 1) {
            return null;
        }
        ByteBuffer buffer = (ByteBuffer) uriBuffer.get(fonts[0].getUri());
        File tmpFile = TypefaceCompatUtil.getTempFile(context);
        if (tmpFile == null) {
            return null;
        }
        try {
            if (!TypefaceCompatUtil.copyToFile(tmpFile, buffer)) {
                return null;
            }
            Typeface createFromFile = Typeface.createFromFile(tmpFile.getPath());
            tmpFile.delete();
            return createFromFile;
        } catch (RuntimeException e) {
            return null;
        } finally {
            tmpFile.delete();
        }
    }

    private FontFileResourceEntry findBestEntry(FontFamilyFilesResourceEntry entry, int targetWeight, boolean isTargetItalic) {
        FontFileResourceEntry bestEntry = null;
        int bestScore = Integer.MAX_VALUE;
        for (FontFileResourceEntry e : entry.getEntries()) {
            int score = (Math.abs(e.getWeight() - targetWeight) * 2) + (isTargetItalic == e.isItalic() ? 0 : 1);
            if (bestEntry == null || bestScore > score) {
                bestEntry = e;
                bestScore = score;
            }
        }
        return bestEntry;
    }

    @Nullable
    public Typeface createFromFontFamilyFilesResourceEntry(Context context, FontFamilyFilesResourceEntry entry, Resources resources, int style) {
        boolean z = false;
        int i = (style & 1) == 0 ? 400 : 700;
        if ((style & 2) != 0) {
            z = true;
        }
        FontFileResourceEntry best = findBestEntry(entry, i, z);
        if (best == null) {
            return null;
        }
        return TypefaceCompat.createFromResourcesFontFile(context, resources, best.getResourceId(), style);
    }
}
