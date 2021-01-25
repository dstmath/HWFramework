package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.List;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

/* access modifiers changed from: package-private */
public final class UnsupportedElement extends SyntaxTreeNode {
    private Vector _fallbacks = null;
    private boolean _isExtension = false;
    private ErrorMsg _message = null;

    public UnsupportedElement(String str, String str2, String str3, boolean z) {
        super(str, str2, str3);
        this._isExtension = z;
    }

    public void setErrorMessage(ErrorMsg errorMsg) {
        this._message = errorMsg;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("Unsupported element = " + this._qname.getNamespace() + ":" + this._qname.getLocalPart());
        displayContents(i + 4);
    }

    private void processFallbacks(Parser parser) {
        List<SyntaxTreeNode> contents = getContents();
        if (contents != null) {
            int size = contents.size();
            for (int i = 0; i < size; i++) {
                SyntaxTreeNode syntaxTreeNode = contents.get(i);
                if (syntaxTreeNode instanceof Fallback) {
                    Fallback fallback = (Fallback) syntaxTreeNode;
                    fallback.activate();
                    fallback.parseContents(parser);
                    if (this._fallbacks == null) {
                        this._fallbacks = new Vector();
                    }
                    this._fallbacks.addElement(syntaxTreeNode);
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        processFallbacks(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Vector vector = this._fallbacks;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                ((Fallback) this._fallbacks.elementAt(i)).typeCheck(symbolTable);
            }
        }
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Vector vector = this._fallbacks;
        if (vector != null) {
            int size = vector.size();
            for (int i = 0; i < size; i++) {
                ((Fallback) this._fallbacks.elementAt(i)).translate(classGenerator, methodGenerator);
            }
            return;
        }
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "unsupported_ElementF", "(Ljava/lang/String;Z)V");
        instructionList.append(new PUSH(constantPool, getQName().toString()));
        instructionList.append(new PUSH(constantPool, this._isExtension));
        instructionList.append(new INVOKESTATIC(addMethodref));
    }
}
