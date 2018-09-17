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

    /* JADX WARNING: Removed duplicated region for block: B:40:0x007a A:{Splitter: B:25:0x0053, ExcHandler: javax.xml.parsers.FactoryConfigurationError (r3_0 'ex1' javax.xml.parsers.FactoryConfigurationError)} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x008d A:{Splitter: B:25:0x0053, ExcHandler: java.lang.NoSuchMethodError (e java.lang.NoSuchMethodError)} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x008f A:{Splitter: B:25:0x0053, ExcHandler: java.lang.AbstractMethodError (e java.lang.AbstractMethodError)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x007a A:{Splitter: B:25:0x0053, ExcHandler: javax.xml.parsers.FactoryConfigurationError (r3_0 'ex1' javax.xml.parsers.FactoryConfigurationError)} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x008d A:{Splitter: B:25:0x0053, ExcHandler: java.lang.NoSuchMethodError (e java.lang.NoSuchMethodError)} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x008f A:{Splitter: B:25:0x0053, ExcHandler: java.lang.AbstractMethodError (e java.lang.AbstractMethodError)} */
    /* JADX WARNING: Missing block: B:40:0x007a, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:43:0x0084, code:
            throw new org.xml.sax.SAXException(r3.toString());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        if (!threadHasReader || this.m_inUse.get(reader) == Boolean.TRUE) {
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
                } catch (NoSuchMethodError e2) {
                } catch (AbstractMethodError e3) {
                } catch (ParserConfigurationException ex) {
                    throw new SAXException(ex);
                }
            }
            try {
                reader.setFeature(NAMESPACES_FEATURE, true);
                reader.setFeature(NAMESPACE_PREFIXES_FEATURE, false);
            } catch (SAXException e4) {
            }
            if (!threadHasReader) {
                this.m_readers.set(reader);
                this.m_inUse.put(reader, Boolean.TRUE);
            }
        } else {
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
