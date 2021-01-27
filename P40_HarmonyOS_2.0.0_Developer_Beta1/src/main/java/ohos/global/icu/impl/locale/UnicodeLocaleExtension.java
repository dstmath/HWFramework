package ohos.global.icu.impl.locale;

import java.util.Collections;
import java.util.Map;
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
        super(SINGLETON);
        this._attributes = EMPTY_SORTED_SET;
        this._keywords = EMPTY_SORTED_MAP;
    }

    UnicodeLocaleExtension(SortedSet<String> sortedSet, SortedMap<String, String> sortedMap) {
        this();
        if (sortedSet != null && sortedSet.size() > 0) {
            this._attributes = sortedSet;
        }
        if (sortedMap != null && sortedMap.size() > 0) {
            this._keywords = sortedMap;
        }
        if (this._attributes.size() > 0 || this._keywords.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String str : this._attributes) {
                sb.append(LanguageTag.SEP);
                sb.append(str);
            }
            for (Map.Entry<String, String> entry : this._keywords.entrySet()) {
                String value = entry.getValue();
                sb.append(LanguageTag.SEP);
                sb.append(entry.getKey());
                if (value.length() > 0) {
                    sb.append(LanguageTag.SEP);
                    sb.append(value);
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

    public String getUnicodeLocaleType(String str) {
        return this._keywords.get(str);
    }

    public static boolean isSingletonChar(char c) {
        return 'u' == AsciiUtil.toLower(c);
    }

    public static boolean isAttribute(String str) {
        return str.length() >= 3 && str.length() <= 8 && AsciiUtil.isAlphaNumericString(str);
    }

    public static boolean isKey(String str) {
        return str.length() == 2 && AsciiUtil.isAlphaNumeric(str.charAt(0)) && AsciiUtil.isAlpha(str.charAt(1));
    }

    public static boolean isTypeSubtag(String str) {
        return str.length() >= 3 && str.length() <= 8 && AsciiUtil.isAlphaNumericString(str);
    }

    public static boolean isType(String str) {
        int i = 0;
        while (true) {
            int indexOf = str.indexOf(LanguageTag.SEP, i);
            if (!isTypeSubtag(indexOf < 0 ? str.substring(i) : str.substring(i, indexOf))) {
                return false;
            }
            if (indexOf >= 0) {
                i = indexOf + 1;
            } else if (i < str.length()) {
                return true;
            } else {
                return false;
            }
        }
    }
}
