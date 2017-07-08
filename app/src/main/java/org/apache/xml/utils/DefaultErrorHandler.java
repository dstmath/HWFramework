package org.apache.xml.utils;

import java.io.PrintStream;
import java.io.PrintWriter;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DefaultErrorHandler implements ErrorHandler, ErrorListener {
    PrintWriter m_pw;
    boolean m_throwExceptionOnError;

    public DefaultErrorHandler(PrintWriter pw) {
        this.m_throwExceptionOnError = true;
        this.m_pw = pw;
    }

    public DefaultErrorHandler(PrintStream pw) {
        this.m_throwExceptionOnError = true;
        this.m_pw = new PrintWriter(pw, true);
    }

    public DefaultErrorHandler() {
        this(true);
    }

    public DefaultErrorHandler(boolean throwExceptionOnError) {
        this.m_throwExceptionOnError = true;
        this.m_throwExceptionOnError = throwExceptionOnError;
    }

    public PrintWriter getErrorWriter() {
        if (this.m_pw == null) {
            this.m_pw = new PrintWriter(System.err, true);
        }
        return this.m_pw;
    }

    public void warning(SAXParseException exception) throws SAXException {
        PrintWriter pw = getErrorWriter();
        printLocation(pw, (Throwable) exception);
        pw.println("Parser warning: " + exception.getMessage());
    }

    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public void warning(TransformerException exception) throws TransformerException {
        PrintWriter pw = getErrorWriter();
        printLocation(pw, (Throwable) exception);
        pw.println(exception.getMessage());
    }

    public void error(TransformerException exception) throws TransformerException {
        if (this.m_throwExceptionOnError) {
            throw exception;
        }
        PrintWriter pw = getErrorWriter();
        printLocation(pw, (Throwable) exception);
        pw.println(exception.getMessage());
    }

    public void fatalError(TransformerException exception) throws TransformerException {
        if (this.m_throwExceptionOnError) {
            throw exception;
        }
        PrintWriter pw = getErrorWriter();
        printLocation(pw, (Throwable) exception);
        pw.println(exception.getMessage());
    }

    public static void ensureLocationSet(TransformerException exception) {
        SourceLocator locator = null;
        Throwable th = exception;
        do {
            if (th instanceof SAXParseException) {
                locator = new SAXSourceLocator((SAXParseException) th);
            } else if (th instanceof TransformerException) {
                SourceLocator causeLocator = ((TransformerException) th).getLocator();
                if (causeLocator != null) {
                    locator = causeLocator;
                }
            }
            if (th instanceof TransformerException) {
                th = ((TransformerException) th).getCause();
                continue;
            } else if (th instanceof SAXException) {
                th = ((SAXException) th).getException();
                continue;
            } else {
                th = null;
                continue;
            }
        } while (th != null);
        exception.setLocator(locator);
    }

    public static void printLocation(PrintStream pw, TransformerException exception) {
        printLocation(new PrintWriter(pw), (Throwable) exception);
    }

    public static void printLocation(PrintStream pw, SAXParseException exception) {
        printLocation(new PrintWriter(pw), (Throwable) exception);
    }

    public static void printLocation(PrintWriter pw, Throwable exception) {
        SourceLocator locator = null;
        Throwable th = exception;
        do {
            if (th instanceof SAXParseException) {
                locator = new SAXSourceLocator((SAXParseException) th);
            } else if (th instanceof TransformerException) {
                SourceLocator causeLocator = ((TransformerException) th).getLocator();
                if (causeLocator != null) {
                    locator = causeLocator;
                }
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
        if (locator != null) {
            String id = locator.getPublicId() != null ? locator.getPublicId() : locator.getSystemId() != null ? locator.getSystemId() : XMLMessages.createXMLMessage(XMLErrorResources.ER_SYSTEMID_UNKNOWN, null);
            pw.print(id + "; " + XMLMessages.createXMLMessage("line", null) + locator.getLineNumber() + "; " + XMLMessages.createXMLMessage("column", null) + locator.getColumnNumber() + "; ");
            return;
        }
        pw.print("(" + XMLMessages.createXMLMessage(XMLErrorResources.ER_LOCATION_UNKNOWN, null) + ")");
    }
}
