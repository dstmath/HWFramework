package org.apache.xml.serializer.dom3;

import java.util.Vector;
import org.w3c.dom.DOMStringList;

final class DOMStringListImpl implements DOMStringList {
    private Vector fStrings;

    DOMStringListImpl() {
        this.fStrings = new Vector();
    }

    DOMStringListImpl(Vector params) {
        this.fStrings = params;
    }

    DOMStringListImpl(String[] params) {
        this.fStrings = new Vector();
        if (params != null) {
            for (Object add : params) {
                this.fStrings.add(add);
            }
        }
    }

    public String item(int index) {
        try {
            return (String) this.fStrings.elementAt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public int getLength() {
        return this.fStrings.size();
    }

    public boolean contains(String param) {
        return this.fStrings.contains(param);
    }

    public void add(String param) {
        this.fStrings.add(param);
    }
}
