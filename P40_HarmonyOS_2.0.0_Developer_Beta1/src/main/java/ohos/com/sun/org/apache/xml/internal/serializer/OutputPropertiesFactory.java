package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Utils;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;

public final class OutputPropertiesFactory {
    private static final Class ACCESS_CONTROLLER_CLASS = findAccessControllerClass();
    public static final String ORACLE_IS_STANDALONE = "http://www.oracle.com/xml/is-standalone";
    private static final String PROP_DIR = "com/sun/org/apache/xml/internal/serializer/";
    private static final String PROP_FILE_HTML = "output_html.properties";
    private static final String PROP_FILE_TEXT = "output_text.properties";
    private static final String PROP_FILE_UNKNOWN = "output_unknown.properties";
    private static final String PROP_FILE_XML = "output_xml.properties";
    public static final String S_BUILTIN_EXTENSIONS_UNIVERSAL = "{http://xml.apache.org/xalan}";
    private static final String S_BUILTIN_EXTENSIONS_URL = "http://xml.apache.org/xalan";
    public static final String S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL = "{http://xml.apache.org/xslt}";
    public static final int S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN = 28;
    private static final String S_BUILTIN_OLD_EXTENSIONS_URL = "http://xml.apache.org/xslt";
    public static final String S_KEY_CONTENT_HANDLER = "{http://xml.apache.org/xalan}content-handler";
    public static final String S_KEY_ENTITIES = "{http://xml.apache.org/xalan}entities";
    public static final String S_KEY_INDENT_AMOUNT = "{http://xml.apache.org/xalan}indent-amount";
    public static final String S_KEY_LINE_SEPARATOR = "{http://xml.apache.org/xalan}line-separator";
    public static final String S_OMIT_META_TAG = "{http://xml.apache.org/xalan}omit-meta-tag";
    public static final String S_USE_URL_ESCAPING = "{http://xml.apache.org/xalan}use-url-escaping";
    private static final String S_XALAN_PREFIX = "org.apache.xslt.";
    private static final int S_XALAN_PREFIX_LEN = 16;
    private static final String S_XSLT_PREFIX = "xslt.output.";
    private static final int S_XSLT_PREFIX_LEN = 12;
    private static Properties m_html_properties = null;
    private static Integer m_synch_object = new Integer(1);
    private static Properties m_text_properties = null;
    private static Properties m_unknown_properties = null;
    private static Properties m_xml_properties = null;

    private static Class findAccessControllerClass() {
        try {
            return Class.forName("java.security.AccessController");
        } catch (Exception unused) {
            return null;
        }
    }

    public static final Properties getDefaultMethodProperties(String str) {
        String str2;
        IOException e;
        Throwable th;
        Properties properties;
        try {
            synchronized (m_synch_object) {
                try {
                    if (m_xml_properties == null) {
                        str2 = PROP_FILE_XML;
                        try {
                            m_xml_properties = loadPropertiesFile(str2, null);
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } else {
                        str2 = null;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            try {
                if (str.equals("xml")) {
                    properties = m_xml_properties;
                } else if (str.equals("html")) {
                    if (m_html_properties == null) {
                        m_html_properties = loadPropertiesFile(PROP_FILE_HTML, m_xml_properties);
                    }
                    properties = m_html_properties;
                } else if (str.equals("text")) {
                    if (m_text_properties == null) {
                        try {
                            m_text_properties = loadPropertiesFile(PROP_FILE_TEXT, m_xml_properties);
                            if (m_text_properties.getProperty(Constants.ATTRNAME_OUTPUT_ENCODING) == null) {
                                m_text_properties.put(Constants.ATTRNAME_OUTPUT_ENCODING, Encodings.getMimeEncoding(null));
                            }
                        } catch (IOException e2) {
                            e = e2;
                            str2 = PROP_FILE_TEXT;
                            throw new WrappedRuntimeException(Utils.messages.createMessage("ER_COULD_NOT_LOAD_METHOD_PROPERTY", new Object[]{str2, str}), e);
                        }
                    }
                    properties = m_text_properties;
                } else if (str.equals("")) {
                    if (m_unknown_properties == null) {
                        m_unknown_properties = loadPropertiesFile(PROP_FILE_UNKNOWN, m_xml_properties);
                    }
                    properties = m_unknown_properties;
                } else {
                    properties = m_xml_properties;
                }
                return new Properties(properties);
            } catch (IOException e3) {
                e = e3;
                throw new WrappedRuntimeException(Utils.messages.createMessage("ER_COULD_NOT_LOAD_METHOD_PROPERTY", new Object[]{str2, str}), e);
            }
        } catch (IOException e4) {
            str2 = null;
            e = e4;
            throw new WrappedRuntimeException(Utils.messages.createMessage("ER_COULD_NOT_LOAD_METHOD_PROPERTY", new Object[]{str2, str}), e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x008e A[SYNTHETIC, Splitter:B:52:0x008e] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x008f A[Catch:{ IOException -> 0x009f, SecurityException -> 0x008a, all -> 0x0087, all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00a3 A[Catch:{ IOException -> 0x009f, SecurityException -> 0x008a, all -> 0x0087, all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00a4 A[Catch:{ IOException -> 0x009f, SecurityException -> 0x008a, all -> 0x0087, all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00b7  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00bc  */
    private static Properties loadPropertiesFile(final String str, Properties properties) throws IOException {
        Throwable th;
        BufferedInputStream bufferedInputStream;
        IOException e;
        SecurityException e2;
        InputStream inputStream;
        String str2;
        String str3;
        String str4;
        Properties properties2 = new Properties(properties);
        InputStream inputStream2 = null;
        try {
            if (ACCESS_CONTROLLER_CLASS != null) {
                inputStream = (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
                    /* class ohos.com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory.AnonymousClass1 */

                    @Override // java.security.PrivilegedAction
                    public Object run() {
                        return OutputPropertiesFactory.class.getResourceAsStream(str);
                    }
                });
            } else {
                inputStream = OutputPropertiesFactory.class.getResourceAsStream(str);
            }
            try {
                bufferedInputStream = new BufferedInputStream(inputStream);
                try {
                    properties2.load(bufferedInputStream);
                    bufferedInputStream.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    Enumeration keys = ((Properties) properties2.clone()).keys();
                    while (keys.hasMoreElements()) {
                        String str5 = (String) keys.nextElement();
                        try {
                            str2 = SecuritySupport.getSystemProperty(str5);
                        } catch (SecurityException unused) {
                            str2 = null;
                        }
                        if (str2 == null) {
                            str2 = (String) properties2.get(str5);
                        }
                        String fixupPropertyString = fixupPropertyString(str5, true);
                        try {
                            str3 = SecuritySupport.getSystemProperty(fixupPropertyString);
                        } catch (SecurityException unused2) {
                            str3 = null;
                        }
                        if (str3 == null) {
                            str4 = fixupPropertyString(str2, false);
                        } else {
                            str4 = fixupPropertyString(str3, false);
                        }
                        if (str5 != fixupPropertyString || str2 != str4) {
                            properties2.remove(str5);
                            properties2.put(fixupPropertyString, str4);
                        }
                    }
                    return properties2;
                } catch (IOException e3) {
                    e = e3;
                    if (properties == null) {
                    }
                } catch (SecurityException e4) {
                    e2 = e4;
                    inputStream2 = inputStream;
                    if (properties == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    inputStream2 = inputStream;
                    if (bufferedInputStream != null) {
                    }
                    if (inputStream2 != null) {
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e = e5;
                if (properties == null) {
                    throw e;
                }
                throw new WrappedRuntimeException(Utils.messages.createMessage("ER_COULD_NOT_LOAD_RESOURCE", new Object[]{str}), e);
            } catch (SecurityException e6) {
                e2 = e6;
                bufferedInputStream = null;
                inputStream2 = inputStream;
                if (properties == null) {
                    throw e2;
                }
                throw new WrappedRuntimeException(Utils.messages.createMessage("ER_COULD_NOT_LOAD_RESOURCE", new Object[]{str}), e2);
            } catch (Throwable th3) {
                th = th3;
                bufferedInputStream = null;
                inputStream2 = inputStream;
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                throw th;
            }
        } catch (IOException e7) {
            e = e7;
            if (properties == null) {
            }
        } catch (SecurityException e8) {
            e2 = e8;
            bufferedInputStream = null;
            if (properties == null) {
            }
        } catch (Throwable th4) {
            th = th4;
            if (bufferedInputStream != null) {
            }
            if (inputStream2 != null) {
            }
            throw th;
        }
    }

    private static String fixupPropertyString(String str, boolean z) {
        if (z && str.startsWith(S_XSLT_PREFIX)) {
            str = str.substring(S_XSLT_PREFIX_LEN);
        }
        if (str.startsWith(S_XALAN_PREFIX)) {
            str = S_BUILTIN_EXTENSIONS_UNIVERSAL + str.substring(S_XALAN_PREFIX_LEN);
        }
        int indexOf = str.indexOf("\\u003a");
        if (indexOf <= 0) {
            return str;
        }
        return str.substring(0, indexOf) + ":" + str.substring(indexOf + 6);
    }
}
