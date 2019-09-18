package android.icu.impl.number;

import android.icu.impl.PatternTokenizer;
import android.icu.lang.UCharacter;
import android.icu.text.NumberFormat;

public class AffixUtils {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int STATE_AFTER_QUOTE = 3;
    private static final int STATE_BASE = 0;
    private static final int STATE_FIFTH_CURR = 8;
    private static final int STATE_FIRST_CURR = 4;
    private static final int STATE_FIRST_QUOTE = 1;
    private static final int STATE_FOURTH_CURR = 7;
    private static final int STATE_INSIDE_QUOTE = 2;
    private static final int STATE_OVERFLOW_CURR = 9;
    private static final int STATE_SECOND_CURR = 5;
    private static final int STATE_THIRD_CURR = 6;
    private static final int TYPE_CODEPOINT = 0;
    public static final int TYPE_CURRENCY_DOUBLE = -6;
    public static final int TYPE_CURRENCY_OVERFLOW = -15;
    public static final int TYPE_CURRENCY_QUAD = -8;
    public static final int TYPE_CURRENCY_QUINT = -9;
    public static final int TYPE_CURRENCY_SINGLE = -5;
    public static final int TYPE_CURRENCY_TRIPLE = -7;
    public static final int TYPE_MINUS_SIGN = -1;
    public static final int TYPE_PERCENT = -3;
    public static final int TYPE_PERMILLE = -4;
    public static final int TYPE_PLUS_SIGN = -2;

    public interface SymbolProvider {
        CharSequence getSymbol(int i);
    }

    public static int estimateLength(CharSequence patternString) {
        int length = 0;
        if (patternString == null) {
            return 0;
        }
        int state = 0;
        int offset = 0;
        while (offset < patternString.length()) {
            int cp = Character.codePointAt(patternString, offset);
            switch (state) {
                case 0:
                    if (cp != 39) {
                        length++;
                        break;
                    } else {
                        state = 1;
                        break;
                    }
                case 1:
                    if (cp != 39) {
                        length++;
                        state = 2;
                        break;
                    } else {
                        length++;
                        state = 0;
                        break;
                    }
                case 2:
                    if (cp != 39) {
                        length++;
                        break;
                    } else {
                        state = 3;
                        break;
                    }
                case 3:
                    if (cp != 39) {
                        length++;
                        break;
                    } else {
                        length++;
                        state = 2;
                        break;
                    }
                default:
                    throw new AssertionError();
            }
            offset += Character.charCount(cp);
        }
        switch (state) {
            case 1:
            case 2:
                throw new IllegalArgumentException("Unterminated quote: \"" + patternString + "\"");
            default:
                return length;
        }
    }

    public static int escape(CharSequence input, StringBuilder output) {
        if (input == null) {
            return 0;
        }
        int state = 0;
        int offset = 0;
        int startLength = output.length();
        while (offset < input.length()) {
            int cp = Character.codePointAt(input, offset);
            if (cp != 37) {
                if (cp == 39) {
                    output.append("''");
                } else if (!(cp == 43 || cp == 45 || cp == 164 || cp == 8240)) {
                    if (state == 2) {
                        output.append(PatternTokenizer.SINGLE_QUOTE);
                        output.appendCodePoint(cp);
                        state = 0;
                    } else {
                        output.appendCodePoint(cp);
                    }
                }
                offset += Character.charCount(cp);
            }
            if (state == 0) {
                output.append(PatternTokenizer.SINGLE_QUOTE);
                output.appendCodePoint(cp);
                state = 2;
            } else {
                output.appendCodePoint(cp);
            }
            offset += Character.charCount(cp);
        }
        if (state == 2) {
            output.append(PatternTokenizer.SINGLE_QUOTE);
        }
        return output.length() - startLength;
    }

    public static String escape(CharSequence input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        escape(input, sb);
        return sb.toString();
    }

    public static final NumberFormat.Field getFieldForType(int type) {
        if (type == -15) {
            return NumberFormat.Field.CURRENCY;
        }
        switch (type) {
            case TYPE_CURRENCY_QUINT /*-9*/:
                return NumberFormat.Field.CURRENCY;
            case TYPE_CURRENCY_QUAD /*-8*/:
                return NumberFormat.Field.CURRENCY;
            case TYPE_CURRENCY_TRIPLE /*-7*/:
                return NumberFormat.Field.CURRENCY;
            case TYPE_CURRENCY_DOUBLE /*-6*/:
                return NumberFormat.Field.CURRENCY;
            case TYPE_CURRENCY_SINGLE /*-5*/:
                return NumberFormat.Field.CURRENCY;
            case TYPE_PERMILLE /*-4*/:
                return NumberFormat.Field.PERMILLE;
            case TYPE_PERCENT /*-3*/:
                return NumberFormat.Field.PERCENT;
            case -2:
                return NumberFormat.Field.SIGN;
            case -1:
                return NumberFormat.Field.SIGN;
            default:
                throw new AssertionError();
        }
    }

    public static int unescape(CharSequence affixPattern, NumberStringBuilder output, int position, SymbolProvider provider) {
        int length = 0;
        long tag = 0;
        while (hasNext(tag, affixPattern)) {
            tag = nextToken(tag, affixPattern);
            int typeOrCp = getTypeOrCp(tag);
            if (typeOrCp == -15) {
                length += output.insertCodePoint(position + length, UCharacter.REPLACEMENT_CHAR, NumberFormat.Field.CURRENCY);
            } else if (typeOrCp < 0) {
                length += output.insert(position + length, provider.getSymbol(typeOrCp), getFieldForType(typeOrCp));
            } else {
                length += output.insertCodePoint(position + length, typeOrCp, null);
            }
        }
        return length;
    }

    public static int unescapedCodePointCount(CharSequence affixPattern, SymbolProvider provider) {
        int length = 0;
        long tag = 0;
        while (hasNext(tag, affixPattern)) {
            tag = nextToken(tag, affixPattern);
            int typeOrCp = getTypeOrCp(tag);
            if (typeOrCp == -15) {
                length++;
            } else if (typeOrCp < 0) {
                CharSequence symbol = provider.getSymbol(typeOrCp);
                length += Character.codePointCount(symbol, 0, symbol.length());
            } else {
                length++;
            }
        }
        return length;
    }

    public static boolean containsType(CharSequence affixPattern, int type) {
        if (affixPattern == null || affixPattern.length() == 0) {
            return false;
        }
        long tag = 0;
        while (hasNext(tag, affixPattern)) {
            tag = nextToken(tag, affixPattern);
            if (getTypeOrCp(tag) == type) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasCurrencySymbols(CharSequence affixPattern) {
        if (affixPattern == null || affixPattern.length() == 0) {
            return false;
        }
        long tag = 0;
        while (hasNext(tag, affixPattern)) {
            tag = nextToken(tag, affixPattern);
            int typeOrCp = getTypeOrCp(tag);
            if (typeOrCp < 0 && getFieldForType(typeOrCp) == NumberFormat.Field.CURRENCY) {
                return true;
            }
        }
        return false;
    }

    public static String replaceType(CharSequence affixPattern, int type, char replacementChar) {
        if (affixPattern == null || affixPattern.length() == 0) {
            return "";
        }
        char[] chars = affixPattern.toString().toCharArray();
        long tag = 0;
        while (hasNext(tag, affixPattern)) {
            tag = nextToken(tag, affixPattern);
            if (getTypeOrCp(tag) == type) {
                chars[getOffset(tag) - 1] = replacementChar;
            }
        }
        return new String(chars);
    }

    public static long nextToken(long tag, CharSequence patternString) {
        CharSequence charSequence = patternString;
        int offset = getOffset(tag);
        int state = getState(tag);
        while (offset < patternString.length()) {
            int cp = Character.codePointAt(charSequence, offset);
            int count = Character.charCount(cp);
            switch (state) {
                case 0:
                    if (cp != 37) {
                        if (cp == 39) {
                            state = 1;
                            offset += count;
                            break;
                        } else if (cp == 43) {
                            return makeTag(offset + count, -2, 0, 0);
                        } else {
                            if (cp != 45) {
                                if (cp == 164) {
                                    state = 4;
                                    offset += count;
                                    break;
                                } else {
                                    return cp != 8240 ? makeTag(offset + count, 0, 0, cp) : makeTag(offset + count, -4, 0, 0);
                                }
                            } else {
                                return makeTag(offset + count, -1, 0, 0);
                            }
                        }
                    } else {
                        return makeTag(offset + count, -3, 0, 0);
                    }
                case 1:
                    if (cp == 39) {
                        return makeTag(offset + count, 0, 0, cp);
                    }
                    return makeTag(offset + count, 0, 2, cp);
                case 2:
                    if (cp == 39) {
                        state = 3;
                        offset += count;
                        break;
                    } else {
                        return makeTag(offset + count, 0, 2, cp);
                    }
                case 3:
                    if (cp != 39) {
                        state = 0;
                        break;
                    } else {
                        return makeTag(offset + count, 0, 2, cp);
                    }
                case 4:
                    if (cp == 164) {
                        state = 5;
                        offset += count;
                        break;
                    } else {
                        return makeTag(offset, -5, 0, 0);
                    }
                case 5:
                    if (cp == 164) {
                        state = 6;
                        offset += count;
                        break;
                    } else {
                        return makeTag(offset, -6, 0, 0);
                    }
                case 6:
                    if (cp == 164) {
                        state = 7;
                        offset += count;
                        break;
                    } else {
                        return makeTag(offset, -7, 0, 0);
                    }
                case 7:
                    if (cp == 164) {
                        state = 8;
                        offset += count;
                        break;
                    } else {
                        return makeTag(offset, -8, 0, 0);
                    }
                case 8:
                    if (cp == 164) {
                        state = 9;
                        offset += count;
                        break;
                    } else {
                        return makeTag(offset, -9, 0, 0);
                    }
                case 9:
                    if (cp == 164) {
                        offset += count;
                        break;
                    } else {
                        return makeTag(offset, -15, 0, 0);
                    }
                default:
                    throw new AssertionError();
            }
        }
        switch (state) {
            case 0:
                return -1;
            case 1:
            case 2:
                throw new IllegalArgumentException("Unterminated quote in pattern affix: \"" + charSequence + "\"");
            case 3:
                return -1;
            case 4:
                return makeTag(offset, -5, 0, 0);
            case 5:
                return makeTag(offset, -6, 0, 0);
            case 6:
                return makeTag(offset, -7, 0, 0);
            case 7:
                return makeTag(offset, -8, 0, 0);
            case 8:
                return makeTag(offset, -9, 0, 0);
            case 9:
                return makeTag(offset, -15, 0, 0);
            default:
                throw new AssertionError();
        }
    }

    public static boolean hasNext(long tag, CharSequence string) {
        int state = getState(tag);
        int offset = getOffset(tag);
        boolean z = false;
        if (state == 2 && offset == string.length() - 1 && string.charAt(offset) == '\'') {
            return false;
        }
        if (state != 0) {
            return true;
        }
        if (offset < string.length()) {
            z = true;
        }
        return z;
    }

    public static int getTypeOrCp(long tag) {
        int type = getType(tag);
        return type == 0 ? getCodePoint(tag) : -type;
    }

    private static long makeTag(int offset, int type, int state, int cp) {
        return 0 | ((long) offset) | ((-((long) type)) << 32) | (((long) state) << 36) | (((long) cp) << 40);
    }

    static int getOffset(long tag) {
        return (int) (-1 & tag);
    }

    static int getType(long tag) {
        return (int) ((tag >>> 32) & 15);
    }

    static int getState(long tag) {
        return (int) ((tag >>> 36) & 15);
    }

    static int getCodePoint(long tag) {
        return (int) (tag >>> 40);
    }
}
