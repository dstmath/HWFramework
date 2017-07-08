package java.util.prefs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.PolicyMappingsExtension;
import sun.security.x509.X509CertInfo;

class XmlSupport {
    private static final String EXTERNAL_XML_VERSION = "1.0";
    private static final String MAP_XML_VERSION = "1.0";
    private static final String PREFS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- DTD for preferences --><!ELEMENT preferences (root) ><!ATTLIST preferences EXTERNAL_XML_VERSION CDATA \"0.0\"  ><!ELEMENT root (map, node*) ><!ATTLIST root          type (system|user) #REQUIRED ><!ELEMENT node (map, node*) ><!ATTLIST node          name CDATA #REQUIRED ><!ELEMENT map (entry*) ><!ATTLIST map  MAP_XML_VERSION CDATA \"0.0\"  ><!ELEMENT entry EMPTY ><!ATTLIST entry          key CDATA #REQUIRED          value CDATA #REQUIRED >";
    private static final String PREFS_DTD_URI = "http://java.sun.com/dtd/preferences.dtd";

    private static class EH implements ErrorHandler {
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
        private Resolver() {
        }

        public InputSource resolveEntity(String pid, String sid) throws SAXException {
            if (sid.equals(XmlSupport.PREFS_DTD_URI)) {
                InputSource is = new InputSource(new StringReader(XmlSupport.PREFS_DTD));
                is.setSystemId(XmlSupport.PREFS_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + sid);
        }
    }

    XmlSupport() {
    }

    static void export(OutputStream os, Preferences p, boolean subTree) throws IOException, BackingStoreException {
        if (((AbstractPreferences) p).isRemoved()) {
            throw new IllegalStateException("Node has been removed");
        }
        Document doc = createPrefsDoc("preferences");
        Element preferences = doc.getDocumentElement();
        preferences.setAttribute("EXTERNAL_XML_VERSION", MAP_XML_VERSION);
        Element xmlRoot = (Element) preferences.appendChild(doc.createElement("root"));
        xmlRoot.setAttribute("type", p.isUserNode() ? "user" : "system");
        List ancestors = new ArrayList();
        Preferences kid = p;
        for (Preferences dad = p.parent(); dad != null; dad = dad.parent()) {
            ancestors.add(kid);
            kid = dad;
        }
        Element e = xmlRoot;
        for (int i = ancestors.size() - 1; i >= 0; i--) {
            e.appendChild(doc.createElement(PolicyMappingsExtension.MAP));
            e = (Element) e.appendChild(doc.createElement("node"));
            e.setAttribute("name", ((Preferences) ancestors.get(i)).name());
        }
        putPreferencesInXml(e, doc, p, subTree);
        writeDoc(doc, os);
    }

    private static void putPreferencesInXml(Element elt, Document doc, Preferences prefs, boolean subTree) throws BackingStoreException {
        Preferences[] preferencesArr = null;
        String[] strArr = null;
        synchronized (((AbstractPreferences) prefs).lock) {
            if (((AbstractPreferences) prefs).isRemoved()) {
                elt.getParentNode().removeChild(elt);
                return;
            }
            int i;
            String[] keys = prefs.keys();
            Element map = (Element) elt.appendChild(doc.createElement(PolicyMappingsExtension.MAP));
            for (i = 0; i < keys.length; i++) {
                Element entry = (Element) map.appendChild(doc.createElement("entry"));
                entry.setAttribute(X509CertInfo.KEY, keys[i]);
                entry.setAttribute(CertificateX509Key.KEY, prefs.get(keys[i], null));
            }
            if (subTree) {
                strArr = prefs.childrenNames();
                preferencesArr = new Preferences[strArr.length];
                for (i = 0; i < strArr.length; i++) {
                    preferencesArr[i] = prefs.node(strArr[i]);
                }
            }
            if (subTree) {
                for (i = 0; i < strArr.length; i++) {
                    Element xmlKid = (Element) elt.appendChild(doc.createElement("node"));
                    xmlKid.setAttribute("name", strArr[i]);
                    putPreferencesInXml(xmlKid, doc, preferencesArr[i], subTree);
                }
            }
        }
    }

    static void importPreferences(InputStream is) throws IOException, InvalidPreferencesFormatException {
        try {
            Document doc = loadPrefsDoc(is);
            String xmlVersion = doc.getDocumentElement().getAttribute("EXTERNAL_XML_VERSION");
            if (xmlVersion.compareTo(MAP_XML_VERSION) > 0) {
                throw new InvalidPreferencesFormatException("Exported preferences file format version " + xmlVersion + " is not supported. This java installation can read" + " versions " + MAP_XML_VERSION + " or older. You may need" + " to install a newer version of JDK.");
            }
            NodeList elements = doc.getDocumentElement().getElementsByTagName("root");
            if (elements == null || elements.getLength() != 1) {
                throw new InvalidPreferencesFormatException("invalid root node");
            }
            Element xmlRoot = (Element) elements.item(0);
            ImportSubtree(xmlRoot.getAttribute("type").equals("user") ? Preferences.userRoot() : Preferences.systemRoot(), xmlRoot);
        } catch (Throwable e) {
            throw new InvalidPreferencesFormatException(e);
        }
    }

    private static Document createPrefsDoc(String qname) {
        try {
            DOMImplementation di = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
            return di.createDocument(null, qname, di.createDocumentType(qname, null, PREFS_DTD_URI));
        } catch (Object e) {
            throw new AssertionError(e);
        }
    }

    private static Document loadPrefsDoc(InputStream in) throws SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new Resolver());
            db.setErrorHandler(new EH());
            return db.parse(new InputSource(in));
        } catch (Object e) {
            throw new AssertionError(e);
        }
    }

    private static final void writeDoc(Document doc, OutputStream out) throws IOException {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                tf.setAttribute("indent-number", new Integer(2));
            } catch (IllegalArgumentException e) {
            }
            Transformer t = tf.newTransformer();
            t.setOutputProperty("doctype-system", doc.getDoctype().getSystemId());
            t.setOutputProperty("indent", "yes");
            t.transform(new DOMSource(doc), new StreamResult(new BufferedWriter(new OutputStreamWriter(out, "UTF-8"))));
        } catch (Object e2) {
            throw new AssertionError(e2);
        }
    }

    private static List<Element> getChildElements(Element node) {
        NodeList xmlKids = node.getChildNodes();
        ArrayList<Element> elements = new ArrayList(xmlKids.getLength());
        for (int i = 0; i < xmlKids.getLength(); i++) {
            if (xmlKids.item(i) instanceof Element) {
                elements.add((Element) xmlKids.item(i));
            }
        }
        return elements;
    }

    private static void ImportSubtree(Preferences prefsNode, Element xmlNode) {
        List<Element> xmlKids = getChildElements(xmlNode);
        synchronized (((AbstractPreferences) prefsNode).lock) {
            if (((AbstractPreferences) prefsNode).isRemoved()) {
                return;
            }
            int i;
            ImportPrefs(prefsNode, (Element) xmlKids.get(0));
            Preferences[] prefsKids = new Preferences[(xmlKids.size() - 1)];
            for (i = 1; i < xmlKids.size(); i++) {
                prefsKids[i - 1] = prefsNode.node(((Element) xmlKids.get(i)).getAttribute("name"));
            }
            for (i = 1; i < xmlKids.size(); i++) {
                ImportSubtree(prefsKids[i - 1], (Element) xmlKids.get(i));
            }
        }
    }

    private static void ImportPrefs(Preferences prefsNode, Element map) {
        List<Element> entries = getChildElements(map);
        int numEntries = entries.size();
        for (int i = 0; i < numEntries; i++) {
            Element entry = (Element) entries.get(i);
            prefsNode.put(entry.getAttribute(X509CertInfo.KEY), entry.getAttribute(CertificateX509Key.KEY));
        }
    }

    static void exportMap(OutputStream os, Map map) throws IOException {
        Document doc = createPrefsDoc(PolicyMappingsExtension.MAP);
        Element xmlMap = doc.getDocumentElement();
        xmlMap.setAttribute("MAP_XML_VERSION", MAP_XML_VERSION);
        for (Entry e : map.entrySet()) {
            Element xe = (Element) xmlMap.appendChild(doc.createElement("entry"));
            xe.setAttribute(X509CertInfo.KEY, (String) e.getKey());
            xe.setAttribute(CertificateX509Key.KEY, (String) e.getValue());
        }
        writeDoc(doc, os);
    }

    static void importMap(InputStream is, Map m) throws IOException, InvalidPreferencesFormatException {
        try {
            Element xmlMap = loadPrefsDoc(is).getDocumentElement();
            String mapVersion = xmlMap.getAttribute("MAP_XML_VERSION");
            if (mapVersion.compareTo(MAP_XML_VERSION) > 0) {
                throw new InvalidPreferencesFormatException("Preferences map file format version " + mapVersion + " is not supported. This java installation can read" + " versions " + MAP_XML_VERSION + " or older. You may need" + " to install a newer version of JDK.");
            }
            NodeList entries = xmlMap.getChildNodes();
            int numEntries = entries.getLength();
            for (int i = 0; i < numEntries; i++) {
                if (entries.item(i) instanceof Element) {
                    Element entry = (Element) entries.item(i);
                    m.put(entry.getAttribute(X509CertInfo.KEY), entry.getAttribute(CertificateX509Key.KEY));
                }
            }
        } catch (Throwable e) {
            throw new InvalidPreferencesFormatException(e);
        }
    }
}
