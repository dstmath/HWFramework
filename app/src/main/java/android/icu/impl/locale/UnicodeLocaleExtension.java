package android.icu.impl.locale;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class UnicodeLocaleExtension extends Extension {
    public static final UnicodeLocaleExtension CA_JAPANESE = null;
    private static final SortedMap<String, String> EMPTY_SORTED_MAP = null;
    private static final SortedSet<String> EMPTY_SORTED_SET = null;
    public static final UnicodeLocaleExtension NU_THAI = null;
    public static final char SINGLETON = 'u';
    private SortedSet<String> _attributes;
    private SortedMap<String, String> _keywords;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.locale.UnicodeLocaleExtension.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.locale.UnicodeLocaleExtension.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.locale.UnicodeLocaleExtension.<clinit>():void");
    }

    private UnicodeLocaleExtension() {
        super(SINGLETON);
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
        return SINGLETON == AsciiUtil.toLower(c);
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
                break;
            }
            startIdx = idx + 1;
        }
        if (true && startIdx < s.length()) {
            z = true;
        }
        return z;
    }
}
