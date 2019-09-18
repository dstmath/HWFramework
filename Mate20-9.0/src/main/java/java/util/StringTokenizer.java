package java.util;

public class StringTokenizer implements Enumeration<Object> {
    private int currentPosition;
    private int[] delimiterCodePoints;
    private String delimiters;
    private boolean delimsChanged;
    private boolean hasSurrogates;
    private int maxDelimCodePoint;
    private int maxPosition;
    private int newPosition;
    private boolean retDelims;
    private String str;

    private void setMaxDelimCodePoint() {
        int j = 0;
        if (this.delimiters == null) {
            this.maxDelimCodePoint = 0;
            return;
        }
        int count = 0;
        int m = 0;
        int i = 0;
        while (i < this.delimiters.length()) {
            int c = this.delimiters.charAt(i);
            if (c >= 55296 && c <= 57343) {
                c = this.delimiters.codePointAt(i);
                this.hasSurrogates = true;
            }
            if (m < c) {
                m = c;
            }
            count++;
            i += Character.charCount(c);
        }
        this.maxDelimCodePoint = m;
        if (this.hasSurrogates) {
            this.delimiterCodePoints = new int[count];
            int i2 = 0;
            while (i2 < count) {
                int c2 = this.delimiters.codePointAt(j);
                this.delimiterCodePoints[i2] = c2;
                i2++;
                j += Character.charCount(c2);
            }
        }
    }

    public StringTokenizer(String str2, String delim, boolean returnDelims) {
        this.hasSurrogates = false;
        this.currentPosition = 0;
        this.newPosition = -1;
        this.delimsChanged = false;
        this.str = str2;
        this.maxPosition = str2.length();
        this.delimiters = delim;
        this.retDelims = returnDelims;
        setMaxDelimCodePoint();
    }

    public StringTokenizer(String str2, String delim) {
        this(str2, delim, false);
    }

    public StringTokenizer(String str2) {
        this(str2, " \t\n\r\f", false);
    }

    private int skipDelimiters(int startPos) {
        if (this.delimiters != null) {
            int position = startPos;
            while (!this.retDelims && position < this.maxPosition) {
                if (this.hasSurrogates) {
                    int c = this.str.codePointAt(position);
                    if (c > this.maxDelimCodePoint || !isDelimiter(c)) {
                        break;
                    }
                    position += Character.charCount(c);
                } else {
                    char c2 = this.str.charAt(position);
                    if (c2 > this.maxDelimCodePoint || this.delimiters.indexOf((int) c2) < 0) {
                        break;
                    }
                    position++;
                }
            }
            return position;
        }
        throw new NullPointerException();
    }

    private int scanToken(int startPos) {
        int position = startPos;
        while (position < this.maxPosition) {
            if (this.hasSurrogates) {
                int c = this.str.codePointAt(position);
                if (c <= this.maxDelimCodePoint && isDelimiter(c)) {
                    break;
                }
                position += Character.charCount(c);
            } else {
                char c2 = this.str.charAt(position);
                if (c2 <= this.maxDelimCodePoint && this.delimiters.indexOf((int) c2) >= 0) {
                    break;
                }
                position++;
            }
        }
        if (!this.retDelims || startPos != position) {
            return position;
        }
        if (!this.hasSurrogates) {
            char c3 = this.str.charAt(position);
            if (c3 > this.maxDelimCodePoint || this.delimiters.indexOf((int) c3) < 0) {
                return position;
            }
            return position + 1;
        }
        int c4 = this.str.codePointAt(position);
        if (c4 > this.maxDelimCodePoint || !isDelimiter(c4)) {
            return position;
        }
        return position + Character.charCount(c4);
    }

    private boolean isDelimiter(int codePoint) {
        for (int i : this.delimiterCodePoints) {
            if (i == codePoint) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMoreTokens() {
        this.newPosition = skipDelimiters(this.currentPosition);
        return this.newPosition < this.maxPosition;
    }

    public String nextToken() {
        this.currentPosition = (this.newPosition < 0 || this.delimsChanged) ? skipDelimiters(this.currentPosition) : this.newPosition;
        this.delimsChanged = false;
        this.newPosition = -1;
        if (this.currentPosition < this.maxPosition) {
            int start = this.currentPosition;
            this.currentPosition = scanToken(this.currentPosition);
            return this.str.substring(start, this.currentPosition);
        }
        throw new NoSuchElementException();
    }

    public String nextToken(String delim) {
        this.delimiters = delim;
        this.delimsChanged = true;
        setMaxDelimCodePoint();
        return nextToken();
    }

    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    public Object nextElement() {
        return nextToken();
    }

    public int countTokens() {
        int count = 0;
        int currpos = this.currentPosition;
        while (currpos < this.maxPosition) {
            int currpos2 = skipDelimiters(currpos);
            if (currpos2 >= this.maxPosition) {
                break;
            }
            currpos = scanToken(currpos2);
            count++;
        }
        return count;
    }
}
