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
        this.peekc = NEED_CHAR;
        this.LINENO = 1;
        this.eolIsSignificantP = false;
        this.slashSlashCommentsP = false;
        this.slashStarCommentsP = false;
        this.ctype = new byte[Record.maxPadding];
        this.ttype = TT_NOTHING;
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
            i += TT_EOF;
            if (i >= 0) {
                this.ctype[i] = (byte) 0;
            } else {
                return;
            }
        }
    }

    public void wordChars(int r4, int r5) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r3 = this;
        if (r4 >= 0) goto L_0x0003;
    L_0x0002:
        r4 = 0;
    L_0x0003:
        r1 = r3.ctype;
        r1 = r1.length;
        if (r5 < r1) goto L_0x001b;
    L_0x0008:
        r1 = r3.ctype;
        r1 = r1.length;
        r5 = r1 + -1;
        r0 = r4;
    L_0x000e:
        if (r0 > r5) goto L_0x001d;
    L_0x0010:
        r1 = r3.ctype;
        r4 = r0 + 1;
        r2 = r1[r0];
        r2 = r2 | 4;
        r2 = (byte) r2;
        r1[r0] = r2;
    L_0x001b:
        r0 = r4;
        goto L_0x000e;
    L_0x001d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.StreamTokenizer.wordChars(int, int):void");
    }

    public void whitespaceChars(int r4, int r5) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r3 = this;
        if (r4 >= 0) goto L_0x0003;
    L_0x0002:
        r4 = 0;
    L_0x0003:
        r1 = r3.ctype;
        r1 = r1.length;
        if (r5 < r1) goto L_0x0017;
    L_0x0008:
        r1 = r3.ctype;
        r1 = r1.length;
        r5 = r1 + -1;
        r0 = r4;
    L_0x000e:
        if (r0 > r5) goto L_0x0019;
    L_0x0010:
        r1 = r3.ctype;
        r4 = r0 + 1;
        r2 = 1;
        r1[r0] = r2;
    L_0x0017:
        r0 = r4;
        goto L_0x000e;
    L_0x0019:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.StreamTokenizer.whitespaceChars(int, int):void");
    }

    public void ordinaryChars(int r4, int r5) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r3 = this;
        r2 = 0;
        if (r4 >= 0) goto L_0x0004;
    L_0x0003:
        r4 = 0;
    L_0x0004:
        r1 = r3.ctype;
        r1 = r1.length;
        if (r5 < r1) goto L_0x0017;
    L_0x0009:
        r1 = r3.ctype;
        r1 = r1.length;
        r5 = r1 + -1;
        r0 = r4;
    L_0x000f:
        if (r0 > r5) goto L_0x0019;
    L_0x0011:
        r1 = r3.ctype;
        r4 = r0 + 1;
        r1[r0] = r2;
    L_0x0017:
        r0 = r4;
        goto L_0x000f;
    L_0x0019:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.StreamTokenizer.ordinaryChars(int, int):void");
    }

    public void ordinaryChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = (byte) 0;
        }
    }

    public void commentChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = CT_COMMENT;
        }
    }

    public void quoteChar(int ch) {
        if (ch >= 0 && ch < this.ctype.length) {
            this.ctype[ch] = CT_QUOTE;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int nextToken() throws IOException {
        if (this.pushedBack) {
            this.pushedBack = false;
            return this.ttype;
        }
        byte[] ct = this.ctype;
        this.sval = null;
        int c = this.peekc;
        if (c < 0) {
            c = NEED_CHAR;
        }
        if (c == SKIP_LF) {
            c = read();
            if (c < 0) {
                this.ttype = TT_EOF;
                return TT_EOF;
            } else if (c == TT_EOL) {
                c = NEED_CHAR;
            }
        }
        if (c == NEED_CHAR) {
            c = read();
            if (c < 0) {
                this.ttype = TT_EOF;
                return TT_EOF;
            }
        }
        this.ttype = c;
        this.peekc = NEED_CHAR;
        int ctype = c < 256 ? ct[c] : 4;
        while ((ctype & 1) != 0) {
            if (c == 13) {
                this.LINENO++;
                if (this.eolIsSignificantP) {
                    this.peekc = SKIP_LF;
                    this.ttype = TT_EOL;
                    return TT_EOL;
                }
                c = read();
                if (c == TT_EOL) {
                    c = read();
                }
            } else {
                if (c == TT_EOL) {
                    this.LINENO++;
                    if (this.eolIsSignificantP) {
                        this.ttype = TT_EOL;
                        return TT_EOL;
                    }
                }
                c = read();
            }
            if (c < 0) {
                this.ttype = TT_EOF;
                return TT_EOF;
            }
            ctype = c < 256 ? ct[c] : 4;
        }
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
                for (decexp += TT_EOF; decexp > 0; decexp += TT_EOF) {
                    denom *= 10.0d;
                }
                v /= denom;
            }
            if (neg) {
                v = -v;
            }
            this.nval = v;
            this.ttype = TT_NUMBER;
            return TT_NUMBER;
        } else if ((ctype & 4) != 0) {
            i = 0;
            while (true) {
                r20 = this.buf.length;
                if (i >= r0) {
                    this.buf = Arrays.copyOf(this.buf, this.buf.length * 2);
                }
                i = i + 1;
                this.buf[i] = (char) c;
                c = read();
                ctype = c < 0 ? 1 : c < 256 ? ct[c] : 4;
                if ((ctype & 6) == 0) {
                    break;
                }
                i = i;
            }
            this.peekc = c;
            this.sval = String.copyValueOf(this.buf, 0, i);
            if (this.forceLower) {
                this.sval = this.sval.toLowerCase();
            }
            this.ttype = TT_WORD;
            return TT_WORD;
        } else if ((ctype & 8) != 0) {
            this.ttype = c;
            int d = read();
            i = 0;
            while (d >= 0) {
                r20 = this.ttype;
                if (!(d == r0 || d == TT_EOL || d == 13)) {
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
                                    c = TT_EOL;
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
                    r20 = this.buf.length;
                    if (i >= r0) {
                        this.buf = Arrays.copyOf(this.buf, this.buf.length * 2);
                    }
                    i = i + 1;
                    this.buf[i] = (char) c;
                    i = i;
                }
                r20 = this.ttype;
                if (d == r0) {
                    d = NEED_CHAR;
                }
                this.peekc = d;
                this.sval = String.copyValueOf(this.buf, 0, i);
                return this.ttype;
            }
            r20 = this.ttype;
            if (d == r0) {
                d = NEED_CHAR;
            }
            this.peekc = d;
            this.sval = String.copyValueOf(this.buf, 0, i);
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
                        if (c == TT_EOL) {
                            c = read();
                        }
                    } else if (c == TT_EOL) {
                        this.LINENO++;
                        c = read();
                    }
                    if (c < 0) {
                        this.ttype = TT_EOF;
                        return TT_EOF;
                    }
                    prevc = c;
                }
            } else if (c == 47 && this.slashSlashCommentsP) {
                do {
                    c = read();
                    if (c == TT_EOL || c == 13) {
                        this.peekc = c;
                    }
                } while (c >= 0);
                this.peekc = c;
                return nextToken();
            } else if ((ct[47] & 16) != 0) {
                do {
                    c = read();
                    if (c == TT_EOL || c == 13) {
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
                if (c == TT_EOL || c == 13) {
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
        if (this.ttype != TT_NOTHING) {
            this.pushedBack = true;
        }
    }

    public int lineno() {
        return this.LINENO;
    }

    public String toString() {
        String ret;
        switch (this.ttype) {
            case TT_NOTHING /*-4*/:
                ret = "NOTHING";
                break;
            case TT_WORD /*-3*/:
                ret = this.sval;
                break;
            case TT_NUMBER /*-2*/:
                ret = "n=" + this.nval;
                break;
            case TT_EOF /*-1*/:
                ret = "EOF";
                break;
            case TT_EOL /*10*/:
                ret = "EOL";
                break;
            default:
                if (this.ttype < Record.maxPadding && (this.ctype[this.ttype] & 8) != 0) {
                    ret = this.sval;
                    break;
                }
                ret = new String(new char[]{'\'', '\'', (char) this.ttype});
                break;
        }
        return "Token[" + ret + "], line " + this.LINENO;
    }
}
