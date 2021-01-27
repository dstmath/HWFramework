package ohos.com.sun.org.apache.xerces.internal.xs;

import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.ls.LSInput;

public interface XSLoader {
    DOMConfiguration getConfig();

    XSModel load(LSInput lSInput);

    XSModel loadInputList(LSInputList lSInputList);

    XSModel loadURI(String str);

    XSModel loadURIList(StringList stringList);
}
