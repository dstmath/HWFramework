package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/* access modifiers changed from: package-private */
public final class AbsoluteLocationPath extends Expression {
    private Expression _path;

    public AbsoluteLocationPath() {
        this._path = null;
    }

    public AbsoluteLocationPath(Expression expression) {
        this._path = expression;
        if (expression != null) {
            this._path.setParent(this);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        Expression expression = this._path;
        if (expression != null) {
            expression.setParser(parser);
        }
    }

    public Expression getPath() {
        return this._path;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AbsoluteLocationPath(");
        Expression expression = this._path;
        sb.append(expression != null ? expression.toString() : "null");
        sb.append(')');
        return sb.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Expression expression = this._path;
        if (expression != null && (expression.typeCheck(symbolTable) instanceof NodeType)) {
            this._path = new CastExpr(this._path, Type.NodeSet);
        }
        Type type = Type.NodeSet;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (this._path != null) {
            int addMethodref = constantPool.addMethodref(Constants.ABSOLUTE_ITERATOR, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;)V");
            this._path.translate(classGenerator, methodGenerator);
            LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("abs_location_path_tmp", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
            addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
            instructionList.append(new NEW(constantPool.addClass(Constants.ABSOLUTE_ITERATOR)));
            instructionList.append(DUP);
            addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
            instructionList.append(new INVOKESPECIAL(addMethodref));
            return;
        }
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getIterator", "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 1));
    }
}
