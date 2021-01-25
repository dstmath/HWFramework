package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.Vector;
import ohos.org.w3c.dom.DOMStringList;

public class DOMStringListImpl implements DOMStringList {
    private Vector fStrings;

    public DOMStringListImpl() {
        this.fStrings = new Vector();
    }

    public DOMStringListImpl(Vector vector) {
        this.fStrings = vector;
    }

    public String item(int i) {
        try {
            return (String) this.fStrings.elementAt(i);
        } catch (ArrayIndexOutOfBoundsException unused) {
            return null;
        }
    }

    public int getLength() {
        return this.fStrings.size();
    }

    public boolean contains(String str) {
        return this.fStrings.contains(str);
    }

    public void add(String str) {
        this.fStrings.add(str);
    }
}
