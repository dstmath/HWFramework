package ohos.com.sun.org.apache.xml.internal.resolver.readers;

import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xml.internal.resolver.Catalog;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import ohos.com.sun.org.apache.xml.internal.resolver.CatalogException;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import ohos.com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import ohos.data.search.model.IndexType;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;

public class OASISXMLCatalogReader extends SAXCatalogReader implements SAXCatalogParser {
    public static final String namespaceName = "urn:oasis:names:tc:entity:xmlns:xml:catalog";
    public static final String tr9401NamespaceName = "urn:oasis:names:tc:entity:xmlns:tr9401:catalog";
    protected Stack baseURIStack = new Stack();
    protected Catalog catalog = null;
    protected Stack namespaceStack = new Stack();
    protected Stack overrideStack = new Stack();

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void characters(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void endDocument() throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void endPrefixMapping(String str) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void processingInstruction(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void setDocumentLocator(Locator locator) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void skippedEntity(String str) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void startPrefixMapping(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogParser
    public void setCatalog(Catalog catalog2) {
        this.catalog = catalog2;
        this.debug = catalog2.getCatalogManager().debug;
    }

    public Catalog getCatalog() {
        return this.catalog;
    }

    /* access modifiers changed from: protected */
    public boolean inExtensionNamespace() {
        Enumeration elements = this.namespaceStack.elements();
        boolean z = false;
        while (!z && elements.hasMoreElements()) {
            String str = (String) elements.nextElement();
            boolean z2 = true;
            if (str != null && (str.equals(tr9401NamespaceName) || str.equals(namespaceName))) {
                z2 = false;
            }
            z = z2;
        }
        return z;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void startDocument() throws SAXException {
        this.baseURIStack.push(this.catalog.getCurrentBase());
        this.overrideStack.push(this.catalog.getDefaultOverride());
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x035c A[SYNTHETIC, Splitter:B:107:0x035c] */
    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        String str4;
        int i;
        int i2;
        String str5;
        Vector vector = new Vector();
        this.namespaceStack.push(str);
        boolean inExtensionNamespace = inExtensionNamespace();
        if (str == null || !namespaceName.equals(str) || inExtensionNamespace) {
            str4 = "xml:base";
            i = -1;
        } else {
            if (attributes.getValue("xml:base") != null) {
                String value = attributes.getValue("xml:base");
                int i3 = Catalog.BASE;
                vector.add(value);
                this.baseURIStack.push(value);
                this.debug.message(4, "xml:base", value);
                try {
                    this.catalog.addEntry(new CatalogEntry(i3, vector));
                } catch (CatalogException e) {
                    if (e.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry (base)", str2);
                    }
                }
                vector = new Vector();
            } else {
                Stack stack = this.baseURIStack;
                stack.push(stack.peek());
            }
            if ((str2.equals("catalog") || str2.equals("group")) && attributes.getValue("prefer") != null) {
                String value2 = attributes.getValue("prefer");
                if (value2.equals("public")) {
                    str5 = "yes";
                } else if (value2.equals("system")) {
                    str5 = IndexType.NO;
                } else {
                    this.debug.message(1, "Invalid prefer: must be 'system' or 'public'", str2);
                    str5 = this.catalog.getDefaultOverride();
                }
                int i4 = Catalog.OVERRIDE;
                vector.add(str5);
                this.overrideStack.push(str5);
                str4 = "xml:base";
                this.debug.message(4, "override", str5);
                try {
                    this.catalog.addEntry(new CatalogEntry(i4, vector));
                } catch (CatalogException e2) {
                    if (e2.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e2.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry (override)", str2);
                    }
                }
                vector = new Vector();
            } else {
                str4 = "xml:base";
                Stack stack2 = this.overrideStack;
                stack2.push(stack2.peek());
            }
            if (str2.equals("delegatePublic")) {
                if (checkAttributes(attributes, "publicIdStartString", "catalog")) {
                    i2 = Catalog.DELEGATE_PUBLIC;
                    vector.add(attributes.getValue("publicIdStartString"));
                    vector.add(attributes.getValue("catalog"));
                    this.debug.message(4, "delegatePublic", PublicId.normalize(attributes.getValue("publicIdStartString")), attributes.getValue("catalog"));
                }
                i = -1;
                if (i >= 0) {
                    try {
                        this.catalog.addEntry(new CatalogEntry(i, vector));
                    } catch (CatalogException e3) {
                        if (e3.getExceptionType() == 3) {
                            this.debug.message(1, "Invalid catalog entry type", str2);
                        } else if (e3.getExceptionType() == 2) {
                            this.debug.message(1, "Invalid catalog entry", str2);
                        }
                    }
                }
                vector = vector;
            } else if (str2.equals("delegateSystem")) {
                if (checkAttributes(attributes, "systemIdStartString", "catalog")) {
                    i2 = Catalog.DELEGATE_SYSTEM;
                    vector.add(attributes.getValue("systemIdStartString"));
                    vector.add(attributes.getValue("catalog"));
                    this.debug.message(4, "delegateSystem", attributes.getValue("systemIdStartString"), attributes.getValue("catalog"));
                }
                i = -1;
                if (i >= 0) {
                }
                vector = vector;
            } else if (str2.equals("delegateURI")) {
                if (checkAttributes(attributes, "uriStartString", "catalog")) {
                    i2 = Catalog.DELEGATE_URI;
                    vector.add(attributes.getValue("uriStartString"));
                    vector.add(attributes.getValue("catalog"));
                    this.debug.message(4, "delegateURI", attributes.getValue("uriStartString"), attributes.getValue("catalog"));
                }
                i = -1;
                if (i >= 0) {
                }
                vector = vector;
            } else if (str2.equals("rewriteSystem")) {
                if (checkAttributes(attributes, "systemIdStartString", "rewritePrefix")) {
                    i2 = Catalog.REWRITE_SYSTEM;
                    vector.add(attributes.getValue("systemIdStartString"));
                    vector.add(attributes.getValue("rewritePrefix"));
                    this.debug.message(4, "rewriteSystem", attributes.getValue("systemIdStartString"), attributes.getValue("rewritePrefix"));
                }
                i = -1;
                if (i >= 0) {
                }
                vector = vector;
            } else if (str2.equals("systemSuffix")) {
                if (checkAttributes(attributes, "systemIdSuffix", Constants.ELEMNAME_URL_STRING)) {
                    i2 = Catalog.SYSTEM_SUFFIX;
                    vector.add(attributes.getValue("systemIdSuffix"));
                    vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
                    this.debug.message(4, "systemSuffix", attributes.getValue("systemIdSuffix"), attributes.getValue(Constants.ELEMNAME_URL_STRING));
                }
                i = -1;
                if (i >= 0) {
                }
                vector = vector;
            } else if (str2.equals("rewriteURI")) {
                if (checkAttributes(attributes, "uriStartString", "rewritePrefix")) {
                    i2 = Catalog.REWRITE_URI;
                    vector.add(attributes.getValue("uriStartString"));
                    vector.add(attributes.getValue("rewritePrefix"));
                    this.debug.message(4, "rewriteURI", attributes.getValue("uriStartString"), attributes.getValue("rewritePrefix"));
                }
                i = -1;
                if (i >= 0) {
                }
                vector = vector;
            } else {
                if (str2.equals("uriSuffix")) {
                    if (checkAttributes(attributes, "uriSuffix", Constants.ELEMNAME_URL_STRING)) {
                        i = Catalog.URI_SUFFIX;
                        vector.add(attributes.getValue("uriSuffix"));
                        vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
                        this.debug.message(4, "uriSuffix", attributes.getValue("uriSuffix"), attributes.getValue(Constants.ELEMNAME_URL_STRING));
                        if (i >= 0) {
                        }
                        vector = vector;
                    }
                } else if (str2.equals("nextCatalog")) {
                    if (checkAttributes(attributes, "catalog")) {
                        i2 = Catalog.CATALOG;
                        vector.add(attributes.getValue("catalog"));
                        this.debug.message(4, "nextCatalog", attributes.getValue("catalog"));
                    }
                } else if (str2.equals("public")) {
                    if (checkAttributes(attributes, "publicId", Constants.ELEMNAME_URL_STRING)) {
                        i2 = Catalog.PUBLIC;
                        vector.add(attributes.getValue("publicId"));
                        vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
                        this.debug.message(4, "public", PublicId.normalize(attributes.getValue("publicId")), attributes.getValue(Constants.ELEMNAME_URL_STRING));
                    }
                } else if (str2.equals("system")) {
                    if (checkAttributes(attributes, "systemId", Constants.ELEMNAME_URL_STRING)) {
                        i2 = Catalog.SYSTEM;
                        vector.add(attributes.getValue("systemId"));
                        vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
                        this.debug.message(4, "system", attributes.getValue("systemId"), attributes.getValue(Constants.ELEMNAME_URL_STRING));
                    }
                } else if (str2.equals(Constants.ELEMNAME_URL_STRING)) {
                    if (checkAttributes(attributes, "name", Constants.ELEMNAME_URL_STRING)) {
                        i2 = Catalog.URI;
                        vector.add(attributes.getValue("name"));
                        vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
                        this.debug.message(4, Constants.ELEMNAME_URL_STRING, attributes.getValue("name"), attributes.getValue(Constants.ELEMNAME_URL_STRING));
                    }
                } else if (!str2.equals("catalog") && !str2.equals("group")) {
                    this.debug.message(1, "Invalid catalog entry type", str2);
                }
                i = -1;
                if (i >= 0) {
                }
                vector = vector;
            }
            i = i2;
            if (i >= 0) {
            }
            vector = vector;
        }
        if (str != null && tr9401NamespaceName.equals(str) && !inExtensionNamespace) {
            if (attributes.getValue(str4) != null) {
                String value3 = attributes.getValue(str4);
                int i5 = Catalog.BASE;
                vector.add(value3);
                this.baseURIStack.push(value3);
                this.debug.message(4, str4, value3);
                try {
                    this.catalog.addEntry(new CatalogEntry(i5, vector));
                } catch (CatalogException e4) {
                    if (e4.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e4.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry (base)", str2);
                    }
                }
                vector = new Vector();
                i = -1;
            } else {
                Stack stack3 = this.baseURIStack;
                stack3.push(stack3.peek());
            }
            if (str2.equals("doctype")) {
                Catalog catalog2 = this.catalog;
                i = Catalog.DOCTYPE;
                vector.add(attributes.getValue("name"));
                vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
            } else if (str2.equals(ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.DOCUMENT_PNAME)) {
                Catalog catalog3 = this.catalog;
                i = Catalog.DOCUMENT;
                vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
            } else if (str2.equals("dtddecl")) {
                Catalog catalog4 = this.catalog;
                i = Catalog.DTDDECL;
                vector.add(attributes.getValue("publicId"));
                vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
            } else if (str2.equals("entity")) {
                i = Catalog.ENTITY;
                vector.add(attributes.getValue("name"));
                vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
            } else if (str2.equals("linktype")) {
                i = Catalog.LINKTYPE;
                vector.add(attributes.getValue("name"));
                vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
            } else if (str2.equals("notation")) {
                i = Catalog.NOTATION;
                vector.add(attributes.getValue("name"));
                vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
            } else if (str2.equals("sgmldecl")) {
                i = Catalog.SGMLDECL;
                vector.add(attributes.getValue(Constants.ELEMNAME_URL_STRING));
            } else {
                this.debug.message(1, "Invalid catalog entry type", str2);
            }
            if (i >= 0) {
                try {
                    this.catalog.addEntry(new CatalogEntry(i, vector));
                } catch (CatalogException e5) {
                    if (e5.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e5.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry", str2);
                    }
                }
            }
        }
    }

    public boolean checkAttributes(Attributes attributes, String str) {
        if (attributes.getValue(str) != null) {
            return true;
        }
        Debug debug = this.debug;
        debug.message(1, "Error: required attribute " + str + " missing.");
        return false;
    }

    public boolean checkAttributes(Attributes attributes, String str, String str2) {
        return checkAttributes(attributes, str) && checkAttributes(attributes, str2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader
    public void endElement(String str, String str2, String str3) throws SAXException {
        Vector vector = new Vector();
        boolean inExtensionNamespace = inExtensionNamespace();
        if (str != null && !inExtensionNamespace && (namespaceName.equals(str) || tr9401NamespaceName.equals(str))) {
            String str4 = (String) this.baseURIStack.peek();
            if (!str4.equals((String) this.baseURIStack.pop())) {
                Catalog catalog2 = this.catalog;
                int i = Catalog.BASE;
                vector.add(str4);
                this.debug.message(4, "(reset) xml:base", str4);
                try {
                    this.catalog.addEntry(new CatalogEntry(i, vector));
                } catch (CatalogException e) {
                    if (e.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry (rbase)", str2);
                    }
                }
            }
        }
        if (str != null && namespaceName.equals(str) && !inExtensionNamespace && (str2.equals("catalog") || str2.equals("group"))) {
            String str5 = (String) this.overrideStack.peek();
            if (!str5.equals((String) this.overrideStack.pop())) {
                Catalog catalog3 = this.catalog;
                int i2 = Catalog.OVERRIDE;
                vector.add(str5);
                this.overrideStack.push(str5);
                this.debug.message(4, "(reset) override", str5);
                try {
                    this.catalog.addEntry(new CatalogEntry(i2, vector));
                } catch (CatalogException e2) {
                    if (e2.getExceptionType() == 3) {
                        this.debug.message(1, "Invalid catalog entry type", str2);
                    } else if (e2.getExceptionType() == 2) {
                        this.debug.message(1, "Invalid catalog entry (roverride)", str2);
                    }
                }
            }
        }
        this.namespaceStack.pop();
    }
}
