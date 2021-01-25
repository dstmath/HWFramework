package ohos.agp.text;

import java.io.File;

public class Font {
    public static final int BOLD = 700;
    public static final Font DEFAULT = new Font(Typeface.DEFAULT);
    public static final Font DEFAULT_BOLD = new Font(Typeface.DEFAULT_BOLD);
    public static final int MEDIUM = 500;
    public static final Font MONOSPACE = new Font(Typeface.MONOSPACE);
    public static final int REGULAR = 400;
    public static final Font SANS_SERIF = new Font(Typeface.SANS_SERIF);
    public static final Font SERIF = new Font(Typeface.SERIF);
    private Typeface mTypeface;

    private Font(Typeface typeface) {
        this.mTypeface = typeface;
    }

    public static class Builder {
        private static final int MAX_FONT_WEIGHT = 1000;
        private static final int MIN_FONT_WEIGHT = 1;
        private boolean mIsItalic = false;
        private Typeface mTempTypeface = null;
        private int mWeight = -1;

        public Builder(String str) {
            this.mTempTypeface = Typeface.create(str, 0);
        }

        public Builder(File file) {
            this.mTempTypeface = Typeface.createFromFile(file);
        }

        public Builder makeItalic(boolean z) {
            this.mIsItalic = z;
            return this;
        }

        public Builder setWeight(int i) {
            if (i < 1) {
                this.mWeight = 1;
            } else if (i > 1000) {
                this.mWeight = 1000;
            } else {
                this.mWeight = i;
            }
            return this;
        }

        public Font build() {
            int i = this.mWeight;
            if (i == -1) {
                i = this.mTempTypeface.getWeight();
            }
            this.mTempTypeface = Typeface.create(this.mTempTypeface, i, this.mIsItalic);
            return new Font(this.mTempTypeface);
        }
    }

    public int getWeight() {
        return this.mTypeface.getWeight();
    }

    public boolean isItalic() {
        return this.mTypeface.isItalic();
    }

    public Typeface convertToTypeface() {
        return this.mTypeface;
    }
}
