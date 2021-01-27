package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

final class TransletOutput extends Instruction {
    private boolean _append;
    private Expression _filename;

    TransletOutput() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("TransletOutput: " + this._filename);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute(AsrConstants.ASR_SRC_FILE);
        String attribute2 = getAttribute("append");
        if (attribute == null || attribute.equals("")) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, AsrConstants.ASR_SRC_FILE);
        }
        this._filename = AttributeValue.create(this, attribute, parser);
        if (attribute2 == null || (!attribute2.toLowerCase().equals("yes") && !attribute2.toLowerCase().equals("true"))) {
            this._append = false;
        } else {
            this._append = true;
        }
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (!(this._filename.typeCheck(symbolTable) instanceof StringType)) {
            this._filename = new CastExpr(this._filename, Type.String);
        }
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (classGenerator.getParser().getXSLTC().isSecureProcessing()) {
            int addMethodref = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "unallowed_extension_elementF", "(Ljava/lang/String;)V");
            instructionList.append(new PUSH(constantPool, "redirect"));
            instructionList.append(new INVOKESTATIC(addMethodref));
            return;
        }
        instructionList.append(methodGenerator.loadHandler());
        int addMethodref2 = constantPool.addMethodref(Constants.TRANSLET_CLASS, "openOutputHandler", "(Ljava/lang/String;Z)Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;");
        int addMethodref3 = constantPool.addMethodref(Constants.TRANSLET_CLASS, "closeOutputHandler", "(Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;)V");
        instructionList.append(classGenerator.loadTranslet());
        this._filename.translate(classGenerator, methodGenerator);
        instructionList.append(new PUSH(constantPool, this._append));
        instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        instructionList.append(methodGenerator.storeHandler());
        translateContents(classGenerator, methodGenerator);
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new INVOKEVIRTUAL(addMethodref3));
        instructionList.append(methodGenerator.storeHandler());
    }
}
