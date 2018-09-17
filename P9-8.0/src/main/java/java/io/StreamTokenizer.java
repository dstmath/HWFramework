package java.io;

import java.util.Arrays;

public class StreamTokenizer {
    private static final byte CT_ALPHA = (byte) 4;
    private static final byte CT_COMMENT = (byte) 16;
    private static final byte CT_DIGIT = (byte) 2;
    private static final byte CT_QUOTE = (byte) 8;
    private static final byte CT_WHITESPACE = (byte) 1;
    private static final int NEED_CHAR = Integer.MAX_VALUE;
    private static final int SKIP_LF = 2147483646;
    public static final int TT_EOF = -1;
    public static final int TT_EOL = 10;
    private static final int TT_NOTHING = -4;
    public static final int TT_NUMBER = -2;
    public static final int TT_WORD = -3;
    private int LINENO;
    private char[] buf;
    private byte[] ctype;
    private boolean eolIsSignificantP;
    private boolean forceLower;
    private InputStream input;
    public double nval;
    private int peekc;
    private boolean pushedBack;
    private Reader reader;
    private boolean slashSlashCommentsP;
    private boolean slashStarCommentsP;
    public String sval;
    public int ttype;

    private StreamTokenizer() {
        this.reader = null;
        this.input = null;
        this.buf = new char[20];
        this.peekc = Integer.MAX_VALUE;
        this.LINENO = 1;
        this.eolIsSignificantP = false;
        this.slashSlashCommentsP = false;
        this.slashStarCommentsP = false;
        this.ctype = new byte[256];
        this.ttype = -4;
        wordChars(97, 122);
        wordChars(65, 90);
        wordChars(160, 255);
        whitespaceChars(0, 32);
        commentChar(47);
        quoteChar(34);
        quoteChar(39);
        parseNumbers();
    }

    @Deprecated
    public StreamTokenizer(InputStream is) {
        this();
        if (is == null) {
            throw new NullPointerException();
        }
        this.input = is;
    }

    public StreamTokenizer(Reader r) {
        this();
        if (r == null) {
            throw new NullPointerException();
        }
        this.reader = r;
    }

    public void resetSyntax() {
        int i = this.ctype.length;
        while (true) {
            i--;
            if (i >= 0) {
                this.ctype[i] = (byte) 0;
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0010  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void wordChars(int low, int hi) {
        int i;
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
            i = low;
            if (i > hi) {
                byte[] bArr = this.ctype;
                low = i + 1;
                bArr[i] = (byte) (bArr[i] | 4);
            }
        }
        i = low;
        if (i > hi) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0010  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void whitespaceChars(int low, int hi) {
        int i;
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
            i = low;
            if (i > hi) {
                low = i + 1;
                this.ctype[i] = (byte) 1;
            }
        }
        i = low;
        if (i > hi) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0011  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void ordinaryChars(int low, int hi) {
        int low2;
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
            low2 = low;
            if (low2 > hi) {
                low = low2 + 1;
                this.ctype[low2] = (byte) 0;
            }
        }
        low2 = low;
        if (low2 > hi) {
        }
    }

    public void ordinaryChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = (byte) 0;
        }
    }

    public void commentChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = (byte) 16;
        }
    }

    public void quoteChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = (byte) 8;
        }
    }

    public void parseNumbers() {
        byte[] bArr;
        for (int i = 48; i <= 57; i++) {
            bArr = this.ctype;
            bArr[i] = (byte) (bArr[i] | 2);
        }
        bArr = this.ctype;
        bArr[46] = (byte) (bArr[46] | 2);
        bArr = this.ctype;
        bArr[45] = (byte) (bArr[45] | 2);
    }

    public void eolIsSignificant(boolean flag) {
        this.eolIsSignificantP = flag;
    }

    public void slashStarComments(boolean flag) {
        this.slashStarCommentsP = flag;
    }

    public void slashSlashComments(boolean flag) {
        this.slashSlashCommentsP = flag;
    }

    public void lowerCaseMode(boolean fl) {
        this.forceLower = fl;
    }

    private int read() throws IOException {
        if (this.reader != null) {
            return this.reader.read();
        }
        if (this.input != null) {
            return this.input.read();
        }
        throw new IllegalStateException();
    }

    public int nextToken() throws IOException {
        if (this.pushedBack) {
            this.pushedBack = false;
            return this.ttype;
        }
        byte[] ct = this.ctype;
        this.sval = null;
        int c = this.peekc;
        if (c < 0) {
            c = Integer.MAX_VALUE;
        }
        if (c == SKIP_LF) {
            c = read();
            if (c < 0) {
                this.ttype = -1;
                return -1;
            } else if (c == 10) {
                c = Integer.MAX_VALUE;
            }
        }
        if (c == Integer.MAX_VALUE) {
            c = read();
            if (c < 0) {
                this.ttype = -1;
                return -1;
            }
        }
        this.ttype = c;
        this.peekc = Integer.MAX_VALUE;
        int ctype = c < 256 ? ct[c] : 4;
        while ((ctype & 1) != 0) {
            if (c == 13) {
                this.LINENO++;
                if (this.eolIsSignificantP) {
                    this.peekc = SKIP_LF;
                    this.ttype = 10;
                    return 10;
                }
                c = read();
                if (c == 10) {
                    c = read();
                }
            } else {
                if (c == 10) {
                    this.LINENO++;
                    if (this.eolIsSignificantP) {
                        this.ttype = 10;
                        return 10;
                    }
                }
                c = read();
            }
            if (c < 0) {
                this.ttype = -1;
                return -1;
            }
            ctype = c < 256 ? ct[c] : 4;
        }
        int i;
        int i2;
        if ((ctype & 2) != 0) {
            boolean neg = false;
            if (c == 45) {
                c = read();
                if (c == 46 || (c >= 48 && c <= 57)) {
                    neg = true;
                } else {
                    this.peekc = c;
                    this.ttype = 45;
                    return 45;
                }
            }
            double v = 0.0d;
            int decexp = 0;
            int seendot = 0;
            while (true) {
                if (c == 46 && seendot == 0) {
                    seendot = 1;
                } else if (48 > c || c > 57) {
                    this.peekc = c;
                } else {
                    v = (10.0d * v) + ((double) (c - 48));
                    decexp += seendot;
                }
                c = read();
            }
            this.peekc = c;
            if (decexp != 0) {
                double denom = 10.0d;
                for (decexp--; decexp > 0; decexp--) {
                    denom *= 10.0d;
                }
                v /= denom;
            }
            if (neg) {
                v = -v;
            }
            this.nval = v;
            this.ttype = -2;
            return -2;
        } else if ((ctype & 4) != 0) {
            i = 0;
            while (true) {
                if (i >= this.buf.length) {
                    this.buf = Arrays.copyOf(this.buf, this.buf.length * 2);
                }
                i2 = i + 1;
                this.buf[i] = (char) c;
                c = read();
                ctype = c < 0 ? 1 : c < 256 ? ct[c] : 4;
                if ((ctype & 6) == 0) {
                    break;
                }
                i = i2;
            }
            this.peekc = c;
            this.sval = String.copyValueOf(this.buf, 0, i2);
            if (this.forceLower) {
                this.sval = this.sval.toLowerCase();
            }
            this.ttype = -3;
            return -3;
        } else if ((ctype & 8) != 0) {
            this.ttype = c;
            int d = read();
            i2 = 0;
            while (d >= 0 && d != this.ttype && d != 10 && d != 13) {
                if (d == 92) {
                    c = read();
                    int first = c;
                    if (c < 48 || c > 55) {
                        switch (c) {
                            case 97:
                                c = 7;
                                break;
                            case 98:
                                c = 8;
                                break;
                            case 102:
                                c = 12;
                                break;
                            case 110:
                                c = 10;
                                break;
                            case 114:
                                c = 13;
                                break;
                            case 116:
                                c = 9;
                                break;
                            case 118:
                                c = 11;
                                break;
                        }
                        d = read();
                    } else {
                        c -= 48;
                        int c2 = read();
                        if (48 > c2 || c2 > 55) {
                            d = c2;
                        } else {
                            c = (c << 3) + (c2 - 48);
                            c2 = read();
                            if (48 > c2 || c2 > 55 || first > 51) {
                                d = c2;
                            } else {
                                c = (c << 3) + (c2 - 48);
                                d = read();
                            }
                        }
                    }
                } else {
                    c = d;
                    d = read();
                }
                if (i2 >= this.buf.length) {
                    this.buf = Arrays.copyOf(this.buf, this.buf.length * 2);
                }
                i = i2 + 1;
                this.buf[i2] = (char) c;
                i2 = i;
            }
            if (d == this.ttype) {
                d = Integer.MAX_VALUE;
            }
            this.peekc = d;
            this.sval = String.copyValueOf(this.buf, 0, i2);
            return this.ttype;
        } else if (c == 47 && (this.slashSlashCommentsP || this.slashStarCommentsP)) {
            c = read();
            if (c == 42 && this.slashStarCommentsP) {
                int prevc = 0;
                while (true) {
                    c = read();
                    if (c == 47 && prevc == 42) {
                        return nextToken();
                    }
                    if (c == 13) {
                        this.LINENO++;
                        c = read();
                        if (c == 10) {
                            c = read();
                        }
                    } else if (c == 10) {
                        this.LINENO++;
                        c = read();
                    }
                    if (c < 0) {
                        this.ttype = -1;
                        return -1;
                    }
                    prevc = c;
                }
            } else if (c == 47 && this.slashSlashCommentsP) {
                do {
                    c = read();
                    if (c == 10 || c == 13) {
                        this.peekc = c;
                    }
                } while (c >= 0);
                this.peekc = c;
                return nextToken();
            } else if ((ct[47] & 16) != 0) {
                do {
                    c = read();
                    if (c == 10 || c == 13) {
                        this.peekc = c;
                    }
                } while (c >= 0);
                this.peekc = c;
                return nextToken();
            } else {
                this.peekc = c;
                this.ttype = 47;
                return 47;
            }
        } else if ((ctype & 16) != 0) {
            do {
                c = read();
                if (c == 10 || c == 13) {
                    this.peekc = c;
                }
            } while (c >= 0);
            this.peekc = c;
            return nextToken();
        } else {
            this.ttype = c;
            return c;
        }
    }

    public void pushBack() {
        if (this.ttype != -4) {
            this.pushedBack = true;
        }
    }

    public int lineno() {
        return this.LINENO;
    }

    public String toString() {
        String ret;
        switch (this.ttype) {
            case -4:
                ret = "NOTHING";
                break;
            case -3:
                ret = this.sval;
                break;
            case -2:
                ret = "n=" + this.nval;
                break;
            case -1:
                ret = "EOF";
                break;
            case 10:
                ret = "EOL";
                break;
            default:
                if (this.ttype < 256 && (this.ctype[this.ttype] & 8) != 0) {
                    ret = this.sval;
                    break;
                }
                ret = new String(new char[]{'\'', '\'', (char) this.ttype});
                break;
        }
        return "Token[" + ret + "], line " + this.LINENO;
    }
}
