package ohos.accessibility;

import java.util.Locale;
import java.util.Objects;
import ohos.agp.text.Font;
import ohos.agp.utils.Color;

public class CaptionProperties {
    public static final float ACCESSIBILITY_CAPTION_FONT_EXTRA_SMALL = 1.0f;
    public static final float ACCESSIBILITY_CAPTION_FONT_SIZE_EXTRA_LARGE = 5.0f;
    public static final float ACCESSIBILITY_CAPTION_FONT_SIZE_LARGE = 4.0f;
    public static final float ACCESSIBILITY_CAPTION_FONT_SIZE_NORMAL = 3.0f;
    public static final float ACCESSIBILITY_CAPTION_FONT_SIZE_SMALL = 2.0f;
    public static final int CAPTION_PROPERTY_DEFAULT_VALUE = -100;
    public static final String CAPTION_PROP_BACKGROUND_COLOR = "BackgroundColor";
    public static final String CAPTION_PROP_EDGE_COLOR = "EdgeColor";
    public static final String CAPTION_PROP_EDGE_TYPE = "EdgeType";
    public static final String CAPTION_PROP_FOREGROUND_COLOR = "ForegroundColor";
    public static final String CAPTION_PROP_WINDOW_COLOR = "WindowColor";
    private static final int COLOR_MASK = 16776960;
    private static final int COLOR_MASK_LENGTH = 24;
    private static final int COLOR_NONE_OPAQUE = 255;
    public static final int EDGE_TYPE_BULGE = 3;
    public static final int EDGE_TYPE_DROP_SHADOW = 2;
    private static final int[] EDGE_TYPE_LIST = {0, 1, 2, 3, 4};
    public static final int EDGE_TYPE_NONE = 0;
    public static final int EDGE_TYPE_OUTLINE = 1;
    public static final int EDGE_TYPE_SUNKEN = 4;
    private int mBackgroundColor = -100;
    private int mEdgeColor = -100;
    private int mEdgeType = -100;
    private Font mFont;
    private String mFontFamilyName;
    private float mFontSizeType;
    private int mForegroundColor = -100;
    private boolean mIsEnabled;
    private Locale mLocale;
    private int mWindowColor = -100;

    private boolean hasColor(int i) {
        return (i >>> 24) != 0 || (COLOR_MASK & i) == 0;
    }

    private boolean hasUserSetValue(int i) {
        return i != -100;
    }

    public float getFontSizeType() {
        return this.mFontSizeType;
    }

    public Font getFont() {
        String str;
        if (this.mFont == null && (str = this.mFontFamilyName) != null && str.trim().length() > 0) {
            this.mFont = new Font.Builder(this.mFontFamilyName).build();
        }
        return this.mFont;
    }

    public boolean checkProperty(String str) {
        if (str == null) {
            return false;
        }
        char c = 65535;
        switch (str.hashCode()) {
            case -2092499066:
                if (str.equals(CAPTION_PROP_EDGE_COLOR)) {
                    c = 1;
                    break;
                }
                break;
            case -1638080576:
                if (str.equals(CAPTION_PROP_FOREGROUND_COLOR)) {
                    c = 3;
                    break;
                }
                break;
            case -738328269:
                if (str.equals(CAPTION_PROP_WINDOW_COLOR)) {
                    c = 4;
                    break;
                }
                break;
            case 290107061:
                if (str.equals(CAPTION_PROP_BACKGROUND_COLOR)) {
                    c = 0;
                    break;
                }
                break;
            case 1595584183:
                if (str.equals(CAPTION_PROP_EDGE_TYPE)) {
                    c = 2;
                    break;
                }
                break;
        }
        if (c == 0) {
            return hasUserSetValue(this.mBackgroundColor) && hasColor(this.mBackgroundColor);
        }
        if (c == 1) {
            return hasUserSetValue(this.mEdgeColor) && hasColor(this.mEdgeColor);
        }
        if (c != 2) {
            return c != 3 ? c == 4 && hasUserSetValue(this.mWindowColor) && hasColor(this.mWindowColor) : hasUserSetValue(this.mForegroundColor) && hasColor(this.mForegroundColor);
        }
        return hasUserSetValue(this.mEdgeType);
    }

    public int getBackgroundColor() {
        return checkProperty(CAPTION_PROP_BACKGROUND_COLOR) ? this.mBackgroundColor : Color.BLACK.getValue();
    }

    public int getEdgeColor() {
        return checkProperty(CAPTION_PROP_EDGE_COLOR) ? this.mEdgeColor : Color.BLACK.getValue();
    }

    public int getEdgeType() {
        if (checkProperty(CAPTION_PROP_EDGE_TYPE)) {
            return this.mEdgeType;
        }
        return 0;
    }

    public int getForegroundColor() {
        return checkProperty(CAPTION_PROP_FOREGROUND_COLOR) ? this.mForegroundColor : Color.WHITE.getValue();
    }

    public int getWindowColor() {
        if (checkProperty(CAPTION_PROP_WINDOW_COLOR)) {
            return this.mWindowColor;
        }
        return 255;
    }

    public void setFontSizeType(float f) {
        this.mFontSizeType = f;
    }

    public void setBackgroundColor(int i) {
        this.mBackgroundColor = i;
    }

    public void setEdgeColor(int i) {
        this.mEdgeColor = i;
    }

    public void setEdgeType(int i) {
        int i2 = 0;
        while (true) {
            int[] iArr = EDGE_TYPE_LIST;
            if (i2 >= iArr.length) {
                return;
            }
            if (i == iArr[i2]) {
                this.mEdgeType = i;
                return;
            }
            i2++;
        }
    }

    public void setForegroundColor(int i) {
        this.mForegroundColor = i;
    }

    public void setWindowColor(int i) {
        this.mWindowColor = i;
    }

    public void setLocale(Locale locale) {
        this.mLocale = locale;
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    public boolean getIsEnabled() {
        return this.mIsEnabled;
    }

    public void setIsEnabled(boolean z) {
        this.mIsEnabled = z;
    }

    public void setFontFamilyName(String str) {
        if (!Objects.equals(this.mFontFamilyName, str)) {
            this.mFontFamilyName = str;
            this.mFont = null;
        }
    }

    public String getFontFamilyName() {
        return this.mFontFamilyName;
    }
}
