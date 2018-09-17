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
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final LocaleExtensions CALENDAR_JAPANESE = null;
    public static final LocaleExtensions NUMBER_THAI = null;
    private final Map<Character, Extension> extensionMap;
    private final String id;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.locale.LocaleExtensions.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.locale.LocaleExtensions.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.LocaleExtensions.<clinit>():void");
    }

    private LocaleExtensions(String id, Character key, Extension value) {
        this.id = id;
        this.extensionMap = Collections.singletonMap(key, value);
    }

    LocaleExtensions(Map<CaseInsensitiveChar, String> extensions, Set<CaseInsensitiveString> uattributes, Map<CaseInsensitiveString, String> ukeywords) {
        boolean hasExtension = !LocaleUtils.isEmpty((Map) extensions);
        boolean hasUAttributes = !LocaleUtils.isEmpty((Set) uattributes);
        boolean hasUKeywords = !LocaleUtils.isEmpty((Map) ukeywords);
        if (hasExtension || hasUAttributes || hasUKeywords) {
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
                SortedSet sortedSet = null;
                SortedMap<String, String> sortedMap = null;
                if (hasUAttributes) {
                    sortedSet = new TreeSet();
                    for (CaseInsensitiveString cis : uattributes) {
                        sortedSet.add(LocaleUtils.toLowerString(cis.value()));
                    }
                }
                if (hasUKeywords) {
                    sortedMap = new TreeMap();
                    for (Entry<CaseInsensitiveString, String> kwd : ukeywords.entrySet()) {
                        sortedMap.put(LocaleUtils.toLowerString(((CaseInsensitiveString) kwd.getKey()).value()), LocaleUtils.toLowerString((String) kwd.getValue()));
                    }
                }
                map.put(Character.valueOf(UnicodeLocaleExtension.SINGLETON), new UnicodeLocaleExtension(sortedSet, (SortedMap) sortedMap));
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
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf(UnicodeLocaleExtension.SINGLETON));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleAttributes();
        }
        throw new AssertionError();
    }

    public Set<String> getUnicodeLocaleKeys() {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf(UnicodeLocaleExtension.SINGLETON));
        if (ext == null) {
            return Collections.emptySet();
        }
        if (-assertionsDisabled || (ext instanceof UnicodeLocaleExtension)) {
            return ((UnicodeLocaleExtension) ext).getUnicodeLocaleKeys();
        }
        throw new AssertionError();
    }

    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        Extension ext = (Extension) this.extensionMap.get(Character.valueOf(UnicodeLocaleExtension.SINGLETON));
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
        return buf.toString();
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
