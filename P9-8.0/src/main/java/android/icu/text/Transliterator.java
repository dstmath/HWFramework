package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Utility;
import android.icu.util.CaseInsensitiveString;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
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
        static final /* synthetic */ boolean -assertionsDisabled = (Position.class.desiredAssertionStatus() ^ 1);
        public int contextLimit;
        public int contextStart;
        public int limit;
        public int start;

        public Position() {
            this(0, 0, 0, 0);
        }

        public Position(int contextStart, int contextLimit, int start) {
            this(contextStart, contextLimit, start, contextLimit);
        }

        public Position(int contextStart, int contextLimit, int start, int limit) {
            this.contextStart = contextStart;
            this.contextLimit = contextLimit;
            this.start = start;
            this.limit = limit;
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
            if (-assertionsDisabled) {
                return 42;
            }
            throw new AssertionError("hashCode not designed");
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

    protected abstract void handleTransliterate(Replaceable replaceable, Position position, boolean z);

    protected Transliterator(String ID, UnicodeFilter filter) {
        if (ID == null) {
            throw new NullPointerException();
        }
        this.ID = ID;
        setFilter(filter);
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
        Replaceable result = new ReplaceableString(text);
        transliterate(result);
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
        transliterate(text, index, null);
    }

    public final void finishTransliteration(Replaceable text, Position index) {
        index.validate(text.length());
        filteredTransliterate(text, index, false, true);
    }

    private void filteredTransliterate(Replaceable text, Position index, boolean incremental, boolean rollback) {
        if (this.filter != null || (rollback ^ 1) == 0) {
            int globalLimit = index.limit;
            boolean isIncrementalRun;
            do {
                if (this.filter != null) {
                    UnicodeSet unicodeSet;
                    int c;
                    while (index.start < globalLimit) {
                        unicodeSet = this.filter;
                        c = text.char32At(index.start);
                        if ((unicodeSet.contains(c) ^ 1) == 0) {
                            break;
                        }
                        index.start += UTF16.getCharCount(c);
                    }
                    index.limit = index.start;
                    while (index.limit < globalLimit) {
                        unicodeSet = this.filter;
                        c = text.char32At(index.limit);
                        if (!unicodeSet.contains(c)) {
                            break;
                        }
                        index.limit += UTF16.getCharCount(c);
                    }
                }
                if (index.start == index.limit) {
                    break;
                }
                if (index.limit < globalLimit) {
                    isIncrementalRun = false;
                } else {
                    isIncrementalRun = incremental;
                }
                int delta;
                if (rollback && isIncrementalRun) {
                    int runStart = index.start;
                    int runLimit = index.limit;
                    int runLength = runLimit - runStart;
                    int rollbackOrigin = text.length();
                    text.copy(runStart, runLimit, rollbackOrigin);
                    int passStart = runStart;
                    int rollbackStart = rollbackOrigin;
                    int passLimit = index.start;
                    int uncommittedLength = 0;
                    int totalDelta = 0;
                    while (true) {
                        int charLength = UTF16.getCharCount(text.char32At(passLimit));
                        passLimit += charLength;
                        if (passLimit > runLimit) {
                            break;
                        }
                        uncommittedLength += charLength;
                        index.limit = passLimit;
                        handleTransliterate(text, index, true);
                        delta = index.limit - passLimit;
                        if (index.start != index.limit) {
                            int rs = (rollbackStart + delta) - (index.limit - passStart);
                            text.replace(passStart, index.limit, "");
                            text.copy(rs, rs + uncommittedLength, passStart);
                            index.start = passStart;
                            index.limit = passLimit;
                            index.contextLimit -= delta;
                        } else {
                            passLimit = index.start;
                            passStart = passLimit;
                            rollbackStart += delta + uncommittedLength;
                            uncommittedLength = 0;
                            runLimit += delta;
                            totalDelta += delta;
                        }
                    }
                    rollbackOrigin += totalDelta;
                    globalLimit += totalDelta;
                    text.replace(rollbackOrigin, rollbackOrigin + runLength, "");
                    index.start = passStart;
                } else {
                    int limit = index.limit;
                    handleTransliterate(text, index, isIncrementalRun);
                    delta = index.limit - limit;
                    if (isIncrementalRun || index.start == index.limit) {
                        globalLimit += delta;
                    } else {
                        throw new RuntimeException("ERROR: Incomplete non-incremental transliteration by " + getID());
                    }
                }
                if (this.filter == null) {
                    break;
                }
            } while (!isIncrementalRun);
            index.limit = globalLimit;
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

    protected void setMaximumContextLength(int a) {
        if (a < 0) {
            throw new IllegalArgumentException("Invalid context length " + a);
        }
        this.maximumContextLength = a;
    }

    public final String getID() {
        return this.ID;
    }

    protected final void setID(String id) {
        this.ID = id;
    }

    public static final String getDisplayName(String ID) {
        return getDisplayName(ID, ULocale.getDefault(Category.DISPLAY));
    }

    public static String getDisplayName(String id, Locale inLocale) {
        return getDisplayName(id, ULocale.forLocale(inLocale));
    }

    public static String getDisplayName(String id, ULocale inLocale) {
        ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME, inLocale);
        String[] stv = TransliteratorIDParser.IDtoSTV(id);
        if (stv == null) {
            return "";
        }
        String ID = stv[0] + ID_SEP + stv[1];
        if (stv[2] != null && stv[2].length() > 0) {
            ID = ID + VARIANT_SEP + stv[2];
        }
        String n = (String) displayNameCache.get(new CaseInsensitiveString(ID));
        if (n != null) {
            return n;
        }
        try {
            return bundle.getString(RB_DISPLAY_NAME_PREFIX + ID);
        } catch (MissingResourceException e) {
            try {
                String str;
                MessageFormat format = new MessageFormat(bundle.getString(RB_DISPLAY_NAME_PATTERN));
                Object[] args = new Object[]{Integer.valueOf(2), stv[0], stv[1]};
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

    public void setFilter(UnicodeFilter filter) {
        if (filter == null) {
            this.filter = null;
            return;
        }
        try {
            this.filter = new UnicodeSet((UnicodeSet) filter).freeze();
        } catch (Exception e) {
            this.filter = new UnicodeSet();
            filter.addMatchSetTo(this.filter);
            this.filter.freeze();
        }
    }

    public static final Transliterator getInstance(String ID) {
        return getInstance(ID, 0);
    }

    public static Transliterator getInstance(String ID, int dir) {
        StringBuffer canonID = new StringBuffer();
        List<SingleID> list = new ArrayList();
        UnicodeSet[] globalFilter = new UnicodeSet[1];
        if (TransliteratorIDParser.parseCompoundID(ID, dir, canonID, list, globalFilter)) {
            Transliterator t;
            List<Transliterator> translits = TransliteratorIDParser.instantiateList(list);
            if (list.size() > 1 || canonID.indexOf(";") >= 0) {
                t = new CompoundTransliterator(translits);
            } else {
                t = (Transliterator) translits.get(0);
            }
            t.setID(canonID.toString());
            if (globalFilter[0] != null) {
                t.setFilter(globalFilter[0]);
            }
            return t;
        }
        throw new IllegalArgumentException("Invalid ID " + ID);
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

    public static final Transliterator createFromRules(String ID, String rules, int dir) {
        TransliteratorParser parser = new TransliteratorParser();
        parser.parse(rules, dir);
        if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == 0) {
            return new NullTransliterator();
        }
        if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == 1) {
            return new RuleBasedTransliterator(ID, (Data) parser.dataVector.get(0), parser.compoundFilter);
        }
        Transliterator t;
        if (parser.idBlockVector.size() == 1 && parser.dataVector.size() == 0) {
            if (parser.compoundFilter != null) {
                t = getInstance(parser.compoundFilter.toPattern(false) + ";" + ((String) parser.idBlockVector.get(0)));
            } else {
                t = getInstance((String) parser.idBlockVector.get(0));
            }
            if (t == null) {
                return t;
            }
            t.setID(ID);
            return t;
        }
        List<Transliterator> transliterators = new ArrayList();
        int limit = Math.max(parser.idBlockVector.size(), parser.dataVector.size());
        int i = 0;
        int passNumber = 1;
        while (i < limit) {
            int passNumber2;
            if (i < parser.idBlockVector.size()) {
                String idBlock = (String) parser.idBlockVector.get(i);
                if (idBlock.length() > 0 && !(getInstance(idBlock) instanceof NullTransliterator)) {
                    transliterators.add(getInstance(idBlock));
                }
            }
            if (i < parser.dataVector.size()) {
                passNumber2 = passNumber + 1;
                transliterators.add(new RuleBasedTransliterator("%Pass" + passNumber, (Data) parser.dataVector.get(i), null));
            } else {
                passNumber2 = passNumber;
            }
            i++;
            passNumber = passNumber2;
        }
        t = new CompoundTransliterator(transliterators, passNumber - 1);
        t.setID(ID);
        if (parser.compoundFilter == null) {
            return t;
        }
        t.setFilter(parser.compoundFilter);
        return t;
    }

    public String toRules(boolean escapeUnprintable) {
        return baseToRules(escapeUnprintable);
    }

    protected final String baseToRules(boolean escapeUnprintable) {
        if (!escapeUnprintable) {
            return "::" + getID() + ID_DELIM;
        }
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

    public Transliterator[] getElements() {
        if (this instanceof CompoundTransliterator) {
            CompoundTransliterator cpd = (CompoundTransliterator) this;
            Transliterator[] result = new Transliterator[cpd.getCount()];
            for (int i = 0; i < result.length; i++) {
                result[i] = cpd.getTransliterator(i);
            }
            return result;
        }
        return new Transliterator[]{this};
    }

    public final UnicodeSet getSourceSet() {
        UnicodeSet result = new UnicodeSet();
        addSourceTargetSet(getFilterAsUnicodeSet(UnicodeSet.ALL_CODE_POINTS), result, new UnicodeSet());
        return result;
    }

    protected UnicodeSet handleGetSourceSet() {
        return new UnicodeSet();
    }

    public UnicodeSet getTargetSet() {
        UnicodeSet result = new UnicodeSet();
        addSourceTargetSet(getFilterAsUnicodeSet(UnicodeSet.ALL_CODE_POINTS), new UnicodeSet(), result);
        return result;
    }

    @Deprecated
    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet<String> temp = new UnicodeSet(handleGetSourceSet()).retainAll(getFilterAsUnicodeSet(inputFilter));
        sourceSet.addAll((UnicodeSet) temp);
        for (String s : temp) {
            CharSequence t = transliterate(s);
            if (!s.equals(t)) {
                targetSet.addAll(t);
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
            temp = new UnicodeSet();
            unicodeSet.addMatchSetTo(temp);
        }
        return filterSet.retainAll(temp).freeze();
    }

    public final Transliterator getInverse() {
        return getInstance(this.ID, 1);
    }

    public static void registerClass(String ID, Class<? extends Transliterator> transClass, String displayName) {
        registry.put(ID, (Class) transClass, true);
        if (displayName != null) {
            displayNameCache.put(new CaseInsensitiveString(ID), displayName);
        }
    }

    public static void registerFactory(String ID, Factory factory) {
        registry.put(ID, factory, true);
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

    public static void unregister(String ID) {
        displayNameCache.remove(new CaseInsensitiveString(ID));
        registry.remove(ID);
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
        UResourceBundle transIDs = UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME, ROOT).get(RB_RULE_BASED_IDS);
        int maxRows = transIDs.getSize();
        for (int row = 0; row < maxRows; row++) {
            UResourceBundle colBund = transIDs.get(row);
            String ID = colBund.getKey();
            if (ID.indexOf("-t-") < 0) {
                UResourceBundle res = colBund.get(0);
                String type = res.getKey();
                if (type.equals("file") || type.equals("internal")) {
                    int dir;
                    String resString = res.getString("resource");
                    String direction = res.getString("direction");
                    switch (direction.charAt(0)) {
                        case 'F':
                            dir = 0;
                            break;
                        case 'R':
                            dir = 1;
                            break;
                        default:
                            throw new RuntimeException("Can't parse direction: " + direction);
                    }
                    registry.put(ID, resString, dir, type.equals("internal") ^ 1);
                } else if (type.equals("alias")) {
                    registry.put(ID, res.getString(), true);
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
