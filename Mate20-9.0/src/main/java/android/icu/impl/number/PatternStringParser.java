package android.icu.impl.number;

import android.icu.impl.coll.Collation;
import android.icu.impl.coll.CollationSettings;
import android.icu.impl.number.Padder;

public class PatternStringParser {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int IGNORE_ROUNDING_ALWAYS = 2;
    public static final int IGNORE_ROUNDING_IF_CURRENCY = 1;
    public static final int IGNORE_ROUNDING_NEVER = 0;

    public static class ParsedPatternInfo implements AffixPatternProvider {
        public ParsedSubpatternInfo negative;
        public String pattern;
        public ParsedSubpatternInfo positive;

        private ParsedPatternInfo(String pattern2) {
            this.pattern = pattern2;
        }

        public char charAt(int flags, int index) {
            long endpoints = getEndpoints(flags);
            int left = (int) (-1 & endpoints);
            int right = (int) (endpoints >>> 32);
            if (index >= 0 && index < right - left) {
                return this.pattern.charAt(left + index);
            }
            throw new IndexOutOfBoundsException();
        }

        public int length(int flags) {
            return getLengthFromEndpoints(getEndpoints(flags));
        }

        public static int getLengthFromEndpoints(long endpoints) {
            return ((int) (endpoints >>> 32)) - ((int) (-1 & endpoints));
        }

        public String getString(int flags) {
            long endpoints = getEndpoints(flags);
            int left = (int) (-1 & endpoints);
            int right = (int) (endpoints >>> 32);
            if (left == right) {
                return "";
            }
            return this.pattern.substring(left, right);
        }

        private long getEndpoints(int flags) {
            boolean padding = false;
            boolean prefix = (flags & 256) != 0;
            boolean isNegative = (flags & 512) != 0;
            if ((flags & 1024) != 0) {
                padding = true;
            }
            if (isNegative && padding) {
                return this.negative.paddingEndpoints;
            }
            if (padding) {
                return this.positive.paddingEndpoints;
            }
            if (prefix && isNegative) {
                return this.negative.prefixEndpoints;
            }
            if (prefix) {
                return this.positive.prefixEndpoints;
            }
            if (isNegative) {
                return this.negative.suffixEndpoints;
            }
            return this.positive.suffixEndpoints;
        }

        public boolean positiveHasPlusSign() {
            return this.positive.hasPlusSign;
        }

        public boolean hasNegativeSubpattern() {
            return this.negative != null;
        }

        public boolean negativeHasMinusSign() {
            return this.negative.hasMinusSign;
        }

        public boolean hasCurrencySign() {
            return this.positive.hasCurrencySign || (this.negative != null && this.negative.hasCurrencySign);
        }

        public boolean containsSymbolType(int type) {
            return AffixUtils.containsType(this.pattern, type);
        }
    }

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

    private static class ParserState {
        int offset = 0;
        final String pattern;

        ParserState(String pattern2) {
            this.pattern = pattern2;
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
            int codePoint = peek();
            this.offset += Character.charCount(codePoint);
            return codePoint;
        }

        /* access modifiers changed from: package-private */
        public IllegalArgumentException toParseException(String message) {
            return new IllegalArgumentException("Malformed pattern for ICU DecimalFormat: \"" + this.pattern + "\": " + message + " at position " + this.offset);
        }
    }

    public static ParsedPatternInfo parseToPatternInfo(String patternString) {
        ParserState state = new ParserState(patternString);
        ParsedPatternInfo result = new ParsedPatternInfo(patternString);
        consumePattern(state, result);
        return result;
    }

    public static DecimalFormatProperties parseToProperties(String pattern, int ignoreRounding) {
        DecimalFormatProperties properties = new DecimalFormatProperties();
        parseToExistingPropertiesImpl(pattern, properties, ignoreRounding);
        return properties;
    }

    public static DecimalFormatProperties parseToProperties(String pattern) {
        return parseToProperties(pattern, 0);
    }

    public static void parseToExistingProperties(String pattern, DecimalFormatProperties properties, int ignoreRounding) {
        parseToExistingPropertiesImpl(pattern, properties, ignoreRounding);
    }

    public static void parseToExistingProperties(String pattern, DecimalFormatProperties properties) {
        parseToExistingProperties(pattern, properties, 0);
    }

    private static void consumePattern(ParserState state, ParsedPatternInfo result) {
        result.positive = new ParsedSubpatternInfo();
        consumeSubpattern(state, result.positive);
        if (state.peek() == 59) {
            state.next();
            if (state.peek() != -1) {
                result.negative = new ParsedSubpatternInfo();
                consumeSubpattern(state, result.negative);
            }
        }
        if (state.peek() != -1) {
            throw state.toParseException("Found unquoted special character");
        }
    }

    private static void consumeSubpattern(ParserState state, ParsedSubpatternInfo result) {
        consumePadding(state, result, Padder.PadPosition.BEFORE_PREFIX);
        result.prefixEndpoints = consumeAffix(state, result);
        consumePadding(state, result, Padder.PadPosition.AFTER_PREFIX);
        consumeFormat(state, result);
        consumeExponent(state, result);
        consumePadding(state, result, Padder.PadPosition.BEFORE_SUFFIX);
        result.suffixEndpoints = consumeAffix(state, result);
        consumePadding(state, result, Padder.PadPosition.AFTER_SUFFIX);
    }

    private static void consumePadding(ParserState state, ParsedSubpatternInfo result, Padder.PadPosition paddingLocation) {
        if (state.peek() == 42) {
            if (result.paddingLocation == null) {
                result.paddingLocation = paddingLocation;
                state.next();
                result.paddingEndpoints |= (long) state.offset;
                consumeLiteral(state);
                result.paddingEndpoints |= ((long) state.offset) << 32;
                return;
            }
            throw state.toParseException("Cannot have multiple pad specifiers");
        }
    }

    private static long consumeAffix(ParserState state, ParsedSubpatternInfo result) {
        long endpoints = (long) state.offset;
        while (true) {
            int peek = state.peek();
            if (!(peek == -1 || peek == 35)) {
                if (peek == 37) {
                    result.hasPercentSign = true;
                } else if (!(peek == 59 || peek == 64)) {
                    if (peek == 164) {
                        result.hasCurrencySign = true;
                    } else if (peek != 8240) {
                        switch (peek) {
                            case 42:
                            case 44:
                            case 46:
                                break;
                            case 43:
                                result.hasPlusSign = true;
                                continue;
                            case 45:
                                result.hasMinusSign = true;
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
                        result.hasPerMilleSign = true;
                    }
                }
                consumeLiteral(state);
            }
        }
        return endpoints | (((long) state.offset) << 32);
    }

    private static void consumeLiteral(ParserState state) {
        if (state.peek() == -1) {
            throw state.toParseException("Expected unquoted literal but found EOL");
        } else if (state.peek() == 39) {
            state.next();
            while (state.peek() != 39) {
                if (state.peek() != -1) {
                    state.next();
                } else {
                    throw state.toParseException("Expected quoted literal but found EOL");
                }
            }
            state.next();
        } else {
            state.next();
        }
    }

    private static void consumeFormat(ParserState state, ParsedSubpatternInfo result) {
        consumeIntegerFormat(state, result);
        if (state.peek() == 46) {
            state.next();
            result.hasDecimal = true;
            result.widthExceptAffixes++;
            consumeFractionFormat(state, result);
        }
    }

    private static void consumeIntegerFormat(ParserState state, ParsedSubpatternInfo result) {
        while (true) {
            int peek = state.peek();
            if (peek != 35) {
                if (peek == 44) {
                    result.widthExceptAffixes++;
                    result.groupingSizes <<= 16;
                } else if (peek != 64) {
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
                            if (result.integerAtSigns <= 0) {
                                result.widthExceptAffixes++;
                                result.groupingSizes++;
                                result.integerNumerals++;
                                result.integerTotal++;
                                if (state.peek() != 48 && result.rounding == null) {
                                    result.rounding = new DecimalQuantity_DualStorageBCD();
                                }
                                if (result.rounding == null) {
                                    break;
                                } else {
                                    result.rounding.appendDigit((byte) (state.peek() - 48), 0, true);
                                    break;
                                }
                            } else {
                                throw state.toParseException("Cannot mix @ and 0");
                            }
                        default:
                            short grouping1 = (short) ((int) (result.groupingSizes & 65535));
                            short grouping2 = (short) ((int) ((result.groupingSizes >>> 16) & 65535));
                            short grouping3 = (short) ((int) (65535 & (result.groupingSizes >>> 32)));
                            if (grouping1 == 0 && grouping2 != -1) {
                                throw state.toParseException("Trailing grouping separator is invalid");
                            } else if (grouping2 == 0 && grouping3 != -1) {
                                throw state.toParseException("Grouping width of zero is invalid");
                            } else {
                                return;
                            }
                    }
                } else if (result.integerNumerals > 0) {
                    throw state.toParseException("Cannot mix 0 and @");
                } else if (result.integerTrailingHashSigns <= 0) {
                    result.widthExceptAffixes++;
                    result.groupingSizes++;
                    result.integerAtSigns++;
                    result.integerTotal++;
                } else {
                    throw state.toParseException("Cannot nest # inside of a run of @");
                }
            } else if (result.integerNumerals <= 0) {
                result.widthExceptAffixes++;
                result.groupingSizes++;
                if (result.integerAtSigns > 0) {
                    result.integerTrailingHashSigns++;
                } else {
                    result.integerLeadingHashSigns++;
                }
                result.integerTotal++;
            } else {
                throw state.toParseException("# cannot follow 0 before decimal point");
            }
            state.next();
        }
    }

    private static void consumeFractionFormat(ParserState state, ParsedSubpatternInfo result) {
        int zeroCounter = 0;
        while (true) {
            int peek = state.peek();
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
                        if (result.fractionHashSigns <= 0) {
                            result.widthExceptAffixes++;
                            result.fractionNumerals++;
                            result.fractionTotal++;
                            if (state.peek() != 48) {
                                if (result.rounding == null) {
                                    result.rounding = new DecimalQuantity_DualStorageBCD();
                                }
                                result.rounding.appendDigit((byte) (state.peek() - 48), zeroCounter, false);
                                zeroCounter = 0;
                                break;
                            } else {
                                zeroCounter++;
                                break;
                            }
                        } else {
                            throw state.toParseException("0 cannot follow # after decimal point");
                        }
                    default:
                        return;
                }
            } else {
                result.widthExceptAffixes++;
                result.fractionHashSigns++;
                result.fractionTotal++;
                zeroCounter++;
            }
            state.next();
        }
    }

    private static void consumeExponent(ParserState state, ParsedSubpatternInfo result) {
        if (state.peek() == 69) {
            if ((result.groupingSizes & Collation.MAX_PRIMARY) == Collation.MAX_PRIMARY) {
                state.next();
                result.widthExceptAffixes++;
                if (state.peek() == 43) {
                    state.next();
                    result.exponentHasPlusSign = true;
                    result.widthExceptAffixes++;
                }
                while (state.peek() == 48) {
                    state.next();
                    result.exponentZeros++;
                    result.widthExceptAffixes++;
                }
                return;
            }
            throw state.toParseException("Cannot have grouping separator in scientific notation");
        }
    }

    private static void parseToExistingPropertiesImpl(String pattern, DecimalFormatProperties properties, int ignoreRounding) {
        if (pattern == null || pattern.length() == 0) {
            properties.clear();
        } else {
            patternInfoToProperties(properties, parseToPatternInfo(pattern), ignoreRounding);
        }
    }

    private static void patternInfoToProperties(DecimalFormatProperties properties, ParsedPatternInfo patternInfo, int _ignoreRounding) {
        boolean ignoreRounding;
        int minFrac;
        int minInt;
        DecimalFormatProperties decimalFormatProperties = properties;
        ParsedPatternInfo parsedPatternInfo = patternInfo;
        int i = _ignoreRounding;
        ParsedSubpatternInfo positive = parsedPatternInfo.positive;
        if (i == 0) {
            ignoreRounding = false;
        } else if (i == 1) {
            ignoreRounding = positive.hasCurrencySign;
        } else {
            ignoreRounding = true;
        }
        short grouping1 = (short) ((int) (positive.groupingSizes & 65535));
        short grouping2 = (short) ((int) ((positive.groupingSizes >>> 16) & 65535));
        short grouping3 = (short) ((int) (65535 & (positive.groupingSizes >>> 32)));
        if (grouping2 != -1) {
            decimalFormatProperties.setGroupingSize(grouping1);
        } else {
            decimalFormatProperties.setGroupingSize(-1);
        }
        if (grouping3 != -1) {
            decimalFormatProperties.setSecondaryGroupingSize(grouping2);
        } else {
            decimalFormatProperties.setSecondaryGroupingSize(-1);
        }
        if (positive.integerTotal == 0 && positive.fractionTotal > 0) {
            minInt = 0;
            minFrac = Math.max(1, positive.fractionNumerals);
        } else if (positive.integerNumerals == 0 && positive.fractionNumerals == 0) {
            minInt = 1;
            minFrac = 0;
        } else {
            minInt = positive.integerNumerals;
            minFrac = positive.fractionNumerals;
        }
        if (positive.integerAtSigns > 0) {
            decimalFormatProperties.setMinimumFractionDigits(-1);
            decimalFormatProperties.setMaximumFractionDigits(-1);
            decimalFormatProperties.setRoundingIncrement(null);
            decimalFormatProperties.setMinimumSignificantDigits(positive.integerAtSigns);
            decimalFormatProperties.setMaximumSignificantDigits(positive.integerAtSigns + positive.integerTrailingHashSigns);
        } else if (positive.rounding != null) {
            if (!ignoreRounding) {
                decimalFormatProperties.setMinimumFractionDigits(minFrac);
                decimalFormatProperties.setMaximumFractionDigits(positive.fractionTotal);
                decimalFormatProperties.setRoundingIncrement(positive.rounding.toBigDecimal().setScale(positive.fractionNumerals));
            } else {
                decimalFormatProperties.setMinimumFractionDigits(-1);
                decimalFormatProperties.setMaximumFractionDigits(-1);
                decimalFormatProperties.setRoundingIncrement(null);
            }
            decimalFormatProperties.setMinimumSignificantDigits(-1);
            decimalFormatProperties.setMaximumSignificantDigits(-1);
        } else {
            if (!ignoreRounding) {
                decimalFormatProperties.setMinimumFractionDigits(minFrac);
                decimalFormatProperties.setMaximumFractionDigits(positive.fractionTotal);
                decimalFormatProperties.setRoundingIncrement(null);
            } else {
                decimalFormatProperties.setMinimumFractionDigits(-1);
                decimalFormatProperties.setMaximumFractionDigits(-1);
                decimalFormatProperties.setRoundingIncrement(null);
            }
            decimalFormatProperties.setMinimumSignificantDigits(-1);
            decimalFormatProperties.setMaximumSignificantDigits(-1);
        }
        if (!positive.hasDecimal || positive.fractionTotal != 0) {
            decimalFormatProperties.setDecimalSeparatorAlwaysShown(false);
        } else {
            decimalFormatProperties.setDecimalSeparatorAlwaysShown(true);
        }
        if (positive.exponentZeros > 0) {
            decimalFormatProperties.setExponentSignAlwaysShown(positive.exponentHasPlusSign);
            decimalFormatProperties.setMinimumExponentDigits(positive.exponentZeros);
            if (positive.integerAtSigns == 0) {
                decimalFormatProperties.setMinimumIntegerDigits(positive.integerNumerals);
                decimalFormatProperties.setMaximumIntegerDigits(positive.integerTotal);
            } else {
                decimalFormatProperties.setMinimumIntegerDigits(1);
                decimalFormatProperties.setMaximumIntegerDigits(-1);
            }
        } else {
            decimalFormatProperties.setExponentSignAlwaysShown(false);
            decimalFormatProperties.setMinimumExponentDigits(-1);
            decimalFormatProperties.setMinimumIntegerDigits(minInt);
            decimalFormatProperties.setMaximumIntegerDigits(-1);
        }
        String posPrefix = parsedPatternInfo.getString(256);
        String posSuffix = parsedPatternInfo.getString(0);
        if (positive.paddingLocation != null) {
            decimalFormatProperties.setFormatWidth(positive.widthExceptAffixes + AffixUtils.estimateLength(posPrefix) + AffixUtils.estimateLength(posSuffix));
            String rawPaddingString = parsedPatternInfo.getString(1024);
            if (rawPaddingString.length() == 1) {
                decimalFormatProperties.setPadString(rawPaddingString);
            } else if (rawPaddingString.length() != 2) {
                decimalFormatProperties.setPadString(rawPaddingString.substring(1, rawPaddingString.length() - 1));
            } else if (rawPaddingString.charAt(0) == '\'') {
                decimalFormatProperties.setPadString("'");
            } else {
                decimalFormatProperties.setPadString(rawPaddingString);
            }
            decimalFormatProperties.setPadPosition(positive.paddingLocation);
        } else {
            decimalFormatProperties.setFormatWidth(-1);
            decimalFormatProperties.setPadString(null);
            decimalFormatProperties.setPadPosition(null);
        }
        decimalFormatProperties.setPositivePrefixPattern(posPrefix);
        decimalFormatProperties.setPositiveSuffixPattern(posSuffix);
        if (parsedPatternInfo.negative != null) {
            decimalFormatProperties.setNegativePrefixPattern(parsedPatternInfo.getString(CollationSettings.CASE_FIRST_AND_UPPER_MASK));
            decimalFormatProperties.setNegativeSuffixPattern(parsedPatternInfo.getString(512));
        } else {
            decimalFormatProperties.setNegativePrefixPattern(null);
            decimalFormatProperties.setNegativeSuffixPattern(null);
        }
        if (positive.hasPercentSign) {
            decimalFormatProperties.setMagnitudeMultiplier(2);
        } else if (positive.hasPerMilleSign) {
            decimalFormatProperties.setMagnitudeMultiplier(3);
        } else {
            decimalFormatProperties.setMagnitudeMultiplier(0);
        }
    }
}
