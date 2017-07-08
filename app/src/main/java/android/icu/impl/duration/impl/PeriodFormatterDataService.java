package android.icu.impl.duration.impl;

import java.util.Collection;

public abstract class PeriodFormatterDataService {
    public abstract PeriodFormatterData get(String str);

    public abstract Collection<String> getAvailableLocales();
}
