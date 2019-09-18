package org.apache.xalan.transformer;

import java.util.Hashtable;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemNumber;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathContext;

public class CountersTable extends Hashtable {
    static final long serialVersionUID = 2159100770924179875L;
    transient int m_countersMade = 0;
    private transient NodeSetDTM m_newFound;

    /* access modifiers changed from: package-private */
    public Vector getCounters(ElemNumber numberElem) {
        Vector counters = (Vector) get(numberElem);
        return counters == null ? putElemNumber(numberElem) : counters;
    }

    /* access modifiers changed from: package-private */
    public Vector putElemNumber(ElemNumber numberElem) {
        Vector counters = new Vector();
        put(numberElem, counters);
        return counters;
    }

    /* access modifiers changed from: package-private */
    public void appendBtoFList(NodeSetDTM flist, NodeSetDTM blist) {
        for (int i = blist.size() - 1; i >= 0; i--) {
            flist.addElement(blist.item(i));
        }
    }

    public int countNode(XPathContext support, ElemNumber numberElem, int node) throws TransformerException {
        int count = 0;
        Vector counters = getCounters(numberElem);
        int nCounters = counters.size();
        int target = numberElem.getTargetNode(support, node);
        if (-1 != target) {
            for (int i = 0; i < nCounters; i++) {
                int count2 = ((Counter) counters.elementAt(i)).getPreviouslyCounted(support, target);
                if (count2 > 0) {
                    return count2;
                }
            }
            count = 0;
            if (this.m_newFound == null) {
                this.m_newFound = new NodeSetDTM(support.getDTMManager());
            }
            while (-1 != target) {
                if (count != 0) {
                    int i2 = 0;
                    while (i2 < nCounters) {
                        Counter counter = (Counter) counters.elementAt(i2);
                        int cacheLen = counter.m_countNodes.size();
                        if (cacheLen <= 0 || counter.m_countNodes.elementAt(cacheLen - 1) != target) {
                            i2++;
                        } else {
                            int count3 = count + counter.m_countNodesStartCount + cacheLen;
                            if (cacheLen > 0) {
                                appendBtoFList(counter.m_countNodes, this.m_newFound);
                            }
                            this.m_newFound.removeAllElements();
                            return count3;
                        }
                    }
                    continue;
                }
                this.m_newFound.addElement(target);
                count++;
                target = numberElem.getPreviousNode(support, target);
            }
            Counter counter2 = new Counter(numberElem, new NodeSetDTM(support.getDTMManager()));
            this.m_countersMade++;
            appendBtoFList(counter2.m_countNodes, this.m_newFound);
            this.m_newFound.removeAllElements();
            counters.addElement(counter2);
        }
        return count;
    }
}
