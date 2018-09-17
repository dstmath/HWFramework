package android.text.method;

import android.icu.lang.UCharacter;
import android.icu.text.BreakIterator;
import android.text.CharSequenceCharacterIterator;
import android.text.Selection.PositionIterator;
import java.util.Locale;

public class WordIterator implements PositionIterator {
    private static final int WINDOW_WIDTH = 50;
    private CharSequence mCharSeq;
    private int mEnd;
    private final BreakIterator mIterator;
    private int mStart;

    public WordIterator() {
        this(Locale.getDefault());
    }

    public WordIterator(Locale locale) {
        this.mIterator = BreakIterator.getWordInstance(locale);
    }

    public void setCharSequence(CharSequence charSequence, int start, int end) {
        if (start < 0 || end > charSequence.length()) {
            throw new IndexOutOfBoundsException("input indexes are outside the CharSequence");
        }
        this.mCharSeq = charSequence;
        this.mStart = Math.max(0, start - 50);
        this.mEnd = Math.min(charSequence.length(), end + 50);
        this.mIterator.setText(new CharSequenceCharacterIterator(charSequence, this.mStart, this.mEnd));
    }

    public int preceding(int offset) {
        checkOffsetIsValid(offset);
        do {
            offset = this.mIterator.preceding(offset);
            if (offset == -1) {
                break;
            }
        } while (!isOnLetterOrDigit(offset));
        return offset;
    }

    public int following(int offset) {
        checkOffsetIsValid(offset);
        do {
            offset = this.mIterator.following(offset);
            if (offset == -1) {
                break;
            }
        } while (!isAfterLetterOrDigit(offset));
        return offset;
    }

    public boolean isBoundary(int offset) {
        checkOffsetIsValid(offset);
        return this.mIterator.isBoundary(offset);
    }

    public int nextBoundary(int offset) {
        checkOffsetIsValid(offset);
        return this.mIterator.following(offset);
    }

    public int prevBoundary(int offset) {
        checkOffsetIsValid(offset);
        return this.mIterator.preceding(offset);
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
        checkOffsetIsValid(offset);
        if (isOnLetterOrDigit(offset)) {
            if (!this.mIterator.isBoundary(offset) || (isAfterLetterOrDigit(offset) && (getPrevWordBeginningOnTwoWordsBoundary ^ 1) == 0)) {
                return this.mIterator.preceding(offset);
            }
            return offset;
        } else if (isAfterLetterOrDigit(offset)) {
            return this.mIterator.preceding(offset);
        } else {
            return -1;
        }
    }

    private int getEnd(int offset, boolean getNextWordEndOnTwoWordBoundary) {
        checkOffsetIsValid(offset);
        if (isAfterLetterOrDigit(offset)) {
            if (!this.mIterator.isBoundary(offset) || (isOnLetterOrDigit(offset) && (getNextWordEndOnTwoWordBoundary ^ 1) == 0)) {
                return this.mIterator.following(offset);
            }
            return offset;
        } else if (isOnLetterOrDigit(offset)) {
            return this.mIterator.following(offset);
        } else {
            return -1;
        }
    }

    public int getPunctuationBeginning(int offset) {
        checkOffsetIsValid(offset);
        while (offset != -1 && (isPunctuationStartBoundary(offset) ^ 1) != 0) {
            offset = prevBoundary(offset);
        }
        return offset;
    }

    public int getPunctuationEnd(int offset) {
        checkOffsetIsValid(offset);
        while (offset != -1 && (isPunctuationEndBoundary(offset) ^ 1) != 0) {
            offset = nextBoundary(offset);
        }
        return offset;
    }

    public boolean isAfterPunctuation(int offset) {
        if (this.mStart >= offset || offset > this.mEnd) {
            return false;
        }
        return isPunctuation(Character.codePointBefore(this.mCharSeq, offset));
    }

    public boolean isOnPunctuation(int offset) {
        if (this.mStart > offset || offset >= this.mEnd) {
            return false;
        }
        return isPunctuation(Character.codePointAt(this.mCharSeq, offset));
    }

    public static boolean isMidWordPunctuation(Locale locale, int codePoint) {
        int wb = UCharacter.getIntPropertyValue(codePoint, 4116);
        if (wb == 4 || wb == 11 || wb == 15) {
            return true;
        }
        return false;
    }

    private boolean isPunctuationStartBoundary(int offset) {
        return isOnPunctuation(offset) ? isAfterPunctuation(offset) ^ 1 : false;
    }

    private boolean isPunctuationEndBoundary(int offset) {
        return !isOnPunctuation(offset) ? isAfterPunctuation(offset) : false;
    }

    private static boolean isPunctuation(int cp) {
        int type = Character.getType(cp);
        if (type == 23 || type == 20 || type == 22 || type == 30 || type == 29 || type == 24 || type == 21) {
            return true;
        }
        return false;
    }

    private boolean isAfterLetterOrDigit(int offset) {
        if (this.mStart >= offset || offset > this.mEnd || !Character.isLetterOrDigit(Character.codePointBefore(this.mCharSeq, offset))) {
            return false;
        }
        return true;
    }

    private boolean isOnLetterOrDigit(int offset) {
        if (this.mStart > offset || offset >= this.mEnd || !Character.isLetterOrDigit(Character.codePointAt(this.mCharSeq, offset))) {
            return false;
        }
        return true;
    }

    private void checkOffsetIsValid(int offset) {
        if (this.mStart > offset || offset > this.mEnd) {
            throw new IllegalArgumentException("Invalid offset: " + offset + ". Valid range is [" + this.mStart + ", " + this.mEnd + "]");
        }
    }
}
