package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.content.res.FontResourcesParserCompat;
import android.support.v4.media.MediaPlayer2;
import android.support.v4.provider.FontsContractCompat;
import com.huawei.nearbysdk.util.Version;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/* access modifiers changed from: package-private */
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class TypefaceCompatBaseImpl {
    private static final String CACHE_FILE_PREFIX = "cached_font_";
    private static final String TAG = "TypefaceCompatBaseImpl";

    /* access modifiers changed from: private */
    public interface StyleExtractor<T> {
        int getWeight(T t);

        boolean isItalic(T t);
    }

    TypefaceCompatBaseImpl() {
    }

    private static <T> T findBestFont(T[] fonts, int style, StyleExtractor<T> extractor) {
        int targetWeight = (style & 1) == 0 ? Version.DTCP_VER_400 : MediaPlayer2.MEDIA_INFO_VIDEO_TRACK_LAGGING;
        boolean isTargetItalic = (style & 2) != 0;
        int bestScore = Integer.MAX_VALUE;
        T best = null;
        for (T font : fonts) {
            int score = (Math.abs(extractor.getWeight(font) - targetWeight) * 2) + (extractor.isItalic(font) == isTargetItalic ? 0 : 1);
            if (best == null || bestScore > score) {
                best = font;
                bestScore = score;
            }
        }
        return best;
    }

    /* access modifiers changed from: protected */
    public FontsContractCompat.FontInfo findBestInfo(FontsContractCompat.FontInfo[] fonts, int style) {
        return (FontsContractCompat.FontInfo) findBestFont(fonts, style, new StyleExtractor<FontsContractCompat.FontInfo>() {
            /* class android.support.v4.graphics.TypefaceCompatBaseImpl.AnonymousClass1 */

            public int getWeight(FontsContractCompat.FontInfo info) {
                return info.getWeight();
            }

            public boolean isItalic(FontsContractCompat.FontInfo info) {
                return info.isItalic();
            }
        });
    }

    /* access modifiers changed from: protected */
    public Typeface createFromInputStream(Context context, InputStream is) {
        File tmpFile = TypefaceCompatUtil.getTempFile(context);
        if (tmpFile == null) {
            return null;
        }
        try {
            if (TypefaceCompatUtil.copyToFile(tmpFile, is)) {
                Typeface createFromFile = Typeface.createFromFile(tmpFile.getPath());
                tmpFile.delete();
                return createFromFile;
            }
        } catch (RuntimeException e) {
        } catch (Throwable th) {
            tmpFile.delete();
            throw th;
        }
        tmpFile.delete();
        return null;
    }

    public Typeface createFromFontInfo(Context context, @Nullable CancellationSignal cancellationSignal, @NonNull FontsContractCompat.FontInfo[] fonts, int style) {
        if (fonts.length < 1) {
            return null;
        }
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(findBestInfo(fonts, style).getUri());
            return createFromInputStream(context, is);
        } catch (IOException e) {
            return null;
        } finally {
            TypefaceCompatUtil.closeQuietly(is);
        }
    }

    private FontResourcesParserCompat.FontFileResourceEntry findBestEntry(FontResourcesParserCompat.FontFamilyFilesResourceEntry entry, int style) {
        return (FontResourcesParserCompat.FontFileResourceEntry) findBestFont(entry.getEntries(), style, new StyleExtractor<FontResourcesParserCompat.FontFileResourceEntry>() {
            /* class android.support.v4.graphics.TypefaceCompatBaseImpl.AnonymousClass2 */

            public int getWeight(FontResourcesParserCompat.FontFileResourceEntry entry) {
                return entry.getWeight();
            }

            public boolean isItalic(FontResourcesParserCompat.FontFileResourceEntry entry) {
                return entry.isItalic();
            }
        });
    }

    @Nullable
    public Typeface createFromFontFamilyFilesResourceEntry(Context context, FontResourcesParserCompat.FontFamilyFilesResourceEntry entry, Resources resources, int style) {
        FontResourcesParserCompat.FontFileResourceEntry best = findBestEntry(entry, style);
        if (best == null) {
            return null;
        }
        return TypefaceCompat.createFromResourcesFontFile(context, resources, best.getResourceId(), best.getFileName(), style);
    }

    @Nullable
    public Typeface createFromResourcesFontFile(Context context, Resources resources, int id, String path, int style) {
        File tmpFile = TypefaceCompatUtil.getTempFile(context);
        if (tmpFile == null) {
            return null;
        }
        try {
            if (TypefaceCompatUtil.copyToFile(tmpFile, resources, id)) {
                Typeface createFromFile = Typeface.createFromFile(tmpFile.getPath());
                tmpFile.delete();
                return createFromFile;
            }
        } catch (RuntimeException e) {
        } catch (Throwable th) {
            tmpFile.delete();
            throw th;
        }
        tmpFile.delete();
        return null;
    }
}
