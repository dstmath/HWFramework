package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode;

public class TypeCheckError extends Exception {
    static final long serialVersionUID = 3246224233917854640L;
    ErrorMsg _error = null;
    SyntaxTreeNode _node = null;

    public TypeCheckError(SyntaxTreeNode syntaxTreeNode) {
        this._node = syntaxTreeNode;
    }

    public TypeCheckError(ErrorMsg errorMsg) {
        this._error = errorMsg;
    }

    public TypeCheckError(String str, Object obj) {
        this._error = new ErrorMsg(str, obj);
    }

    public TypeCheckError(String str, Object obj, Object obj2) {
        this._error = new ErrorMsg(str, obj, obj2);
    }

    public ErrorMsg getErrorMsg() {
        return this._error;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return toString();
    }

    @Override // java.lang.Throwable, java.lang.Object
    public String toString() {
        if (this._error == null) {
            SyntaxTreeNode syntaxTreeNode = this._node;
            if (syntaxTreeNode != null) {
                this._error = new ErrorMsg(ErrorMsg.TYPE_CHECK_ERR, syntaxTreeNode.toString());
            } else {
                this._error = new ErrorMsg(ErrorMsg.TYPE_CHECK_UNK_LOC_ERR);
            }
        }
        return this._error.toString();
    }
}
