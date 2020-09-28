package com.android.internal.colorextraction.types;

import android.app.WallpaperColors;
import android.app.slice.Slice;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiEnterpriseConfig;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.MathUtils;
import android.util.Range;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.graphics.ColorUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Tonal implements ExtractionType {
    private static final boolean DEBUG = true;
    private static final float FIT_WEIGHT_H = 1.0f;
    private static final float FIT_WEIGHT_L = 10.0f;
    private static final float FIT_WEIGHT_S = 1.0f;
    public static final int MAIN_COLOR_DARK = -14671580;
    public static final int MAIN_COLOR_LIGHT = -2433824;
    public static final int MAIN_COLOR_REGULAR = -16777216;
    private static final String TAG = "Tonal";
    private final Context mContext;
    private final TonalPalette mGreyPalette;
    private float[] mTmpHSL = new float[3];
    private final ArrayList<TonalPalette> mTonalPalettes;

    public Tonal(Context context) {
        this.mTonalPalettes = new ConfigParser(context).getTonalPalettes();
        this.mContext = context;
        this.mGreyPalette = this.mTonalPalettes.get(0);
        this.mTonalPalettes.remove(0);
    }

    @Override // com.android.internal.colorextraction.types.ExtractionType
    public void extractInto(WallpaperColors inWallpaperColors, ColorExtractor.GradientColors outColorsNormal, ColorExtractor.GradientColors outColorsDark, ColorExtractor.GradientColors outColorsExtraDark) {
        if (!runTonalExtraction(inWallpaperColors, outColorsNormal, outColorsDark, outColorsExtraDark)) {
            applyFallback(inWallpaperColors, outColorsNormal, outColorsDark, outColorsExtraDark);
        }
    }

    private boolean runTonalExtraction(WallpaperColors inWallpaperColors, ColorExtractor.GradientColors outColorsNormal, ColorExtractor.GradientColors outColorsDark, ColorExtractor.GradientColors outColorsExtraDark) {
        int primaryIndex;
        int primaryIndex2;
        if (inWallpaperColors == null) {
            return false;
        }
        List<Color> mainColors = inWallpaperColors.getMainColors();
        int mainColorsSize = mainColors.size();
        boolean supportsDarkText = (inWallpaperColors.getColorHints() & 1) != 0;
        if (mainColorsSize == 0) {
            return false;
        }
        int colorValue = mainColors.get(0).toArgb();
        float[] hsl = new float[3];
        ColorUtils.RGBToHSL(Color.red(colorValue), Color.green(colorValue), Color.blue(colorValue), hsl);
        hsl[0] = hsl[0] / 360.0f;
        TonalPalette palette = findTonalPalette(hsl[0], hsl[1]);
        if (palette == null) {
            Log.w(TAG, "Could not find a tonal palette!");
            return false;
        }
        int fitIndex = bestFit(palette, hsl[0], hsl[1], hsl[2]);
        if (fitIndex == -1) {
            Log.w(TAG, "Could not find best fit!");
            return false;
        }
        float[] h = fit(palette.h, hsl[0], fitIndex, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        float[] s = fit(palette.s, hsl[1], fitIndex, 0.0f, 1.0f);
        float[] l = fit(palette.l, hsl[2], fitIndex, 0.0f, 1.0f);
        int[] colorPalette = getColorPalette(h, s, l);
        StringBuilder builder = new StringBuilder("Tonal Palette - index: " + fitIndex + ". Main color: " + Integer.toHexString(getColorInt(fitIndex, h, s, l)) + "\nColors: ");
        for (int i = 0; i < h.length; i++) {
            builder.append(Integer.toHexString(getColorInt(i, h, s, l)));
            if (i < h.length - 1) {
                builder.append(", ");
            }
        }
        Log.d(TAG, builder.toString());
        int mainColor = getColorInt(fitIndex, h, s, l);
        ColorUtils.colorToHSL(mainColor, this.mTmpHSL);
        float[] fArr = this.mTmpHSL;
        float mainLuminosity = fArr[2];
        ColorUtils.colorToHSL(MAIN_COLOR_LIGHT, fArr);
        float[] fArr2 = this.mTmpHSL;
        if (mainLuminosity > fArr2[2]) {
            return false;
        }
        ColorUtils.colorToHSL(MAIN_COLOR_DARK, fArr2);
        if (mainLuminosity < this.mTmpHSL[2]) {
            return false;
        }
        outColorsNormal.setMainColor(mainColor);
        outColorsNormal.setSecondaryColor(mainColor);
        outColorsNormal.setColorPalette(colorPalette);
        if (supportsDarkText) {
            primaryIndex = h.length - 1;
        } else if (fitIndex < 2) {
            primaryIndex = 0;
        } else {
            primaryIndex = Math.min(fitIndex, 3);
        }
        int mainColor2 = getColorInt(primaryIndex, h, s, l);
        outColorsDark.setMainColor(mainColor2);
        outColorsDark.setSecondaryColor(mainColor2);
        outColorsDark.setColorPalette(colorPalette);
        if (supportsDarkText) {
            primaryIndex2 = h.length - 1;
        } else if (fitIndex < 2) {
            primaryIndex2 = 0;
        } else {
            primaryIndex2 = 2;
        }
        int mainColor3 = getColorInt(primaryIndex2, h, s, l);
        outColorsExtraDark.setMainColor(mainColor3);
        outColorsExtraDark.setSecondaryColor(mainColor3);
        outColorsExtraDark.setColorPalette(colorPalette);
        outColorsNormal.setSupportsDarkText(supportsDarkText);
        outColorsDark.setSupportsDarkText(supportsDarkText);
        outColorsExtraDark.setSupportsDarkText(supportsDarkText);
        Log.d(TAG, "Gradients: \n\tNormal " + outColorsNormal + "\n\tDark " + outColorsDark + "\n\tExtra dark: " + outColorsExtraDark);
        return true;
    }

    private void applyFallback(WallpaperColors inWallpaperColors, ColorExtractor.GradientColors outColorsNormal, ColorExtractor.GradientColors outColorsDark, ColorExtractor.GradientColors outColorsExtraDark) {
        applyFallback(inWallpaperColors, outColorsNormal);
        applyFallback(inWallpaperColors, outColorsDark);
        applyFallback(inWallpaperColors, outColorsExtraDark);
    }

    public void applyFallback(WallpaperColors inWallpaperColors, ColorExtractor.GradientColors outGradientColors) {
        int color;
        boolean light = (inWallpaperColors == null || (inWallpaperColors.getColorHints() & 1) == 0) ? false : true;
        boolean dark = (inWallpaperColors == null || (inWallpaperColors.getColorHints() & 2) == 0) ? false : true;
        boolean inNightMode = (this.mContext.getResources().getConfiguration().uiMode & 48) == 32;
        if (light) {
            color = MAIN_COLOR_LIGHT;
        } else if (dark || inNightMode) {
            color = MAIN_COLOR_DARK;
        } else {
            color = -16777216;
        }
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        outGradientColors.setMainColor(color);
        outGradientColors.setSecondaryColor(color);
        outGradientColors.setSupportsDarkText(light);
        outGradientColors.setColorPalette(getColorPalette(findTonalPalette(hsl[0], hsl[1])));
    }

    private int getColorInt(int fitIndex, float[] h, float[] s, float[] l) {
        this.mTmpHSL[0] = fract(h[fitIndex]) * 360.0f;
        float[] fArr = this.mTmpHSL;
        fArr[1] = s[fitIndex];
        fArr[2] = l[fitIndex];
        return ColorUtils.HSLToColor(fArr);
    }

    private int[] getColorPalette(float[] h, float[] s, float[] l) {
        int[] colorPalette = new int[h.length];
        for (int i = 0; i < colorPalette.length; i++) {
            colorPalette[i] = getColorInt(i, h, s, l);
        }
        return colorPalette;
    }

    private int[] getColorPalette(TonalPalette palette) {
        return getColorPalette(palette.h, palette.s, palette.l);
    }

    private static float[] fit(float[] data, float v, int index, float min, float max) {
        float[] fitData = new float[data.length];
        float delta = v - data[index];
        for (int i = 0; i < data.length; i++) {
            fitData[i] = MathUtils.constrain(data[i] + delta, min, max);
        }
        return fitData;
    }

    private static int bestFit(TonalPalette palette, float h, float s, float l) {
        int minErrorIndex = -1;
        float minError = Float.POSITIVE_INFINITY;
        for (int i = 0; i < palette.h.length; i++) {
            float error = (Math.abs(h - palette.h[i]) * 1.0f) + (Math.abs(s - palette.s[i]) * 1.0f) + (Math.abs(l - palette.l[i]) * FIT_WEIGHT_L);
            if (error < minError) {
                minError = error;
                minErrorIndex = i;
            }
        }
        return minErrorIndex;
    }

    private TonalPalette findTonalPalette(float h, float s) {
        if (s < 0.05f) {
            return this.mGreyPalette;
        }
        TonalPalette best = null;
        float error = Float.POSITIVE_INFINITY;
        int tonalPalettesCount = this.mTonalPalettes.size();
        for (int i = 0; i < tonalPalettesCount; i++) {
            TonalPalette candidate = this.mTonalPalettes.get(i);
            if (h >= candidate.minHue && h <= candidate.maxHue) {
                return candidate;
            }
            if (candidate.maxHue > 1.0f && h >= 0.0f && h <= fract(candidate.maxHue)) {
                return candidate;
            }
            if (candidate.minHue < 0.0f && h >= fract(candidate.minHue) && h <= 1.0f) {
                return candidate;
            }
            if (h <= candidate.minHue && candidate.minHue - h < error) {
                best = candidate;
                error = candidate.minHue - h;
            } else if (h >= candidate.maxHue && h - candidate.maxHue < error) {
                best = candidate;
                error = h - candidate.maxHue;
            } else if (candidate.maxHue > 1.0f && h >= fract(candidate.maxHue) && h - fract(candidate.maxHue) < error) {
                best = candidate;
                error = h - fract(candidate.maxHue);
            } else if (candidate.minHue < 0.0f && h <= fract(candidate.minHue) && fract(candidate.minHue) - h < error) {
                best = candidate;
                error = fract(candidate.minHue) - h;
            }
        }
        return best;
    }

    private static float fract(float v) {
        return v - ((float) Math.floor((double) v));
    }

    @VisibleForTesting
    public static class TonalPalette {
        public final float[] h;
        public final float[] l;
        public final float maxHue;
        public final float minHue;
        public final float[] s;

        TonalPalette(float[] h2, float[] s2, float[] l2) {
            if (h2.length == s2.length && s2.length == l2.length) {
                this.h = h2;
                this.s = s2;
                this.l = l2;
                float minHue2 = Float.POSITIVE_INFINITY;
                float maxHue2 = Float.NEGATIVE_INFINITY;
                for (float v : h2) {
                    minHue2 = Math.min(v, minHue2);
                    maxHue2 = Math.max(v, maxHue2);
                }
                this.minHue = minHue2;
                this.maxHue = maxHue2;
                return;
            }
            throw new IllegalArgumentException("All arrays should have the same size. h: " + Arrays.toString(h2) + " s: " + Arrays.toString(s2) + " l: " + Arrays.toString(l2));
        }
    }

    @VisibleForTesting
    public static class ColorRange {
        private Range<Float> mHue;
        private Range<Float> mLightness;
        private Range<Float> mSaturation;

        public ColorRange(Range<Float> hue, Range<Float> saturation, Range<Float> lightness) {
            this.mHue = hue;
            this.mSaturation = saturation;
            this.mLightness = lightness;
        }

        public boolean containsColor(float h, float s, float l) {
            if (this.mHue.contains(Float.valueOf(h)) && this.mSaturation.contains(Float.valueOf(s)) && this.mLightness.contains(Float.valueOf(l))) {
                return true;
            }
            return false;
        }

        public float[] getCenter() {
            return new float[]{this.mHue.getLower().floatValue() + ((this.mHue.getUpper().floatValue() - this.mHue.getLower().floatValue()) / 2.0f), this.mSaturation.getLower().floatValue() + ((this.mSaturation.getUpper().floatValue() - this.mSaturation.getLower().floatValue()) / 2.0f), this.mLightness.getLower().floatValue() + ((this.mLightness.getUpper().floatValue() - this.mLightness.getLower().floatValue()) / 2.0f)};
        }

        public String toString() {
            return String.format("H: %s, S: %s, L %s", this.mHue, this.mSaturation, this.mLightness);
        }
    }

    @VisibleForTesting
    public static class ConfigParser {
        private final ArrayList<TonalPalette> mTonalPalettes = new ArrayList<>();

        public ConfigParser(Context context) {
            try {
                XmlPullParser parser = context.getResources().getXml(R.xml.color_extraction);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0) {
                        if (eventType != 3) {
                            if (eventType != 2) {
                                throw new XmlPullParserException("Invalid XML event " + eventType + " - " + parser.getName(), parser, null);
                            } else if (parser.getName().equals("palettes")) {
                                parsePalettes(parser);
                            }
                        }
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                throw new RuntimeException(e);
            }
        }

        public ArrayList<TonalPalette> getTonalPalettes() {
            return this.mTonalPalettes;
        }

        private ColorRange readRange(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(2, null, Slice.SUBTYPE_RANGE);
            float[] h = readFloatArray(parser.getAttributeValue(null, "h"));
            float[] s = readFloatArray(parser.getAttributeValue(null, "s"));
            float[] l = readFloatArray(parser.getAttributeValue(null, "l"));
            if (h != null && s != null && l != null) {
                return new ColorRange(new Range(Float.valueOf(h[0]), Float.valueOf(h[1])), new Range(Float.valueOf(s[0]), Float.valueOf(s[1])), new Range(Float.valueOf(l[0]), Float.valueOf(l[1])));
            }
            throw new XmlPullParserException("Incomplete range tag.", parser, null);
        }

        private void parsePalettes(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(2, null, "palettes");
            while (parser.next() != 3) {
                if (parser.getEventType() == 2) {
                    String name = parser.getName();
                    if (name.equals("palette")) {
                        this.mTonalPalettes.add(readPalette(parser));
                        parser.next();
                    } else {
                        throw new XmlPullParserException("Invalid tag: " + name);
                    }
                }
            }
        }

        private TonalPalette readPalette(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(2, null, "palette");
            float[] h = readFloatArray(parser.getAttributeValue(null, "h"));
            float[] s = readFloatArray(parser.getAttributeValue(null, "s"));
            float[] l = readFloatArray(parser.getAttributeValue(null, "l"));
            if (h != null && s != null && l != null) {
                return new TonalPalette(h, s, l);
            }
            throw new XmlPullParserException("Incomplete range tag.", parser, null);
        }

        private float[] readFloatArray(String attributeValue) throws IOException, XmlPullParserException {
            String[] tokens = attributeValue.replaceAll(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, "").replaceAll("\n", "").split(SmsManager.REGEX_PREFIX_DELIMITER);
            float[] numbers = new float[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                numbers[i] = Float.parseFloat(tokens[i]);
            }
            return numbers;
        }
    }
}
