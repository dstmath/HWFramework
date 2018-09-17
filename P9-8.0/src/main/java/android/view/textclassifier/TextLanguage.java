package android.view.textclassifier;

import com.android.internal.util.Preconditions;
import java.util.List;
import java.util.Locale;

public final class TextLanguage {
    private final int mEndIndex;
    private final EntityConfidence<Locale> mLanguageConfidence;
    private final List<Locale> mLanguages;
    private final int mStartIndex;

    public static final class Builder {
        private final int mEndIndex;
        private final EntityConfidence<Locale> mLanguageConfidence = new EntityConfidence();
        private final int mStartIndex;

        public Builder(int startIndex, int endIndex) {
            boolean z;
            boolean z2 = true;
            if (startIndex >= 0) {
                z = true;
            } else {
                z = false;
            }
            Preconditions.checkArgument(z);
            if (endIndex <= startIndex) {
                z2 = false;
            }
            Preconditions.checkArgument(z2);
            this.mStartIndex = startIndex;
            this.mEndIndex = endIndex;
        }

        public Builder setLanguage(Locale locale, float confidenceScore) {
            this.mLanguageConfidence.setEntityType(locale, confidenceScore);
            return this;
        }

        public TextLanguage build() {
            return new TextLanguage(this.mStartIndex, this.mEndIndex, this.mLanguageConfidence, null);
        }
    }

    /* synthetic */ TextLanguage(int startIndex, int endIndex, EntityConfidence languageConfidence, TextLanguage -this3) {
        this(startIndex, endIndex, languageConfidence);
    }

    private TextLanguage(int startIndex, int endIndex, EntityConfidence<Locale> languageConfidence) {
        this.mStartIndex = startIndex;
        this.mEndIndex = endIndex;
        this.mLanguageConfidence = new EntityConfidence(languageConfidence);
        this.mLanguages = this.mLanguageConfidence.getEntities();
    }

    public int getStartIndex() {
        return this.mStartIndex;
    }

    public int getEndIndex() {
        return this.mEndIndex;
    }

    public int getLanguageCount() {
        return this.mLanguages.size();
    }

    public Locale getLanguage(int index) {
        return (Locale) this.mLanguages.get(index);
    }

    public float getConfidenceScore(Locale language) {
        return this.mLanguageConfidence.getConfidenceScore(language);
    }

    public String toString() {
        return String.format("TextLanguage {%d, %d, %s}", new Object[]{Integer.valueOf(this.mStartIndex), Integer.valueOf(this.mEndIndex), this.mLanguageConfidence});
    }
}
