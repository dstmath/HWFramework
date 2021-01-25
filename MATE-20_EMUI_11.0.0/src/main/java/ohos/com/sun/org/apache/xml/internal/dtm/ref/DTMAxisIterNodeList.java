package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.utils.IntVector;
import ohos.org.w3c.dom.Node;

public class DTMAxisIterNodeList extends DTMNodeListBase {
    private IntVector m_cachedNodes;
    private DTM m_dtm;
    private DTMAxisIterator m_iter;
    private int m_last = -1;

    private DTMAxisIterNodeList() {
    }

    public DTMAxisIterNodeList(DTM dtm, DTMAxisIterator dTMAxisIterator) {
        if (dTMAxisIterator == null) {
            this.m_last = 0;
        } else {
            this.m_cachedNodes = new IntVector();
            this.m_dtm = dtm;
        }
        this.m_iter = dTMAxisIterator;
    }

    public DTMAxisIterator getDTMAxisIterator() {
        return this.m_iter;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeListBase
    public Node item(int i) {
        if (this.m_iter == null) {
            return null;
        }
        int i2 = 0;
        int size = this.m_cachedNodes.size();
        if (size > i) {
            return this.m_dtm.getNode(this.m_cachedNodes.elementAt(i));
        } else if (this.m_last != -1) {
            return null;
        } else {
            while (size <= i) {
                i2 = this.m_iter.next();
                if (i2 == -1) {
                    break;
                }
                this.m_cachedNodes.addElement(i2);
                size++;
            }
            if (i2 != -1) {
                return this.m_dtm.getNode(i2);
            }
            this.m_last = size;
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeListBase
    public int getLength() {
        if (this.m_last == -1) {
            while (true) {
                int next = this.m_iter.next();
                if (next == -1) {
                    break;
                }
                this.m_cachedNodes.addElement(next);
            }
            this.m_last = this.m_cachedNodes.size();
        }
        return this.m_last;
    }
}
