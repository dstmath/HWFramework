package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

final class CopyOf extends Instruction {
    private Expression _select;

    CopyOf() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("CopyOf");
        indent(i + 4);
        Util.println("select " + this._select.toString());
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        this._select = parser.parseExpression(this, Constants.ATTRNAME_SELECT, null);
        if (this._select.isDummy()) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, Constants.ATTRNAME_SELECT);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Type typeCheck = this._select.typeCheck(symbolTable);
        if (!(typeCheck instanceof NodeType) && !(typeCheck instanceof NodeSetType) && !(typeCheck instanceof ReferenceType) && !(typeCheck instanceof ResultTreeType)) {
            this._select = new CastExpr(this._select, Type.String);
        }
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        Type type = this._select.getType();
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.ELEMNAME_COPY_STRING, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;)V");
        int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.ELEMNAME_COPY_STRING, Constants.CHARACTERS_SIG);
        int addInterfaceMethodref3 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getDocument", "()I");
        if (type instanceof NodeSetType) {
            instructionList.append(methodGenerator.loadDOM());
            this._select.translate(classGenerator, methodGenerator);
            this._select.startIterator(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 3));
        } else if (type instanceof NodeType) {
            instructionList.append(methodGenerator.loadDOM());
            this._select.translate(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 3));
        } else if (type instanceof ResultTreeType) {
            this._select.translate(classGenerator, methodGenerator);
            instructionList.append(DUP);
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref3, 1));
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 3));
        } else if (type instanceof ReferenceType) {
            this._select.translate(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(methodGenerator.loadCurrentNode());
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, Constants.ELEMNAME_COPY_STRING, "(Ljava/lang/Object;Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;ILohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)V")));
        } else {
            instructionList.append(classGenerator.loadTranslet());
            this._select.translate(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, "characters", Constants.CHARACTERSW_SIG)));
        }
    }
}
