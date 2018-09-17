package android.icu.impl.duration.impl;

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
    public static boolean trace = false;
    final DataRecord dr;
    String localeName;

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
            ScopeData sd = this.dr.scopeData[(tl * 3) + td];
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
            ScopeData sd = this.dr.scopeData[(tl * 3) + td];
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

    /* JADX WARNING: Missing block: B:68:0x0139, code:
            if (r24 <= 1000) goto L_0x013b;
     */
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
            boolean useMedium = uv == 1;
            String[] names = useMedium ? this.dr.mediumNames : this.dr.shortNames;
            if (names == null || names[px] == null) {
                names = useMedium ? this.dr.shortNames : this.dr.mediumNames;
            }
            if (!(names == null || names[px] == null)) {
                appendCount(unit, false, false, count, cv, useCountSep, names[px], last, sb);
                return false;
            }
        }
        if (cv == 2 && this.dr.halfSupport != null) {
            switch (this.dr.halfSupport[px]) {
                case (byte) 2:
                    break;
                case (byte) 1:
                    count = (count / BreakIterator.WORD_IDEO_LIMIT) * BreakIterator.WORD_IDEO_LIMIT;
                    cv = 3;
                    break;
            }
        }
        int form = computeForm(unit, count, cv, multiple ? last : false);
        if (form == 4) {
            if (this.dr.singularNames == null) {
                form = 1;
                name = this.dr.pluralNames[px][1];
            } else {
                name = this.dr.singularNames[px];
            }
        } else if (form == 5) {
            name = this.dr.pluralNames[px][1];
        } else if (form == 6) {
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
            form = 0;
            name = this.dr.pluralNames[px][0];
        }
        boolean omitCount = (form == 4 || form == 6 || (this.dr.omitSingularCount && form == 1)) ? true : this.dr.omitDualCount && form == 2;
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
        if (cv == 2 && this.dr.halves == null) {
            cv = 0;
        }
        if (!(omitCount || !useDigitPrefix || this.dr.digitPrefix == null)) {
            sb.append(this.dr.digitPrefix);
        }
        int index = unit.ordinal();
        switch (cv) {
            case 0:
                if (!omitCount) {
                    appendInteger(count / 1000, 1, 10, sb);
                    break;
                }
                break;
            case 1:
                int val = count / 1000;
                if (unit == TimeUnit.MINUTE && !((this.dr.fiveMinutes == null && this.dr.fifteenMinutes == null) || val == 0 || val % 5 != 0)) {
                    if (this.dr.fifteenMinutes == null || (val != 15 && val != 45)) {
                        if (this.dr.fiveMinutes != null) {
                            val /= 5;
                            if (!omitCount) {
                                appendInteger(val, 1, 10, sb);
                            }
                            name = this.dr.fiveMinutes;
                            index = 9;
                            break;
                        }
                    }
                    val = val == 15 ? 1 : 3;
                    if (!omitCount) {
                        appendInteger(val, 1, 10, sb);
                    }
                    name = this.dr.fifteenMinutes;
                    index = 8;
                    break;
                }
                if (!omitCount) {
                    appendInteger(val, 1, 10, sb);
                    break;
                }
                break;
            case 2:
                int v = count / BreakIterator.WORD_IDEO_LIMIT;
                if (!(v == 1 || omitCount)) {
                    appendCountValue(count, 1, 0, sb);
                }
                if ((v & 1) == 1) {
                    if (v != 1 || this.dr.halfNames == null || this.dr.halfNames[index] == null) {
                        int hp;
                        int solox = v == 1 ? 0 : 1;
                        if (this.dr.genders != null && this.dr.halves.length > 2 && this.dr.genders[index] == (byte) 1) {
                            solox += 2;
                        }
                        if (this.dr.halfPlacements == null) {
                            hp = 0;
                        } else {
                            hp = this.dr.halfPlacements[solox & 1];
                        }
                        String half = this.dr.halves[solox];
                        str = this.dr.measures == null ? null : this.dr.measures[index];
                        switch (hp) {
                            case 0:
                                sb.append(half);
                                break;
                            case 1:
                                if (str != null) {
                                    sb.append(str);
                                    sb.append(half);
                                    if (useSep && (omitCount ^ 1) != 0) {
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
                            case 2:
                                if (str != null) {
                                    sb.append(str);
                                }
                                if (useSep && (omitCount ^ 1) != 0) {
                                    sb.append(this.dr.countSep);
                                }
                                sb.append(name);
                                sb.append(half);
                                if (!last) {
                                    index = -1;
                                }
                                return index;
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
                int decimals = 1;
                switch (cv) {
                    case 4:
                        decimals = 2;
                        break;
                    case 5:
                        decimals = 3;
                        break;
                }
                if (!omitCount) {
                    appendCountValue(count, 1, decimals, sb);
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
        int ival = count / 1000;
        if (decimalDigits == 0) {
            appendInteger(ival, integralDigits, 10, sb);
            return;
        }
        if (this.dr.requiresDigitSeparator && sb.length() > 0) {
            sb.append(' ');
        }
        appendDigits((long) ival, integralDigits, 10, sb);
        int dval = count % 1000;
        if (decimalDigits == 1) {
            dval /= 100;
        } else if (decimalDigits == 2) {
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
            case (byte) 0:
                appendDigits((long) num, mindigits, maxdigits, sb);
                break;
            case (byte) 1:
                sb.append(Utils.chineseNumber((long) num, ChineseDigits.TRADITIONAL));
                break;
            case (byte) 2:
                sb.append(Utils.chineseNumber((long) num, ChineseDigits.SIMPLIFIED));
                break;
            case (byte) 3:
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
                int ix = (afterFirst ? 2 : 0) + (beforeLast ? 1 : 0);
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
        if (this.dr.pl == (byte) 0) {
            return 0;
        }
        int v;
        int val = count / 1000;
        switch (cv) {
            case 0:
            case 1:
                break;
            case 2:
                switch (this.dr.fractionHandling) {
                    case (byte) 0:
                        return 0;
                    case (byte) 1:
                    case (byte) 2:
                        v = count / BreakIterator.WORD_IDEO_LIMIT;
                        if (v == 1) {
                            if (this.dr.halfNames == null || this.dr.halfNames[unit.ordinal()] == null) {
                                return 5;
                            }
                            return 6;
                        } else if ((v & 1) == 1) {
                            if (this.dr.pl == (byte) 5 && v > 21) {
                                return 5;
                            }
                            if (v == 3 && this.dr.pl == (byte) 1 && this.dr.fractionHandling != (byte) 2) {
                                return 0;
                            }
                        }
                        break;
                    case (byte) 3:
                        v = count / BreakIterator.WORD_IDEO_LIMIT;
                        if (v == 1 || v == 3) {
                            return 3;
                        }
                    default:
                        throw new IllegalStateException();
                }
                break;
            default:
                switch (this.dr.decimalHandling) {
                    case (byte) 1:
                        return 5;
                    case (byte) 2:
                        if (count < 1000) {
                            return 5;
                        }
                        break;
                    case (byte) 3:
                        if (this.dr.pl == (byte) 3) {
                            return 3;
                        }
                        break;
                }
                return 0;
        }
        if (trace && count == 0) {
            System.err.println("EZeroHandling = " + this.dr.zeroHandling);
        }
        if (count == 0 && this.dr.zeroHandling == (byte) 1) {
            return 4;
        }
        int form = 0;
        switch (this.dr.pl) {
            case (byte) 0:
                break;
            case (byte) 1:
                if (val == 1) {
                    form = 4;
                    break;
                }
                break;
            case (byte) 2:
                if (val != 2) {
                    if (val == 1) {
                        form = 1;
                        break;
                    }
                }
                form = 2;
                break;
                break;
            case (byte) 3:
                v = val;
                v = val % 100;
                if (v > 20) {
                    v %= 10;
                }
                if (v != 1) {
                    if (v > 1 && v < 5) {
                        form = 3;
                        break;
                    }
                }
                form = 1;
                break;
            case (byte) 4:
                if (val != 2) {
                    if (val != 1) {
                        if (unit == TimeUnit.YEAR && val > 11) {
                            form = 5;
                            break;
                        }
                    } else if (!lastOfMultiple) {
                        form = 1;
                        break;
                    } else {
                        form = 4;
                        break;
                    }
                }
                form = 2;
                break;
            case (byte) 5:
                if (val != 2) {
                    if (val != 1) {
                        if (val > 10) {
                            form = 5;
                            break;
                        }
                    }
                    form = 1;
                    break;
                }
                form = 2;
                break;
                break;
            default:
                System.err.println("dr.pl is " + this.dr.pl);
                throw new IllegalStateException();
        }
        return form;
    }
}
