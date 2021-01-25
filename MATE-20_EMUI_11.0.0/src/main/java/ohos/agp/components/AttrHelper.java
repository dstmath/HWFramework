package ohos.agp.components;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbColor;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.image.PixelMapFactory;
import ohos.agp.utils.Color;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Theme;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AttrHelper {
    private static final float DEFAULT_DENSITY = 0.0f;
    private static final int DEFAULT_DENSITY_DPI = 160;
    private static final String DP = "dp";
    private static final Pattern NUM_PATTERN = Pattern.compile("[0-9|.]");
    private static final String PX = "px";
    private static final String SP = "sp";
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_AttrHelper");
    private static final Pattern UNIT_PATTERN = Pattern.compile("[A-Za-z]");
    private static float sDensity = DEFAULT_DENSITY;

    private static int dip2px(float f, float f2) {
        return (int) ((f * f2) + 0.5f);
    }

    public static float getDensity(Context context) {
        ResourceManager resourceManager;
        float f = sDensity;
        if (f != DEFAULT_DENSITY) {
            return f;
        }
        if (!(context == null || (resourceManager = context.getResourceManager()) == null || resourceManager.getDeviceCapability() == null)) {
            sDensity = ((float) resourceManager.getDeviceCapability().screenDensity) / 160.0f;
        }
        return sDensity;
    }

    public static int convertValueToInt(String str, int i) {
        if (str == null) {
            return i;
        }
        try {
            return Long.decode(str).intValue();
        } catch (NumberFormatException unused) {
            return i;
        }
    }

    public static boolean convertValueToBoolean(String str, boolean z) {
        return str == null ? z : !str.equalsIgnoreCase("false");
    }

    public static float convertValueToFloat(String str, float f) {
        if (str == null) {
            return f;
        }
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException unused) {
            return f;
        }
    }

    public static long convertValueToLong(String str, long j) {
        if (str == null) {
            return j;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException unused) {
            return j;
        }
    }

    public static Element convertValueToElement(String str) {
        try {
            ShapeElement shapeElement = new ShapeElement();
            shapeElement.setRgbColor(RgbColor.fromArgbInt(RgbPalette.parse(str)));
            return shapeElement;
        } catch (IllegalArgumentException unused) {
            if (isPathFormat(str)) {
                return new PixelMapElement(PixelMapFactory.createFromPath(str));
            }
            return null;
        }
    }

    private static boolean isPathFormat(String str) {
        try {
            Paths.get(str, new String[0]);
            return true;
        } catch (NullPointerException | InvalidPathException unused) {
            return false;
        }
    }

    public static Color convertValueToColor(String str) {
        return new Color(RgbPalette.parse(str));
    }

    public static int convertDimensionToPix(String str, float f, int i) {
        if (str == null) {
            return i;
        }
        String replaceAll = UNIT_PATTERN.matcher(str).replaceAll("");
        String lowerCase = NUM_PATTERN.matcher(str).replaceAll("").toLowerCase(Locale.getDefault());
        try {
            float parseFloat = Float.parseFloat(replaceAll);
            if ("".equals(lowerCase) || PX.equals(lowerCase) || parseFloat < DEFAULT_DENSITY || f == DEFAULT_DENSITY) {
                return (int) parseFloat;
            }
            if (DP.equals(lowerCase) || SP.equals(lowerCase)) {
                return dip2px(parseFloat, f);
            }
            return i;
        } catch (NumberFormatException unused) {
            return i;
        }
    }

    public static AttrSet mergeStyle(Context context, AttrSet attrSet, int i) {
        Theme theme;
        AttrSetImpl attrSetImpl = new AttrSetImpl(attrSet);
        if (context == null) {
            return attrSetImpl;
        }
        Theme theme2 = null;
        ResourceManager resourceManager = context.getResourceManager();
        if (!(resourceManager == null || i == 0)) {
            try {
                theme2 = resourceManager.getElement(i).getTheme();
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.debug(TAG, "no default theme!", new Object[0]);
            }
        }
        if (theme2 == null) {
            try {
                theme = context.getTheme();
            } catch (IllegalArgumentException unused2) {
                HiLog.error(TAG, "Context getTheme failed!", new Object[0]);
            }
        } else {
            theme = context.getCombinedTheme(theme2);
        }
        if (theme == null) {
            return attrSetImpl;
        }
        theme.getThemeHash().forEach(new BiConsumer(context) {
            /* class ohos.agp.components.$$Lambda$AttrHelper$FIlldv3eNdNE6yOahGVYsD0YDP4 */
            private final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                AttrHelper.lambda$mergeStyle$0(AttrSetImpl.this, this.f$1, (String) obj, (TypedAttribute) obj2);
            }
        });
        return attrSetImpl;
    }

    static /* synthetic */ void lambda$mergeStyle$0(AttrSetImpl attrSetImpl, Context context, String str, TypedAttribute typedAttribute) {
        if (!attrSetImpl.getAttr(str).isPresent()) {
            attrSetImpl.addAttr(new AttrImpl(str, typedAttribute, context));
        }
    }
}
