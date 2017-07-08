package android.os;

import android.os.Parcelable.Creator;

public class PatternMatcher implements Parcelable {
    public static final Creator<PatternMatcher> CREATOR = null;
    public static final int PATTERN_LITERAL = 0;
    public static final int PATTERN_PREFIX = 1;
    public static final int PATTERN_SIMPLE_GLOB = 2;
    private final String mPattern;
    private final int mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.PatternMatcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.PatternMatcher.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.PatternMatcher.<clinit>():void");
    }

    public PatternMatcher(String pattern, int type) {
        this.mPattern = pattern;
        this.mType = type;
    }

    public final String getPath() {
        return this.mPattern;
    }

    public final int getType() {
        return this.mType;
    }

    public boolean match(String str) {
        return matchPattern(this.mPattern, str, this.mType);
    }

    public String toString() {
        String type = "? ";
        switch (this.mType) {
            case PATTERN_LITERAL /*0*/:
                type = "LITERAL: ";
                break;
            case PATTERN_PREFIX /*1*/:
                type = "PREFIX: ";
                break;
            case PATTERN_SIMPLE_GLOB /*2*/:
                type = "GLOB: ";
                break;
        }
        return "PatternMatcher{" + type + this.mPattern + "}";
    }

    public int describeContents() {
        return PATTERN_LITERAL;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPattern);
        dest.writeInt(this.mType);
    }

    public PatternMatcher(Parcel src) {
        this.mPattern = src.readString();
        this.mType = src.readInt();
    }

    static boolean matchPattern(String pattern, String match, int type) {
        boolean z = true;
        if (match == null) {
            return false;
        }
        if (type == 0) {
            return pattern.equals(match);
        }
        if (type == PATTERN_PREFIX) {
            return match.startsWith(pattern);
        }
        if (type != PATTERN_SIMPLE_GLOB) {
            return false;
        }
        int NP = pattern.length();
        if (NP <= 0) {
            if (match.length() > 0) {
                z = false;
            }
            return z;
        }
        int NM = match.length();
        int ip = PATTERN_LITERAL;
        int im = PATTERN_LITERAL;
        char charAt = pattern.charAt(PATTERN_LITERAL);
        while (ip < NP && im < NM) {
            boolean escaped;
            char c = charAt;
            ip += PATTERN_PREFIX;
            charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
            if (c == '\\') {
                escaped = true;
            } else {
                escaped = false;
            }
            if (escaped) {
                c = charAt;
                ip += PATTERN_PREFIX;
                charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
            }
            if (charAt == '*') {
                if (escaped || c != '.') {
                    while (match.charAt(im) == c) {
                        im += PATTERN_PREFIX;
                        if (im >= NM) {
                            break;
                        }
                    }
                    ip += PATTERN_PREFIX;
                    charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
                } else if (ip >= NP - 1) {
                    return true;
                } else {
                    ip += PATTERN_PREFIX;
                    charAt = pattern.charAt(ip);
                    if (charAt == '\\') {
                        ip += PATTERN_PREFIX;
                        charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
                    }
                    while (match.charAt(im) != charAt) {
                        im += PATTERN_PREFIX;
                        if (im >= NM) {
                            break;
                        }
                    }
                    if (im == NM) {
                        return false;
                    }
                    ip += PATTERN_PREFIX;
                    charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
                    im += PATTERN_PREFIX;
                }
            } else if (c != '.' && match.charAt(im) != c) {
                return false;
            } else {
                im += PATTERN_PREFIX;
            }
        }
        if (ip < NP || im < NM) {
            return ip == NP + -2 && pattern.charAt(ip) == '.' && pattern.charAt(ip + PATTERN_PREFIX) == '*';
        } else {
            return true;
        }
    }
}
