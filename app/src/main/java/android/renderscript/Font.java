package android.renderscript;

import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.os.Environment;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Font extends BaseObj {
    private static final /* synthetic */ int[] -android-renderscript-Font$StyleSwitchesValues = null;
    private static Map<String, FontFamily> sFontFamilyMap;
    private static final String[] sMonoNames = null;
    private static final String[] sSansNames = null;
    private static final String[] sSerifNames = null;

    private static class FontFamily {
        String mBoldFileName;
        String mBoldItalicFileName;
        String mItalicFileName;
        String[] mNames;
        String mNormalFileName;

        private FontFamily() {
        }
    }

    public enum Style {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.Font.Style.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.Font.Style.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.renderscript.Font.Style.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getandroid-renderscript-Font$StyleSwitchesValues() {
        if (-android-renderscript-Font$StyleSwitchesValues != null) {
            return -android-renderscript-Font$StyleSwitchesValues;
        }
        int[] iArr = new int[Style.values().length];
        try {
            iArr[Style.BOLD.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Style.BOLD_ITALIC.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Style.ITALIC.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Style.NORMAL.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-renderscript-Font$StyleSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.renderscript.Font.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.renderscript.Font.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.Font.<clinit>():void");
    }

    private static void addFamilyToMap(FontFamily family) {
        for (Object put : family.mNames) {
            sFontFamilyMap.put(put, family);
        }
    }

    private static void initFontFamilyMap() {
        sFontFamilyMap = new HashMap();
        FontFamily sansFamily = new FontFamily();
        sansFamily.mNames = sSansNames;
        sansFamily.mNormalFileName = "Roboto-Regular.ttf";
        sansFamily.mBoldFileName = "Roboto-Bold.ttf";
        sansFamily.mItalicFileName = "Roboto-Italic.ttf";
        sansFamily.mBoldItalicFileName = "Roboto-BoldItalic.ttf";
        addFamilyToMap(sansFamily);
        FontFamily serifFamily = new FontFamily();
        serifFamily.mNames = sSerifNames;
        serifFamily.mNormalFileName = "NotoSerif-Regular.ttf";
        serifFamily.mBoldFileName = "NotoSerif-Bold.ttf";
        serifFamily.mItalicFileName = "NotoSerif-Italic.ttf";
        serifFamily.mBoldItalicFileName = "NotoSerif-BoldItalic.ttf";
        addFamilyToMap(serifFamily);
        FontFamily monoFamily = new FontFamily();
        monoFamily.mNames = sMonoNames;
        monoFamily.mNormalFileName = "DroidSansMono.ttf";
        monoFamily.mBoldFileName = "DroidSansMono.ttf";
        monoFamily.mItalicFileName = "DroidSansMono.ttf";
        monoFamily.mBoldItalicFileName = "DroidSansMono.ttf";
        addFamilyToMap(monoFamily);
    }

    static String getFontFileName(String familyName, Style style) {
        FontFamily family = (FontFamily) sFontFamilyMap.get(familyName);
        if (family != null) {
            switch (-getandroid-renderscript-Font$StyleSwitchesValues()[style.ordinal()]) {
                case AudioState.ROUTE_EARPIECE /*1*/:
                    return family.mBoldFileName;
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    return family.mBoldItalicFileName;
                case Engine.DEFAULT_STREAM /*3*/:
                    return family.mItalicFileName;
                case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                    return family.mNormalFileName;
            }
        }
        return "DroidSans.ttf";
    }

    Font(long id, RenderScript rs) {
        super(id, rs);
        this.guard.open("destroy");
    }

    public static Font createFromFile(RenderScript rs, Resources res, String path, float pointSize) {
        rs.validate();
        long fontId = rs.nFontCreateFromFile(path, pointSize, res.getDisplayMetrics().densityDpi);
        if (fontId != 0) {
            return new Font(fontId, rs);
        }
        throw new RSRuntimeException("Unable to create font from file " + path);
    }

    public static Font createFromFile(RenderScript rs, Resources res, File path, float pointSize) {
        return createFromFile(rs, res, path.getAbsolutePath(), pointSize);
    }

    public static Font createFromAsset(RenderScript rs, Resources res, String path, float pointSize) {
        rs.validate();
        long fontId = rs.nFontCreateFromAsset(res.getAssets(), path, pointSize, res.getDisplayMetrics().densityDpi);
        if (fontId != 0) {
            return new Font(fontId, rs);
        }
        throw new RSRuntimeException("Unable to create font from asset " + path);
    }

    public static Font createFromResource(RenderScript rs, Resources res, int id, float pointSize) {
        String name = "R." + Integer.toString(id);
        rs.validate();
        try {
            InputStream is = res.openRawResource(id);
            int dpi = res.getDisplayMetrics().densityDpi;
            if (is instanceof AssetInputStream) {
                long fontId = rs.nFontCreateFromAssetStream(name, pointSize, dpi, ((AssetInputStream) is).getNativeAsset());
                if (fontId != 0) {
                    return new Font(fontId, rs);
                }
                throw new RSRuntimeException("Unable to create font from resource " + id);
            }
            throw new RSRuntimeException("Unsupported asset stream created");
        } catch (Exception e) {
            throw new RSRuntimeException("Unable to open resource " + id);
        }
    }

    public static Font create(RenderScript rs, Resources res, String familyName, Style fontStyle, float pointSize) {
        return createFromFile(rs, res, Environment.getRootDirectory().getAbsolutePath() + "/fonts/" + getFontFileName(familyName, fontStyle), pointSize);
    }
}
