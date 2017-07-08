package org.apache.xalan.transformer;

import javax.xml.transform.TransformerException;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.DTMTreeWalker;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPathContext;
import org.xml.sax.SAXException;

public class TreeWalker2Result extends DTMTreeWalker {
    SerializationHandler m_handler;
    int m_startNode;
    TransformerImpl m_transformer;

    public TreeWalker2Result(TransformerImpl transformer, SerializationHandler handler) {
        super(handler, null);
        this.m_transformer = transformer;
        this.m_handler = handler;
    }

    public void traverse(int pos) throws SAXException {
        this.m_dtm = this.m_transformer.getXPathContext().getDTM(pos);
        this.m_startNode = pos;
        super.traverse(pos);
    }

    protected void endNode(int node) throws SAXException {
        super.endNode(node);
        if ((short) 1 == this.m_dtm.getNodeType(node)) {
            this.m_transformer.getXPathContext().popCurrentNode();
        }
    }

    protected void startNode(int node) throws SAXException {
        XPathContext xcntxt = this.m_transformer.getXPathContext();
        try {
            if ((short) 1 == this.m_dtm.getNodeType(node)) {
                xcntxt.pushCurrentNode(node);
                if (this.m_startNode != node) {
                    super.startNode(node);
                    return;
                }
                String elemName = this.m_dtm.getNodeName(node);
                String localName = this.m_dtm.getLocalName(node);
                this.m_handler.startElement(this.m_dtm.getNamespaceURI(node), localName, elemName);
                DTM dtm = this.m_dtm;
                int ns = dtm.getFirstNamespaceNode(node, true);
                while (-1 != ns) {
                    SerializerUtils.ensureNamespaceDeclDeclared(this.m_handler, dtm, ns);
                    ns = dtm.getNextNamespaceNode(node, ns, true);
                }
                for (int attr = dtm.getFirstAttribute(node); -1 != attr; attr = dtm.getNextAttribute(attr)) {
                    SerializerUtils.addAttribute(this.m_handler, attr);
                }
                return;
            }
            xcntxt.pushCurrentNode(node);
            super.startNode(node);
            xcntxt.popCurrentNode();
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }
}
