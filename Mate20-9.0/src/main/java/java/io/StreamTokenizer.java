package java.io;

import java.util.Arrays;

public class StreamTokenizer {
    private static final byte CT_ALPHA = 4;
    private static final byte CT_COMMENT = 16;
    private static final byte CT_DIGIT = 2;
    private static final byte CT_QUOTE = 8;
    private static final byte CT_WHITESPACE = 1;
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
        if (is != null) {
            this.input = is;
            return;
        }
        throw new NullPointerException();
    }

    public StreamTokenizer(Reader r) {
        this();
        if (r != null) {
            this.reader = r;
            return;
        }
        throw new NullPointerException();
    }

    public void resetSyntax() {
        int i = this.ctype.length;
        while (true) {
            i--;
            if (i >= 0) {
                this.ctype[i] = 0;
            } else {
                return;
            }
        }
    }

    public void wordChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
        }
        while (low <= hi) {
            byte[] bArr = this.ctype;
            bArr[low] = (byte) (bArr[low] | 4);
            low++;
        }
    }

    public void whitespaceChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
        }
        while (low <= hi) {
            this.ctype[low] = 1;
            low++;
        }
    }

    public void ordinaryChars(int low, int hi) {
        if (low < 0) {
            low = 0;
        }
        if (hi >= this.ctype.length) {
            hi = this.ctype.length - 1;
        }
        while (low <= hi) {
            this.ctype[low] = 0;
            low++;
        }
    }

    public void ordinaryChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = 0;
        }
    }

    public void commentChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = 16;
        }
    }

    public void quoteChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = 8;
        }
    }

    public void parseNumbers() {
        for (int i = 48; i <= 57; i++) {
            byte[] bArr = this.ctype;
            bArr[i] = (byte) (bArr[i] | 2);
        }
        byte[] bArr2 = this.ctype;
        bArr2[46] = (byte) (bArr2[46] | 2);
        byte[] bArr3 = this.ctype;
        bArr3[45] = (byte) (bArr3[45] | 2);
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
        int c;
        int c2;
        int c3;
        int c4;
        int c5;
        int i;
        int seendot = 0;
        if (this.pushedBack) {
            this.pushedBack = false;
            return this.ttype;
        }
        byte[] ct = this.ctype;
        this.sval = null;
        int c6 = this.peekc;
        if (c6 < 0) {
            c6 = Integer.MAX_VALUE;
        }
        if (c6 == SKIP_LF) {
            c6 = read();
            if (c6 < 0) {
                this.ttype = -1;
                return -1;
            } else if (c6 == 10) {
                c6 = Integer.MAX_VALUE;
            }
        }
        int i2 = Integer.MAX_VALUE;
        if (c6 == Integer.MAX_VALUE) {
            c6 = read();
            if (c6 < 0) {
                this.ttype = -1;
                return -1;
            }
        }
        this.ttype = c;
        this.peekc = Integer.MAX_VALUE;
        int ctype2 = c < 256 ? ct[c] : 4;
        while ((ctype2 & 1) != 0) {
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
            ctype2 = c < 256 ? ct[c] : 4;
        }
        if ((ctype2 & 2) != 0) {
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
            while (true) {
                if (c == 46 && seendot == 0) {
                    seendot = 1;
                } else if (48 > c || c > 57) {
                    this.peekc = c;
                } else {
                    decexp += seendot;
                    v = (10.0d * v) + ((double) (c - 48));
                }
                c = read();
            }
            this.peekc = c;
            if (decexp != 0) {
                double denom = 10.0d;
                for (int decexp2 = decexp - 1; decexp2 > 0; decexp2--) {
                    denom *= 10.0d;
                }
                v /= denom;
            }
            this.nval = neg ? -v : v;
            this.ttype = -2;
            return -2;
        } else if (ctype2 != false && true) {
            int c7 = c;
            int c8 = 0;
            while (true) {
                if (c8 >= this.buf.length) {
                    this.buf = Arrays.copyOf(this.buf, this.buf.length * 2);
                }
                i = c8 + 1;
                this.buf[c8] = (char) c7;
                c7 = read();
                if (((c7 < 0 ? 1 : c7 < 256 ? ct[c7] : 4) & 6) == 0) {
                    break;
                }
                c8 = i;
            }
            this.peekc = c7;
            this.sval = String.copyValueOf(this.buf, 0, i);
            if (this.forceLower) {
                this.sval = this.sval.toLowerCase();
            }
            this.ttype = -3;
            return -3;
        } else if ((ctype2 & 8) != 0) {
            this.ttype = c;
            int i3 = 0;
            int d = read();
            while (d >= 0 && d != this.ttype && d != 10 && d != 13) {
                if (d == 92) {
                    c5 = read();
                    int first = c5;
                    if (c5 < 48 || c5 > 55) {
                        if (c5 == 102) {
                            c5 = 12;
                        } else if (c5 == 110) {
                            c5 = 10;
                        } else if (c5 == 114) {
                            c5 = 13;
                        } else if (c5 == 116) {
                            c5 = 9;
                        } else if (c5 != 118) {
                            switch (c5) {
                                case 97:
                                    c5 = 7;
                                    break;
                                case 98:
                                    c5 = 8;
                                    break;
                            }
                        } else {
                            c5 = 11;
                        }
                        d = read();
                    } else {
                        c5 -= 48;
                        int c22 = read();
                        if (48 > c22 || c22 > 55) {
                            d = c22;
                        } else {
                            c5 = (c5 << 3) + (c22 - 48);
                            int c23 = read();
                            if (48 > c23 || c23 > 55 || first > 51) {
                                d = c23;
                            } else {
                                c5 = (c5 << 3) + (c23 - 48);
                                d = read();
                            }
                        }
                    }
                } else {
                    c5 = d;
                    d = read();
                }
                if (i3 >= this.buf.length) {
                    this.buf = Arrays.copyOf(this.buf, this.buf.length * 2);
                }
                this.buf[i3] = (char) c5;
                i3++;
            }
            if (d != this.ttype) {
                i2 = d;
            }
            this.peekc = i2;
            this.sval = String.copyValueOf(this.buf, 0, i3);
            return this.ttype;
        } else if (c == 47 && (this.slashSlashCommentsP || this.slashStarCommentsP)) {
            int c9 = read();
            if (c9 == 42 && this.slashStarCommentsP) {
                while (true) {
                    int read = read();
                    int c10 = read;
                    if (read == 47 && seendot == 42) {
                        return nextToken();
                    }
                    if (c10 == 13) {
                        this.LINENO++;
                        c10 = read();
                        if (c10 == 10) {
                            c10 = read();
                        }
                    } else if (c10 == 10) {
                        this.LINENO++;
                        c10 = read();
                    }
                    if (c10 < 0) {
                        this.ttype = -1;
                        return -1;
                    }
                    seendot = c10;
                }
            } else if (c9 == 47 && this.slashSlashCommentsP) {
                do {
                    int read2 = read();
                    c4 = read2;
                    if (read2 == 10 || c4 == 13) {
                        this.peekc = c4;
                    }
                } while (c4 >= 0);
                this.peekc = c4;
                return nextToken();
            } else if ((ct[47] & 16) != 0) {
                do {
                    int read3 = read();
                    c3 = read3;
                    if (read3 == 10 || c3 == 13) {
                        this.peekc = c3;
                    }
                } while (c3 >= 0);
                this.peekc = c3;
                return nextToken();
            } else {
                this.peekc = c9;
                this.ttype = 47;
                return 47;
            }
        } else if ((ctype2 & 16) != 0) {
            do {
                int read4 = read();
                c2 = read4;
                if (read4 == 10 || c2 == 13) {
                    this.peekc = c2;
                }
            } while (c2 >= 0);
            this.peekc = c2;
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
        int i = this.ttype;
        if (i != 10) {
            switch (i) {
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
                default:
                    if (this.ttype < 256 && (this.ctype[this.ttype] & 8) != 0) {
                        ret = this.sval;
                        break;
                    } else {
                        char[] s = new char[3];
                        s[2] = '\'';
                        s[0] = '\'';
                        s[1] = (char) this.ttype;
                        ret = new String(s);
                        break;
                    }
            }
        } else {
            ret = "EOL";
        }
        return "Token[" + ret + "], line " + this.LINENO;
    }
}
