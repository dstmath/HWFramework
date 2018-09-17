package android.graphics.fonts;

import android.net.ProxyInfo;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.regex.Pattern;

public final class FontVariationAxis {
    private static final Pattern STYLE_VALUE_PATTERN = Pattern.compile("-?(([0-9]+(\\.[0-9]+)?)|(\\.[0-9]+))");
    private static final Pattern TAG_PATTERN = Pattern.compile("[ -~]{4}");
    private final float mStyleValue;
    private final int mTag;
    private final String mTagString;

    public FontVariationAxis(String tagString, float styleValue) {
        if (isValidTag(tagString)) {
            this.mTag = makeTag(tagString);
            this.mTagString = tagString;
            this.mStyleValue = styleValue;
            return;
        }
        throw new IllegalArgumentException("Illegal tag pattern: " + tagString);
    }

    public int getOpenTypeTagValue() {
        return this.mTag;
    }

    public String getTag() {
        return this.mTagString;
    }

    public float getStyleValue() {
        return this.mStyleValue;
    }

    public String toString() {
        return "'" + this.mTagString + "' " + Float.toString(this.mStyleValue);
    }

    private static boolean isValidTag(String tagString) {
        return tagString != null ? TAG_PATTERN.matcher(tagString).matches() : false;
    }

    private static boolean isValidValueFormat(String valueString) {
        return valueString != null ? STYLE_VALUE_PATTERN.matcher(valueString).matches() : false;
    }

    public static int makeTag(String tagString) {
        return (((tagString.charAt(0) << 24) | (tagString.charAt(1) << 16)) | (tagString.charAt(2) << 8)) | tagString.charAt(3);
    }

    public static FontVariationAxis[] fromFontVariationSettings(String settings) {
        if (settings == null || settings.isEmpty()) {
            return null;
        }
        ArrayList<FontVariationAxis> axisList = new ArrayList();
        int length = settings.length();
        int i = 0;
        while (i < length) {
            char c = settings.charAt(i);
            if (!Character.isWhitespace(c)) {
                if ((c == '\'' || c == '\"') && length >= i + 6 && settings.charAt(i + 5) == c) {
                    String tagString = settings.substring(i + 1, i + 5);
                    i += 6;
                    int endOfValueString = settings.indexOf(44, i);
                    if (endOfValueString == -1) {
                        endOfValueString = length;
                    }
                    try {
                        axisList.add(new FontVariationAxis(tagString, Float.parseFloat(settings.substring(i, endOfValueString))));
                        i = endOfValueString;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Failed to parse float string: " + e.getMessage());
                    }
                }
                throw new IllegalArgumentException("Tag should be wrapped with double or single quote: " + settings);
            }
            i++;
        }
        if (axisList.isEmpty()) {
            return null;
        }
        return (FontVariationAxis[]) axisList.toArray(new FontVariationAxis[0]);
    }

    public static String toFontVariationSettings(FontVariationAxis[] axes) {
        if (axes == null || axes.length == 0) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        return TextUtils.join(",", axes);
    }
}
