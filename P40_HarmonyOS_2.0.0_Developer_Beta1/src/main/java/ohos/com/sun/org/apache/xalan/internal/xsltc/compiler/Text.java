package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;

/* access modifiers changed from: package-private */
public final class Text extends Instruction {
    private boolean _escaping;
    private boolean _ignore;
    private String _text;
    private boolean _textElement;

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public boolean contextDependent() {
        return false;
    }

    public Text() {
        this._escaping = true;
        this._ignore = false;
        this._textElement = false;
        this._textElement = true;
    }

    public Text(String str) {
        this._escaping = true;
        this._ignore = false;
        this._textElement = false;
        this._text = str;
    }

    /* access modifiers changed from: protected */
    public String getText() {
        return this._text;
    }

    /* access modifiers changed from: protected */
    public void setText(String str) {
        if (this._text == null) {
            this._text = str;
            return;
        }
        this._text += str;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("Text");
        indent(i + 4);
        Util.println(this._text);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute(Constants.ATTRNAME_DISABLE_OUTPUT_ESCAPING);
        int i = 0;
        if (attribute != null && attribute.equals("yes")) {
            this._escaping = false;
        }
        parseChildren(parser);
        String str = this._text;
        if (str == null) {
            if (this._textElement) {
                this._text = "";
            } else {
                this._ignore = true;
            }
        } else if (this._textElement) {
            if (str.length() == 0) {
                this._ignore = true;
            }
        } else if (getParent() instanceof LiteralElement) {
            String attribute2 = ((LiteralElement) getParent()).getAttribute(Constants.ATTRNAME_XMLSPACE);
            if (attribute2 == null || !attribute2.equals(SchemaSymbols.ATTVAL_PRESERVE)) {
                int length = this._text.length();
                while (i < length && isWhitespace(this._text.charAt(i))) {
                    i++;
                }
                if (i == length) {
                    this._ignore = true;
                }
            }
        } else {
            int length2 = this._text.length();
            while (i < length2 && isWhitespace(this._text.charAt(i))) {
                i++;
            }
            if (i == length2) {
                this._ignore = true;
            }
        }
    }

    public void ignore() {
        this._ignore = true;
    }

    public boolean isIgnore() {
        return this._ignore;
    }

    public boolean isTextElement() {
        return this._textElement;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (!this._ignore) {
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "setEscaping", "(Z)Z");
            if (!this._escaping) {
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(new PUSH(constantPool, false));
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
            }
            instructionList.append(methodGenerator.loadHandler());
            if (!canLoadAsArrayOffsetLength()) {
                int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "characters", "(Ljava/lang/String;)V");
                instructionList.append(new PUSH(constantPool, this._text));
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 2));
            } else {
                int addInterfaceMethodref3 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "characters", "([CII)V");
                loadAsArrayOffsetLength(classGenerator, methodGenerator);
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref3, 4));
            }
            if (!this._escaping) {
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(SWAP);
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
                instructionList.append(POP);
            }
        }
        translateContents(classGenerator, methodGenerator);
    }

    public boolean canLoadAsArrayOffsetLength() {
        return this._text.length() <= 21845;
    }

    public void loadAsArrayOffsetLength(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        XSLTC xsltc = classGenerator.getParser().getXSLTC();
        int addCharacterData = xsltc.addCharacterData(this._text);
        this._text.length();
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.STATIC_CHAR_DATA_FIELD);
        sb.append(xsltc.getCharacterDataCount() - 1);
        instructionList.append(new GETSTATIC(constantPool.addFieldref(xsltc.getClassName(), sb.toString(), Constants.STATIC_CHAR_DATA_FIELD_SIG)));
        instructionList.append(new PUSH(constantPool, addCharacterData));
        instructionList.append(new PUSH(constantPool, this._text.length()));
    }
}
