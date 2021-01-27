package ohos.global.icu.text;

import java.nio.CharBuffer;
import java.text.CharacterIterator;
import ohos.global.icu.impl.Norm2AllModes;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.UCaseProps;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.util.ICUCloneNotSupportedException;

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
    public static final QuickCheckResult MAYBE = new QuickCheckResult(2);
    @Deprecated
    public static final Mode NFC = new NFCMode();
    @Deprecated
    public static final Mode NFD = new NFDMode();
    @Deprecated
    public static final Mode NFKC = new NFKCMode();
    @Deprecated
    public static final Mode NFKD = new NFKDMode();
    public static final QuickCheckResult NO = new QuickCheckResult(0);
    @Deprecated
    public static final Mode NONE = new NONEMode();
    @Deprecated
    public static final Mode NO_OP = NONE;
    @Deprecated
    public static final int UNICODE_3_2 = 32;
    public static final QuickCheckResult YES = new QuickCheckResult(1);
    private StringBuilder buffer;
    private int bufferPos;
    private int currentIndex;
    private Mode mode;
    private int nextIndex;
    private Normalizer2 norm2;
    private int options;
    private UCharacterIterator text;

    @Deprecated
    public int getBeginIndex() {
        return 0;
    }

    @Deprecated
    public int startIndex() {
        return 0;
    }

    /* access modifiers changed from: private */
    public static final class ModeImpl {
        private final Normalizer2 normalizer2;

        private ModeImpl(Normalizer2 normalizer22) {
            this.normalizer2 = normalizer22;
        }
    }

    private static final class NFDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFDInstance());

        private NFDModeImpl() {
        }
    }

    private static final class NFKDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFKDInstance());

        private NFKDModeImpl() {
        }
    }

    private static final class NFCModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFCInstance());

        private NFCModeImpl() {
        }
    }

    /* access modifiers changed from: private */
    public static final class NFKCModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Normalizer2.getNFKCInstance());

        private NFKCModeImpl() {
        }
    }

    private static final class FCDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Norm2AllModes.getFCDNormalizer2());

        private FCDModeImpl() {
        }
    }

    private static final class Unicode32 {
        private static final UnicodeSet INSTANCE = new UnicodeSet("[:age=3.2:]").freeze();

        private Unicode32() {
        }
    }

    private static final class NFD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFDInstance(), Unicode32.INSTANCE));

        private NFD32ModeImpl() {
        }
    }

    private static final class NFKD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFKDInstance(), Unicode32.INSTANCE));

        private NFKD32ModeImpl() {
        }
    }

    private static final class NFC32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFCInstance(), Unicode32.INSTANCE));

        private NFC32ModeImpl() {
        }
    }

    private static final class NFKC32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Normalizer2.getNFKCInstance(), Unicode32.INSTANCE));

        private NFKC32ModeImpl() {
        }
    }

    private static final class FCD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Norm2AllModes.getFCDNormalizer2(), Unicode32.INSTANCE));

        private FCD32ModeImpl() {
        }
    }

    @Deprecated
    public static abstract class Mode {
        /* access modifiers changed from: protected */
        @Deprecated
        public abstract Normalizer2 getNormalizer2(int i);

        @Deprecated
        protected Mode() {
        }
    }

    private static final class NONEMode extends Mode {
        private NONEMode() {
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.text.Normalizer.Mode
        public Normalizer2 getNormalizer2(int i) {
            return Norm2AllModes.NOOP_NORMALIZER2;
        }
    }

    private static final class NFDMode extends Mode {
        private NFDMode() {
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.text.Normalizer.Mode
        public Normalizer2 getNormalizer2(int i) {
            return ((i & 32) != 0 ? NFD32ModeImpl.INSTANCE : NFDModeImpl.INSTANCE).normalizer2;
        }
    }

    private static final class NFKDMode extends Mode {
        private NFKDMode() {
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.text.Normalizer.Mode
        public Normalizer2 getNormalizer2(int i) {
            return ((i & 32) != 0 ? NFKD32ModeImpl.INSTANCE : NFKDModeImpl.INSTANCE).normalizer2;
        }
    }

    private static final class NFCMode extends Mode {
        private NFCMode() {
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.text.Normalizer.Mode
        public Normalizer2 getNormalizer2(int i) {
            return ((i & 32) != 0 ? NFC32ModeImpl.INSTANCE : NFCModeImpl.INSTANCE).normalizer2;
        }
    }

    private static final class NFKCMode extends Mode {
        private NFKCMode() {
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.text.Normalizer.Mode
        public Normalizer2 getNormalizer2(int i) {
            return ((i & 32) != 0 ? NFKC32ModeImpl.INSTANCE : NFKCModeImpl.INSTANCE).normalizer2;
        }
    }

    private static final class FCDMode extends Mode {
        private FCDMode() {
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.text.Normalizer.Mode
        public Normalizer2 getNormalizer2(int i) {
            return ((i & 32) != 0 ? FCD32ModeImpl.INSTANCE : FCDModeImpl.INSTANCE).normalizer2;
        }
    }

    public static final class QuickCheckResult {
        private QuickCheckResult(int i) {
        }
    }

    @Deprecated
    public Normalizer(String str, Mode mode2, int i) {
        this.text = UCharacterIterator.getInstance(str);
        this.mode = mode2;
        this.options = i;
        this.norm2 = mode2.getNormalizer2(i);
        this.buffer = new StringBuilder();
    }

    @Deprecated
    public Normalizer(CharacterIterator characterIterator, Mode mode2, int i) {
        this.text = UCharacterIterator.getInstance((CharacterIterator) characterIterator.clone());
        this.mode = mode2;
        this.options = i;
        this.norm2 = mode2.getNormalizer2(i);
        this.buffer = new StringBuilder();
    }

    @Deprecated
    public Normalizer(UCharacterIterator uCharacterIterator, Mode mode2, int i) {
        try {
            this.text = (UCharacterIterator) uCharacterIterator.clone();
            this.mode = mode2;
            this.options = i;
            this.norm2 = mode2.getNormalizer2(i);
            this.buffer = new StringBuilder();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    @Override // java.lang.Object
    @Deprecated
    public Object clone() {
        try {
            Normalizer normalizer = (Normalizer) super.clone();
            normalizer.text = (UCharacterIterator) this.text.clone();
            normalizer.mode = this.mode;
            normalizer.options = this.options;
            normalizer.norm2 = this.norm2;
            normalizer.buffer = new StringBuilder(this.buffer);
            normalizer.bufferPos = this.bufferPos;
            normalizer.currentIndex = this.currentIndex;
            normalizer.nextIndex = this.nextIndex;
            return normalizer;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    private static final Normalizer2 getComposeNormalizer2(boolean z, int i) {
        return (z ? NFKC : NFC).getNormalizer2(i);
    }

    private static final Normalizer2 getDecomposeNormalizer2(boolean z, int i) {
        return (z ? NFKD : NFD).getNormalizer2(i);
    }

    @Deprecated
    public static String compose(String str, boolean z) {
        return compose(str, z, 0);
    }

    @Deprecated
    public static String compose(String str, boolean z, int i) {
        return getComposeNormalizer2(z, i).normalize(str);
    }

    @Deprecated
    public static int compose(char[] cArr, char[] cArr2, boolean z, int i) {
        return compose(cArr, 0, cArr.length, cArr2, 0, cArr2.length, z, i);
    }

    @Deprecated
    public static int compose(char[] cArr, int i, int i2, char[] cArr2, int i3, int i4, boolean z, int i5) {
        CharBuffer wrap = CharBuffer.wrap(cArr, i, i2 - i);
        CharsAppendable charsAppendable = new CharsAppendable(cArr2, i3, i4);
        getComposeNormalizer2(z, i5).normalize(wrap, charsAppendable);
        return charsAppendable.length();
    }

    @Deprecated
    public static String decompose(String str, boolean z) {
        return decompose(str, z, 0);
    }

    @Deprecated
    public static String decompose(String str, boolean z, int i) {
        return getDecomposeNormalizer2(z, i).normalize(str);
    }

    @Deprecated
    public static int decompose(char[] cArr, char[] cArr2, boolean z, int i) {
        return decompose(cArr, 0, cArr.length, cArr2, 0, cArr2.length, z, i);
    }

    @Deprecated
    public static int decompose(char[] cArr, int i, int i2, char[] cArr2, int i3, int i4, boolean z, int i5) {
        CharBuffer wrap = CharBuffer.wrap(cArr, i, i2 - i);
        CharsAppendable charsAppendable = new CharsAppendable(cArr2, i3, i4);
        getDecomposeNormalizer2(z, i5).normalize(wrap, charsAppendable);
        return charsAppendable.length();
    }

    @Deprecated
    public static String normalize(String str, Mode mode2, int i) {
        return mode2.getNormalizer2(i).normalize(str);
    }

    @Deprecated
    public static String normalize(String str, Mode mode2) {
        return normalize(str, mode2, 0);
    }

    @Deprecated
    public static int normalize(char[] cArr, char[] cArr2, Mode mode2, int i) {
        return normalize(cArr, 0, cArr.length, cArr2, 0, cArr2.length, mode2, i);
    }

    @Deprecated
    public static int normalize(char[] cArr, int i, int i2, char[] cArr2, int i3, int i4, Mode mode2, int i5) {
        CharBuffer wrap = CharBuffer.wrap(cArr, i, i2 - i);
        CharsAppendable charsAppendable = new CharsAppendable(cArr2, i3, i4);
        mode2.getNormalizer2(i5).normalize(wrap, charsAppendable);
        return charsAppendable.length();
    }

    @Deprecated
    public static String normalize(int i, Mode mode2, int i2) {
        if (mode2 != NFD || i2 != 0) {
            return normalize(UTF16.valueOf(i), mode2, i2);
        }
        String decomposition = Normalizer2.getNFCInstance().getDecomposition(i);
        return decomposition == null ? UTF16.valueOf(i) : decomposition;
    }

    @Deprecated
    public static String normalize(int i, Mode mode2) {
        return normalize(i, mode2, 0);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(String str, Mode mode2) {
        return quickCheck(str, mode2, 0);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(String str, Mode mode2, int i) {
        return mode2.getNormalizer2(i).quickCheck(str);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(char[] cArr, Mode mode2, int i) {
        return quickCheck(cArr, 0, cArr.length, mode2, i);
    }

    @Deprecated
    public static QuickCheckResult quickCheck(char[] cArr, int i, int i2, Mode mode2, int i3) {
        return mode2.getNormalizer2(i3).quickCheck(CharBuffer.wrap(cArr, i, i2 - i));
    }

    @Deprecated
    public static boolean isNormalized(char[] cArr, int i, int i2, Mode mode2, int i3) {
        return mode2.getNormalizer2(i3).isNormalized(CharBuffer.wrap(cArr, i, i2 - i));
    }

    @Deprecated
    public static boolean isNormalized(String str, Mode mode2, int i) {
        return mode2.getNormalizer2(i).isNormalized(str);
    }

    @Deprecated
    public static boolean isNormalized(int i, Mode mode2, int i2) {
        return isNormalized(UTF16.valueOf(i), mode2, i2);
    }

    public static int compare(char[] cArr, int i, int i2, char[] cArr2, int i3, int i4, int i5) {
        if (cArr != null && i >= 0 && i2 >= 0 && cArr2 != null && i3 >= 0 && i4 >= 0 && i2 >= i && i4 >= i3) {
            return internalCompare(CharBuffer.wrap(cArr, i, i2 - i), CharBuffer.wrap(cArr2, i3, i4 - i3), i5);
        }
        throw new IllegalArgumentException();
    }

    public static int compare(String str, String str2, int i) {
        return internalCompare(str, str2, i);
    }

    public static int compare(char[] cArr, char[] cArr2, int i) {
        return internalCompare(CharBuffer.wrap(cArr), CharBuffer.wrap(cArr2), i);
    }

    public static int compare(int i, int i2, int i3) {
        return internalCompare(UTF16.valueOf(i), UTF16.valueOf(i2), i3 | 131072);
    }

    public static int compare(int i, String str, int i2) {
        return internalCompare(UTF16.valueOf(i), str, i2);
    }

    @Deprecated
    public static int concatenate(char[] cArr, int i, int i2, char[] cArr2, int i3, int i4, char[] cArr3, int i5, int i6, Mode mode2, int i7) {
        if (cArr3 == null) {
            throw new IllegalArgumentException();
        } else if (cArr2 != cArr3 || i3 >= i6 || i5 >= i4) {
            int i8 = i2 - i;
            StringBuilder sb = new StringBuilder(((i8 + i4) - i3) + 16);
            sb.append(cArr, i, i8);
            mode2.getNormalizer2(i7).append(sb, CharBuffer.wrap(cArr2, i3, i4 - i3));
            int length = sb.length();
            if (length <= i6 - i5) {
                sb.getChars(0, length, cArr3, i5);
                return length;
            }
            throw new IndexOutOfBoundsException(Integer.toString(length));
        } else {
            throw new IllegalArgumentException("overlapping right and dst ranges");
        }
    }

    @Deprecated
    public static String concatenate(char[] cArr, char[] cArr2, Mode mode2, int i) {
        StringBuilder sb = new StringBuilder(cArr.length + cArr2.length + 16);
        sb.append(cArr);
        return mode2.getNormalizer2(i).append(sb, CharBuffer.wrap(cArr2)).toString();
    }

    @Deprecated
    public static String concatenate(String str, String str2, Mode mode2, int i) {
        StringBuilder sb = new StringBuilder(str.length() + str2.length() + 16);
        sb.append(str);
        return mode2.getNormalizer2(i).append(sb, str2).toString();
    }

    @Deprecated
    public static int getFC_NFKC_Closure(int i, char[] cArr) {
        String fC_NFKC_Closure = getFC_NFKC_Closure(i);
        int length = fC_NFKC_Closure.length();
        if (!(length == 0 || cArr == null || length > cArr.length)) {
            fC_NFKC_Closure.getChars(0, length, cArr, 0);
        }
        return length;
    }

    @Deprecated
    public static String getFC_NFKC_Closure(int i) {
        Norm2AllModes.Normalizer2WithImpl normalizer2WithImpl = NFKCModeImpl.INSTANCE.normalizer2;
        UCaseProps uCaseProps = UCaseProps.INSTANCE;
        StringBuilder sb = new StringBuilder();
        int fullFolding = uCaseProps.toFullFolding(i, sb, 0);
        if (fullFolding < 0) {
            Normalizer2Impl normalizer2Impl = normalizer2WithImpl.impl;
            if (normalizer2Impl.getCompQuickCheck(normalizer2Impl.getNorm16(i)) != 0) {
                return "";
            }
            sb.appendCodePoint(i);
        } else if (fullFolding > 31) {
            sb.appendCodePoint(fullFolding);
        }
        String normalize = normalizer2WithImpl.normalize(sb);
        String normalize2 = normalizer2WithImpl.normalize(UCharacter.foldCase(normalize, 0));
        if (normalize.equals(normalize2)) {
            return "";
        }
        return normalize2;
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
        int codePointAt = this.buffer.codePointAt(this.bufferPos);
        this.bufferPos += Character.charCount(codePointAt);
        return codePointAt;
    }

    @Deprecated
    public int previous() {
        if (this.bufferPos <= 0 && !previousNormalize()) {
            return -1;
        }
        int codePointBefore = this.buffer.codePointBefore(this.bufferPos);
        this.bufferPos -= Character.charCount(codePointBefore);
        return codePointBefore;
    }

    @Deprecated
    public void reset() {
        this.text.setToStart();
        this.nextIndex = 0;
        this.currentIndex = 0;
        clearBuffer();
    }

    @Deprecated
    public void setIndexOnly(int i) {
        this.text.setIndex(i);
        this.nextIndex = i;
        this.currentIndex = i;
        clearBuffer();
    }

    @Deprecated
    public int setIndex(int i) {
        setIndexOnly(i);
        return current();
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
    public int endIndex() {
        return this.text.getLength();
    }

    @Deprecated
    public void setMode(Mode mode2) {
        this.mode = mode2;
        this.norm2 = this.mode.getNormalizer2(this.options);
    }

    @Deprecated
    public Mode getMode() {
        return this.mode;
    }

    @Deprecated
    public void setOption(int i, boolean z) {
        if (z) {
            this.options = i | this.options;
        } else {
            this.options = (~i) & this.options;
        }
        this.norm2 = this.mode.getNormalizer2(this.options);
    }

    @Deprecated
    public int getOption(int i) {
        return (this.options & i) != 0 ? 1 : 0;
    }

    @Deprecated
    public int getText(char[] cArr) {
        return this.text.getText(cArr);
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
    public void setText(StringBuffer stringBuffer) {
        UCharacterIterator instance = UCharacterIterator.getInstance(stringBuffer);
        if (instance != null) {
            this.text = instance;
            reset();
            return;
        }
        throw new IllegalStateException("Could not create a new UCharacterIterator");
    }

    @Deprecated
    public void setText(char[] cArr) {
        UCharacterIterator instance = UCharacterIterator.getInstance(cArr);
        if (instance != null) {
            this.text = instance;
            reset();
            return;
        }
        throw new IllegalStateException("Could not create a new UCharacterIterator");
    }

    @Deprecated
    public void setText(String str) {
        UCharacterIterator instance = UCharacterIterator.getInstance(str);
        if (instance != null) {
            this.text = instance;
            reset();
            return;
        }
        throw new IllegalStateException("Could not create a new UCharacterIterator");
    }

    @Deprecated
    public void setText(CharacterIterator characterIterator) {
        UCharacterIterator instance = UCharacterIterator.getInstance(characterIterator);
        if (instance != null) {
            this.text = instance;
            reset();
            return;
        }
        throw new IllegalStateException("Could not create a new UCharacterIterator");
    }

    @Deprecated
    public void setText(UCharacterIterator uCharacterIterator) {
        try {
            UCharacterIterator uCharacterIterator2 = (UCharacterIterator) uCharacterIterator.clone();
            if (uCharacterIterator2 != null) {
                this.text = uCharacterIterator2;
                reset();
                return;
            }
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Could not clone the UCharacterIterator", e);
        }
    }

    private void clearBuffer() {
        this.buffer.setLength(0);
        this.bufferPos = 0;
    }

    private boolean nextNormalize() {
        clearBuffer();
        int i = this.nextIndex;
        this.currentIndex = i;
        this.text.setIndex(i);
        int nextCodePoint = this.text.nextCodePoint();
        if (nextCodePoint < 0) {
            return false;
        }
        StringBuilder appendCodePoint = new StringBuilder().appendCodePoint(nextCodePoint);
        while (true) {
            int nextCodePoint2 = this.text.nextCodePoint();
            if (nextCodePoint2 < 0) {
                break;
            } else if (this.norm2.hasBoundaryBefore(nextCodePoint2)) {
                this.text.moveCodePointIndex(-1);
                break;
            } else {
                appendCodePoint.appendCodePoint(nextCodePoint2);
            }
        }
        this.nextIndex = this.text.getIndex();
        this.norm2.normalize((CharSequence) appendCodePoint, this.buffer);
        if (this.buffer.length() != 0) {
            return true;
        }
        return false;
    }

    private boolean previousNormalize() {
        int previousCodePoint;
        clearBuffer();
        int i = this.currentIndex;
        this.nextIndex = i;
        this.text.setIndex(i);
        StringBuilder sb = new StringBuilder();
        do {
            previousCodePoint = this.text.previousCodePoint();
            if (previousCodePoint < 0) {
                break;
            } else if (previousCodePoint <= 65535) {
                sb.insert(0, (char) previousCodePoint);
            } else {
                sb.insert(0, Character.toChars(previousCodePoint));
            }
        } while (!this.norm2.hasBoundaryBefore(previousCodePoint));
        this.currentIndex = this.text.getIndex();
        this.norm2.normalize((CharSequence) sb, this.buffer);
        this.bufferPos = this.buffer.length();
        if (this.buffer.length() != 0) {
            return true;
        }
        return false;
    }

    private static int internalCompare(CharSequence charSequence, CharSequence charSequence2, int i) {
        Normalizer2 normalizer2;
        int i2 = i >>> 20;
        int i3 = i | 524288;
        if ((131072 & i3) == 0 || (i3 & 1) != 0) {
            if ((i3 & 1) != 0) {
                normalizer2 = NFD.getNormalizer2(i2);
            } else {
                normalizer2 = FCD.getNormalizer2(i2);
            }
            int spanQuickCheckYes = normalizer2.spanQuickCheckYes(charSequence);
            int spanQuickCheckYes2 = normalizer2.spanQuickCheckYes(charSequence2);
            if (spanQuickCheckYes < charSequence.length()) {
                StringBuilder sb = new StringBuilder(charSequence.length() + 16);
                sb.append(charSequence, 0, spanQuickCheckYes);
                charSequence = normalizer2.normalizeSecondAndAppend(sb, charSequence.subSequence(spanQuickCheckYes, charSequence.length()));
            }
            if (spanQuickCheckYes2 < charSequence2.length()) {
                StringBuilder sb2 = new StringBuilder(charSequence2.length() + 16);
                sb2.append(charSequence2, 0, spanQuickCheckYes2);
                charSequence2 = normalizer2.normalizeSecondAndAppend(sb2, charSequence2.subSequence(spanQuickCheckYes2, charSequence2.length()));
            }
        }
        return cmpEquivFold(charSequence, charSequence2, i3);
    }

    /* access modifiers changed from: private */
    public static final class CmpEquivLevel {
        CharSequence cs;
        int s;

        private CmpEquivLevel() {
        }
    }

    private static final CmpEquivLevel[] createCmpEquivLevelStack() {
        return new CmpEquivLevel[]{new CmpEquivLevel(), new CmpEquivLevel()};
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:188:0x0040 */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x01f8  */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x020d  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0216  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0241 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x02ad  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x02ce  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x02e1  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x02ea  */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x0301 A[EDGE_INSN: B:182:0x0301->B:147:0x0301 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00c3  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00df  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x012f  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0166 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x01cb A[ADDED_TO_REGION] */
    static int cmpEquivFold(CharSequence charSequence, CharSequence charSequence2, int i) {
        StringBuilder sb;
        StringBuilder sb2;
        UCaseProps uCaseProps;
        int i2;
        CharSequence charSequence3;
        int i3;
        int i4;
        int i5;
        int i6;
        Normalizer2Impl normalizer2Impl;
        int i7;
        CharSequence charSequence4;
        int i8;
        int i9;
        char c;
        int i10;
        int i11;
        int i12;
        char c2;
        int i13;
        int i14;
        int i15;
        int i16;
        StringBuilder sb3;
        Normalizer2Impl normalizer2Impl2;
        String decomposition;
        int fullFolding;
        int i17;
        CharSequence charSequence5;
        int i18 = 524288 & i;
        Normalizer2Impl normalizer2Impl3 = i18 != 0 ? Norm2AllModes.getNFCInstance().impl : null;
        int i19 = 65536 & i;
        if (i19 != 0) {
            uCaseProps = UCaseProps.INSTANCE;
            sb2 = new StringBuilder();
            sb = new StringBuilder();
        } else {
            uCaseProps = null;
            sb2 = null;
            sb = null;
        }
        int i20 = -1;
        CharSequence charSequence6 = charSequence;
        StringBuilder sb4 = charSequence2;
        int length = charSequence.length();
        int length2 = charSequence2.length();
        int i21 = -1;
        int i22 = -1;
        int i23 = 0;
        CmpEquivLevel[] cmpEquivLevelArr = null;
        int i24 = 0;
        int i25 = 0;
        CmpEquivLevel[] cmpEquivLevelArr2 = null;
        int i26 = 0;
        while (true) {
            if (i21 < 0) {
                int i27 = length;
                while (true) {
                    if (i24 != i27) {
                        int i28 = i24 + 1;
                        int charAt = charSequence6.charAt(i24);
                        i5 = i23;
                        charSequence3 = charSequence6;
                        i3 = i27;
                        i4 = i28;
                        i2 = charAt;
                        break;
                    } else if (i23 == 0) {
                        i5 = i23;
                        charSequence3 = charSequence6;
                        i3 = i27;
                        i4 = i24;
                        i2 = i20;
                        break;
                    } else {
                        do {
                            i23 += i20;
                            charSequence6 = cmpEquivLevelArr[i23].cs;
                        } while (charSequence6 == null);
                        i24 = cmpEquivLevelArr[i23].s;
                        i27 = charSequence6.length();
                    }
                }
            } else {
                i5 = i23;
                charSequence3 = charSequence6;
                i3 = length;
                i2 = i21;
                i4 = i24;
            }
            if (i22 < 0) {
                CharSequence charSequence7 = sb4;
                int i29 = i26;
                int i30 = length2;
                while (i29 == i30) {
                    if (i25 == 0) {
                        normalizer2Impl = normalizer2Impl3;
                        i8 = i30;
                        i7 = i29;
                        i22 = i20;
                        charSequence4 = charSequence7;
                    } else {
                        do {
                            i25--;
                            charSequence5 = cmpEquivLevelArr2[i25].cs;
                        } while (charSequence5 == null);
                        int i31 = cmpEquivLevelArr2[i25].s;
                        int length3 = charSequence5.length();
                        charSequence7 = charSequence5;
                        i29 = i31;
                        i30 = length3;
                    }
                }
                int i32 = i29 + 1;
                charSequence4 = charSequence7;
                i22 = charSequence4.charAt(i29);
                i6 = i18;
                i9 = i25;
                normalizer2Impl = normalizer2Impl3;
                i8 = i30;
                i7 = i32;
                if (i2 != i22) {
                    if (i2 < 0) {
                        return 0;
                    }
                    length2 = i8;
                    i24 = i4;
                    i26 = i7;
                    length = i3;
                    charSequence6 = charSequence3;
                    normalizer2Impl3 = normalizer2Impl;
                    i21 = -1;
                    i22 = -1;
                    i23 = i5;
                    sb4 = charSequence4;
                    i20 = -1;
                } else if (i2 < 0) {
                    return -1;
                } else {
                    if (i22 < 0) {
                        return 1;
                    }
                    c = (char) i2;
                    if (UTF16.isSurrogate(c)) {
                        if (!Normalizer2Impl.UTF16Plus.isSurrogateLead(i2)) {
                            i11 = i3;
                            int i33 = i4 - 2;
                            if (i33 >= 0) {
                                char charAt2 = charSequence3.charAt(i33);
                                if (Character.isHighSurrogate(charAt2)) {
                                    i12 = Character.toCodePoint(charAt2, c);
                                }
                            }
                            i10 = i9;
                            i12 = i2;
                            c2 = (char) i22;
                            if (UTF16.isSurrogate(c2)) {
                            }
                            i14 = i8;
                            int i34 = i22;
                            i13 = i34 == 1 ? 1 : 0;
                            i15 = i34;
                            if (i5 == 0) {
                            }
                            if (i10 == 0) {
                            }
                            sb3 = sb;
                            i16 = i19;
                            if (i5 < 2) {
                            }
                            normalizer2Impl2 = normalizer2Impl;
                            int i35 = i15 == 1 ? 1 : 0;
                            int i36 = i15 == 1 ? 1 : 0;
                            decomposition = normalizer2Impl2.getDecomposition(i35);
                            if (decomposition == null) {
                            }
                        } else if (i4 != i3) {
                            i11 = i3;
                            char charAt3 = charSequence3.charAt(i4);
                            if (Character.isLowSurrogate(charAt3)) {
                                i12 = Character.toCodePoint(c, charAt3);
                            }
                            i10 = i9;
                            i12 = i2;
                            c2 = (char) i22;
                            if (UTF16.isSurrogate(c2)) {
                                if (!Normalizer2Impl.UTF16Plus.isSurrogateLead(i22)) {
                                    i14 = i8;
                                    int i37 = i7 - 2;
                                    if (i37 >= 0) {
                                        char charAt4 = charSequence4.charAt(i37);
                                        if (Character.isHighSurrogate(charAt4)) {
                                            i17 = Character.toCodePoint(charAt4, c2);
                                        }
                                    }
                                    int i342 = i22;
                                    i13 = i342 == 1 ? 1 : 0;
                                    i15 = i342;
                                    if (i5 == 0) {
                                    }
                                    if (i10 == 0) {
                                    }
                                    sb3 = sb;
                                    i16 = i19;
                                    if (i5 < 2) {
                                    }
                                    normalizer2Impl2 = normalizer2Impl;
                                    int i352 = i15 == 1 ? 1 : 0;
                                    int i362 = i15 == 1 ? 1 : 0;
                                    decomposition = normalizer2Impl2.getDecomposition(i352);
                                    if (decomposition == null) {
                                    }
                                } else if (i7 != i8) {
                                    i14 = i8;
                                    char charAt5 = charSequence4.charAt(i7);
                                    if (Character.isLowSurrogate(charAt5)) {
                                        i17 = Character.toCodePoint(c2, charAt5);
                                    }
                                    int i3422 = i22;
                                    i13 = i3422 == 1 ? 1 : 0;
                                    i15 = i3422;
                                    if (i5 == 0 || i19 == 0 || (fullFolding = uCaseProps.toFullFolding(i12, sb2, i)) < 0) {
                                        if (i10 == 0 || i19 == 0) {
                                            sb3 = sb;
                                            i16 = i19;
                                        } else {
                                            sb3 = sb;
                                            i16 = i19;
                                            int fullFolding2 = uCaseProps.toFullFolding(i15, sb3, i);
                                            if (fullFolding2 >= 0) {
                                                if (UTF16.isSurrogate(c2)) {
                                                    if (Normalizer2Impl.UTF16Plus.isSurrogateLead(i13)) {
                                                        i7++;
                                                    } else {
                                                        int i38 = i4 - 1;
                                                        i24 = i38;
                                                        i21 = charSequence3.charAt(i38 - 1);
                                                        if (cmpEquivLevelArr2 == null) {
                                                            cmpEquivLevelArr2 = createCmpEquivLevelStack();
                                                        }
                                                        cmpEquivLevelArr2[0].cs = charSequence4;
                                                        cmpEquivLevelArr2[0].s = i7;
                                                        int i39 = i10 + 1;
                                                        if (fullFolding2 > 31) {
                                                            sb3.delete(0, sb3.length() - fullFolding2);
                                                        } else {
                                                            sb3.setLength(0);
                                                            sb3.appendCodePoint(fullFolding2);
                                                        }
                                                        int length4 = sb3.length();
                                                        sb = sb3;
                                                        charSequence6 = charSequence3;
                                                        i20 = -1;
                                                        length = i11;
                                                        i23 = i5;
                                                        i22 = -1;
                                                        i26 = 0;
                                                        i18 = i6;
                                                        i25 = i39;
                                                        normalizer2Impl3 = normalizer2Impl;
                                                        sb4 = sb;
                                                        length2 = length4;
                                                        i19 = i16;
                                                    }
                                                }
                                                i24 = i4;
                                                i21 = i2;
                                                if (cmpEquivLevelArr2 == null) {
                                                }
                                                cmpEquivLevelArr2[0].cs = charSequence4;
                                                cmpEquivLevelArr2[0].s = i7;
                                                int i392 = i10 + 1;
                                                if (fullFolding2 > 31) {
                                                }
                                                int length42 = sb3.length();
                                                sb = sb3;
                                                charSequence6 = charSequence3;
                                                i20 = -1;
                                                length = i11;
                                                i23 = i5;
                                                i22 = -1;
                                                i26 = 0;
                                                i18 = i6;
                                                i25 = i392;
                                                normalizer2Impl3 = normalizer2Impl;
                                                sb4 = sb;
                                                length2 = length42;
                                                i19 = i16;
                                            }
                                        }
                                        if (i5 < 2 || i6 == 0) {
                                            normalizer2Impl2 = normalizer2Impl;
                                        } else {
                                            normalizer2Impl2 = normalizer2Impl;
                                            String decomposition2 = normalizer2Impl2.getDecomposition(i12);
                                            if (decomposition2 != null) {
                                                if (UTF16.isSurrogate(c)) {
                                                    if (Normalizer2Impl.UTF16Plus.isSurrogateLead(i2)) {
                                                        i4++;
                                                    } else {
                                                        i7--;
                                                        i13 = charSequence4.charAt(i7 - 1);
                                                    }
                                                }
                                                if (cmpEquivLevelArr == null) {
                                                    cmpEquivLevelArr = createCmpEquivLevelStack();
                                                }
                                                cmpEquivLevelArr[i5].cs = charSequence3;
                                                cmpEquivLevelArr[i5].s = i4;
                                                int i40 = i5 + 1;
                                                if (i40 < 2) {
                                                    cmpEquivLevelArr[i40].cs = null;
                                                    i23 = i40 + 1;
                                                } else {
                                                    i23 = i40;
                                                }
                                                length = decomposition2.length();
                                                normalizer2Impl3 = normalizer2Impl2;
                                                sb = sb3;
                                                sb4 = charSequence4;
                                                i18 = i6;
                                                i21 = -1;
                                                i20 = -1;
                                                i19 = i16;
                                                length2 = i14;
                                                i22 = i13;
                                                i24 = 0;
                                                i25 = i10;
                                                charSequence6 = decomposition2;
                                            }
                                        }
                                        if (i10 >= 2 || i6 == 0) {
                                            break;
                                        }
                                        int i3522 = i15 == 1 ? 1 : 0;
                                        int i3622 = i15 == 1 ? 1 : 0;
                                        decomposition = normalizer2Impl2.getDecomposition(i3522);
                                        if (decomposition == null) {
                                            break;
                                        }
                                        if (UTF16.isSurrogate(c2)) {
                                            if (Normalizer2Impl.UTF16Plus.isSurrogateLead(i13)) {
                                                i7++;
                                            } else {
                                                int i41 = i4 - 1;
                                                i24 = i41;
                                                i21 = charSequence3.charAt(i41 - 1);
                                                if (cmpEquivLevelArr2 == null) {
                                                    cmpEquivLevelArr2 = createCmpEquivLevelStack();
                                                }
                                                cmpEquivLevelArr2[i10].cs = charSequence4;
                                                cmpEquivLevelArr2[i10].s = i7;
                                                i9 = i10 + 1;
                                                if (i9 >= 2) {
                                                    cmpEquivLevelArr2[i9].cs = null;
                                                    i9++;
                                                }
                                                normalizer2Impl3 = normalizer2Impl2;
                                                sb = sb3;
                                                charSequence6 = charSequence3;
                                                i22 = -1;
                                                i20 = -1;
                                                i19 = i16;
                                                length = i11;
                                                i26 = 0;
                                                i23 = i5;
                                                length2 = decomposition.length();
                                                sb4 = decomposition;
                                            }
                                        }
                                        i24 = i4;
                                        i21 = i2;
                                        if (cmpEquivLevelArr2 == null) {
                                        }
                                        cmpEquivLevelArr2[i10].cs = charSequence4;
                                        cmpEquivLevelArr2[i10].s = i7;
                                        i9 = i10 + 1;
                                        if (i9 >= 2) {
                                        }
                                        normalizer2Impl3 = normalizer2Impl2;
                                        sb = sb3;
                                        charSequence6 = charSequence3;
                                        i22 = -1;
                                        i20 = -1;
                                        i19 = i16;
                                        length = i11;
                                        i26 = 0;
                                        i23 = i5;
                                        length2 = decomposition.length();
                                        sb4 = decomposition;
                                    } else {
                                        if (UTF16.isSurrogate(c)) {
                                            if (Normalizer2Impl.UTF16Plus.isSurrogateLead(i2)) {
                                                i4++;
                                            } else {
                                                i7--;
                                                i13 = charSequence4.charAt(i7 - 1);
                                            }
                                        }
                                        if (cmpEquivLevelArr == null) {
                                            cmpEquivLevelArr = createCmpEquivLevelStack();
                                        }
                                        cmpEquivLevelArr[0].cs = charSequence3;
                                        cmpEquivLevelArr[0].s = i4;
                                        i23 = i5 + 1;
                                        if (fullFolding <= 31) {
                                            sb2.delete(0, sb2.length() - fullFolding);
                                        } else {
                                            sb2.setLength(0);
                                            sb2.appendCodePoint(fullFolding);
                                        }
                                        length = sb2.length();
                                        charSequence6 = sb2;
                                        normalizer2Impl3 = normalizer2Impl;
                                        i18 = i6;
                                        i21 = -1;
                                        sb = sb;
                                        length2 = i14;
                                        i22 = i13;
                                        i24 = 0;
                                        i25 = i10;
                                        sb4 = charSequence4;
                                        i20 = -1;
                                    }
                                    i26 = i7;
                                }
                                i13 = i22;
                                i15 = i17;
                                if (i5 == 0) {
                                }
                                if (i10 == 0) {
                                }
                                sb3 = sb;
                                i16 = i19;
                                if (i5 < 2) {
                                }
                                normalizer2Impl2 = normalizer2Impl;
                                int i35222 = i15 == 1 ? 1 : 0;
                                int i36222 = i15 == 1 ? 1 : 0;
                                decomposition = normalizer2Impl2.getDecomposition(i35222);
                                if (decomposition == null) {
                                }
                            }
                            i14 = i8;
                            int i34222 = i22;
                            i13 = i34222 == 1 ? 1 : 0;
                            i15 = i34222;
                            if (i5 == 0) {
                            }
                            if (i10 == 0) {
                            }
                            sb3 = sb;
                            i16 = i19;
                            if (i5 < 2) {
                            }
                            normalizer2Impl2 = normalizer2Impl;
                            int i352222 = i15 == 1 ? 1 : 0;
                            int i362222 = i15 == 1 ? 1 : 0;
                            decomposition = normalizer2Impl2.getDecomposition(i352222);
                            if (decomposition == null) {
                            }
                        }
                        i10 = i9;
                        c2 = (char) i22;
                        if (UTF16.isSurrogate(c2)) {
                        }
                        i14 = i8;
                        int i342222 = i22;
                        i13 = i342222 == 1 ? 1 : 0;
                        i15 = i342222;
                        if (i5 == 0) {
                        }
                        if (i10 == 0) {
                        }
                        sb3 = sb;
                        i16 = i19;
                        if (i5 < 2) {
                        }
                        normalizer2Impl2 = normalizer2Impl;
                        int i3522222 = i15 == 1 ? 1 : 0;
                        int i3622222 = i15 == 1 ? 1 : 0;
                        decomposition = normalizer2Impl2.getDecomposition(i3522222);
                        if (decomposition == null) {
                        }
                    }
                    i11 = i3;
                    i10 = i9;
                    i12 = i2;
                    c2 = (char) i22;
                    if (UTF16.isSurrogate(c2)) {
                    }
                    i14 = i8;
                    int i3422222 = i22;
                    i13 = i3422222 == 1 ? 1 : 0;
                    i15 = i3422222;
                    if (i5 == 0) {
                    }
                    if (i10 == 0) {
                    }
                    sb3 = sb;
                    i16 = i19;
                    if (i5 < 2) {
                    }
                    normalizer2Impl2 = normalizer2Impl;
                    int i35222222 = i15 == 1 ? 1 : 0;
                    int i36222222 = i15 == 1 ? 1 : 0;
                    decomposition = normalizer2Impl2.getDecomposition(i35222222);
                    if (decomposition == null) {
                    }
                }
                i25 = i9;
                i18 = i6;
            } else {
                charSequence4 = sb4;
                i7 = i26;
                normalizer2Impl = normalizer2Impl3;
                i8 = length2;
            }
            i6 = i18;
            i9 = i25;
            if (i2 != i22) {
            }
            i25 = i9;
            i18 = i6;
        }
        int i42 = i13;
        if (i2 >= 55296 && i42 >= 55296 && (i & 32768) != 0) {
            if ((i2 > 56319 || i4 == i11 || !Character.isLowSurrogate(charSequence3.charAt(i4))) && (!Character.isLowSurrogate(c) || i4 - 1 == 0 || !Character.isHighSurrogate(charSequence3.charAt(i4 - 2)))) {
                i2 -= 10240;
            }
            if ((i42 > 56319 || i7 == i14 || !Character.isLowSurrogate(charSequence4.charAt(i7))) && (!Character.isLowSurrogate(c2) || i7 - 1 == 0 || !Character.isHighSurrogate(charSequence4.charAt(i7 - 2)))) {
                i42 -= 10240;
            }
        }
        return i2 - i42;
    }

    /* access modifiers changed from: private */
    public static final class CharsAppendable implements Appendable {
        private final char[] chars;
        private final int limit;
        private int offset;
        private final int start;

        public CharsAppendable(char[] cArr, int i, int i2) {
            this.chars = cArr;
            this.offset = i;
            this.start = i;
            this.limit = i2;
        }

        public int length() {
            int i = this.offset;
            int i2 = i - this.start;
            if (i <= this.limit) {
                return i2;
            }
            throw new IndexOutOfBoundsException(Integer.toString(i2));
        }

        @Override // java.lang.Appendable
        public Appendable append(char c) {
            int i = this.offset;
            if (i < this.limit) {
                this.chars[i] = c;
            }
            this.offset++;
            return this;
        }

        @Override // java.lang.Appendable
        public Appendable append(CharSequence charSequence) {
            return append(charSequence, 0, charSequence.length());
        }

        @Override // java.lang.Appendable
        public Appendable append(CharSequence charSequence, int i, int i2) {
            int i3 = i2 - i;
            int i4 = this.limit;
            int i5 = this.offset;
            if (i3 <= i4 - i5) {
                while (i < i2) {
                    char[] cArr = this.chars;
                    int i6 = this.offset;
                    this.offset = i6 + 1;
                    cArr[i6] = charSequence.charAt(i);
                    i++;
                }
            } else {
                this.offset = i5 + i3;
            }
            return this;
        }
    }
}
