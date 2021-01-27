package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.DOMImplementationList;

public class DOMXSImplementationSourceImpl extends DOMImplementationSourceImpl {
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl
    public DOMImplementation getDOMImplementation(String str) {
        DOMImplementation dOMImplementation = super.getDOMImplementation(str);
        if (dOMImplementation != null) {
            return dOMImplementation;
        }
        DOMImplementation dOMImplementation2 = PSVIDOMImplementationImpl.getDOMImplementation();
        if (testImpl(dOMImplementation2, str)) {
            return dOMImplementation2;
        }
        DOMImplementation dOMImplementation3 = XSImplementationImpl.getDOMImplementation();
        if (testImpl(dOMImplementation3, str)) {
            return dOMImplementation3;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl
    public DOMImplementationList getDOMImplementationList(String str) {
        Vector vector = new Vector();
        DOMImplementationList dOMImplementationList = super.getDOMImplementationList(str);
        for (int i = 0; i < dOMImplementationList.getLength(); i++) {
            vector.addElement(dOMImplementationList.item(i));
        }
        DOMImplementation dOMImplementation = PSVIDOMImplementationImpl.getDOMImplementation();
        if (testImpl(dOMImplementation, str)) {
            vector.addElement(dOMImplementation);
        }
        DOMImplementation dOMImplementation2 = XSImplementationImpl.getDOMImplementation();
        if (testImpl(dOMImplementation2, str)) {
            vector.addElement(dOMImplementation2);
        }
        return new DOMImplementationListImpl(vector);
    }
}
