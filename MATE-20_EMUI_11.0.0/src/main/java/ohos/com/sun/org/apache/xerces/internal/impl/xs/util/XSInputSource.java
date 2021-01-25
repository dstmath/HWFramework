package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObject;

public final class XSInputSource extends XMLInputSource {
    private XSObject[] fComponents;
    private SchemaGrammar[] fGrammars;

    public XSInputSource(SchemaGrammar[] schemaGrammarArr) {
        super(null, null, null);
        this.fGrammars = schemaGrammarArr;
        this.fComponents = null;
    }

    public XSInputSource(XSObject[] xSObjectArr) {
        super(null, null, null);
        this.fGrammars = null;
        this.fComponents = xSObjectArr;
    }

    public SchemaGrammar[] getGrammars() {
        return this.fGrammars;
    }

    public void setGrammars(SchemaGrammar[] schemaGrammarArr) {
        this.fGrammars = schemaGrammarArr;
    }

    public XSObject[] getComponents() {
        return this.fComponents;
    }

    public void setComponents(XSObject[] xSObjectArr) {
        this.fComponents = xSObjectArr;
    }
}
