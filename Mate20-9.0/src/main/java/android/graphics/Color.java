package android.graphics;

import android.graphics.ColorSpace;
import android.hardware.Camera;
import android.util.Half;
import com.android.internal.util.XmlUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.DoubleUnaryOperator;

public class Color {
    public static final int BLACK = -16777216;
    public static final int BLUE = -16776961;
    public static final int CYAN = -16711681;
    public static final int DKGRAY = -12303292;
    public static final int GRAY = -7829368;
    public static final int GREEN = -16711936;
    public static final int LTGRAY = -3355444;
    public static final int MAGENTA = -65281;
    public static final int RED = -65536;
    public static final int TRANSPARENT = 0;
    public static final int WHITE = -1;
    public static final int YELLOW = -256;
    private static final HashMap<String, Integer> sColorNameMap = new HashMap<>();
    private final ColorSpace mColorSpace;
    private final float[] mComponents;

    private static native int nativeHSVToColor(int i, float[] fArr);

    private static native void nativeRGBToHSV(int i, int i2, int i3, float[] fArr);

    public Color() {
        this.mComponents = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        this.mColorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
    }

    private Color(float r, float g, float b, float a) {
        this(r, g, b, a, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    private Color(float r, float g, float b, float a, ColorSpace colorSpace) {
        this.mComponents = new float[]{r, g, b, a};
        this.mColorSpace = colorSpace;
    }

    private Color(float[] components, ColorSpace colorSpace) {
        this.mComponents = components;
        this.mColorSpace = colorSpace;
    }

    public ColorSpace getColorSpace() {
        return this.mColorSpace;
    }

    public ColorSpace.Model getModel() {
        return this.mColorSpace.getModel();
    }

    public boolean isWideGamut() {
        return getColorSpace().isWideGamut();
    }

    public boolean isSrgb() {
        return getColorSpace().isSrgb();
    }

    public int getComponentCount() {
        return this.mColorSpace.getComponentCount() + 1;
    }

    public long pack() {
        return pack(this.mComponents[0], this.mComponents[1], this.mComponents[2], this.mComponents[3], this.mColorSpace);
    }

    public Color convert(ColorSpace colorSpace) {
        ColorSpace.Connector connector = ColorSpace.connect(this.mColorSpace, colorSpace);
        float[] color = {this.mComponents[0], this.mComponents[1], this.mComponents[2], this.mComponents[3]};
        connector.transform(color);
        return new Color(color, colorSpace);
    }

    public int toArgb() {
        if (this.mColorSpace.isSrgb()) {
            return (((int) ((this.mComponents[3] * 255.0f) + 0.5f)) << 24) | (((int) ((this.mComponents[0] * 255.0f) + 0.5f)) << 16) | (((int) ((this.mComponents[1] * 255.0f) + 0.5f)) << 8) | ((int) ((this.mComponents[2] * 255.0f) + 0.5f));
        }
        float[] color = {this.mComponents[0], this.mComponents[1], this.mComponents[2], this.mComponents[3]};
        ColorSpace.connect(this.mColorSpace).transform(color);
        int i = ((int) ((color[0] * 255.0f) + 0.5f)) << 16;
        int i2 = ((int) ((color[1] * 255.0f) + 0.5f)) << 8;
        return ((int) ((color[2] * 255.0f) + 0.5f)) | i2 | i | (((int) ((color[3] * 255.0f) + 0.5f)) << 24);
    }

    public float red() {
        return this.mComponents[0];
    }

    public float green() {
        return this.mComponents[1];
    }

    public float blue() {
        return this.mComponents[2];
    }

    public float alpha() {
        return this.mComponents[this.mComponents.length - 1];
    }

    public float[] getComponents() {
        return Arrays.copyOf(this.mComponents, this.mComponents.length);
    }

    public float[] getComponents(float[] components) {
        if (components == null) {
            return Arrays.copyOf(this.mComponents, this.mComponents.length);
        }
        if (components.length >= this.mComponents.length) {
            System.arraycopy(this.mComponents, 0, components, 0, this.mComponents.length);
            return components;
        }
        throw new IllegalArgumentException("The specified array's length must be at least " + this.mComponents.length);
    }

    public float getComponent(int component) {
        return this.mComponents[component];
    }

    public float luminance() {
        if (this.mColorSpace.getModel() == ColorSpace.Model.RGB) {
            DoubleUnaryOperator eotf = ((ColorSpace.Rgb) this.mColorSpace).getEotf();
            return saturate((float) ((0.2126d * eotf.applyAsDouble((double) this.mComponents[0])) + (0.7152d * eotf.applyAsDouble((double) this.mComponents[1])) + (0.0722d * eotf.applyAsDouble((double) this.mComponents[2]))));
        }
        throw new IllegalArgumentException("The specified color must be encoded in an RGB color space. The supplied color space is " + this.mColorSpace.getModel());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Color color = (Color) o;
        if (!Arrays.equals(this.mComponents, color.mComponents)) {
            return false;
        }
        return this.mColorSpace.equals(color.mColorSpace);
    }

    public int hashCode() {
        return (31 * Arrays.hashCode(this.mComponents)) + this.mColorSpace.hashCode();
    }

    public String toString() {
        StringBuilder b = new StringBuilder("Color(");
        for (float c : this.mComponents) {
            b.append(c);
            b.append(", ");
        }
        b.append(this.mColorSpace.getName());
        b.append(')');
        return b.toString();
    }

    public static ColorSpace colorSpace(long color) {
        return ColorSpace.get((int) (63 & color));
    }

    public static float red(long color) {
        if ((63 & color) == 0) {
            return ((float) ((color >> 48) & 255)) / 255.0f;
        }
        return Half.toFloat((short) ((int) ((color >> 48) & 65535)));
    }

    public static float green(long color) {
        if ((63 & color) == 0) {
            return ((float) ((color >> 40) & 255)) / 255.0f;
        }
        return Half.toFloat((short) ((int) ((color >> 32) & 65535)));
    }

    public static float blue(long color) {
        if ((63 & color) == 0) {
            return ((float) ((color >> 32) & 255)) / 255.0f;
        }
        return Half.toFloat((short) ((int) ((color >> 16) & 65535)));
    }

    public static float alpha(long color) {
        if ((63 & color) == 0) {
            return ((float) ((color >> 56) & 255)) / 255.0f;
        }
        return ((float) ((color >> 6) & 1023)) / 1023.0f;
    }

    public static boolean isSrgb(long color) {
        return colorSpace(color).isSrgb();
    }

    public static boolean isWideGamut(long color) {
        return colorSpace(color).isWideGamut();
    }

    public static boolean isInColorSpace(long color, ColorSpace colorSpace) {
        return ((int) (63 & color)) == colorSpace.getId();
    }

    public static int toArgb(long color) {
        if ((63 & color) == 0) {
            return (int) (color >> 32);
        }
        float r = red(color);
        float g = green(color);
        float b = blue(color);
        float a = alpha(color);
        float[] c = ColorSpace.connect(colorSpace(color)).transform(r, g, b);
        return ((int) ((c[2] * 255.0f) + 0.5f)) | (((int) ((a * 255.0f) + 0.5f)) << 24) | (((int) ((c[0] * 255.0f) + 0.5f)) << 16) | (((int) ((c[1] * 255.0f) + 0.5f)) << 8);
    }

    public static Color valueOf(int color) {
        Color color2 = new Color(((float) ((color >> 16) & 255)) / 255.0f, ((float) ((color >> 8) & 255)) / 255.0f, ((float) (color & 255)) / 255.0f, ((float) ((color >> 24) & 255)) / 255.0f, ColorSpace.get(ColorSpace.Named.SRGB));
        return color2;
    }

    public static Color valueOf(long color) {
        Color color2 = new Color(red(color), green(color), blue(color), alpha(color), colorSpace(color));
        return color2;
    }

    public static Color valueOf(float r, float g, float b) {
        return new Color(r, g, b, 1.0f);
    }

    public static Color valueOf(float r, float g, float b, float a) {
        return new Color(saturate(r), saturate(g), saturate(b), saturate(a));
    }

    public static Color valueOf(float r, float g, float b, float a, ColorSpace colorSpace) {
        if (colorSpace.getComponentCount() <= 3) {
            Color color = new Color(r, g, b, a, colorSpace);
            return color;
        }
        throw new IllegalArgumentException("The specified color space must use a color model with at most 3 color components");
    }

    public static Color valueOf(float[] components, ColorSpace colorSpace) {
        if (components.length >= colorSpace.getComponentCount() + 1) {
            return new Color(Arrays.copyOf(components, colorSpace.getComponentCount() + 1), colorSpace);
        }
        throw new IllegalArgumentException("Received a component array of length " + components.length + " but the color model requires " + (colorSpace.getComponentCount() + 1) + " (including alpha)");
    }

    public static long pack(int color) {
        return (((long) color) & 4294967295L) << 32;
    }

    public static long pack(float red, float green, float blue) {
        return pack(red, green, blue, 1.0f, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    public static long pack(float red, float green, float blue, float alpha) {
        return pack(red, green, blue, alpha, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    public static long pack(float red, float green, float blue, float alpha, ColorSpace colorSpace) {
        float f = alpha;
        if (colorSpace.isSrgb()) {
            return (((long) (((int) ((255.0f * blue) + 0.5f)) | (((((int) ((red * 255.0f) + 0.5f)) << 16) | (((int) ((f * 255.0f) + 0.5f)) << 24)) | (((int) ((green * 255.0f) + 0.5f)) << 8)))) & 4294967295L) << 32;
        }
        int id = colorSpace.getId();
        if (id == -1) {
            throw new IllegalArgumentException("Unknown color space, please use a color space returned by ColorSpace.get()");
        } else if (colorSpace.getComponentCount() <= 3) {
            short r = Half.toHalf(red);
            short g = Half.toHalf(green);
            return ((((long) g) & 65535) << 32) | ((((long) r) & 65535) << 48) | ((((long) Half.toHalf(blue)) & 65535) << 16) | ((((long) ((int) ((Math.max(0.0f, Math.min(f, 1.0f)) * 1023.0f) + 0.5f))) & 1023) << 6) | (((long) id) & 63);
        } else {
            throw new IllegalArgumentException("The color space must use a color model with at most 3 components");
        }
    }

    public static long convert(int color, ColorSpace colorSpace) {
        return convert(((float) ((color >> 16) & 255)) / 255.0f, ((float) ((color >> 8) & 255)) / 255.0f, ((float) (color & 255)) / 255.0f, ((float) ((color >> 24) & 255)) / 255.0f, ColorSpace.get(ColorSpace.Named.SRGB), colorSpace);
    }

    public static long convert(long color, ColorSpace colorSpace) {
        return convert(red(color), green(color), blue(color), alpha(color), colorSpace(color), colorSpace);
    }

    public static long convert(float r, float g, float b, float a, ColorSpace source, ColorSpace destination) {
        float[] c = ColorSpace.connect(source, destination).transform(r, g, b);
        return pack(c[0], c[1], c[2], a, destination);
    }

    public static long convert(long color, ColorSpace.Connector connector) {
        return convert(red(color), green(color), blue(color), alpha(color), connector);
    }

    public static long convert(float r, float g, float b, float a, ColorSpace.Connector connector) {
        float[] c = connector.transform(r, g, b);
        return pack(c[0], c[1], c[2], a, connector.getDestination());
    }

    public static float luminance(long color) {
        ColorSpace colorSpace = colorSpace(color);
        if (colorSpace.getModel() == ColorSpace.Model.RGB) {
            DoubleUnaryOperator eotf = ((ColorSpace.Rgb) colorSpace).getEotf();
            return saturate((float) ((0.2126d * eotf.applyAsDouble((double) red(color))) + (0.7152d * eotf.applyAsDouble((double) green(color))) + (0.0722d * eotf.applyAsDouble((double) blue(color)))));
        }
        throw new IllegalArgumentException("The specified color must be encoded in an RGB color space. The supplied color space is " + colorSpace.getModel());
    }

    private static float saturate(float v) {
        if (v <= 0.0f) {
            return 0.0f;
        }
        if (v >= 1.0f) {
            return 1.0f;
        }
        return v;
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return (color >> 16) & 255;
    }

    public static int green(int color) {
        return (color >> 8) & 255;
    }

    public static int blue(int color) {
        return color & 255;
    }

    public static int rgb(int red, int green, int blue) {
        return (red << 16) | -16777216 | (green << 8) | blue;
    }

    public static int rgb(float red, float green, float blue) {
        return ((int) ((255.0f * blue) + 0.5f)) | (((int) ((red * 255.0f) + 0.5f)) << 16) | -16777216 | (((int) ((green * 255.0f) + 0.5f)) << 8);
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int argb(float alpha, float red, float green, float blue) {
        return ((int) ((255.0f * blue) + 0.5f)) | (((int) ((alpha * 255.0f) + 0.5f)) << 24) | (((int) ((red * 255.0f) + 0.5f)) << 16) | (((int) ((green * 255.0f) + 0.5f)) << 8);
    }

    public static float luminance(int color) {
        DoubleUnaryOperator eotf = ((ColorSpace.Rgb) ColorSpace.get(ColorSpace.Named.SRGB)).getEotf();
        return (float) ((0.2126d * eotf.applyAsDouble(((double) red(color)) / 255.0d)) + (0.7152d * eotf.applyAsDouble(((double) green(color)) / 255.0d)) + (0.0722d * eotf.applyAsDouble(((double) blue(color)) / 255.0d)));
    }

    public static int parseColor(String colorString) {
        if (colorString.charAt(0) == '#') {
            long color = Long.parseLong(colorString.substring(1), 16);
            if (colorString.length() == 7) {
                color |= -16777216;
            } else if (colorString.length() != 9) {
                throw new IllegalArgumentException("Unknown color");
            }
            return (int) color;
        }
        Integer color2 = sColorNameMap.get(colorString.toLowerCase(Locale.ROOT));
        if (color2 != null) {
            return color2.intValue();
        }
        throw new IllegalArgumentException("Unknown color");
    }

    public static void RGBToHSV(int red, int green, int blue, float[] hsv) {
        if (hsv.length >= 3) {
            nativeRGBToHSV(red, green, blue, hsv);
            return;
        }
        throw new RuntimeException("3 components required for hsv");
    }

    public static void colorToHSV(int color, float[] hsv) {
        RGBToHSV((color >> 16) & 255, (color >> 8) & 255, color & 255, hsv);
    }

    public static int HSVToColor(float[] hsv) {
        return HSVToColor(255, hsv);
    }

    public static int HSVToColor(int alpha, float[] hsv) {
        if (hsv.length >= 3) {
            return nativeHSVToColor(alpha, hsv);
        }
        throw new RuntimeException("3 components required for hsv");
    }

    public static int getHtmlColor(String color) {
        Integer i = sColorNameMap.get(color.toLowerCase(Locale.ROOT));
        if (i != null) {
            return i.intValue();
        }
        try {
            return XmlUtils.convertValueToInt(color, -1);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static {
        sColorNameMap.put("black", -16777216);
        sColorNameMap.put("darkgray", Integer.valueOf(DKGRAY));
        sColorNameMap.put("gray", Integer.valueOf(GRAY));
        sColorNameMap.put("lightgray", Integer.valueOf(LTGRAY));
        sColorNameMap.put("white", -1);
        sColorNameMap.put("red", Integer.valueOf(RED));
        sColorNameMap.put("green", Integer.valueOf(GREEN));
        sColorNameMap.put("blue", Integer.valueOf(BLUE));
        sColorNameMap.put("yellow", Integer.valueOf(YELLOW));
        sColorNameMap.put("cyan", Integer.valueOf(CYAN));
        sColorNameMap.put("magenta", Integer.valueOf(MAGENTA));
        sColorNameMap.put(Camera.Parameters.EFFECT_AQUA, Integer.valueOf(CYAN));
        sColorNameMap.put("fuchsia", Integer.valueOf(MAGENTA));
        sColorNameMap.put("darkgrey", Integer.valueOf(DKGRAY));
        sColorNameMap.put("grey", Integer.valueOf(GRAY));
        sColorNameMap.put("lightgrey", Integer.valueOf(LTGRAY));
        sColorNameMap.put("lime", Integer.valueOf(GREEN));
        sColorNameMap.put("maroon", -8388608);
        sColorNameMap.put("navy", -16777088);
        sColorNameMap.put("olive", -8355840);
        sColorNameMap.put("purple", -8388480);
        sColorNameMap.put("silver", -4144960);
        sColorNameMap.put("teal", -16744320);
    }
}
