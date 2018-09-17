package org.apache.xml.dtm.ref;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMException;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.dtm.ref.dom2dtm.DOM2DTM;
import org.apache.xml.dtm.ref.dom2dtm.DOM2DTMdefaultNamespaceDeclarationNode;
import org.apache.xml.dtm.ref.sax2dtm.SAX2DTM;
import org.apache.xml.dtm.ref.sax2dtm.SAX2RTFDTM;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.SuballocatedIntVector;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xml.utils.XMLReaderManager;
import org.apache.xml.utils.XMLStringFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class DTMManagerDefault extends DTMManager {
    private static final boolean DEBUG = false;
    private static final boolean DUMPTREE = false;
    protected DefaultHandler m_defaultHandler = new DefaultHandler();
    int[] m_dtm_offsets = new int[DTMFilter.SHOW_DOCUMENT];
    protected DTM[] m_dtms = new DTM[DTMFilter.SHOW_DOCUMENT];
    private ExpandedNameTable m_expandedNameTable = new ExpandedNameTable();
    protected XMLReaderManager m_readerManager = null;

    public synchronized void addDTM(DTM dtm, int id) {
        addDTM(dtm, id, 0);
    }

    public synchronized void addDTM(DTM dtm, int id, int offset) {
        if (id >= 65536) {
            throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NO_DTMIDS_AVAIL, null));
        }
        int oldlen = this.m_dtms.length;
        if (oldlen <= id) {
            int newlen = Math.min(id + DTMFilter.SHOW_DOCUMENT, 65536);
            DTM[] new_m_dtms = new DTM[newlen];
            System.arraycopy(this.m_dtms, 0, new_m_dtms, 0, oldlen);
            this.m_dtms = new_m_dtms;
            int[] new_m_dtm_offsets = new int[newlen];
            System.arraycopy(this.m_dtm_offsets, 0, new_m_dtm_offsets, 0, oldlen);
            this.m_dtm_offsets = new_m_dtm_offsets;
        }
        this.m_dtms[id] = dtm;
        this.m_dtm_offsets[id] = offset;
        dtm.documentRegistration();
    }

    public synchronized int getFirstFreeDTMID() {
        int n = this.m_dtms.length;
        for (int i = 1; i < n; i++) {
            if (this.m_dtms[i] == null) {
                return i;
            }
        }
        return n;
    }

    public synchronized DTM getDTM(Source source, boolean unique, DTMWSFilter whiteSpaceFilter, boolean incremental, boolean doIndexing) {
        SAX2DTM dtm;
        XMLStringFactory xstringFactory = this.m_xsf;
        int dtmPos = getFirstFreeDTMID();
        int documentID = dtmPos << 16;
        if (source == null || !(source instanceof DOMSource)) {
            boolean isSAXSource = source != null ? source instanceof SAXSource : true;
            boolean isStreamSource = source != null ? source instanceof StreamSource : false;
            if (isSAXSource || isStreamSource) {
                InputSource xmlSource;
                boolean haveXercesParser;
                XMLReader reader = null;
                if (source == null) {
                    xmlSource = null;
                } else {
                    reader = getXMLReader(source);
                    xmlSource = SAXSource.sourceToInputSource(source);
                    String urlOfSource = xmlSource.getSystemId();
                    if (urlOfSource != null) {
                        try {
                            urlOfSource = SystemIDResolver.getAbsoluteURI(urlOfSource);
                        } catch (Exception e) {
                            System.err.println("Can not absolutize URL: " + urlOfSource);
                        }
                        try {
                            xmlSource.setSystemId(urlOfSource);
                        } catch (RuntimeException re) {
                            dtm.clearCoRoutine();
                            throw re;
                        } catch (Exception e2) {
                            dtm.clearCoRoutine();
                            throw new WrappedRuntimeException(e2);
                        } catch (RuntimeException re2) {
                            dtm.clearCoRoutine();
                            throw re2;
                        } catch (Exception e22) {
                            dtm.clearCoRoutine();
                            throw new WrappedRuntimeException(e22);
                        } catch (Throwable th) {
                            if (reader != null) {
                                if (((this.m_incremental ? incremental : 0) ^ 1) != 0) {
                                    reader.setContentHandler(this.m_defaultHandler);
                                    reader.setDTDHandler(this.m_defaultHandler);
                                    reader.setErrorHandler(this.m_defaultHandler);
                                    try {
                                        reader.setProperty("http://xml.org/sax/properties/lexical-handler", null);
                                    } catch (Exception e3) {
                                    }
                                }
                            }
                            releaseXMLReader(reader);
                        }
                    }
                }
                if (source != null || !unique || (incremental ^ 1) == 0 || (doIndexing ^ 1) == 0) {
                    dtm = new SAX2DTM(this, source, documentID, whiteSpaceFilter, xstringFactory, doIndexing);
                } else {
                    dtm = new SAX2RTFDTM(this, source, documentID, whiteSpaceFilter, xstringFactory, doIndexing);
                }
                addDTM(dtm, dtmPos, 0);
                if (reader != null) {
                    haveXercesParser = reader.getClass().getName().equals("org.apache.xerces.parsers.SAXParser");
                } else {
                    haveXercesParser = false;
                }
                if (haveXercesParser) {
                    incremental = true;
                }
                if (this.m_incremental && incremental) {
                    IncrementalSAXSource coParser = null;
                    if (haveXercesParser) {
                        try {
                            coParser = (IncrementalSAXSource) Class.forName("org.apache.xml.dtm.ref.IncrementalSAXSource_Xerces").newInstance();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            coParser = null;
                        }
                    }
                    if (coParser == null) {
                        if (reader == null) {
                            coParser = new IncrementalSAXSource_Filter();
                        } else {
                            IncrementalSAXSource_Filter filter = new IncrementalSAXSource_Filter();
                            filter.setXMLReader(reader);
                            Object coParser2 = filter;
                        }
                    }
                    dtm.setIncrementalSAXSource(coParser);
                    if (xmlSource == null) {
                        if (reader != null) {
                            if (((this.m_incremental ? incremental : 0) ^ 1) != 0) {
                                reader.setContentHandler(this.m_defaultHandler);
                                reader.setDTDHandler(this.m_defaultHandler);
                                reader.setErrorHandler(this.m_defaultHandler);
                                try {
                                    reader.setProperty("http://xml.org/sax/properties/lexical-handler", null);
                                } catch (Exception e4) {
                                }
                            }
                        }
                        releaseXMLReader(reader);
                        return dtm;
                    }
                    if (reader.getErrorHandler() == null) {
                        reader.setErrorHandler(dtm);
                    }
                    reader.setDTDHandler(dtm);
                    coParser.startParse(xmlSource);
                } else if (reader == null) {
                    if (reader != null) {
                        if (((this.m_incremental ? incremental : 0) ^ 1) != 0) {
                            reader.setContentHandler(this.m_defaultHandler);
                            reader.setDTDHandler(this.m_defaultHandler);
                            reader.setErrorHandler(this.m_defaultHandler);
                            try {
                                reader.setProperty("http://xml.org/sax/properties/lexical-handler", null);
                            } catch (Exception e5) {
                            }
                        }
                    }
                    releaseXMLReader(reader);
                    return dtm;
                } else {
                    reader.setContentHandler(dtm);
                    reader.setDTDHandler(dtm);
                    if (reader.getErrorHandler() == null) {
                        reader.setErrorHandler(dtm);
                    }
                    try {
                        reader.setProperty("http://xml.org/sax/properties/lexical-handler", dtm);
                    } catch (SAXNotRecognizedException e6) {
                    } catch (SAXNotSupportedException e7) {
                    }
                    reader.parse(xmlSource);
                }
                if (reader != null) {
                    if (((this.m_incremental ? incremental : 0) ^ 1) != 0) {
                        reader.setContentHandler(this.m_defaultHandler);
                        reader.setDTDHandler(this.m_defaultHandler);
                        reader.setErrorHandler(this.m_defaultHandler);
                        try {
                            reader.setProperty("http://xml.org/sax/properties/lexical-handler", null);
                        } catch (Exception e8) {
                        }
                    }
                }
                releaseXMLReader(reader);
                return dtm;
            }
            throw new DTMException(XMLMessages.createXMLMessage("ER_NOT_SUPPORTED", new Object[]{source}));
        }
        DOM2DTM dtm2 = new DOM2DTM(this, (DOMSource) source, documentID, whiteSpaceFilter, xstringFactory, doIndexing);
        addDTM(dtm2, dtmPos, 0);
        return dtm2;
    }

    public synchronized int getDTMHandleFromNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_NODE_NON_NULL, null));
        } else if (node instanceof DTMNodeProxy) {
            return ((DTMNodeProxy) node).getDTMNodeNumber();
        } else {
            int handle;
            for (DTM thisDTM : this.m_dtms) {
                if (thisDTM != null && (thisDTM instanceof DOM2DTM)) {
                    handle = ((DOM2DTM) thisDTM).getHandleOfNode(node);
                    if (handle != -1) {
                        return handle;
                    }
                }
            }
            Node root = node;
            Node p = node.getNodeType() == (short) 2 ? ((Attr) node).getOwnerElement() : node.getParentNode();
            while (p != null) {
                root = p;
                p = p.getParentNode();
            }
            DOM2DTM dtm = (DOM2DTM) getDTM(new DOMSource(root), false, null, true, true);
            if (node instanceof DOM2DTMdefaultNamespaceDeclarationNode) {
                handle = dtm.getAttributeNode(dtm.getHandleOfNode(((Attr) node).getOwnerElement()), node.getNamespaceURI(), node.getLocalName());
            } else {
                handle = dtm.getHandleOfNode(node);
            }
            if (-1 != handle) {
                return handle;
            }
            throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_COULD_NOT_RESOLVE_NODE, null));
        }
    }

    public synchronized XMLReader getXMLReader(Source inputSource) {
        XMLReader reader;
        try {
            reader = inputSource instanceof SAXSource ? ((SAXSource) inputSource).getXMLReader() : null;
            if (reader == null) {
                if (this.m_readerManager == null) {
                    this.m_readerManager = XMLReaderManager.getInstance();
                }
                reader = this.m_readerManager.getXMLReader();
            }
        } catch (Throwable se) {
            throw new DTMException(se.getMessage(), se);
        }
        return reader;
    }

    public synchronized void releaseXMLReader(XMLReader reader) {
        if (this.m_readerManager != null) {
            this.m_readerManager.releaseXMLReader(reader);
        }
    }

    public synchronized DTM getDTM(int nodeHandle) {
        try {
        } catch (ArrayIndexOutOfBoundsException e) {
            if (nodeHandle == -1) {
                return null;
            }
            throw e;
        }
        return this.m_dtms[nodeHandle >>> 16];
    }

    public synchronized int getDTMIdentity(DTM dtm) {
        if (dtm instanceof DTMDefaultBase) {
            DTMDefaultBase dtmdb = (DTMDefaultBase) dtm;
            if (dtmdb.getManager() != this) {
                return -1;
            }
            return dtmdb.getDTMIDs().elementAt(0);
        }
        int n = this.m_dtms.length;
        int i = 0;
        while (i < n) {
            if (this.m_dtms[i] == dtm && this.m_dtm_offsets[i] == 0) {
                return i << 16;
            }
            i++;
        }
        return -1;
    }

    public synchronized boolean release(DTM dtm, boolean shouldHardDelete) {
        if (dtm instanceof SAX2DTM) {
            ((SAX2DTM) dtm).clearCoRoutine();
        }
        int i;
        if (dtm instanceof DTMDefaultBase) {
            SuballocatedIntVector ids = ((DTMDefaultBase) dtm).getDTMIDs();
            for (i = ids.size() - 1; i >= 0; i--) {
                this.m_dtms[ids.elementAt(i) >>> 16] = null;
            }
        } else {
            i = getDTMIdentity(dtm);
            if (i >= 0) {
                this.m_dtms[i >>> 16] = null;
            }
        }
        dtm.documentRelease();
        return true;
    }

    public synchronized DTM createDocumentFragment() {
        DocumentBuilderFactory dbf;
        try {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
        } catch (Throwable e) {
            throw new DTMException(e);
        }
        return getDTM(new DOMSource(dbf.newDocumentBuilder().newDocument().createDocumentFragment()), true, null, false, false);
    }

    public synchronized DTMIterator createDTMIterator(int whatToShow, DTMFilter filter, boolean entityReferenceExpansion) {
        return null;
    }

    public synchronized DTMIterator createDTMIterator(String xpathString, PrefixResolver presolver) {
        return null;
    }

    public synchronized DTMIterator createDTMIterator(int node) {
        return null;
    }

    public synchronized DTMIterator createDTMIterator(Object xpathCompiler, int pos) {
        return null;
    }

    public ExpandedNameTable getExpandedNameTable(DTM dtm) {
        return this.m_expandedNameTable;
    }
}
