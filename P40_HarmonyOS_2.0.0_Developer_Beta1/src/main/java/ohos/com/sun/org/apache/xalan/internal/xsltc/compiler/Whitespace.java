package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/* access modifiers changed from: package-private */
public final class Whitespace extends TopLevelElement {
    public static final int PRESERVE_SPACE = 2;
    public static final int RULE_ALL = 3;
    public static final int RULE_ELEMENT = 1;
    public static final int RULE_NAMESPACE = 2;
    public static final int RULE_NONE = 0;
    public static final int STRIP_SPACE = 1;
    public static final int USE_PREDICATE = 0;
    private int _action;
    private String _elementList;
    private int _importPrecedence;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
    }

    Whitespace() {
    }

    /* access modifiers changed from: private */
    public static final class WhitespaceRule {
        private final int _action;
        private String _element;
        private String _namespace;
        private int _priority;
        private int _type;

        public WhitespaceRule(int i, String str, int i2) {
            this._action = i;
            int lastIndexOf = str.lastIndexOf(58);
            if (lastIndexOf >= 0) {
                this._namespace = str.substring(0, lastIndexOf);
                this._element = str.substring(lastIndexOf + 1, str.length());
            } else {
                this._namespace = "";
                this._element = str;
            }
            this._priority = i2 << 2;
            if (!this._element.equals("*")) {
                this._type = 1;
            } else if (this._namespace == "") {
                this._type = 3;
                this._priority += 2;
            } else {
                this._type = 2;
                this._priority++;
            }
        }

        public int compareTo(WhitespaceRule whitespaceRule) {
            int i = this._priority;
            int i2 = whitespaceRule._priority;
            if (i < i2) {
                return -1;
            }
            return i > i2 ? 1 : 0;
        }

        public int getAction() {
            return this._action;
        }

        public int getStrength() {
            return this._type;
        }

        public int getPriority() {
            return this._priority;
        }

        public String getElement() {
            return this._element;
        }

        public String getNamespace() {
            return this._namespace;
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        this._action = this._qname.getLocalPart().endsWith(Constants.ELEMNAME_STRIPSPACE_STRING) ? 1 : 2;
        this._importPrecedence = parser.getCurrentImportPrecedence();
        this._elementList = getAttribute("elements");
        String str = this._elementList;
        if (str == null || str.length() == 0) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "elements");
            return;
        }
        parser.getSymbolTable();
        StringTokenizer stringTokenizer = new StringTokenizer(this._elementList);
        StringBuffer stringBuffer = new StringBuffer("");
        while (stringTokenizer.hasMoreElements()) {
            String nextToken = stringTokenizer.nextToken();
            int indexOf = nextToken.indexOf(58);
            if (indexOf != -1) {
                String lookupNamespace = lookupNamespace(nextToken.substring(0, indexOf));
                if (lookupNamespace != null) {
                    stringBuffer.append(lookupNamespace);
                    stringBuffer.append(':');
                    stringBuffer.append(nextToken.substring(indexOf + 1));
                } else {
                    stringBuffer.append(nextToken);
                }
            } else {
                stringBuffer.append(nextToken);
            }
            if (stringTokenizer.hasMoreElements()) {
                stringBuffer.append(" ");
            }
        }
        this._elementList = stringBuffer.toString();
    }

    public Vector getRules() {
        Vector vector = new Vector();
        StringTokenizer stringTokenizer = new StringTokenizer(this._elementList);
        while (stringTokenizer.hasMoreElements()) {
            vector.add(new WhitespaceRule(this._action, stringTokenizer.nextToken(), this._importPrecedence));
        }
        return vector;
    }

    private static WhitespaceRule findContradictingRule(Vector vector, WhitespaceRule whitespaceRule) {
        WhitespaceRule whitespaceRule2;
        int i = 0;
        while (i < vector.size() && (whitespaceRule2 = (WhitespaceRule) vector.elementAt(i)) != whitespaceRule) {
            int strength = whitespaceRule2.getStrength();
            if (strength != 1) {
                if (strength != 2) {
                    if (strength == 3) {
                        return whitespaceRule2;
                    }
                    i++;
                }
            } else if (!whitespaceRule.getElement().equals(whitespaceRule2.getElement())) {
                continue;
                i++;
            }
            if (whitespaceRule.getNamespace().equals(whitespaceRule2.getNamespace())) {
                return whitespaceRule2;
            }
            i++;
        }
        return null;
    }

    private static int prioritizeRules(Vector vector) {
        int i = 0;
        quicksort(vector, 0, vector.size() - 1);
        boolean z = false;
        for (int i2 = 0; i2 < vector.size(); i2++) {
            if (((WhitespaceRule) vector.elementAt(i2)).getAction() == 1) {
                z = true;
            }
        }
        int i3 = 2;
        if (!z) {
            vector.removeAllElements();
            return 2;
        }
        while (i < vector.size()) {
            WhitespaceRule whitespaceRule = (WhitespaceRule) vector.elementAt(i);
            if (findContradictingRule(vector, whitespaceRule) != null) {
                vector.remove(i);
            } else {
                if (whitespaceRule.getStrength() == 3) {
                    i3 = whitespaceRule.getAction();
                    for (int i4 = i; i4 < vector.size(); i4++) {
                        vector.removeElementAt(i4);
                    }
                }
                i++;
            }
        }
        if (vector.size() == 0) {
            return i3;
        }
        while (((WhitespaceRule) vector.lastElement()).getAction() == i3) {
            vector.removeElementAt(vector.size() - 1);
            if (vector.size() <= 0) {
                break;
            }
        }
        return i3;
    }

    public static void compileStripSpace(BranchHandle[] branchHandleArr, int i, InstructionList instructionList) {
        InstructionHandle append = instructionList.append(ICONST_1);
        instructionList.append(IRETURN);
        for (int i2 = 0; i2 < i; i2++) {
            branchHandleArr[i2].setTarget(append);
        }
    }

    public static void compilePreserveSpace(BranchHandle[] branchHandleArr, int i, InstructionList instructionList) {
        InstructionHandle append = instructionList.append(ICONST_0);
        instructionList.append(IRETURN);
        for (int i2 = 0; i2 < i; i2++) {
            branchHandleArr[i2].setTarget(append);
        }
    }

    private static void compilePredicate(Vector vector, int i, ClassGenerator classGenerator) {
        XSLTC xsltc;
        QName qName;
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = new InstructionList();
        XSLTC xsltc2 = classGenerator.getParser().getXSLTC();
        MethodGenerator methodGenerator = new MethodGenerator(17, Type.BOOLEAN, new Type[]{Util.getJCRefType(Constants.DOM_INTF_SIG), Type.INT, Type.INT}, new String[]{Constants.DOM_PNAME, "node", "type"}, Constants.STRIP_SPACE, classGenerator.getClassName(), instructionList, constantPool);
        classGenerator.addInterface(Constants.STRIP_SPACE_INTF);
        int localIndex = methodGenerator.getLocalIndex(Constants.DOM_PNAME);
        int localIndex2 = methodGenerator.getLocalIndex("node");
        int localIndex3 = methodGenerator.getLocalIndex("type");
        BranchHandle[] branchHandleArr = new BranchHandle[vector.size()];
        BranchHandle[] branchHandleArr2 = new BranchHandle[vector.size()];
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (i2 < vector.size()) {
            WhitespaceRule whitespaceRule = (WhitespaceRule) vector.elementAt(i2);
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNamespaceName", "(I)Ljava/lang/String;");
            int addMethodref = constantPool.addMethodref("java/lang/String", "compareTo", Constants.STRING_TO_INT_SIG);
            if (whitespaceRule.getStrength() == 2) {
                instructionList.append(new ALOAD(localIndex));
                instructionList.append(new ILOAD(localIndex2));
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
                instructionList.append(new PUSH(constantPool, whitespaceRule.getNamespace()));
                instructionList.append(new INVOKEVIRTUAL(addMethodref));
                instructionList.append(ICONST_0);
                if (whitespaceRule.getAction() == 1) {
                    branchHandleArr[i4] = instructionList.append((BranchInstruction) new IF_ICMPEQ(null));
                    i4++;
                } else {
                    branchHandleArr2[i3] = instructionList.append((BranchInstruction) new IF_ICMPEQ(null));
                    i3++;
                }
            } else if (whitespaceRule.getStrength() == 1) {
                Parser parser = classGenerator.getParser();
                if (whitespaceRule.getNamespace() != "") {
                    qName = parser.getQName(whitespaceRule.getNamespace(), (String) null, whitespaceRule.getElement());
                } else {
                    qName = parser.getQName(whitespaceRule.getElement());
                }
                xsltc = xsltc2;
                int registerElement = xsltc.registerElement(qName);
                instructionList.append(new ILOAD(localIndex3));
                instructionList.append(new PUSH(constantPool, registerElement));
                if (whitespaceRule.getAction() == 1) {
                    branchHandleArr[i4] = instructionList.append((BranchInstruction) new IF_ICMPEQ(null));
                    i4++;
                } else {
                    branchHandleArr2[i3] = instructionList.append((BranchInstruction) new IF_ICMPEQ(null));
                    i3++;
                }
                i2++;
                xsltc2 = xsltc;
            }
            xsltc = xsltc2;
            i2++;
            xsltc2 = xsltc;
        }
        if (i == 1) {
            compileStripSpace(branchHandleArr, i4, instructionList);
            compilePreserveSpace(branchHandleArr2, i3, instructionList);
        } else {
            compilePreserveSpace(branchHandleArr2, i3, instructionList);
            compileStripSpace(branchHandleArr, i4, instructionList);
        }
        classGenerator.addMethod(methodGenerator);
    }

    private static void compileDefault(int i, ClassGenerator classGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = new InstructionList();
        classGenerator.getParser().getXSLTC();
        MethodGenerator methodGenerator = new MethodGenerator(17, Type.BOOLEAN, new Type[]{Util.getJCRefType(Constants.DOM_INTF_SIG), Type.INT, Type.INT}, new String[]{Constants.DOM_PNAME, "node", "type"}, Constants.STRIP_SPACE, classGenerator.getClassName(), instructionList, constantPool);
        classGenerator.addInterface(Constants.STRIP_SPACE_INTF);
        if (i == 1) {
            instructionList.append(ICONST_1);
        } else {
            instructionList.append(ICONST_0);
        }
        instructionList.append(IRETURN);
        classGenerator.addMethod(methodGenerator);
    }

    public static int translateRules(Vector vector, ClassGenerator classGenerator) {
        int prioritizeRules = prioritizeRules(vector);
        if (vector.size() == 0) {
            compileDefault(prioritizeRules, classGenerator);
            return prioritizeRules;
        }
        compilePredicate(vector, prioritizeRules, classGenerator);
        return 0;
    }

    private static void quicksort(Vector vector, int i, int i2) {
        while (i < i2) {
            int partition = partition(vector, i, i2);
            quicksort(vector, i, partition);
            i = partition + 1;
        }
    }

    private static int partition(Vector vector, int i, int i2) {
        WhitespaceRule whitespaceRule = (WhitespaceRule) vector.elementAt((i + i2) >>> 1);
        int i3 = i - 1;
        int i4 = i2 + 1;
        while (true) {
            i4--;
            if (whitespaceRule.compareTo((WhitespaceRule) vector.elementAt(i4)) >= 0) {
                do {
                    i3++;
                } while (whitespaceRule.compareTo((WhitespaceRule) vector.elementAt(i3)) > 0);
                if (i3 >= i4) {
                    return i4;
                }
                vector.setElementAt(vector.elementAt(i4), i3);
                vector.setElementAt((WhitespaceRule) vector.elementAt(i3), i4);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        return ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Void;
    }
}
