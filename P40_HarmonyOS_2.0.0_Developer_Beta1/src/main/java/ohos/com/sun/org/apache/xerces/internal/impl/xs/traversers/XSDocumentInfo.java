package ohos.com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.util.Stack;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationState;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaNamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaException;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

/* access modifiers changed from: package-private */
public class XSDocumentInfo {
    protected Stack SchemaNamespaceSupportStack = new Stack();
    protected XSAnnotationInfo fAnnotations = null;
    protected boolean fAreLocalAttributesQualified;
    protected boolean fAreLocalElementsQualified;
    protected XSAttributeChecker fAttrChecker;
    protected short fBlockDefault;
    protected short fFinalDefault;
    Vector fImportedNS = new Vector();
    protected boolean fIsChameleonSchema;
    protected SchemaNamespaceSupport fNamespaceSupport;
    protected SchemaNamespaceSupport fNamespaceSupportRoot;
    private Vector fReportedTNS = null;
    protected Object[] fSchemaAttrs;
    protected Element fSchemaElement;
    SymbolTable fSymbolTable = null;
    String fTargetNamespace;
    protected ValidationState fValidationContext = new ValidationState();

    XSDocumentInfo(Element element, XSAttributeChecker xSAttributeChecker, SymbolTable symbolTable) throws XMLSchemaException {
        this.fSchemaElement = element;
        initNamespaceSupport(element);
        boolean z = false;
        this.fIsChameleonSchema = false;
        this.fSymbolTable = symbolTable;
        this.fAttrChecker = xSAttributeChecker;
        if (element != null) {
            this.fSchemaAttrs = xSAttributeChecker.checkAttributes(element, true, this);
            Object[] objArr = this.fSchemaAttrs;
            if (objArr != null) {
                this.fAreLocalAttributesQualified = ((XInt) objArr[XSAttributeChecker.ATTIDX_AFORMDEFAULT]).intValue() == 1;
                this.fAreLocalElementsQualified = ((XInt) this.fSchemaAttrs[XSAttributeChecker.ATTIDX_EFORMDEFAULT]).intValue() == 1 ? true : z;
                this.fBlockDefault = ((XInt) this.fSchemaAttrs[XSAttributeChecker.ATTIDX_BLOCKDEFAULT]).shortValue();
                this.fFinalDefault = ((XInt) this.fSchemaAttrs[XSAttributeChecker.ATTIDX_FINALDEFAULT]).shortValue();
                this.fTargetNamespace = (String) this.fSchemaAttrs[XSAttributeChecker.ATTIDX_TARGETNAMESPACE];
                String str = this.fTargetNamespace;
                if (str != null) {
                    this.fTargetNamespace = symbolTable.addSymbol(str);
                }
                this.fNamespaceSupportRoot = new SchemaNamespaceSupport(this.fNamespaceSupport);
                this.fValidationContext.setNamespaceSupport(this.fNamespaceSupport);
                this.fValidationContext.setSymbolTable(symbolTable);
                return;
            }
            throw new XMLSchemaException(null, null);
        }
    }

    private void initNamespaceSupport(Element element) {
        this.fNamespaceSupport = new SchemaNamespaceSupport();
        this.fNamespaceSupport.reset();
        Node parentNode = element.getParentNode();
        while (parentNode != null && parentNode.getNodeType() == 1 && !parentNode.getNodeName().equals("DOCUMENT_NODE")) {
            NamedNodeMap attributes = ((Element) parentNode).getAttributes();
            int length = attributes != null ? attributes.getLength() : 0;
            for (int i = 0; i < length; i++) {
                Attr item = attributes.item(i);
                String namespaceURI = item.getNamespaceURI();
                if (namespaceURI != null && namespaceURI.equals("http://www.w3.org/2000/xmlns/")) {
                    String intern = item.getLocalName().intern();
                    if (intern == "xmlns") {
                        intern = "";
                    }
                    if (this.fNamespaceSupport.getURI(intern) == null) {
                        this.fNamespaceSupport.declarePrefix(intern, item.getValue().intern());
                    }
                }
            }
            parentNode = parentNode.getParentNode();
        }
    }

    /* access modifiers changed from: package-private */
    public void backupNSSupport(SchemaNamespaceSupport schemaNamespaceSupport) {
        this.SchemaNamespaceSupportStack.push(this.fNamespaceSupport);
        if (schemaNamespaceSupport == null) {
            schemaNamespaceSupport = this.fNamespaceSupportRoot;
        }
        this.fNamespaceSupport = new SchemaNamespaceSupport(schemaNamespaceSupport);
        this.fValidationContext.setNamespaceSupport(this.fNamespaceSupport);
    }

    /* access modifiers changed from: package-private */
    public void restoreNSSupport() {
        this.fNamespaceSupport = (SchemaNamespaceSupport) this.SchemaNamespaceSupportStack.pop();
        this.fValidationContext.setNamespaceSupport(this.fNamespaceSupport);
    }

    public String toString() {
        if (this.fTargetNamespace == null) {
            return "no targetNamspace";
        }
        return "targetNamespace is " + this.fTargetNamespace;
    }

    public void addAllowedNS(String str) {
        Vector vector = this.fImportedNS;
        if (str == null) {
            str = "";
        }
        vector.addElement(str);
    }

    public boolean isAllowedNS(String str) {
        Vector vector = this.fImportedNS;
        if (str == null) {
            str = "";
        }
        return vector.contains(str);
    }

    /* access modifiers changed from: package-private */
    public final boolean needReportTNSError(String str) {
        Vector vector = this.fReportedTNS;
        if (vector == null) {
            this.fReportedTNS = new Vector();
        } else if (vector.contains(str)) {
            return false;
        }
        this.fReportedTNS.addElement(str);
        return true;
    }

    /* access modifiers changed from: package-private */
    public Object[] getSchemaAttrs() {
        return this.fSchemaAttrs;
    }

    /* access modifiers changed from: package-private */
    public void returnSchemaAttrs() {
        this.fAttrChecker.returnAttrArray(this.fSchemaAttrs, null);
        this.fSchemaAttrs = null;
    }

    /* access modifiers changed from: package-private */
    public void addAnnotation(XSAnnotationInfo xSAnnotationInfo) {
        xSAnnotationInfo.next = this.fAnnotations;
        this.fAnnotations = xSAnnotationInfo;
    }

    /* access modifiers changed from: package-private */
    public XSAnnotationInfo getAnnotations() {
        return this.fAnnotations;
    }

    /* access modifiers changed from: package-private */
    public void removeAnnotations() {
        this.fAnnotations = null;
    }
}
