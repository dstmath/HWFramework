package sun.util.locale;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class UnicodeLocaleExtension extends Extension {
    public static final UnicodeLocaleExtension CA_JAPANESE = null;
    public static final UnicodeLocaleExtension NU_THAI = null;
    public static final char SINGLETON = 'u';
    private final Set<String> attributes;
    private final Map<String, String> keywords;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.locale.UnicodeLocaleExtension.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.locale.UnicodeLocaleExtension.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.UnicodeLocaleExtension.<clinit>():void");
    }

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
        super(SINGLETON, key + LanguageTag.SEP + value);
        this.attributes = Collections.emptySet();
        this.keywords = Collections.singletonMap(key, value);
    }

    UnicodeLocaleExtension(SortedSet<String> attributes, SortedMap<String, String> keywords) {
        super(SINGLETON);
        if (attributes != null) {
            this.attributes = attributes;
        } else {
            this.attributes = Collections.emptySet();
        }
        if (keywords != null) {
            this.keywords = keywords;
        } else {
            this.keywords = Collections.emptyMap();
        }
        if (!this.attributes.isEmpty() || !this.keywords.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String attribute : this.attributes) {
                sb.append(LanguageTag.SEP).append(attribute);
            }
            for (Entry<String, String> keyword : this.keywords.entrySet()) {
                String value = (String) keyword.getValue();
                sb.append(LanguageTag.SEP).append((String) keyword.getKey());
                if (value.length() > 0) {
                    sb.append(LanguageTag.SEP).append(value);
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
        return (String) this.keywords.get(unicodeLocaleKey);
    }

    public static boolean isSingletonChar(char c) {
        return SINGLETON == LocaleUtils.toLower(c);
    }

    public static boolean isAttribute(String s) {
        int len = s.length();
        return (len < 3 || len > 8) ? false : LocaleUtils.isAlphaNumericString(s);
    }

    public static boolean isKey(String s) {
        return s.length() == 2 ? LocaleUtils.isAlphaNumericString(s) : false;
    }

    public static boolean isTypeSubtag(String s) {
        int len = s.length();
        return (len < 3 || len > 8) ? false : LocaleUtils.isAlphaNumericString(s);
    }
}
