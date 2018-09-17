package sun.util.locale;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class LocaleExtensions {
    static final /* synthetic */ boolean -assertionsDisabled = (LocaleExtensions.class.desiredAssertionStatus() ^ 1);
    public static final LocaleExtensions CALENDAR_JAPANESE = new LocaleExtensions("u-ca-japanese", Character.valueOf('u'), UnicodeLocaleExtension.CA_JAPANESE);
    public static final LocaleExtensions NUMBER_THAI = new LocaleExtensions("u-nu-thai", Character.valueOf('u'), UnicodeLocaleExtension.NU_THAI);
    private final Map<Character, Extension> extensionMap;
    private final String id;

    private LocaleExtensions(String id, Character key, Extension value) {
        this.id = id;
        this.extensionMap = Collections.singletonMap(key, value);
    }

    LocaleExtensions(Map<CaseInsensitiveChar, String> extensions, Set<CaseInsensitiveString> uattributes, Map<CaseInsensitiveString, String> ukeywords) {
        boolean hasExtension = LocaleUtils.isEmpty((Map) extensions) ^ 1;
        boolean hasUAttributes = LocaleUtils.isEmpty((Set) uattributes) ^ 1;
        boolean hasUKeywords = LocaleUtils.isEmpty((Map) ukeywords) ^ 1;
        if (hasExtension || (hasUAttributes ^ 1) == 0 || (hasUKeywords ^ 1) == 0) {
            SortedMap<Character, Extension> map = new TreeMap();
            if (hasExtension) {
                for (Entry<CaseInsensitiveChar, String> ext : extensions.entrySet()) {
                    char key = LocaleUtils.toLower(((CaseInsensitiveChar) ext.getKey()).value());
                    String value = (String) ext.getValue();
                    if (LanguageTag.isPrivateusePrefixChar(key)) {
                        value = InternalLocaleBuilder.removePrivateuseVariant(value);
                        if (value == null) {
                        }
                    }
                    map.put(Character.valueOf(key), new Extension(key, LocaleUtils.toLowerString(value)));
                }
            }
            if (hasUAttributes || hasUKeywords) {
                SortedSet uaset = null;
                SortedMap<String, String> ukmap = null;
                if (hasUAttributes) {
                    uaset = new TreeSet();
                    for (CaseInsensitiveString cis : uattributes) {
                        uaset.-java_util_stream_Collectors-mthref-4(LocaleUtils.toLowerString(cis.value()));
                    }
                }
                if (hasUKeywords) {
                    ukmap = new TreeMap();
                    for (Entry<CaseInsensitiveString, String> kwd : ukeywords.entrySet()) {
                        ukmap.put(LocaleUtils.toLowerString(((CaseInsensitiveString) kwd.getKey()).value()), LocaleUtils.toLowerString((String) kwd.getValue()));
                    }
                }
                map.put(Character.valueOf('u'), new UnicodeLocaleExtension(uaset, (SortedMap) ukmap));
            }
            if (map.isEmpty()) {
                this.id = "";
                this.extensionMap = Collections.emptyMap();
            } else {
                this.id = toID(map);
                this.extensionMap = map;
            }
            return;
        }
        this.id = "";
        this.extensionMap = Collections.emptyMap();
    }

    public Set<Character> getKeys() {
        if (this.extensionMap.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(this.extensionMap.keySet());
    }

    public Extension getExtension(Character key) {
        return (Extension) this.extensionMap.get(Character.valueOf(LocaleUtils.toLower(key.charValue())));
    }

    public String getExtensionValue(Character key) {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf(LocaleUtils.toLower(key.charValue())));
        if (ext == null) {
            return null;
        }
        return ext.getValue();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf('u'));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleAttributes();
        }
        throw new AssertionError();
    }

    public Set<String> getUnicodeLocaleKeys() {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf('u'));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleKeys();
        }
        throw new AssertionError();
    }

    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf('u'));
        if (ext == null) {
            return null;
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleType(LocaleUtils.toLowerString(unicodeLocaleKey));
        }
        throw new AssertionError();
    }

    public boolean isEmpty() {
        return this.extensionMap.isEmpty();
    }

    public static boolean isValidKey(char c) {
        return !LanguageTag.isExtensionSingletonChar(c) ? LanguageTag.isPrivateusePrefixChar(c) : true;
    }

    public static boolean isValidUnicodeLocaleKey(String ukey) {
        return UnicodeLocaleExtension.isKey(ukey);
    }

    private static String toID(SortedMap<Character, Extension> map) {
        StringBuilder buf = new StringBuilder();
        Object privuse = null;
        for (Entry<Character, Extension> entry : map.entrySet()) {
            Object extension = (Extension) entry.getValue();
            if (LanguageTag.isPrivateusePrefixChar(((Character) entry.getKey()).charValue())) {
                privuse = extension;
            } else {
                if (buf.length() > 0) {
                    buf.append(LanguageTag.SEP);
                }
                buf.append(extension);
            }
        }
        if (privuse != null) {
            if (buf.length() > 0) {
                buf.append(LanguageTag.SEP);
            }
            buf.append(privuse);
        }
        return buf.-java_util_stream_Collectors-mthref-7();
    }

    public String toString() {
        return this.id;
    }

    public String getID() {
        return this.id;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof LocaleExtensions) {
            return this.id.equals(((LocaleExtensions) other).id);
        }
        return false;
    }
}
