package java.text;

class PatternEntry {
    static final int RESET = -2;
    static final int UNSET = -1;
    String chars = "";
    String extension = "";
    int strength = -1;

    static class Parser {
        private int i;
        private StringBuffer newChars = new StringBuffer();
        private StringBuffer newExtension = new StringBuffer();
        private String pattern;

        public Parser(String pattern) {
            this.pattern = pattern;
            this.i = 0;
        }

        /* JADX WARNING: Removed duplicated region for block: B:51:0x010b  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0088  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public PatternEntry next() throws ParseException {
            int newStrength = -1;
            this.newChars.setLength(0);
            this.newExtension.setLength(0);
            boolean inChars = true;
            boolean inQuote = false;
            while (this.i < this.pattern.length()) {
                char ch = this.pattern.charAt(this.i);
                if (!inQuote) {
                    switch (ch) {
                        case 9:
                        case 10:
                        case 12:
                        case 13:
                        case ' ':
                            continue;
                        case ZipConstants.CENATX /*38*/:
                            if (newStrength != -1) {
                                break;
                            }
                            newStrength = -2;
                            continue;
                        case '\'':
                            inQuote = true;
                            String str = this.pattern;
                            int i = this.i + 1;
                            this.i = i;
                            ch = str.charAt(i);
                            if (this.newChars.length() != 0) {
                                if (!inChars) {
                                    this.newExtension.append(ch);
                                    break;
                                }
                                this.newChars.append(ch);
                                break;
                            }
                            this.newChars.append(ch);
                            continue;
                        case ',':
                            if (newStrength != -1) {
                                break;
                            }
                            newStrength = 2;
                            continue;
                        case '/':
                            inChars = false;
                            continue;
                        case ';':
                            if (newStrength != -1) {
                                break;
                            }
                            newStrength = 1;
                            continue;
                        case '<':
                            if (newStrength != -1) {
                                break;
                            }
                            newStrength = 0;
                            continue;
                        case '=':
                            if (newStrength != -1) {
                                break;
                            }
                            newStrength = 3;
                            continue;
                        default:
                            if (newStrength == -1) {
                                throw new ParseException("missing char (=,;<&) : " + this.pattern.substring(this.i, this.i + 10 < this.pattern.length() ? this.i + 10 : this.pattern.length()), this.i);
                            } else if (!PatternEntry.isSpecialChar(ch) || inQuote) {
                                if (!inChars) {
                                    this.newExtension.append(ch);
                                    break;
                                }
                                this.newChars.append(ch);
                                continue;
                            } else {
                                throw new ParseException("Unquoted punctuation character : " + Integer.toString(ch, 16), this.i);
                            }
                    }
                    if (newStrength != -1) {
                        return null;
                    }
                    if (this.newChars.length() != 0) {
                        return new PatternEntry(newStrength, this.newChars, this.newExtension);
                    }
                    throw new ParseException("missing chars (=,;<&): " + this.pattern.substring(this.i, this.i + 10 < this.pattern.length() ? this.i + 10 : this.pattern.length()), this.i);
                } else if (ch == '\'') {
                    inQuote = false;
                } else if (this.newChars.length() == 0) {
                    this.newChars.append(ch);
                } else if (inChars) {
                    this.newChars.append(ch);
                } else {
                    this.newExtension.append(ch);
                }
                this.i++;
            }
            if (newStrength != -1) {
            }
        }
    }

    public void appendQuotedExtension(StringBuffer toAddTo) {
        appendQuoted(this.extension, toAddTo);
    }

    public void appendQuotedChars(StringBuffer toAddTo) {
        appendQuoted(this.chars, toAddTo);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return this.chars.equals(((PatternEntry) obj).chars);
    }

    public int hashCode() {
        return this.chars.hashCode();
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        addToBuffer(result, true, false, null);
        return result.toString();
    }

    final int getStrength() {
        return this.strength;
    }

    final String getExtension() {
        return this.extension;
    }

    final String getChars() {
        return this.chars;
    }

    void addToBuffer(StringBuffer toAddTo, boolean showExtension, boolean showWhiteSpace, PatternEntry lastEntry) {
        if (showWhiteSpace && toAddTo.length() > 0) {
            if (this.strength == 0 || lastEntry != null) {
                toAddTo.append(10);
            } else {
                toAddTo.append(' ');
            }
        }
        if (lastEntry != null) {
            toAddTo.append('&');
            if (showWhiteSpace) {
                toAddTo.append(' ');
            }
            lastEntry.appendQuotedChars(toAddTo);
            appendQuotedExtension(toAddTo);
            if (showWhiteSpace) {
                toAddTo.append(' ');
            }
        }
        switch (this.strength) {
            case -2:
                toAddTo.append('&');
                break;
            case -1:
                toAddTo.append('?');
                break;
            case 0:
                toAddTo.append('<');
                break;
            case 1:
                toAddTo.append(';');
                break;
            case 2:
                toAddTo.append(',');
                break;
            case 3:
                toAddTo.append('=');
                break;
        }
        if (showWhiteSpace) {
            toAddTo.append(' ');
        }
        appendQuoted(this.chars, toAddTo);
        if (showExtension && this.extension.length() != 0) {
            toAddTo.append('/');
            appendQuoted(this.extension, toAddTo);
        }
    }

    static void appendQuoted(String chars, StringBuffer toAddTo) {
        boolean inQuote = false;
        char ch = chars.charAt(0);
        if (!Character.isSpaceChar(ch)) {
            if (!isSpecialChar(ch)) {
                switch (ch) {
                    case 9:
                    case 10:
                    case 12:
                    case 13:
                    case 16:
                    case '@':
                        inQuote = true;
                        toAddTo.append('\'');
                        break;
                    case '\'':
                        inQuote = true;
                        toAddTo.append('\'');
                        break;
                    default:
                        if (null != null) {
                            inQuote = false;
                            toAddTo.append('\'');
                            break;
                        }
                        break;
                }
            }
            inQuote = true;
            toAddTo.append('\'');
        } else {
            inQuote = true;
            toAddTo.append('\'');
        }
        toAddTo.append(chars);
        if (inQuote) {
            toAddTo.append('\'');
        }
    }

    PatternEntry(int strength, StringBuffer chars, StringBuffer extension) {
        String stringBuffer;
        this.strength = strength;
        this.chars = chars.toString();
        if (extension.length() > 0) {
            stringBuffer = extension.toString();
        } else {
            stringBuffer = "";
        }
        this.extension = stringBuffer;
    }

    static boolean isSpecialChar(char ch) {
        if (ch == ' ') {
            return true;
        }
        if (ch <= '/' && ch >= '\"') {
            return true;
        }
        if (ch <= '?' && ch >= ':') {
            return true;
        }
        if (ch <= '`' && ch >= '[') {
            return true;
        }
        if (ch > '~' || ch < '{') {
            return false;
        }
        return true;
    }
}
