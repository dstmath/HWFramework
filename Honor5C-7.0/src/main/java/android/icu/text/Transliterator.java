package android.icu.text;

import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Utility;
import android.icu.util.CaseInsensitiveString;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import org.xmlpull.v1.XmlPullParser;

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
    private static Map<CaseInsensitiveString, String> displayNameCache;
    private static TransliteratorRegistry registry;
    private String ID;
    private UnicodeSet filter;
    private int maximumContextLength;

    public interface Factory {
        Transliterator getInstance(String str);
    }

    public static class Position {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        public int contextLimit;
        public int contextStart;
        public int limit;
        public int start;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Transliterator.Position.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Transliterator.Position.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Transliterator.Position.<clinit>():void");
        }

        public Position() {
            this(Transliterator.FORWARD, Transliterator.FORWARD, Transliterator.FORWARD, Transliterator.FORWARD);
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
            boolean z = Transliterator.DEBUG;
            if (!(obj instanceof Position)) {
                return Transliterator.DEBUG;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Transliterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Transliterator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Transliterator.<clinit>():void");
    }

    protected abstract void handleTransliterate(Replaceable replaceable, Position position, boolean z);

    protected Transliterator(String ID, UnicodeFilter filter) {
        this.maximumContextLength = FORWARD;
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
        filteredTransliterate(text, pos, DEBUG, true);
        return pos.limit;
    }

    public final void transliterate(Replaceable text) {
        transliterate(text, (int) FORWARD, text.length());
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
        filteredTransliterate(text, index, DEBUG, true);
    }

    private void filteredTransliterate(Replaceable text, Position index, boolean incremental, boolean rollback) {
        if (this.filter != null || rollback) {
            int globalLimit = index.limit;
            boolean z;
            do {
                int i;
                if (this.filter != null) {
                    UnicodeSet unicodeSet;
                    int c;
                    while (true) {
                        i = index.start;
                        if (r0 >= globalLimit) {
                            break;
                        }
                        unicodeSet = this.filter;
                        c = text.char32At(index.start);
                        if (unicodeSet.contains(c)) {
                            break;
                        }
                        index.start += UTF16.getCharCount(c);
                    }
                    index.limit = index.start;
                    while (true) {
                        i = index.limit;
                        if (r0 >= globalLimit) {
                            break;
                        }
                        unicodeSet = this.filter;
                        c = text.char32At(index.limit);
                        if (!unicodeSet.contains(c)) {
                            break;
                        }
                        index.limit += UTF16.getCharCount(c);
                    }
                }
                if (index.start != index.limit) {
                    i = index.limit;
                    if (r0 < globalLimit) {
                        z = DEBUG;
                    } else {
                        z = incremental;
                    }
                    int delta;
                    if (rollback && z) {
                        int runStart = index.start;
                        int runLimit = index.limit;
                        int runLength = runLimit - runStart;
                        int rollbackOrigin = text.length();
                        text.copy(runStart, runLimit, rollbackOrigin);
                        int passStart = runStart;
                        int rollbackStart = rollbackOrigin;
                        int passLimit = index.start;
                        int uncommittedLength = FORWARD;
                        int totalDelta = FORWARD;
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
                                text.replace(passStart, index.limit, XmlPullParser.NO_NAMESPACE);
                                text.copy(rs, rs + uncommittedLength, passStart);
                                index.start = passStart;
                                index.limit = passLimit;
                                index.contextLimit -= delta;
                            } else {
                                passLimit = index.start;
                                passStart = passLimit;
                                rollbackStart += delta + uncommittedLength;
                                uncommittedLength = FORWARD;
                                runLimit += delta;
                                totalDelta += delta;
                            }
                        }
                        rollbackOrigin += totalDelta;
                        globalLimit += totalDelta;
                        text.replace(rollbackOrigin, rollbackOrigin + runLength, XmlPullParser.NO_NAMESPACE);
                        index.start = passStart;
                    } else {
                        int limit = index.limit;
                        handleTransliterate(text, index, z);
                        delta = index.limit - limit;
                        if (z || index.start == index.limit) {
                            globalLimit += delta;
                        } else {
                            throw new RuntimeException("ERROR: Incomplete non-incremental transliteration by " + getID());
                        }
                    }
                    if (this.filter == null) {
                        break;
                    }
                } else {
                    break;
                }
            } while (!z);
            index.limit = globalLimit;
            return;
        }
        handleTransliterate(text, index, incremental);
    }

    public void filteredTransliterate(Replaceable text, Position index, boolean incremental) {
        filteredTransliterate(text, index, incremental, DEBUG);
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
        ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_TRANSLIT_BASE_NAME, inLocale);
        String[] stv = TransliteratorIDParser.IDtoSTV(id);
        if (stv == null) {
            return XmlPullParser.NO_NAMESPACE;
        }
        String ID = stv[FORWARD] + ID_SEP + stv[REVERSE];
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
                Object[] args = new Object[]{Integer.valueOf(2), stv[FORWARD], stv[REVERSE]};
                for (int j = REVERSE; j <= 2; j += REVERSE) {
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
        return getInstance(ID, FORWARD);
    }

    public static Transliterator getInstance(String ID, int dir) {
        StringBuffer canonID = new StringBuffer();
        List<SingleID> list = new ArrayList();
        UnicodeSet[] globalFilter = new UnicodeSet[REVERSE];
        if (TransliteratorIDParser.parseCompoundID(ID, dir, canonID, list, globalFilter)) {
            Transliterator t;
            List<Transliterator> translits = TransliteratorIDParser.instantiateList(list);
            if (list.size() > REVERSE || canonID.indexOf(";") >= 0) {
                t = new CompoundTransliterator(translits);
            } else {
                t = (Transliterator) translits.get(FORWARD);
            }
            t.setID(canonID.toString());
            if (globalFilter[FORWARD] != null) {
                t.setFilter(globalFilter[FORWARD]);
            }
            return t;
        }
        throw new IllegalArgumentException("Invalid ID " + ID);
    }

    static Transliterator getBasicInstance(String id, String canonID) {
        StringBuffer s = new StringBuffer();
        Transliterator t = registry.get(id, s);
        if (s.length() != 0) {
            t = getInstance(s.toString(), FORWARD);
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
        if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == REVERSE) {
            return new RuleBasedTransliterator(ID, (Data) parser.dataVector.get(FORWARD), parser.compoundFilter);
        }
        Transliterator t;
        if (parser.idBlockVector.size() == REVERSE && parser.dataVector.size() == 0) {
            if (parser.compoundFilter != null) {
                t = getInstance(parser.compoundFilter.toPattern(DEBUG) + ";" + ((String) parser.idBlockVector.get(FORWARD)));
            } else {
                t = getInstance((String) parser.idBlockVector.get(FORWARD));
            }
            if (t == null) {
                return t;
            }
            t.setID(ID);
            return t;
        }
        List<Transliterator> transliterators = new ArrayList();
        int limit = Math.max(parser.idBlockVector.size(), parser.dataVector.size());
        int i = FORWARD;
        int passNumber = REVERSE;
        while (i < limit) {
            int passNumber2;
            if (i < parser.idBlockVector.size()) {
                String idBlock = (String) parser.idBlockVector.get(i);
                if (idBlock.length() > 0 && !(getInstance(idBlock) instanceof NullTransliterator)) {
                    transliterators.add(getInstance(idBlock));
                }
            }
            if (i < parser.dataVector.size()) {
                passNumber2 = passNumber + REVERSE;
                transliterators.add(new RuleBasedTransliterator("%Pass" + passNumber, (Data) parser.dataVector.get(i), null));
            } else {
                passNumber2 = passNumber;
            }
            i += REVERSE;
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
        int i = FORWARD;
        while (i < id.length()) {
            int c = UTF16.charAt(id, i);
            if (!Utility.escapeUnprintable(rulesSource, c)) {
                UTF16.append(rulesSource, c);
            }
            i += UTF16.getCharCount(c);
        }
        rulesSource.insert(FORWARD, "::");
        rulesSource.append(ID_DELIM);
        return rulesSource.toString();
    }

    public Transliterator[] getElements() {
        Transliterator[] result;
        if (this instanceof CompoundTransliterator) {
            CompoundTransliterator cpd = (CompoundTransliterator) this;
            result = new Transliterator[cpd.getCount()];
            for (int i = FORWARD; i < result.length; i += REVERSE) {
                result[i] = cpd.getTransliterator(i);
            }
            return result;
        }
        result = new Transliterator[REVERSE];
        result[FORWARD] = this;
        return result;
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
        if (this.filter == null) {
            return externalFilter;
        }
        UnicodeSet temp;
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
        return getInstance(this.ID, REVERSE);
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

    @Deprecated
    public static void registerAny() {
        AnyTransliterator.register();
    }

    public /* bridge */ /* synthetic */ Object transform(Object source) {
        return transform((String) source);
    }

    public String transform(String source) {
        return transliterate(source);
    }
}
