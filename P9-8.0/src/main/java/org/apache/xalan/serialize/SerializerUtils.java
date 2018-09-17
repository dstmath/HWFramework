package org.apache.xalan.serialize;

import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.serializer.NamespaceMappings;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.xml.sax.SAXException;

public class SerializerUtils {
    public static void addAttribute(SerializationHandler handler, int attr) throws TransformerException {
        DTM dtm = ((TransformerImpl) handler.getTransformer()).getXPathContext().getDTM(attr);
        if (!isDefinedNSDecl(handler, attr, dtm)) {
            String ns = dtm.getNamespaceURI(attr);
            if (ns == null) {
                ns = "";
            }
            try {
                handler.addAttribute(ns, dtm.getLocalName(attr), dtm.getNodeName(attr), "CDATA", dtm.getNodeValue(attr), false);
            } catch (SAXException e) {
            }
        }
    }

    public static void addAttributes(SerializationHandler handler, int src) throws TransformerException {
        DTM dtm = ((TransformerImpl) handler.getTransformer()).getXPathContext().getDTM(src);
        for (int node = dtm.getFirstAttribute(src); -1 != node; node = dtm.getNextAttribute(node)) {
            addAttribute(handler, node);
        }
    }

    public static void outputResultTreeFragment(SerializationHandler handler, XObject obj, XPathContext support) throws SAXException {
        int doc = obj.rtf();
        DTM dtm = support.getDTM(doc);
        if (dtm != null) {
            int n = dtm.getFirstChild(doc);
            while (-1 != n) {
                handler.flushPending();
                if (dtm.getNodeType(n) == (short) 1 && dtm.getNamespaceURI(n) == null) {
                    handler.startPrefixMapping("", "");
                }
                dtm.dispatchToEvents(n, handler);
                n = dtm.getNextSibling(n);
            }
        }
    }

    public static void processNSDecls(SerializationHandler handler, int src, int type, DTM dtm) throws TransformerException {
        String prefix;
        String desturi;
        String srcURI;
        if (type == 1) {
            try {
                int namespace = dtm.getFirstNamespaceNode(src, true);
                while (-1 != namespace) {
                    prefix = dtm.getNodeNameX(namespace);
                    desturi = handler.getNamespaceURIFromPrefix(prefix);
                    srcURI = dtm.getNodeValue(namespace);
                    if (!srcURI.equalsIgnoreCase(desturi)) {
                        handler.startPrefixMapping(prefix, srcURI, false);
                    }
                    namespace = dtm.getNextNamespaceNode(src, namespace, true);
                }
            } catch (SAXException se) {
                throw new TransformerException(se);
            }
        } else if (type == 13) {
            prefix = dtm.getNodeNameX(src);
            desturi = handler.getNamespaceURIFromPrefix(prefix);
            srcURI = dtm.getNodeValue(src);
            if (!srcURI.equalsIgnoreCase(desturi)) {
                handler.startPrefixMapping(prefix, srcURI, false);
            }
        }
    }

    public static boolean isDefinedNSDecl(SerializationHandler serializer, int attr, DTM dtm) {
        if ((short) 13 == dtm.getNodeType(attr)) {
            String uri = serializer.getNamespaceURIFromPrefix(dtm.getNodeNameX(attr));
            if (uri != null && uri.equals(dtm.getStringValue(attr))) {
                return true;
            }
        }
        return false;
    }

    public static void ensureNamespaceDeclDeclared(SerializationHandler handler, DTM dtm, int namespace) throws SAXException {
        String uri = dtm.getNodeValue(namespace);
        String prefix = dtm.getNodeNameX(namespace);
        if (uri != null && uri.length() > 0 && prefix != null) {
            NamespaceMappings ns = handler.getNamespaceMappings();
            if (ns != null) {
                String foundURI = ns.lookupNamespace(prefix);
                if (foundURI == null || (foundURI.equals(uri) ^ 1) != 0) {
                    handler.startPrefixMapping(prefix, uri, false);
                }
            }
        }
    }
}
