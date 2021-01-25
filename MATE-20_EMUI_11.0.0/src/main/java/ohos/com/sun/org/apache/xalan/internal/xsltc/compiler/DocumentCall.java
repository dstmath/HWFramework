package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class DocumentCall extends FunctionCall {
    private Expression _arg1 = null;
    private Type _arg1Type;
    private Expression _arg2 = null;

    public DocumentCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        int argumentCount = argumentCount();
        if (argumentCount < 1 || argumentCount > 2) {
            throw new TypeCheckError(new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, (SyntaxTreeNode) this));
        } else if (getStylesheet() != null) {
            this._arg1 = argument(0);
            Expression expression = this._arg1;
            if (expression != null) {
                this._arg1Type = expression.typeCheck(symbolTable);
                if (!(this._arg1Type == Type.NodeSet || this._arg1Type == Type.String)) {
                    this._arg1 = new CastExpr(this._arg1, Type.String);
                }
                if (argumentCount == 2) {
                    this._arg2 = argument(1);
                    Expression expression2 = this._arg2;
                    if (expression2 != null) {
                        Type typeCheck = expression2.typeCheck(symbolTable);
                        if (typeCheck.identicalTo(Type.Node)) {
                            this._arg2 = new CastExpr(this._arg2, Type.NodeSet);
                        } else if (!typeCheck.identicalTo(Type.NodeSet)) {
                            throw new TypeCheckError(new ErrorMsg(ErrorMsg.DOCUMENT_ARG_ERR, (SyntaxTreeNode) this));
                        }
                    } else {
                        throw new TypeCheckError(new ErrorMsg(ErrorMsg.DOCUMENT_ARG_ERR, (SyntaxTreeNode) this));
                    }
                }
                Type type = Type.NodeSet;
                this._type = type;
                return type;
            }
            throw new TypeCheckError(new ErrorMsg(ErrorMsg.DOCUMENT_ARG_ERR, (SyntaxTreeNode) this));
        } else {
            throw new TypeCheckError(new ErrorMsg(ErrorMsg.ILLEGAL_ARG_ERR, (SyntaxTreeNode) this));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int argumentCount = argumentCount();
        int addFieldref = constantPool.addFieldref(classGenerator.getClassName(), Constants.DOM_FIELD, Constants.DOM_INTF_SIG);
        int addMethodref = constantPool.addMethodref(Constants.LOAD_DOCUMENT_CLASS, "documentF", argumentCount == 1 ? "(Ljava/lang/Object;Ljava/lang/String;Lohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;" : "(Ljava/lang/Object;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Ljava/lang/String;Lohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        this._arg1.translate(classGenerator, methodGenerator);
        if (this._arg1Type == Type.NodeSet) {
            this._arg1.startIterator(classGenerator, methodGenerator);
        }
        if (argumentCount == 2) {
            this._arg2.translate(classGenerator, methodGenerator);
            this._arg2.startIterator(classGenerator, methodGenerator);
        }
        instructionList.append(new PUSH(constantPool, getStylesheet().getSystemId()));
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(DUP);
        instructionList.append(new GETFIELD(addFieldref));
        instructionList.append(new INVOKESTATIC(addMethodref));
    }
}
