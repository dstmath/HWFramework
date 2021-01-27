package android.text;

import android.annotation.UnsupportedAppUsage;
import android.graphics.fonts.FontVariationAxis;
import android.net.Uri;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class FontConfig {
    private final Alias[] mAliases;
    private final Family[] mFamilies;

    public FontConfig(Family[] families, Alias[] aliases) {
        this.mFamilies = families;
        this.mAliases = aliases;
    }

    @UnsupportedAppUsage
    public Family[] getFamilies() {
        return this.mFamilies;
    }

    public Alias[] getAliases() {
        return this.mAliases;
    }

    public static final class Font {
        private final FontVariationAxis[] mAxes;
        private final String mFallbackFor;
        private String mFontName;
        private final boolean mIsItalic;
        private final int mTtcIndex;
        private Uri mUri;
        private final int mWeight;

        public Font(String fontName, int ttcIndex, FontVariationAxis[] axes, int weight, boolean isItalic, String fallbackFor) {
            this.mFontName = fontName;
            this.mTtcIndex = ttcIndex;
            this.mAxes = axes;
            this.mWeight = weight;
            this.mIsItalic = isItalic;
            this.mFallbackFor = fallbackFor;
        }

        public String getFontName() {
            return this.mFontName;
        }

        public void setFontName(String fontName) {
            this.mFontName = fontName;
        }

        @UnsupportedAppUsage
        public int getTtcIndex() {
            return this.mTtcIndex;
        }

        @UnsupportedAppUsage
        public FontVariationAxis[] getAxes() {
            return this.mAxes;
        }

        @UnsupportedAppUsage
        public int getWeight() {
            return this.mWeight;
        }

        @UnsupportedAppUsage
        public boolean isItalic() {
            return this.mIsItalic;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public void setUri(Uri uri) {
            this.mUri = uri;
        }

        public String getFallbackFor() {
            return this.mFallbackFor;
        }
    }

    public static final class Alias {
        private final String mName;
        private final String mToName;
        private final int mWeight;

        public Alias(String name, String toName, int weight) {
            this.mName = name;
            this.mToName = toName;
            this.mWeight = weight;
        }

        public String getName() {
            return this.mName;
        }

        public String getToName() {
            return this.mToName;
        }

        public int getWeight() {
            return this.mWeight;
        }
    }

    public static final class Family {
        public static final int VARIANT_COMPACT = 1;
        public static final int VARIANT_DEFAULT = 0;
        public static final int VARIANT_ELEGANT = 2;
        private final Font[] mFonts;
        private final String mLanguages;
        private final String mName;
        private final int mVariant;

        @Retention(RetentionPolicy.SOURCE)
        public @interface Variant {
        }

        public Family(String name, Font[] fonts, String languages, int variant) {
            this.mName = name;
            this.mFonts = fonts;
            this.mLanguages = languages;
            this.mVariant = variant;
        }

        @UnsupportedAppUsage
        public String getName() {
            return this.mName;
        }

        @UnsupportedAppUsage
        public Font[] getFonts() {
            return this.mFonts;
        }

        public String getLanguages() {
            return this.mLanguages;
        }

        @UnsupportedAppUsage
        public int getVariant() {
            return this.mVariant;
        }
    }
}
