package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNULL;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/* access modifiers changed from: package-private */
public final class Copy extends Instruction {
    private UseAttributeSets _useSets;

    Copy() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute(Constants.ATTRNAME_USEATTRIBUTESETS);
        if (attribute.length() > 0) {
            if (!Util.isValidQNames(attribute)) {
                parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute, (SyntaxTreeNode) this));
            }
            this._useSets = new UseAttributeSets(attribute, parser);
        }
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("Copy");
        int i2 = i + 4;
        indent(i2);
        displayContents(i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        UseAttributeSets useAttributeSets = this._useSets;
        if (useAttributeSets != null) {
            useAttributeSets.typeCheck(symbolTable);
        }
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable2("name", Util.getJCRefType(Constants.STRING_SIG), null);
        LocalVariableGen addLocalVariable22 = methodGenerator.addLocalVariable2("length", Util.getJCRefType("I"), null);
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "shallowCopy", "(ILohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;)Ljava/lang/String;"), 3));
        instructionList.append(DUP);
        addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
        BranchHandle append = instructionList.append((BranchInstruction) new IFNULL(null));
        instructionList.append(new ALOAD(addLocalVariable2.getIndex()));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref("java.lang.String", "length", "()I")));
        instructionList.append(DUP);
        addLocalVariable22.setStart(instructionList.append(new ISTORE(addLocalVariable22.getIndex())));
        BranchHandle append2 = instructionList.append((BranchInstruction) new IFEQ(null));
        if (this._useSets != null) {
            boolean z = getParent() instanceof LiteralElement;
            if (z || z) {
                this._useSets.translate(classGenerator, methodGenerator);
            } else {
                instructionList.append(new ILOAD(addLocalVariable22.getIndex()));
                BranchHandle append3 = instructionList.append((BranchInstruction) new IFEQ(null));
                this._useSets.translate(classGenerator, methodGenerator);
                append3.setTarget(instructionList.append(NOP));
            }
        }
        append2.setTarget(instructionList.append(NOP));
        translateContents(classGenerator, methodGenerator);
        addLocalVariable22.setEnd(instructionList.append(new ILOAD(addLocalVariable22.getIndex())));
        BranchHandle append4 = instructionList.append((BranchInstruction) new IFEQ(null));
        instructionList.append(methodGenerator.loadHandler());
        addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
        instructionList.append(methodGenerator.endElement());
        InstructionHandle append5 = instructionList.append(NOP);
        append.setTarget(append5);
        append4.setTarget(append5);
        methodGenerator.removeLocalVariable(addLocalVariable2);
        methodGenerator.removeLocalVariable(addLocalVariable22);
    }
}
