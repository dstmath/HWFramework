package org.apache.xalan.transformer;

import java.io.IOException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

public class TrAXFilter extends XMLFilterImpl {
    private Templates m_templates;
    private TransformerImpl m_transformer;

    public TrAXFilter(Templates templates) throws TransformerConfigurationException {
        this.m_templates = templates;
        this.m_transformer = (TransformerImpl) templates.newTransformer();
    }

    public TransformerImpl getTransformer() {
        return this.m_transformer;
    }

    public void setParent(XMLReader parent) {
        super.setParent(parent);
        if (parent.getContentHandler() != null) {
            setContentHandler(parent.getContentHandler());
        }
        setupParse();
    }

    public void parse(InputSource input) throws SAXException, IOException {
        if (getParent() == null) {
            XMLReader parent;
            XMLReader reader = null;
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                if (this.m_transformer.getStylesheet().isSecureProcessing()) {
                    try {
                        factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                    } catch (SAXException e) {
                    }
                }
                reader = factory.newSAXParser().getXMLReader();
            } catch (ParserConfigurationException ex) {
                throw new SAXException(ex);
            } catch (FactoryConfigurationError ex1) {
                throw new SAXException(ex1.toString());
            } catch (NoSuchMethodError e2) {
            } catch (AbstractMethodError e3) {
            }
            if (reader == null) {
                parent = XMLReaderFactory.createXMLReader();
            } else {
                parent = reader;
            }
            try {
                parent.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            } catch (SAXException e4) {
            }
            setParent(parent);
        } else {
            setupParse();
        }
        if (this.m_transformer.getContentHandler() == null) {
            throw new SAXException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_CALL_PARSE, null));
        }
        getParent().parse(input);
        Exception e5 = this.m_transformer.getExceptionThrown();
        if (e5 == null) {
            return;
        }
        if (e5 instanceof SAXException) {
            throw ((SAXException) e5);
        }
        throw new SAXException(e5);
    }

    public void parse(String systemId) throws SAXException, IOException {
        parse(new InputSource(systemId));
    }

    private void setupParse() {
        XMLReader p = getParent();
        if (p == null) {
            throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_PARENT_FOR_FILTER, null));
        }
        p.setContentHandler(this.m_transformer.getInputContentHandler());
        p.setEntityResolver(this);
        p.setDTDHandler(this);
        p.setErrorHandler(this);
    }

    public void setContentHandler(ContentHandler handler) {
        this.m_transformer.setContentHandler(handler);
    }

    public void setErrorListener(ErrorListener handler) {
        this.m_transformer.setErrorListener(handler);
    }
}
