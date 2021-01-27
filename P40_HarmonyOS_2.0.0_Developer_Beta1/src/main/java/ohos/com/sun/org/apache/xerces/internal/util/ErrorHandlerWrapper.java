package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;

public class ErrorHandlerWrapper implements XMLErrorHandler {
    protected ErrorHandler fErrorHandler;

    public ErrorHandlerWrapper() {
    }

    public ErrorHandlerWrapper(ErrorHandler errorHandler) {
        setErrorHandler(errorHandler);
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.fErrorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return this.fErrorHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void warning(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        if (this.fErrorHandler != null) {
            try {
                this.fErrorHandler.warning(createSAXParseException(xMLParseException));
            } catch (SAXParseException e) {
                throw createXMLParseException(e);
            } catch (SAXException e2) {
                throw createXNIException(e2);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void error(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        if (this.fErrorHandler != null) {
            try {
                this.fErrorHandler.error(createSAXParseException(xMLParseException));
            } catch (SAXParseException e) {
                throw createXMLParseException(e);
            } catch (SAXException e2) {
                throw createXNIException(e2);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void fatalError(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        if (this.fErrorHandler != null) {
            try {
                this.fErrorHandler.fatalError(createSAXParseException(xMLParseException));
            } catch (SAXParseException e) {
                throw createXMLParseException(e);
            } catch (SAXException e2) {
                throw createXNIException(e2);
            }
        }
    }

    protected static SAXParseException createSAXParseException(XMLParseException xMLParseException) {
        return new SAXParseException(xMLParseException.getMessage(), xMLParseException.getPublicId(), xMLParseException.getExpandedSystemId(), xMLParseException.getLineNumber(), xMLParseException.getColumnNumber(), xMLParseException.getException());
    }

    protected static XMLParseException createXMLParseException(SAXParseException sAXParseException) {
        final String publicId = sAXParseException.getPublicId();
        final String systemId = sAXParseException.getSystemId();
        final int lineNumber = sAXParseException.getLineNumber();
        final int columnNumber = sAXParseException.getColumnNumber();
        return new XMLParseException(new XMLLocator() {
            /* class ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper.AnonymousClass1 */

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public String getBaseSystemId() {
                return null;
            }

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public int getCharacterOffset() {
                return -1;
            }

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public String getEncoding() {
                return null;
            }

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public String getLiteralSystemId() {
                return null;
            }

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public String getXMLVersion() {
                return null;
            }

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public String getPublicId() {
                return publicId;
            }

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public String getExpandedSystemId() {
                return systemId;
            }

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public int getColumnNumber() {
                return columnNumber;
            }

            @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
            public int getLineNumber() {
                return lineNumber;
            }
        }, sAXParseException.getMessage(), sAXParseException);
    }

    protected static XNIException createXNIException(SAXException sAXException) {
        return new XNIException(sAXException.getMessage(), sAXException);
    }
}
