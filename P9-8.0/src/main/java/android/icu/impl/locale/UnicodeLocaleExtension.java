package android.icu.impl.locale;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class UnicodeLocaleExtension extends Extension {
    public static final UnicodeLocaleExtension CA_JAPANESE = new UnicodeLocaleExtension();
    private static final SortedMap<String, String> EMPTY_SORTED_MAP = new TreeMap();
    private static final SortedSet<String> EMPTY_SORTED_SET = new TreeSet();
    public static final UnicodeLocaleExtension NU_THAI = new UnicodeLocaleExtension();
    public static final char SINGLETON = 'u';
    private SortedSet<String> _attributes;
    private SortedMap<String, String> _keywords;

    static {
        CA_JAPANESE._keywords = new TreeMap();
        CA_JAPANESE._keywords.put("ca", "japanese");
        CA_JAPANESE._value = "ca-japanese";
        NU_THAI._keywords = new TreeMap();
        NU_THAI._keywords.put("nu", "thai");
        NU_THAI._value = "nu-thai";
    }

    private UnicodeLocaleExtension() {
        super('u');
        this._attributes = EMPTY_SORTED_SET;
        this._keywords = EMPTY_SORTED_MAP;
    }

    UnicodeLocaleExtension(SortedSet<String> attributes, SortedMap<String, String> keywords) {
        this();
        if (attributes != null && attributes.size() > 0) {
            this._attributes = attributes;
        }
        if (keywords != null && keywords.size() > 0) {
            this._keywords = keywords;
        }
        if (this._attributes.size() > 0 || this._keywords.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String attribute : this._attributes) {
                sb.append(LanguageTag.SEP).append(attribute);
            }
            for (Entry<String, String> keyword : this._keywords.entrySet()) {
                String value = (String) keyword.getValue();
                sb.append(LanguageTag.SEP).append((String) keyword.getKey());
                if (value.length() > 0) {
                    sb.append(LanguageTag.SEP).append(value);
                }
            }
            this._value = sb.substring(1);
        }
    }

    public Set<String> getUnicodeLocaleAttributes() {
        return Collections.unmodifiableSet(this._attributes);
    }

    public Set<String> getUnicodeLocaleKeys() {
        return Collections.unmodifiableSet(this._keywords.keySet());
    }

    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        return (String) this._keywords.get(unicodeLocaleKey);
    }

    public static boolean isSingletonChar(char c) {
        return 'u' == AsciiUtil.toLower(c);
    }

    public static boolean isAttribute(String s) {
        return (s.length() < 3 || s.length() > 8) ? false : AsciiUtil.isAlphaNumericString(s);
    }

    public static boolean isKey(String s) {
        return s.length() == 2 ? AsciiUtil.isAlphaNumericString(s) : false;
    }

    public static boolean isTypeSubtag(String s) {
        return (s.length() < 3 || s.length() > 8) ? false : AsciiUtil.isAlphaNumericString(s);
    }

    public static boolean isType(String s) {
        boolean z = false;
        int startIdx = 0;
        while (true) {
            int idx = s.indexOf(LanguageTag.SEP, startIdx);
            if (!isTypeSubtag(idx < 0 ? s.substring(startIdx) : s.substring(startIdx, idx))) {
                return false;
            }
            if (idx < 0) {
                if (true && startIdx < s.length()) {
                    z = true;
                }
                return z;
            }
            startIdx = idx + 1;
        }
    }
}
