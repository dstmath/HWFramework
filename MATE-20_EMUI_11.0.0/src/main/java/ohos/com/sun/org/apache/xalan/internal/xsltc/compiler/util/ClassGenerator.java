package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.classfile.Method;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ClassGen;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Parser;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet;

public class ClassGenerator extends ClassGen {
    protected static int INVALID_INDEX = -1;
    protected static final int TRANSLET_INDEX = 0;
    private final Instruction _aloadTranslet = new ALOAD(0);
    private final String _applyTemplatesSig;
    private final String _applyTemplatesSigForImport;
    private final String _domClass;
    private final String _domClassSig;
    private final Parser _parser;
    private Stylesheet _stylesheet;

    public boolean isExternal() {
        return false;
    }

    public ClassGenerator(String str, String str2, String str3, int i, String[] strArr, Stylesheet stylesheet) {
        super(str, str2, str3, i, strArr);
        this._stylesheet = stylesheet;
        this._parser = stylesheet.getParser();
        if (stylesheet.isMultiDocument()) {
            this._domClass = Constants.MULTI_DOM_CLASS;
            this._domClassSig = Constants.MULTI_DOM_SIG;
        } else {
            this._domClass = Constants.DOM_ADAPTER_CLASS;
            this._domClassSig = Constants.DOM_ADAPTER_SIG;
        }
        this._applyTemplatesSig = "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;)V";
        this._applyTemplatesSigForImport = Constants.ATTR_SET_SIG;
    }

    public final Parser getParser() {
        return this._parser;
    }

    public final Stylesheet getStylesheet() {
        return this._stylesheet;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ClassGen
    public final String getClassName() {
        return this._stylesheet.getClassName();
    }

    public Instruction loadTranslet() {
        return this._aloadTranslet;
    }

    public final String getDOMClass() {
        return this._domClass;
    }

    public final String getDOMClassSig() {
        return this._domClassSig;
    }

    public final String getApplyTemplatesSig() {
        return this._applyTemplatesSig;
    }

    public final String getApplyTemplatesSigForImport() {
        return this._applyTemplatesSigForImport;
    }

    public void addMethod(MethodGenerator methodGenerator) {
        Method[] generatedMethods;
        for (Method method : methodGenerator.getGeneratedMethods(this)) {
            addMethod(method);
        }
    }
}
