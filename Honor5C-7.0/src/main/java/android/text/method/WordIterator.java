package android.text.method;

import android.icu.text.BreakIterator;
import android.text.Selection.PositionIterator;
import android.text.SpannableStringBuilder;
import java.util.Locale;

public class WordIterator implements PositionIterator {
    private static final int WINDOW_WIDTH = 50;
    private BreakIterator mIterator;
    private int mOffsetShift;
    private String mString;

    public WordIterator() {
        this(Locale.getDefault());
    }

    public WordIterator(Locale locale) {
        this.mIterator = BreakIterator.getWordInstance(locale);
    }

    public void setCharSequence(CharSequence charSequence, int start, int end) {
        this.mOffsetShift = Math.max(0, start - 50);
        int windowEnd = Math.min(charSequence.length(), end + WINDOW_WIDTH);
        if (charSequence instanceof SpannableStringBuilder) {
            this.mString = ((SpannableStringBuilder) charSequence).substring(this.mOffsetShift, windowEnd);
        } else {
            this.mString = charSequence.subSequence(this.mOffsetShift, windowEnd).toString();
        }
        this.mIterator.setText(this.mString);
    }

    public int preceding(int offset) {
        int shiftedOffset = offset - this.mOffsetShift;
        do {
            shiftedOffset = this.mIterator.preceding(shiftedOffset);
            if (shiftedOffset == -1) {
                return -1;
            }
        } while (!isOnLetterOrDigit(shiftedOffset));
        return this.mOffsetShift + shiftedOffset;
    }

    public int following(int offset) {
        int shiftedOffset = offset - this.mOffsetShift;
        do {
            shiftedOffset = this.mIterator.following(shiftedOffset);
            if (shiftedOffset == -1) {
                return -1;
            }
        } while (!isAfterLetterOrDigit(shiftedOffset));
        return this.mOffsetShift + shiftedOffset;
    }

    public boolean isBoundary(int offset) {
        int shiftedOffset = offset - this.mOffsetShift;
        checkOffsetIsValid(shiftedOffset);
        return this.mIterator.isBoundary(shiftedOffset);
    }

    public int nextBoundary(int offset) {
        int shiftedOffset = this.mIterator.following(offset - this.mOffsetShift);
        if (shiftedOffset == -1) {
            return -1;
        }
        return this.mOffsetShift + shiftedOffset;
    }

    public int prevBoundary(int offset) {
        int shiftedOffset = this.mIterator.preceding(offset - this.mOffsetShift);
        if (shiftedOffset == -1) {
            return -1;
        }
        return this.mOffsetShift + shiftedOffset;
    }

    public int getBeginning(int offset) {
        return getBeginning(offset, false);
    }

    public int getEnd(int offset) {
        return getEnd(offset, false);
    }

    public int getPrevWordBeginningOnTwoWordsBoundary(int offset) {
        return getBeginning(offset, true);
    }

    public int getNextWordEndOnTwoWordBoundary(int offset) {
        return getEnd(offset, true);
    }

    private int getBeginning(int offset, boolean getPrevWordBeginningOnTwoWordsBoundary) {
        int shiftedOffset = offset - this.mOffsetShift;
        checkOffsetIsValid(shiftedOffset);
        if (isOnLetterOrDigit(shiftedOffset)) {
            if (!this.mIterator.isBoundary(shiftedOffset) || (isAfterLetterOrDigit(shiftedOffset) && getPrevWordBeginningOnTwoWordsBoundary)) {
                return this.mIterator.preceding(shiftedOffset) + this.mOffsetShift;
            }
            return this.mOffsetShift + shiftedOffset;
        } else if (isAfterLetterOrDigit(shiftedOffset)) {
            return this.mIterator.preceding(shiftedOffset) + this.mOffsetShift;
        } else {
            return -1;
        }
    }

    private int getEnd(int offset, boolean getNextWordEndOnTwoWordBoundary) {
        int shiftedOffset = offset - this.mOffsetShift;
        checkOffsetIsValid(shiftedOffset);
        if (isAfterLetterOrDigit(shiftedOffset)) {
            if (!this.mIterator.isBoundary(shiftedOffset) || (isOnLetterOrDigit(shiftedOffset) && getNextWordEndOnTwoWordBoundary)) {
                return this.mIterator.following(shiftedOffset) + this.mOffsetShift;
            }
            return this.mOffsetShift + shiftedOffset;
        } else if (isOnLetterOrDigit(shiftedOffset)) {
            return this.mIterator.following(shiftedOffset) + this.mOffsetShift;
        } else {
            return -1;
        }
    }

    public int getPunctuationBeginning(int offset) {
        while (offset != -1 && !isPunctuationStartBoundary(offset)) {
            offset = prevBoundary(offset);
        }
        return offset;
    }

    public int getPunctuationEnd(int offset) {
        while (offset != -1 && !isPunctuationEndBoundary(offset)) {
            offset = nextBoundary(offset);
        }
        return offset;
    }

    public boolean isAfterPunctuation(int offset) {
        int shiftedOffset = offset - this.mOffsetShift;
        if (shiftedOffset < 1 || shiftedOffset > this.mString.length()) {
            return false;
        }
        return isPunctuation(this.mString.codePointBefore(shiftedOffset));
    }

    public boolean isOnPunctuation(int offset) {
        int shiftedOffset = offset - this.mOffsetShift;
        if (shiftedOffset < 0 || shiftedOffset >= this.mString.length()) {
            return false;
        }
        return isPunctuation(this.mString.codePointAt(shiftedOffset));
    }

    private boolean isPunctuationStartBoundary(int offset) {
        return isOnPunctuation(offset) && !isAfterPunctuation(offset);
    }

    private boolean isPunctuationEndBoundary(int offset) {
        return !isOnPunctuation(offset) ? isAfterPunctuation(offset) : false;
    }

    private boolean isPunctuation(int cp) {
        int type = Character.getType(cp);
        if (type == 23 || type == 20 || type == 22 || type == 30 || type == 29 || type == 24 || type == 21) {
            return true;
        }
        return false;
    }

    private boolean isAfterLetterOrDigit(int shiftedOffset) {
        if (shiftedOffset < 1 || shiftedOffset > this.mString.length() || !Character.isLetterOrDigit(this.mString.codePointBefore(shiftedOffset))) {
            return false;
        }
        return true;
    }

    private boolean isOnLetterOrDigit(int shiftedOffset) {
        if (shiftedOffset < 0 || shiftedOffset >= this.mString.length() || !Character.isLetterOrDigit(this.mString.codePointAt(shiftedOffset))) {
            return false;
        }
        return true;
    }

    private void checkOffsetIsValid(int shiftedOffset) {
        if (shiftedOffset < 0 || shiftedOffset > this.mString.length()) {
            throw new IllegalArgumentException("Invalid offset: " + (this.mOffsetShift + shiftedOffset) + ". Valid range is [" + this.mOffsetShift + ", " + (this.mString.length() + this.mOffsetShift) + "]");
        }
    }
}
