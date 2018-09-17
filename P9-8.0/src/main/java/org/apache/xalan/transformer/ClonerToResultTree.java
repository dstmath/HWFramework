package org.apache.xalan.transformer;

import javax.xml.transform.TransformerException;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.dtm.DTM;
import org.apache.xml.serializer.SerializationHandler;
import org.xml.sax.SAXException;

public class ClonerToResultTree {
    public static void cloneToResultTree(int node, int nodeType, DTM dtm, SerializationHandler rth, boolean shouldCloneAttributes) throws TransformerException {
        switch (nodeType) {
            case 1:
                String ns = dtm.getNamespaceURI(node);
                if (ns == null) {
                    ns = "";
                }
                rth.startElement(ns, dtm.getLocalName(node), dtm.getNodeNameX(node));
                if (shouldCloneAttributes) {
                    SerializerUtils.addAttributes(rth, node);
                    SerializerUtils.processNSDecls(rth, node, nodeType, dtm);
                    return;
                }
                return;
            case 2:
                SerializerUtils.addAttribute(rth, node);
                return;
            case 3:
                dtm.dispatchCharactersEvents(node, rth, false);
                return;
            case 4:
                rth.startCDATA();
                dtm.dispatchCharactersEvents(node, rth, false);
                rth.endCDATA();
                return;
            case 5:
                rth.entityReference(dtm.getNodeNameX(node));
                return;
            case 7:
                rth.processingInstruction(dtm.getNodeNameX(node), dtm.getNodeValue(node));
                return;
            case 8:
                dtm.getStringValue(node).dispatchAsComment(rth);
                return;
            case 9:
            case 11:
                return;
            case 13:
                SerializerUtils.processNSDecls(rth, node, 13, dtm);
                return;
            default:
                try {
                    throw new TransformerException("Can't clone node: " + dtm.getNodeName(node));
                } catch (SAXException se) {
                    throw new TransformerException(se);
                }
        }
        throw new TransformerException(se);
    }
}
