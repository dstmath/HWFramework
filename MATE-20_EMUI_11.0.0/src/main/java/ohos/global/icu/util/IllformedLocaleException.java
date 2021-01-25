package ohos.global.icu.util;

public class IllformedLocaleException extends RuntimeException {
    private static final long serialVersionUID = 1;
    private int _errIdx;

    public IllformedLocaleException() {
        this._errIdx = -1;
    }

    public IllformedLocaleException(String str) {
        super(str);
        this._errIdx = -1;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public IllformedLocaleException(String str, int i) {
        super(r0.toString());
        String str2;
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        if (i < 0) {
            str2 = "";
        } else {
            str2 = " [at index " + i + "]";
        }
        sb.append(str2);
        this._errIdx = -1;
        this._errIdx = i;
    }

    public int getErrorIndex() {
        return this._errIdx;
    }
}
