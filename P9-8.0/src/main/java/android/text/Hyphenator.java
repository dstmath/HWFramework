package android.text;

import android.util.Log;
import android.util.LogException;
import com.android.internal.annotations.GuardedBy;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Locale.Builder;

public class Hyphenator {
    private static final HyphenationData[] AVAILABLE_LANGUAGES = new HyphenationData[]{new HyphenationData("as", 2, 2), new HyphenationData("bg", 2, 2), new HyphenationData("bn", 2, 2), new HyphenationData("cu", 1, 2), new HyphenationData("cy", 2, 3), new HyphenationData("da", 2, 2), new HyphenationData("de-1901", 2, 2), new HyphenationData("de-1996", 2, 2), new HyphenationData("de-CH-1901", 2, 2), new HyphenationData("en-GB", 2, 3), new HyphenationData("en-US", 2, 3), new HyphenationData("es", 2, 2), new HyphenationData("et", 2, 3), new HyphenationData("eu", 2, 2), new HyphenationData("fr", 2, 3), new HyphenationData("ga", 2, 3), new HyphenationData("gu", 2, 2), new HyphenationData("hi", 2, 2), new HyphenationData("hr", 2, 2), new HyphenationData("hu", 2, 2), new HyphenationData("hy", 2, 2), new HyphenationData("kn", 2, 2), new HyphenationData("ml", 2, 2), new HyphenationData("mn-Cyrl", 2, 2), new HyphenationData("mr", 2, 2), new HyphenationData("nb", 2, 2), new HyphenationData("nn", 2, 2), new HyphenationData("or", 2, 2), new HyphenationData("pa", 2, 2), new HyphenationData("pt", 2, 3), new HyphenationData("sl", 2, 2), new HyphenationData("ta", 2, 2), new HyphenationData("te", 2, 2), new HyphenationData("tk", 2, 2), new HyphenationData("und-Ethi", 1, 1)};
    private static final int DEFAULT_MIN_PREFIX = 2;
    private static final int DEFAULT_MIN_SUFFIX = 2;
    private static final int INDIC_MIN_PREFIX = 2;
    private static final int INDIC_MIN_SUFFIX = 2;
    private static final String[][] LOCALE_FALLBACK_DATA;
    private static String TAG = "Hyphenator";
    static final Hyphenator sEmptyHyphenator = new Hyphenator(StaticLayout.nLoadHyphenator(null, 0, 2, 2), null);
    private static final Object sLock = new Object();
    @GuardedBy("sLock")
    static final HashMap<Locale, Hyphenator> sMap = new HashMap();
    private final ByteBuffer mBuffer;
    private final long mNativePtr;

    private static class HyphenationData {
        final String mLanguageTag;
        final int mMinPrefix;
        final int mMinSuffix;

        HyphenationData(String languageTag, int minPrefix, int minSuffix) {
            this.mLanguageTag = languageTag;
            this.mMinPrefix = minPrefix;
            this.mMinSuffix = minSuffix;
        }
    }

    static {
        String[][] strArr = new String[17][];
        strArr[0] = new String[]{"en-AS", "en-US"};
        strArr[1] = new String[]{"en-GU", "en-US"};
        strArr[2] = new String[]{"en-MH", "en-US"};
        strArr[3] = new String[]{"en-MP", "en-US"};
        strArr[4] = new String[]{"en-PR", "en-US"};
        strArr[5] = new String[]{"en-UM", "en-US"};
        strArr[6] = new String[]{"en-VI", "en-US"};
        strArr[7] = new String[]{"en", "en-GB"};
        strArr[8] = new String[]{"de", "de-1996"};
        strArr[9] = new String[]{"de-LI-1901", "de-CH-1901"};
        strArr[10] = new String[]{"no", "nb"};
        strArr[11] = new String[]{"mn", "mn-Cyrl"};
        strArr[12] = new String[]{"am", "und-Ethi"};
        strArr[13] = new String[]{"byn", "und-Ethi"};
        strArr[14] = new String[]{"gez", "und-Ethi"};
        strArr[15] = new String[]{"ti", "und-Ethi"};
        strArr[16] = new String[]{"wal", "und-Ethi"};
        LOCALE_FALLBACK_DATA = strArr;
    }

    private Hyphenator(long nativePtr, ByteBuffer b) {
        this.mNativePtr = nativePtr;
        this.mBuffer = b;
    }

    public long getNativePtr() {
        return this.mNativePtr;
    }

    public static Hyphenator get(Locale locale) {
        synchronized (sLock) {
            Hyphenator result = (Hyphenator) sMap.get(locale);
            if (result != null) {
                return result;
            }
            String variant = locale.getVariant();
            if (!variant.isEmpty()) {
                result = (Hyphenator) sMap.get(new Locale(locale.getLanguage(), LogException.NO_VALUE, variant));
                if (result != null) {
                    sMap.put(locale, result);
                    return result;
                }
            }
            result = (Hyphenator) sMap.get(new Locale(locale.getLanguage()));
            if (result != null) {
                sMap.put(locale, result);
                return result;
            }
            String script = locale.getScript();
            if (!script.equals(LogException.NO_VALUE)) {
                result = (Hyphenator) sMap.get(new Builder().setLanguage("und").setScript(script).build());
                if (result != null) {
                    sMap.put(locale, result);
                    return result;
                }
            }
            sMap.put(locale, sEmptyHyphenator);
            return sEmptyHyphenator;
        }
    }

    private static Hyphenator loadHyphenator(HyphenationData data) {
        File patternFile = new File(getSystemHyphenatorLocation(), "hyph-" + data.mLanguageTag.toLowerCase(Locale.US) + ".hyb");
        if (patternFile.canRead()) {
            RandomAccessFile f;
            try {
                f = new RandomAccessFile(patternFile, "r");
                FileChannel fc = f.getChannel();
                MappedByteBuffer buf = fc.map(MapMode.READ_ONLY, 0, fc.size());
                Hyphenator hyphenator = new Hyphenator(StaticLayout.nLoadHyphenator(buf, 0, data.mMinPrefix, data.mMinSuffix), buf);
                f.close();
                return hyphenator;
            } catch (IOException e) {
                Log.e(TAG, "error loading hyphenation " + patternFile, e);
                return null;
            } catch (Throwable th) {
                f.close();
            }
        }
        Log.e(TAG, "hyphenation patterns for " + patternFile + " not found or unreadable");
        return null;
    }

    private static File getSystemHyphenatorLocation() {
        return new File("/system/usr/hyphen-data");
    }

    public static void init() {
        int i;
        sMap.put(null, null);
        for (HyphenationData data : AVAILABLE_LANGUAGES) {
            Hyphenator h = loadHyphenator(data);
            if (h != null) {
                sMap.put(Locale.forLanguageTag(data.mLanguageTag), h);
            }
        }
        for (i = 0; i < LOCALE_FALLBACK_DATA.length; i++) {
            sMap.put(Locale.forLanguageTag(LOCALE_FALLBACK_DATA[i][0]), (Hyphenator) sMap.get(Locale.forLanguageTag(LOCALE_FALLBACK_DATA[i][1])));
        }
    }
}
