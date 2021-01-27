package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.ICONST;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.dtm.Axis;

/* access modifiers changed from: package-private */
public final class Step extends RelativeLocationPath {
    private int _axis;
    private boolean _hadPredicates = false;
    private int _nodeType;
    private Vector _predicates;

    public Step(int i, int i2, Vector vector) {
        this._axis = i;
        this._nodeType = i2;
        this._predicates = vector;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        Vector vector = this._predicates;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                Predicate predicate = (Predicate) this._predicates.elementAt(i);
                predicate.setParser(parser);
                predicate.setParent(this);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.RelativeLocationPath
    public int getAxis() {
        return this._axis;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.RelativeLocationPath
    public void setAxis(int i) {
        this._axis = i;
    }

    public int getNodeType() {
        return this._nodeType;
    }

    public Vector getPredicates() {
        return this._predicates;
    }

    public void addPredicates(Vector vector) {
        Vector vector2 = this._predicates;
        if (vector2 == null) {
            this._predicates = vector;
        } else {
            vector2.addAll(vector);
        }
    }

    private boolean hasParentPattern() {
        SyntaxTreeNode parent = getParent();
        return (parent instanceof ParentPattern) || (parent instanceof ParentLocationPath) || (parent instanceof UnionPathExpr) || (parent instanceof FilterParentPath);
    }

    private boolean hasParentLocationPath() {
        return getParent() instanceof ParentLocationPath;
    }

    private boolean hasPredicates() {
        Vector vector = this._predicates;
        return vector != null && vector.size() > 0;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:7:? */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0, types: [ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Step] */
    /* JADX WARN: Type inference failed for: r1v1, types: [ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode] */
    /* JADX WARN: Type inference failed for: r1v3, types: [ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode] */
    private boolean isPredicate() {
        while (this != 0) {
            this = this.getParent();
            if (this instanceof Predicate) {
                return true;
            }
        }
        return false;
    }

    public boolean isAbbreviatedDot() {
        return this._nodeType == -1 && this._axis == 13;
    }

    public boolean isAbbreviatedDDot() {
        return this._nodeType == -1 && this._axis == 10;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._hadPredicates = hasPredicates();
        if (isAbbreviatedDot()) {
            this._type = (hasParentPattern() || hasPredicates() || hasParentLocationPath()) ? Type.NodeSet : Type.Node;
        } else {
            this._type = Type.NodeSet;
        }
        Vector vector = this._predicates;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                ((Expression) this._predicates.elementAt(i)).typeCheck(symbolTable);
            }
        }
        return this._type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        translateStep(classGenerator, methodGenerator, hasPredicates() ? this._predicates.size() - 1 : -1);
    }

    private void translateStep(ClassGenerator classGenerator, MethodGenerator methodGenerator, int i) {
        int i2;
        String str;
        int i3;
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (i >= 0) {
            translatePredicates(classGenerator, methodGenerator, i);
            return;
        }
        String str2 = null;
        XSLTC xsltc = getParser().getXSLTC();
        if (this._nodeType >= 14) {
            str2 = (String) xsltc.getNamesIndex().elementAt(this._nodeType - 14);
            i2 = str2.lastIndexOf(42);
        } else {
            i2 = 0;
        }
        if (this._axis != 2 || (i3 = this._nodeType) == 2 || i3 == -1 || hasParentPattern() || i2 != 0) {
            SyntaxTreeNode parent = getParent();
            if (!isAbbreviatedDot()) {
                if ((parent instanceof ParentLocationPath) && (parent.getParent() instanceof ParentLocationPath) && this._nodeType == 1 && !this._hadPredicates) {
                    this._nodeType = -1;
                }
                int i4 = this._nodeType;
                if (i4 != -1) {
                    if (i4 != 1) {
                        if (i4 == 2) {
                            this._axis = 2;
                        } else if (i2 > 1) {
                            if (this._axis == 2) {
                                str = str2.substring(0, i2 - 2);
                            } else {
                                str = str2.substring(0, i2 - 1);
                            }
                            int registerNamespace = xsltc.registerNamespace(str);
                            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNamespaceAxisIterator", "(II)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
                            instructionList.append(methodGenerator.loadDOM());
                            instructionList.append(new PUSH(constantPool, this._axis));
                            instructionList.append(new PUSH(constantPool, registerNamespace));
                            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 3));
                            return;
                        }
                    }
                    int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getTypedAxisIterator", "(II)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
                    instructionList.append(methodGenerator.loadDOM());
                    instructionList.append(new PUSH(constantPool, this._axis));
                    instructionList.append(new PUSH(constantPool, this._nodeType));
                    instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 3));
                    return;
                }
                int addInterfaceMethodref3 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getAxisIterator", "(I)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
                instructionList.append(methodGenerator.loadDOM());
                instructionList.append(new PUSH(constantPool, this._axis));
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref3, 2));
            } else if (this._type == Type.Node) {
                instructionList.append(methodGenerator.loadContextNode());
            } else if (parent instanceof ParentLocationPath) {
                int addMethodref = constantPool.addMethodref(Constants.SINGLETON_ITERATOR, Constants.CONSTRUCTOR_NAME, "(I)V");
                instructionList.append(new NEW(constantPool.addClass(Constants.SINGLETON_ITERATOR)));
                instructionList.append(DUP);
                instructionList.append(methodGenerator.loadContextNode());
                instructionList.append(new INVOKESPECIAL(addMethodref));
            } else {
                int addInterfaceMethodref4 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getAxisIterator", "(I)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
                instructionList.append(methodGenerator.loadDOM());
                instructionList.append(new PUSH(constantPool, this._axis));
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref4, 2));
            }
        } else {
            int addInterfaceMethodref5 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getTypedAxisIterator", "(II)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new PUSH(constantPool, 2));
            instructionList.append(new PUSH(constantPool, this._nodeType));
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref5, 3));
        }
    }

    public void translatePredicates(ClassGenerator classGenerator, MethodGenerator methodGenerator, int i) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (i < 0) {
            translateStep(classGenerator, methodGenerator, i);
            return;
        }
        int i2 = i - 1;
        Predicate predicate = (Predicate) this._predicates.get(i);
        if (predicate.isNodeValueTest()) {
            Step step = predicate.getStep();
            instructionList.append(methodGenerator.loadDOM());
            if (step.isAbbreviatedDot()) {
                translateStep(classGenerator, methodGenerator, i2);
                instructionList.append(new ICONST(0));
            } else {
                ParentLocationPath parentLocationPath = new ParentLocationPath(this, step);
                step._parent = parentLocationPath;
                this._parent = parentLocationPath;
                try {
                    parentLocationPath.typeCheck(getParser().getSymbolTable());
                } catch (TypeCheckError unused) {
                }
                translateStep(classGenerator, methodGenerator, i2);
                parentLocationPath.translateStep(classGenerator, methodGenerator);
                instructionList.append(new ICONST(1));
            }
            predicate.translate(classGenerator, methodGenerator);
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_NODE_VALUE_ITERATOR, Constants.GET_NODE_VALUE_ITERATOR_SIG), 5));
        } else if (predicate.isNthDescendant()) {
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new PUSH(constantPool, predicate.getPosType()));
            predicate.translate(classGenerator, methodGenerator);
            instructionList.append(new ICONST(0));
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNthDescendant", "(IIZ)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), 4));
        } else if (predicate.isNthPositionFilter()) {
            int addMethodref = constantPool.addMethodref(Constants.NTH_ITERATOR_CLASS, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;I)V");
            translatePredicates(classGenerator, methodGenerator, i2);
            LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("step_tmp1", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
            addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
            predicate.translate(classGenerator, methodGenerator);
            LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("step_tmp2", Util.getJCRefType("I"), null, null);
            addLocalVariable2.setStart(instructionList.append(new ISTORE(addLocalVariable2.getIndex())));
            instructionList.append(new NEW(constantPool.addClass(Constants.NTH_ITERATOR_CLASS)));
            instructionList.append(DUP);
            addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
            addLocalVariable2.setEnd(instructionList.append(new ILOAD(addLocalVariable2.getIndex())));
            instructionList.append(new INVOKESPECIAL(addMethodref));
        } else {
            int addMethodref2 = constantPool.addMethodref(Constants.CURRENT_NODE_LIST_ITERATOR, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.CurrentNodeListFilter;ILohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;)V");
            translatePredicates(classGenerator, methodGenerator, i2);
            LocalVariableGen addLocalVariable3 = methodGenerator.addLocalVariable("step_tmp1", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
            addLocalVariable3.setStart(instructionList.append(new ASTORE(addLocalVariable3.getIndex())));
            predicate.translateFilter(classGenerator, methodGenerator);
            LocalVariableGen addLocalVariable4 = methodGenerator.addLocalVariable("step_tmp2", Util.getJCRefType(Constants.CURRENT_NODE_LIST_FILTER_SIG), null, null);
            addLocalVariable4.setStart(instructionList.append(new ASTORE(addLocalVariable4.getIndex())));
            instructionList.append(new NEW(constantPool.addClass(Constants.CURRENT_NODE_LIST_ITERATOR)));
            instructionList.append(DUP);
            addLocalVariable3.setEnd(instructionList.append(new ALOAD(addLocalVariable3.getIndex())));
            addLocalVariable4.setEnd(instructionList.append(new ALOAD(addLocalVariable4.getIndex())));
            instructionList.append(methodGenerator.loadCurrentNode());
            instructionList.append(classGenerator.loadTranslet());
            if (classGenerator.isExternal()) {
                instructionList.append(new CHECKCAST(constantPool.addClass(classGenerator.getClassName())));
            }
            instructionList.append(new INVOKESPECIAL(addMethodref2));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("step(\"");
        stringBuffer.append(Axis.getNames(this._axis));
        stringBuffer.append("\", ");
        stringBuffer.append(this._nodeType);
        Vector vector = this._predicates;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                stringBuffer.append(", ");
                stringBuffer.append(((Predicate) this._predicates.elementAt(i)).toString());
            }
        }
        stringBuffer.append(')');
        return stringBuffer.toString();
    }
}
