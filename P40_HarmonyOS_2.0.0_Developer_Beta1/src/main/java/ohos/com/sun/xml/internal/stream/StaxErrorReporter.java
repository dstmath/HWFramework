package ohos.com.sun.xml.internal.stream;

import ohos.com.sun.org.apache.xerces.internal.impl.PropertyManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.MessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.javax.xml.stream.Location;
import ohos.javax.xml.stream.XMLReporter;
import ohos.javax.xml.stream.XMLStreamException;

public class StaxErrorReporter extends XMLErrorReporter {
    protected XMLReporter fXMLReporter = null;

    public StaxErrorReporter(PropertyManager propertyManager) {
        putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", new XMLMessageFormatter());
        reset(propertyManager);
    }

    public StaxErrorReporter() {
        putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", new XMLMessageFormatter());
    }

    public void reset(PropertyManager propertyManager) {
        this.fXMLReporter = (XMLReporter) propertyManager.getProperty("javax.xml.stream.reporter");
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter
    public String reportError(XMLLocator xMLLocator, String str, String str2, Object[] objArr, short s) throws XNIException {
        String str3;
        MessageFormatter messageFormatter = getMessageFormatter(str);
        if (messageFormatter != null) {
            str3 = messageFormatter.formatMessage(this.fLocale, str2, objArr);
        } else {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str);
            stringBuffer.append('#');
            stringBuffer.append(str2);
            int length = objArr != null ? objArr.length : 0;
            if (length > 0) {
                stringBuffer.append('?');
                for (int i = 0; i < length; i++) {
                    stringBuffer.append(objArr[i]);
                    if (i < length - 1) {
                        stringBuffer.append('&');
                    }
                }
            }
            str3 = stringBuffer.toString();
        }
        if (s == 0) {
            try {
                if (this.fXMLReporter != null) {
                    this.fXMLReporter.report(str3, "WARNING", (Object) null, convertToStaxLocation(xMLLocator));
                }
            } catch (XMLStreamException e) {
                throw new XNIException((Exception) e);
            }
        } else if (s == 1) {
            try {
                if (this.fXMLReporter != null) {
                    this.fXMLReporter.report(str3, "ERROR", (Object) null, convertToStaxLocation(xMLLocator));
                }
            } catch (XMLStreamException e2) {
                throw new XNIException((Exception) e2);
            }
        } else if (s == 2 && !this.fContinueAfterFatalError) {
            throw new XNIException(str3);
        }
        return str3;
    }

    /* access modifiers changed from: package-private */
    public Location convertToStaxLocation(final XMLLocator xMLLocator) {
        return new Location() {
            /* class ohos.com.sun.xml.internal.stream.StaxErrorReporter.AnonymousClass1 */

            public String getLocationURI() {
                return "";
            }

            public int getColumnNumber() {
                return xMLLocator.getColumnNumber();
            }

            public int getLineNumber() {
                return xMLLocator.getLineNumber();
            }

            public String getPublicId() {
                return xMLLocator.getPublicId();
            }

            public String getSystemId() {
                return xMLLocator.getLiteralSystemId();
            }

            public int getCharacterOffset() {
                return xMLLocator.getCharacterOffset();
            }
        };
    }
}
