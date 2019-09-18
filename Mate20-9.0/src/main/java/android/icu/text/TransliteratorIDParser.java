package android.icu.text;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.util.CaseInsensitiveString;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TransliteratorIDParser {
    private static final String ANY = "Any";
    private static final char CLOSE_REV = ')';
    private static final int FORWARD = 0;
    private static final char ID_DELIM = ';';
    private static final char OPEN_REV = '(';
    private static final int REVERSE = 1;
    private static final Map<CaseInsensitiveString, String> SPECIAL_INVERSES = Collections.synchronizedMap(new HashMap());
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

        /* access modifiers changed from: package-private */
        public Transliterator getInstance() {
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

    TransliteratorIDParser() {
    }

    public static SingleID parseFilterID(String id, int[] pos) {
        int start = pos[0];
        Specs specs = parseFilterID(id, pos, true);
        if (specs == null) {
            pos[0] = start;
            return null;
        }
        SingleID single = specsToID(specs, 0);
        single.filter = specs.filter;
        return single;
    }

    public static SingleID parseSingleID(String id, int[] pos, int dir) {
        SingleID single;
        SingleID single2;
        int start = pos[0];
        Specs specsB = null;
        boolean sawParen = false;
        Specs specsA = null;
        int pass = 1;
        while (true) {
            if (pass > 2) {
                break;
            }
            if (pass == 2) {
                specsA = parseFilterID(id, pos, true);
                if (specsA == null) {
                    pos[0] = start;
                    return null;
                }
            }
            if (Utility.parseChar(id, pos, OPEN_REV)) {
                sawParen = true;
                if (!Utility.parseChar(id, pos, CLOSE_REV)) {
                    specsB = parseFilterID(id, pos, true);
                    if (specsB == null || !Utility.parseChar(id, pos, CLOSE_REV)) {
                        pos[0] = start;
                        return null;
                    }
                }
            } else {
                pass++;
            }
        }
        if (!sawParen) {
            if (dir == 0) {
                single2 = specsToID(specsA, 0);
            } else {
                single2 = specsToSpecialInverse(specsA);
                if (single2 == null) {
                    single2 = specsToID(specsA, 1);
                }
            }
            single = single2;
            single.filter = specsA.filter;
        } else if (dir == 0) {
            single = specsToID(specsA, 0);
            single.canonID += OPEN_REV + specsToID(specsB, 0).canonID + CLOSE_REV;
            if (specsA != null) {
                single.filter = specsA.filter;
            }
        } else {
            single = specsToID(specsB, 0);
            single.canonID += OPEN_REV + specsToID(specsA, 0).canonID + CLOSE_REV;
            if (specsB != null) {
                single.filter = specsB.filter;
            }
        }
        return single;
    }

    public static UnicodeSet parseGlobalFilter(String id, int[] pos, int dir, int[] withParens, StringBuffer canonID) {
        UnicodeSet filter = null;
        int start = pos[0];
        if (withParens[0] == -1) {
            withParens[0] = Utility.parseChar(id, pos, OPEN_REV);
        } else if (withParens[0] == 1 && !Utility.parseChar(id, pos, OPEN_REV)) {
            pos[0] = start;
            return null;
        }
        pos[0] = PatternProps.skipWhiteSpace(id, pos[0]);
        if (UnicodeSet.resemblesPattern(id, pos[0])) {
            ParsePosition ppos = new ParsePosition(pos[0]);
            try {
                filter = new UnicodeSet(id, ppos, null);
                String pattern = id.substring(pos[0], ppos.getIndex());
                pos[0] = ppos.getIndex();
                if (withParens[0] == 1 && !Utility.parseChar(id, pos, CLOSE_REV)) {
                    pos[0] = start;
                    return null;
                } else if (canonID != null) {
                    if (dir == 0) {
                        if (withParens[0] == 1) {
                            pattern = String.valueOf(OPEN_REV) + pattern + CLOSE_REV;
                        }
                        canonID.append(pattern + ID_DELIM);
                    } else {
                        if (withParens[0] == 0) {
                            pattern = String.valueOf(OPEN_REV) + pattern + CLOSE_REV;
                        }
                        canonID.insert(0, pattern + ID_DELIM);
                    }
                }
            } catch (IllegalArgumentException e) {
                pos[0] = start;
                return null;
            }
        }
        return filter;
    }

    public static boolean parseCompoundID(String id, int dir, StringBuffer canonID, List<SingleID> list, UnicodeSet[] globalFilter) {
        int[] pos = {0};
        list.clear();
        globalFilter[0] = null;
        canonID.setLength(0);
        int[] withParens = {0};
        UnicodeSet filter = parseGlobalFilter(id, pos, dir, withParens, canonID);
        if (filter != null) {
            if (!Utility.parseChar(id, pos, ID_DELIM)) {
                canonID.setLength(0);
                pos[0] = 0;
            }
            if (dir == 0) {
                globalFilter[0] = filter;
            }
        }
        boolean sawDelimiter = true;
        while (true) {
            SingleID single = parseSingleID(id, pos, dir);
            if (single == null) {
                break;
            }
            if (dir == 0) {
                list.add(single);
            } else {
                list.add(0, single);
            }
            if (!Utility.parseChar(id, pos, ID_DELIM)) {
                sawDelimiter = false;
                break;
            }
        }
        if (list.size() == 0) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            canonID.append(list.get(i).canonID);
            if (i != list.size() - 1) {
                canonID.append(ID_DELIM);
            }
        }
        if (sawDelimiter) {
            withParens[0] = 1;
            UnicodeSet filter2 = parseGlobalFilter(id, pos, dir, withParens, canonID);
            if (filter2 != null) {
                Utility.parseChar(id, pos, ID_DELIM);
                if (dir == 1) {
                    globalFilter[0] = filter2;
                }
            }
        }
        pos[0] = PatternProps.skipWhiteSpace(id, pos[0]);
        return pos[0] == id.length();
    }

    static List<Transliterator> instantiateList(List<SingleID> ids) {
        List<Transliterator> translits = new ArrayList<>();
        for (SingleID single : ids) {
            if (single.basicID.length() != 0) {
                Transliterator t = single.getInstance();
                if (t != null) {
                    translits.add(t);
                } else {
                    throw new IllegalArgumentException("Illegal ID " + single.canonID);
                }
            }
        }
        if (translits.size() == 0) {
            Transliterator t2 = Transliterator.getBasicInstance("Any-Null", null);
            if (t2 != null) {
                translits.add(t2);
            } else {
                throw new IllegalArgumentException("Internal error; cannot instantiate Any-Null");
            }
        }
        return translits;
    }

    public static String[] IDtoSTV(String id) {
        String variant;
        String target;
        String source = ANY;
        int sep = id.indexOf(45);
        int var = id.indexOf(47);
        if (var < 0) {
            var = id.length();
        }
        boolean isSourcePresent = false;
        if (sep < 0) {
            target = id.substring(0, var);
            variant = id.substring(var);
        } else if (sep < var) {
            if (sep > 0) {
                source = id.substring(0, sep);
                isSourcePresent = true;
            }
            target = id.substring(sep + 1, var);
            variant = id.substring(var);
        } else {
            if (var > 0) {
                source = id.substring(0, var);
                isSourcePresent = true;
            }
            int sep2 = sep + 1;
            variant = id.substring(var, sep);
            target = id.substring(sep2);
            int i = sep2;
        }
        if (variant.length() > 0) {
            variant = variant.substring(1);
        }
        String[] strArr = new String[4];
        strArr[0] = source;
        strArr[1] = target;
        strArr[2] = variant;
        strArr[3] = isSourcePresent ? "" : null;
        return strArr;
    }

    public static String STVtoID(String source, String target, String variant) {
        StringBuilder id = new StringBuilder(source);
        if (id.length() == 0) {
            id.append(ANY);
        }
        id.append(TARGET_SEP);
        id.append(target);
        if (!(variant == null || variant.length() == 0)) {
            id.append(VARIANT_SEP);
            id.append(variant);
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
        String str = id;
        String first = null;
        String source = null;
        String target = null;
        String variant = null;
        String filter = null;
        char delimiter = 0;
        int specCount = 0;
        int start = pos[0];
        while (true) {
            pos[0] = PatternProps.skipWhiteSpace(str, pos[0]);
            if (pos[0] == id.length()) {
                break;
            } else if (!allowFilter || filter != null || !UnicodeSet.resemblesPattern(str, pos[0])) {
                if (delimiter == 0) {
                    char c = str.charAt(pos[0]);
                    if ((c == '-' && target == null) || (c == '/' && variant == null)) {
                        delimiter = c;
                        pos[0] = pos[0] + 1;
                    }
                }
                if (delimiter == 0 && specCount > 0) {
                    break;
                }
                String spec = Utility.parseUnicodeIdentifier(id, pos);
                if (spec == null) {
                    break;
                }
                if (delimiter == 0) {
                    first = spec;
                } else if (delimiter == '-') {
                    target = spec;
                } else if (delimiter == '/') {
                    variant = spec;
                }
                specCount++;
                delimiter = 0;
            } else {
                ParsePosition ppos = new ParsePosition(pos[0]);
                new UnicodeSet(str, ppos, null);
                filter = str.substring(pos[0], ppos.getIndex());
                pos[0] = ppos.getIndex();
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
            pos[0] = start;
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
        Specs specs = new Specs(source, target, variant, sawSource, filter);
        return specs;
    }

    private static SingleID specsToID(Specs specs, int dir) {
        String canonID = "";
        String basicID = "";
        String basicPrefix = "";
        if (specs != null) {
            StringBuilder buf = new StringBuilder();
            if (dir == 0) {
                if (specs.sawSource) {
                    buf.append(specs.source);
                    buf.append(TARGET_SEP);
                } else {
                    basicPrefix = specs.source + TARGET_SEP;
                }
                buf.append(specs.target);
            } else {
                buf.append(specs.target);
                buf.append(TARGET_SEP);
                buf.append(specs.source);
            }
            if (specs.variant != null) {
                buf.append(VARIANT_SEP);
                buf.append(specs.variant);
            }
            basicID = basicPrefix + buf.toString();
            if (specs.filter != null) {
                buf.insert(0, specs.filter);
            }
            canonID = buf.toString();
        }
        return new SingleID(canonID, basicID);
    }

    private static SingleID specsToSpecialInverse(Specs specs) {
        if (!specs.source.equalsIgnoreCase(ANY)) {
            return null;
        }
        String inverseTarget = SPECIAL_INVERSES.get(new CaseInsensitiveString(specs.target));
        if (inverseTarget == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        if (specs.filter != null) {
            buf.append(specs.filter);
        }
        if (specs.sawSource) {
            buf.append(ANY);
            buf.append(TARGET_SEP);
        }
        buf.append(inverseTarget);
        String basicID = "Any-" + inverseTarget;
        if (specs.variant != null) {
            buf.append(VARIANT_SEP);
            buf.append(specs.variant);
            basicID = basicID + VARIANT_SEP + specs.variant;
        }
        return new SingleID(buf.toString(), basicID);
    }
}
