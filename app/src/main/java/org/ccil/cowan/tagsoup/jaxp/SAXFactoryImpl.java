package org.ccil.cowan.tagsoup.jaxp;

import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class SAXFactoryImpl extends SAXParserFactory {
    private HashMap features;
    private SAXParserImpl prototypeParser;

    public SAXFactoryImpl() {
        this.prototypeParser = null;
        this.features = null;
    }

    public SAXParser newSAXParser() throws ParserConfigurationException {
        try {
            return SAXParserImpl.newInstance(this.features);
        } catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
    }

    public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        Object obj;
        getPrototype().setFeature(name, value);
        if (this.features == null) {
            this.features = new LinkedHashMap();
        }
        HashMap hashMap = this.features;
        if (value) {
            obj = Boolean.TRUE;
        } else {
            obj = Boolean.FALSE;
        }
        hashMap.put(name, obj);
    }

    public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        return getPrototype().getFeature(name);
    }

    private SAXParserImpl getPrototype() {
        if (this.prototypeParser == null) {
            this.prototypeParser = new SAXParserImpl();
        }
        return this.prototypeParser;
    }
}
