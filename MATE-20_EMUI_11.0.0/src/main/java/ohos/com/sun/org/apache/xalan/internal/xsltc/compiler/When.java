package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.BooleanType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/* access modifiers changed from: package-private */
public final class When extends Instruction {
    private boolean _ignore = false;
    private Expression _test;

    When() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("When");
        int i2 = i + 4;
        indent(i2);
        System.out.print("test ");
        Util.println(this._test.toString());
        displayContents(i2);
    }

    public Expression getTest() {
        return this._test;
    }

    public boolean ignore() {
        return this._ignore;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        this._test = parser.parseExpression(this, Constants.ATTRNAME_TEST, null);
        Object evaluateAtCompileTime = this._test.evaluateAtCompileTime();
        if (evaluateAtCompileTime != null && (evaluateAtCompileTime instanceof Boolean)) {
            this._ignore = !((Boolean) evaluateAtCompileTime).booleanValue();
        }
        parseChildren(parser);
        if (this._test.isDummy()) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, Constants.ATTRNAME_TEST);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (!(this._test.typeCheck(symbolTable) instanceof BooleanType)) {
            this._test = new CastExpr(this._test, Type.Boolean);
        }
        if (!this._ignore) {
            typeCheckContents(symbolTable);
        }
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        getParser().reportError(3, new ErrorMsg(ErrorMsg.STRAY_WHEN_ERR, (SyntaxTreeNode) this));
    }
}
