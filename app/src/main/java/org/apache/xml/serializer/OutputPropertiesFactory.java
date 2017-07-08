package org.apache.xml.serializer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;

public final class OutputPropertiesFactory {
    private static final Class ACCESS_CONTROLLER_CLASS = null;
    private static final String PROP_DIR = null;
    private static final String PROP_FILE_HTML = "output_html.properties";
    private static final String PROP_FILE_TEXT = "output_text.properties";
    private static final String PROP_FILE_UNKNOWN = "output_unknown.properties";
    private static final String PROP_FILE_XML = "output_xml.properties";
    public static final String S_BUILTIN_EXTENSIONS_UNIVERSAL = "{http://xml.apache.org/xalan}";
    private static final String S_BUILTIN_EXTENSIONS_URL = "http://xml.apache.org/xalan";
    public static final String S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL = "{http://xml.apache.org/xslt}";
    public static final int S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN = 0;
    private static final String S_BUILTIN_OLD_EXTENSIONS_URL = "http://xml.apache.org/xslt";
    public static final String S_KEY_CONTENT_HANDLER = "{http://xml.apache.org/xalan}content-handler";
    public static final String S_KEY_ENTITIES = "{http://xml.apache.org/xalan}entities";
    public static final String S_KEY_INDENT_AMOUNT = "{http://xml.apache.org/xalan}indent-amount";
    public static final String S_KEY_LINE_SEPARATOR = "{http://xml.apache.org/xalan}line-separator";
    public static final String S_OMIT_META_TAG = "{http://xml.apache.org/xalan}omit-meta-tag";
    public static final String S_USE_URL_ESCAPING = "{http://xml.apache.org/xalan}use-url-escaping";
    private static final String S_XALAN_PREFIX = "org.apache.xslt.";
    private static final int S_XALAN_PREFIX_LEN = 0;
    private static final String S_XSLT_PREFIX = "xslt.output.";
    private static final int S_XSLT_PREFIX_LEN = 0;
    private static Properties m_html_properties;
    private static Integer m_synch_object;
    private static Properties m_text_properties;
    private static Properties m_unknown_properties;
    private static Properties m_xml_properties;

    /* renamed from: org.apache.xml.serializer.OutputPropertiesFactory.1 */
    static class AnonymousClass1 implements PrivilegedAction {
        final /* synthetic */ String val$resourceName;

        AnonymousClass1(String val$resourceName) {
            this.val$resourceName = val$resourceName;
        }

        public Object run() {
            return OutputPropertiesFactory.class.getResourceAsStream(this.val$resourceName);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.OutputPropertiesFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.OutputPropertiesFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.OutputPropertiesFactory.<clinit>():void");
    }

    private static Class findAccessControllerClass() {
        try {
            return Class.forName("java.security.AccessController");
        } catch (Exception e) {
            return null;
        }
    }

    public static final Properties getDefaultMethodProperties(String method) {
        String str = null;
        try {
            Properties defaultProperties;
            synchronized (m_synch_object) {
                if (m_xml_properties == null) {
                    str = PROP_FILE_XML;
                    m_xml_properties = loadPropertiesFile(str, null);
                }
            }
            if (method.equals(SerializerConstants.XML_PREFIX)) {
                defaultProperties = m_xml_properties;
            } else if (method.equals(Method.HTML)) {
                if (m_html_properties == null) {
                    m_html_properties = loadPropertiesFile(PROP_FILE_HTML, m_xml_properties);
                }
                defaultProperties = m_html_properties;
            } else if (method.equals(Method.TEXT)) {
                if (m_text_properties == null) {
                    m_text_properties = loadPropertiesFile(PROP_FILE_TEXT, m_xml_properties);
                    if (m_text_properties.getProperty(DOMConstants.S_XSL_OUTPUT_ENCODING) == null) {
                        m_text_properties.put(DOMConstants.S_XSL_OUTPUT_ENCODING, Encodings.getMimeEncoding(null));
                    }
                }
                defaultProperties = m_text_properties;
            } else if (method.equals(SerializerConstants.EMPTYSTRING)) {
                if (m_unknown_properties == null) {
                    m_unknown_properties = loadPropertiesFile(PROP_FILE_UNKNOWN, m_xml_properties);
                }
                defaultProperties = m_unknown_properties;
            } else {
                defaultProperties = m_xml_properties;
            }
            return new Properties(defaultProperties);
        } catch (IOException ioe) {
            throw new WrappedRuntimeException(Utils.messages.createMessage(MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, new Object[]{str, method}), ioe);
        }
    }

    private static Properties loadPropertiesFile(String resourceName, Properties defaults) throws IOException {
        IOException ioe;
        SecurityException se;
        Throwable th;
        Properties props = new Properties(defaults);
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            if (ACCESS_CONTROLLER_CLASS != null) {
                inputStream = (InputStream) AccessController.doPrivileged(new AnonymousClass1(resourceName));
            } else {
                inputStream = OutputPropertiesFactory.class.getResourceAsStream(resourceName);
            }
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            try {
                props.load(bis);
                if (bis != null) {
                    bis.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                Enumeration keys = ((Properties) props.clone()).keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String str = null;
                    try {
                        str = System.getProperty(key);
                    } catch (SecurityException e) {
                    }
                    if (str == null) {
                        str = (String) props.get(key);
                    }
                    String newKey = fixupPropertyString(key, true);
                    String newValue = null;
                    try {
                        newValue = System.getProperty(newKey);
                    } catch (SecurityException e2) {
                    }
                    if (newValue == null) {
                        newValue = fixupPropertyString(str, false);
                    } else {
                        newValue = fixupPropertyString(newValue, false);
                    }
                    if (key != newKey || str != newValue) {
                        props.remove(key);
                        props.put(newKey, newValue);
                    }
                }
                return props;
            } catch (IOException e3) {
                ioe = e3;
                bufferedInputStream = bis;
                if (defaults != null) {
                    throw new WrappedRuntimeException(Utils.messages.createMessage(MsgKey.ER_COULD_NOT_LOAD_RESOURCE, new Object[]{resourceName}), ioe);
                }
                throw ioe;
            } catch (SecurityException e4) {
                se = e4;
                bufferedInputStream = bis;
                if (defaults != null) {
                    throw new WrappedRuntimeException(Utils.messages.createMessage(MsgKey.ER_COULD_NOT_LOAD_RESOURCE, new Object[]{resourceName}), se);
                }
                throw se;
            } catch (Throwable th2) {
                th = th2;
                bufferedInputStream = bis;
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            ioe = e5;
            if (defaults != null) {
                throw ioe;
            }
            throw new WrappedRuntimeException(Utils.messages.createMessage(MsgKey.ER_COULD_NOT_LOAD_RESOURCE, new Object[]{resourceName}), ioe);
        } catch (SecurityException e6) {
            se = e6;
            if (defaults != null) {
                throw se;
            } else {
                throw new WrappedRuntimeException(Utils.messages.createMessage(MsgKey.ER_COULD_NOT_LOAD_RESOURCE, new Object[]{resourceName}), se);
            }
        } catch (Throwable th3) {
            th = th3;
        }
    }

    private static String fixupPropertyString(String s, boolean doClipping) {
        if (doClipping && s.startsWith(S_XSLT_PREFIX)) {
            s = s.substring(S_XSLT_PREFIX_LEN);
        }
        if (s.startsWith(S_XALAN_PREFIX)) {
            s = S_BUILTIN_EXTENSIONS_UNIVERSAL + s.substring(S_XALAN_PREFIX_LEN);
        }
        int index = s.indexOf("\\u003a");
        if (index <= 0) {
            return s;
        }
        return s.substring(S_XALAN_PREFIX_LEN, index) + ":" + s.substring(index + 6);
    }
}
