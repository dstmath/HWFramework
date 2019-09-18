package sun.util.locale;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class UnicodeLocaleExtension extends Extension {
    public static final UnicodeLocaleExtension CA_JAPANESE = new UnicodeLocaleExtension("ca", "japanese");
    public static final UnicodeLocaleExtension NU_THAI = new UnicodeLocaleExtension("nu", "thai");
    public static final char SINGLETON = 'u';
    private final Set<String> attributes;
    private final Map<String, String> keywords;

    public /* bridge */ /* synthetic */ String getID() {
        return super.getID();
    }

    public /* bridge */ /* synthetic */ char getKey() {
        return super.getKey();
    }

    public /* bridge */ /* synthetic */ String getValue() {
        return super.getValue();
    }

    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    private UnicodeLocaleExtension(String key, String value) {
        super('u', key + LanguageTag.SEP + value);
        this.attributes = Collections.emptySet();
        this.keywords = Collections.singletonMap(key, value);
    }

    UnicodeLocaleExtension(SortedSet<String> attributes2, SortedMap<String, String> keywords2) {
        super('u');
        if (attributes2 != null) {
            this.attributes = attributes2;
        } else {
            this.attributes = Collections.emptySet();
        }
        if (keywords2 != null) {
            this.keywords = keywords2;
        } else {
            this.keywords = Collections.emptyMap();
        }
        if (!this.attributes.isEmpty() || !this.keywords.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String attribute : this.attributes) {
                sb.append(LanguageTag.SEP);
                sb.append(attribute);
            }
            for (Map.Entry<String, String> keyword : this.keywords.entrySet()) {
                String value = keyword.getValue();
                sb.append(LanguageTag.SEP);
                sb.append(keyword.getKey());
                if (value.length() > 0) {
                    sb.append(LanguageTag.SEP);
                    sb.append(value);
                }
            }
            setValue(sb.substring(1));
        }
    }

    public Set<String> getUnicodeLocaleAttributes() {
        if (this.attributes == Collections.EMPTY_SET) {
            return this.attributes;
        }
        return Collections.unmodifiableSet(this.attributes);
    }

    public Set<String> getUnicodeLocaleKeys() {
        if (this.keywords == Collections.EMPTY_MAP) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(this.keywords.keySet());
    }

    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        return this.keywords.get(unicodeLocaleKey);
    }

    public static boolean isSingletonChar(char c) {
        return 'u' == LocaleUtils.toLower(c);
    }

    public static boolean isAttribute(String s) {
        int len = s.length();
        return len >= 3 && len <= 8 && LocaleUtils.isAlphaNumericString(s);
    }

    public static boolean isKey(String s) {
        return s.length() == 2 && LocaleUtils.isAlphaNumericString(s);
    }

    public static boolean isTypeSubtag(String s) {
        int len = s.length();
        return len >= 3 && len <= 8 && LocaleUtils.isAlphaNumericString(s);
    }
}
