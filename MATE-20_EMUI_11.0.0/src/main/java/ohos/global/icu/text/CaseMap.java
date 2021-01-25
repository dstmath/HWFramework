package ohos.global.icu.text;

import java.util.Locale;
import ohos.global.icu.impl.CaseMapImpl;
import ohos.global.icu.impl.UCaseProps;
import ohos.global.icu.lang.UProperty;

public abstract class CaseMap {
    @Deprecated
    protected int internalOptions;

    public abstract CaseMap omitUnchangedText();

    private CaseMap(int i) {
        this.internalOptions = i;
    }

    /* access modifiers changed from: private */
    public static int getCaseLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return UCaseProps.getCaseLocale(locale);
    }

    public static Lower toLower() {
        return Lower.DEFAULT;
    }

    public static Upper toUpper() {
        return Upper.DEFAULT;
    }

    public static Title toTitle() {
        return Title.DEFAULT;
    }

    public static Fold fold() {
        return Fold.DEFAULT;
    }

    public static final class Lower extends CaseMap {
        private static final Lower DEFAULT = new Lower(0);
        private static final Lower OMIT_UNCHANGED = new Lower(16384);

        private Lower(int i) {
            super(i);
        }

        @Override // ohos.global.icu.text.CaseMap
        public Lower omitUnchangedText() {
            return OMIT_UNCHANGED;
        }

        public String apply(Locale locale, CharSequence charSequence) {
            return CaseMapImpl.toLower(CaseMap.getCaseLocale(locale), this.internalOptions, charSequence);
        }

        public <A extends Appendable> A apply(Locale locale, CharSequence charSequence, A a, Edits edits) {
            return (A) CaseMapImpl.toLower(CaseMap.getCaseLocale(locale), this.internalOptions, charSequence, a, edits);
        }
    }

    public static final class Upper extends CaseMap {
        private static final Upper DEFAULT = new Upper(0);
        private static final Upper OMIT_UNCHANGED = new Upper(16384);

        private Upper(int i) {
            super(i);
        }

        @Override // ohos.global.icu.text.CaseMap
        public Upper omitUnchangedText() {
            return OMIT_UNCHANGED;
        }

        public String apply(Locale locale, CharSequence charSequence) {
            return CaseMapImpl.toUpper(CaseMap.getCaseLocale(locale), this.internalOptions, charSequence);
        }

        public <A extends Appendable> A apply(Locale locale, CharSequence charSequence, A a, Edits edits) {
            return (A) CaseMapImpl.toUpper(CaseMap.getCaseLocale(locale), this.internalOptions, charSequence, a, edits);
        }
    }

    public static final class Title extends CaseMap {
        private static final Title DEFAULT = new Title(0);
        private static final Title OMIT_UNCHANGED = new Title(16384);

        private Title(int i) {
            super(i);
        }

        public Title wholeString() {
            return new Title(CaseMapImpl.addTitleIteratorOption(this.internalOptions, 32));
        }

        public Title sentences() {
            return new Title(CaseMapImpl.addTitleIteratorOption(this.internalOptions, 64));
        }

        @Override // ohos.global.icu.text.CaseMap
        public Title omitUnchangedText() {
            if (this.internalOptions == 0 || this.internalOptions == 16384) {
                return OMIT_UNCHANGED;
            }
            return new Title(this.internalOptions | 16384);
        }

        public Title noLowercase() {
            return new Title(this.internalOptions | 256);
        }

        public Title noBreakAdjustment() {
            return new Title(CaseMapImpl.addTitleAdjustmentOption(this.internalOptions, 512));
        }

        public Title adjustToCased() {
            return new Title(CaseMapImpl.addTitleAdjustmentOption(this.internalOptions, 1024));
        }

        public String apply(Locale locale, BreakIterator breakIterator, CharSequence charSequence) {
            if (breakIterator == null && locale == null) {
                locale = Locale.getDefault();
            }
            BreakIterator titleBreakIterator = CaseMapImpl.getTitleBreakIterator(locale, this.internalOptions, breakIterator);
            titleBreakIterator.setText(charSequence);
            return CaseMapImpl.toTitle(CaseMap.getCaseLocale(locale), this.internalOptions, titleBreakIterator, charSequence);
        }

        public <A extends Appendable> A apply(Locale locale, BreakIterator breakIterator, CharSequence charSequence, A a, Edits edits) {
            if (breakIterator == null && locale == null) {
                locale = Locale.getDefault();
            }
            BreakIterator titleBreakIterator = CaseMapImpl.getTitleBreakIterator(locale, this.internalOptions, breakIterator);
            titleBreakIterator.setText(charSequence);
            return (A) CaseMapImpl.toTitle(CaseMap.getCaseLocale(locale), this.internalOptions, titleBreakIterator, charSequence, a, edits);
        }
    }

    public static final class Fold extends CaseMap {
        private static final Fold DEFAULT = new Fold(0);
        private static final Fold OMIT_UNCHANGED = new Fold(16384);
        private static final Fold TURKIC = new Fold(1);
        private static final Fold TURKIC_OMIT_UNCHANGED = new Fold(UProperty.BIDI_MIRRORING_GLYPH);

        private Fold(int i) {
            super(i);
        }

        @Override // ohos.global.icu.text.CaseMap
        public Fold omitUnchangedText() {
            return (this.internalOptions & 1) == 0 ? OMIT_UNCHANGED : TURKIC_OMIT_UNCHANGED;
        }

        public Fold turkic() {
            return (this.internalOptions & 16384) == 0 ? TURKIC : TURKIC_OMIT_UNCHANGED;
        }

        public String apply(CharSequence charSequence) {
            return CaseMapImpl.fold(this.internalOptions, charSequence);
        }

        public <A extends Appendable> A apply(CharSequence charSequence, A a, Edits edits) {
            return (A) CaseMapImpl.fold(this.internalOptions, charSequence, a, edits);
        }
    }
}
