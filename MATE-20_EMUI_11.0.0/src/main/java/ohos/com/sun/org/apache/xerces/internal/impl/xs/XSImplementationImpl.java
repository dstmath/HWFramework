package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSException;
import ohos.com.sun.org.apache.xerces.internal.xs.XSImplementation;
import ohos.com.sun.org.apache.xerces.internal.xs.XSLoader;
import ohos.org.w3c.dom.DOMImplementation;

public class XSImplementationImpl extends CoreDOMImplementationImpl implements XSImplementation {
    static XSImplementationImpl singleton = new XSImplementationImpl();

    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl
    public boolean hasFeature(String str, String str2) {
        return (str.equalsIgnoreCase("XS-Loader") && (str2 == null || str2.equals("1.0"))) || super.hasFeature(str, str2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSImplementation
    public XSLoader createXSLoader(StringList stringList) throws XSException {
        XSLoaderImpl xSLoaderImpl = new XSLoaderImpl();
        if (stringList == null) {
            return xSLoaderImpl;
        }
        for (int i = 0; i < stringList.getLength(); i++) {
            if (!stringList.item(i).equals("1.0")) {
                throw new XSException(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "FEATURE_NOT_SUPPORTED", new Object[]{stringList.item(i)}));
            }
        }
        return xSLoaderImpl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSImplementation
    public StringList getRecognizedVersions() {
        return new StringListImpl(new String[]{"1.0"}, 1);
    }
}
