package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Iterator;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFGT;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
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

/* access modifiers changed from: package-private */
public final class ForEach extends Instruction {
    private Expression _select;
    private Type _type;

    ForEach() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("ForEach");
        int i2 = i + 4;
        indent(i2);
        Util.println("select " + this._select.toString());
        displayContents(i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        this._select = parser.parseExpression(this, Constants.ATTRNAME_SELECT, null);
        parseChildren(parser);
        if (this._select.isDummy()) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, Constants.ATTRNAME_SELECT);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._type = this._select.typeCheck(symbolTable);
        Type type = this._type;
        if ((type instanceof ReferenceType) || (type instanceof NodeType)) {
            this._select = new CastExpr(this._select, Type.NodeSet);
            typeCheckContents(symbolTable);
            return Type.Void;
        } else if ((type instanceof NodeSetType) || (type instanceof ResultTreeType)) {
            typeCheckContents(symbolTable);
            return Type.Void;
        } else {
            throw new TypeCheckError(this);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(methodGenerator.loadIterator());
        Vector vector = new Vector();
        Iterator<SyntaxTreeNode> elements = elements();
        while (elements.hasNext()) {
            SyntaxTreeNode next = elements.next();
            if (next instanceof Sort) {
                vector.addElement(next);
            }
        }
        Type type = this._type;
        if (type == null || !(type instanceof ResultTreeType)) {
            if (vector.size() > 0) {
                Sort.translateSortIterator(classGenerator, methodGenerator, this._select, vector);
            } else {
                this._select.translate(classGenerator, methodGenerator);
            }
            if (!(this._type instanceof ReferenceType)) {
                instructionList.append(methodGenerator.loadContextNode());
                instructionList.append(methodGenerator.setStartNode());
            }
        } else {
            instructionList.append(methodGenerator.loadDOM());
            if (vector.size() > 0) {
                getParser().reportError(4, new ErrorMsg(ErrorMsg.RESULT_TREE_SORT_ERR, (SyntaxTreeNode) this));
            }
            this._select.translate(classGenerator, methodGenerator);
            this._type.translateTo(classGenerator, methodGenerator, Type.NodeSet);
            instructionList.append(SWAP);
            instructionList.append(methodGenerator.storeDOM());
        }
        instructionList.append(methodGenerator.storeIterator());
        initializeVariables(classGenerator, methodGenerator);
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        InstructionHandle append2 = instructionList.append(NOP);
        translateContents(classGenerator, methodGenerator);
        append.setTarget(instructionList.append(methodGenerator.loadIterator()));
        instructionList.append(methodGenerator.nextNode());
        instructionList.append(DUP);
        instructionList.append(methodGenerator.storeCurrentNode());
        instructionList.append((BranchInstruction) new IFGT(append2));
        Type type2 = this._type;
        if (type2 != null && (type2 instanceof ResultTreeType)) {
            instructionList.append(methodGenerator.storeDOM());
        }
        instructionList.append(methodGenerator.storeIterator());
        instructionList.append(methodGenerator.storeCurrentNode());
    }

    public void initializeVariables(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        int elementCount = elementCount();
        for (int i = 0; i < elementCount; i++) {
            SyntaxTreeNode syntaxTreeNode = getContents().get(i);
            if (syntaxTreeNode instanceof Variable) {
                ((Variable) syntaxTreeNode).initialize(classGenerator, methodGenerator);
            }
        }
    }
}
