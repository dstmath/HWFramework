package android.view;

import android.content.res.Configuration;
import android.view.ViewRootImpl.ConfigChangedCallback;
import java.text.BreakIterator;
import java.util.Locale;

public final class AccessibilityIterators {

    public interface TextSegmentIterator {
        int[] following(int i);

        int[] preceding(int i);
    }

    public static abstract class AbstractTextSegmentIterator implements TextSegmentIterator {
        private final int[] mSegment = new int[2];
        protected String mText;

        public void initialize(String text) {
            this.mText = text;
        }

        protected int[] getRange(int start, int end) {
            if (start < 0 || end < 0 || start == end) {
                return null;
            }
            this.mSegment[0] = start;
            this.mSegment[1] = end;
            return this.mSegment;
        }
    }

    static class CharacterTextSegmentIterator extends AbstractTextSegmentIterator implements ConfigChangedCallback {
        private static CharacterTextSegmentIterator sInstance;
        protected BreakIterator mImpl;
        private Locale mLocale;

        /* synthetic */ CharacterTextSegmentIterator(Locale locale, CharacterTextSegmentIterator -this1) {
            this(locale);
        }

        public static CharacterTextSegmentIterator getInstance(Locale locale) {
            if (sInstance == null) {
                sInstance = new CharacterTextSegmentIterator(locale);
            }
            return sInstance;
        }

        private CharacterTextSegmentIterator(Locale locale) {
            this.mLocale = locale;
            onLocaleChanged(locale);
            ViewRootImpl.addConfigCallback(this);
        }

        public void initialize(String text) {
            super.initialize(text);
            this.mImpl.setText(text);
        }

        public int[] following(int offset) {
            int textLegth = this.mText.length();
            if (textLegth <= 0 || offset >= textLegth) {
                return null;
            }
            int start = offset;
            if (offset < 0) {
                start = 0;
            }
            while (!this.mImpl.isBoundary(start)) {
                start = this.mImpl.following(start);
                if (start == -1) {
                    return null;
                }
            }
            int end = this.mImpl.following(start);
            if (end == -1) {
                return null;
            }
            return getRange(start, end);
        }

        public int[] preceding(int offset) {
            int textLegth = this.mText.length();
            if (textLegth <= 0 || offset <= 0) {
                return null;
            }
            int end = offset;
            if (offset > textLegth) {
                end = textLegth;
            }
            while (!this.mImpl.isBoundary(end)) {
                end = this.mImpl.preceding(end);
                if (end == -1) {
                    return null;
                }
            }
            int start = this.mImpl.preceding(end);
            if (start == -1) {
                return null;
            }
            return getRange(start, end);
        }

        public void onConfigurationChanged(Configuration globalConfig) {
            Locale locale = globalConfig.getLocales().get(0);
            if (!this.mLocale.equals(locale)) {
                this.mLocale = locale;
                onLocaleChanged(locale);
            }
        }

        protected void onLocaleChanged(Locale locale) {
            this.mImpl = BreakIterator.getCharacterInstance(locale);
        }
    }

    static class ParagraphTextSegmentIterator extends AbstractTextSegmentIterator {
        private static ParagraphTextSegmentIterator sInstance;

        ParagraphTextSegmentIterator() {
        }

        public static ParagraphTextSegmentIterator getInstance() {
            if (sInstance == null) {
                sInstance = new ParagraphTextSegmentIterator();
            }
            return sInstance;
        }

        public int[] following(int offset) {
            int textLength = this.mText.length();
            if (textLength <= 0 || offset >= textLength) {
                return null;
            }
            int start = offset;
            if (offset < 0) {
                start = 0;
            }
            while (start < textLength && this.mText.charAt(start) == 10 && (isStartBoundary(start) ^ 1) != 0) {
                start++;
            }
            if (start >= textLength) {
                return null;
            }
            int end = start + 1;
            while (end < textLength && (isEndBoundary(end) ^ 1) != 0) {
                end++;
            }
            return getRange(start, end);
        }

        public int[] preceding(int offset) {
            int textLength = this.mText.length();
            if (textLength <= 0 || offset <= 0) {
                return null;
            }
            int end = offset;
            if (offset > textLength) {
                end = textLength;
            }
            while (end > 0 && this.mText.charAt(end - 1) == 10 && (isEndBoundary(end) ^ 1) != 0) {
                end--;
            }
            if (end <= 0) {
                return null;
            }
            int start = end - 1;
            while (start > 0 && (isStartBoundary(start) ^ 1) != 0) {
                start--;
            }
            return getRange(start, end);
        }

        private boolean isStartBoundary(int index) {
            if (this.mText.charAt(index) != 10) {
                return index == 0 || this.mText.charAt(index - 1) == 10;
            } else {
                return false;
            }
        }

        private boolean isEndBoundary(int index) {
            if (index <= 0 || this.mText.charAt(index - 1) == 10) {
                return false;
            }
            return index == this.mText.length() || this.mText.charAt(index) == 10;
        }
    }

    static class WordTextSegmentIterator extends CharacterTextSegmentIterator {
        private static WordTextSegmentIterator sInstance;

        public static WordTextSegmentIterator getInstance(Locale locale) {
            if (sInstance == null) {
                sInstance = new WordTextSegmentIterator(locale);
            }
            return sInstance;
        }

        private WordTextSegmentIterator(Locale locale) {
            super(locale, null);
        }

        protected void onLocaleChanged(Locale locale) {
            this.mImpl = BreakIterator.getWordInstance(locale);
        }

        public int[] following(int offset) {
            if (this.mText.length() <= 0 || offset >= this.mText.length()) {
                return null;
            }
            int start = offset;
            if (offset < 0) {
                start = 0;
            }
            while (!isLetterOrDigit(start) && (isStartBoundary(start) ^ 1) != 0) {
                start = this.mImpl.following(start);
                if (start == -1) {
                    return null;
                }
            }
            int end = this.mImpl.following(start);
            if (end == -1 || (isEndBoundary(end) ^ 1) != 0) {
                return null;
            }
            return getRange(start, end);
        }

        public int[] preceding(int offset) {
            int textLegth = this.mText.length();
            if (textLegth <= 0 || offset <= 0) {
                return null;
            }
            int end = offset;
            if (offset > textLegth) {
                end = textLegth;
            }
            while (end > 0 && (isLetterOrDigit(end - 1) ^ 1) != 0 && (isEndBoundary(end) ^ 1) != 0) {
                end = this.mImpl.preceding(end);
                if (end == -1) {
                    return null;
                }
            }
            int start = this.mImpl.preceding(end);
            if (start == -1 || (isStartBoundary(start) ^ 1) != 0) {
                return null;
            }
            return getRange(start, end);
        }

        private boolean isStartBoundary(int index) {
            if (isLetterOrDigit(index)) {
                return index != 0 ? isLetterOrDigit(index - 1) ^ 1 : true;
            } else {
                return false;
            }
        }

        private boolean isEndBoundary(int index) {
            if (index <= 0 || !isLetterOrDigit(index - 1)) {
                return false;
            }
            return index != this.mText.length() ? isLetterOrDigit(index) ^ 1 : true;
        }

        private boolean isLetterOrDigit(int index) {
            if (index < 0 || index >= this.mText.length()) {
                return false;
            }
            return Character.isLetterOrDigit(this.mText.codePointAt(index));
        }
    }
}
