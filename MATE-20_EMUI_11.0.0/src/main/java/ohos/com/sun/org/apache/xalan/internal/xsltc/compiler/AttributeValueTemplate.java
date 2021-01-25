package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;

final class AttributeValueTemplate extends AttributeValue {
    static final String DELIMITER = "￾";
    static final int IN_EXPR = 1;
    static final int IN_EXPR_DQUOTES = 3;
    static final int IN_EXPR_SQUOTES = 2;
    static final int OUT_EXPR = 0;

    public AttributeValueTemplate(String str, Parser parser, SyntaxTreeNode syntaxTreeNode) {
        setParent(syntaxTreeNode);
        setParser(parser);
        try {
            parseAVTemplate(str, parser);
        } catch (NoSuchElementException unused) {
            reportError(syntaxTreeNode, parser, ErrorMsg.ATTR_VAL_TEMPLATE_ERR, str);
        }
    }

    private void parseAVTemplate(String str, Parser parser) {
        String str2;
        String nextToken;
        StringTokenizer stringTokenizer = new StringTokenizer(str, "{}\"'", true);
        StringBuffer stringBuffer = new StringBuffer();
        int i = 0;
        boolean z = false;
        String str3 = null;
        while (stringTokenizer.hasMoreTokens()) {
            if (str3 != null) {
                str2 = null;
            } else {
                str2 = str3;
                str3 = stringTokenizer.nextToken();
            }
            if (str3.length() == 1) {
                char charAt = str3.charAt(i);
                if (charAt == '\"') {
                    if (z) {
                        z = true;
                    } else if (z) {
                        z = true;
                    }
                    stringBuffer.append(str3);
                } else if (charAt != '\'') {
                    if (charAt != '{') {
                        if (charAt != '}') {
                            stringBuffer.append(str3);
                        } else if (!z) {
                            nextToken = stringTokenizer.nextToken();
                            if (nextToken.equals("}")) {
                                stringBuffer.append(nextToken);
                            } else {
                                reportError(getParent(), parser, ErrorMsg.ATTR_VAL_TEMPLATE_ERR, str);
                                str3 = nextToken;
                                i = 0;
                            }
                        } else if (z) {
                            stringBuffer.append(DELIMITER);
                            str3 = str2;
                            z = false;
                            i = 0;
                        } else if (z || z) {
                            stringBuffer.append(str3);
                        }
                    } else if (!z) {
                        nextToken = stringTokenizer.nextToken();
                        if (nextToken.equals("{")) {
                            stringBuffer.append(nextToken);
                        } else {
                            stringBuffer.append(DELIMITER);
                            z = true;
                            str3 = nextToken;
                            i = 0;
                        }
                    } else if (z || z || z) {
                        reportError(getParent(), parser, ErrorMsg.ATTR_VAL_TEMPLATE_ERR, str);
                    }
                    str3 = null;
                    i = 0;
                } else {
                    if (z) {
                        z = true;
                    } else if (z) {
                        z = true;
                    }
                    stringBuffer.append(str3);
                }
            } else {
                stringBuffer.append(str3);
            }
            str3 = str2;
            i = 0;
        }
        if (z) {
            reportError(getParent(), parser, ErrorMsg.ATTR_VAL_TEMPLATE_ERR, str);
        }
        StringTokenizer stringTokenizer2 = new StringTokenizer(stringBuffer.toString(), DELIMITER, true);
        while (stringTokenizer2.hasMoreTokens()) {
            String nextToken2 = stringTokenizer2.nextToken();
            if (nextToken2.equals(DELIMITER)) {
                addElement(parser.parseExpression(this, stringTokenizer2.nextToken()));
                stringTokenizer2.nextToken();
            } else {
                addElement(new LiteralExpr(nextToken2));
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        List<SyntaxTreeNode> contents = getContents();
        int size = contents.size();
        for (int i = 0; i < size; i++) {
            Expression expression = (Expression) contents.get(i);
            if (!expression.typeCheck(symbolTable).identicalTo(Type.String)) {
                contents.set(i, new CastExpr(expression, Type.String));
            }
        }
        Type type = Type.String;
        this._type = type;
        return type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("AVT:[");
        int elementCount = elementCount();
        for (int i = 0; i < elementCount; i++) {
            stringBuffer.append(elementAt(i).toString());
            if (i < elementCount - 1) {
                stringBuffer.append(' ');
            }
        }
        stringBuffer.append(']');
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Expression, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        if (elementCount() == 1) {
            ((Expression) elementAt(0)).translate(classGenerator, methodGenerator);
            return;
        }
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.STRING_BUFFER_CLASS, Constants.CONSTRUCTOR_NAME, "()V");
        INVOKEVIRTUAL invokevirtual = new INVOKEVIRTUAL(constantPool.addMethodref(Constants.STRING_BUFFER_CLASS, "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;"));
        int addMethodref2 = constantPool.addMethodref(Constants.STRING_BUFFER_CLASS, "toString", "()Ljava/lang/String;");
        instructionList.append(new NEW(constantPool.addClass(Constants.STRING_BUFFER_CLASS)));
        instructionList.append(DUP);
        instructionList.append(new INVOKESPECIAL(addMethodref));
        Iterator<SyntaxTreeNode> elements = elements();
        while (elements.hasNext()) {
            ((Expression) elements.next()).translate(classGenerator, methodGenerator);
            instructionList.append(invokevirtual);
        }
        instructionList.append(new INVOKEVIRTUAL(addMethodref2));
    }
}
