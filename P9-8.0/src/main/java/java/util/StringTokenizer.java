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
        if (this.delimiters == null) {
            this.maxDelimCodePoint = 0;
            return;
        }
        int c;
        int m = 0;
        int count = 0;
        int i = 0;
        while (i < this.delimiters.length()) {
            c = this.delimiters.charAt(i);
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
            i = 0;
            int j = 0;
            while (i < count) {
                c = this.delimiters.codePointAt(j);
                this.delimiterCodePoints[i] = c;
                i++;
                j += Character.charCount(c);
            }
        }
    }

    public StringTokenizer(String str, String delim, boolean returnDelims) {
        this.hasSurrogates = false;
        this.currentPosition = 0;
        this.newPosition = -1;
        this.delimsChanged = false;
        this.str = str;
        this.maxPosition = str.length();
        this.delimiters = delim;
        this.retDelims = returnDelims;
        setMaxDelimCodePoint();
    }

    public StringTokenizer(String str, String delim) {
        this(str, delim, false);
    }

    public StringTokenizer(String str) {
        this(str, " \t\n\r\f", false);
    }

    private int skipDelimiters(int startPos) {
        if (this.delimiters == null) {
            throw new NullPointerException();
        }
        int position = startPos;
        while (!this.retDelims && position < this.maxPosition) {
            if (!this.hasSurrogates) {
                int c = this.str.charAt(position);
                if (c > this.maxDelimCodePoint || this.delimiters.indexOf(c) < 0) {
                    break;
                }
                position++;
            } else {
                int c2 = this.str.codePointAt(position);
                if (c2 > this.maxDelimCodePoint || (isDelimiter(c2) ^ 1) != 0) {
                    break;
                }
                position += Character.charCount(c2);
            }
        }
        return position;
    }

    private int scanToken(int startPos) {
        int c;
        int c2;
        int position = startPos;
        while (position < this.maxPosition) {
            if (!this.hasSurrogates) {
                c = this.str.charAt(position);
                if (c <= this.maxDelimCodePoint && this.delimiters.indexOf(c) >= 0) {
                    break;
                }
                position++;
            } else {
                c2 = this.str.codePointAt(position);
                if (c2 <= this.maxDelimCodePoint && isDelimiter(c2)) {
                    break;
                }
                position += Character.charCount(c2);
            }
        }
        if (!this.retDelims || startPos != position) {
            return position;
        }
        if (this.hasSurrogates) {
            c2 = this.str.codePointAt(position);
            if (c2 > this.maxDelimCodePoint || !isDelimiter(c2)) {
                return position;
            }
            return position + Character.charCount(c2);
        }
        c = this.str.charAt(position);
        if (c > this.maxDelimCodePoint || this.delimiters.indexOf(c) < 0) {
            return position;
        }
        return position + 1;
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
        int skipDelimiters = (this.newPosition < 0 || (this.delimsChanged ^ 1) == 0) ? skipDelimiters(this.currentPosition) : this.newPosition;
        this.currentPosition = skipDelimiters;
        this.delimsChanged = false;
        this.newPosition = -1;
        if (this.currentPosition >= this.maxPosition) {
            throw new NoSuchElementException();
        }
        int start = this.currentPosition;
        this.currentPosition = scanToken(this.currentPosition);
        return this.str.substring(start, this.currentPosition);
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
            currpos = skipDelimiters(currpos);
            if (currpos >= this.maxPosition) {
                break;
            }
            currpos = scanToken(currpos);
            count++;
        }
        return count;
    }
}
