package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Text;

public class DefaultText extends NodeImpl implements Text {
    public String getData() throws DOMException {
        return null;
    }

    public int getLength() {
        return 0;
    }

    public void setData(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public String substringData(int i, int i2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void appendData(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void insertData(int i, String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void deleteData(int i, int i2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void replaceData(int i, int i2, String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Text splitText(int i) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public boolean isElementContentWhitespace() {
        throw new DOMException(9, "Method not supported");
    }

    public String getWholeText() {
        throw new DOMException(9, "Method not supported");
    }

    public Text replaceWholeText(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }
}
