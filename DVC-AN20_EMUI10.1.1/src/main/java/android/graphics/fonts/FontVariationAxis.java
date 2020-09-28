package android.graphics.fonts;

import android.annotation.UnsupportedAppUsage;
import android.telephony.SmsManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public final class FontVariationAxis {
    private static final Pattern STYLE_VALUE_PATTERN = Pattern.compile("-?(([0-9]+(\\.[0-9]+)?)|(\\.[0-9]+))");
    private static final Pattern TAG_PATTERN = Pattern.compile("[ -~]{4}");
    @UnsupportedAppUsage
    private final float mStyleValue;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
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
        return tagString != null && TAG_PATTERN.matcher(tagString).matches();
    }

    private static boolean isValidValueFormat(String valueString) {
        return valueString != null && STYLE_VALUE_PATTERN.matcher(valueString).matches();
    }

    public static int makeTag(String tagString) {
        return (tagString.charAt(0) << 24) | (tagString.charAt(1) << 16) | (tagString.charAt(2) << '\b') | tagString.charAt(3);
    }

    public static FontVariationAxis[] fromFontVariationSettings(String settings) {
        if (settings == null || settings.isEmpty()) {
            return null;
        }
        ArrayList<FontVariationAxis> axisList = new ArrayList<>();
        int length = settings.length();
        int i = 0;
        while (i < length) {
            char c = settings.charAt(i);
            if (!Character.isWhitespace(c)) {
                if ((c == '\'' || c == '\"') && length >= i + 6 && settings.charAt(i + 5) == c) {
                    String tagString = settings.substring(i + 1, i + 5);
                    int i2 = i + 6;
                    int endOfValueString = settings.indexOf(44, i2);
                    if (endOfValueString == -1) {
                        endOfValueString = length;
                    }
                    try {
                        axisList.add(new FontVariationAxis(tagString, Float.parseFloat(settings.substring(i2, endOfValueString))));
                        i = endOfValueString;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Failed to parse float string: " + e.getMessage());
                    }
                } else {
                    throw new IllegalArgumentException("Tag should be wrapped with double or single quote: " + settings);
                }
            }
            i++;
        }
        if (axisList.isEmpty()) {
            return null;
        }
        return (FontVariationAxis[]) axisList.toArray(new FontVariationAxis[0]);
    }

    public static String toFontVariationSettings(FontVariationAxis[] axes) {
        if (axes == null) {
            return "";
        }
        return TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, axes);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof FontVariationAxis)) {
            return false;
        }
        FontVariationAxis axis = (FontVariationAxis) o;
        if (axis.mTag == this.mTag && axis.mStyleValue == this.mStyleValue) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mTag), Float.valueOf(this.mStyleValue));
    }
}
