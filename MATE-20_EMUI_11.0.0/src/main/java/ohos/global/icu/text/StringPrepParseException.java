package ohos.global.icu.text;

import java.text.ParseException;

public class StringPrepParseException extends ParseException {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int ACE_PREFIX_ERROR = 6;
    public static final int BUFFER_OVERFLOW_ERROR = 9;
    public static final int CHECK_BIDI_ERROR = 4;
    public static final int DOMAIN_NAME_TOO_LONG_ERROR = 11;
    public static final int ILLEGAL_CHAR_FOUND = 1;
    public static final int INVALID_CHAR_FOUND = 0;
    public static final int LABEL_TOO_LONG_ERROR = 8;
    private static final int PARSE_CONTEXT_LEN = 16;
    public static final int PROHIBITED_ERROR = 2;
    public static final int STD3_ASCII_RULES_ERROR = 5;
    public static final int UNASSIGNED_ERROR = 3;
    public static final int VERIFICATION_ERROR = 7;
    public static final int ZERO_LENGTH_LABEL = 10;
    static final long serialVersionUID = 7160264827701651255L;
    private int error;
    private int line;
    private StringBuffer postContext = new StringBuffer();
    private StringBuffer preContext = new StringBuffer();

    @Override // java.lang.Object
    public int hashCode() {
        return 42;
    }

    public StringPrepParseException(String str, int i) {
        super(str, -1);
        this.error = i;
        this.line = 0;
    }

    public StringPrepParseException(String str, int i, String str2, int i2) {
        super(str, -1);
        this.error = i;
        setContext(str2, i2);
        this.line = 0;
    }

    public StringPrepParseException(String str, int i, String str2, int i2, int i3) {
        super(str, -1);
        this.error = i;
        setContext(str2, i2);
        this.line = i3;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if ((obj instanceof StringPrepParseException) && ((StringPrepParseException) obj).error == this.error) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Throwable, java.lang.Object
    public String toString() {
        return super.getMessage() + ". line:  " + this.line + ". preContext:  " + this.preContext + ". postContext: " + this.postContext + "\n";
    }

    private void setPreContext(String str, int i) {
        setPreContext(str.toCharArray(), i);
    }

    private void setPreContext(char[] cArr, int i) {
        int i2 = 16;
        int i3 = i <= 16 ? 0 : i - 15;
        if (i3 <= 16) {
            i2 = i3;
        }
        this.preContext.append(cArr, i3, i2);
    }

    private void setPostContext(String str, int i) {
        setPostContext(str.toCharArray(), i);
    }

    private void setPostContext(char[] cArr, int i) {
        this.postContext.append(cArr, i, cArr.length - i);
    }

    private void setContext(String str, int i) {
        setPreContext(str, i);
        setPostContext(str, i);
    }

    public int getError() {
        return this.error;
    }
}
