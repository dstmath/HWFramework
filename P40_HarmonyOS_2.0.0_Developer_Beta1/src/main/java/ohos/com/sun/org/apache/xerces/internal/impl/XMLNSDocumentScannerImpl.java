package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidatorFilter;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public class XMLNSDocumentScannerImpl extends XMLDocumentScannerImpl {
    protected boolean fBindNamespaces;
    private XMLDTDValidatorFilter fDTDValidator;
    protected boolean fNotAddNSDeclAsAttribute = false;
    protected boolean fPerformValidation;
    private boolean fXmlnsDeclared = false;

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner
    public void reset(PropertyManager propertyManager) {
        setPropertyManager(propertyManager);
        super.reset(propertyManager);
        this.fBindNamespaces = false;
        this.fNotAddNSDeclAsAttribute = !((Boolean) propertyManager.getProperty(Constants.ADD_NAMESPACE_DECL_AS_ATTRIBUTE)).booleanValue();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        super.reset(xMLComponentManager);
        this.fNotAddNSDeclAsAttribute = false;
        this.fPerformValidation = false;
        this.fBindNamespaces = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner
    public int next() throws IOException, XNIException {
        if (this.fScannerLastState == 2 && this.fBindNamespaces) {
            this.fScannerLastState = -1;
            this.fNamespaceContext.popContext();
        }
        int next = super.next();
        this.fScannerLastState = next;
        return next;
    }

    public void setDTDValidator(XMLDTDValidatorFilter xMLDTDValidatorFilter) {
        this.fDTDValidator = xMLDTDValidatorFilter;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl
    public boolean scanStartElement() throws IOException, XNIException {
        QName checkDuplicatesNS;
        if (this.fSkip && !this.fAdd) {
            QName next = this.fElementStack.getNext();
            this.fSkip = this.fEntityScanner.skipString(next.rawname);
            if (this.fSkip) {
                this.fElementStack.push();
                this.fElementQName = next;
            } else {
                this.fElementStack.reposition();
            }
        }
        if (!this.fSkip || this.fAdd) {
            this.fElementQName = this.fElementStack.nextElement();
            if (this.fNamespaces) {
                this.fEntityScanner.scanQName(this.fElementQName, XMLScanner.NameType.ELEMENTSTART);
            } else {
                String scanName = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART);
                this.fElementQName.setValues(null, scanName, scanName, null);
            }
        }
        if (this.fAdd) {
            this.fElementStack.matchElement(this.fElementQName);
        }
        this.fCurrentElement = this.fElementQName;
        String str = this.fElementQName.rawname;
        checkDepth(str);
        if (this.fBindNamespaces) {
            this.fNamespaceContext.pushContext();
            if (this.fScannerState == 26 && this.fPerformValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_GRAMMAR_NOT_FOUND", new Object[]{str}, 1);
                if (this.fDoctypeName == null || !this.fDoctypeName.equals(str)) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "RootElementTypeMustMatchDoctypedecl", new Object[]{this.fDoctypeName, str}, 1);
                }
            }
        }
        this.fEmptyElement = false;
        this.fAttributes.removeAllAttributes();
        if (!seekCloseOfStartTag()) {
            this.fReadingAttributes = true;
            this.fAttributeCacheUsedCount = 0;
            this.fStringBufferIndex = 0;
            this.fAddDefaultAttr = true;
            this.fXmlnsDeclared = false;
            do {
                scanAttribute((XMLAttributesImpl) this.fAttributes);
                if (this.fSecurityManager != null && !this.fSecurityManager.isNoLimit(this.fElementAttributeLimit) && this.fAttributes.getLength() > this.fElementAttributeLimit) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementAttributeLimit", new Object[]{str, Integer.valueOf(this.fElementAttributeLimit)}, 2);
                }
            } while (!seekCloseOfStartTag());
            this.fReadingAttributes = false;
        }
        if (this.fBindNamespaces) {
            if (this.fElementQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementXMLNSPrefix", new Object[]{this.fElementQName.rawname}, 2);
            }
            this.fElementQName.uri = this.fNamespaceContext.getURI(this.fElementQName.prefix != null ? this.fElementQName.prefix : XMLSymbols.EMPTY_STRING);
            this.fCurrentElement.uri = this.fElementQName.uri;
            if (this.fElementQName.prefix == null && this.fElementQName.uri != null) {
                this.fElementQName.prefix = XMLSymbols.EMPTY_STRING;
            }
            if (this.fElementQName.prefix != null && this.fElementQName.uri == null) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementPrefixUnbound", new Object[]{this.fElementQName.prefix, this.fElementQName.rawname}, 2);
            }
            int length = this.fAttributes.getLength();
            for (int i = 0; i < length; i++) {
                this.fAttributes.getName(i, this.fAttributeQName);
                String str2 = this.fAttributeQName.prefix != null ? this.fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
                String uri = this.fNamespaceContext.getURI(str2);
                if ((this.fAttributeQName.uri == null || this.fAttributeQName.uri != uri) && str2 != XMLSymbols.EMPTY_STRING) {
                    this.fAttributeQName.uri = uri;
                    if (uri == null) {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributePrefixUnbound", new Object[]{this.fElementQName.rawname, this.fAttributeQName.rawname, str2}, 2);
                    }
                    this.fAttributes.setURI(i, uri);
                }
            }
            if (length > 1 && (checkDuplicatesNS = this.fAttributes.checkDuplicatesNS()) != null) {
                if (checkDuplicatesNS.uri != null) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributeNSNotUnique", new Object[]{this.fElementQName.rawname, checkDuplicatesNS.localpart, checkDuplicatesNS.uri}, 2);
                } else {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "AttributeNotUnique", new Object[]{this.fElementQName.rawname, checkDuplicatesNS.rawname}, 2);
                }
            }
        }
        if (this.fEmptyElement) {
            this.fMarkupDepth--;
            if (this.fMarkupDepth < this.fEntityStack[this.fEntityDepth - 1]) {
                reportFatalError("ElementEntityMismatch", new Object[]{this.fCurrentElement.rawname});
            }
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.emptyElement(this.fElementQName, this.fAttributes, null);
            }
            this.fScanEndElement = true;
            this.fElementStack.popElement();
        } else {
            if (this.dtdGrammarUtil != null) {
                this.dtdGrammarUtil.startElement(this.fElementQName, this.fAttributes);
            }
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.startElement(this.fElementQName, this.fAttributes, null);
            }
        }
        return this.fEmptyElement;
    }

    /* access modifiers changed from: protected */
    public void scanAttribute(XMLAttributesImpl xMLAttributesImpl) throws IOException, XNIException {
        String str;
        int i;
        this.fEntityScanner.scanQName(this.fAttributeQName, XMLScanner.NameType.ATTRIBUTENAME);
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(61, XMLScanner.NameType.ATTRIBUTE)) {
            reportFatalError("EqRequiredInAttribute", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
        }
        this.fEntityScanner.skipSpaces();
        boolean z = this.fHasExternalDTD && !this.fStandalone;
        XMLString string = getString();
        String str2 = this.fAttributeQName.localpart;
        String str3 = this.fAttributeQName.prefix != null ? this.fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
        boolean z2 = this.fBindNamespaces & (str3 == XMLSymbols.PREFIX_XMLNS || (str3 == XMLSymbols.EMPTY_STRING && str2 == XMLSymbols.PREFIX_XMLNS));
        scanAttributeValue(string, this.fTempString2, this.fAttributeQName.rawname, xMLAttributesImpl, 0, z, this.fCurrentElement.rawname, z2);
        if (!this.fBindNamespaces || !z2) {
            str = null;
        } else {
            if (string.length > this.fXMLNameLimit) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MaxXMLNameLimit", new Object[]{new String(string.ch, string.offset, string.length), Integer.valueOf(string.length), Integer.valueOf(this.fXMLNameLimit), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.MAX_NAME_LIMIT)}, 2);
            }
            str = this.fSymbolTable.addSymbol(string.ch, string.offset, string.length);
            if (str3 == XMLSymbols.PREFIX_XMLNS && str2 == XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{this.fAttributeQName}, 2);
            }
            if (str == NamespaceContext.XMLNS_URI) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{this.fAttributeQName}, 2);
            }
            if (str2 == XMLSymbols.PREFIX_XML) {
                if (str != NamespaceContext.XML_URI) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{this.fAttributeQName}, 2);
                }
            } else if (str == NamespaceContext.XML_URI) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{this.fAttributeQName}, 2);
            }
            String str4 = str2 != XMLSymbols.PREFIX_XMLNS ? str2 : XMLSymbols.EMPTY_STRING;
            if (str4 == XMLSymbols.EMPTY_STRING && str2 == XMLSymbols.PREFIX_XMLNS) {
                this.fAttributeQName.prefix = XMLSymbols.PREFIX_XMLNS;
            }
            if (str == XMLSymbols.EMPTY_STRING && str2 != XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "EmptyPrefixedAttName", new Object[]{this.fAttributeQName}, 2);
            }
            if (((NamespaceSupport) this.fNamespaceContext).containsPrefixInCurrentContext(str4)) {
                reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
            }
            if (!this.fNamespaceContext.declarePrefix(str4, str.length() != 0 ? str : null)) {
                if (this.fXmlnsDeclared) {
                    reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
                }
                this.fXmlnsDeclared = true;
            }
            if (this.fNotAddNSDeclAsAttribute) {
                return;
            }
        }
        if (this.fBindNamespaces) {
            i = xMLAttributesImpl.getLength();
            xMLAttributesImpl.addAttributeNS(this.fAttributeQName, XMLSymbols.fCDATASymbol, null);
        } else {
            int length = xMLAttributesImpl.getLength();
            int addAttribute = xMLAttributesImpl.addAttribute(this.fAttributeQName, XMLSymbols.fCDATASymbol, null);
            if (length == xMLAttributesImpl.getLength()) {
                reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
            }
            i = addAttribute;
        }
        xMLAttributesImpl.setValue(i, str, string);
        xMLAttributesImpl.setSpecified(i, true);
        if (this.fAttributeQName.prefix != null) {
            xMLAttributesImpl.setURI(i, this.fNamespaceContext.getURI(this.fAttributeQName.prefix));
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl
    public XMLDocumentFragmentScannerImpl.Driver createContentDriver() {
        return new NSContentDriver();
    }

    protected final class NSContentDriver extends XMLDocumentScannerImpl.ContentDriver {
        protected NSContentDriver() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl.ContentDriver, ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.FragmentContentDriver
        public boolean scanRootElementHook() throws IOException, XNIException {
            reconfigurePipeline();
            if (!XMLNSDocumentScannerImpl.this.scanStartElement()) {
                return false;
            }
            XMLNSDocumentScannerImpl.this.setScannerState(44);
            XMLNSDocumentScannerImpl xMLNSDocumentScannerImpl = XMLNSDocumentScannerImpl.this;
            xMLNSDocumentScannerImpl.setDriver(xMLNSDocumentScannerImpl.fTrailingMiscDriver);
            return true;
        }

        private void reconfigurePipeline() {
            if (XMLNSDocumentScannerImpl.this.fNamespaces && XMLNSDocumentScannerImpl.this.fDTDValidator == null) {
                XMLNSDocumentScannerImpl.this.fBindNamespaces = true;
            } else if (XMLNSDocumentScannerImpl.this.fNamespaces && !XMLNSDocumentScannerImpl.this.fDTDValidator.hasGrammar()) {
                XMLNSDocumentScannerImpl xMLNSDocumentScannerImpl = XMLNSDocumentScannerImpl.this;
                xMLNSDocumentScannerImpl.fBindNamespaces = true;
                xMLNSDocumentScannerImpl.fPerformValidation = xMLNSDocumentScannerImpl.fDTDValidator.validate();
                XMLDocumentSource documentSource = XMLNSDocumentScannerImpl.this.fDTDValidator.getDocumentSource();
                XMLDocumentHandler documentHandler = XMLNSDocumentScannerImpl.this.fDTDValidator.getDocumentHandler();
                documentSource.setDocumentHandler(documentHandler);
                if (documentHandler != null) {
                    documentHandler.setDocumentSource(documentSource);
                }
                XMLNSDocumentScannerImpl.this.fDTDValidator.setDocumentSource(null);
                XMLNSDocumentScannerImpl.this.fDTDValidator.setDocumentHandler(null);
            }
        }
    }
}
