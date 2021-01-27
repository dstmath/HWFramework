package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;

public interface XSGrammarPoolContainer {
    Boolean getFeature(String str);

    XMLGrammarPool getGrammarPool();

    Object getProperty(String str);

    boolean isFullyComposed();

    void setFeature(String str, boolean z);

    void setProperty(String str, Object obj);
}
