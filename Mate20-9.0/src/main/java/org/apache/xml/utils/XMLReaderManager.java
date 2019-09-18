package org.apache.xml.utils;

import java.util.Hashtable;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLReaderManager {
    private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    private static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
    private static SAXParserFactory m_parserFactory;
    private static final XMLReaderManager m_singletonManager = new XMLReaderManager();
    private Hashtable m_inUse;
    private ThreadLocal m_readers;

    private XMLReaderManager() {
    }

    public static XMLReaderManager getInstance() {
        return m_singletonManager;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0045, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0091, code lost:
        throw new org.xml.sax.SAXException(r1.toString());
     */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0041 A[ExcHandler: AbstractMethodError | NoSuchMethodError (e java.lang.Throwable), Splitter:B:24:0x004a] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0045 A[ExcHandler: FactoryConfigurationError (r1v6 'ex1' javax.xml.parsers.FactoryConfigurationError A[CUSTOM_DECLARE]), Splitter:B:24:0x004a] */
    public synchronized XMLReader getXMLReader() throws SAXException {
        XMLReader reader;
        if (this.m_readers == null) {
            this.m_readers = new ThreadLocal();
        }
        if (this.m_inUse == null) {
            this.m_inUse = new Hashtable();
        }
        reader = (XMLReader) this.m_readers.get();
        boolean threadHasReader = reader != null;
        if (threadHasReader) {
            if (this.m_inUse.get(reader) != Boolean.TRUE) {
                this.m_inUse.put(reader, Boolean.TRUE);
            }
        }
        try {
            reader = XMLReaderFactory.createXMLReader();
        } catch (Exception e) {
            try {
                if (m_parserFactory == null) {
                    m_parserFactory = SAXParserFactory.newInstance();
                    m_parserFactory.setNamespaceAware(true);
                }
                reader = m_parserFactory.newSAXParser().getXMLReader();
            } catch (ParserConfigurationException pce) {
                throw pce;
            } catch (FactoryConfigurationError ex1) {
            } catch (AbstractMethodError | NoSuchMethodError e2) {
            } catch (ParserConfigurationException ex) {
                throw new SAXException(ex);
            }
        }
        try {
            reader.setFeature(NAMESPACES_FEATURE, true);
            reader.setFeature(NAMESPACE_PREFIXES_FEATURE, false);
        } catch (SAXException e3) {
        }
        if (!threadHasReader) {
            this.m_readers.set(reader);
            this.m_inUse.put(reader, Boolean.TRUE);
        }
        return reader;
    }

    public synchronized void releaseXMLReader(XMLReader reader) {
        if (this.m_readers.get() == reader && reader != null) {
            this.m_inUse.remove(reader);
        }
    }
}
