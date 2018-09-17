package android.icu.impl.locale;

import android.icu.util.ULocale;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.xmlpull.v1.XmlPullParser;

public class LocaleExtensions {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final LocaleExtensions CALENDAR_JAPANESE = null;
    public static final LocaleExtensions EMPTY_EXTENSIONS = null;
    private static final SortedMap<Character, Extension> EMPTY_MAP = null;
    public static final LocaleExtensions NUMBER_THAI = null;
    private String _id;
    private SortedMap<Character, Extension> _map;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.locale.LocaleExtensions.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.locale.LocaleExtensions.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.locale.LocaleExtensions.<clinit>():void");
    }

    private LocaleExtensions() {
    }

    LocaleExtensions(Map<CaseInsensitiveChar, String> extensions, Set<CaseInsensitiveString> uattributes, Map<CaseInsensitiveString, String> ukeywords) {
        boolean hasExtension = extensions != null && extensions.size() > 0;
        boolean hasUAttributes = uattributes != null && uattributes.size() > 0;
        boolean hasUKeywords = ukeywords != null && ukeywords.size() > 0;
        if (hasExtension || hasUAttributes || hasUKeywords) {
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
                TreeSet<String> treeSet = null;
                TreeMap<String, String> treeMap = null;
                if (hasUAttributes) {
                    treeSet = new TreeSet();
                    for (CaseInsensitiveString cis : uattributes) {
                        treeSet.add(AsciiUtil.toLowerString(cis.value()));
                    }
                }
                if (hasUKeywords) {
                    treeMap = new TreeMap();
                    for (Entry<CaseInsensitiveString, String> kwd : ukeywords.entrySet()) {
                        treeMap.put(AsciiUtil.toLowerString(((CaseInsensitiveString) kwd.getKey()).value()), AsciiUtil.toLowerString((String) kwd.getValue()));
                    }
                }
                this._map.put(Character.valueOf(ULocale.UNICODE_LOCALE_EXTENSION), new UnicodeLocaleExtension(treeSet, treeMap));
            }
            if (this._map.size() == 0) {
                this._map = EMPTY_MAP;
                this._id = XmlPullParser.NO_NAMESPACE;
            } else {
                this._id = toID(this._map);
            }
            return;
        }
        this._map = EMPTY_MAP;
        this._id = XmlPullParser.NO_NAMESPACE;
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
        Extension ext = (Extension) this._map.get(Character.valueOf(ULocale.UNICODE_LOCALE_EXTENSION));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleAttributes();
        }
        throw new AssertionError();
    }

    public Set<String> getUnicodeLocaleKeys() {
        Extension ext = (Extension) this._map.get(Character.valueOf(ULocale.UNICODE_LOCALE_EXTENSION));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleKeys();
        }
        throw new AssertionError();
    }

    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        Extension ext = (Extension) this._map.get(Character.valueOf(ULocale.UNICODE_LOCALE_EXTENSION));
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
