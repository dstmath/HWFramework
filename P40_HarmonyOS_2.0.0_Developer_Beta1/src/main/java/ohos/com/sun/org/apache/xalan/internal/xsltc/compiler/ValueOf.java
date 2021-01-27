package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/* access modifiers changed from: package-private */
public final class ValueOf extends Instruction {
    private boolean _escaping = true;
    private boolean _isString = false;
    private Expression _select;

    ValueOf() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("ValueOf");
        indent(i + 4);
        Util.println("select " + this._select.toString());
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        this._select = parser.parseExpression(this, Constants.ATTRNAME_SELECT, null);
        if (this._select.isDummy()) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, Constants.ATTRNAME_SELECT);
            return;
        }
        String attribute = getAttribute(Constants.ATTRNAME_DISABLE_OUTPUT_ESCAPING);
        if (attribute != null && attribute.equals("yes")) {
            this._escaping = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type typeCheck = this._select.typeCheck(symbolTable);
        if (typeCheck != null && !typeCheck.identicalTo(Type.Node)) {
            if (typeCheck.identicalTo(Type.NodeSet)) {
                this._select = new CastExpr(this._select, Type.Node);
            } else {
                this._isString = true;
                if (!typeCheck.identicalTo(Type.String)) {
                    this._select = new CastExpr(this._select, Type.String);
                }
                this._isString = true;
            }
        }
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "setEscaping", "(Z)Z");
        if (!this._escaping) {
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new PUSH(constantPool, false));
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        }
        if (this._isString) {
            int addMethodref = constantPool.addMethodref(Constants.TRANSLET_CLASS, "characters", Constants.CHARACTERSW_SIG);
            instructionList.append(classGenerator.loadTranslet());
            this._select.translate(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEVIRTUAL(addMethodref));
        } else {
            int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "characters", Constants.CHARACTERS_SIG);
            instructionList.append(methodGenerator.loadDOM());
            this._select.translate(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 3));
        }
        if (!this._escaping) {
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(SWAP);
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
            instructionList.append(POP);
        }
    }
}
