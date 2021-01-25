package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public class NamedNodeMapImpl implements NamedNodeMap {
    Attr[] attrs;

    public NamedNodeMapImpl(Attr[] attrArr) {
        this.attrs = attrArr;
    }

    public Node getNamedItem(String str) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return null;
            }
            if (attrArr[i].getName().equals(str)) {
                return this.attrs[i];
            }
            i++;
        }
    }

    public Node item(int i) {
        if (i >= 0 || i <= getLength()) {
            return this.attrs[i];
        }
        return null;
    }

    public int getLength() {
        return this.attrs.length;
    }

    public Node getNamedItemNS(String str, String str2) {
        int i = 0;
        while (true) {
            Attr[] attrArr = this.attrs;
            if (i >= attrArr.length) {
                return null;
            }
            if (attrArr[i].getName().equals(str2) && this.attrs[i].getNamespaceURI().equals(str)) {
                return this.attrs[i];
            }
            i++;
        }
    }

    public Node setNamedItemNS(Node node) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Node setNamedItem(Node node) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Node removeNamedItem(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Node removeNamedItemNS(String str, String str2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }
}
