package android.icu.text;

import android.icu.impl.CaseMapImpl;
import android.icu.impl.UCaseProps;
import android.icu.lang.UProperty;
import java.util.Locale;

public abstract class CaseMap {
    @Deprecated
    protected int internalOptions;

    public static final class Fold extends CaseMap {
        private static final Fold DEFAULT = new Fold(0);
        private static final Fold OMIT_UNCHANGED = new Fold(16384);
        private static final Fold TURKIC = new Fold(1);
        private static final Fold TURKIC_OMIT_UNCHANGED = new Fold(UProperty.BIDI_MIRRORING_GLYPH);

        private Fold(int opt) {
            super(opt, null);
        }

        public Fold omitUnchangedText() {
            return (this.internalOptions & 1) == 0 ? OMIT_UNCHANGED : TURKIC_OMIT_UNCHANGED;
        }

        public Fold turkic() {
            return (this.internalOptions & 16384) == 0 ? TURKIC : TURKIC_OMIT_UNCHANGED;
        }

        public <A extends Appendable> A apply(CharSequence src, A dest, Edits edits) {
            return CaseMapImpl.fold(this.internalOptions, src, dest, edits);
        }
    }

    public static final class Lower extends CaseMap {
        private static final Lower DEFAULT = new Lower(0);
        private static final Lower OMIT_UNCHANGED = new Lower(16384);

        private Lower(int opt) {
            super(opt, null);
        }

        public Lower omitUnchangedText() {
            return OMIT_UNCHANGED;
        }

        public <A extends Appendable> A apply(Locale locale, CharSequence src, A dest, Edits edits) {
            return CaseMapImpl.toLower(CaseMap.getCaseLocale(locale), this.internalOptions, src, dest, edits);
        }
    }

    public static final class Title extends CaseMap {
        private static final Title DEFAULT = new Title(0);
        private static final Title OMIT_UNCHANGED = new Title(16384);

        private Title(int opt) {
            super(opt, null);
        }

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
            return new Title(this.internalOptions | 512);
        }

        public <A extends Appendable> A apply(Locale locale, BreakIterator iter, CharSequence src, A dest, Edits edits) {
            if (iter == null) {
                iter = BreakIterator.getWordInstance(locale);
            }
            iter.setText(src.toString());
            return CaseMapImpl.toTitle(CaseMap.getCaseLocale(locale), this.internalOptions, iter, src, dest, edits);
        }
    }

    public static final class Upper extends CaseMap {
        private static final Upper DEFAULT = new Upper(0);
        private static final Upper OMIT_UNCHANGED = new Upper(16384);

        private Upper(int opt) {
            super(opt, null);
        }

        public Upper omitUnchangedText() {
            return OMIT_UNCHANGED;
        }

        public <A extends Appendable> A apply(Locale locale, CharSequence src, A dest, Edits edits) {
            return CaseMapImpl.toUpper(CaseMap.getCaseLocale(locale), this.internalOptions, src, dest, edits);
        }
    }

    /* synthetic */ CaseMap(int opt, CaseMap -this1) {
        this(opt);
    }

    public abstract CaseMap omitUnchangedText();

    private CaseMap(int opt) {
        this.internalOptions = opt;
    }

    private static int getCaseLocale(Locale locale) {
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
