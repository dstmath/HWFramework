package android.icu.impl;

import android.icu.text.PluralRules;
import java.util.List;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public enum StandardPlural {
    ;
    
    public static final int COUNT = 0;
    public static final int OTHER_INDEX = 0;
    public static final List<StandardPlural> VALUES = null;
    private final String keyword;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.StandardPlural.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.StandardPlural.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.StandardPlural.<clinit>():void");
    }

    private StandardPlural(String kw) {
        this.keyword = kw;
    }

    public final String getKeyword() {
        return this.keyword;
    }

    public static final StandardPlural orNullFromString(CharSequence keyword) {
        switch (keyword.length()) {
            case XmlPullParser.END_TAG /*3*/:
                if (PluralRules.KEYWORD_ONE.contentEquals(keyword)) {
                    return ONE;
                }
                if (PluralRules.KEYWORD_TWO.contentEquals(keyword)) {
                    return TWO;
                }
                if (PluralRules.KEYWORD_FEW.contentEquals(keyword)) {
                    return FEW;
                }
                break;
            case NodeFilter.SHOW_TEXT /*4*/:
                if (PluralRules.KEYWORD_MANY.contentEquals(keyword)) {
                    return MANY;
                }
                if (PluralRules.KEYWORD_ZERO.contentEquals(keyword)) {
                    return ZERO;
                }
                break;
            case XmlPullParser.CDSECT /*5*/:
                if (PluralRules.KEYWORD_OTHER.contentEquals(keyword)) {
                    return OTHER;
                }
                break;
        }
        return null;
    }

    public static final StandardPlural orOtherFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p : OTHER;
    }

    public static final StandardPlural fromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        if (p != null) {
            return p;
        }
        throw new IllegalArgumentException(keyword.toString());
    }

    public static final int indexOrNegativeFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p.ordinal() : -1;
    }

    public static final int indexOrOtherIndexFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p.ordinal() : OTHER.ordinal();
    }

    public static final int indexFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        if (p != null) {
            return p.ordinal();
        }
        throw new IllegalArgumentException(keyword.toString());
    }
}
