package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import ohos.com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMWSFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.dom.DOMResult;
import ohos.javax.xml.transform.sax.TransformerHandler;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.DeclHandler;
import ohos.org.xml.sax.ext.LexicalHandler;
import ohos.org.xml.sax.helpers.DefaultHandler;

public class TransformerHandlerImpl implements TransformerHandler, DeclHandler {
    private DeclHandler _declHandler = null;
    private SAXImpl _dom = null;
    private boolean _done = false;
    private DTDHandler _dtdHandler = null;
    private ContentHandler _handler = null;
    private boolean _isIdentity = false;
    private LexicalHandler _lexHandler = null;
    private Locator _locator = null;
    private Result _result = null;
    private String _systemId;
    private TransformerImpl _transformer;
    private AbstractTranslet _translet = null;

    public TransformerHandlerImpl(TransformerImpl transformerImpl) {
        this._transformer = transformerImpl;
        if (transformerImpl.isIdentity()) {
            this._handler = new DefaultHandler();
            this._isIdentity = true;
            return;
        }
        this._translet = this._transformer.getTranslet();
    }

    public String getSystemId() {
        return this._systemId;
    }

    public void setSystemId(String str) {
        this._systemId = str;
    }

    public Transformer getTransformer() {
        return this._transformer;
    }

    public void setResult(Result result) throws IllegalArgumentException {
        this._result = result;
        if (result == null) {
            throw new IllegalArgumentException(new ErrorMsg("ER_RESULT_NULL").toString());
        } else if (this._isIdentity) {
            try {
                SerializationHandler outputHandler = this._transformer.getOutputHandler(result);
                this._transformer.transferOutputProperties(outputHandler);
                this._handler = outputHandler;
                this._lexHandler = outputHandler;
            } catch (TransformerException unused) {
                this._result = null;
            }
        } else if (this._done) {
            try {
                this._transformer.setDOM(this._dom);
                this._transformer.transform(null, this._result);
            } catch (TransformerException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        this._handler.characters(cArr, i, i2);
    }

    public void startDocument() throws SAXException {
        if (this._result != null) {
            if (!this._isIdentity) {
                AbstractTranslet abstractTranslet = this._translet;
                boolean hasIdCall = abstractTranslet != null ? abstractTranslet.hasIdCall() : false;
                try {
                    XSLTCDTMManager createNewDTMManagerInstance = this._transformer.getTransformerFactory().createNewDTMManagerInstance();
                    AbstractTranslet abstractTranslet2 = this._translet;
                    this._dom = (SAXImpl) createNewDTMManagerInstance.getDTM(null, false, (abstractTranslet2 == null || !(abstractTranslet2 instanceof StripFilter)) ? null : new DOMWSFilter(abstractTranslet2), true, false, hasIdCall);
                    this._handler = this._dom.getBuilder();
                    DeclHandler declHandler = this._handler;
                    this._lexHandler = (LexicalHandler) declHandler;
                    this._dtdHandler = (DTDHandler) declHandler;
                    this._declHandler = declHandler;
                    this._dom.setDocumentURI(this._systemId);
                    Locator locator = this._locator;
                    if (locator != null) {
                        this._handler.setDocumentLocator(locator);
                    }
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            }
            this._handler.startDocument();
            return;
        }
        throw new SAXException(new ErrorMsg(ErrorMsg.JAXP_SET_RESULT_ERR).toString());
    }

    public void endDocument() throws SAXException {
        this._handler.endDocument();
        if (!this._isIdentity) {
            if (this._result != null) {
                try {
                    this._transformer.setDOM(this._dom);
                    this._transformer.transform(null, this._result);
                } catch (TransformerException e) {
                    throw new SAXException(e);
                }
            }
            this._done = true;
            this._transformer.setDOM(this._dom);
        }
        if (this._isIdentity) {
            DOMResult dOMResult = this._result;
            if (dOMResult instanceof DOMResult) {
                dOMResult.setNode(this._transformer.getTransletOutputHandlerFactory().getNode());
            }
        }
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        this._handler.startElement(str, str2, str3, attributes);
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        this._handler.endElement(str, str2, str3);
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        this._handler.processingInstruction(str, str2);
    }

    public void startCDATA() throws SAXException {
        LexicalHandler lexicalHandler = this._lexHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }

    public void endCDATA() throws SAXException {
        LexicalHandler lexicalHandler = this._lexHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        LexicalHandler lexicalHandler = this._lexHandler;
        if (lexicalHandler != null) {
            lexicalHandler.comment(cArr, i, i2);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        this._handler.ignorableWhitespace(cArr, i, i2);
    }

    public void setDocumentLocator(Locator locator) {
        this._locator = locator;
        ContentHandler contentHandler = this._handler;
        if (contentHandler != null) {
            contentHandler.setDocumentLocator(locator);
        }
    }

    public void skippedEntity(String str) throws SAXException {
        this._handler.skippedEntity(str);
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
        this._handler.startPrefixMapping(str, str2);
    }

    public void endPrefixMapping(String str) throws SAXException {
        this._handler.endPrefixMapping(str);
    }

    public void startDTD(String str, String str2, String str3) throws SAXException {
        LexicalHandler lexicalHandler = this._lexHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startDTD(str, str2, str3);
        }
    }

    public void endDTD() throws SAXException {
        LexicalHandler lexicalHandler = this._lexHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endDTD();
        }
    }

    public void startEntity(String str) throws SAXException {
        LexicalHandler lexicalHandler = this._lexHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startEntity(str);
        }
    }

    public void endEntity(String str) throws SAXException {
        LexicalHandler lexicalHandler = this._lexHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endEntity(str);
        }
    }

    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        DTDHandler dTDHandler = this._dtdHandler;
        if (dTDHandler != null) {
            dTDHandler.unparsedEntityDecl(str, str2, str3, str4);
        }
    }

    public void notationDecl(String str, String str2, String str3) throws SAXException {
        DTDHandler dTDHandler = this._dtdHandler;
        if (dTDHandler != null) {
            dTDHandler.notationDecl(str, str2, str3);
        }
    }

    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
        DeclHandler declHandler = this._declHandler;
        if (declHandler != null) {
            declHandler.attributeDecl(str, str2, str3, str4, str5);
        }
    }

    public void elementDecl(String str, String str2) throws SAXException {
        DeclHandler declHandler = this._declHandler;
        if (declHandler != null) {
            declHandler.elementDecl(str, str2);
        }
    }

    public void externalEntityDecl(String str, String str2, String str3) throws SAXException {
        DeclHandler declHandler = this._declHandler;
        if (declHandler != null) {
            declHandler.externalEntityDecl(str, str2, str3);
        }
    }

    public void internalEntityDecl(String str, String str2) throws SAXException {
        DeclHandler declHandler = this._declHandler;
        if (declHandler != null) {
            declHandler.internalEntityDecl(str, str2);
        }
    }

    public void reset() {
        this._systemId = null;
        this._dom = null;
        this._handler = null;
        this._lexHandler = null;
        this._dtdHandler = null;
        this._declHandler = null;
        this._result = null;
        this._locator = null;
    }
}
