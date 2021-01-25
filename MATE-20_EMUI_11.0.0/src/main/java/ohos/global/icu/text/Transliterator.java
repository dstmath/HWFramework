package ohos.global.icu.text;

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
import java.util.Objects;
import ohos.ai.asr.util.AsrConstants;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.util.CaseInsensitiveString;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

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

    /* access modifiers changed from: protected */
    public abstract void handleTransliterate(Replaceable replaceable, Position position, boolean z);

    public static class Position {
        public int contextLimit;
        public int contextStart;
        public int limit;
        public int start;

        public Position() {
            this(0, 0, 0, 0);
        }

        public Position(int i, int i2, int i3) {
            this(i, i2, i3, i2);
        }

        public Position(int i, int i2, int i3, int i4) {
            this.contextStart = i;
            this.contextLimit = i2;
            this.start = i3;
            this.limit = i4;
        }

        public Position(Position position) {
            set(position);
        }

        public void set(Position position) {
            this.contextStart = position.contextStart;
            this.contextLimit = position.contextLimit;
            this.start = position.start;
            this.limit = position.limit;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Position)) {
                return false;
            }
            Position position = (Position) obj;
            if (this.contextStart == position.contextStart && this.contextLimit == position.contextLimit && this.start == position.start && this.limit == position.limit) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.contextStart), Integer.valueOf(this.contextLimit), Integer.valueOf(this.start), Integer.valueOf(this.limit));
        }

        public String toString() {
            return "[cs=" + this.contextStart + ", s=" + this.start + ", l=" + this.limit + ", cl=" + this.contextLimit + "]";
        }

        public final void validate(int i) {
            int i2;
            int i3;
            int i4;
            int i5 = this.contextStart;
            if (i5 < 0 || (i2 = this.start) < i5 || (i3 = this.limit) < i2 || (i4 = this.contextLimit) < i3 || i < i4) {
                throw new IllegalArgumentException("Invalid Position {cs=" + this.contextStart + ", s=" + this.start + ", l=" + this.limit + ", cl=" + this.contextLimit + "}, len=" + i);
            }
        }
    }

    protected Transliterator(String str, UnicodeFilter unicodeFilter) {
        if (str != null) {
            this.ID = str;
            setFilter(unicodeFilter);
            return;
        }
        throw new NullPointerException();
    }

    public final int transliterate(Replaceable replaceable, int i, int i2) {
        if (i < 0 || i2 < i || replaceable.length() < i2) {
            return -1;
        }
        Position position = new Position(i, i2, i);
        filteredTransliterate(replaceable, position, false, true);
        return position.limit;
    }

    public final void transliterate(Replaceable replaceable) {
        transliterate(replaceable, 0, replaceable.length());
    }

    public final String transliterate(String str) {
        ReplaceableString replaceableString = new ReplaceableString(str);
        transliterate(replaceableString);
        return replaceableString.toString();
    }

    public final void transliterate(Replaceable replaceable, Position position, String str) {
        position.validate(replaceable.length());
        if (str != null) {
            replaceable.replace(position.limit, position.limit, str);
            position.limit += str.length();
            position.contextLimit += str.length();
        }
        if (position.limit <= 0 || !UTF16.isLeadSurrogate(replaceable.charAt(position.limit - 1))) {
            filteredTransliterate(replaceable, position, true, true);
        }
    }

    public final void transliterate(Replaceable replaceable, Position position, int i) {
        transliterate(replaceable, position, UTF16.valueOf(i));
    }

    public final void transliterate(Replaceable replaceable, Position position) {
        transliterate(replaceable, position, (String) null);
    }

    public final void finishTransliteration(Replaceable replaceable, Position position) {
        position.validate(replaceable.length());
        filteredTransliterate(replaceable, position, false, true);
    }

    private void filteredTransliterate(Replaceable replaceable, Position position, boolean z, boolean z2) {
        boolean z3;
        if (this.filter != null || z2) {
            int i = position.limit;
            do {
                if (this.filter != null) {
                    while (position.start < i) {
                        UnicodeSet unicodeSet = this.filter;
                        int char32At = replaceable.char32At(position.start);
                        if (unicodeSet.contains(char32At)) {
                            break;
                        }
                        position.start += UTF16.getCharCount(char32At);
                    }
                    position.limit = position.start;
                    while (position.limit < i) {
                        UnicodeSet unicodeSet2 = this.filter;
                        int char32At2 = replaceable.char32At(position.limit);
                        if (!unicodeSet2.contains(char32At2)) {
                            break;
                        }
                        position.limit += UTF16.getCharCount(char32At2);
                    }
                }
                if (position.start == position.limit) {
                    break;
                }
                z3 = position.limit < i ? false : z;
                if (!z2 || !z3) {
                    int i2 = position.limit;
                    handleTransliterate(replaceable, position, z3);
                    int i3 = position.limit - i2;
                    if (z3 || position.start == position.limit) {
                        i += i3;
                    } else {
                        throw new RuntimeException("ERROR: Incomplete non-incremental transliteration by " + getID());
                    }
                } else {
                    int i4 = position.start;
                    int i5 = position.limit;
                    int i6 = i5 - i4;
                    int length = replaceable.length();
                    replaceable.copy(i4, i5, length);
                    int i7 = position.start;
                    int i8 = i4;
                    int i9 = length;
                    int i10 = 0;
                    int i11 = 0;
                    while (true) {
                        int charCount = UTF16.getCharCount(replaceable.char32At(i7));
                        i7 += charCount;
                        if (i7 > i5) {
                            break;
                        }
                        i10 += charCount;
                        position.limit = i7;
                        handleTransliterate(replaceable, position, true);
                        int i12 = position.limit - i7;
                        if (position.start != position.limit) {
                            int i13 = (i9 + i12) - (position.limit - i8);
                            replaceable.replace(i8, position.limit, "");
                            replaceable.copy(i13, i13 + i10, i8);
                            position.start = i8;
                            position.limit = i7;
                            position.contextLimit -= i12;
                        } else {
                            i9 += i10 + i12;
                            i5 += i12;
                            i11 += i12;
                            i7 = position.start;
                            i8 = i7;
                            i10 = 0;
                        }
                        i6 = i6;
                    }
                    int i14 = length + i11;
                    i += i11;
                    replaceable.replace(i14, i6 + i14, "");
                    position.start = i8;
                }
                if (this.filter == null) {
                    break;
                }
            } while (!z3);
            position.limit = i;
            return;
        }
        handleTransliterate(replaceable, position, z);
    }

    public void filteredTransliterate(Replaceable replaceable, Position position, boolean z) {
        filteredTransliterate(replaceable, position, z, false);
    }

    public final int getMaximumContextLength() {
        return this.maximumContextLength;
    }

    /* access modifiers changed from: protected */
    public void setMaximumContextLength(int i) {
        if (i >= 0) {
            this.maximumContextLength = i;
            return;
        }
        throw new IllegalArgumentException("Invalid context length " + i);
    }

    public final String getID() {
        return this.ID;
    }

    /* access modifiers changed from: protected */
    public final void setID(String str) {
        this.ID = str;
    }

    public static final String getDisplayName(String str) {
        return getDisplayName(str, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public static String getDisplayName(String str, Locale locale) {
        return getDisplayName(str, ULocale.forLocale(locale));
    }

    public static String getDisplayName(String str, ULocale uLocale) {
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME, uLocale);
        String[] IDtoSTV = TransliteratorIDParser.IDtoSTV(str);
        if (IDtoSTV == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(IDtoSTV[0]);
        sb.append('-');
        sb.append(IDtoSTV[1]);
        String sb2 = sb.toString();
        if (IDtoSTV[2] != null && IDtoSTV[2].length() > 0) {
            sb2 = sb2 + VARIANT_SEP + IDtoSTV[2];
        }
        String str2 = displayNameCache.get(new CaseInsensitiveString(sb2));
        if (str2 != null) {
            return str2;
        }
        try {
            return bundleInstance.getString(RB_DISPLAY_NAME_PREFIX + sb2);
        } catch (MissingResourceException unused) {
            try {
                MessageFormat messageFormat = new MessageFormat(bundleInstance.getString(RB_DISPLAY_NAME_PATTERN));
                Object[] objArr = {2, IDtoSTV[0], IDtoSTV[1]};
                for (int i = 1; i <= 2; i++) {
                    try {
                        objArr[i] = bundleInstance.getString(RB_SCRIPT_DISPLAY_NAME_PREFIX + ((String) objArr[i]));
                    } catch (MissingResourceException unused2) {
                    }
                }
                if (IDtoSTV[2].length() <= 0) {
                    return messageFormat.format(objArr);
                }
                return messageFormat.format(objArr) + VARIANT_SEP + IDtoSTV[2];
            } catch (MissingResourceException unused3) {
                throw new RuntimeException();
            }
        }
    }

    public final UnicodeFilter getFilter() {
        return this.filter;
    }

    public void setFilter(UnicodeFilter unicodeFilter) {
        if (unicodeFilter == null) {
            this.filter = null;
            return;
        }
        try {
            this.filter = new UnicodeSet((UnicodeSet) unicodeFilter).freeze();
        } catch (Exception unused) {
            this.filter = new UnicodeSet();
            unicodeFilter.addMatchSetTo(this.filter);
            this.filter.freeze();
        }
    }

    public static final Transliterator getInstance(String str) {
        return getInstance(str, 0);
    }

    public static Transliterator getInstance(String str, int i) {
        Transliterator transliterator;
        StringBuffer stringBuffer = new StringBuffer();
        ArrayList arrayList = new ArrayList();
        UnicodeSet[] unicodeSetArr = new UnicodeSet[1];
        if (TransliteratorIDParser.parseCompoundID(str, i, stringBuffer, arrayList, unicodeSetArr)) {
            List<Transliterator> instantiateList = TransliteratorIDParser.instantiateList(arrayList);
            if (arrayList.size() > 1 || stringBuffer.indexOf(";") >= 0) {
                transliterator = new CompoundTransliterator(instantiateList);
            } else {
                transliterator = instantiateList.get(0);
            }
            transliterator.setID(stringBuffer.toString());
            if (unicodeSetArr[0] != null) {
                transliterator.setFilter(unicodeSetArr[0]);
            }
            return transliterator;
        }
        throw new IllegalArgumentException("Invalid ID " + str);
    }

    static Transliterator getBasicInstance(String str, String str2) {
        StringBuffer stringBuffer = new StringBuffer();
        Transliterator transliterator = registry.get(str, stringBuffer);
        if (stringBuffer.length() != 0) {
            transliterator = getInstance(stringBuffer.toString(), 0);
        }
        if (!(transliterator == null || str2 == null)) {
            transliterator.setID(str2);
        }
        return transliterator;
    }

    public static final Transliterator createFromRules(String str, String str2, int i) {
        Transliterator transliterator;
        TransliteratorParser transliteratorParser = new TransliteratorParser();
        transliteratorParser.parse(str2, i);
        if (transliteratorParser.idBlockVector.size() == 0 && transliteratorParser.dataVector.size() == 0) {
            return new NullTransliterator();
        }
        if (transliteratorParser.idBlockVector.size() == 0 && transliteratorParser.dataVector.size() == 1) {
            return new RuleBasedTransliterator(str, transliteratorParser.dataVector.get(0), transliteratorParser.compoundFilter);
        }
        if (transliteratorParser.idBlockVector.size() == 1 && transliteratorParser.dataVector.size() == 0) {
            if (transliteratorParser.compoundFilter != null) {
                transliterator = getInstance(transliteratorParser.compoundFilter.toPattern(false) + ";" + transliteratorParser.idBlockVector.get(0));
            } else {
                transliterator = getInstance(transliteratorParser.idBlockVector.get(0));
            }
            if (transliterator == null) {
                return transliterator;
            }
            transliterator.setID(str);
            return transliterator;
        }
        ArrayList arrayList = new ArrayList();
        int max = Math.max(transliteratorParser.idBlockVector.size(), transliteratorParser.dataVector.size());
        int i2 = 1;
        for (int i3 = 0; i3 < max; i3++) {
            if (i3 < transliteratorParser.idBlockVector.size()) {
                String str3 = transliteratorParser.idBlockVector.get(i3);
                if (str3.length() > 0 && !(getInstance(str3) instanceof NullTransliterator)) {
                    arrayList.add(getInstance(str3));
                }
            }
            if (i3 < transliteratorParser.dataVector.size()) {
                StringBuilder sb = new StringBuilder();
                sb.append("%Pass");
                sb.append(i2);
                arrayList.add(new RuleBasedTransliterator(sb.toString(), transliteratorParser.dataVector.get(i3), null));
                i2++;
            }
        }
        CompoundTransliterator compoundTransliterator = new CompoundTransliterator(arrayList, i2 - 1);
        compoundTransliterator.setID(str);
        if (transliteratorParser.compoundFilter != null) {
            compoundTransliterator.setFilter(transliteratorParser.compoundFilter);
        }
        return compoundTransliterator;
    }

    public String toRules(boolean z) {
        return baseToRules(z);
    }

    /* access modifiers changed from: protected */
    public final String baseToRules(boolean z) {
        if (z) {
            StringBuffer stringBuffer = new StringBuffer();
            String id = getID();
            int i = 0;
            while (i < id.length()) {
                int charAt = UTF16.charAt(id, i);
                if (!Utility.escapeUnprintable(stringBuffer, charAt)) {
                    UTF16.append(stringBuffer, charAt);
                }
                i += UTF16.getCharCount(charAt);
            }
            stringBuffer.insert(0, "::");
            stringBuffer.append(ID_DELIM);
            return stringBuffer.toString();
        }
        return "::" + getID() + ID_DELIM;
    }

    public Transliterator[] getElements() {
        if (!(this instanceof CompoundTransliterator)) {
            return new Transliterator[]{this};
        }
        CompoundTransliterator compoundTransliterator = (CompoundTransliterator) this;
        Transliterator[] transliteratorArr = new Transliterator[compoundTransliterator.getCount()];
        for (int i = 0; i < transliteratorArr.length; i++) {
            transliteratorArr[i] = compoundTransliterator.getTransliterator(i);
        }
        return transliteratorArr;
    }

    public final UnicodeSet getSourceSet() {
        UnicodeSet unicodeSet = new UnicodeSet();
        addSourceTargetSet(getFilterAsUnicodeSet(UnicodeSet.ALL_CODE_POINTS), unicodeSet, new UnicodeSet());
        return unicodeSet;
    }

    /* access modifiers changed from: protected */
    public UnicodeSet handleGetSourceSet() {
        return new UnicodeSet();
    }

    public UnicodeSet getTargetSet() {
        UnicodeSet unicodeSet = new UnicodeSet();
        addSourceTargetSet(getFilterAsUnicodeSet(UnicodeSet.ALL_CODE_POINTS), new UnicodeSet(), unicodeSet);
        return unicodeSet;
    }

    @Deprecated
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        UnicodeSet retainAll = new UnicodeSet(handleGetSourceSet()).retainAll(getFilterAsUnicodeSet(unicodeSet));
        unicodeSet2.addAll(retainAll);
        Iterator<String> it = retainAll.iterator();
        while (it.hasNext()) {
            String next = it.next();
            String transliterate = transliterate(next);
            if (!next.equals(transliterate)) {
                unicodeSet3.addAll(transliterate);
            }
        }
    }

    @Deprecated
    public UnicodeSet getFilterAsUnicodeSet(UnicodeSet unicodeSet) {
        UnicodeSet unicodeSet2;
        if (this.filter == null) {
            return unicodeSet;
        }
        UnicodeSet unicodeSet3 = new UnicodeSet(unicodeSet);
        try {
            unicodeSet2 = this.filter;
        } catch (ClassCastException unused) {
            UnicodeSet unicodeSet4 = this.filter;
            UnicodeSet unicodeSet5 = new UnicodeSet();
            unicodeSet4.addMatchSetTo(unicodeSet5);
            unicodeSet2 = unicodeSet5;
        }
        return unicodeSet3.retainAll(unicodeSet2).freeze();
    }

    public final Transliterator getInverse() {
        return getInstance(this.ID, 1);
    }

    public static void registerClass(String str, Class<? extends Transliterator> cls, String str2) {
        registry.put(str, cls, true);
        if (str2 != null) {
            displayNameCache.put(new CaseInsensitiveString(str), str2);
        }
    }

    public static void registerFactory(String str, Factory factory) {
        registry.put(str, factory, true);
    }

    public static void registerInstance(Transliterator transliterator) {
        registry.put(transliterator.getID(), transliterator, true);
    }

    static void registerInstance(Transliterator transliterator, boolean z) {
        registry.put(transliterator.getID(), transliterator, z);
    }

    public static void registerAlias(String str, String str2) {
        registry.put(str, str2, true);
    }

    static void registerSpecialInverse(String str, String str2, boolean z) {
        TransliteratorIDParser.registerSpecialInverse(str, str2, z);
    }

    public static void unregister(String str) {
        displayNameCache.remove(new CaseInsensitiveString(str));
        registry.remove(str);
    }

    public static final Enumeration<String> getAvailableIDs() {
        return registry.getAvailableIDs();
    }

    public static final Enumeration<String> getAvailableSources() {
        return registry.getAvailableSources();
    }

    public static final Enumeration<String> getAvailableTargets(String str) {
        return registry.getAvailableTargets(str);
    }

    public static final Enumeration<String> getAvailableVariants(String str, String str2) {
        return registry.getAvailableVariants(str, str2);
    }

    static {
        int i;
        UResourceBundle uResourceBundle = UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME, "root").get(RB_RULE_BASED_IDS);
        int size = uResourceBundle.getSize();
        for (int i2 = 0; i2 < size; i2++) {
            UResourceBundle uResourceBundle2 = uResourceBundle.get(i2);
            String key = uResourceBundle2.getKey();
            if (key.indexOf("-t-") < 0) {
                UResourceBundle uResourceBundle3 = uResourceBundle2.get(0);
                String key2 = uResourceBundle3.getKey();
                if (key2.equals(AsrConstants.ASR_SRC_FILE) || key2.equals("internal")) {
                    String string = uResourceBundle3.getString("resource");
                    String string2 = uResourceBundle3.getString("direction");
                    char charAt = string2.charAt(0);
                    if (charAt == 'F') {
                        i = 0;
                    } else if (charAt == 'R') {
                        i = 1;
                    } else {
                        throw new RuntimeException("Can't parse direction: " + string2);
                    }
                    registry.put(key, string, i, !key2.equals("internal"));
                } else if (key2.equals("alias")) {
                    registry.put(key, uResourceBundle3.getString(), true);
                } else {
                    throw new RuntimeException("Unknow type: " + key2);
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

    @Override // ohos.global.icu.text.StringTransform
    public String transform(String str) {
        return transliterate(str);
    }
}
