package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.lang.ref.WeakReference;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

final class WeakReferenceXMLSchema extends AbstractXMLSchema {
    private WeakReference fGrammarPool = new WeakReference(null);

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public boolean isFullyComposed() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer
    public synchronized XMLGrammarPool getGrammarPool() {
        XMLGrammarPool xMLGrammarPool;
        xMLGrammarPool = (XMLGrammarPool) this.fGrammarPool.get();
        if (xMLGrammarPool == null) {
            xMLGrammarPool = new SoftReferenceGrammarPool();
            this.fGrammarPool = new WeakReference(xMLGrammarPool);
        }
        return xMLGrammarPool;
    }
}
