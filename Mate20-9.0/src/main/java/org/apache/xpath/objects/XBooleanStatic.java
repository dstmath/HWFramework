package org.apache.xpath.objects;

import javax.xml.transform.TransformerException;
import org.apache.xml.utils.WrappedRuntimeException;

public class XBooleanStatic extends XBoolean {
    static final long serialVersionUID = -8064147275772687409L;
    private final boolean m_val;

    public XBooleanStatic(boolean b) {
        super(b);
        this.m_val = b;
    }

    public boolean equals(XObject obj2) {
        try {
            return this.m_val == obj2.bool();
        } catch (TransformerException te) {
            throw new WrappedRuntimeException(te);
        }
    }
}
