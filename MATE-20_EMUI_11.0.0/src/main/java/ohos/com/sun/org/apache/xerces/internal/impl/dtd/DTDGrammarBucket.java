package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;

public class DTDGrammarBucket {
    protected DTDGrammar fActiveGrammar;
    protected Map<XMLDTDDescription, DTDGrammar> fGrammars = new HashMap();
    protected boolean fIsStandalone;

    public void putGrammar(DTDGrammar dTDGrammar) {
        this.fGrammars.put((XMLDTDDescription) dTDGrammar.getGrammarDescription(), dTDGrammar);
    }

    public DTDGrammar getGrammar(XMLGrammarDescription xMLGrammarDescription) {
        return this.fGrammars.get((XMLDTDDescription) xMLGrammarDescription);
    }

    public void clear() {
        this.fGrammars.clear();
        this.fActiveGrammar = null;
        this.fIsStandalone = false;
    }

    /* access modifiers changed from: package-private */
    public void setStandalone(boolean z) {
        this.fIsStandalone = z;
    }

    /* access modifiers changed from: package-private */
    public boolean getStandalone() {
        return this.fIsStandalone;
    }

    /* access modifiers changed from: package-private */
    public void setActiveGrammar(DTDGrammar dTDGrammar) {
        this.fActiveGrammar = dTDGrammar;
    }

    /* access modifiers changed from: package-private */
    public DTDGrammar getActiveGrammar() {
        return this.fActiveGrammar;
    }
}
