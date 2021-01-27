package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

final class EmptyXMLSchema extends AbstractXMLSchema implements XMLGrammarPool {
    private static final Grammar[] ZERO_LENGTH_GRAMMAR_ARRAY = new Grammar[0];

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void cacheGrammars(String str, Grammar[] grammarArr) {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void clear() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public XMLGrammarPool getGrammarPool() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public boolean isFullyComposed() {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void lockPool() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void unlockPool() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar[] retrieveInitialGrammarSet(String str) {
        return ZERO_LENGTH_GRAMMAR_ARRAY;
    }
}
