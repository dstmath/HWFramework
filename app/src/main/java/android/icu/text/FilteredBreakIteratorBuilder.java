package android.icu.text;

import android.icu.impl.SimpleFilteredSentenceBreakIterator.Builder;
import android.icu.util.ULocale;

@Deprecated
public abstract class FilteredBreakIteratorBuilder {
    @Deprecated
    public abstract BreakIterator build(BreakIterator breakIterator);

    @Deprecated
    public abstract boolean suppressBreakAfter(String str);

    @Deprecated
    public abstract boolean unsuppressBreakAfter(String str);

    @Deprecated
    public static FilteredBreakIteratorBuilder createInstance(ULocale where) {
        return new Builder(where);
    }

    @Deprecated
    public static FilteredBreakIteratorBuilder createInstance() {
        return new Builder();
    }

    @Deprecated
    protected FilteredBreakIteratorBuilder() {
    }
}
