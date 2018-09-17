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
    private static final XMLReaderManager m_singletonManager = null;
    private Hashtable m_inUse;
    private ThreadLocal m_readers;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.utils.XMLReaderManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.utils.XMLReaderManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.utils.XMLReaderManager.<clinit>():void");
    }

    private XMLReaderManager() {
    }

    public static XMLReaderManager getInstance() {
        return m_singletonManager;
    }

    public synchronized XMLReader getXMLReader() throws SAXException {
        XMLReader reader;
        boolean threadHasReader = true;
        synchronized (this) {
            if (this.m_readers == null) {
                this.m_readers = new ThreadLocal();
            }
            if (this.m_inUse == null) {
                this.m_inUse = new Hashtable();
            }
            reader = (XMLReader) this.m_readers.get();
            if (reader == null) {
                threadHasReader = false;
            }
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
                        throw new SAXException(ex1.toString());
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
        }
        return reader;
    }

    public synchronized void releaseXMLReader(XMLReader reader) {
        if (this.m_readers.get() == reader && reader != null) {
            this.m_inUse.remove(reader);
        }
    }
}
