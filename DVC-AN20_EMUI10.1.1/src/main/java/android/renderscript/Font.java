package android.renderscript;

import android.annotation.UnsupportedAppUsage;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Font extends BaseObj {
    private static Map<String, FontFamily> sFontFamilyMap;
    private static final String[] sMonoNames = {"monospace", "courier", "courier new", "monaco"};
    private static final String[] sSansNames = {"sans-serif", "arial", "helvetica", "tahoma", "verdana"};
    private static final String[] sSerifNames = {"serif", "times", "times new roman", "palatino", "georgia", "baskerville", "goudy", "fantasy", "cursive", "ITC Stone Serif"};

    public enum Style {
        NORMAL,
        BOLD,
        ITALIC,
        BOLD_ITALIC
    }

    static {
        initFontFamilyMap();
    }

    /* access modifiers changed from: private */
    public static class FontFamily {
        String mBoldFileName;
        String mBoldItalicFileName;
        String mItalicFileName;
        String[] mNames;
        String mNormalFileName;

        private FontFamily() {
        }

        /* synthetic */ FontFamily(AnonymousClass1 x0) {
            this();
        }
    }

    private static void addFamilyToMap(FontFamily family) {
        for (int i = 0; i < family.mNames.length; i++) {
            sFontFamilyMap.put(family.mNames[i], family);
        }
    }

    private static void initFontFamilyMap() {
        sFontFamilyMap = new HashMap();
        FontFamily sansFamily = new FontFamily(null);
        sansFamily.mNames = sSansNames;
        sansFamily.mNormalFileName = "Roboto-Regular.ttf";
        sansFamily.mBoldFileName = "Roboto-Bold.ttf";
        sansFamily.mItalicFileName = "Roboto-Italic.ttf";
        sansFamily.mBoldItalicFileName = "Roboto-BoldItalic.ttf";
        addFamilyToMap(sansFamily);
        FontFamily serifFamily = new FontFamily(null);
        serifFamily.mNames = sSerifNames;
        serifFamily.mNormalFileName = "NotoSerif-Regular.ttf";
        serifFamily.mBoldFileName = "NotoSerif-Bold.ttf";
        serifFamily.mItalicFileName = "NotoSerif-Italic.ttf";
        serifFamily.mBoldItalicFileName = "NotoSerif-BoldItalic.ttf";
        addFamilyToMap(serifFamily);
        FontFamily monoFamily = new FontFamily(null);
        monoFamily.mNames = sMonoNames;
        monoFamily.mNormalFileName = "DroidSansMono.ttf";
        monoFamily.mBoldFileName = "DroidSansMono.ttf";
        monoFamily.mItalicFileName = "DroidSansMono.ttf";
        monoFamily.mBoldItalicFileName = "DroidSansMono.ttf";
        addFamilyToMap(monoFamily);
    }

    static String getFontFileName(String familyName, Style style) {
        FontFamily family = sFontFamilyMap.get(familyName);
        if (family == null) {
            return "DroidSans.ttf";
        }
        int i = AnonymousClass1.$SwitchMap$android$renderscript$Font$Style[style.ordinal()];
        if (i == 1) {
            return family.mNormalFileName;
        }
        if (i == 2) {
            return family.mBoldFileName;
        }
        if (i == 3) {
            return family.mItalicFileName;
        }
        if (i != 4) {
            return "DroidSans.ttf";
        }
        return family.mBoldItalicFileName;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.renderscript.Font$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$renderscript$Font$Style = new int[Style.values().length];

        static {
            try {
                $SwitchMap$android$renderscript$Font$Style[Style.NORMAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$renderscript$Font$Style[Style.BOLD.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$renderscript$Font$Style[Style.ITALIC.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$renderscript$Font$Style[Style.BOLD_ITALIC.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
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
            if (is instanceof AssetManager.AssetInputStream) {
                long fontId = rs.nFontCreateFromAssetStream(name, pointSize, dpi, ((AssetManager.AssetInputStream) is).getNativeAsset());
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

    @UnsupportedAppUsage
    public static Font create(RenderScript rs, Resources res, String familyName, Style fontStyle, float pointSize) {
        String fileName = getFontFileName(familyName, fontStyle);
        String fontPath = Environment.getRootDirectory().getAbsolutePath();
        return createFromFile(rs, res, fontPath + "/fonts/" + fileName, pointSize);
    }
}
