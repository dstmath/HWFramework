package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

/* access modifiers changed from: package-private */
public final class CallTemplate extends Instruction {
    private Template _calleeTemplate = null;
    private QName _name;
    private SyntaxTreeNode[] _parameters = null;

    CallTemplate() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        System.out.print("CallTemplate");
        Util.println(" name " + this._name);
        displayContents(i + 4);
    }

    public boolean hasWithParams() {
        return elementCount() > 0;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute("name");
        if (attribute.length() > 0) {
            if (!XML11Char.isXML11ValidQName(attribute)) {
                parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute, (SyntaxTreeNode) this));
            }
            this._name = parser.getQNameIgnoreDefaultNs(attribute);
        } else {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
        }
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (symbolTable.lookupTemplate(this._name) != null) {
            typeCheckContents(symbolTable);
            return Type.Void;
        }
        throw new TypeCheckError(new ErrorMsg(ErrorMsg.TEMPLATE_UNDEF_ERR, (Object) this._name, (SyntaxTreeNode) this));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Stylesheet stylesheet = classGenerator.getStylesheet();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (stylesheet.hasLocalParams() || hasContents()) {
            this._calleeTemplate = getCalleeTemplate();
            if (this._calleeTemplate != null) {
                buildParameterList();
            } else {
                int addMethodref = constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.PUSH_PARAM_FRAME, "()V");
                instructionList.append(classGenerator.loadTranslet());
                instructionList.append(new INVOKEVIRTUAL(addMethodref));
                translateContents(classGenerator, methodGenerator);
            }
        }
        String className = stylesheet.getClassName();
        String escape = Util.escape(this._name.toString());
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadIterator());
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(methodGenerator.loadCurrentNode());
        StringBuffer stringBuffer = new StringBuffer("(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;I");
        int i = 0;
        if (this._calleeTemplate != null) {
            int length = this._parameters.length;
            for (int i2 = 0; i2 < length; i2++) {
                SyntaxTreeNode syntaxTreeNode = this._parameters[i2];
                stringBuffer.append(Constants.OBJECT_SIG);
                if (syntaxTreeNode instanceof Param) {
                    instructionList.append(ACONST_NULL);
                } else {
                    syntaxTreeNode.translate(classGenerator, methodGenerator);
                }
            }
        }
        stringBuffer.append(")V");
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(className, escape, stringBuffer.toString())));
        if (this._parameters != null) {
            while (true) {
                SyntaxTreeNode[] syntaxTreeNodeArr = this._parameters;
                if (i >= syntaxTreeNodeArr.length) {
                    break;
                }
                if (syntaxTreeNodeArr[i] instanceof WithParam) {
                    ((WithParam) syntaxTreeNodeArr[i]).releaseResultTree(classGenerator, methodGenerator);
                }
                i++;
            }
        }
        if (this._calleeTemplate != null) {
            return;
        }
        if (stylesheet.hasLocalParams() || hasContents()) {
            int addMethodref2 = constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.POP_PARAM_FRAME, "()V");
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        }
    }

    public Template getCalleeTemplate() {
        Template lookupTemplate = getXSLTC().getParser().getSymbolTable().lookupTemplate(this._name);
        if (lookupTemplate.isSimpleNamedTemplate()) {
            return lookupTemplate;
        }
        return null;
    }

    private void buildParameterList() {
        Vector<Param> parameters = this._calleeTemplate.getParameters();
        int size = parameters.size();
        this._parameters = new SyntaxTreeNode[size];
        for (int i = 0; i < size; i++) {
            this._parameters[i] = parameters.elementAt(i);
        }
        int elementCount = elementCount();
        for (int i2 = 0; i2 < elementCount; i2++) {
            SyntaxTreeNode elementAt = elementAt(i2);
            if (elementAt instanceof WithParam) {
                WithParam withParam = (WithParam) elementAt;
                QName name = withParam.getName();
                int i3 = 0;
                while (true) {
                    if (i3 >= size) {
                        break;
                    }
                    SyntaxTreeNode syntaxTreeNode = this._parameters[i3];
                    if ((syntaxTreeNode instanceof Param) && ((Param) syntaxTreeNode).getName().equals(name)) {
                        withParam.setDoParameterOptimization(true);
                        this._parameters[i3] = withParam;
                        break;
                    }
                    if ((syntaxTreeNode instanceof WithParam) && ((WithParam) syntaxTreeNode).getName().equals(name)) {
                        withParam.setDoParameterOptimization(true);
                        this._parameters[i3] = withParam;
                        break;
                    }
                    i3++;
                }
            }
        }
    }
}
