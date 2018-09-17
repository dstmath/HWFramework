package android.icu.text;

import android.icu.lang.UScript;
import android.icu.text.Transliterator.Position;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class AnyTransliterator extends Transliterator {
    static final String ANY = "Any";
    static final String LATIN_PIVOT = "-Latin;Latin-";
    static final String NULL_ID = "Null";
    static final char TARGET_SEP = '-';
    static final char VARIANT_SEP = '/';
    private ConcurrentHashMap<Integer, Transliterator> cache;
    private String target;
    private int targetScript;
    private Transliterator widthFix = Transliterator.getInstance("[[:dt=Nar:][:dt=Wide:]] nfkd");

    private static class ScriptRunIterator {
        public int limit;
        public int scriptCode;
        public int start;
        private Replaceable text;
        private int textLimit;
        private int textStart;

        public ScriptRunIterator(Replaceable text, int start, int limit) {
            this.text = text;
            this.textStart = start;
            this.textLimit = limit;
            this.limit = start;
        }

        public boolean next() {
            this.scriptCode = -1;
            this.start = this.limit;
            if (this.start == this.textLimit) {
                return false;
            }
            int s;
            while (this.start > this.textStart) {
                s = UScript.getScript(this.text.char32At(this.start - 1));
                if (s != 0 && s != 1) {
                    break;
                }
                this.start--;
            }
            while (this.limit < this.textLimit) {
                s = UScript.getScript(this.text.char32At(this.limit));
                if (!(s == 0 || s == 1)) {
                    if (this.scriptCode == -1) {
                        this.scriptCode = s;
                    } else if (s != this.scriptCode) {
                        break;
                    }
                }
                this.limit++;
            }
            return true;
        }

        public void adjustLimit(int delta) {
            this.limit += delta;
            this.textLimit += delta;
        }
    }

    protected void handleTransliterate(Replaceable text, Position pos, boolean isIncremental) {
        int allStart = pos.start;
        int allLimit = pos.limit;
        ScriptRunIterator it = new ScriptRunIterator(text, pos.contextStart, pos.contextLimit);
        while (it.next()) {
            if (it.limit > allStart) {
                Transliterator t = getTransliterator(it.scriptCode);
                if (t == null) {
                    pos.start = it.limit;
                } else {
                    boolean incremental = isIncremental && it.limit >= allLimit;
                    pos.start = Math.max(allStart, it.start);
                    pos.limit = Math.min(allLimit, it.limit);
                    int limit = pos.limit;
                    t.filteredTransliterate(text, pos, incremental);
                    int delta = pos.limit - limit;
                    allLimit += delta;
                    it.adjustLimit(delta);
                    if (it.limit >= allLimit) {
                        break;
                    }
                }
            }
        }
        pos.limit = allLimit;
    }

    private AnyTransliterator(String id, String theTarget, String theVariant, int theTargetScript) {
        super(id, null);
        this.targetScript = theTargetScript;
        this.cache = new ConcurrentHashMap();
        this.target = theTarget;
        if (theVariant.length() > 0) {
            this.target = theTarget + VARIANT_SEP + theVariant;
        }
    }

    public AnyTransliterator(String id, UnicodeFilter filter, String target2, int targetScript2, Transliterator widthFix2, ConcurrentHashMap<Integer, Transliterator> cache2) {
        super(id, filter);
        this.targetScript = targetScript2;
        this.cache = cache2;
        this.target = target2;
    }

    private Transliterator getTransliterator(int source) {
        if (source != this.targetScript && source != -1) {
            Integer key = Integer.valueOf(source);
            Transliterator t = (Transliterator) this.cache.get(key);
            if (t == null) {
                String sourceName = UScript.getName(source);
                try {
                    t = Transliterator.getInstance(sourceName + TARGET_SEP + this.target, 0);
                } catch (RuntimeException e) {
                }
                if (t == null) {
                    try {
                        t = Transliterator.getInstance(sourceName + LATIN_PIVOT + this.target, 0);
                    } catch (RuntimeException e2) {
                    }
                }
                if (t != null) {
                    if (!isWide(this.targetScript)) {
                        List<Transliterator> v = new ArrayList();
                        v.add(this.widthFix);
                        v.add(t);
                        t = new CompoundTransliterator(v);
                    }
                    Transliterator prevCachedT = (Transliterator) this.cache.putIfAbsent(key, t);
                    if (prevCachedT != null) {
                        t = prevCachedT;
                    }
                } else if (!isWide(this.targetScript)) {
                    return this.widthFix;
                }
            }
            return t;
        } else if (isWide(this.targetScript)) {
            return null;
        } else {
            return this.widthFix;
        }
    }

    private boolean isWide(int script) {
        return script == 5 || script == 17 || script == 18 || script == 20 || script == 22;
    }

    static void register() {
        HashMap<String, Set<String>> seen = new HashMap();
        Enumeration<String> s = Transliterator.getAvailableSources();
        while (s.hasMoreElements()) {
            String source = (String) s.nextElement();
            if (!source.equalsIgnoreCase(ANY)) {
                Enumeration<String> t = Transliterator.getAvailableTargets(source);
                while (t.hasMoreElements()) {
                    String target = (String) t.nextElement();
                    int targetScript = scriptNameToCode(target);
                    if (targetScript != -1) {
                        Set<String> seenVariants = (Set) seen.get(target);
                        if (seenVariants == null) {
                            seenVariants = new HashSet();
                            seen.put(target, seenVariants);
                        }
                        Enumeration<String> v = Transliterator.getAvailableVariants(source, target);
                        while (v.hasMoreElements()) {
                            String variant = (String) v.nextElement();
                            if (!seenVariants.contains(variant)) {
                                seenVariants.add(variant);
                                Transliterator.registerInstance(new AnyTransliterator(TransliteratorIDParser.STVtoID(ANY, target, variant), target, variant, targetScript));
                                Transliterator.registerSpecialInverse(target, NULL_ID, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private static int scriptNameToCode(String name) {
        int i = -1;
        try {
            int[] codes = UScript.getCode(name);
            if (codes != null) {
                i = codes[0];
            }
            return i;
        } catch (MissingResourceException e) {
            return -1;
        }
    }

    public Transliterator safeClone() {
        UnicodeFilter filter = getFilter();
        if (filter != null && (filter instanceof UnicodeSet)) {
            filter = new UnicodeSet((UnicodeSet) filter);
        }
        return new AnyTransliterator(getID(), filter, this.target, this.targetScript, this.widthFix, this.cache);
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        sourceSet.addAll(myFilter);
        if (myFilter.size() != 0) {
            targetSet.addAll(0, 1114111);
        }
    }
}
