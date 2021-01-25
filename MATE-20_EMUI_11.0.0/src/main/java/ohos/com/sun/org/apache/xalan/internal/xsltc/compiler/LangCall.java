package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.FilterGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class LangCall extends FunctionCall {
    private Expression _lang = argument(0);
    private Type _langType;

    public LangCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._langType = this._lang.typeCheck(symbolTable);
        if (!(this._langType instanceof StringType)) {
            this._lang = new CastExpr(this._lang, Type.String);
        }
        return Type.Boolean;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public Type getType() {
        return Type.Boolean;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "testLanguage", "(Ljava/lang/String;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;I)Z");
        this._lang.translate(classGenerator, methodGenerator);
        instructionList.append(methodGenerator.loadDOM());
        if (classGenerator instanceof FilterGenerator) {
            instructionList.append(new ILOAD(1));
        } else {
            instructionList.append(methodGenerator.loadContextNode());
        }
        instructionList.append(new INVOKESTATIC(addMethodref));
    }
}
