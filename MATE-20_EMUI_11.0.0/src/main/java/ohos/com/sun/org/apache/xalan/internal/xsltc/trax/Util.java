package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.io.InputStream;
import java.io.Reader;
import ohos.com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.TransformerConfigurationException;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.javax.xml.transform.stax.StAXSource;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.jdk.xml.internal.JdkXmlFeatures;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;

public final class Util {
    private static final String property = "org.xml.sax.driver";

    public static String baseName(String str) {
        return ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util.baseName(str);
    }

    public static String noExtName(String str) {
        return ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util.noExtName(str);
    }

    public static String toJavaName(String str) {
        return ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util.toJavaName(str);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003c, code lost:
        ohos.com.sun.org.apache.xalan.internal.utils.XMLSecurityManager.printWarning(r11.getClass().getName(), "http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x008d, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0095, code lost:
        throw new ohos.javax.xml.transform.TransformerConfigurationException("SAXNotSupportedException ", r10);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x008d A[ExcHandler: SAXNotSupportedException (r10v16 'e' ohos.org.xml.sax.SAXNotSupportedException A[CUSTOM_DECLARE]), Splitter:B:5:0x0012] */
    public static InputSource getInputSource(XSLTC xsltc, Source source) throws TransformerConfigurationException {
        InputSource inputSource;
        InputSource inputSource2;
        SAXException e;
        String systemId = source.getSystemId();
        try {
            if (source instanceof SAXSource) {
                SAXSource sAXSource = (SAXSource) source;
                inputSource = sAXSource.getInputSource();
                try {
                    XMLReader xMLReader = sAXSource.getXMLReader();
                    if (xMLReader == null) {
                        xMLReader = JdkXmlUtils.getXMLReader(xsltc.getFeature(JdkXmlFeatures.XmlFeature.JDK_OVERRIDE_PARSER), xsltc.isSecureProcessing());
                    } else {
                        xMLReader.setFeature("http://xml.org/sax/features/namespaces", true);
                        xMLReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
                    }
                    xMLReader.setProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", xsltc.getProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD"));
                    String str = "";
                    try {
                        XMLSecurityManager xMLSecurityManager = (XMLSecurityManager) xsltc.getProperty("http://apache.org/xml/properties/security-manager");
                        if (xMLSecurityManager != null) {
                            XMLSecurityManager.Limit[] values = XMLSecurityManager.Limit.values();
                            for (XMLSecurityManager.Limit limit : values) {
                                xMLReader.setProperty(limit.apiProperty(), xMLSecurityManager.getLimitValueAsString(limit));
                            }
                            if (xMLSecurityManager.printEntityCountInfo()) {
                                try {
                                    xMLReader.setProperty("http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo", "yes");
                                } catch (SAXException e2) {
                                    str = "http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo";
                                    e = e2;
                                }
                            }
                        }
                    } catch (SAXException e3) {
                        e = e3;
                        XMLSecurityManager.printWarning(xMLReader.getClass().getName(), str, e);
                        xsltc.setXMLReader(xMLReader);
                        inputSource.setSystemId(systemId);
                        return inputSource;
                    }
                    xsltc.setXMLReader(xMLReader);
                } catch (SAXNotRecognizedException e4) {
                    throw new TransformerConfigurationException("SAXNotRecognizedException ", e4);
                } catch (SAXNotSupportedException e5) {
                }
            } else {
                if (source instanceof DOMSource) {
                    DOMSource dOMSource = (DOMSource) source;
                    xsltc.setXMLReader(new DOM2SAX(dOMSource.getNode()));
                    inputSource2 = SAXSource.sourceToInputSource(source);
                    if (inputSource2 == null) {
                        inputSource2 = new InputSource(dOMSource.getSystemId());
                    }
                } else if (source instanceof StAXSource) {
                    StAXSource stAXSource = (StAXSource) source;
                    if (stAXSource.getXMLEventReader() != null) {
                        xsltc.setXMLReader(new StAXEvent2SAX(stAXSource.getXMLEventReader()));
                    } else if (stAXSource.getXMLStreamReader() != null) {
                        xsltc.setXMLReader(new StAXStream2SAX(stAXSource.getXMLStreamReader()));
                    }
                    inputSource2 = SAXSource.sourceToInputSource(source);
                    if (inputSource2 == null) {
                        inputSource2 = new InputSource(stAXSource.getSystemId());
                    }
                } else if (source instanceof StreamSource) {
                    StreamSource streamSource = (StreamSource) source;
                    InputStream inputStream = streamSource.getInputStream();
                    Reader reader = streamSource.getReader();
                    xsltc.setXMLReader(null);
                    if (inputStream != null) {
                        inputSource2 = new InputSource(inputStream);
                    } else if (reader != null) {
                        inputSource2 = new InputSource(reader);
                    } else {
                        inputSource2 = new InputSource(systemId);
                    }
                } else {
                    throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_SOURCE_ERR).toString());
                }
                inputSource = inputSource2;
            }
            inputSource.setSystemId(systemId);
            return inputSource;
        } catch (NullPointerException unused) {
            throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.JAXP_NO_SOURCE_ERR, "TransformerFactory.newTemplates()").toString());
        } catch (SecurityException unused2) {
            throw new TransformerConfigurationException(new ErrorMsg(ErrorMsg.FILE_ACCESS_ERR, systemId).toString());
        }
    }
}
