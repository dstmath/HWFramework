package android.icu.impl.locale;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class LocaleExtensions {
    static final /* synthetic */ boolean -assertionsDisabled = (LocaleExtensions.class.desiredAssertionStatus() ^ 1);
    public static final LocaleExtensions CALENDAR_JAPANESE = new LocaleExtensions();
    public static final LocaleExtensions EMPTY_EXTENSIONS = new LocaleExtensions();
    private static final SortedMap<Character, Extension> EMPTY_MAP = Collections.unmodifiableSortedMap(new TreeMap());
    public static final LocaleExtensions NUMBER_THAI = new LocaleExtensions();
    private String _id;
    private SortedMap<Character, Extension> _map;

    static {
        EMPTY_EXTENSIONS._id = "";
        EMPTY_EXTENSIONS._map = EMPTY_MAP;
        CALENDAR_JAPANESE._id = "u-ca-japanese";
        CALENDAR_JAPANESE._map = new TreeMap();
        CALENDAR_JAPANESE._map.put(Character.valueOf('u'), UnicodeLocaleExtension.CA_JAPANESE);
        NUMBER_THAI._id = "u-nu-thai";
        NUMBER_THAI._map = new TreeMap();
        NUMBER_THAI._map.put(Character.valueOf('u'), UnicodeLocaleExtension.NU_THAI);
    }

    private LocaleExtensions() {
    }

    LocaleExtensions(Map<CaseInsensitiveChar, String> extensions, Set<CaseInsensitiveString> uattributes, Map<CaseInsensitiveString, String> ukeywords) {
        boolean hasExtension = extensions != null && extensions.size() > 0;
        boolean hasUAttributes = uattributes != null && uattributes.size() > 0;
        boolean hasUKeywords = ukeywords != null && ukeywords.size() > 0;
        if (hasExtension || (hasUAttributes ^ 1) == 0 || (hasUKeywords ^ 1) == 0) {
            this._map = new TreeMap();
            if (hasExtension) {
                for (Entry<CaseInsensitiveChar, String> ext : extensions.entrySet()) {
                    char key = AsciiUtil.toLower(((CaseInsensitiveChar) ext.getKey()).value());
                    String value = (String) ext.getValue();
                    if (LanguageTag.isPrivateusePrefixChar(key)) {
                        value = InternalLocaleBuilder.removePrivateuseVariant(value);
                        if (value == null) {
                        }
                    }
                    this._map.put(Character.valueOf(key), new Extension(key, AsciiUtil.toLowerString(value)));
                }
            }
            if (hasUAttributes || hasUKeywords) {
                TreeSet<String> uaset = null;
                TreeMap<String, String> ukmap = null;
                if (hasUAttributes) {
                    uaset = new TreeSet();
                    for (CaseInsensitiveString cis : uattributes) {
                        uaset.add(AsciiUtil.toLowerString(cis.value()));
                    }
                }
                if (hasUKeywords) {
                    ukmap = new TreeMap();
                    for (Entry<CaseInsensitiveString, String> kwd : ukeywords.entrySet()) {
                        ukmap.put(AsciiUtil.toLowerString(((CaseInsensitiveString) kwd.getKey()).value()), AsciiUtil.toLowerString((String) kwd.getValue()));
                    }
                }
                this._map.put(Character.valueOf('u'), new UnicodeLocaleExtension(uaset, ukmap));
            }
            if (this._map.size() == 0) {
                this._map = EMPTY_MAP;
                this._id = "";
            } else {
                this._id = toID(this._map);
            }
            return;
        }
        this._map = EMPTY_MAP;
        this._id = "";
    }

    public Set<Character> getKeys() {
        return Collections.unmodifiableSet(this._map.keySet());
    }

    public Extension getExtension(Character key) {
        return (Extension) this._map.get(Character.valueOf(AsciiUtil.toLower(key.charValue())));
    }

    public String getExtensionValue(Character key) {
        Extension ext = (Extension) this._map.get(Character.valueOf(AsciiUtil.toLower(key.charValue())));
        if (ext == null) {
            return null;
        }
        return ext.getValue();
    }

    public Set<String> getUnicodeLocaleAttributes() {
        Extension ext = (Extension) this._map.get(Character.valueOf('u'));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleAttributes();
        }
        throw new AssertionError();
    }

    public Set<String> getUnicodeLocaleKeys() {
        Extension ext = (Extension) this._map.get(Character.valueOf('u'));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleKeys();
        }
        throw new AssertionError();
    }

    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        Extension ext = (Extension) this._map.get(Character.valueOf('u'));
        if (ext == null) {
            return null;
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleType(AsciiUtil.toLowerString(unicodeLocaleKey));
        }
        throw new AssertionError();
    }

    public boolean isEmpty() {
        return this._map.isEmpty();
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
            Extension extension = (Extension) entry.getValue();
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
        return buf.toString();
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

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof LocaleExtensions) {
            return this._id.equals(((LocaleExtensions) other)._id);
        }
        return false;
    }
}
