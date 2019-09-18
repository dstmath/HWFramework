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

    /* access modifiers changed from: package-private */
    public int getPreviouslyCounted(XPathContext support, int node) {
        int n = this.m_countNodes.size();
        this.m_countResult = 0;
        int i = n - 1;
        while (true) {
            if (i < 0) {
                break;
            }
            int countedNode = this.m_countNodes.elementAt(i);
            if (node == countedNode) {
                this.m_countResult = i + 1 + this.m_countNodesStartCount;
                break;
            } else if (support.getDTM(countedNode).isNodeAfter(countedNode, node)) {
                break;
            } else {
                i--;
            }
        }
        return this.m_countResult;
    }

    /* access modifiers changed from: package-private */
    public int getLast() {
        int size = this.m_countNodes.size();
        if (size > 0) {
            return this.m_countNodes.elementAt(size - 1);
        }
        return -1;
    }
}
