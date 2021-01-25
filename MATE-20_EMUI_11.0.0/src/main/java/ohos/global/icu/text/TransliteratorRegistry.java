package ohos.global.icu.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.LocaleUtility;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.lang.UScript;
import ohos.global.icu.text.RuleBasedTransliterator;
import ohos.global.icu.text.Transliterator;
import ohos.global.icu.util.CaseInsensitiveString;
import ohos.global.icu.util.UResourceBundle;

/* access modifiers changed from: package-private */
public class TransliteratorRegistry {
    private static final String ANY = "Any";
    private static final boolean DEBUG = false;
    private static final char LOCALE_SEP = '_';
    private static final String NO_VARIANT = "";
    private List<CaseInsensitiveString> availableIDs = new ArrayList();
    private Map<CaseInsensitiveString, Object[]> registry = Collections.synchronizedMap(new HashMap());
    private Map<CaseInsensitiveString, Map<CaseInsensitiveString, List<CaseInsensitiveString>>> specDAG = Collections.synchronizedMap(new HashMap());

    /* access modifiers changed from: package-private */
    public static class Spec {
        private boolean isNextLocale;
        private boolean isSpecLocale;
        private String nextSpec;
        private ICUResourceBundle res;
        private String scriptName = null;
        private String spec = null;
        private String top;

        public Spec(String str) {
            this.top = str;
            try {
                int codeFromName = UScript.getCodeFromName(this.top);
                int[] code = UScript.getCode(this.top);
                if (code != null) {
                    this.scriptName = UScript.getName(code[0]);
                    if (this.scriptName.equalsIgnoreCase(this.top)) {
                        this.scriptName = null;
                    }
                }
                this.isSpecLocale = false;
                this.res = null;
                if (codeFromName == -1) {
                    this.res = UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME, LocaleUtility.getLocaleFromName(this.top));
                    if (this.res != null && LocaleUtility.isFallbackOf(this.res.getULocale().toString(), this.top)) {
                        this.isSpecLocale = true;
                    }
                }
            } catch (MissingResourceException unused) {
                this.scriptName = null;
            }
            reset();
        }

        public boolean hasFallback() {
            return this.nextSpec != null;
        }

        public void reset() {
            if (!Utility.sameObjects(this.spec, this.top)) {
                this.spec = this.top;
                this.isSpecLocale = this.res != null;
                setupNext();
            }
        }

        private void setupNext() {
            this.isNextLocale = false;
            if (this.isSpecLocale) {
                this.nextSpec = this.spec;
                int lastIndexOf = this.nextSpec.lastIndexOf(95);
                if (lastIndexOf > 0) {
                    this.nextSpec = this.spec.substring(0, lastIndexOf);
                    this.isNextLocale = true;
                    return;
                }
                this.nextSpec = this.scriptName;
            } else if (!Utility.sameObjects(this.nextSpec, this.scriptName)) {
                this.nextSpec = this.scriptName;
            } else {
                this.nextSpec = null;
            }
        }

        public String next() {
            this.spec = this.nextSpec;
            this.isSpecLocale = this.isNextLocale;
            setupNext();
            return this.spec;
        }

        public String get() {
            return this.spec;
        }

        public boolean isLocale() {
            return this.isSpecLocale;
        }

        /* JADX WARN: Type inference failed for: r2v3, types: [java.util.ResourceBundle, ohos.global.icu.impl.ICUResourceBundle] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public ResourceBundle getBundle() {
            ICUResourceBundle iCUResourceBundle = this.res;
            if (iCUResourceBundle == null || !iCUResourceBundle.getULocale().toString().equals(this.spec)) {
                return null;
            }
            return this.res;
        }

        public String getTop() {
            return this.top;
        }
    }

    /* access modifiers changed from: package-private */
    public static class ResourceEntry {
        public int direction;
        public String resource;

        public ResourceEntry(String str, int i) {
            this.resource = str;
            this.direction = i;
        }
    }

    /* access modifiers changed from: package-private */
    public static class LocaleEntry {
        public int direction;
        public String rule;

        public LocaleEntry(String str, int i) {
            this.rule = str;
            this.direction = i;
        }
    }

    /* access modifiers changed from: package-private */
    public static class AliasEntry {
        public String alias;

        public AliasEntry(String str) {
            this.alias = str;
        }
    }

    /* access modifiers changed from: package-private */
    public static class CompoundRBTEntry {
        private String ID;
        private UnicodeSet compoundFilter;
        private List<RuleBasedTransliterator.Data> dataVector;
        private List<String> idBlockVector;

        public CompoundRBTEntry(String str, List<String> list, List<RuleBasedTransliterator.Data> list2, UnicodeSet unicodeSet) {
            this.ID = str;
            this.idBlockVector = list;
            this.dataVector = list2;
            this.compoundFilter = unicodeSet;
        }

        public Transliterator getInstance() {
            ArrayList arrayList = new ArrayList();
            int max = Math.max(this.idBlockVector.size(), this.dataVector.size());
            int i = 1;
            for (int i2 = 0; i2 < max; i2++) {
                if (i2 < this.idBlockVector.size()) {
                    String str = this.idBlockVector.get(i2);
                    if (str.length() > 0) {
                        arrayList.add(Transliterator.getInstance(str));
                    }
                }
                if (i2 < this.dataVector.size()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("%Pass");
                    sb.append(i);
                    arrayList.add(new RuleBasedTransliterator(sb.toString(), this.dataVector.get(i2), null));
                    i++;
                }
            }
            CompoundTransliterator compoundTransliterator = new CompoundTransliterator(arrayList, i - 1);
            compoundTransliterator.setID(this.ID);
            UnicodeSet unicodeSet = this.compoundFilter;
            if (unicodeSet != null) {
                compoundTransliterator.setFilter(unicodeSet);
            }
            return compoundTransliterator;
        }
    }

    public Transliterator get(String str, StringBuffer stringBuffer) {
        Object[] find = find(str);
        if (find == null) {
            return null;
        }
        return instantiateEntry(str, find, stringBuffer);
    }

    public void put(String str, Class<? extends Transliterator> cls, boolean z) {
        registerEntry(str, cls, z);
    }

    public void put(String str, Transliterator.Factory factory, boolean z) {
        registerEntry(str, factory, z);
    }

    public void put(String str, String str2, int i, boolean z) {
        registerEntry(str, new ResourceEntry(str2, i), z);
    }

    public void put(String str, String str2, boolean z) {
        registerEntry(str, new AliasEntry(str2), z);
    }

    public void put(String str, Transliterator transliterator, boolean z) {
        registerEntry(str, transliterator, z);
    }

    public void remove(String str) {
        String[] IDtoSTV = TransliteratorIDParser.IDtoSTV(str);
        String STVtoID = TransliteratorIDParser.STVtoID(IDtoSTV[0], IDtoSTV[1], IDtoSTV[2]);
        this.registry.remove(new CaseInsensitiveString(STVtoID));
        removeSTV(IDtoSTV[0], IDtoSTV[1], IDtoSTV[2]);
        this.availableIDs.remove(new CaseInsensitiveString(STVtoID));
    }

    /* access modifiers changed from: private */
    public static class IDEnumeration implements Enumeration<String> {
        Enumeration<CaseInsensitiveString> en;

        public IDEnumeration(Enumeration<CaseInsensitiveString> enumeration) {
            this.en = enumeration;
        }

        @Override // java.util.Enumeration
        public boolean hasMoreElements() {
            Enumeration<CaseInsensitiveString> enumeration = this.en;
            return enumeration != null && enumeration.hasMoreElements();
        }

        @Override // java.util.Enumeration
        public String nextElement() {
            return this.en.nextElement().getString();
        }
    }

    public Enumeration<String> getAvailableIDs() {
        return new IDEnumeration(Collections.enumeration(this.availableIDs));
    }

    public Enumeration<String> getAvailableSources() {
        return new IDEnumeration(Collections.enumeration(this.specDAG.keySet()));
    }

    public Enumeration<String> getAvailableTargets(String str) {
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> map = this.specDAG.get(new CaseInsensitiveString(str));
        if (map == null) {
            return new IDEnumeration(null);
        }
        return new IDEnumeration(Collections.enumeration(map.keySet()));
    }

    public Enumeration<String> getAvailableVariants(String str, String str2) {
        CaseInsensitiveString caseInsensitiveString = new CaseInsensitiveString(str);
        CaseInsensitiveString caseInsensitiveString2 = new CaseInsensitiveString(str2);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> map = this.specDAG.get(caseInsensitiveString);
        if (map == null) {
            return new IDEnumeration(null);
        }
        List<CaseInsensitiveString> list = map.get(caseInsensitiveString2);
        if (list == null) {
            return new IDEnumeration(null);
        }
        return new IDEnumeration(Collections.enumeration(list));
    }

    private void registerEntry(String str, String str2, String str3, Object obj, boolean z) {
        registerEntry(TransliteratorIDParser.STVtoID(str, str2, str3), str.length() == 0 ? ANY : str, str2, str3, obj, z);
    }

    private void registerEntry(String str, Object obj, boolean z) {
        String[] IDtoSTV = TransliteratorIDParser.IDtoSTV(str);
        registerEntry(TransliteratorIDParser.STVtoID(IDtoSTV[0], IDtoSTV[1], IDtoSTV[2]), IDtoSTV[0], IDtoSTV[1], IDtoSTV[2], obj, z);
    }

    private void registerEntry(String str, String str2, String str3, String str4, Object obj, boolean z) {
        CaseInsensitiveString caseInsensitiveString = new CaseInsensitiveString(str);
        this.registry.put(caseInsensitiveString, obj instanceof Object[] ? (Object[]) obj : new Object[]{obj});
        if (z) {
            registerSTV(str2, str3, str4);
            if (!this.availableIDs.contains(caseInsensitiveString)) {
                this.availableIDs.add(caseInsensitiveString);
                return;
            }
            return;
        }
        removeSTV(str2, str3, str4);
        this.availableIDs.remove(caseInsensitiveString);
    }

    private void registerSTV(String str, String str2, String str3) {
        CaseInsensitiveString caseInsensitiveString = new CaseInsensitiveString(str);
        CaseInsensitiveString caseInsensitiveString2 = new CaseInsensitiveString(str2);
        CaseInsensitiveString caseInsensitiveString3 = new CaseInsensitiveString(str3);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> map = this.specDAG.get(caseInsensitiveString);
        if (map == null) {
            map = Collections.synchronizedMap(new HashMap());
            this.specDAG.put(caseInsensitiveString, map);
        }
        List<CaseInsensitiveString> list = map.get(caseInsensitiveString2);
        if (list == null) {
            list = new ArrayList<>();
            map.put(caseInsensitiveString2, list);
        }
        if (list.contains(caseInsensitiveString3)) {
            return;
        }
        if (str3.length() > 0) {
            list.add(caseInsensitiveString3);
        } else {
            list.add(0, caseInsensitiveString3);
        }
    }

    private void removeSTV(String str, String str2, String str3) {
        List<CaseInsensitiveString> list;
        CaseInsensitiveString caseInsensitiveString = new CaseInsensitiveString(str);
        CaseInsensitiveString caseInsensitiveString2 = new CaseInsensitiveString(str2);
        CaseInsensitiveString caseInsensitiveString3 = new CaseInsensitiveString(str3);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> map = this.specDAG.get(caseInsensitiveString);
        if (map != null && (list = map.get(caseInsensitiveString2)) != null) {
            list.remove(caseInsensitiveString3);
            if (list.size() == 0) {
                map.remove(caseInsensitiveString2);
                if (map.size() == 0) {
                    this.specDAG.remove(caseInsensitiveString);
                }
            }
        }
    }

    private Object[] findInDynamicStore(Spec spec, Spec spec2, String str) {
        return this.registry.get(new CaseInsensitiveString(TransliteratorIDParser.STVtoID(spec.get(), spec2.get(), str)));
    }

    private Object[] findInStaticStore(Spec spec, Spec spec2, String str) {
        Object[] objArr;
        if (spec.isLocale()) {
            objArr = findInBundle(spec, spec2, str, 0);
        } else {
            objArr = spec2.isLocale() ? findInBundle(spec2, spec, str, 1) : null;
        }
        if (objArr != null) {
            registerEntry(spec.getTop(), spec2.getTop(), str, objArr, false);
        }
        return objArr;
    }

    private Object[] findInBundle(Spec spec, Spec spec2, String str, int i) {
        int i2;
        ResourceBundle bundle = spec.getBundle();
        if (bundle == null) {
            return null;
        }
        int i3 = 0;
        while (i3 < 2) {
            StringBuilder sb = new StringBuilder();
            if (i3 == 0) {
                sb.append(i == 0 ? "TransliterateTo" : "TransliterateFrom");
            } else {
                sb.append("Transliterate");
            }
            sb.append(spec2.get().toUpperCase(Locale.ENGLISH));
            try {
                String[] stringArray = bundle.getStringArray(sb.toString());
                if (str.length() != 0) {
                    i2 = 0;
                    while (true) {
                        if (i2 >= stringArray.length) {
                            break;
                        } else if (stringArray[i2].equalsIgnoreCase(str)) {
                            break;
                        } else {
                            i2 += 2;
                        }
                    }
                } else {
                    i2 = 0;
                }
                if (i2 < stringArray.length) {
                    return new Object[]{new LocaleEntry(stringArray[i2 + 1], i3 == 0 ? 0 : i)};
                }
                i3++;
            } catch (MissingResourceException unused) {
            }
        }
        return null;
    }

    private Object[] find(String str) {
        String[] IDtoSTV = TransliteratorIDParser.IDtoSTV(str);
        return find(IDtoSTV[0], IDtoSTV[1], IDtoSTV[2]);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003b, code lost:
        if (r2.hasFallback() != false) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003d, code lost:
        return null;
     */
    private Object[] find(String str, String str2, String str3) {
        Spec spec = new Spec(str);
        Spec spec2 = new Spec(str2);
        if (str3.length() != 0) {
            Object[] findInDynamicStore = findInDynamicStore(spec, spec2, str3);
            if (findInDynamicStore != null) {
                return findInDynamicStore;
            }
            Object[] findInStaticStore = findInStaticStore(spec, spec2, str3);
            if (findInStaticStore != null) {
                return findInStaticStore;
            }
        }
        while (true) {
            spec.reset();
            while (true) {
                Object[] findInDynamicStore2 = findInDynamicStore(spec, spec2, "");
                if (findInDynamicStore2 != null) {
                    return findInDynamicStore2;
                }
                Object[] findInStaticStore2 = findInStaticStore(spec, spec2, "");
                if (findInStaticStore2 != null) {
                    return findInStaticStore2;
                }
                if (!spec.hasFallback()) {
                    break;
                }
                spec.next();
            }
            spec2.next();
        }
    }

    private Transliterator instantiateEntry(String str, Object[] objArr, StringBuffer stringBuffer) {
        while (true) {
            Object obj = objArr[0];
            if (obj instanceof RuleBasedTransliterator.Data) {
                return new RuleBasedTransliterator(str, (RuleBasedTransliterator.Data) obj, null);
            }
            if (obj instanceof Class) {
                try {
                    return (Transliterator) ((Class) obj).newInstance();
                } catch (IllegalAccessException | InstantiationException unused) {
                    return null;
                }
            } else if (obj instanceof AliasEntry) {
                stringBuffer.append(((AliasEntry) obj).alias);
                return null;
            } else if (obj instanceof Transliterator.Factory) {
                return ((Transliterator.Factory) obj).getInstance(str);
            } else {
                if (obj instanceof CompoundRBTEntry) {
                    return ((CompoundRBTEntry) obj).getInstance();
                }
                if (obj instanceof AnyTransliterator) {
                    return ((AnyTransliterator) obj).safeClone();
                }
                if (obj instanceof RuleBasedTransliterator) {
                    return ((RuleBasedTransliterator) obj).safeClone();
                }
                if (obj instanceof CompoundTransliterator) {
                    return ((CompoundTransliterator) obj).safeClone();
                }
                if (obj instanceof Transliterator) {
                    return (Transliterator) obj;
                }
                TransliteratorParser transliteratorParser = new TransliteratorParser();
                try {
                    ResourceEntry resourceEntry = (ResourceEntry) obj;
                    transliteratorParser.parse(resourceEntry.resource, resourceEntry.direction);
                } catch (ClassCastException unused2) {
                    LocaleEntry localeEntry = (LocaleEntry) obj;
                    transliteratorParser.parse(localeEntry.rule, localeEntry.direction);
                }
                if (transliteratorParser.idBlockVector.size() == 0 && transliteratorParser.dataVector.size() == 0) {
                    objArr[0] = new AliasEntry("Any-Null");
                } else if (transliteratorParser.idBlockVector.size() == 0 && transliteratorParser.dataVector.size() == 1) {
                    objArr[0] = transliteratorParser.dataVector.get(0);
                } else if (transliteratorParser.idBlockVector.size() != 1 || transliteratorParser.dataVector.size() != 0) {
                    objArr[0] = new CompoundRBTEntry(str, transliteratorParser.idBlockVector, transliteratorParser.dataVector, transliteratorParser.compoundFilter);
                } else if (transliteratorParser.compoundFilter != null) {
                    objArr[0] = new AliasEntry(transliteratorParser.compoundFilter.toPattern(false) + ";" + transliteratorParser.idBlockVector.get(0));
                } else {
                    objArr[0] = new AliasEntry(transliteratorParser.idBlockVector.get(0));
                }
            }
        }
    }
}
