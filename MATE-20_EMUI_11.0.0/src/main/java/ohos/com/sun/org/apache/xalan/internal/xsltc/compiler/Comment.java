package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class Comment extends Instruction {
    Comment() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        typeCheckContents(symbolTable);
        return Type.String;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0026  */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Text text;
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (elementCount() == 1) {
            SyntaxTreeNode elementAt = elementAt(0);
            if (elementAt instanceof Text) {
                text = (Text) elementAt;
                if (text == null) {
                    instructionList.append(methodGenerator.loadHandler());
                    if (text.canLoadAsArrayOffsetLength()) {
                        text.loadAsArrayOffsetLength(classGenerator, methodGenerator);
                        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", Constants.ELEMNAME_COMMENT_STRING, "([CII)V"), 4));
                        return;
                    }
                    instructionList.append(new PUSH(constantPool, text.getText()));
                    instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", Constants.ELEMNAME_COMMENT_STRING, "(Ljava/lang/String;)V"), 2));
                    return;
                }
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(DUP);
                instructionList.append(classGenerator.loadTranslet());
                instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, "stringValueHandler", Constants.STRING_VALUE_HANDLER_SIG)));
                instructionList.append(DUP);
                instructionList.append(methodGenerator.storeHandler());
                translateContents(classGenerator, methodGenerator);
                instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.STRING_VALUE_HANDLER, "getValue", "()Ljava/lang/String;")));
                instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", Constants.ELEMNAME_COMMENT_STRING, "(Ljava/lang/String;)V"), 2));
                instructionList.append(methodGenerator.storeHandler());
                return;
            }
        }
        text = null;
        if (text == null) {
        }
    }
}
