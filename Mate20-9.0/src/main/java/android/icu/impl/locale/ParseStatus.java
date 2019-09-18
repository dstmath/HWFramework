package android.icu.impl.locale;

public class ParseStatus {
    int _errorIndex = -1;
    String _errorMsg = null;
    int _parseLength = 0;

    public void reset() {
        this._parseLength = 0;
        this._errorIndex = -1;
        this._errorMsg = null;
    }

    public boolean isError() {
        return this._errorIndex >= 0;
    }

    public int getErrorIndex() {
        return this._errorIndex;
    }

    public int getParseLength() {
        return this._parseLength;
    }

    public String getErrorMessage() {
        return this._errorMsg;
    }
}
