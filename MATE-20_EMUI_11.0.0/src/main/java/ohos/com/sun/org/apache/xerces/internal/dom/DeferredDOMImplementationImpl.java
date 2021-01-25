package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.DOMImplementation;

public class DeferredDOMImplementationImpl extends DOMImplementationImpl {
    static DeferredDOMImplementationImpl singleton = new DeferredDOMImplementationImpl();

    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }
}
