package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

final class SimpleXMLSchema extends AbstractXMLSchema implements XMLGrammarPool {
    private static final Grammar[] ZERO_LENGTH_GRAMMAR_ARRAY = new Grammar[0];
    private Grammar fGrammar;
    private XMLGrammarDescription fGrammarDescription;
    private Grammar[] fGrammars;

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
    public void unlockPool() {
    }

    public SimpleXMLSchema(Grammar grammar) {
        this.fGrammar = grammar;
        this.fGrammars = new Grammar[]{grammar};
        this.fGrammarDescription = grammar.getGrammarDescription();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar[] retrieveInitialGrammarSet(String str) {
        return "http://www.w3.org/2001/XMLSchema".equals(str) ? (Grammar[]) this.fGrammars.clone() : ZERO_LENGTH_GRAMMAR_ARRAY;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
        if (this.fGrammarDescription.equals(xMLGrammarDescription)) {
            return this.fGrammar;
        }
        return null;
    }
}
