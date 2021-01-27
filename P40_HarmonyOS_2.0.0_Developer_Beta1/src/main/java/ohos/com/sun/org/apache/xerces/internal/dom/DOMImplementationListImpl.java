package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.Vector;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.DOMImplementationList;

public class DOMImplementationListImpl implements DOMImplementationList {
    private Vector fImplementations;

    public DOMImplementationListImpl() {
        this.fImplementations = new Vector();
    }

    public DOMImplementationListImpl(Vector vector) {
        this.fImplementations = vector;
    }

    public DOMImplementation item(int i) {
        try {
            return (DOMImplementation) this.fImplementations.elementAt(i);
        } catch (ArrayIndexOutOfBoundsException unused) {
            return null;
        }
    }

    public int getLength() {
        return this.fImplementations.size();
    }
}
