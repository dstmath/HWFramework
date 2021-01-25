package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.org.w3c.dom.Node;

public class DTMNodeList extends DTMNodeListBase {
    private DTMIterator m_iter;

    private DTMNodeList() {
    }

    public DTMNodeList(DTMIterator dTMIterator) {
        if (dTMIterator != null) {
            int currentPos = dTMIterator.getCurrentPos();
            try {
                this.m_iter = dTMIterator.cloneWithReset();
            } catch (CloneNotSupportedException unused) {
                this.m_iter = dTMIterator;
            }
            this.m_iter.setShouldCacheNodes(true);
            this.m_iter.runTo(-1);
            this.m_iter.setCurrentPos(currentPos);
        }
    }

    public DTMIterator getDTMIterator() {
        return this.m_iter;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeListBase
    public Node item(int i) {
        int item;
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator == null || (item = dTMIterator.item(i)) == -1) {
            return null;
        }
        return this.m_iter.getDTM(item).getNode(item);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeListBase
    public int getLength() {
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            return dTMIterator.getLength();
        }
        return 0;
    }
}
