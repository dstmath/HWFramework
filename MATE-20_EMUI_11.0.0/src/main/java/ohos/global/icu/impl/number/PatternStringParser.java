package ohos.global.icu.impl.number;

import ohos.global.icu.impl.coll.Collation;
import ohos.global.icu.impl.number.Padder;

public class PatternStringParser {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int IGNORE_ROUNDING_ALWAYS = 2;
    public static final int IGNORE_ROUNDING_IF_CURRENCY = 1;
    public static final int IGNORE_ROUNDING_NEVER = 0;

    public static class ParsedSubpatternInfo {
        public boolean exponentHasPlusSign = false;
        public int exponentZeros = 0;
        public int fractionHashSigns = 0;
        public int fractionNumerals = 0;
        public int fractionTotal = 0;
        public long groupingSizes = 281474976645120L;
        public boolean hasCurrencySign = false;
        public boolean hasDecimal = false;
        public boolean hasMinusSign = false;
        public boolean hasPerMilleSign = false;
        public boolean hasPercentSign = false;
        public boolean hasPlusSign = false;
        public int integerAtSigns = 0;
        public int integerLeadingHashSigns = 0;
        public int integerNumerals = 0;
        public int integerTotal = 0;
        public int integerTrailingHashSigns = 0;
        public long paddingEndpoints = 0;
        public Padder.PadPosition paddingLocation = null;
        public long prefixEndpoints = 0;
        public DecimalQuantity_DualStorageBCD rounding = null;
        public long suffixEndpoints = 0;
        public int widthExceptAffixes = 0;
    }

    public static ParsedPatternInfo parseToPatternInfo(String str) {
        ParserState parserState = new ParserState(str);
        ParsedPatternInfo parsedPatternInfo = new ParsedPatternInfo(str);
        consumePattern(parserState, parsedPatternInfo);
        return parsedPatternInfo;
    }

    public static DecimalFormatProperties parseToProperties(String str, int i) {
        DecimalFormatProperties decimalFormatProperties = new DecimalFormatProperties();
        parseToExistingPropertiesImpl(str, decimalFormatProperties, i);
        return decimalFormatProperties;
    }

    public static DecimalFormatProperties parseToProperties(String str) {
        return parseToProperties(str, 0);
    }

    public static void parseToExistingProperties(String str, DecimalFormatProperties decimalFormatProperties, int i) {
        parseToExistingPropertiesImpl(str, decimalFormatProperties, i);
    }

    public static void parseToExistingProperties(String str, DecimalFormatProperties decimalFormatProperties) {
        parseToExistingProperties(str, decimalFormatProperties, 0);
    }

    public static class ParsedPatternInfo implements AffixPatternProvider {
        public ParsedSubpatternInfo negative;
        public String pattern;
        public ParsedSubpatternInfo positive;

        public static int getLengthFromEndpoints(long j) {
            return ((int) (j >>> 32)) - ((int) (-1 & j));
        }

        private ParsedPatternInfo(String str) {
            this.pattern = str;
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public char charAt(int i, int i2) {
            long endpoints = getEndpoints(i);
            int i3 = (int) (-1 & endpoints);
            int i4 = (int) (endpoints >>> 32);
            if (i2 >= 0 && i2 < i4 - i3) {
                return this.pattern.charAt(i3 + i2);
            }
            throw new IndexOutOfBoundsException();
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public int length(int i) {
            return getLengthFromEndpoints(getEndpoints(i));
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public String getString(int i) {
            long endpoints = getEndpoints(i);
            int i2 = (int) (-1 & endpoints);
            int i3 = (int) (endpoints >>> 32);
            if (i2 == i3) {
                return "";
            }
            return this.pattern.substring(i2, i3);
        }

        private long getEndpoints(int i) {
            boolean z = true;
            boolean z2 = (i & 256) != 0;
            boolean z3 = (i & 512) != 0;
            if ((i & 1024) == 0) {
                z = false;
            }
            if (z3 && z) {
                return this.negative.paddingEndpoints;
            }
            if (z) {
                return this.positive.paddingEndpoints;
            }
            if (z2 && z3) {
                return this.negative.prefixEndpoints;
            }
            if (z2) {
                return this.positive.prefixEndpoints;
            }
            if (z3) {
                return this.negative.suffixEndpoints;
            }
            return this.positive.suffixEndpoints;
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public boolean positiveHasPlusSign() {
            return this.positive.hasPlusSign;
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public boolean hasNegativeSubpattern() {
            return this.negative != null;
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public boolean negativeHasMinusSign() {
            return this.negative.hasMinusSign;
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public boolean hasCurrencySign() {
            ParsedSubpatternInfo parsedSubpatternInfo;
            return this.positive.hasCurrencySign || ((parsedSubpatternInfo = this.negative) != null && parsedSubpatternInfo.hasCurrencySign);
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public boolean containsSymbolType(int i) {
            return AffixUtils.containsType(this.pattern, i);
        }

        @Override // ohos.global.icu.impl.number.AffixPatternProvider
        public boolean hasBody() {
            return this.positive.integerTotal > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ParserState {
        int offset = 0;
        final String pattern;

        ParserState(String str) {
            this.pattern = str;
        }

        /* access modifiers changed from: package-private */
        public int peek() {
            if (this.offset == this.pattern.length()) {
                return -1;
            }
            return this.pattern.codePointAt(this.offset);
        }

        /* access modifiers changed from: package-private */
        public int next() {
            int peek = peek();
            this.offset += Character.charCount(peek);
            return peek;
        }

        /* access modifiers changed from: package-private */
        public IllegalArgumentException toParseException(String str) {
            return new IllegalArgumentException("Malformed pattern for ICU DecimalFormat: \"" + this.pattern + "\": " + str + " at position " + this.offset);
        }
    }

    private static void consumePattern(ParserState parserState, ParsedPatternInfo parsedPatternInfo) {
        parsedPatternInfo.positive = new ParsedSubpatternInfo();
        consumeSubpattern(parserState, parsedPatternInfo.positive);
        if (parserState.peek() == 59) {
            parserState.next();
            if (parserState.peek() != -1) {
                parsedPatternInfo.negative = new ParsedSubpatternInfo();
                consumeSubpattern(parserState, parsedPatternInfo.negative);
            }
        }
        if (parserState.peek() != -1) {
            throw parserState.toParseException("Found unquoted special character");
        }
    }

    private static void consumeSubpattern(ParserState parserState, ParsedSubpatternInfo parsedSubpatternInfo) {
        consumePadding(parserState, parsedSubpatternInfo, Padder.PadPosition.BEFORE_PREFIX);
        parsedSubpatternInfo.prefixEndpoints = consumeAffix(parserState, parsedSubpatternInfo);
        consumePadding(parserState, parsedSubpatternInfo, Padder.PadPosition.AFTER_PREFIX);
        consumeFormat(parserState, parsedSubpatternInfo);
        consumeExponent(parserState, parsedSubpatternInfo);
        consumePadding(parserState, parsedSubpatternInfo, Padder.PadPosition.BEFORE_SUFFIX);
        parsedSubpatternInfo.suffixEndpoints = consumeAffix(parserState, parsedSubpatternInfo);
        consumePadding(parserState, parsedSubpatternInfo, Padder.PadPosition.AFTER_SUFFIX);
    }

    private static void consumePadding(ParserState parserState, ParsedSubpatternInfo parsedSubpatternInfo, Padder.PadPosition padPosition) {
        if (parserState.peek() == 42) {
            if (parsedSubpatternInfo.paddingLocation == null) {
                parsedSubpatternInfo.paddingLocation = padPosition;
                parserState.next();
                parsedSubpatternInfo.paddingEndpoints |= (long) parserState.offset;
                consumeLiteral(parserState);
                parsedSubpatternInfo.paddingEndpoints |= ((long) parserState.offset) << 32;
                return;
            }
            throw parserState.toParseException("Cannot have multiple pad specifiers");
        }
    }

    private static long consumeAffix(ParserState parserState, ParsedSubpatternInfo parsedSubpatternInfo) {
        long j = (long) parserState.offset;
        while (true) {
            int peek = parserState.peek();
            if (!(peek == -1 || peek == 35)) {
                if (peek == 37) {
                    parsedSubpatternInfo.hasPercentSign = true;
                } else if (!(peek == 59 || peek == 64)) {
                    if (peek == 164) {
                        parsedSubpatternInfo.hasCurrencySign = true;
                    } else if (peek != 8240) {
                        switch (peek) {
                            case 42:
                            case 44:
                            case 46:
                                break;
                            case 43:
                                parsedSubpatternInfo.hasPlusSign = true;
                                continue;
                            case 45:
                                parsedSubpatternInfo.hasMinusSign = true;
                                continue;
                            default:
                                switch (peek) {
                                    case 48:
                                    case 49:
                                    case 50:
                                    case 51:
                                    case 52:
                                    case 53:
                                    case 54:
                                    case 55:
                                    case 56:
                                    case 57:
                                        break;
                                    default:
                                        continue;
                                }
                        }
                    } else {
                        parsedSubpatternInfo.hasPerMilleSign = true;
                    }
                }
                consumeLiteral(parserState);
            }
        }
        return (((long) parserState.offset) << 32) | j;
    }

    private static void consumeLiteral(ParserState parserState) {
        if (parserState.peek() == -1) {
            throw parserState.toParseException("Expected unquoted literal but found EOL");
        } else if (parserState.peek() == 39) {
            parserState.next();
            while (parserState.peek() != 39) {
                if (parserState.peek() != -1) {
                    parserState.next();
                } else {
                    throw parserState.toParseException("Expected quoted literal but found EOL");
                }
            }
            parserState.next();
        } else {
            parserState.next();
        }
    }

    private static void consumeFormat(ParserState parserState, ParsedSubpatternInfo parsedSubpatternInfo) {
        consumeIntegerFormat(parserState, parsedSubpatternInfo);
        if (parserState.peek() == 46) {
            parserState.next();
            parsedSubpatternInfo.hasDecimal = true;
            parsedSubpatternInfo.widthExceptAffixes++;
            consumeFractionFormat(parserState, parsedSubpatternInfo);
        }
    }

    private static void consumeIntegerFormat(ParserState parserState, ParsedSubpatternInfo parsedSubpatternInfo) {
        while (true) {
            int peek = parserState.peek();
            if (peek != 35) {
                if (peek == 44) {
                    parsedSubpatternInfo.widthExceptAffixes++;
                    parsedSubpatternInfo.groupingSizes <<= 16;
                } else if (peek != 64) {
                    switch (peek) {
                        default:
                            short s = (short) ((int) (parsedSubpatternInfo.groupingSizes & 65535));
                            short s2 = (short) ((int) ((parsedSubpatternInfo.groupingSizes >>> 16) & 65535));
                            short s3 = (short) ((int) (65535 & (parsedSubpatternInfo.groupingSizes >>> 32)));
                            if (s == 0 && s2 != -1) {
                                throw parserState.toParseException("Trailing grouping separator is invalid");
                            } else if (s2 == 0 && s3 != -1) {
                                throw parserState.toParseException("Grouping width of zero is invalid");
                            } else {
                                return;
                            }
                        case 48:
                        case 49:
                        case 50:
                        case 51:
                        case 52:
                        case 53:
                        case 54:
                        case 55:
                        case 56:
                        case 57:
                            if (parsedSubpatternInfo.integerAtSigns <= 0) {
                                parsedSubpatternInfo.widthExceptAffixes++;
                                parsedSubpatternInfo.groupingSizes++;
                                parsedSubpatternInfo.integerNumerals++;
                                parsedSubpatternInfo.integerTotal++;
                                if (parserState.peek() != 48 && parsedSubpatternInfo.rounding == null) {
                                    parsedSubpatternInfo.rounding = new DecimalQuantity_DualStorageBCD();
                                }
                                if (parsedSubpatternInfo.rounding != null) {
                                    parsedSubpatternInfo.rounding.appendDigit((byte) (parserState.peek() - 48), 0, true);
                                    break;
                                } else {
                                    continue;
                                }
                            } else {
                                throw parserState.toParseException("Cannot mix @ and 0");
                            }
                    }
                } else if (parsedSubpatternInfo.integerNumerals > 0) {
                    throw parserState.toParseException("Cannot mix 0 and @");
                } else if (parsedSubpatternInfo.integerTrailingHashSigns <= 0) {
                    parsedSubpatternInfo.widthExceptAffixes++;
                    parsedSubpatternInfo.groupingSizes++;
                    parsedSubpatternInfo.integerAtSigns++;
                    parsedSubpatternInfo.integerTotal++;
                } else {
                    throw parserState.toParseException("Cannot nest # inside of a run of @");
                }
            } else if (parsedSubpatternInfo.integerNumerals <= 0) {
                parsedSubpatternInfo.widthExceptAffixes++;
                parsedSubpatternInfo.groupingSizes++;
                if (parsedSubpatternInfo.integerAtSigns > 0) {
                    parsedSubpatternInfo.integerTrailingHashSigns++;
                } else {
                    parsedSubpatternInfo.integerLeadingHashSigns++;
                }
                parsedSubpatternInfo.integerTotal++;
            } else {
                throw parserState.toParseException("# cannot follow 0 before decimal point");
            }
            parserState.next();
        }
    }

    private static void consumeFractionFormat(ParserState parserState, ParsedSubpatternInfo parsedSubpatternInfo) {
        int i = 0;
        while (true) {
            int peek = parserState.peek();
            if (peek != 35) {
                switch (peek) {
                    case 48:
                    case 49:
                    case 50:
                    case 51:
                    case 52:
                    case 53:
                    case 54:
                    case 55:
                    case 56:
                    case 57:
                        if (parsedSubpatternInfo.fractionHashSigns <= 0) {
                            parsedSubpatternInfo.widthExceptAffixes++;
                            parsedSubpatternInfo.fractionNumerals++;
                            parsedSubpatternInfo.fractionTotal++;
                            if (parserState.peek() != 48) {
                                if (parsedSubpatternInfo.rounding == null) {
                                    parsedSubpatternInfo.rounding = new DecimalQuantity_DualStorageBCD();
                                }
                                parsedSubpatternInfo.rounding.appendDigit((byte) (parserState.peek() - 48), i, false);
                                i = 0;
                                parserState.next();
                            }
                        } else {
                            throw parserState.toParseException("0 cannot follow # after decimal point");
                        }
                        break;
                    default:
                        return;
                }
            } else {
                parsedSubpatternInfo.widthExceptAffixes++;
                parsedSubpatternInfo.fractionHashSigns++;
                parsedSubpatternInfo.fractionTotal++;
            }
            i++;
            parserState.next();
        }
    }

    private static void consumeExponent(ParserState parserState, ParsedSubpatternInfo parsedSubpatternInfo) {
        if (parserState.peek() == 69) {
            if ((parsedSubpatternInfo.groupingSizes & Collation.MAX_PRIMARY) == Collation.MAX_PRIMARY) {
                parserState.next();
                parsedSubpatternInfo.widthExceptAffixes++;
                if (parserState.peek() == 43) {
                    parserState.next();
                    parsedSubpatternInfo.exponentHasPlusSign = true;
                    parsedSubpatternInfo.widthExceptAffixes++;
                }
                while (parserState.peek() == 48) {
                    parserState.next();
                    parsedSubpatternInfo.exponentZeros++;
                    parsedSubpatternInfo.widthExceptAffixes++;
                }
                return;
            }
            throw parserState.toParseException("Cannot have grouping separator in scientific notation");
        }
    }

    private static void parseToExistingPropertiesImpl(String str, DecimalFormatProperties decimalFormatProperties, int i) {
        if (str == null || str.length() == 0) {
            decimalFormatProperties.clear();
        } else {
            patternInfoToProperties(decimalFormatProperties, parseToPatternInfo(str), i);
        }
    }

    private static void patternInfoToProperties(DecimalFormatProperties decimalFormatProperties, ParsedPatternInfo parsedPatternInfo, int i) {
        boolean z;
        int i2;
        int i3;
        ParsedSubpatternInfo parsedSubpatternInfo = parsedPatternInfo.positive;
        if (i == 0) {
            z = false;
        } else {
            z = i == 1 ? parsedSubpatternInfo.hasCurrencySign : true;
        }
        short s = (short) ((int) (parsedSubpatternInfo.groupingSizes & 65535));
        short s2 = (short) ((int) ((parsedSubpatternInfo.groupingSizes >>> 16) & 65535));
        short s3 = (short) ((int) (65535 & (parsedSubpatternInfo.groupingSizes >>> 32)));
        if (s2 != -1) {
            decimalFormatProperties.setGroupingSize(s);
            decimalFormatProperties.setGroupingUsed(true);
        } else {
            decimalFormatProperties.setGroupingSize(-1);
            decimalFormatProperties.setGroupingUsed(false);
        }
        if (s3 != -1) {
            decimalFormatProperties.setSecondaryGroupingSize(s2);
        } else {
            decimalFormatProperties.setSecondaryGroupingSize(-1);
        }
        if (parsedSubpatternInfo.integerTotal == 0 && parsedSubpatternInfo.fractionTotal > 0) {
            i3 = Math.max(1, parsedSubpatternInfo.fractionNumerals);
            i2 = 0;
        } else if (parsedSubpatternInfo.integerNumerals == 0 && parsedSubpatternInfo.fractionNumerals == 0) {
            i3 = 0;
            i2 = 1;
        } else {
            i2 = parsedSubpatternInfo.integerNumerals;
            i3 = parsedSubpatternInfo.fractionNumerals;
        }
        if (parsedSubpatternInfo.integerAtSigns > 0) {
            decimalFormatProperties.setMinimumFractionDigits(-1);
            decimalFormatProperties.setMaximumFractionDigits(-1);
            decimalFormatProperties.setRoundingIncrement(null);
            decimalFormatProperties.setMinimumSignificantDigits(parsedSubpatternInfo.integerAtSigns);
            decimalFormatProperties.setMaximumSignificantDigits(parsedSubpatternInfo.integerAtSigns + parsedSubpatternInfo.integerTrailingHashSigns);
        } else if (parsedSubpatternInfo.rounding != null) {
            if (!z) {
                decimalFormatProperties.setMinimumFractionDigits(i3);
                decimalFormatProperties.setMaximumFractionDigits(parsedSubpatternInfo.fractionTotal);
                decimalFormatProperties.setRoundingIncrement(parsedSubpatternInfo.rounding.toBigDecimal().setScale(parsedSubpatternInfo.fractionNumerals));
            } else {
                decimalFormatProperties.setMinimumFractionDigits(-1);
                decimalFormatProperties.setMaximumFractionDigits(-1);
                decimalFormatProperties.setRoundingIncrement(null);
            }
            decimalFormatProperties.setMinimumSignificantDigits(-1);
            decimalFormatProperties.setMaximumSignificantDigits(-1);
        } else {
            if (!z) {
                decimalFormatProperties.setMinimumFractionDigits(i3);
                decimalFormatProperties.setMaximumFractionDigits(parsedSubpatternInfo.fractionTotal);
                decimalFormatProperties.setRoundingIncrement(null);
            } else {
                decimalFormatProperties.setMinimumFractionDigits(-1);
                decimalFormatProperties.setMaximumFractionDigits(-1);
                decimalFormatProperties.setRoundingIncrement(null);
            }
            decimalFormatProperties.setMinimumSignificantDigits(-1);
            decimalFormatProperties.setMaximumSignificantDigits(-1);
        }
        if (!parsedSubpatternInfo.hasDecimal || parsedSubpatternInfo.fractionTotal != 0) {
            decimalFormatProperties.setDecimalSeparatorAlwaysShown(false);
        } else {
            decimalFormatProperties.setDecimalSeparatorAlwaysShown(true);
        }
        if (parsedSubpatternInfo.exponentZeros > 0) {
            decimalFormatProperties.setExponentSignAlwaysShown(parsedSubpatternInfo.exponentHasPlusSign);
            decimalFormatProperties.setMinimumExponentDigits(parsedSubpatternInfo.exponentZeros);
            if (parsedSubpatternInfo.integerAtSigns == 0) {
                decimalFormatProperties.setMinimumIntegerDigits(parsedSubpatternInfo.integerNumerals);
                decimalFormatProperties.setMaximumIntegerDigits(parsedSubpatternInfo.integerTotal);
            } else {
                decimalFormatProperties.setMinimumIntegerDigits(1);
                decimalFormatProperties.setMaximumIntegerDigits(-1);
            }
        } else {
            decimalFormatProperties.setExponentSignAlwaysShown(false);
            decimalFormatProperties.setMinimumExponentDigits(-1);
            decimalFormatProperties.setMinimumIntegerDigits(i2);
            decimalFormatProperties.setMaximumIntegerDigits(-1);
        }
        String string = parsedPatternInfo.getString(256);
        String string2 = parsedPatternInfo.getString(0);
        if (parsedSubpatternInfo.paddingLocation != null) {
            decimalFormatProperties.setFormatWidth(parsedSubpatternInfo.widthExceptAffixes + AffixUtils.estimateLength(string) + AffixUtils.estimateLength(string2));
            String string3 = parsedPatternInfo.getString(1024);
            if (string3.length() == 1) {
                decimalFormatProperties.setPadString(string3);
            } else if (string3.length() != 2) {
                decimalFormatProperties.setPadString(string3.substring(1, string3.length() - 1));
            } else if (string3.charAt(0) == '\'') {
                decimalFormatProperties.setPadString("'");
            } else {
                decimalFormatProperties.setPadString(string3);
            }
            decimalFormatProperties.setPadPosition(parsedSubpatternInfo.paddingLocation);
        } else {
            decimalFormatProperties.setFormatWidth(-1);
            decimalFormatProperties.setPadString(null);
            decimalFormatProperties.setPadPosition(null);
        }
        decimalFormatProperties.setPositivePrefixPattern(string);
        decimalFormatProperties.setPositiveSuffixPattern(string2);
        if (parsedPatternInfo.negative != null) {
            decimalFormatProperties.setNegativePrefixPattern(parsedPatternInfo.getString(768));
            decimalFormatProperties.setNegativeSuffixPattern(parsedPatternInfo.getString(512));
        } else {
            decimalFormatProperties.setNegativePrefixPattern(null);
            decimalFormatProperties.setNegativeSuffixPattern(null);
        }
        if (parsedSubpatternInfo.hasPercentSign) {
            decimalFormatProperties.setMagnitudeMultiplier(2);
        } else if (parsedSubpatternInfo.hasPerMilleSign) {
            decimalFormatProperties.setMagnitudeMultiplier(3);
        } else {
            decimalFormatProperties.setMagnitudeMultiplier(0);
        }
    }
}
