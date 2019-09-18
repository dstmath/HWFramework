package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Utility;
import android.icu.text.TransliteratorIDParser;
import android.icu.util.CaseInsensitiveString;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

public abstract class Transliterator implements StringTransform {
    static final boolean DEBUG = false;
    public static final int FORWARD = 0;
    static final char ID_DELIM = ';';
    static final char ID_SEP = '-';
    private static final String RB_DISPLAY_NAME_PATTERN = "TransliteratorNamePattern";
    private static final String RB_DISPLAY_NAME_PREFIX = "%Translit%%";
    private static final String RB_RULE_BASED_IDS = "RuleBasedTransliteratorIDs";
    private static final String RB_SCRIPT_DISPLAY_NAME_PREFIX = "%Translit%";
    public static final int REVERSE = 1;
    private static final String ROOT = "root";
    static final char VARIANT_SEP = '/';
    private static Map<CaseInsensitiveString, String> displayNameCache = Collections.synchronizedMap(new HashMap());
    private static TransliteratorRegistry registry = new TransliteratorRegistry();
    private String ID;
    private UnicodeSet filter;
    private int maximumContextLength = 0;

    public interface Factory {
        Transliterator getInstance(String str);
    }

    public static class Position {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        public int contextLimit;
        public int contextStart;
        public int limit;
        public int start;

        static {
            Class<Transliterator> cls = Transliterator.class;
        }

        public Position() {
            this(0, 0, 0, 0);
        }

        public Position(int contextStart2, int contextLimit2, int start2) {
            this(contextStart2, contextLimit2, start2, contextLimit2);
        }

        public Position(int contextStart2, int contextLimit2, int start2, int limit2) {
            this.contextStart = contextStart2;
            this.contextLimit = contextLimit2;
            this.start = start2;
            this.limit = limit2;
        }

        public Position(Position pos) {
            set(pos);
        }

        public void set(Position pos) {
            this.contextStart = pos.contextStart;
            this.contextLimit = pos.contextLimit;
            this.start = pos.start;
            this.limit = pos.limit;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof Position)) {
                return false;
            }
            Position pos = (Position) obj;
            if (this.contextStart == pos.contextStart && this.contextLimit == pos.contextLimit && this.start == pos.start && this.limit == pos.limit) {
                z = true;
            }
            return z;
        }

        @Deprecated
        public int hashCode() {
            return 42;
        }

        public String toString() {
            return "[cs=" + this.contextStart + ", s=" + this.start + ", l=" + this.limit + ", cl=" + this.contextLimit + "]";
        }

        public final void validate(int length) {
            if (this.contextStart < 0 || this.start < this.contextStart || this.limit < this.start || this.contextLimit < this.limit || length < this.contextLimit) {
                throw new IllegalArgumentException("Invalid Position {cs=" + this.contextStart + ", s=" + this.start + ", l=" + this.limit + ", cl=" + this.contextLimit + "}, len=" + length);
            }
        }
    }

    /* access modifiers changed from: protected */
    public abstract void handleTransliterate(Replaceable replaceable, Position position, boolean z);

    protected Transliterator(String ID2, UnicodeFilter filter2) {
        if (ID2 != null) {
            this.ID = ID2;
            setFilter(filter2);
            return;
        }
        throw new NullPointerException();
    }

    public final int transliterate(Replaceable text, int start, int limit) {
        if (start < 0 || limit < start || text.length() < limit) {
            return -1;
        }
        Position pos = new Position(start, limit, start);
        filteredTransliterate(text, pos, false, true);
        return pos.limit;
    }

    public final void transliterate(Replaceable text) {
        transliterate(text, 0, text.length());
    }

    public final String transliterate(String text) {
        ReplaceableString result = new ReplaceableString(text);
        transliterate((Replaceable) result);
        return result.toString();
    }

    public final void transliterate(Replaceable text, Position index, String insertion) {
        index.validate(text.length());
        if (insertion != null) {
            text.replace(index.limit, index.limit, insertion);
            index.limit += insertion.length();
            index.contextLimit += insertion.length();
        }
        if (index.limit <= 0 || !UTF16.isLeadSurrogate(text.charAt(index.limit - 1))) {
            filteredTransliterate(text, index, true, true);
        }
    }

    public final void transliterate(Replaceable text, Position index, int insertion) {
        transliterate(text, index, UTF16.valueOf(insertion));
    }

    public final void transliterate(Replaceable text, Position index) {
        transliterate(text, index, (String) null);
    }

    public final void finishTransliteration(Replaceable text, Position index) {
        index.validate(text.length());
        filteredTransliterate(text, index, false, true);
    }

    private void filteredTransliterate(Replaceable text, Position index, boolean incremental, boolean rollback) {
        StringBuffer log;
        int runLength;
        Replaceable replaceable = text;
        Position position = index;
        if (this.filter != null || rollback) {
            int globalLimit = position.limit;
            StringBuffer log2 = null;
            while (true) {
                if (this.filter != null) {
                    while (position.start < globalLimit) {
                        UnicodeSet unicodeSet = this.filter;
                        int char32At = replaceable.char32At(position.start);
                        int c = char32At;
                        if (unicodeSet.contains(char32At)) {
                            break;
                        }
                        position.start += UTF16.getCharCount(c);
                    }
                    position.limit = position.start;
                    while (position.limit < globalLimit) {
                        UnicodeSet unicodeSet2 = this.filter;
                        int char32At2 = replaceable.char32At(position.limit);
                        int c2 = char32At2;
                        if (!unicodeSet2.contains(char32At2)) {
                            break;
                        }
                        position.limit += UTF16.getCharCount(c2);
                    }
                }
                if (position.start != position.limit) {
                    int totalDelta = 0;
                    boolean isIncrementalRun = position.limit < globalLimit ? false : incremental;
                    if (!rollback || !isIncrementalRun) {
                        log = log2;
                        int limit = position.limit;
                        handleTransliterate(replaceable, position, isIncrementalRun);
                        int delta = position.limit - limit;
                        if (isIncrementalRun || position.start == position.limit) {
                            globalLimit += delta;
                        } else {
                            throw new RuntimeException("ERROR: Incomplete non-incremental transliteration by " + getID());
                        }
                    } else {
                        int runStart = position.start;
                        int runLimit = position.limit;
                        int runLength2 = runLimit - runStart;
                        int rollbackOrigin = text.length();
                        replaceable.copy(runStart, runLimit, rollbackOrigin);
                        int passStart = runStart;
                        int rollbackStart = rollbackOrigin;
                        int passLimit = position.start;
                        int uncommittedLength = 0;
                        while (true) {
                            int charLength = UTF16.getCharCount(replaceable.char32At(passLimit));
                            passLimit += charLength;
                            if (passLimit > runLimit) {
                                break;
                            }
                            uncommittedLength += charLength;
                            position.limit = passLimit;
                            int i = charLength;
                            handleTransliterate(replaceable, position, true);
                            int delta2 = position.limit - passLimit;
                            StringBuffer log3 = log2;
                            int runStart2 = runStart;
                            if (position.start != position.limit) {
                                int rs = (rollbackStart + delta2) - (position.limit - passStart);
                                runLength = runLength2;
                                replaceable.replace(passStart, position.limit, "");
                                replaceable.copy(rs, rs + uncommittedLength, passStart);
                                position.start = passStart;
                                position.limit = passLimit;
                                position.contextLimit -= delta2;
                            } else {
                                runLength = runLength2;
                                int passStart2 = position.start;
                                rollbackStart += delta2 + uncommittedLength;
                                runLimit += delta2;
                                totalDelta += delta2;
                                passStart = passStart2;
                                passLimit = passStart2;
                                uncommittedLength = 0;
                            }
                            log2 = log3;
                            runStart = runStart2;
                            runLength2 = runLength;
                        }
                        int rollbackOrigin2 = rollbackOrigin + totalDelta;
                        replaceable.replace(rollbackOrigin2, rollbackOrigin2 + runLength2, "");
                        position.start = passStart;
                        log = log2;
                        globalLimit += totalDelta;
                    }
                    if (this.filter == null || isIncrementalRun) {
                        break;
                    }
                    log2 = log;
                } else {
                    StringBuffer stringBuffer = log2;
                    break;
                }
            }
            position.limit = globalLimit;
            return;
        }
        handleTransliterate(text, index, incremental);
    }

    public void filteredTransliterate(Replaceable text, Position index, boolean incremental) {
        filteredTransliterate(text, index, incremental, false);
    }

    public final int getMaximumContextLength() {
        return this.maximumContextLength;
    }

    /* access modifiers changed from: protected */
    public void setMaximumContextLength(int a) {
        if (a >= 0) {
            this.maximumContextLength = a;
            return;
        }
        throw new IllegalArgumentException("Invalid context length " + a);
    }

    public final String getID() {
        return this.ID;
    }

    /* access modifiers changed from: protected */
    public final void setID(String id) {
        this.ID = id;
    }

    public static final String getDisplayName(String ID2) {
        return getDisplayName(ID2, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public static String getDisplayName(String id, Locale inLocale) {
        return getDisplayName(id, ULocale.forLocale(inLocale));
    }

    public static String getDisplayName(String id, ULocale inLocale) {
        String str;
        ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME, inLocale);
        String[] stv = TransliteratorIDParser.IDtoSTV(id);
        if (stv == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(stv[0]);
        sb.append(ID_SEP);
        sb.append(stv[1]);
        String ID2 = sb.toString();
        if (stv[2] != null && stv[2].length() > 0) {
            ID2 = ID2 + VARIANT_SEP + stv[2];
        }
        String n = displayNameCache.get(new CaseInsensitiveString(ID2));
        if (n != null) {
            return n;
        }
        try {
            return bundle.getString(RB_DISPLAY_NAME_PREFIX + ID2);
        } catch (MissingResourceException e) {
            try {
                MessageFormat format = new MessageFormat(bundle.getString(RB_DISPLAY_NAME_PATTERN));
                Object[] args = {2, stv[0], stv[1]};
                for (int j = 1; j <= 2; j++) {
                    try {
                        args[j] = bundle.getString(RB_SCRIPT_DISPLAY_NAME_PREFIX + ((String) args[j]));
                    } catch (MissingResourceException e2) {
                    }
                }
                if (stv[2].length() > 0) {
                    str = format.format(args) + VARIANT_SEP + stv[2];
                } else {
                    str = format.format(args);
                }
                return str;
            } catch (MissingResourceException e3) {
                throw new RuntimeException();
            }
        }
    }

    public final UnicodeFilter getFilter() {
        return this.filter;
    }

    public void setFilter(UnicodeFilter filter2) {
        if (filter2 == null) {
            this.filter = null;
            return;
        }
        try {
            this.filter = new UnicodeSet((UnicodeSet) filter2).freeze();
        } catch (Exception e) {
            this.filter = new UnicodeSet();
            filter2.addMatchSetTo(this.filter);
            this.filter.freeze();
        }
    }

    public static final Transliterator getInstance(String ID2) {
        return getInstance(ID2, 0);
    }

    public static Transliterator getInstance(String ID2, int dir) {
        Transliterator t;
        StringBuffer canonID = new StringBuffer();
        List<TransliteratorIDParser.SingleID> list = new ArrayList<>();
        UnicodeSet[] globalFilter = new UnicodeSet[1];
        if (TransliteratorIDParser.parseCompoundID(ID2, dir, canonID, list, globalFilter)) {
            List<Transliterator> translits = TransliteratorIDParser.instantiateList(list);
            if (list.size() > 1 || canonID.indexOf(";") >= 0) {
                t = new CompoundTransliterator(translits);
            } else {
                t = translits.get(0);
            }
            t.setID(canonID.toString());
            if (globalFilter[0] != null) {
                t.setFilter(globalFilter[0]);
            }
            return t;
        }
        throw new IllegalArgumentException("Invalid ID " + ID2);
    }

    static Transliterator getBasicInstance(String id, String canonID) {
        StringBuffer s = new StringBuffer();
        Transliterator t = registry.get(id, s);
        if (s.length() != 0) {
            t = getInstance(s.toString(), 0);
        }
        if (!(t == null || canonID == null)) {
            t.setID(canonID);
        }
        return t;
    }

    public static final Transliterator createFromRules(String ID2, String rules, int dir) {
        Transliterator t;
        TransliteratorParser parser = new TransliteratorParser();
        parser.parse(rules, dir);
        if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == 0) {
            return new NullTransliterator();
        }
        if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == 1) {
            return new RuleBasedTransliterator(ID2, parser.dataVector.get(0), parser.compoundFilter);
        }
        if (parser.idBlockVector.size() == 1 && parser.dataVector.size() == 0) {
            if (parser.compoundFilter != null) {
                t = getInstance(parser.compoundFilter.toPattern(false) + ";" + parser.idBlockVector.get(0));
            } else {
                t = getInstance(parser.idBlockVector.get(0));
            }
            if (t == null) {
                return t;
            }
            t.setID(ID2);
            return t;
        }
        List<Transliterator> transliterators = new ArrayList<>();
        int passNumber = 1;
        int limit = Math.max(parser.idBlockVector.size(), parser.dataVector.size());
        for (int i = 0; i < limit; i++) {
            if (i < parser.idBlockVector.size()) {
                String idBlock = parser.idBlockVector.get(i);
                if (idBlock.length() > 0 && !(getInstance(idBlock) instanceof NullTransliterator)) {
                    transliterators.add(getInstance(idBlock));
                }
            }
            if (i < parser.dataVector.size()) {
                StringBuilder sb = new StringBuilder();
                sb.append("%Pass");
                int passNumber2 = passNumber + 1;
                sb.append(passNumber);
                transliterators.add(new RuleBasedTransliterator(sb.toString(), parser.dataVector.get(i), null));
                passNumber = passNumber2;
            }
        }
        Transliterator t2 = new CompoundTransliterator(transliterators, passNumber - 1);
        t2.setID(ID2);
        if (parser.compoundFilter == null) {
            return t2;
        }
        t2.setFilter(parser.compoundFilter);
        return t2;
    }

    public String toRules(boolean escapeUnprintable) {
        return baseToRules(escapeUnprintable);
    }

    /* access modifiers changed from: protected */
    public final String baseToRules(boolean escapeUnprintable) {
        if (escapeUnprintable) {
            StringBuffer rulesSource = new StringBuffer();
            String id = getID();
            int i = 0;
            while (i < id.length()) {
                int c = UTF16.charAt(id, i);
                if (!Utility.escapeUnprintable(rulesSource, c)) {
                    UTF16.append(rulesSource, c);
                }
                i += UTF16.getCharCount(c);
            }
            rulesSource.insert(0, "::");
            rulesSource.append(ID_DELIM);
            return rulesSource.toString();
        }
        return "::" + getID() + ID_DELIM;
    }

    public Transliterator[] getElements() {
        Transliterator[] result;
        if (this instanceof CompoundTransliterator) {
            CompoundTransliterator cpd = (CompoundTransliterator) this;
            result = new Transliterator[cpd.getCount()];
            for (int i = 0; i < result.length; i++) {
                result[i] = cpd.getTransliterator(i);
            }
        } else {
            result = new Transliterator[]{this};
        }
        return result;
    }

    public final UnicodeSet getSourceSet() {
        UnicodeSet result = new UnicodeSet();
        addSourceTargetSet(getFilterAsUnicodeSet(UnicodeSet.ALL_CODE_POINTS), result, new UnicodeSet());
        return result;
    }

    /* access modifiers changed from: protected */
    public UnicodeSet handleGetSourceSet() {
        return new UnicodeSet();
    }

    public UnicodeSet getTargetSet() {
        UnicodeSet result = new UnicodeSet();
        addSourceTargetSet(getFilterAsUnicodeSet(UnicodeSet.ALL_CODE_POINTS), new UnicodeSet(), result);
        return result;
    }

    @Deprecated
    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet temp = new UnicodeSet(handleGetSourceSet()).retainAll(getFilterAsUnicodeSet(inputFilter));
        sourceSet.addAll(temp);
        Iterator<String> it = temp.iterator();
        while (it.hasNext()) {
            String s = it.next();
            String t = transliterate(s);
            if (!s.equals(t)) {
                targetSet.addAll((CharSequence) t);
            }
        }
    }

    @Deprecated
    public UnicodeSet getFilterAsUnicodeSet(UnicodeSet externalFilter) {
        UnicodeSet temp;
        if (this.filter == null) {
            return externalFilter;
        }
        UnicodeSet filterSet = new UnicodeSet(externalFilter);
        try {
            temp = this.filter;
        } catch (ClassCastException e) {
            UnicodeSet unicodeSet = this.filter;
            UnicodeSet temp2 = new UnicodeSet();
            unicodeSet.addMatchSetTo(temp2);
            temp = temp2;
        }
        return filterSet.retainAll(temp).freeze();
    }

    public final Transliterator getInverse() {
        return getInstance(this.ID, 1);
    }

    public static void registerClass(String ID2, Class<? extends Transliterator> transClass, String displayName) {
        registry.put(ID2, transClass, true);
        if (displayName != null) {
            displayNameCache.put(new CaseInsensitiveString(ID2), displayName);
        }
    }

    public static void registerFactory(String ID2, Factory factory) {
        registry.put(ID2, factory, true);
    }

    public static void registerInstance(Transliterator trans) {
        registry.put(trans.getID(), trans, true);
    }

    static void registerInstance(Transliterator trans, boolean visible) {
        registry.put(trans.getID(), trans, visible);
    }

    public static void registerAlias(String aliasID, String realID) {
        registry.put(aliasID, realID, true);
    }

    static void registerSpecialInverse(String target, String inverseTarget, boolean bidirectional) {
        TransliteratorIDParser.registerSpecialInverse(target, inverseTarget, bidirectional);
    }

    public static void unregister(String ID2) {
        displayNameCache.remove(new CaseInsensitiveString(ID2));
        registry.remove(ID2);
    }

    public static final Enumeration<String> getAvailableIDs() {
        return registry.getAvailableIDs();
    }

    public static final Enumeration<String> getAvailableSources() {
        return registry.getAvailableSources();
    }

    public static final Enumeration<String> getAvailableTargets(String source) {
        return registry.getAvailableTargets(source);
    }

    public static final Enumeration<String> getAvailableVariants(String source, String target) {
        return registry.getAvailableVariants(source, target);
    }

    static {
        int dir;
        UResourceBundle transIDs = UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME, ROOT).get(RB_RULE_BASED_IDS);
        int maxRows = transIDs.getSize();
        for (int row = 0; row < maxRows; row++) {
            UResourceBundle colBund = transIDs.get(row);
            String ID2 = colBund.getKey();
            if (ID2.indexOf("-t-") < 0) {
                UResourceBundle res = colBund.get(0);
                String type = res.getKey();
                if (type.equals("file") || type.equals("internal")) {
                    String resString = res.getString("resource");
                    char charAt = res.getString("direction").charAt(0);
                    if (charAt == 'F') {
                        dir = 0;
                    } else if (charAt == 'R') {
                        dir = 1;
                    } else {
                        throw new RuntimeException("Can't parse direction: " + direction);
                    }
                    registry.put(ID2, resString, dir, true ^ type.equals("internal"));
                } else if (type.equals("alias")) {
                    registry.put(ID2, res.getString(), true);
                } else {
                    throw new RuntimeException("Unknow type: " + type);
                }
            }
        }
        registerSpecialInverse("Null", "Null", false);
        registerClass("Any-Null", NullTransliterator.class, null);
        RemoveTransliterator.register();
        EscapeTransliterator.register();
        UnescapeTransliterator.register();
        LowercaseTransliterator.register();
        UppercaseTransliterator.register();
        TitlecaseTransliterator.register();
        CaseFoldTransliterator.register();
        UnicodeNameTransliterator.register();
        NameUnicodeTransliterator.register();
        NormalizationTransliterator.register();
        BreakTransliterator.register();
        AnyTransliterator.register();
    }

    @Deprecated
    public static void registerAny() {
        AnyTransliterator.register();
    }

    public String transform(String source) {
        return transliterate(source);
    }
}
