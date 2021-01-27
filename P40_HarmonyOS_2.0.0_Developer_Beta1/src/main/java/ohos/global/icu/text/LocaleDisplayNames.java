package ohos.global.icu.text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import ohos.global.icu.impl.ICUConfig;
import ohos.global.icu.lang.UScript;
import ohos.global.icu.text.DisplayContext;
import ohos.global.icu.util.ULocale;

public abstract class LocaleDisplayNames {
    private static final Method FACTORY_DIALECTHANDLING;
    private static final Method FACTORY_DISPLAYCONTEXT;

    public enum DialectHandling {
        STANDARD_NAMES,
        DIALECT_NAMES
    }

    public abstract DisplayContext getContext(DisplayContext.Type type);

    public abstract DialectHandling getDialectHandling();

    public abstract ULocale getLocale();

    public abstract List<UiListItem> getUiListCompareWholeItems(Set<ULocale> set, Comparator<UiListItem> comparator);

    public abstract String keyDisplayName(String str);

    public abstract String keyValueDisplayName(String str, String str2);

    public abstract String languageDisplayName(String str);

    public abstract String localeDisplayName(String str);

    public abstract String localeDisplayName(Locale locale);

    public abstract String localeDisplayName(ULocale uLocale);

    public abstract String regionDisplayName(String str);

    public abstract String scriptDisplayName(int i);

    public abstract String scriptDisplayName(String str);

    public abstract String variantDisplayName(String str);

    public static LocaleDisplayNames getInstance(ULocale uLocale) {
        return getInstance(uLocale, DialectHandling.STANDARD_NAMES);
    }

    public static LocaleDisplayNames getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0018  */
    public static LocaleDisplayNames getInstance(ULocale uLocale, DialectHandling dialectHandling) {
        LocaleDisplayNames localeDisplayNames;
        Method method = FACTORY_DIALECTHANDLING;
        if (method != null) {
            try {
                localeDisplayNames = (LocaleDisplayNames) method.invoke(null, uLocale, dialectHandling);
            } catch (IllegalAccessException | InvocationTargetException unused) {
            }
            return localeDisplayNames != null ? new LastResortLocaleDisplayNames(uLocale, dialectHandling) : localeDisplayNames;
        }
        localeDisplayNames = null;
        if (localeDisplayNames != null) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0018  */
    public static LocaleDisplayNames getInstance(ULocale uLocale, DisplayContext... displayContextArr) {
        LocaleDisplayNames localeDisplayNames;
        Method method = FACTORY_DISPLAYCONTEXT;
        if (method != null) {
            try {
                localeDisplayNames = (LocaleDisplayNames) method.invoke(null, uLocale, displayContextArr);
            } catch (IllegalAccessException | InvocationTargetException unused) {
            }
            return localeDisplayNames != null ? new LastResortLocaleDisplayNames(uLocale, displayContextArr) : localeDisplayNames;
        }
        localeDisplayNames = null;
        if (localeDisplayNames != null) {
        }
    }

    public static LocaleDisplayNames getInstance(Locale locale, DisplayContext... displayContextArr) {
        return getInstance(ULocale.forLocale(locale), displayContextArr);
    }

    @Deprecated
    public String scriptDisplayNameInContext(String str) {
        return scriptDisplayName(str);
    }

    public List<UiListItem> getUiList(Set<ULocale> set, boolean z, Comparator<Object> comparator) {
        return getUiListCompareWholeItems(set, UiListItem.getComparator(comparator, z));
    }

    public static class UiListItem {
        public final ULocale minimized;
        public final ULocale modified;
        public final String nameInDisplayLocale;
        public final String nameInSelf;

        public UiListItem(ULocale uLocale, ULocale uLocale2, String str, String str2) {
            this.minimized = uLocale;
            this.modified = uLocale2;
            this.nameInDisplayLocale = str;
            this.nameInSelf = str2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof UiListItem)) {
                return false;
            }
            UiListItem uiListItem = (UiListItem) obj;
            return this.nameInDisplayLocale.equals(uiListItem.nameInDisplayLocale) && this.nameInSelf.equals(uiListItem.nameInSelf) && this.minimized.equals(uiListItem.minimized) && this.modified.equals(uiListItem.modified);
        }

        public int hashCode() {
            return this.nameInDisplayLocale.hashCode() ^ this.modified.hashCode();
        }

        public String toString() {
            return "{" + this.minimized + ", " + this.modified + ", " + this.nameInDisplayLocale + ", " + this.nameInSelf + "}";
        }

        public static Comparator<UiListItem> getComparator(Comparator<Object> comparator, boolean z) {
            return new UiListItemComparator(comparator, z);
        }

        /* access modifiers changed from: private */
        public static class UiListItemComparator implements Comparator<UiListItem> {
            private final Comparator<Object> collator;
            private final boolean useSelf;

            UiListItemComparator(Comparator<Object> comparator, boolean z) {
                this.collator = comparator;
                this.useSelf = z;
            }

            public int compare(UiListItem uiListItem, UiListItem uiListItem2) {
                int i;
                if (this.useSelf) {
                    i = this.collator.compare(uiListItem.nameInSelf, uiListItem2.nameInSelf);
                } else {
                    i = this.collator.compare(uiListItem.nameInDisplayLocale, uiListItem2.nameInDisplayLocale);
                }
                return i != 0 ? i : uiListItem.modified.compareTo(uiListItem2.modified);
            }
        }
    }

    @Deprecated
    protected LocaleDisplayNames() {
    }

    static {
        Method method;
        Method method2 = null;
        try {
            Class<?> cls = Class.forName(ICUConfig.get("ohos.global.icu.text.LocaleDisplayNames.impl", "ohos.global.icu.impl.LocaleDisplayNamesImpl"));
            try {
                method = cls.getMethod("getInstance", ULocale.class, DialectHandling.class);
            } catch (NoSuchMethodException unused) {
                method = null;
            }
            try {
                method2 = cls.getMethod("getInstance", ULocale.class, DisplayContext[].class);
            } catch (ClassNotFoundException | NoSuchMethodException unused2) {
            }
        } catch (ClassNotFoundException unused3) {
            method = null;
        }
        FACTORY_DIALECTHANDLING = method;
        FACTORY_DISPLAYCONTEXT = method2;
    }

    /* access modifiers changed from: private */
    public static class LastResortLocaleDisplayNames extends LocaleDisplayNames {
        private DisplayContext[] contexts;
        private ULocale locale;

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String keyDisplayName(String str) {
            return str;
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String keyValueDisplayName(String str, String str2) {
            return str2;
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String languageDisplayName(String str) {
            return str;
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String regionDisplayName(String str) {
            return str;
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String scriptDisplayName(String str) {
            return str;
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String variantDisplayName(String str) {
            return str;
        }

        private LastResortLocaleDisplayNames(ULocale uLocale, DialectHandling dialectHandling) {
            this.locale = uLocale;
            this.contexts = new DisplayContext[]{dialectHandling == DialectHandling.DIALECT_NAMES ? DisplayContext.DIALECT_NAMES : DisplayContext.STANDARD_NAMES};
        }

        private LastResortLocaleDisplayNames(ULocale uLocale, DisplayContext... displayContextArr) {
            this.locale = uLocale;
            this.contexts = new DisplayContext[displayContextArr.length];
            System.arraycopy(displayContextArr, 0, this.contexts, 0, displayContextArr.length);
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public ULocale getLocale() {
            return this.locale;
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public DialectHandling getDialectHandling() {
            DialectHandling dialectHandling = DialectHandling.STANDARD_NAMES;
            DisplayContext[] displayContextArr = this.contexts;
            for (DisplayContext displayContext : displayContextArr) {
                if (displayContext.type() == DisplayContext.Type.DIALECT_HANDLING && displayContext.value() == DisplayContext.DIALECT_NAMES.ordinal()) {
                    return DialectHandling.DIALECT_NAMES;
                }
            }
            return dialectHandling;
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public DisplayContext getContext(DisplayContext.Type type) {
            DisplayContext displayContext = DisplayContext.STANDARD_NAMES;
            DisplayContext[] displayContextArr = this.contexts;
            for (DisplayContext displayContext2 : displayContextArr) {
                if (displayContext2.type() == type) {
                    return displayContext2;
                }
            }
            return displayContext;
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String localeDisplayName(ULocale uLocale) {
            return uLocale.getName();
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String localeDisplayName(Locale locale2) {
            return ULocale.forLocale(locale2).getName();
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String localeDisplayName(String str) {
            return new ULocale(str).getName();
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public String scriptDisplayName(int i) {
            return UScript.getShortName(i);
        }

        @Override // ohos.global.icu.text.LocaleDisplayNames
        public List<UiListItem> getUiListCompareWholeItems(Set<ULocale> set, Comparator<UiListItem> comparator) {
            return Collections.emptyList();
        }
    }
}
