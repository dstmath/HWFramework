package java.nio.charset;

public class UnsupportedCharsetException extends IllegalArgumentException {
    private static final long serialVersionUID = 1490765524727386367L;
    private String charsetName;

    public UnsupportedCharsetException(String charsetName2) {
        super(String.valueOf((Object) charsetName2));
        this.charsetName = charsetName2;
    }

    public String getCharsetName() {
        return this.charsetName;
    }
}
