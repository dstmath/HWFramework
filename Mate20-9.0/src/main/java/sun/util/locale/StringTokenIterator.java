package sun.util.locale;

public class StringTokenIterator {
    private char delimiterChar;
    private String dlms;
    private boolean done;
    private int end;
    private int start;
    private String text;
    private String token;

    public StringTokenIterator(String text2, String dlms2) {
        this.text = text2;
        if (dlms2.length() == 1) {
            this.delimiterChar = dlms2.charAt(0);
        } else {
            this.dlms = dlms2;
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
        if (offset <= this.text.length()) {
            this.start = offset;
            this.end = nextDelimiter(this.start);
            this.token = this.text.substring(this.start, this.end);
            this.done = false;
            return this;
        }
        throw new IndexOutOfBoundsException();
    }

    public StringTokenIterator setText(String text2) {
        this.text = text2;
        setStart(0);
        return this;
    }

    private int nextDelimiter(int start2) {
        int textlen = this.text.length();
        if (this.dlms == null) {
            for (int idx = start2; idx < textlen; idx++) {
                if (this.text.charAt(idx) == this.delimiterChar) {
                    return idx;
                }
            }
        } else {
            int dlmslen = this.dlms.length();
            for (int idx2 = start2; idx2 < textlen; idx2++) {
                char c = this.text.charAt(idx2);
                for (int i = 0; i < dlmslen; i++) {
                    if (c == this.dlms.charAt(i)) {
                        return idx2;
                    }
                }
            }
        }
        return textlen;
    }
}
