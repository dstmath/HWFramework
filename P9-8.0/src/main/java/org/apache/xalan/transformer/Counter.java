package org.apache.xalan.transformer;

import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemNumber;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathContext;

public class Counter {
    static final int MAXCOUNTNODES = 500;
    NodeSetDTM m_countNodes;
    int m_countNodesStartCount = 0;
    int m_countResult;
    int m_fromNode = -1;
    ElemNumber m_numberElem;

    Counter(ElemNumber numberElem, NodeSetDTM countNodes) throws TransformerException {
        this.m_countNodes = countNodes;
        this.m_numberElem = numberElem;
    }

    int getPreviouslyCounted(XPathContext support, int node) {
        int n = this.m_countNodes.size();
        this.m_countResult = 0;
        int i = n - 1;
        while (i >= 0) {
            int countedNode = this.m_countNodes.elementAt(i);
            if (node != countedNode) {
                if (support.getDTM(countedNode).isNodeAfter(countedNode, node)) {
                    break;
                }
                i--;
            } else {
                this.m_countResult = (i + 1) + this.m_countNodesStartCount;
                break;
            }
        }
        return this.m_countResult;
    }

    int getLast() {
        int size = this.m_countNodes.size();
        return size > 0 ? this.m_countNodes.elementAt(size - 1) : -1;
    }
}
