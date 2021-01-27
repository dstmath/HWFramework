package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.IFLT;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class ContainsCall extends FunctionCall {
    private Expression _base = null;
    private Expression _token = null;

    public boolean isBoolean() {
        return true;
    }

    public ContainsCall(QName qName, Vector vector) {
        super(qName, vector);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (argumentCount() == 2) {
            this._base = argument(0);
            if (this._base.typeCheck(symbolTable) != Type.String) {
                this._base = new CastExpr(this._base, Type.String);
            }
            this._token = argument(1);
            if (this._token.typeCheck(symbolTable) != Type.String) {
                this._token = new CastExpr(this._token, Type.String);
            }
            Type type = Type.Boolean;
            this._type = type;
            return type;
        }
        throw new TypeCheckError(ErrorMsg.ILLEGAL_ARG_ERR, getName(), this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        translateDesynthesized(classGenerator, methodGenerator);
        synthesize(classGenerator, methodGenerator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FunctionCall, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public void translateDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        this._base.translate(classGenerator, methodGenerator);
        this._token.translate(classGenerator, methodGenerator);
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref("java.lang.String", "indexOf", Constants.STRING_TO_INT_SIG)));
        this._falseList.add(instructionList.append((BranchInstruction) new IFLT(null)));
    }
}
