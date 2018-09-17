package org.apache.xml.dtm.ref;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.utils.IntVector;
import org.w3c.dom.Node;

public class DTMAxisIterNodeList extends DTMNodeListBase {
    private IntVector m_cachedNodes;
    private DTM m_dtm;
    private DTMAxisIterator m_iter;
    private int m_last = -1;

    private DTMAxisIterNodeList() {
    }

    public DTMAxisIterNodeList(DTM dtm, DTMAxisIterator dtmAxisIterator) {
        if (dtmAxisIterator == null) {
            this.m_last = 0;
        } else {
            this.m_cachedNodes = new IntVector();
            this.m_dtm = dtm;
        }
        this.m_iter = dtmAxisIterator;
    }

    public DTMAxisIterator getDTMAxisIterator() {
        return this.m_iter;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Node item(int index) {
        if (this.m_iter != null) {
            int count = this.m_cachedNodes.size();
            if (count > index) {
                return this.m_dtm.getNode(this.m_cachedNodes.elementAt(index));
            } else if (this.m_last == -1) {
                int node;
                while (true) {
                    node = this.m_iter.next();
                    if (node != -1 && count <= index) {
                        this.m_cachedNodes.addElement(node);
                        count++;
                    } else if (node == -1) {
                        return this.m_dtm.getNode(node);
                    } else {
                        this.m_last = count;
                    }
                }
                if (node == -1) {
                }
            }
        }
        return null;
    }

    public int getLength() {
        if (this.m_last == -1) {
            while (true) {
                int node = this.m_iter.next();
                if (node == -1) {
                    break;
                }
                this.m_cachedNodes.addElement(node);
            }
            this.m_last = this.m_cachedNodes.size();
        }
        return this.m_last;
    }
}
