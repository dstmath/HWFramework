package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.serializer.ElemDesc;

/* access modifiers changed from: package-private */
public final class LiteralAttribute extends Instruction {
    private final String _name;
    private final AttributeValue _value;

    public LiteralAttribute(String str, String str2, Parser parser, SyntaxTreeNode syntaxTreeNode) {
        this._name = str;
        setParent(syntaxTreeNode);
        this._value = AttributeValue.create(this, str2, parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("LiteralAttribute name=" + this._name + " value=" + this._value);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._value.typeCheck(symbolTable);
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public boolean contextDependent() {
        return this._value.contextDependent();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new PUSH(constantPool, this._name));
        this._value.translate(classGenerator, methodGenerator);
        SyntaxTreeNode parent = getParent();
        if (parent instanceof LiteralElement) {
            LiteralElement literalElement = (LiteralElement) parent;
            if (literalElement.allAttributesUnique()) {
                ElemDesc elemDesc = literalElement.getElemDesc();
                int i = 2;
                boolean z = false;
                if (elemDesc != null) {
                    if (elemDesc.isAttrFlagSet(this._name, 4)) {
                        z = true;
                    } else if (elemDesc.isAttrFlagSet(this._name, 2)) {
                        i = 4;
                    }
                    AttributeValue attributeValue = this._value;
                    if ((attributeValue instanceof SimpleAttributeValue) && !hasBadChars(((SimpleAttributeValue) attributeValue).toString()) && !z) {
                        i |= 1;
                    }
                    instructionList.append(new PUSH(constantPool, i));
                    instructionList.append(methodGenerator.uniqueAttribute());
                    return;
                }
                i = 0;
                AttributeValue attributeValue2 = this._value;
                i |= 1;
                instructionList.append(new PUSH(constantPool, i));
                instructionList.append(methodGenerator.uniqueAttribute());
                return;
            }
        }
        instructionList.append(methodGenerator.attribute());
    }

    private boolean hasBadChars(String str) {
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            if (c < ' ' || '~' < c || c == '<' || c == '>' || c == '&' || c == '\"') {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return this._name;
    }

    public AttributeValue getValue() {
        return this._value;
    }
}
