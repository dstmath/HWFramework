package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.CDATASection;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.EntityReference;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;

public class DefaultDocument extends NodeImpl implements Document {
    private String fDocumentURI = null;

    public Comment createComment(String str) {
        return null;
    }

    public DocumentFragment createDocumentFragment() {
        return null;
    }

    public Text createTextNode(String str) {
        return null;
    }

    public DocumentType getDoctype() {
        return null;
    }

    public Element getDocumentElement() {
        return null;
    }

    public Element getElementById(String str) {
        return null;
    }

    public NodeList getElementsByTagName(String str) {
        return null;
    }

    public NodeList getElementsByTagNameNS(String str, String str2) {
        return null;
    }

    public DOMImplementation getImplementation() {
        return null;
    }

    public String getInputEncoding() {
        return null;
    }

    public boolean getStrictErrorChecking() {
        return false;
    }

    public String getXmlEncoding() {
        return null;
    }

    public String getXmlVersion() {
        return null;
    }

    public Node importNode(Node node, boolean z) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Element createElement(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public CDATASection createCDATASection(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public ProcessingInstruction createProcessingInstruction(String str, String str2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Attr createAttribute(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public EntityReference createEntityReference(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Element createElementNS(String str, String str2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public Attr createAttributeNS(String str, String str2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public boolean getXmlStandalone() {
        throw new DOMException(9, "Method not supported");
    }

    public void setXmlStandalone(boolean z) {
        throw new DOMException(9, "Method not supported");
    }

    public void setXmlVersion(String str) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void setStrictErrorChecking(boolean z) {
        throw new DOMException(9, "Method not supported");
    }

    public String getDocumentURI() {
        return this.fDocumentURI;
    }

    public void setDocumentURI(String str) {
        this.fDocumentURI = str;
    }

    public Node adoptNode(Node node) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }

    public void normalizeDocument() {
        throw new DOMException(9, "Method not supported");
    }

    public DOMConfiguration getDomConfig() {
        throw new DOMException(9, "Method not supported");
    }

    public Node renameNode(Node node, String str, String str2) throws DOMException {
        throw new DOMException(9, "Method not supported");
    }
}
