package sun.util.locale;

public class StringTokenIterator {
    private char delimiterChar;
    private String dlms;
    private boolean done;
    private int end;
    private int start;
    private String text;
    private String token;

    public StringTokenIterator(String text, String dlms) {
        this.text = text;
        if (dlms.length() == 1) {
            this.delimiterChar = dlms.charAt(0);
        } else {
            this.dlms = dlms;
        }
        setStart(0);
    }

    public String first() {
        setStart(0);
        return this.token;
    }

    public String current() {
        return this.token;
    }

    public int currentStart() {
        return this.start;
    }

    public int currentEnd() {
        return this.end;
    }

    public boolean isDone() {
        return this.done;
    }

    public String next() {
        if (hasNext()) {
            this.start = this.end + 1;
            this.end = nextDelimiter(this.start);
            this.token = this.text.substring(this.start, this.end);
        } else {
            this.start = this.end;
            this.token = null;
            this.done = true;
        }
        return this.token;
    }

    public boolean hasNext() {
        return this.end < this.text.length();
    }

    public StringTokenIterator setStart(int offset) {
        if (offset > this.text.length()) {
            throw new IndexOutOfBoundsException();
        }
        this.start = offset;
        this.end = nextDelimiter(this.start);
        this.token = this.text.substring(this.start, this.end);
        this.done = false;
        return this;
    }

    public StringTokenIterator setText(String text) {
        this.text = text;
        setStart(0);
        return this;
    }

    private int nextDelimiter(int start) {
        int textlen = this.text.length();
        int idx;
        if (this.dlms == null) {
            for (idx = start; idx < textlen; idx++) {
                if (this.text.charAt(idx) == this.delimiterChar) {
                    return idx;
                }
            }
        } else {
            int dlmslen = this.dlms.length();
            for (idx = start; idx < textlen; idx++) {
                char c = this.text.charAt(idx);
                for (int i = 0; i < dlmslen; i++) {
                    if (c == this.dlms.charAt(i)) {
                        return idx;
                    }
                }
            }
        }
        return textlen;
    }
}
