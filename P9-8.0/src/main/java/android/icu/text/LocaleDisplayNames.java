package android.icu.text;

import android.icu.impl.ICUConfig;
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
    private static final Method FACTORY_DIALECTHANDLING;
    private static final Method FACTORY_DISPLAYCONTEXT;

    public enum DialectHandling {
        STANDARD_NAMES,
        DIALECT_NAMES
    }

    private static class LastResortLocaleDisplayNames extends LocaleDisplayNames {
        private DisplayContext[] contexts;
        private ULocale locale;

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
            boolean z = false;
            if (this == obj) {
                return true;
            }
            if (obj == null || ((obj instanceof UiListItem) ^ 1) != 0) {
                return false;
            }
            UiListItem other = (UiListItem) obj;
            if (this.nameInDisplayLocale.equals(other.nameInDisplayLocale) && this.nameInSelf.equals(other.nameInSelf) && this.minimized.equals(other.minimized)) {
                z = this.modified.equals(other.modified);
            }
            return z;
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

    static {
        Method factoryDialectHandling = null;
        Method factoryDisplayContext = null;
        try {
            Class<?> implClass = Class.forName(ICUConfig.get("android.icu.text.LocaleDisplayNames.impl", "android.icu.impl.LocaleDisplayNamesImpl"));
            try {
                factoryDialectHandling = implClass.getMethod("getInstance", new Class[]{ULocale.class, DialectHandling.class});
            } catch (NoSuchMethodException e) {
            }
            try {
                factoryDisplayContext = implClass.getMethod("getInstance", new Class[]{ULocale.class, DisplayContext[].class});
            } catch (NoSuchMethodException e2) {
            }
        } catch (ClassNotFoundException e3) {
        }
        FACTORY_DIALECTHANDLING = factoryDialectHandling;
        FACTORY_DISPLAYCONTEXT = factoryDisplayContext;
    }
}
