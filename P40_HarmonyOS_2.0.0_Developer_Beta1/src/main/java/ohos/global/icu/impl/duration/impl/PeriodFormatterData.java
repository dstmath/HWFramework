package ohos.global.icu.impl.duration.impl;

import java.io.PrintStream;
import java.util.Arrays;
import ohos.global.icu.impl.duration.TimeUnit;
import ohos.global.icu.impl.duration.impl.DataRecord;
import ohos.global.icu.impl.duration.impl.Utils;

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

    public PeriodFormatterData(String str, DataRecord dataRecord) {
        this.dr = dataRecord;
        this.localeName = str;
        if (str == null) {
            throw new NullPointerException("localename is null");
        } else if (dataRecord == null) {
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

    public boolean appendPrefix(int i, int i2, StringBuffer stringBuffer) {
        DataRecord.ScopeData scopeData;
        String str;
        if (this.dr.scopeData == null || (scopeData = this.dr.scopeData[(i * 3) + i2]) == null || (str = scopeData.prefix) == null) {
            return false;
        }
        stringBuffer.append(str);
        return scopeData.requiresDigitPrefix;
    }

    public void appendSuffix(int i, int i2, StringBuffer stringBuffer) {
        DataRecord.ScopeData scopeData;
        String str;
        if (this.dr.scopeData != null && (scopeData = this.dr.scopeData[(i * 3) + i2]) != null && (str = scopeData.suffix) != null) {
            if (trace) {
                PrintStream printStream = System.out;
                printStream.println("appendSuffix '" + str + "'");
            }
            stringBuffer.append(str);
        }
    }

    public boolean appendUnit(TimeUnit timeUnit, int i, int i2, int i3, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, StringBuffer stringBuffer) {
        boolean z6;
        boolean z7;
        TimeUnit timeUnit2;
        String str;
        byte b;
        int i4 = i;
        int ordinal = timeUnit.ordinal();
        if (this.dr.requiresSkipMarker == null || !this.dr.requiresSkipMarker[ordinal] || this.dr.skippedUnitMarker == null) {
            z6 = false;
        } else {
            if (!z5 && z4) {
                stringBuffer.append(this.dr.skippedUnitMarker);
            }
            z6 = true;
        }
        if (i3 != 0) {
            boolean z8 = i3 == 1;
            DataRecord dataRecord = this.dr;
            String[] strArr = z8 ? dataRecord.mediumNames : dataRecord.shortNames;
            if (strArr == null || strArr[ordinal] == null) {
                strArr = z8 ? this.dr.shortNames : this.dr.mediumNames;
            }
            if (!(strArr == null || strArr[ordinal] == null)) {
                appendCount(timeUnit, false, false, i, i2, z, strArr[ordinal], z4, stringBuffer);
                return false;
            }
        }
        int i5 = i2;
        if (i5 == 2 && this.dr.halfSupport != null && (b = this.dr.halfSupport[ordinal]) != 0 && (b == 1 || (b == 2 && i4 <= 1000))) {
            i4 = (i4 / 500) * 500;
            i5 = 3;
        }
        if (!z3 || !z4) {
            timeUnit2 = timeUnit;
            z7 = false;
        } else {
            timeUnit2 = timeUnit;
            z7 = true;
        }
        int computeForm = computeForm(timeUnit2, i4, i5, z7);
        if (computeForm == 4) {
            if (this.dr.singularNames == null) {
                str = this.dr.pluralNames[ordinal][1];
                computeForm = 1;
            } else {
                str = this.dr.singularNames[ordinal];
            }
        } else if (computeForm == 5) {
            str = this.dr.pluralNames[ordinal][1];
        } else if (computeForm == 6) {
            str = this.dr.halfNames[ordinal];
        } else {
            try {
                str = this.dr.pluralNames[ordinal][computeForm];
            } catch (NullPointerException e) {
                System.out.println("Null Pointer in PeriodFormatterData[" + this.localeName + "].au px: " + ordinal + " form: " + computeForm + " pn: " + Arrays.toString(this.dr.pluralNames));
                throw e;
            }
        }
        if (str == null) {
            str = this.dr.pluralNames[ordinal][0];
            computeForm = 0;
        }
        int appendCount = appendCount(timeUnit, computeForm == 4 || computeForm == 6 || (this.dr.omitSingularCount && computeForm == 1) || (this.dr.omitDualCount && computeForm == 2), z2, i4, i5, z, str, z4, stringBuffer);
        if (z4 && appendCount >= 0) {
            String str2 = null;
            if (this.dr.rqdSuffixes != null && appendCount < this.dr.rqdSuffixes.length) {
                str2 = this.dr.rqdSuffixes[appendCount];
            }
            if (str2 == null && this.dr.optSuffixes != null && appendCount < this.dr.optSuffixes.length) {
                str2 = this.dr.optSuffixes[appendCount];
            }
            if (str2 != null) {
                stringBuffer.append(str2);
            }
        }
        return z6;
    }

    /* JADX WARNING: Removed duplicated region for block: B:119:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:127:? A[RETURN, SYNTHETIC] */
    public int appendCount(TimeUnit timeUnit, boolean z, boolean z2, int i, int i2, boolean z3, String str, boolean z4, StringBuffer stringBuffer) {
        String str2;
        String str3;
        byte b = 0;
        int i3 = i2;
        if (i3 == 2 && this.dr.halves == null) {
            i3 = 0;
        }
        if (!z && z2 && this.dr.digitPrefix != null) {
            stringBuffer.append(this.dr.digitPrefix);
        }
        int ordinal = timeUnit.ordinal();
        if (i3 != 0) {
            int i4 = 3;
            if (i3 == 1) {
                int i5 = i / 1000;
                if (timeUnit == TimeUnit.MINUTE && !((this.dr.fiveMinutes == null && this.dr.fifteenMinutes == null) || i5 == 0 || i5 % 5 != 0)) {
                    if (this.dr.fifteenMinutes != null && (i5 == 15 || i5 == 45)) {
                        if (i5 == 15) {
                            i4 = 1;
                        }
                        if (!z) {
                            appendInteger(i4, 1, 10, stringBuffer);
                        }
                        str2 = this.dr.fifteenMinutes;
                        ordinal = 8;
                        stringBuffer.append(this.dr.countSep);
                        stringBuffer.append(str3);
                        stringBuffer.append(str2);
                        if (z4) {
                        }
                    } else if (this.dr.fiveMinutes != null) {
                        int i6 = i5 / 5;
                        if (!z) {
                            appendInteger(i6, 1, 10, stringBuffer);
                        }
                        str2 = this.dr.fiveMinutes;
                        ordinal = 9;
                        if (!z && z3) {
                            stringBuffer.append(this.dr.countSep);
                        }
                        if (!z && this.dr.measures != null && ordinal < this.dr.measures.length && (str3 = this.dr.measures[ordinal]) != null) {
                            stringBuffer.append(str3);
                        }
                        stringBuffer.append(str2);
                        if (z4) {
                            return ordinal;
                        }
                        return -1;
                    }
                }
                if (!z) {
                    appendInteger(i5, 1, 10, stringBuffer);
                }
            } else if (i3 != 2) {
                if (i3 == 4) {
                    i4 = 2;
                } else if (i3 != 5) {
                    i4 = 1;
                }
                if (!z) {
                    appendCountValue(i, 1, i4, stringBuffer);
                }
            } else {
                int i7 = i / 500;
                if (i7 != 1 && !z) {
                    appendCountValue(i, 1, 0, stringBuffer);
                }
                if ((i7 & 1) == 1) {
                    if (i7 != 1 || this.dr.halfNames == null || this.dr.halfNames[ordinal] == null) {
                        int i8 = i7 == 1 ? 0 : 1;
                        if (this.dr.genders != null && this.dr.halves.length > 2 && this.dr.genders[ordinal] == 1) {
                            i8 += 2;
                        }
                        if (this.dr.halfPlacements != null) {
                            b = this.dr.halfPlacements[i8 & 1];
                        }
                        String str4 = this.dr.halves[i8];
                        String str5 = this.dr.measures == null ? null : this.dr.measures[ordinal];
                        if (b == 0) {
                            stringBuffer.append(str4);
                        } else if (b != 1) {
                            if (b == 2) {
                                if (str5 != null) {
                                    stringBuffer.append(str5);
                                }
                                if (z3 && !z) {
                                    stringBuffer.append(this.dr.countSep);
                                }
                                stringBuffer.append(str);
                                stringBuffer.append(str4);
                                if (z4) {
                                    return ordinal;
                                }
                                return -1;
                            }
                        } else if (str5 != null) {
                            stringBuffer.append(str5);
                            stringBuffer.append(str4);
                            if (z3 && !z) {
                                stringBuffer.append(this.dr.countSep);
                            }
                            stringBuffer.append(str);
                            return -1;
                        } else {
                            stringBuffer.append(str);
                            stringBuffer.append(str4);
                            if (z4) {
                                return ordinal;
                            }
                            return -1;
                        }
                    } else {
                        stringBuffer.append(str);
                        if (z4) {
                            return ordinal;
                        }
                        return -1;
                    }
                }
            }
        } else if (!z) {
            appendInteger(i / 1000, 1, 10, stringBuffer);
        }
        str2 = str;
        stringBuffer.append(this.dr.countSep);
        stringBuffer.append(str3);
        stringBuffer.append(str2);
        if (z4) {
        }
    }

    public void appendCountValue(int i, int i2, int i3, StringBuffer stringBuffer) {
        int i4 = i / 1000;
        if (i3 == 0) {
            appendInteger(i4, i2, 10, stringBuffer);
            return;
        }
        if (this.dr.requiresDigitSeparator && stringBuffer.length() > 0) {
            stringBuffer.append(' ');
        }
        appendDigits((long) i4, i2, 10, stringBuffer);
        int i5 = i % 1000;
        if (i3 == 1) {
            i5 /= 100;
        } else if (i3 == 2) {
            i5 /= 10;
        }
        stringBuffer.append(this.dr.decimalSep);
        appendDigits((long) i5, i3, i3, stringBuffer);
        if (this.dr.requiresDigitSeparator) {
            stringBuffer.append(' ');
        }
    }

    public void appendInteger(int i, int i2, int i3, StringBuffer stringBuffer) {
        String str;
        if (this.dr.numberNames == null || i >= this.dr.numberNames.length || (str = this.dr.numberNames[i]) == null) {
            if (this.dr.requiresDigitSeparator && stringBuffer.length() > 0) {
                stringBuffer.append(' ');
            }
            byte b = this.dr.numberSystem;
            if (b == 0) {
                appendDigits((long) i, i2, i3, stringBuffer);
            } else if (b == 1) {
                stringBuffer.append(Utils.chineseNumber((long) i, Utils.ChineseDigits.TRADITIONAL));
            } else if (b == 2) {
                stringBuffer.append(Utils.chineseNumber((long) i, Utils.ChineseDigits.SIMPLIFIED));
            } else if (b == 3) {
                stringBuffer.append(Utils.chineseNumber((long) i, Utils.ChineseDigits.KOREAN));
            }
            if (this.dr.requiresDigitSeparator) {
                stringBuffer.append(' ');
                return;
            }
            return;
        }
        stringBuffer.append(str);
    }

    public void appendDigits(long j, int i, int i2, StringBuffer stringBuffer) {
        char[] cArr = new char[i2];
        long j2 = j;
        int i3 = i2;
        while (i3 > 0 && j2 > 0) {
            i3--;
            cArr[i3] = (char) ((int) (((long) this.dr.zero) + (j2 % 10)));
            j2 /= 10;
        }
        int i4 = i2 - i;
        while (i3 > i4) {
            i3--;
            cArr[i3] = this.dr.zero;
        }
        stringBuffer.append(cArr, i3, i2 - i3);
    }

    public void appendSkippedUnit(StringBuffer stringBuffer) {
        if (this.dr.skippedUnitMarker != null) {
            stringBuffer.append(this.dr.skippedUnitMarker);
        }
    }

    public boolean appendUnitSeparator(TimeUnit timeUnit, boolean z, boolean z2, boolean z3, StringBuffer stringBuffer) {
        if ((z && this.dr.unitSep != null) || this.dr.shortUnitSep != null) {
            if (!z || this.dr.unitSep == null) {
                stringBuffer.append(this.dr.shortUnitSep);
            } else {
                int i = (z2 ? 2 : 0) + (z3 ? 1 : 0);
                stringBuffer.append(this.dr.unitSep[i]);
                if (this.dr.unitSepRequiresDP == null || !this.dr.unitSepRequiresDP[i]) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00b5, code lost:
        if (r8.dr.fractionHandling != 2) goto L_0x00b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x00fb, code lost:
        if (r0 > 10) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0124, code lost:
        if (r12 != false) goto L_0x014b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x012d, code lost:
        if (r0 > 11) goto L_0x00fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0145, code lost:
        if (r0 == 1) goto L_0x0147;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0149, code lost:
        if (r0 == 1) goto L_0x014b;
     */
    private int computeForm(TimeUnit timeUnit, int i, int i2, boolean z) {
        if (trace) {
            System.err.println("pfd.cf unit: " + timeUnit + " count: " + i + " cv: " + i2 + " dr.pl: " + ((int) this.dr.pl));
            Thread.dumpStack();
        }
        int i3 = 0;
        if (this.dr.pl == 0) {
            return 0;
        }
        int i4 = i / 1000;
        if (!(i2 == 0 || i2 == 1)) {
            if (i2 != 2) {
                byte b = this.dr.decimalHandling;
                if (b != 0) {
                    if (b != 1) {
                        if (b != 2) {
                            if (b == 3 && this.dr.pl == 3) {
                                return 3;
                            }
                        } else if (i < 1000) {
                        }
                    }
                    return 5;
                }
                return 0;
            }
            byte b2 = this.dr.fractionHandling;
            if (b2 != 0) {
                if (b2 == 1 || b2 == 2) {
                    int i5 = i / 500;
                    if (i5 == 1) {
                        if (this.dr.halfNames == null || this.dr.halfNames[timeUnit.ordinal()] == null) {
                            return 5;
                        }
                        return 6;
                    } else if ((i5 & 1) == 1) {
                        if (this.dr.pl == 5 && i5 > 21) {
                            return 5;
                        }
                        if (i5 == 3) {
                            if (this.dr.pl == 1) {
                            }
                        }
                    }
                } else if (b2 == 3) {
                    int i6 = i / 500;
                    if (i6 == 1 || i6 == 3) {
                        return 3;
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
            return 0;
        }
        if (trace && i == 0) {
            System.err.println("EZeroHandling = " + ((int) this.dr.zeroHandling));
        }
        if (i == 0 && this.dr.zeroHandling == 1) {
            return 4;
        }
        byte b3 = this.dr.pl;
        if (b3 != 0) {
            if (b3 != 1) {
                if (b3 != 2) {
                    if (b3 != 3) {
                        if (b3 != 4) {
                            if (b3 != 5) {
                                System.err.println("dr.pl is " + ((int) this.dr.pl));
                                throw new IllegalStateException();
                            } else if (i4 == 2) {
                                return 2;
                            } else {
                                if (i4 != 1) {
                                }
                            }
                        } else if (i4 == 2) {
                            return 2;
                        } else {
                            if (i4 != 1) {
                                if (timeUnit == TimeUnit.YEAR) {
                                }
                            }
                        }
                        return 5;
                    }
                    int i7 = i4 % 100;
                    if (i7 > 20) {
                        i7 %= 10;
                    }
                    if (i7 == 1) {
                        i3 = 1;
                    } else if (i7 > 1 && i7 < 5) {
                        i3 = 3;
                    }
                } else if (i4 == 2) {
                    return 2;
                }
                return 1;
            }
            return 4;
        }
        return i3;
    }
}
