package ohos.com.sun.org.apache.xerces.internal.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.util.DefaultErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerProxy;
import ohos.com.sun.org.apache.xerces.internal.util.MessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.org.xml.sax.ErrorHandler;

public class XMLErrorReporter implements XMLComponent {
    protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    private static final Boolean[] FEATURE_DEFAULTS = {null};
    private static final Object[] PROPERTY_DEFAULTS = {null};
    private static final String[] RECOGNIZED_FEATURES = {CONTINUE_AFTER_FATAL_ERROR};
    private static final String[] RECOGNIZED_PROPERTIES = {ERROR_HANDLER};
    public static final short SEVERITY_ERROR = 1;
    public static final short SEVERITY_FATAL_ERROR = 2;
    public static final short SEVERITY_WARNING = 0;
    protected boolean fContinueAfterFatalError;
    protected XMLErrorHandler fDefaultErrorHandler;
    protected XMLErrorHandler fErrorHandler;
    protected Locale fLocale;
    protected XMLLocator fLocator;
    protected Map<String, MessageFormatter> fMessageFormatters = new HashMap();
    private ErrorHandler fSaxProxy = null;

    public void setLocale(Locale locale) {
        this.fLocale = locale;
    }

    public Locale getLocale() {
        return this.fLocale;
    }

    public void setDocumentLocator(XMLLocator xMLLocator) {
        this.fLocator = xMLLocator;
    }

    public void putMessageFormatter(String str, MessageFormatter messageFormatter) {
        this.fMessageFormatters.put(str, messageFormatter);
    }

    public MessageFormatter getMessageFormatter(String str) {
        return this.fMessageFormatters.get(str);
    }

    public MessageFormatter removeMessageFormatter(String str) {
        return this.fMessageFormatters.remove(str);
    }

    public String reportError(String str, String str2, Object[] objArr, short s) throws XNIException {
        return reportError(this.fLocator, str, str2, objArr, s);
    }

    public String reportError(String str, String str2, Object[] objArr, short s, Exception exc) throws XNIException {
        return reportError(this.fLocator, str, str2, objArr, s, exc);
    }

    public String reportError(XMLLocator xMLLocator, String str, String str2, Object[] objArr, short s) throws XNIException {
        return reportError(xMLLocator, str, str2, objArr, s, null);
    }

    public String reportError(XMLLocator xMLLocator, String str, String str2, Object[] objArr, short s, Exception exc) throws XNIException {
        String str3;
        XMLParseException xMLParseException;
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
        if (exc != null) {
            xMLParseException = new XMLParseException(xMLLocator, str3, exc);
        } else {
            xMLParseException = new XMLParseException(xMLLocator, str3);
        }
        XMLErrorHandler xMLErrorHandler = this.fErrorHandler;
        if (xMLErrorHandler == null) {
            if (this.fDefaultErrorHandler == null) {
                this.fDefaultErrorHandler = new DefaultErrorHandler();
            }
            xMLErrorHandler = this.fDefaultErrorHandler;
        }
        if (s == 0) {
            xMLErrorHandler.warning(str, str2, xMLParseException);
        } else if (s == 1) {
            xMLErrorHandler.error(str, str2, xMLParseException);
        } else if (s == 2) {
            xMLErrorHandler.fatalError(str, str2, xMLParseException);
            if (!this.fContinueAfterFatalError) {
                throw xMLParseException;
            }
        }
        return str3;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XNIException {
        this.fContinueAfterFatalError = xMLComponentManager.getFeature(CONTINUE_AFTER_FATAL_ERROR, false);
        this.fErrorHandler = (XMLErrorHandler) xMLComponentManager.getProperty(ERROR_HANDLER);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return (String[]) RECOGNIZED_FEATURES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_FEATURE_PREFIX) && str.length() - 31 == 26 && str.endsWith(Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE)) {
            this.fContinueAfterFatalError = z;
        }
    }

    public boolean getFeature(String str) throws XMLConfigurationException {
        if (!str.startsWith(Constants.XERCES_FEATURE_PREFIX) || str.length() - 31 != 26 || !str.endsWith(Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE)) {
            return false;
        }
        return this.fContinueAfterFatalError;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        return (String[]) RECOGNIZED_PROPERTIES.clone();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        if (str.startsWith(Constants.XERCES_PROPERTY_PREFIX) && str.length() - 33 == 22 && str.endsWith(Constants.ERROR_HANDLER_PROPERTY)) {
            this.fErrorHandler = (XMLErrorHandler) obj;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Boolean getFeatureDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_FEATURES;
            if (i >= strArr.length) {
                return null;
            }
            if (strArr[i].equals(str)) {
                return FEATURE_DEFAULTS[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Object getPropertyDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_PROPERTIES;
            if (i >= strArr.length) {
                return null;
            }
            if (strArr[i].equals(str)) {
                return PROPERTY_DEFAULTS[i];
            }
            i++;
        }
    }

    public XMLErrorHandler getErrorHandler() {
        return this.fErrorHandler;
    }

    public ErrorHandler getSAXErrorHandler() {
        if (this.fSaxProxy == null) {
            this.fSaxProxy = new ErrorHandlerProxy() {
                /* class ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // ohos.com.sun.org.apache.xerces.internal.util.ErrorHandlerProxy
                public XMLErrorHandler getErrorHandler() {
                    return XMLErrorReporter.this.fErrorHandler;
                }
            };
        }
        return this.fSaxProxy;
    }
}
