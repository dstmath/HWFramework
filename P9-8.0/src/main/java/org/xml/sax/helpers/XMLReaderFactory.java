package org.xml.sax.helpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public final class XMLReaderFactory {
    private static final String property = "org.xml.sax.driver";

    private XMLReaderFactory() {
    }

    public static XMLReader createXMLReader() throws SAXException {
        String className = null;
        ClassLoader loader = NewInstance.getClassLoader();
        try {
            className = System.getProperty(property);
        } catch (RuntimeException e) {
        }
        if (className == null) {
            InputStream in;
            try {
                String service = "META-INF/services/org.xml.sax.driver";
                if (loader == null) {
                    in = ClassLoader.getSystemResourceAsStream(service);
                } else {
                    in = loader.getResourceAsStream(service);
                }
                if (in != null) {
                    className = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).readLine();
                    in.close();
                }
            } catch (Exception e2) {
            } catch (Throwable th) {
                in.close();
            }
        }
        if (className != null) {
            return loadClass(loader, className);
        }
        try {
            return new ParserAdapter(ParserFactory.makeParser());
        } catch (Exception e3) {
            throw new SAXException("Can't create default XMLReader; is system property org.xml.sax.driver set?");
        }
    }

    public static XMLReader createXMLReader(String className) throws SAXException {
        return loadClass(NewInstance.getClassLoader(), className);
    }

    private static XMLReader loadClass(ClassLoader loader, String className) throws SAXException {
        try {
            return (XMLReader) NewInstance.newInstance(loader, className);
        } catch (ClassNotFoundException e1) {
            throw new SAXException("SAX2 driver class " + className + " not found", e1);
        } catch (IllegalAccessException e2) {
            throw new SAXException("SAX2 driver class " + className + " found but cannot be loaded", e2);
        } catch (InstantiationException e3) {
            throw new SAXException("SAX2 driver class " + className + " loaded but cannot be instantiated (no empty public constructor?)", e3);
        } catch (ClassCastException e4) {
            throw new SAXException("SAX2 driver class " + className + " does not implement XMLReader", e4);
        }
    }
}
