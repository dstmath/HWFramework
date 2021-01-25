package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

public final class CompilerException extends Exception {
    static final long serialVersionUID = 1732939618562742663L;
    private String _msg;

    public CompilerException() {
    }

    public CompilerException(Exception exc) {
        super(exc.toString());
        this._msg = exc.toString();
    }

    public CompilerException(String str) {
        super(str);
        this._msg = str;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        int indexOf = this._msg.indexOf(58);
        if (indexOf > -1) {
            return this._msg.substring(indexOf);
        }
        return this._msg;
    }
}
