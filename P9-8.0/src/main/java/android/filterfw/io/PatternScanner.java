package android.filterfw.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternScanner {
    private Pattern mIgnorePattern;
    private String mInput;
    private int mLineNo = 0;
    private int mOffset = 0;
    private int mStartOfLine = 0;

    public PatternScanner(String input) {
        this.mInput = input;
    }

    public PatternScanner(String input, Pattern ignorePattern) {
        this.mInput = input;
        this.mIgnorePattern = ignorePattern;
        skip(this.mIgnorePattern);
    }

    public String tryEat(Pattern pattern) {
        if (this.mIgnorePattern != null) {
            skip(this.mIgnorePattern);
        }
        Matcher matcher = pattern.matcher(this.mInput);
        matcher.region(this.mOffset, this.mInput.length());
        String result = null;
        if (matcher.lookingAt()) {
            updateLineCount(this.mOffset, matcher.end());
            this.mOffset = matcher.end();
            result = this.mInput.substring(matcher.start(), matcher.end());
        }
        if (!(result == null || this.mIgnorePattern == null)) {
            skip(this.mIgnorePattern);
        }
        return result;
    }

    public String eat(Pattern pattern, String tokenName) {
        String result = tryEat(pattern);
        if (result != null) {
            return result;
        }
        throw new RuntimeException(unexpectedTokenMessage(tokenName));
    }

    public boolean peek(Pattern pattern) {
        if (this.mIgnorePattern != null) {
            skip(this.mIgnorePattern);
        }
        Matcher matcher = pattern.matcher(this.mInput);
        matcher.region(this.mOffset, this.mInput.length());
        return matcher.lookingAt();
    }

    public void skip(Pattern pattern) {
        Matcher matcher = pattern.matcher(this.mInput);
        matcher.region(this.mOffset, this.mInput.length());
        if (matcher.lookingAt()) {
            updateLineCount(this.mOffset, matcher.end());
            this.mOffset = matcher.end();
        }
    }

    public boolean atEnd() {
        return this.mOffset >= this.mInput.length();
    }

    public int lineNo() {
        return this.mLineNo;
    }

    public String unexpectedTokenMessage(String tokenName) {
        return "Unexpected token on line " + (this.mLineNo + 1) + " after '" + this.mInput.substring(this.mStartOfLine, this.mOffset) + "' <- Expected " + tokenName + "!";
    }

    public void updateLineCount(int start, int end) {
        for (int i = start; i < end; i++) {
            if (this.mInput.charAt(i) == 10) {
                this.mLineNo++;
                this.mStartOfLine = i + 1;
            }
        }
    }
}
