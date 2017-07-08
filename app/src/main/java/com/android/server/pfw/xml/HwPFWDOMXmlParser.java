package com.android.server.pfw.xml;

import com.android.server.pfw.log.HwPFWLogger;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class HwPFWDOMXmlParser {
    private static final String TAG = "HwPFWDOMXmlParser";
    private InnerParser parser;

    private static class InnerParser {
        private Document document;

        private InnerParser() {
            this.document = null;
        }

        public void do_parse(InputStream is) {
            try {
                this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            } catch (ParserConfigurationException ex) {
                HwPFWLogger.e(HwPFWDOMXmlParser.TAG, "do_parse ParserConfigurationException " + ex.getMessage());
                this.document = null;
            } catch (SAXException ex2) {
                HwPFWLogger.e(HwPFWDOMXmlParser.TAG, "do_parse SAXException " + ex2.getMessage());
                this.document = null;
            } catch (IOException ex3) {
                HwPFWLogger.e(HwPFWDOMXmlParser.TAG, "do_parse IOException " + ex3.getMessage());
                this.document = null;
            }
        }

        public Document getDocument() {
            return this.document;
        }
    }

    public HwPFWDOMXmlParser(InputStream is) {
        this.parser = new InnerParser();
        this.parser.do_parse(is);
    }

    public Element rootElement() {
        if (this.parser.getDocument() != null) {
            return this.parser.getDocument().getDocumentElement();
        }
        return null;
    }
}
