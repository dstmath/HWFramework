package org.apache.xpath.objects;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMManager;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

public class XNodeSetForDOM extends XNodeSet {
    static final long serialVersionUID = -8396190713754624640L;
    Object m_origObj;

    public XNodeSetForDOM(Node node, DTMManager dtmMgr) {
        this.m_dtmMgr = dtmMgr;
        this.m_origObj = node;
        int dtmHandle = dtmMgr.getDTMHandleFromNode(node);
        setObject(new NodeSetDTM(dtmMgr));
        ((NodeSetDTM) this.m_obj).addNode(dtmHandle);
    }

    public XNodeSetForDOM(XNodeSet val) {
        super(val);
        if (val instanceof XNodeSetForDOM) {
            this.m_origObj = ((XNodeSetForDOM) val).m_origObj;
        }
    }

    public XNodeSetForDOM(NodeList nodeList, XPathContext xctxt) {
        this.m_dtmMgr = xctxt.getDTMManager();
        this.m_origObj = nodeList;
        NodeSetDTM nsdtm = new NodeSetDTM(nodeList, xctxt);
        this.m_last = nsdtm.getLength();
        setObject(nsdtm);
    }

    public XNodeSetForDOM(NodeIterator nodeIter, XPathContext xctxt) {
        this.m_dtmMgr = xctxt.getDTMManager();
        this.m_origObj = nodeIter;
        NodeSetDTM nsdtm = new NodeSetDTM(nodeIter, xctxt);
        this.m_last = nsdtm.getLength();
        setObject(nsdtm);
    }

    public Object object() {
        return this.m_origObj;
    }

    public NodeIterator nodeset() throws TransformerException {
        return this.m_origObj instanceof NodeIterator ? (NodeIterator) this.m_origObj : super.nodeset();
    }

    public NodeList nodelist() throws TransformerException {
        return this.m_origObj instanceof NodeList ? (NodeList) this.m_origObj : super.nodelist();
    }
}
