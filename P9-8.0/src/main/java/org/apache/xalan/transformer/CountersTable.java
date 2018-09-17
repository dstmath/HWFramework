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

    Vector getCounters(ElemNumber numberElem) {
        Vector counters = (Vector) get(numberElem);
        return counters == null ? putElemNumber(numberElem) : counters;
    }

    Vector putElemNumber(ElemNumber numberElem) {
        Vector counters = new Vector();
        put(numberElem, counters);
        return counters;
    }

    void appendBtoFList(NodeSetDTM flist, NodeSetDTM blist) {
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
            int i;
            Counter counter;
            for (i = 0; i < nCounters; i++) {
                count = ((Counter) counters.elementAt(i)).getPreviouslyCounted(support, target);
                if (count > 0) {
                    return count;
                }
            }
            count = 0;
            if (this.m_newFound == null) {
                this.m_newFound = new NodeSetDTM(support.getDTMManager());
            }
            while (-1 != target) {
                if (count != 0) {
                    i = 0;
                    while (i < nCounters) {
                        counter = (Counter) counters.elementAt(i);
                        int cacheLen = counter.m_countNodes.size();
                        if (cacheLen <= 0 || counter.m_countNodes.elementAt(cacheLen - 1) != target) {
                            i++;
                        } else {
                            count += counter.m_countNodesStartCount + cacheLen;
                            if (cacheLen > 0) {
                                appendBtoFList(counter.m_countNodes, this.m_newFound);
                            }
                            this.m_newFound.removeAllElements();
                            return count;
                        }
                    }
                    continue;
                }
                this.m_newFound.addElement(target);
                count++;
                target = numberElem.getPreviousNode(support, target);
            }
            counter = new Counter(numberElem, new NodeSetDTM(support.getDTMManager()));
            this.m_countersMade++;
            appendBtoFList(counter.m_countNodes, this.m_newFound);
            this.m_newFound.removeAllElements();
            counters.addElement(counter);
        }
        return count;
    }
}
