package android.text;

import android.graphics.fonts.FontVariationAxis;
import android.net.Uri;

public final class FontConfig {
    private final Alias[] mAliases;
    private final Family[] mFamilies;

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
        private final String mLanguage;
        private final String mName;
        private final int mVariant;

        public Family(String name, Font[] fonts, String language, int variant) {
            this.mName = name;
            this.mFonts = fonts;
            this.mLanguage = language;
            this.mVariant = variant;
        }

        public String getName() {
            return this.mName;
        }

        public Font[] getFonts() {
            return this.mFonts;
        }

        public String getLanguage() {
            return this.mLanguage;
        }

        public int getVariant() {
            return this.mVariant;
        }
    }

    public static final class Font {
        private final FontVariationAxis[] mAxes;
        private String mFontName;
        private final boolean mIsItalic;
        private final int mTtcIndex;
        private Uri mUri;
        private final int mWeight;

        public Font(String fontName, int ttcIndex, FontVariationAxis[] axes, int weight, boolean isItalic) {
            this.mFontName = fontName;
            this.mTtcIndex = ttcIndex;
            this.mAxes = axes;
            this.mWeight = weight;
            this.mIsItalic = isItalic;
        }

        public String getFontName() {
            return this.mFontName;
        }

        public void setFontName(String fontName) {
            this.mFontName = fontName;
        }

        public int getTtcIndex() {
            return this.mTtcIndex;
        }

        public FontVariationAxis[] getAxes() {
            return this.mAxes;
        }

        public int getWeight() {
            return this.mWeight;
        }

        public boolean isItalic() {
            return this.mIsItalic;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public void setUri(Uri uri) {
            this.mUri = uri;
        }
    }

    public FontConfig(Family[] families, Alias[] aliases) {
        this.mFamilies = families;
        this.mAliases = aliases;
    }

    public Family[] getFamilies() {
        return this.mFamilies;
    }

    public Alias[] getAliases() {
        return this.mAliases;
    }
}
