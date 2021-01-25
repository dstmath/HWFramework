package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

final class XMLSchema extends AbstractXMLSchema {
    private final XMLGrammarPool fGrammarPool;

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public boolean isFullyComposed() {
        return true;
    }

    public XMLSchema(XMLGrammarPool xMLGrammarPool) {
        this.fGrammarPool = xMLGrammarPool;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public XMLGrammarPool getGrammarPool() {
        return this.fGrammarPool;
    }
}
