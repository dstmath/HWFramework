package ohos.global.icu.impl.locale;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import ohos.global.icu.impl.locale.InternalLocaleBuilder;

public class LocaleExtensions {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final LocaleExtensions CALENDAR_JAPANESE = new LocaleExtensions();
    public static final LocaleExtensions EMPTY_EXTENSIONS = new LocaleExtensions();
    private static final SortedMap<Character, Extension> EMPTY_MAP = Collections.unmodifiableSortedMap(new TreeMap());
    public static final LocaleExtensions NUMBER_THAI = new LocaleExtensions();
    private String _id;
    private SortedMap<Character, Extension> _map;

    static {
        LocaleExtensions localeExtensions = EMPTY_EXTENSIONS;
        localeExtensions._id = "";
        localeExtensions._map = EMPTY_MAP;
        LocaleExtensions localeExtensions2 = CALENDAR_JAPANESE;
        localeExtensions2._id = "u-ca-japanese";
        localeExtensions2._map = new TreeMap();
        CALENDAR_JAPANESE._map.put(Character.valueOf(UnicodeLocaleExtension.SINGLETON), UnicodeLocaleExtension.CA_JAPANESE);
        LocaleExtensions localeExtensions3 = NUMBER_THAI;
        localeExtensions3._id = "u-nu-thai";
        localeExtensions3._map = new TreeMap();
        NUMBER_THAI._map.put(Character.valueOf(UnicodeLocaleExtension.SINGLETON), UnicodeLocaleExtension.NU_THAI);
    }

    private LocaleExtensions() {
    }

    LocaleExtensions(Map<InternalLocaleBuilder.CaseInsensitiveChar, String> map, Set<InternalLocaleBuilder.CaseInsensitiveString> set, Map<InternalLocaleBuilder.CaseInsensitiveString, String> map2) {
        TreeSet treeSet;
        boolean z = true;
        boolean z2 = map != null && map.size() > 0;
        boolean z3 = set != null && set.size() > 0;
        z = (map2 == null || map2.size() <= 0) ? false : z;
        if (z2 || z3 || z) {
            this._map = new TreeMap();
            if (z2) {
                for (Map.Entry<InternalLocaleBuilder.CaseInsensitiveChar, String> entry : map.entrySet()) {
                    char lower = AsciiUtil.toLower(entry.getKey().value());
                    String value = entry.getValue();
                    if (!LanguageTag.isPrivateusePrefixChar(lower) || (value = InternalLocaleBuilder.removePrivateuseVariant(value)) != null) {
                        this._map.put(Character.valueOf(lower), new Extension(lower, AsciiUtil.toLowerString(value)));
                    }
                }
            }
            if (z3 || z) {
                TreeMap treeMap = null;
                if (z3) {
                    treeSet = new TreeSet();
                    for (InternalLocaleBuilder.CaseInsensitiveString caseInsensitiveString : set) {
                        treeSet.add(AsciiUtil.toLowerString(caseInsensitiveString.value()));
                    }
                } else {
                    treeSet = null;
                }
                if (z) {
                    treeMap = new TreeMap();
                    for (Map.Entry<InternalLocaleBuilder.CaseInsensitiveString, String> entry2 : map2.entrySet()) {
                        treeMap.put(AsciiUtil.toLowerString(entry2.getKey().value()), AsciiUtil.toLowerString(entry2.getValue()));
                    }
                }
                this._map.put(Character.valueOf(UnicodeLocaleExtension.SINGLETON), new UnicodeLocaleExtension(treeSet, treeMap));
            }
            if (this._map.size() == 0) {
                this._map = EMPTY_MAP;
                this._id = "";
                return;
            }
            this._id = toID(this._map);
            return;
        }
        this._map = EMPTY_MAP;
        this._id = "";
    }

    public Set<Character> getKeys() {
        return Collections.unmodifiableSet(this._map.keySet());
    }

    public Extension getExtension(Character ch) {
        return this._map.get(Character.valueOf(AsciiUtil.toLower(ch.charValue())));
    }

    public String getExtensionValue(Character ch) {
        Extension extension = this._map.get(Character.valueOf(AsciiUtil.toLower(ch.charValue())));
        if (extension == null) {
            return null;
        }
        return extension.getValue();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        Extension extension = this._map.get(Character.valueOf(UnicodeLocaleExtension.SINGLETON));
        if (extension == null) {
            return Collections.emptySet();
        }
        return ((UnicodeLocaleExtension) extension).getUnicodeLocaleAttributes();
    }

    public Set<String> getUnicodeLocaleKeys() {
        Extension extension = this._map.get(Character.valueOf(UnicodeLocaleExtension.SINGLETON));
        if (extension == null) {
            return Collections.emptySet();
        }
        return ((UnicodeLocaleExtension) extension).getUnicodeLocaleKeys();
    }

    public String getUnicodeLocaleType(String str) {
        Extension extension = this._map.get(Character.valueOf(UnicodeLocaleExtension.SINGLETON));
        if (extension == null) {
            return null;
        }
        return ((UnicodeLocaleExtension) extension).getUnicodeLocaleType(AsciiUtil.toLowerString(str));
    }

    public boolean isEmpty() {
        return this._map.isEmpty();
    }

    public static boolean isValidKey(char c) {
        return LanguageTag.isExtensionSingletonChar(c) || LanguageTag.isPrivateusePrefixChar(c);
    }

    public static boolean isValidUnicodeLocaleKey(String str) {
        return UnicodeLocaleExtension.isKey(str);
    }

    private static String toID(SortedMap<Character, Extension> sortedMap) {
        StringBuilder sb = new StringBuilder();
        Extension extension = null;
        for (Map.Entry<Character, Extension> entry : sortedMap.entrySet()) {
            char charValue = entry.getKey().charValue();
            Extension value = entry.getValue();
            if (LanguageTag.isPrivateusePrefixChar(charValue)) {
                extension = value;
            } else {
                if (sb.length() > 0) {
                    sb.append(LanguageTag.SEP);
                }
                sb.append(value);
            }
        }
        if (extension != null) {
            if (sb.length() > 0) {
                sb.append(LanguageTag.SEP);
            }
            sb.append(extension);
        }
        return sb.toString();
    }

    public String toString() {
        return this._id;
    }

    public String getID() {
        return this._id;
    }

    public int hashCode() {
        return this._id.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LocaleExtensions)) {
            return false;
        }
        return this._id.equals(((LocaleExtensions) obj)._id);
    }
}
