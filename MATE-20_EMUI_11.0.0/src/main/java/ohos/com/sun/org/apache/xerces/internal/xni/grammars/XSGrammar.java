package ohos.com.sun.org.apache.xerces.internal.xni.grammars;

import ohos.com.sun.org.apache.xerces.internal.xs.XSModel;

public interface XSGrammar extends Grammar {
    XSModel toXSModel();

    XSModel toXSModel(XSGrammar[] xSGrammarArr);
}
