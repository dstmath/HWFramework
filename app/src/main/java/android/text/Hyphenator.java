package android.text;

import android.util.Log;
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
    private static final String[][] LOCALE_FALLBACK_DATA = null;
    private static String TAG;
    static final Hyphenator sEmptyHyphenator = null;
    private static final Object sLock = null;
    @GuardedBy("sLock")
    static final HashMap<Locale, Hyphenator> sMap = null;
    private final ByteBuffer mBuffer;
    private final long mNativePtr;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.Hyphenator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.Hyphenator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.Hyphenator.<clinit>():void");
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
                result = (Hyphenator) sMap.get(new Locale(locale.getLanguage(), "", variant));
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
            if (!script.equals("")) {
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

    private static Hyphenator loadHyphenator(String languageTag) {
        File patternFile = new File(getSystemHyphenatorLocation(), "hyph-" + languageTag.toLowerCase(Locale.US) + ".hyb");
        RandomAccessFile f;
        try {
            f = new RandomAccessFile(patternFile, "r");
            FileChannel fc = f.getChannel();
            MappedByteBuffer buf = fc.map(MapMode.READ_ONLY, 0, fc.size());
            Hyphenator hyphenator = new Hyphenator(StaticLayout.nLoadHyphenator(buf, 0), buf);
            f.close();
            return hyphenator;
        } catch (IOException e) {
            Log.e(TAG, "error loading hyphenation " + patternFile, e);
            return null;
        } catch (Throwable th) {
            f.close();
        }
    }

    private static File getSystemHyphenatorLocation() {
        return new File("/system/usr/hyphen-data");
    }

    public static void init() {
        int i;
        sMap.put(null, null);
        String[] availableLanguages = new String[]{"as", "bn", "cy", "da", "de-1901", "de-1996", "de-CH-1901", "en-GB", "en-US", "es", "et", "eu", "fr", "ga", "gu", "hi", "hr", "hu", "hy", "kn", "ml", "mn-Cyrl", "mr", "nb", "nn", "or", "pa", "pt", "sl", "ta", "te", "tk", "und-Ethi"};
        for (String languageTag : availableLanguages) {
            Hyphenator h = loadHyphenator(languageTag);
            if (h != null) {
                sMap.put(Locale.forLanguageTag(languageTag), h);
            }
        }
        for (i = 0; i < LOCALE_FALLBACK_DATA.length; i++) {
            sMap.put(Locale.forLanguageTag(LOCALE_FALLBACK_DATA[i][0]), (Hyphenator) sMap.get(Locale.forLanguageTag(LOCALE_FALLBACK_DATA[i][1])));
        }
    }
}
