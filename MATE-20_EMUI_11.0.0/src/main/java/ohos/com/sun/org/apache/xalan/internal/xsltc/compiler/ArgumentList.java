package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

final class ArgumentList {
    private final Expression _arg;
    private final ArgumentList _rest;

    public ArgumentList(Expression expression, ArgumentList argumentList) {
        this._arg = expression;
        this._rest = argumentList;
    }

    public String toString() {
        if (this._rest == null) {
            return this._arg.toString();
        }
        return this._arg.toString() + ", " + this._rest.toString();
    }
}
