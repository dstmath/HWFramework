package com.android.internal.alsa;

public class LineTokenizer {
    public static final int kTokenNotFound = -1;
    private String mDelimiters;

    public LineTokenizer(String delimiters) {
        this.mDelimiters = "";
        this.mDelimiters = delimiters;
    }

    int nextToken(String line, int startIndex) {
        int len = line.length();
        int offset = startIndex;
        while (offset < len && this.mDelimiters.indexOf(line.charAt(offset)) != kTokenNotFound) {
            offset++;
        }
        if (offset < len) {
            return offset;
        }
        return kTokenNotFound;
    }

    int nextDelimiter(String line, int startIndex) {
        int len = line.length();
        int offset = startIndex;
        while (offset < len && this.mDelimiters.indexOf(line.charAt(offset)) == kTokenNotFound) {
            offset++;
        }
        if (offset < len) {
            return offset;
        }
        return kTokenNotFound;
    }
}
