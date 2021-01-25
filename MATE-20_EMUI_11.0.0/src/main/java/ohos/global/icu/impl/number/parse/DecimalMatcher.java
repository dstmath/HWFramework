package ohos.global.icu.impl.number.parse;

import ohos.global.icu.impl.StaticUnicodeSets;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.impl.number.DecimalQuantity_DualStorageBCD;
import ohos.global.icu.impl.number.Grouper;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.text.UnicodeSet;

public class DecimalMatcher implements NumberParseMatcher {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private final String decimalSeparator;
    private final UnicodeSet decimalUniSet;
    private final String[] digitStrings;
    private final int grouping1;
    private final int grouping2;
    private final boolean groupingDisabled;
    private final String groupingSeparator;
    private final UnicodeSet groupingUniSet;
    private final boolean integerOnly;
    private final UnicodeSet leadSet;
    private final boolean requireGroupingMatch;
    private final UnicodeSet separatorSet;

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public void postProcess(ParsedNumber parsedNumber) {
    }

    public String toString() {
        return "<DecimalMatcher>";
    }

    public static DecimalMatcher getInstance(DecimalFormatSymbols decimalFormatSymbols, Grouper grouper, int i) {
        return new DecimalMatcher(decimalFormatSymbols, grouper, i);
    }

    private DecimalMatcher(DecimalFormatSymbols decimalFormatSymbols, Grouper grouper, int i) {
        StaticUnicodeSets.Key key;
        if ((i & 2) != 0) {
            this.groupingSeparator = decimalFormatSymbols.getMonetaryGroupingSeparatorString();
            this.decimalSeparator = decimalFormatSymbols.getMonetaryDecimalSeparatorString();
        } else {
            this.groupingSeparator = decimalFormatSymbols.getGroupingSeparatorString();
            this.decimalSeparator = decimalFormatSymbols.getDecimalSeparatorString();
        }
        boolean z = true;
        boolean z2 = (i & 4) != 0;
        StaticUnicodeSets.Key key2 = z2 ? StaticUnicodeSets.Key.STRICT_ALL_SEPARATORS : StaticUnicodeSets.Key.ALL_SEPARATORS;
        this.groupingUniSet = StaticUnicodeSets.get(key2);
        StaticUnicodeSets.Key chooseFrom = StaticUnicodeSets.chooseFrom(this.decimalSeparator, z2 ? StaticUnicodeSets.Key.STRICT_COMMA : StaticUnicodeSets.Key.COMMA, z2 ? StaticUnicodeSets.Key.STRICT_PERIOD : StaticUnicodeSets.Key.PERIOD);
        if (chooseFrom != null) {
            this.decimalUniSet = StaticUnicodeSets.get(chooseFrom);
        } else if (!this.decimalSeparator.isEmpty()) {
            this.decimalUniSet = new UnicodeSet().add(this.decimalSeparator.codePointAt(0)).freeze();
        } else {
            this.decimalUniSet = UnicodeSet.EMPTY;
        }
        if (key2 == null || chooseFrom == null) {
            this.separatorSet = new UnicodeSet().addAll(this.groupingUniSet).addAll(this.decimalUniSet).freeze();
            this.leadSet = null;
        } else {
            this.separatorSet = this.groupingUniSet;
            if (z2) {
                key = StaticUnicodeSets.Key.DIGITS_OR_ALL_SEPARATORS;
            } else {
                key = StaticUnicodeSets.Key.DIGITS_OR_STRICT_ALL_SEPARATORS;
            }
            this.leadSet = StaticUnicodeSets.get(key);
        }
        int codePointZero = decimalFormatSymbols.getCodePointZero();
        if (codePointZero == -1 || !UCharacter.isDigit(codePointZero) || UCharacter.digit(codePointZero) != 0) {
            this.digitStrings = decimalFormatSymbols.getDigitStringsLocal();
        } else {
            this.digitStrings = null;
        }
        this.requireGroupingMatch = (i & 8) != 0;
        this.groupingDisabled = (i & 32) != 0;
        this.integerOnly = (i & 16) == 0 ? false : z;
        this.grouping1 = grouper.getPrimary();
        this.grouping2 = grouper.getSecondary();
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean match(StringSegment stringSegment, ParsedNumber parsedNumber) {
        return match(stringSegment, parsedNumber, 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r20v0, resolved type: ohos.global.icu.impl.number.parse.DecimalMatcher */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r3v9 */
    /* JADX WARN: Type inference failed for: r3v10 */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x01a5, code lost:
        if (r3 == false) goto L_0x01aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x01a7, code lost:
        if (r8 != 0) goto L_0x01aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x01ac, code lost:
        if (r20.requireGroupingMatch == false) goto L_0x01b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x01ae, code lost:
        r5 = r17;
        r2 = 2;
        r9 = null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x0168 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x0247  */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x01a5 A[ADDED_TO_REGION, EDGE_INSN: B:186:0x01a5->B:119:0x01a5 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00ee A[ADDED_TO_REGION] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public boolean match(StringSegment stringSegment, ParsedNumber parsedNumber, int i) {
        ?? r3;
        int i2;
        ParsedNumber parsedNumber2;
        boolean z;
        Object[] objArr;
        int i3;
        byte b;
        byte b2;
        byte b3;
        boolean z2;
        boolean z3;
        boolean z4;
        boolean validateGroup;
        if (parsedNumber.seenNumber() && i == 0) {
            return false;
        }
        int offset = stringSegment.getOffset();
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        boolean z5 = false;
        DecimalQuantity_DualStorageBCD decimalQuantity_DualStorageBCD = null;
        String str = null;
        String str2 = null;
        int i8 = -1;
        int i9 = -1;
        int i10 = -1;
        while (true) {
            if (stringSegment.length() <= 0) {
                break;
            }
            int codePoint = stringSegment.getCodePoint();
            if (UCharacter.isDigit(codePoint)) {
                stringSegment.adjustOffset(Character.charCount(codePoint));
                b = (byte) UCharacter.digit(codePoint);
                b2 = -1;
            } else {
                b2 = -1;
                b = -1;
            }
            if (b == b2 && this.digitStrings != null) {
                int i11 = 0;
                z5 = false;
                while (true) {
                    String[] strArr = this.digitStrings;
                    if (i11 >= strArr.length) {
                        b3 = b;
                        break;
                    }
                    String str3 = strArr[i11];
                    if (!str3.isEmpty()) {
                        int commonPrefixLength = stringSegment.getCommonPrefixLength(str3);
                        if (commonPrefixLength == str3.length()) {
                            stringSegment.adjustOffset(commonPrefixLength);
                            b3 = (byte) i11;
                            break;
                        }
                        z5 = z5 || commonPrefixLength == stringSegment.length();
                    }
                    i11++;
                }
            } else {
                b3 = b;
                z5 = false;
            }
            if (b3 >= 0) {
                if (decimalQuantity_DualStorageBCD == null) {
                    decimalQuantity_DualStorageBCD = new DecimalQuantity_DualStorageBCD();
                }
                decimalQuantity_DualStorageBCD.appendDigit(b3, 0, true);
                i5++;
                if (str != null) {
                    i6++;
                }
            } else {
                if (str == null && !this.decimalSeparator.isEmpty()) {
                    int commonPrefixLength2 = stringSegment.getCommonPrefixLength(this.decimalSeparator);
                    z5 = z5 || commonPrefixLength2 == stringSegment.length();
                    if (commonPrefixLength2 == this.decimalSeparator.length()) {
                        str = this.decimalSeparator;
                        z2 = true;
                        if (str2 != null) {
                            int commonPrefixLength3 = stringSegment.getCommonPrefixLength(str2);
                            z5 = z5 || commonPrefixLength3 == stringSegment.length();
                            if (commonPrefixLength3 == str2.length()) {
                                z3 = true;
                                if (!this.groupingDisabled || str2 != null || str != null || this.groupingSeparator.isEmpty()) {
                                    z4 = z2;
                                } else {
                                    int commonPrefixLength4 = stringSegment.getCommonPrefixLength(this.groupingSeparator);
                                    z4 = z2;
                                    z5 = z5 || commonPrefixLength4 == stringSegment.length();
                                    if (commonPrefixLength4 == this.groupingSeparator.length()) {
                                        str2 = this.groupingSeparator;
                                        z3 = true;
                                    }
                                }
                                if (!z3 && str == null && this.decimalUniSet.contains(codePoint)) {
                                    str = UCharacter.toString(codePoint);
                                    z4 = true;
                                }
                                if (!this.groupingDisabled && str2 == null && str == null && this.groupingUniSet.contains(codePoint)) {
                                    str2 = UCharacter.toString(codePoint);
                                    z3 = true;
                                }
                                if ((!z4 && !z3) || ((z4 && this.integerOnly) || (i7 == 2 && z3))) {
                                    break;
                                }
                                validateGroup = validateGroup(i8, i9, false);
                                boolean validateGroup2 = validateGroup(i7, i5, true);
                                if (!validateGroup && (!z4 || validateGroup2)) {
                                    if (this.requireGroupingMatch && i5 == 0 && i7 == 1) {
                                        break;
                                    }
                                    i8 = z4 ? -1 : i7;
                                    int offset2 = i5 != 0 ? stringSegment.getOffset() : i4;
                                    i7 = z3 ? 1 : 2;
                                    if (z3) {
                                        stringSegment.adjustOffset(str2.length());
                                    } else {
                                        stringSegment.adjustOffset(str.length());
                                    }
                                    i10 = i4;
                                    i9 = i5;
                                    i5 = 0;
                                    i4 = offset2;
                                } else {
                                    break;
                                }
                            }
                        }
                        z3 = false;
                        if (!this.groupingDisabled) {
                        }
                        z4 = z2;
                        str = UCharacter.toString(codePoint);
                        z4 = true;
                        str2 = UCharacter.toString(codePoint);
                        z3 = true;
                        validateGroup = validateGroup(i8, i9, false);
                        boolean validateGroup22 = validateGroup(i7, i5, true);
                        if (!validateGroup) {
                            break;
                        }
                        break;
                    }
                }
                z2 = false;
                if (str2 != null) {
                }
                z3 = false;
                if (!this.groupingDisabled) {
                }
                z4 = z2;
                str = UCharacter.toString(codePoint);
                z4 = true;
                str2 = UCharacter.toString(codePoint);
                z3 = true;
                validateGroup = validateGroup(i8, i9, false);
                boolean validateGroup222 = validateGroup(i7, i5, true);
                if (!validateGroup) {
                }
            }
        }
        boolean z6 = z5;
        int i12 = 2;
        if (i7 == i12 || i5 != 0) {
            i2 = i10;
            r3 = 0;
        } else {
            stringSegment.setOffset(i4);
            i7 = i8;
            i5 = i9;
            i4 = i10;
            i2 = -1;
            r3 = 0;
            z6 = true;
            i8 = 0;
            i9 = 1;
        }
        boolean validateGroup3 = validateGroup(i8, i9, r3);
        boolean z7 = true;
        boolean validateGroup4 = validateGroup(i7, i5, true);
        if (!this.requireGroupingMatch) {
            if (!validateGroup3) {
                stringSegment.setOffset(i2);
                i3 = i9 + r3 + i5;
            } else if (validateGroup4 || (i8 == 0 && i9 == 0)) {
                i3 = 0;
            } else {
                stringSegment.setOffset(i4);
                int i13 = r3 == true ? 1 : 0;
                int i14 = r3 == true ? 1 : 0;
                int i15 = r3 == true ? 1 : 0;
                i3 = i13 + i5;
                z6 = true;
            }
            if (i3 != 0) {
                decimalQuantity_DualStorageBCD.adjustMagnitude(-i3);
                decimalQuantity_DualStorageBCD.truncate();
            }
            validateGroup3 = true;
            validateGroup4 = true;
        }
        if (i7 != 2 && (!validateGroup3 || !validateGroup4)) {
            decimalQuantity_DualStorageBCD = null;
        }
        if (decimalQuantity_DualStorageBCD == null) {
            if (!z6 && stringSegment.length() != 0) {
                z7 = false;
            }
            stringSegment.setOffset(offset);
            return z7;
        }
        decimalQuantity_DualStorageBCD.adjustMagnitude(-i6);
        if (i == 0 || stringSegment.getOffset() == offset) {
            parsedNumber2 = parsedNumber;
            z = false;
            parsedNumber2.quantity = decimalQuantity_DualStorageBCD;
        } else {
            if (decimalQuantity_DualStorageBCD.fitsInLong()) {
                z = false;
                long j = decimalQuantity_DualStorageBCD.toLong(false);
                if (j <= 2147483647L) {
                    parsedNumber2 = parsedNumber;
                    try {
                        parsedNumber2.quantity.adjustMagnitude(((int) j) * i);
                        objArr = null;
                    } catch (ArithmeticException unused) {
                    }
                    if (objArr != null) {
                        if (i == -1) {
                            parsedNumber2.quantity.clear();
                        } else {
                            parsedNumber2.quantity = null;
                            parsedNumber2.flags |= 128;
                        }
                    }
                } else {
                    parsedNumber2 = parsedNumber;
                }
            } else {
                parsedNumber2 = parsedNumber;
                z = false;
            }
            objArr = 1;
            if (objArr != null) {
            }
        }
        if (str != null) {
            parsedNumber2.flags |= 32;
        }
        parsedNumber2.setCharsConsumed(stringSegment);
        if (stringSegment.length() == 0 || z6) {
            return true;
        }
        return z;
    }

    private boolean validateGroup(int i, int i2, boolean z) {
        if (this.requireGroupingMatch) {
            if (i == -1) {
                return true;
            }
            if (i == 0) {
                if (z) {
                    return true;
                }
                return i2 != 0 && i2 <= this.grouping2;
            } else if (i == 1) {
                return z ? i2 == this.grouping1 : i2 == this.grouping2;
            } else {
                return true;
            }
        } else if (i == 1) {
            return i2 != 1;
        } else {
            return true;
        }
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean smokeTest(StringSegment stringSegment) {
        UnicodeSet unicodeSet;
        if (this.digitStrings == null && (unicodeSet = this.leadSet) != null) {
            return stringSegment.startsWith(unicodeSet);
        }
        if (stringSegment.startsWith(this.separatorSet) || UCharacter.isDigit(stringSegment.getCodePoint())) {
            return true;
        }
        if (this.digitStrings == null) {
            return false;
        }
        int i = 0;
        while (true) {
            String[] strArr = this.digitStrings;
            if (i >= strArr.length) {
                return false;
            }
            if (stringSegment.startsWith(strArr[i])) {
                return true;
            }
            i++;
        }
    }
}
