package org.apache.xml.dtm.ref;

import org.apache.xml.dtm.DTM;
import org.w3c.dom.Node;

public class DTMChildIterNodeList extends DTMNodeListBase {
    private int m_firstChild;
    private DTM m_parentDTM;

    private DTMChildIterNodeList() {
    }

    public DTMChildIterNodeList(DTM parentDTM, int parentHandle) {
        this.m_parentDTM = parentDTM;
        this.m_firstChild = parentDTM.getFirstChild(parentHandle);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0014  */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0012  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Node item(int index) {
        int handle = this.m_firstChild;
        while (true) {
            index--;
            if (index >= 0 && handle != -1) {
                handle = this.m_parentDTM.getNextSibling(handle);
            } else if (handle != -1) {
                return null;
            } else {
                return this.m_parentDTM.getNode(handle);
            }
        }
        if (handle != -1) {
        }
    }

    public int getLength() {
        int count = 0;
        int handle = this.m_firstChild;
        while (handle != -1) {
            count++;
            handle = this.m_parentDTM.getNextSibling(handle);
        }
        return count;
    }
}
