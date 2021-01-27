package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime;

import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.org.xml.sax.AttributeList;

public final class Attributes implements AttributeList {
    private DOM _document;
    private int _element;

    public int getLength() {
        return 0;
    }

    public String getName(int i) {
        return null;
    }

    public String getType(int i) {
        return null;
    }

    public String getType(String str) {
        return null;
    }

    public String getValue(int i) {
        return null;
    }

    public String getValue(String str) {
        return null;
    }

    public Attributes(DOM dom, int i) {
        this._element = i;
        this._document = dom;
    }
}
