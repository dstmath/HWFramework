package android.icu.text;

import android.icu.impl.SimpleFilteredSentenceBreakIterator;
import android.icu.util.ULocale;
import java.util.Locale;

public abstract class FilteredBreakIteratorBuilder {
    public abstract boolean suppressBreakAfter(CharSequence charSequence);

    public abstract boolean unsuppressBreakAfter(CharSequence charSequence);

    public abstract BreakIterator wrapIteratorWithFilter(BreakIterator breakIterator);

    public static final FilteredBreakIteratorBuilder getInstance(Locale where) {
        return new SimpleFilteredSentenceBreakIterator.Builder(where);
    }

    public static final FilteredBreakIteratorBuilder getInstance(ULocale where) {
        return new SimpleFilteredSentenceBreakIterator.Builder(where);
    }

    public static final FilteredBreakIteratorBuilder getEmptyInstance() {
        return new SimpleFilteredSentenceBreakIterator.Builder();
    }

    @Deprecated
    protected FilteredBreakIteratorBuilder() {
    }
}
