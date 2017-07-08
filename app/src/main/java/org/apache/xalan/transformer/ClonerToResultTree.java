package org.apache.xalan.transformer;

import javax.xml.transform.TransformerException;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.dtm.DTM;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.compiler.OpCodes;
import org.xml.sax.SAXException;

public class ClonerToResultTree {
    public static void cloneToResultTree(int node, int nodeType, DTM dtm, SerializationHandler rth, boolean shouldCloneAttributes) throws TransformerException {
        switch (nodeType) {
            case OpCodes.OP_XPATH /*1*/:
                String ns = dtm.getNamespaceURI(node);
                if (ns == null) {
                    ns = SerializerConstants.EMPTYSTRING;
                }
                rth.startElement(ns, dtm.getLocalName(node), dtm.getNodeNameX(node));
                if (shouldCloneAttributes) {
                    SerializerUtils.addAttributes(rth, node);
                    SerializerUtils.processNSDecls(rth, node, nodeType, dtm);
                    return;
                }
                return;
            case OpCodes.OP_OR /*2*/:
                SerializerUtils.addAttribute(rth, node);
                return;
            case OpCodes.OP_AND /*3*/:
                dtm.dispatchCharactersEvents(node, rth, false);
                return;
            case OpCodes.OP_NOTEQUALS /*4*/:
                rth.startCDATA();
                dtm.dispatchCharactersEvents(node, rth, false);
                rth.endCDATA();
                return;
            case OpCodes.OP_EQUALS /*5*/:
                rth.entityReference(dtm.getNodeNameX(node));
                return;
            case OpCodes.OP_LT /*7*/:
                rth.processingInstruction(dtm.getNodeNameX(node), dtm.getNodeValue(node));
                return;
            case OpCodes.OP_GTE /*8*/:
                dtm.getStringValue(node).dispatchAsComment(rth);
                return;
            case OpCodes.OP_GT /*9*/:
            case OpCodes.OP_MINUS /*11*/:
                return;
            case OpCodes.OP_DIV /*13*/:
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
