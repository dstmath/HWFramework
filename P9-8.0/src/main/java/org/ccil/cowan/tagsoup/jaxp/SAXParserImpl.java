package org.ccil.cowan.tagsoup.jaxp;

import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.SAXParser;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class SAXParserImpl extends SAXParser {
    final Parser parser = new Parser();

    protected SAXParserImpl() {
    }

    public static SAXParserImpl newInstance(Map features) throws SAXException {
        SAXParserImpl parser = new SAXParserImpl();
        if (features != null) {
            for (Entry entry : features.entrySet()) {
                parser.setFeature((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
            }
        }
        return parser;
    }

    public org.xml.sax.Parser getParser() throws SAXException {
        return new SAX1ParserAdapter(this.parser);
    }

    public XMLReader getXMLReader() {
        return this.parser;
    }

    public boolean isNamespaceAware() {
        try {
            return this.parser.getFeature(Parser.namespacesFeature);
        } catch (SAXException sex) {
            throw new RuntimeException(sex.getMessage());
        }
    }

    public boolean isValidating() {
        try {
            return this.parser.getFeature(Parser.validationFeature);
        } catch (SAXException sex) {
            throw new RuntimeException(sex.getMessage());
        }
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.parser.setProperty(name, value);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.parser.getProperty(name);
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.parser.setFeature(name, value);
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.parser.getFeature(name);
    }
}
