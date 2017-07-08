package java.text;

import sun.misc.FloatConsts;

public final class Bidi {
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = -2;
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = -1;
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;
    private android.icu.text.Bidi bidiBase;

    private static int translateConstToIcu(int javaInt) {
        switch (javaInt) {
            case DIRECTION_DEFAULT_LEFT_TO_RIGHT /*-2*/:
                return 126;
            case DIRECTION_DEFAULT_RIGHT_TO_LEFT /*-1*/:
                return FloatConsts.MAX_EXPONENT;
            case DIRECTION_LEFT_TO_RIGHT /*0*/:
                return DIRECTION_LEFT_TO_RIGHT;
            case DIRECTION_RIGHT_TO_LEFT /*1*/:
                return DIRECTION_RIGHT_TO_LEFT;
            default:
                return DIRECTION_LEFT_TO_RIGHT;
        }
    }

    public Bidi(String paragraph, int flags) {
        this(paragraph == null ? null : paragraph.toCharArray(), DIRECTION_LEFT_TO_RIGHT, null, DIRECTION_LEFT_TO_RIGHT, paragraph == null ? DIRECTION_LEFT_TO_RIGHT : paragraph.length(), flags);
    }

    public Bidi(AttributedCharacterIterator paragraph) {
        if (paragraph == null) {
            throw new IllegalArgumentException("paragraph is null");
        }
        this.bidiBase = new android.icu.text.Bidi(paragraph);
    }

    public Bidi(char[] text, int textStart, byte[] embeddings, int embStart, int paragraphLength, int flags) {
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        } else if (paragraphLength < 0) {
            throw new IllegalArgumentException("bad length: " + paragraphLength);
        } else if (textStart < 0 || paragraphLength > text.length - textStart) {
            throw new IllegalArgumentException("bad range: " + textStart + " length: " + paragraphLength + " for text of length: " + text.length);
        } else if (embeddings == null || (embStart >= 0 && paragraphLength <= embeddings.length - embStart)) {
            this.bidiBase = new android.icu.text.Bidi(text, textStart, embeddings, embStart, paragraphLength, translateConstToIcu(flags));
        } else {
            throw new IllegalArgumentException("bad range: " + embStart + " length: " + paragraphLength + " for embeddings of length: " + text.length);
        }
    }

    private Bidi(android.icu.text.Bidi bidiBase) {
        this.bidiBase = bidiBase;
    }

    public Bidi createLineBidi(int lineStart, int lineLimit) {
        if (lineStart < 0 || lineLimit < 0 || lineStart > lineLimit || lineLimit > getLength()) {
            throw new IllegalArgumentException("Invalid ranges (start=" + lineStart + ", " + "limit=" + lineLimit + ", length=" + getLength() + ")");
        } else if (lineStart != lineLimit) {
            return new Bidi(this.bidiBase.createLineBidi(lineStart, lineLimit));
        } else {
            return new Bidi(new android.icu.text.Bidi(new char[DIRECTION_LEFT_TO_RIGHT], DIRECTION_LEFT_TO_RIGHT, new byte[DIRECTION_LEFT_TO_RIGHT], DIRECTION_LEFT_TO_RIGHT, DIRECTION_LEFT_TO_RIGHT, translateConstToIcu(DIRECTION_LEFT_TO_RIGHT)));
        }
    }

    public boolean isMixed() {
        return this.bidiBase.isMixed();
    }

    public boolean isLeftToRight() {
        return this.bidiBase.isLeftToRight();
    }

    public boolean isRightToLeft() {
        return this.bidiBase.isRightToLeft();
    }

    public int getLength() {
        return this.bidiBase.getLength();
    }

    public boolean baseIsLeftToRight() {
        return this.bidiBase.baseIsLeftToRight();
    }

    public int getBaseLevel() {
        return this.bidiBase.getParaLevel();
    }

    public int getLevelAt(int offset) {
        try {
            return this.bidiBase.getLevelAt(offset);
        } catch (IllegalArgumentException e) {
            return getBaseLevel();
        }
    }

    public int getRunCount() {
        int runCount = this.bidiBase.countRuns();
        return runCount == 0 ? DIRECTION_RIGHT_TO_LEFT : runCount;
    }

    public int getRunLevel(int run) {
        if (run == getRunCount()) {
            return getBaseLevel();
        }
        return this.bidiBase.countRuns() == 0 ? this.bidiBase.getBaseLevel() : this.bidiBase.getRunLevel(run);
    }

    public int getRunStart(int run) {
        int i = DIRECTION_LEFT_TO_RIGHT;
        if (run == getRunCount()) {
            return getBaseLevel();
        }
        if (this.bidiBase.countRuns() != 0) {
            i = this.bidiBase.getRunStart(run);
        }
        return i;
    }

    public int getRunLimit(int run) {
        if (run == getRunCount()) {
            return getBaseLevel();
        }
        return this.bidiBase.countRuns() == 0 ? this.bidiBase.getLength() : this.bidiBase.getRunLimit(run);
    }

    public static boolean requiresBidi(char[] text, int start, int limit) {
        if (start >= 0 && start <= limit && limit <= text.length) {
            return android.icu.text.Bidi.requiresBidi(text, start, limit);
        }
        throw new IllegalArgumentException("Value start " + start + " is out of range 0 to " + limit);
    }

    public static void reorderVisually(byte[] levels, int levelStart, Object[] objects, int objectStart, int count) {
        if (levelStart < 0 || levels.length <= levelStart) {
            throw new IllegalArgumentException("Value levelStart " + levelStart + " is out of range 0 to " + (levels.length + DIRECTION_DEFAULT_RIGHT_TO_LEFT));
        } else if (objectStart < 0 || objects.length <= objectStart) {
            throw new IllegalArgumentException("Value objectStart " + levelStart + " is out of range 0 to " + (objects.length + DIRECTION_DEFAULT_RIGHT_TO_LEFT));
        } else if (count < 0 || objects.length < objectStart + count) {
            throw new IllegalArgumentException("Value count " + levelStart + " is out of range 0 to " + (objects.length - objectStart));
        } else {
            android.icu.text.Bidi.reorderVisually(levels, levelStart, objects, objectStart, count);
        }
    }

    public String toString() {
        return getClass().getName() + "[direction: " + this.bidiBase.getDirection() + " baseLevel: " + this.bidiBase.getBaseLevel() + " length: " + this.bidiBase.getLength() + " runs: " + this.bidiBase.getRunCount() + "]";
    }
}
