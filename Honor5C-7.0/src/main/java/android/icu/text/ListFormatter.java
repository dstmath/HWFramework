package android.icu.text;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimplePatternFormatter;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class ListFormatter {
    static Cache cache;
    private final String end;
    private final ULocale locale;
    private final String middle;
    private final String start;
    private final String two;

    private static class Cache {
        private final ICUCache<String, ListFormatter> cache;

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
            ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, ulocale);
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
            int[] iArr = (recordOffset || offsetRecorded()) ? new int[2] : null;
            SimplePatternFormatter.formatAndReplace(pattern, this.current, iArr, this.current, next.toString());
            if (iArr != null) {
                if (iArr[0] == -1 || iArr[1] == -1) {
                    throw new IllegalArgumentException("{0} or {1} missing from pattern " + pattern);
                } else if (recordOffset) {
                    this.offset = iArr[1];
                } else {
                    this.offset += iArr[0];
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
        ;
        
        private final String name;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.ListFormatter.Style.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.ListFormatter.Style.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.ListFormatter.Style.<clinit>():void");
        }

        private Style(String name) {
            this.name = name;
        }

        @Deprecated
        public String getName() {
            return this.name;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.ListFormatter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.ListFormatter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.ListFormatter.<clinit>():void");
    }

    /* synthetic */ ListFormatter(String two, String start, String middle, String end, ULocale locale, ListFormatter listFormatter) {
        this(two, start, middle, end, locale);
    }

    @Deprecated
    public ListFormatter(String two, String start, String middle, String end) {
        this(compilePattern(two, new StringBuilder()), compilePattern(start, new StringBuilder()), compilePattern(middle, new StringBuilder()), compilePattern(end, new StringBuilder()), null);
    }

    private ListFormatter(String two, String start, String middle, String end, ULocale locale) {
        this.two = two;
        this.start = start;
        this.middle = middle;
        this.end = end;
        this.locale = locale;
    }

    private static String compilePattern(String pattern, StringBuilder sb) {
        return SimplePatternFormatter.compileToStringMinMaxPlaceholders(pattern, sb, 2, 2);
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
            case XmlPullParser.START_DOCUMENT /*0*/:
                return new FormattedListBuilder(XmlPullParser.NO_NAMESPACE, false);
            case NodeFilter.SHOW_ELEMENT /*1*/:
                next = it.next();
                if (index != 0) {
                    z = false;
                }
                return new FormattedListBuilder(next, z);
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
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
