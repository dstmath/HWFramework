package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public class SchemaDOM extends DefaultDocument {
    static final int relationsColResizeFactor = 10;
    static final int relationsRowResizeFactor = 15;
    int currLoc;
    private StringBuffer fAnnotationBuffer = null;
    boolean hidden;
    boolean inCDATA;
    int nextFreeLoc;
    ElementImpl parent;
    NodeImpl[][] relations;

    public void printDOM() {
    }

    public SchemaDOM() {
        reset();
    }

    public ElementImpl startElement(QName qName, XMLAttributes xMLAttributes, int i, int i2, int i3) {
        ElementImpl elementImpl = new ElementImpl(i, i2, i3);
        processElement(qName, xMLAttributes, elementImpl);
        this.parent = elementImpl;
        return elementImpl;
    }

    public ElementImpl emptyElement(QName qName, XMLAttributes xMLAttributes, int i, int i2, int i3) {
        ElementImpl elementImpl = new ElementImpl(i, i2, i3);
        processElement(qName, xMLAttributes, elementImpl);
        return elementImpl;
    }

    public ElementImpl startElement(QName qName, XMLAttributes xMLAttributes, int i, int i2) {
        return startElement(qName, xMLAttributes, i, i2, -1);
    }

    public ElementImpl emptyElement(QName qName, XMLAttributes xMLAttributes, int i, int i2) {
        return emptyElement(qName, xMLAttributes, i, i2, -1);
    }

    private void processElement(QName qName, XMLAttributes xMLAttributes, ElementImpl elementImpl) {
        elementImpl.prefix = qName.prefix;
        elementImpl.localpart = qName.localpart;
        elementImpl.rawname = qName.rawname;
        elementImpl.uri = qName.uri;
        elementImpl.schemaDOM = this;
        Attr[] attrArr = new Attr[xMLAttributes.getLength()];
        for (int i = 0; i < xMLAttributes.getLength(); i++) {
            attrArr[i] = new AttrImpl(elementImpl, xMLAttributes.getPrefix(i), xMLAttributes.getLocalName(i), xMLAttributes.getQName(i), xMLAttributes.getURI(i), xMLAttributes.getValue(i));
        }
        elementImpl.attrs = attrArr;
        if (this.nextFreeLoc == this.relations.length) {
            resizeRelations();
        }
        NodeImpl[][] nodeImplArr = this.relations;
        NodeImpl nodeImpl = nodeImplArr[this.currLoc][0];
        ElementImpl elementImpl2 = this.parent;
        if (nodeImpl != elementImpl2) {
            int i2 = this.nextFreeLoc;
            nodeImplArr[i2][0] = elementImpl2;
            this.nextFreeLoc = i2 + 1;
            this.currLoc = i2;
        }
        boolean z = true;
        int i3 = 1;
        while (true) {
            NodeImpl[][] nodeImplArr2 = this.relations;
            int i4 = this.currLoc;
            if (i3 >= nodeImplArr2[i4].length) {
                z = false;
                break;
            } else if (nodeImplArr2[i4][i3] == null) {
                break;
            } else {
                i3++;
            }
        }
        if (!z) {
            resizeRelations(this.currLoc);
        }
        NodeImpl[][] nodeImplArr3 = this.relations;
        int i5 = this.currLoc;
        nodeImplArr3[i5][i3] = elementImpl;
        this.parent.parentRow = i5;
        elementImpl.row = i5;
        elementImpl.col = i3;
    }

    public void endElement() {
        this.currLoc = this.parent.row;
        this.parent = (ElementImpl) this.relations[this.currLoc][0];
    }

    /* access modifiers changed from: package-private */
    public void comment(XMLString xMLString) {
        this.fAnnotationBuffer.append("<!--");
        if (xMLString.length > 0) {
            this.fAnnotationBuffer.append(xMLString.ch, xMLString.offset, xMLString.length);
        }
        this.fAnnotationBuffer.append("-->");
    }

    /* access modifiers changed from: package-private */
    public void processingInstruction(String str, XMLString xMLString) {
        StringBuffer stringBuffer = this.fAnnotationBuffer;
        stringBuffer.append("<?");
        stringBuffer.append(str);
        if (xMLString.length > 0) {
            StringBuffer stringBuffer2 = this.fAnnotationBuffer;
            stringBuffer2.append(' ');
            stringBuffer2.append(xMLString.ch, xMLString.offset, xMLString.length);
        }
        this.fAnnotationBuffer.append("?>");
    }

    /* access modifiers changed from: package-private */
    public void characters(XMLString xMLString) {
        if (!this.inCDATA) {
            StringBuffer stringBuffer = this.fAnnotationBuffer;
            for (int i = xMLString.offset; i < xMLString.offset + xMLString.length; i++) {
                char c = xMLString.ch[i];
                if (c == '&') {
                    stringBuffer.append(SerializerConstants.ENTITY_AMP);
                } else if (c == '<') {
                    stringBuffer.append(SerializerConstants.ENTITY_LT);
                } else if (c == '>') {
                    stringBuffer.append(SerializerConstants.ENTITY_GT);
                } else if (c == '\r') {
                    stringBuffer.append("&#xD;");
                } else {
                    stringBuffer.append(c);
                }
            }
            return;
        }
        this.fAnnotationBuffer.append(xMLString.ch, xMLString.offset, xMLString.length);
    }

    /* access modifiers changed from: package-private */
    public void charactersRaw(String str) {
        this.fAnnotationBuffer.append(str);
    }

    /* access modifiers changed from: package-private */
    public void endAnnotation(QName qName, ElementImpl elementImpl) {
        StringBuffer stringBuffer = this.fAnnotationBuffer;
        stringBuffer.append("\n</");
        stringBuffer.append(qName.rawname);
        stringBuffer.append(">");
        elementImpl.fAnnotation = this.fAnnotationBuffer.toString();
        this.fAnnotationBuffer = null;
    }

    /* access modifiers changed from: package-private */
    public void endAnnotationElement(QName qName) {
        endAnnotationElement(qName.rawname);
    }

    /* access modifiers changed from: package-private */
    public void endAnnotationElement(String str) {
        StringBuffer stringBuffer = this.fAnnotationBuffer;
        stringBuffer.append("</");
        stringBuffer.append(str);
        stringBuffer.append(">");
    }

    /* access modifiers changed from: package-private */
    public void endSyntheticAnnotationElement(QName qName, boolean z) {
        endSyntheticAnnotationElement(qName.rawname, z);
    }

    /* access modifiers changed from: package-private */
    public void endSyntheticAnnotationElement(String str, boolean z) {
        if (z) {
            StringBuffer stringBuffer = this.fAnnotationBuffer;
            stringBuffer.append("\n</");
            stringBuffer.append(str);
            stringBuffer.append(">");
            this.parent.fSyntheticAnnotation = this.fAnnotationBuffer.toString();
            this.fAnnotationBuffer = null;
            return;
        }
        StringBuffer stringBuffer2 = this.fAnnotationBuffer;
        stringBuffer2.append("</");
        stringBuffer2.append(str);
        stringBuffer2.append(">");
    }

    /* access modifiers changed from: package-private */
    public void startAnnotationCDATA() {
        this.inCDATA = true;
        this.fAnnotationBuffer.append("<![CDATA[");
    }

    /* access modifiers changed from: package-private */
    public void endAnnotationCDATA() {
        this.fAnnotationBuffer.append("]]>");
        this.inCDATA = false;
    }

    private void resizeRelations() {
        NodeImpl[][] nodeImplArr = this.relations;
        NodeImpl[][] nodeImplArr2 = new NodeImpl[(nodeImplArr.length + 15)][];
        System.arraycopy(nodeImplArr, 0, nodeImplArr2, 0, nodeImplArr.length);
        for (int length = this.relations.length; length < nodeImplArr2.length; length++) {
            nodeImplArr2[length] = new NodeImpl[10];
        }
        this.relations = nodeImplArr2;
    }

    private void resizeRelations(int i) {
        NodeImpl[][] nodeImplArr = this.relations;
        NodeImpl[] nodeImplArr2 = new NodeImpl[(nodeImplArr[i].length + 10)];
        System.arraycopy(nodeImplArr[i], 0, nodeImplArr2, 0, nodeImplArr[i].length);
        this.relations[i] = nodeImplArr2;
    }

    public void reset() {
        if (this.relations != null) {
            for (int i = 0; i < this.relations.length; i++) {
                int i2 = 0;
                while (true) {
                    NodeImpl[][] nodeImplArr = this.relations;
                    if (i2 >= nodeImplArr[i].length) {
                        break;
                    }
                    nodeImplArr[i][i2] = null;
                    i2++;
                }
            }
        }
        this.relations = new NodeImpl[15][];
        this.parent = new ElementImpl(0, 0, 0);
        this.parent.rawname = "DOCUMENT_NODE";
        this.currLoc = 0;
        this.nextFreeLoc = 1;
        this.inCDATA = false;
        for (int i3 = 0; i3 < 15; i3++) {
            this.relations[i3] = new NodeImpl[10];
        }
        this.relations[this.currLoc][0] = this.parent;
    }

    public static void traverse(Node node, int i) {
        indent(i);
        PrintStream printStream = System.out;
        printStream.print("<" + node.getNodeName());
        if (node.hasAttributes()) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i2 = 0; i2 < attributes.getLength(); i2++) {
                PrintStream printStream2 = System.out;
                printStream2.print("  " + attributes.item(i2).getName() + "=\"" + attributes.item(i2).getValue() + "\"");
            }
        }
        if (node.hasChildNodes()) {
            System.out.println(">");
            int i3 = i + 4;
            for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                traverse(firstChild, i3);
            }
            indent(i3 - 4);
            PrintStream printStream3 = System.out;
            printStream3.println("</" + node.getNodeName() + ">");
            return;
        }
        System.out.println("/>");
    }

    public static void indent(int i) {
        for (int i2 = 0; i2 < i; i2++) {
            System.out.print(' ');
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultDocument
    public Element getDocumentElement() {
        return (ElementImpl) this.relations[0][1];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultDocument
    public DOMImplementation getImplementation() {
        return SchemaDOMImplementation.getDOMImplementation();
    }

    /* access modifiers changed from: package-private */
    public void startAnnotation(QName qName, XMLAttributes xMLAttributes, NamespaceContext namespaceContext) {
        startAnnotation(qName.rawname, xMLAttributes, namespaceContext);
    }

    /* access modifiers changed from: package-private */
    public void startAnnotation(String str, XMLAttributes xMLAttributes, NamespaceContext namespaceContext) {
        if (this.fAnnotationBuffer == null) {
            this.fAnnotationBuffer = new StringBuffer(256);
        }
        StringBuffer stringBuffer = this.fAnnotationBuffer;
        stringBuffer.append("<");
        stringBuffer.append(str);
        stringBuffer.append(" ");
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < xMLAttributes.getLength(); i++) {
            String value = xMLAttributes.getValue(i);
            String prefix = xMLAttributes.getPrefix(i);
            String qName = xMLAttributes.getQName(i);
            if (prefix == XMLSymbols.PREFIX_XMLNS || qName == XMLSymbols.PREFIX_XMLNS) {
                arrayList.add(prefix == XMLSymbols.PREFIX_XMLNS ? xMLAttributes.getLocalName(i) : XMLSymbols.EMPTY_STRING);
            }
            StringBuffer stringBuffer2 = this.fAnnotationBuffer;
            stringBuffer2.append(qName);
            stringBuffer2.append("=\"");
            stringBuffer2.append(processAttValue(value));
            stringBuffer2.append("\" ");
        }
        Enumeration allPrefixes = namespaceContext.getAllPrefixes();
        while (allPrefixes.hasMoreElements()) {
            String str2 = (String) allPrefixes.nextElement();
            String uri = namespaceContext.getURI(str2);
            if (uri == null) {
                uri = XMLSymbols.EMPTY_STRING;
            }
            if (!arrayList.contains(str2)) {
                if (str2 == XMLSymbols.EMPTY_STRING) {
                    StringBuffer stringBuffer3 = this.fAnnotationBuffer;
                    stringBuffer3.append("xmlns");
                    stringBuffer3.append("=\"");
                    stringBuffer3.append(processAttValue(uri));
                    stringBuffer3.append("\" ");
                } else {
                    StringBuffer stringBuffer4 = this.fAnnotationBuffer;
                    stringBuffer4.append("xmlns:");
                    stringBuffer4.append(str2);
                    stringBuffer4.append("=\"");
                    stringBuffer4.append(processAttValue(uri));
                    stringBuffer4.append("\" ");
                }
            }
        }
        this.fAnnotationBuffer.append(">\n");
    }

    /* access modifiers changed from: package-private */
    public void startAnnotationElement(QName qName, XMLAttributes xMLAttributes) {
        startAnnotationElement(qName.rawname, xMLAttributes);
    }

    /* access modifiers changed from: package-private */
    public void startAnnotationElement(String str, XMLAttributes xMLAttributes) {
        StringBuffer stringBuffer = this.fAnnotationBuffer;
        stringBuffer.append("<");
        stringBuffer.append(str);
        for (int i = 0; i < xMLAttributes.getLength(); i++) {
            String value = xMLAttributes.getValue(i);
            StringBuffer stringBuffer2 = this.fAnnotationBuffer;
            stringBuffer2.append(" ");
            stringBuffer2.append(xMLAttributes.getQName(i));
            stringBuffer2.append("=\"");
            stringBuffer2.append(processAttValue(value));
            stringBuffer2.append("\"");
        }
        this.fAnnotationBuffer.append(">");
    }

    private static String processAttValue(String str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (charAt == '\"' || charAt == '<' || charAt == '&' || charAt == '\t' || charAt == '\n' || charAt == '\r') {
                return escapeAttValue(str, i);
            }
        }
        return str;
    }

    private static String escapeAttValue(String str, int i) {
        int length = str.length();
        StringBuffer stringBuffer = new StringBuffer(length);
        stringBuffer.append(str.substring(0, i));
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == '\"') {
                stringBuffer.append(SerializerConstants.ENTITY_QUOT);
            } else if (charAt == '<') {
                stringBuffer.append(SerializerConstants.ENTITY_LT);
            } else if (charAt == '&') {
                stringBuffer.append(SerializerConstants.ENTITY_AMP);
            } else if (charAt == '\t') {
                stringBuffer.append("&#x9;");
            } else if (charAt == '\n') {
                stringBuffer.append(SerializerConstants.ENTITY_CRLF);
            } else if (charAt == '\r') {
                stringBuffer.append("&#xD;");
            } else {
                stringBuffer.append(charAt);
            }
            i++;
        }
        return stringBuffer.toString();
    }
}
