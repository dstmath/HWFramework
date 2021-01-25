package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

/* access modifiers changed from: package-private */
public final class DecimalFormatting extends TopLevelElement {
    private static final String DFS_CLASS = "java.text.DecimalFormatSymbols";
    private static final String DFS_SIG = "Ljava/text/DecimalFormatSymbols;";
    private QName _name = null;

    DecimalFormatting() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute("name");
        if (attribute.length() > 0 && !XML11Char.isXML11ValidQName(attribute)) {
            parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute, (SyntaxTreeNode) this));
        }
        this._name = parser.getQNameIgnoreDefaultNs(attribute);
        if (this._name == null) {
            this._name = parser.getQNameIgnoreDefaultNs("");
        }
        SymbolTable symbolTable = parser.getSymbolTable();
        if (symbolTable.getDecimalFormatting(this._name) != null) {
            reportWarning(this, parser, ErrorMsg.SYMBOLS_REDEF_ERR, this._name.toString());
        } else {
            symbolTable.addDecimalFormatting(this._name, this);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0193  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x01ac A[SYNTHETIC] */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        int i;
        boolean z;
        int addMethodref;
        int addMethodref2;
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref3 = constantPool.addMethodref(DFS_CLASS, Constants.CONSTRUCTOR_NAME, "(Ljava/util/Locale;)V");
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new PUSH(constantPool, this._name.toString()));
        instructionList.append(new NEW(constantPool.addClass(DFS_CLASS)));
        instructionList.append(DUP);
        instructionList.append(new GETSTATIC(constantPool.addFieldref(Constants.LOCALE_CLASS, "US", Constants.LOCALE_SIG)));
        instructionList.append(new INVOKESPECIAL(addMethodref3));
        String attribute = getAttribute("NaN");
        if (attribute == null || attribute.equals("")) {
            int addMethodref4 = constantPool.addMethodref(DFS_CLASS, "setNaN", "(Ljava/lang/String;)V");
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, "NaN"));
            instructionList.append(new INVOKEVIRTUAL(addMethodref4));
        }
        String attribute2 = getAttribute(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_INFINITY);
        if (attribute2 == null || attribute2.equals("")) {
            int addMethodref5 = constantPool.addMethodref(DFS_CLASS, "setInfinity", "(Ljava/lang/String;)V");
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRVAL_INFINITY));
            instructionList.append(new INVOKEVIRTUAL(addMethodref5));
        }
        int length = this._attributes.getLength();
        for (int i2 = 0; i2 < length; i2++) {
            String qName = this._attributes.getQName(i2);
            String value = this._attributes.getValue(i2);
            if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_DECIMALSEPARATOR)) {
                addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setDecimalSeparator", "(C)V");
            } else if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_GROUPINGSEPARATOR)) {
                addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setGroupingSeparator", "(C)V");
            } else if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_MINUSSIGN)) {
                addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setMinusSign", "(C)V");
            } else if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_PERCENT)) {
                addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setPercent", "(C)V");
            } else if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_PERMILLE)) {
                addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setPerMill", "(C)V");
            } else if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_ZERODIGIT)) {
                addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setZeroDigit", "(C)V");
            } else if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_DIGIT)) {
                addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setDigit", "(C)V");
            } else if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_PATTERNSEPARATOR)) {
                addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setPatternSeparator", "(C)V");
            } else {
                if (qName.equals("NaN")) {
                    addMethodref = constantPool.addMethodref(DFS_CLASS, "setNaN", "(Ljava/lang/String;)V");
                    instructionList.append(DUP);
                    instructionList.append(new PUSH(constantPool, value));
                    instructionList.append(new INVOKEVIRTUAL(addMethodref));
                } else if (qName.equals(ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRNAME_INFINITY)) {
                    addMethodref = constantPool.addMethodref(DFS_CLASS, "setInfinity", "(Ljava/lang/String;)V");
                    instructionList.append(DUP);
                    instructionList.append(new PUSH(constantPool, value));
                    instructionList.append(new INVOKEVIRTUAL(addMethodref));
                } else {
                    z = false;
                    i = 0;
                    if (z) {
                        instructionList.append(DUP);
                        instructionList.append(new PUSH(constantPool, (int) value.charAt(0)));
                        instructionList.append(new INVOKEVIRTUAL(i));
                    }
                }
                i = addMethodref;
                z = false;
                if (z) {
                }
            }
            i = addMethodref2;
            z = true;
            if (z) {
            }
        }
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, "addDecimalFormat", "(Ljava/lang/String;Ljava/text/DecimalFormatSymbols;)V")));
    }

    public static void translateDefaultDFS(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(DFS_CLASS, Constants.CONSTRUCTOR_NAME, "(Ljava/util/Locale;)V");
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new PUSH(constantPool, ""));
        instructionList.append(new NEW(constantPool.addClass(DFS_CLASS)));
        instructionList.append(DUP);
        instructionList.append(new GETSTATIC(constantPool.addFieldref(Constants.LOCALE_CLASS, "US", Constants.LOCALE_SIG)));
        instructionList.append(new INVOKESPECIAL(addMethodref));
        int addMethodref2 = constantPool.addMethodref(DFS_CLASS, "setNaN", "(Ljava/lang/String;)V");
        instructionList.append(DUP);
        instructionList.append(new PUSH(constantPool, "NaN"));
        instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        int addMethodref3 = constantPool.addMethodref(DFS_CLASS, "setInfinity", "(Ljava/lang/String;)V");
        instructionList.append(DUP);
        instructionList.append(new PUSH(constantPool, ohos.com.sun.org.apache.xalan.internal.templates.Constants.ATTRVAL_INFINITY));
        instructionList.append(new INVOKEVIRTUAL(addMethodref3));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, "addDecimalFormat", "(Ljava/lang/String;Ljava/text/DecimalFormatSymbols;)V")));
    }
}
