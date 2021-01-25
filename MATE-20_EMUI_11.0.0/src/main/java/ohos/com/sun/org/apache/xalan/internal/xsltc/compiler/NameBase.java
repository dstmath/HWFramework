package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

/* access modifiers changed from: package-private */
public class NameBase extends FunctionCall {
    private Expression _param;
    private Type _paramType;

    public NameBase(QName qName) {
        super(qName);
        this._param = null;
        this._paramType = Type.Node;
    }

    public NameBase(QName qName, Vector vector) {
        super(qName, vector);
        this._param = null;
        this._paramType = Type.Node;
        this._param = argument(0);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        int argumentCount = argumentCount();
        if (argumentCount == 0) {
            this._paramType = Type.Node;
        } else if (argumentCount == 1) {
            this._paramType = this._param.typeCheck(symbolTable);
        } else {
            throw new TypeCheckError(this);
        }
        if (this._paramType == Type.NodeSet || this._paramType == Type.Node || this._paramType == Type.Reference) {
            Type type = Type.String;
            this._type = type;
            return type;
        }
        throw new TypeCheckError(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public Type getType() {
        return this._type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(methodGenerator.loadDOM());
        if (argumentCount() == 0) {
            instructionList.append(methodGenerator.loadContextNode());
        } else if (this._paramType == Type.Node) {
            this._param.translate(classGenerator, methodGenerator);
        } else if (this._paramType == Type.Reference) {
            this._param.translate(classGenerator, methodGenerator);
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToNodeSet", "(Ljava/lang/Object;)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;")));
            instructionList.append(methodGenerator.nextNode());
        } else {
            this._param.translate(classGenerator, methodGenerator);
            this._param.startIterator(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.nextNode());
        }
    }
}
