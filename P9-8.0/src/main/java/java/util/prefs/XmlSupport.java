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
import sun.security.x509.PolicyMappingsExtension;

class XmlSupport {
    private static final String EXTERNAL_XML_VERSION = "1.0";
    private static final String MAP_XML_VERSION = "1.0";
    private static final String PREFS_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- DTD for preferences --><!ELEMENT preferences (root) ><!ATTLIST preferences EXTERNAL_XML_VERSION CDATA \"0.0\"  ><!ELEMENT root (map, node*) ><!ATTLIST root          type (system|user) #REQUIRED ><!ELEMENT node (map, node*) ><!ATTLIST node          name CDATA #REQUIRED ><!ELEMENT map (entry*) ><!ATTLIST map  MAP_XML_VERSION CDATA \"0.0\"  ><!ELEMENT entry EMPTY ><!ATTLIST entry          key CDATA #REQUIRED          value CDATA #REQUIRED >";
    private static final String PREFS_DTD_URI = "http://java.sun.com/dtd/preferences.dtd";

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
        preferences.setAttribute("EXTERNAL_XML_VERSION", "1.0");
        Element xmlRoot = (Element) preferences.appendChild(doc.createElement("root"));
        xmlRoot.setAttribute("type", p.isUserNode() ? "user" : "system");
        List<Preferences> ancestors = new ArrayList();
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

    /* JADX WARNING: Missing block: B:20:0x006e, code:
            if (r15 == false) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:21:0x0070, code:
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:23:0x0072, code:
            if (r2 >= r4.length) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:24:0x0074, code:
            r7 = (org.w3c.dom.Element) r12.appendChild(r13.createElement("node"));
            r7.setAttribute("name", r4[r2]);
            putPreferencesInXml(r7, r13, r5[r2], r15);
            r2 = r2 + 1;
     */
    /* JADX WARNING: Missing block: B:28:0x0094, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void putPreferencesInXml(Element elt, Document doc, Preferences prefs, boolean subTree) throws BackingStoreException {
        Preferences[] kidsCopy = null;
        String[] kidNames = null;
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
                entry.setAttribute("key", keys[i]);
                entry.setAttribute("value", prefs.get(keys[i], null));
            }
            if (subTree) {
                kidNames = prefs.childrenNames();
                kidsCopy = new Preferences[kidNames.length];
                for (i = 0; i < kidNames.length; i++) {
                    kidsCopy[i] = prefs.node(kidNames[i]);
                }
            }
        }
    }

    static void importPreferences(InputStream is) throws IOException, InvalidPreferencesFormatException {
        try {
            Document doc = loadPrefsDoc(is);
            String xmlVersion = doc.getDocumentElement().getAttribute("EXTERNAL_XML_VERSION");
            if (xmlVersion.compareTo("1.0") > 0) {
                throw new InvalidPreferencesFormatException("Exported preferences file format version " + xmlVersion + " is not supported. This java installation can read" + " versions " + "1.0" + " or older. You may need" + " to install a newer version of JDK.");
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

    /* JADX WARNING: Missing block: B:15:0x0048, code:
            r2 = 1;
     */
    /* JADX WARNING: Missing block: B:17:0x004d, code:
            if (r2 >= r5.size()) goto L_0x0062;
     */
    /* JADX WARNING: Missing block: B:18:0x004f, code:
            ImportSubtree(r3[r2 - 1], (org.w3c.dom.Element) r5.get(r2));
            r2 = r2 + 1;
     */
    /* JADX WARNING: Missing block: B:22:0x0062, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void ImportSubtree(Preferences prefsNode, Element xmlNode) {
        List<Element> xmlKids = getChildElements(xmlNode);
        synchronized (((AbstractPreferences) prefsNode).lock) {
            if (((AbstractPreferences) prefsNode).isRemoved()) {
                return;
            }
            ImportPrefs(prefsNode, (Element) xmlKids.get(0));
            Preferences[] prefsKids = new Preferences[(xmlKids.size() - 1)];
            for (int i = 1; i < xmlKids.size(); i++) {
                prefsKids[i - 1] = prefsNode.node(((Element) xmlKids.get(i)).getAttribute("name"));
            }
        }
    }

    private static void ImportPrefs(Preferences prefsNode, Element map) {
        List<Element> entries = getChildElements(map);
        int numEntries = entries.size();
        for (int i = 0; i < numEntries; i++) {
            Element entry = (Element) entries.get(i);
            prefsNode.put(entry.getAttribute("key"), entry.getAttribute("value"));
        }
    }

    static void exportMap(OutputStream os, Map<String, String> map) throws IOException {
        Document doc = createPrefsDoc(PolicyMappingsExtension.MAP);
        Element xmlMap = doc.getDocumentElement();
        xmlMap.setAttribute("MAP_XML_VERSION", "1.0");
        for (Entry<String, String> e : map.entrySet()) {
            Element xe = (Element) xmlMap.appendChild(doc.createElement("entry"));
            xe.setAttribute("key", (String) e.getKey());
            xe.setAttribute("value", (String) e.getValue());
        }
        writeDoc(doc, os);
    }

    static void importMap(InputStream is, Map<String, String> m) throws IOException, InvalidPreferencesFormatException {
        try {
            Element xmlMap = loadPrefsDoc(is).getDocumentElement();
            String mapVersion = xmlMap.getAttribute("MAP_XML_VERSION");
            if (mapVersion.compareTo("1.0") > 0) {
                throw new InvalidPreferencesFormatException("Preferences map file format version " + mapVersion + " is not supported. This java installation can read" + " versions " + "1.0" + " or older. You may need" + " to install a newer version of JDK.");
            }
            NodeList entries = xmlMap.getChildNodes();
            int numEntries = entries.getLength();
            for (int i = 0; i < numEntries; i++) {
                if (entries.item(i) instanceof Element) {
                    Element entry = (Element) entries.item(i);
                    m.put(entry.getAttribute("key"), entry.getAttribute("value"));
                }
            }
        } catch (Throwable e) {
            throw new InvalidPreferencesFormatException(e);
        }
    }
}
