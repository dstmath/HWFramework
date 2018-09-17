package android.icu.impl;

import android.icu.impl.locale.AsciiUtil;
import android.icu.util.ULocale;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public final class LocaleIDParser {
    private static final char COMMA = ',';
    private static final char DONE = '￿';
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

    public LocaleIDParser(String localeID) {
        this(localeID, false);
    }

    public LocaleIDParser(String localeID, boolean canonicalize) {
        this.id = localeID.toCharArray();
        this.index = 0;
        this.buffer = new StringBuilder(this.id.length + 5);
        this.canonicalize = canonicalize;
    }

    private void reset() {
        this.index = 0;
        this.buffer = new StringBuilder(this.id.length + 5);
    }

    private void append(char c) {
        this.buffer.append(c);
    }

    private void addSeparator() {
        append((char) UNDERSCORE);
    }

    private String getString(int start) {
        return this.buffer.substring(start);
    }

    private void set(int pos, String s) {
        this.buffer.delete(pos, this.buffer.length());
        this.buffer.insert(pos, s);
    }

    private void append(String s) {
        this.buffer.append(s);
    }

    private char next() {
        if (this.index == this.id.length) {
            this.index++;
            return 65535;
        }
        char[] cArr = this.id;
        int i = this.index;
        this.index = i + 1;
        return cArr[i];
    }

    private void skipUntilTerminatorOrIDSeparator() {
        do {
        } while (!isTerminatorOrIDSeparator(next()));
        this.index--;
    }

    private boolean atTerminator() {
        return this.index < this.id.length ? isTerminator(this.id[this.index]) : true;
    }

    private boolean isTerminator(char c) {
        return c == KEYWORD_SEPARATOR || c == 65535 || c == DOT;
    }

    private boolean isTerminatorOrIDSeparator(char c) {
        return (c == UNDERSCORE || c == HYPHEN) ? true : isTerminator(c);
    }

    private boolean haveExperimentalLanguagePrefix() {
        boolean z = true;
        if (this.id.length > 2) {
            char c = this.id[1];
            if (c == HYPHEN || c == UNDERSCORE) {
                c = this.id[0];
                if (!(c == ULocale.PRIVATE_USE_EXTENSION || c == 'X' || c == UCharacterProperty.LATIN_SMALL_LETTER_I_ || c == 'I')) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    private boolean haveKeywordAssign() {
        for (int i = this.index; i < this.id.length; i++) {
            if (this.id[i] == KEYWORD_ASSIGN) {
                return true;
            }
        }
        return false;
    }

    private int parseLanguage() {
        int startLength = this.buffer.length();
        if (haveExperimentalLanguagePrefix()) {
            append(AsciiUtil.toLower(this.id[0]));
            append((char) HYPHEN);
            this.index = 2;
        }
        while (true) {
            char c = next();
            if (isTerminatorOrIDSeparator(c)) {
                break;
            }
            append(AsciiUtil.toLower(c));
        }
        this.index--;
        if (this.buffer.length() - startLength == 3) {
            String lang = LocaleIDs.threeToTwoLetterLanguage(getString(0));
            if (lang != null) {
                set(0, lang);
            }
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
        int oldIndex = this.index;
        this.index++;
        int oldBlen = this.buffer.length();
        boolean firstPass = true;
        while (true) {
            char c = next();
            if (isTerminatorOrIDSeparator(c) || !AsciiUtil.isAlpha(c)) {
                this.index--;
            } else if (firstPass) {
                addSeparator();
                append(AsciiUtil.toUpper(c));
                firstPass = false;
            } else {
                append(AsciiUtil.toLower(c));
            }
        }
        this.index--;
        if (this.index - oldIndex != 5) {
            this.index = oldIndex;
            this.buffer.delete(oldBlen, this.buffer.length());
        } else {
            oldBlen++;
        }
        return oldBlen;
    }

    private void skipScript() {
        if (!atTerminator()) {
            int oldIndex = this.index;
            this.index++;
            char c;
            do {
                c = next();
                if (isTerminatorOrIDSeparator(c)) {
                    break;
                }
            } while (AsciiUtil.isAlpha(c));
            this.index--;
            if (this.index - oldIndex != 5) {
                this.index = oldIndex;
            }
        }
    }

    private int parseCountry() {
        if (atTerminator()) {
            return this.buffer.length();
        }
        int oldIndex = this.index;
        this.index++;
        int oldBlen = this.buffer.length();
        boolean firstPass = true;
        while (true) {
            char c = next();
            if (isTerminatorOrIDSeparator(c)) {
                break;
            }
            if (firstPass) {
                this.hadCountry = true;
                addSeparator();
                oldBlen++;
                firstPass = false;
            }
            append(AsciiUtil.toUpper(c));
        }
        this.index--;
        int charsAppended = this.buffer.length() - oldBlen;
        if (charsAppended != 0) {
            if (charsAppended < 2 || charsAppended > 3) {
                this.index = oldIndex;
                oldBlen--;
                this.buffer.delete(oldBlen, this.buffer.length());
                this.hadCountry = false;
            } else if (charsAppended == 3) {
                String region = LocaleIDs.threeToTwoLetterRegion(getString(oldBlen));
                if (region != null) {
                    set(oldBlen, region);
                }
            }
        }
        return oldBlen;
    }

    private void skipCountry() {
        if (!atTerminator()) {
            if (this.id[this.index] == UNDERSCORE || this.id[this.index] == HYPHEN) {
                this.index++;
            }
            int oldIndex = this.index;
            skipUntilTerminatorOrIDSeparator();
            int charsSkipped = this.index - oldIndex;
            if (charsSkipped < 2 || charsSkipped > 3) {
                this.index = oldIndex;
            }
        }
    }

    private int parseVariant() {
        int oldBlen = this.buffer.length();
        boolean start = true;
        boolean needSeparator = true;
        boolean skipping = false;
        boolean firstPass = true;
        while (true) {
            char c = next();
            if (c == 65535) {
                break;
            } else if (c == DOT) {
                start = false;
                skipping = true;
            } else if (c == KEYWORD_SEPARATOR) {
                if (haveKeywordAssign()) {
                    break;
                }
                skipping = false;
                start = false;
                needSeparator = true;
            } else if (start) {
                start = false;
                if (!(c == UNDERSCORE || c == HYPHEN)) {
                    this.index--;
                }
            } else if (!skipping) {
                if (needSeparator) {
                    needSeparator = false;
                    if (firstPass && (this.hadCountry ^ 1) != 0) {
                        addSeparator();
                        oldBlen++;
                    }
                    addSeparator();
                    if (firstPass) {
                        oldBlen++;
                        firstPass = false;
                    }
                }
                c = AsciiUtil.toUpper(c);
                if (c == HYPHEN || c == COMMA) {
                    c = UNDERSCORE;
                }
                append(c);
            }
        }
        this.index--;
        return oldBlen;
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

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public void parseBaseName() {
        if (this.baseName != null) {
            set(0, this.baseName);
            return;
        }
        reset();
        parseLanguage();
        parseScript();
        parseCountry();
        parseVariant();
        int len = this.buffer.length();
        if (len > 0 && this.buffer.charAt(len - 1) == UNDERSCORE) {
            this.buffer.deleteCharAt(len - 1);
        }
    }

    public String getBaseName() {
        if (this.baseName != null) {
            return this.baseName;
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
        for (int i = this.index; i < this.id.length; i++) {
            if (this.id[i] == KEYWORD_SEPARATOR) {
                if (this.canonicalize) {
                    i++;
                    for (int j = i; j < this.id.length; j++) {
                        if (this.id[j] == KEYWORD_ASSIGN) {
                            this.index = i;
                            return true;
                        }
                    }
                } else {
                    i++;
                    if (i < this.id.length) {
                        this.index = i;
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    private static boolean isDoneOrKeywordAssign(char c) {
        return c == 65535 || c == KEYWORD_ASSIGN;
    }

    private static boolean isDoneOrItemSeparator(char c) {
        return c == 65535 || c == ITEM_SEPARATOR;
    }

    private String getKeyword() {
        int start = this.index;
        do {
        } while (!isDoneOrKeywordAssign(next()));
        this.index--;
        return AsciiUtil.toLowerString(new String(this.id, start, this.index - start).trim());
    }

    private String getValue() {
        int start = this.index;
        do {
        } while (!isDoneOrItemSeparator(next()));
        this.index--;
        return new String(this.id, start, this.index - start).trim();
    }

    private Comparator<String> getKeyComparator() {
        return new Comparator<String>() {
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        };
    }

    /* JADX WARNING: Missing block: B:23:0x004f, code:
            if (r2.containsKey(r1) != false) goto L_0x0029;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Map<String, String> getKeywordMap() {
        if (this.keywords == null) {
            Map map = null;
            if (setToKeywordStart()) {
                while (true) {
                    String key = getKeyword();
                    if (key.length() != 0) {
                        char c = next();
                        if (c != KEYWORD_ASSIGN) {
                            if (c == 65535) {
                                break;
                            }
                        }
                        String value = getValue();
                        if (value.length() != 0) {
                            if (map == null) {
                                map = new TreeMap(getKeyComparator());
                            }
                            map.put(key, value);
                        }
                        if (next() != ITEM_SEPARATOR) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (map == null) {
                map = Collections.emptyMap();
            }
            this.keywords = map;
        }
        return this.keywords;
    }

    private int parseKeywords() {
        int oldBlen = this.buffer.length();
        Map<String, String> m = getKeywordMap();
        if (m.isEmpty()) {
            return oldBlen;
        }
        boolean first = true;
        for (Entry<String, String> e : m.entrySet()) {
            append(first ? KEYWORD_SEPARATOR : ITEM_SEPARATOR);
            first = false;
            append((String) e.getKey());
            append((char) KEYWORD_ASSIGN);
            append((String) e.getValue());
        }
        if (first) {
            return oldBlen;
        }
        return oldBlen + 1;
    }

    public Iterator<String> getKeywords() {
        Map<String, String> m = getKeywordMap();
        return m.isEmpty() ? null : m.keySet().iterator();
    }

    public String getKeywordValue(String keywordName) {
        Map<String, String> m = getKeywordMap();
        return m.isEmpty() ? null : (String) m.get(AsciiUtil.toLowerString(keywordName.trim()));
    }

    public void defaultKeywordValue(String keywordName, String value) {
        setKeywordValue(keywordName, value, false);
    }

    public void setKeywordValue(String keywordName, String value) {
        setKeywordValue(keywordName, value, true);
    }

    private void setKeywordValue(String keywordName, String value, boolean reset) {
        if (keywordName != null) {
            keywordName = AsciiUtil.toLowerString(keywordName.trim());
            if (keywordName.length() == 0) {
                throw new IllegalArgumentException("keyword must not be empty");
            }
            if (value != null) {
                value = value.trim();
                if (value.length() == 0) {
                    throw new IllegalArgumentException("value must not be empty");
                }
            }
            Map<String, String> m = getKeywordMap();
            if (m.isEmpty()) {
                if (value != null) {
                    this.keywords = new TreeMap(getKeyComparator());
                    this.keywords.put(keywordName, value.trim());
                }
            } else if (!reset && (m.containsKey(keywordName) ^ 1) == 0) {
            } else {
                if (value != null) {
                    m.put(keywordName, value);
                    return;
                }
                m.remove(keywordName);
                if (m.isEmpty()) {
                    this.keywords = Collections.emptyMap();
                }
            }
        } else if (reset) {
            this.keywords = Collections.emptyMap();
        }
    }
}
