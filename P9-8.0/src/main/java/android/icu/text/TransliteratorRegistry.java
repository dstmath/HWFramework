package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.LocaleUtility;
import android.icu.impl.Utility;
import android.icu.lang.UScript;
import android.icu.text.Transliterator.Factory;
import android.icu.util.CaseInsensitiveString;
import android.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class TransliteratorRegistry {
    private static final String ANY = "Any";
    private static final boolean DEBUG = false;
    private static final char LOCALE_SEP = '_';
    private static final String NO_VARIANT = "";
    private List<CaseInsensitiveString> availableIDs = new ArrayList();
    private Map<CaseInsensitiveString, Object[]> registry = Collections.synchronizedMap(new HashMap());
    private Map<CaseInsensitiveString, Map<CaseInsensitiveString, List<CaseInsensitiveString>>> specDAG = Collections.synchronizedMap(new HashMap());

    static class AliasEntry {
        public String alias;

        public AliasEntry(String a) {
            this.alias = a;
        }
    }

    static class CompoundRBTEntry {
        private String ID;
        private UnicodeSet compoundFilter;
        private List<Data> dataVector;
        private List<String> idBlockVector;

        public CompoundRBTEntry(String theID, List<String> theIDBlockVector, List<Data> theDataVector, UnicodeSet theCompoundFilter) {
            this.ID = theID;
            this.idBlockVector = theIDBlockVector;
            this.dataVector = theDataVector;
            this.compoundFilter = theCompoundFilter;
        }

        public Transliterator getInstance() {
            List<Transliterator> transliterators = new ArrayList();
            int limit = Math.max(this.idBlockVector.size(), this.dataVector.size());
            int i = 0;
            int passNumber = 1;
            while (i < limit) {
                int passNumber2;
                if (i < this.idBlockVector.size()) {
                    String idBlock = (String) this.idBlockVector.get(i);
                    if (idBlock.length() > 0) {
                        transliterators.add(Transliterator.getInstance(idBlock));
                    }
                }
                if (i < this.dataVector.size()) {
                    passNumber2 = passNumber + 1;
                    transliterators.add(new RuleBasedTransliterator("%Pass" + passNumber, (Data) this.dataVector.get(i), null));
                } else {
                    passNumber2 = passNumber;
                }
                i++;
                passNumber = passNumber2;
            }
            Transliterator t = new CompoundTransliterator(transliterators, passNumber - 1);
            t.setID(this.ID);
            if (this.compoundFilter != null) {
                t.setFilter(this.compoundFilter);
            }
            return t;
        }
    }

    private static class IDEnumeration implements Enumeration<String> {
        Enumeration<CaseInsensitiveString> en;

        public IDEnumeration(Enumeration<CaseInsensitiveString> e) {
            this.en = e;
        }

        public boolean hasMoreElements() {
            return this.en != null ? this.en.hasMoreElements() : false;
        }

        public String nextElement() {
            return ((CaseInsensitiveString) this.en.nextElement()).getString();
        }
    }

    static class LocaleEntry {
        public int direction;
        public String rule;

        public LocaleEntry(String r, int d) {
            this.rule = r;
            this.direction = d;
        }
    }

    static class ResourceEntry {
        public int direction;
        public String resource;

        public ResourceEntry(String n, int d) {
            this.resource = n;
            this.direction = d;
        }
    }

    static class Spec {
        private boolean isNextLocale;
        private boolean isSpecLocale;
        private String nextSpec;
        private ICUResourceBundle res;
        private String scriptName = null;
        private String spec = null;
        private String top;

        public Spec(String theSpec) {
            this.top = theSpec;
            try {
                int script = UScript.getCodeFromName(this.top);
                int[] s = UScript.getCode(this.top);
                if (s != null) {
                    this.scriptName = UScript.getName(s[0]);
                    if (this.scriptName.equalsIgnoreCase(this.top)) {
                        this.scriptName = null;
                    }
                }
                this.isSpecLocale = false;
                this.res = null;
                if (script == -1) {
                    this.res = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME, LocaleUtility.getLocaleFromName(this.top));
                    if (this.res != null && LocaleUtility.isFallbackOf(this.res.getULocale().toString(), this.top)) {
                        this.isSpecLocale = true;
                    }
                }
            } catch (MissingResourceException e) {
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
                int i = this.nextSpec.lastIndexOf(95);
                if (i > 0) {
                    this.nextSpec = this.spec.substring(0, i);
                    this.isNextLocale = true;
                    return;
                }
                this.nextSpec = this.scriptName;
            } else if (Utility.sameObjects(this.nextSpec, this.scriptName)) {
                this.nextSpec = null;
            } else {
                this.nextSpec = this.scriptName;
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

        public ResourceBundle getBundle() {
            if (this.res == null || !this.res.getULocale().toString().equals(this.spec)) {
                return null;
            }
            return this.res;
        }

        public String getTop() {
            return this.top;
        }
    }

    public Transliterator get(String ID, StringBuffer aliasReturn) {
        Object[] entry = find(ID);
        if (entry == null) {
            return null;
        }
        return instantiateEntry(ID, entry, aliasReturn);
    }

    public void put(String ID, Class<? extends Transliterator> transliteratorSubclass, boolean visible) {
        registerEntry(ID, transliteratorSubclass, visible);
    }

    public void put(String ID, Factory factory, boolean visible) {
        registerEntry(ID, factory, visible);
    }

    public void put(String ID, String resourceName, int dir, boolean visible) {
        registerEntry(ID, new ResourceEntry(resourceName, dir), visible);
    }

    public void put(String ID, String alias, boolean visible) {
        registerEntry(ID, new AliasEntry(alias), visible);
    }

    public void put(String ID, Transliterator trans, boolean visible) {
        registerEntry(ID, trans, visible);
    }

    public void remove(String ID) {
        String[] stv = TransliteratorIDParser.IDtoSTV(ID);
        String id = TransliteratorIDParser.STVtoID(stv[0], stv[1], stv[2]);
        this.registry.remove(new CaseInsensitiveString(id));
        removeSTV(stv[0], stv[1], stv[2]);
        this.availableIDs.remove(new CaseInsensitiveString(id));
    }

    public Enumeration<String> getAvailableIDs() {
        return new IDEnumeration(Collections.enumeration(this.availableIDs));
    }

    public Enumeration<String> getAvailableSources() {
        return new IDEnumeration(Collections.enumeration(this.specDAG.keySet()));
    }

    public Enumeration<String> getAvailableTargets(String source) {
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> targets = (Map) this.specDAG.get(new CaseInsensitiveString(source));
        if (targets == null) {
            return new IDEnumeration(null);
        }
        return new IDEnumeration(Collections.enumeration(targets.keySet()));
    }

    public Enumeration<String> getAvailableVariants(String source, String target) {
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> targets = (Map) this.specDAG.get(cisrc);
        if (targets == null) {
            return new IDEnumeration(null);
        }
        List<CaseInsensitiveString> variants = (List) targets.get(citrg);
        if (variants == null) {
            return new IDEnumeration(null);
        }
        return new IDEnumeration(Collections.enumeration(variants));
    }

    private void registerEntry(String source, String target, String variant, Object entry, boolean visible) {
        String s = source;
        if (source.length() == 0) {
            s = ANY;
        }
        registerEntry(TransliteratorIDParser.STVtoID(source, target, variant), s, target, variant, entry, visible);
    }

    private void registerEntry(String ID, Object entry, boolean visible) {
        String[] stv = TransliteratorIDParser.IDtoSTV(ID);
        registerEntry(TransliteratorIDParser.STVtoID(stv[0], stv[1], stv[2]), stv[0], stv[1], stv[2], entry, visible);
    }

    private void registerEntry(String ID, String source, String target, String variant, Object entry, boolean visible) {
        CaseInsensitiveString ciID = new CaseInsensitiveString(ID);
        this.registry.put(ciID, entry instanceof Object[] ? (Object[]) entry : new Object[]{entry});
        if (visible) {
            registerSTV(source, target, variant);
            if (!this.availableIDs.contains(ciID)) {
                this.availableIDs.add(ciID);
                return;
            }
            return;
        }
        removeSTV(source, target, variant);
        this.availableIDs.remove(ciID);
    }

    private void registerSTV(String source, String target, String variant) {
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        CaseInsensitiveString civar = new CaseInsensitiveString(variant);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> targets = (Map) this.specDAG.get(cisrc);
        if (targets == null) {
            targets = Collections.synchronizedMap(new HashMap());
            this.specDAG.put(cisrc, targets);
        }
        List<CaseInsensitiveString> variants = (List) targets.get(citrg);
        if (variants == null) {
            variants = new ArrayList();
            targets.put(citrg, variants);
        }
        if (!variants.contains(civar)) {
            if (variant.length() > 0) {
                variants.add(civar);
            } else {
                variants.add(0, civar);
            }
        }
    }

    private void removeSTV(String source, String target, String variant) {
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        CaseInsensitiveString civar = new CaseInsensitiveString(variant);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> targets = (Map) this.specDAG.get(cisrc);
        if (targets != null) {
            List<CaseInsensitiveString> variants = (List) targets.get(citrg);
            if (variants != null) {
                variants.remove(civar);
                if (variants.size() == 0) {
                    targets.remove(citrg);
                    if (targets.size() == 0) {
                        this.specDAG.remove(cisrc);
                    }
                }
            }
        }
    }

    private Object[] findInDynamicStore(Spec src, Spec trg, String variant) {
        return (Object[]) this.registry.get(new CaseInsensitiveString(TransliteratorIDParser.STVtoID(src.get(), trg.get(), variant)));
    }

    private Object[] findInStaticStore(Spec src, Spec trg, String variant) {
        Object entry = null;
        if (src.isLocale()) {
            entry = findInBundle(src, trg, variant, 0);
        } else if (trg.isLocale()) {
            entry = findInBundle(trg, src, variant, 1);
        }
        if (entry != null) {
            registerEntry(src.getTop(), trg.getTop(), variant, entry, false);
        }
        return entry;
    }

    private Object[] findInBundle(Spec specToOpen, Spec specToFind, String variant, int direction) {
        ResourceBundle res = specToOpen.getBundle();
        if (res == null) {
            return null;
        }
        int pass = 0;
        while (pass < 2) {
            StringBuilder tag = new StringBuilder();
            if (pass == 0) {
                tag.append(direction == 0 ? "TransliterateTo" : "TransliterateFrom");
            } else {
                tag.append("Transliterate");
            }
            tag.append(specToFind.get().toUpperCase(Locale.ENGLISH));
            try {
                String[] subres = res.getStringArray(tag.toString());
                int i = 0;
                if (variant.length() != 0) {
                    i = 0;
                    while (i < subres.length && !subres[i].equalsIgnoreCase(variant)) {
                        i += 2;
                    }
                }
                if (i < subres.length) {
                    int dir = pass == 0 ? 0 : direction;
                    return new Object[]{new LocaleEntry(subres[i + 1], dir)};
                }
                pass++;
            } catch (MissingResourceException e) {
            }
        }
        return null;
    }

    private Object[] find(String ID) {
        String[] stv = TransliteratorIDParser.IDtoSTV(ID);
        return find(stv[0], stv[1], stv[2]);
    }

    /* JADX WARNING: Missing block: B:19:0x0044, code:
            if (r2.hasFallback() != false) goto L_0x0020;
     */
    /* JADX WARNING: Missing block: B:20:0x0046, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Object[] find(String source, String target, String variant) {
        Object[] entry;
        Spec src = new Spec(source);
        Spec trg = new Spec(target);
        if (variant.length() != 0) {
            entry = findInDynamicStore(src, trg, variant);
            if (entry != null) {
                return entry;
            }
            entry = findInStaticStore(src, trg, variant);
            if (entry != null) {
                return entry;
            }
        }
        while (true) {
            src.reset();
            while (true) {
                entry = findInDynamicStore(src, trg, "");
                if (entry != null) {
                    return entry;
                }
                entry = findInStaticStore(src, trg, "");
                if (entry != null) {
                    return entry;
                }
                if (!src.hasFallback()) {
                    break;
                }
                src.next();
            }
            trg.next();
        }
    }

    private Transliterator instantiateEntry(String ID, Object[] entryWrapper, StringBuffer aliasReturn) {
        while (true) {
            Data entry = entryWrapper[0];
            if (entry instanceof Data) {
                return new RuleBasedTransliterator(ID, entry, null);
            } else if (entry instanceof Class) {
                try {
                    return (Transliterator) ((Class) entry).newInstance();
                } catch (InstantiationException e) {
                    return null;
                } catch (IllegalAccessException e2) {
                    return null;
                }
            } else if (entry instanceof AliasEntry) {
                aliasReturn.append(((AliasEntry) entry).alias);
                return null;
            } else if (entry instanceof Factory) {
                return ((Factory) entry).getInstance(ID);
            } else {
                if (entry instanceof CompoundRBTEntry) {
                    return ((CompoundRBTEntry) entry).getInstance();
                }
                if (entry instanceof AnyTransliterator) {
                    return ((AnyTransliterator) entry).safeClone();
                }
                if (entry instanceof RuleBasedTransliterator) {
                    return ((RuleBasedTransliterator) entry).safeClone();
                }
                if (entry instanceof CompoundTransliterator) {
                    return ((CompoundTransliterator) entry).safeClone();
                }
                if (entry instanceof Transliterator) {
                    return (Transliterator) entry;
                }
                TransliteratorParser parser = new TransliteratorParser();
                try {
                    ResourceEntry re = (ResourceEntry) entry;
                    parser.parse(re.resource, re.direction);
                } catch (ClassCastException e3) {
                    LocaleEntry le = (LocaleEntry) entry;
                    parser.parse(le.rule, le.direction);
                }
                if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == 0) {
                    entryWrapper[0] = new AliasEntry("Any-Null");
                } else if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == 1) {
                    entryWrapper[0] = parser.dataVector.get(0);
                } else if (parser.idBlockVector.size() != 1 || parser.dataVector.size() != 0) {
                    entryWrapper[0] = new CompoundRBTEntry(ID, parser.idBlockVector, parser.dataVector, parser.compoundFilter);
                } else if (parser.compoundFilter != null) {
                    entryWrapper[0] = new AliasEntry(parser.compoundFilter.toPattern(false) + ";" + ((String) parser.idBlockVector.get(0)));
                } else {
                    entryWrapper[0] = new AliasEntry((String) parser.idBlockVector.get(0));
                }
            }
        }
    }
}
