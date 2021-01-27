package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.ArrayList;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNONNULL;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MatchGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeCounterGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.RealType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

/* access modifiers changed from: package-private */
public final class Number extends Instruction implements Closure {
    private static final String[] ClassNames = {"ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SingleNodeCounter", "ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultipleNodeCounter", "ohos.com.sun.org.apache.xalan.internal.xsltc.dom.AnyNodeCounter"};
    private static final String[] FieldNames = {"___single_node_counter", "___multiple_node_counter", "___any_node_counter"};
    private static final int LEVEL_ANY = 2;
    private static final int LEVEL_MULTIPLE = 1;
    private static final int LEVEL_SINGLE = 0;
    private String _className = null;
    private ArrayList _closureVars = null;
    private Pattern _count = null;
    private AttributeValueTemplate _format = null;
    private boolean _formatNeeded = false;
    private Pattern _from = null;
    private AttributeValueTemplate _groupingSeparator = null;
    private AttributeValueTemplate _groupingSize = null;
    private AttributeValueTemplate _lang = null;
    private AttributeValueTemplate _letterValue = null;
    private int _level = 0;
    private Expression _value = null;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public Closure getParentClosure() {
        return null;
    }

    Number() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public boolean inInnerClass() {
        return this._className != null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public String getInnerClassName() {
        return this._className;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public void addVariable(VariableRefBase variableRefBase) {
        if (this._closureVars == null) {
            this._closureVars = new ArrayList();
        }
        if (!this._closureVars.contains(variableRefBase)) {
            this._closureVars.add(variableRefBase);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        int length = this._attributes.getLength();
        for (int i = 0; i < length; i++) {
            String qName = this._attributes.getQName(i);
            String value = this._attributes.getValue(i);
            if (qName.equals("value")) {
                this._value = parser.parseExpression(this, qName, null);
            } else if (qName.equals("count")) {
                this._count = parser.parsePattern(this, qName, null);
            } else if (qName.equals(Constants.ATTRNAME_FROM)) {
                this._from = parser.parsePattern(this, qName, null);
            } else if (qName.equals("level")) {
                if (value.equals(Constants.ATTRVAL_SINGLE)) {
                    this._level = 0;
                } else if (value.equals(Constants.ATTRVAL_MULTI)) {
                    this._level = 1;
                } else if (value.equals("any")) {
                    this._level = 2;
                }
            } else if (qName.equals("format")) {
                this._format = new AttributeValueTemplate(value, parser, this);
                this._formatNeeded = true;
            } else if (qName.equals("lang")) {
                this._lang = new AttributeValueTemplate(value, parser, this);
                this._formatNeeded = true;
            } else if (qName.equals(Constants.ATTRNAME_LETTERVALUE)) {
                this._letterValue = new AttributeValueTemplate(value, parser, this);
                this._formatNeeded = true;
            } else if (qName.equals(Constants.ATTRNAME_GROUPINGSEPARATOR)) {
                this._groupingSeparator = new AttributeValueTemplate(value, parser, this);
                this._formatNeeded = true;
            } else if (qName.equals(Constants.ATTRNAME_GROUPINGSIZE)) {
                this._groupingSize = new AttributeValueTemplate(value, parser, this);
                this._formatNeeded = true;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Expression expression = this._value;
        if (expression != null && !(expression.typeCheck(symbolTable) instanceof RealType)) {
            this._value = new CastExpr(this._value, Type.Real);
        }
        Pattern pattern = this._count;
        if (pattern != null) {
            pattern.typeCheck(symbolTable);
        }
        Pattern pattern2 = this._from;
        if (pattern2 != null) {
            pattern2.typeCheck(symbolTable);
        }
        AttributeValueTemplate attributeValueTemplate = this._format;
        if (attributeValueTemplate != null) {
            attributeValueTemplate.typeCheck(symbolTable);
        }
        AttributeValueTemplate attributeValueTemplate2 = this._lang;
        if (attributeValueTemplate2 != null) {
            attributeValueTemplate2.typeCheck(symbolTable);
        }
        AttributeValueTemplate attributeValueTemplate3 = this._letterValue;
        if (attributeValueTemplate3 != null) {
            attributeValueTemplate3.typeCheck(symbolTable);
        }
        AttributeValueTemplate attributeValueTemplate4 = this._groupingSeparator;
        if (attributeValueTemplate4 != null) {
            attributeValueTemplate4.typeCheck(symbolTable);
        }
        AttributeValueTemplate attributeValueTemplate5 = this._groupingSize;
        if (attributeValueTemplate5 != null) {
            attributeValueTemplate5.typeCheck(symbolTable);
        }
        return Type.Void;
    }

    public boolean hasValue() {
        return this._value != null;
    }

    public boolean isDefault() {
        return this._from == null && this._count == null;
    }

    private void compileDefault(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int[] numberFieldIndexes = getXSLTC().getNumberFieldIndexes();
        int i = this._level;
        if (numberFieldIndexes[i] == -1) {
            classGenerator.addField(new Field(2, constantPool.addUtf8(FieldNames[i]), constantPool.addUtf8(Constants.NODE_COUNTER_SIG), null, constantPool.getConstantPool()));
            numberFieldIndexes[this._level] = constantPool.addFieldref(classGenerator.getClassName(), FieldNames[this._level], Constants.NODE_COUNTER_SIG);
        }
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETFIELD(numberFieldIndexes[this._level]));
        BranchHandle append = instructionList.append((BranchInstruction) new IFNONNULL(null));
        int addMethodref = constantPool.addMethodref(ClassNames[this._level], "getDefaultNodeCounter", "(Lohos.com.sun.org.apache.xalan.internal.xsltc.Translet;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;)Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter;");
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadIterator());
        instructionList.append(new INVOKESTATIC(addMethodref));
        instructionList.append(DUP);
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(SWAP);
        instructionList.append(new PUTFIELD(numberFieldIndexes[this._level]));
        BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
        append.setTarget(instructionList.append(classGenerator.loadTranslet()));
        instructionList.append(new GETFIELD(numberFieldIndexes[this._level]));
        append2.setTarget(instructionList.append(NOP));
    }

    private void compileConstructor(ClassGenerator classGenerator) {
        InstructionList instructionList = new InstructionList();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        MethodGenerator methodGenerator = new MethodGenerator(1, ohos.com.sun.org.apache.bcel.internal.generic.Type.VOID, new ohos.com.sun.org.apache.bcel.internal.generic.Type[]{Util.getJCRefType(Constants.TRANSLET_INTF_SIG), Util.getJCRefType(Constants.DOM_INTF_SIG), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), ohos.com.sun.org.apache.bcel.internal.generic.Type.BOOLEAN}, new String[]{Constants.DOM_PNAME, "translet", Constants.ITERATOR_PNAME, "hasFrom"}, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, this._className, instructionList, constantPool);
        instructionList.append(ALOAD_0);
        instructionList.append(ALOAD_1);
        instructionList.append(ALOAD_2);
        instructionList.append(new ALOAD(3));
        instructionList.append(new ILOAD(4));
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(ClassNames[this._level], ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.Translet;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Z)V")));
        instructionList.append(RETURN);
        classGenerator.addMethod(methodGenerator);
    }

    private void compileLocals(NodeCounterGenerator nodeCounterGenerator, MatchGenerator matchGenerator, InstructionList instructionList) {
        ConstantPoolGen constantPool = nodeCounterGenerator.getConstantPool();
        LocalVariableGen addLocalVariable = matchGenerator.addLocalVariable(Constants.ITERATOR_PNAME, Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        int addFieldref = constantPool.addFieldref(Constants.NODE_COUNTER, "_iterator", "Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        instructionList.append(ALOAD_0);
        instructionList.append(new GETFIELD(addFieldref));
        addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
        matchGenerator.setIteratorIndex(addLocalVariable.getIndex());
        LocalVariableGen addLocalVariable2 = matchGenerator.addLocalVariable("translet", Util.getJCRefType("Lohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;"), null, null);
        int addFieldref2 = constantPool.addFieldref(Constants.NODE_COUNTER, "_translet", Constants.TRANSLET_INTF_SIG);
        instructionList.append(ALOAD_0);
        instructionList.append(new GETFIELD(addFieldref2));
        instructionList.append(new CHECKCAST(constantPool.addClass(Constants.TRANSLET_CLASS)));
        addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
        nodeCounterGenerator.setTransletIndex(addLocalVariable2.getIndex());
        LocalVariableGen addLocalVariable3 = matchGenerator.addLocalVariable(Constants.DOCUMENT_PNAME, Util.getJCRefType(Constants.DOM_INTF_SIG), null, null);
        int addFieldref3 = constantPool.addFieldref(this._className, "_document", Constants.DOM_INTF_SIG);
        instructionList.append(ALOAD_0);
        instructionList.append(new GETFIELD(addFieldref3));
        addLocalVariable3.setStart(instructionList.append(new ASTORE(addLocalVariable3.getIndex())));
        matchGenerator.setDomIndex(addLocalVariable3.getIndex());
    }

    private void compilePatterns(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._className = getXSLTC().getHelperClassName();
        NodeCounterGenerator nodeCounterGenerator = new NodeCounterGenerator(this._className, ClassNames[this._level], toString(), 33, null, classGenerator.getStylesheet());
        ConstantPoolGen constantPool = nodeCounterGenerator.getConstantPool();
        ArrayList arrayList = this._closureVars;
        int size = arrayList == null ? 0 : arrayList.size();
        for (int i = 0; i < size; i++) {
            VariableBase variable = ((VariableRefBase) this._closureVars.get(i)).getVariable();
            nodeCounterGenerator.addField(new Field(1, constantPool.addUtf8(variable.getEscapedName()), constantPool.addUtf8(variable.getType().toSignature()), null, constantPool.getConstantPool()));
        }
        compileConstructor(nodeCounterGenerator);
        if (this._from != null) {
            InstructionList instructionList = new InstructionList();
            MatchGenerator matchGenerator = new MatchGenerator(17, ohos.com.sun.org.apache.bcel.internal.generic.Type.BOOLEAN, new ohos.com.sun.org.apache.bcel.internal.generic.Type[]{ohos.com.sun.org.apache.bcel.internal.generic.Type.INT}, new String[]{"node"}, "matchesFrom", this._className, instructionList, constantPool);
            compileLocals(nodeCounterGenerator, matchGenerator, instructionList);
            instructionList.append(matchGenerator.loadContextNode());
            this._from.translate(nodeCounterGenerator, matchGenerator);
            this._from.synthesize(nodeCounterGenerator, matchGenerator);
            instructionList.append(IRETURN);
            nodeCounterGenerator.addMethod(matchGenerator);
        }
        if (this._count != null) {
            InstructionList instructionList2 = new InstructionList();
            MatchGenerator matchGenerator2 = new MatchGenerator(17, ohos.com.sun.org.apache.bcel.internal.generic.Type.BOOLEAN, new ohos.com.sun.org.apache.bcel.internal.generic.Type[]{ohos.com.sun.org.apache.bcel.internal.generic.Type.INT}, new String[]{"node"}, "matchesCount", this._className, instructionList2, constantPool);
            compileLocals(nodeCounterGenerator, matchGenerator2, instructionList2);
            instructionList2.append(matchGenerator2.loadContextNode());
            this._count.translate(nodeCounterGenerator, matchGenerator2);
            this._count.synthesize(nodeCounterGenerator, matchGenerator2);
            instructionList2.append(IRETURN);
            nodeCounterGenerator.addMethod(matchGenerator2);
        }
        getXSLTC().dumpClass(nodeCounterGenerator.getJavaClass());
        ConstantPoolGen constantPool2 = classGenerator.getConstantPool();
        InstructionList instructionList3 = methodGenerator.getInstructionList();
        int addMethodref = constantPool2.addMethodref(this._className, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.Translet;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Z)V");
        instructionList3.append(new NEW(constantPool2.addClass(this._className)));
        instructionList3.append(DUP);
        instructionList3.append(classGenerator.loadTranslet());
        instructionList3.append(methodGenerator.loadDOM());
        instructionList3.append(methodGenerator.loadIterator());
        instructionList3.append(this._from != null ? ICONST_1 : ICONST_0);
        instructionList3.append(new INVOKESPECIAL(addMethodref));
        for (int i2 = 0; i2 < size; i2++) {
            VariableBase variable2 = ((VariableRefBase) this._closureVars.get(i2)).getVariable();
            Type type = variable2.getType();
            instructionList3.append(DUP);
            instructionList3.append(variable2.loadInstruction());
            instructionList3.append(new PUTFIELD(constantPool2.addFieldref(this._className, variable2.getEscapedName(), type.toSignature())));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(classGenerator.loadTranslet());
        if (hasValue()) {
            compileDefault(classGenerator, methodGenerator);
            this._value.translate(classGenerator, methodGenerator);
            instructionList.append(new PUSH(constantPool, 0.5d));
            instructionList.append(DADD);
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.MATH_CLASS, Keywords.FUNC_FLOOR_STRING, "(D)D")));
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.NODE_COUNTER, "setValue", "(D)Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter;")));
        } else if (isDefault()) {
            compileDefault(classGenerator, methodGenerator);
        } else {
            compilePatterns(classGenerator, methodGenerator);
        }
        if (!hasValue()) {
            instructionList.append(methodGenerator.loadContextNode());
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.NODE_COUNTER, Constants.SET_START_NODE, "(I)Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter;")));
        }
        if (this._formatNeeded) {
            AttributeValueTemplate attributeValueTemplate = this._format;
            if (attributeValueTemplate != null) {
                attributeValueTemplate.translate(classGenerator, methodGenerator);
            } else {
                instructionList.append(new PUSH(constantPool, "1"));
            }
            AttributeValueTemplate attributeValueTemplate2 = this._lang;
            if (attributeValueTemplate2 != null) {
                attributeValueTemplate2.translate(classGenerator, methodGenerator);
            } else {
                instructionList.append(new PUSH(constantPool, "en"));
            }
            AttributeValueTemplate attributeValueTemplate3 = this._letterValue;
            if (attributeValueTemplate3 != null) {
                attributeValueTemplate3.translate(classGenerator, methodGenerator);
            } else {
                instructionList.append(new PUSH(constantPool, ""));
            }
            AttributeValueTemplate attributeValueTemplate4 = this._groupingSeparator;
            if (attributeValueTemplate4 != null) {
                attributeValueTemplate4.translate(classGenerator, methodGenerator);
            } else {
                instructionList.append(new PUSH(constantPool, ""));
            }
            AttributeValueTemplate attributeValueTemplate5 = this._groupingSize;
            if (attributeValueTemplate5 != null) {
                attributeValueTemplate5.translate(classGenerator, methodGenerator);
            } else {
                instructionList.append(new PUSH(constantPool, "0"));
            }
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.NODE_COUNTER, "getCounter", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")));
        } else {
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.NODE_COUNTER, "setDefaultFormatting", "()Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter;")));
            instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.NODE_COUNTER, "getCounter", "()Ljava/lang/String;")));
        }
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, "characters", Constants.CHARACTERSW_SIG)));
    }
}
