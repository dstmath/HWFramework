package gov.nist.core;

public class Token {
    protected int tokenType;
    protected String tokenValue;

    public String getTokenValue() {
        return this.tokenValue;
    }

    public int getTokenType() {
        return this.tokenType;
    }

    public String toString() {
        return "tokenValue = " + this.tokenValue + "/tokenType = " + this.tokenType;
    }
}
