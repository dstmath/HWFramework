package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.javax.xml.transform.stream.StreamSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;

/* access modifiers changed from: package-private */
public final class Util {
    Util() {
    }

    public static final XMLInputSource toXMLInputSource(StreamSource streamSource) {
        if (streamSource.getReader() != null) {
            return new XMLInputSource(streamSource.getPublicId(), streamSource.getSystemId(), streamSource.getSystemId(), streamSource.getReader(), (String) null);
        }
        if (streamSource.getInputStream() != null) {
            return new XMLInputSource(streamSource.getPublicId(), streamSource.getSystemId(), streamSource.getSystemId(), streamSource.getInputStream(), (String) null);
        }
        return new XMLInputSource(streamSource.getPublicId(), streamSource.getSystemId(), streamSource.getSystemId());
    }

    public static SAXException toSAXException(XNIException xNIException) {
        if (xNIException instanceof XMLParseException) {
            return toSAXParseException((XMLParseException) xNIException);
        }
        if (xNIException.getException() instanceof SAXException) {
            return xNIException.getException();
        }
        return new SAXException(xNIException.getMessage(), xNIException.getException());
    }

    public static SAXParseException toSAXParseException(XMLParseException xMLParseException) {
        if (xMLParseException.getException() instanceof SAXParseException) {
            return xMLParseException.getException();
        }
        return new SAXParseException(xMLParseException.getMessage(), xMLParseException.getPublicId(), xMLParseException.getExpandedSystemId(), xMLParseException.getLineNumber(), xMLParseException.getColumnNumber(), xMLParseException.getException());
    }
}
