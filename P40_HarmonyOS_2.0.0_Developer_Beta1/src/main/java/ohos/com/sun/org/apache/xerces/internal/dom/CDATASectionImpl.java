package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.CDATASection;

public class CDATASectionImpl extends TextImpl implements CDATASection {
    static final long serialVersionUID = 2372071297878177780L;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.TextImpl, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeName() {
        return "#cdata-section";
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.TextImpl, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 4;
    }

    public CDATASectionImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl, str);
    }
}
