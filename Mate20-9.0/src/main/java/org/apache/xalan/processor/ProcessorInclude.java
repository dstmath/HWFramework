package org.apache.xalan.processor;

import java.io.IOException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.utils.DOM2Helper;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.TreeWalker;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class ProcessorInclude extends XSLTElementProcessor {
    static final long serialVersionUID = -4570078731972673481L;
    private String m_href = null;

    public String getHref() {
        return this.m_href;
    }

    public void setHref(String baseIdent) {
        this.m_href = baseIdent;
    }

    /* access modifiers changed from: protected */
    public int getStylesheetType() {
        return 2;
    }

    /* access modifiers changed from: protected */
    public String getStylesheetInclErr() {
        return XSLTErrorResources.ER_STYLESHEET_INCLUDES_ITSELF;
    }

    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        int savedStylesheetType;
        setPropertiesFromAttributes(handler, rawName, attributes, this);
        try {
            Source sourceFromURIResolver = getSourceFromUriResolver(handler);
            String hrefUrl = getBaseURIOfIncludedStylesheet(handler, sourceFromURIResolver);
            if (!handler.importStackContains(hrefUrl)) {
                handler.pushImportURL(hrefUrl);
                handler.pushImportSource(sourceFromURIResolver);
                savedStylesheetType = handler.getStylesheetType();
                handler.setStylesheetType(getStylesheetType());
                handler.pushNewNamespaceSupport();
                parse(handler, uri, localName, rawName, attributes);
                handler.setStylesheetType(savedStylesheetType);
                handler.popImportURL();
                handler.popImportSource();
                handler.popNamespaceSupport();
                return;
            }
            throw new SAXException(XSLMessages.createMessage(getStylesheetInclErr(), new Object[]{hrefUrl}));
        } catch (TransformerException te) {
            handler.error(te.getMessage(), te);
        } catch (Throwable th) {
            handler.setStylesheetType(savedStylesheetType);
            handler.popImportURL();
            handler.popImportSource();
            handler.popNamespaceSupport();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void parse(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        Source source = null;
        if (handler.getStylesheetProcessor().getURIResolver() != null) {
            try {
                source = handler.peekSourceFromURIResolver();
                if (source != null && (source instanceof DOMSource)) {
                    Node node = ((DOMSource) source).getNode();
                    String systemId = handler.peekImportURL();
                    if (systemId != null) {
                        handler.pushBaseIndentifier(systemId);
                    }
                    new TreeWalker(handler, new DOM2Helper(), systemId).traverse(node);
                    if (systemId != null) {
                        handler.popBaseIndentifier();
                    }
                    return;
                }
            } catch (ParserConfigurationException ex) {
                throw new SAXException(ex);
            } catch (FactoryConfigurationError ex1) {
                throw new SAXException(ex1.toString());
            } catch (AbstractMethodError | NoSuchMethodError e) {
            } catch (SAXException se) {
                throw new TransformerException(se);
            } catch (IOException ioe) {
                handler.error(XSLTErrorResources.ER_IOEXCEPTION, new Object[]{getHref()}, ioe);
            } catch (TransformerException te) {
                handler.error(te.getMessage(), te);
            } catch (Throwable th) {
                handler.popBaseIndentifier();
                throw th;
            }
        }
        if (source == null) {
            source = new StreamSource(SystemIDResolver.getAbsoluteURI(getHref(), handler.getBaseIdentifier()));
        }
        Source source2 = processSource(handler, source);
        XMLReader reader = null;
        if (source2 instanceof SAXSource) {
            reader = ((SAXSource) source2).getXMLReader();
        }
        InputSource inputSource = SAXSource.sourceToInputSource(source2);
        if (reader == null) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            if (handler.getStylesheetProcessor().isSecureProcessing()) {
                try {
                    factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                } catch (SAXException e2) {
                }
            }
            reader = factory.newSAXParser().getXMLReader();
        }
        if (reader == null) {
            reader = XMLReaderFactory.createXMLReader();
        }
        if (reader != null) {
            reader.setContentHandler(handler);
            handler.pushBaseIndentifier(inputSource.getSystemId());
            reader.parse(inputSource);
            handler.popBaseIndentifier();
        }
    }

    /* access modifiers changed from: protected */
    public Source processSource(StylesheetHandler handler, Source source) {
        return source;
    }

    private Source getSourceFromUriResolver(StylesheetHandler handler) throws TransformerException {
        URIResolver uriresolver = handler.getStylesheetProcessor().getURIResolver();
        if (uriresolver != null) {
            return uriresolver.resolve(getHref(), handler.getBaseIdentifier());
        }
        return null;
    }

    private String getBaseURIOfIncludedStylesheet(StylesheetHandler handler, Source s) throws TransformerException {
        if (s != null) {
            String systemId = s.getSystemId();
            String idFromUriResolverSource = systemId;
            if (systemId != null) {
                return idFromUriResolverSource;
            }
        }
        return SystemIDResolver.getAbsoluteURI(getHref(), handler.getBaseIdentifier());
    }
}
