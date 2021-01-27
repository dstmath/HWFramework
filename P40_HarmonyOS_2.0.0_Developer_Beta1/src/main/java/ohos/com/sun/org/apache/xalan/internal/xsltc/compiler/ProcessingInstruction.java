package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

/* access modifiers changed from: package-private */
public final class ProcessingInstruction extends Instruction {
    private boolean _isLiteral = false;
    private AttributeValue _name;

    ProcessingInstruction() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute("name");
        if (attribute.length() > 0) {
            this._isLiteral = Util.isLiteral(attribute);
            if (this._isLiteral && !XML11Char.isXML11ValidNCName(attribute)) {
                parser.reportError(3, new ErrorMsg("INVALID_NCNAME_ERR", (Object) attribute, (SyntaxTreeNode) this));
            }
            this._name = AttributeValue.create(this, attribute, parser);
        } else {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
        }
        if (attribute.equals("xml")) {
            reportError(this, parser, ErrorMsg.ILLEGAL_PI_ERR, "xml");
        }
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._name.typeCheck(symbolTable);
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (!this._isLiteral) {
            LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable2("nameValue", Util.getJCRefType(Constants.STRING_SIG), null);
            this._name.translate(classGenerator, methodGenerator);
            addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
            instructionList.append(new ALOAD(addLocalVariable2.getIndex()));
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "checkNCName", "(Ljava/lang/String;)V")));
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(DUP);
            addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
        } else {
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(DUP);
            this._name.translate(classGenerator, methodGenerator);
        }
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, "stringValueHandler", Constants.STRING_VALUE_HANDLER_SIG)));
        instructionList.append(DUP);
        instructionList.append(methodGenerator.storeHandler());
        translateContents(classGenerator, methodGenerator);
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.STRING_VALUE_HANDLER, "getValueOfPI", "()Ljava/lang/String;")));
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "processingInstruction", "(Ljava/lang/String;Ljava/lang/String;)V"), 3));
        instructionList.append(methodGenerator.storeHandler());
    }
}
