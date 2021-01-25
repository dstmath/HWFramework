package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMException;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMFilter;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.dom2dtm.DOM2DTMdefaultNamespaceDeclarationNode;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2RTFDTM;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xml.internal.utils.SuballocatedIntVector;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xml.internal.utils.XMLReaderManager;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.helpers.DefaultHandler;

public class DTMManagerDefault extends DTMManager {
    private static final boolean DEBUG = false;
    private static final boolean DUMPTREE = false;
    protected DefaultHandler m_defaultHandler = new DefaultHandler();
    int[] m_dtm_offsets = new int[256];
    protected DTM[] m_dtms = new DTM[256];
    private ExpandedNameTable m_expandedNameTable = new ExpandedNameTable();
    protected XMLReaderManager m_readerManager = null;

    public synchronized void addDTM(DTM dtm, int i) {
        addDTM(dtm, i, 0);
    }

    public synchronized void addDTM(DTM dtm, int i, int i2) {
        if (i < 65536) {
            int length = this.m_dtms.length;
            if (length <= i) {
                int min = Math.min(i + 256, 65536);
                DTM[] dtmArr = new DTM[min];
                System.arraycopy(this.m_dtms, 0, dtmArr, 0, length);
                this.m_dtms = dtmArr;
                int[] iArr = new int[min];
                System.arraycopy(this.m_dtm_offsets, 0, iArr, 0, length);
                this.m_dtm_offsets = iArr;
            }
            this.m_dtms[i] = dtm;
            this.m_dtm_offsets[i] = i2;
            dtm.documentRegistration();
        } else {
            throw new DTMException(XMLMessages.createXMLMessage("ER_NO_DTMIDS_AVAIL", null));
        }
    }

    public synchronized int getFirstFreeDTMID() {
        int length = this.m_dtms.length;
        for (int i = 1; i < length; i++) {
            if (this.m_dtms[i] == null) {
                return i;
            }
        }
        return length;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:60:0x00da */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r16v0, types: [ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault, ohos.com.sun.org.apache.xml.internal.dtm.DTMManager] */
    /* JADX WARN: Type inference failed for: r0v37, types: [ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00f1 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0112  */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized DTM getDTM(Source source, boolean z, DTMWSFilter dTMWSFilter, boolean z2, boolean z3) {
        boolean z4;
        XMLReader xMLReader;
        Throwable th;
        InputSource inputSource;
        SAX2RTFDTM sax2rtfdtm;
        IncrementalSAXSource_Filter incrementalSAXSource_Filter;
        String str;
        XMLStringFactory xMLStringFactory = ((DTMManagerDefault) this).m_xsf;
        int firstFreeDTMID = getFirstFreeDTMID();
        int i = firstFreeDTMID << 16;
        boolean z5 = false;
        if (source == null || !(source instanceof DOMSource)) {
            boolean z6 = true;
            boolean z7 = source != null ? source instanceof SAXSource : true;
            if (source != null) {
                z4 = source instanceof StreamSource;
            } else {
                z4 = false;
            }
            if (z7 || z4) {
                if (source == null) {
                    xMLReader = null;
                    inputSource = null;
                } else {
                    try {
                        XMLReader xMLReader2 = getXMLReader(source);
                        try {
                            InputSource sourceToInputSource = SAXSource.sourceToInputSource(source);
                            String systemId = sourceToInputSource.getSystemId();
                            if (systemId != null) {
                                try {
                                    str = SystemIDResolver.getAbsoluteURI(systemId);
                                } catch (Exception unused) {
                                    System.err.println("Can not absolutize URL: " + str);
                                }
                                sourceToInputSource.setSystemId(str);
                            }
                            xMLReader = xMLReader2;
                            inputSource = sourceToInputSource;
                        } catch (Throwable th2) {
                            th = th2;
                            z6 = z2;
                            xMLReader = xMLReader2;
                            xMLReader.setContentHandler(this.m_defaultHandler);
                            xMLReader.setDTDHandler(this.m_defaultHandler);
                            xMLReader.setErrorHandler(this.m_defaultHandler);
                            try {
                                xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", (Object) null);
                            } catch (Exception unused2) {
                            }
                            releaseXMLReader(xMLReader);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        z6 = z2;
                        xMLReader = null;
                        xMLReader.setContentHandler(this.m_defaultHandler);
                        xMLReader.setDTDHandler(this.m_defaultHandler);
                        xMLReader.setErrorHandler(this.m_defaultHandler);
                        xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", (Object) null);
                        releaseXMLReader(xMLReader);
                        throw th;
                    }
                }
                if (source != null || !z || z2 || z3) {
                    sax2rtfdtm = new SAX2DTM(this, source, i, dTMWSFilter, xMLStringFactory, z3);
                } else {
                    try {
                        sax2rtfdtm = new SAX2RTFDTM(this, source, i, dTMWSFilter, xMLStringFactory, z3);
                    } catch (Throwable th4) {
                        th = th4;
                        z6 = z2;
                        if (xMLReader != null && (!((DTMManagerDefault) this).m_incremental || !z6)) {
                            xMLReader.setContentHandler(this.m_defaultHandler);
                            xMLReader.setDTDHandler(this.m_defaultHandler);
                            xMLReader.setErrorHandler(this.m_defaultHandler);
                            xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", (Object) null);
                        }
                        releaseXMLReader(xMLReader);
                        throw th;
                    }
                }
                addDTM(sax2rtfdtm, firstFreeDTMID, 0);
                if (xMLReader != null && xMLReader.getClass().getName().equals("ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser")) {
                    z5 = true;
                }
                if (!z5) {
                    z6 = z2;
                }
                try {
                    if (((DTMManagerDefault) this).m_incremental && z6) {
                        if (z5) {
                            try {
                                incrementalSAXSource_Filter = (IncrementalSAXSource) Class.forName("ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Xerces").newInstance();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (incrementalSAXSource_Filter == null) {
                                if (xMLReader == null) {
                                    incrementalSAXSource_Filter = new IncrementalSAXSource_Filter();
                                } else {
                                    incrementalSAXSource_Filter = new IncrementalSAXSource_Filter();
                                    incrementalSAXSource_Filter.setXMLReader(xMLReader);
                                }
                            }
                            sax2rtfdtm.setIncrementalSAXSource(incrementalSAXSource_Filter);
                            if (inputSource != null) {
                                if (xMLReader != null && (!((DTMManagerDefault) this).m_incremental || !z6)) {
                                    xMLReader.setContentHandler(this.m_defaultHandler);
                                    xMLReader.setDTDHandler(this.m_defaultHandler);
                                    xMLReader.setErrorHandler(this.m_defaultHandler);
                                    try {
                                        xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", (Object) null);
                                    } catch (Exception unused3) {
                                    }
                                }
                                releaseXMLReader(xMLReader);
                                return sax2rtfdtm;
                            }
                            if (xMLReader.getErrorHandler() == null) {
                                xMLReader.setErrorHandler(sax2rtfdtm);
                            }
                            xMLReader.setDTDHandler(sax2rtfdtm);
                            try {
                                incrementalSAXSource_Filter.startParse(inputSource);
                            } catch (RuntimeException e2) {
                                sax2rtfdtm.clearCoRoutine();
                                throw e2;
                            } catch (Exception e3) {
                                sax2rtfdtm.clearCoRoutine();
                                throw new WrappedRuntimeException(e3);
                            }
                        }
                        incrementalSAXSource_Filter = null;
                        if (incrementalSAXSource_Filter == null) {
                        }
                        sax2rtfdtm.setIncrementalSAXSource(incrementalSAXSource_Filter);
                        if (inputSource != null) {
                        }
                    } else if (xMLReader == null) {
                        if (xMLReader != null && (!((DTMManagerDefault) this).m_incremental || !z6)) {
                            xMLReader.setContentHandler(this.m_defaultHandler);
                            xMLReader.setDTDHandler(this.m_defaultHandler);
                            xMLReader.setErrorHandler(this.m_defaultHandler);
                            try {
                                xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", (Object) null);
                            } catch (Exception unused4) {
                            }
                        }
                        releaseXMLReader(xMLReader);
                        return sax2rtfdtm;
                    } else {
                        xMLReader.setContentHandler(sax2rtfdtm);
                        xMLReader.setDTDHandler(sax2rtfdtm);
                        if (xMLReader.getErrorHandler() == null) {
                            xMLReader.setErrorHandler(sax2rtfdtm);
                        }
                        try {
                            xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", sax2rtfdtm);
                        } catch (SAXNotRecognizedException | SAXNotSupportedException unused5) {
                        }
                        try {
                            xMLReader.parse(inputSource);
                        } catch (RuntimeException e4) {
                            sax2rtfdtm.clearCoRoutine();
                            throw e4;
                        } catch (Exception e5) {
                            sax2rtfdtm.clearCoRoutine();
                            throw new WrappedRuntimeException(e5);
                        }
                    }
                    if (xMLReader != null && (!((DTMManagerDefault) this).m_incremental || !z6)) {
                        xMLReader.setContentHandler(this.m_defaultHandler);
                        xMLReader.setDTDHandler(this.m_defaultHandler);
                        xMLReader.setErrorHandler(this.m_defaultHandler);
                        try {
                            xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", (Object) null);
                        } catch (Exception unused6) {
                        }
                    }
                    releaseXMLReader(xMLReader);
                    return sax2rtfdtm;
                } catch (Throwable th5) {
                    th = th5;
                    xMLReader.setContentHandler(this.m_defaultHandler);
                    xMLReader.setDTDHandler(this.m_defaultHandler);
                    xMLReader.setErrorHandler(this.m_defaultHandler);
                    xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", (Object) null);
                    releaseXMLReader(xMLReader);
                    throw th;
                }
            } else {
                throw new DTMException(XMLMessages.createXMLMessage("ER_NOT_SUPPORTED", new Object[]{source}));
            }
        } else {
            DOM2DTM dom2dtm = new DOM2DTM(this, (DOMSource) source, i, dTMWSFilter, xMLStringFactory, z3);
            addDTM(dom2dtm, firstFreeDTMID, 0);
            return dom2dtm;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized int getDTMHandleFromNode(Node node) {
        int i;
        int handleOfNode;
        if (node == null) {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage("ER_NODE_NON_NULL", null));
        } else if (node instanceof DTMNodeProxy) {
            return ((DTMNodeProxy) node).getDTMNodeNumber();
        } else {
            int length = this.m_dtms.length;
            for (int i2 = 0; i2 < length; i2++) {
                DTM dtm = this.m_dtms[i2];
                if (!(dtm == null || !(dtm instanceof DOM2DTM) || (handleOfNode = ((DOM2DTM) dtm).getHandleOfNode(node)) == -1)) {
                    return handleOfNode;
                }
            }
            Node node2 = node;
            for (Node ownerElement = node.getNodeType() == 2 ? ((Attr) node).getOwnerElement() : node.getParentNode(); ownerElement != null; ownerElement = ownerElement.getParentNode()) {
                node2 = ownerElement;
            }
            DOM2DTM dom2dtm = (DOM2DTM) getDTM(new DOMSource(node2), false, null, true, true);
            if (node instanceof DOM2DTMdefaultNamespaceDeclarationNode) {
                i = dom2dtm.getAttributeNode(dom2dtm.getHandleOfNode(((Attr) node).getOwnerElement()), node.getNamespaceURI(), node.getLocalName());
            } else {
                i = dom2dtm.getHandleOfNode(node);
            }
            if (-1 != i) {
                return i;
            }
            throw new RuntimeException(XMLMessages.createXMLMessage("ER_COULD_NOT_RESOLVE_NODE", null));
        }
    }

    public synchronized XMLReader getXMLReader(Source source) {
        XMLReader xMLReader;
        try {
            xMLReader = source instanceof SAXSource ? ((SAXSource) source).getXMLReader() : null;
            if (xMLReader == null) {
                if (this.m_readerManager == null) {
                    this.m_readerManager = XMLReaderManager.getInstance(super.overrideDefaultParser());
                }
                xMLReader = this.m_readerManager.getXMLReader();
            }
        } catch (SAXException e) {
            throw new DTMException(e.getMessage(), e);
        }
        return xMLReader;
    }

    public synchronized void releaseXMLReader(XMLReader xMLReader) {
        if (this.m_readerManager != null) {
            this.m_readerManager.releaseXMLReader(xMLReader);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized DTM getDTM(int i) {
        try {
        } catch (ArrayIndexOutOfBoundsException e) {
            if (i == -1) {
                return null;
            }
            throw e;
        }
        return this.m_dtms[i >>> 16];
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized int getDTMIdentity(DTM dtm) {
        if (dtm instanceof DTMDefaultBase) {
            DTMDefaultBase dTMDefaultBase = (DTMDefaultBase) dtm;
            if (dTMDefaultBase.getManager() != this) {
                return -1;
            }
            return dTMDefaultBase.getDTMIDs().elementAt(0);
        }
        int length = this.m_dtms.length;
        for (int i = 0; i < length; i++) {
            if (this.m_dtms[i] == dtm && this.m_dtm_offsets[i] == 0) {
                return i << 16;
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized boolean release(DTM dtm, boolean z) {
        if (dtm instanceof SAX2DTM) {
            ((SAX2DTM) dtm).clearCoRoutine();
        }
        if (dtm instanceof DTMDefaultBase) {
            SuballocatedIntVector dTMIDs = ((DTMDefaultBase) dtm).getDTMIDs();
            for (int size = dTMIDs.size() - 1; size >= 0; size--) {
                this.m_dtms[dTMIDs.elementAt(size) >>> 16] = null;
            }
        } else {
            int dTMIdentity = getDTMIdentity(dtm);
            if (dTMIdentity >= 0) {
                this.m_dtms[dTMIdentity >>> 16] = null;
            }
        }
        dtm.documentRelease();
        return true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized DTM createDocumentFragment() {
        try {
        } catch (Exception e) {
            throw new DTMException(e);
        }
        return getDTM(new DOMSource(JdkXmlUtils.getDOMFactory(super.overrideDefaultParser()).newDocumentBuilder().newDocument().createDocumentFragment()), true, null, false, false);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized DTMIterator createDTMIterator(int i, DTMFilter dTMFilter, boolean z) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized DTMIterator createDTMIterator(String str, PrefixResolver prefixResolver) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized DTMIterator createDTMIterator(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public synchronized DTMIterator createDTMIterator(Object obj, int i) {
        return null;
    }

    public ExpandedNameTable getExpandedNameTable(DTM dtm) {
        return this.m_expandedNameTable;
    }
}
