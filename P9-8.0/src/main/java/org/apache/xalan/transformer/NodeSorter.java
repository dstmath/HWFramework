package org.apache.xalan.transformer;

import java.text.CollationKey;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;

public class NodeSorter {
    XPathContext m_execContext;
    Vector m_keys;

    class NodeCompareElem {
        Object m_key1Value;
        Object m_key2Value;
        int m_node;
        int maxkey = 2;

        NodeCompareElem(int node) throws TransformerException {
            this.m_node = node;
            if (!NodeSorter.this.m_keys.isEmpty()) {
                NodeSortKey k1 = (NodeSortKey) NodeSorter.this.m_keys.elementAt(0);
                XObject r = k1.m_selectPat.execute(NodeSorter.this.m_execContext, node, k1.m_namespaceContext);
                if (k1.m_treatAsNumbers) {
                    this.m_key1Value = new Double(r.num());
                } else {
                    this.m_key1Value = k1.m_col.getCollationKey(r.str());
                }
                if (r.getType() == 4) {
                    DTMIterator ni = ((XNodeSet) r).iterRaw();
                    if (-1 == ni.getCurrentNode()) {
                        int current = ni.nextNode();
                    }
                }
                if (NodeSorter.this.m_keys.size() > 1) {
                    NodeSortKey k2 = (NodeSortKey) NodeSorter.this.m_keys.elementAt(1);
                    XObject r2 = k2.m_selectPat.execute(NodeSorter.this.m_execContext, node, k2.m_namespaceContext);
                    if (k2.m_treatAsNumbers) {
                        this.m_key2Value = new Double(r2.num());
                    } else {
                        this.m_key2Value = k2.m_col.getCollationKey(r2.str());
                    }
                }
            }
        }
    }

    public NodeSorter(XPathContext p) {
        this.m_execContext = p;
    }

    public void sort(DTMIterator v, Vector keys, XPathContext support) throws TransformerException {
        int i;
        this.m_keys = keys;
        int n = v.getLength();
        Vector nodes = new Vector();
        for (i = 0; i < n; i++) {
            nodes.addElement(new NodeCompareElem(v.item(i)));
        }
        mergesort(nodes, new Vector(), 0, n - 1, support);
        for (i = 0; i < n; i++) {
            v.setItem(((NodeCompareElem) nodes.elementAt(i)).m_node, i);
        }
        v.setCurrentPos(0);
    }

    int compare(NodeCompareElem n1, NodeCompareElem n2, int kIndex, XPathContext support) throws TransformerException {
        int result;
        NodeSortKey k = (NodeSortKey) this.m_keys.elementAt(kIndex);
        XObject r1;
        XObject r2;
        if (k.m_treatAsNumbers) {
            double n1Num;
            double n2Num;
            if (kIndex == 0) {
                n1Num = ((Double) n1.m_key1Value).doubleValue();
                n2Num = ((Double) n2.m_key1Value).doubleValue();
            } else if (kIndex == 1) {
                n1Num = ((Double) n1.m_key2Value).doubleValue();
                n2Num = ((Double) n2.m_key2Value).doubleValue();
            } else {
                r1 = k.m_selectPat.execute(this.m_execContext, n1.m_node, k.m_namespaceContext);
                r2 = k.m_selectPat.execute(this.m_execContext, n2.m_node, k.m_namespaceContext);
                n1Num = r1.num();
                n2Num = r2.num();
            }
            if (n1Num != n2Num || kIndex + 1 >= this.m_keys.size()) {
                double diff;
                if (Double.isNaN(n1Num)) {
                    if (Double.isNaN(n2Num)) {
                        diff = XPath.MATCH_SCORE_QNAME;
                    } else {
                        diff = -1.0d;
                    }
                } else if (Double.isNaN(n2Num)) {
                    diff = 1.0d;
                } else {
                    diff = n1Num - n2Num;
                }
                result = diff < XPath.MATCH_SCORE_QNAME ? k.m_descending ? 1 : -1 : diff > XPath.MATCH_SCORE_QNAME ? k.m_descending ? -1 : 1 : 0;
            } else {
                result = compare(n1, n2, kIndex + 1, support);
            }
        } else {
            CollationKey n1String;
            CollationKey n2String;
            if (kIndex == 0) {
                n1String = n1.m_key1Value;
                n2String = (CollationKey) n2.m_key1Value;
            } else if (kIndex == 1) {
                n1String = (CollationKey) n1.m_key2Value;
                n2String = (CollationKey) n2.m_key2Value;
            } else {
                r1 = k.m_selectPat.execute(this.m_execContext, n1.m_node, k.m_namespaceContext);
                r2 = k.m_selectPat.execute(this.m_execContext, n2.m_node, k.m_namespaceContext);
                n1String = k.m_col.getCollationKey(r1.str());
                n2String = k.m_col.getCollationKey(r2.str());
            }
            result = n1String.compareTo(n2String);
            if (k.m_caseOrderUpper && n1String.getSourceString().toLowerCase().equals(n2String.getSourceString().toLowerCase())) {
                result = result == 0 ? 0 : -result;
            }
            if (k.m_descending) {
                result = -result;
            }
        }
        if (result == 0 && kIndex + 1 < this.m_keys.size()) {
            result = compare(n1, n2, kIndex + 1, support);
        }
        if (result != 0) {
            return result;
        }
        return support.getDTM(n1.m_node).isNodeAfter(n1.m_node, n2.m_node) ? -1 : 1;
    }

    void mergesort(Vector a, Vector b, int l, int r, XPathContext support) throws TransformerException {
        if (r - l > 0) {
            int i;
            int j;
            int m = (r + l) / 2;
            mergesort(a, b, l, m, support);
            mergesort(a, b, m + 1, r, support);
            for (i = m; i >= l; i--) {
                if (i >= b.size()) {
                    b.insertElementAt(a.elementAt(i), i);
                } else {
                    b.setElementAt(a.elementAt(i), i);
                }
            }
            i = l;
            for (j = m + 1; j <= r; j++) {
                if (((r + m) + 1) - j >= b.size()) {
                    b.insertElementAt(a.elementAt(j), ((r + m) + 1) - j);
                } else {
                    b.setElementAt(a.elementAt(j), ((r + m) + 1) - j);
                }
            }
            j = r;
            for (int k = l; k <= r; k++) {
                int compVal;
                if (i == j) {
                    compVal = -1;
                } else {
                    compVal = compare((NodeCompareElem) b.elementAt(i), (NodeCompareElem) b.elementAt(j), 0, support);
                }
                if (compVal < 0) {
                    a.setElementAt(b.elementAt(i), k);
                    i++;
                } else if (compVal > 0) {
                    a.setElementAt(b.elementAt(j), k);
                    j--;
                }
            }
        }
    }
}
