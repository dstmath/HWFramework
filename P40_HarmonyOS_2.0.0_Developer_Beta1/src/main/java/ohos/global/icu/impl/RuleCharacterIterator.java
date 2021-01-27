package ohos.global.icu.impl;

import java.text.ParsePosition;
import ohos.global.icu.text.SymbolTable;
import ohos.global.icu.text.UTF16;

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

    public RuleCharacterIterator(String str, SymbolTable symbolTable, ParsePosition parsePosition) {
        if (str == null || parsePosition.getIndex() > str.length()) {
            throw new IllegalArgumentException();
        }
        this.text = str;
        this.sym = symbolTable;
        this.pos = parsePosition;
        this.buf = null;
    }

    public boolean atEnd() {
        return this.buf == null && this.pos.getIndex() == this.text.length();
    }

    public int next(int i) {
        int _current;
        SymbolTable symbolTable;
        this.isEscaped = false;
        while (true) {
            _current = _current();
            _advance(UTF16.getCharCount(_current));
            if (_current == 36 && this.buf == null && (i & 1) != 0 && (symbolTable = this.sym) != null) {
                String str = this.text;
                String parseReference = symbolTable.parseReference(str, this.pos, str.length());
                if (parseReference == null) {
                    return _current;
                }
                this.bufPos = 0;
                this.buf = this.sym.lookup(parseReference);
                char[] cArr = this.buf;
                if (cArr == null) {
                    throw new IllegalArgumentException("Undefined variable: " + parseReference);
                } else if (cArr.length == 0) {
                    this.buf = null;
                }
            } else if ((i & 4) == 0 || !PatternProps.isWhiteSpace(_current)) {
                break;
            }
        }
        if (_current != 92 || (i & 2) == 0) {
            return _current;
        }
        int[] iArr = {0};
        int unescapeAt = Utility.unescapeAt(lookahead(), iArr);
        jumpahead(iArr[0]);
        this.isEscaped = true;
        if (unescapeAt >= 0) {
            return unescapeAt;
        }
        throw new IllegalArgumentException("Invalid escape");
    }

    public boolean isEscaped() {
        return this.isEscaped;
    }

    public boolean inVariable() {
        return this.buf != null;
    }

    public Object getPos(Object obj) {
        if (obj == null) {
            return new Object[]{this.buf, new int[]{this.pos.getIndex(), this.bufPos}};
        }
        Object[] objArr = (Object[]) obj;
        objArr[0] = this.buf;
        int[] iArr = (int[]) objArr[1];
        iArr[0] = this.pos.getIndex();
        iArr[1] = this.bufPos;
        return obj;
    }

    public void setPos(Object obj) {
        Object[] objArr = (Object[]) obj;
        this.buf = (char[]) objArr[0];
        int[] iArr = (int[]) objArr[1];
        this.pos.setIndex(iArr[0]);
        this.bufPos = iArr[1];
    }

    public void skipIgnored(int i) {
        if ((i & 4) != 0) {
            while (true) {
                int _current = _current();
                if (PatternProps.isWhiteSpace(_current)) {
                    _advance(UTF16.getCharCount(_current));
                } else {
                    return;
                }
            }
        }
    }

    public String lookahead() {
        char[] cArr = this.buf;
        if (cArr == null) {
            return this.text.substring(this.pos.getIndex());
        }
        int i = this.bufPos;
        return new String(cArr, i, cArr.length - i);
    }

    public void jumpahead(int i) {
        if (i >= 0) {
            char[] cArr = this.buf;
            if (cArr != null) {
                this.bufPos += i;
                int i2 = this.bufPos;
                if (i2 > cArr.length) {
                    throw new IllegalArgumentException();
                } else if (i2 == cArr.length) {
                    this.buf = null;
                }
            } else {
                int index = this.pos.getIndex() + i;
                this.pos.setIndex(index);
                if (index > this.text.length()) {
                    throw new IllegalArgumentException();
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String toString() {
        int index = this.pos.getIndex();
        return this.text.substring(0, index) + '|' + this.text.substring(index);
    }

    private int _current() {
        char[] cArr = this.buf;
        if (cArr != null) {
            return UTF16.charAt(cArr, 0, cArr.length, this.bufPos);
        }
        int index = this.pos.getIndex();
        if (index < this.text.length()) {
            return UTF16.charAt(this.text, index);
        }
        return -1;
    }

    private void _advance(int i) {
        char[] cArr = this.buf;
        if (cArr != null) {
            this.bufPos += i;
            if (this.bufPos == cArr.length) {
                this.buf = null;
                return;
            }
            return;
        }
        ParsePosition parsePosition = this.pos;
        parsePosition.setIndex(parsePosition.getIndex() + i);
        if (this.pos.getIndex() > this.text.length()) {
            this.pos.setIndex(this.text.length());
        }
    }
}
