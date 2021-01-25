package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.global.icu.text.PluralRules;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.SourceLocator;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;

public class ListingErrorHandler implements ErrorHandler, ErrorListener {
    protected PrintWriter m_pw;
    protected boolean throwOnError;
    protected boolean throwOnFatalError;
    protected boolean throwOnWarning;

    public ListingErrorHandler(PrintWriter printWriter) {
        this.m_pw = null;
        this.throwOnWarning = false;
        this.throwOnError = true;
        this.throwOnFatalError = true;
        if (printWriter != null) {
            this.m_pw = printWriter;
            return;
        }
        throw new NullPointerException(XMLMessages.createXMLMessage("ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER", null));
    }

    public ListingErrorHandler() {
        this.m_pw = null;
        this.throwOnWarning = false;
        this.throwOnError = true;
        this.throwOnFatalError = true;
        this.m_pw = new PrintWriter((OutputStream) System.err, true);
    }

    public void warning(SAXParseException sAXParseException) throws SAXException {
        logExceptionLocation(this.m_pw, sAXParseException);
        PrintWriter printWriter = this.m_pw;
        printWriter.println("warning: " + sAXParseException.getMessage());
        this.m_pw.flush();
        if (getThrowOnWarning()) {
            throw sAXParseException;
        }
    }

    public void error(SAXParseException sAXParseException) throws SAXException {
        logExceptionLocation(this.m_pw, sAXParseException);
        PrintWriter printWriter = this.m_pw;
        printWriter.println("error: " + sAXParseException.getMessage());
        this.m_pw.flush();
        if (getThrowOnError()) {
            throw sAXParseException;
        }
    }

    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        logExceptionLocation(this.m_pw, sAXParseException);
        PrintWriter printWriter = this.m_pw;
        printWriter.println("fatalError: " + sAXParseException.getMessage());
        this.m_pw.flush();
        if (getThrowOnFatalError()) {
            throw sAXParseException;
        }
    }

    public void warning(TransformerException transformerException) throws TransformerException {
        logExceptionLocation(this.m_pw, transformerException);
        PrintWriter printWriter = this.m_pw;
        printWriter.println("warning: " + transformerException.getMessage());
        this.m_pw.flush();
        if (getThrowOnWarning()) {
            throw transformerException;
        }
    }

    public void error(TransformerException transformerException) throws TransformerException {
        logExceptionLocation(this.m_pw, transformerException);
        PrintWriter printWriter = this.m_pw;
        printWriter.println("error: " + transformerException.getMessage());
        this.m_pw.flush();
        if (getThrowOnError()) {
            throw transformerException;
        }
    }

    public void fatalError(TransformerException transformerException) throws TransformerException {
        logExceptionLocation(this.m_pw, transformerException);
        PrintWriter printWriter = this.m_pw;
        printWriter.println("error: " + transformerException.getMessage());
        this.m_pw.flush();
        if (getThrowOnError()) {
            throw transformerException;
        }
    }

    public static void logExceptionLocation(PrintWriter printWriter, Throwable th) {
        String str;
        SAXSourceLocator locator;
        if (printWriter == null) {
            printWriter = new PrintWriter((OutputStream) System.err, true);
        }
        Throwable th2 = th;
        SAXSourceLocator sAXSourceLocator = null;
        do {
            if (th2 instanceof SAXParseException) {
                sAXSourceLocator = new SAXSourceLocator((SAXParseException) th2);
            } else if ((th2 instanceof TransformerException) && (locator = ((TransformerException) th2).getLocator()) != null) {
                sAXSourceLocator = locator;
            }
            if (th2 instanceof TransformerException) {
                th2 = ((TransformerException) th2).getCause();
                continue;
            } else if (th2 instanceof WrappedRuntimeException) {
                th2 = ((WrappedRuntimeException) th2).getException();
                continue;
            } else if (th2 instanceof SAXException) {
                th2 = ((SAXException) th2).getException();
                continue;
            } else {
                th2 = null;
                continue;
            }
        } while (th2 != null);
        String str2 = "null";
        if (sAXSourceLocator != null) {
            if (sAXSourceLocator.getPublicId() != sAXSourceLocator.getPublicId()) {
                str = sAXSourceLocator.getPublicId();
            } else {
                str = sAXSourceLocator.getSystemId() != null ? sAXSourceLocator.getSystemId() : "SystemId-Unknown";
            }
            printWriter.print(str + ":Line=" + sAXSourceLocator.getLineNumber() + ";Column=" + sAXSourceLocator.getColumnNumber() + PluralRules.KEYWORD_RULE_SEPARATOR);
            StringBuilder sb = new StringBuilder();
            sb.append("exception:");
            sb.append(th.getMessage());
            printWriter.println(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("root-cause:");
            if (th2 != null) {
                str2 = th2.getMessage();
            }
            sb2.append(str2);
            printWriter.println(sb2.toString());
            logSourceLine(printWriter, sAXSourceLocator);
            return;
        }
        printWriter.print("SystemId-Unknown:locator-unavailable: ");
        printWriter.println("exception:" + th.getMessage());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("root-cause:");
        if (th2 != null) {
            str2 = th2.getMessage();
        }
        sb3.append(str2);
        printWriter.println(sb3.toString());
    }

    public static void logSourceLine(PrintWriter printWriter, SourceLocator sourceLocator) {
        if (sourceLocator != null) {
            if (printWriter == null) {
                printWriter = new PrintWriter((OutputStream) System.err, true);
            }
            String systemId = sourceLocator.getSystemId();
            if (systemId == null) {
                printWriter.println("line: (No systemId; cannot read file)");
                printWriter.println();
                return;
            }
            try {
                int lineNumber = sourceLocator.getLineNumber();
                int columnNumber = sourceLocator.getColumnNumber();
                printWriter.println("line: " + getSourceLine(systemId, lineNumber));
                StringBuffer stringBuffer = new StringBuffer("line: ");
                for (int i = 1; i < columnNumber; i++) {
                    stringBuffer.append(' ');
                }
                stringBuffer.append('^');
                printWriter.println(stringBuffer.toString());
            } catch (Exception e) {
                printWriter.println("line: logSourceLine unavailable due to: " + e.getMessage());
                printWriter.println();
            }
        }
    }

    protected static String getSourceLine(String str, int i) throws Exception {
        URL url;
        Throwable th;
        InputStream inputStream;
        try {
            url = new URL(str);
        } catch (MalformedURLException e) {
            int indexOf = str.indexOf(58);
            int indexOf2 = str.indexOf(47);
            if (indexOf == -1 || indexOf2 == -1 || indexOf >= indexOf2) {
                url = new URL(SystemIDResolver.getAbsoluteURI(str));
            } else {
                throw e;
            }
        }
        BufferedReader bufferedReader = null;
        String str2 = null;
        bufferedReader = null;
        try {
            inputStream = url.openConnection().getInputStream();
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream));
                for (int i2 = 1; i2 <= i; i2++) {
                    try {
                        str2 = bufferedReader2.readLine();
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = bufferedReader2;
                        bufferedReader.close();
                        inputStream.close();
                        throw th;
                    }
                }
                bufferedReader2.close();
                inputStream.close();
                return str2;
            } catch (Throwable th3) {
                th = th3;
                bufferedReader.close();
                inputStream.close();
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            inputStream = null;
            bufferedReader.close();
            inputStream.close();
            throw th;
        }
    }

    public void setThrowOnWarning(boolean z) {
        this.throwOnWarning = z;
    }

    public boolean getThrowOnWarning() {
        return this.throwOnWarning;
    }

    public void setThrowOnError(boolean z) {
        this.throwOnError = z;
    }

    public boolean getThrowOnError() {
        return this.throwOnError;
    }

    public void setThrowOnFatalError(boolean z) {
        this.throwOnFatalError = z;
    }

    public boolean getThrowOnFatalError() {
        return this.throwOnFatalError;
    }
}
