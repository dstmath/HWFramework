package ohos.com.sun.org.apache.xpath.internal.axes;

import java.io.Serializable;
import java.util.ArrayList;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;

public final class IteratorPool implements Serializable {
    static final long serialVersionUID = -460927331149566998L;
    private final ArrayList m_freeStack = new ArrayList();
    private final DTMIterator m_orig;

    public IteratorPool(DTMIterator dTMIterator) {
        this.m_orig = dTMIterator;
    }

    public synchronized DTMIterator getInstanceOrThrow() throws CloneNotSupportedException {
        if (this.m_freeStack.isEmpty()) {
            return (DTMIterator) this.m_orig.clone();
        }
        return (DTMIterator) this.m_freeStack.remove(this.m_freeStack.size() - 1);
    }

    public synchronized DTMIterator getInstance() {
        if (this.m_freeStack.isEmpty()) {
            try {
                return (DTMIterator) this.m_orig.clone();
            } catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        } else {
            return (DTMIterator) this.m_freeStack.remove(this.m_freeStack.size() - 1);
        }
    }

    public synchronized void freeInstance(DTMIterator dTMIterator) {
        this.m_freeStack.add(dTMIterator);
    }
}
