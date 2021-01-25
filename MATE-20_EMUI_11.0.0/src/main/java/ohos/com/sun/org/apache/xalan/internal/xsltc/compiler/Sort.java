package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.ArrayList;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ANEWARRAY;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.NOP;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.TABLESWITCH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.CompareGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSortRecordFactGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSortRecordGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

final class Sort extends Instruction implements Closure {
    private AttributeValue _caseOrder;
    private String _className = null;
    private ArrayList<VariableRefBase> _closureVars = null;
    private AttributeValue _dataType;
    private AttributeValue _lang;
    private boolean _needsSortRecordFactory = false;
    private AttributeValue _order;
    private Expression _select;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Closure
    public Closure getParentClosure() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
    }

    Sort() {
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
            this._closureVars = new ArrayList<>();
        }
        if (!this._closureVars.contains(variableRefBase)) {
            this._closureVars.add(variableRefBase);
            this._needsSortRecordFactory = true;
        }
    }

    private void setInnerClassName(String str) {
        this._className = str;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        SyntaxTreeNode parent = getParent();
        if ((parent instanceof ApplyTemplates) || (parent instanceof ForEach)) {
            this._select = parser.parseExpression(this, Constants.ATTRNAME_SELECT, "string(.)");
            String attribute = getAttribute(Constants.ATTRNAME_ORDER);
            if (attribute.length() == 0) {
                attribute = Constants.ATTRVAL_ORDER_ASCENDING;
            }
            this._order = AttributeValue.create(this, attribute, parser);
            String attribute2 = getAttribute(Constants.ATTRNAME_DATATYPE);
            if (attribute2.length() == 0) {
                try {
                    if (this._select.typeCheck(parser.getSymbolTable()) instanceof IntType) {
                        attribute2 = "number";
                    }
                } catch (TypeCheckError unused) {
                }
                attribute2 = "text";
            }
            this._dataType = AttributeValue.create(this, attribute2, parser);
            this._lang = AttributeValue.create(this, getAttribute("lang"), parser);
            this._caseOrder = AttributeValue.create(this, getAttribute(Constants.ATTRNAME_CASEORDER), parser);
            return;
        }
        reportError(this, parser, ErrorMsg.STRAY_SORT_ERR, null);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (!(this._select.typeCheck(symbolTable) instanceof StringType)) {
            this._select = new CastExpr(this._select, Type.String);
        }
        this._order.typeCheck(symbolTable);
        this._caseOrder.typeCheck(symbolTable);
        this._dataType.typeCheck(symbolTable);
        this._lang.typeCheck(symbolTable);
        return Type.Void;
    }

    public void translateSortType(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._dataType.translate(classGenerator, methodGenerator);
    }

    public void translateSortOrder(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._order.translate(classGenerator, methodGenerator);
    }

    public void translateCaseOrder(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._caseOrder.translate(classGenerator, methodGenerator);
    }

    public void translateLang(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._lang.translate(classGenerator, methodGenerator);
    }

    public void translateSelect(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        this._select.translate(classGenerator, methodGenerator);
    }

    public static void translateSortIterator(ClassGenerator classGenerator, MethodGenerator methodGenerator, Expression expression, Vector<Sort> vector) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.SORT_ITERATOR, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeSortRecordFactory;)V");
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("sort_tmp1", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), null, null);
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("sort_tmp2", Util.getJCRefType(Constants.NODE_SORT_FACTORY_SIG), null, null);
        if (expression == null) {
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getAxisIterator", "(I)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new PUSH(constantPool, 3));
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        } else {
            expression.translate(classGenerator, methodGenerator);
        }
        addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
        compileSortRecordFactory(vector, classGenerator, methodGenerator);
        addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
        instructionList.append(new NEW(constantPool.addClass(Constants.SORT_ITERATOR)));
        instructionList.append(DUP);
        addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
        addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
        instructionList.append(new INVOKESPECIAL(addMethodref));
    }

    public static void compileSortRecordFactory(Vector<Sort> vector, ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        int i;
        String compileSortRecord = compileSortRecord(vector, classGenerator, methodGenerator);
        int size = vector.size();
        boolean z = false;
        for (int i2 = 0; i2 < size; i2++) {
            z |= vector.elementAt(i2)._needsSortRecordFactory;
        }
        String compileSortRecordFactory = z ? compileSortRecordFactory(vector, classGenerator, methodGenerator, compileSortRecord) : Constants.NODE_SORT_FACTORY;
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("sort_order_tmp", Util.getJCRefType("[Ljava/lang/String;"), null, null);
        instructionList.append(new PUSH(constantPool, size));
        instructionList.append(new ANEWARRAY(constantPool.addClass("java.lang.String")));
        for (int i3 = 0; i3 < size; i3++) {
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, i3));
            vector.elementAt(i3).translateSortOrder(classGenerator, methodGenerator);
            instructionList.append(AASTORE);
        }
        addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("sort_type_tmp", Util.getJCRefType("[Ljava/lang/String;"), null, null);
        instructionList.append(new PUSH(constantPool, size));
        instructionList.append(new ANEWARRAY(constantPool.addClass("java.lang.String")));
        for (int i4 = 0; i4 < size; i4++) {
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, i4));
            vector.elementAt(i4).translateSortType(classGenerator, methodGenerator);
            instructionList.append(AASTORE);
        }
        addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
        LocalVariableGen addLocalVariable3 = methodGenerator.addLocalVariable("sort_lang_tmp", Util.getJCRefType("[Ljava/lang/String;"), null, null);
        instructionList.append(new PUSH(constantPool, size));
        instructionList.append(new ANEWARRAY(constantPool.addClass("java.lang.String")));
        for (int i5 = 0; i5 < size; i5++) {
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, i5));
            vector.elementAt(i5).translateLang(classGenerator, methodGenerator);
            instructionList.append(AASTORE);
        }
        addLocalVariable3.setStart(instructionList.append(new ASTORE(addLocalVariable3.getIndex())));
        LocalVariableGen addLocalVariable4 = methodGenerator.addLocalVariable("sort_case_order_tmp", Util.getJCRefType("[Ljava/lang/String;"), null, null);
        instructionList.append(new PUSH(constantPool, size));
        instructionList.append(new ANEWARRAY(constantPool.addClass("java.lang.String")));
        for (int i6 = 0; i6 < size; i6++) {
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, i6));
            vector.elementAt(i6).translateCaseOrder(classGenerator, methodGenerator);
            instructionList.append(AASTORE);
        }
        addLocalVariable4.setStart(instructionList.append(new ASTORE(addLocalVariable4.getIndex())));
        instructionList.append(new NEW(constantPool.addClass(compileSortRecordFactory)));
        instructionList.append(DUP);
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new PUSH(constantPool, compileSortRecord));
        instructionList.append(classGenerator.loadTranslet());
        addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
        addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
        addLocalVariable3.setEnd(instructionList.append(new ALOAD(addLocalVariable3.getIndex())));
        addLocalVariable4.setEnd(instructionList.append(new ALOAD(addLocalVariable4.getIndex())));
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(compileSortRecordFactory, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Ljava/lang/String;Lohos.com.sun.org.apache.xalan.internal.xsltc.Translet;[Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V")));
        ArrayList arrayList = new ArrayList();
        for (int i7 = 0; i7 < size; i7++) {
            Sort sort = vector.get(i7);
            ArrayList<VariableRefBase> arrayList2 = sort._closureVars;
            if (arrayList2 == null) {
                i = 0;
            } else {
                i = arrayList2.size();
            }
            for (int i8 = 0; i8 < i; i8++) {
                VariableRefBase variableRefBase = sort._closureVars.get(i8);
                if (!arrayList.contains(variableRefBase)) {
                    VariableBase variable = variableRefBase.getVariable();
                    instructionList.append(DUP);
                    instructionList.append(variable.loadInstruction());
                    instructionList.append(new PUTFIELD(constantPool.addFieldref(compileSortRecordFactory, variable.getEscapedName(), variable.getType().toSignature())));
                    arrayList.add(variableRefBase);
                }
            }
        }
    }

    public static String compileSortRecordFactory(Vector<Sort> vector, ClassGenerator classGenerator, MethodGenerator methodGenerator, String str) {
        int i;
        XSLTC xsltc = vector.firstElement().getXSLTC();
        String helperClassName = xsltc.getHelperClassName();
        int i2 = 0;
        NodeSortRecordFactGenerator nodeSortRecordFactGenerator = new NodeSortRecordFactGenerator(helperClassName, Constants.NODE_SORT_FACTORY, helperClassName + ".java", 49, new String[0], classGenerator.getStylesheet());
        ConstantPoolGen constantPool = nodeSortRecordFactGenerator.getConstantPool();
        int size = vector.size();
        ArrayList arrayList = new ArrayList();
        int i3 = 0;
        while (i3 < size) {
            Sort sort = vector.get(i3);
            ArrayList<VariableRefBase> arrayList2 = sort._closureVars;
            if (arrayList2 == null) {
                i = i2;
            } else {
                i = arrayList2.size();
            }
            for (int i4 = i2; i4 < i; i4++) {
                VariableRefBase variableRefBase = sort._closureVars.get(i4);
                if (!arrayList.contains(variableRefBase)) {
                    VariableBase variable = variableRefBase.getVariable();
                    nodeSortRecordFactGenerator.addField(new Field(1, constantPool.addUtf8(variable.getEscapedName()), constantPool.addUtf8(variable.getType().toSignature()), null, constantPool.getConstantPool()));
                    arrayList.add(variableRefBase);
                }
            }
            i3++;
            i2 = 0;
        }
        ohos.com.sun.org.apache.bcel.internal.generic.Type[] typeArr = {Util.getJCRefType(Constants.DOM_INTF_SIG), Util.getJCRefType(Constants.STRING_SIG), Util.getJCRefType(Constants.TRANSLET_INTF_SIG), Util.getJCRefType("[Ljava/lang/String;"), Util.getJCRefType("[Ljava/lang/String;"), Util.getJCRefType("[Ljava/lang/String;"), Util.getJCRefType("[Ljava/lang/String;")};
        String[] strArr = {Constants.DOCUMENT_PNAME, "className", "translet", Constants.ATTRNAME_ORDER, "type", "lang", "case_order"};
        InstructionList instructionList = new InstructionList();
        MethodGenerator methodGenerator2 = new MethodGenerator(1, ohos.com.sun.org.apache.bcel.internal.generic.Type.VOID, typeArr, strArr, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, helperClassName, instructionList, constantPool);
        instructionList.append(ALOAD_0);
        instructionList.append(ALOAD_1);
        instructionList.append(ALOAD_2);
        instructionList.append(new ALOAD(3));
        instructionList.append(new ALOAD(4));
        instructionList.append(new ALOAD(5));
        instructionList.append(new ALOAD(6));
        instructionList.append(new ALOAD(7));
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.NODE_SORT_FACTORY, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Ljava/lang/String;Lohos.com.sun.org.apache.xalan.internal.xsltc.Translet;[Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V")));
        instructionList.append(RETURN);
        InstructionList instructionList2 = new InstructionList();
        MethodGenerator methodGenerator3 = new MethodGenerator(1, Util.getJCRefType(Constants.NODE_SORT_RECORD_SIG), new ohos.com.sun.org.apache.bcel.internal.generic.Type[]{ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, ohos.com.sun.org.apache.bcel.internal.generic.Type.INT}, new String[]{"node", Keywords.FUNC_LAST_STRING}, "makeNodeSortRecord", helperClassName, instructionList2, constantPool);
        instructionList2.append(ALOAD_0);
        instructionList2.append(ILOAD_1);
        instructionList2.append(ILOAD_2);
        instructionList2.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.NODE_SORT_FACTORY, "makeNodeSortRecord", "(II)Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.NodeSortRecord;")));
        instructionList2.append(DUP);
        instructionList2.append(new CHECKCAST(constantPool.addClass(str)));
        int size2 = arrayList.size();
        for (int i5 = 0; i5 < size2; i5++) {
            VariableBase variable2 = ((VariableRefBase) arrayList.get(i5)).getVariable();
            Type type = variable2.getType();
            instructionList2.append(DUP);
            instructionList2.append(ALOAD_0);
            instructionList2.append(new GETFIELD(constantPool.addFieldref(helperClassName, variable2.getEscapedName(), type.toSignature())));
            instructionList2.append(new PUTFIELD(constantPool.addFieldref(str, variable2.getEscapedName(), type.toSignature())));
        }
        instructionList2.append(POP);
        instructionList2.append(ARETURN);
        methodGenerator2.setMaxLocals();
        methodGenerator2.setMaxStack();
        nodeSortRecordFactGenerator.addMethod(methodGenerator2);
        methodGenerator3.setMaxLocals();
        methodGenerator3.setMaxStack();
        nodeSortRecordFactGenerator.addMethod(methodGenerator3);
        xsltc.dumpClass(nodeSortRecordFactGenerator.getJavaClass());
        return helperClassName;
    }

    private static String compileSortRecord(Vector<Sort> vector, ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        int i;
        XSLTC xsltc = vector.firstElement().getXSLTC();
        String helperClassName = xsltc.getHelperClassName();
        int i2 = 0;
        NodeSortRecordGenerator nodeSortRecordGenerator = new NodeSortRecordGenerator(helperClassName, Constants.NODE_SORT_RECORD, "sort$0.java", 49, new String[0], classGenerator.getStylesheet());
        ConstantPoolGen constantPool = nodeSortRecordGenerator.getConstantPool();
        int size = vector.size();
        ArrayList arrayList = new ArrayList();
        int i3 = 0;
        while (i3 < size) {
            Sort sort = vector.get(i3);
            sort.setInnerClassName(helperClassName);
            ArrayList<VariableRefBase> arrayList2 = sort._closureVars;
            if (arrayList2 == null) {
                i = i2;
            } else {
                i = arrayList2.size();
            }
            for (int i4 = i2; i4 < i; i4++) {
                VariableRefBase variableRefBase = sort._closureVars.get(i4);
                if (!arrayList.contains(variableRefBase)) {
                    VariableBase variable = variableRefBase.getVariable();
                    nodeSortRecordGenerator.addField(new Field(1, constantPool.addUtf8(variable.getEscapedName()), constantPool.addUtf8(variable.getType().toSignature()), null, constantPool.getConstantPool()));
                    arrayList.add(variableRefBase);
                }
            }
            i3++;
            i2 = 0;
        }
        MethodGenerator compileInit = compileInit(nodeSortRecordGenerator, constantPool, helperClassName);
        MethodGenerator compileExtract = compileExtract(vector, nodeSortRecordGenerator, constantPool, helperClassName);
        nodeSortRecordGenerator.addMethod(compileInit);
        nodeSortRecordGenerator.addMethod(compileExtract);
        xsltc.dumpClass(nodeSortRecordGenerator.getJavaClass());
        return helperClassName;
    }

    private static MethodGenerator compileInit(NodeSortRecordGenerator nodeSortRecordGenerator, ConstantPoolGen constantPoolGen, String str) {
        InstructionList instructionList = new InstructionList();
        MethodGenerator methodGenerator = new MethodGenerator(1, ohos.com.sun.org.apache.bcel.internal.generic.Type.VOID, null, null, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, str, instructionList, constantPoolGen);
        instructionList.append(ALOAD_0);
        instructionList.append(new INVOKESPECIAL(constantPoolGen.addMethodref(Constants.NODE_SORT_RECORD, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "()V")));
        instructionList.append(RETURN);
        return methodGenerator;
    }

    private static MethodGenerator compileExtract(Vector<Sort> vector, NodeSortRecordGenerator nodeSortRecordGenerator, ConstantPoolGen constantPoolGen, String str) {
        InstructionHandle instructionHandle;
        InstructionList instructionList = new InstructionList();
        CompareGenerator compareGenerator = new CompareGenerator(17, ohos.com.sun.org.apache.bcel.internal.generic.Type.STRING, new ohos.com.sun.org.apache.bcel.internal.generic.Type[]{Util.getJCRefType(Constants.DOM_INTF_SIG), ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, Util.getJCRefType("Lohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;"), ohos.com.sun.org.apache.bcel.internal.generic.Type.INT}, new String[]{Constants.DOM_PNAME, Keywords.FUNC_CURRENT_STRING, "level", "translet", Keywords.FUNC_LAST_STRING}, "extractValueFromDOM", str, instructionList, constantPoolGen);
        int size = vector.size();
        int[] iArr = new int[size];
        InstructionHandle[] instructionHandleArr = new InstructionHandle[size];
        if (size > 1) {
            instructionList.append(new ILOAD(compareGenerator.getLocalIndex("level")));
            instructionHandle = instructionList.append(new NOP());
        } else {
            instructionHandle = null;
        }
        for (int i = 0; i < size; i++) {
            iArr[i] = i;
            instructionHandleArr[i] = instructionList.append(NOP);
            vector.elementAt(i).translateSelect(nodeSortRecordGenerator, compareGenerator);
            instructionList.append(ARETURN);
        }
        if (size > 1) {
            instructionList.insert(instructionHandle, (BranchInstruction) new TABLESWITCH(iArr, instructionHandleArr, instructionList.append(new PUSH(constantPoolGen, ""))));
            instructionList.append(ARETURN);
        }
        return compareGenerator;
    }
}
