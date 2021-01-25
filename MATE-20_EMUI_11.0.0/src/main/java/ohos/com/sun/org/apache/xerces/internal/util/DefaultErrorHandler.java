package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.PrintWriter;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.global.icu.text.PluralRules;

public class DefaultErrorHandler implements XMLErrorHandler {
    protected PrintWriter fOut;

    public DefaultErrorHandler() {
        this(new PrintWriter(System.err));
    }

    public DefaultErrorHandler(PrintWriter printWriter) {
        this.fOut = printWriter;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void warning(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        printError("Warning", xMLParseException);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void error(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        printError("Error", xMLParseException);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void fatalError(String str, String str2, XMLParseException xMLParseException) throws XNIException {
        printError("Fatal Error", xMLParseException);
        throw xMLParseException;
    }

    private void printError(String str, XMLParseException xMLParseException) {
        this.fOut.print("[");
        this.fOut.print(str);
        this.fOut.print("] ");
        String expandedSystemId = xMLParseException.getExpandedSystemId();
        if (expandedSystemId != null) {
            int lastIndexOf = expandedSystemId.lastIndexOf(47);
            if (lastIndexOf != -1) {
                expandedSystemId = expandedSystemId.substring(lastIndexOf + 1);
            }
            this.fOut.print(expandedSystemId);
        }
        this.fOut.print(':');
        this.fOut.print(xMLParseException.getLineNumber());
        this.fOut.print(':');
        this.fOut.print(xMLParseException.getColumnNumber());
        this.fOut.print(PluralRules.KEYWORD_RULE_SEPARATOR);
        this.fOut.print(xMLParseException.getMessage());
        this.fOut.println();
        this.fOut.flush();
    }
}
