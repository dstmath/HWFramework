package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;

public class DefaultErrorHandler implements ErrorHandler, ErrorListener {
    PrintWriter m_pw;
    boolean m_throwExceptionOnError;

    public DefaultErrorHandler(PrintWriter printWriter) {
        this.m_throwExceptionOnError = true;
        this.m_pw = printWriter;
    }

    public DefaultErrorHandler(PrintStream printStream) {
        this.m_throwExceptionOnError = true;
        this.m_pw = new PrintWriter((OutputStream) printStream, true);
    }

    public DefaultErrorHandler() {
        this(true);
    }

    public DefaultErrorHandler(boolean z) {
        this.m_throwExceptionOnError = true;
        this.m_pw = new PrintWriter((OutputStream) System.err, true);
        this.m_throwExceptionOnError = z;
    }

    public void warning(SAXParseException sAXParseException) throws SAXException {
        printLocation(this.m_pw, (Throwable) sAXParseException);
        PrintWriter printWriter = this.m_pw;
        printWriter.println("Parser warning: " + sAXParseException.getMessage());
    }

    public void error(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }

    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }

    public void warning(TransformerException transformerException) throws TransformerException {
        printLocation(this.m_pw, (Throwable) transformerException);
        this.m_pw.println(transformerException.getMessage());
    }

    public void error(TransformerException transformerException) throws TransformerException {
        if (!this.m_throwExceptionOnError) {
            printLocation(this.m_pw, (Throwable) transformerException);
            this.m_pw.println(transformerException.getMessage());
            return;
        }
        throw transformerException;
    }

    public void fatalError(TransformerException transformerException) throws TransformerException {
        if (!this.m_throwExceptionOnError) {
            printLocation(this.m_pw, (Throwable) transformerException);
            this.m_pw.println(transformerException.getMessage());
            return;
        }
        throw transformerException;
    }

    public static void ensureLocationSet(TransformerException transformerException) {
        SAXSourceLocator locator;
        TransformerException transformerException2 = transformerException;
        SAXSourceLocator sAXSourceLocator = null;
        do {
            if (transformerException2 instanceof SAXParseException) {
                sAXSourceLocator = new SAXSourceLocator((SAXParseException) transformerException2);
            } else if ((transformerException2 instanceof TransformerException) && (locator = transformerException2.getLocator()) != null) {
                sAXSourceLocator = locator;
            }
            if (transformerException2 instanceof TransformerException) {
                transformerException2 = transformerException2.getCause();
                continue;
            } else if (transformerException2 instanceof SAXException) {
                transformerException2 = ((SAXException) transformerException2).getException();
                continue;
            } else {
                transformerException2 = null;
                continue;
            }
        } while (transformerException2 != null);
        transformerException.setLocator(sAXSourceLocator);
    }

    public static void printLocation(PrintStream printStream, TransformerException transformerException) {
        printLocation(new PrintWriter(printStream), (Throwable) transformerException);
    }

    public static void printLocation(PrintStream printStream, SAXParseException sAXParseException) {
        printLocation(new PrintWriter(printStream), (Throwable) sAXParseException);
    }

    public static void printLocation(PrintWriter printWriter, Throwable th) {
        String str;
        SAXSourceLocator locator;
        SAXSourceLocator sAXSourceLocator = null;
        do {
            if (th instanceof SAXParseException) {
                sAXSourceLocator = new SAXSourceLocator((SAXParseException) th);
            } else if ((th instanceof TransformerException) && (locator = ((TransformerException) th).getLocator()) != null) {
                sAXSourceLocator = locator;
            }
            if (th instanceof TransformerException) {
                th = ((TransformerException) th).getCause();
                continue;
            } else if (th instanceof WrappedRuntimeException) {
                th = ((WrappedRuntimeException) th).getException();
                continue;
            } else if (th instanceof SAXException) {
                th = ((SAXException) th).getException();
                continue;
            } else {
                th = null;
                continue;
            }
        } while (th != null);
        if (sAXSourceLocator != null) {
            if (sAXSourceLocator.getPublicId() != null) {
                str = sAXSourceLocator.getPublicId();
            } else {
                str = sAXSourceLocator.getSystemId() != null ? sAXSourceLocator.getSystemId() : XMLMessages.createXMLMessage("ER_SYSTEMID_UNKNOWN", null);
            }
            printWriter.print(str + "; " + XMLMessages.createXMLMessage("line", null) + sAXSourceLocator.getLineNumber() + "; " + XMLMessages.createXMLMessage("column", null) + sAXSourceLocator.getColumnNumber() + "; ");
            return;
        }
        printWriter.print("(" + XMLMessages.createXMLMessage("ER_LOCATION_UNKNOWN", null) + ")");
    }
}
