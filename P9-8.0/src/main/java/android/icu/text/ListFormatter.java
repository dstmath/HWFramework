package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

public final class ListFormatter {
    static Cache cache = new Cache();
    private final String end;
    private final ULocale locale;
    private final String middle;
    private final String start;
    private final String two;

    private static class Cache {
        private final ICUCache<String, ListFormatter> cache;

        /* synthetic */ Cache(Cache -this0) {
            this();
        }

        private Cache() {
            this.cache = new SimpleCache();
        }

        public ListFormatter get(ULocale locale, String style) {
            String key = String.format("%s:%s", new Object[]{locale.toString(), style});
            ListFormatter result = (ListFormatter) this.cache.get(key);
            if (result != null) {
                return result;
            }
            result = load(locale, style);
            this.cache.put(key, result);
            return result;
        }

        private static ListFormatter load(ULocale ulocale, String style) {
            ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ulocale);
            StringBuilder sb = new StringBuilder();
            return new ListFormatter(ListFormatter.compilePattern(r.getWithFallback("listPattern/" + style + "/2").getString(), sb), ListFormatter.compilePattern(r.getWithFallback("listPattern/" + style + "/start").getString(), sb), ListFormatter.compilePattern(r.getWithFallback("listPattern/" + style + "/middle").getString(), sb), ListFormatter.compilePattern(r.getWithFallback("listPattern/" + style + "/end").getString(), sb), ulocale, null);
        }
    }

    static class FormattedListBuilder {
        private StringBuilder current;
        private int offset;

        public FormattedListBuilder(Object start, boolean recordOffset) {
            this.current = new StringBuilder(start.toString());
            this.offset = recordOffset ? 0 : -1;
        }

        public FormattedListBuilder append(String pattern, Object next, boolean recordOffset) {
            int[] offsets = (recordOffset || offsetRecorded()) ? new int[2] : null;
            SimpleFormatterImpl.formatAndReplace(pattern, this.current, offsets, this.current, next.toString());
            if (offsets != null) {
                if (offsets[0] == -1 || offsets[1] == -1) {
                    throw new IllegalArgumentException("{0} or {1} missing from pattern " + pattern);
                } else if (recordOffset) {
                    this.offset = offsets[1];
                } else {
                    this.offset += offsets[0];
                }
            }
            return this;
        }

        public String toString() {
            return this.current.toString();
        }

        public int getOffset() {
            return this.offset;
        }

        private boolean offsetRecorded() {
            return this.offset >= 0;
        }
    }

    @Deprecated
    public enum Style {
        STANDARD("standard"),
        DURATION("unit"),
        DURATION_SHORT("unit-short"),
        DURATION_NARROW("unit-narrow");
        
        private final String name;

        private Style(String name) {
            this.name = name;
        }

        @Deprecated
        public String getName() {
            return this.name;
        }
    }

    /* synthetic */ ListFormatter(String two, String start, String middle, String end, ULocale locale, ListFormatter -this5) {
        this(two, start, middle, end, locale);
    }

    @Deprecated
    public ListFormatter(String two, String start, String middle, String end) {
        String compilePattern = compilePattern(two, new StringBuilder());
        String compilePattern2 = compilePattern(start, new StringBuilder());
        String compilePattern3 = compilePattern(middle, new StringBuilder());
        String compilePattern4 = compilePattern(end, new StringBuilder());
        this(compilePattern, compilePattern2, compilePattern3, compilePattern4, null);
    }

    private ListFormatter(String two, String start, String middle, String end, ULocale locale) {
        this.two = two;
        this.start = start;
        this.middle = middle;
        this.end = end;
        this.locale = locale;
    }

    private static String compilePattern(String pattern, StringBuilder sb) {
        return SimpleFormatterImpl.compileToStringMinMaxArguments(pattern, sb, 2, 2);
    }

    public static ListFormatter getInstance(ULocale locale) {
        return getInstance(locale, Style.STANDARD);
    }

    public static ListFormatter getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale), Style.STANDARD);
    }

    @Deprecated
    public static ListFormatter getInstance(ULocale locale, Style style) {
        return cache.get(locale, style.getName());
    }

    public static ListFormatter getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT));
    }

    public String format(Object... items) {
        return format(Arrays.asList(items));
    }

    public String format(Collection<?> items) {
        return format(items, -1).toString();
    }

    FormattedListBuilder format(Collection<?> items, int index) {
        boolean z = true;
        Iterator<?> it = items.iterator();
        int count = items.size();
        Object next;
        String str;
        Object next2;
        switch (count) {
            case 0:
                return new FormattedListBuilder("", false);
            case 1:
                next = it.next();
                if (index != 0) {
                    z = false;
                }
                return new FormattedListBuilder(next, z);
            case 2:
                FormattedListBuilder formattedListBuilder = new FormattedListBuilder(it.next(), index == 0);
                str = this.two;
                next2 = it.next();
                if (index != 1) {
                    z = false;
                }
                return formattedListBuilder.append(str, next2, z);
            default:
                boolean z2;
                next = it.next();
                if (index == 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                FormattedListBuilder builder = new FormattedListBuilder(next, z2);
                String str2 = this.start;
                next2 = it.next();
                if (index == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                builder.append(str2, next2, z2);
                for (int idx = 2; idx < count - 1; idx++) {
                    str2 = this.middle;
                    next2 = it.next();
                    if (index == idx) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    builder.append(str2, next2, z2);
                }
                str = this.end;
                next = it.next();
                if (index != count - 1) {
                    z = false;
                }
                return builder.append(str, next, z);
        }
    }

    public String getPatternForNumItems(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        Collection list = new ArrayList();
        for (int i = 0; i < count; i++) {
            list.add(String.format("{%d}", new Object[]{Integer.valueOf(i)}));
        }
        return format(list);
    }

    @Deprecated
    public ULocale getLocale() {
        return this.locale;
    }
}
