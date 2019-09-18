package java.nio.charset;

public class IllegalCharsetNameException extends IllegalArgumentException {
    private static final long serialVersionUID = 1457525358470002989L;
    private String charsetName;

    public IllegalCharsetNameException(String charsetName2) {
        super(String.valueOf((Object) charsetName2));
        this.charsetName = charsetName2;
    }

    public String getCharsetName() {
        return this.charsetName;
    }
}
