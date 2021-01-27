package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.DUP;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO_W;
import ohos.com.sun.org.apache.bcel.internal.generic.IFLT;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.SWITCH;
import ohos.com.sun.org.apache.bcel.internal.generic.TargetLostException;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.bcel.internal.util.InstructionFinder;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NamedMethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

/* access modifiers changed from: package-private */
public final class Mode implements Constants {
    private Vector _attribNodeGroup = null;
    private TestSeq _attribNodeTestSeq = null;
    private Vector _childNodeGroup = null;
    private TestSeq _childNodeTestSeq = null;
    private int _currentIndex;
    private Vector _idxGroup = null;
    private TestSeq _idxTestSeq = null;
    private Map<Integer, Integer> _importLevels = null;
    private Map<String, Key> _keys = null;
    private final String _methodName;
    private final QName _name;
    private Map<Template, Mode> _namedTemplates = new HashMap();
    private Map<Template, Object> _neededTemplates = new HashMap();
    private Vector[] _patternGroups;
    private LocationPathPattern _rootPattern = null;
    private final Stylesheet _stylesheet;
    private Map<Template, InstructionHandle> _templateIHs = new HashMap();
    private Map<Template, InstructionList> _templateILs = new HashMap();
    private Vector _templates;
    private TestSeq[] _testSeq;

    public Mode(QName qName, Stylesheet stylesheet, String str) {
        this._name = qName;
        this._stylesheet = stylesheet;
        this._methodName = Constants.APPLY_TEMPLATES + str;
        this._templates = new Vector();
        this._patternGroups = new Vector[32];
    }

    public String functionName() {
        return this._methodName;
    }

    public String functionName(int i, int i2) {
        if (this._importLevels == null) {
            this._importLevels = new HashMap();
        }
        this._importLevels.put(Integer.valueOf(i2), Integer.valueOf(i));
        return this._methodName + '_' + i2;
    }

    private String getClassName() {
        return this._stylesheet.getClassName();
    }

    public Stylesheet getStylesheet() {
        return this._stylesheet;
    }

    public void addTemplate(Template template) {
        this._templates.addElement(template);
    }

    private Vector quicksort(Vector vector, int i, int i2) {
        if (i < i2) {
            int partition = partition(vector, i, i2);
            quicksort(vector, i, partition);
            quicksort(vector, partition + 1, i2);
        }
        return vector;
    }

    private int partition(Vector vector, int i, int i2) {
        Template template = (Template) vector.elementAt(i);
        int i3 = i - 1;
        int i4 = i2 + 1;
        while (true) {
            i4--;
            if (template.compareTo((Template) vector.elementAt(i4)) <= 0) {
                do {
                    i3++;
                } while (template.compareTo((Template) vector.elementAt(i3)) < 0);
                if (i3 >= i4) {
                    return i4;
                }
                vector.set(i4, vector.set(i3, vector.elementAt(i4)));
            }
        }
    }

    public void processPatterns(Map<String, Key> map) {
        this._keys = map;
        Vector vector = this._templates;
        this._templates = quicksort(vector, 0, vector.size() - 1);
        Enumeration elements = this._templates.elements();
        while (elements.hasMoreElements()) {
            Template template = (Template) elements.nextElement();
            if (template.isNamed() && !template.disabled()) {
                this._namedTemplates.put(template, this);
            }
            Pattern pattern = template.getPattern();
            if (pattern != null) {
                flattenAlternative(pattern, template, map);
            }
        }
        prepareTestSequences();
    }

    private void flattenAlternative(Pattern pattern, Template template, Map<String, Key> map) {
        if (pattern instanceof IdKeyPattern) {
            ((IdKeyPattern) pattern).setTemplate(template);
            if (this._idxGroup == null) {
                this._idxGroup = new Vector();
            }
            this._idxGroup.add(pattern);
        } else if (pattern instanceof AlternativePattern) {
            AlternativePattern alternativePattern = (AlternativePattern) pattern;
            flattenAlternative(alternativePattern.getLeft(), template, map);
            flattenAlternative(alternativePattern.getRight(), template, map);
        } else if (pattern instanceof LocationPathPattern) {
            LocationPathPattern locationPathPattern = (LocationPathPattern) pattern;
            locationPathPattern.setTemplate(template);
            addPatternToGroup(locationPathPattern);
        }
    }

    private void addPatternToGroup(LocationPathPattern locationPathPattern) {
        if (locationPathPattern instanceof IdKeyPattern) {
            addPattern(-1, locationPathPattern);
            return;
        }
        StepPattern kernelPattern = locationPathPattern.getKernelPattern();
        if (kernelPattern != null) {
            addPattern(kernelPattern.getNodeType(), locationPathPattern);
            return;
        }
        LocationPathPattern locationPathPattern2 = this._rootPattern;
        if (locationPathPattern2 == null || locationPathPattern.noSmallerThan(locationPathPattern2)) {
            this._rootPattern = locationPathPattern;
        }
    }

    private void addPattern(int i, LocationPathPattern locationPathPattern) {
        Vector vector;
        Vector[] vectorArr = this._patternGroups;
        int length = vectorArr.length;
        boolean z = false;
        if (i >= length) {
            Vector[] vectorArr2 = new Vector[(i * 2)];
            System.arraycopy(vectorArr, 0, vectorArr2, 0, length);
            this._patternGroups = vectorArr2;
        }
        if (i != -1) {
            Vector[] vectorArr3 = this._patternGroups;
            if (vectorArr3[i] == null) {
                Vector vector2 = new Vector(2);
                vectorArr3[i] = vector2;
                vector = vector2;
            } else {
                vector = vectorArr3[i];
            }
        } else if (locationPathPattern.getAxis() == 2) {
            vector = this._attribNodeGroup;
            if (vector == null) {
                vector = new Vector(2);
                this._attribNodeGroup = vector;
            }
        } else {
            vector = this._childNodeGroup;
            if (vector == null) {
                vector = new Vector(2);
                this._childNodeGroup = vector;
            }
        }
        if (vector.size() == 0) {
            vector.addElement(locationPathPattern);
            return;
        }
        int i2 = 0;
        while (true) {
            if (i2 >= vector.size()) {
                break;
            } else if (locationPathPattern.noSmallerThan((LocationPathPattern) vector.elementAt(i2))) {
                vector.insertElementAt(locationPathPattern, i2);
                z = true;
                break;
            } else {
                i2++;
            }
        }
        if (!z) {
            vector.addElement(locationPathPattern);
        }
    }

    private void completeTestSequences(int i, Vector vector) {
        if (vector != null) {
            Vector[] vectorArr = this._patternGroups;
            if (vectorArr[i] == null) {
                vectorArr[i] = vector;
                return;
            }
            int size = vector.size();
            for (int i2 = 0; i2 < size; i2++) {
                addPattern(i, (LocationPathPattern) vector.elementAt(i2));
            }
        }
    }

    private void prepareTestSequences() {
        Vector[] vectorArr = this._patternGroups;
        Vector vector = vectorArr[1];
        Vector vector2 = vectorArr[2];
        completeTestSequences(3, this._childNodeGroup);
        completeTestSequences(1, this._childNodeGroup);
        completeTestSequences(7, this._childNodeGroup);
        completeTestSequences(8, this._childNodeGroup);
        completeTestSequences(2, this._attribNodeGroup);
        Vector namesIndex = this._stylesheet.getXSLTC().getNamesIndex();
        if (!(vector == null && vector2 == null && this._childNodeGroup == null && this._attribNodeGroup == null)) {
            int length = this._patternGroups.length;
            for (int i = 14; i < length; i++) {
                if (this._patternGroups[i] != null) {
                    if (isAttributeName((String) namesIndex.elementAt(i - 14))) {
                        completeTestSequences(i, vector2);
                        completeTestSequences(i, this._attribNodeGroup);
                    } else {
                        completeTestSequences(i, vector);
                        completeTestSequences(i, this._childNodeGroup);
                    }
                }
            }
        }
        this._testSeq = new TestSeq[(namesIndex.size() + 14)];
        int length2 = this._patternGroups.length;
        for (int i2 = 0; i2 < length2; i2++) {
            Vector vector3 = this._patternGroups[i2];
            if (vector3 != null) {
                TestSeq testSeq = new TestSeq(vector3, i2, this);
                testSeq.reduce();
                this._testSeq[i2] = testSeq;
                testSeq.findTemplates(this._neededTemplates);
            }
        }
        Vector vector4 = this._childNodeGroup;
        if (vector4 != null && vector4.size() > 0) {
            this._childNodeTestSeq = new TestSeq(this._childNodeGroup, -1, this);
            this._childNodeTestSeq.reduce();
            this._childNodeTestSeq.findTemplates(this._neededTemplates);
        }
        Vector vector5 = this._idxGroup;
        if (vector5 != null && vector5.size() > 0) {
            this._idxTestSeq = new TestSeq(this._idxGroup, this);
            this._idxTestSeq.reduce();
            this._idxTestSeq.findTemplates(this._neededTemplates);
        }
        LocationPathPattern locationPathPattern = this._rootPattern;
        if (locationPathPattern != null) {
            this._neededTemplates.put(locationPathPattern.getTemplate(), this);
        }
    }

    private void compileNamedTemplate(Template template, ClassGenerator classGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = new InstructionList();
        String escape = Util.escape(template.getName().toString());
        int size = (template.isSimpleNamedTemplate() ? template.getParameters().size() : 0) + 4;
        Type[] typeArr = new Type[size];
        String[] strArr = new String[size];
        typeArr[0] = Util.getJCRefType(Constants.DOM_INTF_SIG);
        typeArr[1] = Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        typeArr[2] = Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;");
        typeArr[3] = Type.INT;
        strArr[0] = Constants.DOCUMENT_PNAME;
        strArr[1] = Constants.ITERATOR_PNAME;
        strArr[2] = Constants.TRANSLET_OUTPUT_PNAME;
        strArr[3] = "node";
        for (int i = 4; i < size; i++) {
            typeArr[i] = Util.getJCRefType(Constants.OBJECT_SIG);
            strArr[i] = Constants.ELEMNAME_PARAMVARIABLE_STRING + String.valueOf(i - 4);
        }
        NamedMethodGenerator namedMethodGenerator = new NamedMethodGenerator(1, Type.VOID, typeArr, strArr, escape, getClassName(), instructionList, constantPool);
        instructionList.append(template.compile(classGenerator, namedMethodGenerator));
        instructionList.append(RETURN);
        classGenerator.addMethod(namedMethodGenerator);
    }

    private void compileTemplates(ClassGenerator classGenerator, MethodGenerator methodGenerator, InstructionHandle instructionHandle) {
        for (Template template : this._namedTemplates.keySet()) {
            compileNamedTemplate(template, classGenerator);
        }
        for (Template template2 : this._neededTemplates.keySet()) {
            if (template2.hasContents()) {
                InstructionList compile = template2.compile(classGenerator, methodGenerator);
                compile.append((BranchInstruction) new GOTO_W(instructionHandle));
                this._templateILs.put(template2, compile);
                this._templateIHs.put(template2, compile.getStart());
            } else {
                this._templateIHs.put(template2, instructionHandle);
            }
        }
    }

    private void appendTemplateCode(InstructionList instructionList) {
        for (Template template : this._neededTemplates.keySet()) {
            InstructionList instructionList2 = this._templateILs.get(template);
            if (instructionList2 != null) {
                instructionList.append(instructionList2);
            }
        }
    }

    private void appendTestSequences(InstructionList instructionList) {
        InstructionList instructionList2;
        int length = this._testSeq.length;
        for (int i = 0; i < length; i++) {
            TestSeq testSeq = this._testSeq[i];
            if (!(testSeq == null || (instructionList2 = testSeq.getInstructionList()) == null)) {
                instructionList.append(instructionList2);
            }
        }
    }

    public static void compileGetChildren(ClassGenerator classGenerator, MethodGenerator methodGenerator, int i) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_CHILDREN, "(I)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new ILOAD(i));
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
    }

    private InstructionList compileDefaultRecursion(ClassGenerator classGenerator, MethodGenerator methodGenerator, InstructionHandle instructionHandle) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = new InstructionList();
        String applyTemplatesSig = classGenerator.getApplyTemplatesSig();
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_CHILDREN, "(I)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        int addMethodref = constantPool.addMethodref(getClassName(), functionName(), applyTemplatesSig);
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new ILOAD(this._currentIndex));
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new INVOKEVIRTUAL(addMethodref));
        instructionList.append((BranchInstruction) new GOTO_W(instructionHandle));
        return instructionList;
    }

    private InstructionList compileDefaultText(ClassGenerator classGenerator, MethodGenerator methodGenerator, InstructionHandle instructionHandle) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = new InstructionList();
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "characters", Constants.CHARACTERS_SIG);
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new ILOAD(this._currentIndex));
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 3));
        instructionList.append((BranchInstruction) new GOTO_W(instructionHandle));
        return instructionList;
    }

    private InstructionList compileNamespaces(ClassGenerator classGenerator, MethodGenerator methodGenerator, boolean[] zArr, boolean[] zArr2, boolean z, InstructionHandle instructionHandle) {
        XSLTC xsltc = classGenerator.getParser().getXSLTC();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        Vector namespaceIndex = xsltc.getNamespaceIndex();
        Vector namesIndex = xsltc.getNamesIndex();
        int size = namespaceIndex.size() + 1;
        int size2 = namesIndex.size();
        InstructionList instructionList = new InstructionList();
        int[] iArr = new int[size];
        InstructionHandle[] instructionHandleArr = new InstructionHandle[iArr.length];
        if (size <= 0) {
            return null;
        }
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            instructionHandleArr[i2] = instructionHandle;
            iArr[i2] = i2;
        }
        int i3 = 14;
        boolean z2 = false;
        while (i3 < size2 + 14) {
            if (zArr[i3]) {
                if (zArr2[i3] == z) {
                    String str = (String) namesIndex.elementAt(i3 - 14);
                    int registerNamespace = xsltc.registerNamespace(str.substring(i, str.lastIndexOf(58)));
                    TestSeq[] testSeqArr = this._testSeq;
                    if (i3 < testSeqArr.length && testSeqArr[i3] != null) {
                        instructionHandleArr[registerNamespace] = testSeqArr[i3].compile(classGenerator, methodGenerator, instructionHandle);
                        z2 = true;
                    }
                }
            }
            i3++;
            i = 0;
        }
        if (!z2) {
            return null;
        }
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNamespaceType", Constants.GET_PARENT_SIG);
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new ILOAD(this._currentIndex));
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        instructionList.append(new SWITCH(iArr, instructionHandleArr, instructionHandle));
        return instructionList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:113:0x0314  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x0350  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x0355  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0376  */
    /* JADX WARNING: Removed duplicated region for block: B:138:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x01fe  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0204  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x021e  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x022a  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0265  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x026e  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0278  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x028b  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x02a1  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x02a4  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x02ac  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x02b5  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x02bb  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x02be  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x02c7  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x02e9  */
    public void compileApplyTemplates(ClassGenerator classGenerator) {
        boolean[] zArr;
        InstructionHandle instructionHandle;
        InstructionHandle instructionHandle2;
        InstructionList instructionList;
        InstructionHandle instructionHandle3;
        InstructionHandle instructionHandle4;
        InstructionHandle instructionHandle5;
        InstructionList compileNamespaces;
        InstructionList compileNamespaces2;
        InstructionHandle[] instructionHandleArr;
        int i;
        LocationPathPattern locationPathPattern;
        TestSeq[] testSeqArr;
        TestSeq[] testSeqArr2;
        TestSeq[] testSeqArr3;
        int i2;
        Map<Integer, Integer> map;
        MethodGenerator methodGenerator;
        double d;
        int i3;
        XSLTC xsltc = classGenerator.getParser().getXSLTC();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        Vector namesIndex = xsltc.getNamesIndex();
        Type[] typeArr = {Util.getJCRefType(Constants.DOM_INTF_SIG), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;")};
        String[] strArr = {Constants.DOCUMENT_PNAME, Constants.ITERATOR_PNAME, Constants.TRANSLET_OUTPUT_PNAME};
        InstructionList instructionList2 = new InstructionList();
        MethodGenerator methodGenerator2 = new MethodGenerator(17, Type.VOID, typeArr, strArr, functionName(), getClassName(), instructionList2, classGenerator.getConstantPool());
        methodGenerator2.addException("ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException");
        instructionList2.append(NOP);
        LocalVariableGen addLocalVariable2 = methodGenerator2.addLocalVariable2(Keywords.FUNC_CURRENT_STRING, Type.INT, null);
        this._currentIndex = addLocalVariable2.getIndex();
        InstructionList instructionList3 = new InstructionList();
        instructionList3.append(NOP);
        InstructionList instructionList4 = new InstructionList();
        instructionList4.append(methodGenerator2.loadIterator());
        instructionList4.append(methodGenerator2.nextNode());
        instructionList4.append(DUP);
        instructionList4.append(new ISTORE(this._currentIndex));
        BranchHandle append = instructionList4.append((BranchInstruction) new IFLT(null));
        BranchHandle append2 = instructionList4.append((BranchInstruction) new GOTO_W(null));
        append.setTarget(instructionList4.append(RETURN));
        InstructionHandle start = instructionList4.getStart();
        addLocalVariable2.setStart(instructionList2.append((BranchInstruction) new GOTO_W(start)));
        addLocalVariable2.setEnd(append2);
        InstructionList compileDefaultRecursion = compileDefaultRecursion(classGenerator, methodGenerator2, start);
        InstructionHandle start2 = compileDefaultRecursion.getStart();
        InstructionList compileDefaultText = compileDefaultText(classGenerator, methodGenerator2, start);
        InstructionHandle start3 = compileDefaultText.getStart();
        int[] iArr = new int[(namesIndex.size() + 14)];
        for (int i4 = 0; i4 < iArr.length; i4++) {
            iArr[i4] = i4;
        }
        boolean[] zArr2 = new boolean[iArr.length];
        boolean[] zArr3 = new boolean[iArr.length];
        for (int i5 = 0; i5 < namesIndex.size(); i5++) {
            String str = (String) namesIndex.elementAt(i5);
            int i6 = i5 + 14;
            zArr2[i6] = isAttributeName(str);
            zArr3[i6] = isNamespaceName(str);
        }
        compileTemplates(classGenerator, methodGenerator2, start);
        TestSeq testSeq = this._testSeq[1];
        InstructionHandle compile = testSeq != null ? testSeq.compile(classGenerator, methodGenerator2, start2) : start2;
        TestSeq testSeq2 = this._testSeq[2];
        if (testSeq2 != null) {
            instructionHandle = testSeq2.compile(classGenerator, methodGenerator2, start3);
            zArr = zArr3;
        } else {
            zArr = zArr3;
            instructionHandle = start3;
        }
        TestSeq testSeq3 = this._idxTestSeq;
        if (testSeq3 != null) {
            instructionHandle2 = start3;
            append2.setTarget(testSeq3.compile(classGenerator, methodGenerator2, instructionList3.getStart()));
            instructionList = this._idxTestSeq.getInstructionList();
        } else {
            instructionHandle2 = start3;
            append2.setTarget(instructionList3.getStart());
            instructionList = null;
        }
        TestSeq testSeq4 = this._childNodeTestSeq;
        if (testSeq4 != null) {
            double priority = testSeq4.getPriority();
            instructionHandle4 = start2;
            int position = this._childNodeTestSeq.getPosition();
            int i7 = Integer.MIN_VALUE;
            double d2 = -1.7976931348623157E308d;
            if (testSeq != null) {
                d = testSeq.getPriority();
                i3 = testSeq.getPosition();
            } else {
                i3 = Integer.MIN_VALUE;
                d = -1.7976931348623157E308d;
            }
            if (d == Double.NaN || d < priority || (d == priority && i3 < position)) {
                compile = this._childNodeTestSeq.compile(classGenerator, methodGenerator2, start);
            }
            TestSeq testSeq5 = this._testSeq[3];
            if (testSeq5 != null) {
                d2 = testSeq5.getPriority();
                i7 = testSeq5.getPosition();
            }
            if (d2 == Double.NaN || d2 < priority || (d2 == priority && i7 < position)) {
                InstructionHandle compile2 = this._childNodeTestSeq.compile(classGenerator, methodGenerator2, start);
                this._testSeq[3] = this._childNodeTestSeq;
                instructionHandle3 = compile;
                instructionHandle5 = compile2;
                MethodGenerator methodGenerator3 = methodGenerator2;
                InstructionHandle instructionHandle6 = instructionHandle5;
                compileNamespaces = compileNamespaces(classGenerator, methodGenerator2, zArr, zArr2, false, instructionHandle3);
                InstructionHandle start4 = compileNamespaces == null ? compileNamespaces.getStart() : instructionHandle3;
                compileNamespaces2 = compileNamespaces(classGenerator, methodGenerator3, zArr, zArr2, true, instructionHandle);
                if (compileNamespaces2 != null) {
                    instructionHandle = compileNamespaces2.getStart();
                }
                instructionHandleArr = new InstructionHandle[iArr.length];
                i = 14;
                while (i < instructionHandleArr.length) {
                    TestSeq testSeq6 = this._testSeq[i];
                    if (zArr[i]) {
                        if (zArr2[i]) {
                            instructionHandleArr[i] = instructionHandle;
                        } else {
                            instructionHandleArr[i] = start4;
                        }
                        methodGenerator = methodGenerator3;
                    } else if (testSeq6 == null) {
                        methodGenerator = methodGenerator3;
                        instructionHandleArr[i] = start;
                    } else if (zArr2[i]) {
                        methodGenerator = methodGenerator3;
                        instructionHandleArr[i] = testSeq6.compile(classGenerator, methodGenerator, instructionHandle);
                    } else {
                        methodGenerator = methodGenerator3;
                        instructionHandleArr[i] = testSeq6.compile(classGenerator, methodGenerator, start4);
                    }
                    i++;
                    methodGenerator3 = methodGenerator;
                }
                LocationPathPattern locationPathPattern2 = this._rootPattern;
                instructionHandleArr[0] = locationPathPattern2 == null ? getTemplateInstructionHandle(locationPathPattern2.getTemplate()) : instructionHandle4;
                locationPathPattern = this._rootPattern;
                if (locationPathPattern != null) {
                    instructionHandle4 = getTemplateInstructionHandle(locationPathPattern.getTemplate());
                }
                instructionHandleArr[9] = instructionHandle4;
                testSeqArr = this._testSeq;
                if (testSeqArr[3] != null) {
                    instructionHandle6 = testSeqArr[3].compile(classGenerator, methodGenerator3, instructionHandle6);
                }
                instructionHandleArr[3] = instructionHandle6;
                instructionHandleArr[13] = start;
                instructionHandleArr[1] = start4;
                instructionHandleArr[2] = instructionHandle;
                InstructionHandle instructionHandle7 = this._childNodeTestSeq == null ? instructionHandle3 : start;
                testSeqArr2 = this._testSeq;
                if (testSeqArr2[7] == null) {
                    instructionHandleArr[7] = testSeqArr2[7].compile(classGenerator, methodGenerator3, instructionHandle7);
                } else {
                    instructionHandleArr[7] = instructionHandle7;
                }
                InstructionHandle instructionHandle8 = this._childNodeTestSeq == null ? instructionHandle3 : start;
                testSeqArr3 = this._testSeq;
                if (testSeqArr3[8] != null) {
                    instructionHandle8 = testSeqArr3[8].compile(classGenerator, methodGenerator3, instructionHandle8);
                }
                instructionHandleArr[8] = instructionHandle8;
                instructionHandleArr[4] = start;
                instructionHandleArr[11] = start;
                instructionHandleArr[10] = start;
                instructionHandleArr[6] = start;
                instructionHandleArr[5] = start;
                instructionHandleArr[12] = start;
                for (i2 = 14; i2 < instructionHandleArr.length; i2++) {
                    TestSeq testSeq7 = this._testSeq[i2];
                    if (testSeq7 == null || zArr[i2]) {
                        if (zArr2[i2]) {
                            instructionHandleArr[i2] = instructionHandle;
                        } else {
                            instructionHandleArr[i2] = start4;
                        }
                    } else if (zArr2[i2]) {
                        instructionHandleArr[i2] = testSeq7.compile(classGenerator, methodGenerator3, instructionHandle);
                    } else {
                        instructionHandleArr[i2] = testSeq7.compile(classGenerator, methodGenerator3, start4);
                    }
                }
                if (instructionList != null) {
                    instructionList3.insert(instructionList);
                }
                int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getExpandedTypeID", Constants.GET_PARENT_SIG);
                instructionList3.append(methodGenerator3.loadDOM());
                instructionList3.append(new ILOAD(this._currentIndex));
                instructionList3.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
                instructionList3.append(new SWITCH(iArr, instructionHandleArr, start));
                appendTestSequences(instructionList3);
                appendTemplateCode(instructionList3);
                if (compileNamespaces != null) {
                    instructionList3.append(compileNamespaces);
                }
                if (compileNamespaces2 != null) {
                    instructionList3.append(compileNamespaces2);
                }
                instructionList3.append(compileDefaultRecursion);
                instructionList3.append(compileDefaultText);
                instructionList2.append(instructionList3);
                instructionList2.append(instructionList4);
                peepHoleOptimization(methodGenerator3);
                classGenerator.addMethod(methodGenerator3);
                map = this._importLevels;
                if (map == null) {
                    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                        compileApplyImports(classGenerator, entry.getValue().intValue(), entry.getKey().intValue());
                    }
                    return;
                }
                return;
            }
        } else {
            instructionHandle4 = start2;
        }
        instructionHandle3 = compile;
        instructionHandle5 = instructionHandle2;
        MethodGenerator methodGenerator32 = methodGenerator2;
        InstructionHandle instructionHandle62 = instructionHandle5;
        compileNamespaces = compileNamespaces(classGenerator, methodGenerator2, zArr, zArr2, false, instructionHandle3);
        if (compileNamespaces == null) {
        }
        compileNamespaces2 = compileNamespaces(classGenerator, methodGenerator32, zArr, zArr2, true, instructionHandle);
        if (compileNamespaces2 != null) {
        }
        instructionHandleArr = new InstructionHandle[iArr.length];
        i = 14;
        while (i < instructionHandleArr.length) {
        }
        LocationPathPattern locationPathPattern22 = this._rootPattern;
        instructionHandleArr[0] = locationPathPattern22 == null ? getTemplateInstructionHandle(locationPathPattern22.getTemplate()) : instructionHandle4;
        locationPathPattern = this._rootPattern;
        if (locationPathPattern != null) {
        }
        instructionHandleArr[9] = instructionHandle4;
        testSeqArr = this._testSeq;
        if (testSeqArr[3] != null) {
        }
        instructionHandleArr[3] = instructionHandle62;
        instructionHandleArr[13] = start;
        instructionHandleArr[1] = start4;
        instructionHandleArr[2] = instructionHandle;
        if (this._childNodeTestSeq == null) {
        }
        testSeqArr2 = this._testSeq;
        if (testSeqArr2[7] == null) {
        }
        if (this._childNodeTestSeq == null) {
        }
        testSeqArr3 = this._testSeq;
        if (testSeqArr3[8] != null) {
        }
        instructionHandleArr[8] = instructionHandle8;
        instructionHandleArr[4] = start;
        instructionHandleArr[11] = start;
        instructionHandleArr[10] = start;
        instructionHandleArr[6] = start;
        instructionHandleArr[5] = start;
        instructionHandleArr[12] = start;
        while (i2 < instructionHandleArr.length) {
        }
        if (instructionList != null) {
        }
        int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getExpandedTypeID", Constants.GET_PARENT_SIG);
        instructionList3.append(methodGenerator32.loadDOM());
        instructionList3.append(new ILOAD(this._currentIndex));
        instructionList3.append(new INVOKEINTERFACE(addInterfaceMethodref2, 2));
        instructionList3.append(new SWITCH(iArr, instructionHandleArr, start));
        appendTestSequences(instructionList3);
        appendTemplateCode(instructionList3);
        if (compileNamespaces != null) {
        }
        if (compileNamespaces2 != null) {
        }
        instructionList3.append(compileDefaultRecursion);
        instructionList3.append(compileDefaultText);
        instructionList2.append(instructionList3);
        instructionList2.append(instructionList4);
        peepHoleOptimization(methodGenerator32);
        classGenerator.addMethod(methodGenerator32);
        map = this._importLevels;
        if (map == null) {
        }
    }

    private void compileTemplateCalls(ClassGenerator classGenerator, MethodGenerator methodGenerator, InstructionHandle instructionHandle, int i, int i2) {
        for (Template template : this._neededTemplates.keySet()) {
            int importPrecedence = template.getImportPrecedence();
            if (importPrecedence >= i && importPrecedence < i2) {
                if (template.hasContents()) {
                    InstructionList compile = template.compile(classGenerator, methodGenerator);
                    compile.append((BranchInstruction) new GOTO_W(instructionHandle));
                    this._templateILs.put(template, compile);
                    this._templateIHs.put(template, compile.getStart());
                } else {
                    this._templateIHs.put(template, instructionHandle);
                }
            }
        }
    }

    public void compileApplyImports(ClassGenerator classGenerator, int i, int i2) {
        double d;
        int i3;
        XSLTC xsltc = classGenerator.getParser().getXSLTC();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        Vector namesIndex = xsltc.getNamesIndex();
        this._namedTemplates = new HashMap();
        this._neededTemplates = new HashMap();
        this._templateIHs = new HashMap();
        this._templateILs = new HashMap();
        this._patternGroups = new Vector[32];
        this._rootPattern = null;
        Vector vector = this._templates;
        this._templates = new Vector();
        Enumeration elements = vector.elements();
        while (elements.hasMoreElements()) {
            Template template = (Template) elements.nextElement();
            int importPrecedence = template.getImportPrecedence();
            if (importPrecedence >= i && importPrecedence < i2) {
                addTemplate(template);
            }
        }
        processPatterns(this._keys);
        Type[] typeArr = {Util.getJCRefType(Constants.DOM_INTF_SIG), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;"), Type.INT};
        String[] strArr = {Constants.DOCUMENT_PNAME, Constants.ITERATOR_PNAME, Constants.TRANSLET_OUTPUT_PNAME, "node"};
        InstructionList instructionList = new InstructionList();
        MethodGenerator methodGenerator = new MethodGenerator(17, Type.VOID, typeArr, strArr, functionName() + '_' + i2, getClassName(), instructionList, classGenerator.getConstantPool());
        methodGenerator.addException("ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException");
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable2(Keywords.FUNC_CURRENT_STRING, Type.INT, null);
        this._currentIndex = addLocalVariable2.getIndex();
        instructionList.append(new ILOAD(methodGenerator.getLocalIndex("node")));
        addLocalVariable2.setStart(instructionList.append(new ISTORE(this._currentIndex)));
        InstructionList instructionList2 = new InstructionList();
        instructionList2.append(NOP);
        InstructionList instructionList3 = new InstructionList();
        instructionList3.append(RETURN);
        InstructionHandle start = instructionList3.getStart();
        InstructionList compileDefaultRecursion = compileDefaultRecursion(classGenerator, methodGenerator, start);
        InstructionHandle start2 = compileDefaultRecursion.getStart();
        InstructionList compileDefaultText = compileDefaultText(classGenerator, methodGenerator, start);
        InstructionHandle start3 = compileDefaultText.getStart();
        int[] iArr = new int[(namesIndex.size() + 14)];
        for (int i4 = 0; i4 < iArr.length; i4++) {
            iArr[i4] = i4;
        }
        boolean[] zArr = new boolean[iArr.length];
        boolean[] zArr2 = new boolean[iArr.length];
        for (int i5 = 0; i5 < namesIndex.size(); i5++) {
            String str = (String) namesIndex.elementAt(i5);
            int i6 = i5 + 14;
            zArr[i6] = isAttributeName(str);
            zArr2[i6] = isNamespaceName(str);
        }
        compileTemplateCalls(classGenerator, methodGenerator, start, i, i2);
        TestSeq testSeq = this._testSeq[1];
        InstructionHandle compile = testSeq != null ? testSeq.compile(classGenerator, methodGenerator, start) : start2;
        TestSeq testSeq2 = this._testSeq[2];
        InstructionHandle compile2 = testSeq2 != null ? testSeq2.compile(classGenerator, methodGenerator, start) : start;
        TestSeq testSeq3 = this._idxTestSeq;
        InstructionList instructionList4 = testSeq3 != null ? testSeq3.getInstructionList() : null;
        TestSeq testSeq4 = this._childNodeTestSeq;
        if (testSeq4 != null) {
            double priority = testSeq4.getPriority();
            int position = this._childNodeTestSeq.getPosition();
            int i7 = Integer.MIN_VALUE;
            double d2 = -1.7976931348623157E308d;
            if (testSeq != null) {
                d = testSeq.getPriority();
                i3 = testSeq.getPosition();
            } else {
                i3 = Integer.MIN_VALUE;
                d = -1.7976931348623157E308d;
            }
            if (d == Double.NaN || d < priority || (d == priority && i3 < position)) {
                compile = this._childNodeTestSeq.compile(classGenerator, methodGenerator, start);
            }
            TestSeq testSeq5 = this._testSeq[3];
            if (testSeq5 != null) {
                d2 = testSeq5.getPriority();
                i7 = testSeq5.getPosition();
            }
            if (d2 == Double.NaN || d2 < priority || (d2 == priority && i7 < position)) {
                start3 = this._childNodeTestSeq.compile(classGenerator, methodGenerator, start);
                this._testSeq[3] = this._childNodeTestSeq;
            }
        }
        InstructionHandle instructionHandle = start3;
        InstructionList compileNamespaces = compileNamespaces(classGenerator, methodGenerator, zArr2, zArr, false, compile);
        InstructionHandle start4 = compileNamespaces != null ? compileNamespaces.getStart() : compile;
        InstructionList compileNamespaces2 = compileNamespaces(classGenerator, methodGenerator, zArr2, zArr, true, compile2);
        if (compileNamespaces2 != null) {
            compile2 = compileNamespaces2.getStart();
        }
        InstructionHandle[] instructionHandleArr = new InstructionHandle[iArr.length];
        for (int i8 = 14; i8 < instructionHandleArr.length; i8++) {
            TestSeq testSeq6 = this._testSeq[i8];
            if (zArr2[i8]) {
                if (zArr[i8]) {
                    instructionHandleArr[i8] = compile2;
                } else {
                    instructionHandleArr[i8] = start4;
                }
            } else if (testSeq6 == null) {
                instructionHandleArr[i8] = start;
            } else if (zArr[i8]) {
                instructionHandleArr[i8] = testSeq6.compile(classGenerator, methodGenerator, compile2);
            } else {
                instructionHandleArr[i8] = testSeq6.compile(classGenerator, methodGenerator, start4);
            }
        }
        LocationPathPattern locationPathPattern = this._rootPattern;
        instructionHandleArr[0] = locationPathPattern != null ? getTemplateInstructionHandle(locationPathPattern.getTemplate()) : start2;
        LocationPathPattern locationPathPattern2 = this._rootPattern;
        if (locationPathPattern2 != null) {
            start2 = getTemplateInstructionHandle(locationPathPattern2.getTemplate());
        }
        instructionHandleArr[9] = start2;
        TestSeq[] testSeqArr = this._testSeq;
        if (testSeqArr[3] != null) {
            instructionHandle = testSeqArr[3].compile(classGenerator, methodGenerator, instructionHandle);
        }
        instructionHandleArr[3] = instructionHandle;
        instructionHandleArr[13] = start;
        instructionHandleArr[1] = start4;
        instructionHandleArr[2] = compile2;
        InstructionHandle instructionHandle2 = this._childNodeTestSeq != null ? compile : start;
        TestSeq[] testSeqArr2 = this._testSeq;
        if (testSeqArr2[7] != null) {
            instructionHandleArr[7] = testSeqArr2[7].compile(classGenerator, methodGenerator, instructionHandle2);
        } else {
            instructionHandleArr[7] = instructionHandle2;
        }
        InstructionHandle instructionHandle3 = this._childNodeTestSeq != null ? compile : start;
        TestSeq[] testSeqArr3 = this._testSeq;
        if (testSeqArr3[8] != null) {
            instructionHandle3 = testSeqArr3[8].compile(classGenerator, methodGenerator, instructionHandle3);
        }
        instructionHandleArr[8] = instructionHandle3;
        instructionHandleArr[4] = start;
        instructionHandleArr[11] = start;
        instructionHandleArr[10] = start;
        instructionHandleArr[6] = start;
        instructionHandleArr[5] = start;
        instructionHandleArr[12] = start;
        for (int i9 = 14; i9 < instructionHandleArr.length; i9++) {
            TestSeq testSeq7 = this._testSeq[i9];
            if (testSeq7 == null || zArr2[i9]) {
                if (zArr[i9]) {
                    instructionHandleArr[i9] = compile2;
                } else {
                    instructionHandleArr[i9] = start4;
                }
            } else if (zArr[i9]) {
                instructionHandleArr[i9] = testSeq7.compile(classGenerator, methodGenerator, compile2);
            } else {
                instructionHandleArr[i9] = testSeq7.compile(classGenerator, methodGenerator, start4);
            }
        }
        if (instructionList4 != null) {
            instructionList2.insert(instructionList4);
        }
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getExpandedTypeID", Constants.GET_PARENT_SIG);
        instructionList2.append(methodGenerator.loadDOM());
        instructionList2.append(new ILOAD(this._currentIndex));
        instructionList2.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        instructionList2.append(new SWITCH(iArr, instructionHandleArr, start));
        appendTestSequences(instructionList2);
        appendTemplateCode(instructionList2);
        if (compileNamespaces != null) {
            instructionList2.append(compileNamespaces);
        }
        if (compileNamespaces2 != null) {
            instructionList2.append(compileNamespaces2);
        }
        instructionList2.append(compileDefaultRecursion);
        instructionList2.append(compileDefaultText);
        instructionList.append(instructionList2);
        addLocalVariable2.setEnd(instructionList2.getEnd());
        instructionList.append(instructionList3);
        peepHoleOptimization(methodGenerator);
        classGenerator.addMethod(methodGenerator);
        this._templates = vector;
    }

    private void peepHoleOptimization(MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        InstructionFinder instructionFinder = new InstructionFinder(instructionList);
        Iterator search = instructionFinder.search("loadinstruction pop");
        while (search.hasNext()) {
            InstructionHandle[] instructionHandleArr = (InstructionHandle[]) search.next();
            try {
                if (!instructionHandleArr[0].hasTargeters() && !instructionHandleArr[1].hasTargeters()) {
                    instructionList.delete(instructionHandleArr[0], instructionHandleArr[1]);
                }
            } catch (TargetLostException unused) {
            }
        }
        Iterator search2 = instructionFinder.search("iload iload swap istore");
        while (search2.hasNext()) {
            InstructionHandle[] instructionHandleArr2 = (InstructionHandle[]) search2.next();
            try {
                ILOAD iload = (ILOAD) instructionHandleArr2[0].getInstruction();
                ILOAD iload2 = (ILOAD) instructionHandleArr2[1].getInstruction();
                ISTORE istore = (ISTORE) instructionHandleArr2[3].getInstruction();
                if (!instructionHandleArr2[1].hasTargeters() && !instructionHandleArr2[2].hasTargeters() && !instructionHandleArr2[3].hasTargeters() && iload.getIndex() == iload2.getIndex() && iload2.getIndex() == istore.getIndex()) {
                    instructionList.delete(instructionHandleArr2[1], instructionHandleArr2[3]);
                }
            } catch (TargetLostException unused2) {
            }
        }
        Iterator search3 = instructionFinder.search("loadinstruction loadinstruction swap");
        while (search3.hasNext()) {
            InstructionHandle[] instructionHandleArr3 = (InstructionHandle[]) search3.next();
            try {
                if (!instructionHandleArr3[0].hasTargeters() && !instructionHandleArr3[1].hasTargeters() && !instructionHandleArr3[2].hasTargeters()) {
                    instructionList.insert(instructionHandleArr3[0], instructionHandleArr3[1].getInstruction());
                    instructionList.delete(instructionHandleArr3[1], instructionHandleArr3[2]);
                }
            } catch (TargetLostException unused3) {
            }
        }
        Iterator search4 = instructionFinder.search("aload aload");
        while (search4.hasNext()) {
            InstructionHandle[] instructionHandleArr4 = (InstructionHandle[]) search4.next();
            try {
                if (!instructionHandleArr4[1].hasTargeters() && ((ALOAD) instructionHandleArr4[0].getInstruction()).getIndex() == ((ALOAD) instructionHandleArr4[1].getInstruction()).getIndex()) {
                    instructionList.insert(instructionHandleArr4[1], new DUP());
                    instructionList.delete(instructionHandleArr4[1]);
                }
            } catch (TargetLostException unused4) {
            }
        }
    }

    public InstructionHandle getTemplateInstructionHandle(Template template) {
        return this._templateIHs.get(template);
    }

    private static boolean isAttributeName(String str) {
        if (str.charAt(str.lastIndexOf(58) + 1) == '@') {
            return true;
        }
        return false;
    }

    private static boolean isNamespaceName(String str) {
        if (str.lastIndexOf(58) <= -1 || str.charAt(str.length() - 1) != '*') {
            return false;
        }
        return true;
    }
}
