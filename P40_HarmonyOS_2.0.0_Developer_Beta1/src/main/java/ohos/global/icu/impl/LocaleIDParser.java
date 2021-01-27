package ohos.global.icu.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import ohos.global.icu.impl.locale.AsciiUtil;

public final class LocaleIDParser {
    private static final char COMMA = ',';
    private static final char DONE = 65535;
    private static final char DOT = '.';
    private static final char HYPHEN = '-';
    private static final char ITEM_SEPARATOR = ';';
    private static final char KEYWORD_ASSIGN = '=';
    private static final char KEYWORD_SEPARATOR = '@';
    private static final char UNDERSCORE = '_';
    String baseName;
    private StringBuilder buffer;
    private boolean canonicalize;
    private boolean hadCountry;
    private char[] id;
    private int index;
    Map<String, String> keywords;

    private static boolean isDoneOrItemSeparator(char c) {
        return c == 65535 || c == ';';
    }

    private static boolean isDoneOrKeywordAssign(char c) {
        return c == 65535 || c == '=';
    }

    private boolean isTerminator(char c) {
        return c == '@' || c == 65535 || c == '.';
    }

    public LocaleIDParser(String str) {
        this(str, false);
    }

    public LocaleIDParser(String str, boolean z) {
        this.id = str.toCharArray();
        this.index = 0;
        this.buffer = new StringBuilder(this.id.length + 5);
        this.canonicalize = z;
    }

    private void reset() {
        this.index = 0;
        this.buffer = new StringBuilder(this.id.length + 5);
    }

    private void append(char c) {
        this.buffer.append(c);
    }

    private void addSeparator() {
        append(UNDERSCORE);
    }

    private String getString(int i) {
        return this.buffer.substring(i);
    }

    private void set(int i, String str) {
        StringBuilder sb = this.buffer;
        sb.delete(i, sb.length());
        this.buffer.insert(i, str);
    }

    private void append(String str) {
        this.buffer.append(str);
    }

    private char next() {
        int i = this.index;
        char[] cArr = this.id;
        if (i == cArr.length) {
            this.index = i + 1;
            return DONE;
        }
        this.index = i + 1;
        return cArr[i];
    }

    private void skipUntilTerminatorOrIDSeparator() {
        do {
        } while (!isTerminatorOrIDSeparator(next()));
        this.index--;
    }

    private boolean atTerminator() {
        int i = this.index;
        char[] cArr = this.id;
        return i >= cArr.length || isTerminator(cArr[i]);
    }

    private boolean isTerminatorOrIDSeparator(char c) {
        return c == '_' || c == '-' || isTerminator(c);
    }

    private boolean haveExperimentalLanguagePrefix() {
        char c;
        char[] cArr = this.id;
        if (cArr.length <= 2 || ((c = cArr[1]) != '-' && c != '_')) {
            return false;
        }
        char c2 = this.id[0];
        return c2 == 'x' || c2 == 'X' || c2 == 'i' || c2 == 'I';
    }

    private boolean haveKeywordAssign() {
        int i = this.index;
        while (true) {
            char[] cArr = this.id;
            if (i >= cArr.length) {
                return false;
            }
            if (cArr[i] == '=') {
                return true;
            }
            i++;
        }
    }

    private int parseLanguage() {
        String threeToTwoLetterLanguage;
        int length = this.buffer.length();
        if (haveExperimentalLanguagePrefix()) {
            append(AsciiUtil.toLower(this.id[0]));
            append('-');
            this.index = 2;
        }
        while (true) {
            char next = next();
            if (isTerminatorOrIDSeparator(next)) {
                break;
            }
            append(AsciiUtil.toLower(next));
        }
        this.index--;
        if (this.buffer.length() - length == 3 && (threeToTwoLetterLanguage = LocaleIDs.threeToTwoLetterLanguage(getString(0))) != null) {
            set(0, threeToTwoLetterLanguage);
        }
        return 0;
    }

    private void skipLanguage() {
        if (haveExperimentalLanguagePrefix()) {
            this.index = 2;
        }
        skipUntilTerminatorOrIDSeparator();
    }

    private int parseScript() {
        if (atTerminator()) {
            return this.buffer.length();
        }
        int i = this.index;
        this.index = i + 1;
        int length = this.buffer.length();
        boolean z = true;
        while (true) {
            char next = next();
            if (isTerminatorOrIDSeparator(next) || !AsciiUtil.isAlpha(next)) {
                break;
            } else if (z) {
                addSeparator();
                append(AsciiUtil.toUpper(next));
                z = false;
            } else {
                append(AsciiUtil.toLower(next));
            }
        }
        this.index--;
        if (this.index - i == 5) {
            return length + 1;
        }
        this.index = i;
        StringBuilder sb = this.buffer;
        sb.delete(length, sb.length());
        return length;
    }

    private void skipScript() {
        char next;
        if (!atTerminator()) {
            int i = this.index;
            this.index = i + 1;
            do {
                next = next();
                if (isTerminatorOrIDSeparator(next)) {
                    break;
                }
            } while (AsciiUtil.isAlpha(next));
            this.index--;
            if (this.index - i != 5) {
                this.index = i;
            }
        }
    }

    private int parseCountry() {
        String threeToTwoLetterRegion;
        if (atTerminator()) {
            return this.buffer.length();
        }
        int i = this.index;
        this.index = i + 1;
        int length = this.buffer.length();
        boolean z = true;
        while (true) {
            char next = next();
            if (isTerminatorOrIDSeparator(next)) {
                break;
            }
            if (z) {
                this.hadCountry = true;
                addSeparator();
                length++;
                z = false;
            }
            append(AsciiUtil.toUpper(next));
        }
        this.index--;
        int length2 = this.buffer.length() - length;
        if (length2 == 0) {
            return length;
        }
        if (length2 < 2 || length2 > 3) {
            this.index = i;
            int i2 = length - 1;
            StringBuilder sb = this.buffer;
            sb.delete(i2, sb.length());
            this.hadCountry = false;
            return i2;
        } else if (length2 != 3 || (threeToTwoLetterRegion = LocaleIDs.threeToTwoLetterRegion(getString(length))) == null) {
            return length;
        } else {
            set(length, threeToTwoLetterRegion);
            return length;
        }
    }

    private void skipCountry() {
        if (!atTerminator()) {
            char[] cArr = this.id;
            int i = this.index;
            if (cArr[i] == '_' || cArr[i] == '-') {
                this.index++;
            }
            int i2 = this.index;
            skipUntilTerminatorOrIDSeparator();
            int i3 = this.index - i2;
            if (i3 < 2 || i3 > 3) {
                this.index = i2;
            }
        }
    }

    private int parseVariant() {
        int length = this.buffer.length();
        boolean z = false;
        boolean z2 = true;
        boolean z3 = true;
        boolean z4 = true;
        while (true) {
            char next = next();
            if (next == 65535) {
                break;
            } else if (next == '.') {
                z2 = false;
                z = true;
            } else if (next == '@') {
                if (haveKeywordAssign()) {
                    break;
                }
                z2 = false;
                z = false;
                z3 = true;
            } else if (z2) {
                if (!(next == '_' || next == '-')) {
                    this.index--;
                }
                z2 = false;
            } else if (!z) {
                if (z3) {
                    if (z4 && !this.hadCountry) {
                        addSeparator();
                        length++;
                    }
                    addSeparator();
                    if (z4) {
                        length++;
                        z3 = false;
                        z4 = false;
                    } else {
                        z3 = false;
                    }
                }
                char upper = AsciiUtil.toUpper(next);
                if (upper == '-' || upper == ',') {
                    upper = '_';
                }
                append(upper);
            }
        }
        this.index--;
        return length;
    }

    public String getLanguage() {
        reset();
        return getString(parseLanguage());
    }

    public String getScript() {
        reset();
        skipLanguage();
        return getString(parseScript());
    }

    public String getCountry() {
        reset();
        skipLanguage();
        skipScript();
        return getString(parseCountry());
    }

    public String getVariant() {
        reset();
        skipLanguage();
        skipScript();
        skipCountry();
        return getString(parseVariant());
    }

    public String[] getLanguageScriptCountryVariant() {
        reset();
        return new String[]{getString(parseLanguage()), getString(parseScript()), getString(parseCountry()), getString(parseVariant())};
    }

    public void setBaseName(String str) {
        this.baseName = str;
    }

    public void parseBaseName() {
        String str = this.baseName;
        if (str != null) {
            set(0, str);
            return;
        }
        reset();
        parseLanguage();
        parseScript();
        parseCountry();
        parseVariant();
        int length = this.buffer.length();
        if (length > 0) {
            int i = length - 1;
            if (this.buffer.charAt(i) == '_') {
                this.buffer.deleteCharAt(i);
            }
        }
    }

    public String getBaseName() {
        String str = this.baseName;
        if (str != null) {
            return str;
        }
        parseBaseName();
        return getString(0);
    }

    public String getName() {
        parseBaseName();
        parseKeywords();
        return getString(0);
    }

    private boolean setToKeywordStart() {
        int i = this.index;
        while (true) {
            char[] cArr = this.id;
            if (i >= cArr.length) {
                return false;
            }
            if (cArr[i] != '@') {
                i++;
            } else if (this.canonicalize) {
                int i2 = i + 1;
                int i3 = i2;
                while (true) {
                    char[] cArr2 = this.id;
                    if (i3 >= cArr2.length) {
                        return false;
                    }
                    if (cArr2[i3] == '=') {
                        this.index = i2;
                        return true;
                    }
                    i3++;
                }
            } else {
                int i4 = i + 1;
                if (i4 >= cArr.length) {
                    return false;
                }
                this.index = i4;
                return true;
            }
        }
    }

    private String getKeyword() {
        int i = this.index;
        do {
        } while (!isDoneOrKeywordAssign(next()));
        this.index--;
        return AsciiUtil.toLowerString(new String(this.id, i, this.index - i).trim());
    }

    private String getValue() {
        int i = this.index;
        do {
        } while (!isDoneOrItemSeparator(next()));
        this.index--;
        return new String(this.id, i, this.index - i).trim();
    }

    private Comparator<String> getKeyComparator() {
        return new Comparator<String>() {
            /* class ohos.global.icu.impl.LocaleIDParser.AnonymousClass1 */

            public int compare(String str, String str2) {
                return str.compareTo(str2);
            }
        };
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:26:0x004d */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:19:0x004d */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1 */
    /* JADX WARN: Type inference failed for: r0v2 */
    /* JADX WARN: Type inference failed for: r0v3, types: [java.util.Map<java.lang.String, java.lang.String>] */
    /* JADX WARN: Type inference failed for: r0v4, types: [java.util.Map] */
    /* JADX WARN: Type inference failed for: r0v5, types: [java.util.TreeMap] */
    /* JADX WARN: Type inference failed for: r0v6 */
    /* JADX WARN: Type inference failed for: r0v9 */
    /* JADX WARN: Type inference failed for: r0v10 */
    /* JADX WARN: Type inference failed for: r0v13 */
    /* JADX WARN: Type inference failed for: r0v14 */
    /* JADX WARN: Type inference failed for: r0v15 */
    /* JADX WARN: Type inference failed for: r0v16 */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003f, code lost:
        if (r3 != false) goto L_0x0045;
     */
    /* JADX WARNING: Unknown variable types count: 1 */
    public Map<String, String> getKeywordMap() {
        TreeMap treeMap;
        if (this.keywords == null) {
            ?? r0 = 0;
            r0 = 0;
            if (setToKeywordStart()) {
                do {
                    String keyword = getKeyword();
                    if (keyword.length() == 0) {
                        break;
                    }
                    char next = next();
                    if (next != '=') {
                        r0 = r0;
                        if (next == 65535) {
                            break;
                        }
                    } else {
                        String value = getValue();
                        r0 = r0;
                        if (value.length() != 0) {
                            if (r0 == 0) {
                                treeMap = new TreeMap(getKeyComparator());
                            } else {
                                boolean containsKey = r0.containsKey(keyword);
                                treeMap = r0;
                                r0 = r0;
                            }
                            treeMap.put(keyword, value);
                            r0 = treeMap;
                        }
                    }
                    r0 = r0;
                } while (next() == ';');
            }
            if (r0 == 0) {
                r0 = Collections.emptyMap();
            }
            this.keywords = r0;
        }
        return this.keywords;
    }

    private int parseKeywords() {
        int length = this.buffer.length();
        Map<String, String> keywordMap = getKeywordMap();
        if (keywordMap.isEmpty()) {
            return length;
        }
        boolean z = true;
        for (Map.Entry<String, String> entry : keywordMap.entrySet()) {
            append(z ? KEYWORD_SEPARATOR : ITEM_SEPARATOR);
            z = false;
            append(entry.getKey());
            append(KEYWORD_ASSIGN);
            append(entry.getValue());
        }
        return !z ? length + 1 : length;
    }

    public Iterator<String> getKeywords() {
        Map<String, String> keywordMap = getKeywordMap();
        if (keywordMap.isEmpty()) {
            return null;
        }
        return keywordMap.keySet().iterator();
    }

    public String getKeywordValue(String str) {
        Map<String, String> keywordMap = getKeywordMap();
        if (keywordMap.isEmpty()) {
            return null;
        }
        return keywordMap.get(AsciiUtil.toLowerString(str.trim()));
    }

    public void defaultKeywordValue(String str, String str2) {
        setKeywordValue(str, str2, false);
    }

    public void setKeywordValue(String str, String str2) {
        setKeywordValue(str, str2, true);
    }

    private void setKeywordValue(String str, String str2, boolean z) {
        if (str != null) {
            String lowerString = AsciiUtil.toLowerString(str.trim());
            if (lowerString.length() != 0) {
                if (str2 != null) {
                    str2 = str2.trim();
                    if (str2.length() == 0) {
                        throw new IllegalArgumentException("value must not be empty");
                    }
                }
                Map<String, String> keywordMap = getKeywordMap();
                if (keywordMap.isEmpty()) {
                    if (str2 != null) {
                        this.keywords = new TreeMap(getKeyComparator());
                        this.keywords.put(lowerString, str2.trim());
                    }
                } else if (!z && keywordMap.containsKey(lowerString)) {
                } else {
                    if (str2 != null) {
                        keywordMap.put(lowerString, str2);
                        return;
                    }
                    keywordMap.remove(lowerString);
                    if (keywordMap.isEmpty()) {
                        this.keywords = Collections.emptyMap();
                    }
                }
            } else {
                throw new IllegalArgumentException("keyword must not be empty");
            }
        } else if (z) {
            this.keywords = Collections.emptyMap();
        }
    }
}
