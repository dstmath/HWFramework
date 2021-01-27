package ohos.global.icu.text;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import ohos.global.icu.lang.UScript;
import ohos.global.icu.text.Transliterator;

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

    private boolean isWide(int i) {
        return i == 5 || i == 17 || i == 18 || i == 20 || i == 22;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.Transliterator
    public void handleTransliterate(Replaceable replaceable, Transliterator.Position position, boolean z) {
        int i = position.start;
        int i2 = position.limit;
        ScriptRunIterator scriptRunIterator = new ScriptRunIterator(replaceable, position.contextStart, position.contextLimit);
        while (scriptRunIterator.next()) {
            if (scriptRunIterator.limit > i) {
                Transliterator transliterator = getTransliterator(scriptRunIterator.scriptCode);
                if (transliterator == null) {
                    position.start = scriptRunIterator.limit;
                } else {
                    boolean z2 = z && scriptRunIterator.limit >= i2;
                    position.start = Math.max(i, scriptRunIterator.start);
                    position.limit = Math.min(i2, scriptRunIterator.limit);
                    int i3 = position.limit;
                    transliterator.filteredTransliterate(replaceable, position, z2);
                    int i4 = position.limit - i3;
                    i2 += i4;
                    scriptRunIterator.adjustLimit(i4);
                    if (scriptRunIterator.limit >= i2) {
                        break;
                    }
                }
            }
        }
        position.limit = i2;
    }

    private AnyTransliterator(String str, String str2, String str3, int i) {
        super(str, null);
        this.targetScript = i;
        this.cache = new ConcurrentHashMap<>();
        this.target = str2;
        if (str3.length() > 0) {
            this.target = str2 + VARIANT_SEP + str3;
        }
    }

    public AnyTransliterator(String str, UnicodeFilter unicodeFilter, String str2, int i, Transliterator transliterator, ConcurrentHashMap<Integer, Transliterator> concurrentHashMap) {
        super(str, unicodeFilter);
        this.targetScript = i;
        this.cache = concurrentHashMap;
        this.target = str2;
    }

    private Transliterator getTransliterator(int i) {
        if (i != this.targetScript && i != -1) {
            Integer valueOf = Integer.valueOf(i);
            Transliterator transliterator = this.cache.get(valueOf);
            if (transliterator != null) {
                return transliterator;
            }
            String name = UScript.getName(i);
            try {
                transliterator = Transliterator.getInstance(name + TARGET_SEP + this.target, 0);
            } catch (RuntimeException unused) {
            }
            if (transliterator == null) {
                try {
                    transliterator = Transliterator.getInstance(name + LATIN_PIVOT + this.target, 0);
                } catch (RuntimeException unused2) {
                }
            }
            if (transliterator != null) {
                if (!isWide(this.targetScript)) {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(this.widthFix);
                    arrayList.add(transliterator);
                    transliterator = new CompoundTransliterator(arrayList);
                }
                Transliterator putIfAbsent = this.cache.putIfAbsent(valueOf, transliterator);
                return putIfAbsent != null ? putIfAbsent : transliterator;
            } else if (!isWide(this.targetScript)) {
                return this.widthFix;
            } else {
                return transliterator;
            }
        } else if (isWide(this.targetScript)) {
            return null;
        } else {
            return this.widthFix;
        }
    }

    static void register() {
        HashMap hashMap = new HashMap();
        Enumeration<String> availableSources = Transliterator.getAvailableSources();
        while (availableSources.hasMoreElements()) {
            String nextElement = availableSources.nextElement();
            if (!nextElement.equalsIgnoreCase(ANY)) {
                Enumeration<String> availableTargets = Transliterator.getAvailableTargets(nextElement);
                while (availableTargets.hasMoreElements()) {
                    String nextElement2 = availableTargets.nextElement();
                    int scriptNameToCode = scriptNameToCode(nextElement2);
                    if (scriptNameToCode != -1) {
                        Set set = (Set) hashMap.get(nextElement2);
                        if (set == null) {
                            set = new HashSet();
                            hashMap.put(nextElement2, set);
                        }
                        Enumeration<String> availableVariants = Transliterator.getAvailableVariants(nextElement, nextElement2);
                        while (availableVariants.hasMoreElements()) {
                            String nextElement3 = availableVariants.nextElement();
                            if (!set.contains(nextElement3)) {
                                set.add(nextElement3);
                                Transliterator.registerInstance(new AnyTransliterator(TransliteratorIDParser.STVtoID(ANY, nextElement2, nextElement3), nextElement2, nextElement3, scriptNameToCode));
                                Transliterator.registerSpecialInverse(nextElement2, NULL_ID, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private static int scriptNameToCode(String str) {
        try {
            int[] code = UScript.getCode(str);
            if (code != null) {
                return code[0];
            }
            return -1;
        } catch (MissingResourceException unused) {
            return -1;
        }
    }

    private static class ScriptRunIterator {
        public int limit;
        public int scriptCode;
        public int start;
        private Replaceable text;
        private int textLimit;
        private int textStart;

        public ScriptRunIterator(Replaceable replaceable, int i, int i2) {
            this.text = replaceable;
            this.textStart = i;
            this.textLimit = i2;
            this.limit = i;
        }

        public boolean next() {
            int script;
            this.scriptCode = -1;
            this.start = this.limit;
            if (this.start == this.textLimit) {
                return false;
            }
            while (true) {
                int i = this.start;
                if (i <= this.textStart || !((script = UScript.getScript(this.text.char32At(i - 1))) == 0 || script == 1)) {
                    break;
                }
                this.start--;
            }
            while (true) {
                int i2 = this.limit;
                if (i2 >= this.textLimit) {
                    break;
                }
                int script2 = UScript.getScript(this.text.char32At(i2));
                if (!(script2 == 0 || script2 == 1)) {
                    int i3 = this.scriptCode;
                    if (i3 == -1) {
                        this.scriptCode = script2;
                    } else if (script2 != i3) {
                        break;
                    }
                }
                this.limit++;
            }
            return true;
        }

        public void adjustLimit(int i) {
            this.limit += i;
            this.textLimit += i;
        }
    }

    public Transliterator safeClone() {
        UnicodeFilter filter = getFilter();
        return new AnyTransliterator(getID(), (filter == null || !(filter instanceof UnicodeSet)) ? filter : new UnicodeSet((UnicodeSet) filter), this.target, this.targetScript, this.widthFix, this.cache);
    }

    @Override // ohos.global.icu.text.Transliterator
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        UnicodeSet filterAsUnicodeSet = getFilterAsUnicodeSet(unicodeSet);
        unicodeSet2.addAll(filterAsUnicodeSet);
        if (filterAsUnicodeSet.size() != 0) {
            unicodeSet3.addAll(0, 1114111);
        }
    }
}
