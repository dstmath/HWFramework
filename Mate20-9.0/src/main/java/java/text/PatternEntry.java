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

        public Parser(String pattern2) {
            this.pattern = pattern2;
            this.i = 0;
        }

        /* JADX WARNING: Removed duplicated region for block: B:49:0x0113  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0115  */
        public PatternEntry next() throws ParseException {
            int i2;
            int newStrength = -1;
            boolean inQuote = false;
            this.newChars.setLength(0);
            this.newExtension.setLength(0);
            boolean inChars = true;
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
                            } else {
                                newStrength = -2;
                                continue;
                            }
                        case '\'':
                            inQuote = true;
                            String str = this.pattern;
                            int i3 = this.i + 1;
                            this.i = i3;
                            char ch2 = str.charAt(i3);
                            if (this.newChars.length() != 0) {
                                if (!inChars) {
                                    this.newExtension.append(ch2);
                                    break;
                                } else {
                                    this.newChars.append(ch2);
                                    break;
                                }
                            } else {
                                this.newChars.append(ch2);
                                continue;
                            }
                        case ',':
                            if (newStrength != -1) {
                                break;
                            } else {
                                newStrength = 2;
                                continue;
                            }
                        case '/':
                            inChars = false;
                            continue;
                        case ';':
                            if (newStrength != -1) {
                                break;
                            } else {
                                newStrength = 1;
                                continue;
                            }
                        case '<':
                            if (newStrength != -1) {
                                break;
                            } else {
                                newStrength = 0;
                                continue;
                            }
                        case '=':
                            if (newStrength != -1) {
                                break;
                            } else {
                                newStrength = 3;
                                continue;
                            }
                        default:
                            if (newStrength == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("missing char (=,;<&) : ");
                                String str2 = this.pattern;
                                int i4 = this.i;
                                if (this.i + 10 < this.pattern.length()) {
                                    i2 = this.i + 10;
                                } else {
                                    i2 = this.pattern.length();
                                }
                                sb.append(str2.substring(i4, i2));
                                throw new ParseException(sb.toString(), this.i);
                            } else if (PatternEntry.isSpecialChar(ch) && !inQuote) {
                                throw new ParseException("Unquoted punctuation character : " + Integer.toString(ch, 16), this.i);
                            } else if (inChars) {
                                this.newChars.append(ch);
                                break;
                            } else {
                                this.newExtension.append(ch);
                                continue;
                            }
                    }
                    if (newStrength != -1) {
                        return null;
                    }
                    if (this.newChars.length() != 0) {
                        return new PatternEntry(newStrength, this.newChars, this.newExtension);
                    }
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("missing chars (=,;<&): ");
                    sb2.append(this.pattern.substring(this.i, this.i + 10 < this.pattern.length() ? this.i + 10 : this.pattern.length()));
                    throw new ParseException(sb2.toString(), this.i);
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

    /* access modifiers changed from: package-private */
    public final int getStrength() {
        return this.strength;
    }

    /* access modifiers changed from: package-private */
    public final String getExtension() {
        return this.extension;
    }

    /* access modifiers changed from: package-private */
    public final String getChars() {
        return this.chars;
    }

    /* access modifiers changed from: package-private */
    public void addToBuffer(StringBuffer toAddTo, boolean showExtension, boolean showWhiteSpace, PatternEntry lastEntry) {
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

    static void appendQuoted(String chars2, StringBuffer toAddTo) {
        boolean inQuote = false;
        char ch = chars2.charAt(0);
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
                        if (0 != 0) {
                            inQuote = false;
                            toAddTo.append('\'');
                            break;
                        }
                        break;
                }
            } else {
                inQuote = true;
                toAddTo.append('\'');
            }
        } else {
            inQuote = true;
            toAddTo.append('\'');
        }
        toAddTo.append(chars2);
        if (inQuote) {
            toAddTo.append('\'');
        }
    }

    PatternEntry(int strength2, StringBuffer chars2, StringBuffer extension2) {
        String str;
        this.strength = strength2;
        this.chars = chars2.toString();
        if (extension2.length() > 0) {
            str = extension2.toString();
        } else {
            str = "";
        }
        this.extension = str;
    }

    static boolean isSpecialChar(char ch) {
        return ch == ' ' || (ch <= '/' && ch >= '\"') || ((ch <= '?' && ch >= ':') || ((ch <= '`' && ch >= '[') || (ch <= '~' && ch >= '{')));
    }
}
