package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class Message extends Instruction {
    private boolean _terminate = false;

    Message() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute(Constants.ATTRNAME_TERMINATE);
        if (attribute != null) {
            this._terminate = attribute.equals("yes");
        }
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(classGenerator.loadTranslet());
        int elementCount = elementCount();
        if (elementCount != 0) {
            if (elementCount == 1) {
                SyntaxTreeNode elementAt = elementAt(0);
                if (elementAt instanceof Text) {
                    instructionList.append(new PUSH(constantPool, ((Text) elementAt).getText()));
                }
            }
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new NEW(constantPool.addClass(Constants.STREAM_XML_OUTPUT)));
            instructionList.append(methodGenerator.storeHandler());
            instructionList.append(new NEW(constantPool.addClass(Constants.STRING_WRITER)));
            instructionList.append(DUP);
            instructionList.append(DUP);
            instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.STRING_WRITER, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "()V")));
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.STREAM_XML_OUTPUT, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "()V")));
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(SWAP);
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "setWriter", "(Ljava/io/Writer;)V"), 2));
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new PUSH(constantPool, "UTF-8"));
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "setEncoding", "(Ljava/lang/String;)V"), 2));
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(ICONST_1);
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "setOmitXMLDeclaration", "(Z)V"), 2));
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "startDocument", "()V"), 1));
            translateContents(classGenerator, methodGenerator);
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "endDocument", "()V"), 1));
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.STRING_WRITER, "toString", "()Ljava/lang/String;")));
            instructionList.append(SWAP);
            instructionList.append(methodGenerator.storeHandler());
        } else {
            instructionList.append(new PUSH(constantPool, ""));
        }
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, "displayMessage", "(Ljava/lang/String;)V")));
        if (this._terminate) {
            int addMethodref = constantPool.addMethodref("java.lang.RuntimeException", ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Ljava/lang/String;)V");
            instructionList.append(new NEW(constantPool.addClass("java.lang.RuntimeException")));
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, "Termination forced by an xsl:message instruction"));
            instructionList.append(new INVOKESPECIAL(addMethodref));
            instructionList.append(ATHROW);
        }
    }
}
