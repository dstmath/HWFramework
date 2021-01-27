package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.io.IOException;
import ohos.com.sun.org.apache.xml.internal.utils.XMLReaderManager;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.Templates;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.sax.SAXResult;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.helpers.XMLFilterImpl;

public class TrAXFilter extends XMLFilterImpl {
    private boolean _overrideDefaultParser = this._transformer.overrideDefaultParser();
    private Templates _templates;
    private TransformerImpl _transformer;
    private TransformerHandlerImpl _transformerHandler = new TransformerHandlerImpl(this._transformer);

    public void setErrorListener(ErrorListener errorListener) {
    }

    public TrAXFilter(Templates templates) throws TransformerConfigurationException {
        this._templates = templates;
        this._transformer = templates.newTransformer();
    }

    public Transformer getTransformer() {
        return this._transformer;
    }

    private void createParent() throws SAXException {
        setParent(JdkXmlUtils.getXMLReader(this._overrideDefaultParser, this._transformer.isSecureProcessing()));
    }

    public void parse(InputSource inputSource) throws SAXException, IOException {
        XMLReader xMLReader = null;
        try {
            if (getParent() == null) {
                try {
                    xMLReader = XMLReaderManager.getInstance(this._overrideDefaultParser).getXMLReader();
                    setParent(xMLReader);
                } catch (SAXException e) {
                    throw new SAXException(e.toString());
                }
            }
            getParent().parse(inputSource);
        } finally {
            if (xMLReader != null) {
                XMLReaderManager.getInstance(this._overrideDefaultParser).releaseXMLReader(xMLReader);
            }
        }
    }

    public void parse(String str) throws SAXException, IOException {
        parse(new InputSource(str));
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this._transformerHandler.setResult(new SAXResult(contentHandler));
        if (getParent() == null) {
            try {
                createParent();
            } catch (SAXException unused) {
                return;
            }
        }
        getParent().setContentHandler(this._transformerHandler);
    }
}
