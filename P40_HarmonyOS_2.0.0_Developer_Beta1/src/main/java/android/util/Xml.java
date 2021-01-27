package android.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import libcore.util.XmlObjectFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Xml {
    public static String FEATURE_RELAXED = "http://xmlpull.org/v1/doc/features.html#relaxed";

    private Xml() {
    }

    public static void parse(String xml, ContentHandler contentHandler) throws SAXException {
        try {
            XMLReader reader = XmlObjectFactory.newXMLReader();
            reader.setContentHandler(contentHandler);
            reader.parse(new InputSource(new StringReader(xml)));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static void parse(Reader in, ContentHandler contentHandler) throws IOException, SAXException {
        XMLReader reader = XmlObjectFactory.newXMLReader();
        reader.setContentHandler(contentHandler);
        reader.parse(new InputSource(in));
    }

    public static void parse(InputStream in, Encoding encoding, ContentHandler contentHandler) throws IOException, SAXException {
        XMLReader reader = XmlObjectFactory.newXMLReader();
        reader.setContentHandler(contentHandler);
        InputSource source = new InputSource(in);
        source.setEncoding(encoding.expatName);
        reader.parse(source);
    }

    public static XmlPullParser newPullParser() {
        try {
            XmlPullParser parser = XmlObjectFactory.newXmlPullParser();
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-docdecl", true);
            parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
            return parser;
        } catch (XmlPullParserException e) {
            throw new AssertionError();
        }
    }

    public static XmlSerializer newSerializer() {
        return XmlObjectFactory.newXmlSerializer();
    }

    public enum Encoding {
        US_ASCII("US-ASCII"),
        UTF_8("UTF-8"),
        UTF_16("UTF-16"),
        ISO_8859_1("ISO-8859-1");
        
        final String expatName;

        private Encoding(String expatName2) {
            this.expatName = expatName2;
        }
    }

    public static Encoding findEncodingByName(String encodingName) throws UnsupportedEncodingException {
        if (encodingName == null) {
            return Encoding.UTF_8;
        }
        Encoding[] values = Encoding.values();
        for (Encoding encoding : values) {
            if (encoding.expatName.equalsIgnoreCase(encodingName)) {
                return encoding;
            }
        }
        throw new UnsupportedEncodingException(encodingName);
    }

    public static AttributeSet asAttributeSet(XmlPullParser parser) {
        if (parser instanceof AttributeSet) {
            return (AttributeSet) parser;
        }
        return new XmlPullAttributes(parser);
    }
}
