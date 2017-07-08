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

    public RuleCharacterIterator(String text, SymbolTable sym, ParsePosition pos) {
        if (text == null || pos.getIndex() > text.length()) {
            throw new IllegalArgumentException();
        }
        this.text = text;
        this.sym = sym;
        this.pos = pos;
        this.buf = null;
    }

    public boolean atEnd() {
        return this.buf == null && this.pos.getIndex() == this.text.length();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int next(int options) {
        int c;
        this.isEscaped = false;
        while (true) {
            c = _current();
            _advance(UTF16.getCharCount(c));
            if (c == 36 && this.buf == null && (options & PARSE_VARIABLES) != 0 && this.sym != null) {
                String name = this.sym.parseReference(this.text, this.pos, this.text.length());
                if (name == null) {
                    break;
                }
                this.bufPos = 0;
                this.buf = this.sym.lookup(name);
                if (this.buf == null) {
                    break;
                } else if (this.buf.length == 0) {
                    this.buf = null;
                }
            } else if ((options & SKIP_WHITESPACE) == 0 || !PatternProps.isWhiteSpace(c)) {
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

    public Object getPos(Object p) {
        if (p == null) {
            Object obj = new Object[PARSE_ESCAPES];
            obj[0] = this.buf;
            int[] iArr = new int[PARSE_ESCAPES];
            iArr[0] = this.pos.getIndex();
            iArr[PARSE_VARIABLES] = this.bufPos;
            obj[PARSE_VARIABLES] = iArr;
            return obj;
        }
        Object[] a = (Object[]) p;
        a[0] = this.buf;
        int[] v = a[PARSE_VARIABLES];
        v[0] = this.pos.getIndex();
        v[PARSE_VARIABLES] = this.bufPos;
        return p;
    }

    public void setPos(Object p) {
        Object[] a = (Object[]) p;
        this.buf = (char[]) a[0];
        int[] v = a[PARSE_VARIABLES];
        this.pos.setIndex(v[0]);
        this.bufPos = v[PARSE_VARIABLES];
    }

    public void skipIgnored(int options) {
        if ((options & SKIP_WHITESPACE) != 0) {
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
        return i < this.text.length() ? UTF16.charAt(this.text, i) : DONE;
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
