package android.icu.impl.duration.impl;

import android.icu.impl.duration.TimeUnit;
import android.icu.impl.duration.impl.DataRecord;
import android.icu.impl.duration.impl.Utils;
import android.icu.text.BreakIterator;
import java.io.PrintStream;
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

    public PeriodFormatterData(String localeName2, DataRecord dr2) {
        this.dr = dr2;
        this.localeName = localeName2;
        if (localeName2 == null) {
            throw new NullPointerException("localename is null");
        } else if (dr2 == null) {
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
            DataRecord.ScopeData sd = this.dr.scopeData[(tl * 3) + td];
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
            DataRecord.ScopeData sd = this.dr.scopeData[(tl * 3) + td];
            if (sd != null) {
                String suffix = sd.suffix;
                if (suffix != null) {
                    if (trace) {
                        PrintStream printStream = System.out;
                        printStream.println("appendSuffix '" + suffix + "'");
                    }
                    sb.append(suffix);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x008d, code lost:
        if (r0 > 1000) goto L_0x0097;
     */
    public boolean appendUnit(TimeUnit unit, int count, int cv, int uv, boolean useCountSep, boolean useDigitPrefix, boolean multiple, boolean last, boolean wasSkipped, StringBuffer sb) {
        String name;
        int i = count;
        int i2 = uv;
        StringBuffer stringBuffer = sb;
        int px = unit.ordinal();
        boolean willRequireSkipMarker = false;
        if (!(this.dr.requiresSkipMarker == null || !this.dr.requiresSkipMarker[px] || this.dr.skippedUnitMarker == null)) {
            if (!wasSkipped && last) {
                stringBuffer.append(this.dr.skippedUnitMarker);
            }
            willRequireSkipMarker = true;
        }
        boolean willRequireSkipMarker2 = willRequireSkipMarker;
        if (i2 != 0) {
            boolean useMedium = i2 == 1;
            String[] names = useMedium ? this.dr.mediumNames : this.dr.shortNames;
            if (names == null || names[px] == null) {
                names = useMedium ? this.dr.shortNames : this.dr.mediumNames;
            }
            String[] names2 = names;
            if (!(names2 == null || names2[px] == null)) {
                appendCount(unit, false, false, i, cv, useCountSep, names2[px], last, stringBuffer);
                return false;
            }
        }
        int i3 = cv;
        if (i3 == 2 && this.dr.halfSupport != null) {
            switch (this.dr.halfSupport[px]) {
                case 2:
                    break;
                case 1:
                    i = (i / BreakIterator.WORD_IDEO_LIMIT) * BreakIterator.WORD_IDEO_LIMIT;
                    i3 = 3;
                    break;
            }
        }
        int count2 = i;
        int cv2 = i3;
        TimeUnit timeUnit = unit;
        int form = computeForm(timeUnit, count2, cv2, multiple && last);
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
                int i4 = cv2;
                int i5 = count2;
                PrintStream printStream = System.out;
                printStream.println("Null Pointer in PeriodFormatterData[" + this.localeName + "].au px: " + px + " form: " + form + " pn: " + Arrays.toString(this.dr.pluralNames));
                throw e;
            }
        }
        if (name == null) {
            form = 0;
            name = this.dr.pluralNames[px][0];
        }
        int form2 = form;
        int i6 = form2;
        int i7 = cv2;
        int i8 = count2;
        int suffixIndex = appendCount(timeUnit, form2 == 4 || form2 == 6 || (this.dr.omitSingularCount && form2 == 1) || (this.dr.omitDualCount && form2 == 2), useDigitPrefix, count2, cv2, useCountSep, name, last, stringBuffer);
        if (last && suffixIndex >= 0) {
            String suffix = null;
            if (this.dr.rqdSuffixes != null && suffixIndex < this.dr.rqdSuffixes.length) {
                suffix = this.dr.rqdSuffixes[suffixIndex];
            }
            if (suffix == null && this.dr.optSuffixes != null && suffixIndex < this.dr.optSuffixes.length) {
                suffix = this.dr.optSuffixes[suffixIndex];
            }
            if (suffix != null) {
                stringBuffer.append(suffix);
            }
        }
        return willRequireSkipMarker2;
    }

    public int appendCount(TimeUnit unit, boolean omitCount, boolean useDigitPrefix, int count, int cv, boolean useSep, String name, boolean last, StringBuffer sb) {
        int i = count;
        String name2 = name;
        StringBuffer stringBuffer = sb;
        int cv2 = cv;
        if (cv2 == 2 && this.dr.halves == null) {
            cv2 = 0;
        }
        if (!omitCount && useDigitPrefix && this.dr.digitPrefix != null) {
            stringBuffer.append(this.dr.digitPrefix);
        }
        int index = unit.ordinal();
        switch (cv2) {
            case 0:
                TimeUnit timeUnit = unit;
                if (!omitCount) {
                    appendInteger(i / 1000, 1, 10, stringBuffer);
                    break;
                }
                break;
            case 1:
                int val = i / 1000;
                if (unit == TimeUnit.MINUTE && !((this.dr.fiveMinutes == null && this.dr.fifteenMinutes == null) || val == 0 || val % 5 != 0)) {
                    if (this.dr.fifteenMinutes == null || (val != 15 && val != 45)) {
                        if (this.dr.fiveMinutes != null) {
                            int val2 = val / 5;
                            if (!omitCount) {
                                appendInteger(val2, 1, 10, stringBuffer);
                            }
                            name2 = this.dr.fiveMinutes;
                            index = 9;
                            break;
                        }
                    } else {
                        int val3 = val == 15 ? 1 : 3;
                        if (!omitCount) {
                            appendInteger(val3, 1, 10, stringBuffer);
                        }
                        name2 = this.dr.fifteenMinutes;
                        index = 8;
                        break;
                    }
                }
                if (!omitCount) {
                    appendInteger(val, 1, 10, stringBuffer);
                    break;
                }
                break;
            case 2:
                int v = i / BreakIterator.WORD_IDEO_LIMIT;
                if (v != 1 && !omitCount) {
                    appendCountValue(i, 1, 0, stringBuffer);
                }
                if ((v & 1) == 1) {
                    if (v != 1 || this.dr.halfNames == null || this.dr.halfNames[index] == null) {
                        int solox = v == 1 ? 0 : 1;
                        if (this.dr.genders != null && this.dr.halves.length > 2 && this.dr.genders[index] == 1) {
                            solox += 2;
                        }
                        int hp = this.dr.halfPlacements == null ? 0 : this.dr.halfPlacements[solox & 1];
                        String half = this.dr.halves[solox];
                        String measure = this.dr.measures == null ? null : this.dr.measures[index];
                        switch (hp) {
                            case 0:
                                stringBuffer.append(half);
                                break;
                            case 1:
                                if (measure != null) {
                                    stringBuffer.append(measure);
                                    stringBuffer.append(half);
                                    if (useSep && !omitCount) {
                                        stringBuffer.append(this.dr.countSep);
                                    }
                                    stringBuffer.append(name2);
                                    return -1;
                                }
                                int i2 = -1;
                                stringBuffer.append(name2);
                                stringBuffer.append(half);
                                if (last) {
                                    i2 = index;
                                }
                                return i2;
                            case 2:
                                if (measure != null) {
                                    stringBuffer.append(measure);
                                }
                                if (useSep && !omitCount) {
                                    stringBuffer.append(this.dr.countSep);
                                }
                                stringBuffer.append(name2);
                                stringBuffer.append(half);
                                return last ? index : -1;
                        }
                    } else {
                        stringBuffer.append(name2);
                        return last ? index : -1;
                    }
                }
                TimeUnit timeUnit2 = unit;
                break;
            default:
                TimeUnit timeUnit3 = unit;
                int decimals = 1;
                switch (cv2) {
                    case 4:
                        decimals = 2;
                        break;
                    case 5:
                        decimals = 3;
                        break;
                }
                if (!omitCount) {
                    appendCountValue(i, 1, decimals, stringBuffer);
                    break;
                }
                break;
        }
        int index2 = index;
        if (!omitCount && useSep) {
            stringBuffer.append(this.dr.countSep);
        }
        if (!omitCount && this.dr.measures != null && index2 < this.dr.measures.length) {
            String measure2 = this.dr.measures[index2];
            if (measure2 != null) {
                stringBuffer.append(measure2);
            }
        }
        stringBuffer.append(name2);
        return last ? index2 : -1;
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
            case 0:
                appendDigits((long) num, mindigits, maxdigits, sb);
                break;
            case 1:
                sb.append(Utils.chineseNumber((long) num, Utils.ChineseDigits.TRADITIONAL));
                break;
            case 2:
                sb.append(Utils.chineseNumber((long) num, Utils.ChineseDigits.SIMPLIFIED));
                break;
            case 3:
                sb.append(Utils.chineseNumber((long) num, Utils.ChineseDigits.KOREAN));
                break;
        }
        if (this.dr.requiresDigitSeparator) {
            sb.append(' ');
        }
    }

    public void appendDigits(long num, int mindigits, int maxdigits, StringBuffer sb) {
        char[] buf = new char[maxdigits];
        long num2 = num;
        int ix = maxdigits;
        while (ix > 0 && num2 > 0) {
            ix--;
            buf[ix] = (char) ((int) (((long) this.dr.zero) + (num2 % 10)));
            num2 /= 10;
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
                int ix = (afterFirst ? 2 : 0) + (beforeLast);
                sb.append(this.dr.unitSep[ix]);
                if (this.dr.unitSepRequiresDP != null && this.dr.unitSepRequiresDP[ix]) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    private int computeForm(TimeUnit unit, int count, int cv, boolean lastOfMultiple) {
        if (trace) {
            PrintStream printStream = System.err;
            printStream.println("pfd.cf unit: " + unit + " count: " + count + " cv: " + cv + " dr.pl: " + this.dr.pl);
            Thread.dumpStack();
        }
        if (this.dr.pl == 0) {
            return 0;
        }
        int val = count / 1000;
        switch (cv) {
            case 0:
            case 1:
                break;
            case 2:
                switch (this.dr.fractionHandling) {
                    case 0:
                        return 0;
                    case 1:
                    case 2:
                        int v = count / BreakIterator.WORD_IDEO_LIMIT;
                        if (v == 1) {
                            if (this.dr.halfNames == null || this.dr.halfNames[unit.ordinal()] == null) {
                                return 5;
                            }
                            return 6;
                        } else if ((v & 1) == 1) {
                            if (this.dr.pl == 5 && v > 21) {
                                return 5;
                            }
                            if (v == 3 && this.dr.pl == 1 && this.dr.fractionHandling != 2) {
                                return 0;
                            }
                        }
                        break;
                    case 3:
                        int v2 = count / BreakIterator.WORD_IDEO_LIMIT;
                        if (v2 == 1 || v2 == 3) {
                            return 3;
                        }
                    default:
                        throw new IllegalStateException();
                }
                break;
            default:
                switch (this.dr.decimalHandling) {
                    case 1:
                        return 5;
                    case 2:
                        if (count < 1000) {
                            return 5;
                        }
                        break;
                    case 3:
                        if (this.dr.pl == 3) {
                            return 3;
                        }
                        break;
                }
                return 0;
        }
        if (trace && count == 0) {
            PrintStream printStream2 = System.err;
            printStream2.println("EZeroHandling = " + this.dr.zeroHandling);
        }
        if (count == 0 && this.dr.zeroHandling == 1) {
            return 4;
        }
        int form = 0;
        switch (this.dr.pl) {
            case 0:
                break;
            case 1:
                if (val == 1) {
                    form = 4;
                    break;
                }
                break;
            case 2:
                if (val != 2) {
                    if (val == 1) {
                        form = 1;
                        break;
                    }
                } else {
                    form = 2;
                    break;
                }
                break;
            case 3:
                int v3 = val % 100;
                if (v3 > 20) {
                    v3 %= 10;
                }
                if (v3 != 1) {
                    if (v3 > 1 && v3 < 5) {
                        form = 3;
                        break;
                    }
                } else {
                    form = 1;
                    break;
                }
            case 4:
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
                } else {
                    form = 2;
                    break;
                }
            case 5:
                if (val != 2) {
                    if (val != 1) {
                        if (val > 10) {
                            form = 5;
                            break;
                        }
                    } else {
                        form = 1;
                        break;
                    }
                } else {
                    form = 2;
                    break;
                }
                break;
            default:
                PrintStream printStream3 = System.err;
                printStream3.println("dr.pl is " + this.dr.pl);
                throw new IllegalStateException();
        }
        return form;
    }
}
