package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidatorFilter;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;

public class XML11NSDocumentScannerImpl extends XML11DocumentScannerImpl {
    protected boolean fBindNamespaces;
    private XMLDTDValidatorFilter fDTDValidator;
    protected boolean fPerformValidation;
    private boolean fSawSpace;

    public void setDTDValidator(XMLDTDValidatorFilter xMLDTDValidatorFilter) {
        this.fDTDValidator = xMLDTDValidatorFilter;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl
    public boolean scanStartElement() throws IOException, XNIException {
        boolean z;
        String str;
        QName checkDuplicatesNS;
        String str2;
        this.fEntityScanner.scanQName(this.fElementQName, XMLScanner.NameType.ELEMENTSTART);
        String str3 = this.fElementQName.rawname;
        if (this.fBindNamespaces) {
            this.fNamespaceContext.pushContext();
            if (this.fScannerState == 26 && this.fPerformValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_GRAMMAR_NOT_FOUND", new Object[]{str3}, 1);
                if (this.fDoctypeName == null || !this.fDoctypeName.equals(str3)) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "RootElementTypeMustMatchDoctypedecl", new Object[]{this.fDoctypeName, str3}, 1);
                }
            }
        }
        this.fCurrentElement = this.fElementStack.pushElement(this.fElementQName);
        this.fAttributes.removeAllAttributes();
        while (true) {
            boolean skipSpaces = this.fEntityScanner.skipSpaces();
            int peekChar = this.fEntityScanner.peekChar();
            if (peekChar == 62) {
                this.fEntityScanner.scanChar(null);
                z = false;
                break;
            } else if (peekChar == 47) {
                this.fEntityScanner.scanChar(null);
                if (!this.fEntityScanner.skipChar(62, null)) {
                    reportFatalError("ElementUnterminated", new Object[]{str3});
                }
                z = true;
            } else {
                if ((!isValidNameStartChar(peekChar) || !skipSpaces) && (!isValidNameStartHighSurrogate(peekChar) || !skipSpaces)) {
                    reportFatalError("ElementUnterminated", new Object[]{str3});
                }
                scanAttribute((XMLAttributesImpl) this.fAttributes);
                if (this.fSecurityManager != null && !this.fSecurityManager.isNoLimit(this.fElementAttributeLimit) && this.fAttributes.getLength() > this.fElementAttributeLimit) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementAttributeLimit", new Object[]{str3, new Integer(this.fElementAttributeLimit)}, 2);
                }
            }
        }
        if (this.fBindNamespaces) {
            if (this.fElementQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementXMLNSPrefix", new Object[]{this.fElementQName.rawname}, 2);
            }
            if (this.fElementQName.prefix != null) {
                str = this.fElementQName.prefix;
            } else {
                str = XMLSymbols.EMPTY_STRING;
            }
            this.fElementQName.uri = this.fNamespaceContext.getURI(str);
            this.fCurrentElement.uri = this.fElementQName.uri;
            if (this.fElementQName.prefix == null && this.fElementQName.uri != null) {
                this.fElementQName.prefix = XMLSymbols.EMPTY_STRING;
                this.fCurrentElement.prefix = XMLSymbols.EMPTY_STRING;
            }
            if (this.fElementQName.prefix != null && this.fElementQName.uri == null) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementPrefixUnbound", new Object[]{this.fElementQName.prefix, this.fElementQName.rawname}, 2);
            }
            int length = this.fAttributes.getLength();
            for (int i = 0; i < length; i++) {
                this.fAttributes.getName(i, this.fAttributeQName);
                if (this.fAttributeQName.prefix != null) {
                    str2 = this.fAttributeQName.prefix;
                } else {
                    str2 = XMLSymbols.EMPTY_STRING;
                }
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
        if (z) {
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
        return z;
    }

    /* access modifiers changed from: protected */
    public void scanStartElementName() throws IOException, XNIException {
        this.fEntityScanner.scanQName(this.fElementQName, XMLScanner.NameType.ELEMENTSTART);
        this.fSawSpace = this.fEntityScanner.skipSpaces();
    }

    /* access modifiers changed from: protected */
    public boolean scanStartElementAfterName() throws IOException, XNIException {
        boolean z;
        String str;
        QName checkDuplicatesNS;
        String str2;
        String str3 = this.fElementQName.rawname;
        if (this.fBindNamespaces) {
            this.fNamespaceContext.pushContext();
            if (this.fScannerState == 26 && this.fPerformValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_GRAMMAR_NOT_FOUND", new Object[]{str3}, 1);
                if (this.fDoctypeName == null || !this.fDoctypeName.equals(str3)) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "RootElementTypeMustMatchDoctypedecl", new Object[]{this.fDoctypeName, str3}, 1);
                }
            }
        }
        this.fCurrentElement = this.fElementStack.pushElement(this.fElementQName);
        this.fAttributes.removeAllAttributes();
        while (true) {
            int peekChar = this.fEntityScanner.peekChar();
            if (peekChar == 62) {
                this.fEntityScanner.scanChar(null);
                z = false;
                break;
            } else if (peekChar == 47) {
                this.fEntityScanner.scanChar(null);
                if (!this.fEntityScanner.skipChar(62, null)) {
                    reportFatalError("ElementUnterminated", new Object[]{str3});
                }
                z = true;
            } else {
                if ((!isValidNameStartChar(peekChar) || !this.fSawSpace) && (!isValidNameStartHighSurrogate(peekChar) || !this.fSawSpace)) {
                    reportFatalError("ElementUnterminated", new Object[]{str3});
                }
                scanAttribute((XMLAttributesImpl) this.fAttributes);
                this.fSawSpace = this.fEntityScanner.skipSpaces();
            }
        }
        if (this.fBindNamespaces) {
            if (this.fElementQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementXMLNSPrefix", new Object[]{this.fElementQName.rawname}, 2);
            }
            if (this.fElementQName.prefix != null) {
                str = this.fElementQName.prefix;
            } else {
                str = XMLSymbols.EMPTY_STRING;
            }
            this.fElementQName.uri = this.fNamespaceContext.getURI(str);
            this.fCurrentElement.uri = this.fElementQName.uri;
            if (this.fElementQName.prefix == null && this.fElementQName.uri != null) {
                this.fElementQName.prefix = XMLSymbols.EMPTY_STRING;
                this.fCurrentElement.prefix = XMLSymbols.EMPTY_STRING;
            }
            if (this.fElementQName.prefix != null && this.fElementQName.uri == null) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "ElementPrefixUnbound", new Object[]{this.fElementQName.prefix, this.fElementQName.rawname}, 2);
            }
            int length = this.fAttributes.getLength();
            for (int i = 0; i < length; i++) {
                this.fAttributes.getName(i, this.fAttributeQName);
                if (this.fAttributeQName.prefix != null) {
                    str2 = this.fAttributeQName.prefix;
                } else {
                    str2 = XMLSymbols.EMPTY_STRING;
                }
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
        if (this.fDocumentHandler != null) {
            if (z) {
                this.fMarkupDepth--;
                if (this.fMarkupDepth < this.fEntityStack[this.fEntityDepth - 1]) {
                    reportFatalError("ElementEntityMismatch", new Object[]{this.fCurrentElement.rawname});
                }
                this.fDocumentHandler.emptyElement(this.fElementQName, this.fAttributes, null);
                if (this.fBindNamespaces) {
                    this.fNamespaceContext.popContext();
                }
                this.fElementStack.popElement();
            } else {
                this.fDocumentHandler.startElement(this.fElementQName, this.fAttributes, null);
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void scanAttribute(XMLAttributesImpl xMLAttributesImpl) throws IOException, XNIException {
        int i;
        this.fEntityScanner.scanQName(this.fAttributeQName, XMLScanner.NameType.ATTRIBUTENAME);
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(61, XMLScanner.NameType.ATTRIBUTE)) {
            reportFatalError("EqRequiredInAttribute", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
        }
        this.fEntityScanner.skipSpaces();
        if (this.fBindNamespaces) {
            int length = xMLAttributesImpl.getLength();
            xMLAttributesImpl.addAttributeNS(this.fAttributeQName, XMLSymbols.fCDATASymbol, null);
            i = length;
        } else {
            int length2 = xMLAttributesImpl.getLength();
            int addAttribute = xMLAttributesImpl.addAttribute(this.fAttributeQName, XMLSymbols.fCDATASymbol, null);
            if (length2 == xMLAttributesImpl.getLength()) {
                reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
            }
            i = addAttribute;
        }
        boolean z = this.fHasExternalDTD && !this.fStandalone;
        String str = this.fAttributeQName.localpart;
        String str2 = this.fAttributeQName.prefix != null ? this.fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
        boolean z2 = this.fBindNamespaces & (str2 == XMLSymbols.PREFIX_XMLNS || (str2 == XMLSymbols.EMPTY_STRING && str == XMLSymbols.PREFIX_XMLNS));
        scanAttributeValue(this.fTempString, this.fTempString2, this.fAttributeQName.rawname, z, this.fCurrentElement.rawname, z2);
        String xMLString = this.fTempString.toString();
        xMLAttributesImpl.setValue(i, xMLString);
        xMLAttributesImpl.setNonNormalizedValue(i, this.fTempString2.toString());
        xMLAttributesImpl.setSpecified(i, true);
        if (!this.fBindNamespaces) {
            return;
        }
        if (z2) {
            if (xMLString.length() > this.fXMLNameLimit) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MaxXMLNameLimit", new Object[]{xMLString, Integer.valueOf(xMLString.length()), Integer.valueOf(this.fXMLNameLimit), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.MAX_NAME_LIMIT)}, 2);
            }
            String addSymbol = this.fSymbolTable.addSymbol(xMLString);
            if (str2 == XMLSymbols.PREFIX_XMLNS && str == XMLSymbols.PREFIX_XMLNS) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{this.fAttributeQName}, 2);
            }
            if (addSymbol == NamespaceContext.XMLNS_URI) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXMLNS", new Object[]{this.fAttributeQName}, 2);
            }
            if (str == XMLSymbols.PREFIX_XML) {
                if (addSymbol != NamespaceContext.XML_URI) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{this.fAttributeQName}, 2);
                }
            } else if (addSymbol == NamespaceContext.XML_URI) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1999/REC-xml-names-19990114", "CantBindXML", new Object[]{this.fAttributeQName}, 2);
            }
            if (str == XMLSymbols.PREFIX_XMLNS) {
                str = XMLSymbols.EMPTY_STRING;
            }
            NamespaceContext namespaceContext = this.fNamespaceContext;
            if (addSymbol.length() == 0) {
                addSymbol = null;
            }
            namespaceContext.declarePrefix(str, addSymbol);
            xMLAttributesImpl.setURI(i, this.fNamespaceContext.getURI(XMLSymbols.PREFIX_XMLNS));
        } else if (this.fAttributeQName.prefix != null) {
            xMLAttributesImpl.setURI(i, this.fNamespaceContext.getURI(this.fAttributeQName.prefix));
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl
    public int scanEndElement() throws IOException, XNIException {
        QName popElement = this.fElementStack.popElement();
        if (!this.fEntityScanner.skipString(popElement.rawname)) {
            reportFatalError("ETagRequired", new Object[]{popElement.rawname});
        }
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(62, XMLScanner.NameType.ELEMENTEND)) {
            reportFatalError("ETagUnterminated", new Object[]{popElement.rawname});
        }
        this.fMarkupDepth--;
        this.fMarkupDepth--;
        if (this.fMarkupDepth < this.fEntityStack[this.fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch", new Object[]{popElement.rawname});
        }
        if (this.fDocumentHandler != null) {
            this.fDocumentHandler.endElement(popElement, null);
        }
        if (this.dtdGrammarUtil != null) {
            this.dtdGrammarUtil.endElement(popElement);
        }
        return this.fMarkupDepth;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        super.reset(xMLComponentManager);
        this.fPerformValidation = false;
        this.fBindNamespaces = false;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl, ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl
    public XMLDocumentFragmentScannerImpl.Driver createContentDriver() {
        return new NS11ContentDriver();
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

    protected final class NS11ContentDriver extends XMLDocumentScannerImpl.ContentDriver {
        protected NS11ContentDriver() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl.ContentDriver, ohos.com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.FragmentContentDriver
        public boolean scanRootElementHook() throws IOException, XNIException {
            if (XML11NSDocumentScannerImpl.this.fExternalSubsetResolver == null || XML11NSDocumentScannerImpl.this.fSeenDoctypeDecl || XML11NSDocumentScannerImpl.this.fDisallowDoctype || (!XML11NSDocumentScannerImpl.this.fValidation && !XML11NSDocumentScannerImpl.this.fLoadExternalDTD)) {
                reconfigurePipeline();
                if (!XML11NSDocumentScannerImpl.this.scanStartElement()) {
                    return false;
                }
                XML11NSDocumentScannerImpl.this.setScannerState(44);
                XML11NSDocumentScannerImpl xML11NSDocumentScannerImpl = XML11NSDocumentScannerImpl.this;
                xML11NSDocumentScannerImpl.setDriver(xML11NSDocumentScannerImpl.fTrailingMiscDriver);
                return true;
            }
            XML11NSDocumentScannerImpl.this.scanStartElementName();
            resolveExternalSubsetAndRead();
            reconfigurePipeline();
            if (!XML11NSDocumentScannerImpl.this.scanStartElementAfterName()) {
                return false;
            }
            XML11NSDocumentScannerImpl.this.setScannerState(44);
            XML11NSDocumentScannerImpl xML11NSDocumentScannerImpl2 = XML11NSDocumentScannerImpl.this;
            xML11NSDocumentScannerImpl2.setDriver(xML11NSDocumentScannerImpl2.fTrailingMiscDriver);
            return true;
        }

        private void reconfigurePipeline() {
            if (XML11NSDocumentScannerImpl.this.fDTDValidator == null) {
                XML11NSDocumentScannerImpl.this.fBindNamespaces = true;
            } else if (!XML11NSDocumentScannerImpl.this.fDTDValidator.hasGrammar()) {
                XML11NSDocumentScannerImpl xML11NSDocumentScannerImpl = XML11NSDocumentScannerImpl.this;
                xML11NSDocumentScannerImpl.fBindNamespaces = true;
                xML11NSDocumentScannerImpl.fPerformValidation = xML11NSDocumentScannerImpl.fDTDValidator.validate();
                XMLDocumentSource documentSource = XML11NSDocumentScannerImpl.this.fDTDValidator.getDocumentSource();
                XMLDocumentHandler documentHandler = XML11NSDocumentScannerImpl.this.fDTDValidator.getDocumentHandler();
                documentSource.setDocumentHandler(documentHandler);
                if (documentHandler != null) {
                    documentHandler.setDocumentSource(documentSource);
                }
                XML11NSDocumentScannerImpl.this.fDTDValidator.setDocumentSource(null);
                XML11NSDocumentScannerImpl.this.fDTDValidator.setDocumentHandler(null);
            }
        }
    }
}
