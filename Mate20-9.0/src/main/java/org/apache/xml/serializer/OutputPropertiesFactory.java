package org.apache.xml.serializer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;

public final class OutputPropertiesFactory {
    private static final Class ACCESS_CONTROLLER_CLASS = findAccessControllerClass();
    private static final String PROP_DIR = (SerializerBase.PKG_PATH + '/');
    private static final String PROP_FILE_HTML = "output_html.properties";
    private static final String PROP_FILE_TEXT = "output_text.properties";
    private static final String PROP_FILE_UNKNOWN = "output_unknown.properties";
    private static final String PROP_FILE_XML = "output_xml.properties";
    public static final String S_BUILTIN_EXTENSIONS_UNIVERSAL = "{http://xml.apache.org/xalan}";
    private static final String S_BUILTIN_EXTENSIONS_URL = "http://xml.apache.org/xalan";
    public static final String S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL = "{http://xml.apache.org/xslt}";
    public static final int S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN = S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL.length();
    private static final String S_BUILTIN_OLD_EXTENSIONS_URL = "http://xml.apache.org/xslt";
    public static final String S_KEY_CONTENT_HANDLER = "{http://xml.apache.org/xalan}content-handler";
    public static final String S_KEY_ENTITIES = "{http://xml.apache.org/xalan}entities";
    public static final String S_KEY_INDENT_AMOUNT = "{http://xml.apache.org/xalan}indent-amount";
    public static final String S_KEY_LINE_SEPARATOR = "{http://xml.apache.org/xalan}line-separator";
    public static final String S_OMIT_META_TAG = "{http://xml.apache.org/xalan}omit-meta-tag";
    public static final String S_USE_URL_ESCAPING = "{http://xml.apache.org/xalan}use-url-escaping";
    private static final String S_XALAN_PREFIX = "org.apache.xslt.";
    private static final int S_XALAN_PREFIX_LEN = S_XALAN_PREFIX.length();
    private static final String S_XSLT_PREFIX = "xslt.output.";
    private static final int S_XSLT_PREFIX_LEN = S_XSLT_PREFIX.length();
    private static Properties m_html_properties = null;
    private static Integer m_synch_object = new Integer(1);
    private static Properties m_text_properties = null;
    private static Properties m_unknown_properties = null;
    private static Properties m_xml_properties = null;

    private static Class findAccessControllerClass() {
        try {
            return Class.forName("java.security.AccessController");
        } catch (Exception e) {
            return null;
        }
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    public static final java.util.Properties getDefaultMethodProperties(java.lang.String r7) {
        /*
            r0 = 0
            r1 = 0
            r2 = r1
            java.lang.Integer r3 = m_synch_object     // Catch:{ IOException -> 0x008f }
            monitor-enter(r3)     // Catch:{ IOException -> 0x008f }
            java.util.Properties r4 = m_xml_properties     // Catch:{ all -> 0x008c }
            if (r4 != 0) goto L_0x0013
            java.lang.String r4 = "output_xml.properties"
            r0 = r4
            java.util.Properties r4 = loadPropertiesFile(r0, r1)     // Catch:{ all -> 0x008c }
            m_xml_properties = r4     // Catch:{ all -> 0x008c }
        L_0x0013:
            monitor-exit(r3)     // Catch:{ all -> 0x008c }
            java.lang.String r3 = "xml"
            boolean r3 = r7.equals(r3)     // Catch:{ IOException -> 0x008f }
            if (r3 == 0) goto L_0x001f
            java.util.Properties r1 = m_xml_properties     // Catch:{ IOException -> 0x008f }
        L_0x001e:
            goto L_0x0085
        L_0x001f:
            java.lang.String r3 = "html"
            boolean r3 = r7.equals(r3)     // Catch:{ IOException -> 0x008f }
            if (r3 == 0) goto L_0x0039
            java.util.Properties r1 = m_html_properties     // Catch:{ IOException -> 0x008f }
            if (r1 != 0) goto L_0x0036
            java.lang.String r1 = "output_html.properties"
            r0 = r1
            java.util.Properties r1 = m_xml_properties     // Catch:{ IOException -> 0x008f }
            java.util.Properties r1 = loadPropertiesFile(r0, r1)     // Catch:{ IOException -> 0x008f }
            m_html_properties = r1     // Catch:{ IOException -> 0x008f }
        L_0x0036:
            java.util.Properties r1 = m_html_properties     // Catch:{ IOException -> 0x008f }
            goto L_0x001e
        L_0x0039:
            java.lang.String r3 = "text"
            boolean r3 = r7.equals(r3)     // Catch:{ IOException -> 0x008f }
            if (r3 == 0) goto L_0x0068
            java.util.Properties r3 = m_text_properties     // Catch:{ IOException -> 0x008f }
            if (r3 != 0) goto L_0x0065
            java.lang.String r3 = "output_text.properties"
            r0 = r3
            java.util.Properties r3 = m_xml_properties     // Catch:{ IOException -> 0x008f }
            java.util.Properties r3 = loadPropertiesFile(r0, r3)     // Catch:{ IOException -> 0x008f }
            m_text_properties = r3     // Catch:{ IOException -> 0x008f }
            java.util.Properties r3 = m_text_properties     // Catch:{ IOException -> 0x008f }
            java.lang.String r4 = "encoding"
            java.lang.String r3 = r3.getProperty(r4)     // Catch:{ IOException -> 0x008f }
            if (r3 != 0) goto L_0x0065
            java.lang.String r1 = org.apache.xml.serializer.Encodings.getMimeEncoding(r1)     // Catch:{ IOException -> 0x008f }
            java.util.Properties r3 = m_text_properties     // Catch:{ IOException -> 0x008f }
            java.lang.String r4 = "encoding"
            r3.put(r4, r1)     // Catch:{ IOException -> 0x008f }
        L_0x0065:
            java.util.Properties r1 = m_text_properties     // Catch:{ IOException -> 0x008f }
            goto L_0x001e
        L_0x0068:
            java.lang.String r1 = ""
            boolean r1 = r7.equals(r1)     // Catch:{ IOException -> 0x008f }
            if (r1 == 0) goto L_0x0082
            java.util.Properties r1 = m_unknown_properties     // Catch:{ IOException -> 0x008f }
            if (r1 != 0) goto L_0x007f
            java.lang.String r1 = "output_unknown.properties"
            r0 = r1
            java.util.Properties r1 = m_xml_properties     // Catch:{ IOException -> 0x008f }
            java.util.Properties r1 = loadPropertiesFile(r0, r1)     // Catch:{ IOException -> 0x008f }
            m_unknown_properties = r1     // Catch:{ IOException -> 0x008f }
        L_0x007f:
            java.util.Properties r1 = m_unknown_properties     // Catch:{ IOException -> 0x008f }
            goto L_0x001e
        L_0x0082:
            java.util.Properties r1 = m_xml_properties     // Catch:{ IOException -> 0x008f }
            goto L_0x001e
        L_0x0085:
            java.util.Properties r2 = new java.util.Properties
            r2.<init>(r1)
            return r2
        L_0x008c:
            r1 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x008c }
            throw r1     // Catch:{ IOException -> 0x008f }
        L_0x008f:
            r1 = move-exception
            org.apache.xml.serializer.utils.WrappedRuntimeException r3 = new org.apache.xml.serializer.utils.WrappedRuntimeException
            org.apache.xml.serializer.utils.Messages r4 = org.apache.xml.serializer.utils.Utils.messages
            r5 = 2
            java.lang.Object[] r5 = new java.lang.Object[r5]
            r6 = 0
            r5[r6] = r0
            r6 = 1
            r5[r6] = r7
            java.lang.String r6 = "ER_COULD_NOT_LOAD_METHOD_PROPERTY"
            java.lang.String r4 = r4.createMessage(r6, r5)
            r3.<init>(r4, r1)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.OutputPropertiesFactory.getDefaultMethodProperties(java.lang.String):java.util.Properties");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v5, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private static Properties loadPropertiesFile(final String resourceName, Properties defaults) throws IOException {
        InputStream is;
        String newValue;
        Properties props = new Properties(defaults);
        InputStream is2 = null;
        BufferedInputStream bis = null;
        try {
            if (ACCESS_CONTROLLER_CLASS != null) {
                is = (InputStream) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return OutputPropertiesFactory.class.getResourceAsStream(resourceName);
                    }
                });
            } else {
                is = OutputPropertiesFactory.class.getResourceAsStream(resourceName);
            }
            BufferedInputStream bis2 = new BufferedInputStream(is);
            props.load(bis2);
            bis2.close();
            if (is != null) {
                is.close();
            }
            Enumeration keys = ((Properties) props.clone()).keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = null;
                try {
                    value = System.getProperty(key);
                } catch (SecurityException e) {
                }
                if (value == null) {
                    value = props.get(key);
                }
                String newKey = fixupPropertyString(key, true);
                String newValue2 = null;
                try {
                    newValue2 = System.getProperty(newKey);
                } catch (SecurityException e2) {
                }
                if (newValue2 == null) {
                    newValue = fixupPropertyString(value, false);
                } else {
                    newValue = fixupPropertyString(newValue2, false);
                }
                if (key != newKey || value != newValue) {
                    props.remove(key);
                    props.put(newKey, newValue);
                }
            }
            return props;
        } catch (IOException ioe) {
            if (defaults == null) {
                throw ioe;
            }
            throw new WrappedRuntimeException(Utils.messages.createMessage("ER_COULD_NOT_LOAD_RESOURCE", new Object[]{resourceName}), ioe);
        } catch (SecurityException se) {
            if (defaults == null) {
                throw se;
            }
            throw new WrappedRuntimeException(Utils.messages.createMessage("ER_COULD_NOT_LOAD_RESOURCE", new Object[]{resourceName}), se);
        } catch (Throwable th) {
            if (bis != null) {
                bis.close();
            }
            if (is2 != null) {
                is2.close();
            }
            throw th;
        }
    }

    private static String fixupPropertyString(String s, boolean doClipping) {
        if (doClipping && s.startsWith(S_XSLT_PREFIX)) {
            s = s.substring(S_XSLT_PREFIX_LEN);
        }
        if (s.startsWith(S_XALAN_PREFIX)) {
            s = S_BUILTIN_EXTENSIONS_UNIVERSAL + s.substring(S_XALAN_PREFIX_LEN);
        }
        int indexOf = s.indexOf("\\u003a");
        int index = indexOf;
        if (indexOf <= 0) {
            return s;
        }
        String temp = s.substring(index + 6);
        return s.substring(0, index) + ":" + temp;
    }
}
