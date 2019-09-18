package android.icu.impl;

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

    public PatternTokenizer setIgnorableCharacters(UnicodeSet ignorableCharacters2) {
        this.ignorableCharacters = (UnicodeSet) ignorableCharacters2.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public UnicodeSet getSyntaxCharacters() {
        return (UnicodeSet) this.syntaxCharacters.clone();
    }

    public UnicodeSet getExtraQuotingCharacters() {
        return (UnicodeSet) this.extraQuotingCharacters.clone();
    }

    public PatternTokenizer setSyntaxCharacters(UnicodeSet syntaxCharacters2) {
        this.syntaxCharacters = (UnicodeSet) syntaxCharacters2.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public PatternTokenizer setExtraQuotingCharacters(UnicodeSet syntaxCharacters2) {
        this.extraQuotingCharacters = (UnicodeSet) syntaxCharacters2.clone();
        this.needingQuoteCharacters = null;
        return this;
    }

    public UnicodeSet getEscapeCharacters() {
        return (UnicodeSet) this.escapeCharacters.clone();
    }

    public PatternTokenizer setEscapeCharacters(UnicodeSet escapeCharacters2) {
        this.escapeCharacters = (UnicodeSet) escapeCharacters2.clone();
        return this;
    }

    public boolean isUsingQuote() {
        return this.usingQuote;
    }

    public PatternTokenizer setUsingQuote(boolean usingQuote2) {
        this.usingQuote = usingQuote2;
        this.needingQuoteCharacters = null;
        return this;
    }

    public boolean isUsingSlash() {
        return this.usingSlash;
    }

    public PatternTokenizer setUsingSlash(boolean usingSlash2) {
        this.usingSlash = usingSlash2;
        this.needingQuoteCharacters = null;
        return this;
    }

    public int getLimit() {
        return this.limit;
    }

    public PatternTokenizer setLimit(int limit2) {
        this.limit = limit2;
        return this;
    }

    public int getStart() {
        return this.start;
    }

    public PatternTokenizer setStart(int start2) {
        this.start = start2;
        return this;
    }

    public PatternTokenizer setPattern(CharSequence pattern2) {
        return setPattern(pattern2.toString());
    }

    public PatternTokenizer setPattern(String pattern2) {
        if (pattern2 != null) {
            this.start = 0;
            this.limit = pattern2.length();
            this.pattern = pattern2;
            return this;
        }
        throw new IllegalArgumentException("Inconsistent arguments");
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
        if (cp <= 65535) {
            result.append("\\u");
            result.append(Utility.hex((long) cp, 4));
            return;
        }
        result.append("\\U");
        result.append(Utility.hex((long) cp, 8));
    }

    public String normalize() {
        int oldStart = this.start;
        StringBuffer result = new StringBuffer();
        StringBuffer buffer = new StringBuffer();
        while (true) {
            buffer.setLength(0);
            int status = next(buffer);
            if (status == 0) {
                this.start = oldStart;
                return result.toString();
            } else if (status != 1) {
                result.append(quoteLiteral((CharSequence) buffer));
            } else {
                result.append(buffer);
            }
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
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
            if (quoteStatus != -1) {
                switch (quoteStatus) {
                    case 1:
                        if (cp != lastQuote) {
                            UTF16.append(buffer, cp);
                            quoteStatus = 2;
                            break;
                        } else {
                            UTF16.append(buffer, cp);
                            quoteStatus = 0;
                            continue;
                        }
                    case 2:
                        if (cp != lastQuote) {
                            UTF16.append(buffer, cp);
                            break;
                        } else {
                            quoteStatus = -1;
                            continue;
                        }
                    case 3:
                        if (cp != 85) {
                            if (cp == 117) {
                                quoteStatus = 4;
                                hexCount = 4;
                                hexValue = 0;
                                break;
                            } else if (this.usingSlash) {
                                UTF16.append(buffer, cp);
                                quoteStatus = 0;
                                continue;
                            } else {
                                buffer.append(BACK_SLASH);
                                quoteStatus = 0;
                            }
                        } else {
                            quoteStatus = 4;
                            hexCount = 8;
                            hexValue = 0;
                            break;
                        }
                    case 4:
                        int hexValue2 = (hexValue << 4) + cp;
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
                                hexValue = hexValue2 - 48;
                                break;
                            default:
                                switch (cp) {
                                    case 65:
                                    case 66:
                                    case 67:
                                    case 68:
                                    case 69:
                                    case 70:
                                        hexValue = hexValue2 - 55;
                                        break;
                                    default:
                                        switch (cp) {
                                            case 97:
                                            case 98:
                                            case 99:
                                            case 100:
                                            case 101:
                                            case 102:
                                                hexValue = hexValue2 - 87;
                                                break;
                                            default:
                                                this.start = i;
                                                return 4;
                                        }
                                }
                        }
                        hexCount--;
                        if (hexCount == 0) {
                            quoteStatus = 0;
                            UTF16.append(buffer, hexValue);
                            break;
                        } else {
                            continue;
                        }
                }
            } else if (cp == lastQuote) {
                UTF16.append(buffer, cp);
                quoteStatus = 2;
                i += UTF16.getCharCount(cp);
            } else {
                quoteStatus = 0;
            }
            if (this.ignorableCharacters.contains(cp)) {
                continue;
            } else if (!this.syntaxCharacters.contains(cp)) {
                status = 2;
                if (cp == 92) {
                    quoteStatus = 3;
                } else if (!this.usingQuote || cp != 39) {
                    UTF16.append(buffer, cp);
                } else {
                    lastQuote = cp;
                    quoteStatus = 1;
                }
            } else if (status == 5) {
                UTF16.append(buffer, cp);
                this.start = UTF16.getCharCount(cp) + i;
                return 1;
            } else {
                this.start = i;
                return status;
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
                } else {
                    status = 4;
                    break;
                }
            case 4:
                status = 4;
                break;
        }
        return status;
    }
}
