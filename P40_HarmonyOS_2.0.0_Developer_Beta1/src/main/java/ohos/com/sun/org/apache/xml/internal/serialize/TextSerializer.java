package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.IOException;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.AttributeList;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.SAXException;

public class TextSerializer extends BaseMarkupSerializer {
    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer
    public void comment(String str) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer
    public void comment(char[] cArr, int i, int i2) {
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer
    public String getEntityRef(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer
    public void processingInstructionIO(String str, String str2) throws IOException {
    }

    public TextSerializer() {
        super(new OutputFormat("text", (String) null, false));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer, ohos.com.sun.org.apache.xml.internal.serialize.Serializer
    public void setOutputFormat(OutputFormat outputFormat) {
        if (outputFormat == null) {
            outputFormat = new OutputFormat("text", (String) null, false);
        }
        super.setOutputFormat(outputFormat);
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        if (str3 != null) {
            str2 = str3;
        }
        startElement(str2, null);
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        if (str3 != null) {
            str2 = str3;
        }
        endElement(str2);
    }

    public void startElement(String str, AttributeList attributeList) throws SAXException {
        try {
            ElementState elementState = getElementState();
            if (isDocumentState() && !this._started) {
                startDocument(str);
            }
            enterElementState(null, null, str, elementState.preserveSpace);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endElement(String str) throws SAXException {
        try {
            endElementIO(str);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endElementIO(String str) throws IOException {
        getElementState();
        ElementState leaveElementState = leaveElementState();
        leaveElementState.afterElement = true;
        leaveElementState.empty = false;
        if (isDocumentState()) {
            this._printer.flush();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        try {
            ElementState content = content();
            content.inCData = false;
            content.doCData = false;
            printText(cArr, i, i2, true, true);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void characters(String str, boolean z) throws IOException {
        ElementState content = content();
        content.inCData = false;
        content.doCData = false;
        printText(str, true, true);
    }

    /* access modifiers changed from: protected */
    public void startDocument(String str) throws IOException {
        this._printer.leaveDTD();
        this._started = true;
        serializePreRoot();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer
    public void serializeElement(Element element) throws IOException {
        String tagName = element.getTagName();
        ElementState elementState = getElementState();
        if (isDocumentState() && !this._started) {
            startDocument(tagName);
        }
        boolean z = elementState.preserveSpace;
        if (element.hasChildNodes()) {
            enterElementState(null, null, tagName, z);
            for (Node firstChild = element.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                serializeNode(firstChild);
            }
            endElementIO(tagName);
        } else if (!isDocumentState()) {
            elementState.afterElement = true;
            elementState.empty = false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer
    public void serializeNode(Node node) throws IOException {
        short nodeType = node.getNodeType();
        if (nodeType != 1) {
            if (nodeType != 11) {
                if (nodeType != 3) {
                    if (nodeType != 4) {
                        if (nodeType == 5 || nodeType == 7 || nodeType == 8 || nodeType != 9) {
                            return;
                        }
                    } else if (node.getNodeValue() != null) {
                        characters(node.getNodeValue(), true);
                        return;
                    } else {
                        return;
                    }
                } else if (node.getNodeValue() != null) {
                    characters(node.getNodeValue(), true);
                    return;
                } else {
                    return;
                }
            }
            for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                serializeNode(firstChild);
            }
            return;
        }
        serializeElement((Element) node);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer
    public ElementState content() {
        ElementState elementState = getElementState();
        if (!isDocumentState()) {
            if (elementState.empty) {
                elementState.empty = false;
            }
            elementState.afterElement = false;
        }
        return elementState;
    }
}
