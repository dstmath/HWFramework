package org.apache.http.message;

import java.util.NoSuchElementException;
import org.apache.http.HeaderIterator;
import org.apache.http.ParseException;
import org.apache.http.TokenIterator;

@Deprecated
public class BasicTokenIterator implements TokenIterator {
    public static final String HTTP_SEPARATORS = " ,;=()<>@:\\\"/[]?{}\t";
    protected String currentHeader;
    protected String currentToken;
    protected final HeaderIterator headerIt;
    protected int searchPos;

    public BasicTokenIterator(HeaderIterator headerIterator) {
        if (headerIterator != null) {
            this.headerIt = headerIterator;
            this.searchPos = findNext(-1);
            return;
        }
        throw new IllegalArgumentException("Header iterator must not be null.");
    }

    @Override // org.apache.http.TokenIterator, java.util.Iterator
    public boolean hasNext() {
        return this.currentToken != null;
    }

    @Override // org.apache.http.TokenIterator
    public String nextToken() throws NoSuchElementException, ParseException {
        if (this.currentToken != null) {
            String result = this.currentToken;
            this.searchPos = findNext(this.searchPos);
            return result;
        }
        throw new NoSuchElementException("Iteration already finished.");
    }

    @Override // java.util.Iterator
    public final Object next() throws NoSuchElementException, ParseException {
        return nextToken();
    }

    @Override // java.util.Iterator
    public final void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Removing tokens is not supported.");
    }

    /* access modifiers changed from: protected */
    public int findNext(int from) throws ParseException {
        int from2;
        if (from >= 0) {
            from2 = findTokenSeparator(from);
        } else if (!this.headerIt.hasNext()) {
            return -1;
        } else {
            this.currentHeader = this.headerIt.nextHeader().getValue();
            from2 = 0;
        }
        int start = findTokenStart(from2);
        if (start < 0) {
            this.currentToken = null;
            return -1;
        }
        int end = findTokenEnd(start);
        this.currentToken = createToken(this.currentHeader, start, end);
        return end;
    }

    /* access modifiers changed from: protected */
    public String createToken(String value, int start, int end) {
        return value.substring(start, end);
    }

    /* access modifiers changed from: protected */
    public int findTokenStart(int from) {
        if (from >= 0) {
            boolean found = false;
            while (!found) {
                String str = this.currentHeader;
                if (str == null) {
                    break;
                }
                int to = str.length();
                while (!found && from < to) {
                    char ch = this.currentHeader.charAt(from);
                    if (isTokenSeparator(ch) || isWhitespace(ch)) {
                        from++;
                    } else if (isTokenChar(this.currentHeader.charAt(from))) {
                        found = true;
                    } else {
                        throw new ParseException("Invalid character before token (pos " + from + "): " + this.currentHeader);
                    }
                }
                if (!found) {
                    if (this.headerIt.hasNext()) {
                        this.currentHeader = this.headerIt.nextHeader().getValue();
                        from = 0;
                    } else {
                        this.currentHeader = null;
                    }
                }
            }
            if (found) {
                return from;
            }
            return -1;
        }
        throw new IllegalArgumentException("Search position must not be negative: " + from);
    }

    /* access modifiers changed from: protected */
    public int findTokenSeparator(int from) {
        if (from >= 0) {
            boolean found = false;
            int to = this.currentHeader.length();
            while (!found && from < to) {
                char ch = this.currentHeader.charAt(from);
                if (isTokenSeparator(ch)) {
                    found = true;
                } else if (isWhitespace(ch)) {
                    from++;
                } else if (isTokenChar(ch)) {
                    throw new ParseException("Tokens without separator (pos " + from + "): " + this.currentHeader);
                } else {
                    throw new ParseException("Invalid character after token (pos " + from + "): " + this.currentHeader);
                }
            }
            return from;
        }
        throw new IllegalArgumentException("Search position must not be negative: " + from);
    }

    /* access modifiers changed from: protected */
    public int findTokenEnd(int from) {
        if (from >= 0) {
            int to = this.currentHeader.length();
            int end = from + 1;
            while (end < to && isTokenChar(this.currentHeader.charAt(end))) {
                end++;
            }
            return end;
        }
        throw new IllegalArgumentException("Token start position must not be negative: " + from);
    }

    /* access modifiers changed from: protected */
    public boolean isTokenSeparator(char ch) {
        return ch == ',';
    }

    /* access modifiers changed from: protected */
    public boolean isWhitespace(char ch) {
        return ch == '\t' || Character.isSpaceChar(ch);
    }

    /* access modifiers changed from: protected */
    public boolean isTokenChar(char ch) {
        if (Character.isLetterOrDigit(ch)) {
            return true;
        }
        if (!Character.isISOControl(ch) && !isHttpSeparator(ch)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isHttpSeparator(char ch) {
        return HTTP_SEPARATORS.indexOf(ch) >= 0;
    }
}
