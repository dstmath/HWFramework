package java.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class XMLUtils {
    static final /* synthetic */ boolean -assertionsDisabled = (XMLUtils.class.desiredAssertionStatus() ^ 1);
    private static final String EXTERNAL_XML_VERSION = "1.0";
    private static final String PROPS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- DTD for properties --><!ELEMENT properties ( comment?, entry* ) ><!ATTLIST properties version CDATA #FIXED \"1.0\"><!ELEMENT comment (#PCDATA) ><!ELEMENT entry (#PCDATA) ><!ATTLIST entry  key CDATA #REQUIRED>";
    private static final String PROPS_DTD_URI = "http://java.sun.com/dtd/properties.dtd";

    private static class EH implements ErrorHandler {
        /* synthetic */ EH(EH -this0) {
            this();
        }

        private EH() {
        }

        public void error(SAXParseException x) throws SAXException {
            throw x;
        }

        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }

        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }

    private static class Resolver implements EntityResolver {
        /* synthetic */ Resolver(Resolver -this0) {
            this();
        }

        private Resolver() {
        }

        public InputSource resolveEntity(String pid, String sid) throws SAXException {
            if (sid.equals(XMLUtils.PROPS_DTD_URI)) {
                InputSource is = new InputSource(new StringReader(XMLUtils.PROPS_DTD));
                is.setSystemId(XMLUtils.PROPS_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + sid);
        }
    }

    XMLUtils() {
    }

    static void load(Properties props, InputStream in) throws IOException, InvalidPropertiesFormatException {
        try {
            Element propertiesElement = getLoadingDoc(in).getDocumentElement();
            String xmlVersion = propertiesElement.getAttribute("version");
            if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0) {
                throw new InvalidPropertiesFormatException("Exported Properties file format version " + xmlVersion + " is not supported. This java installation can read" + " versions " + EXTERNAL_XML_VERSION + " or older. You" + " may need to install a newer version of JDK.");
            }
            importProperties(props, propertiesElement);
        } catch (Throwable saxe) {
            throw new InvalidPropertiesFormatException(saxe);
        }
    }

    static Document getLoadingDoc(InputStream in) throws SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new Resolver());
            db.setErrorHandler(new EH());
            return db.parse(new InputSource(in));
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    static void importProperties(Properties props, Element propertiesElement) {
        NodeList entries = propertiesElement.getChildNodes();
        int numEntries = entries.getLength();
        int start = (numEntries <= 0 || !entries.item(0).getNodeName().equals("comment")) ? 0 : 1;
        for (int i = start; i < numEntries; i++) {
            if (entries.item(i) instanceof Element) {
                Element entry = (Element) entries.item(i);
                if (entry.hasAttribute("key")) {
                    Node n = entry.getFirstChild();
                    props.setProperty(entry.getAttribute("key"), n == null ? "" : n.getNodeValue());
                }
            }
        }
    }

    static void save(Properties props, OutputStream os, String comment, String encoding) throws IOException {
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        Document doc = db.newDocument();
        Element properties = (Element) doc.appendChild(doc.createElement("properties"));
        if (comment != null) {
            ((Element) properties.appendChild(doc.createElement("comment"))).appendChild(doc.createTextNode(comment));
        }
        synchronized (props) {
            for (String key : props.stringPropertyNames()) {
                Element entry = (Element) properties.appendChild(doc.createElement("entry"));
                entry.setAttribute("key", key);
                entry.appendChild(doc.createTextNode(props.getProperty(key)));
            }
        }
        emitDocument(doc, os, encoding);
    }

    static void emitDocument(Document doc, OutputStream os, String encoding) throws IOException {
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("doctype-system", PROPS_DTD_URI);
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("method", "xml");
            transformer.setOutputProperty("encoding", encoding);
        } catch (TransformerConfigurationException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        try {
            transformer.transform(new DOMSource(doc), new StreamResult(os));
        } catch (TransformerException te) {
            IOException ioe = new IOException();
            ioe.initCause(te);
            throw ioe;
        }
    }
}
