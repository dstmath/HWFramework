package org.apache.harmony.xml.parsers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.SAXParser;
import org.apache.harmony.xml.ExpatReader;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;

final class SAXParserImpl extends SAXParser {
    private Map<String, Boolean> initialFeatures;
    private Parser parser;
    private XMLReader reader;

    SAXParserImpl(Map<String, Boolean> initialFeatures) throws SAXNotRecognizedException, SAXNotSupportedException {
        Map emptyMap;
        if (initialFeatures.isEmpty()) {
            emptyMap = Collections.emptyMap();
        } else {
            emptyMap = new HashMap(initialFeatures);
        }
        this.initialFeatures = emptyMap;
        resetInternal();
    }

    private void resetInternal() throws SAXNotSupportedException, SAXNotRecognizedException {
        this.reader = new ExpatReader();
        for (Entry<String, Boolean> entry : this.initialFeatures.entrySet()) {
            this.reader.setFeature((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
        }
    }

    public void reset() {
        try {
            resetInternal();
        } catch (SAXNotRecognizedException e) {
            throw new AssertionError();
        } catch (SAXNotSupportedException e2) {
            throw new AssertionError();
        }
    }

    public Parser getParser() {
        if (this.parser == null) {
            this.parser = new XMLReaderAdapter(this.reader);
        }
        return this.parser;
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.reader.getProperty(name);
    }

    public XMLReader getXMLReader() {
        return this.reader;
    }

    public boolean isNamespaceAware() {
        try {
            return this.reader.getFeature("http://xml.org/sax/features/namespaces");
        } catch (SAXException e) {
            return false;
        }
    }

    public boolean isValidating() {
        return false;
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.reader.setProperty(name, value);
    }
}
