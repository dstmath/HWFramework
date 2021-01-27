package ohos.global.icu.impl.locale;

public class StringTokenIterator {
    private String _dlms;
    private boolean _done;
    private int _end;
    private int _start;
    private String _text;
    private String _token;

    public StringTokenIterator(String str, String str2) {
        this._text = str;
        this._dlms = str2;
        setStart(0);
    }

    public String first() {
        setStart(0);
        return this._token;
    }

    public String current() {
        return this._token;
    }

    public int currentStart() {
        return this._start;
    }

    public int currentEnd() {
        return this._end;
    }

    public boolean isDone() {
        return this._done;
    }

    public String next() {
        if (hasNext()) {
            this._start = this._end + 1;
            this._end = nextDelimiter(this._start);
            this._token = this._text.substring(this._start, this._end);
        } else {
            this._start = this._end;
            this._token = null;
            this._done = true;
        }
        return this._token;
    }

    public boolean hasNext() {
        return this._end < this._text.length();
    }

    public StringTokenIterator setStart(int i) {
        if (i <= this._text.length()) {
            this._start = i;
            this._end = nextDelimiter(this._start);
            this._token = this._text.substring(this._start, this._end);
            this._done = false;
            return this;
        }
        throw new IndexOutOfBoundsException();
    }

    public StringTokenIterator setText(String str) {
        this._text = str;
        setStart(0);
        return this;
    }

    private int nextDelimiter(int i) {
        loop0:
        while (i < this._text.length()) {
            char charAt = this._text.charAt(i);
            for (int i2 = 0; i2 < this._dlms.length(); i2++) {
                if (charAt == this._dlms.charAt(i2)) {
                    break loop0;
                }
            }
            i++;
        }
        return i;
    }
}
