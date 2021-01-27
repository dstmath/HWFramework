package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

final class ApplyImports extends Instruction {
    private QName _modeName;
    private int _precedence;

    ApplyImports() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("ApplyTemplates");
        int i2 = i + 4;
        indent(i2);
        if (this._modeName != null) {
            indent(i2);
            Util.println("mode " + this._modeName);
        }
    }

    public boolean hasWithParams() {
        return hasContents();
    }

    private int getMinPrecedence(int i) {
        Stylesheet stylesheet = getStylesheet();
        while (stylesheet._includedFrom != null) {
            stylesheet = stylesheet._includedFrom;
        }
        return stylesheet.getMinimumDescendantPrecedence();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        getStylesheet().setTemplateInlining(false);
        Template template = getTemplate();
        this._modeName = template.getModeName();
        this._precedence = template.getImportPrecedence();
        parser.getTopLevelStylesheet();
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Stylesheet stylesheet = classGenerator.getStylesheet();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        methodGenerator.getLocalIndex(Keywords.FUNC_CURRENT_STRING);
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadIterator());
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(methodGenerator.loadCurrentNode());
        if (stylesheet.hasLocalParams()) {
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.PUSH_PARAM_FRAME, "()V")));
        }
        int i = this._precedence;
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(classGenerator.getStylesheet().getClassName(), stylesheet.getMode(this._modeName).functionName(getMinPrecedence(i), i), classGenerator.getApplyTemplatesSigForImport())));
        if (stylesheet.hasLocalParams()) {
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.POP_PARAM_FRAME, "()V")));
        }
    }
}
