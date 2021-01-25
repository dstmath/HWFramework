package ohos.org.xml.sax.helpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;

public final class XMLReaderFactory {
    private static String _clsFromJar = null;
    private static boolean _jarread = false;
    private static final String property = "org.xml.sax.driver";
    private static SecuritySupport ss = new SecuritySupport();

    private XMLReaderFactory() {
    }

    public static XMLReader createXMLReader() throws SAXException {
        String str;
        InputStream inputStream;
        ClassLoader contextClassLoader = ss.getContextClassLoader();
        try {
            str = ss.getSystemProperty(property);
        } catch (RuntimeException unused) {
            str = null;
        }
        if (str == null) {
            if (!_jarread) {
                _jarread = true;
                if (contextClassLoader != null) {
                    try {
                        inputStream = ss.getResourceAsStream(contextClassLoader, "META-INF/services/org.xml.sax.driver");
                        if (inputStream == null) {
                            try {
                                inputStream = ss.getResourceAsStream(null, "META-INF/services/org.xml.sax.driver");
                                contextClassLoader = null;
                            } catch (Exception unused2) {
                                contextClassLoader = null;
                            }
                        }
                    } catch (Exception unused3) {
                    }
                } else {
                    inputStream = ss.getResourceAsStream(contextClassLoader, "META-INF/services/org.xml.sax.driver");
                }
                if (inputStream != null) {
                    _clsFromJar = new BufferedReader(new InputStreamReader(inputStream, "UTF8")).readLine();
                    inputStream.close();
                }
            }
            str = _clsFromJar;
        }
        if (str == null) {
            str = "ohos.com.sun.org.apache.xerces.internal.parsers.SAXParser";
        }
        return loadClass(contextClassLoader, str);
    }

    public static XMLReader createXMLReader(String str) throws SAXException {
        return loadClass(ss.getContextClassLoader(), str);
    }

    private static XMLReader loadClass(ClassLoader classLoader, String str) throws SAXException {
        try {
            return (XMLReader) NewInstance.newInstance(classLoader, str);
        } catch (ClassNotFoundException e) {
            throw new SAXException("SAX2 driver class " + str + " not found", e);
        } catch (IllegalAccessException e2) {
            throw new SAXException("SAX2 driver class " + str + " found but cannot be loaded", e2);
        } catch (InstantiationException e3) {
            throw new SAXException("SAX2 driver class " + str + " loaded but cannot be instantiated (no empty public constructor?)", e3);
        } catch (ClassCastException e4) {
            throw new SAXException("SAX2 driver class " + str + " does not implement XMLReader", e4);
        }
    }
}
