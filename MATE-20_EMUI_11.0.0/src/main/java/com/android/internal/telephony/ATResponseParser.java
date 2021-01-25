package com.android.internal.telephony;

public class ATResponseParser {
    private String mLine;
    private int mNext = 0;
    private int mTokEnd;
    private int mTokStart;

    public ATResponseParser(String line) {
        this.mLine = line;
    }

    public boolean nextBoolean() {
        nextTok();
        int i = this.mTokEnd;
        int i2 = this.mTokStart;
        if (i - i2 <= 1) {
            char c = this.mLine.charAt(i2);
            if (c == '0') {
                return false;
            }
            if (c == '1') {
                return true;
            }
            throw new ATParseEx();
        }
        throw new ATParseEx();
    }

    public int nextInt() {
        int ret = 0;
        nextTok();
        for (int i = this.mTokStart; i < this.mTokEnd; i++) {
            char c = this.mLine.charAt(i);
            if (c < '0' || c > '9') {
                throw new ATParseEx();
            }
            ret = (ret * 10) + (c - '0');
        }
        return ret;
    }

    public String nextString() {
        nextTok();
        return this.mLine.substring(this.mTokStart, this.mTokEnd);
    }

    public boolean hasMore() {
        return this.mNext < this.mLine.length();
    }

    private void nextTok() {
        int len = this.mLine.length();
        if (this.mNext == 0) {
            skipPrefix();
        }
        int i = this.mNext;
        if (i < len) {
            try {
                String str = this.mLine;
                this.mNext = i + 1;
                char c = skipWhiteSpace(str.charAt(i));
                if (c != '\"') {
                    this.mTokStart = this.mNext - 1;
                    this.mTokEnd = this.mTokStart;
                    while (c != ',') {
                        if (!Character.isWhitespace(c)) {
                            this.mTokEnd = this.mNext;
                        }
                        if (this.mNext != len) {
                            String str2 = this.mLine;
                            int i2 = this.mNext;
                            this.mNext = i2 + 1;
                            c = str2.charAt(i2);
                        } else {
                            return;
                        }
                    }
                } else if (this.mNext < len) {
                    String str3 = this.mLine;
                    int i3 = this.mNext;
                    this.mNext = i3 + 1;
                    char c2 = str3.charAt(i3);
                    this.mTokStart = this.mNext - 1;
                    while (c2 != '\"' && this.mNext < len) {
                        String str4 = this.mLine;
                        int i4 = this.mNext;
                        this.mNext = i4 + 1;
                        c2 = str4.charAt(i4);
                    }
                    if (c2 == '\"') {
                        this.mTokEnd = this.mNext - 1;
                        if (this.mNext < len) {
                            String str5 = this.mLine;
                            int i5 = this.mNext;
                            this.mNext = i5 + 1;
                            if (str5.charAt(i5) != ',') {
                                throw new ATParseEx();
                            }
                            return;
                        }
                        return;
                    }
                    throw new ATParseEx();
                } else {
                    throw new ATParseEx();
                }
            } catch (StringIndexOutOfBoundsException e) {
                throw new ATParseEx();
            }
        } else {
            throw new ATParseEx();
        }
    }

    private char skipWhiteSpace(char c) {
        int len = this.mLine.length();
        while (this.mNext < len && Character.isWhitespace(c)) {
            String str = this.mLine;
            int i = this.mNext;
            this.mNext = i + 1;
            c = str.charAt(i);
        }
        if (!Character.isWhitespace(c)) {
            return c;
        }
        throw new ATParseEx();
    }

    private void skipPrefix() {
        int i;
        String str;
        this.mNext = 0;
        int s = this.mLine.length();
        do {
            i = this.mNext;
            if (i < s) {
                str = this.mLine;
                this.mNext = i + 1;
            } else {
                throw new ATParseEx("missing prefix");
            }
        } while (str.charAt(i) != ':');
    }
}
