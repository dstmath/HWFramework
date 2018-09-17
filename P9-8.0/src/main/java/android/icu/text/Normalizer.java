package android.icu.text;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Norm2AllModes.Normalizer2WithImpl;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.UTF16Plus;
import android.icu.impl.UCaseProps;
import android.icu.lang.UCharacter;
import android.icu.util.ICUCloneNotSupportedException;
import java.nio.CharBuffer;
import java.text.CharacterIterator;

public final class Normalizer implements Cloneable {
    public static final int COMPARE_CODE_POINT_ORDER = 32768;
    private static final int COMPARE_EQUIV = 524288;
    public static final int COMPARE_IGNORE_CASE = 65536;
    @Deprecated
    public static final int COMPARE_NORM_OPTIONS_SHIFT = 20;
    @Deprecated
    public static final Mode COMPOSE = NFC;
    @Deprecated
    public static final Mode COMPOSE_COMPAT = NFKC;
    @Deprecated
    public static final Mode DECOMP = NFD;
    @Deprecated
    public static final Mode DECOMP_COMPAT = NFKD;
    @Deprecated
    public static final Mode DEFAULT = NFC;
    @Deprecated
    public static final int DONE = -1;
    @Deprecated
    public static final Mode FCD = new FCDMode();
    public static final int FOLD_CASE_DEFAULT = 0;
    public static final int FOLD_CASE_EXCLUDE_SPECIAL_I = 1;
    @Deprecated
    public static final int IGNORE_HANGUL = 1;
    public static final int INPUT_IS_FCD = 131072;
    public static final QuickCheckResult MAYBE = new QuickCheckResult(2, null);
    @Deprecated
    public static final Mode NFC = new NFCMode();
    @Deprecated
    public static final Mode NFD = new NFDMode();
    @Deprecated
    public static final Mode NFKC = new NFKCMode();
    @Deprecated
    public static final Mode NFKD = new NFKDMode();
    public static final QuickCheckResult NO = new QuickCheckResult(0, null);
    @Deprecated
    public static final Mode NONE = new NONEMode();
    @Deprecated
    public static final Mode NO_OP = NONE;
    @Deprecated
    public static final int UNICODE_3_2 = 32;
    public static final QuickCheckResult YES = new QuickCheckResult(1, null);
    private StringBuilder buffer;
    private int bufferPos;
    private int currentIndex;
    private Mode mode;
    private int nextIndex;
    private Normalizer2 norm2;
    private int options;
    private UCharacterIterator text;

    private static final class CharsAppendable implements Appendable {
        private final char[] chars;
        private final int limit;
        private int offset;
        private final int start;

        public CharsAppendable(char[] dest, int destStart, int destLimit) {
            this.chars = dest;
            this.offset = destStart;
            this.start = destStart;
            this.limit = destLimit;
        }

        public int length() {
            int len = this.offset - this.start;
            if (this.offset <= this.limit) {
                return len;
            }
            throw new IndexOutOfBoundsException(Integer.toString(len));
        }

        public Appendable append(char c) {
            if (this.offset < this.limit) {
                this.chars[this.offset] = c;
            }
            this.offset++;
            return this;
        }

        public Appendable append(CharSequence s) {
            return append(s, 0, s.length());
        }

        public Appendable append(CharSequence s, int sStart, int sLimit) {
            int len = sLimit - sStart;
            if (len <= this.limit - this.offset) {
                while (true) {
                    int sStart2 = sStart;
                    if (sStart2 >= sLimit) {
                        break;
                    }
                    char[] cArr = this.chars;
                    int i = this.offset;
                    this.offset = i + 1;
                    sStart = sStart2 + 1;
                    cArr[i] = s.charAt(sStart2);
                }
            } else {
                this.offset += len;
            }
            return this;
        }
    }

    private static final class CmpEquivLevel {
        CharSequence cs;
        int s;

        /* synthetic */ CmpEquivLevel(CmpEquivLevel -this0) {
            this();
        }

        private CmpEquivLevel() {
        }
    }

    private static final class FCD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Norm2AllModes.getFCDNormalizer2(), Unicode32.INSTANCE), null);

        private FCD32ModeImpl() {
        }
    }

    @Deprecated
    public static abstract class Mode {
        @Deprecated
        protected abstract Normalizer2 getNormalizer2(int i);

        @Deprecated
        protected Mode() {
        }
    }

    private static final class FCDMode extends Mode {
        /* synthetic */ FCDMode(FCDMode -this0) {
            this();
        }

        private FCDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 32) != 0 ? FCD32ModeImpl.INSTANCE.normalizer2 : FCDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class FCDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Norm2AllModes.getFCDNormalizer2(), null);

        private FCDModeImpl() {
        }
    }

    private static final class ModeImpl {
        private final Normalizer2 normalizer2;

        /* synthetic */ ModeImpl(Normalizer2 n2, ModeImpl -this1) {
            this(n2);
        }

        private ModeImpl(Normalizer2 n2) {
            this.normalizer2 = n2;
        }
    }

    private static final class NFC32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFCInstance(), Unicode32.INSTANCE), null);

        private NFC32ModeImpl() {
        }
    }

    private static final class NFCMode extends Mode {
        /* synthetic */ NFCMode(NFCMode -this0) {
            this();
        }

        private NFCMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 32) != 0 ? NFC32ModeImpl.INSTANCE.normalizer2 : NFCModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFCModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFCInstance(), null);

        private NFCModeImpl() {
        }
    }

    private static final class NFD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFDInstance(), Unicode32.INSTANCE), null);

        private NFD32ModeImpl() {
        }
    }

    private static final class NFDMode extends Mode {
        /* synthetic */ NFDMode(NFDMode -this0) {
            this();
        }

        private NFDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 32) != 0 ? NFD32ModeImpl.INSTANCE.normalizer2 : NFDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFDInstance(), null);

        private NFDModeImpl() {
        }
    }

    private static final class NFKC32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFKCInstance(), Unicode32.INSTANCE), null);

        private NFKC32ModeImpl() {
        }
    }

    private static final class NFKCMode extends Mode {
        /* synthetic */ NFKCMode(NFKCMode -this0) {
            this();
        }

        private NFKCMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 32) != 0 ? NFKC32ModeImpl.INSTANCE.normalizer2 : NFKCModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKCModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFKCInstance(), null);

        private NFKCModeImpl() {
        }
    }

    private static final class NFKD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFKDInstance(), Unicode32.INSTANCE), null);

        private NFKD32ModeImpl() {
        }
    }

    private static final class NFKDMode extends Mode {
        /* synthetic */ NFKDMode(NFKDMode -this0) {
            this();
        }

        private NFKDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 32) != 0 ? NFKD32ModeImpl.INSTANCE.normalizer2 : NFKDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFKDInstance(), null);

        private NFKDModeImpl() {
        }
    }

    private static final class NONEMode extends Mode {
        /* synthetic */ NONEMode(NONEMode -this0) {
            this();
        }

        private NONEMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return Norm2AllModes.NOOP_NORMALIZER2;
        }
    }

    public static final class QuickCheckResult {
        /* synthetic */ QuickCheckResult(int value, QuickCheckResult -this1) {
            this(value);
        }

        private QuickCheckResult(int value) {
        }
    }

    private static final class Unicode32 {
        private static final UnicodeSet INSTANCE = new UnicodeSet("[:age=3.2:]").freeze();

        private Unicode32() {
        }
    }

    @Deprecated
    public Normalizer(String str, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance(str);
        this.mode = mode;
        this.options = opt;
        this.norm2 = mode.getNormalizer2(opt);
        this.buffer = new StringBuilder();
    }

    @Deprecated
    public Normalizer(CharacterIterator iter, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance((CharacterIterator) iter.clone());
        this.mode = mode;
        this.options = opt;
        this.norm2 = mode.getNormalizer2(opt);
        this.buffer = new StringBuilder();
    }

    @Deprecated
    public Normalizer(UCharacterIterator iter, Mode mode, int options) {
        try {
            this.text = (UCharacterIterator) iter.clone();
            this.mode = mode;
            this.options = options;
            this.norm2 = mode.getNormalizer2(options);
            this.buffer = new StringBuilder();
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    @Deprecated
    public Object clone() {
        try {
            Normalizer copy = (Normalizer) super.clone();
            copy.text = (UCharacterIterator) this.text.clone();
            copy.mode = this.mode;
            copy.options = this.options;
            copy.norm2 = this.norm2;
            copy.buffer = new StringBuilder(this.buffer);
            copy.bufferPos = this.bufferPos;
            copy.currentIndex = this.currentIndex;
            copy.nextIndex = this.nextIndex;
            return copy;
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    private static final Normalizer2 getComposeNormalizer2(boolean compat, int options) {
        return (compat ? NFKC : NFC).getNormalizer2(options);
    }

    private static final Normalizer2 getDecomposeNormalizer2(boolean compat, int options) {
        return (compat ? NFKD : NFD).getNormalizer2(options);
    }

    @Deprecated
    public static String compose(String str, boolean compat) {
        return compose(str, compat, 0);
    }

    @Deprecated
    public static String compose(String str, boolean compat, int options) {
        return getComposeNormalizer2(compat, options).normalize(str);
    }

    @Deprecated
    public static int compose(char[] source, char[] target, boolean compat, int options) {
        return compose(source, 0, source.length, target, 0, target.length, compat, options);
    }

    @Deprecated
    public static int compose(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, boolean compat, int options) {
        CharSequence srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        Appendable app = new CharsAppendable(dest, destStart, destLimit);
        getComposeNormalizer2(compat, options).normalize(srcBuffer, app);
        return app.length();
    }

    @Deprecated
    public static String decompose(String str, boolean compat) {
        return decompose(str, compat, 0);
    }

    @Deprecated
    public static String decompose(String str, boolean compat, int options) {
        return getDecomposeNormalizer2(compat, options).normalize(str);
    }

    @Deprecated
    public static int decompose(char[] source, char[] target, boolean compat, int options) {
        return decompose(source, 0, source.length, target, 0, target.length, compat, options);
    }

    @Deprecated
    public static int decompose(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, boolean compat, int options) {
        CharSequence srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        Appendable app = new CharsAppendable(dest, destStart, destLimit);
        getDecomposeNormalizer2(compat, options).normalize(srcBuffer, app);
        return app.length();
    }

    @Deprecated
    public static String normalize(String str, Mode mode, int options) {
        return mode.getNormalizer2(options).normalize(str);
    }

    @Deprecated
    public static String normalize(String src, Mode mode) {
        return normalize(src, mode, 0);
    }

    @Deprecated
    public static int normalize(char[] source, char[] target, Mode mode, int options) {
        return normalize(source, 0, source.length, target, 0, target.length, mode, options);
    }

    @Deprecated
    public static int normalize(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, Mode mode, int options) {
        CharSequence srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        Appendable app = new CharsAppendable(dest, destStart, destLimit);
        mode.getNormalizer2(options).normalize(srcBuffer, app);
        return app.length();
    }

    @Deprecated
    public static String normalize(int char32, Mode mode, int options) {
        if (mode != NFD || options != 0) {
            return normalize(UTF16.valueOf(char32), mode, options);
        }
        String decomposition = Normalizer2.getNFCInstance().getDecomposition(char32);
        if (decomposition == null) {
            decomposition = UTF16.valueOf(char32);
        }
        return decomposition;
    }

    @Deprecated
    public static String normalize(int char32, Mode mode) {
        return normalize(char32, mode, 0);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(String source, Mode mode) {
        return quickCheck(source, mode, 0);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(String source, Mode mode, int options) {
        return mode.getNormalizer2(options).quickCheck(source);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(char[] source, Mode mode, int options) {
        return quickCheck(source, 0, source.length, mode, options);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(char[] source, int start, int limit, Mode mode, int options) {
        return mode.getNormalizer2(options).quickCheck(CharBuffer.wrap(source, start, limit - start));
    }

    @Deprecated
    public static boolean isNormalized(char[] src, int start, int limit, Mode mode, int options) {
        return mode.getNormalizer2(options).isNormalized(CharBuffer.wrap(src, start, limit - start));
    }

    @Deprecated
    public static boolean isNormalized(String str, Mode mode, int options) {
        return mode.getNormalizer2(options).isNormalized(str);
    }

    @Deprecated
    public static boolean isNormalized(int char32, Mode mode, int options) {
        return isNormalized(UTF16.valueOf(char32), mode, options);
    }

    public static int compare(char[] s1, int s1Start, int s1Limit, char[] s2, int s2Start, int s2Limit, int options) {
        if (s1 != null && s1Start >= 0 && s1Limit >= 0 && s2 != null && s2Start >= 0 && s2Limit >= 0 && s1Limit >= s1Start && s2Limit >= s2Start) {
            return internalCompare(CharBuffer.wrap(s1, s1Start, s1Limit - s1Start), CharBuffer.wrap(s2, s2Start, s2Limit - s2Start), options);
        }
        throw new IllegalArgumentException();
    }

    public static int compare(String s1, String s2, int options) {
        return internalCompare(s1, s2, options);
    }

    public static int compare(char[] s1, char[] s2, int options) {
        return internalCompare(CharBuffer.wrap(s1), CharBuffer.wrap(s2), options);
    }

    public static int compare(int char32a, int char32b, int options) {
        return internalCompare(UTF16.valueOf(char32a), UTF16.valueOf(char32b), 131072 | options);
    }

    public static int compare(int char32a, String str2, int options) {
        return internalCompare(UTF16.valueOf(char32a), str2, options);
    }

    @Deprecated
    public static int concatenate(char[] left, int leftStart, int leftLimit, char[] right, int rightStart, int rightLimit, char[] dest, int destStart, int destLimit, Mode mode, int options) {
        if (dest == null) {
            throw new IllegalArgumentException();
        } else if (right != dest || rightStart >= destLimit || destStart >= rightLimit) {
            StringBuilder destBuilder = new StringBuilder((((leftLimit - leftStart) + rightLimit) - rightStart) + 16);
            destBuilder.append(left, leftStart, leftLimit - leftStart);
            mode.getNormalizer2(options).append(destBuilder, CharBuffer.wrap(right, rightStart, rightLimit - rightStart));
            int destLength = destBuilder.length();
            if (destLength <= destLimit - destStart) {
                destBuilder.getChars(0, destLength, dest, destStart);
                return destLength;
            }
            throw new IndexOutOfBoundsException(Integer.toString(destLength));
        } else {
            throw new IllegalArgumentException("overlapping right and dst ranges");
        }
    }

    @Deprecated
    public static String concatenate(char[] left, char[] right, Mode mode, int options) {
        return mode.getNormalizer2(options).append(new StringBuilder((left.length + right.length) + 16).append(left), CharBuffer.wrap(right)).toString();
    }

    @Deprecated
    public static String concatenate(String left, String right, Mode mode, int options) {
        return mode.getNormalizer2(options).append(new StringBuilder((left.length() + right.length()) + 16).append(left), right).toString();
    }

    @Deprecated
    public static int getFC_NFKC_Closure(int c, char[] dest) {
        String closure = getFC_NFKC_Closure(c);
        int length = closure.length();
        if (!(length == 0 || dest == null || length > dest.length)) {
            closure.getChars(0, length, dest, 0);
        }
        return length;
    }

    @Deprecated
    public static String getFC_NFKC_Closure(int c) {
        Normalizer2 nfkc = NFKCModeImpl.INSTANCE.normalizer2;
        UCaseProps csp = UCaseProps.INSTANCE;
        StringBuilder folded = new StringBuilder();
        int folded1Length = csp.toFullFolding(c, folded, 0);
        if (folded1Length < 0) {
            Normalizer2Impl nfkcImpl = ((Normalizer2WithImpl) nfkc).impl;
            if (nfkcImpl.getCompQuickCheck(nfkcImpl.getNorm16(c)) != 0) {
                return "";
            }
            folded.appendCodePoint(c);
        } else if (folded1Length > 31) {
            folded.appendCodePoint(folded1Length);
        }
        String kc1 = nfkc.normalize(folded);
        String kc2 = nfkc.normalize(UCharacter.foldCase(kc1, 0));
        if (kc1.equals(kc2)) {
            return "";
        }
        return kc2;
    }

    @Deprecated
    public int current() {
        if (this.bufferPos < this.buffer.length() || nextNormalize()) {
            return this.buffer.codePointAt(this.bufferPos);
        }
        return -1;
    }

    @Deprecated
    public int next() {
        if (this.bufferPos >= this.buffer.length() && !nextNormalize()) {
            return -1;
        }
        int c = this.buffer.codePointAt(this.bufferPos);
        this.bufferPos += Character.charCount(c);
        return c;
    }

    @Deprecated
    public int previous() {
        if (this.bufferPos <= 0 && !previousNormalize()) {
            return -1;
        }
        int c = this.buffer.codePointBefore(this.bufferPos);
        this.bufferPos -= Character.charCount(c);
        return c;
    }

    @Deprecated
    public void reset() {
        this.text.setToStart();
        this.nextIndex = 0;
        this.currentIndex = 0;
        clearBuffer();
    }

    @Deprecated
    public void setIndexOnly(int index) {
        this.text.setIndex(index);
        this.nextIndex = index;
        this.currentIndex = index;
        clearBuffer();
    }

    @Deprecated
    public int setIndex(int index) {
        setIndexOnly(index);
        return current();
    }

    @Deprecated
    public int getBeginIndex() {
        return 0;
    }

    @Deprecated
    public int getEndIndex() {
        return endIndex();
    }

    @Deprecated
    public int first() {
        reset();
        return next();
    }

    @Deprecated
    public int last() {
        this.text.setToLimit();
        int index = this.text.getIndex();
        this.nextIndex = index;
        this.currentIndex = index;
        clearBuffer();
        return previous();
    }

    @Deprecated
    public int getIndex() {
        if (this.bufferPos < this.buffer.length()) {
            return this.currentIndex;
        }
        return this.nextIndex;
    }

    @Deprecated
    public int startIndex() {
        return 0;
    }

    @Deprecated
    public int endIndex() {
        return this.text.getLength();
    }

    @Deprecated
    public void setMode(Mode newMode) {
        this.mode = newMode;
        this.norm2 = this.mode.getNormalizer2(this.options);
    }

    @Deprecated
    public Mode getMode() {
        return this.mode;
    }

    @Deprecated
    public void setOption(int option, boolean value) {
        if (value) {
            this.options |= option;
        } else {
            this.options &= ~option;
        }
        this.norm2 = this.mode.getNormalizer2(this.options);
    }

    @Deprecated
    public int getOption(int option) {
        if ((this.options & option) != 0) {
            return 1;
        }
        return 0;
    }

    @Deprecated
    public int getText(char[] fillIn) {
        return this.text.getText(fillIn);
    }

    @Deprecated
    public int getLength() {
        return this.text.getLength();
    }

    @Deprecated
    public String getText() {
        return this.text.getText();
    }

    @Deprecated
    public void setText(StringBuffer newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        reset();
    }

    @Deprecated
    public void setText(char[] newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        reset();
    }

    @Deprecated
    public void setText(String newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        reset();
    }

    @Deprecated
    public void setText(CharacterIterator newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        reset();
    }

    @Deprecated
    public void setText(UCharacterIterator newText) {
        try {
            UCharacterIterator newIter = (UCharacterIterator) newText.clone();
            if (newIter == null) {
                throw new IllegalStateException("Could not create a new UCharacterIterator");
            }
            this.text = newIter;
            reset();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Could not clone the UCharacterIterator", e);
        }
    }

    private void clearBuffer() {
        this.buffer.setLength(0);
        this.bufferPos = 0;
    }

    private boolean nextNormalize() {
        boolean z = false;
        clearBuffer();
        this.currentIndex = this.nextIndex;
        this.text.setIndex(this.nextIndex);
        int c = this.text.nextCodePoint();
        if (c < 0) {
            return false;
        }
        CharSequence segment = new StringBuilder().appendCodePoint(c);
        while (true) {
            c = this.text.nextCodePoint();
            if (c < 0) {
                break;
            } else if (this.norm2.hasBoundaryBefore(c)) {
                this.text.moveCodePointIndex(-1);
                break;
            } else {
                segment.appendCodePoint(c);
            }
        }
        this.nextIndex = this.text.getIndex();
        this.norm2.normalize(segment, this.buffer);
        if (this.buffer.length() != 0) {
            z = true;
        }
        return z;
    }

    private boolean previousNormalize() {
        clearBuffer();
        this.nextIndex = this.currentIndex;
        this.text.setIndex(this.currentIndex);
        CharSequence segment = new StringBuilder();
        int c;
        do {
            c = this.text.previousCodePoint();
            if (c < 0) {
                break;
            } else if (c <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                segment.insert(0, (char) c);
            } else {
                segment.insert(0, Character.toChars(c));
            }
        } while (!this.norm2.hasBoundaryBefore(c));
        this.currentIndex = this.text.getIndex();
        this.norm2.normalize(segment, this.buffer);
        this.bufferPos = this.buffer.length();
        if (this.buffer.length() != 0) {
            return true;
        }
        return false;
    }

    private static int internalCompare(CharSequence s1, CharSequence s2, int options) {
        int normOptions = options >>> 20;
        options |= 524288;
        if ((131072 & options) == 0 || (options & 1) != 0) {
            Normalizer2 n2;
            if ((options & 1) != 0) {
                n2 = NFD.getNormalizer2(normOptions);
            } else {
                n2 = FCD.getNormalizer2(normOptions);
            }
            int spanQCYes1 = n2.spanQuickCheckYes(s1);
            int spanQCYes2 = n2.spanQuickCheckYes(s2);
            if (spanQCYes1 < s1.length()) {
                s1 = n2.normalizeSecondAndAppend(new StringBuilder(s1.length() + 16).append(s1, 0, spanQCYes1), s1.subSequence(spanQCYes1, s1.length()));
            }
            if (spanQCYes2 < s2.length()) {
                s2 = n2.normalizeSecondAndAppend(new StringBuilder(s2.length() + 16).append(s2, 0, spanQCYes2), s2.subSequence(spanQCYes2, s2.length()));
            }
        }
        return cmpEquivFold(s1, s2, options);
    }

    private static final CmpEquivLevel[] createCmpEquivLevelStack() {
        return new CmpEquivLevel[]{new CmpEquivLevel(), new CmpEquivLevel()};
    }

    static int cmpEquivFold(CharSequence cs1, CharSequence cs2, int options) {
        Normalizer2Impl nfcImpl;
        UCaseProps csp;
        Appendable fold1;
        Appendable fold2;
        CmpEquivLevel[] stack1 = null;
        CmpEquivLevel[] stack2 = null;
        if ((524288 & options) != 0) {
            nfcImpl = Norm2AllModes.getNFCInstance().impl;
        } else {
            nfcImpl = null;
        }
        if ((65536 & options) != 0) {
            csp = UCaseProps.INSTANCE;
            fold1 = new StringBuilder();
            fold2 = new StringBuilder();
        } else {
            csp = null;
            fold2 = null;
            fold1 = null;
        }
        int s1 = 0;
        int limit1 = cs1.length();
        int s2 = 0;
        int limit2 = cs2.length();
        int level2 = 0;
        int level1 = 0;
        int c2 = -1;
        int c1 = -1;
        while (true) {
            if (c1 < 0) {
                while (true) {
                    int i = s1;
                    if (i != limit1) {
                        s1 = i + 1;
                        c1 = cs1.charAt(i);
                        break;
                    } else if (level1 == 0) {
                        c1 = -1;
                        s1 = i;
                        break;
                    } else {
                        do {
                            level1--;
                            cs1 = stack1[level1].cs;
                        } while (cs1 == null);
                        s1 = stack1[level1].s;
                        limit1 = cs1.length();
                    }
                }
            }
            if (c2 < 0) {
                while (true) {
                    int i2 = s2;
                    if (i2 != limit2) {
                        s2 = i2 + 1;
                        c2 = cs2.charAt(i2);
                        break;
                    } else if (level2 == 0) {
                        c2 = -1;
                        s2 = i2;
                        break;
                    } else {
                        do {
                            level2--;
                            cs2 = stack2[level2].cs;
                        } while (cs2 == null);
                        s2 = stack2[level2].s;
                        limit2 = cs2.length();
                    }
                }
            }
            if (c1 != c2) {
                if (c1 >= 0) {
                    if (c2 >= 0) {
                        char c;
                        int length;
                        int cp1 = c1;
                        if (UTF16.isSurrogate((char) c1)) {
                            if (UTF16Plus.isSurrogateLead(c1)) {
                                if (s1 != limit1) {
                                    c = cs1.charAt(s1);
                                    if (Character.isLowSurrogate(c)) {
                                        cp1 = Character.toCodePoint((char) c1, c);
                                    }
                                }
                            } else if (s1 - 2 >= 0) {
                                c = cs1.charAt(s1 - 2);
                                if (Character.isHighSurrogate(c)) {
                                    cp1 = Character.toCodePoint(c, (char) c1);
                                }
                            }
                        }
                        int cp2 = c2;
                        if (UTF16.isSurrogate((char) c2)) {
                            if (UTF16Plus.isSurrogateLead(c2)) {
                                if (s2 != limit2) {
                                    c = cs2.charAt(s2);
                                    if (Character.isLowSurrogate(c)) {
                                        cp2 = Character.toCodePoint((char) c2, c);
                                    }
                                }
                            } else if (s2 - 2 >= 0) {
                                c = cs2.charAt(s2 - 2);
                                if (Character.isHighSurrogate(c)) {
                                    cp2 = Character.toCodePoint(c, (char) c2);
                                }
                            }
                        }
                        if (level1 == 0 && (65536 & options) != 0) {
                            length = csp.toFullFolding(cp1, fold1, options);
                            if (length >= 0) {
                                if (UTF16.isSurrogate((char) c1)) {
                                    if (UTF16Plus.isSurrogateLead(c1)) {
                                        s1++;
                                    } else {
                                        s2--;
                                        c2 = cs2.charAt(s2 - 1);
                                    }
                                }
                                if (stack1 == null) {
                                    stack1 = createCmpEquivLevelStack();
                                }
                                stack1[0].cs = cs1;
                                stack1[0].s = s1;
                                level1++;
                                if (length <= 31) {
                                    fold1.delete(0, fold1.length() - length);
                                } else {
                                    fold1.setLength(0);
                                    fold1.appendCodePoint(length);
                                }
                                cs1 = fold1;
                                s1 = 0;
                                limit1 = fold1.length();
                                c1 = -1;
                            }
                        }
                        if (level2 == 0 && (65536 & options) != 0) {
                            length = csp.toFullFolding(cp2, fold2, options);
                            if (length >= 0) {
                                if (UTF16.isSurrogate((char) c2)) {
                                    if (UTF16Plus.isSurrogateLead(c2)) {
                                        s2++;
                                    } else {
                                        s1--;
                                        c1 = cs1.charAt(s1 - 1);
                                    }
                                }
                                if (stack2 == null) {
                                    stack2 = createCmpEquivLevelStack();
                                }
                                stack2[0].cs = cs2;
                                stack2[0].s = s2;
                                level2++;
                                if (length <= 31) {
                                    fold2.delete(0, fold2.length() - length);
                                } else {
                                    fold2.setLength(0);
                                    fold2.appendCodePoint(length);
                                }
                                cs2 = fold2;
                                s2 = 0;
                                limit2 = fold2.length();
                                c2 = -1;
                            }
                        }
                        if (level1 < 2 && (524288 & options) != 0) {
                            String decomp1 = nfcImpl.getDecomposition(cp1);
                            if (decomp1 != null) {
                                if (UTF16.isSurrogate((char) c1)) {
                                    if (UTF16Plus.isSurrogateLead(c1)) {
                                        s1++;
                                    } else {
                                        s2--;
                                        c2 = cs2.charAt(s2 - 1);
                                    }
                                }
                                if (stack1 == null) {
                                    stack1 = createCmpEquivLevelStack();
                                }
                                stack1[level1].cs = cs1;
                                stack1[level1].s = s1;
                                level1++;
                                if (level1 < 2) {
                                    int level12 = level1 + 1;
                                    stack1[level1].cs = null;
                                    level1 = level12;
                                }
                                cs1 = decomp1;
                                s1 = 0;
                                limit1 = decomp1.length();
                                c1 = -1;
                            }
                        }
                        if (level2 >= 2 || (524288 & options) == 0) {
                            break;
                        }
                        String decomp2 = nfcImpl.getDecomposition(cp2);
                        if (decomp2 == null) {
                            break;
                        }
                        if (UTF16.isSurrogate((char) c2)) {
                            if (UTF16Plus.isSurrogateLead(c2)) {
                                s2++;
                            } else {
                                s1--;
                                c1 = cs1.charAt(s1 - 1);
                            }
                        }
                        if (stack2 == null) {
                            stack2 = createCmpEquivLevelStack();
                        }
                        stack2[level2].cs = cs2;
                        stack2[level2].s = s2;
                        level2++;
                        if (level2 < 2) {
                            int level22 = level2 + 1;
                            stack2[level2].cs = null;
                            level2 = level22;
                        }
                        cs2 = decomp2;
                        s2 = 0;
                        limit2 = decomp2.length();
                        c2 = -1;
                    } else {
                        return 1;
                    }
                }
                return -1;
            } else if (c1 < 0) {
                return 0;
            } else {
                c2 = -1;
                c1 = -1;
            }
        }
        if (c1 >= 55296 && c2 >= 55296 && (32768 & options) != 0) {
            if ((c1 > 56319 || s1 == limit1 || !Character.isLowSurrogate(cs1.charAt(s1))) && !(Character.isLowSurrogate((char) c1) && s1 - 1 != 0 && Character.isHighSurrogate(cs1.charAt(s1 - 2)))) {
                c1 -= 10240;
            }
            if ((c2 > 56319 || s2 == limit2 || !Character.isLowSurrogate(cs2.charAt(s2))) && !(Character.isLowSurrogate((char) c2) && s2 - 1 != 0 && Character.isHighSurrogate(cs2.charAt(s2 - 2)))) {
                c2 -= 10240;
            }
        }
        return c1 - c2;
    }
}
