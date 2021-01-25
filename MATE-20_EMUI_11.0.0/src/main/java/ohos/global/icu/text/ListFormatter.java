package ohos.global.icu.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import ohos.global.icu.impl.ICUCache;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.SimpleCache;
import ohos.global.icu.impl.SimpleFormatterImpl;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public final class ListFormatter {
    static Cache cache = new Cache();
    private final String end;
    private final ULocale locale;
    private final String middle;
    private final String start;
    private final String two;

    @Deprecated
    public enum Style {
        STANDARD("standard"),
        OR("or"),
        UNIT("unit"),
        UNIT_SHORT("unit-short"),
        UNIT_NARROW("unit-narrow");
        
        private final String name;

        private Style(String str) {
            this.name = str;
        }

        @Deprecated
        public String getName() {
            return this.name;
        }
    }

    @Deprecated
    public ListFormatter(String str, String str2, String str3, String str4) {
        this(compilePattern(str, new StringBuilder()), compilePattern(str2, new StringBuilder()), compilePattern(str3, new StringBuilder()), compilePattern(str4, new StringBuilder()), null);
    }

    private ListFormatter(String str, String str2, String str3, String str4, ULocale uLocale) {
        this.two = str;
        this.start = str2;
        this.middle = str3;
        this.end = str4;
        this.locale = uLocale;
    }

    /* access modifiers changed from: private */
    public static String compilePattern(String str, StringBuilder sb) {
        return SimpleFormatterImpl.compileToStringMinMaxArguments(str, sb, 2, 2);
    }

    public static ListFormatter getInstance(ULocale uLocale) {
        return getInstance(uLocale, Style.STANDARD);
    }

    public static ListFormatter getInstance(Locale locale2) {
        return getInstance(ULocale.forLocale(locale2), Style.STANDARD);
    }

    @Deprecated
    public static ListFormatter getInstance(ULocale uLocale, Style style) {
        return cache.get(uLocale, style.getName());
    }

    public static ListFormatter getInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public String format(Object... objArr) {
        return format(Arrays.asList(objArr));
    }

    public String format(Collection<?> collection) {
        return format(collection, -1).toString();
    }

    /* access modifiers changed from: package-private */
    public FormattedListBuilder format(Collection<?> collection, int i) {
        int i2;
        Iterator<?> it = collection.iterator();
        int size = collection.size();
        boolean z = false;
        if (size == 0) {
            return new FormattedListBuilder("", false);
        }
        if (size != 1) {
            int i3 = 2;
            if (size != 2) {
                FormattedListBuilder formattedListBuilder = new FormattedListBuilder(it.next(), i == 0);
                formattedListBuilder.append(this.start, it.next(), i == 1);
                while (true) {
                    i2 = size - 1;
                    if (i3 >= i2) {
                        break;
                    }
                    formattedListBuilder.append(this.middle, it.next(), i == i3);
                    i3++;
                }
                String str = this.end;
                Object next = it.next();
                if (i == i2) {
                    z = true;
                }
                return formattedListBuilder.append(str, next, z);
            }
            FormattedListBuilder formattedListBuilder2 = new FormattedListBuilder(it.next(), i == 0);
            String str2 = this.two;
            Object next2 = it.next();
            if (i == 1) {
                z = true;
            }
            return formattedListBuilder2.append(str2, next2, z);
        }
        Object next3 = it.next();
        if (i == 0) {
            z = true;
        }
        return new FormattedListBuilder(next3, z);
    }

    public String getPatternForNumItems(int i) {
        if (i > 0) {
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < i; i2++) {
                arrayList.add(String.format("{%d}", Integer.valueOf(i2)));
            }
            return format(arrayList);
        }
        throw new IllegalArgumentException("count must be > 0");
    }

    @Deprecated
    public ULocale getLocale() {
        return this.locale;
    }

    /* access modifiers changed from: package-private */
    public static class FormattedListBuilder {
        private StringBuilder current;
        private int offset;

        public FormattedListBuilder(Object obj, boolean z) {
            this.current = new StringBuilder(obj.toString());
            this.offset = z ? 0 : -1;
        }

        public FormattedListBuilder append(String str, Object obj, boolean z) {
            int[] iArr = (z || offsetRecorded()) ? new int[2] : null;
            StringBuilder sb = this.current;
            SimpleFormatterImpl.formatAndReplace(str, sb, iArr, sb, obj.toString());
            if (iArr != null) {
                if (iArr[0] == -1 || iArr[1] == -1) {
                    throw new IllegalArgumentException("{0} or {1} missing from pattern " + str);
                } else if (z) {
                    this.offset = iArr[1];
                } else {
                    this.offset += iArr[0];
                }
            }
            return this;
        }

        public void appendTo(Appendable appendable) {
            try {
                appendable.append(this.current);
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
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

    /* access modifiers changed from: private */
    public static class Cache {
        private final ICUCache<String, ListFormatter> cache;

        private Cache() {
            this.cache = new SimpleCache();
        }

        public ListFormatter get(ULocale uLocale, String str) {
            String format = String.format("%s:%s", uLocale.toString(), str);
            ListFormatter listFormatter = this.cache.get(format);
            if (listFormatter != null) {
                return listFormatter;
            }
            ListFormatter load = load(uLocale, str);
            this.cache.put(format, load);
            return load;
        }

        private static ListFormatter load(ULocale uLocale, String str) {
            ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
            StringBuilder sb = new StringBuilder();
            String compilePattern = ListFormatter.compilePattern(bundleInstance.getWithFallback("listPattern/" + str + "/2").getString(), sb);
            String compilePattern2 = ListFormatter.compilePattern(bundleInstance.getWithFallback("listPattern/" + str + "/start").getString(), sb);
            String compilePattern3 = ListFormatter.compilePattern(bundleInstance.getWithFallback("listPattern/" + str + "/middle").getString(), sb);
            return new ListFormatter(compilePattern, compilePattern2, compilePattern3, ListFormatter.compilePattern(bundleInstance.getWithFallback("listPattern/" + str + "/end").getString(), sb), uLocale);
        }
    }
}
