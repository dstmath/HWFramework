package android.icu.impl;

import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;

public class PatternTokenizer {
    private static final int AFTER_QUOTE = -1;
    public static final char BACK_SLASH = '\\';
    public static final int BROKEN_ESCAPE = 4;
    public static final int BROKEN_QUOTE = 3;
    public static final int DONE = 0;
    private static final int HEX = 4;
    private static int IN_QUOTE = -2;
    public static final int LITERAL = 2;
    private static final int NONE = 0;
    private static final int NORMAL_QUOTE = 2;
    private static int NO_QUOTE = -1;
    public static final char SINGLE_QUOTE = '\'';
    private static final int SLASH_START = 3;
    private static final int START_QUOTE = 1;
    public static final int SYNTAX = 1;
    public static final int UNKNOWN = 5;
    private UnicodeSet escapeCharacters = new UnicodeSet();
    private UnicodeSet extraQuotingCharacters = new UnicodeSet();
    private UnicodeSet ignorableCharacters = new UnicodeSet();
    private int limit;
    private transient UnicodeSet needingQuoteCharacters = null;
    private String pattern;
    private int start;
    private UnicodeSet syntaxCharacters = new UnicodeSet();
    private boolean usingQuote = false;
    private boolean usingSlash = false;

    public UnicodeSet getIgnorableCharacters() {
        return (UnicodeSet) this.ignorableCharacters.clone();
    }

    public PatternTokenizer setIgnorableCharacters(UnicodeSet ignorableCharacters) {
        this.ignorableCharacters = (UnicodeSet) ignorableCharacters.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public UnicodeSet getSyntaxCharacters() {
        return (UnicodeSet) this.syntaxCharacters.clone();
    }

    public UnicodeSet getExtraQuotingCharacters() {
        return (UnicodeSet) this.extraQuotingCharacters.clone();
    }

    public PatternTokenizer setSyntaxCharacters(UnicodeSet syntaxCharacters) {
        this.syntaxCharacters = (UnicodeSet) syntaxCharacters.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public PatternTokenizer setExtraQuotingCharacters(UnicodeSet syntaxCharacters) {
        this.extraQuotingCharacters = (UnicodeSet) syntaxCharacters.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public UnicodeSet getEscapeCharacters() {
        return (UnicodeSet) this.escapeCharacters.clone();
    }

    public PatternTokenizer setEscapeCharacters(UnicodeSet escapeCharacters) {
        this.escapeCharacters = (UnicodeSet) escapeCharacters.clone();
        return this;
    }

    public boolean isUsingQuote() {
        return this.usingQuote;
    }

    public PatternTokenizer setUsingQuote(boolean usingQuote) {
        this.usingQuote = usingQuote;
        this.needingQuoteCharacters = null;
        return this;
    }

    public boolean isUsingSlash() {
        return this.usingSlash;
    }

    public PatternTokenizer setUsingSlash(boolean usingSlash) {
        this.usingSlash = usingSlash;
        this.needingQuoteCharacters = null;
        return this;
    }

    public int getLimit() {
        return this.limit;
    }

    public PatternTokenizer setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public int getStart() {
        return this.start;
    }

    public PatternTokenizer setStart(int start) {
        this.start = start;
        return this;
    }

    public PatternTokenizer setPattern(CharSequence pattern) {
        return setPattern(pattern.toString());
    }

    public PatternTokenizer setPattern(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Inconsistent arguments");
        }
        this.start = 0;
        this.limit = pattern.length();
        this.pattern = pattern;
        return this;
    }

    public String quoteLiteral(CharSequence string) {
        return quoteLiteral(string.toString());
    }

    public String quoteLiteral(String string) {
        if (this.needingQuoteCharacters == null) {
            this.needingQuoteCharacters = new UnicodeSet().addAll(this.syntaxCharacters).addAll(this.ignorableCharacters).addAll(this.extraQuotingCharacters);
            if (this.usingSlash) {
                this.needingQuoteCharacters.add(92);
            }
            if (this.usingQuote) {
                this.needingQuoteCharacters.add(39);
            }
        }
        StringBuffer result = new StringBuffer();
        int quotedChar = NO_QUOTE;
        int i = 0;
        while (i < string.length()) {
            int cp = UTF16.charAt(string, i);
            if (this.escapeCharacters.contains(cp)) {
                if (quotedChar == IN_QUOTE) {
                    result.append(SINGLE_QUOTE);
                    quotedChar = NO_QUOTE;
                }
                appendEscaped(result, cp);
            } else if (!this.needingQuoteCharacters.contains(cp)) {
                if (quotedChar == IN_QUOTE) {
                    result.append(SINGLE_QUOTE);
                    quotedChar = NO_QUOTE;
                }
                UTF16.append(result, cp);
            } else if (quotedChar == IN_QUOTE) {
                UTF16.append(result, cp);
                if (this.usingQuote && cp == 39) {
                    result.append(SINGLE_QUOTE);
                }
            } else if (this.usingSlash) {
                result.append(BACK_SLASH);
                UTF16.append(result, cp);
            } else if (!this.usingQuote) {
                appendEscaped(result, cp);
            } else if (cp == 39) {
                result.append(SINGLE_QUOTE);
                result.append(SINGLE_QUOTE);
            } else {
                result.append(SINGLE_QUOTE);
                UTF16.append(result, cp);
                quotedChar = IN_QUOTE;
            }
            i += UTF16.getCharCount(cp);
        }
        if (quotedChar == IN_QUOTE) {
            result.append(SINGLE_QUOTE);
        }
        return result.toString();
    }

    private void appendEscaped(StringBuffer result, int cp) {
        if (cp <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            result.append("\\u").append(Utility.hex((long) cp, 4));
        } else {
            result.append("\\U").append(Utility.hex((long) cp, 8));
        }
    }

    public String normalize() {
        int oldStart = this.start;
        StringBuffer result = new StringBuffer();
        CharSequence buffer = new StringBuffer();
        while (true) {
            buffer.setLength(0);
            int status = next(buffer);
            if (status == 0) {
                this.start = oldStart;
                return result.toString();
            } else if (status != 1) {
                result.append(quoteLiteral(buffer));
            } else {
                result.append(buffer);
            }
        }
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int next(StringBuffer buffer) {
        if (this.start >= this.limit) {
            return 0;
        }
        int status = 5;
        int lastQuote = 5;
        int quoteStatus = 0;
        int hexCount = 0;
        int hexValue = 0;
        int i = this.start;
        while (i < this.limit) {
            int cp = UTF16.charAt(this.pattern, i);
            switch (quoteStatus) {
                case -1:
                    if (cp == lastQuote) {
                        UTF16.append(buffer, cp);
                        quoteStatus = 2;
                        break;
                    }
                    quoteStatus = 0;
                case 1:
                    if (cp != lastQuote) {
                        UTF16.append(buffer, cp);
                        quoteStatus = 2;
                        break;
                    }
                    UTF16.append(buffer, cp);
                    quoteStatus = 0;
                    break;
                case 2:
                    if (cp != lastQuote) {
                        UTF16.append(buffer, cp);
                        break;
                    }
                    quoteStatus = -1;
                    break;
                case 3:
                    switch (cp) {
                        case 85:
                            quoteStatus = 4;
                            hexCount = 8;
                            hexValue = 0;
                            continue;
                        case 117:
                            quoteStatus = 4;
                            hexCount = 4;
                            hexValue = 0;
                            continue;
                        default:
                            if (this.usingSlash) {
                                UTF16.append(buffer, cp);
                                quoteStatus = 0;
                                continue;
                            } else {
                                buffer.append(BACK_SLASH);
                                quoteStatus = 0;
                            }
                    }
                case 4:
                    hexValue = (hexValue << 4) + cp;
                    switch (cp) {
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
                            hexValue -= 48;
                            break;
                        case 65:
                        case 66:
                        case 67:
                        case 68:
                        case 69:
                        case 70:
                            hexValue -= 55;
                            break;
                        case 97:
                        case 98:
                        case 99:
                        case 100:
                        case 101:
                        case 102:
                            hexValue -= 87;
                            break;
                        default:
                            this.start = i;
                            return 4;
                    }
                    hexCount--;
                    if (hexCount != 0) {
                        break;
                    }
                    quoteStatus = 0;
                    UTF16.append(buffer, hexValue);
                    break;
                default:
                    if (!this.ignorableCharacters.contains(cp)) {
                        if (!this.syntaxCharacters.contains(cp)) {
                            status = 2;
                            if (cp != 92) {
                                if (!this.usingQuote || cp != 39) {
                                    UTF16.append(buffer, cp);
                                    break;
                                }
                                lastQuote = cp;
                                quoteStatus = 1;
                                break;
                            }
                            quoteStatus = 3;
                            break;
                        } else if (status == 5) {
                            UTF16.append(buffer, cp);
                            this.start = UTF16.getCharCount(cp) + i;
                            return 1;
                        } else {
                            this.start = i;
                            return status;
                        }
                    }
                    continue;
            }
            i += UTF16.getCharCount(cp);
        }
        this.start = this.limit;
        switch (quoteStatus) {
            case 1:
            case 2:
                status = 3;
                break;
            case 3:
                if (!this.usingSlash) {
                    buffer.append(BACK_SLASH);
                    break;
                }
                status = 4;
                break;
            case 4:
                status = 4;
                break;
        }
        return status;
    }
}
