package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

import java.util.ArrayList;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSModelImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModel;

public class XSGrammarPool extends XMLGrammarPoolImpl {
    public XSModel toXSModel() {
        return toXSModel(1);
    }

    public XSModel toXSModel(short s) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.fGrammars.length; i++) {
            for (XMLGrammarPoolImpl.Entry entry = this.fGrammars[i]; entry != null; entry = entry.next) {
                if (entry.desc.getGrammarType().equals("http://www.w3.org/2001/XMLSchema")) {
                    arrayList.add(entry.grammar);
                }
            }
        }
        int size = arrayList.size();
        if (size == 0) {
            return toXSModel(new SchemaGrammar[0], s);
        }
        return toXSModel((SchemaGrammar[]) arrayList.toArray(new SchemaGrammar[size]), s);
    }

    /* access modifiers changed from: protected */
    public XSModel toXSModel(SchemaGrammar[] schemaGrammarArr, short s) {
        return new XSModelImpl(schemaGrammarArr, s);
    }
}
