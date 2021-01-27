package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.trax.DOM2SAX;
import ohos.com.sun.org.apache.xalan.internal.xsltc.trax.StAXEvent2SAX;
import ohos.com.sun.org.apache.xalan.internal.xsltc.trax.StAXStream2SAX;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMException;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.stax.StAXSource;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;

public class XSLTCDTMManager extends DTMManagerDefault {
    private static final boolean DEBUG = false;
    private static final boolean DUMPTREE = false;

    public static XSLTCDTMManager newInstance() {
        return new XSLTCDTMManager();
    }

    public static XSLTCDTMManager createNewDTMManagerInstance() {
        return newInstance();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault, ohos.com.sun.org.apache.xml.internal.dtm.DTMManager
    public DTM getDTM(Source source, boolean z, DTMWSFilter dTMWSFilter, boolean z2, boolean z3) {
        return getDTM(source, z, dTMWSFilter, z2, z3, false, 0, true, false);
    }

    public DTM getDTM(Source source, boolean z, DTMWSFilter dTMWSFilter, boolean z2, boolean z3, boolean z4) {
        return getDTM(source, z, dTMWSFilter, z2, z3, false, 0, z4, false);
    }

    public DTM getDTM(Source source, boolean z, DTMWSFilter dTMWSFilter, boolean z2, boolean z3, boolean z4, boolean z5) {
        return getDTM(source, z, dTMWSFilter, z2, z3, false, 0, z4, z5);
    }

    public DTM getDTM(Source source, boolean z, DTMWSFilter dTMWSFilter, boolean z2, boolean z3, boolean z4, int i, boolean z5) {
        return getDTM(source, z, dTMWSFilter, z2, z3, z4, i, z5, false);
    }

    public DTM getDTM(Source source, boolean z, DTMWSFilter dTMWSFilter, boolean z2, boolean z3, boolean z4, int i, boolean z5, boolean z6) {
        boolean z7;
        boolean z8;
        XMLReader xMLReader;
        InputSource inputSource;
        SAXImpl sAXImpl;
        InputSource inputSource2;
        SAXImpl sAXImpl2;
        StAXStream2SAX stAXStream2SAX;
        StAXEvent2SAX stAXEvent2SAX;
        SAXImpl sAXImpl3;
        int firstFreeDTMID = getFirstFreeDTMID();
        int i2 = firstFreeDTMID << 16;
        if (source != null && (source instanceof StAXSource)) {
            StAXSource stAXSource = (StAXSource) source;
            if (stAXSource.getXMLEventReader() != null) {
                stAXStream2SAX = null;
                stAXEvent2SAX = new StAXEvent2SAX(stAXSource.getXMLEventReader());
            } else if (stAXSource.getXMLStreamReader() != null) {
                stAXEvent2SAX = null;
                stAXStream2SAX = new StAXStream2SAX(stAXSource.getXMLStreamReader());
            } else {
                stAXEvent2SAX = null;
                stAXStream2SAX = null;
            }
            if (i <= 0) {
                sAXImpl3 = new SAXImpl(this, source, i2, dTMWSFilter, null, z3, 512, z5, z6);
            } else {
                sAXImpl3 = new SAXImpl(this, source, i2, dTMWSFilter, null, z3, i, z5, z6);
            }
            sAXImpl3.setDocumentURI(source.getSystemId());
            addDTM(sAXImpl3, firstFreeDTMID, 0);
            if (stAXEvent2SAX != null) {
                try {
                    stAXEvent2SAX.setContentHandler(sAXImpl3);
                    stAXEvent2SAX.parse();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e2) {
                    throw new WrappedRuntimeException(e2);
                }
            } else if (stAXStream2SAX != null) {
                stAXStream2SAX.setContentHandler(sAXImpl3);
                stAXStream2SAX.parse();
            }
            return sAXImpl3;
        } else if (source == null || !(source instanceof DOMSource)) {
            boolean z9 = source != null ? source instanceof SAXSource : true;
            if (source != null) {
                z7 = source instanceof StreamSource;
            } else {
                z7 = false;
            }
            if (z9 || z7) {
                if (source == null) {
                    inputSource = null;
                    xMLReader = null;
                    z8 = false;
                } else {
                    XMLReader xMLReader2 = getXMLReader(source);
                    InputSource sourceToInputSource = SAXSource.sourceToInputSource(source);
                    String systemId = sourceToInputSource.getSystemId();
                    if (systemId != null) {
                        try {
                            systemId = SystemIDResolver.getAbsoluteURI(systemId);
                        } catch (Exception unused) {
                            System.err.println("Can not absolutize URL: " + systemId);
                        }
                        sourceToInputSource.setSystemId(systemId);
                    }
                    z8 = z4;
                    xMLReader = xMLReader2;
                    inputSource = sourceToInputSource;
                }
                if (i <= 0) {
                    inputSource2 = inputSource;
                    sAXImpl = new SAXImpl(this, source, i2, dTMWSFilter, null, z3, 512, z5, z6);
                } else {
                    inputSource2 = inputSource;
                    sAXImpl = new SAXImpl(this, source, i2, dTMWSFilter, null, z3, i, z5, z6);
                }
                addDTM(sAXImpl, firstFreeDTMID, 0);
                if (xMLReader == null) {
                    return sAXImpl;
                }
                xMLReader.setContentHandler(sAXImpl.getBuilder());
                if (!z8 || xMLReader.getDTDHandler() == null) {
                    xMLReader.setDTDHandler(sAXImpl);
                }
                if (!z8 || xMLReader.getErrorHandler() == null) {
                    xMLReader.setErrorHandler(sAXImpl);
                }
                try {
                    xMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", sAXImpl);
                } catch (SAXNotRecognizedException | SAXNotSupportedException unused2) {
                }
                try {
                    xMLReader.parse(inputSource2);
                    if (!z8) {
                        releaseXMLReader(xMLReader);
                    }
                    return sAXImpl;
                } catch (RuntimeException e3) {
                    throw e3;
                } catch (Exception e4) {
                    throw new WrappedRuntimeException(e4);
                } catch (Throwable th) {
                    if (!z8) {
                        releaseXMLReader(xMLReader);
                    }
                    throw th;
                }
            } else {
                throw new DTMException(XMLMessages.createXMLMessage("ER_NOT_SUPPORTED", new Object[]{source}));
            }
        } else {
            DOM2SAX dom2sax = new DOM2SAX(((DOMSource) source).getNode());
            if (i <= 0) {
                sAXImpl2 = new SAXImpl(this, source, i2, dTMWSFilter, null, z3, 512, z5, z6);
            } else {
                sAXImpl2 = new SAXImpl(this, source, i2, dTMWSFilter, null, z3, i, z5, z6);
            }
            sAXImpl2.setDocumentURI(source.getSystemId());
            addDTM(sAXImpl2, firstFreeDTMID, 0);
            dom2sax.setContentHandler(sAXImpl2);
            try {
                dom2sax.parse();
                return sAXImpl2;
            } catch (RuntimeException e5) {
                throw e5;
            } catch (Exception e6) {
                throw new WrappedRuntimeException(e6);
            }
        }
    }
}
