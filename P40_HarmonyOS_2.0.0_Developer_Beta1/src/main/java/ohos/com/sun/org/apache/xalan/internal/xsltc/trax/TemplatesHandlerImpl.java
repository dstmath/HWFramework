package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.util.ArrayList;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Parser;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.Templates;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.sax.TemplatesHandler;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;

public class TemplatesHandlerImpl implements ContentHandler, TemplatesHandler, SourceLoader {
    private int _indentNumber;
    private Parser _parser = null;
    private String _systemId;
    private TemplatesImpl _templates = null;
    private TransformerFactoryImpl _tfactory = null;
    private URIResolver _uriResolver = null;

    protected TemplatesHandlerImpl(int i, TransformerFactoryImpl transformerFactoryImpl) {
        this._indentNumber = i;
        this._tfactory = transformerFactoryImpl;
        XSLTC xsltc = new XSLTC(transformerFactoryImpl.getJdkXmlFeatures());
        if (transformerFactoryImpl.getFeature(Constants.FEATURE_SECURE_PROCESSING)) {
            xsltc.setSecureProcessing(true);
        }
        xsltc.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet", (String) transformerFactoryImpl.getAttribute("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet"));
        xsltc.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", (String) transformerFactoryImpl.getAttribute("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD"));
        xsltc.setProperty("http://apache.org/xml/properties/security-manager", transformerFactoryImpl.getAttribute("http://apache.org/xml/properties/security-manager"));
        if ("true".equals(transformerFactoryImpl.getAttribute(TransformerFactoryImpl.ENABLE_INLINING))) {
            xsltc.setTemplateInlining(true);
        } else {
            xsltc.setTemplateInlining(false);
        }
        this._parser = xsltc.getParser();
    }

    public String getSystemId() {
        return this._systemId;
    }

    public void setSystemId(String str) {
        this._systemId = str;
    }

    public void setURIResolver(URIResolver uRIResolver) {
        this._uriResolver = uRIResolver;
    }

    public Templates getTemplates() {
        return this._templates;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader
    public InputSource loadSource(String str, String str2, XSLTC xsltc) {
        try {
            Source resolve = this._uriResolver.resolve(str, str2);
            if (resolve != null) {
                return Util.getInputSource(xsltc, resolve);
            }
            return null;
        } catch (TransformerException unused) {
            return null;
        }
    }

    public void startDocument() {
        XSLTC xsltc = this._parser.getXSLTC();
        xsltc.init();
        xsltc.setOutputType(2);
        this._parser.startDocument();
    }

    public void endDocument() throws SAXException {
        String str;
        Stylesheet stylesheet;
        this._parser.endDocument();
        try {
            XSLTC xsltc = this._parser.getXSLTC();
            if (this._systemId != null) {
                str = Util.baseName(this._systemId);
            } else {
                str = (String) this._tfactory.getAttribute(TransformerFactoryImpl.TRANSLET_NAME);
            }
            xsltc.setClassName(str);
            String className = xsltc.getClassName();
            SyntaxTreeNode documentRoot = this._parser.getDocumentRoot();
            if (this._parser.errorsFound() || documentRoot == null) {
                stylesheet = null;
            } else {
                stylesheet = this._parser.makeStylesheet(documentRoot);
                stylesheet.setSystemId(this._systemId);
                stylesheet.setParentStylesheet(null);
                if (xsltc.getTemplateInlining()) {
                    stylesheet.setTemplateInlining(true);
                } else {
                    stylesheet.setTemplateInlining(false);
                }
                if (this._uriResolver != null) {
                    stylesheet.setSourceLoader(this);
                }
                this._parser.setCurrentStylesheet(stylesheet);
                xsltc.setStylesheet(stylesheet);
                this._parser.createAST(stylesheet);
            }
            if (!this._parser.errorsFound() && stylesheet != null) {
                stylesheet.setMultiDocument(xsltc.isMultiDocument());
                stylesheet.setHasIdCall(xsltc.hasIdCall());
                synchronized (xsltc.getClass()) {
                    stylesheet.translate();
                }
            }
            if (this._parser.errorsFound()) {
                StringBuilder sb = new StringBuilder();
                ArrayList<ErrorMsg> errors = this._parser.getErrors();
                int size = errors.size();
                for (int i = 0; i < size; i++) {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(errors.get(i).toString());
                }
                throw new SAXException(ErrorMsg.JAXP_COMPILE_ERR, new TransformerException(sb.toString()));
            } else if (xsltc.getBytecodes() != null) {
                this._templates = new TemplatesImpl(xsltc.getBytecodes(), className, this._parser.getOutputProperties(), this._indentNumber, this._tfactory);
                if (this._uriResolver != null) {
                    this._templates.setURIResolver(this._uriResolver);
                }
            }
        } catch (CompilerException e) {
            throw new SAXException(ErrorMsg.JAXP_COMPILE_ERR, e);
        }
    }

    public void startPrefixMapping(String str, String str2) {
        this._parser.startPrefixMapping(str, str2);
    }

    public void endPrefixMapping(String str) {
        this._parser.endPrefixMapping(str);
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        this._parser.startElement(str, str2, str3, attributes);
    }

    public void endElement(String str, String str2, String str3) {
        this._parser.endElement(str, str2, str3);
    }

    public void characters(char[] cArr, int i, int i2) {
        this._parser.characters(cArr, i, i2);
    }

    public void processingInstruction(String str, String str2) {
        this._parser.processingInstruction(str, str2);
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) {
        this._parser.ignorableWhitespace(cArr, i, i2);
    }

    public void skippedEntity(String str) {
        this._parser.skippedEntity(str);
    }

    public void setDocumentLocator(Locator locator) {
        setSystemId(locator.getSystemId());
        this._parser.setDocumentLocator(locator);
    }
}
