package ohos.com.sun.org.apache.xpath.internal;

import java.io.IOException;
import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.javax.xml.parsers.FactoryConfigurationError;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.SourceLocator;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.helpers.XMLReaderFactory;

public class SourceTreeManager {
    private Vector m_sourceTree = new Vector();
    URIResolver m_uriResolver;

    public void reset() {
        this.m_sourceTree = new Vector();
    }

    public void setURIResolver(URIResolver uRIResolver) {
        this.m_uriResolver = uRIResolver;
    }

    public URIResolver getURIResolver() {
        return this.m_uriResolver;
    }

    public String findURIFromDoc(int i) {
        int size = this.m_sourceTree.size();
        for (int i2 = 0; i2 < size; i2++) {
            SourceTree sourceTree = (SourceTree) this.m_sourceTree.elementAt(i2);
            if (i == sourceTree.m_root) {
                return sourceTree.m_url;
            }
        }
        return null;
    }

    public Source resolveURI(String str, String str2, SourceLocator sourceLocator) throws TransformerException, IOException {
        URIResolver uRIResolver = this.m_uriResolver;
        Source resolve = uRIResolver != null ? uRIResolver.resolve(str2, str) : null;
        return resolve == null ? new StreamSource(SystemIDResolver.getAbsoluteURI(str2, str)) : resolve;
    }

    public void removeDocumentFromCache(int i) {
        if (-1 != i) {
            for (int size = this.m_sourceTree.size() - 1; size >= 0; size--) {
                SourceTree sourceTree = (SourceTree) this.m_sourceTree.elementAt(size);
                if (sourceTree != null && sourceTree.m_root == i) {
                    this.m_sourceTree.removeElementAt(size);
                    return;
                }
            }
        }
    }

    public void putDocumentInCache(int i, Source source) {
        int node = getNode(source);
        if (-1 != node) {
            if (node != i) {
                throw new RuntimeException("Programmer's Error!  putDocumentInCache found reparse of doc: " + source.getSystemId());
            }
        } else if (source.getSystemId() != null) {
            this.m_sourceTree.addElement(new SourceTree(i, source.getSystemId()));
        }
    }

    public int getNode(Source source) {
        String systemId = source.getSystemId();
        if (systemId == null) {
            return -1;
        }
        int size = this.m_sourceTree.size();
        for (int i = 0; i < size; i++) {
            SourceTree sourceTree = (SourceTree) this.m_sourceTree.elementAt(i);
            if (systemId.equals(sourceTree.m_url)) {
                return sourceTree.m_root;
            }
        }
        return -1;
    }

    public int getSourceTree(String str, String str2, SourceLocator sourceLocator, XPathContext xPathContext) throws TransformerException {
        try {
            return getSourceTree(resolveURI(str, str2, sourceLocator), sourceLocator, xPathContext);
        } catch (IOException e) {
            throw new TransformerException(e.getMessage(), sourceLocator, e);
        }
    }

    public int getSourceTree(Source source, SourceLocator sourceLocator, XPathContext xPathContext) throws TransformerException {
        int node = getNode(source);
        if (-1 != node) {
            return node;
        }
        int parseToNode = parseToNode(source, sourceLocator, xPathContext);
        if (-1 != parseToNode) {
            putDocumentInCache(parseToNode, source);
        }
        return parseToNode;
    }

    public int parseToNode(Source source, SourceLocator sourceLocator, XPathContext xPathContext) throws TransformerException {
        DTM dtm;
        try {
            Object ownerObject = xPathContext.getOwnerObject();
            if (ownerObject == null || !(ownerObject instanceof DTMWSFilter)) {
                dtm = xPathContext.getDTM(source, false, null, false, true);
            } else {
                dtm = xPathContext.getDTM(source, false, (DTMWSFilter) ownerObject, false, true);
            }
            return dtm.getDocument();
        } catch (Exception e) {
            throw new TransformerException(e.getMessage(), sourceLocator, e);
        }
    }

    public static XMLReader getXMLReader(Source source, SourceLocator sourceLocator) throws TransformerException {
        try {
            XMLReader xMLReader = source instanceof SAXSource ? ((SAXSource) source).getXMLReader() : null;
            if (xMLReader == null) {
                try {
                    SAXParserFactory newInstance = SAXParserFactory.newInstance();
                    newInstance.setNamespaceAware(true);
                    xMLReader = newInstance.newSAXParser().getXMLReader();
                } catch (ParserConfigurationException e) {
                    throw new SAXException(e);
                } catch (FactoryConfigurationError e2) {
                    throw new SAXException(e2.toString());
                } catch (AbstractMethodError | NoSuchMethodError unused) {
                }
                if (xMLReader == null) {
                    xMLReader = XMLReaderFactory.createXMLReader();
                }
            }
            try {
                xMLReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            } catch (SAXException unused2) {
            }
            return xMLReader;
        } catch (SAXException e3) {
            throw new TransformerException(e3.getMessage(), sourceLocator, e3);
        }
    }
}
