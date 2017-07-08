package android.icu.text;

import android.icu.lang.UScript;
import android.icu.text.DisplayContext.Type;
import android.icu.util.ULocale;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class LocaleDisplayNames {
    private static final Method FACTORY_DIALECTHANDLING = null;
    private static final Method FACTORY_DISPLAYCONTEXT = null;

    public enum DialectHandling {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.LocaleDisplayNames.DialectHandling.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.LocaleDisplayNames.DialectHandling.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.LocaleDisplayNames.DialectHandling.<clinit>():void");
        }
    }

    private static class LastResortLocaleDisplayNames extends LocaleDisplayNames {
        private DisplayContext[] contexts;
        private ULocale locale;

        /* synthetic */ LastResortLocaleDisplayNames(ULocale locale, DialectHandling dialectHandling, LastResortLocaleDisplayNames lastResortLocaleDisplayNames) {
            this(locale, dialectHandling);
        }

        /* synthetic */ LastResortLocaleDisplayNames(ULocale locale, DisplayContext[] contexts, LastResortLocaleDisplayNames lastResortLocaleDisplayNames) {
            this(locale, contexts);
        }

        private LastResortLocaleDisplayNames(ULocale locale, DialectHandling dialectHandling) {
            this.locale = locale;
            DisplayContext context = dialectHandling == DialectHandling.DIALECT_NAMES ? DisplayContext.DIALECT_NAMES : DisplayContext.STANDARD_NAMES;
            this.contexts = new DisplayContext[]{context};
        }

        private LastResortLocaleDisplayNames(ULocale locale, DisplayContext... contexts) {
            this.locale = locale;
            this.contexts = new DisplayContext[contexts.length];
            System.arraycopy(contexts, 0, this.contexts, 0, contexts.length);
        }

        public ULocale getLocale() {
            return this.locale;
        }

        public DialectHandling getDialectHandling() {
            DialectHandling result = DialectHandling.STANDARD_NAMES;
            for (DisplayContext context : this.contexts) {
                if (context.type() == Type.DIALECT_HANDLING && context.value() == DisplayContext.DIALECT_NAMES.ordinal()) {
                    return DialectHandling.DIALECT_NAMES;
                }
            }
            return result;
        }

        public DisplayContext getContext(Type type) {
            DisplayContext result = DisplayContext.STANDARD_NAMES;
            for (DisplayContext context : this.contexts) {
                if (context.type() == type) {
                    return context;
                }
            }
            return result;
        }

        public String localeDisplayName(ULocale locale) {
            return locale.getName();
        }

        public String localeDisplayName(Locale locale) {
            return ULocale.forLocale(locale).getName();
        }

        public String localeDisplayName(String localeId) {
            return new ULocale(localeId).getName();
        }

        public String languageDisplayName(String lang) {
            return lang;
        }

        public String scriptDisplayName(String script) {
            return script;
        }

        public String scriptDisplayName(int scriptCode) {
            return UScript.getShortName(scriptCode);
        }

        public String regionDisplayName(String region) {
            return region;
        }

        public String variantDisplayName(String variant) {
            return variant;
        }

        public String keyDisplayName(String key) {
            return key;
        }

        public String keyValueDisplayName(String key, String value) {
            return value;
        }

        public List<UiListItem> getUiListCompareWholeItems(Set<ULocale> set, Comparator<UiListItem> comparator) {
            return Collections.emptyList();
        }
    }

    public static class UiListItem {
        public final ULocale minimized;
        public final ULocale modified;
        public final String nameInDisplayLocale;
        public final String nameInSelf;

        private static class UiListItemComparator implements Comparator<UiListItem> {
            private final Comparator<Object> collator;
            private final boolean useSelf;

            UiListItemComparator(Comparator<Object> collator, boolean useSelf) {
                this.collator = collator;
                this.useSelf = useSelf;
            }

            public /* bridge */ /* synthetic */ int compare(Object o1, Object o2) {
                return compare((UiListItem) o1, (UiListItem) o2);
            }

            public int compare(UiListItem o1, UiListItem o2) {
                int result;
                if (this.useSelf) {
                    result = this.collator.compare(o1.nameInSelf, o2.nameInSelf);
                } else {
                    result = this.collator.compare(o1.nameInDisplayLocale, o2.nameInDisplayLocale);
                }
                return result != 0 ? result : o1.modified.compareTo(o2.modified);
            }
        }

        public UiListItem(ULocale minimized, ULocale modified, String nameInDisplayLocale, String nameInSelf) {
            this.minimized = minimized;
            this.modified = modified;
            this.nameInDisplayLocale = nameInDisplayLocale;
            this.nameInSelf = nameInSelf;
        }

        public boolean equals(Object obj) {
            UiListItem other = (UiListItem) obj;
            if (this.nameInDisplayLocale.equals(other.nameInDisplayLocale) && this.nameInSelf.equals(other.nameInSelf) && this.minimized.equals(other.minimized)) {
                return this.modified.equals(other.modified);
            }
            return false;
        }

        public int hashCode() {
            return this.modified.hashCode() ^ this.nameInDisplayLocale.hashCode();
        }

        public String toString() {
            return "{" + this.minimized + ", " + this.modified + ", " + this.nameInDisplayLocale + ", " + this.nameInSelf + "}";
        }

        public static Comparator<UiListItem> getComparator(Comparator<Object> comparator, boolean inSelf) {
            return new UiListItemComparator(comparator, inSelf);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.LocaleDisplayNames.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.LocaleDisplayNames.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.LocaleDisplayNames.<clinit>():void");
    }

    public abstract DisplayContext getContext(Type type);

    public abstract DialectHandling getDialectHandling();

    public abstract ULocale getLocale();

    public abstract List<UiListItem> getUiListCompareWholeItems(Set<ULocale> set, Comparator<UiListItem> comparator);

    public abstract String keyDisplayName(String str);

    public abstract String keyValueDisplayName(String str, String str2);

    public abstract String languageDisplayName(String str);

    public abstract String localeDisplayName(ULocale uLocale);

    public abstract String localeDisplayName(String str);

    public abstract String localeDisplayName(Locale locale);

    public abstract String regionDisplayName(String str);

    public abstract String scriptDisplayName(int i);

    public abstract String scriptDisplayName(String str);

    public abstract String variantDisplayName(String str);

    public static LocaleDisplayNames getInstance(ULocale locale) {
        return getInstance(locale, DialectHandling.STANDARD_NAMES);
    }

    public static LocaleDisplayNames getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    public static LocaleDisplayNames getInstance(ULocale locale, DialectHandling dialectHandling) {
        LocaleDisplayNames result = null;
        if (FACTORY_DIALECTHANDLING != null) {
            try {
                result = (LocaleDisplayNames) FACTORY_DIALECTHANDLING.invoke(null, new Object[]{locale, dialectHandling});
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e2) {
            }
        }
        if (result == null) {
            return new LastResortLocaleDisplayNames(locale, dialectHandling, null);
        }
        return result;
    }

    public static LocaleDisplayNames getInstance(ULocale locale, DisplayContext... contexts) {
        LocaleDisplayNames result = null;
        if (FACTORY_DISPLAYCONTEXT != null) {
            try {
                result = (LocaleDisplayNames) FACTORY_DISPLAYCONTEXT.invoke(null, new Object[]{locale, contexts});
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e2) {
            }
        }
        if (result == null) {
            return new LastResortLocaleDisplayNames(locale, contexts, null);
        }
        return result;
    }

    public static LocaleDisplayNames getInstance(Locale locale, DisplayContext... contexts) {
        return getInstance(ULocale.forLocale(locale), contexts);
    }

    @Deprecated
    public String scriptDisplayNameInContext(String script) {
        return scriptDisplayName(script);
    }

    public List<UiListItem> getUiList(Set<ULocale> localeSet, boolean inSelf, Comparator<Object> collator) {
        return getUiListCompareWholeItems(localeSet, UiListItem.getComparator(collator, inSelf));
    }

    @Deprecated
    protected LocaleDisplayNames() {
    }
}
