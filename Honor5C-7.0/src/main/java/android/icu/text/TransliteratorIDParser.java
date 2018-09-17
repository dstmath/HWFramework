package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.util.CaseInsensitiveString;
import dalvik.bytecode.Opcodes;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

class TransliteratorIDParser {
    private static final String ANY = "Any";
    private static final char CLOSE_REV = ')';
    private static final int FORWARD = 0;
    private static final char ID_DELIM = ';';
    private static final char OPEN_REV = '(';
    private static final int REVERSE = 1;
    private static final Map<CaseInsensitiveString, String> SPECIAL_INVERSES = null;
    private static final char TARGET_SEP = '-';
    private static final char VARIANT_SEP = '/';

    static class SingleID {
        public String basicID;
        public String canonID;
        public String filter;

        SingleID(String c, String b, String f) {
            this.canonID = c;
            this.basicID = b;
            this.filter = f;
        }

        SingleID(String c, String b) {
            this(c, b, null);
        }

        Transliterator getInstance() {
            Transliterator t;
            if (this.basicID == null || this.basicID.length() == 0) {
                t = Transliterator.getBasicInstance("Any-Null", this.canonID);
            } else {
                t = Transliterator.getBasicInstance(this.basicID, this.canonID);
            }
            if (!(t == null || this.filter == null)) {
                t.setFilter(new UnicodeSet(this.filter));
            }
            return t;
        }
    }

    private static class Specs {
        public String filter;
        public boolean sawSource;
        public String source;
        public String target;
        public String variant;

        Specs(String s, String t, String v, boolean sawS, String f) {
            this.source = s;
            this.target = t;
            this.variant = v;
            this.sawSource = sawS;
            this.filter = f;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.TransliteratorIDParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.TransliteratorIDParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.TransliteratorIDParser.<clinit>():void");
    }

    TransliteratorIDParser() {
    }

    public static SingleID parseFilterID(String id, int[] pos) {
        int start = pos[FORWARD];
        Specs specs = parseFilterID(id, pos, true);
        if (specs == null) {
            pos[FORWARD] = start;
            return null;
        }
        SingleID single = specsToID(specs, FORWARD);
        single.filter = specs.filter;
        return single;
    }

    public static SingleID parseSingleID(String id, int[] pos, int dir) {
        SingleID single;
        int start = pos[FORWARD];
        Specs specsA = null;
        Specs specs = null;
        boolean sawParen = false;
        for (int pass = REVERSE; pass <= 2; pass += REVERSE) {
            if (pass == 2) {
                specsA = parseFilterID(id, pos, true);
                if (specsA == null) {
                    pos[FORWARD] = start;
                    return null;
                }
            }
            if (Utility.parseChar(id, pos, OPEN_REV)) {
                sawParen = true;
                if (!Utility.parseChar(id, pos, CLOSE_REV)) {
                    specs = parseFilterID(id, pos, true);
                    if (specs == null || !Utility.parseChar(id, pos, CLOSE_REV)) {
                        pos[FORWARD] = start;
                        return null;
                    }
                }
                if (sawParen) {
                    if (dir != 0) {
                        single = specsToID(specsA, FORWARD);
                    } else {
                        single = specsToSpecialInverse(specsA);
                        if (single == null) {
                            single = specsToID(specsA, REVERSE);
                        }
                    }
                    single.filter = specsA.filter;
                } else if (dir != 0) {
                    single = specsToID(specsA, FORWARD);
                    single.canonID += OPEN_REV + specsToID(specs, FORWARD).canonID + CLOSE_REV;
                    if (specsA != null) {
                        single.filter = specsA.filter;
                    }
                } else {
                    single = specsToID(specs, FORWARD);
                    single.canonID += OPEN_REV + specsToID(specsA, FORWARD).canonID + CLOSE_REV;
                    if (specs != null) {
                        single.filter = specs.filter;
                    }
                }
                return single;
            }
        }
        if (sawParen) {
            if (dir != 0) {
                single = specsToSpecialInverse(specsA);
                if (single == null) {
                    single = specsToID(specsA, REVERSE);
                }
            } else {
                single = specsToID(specsA, FORWARD);
            }
            single.filter = specsA.filter;
        } else if (dir != 0) {
            single = specsToID(specs, FORWARD);
            single.canonID += OPEN_REV + specsToID(specsA, FORWARD).canonID + CLOSE_REV;
            if (specs != null) {
                single.filter = specs.filter;
            }
        } else {
            single = specsToID(specsA, FORWARD);
            single.canonID += OPEN_REV + specsToID(specs, FORWARD).canonID + CLOSE_REV;
            if (specsA != null) {
                single.filter = specsA.filter;
            }
        }
        return single;
    }

    public static UnicodeSet parseGlobalFilter(String id, int[] pos, int dir, int[] withParens, StringBuffer canonID) {
        UnicodeSet filter = null;
        int start = pos[FORWARD];
        if (withParens[FORWARD] == -1) {
            withParens[FORWARD] = Utility.parseChar(id, pos, OPEN_REV) ? REVERSE : FORWARD;
        } else if (withParens[FORWARD] == REVERSE && !Utility.parseChar(id, pos, OPEN_REV)) {
            pos[FORWARD] = start;
            return null;
        }
        pos[FORWARD] = PatternProps.skipWhiteSpace(id, pos[FORWARD]);
        if (UnicodeSet.resemblesPattern(id, pos[FORWARD])) {
            ParsePosition ppos = new ParsePosition(pos[FORWARD]);
            try {
                filter = new UnicodeSet(id, ppos, null);
                String pattern = id.substring(pos[FORWARD], ppos.getIndex());
                pos[FORWARD] = ppos.getIndex();
                if (withParens[FORWARD] == REVERSE && !Utility.parseChar(id, pos, CLOSE_REV)) {
                    pos[FORWARD] = start;
                    return null;
                } else if (canonID != null) {
                    if (dir == 0) {
                        if (withParens[FORWARD] == REVERSE) {
                            pattern = String.valueOf(OPEN_REV) + pattern + CLOSE_REV;
                        }
                        canonID.append(pattern + ID_DELIM);
                    } else {
                        if (withParens[FORWARD] == 0) {
                            pattern = String.valueOf(OPEN_REV) + pattern + CLOSE_REV;
                        }
                        canonID.insert(FORWARD, pattern + ID_DELIM);
                    }
                }
            } catch (IllegalArgumentException e) {
                pos[FORWARD] = start;
                return null;
            }
        }
        return filter;
    }

    public static boolean parseCompoundID(String id, int dir, StringBuffer canonID, List<SingleID> list, UnicodeSet[] globalFilter) {
        int[] pos = new int[REVERSE];
        pos[FORWARD] = FORWARD;
        int[] withParens = new int[REVERSE];
        list.clear();
        globalFilter[FORWARD] = null;
        canonID.setLength(FORWARD);
        withParens[FORWARD] = FORWARD;
        UnicodeSet filter = parseGlobalFilter(id, pos, dir, withParens, canonID);
        if (filter != null) {
            if (!Utility.parseChar(id, pos, ID_DELIM)) {
                canonID.setLength(FORWARD);
                pos[FORWARD] = FORWARD;
            }
            if (dir == 0) {
                globalFilter[FORWARD] = filter;
            }
        }
        boolean sawDelimiter = true;
        do {
            SingleID single = parseSingleID(id, pos, dir);
            if (single == null) {
                break;
            } else if (dir == 0) {
                list.add(single);
            } else {
                list.add(FORWARD, single);
            }
        } while (Utility.parseChar(id, pos, ID_DELIM));
        sawDelimiter = false;
        if (list.size() == 0) {
            return false;
        }
        for (int i = FORWARD; i < list.size(); i += REVERSE) {
            canonID.append(((SingleID) list.get(i)).canonID);
            if (i != list.size() - 1) {
                canonID.append(ID_DELIM);
            }
        }
        if (sawDelimiter) {
            withParens[FORWARD] = REVERSE;
            filter = parseGlobalFilter(id, pos, dir, withParens, canonID);
            if (filter != null) {
                Utility.parseChar(id, pos, ID_DELIM);
                if (dir == REVERSE) {
                    globalFilter[FORWARD] = filter;
                }
            }
        }
        pos[FORWARD] = PatternProps.skipWhiteSpace(id, pos[FORWARD]);
        return pos[FORWARD] == id.length();
    }

    static List<Transliterator> instantiateList(List<SingleID> ids) {
        List<Transliterator> translits = new ArrayList();
        for (SingleID single : ids) {
            Transliterator t;
            if (single.basicID.length() != 0) {
                t = single.getInstance();
                if (t == null) {
                    throw new IllegalArgumentException("Illegal ID " + single.canonID);
                }
                translits.add(t);
            }
        }
        if (translits.size() == 0) {
            t = Transliterator.getBasicInstance("Any-Null", null);
            if (t == null) {
                throw new IllegalArgumentException("Internal error; cannot instantiate Any-Null");
            }
            translits.add(t);
        }
        return translits;
    }

    public static String[] IDtoSTV(String id) {
        String target;
        String source = ANY;
        String variant = XmlPullParser.NO_NAMESPACE;
        int sep = id.indexOf(45);
        int var = id.indexOf(47);
        if (var < 0) {
            var = id.length();
        }
        boolean isSourcePresent = false;
        if (sep < 0) {
            target = id.substring(FORWARD, var);
            variant = id.substring(var);
        } else if (sep < var) {
            if (sep > 0) {
                source = id.substring(FORWARD, sep);
                isSourcePresent = true;
            }
            target = id.substring(sep + REVERSE, var);
            variant = id.substring(var);
        } else {
            if (var > 0) {
                source = id.substring(FORWARD, var);
                isSourcePresent = true;
            }
            int sep2 = sep + REVERSE;
            variant = id.substring(var, sep);
            target = id.substring(sep2);
            sep = sep2;
        }
        if (variant.length() > 0) {
            variant = variant.substring(REVERSE);
        }
        String[] strArr = new String[4];
        strArr[FORWARD] = source;
        strArr[REVERSE] = target;
        strArr[2] = variant;
        strArr[3] = isSourcePresent ? XmlPullParser.NO_NAMESPACE : null;
        return strArr;
    }

    public static String STVtoID(String source, String target, String variant) {
        StringBuilder id = new StringBuilder(source);
        if (id.length() == 0) {
            id.append(ANY);
        }
        id.append(TARGET_SEP).append(target);
        if (!(variant == null || variant.length() == 0)) {
            id.append(VARIANT_SEP).append(variant);
        }
        return id.toString();
    }

    public static void registerSpecialInverse(String target, String inverseTarget, boolean bidirectional) {
        SPECIAL_INVERSES.put(new CaseInsensitiveString(target), inverseTarget);
        if (bidirectional && !target.equalsIgnoreCase(inverseTarget)) {
            SPECIAL_INVERSES.put(new CaseInsensitiveString(inverseTarget), target);
        }
    }

    private static Specs parseFilterID(String id, int[] pos, boolean allowFilter) {
        String first = null;
        String source = null;
        String target = null;
        String variant = null;
        String filter = null;
        char delimiter = '\u0000';
        int specCount = FORWARD;
        int start = pos[FORWARD];
        while (true) {
            pos[FORWARD] = PatternProps.skipWhiteSpace(id, pos[FORWARD]);
            if (pos[FORWARD] != id.length()) {
                if (allowFilter && filter == null && UnicodeSet.resemblesPattern(id, pos[FORWARD])) {
                    ParsePosition ppos = new ParsePosition(pos[FORWARD]);
                    UnicodeSet unicodeSet = new UnicodeSet(id, ppos, null);
                    filter = id.substring(pos[FORWARD], ppos.getIndex());
                    pos[FORWARD] = ppos.getIndex();
                } else {
                    if (delimiter == '\u0000') {
                        char c = id.charAt(pos[FORWARD]);
                        if ((c == TARGET_SEP && target == null) || (c == VARIANT_SEP && variant == null)) {
                            delimiter = c;
                            pos[FORWARD] = pos[FORWARD] + REVERSE;
                        }
                    }
                    if (delimiter != '\u0000' || specCount <= 0) {
                        String spec = Utility.parseUnicodeIdentifier(id, pos);
                        if (spec != null) {
                            switch (delimiter) {
                                case FORWARD /*0*/:
                                    first = spec;
                                    break;
                                case Opcodes.OP_CMPL_FLOAT /*45*/:
                                    target = spec;
                                    break;
                                case Opcodes.OP_CMPL_DOUBLE /*47*/:
                                    variant = spec;
                                    break;
                            }
                            specCount += REVERSE;
                            delimiter = '\u0000';
                        }
                    }
                }
            }
            if (first != null) {
                if (target == null) {
                    target = first;
                } else {
                    source = first;
                }
            }
            if (source == null && target == null) {
                pos[FORWARD] = start;
                return null;
            }
            boolean sawSource = true;
            if (source == null) {
                source = ANY;
                sawSource = false;
            }
            if (target == null) {
                target = ANY;
            }
            return new Specs(source, target, variant, sawSource, filter);
        }
    }

    private static SingleID specsToID(Specs specs, int dir) {
        String canonID = XmlPullParser.NO_NAMESPACE;
        String basicID = XmlPullParser.NO_NAMESPACE;
        String basicPrefix = XmlPullParser.NO_NAMESPACE;
        if (specs != null) {
            StringBuilder buf = new StringBuilder();
            if (dir == 0) {
                if (specs.sawSource) {
                    buf.append(specs.source).append(TARGET_SEP);
                } else {
                    basicPrefix = specs.source + TARGET_SEP;
                }
                buf.append(specs.target);
            } else {
                buf.append(specs.target).append(TARGET_SEP).append(specs.source);
            }
            if (specs.variant != null) {
                buf.append(VARIANT_SEP).append(specs.variant);
            }
            basicID = basicPrefix + buf.toString();
            if (specs.filter != null) {
                buf.insert(FORWARD, specs.filter);
            }
            canonID = buf.toString();
        }
        return new SingleID(canonID, basicID);
    }

    private static SingleID specsToSpecialInverse(Specs specs) {
        if (!specs.source.equalsIgnoreCase(ANY)) {
            return null;
        }
        String inverseTarget = (String) SPECIAL_INVERSES.get(new CaseInsensitiveString(specs.target));
        if (inverseTarget == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        if (specs.filter != null) {
            buf.append(specs.filter);
        }
        if (specs.sawSource) {
            buf.append(ANY).append(TARGET_SEP);
        }
        buf.append(inverseTarget);
        String basicID = "Any-" + inverseTarget;
        if (specs.variant != null) {
            buf.append(VARIANT_SEP).append(specs.variant);
            basicID = basicID + VARIANT_SEP + specs.variant;
        }
        return new SingleID(buf.toString(), basicID);
    }
}
