package android.icu.text;

import android.icu.impl.CaseMapImpl;
import android.icu.impl.UCaseProps;
import android.icu.lang.UProperty;
import java.util.Locale;

public abstract class CaseMap {
    @Deprecated
    protected int internalOptions;

    public static final class Fold extends CaseMap {
        /* access modifiers changed from: private */
        public static final Fold DEFAULT = new Fold(0);
        private static final Fold OMIT_UNCHANGED = new Fold(16384);
        private static final Fold TURKIC = new Fold(1);
        private static final Fold TURKIC_OMIT_UNCHANGED = new Fold(UProperty.BIDI_MIRRORING_GLYPH);

        private Fold(int opt) {
            super(opt);
        }

        public Fold omitUnchangedText() {
            return (this.internalOptions & 1) == 0 ? OMIT_UNCHANGED : TURKIC_OMIT_UNCHANGED;
        }

        public Fold turkic() {
            return (this.internalOptions & 16384) == 0 ? TURKIC : TURKIC_OMIT_UNCHANGED;
        }

        public String apply(CharSequence src) {
            return CaseMapImpl.fold(this.internalOptions, src);
        }

        public <A extends Appendable> A apply(CharSequence src, A dest, Edits edits) {
            return CaseMapImpl.fold(this.internalOptions, src, dest, edits);
        }
    }

    public static final class Lower extends CaseMap {
        /* access modifiers changed from: private */
        public static final Lower DEFAULT = new Lower(0);
        private static final Lower OMIT_UNCHANGED = new Lower(16384);

        private Lower(int opt) {
            super(opt);
        }

        public Lower omitUnchangedText() {
            return OMIT_UNCHANGED;
        }

        public String apply(Locale locale, CharSequence src) {
            return CaseMapImpl.toLower(CaseMap.getCaseLocale(locale), this.internalOptions, src);
        }

        public <A extends Appendable> A apply(Locale locale, CharSequence src, A dest, Edits edits) {
            return CaseMapImpl.toLower(CaseMap.getCaseLocale(locale), this.internalOptions, src, dest, edits);
        }
    }

    public static final class Title extends CaseMap {
        /* access modifiers changed from: private */
        public static final Title DEFAULT = new Title(0);
        private static final Title OMIT_UNCHANGED = new Title(16384);

        private Title(int opt) {
            super(opt);
        }

        public Title wholeString() {
            return new Title(CaseMapImpl.addTitleIteratorOption(this.internalOptions, 32));
        }

        public Title sentences() {
            return new Title(CaseMapImpl.addTitleIteratorOption(this.internalOptions, 64));
        }

        public Title omitUnchangedText() {
            if (this.internalOptions == 0 || this.internalOptions == 16384) {
                return OMIT_UNCHANGED;
            }
            return new Title(16384 | this.internalOptions);
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

        public String apply(Locale locale, BreakIterator iter, CharSequence src) {
            if (iter == null && locale == null) {
                locale = Locale.getDefault();
            }
            BreakIterator iter2 = CaseMapImpl.getTitleBreakIterator(locale, this.internalOptions, iter);
            iter2.setText(src);
            return CaseMapImpl.toTitle(CaseMap.getCaseLocale(locale), this.internalOptions, iter2, src);
        }

        public <A extends Appendable> A apply(Locale locale, BreakIterator iter, CharSequence src, A dest, Edits edits) {
            if (iter == null && locale == null) {
                locale = Locale.getDefault();
            }
            BreakIterator iter2 = CaseMapImpl.getTitleBreakIterator(locale, this.internalOptions, iter);
            iter2.setText(src);
            return CaseMapImpl.toTitle(CaseMap.getCaseLocale(locale), this.internalOptions, iter2, src, dest, edits);
        }
    }

    public static final class Upper extends CaseMap {
        /* access modifiers changed from: private */
        public static final Upper DEFAULT = new Upper(0);
        private static final Upper OMIT_UNCHANGED = new Upper(16384);

        private Upper(int opt) {
            super(opt);
        }

        public Upper omitUnchangedText() {
            return OMIT_UNCHANGED;
        }

        public String apply(Locale locale, CharSequence src) {
            return CaseMapImpl.toUpper(CaseMap.getCaseLocale(locale), this.internalOptions, src);
        }

        public <A extends Appendable> A apply(Locale locale, CharSequence src, A dest, Edits edits) {
            return CaseMapImpl.toUpper(CaseMap.getCaseLocale(locale), this.internalOptions, src, dest, edits);
        }
    }

    public abstract CaseMap omitUnchangedText();

    private CaseMap(int opt) {
        this.internalOptions = opt;
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
}
