package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMErrorImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMLocatorImpl;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.global.icu.text.PluralRules;
import ohos.org.w3c.dom.DOMError;
import ohos.org.w3c.dom.DOMErrorHandler;
import ohos.org.w3c.dom.DOMLocator;
import ohos.org.w3c.dom.Node;

public class DOMErrorHandlerWrapper implements XMLErrorHandler, DOMErrorHandler {
    boolean eStatus;
    public Node fCurrentNode;
    protected final DOMErrorImpl fDOMError;
    protected DOMErrorHandler fDomErrorHandler;
    protected final XMLErrorCode fErrorCode;
    protected PrintWriter fOut;

    public DOMErrorHandlerWrapper() {
        this.eStatus = true;
        this.fErrorCode = new XMLErrorCode(null, null);
        this.fDOMError = new DOMErrorImpl();
        this.fOut = new PrintWriter(System.err);
    }

    public DOMErrorHandlerWrapper(DOMErrorHandler dOMErrorHandler) {
        this.eStatus = true;
        this.fErrorCode = new XMLErrorCode(null, null);
        this.fDOMError = new DOMErrorImpl();
        this.fDomErrorHandler = dOMErrorHandler;
    }

    public void setErrorHandler(DOMErrorHandler dOMErrorHandler) {
        this.fDomErrorHandler = dOMErrorHandler;
    }

    public DOMErrorHandler getErrorHandler() {
        return this.fDomErrorHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void warning(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        DOMErrorImpl dOMErrorImpl = this.fDOMError;
        dOMErrorImpl.fSeverity = 1;
        dOMErrorImpl.fException = xMLParseException;
        dOMErrorImpl.fType = str2;
        String message = xMLParseException.getMessage();
        dOMErrorImpl.fMessage = message;
        dOMErrorImpl.fRelatedData = message;
        DOMLocatorImpl dOMLocatorImpl = this.fDOMError.fLocator;
        if (dOMLocatorImpl != null) {
            dOMLocatorImpl.fColumnNumber = xMLParseException.getColumnNumber();
            dOMLocatorImpl.fLineNumber = xMLParseException.getLineNumber();
            dOMLocatorImpl.fUtf16Offset = xMLParseException.getCharacterOffset();
            dOMLocatorImpl.fUri = xMLParseException.getExpandedSystemId();
            dOMLocatorImpl.fRelatedNode = this.fCurrentNode;
        }
        DOMErrorHandler dOMErrorHandler = this.fDomErrorHandler;
        if (dOMErrorHandler != null) {
            dOMErrorHandler.handleError(this.fDOMError);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void error(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        DOMErrorImpl dOMErrorImpl = this.fDOMError;
        dOMErrorImpl.fSeverity = 2;
        dOMErrorImpl.fException = xMLParseException;
        dOMErrorImpl.fType = str2;
        String message = xMLParseException.getMessage();
        dOMErrorImpl.fMessage = message;
        dOMErrorImpl.fRelatedData = message;
        DOMLocatorImpl dOMLocatorImpl = this.fDOMError.fLocator;
        if (dOMLocatorImpl != null) {
            dOMLocatorImpl.fColumnNumber = xMLParseException.getColumnNumber();
            dOMLocatorImpl.fLineNumber = xMLParseException.getLineNumber();
            dOMLocatorImpl.fUtf16Offset = xMLParseException.getCharacterOffset();
            dOMLocatorImpl.fUri = xMLParseException.getExpandedSystemId();
            dOMLocatorImpl.fRelatedNode = this.fCurrentNode;
        }
        DOMErrorHandler dOMErrorHandler = this.fDomErrorHandler;
        if (dOMErrorHandler != null) {
            dOMErrorHandler.handleError(this.fDOMError);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void fatalError(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        DOMErrorImpl dOMErrorImpl = this.fDOMError;
        dOMErrorImpl.fSeverity = 3;
        dOMErrorImpl.fException = xMLParseException;
        this.fErrorCode.setValues(str, str2);
        String dOMErrorType = DOMErrorTypeMap.getDOMErrorType(this.fErrorCode);
        DOMErrorImpl dOMErrorImpl2 = this.fDOMError;
        if (dOMErrorType == null) {
            dOMErrorType = str2;
        }
        dOMErrorImpl2.fType = dOMErrorType;
        DOMErrorImpl dOMErrorImpl3 = this.fDOMError;
        String message = xMLParseException.getMessage();
        dOMErrorImpl3.fMessage = message;
        dOMErrorImpl3.fRelatedData = message;
        DOMLocatorImpl dOMLocatorImpl = this.fDOMError.fLocator;
        if (dOMLocatorImpl != null) {
            dOMLocatorImpl.fColumnNumber = xMLParseException.getColumnNumber();
            dOMLocatorImpl.fLineNumber = xMLParseException.getLineNumber();
            dOMLocatorImpl.fUtf16Offset = xMLParseException.getCharacterOffset();
            dOMLocatorImpl.fUri = xMLParseException.getExpandedSystemId();
            dOMLocatorImpl.fRelatedNode = this.fCurrentNode;
        }
        DOMErrorHandler dOMErrorHandler = this.fDomErrorHandler;
        if (dOMErrorHandler != null) {
            dOMErrorHandler.handleError(this.fDOMError);
        }
    }

    public boolean handleError(DOMError dOMError) {
        printError(dOMError);
        return this.eStatus;
    }

    private void printError(DOMError dOMError) {
        short severity = dOMError.getSeverity();
        this.fOut.print("[");
        if (severity == 1) {
            this.fOut.print("Warning");
        } else if (severity == 2) {
            this.fOut.print("Error");
        } else {
            this.fOut.print("FatalError");
            this.eStatus = false;
        }
        this.fOut.print("] ");
        DOMLocator location = dOMError.getLocation();
        if (location != null) {
            this.fOut.print(location.getLineNumber());
            this.fOut.print(":");
            this.fOut.print(location.getColumnNumber());
            this.fOut.print(":");
            this.fOut.print(location.getByteOffset());
            this.fOut.print(",");
            this.fOut.print(location.getUtf16Offset());
            Node relatedNode = location.getRelatedNode();
            if (relatedNode != null) {
                this.fOut.print("[");
                this.fOut.print(relatedNode.getNodeName());
                this.fOut.print("]");
            }
            String uri = location.getUri();
            if (uri != null) {
                int lastIndexOf = uri.lastIndexOf(47);
                if (lastIndexOf != -1) {
                    uri = uri.substring(lastIndexOf + 1);
                }
                this.fOut.print(PluralRules.KEYWORD_RULE_SEPARATOR);
                this.fOut.print(uri);
            }
        }
        this.fOut.print(":");
        this.fOut.print(dOMError.getMessage());
        this.fOut.println();
        this.fOut.flush();
    }

    private static class DOMErrorTypeMap {
        private static final Map<XMLErrorCode, String> fgDOMErrorTypeTable;

        static {
            HashMap hashMap = new HashMap();
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInCDSect"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInContent"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "TwoColonsInQName"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ColonNotLegalWithNS"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInProlog"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "CDEndInContent"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "CDSectUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "DoctypeNotAllowed"), "doctype-not-allowed");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ETagRequired"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementUnterminated"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "EqRequiredInAttribute"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "OpenQuoteExpected"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "CloseQuoteExpected"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ETagUnterminated"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MarkupNotRecognizedInContent"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "DoctypeIllegalInContent"), "doctype-not-allowed");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInAttValue"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInPI"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInInternalSubset"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "QuoteRequiredInAttValue"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "LessthanInAttValue"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "AttributeValueUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "PITargetRequired"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "SpaceRequiredInPI"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "PIUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ReservedPITarget"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "PI_NOT_IN_ONE_ENTITY"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "PINotInOneEntity"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid"), "unsupported-encoding");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported"), "unsupported-encoding");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInEntityValue"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInExternalSubset"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInIgnoreSect"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInPublicID"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInSystemID"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "SpaceRequiredAfterSYSTEM"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "QuoteRequiredInSystemID"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "SystemIDUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "SpaceRequiredAfterPUBLIC"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "QuoteRequiredInPublicID"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "PublicIDUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "PubidCharIllegal"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "SpaceRequiredBetweenPublicAndSystem"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ROOT_ELEMENT_TYPE_REQUIRED"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "DoctypedeclUnterminated"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "PEReferenceWithinMarkup"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_MARKUP_NOT_RECOGNIZED_IN_DTD"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_CONTENTSPEC_REQUIRED_IN_ELEMENTDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementDeclUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_CLOSE_PAREN_REQUIRED_IN_MIXED"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MixedContentUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "AttNameRequiredInAttDef"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "AttTypeRequiredInAttDef"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ATTRIBUTE_DEFINITION"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_NAME_REQUIRED_IN_NOTATIONTYPE"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "NotationTypeUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_NMTOKEN_REQUIRED_IN_ENUMERATION"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "EnumerationUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DISTINCT_TOKENS_IN_ENUMERATION"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DISTINCT_NOTATION_IN_ENUMERATION"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "IncludeSectUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "IgnoreSectUnterminated"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "NameRequiredInPEReference"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "SemicolonRequiredInPEReference"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_PEDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_NDATA_IN_UNPARSED_ENTITYDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "EntityDeclUnterminated"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ENTITY_DEFINITION"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ExternalIDRequired"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_PUBIDLITERAL_IN_EXTERNALID"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_AFTER_PUBIDLITERAL_IN_EXTERNALID"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_SYSTEMLITERAL_IN_EXTERNALID"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_URI_FRAGMENT_IN_SYSTEMID"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ExternalIDorPublicIDRequired"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "NotationDeclUnterminated"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ReferenceToExternalEntity"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ReferenceToUnparsedEntity"), "wf-invalid-character");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingNotSupported"), "unsupported-encoding");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingRequired"), "unsupported-encoding");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "IllegalQName"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementXMLNSPrefix"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementPrefixUnbound"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "AttributePrefixUnbound"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "EmptyPrefixedAttName"), "wf-invalid-character-in-node-name");
            hashMap.put(new XMLErrorCode("http://www.w3.org/TR/1998/REC-xml-19980210", "PrefixDeclared"), "wf-invalid-character-in-node-name");
            fgDOMErrorTypeTable = Collections.unmodifiableMap(hashMap);
        }

        public static String getDOMErrorType(XMLErrorCode xMLErrorCode) {
            return fgDOMErrorTypeTable.get(xMLErrorCode);
        }

        private DOMErrorTypeMap() {
        }
    }
}
