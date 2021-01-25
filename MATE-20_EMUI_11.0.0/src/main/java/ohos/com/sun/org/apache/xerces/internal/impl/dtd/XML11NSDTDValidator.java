package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public class XML11NSDTDValidator extends XML11DTDValidator {
    private QName fAttributeQName = new QName();

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator
    public final void startNamespaceScope(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        this.fNamespaceContext.pushContext();
        if (qName.prefix == XMLSymbols.PREFIX_XMLNS) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementXMLNSPrefix", new Object[]{qName.rawname}, 2);
        }
        int length = xMLAttributes.getLength();
        for (int i = 0; i < length; i++) {
            String localName = xMLAttributes.getLocalName(i);
            String prefix = xMLAttributes.getPrefix(i);
            if (prefix == XMLSymbols.PREFIX_XMLNS || (prefix == XMLSymbols.EMPTY_STRING && localName == XMLSymbols.PREFIX_XMLNS)) {
                String addSymbol = this.fSymbolTable.addSymbol(xMLAttributes.getValue(i));
                if (prefix == XMLSymbols.PREFIX_XMLNS && localName == XMLSymbols.PREFIX_XMLNS) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{xMLAttributes.getQName(i)}, 2);
                }
                if (addSymbol == NamespaceContext.XMLNS_URI) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{xMLAttributes.getQName(i)}, 2);
                }
                if (localName == XMLSymbols.PREFIX_XML) {
                    if (addSymbol != NamespaceContext.XML_URI) {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{xMLAttributes.getQName(i)}, 2);
                    }
                } else if (addSymbol == NamespaceContext.XML_URI) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{xMLAttributes.getQName(i)}, 2);
                }
                if (localName == XMLSymbols.PREFIX_XMLNS) {
                    localName = XMLSymbols.EMPTY_STRING;
                }
                NamespaceContext namespaceContext = this.fNamespaceContext;
                if (addSymbol.length() == 0) {
                    addSymbol = null;
                }
                namespaceContext.declarePrefix(localName, addSymbol);
            }
        }
        qName.uri = this.fNamespaceContext.getURI(qName.prefix != null ? qName.prefix : XMLSymbols.EMPTY_STRING);
        if (qName.prefix == null && qName.uri != null) {
            qName.prefix = XMLSymbols.EMPTY_STRING;
        }
        if (qName.prefix != null && qName.uri == null) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementPrefixUnbound", new Object[]{qName.prefix, qName.rawname}, 2);
        }
        for (int i2 = 0; i2 < length; i2++) {
            xMLAttributes.getName(i2, this.fAttributeQName);
            String str = this.fAttributeQName.prefix != null ? this.fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
            String str2 = this.fAttributeQName.rawname;
            if (str2 == XMLSymbols.PREFIX_XMLNS) {
                this.fAttributeQName.uri = this.fNamespaceContext.getURI(XMLSymbols.PREFIX_XMLNS);
                xMLAttributes.setName(i2, this.fAttributeQName);
            } else if (str != XMLSymbols.EMPTY_STRING) {
                this.fAttributeQName.uri = this.fNamespaceContext.getURI(str);
                if (this.fAttributeQName.uri == null) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributePrefixUnbound", new Object[]{qName.rawname, str2, str}, 2);
                }
                xMLAttributes.setName(i2, this.fAttributeQName);
            }
        }
        int length2 = xMLAttributes.getLength();
        for (int i3 = 0; i3 < length2 - 1; i3++) {
            String uri = xMLAttributes.getURI(i3);
            if (!(uri == null || uri == NamespaceContext.XMLNS_URI)) {
                String localName2 = xMLAttributes.getLocalName(i3);
                for (int i4 = i3 + 1; i4 < length2; i4++) {
                    String localName3 = xMLAttributes.getLocalName(i4);
                    String uri2 = xMLAttributes.getURI(i4);
                    if (localName2 == localName3 && uri == uri2) {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributeNSNotUnique", new Object[]{qName.rawname, localName2, uri}, 2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator
    public void endNamespaceScope(QName qName, Augmentations augmentations, boolean z) throws XNIException {
        String str = qName.prefix != null ? qName.prefix : XMLSymbols.EMPTY_STRING;
        qName.uri = this.fNamespaceContext.getURI(str);
        if (qName.uri != null) {
            qName.prefix = str;
        }
        if (this.fDocumentHandler != null && !z) {
            this.fDocumentHandler.endElement(qName, augmentations);
        }
        this.fNamespaceContext.popContext();
    }
}
