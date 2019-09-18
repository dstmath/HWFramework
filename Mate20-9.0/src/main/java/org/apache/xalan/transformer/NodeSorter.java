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
                        ni.nextNode();
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
        this.m_keys = keys;
        int n = v.getLength();
        Vector nodes = new Vector();
        for (int i = 0; i < n; i++) {
            nodes.addElement(new NodeCompareElem(v.item(i)));
        }
        mergesort(nodes, new Vector(), 0, n - 1, support);
        for (int i2 = 0; i2 < n; i2++) {
            v.setItem(((NodeCompareElem) nodes.elementAt(i2)).m_node, i2);
        }
        v.setCurrentPos(0);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x009c, code lost:
        if (r6.m_descending != false) goto L_0x009e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a1, code lost:
        r8 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a9, code lost:
        if (r6.m_descending != false) goto L_0x00a1;
     */
    public int compare(NodeCompareElem n1, NodeCompareElem n2, int kIndex, XPathContext support) throws TransformerException {
        int result;
        CollationKey n2String;
        CollationKey n1String;
        double n2Num;
        double n1Num;
        double diff;
        NodeCompareElem nodeCompareElem = n1;
        NodeCompareElem nodeCompareElem2 = n2;
        int i = kIndex;
        XPathContext xPathContext = support;
        NodeSortKey k = (NodeSortKey) this.m_keys.elementAt(i);
        int i2 = 0;
        int result2 = -1;
        if (k.m_treatAsNumbers) {
            if (i == 0) {
                n1Num = ((Double) nodeCompareElem.m_key1Value).doubleValue();
                n2Num = ((Double) nodeCompareElem2.m_key1Value).doubleValue();
            } else if (i == 1) {
                n1Num = ((Double) nodeCompareElem.m_key2Value).doubleValue();
                n2Num = ((Double) nodeCompareElem2.m_key2Value).doubleValue();
            } else {
                XObject r1 = k.m_selectPat.execute(this.m_execContext, nodeCompareElem.m_node, k.m_namespaceContext);
                XObject r2 = k.m_selectPat.execute(this.m_execContext, nodeCompareElem2.m_node, k.m_namespaceContext);
                double n1Num2 = r1.num();
                double num = r2.num();
                n1Num = n1Num2;
                n2Num = num;
            }
            if (n1Num != n2Num || i + 1 >= this.m_keys.size()) {
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
                if (diff >= XPath.MATCH_SCORE_QNAME) {
                    if (diff > XPath.MATCH_SCORE_QNAME) {
                    }
                    result = i2;
                }
                i2 = 1;
                result = i2;
            } else {
                result = compare(nodeCompareElem, nodeCompareElem2, i + 1, xPathContext);
            }
        } else {
            if (i == 0) {
                n1String = (CollationKey) nodeCompareElem.m_key1Value;
                n2String = (CollationKey) nodeCompareElem2.m_key1Value;
            } else if (i == 1) {
                n1String = (CollationKey) nodeCompareElem.m_key2Value;
                n2String = (CollationKey) nodeCompareElem2.m_key2Value;
            } else {
                XObject r12 = k.m_selectPat.execute(this.m_execContext, nodeCompareElem.m_node, k.m_namespaceContext);
                XObject r22 = k.m_selectPat.execute(this.m_execContext, nodeCompareElem2.m_node, k.m_namespaceContext);
                CollationKey n1String2 = k.m_col.getCollationKey(r12.str());
                n2String = k.m_col.getCollationKey(r22.str());
                n1String = n1String2;
            }
            result = n1String.compareTo(n2String);
            if (k.m_caseOrderUpper && n1String.getSourceString().toLowerCase().equals(n2String.getSourceString().toLowerCase())) {
                if (result != 0) {
                    i2 = -result;
                }
                result = i2;
            }
            if (k.m_descending) {
                result = -result;
            }
        }
        if (result == 0 && i + 1 < this.m_keys.size()) {
            result = compare(nodeCompareElem, nodeCompareElem2, i + 1, xPathContext);
        }
        if (result != 0) {
            return result;
        }
        if (!xPathContext.getDTM(nodeCompareElem.m_node).isNodeAfter(nodeCompareElem.m_node, nodeCompareElem2.m_node)) {
            result2 = 1;
        }
        return result2;
    }

    /* access modifiers changed from: package-private */
    public void mergesort(Vector a, Vector b, int l, int r, XPathContext support) throws TransformerException {
        int compVal;
        if (r - l > 0) {
            int m = (r + l) / 2;
            Vector vector = a;
            Vector vector2 = b;
            XPathContext xPathContext = support;
            mergesort(vector, vector2, l, m, xPathContext);
            mergesort(vector, vector2, m + 1, r, xPathContext);
            for (int i = m; i >= l; i--) {
                if (i >= b.size()) {
                    b.insertElementAt(a.elementAt(i), i);
                } else {
                    b.setElementAt(a.elementAt(i), i);
                }
            }
            int i2 = l;
            for (int j = m + 1; j <= r; j++) {
                if (((r + m) + 1) - j >= b.size()) {
                    b.insertElementAt(a.elementAt(j), ((r + m) + 1) - j);
                } else {
                    b.setElementAt(a.elementAt(j), ((r + m) + 1) - j);
                }
            }
            int j2 = r;
            int i3 = i2;
            for (int k = l; k <= r; k++) {
                if (i3 == j2) {
                    compVal = -1;
                } else {
                    compVal = compare((NodeCompareElem) b.elementAt(i3), (NodeCompareElem) b.elementAt(j2), 0, support);
                }
                if (compVal < 0) {
                    a.setElementAt(b.elementAt(i3), k);
                    i3++;
                } else if (compVal > 0) {
                    a.setElementAt(b.elementAt(j2), k);
                    j2--;
                }
            }
        }
    }
}
