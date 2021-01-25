package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

final class ReadOnlyGrammarPool implements XMLGrammarPool {
    private final XMLGrammarPool core;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void cacheGrammars(String str, Grammar[] grammarArr) {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void clear() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void lockPool() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public void unlockPool() {
    }

    public ReadOnlyGrammarPool(XMLGrammarPool xMLGrammarPool) {
        this.core = xMLGrammarPool;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
        return this.core.retrieveGrammar(xMLGrammarDescription);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
    public Grammar[] retrieveInitialGrammarSet(String str) {
        return this.core.retrieveInitialGrammarSet(str);
    }
}
