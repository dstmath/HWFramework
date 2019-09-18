package android.icu.impl;

import android.icu.text.SymbolTable;
import android.icu.text.UTF16;
import java.text.ParsePosition;

public class RuleCharacterIterator {
    public static final int DONE = -1;
    public static final int PARSE_ESCAPES = 2;
    public static final int PARSE_VARIABLES = 1;
    public static final int SKIP_WHITESPACE = 4;
    private char[] buf;
    private int bufPos;
    private boolean isEscaped;
    private ParsePosition pos;
    private SymbolTable sym;
    private String text;

    public RuleCharacterIterator(String text2, SymbolTable sym2, ParsePosition pos2) {
        if (text2 == null || pos2.getIndex() > text2.length()) {
            throw new IllegalArgumentException();
        }
        this.text = text2;
        this.sym = sym2;
        this.pos = pos2;
        this.buf = null;
    }

    public boolean atEnd() {
        return this.buf == null && this.pos.getIndex() == this.text.length();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006d, code lost:
        if (r0 != 92) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0071, code lost:
        if ((r7 & 2) == 0) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0073, code lost:
        r3 = new int[]{0};
        r0 = android.icu.impl.Utility.unescapeAt(lookahead(), r3);
        jumpahead(r3[0]);
        r6.isEscaped = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0087, code lost:
        if (r0 < 0) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0091, code lost:
        throw new java.lang.IllegalArgumentException("Invalid escape");
     */
    public int next(int options) {
        int c;
        this.isEscaped = false;
        while (true) {
            c = _current();
            _advance(UTF16.getCharCount(c));
            if (c == 36 && this.buf == null && (options & 1) != 0 && this.sym != null) {
                String name = this.sym.parseReference(this.text, this.pos, this.text.length());
                if (name == null) {
                    break;
                }
                this.bufPos = 0;
                this.buf = this.sym.lookup(name);
                if (this.buf == null) {
                    throw new IllegalArgumentException("Undefined variable: " + name);
                } else if (this.buf.length == 0) {
                    this.buf = null;
                }
            } else if ((options & 4) == 0 || !PatternProps.isWhiteSpace(c)) {
            }
        }
        return c;
    }

    public boolean isEscaped() {
        return this.isEscaped;
    }

    public boolean inVariable() {
        return this.buf != null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    public Object getPos(Object p) {
        if (p == null) {
            return new Object[]{this.buf, new int[]{this.pos.getIndex(), this.bufPos}};
        }
        Object[] a = (Object[]) p;
        a[0] = this.buf;
        int[] v = (int[]) a[1];
        v[0] = this.pos.getIndex();
        v[1] = this.bufPos;
        return p;
    }

    public void setPos(Object p) {
        Object[] a = (Object[]) p;
        this.buf = (char[]) a[0];
        int[] v = (int[]) a[1];
        this.pos.setIndex(v[0]);
        this.bufPos = v[1];
    }

    public void skipIgnored(int options) {
        if ((options & 4) != 0) {
            while (true) {
                int a = _current();
                if (PatternProps.isWhiteSpace(a)) {
                    _advance(UTF16.getCharCount(a));
                } else {
                    return;
                }
            }
        }
    }

    public String lookahead() {
        if (this.buf != null) {
            return new String(this.buf, this.bufPos, this.buf.length - this.bufPos);
        }
        return this.text.substring(this.pos.getIndex());
    }

    public void jumpahead(int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        } else if (this.buf != null) {
            this.bufPos += count;
            if (this.bufPos > this.buf.length) {
                throw new IllegalArgumentException();
            } else if (this.bufPos == this.buf.length) {
                this.buf = null;
            }
        } else {
            int i = this.pos.getIndex() + count;
            this.pos.setIndex(i);
            if (i > this.text.length()) {
                throw new IllegalArgumentException();
            }
        }
    }

    public String toString() {
        int b = this.pos.getIndex();
        return this.text.substring(0, b) + '|' + this.text.substring(b);
    }

    private int _current() {
        if (this.buf != null) {
            return UTF16.charAt(this.buf, 0, this.buf.length, this.bufPos);
        }
        int i = this.pos.getIndex();
        return i < this.text.length() ? UTF16.charAt(this.text, i) : -1;
    }

    private void _advance(int count) {
        if (this.buf != null) {
            this.bufPos += count;
            if (this.bufPos == this.buf.length) {
                this.buf = null;
                return;
            }
            return;
        }
        this.pos.setIndex(this.pos.getIndex() + count);
        if (this.pos.getIndex() > this.text.length()) {
            this.pos.setIndex(this.text.length());
        }
    }
}
