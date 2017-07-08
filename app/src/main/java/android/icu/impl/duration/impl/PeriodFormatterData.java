package android.icu.impl.duration.impl;

import android.icu.impl.Grego;
import android.icu.impl.duration.TimeUnit;
import android.icu.impl.duration.impl.DataRecord.ScopeData;
import android.icu.impl.duration.impl.Utils.ChineseDigits;
import android.icu.text.BreakIterator;
import java.util.Arrays;

public class PeriodFormatterData {
    private static final int FORM_DUAL = 2;
    private static final int FORM_HALF_SPELLED = 6;
    private static final int FORM_PAUCAL = 3;
    private static final int FORM_PLURAL = 0;
    private static final int FORM_SINGULAR = 1;
    private static final int FORM_SINGULAR_NO_OMIT = 5;
    private static final int FORM_SINGULAR_SPELLED = 4;
    public static boolean trace;
    final DataRecord dr;
    String localeName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.PeriodFormatterData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.PeriodFormatterData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.PeriodFormatterData.<clinit>():void");
    }

    public PeriodFormatterData(String localeName, DataRecord dr) {
        this.dr = dr;
        this.localeName = localeName;
        if (localeName == null) {
            throw new NullPointerException("localename is null");
        } else if (dr == null) {
            throw new NullPointerException("data record is null");
        }
    }

    public int pluralization() {
        return this.dr.pl;
    }

    public boolean allowZero() {
        return this.dr.allowZero;
    }

    public boolean weeksAloneOnly() {
        return this.dr.weeksAloneOnly;
    }

    public int useMilliseconds() {
        return this.dr.useMilliseconds;
    }

    public boolean appendPrefix(int tl, int td, StringBuffer sb) {
        if (this.dr.scopeData != null) {
            ScopeData sd = this.dr.scopeData[(tl * FORM_PAUCAL) + td];
            if (sd != null) {
                String prefix = sd.prefix;
                if (prefix != null) {
                    sb.append(prefix);
                    return sd.requiresDigitPrefix;
                }
            }
        }
        return false;
    }

    public void appendSuffix(int tl, int td, StringBuffer sb) {
        if (this.dr.scopeData != null) {
            ScopeData sd = this.dr.scopeData[(tl * FORM_PAUCAL) + td];
            if (sd != null) {
                String suffix = sd.suffix;
                if (suffix != null) {
                    if (trace) {
                        System.out.println("appendSuffix '" + suffix + "'");
                    }
                    sb.append(suffix);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean appendUnit(TimeUnit unit, int count, int cv, int uv, boolean useCountSep, boolean useDigitPrefix, boolean multiple, boolean last, boolean wasSkipped, StringBuffer sb) {
        String name;
        int px = unit.ordinal();
        boolean willRequireSkipMarker = false;
        if (!(this.dr.requiresSkipMarker == null || !this.dr.requiresSkipMarker[px] || this.dr.skippedUnitMarker == null)) {
            if (!wasSkipped && last) {
                sb.append(this.dr.skippedUnitMarker);
            }
            willRequireSkipMarker = true;
        }
        if (uv != 0) {
            boolean useMedium = uv == FORM_SINGULAR;
            String[] names = useMedium ? this.dr.mediumNames : this.dr.shortNames;
            if (names == null || names[px] == null) {
                names = useMedium ? this.dr.shortNames : this.dr.mediumNames;
            }
            if (!(names == null || names[px] == null)) {
                appendCount(unit, false, false, count, cv, useCountSep, names[px], last, sb);
                return false;
            }
        }
        if (cv == FORM_DUAL && this.dr.halfSupport != null) {
            switch (this.dr.halfSupport[px]) {
                case FORM_DUAL /*2*/:
                    break;
                case FORM_SINGULAR /*1*/:
                    count = (count / BreakIterator.WORD_IDEO_LIMIT) * BreakIterator.WORD_IDEO_LIMIT;
                    cv = FORM_PAUCAL;
                    break;
            }
        }
        int form = computeForm(unit, count, cv, multiple ? last : false);
        if (form == FORM_SINGULAR_SPELLED) {
            if (this.dr.singularNames == null) {
                form = FORM_SINGULAR;
                name = this.dr.pluralNames[px][FORM_SINGULAR];
            } else {
                name = this.dr.singularNames[px];
            }
        } else if (form == FORM_SINGULAR_NO_OMIT) {
            name = this.dr.pluralNames[px][FORM_SINGULAR];
        } else if (form == FORM_HALF_SPELLED) {
            name = this.dr.halfNames[px];
        } else {
            try {
                name = this.dr.pluralNames[px][form];
            } catch (NullPointerException e) {
                System.out.println("Null Pointer in PeriodFormatterData[" + this.localeName + "].au px: " + px + " form: " + form + " pn: " + Arrays.toString(this.dr.pluralNames));
                throw e;
            }
        }
        if (name == null) {
            form = FORM_PLURAL;
            name = this.dr.pluralNames[px][FORM_PLURAL];
        }
        boolean omitCount = (form == FORM_SINGULAR_SPELLED || form == FORM_HALF_SPELLED || (this.dr.omitSingularCount && form == FORM_SINGULAR)) ? true : this.dr.omitDualCount && form == FORM_DUAL;
        int suffixIndex = appendCount(unit, omitCount, useDigitPrefix, count, cv, useCountSep, name, last, sb);
        if (last && suffixIndex >= 0) {
            String suffix = null;
            if (this.dr.rqdSuffixes != null && suffixIndex < this.dr.rqdSuffixes.length) {
                suffix = this.dr.rqdSuffixes[suffixIndex];
            }
            if (suffix == null && this.dr.optSuffixes != null && suffixIndex < this.dr.optSuffixes.length) {
                suffix = this.dr.optSuffixes[suffixIndex];
            }
            if (suffix != null) {
                sb.append(suffix);
            }
        }
        return willRequireSkipMarker;
    }

    public int appendCount(TimeUnit unit, boolean omitCount, boolean useDigitPrefix, int count, int cv, boolean useSep, String name, boolean last, StringBuffer sb) {
        String str;
        if (cv == FORM_DUAL && this.dr.halves == null) {
            cv = FORM_PLURAL;
        }
        if (!(omitCount || !useDigitPrefix || this.dr.digitPrefix == null)) {
            sb.append(this.dr.digitPrefix);
        }
        int index = unit.ordinal();
        switch (cv) {
            case FORM_PLURAL /*0*/:
                if (!omitCount) {
                    appendInteger(count / Grego.MILLIS_PER_SECOND, FORM_SINGULAR, 10, sb);
                    break;
                }
                break;
            case FORM_SINGULAR /*1*/:
                int val = count / Grego.MILLIS_PER_SECOND;
                if (unit == TimeUnit.MINUTE && !((this.dr.fiveMinutes == null && this.dr.fifteenMinutes == null) || val == 0 || val % FORM_SINGULAR_NO_OMIT != 0)) {
                    if (this.dr.fifteenMinutes == null || (val != 15 && val != 45)) {
                        if (this.dr.fiveMinutes != null) {
                            val /= FORM_SINGULAR_NO_OMIT;
                            if (!omitCount) {
                                appendInteger(val, FORM_SINGULAR, 10, sb);
                            }
                            name = this.dr.fiveMinutes;
                            index = 9;
                            break;
                        }
                    }
                    val = val == 15 ? FORM_SINGULAR : FORM_PAUCAL;
                    if (!omitCount) {
                        appendInteger(val, FORM_SINGULAR, 10, sb);
                    }
                    name = this.dr.fifteenMinutes;
                    index = 8;
                    break;
                }
                if (!omitCount) {
                    appendInteger(val, FORM_SINGULAR, 10, sb);
                    break;
                }
                break;
            case FORM_DUAL /*2*/:
                int v = count / BreakIterator.WORD_IDEO_LIMIT;
                if (!(v == FORM_SINGULAR || omitCount)) {
                    appendCountValue(count, FORM_SINGULAR, FORM_PLURAL, sb);
                }
                if ((v & FORM_SINGULAR) == FORM_SINGULAR) {
                    if (v != FORM_SINGULAR || this.dr.halfNames == null || this.dr.halfNames[index] == null) {
                        int hp;
                        int solox = v == FORM_SINGULAR ? FORM_PLURAL : FORM_SINGULAR;
                        if (this.dr.genders != null && this.dr.halves.length > FORM_DUAL && this.dr.genders[index] == FORM_SINGULAR) {
                            solox += FORM_DUAL;
                        }
                        if (this.dr.halfPlacements == null) {
                            hp = FORM_PLURAL;
                        } else {
                            hp = this.dr.halfPlacements[solox & FORM_SINGULAR];
                        }
                        String half = this.dr.halves[solox];
                        str = this.dr.measures == null ? null : this.dr.measures[index];
                        switch (hp) {
                            case FORM_PLURAL /*0*/:
                                sb.append(half);
                                break;
                            case FORM_SINGULAR /*1*/:
                                if (str != null) {
                                    sb.append(str);
                                    sb.append(half);
                                    if (useSep && !omitCount) {
                                        sb.append(this.dr.countSep);
                                    }
                                    sb.append(name);
                                    return -1;
                                }
                                sb.append(name);
                                sb.append(half);
                                if (!last) {
                                    index = -1;
                                }
                                return index;
                            case FORM_DUAL /*2*/:
                                if (str != null) {
                                    sb.append(str);
                                }
                                if (useSep && !omitCount) {
                                    sb.append(this.dr.countSep);
                                }
                                sb.append(name);
                                sb.append(half);
                                if (!last) {
                                    index = -1;
                                }
                                return index;
                            default:
                                break;
                        }
                    }
                    sb.append(name);
                    if (!last) {
                        index = -1;
                    }
                    return index;
                }
                break;
            default:
                int decimals = FORM_SINGULAR;
                switch (cv) {
                    case FORM_SINGULAR_SPELLED /*4*/:
                        decimals = FORM_DUAL;
                        break;
                    case FORM_SINGULAR_NO_OMIT /*5*/:
                        decimals = FORM_PAUCAL;
                        break;
                }
                if (!omitCount) {
                    appendCountValue(count, FORM_SINGULAR, decimals, sb);
                    break;
                }
                break;
        }
        if (!omitCount && useSep) {
            sb.append(this.dr.countSep);
        }
        if (!(omitCount || this.dr.measures == null || index >= this.dr.measures.length)) {
            str = this.dr.measures[index];
            if (str != null) {
                sb.append(str);
            }
        }
        sb.append(name);
        if (!last) {
            index = -1;
        }
        return index;
    }

    public void appendCountValue(int count, int integralDigits, int decimalDigits, StringBuffer sb) {
        int ival = count / Grego.MILLIS_PER_SECOND;
        if (decimalDigits == 0) {
            appendInteger(ival, integralDigits, 10, sb);
            return;
        }
        if (this.dr.requiresDigitSeparator && sb.length() > 0) {
            sb.append(' ');
        }
        appendDigits((long) ival, integralDigits, 10, sb);
        int dval = count % Grego.MILLIS_PER_SECOND;
        if (decimalDigits == FORM_SINGULAR) {
            dval /= 100;
        } else if (decimalDigits == FORM_DUAL) {
            dval /= 10;
        }
        sb.append(this.dr.decimalSep);
        appendDigits((long) dval, decimalDigits, decimalDigits, sb);
        if (this.dr.requiresDigitSeparator) {
            sb.append(' ');
        }
    }

    public void appendInteger(int num, int mindigits, int maxdigits, StringBuffer sb) {
        if (this.dr.numberNames != null && num < this.dr.numberNames.length) {
            String name = this.dr.numberNames[num];
            if (name != null) {
                sb.append(name);
                return;
            }
        }
        if (this.dr.requiresDigitSeparator && sb.length() > 0) {
            sb.append(' ');
        }
        switch (this.dr.numberSystem) {
            case FORM_PLURAL /*0*/:
                appendDigits((long) num, mindigits, maxdigits, sb);
                break;
            case FORM_SINGULAR /*1*/:
                sb.append(Utils.chineseNumber((long) num, ChineseDigits.TRADITIONAL));
                break;
            case FORM_DUAL /*2*/:
                sb.append(Utils.chineseNumber((long) num, ChineseDigits.SIMPLIFIED));
                break;
            case FORM_PAUCAL /*3*/:
                sb.append(Utils.chineseNumber((long) num, ChineseDigits.KOREAN));
                break;
        }
        if (this.dr.requiresDigitSeparator) {
            sb.append(' ');
        }
    }

    public void appendDigits(long num, int mindigits, int maxdigits, StringBuffer sb) {
        char[] buf = new char[maxdigits];
        int ix = maxdigits;
        while (ix > 0 && num > 0) {
            ix--;
            buf[ix] = (char) ((int) (((long) this.dr.zero) + (num % 10)));
            num /= 10;
        }
        int e = maxdigits - mindigits;
        while (ix > e) {
            ix--;
            buf[ix] = this.dr.zero;
        }
        sb.append(buf, ix, maxdigits - ix);
    }

    public void appendSkippedUnit(StringBuffer sb) {
        if (this.dr.skippedUnitMarker != null) {
            sb.append(this.dr.skippedUnitMarker);
        }
    }

    public boolean appendUnitSeparator(TimeUnit unit, boolean longSep, boolean afterFirst, boolean beforeLast, StringBuffer sb) {
        boolean z = false;
        if ((longSep && this.dr.unitSep != null) || this.dr.shortUnitSep != null) {
            if (!longSep || this.dr.unitSep == null) {
                sb.append(this.dr.shortUnitSep);
            } else {
                int ix = (afterFirst ? FORM_DUAL : FORM_PLURAL) + (beforeLast ? FORM_SINGULAR : FORM_PLURAL);
                sb.append(this.dr.unitSep[ix]);
                if (this.dr.unitSepRequiresDP != null) {
                    z = this.dr.unitSepRequiresDP[ix];
                }
                return z;
            }
        }
        return false;
    }

    private int computeForm(TimeUnit unit, int count, int cv, boolean lastOfMultiple) {
        if (trace) {
            System.err.println("pfd.cf unit: " + unit + " count: " + count + " cv: " + cv + " dr.pl: " + this.dr.pl);
            Thread.dumpStack();
        }
        if (this.dr.pl == null) {
            return FORM_PLURAL;
        }
        int v;
        int val = count / Grego.MILLIS_PER_SECOND;
        switch (cv) {
            case FORM_PLURAL /*0*/:
            case FORM_SINGULAR /*1*/:
                break;
            case FORM_DUAL /*2*/:
                switch (this.dr.fractionHandling) {
                    case FORM_PLURAL /*0*/:
                        return FORM_PLURAL;
                    case FORM_SINGULAR /*1*/:
                    case FORM_DUAL /*2*/:
                        v = count / BreakIterator.WORD_IDEO_LIMIT;
                        if (v == FORM_SINGULAR) {
                            if (this.dr.halfNames == null || this.dr.halfNames[unit.ordinal()] == null) {
                                return FORM_SINGULAR_NO_OMIT;
                            }
                            return FORM_HALF_SPELLED;
                        } else if ((v & FORM_SINGULAR) == FORM_SINGULAR) {
                            if (this.dr.pl == (byte) 5 && v > 21) {
                                return FORM_SINGULAR_NO_OMIT;
                            }
                            if (v == FORM_PAUCAL && this.dr.pl == (byte) 1 && this.dr.fractionHandling != (byte) 2) {
                                return FORM_PLURAL;
                            }
                        }
                        break;
                    case FORM_PAUCAL /*3*/:
                        v = count / BreakIterator.WORD_IDEO_LIMIT;
                        if (v == FORM_SINGULAR || v == FORM_PAUCAL) {
                            return FORM_PAUCAL;
                        }
                    default:
                        throw new IllegalStateException();
                }
                break;
            default:
                switch (this.dr.decimalHandling) {
                    case FORM_SINGULAR /*1*/:
                        return FORM_SINGULAR_NO_OMIT;
                    case FORM_DUAL /*2*/:
                        if (count < Grego.MILLIS_PER_SECOND) {
                            return FORM_SINGULAR_NO_OMIT;
                        }
                        break;
                    case FORM_PAUCAL /*3*/:
                        if (this.dr.pl == (byte) 3) {
                            return FORM_PAUCAL;
                        }
                        break;
                }
                return FORM_PLURAL;
        }
        if (trace && count == 0) {
            System.err.println("EZeroHandling = " + this.dr.zeroHandling);
        }
        if (count == 0 && this.dr.zeroHandling == (byte) 1) {
            return FORM_SINGULAR_SPELLED;
        }
        int form = FORM_PLURAL;
        switch (this.dr.pl) {
            case FORM_PLURAL /*0*/:
                break;
            case FORM_SINGULAR /*1*/:
                if (val == FORM_SINGULAR) {
                    form = FORM_SINGULAR_SPELLED;
                    break;
                }
                break;
            case FORM_DUAL /*2*/:
                if (val != FORM_DUAL) {
                    if (val == FORM_SINGULAR) {
                        form = FORM_SINGULAR;
                        break;
                    }
                }
                form = FORM_DUAL;
                break;
                break;
            case FORM_PAUCAL /*3*/:
                v = val;
                v = val % 100;
                if (v > 20) {
                    v %= 10;
                }
                if (v != FORM_SINGULAR) {
                    if (v > FORM_SINGULAR && v < FORM_SINGULAR_NO_OMIT) {
                        form = FORM_PAUCAL;
                        break;
                    }
                }
                form = FORM_SINGULAR;
                break;
            case FORM_SINGULAR_SPELLED /*4*/:
                if (val != FORM_DUAL) {
                    if (val != FORM_SINGULAR) {
                        if (unit == TimeUnit.YEAR && val > 11) {
                            form = FORM_SINGULAR_NO_OMIT;
                            break;
                        }
                    } else if (!lastOfMultiple) {
                        form = FORM_SINGULAR;
                        break;
                    } else {
                        form = FORM_SINGULAR_SPELLED;
                        break;
                    }
                }
                form = FORM_DUAL;
                break;
            case FORM_SINGULAR_NO_OMIT /*5*/:
                if (val != FORM_DUAL) {
                    if (val != FORM_SINGULAR) {
                        if (val > 10) {
                            form = FORM_SINGULAR_NO_OMIT;
                            break;
                        }
                    }
                    form = FORM_SINGULAR;
                    break;
                }
                form = FORM_DUAL;
                break;
                break;
            default:
                System.err.println("dr.pl is " + this.dr.pl);
                throw new IllegalStateException();
        }
        return form;
    }
}
