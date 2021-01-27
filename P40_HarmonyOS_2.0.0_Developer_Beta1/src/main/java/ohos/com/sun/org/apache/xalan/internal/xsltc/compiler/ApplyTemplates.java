package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
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
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

/* access modifiers changed from: package-private */
public final class ApplyTemplates extends Instruction {
    private String _functionName;
    private QName _modeName;
    private Expression _select;
    private Type _type = null;

    ApplyTemplates() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("ApplyTemplates");
        int i2 = i + 4;
        indent(i2);
        Util.println("select " + this._select.toString());
        if (this._modeName != null) {
            indent(i2);
            Util.println("mode " + this._modeName);
        }
    }

    public boolean hasWithParams() {
        return hasContents();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute(Constants.ATTRNAME_SELECT);
        String attribute2 = getAttribute(Constants.ATTRNAME_MODE);
        if (attribute.length() > 0) {
            this._select = parser.parseExpression(this, Constants.ATTRNAME_SELECT, null);
        }
        if (attribute2.length() > 0) {
            if (!XML11Char.isXML11ValidQName(attribute2)) {
                parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute2, (SyntaxTreeNode) this));
            }
            this._modeName = parser.getQNameIgnoreDefaultNs(attribute2);
        }
        this._functionName = parser.getTopLevelStylesheet().getMode(this._modeName).functionName();
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Expression expression = this._select;
        if (expression != null) {
            this._type = expression.typeCheck(symbolTable);
            Type type = this._type;
            if ((type instanceof NodeType) || (type instanceof ReferenceType)) {
                this._select = new CastExpr(this._select, Type.NodeSet);
                this._type = Type.NodeSet;
            }
            Type type2 = this._type;
            if ((type2 instanceof NodeSetType) || (type2 instanceof ResultTreeType)) {
                typeCheckContents(symbolTable);
                return Type.Void;
            }
            throw new TypeCheckError(this);
        }
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0103  */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        boolean z;
        Expression expression;
        Stylesheet stylesheet = classGenerator.getStylesheet();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int localIndex = methodGenerator.getLocalIndex(Keywords.FUNC_CURRENT_STRING);
        Vector vector = new Vector();
        for (SyntaxTreeNode syntaxTreeNode : getContents()) {
            if (syntaxTreeNode instanceof Sort) {
                vector.addElement((Sort) syntaxTreeNode);
            }
        }
        if (stylesheet.hasLocalParams() || hasContents()) {
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.PUSH_PARAM_FRAME, "()V")));
            translateContents(classGenerator, methodGenerator);
        }
        instructionList.append(classGenerator.loadTranslet());
        Type type = this._type;
        if (type == null || !(type instanceof ResultTreeType)) {
            instructionList.append(methodGenerator.loadDOM());
            if (vector.size() > 0) {
                Sort.translateSortIterator(classGenerator, methodGenerator, this._select, vector);
                int addInterfaceMethodref = constantPool.addInterfaceMethodref(Constants.NODE_ITERATOR, Constants.SET_START_NODE, "(I)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
                instructionList.append(methodGenerator.loadCurrentNode());
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
                z = true;
                expression = this._select;
                if (expression != null && !z) {
                    expression.startIterator(classGenerator, methodGenerator);
                }
                String className = classGenerator.getStylesheet().getClassName();
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(className, this._functionName, classGenerator.getApplyTemplatesSig())));
                for (SyntaxTreeNode syntaxTreeNode2 : getContents()) {
                    if (syntaxTreeNode2 instanceof WithParam) {
                        ((WithParam) syntaxTreeNode2).releaseResultTree(classGenerator, methodGenerator);
                    }
                }
                if (!stylesheet.hasLocalParams() || hasContents()) {
                    instructionList.append(classGenerator.loadTranslet());
                    instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.POP_PARAM_FRAME, "()V")));
                }
                return;
            }
            Expression expression2 = this._select;
            if (expression2 == null) {
                Mode.compileGetChildren(classGenerator, methodGenerator, localIndex);
            } else {
                expression2.translate(classGenerator, methodGenerator);
            }
        } else {
            if (vector.size() > 0) {
                getParser().reportError(4, new ErrorMsg(ErrorMsg.RESULT_TREE_SORT_ERR, (SyntaxTreeNode) this));
            }
            this._select.translate(classGenerator, methodGenerator);
            this._type.translateTo(classGenerator, methodGenerator, Type.NodeSet);
        }
        z = false;
        expression = this._select;
        expression.startIterator(classGenerator, methodGenerator);
        String className2 = classGenerator.getStylesheet().getClassName();
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(className2, this._functionName, classGenerator.getApplyTemplatesSig())));
        while (r3.hasNext()) {
        }
        if (!stylesheet.hasLocalParams()) {
        }
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.POP_PARAM_FRAME, "()V")));
    }
}
